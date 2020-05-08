 package edu.wustl.query.action;
 
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import edu.common.dynamicextensions.domain.Entity;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.wustl.cab2b.server.cache.EntityCache;
 import edu.wustl.common.query.pvmanager.impl.PVManagerException;
 import edu.wustl.common.vocab.IConcept;
 import edu.wustl.common.vocab.IVocabulary;
 import edu.wustl.common.vocab.VocabularyException;
 import edu.wustl.common.vocab.impl.Vocabulary;
 import edu.wustl.query.bizlogic.BizLogicFactory;
 import edu.wustl.query.bizlogic.SearchPermissibleValueBizlogic;
 import edu.wustl.query.util.global.Constants;
 import edu.wustl.query.util.global.VIProperties;
 
 /**
  * @author amit_doshi
  * Action Class to show the UI for Vocabulary Interface and to handle the Ajax request
  */
 public class SearchMappedPVsAction extends Action
 {
 
 	/**
 	 * This method handles the various Ajax request for VI
 	 * @param mapping mapping
 	 * @param form form
 	 * @param request request
 	 * @param response response
 	 * @throws Exception Exception
 	 * @return ActionForward actionForward
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public ActionForward execute(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response) throws Exception
 	{
 		
 		final String targetVocabURN = request.getParameter(Constants.SELECTED_BOX);
 		//get the id of the component on which user click to search for PVs
 		String componentId = request.getParameter(Constants.COMPONENT_ID);
 		String editVocabURN = request.getParameter("editVocabURN");
 		componentId = getComponentId(request, componentId);
 		
 		String entityName = (String) request.getSession().getAttribute(Constants.ENTITY_NAME);
 		Entity entity = (Entity) EntityCache.getCache().getEntityById(Long.valueOf((entityName)));
 		Map<String, AttributeInterface> enumAttributeMap = (HashMap<String, AttributeInterface>)
 		request.getSession().getAttribute(Constants.ENUMRATED_ATTRIBUTE);
 		
 		AttributeInterface attribute = (AttributeInterface) 
 		enumAttributeMap.get(Constants.ATTRIBUTE_INTERFACE + componentId);
 		
 		if (targetVocabURN != null)// Need to retrieve HTML for requested Vocabulary Mapped or sou
 		{
 			// user clicked on radio boxes
 			//AJAX Request handler for Getting Mapping data for source or target vocabularies
 			String htmlResponse = getPVsForRequestedVoab(request, targetVocabURN, componentId,
 					entity, attribute);
 			response.getWriter().write(htmlResponse);
 			return null;
 		}
 		
 		//new request for entity; remove the message from the session 
 		removeHTMLFromSesson(request);
 		if(editVocabURN.equals("null") || editVocabURN.equals(VIProperties.sourceVocabUrn) )
 		{
 			/* load source vocabulary if in edit mode as well as in not edit mode*/
 			String srcHTML=getPVsForSourceVocab(attribute, entity, componentId, request);
 			//set the data in session because need to show this data on page load
 			request.getSession().setAttribute(Constants.PV_HTML+VIProperties.sourceVocabUrn, srcHTML);
 			
 		}
 		else
 		{
 			//need to load other vocabulary in edit mode
 			 setEditVocabHTML(request, editVocabURN, entity, attribute);
 		}
 		
 		setComponentId(request, componentId);
 		SearchPermissibleValueBizlogic bizLogic = (SearchPermissibleValueBizlogic) BizLogicFactory
 		.getInstance().getBizLogic(Constants.SEARCH_PV_FROM_VOCAB_BILOGIC_ID);
         request.getSession().setAttribute(Constants.VOCABULIRES, bizLogic.getVocabularies());
 		return mapping.findForward(edu.wustl.query.util.global.Constants.SUCCESS);
 	}
 	/**
 	 * @param request
 	 * @param editVocabURN
 	 * @param entity
 	 * @param attribute
 	 * @throws VocabularyException
 	 * @throws PVManagerException
 	 */
 	private void setEditVocabHTML(HttpServletRequest request, String editVocabURN, Entity entity,
 			AttributeInterface attribute) throws VocabularyException, PVManagerException
 	{
 		String trgHTML=getMappingForTargetVocab(editVocabURN, attribute, entity);
 		 String[] trgHTMLAll=trgHTML.split(Constants.MSG_DEL);
 		 if(trgHTMLAll.length>1)
 		 {
 			 request.getSession().setAttribute(Constants.PV_HTML+editVocabURN, trgHTMLAll[0]);
 			 request.getSession().setAttribute(Constants.SRC_VOCAB_MESSAGE, trgHTMLAll[1]);
 		 }
 		 else
 		 {
 			 request.getSession().setAttribute(Constants.PV_HTML+editVocabURN, trgHTML);
 		 }
 	}
 	/**
 	 * @param request
 	 * @param targetVocabURN
 	 * @param componentId
 	 * @param entity
 	 * @param attribute
 	 * @return
 	 * @throws VocabularyException
 	 * @throws PVManagerException
 	 */
 	private String getPVsForRequestedVoab(HttpServletRequest request, final String targetVocabURN,
 			String componentId, Entity entity, AttributeInterface attribute)
 			throws VocabularyException, PVManagerException
 	{
 		String htmlResponse =null;
 		if(targetVocabURN.equals(VIProperties.sourceVocabUrn))
 		{
 			htmlResponse= getPVsForSourceVocab(attribute, entity, componentId, request);
 		}
 		else
 		{
 			htmlResponse= getMappingForTargetVocab(targetVocabURN, attribute, entity);
 		}
 		return htmlResponse;
 	}
 	/**
 	 * @param request
 	 * @param componentId
 	 */
 	private void setComponentId(HttpServletRequest request, String componentId)
 	{
 		if (componentId != null)
 		{
 			request.getSession().setAttribute(Constants.COMPONENT_ID, componentId);
 		}
 	}
 	/**
 	 * @param request
 	 * @param componentId
 	 * @return
 	 */
 	private String getComponentId(HttpServletRequest request, String componentId)
 	{
 		if (componentId == null)
 		{
 			//need to save component id into the session for next Ajax requests
 			componentId = (String) request.getSession().getAttribute(Constants.COMPONENT_ID);
 		}
 		return componentId;
 	}
 	/**
 	 * @param request
 	 */
 	@SuppressWarnings("unchecked")
 	private void removeHTMLFromSesson(HttpServletRequest request)
 	{
 		request.getSession().removeAttribute(Constants.SRC_VOCAB_MESSAGE);
 		Enumeration attributeNames = request.getSession().getAttributeNames();
 		while(attributeNames.hasMoreElements())
 		{
 			String atr=attributeNames.nextElement().toString();
 			if(atr.indexOf(Constants.PV_HTML)==0)
 			{
 				request.getSession().removeAttribute(atr);
 			}
 		}
 	}
 		/**
 	 * This method generate the HTML for the Source vocabulary  (MED 1.0)
 	 * @param attribute
 	 * @param entity
 	 * @param componentId
 	 * @param request
 	 * @throws VocabularyException
 	 * @throws PVManagerException
 	 */
 	private String getPVsForSourceVocab(AttributeInterface attribute, EntityInterface entity,
 			String componentId, HttpServletRequest request) throws VocabularyException,
 			PVManagerException
 	{
 
 		SearchPermissibleValueBizlogic bizLogic = (SearchPermissibleValueBizlogic) BizLogicFactory
 				.getInstance().getBizLogic(Constants.SEARCH_PV_FROM_VOCAB_BILOGIC_ID);
 		StringBuffer html = new StringBuffer();
 	
 		List<IConcept> pvList = bizLogic.getConfiguredPermissibleValueList(attribute, entity);
 		String srcVocabURN = VIProperties.sourceVocabUrn;
 		String vocabDisName = bizLogic.getDisplayNameForVocab(srcVocabURN);
 		html.append(bizLogic.getRootVocabularyNodeHTML(srcVocabURN, vocabDisName));
 		if(pvList != null && !pvList.isEmpty())
 		{
 			for(IConcept concept:pvList)
 			{
				String checkboxId = srcVocabURN + Constants.ID_DEL + concept.getCode();
 				html.append(bizLogic.getHTMLForConcept(srcVocabURN,concept,checkboxId));
 			}
 			html.append(bizLogic.getEndHTML());
 			if( pvList.size()==VIProperties.maxPVsToShow)// Need to show Message Too Many Result on UI 
 			{
 				html.append(bizLogic.getInfoMessage());
 				request.getSession().setAttribute(Constants.SRC_VOCAB_MESSAGE, bizLogic.getInfoMessage()
 						.replace(Constants.MSG_DEL,""));
 			}
 		}
 		else
 		{
 			html.append(bizLogic.getNoMappingFoundHTML());
 			html.append(bizLogic.getEndHTML());
 		}
 		
 		return html.toString();
 		
 	}
 	
 	/**
 	 * This method returns the data mapped vocabularies
 	 * @param targetVocabURN
 	 * @param attribute
 	 * @param entity
 	 * @return
 	 * @throws VocabularyException
 	 * @throws PVManagerException
 	 */
 	private String getMappingForTargetVocab(String targetVocabURN, AttributeInterface attribute,
 			EntityInterface entity) throws VocabularyException, PVManagerException
 	{
 
 		SearchPermissibleValueBizlogic bizLogic = (SearchPermissibleValueBizlogic) BizLogicFactory
 				.getInstance().getBizLogic(Constants.SEARCH_PV_FROM_VOCAB_BILOGIC_ID);
 		IVocabulary souVocabulary = bizLogic.getVocabulary(VIProperties.sourceVocabUrn);
 		// Get the target vocabulary info from parameter
 		
 		String targetVocabDisName=bizLogic.getDisplayNameForVocab(targetVocabURN);
 		IVocabulary targVocabulary = bizLogic.getVocabulary(targetVocabURN);
 		
 		StringBuffer html = new StringBuffer();
 		if (!((Vocabulary)souVocabulary).equals(targVocabulary))
 			{
 				html.append(bizLogic.getRootVocabularyNodeHTML(targetVocabURN,targetVocabDisName));
 				Map<String, List<IConcept>> vocabMappings = bizLogic.getMappedConcepts(attribute,
 						targVocabulary, entity);
 				html.append(getMappedHTMLForTargetVocab(targetVocabURN,vocabMappings));
 	
 			}
 		return html.toString();
 	}
 	/**
 	 * This method returns the mapping data as HTML
 	 * @param html
 	 * @param vocabName
 	 * @param vocabversoin
 	 * @param vocabMappings
 	 * @throws VocabularyException 
 	 * @throws NumberFormatException 
 	 */
 	private StringBuffer getMappedHTMLForTargetVocab(String vocabURN,
 			Map<String, List<IConcept>> vocabMappings) throws NumberFormatException, VocabularyException
 	{
 		SearchPermissibleValueBizlogic bizLogic = (SearchPermissibleValueBizlogic) BizLogicFactory
 				.getInstance().getBizLogic(Constants.SEARCH_PV_FROM_VOCAB_BILOGIC_ID);
 		StringBuffer mappedHTML=new StringBuffer();
 		int displayPVCount=1;
 		int maxPv=0;
 		boolean isMsgDisplayed=false;
 		
 		if (vocabMappings != null && vocabMappings.size()!=0)
 		{
 			Set<String> keySet = vocabMappings.keySet();
 			Iterator<String> iterator = keySet.iterator();
 			while (iterator.hasNext())
 			{
 				String conceptCode = iterator.next();
 				List<IConcept> mappingList = vocabMappings.get(conceptCode);
 				maxPv=maxPv+mappingList.size();
 				ListIterator<IConcept> mappingListItr = mappingList.listIterator();
 				while (mappingListItr.hasNext())
 				{
 					IConcept concept = (IConcept) mappingListItr.next();
 					String checkboxId = vocabURN + Constants.ID_DEL + conceptCode;
 					mappedHTML.append(bizLogic.getHTMLForConcept(vocabURN,concept, checkboxId));
 				}
 					
 			}
 		}
 		else
 		{
 			mappedHTML.append(bizLogic.getNoMappingFoundHTML());
 		}
 		
 		mappedHTML.append(bizLogic.getEndHTML());
 		if(maxPv>=VIProperties.maxPVsToShow)
 		{
 			mappedHTML.append(bizLogic.getInfoMessage());
 		}
 		return mappedHTML;
 	}
 }
