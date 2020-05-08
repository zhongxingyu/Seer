 package org.fao.geonet.services.metadata.validation.agiv;
 
 import jeeves.resources.dbms.Dbms;
 import jeeves.server.context.ServiceContext;
 import jeeves.utils.Xml;
 import org.fao.geonet.constants.Geonet;
 import org.fao.geonet.services.metadata.validation.AbstractValidationHook;
 import org.fao.geonet.services.metadata.validation.ValidationHookException;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.Namespace;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Records validation result inside the metadata itself.
  *
  * NOTE: the valTypeAndStatus map contains the validation summary, same as it is saved to the DB. Its structure is:
  *
  * key : string. Name of validation type. E.g. "xsd", "schematron-rules-iso", "schematron-rules-inspire"
  * value: array of 3 integers where
  *     value[0] : can be 1 or 0, meaning validation succeeded (1) or failed (0).
  *     value[1] : only applicable to schematron validation types. Total number of asserts in this validation.
  *     value[2] : only applicable to schematron validation types. Number of failed asserts in this validation.
  *
  * @author heikki doeleman
  */
 public class AGIVValidationHook extends AbstractValidationHook {
     private String metadataId;
     private String timestamp;
     private Map<String, Integer[]> valTypeAndStatus;
     private boolean workspace;
 
     private static final String XSD_KEY = "xsd";
     private static final String ISO_SCHEMATRON_KEY = "schematron-rules-iso";
     private static final String INSPIRE_SCHEMATRON_KEY = "schematron-rules-inspire";
     private static final String AGIV_SCHEMATRON_KEY = "schematron-rules-GDI-Vlaanderen";
 
     //
     // XSLT file names
     //
 
     private static final String EMPTY_MDSTANDARDNAME_AND_MDSTANDARDVERSION = "agiv-empty-mdstandardname-mdstandardversion.xsl";
     private static final String DATASET_MDSTANDARDNAME_AND_MDSTANDARDVERSION = "agiv-dataset-mdstandardname-mdstandardversion.xsl";
     private static final String SERVICE_MDSTANDARDNAME_AND_MDSTANDARDVERSION = "agiv-service-mdstandardname-mdstandardversion.xsl";
 
     private static final String ADD_INSPIRE_KEYWORD = "agiv-add-inspire-keyword.xsl";
     private static final String REMOVE_INSPIRE_KEYWORD = "agiv-remove-inspire-keyword.xsl";
 
     private static final String ADD_AGIV_KEYWORD = "agiv-add-agiv-keyword.xsl";
     private static final String REMOVE_AGIV_KEYWORD = "agiv-remove-agiv-keyword.xsl";
     /**
      * Invoked when validation has finished. The arguments should be :
      * - the metadata id (a String)
      * - the validation results summary (a Map<String, Integer[]>)
      * - the timestamp of the validation (a String)
      * - whether this is workspace metadata or not (a boolean)
      *
      * @param args zero or more implementation-dependent arguments
      * @throws ValidationHookException hmm
      */
     @Override
     public void onValidate(Object... args) throws ValidationHookException {
         try {
 
             for(Object o : args) {
                 System.out.println("AGIVValidationHook onValidate arg: " + o.toString());
             }
             if(args.length != 4) {
                 throw new IllegalArgumentException("AGIVValidationHook onValidate expects #4 arguments but received # " + args.length);
             }
             metadataId = (String)args[0];
             valTypeAndStatus = (Map<String, Integer[]>) args[1];
             timestamp = (String) args[2];
             workspace = (Boolean) args[3];
 
             for(Iterator<String> i = valTypeAndStatus.keySet().iterator(); i.hasNext();) {
                 String key = i.next();
                 Integer[] values = valTypeAndStatus.get(key);
                 for(int j = 0;j < values.length; j++) {
                     System.out.println("AGIVValidationHook key: " + key + " value # " + j + ": " + values[j]);
                 }
             }
 
             Element metadata = getMetadata(workspace);
 
             Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
             String schema = dm.getMetadataSchema(dbms, metadataId);
 
 //            metadata = logISO19115Compliance(metadata, schema, valTypeAndStatus.get(XSD_KEY), valTypeAndStatus.get(ISO_SCHEMATRON_KEY));
             metadata = logINSPIRECompliance(metadata, schema, valTypeAndStatus.get(INSPIRE_SCHEMATRON_KEY));
             metadata = logAGIVCompliance(metadata, schema, valTypeAndStatus.get(AGIV_SCHEMATRON_KEY));
 
             //
             // save & index
             //
 
             if(workspace) {
                 dm.updateMetadataWorkspace(context, dbms, metadataId, metadata, false, false, true, context.getLanguage(), null, false);
             }
             else {
                 dm.updateMetadata(context, dbms, metadataId, metadata, false, false, true, context.getLanguage(), null, false);
             }
             dm.indexInThreadPoolIfPossible(dbms, metadataId, workspace);
 
         }
         catch(Exception x) {
             throw new ValidationHookException(x.getMessage(), x);
         }
     }
 
     /**
      *
      * Overwrites metadataStandardName and metadataStandardVersion.
      *
      * AGIV mss120716v01_D2200 9_Stepped_MetadataValidation_c110148_M12ss006v031.doc § 4.2 :
      *
      * Conformity against the ISO 19115-ISO19139 and ISO19119-19139 standards. This conformance is verified by
      * validating against the schema and schematrons and will happen for respectively the datasets and dataset series
      * and the services.
      *
      * This conformance is logged inside the existing ISO19115/9 elements metadataStandardName and
      * metadataStandardVersion:
      *
      *  * metadataStandardName will be set to ISO19115 and metadataStandardVersion to 2003/Cor.1:2006 for metadata of datasets and dataset series.
      *  * metadataStandardName = ISO19119 and metadataStandardVersion = 2005/Amd 1:2008 for services metadata.
      *
      * When metadata is not conforming, both elements will be emptied if they contained the above listed values. If
      * metadata is conforming, the value of the metadataStandardname and metadataStandardVersion will be overwritten
      * with the above.
      *
      * @param metadata
      * @param schema
      * @param xsdResults
      * @param schematronResults
      * @throws ValidationHookException
      */
     private Element logISO19115Compliance(Element metadata, String schema, Integer[] xsdResults, Integer[] schematronResults) throws ValidationHookException {
         if(xsdResults == null) {
             System.out.println("WARNING logISO19115Compliance received null xsdResults results - skipping it.");
             return metadata;
         }
         if(schematronResults == null) {
             System.out.println("WARNING logISO19115Compliance received null schematron results - skipping it.");
             return metadata;
         }
         try {
             boolean xsdValid = xsdResults[0] == 1;
             boolean schematronValid = schematronResults[0] == 1;
 
             // valid
             if(xsdValid && schematronValid) {
                 String hierarchyLevel = getHierarchyLevel(metadata);
                 if(hierarchyLevel.equals("service")) {
                     transformMd(metadata, schema, SERVICE_MDSTANDARDNAME_AND_MDSTANDARDVERSION);
                 }
                 else if(hierarchyLevel.equals("dataset") || hierarchyLevel.equals("series")) {
                     transformMd(metadata, schema, DATASET_MDSTANDARDNAME_AND_MDSTANDARDVERSION);
                 }
             }
             // not valid
             else {
                 // empty metadataStandardname and metadataStandardVersion
                 transformMd(metadata, schema, EMPTY_MDSTANDARDNAME_AND_MDSTANDARDVERSION);
             }
/*
             System.out.println("***** result of logISO19115Compliance:\n" + Xml.getString(metadata));
             System.out.println("***** end result of logISO19115Compliance..\n");
*/
             return metadata;
         }
         catch(Exception x) {
             throw new ValidationHookException(x.getMessage(), x);
         }
     }
 
     /**
      * Adds or removes keyword to indicate INSPIRE compliance.
      *
      * AGIV mss120716v01_D2200 9_Stepped_MetadataValidation_c110148_M12ss006v031.doc § 4.2 :
      *
      * Conformity against the INSPIRE Metadata Implementing Rule for datasets, dataset series and services. This is
      * verified by checking against the INSPIRE schematrons and will happen for dataset (series) and services metadata.
      *
      * If the metadata is conforming to INSPIRE the keyword 'Conform INSPIRE – Metadata Implementing Rules' is added to
      * the list of available keywords with as thesaurus 'GDI-Vlaanderen Trefwoorden' with thesaurus date '2012-07-10'.
      *
      * Obviously it will first be checked if this keyword is already present to avoid duplicate keywords. In case the
      * metadata is not conforming, the list of keywords will be checked and the above mentioned keyword removed - if it
      * was present.
      *
      * @param metadata
      * @param schema
      * @param schematronResults
      * @throws ValidationHookException
      */
     private Element logINSPIRECompliance(Element metadata, String schema, Integer[] schematronResults) throws ValidationHookException {
         if(schematronResults == null) {
             System.out.println("WARNING logINSPIRECompliance received null schematron results - skipping it.");
             return metadata;
         }
         try {
             boolean inspireValid = schematronResults[0] == 1;
             System.out.println("INSPIRE validation hook: INSPIRE compliant? " + inspireValid);
             if(inspireValid) {
                 metadata = transformMd(metadata, schema, ADD_INSPIRE_KEYWORD);
             }
             else {
                 metadata = transformMd(metadata, schema, REMOVE_INSPIRE_KEYWORD);
             }
/*
             System.out.println("***** result of logINSPIRECompliance:\n" + Xml.getString(metadata));
             System.out.println("***** end result of logINSPIRECompliance..\n");
*/
             return metadata;
         }
         catch(Exception x) {
             throw new ValidationHookException(x.getMessage(), x);
         }
     }
 
     /**
      *
      * Adds or removes keyword to indicate AGIV compliance.
      *
      * AGIV mss120716v01_D2200 9_Stepped_MetadataValidation_c110148_M12ss006v031.doc § 4.2 :
      *
      * Conformity against the GDI-Vlaanderen Best Practices. This is verified by checking against the GDI-Vlaanderen
      * schematrons.
      *
      * If the metadata is conforming, the keyword 'Conform GDI-Vlaanderen Best Practices' is added to the list of
      * available keywords with as thesaurus 'GDI-Vlaanderen Trefwoorden' and as thesaurus date '2012-07-10'.
      *
      * In case the metadata is not conforming, the list of keywords will be checked and the above mentioned keyword is
      * removed, if it was present. Obviously it will first be checked if this keyword is already present to avoid
      * duplicate keywords.
      *
      * @param metadata
      * @param schema
      * @param schematronResults
      * @throws ValidationHookException
      */
     private Element logAGIVCompliance(Element metadata, String schema, Integer[] schematronResults) throws ValidationHookException {
         if(schematronResults == null) {
             System.out.println("WARNING logAGIVCompliance received null schematron results - skipping it.");
             return metadata;
         }
         try {
             boolean agivValid = schematronResults[0] == 1;
             System.out.println("INSPIRE validation hook: INSPIRE compliant? " + agivValid);
             if(agivValid) {
                 metadata = transformMd(metadata, schema, ADD_AGIV_KEYWORD);
             }
             else {
                 metadata = transformMd(metadata, schema, REMOVE_AGIV_KEYWORD);
             }
/*
             System.out.println("***** result of logAGIVCompliance:\n" + Xml.getString(metadata));
             System.out.println("***** end result of logAGIVCompliance..\n");
*/
             return metadata;
         }
         catch(Exception x) {
             throw new ValidationHookException(x.getMessage(), x);
         }
     }
 
     @Override
     public void init(ServiceContext context, Dbms dbms) {
         super.init(context, dbms);
     }
 
     /**
      * Retrieves the metadata to operate on.
      *
      * @return
      * @throws Exception
      */
     private Element getMetadata(boolean workspace) throws ValidationHookException {
         try {
             Element md;
             if(!workspace) {
                 md = dm.getMetadataNoInfo(context, metadataId);
             }
             else {
                 md = dm.getMetadataFromWorkspaceNoInfo(context, metadataId);
             }
 
             if (md == null) {
                 return null;
             }
             md.detach();
             return md;
         }
         catch(Exception x) {
             throw new ValidationHookException(x.getMessage(), x);
         }
     }
 
     /**
      * @param md
      * @param schema
      * @param styleSheet
      * @throws Exception
      */
     private Element transformMd(Element md, String schema, String styleSheet) throws Exception {
         System.out.println("AVH transforming with stylesheet " + styleSheet);
         //--- do an XSL  transformation
         styleSheet = dm.getSchemaDir(schema) + styleSheet;
         return Xml.transform(md, styleSheet);
     }
 
     /**
      * Returns value of /gmd:Metadata/gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue.
      *
      * @param metadata
      * @return
      * @throws JDOMException
      */
     private String getHierarchyLevel(Element metadata) throws JDOMException {
         List<Namespace> nsList = new ArrayList<Namespace>(2);
         nsList.add(Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd"));
         nsList.add(Namespace.getNamespace("gco", "http://www.isotc211.org/2005/gco"));
         String level = Xml.selectString(metadata, "//gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue", nsList);
         System.out.println("getHierarchyLevel: " + level);
         return level;
     }
 }
