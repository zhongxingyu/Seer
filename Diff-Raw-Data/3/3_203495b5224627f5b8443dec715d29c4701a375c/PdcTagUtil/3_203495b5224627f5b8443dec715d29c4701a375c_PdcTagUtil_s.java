 package com.silverpeas.tags.pdc;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import com.silverpeas.admin.ejb.AdminBm;
 import com.silverpeas.admin.ejb.AdminBmRuntimeException;
 import com.silverpeas.pdc.ejb.PdcBm;
 import com.silverpeas.pdc.ejb.PdcBmRuntimeException;
 import com.silverpeas.tags.organization.MenuItem;
 import com.silverpeas.tags.publication.PublicationTagUtil;
 import com.silverpeas.tags.util.EJBDynaProxy;
 import com.silverpeas.tags.util.SiteTagUtil;
 import com.silverpeas.thesaurus.ejb.ThesaurusBm;
 import com.silverpeas.thesaurus.model.Synonym;
 import com.silverpeas.util.StringUtil;
 import com.stratelia.silverpeas.contentManager.GlobalSilverContent;
 import com.stratelia.silverpeas.contentManager.GlobalSilverContentI18N;
 import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
 import com.stratelia.silverpeas.pdc.model.ClassifyValue;
 import com.stratelia.silverpeas.pdc.model.SearchContext;
 import com.stratelia.silverpeas.pdc.model.SearchCriteria;
 import com.stratelia.silverpeas.pdc.model.Value;
 import com.stratelia.silverpeas.silvertrace.SilverTrace;
 import com.stratelia.silverpeas.treeManager.model.TreeNodeI18N;
 import com.stratelia.webactiv.beans.admin.ComponentInstLight;
 import com.stratelia.webactiv.util.JNDINames;
 import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
 import com.stratelia.webactiv.util.publication.model.PublicationPK;
 
 public class PdcTagUtil implements java.io.Serializable
 {
 	private String	axisId		= null;
 	private String	valueId 	= null;
 	private String	spaceId		= null;
 	private String	componentId = null;
 	private String	userId		= null;
 	private boolean skipSpaceId = false;
 
 	private PdcBm				pdcBm				= null;
 	private AdminBm				adminBm				= null;
 	private ThesaurusBm			thesaurusBm			= null;
 	private PublicationTagUtil	publicationTagUtil	= null;
 
     public PdcTagUtil(String axisId, String valueIdOrPath, int depth, String spaceId)
     {
 		this.axisId		= axisId;
 		this.valueId	= valueIdOrPath;
 		this.spaceId	= spaceId;
     }
 
 	private String getAxisId()
 	{
 		return axisId;
 	}
 
 	private String getValueId()
 	{
 		return valueId;
 	}
 
 	private String getSpaceId()
 	{
 		return spaceId;
 	}
 
 	private String getSiteLanguage()
 	{
 		return SiteTagUtil.getLanguage();
 	}
 
 	private String getUserId()
 	{
 		if (userId == null)
 		{
 			SilverTrace.info("Pdc", "PdcTagUtil.getUserId()", "root.MSG_GEN_PARAM_VALUE", "userId = "+SiteTagUtil.getUserId());
 			return SiteTagUtil.getUserId();
 		}
 		else
 			return userId;
 	}
 
 	public void setUserId(String userId)
 	{
 		this.userId = userId;
 	}
 
 	public Value getValue(String axisIdAndValueId) throws Exception
 	{
 		String			axisId		= "";
 		String			valueId		= "";
 		StringTokenizer tokenizer	= new StringTokenizer(axisIdAndValueId, ",");
 		String			param		= "";
 		int				i			= 1;
 		while (tokenizer.hasMoreTokens()) {
 			param = tokenizer.nextToken();
 			if (i == 1)
 				axisId = param;
 			else if (i == 2)
 				valueId = param;
 			i++;
 		}
 		return getValue(axisId, valueId);
 	}
 
 	public Value getAltValue(String axisIdAndValueIdAndVocaId) throws Exception
 	{
 		String			axisId		= "";
 		String			valueId		= "";
 		String			vocaId		= "";
 		StringTokenizer tokenizer	= new StringTokenizer(axisIdAndValueIdAndVocaId, ",");
 		String			param		= "";
 		int				i			= 1;
 		while (tokenizer.hasMoreTokens()) {
 			param = tokenizer.nextToken();
 			if (i == 1)
 				axisId = param;
 			else if (i == 2)
 				valueId = param;
 			else if (i == 3)
 				vocaId = param;
 			i++;
 		}
 
 		SilverTrace.info("Pdc", "PdcTagUtil.getAltValue()", "root.MSG_GEN_PARAM_VALUE", "axisId = "+axisId+", valueId = "+valueId+", vocaId = "+vocaId);
 
 		Value value = getValue(axisId, valueId);
 
 		SilverTrace.info("Pdc", "PdcTagUtil.getAltValue()", "root.MSG_GEN_PARAM_VALUE", "value = "+value.toString());
 
 		//Replace value's name by synonym's name
 		String synonym = getSynonymOfValue(valueId, vocaId);
 
 		if (synonym != null)
 		{
 			SilverTrace.info("Pdc", "PdcTagUtil.getAltValue()", "root.MSG_GEN_PARAM_VALUE", "synonym = "+synonym.toString());
 			value.setName(synonym);
 		}
 
 		return value;
 	}
 
 	private String getSynonymOfValue(String valueId, String vocaId) throws Exception
 	{
 		List synonyms = getSynonymsOfValue(valueId, vocaId);
 		String synonym = null;
 		if (synonyms != null  && synonyms.size() > 0)
 		{
 			synonym = (String) synonyms.get(0);
 		}
 		return synonym;
 	}
 
 	private Value getValue(String axisId, String valueId) throws Exception
 	{
 		Value value = getPdcBm().getValue(axisId, valueId);
 		return getTranslatedValue(value, null);
 	}
 
 	public List getTreeView() throws Exception
 	{
 		return valuesTree2MenuItemTree((ArrayList) getPdcBm().getDaughters(getAxisId(), "0"));
 	}
 
 	/**
 	 * Retourne l'axe dont les valeurs ayant un synonyme seront remplacées par le synonyme
 	 * @param vocaId - Id du vocabulaire utilisé
 	 * @return une liste de MenuItem
 	 * @throws Exception
 	 */
 	public List getAltTreeView(String vocaId) throws Exception
 	{
 		List treeView = getTreeView();
 		addSynonyms(treeView, vocaId);
 		return treeView;
 	}
 
 	/**
 	 * Remplace le nom des valeurs d'un axe par leurs synonymes. La valeur reste inchangée si elle n'a pas de synonyme.
 	 * @param treeView - L'axe qui va être enrichit par les synonymes
 	 * @param vocaId - Id du vocabulaire utilisé
 	 * @throws Exception
 	 */
 	private void addSynonyms(List treeView, String vocaId) throws Exception
 	{
 		long lvocaId = new Long(vocaId).longValue();
 		long ltreeId = new Long(getPdcBm().getAxisHeader(getAxisId()).getRootId()).longValue();
 
 		List synonyms = getThesaurusBm().getSynonymsByTree(ltreeId, lvocaId);
 
 		MenuItem 	value 	= null;
 		Synonym 	synonym = null;
 		for (int i=0; i<treeView.size(); i++)
 		{
 			value 	= (MenuItem) treeView.get(i);
 			synonym = getSynonym(value.getId(), synonyms);
 			if (synonym != null)
 			{
 				value.setName(synonym.getName());
 			}
 		}
 	}
 
 	private List getSynonymsOfValue(String valueId, String vocaId) throws Exception
 	{
 		SilverTrace.info("Pdc", "PdcTagUtil.getSynonymsOfValue()", "root.MSG_GEN_PARAM_VALUE", "valueId = "+valueId+", vocaId = "+vocaId);
 		long lvocaId 	= new Long(vocaId).longValue();
 		long lvalueId 	= new Long(valueId).longValue();
 		long ltreeId 	= new Long(getPdcBm().getAxisHeader(getAxisId()).getRootId()).longValue();
 
 		List synonyms = getThesaurusBm().getSynonyms(ltreeId, lvalueId, lvocaId);
 
 		return synonyms;
 	}
 
 	/**
 	 * @param value - La valeur a recherché dans la liste des synonymes
 	 * @param synonyms - Une liste de Synonym
 	 * @return Le synonyme associé à la valeur. null si aucun synonyme n'est associé à la valeur.
 	 */
 	private Synonym getSynonym(String valueId, List synonyms)
 	{
 		Synonym synonym = null;
 		for (int s=0; s<synonyms.size(); s++)
 		{
 			synonym = (Synonym) synonyms.get(s);
 			if (synonym.getIdTerm() == new Long(valueId).longValue())
 				return synonym;
 		}
 		return null;
 	}
 
 	public List getSubTreeView() throws Exception
 	{
 		return valuesTree2MenuItemTree((ArrayList) getPdcBm().getSubAxisValues(getAxisId(), getValueId()));
 	}
 
 	public List getAltSubTreeView(String vocaId) throws Exception
 	{
 		List subTreeView = getSubTreeView();
 		addSynonyms(subTreeView, vocaId);
 		return subTreeView;
 	}
 
 	/**
 	 * Cette méthode est très utile pour gérer les menus déroulants.
 	 * @param targetValueId - l'id de la valeur sélectionnée
 	 * @return une liste contenant les filles de la valeur sélectionnée ainsi que tous les ascendants de la valeur et leurs soeurs.
 	 * @throws Exception
 	 */
 	public List getSubTreeViewContextual(String targetValueId) throws Exception
 	{
 		SilverTrace.info("Pdc", "PdcTagUtil.getSubTreeViewContextual()", "root.MSG_GEN_PARAM_VALUE", "targetValueId = "+targetValueId);
 		List values = (ArrayList) getPdcBm().getSubAxisValues(getAxisId(), getValueId());
 		List result = new ArrayList();
 		if (values != null)
 		{
 			SilverTrace.info("Pdc", "PdcTagUtil.getSubTreeViewContextual()", "root.MSG_GEN_PARAM_VALUE", "values.size = "+values.size());
 			Value 	targetValue 	= getPdcBm().getValue(getAxisId(), targetValueId);
 			String 	targetValuePath = targetValue.getPath();
 			SilverTrace.info("Pdc", "PdcTagUtil.getSubTreeViewContextual()", "root.MSG_GEN_PARAM_VALUE", "targetValuePath ="+targetValuePath);
 			Iterator 	iValues = values.iterator();
 			Value 		value 	= null;
 			while (iValues.hasNext())
 			{
 				value = (Value) iValues.next();
 				value = getTranslatedValue(value, null);
 
 				if (targetValuePath.indexOf(value.getPath()) != -1)
 				{
 					result.add(value);
 				}
 				else if (value.getFatherId().equals(targetValueId))
 				{
 					result.add(value);
 				}
 			}
 		}
 		return valuesTree2MenuItemTree(result);
 	}
 
 
 	public List getAltSubTreeViewContextual(String targetValueIdAndVocaId) throws Exception
 	{
 		int				i				= 1;
 		String			targetValueId	= "";
 		String			vocaId 			= "";
 		StringTokenizer tokenizer		= new StringTokenizer(targetValueIdAndVocaId, ",");
 		String			param			= "";
 		while (tokenizer.hasMoreTokens()) {
 			param = tokenizer.nextToken();
 			if (i == 1)
 				targetValueId = param;
 			else if (i == 2)
 				vocaId = param;
 			i++;
 		}
 
 		List treeView = getSubTreeViewContextual(targetValueId);
 		addSynonyms(treeView, vocaId);
 		return treeView;
 	}
 
 	public List getPath(String targetValueId) throws Exception
 	{
 		return getPath(targetValueId, null);
 	}
 
 	/**
 	 * Get Path (use Language or Synonym)
 	 * @param targetValueId
 	 * @param vocaId
 	 * @return List
 	 * @throws Exception
 	 */
 	private List getPath(String targetValueId, String vocaId) throws Exception
 	{
 		List values = new ArrayList();
 		Value targetValue = getValue(getAxisId(), targetValueId);
 
 		String path = targetValue.getPath();
 
 		int indexOfRootId = path.indexOf(getValueId());
 		if (indexOfRootId != -1)
 		{
 			path = path.substring(indexOfRootId);
 			StringTokenizer tokenizer 	= new StringTokenizer(path, "/");
 			String			valueId		= "";
 			Value			value		= null;
 			while (tokenizer.hasMoreTokens()) {
 				valueId = tokenizer.nextToken();
 				if (valueId.length()>0)
 				{
 					value = getValue(getAxisId(), valueId);
 
 					//Use synonym if needed
 					if (vocaId != null)
 					{
 						String synonym = getSynonymOfValue(valueId, vocaId);
 						if (synonym != null)
 							value.setName(synonym);
 					}
 
 					if (value != null)
 						values.add(value);
 				}
 			}
 		}
 		return values;
 	}
 
 	public List getAltPath(String targetValueIdAndVocaId) throws Exception
 	{
 		int				i				= 1;
 		String			targetValueId	= "";
 		String			vocaId 			= "";
 		StringTokenizer tokenizer		= new StringTokenizer(targetValueIdAndVocaId, ",");
 		String			param			= "";
 		while (tokenizer.hasMoreTokens()) {
 			param = tokenizer.nextToken();
 			if (i == 1)
 				targetValueId = param;
 			else if (i == 2)
 				vocaId = param;
 			i++;
 		}
 
 		return getPath(targetValueId, vocaId);
 	}
 
 	public List getSilverContents() throws Exception
 	{
 		SilverTrace.info("Pdc", "PdcTagUtil.getSilverContents()", "root.MSG_GEN_PARAM_VALUE", "No Sorting !");
 		List componentIds = getAvailableComponentIds();
 		List globalSilverContents = getSilverContents(getValueId(), componentIds);
 		SilverTrace.info("Pdc", "PdcTagUtil.getSilverContents()", "root.MSG_GEN_PARAM_VALUE", "globalSilverContents.size() = " + globalSilverContents.size());
 
 		try
 		{
 			addSpaceId2GlobalSilverContents(globalSilverContents);
 		}
 		catch (Exception e)
 		{
 			SilverTrace.error("Pdc", "PdcTagUtil.getSilverContents()", "root.MSG_GEN_PARAM_VALUE", e);
 		}
 
 
 		return globalSilverContents;
 	}
 
 	public List getOrderedSilverContents(String columnAndSort) throws Exception
 	{
 		SilverTrace.info("Pdc", "PdcTagUtil.getOrderedSilverContents()", "root.MSG_GEN_PARAM_VALUE", "columnAndSort = " + columnAndSort);
 
 		List silverContents = getSilverContents();
 		addSpaceId2GlobalSilverContents(silverContents);
 
 		Comparator comparator = getComparator(columnAndSort);
 
 		SilverTrace.info("Pdc", "PdcTagUtil.getOrderedSilverContents()", "root.MSG_GEN_PARAM_VALUE", "comparator is null ? " + (comparator == null));
 
 		Collections.sort(silverContents, comparator);
 
 		SilverTrace.info("Pdc", "PdcTagUtil.getOrderedSilverContents()", "root.MSG_GEN_PARAM_VALUE", "silverContents sorted. Size = " + silverContents.size());
 
 		return silverContents;
 	}
 
 	public List getSilverContentsRecursive() throws Exception
 	{
 		List componentIds			= getAvailableComponentIds();
 		List globalSilverContents	= getSilverContents(getValueId(), componentIds, true);
 		addSpaceId2GlobalSilverContents(globalSilverContents);
 
 		return globalSilverContents;
 	}
 
 	public List getOrderedSilverContentsRecursive(String columnAndSort) throws Exception
 	{
 		List silverContents = getSilverContentsRecursive();
 		addSpaceId2GlobalSilverContents(silverContents);
 
 		Comparator comparator = getComparator(columnAndSort);
 		Collections.sort(silverContents, comparator);
 
 		return silverContents;
 	}
 
 	public List getSilverContentsRecursiveGroupByValues(String axisIdAndColumnAndSort) throws Exception
 	{
 		String axisId			= axisIdAndColumnAndSort.substring(0, axisIdAndColumnAndSort.indexOf(","));
 		String columnAndSort	= axisIdAndColumnAndSort.substring(axisIdAndColumnAndSort.indexOf(",")+1, axisIdAndColumnAndSort.length());
 		return getSilverContentsGroupByValues(axisId, columnAndSort, true);
 	}
 
 	public List getSilverContentsGroupByValues(String axisIdAndColumnAndSort) throws Exception
 	{
 		String axisId			= axisIdAndColumnAndSort.substring(0, axisIdAndColumnAndSort.indexOf(","));
 		String columnAndSort	= axisIdAndColumnAndSort.substring(axisIdAndColumnAndSort.indexOf(",")+1, axisIdAndColumnAndSort.length());
 		return getSilverContentsGroupByValues(axisId, columnAndSort, false);
 	}
 
 	private List getSilverContentsGroupByValues(String axisId, String columnAndSort, boolean recursiveMode) throws Exception
 	{
 		SilverTrace.info("Pdc", "PdcTagUtil.getSilverContentsGroupByValues()", "root.MSG_GEN_PARAM_VALUE", "axisId = " + axisId);
 
 		//first, we get silverContents classified on current axis
 		//This list contains SilverContents
 		List silverContentsToReturn = null;
 		if (recursiveMode)
 			silverContentsToReturn = getOrderedSilverContentsRecursive(columnAndSort);
 		else
 			silverContentsToReturn = getOrderedSilverContents(columnAndSort);
 
 		SilverTrace.info("Pdc", "PdcTagUtil.getSilverContentsGroupByValues()", "root.MSG_GEN_PARAM_VALUE", "silverContentsToReturn.size() = " + silverContentsToReturn.size());
 
 		//secondly, we get all silverContents on the other axis identified by axisId
 		//This list contains MenuItem
 		List allSilverContents = getFullTreeView(axisId, "0", columnAndSort);
 
 		SilverTrace.info("Pdc", "PdcTagUtil.getSilverContentsGroupByValues()", "root.MSG_GEN_PARAM_VALUE", "allSilverContents.size() = " + allSilverContents.size());
 
 		//Then, in allSilverContents we keep only items which are in silverContentsToReturn
 		ArrayList	silverContentsGroupByValues = new ArrayList();
 		MenuItem	item						= null;
 		int			indexInList					= -1;
 		for (int i=0; i<allSilverContents.size(); i++)
 		{
 			item = (MenuItem) allSilverContents.get(i);
 			if (item.getType() == MenuItem.TYPE_PDC_VALUE)
 			{
 				//It's a value. We let this here.
 				//ATTENTION = Si une valeur ne contient pas d'item, il faut la supprimer !
 				silverContentsGroupByValues.add(item);
 
 				SilverTrace.info("Pdc", "PdcTagUtil.getSilverContentsGroupByValues()", "root.MSG_GEN_PARAM_VALUE", "value added = " + item.getName());
 			}
 			else
 			{
 				indexInList = isInList(item.getId(), silverContentsToReturn);
 				if (indexInList == -1)
 				{
 					//The silverContent is not in the initial list.
 					SilverTrace.info("Pdc", "PdcTagUtil.getSilverContentsGroupByValues()", "root.MSG_GEN_PARAM_VALUE", "silvercontent is not in the initial list : " + item.getName());
 				}
 				else
 				{
 					//The silverContent is in the initial list.
 					silverContentsGroupByValues.add(item);
 
 					SilverTrace.info("Pdc", "PdcTagUtil.getSilverContentsGroupByValues()", "root.MSG_GEN_PARAM_VALUE", "silvercontent is in the initial list : " + item.getName());
 
 					//We remove it from the initial list.
 					silverContentsToReturn.remove(indexInList);
 				}
 			}
 		}
 
 		SilverTrace.info("Pdc", "PdcTagUtil.getSilverContentsGroupByValues()", "root.MSG_GEN_PARAM_VALUE", "start to remove empty values");
 
 		//We have to remove values which doesn't contains items.
 		ArrayList	finalList			= new ArrayList();
 		MenuItem	nextItem			= null;
 		for (int s=0; s<silverContentsGroupByValues.size(); s++)
 		{
 			item = (MenuItem) silverContentsGroupByValues.get(s);
 
 			if (s<silverContentsGroupByValues.size()-1)
 				nextItem = (MenuItem) silverContentsGroupByValues.get(s+1);
 			else
 				nextItem = null;
 
 			if (item.getType() == MenuItem.TYPE_PDC_VALUE)
 			{
 				if (nextItem == null || nextItem.getType() == MenuItem.TYPE_PDC_VALUE)
 				{
 					//We don't put current item in the final list
 				}
 				else
 				{
 					finalList.add(item);
 				}
 			}
 			else
 			{
 				finalList.add(item);
 			}
 		}
 
 		//We have to process silverContents which are still in the initial list.
 		if (silverContentsToReturn.size()>0)
 		{
 			SilverTrace.info("Pdc", "PdcTagUtil.getSilverContentsGroupByValues()", "root.MSG_GEN_PARAM_VALUE", "start to add items which are not in second axis");
 			MenuItem variousItem = new MenuItem("Autres", MenuItem.TYPE_PDC_VALUE);
 			finalList.add(variousItem);
 			finalList.addAll(silverContents2MenuItems(silverContentsToReturn));
 		}
 
 		SilverTrace.info("Pdc", "PdcTagUtil.getSilverContentsGroupByValues()", "root.MSG_GEN_PARAM_VALUE", "result returned !");
 
 		return finalList;
 	}
 
 	/**
 	 * list is a list of SilverContent
 	 *
 	 */
 	private int isInList(String valueId, List silverContents)
 	{
 		GlobalSilverContent item = null;
 		for (int i=0; i<silverContents.size(); i++)
 		{
 			item = (GlobalSilverContent) silverContents.get(i);
 			if (item.getId().equals(valueId))
 			{
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	public Collection getPositions(String objectIdAndComponentId) throws Exception
 	{
 		int				i			= 1;
 		String			objectId	= "";
 		String			componentId = "";
 		StringTokenizer tokenizer	= new StringTokenizer(objectIdAndComponentId, ",");
 		String			param		= "";
 		while (tokenizer.hasMoreTokens()) {
 			param = tokenizer.nextToken();
 			if (i == 1)
 				objectId = param;
 			else if (i == 2)
 				componentId = param;
 			i++;
 		}
 
 		//get the unique id (silverContentId) of the object identified by objectId
 		int silverContentId = getPdcBm().getSilverContentId(objectId, componentId);
 
		ArrayList positions = getPdcBm().getPositions(silverContentId, componentId);
		return positions;
 	}
 
 	public Collection getValuesOnAxis(String objectIdAndComponentIdAndAxisId) throws Exception
 	{
 		String axisIdStr = objectIdAndComponentIdAndAxisId.substring(objectIdAndComponentIdAndAxisId.lastIndexOf(",")+1, objectIdAndComponentIdAndAxisId.length());
 
 		SilverTrace.info("Pdc", "PdcTagUtil.getValuesOnAxis()", "root.MSG_GEN_PARAM_VALUE", "axisIdStr = " + axisIdStr);
 
 		int axisId = new Integer(axisIdStr).intValue();
 
 		ArrayList resultValues = new ArrayList();
 
 		ArrayList positions = (ArrayList) getPositions(objectIdAndComponentIdAndAxisId);
 
 		SilverTrace.info("Pdc", "PdcTagUtil.getValuesOnAxis()", "root.MSG_GEN_PARAM_VALUE", "positions.size() = " + positions.size());
 
 		ClassifyPosition	position		= null;
 		Value				value			= null;
 		ArrayList			classifyValues	= null;
 		ArrayList			values			= null;
 		for (int i=0; i<positions.size(); i++)
 		{
 			//check each position
 			position = (ClassifyPosition) positions.get(i);
 
 			//collection of ClassifyValue
 			classifyValues = (ArrayList) position.getValues();
 
 			SilverTrace.info("Pdc", "PdcTagUtil.getValuesOnAxis()", "root.MSG_GEN_PARAM_VALUE", "classifyValues.size() = " + classifyValues.size());
 
 			ClassifyValue	classifyValue	= null;
 			int				size			= -1;
 			for (int j=0; j<classifyValues.size(); j++)
 			{
 				SilverTrace.info("Pdc", "PdcTagUtil.getValuesOnAxis()", "root.MSG_GEN_PARAM_VALUE", "j = " + j);
 				classifyValue = (ClassifyValue) classifyValues.get(j);
 				SilverTrace.info("Pdc", "PdcTagUtil.getValuesOnAxis()", "root.MSG_GEN_PARAM_VALUE", "classifyValue = " + classifyValue.toString());
 				if (classifyValue.getAxisId() == axisId) {
 					values	= (ArrayList) classifyValue.getFullPath();
 					size	= values.size();
 					SilverTrace.info("Pdc", "PdcTagUtil.getValuesOnAxis()", "root.MSG_GEN_PARAM_VALUE", "size = " + size);
 					value	= (Value) values.get(size-1);
 					SilverTrace.info("Pdc", "PdcTagUtil.getValuesOnAxis()", "root.MSG_GEN_PARAM_VALUE", "value = " + value.toString());
 
 					if (value != null) {
 						SilverTrace.info("Pdc", "PdcTagUtil.getValuesOnAxis()", "root.MSG_GEN_PARAM_VALUE", "value added = " + value.toString());
 						value.setAxisId(classifyValue.getAxisId());
 						resultValues.add(value);
 					}
 				}
 			}
 		}
 
 		return resultValues;
 	}
 
 	public String getClassifiedOnAxisValue(String objectIdAndComponentIdAndAxisIdAndValueId) throws Exception
 	{
 		String result = "false";
 
 		String valueId = objectIdAndComponentIdAndAxisIdAndValueId.substring(objectIdAndComponentIdAndAxisIdAndValueId.lastIndexOf(",")+1, objectIdAndComponentIdAndAxisIdAndValueId.length());
 
 		SilverTrace.info("Pdc", "PdcTagUtil.isClassifiedOnAxisValue()", "root.MSG_GEN_PARAM_VALUE", "valueId = " + valueId);
 
 		ArrayList values = (ArrayList) getValuesOnAxis(objectIdAndComponentIdAndAxisIdAndValueId.substring(0, objectIdAndComponentIdAndAxisIdAndValueId.lastIndexOf(",")));
 
 		Value	value	= null;
 		for (int i=0; i<values.size(); i++)
 		{
 			value = (Value) values.get(i);
 			if (value.getValuePK().getId().equals(valueId)) {
 				return "true";
 			}
 		}
 
 		return result;
 	}
 
 	private List getSilverContents(String axisId, String valueId, List componentIds) throws Exception
 	{
 		SearchContext context = getSearchContext(axisId, valueId);
 		return findSilverContents(context, componentIds, false);
 	}
 
 	private List getSilverContents(String valueId, List componentIds) throws Exception
 	{
 		SearchContext context = getSearchContext(getAxisId(), valueId);
 		return findSilverContents(context, componentIds, false);
 	}
 
 	private List getSilverContents(String valueId, List componentIds, boolean recursive) throws Exception
 	{
 		SearchContext context = getSearchContext(getAxisId(), valueId);
 		return findSilverContents(context, componentIds, recursive);
 	}
 
 	private List findSilverContents(SearchContext context, List componentIds, boolean recursiveSearch) throws Exception
 	{
 		SilverTrace.info("Pdc", "PdcTagUtil.findSilverContents()", "root.MSG_GEN_ENTER_METHOD");
 		boolean		visibilitySensitive = true;
 		if (SiteTagUtil.isDevMode() || SiteTagUtil.isRecetteMode())
 		{
 			//Le site n'est pas en mode production. Une recherche est faite sur le PDC sans tenir compte de la colonne isVisible.
 			visibilitySensitive = false;
 		} else {
 			//Le site est en mode production. Une recherche est faite sur le PDC en tenant compte de la colonne isVisible.
 			//C'est le comportement standard de Silverpeas.
 		}
 
 		List silverContents				= new ArrayList();
 		List silverContentsToFiltered 	= getPdcBm().findGlobalSilverContents(context, componentIds, recursiveSearch, visibilitySensitive);
 
 		SilverTrace.info("Pdc", "PdcTagUtil.findSilverContents()", "root.MSG_GEN_PARAM_VALUE", "silverContentsToFiltered = "+silverContentsToFiltered.size());
 
 		if (SiteTagUtil.isDevMode())
 		{
 			return silverContentsToFiltered;
 		}
 		else
 		{
 			//Les silverContents doivent être filtrés.
 		  	GlobalSilverContent	gsc	= null;
 			for (int i=0; i<silverContentsToFiltered.size(); i++)
 			{
 				gsc	= (GlobalSilverContent) silverContentsToFiltered.get(i);
 				gsc = getTranslatedGlobalSilveContent(gsc, null);
 				if (getPublicationTagUtil().isPublicationVisible(new PublicationPK(gsc.getId(), "useless", gsc.getInstanceId())))
 				{
 					silverContents.add(gsc);
 				}
 			}
 			return silverContents;
   		}
 	}
 
 	/**
 	 * build the list of component where the search will be processed
 	 * @return a list of component ids
 	 * @throws Exception
 	 */
 	private List getAvailableComponentIds() throws Exception
 	{
 		ArrayList instanceIds = new ArrayList();
 		if (getComponentId() != null)
 		{
 			instanceIds.add(getComponentId());
 		}
 		else
 		{
 			List allComponents = (List) new ArrayList();
 			try
 			{
 				allComponents.addAll(getAdminBm().getAvailCompoIds(getSpaceId(), getUserId()));
 			}
 			catch (Exception e)
 			{
 				SilverTrace.error("Pdc", "PdcTagUtil.getAvailableComponentIds()", "", e);
 			}
 
 			//19/05/2005 - Only components Theme Tracker, quickInfo and almanach are used actually
 			//There is no point in doing a search through all components
 			String instanceId = null;
 			for (int i=0; i<allComponents.size(); i++)
 			{
 				instanceId = (String) allComponents.get(i);
 				if (instanceId != null && (instanceId.startsWith("kmelia") || instanceId.startsWith("quickInfo") || instanceId.startsWith("almanach") ))
 				{
 					instanceIds.add(instanceId);
 					SilverTrace.info("Pdc", "PdcTagUtil.getAvailableComponentIds()", "root.MSG_GEN_PARAM_VALUE", "instanceId = " + instanceId);
 				}
 			}
 		}
 		return instanceIds;
 	}
 
 	private SearchContext getSearchContext(String axisId, String valueId) throws Exception
 	{
 		//build the search context
 		SearchContext	context		= new SearchContext();
 
 		if (valueId != null && !valueId.startsWith("/"))
 		{
 			SilverTrace.info("Pdc", "PdcTagUtil.getSearchContext()", "root.MSG_GEN_PARAM_VALUE", "axisId = "+axisId+", valueId = "+valueId+" isn't the path !");
 
 			//The valueId is not like /0/1/ but instead 1
 			//We have to retrieve path of the value
 			Value value = getPdcBm().getValue(axisId, valueId);
 			valueId = value.getFullPath();
 
 			SilverTrace.info("Pdc", "PdcTagUtil.getSearchContext()", "root.MSG_GEN_PARAM_VALUE", "new valueId = "+valueId);
 		}
 
 		SearchCriteria	criteria	= new SearchCriteria(new Integer(axisId).intValue(), valueId);
 		context.addCriteria(criteria);
 
 		return context;
 	}
 
 	public ArrayList getFullTreeView() throws Exception
 	{
 		return getFullTreeView(getAxisId(), getValueId(), "Name,Asc");
 	}
 
 	public List getOrderedFullTreeView(String columnAndSort) throws Exception
 	{
 		return getFullTreeView(getAxisId(), getValueId(), columnAndSort);
 	}
 
 	/*
 	 * Get a tree containing values and silverContents according to each value. Values that not contains any silverContents are not returned.
 	 * @return a sorted list of MenuItem
 	 */
 	private ArrayList getFullTreeView(String axisId, String valueId, String columnAndSort) throws Exception
 	{
 		SilverTrace.info("Pdc", "PdcTagUtil.getFullTreeView()", "root.MSG_GEN_PARAM_VALUE", "axisId = "+axisId+", valueId = "+valueId+", columnAndSort = "+columnAndSort);
 
 		//the treeview which will be returned (list of MenuItem)
 		ArrayList fullTreeView = new ArrayList();
 
 		//get all values of the axis (list of Value)
 		ArrayList treeView = (ArrayList) getPdcBm().getSubAxisValues(axisId, valueId);
 
 		//reverse treeView (from leaf to root)
 		ArrayList reversedTreeView = reverseList(treeView);
 
 		//get all component ids according to guest's rights
 		List componentIds = getAvailableComponentIds();
 
 		Comparator comparator = getComparator(columnAndSort);
 
 		List	silverContents	= null;
 		Value	value			= null;
 		Value	lastValueAdded  = null;
 
 		for (int i=0; i<reversedTreeView.size(); i++)
 		{
 			value = (Value) reversedTreeView.get(i);
 
 			SilverTrace.info("Pdc", "PdcTagUtil.getFullTreeView()", "root.MSG_GEN_PARAM_VALUE", "value = " + value.toString());
 
 			//for each value, check if there are some silverContents
 			silverContents = getSilverContents(axisId, value.getFullPath(), componentIds);
 
 			SilverTrace.info("Pdc", "PdcTagUtil.getFullTreeView()", "root.MSG_GEN_PARAM_VALUE", "silverContents.size() = " + silverContents.size());
 
 			if (silverContents.size() > 0)
 			{
 				if (columnAndSort != null)
 				{
 					//we have to sort silverContents
 					SilverTrace.info("Pdc", "PdcTagUtil.getFullTreeView()", "root.MSG_GEN_PARAM_VALUE", "ordering silvercontents on value " + value.getValuePK().getId());
 					Collections.sort(silverContents, comparator);
 				}
 
 				fullTreeView = addSilverContentsToTreeView(silverContents, fullTreeView, value);
 
 				//add current value to final treeView
 				fullTreeView.add(value2MenuItem(value));
 
 				lastValueAdded = value;
 			}
 			else
 			{
 				//no item are classified on this value
 				//if this value is the mother of the last added value, we have to add this value too
 				//if this value is not the mother (it's a leaf), we don't add this value
 				if (lastValueAdded != null && value.getValuePK().getId().equals(lastValueAdded.getMotherId()))
 				{
 					fullTreeView.add(value2MenuItem(value));
 					lastValueAdded = value;
 				}
 			}
 
 			SilverTrace.info("Pdc", "PdcTagUtil.getFullTreeView()", "root.MSG_GEN_PARAM_VALUE", "fullTreeView.size() = " + fullTreeView.size());
 		}
 
 		//reverse new treeview
 		return reverseMenuItemList(fullTreeView);
 	}
 
 	private ArrayList addSilverContentsToTreeView(List silverContents, ArrayList tree, Value value) throws Exception
 	{
 		GlobalSilverContent silverContent	= null;
 		MenuItem			item			= null;
 
 		//cast each GlobalSilverContent into MenuItem
 		for (int j=0; j<silverContents.size(); j++)
 		{
 			silverContent = (GlobalSilverContent) silverContents.get(j);
 			item = silverContent2MenuItem(silverContent, value);
 
 			//add each silverContent to final treeview
 			tree.add(item);
 
 			SilverTrace.info("Pdc", "PdcTagUtil.addSilverContentsToTreeView()", "root.MSG_GEN_PARAM_VALUE", "new item added = " + item.getName());
 		}
 		return tree;
 	}
 
 	private ArrayList reverseList(ArrayList listToReverse)
 	{
 		ArrayList	reversedList	= new ArrayList();
 		Value		value			= null;
 		for (int i=0; i<listToReverse.size(); i++)
 		{
 			value = (Value) listToReverse.get(i);
 			reversedList.add(0, value);
 		}
 		return reversedList;
 	}
 
 	private ArrayList reverseMenuItemList(ArrayList listToReverse)
 	{
 		ArrayList	reversedList	= new ArrayList();
 		MenuItem	item			= null;
 		int			indexToPlace	= 0;
 		for (int i=0; i<listToReverse.size(); i++)
 		{
 			item = (MenuItem) listToReverse.get(i);
 			if (item.getType() == MenuItem.TYPE_PDC_VALUE)
 			{
 				reversedList.add(0, item);
 				indexToPlace = 0;
 			}
 			else
 			{
 				reversedList.add(indexToPlace, item);
 				indexToPlace++;
 			}
 		}
 		return reversedList;
 	}
 
 	/**
 	 * Convert valuesTree2MenuItemTree (with translated datas)
 	 * @param values
 	 * @return
 	 */
 	private ArrayList valuesTree2MenuItemTree(List	values)
 	{
 		ArrayList	treeView	= new ArrayList();
 		Value		value		= null;
 
 		for (int i=0; i<values.size(); i++)
 		{
 			value = (Value) values.get(i);
 			value = getTranslatedValue(value, null);
 			treeView.add(value2MenuItem(value));
 		}
 		return treeView;
 	}
 
 	private MenuItem value2MenuItem(Value value)
 	{
 		MenuItem item = new MenuItem(value.getName(), value.getDescription(), value.getLevelNumber(), MenuItem.TYPE_PDC_VALUE, value.getPK().getId(), value.getFullPath());
 		return item;
 	}
 
 	private List silverContents2MenuItems(List silverContents) throws Exception
 	{
 		GlobalSilverContent gsc			= null;
 		ArrayList			menuItems	= new ArrayList();
 		for (int i=0; i<silverContents.size(); i++)
 		{
 			gsc = (GlobalSilverContent) silverContents.get(i);
 			menuItems.add(silverContent2MenuItem(gsc));
 		}
 		return menuItems;
 	}
 
 	private MenuItem silverContent2MenuItem(GlobalSilverContent gsc) throws Exception
 	{
 		MenuItem item = new MenuItem(gsc.getName(), gsc.getDescription(), 0, MenuItem.TYPE_COMPONENT_CONTENT, gsc.getId(), "unknown");
 		item.setComponentId(gsc.getInstanceId());
 
 		//must be modified
 		return addSpaceId2MenuItem(item);
 	}
 
 	private MenuItem silverContent2MenuItem(GlobalSilverContent gsc, Value value) throws Exception
 	{
 		MenuItem item = new MenuItem(gsc.getName(), gsc.getDescription(), value.getLevelNumber()+1, MenuItem.TYPE_COMPONENT_CONTENT, gsc.getId(), value.getFullPath());
 		item.setComponentId(gsc.getInstanceId());
 
 		//must be modified
 		return addSpaceId2MenuItem(item);
 	}
 
 	private MenuItem addSpaceId2MenuItem(MenuItem item) throws Exception
 	{
 		if (!isSpaceIdSkipped())
 			item.setSpaceId(getSpaceId(item.getComponentId()));
 		return item;
 	}
 
 	private List addSpaceId2GlobalSilverContents(List globalSilverContents) throws Exception
 	{
 		GlobalSilverContent gsc	= null;
 		for (int i=0; i<globalSilverContents.size(); i++)
 		{
 			gsc = (GlobalSilverContent) globalSilverContents.get(i);
 			addSpaceId2GlobalSilverContent(gsc);
 		}
 		return globalSilverContents;
 	}
 
 	private GlobalSilverContent addSpaceId2GlobalSilverContent(GlobalSilverContent gsc) throws Exception
 	{
 		if (!isSpaceIdSkipped())
 			gsc.setSpaceId(getSpaceId(gsc.getInstanceId()));
 		return gsc;
 	}
 
 	private String getSpaceId(String instanceId) throws Exception
 	{
 		SilverTrace.info("Pdc", "PdcTagUtil.getSpaceId()", "root.MSG_GEN_PARAM_VALUE", "instanceId a = " + instanceId);
 		ComponentInstLight compoInst = getAdminBm().getComponentInstLight(instanceId);
 		SilverTrace.info("Pdc", "PdcTagUtil.getSpaceId()", "root.MSG_GEN_PARAM_VALUE", "spaceId = " + compoInst.getDomainFatherId());
 		return compoInst.getDomainFatherId();
 	}
 
 	private Comparator getComparator(String columnAndSort)
 	{
 		SilverTrace.info("Pdc", "PdcTagUtil.getComparator()", "root.MSG_GEN_PARAM_VALUE", "columnAndSort = " + columnAndSort);
 		if (columnAndSort != null)
 		{
 			int				i			= 1;
 			String			column		= "";
 			String			sort		= "";
 			StringTokenizer tokenizer	= new StringTokenizer(columnAndSort, ",");
 			String			param		= "";
 			while (tokenizer.hasMoreTokens()) {
 				param = tokenizer.nextToken();
 				if (i == 1)
 					column = param;
 				else if (i == 2)
 					sort = param;
 				i++;
 			}
 			SilverTrace.info("Pdc", "PdcTagUtil.getComparator()", "root.MSG_GEN_PARAM_VALUE", "column = " + column);
 			SilverTrace.info("Pdc", "PdcTagUtil.getComparator()", "root.MSG_GEN_PARAM_VALUE", "sort = " + sort);
 
 			if (column != null && column.equalsIgnoreCase("Name"))
 			{
 				if (sort != null && sort.equalsIgnoreCase("Desc"))
 				{
 					//return GSCNameComparatorDesc.comparator;
 					return nameComparatorDesc;
 				}
 				else
 				{
 					//return GSCNameComparatorAsc.comparator;
 					return nameComparatorAsc;
 				}
 			}
 			else if (column != null && (column.equalsIgnoreCase("Date")|| column.equalsIgnoreCase("UpdateDate")))
 			{
 				if (sort != null && sort.equalsIgnoreCase("Desc"))
 				{
 					//return GSCDateComparatorDesc.comparator;
 					return dateComparatorDesc;
 				}
 				else
 				{
 					//return GSCDateComparatorAsc.comparator;
 					return dateComparatorAsc;
 				}
 			}
 			else if (column != null && column.equalsIgnoreCase("CreationDate"))
 			{
 				if (sort != null && sort.equalsIgnoreCase("Desc"))
 				{
 					//return GSCCreationDateComparatorDesc.comparator;
 					return creationDateComparatorDesc;
 				}
 				else
 				{
 					//return GSCCreationDateComparatorAsc.comparator;
 					return creationDateComparatorAsc;
 				}
 			}
 			else
 			{
 				//return GSCNameComparatorAsc.comparator;
 				return nameComparatorAsc;
 			}
 		} else {
 			//return GSCNameComparatorAsc.comparator;
 			return nameComparatorAsc;
 		}
 	}
 
 	private PdcBm getPdcBm()
 	{
 		if (pdcBm == null)
 		{
 			try
 			{
 				pdcBm = (PdcBm)EJBDynaProxy.createProxy(JNDINames.PDCBM_EJBHOME, PdcBm.class);
 			}
 			catch (Exception e)
 			{
 				throw new PdcBmRuntimeException("PdcTagUtil.getPdcBm", SilverpeasRuntimeException.ERROR,"root.EX_CANT_GET_REMOTE_OBJECT",e);
 			}
 		}
 		return pdcBm;
 	}
 
 	private AdminBm getAdminBm()
 	{
 		if (adminBm == null) {
 			try
 			{
 				adminBm = (AdminBm)EJBDynaProxy.createProxy(JNDINames.ADMINBM_EJBHOME, AdminBm.class);
 			}
 			catch (Exception e)
 			{
 				throw new AdminBmRuntimeException("PdcTagUtil.getAdminBm", SilverpeasRuntimeException.ERROR,"root.EX_CANT_GET_REMOTE_OBJECT",e);
 			}
 		}
 		return adminBm;
     }
 
 	private ThesaurusBm getThesaurusBm()
 	{
 		if (thesaurusBm == null) {
 			try
 			{
 				thesaurusBm = (ThesaurusBm)EJBDynaProxy.createProxy(JNDINames.THESAURUSBM_EJBHOME, ThesaurusBm.class);
 			}
 			catch (Exception e)
 			{
 				throw new AdminBmRuntimeException("PdcTagUtil.getThesaurusBm", SilverpeasRuntimeException.ERROR,"root.EX_CANT_GET_REMOTE_OBJECT",e);
 			}
 		}
 		return thesaurusBm;
     }
 
 	private PublicationTagUtil getPublicationTagUtil()
 	{
 		if (publicationTagUtil==null)
 		{
 			publicationTagUtil = new PublicationTagUtil();
 		}
 		return publicationTagUtil;
 	}
 
 	public String getComponentId() {
 		return componentId;
 	}
 
 	public void setComponentId(String componentId) {
 		if (componentId != null && componentId.length()==0)
 			this.componentId = null;
 		else
 			this.componentId = componentId;
 	}
 
 	public boolean isSpaceIdSkipped() {
 		return skipSpaceId;
 	}
 
 	public void setSkipSpaceId(boolean skipSpaceId) {
 		this.skipSpaceId = skipSpaceId;
 	}
 
 	/**
 	 * Get translated GlobalSilverContent in current site lang or lang as parameter
 	 * @param gsc
 	 * @param language
 	 * @return GlobalSilverContent
 	 */
 	public GlobalSilverContent getTranslatedGlobalSilveContent(GlobalSilverContent gsc, String language)
 	{
 		String lang = null;
 		if (StringUtil.isDefined(language))
 		{
 			lang = language;
 		}
 		else if (StringUtil.isDefined(getSiteLanguage()))
 		{
 			lang = getSiteLanguage();
 		}
 		if (StringUtil.isDefined(lang))
 		{
 			GlobalSilverContentI18N gsci18n = (GlobalSilverContentI18N) gsc.getTranslation(getSiteLanguage());
 			if (gsci18n != null)
 				gsc.setTitle(gsci18n.getName());
 		}
 		return gsc;
 	}
 
 	/**
 	 * Get translated Value in current site lang or lang as parameter
 	 * @param gsc
 	 * @param language
 	 * @return Value
 	 */
 	public Value getTranslatedValue(Value value, String language)
 	{
 		String lang = null;
 		if (StringUtil.isDefined(language))
 		{
 			lang = language;
 		}
 		else if (StringUtil.isDefined(getSiteLanguage()))
 		{
 			lang = getSiteLanguage();
 		}
 
 		if (StringUtil.isDefined(lang))
 		{
 			TreeNodeI18N treeNodei18n = (TreeNodeI18N) value.getTranslation(getSiteLanguage());
 			if (treeNodei18n != null)
 			{
 				value.setName(treeNodei18n.getName());
 				value.setDescription(treeNodei18n.getDescription());
 			}
 		}
 		return value;
 	}
 
 	Comparator nameComparatorAsc = new Comparator()
 	{
 		public int compare(Object o1, Object o2)
 		{
 			GlobalSilverContent gsc1 = (GlobalSilverContent) o1;
 			GlobalSilverContent gsc2 = (GlobalSilverContent) o2;
 
 			return gsc1.getName().compareToIgnoreCase(gsc2.getName());
 		}
 	};
 
 	Comparator nameComparatorDesc = new Comparator()
 	{
 		public int compare(Object o1, Object o2)
 		{
 			GlobalSilverContent gsc1 = (GlobalSilverContent) o1;
 			GlobalSilverContent gsc2 = (GlobalSilverContent) o2;
 
 			return 0-(gsc1.getName().compareToIgnoreCase(gsc2.getName()));
 		}
 	};
 
 	Comparator dateComparatorAsc = new Comparator()
 	{
 		public int compare(Object o1, Object o2)
 		{
 			GlobalSilverContent gsc1 = (GlobalSilverContent) o1;
 			GlobalSilverContent gsc2 = (GlobalSilverContent) o2;
 
 			int compareResult = gsc1.getDate().compareTo(gsc2.getDate());
 			if (compareResult == 0) {
 				//both objects have been created on the same date
 				compareResult = gsc1.getId().compareTo(gsc2.getId());
 			}
 
 			return compareResult;
 		}
 	};
 
 	Comparator dateComparatorDesc = new Comparator()
 	{
 		public int compare(Object o1, Object o2)
 		{
 			GlobalSilverContent gsc1 = (GlobalSilverContent) o1;
 			GlobalSilverContent gsc2 = (GlobalSilverContent) o2;
 
 			int compareResult = gsc1.getDate().compareTo(gsc2.getDate());
 			if (compareResult == 0) {
 				//both objects have been created on the same date
 				compareResult = gsc1.getId().compareTo(gsc2.getId());
 			}
 
 			return 0-compareResult;
 		}
 	};
 
 	Comparator creationDateComparatorAsc = new Comparator()
 	{
 		public int compare(Object o1, Object o2)
 		{
 			GlobalSilverContent gsc1 = (GlobalSilverContent) o1;
 			GlobalSilverContent gsc2 = (GlobalSilverContent) o2;
 
 			int compareResult = gsc1.getCreationDate().compareTo(gsc2.getCreationDate());
 			if (compareResult == 0) {
 				//both objects have been created on the same date
 				compareResult = gsc1.getId().compareTo(gsc2.getId());
 			}
 
 			return compareResult;
 		}
 	};
 
 	Comparator creationDateComparatorDesc = new Comparator()
 	{
 		public int compare(Object o1, Object o2)
 		{
 			GlobalSilverContent gsc1 = (GlobalSilverContent) o1;
 			GlobalSilverContent gsc2 = (GlobalSilverContent) o2;
 
 			int compareResult = gsc1.getCreationDate().compareTo(gsc2.getCreationDate());
 			if (compareResult == 0) {
 				//both objects have been created on the same date
 				compareResult = gsc1.getId().compareTo(gsc2.getId());
 			}
 
 			return 0-compareResult;
 		}
 	};
 }
