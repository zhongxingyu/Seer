 package ir.xweb.module;
 
 import ir.xweb.server.XWebUser;
 import ir.xweb.util.MimeType;
 import ir.xweb.util.Tools;
 import ir.xweb.util.XmlBundle;
 import org.apache.commons.fileupload.FileItem;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.InputSource;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import java.io.*;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.Channels;
 import java.nio.channels.FileChannel;
 import java.nio.channels.WritableByteChannel;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.attribute.FileAttribute;
 import java.sql.Date;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 public class ResourceModule extends Module {
 
     private final static Logger logger = LoggerFactory.getLogger("ResourceModule");
 
     private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss zzz", Locale.ENGLISH);
 
     private final int BUFFER_SIZE = 256;
 
     private final static String DEFAULT_BUNDLE = "bundle";
 
     private long startTempNumber = System.currentTimeMillis();
 
     public final static String PROPERTY_TEMP_DIR = "dir.temp";
 
     public final static String PROPERTY_DEFAULT_ID = "default.id";
 
     public final static String PROPERTY_DATA_DIR   = "dir.data";
 
    public final static String PROPERTY_TEMPLATE_DIR   = "dir.template";
 
     public final static String PROPERTY_RESOURCE_BUNDLE_DIR   = "dir.bundle";
 
     public final static String PROPERTY_STORE_PATTERN   = "store.pattern";
 
     public final static String PROPERTY_DEFAULT_LANGUAGE = "default.language";
 
     public final static String MODE_PUBLIC = "public";
 
     public final static String MODE_PROTECTED = "protected";
 
     public final static String MODE_PRIVATE= "private";
 
     private final File tempDir;
 
     private final File dataDir;
 
     private final File templateDir;
 
     /** Remember bundle dir can be NULL **/
     private final File bundleDir;
 
     private final String defaultId;
 
     private final String storePattern;
 
     private final String defaultLanguage;
 
     private ServletContext context;
 
     private final Map<String, XmlBundle> bundles = new HashMap<String, XmlBundle>();
 
     public ResourceModule(final Manager manager, final ModuleInfo info, final ModuleParam properties) {
         super(manager, info, properties);
 
         File tempDir;
         try {
             tempDir = Files.createTempDirectory("PDROID" + System.currentTimeMillis()).toFile();
         } catch (Exception ex) {
             tempDir = new File(
                     System.getProperty("java.io.tmpdir") + File.separator + System.currentTimeMillis());
         }
 
         this.tempDir = properties.getFile(PROPERTY_TEMP_DIR, tempDir);
 
         this.dataDir = properties.getFile(PROPERTY_DATA_DIR, new File(tempDir, "data"));
         if((!this.dataDir.exists() && !this.dataDir.mkdirs()) || !this.dataDir.canWrite()) {
             throw new IllegalArgumentException("Data is not accessible: " + this.dataDir);
         }
 
         this.templateDir = properties.getFile(PROPERTY_TEMPLATE_DIR, new File(getContext().getRealPath("template")));
 
         // set bundle directory and load default bundle
         this.bundleDir = properties.getFile(PROPERTY_RESOURCE_BUNDLE_DIR, new File(getContext().getRealPath("bundle")));
         final File defaultBundle = new File(DEFAULT_BUNDLE, DEFAULT_BUNDLE + ".xml");
         if(defaultBundle.exists()) {
             try {
                 bundles.put(DEFAULT_BUNDLE, new XmlBundle(defaultBundle));
             } catch (Exception ex) {
                 new IllegalArgumentException("Illegal default bundle file: " + defaultBundle, ex);
             }
         }
 
         this.storePattern = properties.getString(PROPERTY_STORE_PATTERN, null);
 
         // deprecated
         this.defaultId = properties.getString(PROPERTY_DEFAULT_ID, null);
 
         this.defaultLanguage = properties.getString(
                 PROPERTY_DEFAULT_LANGUAGE,
                 getManager().getProperty(PROPERTY_DEFAULT_LANGUAGE));
     }
 
     @Override
     public void init(final ServletContext context) {
         this.context = context;
     }
 
     @Override
     public void process(
             final ServletContext context,
             final HttpServletRequest request,
             final HttpServletResponse response,
             final ModuleParam params,
             final HashMap<String, FileItem> files) throws IOException {
 
         final XWebUser user = (XWebUser)request.getSession().getAttribute(ir.xweb.server.Constants.SESSION_USER);
 
         if(params.containsKey("download")) {
             final String path = params.getString("download", null);
             final String id = params.getString("id", null);
 
             File file = null;
             if(id == null) {
                 if(user == null) {
                     file = getFile(path);
                 } else {
                     file = getFile(user, path);
 
                     // if file not found for current user, try to get it from default user
                     if(file == null) {
                         file = getFile(path);
                     }
                 }
             } else {
                 if(path.startsWith(MODE_PUBLIC + File.separator)) {
                     file = getFile(id, path);
                 } else if(path.startsWith(MODE_PROTECTED + File.separator)) {
                     if(user != null) {
                         file = getFile(id, path);
                     } else {
                         logger.info("Try to access resource that is protected by user session is not active: " + path);
                     }
                 } else if(isAdmin(context, user)) {
                     file = getFile(id, path);
                 } else {
                     logger.info("Try to access resource that is private by user session is not active: " + path);
                 }
             }
 
             if(file == null || !file.exists()) {
                 // we do not rise exception because it's not important to log this message
                 throw new ModuleException(HttpServletResponse.SC_NOT_FOUND, "Resource not found: " + path);
             } else {
                 // check to store
                 boolean store = false;
                 //DataSource dataSource = getManager().getDataSource("data-resource");
 
                 //if(dataSource != null) {
                     if(storePattern != null && path.matches(storePattern)) {
                         if (request.getHeader("Range") != null) {
                             // record it just for the first section of file
                             store = request.getHeader("Range").startsWith("bytes=0") || request.getHeader("Range").startsWith("bytes=-");
                         } else {
                             store = true;
                         }
                     }
                 //}
 
                 try {
                     // store count
                     if(store) {
                         storeResourceUsage(context, path);
                         //dataSource.setData(context, ResourceDataSource.DATA_SOURCE_STORE, path);
                     }
 
                     if (request.getHeader("Range") != null) {
                         rangeDownload(request, response, file);
                     } else {
                         fullDownload(request, response, file);
                     }
 
                 } catch (Exception ex) {
                     ex.printStackTrace();
                     throw new ModuleException("", ex);
                 }
             }
         }
     }
 
     private void rangeDownload(final HttpServletRequest request,
                                final HttpServletResponse response,
                                final File file) throws ServletException, IOException {
 
         response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
         final ArrayList<Long> rawRangesList = new ArrayList<Long>();
         final String range = request.getHeader("Range");
         if(!range.startsWith("bytes=")){
             response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
             return;
         }
         try {
             final StringTokenizer tokenizer = new StringTokenizer(range.substring("bytes=".length()), ",");
             String token;
             while(tokenizer.hasMoreTokens()){
                 token = tokenizer.nextToken();
                 rawRangesList.add(getRangeStart(token));
                 rawRangesList.add(getRangeEnd(token));
             }
         } catch (NumberFormatException e) {
             response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
             return;
         }
         if(rawRangesList.size()==0){
             response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
             return;
         }
 
         //calculate size and validate values
         final long fileSize = file.length();
         final long validRanges [] = new long[rawRangesList.size()];
         long totalSize = 0;
         for (int i = 0; i < rawRangesList.size()/2; i++) {
             final Long start = rawRangesList.get(i);
             final Long end = rawRangesList.get(i + 1);
             if(start==null&&end==null){
                 response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                 return;
             }
 
             final long from;
             final long till;
             if (start == null) {
                 from = fileSize - end.longValue();
                 till = fileSize;
             } else if (end == null) {
                 from = start.longValue();
                 till = fileSize;
             } else {
                 from = start.longValue();
                 till = end.longValue() + 1;
             }
             if( from >= till || from < 0 || till < 0 ||till > fileSize){
                 response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                 return;
             }
             validRanges[i] = from;
             validRanges[i+1] = till;
             totalSize += till - from;
         }
 
         //set headers
         response.setHeader("Accept-Ranges", "bytes");
         response.setHeader("ETag", getETag(file));
         response.setHeader("Last-Modified", getLastModified(file));
         response.setHeader("Content-Length", String.valueOf(totalSize));
         response.setHeader("Content-Type", MimeType.get(file));
         response.setHeader("Content-Range", getContentRange(validRanges, fileSize));
         response.setHeader("Content-Disposition",
                 "inline; " +
                         "filename=" + file.getName() + "; " +
                         "modification-date=\"" + dateFormat.format(file.lastModified()) + "\"");
 
         //send body
         response.setBufferSize(0);
         final OutputStream out = response.getOutputStream();
 
         if (!"head".equalsIgnoreCase(request.getMethod())) {
             for (int i = 0; i < validRanges.length / 2; i++) {
                 write(file, out, validRanges[i], validRanges[i + 1]);
             }
         }
     }
 
     private String getContentRange(final long [] ranges, final long fileSize) {
         //TODO
         return "bytes " + ranges[0] + "-" + (ranges[1]-1) + "/" + fileSize;
     }
 
     public File getFile(final String path) {
         if(this.defaultId == null) {
             throw new IllegalArgumentException("Default identifier not found. Please set: " + PROPERTY_DEFAULT_ID);
         }
 
         return getFile(this.defaultId, path);
     }
 
     public File getFile(final String id, final String path) {
         if(id == null) {
             throw new IllegalArgumentException("null id");
         }
         if(path == null) {
             throw new IllegalArgumentException("null path");
         }
         if(path.matches("^(\\./|\\.\\./|~/|\\\\)")) {   // try to hack
             throw new IllegalArgumentException("Illegal path: " + path);
         }
 
         File f = new File(this.dataDir, getUserDirectory(context, id) + File.separator + path);
         if(f.exists()) {
             return f;
         }
 
         return null;
     }
 
     public File getFile(final XWebUser user, final String path) {
         return getFile(user.getId(), path);
     }
 
     public File initTempFile() {
         startTempNumber++;
         final File file = new File(this.tempDir, Long.toString(startTempNumber));
         return file;
     }
 
     /**
      * Create temp file and write inputstream on that
      * @param is
      * @return
      */
     public File initTempFile(InputStream is) throws IOException {
         if(is == null) {
             throw new IllegalArgumentException("Inputstream null");
         }
         final File file = initTempFile();
         Files.copy(is, file.toPath());
 
         return file;
 
         /*FileOutputStream fos = null;
 
         try {
             fos = new FileOutputStream(file);
             int size;
             byte[] buffer = new byte[1024];
 
             while((size = is.read(buffer)) > 0) {
                 fos.write(buffer, 0, size);
             }
 
             return file;
         } finally {
             if(fos != null) {
                 try {
                     fos.flush();
                     fos.close();
                 } catch (Exception ex) {}
             }
         }*/
     }
 
     public File initTempDir() throws IOException {
         final File file = new File(this.tempDir, Long.toString(System.currentTimeMillis()));
         if(!file.exists() && !file.mkdirs()) {
             throw new IOException("Can not create temp dir. Please set '" + PROPERTY_TEMP_DIR + "' property. " + file);
         }
         return file;
     }
 
     /**
      * Write file with gzip checking, file <filepath>.gz exist and request support gz file, we will use GZ instead of
      * original file
      * @param request
      * @param response
      * @param file
      * @throws IOException
      */
     public void writeFile(
             final HttpServletRequest request,
             final HttpServletResponse response,
             final File file) throws IOException {
 
         if(file == null) {
             throw new IllegalArgumentException("Null file");
         }
 
         boolean zipSupport = false;
         final File zipFile = new File(file.getPath() + ".gz");
 
         if(!file.exists() && !zipFile.exists()) {
             throw new ModuleException(HttpServletResponse.SC_NOT_FOUND, file + " not found");
         }
 
         if(!response.containsHeader("Content-Encoding")) {
             if(zipFile.exists() && zipFile.canRead()) {
                 final String encoding = request.getHeader("Accept-Encoding");
                 if(encoding != null && encoding.toLowerCase().indexOf("gzip") > -1) {
                     zipSupport = true;
                 }
             }
         }
 
         if(zipSupport) {
             response.addHeader("Content-Encoding", "gzip");
             if(!response.containsHeader("Content-Type")) {
                 response.addHeader("Content-Type", MimeType.get(file));
             }
             writeFile(response, zipFile);
         } else {
             writeFile(response, file);
         }
     }
 
     public void writeFile(final HttpServletResponse response, final File file) throws IOException {
         if(file != null && file.exists()) {
             if(response == null) {
                 throw new IllegalArgumentException("null response");
             }
 
             final long from = 0;
             final long till = file.length();
 
             // set headers
             response.setHeader("Accept-Ranges", "bytes");
             response.setHeader("ETag", getETag(file));
             response.setHeader("Last-Modified", getLastModified(file));
             response.setHeader("Content-Length", String.valueOf(till - from));
             response.setHeader("Content-Disposition",
                     "inline; " +
                     "filename=" + file.getName() + "; " +
                     "modification-date=\"" + dateFormat.format(file.lastModified()) + "\"");
 
             if(!response.containsHeader("Content-Type")) {
                 response.addHeader("Content-Type", MimeType.get(file));
             }
 
             response.setBufferSize(0);
             final OutputStream out = response.getOutputStream();
 
             try {
                 write(file, out, from, till);
             } catch (ServletException ex) {
                 throw new IOException(ex);
             }
         } else {
             throw new ModuleException(HttpServletResponse.SC_NOT_FOUND,
                     (file == null ? null : file.getName()) + " not found");
         }
     }
 
     public File initResourceDir(final String path) {
         if(this.defaultId == null) {
             throw new IllegalArgumentException("Default identifier not found");
         }
 
         return initResourceDir(this.defaultId, path);
     }
 
     public File getDataDir() {
         return dataDir;
     }
 
     public File getTempDir() {
         return tempDir;
     }
 
     public File initResourceDir(String id, String path) {
         File dir = new File(dataDir, id + File.separator + path);
         if(dir.exists() || dir.mkdirs()) {
             return dir;
         }
         return null;
     }
 
     public File initResourceFile(String userId, String path) {
         File dir = new File(dataDir, userId);
         File file = new File(dir, path);
 
         File p = file.getParentFile();
         if(!p.exists() && !p.mkdirs()) {
             return null;
         }
 
         return file;
     }
 
     /*public File initResourceDir(XWebUser user, String path) {
         File dir = new File(dataDir, user.getId() + File.separator + path);
         if(dir.exists() || dir.mkdirs()) {
             return dir;
         }
         return null;
     }*/
 
     /**
      * Write partial file data
      * @param file
      * @param out
      * @param from
      * @param till
      * @throws ServletException
      * @throws IOException
      */
     private void write(final File file, final OutputStream out, final long from, final long till)
             throws ServletException, IOException {
 
         final RandomAccessFile f = new RandomAccessFile(file, "r");
         final WritableByteChannel outChannel = Channels.newChannel(out);
         try {
             final MappedByteBuffer map = f.getChannel().map(FileChannel.MapMode.READ_ONLY, from, till - from);
             outChannel.write(map);
         } finally {
             if(f != null) {
                 try {
                     f.close();
                 } catch (Exception ex) {}
             }
         }
     }
 
     private String getETag(final File file) {
         return "W/\"" + file.length() + "-" + file.lastModified() + "\"";
     }
 
     private String getLastModified(final File file) {
         return this.dateFormat.format(new Date(file.lastModified()));
     }
 
     private void fullDownload(
             final HttpServletRequest request,
             final HttpServletResponse response,
             final File file) throws ServletException, IOException {
 
         final long from = 0;
         final long till = file.length();
 
         // set headers
         response.setHeader("Accept-Ranges", "bytes");
         response.setHeader("ETag", getETag(file));
         response.setHeader("Last-Modified", getLastModified(file));
         response.setHeader("Content-Length", String.valueOf(till - from));
         response.setHeader("Content-Type", MimeType.get(file));
         response.setHeader("Content-Disposition",
                 "inline; " +
                 "filename=" + file.getName() + "; " +
                 "modification-date=\"" + dateFormat.format(file.lastModified()) + "\"");
 
         response.setBufferSize(0);
         final OutputStream out = response.getOutputStream();
 
         if(!"head".equalsIgnoreCase(request.getMethod())){
             write(file, out, from, till);
         }
     }
 
     private Long getRangeStart(final String rangeInterval) throws NumberFormatException{
         final int index = rangeInterval.indexOf("-");
         if(index < 0){
             throw new NumberFormatException();
         }
         final String value = rangeInterval.substring(0, index);
         if(value.length() == 0){
             return null;
         }
 
         return new Long(value);
     }
 
     private Long getRangeEnd(final String rangeInterval)throws NumberFormatException{
         final int index = rangeInterval.indexOf("-");
         if(index < 0){
             throw new NumberFormatException();
         }
         final String value = rangeInterval.substring(index+1);
         if(value.length() == 0){
             return null;
         }
 
         return new Long(value);
     }
 
     public String applyTemplate(
             final String template,
             final String language,
             final Map<String, String> params) {
 
         // find XSLT template
         final File xsltFile = getTemplateFile(template, language, ".xsl");
        if(xsltFile != null) {
             try {
                 final String xml = paramToXml(params);
                 final String html = applyXslt(xsltFile, xml);
 
                 return html;
             } catch (Exception ex) {
                 logger.error("Error to apply XSLT template: " + xsltFile);
             }
         }
 
         // find text template
         final File textFile = getTemplateFile(template, language, ".txt");
        if(textFile != null) {
             try {
                 final String text = applyText(xsltFile, params);
 
                 return text;
             } catch (Exception ex) {
                 logger.error("Error to apply XSLT template: " + xsltFile);
             }
         }
 
         return null;
     }
 
     private String applyText(final File template, final Map<String, String> params) throws Exception {
         String text = Tools.readTextFile(template);
 
         for(Map.Entry<String, String> e:params.entrySet()) {
             text = text.replace("${" + e.getKey() + "}", e.getValue());
         }
 
         return text;
     }
 
     private String applyXslt(final File template, final String xml) throws Exception {
         final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 
         final DocumentBuilder builder = factory.newDocumentBuilder();
         final InputSource is = new InputSource();
         is.setCharacterStream(new StringReader(xml));
 
         // Use a Transformer for output
         final TransformerFactory tFactory = TransformerFactory.newInstance();
         final StreamSource styleSource = new StreamSource(template);
         final Transformer transformer = tFactory.newTransformer(styleSource);
 
         final StringWriter w = new StringWriter();
         final DOMSource source = new DOMSource(builder.parse(is));
         final StreamResult result = new StreamResult(w);
         transformer.transform(source, result);
 
         return w.toString();
     }
 
     private String paramToXml(final Map<String, String> params) throws IOException {
         final Element root = new Element("params");
 
         for(Map.Entry<String, String> e:params.entrySet()) {
             final Element param = new Element(e.getKey());
             param.setText(e.getValue());
             root.addContent(param);
         }
 
         final XMLOutputter xmlOutput = new XMLOutputter();
 
         final StringWriter w = new StringWriter();
         xmlOutput.setFormat(Format.getRawFormat());
         xmlOutput.output(new Document(root), w);
 
         return w.toString();
     }
 
     private File getTemplateFile(
             final String template,
             final String language,
             final String extension) {
 
         File xsltFile = null;
         // find XSLT file
         if(language != null) {
             // ex: template-fa_IR.xsl
             xsltFile = new File(templateDir, template + "-" + language + extension);
 
             if(!xsltFile.exists()) {
                 // ex: template-fa.xsl
                 xsltFile = new File(templateDir, template + "-" + language.split("[_-]")[0] + extension);
             }
         }
 
         if(xsltFile == null || !xsltFile.exists()) {
             xsltFile = new File(templateDir, template + extension);
         }
 
         return xsltFile.exists() ? xsltFile : null;
     }
 
     public String getLanguage(final HttpServletRequest request) {
         // Get language from parameter
         String language = null;
 
         if(request != null) {
             language = request.getParameter("language");
 
             // Get language from cookie
             if(language == null) {
                 final Cookie[] cookies = request.getCookies();
                 if(cookies != null) {
                     for(Cookie c:cookies) {
                         if("language".equals(c.getName())) {
                             language = c.getValue();
                             break;
                         }
                     }
                 }
             }
 
             // get language from user-agent
             if(language == null) {
                 final Enumeration locales = request.getLocales();
                 while (locales.hasMoreElements()) {
                     Locale locale = (Locale) locales.nextElement();
                     language = locale.getDisplayLanguage();
                     break;
                 }
             }
         }
 
         // default language
         if(language == null) {
             language = defaultLanguage;
         }
 
         // default server language
         if(language == null) {
             language = Locale.getDefault().getDisplayLanguage();
         }
 
         return language;
     }
 
     public String getString(final Locale locale, final String key, final String defaultValue) {
         return getString(locale == null ? null : locale.getDisplayLanguage(), key, defaultValue);
     }
 
     public String getString(final String language, final String key, final String defaultValue) {
         final String l = language == null ? defaultLanguage : language;
 
         // try to load bundle if is not loaded
         XmlBundle b = bundles.get(l);
         if(b == null) {
             final File f = new File(this.bundleDir, l + ".xml");
             if(f.exists()) {
                 try {
                     b = new XmlBundle(f);
                     bundles.put(l, b);
                 } catch (Exception ex) {
                     logger.error("Error to load bundle file (" + l + "): " + f);
                 }
             }
         }
 
         final String result = b == null ? defaultValue : b.getString(key);
 
         return result == null ? key : result;
     }
 
     protected boolean isAdmin(ServletContext context, XWebUser user) {
         return false;
     }
 
     protected void storeResourceUsage(ServletContext context, String path) {
 
     }
 
     protected String getUserDirectory(final ServletContext context, final String id) {
         return id;
     }
 
 }
 
