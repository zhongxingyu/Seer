 /**
  * 
 * $Id: EngineeredTransgenePopulateAction.java,v 1.26 2007-07-31 12:02:55 pandyas Exp $
  *
  * $Log: not supported by cvs2svn $
  * Revision 1.25  2007/04/04 13:19:08  pandyas
  * modified names for mutation identifier fields (number changed to id)
  *
  * Revision 1.24  2007/03/26 12:02:31  pandyas
  * caMOd 2.3 enhancements for Zebrafish support
  *
  * Revision 1.23  2006/05/23 16:01:43  pandyas
  * Fixed code for species - not required and must be nulled out when editing
  *
  * Revision 1.22  2006/05/23 14:15:35  schroedn
  * Fixed bug that did not corretly repopulate gene functions
  *
  * Revision 1.21  2006/05/22 18:39:02  pandyas
  * Added brackets to if statement - good java practice
  *
  * Revision 1.20  2006/05/22 16:52:28  pandyas
  * modified isRandom to make consistent between transgene and genomic segment
  *
  * Revision 1.19  2006/04/21 18:28:02  georgeda
  * Fixed issue w/ engineered genes displaying
  *
  * Revision 1.18  2006/04/20 18:11:16  pandyas
  * Cleaned up Species or Strain save of Other in DB
  *
  * Revision 1.17  2006/04/17 19:09:40  pandyas
  * caMod 2.1 OM changes
  *
  * 
  */
 
 
 package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.*;
 import gov.nih.nci.camod.service.impl.EngineeredTransgeneManagerSingleton;
 import gov.nih.nci.camod.webapp.form.EngineeredTransgeneForm;
 import gov.nih.nci.camod.webapp.util.NewDropdownUtil;
 import java.util.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.struts.action.*;
 
 public class EngineeredTransgenePopulateAction extends BaseAction
 {
 
     public ActionForward populate(ActionMapping mapping,
                                   ActionForm form,
                                   HttpServletRequest request,
                                   HttpServletResponse response) throws Exception
     {
 
         log.debug("<EngineeredTransgenePopulateAction populate> Entering populate() ");
 
         EngineeredTransgeneForm theEngineeredTransgeneForm = (EngineeredTransgeneForm) form;
 
         String aEngineeredTransgeneID = request.getParameter("aEngineeredTransgeneID");
         theEngineeredTransgeneForm.setTransgeneId(aEngineeredTransgeneID);
 
         Transgene theEngineeredTransgene = EngineeredTransgeneManagerSingleton.instance().get(aEngineeredTransgeneID);
 
         if (theEngineeredTransgene == null)
         {
             request.setAttribute(Constants.Parameters.DELETED, "true");
         }
         else
         {
             // populate isRandom
             if (theEngineeredTransgene.getIsRandom() == true)
             {
                 theEngineeredTransgeneForm.setIsRandom("yes");
             }
             else
             {
                 theEngineeredTransgeneForm.setIsRandom("no");
                 theEngineeredTransgeneForm.setLocationOfIntegration(theEngineeredTransgene.getLocationOfIntegration());
             }
             // populate Name
             theEngineeredTransgeneForm.setName(theEngineeredTransgene.getName());
 
             NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.SPECIESQUERYDROP, "");
 
             if (theEngineeredTransgene.getSpecies() != null)
             {
                 if (theEngineeredTransgene.getSpecies().getScientificNameUnctrlVocab() != null && theEngineeredTransgene.getSpecies().getScientificNameUnctrlVocab().length() > 0)
                 {
                     theEngineeredTransgeneForm.setScientificName(Constants.Dropdowns.OTHER_OPTION);
                     theEngineeredTransgeneForm.setOtherScientificName(theEngineeredTransgene.getSpecies().getScientificNameUnctrlVocab());
                 }
                 else
                 {
                     theEngineeredTransgeneForm.setScientificName(theEngineeredTransgene.getSpecies().getScientificName());
                 }
             }
             // Transcriptional (Promoter) 1
             Set<RegulatoryElement> theRegElementList = theEngineeredTransgene.getRegulatoryElementCollection();
 
             for (RegulatoryElement regElement : theRegElementList)
             {
                 if (regElement.getRegulatoryElementType().getName().equals("Transcriptional 1"))
                 {
                     theEngineeredTransgeneForm.setTranscriptional1_name(regElement.getName());
                     Species theSpecies = regElement.getSpecies();
 
                     if (theSpecies != null)
                     {
                         if (theSpecies.getScientificNameUnctrlVocab() != null)
                         {
                             theEngineeredTransgeneForm.setTranscriptional1_species("Other");
                             theEngineeredTransgeneForm.setTranscriptional1_otherSpecies(theSpecies.getScientificNameUnctrlVocab());
                         }
                         else
                         {
                             theEngineeredTransgeneForm.setTranscriptional1_species(theSpecies.getScientificName());
                         }
                     }
                 }
 
                 else if (regElement.getRegulatoryElementType().getName().equals("Transcriptional 2"))
                 {
                     theEngineeredTransgeneForm.setTranscriptional2_name(regElement.getName());
                     Species theSpecies = regElement.getSpecies();
 
                     if (theSpecies != null)
                     {
                         if (theSpecies.getScientificNameUnctrlVocab() != null)
                         {
                             theEngineeredTransgeneForm.setTranscriptional2_species("Other");
                             theEngineeredTransgeneForm.setTranscriptional2_otherSpecies(theSpecies.getScientificNameUnctrlVocab());
                         }
                         else
                         {
                             theEngineeredTransgeneForm.setTranscriptional2_species(theSpecies.getScientificName());
                         }
                     }
                 }
 
                 else if (regElement.getRegulatoryElementType().getName().equals("Transcriptional 3"))
                 {
                     theEngineeredTransgeneForm.setTranscriptional3_name(regElement.getName());
                     Species theSpecies = regElement.getSpecies();
 
                     if (theSpecies != null)
                     {
                         if (theSpecies.getScientificNameUnctrlVocab() != null)
                         {
                             theEngineeredTransgeneForm.setTranscriptional3_species("Other");
                             theEngineeredTransgeneForm.setTranscriptional3_otherSpecies(theSpecies.getScientificNameUnctrlVocab());
                         }
                         else
                         {
                             theEngineeredTransgeneForm.setTranscriptional3_species(theSpecies.getScientificName());
                         }
                     }
                 }
 
                 else if (regElement.getRegulatoryElementType().getName().equals("Poly A Signal"))
                 {
                     theEngineeredTransgeneForm.setPolyASignal_name(regElement.getName());
                     Species theSpecies = regElement.getSpecies();
 
                     if (theSpecies != null)
                     {
                         if (theSpecies.getScientificNameUnctrlVocab() != null)
                         {
                             theEngineeredTransgeneForm.setPolyASignal_species("Other");
                             theEngineeredTransgeneForm.setPolyASignal_otherSpecies(theSpecies.getScientificNameUnctrlVocab());
                         }
                         else
                         {
                             theEngineeredTransgeneForm.setPolyASignal_species(theSpecies.getScientificName());
                         }
                     }
                 }
 
                 else if (regElement.getRegulatoryElementType().getName().equals("Splice Site"))
                 {
                     theEngineeredTransgeneForm.setSpliceSites_name(regElement.getName());
                     Species theSpecies = regElement.getSpecies();
 
                     if (theSpecies != null)
                     {
                         if (theSpecies.getScientificNameUnctrlVocab() != null)
                         {
                             theEngineeredTransgeneForm.setSpliceSites_species("Other");
                             theEngineeredTransgeneForm.setSpliceSites_otherSpecies(theSpecies.getScientificNameUnctrlVocab());
                         }
                         else
                         {
                             theEngineeredTransgeneForm.setSpliceSites_species(theSpecies.getScientificName());
                         }
                     }
                 }
 
                 // MGI Number
                 MutationIdentifier theMutationIdentifier = theEngineeredTransgene.getMutationIdentifier();
                 if (theMutationIdentifier != null)
                 {
                 	if (theMutationIdentifier.getMgiId() != null && theMutationIdentifier.getMgiId().length() > 0) {
                 		theEngineeredTransgeneForm.setMgiId(theMutationIdentifier.getMgiId());
                 	}
                 	if (theMutationIdentifier.getZfinId() != null && theMutationIdentifier.getZfinId().length() > 0) {
                         theEngineeredTransgeneForm.setZfinId(theMutationIdentifier.getZfinId());
                     }
                 	if (theMutationIdentifier.getRgdId() != null && theMutationIdentifier.getRgdId().length() > 0) {
                         theEngineeredTransgeneForm.setRgdId(theMutationIdentifier.getRgdId());
                     }                	
                 }
 
                 // Gene Function
                 Set<GeneFunction> theGeneFunctions = theEngineeredTransgene.getGeneFunctionCollection();
 
                 String theGeneFunctionString = "";
 
                 for (GeneFunction theGeneFunction : theGeneFunctions)
                 {
                     if (theGeneFunctionString.trim().length() > 0)
                     {
                         theGeneFunctionString += ", ";
                     }
                     theGeneFunctionString += theGeneFunction.getFunction();
                 }
                 theEngineeredTransgeneForm.setGeneFunctions(theGeneFunctionString);
 
                 // Conditionality
                 Conditionality theConditionality = theEngineeredTransgene.getConditionality();
                 if (theConditionality != null)
                 {
                     if ("1".equals(theConditionality.getConditionedBy()))
                     {
                         theEngineeredTransgeneForm.setConditionedBy(Constants.CONDITIONAL);
                         theEngineeredTransgeneForm.setDescription(theConditionality.getDescription());
                     }
                     else
                     {
                         theEngineeredTransgeneForm.setConditionedBy(Constants.NOT_CONDITIONAL);
                     }
                 }
 
                 // Additional Features / Comments
                 theEngineeredTransgeneForm.setComments(theEngineeredTransgene.getComments());
 
                 // Image
                 Image inImage = theEngineeredTransgene.getImage();
                 if (inImage != null)
                 {
                     theEngineeredTransgeneForm.setTitle(inImage.getTitle());
                     theEngineeredTransgeneForm.setUrl(inImage.getUrl());
                     theEngineeredTransgeneForm.setDescriptionOfConstruct(inImage.getDescription());
                     theEngineeredTransgeneForm.setImageUrl(inImage.getImageUrl());
                     theEngineeredTransgeneForm.setThumbUrl(inImage.getThumbUrl());
                 }
             }
         }
 
         // setup dropdown menus
         this.dropdown(request, response);
 
         return mapping.findForward("submitEngineeredTransgene");
     }
 
     public ActionForward dropdown(ActionMapping mapping,
                                   ActionForm form,
                                   HttpServletRequest request,
                                   HttpServletResponse response) throws Exception
     {
         log.debug("<EngineeredTransgenePopulateAction dropdown> Entering dropdown()");
 
         // setup dropdown menus
         this.dropdown(request, response);
 
         return mapping.findForward("submitEngineeredTransgene");
     }
 
     /**
      * Populate all drowpdowns for this type of form
      * 
      * @param request
      * @param response
      * @throws Exception
      */
     public void dropdown(HttpServletRequest request,
                          HttpServletResponse response) throws Exception
     {
         log.debug("<EngineeredTransgenePopulateAction dropdown> Entering void dropdown()");
 
         NewDropdownUtil.populateDropdown(request, Constants.Dropdowns.SPECIESQUERYDROP, Constants.Dropdowns.ADD_BLANK_AND_OTHER_OPTION);
 
         log.debug("<EngineeredTransgenePopulateAction dropdown> Exiting void dropdown()");
     }
 }
