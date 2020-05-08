 package com.patrick_vane.unrealscript.editor.outline;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
 import com.patrick_vane.unrealscript.editor.UnrealScriptEditor;
 import com.patrick_vane.unrealscript.editor.outline.actions.ToggleSortAction;
 import com.patrick_vane.unrealscript.editor.outline.actions.ToggleVariablesAction;
 import com.patrick_vane.unrealscript.editor.parser.UnrealScriptAttributes;
 
 
 public class OutlineContentPage extends ContentOutlinePage
 {
 	private final IEditorInput input;
 	
 	private TreeViewer outlineViewer;
 	private IFile file;
 	private UnrealScriptAttributes lastAttributes;
 	
 	private boolean runThread = true;
 	private Thread thread = new Thread()
 	{
 		@Override
 		public void run()
 		{
 			while( runThread )
 			{
 				try
 				{
 					if( runThread && file.equals(UnrealScriptEditor.getActiveIFile()) )
 					{
 						update();
 					}
 				}
 				catch( Exception e )
 				{
 				}
 				
 				try
 				{
 					Thread.sleep( 2000 );
 				}
 				catch( Exception e )
 				{
 				}
 			}
 		}
 	};
 	
 	
 	public OutlineContentPage( IFile file, IEditorInput input )
 	{
 		this.input = input;
 		this.file  = file;
 		
 		thread.setDaemon( true );
 	}
 	
 	
 	public void update()
 	{
		update( false );
	}
	public void update( boolean forced )
	{
 		try
 		{
 			final UnrealScriptAttributes attributes = UnrealScriptEditor.getUnrealScriptAttributes( UnrealScriptEditor.getClassName(file) );
			if( forced || (lastAttributes == null) || !lastAttributes.equals(attributes) )
 			{
 				lastAttributes = attributes;
 				
 				Display.getDefault().syncExec
 				(
 					new Runnable()
 					{
 						@Override
 						public void run()
 						{
 							try
 							{
 								Tree control = null;
 								int horizontal = 0;
 								int vertical = 0;
 								if( outlineViewer.getControl() instanceof Tree )
 								{
 									control = (Tree) outlineViewer.getControl();
 									if( control != null )
 									{
 										if( control.getHorizontalBar() != null )
 											horizontal = control.getHorizontalBar().getSelection();
 										if( control.getVerticalBar() != null )
 											vertical = control.getVerticalBar().getSelection();
 									}
 								}
 								
 								Object[] expanded = outlineViewer.getExpandedElements();
 								//ISelection selection = outlineViewer.getSelection();
 								
 								outlineViewer.setInput( attributes );
 								outlineViewer.collapseAll();
 								outlineViewer.refresh( true );
 								
 								outlineViewer.setExpandedElements( expanded );
 								//outlineViewer.setSelection( selection );
 								
 								if( control != null )
 								{
 									if( control.getHorizontalBar() != null )
 										control.getHorizontalBar().setSelection( horizontal );
 									if( control.getVerticalBar() != null )
 										control.getVerticalBar().setSelection( vertical );
 								}
 							}
 							catch( Exception e )
 							{
 							}
 						}
 					}
 				);
 			}
 		}
 		catch( Exception e )
 		{
 		}
 	}
 	
 	
 	@Override
 	public void createControl( Composite parent )
 	{
 		super.createControl( parent );
 		outlineViewer = getTreeViewer();
 		outlineViewer.setContentProvider( new OutlineContentProvider() );
 		outlineViewer.setLabelProvider( new OutlineLabelProvider() );
 		outlineViewer.setSorter( new OutlineSorter() );
 		outlineViewer.addSelectionChangedListener( new OutlineSelectionChangedListener() );
 		outlineViewer.addDoubleClickListener( new OutlineDoubleClickListener() );
 		outlineViewer.setAutoExpandLevel( 0 );
 		outlineViewer.setInput( input );
 		
 		if( runThread && !thread.isAlive() )
 		{
 			thread.start();
 		}
 	}
 	
 	@Override
 	public void setActionBars( IActionBars actionBars )
 	{
 	    actionBars.getToolBarManager().add( new ToggleSortAction(this) );
 	    actionBars.getToolBarManager().add( new ToggleVariablesAction(this) );
 	    actionBars.getToolBarManager().update( false );       
 	    actionBars.updateActionBars();      
 	}
 	
 	
 	@Override
 	public void dispose()
 	{
 		runThread = false;
 		super.dispose();
 	}
 }
