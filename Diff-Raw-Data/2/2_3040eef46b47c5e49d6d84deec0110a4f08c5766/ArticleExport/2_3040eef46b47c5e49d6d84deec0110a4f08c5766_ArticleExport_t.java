 package net.qldarch.ingest.articles;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.io.FileUtils;
 import org.apache.tika.metadata.Metadata;
 import org.apache.tika.parser.ParseContext;
 import org.apache.tika.parser.ParsingReader;
 import org.apache.tika.parser.pdf.PDFParser;
 import org.apache.tika.parser.rtf.RTFParser;
 import org.dom4j.Document;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.XMLWriter;
 import com.google.common.base.Charsets;
 import com.google.common.base.Function;
 import com.google.common.base.Optional;
 import com.google.common.io.CharStreams;
 import com.google.common.io.Closer;
 import org.openrdf.model.Literal;
 import org.openrdf.model.URI;
 import org.openrdf.model.Value;
 import org.openrdf.query.*;
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.http.HTTPRepository;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import net.qldarch.av.parser.TranscriptParser;
 import net.qldarch.ingest.Configuration;
 import net.qldarch.ingest.IngestStage;
 
 public class ArticleExport implements IngestStage {
     public static Logger logger = LoggerFactory.getLogger(ArticleExport.class);
 
     private Configuration configuration;
     private Repository myRepository;
     private RepositoryConnection conn;
     private Exception initError;
     private File outputDir;
 
     public static String OBJECT_WITH_FILE_QUERY =
         " prefix qldarch: <http://qldarch.net/ns/rdf/2012-06/terms#>" +
         " prefix dcterms:<http://purl.org/dc/terms/>" +
         " select distinct ?object ?title ?periodical where {" + 
         "   graph <http://qldarch.net/ns/omeka-export/2013-02-06> {" +
         "     ?object a qldarch:Article ." + 
         "     ?object qldarch:hasFile _:dontcare ." +
         "     OPTIONAL { ?object dcterms:title ?title } ." +
         "     OPTIONAL { ?object qldarch:periodicalTitle ?periodical } ." +
         "   }" +
         " }";
 
     public static String FILE_FOR_OBJECT_QUERY =
         " prefix qldarch: <http://qldarch.net/ns/rdf/2012-06/terms#>" +
         " select ?file ?sysloc ?srcfile ?mimetype where {" + 
         "   graph <http://qldarch.net/ns/omeka-export/2013-02-06> {" +
         "     <%~object~%> qldarch:hasFile ?file ." +
         "     ?file qldarch:systemLocation ?sysloc ." +
         "     ?file qldarch:sourceFilename ?srcfile ." +
         "     ?file qldarch:basicMimeType ?mimetype ." +
         "   }" +
         " }";
 
     public static class ArchiveFile {
         public final java.net.URI fileURI;
         public final String location;
         public final String sourceFile;
         public final String mimetype;
 
         public ArchiveFile(java.net.URI fileURI, String location, String sourceFile, String mimetype) {
             this.fileURI = fileURI;
             this.location = location;
             this.sourceFile = sourceFile;
             this.mimetype = mimetype;
         }
 
         public String toString() {
             return String.format("ArchiveFile(%s, %s, %s, %s)", fileURI, location, sourceFile, mimetype);
         }
     }
 
     public static class ArchiveFileNotFoundException extends Exception {
         public ArchiveFileNotFoundException(String message) {
             super(message);
         }
     }
 
     public static class SummaryFileExistsException extends Exception {
         public SummaryFileExistsException(String message) {
             super(message);
         }
     }
 
     public static class ArchiveFiles implements Iterable<ArchiveFile> {
         private List<ArchiveFile> afs;
 
         public ArchiveFiles() {
             afs = new ArrayList<ArchiveFile>();
         }
 
         public ArchiveFile getByMimeType(String mimetype) throws ArchiveFileNotFoundException {
             for (ArchiveFile af : afs) {
                 if (af.mimetype.equals(mimetype)) {
                     return af;
                 }
             }
             throw new ArchiveFileNotFoundException("ArchiveFile matching " + mimetype + " not found");
         } 
 
         public Optional<ArchiveFile> firstByMimeType(String mimetype) throws ArchiveFileNotFoundException {
             for (ArchiveFile af : afs) {
                 if (af.mimetype.equals(mimetype)) {
                     return Optional.of(af);
                 }
             }
             return Optional.absent();
         } 
 
         public void add(ArchiveFile af) {
             afs.add(af);
         }
 
         public Iterator<ArchiveFile> iterator() {
             return afs.iterator();
         }
 
         public ArchiveFile getFirst() throws ArchiveFileNotFoundException {
             if (afs.size() == 0) {
                 throw new ArchiveFileNotFoundException("No ArchiveFile available");
             }
             return afs.get(0);
         }
 
         public String toString() {
             return afs.toString();
         }
     }
 
     public ArticleExport(Configuration configuration) {
         this.configuration = configuration;
     }
 
     public static Function<Value,String> Value_StringValue = new Function<Value,String>() {
         public String apply(Value value) {
             return value.stringValue();
         }
     };
 
     public void ingest() {
         try {
             logger.info("Connecting to: " + configuration.getEndpoint());
             logger.info("Repository: " + configuration.getRepository());
 
             outputDir = new File(configuration.getOutputDir(), "articles");
             outputDir.mkdirs();
 
             myRepository = new HTTPRepository(configuration.getEndpoint(),
                     configuration.getRepository());
             myRepository.initialize();
 
             conn = myRepository.getConnection();
 
             logger.debug("Performing query: {} ", OBJECT_WITH_FILE_QUERY);
             TupleQueryResult interviewResult = conn.prepareTupleQuery(QueryLanguage.SPARQL, OBJECT_WITH_FILE_QUERY).evaluate();
             while (interviewResult.hasNext()) {
                 BindingSet ibs = interviewResult.next();
                 Value object = ibs.getValue("object");
                 Value titleValue = ibs.getValue("title");
                 Value periodicalValue = ibs.getValue("periodical");
 
                 logger.trace("Retrieved object result: {}, {}, {}", object, titleValue, periodicalValue);
 
                 ArchiveFiles archiveFiles = new ArchiveFiles();
 
                 Optional<String> title = Optional.fromNullable(titleValue).transform(Value_StringValue);
                 Optional<String> periodical = Optional.fromNullable(periodicalValue).transform(Value_StringValue);
 
                 if (!(object instanceof URI)) {
                     logger.warn("object({}) not URI", object);
                 } else {
                     try {
                         String queryString = FILE_FOR_OBJECT_QUERY.replace("%~object~%", object.toString());
                         TupleQueryResult fileResult = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString).evaluate();
                         if (!fileResult.hasNext()) {
                             System.out.println("Error: no results found for query: " + queryString);
                             continue;
                         }
                         while (fileResult.hasNext()) {
                             BindingSet fbs = fileResult.next();
                             Value file = fbs.getValue("file");
                             Value location = fbs.getValue("sysloc");
                             Value sourceFilename = fbs.getValue("srcfile");
                             Value mimetype = fbs.getValue("mimetype");
 
                             if (!(file instanceof URI)) {
                                 System.out.println("file(" + file.toString() + ") not URI");
                             } else if (!(location instanceof Literal)) {
                                 System.out.println("location(" + location.toString() + ") not literal");
                             } else if (!(sourceFilename instanceof Literal)) {
                                 System.out.println("sourceFilename(" + sourceFilename.toString() + ") not literal");
                             } else if (!(mimetype instanceof Literal)) {
                                 System.out.println("mimetype(" + mimetype.toString() + ") not literal");
                             }
 
                             archiveFiles.add(new ArchiveFile(
                                 new java.net.URI(file.toString()),
                                 ((Literal)location).getLabel(),
                                 ((Literal)sourceFilename).getLabel(),
                                 ((Literal)mimetype).getLabel()));
                         }
                         fileResult.close();
 
                         try {
                             URL objectURL = new URL(object.toString());
 
                             writeSummaryFile(objectURL, title, periodical, archiveFiles);
 
                             Optional<ArchiveFile> textFile = archiveFiles.firstByMimeType("text/plain");
                             Optional<ArchiveFile> rtfFile = archiveFiles.firstByMimeType("text/rtf");
                             Optional<ArchiveFile> pdfFile = archiveFiles.firstByMimeType("application/pdf");
                             ArchiveFile sourceFile = null;
                             String bodytext = null;
                             if (textFile.isPresent()) {
                                 bodytext = processTextFile(objectURL, textFile.get());
                                 sourceFile = textFile.get();
                             } else if (rtfFile.isPresent()) {
                                 bodytext = processRtfFile(objectURL, rtfFile.get());
                                 sourceFile = rtfFile.get();
                             } else if (pdfFile.isPresent()) {
                                 bodytext = processRtfFile(objectURL, pdfFile.get());
                                 sourceFile = pdfFile.get();
                             } else {
                                 logger.info("Unable to find suitable file for {}, mimetypes found: {}\n", objectURL, archiveFiles);
                                 continue;
                             }
 
                             writeSolrIngest(sourceFile, objectURL, bodytext, title, periodical);
                         } catch (MalformedURLException em) {
                             em.printStackTrace();
                         } catch (ArchiveFileNotFoundException ea) {
                             ea.printStackTrace();
                         }
                     } catch (URISyntaxException eu) {
                         eu.printStackTrace();
                     } catch (MalformedQueryException em) {
                         em.printStackTrace();
                     } catch (RepositoryException er) {
                         er.printStackTrace();
                     } catch (QueryEvaluationException eq) {
                         eq.printStackTrace();
                     } catch (IOException ei) {
                         System.out.println("IO error processing article(" + object.toString() + "): " + ei.getMessage());
                         ei.printStackTrace();
                     } catch (SummaryFileExistsException es) {
                         logger.warn("Summary File {} already exists", es.getMessage());
                         continue;
                     }
                 }
             }
             interviewResult.close();
         } catch (MalformedQueryException em) {
             em.printStackTrace();
         } catch (RepositoryException er) {
             er.printStackTrace();
         } catch (QueryEvaluationException eq) {
             eq.printStackTrace();
         }
     }
 
     private String processTextFile(URL objectURL, ArchiveFile textFile) throws MalformedURLException, IOException {
        URL locationURL = getArchiveFileURL(textFile);
        return new Scanner(locationURL.openStream()).useDelimiter("\\A").next();
     }
 
     private String processRtfFile(URL objectURL, ArchiveFile rtfFile) throws MalformedURLException, IOException {
         Closer closer = Closer.create();
         try {
             InputStream is = closer.register(getArchiveFileURL(rtfFile).openStream());
             Metadata metadata = new Metadata();
             metadata.set(Metadata.CONTENT_TYPE, rtfFile.mimetype);
 
             ParsingReader reader = closer.register(new ParsingReader(new RTFParser(), is, metadata, new ParseContext()));
 
             return CharStreams.toString(reader);
         } catch (Throwable e) {
             throw closer.rethrow(e, MalformedURLException.class);
         } finally {
             closer.close();
         }
     }
 
     private String processPdfFile(URL objectURL, ArchiveFile pdfFile) throws MalformedURLException, IOException {
         Closer closer = Closer.create();
         try {
             InputStream is = closer.register(getArchiveFileURL(pdfFile).openStream());
             Metadata metadata = new Metadata();
             metadata.set(Metadata.CONTENT_TYPE, pdfFile.mimetype);
 
             ParsingReader reader = closer.register(new ParsingReader(new PDFParser(), is, metadata, new ParseContext()));
 
             return CharStreams.toString(reader);
         } catch (Throwable e) {
             throw closer.rethrow(e, MalformedURLException.class);
         } finally {
             closer.close();
         }
     }
 
     private String urlToFilename(URL url) {
         try {
             return URLEncoder.encode(url.toString(), Charsets.US_ASCII.name());
         } catch (UnsupportedEncodingException eu) {
             throw new IllegalStateException("US_ASCII returned unsupported", eu);
         }
     }
 
     private void writeSummaryFile(URL object, Optional<String> title, Optional<String> periodical, ArchiveFiles afs)
             throws IOException, ArchiveFileNotFoundException, SummaryFileExistsException {
         File summaryFile = new File(outputDir, urlToFilename(object) + ".summary");
         if (summaryFile.exists()) {
             throw new SummaryFileExistsException(summaryFile.toString());
         }
         PrintWriter pw = new PrintWriter(FileUtils.openOutputStream(summaryFile));
         pw.printf("%s:%s\n", "article", object.toString());
         pw.printf("%s:%s\n", "title", title.or(""));
         pw.printf("%s:%s\n", "periodical", periodical.or(""));
         for (ArchiveFile af : afs) {
             pw.printf("file: %s, %s, %s, %s\n", af.fileURI.toString(), af.location, af.sourceFile, af.mimetype);
         }
         pw.flush();
         pw.close();
     }
 
     private URL getArchiveFileURL(ArchiveFile archiveFile) throws MalformedURLException {
         return new URL(new URL(configuration.getArchivePrefix()), archiveFile.location);
     }
 
     private void writeSolrIngest(ArchiveFile source, URL article, String bodytext,
             Optional<String> title, Optional<String> periodical) throws IOException {
         File xmlFile = new File(outputDir, urlToFilename(article) + "-solr.xml");
         if (xmlFile.exists()) {
             logger.info("Error, {} already exists", xmlFile);
             return;
         }
 
         Document document = DocumentHelper.createDocument();
         Element root = document.addElement("add")
             .addAttribute("commitWithin", "30000")
             .addAttribute("overwrite", configuration.getSolrOverwrite() ? "true" : "false");
 
         Element doc = root.addElement("doc");
         doc.addElement("field")
             .addAttribute("name", "id")
             .addText(article.toString());
         doc.addElement("field")
             .addAttribute("name", "content")
             .addText(bodytext.toString());
         if (title.isPresent()) {
             doc.addElement("field")
                 .addAttribute("name", "title")
                 .addText(title.get());
         }
         if (title.isPresent()) {
             doc.addElement("field")
                 .addAttribute("name", "periodical")
                 .addText(periodical.get());
         }
         doc.addElement("field")
             .addAttribute("url", "system_location")
             .addText(source.location);
         doc.addElement("field")
             .addAttribute("url", "original_filename")
             .addText(source.sourceFile);
 
         XMLWriter writer = new XMLWriter(FileUtils.openOutputStream(xmlFile),
                 OutputFormat.createPrettyPrint());
         writer.write(document);
         writer.close();
     }
 }
