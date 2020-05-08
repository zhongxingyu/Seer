 
 package edu.wustl.query.bizlogic;
 
 import java.util.ArrayList;
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
 import edu.wustl.common.util.logger.Logger;
 import edu.wustl.common.vocab.IConcept;
 import edu.wustl.common.vocab.IVocabulary;
 import edu.wustl.common.vocab.IVocabularyManager;
 import edu.wustl.common.vocab.VocabularyException;
 import edu.wustl.common.vocab.impl.Concept;
 import edu.wustl.common.vocab.impl.Vocabulary;
 import edu.wustl.common.vocab.impl.VocabularyManager;
 import edu.wustl.common.vocab.utility.VocabUtil;
 import edu.wustl.query.util.global.Constants;
 
 
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
 	public List<IVocabulary> getVocabulries() throws VocabularyException
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
 	public List<IConcept> getPermissibleValueList(AttributeInterface attribute,
 			EntityInterface entity) throws PVManagerException
 	{
 		List<IConcept> permissibleConcepts = null;
 		try
 		{
 			List<PermissibleValueInterface> permissibleValues = pvManager.getPermissibleValueList(
 					attribute, entity);
 			IVocabulary sourceVocabulary = new Vocabulary(VocabUtil.getVocabProperties()
 					.getProperty("source.vocab.name"), 
 					VocabUtil.getVocabProperties().getProperty("source.vocab.version"));
 			permissibleConcepts = vocabularyManager.getConceptDetails(
 					getConceptCodeList(permissibleValues), sourceVocabulary);
 		}
 		catch (VocabularyException e)
 		{
 			throw new PVManagerException(
 					"Server encountered a problem in fetching the permissible values for "
 							+ entity.getName(), e);
 		}
 		return permissibleConcepts;
 	}
 	/**
 	 * This method returns the list of permissible values 
 	 * @param attribute
 	 * @param entity
 	 * @return
 	 * @throws PVManagerException
 	 */
 	public List<IConcept> getConfiguredPermissibleValueList(AttributeInterface attribute,
 			EntityInterface entity) throws PVManagerException
 	{
 		List<IConcept> permissibleConcepts = null;
 		try
 		{
 			List<PermissibleValueInterface> permissibleValues = pvManager.getPermissibleValueList(
 					attribute, entity);
 			int configPVcount=Integer.parseInt(VocabUtil.getVocabProperties().getProperty("pvs.to.show"));
 			/*If the permissible values are more than the configured value
 			 * take the subset.
 			 */
 			if(permissibleValues.size() > configPVcount)
 			{
 				permissibleValues = permissibleValues.subList(0, configPVcount);
 			}
 			IVocabulary sourceVocabulary = new Vocabulary(VocabUtil.getVocabProperties()
 					.getProperty("source.vocab.name"), 
 					VocabUtil.getVocabProperties().getProperty("source.vocab.version"));
 			permissibleConcepts = vocabularyManager.getConceptDetails(
 					getConceptCodeList(permissibleValues), sourceVocabulary);
 		}
 		catch (VocabularyException e)
 		{
 			throw new PVManagerException(
 					"Server encountered a problem in fetching the permissible values for "
 							+ entity.getName(), e);
 		}
 		return permissibleConcepts;
 	}
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
 			IVocabulary targetVocabulary, EntityInterface entity)
 			throws VocabularyException, PVManagerException
 	{
 		IVocabulary sourceVocabulary = new Vocabulary(VocabUtil.getVocabProperties().getProperty(
 				"source.vocab.name"), VocabUtil.getVocabProperties().getProperty(
 				"source.vocab.version"));
 		
 		List<IConcept> concepts;
 		Map<String, List<IConcept>> mappedConcepts = null;
 		try
 		{
 			List<PermissibleValueInterface> permissibleValues = pvManager.getPermissibleValueList(
 					attribute, entity);
 			
 			concepts = vocabularyManager.getConceptDetails(getConceptCodeList(permissibleValues),
 					sourceVocabulary);
 			mappedConcepts = vocabularyManager.getMappedConcepts(concepts, targetVocabulary);
 		}
 		catch (VocabularyException e)
 		{
 			Logger.out.error(e.getMessage(),e);
 		}
 
 		return mappedConcepts;
 	}
 	/**
 	 * This method will return the concept code list
 	 * @param pvList
 	 * @return
 	 */
 	private List<String> getConceptCodeList(List<PermissibleValueInterface> pvList)
 	{
 		List<String> conceptCodes = null;
 		if (pvList != null)
 		{
 			conceptCodes = new ArrayList<String>();
 			for (PermissibleValueInterface pv : pvList)
 			{
 				conceptCodes.add(pv.getValueAsObject().toString());
 			}
 		}
 
 		return conceptCodes;
 	}
 	/**
 	 * This method will search the given term in across all the vocabularies
 	 * @param term
 	 * @param vocabName
 	 * @param vocabVersion
 	 * @return
 	 * @throws VocabularyException
 	 */
 	public List<IConcept> searchConcept(String term, String vocabName, String vocabVersion)
 			throws VocabularyException
 	{
 		IVocabulary vocabulary = new Vocabulary(vocabName, vocabVersion);
 		return vocabularyManager.searchConcept(term, vocabulary);
 
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
 	 */
 	public String getMessage(int count)
 	{
 		return "<tr><td class='black_ar_tt' colspan='3'>" + Constants.VI_INFO_MESSAGE1 +count+Constants.VI_INFO_MESSAGE2+ "<td></tr>";
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
 	public String getMappedVocabularyPVChildAsHTML(String vocabName, String vocabversoin,
 			IConcept concept, String checkboxId)
 	{
 		return "<tr title='Concept Code: "+concept.getCode()+"'><td style='padding-left:30px'>&nbsp;</td><td class='black_ar_tt' > \n"
 				+ "<input type='checkbox' name='" + vocabName + vocabversoin + "' id='"
 				+ checkboxId + "' value='" + concept.getCode() + ":" + concept.getDescription()
 				+ "' onclick=\"getCheckedBoxId('" + checkboxId + "');\">"
 				+ "</td><td class='black_ar_tt'>" /*+ concept.getCode() + ":"*/
 				+ concept.getDescription() + "\n" + "<td></tr>";
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
 	public String getSearchedVocabPVChildAsHTML(String vocabName, String vocabversoin,
 			IConcept concept, String checkboxId, String textStatus) throws VocabularyException
 	{
 		/*String relationType =VocabUtil.getVocabProperties().getProperty("vocab.translation.association.name");
 		IVocabulary sourceVocabulary = new Vocabulary(VocabUtil.getVocabProperties().getProperty(
 		"source.vocab.name"), VocabUtil.getVocabProperties().getProperty(
 		"source.vocab.version"));
 		if(isSourceVocabCodedTerm(concept, relationType, sourceVocabulary))
 		{
 			System.out.println(concept);
 		}*/
 		String chkBoxStatus="";
 		String cssClass=textStatus;
 		if(textStatus.indexOf("Disabled")>-1)
 		{
 			chkBoxStatus="disabled";
 		}
 		return "<tr title='Concept Code: "+concept.getCode()+"'><td style='padding-left:30px'>&nbsp;</td><td class='black_ar_tt'> \n"
 				+ "<input type='checkbox' "+chkBoxStatus+" name='"+ vocabName + vocabversoin + "' id='"
 				+ checkboxId + "' value='" + concept.getCode() + ":" + concept.getDescription()
 				+ "' onclick=\"getCheckedBoxId('" + checkboxId + "');\">"
 				+ "</td><td class='"+cssClass+"'>" /*+ concept.getCode() + ":"*/
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
 	public String getRootVocabularyNodeHTML(String vocabName, String vocabVer,
 			String vocabDisName)throws VocabularyException
 	{
 		  String srcvocabName = VocabUtil.getVocabProperties().getProperty("source.vocab.name");
 		  String srcvocabVer = VocabUtil.getVocabProperties().getProperty("source.vocab.version");
 		  
 		  String style="display:none"; 
 		  String imgpath="src=\"images/advancequery/nolines_plus.gif\"/";
 		  if(srcvocabName.equalsIgnoreCase(vocabName) && srcvocabVer.equalsIgnoreCase(vocabVer)) 
 		  { 
 			  //to show MED vocabulary  tree or data expanded mode 
 			  style="display:";
 			  imgpath="src=\"images/advancequery/nolines_minus.gif\"/"; 
 		  }
 		String tableHTML = "<table cellpadding ='0' cellspacing ='1'>";
 		return tableHTML 
 				+ "<tr><td>"
 				+ tableHTML + "<tr><td class='grid_header_text'>"
 				+ "<a id=\"image_"+ vocabName+ vocabVer+ "\"\n"
 				+ "onClick=\"showHide('inner_div_"+ vocabName+ vocabVer+ "'," 
 				+"'image_"+ vocabName+ vocabVer+ "');\">\n"
 				+ "<img "+ imgpath+ " align='absmiddle'/></a>"
 				+ "</td><td><input type='checkbox' name='"+ vocabName+vocabVer
 				+ "' id='root_"	+ vocabName+vocabVer+ "' "
 				+ "value='"	+ vocabName
 				+ "' onclick=\"setStatusOfAllCheckBox(this.id);\"></td>"
 				+ "<td align='middle'  class='grid_header_text'>&nbsp;&nbsp;"
 				+ vocabDisName
 				+ "\n"
 				+ "</td></tr></table>"
 				+ "</td></tr><tr><td><div id='inner_div_"+ vocabName+vocabVer+ "'style='"+style+"'>"
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
 	public String getRootVocabularyHTMLForSearch(String vocabName, String vocabVer,
 			String vocabDisName)
 	{
 		String style="display:none"; 
 		String imgpath="src=\"images/advancequery/nolines_plus.gif\"/";
 		String tableHTML = "<table cellpadding ='0' cellspacing ='1'>";
 		return tableHTML 
 				+ "<tr><td>"
 				+ tableHTML
 				+ "<tr><td class='grid_header_text'><a id=\"image_"+ vocabName+vocabVer+ "\"\n"
 				+ "onClick=\"showHide('inner_div_"+ vocabName+vocabVer+ "',"
 				+ "'image_"+ vocabName+ vocabVer+ "');\">\n"
 				+ "<img "+ imgpath+ "align='absmiddle'></a>"
 				+ "</td><td><input type='checkbox' name='"+ vocabName+ vocabVer
 				+ "' id='root_"	+ vocabName+ vocabVer+ "' "
 				+ "value='"+ vocabName+ "'"
 				+ " onclick=\"setStatusOfAllCheckBox(this.id);\"></td>"
 				+ "<td align='middle'  class='grid_header_text'>&nbsp;&nbsp;"
 				+ vocabDisName
 				+ "\n"
 				+ "</td></tr></table>"
 				+ "</td></tr><tr><td><div id='inner_div_"+ vocabName+vocabVer+"'style='"+style+"'>"
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
 	public String getErrorMessageAsHTML()
 	{
 		return "<table width='100%' height='100%'>"
 				+ "<tr><td class='black_ar_tt' style='color:red'>"
				+ "Error occured while Searching.Please report this problem to the Adminstrator.<td></tr></table>";
 	}
 	/**
 	 * 
 	 * @param targetConcept
 	 * @param relationType
 	 * @param baseVocab
 	 * @return
 	 * @throws VocabularyException
 	 */
 	public  List<IConcept> isSourceVocabCodedTerm(IConcept targetConcept, String relationType, IVocabulary baseVocab) throws VocabularyException
 	{
 		 List<IConcept> concepts= vocabularyManager.getSrcConceptsForTarget(targetConcept,relationType,baseVocab);
 		
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
 }
