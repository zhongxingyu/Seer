 package waazdoh.common.model;
 
 import waazdoh.common.IParameters;
 import waazdoh.cutils.xml.JBean;
 
 public final class WParameters implements IParameters {
 	JBean b = new JBean("parameters");
 
 	@Override
 	public JBean getBean() {
 		return b;
 	}
 
 	@Override
 	public void parseBean(JBean nb) {
 		b.addChildren(nb);
 	}
 
	public float getFloat(final String string) {
		return Float.parseFloat(getValue(string));
	}

 	@Override
 	public String getValue(final String string) {
 		return b.getValue(string);
 	}
 
 	@Override
 	public void setValue(final String string, String value) {
 		b.addValue(string, value);
 	}
 
 	@Override
 	public void setValue(final String name, float value) {
 		b.addValue(name, value);
 	}
 
 	@Override
 	public String getBase64Value(final String string) {
 		return b.getBase64Value(string);
 	}
 
 	@Override
 	public void setBase64Value(final String name, String value) {
 		b.setBase64Value(name, value);
 	}
 }
