 package au.csiro.eis.ontology.beans;
 
 import java.io.Serializable;
 
 import com.google.gwt.user.client.rpc.IsSerializable;
 
 
 public class OwlLiteralBean extends AbstractRdfResourceBean  implements Serializable, IsSerializable {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	String lang;
 	String literal;
 	String datatypeIri;
 	
 	public OwlLiteralBean() {
 		
 	}
 
 	public OwlLiteralBean(String literal, String datatypeIri, String lang) {
 		this.literal = literal;
 		this.datatypeIri = datatypeIri;
 		this.lang = lang;
 	}
 
 	public String getLang() {
 		return lang;
 	}
 	public void setLang(String lang) {
 		this.lang = lang;
 	}
 	public String getLiteral() {
 		return literal;
 	}
 	public void setLiteral(String literal) {
 		this.literal = literal;
 	}
 	public String getDatatypeIri() {
 		return datatypeIri;
 	}
 	public void setDatatypeIri(String datatypeIri) {
 		this.datatypeIri = datatypeIri;
 	}
 	
 	
 }
