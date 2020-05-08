 package uk.ac.ebi.arrayexpress.components;
 
 /*
  * Copyright 2009-2011 European Molecular Biology Laboratory
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 import net.sf.saxon.om.DocumentInfo;
 import net.sf.saxon.xpath.XPathEvaluator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import uk.ac.ebi.arrayexpress.app.Application;
 import uk.ac.ebi.arrayexpress.app.ApplicationComponent;
 import uk.ac.ebi.arrayexpress.components.Events.IEventInformation;
 import uk.ac.ebi.arrayexpress.utils.RegexHelper;
 import uk.ac.ebi.arrayexpress.utils.StringTools;
 import uk.ac.ebi.arrayexpress.utils.persistence.PersistableDocumentContainer;
 import uk.ac.ebi.arrayexpress.utils.persistence.PersistableString;
 import uk.ac.ebi.arrayexpress.utils.persistence.PersistableStringList;
 import uk.ac.ebi.arrayexpress.utils.persistence.TextFilePersistence;
 import uk.ac.ebi.arrayexpress.utils.saxon.DocumentUpdater;
 import uk.ac.ebi.arrayexpress.utils.saxon.ExtFunctions;
 import uk.ac.ebi.arrayexpress.utils.saxon.IDocumentSource;
 
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import java.io.File;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class Experiments extends ApplicationComponent implements IDocumentSource
 {
     // logging machinery
     private final Logger logger = LoggerFactory.getLogger(getClass());
 
     private final RegexHelper ARRAY_ACCESSION_REGEX = new RegexHelper("^[aA]-\\w{4}-\\d+$", "");
 
     private TextFilePersistence<PersistableDocumentContainer> document;
     private TextFilePersistence<PersistableStringList> experimentsInAtlas;
     private TextFilePersistence<PersistableString> species;
     private TextFilePersistence<PersistableString> arrays;
     private Map<String, String> assaysByMolecule;
     private Map<String, String> assaysByInstrument;
 
     private SaxonEngine saxon;
     private SearchEngine search;
     private Events events;
     private Autocompletion autocompletion;
 
     public final String INDEX_ID = "experiments";
 
     public enum ExperimentSource
     {
         AE1, AE2;
 
         public String getStylesheetName()
         {
             switch (this) {
                 case AE1:   return "preprocess-experiments-ae1-xml.xsl";
                 case AE2:   return "preprocess-experiments-ae2-xml.xsl";
             }
             return null;
         }
 
         public String toString()
         {
             switch (this) {
                 case AE1:   return "AE1";
                 case AE2:   return "AE2";
             }
             return null;
 
         }
     }
 
     public static class UpdateSourceInformation implements IEventInformation
     {
         private ExperimentSource source;
         private String location = null;
         private Long lastModified = null;
         private boolean outcome;
 
         public UpdateSourceInformation( ExperimentSource source, File sourceFile )
         {
             this.source = source;
             if (null != sourceFile && sourceFile.exists()) {
                 this.location = sourceFile.getAbsolutePath();
                 this.lastModified = sourceFile.lastModified();
             }
         }
 
         public UpdateSourceInformation( ExperimentSource source, String location, Long lastModified )
         {
             this.source = source;
             this.location = location;
             this.lastModified = lastModified;
         }
 
         public void setOutcome( boolean outcome )
         {
             this.outcome = outcome;
         }
 
         public ExperimentSource getSource()
         {
             return this.source;
         }
 
         public DocumentInfo getEventXML() throws Exception
         {
             String xml = "<?xml version=\"1.0\"?><event><category>experiments-update-"
                             + this.source.toString().toLowerCase()
                             + "</category><location>"
                             + this.location + "</location><lastmodified>"
                             + StringTools.longDateTimeToXSDDateTime(lastModified)
                             + "</lastmodified><successful>"
                             + (this.outcome ? "true" : "false")
                             + "</successful></event>";
 
             return ((SaxonEngine) Application.getAppComponent("SaxonEngine")).buildDocument(xml);
         }
     }
 
     public Experiments()
     {
     }
 
     public void initialize() throws Exception
     {
         this.saxon = (SaxonEngine) getComponent("SaxonEngine");
         this.search = (SearchEngine) getComponent("SearchEngine");
         this.events = (Events) getComponent("Events");
         this.autocompletion = (Autocompletion) getComponent("Autocompletion");
 
         this.document = new TextFilePersistence<PersistableDocumentContainer>(
                 new PersistableDocumentContainer("experiments")
                 , new File(getPreferences().getString("ae.experiments.persistence-location"))
         );
 
         this.experimentsInAtlas = new TextFilePersistence<PersistableStringList>(
                 new PersistableStringList()
                 , new File(getPreferences().getString("ae.atlasexperiments.persistence-location"))
         );
 
         this.species = new TextFilePersistence<PersistableString>(
                 new PersistableString()
                 , new File(getPreferences().getString("ae.species.dropdown-html-location"))
 
         );
 
         this.arrays = new TextFilePersistence<PersistableString>(
                 new PersistableString()
                 , new File(getPreferences().getString("ae.arrays.dropdown-html-location"))
         );
 
         this.assaysByMolecule = new HashMap<String, String>();
         this.assaysByMolecule.put("", "<option value=\"\">All assays by molecule</option><option value=\"&quot;DNA assay&quot;\">DNA assay</option><option value=\"&quot;metabolomic profiling&quot;\">Metabolite assay</option><option value=\"&quot;protein assay&quot;\">Protein assay</option><option value=\"&quot;RNA assay&quot;\">RNA assay</option>");
         this.assaysByMolecule.put("array assay", "<option value=\"\">All assays by molecule</option><option value=\"&quot;DNA assay&quot;\">DNA assay</option><option value=\"&quot;RNA assay&quot;\">RNA assay</option>");
         this.assaysByMolecule.put("high throughput sequencing assay", "<option value=\"\">All assays by molecule</option><option value=\"&quot;DNA assay&quot;\">DNA assay</option><option value=\"&quot;RNA assay&quot;\">RNA assay</option>");
         this.assaysByMolecule.put("proteomic profiling by mass spectrometer", "<option value=\"&quot;protein assay&quot;\">Protein assay</option>");
 
         this.assaysByInstrument = new HashMap<String, String>();
         this.assaysByInstrument.put("", "<option value=\"\">All technologies</option><option value=\"&quot;array assay&quot;\">Array</option><option value=\"&quot;high throughput sequencing assay&quot;\">High-throughput sequencing</option><option value=\"&quot;proteomic profiling by mass spectrometer&quot;\">Mass spectrometer</option>");
         this.assaysByInstrument.put("DNA assay", "<option value=\"\">All technologies</option><option value=\"&quot;array assay&quot;\">Array</option><option value=\"&quot;high throughput sequencing assay&quot;\">High-throughput sequencing</option>");
         this.assaysByInstrument.put("metabolomic profiling", "<option value=\"\">All technologies</option>");
         this.assaysByInstrument.put("protein assay", "<option value=\"\">All technologies</option><option value=\"&quot;proteomic profiling by mass spectrometer&quot;\">Mass spectrometer</option>");
         this.assaysByInstrument.put("RNA assay", "<option value=\"\">All technologies</option><option value=\"&quot;array assay&quot;\">Array</option><option value=\"&quot;high throughput sequencing assay&quot;\">High-throughput sequencing</option>");
 
         updateIndex();
         updateAccelerators();
         this.saxon.registerDocumentSource(this);
     }
 
     public void terminate() throws Exception
     {
     }
 
     // implementation of IDocumentSource.getDocumentURI()
     public String getDocumentURI()
     {
         return "experiments.xml";
     }
 
     // implementation of IDocumentSource.getDocument()
     public synchronized DocumentInfo getDocument() throws Exception
     {
         return this.document.getObject().getDocument();
     }
 
     // implementation of IDocumentSource.setDocument(DocumentInfo)
     public synchronized void setDocument( DocumentInfo doc ) throws Exception
     {
         if (null != doc) {
             this.document.setObject(new PersistableDocumentContainer("experiments", doc));
             buildSpeciesArrays();
             updateIndex();
            updateAccelerators();
         } else {
             this.logger.error("Experiments NOT updated, NULL document passed");
         }
     }
 
     public boolean isAccessible( String accession, List<String> userIds ) throws Exception
     {
         for (String userId : userIds) {
             if ( "0".equals(userId)                         // superuser
                 || ARRAY_ACCESSION_REGEX.test(accession)    // todo: check array accessions against arrays
                 || Boolean.parseBoolean(                    // tests document for access
                     saxon.evaluateXPathSingle(              //
                             getDocument()                   //
                             , "exists(/experiments/experiment[accession = '" + accession + "' and user/@id = '" + userId + "'])"
                     )
                 )
             ) {
                 return true;
             }
         }
         return false;
     }
 
     public String getSpecies() throws Exception
     {
         return this.species.getObject().get();
     }
 
     public String getArrays() throws Exception
     {
         return this.arrays.getObject().get();
     }
 
     public String getAssaysByMolecule( String key )
     {
         return this.assaysByMolecule.get(key);
     }
 
     public String getAssaysByInstrument( String key )
     {
         return this.assaysByInstrument.get(key);
     }
 
     public void update( String xmlString, UpdateSourceInformation sourceInformation ) throws Exception
     {
         boolean success = false;
         try {
             DocumentInfo updateDoc = this.saxon.transform(
                     xmlString
                     , sourceInformation.getSource().getStylesheetName()
                     , null
             );
             if (null != updateDoc) {
                 new DocumentUpdater(this, updateDoc).update();
                 success = true;
             }
         } finally {
             sourceInformation.setOutcome(success);
             events.addEvent(sourceInformation);
         }
     }
 
     public void reloadExperimentsInAtlas( String sourceLocation ) throws Exception
     {
         URL source = new URL(sourceLocation);
         String result = this.saxon.transformToString(source, "preprocess-atlas-experiments-txt.xsl", null);
         if (null != result) {
             String[] exps = result.split("\n");
             if (exps.length > 0) {
                 this.experimentsInAtlas.setObject(new PersistableStringList(Arrays.asList(exps)));
                 updateAccelerators();
                 this.logger.info("Stored GXA info, [{}] experiments listed", exps.length);
             } else {
                 this.logger.warn("Atlas returned [0] experiments listed, will NOT update our info");
             }
         }
     }
 
     private void updateIndex()
     {
         try {
             this.search.getController().index(INDEX_ID, this.getDocument());
             this.autocompletion.rebuild();
         } catch (Exception x) {
             this.logger.error("Caught an exception:", x);
         }
     }
 
     private void updateAccelerators()
     {
         this.logger.debug("Updating accelerators for experiments");
 
         ExtFunctions.clearAccelerator("is-in-atlas");
         ExtFunctions.clearAccelerator("visible-experiments");
         try {
             for (String accession : this.experimentsInAtlas.getObject()) {
                 ExtFunctions.addAcceleratorValue("is-in-atlas", accession, "1");
             }
 
             XPath xp = new XPathEvaluator(getDocument().getConfiguration());
             XPathExpression xpe = xp.compile("/experiments/experiment[source/@visible = 'true']");
             List documentNodes = (List) xpe.evaluate(getDocument(), XPathConstants.NODESET);
 
             XPathExpression accessionXpe = xp.compile("accession");
             for (Object node : documentNodes) {
 
                 try {
                     // get all the expressions taken care of
                     String accession = accessionXpe.evaluate(node);
                     ExtFunctions.addAcceleratorValue("visible-experiments", accession, node);
                 } catch (XPathExpressionException x) {
                     this.logger.error("Caught an exception:", x);
                 }
             }
 
             this.logger.debug("Accelerators updated");
         } catch (Exception x) {
             this.logger.error("Caught an exception:", x);
         }
     }
 
     private void buildSpeciesArrays() throws Exception
     {
         // todo: move this to a separate component (autocompletion?)
         String speciesString = saxon.transformToString(this.getDocument(), "build-species-list-html.xsl", null);
         this.species.setObject(new PersistableString(speciesString));
 
         String arraysString = saxon.transformToString(this.getDocument(), "build-arrays-list-html.xsl", null);
         this.arrays.setObject(new PersistableString(arraysString));
     }
 }
