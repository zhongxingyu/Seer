 package edu.wustl.query.action;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import edu.wustl.query.domain.SelectedConcept;
 import edu.wustl.query.util.global.Constants;
 /**
  * @author amit_doshi
  * Action Class to responsible to handle the selected permissible values from VI
  */
 public class SelectedPermissibleValueAction extends Action {
 	
 	/**
 	 * This method loads the data required for categorySearch.jsp
 	 * @param mapping mapping
 	 * @param form form
 	 * @param request request
 	 * @param response response
 	 * @throws Exception Exception
 	 * @return ActionForward actionForward
 	 */
 	@Override
 	public ActionForward execute(ActionMapping mapping, ActionForm form,HttpServletRequest request, HttpServletResponse response)throws Exception
 	{
 		List<SelectedConcept> selectedConceptList=new ArrayList<SelectedConcept>();
 		String conceptCodes=request.getParameter("ConceptCodes");
 		String conceptName=request.getParameter("ConceptName");
 		// codes contains the actual vocab Name+@+version:med Concept code
 		String[] conceptcodes= conceptCodes.split("#");
 		// Name contains the actual selected vocab concept code:Concept Name
		String[] conceptNames= conceptName.split("#");
 		
 		for(int i=0;i<conceptcodes.length;i++)
 		{
 			SelectedConcept selectedConcept=new SelectedConcept();
 			String vocabName=conceptcodes[i].substring(0,conceptcodes[i].indexOf('@'));
 			String vocabVersion=conceptcodes[i].substring(conceptcodes[i].indexOf('@')+1,conceptcodes[i].indexOf(':'));
 			String medCode=conceptcodes[i].substring(conceptcodes[i].indexOf(':')+1);
 			String conceptCode=conceptNames[i].substring(0,conceptNames[i].indexOf(':'));
 			String conceptNam=conceptNames[i].substring(conceptNames[i].indexOf(':')+1);
 			
 			selectedConcept.setVocabName(vocabName);
 			selectedConcept.setVocabVersion(vocabVersion);
 			selectedConcept.setMedCode(medCode);
 			selectedConcept.setConceptName(conceptNam);
 			selectedConcept.setConceptCode(conceptCode);
 			selectedConceptList.add(selectedConcept);
 		}
 		request.getSession().setAttribute(Constants.SELECTED_CONCEPT_LIST, selectedConceptList);
 		
 		response.getWriter().write("success");
 		return null;
 	}
 }
