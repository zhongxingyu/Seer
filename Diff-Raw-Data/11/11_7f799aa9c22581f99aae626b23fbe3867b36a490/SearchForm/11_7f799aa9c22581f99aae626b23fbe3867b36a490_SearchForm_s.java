 /**
  *   The caMOD Software License, Version 1.0
  *
  *   Copyright 2005-2006 SAIC. This software was developed in conjunction with the National Cancer
  *   Institute, and so to the extent government employees are co-authors, any rights in such works
  *   shall be subject to Title 17 of the United States Code, section 105.
  *
  *   Redistribution and use in source and binary forms, with or without modification, are permitted
  *   provided that the following conditions are met:
  *
  *   1. Redistributions of source code must retain the above copyright notice, this list of conditions
  *   and the disclaimer of Article 3, below.  Redistributions in binary form must reproduce the above
  *   copyright notice, this list of conditions and the following disclaimer in the documentation and/or
  *   other materials provided with the distribution.
  *
  *   2.  The end-user documentation included with the redistribution, if any, must include the
  *   following acknowledgment:
  *
  *   "This product includes software developed by the SAIC and the National Cancer
  *   Institute."
  *
  *   If no such end-user documentation is to be included, this acknowledgment shall appear in the
  *   software itself, wherever such third-party acknowledgments normally appear.
  *
  *   3. The names "The National Cancer Institute", "NCI" and "SAIC" must not be used to endorse or
  *   promote products derived from this software.
  *
  *   4. This license does not authorize the incorporation of this software into any third party proprietary
  *   programs.  This license does not authorize the recipient to use any trademarks owned by either
  *   NCI or SAIC.
  *
  *
  *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
  *   WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  *   MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
  *   DISCLAIMED.  IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, SAIC, OR
  *   THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  *   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  *   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  *   PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
  *   OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  *   NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  *   SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *   
 * $Id: SearchForm.java,v 1.49 2008-10-01 18:25:49 pandyas Exp $
  * 
  * $Log: not supported by cvs2svn $
  * Revision 1.48  2008/09/23 17:43:48  pandyas
  * Typo in validation for therapeutic approaches field
  *
  * Revision 1.47  2008/08/14 19:02:01  schroedn
  * clean the pmid for security scans
  *
  * Revision 1.46  2008/08/14 15:57:46  pandyas
  * remove debug line
  *
  * Revision 1.45  2008/08/12 20:16:02  pandyas
  * Code was rolled back to continue work on security scan fixes.  Code added back in jsp again.  Originally From:
  *  Revision 1.42  2008/07/11 17:20:19  schroedn
  *  Bug 11007
  *  Added search for PMID numbers
  *
  * Revision 1.44  2008/08/12 19:40:32  pandyas
  * Fixed #15053  	Search for models with transgenic or targeted modification on advanced search page confusing
  *
  * Revision 1.43  2008/07/17 17:56:23  pandyas
  * Reverted code back to version for security scan fixes
  *
  * Revision 1.41  2008/06/13 17:33:57  pandyas
  * Modified to prevent SQL injection
  * Cleaned parameter name before proceeding
  * Re: Apps Scan run 06/12/2008
  *
  * Revision 1.40  2008/06/10 16:50:57  pandyas
  * Modified to prevent SQL injection
  * Cleaned parameter name before proceeding
  * Re: Apps Scan run 06/09/2008
  *
  * Revision 1.39  2008/06/03 00:28:00  pandyas
  * Fixed typo in SQL word
  *
  * Revision 1.38  2008/05/23 16:05:39  pandyas
  * Modified advanced search and TOC to prevent SQL injection
  * Added validation for diagnosisCode (just in case)
  * Re: Apps Scan run 05/15/2008
  *
  * Revision 1.37  2008/05/23 16:03:00  pandyas
  * Modified advanced search and TOC to prevent SQL injection
  * Added validation for organTissueCode
  * Re: Apps Scan run 05/15/2008
  *
  * Revision 1.36  2008/05/23 14:15:19  pandyas
  * Modified advanced search and TOC to prevent SQL injection
  * Added specific clean methods for text entry fields
  * Re: Apps Scan run 05/15/2008
  *
  * Revision 1.35  2008/05/22 18:24:09  pandyas
  * Modified advanced search and TOC to prevent SQL injection
  * Minor modifications
  * Re: Apps Scan run 05/15/2008
  *
  * Revision 1.34  2008/05/21 19:07:33  pandyas
  * Modified advanced search to prevent SQL injection
  * Consolidated all utility methods in new class
  * Re: Apps Scan run 05/15/2008
  *
  * Revision 1.33  2008/05/12 16:33:52  pandyas
  * REmoved test for letter or digit for organ and tumorClassification - EVS returns results with special characters therefore this can not be validated against malicious characters (special characters) for the security scan
  * We need to obtain an exception
  *
  * Revision 1.32  2008/05/05 15:07:00  pandyas
  * NCI security scan changes
  *
  * Revision 1.31  2008/02/21 21:52:26  pandyas
  * Final version for production
  *
  * Revision 1.30  2008/02/20 21:51:47  pandyas
  * Added code to eliminate blind SQL injection in carcinogenicIntervention parameter on Adv Search screen:
  * "Filter out hazardous characters from user input (High) Parameter: carcinogenicIntervention - Blind SQL Injection"
  *
  * Revision 1.29  2008/02/18 15:36:37  pandyas
  * Modified for annoying functionality with clear button not working
  *
  * Revision 1.28  2008/02/15 16:06:37  pandyas
  * Added code for SQL injection - App Scan report
  *
  * Revision 1.27  2008/01/22 17:21:24  pandyas
  * valid species - comment out
  *
  * Revision 1.26  2008/01/16 18:29:46  pandyas
  * Renamed value to Transplant for #8290
  *
  * Revision 1.25  2007/10/31 17:42:28  pandyas
  * Fixed #8290 	Rename graft object into transplant object
  *
  * Revision 1.24  2007/10/26 14:02:57  pandyas
  * ready for next build
  *
  * Revision 1.23  2007/10/25 16:16:13  pandyas
  * commented out system statments for build to stage
  *
  * Revision 1.22  2007/10/25 16:01:40  pandyas
  * Added validation for text fields to prevent Blind SQL injection attacks following the AppScan report findings
  *
  * Revision 1.21  2007/10/18 18:27:28  pandyas
  * Modified to prevent cross--site scripting attacks
  *
  * Revision 1.20  2007/10/17 18:29:28  pandyas
  * commented out PI until it works
  *
  * Revision 1.19  2007/10/17 18:23:40  pandyas
  * Modified to prevent cross--site scripting attacks - initial version
  *
  * Revision 1.18  2007/07/31 12:02:04  pandyas
  * VCDE silver level  and caMOD 2.3 changes
  *
  * Revision 1.17  2007/03/28 18:16:23  pandyas
  * Modified for the following Test Track items:
  * #462 - Customized search for carcinogens for Jackson Lab data
  * #494 - Advanced search for Carcinogens for Jackson Lab data
  *
  * Revision 1.16  2006/12/28 16:04:00  pandyas
  * Reverted to previous version - changed CE on adv search page
  *
  * Revision 1.14  2006/11/13 16:51:59  pandyas
  * #467 - Clear button on advanced search page doesn't work
  *
  * Revision 1.13  2006/10/17 16:10:47  pandyas
  * modified during development of caMOD 2.2 - various
  *
  * Revision 1.12  2006/05/10 14:25:10  schroedn
  * New Features - Changes from code review
  *
  * Revision 1.11  2006/05/10 13:39:56  schroedn
  * New Features - Changes from code review
  *
  * Revision 1.10  2006/05/10 12:02:12  georgeda
  * Changes for searching on transient interfaces
  *
  * Revision 1.9  2006/04/28 19:30:51  schroedn
  * Defect # 261
  * Added Tumor Classification, so to save the organ properly
  *
  * Revision 1.8  2006/04/17 19:09:19  pandyas
  * caMod 2.1 OM changes
  *
  * Revision 1.7  2005/11/16 19:43:30  georgeda
  * Added clear to search forms
  *
  * Revision 1.6  2005/11/07 16:54:51  georgeda
  * Fixed problem w/ advanced search options being used in simple search
  *
  * Revision 1.5  2005/10/20 19:29:32  georgeda
  * Added xenograft search functionality
  *
  * Revision 1.4  2005/10/19 20:19:26  georgeda
  * Cleanup
  *
  */
 package gov.nih.nci.camod.webapp.form;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.util.NameValue;
 import gov.nih.nci.camod.util.NameValueList;
 import gov.nih.nci.camod.util.SafeHTMLUtil;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import javax.servlet.http.HttpServletRequest;
 import org.apache.struts.Globals;
 import org.apache.struts.action.*;
 import org.apache.struts.util.MessageResources;
 
 
 public class SearchForm extends ActionForm implements Serializable, SearchData
 {
     private static final long serialVersionUID = 3257045453799404851L;
     
     protected String keyword;
     protected String piName;    
     protected String pmid;
     protected String modelDescriptor;
     protected String organ;
     protected String species;
     protected String externalSource;    
     protected String chemicalDrug;
     protected String comment;
     protected String hormone;
     protected String growthFactor;
     protected String radiation;
     protected String viral;
     protected String surgery;
     protected String phenotype;
     protected String disease;
     protected String tumorClassification;
     protected String cellLine;
     protected String organTissueCode;
     protected String organTissueName;
     protected String diagnosisCode;
     protected String diagnosisName;
     protected String inducedMutationAgent;
     protected String transgeneName;    
     protected String geneName;
     protected String genomicSegDesignator;
     protected String therapeuticApproach;
     protected String carcinogenicIntervention;
     protected String agentName;
     protected boolean searchTherapeuticApproaches = false;
     protected boolean searchEngineeredTransgene = false;
     protected boolean searchTargetedModification = false;
     protected boolean searchHistoMetastasis = false;
     protected boolean searchMicroArrayData = false;
     protected boolean searchImageData = false;    
     protected boolean searchTransplant = false;
     protected boolean searchTransientInterference = false;
     protected boolean searchToolStrain = false; 
     
     public SearchForm()
     {
     }    
 
     public void setHormone(String hormone)
     {
         this.hormone = hormone;
     }
 
     public void setGrowthFactor(String growthFactor)
     {
         this.growthFactor = growthFactor;
     }
 
     public String getChemicalDrug()
     {
         return chemicalDrug;
     }
 
     public void setPmid(String pmid)
     {    	
     	this.pmid = pmid;
         // Clean the parameter
         if (this.pmid != null && !this.pmid.equals(""))  {
 
                 this.pmid = SafeHTMLUtil.clean(this.pmid);
         }
     	
     
     }
     
     public String getPmid()
     {
     	return pmid;
     }
     
     public void setChemicalDrug(String chemicalDrug)
     {
         this.chemicalDrug = chemicalDrug;
     }
 
     public String getComment()
     {
         return comment;
     }
 
     public void setComment(String comment)
     {
         this.comment = comment;
     }
 
     public String getKeyword()
     {
         return keyword;
     }
 
     public void setKeyword(String keyword)
     {
         this.keyword = keyword;
         // Clean the parameter
         if (this.keyword != null && !this.keyword.equals(""))  {
                 this.keyword = SafeHTMLUtil.cleanKeyword(this.keyword);
         }        
     }
 
     public String getModelDescriptor()
     {
         return modelDescriptor;
     }
 
     public void setModelDescriptor(String modelDescriptor)
     {
         this.modelDescriptor = modelDescriptor;
         // Clean the parameter
         if (this.modelDescriptor != null && !this.modelDescriptor.equals(""))  {
                 this.modelDescriptor = SafeHTMLUtil.cleanModelDescriptor(this.modelDescriptor);
         }        
     }
 
     public String getOrgan()
     {
         return organ;
     }
 
     public void setOrgan(String organ)
     {
         this.organ = organ;
         // Clean the parameter
         if (this.organ != null && !this.organ.equals(""))  {
                 this.organ = SafeHTMLUtil.clean(this.organ);
         }
     }
     
     public String getPiName() {
         return piName;
     }
 
     public void setPiName(String piName)
     {
         this.piName = piName;
         // Clean the parameter
         if (this.piName != null && !this.piName.equals(""))  {
                 this.piName = SafeHTMLUtil.clean(this.piName);
         }        
     }
 
     public String getSpecies()
     {
         return species;
     }
 
     public void setSpecies(String species)
     {
         this.species = species;
     }
     
     public String getExternalSource()
     {
         return externalSource;
     }
 
     public void setExternalSource(String externalSource)
     {
         this.externalSource = externalSource;
     }    
 
     public String getGrowthFactor()
     {
         return growthFactor;
     }
 
     public String getHormone()
     {
         return hormone;
     }
 
     public String getRadiation()
     {
         return radiation;
     }
 
     public void setRadiation(String radiation)
     {
         this.radiation = radiation;
     }
 
     public String getSurgery()
     {
         return surgery;
     }
 
     public void setSurgery(String surgery)
     {
         this.surgery = surgery;
     }
 
     public String getViral()
     {
         return viral;
     }
 
     public void setViral(String viral)
     {
         this.viral = viral;
     }
 
     public String getPhenotype()
     {
         return phenotype;
     }
 
     public void setPhenotype(String phenotype)
     {
         this.phenotype = phenotype;
         // Clean the parameter
         if (this.phenotype != null && !this.phenotype.equals(""))  {
                 this.phenotype = SafeHTMLUtil.cleanPhenotype(this.phenotype);
         }        
     }
 
     public String getDisease()
     {
         return disease;
     }
 
     public void setDisease(String disease)
     {
         this.disease = disease;
         // Clean the parameter
         if (this.disease != null && !this.disease.equals(""))  {
                 this.disease = SafeHTMLUtil.clean(this.disease);
         }        
     }
 
     public String getCellLine()
     {
         return cellLine;
     }
 
     public void setCellLine(String cellLine)
     {
         this.cellLine = cellLine;
     }
 
     public String getOrganTissueCode()
     {
         return organTissueCode;
     }
 
     public void setOrganTissueCode(String organTissueCode)
     {
         this.organTissueCode = organTissueCode;
         // Clean the parameter
         if (this.organTissueCode != null && !this.organTissueCode.equals(""))  {
                 this.organTissueCode = SafeHTMLUtil.clean(this.organTissueCode);
         }        
     }
 
     public String getOrganTissueName()
     {
         return organTissueName;
     }
 
     public void setOrganTissueName(String organTissueName)
     {
         this.organTissueName = organTissueName;
         // Clean the parameter
         if (this.organTissueName != null && !this.organTissueName.equals(""))  {
                 this.organTissueName = SafeHTMLUtil.clean(this.organTissueName);
         }        
     }
 
     public String getDiagnosisCode()
     {
         return diagnosisCode;
     }
 
     public void setDiagnosisCode(String diagnosisCode)
     {
         this.diagnosisCode = diagnosisCode;
         // Clean the parameter
         if (this.diagnosisCode != null && !this.diagnosisCode.equals(""))  {
                 this.diagnosisCode = SafeHTMLUtil.clean(this.diagnosisCode);
         }         
     }
 
     public String getDiagnosisName()
     {
         return diagnosisName;
     }
 
     public void setDiagnosisName(String diagnosisName)
     {
         this.diagnosisName = diagnosisName;
         // Clean the parameter
         if (this.diagnosisName != null && !this.diagnosisName.equals(""))  {
                 this.diagnosisName = SafeHTMLUtil.clean(this.diagnosisName);
         }        
     }
 
     public String getInducedMutationAgent()
     {
         return inducedMutationAgent;
     }
 
     public void setInducedMutationAgent(String inducedMutationAgent)
     {
         this.inducedMutationAgent = inducedMutationAgent;
     }
 
     public String getGeneName()
     {
         return geneName;
     }
 
     public void setGeneName(String geneName)
     {
         this.geneName = geneName;
         // Clean the parameter
         if (this.geneName != null && !this.geneName.equals(""))  {
                 this.geneName = SafeHTMLUtil.cleanGeneName(this.geneName);
         }         
     }
 
     public String getGenomicSegDesignator()
     {
         return genomicSegDesignator;
     }
 
     public void setGenomicSegDesignator(String genomicSegDesignator)
     {
         this.genomicSegDesignator = genomicSegDesignator;
     }
 
     public boolean isSearchTherapeuticApproaches()
     {
         return searchTherapeuticApproaches;
     }
 
     public void setSearchTherapeuticApproaches(boolean searchTherapeuticApproaches)
     {
         this.searchTherapeuticApproaches = searchTherapeuticApproaches;
     }
 
     public String getTherapeuticApproach()
     {
         return therapeuticApproach;
     }
 
     public void setTherapeuticApproach(String therapeuticApproach)
     {
         this.therapeuticApproach = therapeuticApproach;
     }
 
     public boolean isSearchHistoMetastasis()
     {
         return searchHistoMetastasis;
     }
 
     public void setSearchHistoMetastasis(boolean searchHistoMetastasis)
     {
         this.searchHistoMetastasis = searchHistoMetastasis;
     }
 
     public boolean isSearchMicroArrayData()
     {
         return searchMicroArrayData;
     }
 
     public void setSearchMicroArrayData(boolean searchMicroArrayData)
     {
         this.searchMicroArrayData = searchMicroArrayData;
     }
     
     public boolean isSearchImageData()
     {
         return searchImageData;
     }
 
     public void setSearchImageData(boolean searchImageData)
     {
         this.searchImageData = searchImageData;
     }    
 
     public boolean isSearchTransplant()
     {
         return searchTransplant;
     }
 
     public void setSearchTransplant(boolean searchTransplant)
     {
         this.searchTransplant = searchTransplant;
     }
 
     /**
      * @return Returns the searchTransientInterference.
      */
     public boolean isSearchTransientInterference()
     {
         return searchTransientInterference;
     }
 
     /**
      * @param searchTransientInterference The searchTransientInterference to set.
      */
     public void setSearchTransientInterference(boolean searchTransientInterference)
     {
         this.searchTransientInterference = searchTransientInterference;
     }
     
     /**
      * @return Returns the searchToolStrain.
      */
     public boolean isSearchToolStrain()
     {
         return searchToolStrain;
     }
 
     /**
      * @param searchToolStrain The searchToolStrain to set.
      */
     public void setSearchToolStrain(boolean searchToolStrain)
     {
         this.searchToolStrain = searchToolStrain;
     }    
     
     public String getTumorClassification()
     {
         return tumorClassification;
     }
 
     public void setTumorClassification(String tumorClassification)
     {
         this.tumorClassification = tumorClassification;
         // Clean the parameter
         if (this.tumorClassification != null && !this.tumorClassification.equals(""))  {
                 this.tumorClassification = SafeHTMLUtil.clean(this.tumorClassification);
         }        
     }
     
     /**
      * Reset all fields that are not used in the simple search. Since the form
      * is used for both the simple and advanced search and is stored in the
      * session to allow users to quickly use the back arrow to refine their
      * search we need to make sure that when the user clicks on the simple
      * search page the options from the advanced search page are reset.
      * 
      */
     public void simpleSearchReset()
     {
 
         chemicalDrug = null;
         hormone = null;
         growthFactor = null;
         radiation = null;
         viral = null;
         surgery = null;
         phenotype = null;
         disease = null;
         cellLine = null;
         diagnosisCode = null;
         diagnosisName = null;
         tumorClassification = null;
         organ = null;
         organTissueCode = null;
         organTissueName = null;
         inducedMutationAgent = null;
         geneName = null;
         transgeneName = null;
         genomicSegDesignator = null;
         therapeuticApproach = null;
         carcinogenicIntervention = null;
         agentName = null;
         searchTherapeuticApproaches = false;
         searchTransientInterference = false;
         searchEngineeredTransgene = false;
         searchTargetedModification = false;
         searchHistoMetastasis = false;
         searchMicroArrayData = false;
         searchTransplant = false;
         searchToolStrain = false;
         externalSource = null;
         searchImageData = false;
     }
 
     /**
      * Reset all fields.
      */
     public void allFieldsReset()
     {
 
         keyword = null;
         piName = null;
         modelDescriptor = null;
         species = null;
         
         simpleSearchReset();
     }
 
 	public String getAgentName() {
 		return agentName;
 	}
 
 	public void setAgentName(String agentName) {
 		this.agentName = agentName;
 	}
 
 	public String getCarcinogenicIntervention() {
 		return carcinogenicIntervention;
 	}
 
 	public void setCarcinogenicIntervention(String carcinogenicIntervention) {
 		this.carcinogenicIntervention = carcinogenicIntervention;
 	}
     
      /**
      * validate the search form
      * @param mapping
      * @param request
      * @return
      */
     public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) 
     {
         ActionErrors errors = new ActionErrors();
        
         // Identify the request parameter containing the method name
         String parameter = mapping.getParameter();
         
 
         // validate keyword against malicious characters to prevent blind SQL injection attacks
         if (keyword != null && keyword.length() > 0 )
         { 
             setKeyword(keyword);        
         }
         
         // validate modelDescriptor against malicious characters to prevent blind SQL injection attacks
         if (modelDescriptor != null  && modelDescriptor.length() > 0)
         { 
             setModelDescriptor(modelDescriptor); 
         }
         
         
         // validate for PI
         if (piName != null && piName.length() > 0 )
         {
             List piNameList = new ArrayList();
             piNameList = (List)request.getSession().getAttribute(Constants.Dropdowns.PRINCIPALINVESTIGATORQUERYDROP);
             request.getSession().setAttribute(Constants.Dropdowns.SEARCHPIDROP, piNameList);            
             
             if (!SafeHTMLUtil.isValidStringValue(piName,Constants.Dropdowns.SEARCHPIDROP,request))
             {
                // populate the validation message
                errors.add("piName", new ActionMessage("error.piName.validValue"));
             }
         }
         
         // validate for species
         if (species != null && species.length() > 0 )
         {          
             NameValueList.generateApprovedSpeciesList();
             request.getSession().setAttribute(Constants.Dropdowns.SEARCHSPECIESDROP, NameValueList.getApprovedSpeciesList());
 
             if (!SafeHTMLUtil.isValidValue(species,Constants.Dropdowns.SEARCHSPECIESDROP,request))
             {
                 // populate the validation message
                 errors.add("species", new ActionMessage("error.species.validValue"));               
             }
         }        
         
         // validate tumorClassification against malicious characters to prevent blind SQL injection attacks
         if (tumorClassification != null && tumorClassification.length() > 0)
         {
             setTumorClassification(tumorClassification);
         } 
         
         // validate genomicSegDesignator against malicious characters to prevent blind SQL injection attacks
         if (genomicSegDesignator != null && genomicSegDesignator.length() > 0 )
         {
             List genomicSegDesigList = new ArrayList();
             genomicSegDesigList = (List)request.getSession().getAttribute(Constants.Dropdowns.CLONEDESIGNATORQUERYDROP);
             request.getSession().setAttribute(Constants.Dropdowns.SEARCHGENOMICSEGMENT, genomicSegDesigList);            
             
             if (!SafeHTMLUtil.isValidStringValue(genomicSegDesignator,Constants.Dropdowns.SEARCHGENOMICSEGMENT,request))
             {
                // populate the validation message
                errors.add("genomicSegDesignator", new ActionMessage("error.genomicSegDesignator.validValue"));
                return errors;
             }               
         }           
         
         // validate for inducedMutationAgent
         if (inducedMutationAgent != null && inducedMutationAgent.length() > 0 )
         { 
             List inducedMutationAgentList = new ArrayList();
             inducedMutationAgentList = (List)request.getSession().getAttribute(Constants.Dropdowns.INDUCEDMUTATIONAGENTQUERYDROP);
             request.getSession().setAttribute(Constants.Dropdowns.SEARCHINDUCEDMUTATIONDROP, inducedMutationAgentList);
 
            if (!SafeHTMLUtil.isValidValue(inducedMutationAgent,Constants.Dropdowns.SEARCHINDUCEDMUTATIONDROP,request))
             {
                 // populate the validation message
                 errors.add("inducedMutationAgent", new ActionMessage("error.inducedMutationAgent.validValue"));
             }             
         }        
         
         // validate for carcinogenicIntervention
         if (carcinogenicIntervention != null && carcinogenicIntervention.length() > 0 )
         {           
             NameValueList.generateCarcinogenicInterventionList();
             request.getSession().setAttribute(Constants.Dropdowns.SEARCHCARCINOGENEXPOSUREDROP, NameValueList.getCarcinogenicInterventionList());
 
 	            if (!SafeHTMLUtil.isValidValue(carcinogenicIntervention,Constants.Dropdowns.SEARCHCARCINOGENEXPOSUREDROP,request) 
 	            		| !SafeHTMLUtil.isLetterOrDigitWithExceptions(carcinogenicIntervention))
 	            {
 	                   // populate the validation message
 	                   errors.add("carcinogenicIntervention", new ActionMessage("error.carcinogenicIntervention.validValue"));
 	            } 
             
             // validate for agentName            
             if (agentName != null && agentName.length() > 0 )
             {
                 List agentNameList = new ArrayList();
                 agentNameList = (List)request.getSession().getAttribute(Constants.Dropdowns.ENVIRONMENTALFACTORNAMESDROP);
                 request.getSession().setAttribute(Constants.Dropdowns.SEARCHENVIRONFACTORDROP, agentNameList);            
                 
                 if (!SafeHTMLUtil.isValidStringValue(agentName,Constants.Dropdowns.SEARCHENVIRONFACTORDROP,request))
                 {
                    // populate the validation message
                    errors.add("agentName", new ActionMessage("error.agentName.validValue"));
                    return errors;
                 }               
             }
         }  
 
         
         // validate for cellLine            
         if (cellLine != null && cellLine.length() > 0 )
         {
             List cellLineList = new ArrayList();
             cellLineList = (List)request.getSession().getAttribute(Constants.Dropdowns.CELLLINENAMEQUERYDROP);
             request.getSession().setAttribute(Constants.Dropdowns.SEARCHCELLLINE, cellLineList);            
             
             if (!SafeHTMLUtil.isValidStringValue(cellLine,Constants.Dropdowns.SEARCHCELLLINE,request))
             {
                // populate the validation message
                errors.add("cellLine", new ActionMessage("error.cellLine.validValue"));
                return errors;
             }                
         }        
         
          
         
         // validate therpy compound/drug against malicious characters to prevent blind SQL injection attacks
         if (therapeuticApproach != null   )
         { 
             List drugNameList = new ArrayList();
             drugNameList = (List)request.getSession().getAttribute(Constants.Dropdowns.THERAPEUTICAPPROACHDRUGQUERYDROP);
             request.getSession().setAttribute(Constants.Dropdowns.SEARCHTHERAPEUTICDRUGNAME, drugNameList);            
             
             if (!SafeHTMLUtil.isValidStringValue(therapeuticApproach,Constants.Dropdowns.SEARCHTHERAPEUTICDRUGNAME,request))
             {
                // populate the validation message
                errors.add("therapeuticApproach", new ActionMessage("error.therapeuticApproach.validValue"));
                return errors;
             }
         }
         
         // validate for externalSource
         if (externalSource != null && externalSource.length() > 0 )
         {           
             NameValueList.generateExternalSourceList();
             request.getSession().setAttribute(Constants.Dropdowns.SEARCHEXTERNALSOURCEDROP, NameValueList.getExternalSourceList());
 
             if (!SafeHTMLUtil.isValidValue(externalSource,Constants.Dropdowns.SEARCHEXTERNALSOURCEDROP,request))
             {
                 // populate the validation message
                 errors.add("externalSource", new ActionMessage("error.externalSource.validValue"));
             }             
         } 
         
         // validate for searchTherapeuticApproaches
         if (searchTherapeuticApproaches == true | searchTherapeuticApproaches == false )
         {
            
         } else {           
             // populate the validation message
             errors.add("searchTherapeuticApproaches", new ActionMessage("error.invalidParameter.validValue"));        	
         }
 
         // validate for engineeredTransgene
         if (searchEngineeredTransgene == true | searchEngineeredTransgene == false )
         {
              
         } else {           
             // populate the validation message
             errors.add("engineeredTransgene", new ActionMessage("error.invalidParameter.validValue"));       	
         }
 
         // validate for targetedModification
         if (searchTargetedModification == true | searchTargetedModification == false )
         {
            
         } else {            
             // populate the validation message
             errors.add("targetedModification", new ActionMessage("error.invalidParameter.validValue"));
         }
 
         // validate for searchHistoMetastasis
         if (searchHistoMetastasis == true | searchHistoMetastasis == false )
         {
         } else {           
                 // populate the validation message
                 errors.add("searchHistoMetastasis", new ActionMessage("error.invalidParameter.validValue"));
         } 
 
         // validate for searchMicroArrayData
         if (searchMicroArrayData == true | searchMicroArrayData == false )
         {
         } else {           
                 // populate the validation message
                 errors.add("searchMicroArrayData", new ActionMessage("error.invalidParameter.validValue"));          
         } 
 
         // validate for searchImageData
         if (searchImageData == true | searchImageData == false )
         {
         } else {           
                 // populate the validation message
                 errors.add("searchImageData", new ActionMessage("error.invalidParameter.validValue"));
         } 
 
         // validate for searchTransplant
         if (searchTransplant == true | searchTransplant == false )
         {
         } else {           
                 // populate the validation message
                 errors.add("searchTransplant", new ActionMessage("error.invalidParameter.validValue")); 
         }
 
         // validate for searchTransientInterference
         if (searchTransplant == true | searchTransplant == false )
         {
         } else {           
                 // populate the validation message
                 errors.add("searchTransientInterference", new ActionMessage("error.invalidParameter.validValue"));
         } 
 
         // validate for searchToolStrain
         if (searchToolStrain == true | searchToolStrain == false )
         {
         } else {          
                 // populate the validation message
                 errors.add("searchToolStrain", new ActionMessage("error.invalidParameter.validValue"));
         }        
         
         if (parameter != null) {
             // Identify the method name to be dispatched to.
             String method = request.getParameter(parameter);
             MessageResources resources = (MessageResources) request.getAttribute(Globals.MESSAGES_KEY);
 
             // Identify the localized message for the clear button
             String clear = resources.getMessage("button.clear");
 
             // if message resource matches the clear button then no
             // need to validate
             if ((method != null) && (method.equalsIgnoreCase(clear))) {
                 return null;
             }        
         }       
         return errors;               
     }
 
 	/**
 	 * @return the searchEngineeredTransgene
 	 */
 	public boolean isSearchEngineeredTransgene() {
 		return searchEngineeredTransgene;
 	}
 
 	/**
 	 * @param searchEngineeredTransgene the searchEngineeredTransgene to set
 	 */
 	public void setSearchEngineeredTransgene(boolean searchEngineeredTransgene) {
 		this.searchEngineeredTransgene = searchEngineeredTransgene;
 	}
 
 	/**
 	 * @return the searchTargetedModification
 	 */
 	public boolean isSearchTargetedModification() {
 		return searchTargetedModification;
 	}
 
 	/**
 	 * @param searchTargetedModification the searchTargetedModification to set
 	 */
 	public void setSearchTargetedModification(boolean searchTargetedModification) {
 		this.searchTargetedModification = searchTargetedModification;
 	}
 
 	/**
 	 * @return the transgeneName
 	 */
 	public String getTransgeneName() {
 		return transgeneName;
 	}
 
 	/**
 	 * @param transgeneName the transgeneName to set
 	 */
 	public void setTransgeneName(String transgeneName) {
 		this.transgeneName = transgeneName;
 	} 
     
   
 }
