 package net.qldarch.ingest.transcript;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import com.google.common.base.Optional;
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
 import net.qldarch.ingest.archive.ArchiveFile;
 import net.qldarch.ingest.archive.ArchiveFileNotFoundException;
 import net.qldarch.ingest.archive.ArchiveFiles;
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
                                 transcript.toString(),
                                 new java.net.URI(file.toString()),
                                 ((Literal)location).getLabel(),
                                 ((Literal)sourceFilename).getLabel(),
                                 ((Literal)mimetype).getLabel()));
                         }
                         fileResult.close();
 
                         try {
                             URL interviewURL = new URL(interview.toString());
                             URL transcriptURL = new URL(transcript.toString());
 
                             Properties summary =
                                 prepareSummary(interviewURL, transcriptURL, archiveFiles);
 
                             try {
                                 Optional<ArchiveFile> textFile =
                                     archiveFiles.firstByMimeType("text/plain").or(
                                     archiveFiles.firstByMimeType("text/rtf")).or(
                                     archiveFiles.firstByMimeType("application/msword"));
 
                                 TranscriptParser parser;
                                 if (textFile.isPresent()) {
                                     parser = new TranscriptParser(
                                             textFile.get().toReader(configuration.getArchivePrefix()));
                                 } else {
                                     System.out.println(
                                             "No suitable file found for transcript " +
                                             transcript + " among " + archiveFiles);
                                     continue;
                                 }
 
                                 try {
                                     parser.parse();
                                 } catch (IllegalStateException ei) {
                                     System.out.println(interview.toString() + " " +
                                            transcript.toString() + " " +
                                            ei.getMessage());
                                     continue;
                                 }
 
                                 System.out.println(
                                         interview.toString() + " " +
                                         transcript.toString() + " " +
                                         parser.getTitle());
 
                                 writeJsonTranscript(textFile.get().sourceFile, parser, summary);
 
                                 writeSolrIngest(textFile.get().sourceFile, interviewURL,
                                         transcriptURL, parser, summary);
                             } finally {
                                 writeSummaryFile(archiveFiles, summary);
                             }
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
 
     private Properties prepareSummary(URL interview, URL transcript, ArchiveFiles afs) {
         Properties summary = new Properties();
         summary.setProperty("interview", interview.toString());
         summary.setProperty("transcript", transcript.toString());
         int i = 0;
         for (ArchiveFile af : afs) {
             summary.setProperty(
                     String.format("%s.%d", "file", i++),
                     String.format("%s, %s, %s, %s", af.fileURI.toString(), af.location,
                         af.sourceFile, af.mimetype));
         }
 
         return summary;
     }
 
     private void writeSummaryFile(ArchiveFiles afs, Properties summary) throws IOException, ArchiveFileNotFoundException {
         File summaryFile = new File(configuration.getOutputDir(),
                 FilenameUtils.getBaseName(afs.getFirst().sourceFile) + ".summary");
         OutputStream os = FileUtils.openOutputStream(summaryFile);
         summary.store(os, new Date().toString());
         os.flush();
         os.close();
     }
 
     private void writeJsonTranscript(String source, TranscriptParser parser, Properties summary)
             throws IOException {
         File jsonFile = new File(configuration.getOutputDir(),
                 FilenameUtils.getBaseName(source) + ".json");
         if (jsonFile.exists()) {
             summary.setProperty("json", jsonFile.getAbsoluteFile().getCanonicalFile().toString());
             System.out.println("Error, " + jsonFile + " already exists");
             return;
         }
         PrintStream ps = new PrintStream(FileUtils.openOutputStream(jsonFile));
         parser.printJson(ps);
         ps.flush();
         ps.close();
         summary.setProperty("json", jsonFile.getAbsoluteFile().getCanonicalFile().toString());
     }
 
     private void writeSolrIngest(String source, URL interview, URL transcript,
             TranscriptParser parser, Properties summary) throws IOException {
         File xmlFile = new File(configuration.getOutputDir(),
                 FilenameUtils.getBaseName(source) + "-solr.xml");
         if (xmlFile.exists()) {
             summary.setProperty("solr.input",
                     xmlFile.getAbsoluteFile().getCanonicalFile().toString());
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
        summary.setProperty("solr", xmlFile.getAbsoluteFile().getCanonicalFile().toString());
     }
 }
