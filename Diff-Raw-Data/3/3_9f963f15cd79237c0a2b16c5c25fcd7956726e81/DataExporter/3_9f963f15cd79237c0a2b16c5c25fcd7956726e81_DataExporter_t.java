 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.dialogix.export;
 
 import org.dialogix.beans.InstrumentVersionView;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.*;
 import org.dialogix.entities.*;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import org.dialogix.beans.InstrumentSessionResultBean;
 import org.dialogix.session.DialogixEntitiesFacadeLocal;
 
 /**
  *
  * @author Coevtmw
  */
 public class DataExporter implements java.io.Serializable {
 
     private Logger logger = Logger.getLogger("org.dialogix.export.DataExporter");
     private DialogixEntitiesFacadeLocal dialogixEntitiesFacade = null;
     private InstrumentVersion instrumentVersion = null;
     private InstrumentHash instrumentHash = null;
     private LanguageList languageList = null;
     private Integer numLanguages = null;
     private ArrayList<String> languages = null;
     private boolean isLoggedIn = false;
     private Person person = null;
     private List<Menu> menus = null;
     private String menuSelection = null;
     private InstrumentSession instrumentSession = null;
     private Long pageUsageId = null; 
     private Study study = null; // in case needed for context
     private String currentAnswerListDenormString = null;
     private Integer numAnswerChoices = null;
     private String languageCode = "en";
     private String instrumentTitle = "unknown";
     
     // Fields needed from input form
     private Boolean sas_script=true;
     private Boolean spss_script=true;
     private String spss_unasked="44444";
     private String sas_unasked=".";
     private String spss_na="99999";
     private String sas_na=".";
     private String spss_refused="55555";
     private String sas_refused=".";
     private String spss_unknown="33333";
     private String sas_unknown=".";
     private String spss_huh="22222";
     private String sas_huh=".";
     private String spss_invalid="11111";
     private String sas_invalid=".";
     private String sort_order="sort_order_asked";
     private String exclude_regex="";
     private Boolean value_labels=true;
     private Boolean variable_labels=true;
     private Boolean frequency_distributions=true;
     private Boolean extract_data=true;
     private Boolean show_pi_view=true;
     private Boolean show_irb_view=true;
 
     private static final int UNASKED = 1;
     private static final int NA = 2;
     private static final int REFUSED = 3;
     private static final int INVALID = 4;
     private static final int UNKNOWN = 5;
     private static final int HUH = 6;
     private String[] spssNullFlavors = new String[7];
     private String[] sasNullFlavors = new String[7];
 
     private StringBuffer spssImportFile = new StringBuffer("");
     private String spss_missing_value_labels="";
     private String spss_missing_values_list="";;
     private ArrayList<String> varNames = new ArrayList<String>();   // list of variables which pass filter criteria  - do this as first pass  before searching data
     private ArrayList<Long> varNameIds = new ArrayList<Long>(); // list of VarNameIds for sub-select query
     private HashMap<String,String> sasVarNameFormat = new HashMap<String,String>();    
     private HashMap<String,String> spssVarNameFormat = new HashMap<String,String>();
     private List<InstrumentSessionResultBean> instrumentSessionResultBeans;
     private List<ItemUsage> itemUsages; // for reviewing details of a session
     private String transposedInstrumentSesionResults="";
     private String transposedInstrumentSesionResultsTSV="";
     private String selectedParameters="";
     private StringBuffer sasImportFile = new StringBuffer("");
     
     public DataExporter() {
         lookupDialogixEntitiesFacadeLocal();
     }
     
     /**
      * Picking an instrument version, process all directives
      * @param instrumentVersionId
      */
     public void setInstrumentVersionId(String instrumentVersionId) {
         try {
             lookupDialogixEntitiesFacadeLocal();
             Long id = Long.parseLong(instrumentVersionId);
             instrumentVersion = dialogixEntitiesFacade.getInstrumentVersion(id);
             if (instrumentVersion == null) {
                 throw new Exception("Unable to find Instrument #" + instrumentVersionId);
             }
             instrumentTitle = instrumentVersion.getInstrumentId().getInstrumentName() + " (" + instrumentVersion.getVersionString() + ")[" + instrumentVersion.getInstrumentVersionId() + "]";            
             instrumentHash = instrumentVersion.getInstrumentHashId();
             languageList = instrumentHash.getLanguageListId();
             numLanguages = instrumentHash.getNumLanguages();
             languages = new ArrayList<String>();
             String[] _languages = languageList.getLanguageList().split("\\|");
             for (int i=0;i<_languages.length;++i) {
                 String language = _languages[i];
                 if (language.length() > 2) {
                     language = language.substring(0,2);
                 }
                 languages.add(language);
             }
 //            init();
         } catch (Exception e) {
             logger.log(Level.SEVERE, "Unexpected Error ", e);            
         }
     }
     
     public String getInstrumentVersionId() {
         if (instrumentVersion == null) {
             return "";
         }
         return instrumentVersion.getInstrumentVersionId().toString();
     }
 
     /**
      * Constructor
      * @param instrumentTitle
      * @param major_version
      * @param minor_version
      */
     /*
     public DataExporter(String instrumentTitle,
                          String major_version,
                          String minor_version) {
         try {
             lookupDialogixEntitiesFacadeLocal();
 
             //	handle error if versions not found
             if (major_version == null || major_version.trim().length() == 0) {
                 major_version = "0";
             }
             if (minor_version == null || minor_version.trim().length() == 0) {
                 minor_version = "0";
             }
 
             instrumentVersion = dialogixEntitiesFacade.getInstrumentVersion(instrumentTitle, major_version, minor_version);
             if (instrumentVersion == null) {
                 throw new Exception("Unable to find Instrument " + instrumentTitle + "(" + major_version + "." + minor_version + ")");
             }
             instrumentTitle = instrumentVersion.getInstrumentId().getInstrumentName() + " (" + instrumentVersion.getVersionString() + ")[" + instrumentVersion.getInstrumentVersionId() + "]";            
 //            init();
         } catch (Exception e) {
             logger.log(Level.SEVERE, "Unexpected Error", e);
         }
     }
      */
 
     /**
      * Filter out variables with no data (isMessage() == true), or which match the excluded regex match pattern.  Sort them in either asAsked, or varName order.
      */
     public void filterVarNames() {
         varNames = new ArrayList<String>();
         varNameIds = new ArrayList<Long>();
         ArrayList<InstrumentContent> instrumentContentCollection = new ArrayList(instrumentVersion.getInstrumentContentCollection());
         Collections.sort(instrumentContentCollection, new InstrumentContentsComparator());
         Iterator<InstrumentContent> instrumentContentIterator = instrumentContentCollection.iterator();
         
         while (instrumentContentIterator.hasNext()) {
             InstrumentContent instrumentContent = instrumentContentIterator.next();
             
             if (instrumentContent.getIsMessage() == 1) {
                 continue;
             }
             VarName varName = instrumentContent.getVarNameId();
             String varNameString = varName.getVarName();
             
             if (exclude_regex.trim().length() > 0) {
                 if (varNameString.matches(exclude_regex)) {
                     continue;
                 }
             } 
             varNames.add(varNameString);
             varNameIds.add(varName.getVarNameId());
         }
         if (sort_order.equals("sort_varname")) {
             Collections.sort(varNames);        
         }
     }
 
     /**
      * Process selected directives
      */
     public void init() {
         if (instrumentVersion == null) {
             return;
         }        
         configure();
         showSelectedParameters();
         filterVarNames();
         generateImportFiles();
         if (extract_data == true) {
             findInstrumentSessionResults();            
             transposeInstrumentSessionResultsToTable();
             transposeInstrumentSessionResultsToTSV();       
         }
     }
     
     /**
      * Get the entity manager
      */
     private void lookupDialogixEntitiesFacadeLocal() {
        if (dialogixEntitiesFacade != null) {
            return; // since already loaded
        }        
         try {
             Context c = new InitialContext();
             dialogixEntitiesFacade =
                 (DialogixEntitiesFacadeLocal) c.lookup("java:comp/env/DialogixEntitiesFacade_ejbref");
         } catch (Exception e) {
             logger.log(Level.SEVERE, "", e);
         }
     }
     
     public void setLanguageCode(String languageCode) {
         this.languageCode = languageCode;
     }    
     
     private void showSelectedParameters() {
         StringBuffer sb = new StringBuffer();
         sb.append("/*\n");
         sb.append("Import file for ").append(instrumentTitle).append("\n");
         sb.append("Options:\n");
         sb.append("  Missing Value Mappings (Spss):\n");
         sb.append("    Unasked=").append(spss_unasked).append("\n");
         sb.append("    N/A=").append(spss_na).append("\n");
         sb.append("    Refused=").append(spss_refused).append("\n");
         sb.append("    Unknown=").append(spss_unknown).append("\n");
         sb.append("    Not Understood=").append(spss_huh).append("\n");
         sb.append("    Invalid=").append(spss_invalid).append("\n");
         sb.append("  Missing Value Mappings (Sas):\n");
         sb.append("    Unasked=").append(sas_unasked).append("\n");
         sb.append("    N/A=").append(sas_na).append("\n");
         sb.append("    Refused=").append(sas_refused).append("\n");
         sb.append("    Unknown=").append(sas_unknown).append("\n");
         sb.append("    Not Understood=").append(sas_huh).append("\n");
         sb.append("    Invalid=").append(sas_invalid).append("\n");        
         sb.append("  Output Options:\n");
         sb.append("    Sort Variables by=").append(sort_order).append("\n");
         sb.append("    Exclude Variables matching=").append(exclude_regex).append("\n");
         sb.append("    Value Labels=").append(value_labels).append("\n");
         sb.append("    Variable Labels=").append(variable_labels).append("\n");
         sb.append("    Frequency Distribution=").append(frequency_distributions).append("\n");
         sb.append("    Language Code=").append(languageCode).append("\n");
         sb.append("    Generate Data=").append(extract_data).append("\n");
         sb.append("*/\n\n");     
         selectedParameters = sb.toString();
     }
 
     /**
      * Generate Spss and Sas import file components.
      */
     private void generateImportFiles() {
         StringBuffer spss_import = new StringBuffer();
         StringBuffer spss_labels = new StringBuffer();
         StringBuffer sas_import = new StringBuffer();
         
         // This is the content Spss needs
         spss_import.append("GET DATA /TYPE = TXT\n");
         spss_import.append("/FILE = '").append(instrumentTitle).append(".tsv'\n");
         spss_import.append("/DELCASE = LINE\n");
         spss_import.append("/DELIMITERS = \"\\t\"\n");
         spss_import.append("/ARRANGEMENT = DELIMITED\n");
         spss_import.append("/FIRSTCASE = 2\n");  // FIXME - does Perl output variable names in first row?
         spss_import.append("/IMPORTCASE = ALL\n");
         spss_import.append("/VARIABLES =\n");
         
         // This is what Sas needs
         sas_import.append("data WORK.SUMMARY;\n");
         sas_import.append("%let _EFIERR_ = 0; /* set the ERROR detection macro variable */\n");
         sas_import.append("infile '").append(instrumentTitle).append(".tsv'\n");
 	sas_import.append("delimiter='09'x MISSOVER DSD lrecl=32767 firstobs=2;\n");
         
         /* List variables in desired sort order with this syntax for Spss:
          * VarName Spssformat
          */
         /* This is the syntax for Sas:
          * (1) Declare all first like this:
          * 	informat d_country $25.;  format d_country $25.;
          * (2) Input them like this:
          * 	Title $
          */
         ArrayList<InstrumentContent> instrumentContentCollection = new ArrayList(instrumentVersion.getInstrumentContentCollection());
         Collections.sort(instrumentContentCollection, new InstrumentContentsComparator());
         Iterator<InstrumentContent> instrumentContentIterator = instrumentContentCollection.iterator();
         
         while (instrumentContentIterator.hasNext()) {
             InstrumentContent instrumentContent = instrumentContentIterator.next();
             
             if (instrumentContent.getIsMessage() == 1) {
                 continue;   
             }
             
             String varName = instrumentContent.getVarNameId().getVarName();
             
             if (exclude_regex.trim().length() > 0) {
                 if (varName.matches(exclude_regex)) {
                     continue;
                 }
             }
             
             Item item = instrumentContent.getItemId();
             
             String question = "";
             Iterator<QuestionLocalized> questionLocalizedIterator = item.getQuestionId().getQuestionLocalizedCollection().iterator();
             while (questionLocalizedIterator.hasNext()) {
                 QuestionLocalized questionLocalized = questionLocalizedIterator.next();
                 if (questionLocalized.getLanguageCode().equals(languageCode)) {
                     question = questionLocalized.getQuestionString();
                     question = question.replaceAll("\"", "'");
                 }
             }
             
             // Store mapping of variable name to Spss format statement in HashMap so can sort it
             String sasFormat = " informat " + varName + " " + instrumentContent.getSasInformat() + "; format " + varName + " " + instrumentContent.getSasFormat() + ";\n";
             if (sort_order.equals("sort_varname")) {
                 spssVarNameFormat.put(varName,instrumentContent.getSpssFormat());
                 sasVarNameFormat.put(varName,sasFormat);
             }
             else {
                 // they are already sorted by order asked, so do it here
                 spss_import.append("  ").append(varName).append(" ").append(instrumentContent.getSpssFormat()).append("\n");
                 sas_import.append(sasFormat);
             }
             
             /* Set Variable Levels
                 VARIABLE LABELS ALC219
                     "[Alc219] In the past 12 months, have you often been under the effects of alcohol or suffering its after effects while at work or school or while taking acare of children?".
              */
             if (variable_labels == true) {
                 spss_labels.append("VARIABLE LABELS ").append(varName).append("\n");
                 spss_labels.append("  \"[").append(varName).append("] ").append(question).append("\".\n");
             }
 
             // Iterate over value set, if has one 
             /*
                 VALUE LABELS ALC219
                     99999  "*NA*"
                     44444  "*UNASKED*"
                     1 "[1] Yes"
                     2 "[2] No"
                     .
              */
             if (value_labels == true) {
                 AnswerList answerList = item.getAnswerListId();
                 if (answerList != null) {
                     spss_labels.append("VALUE LABELS ").append(varName).append("\n");
                     spss_labels.append(spss_missing_value_labels);
                     Iterator<AnswerListContent> answerListContentIterator = answerList.getAnswerListContentCollection().iterator();
                     while (answerListContentIterator.hasNext()) {
                         AnswerListContent answerListContent = answerListContentIterator.next();
                         String value = answerListContent.getAnswerCode();
                         Answer answer = answerListContent.getAnswerId();
                         String msg = "";
                         Iterator<AnswerLocalized> answerLocalizeds = answer.getAnswerLocalizedCollection().iterator();
                         while (answerLocalizeds.hasNext()) {
                             AnswerLocalized answerLocalized = answerLocalizeds.next();
                             if (answerLocalized.getLanguageCode().equals(languageCode)) {
                                 msg = answerLocalized.getAnswerString();
                             }
                         }
                         msg = "[" + value + "] " + msg;
                         value = value.replaceAll("'", "\"");
                         msg = msg.replaceAll("\"", "'");
 
                         spss_labels.append("  ").append(value).append(" \"").append(msg).append("\"\n");
                     }
                     spss_labels.append(".\n");
                 }
             }
             /* Set Spss Level Type:
                VARIABLE LEVEL ALC219 (NOMINAL).
              */
             spss_labels.append("VARIABLE LEVEL ").append(varName).append(" (").append(instrumentContent.getSpssLevel()).append(").\n");
             
             /* Set Spss Format type:
                 FORMATS ALC219 (F8.0).
              */
             spss_labels.append("FORMATS ").append(varName).append(" (").append(instrumentContent.getSpssFormat()).append(").\n");
             /* Set Spss Missing Values - FIXME - need to know desired mapping of missing values to internal codes; and whether numeric or string *
                 MISSING VALUES ALC219 (99999,44444).
              */
             if (spss_missing_values_list.trim().length() > 0) {
                 spss_labels.append("MISSING VALUES ").append(varName).append(" ").append(spss_missing_values_list).append(".\n");
             }
             spss_labels.append("\n");
         }
         // Now sort the list of variables
         if (sort_order.equals("sort_varname")) {
             for (int i=0;i<varNames.size();++i) {
                 String varName = varNames.get(i);
                 spss_import.append(" ").append(varName).append(" ").append(spssVarNameFormat.get(varName)).append("\n");
                 sas_import.append(sasVarNameFormat.get(varName));
             }
         }
         
         spss_import.append(".\n\n");
         sas_import.append("\n\n");
         
         if (sas_script == true) {
             sas_import.append(" INPUT\n");
             for (int i=0;i<varNames.size();++i) {
                 String varName = varNames.get(i);
                 sas_import.append(varName);
                 String format = sasVarNameFormat.get(varName);
                 if (format != null && format.contains("$")) {
                     sas_import.append(" $");
                 }
                 sas_import.append("\n");
             }
             sas_import.append(";\n");
             sas_import.append("if _ERROR_ then call symput('_EFIERR_',1);  /* set ERROR detection macro variable */\n");
             sas_import.append("run;\n");
         }
         
         /* FIXME: Should there be  syntax to let users  customize the naming scheme? */
         
         /* Generate Frequencies */
         StringBuffer spss_freq = new StringBuffer("");
         if (frequency_distributions == true) {
             spss_freq.append("\nFREQUENCIES VARIABLES=\n");
             for (int i=0;i<varNames.size();++i) {
                 spss_freq.append(varNames.get(i)).append("\n");
             }
             spss_freq.append("/BARCHART PERCENT.\n");
         }
         
         spssImportFile = new StringBuffer("/* Spss Import File */\n");
         spssImportFile.append(selectedParameters);
         spssImportFile.append(spss_import);
         spssImportFile.append(spss_labels);
         spssImportFile.append(spss_freq);
         
         sasImportFile = new StringBuffer("/* Sas Import File */\n");
         sasImportFile.append(selectedParameters);
         sasImportFile.append(sas_import);
     }
     
     /**
      * Map input parameters to locally needed values
      */
     public void configure() {
         /* Clear buffers */
         spssVarNameFormat = new HashMap<String,String>();
         sasVarNameFormat = new HashMap<String,String>();
         /* Set value labels for Spss */
         StringBuffer sb = new StringBuffer("");
         if (spss_unasked.trim().length() > 0) {
                     sb.append("  ").append(spss_unasked).append(" \"*UNASKED*\"\n");
         }
         if (spss_na.trim().length() > 0) {
                     sb.append("  ").append(spss_na).append(" \"*NA*\"\n");
         }
         if (spss_refused.trim().length() > 0) {
                     sb.append("  ").append(spss_refused).append(" \"*REFUSED*\"\n");
         }
         if (spss_unknown.trim().length() > 0) {
                     sb.append("  ").append(spss_unknown).append(" \"*UNKNOWN*\"\n");
         }
         if (spss_huh.trim().length() > 0) {
                     sb.append("  ").append(spss_huh).append(" \"*HUH*\"\n");
         }
         if (spss_invalid.trim().length() > 0) {
                     sb.append("  ").append(spss_invalid).append(" \"*INVALID*\"\n");
         }
         spss_missing_value_labels = sb.toString();
         
         /* Set missing values list for Spss */
         ArrayList<String> missingValues = new ArrayList<String>();
         if (spss_unasked.trim().length() > 0) {
             missingValues.add(spss_unasked);
         }
         if (spss_na.trim().length() > 0) {
             missingValues.add(spss_na);
         }
         if (spss_refused.trim().length() > 0) {
             missingValues.add(spss_refused);
         }
         if (spss_unknown.trim().length() > 0) {
             missingValues.add(spss_unknown);
         }
         if (spss_huh.trim().length() > 0) {
             missingValues.add(spss_huh);
         }
         if (spss_invalid.trim().length() > 0) {
             missingValues.add(spss_invalid);
         }
         
         sb = new StringBuffer("");
         for (int i=0;i<missingValues.size();++i) {
             if (i == 0) {
                 sb.append("(");
             }
             if (i > 0) {
                 sb.append(", ");
             }
             sb.append(missingValues.get(i));
         }
         sb.append(")");
         
         spss_missing_values_list = sb.toString();
         
         spssNullFlavors[HUH] = this.getSpss_huh();
         spssNullFlavors[INVALID] = this.getSpss_invalid();
         spssNullFlavors[NA] = this.getSpss_na();
         spssNullFlavors[REFUSED] = this.getSpss_refused();
         spssNullFlavors[UNASKED] = this.getSpss_unasked();
         spssNullFlavors[UNKNOWN] = this.getSpss_unknown();
         
         sasNullFlavors[HUH] = this.getSas_huh();
         sasNullFlavors[INVALID] = this.getSas_invalid();
         sasNullFlavors[NA] = this.getSas_na();
         sasNullFlavors[REFUSED] = this.getSas_refused();
         sasNullFlavors[UNASKED] = this.getSas_unasked();
         sasNullFlavors[UNKNOWN] = this.getSas_unknown();        
     }
 
     public String getSpssImportFile() {
         if (!spss_script) {
             return "";
         }        
         return spssImportFile.toString();
     }
     
     public String getSasImportFile() {
         if (!sas_script) {
             return "";
         }
         return sasImportFile.toString();
     }
     
     /**
      * Get list of instruments, showing title, version, num completed sessions
      * @return
      */
     public List<InstrumentVersionView> getInstrumentVersions() {
         return dialogixEntitiesFacade.getAuthorizedInstrumentVersions(person);
     }
     
     /**
      * Get raw results in form amenable to printing as table
      * @return
      */
     public List<InstrumentSessionResultBean> getRawResults()  {
         return instrumentSessionResultBeans;
     }
     
     public List<InstrumentContent> getInstrumentContents() {
         ArrayList<InstrumentContent> instrumentContentCollection = new ArrayList(instrumentVersion.getInstrumentContentCollection());
         Collections.sort(instrumentContentCollection, new InstrumentContentsComparator());
         return instrumentContentCollection;
     }
     
     /**
      * Get status of each session for a  particular version.
      * @return
      */
     public List<InstrumentSession> getInstrumentSessions() {
         return dialogixEntitiesFacade.getInstrumentSessions(instrumentVersion);
     }
     
     public Collection<InstrumentSession> getMyInstrumentSessions() {
         return dialogixEntitiesFacade.getMyInstrumentSessions(person);
     }
         
     /**
      * Extract data for this version
      */
     public void findInstrumentSessionResults() {
         try {
             if (instrumentVersion == null) {
                 return;
             }
             String inVarNameIds = null;
             StringBuffer sb = new StringBuffer("(");
             for (int i=0;i<varNameIds.size();++i) {
                 if (i > 0) {
                     sb.append(",");
                 }
                 sb.append(varNameIds.get(i));
             }
             sb.append(")");
             inVarNameIds = sb.toString();
             instrumentSessionResultBeans = dialogixEntitiesFacade.getFinalInstrumentSessionResults(instrumentVersion.getInstrumentVersionId(), inVarNameIds, (sort_order.equals("sort_varname")));
         } catch (Exception e) {
             logger.log(Level.SEVERE,e.getMessage(), e);
         }
     }
     
     /**
      * Convert from the vertical data  (raw results) to horizontal
      */
     public void transposeInstrumentSessionResultsToTable() {
         StringBuffer sb = new StringBuffer();
         
         sb.append("<table border='1'>\n<tr>");
         sb.append("<th>sessionId</th>");
         for (int i=0;i<varNames.size();++i) {
             sb.append("<th>").append(varNames.get(i)).append("</th>");
         }
         sb.append("</tr>\n");
         
         Iterator<InstrumentSessionResultBean> isrbs = instrumentSessionResultBeans.iterator();
         int counter = 0;
         while (isrbs.hasNext()) {
             if (counter++ == 0) {
                 sb.append("<tr>");
             }
             InstrumentSessionResultBean isrb = isrbs.next();
             if (counter == 1) {
                 sb.append("<td>");
                 sb.append(isrb.getInstrumentSessionId());
                 sb.append("</td>");
             }
             sb.append("<td>");
             if (isrb.getNullFlavorId() == null) {
                 sb.append(spssNullFlavors[UNASKED]);
             }
             else if (isrb.getNullFlavorId() > 0) {
                 sb.append(spssNullFlavors[isrb.getNullFlavorId()]);
             }
             else {
                 String answerCode = isrb.getAnswerCode();
                 if (answerCode == null) {
                     sb.append(spssNullFlavors[INVALID]);    // FIXME - is this correct behavior?
                 }
                 else {
                     sb.append(answerCode);
                 }
             }
             sb.append("</td>");
             if (counter == varNames.size()) {
                 counter = 0;
                 sb.append("</tr>\n");
             }
         }
         sb.append("</table>\n");
         transposedInstrumentSesionResults = sb.toString();
     }
     
     private void transposeInstrumentSessionResultsToTSV() {
         StringBuffer sb = new StringBuffer();
         
         sb.append("sessionId");
         for (int i=0;i<varNames.size();++i) {
             sb.append("\t");
             sb.append(varNames.get(i));
         }
         sb.append("\n");
         
         Iterator<InstrumentSessionResultBean> isrbs = instrumentSessionResultBeans.iterator();
         int counter = 0;
         while (isrbs.hasNext()) {
             counter++;
             InstrumentSessionResultBean isrb = isrbs.next();
             if (counter == 1) {
                 sb.append(isrb.getInstrumentSessionId());
                 sb.append("\t");
             }                
             if (isrb.getNullFlavorId() == null) {
                 sb.append(spssNullFlavors[UNASKED]);
             }
             else if (isrb.getNullFlavorId() > 0) {
                 sb.append(spssNullFlavors[isrb.getNullFlavorId()]);
             }
             else {
                 String answerCode = isrb.getAnswerCode();
                 if (answerCode == null) {
                     sb.append(spssNullFlavors[INVALID]);    // FIXME - is this correct behavior?
                 }
                 else {
                     sb.append(answerCode);
                 }
             }
             sb.append("\t");
             if (counter == varNames.size()) {
                 counter = 0;
                 sb.append("\n");
             }
         }
         transposedInstrumentSesionResultsTSV = sb.toString();
     }
     
     public void setExclude_regex(String exclude_regex) {
         this.exclude_regex = exclude_regex;
     }
 
     public void setFrequency_distributions(String frequency_distributions) {
         this.frequency_distributions = ("1".equals(frequency_distributions));
     }
 
     public void setSas_huh(String sas_huh) {
         this.sas_huh = sas_huh;
     }
 
     public void setSas_invalid(String sas_invalid) {
         this.sas_invalid = sas_invalid;
     }
 
     public void setSas_na(String sas_na) {
         this.sas_na = sas_na;
     }
 
     public void setSas_refused(String sas_refused) {
         this.sas_refused = sas_refused;
     }
 
     public void setSas_script(String sas_script) {
         this.sas_script = ("1".equals(sas_script));
     }
 
     public void setSas_unasked(String sas_unasked) {
         this.sas_unasked = sas_unasked;
     }
 
     public void setSas_unknown(String sas_unknown) {
         this.sas_unknown = sas_unknown;
     }
 
     public void setSort_order(String sort_order) {
         this.sort_order = sort_order;
     }
 
     public void setSpss_huh(String spss_huh) {
         this.spss_huh = spss_huh;
     }
 
     public void setSpss_invalid(String spss_invalid) {
         this.spss_invalid = spss_invalid;
     }
 
     public void setSpss_na(String spss_na) {
         this.spss_na = spss_na;
     }
 
     public void setSpss_refused(String spss_refused) {
         this.spss_refused = spss_refused;
     }
 
     public void setSpss_script(String spss_script) {
         this.spss_script = ("1".equals(spss_script));
     }
 
     public void setSpss_unasked(String spss_unasked) {
         this.spss_unasked = spss_unasked;
     }
 
     public void setSpss_unknown(String spss_unknown) {
         this.spss_unknown = spss_unknown;
     }
 
     public void setValue_labels(String value_labels) {
         this.value_labels = ("1".equals(value_labels));
     }
 
     public void setVariable_labels(String variable_labels) {
         this.variable_labels = ("1".equals(variable_labels));
     }
     
     public String getExclude_regex() {
         return exclude_regex;
     }
 
     public String getFrequency_distributions() {
         return (frequency_distributions == true) ? "checked": "";
     }
 
     public String getInstrumentTitle() {
         return instrumentTitle;
     }
 
     public String getLanguageCode() {
         return languageCode;
     }
 
     public String getSas_huh() {
         return sas_huh;
     }
 
     public String getSas_invalid() {
         return sas_invalid;
     }
 
     public String getSas_na() {
         return sas_na;
     }
 
     public String getSas_refused() {
         return sas_refused;
     }
 
     public String getSas_script() {
         return (sas_script == true) ? "checked" : "";
     }
 
     public String getSas_unasked() {
         return sas_unasked;
     }
 
     public String getSas_unknown() {
         return sas_unknown;
     }
 
     public String getSort_order() {
         return sort_order;
     }
 
     public String getSpss_huh() {
         return spss_huh;
     }
 
     public String getSpss_invalid() {
         return spss_invalid;
     }
 
     public String getSpss_missing_value_labels() {
         return spss_missing_value_labels;
     }
 
     public String getSpss_missing_values_list() {
         return spss_missing_values_list;
     }
 
     public String getSpss_na() {
         return spss_na;
     }
 
     public String getSpss_refused() {
         return spss_refused;
     }
 
     public String getSpss_script() {
         return (spss_script == true) ? "checked" : "";        
     }
 
     public String getSpss_unasked() {
         return spss_unasked;
     }
 
     public String getSpss_unknown() {
         return spss_unknown;
     }
 
     public String getValue_labels() {
         return (value_labels == true) ? "checked" : "";
     }
 
     public String getVariable_labels() {
         return (variable_labels == true) ? "checked" : "";
     }    
     
 
     public String getTransposedInstrumentSesionResults() {
         return transposedInstrumentSesionResults;
     }
 
     public String getTransposedInstrumentSesionResultsTSV() {
         return transposedInstrumentSesionResultsTSV;
     }
     
     public String getExtract_data() {
         return (extract_data == true) ? "checked" : "";
     }
 
     public void setExtract_data(String extract_data) {
         this.extract_data = ("1".equals(extract_data));
     }    
     
     public String getShow_irb_view() {
         return (show_irb_view == true) ? "checked" : "";
     }
 
     public void setShow_irb_view(String show_irb_view) {
         this.show_irb_view = "1".equals(show_irb_view);
     }
 
     public String getShow_pi_view() {
         return (show_pi_view == true) ? "checked" : "";
     }
 
     public void setShow_pi_view(String show_pi_view) {
         this.show_pi_view = "1".equals(show_pi_view);
     }
     
     public void setInstrumentSession(String instrumentSessionId) {
         try {
             Long id = Long.parseLong(instrumentSessionId);
             instrumentSession = dialogixEntitiesFacade.getInstrumentSession(id);
             itemUsages = dialogixEntitiesFacade.getItemUsages(id);
         } catch (Exception e) {
             logger.log(Level.SEVERE, "Unexpected Error ", e);
             instrumentSession = null;
             itemUsages = null;
         }
     }
 
     public List<ItemUsage> getItemUsages() {
         return itemUsages;
     }
     
     public Collection<PageUsage> getPageUsages() {
         if (instrumentSession == null) {
             return null;
         }
         return instrumentSession.getPageUsageCollection();
     }
     
     public InstrumentHash getInstrumentHash() {
         return instrumentHash;
     }
 
     public InstrumentVersion getInstrumentVersion() {
         return instrumentVersion;
     }
 
     public LanguageList getLanguageList() {
         return languageList;
     }
     
     public Integer getNumLanguages() {
         return numLanguages;
     }
 
     public ArrayList<String> getLanguages() {
         return languages;
     }
     
     public void doLogin(String userName, String pwd) {
         person = dialogixEntitiesFacade.getPerson(userName, pwd);
         isLoggedIn = true;  // means that tried to login
         if (person != null) {
             menus = dialogixEntitiesFacade.getMenus(person);
         }
     }
     
     public boolean isLogin() {
         return isLoggedIn;
     }
     
     public boolean isAuthenticated() {
         return (person != null);
     }
     
     public Person getPerson() {
         // Does this need to be refreshed with each access?
 //        dialogixEntitiesFacade.refresh(person);   // FIXME - em.find() might be better
         return person;
     }
     
     public void doLogout() {
         isLoggedIn = false;
         person = null;
         menus = null;
     }
     
     public List<Menu> getMenus() {
         if (menus == null) {
             menus = dialogixEntitiesFacade.getMenus(null);
         }
         return menus;
     }
     
     public void setMenuSelection(String menuString) {
         if (menuString == null || menuString.trim().length() == 0) {
             menuSelection = "Contact";
         }
         else {
             menuSelection = menuString;
         }
     }
     
     public boolean isAuthenticatedForMenu() {
         boolean result = false;
         if ("Login".equals(menuSelection)) {
             result = true;    // allow this for anyone?
         }
         if (isAuthenticated() && "Logout".equals(menuSelection)) {
             result = true;
         }
         Iterator<Menu> menuIterator = menus.iterator();
         while (menuIterator.hasNext()) {
             if (menuIterator.next().getMenuName().equals(menuSelection)) {
                 result = true;
             }
         }
 //        logger.severe("isAuthenticated(" + menuSelection + ") = " + result);
         return result;
     }
     
     public void setPageUsageId(String id) {
         try {
             pageUsageId = Long.parseLong(id);
         } catch (Exception e) {
             logger.log(Level.SEVERE, "Unexpected Error ", e);
             pageUsageId = null;
         }        
     }
     
     public List<PageUsageEvent> getPageUsageEvents() {
         return dialogixEntitiesFacade.getPageUsageEvents(pageUsageId);
     }
     
     public List<Study> getStudies() {
         try {
             return dialogixEntitiesFacade.getStudies();
         } catch (Exception e) {
             logger.log(Level.SEVERE,"Unexpected Error ", e);
             return null;
         }
     }
     
     public Study getStudy() {
         return study;
     }
 
     public void setStudy(String studyId) {
         if (studyId == null || studyId.trim().length() == 0) {
             return;
         }
         Long id = (long) 1;
         try {
             id = Long.parseLong(studyId);
         } catch (Exception e) {
             logger.log(Level.SEVERE,"setStudy",e.getMessage());
         }            
         study = dialogixEntitiesFacade.findStudyById(id);
     }
     
 
     public String getCurrentAnswerListDenormString() {
         return currentAnswerListDenormString;
     }
 
     public void setCurrentAnswerListDenormString(String currentAnswerListDenormString) {
         this.currentAnswerListDenormString = currentAnswerListDenormString;
         if (currentAnswerListDenormString == null) {
             this.numAnswerChoices = 1;  // to avoid divide-by-zero
         }
         else {
             this.numAnswerChoices = currentAnswerListDenormString.split("\\|").length / 2;
         }
     }
     
     public Integer getNumAnswerChoices() {
         return numAnswerChoices;
     }
     
     public List<InstrumentLoadError> getInstrumentLoadErrors() {
         return dialogixEntitiesFacade.getInstrumentLoadErrors(instrumentVersion);
     }
     
     public String getDatFileView() {
         StringBuffer sb = new StringBuffer();
         
         sb.append("RESERVED\t__TRICEPS_FILE_TYPE__\tDATA\n");
         sb.append("RESERVED\t__TRICEPS_VERSION_MAJOR__\t3.0\n");
         sb.append("RESERVED\t__TRICEPS_VERSION_MINOR__\t0\n");
         sb.append("RESERVED\t__START_TIME__\t").append(instrumentSession.getStartTime().getTime()).append("\n");
         
         Iterator<String> keys = InstrumentSession.getReservedWordMap().keySet().iterator();
         while (keys.hasNext()) {
             String key = keys.next();
             String value = instrumentSession.getReserved(key);
             sb.append("RESERVED\t").append(key).append("\t").append(value).append("\n");
         }
         
         Iterator<DataElement> dataElements = instrumentSession.getDataElementCollection().iterator();
         while (dataElements.hasNext()) {
             DataElement de = dataElements.next();
             sb.append("\t").append(de.getVarNameId().getVarName()).append("\t0\t0\t*UNASKED*\n");
         }
         
         Iterator<ItemUsage> itemUsages = getItemUsages().iterator();
         Integer displayNum = -1;
         while (itemUsages.hasNext()) {
             ItemUsage iu = itemUsages.next();
             if (iu.getDisplayNum() != displayNum) {
                 displayNum = iu.getDisplayNum();
                 sb.append("RESERVED\t__DISPLAY_COUNT__\t").append(displayNum).append("\n");
                 sb.append("RESERVED\t__STARTING_STEP__\t").append(iu.getDataElementId().getDataElementSequence()).append("\n");
             }
             sb.append("\t").append(iu.getDataElementId().getVarNameId().getVarName()).append("\t").append(iu.getLanguageCode()).append("\t")
                 .append(iu.getWhenAsMs()).append("\t").append(iu.getQuestionAsAsked()).append("\t");
             NullFlavor nf = iu.getNullFlavorId();
             if (nf == null || nf.getNullFlavorId() == 0) {
                 sb.append(iu.getAnswerCode());
             }
             else {
                 sb.append(nf.getDisplayName());
             }
             sb.append("\n");
         }
         
         return sb.toString();
     }
     
     public String getDatEvtFileView() {
         StringBuffer sb = new StringBuffer();
         
         sb.append("**Dialogix Interviewing System version 3.0.0 started ").append(instrumentSession.getStartTime()).append("\n");
         sb.append("*** ").append(instrumentSession.getIpAddress()).append(" ").append(instrumentSession.getBrowser()).append("\n");
         
         Iterator<PageUsageEvent> pues = this.dialogixEntitiesFacade.getAllPageUsageEvents(instrumentSession.getInstrumentSessionId()).iterator();
         
         Integer displayNum = -1;
         while (pues.hasNext()) {
             PageUsageEvent pue = pues.next();
             PageUsage pu = pue.getPageUsageId();
             if (pu.getDisplayNum() != displayNum) {
                 if (displayNum > 0) {
                     sb.append(displayNum).append("\t\treceived_response\t\t").append(pu.getTotalDuration()).append("\n");
                 }
                 displayNum = pu.getDisplayNum();
                 sb.append(displayNum).append("\t\tsent_request\t").append(pu.getServerSendTime().getTime()).append("\t").append(pu.getServerDuration()).append("\n");
                 sb.append(displayNum).append("\tnull\tnull\tload\t\t").append("\t").append(pu.getLoadDuration()).append("\n");
             }
             sb.append(displayNum).append("\t").append(pue.getVarName())
                 .append("\t").append(pue.getEventType())
                 .append("\t").append(pue.getGuiActionType())
                 .append("\t").append(pue.getTimeStamp().getTime())
                 .append("\t").append(pue.getDuration())
                 .append("\t").append(pue.getValue1())
                 .append("\t").append(pue.getValue2())
                 .append("\n");
         }
         return sb.toString();
     }    
 }
