 package gov.nih.nci.security.upt.forms;
 
 import gov.nih.nci.security.UserProvisioningManager;
 import gov.nih.nci.security.authorization.domainobjects.FilterClause;
 import gov.nih.nci.security.dao.FilterClauseSearchCriteria;
 import gov.nih.nci.security.dao.SearchCriteria;
 import gov.nih.nci.security.upt.constants.DisplayConstants;
 import gov.nih.nci.security.upt.util.HibernateHelper;
 import gov.nih.nci.security.upt.util.StringUtils;
 import gov.nih.nci.security.upt.viewobjects.FormElement;
 import gov.nih.nci.security.upt.viewobjects.SearchResult;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessages;
 import org.apache.struts.upload.FormFile;
 import org.apache.struts.validator.ValidatorForm;
 import org.hibernate.SessionFactory;
 
 public class InstanceLevelForm extends ValidatorForm implements BaseDBForm
 {
 
 	private static final long serialVersionUID = 1L;
 
 	private FormFile uploadedFile1;
 	private FormFile uploadedFile2;
 	private String hibernateFileName;
 
 	private String id;
 	private String className;
 	private String filterChain;
 	private String targetClassName;
 	private String targetClassAttributeName;
 	private String targetClassAttributeType;
 	private String targetClassAlias;
 	private String targetClassAttributeAlias;
 	private String generatedSQL;
 	private String generatedSQLForUser;
 	private String generatedSQLForGroup;
 	private String updateDate;
 
 
 	/**
 	 * @return Returns the uploaded file.
 	 */
 	public FormFile getUploadedFile1()
 	{
 		return uploadedFile1;
 	}
 	
 	/**
 	 * @return Returns the uploaded file.
 	 */
 	public FormFile getUploadedFile2()
 	{
 		return uploadedFile2;
 	}
 
 	public String getHibernateFileName()
 	{
 		return hibernateFileName;
 	}
 	
 	public void setHibernateFileName(String hibernateFileName)
 	{
 		this.hibernateFileName = hibernateFileName;
 	}
 
 	public void setUploadedFile1(FormFile uploadedFile1)
 	{
 		this.uploadedFile1 = uploadedFile1;
 	}
 	
 	public void setUploadedFile2(FormFile uploadedFile2)
 	{
 		this.uploadedFile2 = uploadedFile2;
 	}
 	
 	
 	/**
 	 * @return Returns the className.
 	 */
 	public String getClassName()
 	{
 		return className;
 	}
 
 	
 	/**
 	 * @param className The className to set.
 	 */
 	public void setClassName(String className)
 	{
 		this.className = className;
 	}
 
 	
 	/**
 	 * @return Returns the filterChain.
 	 */
 	public String getFilterChain()
 	{
 		return filterChain;
 	}
 
 	
 	/**
 	 * @param filterChain The filterChain to set.
 	 */
 	public void setFilterChain(String filterChain)
 	{
 		this.filterChain = filterChain;
 	}
 
 	
 	/**
 	 * @return Returns the generatedSQL.
 	 */
 	public String getGeneratedSQL()
 	{
 		return generatedSQL;
 	}
 
 	
 	/**
 	 * @param generatedSQL The generatedSQL to set.
 	 */
 	public void setGeneratedSQL(String generatedSQL)
 	{
 		this.generatedSQL = generatedSQL;
 	}
 
 	
 	/**
 	 * @return Returns the id.
 	 */
 	public String getId()
 	{
 		return id;
 	}
 
 	
 	/**
 	 * @param id The id to set.
 	 */
 	public void setId(String id)
 	{
 		this.id = id;
 	}
 
 	
 	/**
 	 * @return Returns the targetClassAlias.
 	 */
 	public String getTargetClassAlias()
 	{
 		return targetClassAlias;
 	}
 
 	
 	/**
 	 * @param targetClassAlias The targetClassAlias to set.
 	 */
 	public void setTargetClassAlias(String targetClassAlias)
 	{
 		this.targetClassAlias = targetClassAlias;
 	}
 
 	
 	/**
 	 * @return Returns the targetClassAttributeAlias.
 	 */
 	public String getTargetClassAttributeAlias()
 	{
 		return targetClassAttributeAlias;
 	}
 
 	
 	/**
 	 * @param targetClassAttributeAlias The targetClassAttributeAlias to set.
 	 */
 	public void setTargetClassAttributeAlias(String targetClassAttributeAlias)
 	{
 		this.targetClassAttributeAlias = targetClassAttributeAlias;
 	}
 
 	
 	/**
 	 * @return Returns the targetClassAttributeName.
 	 */
 	public String getTargetClassAttributeName()
 	{
 		return targetClassAttributeName;
 	}
 
 	
 	/**
 	 * @param targetClassAttributeName The targetClassAttributeName to set.
 	 */
 	public void setTargetClassAttributeName(String targetClassAttributeName)
 	{
 		this.targetClassAttributeName = targetClassAttributeName;
 	}
 
 	
 	/**
 	 * @return Returns the targetClassAttributeType.
 	 */
 	public String getTargetClassAttributeType()
 	{
 		return targetClassAttributeType;
 	}
 
 	
 	/**
 	 * @param targetClassAttributeType The targetClassAttributeType to set.
 	 */
 	public void setTargetClassAttributeType(String targetClassAttributeType)
 	{
 		this.targetClassAttributeType = targetClassAttributeType;
 	}
 
 	
 	/**
 	 * @return Returns the targetClassName.
 	 */
 	public String getTargetClassName()
 	{
 		return targetClassName;
 	}
 
 	
 	/**
 	 * @param targetClassName The targetClassName to set.
 	 */
 	public void setTargetClassName(String targetClassName)
 	{
 		this.targetClassName = targetClassName;
 	}
 
 	
 	/**
 	 * @return Returns the updateDate.
 	 */
 	public String getUpdateDate()
 	{
 		return updateDate;
 	}
 
 	
 	/**
 	 * @param updateDate The updateDate to set.
 	 */
 	public void setUpdateDate(String updateDate)
 	{
 		this.updateDate = updateDate;
 	}
 
 	public String getFormName()
 	{
 		return DisplayConstants.INSTANCE_LEVEL_ID;
 	}
 
 	public void resetForm()
 	{
 		this.id = null;
 		this.uploadedFile1 = null;
 		this.uploadedFile2 = null;
 		this.hibernateFileName = null;
 		this.className = null;
 		this.filterChain = null;
 		this.targetClassName = null;
 		this.targetClassAttributeName = null;
 		this.targetClassAttributeType = null;
 		this.targetClassAlias = null;
 		this.targetClassAttributeAlias = null;
 		this.generatedSQL = null;
 		this.generatedSQLForGroup = null;
 		this.generatedSQLForUser = null;
 		this.updateDate = null;
 	}
 	
 	
 	
 	public void reset(ActionMapping mapping, HttpServletRequest request)
 	{
 		this.uploadedFile1 = null;
 		this.uploadedFile2 = null;
 		this.hibernateFileName = null;
 		this.className = null;
 		this.filterChain = null;
 		this.targetClassName = null;
 		this.targetClassAttributeName = null;
 		this.targetClassAttributeType = null;
 		this.targetClassAlias = null;
 		this.targetClassAttributeAlias = null;
 		this.generatedSQL = null;
 		this.generatedSQLForGroup = null;
 		this.generatedSQLForUser = null;
 	}
 	
 	
 
 	public ArrayList getAddFormElements()
 	{
 		ArrayList formElementList = new ArrayList();
 
 		formElementList.add(new FormElement("Class Name", "className", StringUtils.initString(getClassName()), DisplayConstants.INPUT_COMBOBOX, DisplayConstants.REQUIRED, DisplayConstants.NOT_DISABLED));
 		formElementList.add(new FormElement("Filter Chain", "filterChain", StringUtils.initString(getFilterChain()), DisplayConstants.INPUT_COMBOBOX, DisplayConstants.REQUIRED, DisplayConstants.NOT_DISABLED));
 		formElementList.add(new FormElement("Target Class Attribute Name ", "targetClassAttributeName", StringUtils.initString(getTargetClassAttributeName()), DisplayConstants.INPUT_COMBOBOX, DisplayConstants.REQUIRED, DisplayConstants.NOT_DISABLED));
 		formElementList.add(new FormElement("Target Class Alias", "targetClassAlias", getTargetClassAlias(), DisplayConstants.INPUT_BOX, DisplayConstants.NOT_REQUIRED, DisplayConstants.NOT_DISABLED));
 		formElementList.add(new FormElement("Target Class Attribute Alias", "targetClassAttributeAlias", getTargetClassAttributeAlias(), DisplayConstants.INPUT_BOX, DisplayConstants.NOT_REQUIRED, DisplayConstants.NOT_DISABLED));
 		
 		return formElementList;
 	}
 
 	public ArrayList getDisplayFormElements()
 	{
 		ArrayList formElementList = new ArrayList();
 
 		formElementList.add(new FormElement("Class Name", "className", StringUtils.initString(getClassName()), DisplayConstants.INPUT_BOX, DisplayConstants.NOT_REQUIRED, DisplayConstants.NOT_DISABLED, DisplayConstants.READONLY));
 		formElementList.add(new FormElement("Filter Chain", "filterChain", getFilterChain(), DisplayConstants.INPUT_BOX, DisplayConstants.NOT_REQUIRED, DisplayConstants.NOT_DISABLED, DisplayConstants.READONLY));
 		formElementList.add(new FormElement("Target Class Name", "targetClassName", getTargetClassName(), DisplayConstants.INPUT_BOX, DisplayConstants.NOT_REQUIRED, DisplayConstants.NOT_DISABLED, DisplayConstants.READONLY));
 		formElementList.add(new FormElement("Target Class Attribute Name", "targetClassAttributeName", getTargetClassAttributeName(), DisplayConstants.INPUT_BOX, DisplayConstants.NOT_REQUIRED, DisplayConstants.NOT_DISABLED, DisplayConstants.READONLY));
 		formElementList.add(new FormElement("Target Class Attribute Type", "targetClassAttributeType", getTargetClassAttributeType(), DisplayConstants.INPUT_BOX, DisplayConstants.NOT_REQUIRED, DisplayConstants.NOT_DISABLED, DisplayConstants.READONLY));
 		formElementList.add(new FormElement("Target Class Alias", "targetClassAlias", getTargetClassAlias(), DisplayConstants.INPUT_BOX, DisplayConstants.NOT_REQUIRED, DisplayConstants.NOT_DISABLED, DisplayConstants.READONLY));
 		formElementList.add(new FormElement("Target Class Attribute Alias", "targetClassAttributeAlias", getTargetClassAttributeAlias(), DisplayConstants.INPUT_BOX, DisplayConstants.NOT_REQUIRED, DisplayConstants.NOT_DISABLED, DisplayConstants.READONLY));
 		formElementList.add(new FormElement("Generated SQL For Group", "generatedSQLForGroup", getGeneratedSQLForGroup(), DisplayConstants.INPUT_TEXTAREA, DisplayConstants.REQUIRED, DisplayConstants.NOT_DISABLED));
 		formElementList.add(new FormElement("Generated SQL For User", "generatedSQLForUser", getGeneratedSQLForUser(), DisplayConstants.INPUT_TEXTAREA, DisplayConstants.REQUIRED, DisplayConstants.NOT_DISABLED));
 		formElementList.add(new FormElement("Update Date", "updateDate", StringUtils.initString(getUpdateDate()), DisplayConstants.INPUT_DATE, DisplayConstants.NOT_REQUIRED, DisplayConstants.DISABLED));
 		
 		return formElementList;
 	}
 
 	public ArrayList getSearchFormElements()
 	{
 		ArrayList formElementList = new ArrayList();
 		formElementList.add(new FormElement("Class Name", "className", StringUtils.initString(getClassName()), DisplayConstants.INPUT_BOX, DisplayConstants.NOT_REQUIRED, DisplayConstants.NOT_DISABLED));
 		return formElementList;
 	}
 
 	public void buildDisplayForm(HttpServletRequest request) throws Exception
 	{
 		UserProvisioningManager userProvisioningManager = (UserProvisioningManager)(request.getSession()).getAttribute(DisplayConstants.USER_PROVISIONING_MANAGER);
 		FilterClause filterClause = userProvisioningManager.getFilterClauseById(this.id);
 
 		this.className = filterClause.getClassName();
 		this.filterChain = filterClause.getFilterChain();
 		this.targetClassName = filterClause.getTargetClassName();
 		this.targetClassAttributeName = filterClause.getTargetClassAttributeName();
 		this.targetClassAttributeType = filterClause.getTargetClassAttributeType();
 		this.targetClassAlias = filterClause.getTargetClassAlias();
 		this.targetClassAttributeAlias = filterClause.getTargetClassAttributeAlias();
 		this.generatedSQLForGroup = filterClause.getGeneratedSQLForGroup();
 		this.generatedSQLForUser = filterClause.getGeneratedSQLForUser();
 		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
 		this.updateDate = simpleDateFormat.format(filterClause.getUpdateDate());
 		
 	}
 
 	public void buildDBObject(HttpServletRequest request) throws Exception
 	{
 		UserProvisioningManager userProvisioningManager = (UserProvisioningManager)(request.getSession()).getAttribute(DisplayConstants.USER_PROVISIONING_MANAGER);
 
 		FilterClause filterClause = null;
 		
 		if ((this.id == null) || ((this.id).equalsIgnoreCase("")))
 		{
 			filterClause = new FilterClause();
 		}
 		else
 		{
 			filterClause = userProvisioningManager.getFilterClauseById(this.id);
 		}
 		
 		filterClause.setClassName(this.className);
 		filterClause.setFilterChain(this.filterChain);
 		filterClause.setTargetClassName(this.targetClassName);
 		filterClause.setTargetClassAttributeName(this.targetClassAttributeName);
 		filterClause.setTargetClassAttributeType(this.targetClassAttributeType);
 		filterClause.setTargetClassAlias(this.targetClassAlias);
 		filterClause.setTargetClassAttributeAlias(this.targetClassAttributeAlias);
 
 		if ((this.id == null) || ((this.id).equalsIgnoreCase("")))
 		{
 			filterClause.setGeneratedSQLForGroup(HibernateHelper.getGeneratedSQL(filterClause, (SessionFactory)request.getSession().getAttribute(DisplayConstants.HIBERNATE_SESSIONFACTORY),true));
 			filterClause.setGeneratedSQLForUser(HibernateHelper.getGeneratedSQL(filterClause, (SessionFactory)request.getSession().getAttribute(DisplayConstants.HIBERNATE_SESSIONFACTORY),false));
 			userProvisioningManager.createFilterClause(filterClause);
 			this.id = filterClause.getId().toString();
 			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
 			this.setGeneratedSQLForGroup(filterClause.getGeneratedSQLForGroup());
 			this.setGeneratedSQLForUser(filterClause.getGeneratedSQLForUser());
 			this.updateDate = simpleDateFormat.format(filterClause.getUpdateDate());
 		}
 		else
 		{
 			filterClause.setGeneratedSQLForGroup(this.generatedSQLForGroup);
 			filterClause.setGeneratedSQLForUser(this.generatedSQLForUser);
 			userProvisioningManager.modifyFilterClause(filterClause);
 			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
 			this.updateDate = simpleDateFormat.format(filterClause.getUpdateDate());
 		}
 	}
 
 	public void removeDBObject(HttpServletRequest request) throws Exception
 	{
 		UserProvisioningManager userProvisioningManager = (UserProvisioningManager)(request.getSession()).getAttribute(DisplayConstants.USER_PROVISIONING_MANAGER);
 		userProvisioningManager.removeFilterClause(this.id);
 		this.resetForm();
 	}
 
 	public SearchResult searchObjects(HttpServletRequest request, ActionErrors errors, ActionMessages messages) throws Exception
 	{
 		UserProvisioningManager userProvisioningManager = (UserProvisioningManager)(request.getSession()).getAttribute(DisplayConstants.USER_PROVISIONING_MANAGER);
 		FilterClause filterClause = new FilterClause();
 		if (this.className != null && !(this.className.trim().equalsIgnoreCase("")))
 			filterClause.setClassName(this.className);
 		
 		SearchCriteria searchCriteria = new FilterClauseSearchCriteria(filterClause);
 		List list = userProvisioningManager.getObjects(searchCriteria);
 		SearchResult searchResult = new SearchResult();
 		searchResult.setSearchResultMessage(searchCriteria.getMessage());
 		searchResult.setSearchResultObjects(list);
 		return searchResult;
 	}
 
 	public String getPrimaryId()
 	{
 		if (this.id == null)
 		{
 			return new String("");
 		}
 		else
 		{
 			return id;
 		}
 	}
 
 	public String getGeneratedSQLForGroup() {
 		return generatedSQLForGroup;
 	}
 
 	public void setGeneratedSQLForGroup(String generatedSQLForGroup) {
 		this.generatedSQLForGroup = generatedSQLForGroup;
 	}
 
 	public String getGeneratedSQLForUser() {
 		return generatedSQLForUser;
 	}
 
 	public void setGeneratedSQLForUser(String generatedSQLForUser) {
 		this.generatedSQLForUser = generatedSQLForUser;
 	}
 
 }
