 package br.org.mosaic.properties;
 
 /** A property of HTML tag
  * @author andrew */
 public class Property {
 	private String	name;
 	private String	value;
 	private Quotation quotation;
 
 	public Property(String name, String value) {
 		this.name = name;
 		this.value = value;
 	}
 
 	public void setQuotation(Quotation quotation) {
 		this.quotation = quotation;
 	}
 
 	@Override
 	public String toString() {
 		return String.format("%s=%s%s%s", name, quotation.getQ(), t(value), quotation.getQ());
 	}
 
 	private String t(String s) {
 		if (s == null) {
 			return "";
 		}
 		return s.contains( quotation.getQ() ) ? s.replace(quotation.getQ(), quotation.getFixedValueQ() ) : s;
 	}
 
 	public static String toString(Quotation quotation, Property... ps) {
 		StringBuilder s = new StringBuilder();
 		for (Property p : ps) {
 			p.setQuotation(quotation);

 			s.append(' ');
 			s.append(p);
 		}
 		if (ps.length > 0) {
 			s.append(' ');
 		}
 		return s.toString();
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getValue() {
 		return value;
 	}
 	
 	@Override
 	public int hashCode() {
 		return name.hashCode();
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if(obj == null || !(obj instanceof Property)){
 			return false;
 		}
 		return ((Property)obj).name.equals( this.name );
 	}
 }
