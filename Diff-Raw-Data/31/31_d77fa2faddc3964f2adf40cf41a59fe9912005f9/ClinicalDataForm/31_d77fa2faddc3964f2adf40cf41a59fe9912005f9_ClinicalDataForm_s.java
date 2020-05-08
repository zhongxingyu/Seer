 
 package gov.nih.nci.nautilus.ui.struts.form;
 
 import gov.nih.nci.nautilus.constants.NautilusConstants;
 import gov.nih.nci.nautilus.criteria.AgeCriteria;
 import gov.nih.nci.nautilus.criteria.ChemoAgentCriteria;
 import gov.nih.nci.nautilus.criteria.DiseaseOrGradeCriteria;
 import gov.nih.nci.nautilus.criteria.GenderCriteria;
 import gov.nih.nci.nautilus.criteria.OccurrenceCriteria;
 import gov.nih.nci.nautilus.criteria.RadiationTherapyCriteria;
 import gov.nih.nci.nautilus.criteria.SampleCriteria;
 import gov.nih.nci.nautilus.criteria.SurgeryTypeCriteria;
 import gov.nih.nci.nautilus.criteria.SurvivalCriteria;
 import gov.nih.nci.nautilus.de.AgeAtDiagnosisDE;
 import gov.nih.nci.nautilus.de.ChemoAgentDE;
 import gov.nih.nci.nautilus.de.DiseaseNameDE;
 import gov.nih.nci.nautilus.de.GenderDE;
 import gov.nih.nci.nautilus.de.GradeDE;
 import gov.nih.nci.nautilus.de.OccurrenceDE;
 import gov.nih.nci.nautilus.de.RadiationTherapyDE;
 import gov.nih.nci.nautilus.de.SampleIDDE;
 import gov.nih.nci.nautilus.de.SurgeryTypeDE;
 import gov.nih.nci.nautilus.de.SurvivalDE;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.upload.FormFile;
 import org.apache.struts.util.LabelValueBean;
 
 public class ClinicalDataForm extends BaseForm {
 
     private static Logger logger = Logger.getLogger(ClinicalDataForm.class);
 
     /** queryName property */
     private String queryName;
 
     private String resultView;
 
     /** tumorType property */
     private String tumorType;
     
     /** sampleList property */
 	private String sampleList;
 
     /** tumorGrade property */
     private String tumorGrade;
 
     /** occurence first presentation property */
     private String firstPresentation;
 
     /** occurence recurrence property */
     private String recurrence;
 
     /** occurence recurrence type property */
     private String recurrenceType;
 
     /** radiation group property */
     private String radiation;
 
     /** radiation type property */
     private String radiationType;
 
     /** chemo agent group property */
     private String chemo;
 
     /** chemo agent type property */
     private String chemoType;
 
     /** surgery group property */
     private String surgery;
 
     /** surgery type property */
     private String surgeryType;
 
     /** survival lower property */
     private String survivalLower;
 
     /** survival upper property */
     private String survivalUpper;
 
     /** age lower property */
     private String ageLower;
 
     /** age upper property */
     private String ageUpper;
 
     /** gender property */
     private String genderType;
     
     /** sampleFile property */
 	private FormFile sampleFile;
 	
 	/** sampleGroup property */
 	private String sampleGroup;
 
     // Collections used for Lookup values.
     //private ArrayList diseaseType;// moved to the upper class: BaseForm.java
     private ArrayList recurrenceTypeColl = new ArrayList();
 
     private ArrayList radiationTypeColl = new ArrayList();
 
     private ArrayList chemoAgentTypeColl = new ArrayList();
 
     private ArrayList surgeryTypeColl = new ArrayList();
 
     private ArrayList survivalLowerColl = new ArrayList();
 
     private ArrayList survivalUpperColl = new ArrayList();
 
     private ArrayList ageLowerColl = new ArrayList();
 
     private ArrayList ageUpperColl = new ArrayList();
 
     private ArrayList genderTypeColl = new ArrayList();
 
     // criteria objects
     private DiseaseOrGradeCriteria diseaseOrGradeCriteria;
 
     private OccurrenceCriteria occurrenceCriteria;
 
     private RadiationTherapyCriteria radiationTherapyCriteria;
 
     private ChemoAgentCriteria chemoAgentCriteria;
 
     private SurgeryTypeCriteria surgeryTypeCriteria;
 
     private SurvivalCriteria survivalCriteria;
 
     private AgeCriteria ageCriteria;
 
     private GenderCriteria genderCriteria;
     
     private SampleCriteria sampleCriteria;
 
     // Hashmap to store Domain elements
     private HashMap diseaseDomainMap = new HashMap();
 
     private HashMap gradeDomainMap = new HashMap();// this one may not be used
                                                    // for this release.
 
     private HashMap occurrenceDomainMap = new HashMap();
 
     private HashMap radiationDomainMap = new HashMap();
 
     private HashMap chemoAgentDomainMap = new HashMap();
 
     private HashMap surgeryDomainMap = new HashMap();
 
     private HashMap survivalDomainMap = new HashMap();
 
     private HashMap ageDomainMap = new HashMap();
 
     private HashMap genderDomainMap = new HashMap();
     
     private HashMap sampleDomainMap = new HashMap();
 
     private HttpServletRequest thisRequest;
 
     // --------------------------------------------------------- Methods
     public ClinicalDataForm() {
         super();
         // Create Lookups for Clinical Data screens
         setClinicalDataLookup();
 
     }
 
    
 
     private void createDiseaseCriteriaObject() {
         if (diseaseDomainMap != null) {
             Set keySet = diseaseDomainMap.keySet();
             Iterator iter = keySet.iterator();
             while (iter.hasNext()) {
                 try {
                     String key = (String) iter.next();
                     String className = (String) diseaseDomainMap.get(key);
                     Constructor[] diseaseConstructors = Class
                             .forName(className).getConstructors();
                     String[] initargs = { key };
                     DiseaseNameDE diseaseDE = (DiseaseNameDE) diseaseConstructors[0]
                             .newInstance(initargs);
                     diseaseOrGradeCriteria.setDisease(diseaseDE);
 
                 } // end of try
                 catch (Exception ex) {
                   logger.error(ex);
               } catch (LinkageError le) {
                   logger.error(le);
               }
 
             }// end of while
 
         }// end of if
     }
 
     private void createOccurrenceCriteriaObject() {
 
         // Loop thru the HashMap, extract the Domain elements and create
         // respective Criteria Objects
         Set keys = occurrenceDomainMap.keySet();
         logger.debug("occurrenceDomainMap.size() is :"
                 + occurrenceDomainMap.size());
         Iterator i = keys.iterator();
         while (i.hasNext()) {
             Object key = i.next();
             try {
                 String strOccurenceClass = (String) occurrenceDomainMap
                         .get(key);
                 Constructor[] occurrenceConstructors = Class.forName(
                         strOccurenceClass).getConstructors();
                 Object[] parameterObjects = { key };
 
                 OccurrenceDE occurrenceDE = (OccurrenceDE) occurrenceConstructors[0]
                         .newInstance(parameterObjects);
 
                 occurrenceCriteria.setOccurrence(occurrenceDE);
      
 
             } catch (Exception ex) {
                 logger.error(ex);
 
             } catch (LinkageError le) {
                 logger.error(le);
             }
 
         }
     }
 
     private void createRadiationTherapyCriteriaObject() {
 
         Set keys = radiationDomainMap.keySet();
         Iterator i = keys.iterator();
         while (i.hasNext()) {
             Object key = i.next();
             try {
                 String strRadiationClass = (String) radiationDomainMap.get(key);
                 Constructor[] radiationConstructors = Class.forName(
                         strRadiationClass).getConstructors();
                 Object[] parameterObjects = { (String) key };
 
                 RadiationTherapyDE radiationTherapyDE = (RadiationTherapyDE) radiationConstructors[0]
                         .newInstance(parameterObjects);
                 radiationTherapyCriteria
                         .setRadiationTherapyDE(radiationTherapyDE);
 
             } catch (Exception ex) {
                 logger.error(ex);
             } catch (LinkageError le) {
                 logger.error(le);
             }
 
         }
     }
 
     private void createChemoAgentCriteriaObject() {
 
         Set keys = chemoAgentDomainMap.keySet();
         Iterator i = keys.iterator();
         while (i.hasNext()) {
             Object key = i.next();
             try {
                 String strChemoDomainClass = (String) chemoAgentDomainMap
                         .get(key);
                 Constructor[] chemoConstructors = Class.forName(
                         strChemoDomainClass).getConstructors();
                 Object[] parameterObjects = { (String) key };
 
                 ChemoAgentDE chemoAgentDE = (ChemoAgentDE) chemoConstructors[0]
                         .newInstance(parameterObjects);
                 chemoAgentCriteria.setChemoAgentDE(chemoAgentDE);
 
             } catch (Exception ex) {
                 logger.error(ex);
             } catch (LinkageError le) {
                 logger.error(le);
             }
 
         }
 
     }
 
     private void createSurgeryTypeCriteriaObject() {
 
         // Loop thru the surgeryDomainMap HashMap, extract the Domain elements
         // and create respective Criteria Objects
         Set keys = surgeryDomainMap.keySet();
         Iterator i = keys.iterator();
         while (i.hasNext()) {
             Object key = i.next();
 
             try {
                 String strSurgeryDomainClass = (String) surgeryDomainMap
                         .get(key);
                 Constructor[] surgeryConstructors = Class.forName(
                         strSurgeryDomainClass).getConstructors();
                 Object[] parameterObjects = { key };
 
                 SurgeryTypeDE surgeryTypeDE = (SurgeryTypeDE) surgeryConstructors[0]
                         .newInstance(parameterObjects);
                 surgeryTypeCriteria.setSurgeryTypeDE(surgeryTypeDE);
             
 
             } catch (Exception ex) {
                 logger.error(ex);
             } catch (LinkageError le) {
                 logger.error(le);
             }
 
         }
 
     }
 
     private void createSurvivalCriteriaObject() {
 
         // Loop thru the survivalDomainMap HashMap, extract the Domain elements
         // and create respective Criteria Objects
         Set keys = survivalDomainMap.keySet();
         Iterator i = keys.iterator();
         while (i.hasNext()) {
             Object key = i.next();
             logger.debug(key + "=>" + survivalDomainMap.get(key));
 
             try {
                 String strSurvivalDomainClass = (String) survivalDomainMap
                         .get(key);
                 Constructor[] survivalConstructors = Class.forName(
                         strSurvivalDomainClass).getConstructors();
 
                 if (strSurvivalDomainClass.endsWith("LowerSurvivalRange")) {
                     Object[] parameterObjects = { Integer.valueOf((String) key) };
                     SurvivalDE.LowerSurvivalRange lowerSurvivalRange = (SurvivalDE.LowerSurvivalRange) survivalConstructors[0]
                             .newInstance(parameterObjects);
                     survivalCriteria.setLowerSurvivalRange(lowerSurvivalRange);
                 } else if (strSurvivalDomainClass
                         .endsWith("UpperSurvivalRange")) {
                     Object[] parameterObjects = { Integer.valueOf((String) key) };
                     SurvivalDE.UpperSurvivalRange upperSurvivalRange = (SurvivalDE.UpperSurvivalRange) survivalConstructors[0]
                             .newInstance(parameterObjects);
                     survivalCriteria.setUpperSurvivalRange(upperSurvivalRange);
                 }
             } catch (Exception ex) {
 
                 //LogEntry logEntry = new LogEntry(Level.ERROR,"Error in
                 // createSurvivalCriteriaObject() method: "+ ex.getMessage());
                 //Logging.add(logEntry);
 
                 //logEntry = new LogEntry(Level.ERROR,ex.fillInStackTrace());
                 //Logging.add(logEntry);
 
             } catch (LinkageError le) {
                 //LogEntry logEntry = new LogEntry(Level.ERROR,"Linkage Error
                 // in createSurvivalCriteriaObject() method: "+
                 // le.getMessage());
                 //Logging.add(logEntry);
 
                 //logEntry = new LogEntry(Level.ERROR,le.fillInStackTrace());
                 //Logging.add(logEntry);
             }
 
         }
     }
 
     private void createAgeCriteriaObject() {
 
         // Loop thru the ageDomainMap HashMap, extract the Domain elements and
         // create respective Criteria Objects
         Set keys = ageDomainMap.keySet();
         Iterator i = keys.iterator();
         while (i.hasNext()) {
             Object key = i.next();
 
             //LogEntry logEntry = new LogEntry(Level.DEBUG,key + "=>" +
             // ageDomainMap.get(key));
             //Logging.add(logEntry);
 
             try {
                 String strAgeDomainClass = (String) ageDomainMap.get(key);
                 Constructor[] ageConstructors = Class
                         .forName(strAgeDomainClass).getConstructors();
 
                 if (strAgeDomainClass.endsWith("LowerAgeLimit")) {
                     Object[] parameterObjects = { Integer.valueOf((String) key) };
                     AgeAtDiagnosisDE.LowerAgeLimit lowerAgeLimit = (AgeAtDiagnosisDE.LowerAgeLimit) ageConstructors[0]
                             .newInstance(parameterObjects);
                     ageCriteria.setLowerAgeLimit(lowerAgeLimit);
                 } else if (strAgeDomainClass.endsWith("UpperAgeLimit")) {
                     Object[] parameterObjects = { Integer.valueOf((String) key) };
                     AgeAtDiagnosisDE.UpperAgeLimit upperAgeLimit = (AgeAtDiagnosisDE.UpperAgeLimit) ageConstructors[0]
                             .newInstance(parameterObjects);
                     ageCriteria.setUpperAgeLimit(upperAgeLimit);
                 }
             } catch (Exception ex) {
 
                 //logEntry = new LogEntry(Level.ERROR,"Error in
                 // createGeneCriteriaObject: "+ ex.getMessage());
                 //Logging.add(logEntry);
 
                 //logEntry = new LogEntry(Level.ERROR,ex.fillInStackTrace());
                 //Logging.add(logEntry);
 
             } catch (LinkageError le) {
 
                 //logEntry = new LogEntry(Level.ERROR,"Linkage Error in
                 // createAgeCriteriaObject() method: "+ le.getMessage());
                 //Logging.add(logEntry);
 
                 //logEntry = new LogEntry(Level.ERROR,le.fillInStackTrace());
                 //Logging.add(logEntry);
 
             }
 
         }
     }
 
     private void createGenderCriteriaObject() {
 
         // Loop thru the genderDomainMap HashMap, extract the Domain elements
         // and create respective Criteria Objects
         Set keys = genderDomainMap.keySet();
         Iterator i = keys.iterator();
         while (i.hasNext()) {
             Object key = i.next();
             try {
                 String strGenderDomainClass = (String) genderDomainMap.get(key);
                 Constructor[] genderConstructors = Class.forName(
                         strGenderDomainClass).getConstructors();
                 Object[] parameterObjects = { key };
 
                 GenderDE genderDE = (GenderDE) genderConstructors[0]
                         .newInstance(parameterObjects);
                 genderCriteria.setGenderDE(genderDE);
      
 
             } catch (Exception ex) {
                 logger.error(ex);
  
             } catch (LinkageError le) {
                 logger.error(le);
             }
 
         }
 
     }
     
     private void createSampleCriteriaObject() {
 
 		// Loop thru the HashMap, extract the Domain elements and create
 		// respective Criteria Objects
 		Set keys = sampleDomainMap.keySet();
 		Iterator i = keys.iterator();
 		while (i.hasNext()) {
 			Object key = i.next();
 			logger.debug(key + "=>" + sampleDomainMap.get(key));
 
 			try {
 				String strSampleDomainClass = (String) sampleDomainMap.get(key);
 				Constructor[] sampleConstructors = Class.forName(
 						strSampleDomainClass).getConstructors();
 				Object[] parameterObjects = { key };
 
 				SampleIDDE sampleIDDEObj = (SampleIDDE) sampleConstructors[0]
 						.newInstance(parameterObjects);
 				sampleCriteria.setSampleID(sampleIDDEObj);
 
 				logger.debug("Sample Domain Element Value==> "
 						+ sampleIDDEObj.getValueObject());
 			} catch (Exception ex) {
 			    logger.debug("Error in createSampleCriteriaObject  "
 						+ ex.getMessage());
 				ex.printStackTrace();
 			} catch (LinkageError le) {
 			    logger.error("Linkage Error in createSampleCriteriaObject "
 						+ le.getMessage());
 				le.printStackTrace();
 			}
 
 		}
 
 	}
 
     
     
 
     public void setClinicalDataLookup() {
 
         recurrenceTypeColl = new ArrayList();
         radiationTypeColl = new ArrayList();
         chemoAgentTypeColl = new ArrayList();
         surgeryTypeColl = new ArrayList();
         survivalLowerColl = new ArrayList();
         survivalUpperColl = new ArrayList();
         ageLowerColl = new ArrayList();
         ageUpperColl = new ArrayList();
         genderTypeColl = new ArrayList();
 
         recurrenceTypeColl.add(new LabelValueBean("Any", "any"));
         recurrenceTypeColl.add(new LabelValueBean("1", "1"));
         recurrenceTypeColl.add(new LabelValueBean("2", "2"));
         recurrenceTypeColl.add(new LabelValueBean("3", "3"));
 
         radiationTypeColl.add(new LabelValueBean("Any", "any"));
         radiationTypeColl.add(new LabelValueBean("Photon", "photon"));
 
         chemoAgentTypeColl.add(new LabelValueBean("Any", "any"));
 
         surgeryTypeColl.add(new LabelValueBean("Any", "any"));
         surgeryTypeColl.add(new LabelValueBean("Complete Resection(CR)",
                 "Complete Resection(CR)"));
         surgeryTypeColl.add(new LabelValueBean("Partial Resection(PR)",
                 "Partial Resection(PR)"));
         surgeryTypeColl.add(new LabelValueBean("Bioposy Only(BX)",
                 "Bioposy Only(BX)"));
 
         survivalLowerColl.add(new LabelValueBean("", ""));
         survivalLowerColl.add(new LabelValueBean("0", "0"));
         survivalLowerColl.add(new LabelValueBean("10", "10"));
         survivalLowerColl.add(new LabelValueBean("20", "20"));
         survivalLowerColl.add(new LabelValueBean("30", "30"));
         survivalLowerColl.add(new LabelValueBean("40", "40"));
         survivalLowerColl.add(new LabelValueBean("50", "50"));
         survivalLowerColl.add(new LabelValueBean("60", "60"));
         survivalLowerColl.add(new LabelValueBean("70", "70"));
         survivalLowerColl.add(new LabelValueBean("80", "80"));
         survivalLowerColl.add(new LabelValueBean("90", "90"));
 
         survivalUpperColl.add(new LabelValueBean("", ""));
         survivalUpperColl.add(new LabelValueBean("0", "0"));
         survivalUpperColl.add(new LabelValueBean("10", "10"));
         survivalUpperColl.add(new LabelValueBean("20", "20"));
         survivalUpperColl.add(new LabelValueBean("30", "30"));
         survivalUpperColl.add(new LabelValueBean("40", "40"));
         survivalUpperColl.add(new LabelValueBean("50", "50"));
         survivalUpperColl.add(new LabelValueBean("60", "60"));
         survivalUpperColl.add(new LabelValueBean("70", "70"));
         survivalUpperColl.add(new LabelValueBean("80", "80"));
         survivalUpperColl.add(new LabelValueBean("90", "90"));
         //	survivalUpperColl.add( new LabelValueBean( "90+", "90+" ) );
 
         ageLowerColl.add(new LabelValueBean("", ""));
         ageLowerColl.add(new LabelValueBean("0", "0"));
         ageLowerColl.add(new LabelValueBean("10", "10"));
         ageLowerColl.add(new LabelValueBean("20", "20"));
         ageLowerColl.add(new LabelValueBean("30", "30"));
         ageLowerColl.add(new LabelValueBean("40", "40"));
         ageLowerColl.add(new LabelValueBean("50", "50"));
         ageLowerColl.add(new LabelValueBean("60", "60"));
         ageLowerColl.add(new LabelValueBean("70", "70"));
         ageLowerColl.add(new LabelValueBean("80", "80"));
         ageLowerColl.add(new LabelValueBean("90", "90"));
 
         ageUpperColl.add(new LabelValueBean("", ""));
         ageUpperColl.add(new LabelValueBean("0", "0"));
         ageUpperColl.add(new LabelValueBean("10", "10"));
         ageUpperColl.add(new LabelValueBean("20", "20"));
         ageUpperColl.add(new LabelValueBean("30", "30"));
         ageUpperColl.add(new LabelValueBean("40", "40"));
         ageUpperColl.add(new LabelValueBean("50", "50"));
         ageUpperColl.add(new LabelValueBean("60", "60"));
         ageUpperColl.add(new LabelValueBean("70", "70"));
         ageUpperColl.add(new LabelValueBean("80", "80"));
         ageUpperColl.add(new LabelValueBean("90", "90"));
         //	ageUpperColl.add( new LabelValueBean( "90+", "90+" ) );
 
         genderTypeColl.add(new LabelValueBean("", ""));
         genderTypeColl.add(new LabelValueBean("Male", "M"));
         genderTypeColl.add(new LabelValueBean("Female", "F"));
         genderTypeColl.add(new LabelValueBean("Other", "O"));
 
     }
 
     /**
      * Method reset. Reset all properties to their default values.
      * 
      * @param ActionMapping
      *            mapping used to select this instance.
      * @param HttpServletRequest
      *            request The servlet request we are processing.
      */
 
     public void reset(ActionMapping mapping, HttpServletRequest request) {
 
         queryName = "";
         resultView = "";
         tumorType = "";
         tumorGrade = "";
         firstPresentation = "";
         recurrence = "";
         recurrenceType = "";
         radiation = "";
         radiationType = "";
         chemo = "";
         chemoType = "";
         surgery = "";
         surgeryType = "";
         survivalLower = "";
         survivalUpper = "";
         ageLower = "";
         ageUpper = "";
         genderType = "";
         sampleGroup = "";
 		sampleList = "";
 		sampleFile = null;
 
         diseaseOrGradeCriteria = new DiseaseOrGradeCriteria();
         occurrenceCriteria = new OccurrenceCriteria();
         radiationTherapyCriteria = new RadiationTherapyCriteria();
         chemoAgentCriteria = new ChemoAgentCriteria();
         surgeryTypeCriteria = new SurgeryTypeCriteria();
         survivalCriteria = new SurvivalCriteria();
         ageCriteria = new AgeCriteria();
         genderCriteria = new GenderCriteria();
         sampleCriteria = new SampleCriteria();
 
         diseaseDomainMap = new HashMap();
         gradeDomainMap = new HashMap();
         occurrenceDomainMap = new HashMap();
         radiationDomainMap = new HashMap();
         chemoAgentDomainMap = new HashMap();
         surgeryDomainMap = new HashMap();
         survivalDomainMap = new HashMap();
         ageDomainMap = new HashMap();
         genderDomainMap = new HashMap();
 
         thisRequest = request;
 
     }
 
     /**
      * Set the tumorType.
      * 
      * @param tumorType
      *            The tumorType to set
      */
     public void setTumorType(String tumorType) {
 
         this.tumorType = tumorType;
         if (this.tumorType.equalsIgnoreCase("ALL")) {
             ArrayList allDiseases = this.getDiseaseType();
             for (Iterator diseaseIter = allDiseases.iterator(); diseaseIter
                     .hasNext();) {
                 LabelValueBean thisLabelBean = (LabelValueBean) diseaseIter
                         .next();
                 String thisDiseaseType = thisLabelBean.getValue();
                 // stuff this in our DomainMap for later use !!
                 if (!thisDiseaseType.equalsIgnoreCase("ALL")) {
                     diseaseDomainMap.put(thisDiseaseType, DiseaseNameDE.class
                             .getName());
                 }
             }
         } else {
             diseaseDomainMap.put(this.tumorType, DiseaseNameDE.class.getName());
         }
     }
     
     /**
 	 * Returns the sampleList.
 	 * 
 	 * @return String
 	 */
 	public String getSampleList() {
 
 		return sampleList;
 	}
 	
 	/**
 	 * Set the sampleList.
 	 * 
 	 * @param sampleList
 	 *            The sampleList to set
 	 */
 	public void setSampleList(String sampleList) {
 		this.sampleList = sampleList;
 		if(thisRequest!=null){
 
 			String thisSampleGroup = this.thisRequest.getParameter("sampleGroup");
 	
 			if ((thisSampleGroup != null)
 					&& thisSampleGroup.equalsIgnoreCase("Specify")
 					&& (this.sampleList.length() > 0)) {
 	
 				String[] splitSampleValue = this.sampleList.split("\\x2C");
 				
 	
 				for (int i = 0; i < splitSampleValue.length; i++) {
 	                sampleDomainMap.put(splitSampleValue[i].trim(),
 					SampleIDDE.class.getName());	
 				}
 			 }
 
 		}
 	}
 
 	
 	
 
     /**
      * Returns the tumorGrade.
      * 
      * @return String
      */
     public String getTumorGrade() {
         return tumorGrade;
     }
 
     /**
      * Set the tumorGrade.
      * 
      * @param tumorType
      *            The tumorGrade to set
      */
     public void setTumorGrade(String tumorGrade) {
         this.tumorGrade = tumorGrade;
         gradeDomainMap.put(this.tumorGrade, GradeDE.class.getName());
     }
 
     /**
      * Returns the tumorType.
      * 
      * @return String
      */
     public String getTumorType() {
         return tumorType;
     }
     
     /**
 	 * Returns the sampleFile.
 	 * 
 	 * @return String
 	 */
 	public FormFile getSampleFile() {
 		return sampleFile;
 	}
 	
 	/**
 	 * Set the sampleFile.
 	 * 
 	 * @param sampleFile
 	 *            The sampleFile to set
 	 */
 	public void setSampleFile(FormFile sampleFile) {
 		this.sampleFile = sampleFile;
 		if(thisRequest!=null){
 			String thisSampleGroup = this.thisRequest.getParameter("sampleGroup");
 	//		retrieve the file name & size
 	 		String fileName= sampleFile.getFileName();
 	 		int fileSize = sampleFile.getFileSize();
 	
 	 		if ((thisSampleGroup != null) && thisSampleGroup.equalsIgnoreCase("Upload")
 					&& (this.sampleFile != null)
 					&& (this.sampleFile.getFileName().endsWith(".txt") || this.sampleFile.getFileName().endsWith(".TXT"))
 					&& (this.sampleFile.getContentType().equals("text/plain"))) {
 				try {
 					InputStream stream = sampleFile.getInputStream();				
 					String inputLine = null;
 					BufferedReader inFile = new BufferedReader( new InputStreamReader(stream));
 					
 					int count = 0;
 					while ((inputLine = inFile.readLine()) != null && count < NautilusConstants.MAX_FILEFORM_COUNT)  {
 						if(UIFormValidator.isAscii(inputLine)){ //make sure all data is ASCII
								count++;
 								sampleDomainMap.put(inputLine,SampleIDDE.class.getName());				 
 						}
 					}// end of while
 	
 					inFile.close();
 				} catch (IOException ex) {
 				    logger.error("Errors when uploading sample file:"
 							+ ex.getMessage());
 				}
 	
 			}
 		}
 	}
 
 
     /**
      * Set the firstPresentation.
      * 
      * @param firstPresentation
      *            The firstPresentation to set
      */
     public void setFirstPresentation(String firstPresentation) {
         if (firstPresentation != null) {
             if (firstPresentation.equalsIgnoreCase("on")) {
                 this.firstPresentation = "first presentation";
             }
             occurrenceDomainMap.put(this.firstPresentation, OccurrenceDE.class
                     .getName());
         }
     }
 
     /**
      * Returns the firstPresentation.
      * 
      * @return String
      */
     public String getFirstPresentation() {
         return firstPresentation;
     }
 
     /**
      * Set the recurrence.
      * 
      * @param recurrence
      *            The recurrence to set
      */
     public void setRecurrence(String recurrence) {
         this.recurrence = recurrence;
         if (thisRequest != null) {
             String recurrenceStr = (String) thisRequest.getParameter("recur");
             String recurrenceSpecify = (String) thisRequest
                     .getParameter("recurrence");
             if (recurrenceStr != null && recurrenceSpecify != null) {
                 occurrenceDomainMap.put(this.recurrence, OccurrenceDE.class
                         .getName());
             }
         }
     }
 
     /**
      * Returns the recurrence.
      * 
      * @return String
      */
     public String getRecurrence() {
         return recurrence;
     }
 
     /**
      * Set the recurrenceType.
      * 
      * @param recurrenceType
      *            The recurrenceType to set
      */
     public void setRecurrenceType(String recurrenceType) {
         this.recurrenceType = recurrenceType;
         occurrenceDomainMap.put(this.recurrenceType, OccurrenceDE.class
                 .getName());
     }
 
     /**
      * Returns the recurrenceType.
      * 
      * @return String
      */
     public String getRecurrenceType() {
         return recurrenceType;
     }
 
     /**
      * Set the radiation.
      * 
      * @param radiation
      *            The radiation to set
      */
     public void setRadiation(String radiation) {
         this.radiation = radiation;
 
     }
 
     /**
      * Returns the radiation.
      * 
      * @return String
      */
     public String getRadiation() {
         return radiation;
     }
 
     /**
      * Set the radiationType.
      * 
      * @param radiationType
      *            The radiationType to set
      */
     public void setRadiationType(String radiationType) {
         this.radiationType = radiationType;
         if (thisRequest != null) {
             // this is to check if radiation option is selected
             String thisRadiation = thisRequest.getParameter("radiation");
 
             // this is to check the type of radiation
             String thisRadiationType = thisRequest.getParameter("radiationType");
 
             if (thisRadiation != null && thisRadiationType != null
                     && !thisRadiationType.equals("")) {
                 radiationDomainMap.put(this.radiationType,
                         RadiationTherapyDE.class.getName());
             }
         }
     }
 
     /**
      * Returns the radiationType.
      * 
      * @return String
      */
     public String getRadiationType() {
         return radiationType;
     }
 
     /**
      * Set the chemo.
      * 
      * @param chemo
      *            The chemo to set
      */
     public void setChemo(String chemo) {
         this.chemo = chemo;
 
     }
 
     /**
      * Returns the chemo.
      * 
      * @return String
      */
     public String getChemo() {
         return chemo;
     }
 
     /**
      * Set the chemoType.
      * 
      * @param chemoType
      *            The chemoType to set
      */
     public void setChemoType(String chemoType) {
         this.chemoType = chemoType;
         if (thisRequest != null) {
             // this is to check if the chemo option is selected
             String thisChemo = thisRequest.getParameter("chemo");
 
             // this is to check the chemo type
             String thisChemoType = thisRequest.getParameter("chemoType");
             if (thisChemo != null && thisChemoType != null
                     && !thisChemoType.equals("")) {
                 chemoAgentDomainMap.put(this.chemoType, ChemoAgentDE.class
                         .getName());
             }
         }
     }
 
     /**
      * Returns the chemoType.
      * 
      * @return String
      */
     public String getChemoType() {
         return chemoType;
     }
 
     /**
      * Set the surgery.
      * 
      * @param surgery
      *            The surgery to set
      */
     public void setSurgery(String surgery) {
         this.surgery = surgery;
 
     }
 
     /**
      * Returns the surgery.
      * 
      * @return String
      */
     public String getSurgery() {
         return surgery;
     }
 
     /**
      * Set the surgeryType.
      * 
      * @param surgeryType
      *            The surgeryType to set
      */
     public void setSurgeryType(String surgeryType) {
         this.surgeryType = surgeryType;
         if (thisRequest != null) {
             String thisSurgery = thisRequest.getParameter("sugery");
             String thisSurgeryType = thisRequest.getParameter("surgeryType");
 
             if (thisSurgery != null && thisSurgeryType != null
                     && !thisSurgeryType.equals("")) {
                 surgeryDomainMap.put(this.surgeryType, SurgeryTypeDE.class
                         .getName());
             }
         }
     }
 
     /**
      * Returns the surgeryType.
      * 
      * @return String
      */
     public String getSurgeryType() {
         return surgeryType;
     }
 
     /**
      * Set the survivalLower.
      * 
      * @param survivalLower
      *            The survivalLower to set
      */
     public void setSurvivalLower(String survivalLower) {
         this.survivalLower = survivalLower;
         survivalDomainMap.put(this.survivalLower,
                 SurvivalDE.LowerSurvivalRange.class.getName());
     }
 
     /**
      * Returns the survivalLower.
      * 
      * @return String
      */
     public String getSurvivalLower() {
         return survivalLower;
     }
 
     /**
      * Set the survivalUpper.
      * 
      * @param survivalUpper
      *            The survivalUpper to set
      */
     public void setSurvivalUpper(String survivalUpper) {
         this.survivalUpper = survivalUpper;
         survivalDomainMap.put(this.survivalUpper,
                 SurvivalDE.UpperSurvivalRange.class.getName());
     }
 
     /**
      * Returns the survivalUpper.
      * 
      * @return String
      */
     public String getSurvivalUpper() {
         return survivalUpper;
     }
 
     /**
      * Set the ageLower.
      * 
      * @param ageLower
      *            The ageLower to set
      */
     public void setAgeLower(String ageLower) {
         this.ageLower = ageLower;
         ageDomainMap.put(this.ageLower, AgeAtDiagnosisDE.LowerAgeLimit.class
                 .getName());
     }
 
     /**
      * Returns the ageLower.
      * 
      * @return String
      */
     public String getAgeLower() {
         return ageLower;
     }
 
     /**
      * Set the ageUpper.
      * 
      * @param ageUpper
      *            The ageUpper to set
      */
     public void setAgeUpper(String ageUpper) {
         this.ageUpper = ageUpper;
         ageDomainMap.put(this.ageUpper, AgeAtDiagnosisDE.UpperAgeLimit.class
                 .getName());
     }
 
     /**
      * Returns the genderType.
      * 
      * @return String
      */
     public String getAgeUpper() {
         return ageUpper;
     }
 
     /**
      * Set the genderType.
      * 
      * @param genderType
      *            The genderType to set
      */
     public void setGenderType(String genderType) {
         this.genderType = genderType;
 
         if (this.genderType != null && !this.genderType.trim().equals("")) {
             genderDomainMap.put(this.genderType, GenderDE.class.getName());
         }
 
     }
 
     /**
      * Returns the genderType.
      * 
      * @return String
      */
     public String getGenderType() {
         return genderType;
     }
 
     /**
      * Set the queryName.
      * 
      * @param queryName
      *            The queryName to set
      */
     public void setQueryName(String queryName) {
         this.queryName = queryName;
     }
 
     /**
      * Returns the queryName.
      * 
      * @return String
      */
     public String getQueryName() {
         return queryName;
     }
 
     /**
      * Returns the resultView.
      * 
      * @return String
      */
     public String getResultView() {
         return resultView;
     }
 
     /**
      * Set the resultView.
      * 
      * @param resultView
      *            The resultView to set
      */
     public void setResultView(String resultView) {
         this.resultView = resultView;
     }
     
     /**
 	 * Returns the geneGroup.
 	 * 
 	 * @return String
 	 */
 	public String getSampleGroup() {
 		return sampleGroup;
 	}
 	
 	/**
 	 * Set the sampleGroup.
 	 * 
 	 * @param sampleGroup
 	 *            The sampleGroup to set
 	 */
 	public void setSampleGroup(String sampleGroup) {
 		this.sampleGroup = sampleGroup;
 	}
 	
 
 
     public DiseaseOrGradeCriteria getDiseaseOrGradeCriteria() {
         return this.diseaseOrGradeCriteria;
     }
     
     public SampleCriteria getSampleCriteria(){
 	    return this.sampleCriteria;
 	}
 
     public OccurrenceCriteria getOccurrenceCriteria() {
         return this.occurrenceCriteria;
     }
 
     public SurgeryTypeCriteria getSurgeryTypeCriteria() {
         return this.surgeryTypeCriteria;
     }
 
     public RadiationTherapyCriteria getRadiationTherapyCriteria() {
         return this.radiationTherapyCriteria;
     }
 
     public ChemoAgentCriteria getChemoAgentCriteria() {
         return this.chemoAgentCriteria;
     }
 
     public SurvivalCriteria getSurvivalCriteria() {
         return this.survivalCriteria;
     }
 
     public AgeCriteria getAgeCriteria() {
         return this.ageCriteria;
     }
 
     public GenderCriteria getGenderCriteria() {
         return this.genderCriteria;
     }
 
     public ArrayList getRecurrenceTypeColl() {
         return recurrenceTypeColl;
     }
 
     public ArrayList getRadiationTypeColl() {
         return radiationTypeColl;
     }
 
     public ArrayList getChemoAgentTypeColl() {
         return chemoAgentTypeColl;
     }
 
     public ArrayList getSurgeryTypeColl() {
         return surgeryTypeColl;
     }
 
     public ArrayList getSurvivalLowerColl() {
         return survivalLowerColl;
     }
 
     public ArrayList getSurvivalUpperColl() {
         return survivalUpperColl;
     }
 
     public ArrayList getAgeLowerColl() {
         return ageLowerColl;
     }
 
     public ArrayList getAgeUpperColl() {
         return ageUpperColl;
     }
 
     public ArrayList getGenderTypeColl() {
         return genderTypeColl;
     }
 
     public ClinicalDataForm cloneMe() {
         ClinicalDataForm form = new ClinicalDataForm();
         form.setQueryName(queryName);
         form.setTumorGrade(tumorGrade);
         form.setTumorType(tumorType);
         form.setResultView(resultView);
         form.setFirstPresentation(firstPresentation);
         form.setRecurrence(recurrence);
         form.setRecurrenceType(recurrenceType);
         form.setRadiation(radiation);
         form.setRadiationType(radiationType);
         form.setChemo(chemo);
         form.setChemoType(chemoType);
         form.setSurgery(surgery);
         form.setSurgeryType(surgeryType);
         form.setSurvivalLower(survivalLower);
         form.setSurvivalUpper(survivalUpper);
         form.setAgeLower(ageLower);
         form.setAgeUpper(ageUpper);
         form.setGenderType(genderType);
         form.setSampleList(sampleList);
         form.setSampleFile(sampleFile);
         form.setSampleGroup(sampleGroup);
         return form;
     }
     /**
      * Method validate
      * 
      * @param ActionMapping
      *            mapping
      * @param HttpServletRequest
      *            request
      * @return ActionErrors
      */
     public ActionErrors validate(ActionMapping mapping,
             HttpServletRequest request) {
 
         ActionErrors errors = new ActionErrors();
 
         // Query Name cannot be blank
 
         if ((queryName == null || queryName.length() < 1))
             errors.add("queryName", new ActionError(
                     "gov.nih.nci.nautilus.ui.struts.form.queryname.no.error"));
 
         // survival range validations
         if (this.survivalLower != null && this.survivalUpper != null) {
             try {
                 if((survivalLower.trim().length() > 0 && !(survivalUpper.trim().length() > 0)) ||
                         (survivalUpper.trim().length() > 0 && !(survivalLower.trim().length() > 0))){
                     errors.add("survivalUpper",new ActionError("gov.nih.nci.nautilus.ui.struts.form.survivalRange.upperOrLowerMissing.error"));                    
                 }
                 else if (Integer.parseInt(survivalLower) >= Integer.parseInt(survivalUpper)) {
                     errors.add("survivalUpper",new ActionError("gov.nih.nci.nautilus.ui.struts.form.survivalRange.upperRange.error"));
                 }
             } catch (NumberFormatException ex) {
               logger.error(ex);
             }
         }
 
         if (this.ageLower != null && this.ageUpper != null) {
             try {
                 if((ageLower.trim().length() > 0 && !(ageUpper.trim().length() > 0)) ||
                         (ageUpper.trim().length() > 0 && !(ageLower.trim().length() > 0))){
                     errors.add("ageUpper",new ActionError("gov.nih.nci.nautilus.ui.struts.form.survivalRange.upperOrLowerMissing.error"));                    
                 }
                 else if(Integer.parseInt(ageLower) >= Integer.parseInt(ageUpper)) {
                     errors.add("ageUpper",
                                   new ActionError("gov.nih.nci.nautilus.ui.struts.form.ageRange.upperRange.error"));
                 }
             } catch (NumberFormatException ex) {
              
             }
         }
 
         if (errors.isEmpty()) {
 
             createDiseaseCriteriaObject();
             createOccurrenceCriteriaObject();
             createRadiationTherapyCriteriaObject();
             createChemoAgentCriteriaObject();
             createSurgeryTypeCriteriaObject();
             createSurvivalCriteriaObject();
             createAgeCriteriaObject();
             createGenderCriteriaObject();
             createSampleCriteriaObject();
         }
 
         return errors;
     }
 
 }
