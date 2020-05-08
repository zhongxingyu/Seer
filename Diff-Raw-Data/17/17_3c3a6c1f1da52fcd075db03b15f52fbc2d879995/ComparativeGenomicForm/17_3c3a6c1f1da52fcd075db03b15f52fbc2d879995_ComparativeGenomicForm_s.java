 /*L
  * Copyright (c) 2006 SAIC, SAIC-F.
  *
  * Distributed under the OSI-approved BSD 3-Clause License.
  * See http://ncip.github.com/rembrandt/LICENSE.txt for details.
  */
 
 package gov.nih.nci.rembrandt.web.struts2.form;
 
 import gov.nih.nci.caintegrator.application.util.ApplicationContext;
 import gov.nih.nci.caintegrator.dto.critieria.AllGenesCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.AlleleFrequencyCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.AnalysisTypeCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.AssayPlatformCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.CloneOrProbeIDCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.CopyNumberCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.SegmentMeanCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.DiseaseOrGradeCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.GeneIDCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.RegionCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.SNPCriteria;
 import gov.nih.nci.caintegrator.dto.critieria.SampleCriteria;
 import gov.nih.nci.caintegrator.dto.de.AlleleFrequencyDE;
 import gov.nih.nci.caintegrator.dto.de.AssayPlatformDE;
 import gov.nih.nci.caintegrator.dto.de.BasePairPositionDE;
 import gov.nih.nci.caintegrator.dto.de.ChromosomeNumberDE;
 import gov.nih.nci.caintegrator.dto.de.CloneIdentifierDE;
 import gov.nih.nci.caintegrator.dto.de.CopyNumberDE;
 import gov.nih.nci.caintegrator.dto.de.SNPableDE;
 import gov.nih.nci.caintegrator.dto.de.SegmentMeanDE;
 import gov.nih.nci.caintegrator.dto.de.CytobandDE;
 import gov.nih.nci.caintegrator.dto.de.GeneIdentifierDE;
 import gov.nih.nci.caintegrator.dto.de.SNPIdentifierDE;
 import gov.nih.nci.caintegrator.dto.de.SampleIDDE;
 import gov.nih.nci.caintegrator.enumeration.AnalysisType;
 import gov.nih.nci.caintegrator.enumeration.ArrayPlatformType;
 import gov.nih.nci.caintegrator.enumeration.SpecimenType;
 import gov.nih.nci.caintegrator.util.MathUtil;
 import gov.nih.nci.rembrandt.util.MoreStringUtils;
 import gov.nih.nci.rembrandt.util.RembrandtConstants;
 import gov.nih.nci.rembrandt.web.bean.ChromosomeBean;
 import gov.nih.nci.rembrandt.web.helper.GroupRetriever;
 
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 //import org.apache.struts.action.ActionError;
 //import org.apache.struts.action.ActionErrors;
 //import org.apache.struts.action.ActionMapping;
 //import org.apache.struts.upload.FormFile;
 import org.apache.struts.util.LabelValueBean;
 
 
 
 /**
 * caIntegrator License
 * 
 * Copyright 2001-2005 Science Applications International Corporation ("SAIC"). 
 * The software subject to this notice and license includes both human readable source code form and machine readable, 
 * binary, object code form ("the caIntegrator Software"). The caIntegrator Software was developed in conjunction with 
 * the National Cancer Institute ("NCI") by NCI employees and employees of SAIC. 
 * To the extent government employees are authors, any rights in such works shall be subject to Title 17 of the United States
 * Code, section 105. 
 * This caIntegrator Software License (the "License") is between NCI and You. "You (or "Your") shall mean a person or an 
 * entity, and all other entities that control, are controlled by, or are under common control with the entity. "Control" 
 * for purposes of this definition means (i) the direct or indirect power to cause the direction or management of such entity,
 *  whether by contract or otherwise, or (ii) ownership of fifty percent (50%) or more of the outstanding shares, or (iii) 
 * beneficial ownership of such entity. 
 * This License is granted provided that You agree to the conditions described below. NCI grants You a non-exclusive, 
 * worldwide, perpetual, fully-paid-up, no-charge, irrevocable, transferable and royalty-free right and license in its rights 
 * in the caIntegrator Software to (i) use, install, access, operate, execute, copy, modify, translate, market, publicly 
 * display, publicly perform, and prepare derivative works of the caIntegrator Software; (ii) distribute and have distributed 
 * to and by third parties the caIntegrator Software and any modifications and derivative works thereof; 
 * and (iii) sublicense the foregoing rights set out in (i) and (ii) to third parties, including the right to license such 
 * rights to further third parties. For sake of clarity, and not by way of limitation, NCI shall have no right of accounting
 * or right of payment from You or Your sublicensees for the rights granted under this License. This License is granted at no
 * charge to You. 
 * 1. Your redistributions of the source code for the Software must retain the above copyright notice, this list of conditions
 *    and the disclaimer and limitation of liability of Article 6, below. Your redistributions in object code form must reproduce 
 *    the above copyright notice, this list of conditions and the disclaimer of Article 6 in the documentation and/or other materials
 *    provided with the distribution, if any. 
 * 2. Your end-user documentation included with the redistribution, if any, must include the following acknowledgment: "This 
 *    product includes software developed by SAIC and the National Cancer Institute." If You do not include such end-user 
 *    documentation, You shall include this acknowledgment in the Software itself, wherever such third-party acknowledgments 
 *    normally appear.
 * 3. You may not use the names "The National Cancer Institute", "NCI" "Science Applications International Corporation" and 
 *    "SAIC" to endorse or promote products derived from this Software. This License does not authorize You to use any 
 *    trademarks, service marks, trade names, logos or product names of either NCI or SAIC, except as required to comply with
 *    the terms of this License. 
 * 4. For sake of clarity, and not by way of limitation, You may incorporate this Software into Your proprietary programs and 
 *    into any third party proprietary programs. However, if You incorporate the Software into third party proprietary 
 *    programs, You agree that You are solely responsible for obtaining any permission from such third parties required to 
 *    incorporate the Software into such third party proprietary programs and for informing Your sublicensees, including 
 *    without limitation Your end-users, of their obligation to secure any required permissions from such third parties 
 *    before incorporating the Software into such third party proprietary software programs. In the event that You fail 
 *    to obtain such permissions, You agree to indemnify NCI for any claims against NCI by such third parties, except to 
 *    the extent prohibited by law, resulting from Your failure to obtain such permissions. 
 * 5. For sake of clarity, and not by way of limitation, You may add Your own copyright statement to Your modifications and 
 *    to the derivative works, and You may provide additional or different license terms and conditions in Your sublicenses 
 *    of modifications of the Software, or any derivative works of the Software as a whole, provided Your use, reproduction, 
 *    and distribution of the Work otherwise complies with the conditions stated in this License.
 * 6. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, 
 *    THE IMPLIED WARRANTIES OF MERCHANTABILITY, NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. 
 *    IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, SAIC, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, 
 *    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 *    GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 *    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 *    OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
 
 public class ComparativeGenomicForm extends BaseForm implements Serializable, Cloneable{
 	private static final long serialVersionUID = 1L;
 
 	private static Logger logger = Logger
             .getLogger(ComparativeGenomicForm.class);
 
      /** selected chromosomes cytobands **/
 	private List cytobands = new ArrayList();
 		
 	/** chromosomes property */
 	private static List<ChromosomeBean> chromosomes;
     
     /**geneOption property */    
 	private String geneOption = "standard";
 	//private String geneOption;
 
     /** geneList property */
     private String geneList;
     
     private static Collection savedGeneList;
     
     private static Collection savedSnpList;
     
     /** tumorGrade property */
     private String tumorGrade;
 
     /** assayPlatform property */
     private String assayPlatform;
     
     private Map<String, String> assayPlatforms;
 
     /** region property */
     private String region;
 
     /** cytobandRegionStart property */
 	private String cytobandRegionStart;
 	
 	/** cytobandRegionEnd property */
 	private String cytobandRegionEnd;
 
     /** snpList property */
     private String snpList;
 
     /** cloneId property */
     private String cloneId;
 
     /** cnAmplified property */
     private String cnAmplified;
 
     /** smAmplified property */
     private String smAmplified;
 
    
 
     /** cloneListFile property */
     private String cloneListFile;
 
     /** snpListFile property */
     private transient String snpListFile;
 
     /** cloneListSpecify property */
     private String cloneListSpecify;
 
     /** snpListSpecify property */
     private String snpListSpecify;
 
     /** cnADAmplified property */
     private String cnADAmplified;
 
     /** genomicTrack property */
     private String genomicTrack;
 
     /** basePairEnd property */
     private String basePairEnd;
 
     /** chrosomeNumber property */
     private String chromosomeNumber;
 
     /** cnADDeleted property */
     private String cnADDeleted;
 
     /** cnUnchangeTo property */
     private String cnUnchangeTo;
 
     /** smUnchangeTo property */
     private String smUnchangeTo;
 
     /** alleleFrequency property */
     private String alleleFrequency;
 
     /** geneType property */
     private String geneType;
 
     /** validatedSNP property */
     private String validatedSNP;
 
     /** resultView property */
     private String resultView;
 
     /** geneFile property */
     private String geneFile;
     
    
 
     /** snpId property */
     private String snpId;
 
     /** cnDeleted property */
     private String cnDeleted;
 
     /** smDeleted property */
     private String smDeleted;
 
     /** geneGroup property */
     private String geneGroup;
       
 
     /** cnUnchangeFrom property */
     private String cnUnchangeFrom;
 
     /** smUnchangeFrom property */
     private String smUnchangeFrom;
 
     /** cloneList property */
     private String cloneList;
 
     /** queryName property */
     private String queryName;
 
     /** copyNumber property */
     private String copyNumber;
 
     /** copyNumberView property */
     private String copyNumberView = "calculatedCN";
 
     /** geneRegionView property */
     private String geneRegionView = "geneView";
 
     /** sampleType property */
     private String sampleType;
 
     /** analysisType property */
     private String analysisType;
 
     /** segmentMean property */
     private String segmentMean;
 
     /** basePairStart property */
     private String basePairStart;
     
     /** isAllGenes property */
     private boolean isAllGenes = false;
     
     /** specimenType property */
     private String specimenType;     
     
 
     // Collections used for Lookup values.
 
     /*
      * * moved to the upper class: BaseForm.java private ArrayList diseaseTypes;
      * private ArrayList geneTypeColl;
      */
     private ArrayList sampleTypeColl = new ArrayList();
     
     private ArrayList analysisTypeColl = new ArrayList();
     
     private ArrayList cloneTypeColl = new ArrayList();
 
     private ArrayList snpTypes = new ArrayList();;
 
     private ArrayList alleleTypes = new ArrayList();
 
     private ArrayList assayTypes = new ArrayList();    
 
     private GeneIDCriteria geneCriteria;
     
     private AllGenesCriteria allGenesCriteria;  
 
     private CopyNumberCriteria copyNumberCriteria;
 
     private SegmentMeanCriteria segmentMeanCriteria;
 
     private RegionCriteria regionCriteria;
 
     private CloneOrProbeIDCriteria cloneOrProbeIDCriteria;
 
     private SNPCriteria snpCriteria;
 
     private AlleleFrequencyCriteria alleleFrequencyCriteria;
 
     private AssayPlatformCriteria assayPlatformCriteria;
 
     private AnalysisTypeCriteria analysisTypeCriteria;
     
     private String selectedView = "geneView";
     private String geneReviewChecked;
     private String regionReviewChecked;
 
     //----------------------------constuctor()
 
     public ComparativeGenomicForm() {
         super();
         startComparativeGemomicLookup();
     }
 
     // --------------------------------------------------------- Methods
 
     private void startComparativeGemomicLookup() {
         /*
          * * moved to the upper class: BaseForm.java diseaseTypes = new
          * ArrayList(); geneTypeColl = new ArrayList();
          */
         sampleTypeColl = new ArrayList();
         analysisTypeColl = new ArrayList();
         cloneTypeColl = new ArrayList();
         snpTypes = new ArrayList();
         alleleTypes = new ArrayList();
         assayTypes = new ArrayList();
         // These are hardcoded but will come from DB
         cloneTypeColl.add(new LabelValueBean("IMAGE Id", "imageId"));
         cloneTypeColl.add(new LabelValueBean("BAC Id", "BACId"));
         //snpTypes.add(new LabelValueBean("TSC Id","TSCId"));
         snpTypes.add(new LabelValueBean("dBSNP Id", "dBSNPId"));
         snpTypes.add(new LabelValueBean("SNP Probe Set Id", "probeSetId"));
         alleleTypes.add(new LabelValueBean("ALL", "ALL"));
         alleleTypes.add(new LabelValueBean("CENTRAL ASIA", "CENTRAL ASIA"));
         alleleTypes.add(new LabelValueBean("CENTRAL/SOUTH AFRICA",
                 "CENTRAL/SOUTH AFRICA"));
         alleleTypes.add(new LabelValueBean("CENTRAL/SOUTH AMERICA",
                 "CENTRAL/SOUTH AMERICA"));
         alleleTypes.add(new LabelValueBean("EAST ASIA", "EAST ASIA"));
         alleleTypes.add(new LabelValueBean("EUROPE", "EUROPE"));
         alleleTypes.add(new LabelValueBean("MULTI-NATIONAL", "MULTI-NATIONAL"));
         alleleTypes.add(new LabelValueBean("NORTH AMERICA", "NORTH AMERICA"));
         alleleTypes.add(new LabelValueBean("NORTH/EAST AFRICA  MIDDLE EASTL",
                 "NORTH/EAST AFRICA  MIDDLE EAST"));
         alleleTypes.add(new LabelValueBean("NOT SPECIFIED", "NOT SPECIFIED"));
         alleleTypes.add(new LabelValueBean("PACIFIC", "PACIFIC"));
         alleleTypes.add(new LabelValueBean("UNKNOWN", "UNKNOWN"));
         alleleTypes.add(new LabelValueBean("WEST AFRICA", "WEST AFRICA"));
 
         assayTypes.add(new LabelValueBean("All", "All"));
         assayTypes.add(new LabelValueBean("100K SNP Array", "100K SNP Array"));
         assayTypes.add(new LabelValueBean("Array CGH", "Array CGH"));
 
          analysisTypeColl.add(new LabelValueBean(AnalysisType.PAIRED.toString(),AnalysisType.PAIRED.name()));
         analysisTypeColl.add(new LabelValueBean(AnalysisType.UNPAIRED.toString(),AnalysisType.UNPAIRED.name()));
         analysisTypeColl.add(new LabelValueBean(AnalysisType.NORMAL.toString(),AnalysisType.NORMAL.name()));
         
         this.assayPlatforms = new HashMap<String, String>();
         assayPlatforms.put("Affymetrix 100K SNP Arrays", "100K SNP Array");
         
         this.geneReviewChecked = "checked";
         this.regionReviewChecked = "";
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
     
     public void validateForCytobands() {
 
     	//if method is "getCytobands" AND they have upload formFiles, do necessary validation for uploaded files
     	String geneGroup = this.getGeneGroup();
     	String snpId = this.getSnpId();
     	String sampelGroup = this.getSampleGroup();
     	if (geneGroup !=null && geneGroup.equalsIgnoreCase("Upload")
     			|| snpId !=null && snpId.equalsIgnoreCase("Upload")
     			||  sampelGroup != null && sampelGroup.equalsIgnoreCase("Upload")) {
     		if(geneGroup != null && geneGroup.equalsIgnoreCase("Upload")) {
     			this.setGeneGroup("");
     		}
 
     		if (sampelGroup != null && sampelGroup.equalsIgnoreCase("Upload")){
     			this.setSampleGroup("");
     		}
     	}
     }
     
     public List<String> validateForSubmitOrPreview() {
     	List<String> errors = new ArrayList<String>();
     	//Query Name cannot be blank
     	errors = UIFormValidator.validateQueryName(queryName, errors);
     	// Chromosomal region validations
     	errors = UIFormValidator.validateChromosomalRegion(chromosomeNumber, region, cytobandRegionStart, basePairStart,basePairEnd, errors);
 
 
     	//JB: Add validation for segment mean
     	// validate copy number or segment mean,it has to be 
     	if ( this.getCopyNumberView().equals("calculatedCN") ) {
     		errors = UIFormValidator.validateCopyNo(copyNumber,"ampdel",cnADAmplified,"cnADAmplified",errors);
     		errors = UIFormValidator.validateCopyNo(copyNumber,"ampdel",cnADDeleted,"cnADDeleted",errors);
     		errors = UIFormValidator.validateCopyNo(copyNumber,"amplified",cnAmplified,"cnAmplified",errors);
     		errors = UIFormValidator.validateCopyNo(copyNumber,"deleted",cnDeleted,"cnDeleted",errors);
     		errors = UIFormValidator.validateCopyNo(copyNumber,"unchange",cnUnchangeFrom,"cnUnchangeFrom",errors);
     		errors = UIFormValidator.validateCopyNo(copyNumber,"unchange",cnUnchangeTo,"cnUnchangeTo",errors);
     	} else { // validate segment mean
     		errors = UIFormValidator.validateSegmentMean(segmentMean,"amplified",smAmplified,"smAmplified",errors);
     		errors = UIFormValidator.validateSegmentMean(segmentMean,"deleted",smDeleted,"smDeleted",errors);
     		errors = UIFormValidator.validateSegmentMean(segmentMean,"unchange",smUnchangeFrom,"smUnchangeFrom",errors);
     		errors = UIFormValidator.validateSegmentMean(segmentMean,"unchange",smUnchangeTo,"smUnchangeTo",errors);
     	}
 
     	// Validate minimum criteria's for CGH Query
     	if (this.getQueryName() != null && this.getQueryName().length() >= 1 && 
     			(this.getGeneOption() != null && this.getGeneOption().equalsIgnoreCase("standard"))) {
     		if ((this.getGeneList() == null || this.getGeneList().trim()
     				.length() < 1)
     				&& (this.getChromosomeNumber() == null || this
     				.getChromosomeNumber().trim().length() < 1)) {
     			String msg = ApplicationContext.getLabelProperties().getProperty("gov.nih.nci.nautilus.ui.struts.form.cgh.minimum.error");
     			errors.add(msg);
 
     		}
     	}
 
     	return errors;
     }
 
     
   
     
    /**
      * Method reset
      * 
      * @param ActionMapping
      *            mapping
      * @param HttpServletRequest
      *            request
      */
     
     //Shan: we need to call this from somewhere explicitly
     public void reset(HttpServletRequest request) {
     	
     	GroupRetriever groupRetriever = new GroupRetriever();
 	    savedGeneList = groupRetriever.getGeneGroupsSubTypeCollection(request.getSession());
 	    savedSnpList = groupRetriever.getSnpGroupsCollection(request.getSession());
 	    
         geneList = "";
         tumorGrade = "";
         assayPlatform = "";
         region = "";
         cytobandRegionStart = "";
         cytobandRegionEnd = "";
         snpList = "";
         cloneId = "";
         cnAmplified = "";
         smAmplified = "";
         specimenType = "";
         cloneListFile = "";
         snpListFile = null;
         cloneListSpecify = "";
         snpListSpecify = "";
         cnADAmplified = "";
         genomicTrack = "";
         basePairEnd = "";
         chromosomeNumber = "";
         cnADDeleted = "";
         cnUnchangeTo = "";
         smUnchangeTo = "";
         alleleFrequency = "";
         geneType = "";
         validatedSNP = "";
         resultView = "";
         geneFile = null;
         snpId = "";
         cnDeleted = "";
         smDeleted = "";
         geneGroup = "";
         cnUnchangeFrom = "";
         smUnchangeFrom = "";
         cloneList = "";
         queryName = "";
         copyNumber = "";
         copyNumberView = "calculatedCN";
         geneRegionView = "geneView";
         segmentMean = "";
         sampleType = "PairedTissue";
         analysisType = AnalysisType.PAIRED.name();
         basePairStart = "";       
         //sampleGroup = "";
 		sampleList = "";
 		//sampleFile = null;
        
 
         diseaseOrGradeCriteria = new DiseaseOrGradeCriteria();
         geneCriteria = new GeneIDCriteria();
         sampleCriteria = new SampleCriteria();
         copyNumberCriteria = new CopyNumberCriteria();
         segmentMeanCriteria = new SegmentMeanCriteria();
         regionCriteria = new RegionCriteria();
         cloneOrProbeIDCriteria = new CloneOrProbeIDCriteria();
         snpCriteria = new SNPCriteria();
         alleleFrequencyCriteria = new AlleleFrequencyCriteria();
         assayPlatformCriteria = new AssayPlatformCriteria();
         allGenesCriteria = new AllGenesCriteria(isAllGenes);
         analysisTypeCriteria = new AnalysisTypeCriteria(AnalysisType.PAIRED);
         // reset the request object
         this.thisRequest = request;
 
     }
     
     /**
 	 * Set the chromosomes Collection
 	 * 
 	 * @param chromosomes
 	 */
 	public void setChromosomes(List chromosomes) {
 		ComparativeGenomicForm.chromosomes = chromosomes;
 	}
 
 	/**
 	 * Return the chromosomes List
 	 * 
 	 * @param chromosomes
 	 */
 	public List getChromosomes() {
 		return ComparativeGenomicForm.chromosomes;
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
      * Set the geneList.
      * 
      * @param geneList
      *            The geneList to set
      */
     public void setGeneList(String geneList) {
 		if (geneList != null )
 			geneList = MoreStringUtils.cleanJavascript(geneList);
     }
     
     public void setGeneListDetails() {
 
 		String thisGeneType =  this.geneType; //this.thisRequest.getParameter("geneType");
 		geneCriteria = new GeneIDCriteria();
 		GeneIdentifierDE geneIdentifierDE = null;
 		if ((geneList != null)
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
 
 				} 
 
 				geneCriteria.setGeneIdentifier(geneIdentifierDE);
 			}
 		}
 	}
     
 //	public void setGeneList(Collection<GeneIdentifierDE> geneIdentifiers) {
 //		StringBuffer geneBuffer = new StringBuffer();
 //		
 //		for( GeneIdentifierDE ge :  geneIdentifiers ) {
 //			geneBuffer.append( ge.getValueObject() );
 //		}
 //			
 //			this.geneList = geneBuffer.toString();
 //	}
 
 	public void setGeneIDCriteria( GeneIDCriteria geneCriteria ) {
 		this.geneCriteria = geneCriteria;
 	}
 	
     
     /**
 	 * Sets the geneOption
 	 * 
 	 * @return String
 	 */
 	public void setGeneOption(String geneOption){
 	    this.geneOption = geneOption;
 	}
 	
 	public void setGeneOptionDetails() {
 
 		String thisGeneOption = this.geneOption;//   this.thisRequest.getParameter("geneOption");
 		if (thisGeneOption != null
 				&& thisGeneOption.equalsIgnoreCase("allgenes")) {
 			isAllGenes = true;
 			allGenesCriteria = new AllGenesCriteria(isAllGenes);
 		}
 	}
 	
 	/**
 	 * Returns the geneOption.
 	 * 
 	 * @return String
 	 */	
 	public String getGeneOption(){
 	    return geneOption;
 	}
     
    
     public GeneIDCriteria getGeneIDCriteria() {
         return this.geneCriteria;
     }
     
     public AllGenesCriteria getAllGenesCriteria(){
         return this.allGenesCriteria;
     }
 
     public CopyNumberCriteria getCopyNumberCriteria() {
         return this.copyNumberCriteria;
     }
 
     public SegmentMeanCriteria getSegmentMeanCriteria() {
         return this.segmentMeanCriteria;
     }
 
     public RegionCriteria getRegionCriteria() {
         return this.regionCriteria;
     }
 
 	public void setRegionCriteria( RegionCriteria regionCriteria ) {
 		this.regionCriteria = regionCriteria;
 	}
 
     public CloneOrProbeIDCriteria getCloneOrProbeIDCriteria() {
         return this.cloneOrProbeIDCriteria;
     }
 
     public SNPCriteria getSNPCriteria() {
         return this.snpCriteria;
     }
 
     public void setSNPCriteria( SNPCriteria sNPCriteria) {
         this.snpCriteria = sNPCriteria;
     }
 
     public AlleleFrequencyCriteria getAlleleFrequencyCriteria() {
         return this.alleleFrequencyCriteria;
     }
 
     public AnalysisTypeCriteria getAnalysisTypeCriteria() {
         return analysisTypeCriteria;
     }
 
     public AssayPlatformCriteria getAssayPlatformCriteria() {
         return assayPlatformCriteria;
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
      * Returns the sampleType.
      * 
      * @return String
      */
     public String getSampleType() {
         return sampleType;
     }
 
     /**
      * Set the sampleType.
      * 
      * @param sampleType
      *            The sampleType to set
      */
     public void setSampleType(String sampleType) {
         this.sampleType = sampleType;
         	                                            
       
     }
 
     /**
      * Returns the analysisType.
      * 
      * @return String
      */
     public String getAnalysisType() {
         return analysisType;
     }
 
     /**
      * Set the analysisType.
      * 
      * @param analysisType
      *            The analysisType to set
      */
 	public void setAnalysisType(String analysisType) {
 		this.analysisType = analysisType;
 	}
 
     /**
      * Returns the assayPlatform.
      * 
      * @return String
      */
     public String getAssayPlatform() {
         return assayPlatform;
     }
 
     /**
      * Set the assayPlatform.
      * 
      * @param assayPlatform
      *            The assayPlatform to set
      */
     public void setAssayPlatform(String assayPlatform) {
         this.assayPlatform = assayPlatform;
         AssayPlatformDE assayPlatformDE = new AssayPlatformDE(this.assayPlatform);
         assayPlatformCriteria = new AssayPlatformCriteria();
         assayPlatformCriteria.setAssayPlatformDE(assayPlatformDE);                                                         
       
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
      * Returns the cytobandRegion.
      * 
      * @return String
      */
     public String getCytobandRegionStart() {
         return cytobandRegionStart;
     }
 
     /**
      * Set the cytobandRegion.
      * 
      * @param cytobandRegion
      *            The cytobandRegion to set
      */
     public void setCytobandRegionStart(String cytobandRegionStart) {
         this.cytobandRegionStart = cytobandRegionStart;
     }
     
     public void setCytobandRegionStartDetails() {
 		
 		//Shan: Need to change this somewhere else
 		//if (thisRequest != null) {
 			String thisRegion = this.region;//this.thisRequest.getParameter("region");
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
 		//}
 
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
     }
     
     public void setCytobandRegionEndDetails() {
 
     	String thisRegion2 = this.region; //this.thisRequest.getParameter("region");
     	String thisChrNumber2 = this.chromosomeNumber;
     	//this.thisRequest.getParameter("chromosomeNumber");
 
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
     
 //	public void setCytobandRegionStart(CytobandDE cytobandDE) {
 //		if ( cytobandDE != null )
 //			this.cytobandRegionStart = cytobandDE.getValueObject();
 //	}
 //	
 //	public void setCytobandRegionEnd(CytobandDE cytobandDE) {
 //		if ( cytobandDE != null )
 //			this.cytobandRegionEnd = cytobandDE.getValueObject();
 //	}
     
 
     /**
      * Returns the snpList.
      * 
      * @return String
      */
     public String getSnpList() {
         return snpList;
     }
 
     /**
      * Set the snpList.
      * 
      * @param snpList
      *            The snpList to set
      */
     public void setSnpList(String snpList) {
         this.snpList = snpList;
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
      * Returns the cnAmplified.
      * 
      * @return String
      */
     public String getCnAmplified() {
         return cnAmplified;
     }
 
     /**
      * Returns the smAmplified.
      * 
      * @return String
      */
     public String getSmAmplified() {
         return smAmplified;
     }
 
     /**
      * Set the cnAmplified.
      * 
      * @param cnAmplified
      *            The cnAmplified to set
      */
     public void setCnAmplified(String cnAmplified) {
         this.cnAmplified = cnAmplified;
     }
     
     public void setCnAmplifiedDetails() {
 
     	//            // need to make sure the parameters such as copyNumberAmplified and
     	//            // regulationStatus
     	//            // match the ones declared on the copyNumber_tile.jsp
     	String thisCopyNumber = this.copyNumber; //  this.thisRequest.getParameter("copyNumber");
     	if (thisCopyNumber != null
     			&& thisCopyNumber.equalsIgnoreCase("amplified")
     			&& (this.cnAmplified != null && this.cnAmplified.trim().length() > 0)) {
     		try{
 
     			copyNumberCriteria = new CopyNumberCriteria();
     			if(this.cnAmplified != null){
     				Double calculatedCopyNumber = new Double(Float.valueOf(this.cnAmplified)/2) ;
     				calculatedCopyNumber = MathUtil.getLog2(calculatedCopyNumber);
     				SNPableDE copyNumberDE = new CopyNumberDE.Amplification(calculatedCopyNumber.floatValue());
     				copyNumberCriteria.setCopyNumber(copyNumberDE);   
     			}
 
     		}
     		catch(NumberFormatException e){}
 
     	}
     	
     }
 
     /**
      * Set the smAmplified.
      * 
      * @param smAmplified
      *            The smAmplified to set
      */
     public void setSmAmplified(String smAmplified) {
         this.smAmplified = smAmplified;
     }
     
     public void setSmAmplifiedDetails() {
 
     	//            // need to make sure the parameters such as segmentMeanAmplified and
     	//            // regulationStatus
     	//            // match the ones declared on the segmentMean_tile.jsp
     	String thisSegmentMean = this.segmentMean; //this.thisRequest.getParameter("segmentMean");
     	if (thisSegmentMean != null
     			&& thisSegmentMean.equalsIgnoreCase("amplified")
     			&& (this.smAmplified != null && this.smAmplified.trim().length() > 0)) {
     		try {
 
     			segmentMeanCriteria = new SegmentMeanCriteria();
     			SegmentMeanDE segmentMeanDE = new SegmentMeanDE.Amplification(Float.valueOf(this.smAmplified));
     			segmentMeanCriteria.setSegmentMean(segmentMeanDE);            	                                            
     		} catch(NumberFormatException e){
     		}
     	}        
     	
     }
     
 //    public void setCnAmplified(SNPableDE copyNumberDE) {
 //    	this.cnAmplified = copyNumberDE.getValueObject().toString();
 //    }
 //    
 //    public void setSmAmplified(SegmentMeanDE segmentMeanDE) {
 //    	this.smAmplified = segmentMeanDE.getValueObject().toString();
 //    }
     
     /**
      * Returns the cloneListFile.
      * 
      * @return String
      */
     public String getCloneListFile() {
         return cloneListFile;
     }
 
     /**
      * Set the cloneListFile.
      * 
      * @param cloneListFile
      *            The cloneListFile to set
      */
     public void setCloneListFile(String cloneListFile) {
         this.cloneListFile = cloneListFile;
     }
     
     public void setCloneListFileDetails() {
 //        if (thisRequest != null) {
 //            // this is to check if the radio button is selected for the clone
 //            // category
             String thisCloneId = this.cloneId;  //(String) thisRequest.getParameter("cloneId");
             // this is to check the type of the clone
             String thisCloneList = this.cloneList;
             		
             		//(String) thisRequest.getParameter("cloneList");
             if ((thisCloneId != null) && thisCloneId.equalsIgnoreCase("Upload")
                     && (thisCloneList.length() > 0)
                     && (this.cloneListFile.length() > 0)) {
 
                 File cloneFile = new File(this.cloneListFile);
                 String line = null;
                 cloneOrProbeIDCriteria = new CloneOrProbeIDCriteria();
                 try {
                     FileReader editfr = new FileReader(cloneFile);
                     BufferedReader inFile = new BufferedReader(editfr);
                     line = inFile.readLine();
                     int i = 0;
 
                     while (line != null && line.length() > 0) {
                         StringTokenizer st = new StringTokenizer(line);
                         while (st.hasMoreTokens()) {
                             String token = st.nextToken();
                             if (thisCloneList.equalsIgnoreCase("imageId")) {
                               	 CloneIdentifierDE cloneIdentifierDE = new CloneIdentifierDE.IMAGEClone(token);                                                                            
                                  cloneOrProbeIDCriteria.setCloneIdentifier(cloneIdentifierDE);
                                
                             } else if (thisCloneList.equalsIgnoreCase("BACId")) {
 	                           	 CloneIdentifierDE cloneIdentifierDE = new CloneIdentifierDE.IMAGEClone(token);                                                                            
 	                             cloneOrProbeIDCriteria.setCloneIdentifier(cloneIdentifierDE);
                             }
 
                         }
                         line = inFile.readLine();
                     }// end of while
 
                     inFile.close();
                 } catch (IOException ex) {
                     logger.error("Errors when uploading gene file:"
                             + ex.getMessage());
                 }
             }
        // }
 
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
     }
     
     //this copy is from geneExpressionForm - slightly different
     public void setCloneListSpecifyDetails2() {
 
     	//Shan: Need to change this somewhere else
     	//		if (thisRequest != null) {
     	// this is to check if the radio button is selected for the clone
     	// category
     	String thisCloneId = this.cloneId; //(String) thisRequest.getParameter("cloneId");
 
     	// this is to check the type of the clone
    	String thisCloneList = (String) thisRequest.getParameter("cloneList");
 
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
     	//		}
     }
 
     public void setCloneListSpecifyDetails() {
     	//        if (thisRequest != null) {
     	//            // this is to check if the radio button is selected for the clone
     	//            // category
     	String thisCloneId = this.cloneId; // (String) thisRequest.getParameter("cloneId");
     	// this is to check the type of the clone
     	String thisCloneList = this.cloneList;
     	//(String) thisRequest.getParameter("cloneList");
     	if (thisCloneId != null && thisCloneList != null
     			&& !thisCloneList.equals("")) {
     		cloneOrProbeIDCriteria = new CloneOrProbeIDCriteria();
     		if (this.cloneListSpecify != null
     				&& !this.cloneListSpecify.equals("")) {
     			String[] cloneStr = this.cloneListSpecify.split("\\x2C");
     			for (int i = 0; i < cloneStr.length; i++) {
     				if (thisCloneList.equalsIgnoreCase("imageId")) {
     					CloneIdentifierDE cloneIdentifierDE = new CloneIdentifierDE.IMAGEClone(cloneStr[i].trim());                                                                            
     					cloneOrProbeIDCriteria.setCloneIdentifier(cloneIdentifierDE);                           
     				} else if (thisCloneList.equalsIgnoreCase("BACId")) {
     					CloneIdentifierDE cloneIdentifierDE = new CloneIdentifierDE.BACClone(cloneStr[i].trim());                                                                            
     					cloneOrProbeIDCriteria.setCloneIdentifier(cloneIdentifierDE);        
 
 
     				}
 
     			} // end of for loop
     		}// end of if(this.cloneListSpecify != null &&
     		// !cloneListSpecify.equals("")){
 
     	}
     	//        }
     }
 
     /**
      * Returns the snpListSpecify.
      * 
      * @return String
      */
     public String getSnpListSpecify() {
         return snpListSpecify;
     }
 
     /**
      * Set the snpListSpecify.
      * 
      * @param snpListSpecify
      *            The snpListSpecify to set
      */
     public void setSnpListSpecify(String snpListSpecify) {
         this.snpListSpecify = snpListSpecify;
     }
     
     public void setSnpListSpecifyDetails() {
 //        if (thisRequest != null) {
 //            // this is to check if the radio button is selected for the SNP
 //            // category
     	String thisSNPId = this.snpId; //thisRequest.getParameter("snpId");
 
     	// this is to check the type of the SNP
     	String thisSNPList = this.snpList;  //thisRequest.getParameter("snpList");
 
     	if (thisSNPId != null && thisSNPId.equalsIgnoreCase("specify")
     			&& thisSNPList != null && !thisSNPList.equals("")) {
     		if (this.snpListSpecify != null
     				&& !this.snpListSpecify.equals("")) {
     			String[] snpStr = this.snpListSpecify.split("\\x2C");
     			snpCriteria = new SNPCriteria();
     			for (int i = 0; i < snpStr.length; i++) {
     				if (thisSNPList.equalsIgnoreCase("TSCId")) {
     					SNPIdentifierDE snpIdentifierDE =   new SNPIdentifierDE.TSC(snpStr[i].trim());
     					snpCriteria.setSNPIdentifier(snpIdentifierDE);
 
     				} else if (thisSNPList.equalsIgnoreCase("dBSNPId")) {
     					SNPIdentifierDE snpIdentifierDE =   new SNPIdentifierDE.DBSNP(snpStr[i].trim());
     					snpCriteria.setSNPIdentifier(snpIdentifierDE);	                     
 
     				} else if (thisSNPList.equalsIgnoreCase("probeSetId")) {
     					SNPIdentifierDE snpIdentifierDE =   new SNPIdentifierDE.SNPProbeSet(snpStr[i].trim());
     					snpCriteria.setSNPIdentifier(snpIdentifierDE);                       
 
     				}
     			} // end of for loop
     		}// end of if(thisSNPId != null && thisSNPList != null &&
     		// !thisSNPList.equals("")){
 
     	}
 //        }
     }
 
     /**
      * Returns the snpListFile.
      * 
      * @return String
      */
     public String getSnpListFile() {
         return snpListFile;
     }
 
     /**
      * Set the snpListFile.
      * 
      * @param snpListFile
      *            The snpListFile to set
      */
     public void setSnpListFile(String snpListFile) {
         this.snpListFile = snpListFile;
     }
 
     /**
      * Returns the cnADAmplified.
      * 
      * @return String
      */
     public String getCnADAmplified() {
         return cnADAmplified;
     }
 
     /**
      * Set the cnADAmplified.
      * 
      * @param cnADAmplified
      *            The cnADAmplified to set
      */
     public void setCnADAmplified(String cnADAmplified) {
         this.cnADAmplified = cnADAmplified;
     }
     
     public void setCnADAmplifiedDetails() {
         // need to make sure the parameters such as copyNumberADAmplified and
         // regulationStatus
         // match the ones declared on the copyNumber_tile.jsp
 //        if (thisRequest != null) {
     	String thisCopyNumber = this.copyNumber; // this.thisRequest.getParameter("copyNumber");
 
     	if (thisCopyNumber != null
     			&& thisCopyNumber.equalsIgnoreCase("ampdel")
     			&& (this.cnADAmplified != null && this.cnADAmplified.trim().length() >=1)) {
     		try{       		
     			Double calculatedCopyNumber = new Double(Float.valueOf(this.cnADAmplified)/2) ;
     			calculatedCopyNumber = MathUtil.getLog2(calculatedCopyNumber);
     			SNPableDE copyNumberDE = new CopyNumberDE.Amplification(calculatedCopyNumber.floatValue());
     			copyNumberCriteria.setCopyNumber(copyNumberDE);            	                                            
 
     		}
     		catch(NumberFormatException e){}
     	}
 //        }
 
     }
     
     public void setCnADAmplified(SNPableDE copyNumberDE) {
     	this.cnADAmplified = copyNumberDE.getValueObject().toString();
     }
     
 
     /**
      * Returns the genomicTrack.
      * 
      * @return String
      */
     public String getGenomicTrack() {
         return genomicTrack;
     }
 
     /**
      * Set the genomicTrack.
      * 
      * @param genomicTrack
      *            The genomicTrack to set
      */
     public void setGenomicTrack(String genomicTrack) {
         this.genomicTrack = genomicTrack;
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
     }
     
     public void setBasePairEndDetails() {
 
 		//Shan: Need to change this somewhere else
 //		if (thisRequest != null) {
 			String thisRegion = this.region; //this.thisRequest.getParameter("region");
 			String thisChrNumber = this.chromosomeNumber; 
 			//this.thisRequest.getParameter("chromosomeNumber");
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
 //		}
 	}
     
 //	public void setBasePairEnd(BasePairPositionDE basePairPositionDE ) {
 //		if ( basePairPositionDE != null )
 //			this.basePairEnd = basePairPositionDE.getValueObject().toString();
 //	}
     
 
     /**
      * Returns the chrosomeNumber.
      * 
      * @return String
      */
     public String getChromosomeNumber() {
         return chromosomeNumber;
     }
 
     /**
      * Set the chrosomeNumber.
      * 
      * @param chrosomeNumber
      *            The chrosomeNumber to set
      */
     public void setChromosomeNumber(String chromosomeIndex) {
 		//IMPORTANT! The chromosomeNumber is actually the
 		//index into the chromosome List where the selected
 		//chromosome can be found.  It is NOT the actual chromosome
 		//number.  Chromosome numbers can actually be characters, like
 		// X and Y so we 
 		this.chromosomeNumber = chromosomeIndex;
 		if(!"".equals(chromosomeIndex) && !"-1".equals(chromosomeIndex)) {
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
     
 //	public void setChromosomeNumber(ChromosomeNumberDE chromosomeNumberDE) {
 //		if ( chromosomeNumberDE != null )
 //		{
 //			for ( ChromosomeBean chromosome : chromosomes){
 //				if ( chromosome.getLabel().equals( chromosomeNumberDE.getValueObject() )) {
 //					this.chromosomeNumber = chromosome.getValue();
 //					break;
 //				}
 //			}
 //		}
 //	}	
     
 
 
     /**
      * Returns the cnADDeleted.
      * 
      * @return String
      */
     public String getCnADDeleted() {
         return cnADDeleted;
     }
 
     /**
      * Set the cnADDeleted.
      * 
      * @param cnADDeleted
      *            The cnADDeleted to set
      */
     public void setCnADDeleted(String cnADDeleted) {
         this.cnADDeleted = cnADDeleted;
     }
     
     public void setCnADDeletedDetails() {
         // need to make sure the parameters such as copyNumberADDeleted and
         // regulationStatus
         // match the ones declared on the copyNumber_tile.jsp
 //        if (thisRequest != null) {
     	String thisCopyNumber = this.copyNumber; //this.thisRequest.getParameter("copyNumber");
 
     	if (thisCopyNumber != null
     			&& thisCopyNumber.equalsIgnoreCase("ampdel")
     			&& (this.cnADDeleted != null && this.cnADDeleted.trim().length() >=1)) {
     		try{ 
 
     			Double calculatedCopyNumber = new Double(Float.valueOf(this.cnADDeleted)/2) ;
     			calculatedCopyNumber = MathUtil.getLog2(calculatedCopyNumber);
     			SNPableDE copyNumberDE = new CopyNumberDE.Deletion(calculatedCopyNumber.floatValue());
     			copyNumberCriteria.setCopyNumber(copyNumberDE);    
 
     		}
     		catch(NumberFormatException e){}
     	}
 //        }
     }
     
 //    public void setCnADDeleted(SNPableDE copyNumberDE) {
 //    	this.cnADDeleted = copyNumberDE.getValueObject().toString();
 //    }
     
 
 
     /**
      * Returns the cnUnchangeTo.
      * 
      * @return String
      */
     public String getCnUnchangeTo() {
         return cnUnchangeTo;
     }
 
 
     /**
      * Returns the smUnchangeTo.
      * 
      * @return String
      */
     public String getSmUnchangeTo() {
         return smUnchangeTo;
     }
 
     /**
      * Set the cnUnchangeTo.
      * 
      * @param cnUnchangeTo
      *            The cnUnchangeTo to set
      */
     public void setCnUnchangeTo(String cnUnchangeTo) {
         this.cnUnchangeTo = cnUnchangeTo;
     }
     
     public void setCnUnchangeToDetails() {
 //        if (thisRequest != null) {
     	String thisCopyNumber = this.copyNumber;  //   this.thisRequest.getParameter("copyNumber");
     	if (thisCopyNumber != null
     			&& thisCopyNumber.equalsIgnoreCase("unchange")
     			&& (this.cnUnchangeTo != null && this.cnUnchangeTo.trim().length() > 0)) {
     		try{
     			Double calculatedCopyNumber = new Double(Float.valueOf(this.cnUnchangeTo)/2) ;
     			calculatedCopyNumber = MathUtil.getLog2(calculatedCopyNumber);
     			SNPableDE copyNumberDE = new CopyNumberDE.UnChangedCopyNumberUpperLimit(calculatedCopyNumber.floatValue());
     			copyNumberCriteria.setCopyNumber(copyNumberDE); 
     		}
 
     		catch(NumberFormatException e){}
     	}
 //        }
 
     }
 
     /**
      * Set the smUnchangeTo.
      * 
      * @param smUnchangeTo
      *            The smUnchangeTo to set
      */
     public void setSmUnchangeTo(String smUnchangeTo) {
         this.smUnchangeTo = smUnchangeTo;
     }
     
     public void setSmUnchangeToDetails() {
 //        if (thisRequest != null) {
     	String thisSegmentMean = this.segmentMean; // this.thisRequest.getParameter("segmentMean");
     	if (thisSegmentMean != null
     			&& thisSegmentMean.equalsIgnoreCase("unchange")
     			&& (this.smUnchangeTo != null && this.smUnchangeTo.trim().length() > 0)) {
     		try{
 
     			SegmentMeanDE segmentMeanDE = new SegmentMeanDE.UnChangedSegmentMeanUpperLimit(Float.valueOf(this.smUnchangeTo));
     			segmentMeanCriteria.setSegmentMean(segmentMeanDE);  
     		}
 
     		catch(NumberFormatException e){}
     	}
 //        }
 
     }
 
 //    public void setCnUnchangeTo(SNPableDE copyNumberDE) {
 //    	this.cnUnchangeTo = copyNumberDE.getValueObject().toString();
 //    }
 //
 //    public void setSmUnchangeTo(SegmentMeanDE segmentMeanDE) {
 //    	this.smUnchangeTo = segmentMeanDE.getValueObject().toString();
 //    }
 
     /**
      * Returns the alleleFrequency.
      * 
      * @return String
      */
     public String getAlleleFrequency() {
         return alleleFrequency;
     }
 
     /**
      * Set the alleleFrequency.
      * 
      * @param alleleFrequency
      *            The alleleFrequency to set
      */
     public void setAlleleFrequency(String alleleFrequency) {
         this.alleleFrequency = alleleFrequency;
         AlleleFrequencyDE alleleFrequencyDE = new AlleleFrequencyDE(this.alleleFrequency);
         alleleFrequencyCriteria = new AlleleFrequencyCriteria();
         alleleFrequencyCriteria.setAlleleFrequencyDE(alleleFrequencyDE);
      
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
      * Returns the validatedSNP.
      * 
      * @return String
      */
     public String getValidatedSNP() {
         return validatedSNP;
     }
 
     /**
      * Set the validatedSNP.
      * 
      * @param validatedSNP
      *            The validatedSNP to set
      */
     public void setValidatedSNP(String validatedSNP) {
         this.validatedSNP = validatedSNP;
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
      * Returns the geneFile.
      * 
      * @return FormFile
      */
     public String getGeneFile() {
         return geneFile;
     }
     
    
   
     public void setGeneFile(String geneFile) {
         this.geneFile = geneFile;
     }
 
     /**
      * Returns the snpId.
      * 
      * @return String
      */
     public String getSnpId() {
         return snpId;
     }
 
     /**
      * Set the snpId.
      * 
      * @param snpId
      *            The snpId to set
      */
     public void setSnpId(String snpId) {
         this.snpId = snpId;
     }
 
     /**
      * Returns the cnDeleted.
      * 
      * @return String
      */
     public String getCnDeleted() {
         return cnDeleted;
     }
 
     /**
      * Returns the smDeleted.
      * 
      * @return String
      */
     public String getSmDeleted() {
         return smDeleted;
     }
 
     /**
      * Set the cnDeleted.
      * 
      * @param cnDeleted
      *            The cnDeleted to set
      */
     public void setCnDeleted(String cnDeleted) {
         this.cnDeleted = cnDeleted;
     }
     
     public void setCnDeletedDetails() {
 //        if (thisRequest != null) {
 //            // need to make sure the parameters such as copyNumberDeleted and
 //            // regulationStatus
 //            // match the ones declared on the copyNumber_tile.jsp
     	String thisCopyNumber = this.copyNumber; //this.thisRequest.getParameter("copyNumber");
     	if (thisCopyNumber != null
     			&& thisCopyNumber.equalsIgnoreCase("deleted")
     			&& (this.cnDeleted != null && this.cnDeleted.trim().length() > 0)) {
     		try{
 
     			copyNumberCriteria = new CopyNumberCriteria();
     			Double calculatedCopyNumber = new Double(Float.valueOf(this.cnDeleted)/2) ;
     			calculatedCopyNumber = MathUtil.getLog2(calculatedCopyNumber);
     			SNPableDE copyNumberDE = new CopyNumberDE.Deletion(calculatedCopyNumber.floatValue());
     			copyNumberCriteria.setCopyNumber(copyNumberDE); 
 
     		}
     		catch(NumberFormatException e){}
     	}
 //        }
     }
 
     /**
      * Set the smDeleted.
      * 
      * @param smDeleted
      *            The smDeleted to set
      */
     public void setSmDeleted(String smDeleted) {
         this.smDeleted = smDeleted;
     }
     
     public void setSmDeletedDetails() {
 //        if (thisRequest != null) {
 //            // need to make sure the parameters such as segmentMeanDeleted and
 //            // regulationStatus
 //            // match the ones declared on the segmentMean_tile.jsp
     	String thisSegmentMean = this.segmentMean; // this.thisRequest.getParameter("segmentMean");
     	if (thisSegmentMean != null
     			&& thisSegmentMean.equalsIgnoreCase("deleted")
     			&& (this.smDeleted != null && this.smDeleted.trim().length() > 0)) {
     		try{
 
     			segmentMeanCriteria = new SegmentMeanCriteria();
     			SegmentMeanDE segmentMeanDE = new SegmentMeanDE.Deletion(Float.valueOf(this.smDeleted));
     			segmentMeanCriteria.setSegmentMean(segmentMeanDE); 
 
     		}
     		catch(NumberFormatException e){}
     	}
 //        }
     }
     
 //    public void setCnDeleted(SNPableDE copyNumberDE) {
 //    	this.cnDeleted = copyNumberDE.getValueObject().toString();
 //    }
 //    
 //    public void setSmDeleted(SegmentMeanDE segmentMeanDE) {
 //    	this.smDeleted = segmentMeanDE.getValueObject().toString();
 //    }
     
 
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
      * Returns the cnUnchangeFrom.
      * 
      * @return String
      */
     public String getCnUnchangeFrom() {
         return cnUnchangeFrom;
     }
 
     /**
      * Returns the smUnchangeFrom.
      * 
      * @return String
      */
     public String getSmUnchangeFrom() {
         return smUnchangeFrom;
     }
 
     /**
      * Set the cnUnchangeFrom.
      * 
      * @param cnUnchangeFrom
      *            The cnUnchangeFrom to set
      */
     public void setCnUnchangeFrom(String cnUnchangeFrom) {
         this.cnUnchangeFrom = cnUnchangeFrom;
     }
     
     public void setCnUnchangeFromDetails() {
 //        if (thisRequest != null) {
             String thisCopyNumber = this.copyNumber;// this.thisRequest.getParameter("copyNumber");
             if (thisCopyNumber != null
                     && thisCopyNumber.equalsIgnoreCase("unchange")
                     && (this.cnUnchangeFrom != null && this.cnUnchangeFrom.trim().length() > 0)) {
             	try{
 	            	
 		 				Double calculatedCopyNumber = new Double(Float.valueOf(this.cnUnchangeFrom)/2) ;
 						calculatedCopyNumber = MathUtil.getLog2(calculatedCopyNumber);
 						SNPableDE copyNumberDE = new CopyNumberDE.UnChangedCopyNumberDownLimit(calculatedCopyNumber.floatValue());
 						copyNumberCriteria.setCopyNumber(copyNumberDE); 
 		           }
             	    
             	 catch(NumberFormatException e){}
               }
 //        }
     }
 
     /**
      * Set the smUnchangeFrom.
      * 
      * @param smUnchangeFrom
      *            The smUnchangeFrom to set
      */
     public void setSmUnchangeFrom(String smUnchangeFrom) {
         this.smUnchangeFrom = smUnchangeFrom;
     }
     
     public void setSmUnchangeFromDetails() {
 //        if (thisRequest != null) {
     	String thisSegmentMean = this.segmentMean; // this.thisRequest.getParameter("segmentMean");
     	if (thisSegmentMean != null
     			&& thisSegmentMean.equalsIgnoreCase("unchange")
     			&& (this.smUnchangeFrom != null && this.smUnchangeFrom.trim().length() > 0)) {
     		try{
 
     			SegmentMeanDE segmentMeanDE = new SegmentMeanDE.UnChangedSegmentMeanDownLimit(Float.valueOf(this.smUnchangeFrom));
     			segmentMeanCriteria.setSegmentMean(segmentMeanDE);           	                                            
     		}
 
     		catch(NumberFormatException e){}
     	}
 //        }
     }
     
 //    public void setCnUnchangeFrom(SNPableDE copyNumberDE) {
 //    	this.cnUnchangeFrom = copyNumberDE.getValueObject().toString();
 //    }
 //    
 //    public void setSmUnchangeFrom(SegmentMeanDE segmentMeanDE) {
 //    	this.smUnchangeFrom = segmentMeanDE.getValueObject().toString();
 //    }
     
 
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
 		if (queryName != null )
 			queryName = MoreStringUtils.cleanJavascript(queryName);
 
         this.queryName = queryName;
     }
 
     /**
      * Returns the copyNumberView.
      * 
      * @return String
      */
     public String getCopyNumberView() {
         return copyNumberView;
     }
 
     /**
      * Set the copyNumberView.
      * 
      * @param copyNumberView
      *            The copyNumberView to set
      */
     public void setCopyNumberView(String copyNumberView) {
         this.copyNumberView = copyNumberView;
     }
 
     /**
      * Returns the geneRegionView.
      * 
      * @return String
      */
     public String getGeneRegionView() {
         return geneRegionView;
     }
 
     /**
      * Set the geneRegionView.
      * 
      * @param geneRegionView
      *            The geneRegionView to set
      */
     public void setGeneRegionView(String geneRegionView) {
         this.geneRegionView = geneRegionView;
     }
 
     /**
      * Returns the copyNumber.
      * 
      * @return String
      */
     public String getCopyNumber() {
         return copyNumber;
     }
 
     /**
      * Set the copyNumber.
      * 
      * @param copyNumber
      *            The copyNumber to set
      */
     public void setCopyNumber(String copyNumber) {
         this.copyNumber = copyNumber;
     }
 
     /**
      * Returns the segmentMean.
      * 
      * @return String
      */
     public String getSegmentMean() {
         return segmentMean;
     }
 
     /**
      * Set the segmentMean.
      * 
      * @param segmentMean
      *            The segmentMean to set
      */
     public void setSegmentMean(String segmentMean) {
         this.segmentMean = segmentMean;
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
     }
     
     public void setBasePairStartDetails() {
 
     	//Shan: Need to change this somewhere else
     	//		if (thisRequest != null) {
     	String thisRegion = this.region; //this.thisRequest.getParameter("region");
     	String thisChrNumber = this.chromosomeNumber;
     	//this.thisRequest.getParameter("chromosomeNumber");
     	String thisBasePairEnd = this.basePairEnd; 
     	//this.thisRequest.getParameter("basePairEnd");
 
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
     	//		}
 
     }
     
 //	public void setBasePairStart(BasePairPositionDE basePairPositionDE) {
 //		if ( basePairPositionDE != null )
 //			this.basePairStart = basePairPositionDE.getValueObject().toString();
 //	}
     
 
     public ArrayList getCloneTypeColl() {
         return cloneTypeColl;
     }
 
     public ArrayList getSnpTypes() {
         return snpTypes;
     }
 
     public ArrayList getAlleleTypes() {
         return alleleTypes;
     }
 
     public ArrayList getSampleTypeColl() {
         return sampleTypeColl;
     }
 
     public ArrayList getAnalysisTypeColl() {
         return analysisTypeColl;
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
 	public boolean getIsAllGenes(){
 	    return this.isAllGenes;
 	}
 
     public ComparativeGenomicForm cloneMe() {
         ComparativeGenomicForm form = new ComparativeGenomicForm();
         form.setGeneList(geneList);
         form.setSampleList(sampleList);
         form.setTumorGrade(tumorGrade);
         form.setTumorType(tumorType);
         form.setAssayPlatform(assayPlatform);
         form.setRegion(region);
         form.setCytobandRegionStart(cytobandRegionStart);
         form.setCytobandRegionEnd(cytobandRegionEnd);
         form.setCloneId(cloneId);
         form.setCloneListFile(cloneListFile);
         form.setCloneListSpecify(cloneListSpecify);
         form.setBasePairEnd(basePairEnd);
         form.setChromosomeNumber(chromosomeNumber);
         form.setGeneType(geneType);
         form.setResultView(resultView);
         form.setGeneFile(geneFile);
         form.setSampleFile(sampleFile);
         form.setGeneGroup(geneGroup);
         form.setSampleGroup(sampleGroup);
         form.setCloneList(cloneList);
         form.setQueryName(queryName);
         form.setBasePairStart(basePairStart);
         form.setSnpList(snpList);
         form.setCnAmplified(cnAmplified);
         form.setSmAmplified(smAmplified);
         form.setSnpListFile(snpListFile);
         form.setSnpListSpecify(snpListSpecify);
         form.setCnADAmplified(cnADAmplified);
         form.setGenomicTrack(genomicTrack);
         form.setCnADDeleted(cnADDeleted);
         form.setCnUnchangeTo(cnUnchangeTo);
         form.setSmUnchangeTo(smUnchangeTo);
         form.setAlleleFrequency(alleleFrequency);
         form.setValidatedSNP(validatedSNP);
         form.setSnpId(snpId);
         form.setCnDeleted(cnDeleted);
         form.setSmDeleted(smDeleted);
         form.setGeneGroup(geneGroup);
         form.setCnUnchangeFrom(cnUnchangeFrom);
         form.setSmUnchangeFrom(smUnchangeFrom);
         form.setCopyNumber(copyNumber);
         form.setCopyNumberView(copyNumberView);
         form.setSegmentMean(segmentMean);
         form.setAnalysisType(analysisType);
         form.setSpecimenType(specimenType);
         return form;
     }
 
 	public Collection getSavedGeneList() {
 		return savedGeneList;
 	}
 
 	public void setSavedGeneList(Collection savedGeneList) {
 		this.savedGeneList = savedGeneList;
 	}
 
 	public Collection getSavedSnpList() {
 		return savedSnpList;
 	}
 
 	public void setSavedSnpList(Collection savedSnpList) {
 		this.savedSnpList = savedSnpList;
 	}
 
 	/**
 	 * @return the specimenType
 	 */
 	public String getSpecimenType() {
 		return specimenType;
 	}
 
 	/**
 	 * @param specimenType the specimenType to set
 	 */
 	public void setSpecimenType(String specimenType) {
 		this.specimenType = specimenType;
 		
 		if (this.specimenType != null){
 			if(sampleCriteria == null){
 				sampleCriteria = new SampleCriteria();
 			}
 			if(specimenType.equalsIgnoreCase(SpecimenType.BLOOD.name()) ||
 					specimenType.equalsIgnoreCase(SpecimenType.BLOOD.toString())){
 						sampleCriteria.setSpecimenType(SpecimenType.BLOOD);
 					}
 			else if(specimenType.equalsIgnoreCase(SpecimenType.TISSUE_BRAIN.name()) ||
 					specimenType.equalsIgnoreCase(SpecimenType.TISSUE_BRAIN.toString())){
 						sampleCriteria.setSpecimenType(SpecimenType.TISSUE_BRAIN);
 					}
 			}
 	}
 
 	/**
 	 * @param analysisTypeCriteria the analysisTypeCriteria to set
 	 */
 	public void setAnalysisTypeCriteria(AnalysisTypeCriteria analysisTypeCriteria) {
 		this.analysisTypeCriteria = analysisTypeCriteria;
 	}
 
 	public Map<String, String> getAssayPlatforms() {
 		return assayPlatforms;
 	}
 
 	public void setAssayPlatforms(Map<String, String> assayPlatforms) {
 		this.assayPlatforms = assayPlatforms;
 	}
 
 	@Override
 	public String getCytobandsActionName() {
 		return "cgGetCytobands";
 	}
 	
 	@Override
 	public String getSubmitActionName() {
 		// TODO Auto-generated method stub
 		return "cgSubmit";
 	}
 
 	@Override
 	public String getPreviewActionName() {
 		// TODO Auto-generated method stub
 		return "cgPreview";
 	}
 
 	public String getGeneReviewChecked() {
 		return geneReviewChecked;
 	}
 
 	public void setGeneReviewChecked(String geneReviewChecked) {
 		this.geneReviewChecked = geneReviewChecked;
 	}
 
 	public String getRegionReviewChecked() {
 		return regionReviewChecked;
 	}
 
 	public void setRegionReviewChecked(String regionReviewChecked) {
 		this.regionReviewChecked = regionReviewChecked;
 	}
 
 	public String getSelectedView() {
 		return selectedView;
 	}
 
 	public void setSelectedView(String selectedView) {
 		this.selectedView = selectedView;
 	}
 	
 	public void setGeneRegionViewChange() {
 		if (selectedView == null || selectedView.equalsIgnoreCase("regionView")) {
 			this.geneReviewChecked = "";
 			this.regionReviewChecked = "checked";
 		} else {
 			this.geneReviewChecked = "checked";
 			this.regionReviewChecked = "";
 		} 
 	}
 }
