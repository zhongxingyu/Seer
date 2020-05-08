 package nu.wasis.jlog.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 
 import freemarker.cache.StringTemplateLoader;
 import freemarker.template.Configuration;
 import freemarker.template.ObjectWrapper;
 import freemarker.template.Template;
 
 public class TemplateLoader {
 
    @SuppressWarnings("unused")
     private static final Logger LOG = Logger.getLogger(TemplateLoader.class);
 
     public static final TemplateLoader INSTANCE = new TemplateLoader();
 
     private final List<File> files = new LinkedList<>();
     private final Configuration configuration = new Configuration();
     private boolean stripCommentsEnabled = false;
 
     private TemplateLoader() {
         configuration.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
     }
 
     public Template getTemplate(final String filename, final Map<String, String> replacements) throws IOException {
         final StringTemplateLoader templateLoader = new StringTemplateLoader();
         for (final File file : files) {
             final String templateName = file.getName();
             String templateSource = readAndReplace(file, replacements);
             if (isStripCommentsEnabled()) {
                 // from http://ostermiller.org/findcomment.html
                 templateSource = templateSource.replaceAll("/\\*(?:.|[\\n\\r])*?\\*/", "");
                // LOG.debug("result for " + file.getPath() + ":");
                // LOG.debug(templateSource);
             }
             templateLoader.putTemplate(templateName, templateSource);
         }
         configuration.setTemplateLoader(templateLoader);
         return configuration.getTemplate(filename);
     }
 
     public void addDirectory(final File directory) throws IOException {
         if (null == directory) {
             throw new IllegalArgumentException("Param `directory' must not be null.");
         }
         if (!directory.isDirectory()) {
             throw new IllegalArgumentException("Param `directory' must be a directory.");
         }
 
         files.addAll(Arrays.asList(directory.listFiles()));
     }
 
     public boolean isStripCommentsEnabled() {
         return stripCommentsEnabled;
     }
 
     public void setStripCommentsEnabled(final boolean stripCommentsEnabled) {
         this.stripCommentsEnabled = stripCommentsEnabled;
     }
 
     private String readAndReplace(final File file, final Map<String, String> replacements) throws IOException {
         return doReplacements(readFile(file), replacements);
     }
 
     private String readFile(final File file) throws IOException {
         final InputStream instream = new FileInputStream(file);
         return IOUtils.toString(instream, "UTF-8");
     }
 
     private String doReplacements(final String input, final Map<String, String> replacements) {
         String result = input;
         for (final Entry<String, String> replacement : replacements.entrySet()) {
             result = result.replace(replacement.getKey(), replacement.getValue());
         }
         return result;
     }
 
 }
