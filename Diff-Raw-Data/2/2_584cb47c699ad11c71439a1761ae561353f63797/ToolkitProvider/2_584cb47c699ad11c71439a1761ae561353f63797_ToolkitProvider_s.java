 package org.mule.tooling.jubula.extensions.client.provider;
 
 import java.net.URL;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 import org.eclipse.jubula.toolkit.common.IToolKitProvider;
 import org.eclipse.jubula.toolkit.common.exception.ToolkitPluginException;
 import org.eclipse.jubula.toolkit.common.utils.ToolkitUtils;
 import org.eclipse.swt.widgets.Composite;
 import org.mule.tooling.jubula.extensions.Activator;
 
 public class ToolkitProvider implements IToolKitProvider {
 
 	
	public static final String BUNDLE = "org.mule.tooling.jubula.extensions.client.i18n";
 	
 	@Override
 	public Composite getAutConfigDialog(Composite arg0, int arg1,
 			Map<String, String> arg2, String arg3)
 			throws ToolkitPluginException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public URL getComponentConfigurationFileURL() {
 		return ToolkitUtils.getURL(Activator.getDefault(), "resources/xml/ComponentConfiguration.xml");
 	}
 
 	@Override
 	public ResourceBundle getI18nResourceBundle() {
 		return ResourceBundle.getBundle(BUNDLE);
 	}
 
 }
