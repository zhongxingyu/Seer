 package org.mapsforge.mapmaker;
 
 import java.io.IOException;
 import java.util.LinkedList;
 
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.mapsforge.mapmaker.gui.MainWizard;
 import org.mapsforge.mapmaker.gui.MapFileWizardPage;
 import org.mapsforge.mapmaker.gui.OptionSelectionWizardPage;
 import org.mapsforge.mapmaker.gui.POIWizardPage;
 import org.mapsforge.mapmaker.gui.ProgressGUI;
 import org.mapsforge.mapmaker.logging.ProgressManager;
 import org.openstreetmap.osmosis.core.TaskRegistrar;
 import org.openstreetmap.osmosis.core.pipeline.common.Pipeline;
 
 public class GUIOsmosisLauncher {
 
 	public static void main(String[] args) {
 		Display display = new Display();
 		Shell shell = new Shell(display);
 		
 		// Launch wizard
         Wizard w = new MainWizard();
         WizardDialog dialog = new WizardDialog(shell, w);
         Window.setDefaultImage(new Image(display, "logo.png"));
 		int dialogExitCode = dialog.open();
 
 		// shell.dispose();
 		// display.dispose();
 
 		switch (dialogExitCode) {
 			case Window.OK:
 				System.out.println("[Main] Launching Osmosis");
 				ProgressGUI gui = ProgressGUI.getInstance();
 				try {
 					System.out.println("[Main] Saving settings");
 					w.getDialogSettings().save("lastSession.settings.xml");
 				} catch (IOException e) {
 					// TODO Error message dialog
 					e.printStackTrace();
 				}
 				printSettings(w.getDialogSettings());
 				invokeOsmosisAsync(w.getDialogSettings());
 				System.out.println("[Main] Opening progress bar window");
 				gui.show(display);
 
 			case Window.CANCEL:
 				// System.out.println("Saving configuration");
 				// TODO offer save configuration if osmosis has finished gracefully
 				break;
 			default:
 		}
 	}
 
 	private static void invokeOsmosisAsync(final IDialogSettings settings) {
 
 		final TaskConfigurationBuilder builder = new TaskConfigurationBuilder();
 		builder.buildTaskConfigurationFromDialogSettings(settings);
 
 		new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				System.out.println("[OsmosisThread] Started");
 				System.out.println("[OsmosisThread] # Tasks: "
 						+ builder.getTaskConfigurations().size());
 				ProgressManager pm = ProgressGUI.getInstance();
 
 				// Launch Osmosis
 				pm.start();
 				TaskRegistrar taskRegistrar = new TaskRegistrar();
 				taskRegistrar.initialize(new LinkedList<String>());
 
 				Pipeline pipeline = new Pipeline(taskRegistrar
 						.getFactoryRegister());
 				System.out.println("[OsmosisThread] Preparing pipeline");
 				try {
 				pipeline.prepare(builder.getTaskConfigurations());
 				} catch (OutOfMemoryError e) {
 					// TODO Proper error report
 					throw e;
 				}
 				System.out.println("[OsmosisThread] Starting pipeline");
 				pipeline.execute();
 
 				System.out
 						.println("[OsmosisThread] Waiting for pipeline to finish");
 				pipeline.waitForCompletion();
 				System.out.println("[OsmosisThread] Done");
 				pm.finish();
 			}
 
 		}).start();
 
 	}
 
 	private static void printSettings(IDialogSettings settings) {
 		System.out.println("== Settings ==");
 		
 		System.out.println("[General settings]");
 		IDialogSettings generalSection = settings.getSection(OptionSelectionWizardPage.getSettingsSectionName());
 		System.out.println("input file path: "
 				+ generalSection.get("inputFilePath"));
 		System.out.println("create vector map: "
 				+ generalSection.getBoolean("createVectorMap"));
 		System.out.println("create POIs: "
 				+ generalSection.getBoolean("createPOIs"));
 
 		System.out.println("[POI settings]");
 		IDialogSettings poiSection = settings.getSection(POIWizardPage.getSettingsSectionName());
 		System.out.println("category config path: "
 				+ poiSection.get("categoryConfigPath"));
 
 		System.out.println("[Map File Settings]");
 		IDialogSettings mapFileSection = settings.getSection(MapFileWizardPage.getSettingsSectionName());
 		System.out.println("Map File path: "
 				+ mapFileSection.getBoolean("mapFilePath"));
 		System.out.println("Enable HDD cache: "
 				+ mapFileSection.getBoolean("enableHDDCache"));
 		System.out.println("Enable custom start position: "
 				+ mapFileSection.getBoolean("enableCustomStartPosition"));
 		if (mapFileSection.getBoolean("enableCustomStartPosition") == true) {
 			System.out.println("  [*] Latitude / Longitude: "
 					+ mapFileSection.getInt("startPositionLat") + " / "
 					+ mapFileSection.getInt("startPositionLat"));
 		}
 		System.out.println("Enable custom start zoom level: "
 				+ mapFileSection.getBoolean("enableCustomStartZoomLevel"));
 		System.out.println("Enable custom tag config: "
 				+ mapFileSection.getBoolean("enableCustomTagConfig"));
 		if (mapFileSection.getBoolean("enableCustomTagConfig")) {
 			System.out.println("  [*] Tag configuration file path: "
 					+ mapFileSection.get("tagConfigurationFilePath"));
 		}
 		System.out.println("Enable polygon clipping: "
 				+ mapFileSection.getBoolean("enablePolygonClipping"));
 		System.out.println("Enable way clipping: "
 				+ mapFileSection.getBoolean("enableWayClipping"));
 		System.out.println("Compute label positions: "
 				+ mapFileSection.getBoolean("computeLabelPositions"));
 		System.out.println("Enable debug file: "
 				+ mapFileSection.getBoolean("enableDebugFile"));
 		
 		System.out.println("Enable custom start zoom level: "
 				+ mapFileSection.getBoolean("enableCustomStartZoomLevel"));
 		if (mapFileSection.getBoolean("enableCustomStartZoomLevel")) {
 			System.out.println("" + mapFileSection.getInt("mapZoomLevel"));
 		}
 		System.out.println("Preferred language: "
 				+ mapFileSection.get("preferredLanguage"));
 		System.out.println("Comment: " + mapFileSection.getBoolean("comment"));
 		System.out
 				.println("Simplification factor: "
 						+ (mapFileSection.getInt("simplificationFactor") * 0.01));
 		System.out.println("Bounding box enlargement"
 				+ mapFileSection.getBoolean("BBEnlargement"));
 		System.out.println("Zoom interval configuration: "
 				+ mapFileSection.get("zoomIntervalConfiguration"));
 		
 		System.out.println("==============");
 
 	}
 }
