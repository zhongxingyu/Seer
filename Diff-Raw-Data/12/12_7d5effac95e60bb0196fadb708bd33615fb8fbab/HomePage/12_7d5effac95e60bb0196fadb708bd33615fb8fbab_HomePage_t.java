 package vlove.web;
 
 import org.apache.wicket.markup.html.basic.Label;
 import org.wicketstuff.annotation.mount.MountPath;
 
 import vlove.VirtException;
 import vlove.model.Capabilities;
 
 @MountPath(path="/home")
 public class HomePage extends BasePage {
 	
 	public HomePage() {
 		super();
 		
 		String capabilities = null;
		if (vm.validateConfig()) {
			try {
				Capabilities c = vm.getCapabilities();
				capabilities = String.format("%s %s - %s - %d cores", c.getVendor(), c.getModel(), c.getCpuArch(), c.getNumProcs());
			} catch (VirtException ve) {
				log.warn("Could not retrieve capabilities.", ve);
			}
 		}
 		add(new Label("capabilities", capabilities));
 	}
 }
