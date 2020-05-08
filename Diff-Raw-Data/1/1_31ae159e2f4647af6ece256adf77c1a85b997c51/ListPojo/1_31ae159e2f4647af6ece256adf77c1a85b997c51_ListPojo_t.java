 package eu.dm2e.ws.grafeo.test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import eu.dm2e.ws.grafeo.annotations.RDFClass;
 import eu.dm2e.ws.grafeo.annotations.RDFId;
 import eu.dm2e.ws.grafeo.annotations.RDFProperty;
 
 @RDFClass("omnom:SomeList")
 public class ListPojo {
 	
 	@RDFId
 	private String idURI;
 	
 	@RDFProperty("omnom:some_number")
 	private List<IntegerPojo> integerResourceList = new ArrayList<IntegerPojo>();
 	
 	public String getIdURI() { return idURI; }
 	public void setIdURI(String idURI) { this.idURI = idURI; }
 	public List<IntegerPojo> getIntegerResourceList() {
 		return integerResourceList;
 	}
 	public void setIntegerResourceList(List<IntegerPojo> integerResourceList) {
 		this.integerResourceList = integerResourceList;
 	}
 
 }
