 package au.csiro.eis.ontology.beans;
 
 import java.io.Serializable;
 
 import com.google.gwt.user.client.rpc.IsSerializable;
 
 public class OwlObjectPropertyAxiomBean extends OwlAxiomBean implements Serializable, IsSerializable {
 
 	String propertyIri;
 	OwlIndividualBean domain;
 	OwlIndividualBean range;
 	public final String type = "ObjectProperty";
 
 	public OwlObjectPropertyAxiomBean() {
 		
 	}
 
 
 	public OwlObjectPropertyAxiomBean(String propertyIri,
 			OwlIndividualBean domain, OwlIndividualBean range) {
 		super();
 		this.propertyIri = propertyIri;
 		this.domain = domain;
 		this.range = range;
 		
 		this.createLabel();
 	}
 
 
 	private void createLabel() {
 		StringBuffer sb = new StringBuffer();
 		
 		sb.append(type+"(");
 		if(this.domain != null) {
 			sb.append(this.domain.getName());
 		}
 		else {
 			sb.append("*");
 		}
 		
 		sb.append(", ");
 		
 		if(this.propertyIri != null) {
 			sb.append(this.propertyIri);
 		}
 		else {
 			sb.append("*");
 		}
 		
 		
 		sb.append(", ");
 
 		if(this.range != null) {
 			sb.append(this.range.getName());
 		}
 		else {
 			sb.append("*");
 		}
 		sb.append(")");
 		
 		
 		this.setLabel(sb.toString());
 		
 	}
 	
 	
 }
