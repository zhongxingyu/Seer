 package gov.nih.nci.camod.webapp.action;
 
 import gov.nih.nci.camod.Constants;
 import gov.nih.nci.camod.domain.Agent;
 import gov.nih.nci.camod.domain.AnimalModel;
 import gov.nih.nci.camod.domain.SexDistribution;
 import gov.nih.nci.camod.domain.Therapy;
 import gov.nih.nci.camod.domain.Treatment;
 import gov.nih.nci.camod.service.AnimalModelManager;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 public class AnimalModelTreePopulateAction extends BaseAction  {
 	/**
 	 * Create the links for the submission subMenu
 	 * 
 	 * @param mapping
 	 * @param form
 	 * @param request
 	 * @param response
 	 * @return
 	 * @throws Exception
 	 */
 	public ActionForward execute( ActionMapping mapping, 
 								   ActionForm form,
 						           HttpServletRequest request,
 						           HttpServletResponse response )
 	  throws Exception {	
 		  
 		System.out.println( "<AnimalModelTreePopulateAction populate> Entering... " );
 				
     	// Grab the current modelID from the session
     	String modelID = (String) request.getSession().getAttribute( Constants.MODELID );    	
 	
         AnimalModelManager animalModelManager = (AnimalModelManager) getBean( "animalModelManager" );	
         
         AnimalModel animalModel = animalModelManager.get( modelID );	 
                 
         // Print the list of EnvironmentalFactors for the Cardiogenic Interventions Section
         List tyList = animalModel.getTherapyCollection();
         
         List surgeryList = new ArrayList();
         List hormoneList = new ArrayList();
         List growthFactorList = new ArrayList();
         List environFactorList = new ArrayList();        
         List radiationList = new ArrayList();
         
         System.out.println( "<AnimalModelTreePopulateAction> Building Tree ...");
         
         if( tyList == null || tyList.size() == 0 ){
         	System.out.println( "<AnimalModelTreePopulateAction populate> nothing!" );
         } else {
 	        for ( int i=0; i < tyList.size(); i++ )
 	        {
 	        	Therapy ty = (Therapy)tyList.get(i);
 	        	
 	        	//check to see if it is an EnvironmentalFactor
 	        	if ( ty.getTherapeuticExperiment() == false ) {	        		
 	        		Agent agent = ty.getAgent();	      
 	        		
 	        		if ( agent.getType().equals( "Other") ) {
 	        			System.out.println( "\tAdded therapy to surgeryList" );
 	        			surgeryList.add( ty );
 	        		}
 	        		if ( agent.getType().equals( "Hormone") ) {
 	        			System.out.println( "\tAdded therapy to hormoneList" );
 	        			hormoneList.add( ty );
 	        		}	  
 	        		if ( agent.getType().equals( "Growth Factor") ) {
 	        			System.out.println( "\tAdded therapy to growthFactorList" );
 	        			growthFactorList.add( ty );
 	        		}	
	        		if ( agent.getType().equals( "Environmental Factor") ) {
 	        			System.out.println( "\tAdded therapy to environFactorList" );
 	        			environFactorList.add( ty );
 	        		}	        		
 	        		if ( agent.getType().equals( "Radiation") ) {
 	        			System.out.println( "\tAdded therapy to radiationList" );
 	        			radiationList.add( ty );
 	        		}	 
 	        	}	 	        	
 	        }
         }
         
         request.getSession().setAttribute( Constants.Submit.GROWTHFACTORS_LIST, growthFactorList );
         request.getSession().setAttribute( Constants.Submit.HORMONE_LIST, hormoneList );
         request.getSession().setAttribute( Constants.Submit.SURGERYOTHER_LIST, surgeryList );
         request.getSession().setAttribute( Constants.Submit.ENVIRONMENTALFACTOR_LIST, environFactorList );
         request.getSession().setAttribute( Constants.Submit.RADIATION_LIST, radiationList );
         
 		return mapping.findForward( "submitOverview" );
 	}
 }
