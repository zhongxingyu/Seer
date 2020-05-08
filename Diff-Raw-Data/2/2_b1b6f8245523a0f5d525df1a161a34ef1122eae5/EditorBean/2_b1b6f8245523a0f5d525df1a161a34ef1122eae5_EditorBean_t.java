 package github.jasonandrewduncan.examples;
 
 import javax.faces.bean.ManagedBean;
 
 @ManagedBean(name = "editor")
 public class EditorBean {
 
	private String value = "This editor is provided by PrimeFaces!! 3.5";
 
 	public String getValue() {
 		return value;
 	}
 
 	public void setValue(String value) {
 		this.value = value;
 	}
 }
