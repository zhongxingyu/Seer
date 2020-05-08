 /*
  * Created on Sep 13, 2004
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package gov.nih.nci.caintegrator.application.service.annotation;

 import gov.nih.nci.caintegrator.dto.de.BasePairPositionDE;
 import gov.nih.nci.caintegrator.dto.de.DatumDE;
 
 import java.util.Collection;
 import java.util.SortedMap;
 import java.util.TreeMap;
 /**
  * @author SahniH
  *
  This class encapulates a collection of SampleResultset objects.
  */
 
 
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
 
 public class ReporterResultset {
 	private DatumDE reporter = null;
     private DatumDE value = null;
     private BasePairPositionDE.StartPosition startPhysicalLocation = null;
 	private SortedMap groupTypes = new TreeMap();
     private Collection assiciatedGeneSymbols = null;
     private Collection assiciatedLocusLinkIDs =  null;
     private Collection assiciatedGenBankAccessionNos = null;
     private Collection associatedPathways = null;
     private Collection associatedGOIds = null;
 	/**
 	 * 
 	 */
 	public ReporterResultset(DatumDE repoter) {
 		setReporter(repoter);
 	}
 	/**
 	 * @param groupResultset Adds groupResultset to this ReporterResultset object.
 	 */
 	public void addGroupByResultset(Groupable groupResultset){
 		if(groupResultset != null && groupResultset.getType() != null){
 			groupTypes.put(groupResultset.getType().getValue().toString(), groupResultset);
 		}
 	}
 	/**
 	 * @param groupResultset Removes groupResultset to this ReporterResultset object.
 	 */
 	public void removeGroupByResultset(Groupable groupResultset){
 		if(groupResultset != null && groupResultset.getType() != null){
 			groupTypes.remove(groupResultset.getType().getValue().toString());
 		}
 	}
     /**
      * @param disease
 	 * @return groupResultset Returns reporterResultset for this ReporterResultset.
 	 */
     public Groupable getGroupByResultset(String groupType){
     	if(groupType != null){
 			return (Groupable) groupTypes.get(groupType);
 		}
     		return null;
     }
 	/**
 	 * @return Collection Returns collection of GroupResultsets to this ReporterResultset object.
 	 */
     public Collection getGroupByResultsets(){
     		return groupTypes.values();
     }
 	/**
 	 * @param none Removes all groupResultset in this ReporterResultset object.
 	 */
     public void removeAllGroupByResultset(){
     	groupTypes.clear();
     }
 	/**
 	 * @return Returns the reporterName.
 	 */
 	public DatumDE getReporter() {
 		return reporter;
 	}
 	/**
 	 * @param reporterID The reporterID to set.
 	 */
 	public void setReporter(DatumDE reporter) {
 		this.reporter = reporter;
 	}
 	/**
 	 * @return Returns the reporterType.
 	 */
 	public String getReporterType() {
 		return reporter.getType();
 	}
 	
 	/**
 	 * @return Returns the assiciatedGenBankAccessionNos.
 	 */
 	public Collection getAssiciatedGenBankAccessionNos() {
 		return assiciatedGenBankAccessionNos;
 	}
 	/**
 	 * @param assiciatedGenBankAccessionNos The assiciatedGenBankAccessionNos to set.
 	 */
 	public void setAssiciatedGenBankAccessionNos(
 			Collection assiciatedGenBankAccessionNos) {
 		this.assiciatedGenBankAccessionNos = assiciatedGenBankAccessionNos;
 	}
 	/**
 	 * @return Returns the assiciatedGeneSymbols.
 	 */
	public Collection getAssociatedGeneSymbols() {
 		return assiciatedGeneSymbols;
 	}
 	/**
 	 * @param assiciatedGeneSymbols The assiciatedGeneSymbols to set.
 	 */
	public void setAssociatedGeneSymbols(Collection assiciatedGeneSymbols) {
 		this.assiciatedGeneSymbols = assiciatedGeneSymbols;
 	}
 	/**
 	 * @return Returns the assiciatedLocusLinkIDs.
 	 */
	public Collection getAssociatedLocusLinkIDs() {
 		return assiciatedLocusLinkIDs;
 	}
 	/**
 	 * @param assiciatedLocusLinkIDs The assiciatedLocusLinkIDs to set.
 	 */
	public void setAssociatedLocusLinkIDs(Collection assiciatedLocusLinkIDs) {
 		this.assiciatedLocusLinkIDs = assiciatedLocusLinkIDs;
 	}
     public BasePairPositionDE.StartPosition getStartPhysicalLocation() {
         return startPhysicalLocation;
     }
     
     public void setStartPhysicalLocation(BasePairPositionDE.StartPosition startPhysicalLocation) {
         this.startPhysicalLocation = startPhysicalLocation;
     }
     public DatumDE getValue() {
         return value;
     }
     
     public void setValue(DatumDE value) {
         this.value = value;
     }
      
 	/**
 	 * @return Returns the associatedPathways.
 	 */
 	public Collection getAssociatedPathways() {
 		return associatedPathways;
 	}
 	/**
 	 * @param associatedPathways The associatedPathways to set.
 	 */
 	public void setAssociatedPathways(Collection associatedPathways) {
 		this.associatedPathways = associatedPathways;
 	}
 
 	/**
 	 * @return Returns the associatedGOIds.
 	 */
 	public Collection getAssociatedGOIds() {
 		return associatedGOIds;
 	}
 	/**
 	 * @param associatedGOIds The associatedGOIds to set.
 	 */
 	public void setAssociatedGOIds(Collection associatedGOIds) {
 		this.associatedGOIds = associatedGOIds;
 	}
 }
