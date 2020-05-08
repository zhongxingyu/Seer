 package org.cobbzilla.restex.targets;
 
 import com.github.mustachejava.DefaultMustacheFactory;
 import com.github.mustachejava.Mustache;
 import lombok.AllArgsConstructor;
 import lombok.EqualsAndHashCode;
 import lombok.Getter;
 import lombok.Setter;
 import org.cobbzilla.restex.RestexCaptureTarget;
 
 import java.io.*;
 import java.util.*;
 
 public class TemplateCaptureTarget implements RestexCaptureTarget {
 
     public static final String SCOPE_HTTP = "http";
     public static final String SCOPE_ANCHOR = "anchor";
     public static final String SCOPE_FILES = "files";
     public static final String HTML_SUFFIX = ".html";
 
     public static final String FOOTER_START = "@@FOOTER@@";
     public static final String INDEX_INSERTION_POINT = "@@MORE-INDEX-FILES@@";
 
     public static final String DEFAULT_INDEX_TEMPLATE = "defaultIndex.mustache";
     public static final String DEFAULT_INDEX_MORE_TEMPLATE = "defaultIndexMore.mustache";
     public static final String DEFAULT_HEADER_TEMPLATE = "defaultHeader.mustache";
     public static final String DEFAULT_FOOTER_TEMPLATE = "defaultFooter.mustache";
     public static final String DEFAULT_ENTRY_TEMPLATE = "defaultEntry.mustache";
 
     private File baseDir;
 
     private final Mustache indexTemplate;
     private final Mustache indexMoreTemplate;
     private final Mustache headerTemplate;
     private final Mustache footerTemplate;
     private final Mustache entryTemplate;
 
     private SortedSet<ContextFile> contextFiles = new TreeSet<>();
     private Map<String, ContextFile> contextFileMap = new HashMap<>();
     private Set<File> filesOpen = new HashSet<>();
 
     // current state, initialized in startRecording, reset in commit
     private SimpleCaptureTarget currentCapture = null;
     @Getter private List<SimpleCaptureTarget> captures = new ArrayList<>();
     private boolean recording = false;
     @Getter private String context = "";
     @Getter private String comment = "";
 
     public TemplateCaptureTarget (String baseDir) {
         this(new File(baseDir), DEFAULT_INDEX_TEMPLATE, DEFAULT_INDEX_MORE_TEMPLATE, DEFAULT_HEADER_TEMPLATE, DEFAULT_FOOTER_TEMPLATE, DEFAULT_ENTRY_TEMPLATE);
     }
 
     public TemplateCaptureTarget (File baseDir) {
         this(baseDir, DEFAULT_INDEX_TEMPLATE, DEFAULT_INDEX_MORE_TEMPLATE, DEFAULT_HEADER_TEMPLATE, DEFAULT_FOOTER_TEMPLATE, DEFAULT_ENTRY_TEMPLATE);
     }
 
     public TemplateCaptureTarget (File baseDir,
                                   String indexTemplate,
                                   String indexMoreTemplate,
                                   String headerTemplate,
                                   String footerTemplate,
                                   String entryTemplate) {
         this.baseDir = baseDir;
         if (!baseDir.exists() && !baseDir.mkdirs()) {
             throw new IllegalArgumentException("baseDir does not exist and could not be created: "+baseDir.getAbsolutePath());
         }
 
         final DefaultMustacheFactory defaultMustacheFactory = new DefaultMustacheFactory();
         this.indexTemplate = defaultMustacheFactory.compile(indexTemplate);
         this.indexMoreTemplate = defaultMustacheFactory.compile(indexMoreTemplate);
         this.headerTemplate = defaultMustacheFactory.compile(headerTemplate);
         this.footerTemplate = defaultMustacheFactory.compile(footerTemplate);
         this.entryTemplate = defaultMustacheFactory.compile(entryTemplate);
     }
 
     public void startRecording (String context, String comment) {
         if (recording) throw new IllegalStateException("Already recording: "+context+"/"+comment);
         this.context = context;
         this.comment = comment;
         currentCapture = new SimpleCaptureTarget();
         recording = true;
     }
 
     @Override public void requestUri(String method, String uri) {
         if (currentCapture != null) currentCapture.requestUri(method, uri);
     }
 
     @Override public void requestHeader(String name, String value) { if (recording) currentCapture.requestHeader(name, value); }
     @Override public void requestEntity(String entityData) { if (recording) currentCapture.requestEntity(entityData); }
     @Override public void responseStatus(int statusCode, String reasonPhrase, String protocolVersion) {
         if (recording) currentCapture.responseStatus(statusCode, reasonPhrase, protocolVersion);
     }
     @Override public void responseHeader(String name, String value) { if (recording) currentCapture.responseHeader(name, value); }
     @Override public void responseEntity(String entityData) {
         if (recording) {
             currentCapture.responseEntity(entityData);
             captures.add(currentCapture);
             currentCapture = new SimpleCaptureTarget();
         }
     }
 
     public void addNote (String note) {
         if (recording) currentCapture.setNote(note);
     }
 
     public void commit () throws IOException {
 
         final String uriFileName = context.replaceAll("[^A-Za-z0-9]", "_") + HTML_SUFFIX;
 
         ContextFile contextFile = contextFileMap.get(context);
         if (contextFile == null) {
             contextFile = new ContextFile(context, uriFileName);
             contextFileMap.put(context, contextFile);
         }
         contextFiles.add(contextFile);
 
         String anchor = comment.replaceAll("[^A-Za-z0-9]", "_");
         contextFile.add(new ContextExample(anchor, comment));
 
         final File uriFile = new File(baseDir, uriFileName);
         final boolean exists = uriFile.exists();
 
         if (!exists) {
             // first time writing to the file, so write the header
             filesOpen.add(uriFile);
             try (FileWriter writer = new FileWriter(uriFile)) {
                 render(headerTemplate, writer);
             }
         } else {
             // file exists -- have we written to it yet?
             if (!filesOpen.contains(uriFile)) {
                 // file exists but we have not written to it, so rewrite the file without the footer
                 removeFooter(uriFile);
             }
         }
 
         try (FileWriter writer = new FileWriter(uriFile, true)) {
             // append the entry
             renderEntry(entryTemplate, writer, anchor);
         }
 
         reset();
     }
 
     public void reset() {
         recording = false;
         context = "";
         comment = "";
         captures = new ArrayList<>();
         currentCapture = null;
     }
 
     public void close () throws IOException {
         if (recording) commit();
         for (File f : filesOpen) {
             try (FileWriter writer = new FileWriter(f, true)) {
                 render(footerTemplate, writer);
             }
         }
         final File indexFile = new File(baseDir, "index.html");
         if (!indexFile.exists()) {
             try (FileWriter writer = new FileWriter(indexFile)) {
                 renderIndex(indexTemplate, writer);
             }
         } else {
             StringWriter writer = new StringWriter();
             renderIndex(indexMoreTemplate, writer);
             replaceInFile(indexFile, INDEX_INSERTION_POINT, writer.toString());
         }
     }
 
     protected void renderEntry(Mustache template, Writer writer, String anchor) {
         Map<String, Object> scope = new HashMap<>();
         scope.put(SCOPE_HTTP, this);
         scope.put(SCOPE_ANCHOR, anchor);
         template.execute(writer, scope);
     }
 
     protected void render(Mustache template, Writer writer) {
         Map<String, Object> scope = new HashMap<>();
         scope.put(SCOPE_HTTP, this);
         template.execute(writer, scope);
     }
 
     protected void renderIndex(Mustache template, Writer writer) {
         Map<String, Object> scope = new HashMap<>();
         scope.put(SCOPE_FILES, contextFiles);
         template.execute(writer, scope);
     }
 
     private void removeFooter(File uriFile) throws IOException {
         File temp = File.createTempFile(getClass().getSimpleName(), HTML_SUFFIX, uriFile.getParentFile());
         try (BufferedReader reader = new BufferedReader(new FileReader(uriFile))) {
             try (FileWriter writer = new FileWriter(temp)) {
                 String line;
                 while ((line = reader.readLine()) != null) {
                     if (line.contains(FOOTER_START)) break;
                     writer.write(line + "\n");
                 }
             }
         }
         if (!temp.renameTo(uriFile)) {
             throw new IllegalStateException("Error rewriting footer in file: "+uriFile.getAbsolutePath());
         }
     }
 
     private void replaceInFile(File file, String insertionPoint, String data) throws IOException {
         File temp = File.createTempFile(getClass().getSimpleName(), HTML_SUFFIX, file.getParentFile());
         try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
             try (FileWriter writer = new FileWriter(temp)) {
                 String line;
                 while ((line = reader.readLine()) != null) {
                     if (line.contains(insertionPoint)) {
                         writer.write(data);
                     }
                     writer.write(line + "\n");
                 }
             }
         }
         if (!temp.renameTo(file)) {
             throw new IllegalStateException("Error rewriting file: "+file.getAbsolutePath());
         }
     }
 
     @AllArgsConstructor @EqualsAndHashCode(of="context")
     class ContextFile implements Comparable {
 
         @Getter @Setter public String context;
         @Getter @Setter public String fsPath;
 
         @Getter public final List<ContextExample> examples = new ArrayList<>();
         public void add(ContextExample contextExample) { examples.add(contextExample); }
 
         @Override
         public int compareTo(Object o) {
             return (o instanceof ContextFile) ? context.compareTo(((ContextFile) o).getContext()) : 0;
         }
     }
 
     @AllArgsConstructor
     class ContextExample {
         @Getter @Setter public String anchor;
         @Getter @Setter public String description;
     }
 }
