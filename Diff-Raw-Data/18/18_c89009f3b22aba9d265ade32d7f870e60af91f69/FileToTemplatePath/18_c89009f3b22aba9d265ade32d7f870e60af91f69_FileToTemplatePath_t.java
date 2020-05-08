 package ixcode.platform.http.server.resource;
 
 class FileToTemplatePath {
 
     private final String rootPath;
     private final String templateExtension;
 
     public FileToTemplatePath(String rootPath, String templateExtension) {
         this.rootPath = rootPath;
         this.templateExtension = templateExtension;
     }
 
 
     public String pathFrom(String path) {
         String pathWithoutRoot = path.substring(rootPath.length());
        String pathWithoutExtension = pathWithoutRoot.substring(0, pathWithoutRoot.length() - templateExtension.length());

        return pathWithoutExtension.replaceAll("\\\\", "/");
     }
 }
