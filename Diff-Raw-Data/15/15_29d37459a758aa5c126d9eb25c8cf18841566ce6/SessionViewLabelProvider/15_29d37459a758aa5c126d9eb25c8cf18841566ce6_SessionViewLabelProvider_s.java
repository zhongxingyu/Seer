 /*
  * Project: com.hudren.woodpile
  * File:    SessionViewLabelProvider.java
  *
  * Author:  Jeff Hudren
  * Created: May 7, 2006
  *
  * Copyright (c) 2006-2013 Hudren Andromeda Connection. All rights reserved. 
  * 
  * The use and distribution terms for this software are covered by the
  * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
  * which can be found in the file epl-v10.html at the root of this distribution.
  * 
  * By using this software in any fashion, you are agreeing to be bound by
  * the terms of this license.
  * 
  * You must not remove this notice, or any other, from this software.
  */
 
 package com.hudren.woodpile.views;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Level;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.PreferenceConverter;
 import org.eclipse.jface.viewers.ITableColorProvider;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 
 import com.hudren.woodpile.WoodpilePlugin;
 import com.hudren.woodpile.model.LogEvent;
 
 import static com.hudren.woodpile.prefs.PreferenceConstants.COLOR_DEBUG_BACK;
 import static com.hudren.woodpile.prefs.PreferenceConstants.COLOR_DEBUG_FORE;
 import static com.hudren.woodpile.prefs.PreferenceConstants.COLOR_ERROR_BACK;
 import static com.hudren.woodpile.prefs.PreferenceConstants.COLOR_ERROR_FORE;
 import static com.hudren.woodpile.prefs.PreferenceConstants.COLOR_FATAL_BACK;
 import static com.hudren.woodpile.prefs.PreferenceConstants.COLOR_FATAL_FORE;
 import static com.hudren.woodpile.prefs.PreferenceConstants.COLOR_INFO_BACK;
 import static com.hudren.woodpile.prefs.PreferenceConstants.COLOR_INFO_FORE;
 import static com.hudren.woodpile.prefs.PreferenceConstants.COLOR_TRACE_BACK;
 import static com.hudren.woodpile.prefs.PreferenceConstants.COLOR_TRACE_FORE;
 import static com.hudren.woodpile.prefs.PreferenceConstants.COLOR_WARN_BACK;
 import static com.hudren.woodpile.prefs.PreferenceConstants.COLOR_WARN_FORE;
 
 /**
  * TODO SessionViewLabelProvider description
  * 
  * @author Jeff Hudren
  */
 public class SessionViewLabelProvider
 	extends LabelProvider
 	implements ITableLabelProvider, ITableColorProvider
 {
 
 	private final DateFormat df = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSS" );
 
 	private static final String NL = "\n";
 
 	private Map<Level, Color> foregroundColors;
 	private Map<Level, Color> backgroundColors;
 	private Map<Level, Image> icons;
 
 	private SessionView view;
 
 	public SessionViewLabelProvider( final SessionView view )
 	{
 		this.view = view;
 
 		createColors();
 		createIcons();
 	}
 
 	void createColors()
 	{
 		if ( foregroundColors != null )
 			disposeColors();
 
 		final IPreferenceStore store = WoodpilePlugin.getDefault().getPreferenceStore();
 
 		foregroundColors = new HashMap<Level, Color>();
 		backgroundColors = new HashMap<Level, Color>();
 
 		foregroundColors.put( Level.FATAL, new Color( null, PreferenceConverter.getColor( store, COLOR_FATAL_FORE ) ) );
 		backgroundColors.put( Level.FATAL, new Color( null, PreferenceConverter.getColor( store, COLOR_FATAL_BACK ) ) );
 		foregroundColors.put( Level.ERROR, new Color( null, PreferenceConverter.getColor( store, COLOR_ERROR_FORE ) ) );
 		backgroundColors.put( Level.ERROR, new Color( null, PreferenceConverter.getColor( store, COLOR_ERROR_BACK ) ) );
 		foregroundColors.put( Level.WARN, new Color( null, PreferenceConverter.getColor( store, COLOR_WARN_FORE ) ) );
 		backgroundColors.put( Level.WARN, new Color( null, PreferenceConverter.getColor( store, COLOR_WARN_BACK ) ) );
 		foregroundColors.put( Level.INFO, new Color( null, PreferenceConverter.getColor( store, COLOR_INFO_FORE ) ) );
 		backgroundColors.put( Level.INFO, new Color( null, PreferenceConverter.getColor( store, COLOR_INFO_BACK ) ) );
 		foregroundColors.put( Level.DEBUG, new Color( null, PreferenceConverter.getColor( store, COLOR_DEBUG_FORE ) ) );
 		backgroundColors.put( Level.DEBUG, new Color( null, PreferenceConverter.getColor( store, COLOR_DEBUG_BACK ) ) );
 		foregroundColors.put( Level.TRACE, new Color( null, PreferenceConverter.getColor( store, COLOR_TRACE_FORE ) ) );
 		backgroundColors.put( Level.TRACE, new Color( null, PreferenceConverter.getColor( store, COLOR_TRACE_BACK ) ) );
 	}
 
 	void disposeColors()
 	{
 		Iterator<Entry<Level, Color>> it = foregroundColors.entrySet().iterator();
 		while ( it.hasNext() )
 		{
 			final Entry<Level, Color> entry = it.next();
 
 			entry.getValue().dispose();
 			it.remove();
 		}
 
 		it = backgroundColors.entrySet().iterator();
 		while ( it.hasNext() )
 		{
 			final Entry<Level, Color> entry = it.next();
 
 			entry.getValue().dispose();
 			it.remove();
 		}
 	}
 
 	void createIcons()
 	{
 		if ( icons == null )
 		{
 			icons = new HashMap<Level, Image>();
 
 			icons.put( Level.FATAL, WoodpilePlugin.getDefault().getImage( "icons/fatalerror_obj.gif" ) );
 			icons.put( Level.ERROR, WoodpilePlugin.getDefault().getImage( "icons/error_obj.gif" ) );
 			icons.put( Level.WARN, WoodpilePlugin.getDefault().getImage( "icons/warning_obj.gif" ) );
 			icons.put( Level.INFO, WoodpilePlugin.getDefault().getImage( "icons/information.gif" ) );
 			icons.put( Level.DEBUG, WoodpilePlugin.getDefault().getImage( "icons/ldebug_obj.gif" ) );
 			icons.put( Level.TRACE, WoodpilePlugin.getDefault().getImage( "icons/trace.gif" ) );
 		}
 	}
 
 	/**
 	 * @see org.eclipse.jface.viewers.LabelProvider#dispose()
 	 */
 	@Override
 	public void dispose()
 	{
 		disposeColors();
 
 		super.dispose();
 	}
 
 	/**
 	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
 	 *      int)
 	 */
 	@Override
 	public Image getColumnImage( final Object element, final int columnIndex )
 	{
 		Image image = null;
 
 		if ( element instanceof LogEvent )
 		{
 			if ( columnIndex == 0 )
 			{
 				if ( icons == null )
 					createIcons();
 
 				image = icons.get( ( (LogEvent) element ).getLevel() );
 			}
 		}
 
 		return image;
 	}
 
 	/**
 	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
 	 *      int)
 	 */
 	@Override
 	public String getColumnText( final Object element, final int columnIndex )
 	{
 		String label = null;
 
 		if ( element instanceof LogEvent )
 		{
 			final LogEvent event = (LogEvent) element;
 
 			switch ( columnIndex )
 			{
 				case 1: // Time
 					label = df.format( event.getTimeStamp() );
 					break;
 
 				case 2: // Server
 					label = event.getServer();
 					break;
 
 				case 3: // Level
 					label = event.getLevel().toString();
 					break;
 
 				case 4: // Logger
 					label = event.getLoggerName();
 
 					if ( view.isShowSimpleName() )
 					{
 						int pos = label.lastIndexOf( '.' );
 						label = label.substring( pos + 1 );
 					}
 					break;
 
 				case 5: // Message
 					label = firstLine( event.getRenderedMessage() );
 					break;
 
 				case 6: // Throwable
 					final String[] rep = event.getThrowableStrRep();
 					if ( rep != null && rep.length > 0 )
 						label = rep[ 0 ];
 
 					if ( label != null )
 					{
 						int pos = label.indexOf( ':' );
						label = label.substring( 0, pos );

						pos = label.lastIndexOf( '.' );
						label = label.substring( pos + 1 );
 					}
 					break;
 
 				case 7: // Thread
 					label = event.getThreadName();
 					break;
 
 				case 8: // Host
 					label = event.getHost();
 					break;
 
 				default:
 					break;
 			}
 		}
 		return label;
 	}
 
 	private String firstLine( final String text )
 	{
 		String line = text;
 
 		if ( line != null )
 		{
 			// Strip newlines at beginning of message
 			int pos = line.indexOf( NL );
 			while ( pos == 0 )
 			{
 				line = line.substring( 1 );
 				pos = line.indexOf( NL );
 			}
 
 			// Include up to first newline
 			pos = line.indexOf( NL );
 			if ( pos > 0 )
 				line = line.substring( 0, pos - 1 );
 		}
 
 		return line;
 	}
 
 	/**
 	 * @see org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang.Object,
 	 *      int)
 	 */
 	@Override
 	public Color getBackground( final Object element, final int columnIndex )
 	{
 		Color color = null;
 
 		if ( element instanceof LogEvent )
 		{
 			final Level level = ( (LogEvent) element ).getLevel();
 
 			color = backgroundColors.get( level );
 		}
 
 		return color;
 	}
 
 	/**
 	 * @see org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang.Object,
 	 *      int)
 	 */
 	@Override
 	public Color getForeground( final Object element, final int columnIndex )
 	{
 		Color color = null;
 
 		if ( element instanceof LogEvent )
 		{
 			final Level level = ( (LogEvent) element ).getLevel();
 
 			color = foregroundColors.get( level );
 		}
 
 		return color;
 	}
 
 }
