 package org.apache.cocoon.components.source.impl;
 
 import org.apache.excalibur.source.ModifiableSource;
 import org.apache.excalibur.source.Source;
 import org.apache.excalibur.source.SourceNotFoundException;
 import org.apache.excalibur.source.SourceUtil;
 import org.apache.excalibur.source.SourceValidity;
 import org.apache.excalibur.source.TraversableSource;
 import org.apache.log4j.Category;
 
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.util.Collection;
 
 import org.wyona.yarep.core.Path;
 import org.wyona.yarep.core.Repository;
 import org.wyona.yarep.core.RepositoryFactory;
 
 /**
  *
  */
 public class YarepSource implements ModifiableSource, TraversableSource {
 
     private static Category log = Category.getInstance(YarepSource.class);
 
     private Path path;
 
     private String SCHEME = "yarep";
 
     private Repository repo;
 
     private RepositoryFactory repoFactory;
 
     /**
      *
      */
     public YarepSource(String src, RepositoryFactory repoFactory) throws MalformedURLException, Exception {
         this.repoFactory = repoFactory;
 
         if (!SourceUtil.getScheme(src.toString()).equals(SCHEME)) throw new MalformedURLException();
	log.debug("Original src = " + src);
 
         this.path = new Path(SourceUtil.getSpecificPart(src.toString()));
	log.debug("Original path = " + path);
 
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
                     return;
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
         return;
     }
 
     /**
      *
      */
     public boolean exists() {
         return repo.exists(path);
     }
 
     /**
      *
      */
     public long getContentLength() {
         log.warn("getContentLength() not implemented yet!");
         return System.currentTimeMillis();
     }
 
     /**
      *
      */
     public InputStream getInputStream() throws IOException, SourceNotFoundException {
         return repo.getInputStream(path);
     }
 
     /**
      *
      */
     public long getLastModified() {
         //return repo.getLastModified(path);
         return System.currentTimeMillis();
     }
 
     /**
      *
      */
     public String getMimeType() {
         log.warn("getMimeType() not implemented yet!");
         return null;
     }
 
     /**
      *
      */
     public String getScheme() {
         return SCHEME;
     }
 
     /**
      *
      */
     public String getURI() {
         log.warn("getURI() not really implemented yet! Path: " + path);
         return SCHEME + ":" + path.toString();
     }
 
     /**
      *
      */
     public SourceValidity getValidity() {
         log.warn("getValidity() not implemented yet!");
         return null;
     }
 
     /**
      *
      */
     public void refresh() {
         log.warn("Not implemented yet!");
     }
 
     /**
      *
      */
     public boolean canCancel(OutputStream out) {
         log.warn("Not implemented yet!");
         return false;
     }
 
     /**
      *
      */
     public void cancel(OutputStream out) {
         log.warn("Not implemented yet!");
     }
 
     /**
      *
      */
     public void delete() {
         log.warn("Not implemented yet!");
     }
 
     /**
      *
      */
     public OutputStream getOutputStream() throws IOException {
         return repo.getOutputStream(path);
     }
 
     /**
      *
      */
     public Source getParent() {
         log.warn("Not implemented yet!");
         return null;
     }
 
     /**
      *
      */
     public String getName() {
         return path.getName();
     }
 
     /**
      *
      */
     public Source getChild(String name) {
         log.warn("Not implemented yet!");
         return null;
     }
 
     /**
      *
      */
     public Collection getChildren() {
         Path[] children = repo.getChildren(path);
         java.util.Vector collection = new java.util.Vector();
         try {
             for (int i = 0; i < children.length; i++) {
                 if (false) { // TODO: Even in the case of the ROOT repo one should add the repo prefix, right?
                     collection.add(new YarepSource("yarep:" + children[i].toString(), repoFactory));
                 } else {
                     collection.add(new YarepSource("yarep:/" + repo.getID() + children[i].toString(), repoFactory));
                 }
             }
         } catch (MalformedURLException e) {
             log.error(e);
         } catch (Exception e) {
             log.error(e);
         }
         return collection;
     }
 
     /**
      *
      */
     public boolean isCollection() {
         return repo.isCollection(path);
     }
 }
