 
 package axirassa.webapp.components;
 
 import org.apache.tapestry5.ComponentResources;
 import org.apache.tapestry5.Field;
 import org.apache.tapestry5.annotations.Component;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.corelib.components.PasswordField;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.services.javascript.JavaScriptSupport;
 
 public class AxPasswordField implements Field {
 	@Inject
 	private ComponentResources componentResources;
 
 	@Inject
 	private JavaScriptSupport jsSupport;
 
 	@Component(publishParameters = "label,annotationProvider,clientId,disabled,nulls,translate,value,id,validate")
 	@Property
 	private PasswordField textfield;
 
 	private String clientId;
 
 
 	public void setupRender() {
		clientId = jsSupport.allocateClientId(componentResources);
 	}
 
 
 	@Override
 	public String getControlName() {
 		return textfield.getControlName();
 	}
 
 
 	@Override
 	public boolean isDisabled() {
 		return textfield.isDisabled();
 	}
 
 
 	@Override
 	public boolean isRequired() {
 		return textfield.isRequired();
 	}
 
 
 	@Override
 	public String getClientId() {
 		return clientId;
 	}
 
 
 	@Override
 	public String getLabel() {
 		return textfield.getLabel();
 	}
 }
