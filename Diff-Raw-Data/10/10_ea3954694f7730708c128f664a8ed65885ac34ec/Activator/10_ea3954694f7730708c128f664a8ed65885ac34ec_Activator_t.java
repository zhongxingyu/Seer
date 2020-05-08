 /*
  * eUptime - an Eclipse widget allowing to indicate its uptime using some fancy colors.
  * Definitively not the most useful thing on earth, but at least I wanted it.
  *
  * ---
  * LICENSE:
  *             DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
  *                     Version 2, December 2004
  *
  *  Copyright (C) 2004 Sam Hocevar <sam@hocevar.net>
  *
  *  Everyone is permitted to copy and distribute verbatim or modified
  *  copies of this license document, and changing it is allowed as long
  *  as the name is changed.
  *
  *             DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
  *    TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION
  *
  *   0. You just DO WHAT THE FUCK YOU WANT TO.
  *
  * -- http://www.wtfpl.net/txt/copying/
  */
 package com.github.aneveux.euptime;
 
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 /**
  * The activator class controls the plug-in life cycle
  * 
  * @author Antoine Neveux
  * @version 1.0
  */
 public class Activator extends AbstractUIPlugin {
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "com.github.aneveux.euptime"; //$NON-NLS-1$
 
 	// The shared instance
 	private static Activator plugin;
 
 	/**
 	 * Returns Eclipse's starting time (as a timestamp)
 	 * 
 	 * @return {@link #startTime}
 	 */
 	public long getStartTime() {
		return Long.parseLong(System.getProperty("eclipse.startTime"));
 	}
 
 	/**
 	 * The constructor
 	 */
 	public Activator() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
 	 * )
 	 */
 	@Override
 	public void start(final BundleContext context) throws Exception {
 		super.start(context);
 		Activator.plugin = this;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
 	 * )
 	 */
 	@Override
 	public void stop(final BundleContext context) throws Exception {
 		Activator.plugin = null;
 		super.stop(context);
 	}
 
 	/**
 	 * Returns the shared instance
 	 * 
 	 * @return the shared instance
 	 */
 	public static Activator getDefault() {
 		return Activator.plugin;
 	}
 
 }
