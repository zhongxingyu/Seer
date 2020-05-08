 package edu.ucdenver.ccp.PhenoGen.data.internal;
 
 import javax.servlet.http.HttpSession;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.List;
 
 import edu.ucdenver.ccp.PhenoGen.data.Dataset;
 import edu.ucdenver.ccp.PhenoGen.data.Experiment;
 import edu.ucdenver.ccp.PhenoGen.data.GeneList;
 import edu.ucdenver.ccp.PhenoGen.data.User;
 
 import edu.ucdenver.ccp.util.Debugger;
 import edu.ucdenver.ccp.util.ObjectHandler;
 
 /* for logging messages */
 import org.apache.log4j.Logger;
 
 /**
  * Class for handling the resources available for downloading
  *  @author  Cheryl Hornbaker
  */
 
 public class Resource {
 
 	private Logger log=null;
 
 	private Debugger myDebugger = new Debugger();
 	private ObjectHandler myObjectHandler = new ObjectHandler();
 	public MarkerDataFile[] markerDataFiles = null;
         public SAMDataFile[] samDataFiles = null;
         public GenotypeDataFile[] genotypeDataFiles = null;
 	public ExpressionDataFile[] expressionDataFiles = null;
 	public EQTLDataFile[] eQTLDataFiles = null;
 	public HeritabilityDataFile[] heritabilityDataFiles = null;
         private Dataset[] publicDatasets = null;
 	private HttpSession session ;
 	private edu.ucdenver.ccp.PhenoGen.data.Array myArray = new edu.ucdenver.ccp.PhenoGen.data.Array();
         public static final String BXDRI_PANEL = "BXD Recombinant Inbred Panel"; 
         public static final String INBRED_PANEL = "Inbred Panel"; 
         public static final String HXBRI_PANEL = "HXB/BXH Recombinant Inbred Panel";
         public static final String LXSRI_PANEL = "ILSXISS Recombinant Inbred Panel";
 
         private int id;
         private String organism;
         private String source;
         private String panel;
         private String tissue;
         private String arrayName;
         private String rnaType;
         private String techType;
         private String readType;
         private String panelStr;
         private Dataset dataset;
         private String population;
         private String ancestry;
         //private String context="";
 
 
 	public Resource() {
 		log = Logger.getRootLogger();
 	}
 
 	public Resource(int id) {
 		log = Logger.getRootLogger();
 		setID(id);
 	}
 
 	public Resource(int id, String organism, String panel, String tissue, String arrayName) {
 		log = Logger.getRootLogger();
 		setID(id);
 		setOrganism(organism);
 		setPanel(panel);
 		setTissue(tissue);
 		setArrayName(arrayName);
 	}
 
 	public Resource(int id, String organism, String panel, Dataset dataset, String tissue, String arrayName, ExpressionDataFile[] expressionFileArray, EQTLDataFile[] eQTLFileArray, HeritabilityDataFile[] heritabilityFileArray) {
 		log = Logger.getRootLogger();
 		setID(id);
 		setOrganism(organism);
 		setPanel(panel);
 		setDataset(dataset);
 		setTissue(tissue);
 		setArrayName(arrayName);
 		setExpressionDataFiles(expressionFileArray);
 		setEQTLDataFiles(eQTLFileArray);
 		setHeritabilityDataFiles(heritabilityFileArray);
 	}
         
         public Resource(int id, String organism, String strain,String rnaType,String tissue,String tech,String readType, SAMDataFile[] samFileArray) {
 		log = Logger.getRootLogger();
 		setID(id);
 		setOrganism(organism);
 		setSource(strain);
 		setSAMDataFiles(samFileArray);
                 setRNAType(rnaType);
                 setTissue(tissue);
                 setTechType(tech);
                 setReadType(readType);
 	}
         
         public Resource(int id, String organism, String population,String ancestry,String tech, GenotypeDataFile[] genotypeFileArray) {
 		log = Logger.getRootLogger();
 		setID(id);
 		setOrganism(organism);
 		setPopulation(population);
 		setGenotypeDataFiles(genotypeFileArray);
                 setAncestry(ancestry);
                 setTechType(tech);
 	}
 
 	public Resource(int id, String organism, String source, Dataset dataset, MarkerDataFile[] markerFileArray, EQTLDataFile[] eQTLFileArray,String paneltmp) {
 		log = Logger.getRootLogger();
 		setID(id);
 		setOrganism(organism);
 		setSource(source);
 		setDataset(dataset);
 		setMarkerDataFiles(markerFileArray);
 		setEQTLDataFiles(eQTLFileArray);
                 setPanelString(paneltmp);
 	}
 
 	public Resource(int id, String organism, String panel) {
 		log = Logger.getRootLogger();
 		setID(id);
 		setOrganism(organism);
 		setPanel(panel);
 	}
 
         public Resource(HttpSession session) {
                 log = Logger.getRootLogger();
 		setSession(session); 
 		//log.debug("instantiated Resource setting session variable");
 	}
 
         public void setID(int inInt) {
                 this.id = inInt;
         }
 
         public int getID() {
                 return this.id;
         }
 
         public void setOrganism(String inString) {
                 this.organism = inString;
         }
 
         public String getOrganism() {
                 return this.organism;
         }
 
         public void setSource(String inString) {
                 this.source = inString;
         }
 
         public String getSource() {
                 return this.source;
         }
 
         public void setPanel(String inString) {
                 this.panel = inString;
         }
 
         public String getPanel() {
                 return this.panel;
         }
         
         public void setPanelString(String inString) {
                 this.panelStr = inString;
         }
 
         public String getPanelString() {
                 return this.panelStr;
         }
 
         public void setTissue(String inString) {
                 this.tissue = inString;
         }
 
         public String getTissue() {
                 return this.tissue;
         }
 
     public String getReadType() {
         return readType;
     }
 
     public void setReadType(String readType) {
         this.readType = readType;
     }
 
     public String getRNAType() {
         return rnaType;
     }
 
     public void setRNAType(String rnaType) {
         this.rnaType = rnaType;
     }
 
     public String getTechType() {
         return techType;
     }
 
     public void setTechType(String techType) {
         this.techType = techType;
     }
 
         public void setArrayName(String inString) {
                 this.arrayName = inString;
         }
 
         public String getArrayName() {
                 return this.arrayName;
         }
 
         public void setDataset(Dataset inDataset) {
                 this.dataset = inDataset;
         }
 
         public Dataset getDataset() {
                 return this.dataset;
         }
 
         public void setMarkerDataFiles(MarkerDataFile[] inMarkerDataFiles) {
                 this.markerDataFiles = inMarkerDataFiles;
         }
 
         public MarkerDataFile[] getMarkerDataFiles() {
                 return this.markerDataFiles;
         }
         
         public void setSAMDataFiles(SAMDataFile[] inSAMDataFiles) {
                 this.samDataFiles = inSAMDataFiles;
         }
         public SAMDataFile[] getSAMDataFiles() {
                 return this.samDataFiles;
         }
         
         public void setGenotypeDataFiles(GenotypeDataFile[] inGenotypeDataFiles) {
                 this.genotypeDataFiles = inGenotypeDataFiles;
         }
         public GenotypeDataFile[] getGenotypeDataFiles() {
                 return this.genotypeDataFiles;
         }
 
         public void setExpressionDataFiles(ExpressionDataFile[] inExpressionDataFiles) {
                 this.expressionDataFiles = inExpressionDataFiles;
         }
 
         public ExpressionDataFile[] getExpressionDataFiles() {
                 return this.expressionDataFiles;
         }
 
         public void setEQTLDataFiles(EQTLDataFile[] inEQTLDataFiles) {
                 this.eQTLDataFiles = inEQTLDataFiles;
         }
 
         public EQTLDataFile[] getEQTLDataFiles() {
                 return this.eQTLDataFiles;
         }
 
         public void setHeritabilityDataFiles(HeritabilityDataFile[] inHeritabilityDataFiles) {
                 this.heritabilityDataFiles = inHeritabilityDataFiles;
         }
 
         public HeritabilityDataFile[] getHeritabilityDataFiles() {
                 return this.heritabilityDataFiles;
         }
 
         public String getPopulation() {
             return population;
         }
 
         public void setPopulation(String population) {
             this.population = population;
         }
 
         public String getAncestry() {
             return ancestry;
         }
 
         public void setAncestry(String ancestry) {
             this.ancestry = ancestry;
         }
         
         
 
 	public HttpSession getSession() {
 		log.debug("in getSession");
 		return session;
 	}
 
 	public void setSession(HttpSession inSession) {
 		log.debug("in Resource.setSession");
 		this.session = inSession;
                 //this.context=(String)this.session.getAttribute("contextRoot");
                 //this.context=this.context.substring(0,this.context.length()-1);
                 this.publicDatasets = ((Dataset[]) session.getAttribute("publicDatasets") == null ?
                                 null :
                                 (Dataset[]) session.getAttribute("publicDatasets"));
 	}
 
 	/**
 	 * Gets all the expression and marker resources
 	 * @return	an array of Resource objects
 	 */
 	public Resource[] getAllResources() {
 		List<Resource> expressionResources = Arrays.asList(getExpressionResources());
 		List<Resource> markerResources = Arrays.asList(getMarkerResources());
                 List<Resource> rnaResources = Arrays.asList(getRNASeqResources());
                 List<Resource> genotypingResources = Arrays.asList(getGenotypingResources());
 		List<Resource> allResources = new ArrayList<Resource>(expressionResources);
 		allResources.addAll(markerResources);
                 allResources.addAll(rnaResources);
                 allResources.addAll(genotypingResources);
 		Resource[] allResourcesArray = myObjectHandler.getAsArray(allResources, Resource.class);
 		return allResourcesArray;
 	}
 
 	/**
 	 * Gets all the expression resources
 	 * @return	an array of Resource objects
 	 */
 	public Resource[] getExpressionResources() {
 
 		log.debug("in getExpressionResources");
 		List<Resource> resourceList = new ArrayList<Resource>();
 		log.debug("publicDatasets has " + publicDatasets.length + " entries");
 
         	Dataset myDataset = new Dataset();
         	Dataset BXDRI_Dataset = myDataset.getDatasetFromMyDatasets(publicDatasets, myDataset.BXDRI_DATASET_NAME);
         	Dataset HXBRI_Dataset = myDataset.getDatasetFromMyDatasets(publicDatasets, myDataset.HXBRI_DATASET_NAME);
         	Dataset Inbred_Dataset = myDataset.getDatasetFromMyDatasets(publicDatasets, myDataset.INBRED_DATASET_NAME);
         	Dataset LXSRI_Dataset = myDataset.getDatasetFromMyDatasets(publicDatasets, myDataset.LXSRI_DATASET_NAME);
         	Dataset HXBRI_Brain_Exon_Dataset = myDataset.getDatasetFromMyDatasets(publicDatasets, myDataset.HXBRI_BRAIN_EXON_DATASET_NAME);
         	Dataset HXBRI_Heart_Exon_Dataset = myDataset.getDatasetFromMyDatasets(publicDatasets, myDataset.HXBRI_HEART_EXON_DATASET_NAME);
         	Dataset HXBRI_Liver_Exon_Dataset = myDataset.getDatasetFromMyDatasets(publicDatasets, myDataset.HXBRI_LIVER_EXON_DATASET_NAME);
         	Dataset HXBRI_Brown_Adipose_Exon_Dataset = myDataset.getDatasetFromMyDatasets(publicDatasets, myDataset.HXBRI_BROWN_ADIPOSE_EXON_DATASET_NAME);
 
 		// Setup the BXDRI stuff
 		String resourcesDir = BXDRI_Dataset.getResourcesDir();
                 resourcesDir=resourcesDir.substring(resourcesDir.indexOf("/userFiles/"));
 		String datasetDir = BXDRI_Dataset.getPath();
 
 		List<ExpressionDataFile> expressionFileList = new ArrayList<ExpressionDataFile>();
 		expressionFileList.add(new ExpressionDataFile("Normalized expression values", resourcesDir + "BXD_v6_Affymetrix.Normalization.output.csv.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 1", resourcesDir + "PublicBXDRIMice_RawData_Part1.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 2", resourcesDir + "PublicBXDRIMice_RawData_Part2.zip"));
 		ExpressionDataFile[] expressionFileArray = myObjectHandler.getAsArray(expressionFileList, ExpressionDataFile.class);
 
 		List<EQTLDataFile> eQTLFileList = new ArrayList<EQTLDataFile>();
 		eQTLFileList.add(new EQTLDataFile("eQTLs using Wellcome Trust Markers", resourcesDir + "BXD_eQTL_WellcomeTrustMarkers_16Apr12.csv.zip"));
 		EQTLDataFile[] eQTLFileArray = myObjectHandler.getAsArray(eQTLFileList, EQTLDataFile.class);
 
 		List<HeritabilityDataFile> heritabilityFileList = new ArrayList<HeritabilityDataFile>();
 		heritabilityFileList.add(new HeritabilityDataFile("Heritability file from RMA normalization plus probe mask", resourcesDir + "herits.BXD.zip"));
 		HeritabilityDataFile[] heritabilityFileArray = myObjectHandler.getAsArray(heritabilityFileList, HeritabilityDataFile.class);
 
                 resourceList.add(new Resource(1, "Mouse", BXDRI_PANEL, BXDRI_Dataset, "Whole Brain", myArray.MOUSE430V2_ARRAY_TYPE,  expressionFileArray, eQTLFileArray, heritabilityFileArray));
 
 		// Setup the LXSRI stuff
 		resourcesDir = LXSRI_Dataset.getResourcesDir();
                 resourcesDir=resourcesDir.substring(resourcesDir.indexOf("/userFiles/"));
 		datasetDir = LXSRI_Dataset.getPath();
 
 		expressionFileList = new ArrayList<ExpressionDataFile>();
 		expressionFileList.add(new ExpressionDataFile("Normalized expression values and DABG p-values for Core Transcripts", resourcesDir + "LXS_v1_Affymetrix.Normalization.output.csv.zip"));
 		expressionFileList.add(new ExpressionDataFile("Normalized expression values and DABG p-values for Full Transcripts", resourcesDir + "LXS_v2_Affymetrix.Normalization.output.csv.zip"));
                 expressionFileList.add(new ExpressionDataFile("Normalized expression values and DABG p-values for Full Probesets", resourcesDir + "LXS_v3_Affymetrix.Normalization.output.csv.zip"));
 		expressionFileList.add(new ExpressionDataFile("Core Transcripts Detection Above Background p-values", resourcesDir + "dabg.coreTrans.LXS.PhenoGen.txt.zip"));
 		expressionFileList.add(new ExpressionDataFile("Core Transcripts Normalized expression values", resourcesDir + "rma.coreTrans.LXS.PhenoGen.txt.zip"));
 		expressionFileList.add(new ExpressionDataFile("Full Transcripts Detection Above Background p-values", resourcesDir + "dabg.fullTrans.LXS.PhenoGen.txt.zip"));
 		expressionFileList.add(new ExpressionDataFile("Full Transcripts Normalized expression values", resourcesDir + "rma.fullTrans.LXS.PhenoGen.txt.zip"));
                 expressionFileList.add(new ExpressionDataFile("Full Probesets Detection Above Background p-values", resourcesDir + "dabg.fullPS.LXS.PhenoGen.txt.zip"));
 		expressionFileList.add(new ExpressionDataFile("Full Probesets Normalized expression values", resourcesDir + "rma.fullPS.LXS.PhenoGen.txt.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 1", resourcesDir + "PublicLXSRIMice_RawData_Part1.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 2", resourcesDir + "PublicLXSRIMice_RawData_Part2.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 3", resourcesDir + "PublicLXSRIMice_RawData_Part3.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 4", resourcesDir + "PublicLXSRIMice_RawData_Part4.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 5", resourcesDir + "PublicLXSRIMice_RawData_Part5.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 6", resourcesDir + "PublicLXSRIMice_RawData_Part6.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 7", resourcesDir + "PublicLXSRIMice_RawData_Part7.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 8", resourcesDir + "PublicLXSRIMice_RawData_Part8.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 9", resourcesDir + "PublicLXSRIMice_RawData_Part9.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 10", resourcesDir + "PublicLXSRIMice_RawData_Part10.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 11", resourcesDir + "PublicLXSRIMice_RawData_Part11.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 12", resourcesDir + "PublicLXSRIMice_RawData_Part12.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 13", resourcesDir + "PublicLXSRIMice_RawData_Part13.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 14", resourcesDir + "PublicLXSRIMice_RawData_Part14.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 15", resourcesDir + "PublicLXSRIMice_RawData_Part15.zip"));
 		expressionFileArray = myObjectHandler.getAsArray(expressionFileList, ExpressionDataFile.class);
                                                                                                                                                                                                 
 		eQTLFileList = new ArrayList<EQTLDataFile>();
 		eQTLFileList.add(new EQTLDataFile("eQTLs for Core Transcripts from the Affymetrix Mouse Diversity SNP Array data gathered by Churchill et al. in .csv format", resourcesDir + "LXS_eQTL_CoreTrans_JAXMarkers_01Jun12.csv.zip"));
 		eQTLFileList.add(new EQTLDataFile("eQTLs for Core Transcripts from the Affymetrix Mouse Diversity SNP Array data gathered by Churchill et al. in .txt format", resourcesDir + "LXS_eQTL_CoreTrans_JAXMarkers_01Jun12.txt.zip"));
                 eQTLFileList.add(new EQTLDataFile("eQTLs for Full Probesets from the Affymetrix Mouse Diversity SNP Array data gathered by Churchill et al. in .csv format", resourcesDir + "LXS_eQTL_FullPS_JAXMarkers_27Jun12.csv.zip"));
 		eQTLFileList.add(new EQTLDataFile("eQTLs for Full Probesets from the Affymetrix Mouse Diversity SNP Array data gathered by Churchill et al. in .txt format", resourcesDir + "LXS_eQTL_FullPS_JAXMarkers_27Jun12.txt.zip"));
 		eQTLFileArray = myObjectHandler.getAsArray(eQTLFileList, EQTLDataFile.class);
 
 		heritabilityFileList = new ArrayList<HeritabilityDataFile>();
 		heritabilityFileList.add(new HeritabilityDataFile("Heritabilty File from Core Transcripts", resourcesDir + "herits.coreTrans.LXS.Brain.txt.zip"));
 		heritabilityFileList.add(new HeritabilityDataFile("Heritabilty File from Full Transcripts", resourcesDir + "herits.fullTrans.LXS.Brain.txt.zip"));
                 heritabilityFileList.add(new HeritabilityDataFile("Heritabilty File from Full Transcripts", resourcesDir + "herits.fullPS.LXS.Brain.txt.zip"));
 		heritabilityFileArray = myObjectHandler.getAsArray(heritabilityFileList, HeritabilityDataFile.class);
 
                 resourceList.add(new Resource(2, "Mouse", LXSRI_PANEL, LXSRI_Dataset, "Whole Brain", myArray.MOUSE_EXON_ARRAY_TYPE,  expressionFileArray, eQTLFileArray, heritabilityFileArray));
 
 
 		// Setup the Inbred stuff
 		resourcesDir =Inbred_Dataset.getResourcesDir();
                 resourcesDir=resourcesDir.substring(resourcesDir.indexOf("/userFiles/"));
 		datasetDir = Inbred_Dataset.getPath();
 
 		expressionFileList = new ArrayList<ExpressionDataFile>();
 		expressionFileList.add(new ExpressionDataFile("Normalized expression values", resourcesDir + "Inbred_v6_Affymetrix.Normalization.output.csv.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 1", resourcesDir + "PublicInbredMice_RawData_Part1.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 2", resourcesDir + "PublicInbredMice_RawData_Part2.zip"));
 		expressionFileArray = myObjectHandler.getAsArray(expressionFileList, ExpressionDataFile.class);
 
 		eQTLFileList = new ArrayList<EQTLDataFile>();
 		eQTLFileArray = myObjectHandler.getAsArray(eQTLFileList, EQTLDataFile.class);
 
 		heritabilityFileList = new ArrayList<HeritabilityDataFile>();
 		heritabilityFileList.add(new HeritabilityDataFile("Heritability file from RMA normalization plus probe mask", resourcesDir + "herits.Inbred.txt.zip"));
 		heritabilityFileArray = myObjectHandler.getAsArray(heritabilityFileList, HeritabilityDataFile.class);
 
                 resourceList.add(new Resource(3, "Mouse", INBRED_PANEL, Inbred_Dataset, "Whole Brain", myArray.MOUSE430V2_ARRAY_TYPE,  expressionFileArray, eQTLFileArray, heritabilityFileArray));
 
 		// Setup the HXBRI stuff
 		resourcesDir = HXBRI_Dataset.getResourcesDir();
                 resourcesDir=resourcesDir.substring(resourcesDir.indexOf("/userFiles/"));
 		datasetDir = HXBRI_Dataset.getPath();
 
 		expressionFileList = new ArrayList<ExpressionDataFile>();
 		expressionFileList.add(new ExpressionDataFile("Normalized expression values", resourcesDir + "HXB_BXH_v6_CodeLink.Normalization.output.csv.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - TXT Files", resourcesDir + "PublicHXB_BXHRIRats_RawData.zip"));
 		expressionFileArray = myObjectHandler.getAsArray(expressionFileList, ExpressionDataFile.class);
 
 		eQTLFileList = new ArrayList<EQTLDataFile>();
 		eQTLFileList.add(new EQTLDataFile("eQTLs using STAR Consortium Markers", resourcesDir + "HXB_BXH_eQTL_STARConsortiumMarkers_07Oct09.txt.zip"));
 		eQTLFileArray = myObjectHandler.getAsArray(eQTLFileList, EQTLDataFile.class);
 
 		heritabilityFileList = new ArrayList<HeritabilityDataFile>();
 		heritabilityFileList.add(new HeritabilityDataFile("Heritability file from RMA normalization plus probe mask", resourcesDir + "herits.HXB.txt.zip"));
 		heritabilityFileArray = myObjectHandler.getAsArray(heritabilityFileList, HeritabilityDataFile.class);
 
                 resourceList.add(new Resource(4, "Rat", HXBRI_PANEL, HXBRI_Dataset, "Whole Brain", myArray.CODELINK_RAT_ARRAY_TYPE,  expressionFileArray, eQTLFileArray, heritabilityFileArray));
 
 		// Setup the HXBRI Brain Exon stuff
 		resourcesDir = HXBRI_Brain_Exon_Dataset.getResourcesDir();
                 resourcesDir=resourcesDir.substring(resourcesDir.indexOf("/userFiles/"));
 		datasetDir = HXBRI_Brain_Exon_Dataset.getPath();
 
 		expressionFileList = new ArrayList<ExpressionDataFile>();
 		expressionFileList.add(new ExpressionDataFile("Normalized expression values and DABG p-values for Core Transcripts", resourcesDir + "HXB_BXH.brain_v1_Affymetrix.Normalization.output.csv.zip"));
 		expressionFileList.add(new ExpressionDataFile("Normalized expression values and DABG p-values for Full Transcripts", resourcesDir + "HXB_BXH.brain_v2_Affymetrix.Normalization.output.csv.zip"));
                 expressionFileList.add(new ExpressionDataFile("Normalized expression values and DABG p-values for Full Probesets", resourcesDir + "HXB_BXH.brain_v3_Affymetrix.Normalization.output.csv.zip"));
 		expressionFileList.add(new ExpressionDataFile("Core Transcripts Detection Above Background p-values", resourcesDir + "dabg.coreTrans.HXB_BXH.brain.PhenoGen.txt.zip"));
 		expressionFileList.add(new ExpressionDataFile("Core Transcripts Normalized expression values", resourcesDir + "rma.coreTrans.HXB_BXH.brain.PhenoGen.txt.zip"));
 		expressionFileList.add(new ExpressionDataFile("Full Transcripts Detection Above Background p-values", resourcesDir + "dabg.fullTrans.HXB_BXH.brain.PhenoGen.txt.zip"));
 		expressionFileList.add(new ExpressionDataFile("Full Transcripts Normalized expression values", resourcesDir + "rma.fullTrans.HXB_BXH.brain.PhenoGen.txt.zip"));
                 expressionFileList.add(new ExpressionDataFile("Full Probesets Detection Above Background p-values", resourcesDir + "dabg.fullPS.HXB_BXH.brain.PhenoGen.txt.zip"));
 		expressionFileList.add(new ExpressionDataFile("Full Probesets Normalized expression values", resourcesDir + "rma.fullPS.HXB_BXH.brain.PhenoGen.txt.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 1", resourcesDir + "PublicHXB_BXH.Brain.Exon.RawData_Part1.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 2", resourcesDir + "PublicHXB_BXH.Brain.Exon.RawData_Part2.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 3", resourcesDir + "PublicHXB_BXH.Brain.Exon.RawData_Part3.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 4", resourcesDir + "PublicHXB_BXH.Brain.Exon.RawData_Part4.zip"));
 		expressionFileArray = myObjectHandler.getAsArray(expressionFileList, ExpressionDataFile.class);
 
 		eQTLFileList = new ArrayList<EQTLDataFile>();
 		eQTLFileList.add(new EQTLDataFile("eQTLs for Core Transcripts in .csv format", resourcesDir + "HXB.BXH.eQTL.brain.coreTrans.11Jan12.csv.zip"));
 		eQTLFileList.add(new EQTLDataFile("eQTLs for Core Transcripts in .txt format", resourcesDir + "HXB.BXH.eQTL.brain.coreTrans.11Jan12.txt.zip"));
                 eQTLFileList.add(new EQTLDataFile("eQTLs for Full Probesets in .csv format", resourcesDir + "HXB.BXH.eQTL.brain.fullPS.3Apr12.csv.zip"));
 		eQTLFileList.add(new EQTLDataFile("eQTLs for Full Probesets in .txt format", resourcesDir + "HXB.BXH.eQTL.brain.fullPS.3Apr12.txt.zip"));
 		eQTLFileArray = myObjectHandler.getAsArray(eQTLFileList, EQTLDataFile.class);
 
 		heritabilityFileList = new ArrayList<HeritabilityDataFile>();
 		heritabilityFileList.add(new HeritabilityDataFile("Heritabilty File from Core Transcripts", resourcesDir + "herits.coreTrans.HXB_BXH.brain.txt.zip"));
 		heritabilityFileList.add(new HeritabilityDataFile("Heritabilty File from Full Transcripts", resourcesDir + "herits.fullTrans.HXB_BXH.brain.txt.zip"));
 		heritabilityFileList.add(new HeritabilityDataFile("Heritabilty File from Full Probesets", resourcesDir + "herits.fullPS.HXB_BXH.brain.txt.zip"));
                 heritabilityFileArray = myObjectHandler.getAsArray(heritabilityFileList, HeritabilityDataFile.class);
 
                 resourceList.add(new Resource(5, "Rat", HXBRI_PANEL, HXBRI_Brain_Exon_Dataset, "Whole Brain", myArray.RAT_EXON_ARRAY_TYPE,  expressionFileArray, eQTLFileArray, heritabilityFileArray));
 
 		// Setup the HXBRI Heart Exon stuff
 		resourcesDir = HXBRI_Heart_Exon_Dataset.getResourcesDir();
                 resourcesDir=resourcesDir.substring(resourcesDir.indexOf("/userFiles/"));
 		datasetDir = HXBRI_Heart_Exon_Dataset.getPath();
 
 		expressionFileList = new ArrayList<ExpressionDataFile>();
 		expressionFileList.add(new ExpressionDataFile("Normalized expression values and DABG p-values for Core Transcripts", resourcesDir + "HXB_BXH.heart_v1_Affymetrix.Normalization.output.csv.zip"));
 		expressionFileList.add(new ExpressionDataFile("Normalized expression values and DABG p-values for Full Transcripts", resourcesDir + "HXB_BXH.heart_v2_Affymetrix.Normalization.output.csv.zip"));
 		expressionFileList.add(new ExpressionDataFile("Normalized expression values and DABG p-values for Full Probesets", resourcesDir + "HXB_BXH.heart_v3_Affymetrix.Normalization.output.csv.zip"));
                 expressionFileList.add(new ExpressionDataFile("Core Transcripts Detection Above Background p-values", resourcesDir + "dabg.coreTrans.HXB_BXH.heart.PhenoGen.txt.zip"));
 		expressionFileList.add(new ExpressionDataFile("Core Transcripts Normalized expression values", resourcesDir + "rma.coreTrans.HXB_BXH.heart.PhenoGen.txt.zip"));
 		expressionFileList.add(new ExpressionDataFile("Full Transcripts Detection Above Background p-values", resourcesDir + "dabg.fullTrans.HXB_BXH.heart.PhenoGen.txt.zip"));
 		expressionFileList.add(new ExpressionDataFile("Full Transcripts Normalized expression values", resourcesDir + "rma.fullTrans.HXB_BXH.heart.PhenoGen.txt.zip"));
 		expressionFileList.add(new ExpressionDataFile("Full Probesets Detection Above Background p-values", resourcesDir + "dabg.fullPS.HXB_BXH.heart.PhenoGen.txt.zip"));
 		expressionFileList.add(new ExpressionDataFile("Full Probesets Normalized expression values", resourcesDir + "rma.fullPS.HXB_BXH.heart.PhenoGen.txt.zip"));
                 expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 1", resourcesDir + "PublicHXB_BXH.Heart.Exon.RawData_Part1.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 2", resourcesDir + "PublicHXB_BXH.Heart.Exon.RawData_Part2.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 3", resourcesDir + "PublicHXB_BXH.Heart.Exon.RawData_Part3.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 4", resourcesDir + "PublicHXB_BXH.Heart.Exon.RawData_Part4.zip"));
 		expressionFileArray = myObjectHandler.getAsArray(expressionFileList, ExpressionDataFile.class);
 
 		//log.debug("csv file exists: "+new File(resourcesDir + "HXB.BXH.eQTL.brain.coreTrans.11Jan12.csv.zip").exists());
 		//log.debug("txt file exists: "+new File(resourcesDir + "HXB.BXH.eQTL.brain.coreTrans.11Jan12.txt.zip").exists());
 
 		eQTLFileList = new ArrayList<EQTLDataFile>();
 		eQTLFileList.add(new EQTLDataFile("eQTLs for Core Transcripts in .csv format", resourcesDir + "HXB.BXH.eQTL.heart.coreTrans.11Jan12.csv.zip"));
 		eQTLFileList.add(new EQTLDataFile("eQTLs for Core Transcripts in .txt format", resourcesDir + "HXB.BXH.eQTL.heart.coreTrans.11Jan12.txt.zip"));
                 eQTLFileList.add(new EQTLDataFile("eQTLs for Full Probesets in .csv format", resourcesDir + "HXB.BXH.eQTL.heart.fullPS.3Apr12.csv.zip"));
 		eQTLFileList.add(new EQTLDataFile("eQTLs for Full Probesets in .txt format", resourcesDir + "HXB.BXH.eQTL.heart.fullPS.3Apr12.txt.zip"));
 		eQTLFileArray = myObjectHandler.getAsArray(eQTLFileList, EQTLDataFile.class);
 
 		heritabilityFileList = new ArrayList<HeritabilityDataFile>();
 		heritabilityFileList.add(new HeritabilityDataFile("Heritabilty File from Core Transcripts", resourcesDir + "herits.coreTrans.HXB_BXH.heart.txt.zip"));
 		heritabilityFileList.add(new HeritabilityDataFile("Heritabilty File from Full Transcripts", resourcesDir + "herits.fullTrans.HXB_BXH.heart.txt.zip"));
                 heritabilityFileList.add(new HeritabilityDataFile("Heritabilty File from Full Probesets", resourcesDir + "herits.fullPS.HXB_BXH.heart.txt.zip"));
 		heritabilityFileArray = myObjectHandler.getAsArray(heritabilityFileList, HeritabilityDataFile.class);
 
                 resourceList.add(new Resource(6, "Rat", HXBRI_PANEL, HXBRI_Heart_Exon_Dataset, "Heart", myArray.RAT_EXON_ARRAY_TYPE,  expressionFileArray, eQTLFileArray, heritabilityFileArray));
 
 		// Setup the HXBRI Liver Exon stuff
 		resourcesDir = HXBRI_Liver_Exon_Dataset.getResourcesDir();
                 resourcesDir=resourcesDir.substring(resourcesDir.indexOf("/userFiles/"));
 		datasetDir = HXBRI_Liver_Exon_Dataset.getPath();
 
 		expressionFileList = new ArrayList<ExpressionDataFile>();
 		expressionFileList.add(new ExpressionDataFile("Normalized expression values and DABG p-values for Core Transcripts", resourcesDir + "HXB_BXH.liver_v1_Affymetrix.Normalization.output.csv.zip"));
 		expressionFileList.add(new ExpressionDataFile("Normalized expression values and DABG p-values for Full Transcripts", resourcesDir + "HXB_BXH.liver_v2_Affymetrix.Normalization.output.csv.zip"));
 		expressionFileList.add(new ExpressionDataFile("Normalized expression values and DABG p-values for Full Probesets", resourcesDir + "HXB_BXH.liver_v3_Affymetrix.Normalization.output.csv.zip"));
                 expressionFileList.add(new ExpressionDataFile("Core Transcripts Detection Above Background p-values", resourcesDir + "dabg.coreTrans.HXB_BXH.liver.PhenoGen.txt.zip"));
 		expressionFileList.add(new ExpressionDataFile("Core Transcripts Normalized expression values", resourcesDir + "rma.coreTrans.HXB_BXH.liver.PhenoGen.txt.zip"));
 		expressionFileList.add(new ExpressionDataFile("Full Transcripts Detection Above Background p-values", resourcesDir + "dabg.fullTrans.HXB_BXH.liver.PhenoGen.txt.zip"));
 		expressionFileList.add(new ExpressionDataFile("Full Transcripts Normalized expression values", resourcesDir + "rma.fullTrans.HXB_BXH.liver.PhenoGen.txt.zip"));
 		expressionFileList.add(new ExpressionDataFile("Full Probesets Detection Above Background p-values", resourcesDir + "dabg.fullPS.HXB_BXH.liver.PhenoGen.txt.zip"));
 		expressionFileList.add(new ExpressionDataFile("Full Probesets Normalized expression values", resourcesDir + "rma.fullPS.HXB_BXH.liver.PhenoGen.txt.zip"));
                 expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 1", resourcesDir + "PublicHXB_BXH.Liver.Exon.RawData_Part1.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 2", resourcesDir + "PublicHXB_BXH.Liver.Exon.RawData_Part2.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 3", resourcesDir + "PublicHXB_BXH.Liver.Exon.RawData_Part3.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 4", resourcesDir + "PublicHXB_BXH.Liver.Exon.RawData_Part4.zip"));
 		expressionFileArray = myObjectHandler.getAsArray(expressionFileList, ExpressionDataFile.class);
 
 		eQTLFileList = new ArrayList<EQTLDataFile>();
 		eQTLFileList.add(new EQTLDataFile("eQTLs for Core Transcripts in .csv format", resourcesDir + "HXB.BXH.eQTL.liver.coreTrans.11Jan12.csv.zip"));
 		eQTLFileList.add(new EQTLDataFile("eQTLs for Core Transcripts in .txt format", resourcesDir + "HXB.BXH.eQTL.liver.coreTrans.11Jan12.txt.zip"));
                 eQTLFileList.add(new EQTLDataFile("eQTLs for Full Probesets in .csv format", resourcesDir + "HXB.BXH.eQTL.liver.fullPS.3Apr12.csv.zip"));
 		eQTLFileList.add(new EQTLDataFile("eQTLs for Full Probesets in .txt format", resourcesDir + "HXB.BXH.eQTL.liver.fullPS.3Apr12.txt.zip"));
 		eQTLFileArray = myObjectHandler.getAsArray(eQTLFileList, EQTLDataFile.class);
 
 		heritabilityFileList = new ArrayList<HeritabilityDataFile>();
 		heritabilityFileList.add(new HeritabilityDataFile("Heritabilty File from Core Transcripts", resourcesDir + "herits.coreTrans.HXB_BXH.liver.txt.zip"));
 		heritabilityFileList.add(new HeritabilityDataFile("Heritabilty File from Full Transcripts", resourcesDir + "herits.fullTrans.HXB_BXH.liver.txt.zip"));
                 heritabilityFileList.add(new HeritabilityDataFile("Heritabilty File from Full Probesets", resourcesDir + "herits.fullPS.HXB_BXH.liver.txt.zip"));
 		heritabilityFileArray = myObjectHandler.getAsArray(heritabilityFileList, HeritabilityDataFile.class);
 
                 resourceList.add(new Resource(7, "Rat", HXBRI_PANEL, HXBRI_Liver_Exon_Dataset, "Liver", myArray.RAT_EXON_ARRAY_TYPE,  expressionFileArray, eQTLFileArray, heritabilityFileArray));
 
 		// Setup the HXBRI Brown Adipose Exon stuff
 		resourcesDir = HXBRI_Brown_Adipose_Exon_Dataset.getResourcesDir();
                 resourcesDir=resourcesDir.substring(resourcesDir.indexOf("/userFiles/"));
 		datasetDir = HXBRI_Brown_Adipose_Exon_Dataset.getPath();
 
 		expressionFileList = new ArrayList<ExpressionDataFile>();
 		expressionFileList.add(new ExpressionDataFile("Normalized expression values and DABG p-values for Core Transcripts", resourcesDir + "HXB_BXH.bat_v1_Affymetrix.Normalization.output.csv.zip"));
 		expressionFileList.add(new ExpressionDataFile("Normalized expression values and DABG p-values for Full Transcripts", resourcesDir + "HXB_BXH.bat_v2_Affymetrix.Normalization.output.csv.zip"));
                 expressionFileList.add(new ExpressionDataFile("Normalized expression values and DABG p-values for Full Probesets", resourcesDir + "HXB_BXH.bat_v3_Affymetrix.Normalization.output.csv.zip"));
 		expressionFileList.add(new ExpressionDataFile("Core Transcripts Detection Above Background p-values", resourcesDir + "dabg.coreTrans.HXB_BXH.BAT.PhenoGen.txt.zip"));
 		expressionFileList.add(new ExpressionDataFile("Core Transcripts Normalized expression values", resourcesDir + "rma.coreTrans.HXB_BXH.BAT.PhenoGen.txt.zip"));
 		expressionFileList.add(new ExpressionDataFile("Full Transcripts Detection Above Background p-values", resourcesDir + "dabg.fullTrans.HXB_BXH.BAT.PhenoGen.txt.zip"));
 		expressionFileList.add(new ExpressionDataFile("Full Transcripts Normalized expression values", resourcesDir + "rma.fullTrans.HXB_BXH.BAT.PhenoGen.txt.zip"));
                 expressionFileList.add(new ExpressionDataFile("Full Probesets Detection Above Background p-values", resourcesDir + "dabg.fullPS.HXB_BXH.BAT.PhenoGen.txt.zip"));
 		expressionFileList.add(new ExpressionDataFile("Full Probesets Normalized expression values", resourcesDir + "rma.fullPS.HXB_BXH.BAT.PhenoGen.txt.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 1", resourcesDir + "PublicHXB_BXH.BrownAdipose.Exon.RawData_Part1.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 2", resourcesDir + "PublicHXB_BXH.BrownAdipose.Exon.RawData_Part2.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 3", resourcesDir + "PublicHXB_BXH.BrownAdipose.Exon.RawData_Part3.zip"));
 		expressionFileList.add(new ExpressionDataFile("Raw Data - CEL Files, Part 4", resourcesDir + "PublicHXB_BXH.BrownAdipose.Exon.RawData_Part4.zip"));
 		expressionFileArray = myObjectHandler.getAsArray(expressionFileList, ExpressionDataFile.class);
 
 		eQTLFileList = new ArrayList<EQTLDataFile>();
 		eQTLFileList.add(new EQTLDataFile("eQTLs for Core Transcripts in .csv format", resourcesDir + "HXB.BXH.eQTL.BAT.coreTrans.11Jan12.csv.zip"));
 		eQTLFileList.add(new EQTLDataFile("eQTLs for Core Transcripts in .txt format", resourcesDir + "HXB.BXH.eQTL.BAT.coreTrans.11Jan12.txt.zip"));
                 eQTLFileList.add(new EQTLDataFile("eQTLs for Full Probesets in .csv format", resourcesDir + "HXB.BXH.eQTL.BAT.fullPS.3Apr12.csv.zip"));
 		eQTLFileList.add(new EQTLDataFile("eQTLs for Full Probesets in .txt format", resourcesDir + "HXB.BXH.eQTL.BAT.fullPS.3Apr12.txt.zip"));
 		eQTLFileArray = myObjectHandler.getAsArray(eQTLFileList, EQTLDataFile.class);
 
 		heritabilityFileList = new ArrayList<HeritabilityDataFile>();
 		heritabilityFileList.add(new HeritabilityDataFile("Heritabilty File from Core Transcripts", resourcesDir + "herits.coreTrans.HXB_BXH.bat.txt.zip"));
 		heritabilityFileList.add(new HeritabilityDataFile("Heritabilty File from Full Transcripts", resourcesDir + "herits.fullTrans.HXB_BXH.bat.txt.zip"));
                 heritabilityFileList.add(new HeritabilityDataFile("Heritabilty File from Full Probesets", resourcesDir + "herits.fullPS.HXB_BXH.bat.txt.zip"));
 		heritabilityFileArray = myObjectHandler.getAsArray(heritabilityFileList, HeritabilityDataFile.class);
 
                 resourceList.add(new Resource(8, "Rat", HXBRI_PANEL, HXBRI_Brown_Adipose_Exon_Dataset, "Brown Adipose", myArray.RAT_EXON_ARRAY_TYPE,  expressionFileArray, eQTLFileArray, heritabilityFileArray));
 
 		Resource[] resourceArray = myObjectHandler.getAsArray(resourceList, Resource.class);
 		return resourceArray;
 	}
 
 	/**
 	 * Gets all the genomic marker resources
 	 * @return	an array of Resource objects
 	 */
 	public Resource[] getMarkerResources() {
 		log.debug("in getMarkerResources");
 		List<Resource> resourceList = new ArrayList<Resource>();
 		log.debug("publicDatasets has " + publicDatasets.length + " entries");
 
         	Dataset myDataset = new Dataset();
         	Dataset BXDRI_Dataset = myDataset.getDatasetFromMyDatasets(publicDatasets, myDataset.BXDRI_DATASET_NAME);
         	Dataset HXBRI_Dataset = myDataset.getDatasetFromMyDatasets(publicDatasets, myDataset.HXBRI_DATASET_NAME);
         	Dataset LXSRI_Dataset = myDataset.getDatasetFromMyDatasets(publicDatasets, myDataset.LXSRI_DATASET_NAME);
 
 		// Setup the BXDRI stuff
 		String resourcesDir = BXDRI_Dataset.getResourcesDir();
                 resourcesDir=resourcesDir.substring(resourcesDir.indexOf("/userFiles/"));
 		String datasetDir = BXDRI_Dataset.getPath();
 
 		List<MarkerDataFile> markerFileList = new ArrayList<MarkerDataFile>();
 		markerFileList.add(new MarkerDataFile("BXD Markers", resourcesDir + "BXD_Markers.zip","BXD"));
 		MarkerDataFile[] markerFileArray = myObjectHandler.getAsArray(markerFileList, MarkerDataFile.class);
 
 		List<EQTLDataFile> eQTLFileList = new ArrayList<EQTLDataFile>();
 		eQTLFileList.add(new EQTLDataFile("eQTLs using Wellcome Trust Markers", resourcesDir + "BXD_eQTL_WellcomeTrustMarkers_16Apr12.csv.zip"));
 		EQTLDataFile[] eQTLFileArray = myObjectHandler.getAsArray(eQTLFileList, EQTLDataFile.class);
 
                 resourceList.add(new Resource(10, "Mouse", "<a href='http://www.well.ox.ac.uk/mouse/INBREDS' target='_blank'>Wellcome-CTC Mouse Strain SNP Genotype Set</a>", BXDRI_Dataset, markerFileArray, eQTLFileArray,"BXD"));
 //Wellcome-CTC Mouse Strain SNP Genotype Set (http://www.well.ox.ac.uk/mouse/INBREDS/)
 
 		// Setup the LXSRI stuff
 		resourcesDir = LXSRI_Dataset.getResourcesDir();
                 resourcesDir=resourcesDir.substring(resourcesDir.indexOf("/userFiles/"));
 		datasetDir = LXSRI_Dataset.getPath();
 
 		markerFileList = new ArrayList<MarkerDataFile>();
 		markerFileList.add(new MarkerDataFile("SNP information on the LXS RI panel was collected by Dr. Gary Churchill and colleagues at the Jackson "+
 					"Laboratory using the Affymetrix Mouse Diversity Genotyping array.  This information was gathered with funding from NIH "+
 					"grants (GM0706833 and AG0038070).", resourcesDir + "LXS_Markers.txt.zip","LXS"));
 		markerFileArray = myObjectHandler.getAsArray(markerFileList, MarkerDataFile.class);
 
 		eQTLFileList = new ArrayList<EQTLDataFile>();
 		eQTLFileList.add(new EQTLDataFile("eQTLs for Core Transcripts from the Affymetrix Mouse Diversity SNP Array", resourcesDir + "LXS_eQTL_CoreTrans_JAXMarkers_13Sep11.txt.zip"));
 		eQTLFileArray = myObjectHandler.getAsArray(eQTLFileList, EQTLDataFile.class);
 
                 resourceList.add(new Resource(11, "Mouse", "Affymetrix Mouse Diversity SNP Array", LXSRI_Dataset, markerFileArray, eQTLFileArray,"LXS"));
 
 		// Setup the HXBRI stuff
 		resourcesDir = HXBRI_Dataset.getResourcesDir();
                 resourcesDir=resourcesDir.substring(resourcesDir.indexOf("/userFiles/"));
 		datasetDir = HXBRI_Dataset.getPath();
 
 		markerFileList = new ArrayList<MarkerDataFile>();
 		markerFileList.add(new MarkerDataFile("HXB Markers", resourcesDir + "HXB_BXH_Markers.txt.zip","HXB/BXH"));
 		markerFileArray = myObjectHandler.getAsArray(markerFileList, MarkerDataFile.class);
 
 		eQTLFileList = new ArrayList<EQTLDataFile>();
 		eQTLFileList.add(new EQTLDataFile("eQTLs using STAR Consortium Markers", resourcesDir + "HXB_BXH_eQTL_STARConsortiumMarkers_07Oct09.txt.zip"));
 		eQTLFileArray = myObjectHandler.getAsArray(eQTLFileList, EQTLDataFile.class);
 
                resourceList.add(new Resource(12, "Rat", "<a href='http://oct2012.archive.ensembl.org/Rattus_norvegicus/Info/Content?file=star.html' target='_blank'>STAR consortium</a>", HXBRI_Dataset, markerFileArray, eQTLFileArray,"HXB/BXH"));
 
 		Resource[] resourceArray = myObjectHandler.getAsArray(resourceList, Resource.class);
 		return resourceArray;
 	}
         
         /**
 	 * Gets all the RNA Seq resources
 	 * @return	an array of Resource objects
 	 */
 	public Resource[] getRNASeqResources() {
 		log.debug("in getRNASeqResources");
                 String seqFilePath="/userFiles/public/RNASeq/";
 		List<Resource> resourceList = new ArrayList<Resource>();
                 
                 SAMDataFile[] bnlxFileList = new SAMDataFile[3];
                 bnlxFileList[0]=new SAMDataFile("BNLX Sample #1 RAW SAM File",seqFilePath+"BNLX/BNLX1.Illumina.PolyA.sam.zip");
                 bnlxFileList[1]=new SAMDataFile("BNLX Sample #2 RAW SAM File",seqFilePath+"BNLX/BNLX2.Illumina.PolyA.sam.zip");
                 bnlxFileList[2]=new SAMDataFile("BNLX Sample #3 RAW SAM File",seqFilePath+"BNLX/BNLX3.Illumina.PolyA.sam.zip");
                 resourceList.add(new Resource(50, "Rat", "BN-Lx/CubPrin","polyA+ (>200 nt) selected","Brain","Illumina HiSeq2000","100 bp paired-end", bnlxFileList ));
         	
                 SAMDataFile[] shrhFileList = new SAMDataFile[3];
                 shrhFileList[0]=new SAMDataFile("SHRH Sample #1 RAW SAM File",seqFilePath+"SHRH/SHRH1.Illumina.PolyA.sam.zip");
                 shrhFileList[1]=new SAMDataFile("SHRH Sample #2 RAW SAM File",seqFilePath+"SHRH/SHRH2.Illumina.PolyA.sam.zip");
                 shrhFileList[2]=new SAMDataFile("SHRH Sample #3 RAW SAM File",seqFilePath+"SHRH/SHRH3.Illumina.PolyA.sam.zip");
                 resourceList.add(new Resource(51, "Rat", "SHR/OlaIpcvPrin","polyA+ (>200 nt) selected","Brain","Illumina HiSeq2000","100 bp paired-end", shrhFileList ));
                 
                 SAMDataFile[] helicosBNLXFileList = new SAMDataFile[3];
                 helicosBNLXFileList[0]=new SAMDataFile("BNLX Sample #1 BED File",seqFilePath+"BNLX/BNLX1.Helicos.bed.zip");
                 helicosBNLXFileList[1]=new SAMDataFile("BNLX Sample #2 BED File",seqFilePath+"BNLX/BNLX2.Helicos.bed.zip");
                 helicosBNLXFileList[2]=new SAMDataFile("BNLX Sample #3 BED File",seqFilePath+"BNLX/BNLX3.Helicos.bed.zip");
                 resourceList.add(new Resource(52, "Rat", "BN-Lx/CubPrin","total RNA (>200 nt) after ribosomal RNA depletion","Brain","Helicos","~33 bp single-end", helicosBNLXFileList ));
         	
                 SAMDataFile[] helicosSHRHFileList = new SAMDataFile[3];
                 helicosSHRHFileList[0]=new SAMDataFile("SHRH Sample #1 BED File",seqFilePath+"SHRH/SHRH1.Helicos.bed.zip");
                 helicosSHRHFileList[1]=new SAMDataFile("SHRH Sample #2 BED File",seqFilePath+"SHRH/SHRH2.Helicos.bed.zip");
                 helicosSHRHFileList[2]=new SAMDataFile("SHRH Sample #3 BED File",seqFilePath+"SHRH/SHRH3.Helicos.bed.zip");
                 resourceList.add(new Resource(53, "Rat", "SHR/OlaIpcvPrin","total RNA (>200 nt) after ribosomal RNA depletion","Brain","Helicos","~33 bp single-end", helicosSHRHFileList ));
                 
                 Resource[] resourceArray = myObjectHandler.getAsArray(resourceList, Resource.class);
 		return resourceArray;
 
 	}
         
         
         /**
 	 * Gets all the Genotyping resources
 	 * @return	an array of Resource objects
 	 */
 	public Resource[] getGenotypingResources() {
 		log.debug("in getGenotypingResources");
                 String seqFilePath="/userFiles/public/Genotyping/";
 		List<Resource> resourceList = new ArrayList<Resource>();
                 
                 GenotypeDataFile[] genotypingFileList = new GenotypeDataFile[5];
                 genotypingFileList[0]=new GenotypeDataFile("Genotype CEL Files Part 1",seqFilePath+"Genotyping_1.zip");
                 genotypingFileList[1]=new GenotypeDataFile("Genotype CEL Files Part 2",seqFilePath+"Genotyping_2.zip");
                 genotypingFileList[2]=new GenotypeDataFile("Genotype CEL Files Part 3",seqFilePath+"Genotyping_3.zip");
                 genotypingFileList[3]=new GenotypeDataFile("Genotype CEL Files Part 4",seqFilePath+"Genotyping_4.zip");
                 genotypingFileList[4]=new GenotypeDataFile("Genotype CEL Files Part 5",seqFilePath+"Genotyping_5.zip");
                 resourceList.add(new Resource(70, "Human", "Alcohol dependent subjects receiving outpatient treatment at the Medical University of Vienna (Austria)",
                                             "self-reported European","Affymetrix Genome-Wide Human SNP Array 6.0", genotypingFileList ));
         	
                 Resource[] resourceArray = myObjectHandler.getAsArray(resourceList, Resource.class);
 		return resourceArray;
 	}
 
 	/**
 	 * Returns one Resource object from an array of Resource objects
 	 * @param myResources	an array of Resource objects 
 	 * @param id	the name of the resources to return 
 	 * @return            an Resource object
 	 */
 	public Resource getResourceFromMyResources(Resource[] myResources, int id) {
         	//
         	// Return the Resource object that contains the id from the myResources
         	//
 
         	myResources = sortResources(myResources, "id");
 
         	int idx = Arrays.binarySearch(myResources, new Resource(id), new ResourceSortComparator());
 		log.debug("idx = " + idx);
 	
         	Resource thisResource = null;
                 if(idx>-1){
                     thisResource=myResources[idx];
                 }
 
         	return thisResource;
 	}
 
 	public boolean equals(Object obj) {
 		if (!(obj instanceof Resource)) return false;
 		return this.id == ((Resource)obj).id;
 	}
         
 	public void print(Resource myResource) {
 		myResource.print();
 	}
 
 	public String toString() {
 		return "This Resource has organism = " + organism +
 		", tissue = " + tissue + ", and panel = " + panel;
 	}
 
 	public void print() {
 		log.debug("Resource = " + toString());
 	}
 
 	public Resource[] sortResources (Resource[] myResources, String sortColumn) {
 		setSortColumn(sortColumn);
 		Arrays.sort(myResources, new ResourceSortComparator());
 		return myResources;
 	}
 
 	private String sortColumn;
 	public void setSortColumn(String inString) {
 		this.sortColumn = inString;
 	}
 
 	public String getSortColumn() {
 		return sortColumn;
 	}
 
 	public class ResourceSortComparator implements Comparator<Resource> {
 		int compare;
 		Resource resource1, resource2;
 
 		public int compare(Resource resource1, Resource resource2) 	{ 
 			//log.debug("in ResourceSortComparator. sortOrder = "+getSortOrder() + ", sortColumn = "+getSortColumn());
 			//log.debug("resource1 organism = "+resource1.getOrganism()+ ", resource2 organism = "+resource2.getOrganism());
 
                 	if (getSortColumn().equals("organism")) {
                         	compare = resource1.getOrganism().compareTo(resource2.getOrganism());
                 	} else if (getSortColumn().equals("panel")) {
                         	compare = resource1.getPanel().compareTo(resource2.getPanel());
                 	} else if (getSortColumn().equals("arrayName")) {
                         	compare = resource1.getArrayName().compareTo(resource2.getArrayName());
                 	} else if (getSortColumn().equals("tissue")) {
                         	compare = resource1.getTissue().compareTo(resource2.getTissue());
                 	} else if (getSortColumn().equals("id")) {
                         	compare = new Integer(resource1.getID()).compareTo(new Integer(resource2.getID()));
 			}
                 	return compare;
         	}
 	}
 }
