 /*L
  * Copyright (c) 2006 SAIC, SAIC-F.
  *
  * Distributed under the OSI-approved BSD 3-Clause License.
  * See http://ncip.github.com/rembrandt/LICENSE.txt for details.
  */
 
 package gov.nih.nci.rembrandt.web.struts2.form;
 
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
 import gov.nih.nci.rembrandt.util.MoreStringUtils;
 import gov.nih.nci.rembrandt.web.bean.ChromosomeBean;
 import gov.nih.nci.rembrandt.web.bean.SessionQueryBag;
 import gov.nih.nci.rembrandt.web.helper.GroupRetriever;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 //import org.apache.struts.action.ActionError;
 //import org.apache.struts.action.ActionErrors;
 //import org.apache.struts.action.ActionMapping;
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
 
 public class GeneExpressionForm extends BaseForm implements Serializable, Cloneable {
 	private static final long serialVersionUID = 1L;
 	private static Logger logger = Logger.getLogger(GeneExpressionForm.class);
 	// --------------------------------------------------------- Instance
 	// Variables
 	/** selected chromosomes cytobands **/
 	
 	private List cytobands = new ArrayList();
 		
 	/** chromosomes property */
 	private static List<ChromosomeBean> chromosomes;
 
 	/** geneOption property */
 	private String geneOption = "standard";
     
     private static Collection savedGeneList;
     
     private static Collection savedCloneList;
     
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
 	private transient String cloneListFile;
 
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
 	private String resultView="";
 
 	/** geneFile property */
 	private transient String geneFile;
 	
 
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
 	// default, on the jsp, it may be commented out for now
 	private UntranslatedRegionCriteria untranslatedRegionCriteria;		
 
 	private boolean excludeResections = false;
 	private transient SessionQueryBag queryCollection;
 	
 	private boolean isAllGenes = false;
     protected transient HttpServletRequest thisRequest; 
     
     
 
 
 	// --------------------------------------------------------- Methods
 	public GeneExpressionForm() {
 
 		// Create Lookups for Gene Expression screens
 		super();
 		setGeneExpressionLookup();        
 	}
 
 	public void setGeneExpressionLookup() {
 
 		// diseaseType = new ArrayList();// moved to the upper class:
 		// BaseForm.java
 		// geneTypeColl = new ArrayList();// moved to the upper class:
 		// BaseForm.java
 		cloneTypeColl = new ArrayList();
 		arrayPlatformTypeColl = new ArrayList();
 
 		geneTypeColl.add( new LabelValueBean( "Locus Link Id", "genelocus" ) );
 		geneTypeColl.add( new LabelValueBean( "GenBank AccNo.", "genbankno" ) );
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
 
 	//Shan: need to call explicitly from somewhere
 	public void reset(HttpServletRequest request) {
 		//geneOption = "";
 		//pathwayName = new String[0];
 		
 		super.reset(request);
 		
 		GroupRetriever groupRetriever = new GroupRetriever();
 	    savedGeneList = groupRetriever.getGeneGroupsCollection(request.getSession());
 	    savedCloneList = groupRetriever.getCloneGroupsCollection(request.getSession());
 	    
 		geneList = "";
 		goBiologicalProcess = "";
 		tumorGrade = "";
 		region = "";
 		foldChangeValueDown = "2";
 		cytobandRegionStart = "";
 		
 		pathways = "";
 		//tumorType = "";
 		arrayPlatform = "";
 		
 		cloneId = "";
 		cloneListFile = "";
 		
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
 		//geneFile = "";
 		foldChangeValueUDDown = "2";
 		//geneGroup = "";
 		cloneList = "";
 		//queryName = "";
 		basePairStart = "";
 		//sampleGroup = "";
 		sampleList = "";
 		//sampleFile = null;
 
 		// Set the Request Object
 		this.thisRequest = request;	 
 
 		diseaseOrGradeCriteria = new DiseaseOrGradeCriteria();
 		geneCriteria = new GeneIDCriteria();
 		foldChangeCriteria = new FoldChangeCriteria();
 		regionCriteria = new RegionCriteria();
 		cloneOrProbeIDCriteria = new CloneOrProbeIDCriteria();
 		geneOntologyCriteria = new GeneOntologyCriteria();
 		pathwayCriteria = new PathwayCriteria();
 		arrayPlatformCriteria = new ArrayPlatformCriteria();
 		allGenesCriteria = new AllGenesCriteria(isAllGenes);
 		sampleCriteria = new SampleCriteria();
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
 
 	public void setGeneListDE(Collection<GeneIdentifierDE> geneIdentifiers) {
 		StringBuffer geneBuffer = new StringBuffer();
 		
 		for( GeneIdentifierDE ge :  geneIdentifiers ) {
 			geneBuffer.append( ge.getValueObject() + "," );
 		}
 			
 			this.geneList = geneBuffer.toString().substring(0,geneBuffer.toString().length()-1);
 	}
 	/**
 	 * Set the geneList.
 	 * 
 	 * @param geneList
 	 *            The geneList to set
 	 */
 	
 	
 	public void setGeneList(String geneList) {
 		this.geneList = geneList;
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
 
 	/**
 	 * Sets the geneOption
 	 * 
 	 * @return String
 	 */
 	public void setGeneOption(String geneOption) {
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
 	public String getGeneOption() {
 		return geneOption;
 	}
 
 	
 	/**
 	 * Returns the geneFile.
 	 * 
 	 * @return String
 	 */
 	public String getGeneFile() {
 		return geneFile;
 	}
 
 	
 
 	/**
 	 * Set the geneFile.
 	 * 
 	 * @param geneFile
 	 *            The geneFile to set
 	 */
 	public void setGeneFile(String geneFile) {
 		this.geneFile = geneFile;	
 	}
 
 	public GeneIDCriteria getGeneIDCriteria() {
 		return this.geneCriteria;
 	}
 
 	public void setGeneIDCriteria( GeneIDCriteria geneCriteria ) {
 		this.geneCriteria = geneCriteria;
 	}
 
 	public AllGenesCriteria getAllGenesCriteria() {
 		return this.allGenesCriteria;
 	}
 
 	
 
 	public FoldChangeCriteria getFoldChangeCriteria() {
 		return this.foldChangeCriteria;
 	}
 
 	public void setFoldChangeCriteria( FoldChangeCriteria foldChangeCriteria) {
 		this.foldChangeCriteria = foldChangeCriteria;
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
 
 	public void setCloneOrProbeIDCriteria( CloneOrProbeIDCriteria cloneOrProbeIDCriteria) {
 		this.cloneOrProbeIDCriteria = cloneOrProbeIDCriteria;
 	}
 
 	public GeneOntologyCriteria getGeneOntologyCriteria() {
 		return this.geneOntologyCriteria;
 	}
 
 	public void setGeneOntologyCriteria( GeneOntologyCriteria geneOntologyCriteria) {
 		this.geneOntologyCriteria = geneOntologyCriteria;
 	}
 
 	public PathwayCriteria getPathwayCriteria() {
 		return this.pathwayCriteria;
 	}
 
 	public void setPathwayCriteria( PathwayCriteria pathwayCriteria) {
 		this.pathwayCriteria = pathwayCriteria;
 	}
 
 	public ArrayPlatformCriteria getArrayPlatformCriteria() {
 		return this.arrayPlatformCriteria;
 	}
 
 	public void setArrayPlatformCriteria( ArrayPlatformCriteria arrayPlatformCriteria) {
 		this.arrayPlatformCriteria = arrayPlatformCriteria;
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
 	}
 		
 	public void setFoldChangeValueDownDetails() {
 		
 		//if (thisRequest != null) {
 		String thisRegulationStatus = this.regulationStatus;
 		//this.thisRequest.getParameter("regulationStatus");
 
 		if (thisRegulationStatus != null
 				&& thisRegulationStatus.equalsIgnoreCase("down")
 				&& (this.foldChangeValueDown.length() > 0)){
 
 			try {
 				ExprFoldChangeDE foldChangeDEObj = new ExprFoldChangeDE.DownRegulation(Float.valueOf(this.foldChangeValueDown));
 				foldChangeCriteria.setFoldChangeObject(foldChangeDEObj);
 			}
 			catch(NumberFormatException e){}
 
 		}
 		//}
 
 	}
 
 	//Shan: Need to do this later
 //	public void setFoldChangeValueDown(ExprFoldChangeDE exprFoldChangeDE) {
 //		if ( exprFoldChangeDE != null ) {
 //			this.foldChangeValueDown = exprFoldChangeDE.getValueObject().toString();
 //		}
 //	}	
 
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
 
 //	public void setCytobandRegionStart(CytobandDE cytobandDE) {
 //		if ( cytobandDE != null )
 //			this.cytobandRegionStart = cytobandDE.getValueObject();
 //	}
 	
 //	public void setCytobandRegionEnd(CytobandDE cytobandDE) {
 //		if ( cytobandDE != null )
 //			this.cytobandRegionEnd = cytobandDE.getValueObject();
 //	}
 
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
 		if (cloneListSpecify != null )
 			cloneListSpecify = MoreStringUtils.cleanJavascript(cloneListSpecify);
 
 		this.cloneListSpecify = cloneListSpecify;
 	}
 	
 	public void setCloneListSpecifyDetails() {
 		
 		//Shan: Need to change this somewhere else
 //		if (thisRequest != null) {
 			// this is to check if the radio button is selected for the clone
 			// category
 			String thisCloneId = this.cloneId; //(String) thisRequest.getParameter("cloneId");
 
 			// this is to check the type of the clone
 			String thisCloneList = this.cloneList; ///(String) thisRequest.getParameter("cloneList");
 
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
 	}
 	
 	public void setGoClassificationDetails() {
 		String goSelect = this.goClassification;// (String) thisRequest.getParameter("goClassification");
 		
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
 	
 	public void setChromosomeRegionCriteria(String chromosomeIndex) {
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
 	 * Returns the regulationStatus.
 	 * 
 	 * @return String
 	 */
 	public String getRegulationStatus() {
 		return regulationStatus;
 	}
 
 	public void setChromosomeNumber(String chromosomeNumber) {
 		this.chromosomeNumber = chromosomeNumber;
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
 	}
 	
 	public void setFoldChangeValueUnchangeFromDetails() {
 		
 		//Shan: Need to change this somewhere else
 //		if (thisRequest != null) {
 		String thisRegulationStatus = this.regulationStatus;
 		//this.thisRequest.getParameter("regulationStatus");
 
 		if (thisRegulationStatus != null
 				&& thisRegulationStatus.equalsIgnoreCase("unchange")
 				&& (this.foldChangeValueUnchangeFrom.length() > 0)){
 			try {	
 				ExprFoldChangeDE foldChangeDEObj = new ExprFoldChangeDE.UnChangedRegulationDownLimit(Float.valueOf(this.foldChangeValueUnchangeFrom));
 				foldChangeCriteria.setFoldChangeObject(foldChangeDEObj);		
 
 			}
 			catch(NumberFormatException e){}
 
 		}
 //		}
 	}
 	
 //	public void setFoldChangeValueUnchangeFrom(ExprFoldChangeDE exprFoldChangeDE) {
 //		if ( exprFoldChangeDE != null ) {
 //			this.foldChangeValueUnchangeFrom = exprFoldChangeDE.getValueObject().toString();
 //		}
 //	}	
 	
 
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
 	}
 	
 	public void setFoldChangeValueUnchangeToDetails() {
 		
 		//Shan: Need to change this somewhere else
 //		if (thisRequest != null) {
 		String thisRegulationStatus = this.regulationStatus;
 				//this.thisRequest.getParameter("regulationStatus");
 
 		if (thisRegulationStatus != null
 				&& thisRegulationStatus.equalsIgnoreCase("unchange")
 				&& (this.foldChangeValueUnchangeTo.length() > 0)) {
 			try {
 				ExprFoldChangeDE foldChangeDEObj = new ExprFoldChangeDE.UnChangedRegulationUpperLimit(Float.valueOf(this.foldChangeValueUnchangeTo));
 				foldChangeCriteria.setFoldChangeObject(foldChangeDEObj);
 			}
 			catch(NumberFormatException e){}
 		}
 //		}
 	}
 	
 //	public void setFoldChangeValueUnchangeTo(ExprFoldChangeDE exprFoldChangeDE) {
 //		if ( exprFoldChangeDE != null ) {
 //			this.foldChangeValueUnchangeTo = exprFoldChangeDE.getValueObject().toString();
 //		}
 //	}	
 	
 
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
 	}
 	
 	public void setFoldChangeValueUpDetails() {
 		
 		//Shan: Need to change this somewhere else
 //		if (thisRequest != null) {
 		String thisRegulationStatus = this.regulationStatus;
 				//this.thisRequest.getParameter("regulationStatus");
 
 
 		if (thisRegulationStatus != null
 				&& thisRegulationStatus.equalsIgnoreCase("up")
 				&& (this.foldChangeValueUp.length() > 0)){
 
 			try {
 
 				ExprFoldChangeDE foldChangeDEObj = new ExprFoldChangeDE.UpRegulation(Float.valueOf(this.foldChangeValueUp));
 				foldChangeCriteria.setFoldChangeObject(foldChangeDEObj);		
 
 			}
 
 			catch(NumberFormatException e){}
 		}
 //		}
 	}
 
 	//Shan: Need to change this somewhere else
 //	public void setFoldChangeValueUp(ExprFoldChangeDE exprFoldChangeDE) {
 //		if ( exprFoldChangeDE != null ) {
 //			this.foldChangeValueUp = exprFoldChangeDE.getValueObject().toString();
 //		}
 //	}	
 	
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
 	}
 	
 	public void setFoldChangeValueUDUpDetails() {
 		
 		//Shan: Need to change this somewhere else
 //		if (thisRequest != null) {
 		String thisRegulationStatus = this.regulationStatus;
 				//this.thisRequest.getParameter("regulationStatus");
 		logger
 		.debug("I am in the setFoldChangeValueUDUp()  thisRegulationStatus:"
 				+ thisRegulationStatus);
 
 		if (thisRegulationStatus != null
 				&& thisRegulationStatus.equalsIgnoreCase("updown")
 				&& (this.foldChangeValueUDUp.length() > 0)) {
 			try {
 				ExprFoldChangeDE foldChangeDEObj = new ExprFoldChangeDE.UpRegulation(Float.valueOf(this.foldChangeValueUDUp));
 				foldChangeCriteria.setFoldChangeObject(foldChangeDEObj);	
 
 			}
 			catch(NumberFormatException e){}
 		}
 //		}
 	}
 	
 //	public void setFoldChangeValueUDUp(ExprFoldChangeDE exprFoldChangeDE) {
 //		if ( exprFoldChangeDE != null ) {
 //			this.foldChangeValueUDUp = exprFoldChangeDE.getValueObject().toString();
 //		}
 //	}	
 	
 
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
 	}
 	
 	public void setFoldChangeValueUDDownDetails() {
 		
 		//Shan: Need to change this somewhere else
 //		if (thisRequest != null) {
 		String thisRegulationStatus = this.regulationStatus;
 				//this.thisRequest.getParameter("regulationStatus");
 		logger.debug("I am in the setFoldChangeValueUDDown() methid: "
 				+ thisRegulationStatus);
 
 		if (thisRegulationStatus != null
 				&& thisRegulationStatus.equalsIgnoreCase("updown")
 				&& (this.foldChangeValueUDDown.length() > 0)){
 
 			try {
 				ExprFoldChangeDE foldChangeDEObj = new ExprFoldChangeDE.DownRegulation(Float.valueOf(this.foldChangeValueUDDown));
 				foldChangeCriteria.setFoldChangeObject(foldChangeDEObj);
 
 			}
 			catch(NumberFormatException e){}
 		}
 //				
 //		}
 
 	}
 	
 //	public void setFoldChangeValueUDDown(ExprFoldChangeDE exprFoldChangeDE) {
 //		if ( exprFoldChangeDE != null ) {
 //			this.foldChangeValueUDDown = exprFoldChangeDE.getValueObject().toString();
 //		}
 //	}	
 	
 
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
 		if (queryName != null )
 			queryName = MoreStringUtils.cleanJavascript(queryName);
 		
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
 
 		form.setGeneOption(geneOption);
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
 		form.setExcludeResections(excludeResections);
 		
 
 		return form;
 	}
 	
 //	public GeneExpressionForm combineFields(GeneExpressionForm to, GeneExpressionForm from) {
 //		GeneExpressionForm form = new GeneExpressionForm();
 //		
 //		String val = from.getGeneOption();
 //		if (val != null && val.length() > 0)
 //			to.setGeneOption(val);
 //		
 //		String[] vals = from.getPathwayName();
 //		if (vals != null && vals.length > 0)
 //			to.setPathwayName(pathwayName);
 //		
 //		val = from.getGeneList();
 //		if (val != null && val.length() > 0)
 //			to.setGeneList(val);
 //		
 //		val = from.getSampleList();
 //		if (val != null && val.length() > 0)
 //			to.setSampleList(val);
 //		
 //		val = from.getGoClassification();
 //		if (val != null && val.length() > 0)
 //			to.setGoClassification(val);
 //		
 //		val = from.getGoCellularComp();
 //		if (val != null && val.length() > 0)
 //			to.setGoCellularComp(val);
 //		
 //		val = from.getGoMolecularFunction();
 //		if (val != null && val.length() > 0)
 //			to.setGoMolecularFunction(val);
 //		
 //		val = from.getGoCellularComp();
 //		if (val != null && val.length() > 0)
 //			to.setGoCellularComp(val);
 //		
 //		val = from.getTumorGrade();
 //		if (val != null && val.length() > 0)
 //			to.setTumorGrade(val);
 //		
 //		val = from.getRegion();
 //		if (val != null && val.length() > 0)
 //			to.setRegion(val);
 //		
 //		val = from.getFoldChangeValueDown();
 //		if (val != null && val.length() > 0)
 //			to.setFoldChangeValueDown(val);
 //		
 //		val = from.getCytobandRegionStart();
 //		if (val != null && val.length() > 0)
 //			to.setCytobandRegionStart(val);
 //		
 //		val = from.getCytobandRegionEnd();
 //		if (val != null && val.length() > 0)
 //			to.setCytobandRegionEnd(val);
 //		
 //		val = from.getCloneId();
 //		if (val != null && val.length() > 0)
 //			to.setCloneId(val);
 //		
 //		val = from.getPathways();
 //		if (val != null && val.length() > 0)
 //			to.setPathways(val);
 //		
 //		vals = from.getTumorType();
 //		if (vals != null && vals.length > 0)
 //			to.setTumorType(vals);
 //		
 //		val = from.getArrayPlatform();
 //		if (val != null && val.length() > 0)
 //			to.setArrayPlatform(val);
 //		
 //		val = from.getCloneListFile();
 //		if (val != null && val.length() > 0)
 //			to.setCloneListFile(val);
 //		
 //		val = from.getCloneListSpecify();
 //		if (val != null && val.length() > 0)
 //			to.setCloneListSpecify(val);
 //		
 //		val = from.getBasePairEnd();
 //		if (val != null && val.length() > 0)
 //			to.setBasePairEnd(val);
 //		
 //		val = from.getChromosomeNumber();
 //		if (val != null && val.length() > 0)
 //			to.setChromosomeNumber(val);
 //		
 //		val = from.getRegulationStatus();
 //		if (val != null && val.length() > 0)
 //			to.setRegulationStatus(val);
 //		
 //		val = from.getFoldChangeValueUnchangeFrom();
 //		if (val != null && val.length() > 0)
 //			to.setFoldChangeValueUnchangeFrom(val);
 //		
 //		val = from.getFoldChangeValueUnchangeTo();
 //		if (val != null && val.length() > 0)
 //			to.setFoldChangeValueUnchangeTo(val);
 //		
 //		val = from.getFoldChangeValueUp();
 //		if (val != null && val.length() > 0)
 //			to.setFoldChangeValueUp(val);
 //		
 //		val = from.getGeneType();
 //		if (val != null && val.length() > 0)
 //			to.setGeneType(val);
 //		
 //		val = from.getFoldChangeValueUDUp();
 //		if (val != null && val.length() > 0)
 //			to.setFoldChangeValueUDUp(val);
 //		
 //		val = from.getResultView();
 //		if (val != null && val.length() > 0)
 //			to.setResultView(val);
 //		
 //		val = from.getGeneFile();
 //		if (val != null && val.length() > 0)
 //			to.setGeneFile(val);
 //		
 //		val = from.getSampleFile();
 //		if (val != null && val.length() > 0)
 //			to.setSampleFile(val);
 //		
 //		val = from.getFoldChangeValueUDDown();
 //		if (val != null && val.length() > 0)
 //			to.setFoldChangeValueUDDown(val);
 //		
 //		val = from.getGeneGroup();
 //		if (val != null && val.length() > 0)
 //			to.setGeneGroup(val);
 //		
 //		val = from.getSampleGroup();
 //		if (val != null && val.length() > 0)
 //			to.setSampleGroup(val);
 //		
 //		val = from.getCloneList();
 //		if (val != null && val.length() > 0)
 //			to.setCloneList(val);
 //		
 //		val = from.getQueryName();
 //		if (val != null && val.length() > 0)
 //			to.setQueryName(val);
 //		
 //		val = from.getBasePairStart();
 //		if (val != null && val.length() > 0)
 //			to.setBasePairStart(val);
 //		
 ////		val = from.getQueryCollection();
 ////		if (val != null && val.length() > 0)
 ////			to.setQueryCollection(val);
 ////		
 ////		val = from.getCytobandRegionStart();
 ////		if (val != null && val.length() > 0)
 ////			to.setExcludeResections(val);
 //		
 //
 //		return form;
 //	}
 
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
 
 
 	public Collection getSavedGeneList() {
 		return savedGeneList;
 	}
 
 
 	public void setSavedGeneList(Collection savedGeneList) {
 		this.savedGeneList = savedGeneList;
 	}
 
 
 	public Collection getSavedCloneList() {
 		return savedCloneList;
 	}
 
 
 	public static void setSavedCloneList(Collection savedCloneList) {
 		GeneExpressionForm.savedCloneList = savedCloneList;
 	}
 
 
 	/**
 	 * @return the excludeResections
 	 */
 	public boolean getExcludeResections() {
 		return excludeResections;
 	}
 
 
 	/**
 	 * @param excludeResections the excludeResections to set
 	 */
 	public void setExcludeResections(boolean excludeResections) {
 		this.excludeResections = excludeResections;
 	}
 
 	public void setUpGeneAndCloneList(HttpServletRequest request) {
 		GroupRetriever groupRetriever = new GroupRetriever();
 	    savedGeneList = groupRetriever.getGeneGroupsCollection(request.getSession());
 	    savedCloneList = groupRetriever.getCloneGroupsCollection(request.getSession());
 	}
 
 	
 	public void setTumorType(String[] tumorType) {
 		super.setTumorType(tumorType);
 	}
 
 	@Override
 	public void setSampleList(String sampleList) {
 		// TODO Auto-generated method stub
 		super.setSampleList(sampleList);
 	}
 
 	@Override
 	public void setSampleFile(String sampleFile) {
 		// TODO Auto-generated method stub
 		super.setSampleFile(sampleFile);
 	}
 
 	@Override
 	public void setSampleGroup(String sampleGroup) {
 		// TODO Auto-generated method stub
 		super.setSampleGroup(sampleGroup);
 	}
 
 	@Override
 	public void setTumorGrades(List<String> tumorGradeList) {
 		// TODO Auto-generated method stub
 		super.setTumorGrades(tumorGradeList);
 	}
 	
 
 	@Override
 	public ArrayList<LabelValueBean> getGeneTypeColl() {
 		// TODO Auto-generated method stub
 		return super.getGeneTypeColl();
 	}
 
 	@Override
 	public void setGeneTypeColl(ArrayList<LabelValueBean> geneTypeColl) {
 		// TODO Auto-generated method stub
 		super.setGeneTypeColl(geneTypeColl);
 	}
 
 	@Override
 	protected Object clone() throws CloneNotSupportedException {
 		// TODO Auto-generated method stub
 		return super.clone();
 	}
 	
 	
 	@Override
 	public String getCytobandsActionName() {
 		return "geGetCytobands";
 	}
 
 	@Override
 	public String getSubmitActionName() {
 		// TODO Auto-generated method stub
 		return "geSubmit";
 	}
 
 	@Override
 	public String getPreviewActionName() {
 		// TODO Auto-generated method stub
 		return "gePreview";
 	}
 	
 	
 	
 }
