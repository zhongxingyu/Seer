 package org.wyona.yarep.util;
 
 import org.apache.log4j.Logger;
 import org.wyona.yarep.core.Node;
 import org.wyona.yarep.core.Path;
 import org.wyona.yarep.core.Repository;
 import org.wyona.yarep.core.RepositoryException;
 import org.wyona.yarep.core.RepositoryFactory;
 import org.wyona.yarep.core.Revision;
 
 import java.util.Date;
 
 /**
  * Various yarep utility methods
  */
 public class YarepUtil {
 
     private static Logger log = Logger.getLogger(YarepUtil.class);
 
     /**
      * Get revision of a specific node for a specific date (or just before)
      * (also see http://en.wikipedia.org/wiki/Point-in-time_recovery)
      *
      * @param node Yarep node for which a specific revision shall be found
      * @param pointInTime Date for which a revision shall be found, whereas the creation date of the revision is equals or older
      */
     public static Revision getRevision(Node node, Date pointInTime) throws RepositoryException {
         String path = null;
         try {
             path = node.getPath();
             // INFO: Find the revision which was the current revision at (or before) the given date
             // IMPORTANT TODO: Improve this algorithm re performance/scalability
             Revision[] revisions = node.getRevisions();
             if (log.isDebugEnabled()) log.debug("Trying to find revision for node " + node.getPath() + " at time " + pointInTime);
             for (int i = revisions.length - 1; i >= 0; i--) {
                 //if (log.isDebugEnabled()) log.debug("Checking revision: " + revisions[i].getName() + " " + revisions[i].getCreationDate());
                 Date creationDate = revisions[i].getCreationDate();
                 if (creationDate.before(pointInTime) || creationDate.equals(pointInTime)) {
                    if (log.isDebugEnabled()) log.debug("Revision found: " + revisions[i].getName());
                     return revisions[i];
                 }
             }
             // TODO: what should happen in this case?
             log.warn("No revision found for node " + path + " and date " + pointInTime);
             return null;
         } catch (Exception e) {
             log.error(e, e);
             throw new RepositoryException("No revision found for node " + path + " and date " + pointInTime + ": " + e.getMessage(), e);
         }
     }
 
     /**
      *
      */
     public RepoPath getRepositoryPath(Path path, RepositoryFactory repoFactory) throws RepositoryException {
         Repository repo = null;
 
         // Determine possible Repository ID. If such a repo ID doesn't exist, then use ROOT repository
         String[] splittedPath = path.toString().split("/");
         if (splittedPath != null) {
             if (splittedPath.length < 2) {
 	        log.debug("Length = " + splittedPath.length + ". Use ROOT repository.");
             } else {
                 if (repoFactory.exists(splittedPath[1])) {
                     repo = repoFactory.newRepository(splittedPath[1]);
                     log.debug("New Repository: " + repo.getID() + " - " + repo.getName());
 
                     log.debug("Repo ID length: " + repo.getID().length());
                     path = new Path(path.toString().substring(repo.getID().length() + 1));
                     log.debug("New Path: " + path);
                     return new RepoPath(repo, path);
                 } else {
                     log.debug("No such repository \"" + splittedPath[1] + "\". Use ROOT repository.");
                 }
             }
         } else {
             log.debug("Path could not be split. Use ROOT repository.");
         }
 
         // First repository shall be ROOT repository
         repo = repoFactory.firstRepository();
         log.debug("ROOT Repository: " + repo.getID() + " - " + repo.getName());
 
         log.debug("Path (still original): " + path);
         return new RepoPath(repo, path);
     }
     
     /**
      * Copies the content of one repository into another repository.
      * Currently copies nodes and properties, but no revisions.
      * @param srcRepo repository to be copied
      * @param destRepo assumed to be empty
      * @throws RepositoryException
      */
     public static void copyRepository(Repository srcRepo, Repository destRepo) throws RepositoryException {
         Node srcRootNode = srcRepo.getRootNode(); 
         
         Node[] childNodes = srcRootNode.getNodes();
         for (int i = 0; i < childNodes.length; i++) {
             importNodeRec(childNodes[i], srcRepo, destRepo);
         }
     }
     
     /**
      * Imports nodes recursively
      * @param srcNode Source node
      * @param srcRepo Source repository
      * @param destRepo Destination repository
      * @throws RepositoryException
      */
     protected static void importNodeRec(Node srcNode, Repository srcRepo, Repository destRepo) throws RepositoryException {
         try {
             String destPath = srcNode.getPath();
             if (log.isInfoEnabled()) log.info("Importing node to "+destPath+"...");
             destRepo.importNode(destPath, srcNode.getPath(), srcRepo);
             // recursively import children
             Node[] childNodes = srcNode.getNodes();
             for (int i = 0; i < childNodes.length; i++) {
                 importNodeRec(childNodes[i], srcRepo, destRepo);
             }
         } catch (Exception e) {
             //throw new RepositoryException(e.getMessage(), e);
             log.error("Could not import node: " + srcNode.getPath() + ": " + e.getMessage(), e);
         }
     }
 
     /**
      * Creates the node named by this abstract pathname, including any necessary but nonexistent parent nodes (similar to java.io.File.mkdirs()).
      */
     public static Node addNodes(Repository repo, String path, int nodeType) throws RepositoryException {
         if (repo.existsNode(path)) {
             return repo.getNode(path);
         } else {
             org.wyona.commons.io.Path parentPath = new org.wyona.commons.io.Path(path).getParent();
             if (parentPath != null) {
                 Node parentNode = null;
                 if (repo.existsNode(parentPath.toString())) {
                     parentNode = repo.getNode(parentPath.toString());
                 } else {
                     parentNode = addNodes(repo, parentPath.toString(), org.wyona.yarep.core.NodeType.COLLECTION);
                 }
                 return parentNode.addNode(new Path(path).getName().toString(), nodeType);
             } else {
                 throw new RepositoryException("Root node does not have a parent!");
             }
         }
     }
 
     /**
      * Copy yarep node
      * @param repo Data repository
      * @param source Path of source node (to be copied)
      * @param destination Path of destination node (copy of orginial node)
      * @return Destination (copy of) node
      */
     public Node copyNode(Repository repo, String source, String destination) throws RepositoryException, java.io.IOException {
         log.warn("DEBUG: Copy node from '" + source + "' to '" + destination + "'.");
         if (!repo.existsNode(source)) throw new RepositoryException("No such source node: " + source);
         Node src = repo.getNode(source);
         Node dest = YarepUtil.addNodes(repo, destination, org.wyona.yarep.core.NodeType.RESOURCE);
 
         byte buffer[] = new byte[8192];
         int bytesRead;
         java.io.InputStream in = src.getInputStream();
         java.io.OutputStream out = dest.getOutputStream();
         while ((bytesRead = in.read(buffer)) != -1) {
             out.write(buffer, 0, bytesRead);
         }
         in.close();
         out.close();
         return null;
     }
 }
