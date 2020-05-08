 package com.pjf.mat.util.attr;
 
 import java.util.List;
 import java.util.SortedSet;
 
 import org.apache.log4j.Logger;
 
 import com.pjf.mat.api.AttrSysType;
 import com.pjf.mat.api.Attribute;
 import com.pjf.mat.api.AttributeType;
 import com.pjf.mat.api.util.ConfigItem;
 import com.pjf.mat.api.Element;
 import com.pjf.mat.api.EnumValue;
 import com.pjf.mat.api.util.AttrConfigGenerator;
 
 
 public class UserDefAttribute implements Attribute, Cloneable {
 	private final static Logger logger = Logger.getLogger(UserDefAttribute.class);
 	private static final String CONVERTER_PKG = "com.pjf.mat.config.converters";
 	private final Element parent;
 	private final String name;
 	private String value;
 	private final AttrSysType sysType;
 	private final int configId;
 	private String converter;
 	
 	public UserDefAttribute(Element parent, String name, int configId, String converter, AttrSysType sysType, String defaultStr) throws Exception {
 		this.parent = parent;
 		this.name = name;
 		this.configId = configId;
 		this.sysType = sysType;
 		this.converter = converter;
 		this.value = "";
 		if (defaultStr != null) {
 			setValue(defaultStr);
 		}
 	}
 
 	public UserDefAttribute(Element el, String name, int configId, AttrSysType sysType) throws Exception {
 		this.parent = el;
 		this.name = name;
 		this.configId = configId;
 		this.sysType = sysType;
 		this.value = "";
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public String getValue() {
 		return value;
 	}
 
 	@Override
 	public void setValue(String value) throws Exception {
 		this.value = value;
 	}
 	
 	@Override
 	public UserDefAttribute clone(Element newParent) {
 		UserDefAttribute attr = null;
 		try {
 			attr = new UserDefAttribute(newParent,name,configId,converter,sysType,getValue());
 		} catch (Exception e) {
 			logger.error("Unable to set default value [" + getValue() + "] on [" + this + "]");
 		}
 		return attr;
 	}
 
 	@Override
 	public String toString() {
 		StringBuffer buf = new StringBuffer(name);
 		buf.append('='); buf.append(this.getValue());
 		buf.append("[c"); buf.append(this.getConfigId()); buf.append(']');
 		return buf.toString();
 	}
 
 	@Override
 	public int getConfigId() {
 		return configId;
 	}
 
 	@Override
 	public int getEncodedData() throws Exception {
 		throw new Exception("Not supported - use getConfigList()");
 	}
 
 	@Override
 	public AttributeType getType() {
 		return AttributeType.USERDEF;
 	}
 
 	@Override
 	public SortedSet<EnumValue> getEnumValues() {
 		return null;	// simple attributes dont have enum values
 	}
 
 	@Override
 	public AttrSysType getSysType() {
 		return sysType;
 	}
 
 	@Override
 	public List<ConfigItem> getConfigList() throws Exception {
 		String cn = CONVERTER_PKG + "." + converter;
 		AttrConfigGenerator gen = (AttrConfigGenerator) ClassLoader.getSystemClassLoader().loadClass(cn).newInstance();
 		List<ConfigItem> configs = gen.generate(this);
 		return configs;
 	}
 
 	@Override
	public Element getParentt() {
 		return parent;
 	}
 
 }
