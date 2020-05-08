 package replacer;
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.StandardCopyOption;
 import java.util.regex.Pattern;
 
 public class PackageName extends Target {
     private final String[] fromPackages;
 
     public PackageName(String from, String to) {
         super(from, to);
         this.fromPackages = from.split(Pattern.quote("."));
     }
 
     @Override
     protected File replaceName(File file) throws IOException {
         File parent = file;
         for (int i = fromPackages.length - 1; i >= 0; i--) {
             parent = parent.getParentFile();
         }
 
         File dest = new File(parent, getTo().replace(".", "/"));
         try {
             Files.createDirectories(dest.toPath());
             Files.move(file.toPath(), dest.toPath(),
                     StandardCopyOption.REPLACE_EXISTING);
 
             // ゴミ削除
             File toBeDeleted = file.getParentFile();
             for (int i = 1; i < fromPackages.length; i++) {
                Files.deleteIfExists(toBeDeleted.toPath());
                 toBeDeleted = toBeDeleted.getParentFile();
             }
         } catch (IOException e) {
             System.err.println(this + " error!");
             throw e;
         }
         return dest;
     }
 
     @Override
     protected boolean nameMatches(File file) {
         File target = file;
         // パッケージの階層一致チェック
         for (int i = fromPackages.length - 1; i >= 0; i--) {
             if (target == null || !target.getName().equals(fromPackages[i])) {
                 return false;
             }
             target = target.getParentFile();
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "[PackageName]";
     }
 }
