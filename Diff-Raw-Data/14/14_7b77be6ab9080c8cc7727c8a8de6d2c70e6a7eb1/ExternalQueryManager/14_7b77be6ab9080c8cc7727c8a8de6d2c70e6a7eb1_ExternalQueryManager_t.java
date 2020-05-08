 /**
  * This file is part of Jahia, next-generation open source CMS:
  * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
  * of enterprise application convergence - web, search, document, social and portal -
  * unified by the simplicity of web content management.
  *
  * For more information, please visit http://www.jahia.com.
  *
  * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
  *
  * As a special exception to the terms and conditions of version 2.0 of
  * the GPL (or any later version), you may redistribute this Program in connection
  * with Free/Libre and Open Source Software ("FLOSS") applications as described
  * in Jahia's FLOSS exception. You should have received a copy of the text
  * describing the FLOSS exception, and it is also available here:
  * http://www.jahia.com/license
  *
  * Commercial and Supported Versions of the program (dual licensing):
  * alternatively, commercial and supported versions of the program may be used
  * in accordance with the terms and conditions contained in a separate
  * written agreement between you and Jahia Solutions Group SA.
  *
  * If you are unsure which license is appropriate for your use,
  * please contact the sales department at sales@jahia.com.
  */
 
 package org.jahia.modules.external.query;
 
 import org.apache.jackrabbit.commons.query.sql2.Parser;
 import org.apache.jackrabbit.spi.commons.conversion.NamePathResolver;
 import org.apache.jackrabbit.spi.commons.query.qom.QueryObjectModelFactoryImpl;
 import org.apache.jackrabbit.spi.commons.query.qom.QueryObjectModelTree;
 import org.jahia.modules.external.ExternalDataSource;
 import org.jahia.modules.external.ExternalQuery;
 import org.jahia.modules.external.ExternalWorkspaceImpl;
 import org.jahia.services.content.nodetypes.ExtendedNodeType;
 import org.jahia.services.content.nodetypes.NodeTypeRegistry;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.jcr.*;
 import javax.jcr.nodetype.NoSuchNodeTypeException;
 import javax.jcr.query.InvalidQueryException;
 import javax.jcr.query.Query;
 import javax.jcr.query.QueryManager;
 import javax.jcr.query.QueryResult;
 import javax.jcr.query.qom.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Implementation of the {@link javax.jcr.query.QueryManager} for the {@link org.jahia.modules.external.ExternalData}.
  */
 public class ExternalQueryManager implements QueryManager {
     private static final String[] SUPPORTED_LANGUAGES = new String[]{Query.JCR_SQL2};
 
     private static Logger logger = LoggerFactory.getLogger(ExternalQueryManager.class);
 
     private ExternalWorkspaceImpl workspace;
 
     public ExternalQueryManager(ExternalWorkspaceImpl workspace) {
         this.workspace = workspace;
     }
 
     public Query createQuery(String statement, String language) throws InvalidQueryException, RepositoryException {
         if (!language.equals(Query.JCR_SQL2)) {
             throw new InvalidQueryException("Unsupported query language");
         }
         Parser p = new Parser(getQOMFactory(), workspace.getSession().getValueFactory());
         return p.createQueryObjectModel(statement);
     }
 
     public QueryObjectModelFactory getQOMFactory() {
         return new MyQOMFactory(workspace.getSession().getRepository().getNamePathResolver());
     }
 
     public Query getQuery(Node node) throws InvalidQueryException, RepositoryException {
         return null;
     }
 
     public String[] getSupportedQueryLanguages() throws RepositoryException {
         return SUPPORTED_LANGUAGES;
     }
 
     class MyQOMFactory extends QueryObjectModelFactoryImpl implements QueryObjectModelFactory {
         MyQOMFactory(NamePathResolver resolver) {
             super(resolver);
         }
 
         @Override
         protected QueryObjectModel createQuery(QueryObjectModelTree qomTree) throws InvalidQueryException, RepositoryException {
             boolean nodeTypeSupported = isNodeTypeSupported(qomTree);
             boolean hasExtension = workspace.getSession().getExtensionSession() != null;
             if (!nodeTypeSupported && !hasExtension) {
                 return null;
             }
             return new ExecutableExternalQuery(qomTree.getSource(), qomTree.getConstraint(), qomTree.getOrderings(), qomTree.getColumns(), nodeTypeSupported, hasExtension);
         }
 
         private boolean isNodeTypeSupported(QueryObjectModelTree qomTree) throws NoSuchNodeTypeException {
             if (!(qomTree.getSource() instanceof Selector)) {
                 return false;
             }
 
             NodeTypeRegistry ntRegistry = NodeTypeRegistry.getInstance();
             ExtendedNodeType type = ntRegistry.getNodeType(((Selector) qomTree.getSource()).getNodeTypeName());
 
             // check supported node types
             String nodeType = type.getName();
             Set<String> supportedNodeTypes = workspace.getSession().getRepository().getDataSource()
                     .getSupportedNodeTypes();
             if (supportedNodeTypes.contains(nodeType)) {
                 return true;
             }
             for (String supportedNodeType : supportedNodeTypes) {
                 if (ntRegistry.getNodeType(supportedNodeType).isNodeType(nodeType)) {
                     return true;
                 }
             }
 
             return false;
         }
 
     }
 
     class ExecutableExternalQuery extends ExternalQuery {
         private boolean nodeTypeSupported;
         private boolean hasExtension;
 
 
         ExecutableExternalQuery(Source source, Constraint constraints, Ordering[] orderings, Column[] columns, boolean nodeTypeSupported, boolean hasExtension) {
             super(source, constraints, orderings, columns);
             this.nodeTypeSupported = nodeTypeSupported;
             this.hasExtension = hasExtension;
         }
 
         @Override
         public QueryResult execute() throws InvalidQueryException, RepositoryException {
             List<String> allExtendedResults = new ArrayList<String>();
             List<String> results = null;
             if (hasExtension) {
                 Session extSession = workspace.getSession().getExtensionSession();
                 QueryManager queryManager = extSession.getWorkspace().getQueryManager();
                 QueryObjectModelFactory qomFactory = queryManager.getQOMFactory();
                 String mountPoint = workspace.getSession().getRepository().getStoreProvider().getMountPoint();
                 Constraint convertedConstraint = convertExistingPathConstraints(getConstraint(), mountPoint, qomFactory);
                if (!(convertedConstraint instanceof DescendantNode)) {
                    // Multiple IsDescendantNode queries are not supported
                    convertedConstraint = addPathConstraints(convertedConstraint, getSource(), mountPoint, qomFactory);
                }
                 Query q = qomFactory.createQuery(getSource(), convertedConstraint, getOrderings(), getColumns());
                 if (!nodeTypeSupported) {
                     // Query is only done in JCR, directly pass limit and offset
                     if (getLimit() > -1) {
                         q.setLimit(getLimit());
                     }
                     q.setOffset(getOffset());
                     NodeIterator nodes = q.execute().getNodes();
                     while (nodes.hasNext()) {
                         Node node = (Node) nodes.next();
                         allExtendedResults.add(node.getPath().substring(mountPoint.length()));
                     }
                     results = allExtendedResults;
                 } else {
                     // Need to get all results to prepare merge
                     NodeIterator nodes = q.execute().getNodes();
                     while (nodes.hasNext()) {
                         Node node = (Node) nodes.next();
                         allExtendedResults.add(node.getPath().substring(mountPoint.length()));
                     }
                     if (allExtendedResults.size() == 0) {
                         // No results at all, ignore search in extension
                         results = null;
                     } else if (getOffset() >= allExtendedResults.size()) {
                         // Offset greater than results here - return an empty result list, still need to merge results
                         results = new ArrayList<String>();
                     } else if (getLimit() > -1) {
                         // Strip results to limit and offset
                         results = allExtendedResults.subList((int) getOffset(), Math.min((int) getLimit(), allExtendedResults.size()));
                     } else if (getOffset() > 0) {
                         // Use offset
                         results = allExtendedResults.subList((int) getOffset(), allExtendedResults.size());
                     } else {
                         // Retuen all results
                         results = allExtendedResults;
                     }
                 }
             }
             if (nodeTypeSupported && (getLimit() == -1 || results == null || results.size() < getLimit())) {
                 ExternalDataSource dataSource = workspace.getSession().getRepository().getDataSource();
                 try {
                     if (getLimit() > -1 && results != null) {
                         // Remove results found. Extend limit with total size of extended result to skip duplicate results
                         setLimit(getLimit() - results.size() + allExtendedResults.size());
                     }
 
                     if (results == null) {
                         // No previous results, no merge to do
                         results = ((ExternalDataSource.Searchable) dataSource).search(this);
                     } else {
                         // Need to merge, move offset to 0
                         int skips = Math.max(0,(int) getOffset() - allExtendedResults.size());
                         setOffset(0);
                         List<String> providerResult = ((ExternalDataSource.Searchable) dataSource).search(this);
                         for (String s : providerResult) {
                             // Skip duplicate result
                             if (!allExtendedResults.contains(s)) {
                                 if (skips > 0) {
                                     skips--;
                                 } else {
                                     results.add(s);
                                 }
                             }
                         }
                     }
                     // Finally , strip results to limit
                     if (getLimit() > -1 && results.size() > getLimit()) {
                         results = results.subList(0, (int) getLimit());
                     }
                 } catch (UnsupportedRepositoryOperationException e) {
                     logger.warn("Unsupported query ", e);
                 }
             }
             return new ExternalQueryResult(this, results, workspace);
         }
 
         private Constraint addPathConstraints(Constraint constraint, Source source, String mountPoint, QueryObjectModelFactory f) throws RepositoryException {
             if (source instanceof Selector) {
                 DescendantNode descendantNode = f.descendantNode(((Selector) source).getSelectorName(), mountPoint);
                 if (constraint == null) {
                     constraint = descendantNode;
                 } else {
                     constraint = f.and(constraint, descendantNode);
                 }
             } else if (source instanceof Join) {
                 constraint = addPathConstraints(constraint, ((Join) source).getLeft(), mountPoint, f);
                 constraint = addPathConstraints(constraint, ((Join) source).getRight(), mountPoint, f);
             }
             return constraint;
         }
 
         private Constraint convertExistingPathConstraints(Constraint constraint, String mountPoint, QueryObjectModelFactory f) throws RepositoryException {
             if (constraint instanceof ChildNode) {
                 String root = ((ChildNode) constraint).getParentPath();
                 // Path constraint is under mount point -> create new constraint with local path
                 return f.childNode(((ChildNode) constraint).getSelectorName(), mountPoint + root);
             } else if (constraint instanceof DescendantNode) {
                 String root = ((DescendantNode) constraint).getAncestorPath();
                 return f.descendantNode(((DescendantNode) constraint).getSelectorName(), mountPoint + root);
             } else if (constraint instanceof And) {
                 Constraint c1 = convertExistingPathConstraints(((And) constraint).getConstraint1(), mountPoint, f);
                 Constraint c2 = convertExistingPathConstraints(((And) constraint).getConstraint2(), mountPoint, f);
                 return f.and(c1, c2);
             } else if (constraint instanceof Or) {
                 Constraint c1 = convertExistingPathConstraints(((Or) constraint).getConstraint1(), mountPoint, f);
                 Constraint c2 = convertExistingPathConstraints(((Or) constraint).getConstraint2(), mountPoint, f);
                 return f.or(c1, c2);
             } else if (constraint instanceof Not) {
                 return f.not(convertExistingPathConstraints(((Not) constraint).getConstraint(), mountPoint, f));
             }
             return constraint;
         }
 
 
     }
 }
