 package @project.package@.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.user.client.ui.DockLayoutPanel.Direction;
 
 import de.pellepelster.myadmin.client.gwt.GWTLayoutFactory;
 import de.pellepelster.myadmin.client.web.MyAdmin;
 import de.pellepelster.myadmin.client.web.modules.navigation.ModuleNavigationModule;
 
 public class @project.name.camelcase@ implements EntryPoint
 {
 	/** {@inheritDoc} */
 	@Override
 	public void onModuleLoad()
 	{
 		GWTLayoutFactory gwtLayoutFactory = new GWTLayoutFactory(Unit.PX);
 		MyAdmin.getInstance().setLayoutFactory(gwtLayoutFactory);
 
 		MyAdmin.getInstance().startModule(ModuleNavigationModule.MODULE_ID, Direction.WEST.toString());
 
 	}
 }
