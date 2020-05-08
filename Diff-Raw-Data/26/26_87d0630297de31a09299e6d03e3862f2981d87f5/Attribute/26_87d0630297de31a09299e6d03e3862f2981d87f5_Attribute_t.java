 package com.pjf.mat.api;
 
 import java.util.List;
 import java.util.SortedSet;
 
 import com.pjf.mat.api.util.ConfigItem;
 
 /** @model */
 public interface Attribute extends Cloneable {
 	/** @model */
 	public AttributeType getType();
 	
 	/** @model */
 	public String getName();
 	/** @model */
 	public String getValue();
 	/** @model */
 	public int getConfigId();
 	
 	public AttrSysType getSysType();
 	
	public Element getParent();
 	
 	public void setValue(String value) throws Exception;
 	
 	/**
 	 * @return sorted set of enum values (or null if not an enum)
 	 */
 	public SortedSet<EnumValue> getEnumValues();
 	
 	/** @param el 
 	 * @model */
 	public Attribute clone(Element newParent);	
 	/** @model changeable="false" volatile="true" */
 	public int getEncodedData() throws Exception;	// return data as 32 bit int
 
 	/** get list of config items to configure this attribute */
 	public List<ConfigItem> getConfigList() throws Exception;
 
 }
