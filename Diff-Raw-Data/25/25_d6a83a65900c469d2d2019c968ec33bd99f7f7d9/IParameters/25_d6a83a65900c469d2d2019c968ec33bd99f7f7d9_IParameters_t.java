 package waazdoh.common;
 
 import waazdoh.cutils.xml.JBean;
 
 public interface IParameters {
 
 	JBean getBean();
 
 	void parseBean(JBean jBean);
 
 	String getValue(final String string);
 
 	void setValue(final String string, String type);
 
 	void setValue(final String string, float value);
 
 	void setBase64Value(final String string, String sscript);
 
 	String getBase64Value(final String string);

	float getFloat(String string);
 }
