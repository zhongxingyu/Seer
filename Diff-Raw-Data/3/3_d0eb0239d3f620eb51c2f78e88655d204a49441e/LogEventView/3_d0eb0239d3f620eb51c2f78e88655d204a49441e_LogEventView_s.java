 /*
  * Project: com.hudren.woodpile
  * File:    LogEventView.java
  *
  * Author:  Jeff Hudren
  * Created: May 6, 2006
  *
  * Copyright (c) 2006 Hudren Andromeda Connection. All rights reserved. 
  */
 
 package com.hudren.woodpile.views;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.resource.FontRegistry;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.dnd.Clipboard;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.ui.themes.ITheme;
 import org.eclipse.ui.themes.IThemeManager;
 
 import com.hudren.woodpile.WoodpilePlugin;
 import com.hudren.woodpile.model.LogEvent;
 import com.hudren.woodpile.model.Session;
 import com.hudren.woodpile.model.SessionListener;
 
 /**
  * TODO LogEventView description
  * 
  * @author Jeff Hudren
  */
 public class LogEventView
 	extends ViewPart
 	implements SessionListener
 {
 
 	public static final String ID = "com.hudren.woodpile.views.LogEventView";
 
 	private LogEvent event;
 
 	private Font font;
 	private StyledText text;
 
 	private Action copyAction;
 
 	private Clipboard clipboard;
 	private ISelectionListener pageSelectionListener;
 
 	/**
 	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
 	 */
 	@Override
 	public void createPartControl( final Composite parent )
 	{
 		createTextViewer( parent );
 		createActions();
 
 		contributeToActionBars();
 		hookPageSelection();
 
 		WoodpilePlugin.getDefault().getCurrentSession().addListener( this );
 	}
 
 	private void createTextViewer( final Composite parent )
 	{
 		// font = new Font( null, "Courier New", 10, SWT.NORMAL );
 
 		IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
 		ITheme currentTheme = themeManager.getCurrentTheme();
 
 		FontRegistry fontRegistry = currentTheme.getFontRegistry();
 		Font font = fontRegistry.get( "org.eclipse.debug.ui.consoleFont" );
 
 		text = new StyledText( parent, SWT.MULTI | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL );
 		text.setFont( font );
 		text.setCaret( null );
 	}
 
 	private void contributeToActionBars()
 	{
 		final IActionBars bars = getViewSite().getActionBars();
 		fillLocalPullDown( bars.getMenuManager() );
 		fillLocalToolBar( bars.getToolBarManager() );
 	}
 
 	private void fillLocalPullDown( final IMenuManager manager )
 	{
 	}
 
 	private void fillLocalToolBar( final IToolBarManager manager )
 	{
 		manager.add( copyAction );
 	}
 
 	private void createActions()
 	{
 		final IWorkbench workbench = PlatformUI.getWorkbench();
 		final ISharedImages platformImages = workbench.getSharedImages();
 
 		copyAction = new Action()
 		{
 
 			@Override
 			public void run()
 			{
 				final TextContent content = new TextContent( Display.getDefault() );
 				content.setContent( event.getStrRep() );
 
 				getClipboard().setContents( new Object[] { content.toPlainText() }, new Transfer[] { TextTransfer.getInstance() } );
 			}
 		};
 		copyAction.setText( "Copy" );
 		copyAction.setToolTipText( "Copy to Clipboard" );
 		copyAction.setImageDescriptor( platformImages.getImageDescriptor( ISharedImages.IMG_TOOL_COPY ) );
 	}
 
 	private void hookPageSelection()
 	{
 		pageSelectionListener = new ISelectionListener()
 		{
 
 			public void selectionChanged( final IWorkbenchPart part, final ISelection selection )
 			{
 				pageSelectionChanged( part, selection );
 			}
 		};
 
 		getSite().getPage().addPostSelectionListener( pageSelectionListener );
 	}
 
 	protected void pageSelectionChanged( final IWorkbenchPart part, final ISelection selection )
 	{
 		if ( part instanceof SessionView )
 			if ( selection instanceof IStructuredSelection )
 				if ( !selection.isEmpty() )
 				{
 					final Iterator it = ( (IStructuredSelection) selection ).iterator();
 					if ( it.hasNext() )
 					{
 						final Object obj = it.next();
 						if ( obj instanceof LogEvent )
 							updateText( ( (LogEvent) obj ) );
 					}
 				}
 				else
 					updateText( null );
 	}
 
 	/**
 	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
 	 */
 	@Override
 	public void setFocus()
 	{
 		final IViewPart view = getSite().getPage().findView( SessionView.ID );
 		if ( view != null )
 		{
 			final ISelectionProvider provider = view.getViewSite().getSelectionProvider();
 			if ( provider != null )
 			{
 				final ISelection selection = provider.getSelection();
 
 				if ( selection instanceof IStructuredSelection )
 				{
 					final Iterator it = ( (IStructuredSelection) selection ).iterator();
 					if ( it.hasNext() )
 					{
 						final Object obj = it.next();
 						if ( obj instanceof LogEvent )
 							updateText( ( (LogEvent) obj ) );
 					}
 				}
 			}
 		}
 
 		text.setFocus();
 	}
 
 	/**
 	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
 	 */
 	@Override
 	public void dispose()
 	{
 		WoodpilePlugin.getDefault().getCurrentSession().removeListener( this );
 
		font.dispose();
 
 		super.dispose();
 	}
 
 	private void updateText( final LogEvent event )
 	{
 		if ( !text.isDisposed() )
 		{
 			if ( event != null )
 			{
 				final TextContent content = new TextContent( Display.getDefault() );
 				content.setContent( event.getStrRep() );
 
 				text.setText( content.toPlainText() );
 				text.setStyleRanges( content.getStyleRanges() );
 			}
 			else
 				text.setText( "" );
 			// text.setStyleRanges( null );
 
 			text.setTopIndex( 0 );
 		}
 
 		this.event = event;
 	}
 
 	public void eventsChanged( final Session session, final List<LogEvent> removed, final List<LogEvent> added )
 	{
 	}
 
 	public void sessionCleared( final Session session )
 	{
 		updateText( null );
 	}
 
 	/**
 	 * @see com.hudren.woodpile.model.SessionListener#sessionChanged(com.hudren.woodpile.model.Session)
 	 */
 	public void sessionChanged( final Session session )
 	{
 	}
 
 	/**
 	 * Getter for event
 	 * 
 	 * @return event
 	 */
 	public LogEvent getEvent()
 	{
 		return event;
 	}
 
 	/**
 	 * Lazily initialize and answer the clipboard
 	 * 
 	 * @return the clipboard (not <code>null</code>)
 	 */
 	public Clipboard getClipboard()
 	{
 		if ( clipboard == null )
 			clipboard = new Clipboard( getSite().getShell().getDisplay() );
 
 		return clipboard;
 	}
 
 }
