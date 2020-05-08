 package org.mule.galaxy.impl.jcr;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import javax.jcr.query.Query;
 import javax.jcr.query.QueryManager;
 import javax.jcr.query.QueryResult;
 import javax.xml.namespace.QName;
 
 import org.springmodules.jcr.JcrCallback;
 
 import org.mule.galaxy.ArtifactType;
 import org.mule.galaxy.ArtifactTypeDao;
 import org.mule.galaxy.impl.jcr.onm.AbstractReflectionDao;
 
 public class ArtifactTypeDaoImpl extends AbstractReflectionDao<ArtifactType> 
     implements ArtifactTypeDao {
     
     private ArtifactType defaultArtifactType;
     
     public ArtifactTypeDaoImpl() throws Exception {
         super(ArtifactType.class, "artifactTypes", true);
     }
 
     public ArtifactType getArtifactType(final String contentType, final QName documentType) {
         return (ArtifactType)execute(new JcrCallback() {
             public Object doInJcr(Session session) throws IOException, RepositoryException {
 
                 QueryManager qm = getQueryManager(session);
                 StringBuilder query = new StringBuilder();
                 query.append("/*/artifactTypes/*");
 
                 if (documentType != null) {
                     query.append("[@documentTypes = ")
                          .append(JcrUtil.stringToJCRSearchExp(documentType.toString()))
                          .append("]");
                 } else {
                     query.append("[@contentType=")
                          .append(JcrUtil.stringToJCRSearchExp(contentType))
                          .append("]");
                 }
                 
                 Query q = qm.createQuery(query.toString(), Query.XPATH);
 
                 QueryResult qr = q.execute();
 
                 NodeIterator nodes = qr.getNodes();
                 if (!nodes.hasNext()) {
                     if (documentType != null) {
                         // fall back to content type
                         return getArtifactType(contentType, null);
                    } else if (!"*/*".equals(contentType)) {
                         return getDefaultArtifactType();
                    } else {
                        return null;
                     }
                 }
 
                 Node node = nodes.nextNode();
 
                 try {
                     return build(node, session);
                 } catch (Exception e) {
                     throw new RuntimeException(e);
                 }
             }
         });
     }
 
     protected List<ArtifactType> doListAll(Session session) throws RepositoryException {
         List<ArtifactType> types = super.doListAll(session);
         
         Collections.sort(types, new Comparator<ArtifactType>() {
             public int compare(ArtifactType o1, ArtifactType o2) {
                 return o1.getDescription().compareTo(o2.getDescription());
             }
         });
         
         return types;
     }
 
     public ArtifactType getDefaultArtifactType() {
         if (defaultArtifactType == null) {
             defaultArtifactType = getArtifactType("*/*", null);
         }
         return defaultArtifactType;
     }
 }
