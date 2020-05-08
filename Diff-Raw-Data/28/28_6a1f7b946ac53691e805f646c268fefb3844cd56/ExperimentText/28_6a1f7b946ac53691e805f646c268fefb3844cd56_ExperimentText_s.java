 package uk.ac.ebi.arrayexpress.utils.search;
 
 import net.sf.saxon.s9api.XdmNode;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import uk.ac.ebi.arrayexpress.app.Application;
 import uk.ac.ebi.arrayexpress.components.SaxonEngine;
 
 /**
  * A transitional class holding text that will be matched when searching for experiments.
  * To be replaced by Lucene engine in 1.2.
  */
 
 public class ExperimentText
 {
     // logging machinery
     private final Log log = LogFactory.getLog(getClass());
 
     // all text, concatenated
     public String text;
 
     public String accession;
 
     // all accessions, concatenated
     public String accessions;
 
     // all species, concatenated
     public String species;
 
     // all array info, concatenated
     public String array;
 
     // experiment type info, concatenated
     public String experimentType;
 
     // user ids, concatenated
     public String users;
 
     public ExperimentText populateFromExperiment( XdmNode exp )
     {
         SaxonEngine saxon = (SaxonEngine) Application.getAppComponent("SaxonEngine");
         if (null != exp) {
             text = saxon.concatAllText(exp);
            accession = saxon.evaluateXPathSingle(exp, "accession");
             accessions = " ".concat(accession).concat(" ").concat(saxon.concatAllText(saxon.evaluateXPath(exp, "secondaryaccession"))).toLowerCase();
             species = saxon.concatAllText(saxon.evaluateXPath(exp, "species")).toLowerCase();
             array = saxon.concatAllText(saxon.evaluateXPath(exp, "arraydesign")).toLowerCase();
             experimentType = saxon.concatAllText(saxon.evaluateXPath(exp, "experimenttype")).toLowerCase();
             users = " ".concat(saxon.concatAllText(saxon.evaluateXPath(exp, "user")));
         }
 
         return this;
     }
 }
