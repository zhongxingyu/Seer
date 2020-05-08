 package de.pellepelster.myadmin.demo.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.user.client.ui.DockLayoutPanel.Direction;
 
 import de.pellepelster.myadmin.client.gwt.GWTLayoutFactory;
 import de.pellepelster.myadmin.client.web.MyAdmin;
 import de.pellepelster.myadmin.client.web.modules.navigation.ModuleNavigationModule;
 
 public class Demo implements EntryPoint
 {
 	/** {@inheritDoc} */
 	@Override
 	public void onModuleLoad()
 	{

		MyAdmin.getInstance().setLayoutFactory(new GWTLayoutFactory(Unit.PX));
 
 		MyAdmin.getInstance().startModule(ModuleNavigationModule.MODULE_ID, Direction.WEST.toString());
 
 	}
 }
