 package gov.nih.nci.camod.webapp.util;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.action.ActionMessages;
 
 public class SidebarUtil extends gov.nih.nci.camod.webapp.action.BaseAction {
 
 		public String findSubMenu( HttpServletRequest request, String jspName )
 		{			
 			System.out.println("<sidebar.jsp> String jspName=" + jspName );
 			
 			if( 		jspName.equals("searchSimple.jsp") || 
 						jspName.equals("searchHelp.jsp") || 
 						jspName.equals("searchAdvanced.jsp") || 
 						jspName.equals("searchTableOfContents.jsp") || 
 						jspName.equals("searchResults.jsp")) {
 				return "subSearchMenu.jsp";
 			} 
 			else if ( 	jspName.equals("viewModelCharacteristics.jsp") || 
 						jspName.equals("viewSpontaneousMutation.jsp") || 
 						jspName.equals("viewTransplantXenograft.jsp") || 
 						jspName.equals("viewGenomicSegment.jsp") || 
 						jspName.equals("viewTargetedModification.jsp") || 
 						jspName.equals("viewInducedMutation.jsp") || 
 						jspName.equals("viewEngineeredTransgene.jsp") || 
 						jspName.equals("viewGeneticDescription.jsp") || 
 						jspName.equals("viewPublications.jsp") ||
 						jspName.equals("viewCarcinogenicInterventions.jsp")  || 
 						jspName.equals("viewHistopathology.jsp")  || 
 						jspName.equals("viewTherapeuticApproaches.jsp") || 
 						jspName.equals("viewCellLines.jsp") || 
 						jspName.equals("viewImages.jsp") || 
 						jspName.equals("viewMicroarrays.jsp") ){
 					return "subViewModelMenu.jsp";
 			} 
 			else if (	jspName.equals("adminRoles.jsp") || 
 						jspName.equals("helpAdmin.jsp") || 
 						jspName.equals("adminUsersAppointment.jsp") || 
 						jspName.equals("adminModelsAssignment.jsp") || 
 						jspName.equals("adminCommentScreening.jsp")  || 
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
