 package de.ptb.epics.eve.editor.gef;
 
 import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
 import org.eclipse.gef.palette.ConnectionCreationToolEntry;
 import org.eclipse.gef.palette.MarqueeToolEntry;
 import org.eclipse.gef.palette.PaletteDrawer;
 import org.eclipse.gef.palette.PaletteEntry;
 import org.eclipse.gef.palette.PaletteRoot;
 import org.eclipse.gef.palette.PaletteToolbar;
 import org.eclipse.gef.palette.PanningSelectionToolEntry;
 import org.eclipse.gef.palette.ToolEntry;
 import org.eclipse.gef.requests.CreationFactory;
 import org.eclipse.gef.tools.AbstractTool;
 
 import de.ptb.epics.eve.data.scandescription.ScanModuleTypes;
 import de.ptb.epics.eve.editor.Activator;
 
 /**
  * @author Marcus Michalsky
  * @since 1.6
  */
public class ScanDescriptionEditorPaletteFactory {
 
 	/**
 	 * Returns the palette root
 	 * 
 	 * @return the palette root
 	 */
 	public static PaletteRoot createPalette() {
 		PaletteRoot palette = new PaletteRoot();
 		palette.add(createToolsGroup(palette));
 		palette.add(createElementsDrawer());
 		return palette;
 	}
 
 	/*
 	 * 
 	 */
 	private static PaletteEntry createToolsGroup(PaletteRoot palette) {
 		PaletteToolbar toolbar = new PaletteToolbar("Tools");
 
 		ToolEntry tool = new PanningSelectionToolEntry();
 		toolbar.add(tool);
 		palette.setDefaultEntry(tool);
 
 		tool = new MarqueeToolEntry();
 		tool.setToolProperty(AbstractTool.PROPERTY_UNLOAD_WHEN_FINISHED,
 				Boolean.TRUE);
 		toolbar.add(tool);
 
 		ConnectionCreationToolEntry connection = new ConnectionCreationToolEntry(
 				"Connection", "Create a connection", null,
 				Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
 						"icons/arrow.gif"),
 				Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
 						"icons/arrow_24.gif"));
 		toolbar.add(connection);
 
 		return toolbar;
 	}
 
 	/*
 	 * 
 	 */
 	private static PaletteEntry createElementsDrawer() {
 		PaletteDrawer componentsDrawer = new PaletteDrawer("Create Tools");
 		
 		CombinedTemplateCreationEntry componentCreateSM2 = new CombinedTemplateCreationEntry(
 				"Scan Module", "Create a Scan Module", new CreationFactory() {
 					@Override
 					public Object getObjectType() {
 						return ScanModuleTypes.CLASSIC;
 					}
 
 					@Override
 					public Object getNewObject() {
 						return null;
 					}
 				}, Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
 						"icons/devices/scanmodule.gif"),
 				Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
 						"icons/devices/scanmodule24.gif"));
 		componentsDrawer.add(componentCreateSM2);
 
 		CombinedTemplateCreationEntry componentSaveAxisPosSM = new CombinedTemplateCreationEntry(
 				"Scan Module", "Save Axis Positions", new CreationFactory() {
 					@Override
 					public Object getObjectType() {
 						return ScanModuleTypes.SAVE_AXIS_POSITIONS;
 					}
 
 					@Override
 					public Object getNewObject() {
 						return null;
 					}
 				}, Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
 						"icons/devices/axis.gif"),
 				Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
 						"icons/devices/scanmoduleaxes24.gif"));
 		componentsDrawer.add(componentSaveAxisPosSM);
 
 		CombinedTemplateCreationEntry componentSaveChannelValSM = new CombinedTemplateCreationEntry(
 				"Scan Module", "Save Channel Values", new CreationFactory() {
 					@Override
 					public Object getObjectType() {
 						return ScanModuleTypes.SAVE_CHANNEL_VALUES;
 					}
 
 					@Override
 					public Object getNewObject() {
 						return null;
 					}
 				}, Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
 						"icons/devices/channel.gif"),
 				Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID,
 						"icons/devices/scanmodulechannels24.gif"));
 		componentsDrawer.add(componentSaveChannelValSM);
 		
 		return componentsDrawer;
 	}
 }
