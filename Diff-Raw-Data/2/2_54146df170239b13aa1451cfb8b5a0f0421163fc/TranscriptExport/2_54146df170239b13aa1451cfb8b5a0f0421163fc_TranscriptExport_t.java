 package net.qldarch.ingest.transcript;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.io.FileUtils;
 import org.dom4j.Document;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.XMLWriter;
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
 
 public class TranscriptExport implements IngestStage {
     public static Logger logger = LoggerFactory.getLogger(TranscriptExport.class);
 
     private Configuration configuration;
     private Repository myRepository;
     private RepositoryConnection conn;
     private Exception initError;
 
     public static String INTERVIEW_QUERY =
         " prefix qldarch: <http://qldarch.net/ns/rdf/2012-06/terms#>" +
        " select ?interview ?transcript where {" + 
         "   graph <http://qldarch.net/ns/omeka-export/2013-02-06> {" +
         "     ?interview a qldarch:Interview ." + 
         "     ?transcript a qldarch:Transcript ." +
         "     ?interview qldarch:hasTranscript ?transcript ." +
         "   }" +
         " }";
 
     public static String TRANSCRIPT_QUERY =
         " prefix qldarch: <http://qldarch.net/ns/rdf/2012-06/terms#>" +
         " select ?file ?tloc ?srcfile ?mimetype where {" + 
         "   graph <http://qldarch.net/ns/omeka-export/2013-02-06> {" +
         "     <%~transcript~%> qldarch:hasFile ?file ." +
         "     ?file qldarch:systemLocation ?tloc ." +
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
     }
 
     public static class ArchiveFileNotFoundException extends Exception {
         public ArchiveFileNotFoundException(String message) {
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
                 System.out.printf("%s:%s == %s ?\n", af.fileURI.toString(), af.mimetype, mimetype);
                 if (af.mimetype.equals(mimetype)) {
                     return af;
                 }
             }
             throw new ArchiveFileNotFoundException("ArchiveFile matching " + mimetype + " not found");
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
     }
 
     public TranscriptExport(Configuration configuration) {
         this.configuration = configuration;
     }
 
     public void ingest() {
         try {
             logger.warn("Connecting to: " + configuration.getEndpoint());
             logger.warn("Repository: " + configuration.getRepository());
 
             File output = new File(configuration.getOutputDir(), "transcripts");
             output.mkdirs();
 
             myRepository = new HTTPRepository(configuration.getEndpoint(),
                     configuration.getRepository());
             myRepository.initialize();
 
             conn = myRepository.getConnection();
 
             TupleQueryResult interviewResult = conn.prepareTupleQuery(QueryLanguage.SPARQL, INTERVIEW_QUERY).evaluate();
             while (interviewResult.hasNext()) {
                 BindingSet ibs = interviewResult.next();
                 Value interview = ibs.getValue("interview");
                 Value transcript = ibs.getValue("transcript");
                 ArchiveFiles archiveFiles = new ArchiveFiles();
 
                 if (!(interview instanceof URI)) {
                     System.out.println("interview(" + interview.toString() + ") not URI");
                 } else if (!(transcript instanceof URI)) {
                     System.out.println("transcript(" + transcript.toString() + ") not URI");
                 } else {
                     try {
                         TupleQueryResult fileResult = conn.prepareTupleQuery(QueryLanguage.SPARQL, TRANSCRIPT_QUERY.replace("%~transcript~%", transcript.toString())).evaluate();
                         if (!fileResult.hasNext()) {
                             System.out.println("Error: no results found for query: " + TRANSCRIPT_QUERY.replace("%~transcript~%", transcript.toString()));
                             continue;
                         }
                         while (fileResult.hasNext()) {
                             BindingSet fbs = fileResult.next();
                             Value file = fbs.getValue("file");
                             Value location = fbs.getValue("tloc");
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
                             URL interviewURL = new URL(interview.toString());
                             URL transcriptURL = new URL(transcript.toString());
 
                             writeSummaryFile(interviewURL, transcriptURL, archiveFiles);
 
                             ArchiveFile textFile = archiveFiles.getByMimeType("text/plain");
 
                             URL locationURL = new URL(new URL(configuration.getArchivePrefix()), textFile.location);
 
                             TranscriptParser parser = new TranscriptParser(locationURL.openStream());
                             try {
                                 parser.parse();
                             } catch (IllegalStateException ei) {
                                 System.out.println(interview.toString() + " " + transcript.toString() + " "
                                        + locationURL.toString() + " " + ei.getMessage());
                                 continue;
                             }
 
                             System.out.println(interview.toString() + " " +
                                     transcript.toString() + " " +
                                     parser.getTitle());
                             writeJsonTranscript(textFile.sourceFile, parser);
                             writeSolrIngest(textFile.sourceFile, interviewURL, transcriptURL, parser);
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
                         System.out.println("IO error processing interview(" + interview.toString() + "): " + ei.getMessage());
                         ei.printStackTrace();
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
 
     private void writeSummaryFile(URL interview, URL transcript, ArchiveFiles afs)
             throws IOException, ArchiveFileNotFoundException {
         File summaryFile = new File(configuration.getOutputDir(),
                 FilenameUtils.getBaseName(afs.getFirst().sourceFile) + ".summary");
         PrintWriter pw = new PrintWriter(FileUtils.openOutputStream(summaryFile));
         pw.printf("%s:%s\n", "interview", interview.toString());
         pw.printf("%s:%s\n", "transcript", transcript.toString());
         for (ArchiveFile af : afs) {
             pw.printf("file:%s:%s:%s:%s\n", af.fileURI.toString(), af.location, af.sourceFile, af.mimetype);
         }
         pw.flush();
         pw.close();
     }
 
     private void writeJsonTranscript(String source, TranscriptParser parser) throws IOException {
         File jsonFile = new File(configuration.getOutputDir(),
                 FilenameUtils.getBaseName(source) + ".json");
         if (jsonFile.exists()) {
             System.out.println("Error, " + jsonFile + " already exists");
             return;
         }
         PrintStream ps = new PrintStream(FileUtils.openOutputStream(jsonFile));
         parser.printJson(ps);
         ps.flush();
         ps.close();
     }
 
     private void writeSolrIngest(String source, URL interview, URL transcript,
             TranscriptParser parser) throws IOException {
         File xmlFile = new File(configuration.getOutputDir(),
                 FilenameUtils.getBaseName(source) + "-solr.xml");
         if (xmlFile.exists()) {
             System.out.println("Error, " + xmlFile + " already exists");
             return;
         }
 
         Document document = DocumentHelper.createDocument();
         Element root = document.addElement("add")
             .addAttribute("commitWithin", "30000")
             .addAttribute("overwrite", configuration.getSolrOverwrite() ? "true" : "false");
 
         for (TranscriptParser.Utterance entry : parser.getInterview()) {
             Element doc = root.addElement("doc");
             doc.addElement("field")
                 .addAttribute("name", "id")
                 .addText(transcript.toString() + "#" + entry.getTimestamp());
             doc.addElement("field")
                 .addAttribute("name", "interview")
                 .addText(interview.toString());
             doc.addElement("field")
                 .addAttribute("name", "transcript")
                 .addText(entry.getUtterance());
         }
 
         XMLWriter writer = new XMLWriter(FileUtils.openOutputStream(xmlFile),
                 OutputFormat.createPrettyPrint());
         writer.write(document);
         writer.close();
     }
 }
