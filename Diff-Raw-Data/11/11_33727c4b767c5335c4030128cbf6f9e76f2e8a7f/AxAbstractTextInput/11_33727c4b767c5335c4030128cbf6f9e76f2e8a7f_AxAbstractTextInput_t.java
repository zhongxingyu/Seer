 
 package axirassa.webapp.components;
 
 import org.apache.tapestry5.ComponentResources;
 import org.apache.tapestry5.Field;
 import org.apache.tapestry5.annotations.Parameter;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.ioc.annotations.Inject;
 
 abstract public class AxAbstractTextInput implements Field {
 
 	@Inject
 	private ComponentResources resources;
 
	@Parameter(required = true, defaultPrefix = "literal")
 	private String name;
 

	public String getName() {
		return name;
	}


 	@Property
 	private String text;
 
 	@Parameter
 	private String label;
 
 
 	public AxAbstractTextInput() {
 		super();
 	}
 
 
 	public void setLabel(String label) {
 		this.label = label;
 	}
 
 
 	@Override
 	public String getLabel() {
 		if (label == null)
 			return resources.getId();
 		else
 			return label;
 	}
 }
