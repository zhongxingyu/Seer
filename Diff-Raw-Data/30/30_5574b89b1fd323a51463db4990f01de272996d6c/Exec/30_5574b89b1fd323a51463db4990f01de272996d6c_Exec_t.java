 /**
  * 
  */
 package Ide.desktopentry;
 
 import java.io.File;
 import java.net.URL;
import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Exec key from Application Desktop Entry. The Exec key must contain a command line. A command line consists
  * of an executable program optionally followed by one or more arguments. The executable program can either be
  * specified with its full path or with the name of the executable only. If no full path is provided the
  * executable is looked up in the $PATH environment variable used by the desktop environment. The name or path
  * of the executable program may not contain the equal sign ("="). Arguments are separated by a space.
  * 
  * @since 0.1
  */
 final class Exec {
   
   public static final String[] FORBIDDEN_CHARACTERS = {"<", ">", "~", "|", "&", ";", "*", "?",
     "#", "(", ")"};
 
   private final String exe;
   private Arguments arguments;
 
   private Exec(final String executable, final Arguments arguments) {
     this.arguments = arguments;
     this.exe = executable;
   }
   
   /**
    * Parses value part of Exec key.
    * @see Exec#parse(String, String)
    * @param string
    * @return
    */
   public static Exec parse(final String string) {
     return parse(string, null);
   }
 
   /**
    * Parses value part of Exec key, does all necessary escaping. See <a
    * href="http://standards.freedesktop.org/desktop-entry-spec/latest/ar01s06.html">Specification</a> for more
    * information. Returns {@code null} when {@code s} is invalid.
    * 
    * @param s Exec's value to parse
    * @param iconName Icon name used in %i parsing
   * @return proper {@link Exec} object or null when {@code s} is invalid.
    */
   public static Exec parse(final String s, final String iconName) {
    if (s == null || s.trim().isEmpty()) return null;
     for (String forbiddenChar : FORBIDDEN_CHARACTERS) {
       if (s.contains(forbiddenChar)) return null;
     }
     
     String[] split = s.split(" ");
     String execName = split[0];
     
     if (execName.contains("=")) return null;
     
     List<String> temp = new TokenizeWithDoubleQuotes().tokenize(s);
     if (temp == null) return null;
     
     List<String> tempArgs = new LinkedList<>();
     Unquote backtick = new UnquoteCharacter('`');
     Unquote dollar = new UnquoteCharacter('$');
     Unquote backslash = new UnquoteCharacter('\\');
     for (String arg : temp) {
       tempArgs.add(backtick.unquote(backslash.unquote(dollar.unquote(arg))));
     }
     
     execName = tempArgs.remove(0);
     Arguments args = Arguments.parse(tempArgs);
 
     return new Exec(execName, args);
   }
  
  public static Exec empty() {
    List<String> emptyList = Collections.emptyList();
    return new Exec(null, Arguments.parse(emptyList));
  }
 
   public String getExecutable() {
     return exe;
   }
 
   public List<String> getArguments() {
     return arguments.format();
   }
 
   public List<String> getFileArguments(File file) {
     return arguments.formatWithFile(file);
   }
   
   public FileArgsSupport argumentsSupport() {
     return arguments.fileArgumentsSupport();
   }
 
   public List<String> getFilesArguments(List<File> files) {
     return arguments.formatWithFiles(files);
   }
 
   public List<String> getURLArguments(URL url) {
     return arguments.formatWithURL(url);
   }
 
   public List<String> getURLsArguments(List<URL> urls) {
     return arguments.formatWithURLs(urls);
   }
 }
