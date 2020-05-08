 /**
  * <p>Title: Constants Class>
  * <p>Description:  This class stores the constants used in the operations in the application.</p>
  * Copyright:    Copyright (c) year
  * Company: Washington University, School of Medicine, St. Louis.
  * @author Gautam Shetty
  * @version 1.00
  * Created on Mar 16, 2005
  */
 
 package edu.common.dynamicextensions.util.global;
 
 
 /**
  * This class stores the constants used in the operations in the application.
  * @author Sujay Narkar
  * @author Rahul Ner
  */
 
 public class Constants extends edu.wustl.common.util.global.Constants
 {
 
 	public static final int ENTITY_FORM_ID = 301;
 	public static final int ATTRIBUTE_FORM_ID = 302;
 	public static final int ENTITY_INFORMATION_FORM_ID = 303;
 	public static final int ENTITY_DATA_FORM_ID = 304;
 	public static final int eNTITY_DATA_INFORMATION_FORM_IDI = 305;
 	public static final int ENTITY_SELECTION_FORM_ID = 306;
 
 	public static final String FORM_DEFINITION_FORM = "formDefinitionForm";
 	public static final String CONTROLS_FORM = "controlsForm";
 	public static final String CACHE_MAP = "cacheMap";
 	public static final String ADD_CONTROLS_TO_FORM = "addControlsToForm";
 	public static final String SHOW_BUILD_FORM_JSP = "showBuildFormJSP";
 	public static final String EDIT_SUB_FORM_PAGE = "editSubForm";
 	public static final String LOAD_FORM_PREVIEW_ACTION = "loadFormPreviewAction";
 	public static final String SHOW_FORM_PREVIEW_JSP = "showFormPreviewJSP";
 	public static final String BUILD_FORM = "buildForm";
 	public static final String SAVE_FORM = "saveForm";
 	public static final String SHOW_CREATE_FORM_JSP = "showCreateFormView";
 	public static final String CONTROL_SELECTED_ACTION = "controlSelectedAction";
 	public static final String SYSTEM_EXCEPTION = "systemException";
 	public static final String APPLICATION_EXCEPTION = "applicationException";
 	public static final String CONTAINER_INTERFACE = "containerInterface";
 	public static final String CONTAINER_STACK = "containerStack";
 	public static final String VALUE_MAP_STACK = "valueMapStack";
 
 	public static final String ENTITYGROUP_INTERFACE = "entityGroupInterface";
 	public static final String CURRENT_CONTAINER_NAME = "currentContainerName";
 	public static final String ERRORS_LIST = "errorsList";
 	public static final String SHOW_DYNAMIC_EXTENSIONS_HOMEPAGE = "showDynamicExtensionsHomePage";
 	public static final String SHOW_EDIT_RECORDS_PAGE = "showRecordListPage";
 	public static final String SUCCESS = "success";
 	public static final String ADD_SUB_FORM = "addSubForm";
 
 	public static final String CONTROL_OPERATION = "controlOperation";
 	public static final String SELECTED_CONTROL_ID = "selectedControlId";
 	public static final String USER_SELECTED_TOOL = "userSelectedTool";
 	public static final String ADD_NEW_FORM = "AddNewForm";
 	public static final String EDIT_FORM = "EditForm";
 	public static final String ADD_SUB_FORM_OPR = "AddSubForm";
 	public static final String EDIT_SUB_FORM_OPR = "EditSubForm";
 	public static final String INSERT_DATA = "insertData";
 	public static final String CALLBACK_URL = "callbackURL";
     public static final String JANUARY = "Jan";
     public static final String FEBRUARY= "Feb";
     public static final String MARCH= "Mar";
     public static final String APRIL= "Apr";
     public static final String MAY= "May";
     public static final String JUNE= "Jun";
     public static final String JULY= "Jul";
     public static final String AUGUST= "Aug";
     public static final String SEPTEMBER= "Sep";
     public static final String OCTOBER= "Oct";
     public static final String NOVEMBER= "Nov";
     public static final String DECEMBER= "Dec";
 
 	//Constant for groupName prefix
 
 	public static final String GROUP_PREFIX = "Group_";
 
 	public static final int DATA_TABLE_STATE_CREATED = 1;
 	public static final int DATA_TABLE_STATE_NOT_CREATED = 2;
 	public static final int DATA_TABLE_STATE_ALREADY_PRESENT = 3;
 	
 	public static final String DATE_ONLY_FORMAT = Constants.DATE_PATTERN_MM_DD_YYYY;
 
 	public static final String ID = "id";
 	public static final String IDENTIFIER = "identifier";
 
 	public enum Cardinality {
		ZERO(0), ONE(1), MANY(2);
 
 		Integer value;
 
 		Cardinality(Integer value)
 		{
 			this.value = value;
 		}
 
 		public Integer getValue()
 		{
 			return value;
 		}
 
 		public static Cardinality get(Integer value)
 		{
 			Cardinality[] cardinalities = Cardinality.values();
 
 			for (Cardinality cardinality : cardinalities)
 			{
 				if (cardinality.getValue().equals(value))
 				{
 					return cardinality;
 				}
 			}
 			return null;
 		}
 	};
 
 	public enum AssociationDirection {
 		SRC_DESTINATION("SRC_DESTINATION"), BI_DIRECTIONAL("BI_DIRECTIONAL");
 
 		String value;
 
 		AssociationDirection(String value)
 		{
 			this.value = value;
 		}
 
 		public String getValue()
 		{
 			return value;
 		}
 
 		public static AssociationDirection get(String value)
 		{
 			AssociationDirection[] associations = AssociationDirection.values();
 
 			for (AssociationDirection association : associations)
 			{
 				if (association.getValue().equalsIgnoreCase(value))
 				{
 					return association;
 				}
 			}
 			return null;
 		}
 	};
 
 	public enum AssociationType {
 		ASSOCIATION("ASSOCIATION"), CONTAINTMENT("CONTAINTMENT");
 
 		String value;
 
 		AssociationType(String value)
 		{
 			this.value = value;
 		}
 
 		public String getValue()
 		{
 			return value;
 		}
 
 		public static AssociationType get(String value)
 		{
 			AssociationType[] associationTypes = AssociationType.values();
 
 			for (AssociationType associationType : associationTypes)
 			{
 				if (associationType.getValue().equalsIgnoreCase(value))
 				{
 					return associationType;
 				}
 			}
 			return null;
 		}
 	}
 
 	/**
 	 * enum to define strategies of the inheritance 
 	 */
 	public enum InheritanceStrategy {
 		TABLE_PER_CONCRETE_CLASS(1), TABLE_PER_HEIRARCHY(2), TABLE_PER_SUB_CLASS(3);
 
 		int value;
 
 		InheritanceStrategy(int value)
 		{
 			this.value = value;
 		}
 
 		public int getValue()
 		{
 			return value;
 		}
 
 		public static InheritanceStrategy get(int value)
 		{
 			InheritanceStrategy[] inheritanceStrategies = InheritanceStrategy.values();
 
 			for (InheritanceStrategy inheritanceStrategy : inheritanceStrategies)
 			{
 				if (inheritanceStrategy.getValue() == value)
 				{
 					return inheritanceStrategy;
 				}
 			}
 			return null;
 		}
 	}
 	
 	
 	public enum ValueDomainType {
 		ENUMERATED("ENUMERATED"), NON_ENUMERATED("NON_ENUMERATED");
 
 		String value;
 
 		ValueDomainType(String value)
 		{
 			this.value = value;
 		}
 
 		public String getValue()
 		{
 			return value;
 		}
 
 		public static ValueDomainType get(String value)
 		{
 			ValueDomainType[] valueDomainTypes = ValueDomainType.values();
 
 			for (ValueDomainType valueDomainType : valueDomainTypes )
 			{
 				if (valueDomainType.getValue().equalsIgnoreCase(value))
 				{
 					return valueDomainType;
 				}
 			}
 			return null;
 		}
 	}
 
 }
