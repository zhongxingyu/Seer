 package org.jboss.pressgang.ccms.provider;
 
 import javax.persistence.EntityManager;
 import javax.persistence.criteria.CriteriaQuery;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.jboss.pressgang.ccms.filter.ContentSpecNodeFieldFilter;
 import org.jboss.pressgang.ccms.filter.builder.ContentSpecNodeFilterQueryBuilder;
 import org.jboss.pressgang.ccms.filter.utils.EntityUtilities;
 import org.jboss.pressgang.ccms.filter.utils.FilterUtilities;
 import org.jboss.pressgang.ccms.model.Filter;
 import org.jboss.pressgang.ccms.model.contentspec.CSNode;
 import org.jboss.pressgang.ccms.model.contentspec.CSNodeToCSNode;
 import org.jboss.pressgang.ccms.provider.listener.ProviderListener;
 import org.jboss.pressgang.ccms.utils.constants.CommonFilterConstants;
 import org.jboss.pressgang.ccms.wrapper.CSNodeWrapper;
 import org.jboss.pressgang.ccms.wrapper.CSRelatedNodeWrapper;
 import org.jboss.pressgang.ccms.wrapper.DBCSNodeWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.UpdateableCollectionWrapper;
 
 public class DBCSNodeProvider extends DBDataProvider implements CSNodeProvider {
     protected DBCSNodeProvider(EntityManager entityManager, DBProviderFactory providerFactory, List<ProviderListener> listeners) {
         super(entityManager, providerFactory, listeners);
     }
 
     @Override
     public CSNodeWrapper getCSNode(int id) {
         return getWrapperFactory().create(getEntity(CSNode.class, id), false);
     }
 
     @Override
     public CSNodeWrapper getCSNode(int id, Integer revision) {
         if (revision == null) {
             return getCSNode(id);
         } else {
             return getWrapperFactory().create(getRevisionEntity(CSNode.class, id, revision), true);
         }
     }
 
     @Override
     public CollectionWrapper<CSNodeWrapper> getCSNodesWithQuery(final String query) {
         final String fixedQuery = query.replace("query;", "");
         final String[] queryValues = fixedQuery.split(";");
         final Map<String, String> queryParameters = new HashMap<String, String>();
         for (final String value : queryValues) {
             if (value.trim().isEmpty()) continue;
             String[] keyValue = value.split("=", 2);
             try {
                 queryParameters.put(keyValue[0], URLDecoder.decode(keyValue[1], "UTF-8"));
             } catch (UnsupportedEncodingException e) {
                 // Should support UTF-8, if not throw a runtime error.
                 throw new RuntimeException(e);
             }
         }
 
         final Filter filter = EntityUtilities.populateFilter(getEntityManager(), queryParameters, CommonFilterConstants.FILTER_ID, null,
                 null, null, null, CommonFilterConstants.MATCH_LOCALE, new ContentSpecNodeFieldFilter());
 
         final ContentSpecNodeFilterQueryBuilder queryBuilder = new ContentSpecNodeFilterQueryBuilder(getEntityManager());
         final CriteriaQuery<CSNode> criteriaQuery = FilterUtilities.buildQuery(filter, queryBuilder);
 
         return getWrapperFactory().createCollection(executeQuery(criteriaQuery), CSNode.class, false, CSNodeWrapper.class);
     }
 
     @Override
     public CollectionWrapper<CSRelatedNodeWrapper> getCSRelatedToNodes(int id, Integer revision) {
         final DBCSNodeWrapper contentSpec = (DBCSNodeWrapper) getCSNode(id, revision);
         if (contentSpec == null) {
             return null;
         } else {
             return getWrapperFactory().createCollection(contentSpec.unwrap().getRelatedToNodes(), CSNodeToCSNode.class, revision != null);
         }
     }
 
     @Override
     public CollectionWrapper<CSRelatedNodeWrapper> getCSRelatedFromNodes(int id, Integer revision) {
         final DBCSNodeWrapper contentSpec = (DBCSNodeWrapper) getCSNode(id, revision);
         if (contentSpec == null) {
             return null;
         } else {
             return getWrapperFactory().createCollection(contentSpec.unwrap().getRelatedFromNodes(), CSNodeToCSNode.class, revision != null);
         }
     }
 
     @Override
     public UpdateableCollectionWrapper<CSNodeWrapper> getCSNodeChildren(int id, Integer revision) {
         final DBCSNodeWrapper contentSpec = (DBCSNodeWrapper) getCSNode(id, revision);
         if (contentSpec == null) {
             return null;
         } else {
             final CollectionWrapper<CSNodeWrapper> collection = getWrapperFactory().createCollection(contentSpec.unwrap().getChildren(),
                     CSNode.class, revision != null);
             return (UpdateableCollectionWrapper<CSNodeWrapper>) collection;
         }
     }
 
     @Override
     public CollectionWrapper<CSNodeWrapper> getCSNodeRevisions(int id, Integer revision) {
         final List<CSNode> revisions = getRevisionList(CSNode.class, id);
         return getWrapperFactory().createCollection(revisions, CSNode.class, revision != null);
     }
 
     @Override
     public CSNodeWrapper createCSNode(CSNodeWrapper node) {
         // Send notification events
         notifyCreateEntity(node);
 
         // Persist the changes
         getEntityManager().persist(node.unwrap());
 
         // Flush the changes to the database
         getEntityManager().flush();
 
         return node;
     }
 
     @Override
     public CSNodeWrapper updateCSNode(CSNodeWrapper node) {
         // Send notification events
         notifyUpdateEntity(node);
 
         // Persist the changes
         getEntityManager().persist(node.unwrap());
 
         // Flush the changes to the database
         getEntityManager().flush();
 
         return node;
     }
 
     @Override
     public boolean deleteCSNode(int id) {
         notifyDeleteEntity(CSNode.class, id);
 
         final CSNode node = getEntityManager().find(CSNode.class, id);

        if (node.getContentSpec() != null) {
            node.getContentSpec().removeChild(node);
        }

         getEntityManager().remove(node);
 
         // Flush the changes to the database
         getEntityManager().flush();
 
         return true;
     }
 
     @Override
     public UpdateableCollectionWrapper<CSNodeWrapper> createCSNodes(UpdateableCollectionWrapper<CSNodeWrapper> nodes) {
         // Send notification events
         notifyCreateEntityCollection(nodes);
 
         // Persist the new entities
         for (final CSNodeWrapper node : nodes.getItems()) {
             getEntityManager().persist(node.unwrap());
         }
 
         // Flush the changes to the database
         getEntityManager().flush();
 
         return nodes;
     }
 
     @Override
     public UpdateableCollectionWrapper<CSNodeWrapper> updateCSNodes(UpdateableCollectionWrapper<CSNodeWrapper> nodes) {
         // Send notification events
         notifyUpdateEntityCollection(nodes);
 
         // Persist the new entities
         for (final CSNodeWrapper node : nodes.getItems()) {
             getEntityManager().persist(node.unwrap());
         }
 
         // Flush the changes to the database
         getEntityManager().flush();
 
         return nodes;
     }
 
     @Override
     public boolean deleteCSNodes(final List<Integer> nodeIds) {
         // Send notification events
         notifyDeleteEntityCollection(CSNode.class, nodeIds);
 
         // Delete the entities
         for (final Integer id : nodeIds) {
             final CSNode node = getEntityManager().find(CSNode.class, id);

            if (node.getContentSpec() != null) {
                node.getContentSpec().removeChild(node);
            }

             getEntityManager().remove(node);
         }
 
         // Flush the changes to the database
         getEntityManager().flush();
 
         return true;
     }
 
     @Override
     public CSNodeWrapper newCSNode() {
         return getWrapperFactory().create(new CSNode(), false, CSNodeWrapper.class);
     }
 
     @Override
     public UpdateableCollectionWrapper<CSNodeWrapper> newCSNodeCollection() {
         final CollectionWrapper<CSNodeWrapper> collection = getWrapperFactory().createCollection(new ArrayList<CSNode>(), CSNode.class,
                 false, CSNodeWrapper.class);
         return (UpdateableCollectionWrapper<CSNodeWrapper>) collection;
     }
 
     @Override
     public CSRelatedNodeWrapper newCSRelatedNode() {
         return getWrapperFactory().create(new CSNodeToCSNode(), false, CSRelatedNodeWrapper.class);
     }
 
     @Override
     public CSRelatedNodeWrapper newCSRelatedNode(CSNodeWrapper node) {
         final CSNodeToCSNode joiner = new CSNodeToCSNode();
         joiner.setRelatedNode((CSNode) node.unwrap());
 
         return getWrapperFactory().create(joiner, false, CSRelatedNodeWrapper.class);
     }
 
     @Override
     public UpdateableCollectionWrapper<CSRelatedNodeWrapper> newCSRelatedNodeCollection() {
         final CollectionWrapper<CSRelatedNodeWrapper> collection = getWrapperFactory().createCollection(new ArrayList<CSNodeToCSNode>(),
                 CSNodeToCSNode.class, false, CSRelatedNodeWrapper.class);
         return (UpdateableCollectionWrapper<CSRelatedNodeWrapper>) collection;
     }
 }
