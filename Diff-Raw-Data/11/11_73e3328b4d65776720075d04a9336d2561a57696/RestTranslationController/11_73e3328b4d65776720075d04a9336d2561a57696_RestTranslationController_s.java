 /**********************************************************************************
  * Copyright (c) 2011, Monnet Project
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the Monnet Project nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *********************************************************************************/
 package eu.monnetproject.translation.controller.webservice;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.openrdf.model.Literal;
 import org.openrdf.model.ValueFactory;
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.sail.SailRepository;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.rdfxml.util.RDFXMLPrettyWriter;
 import org.openrdf.rio.turtle.TurtleWriter;
 import org.openrdf.sail.memory.MemoryStore;
 import org.w3c.dom.Document;
 
 import eu.monnetproject.data.DataSource;
 import eu.monnetproject.label.LabelExtractor;
 import eu.monnetproject.label.custom.CustomLabelExtractionPolicy;
 import eu.monnetproject.label.rdf.RDFLabelExtractionPolicy;
 import eu.monnetproject.label.skos.SKOSLabelExtractionPolicy;
 import eu.monnetproject.lang.Language;
 import eu.monnetproject.lemon.LemonModel;
 import eu.monnetproject.lemon.LemonModels;
 import eu.monnetproject.lemon.LemonSerializer;
 import eu.monnetproject.lemon.conversions.skosxl.SKOSXLConverter;
 import eu.monnetproject.lemon.model.LexicalEntry;
 import eu.monnetproject.lemon.model.LexicalSense;
 import eu.monnetproject.lemon.model.Lexicon;
 import eu.monnetproject.ontology.Entity;
 import eu.monnetproject.ontology.Ontology;
 import eu.monnetproject.ontology.OntologySerializer;
 import eu.monnetproject.translation.OntologyTranslator;
 import eu.monnetproject.translation.monitor.Messages;
 
 /**
  *
  * @author John McCrae
  */
 public class RestTranslationController extends HttpServlet {
     private static final long serialVersionUID = 1L;
 
     private final OntologySerializer ontologySerializer;// = Services.get(OntologySerializer.class);
     private final OntologyTranslator controller;// = Services.getAll(OntologyTranslator.class).iterator().next();
 
     private final LemonSerializer lemonSerializer = LemonSerializer.newInstance();
     //private final LabelExtractorFactory labelExtractorFactory = Services.get(LabelExtractorFactory.class);
     private static final URI RDFS = URI.create("http://www.w3.org/2000/01/rdf-schema#");
     private static final URI SKOSXL = URI.create("http://www.w3.org/2008/05/skos-xl#");
 
     public RestTranslationController(OntologySerializer ontologySerializer, OntologyTranslator controller) {
         this.ontologySerializer = ontologySerializer;
         this.controller = controller;
         // See TranslationController: Ensures that if there is no weight file for the given langauge pair
         // The service won't attempt to translate
         System.setProperty("eu.monnetproject.translation.controller.untrainedPairs", "false");
     }
 
     
     public String getPath() {
         return "/translate";
     }
 
     @Override
     protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         if (!ServletFileUpload.isMultipartContent(req)) {
             resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
         } else {
             try {
                 FileItemFactory factory = new DiskFileItemFactory();
 
                 ServletFileUpload upload = new ServletFileUpload(factory);
 
                 List<?> items = upload.parseRequest(req);
 
                 Params params = new Params();
                 
                 for (Object object : items) {
                     FileItem fi = (FileItem) object;
                     if (fi.getFieldName().equalsIgnoreCase("ontology")) {
                         params.ontology = fi.getInputStream();
                     } else if (fi.getFieldName().equalsIgnoreCase("target-language")) {
                         params.targetLanguage = Language.get(fi.getString());
                     } else if (fi.getFieldName().equalsIgnoreCase("scope")) {
                         if(!fi.getString().matches("\\s*")){
                             final String[] urisAsStr = fi.getString().split(",");
                             for (String uriAsStrt : urisAsStr) {
                                 params.scope.add(URI.create(uriAsStrt.trim()));
                             }
                         }
                     } else if (fi.getFieldName().equalsIgnoreCase("name-prefix")) {
                         if(!fi.getString().matches("\\s*")){
                             params.namePrefix = fi.getString();
                         }
                     } else if (fi.getFieldName().equalsIgnoreCase("custom-label")) {
                         if(!fi.getString().matches("\\s*")) {
                             params.customLabel = URI.create(fi.getString());
                         }
                     } else if (fi.getFieldName().equalsIgnoreCase("n-best")) {
                         if(!fi.getString().matches("\\s*")) {
                             params.nBest = Integer.parseInt(fi.getString());
                         }
                     } else if (fi.getFieldName().equalsIgnoreCase("accept-vocabularies")) {
                         if(!fi.getString().matches("\\s*")) {
                             final String[] urisAsStr = fi.getString().split(",");
                             for (String uriAsStr : urisAsStr) {
                                 params.acceptVocabularies.add(URI.create(uriAsStr.trim()));
                             }
                         }
                     } else if (fi.getFieldName().equalsIgnoreCase("include-source")) {
                         if(!fi.getString().matches("\\s*")) {
                             params.includeSource = fi.getString().equalsIgnoreCase("true") || fi.getString().equals("yes") || fi.getString().equalsIgnoreCase("y") || fi.getString().equalsIgnoreCase("t");
                         }
                     } else if (fi.getFieldName().equalsIgnoreCase("estimate-confidence")) {
                         if(!fi.getString().matches("\\s*")) {
                             params.confidenceEstimation = fi.getString().equalsIgnoreCase("true") || fi.getString().equals("yes") || fi.getString().equalsIgnoreCase("y") || fi.getString().equalsIgnoreCase("t");
                         }
                     } else if (fi.getFieldName().equalsIgnoreCase("speed")) {
                         if(!fi.getString().matches("\\s*")) {
                             params.fast = !fi.getString().equalsIgnoreCase("normal");
                         }
                     } else {
                         System.err.println("Bad field name:" + fi.getFieldName());
                         resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                         return;
                     }
                 }
                 if(params.ontology == null) {
                     resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No ontology");
                     return;
                 }
                 if(params.targetLanguage == null) {
                     resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No target language");
                     return;
                 }
                 final String characterEncoding = req.getCharacterEncoding() != null ? req.getCharacterEncoding() : "UTF-8";
                 resp.setCharacterEncoding(characterEncoding);
                 final Ontology ontology = ontologySerializer.read(new InputStreamReader(params.ontology, characterEncoding));
                 final LemonModel lemonModel = lemonSerializer.read(new InputStreamReader(params.ontology));
                 final HashMap<Language, Lexicon> lexica = new HashMap<Language, Lexicon>();
                 final Collection<Lexicon> sourceLexica;
                 Set<URI> inputVocabulary = new HashSet<URI>();
                 // Check for lemon lexica submitted
                 if (lemonModel.getLexica().isEmpty()) {
 
                     // Create new lexica
                     sourceLexica = new LinkedList<Lexicon>();
                     LabelExtractor extractor = new RDFLabelExtractionPolicy();
                     if (extractLabels(ontology, extractor, lexica, lemonModel, params, sourceLexica)) {
                         inputVocabulary.add(RDFS);
                     }
                     extractor = new SKOSLabelExtractionPolicy();
                     if (extractLabels(ontology, extractor, lexica, lemonModel, params, sourceLexica)) {
                         inputVocabulary.add(SKOSXL);
                     }
                     if (params.customLabel == null) {
                         extractor = new CustomLabelExtractionPolicy(params.customLabel);
                         if (extractLabels(ontology, extractor, lexica, lemonModel, params, sourceLexica)) {
                             inputVocabulary.add(SKOSXL);
                         }
                     }
                 } else {
                     inputVocabulary.add(URI.create(LemonModel.LEMON_URI));
                     sourceLexica = lemonModel.getLexica();
                 }
                 if (lemonModel.getLexica().isEmpty()) {
                     resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No labels found in ontology; please submit a lemon lexicon, or use xml:lang tags on your entries");
                     return;
                 }
                 final Lexicon targetLexicon = lemonModel.addLexicon(params.namePrefix != null ? URI.create(params.namePrefix + "lexicon__" + params.targetLanguage) : URI.create("temp:lexicon__" + params.targetLanguage), params.targetLanguage.toString());
 
                 controller.translate(ontology, sourceLexica, targetLexicon,
                         params.scope, params.namePrefix, params.nBest, (params.confidenceEstimation ? OntologyTranslator.ESTIMATE_CONFIDENCE : 0) |
                         (params.fast ? OntologyTranslator.DECODE_FAST + OntologyTranslator.NO_SEMANTIC_PROCESSING : 0));
 
                 Repository repo = new SailRepository(new MemoryStore());
                 repo.initialize();
                 RepositoryConnection conn = null;
                 try {
                     conn = repo.getConnection();
                     if (params.acceptVocabularies.isEmpty()) {
                         params.acceptVocabularies.addAll(inputVocabulary);
                     }
 
                     if (params.acceptVocabularies.contains(RDFS)) {
                         final ValueFactory valueFactory = conn.getValueFactory();
                         final org.openrdf.model.URI rdfsLabel = valueFactory.createURI(RDFS + "label");
                         for (Lexicon lexicon : lemonModel.getLexica()) {
                             for (LexicalEntry entry : lexicon.getEntrys()) {
                                 for (LexicalSense sense : entry.getSenses()) {
                                     final org.openrdf.model.URI ent = valueFactory.createURI(sense.getReference().toString());
                                     final Literal lit = valueFactory.createLiteral(entry.getCanonicalForm().getWrittenRep().value, entry.getCanonicalForm().getWrittenRep().language);
                                     conn.add(ent, rdfsLabel, lit);
                                 }
                             }
                         }
                     }
                     if (params.acceptVocabularies.contains(URI.create(LemonModel.LEMON_URI))) {
                         final StringWriter sw = new StringWriter();
                         lemonSerializer.write(lemonModel, sw);
                         conn.add(new StringReader(sw.toString()), "unknown:lexicon", RDFFormat.RDFXML);
                     }
                     if (params.acceptVocabularies.contains(SKOSXL)) {
                         final Document doc = SKOSXLConverter.convert(lemonModel);
                         final TransformerFactory transformerFactory = TransformerFactory.newInstance();
 
                         Transformer trans = transformerFactory.newTransformer();
                         StreamResult streamResult = new StreamResult(new StringWriter());
                         DOMSource source = new DOMSource(doc);
                         trans.transform(source, streamResult);
                        // log.info(streamResult.getWriter().toString());
                         conn.add(new StringReader(streamResult.getWriter().toString()), "unknown:lexicon", RDFFormat.RDFXML);
                     }
                     if(getFirstAcceptType(req).equals(TURTLE)) {
                         resp.setContentType(TURTLE);
                         conn.export(new TurtleWriter(resp.getWriter()));
                     } else {
                         resp.setContentType("application/rdf+xml");
                         conn.export(new RDFXMLPrettyWriter(resp.getWriter()));
                     }
                 } catch (Exception x) {
                     throw new ServletException(x);
                 } finally {
                     if (conn != null) {
                         conn.close();
                     }
                     repo.shutDown();
                 }
             } catch (Throwable x) {
                 throw new ServletException(x);
             }
         }
     }
 
     private boolean extractLabels(final Ontology ontology, final LabelExtractor extractor, final HashMap<Language, Lexicon> lexica, final LemonModel lemonModel, Params params, final Collection<Lexicon> sourceLexica) throws UnsupportedEncodingException {
         final Collection<Entity> entities = ontology.getEntities();
         boolean found = false;
         for (Entity entity : entities) {
             final Map<Language, Collection<String>> labels = extractor.getLabels(entity);
             for (Map.Entry<Language, Collection<String>> entry : labels.entrySet()) {
                 final Language labelLang = entry.getKey();
                 for (String label : entry.getValue()) {
                     if (!labelLang.equals(LabelExtractor.NO_LANGUAGE)) {
                         // Check if lexica already created
                         if (!lexica.containsKey(labelLang)) {
                             final Lexicon lexicon = lemonModel.addLexicon(params.namePrefix != null ? URI.create(params.namePrefix + "lexicon__" + labelLang) : URI.create("temp:lexicon__" + labelLang), labelLang.toString());
                             lexica.put(labelLang, lexicon);
                             sourceLexica.add(lexicon);
                         }
                         found = true;
                         LemonModels.addEntryToLexicon(lexica.get(labelLang),
                                 params.namePrefix != null
                                 ? URI.create(params.namePrefix + "lexicon__" + labelLang + "/" + URLEncoder.encode(label, "UTF-8"))
                                 : URI.create("temp:lexicon__" + labelLang + "/" + URLEncoder.encode(label, "UTF-8")), label, entity.getURI());
                     }
                 }
             }
         }
         return found;
     }
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         resp.setContentType("text/html");
         final PrintWriter out = new PrintWriter(resp.getWriter());
         out.println("<html><head></head><body>");
         out.println("<form action='' method='post' enctype='multipart/form-data'>");
         out.println("<label for='ontology'>Ontology</label><input type='file' name='ontology' id='ontology'/><br/>");
         out.println("<label for='target-language'>Target Language</label><input type='text' name='target-language' id='target-language'/><br/>");
         out.println("<label for='scope'>Scope</label><input type='text' name='scope' id='scope'/><br/>");
         out.println("<label for='name-prefix'>Name prefix</label><input type='text' name='name-prefix' id='name-prefix'/><br/>");
         out.println("<label for='custom-label'>Custom label property</label><input type='text' name='custom-label' id='custom-label'/><br/>");
         out.println("<label for='n-best'>Number of results to return</label><input type='text' name='n-best' id='n-best'/><br/>");
        out.println("<label for='accept-vocabularies'>Return vocabulary (RDFS="+RDFS+",SKOS-XL"+SKOSXL+",LEMON=http://www.monnet.project.eu/lemon#)</label><input type='text' name='accept-vocabularies' id='accept-vocabularies'/><br/>");
         out.println("<label for='include-source'>Include source</label><input type='checkbox' name='include-source' id='include-source'/><br/>");
         out.println("<label for='estimate-confidence'>Estimate confidence</label><input type='checkbox' name='estimate-confidence' id='estimate-confidence'/><br/>");
         out.println("<input type='submit'/>");
         out.println("</form></body></html>");
     }
     private final String RDFXML = "application/rdf+xml";
     private final String TURTLE = "application/x-turtle";
     private final String TURTLE2 = "text/turtle";
     private final String TURTLE3 = "application/rdf+turtle";
 
     private String getFirstAcceptType(HttpServletRequest req) {
         if (req.getHeader("Accept") == null) {
             return RDFXML;
         } else {
             for (String typ : req.getHeader("Accept").split(",")) {
                 String typAct = typ;
                 if (typAct.indexOf(";") > 0) {
                     typAct = typAct.substring(0, typAct.indexOf(";"));
                 }
                 if (typAct.equals(TURTLE) || typAct.equals(TURTLE2) || typAct.equals(TURTLE3)) {
                     return TURTLE;
                 } else if (typAct.equals(RDFXML)) {
                     return RDFXML;
                 }
             }
             return RDFXML;
         }
     }
 
     private static class Params {
 
         InputStream ontology;
         Language targetLanguage;
         List<URI> scope = new LinkedList<URI>();
         String namePrefix = null;
         URI customLabel = null;
         int nBest = 1;
         List<URI> acceptVocabularies = new LinkedList<URI>();
         boolean includeSource = true;
         boolean confidenceEstimation = false;
         boolean fast = true;
     }
 
     private static class InputStreamDataSource implements DataSource {
 
         private final InputStream is;
         private final String charSetName;
 
         public InputStreamDataSource(InputStream is, String charSetName) {
             this.is = is;
             this.charSetName = charSetName;
         }
 
         @Override
         public URL asURL() throws UnsupportedOperationException {
             throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public InputStream asInputStream() throws UnsupportedOperationException {
             return is;
         }
 
         @Override
         public Reader asReader() throws UnsupportedOperationException {
             try {
                 return new InputStreamReader(is, charSetName);
             } catch (Exception x) {
                 throw new RuntimeException(x);
             }
         }
 
         @Override
         public File asFile() throws UnsupportedOperationException {
             throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public String getMIMEType() {
             return "application/rdf+xml";
         }
     }
 }
