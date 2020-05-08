 package gov.nih.nci.rembrandt.queryservice.validation;
 
 import gov.nih.nci.caintegrator.dto.de.CloneIdentifierDE;
 import gov.nih.nci.caintegrator.dto.de.GeneIdentifierDE;
 import gov.nih.nci.caintegrator.dto.de.SNPIdentifierDE;
 import gov.nih.nci.caintegrator.dto.de.SampleIDDE;
 import gov.nih.nci.rembrandt.dbbean.AccessionNo;
 import gov.nih.nci.rembrandt.dbbean.AllGeneAlias;
 import gov.nih.nci.rembrandt.dbbean.CloneDim;
 import gov.nih.nci.rembrandt.dbbean.GEPatientData;
 import gov.nih.nci.rembrandt.dbbean.GeneLlAccSnp;
 import gov.nih.nci.rembrandt.dbbean.LocusLink;
 import gov.nih.nci.rembrandt.dbbean.PatientData;
 import gov.nih.nci.rembrandt.dbbean.ProbesetDim;
 import gov.nih.nci.rembrandt.dbbean.SnpProbesetDim;
 import gov.nih.nci.rembrandt.dto.lookup.AccessionNoLookup;
 import gov.nih.nci.rembrandt.dto.lookup.AllGeneAliasLookup;
 import gov.nih.nci.rembrandt.dto.lookup.LocusLinkLookUp;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.apache.ojb.broker.query.Criteria;
 
 /**
  * This class provide validation functionalty for Genes, Reporters and Samples
  * 
  * @author SahniH
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
 
 public class DataValidator{
     private static Logger logger = Logger.getLogger(DataValidator.class);
 
 
 	
 	/**
 	 * Performs the actual lookup query.  Gets the application
 	 * PersistanceBroker and then passes to the 
 	 * @param bean the lookup class 
 	 * @param crit the criteria for the lookup
 	 * @return the collection of lookup values
 	 * @throws Exception
 	 */
 	 
 
     /*private static void getAllGeneAlias() throws Exception{
     	Criteria crit = new Criteria();
 		Collection allGeneAlias = executeQuery(AllGeneAlias.class, (Criteria)crit,LookupManager.ALLGENEALIAS,true);
 		geneSymbols =  new HashSet();
 		for (Iterator iterator = allGeneAlias.iterator(); iterator.hasNext();) {
 			AllGeneAlias geneAlias = (AllGeneAlias) iterator.next();
 			geneSymbols.add(geneAlias.getApprovedSymbol().trim());
 		 }
     }*/
     public static boolean isGeneSymbolFound(String geneSymbol) throws Exception{
 
     	if(geneSymbol != null  ){
             
             if(geneSymbol.indexOf("*")!= -1 || geneSymbol.indexOf("%") != -1){
                 return false;         //make sure your not checking for wildcards
             }
             try {
         	//Create a Criteria for Approved Symbol
             Criteria approvedSymbolCrit = new Criteria();
             approvedSymbolCrit.addLike("upper(approvedSymbol)",geneSymbol.toUpperCase());
             Collection geneCollection;
 	    		
 	    			geneCollection = QueryExecuter.executeQuery(AllGeneAlias.class, approvedSymbolCrit,QueryExecuter.NO_CACHE,true);
 
 		    		if(geneCollection != null && geneCollection.size() == 1){
 		            	return true;
 		            }
 	    		} catch (Exception e) {
 	    			logger.error("Error in geneCollection when searching for "+geneSymbol);
 	    			logger.error(e.getMessage());
 	    			return false;
 	    		}
     	}
     	return false;
     }
     public static Collection<SampleIDDE> validateSampleIds(Collection<SampleIDDE> sampleIds) throws Exception{
     	Collection<SampleIDDE> validSampleList = new ArrayList<SampleIDDE>();
     	if(sampleIds != null  && sampleIds.size() > 0){
             
 
             try {
         	//Create a Criteria for Approved Symbol
            
             Collection<String> values = new ArrayList<String>();
             for (SampleIDDE sampleId : sampleIds){
                 if(sampleId.getValueObject().indexOf("*")!= -1 || sampleId.getValueObject().indexOf("%") != -1){
                     throw new Exception("Sample Id"+ sampleId+ "contains * or %");         //make sure your not checking for wildcards
                 }
                 values.add(sampleId.getValueObject().toUpperCase());
             }
 
 
 	            Criteria sampleCrit = new Criteria();
 	            sampleCrit.addIn("upper(sampleId)",values);	
 	            Collection sampleCollection = QueryExecuter.executeQuery(PatientData.class, sampleCrit,QueryExecuter.NO_CACHE,true);
 
             	if(sampleCollection != null){
             		 for (Object obj : sampleCollection){
             			 if(obj instanceof PatientData){
             				 PatientData pateintData = (PatientData) obj;
             				 validSampleList.add(new SampleIDDE(pateintData.getSampleId()));
             			 }
             		 }
             	}
 
 	    		} catch (Exception e) {
 	    			logger.error("Error in validateSampleIds");
 	    			logger.error(e.getMessage());
 	    			throw e;
 	    		}
     	}
     	return validSampleList;
     }
     public static List<String> validateSampleIdsForGEData(Collection<String> sampleIds) throws Exception{
     	List<String> validSampleList = new ArrayList<String>();
     	if(sampleIds != null  && sampleIds.size() > 0){
             
 
             try {
         	//Create a Criteria for Approved Symbol
            
             Collection<String> values = new ArrayList<String>();
             for (String sampleId : sampleIds){
                 if(sampleId.indexOf("*")!= -1 || sampleId.indexOf("%") != -1){
                     throw new Exception("Sample Id"+ sampleId+ "contains * or %");         //make sure your not checking for wildcards
                 }
                 values.add(sampleId.toUpperCase());
             }
 
 
 
 	            Criteria sampleCrit = new Criteria();
 	            sampleCrit.addIn("upper(sampleId)",values);	
 	            Collection geSampleCollection = QueryExecuter.executeQuery(GEPatientData.class, sampleCrit,QueryExecuter.NO_CACHE,true);
             	if(geSampleCollection != null){
             		 for (Object obj : geSampleCollection){
             			 if(obj instanceof GEPatientData){
             				 GEPatientData pateintData = (GEPatientData) obj;
             				 validSampleList.add(pateintData.getSampleId());
             			 }
             		 }
             	}
 
 	    		} catch (Exception e) {
 	    			logger.error("Error in validateSampleIds");
 	    			logger.error(e.getMessage());
 	    			throw e;
 	    		}
     	}
     	return validSampleList;
     }
     public static Collection<CloneIdentifierDE> validateReporters(Collection<CloneIdentifierDE> reporterIds) throws Exception{
     	List<CloneIdentifierDE> validList = new ArrayList<CloneIdentifierDE>();
 	   	String type = null;
         Collection collection = null;
     	if(reporterIds != null  ){
 
 
             try {
         	//Create a Criteria for Approved Symbol
             Criteria crit = new Criteria();
             Collection<String> values = new ArrayList<String>();
             for (CloneIdentifierDE reporterId : reporterIds){
             	type = reporterId.getCloneIDType();
             	values.add(reporterId.getValueObject().toUpperCase());
             	}
 
             if(type != null && type.equals(CloneIdentifierDE.IMAGE_CLONE)){
             	crit.addIn("upper(cloneName)",values);
             	collection = QueryExecuter.executeQuery(CloneDim.class, crit,QueryExecuter.NO_CACHE,true);
             }
             else if (type != null && type.equals(CloneIdentifierDE.PROBE_SET)){
             	crit.addIn("upper(probesetName)",values);
             	collection = QueryExecuter.executeQuery(ProbesetDim.class, crit,QueryExecuter.NO_CACHE,true);
             }
             	if(collection != null){
             		 for (Object obj : collection){
             			 if(obj instanceof CloneDim){
             				 CloneDim reporter = (CloneDim) obj;
             				 validList.add(new CloneIdentifierDE.IMAGEClone(reporter.getCloneName()));
             			 }
             			 else if(obj instanceof ProbesetDim){
             				 ProbesetDim reporter = (ProbesetDim) obj;
             				 validList.add(new CloneIdentifierDE.ProbesetID(reporter.getProbesetName()));
             			 }
             		 }
             	}
 
 	    		} catch (Exception e) {
 	    			logger.error("Error in validateReporters");
 	    			logger.error(e.getMessage());
 	    			throw e;
 	    		}
     	}
     	return validList;
     }
     public static Collection<SNPIdentifierDE> validateSNPReporters(Collection<SNPIdentifierDE> reporterIds) throws Exception{
     	List<SNPIdentifierDE> validList = new ArrayList<SNPIdentifierDE>();
 	   	String type = null;
         Collection collection = null;
     	if(reporterIds != null  ){
 
 
             try {
         	//Create a Criteria for Approved Symbol
             Criteria crit = new Criteria();
             Collection<String> values = new ArrayList<String>();
             for (SNPIdentifierDE reporterId : reporterIds){
             	type = reporterId.getSNPType();
             	values.add(reporterId.getValueObject().toUpperCase());
             	}
 
             if(type != null && type.equals(SNPIdentifierDE.DBSNP)){
             	crit.addIn("upper(dbSnpId)",values);
             	collection = QueryExecuter.executeQuery(SnpProbesetDim.class, crit,QueryExecuter.NO_CACHE,true);
             }
             else if (type != null && type.equals(SNPIdentifierDE.SNP_PROBESET)){
             	crit.addIn("upper(probesetName)",values);
             	collection = QueryExecuter.executeQuery(SnpProbesetDim.class, crit,QueryExecuter.NO_CACHE,true);
             }
             	if(collection != null){
             		 for (Object obj : collection){
             			 if(obj instanceof SnpProbesetDim  && type.equals(SNPIdentifierDE.DBSNP)){
             				 SnpProbesetDim reporter = (SnpProbesetDim) obj;
             				 validList.add(new SNPIdentifierDE.DBSNP(reporter.getDbSnpId()));
             			 }
             			 else if(obj instanceof SnpProbesetDim  && type.equals(SNPIdentifierDE.SNP_PROBESET)){
             				 SnpProbesetDim reporter = (SnpProbesetDim) obj;
             				 validList.add(new SNPIdentifierDE.SNPProbeSet(reporter.getProbesetName()));
             			 }
             		 }
             	}
 
 	    		} catch (Exception e) {
 	    			logger.error("Error in validateReporters");
 	    			logger.error(e.getMessage());
 	    			throw e;
 	    		}
     	}
     	return validList;
     }
     public static Collection<GeneIdentifierDE> validateGenes(Collection<GeneIdentifierDE> geneList) throws Exception{
     	Collection<GeneIdentifierDE> validList = new ArrayList<GeneIdentifierDE>();
 	   	String type = null;
         Collection collection = null;
     	if(geneList != null  ){
 
 
             try {
         	//Create a Criteria for Approved Symbol
             Criteria crit = new Criteria();
             Collection<String> values = new ArrayList<String>();
             for (GeneIdentifierDE geneID : geneList){
             	type = geneID.getGeneIDType();
             	values.add(geneID.getValueObject().toUpperCase());
             }
 
             if(type != null && type.equals(GeneIdentifierDE.GENESYMBOL)){
             	crit.addIn("upper(approvedSymbol)",values);
             	collection = QueryExecuter.executeQuery(AllGeneAlias.class, crit,QueryExecuter.NO_CACHE,true);
             }
             else if (type != null && type.equals(GeneIdentifierDE.GENBANK_ACCESSION_NUMBER)){
             	crit.addIn("upper(accession)",values);            	 
             	collection = QueryExecuter.executeQuery(AccessionNo.class, crit,QueryExecuter.NO_CACHE,true);
             }
             else if (type != null && type.equals(GeneIdentifierDE.LOCUS_LINK)){
             	crit.addIn("upper(ll_id)",values);            	 
             	collection = QueryExecuter.executeQuery(LocusLink.class, crit,QueryExecuter.NO_CACHE,true);
             }
             	if(collection != null){
             		 for (Object obj : collection){
             			 if(obj instanceof AllGeneAliasLookup){
             				 AllGeneAliasLookup gene = (AllGeneAliasLookup) obj;
             				 validList.add(new GeneIdentifierDE.GeneSymbol(gene.getApprovedSymbol()));
             			 }            			
             			 else if(obj instanceof AccessionNoLookup  && type.equals(GeneIdentifierDE.GENBANK_ACCESSION_NUMBER)){
             				 AccessionNoLookup reporter = (AccessionNoLookup) obj;
             				 validList.add(new GeneIdentifierDE.GenBankAccessionNumber(reporter.getAccession()));
             			 }
             			 else if(obj instanceof LocusLinkLookUp  && type.equals(GeneIdentifierDE.LOCUS_LINK)){
             				 LocusLinkLookUp reporter = (LocusLinkLookUp) obj;
             				 validList.add(new GeneIdentifierDE.LocusLink(reporter.getLl_id()));
             			 }
             		
             		 }
             	}
 
 	    		} catch (Exception e) {
 	    			logger.error("Error in validateReporters");
 	    			logger.error(e.getMessage());
 	    			throw e;
 	    		}
     	}
     	return validList;
     }
 	@SuppressWarnings("unchecked")
 	public static AllGeneAliasLookup[] searchGeneKeyWord(String geneKeyWord){
		//check for null and wild charectors
    	if(geneKeyWord != null  && !geneKeyWord.equals("*")  && !geneKeyWord.equals("%")){
             try {
                 logger.debug("inside searchGeneKeyWord");
 		    	//Create a Criteria for Approved Symbol
 		        Criteria approvedSymbolCrit = new Criteria();
 		        approvedSymbolCrit.addLike("upper(approvedSymbol)",geneKeyWord.toUpperCase());
 		        //Create a Criteria for Alias
 		        Criteria aliasCrit = new Criteria();
 		        aliasCrit.addLike("upper(alias)",geneKeyWord.toUpperCase());
 		        //Create a Criteria for Approved Name
 		        Criteria approvedNameCrit = new Criteria();
 		        approvedNameCrit.addLike("upper(approvedName)",geneKeyWord.toUpperCase());
 		        
 		        //Or the three
 		        approvedSymbolCrit.addOrCriteria(approvedNameCrit);
 		        approvedSymbolCrit.addOrCriteria(aliasCrit);
 		        Collection<AllGeneAliasLookup> allGeneAlias;
 				
 					allGeneAlias = QueryExecuter.executeQuery(AllGeneAlias.class, approvedSymbolCrit,QueryExecuter.NO_CACHE,true);
 		
 				if(allGeneAlias != null && allGeneAlias.size() > 0){
 		        	return (AllGeneAliasLookup[]) allGeneAlias.toArray(new AllGeneAliasLookup[allGeneAlias.size()]);
 		        }
 			return null;
 			} catch (Exception e) {
 				logger.error("Error in AllGeneAliasLookup when searching for "+geneKeyWord);
 				logger.error(e.getMessage());
 				return null;
 			}
     	}
 		return null;
     }
 }
