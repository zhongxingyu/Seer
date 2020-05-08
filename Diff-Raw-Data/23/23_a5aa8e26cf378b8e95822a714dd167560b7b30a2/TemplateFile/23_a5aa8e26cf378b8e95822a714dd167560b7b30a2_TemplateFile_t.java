 package de.tum.ascodt.utils;
 
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
 import java.nio.file.Files;
 import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
 
 import org.eclipse.core.runtime.Assert;
 
 import de.tum.ascodt.plugin.ASCoDTKernel;
 import de.tum.ascodt.plugin.utils.exceptions.ErrorWriterDevice;
 import de.tum.ascodt.plugin.utils.tracing.Trace;
 import de.tum.ascodt.resources.ResourceManager;
 import de.tum.ascodt.utils.exceptions.ASCoDTException;
 
 
 /**
  * Represents a template file
  * 
  * The template file holds one template file and a map from strings to other
  * strings. This way, it is basically a text replacement system, i.e. it can
  * take one template file, replace all strings within this file, and write it
  * to another file.
  * 
  * There's some very simple extensions to a standard text replacement system:
  * You can explicitly give
  * 
  * @author Tobias Weinzierl
  */
 public class TemplateFile {
   static public class LanguageConfiguration {
     public String _namespaceSeparator;
     public String _openNamespaceIdentifier;
     public boolean _openNamespaceHierarchically;
     public boolean _useCloseIdentifier;
     public String _closeNamespaceIdentifier;
 
     /**
      * Do not instantiate yourself but use static getters instead
      */
     private LanguageConfiguration() {}
   }
 
   private static final String KeywordInsertHere = "-- INSERT HERE --";
 
   private static final String KeywordClosePackage = "__CLOSE_PACKAGE__";
 
   private static final String KeywordOpenPackage = "__OPEN_PACKAGE__";
 
   private static Trace _trace = new Trace("de.tum.ascodt.utils.TemplateFile");
 
   static public LanguageConfiguration getLanguageConfigurationForCPP() {
     LanguageConfiguration result = new LanguageConfiguration();
     result._namespaceSeparator = "::";
     result._openNamespaceIdentifier = "namespace";
     result._openNamespaceHierarchically = true;
     result._closeNamespaceIdentifier = "";
     result._useCloseIdentifier = false;
     return result;
   }
 
   static public LanguageConfiguration getLanguageConfigurationForFortran() {
     LanguageConfiguration result = new LanguageConfiguration();
     result._namespaceSeparator = "_";
     result._openNamespaceIdentifier = "module";
     result._closeNamespaceIdentifier = "end module ";
     result._openNamespaceHierarchically = false;
     result._useCloseIdentifier = true;
     return result;
   }
 
   static public LanguageConfiguration getLanguageConfigurationForJava() {
     LanguageConfiguration result = new LanguageConfiguration();
     result._namespaceSeparator = ".";
     result._openNamespaceIdentifier = "package";
     result._closeNamespaceIdentifier = "";
     result._openNamespaceHierarchically = false;
     result._useCloseIdentifier = false;
     return result;
   }
 
   static public LanguageConfiguration getLanguageConfigurationForJNI() {
     LanguageConfiguration result = new LanguageConfiguration();
     result._namespaceSeparator = "_";
     result._openNamespaceIdentifier = "namespace";
     result._openNamespaceHierarchically = false;
     result._closeNamespaceIdentifier = "";
     result._useCloseIdentifier = false;
     return result;
   }
 
   static public LanguageConfiguration getLanguageConfigurationForSIDL() {
     LanguageConfiguration result = new LanguageConfiguration();
     result._namespaceSeparator = ".";
     result._openNamespaceIdentifier = "package";
     result._closeNamespaceIdentifier = "";
     result._openNamespaceHierarchically = true;
     return result;
   }
 
   private BufferedWriter _writer;
   private BufferedReader _reader;
 
   private boolean _hasToCloseOutputStream;
 
   /**
    * All the mappings from keywords to concrete names.
    */
   private java.util.HashMap<String, String> _mapping;
 
   /**
    * All conditional regions identifier
    */
   private java.util.HashSet<String> _conditions;
 
   /**
    * TemplateFile instances can be built on top of each other, i.e. you create
    * one template file and then a second on top of it that inserts additional
    * data into the output stream. In this case, this attribute points to the
    * parent. Otherwise, it is null.
    * 
    * When we do the text replacement, also the parent replacements are
    * performed.
    */
   private TemplateFile _parentTemplateFile;
 
   /**
    * Namespace of generated artifacts. Always is kept separately, as it
    * deserves special treatment.
    */
   private String[] _namespace;
 
   private LanguageConfiguration _languageConfiguration;
 
   private boolean _overwrite;
   private boolean _readable;
 
   public TemplateFile(InputStream sourceStream,
                       Path targetFilePath,
                       String[] namespace,
                       LanguageConfiguration languageConfiguration,
                       boolean overwrite) {
     _trace.in("TemplateFile(...)", targetFilePath.toString(), namespace);
 
     _mapping = new java.util.HashMap<String, String>();
     _namespace = namespace;
     _languageConfiguration = languageConfiguration;
 
     _hasToCloseOutputStream = true;
     _parentTemplateFile = null;
     _overwrite = overwrite;
     _readable = true;
     openTemplateStream(sourceStream);
     openOutputStream(targetFilePath);
     _trace.out("TemplateFile(...)");
   }
 
   /**
    * 
    * @param sourceFilePath
    * @param overwriteExistingFile
    * @param namespace
    * @param namespaceSeparator
    *          For Java and SIDl, use ".", for C++ use "::".
    * @param namespaceIdentifier
    *          For C++ use "namespace", for SIDL use "
    */
   public TemplateFile(Path sourceFilePath,
                       Path targetFilePath,
                       String[] namespace,
                       LanguageConfiguration languageConfiguration,
                       boolean overwrite) {
     try {
       _trace.in("TemplateFile(...)");
 
       _mapping = new java.util.HashMap<String, String>();
       _conditions = new java.util.HashSet<String>();
       _namespace = namespace;
       _languageConfiguration = languageConfiguration;
 
       _hasToCloseOutputStream = true;
       _parentTemplateFile = null;
       _overwrite = overwrite;
       _readable = true;
       openTemplateStream(ResourceManager.getResourceAsStream(sourceFilePath,
                                                              ASCoDTKernel.ID));
       openOutputStream(targetFilePath);
       _trace.out("TemplateFile(...)");
     } catch (Exception e) {
       ErrorWriterDevice.getInstance().println(e);
     }
   }
 
   /**
    * Kind of a copy constructor. Takes another template file and inserts data
    * into it.
    * 
    * @param other
    */
   public TemplateFile(TemplateFile other, Path sourceFilePath) throws ASCoDTException {
     _trace.in("TemplateFile(...)");
 
     try {
       openTemplateStream(ResourceManager.getResourceAsStream(sourceFilePath,
                                                              ASCoDTKernel.ID));
     } catch (Exception e) {
       ErrorWriterDevice.getInstance().println(e);
     }
 
     _writer = other._writer;
     _mapping = new java.util.HashMap<String, String>();
     _namespace = other._namespace;
     _languageConfiguration = other._languageConfiguration;
 
     _hasToCloseOutputStream = false;
     _parentTemplateFile = other;
     _overwrite = other._overwrite;
     _readable = other._readable;
     _trace.out("TemplateFile(...)");
   }
 
   public void addCondition(String conditionVariable) {
     _conditions.add(conditionVariable);
   }
 
   /**
    * Add a new mapping. Keywords typically are started with two underscores and
    * end with two underscores.
    * 
    * @param keyword
    * @param replacement
    */
   public void addMapping(String keyword, String replacement) {
     Assert.isTrue(!_mapping.containsKey(keyword));
     Assert.isTrue(keyword != KeywordOpenPackage);
     Assert.isTrue(keyword != KeywordClosePackage);
     _mapping.put(keyword, replacement);
   }
 
   /**
    * This operation does two things: It first processes the remaining part of
    * the template file. Then, it closes all the streams and returns. Be careful:
    * The output stream is closed if and only if this class also was responsible
    * to open it.
    * 
    * @throws ASCoDTException
    */
   public void close() {
     _trace.in("close()");
 
     processTemplateStream();
     closeStreams();
 
     _trace.out("close()");
   }
 
   private void closeStreams() {
     try {
       if (_hasToCloseOutputStream) {
         if (_writer != null) {
           _writer.flush();
           _writer.close();
         }
       }
       _reader.close();
     } catch (IOException e) {
       ErrorWriterDevice.getInstance().println(e);
     }
   }
 
   /**
    * You always have to call close() after open().
    */
   public void open() {
     _trace.in("open()");
 
     processTemplateStream();
 
     _trace.out("open()");
   }
 
   /**
    * Open the output stream
    * 
    * First, we strip the path from the file name. Here, we may not use \\,
    * i.e. File.separatorChar. This is due to a simple reason: The path
    * comes from an URL and consequently does already contain only slashes.
    * 
    * @param file
    * @throws ASCoDTException
    */
   private void openOutputStream(Path filePath) {
     _trace.in("openOutputStream()");
 
     try {
       if (!_overwrite && !Files.exists(filePath) || _overwrite) {
         if (!Files.exists(filePath.getParent())) {
           _trace.debug("openOutputStream()", "path of " + filePath.getParent()
                                                                   .toString() +
                                              " does not exist. create");
           Files.createDirectories(filePath.getParent());
         } else {
           _trace.debug("openOutputStream(URL)",
                        "path of " + filePath.getParent().toString() +
                            " already exists");
         }
 
        _writer = Files.newBufferedWriter(filePath,StandardCharsets.UTF_8,StandardOpenOption.CREATE,StandardOpenOption.WRITE);
       }
     } catch (Exception e) {
       ErrorWriterDevice.getInstance().println(e);
     }
 
     _trace.out("openOutputStream()");
   }
 
   private void openTemplateStream(InputStream stream) {
     try {
       _reader = new BufferedReader(new InputStreamReader(stream));
     } catch (Exception e) {
       ErrorWriterDevice.getInstance().println(e);
     }
   }
 
   private void processTemplateStream() {
     try {
       String text = _reader.readLine();
 
       while (text != null && !text.trim().equals(KeywordInsertHere)) {
         text = replaceKeywords(text);
         if (_writer != null) {
           _writer.write(text);
           _writer.write("\n");
         }
         text = _reader.readLine();
       }
     } catch (Exception e) {
       ErrorWriterDevice.getInstance().println(e);
     }
   }
 
   /**
    * Replace all the keywords.
    * 
    * @param line
    * @return
    */
   private String replaceKeywords(String line) {
     String text = line;
     if (text.contains("__COND")) {
       text = text.replaceAll("__COND", "_");
       if (_conditions.contains(text.trim())) {
         _readable = true;
 
       } else {
 
         _readable = false;
       }
       text = "";
     } else if (text.contains("__END_COND__")) {
       _readable = true;
       text = "";
     }
     if (_readable) {
       for (String p : _mapping.keySet()) {
         text = text.replaceAll(p, _mapping.get(p));
       }
 
       if (line.contains(KeywordOpenPackage)) {
         if (_languageConfiguration._openNamespaceHierarchically) {
           for (int indent = 0; indent < _namespace.length; indent++) {
             text =
                 text.replaceFirst(KeywordOpenPackage,
                                   _languageConfiguration._openNamespaceIdentifier + " " +
                                       _namespace[indent] +
                                       " { \n");
             for (int i = 0; i < indent; i++) {
               text += "  ";
             }
             text = text + KeywordOpenPackage;
           }
         } else {
           text =
               text.replaceFirst(KeywordOpenPackage,
                                 _languageConfiguration._openNamespaceIdentifier + " " +
                                     _namespace[0] +
                                     "__OPEN_PACKAGE__");
           for (int i = 1; i < _namespace.length; i++) {
             text =
                 text.replaceFirst(KeywordOpenPackage,
                                   _languageConfiguration._namespaceSeparator + _namespace[i] +
                                       KeywordOpenPackage);
           }
         }
         text = text.replaceFirst(KeywordOpenPackage, "");
       } else if (line.contains(KeywordClosePackage)) {
         if (_languageConfiguration._openNamespaceHierarchically) {
           int indent = 0;
           for (; indent < _namespace.length - 1; indent++) {
             for (int i = 0; i < indent; i++) {
               text += "  ";
             }
             text =
                 text.replaceFirst(KeywordClosePackage,
                                   "} \n" + KeywordClosePackage);
           }
           if (indent < _namespace.length) {
             text = text.replaceFirst(KeywordClosePackage, "}");
           }
         } else {
           if (!_languageConfiguration._useCloseIdentifier) {
             text =
                 text.replaceAll(KeywordClosePackage,
                                 _languageConfiguration._closeNamespaceIdentifier);
           } else {
             text =
                 text.replaceFirst(KeywordClosePackage,
                                   _languageConfiguration._closeNamespaceIdentifier + " " +
                                       _namespace[0] +
                                       KeywordClosePackage);
             for (int i = 1; i < _namespace.length; i++) {
               text =
                   text.replaceFirst(KeywordClosePackage,
                                     _languageConfiguration._namespaceSeparator + _namespace[i] +
                                         KeywordClosePackage);
             }
           }
         }
         text = text.replaceFirst(KeywordClosePackage, "");
 
       }
     } else {
       text = "";
     }
     if (_parentTemplateFile != null) {
       return _parentTemplateFile.replaceKeywords(text);
     } else {
       return text;
     }
   }
 }
