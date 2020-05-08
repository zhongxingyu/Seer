 
 package edu.wustl.query.bizlogic;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import edu.common.dynamicextensions.domain.StringValue;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.PermissibleValueInterface;
 import edu.wustl.common.bizlogic.DefaultBizLogic;
 import edu.wustl.common.query.factory.PermissibleValueManagerFactory;
 import edu.wustl.common.query.pvmanager.impl.LexBIGPermissibleValueManager;
 import edu.wustl.common.query.pvmanager.impl.PVManagerException;
 import edu.wustl.common.util.global.Variables;
 import edu.wustl.common.util.logger.Logger;
 import edu.wustl.common.vocab.IConcept;
 import edu.wustl.common.vocab.IDefinition;
 import edu.wustl.common.vocab.IPresentation;
 import edu.wustl.common.vocab.IVocabulary;
 import edu.wustl.common.vocab.IVocabularyManager;
 import edu.wustl.common.vocab.VocabularyException;
 import edu.wustl.common.vocab.impl.Definition;
 import edu.wustl.common.vocab.impl.VocabularyManager;
 import edu.wustl.common.vocab.utility.VIError;
 import edu.wustl.common.vocab.utility.VocabUtil;
 import edu.wustl.query.util.global.Constants;
 import edu.wustl.query.util.global.VIProperties;
 import edu.wustl.vi.enums.VISearchAlgorithm;
 
 
 /**
  * @author amit_doshi
  * Class to hold the bizlogic of Vocabulary Interface
  */
 public class SearchPermissibleValueBizlogic extends DefaultBizLogic
 {
 
 	private LexBIGPermissibleValueManager pvManager = (LexBIGPermissibleValueManager)
 			PermissibleValueManagerFactory.getPermissibleValueManager();
 	private IVocabularyManager vocabularyManager = VocabularyManager.getInstance();
 	
 	/**
 	 * This method returns List of vocabularies
 	 * @return
 	 * @throws VocabularyException
 	 */
 	public List<IVocabulary> getVocabularies() throws VocabularyException
 	{
 		return vocabularyManager.getConfiguredVocabularies();
 	}
 	/**
 	 * This method returns the list of permissible values 
 	 * @param attribute
 	 * @param entity
 	 * @return
 	 * @throws PVManagerException
 	 */
 	/*public List<IConcept> getPermissibleValueList(AttributeInterface attribute,
 			EntityInterface entity) throws PVManagerException
 	{
 		List<IConcept> permissibleConcepts = new ArrayList<IConcept>();
 		try
 		{
 			List<PermissibleValueInterface> permissibleValues = pvManager.getPermissibleValueList(
 					attribute, entity);
 			IVocabulary sourceVocabulary = getVocabulary(VIProperties.sourceVocabUrn);
 			permissibleConcepts = resolvePermissibleCodesToConcept(sourceVocabulary, permissibleValues);
 		}
 		catch (VocabularyException e)
 		{
 			throw new PVManagerException(
 					"Server encountered a problem in fetching the permissible values for "
 							+ entity.getName(), e);
 		}
 		return permissibleConcepts;
 	}*/
 	/**
 	 * This method returns the list of permissible values 
 	 * @param attribute
 	 * @param entity
 	 * @return
 	 * @throws PVManagerException
 	 */
 	public List<IConcept> getConfiguredPermissibleValueList(AttributeInterface attribute,
 			EntityInterface entity,List<Integer> showMessage) throws PVManagerException
 	{
 		List<IConcept> permissibleConcepts =  new ArrayList<IConcept>();;
 		try
 		{
 			List<PermissibleValueInterface> permissibleValues = pvManager.getPermissibleValueList(
 					attribute, entity);
 			/*If the permissible values are more than the configured value
 			 * take the subset.
 			 */
 		/*	if(permissibleValues.size() > VIProperties.maxPVsToShow)
 			{
 				permissibleValues = permissibleValues.subList(0, VIProperties.maxPVsToShow);
 				showMessage.add(true);// may be all concept will not resolved
 			}*/
 			IVocabulary sourceVocabulary = getVocabulary(VIProperties.sourceVocabUrn);
 			//permissibleConcepts = resolvePermissibleCodesToConcept(sourceVocabulary,permissibleValues);
 			if(permissibleValues != null)
 			{
 				for(PermissibleValueInterface perValue:permissibleValues)
 				{
 					List<IConcept> concepts = sourceVocabulary.getConceptForCode(perValue.getValueAsObject().toString());
 					if(permissibleConcepts.size()>=VIProperties.maxPVsToShow)
 					{
 						showMessage.add(1);
 						showMessage.add(permissibleConcepts.size());//number of result to show
 						showMessage.add(permissibleValues.size());//total number of result
 						break;
 					}
 					if(concepts != null && !concepts.isEmpty())
 					{
 						permissibleConcepts.addAll(concepts);
 					}
 				}
 			}
 		}
 		catch (VocabularyException e)
 		{
 			throw new PVManagerException(
 					"Server encountered a problem in fetching the permissible values for "
 							+ entity.getName(), e);
 		}
 		return permissibleConcepts;
 	}
 	
 	/*private List<IConcept> resolvePermissibleCodesToConcept(IVocabulary sourceVocabulary,
 			List<PermissibleValueInterface> permissibleValues) throws VocabularyException
 	{
 		List<IConcept> permissibleConcepts = new ArrayList<IConcept>();
 		if(permissibleValues != null)
 		{
 			for(PermissibleValueInterface perValue:permissibleValues)
 			{
 				List<IConcept> concepts = sourceVocabulary.getConceptForCode(perValue.getValueAsObject().toString());
 				if(concepts != null && !concepts.isEmpty())
 				{
 					permissibleConcepts.addAll(concepts);
 				}
 			}
 		}
 		return permissibleConcepts;
 	}*/
 	
 	public List<PermissibleValueInterface> getPermissibleValueListFromDB(AttributeInterface attribute,
 			EntityInterface entity) throws PVManagerException
 	{
 		return pvManager.getPermissibleValueList(attribute, entity);
 	}
 	/**
 	 * This method returns the Mapped concept code of target vocabulary with source vocabulary
 	 * @param attribute
 	 * @param targetVocabName
 	 * @param targetVocabVer
 	 * @param entity
 	 * @return
 	 * @throws VocabularyException
 	 * @throws PVManagerException
 	 */
 	public Map<String, List<IConcept>> getMappedConcepts(AttributeInterface attribute,
 			IVocabulary targetVocabulary, EntityInterface entity,List<Integer> showMessage)
 			throws VocabularyException, PVManagerException
 	{
 		IVocabulary sourceVocabulary = getVocabulary(VIProperties.sourceVocabUrn);
 		int maxPVsToShow=0;
 		Map<String, List<IConcept>> mappedConcepts = new HashMap<String, List<IConcept>>();
 		try
 		{
 			List<PermissibleValueInterface> permissibleValues = pvManager.getPermissibleValueList(
 					attribute, entity);
 			
 			if(permissibleValues !=null && !permissibleValues.isEmpty())
 			{
 				for(PermissibleValueInterface perValueInterface:permissibleValues)
 				{
 					List<IConcept> conList = sourceVocabulary.getMappedConcepts(perValueInterface.getValueAsObject().toString(), VIProperties.translationAssociation, targetVocabulary);
 					if(conList != null && !conList.isEmpty())
 					{
 						mappedConcepts.put(perValueInterface.getValueAsObject().toString(), conList);
 						maxPVsToShow=maxPVsToShow+conList.size();
 						if(maxPVsToShow>=VIProperties.maxPVsToShow)
 						{
 							showMessage.add(1);
 							showMessage.add(maxPVsToShow);//number of result to show
 							showMessage.add(permissibleValues.size());//total number of result
 							break;
 						}
 					}
 				}
 			}
 		}
 		catch (VocabularyException e)
 		{
 			Logger.out.error(e.getMessage(),e);
 		}
 
 		return mappedConcepts;
 	}
 	
 	/**
 	 * This method will search the given term in across all the vocabularies
 	 * @param term
 	 * @param vocabName
 	 * @param vocabVersion
 	 * @return
 	 * @throws VocabularyException
 	 */
 	public List<IConcept> searchConcept(String term, String vocabURN,int maxToReturn,String  searchCriteria)
 			throws VocabularyException
 	{
 		IVocabulary vocabulary = vocabularyManager.getVocabulary(vocabURN);
 		VISearchAlgorithm searchAloAlgorithm=VISearchAlgorithm.valueOf(searchCriteria);
 		
 		if(searchAloAlgorithm.equals(VISearchAlgorithm.EXACT_PHRASE))
 		{
 			if(term.indexOf("\"")!=-1)
 			{
 				term = term.replace("\"", "");
 			}
 			term = "\"" + term + "\"";
 		}
 		
 		return vocabulary.searchConcept(term,searchAloAlgorithm.getAlgorithm(), maxToReturn);
 
 	}
 	
 	/**
 	 * This method returns the message no result found 
 	 * @return
 	 */
 	public String getNoMappingFoundHTML()
 	{
 		return "<tr><td>&nbsp;</td><td class='black_ar_tt'>" + Constants.NO_RESULT + "<td></tr>";
 	}
 	/**
 	 * This method returns the message no result found 
 	 * @return
 	 * @throws VocabularyException 
 	 */
 	public String getInfoMessage(int noResult,int totalResult) throws VocabularyException
 	{
 		System.out.println( VocabUtil.getVocabProperties().getProperty("too.many.results.default.message."));
 		String message=VocabUtil.getVocabProperties().getProperty("too.many.results.default.message");
 		message=message.replace("##", noResult+"");
 		message=message.replace("@@", totalResult+"");
 		return Constants.MSG_DEL+message ;
 	}
 	/**
 	 * This method returns the HTML for child nodes for all the vocabularies which
 	 *  contains the permissible ,concept code and check box
 	 * @param vocabName
 	 * @param vocabversoin
 	 * @param concept
 	 * @param checkboxId
 	 * @return
 	 */
 	public String getHTMLForConcept(String vocabURN,
 			IConcept concept, String checkboxId)
 	{
 		String value=concept.getCode() + ":" + concept.getDescription();
 		String toolTip = getToolTip(concept);
 		toolTip=org.apache.commons.lang.StringEscapeUtils.escapeJavaScript(toolTip); //bug Fixed:# 11665
 		return "<tr onmouseover=\"Tip('"+toolTip+"');\" onmouseout=\"UnTip();\"><td style=\"padding-left:30px\">&nbsp;</td><td class=\"black_ar_tt\" > \n"
 				+ "<input type=\"checkbox\" name=\"" +vocabURN + "\" id=\""
 				+ checkboxId + "\" value=\"" + value
 				+ "\" onclick=\"getCheckedBoxId('" + checkboxId + "');\">"
 				+ "</td><td class=\"black_ar_tt\" nowrap>&nbsp;" /*+ concept.getCode() + ":"*/
 				+ concept.getDescription() + "\n" + "<td></tr>";
 	}
 	/**
 	 * @param concept
 	 * @return
 	 */
 	private String getToolTip(IConcept concept)
 	{
 		StringBuffer toolTip = new StringBuffer("Concept Code:");
 		toolTip.append(concept.getCode());
 		toolTip.append("<br/>");
 		String definition=Constants.NOT_AVAILABLE;
 		List<IDefinition> defsList=concept.getDefinition();
 		if(defsList!=null &&  !defsList.isEmpty())
 		{
 			for(IDefinition defs:defsList)
 			{
 				Definition def=(Definition)defs;
 				if(def.isPreferred())
 				{
 					definition=def.getDescription();
 					break;
 				}
 			}
 		}
 		
 		toolTip.append("Definition : ");
 		toolTip.append(definition);
 		
 		List<IPresentation> preList = concept.getPresentation();
 		if(preList != null && !preList.isEmpty())
 		{
 			for(IPresentation presentation:preList)
 			{
 				if(presentation.getName().equals(Constants.MED_CONECPT_NAME))
 				{
 					toolTip.append("<br/>");
 					toolTip.append("Med Concept Name : ");
 					toolTip.append(presentation.getDescription());
 				}
 			}
 		}
 		
 		return toolTip.toString();
 	}
 	/**
 	 * This method returns the HTML for child nodes for all the vocabularies which
 	 *  contains the permissible ,concept code and check box
 	 * @param vocabName
 	 * @param vocabversoin
 	 * @param concept
 	 * @param checkboxId
 	 * @param status 
 	 * @return
 	 * @throws VocabularyException 
 	 */
 	public String getHTMLForSearchedConcept(String vocabURN, 
 			IConcept concept, String checkboxId, String textStatus) throws VocabularyException
 	{
 	
 		String chkBoxStatus="";
 		String cssClass=textStatus;
 		if(textStatus.indexOf("Disabled")>-1)
 		{
 			chkBoxStatus="disabled";
 		}
 		String toolTip = getToolTip(concept);
 		toolTip=org.apache.commons.lang.StringEscapeUtils.escapeJavaScript(toolTip);//bug Fixed:# 11665
 		return "<tr onmouseover=\"Tip('"+toolTip+"');\" onmouseout=\"UnTip();\"><td style=\"padding-left:30px\">&nbsp;</td><td class=\"black_ar_tt\"> \n"
 				+ "<input type=\"checkbox\" "+chkBoxStatus+" name=\""+ vocabURN + "\" id=\""
 				+ checkboxId + "\" value=\"" + concept.getCode() + ":" + concept.getDescription()
 				+ "\" onclick=\"getCheckedBoxId('" + checkboxId + "');\">"
 				+ "</td><td class='"+cssClass+"' nowrap>&nbsp;" /*+ concept.getCode() + ":"*/
 				+ concept.getDescription() + "\n" + "<td></tr>";
 	}
 	/**
 	 * This method will return HTML for the root node of each vocabularies that require 
 	 * while creating the tree like structure to show the result 
 	 * @param vocabName
 	 * @param vocabVer
 	 * @param vocabDisName
 	 * @return
 	 * @throws VocabularyException
 	 */
 	public String getRootVocabularyNodeHTML(String vocabURN, String vocabDisName)
 						throws VocabularyException
 	{
 		String  style="display:";
 		String  imgpath="src=\"images/advancequery/nolines_minus.gif\"/"; 
 		String tableHTML = "<table cellpadding ='0' cellspacing ='1'>";
 		return tableHTML 
 				+ "<tr><td>"
 				+ tableHTML + "<tr><td class='grid_header_text'>"
 				+ "<a id=\"image_"+ vocabURN+ "\"\n"
 				+ "onClick=\"showHide('inner_div_"+ vocabURN+ "'," 
 				+"'image_"+ vocabURN+ "');\">\n"
 				+ "<img "+ imgpath+ " align='absmiddle'/></a>"
 				+ "</td><td><input type='checkbox' name='"+ vocabURN
 				+ "' id='root_"	+ vocabURN+ "' "
 				+ "value='"	+ vocabURN
 				+ "' onclick=\"setStatusOfAllCheckBox(this.id);\"></td>"
 				+ "<td align='middle'  class='grid_header_text'>&nbsp;&nbsp;"
 				+ vocabDisName
 				+ "\n"
 				+ "</td></tr></table>"
 				+ "</td></tr><tr><td><div id='inner_div_"+ vocabURN+ "'style='"+style+"'>"
 				+tableHTML;
 	}
 	/**
 	 * This method will return HTML for the root node of each vocabularies that require 
 	 * while creating the tree like structure to show the result 
 	 * @param vocabName
 	 * @param vocabVer
 	 * @param vocabDisName
 	 * @return
 	 */
 	public String getRootVocabularyHTMLForSearch(String vocabURN,
 			String vocabDisName)
 	{
 		String style="display:"; 
 		String imgpath="src=\"images/advancequery/nolines_minus.gif\"/";
 		String tableHTML = "<table cellpadding ='0' cellspacing ='1'>";
 		return tableHTML 
 				+ "<tr><td>"
 				+ tableHTML
 				+ "<tr><td class='grid_header_text'><a id=\"image_"+ vocabURN+ "\"\n"
 				+ "onClick=\"showHide('inner_div_"+ vocabURN+ "',"
 				+ "'image_"+vocabURN+ "');\">\n"
 				+ "<img "+ imgpath+ "align='absmiddle'></a>"
 				+ "</td><td><input type='checkbox' name='"+ vocabURN
 				+ "' id='root_"	+ vocabURN+ "' "
 				+ "value='"+ vocabURN+ "'"
 				+ " onclick=\"setStatusOfAllCheckBox(this.id);\"></td>"
 				+ "<td align='middle'  class='grid_header_text'>&nbsp;&nbsp;"
 				+ vocabDisName
 				+ "\n"
 				+ "</td></tr></table>"
 				+ "</td></tr><tr><td><div id='inner_div_"+ vocabURN+"'style='"+style+"'>"
 				+tableHTML ;
 	}
 	/**
 	 * This method returns end HTML for Tree like structure
 	 * @return
 	 */
 	public String getEndHTML()
 	{
 		return "</table></div></td></tr></table>";
 	}
 
 	/**
 	 * This method returns  HTML Error message
 	 * @return
 	 */
 	public String getErrorMessageAsHTML(String msg)
 	{
 		return "<table width='100%' height='100%'>"
 				+ "<tr><td class='black_ar_tt' style='color:red' valign='top'>"
 				+msg+
 				"<td></tr></table>";
 	}
 	/**
 	 * 
 	 * @param targetConcept
 	 * @param relationType
 	 * @param baseVocab
 	 * @return
 	 * @throws VocabularyException
 	 */
 	public  List<IConcept> isSourceVocabCodedTerm(IConcept targetConcept, String associationName, IVocabulary baseVocab) throws VocabularyException
 	{
 		 List<IConcept> concepts= baseVocab.getSourceOf(targetConcept.getCode(), associationName, targetConcept.getVocabulary());
 		return concepts;
 		
 	}
 	/**
 	 * 
 	 * @param concept
 	 * @param pvList
 	 * @return
 	 */
 	public IConcept isConceptExistInPVList(IConcept concept,List<PermissibleValueInterface> pvList)
 	{
 		IConcept medConcept=null;
 		for(PermissibleValueInterface pValue: pvList)
 		{
 			StringValue pvs= (StringValue)pValue;
 			if(pvs.getValue().equals(concept.getCode()))
 			 {
 				 medConcept= concept;
 			 }
 		}
 		 return medConcept;
 	}
 	/**
 	 * 
 	 * @param concepts
 	 * @param pvList
 	 * @return
 	 */
 	public IConcept isConceptsExistInPVList(List<IConcept> concepts, List<PermissibleValueInterface> pvList)
 	{
 		IConcept sourceConcept=null;
 		for(IConcept conc:concepts)
 		{
 			
 			for(PermissibleValueInterface pValue: pvList)
 			{
 				StringValue pvs= (StringValue)pValue;
 				if(pvs.getValue().equals(conc.getCode()))
 				 {
 					sourceConcept= conc;
 					break;
 				 }
 			}
 			if(sourceConcept!=null)
 			{
 				break;
 			}
 		}
 		return sourceConcept;
 	}
 	
 	/**
 	 * This method returns the display name for given vocabulary Name and vocabulary version
 	 * @param vocabName
 	 * @param vocabVer
 	 * @return
 	 * @throws VocabularyException
 	 */
 	public String getDisplayNameForVocab(String vocabURN)
 			throws VocabularyException
 	{
 		SearchPermissibleValueBizlogic bizLogic = (SearchPermissibleValueBizlogic) BizLogicFactory
 				.getInstance().getBizLogic(Constants.SEARCH_PV_FROM_VOCAB_BILOGIC_ID);
 		List<IVocabulary> vocabularies = bizLogic.getVocabularies();
 		String vocabDisName = "";
 		for (IVocabulary vocabulary : vocabularies)
 		{
 			if (vocabulary.getVocabURN().equals(vocabURN))
 			{
 				vocabDisName = vocabulary.getDisplayName();
 				break;
 			}
 		}
 		if (vocabDisName.equals(""))
 		{
 			throw new VocabularyException("Could not find the vocabulary.",VIError.SYSTEM_ERR);
 		}
 		return vocabDisName;
 	}
 
 	public IVocabulary getVocabulary(String urn) throws VocabularyException
 	{
 		IVocabulary vocabulary = vocabularyManager.getVocabulary(urn);
 		return vocabulary;
 	}
 	public String getSearchMessage() throws VocabularyException
 	{
 		String message=VocabUtil.getVocabProperties().getProperty("too.many.results.search.message");
 		return Constants.MSG_DEL+message ;
 		
 	}
 	public String getExceptionMessage(VocabularyException e)
 	{
 		String message="";
 		if(e.getError().equals(VIError.LB_PARAM_EXCPTION))
 		{
 				message = getMessageFromFile();
 		}
 		else
 		{
 			message= getErrorMessageAsHTML(e.getError().getErrorMessage());
 		}
 		return message;
 	}
 	/**
 	 * @param message
 	 * @return
 	 */
 	public String getMessageFromFile()
 	{
 		FileReader fr=null;
 		String message="";
 		try
 		{
				fr = new FileReader(Variables.applicationHome+System.getProperty("file.separator")+"WEB-INF"+System.getProperty("file.separator")+
							"classes"+System.getProperty("file.separator")+"VISplCharHelp.txt");
 				BufferedReader br=new BufferedReader(fr);
 				String msg="";
 				String line="";
 				while((line=br.readLine())!=null)
 				{
 					msg=msg+line;
 				}
 				 message= getErrorMessageAsHTML(msg);
 		}
 		catch (FileNotFoundException e1)
 		{
 				
 				e1.printStackTrace();
 		}
 		catch (IOException e2)
 		{
 				// TODO Auto-generated catch block
 				e2.printStackTrace();
 		}
 				return message;
 	}
 }
