 package de.ptb.epics.eve.editor;
 
 import java.io.File;
 
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 import de.ptb.epics.eve.data.measuringstation.MeasuringStation;
 import de.ptb.epics.eve.data.measuringstation.processors.MeasuringStationLoader;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class Activator extends AbstractUIPlugin {
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "de.ptb.epics.eve.editor";
 
 	// The shared instance
 	private static Activator plugin;
 	
 	private MeasuringStation measuringStation;
 	
 	/**
 	 * The constructor
 	 */
 	public Activator() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
 	 */
 	public void start( final BundleContext context ) throws Exception {
 		super.start( context );
 		plugin = this;
 		
 		final String measuringStationDescrition = this.getPreferenceStore().getString( PreferenceConstants.P_DEFAULT_MEASURING_STATION_DESCRIPTION );
		final File measuringStationDescriptionFile = new File( measuringStationDescrition );
		final MeasuringStationLoader measuringStationLoader = new MeasuringStationLoader();
		measuringStationLoader.load( measuringStationDescriptionFile );
		this.measuringStation = measuringStationLoader.getMeasuringStation();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop( final BundleContext context ) throws Exception {
 		plugin = null;
 		super.stop( context );
 	}
 
 	/**
 	 * Returns the shared instance
 	 *
 	 * @return the shared instance
 	 */
 	public static Activator getDefault() {
 		return plugin;
 	}
 	
 	public MeasuringStation getMeasuringStation() {
 		return this.measuringStation;
 	}
 
 }
