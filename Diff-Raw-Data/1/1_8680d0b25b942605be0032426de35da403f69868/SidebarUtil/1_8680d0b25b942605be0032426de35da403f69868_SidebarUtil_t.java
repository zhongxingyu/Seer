 package gov.nih.nci.camod.webapp.util;
 
 import gov.nih.nci.camod.Constants;
 
 import javax.servlet.http.HttpServletRequest;
 
 public class SidebarUtil extends gov.nih.nci.camod.webapp.action.BaseAction {
 
 		public String findSubMenu( HttpServletRequest request, String jspName )
 		{			
 			System.out.println("<sidebar.jsp> String jspName=" + jspName );
 			
 			if( 		jspName.equals("searchSimple.jsp") || 
 						jspName.equals("searchHelp.jsp") || 
 						jspName.equals("searchAdvanced.jsp") || 
 						jspName.equals("searchDrugScreening.jsp") || 
 						jspName.equals("searchTableOfContents.jsp") || 
 						jspName.equals("searchResultsDrugScreen.jsp") || 
 						jspName.equals("searchResults.jsp")||
 						jspName.equals("expDesignStage0.jsp")||
 						jspName.equals("expDesignStage1.jsp")||
 						jspName.equals("expDesignStage2.jsp")||
 						jspName.equals("yeastStrainsStage01.jsp")||
 						jspName.equals("yeastStrainsStage2.jsp")) {
 				return "subSearchMenu.jsp";
 			} 
 			else if ( 	jspName.equals("viewModelCharacteristics.jsp") || 
 						jspName.equals("viewTransplantXenograft.jsp") || 
 						jspName.equals("viewGeneticDescription.jsp") || 
                         jspName.equals("viewInvivoDetails.jsp") ||
 						jspName.equals("viewPublications.jsp") ||
 						jspName.equals("viewCarcinogenicInterventions.jsp")  || 
 						jspName.equals("viewHistopathology.jsp")  || 
 						jspName.equals("viewTherapeuticApproaches.jsp") || 
                        jspName.equals("viewTransientInterference.jsp") || 
 						jspName.equals("viewCellLines.jsp") || 
 						jspName.equals("viewImages.jsp") || 
 						jspName.equals("viewMicroarrays.jsp") ){
                 
                     String theSubMenu = "subViewModelMenu.jsp";
                     
                     // For special cases when we don't have an animal model
                     if (request.getSession().getAttribute(Constants.ANIMALMODEL) == null) {
                         theSubMenu = "subSearchMenu.jsp";
                     }
 
 					return theSubMenu;
 			} 
 			else if (	jspName.equals("adminRoles.jsp") || 
 						jspName.equals("helpAdmin.jsp") || 
 						jspName.equals("adminModelsAssignment.jsp") || 
                         jspName.equals("adminCommentsAssignment.jsp") || 
                         jspName.equals("adminRolesAssignment.jsp") || 
                         jspName.equals("adminEditUserRoles.jsp") || 
                         jspName.equals("adminEditUser.jsp") || 
                         jspName.equals("adminEditModels.jsp") || 
                         jspName.equals("adminUserManagement.jsp") ||  
 						jspName.equals("helpDesk.jsp") ) {	
 					return "subAdminMenu.jsp";
 			}
 			else if ( 	jspName.equals("submitOverview.jsp") ||
 						jspName.equals("submitAssocExpression.jsp") || 
 						jspName.equals("submitAssocMetastasis.jsp") || 
 						jspName.equals("submitSpontaneousMutation.jsp") || 
 						jspName.equals("submitClinicalMarkers.jsp") || 
 						jspName.equals("submitGeneDelivery.jsp") || 
 						jspName.equals("submitModelCharacteristics.jsp") || 
 						jspName.equals("submitEngineeredTransgene.jsp") ||  
 						jspName.equals("submitGenomicSegment.jsp") ||  
 						jspName.equals("submitTargetedModification.jsp") ||  
 						jspName.equals("submitInducedMutation.jsp") ||  
 						jspName.equals("submitChemicalDrug.jsp") ||  
 						jspName.equals("submitEnvironmentalFactors.jsp") || 
 						jspName.equals("submitNutritionalFactors.jsp") || 
 						jspName.equals("submitGrowthFactors.jsp") ||  
 						jspName.equals("submitHormone.jsp") ||  
 						jspName.equals("submitRadiation.jsp") ||  
 						jspName.equals("submitViralTreatment.jsp")||  
 						jspName.equals("submitTransplantXenograft.jsp") ||  
 						jspName.equals("submitSurgeryOther.jsp") ||  
 						jspName.equals("submitPublications.jsp") ||  
 						jspName.equals("submitHistopathology.jsp")||  
 						jspName.equals("submitCellLines.jsp") || 
 						jspName.equals("submitTherapy.jsp") || 
 						jspName.equals("submitImages.jsp") ||  
 						jspName.equals("submitMicroarrayData.jsp") ||  
 						jspName.equals("submitJacksonLab.jsp") || 
 						jspName.equals("submitMMHCCRepo.jsp") ||  
 						jspName.equals("submitInvestigator.jsp") || 
                         jspName.equals("submitMorpholino.jsp") || 
 						jspName.equals("submitIMSR.jsp") ) {
 					return "subSubmitMenu.jsp";
 				}
 			else if ( 	jspName.equals("submitModels.jsp") || 
 					    jspName.equals("submitNewModel.jsp") ) {				
 					return "subEmptyMenu.jsp";				
 			} else {
 				return "subEmptyMenu.jsp";
 			}			
 		}
 }
