 /*bin/mkdir /tmp/strip 2> /dev/null
 javac -d /tmp/strip $0
 java -cp /tmp/strip Strip "$@"
 exit
 */
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Strip {
 
   private final Pattern pattern;
   private final Set<File> files;
   private boolean clobber;
 
   public Strip(Pattern pattern, Set<File> files, boolean clobber) {
     this.pattern = pattern;
     this.files = files;
     this.clobber = clobber;
   }
 
   private void strip() throws IOException {
     for (File file : files) {
       FileCharSequence fileCharSequence = new FileCharSequence(file);
 
       File stripped = File.createTempFile(file.getName(), ".strip");
       BufferedWriter out = new BufferedWriter(new FileWriter(stripped));
       int numberFound = 0;
 
       // strip to a copy
       int position = 0;
       Matcher matcher = pattern.matcher(fileCharSequence);
       while (matcher.find(position)) {
         out.append(fileCharSequence.subSequence(position, matcher.start()));
         position = matcher.end();
         numberFound++;
       }
       out.append(fileCharSequence.subSequence(position, fileCharSequence.length()));
       out.close();
       fileCharSequence.close();
 
       // move copy over original
       if (numberFound > 0) {
         if (clobber) {
           copy(stripped, file);
           System.out.println("Stripped " + numberFound + " from " + file);
         } else {
           System.out.println("Stripped " + numberFound + " from " + file + " at " + stripped);
         }
       } else {
         System.out.println("No occurrences in " + file + ", stripped file is " + stripped);
       }
     }
   }
 
   private void copy(File source, File target) throws IOException {
     byte[] buffer = new byte[1024];
     BufferedInputStream in = new BufferedInputStream(new FileInputStream(source));
     BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(target));
 
     int read;
     while ((read = in.read(buffer)) != -1) {
       out.write(buffer, 0, read);
     }
 
     in.close();
     out.close();
   }
 
   static class FileCharSequence implements CharSequence {
 
     final RandomAccessFile randomAccess;
     final long start;
     final long end;
 
     public FileCharSequence(File file) throws IOException {
       randomAccess = new RandomAccessFile(file, "r");
       start = 0;
       end = randomAccess.length();
     }
 
     private FileCharSequence(FileCharSequence prototype, long start, long end) {
       this.randomAccess = prototype.randomAccess;
       this.start = start;
       this.end = end;
     }
 
     public int length() {
       return (int) (end - start);
     }
 
     public char charAt(int index) {
       try {
         randomAccess.seek(start + index);
         return (char) randomAccess.read();
       } catch (IOException e) {
         throw new RuntimeException(e);
       }
     }
 
     public CharSequence subSequence(int start, int end) {
      return new FileCharSequence(this, start, end);
     }
 
     public void close() throws IOException {
       randomAccess.close();
     }
 
     @Override public String toString() {
       try {
         byte[] bytes = new byte[length()];
         randomAccess.seek(start);
         randomAccess.readFully(bytes);
         return new String(bytes, "ISO-8859-1");
       } catch (IOException e) {
         throw new RuntimeException(e);
       }
     }
   }
 
   public static void main(String[] args) throws IOException {
     boolean clobber = false;
 
     List<String> argsList = new ArrayList<String>(Arrays.asList(args));
     for (Iterator<String> a = argsList.iterator(); a.hasNext(); ) {
       String arg = a.next();
       if ("-c".equals(arg) || "--clobber".equals(arg)) {
         clobber = true;
         a.remove();
       }
     }
 
     if (argsList.size() < 2) {
       printUsage();
       System.exit(1);
     }
 
     Pattern pattern = Pattern.compile(argsList.get(0));
 
     Set<File> files = new LinkedHashSet<File>();
     for (String file : argsList.subList(1, argsList.size())) {
       files.add(new File(file));
     }
 
     new Strip(pattern, files, clobber).strip();
   }
 
   public static void printUsage() {
     System.out.println("Usage: Strip <regex> [files]");
     System.out.println();
     System.out.println("  regex: a Java regular expression, with groups");
     System.out.println("  http://java.sun.com/javase/6/docs/api/java/util/regex/Pattern.html");
     System.out.println("         you can (parenthesize) groups");
     System.out.println("         \\s whitespace");
     System.out.println("         \\S non-whitespace");
     System.out.println("         \\w word characters");
     System.out.println("         \\W non-word");
     System.out.println();
     System.out.println("  files: files to strip. These will be overwritten!");
     System.out.println();
     System.out.println("flags:");
     System.out.println("  --clober: overwrite the passed in files rather than creating new ones");
     System.out.println("       -c:");
     System.out.println();
     System.out.println("  Use 'single quotes' to prevent bash from interfering");
     System.out.println();
   }
 }
