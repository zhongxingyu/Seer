 package ideah.compiler;
 
 final class GHCDir {
 
     final String name;
    final Integer[] version;
 
     GHCDir(String name) {
         this.name = name;
         String[] versionStr = name.split("[^0-9]");
         version = new Integer[versionStr.length];
         for (int i = 0; i < versionStr.length; i++) {
            version[i] = Integer.parseInt(versionStr[i]);
         }
     }
 }
