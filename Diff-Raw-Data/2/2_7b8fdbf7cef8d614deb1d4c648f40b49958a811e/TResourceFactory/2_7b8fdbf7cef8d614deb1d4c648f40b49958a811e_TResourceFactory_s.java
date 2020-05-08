 package com.bradmcevoy.http;
 
 import com.bradmcevoy.common.Path;
 
 
 public class TResourceFactory implements ResourceFactory {
 
     private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TResourceFactory.class);
     
     public static final TFolderResource ROOT = new TFolderResource((TFolderResource)null,"localhost:8084/MiltonTestWeb");
     
     static {
         TFolderResource folder;
         TResource file;
         file = new TTextResource(ROOT,"index.html","Hi there");
         folder = new TFolderResource(ROOT,"folder1");
         file = new TTextResource(folder,"index.html","i am a web page in folder1");
         folder = new TFolderResource(ROOT,"folder2");
         new TFolderResource(folder,"folder2a");
         folder = new TFolderResource(ROOT,"folder3");
         TFolderResource fSpecial = new TFolderResource(ROOT,"special chars");
         TFolderResource fSpecialSub = new TFolderResource(ROOT,"folder with ampersand &");
         new TFolderResource(fSpecial,"folder with percentage %");
        //new TFolderResource(fSpecial,"folder with specil chars"); // contains ae character
         file = new TTextResource(folder,"index.html","i am a web page");
         file = new TTextResource(folder,"stuff.html","");
         folder = new TFolderResource(folder,"subfolder1");
         file = new TTextResource(folder,"index.html","");
         folder = new TFolderResource(ROOT,"secure");
         folder.setSecure("test","pwd");
         file = new TTextResource(folder,"index.html","");
         file.setSecure("test","pwd");
     }
     
     
     public Resource getResource(String host, String url) {
         log.debug("getResource: url: " + url );
         Path path = Path.path(url);
         Resource r = find(path);
         log.debug("_found: " + r);
         return r;
     }
 
     private TResource find(Path path) {
         if( isRoot(path) ) return ROOT;
         TResource r = find(path.getParent());
         if( r == null ) return null;
         if( r instanceof TFolderResource ) {
             TFolderResource folder = (TFolderResource)r;
             for( Resource rChild : folder.getChildren() ) {
                 TResource r2 = (TResource) rChild;
                 if( r2.getName().equals(path.getName())) {
                     return r2;
                 } else {
 //                    log.debug( "IS NOT: " + r2.getName() + " - " + path.getName());
                 }
             }
         }
         log.debug("not found: " + path);
         return null;
     }
 
     public String getSupportedLevels() {
         return "1,2";
     }
 
     private boolean isRoot( Path path ) {
         if( path == null ) return true;
         return ( path.getParent() == null || path.getParent().isRoot());
     }
 
 }
