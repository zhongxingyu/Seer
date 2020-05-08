 /*******************************************************************************
  * Copyright (c) 2007-2009  Miguel Rojas <miguelrojasch@users.sf.net>
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
  *
  * Contact: http://www.bioclipse.net/
  ******************************************************************************/
 package net.bioclipse.reaction.view;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import net.bioclipse.reaction.editor.ReactionEditor;
 import net.bioclipse.reaction.editparts.tree.ROutPageEditPartFactory;
 import net.bioclipse.reaction.wizards.FormWizardContextMenuProvider;
 
 import org.eclipse.draw2d.LightweightSystem;
 import org.eclipse.draw2d.Viewport;
 import org.eclipse.draw2d.parts.ScrollableThumbnail;
 import org.eclipse.gef.LayerConstants;
 import org.eclipse.gef.editparts.ScalableRootEditPart;
 import org.eclipse.gef.ui.parts.ContentOutlinePage;
 import org.eclipse.gef.ui.parts.TreeViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.part.IPageSite;
 
 /**
  * A ContentOutlinePage which contains also zoom-viewer
  * 
  * @author Miguel Rojas
  */
 public class ReactionOutLinePage extends ContentOutlinePage implements PropertyChangeListener{
 	private SashForm sash;
 	private ScrollableThumbnail thumbnail;
 	private DisposeListener disposeListener;
 	private ReactionEditor reactionEditor;
 	
 	public ReactionOutLinePage(ReactionEditor reactionEditor) {
 		super(new TreeViewer());
 		this.reactionEditor = reactionEditor;
 	}
 	
 	public void init(IPageSite pageSite) {
 	    super.init(pageSite);
 	    IActionBars bars = pageSite.getActionBars();
 	    bars.setGlobalActionHandler(ActionFactory.UNDO.getId(),reactionEditor.getEditorActionRegistry().getAction(ActionFactory.UNDO.getId())); 
 	    bars.setGlobalActionHandler(ActionFactory.REDO.getId(), reactionEditor.getEditorActionRegistry().getAction(ActionFactory.REDO.getId())); 
 	    bars.setGlobalActionHandler(ActionFactory.DELETE.getId(), reactionEditor.getEditorActionRegistry().getAction(ActionFactory.DELETE.getId()));
 	    bars.updateActionBars();
 	    
 	    getViewer().setKeyHandler(reactionEditor.getKeyHandler());
 	    
 	    getViewer().setContextMenu(new FormWizardContextMenuProvider(  getViewer(), reactionEditor.getEditorActionRegistry()));  
 	}
 	
 	public void createControl(Composite parent) {
 	    sash = new SashForm(parent, SWT.VERTICAL);
         
 	    getViewer().createControl(sash);
 	    
 	    getViewer().setEditDomain(reactionEditor.getEditorEditDomain());
 	    getViewer().setEditPartFactory(new ROutPageEditPartFactory(reactionEditor.getContentsModel()));
 	    getViewer().setContents(reactionEditor.getContentsModel());
 	    reactionEditor.getEditorSelectionSynchronizer().addViewer(getViewer());
 	      
 	    Canvas canvas = new Canvas(sash, SWT.BORDER);
 	    LightweightSystem lws = new LightweightSystem(canvas);
 
 	    thumbnail = new ScrollableThumbnail(
 	        (Viewport) ((ScalableRootEditPart) reactionEditor.getEditorGraphicalViewer()
 	            .getRootEditPart()).getFigure());
 	    thumbnail.setSource(((ScalableRootEditPart) reactionEditor.getEditorGraphicalViewer()
 	        .getRootEditPart())
 	        .getLayer(LayerConstants.PRINTABLE_LAYERS));
 	      
 	    lws.setContents(thumbnail);
 
 	    disposeListener = new DisposeListener() {
 	      public void widgetDisposed(DisposeEvent e) {
 	        if (thumbnail != null) {
 	          thumbnail.deactivate();
 	          thumbnail = null;
 	        }
 	      }
 	    };
 	    reactionEditor.getEditorGraphicalViewer().getControl().addDisposeListener(
 	        disposeListener);
     }
 
 	public Control getControl() {
     	return sash;
 	}
 
 	public void dispose() {
 		reactionEditor.getEditorSelectionSynchronizer().removeViewer(getViewer());
 
 	    if (reactionEditor.getEditorGraphicalViewer().getControl() != null
 	            && !reactionEditor.getEditorGraphicalViewer().getControl().isDisposed())
 	    	reactionEditor.getEditorGraphicalViewer().getControl().removeDisposeListener(disposeListener);
 	    super.dispose();
 
 	}
 
 	public void propertyChange(PropertyChangeEvent evt) {
 		
 	}
 }
