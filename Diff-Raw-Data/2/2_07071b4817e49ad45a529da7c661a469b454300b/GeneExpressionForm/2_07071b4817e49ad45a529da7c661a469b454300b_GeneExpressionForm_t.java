 package gov.nih.nci.rembrandt.web.struts.form;
 
 import gov.nih.nci.caintegrator.dto.critieria.AllGenesCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.ArrayPlatformCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.CloneOrProbeIDCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.DiseaseOrGradeCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.FoldChangeCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.GeneIDCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.GeneOntologyCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.PathwayCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.RegionCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.SampleCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.UntranslatedRegionCriteria;
 import gov.nih.nci.caintegrator.dto.de.ArrayPlatformDE;
 import gov.nih.nci.caintegrator.dto.de.BasePairPositionDE;
 import gov.nih.nci.caintegrator.dto.de.ChromosomeNumberDE;
 import gov.nih.nci.caintegrator.dto.de.CloneIdentifierDE;
 import gov.nih.nci.caintegrator.dto.de.CytobandDE;
 import gov.nih.nci.caintegrator.dto.de.ExprFoldChangeDE;
 import gov.nih.nci.caintegrator.dto.de.GeneIdentifierDE;
 import gov.nih.nci.caintegrator.dto.de.GeneOntologyDE;
 import gov.nih.nci.caintegrator.dto.de.PathwayDE;
 import gov.nih.nci.rembrandt.util.RembrandtConstants;
 import gov.nih.nci.rembrandt.web.bean.ChromosomeBean;
 import gov.nih.nci.rembrandt.web.bean.SessionQueryBag;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.upload.FormFile;
 import org.apache.struts.util.LabelValueBean;
 
 public class GeneExpressionForm extends BaseForm {
 
 	// --------------------------------------------------------- Instance
 	// Variables
 	/** selected chromosomes cytobands **/
 	private List cytobands = new ArrayList();
 		
 	/** chromosomes property */
 	private static List chromosomes;
 
 	/** geneOption property */
 	private String geneOption = "standard";
 
 	/** pathwayName property */
 	private String[] pathwayName;
 	
 
 	/** geneList property */
 	private String geneList;
 
 	/** goClassification property */
 	private String goClassification;
 
 	/** goCellularComp property */
 	private String goCellularComp;
 
 	/** goMolecularFunction property */
 	private String goMolecularFunction;
 
 	/** goBiologicalProcess property */
 	private String goBiologicalProcess;
 
 	/** tumorGrade property */
 	private String tumorGrade;
 
 	/** region property */
 	private String region;
 
 	/** foldChangeValueDown property */
 	private String foldChangeValueDown = "2";
 
 	/** cytobandRegionStart property */
 	private String cytobandRegionStart;
 	
 	/** cytobandRegionEnd property */
 	private String cytobandRegionEnd;
 
 	/** cloneId property */
 	private String cloneId;
 
 	/** pathways property */
 	private String pathways;
 
 	
 
 	/** arrayPlatform property */
 	private String arrayPlatform;
 
 	/** cloneListFile property */
 	private FormFile cloneListFile;
 
 	/** cloneListSpecify property */
 	private String cloneListSpecify;
 
 	/** basePairEnd property */
 	private String basePairEnd;
 
 	/** chromosomeNumber property */
 	private String chromosomeNumber = "";
 
 	/** regulationStatus property */
 	private String regulationStatus;
 
 	/** foldChangeValueUnchangeFrom property */
 	private String foldChangeValueUnchangeFrom = "0.8";
 
 	/** foldChangeValueUnchangeTo property */
 	private String foldChangeValueUnchangeTo = "1.2";
 
 	/** foldChangeValueUp property */
 	private String foldChangeValueUp = "2";
 
 	/** geneType property */
 	private String geneType;
 
 	/** foldChangeValueUDUp property */
 	private String foldChangeValueUDUp;
 
 	/** resultView property */
 	private String resultView;
 
 	/** geneFile property */
 	private FormFile geneFile;
 	
 
 	/** foldChangeValueUDDown property */
 	private String foldChangeValueUDDown = "2";
 
 	/** geneGroup property */
 	private String geneGroup;
 
 	
 	/** cloneList property */
 	private String cloneList;
 
 	/** queryName property */
 	private String queryName;
 
 	/** basePairStart property */
 	private String basePairStart;
 
 	// Collections used for Lookup values.
 	// private ArrayList diseaseType;// moved this to the upperclass:
 	// BaseForm.java
 	// private ArrayList geneTypeColl;// move this to the upperclass:
 	// BaseForm.java
 	private ArrayList cloneTypeColl = new ArrayList();
 
 	private ArrayList arrayPlatformTypeColl = new ArrayList();
 
 	
 
 	private GeneIDCriteria geneCriteria;
 
 	private AllGenesCriteria allGenesCriteria;
 
 	
 
 	private FoldChangeCriteria foldChangeCriteria;
 
 	private RegionCriteria regionCriteria;
 
 	private CloneOrProbeIDCriteria cloneOrProbeIDCriteria;
 
 	private GeneOntologyCriteria geneOntologyCriteria;
 
 	private PathwayCriteria pathwayCriteria;
 
 	private ArrayPlatformCriteria arrayPlatformCriteria;
 
 	// UntranslatedRegionCriteria: for both 5' and 3', "included" is used as
 	// default,
 	// on the jsp, it may be commented out for now
 	private UntranslatedRegionCriteria untranslatedRegionCriteria;		
 
 	//private HttpServletRequest thisRequest;
 
 	private SessionQueryBag queryCollection;
 	
 	private boolean isAllGenes = false;
 
 	private static Logger logger = Logger.getLogger(RembrandtConstants.LOGGER);
 
 	// --------------------------------------------------------- Methods
 	public GeneExpressionForm() {
 
 		// Create Lookups for Gene Expression screens
 		super();
 		setGeneExpressionLookup();
 
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
 
 		// if method is "getCytobands" AND they have upload formFiles, do necessary validation for uploaded files
 		try{
 		    if ((this.getMethod().equalsIgnoreCase("GetCytobands") && this.getGeneGroup().equalsIgnoreCase("Upload"))
 		        || (this.getMethod().equalsIgnoreCase("GetCytobands") && this.getCloneId().equalsIgnoreCase("Upload"))
 		          || (this.getMethod().equalsIgnoreCase("GetCytobands") && this.getSampleGroup().equalsIgnoreCase("Upload"))){
 		    errors = UIFormValidator.validateFormFieldsWithRegion(geneFile, geneGroup, cloneListFile, cloneId, sampleFile, sampleGroup, errors);
 		    if(this.getGeneGroup().equalsIgnoreCase("Upload")){
 		        this.setGeneGroup("");
 		    }
 		    if(this.getCloneId().equalsIgnoreCase("Upload")){
 		        this.setCloneId("");
 		    }
 		    if(this.getSampleGroup().equalsIgnoreCase("Upload")){
 		        this.setSampleGroup("");
 		    }
 		  }
 		}catch(NullPointerException e){
 		    logger.debug("something was set to null");
 		}
 		
 		// if the method of the button is "submit" or "run report", validate
 		if (this.getMethod().equalsIgnoreCase("submit")
 				|| this.getMethod().equalsIgnoreCase("preview")) {
             		    
 			// Query Name cannot be blank
 			errors = UIFormValidator.validateQueryName(queryName, errors);
 			// Chromosomal region validations
 			errors = UIFormValidator.validateChromosomalRegion(chromosomeNumber,
 					region, cytobandRegionStart, basePairStart, basePairEnd, errors);
 			// Validate Go Classification
 			errors = UIFormValidator.validateGOClassification(goClassification,
 					errors);
 			// Validate Gene List, Gene File and Gene Group
 			errors = UIFormValidator.validateTextFileType(sampleFile, 
 			        "sampleGroup", errors);
 			// Make sure the cloneListFile uploaded is of type txt and MIME type
 			// is text/plain
 			errors = UIFormValidator.validateTextFileType(cloneListFile,
 					"cloneId", errors);
 			// Make sure the geneGroup uploaded file is of type txt and MIME
 			// type is text/plain
 			errors = UIFormValidator.validateTextFileType(geneFile,
 					"geneGroup", errors);
 			// Validate CloneId
 			errors = UIFormValidator.validateCloneId(cloneId, cloneListSpecify,
 					cloneListFile, errors);
 
 			// Validate minimum criteria's for GE Query
 			if (this.getQueryName() != null
 					&& this.getQueryName().length() >= 1
 					&& this.getGeneOption().equalsIgnoreCase("standard")) {
 				if ((this.getGeneGroup() == null || this.getGeneGroup().trim()
 						.length() < 1)
 						&& (this.getCloneId() == null || this.getCloneId()
 								.trim().length() < 1)
 						&& (this.getChromosomeNumber() == null || this
 								.getChromosomeNumber().trim().length() < 1)
 						&& (this.getGoClassification() == null || this
 								.getGoClassification().trim().length() < 1)
 						&& (this.getPathways() == null || this.getPathways()
 								.trim().length() < 1)) {
 
 					errors
 							.add(
 									ActionErrors.GLOBAL_ERROR,
 									new ActionError(
 											"gov.nih.nci.nautilus.ui.struts.form.ge.minimum.error"));
 				}
 			}
 		}else {
 			logger.debug("This isn't submit or preview report");
 		}
 
 	
 	 return errors;
 
 	}	
 
 
 	public void setGeneExpressionLookup() {
 
 		// diseaseType = new ArrayList();// moved to the upper class:
 		// BaseForm.java
 		// geneTypeColl = new ArrayList();// moved to the upper class:
 		// BaseForm.java
 		cloneTypeColl = new ArrayList();
 		arrayPlatformTypeColl = new ArrayList();
 
 		// These are hardcoded but will come from DB
 		/*
 		 * *moved to the upperclass:: BaseForm.java
 		 * 
 		 * diseaseType.add( new LabelValueBean( "Astrocytic", "astro" ) );
 		 * diseaseType.add( new LabelValueBean( "Oligodendroglial", "oligo" ) );
 		 * diseaseType.add( new LabelValueBean( "Ependymal cell", "Ependymal
 		 * cell" ) ); diseaseType.add( new LabelValueBean( "Mixed gliomas",
 		 * "Mixed gliomas" ) ); diseaseType.add( new LabelValueBean(
 		 * "Neuroepithelial", "Neuroepithelial" ) ); diseaseType.add( new
 		 * LabelValueBean( "Choroid Plexus", "Choroid Plexus" ) );
 		 * diseaseType.add( new LabelValueBean( "Neuronal and mixed
 		 * neuronal-glial", "neuronal-glial" ) ); diseaseType.add( new
 		 * LabelValueBean( "Pineal Parenchyma", "Pineal Parenchyma" ));
 		 * diseaseType.add( new LabelValueBean( "Embryonal", "Embryonal" ));
 		 * diseaseType.add( new LabelValueBean( "Glioblastoma", "Glioblastoma"
 		 * ));
 		 */
 
 		// geneTypeColl.add( new LabelValueBean( "All Genes", "allgenes" )
 		// );//moved to the upperclass:: BaseForm.java
 		// geneTypeColl.add( new LabelValueBean( "Name/Symbol", "genesymbol" )
 		// );//moved to the upperclass:: BaseForm.java
 		// geneTypeColl.add( new LabelValueBean( "Locus Link Id", "genelocus" )
 		// );//moved to the upperclass:: BaseForm.java
 		// geneTypeColl.add( new LabelValueBean( "GenBank AccNo.", "genbankno" )
 		// );//moved to the upperclass:: BaseForm.java
 		cloneTypeColl.add(new LabelValueBean("IMAGE Id", "imageId"));
 		// cloneTypeColl.add( new LabelValueBean( "BAC Id", "BACId" ) );
 		cloneTypeColl.add(new LabelValueBean("Probe Set Id", "probeSetId"));
 
 		arrayPlatformTypeColl.add(new LabelValueBean("all", "all"));
 		arrayPlatformTypeColl.add(new LabelValueBean("Oligo (Affymetrix)",
 				"Oligo (Affymetrix)"));
 		arrayPlatformTypeColl.add(new LabelValueBean("cDNA", "cDNA"));
 
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
 		// geneOption = "";
 		pathwayName = new String[0];
 		geneList = "";
 		goBiologicalProcess = "";
 		tumorGrade = "";
 		region = "";
 		foldChangeValueDown = "2";
 		cytobandRegionStart = "";
 		cloneId = "";
 		pathways = "";
 		tumorType = "";
 		arrayPlatform = "";
 		cloneListFile = null;
 		goCellularComp = "";
 		goMolecularFunction = "";
 		cloneListSpecify = "";
 		goClassification = "";
 		basePairEnd = "";
 		chromosomeNumber = "";
 		regulationStatus = "";
 		foldChangeValueUnchangeFrom = "0.8";
 		foldChangeValueUnchangeTo = "1.2";
 		foldChangeValueUp = "2";
 		geneType = "";
 		foldChangeValueUDUp = "2";
 		resultView = "";
 		geneFile = null;
 		foldChangeValueUDDown = "2";
 		geneGroup = "";
 		cloneList = "";
 		queryName = "";
 		basePairStart = "";
 		sampleGroup = "";
 		sampleList = "";
 		sampleFile = null;
 
 		// Set the Request Object
 		this.thisRequest = request;		
 		
 		diseaseOrGradeCriteria = new DiseaseOrGradeCriteria();
 		geneCriteria = new GeneIDCriteria();
 		sampleCriteria = new SampleCriteria();
 		foldChangeCriteria = new FoldChangeCriteria();
 		regionCriteria = new RegionCriteria();
 		cloneOrProbeIDCriteria = new CloneOrProbeIDCriteria();
 		geneOntologyCriteria = new GeneOntologyCriteria();
 		pathwayCriteria = new PathwayCriteria();
 		arrayPlatformCriteria = new ArrayPlatformCriteria();
 		allGenesCriteria = new AllGenesCriteria(isAllGenes);
 
 		// arrayPlatformCriteria = new ArrayPlatformCriteria();
 
 	}
 
 	/**
 	 * Returns the geneList.
 	 * 
 	 * @return String
 	 */
 	public String getGeneList() {
 
 		return geneList;
 	}
 
 	/**
 	 * Set the chromosomes Collection
 	 * 
 	 * @param chromosomes
 	 */
 	public void setChromosomes(List chromosomes) {
 		GeneExpressionForm.chromosomes = chromosomes;
 	}
 
 	/**
 	 * Return the chromosomes List
 	 * 
 	 * @param chromosomes
 	 */
 	public List getChromosomes() {
 		return GeneExpressionForm.chromosomes;
 	}
 
 	/**
 	 * Set the geneList.
 	 * 
 	 * @param geneList
 	 *            The geneList to set
 	 */
 	public void setGeneList(String geneList) {
 		this.geneList = geneList;
 		if (thisRequest != null) {
 
 			String thisGeneType = this.thisRequest.getParameter("geneType");
 			String thisGeneGroup = this.thisRequest.getParameter("geneGroup");
             geneCriteria = new GeneIDCriteria();
             GeneIdentifierDE geneIdentifierDE = null;
 			if ((thisGeneGroup != null)
 					&& thisGeneGroup.equalsIgnoreCase("Specify")
 					&& (thisGeneType.length() > 0)
 					&& (this.geneList.length() > 0)) {
 
 				String[] splitValue = this.geneList.split("\\x2C");
 
 				for (int i = 0; i < splitValue.length; i++) {
 
 					if (thisGeneType.equalsIgnoreCase("genesymbol")) {
                         geneIdentifierDE = new GeneIdentifierDE.GeneSymbol(splitValue[i].trim());
 					} else if (thisGeneType.equalsIgnoreCase("genelocus")) {
                         geneIdentifierDE = new GeneIdentifierDE.LocusLink(splitValue[i].trim());
 					} else if (thisGeneType.equalsIgnoreCase("genbankno")) {
                         geneIdentifierDE = new GeneIdentifierDE.GenBankAccessionNumber(splitValue[i].trim());
                         
 					} //else if (thisGeneType.equalsIgnoreCase("allgenes")) {
 						//geneDomainMap.put(splitValue[i].trim(),
 						//		GeneIdentifierDE.GeneSymbol.class.getName());
                         
 					//}
                     geneCriteria.setGeneIdentifier(geneIdentifierDE);
 				}
 			}
 
 			// Set for all genes
 			/*
 			 * if (thisGeneGroup != null &&
 			 * thisGeneGroup.equalsIgnoreCase("Specify") &&
 			 * (thisGeneType.equalsIgnoreCase("allgenes"))){
 			 * geneDomainMap.put("allgenes",
 			 * GeneIdentifierDE.GeneSymbol.class.getName()); }
 			 */
 		}
 	}
 
 	/**
 	 * Sets the geneOption
 	 * 
 	 * @return String
 	 */
 	public void setGeneOption(String geneOption) {
 		this.geneOption = geneOption;
 		if (thisRequest != null) {
 			String thisGeneOption = this.thisRequest.getParameter("geneOption");
 			if (thisGeneOption != null
 					&& thisGeneOption.equalsIgnoreCase("allgenes")) {
 				isAllGenes = true;
 				allGenesCriteria = new AllGenesCriteria(isAllGenes);
 			}
 		}
 	}
 
 	/**
 	 * Returns the geneOption.
 	 * 
 	 * @return String
 	 */
 	public String getGeneOption() {
 		return geneOption;
 	}
 
 	
 	/**
 	 * Returns the geneFile.
 	 * 
 	 * @return String
 	 */
 	public FormFile getGeneFile() {
 		return geneFile;
 	}
 
 	
 
 	/**
 	 * Set the geneFile.
 	 * 
 	 * @param geneFile
 	 *            The geneFile to set
 	 */
 	public void setGeneFile(FormFile geneFile) {
 		this.geneFile = geneFile;
 		if (thisRequest != null) {
 			String thisGeneType = this.thisRequest.getParameter("geneType");
 			String thisGeneGroup = this.thisRequest.getParameter("geneGroup");
 			// retrieve the file name & size
 			String fileName = geneFile.getFileName();
 			int fileSize = geneFile.getFileSize();
 
 			if ((thisGeneGroup != null)
 					&& thisGeneGroup.equalsIgnoreCase("Upload")
 					&& (thisGeneType.length() > 0) && (this.geneFile != null)
 					&& (this.geneFile.getFileName().endsWith(".txt")|| this.geneFile.getFileName().endsWith(".TXT"))
 					&& (this.geneFile.getContentType().equals("text/plain"))) {
 				try {
 					InputStream stream = geneFile.getInputStream();
 					String inputLine = null;
 					BufferedReader inFile = new BufferedReader(
 							new InputStreamReader(stream));
 
 					int count = 0;
                     geneCriteria = new GeneIDCriteria();
                     GeneIdentifierDE geneIdentifierDE = null;
 					while ((inputLine = inFile.readLine()) != null
 							&& count < RembrandtConstants.MAX_FILEFORM_COUNT) {
 						if (UIFormValidator.isAscii(inputLine)) { // make sure
 																	// all data
 																	// is ASCII
 						    inputLine = inputLine.trim();
 						    count++;
                             if (thisGeneType.equalsIgnoreCase("genesymbol")) {
                                 geneIdentifierDE = new GeneIdentifierDE.GeneSymbol(inputLine);
 							} else if (thisGeneType
 									.equalsIgnoreCase("genelocus")) {
                                 geneIdentifierDE = new GeneIdentifierDE.LocusLink(inputLine);
 
 							} else if (thisGeneType
 									.equalsIgnoreCase("genbankno")) {
                                 geneIdentifierDE = new GeneIdentifierDE.GenBankAccessionNumber(inputLine);
 
 							}// else if (thisGeneType
 							//		.equalsIgnoreCase("allgenes")) {
                                 //geneIdentifierDE = new GeneIdentifierDE.LocusLink(inputLine);
 								//geneDomainMap.put(inputLine,
 								//		GeneIdentifierDE.GeneSymbol.class
 								//				.getName());
 							//}
                             geneCriteria.setGeneIdentifier(geneIdentifierDE);
 						}
 					}// end of while
 
 					inFile.close();
 				} catch (IOException ex) {
 					logger.error("Errors when uploading gene file:"
 							+ ex.getMessage());
 				}
 
 			}
 		}
 	}
 
 		public GeneIDCriteria getGeneIDCriteria() {
 		return this.geneCriteria;
 	}
 
 	public AllGenesCriteria getAllGenesCriteria() {
 		return this.allGenesCriteria;
 	}
 
 	
 
 	public FoldChangeCriteria getFoldChangeCriteria() {
 		return this.foldChangeCriteria;
 	}
 
 	public RegionCriteria getRegionCriteria() {
 		return this.regionCriteria;
 	}
 
 	
 
 	public CloneOrProbeIDCriteria getCloneOrProbeIDCriteria() {
 		return this.cloneOrProbeIDCriteria;
 	}
 
 	public GeneOntologyCriteria getGeneOntologyCriteria() {
 		return this.geneOntologyCriteria;
 	}
 
 	public PathwayCriteria getPathwayCriteria() {
 		return this.pathwayCriteria;
 	}
 
 	public ArrayPlatformCriteria getArrayPlatformCriteria() {
 		return this.arrayPlatformCriteria;
 	}
 
 	/**
 	 * Returns the goBiologicalProcess.
 	 * 
 	 * @return String
 	 */
 	public String getGoBiologicalProcess() {
 		return goBiologicalProcess;
 	}
 
 	/**
 	 * Set the goBiologicalProcess.
 	 * 
 	 * @param goBiologicalProcess
 	 *            The goBiologicalProcess to set
 	 */
 	public void setGoBiologicalProcess(String goBiologicalProcess) {
 		this.goBiologicalProcess = goBiologicalProcess;
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
 	 * @param tumorGrade
 	 *            The tumorGrade to set
 	 */
 	public void setTumorGrade(String tumorGrade) {
 		this.tumorGrade = tumorGrade;
 	}
 
 	/**
 	 * Returns the region.
 	 * 
 	 * @return String
 	 */
 	public String getRegion() {
 		return region;
 	}
 
 	/**
 	 * Set the region.
 	 * 
 	 * @param region
 	 *            The region to set
 	 */
 	public void setRegion(String region) {
 		this.region = region;
 	}
 
 	/**
 	 * Returns the foldChangeValueDown.
 	 * 
 	 * @return String
 	 */
 	public String getFoldChangeValueDown() {
 		return foldChangeValueDown;
 	}
 
 	/**
 	 * Set the foldChangeValueDown.
 	 * 
 	 * @param foldChangeValueDown
 	 *            The foldChangeValueDown to set
 	 */
 	public void setFoldChangeValueDown(String foldChangeValueDown) {
 		this.foldChangeValueDown = foldChangeValueDown;
 		if (thisRequest != null) {
 			String thisRegulationStatus = this.thisRequest
 					.getParameter("regulationStatus");
 			
 			if (thisRegulationStatus != null
 					&& thisRegulationStatus.equalsIgnoreCase("down")
 					&& (this.foldChangeValueDown.length() > 0)){
				    ExprFoldChangeDE foldChangeDEObj = new ExprFoldChangeDE.DownRegulation(Float.valueOf(this.foldChangeValueDown));
 				    foldChangeCriteria.setFoldChangeObject(foldChangeDEObj);
 
 				
 			}
 		}
 
 	}
 
 	/**
 	 * Returns the cytobandRegion.
 	 * 
 	 * @return String
 	 */
 	public String getCytobandRegionStart() {
 		return this.cytobandRegionStart;
 	}
 
 	/**
 	 * Set the cytobandRegion.
 	 * 
 	 * @param cytobandRegion
 	 *            The cytobandRegion to set
 	 */
 	public void setCytobandRegionStart(String cytobandRegionStart) {
 		this.cytobandRegionStart = cytobandRegionStart;
 		if (thisRequest != null) {
 			String thisRegion = this.thisRequest.getParameter("region");
 			String thisChrNumber = this.thisRequest
 					.getParameter("chromosomeNumber");
 
 			if (thisChrNumber != null && thisChrNumber.trim().length() > 0) {
 
 				if (thisRegion != null
 						&& thisRegion.equalsIgnoreCase("cytoband")
 						&& this.cytobandRegionStart.trim().length() > 0) {
 					if(regionCriteria == null){
 						regionCriteria = new RegionCriteria();
 					}
 					CytobandDE cytobandDE = new CytobandDE(this.cytobandRegionStart);
 					regionCriteria.setStartCytoband(cytobandDE);
 					
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * Returns the cloneId.
 	 * 
 	 * @return String
 	 */
 	public String getCloneId() {
 		return cloneId;
 	}
 
 	/**
 	 * Set the cloneId.
 	 * 
 	 * @param cloneId
 	 *            The cloneId to set
 	 */
 	public void setCloneId(String cloneId) {
 		this.cloneId = cloneId;
 	}
 
 	/**
 	 * Returns the pathways.
 	 * 
 	 * @return String
 	 */
 	public String getPathways() {
 		return pathways;
 	}
 
 	/**
 	 * Set the pathways.
 	 * 
 	 * @param pathways
 	 *            The pathways to set
 	 */
 	public void setPathways(String pathways) {
 
 		this.pathways = pathways.trim();
 
 		if (this.pathways != null && this.pathways.length() > 0) {
 			String[] splitValue = this.pathways.split("\\r\\n");
 			pathwayCriteria = new PathwayCriteria();
 			for (int i = 0; i < splitValue.length; i++) {
 				PathwayDE pathwayDEObj = new PathwayDE(splitValue[i]);
 				pathwayCriteria.setPathwayName(pathwayDEObj);				
 			}
 		}
 	}
 
 	
 	
 	/**
 	 * Returns the arrayPlatform.
 	 * 
 	 * @return String
 	 */
 	public String getArrayPlatform() {
 		return arrayPlatform;
 	}
 
 	/**
 	 * Set the arrayPlatform.
 	 * 
 	 * @param arrayPlatform
 	 *            The arrayPlatform to set
 	 */
 	public void setArrayPlatform(String arrayPlatform) {
 		this.arrayPlatform = arrayPlatform;
 		if(this.arrayPlatform  != null){
 			ArrayPlatformDE arrayPlatformDEObj = new ArrayPlatformDE(this.arrayPlatform);
 			arrayPlatformCriteria = new ArrayPlatformCriteria();
 			arrayPlatformCriteria.setPlatform(arrayPlatformDEObj);
 		}	
 		
 	}
 
 	/**
 	 * Returns the goCellularComp.
 	 * 
 	 * @return String
 	 */
 	public String getGoCellularComp() {
 		return goCellularComp;
 	}
 
 	/**
 	 * Set the goCellularComp.
 	 * 
 	 * @param goCellularComp
 	 *            The goCellularComp to set
 	 */
 	public void setGoCellularComp(String goCellularComp) {
 		this.goCellularComp = goCellularComp;
 	}
 
 	/**
 	 * Returns the goMolecularFunction.
 	 * 
 	 * @return String
 	 */
 	public String getGoMolecularFunction() {
 		return goMolecularFunction;
 	}
 
 	/**
 	 * Set the goMolecularFunction.
 	 * 
 	 * @param goMolecularFunction
 	 *            The goMolecularFunction to set
 	 */
 	public void setGoMolecularFunction(String goMolecularFunction) {
 		this.goMolecularFunction = goMolecularFunction;
 	}
 
 	/**
 	 * Returns the cloneListSpecify.
 	 * 
 	 * @return String
 	 */
 	public String getCloneListSpecify() {
 		return cloneListSpecify;
 	}
 
 	/**
 	 * Set the cloneListSpecify.
 	 * 
 	 * @param cloneListSpecify
 	 *            The cloneListSpecify to set
 	 */
 	public void setCloneListSpecify(String cloneListSpecify) {
 		this.cloneListSpecify = cloneListSpecify;
 		if (thisRequest != null) {
 			// this is to check if the radio button is selected for the clone
 			// category
 			String thisCloneId = (String) thisRequest.getParameter("cloneId");
 
 			// this is to check the type of the clone
 			String thisCloneList = (String) thisRequest
 					.getParameter("cloneList");
 
 			if (thisCloneId != null && thisCloneList != null
 					&& !thisCloneList.equals("")) {
 				if (this.cloneListSpecify != null
 						&& !cloneListSpecify.equals("")) {
 					cloneOrProbeIDCriteria = new CloneOrProbeIDCriteria();
 					CloneIdentifierDE cloneIdentfierDEObj = null;
 					String[] cloneStr = cloneListSpecify.split("\\x2C");
 					for (int i = 0; i < cloneStr.length; i++) {
 						if (thisCloneList.equalsIgnoreCase("imageId")) {
 							cloneIdentfierDEObj = new CloneIdentifierDE.IMAGEClone(cloneStr[i].trim());							
 						} 
 						else if (thisCloneList.equalsIgnoreCase("BACId")) {
 							cloneIdentfierDEObj = new CloneIdentifierDE.BACClone(cloneStr[i].trim());
 						} 
 						else if (thisCloneList.equalsIgnoreCase("probeSetId")) {
 							cloneIdentfierDEObj = new CloneIdentifierDE.ProbesetID(cloneStr[i].trim());
 						}
 						
 					cloneOrProbeIDCriteria.setCloneIdentifier(cloneIdentfierDEObj);
 					} // end of for loop
 				}// end of if(this.cloneListSpecify != null &&
 				// !cloneListSpecify.equals("")){
 
 			}
 		}
 	}
 
 	/**
 	 * Returns the cloneListFile.
 	 * 
 	 * @return String
 	 */
 	public FormFile getCloneListFile() {
 		return cloneListFile;
 	}
 
 	/**
 	 * Set the cloneListFile.
 	 * 
 	 * @param cloneListFile
 	 *            The cloneListFile to set
 	 */
 	public void setCloneListFile(FormFile cloneListFile) {
 		this.cloneListFile = cloneListFile;
 		if (thisRequest != null) {
 			// this is to check if the radio button is selected for the clone
 			// category
 			String thisCloneId = (String) thisRequest.getParameter("cloneId");
 			// this is to check the type of the clone
 			String thisCloneList = (String) thisRequest
 					.getParameter("cloneList");
 
 			//
 			// retrieve the file name & size
 			String fileName = cloneListFile.getFileName();
 			int fileSize = cloneListFile.getFileSize();
 
 			if ((thisCloneId != null)
 					&& thisCloneId.equalsIgnoreCase("Upload")
 					&& (thisCloneList.length() > 0)
 					&& (this.cloneListFile != null)
 					&& (this.cloneListFile.getFileName().endsWith(".txt") || this.cloneListFile.getFileName().endsWith(".TXT"))
 					&& (this.getCloneListFile().getContentType()
 							.equals("text/plain"))) {
 
 				try {
 					InputStream stream = cloneListFile.getInputStream();
 					String inputLine = null;
 					BufferedReader inFile = new BufferedReader(
 							new InputStreamReader(stream));
 					int count = 0;
 					cloneOrProbeIDCriteria = new CloneOrProbeIDCriteria();
 					CloneIdentifierDE cloneIdentfierDEObj = null;
 					while ((inputLine = inFile.readLine()) != null
 							&& count < RembrandtConstants.MAX_FILEFORM_COUNT) {
 						if (UIFormValidator.isAscii(inputLine)) { // make sure
 																	// all data
 																	// is ASCII
 						    inputLine = inputLine.trim();
 						    count++; // increment
 							if (thisCloneList.equalsIgnoreCase("IMAGEId")) {
 								cloneIdentfierDEObj = new CloneIdentifierDE.IMAGEClone(inputLine);								
 								
 							} 
 							else if (thisCloneList.equalsIgnoreCase("BACId")) {
 								cloneIdentfierDEObj = new CloneIdentifierDE.BACClone(inputLine);
 							} 
 							else if (thisCloneList
 									.equalsIgnoreCase("probeSetId")) {
 								cloneIdentfierDEObj = new CloneIdentifierDE.ProbesetID(inputLine);	
 								
 							}
 						}
 						cloneOrProbeIDCriteria.setCloneIdentifier(cloneIdentfierDEObj);
 					}// end of while
 
 					inFile.close();
 				} catch (IOException ex) {
 					logger.error("Errors when uploading clone/probeset file:"
 							+ ex.getMessage());
 				}
 			}
 		}
 	}
 
 	/**
 	 * Returns the goClassification.
 	 * 
 	 * @return String
 	 */
 	public String getGoClassification() {
 		return goClassification;
 	}
 
 	/**
 	 * Set the goClassification.
 	 * 
 	 * @param goClassification
 	 *            The goClassification to set
 	 */
 	public void setGoClassification(String goClassification) {
 		this.goClassification = goClassification;
 		String goSelect = null;
 		if (thisRequest != null) {
 			goSelect = (String) thisRequest.getParameter("goClassification");
 		}
 		if (goSelect != null && !goSelect.equals("")) {
 			geneOntologyCriteria = new GeneOntologyCriteria();
 			GeneOntologyDE geneOntologyDEObj = new GeneOntologyDE(this.goClassification);
 			geneOntologyCriteria.setGOIdentifier(geneOntologyDEObj);		
 		}
 
 	}
 
 	/**
 	 * Returns the basePairEnd.
 	 * 
 	 * @return String
 	 */
 	public String getBasePairEnd() {
 		return basePairEnd;
 	}
 
 	/**
 	 * Set the basePairEnd.
 	 * 
 	 * @param basePairEnd
 	 *            The basePairEnd to set
 	 */
 	public void setBasePairEnd(String basePairEnd) {
 		this.basePairEnd = basePairEnd.trim();
 
 		if (thisRequest != null) {
 			String thisRegion = this.thisRequest.getParameter("region");
 			String thisChrNumber = this.thisRequest
 					.getParameter("chromosomeNumber");
 			String thisBasePairStart = this.thisRequest
 					.getParameter("basePairStart");
 
 			if (thisChrNumber != null && thisChrNumber.trim().length() > 0) {
 				if (thisRegion != null && thisBasePairStart != null
 						&& this.basePairEnd != null) {
 					if ((thisRegion.equalsIgnoreCase("basePairPosition"))
 							&& (thisBasePairStart.trim().length() > 0)
 							&& (this.basePairEnd.trim().length() > 0)) {
 						if(regionCriteria == null){
 							regionCriteria = new RegionCriteria();
 						}
 						BasePairPositionDE.EndPosition basePairEndDE = new BasePairPositionDE.EndPosition(new Long(this.basePairEnd));
 						regionCriteria.setEnd(basePairEndDE);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Returns the chromosomeNumber.
 	 * 
 	 * @return String
 	 */
 	public String getChromosomeNumber() {
 		return chromosomeNumber;
 	}
 
 	/**
 	 * Set the chromosomeNumber.
 	 * 
 	 * @param chromosomeNumber
 	 *            The chromosomeNumber to set
 	 */
 	public void setChromosomeNumber(String chromosomeIndex) {
 		//IMPORTANT! The chromosomeNumber is actually the
 		//index into the chromosome List where the selected
 		//chromosome can be found.  It is NOT the actual chromosome
 		//number.  Chromosome numbers can actually be characters, like
 		// X and Y so we 
 		this.chromosomeNumber = chromosomeIndex;
 		if(!"".equals(chromosomeIndex)) {
 			//Get the chromosome from the Chromosome List
 			try {
 				ChromosomeBean bean = (ChromosomeBean)chromosomes.get(Integer.parseInt(chromosomeIndex));
 				String chromosomeName = bean.getChromosome();
 				if(regionCriteria == null){
 					regionCriteria = new RegionCriteria();
 				}
 				ChromosomeNumberDE chromosomeDE = new  ChromosomeNumberDE(chromosomeName);
 				regionCriteria.setChromNumber(chromosomeDE);
 				logger.debug("Test Chromosome Criteria "+ regionCriteria.getChromNumber().getValue());
 
 			}catch(NumberFormatException nfe) {
 				logger.error("Expected an Integer index for chromosome, got a char or string");
 				logger.error(nfe);
 			}
 		}
 
 	}
 
 	/**
 	 * Returns the regulationStatus.
 	 * 
 	 * @return String
 	 */
 	public String getRegulationStatus() {
 		return regulationStatus;
 	}
 
 	/**
 	 * Set the regulationStatus.
 	 * 
 	 * @param regulationStatus
 	 *            The regulationStatus to set
 	 */
 	public void setRegulationStatus(String regulationStatus) {
 		this.regulationStatus = regulationStatus;
 	}
 
 	/**
 	 * Returns the foldChangeValueUnchange.
 	 * 
 	 * @return String
 	 */
 	public String getFoldChangeValueUnchangeFrom() {
 		return foldChangeValueUnchangeFrom;
 	}
 
 	/**
 	 * Set the foldChangeValueUnchange.
 	 * 
 	 * @param foldChangeValueUnchange
 	 *            The foldChangeValueUnchange to set
 	 */
 	public void setFoldChangeValueUnchangeFrom(
 			String foldChangeValueUnchangeFrom) {
 		this.foldChangeValueUnchangeFrom = foldChangeValueUnchangeFrom;
 		if (thisRequest != null) {
 			String thisRegulationStatus = this.thisRequest
 					.getParameter("regulationStatus");
 			
 			if (thisRegulationStatus != null
 					&& thisRegulationStatus.equalsIgnoreCase("unchange")
 					&& (this.foldChangeValueUnchangeFrom.length() > 0)){
 			   ExprFoldChangeDE foldChangeDEObj = new ExprFoldChangeDE.UnChangedRegulationDownLimit(Float.valueOf(this.foldChangeValueUnchangeFrom));
 			   foldChangeCriteria.setFoldChangeObject(foldChangeDEObj);		
 				
 
 			
 			}
 		}
 	}
 
 	/**
 	 * Returns the foldChangeValueUp.
 	 * 
 	 * @return String
 	 */
 	/**
 	 * Returns the foldChangeValueUnchange.
 	 * 
 	 * @return String
 	 */
 	public String getFoldChangeValueUnchangeTo() {
 		return foldChangeValueUnchangeTo;
 	}
 
 	/**
 	 * Set the foldChangeValueUnchange.
 	 * 
 	 * @param foldChangeValueUnchange
 	 *            The foldChangeValueUnchange to set
 	 */
 	public void setFoldChangeValueUnchangeTo(String foldChangeValueUnchangeTo) {
 		this.foldChangeValueUnchangeTo = foldChangeValueUnchangeTo;
 		if (thisRequest != null) {
 			String thisRegulationStatus = this.thisRequest
 					.getParameter("regulationStatus");
 			
 			if (thisRegulationStatus != null
 					&& thisRegulationStatus.equalsIgnoreCase("unchange")
 					&& (this.foldChangeValueUnchangeTo.length() > 0)) {
 				    ExprFoldChangeDE foldChangeDEObj = new ExprFoldChangeDE.UnChangedRegulationUpperLimit(Float.valueOf(this.foldChangeValueUnchangeTo));
 				    foldChangeCriteria.setFoldChangeObject(foldChangeDEObj);
 
 				/*foldUpDomainMap.put(this.foldChangeValueUnchangeTo,
 						ExprFoldChangeDE.UnChangedRegulationUpperLimit.class
 								.getName());*/
 			}
 		}
 	}
 
 	/**
 	 * Returns the foldChangeValueUp.
 	 * 
 	 * @return String
 	 */
 	public String getFoldChangeValueUp() {
 		return foldChangeValueUp;
 	}
 
 	/**
 	 * Set the foldChangeValueUp.
 	 * 
 	 * @param foldChangeValueUp
 	 *            The foldChangeValueUp to set
 	 */
 	public void setFoldChangeValueUp(String foldChangeValueUp) {
 		this.foldChangeValueUp = foldChangeValueUp;
 		logger.debug("I am in the setFoldChangeValueUp() method");
 		if (thisRequest != null) {
 			String thisRegulationStatus = this.thisRequest
 					.getParameter("regulationStatus");
 			
 
 			if (thisRegulationStatus != null
 					&& thisRegulationStatus.equalsIgnoreCase("up")
 					&& (this.foldChangeValueUp.length() > 0)){
 				   ExprFoldChangeDE foldChangeDEObj = new ExprFoldChangeDE.UpRegulation(Float.valueOf(this.foldChangeValueUp));
 				   foldChangeCriteria.setFoldChangeObject(foldChangeDEObj);						
 			}
 
 				
 		}
 	}
 
 	/**
 	 * Returns the geneType.
 	 * 
 	 * @return String
 	 */
 	public String getGeneType() {
 		return geneType;
 	}
 
 	/**
 	 * Set the geneType.
 	 * 
 	 * @param geneType
 	 *            The geneType to set
 	 */
 	public void setGeneType(String geneType) {
 		this.geneType = geneType;
 
 	}
 
 	/**
 	 * Returns the foldChangeValueUDUp.
 	 * 
 	 * @return String
 	 */
 	public String getFoldChangeValueUDUp() {
 		return foldChangeValueUDUp;
 	}
 
 	/**
 	 * Set the foldChangeValueUDUp.
 	 * 
 	 * @param foldChangeValueUDUp
 	 *            The foldChangeValueUDUp to set
 	 */
 	public void setFoldChangeValueUDUp(String foldChangeValueUDUp) {
 		this.foldChangeValueUDUp = foldChangeValueUDUp;
 		if (thisRequest != null) {
 			String thisRegulationStatus = this.thisRequest
 					.getParameter("regulationStatus");
 			logger
 					.debug("I am in the setFoldChangeValueUDUp()  thisRegulationStatus:"
 							+ thisRegulationStatus);
 			
 			if (thisRegulationStatus != null
 					&& thisRegulationStatus.equalsIgnoreCase("updown")
 					&& (this.foldChangeValueUDUp.length() > 0)) {
 				   ExprFoldChangeDE foldChangeDEObj = new ExprFoldChangeDE.UpRegulation(Float.valueOf(this.foldChangeValueUDUp));
 				   foldChangeCriteria.setFoldChangeObject(foldChangeDEObj);	
 			
 			}
 		}
 	}
 
 	/**
 	 * Returns the foldChangeValueUDDown.
 	 * 
 	 * @return String
 	 */
 	public String getFoldChangeValueUDDown() {
 		return foldChangeValueUDDown;
 	}
 
 	/**
 	 * Set the foldChangeValueUDDown.
 	 * 
 	 * @param foldChangeValueUDDown
 	 *            The foldChangeValueUDDown to set
 	 */
 	public void setFoldChangeValueUDDown(String foldChangeValueUDDown) {
 		this.foldChangeValueUDDown = foldChangeValueUDDown;
 		if (thisRequest != null) {
 			String thisRegulationStatus = this.thisRequest
 					.getParameter("regulationStatus");
 			logger.debug("I am in the setFoldChangeValueUDDown() methid: "
 					+ thisRegulationStatus);
 			
 			if (thisRegulationStatus != null
 					&& thisRegulationStatus.equalsIgnoreCase("updown")
 					&& (this.foldChangeValueUDDown.length() > 0)){
 				    ExprFoldChangeDE foldChangeDEObj = new ExprFoldChangeDE.DownRegulation(Float.valueOf(this.foldChangeValueUDDown));
 				    foldChangeCriteria.setFoldChangeObject(foldChangeDEObj);
 			}
 				
 		}
 
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
 	public String getGeneGroup() {
 		return geneGroup;
 	}
 
 	/**
 	 * Set the geneGroup.
 	 * 
 	 * @param geneGroup
 	 *            The geneGroup to set
 	 */
 	public void setGeneGroup(String geneGroup) {
 		this.geneGroup = geneGroup;
 	}
 
 	
 
 	
 
 	/**
 	 * Returns the cloneList.
 	 * 
 	 * @return String
 	 */
 	public String getCloneList() {
 		return cloneList;
 	}
 
 	/**
 	 * Set the cloneList.
 	 * 
 	 * @param cloneList
 	 *            The cloneList to set
 	 */
 	public void setCloneList(String cloneList) {
 		this.cloneList = cloneList;
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
 	 * Set the queryName.
 	 * 
 	 * @param queryName
 	 *            The queryName to set
 	 */
 	public void setQueryName(String queryName) {
 		this.queryName = queryName;
 	}
 
 	/**
 	 * Returns the basePairStart.
 	 * 
 	 * @return String
 	 */
 	public String getBasePairStart() {
 		return basePairStart;
 	}
 
 	/**
 	 * Set the basePairStart.
 	 * 
 	 * @param basePairStart
 	 *            The basePairStart to set
 	 */
 	public void setBasePairStart(String basePairStart) {
 		this.basePairStart = basePairStart.trim();
 		if (thisRequest != null) {
 			String thisRegion = this.thisRequest.getParameter("region");
 			String thisChrNumber = this.thisRequest
 					.getParameter("chromosomeNumber");
 			String thisBasePairEnd = this.thisRequest
 					.getParameter("basePairEnd");
 
 			if (thisChrNumber != null && thisChrNumber.trim().length() > 0) {
 				if (thisRegion != null && this.basePairStart != null
 						&& thisBasePairEnd != null) {
 					if ((thisRegion.equalsIgnoreCase("basePairPosition"))
 							&& (thisBasePairEnd.trim().length() > 0)
 							&& (this.basePairStart.trim().length() > 0)) {
 						if(regionCriteria == null){
 							regionCriteria = new RegionCriteria();
 						}
 						BasePairPositionDE.StartPosition basePairStartDE = new BasePairPositionDE.StartPosition(new Long(this.basePairStart));
 						regionCriteria.setStart(basePairStartDE);
 					}
 				}
 			}
 		}
 
 	}
 
 	public ArrayList getCloneTypeColl() {
 		return cloneTypeColl;
 	}
 
 	public void setQueryCollection(SessionQueryBag queryCollection) {
 		this.queryCollection = queryCollection;
 	}
 
 	public SessionQueryBag getQueryCollection() {
 		return this.queryCollection;
 	}
 
 	public String[] getPathwayName() {
 		return pathwayName;
 	}
 
 	public void setPathwayName(String[] pathwayName) {
 		this.pathwayName = pathwayName;
 	}
 	
 	public boolean getIsAllGenes(){
 	    return this.isAllGenes;
 	}
 
 	public GeneExpressionForm cloneMe() {
 		GeneExpressionForm form = new GeneExpressionForm();
 		form.setPathwayName(pathwayName);
 		form.setGeneList(geneList);
 		form.setSampleList(sampleList);
 		form.setGoClassification(goClassification);
 		form.setGoCellularComp(goCellularComp);
 		form.setGoMolecularFunction(goMolecularFunction);
 		form.setGoCellularComp(goBiologicalProcess);
 		form.setTumorGrade(tumorGrade);
 		form.setRegion(region);
 		form.setFoldChangeValueDown(foldChangeValueDown);
 		form.setCytobandRegionStart(cytobandRegionStart);
 		form.setCytobandRegionEnd(cytobandRegionEnd);
 		form.setCloneId(cloneId);
 		form.setPathways(pathways);
 		form.setTumorType(tumorType);
 		form.setArrayPlatform(arrayPlatform);
 		form.setCloneListFile(cloneListFile);
 		form.setCloneListSpecify(cloneListSpecify);
 		form.setBasePairEnd(basePairEnd);
 		form.setChromosomeNumber(chromosomeNumber);
 		form.setRegulationStatus(regulationStatus);
 		form.setFoldChangeValueUnchangeFrom(foldChangeValueUnchangeFrom);
 		form.setFoldChangeValueUnchangeTo(foldChangeValueUnchangeTo);
 		form.setFoldChangeValueUp(foldChangeValueUp);
 		form.setGeneType(geneType);
 		form.setFoldChangeValueUDUp(foldChangeValueUDUp);
 		form.setResultView(resultView);
 		form.setGeneFile(geneFile);
 		form.setSampleFile(sampleFile);
 		form.setFoldChangeValueUDDown(foldChangeValueUDDown);
 		form.setGeneGroup(geneGroup);
 		form.setSampleGroup(sampleGroup);
 		form.setCloneList(cloneList);
 		form.setQueryName(queryName);
 		form.setBasePairStart(basePairStart);
 		form.setQueryCollection(queryCollection);
 		/*
 		 * form.setCloneTypeColl(cloneTypeColl);
 		 * form.setArrayPlatformTypeColl(arrayPlatformTypeColl);
 		 * form.setDiseaseOrGradeCriteria(diseaseOrGradeCriteria);
 		 * form.setGeneCriteria(geneCriteria);
 		 * form.setFoldChangeCriteria(foldChangeCriteria);
 		 * form.setRegionCriteria(regionCriteria);
 		 * form.setCloneOrProbeIDCriteria(cloneOrProbeIDCriteria);
 		 * form.setGeneOntologyCriteria(geneOntologyCriteria);
 		 * form.setPathwayCriteria(pathwayCriteria);
 		 * form.setArrayPlatform(arrayPlatformCriteria);
 		 * form.setUntranslatedRegionCriteria(untranslatedRegionCriteria);
 		 * form.setDiseaseDomainMap(diseaseDomainMap);
 		 * form.setGeneDomainMap(geneDomainMap);
 		 * form.setFoldUpDomainMap(foldUpDomainMap);
 		 * form.setFoldDownDomainMap(foldDownDomainMap);
 		 * form.setRegionDomainMap(regionDomainMap);
 		 * form.setCloneDomainMap(cloneDomainMap);
 		 * form.setGeneOntologyDomainMap(geneOntologyDomainMap);
 		 * form.setPathwayDomainMap(pathwayDomainMap);
 		 * form.setArrayPlatformDomainMap(arrayPlatformDomainMap);
 		 */
 
 		return form;
 	}
 
 	/**
 	 * @return Returns the cytobands.
 	 */
 	public List getCytobands() {
 		//Check to make sure that if we have a chromosome selected
 		//that we also have it's associated cytobands
 		if(!"".equals(chromosomeNumber)) {
 			cytobands = ((ChromosomeBean)(chromosomes.get(Integer.parseInt(chromosomeNumber)))).getCytobands();
 		}
 		return cytobands;
 	}
 	/**
 	 * @param cytobands The cytobands to set.
 	 */
 	public void setCytobands(List cytobands) {
 		this.cytobands = cytobands;
 	}
     /**
      * @return Returns the cytobandRegionEnd.
      */
     public String getCytobandRegionEnd() {
         return cytobandRegionEnd;
     }
     /**
      * @param cytobandRegionEnd The cytobandRegionEnd to set.
      */
     public void setCytobandRegionEnd(String cytobandRegionEnd) {
         this.cytobandRegionEnd = cytobandRegionEnd;
 		if (thisRequest != null) {
 			String thisRegion2 = this.thisRequest.getParameter("region");
 			String thisChrNumber2 = this.thisRequest
 					.getParameter("chromosomeNumber");
 
 			if (thisChrNumber2 != null && thisChrNumber2.trim().length() > 0) {
 
 				if (thisRegion2 != null
 						&& thisRegion2.equalsIgnoreCase("cytoband")
 						&& this.cytobandRegionEnd.trim().length() > 0) {
 					if(regionCriteria == null){
 						regionCriteria = new RegionCriteria();
 					}
 					CytobandDE cytobandDE = new CytobandDE(this.cytobandRegionEnd);
 					regionCriteria.setEndCytoband(cytobandDE);
 				}
 			}
 		}
 		
 
     }
 }
