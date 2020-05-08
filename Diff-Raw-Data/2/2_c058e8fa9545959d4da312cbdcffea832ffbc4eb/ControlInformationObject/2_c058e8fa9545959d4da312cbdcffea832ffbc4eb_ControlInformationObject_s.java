 /**
  *<p>Title: </p>
  *<p>Description:  </p>
  *<p>Copyright:TODO</p>
  *@author Vishvesh Mulay
  *@version 1.0
  */
 
 package edu.common.dynamicextensions.ui.webui.util;
 
 /**
  * @author vishvesh_mulay
  *
  */
 
 public class ControlInformationObject
 {
 
 	private String controlName;
 
 	private String controlType;
 
 	private String identifier;
 
 	/**
	 * Retuurn the control name
 	 * @return : Control Name
 	 */
 	public String getControlName()
 	{
 		return controlName;
 	}
 
 	/**
 	 * @param controlName : Name of the control
 	 */
 	public void setControlName(String controlName)
 	{
 		this.controlName = controlName;
 	}
 
 	/**
 	 * @return : type of control
 	 */
 	public String getControlType()
 	{
 		return controlType;
 	}
 
 	/**
 	 * 
 	 * @param controlType Control type
 	 */
 	public void setControlType(String controlType)
 	{
 		this.controlType = controlType;
 	}
 
 	/**
 	 * 
 	 * @return :  control identifier
 	 */
 	public String getIdentifier()
 	{
 		return identifier;
 	}
 
 	/**
 	 * 
 	 * @param identifier : identifier for the control
 	 */
 	public void setIdentifier(String identifier)
 	{
 		this.identifier = identifier;
 	}
 
 	/**
 	 * 
 	 * @param name : Name of the control
 	 * @param type : Type of control
 	 * @param identifier : Unique Identifier for the control
 	 */
 	public ControlInformationObject(String name, String type, String identifier)
 	{
 		super();
 		controlName = name;
 		controlType = type;
 		this.identifier = identifier;
 	}
 
 }
