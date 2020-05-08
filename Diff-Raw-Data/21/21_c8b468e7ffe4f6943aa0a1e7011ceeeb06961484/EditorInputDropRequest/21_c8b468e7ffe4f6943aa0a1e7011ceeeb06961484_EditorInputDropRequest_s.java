 /*******************************************************************************
  * Copyright (c) 2009 Jens von Pilgrim and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Jens von Pilgrim - initial API and implementation
  ******************************************************************************/
 package org.eclipse.gef3d.ext.multieditor.dnd;
 
 import java.util.List;
 
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.gef.Request;
 import org.eclipse.gef.requests.DropRequest;
 import org.eclipse.gef3d.ext.multieditor.IMultiEditor;
 import org.eclipse.ui.IEditorInput;
 
 /**
  * Request indicating an {@link IEditorInput} has been dropped onto a figure. In
  * order to work, the editor using this policy must implement the
  * {@link IMultiEditor} interface. It is used in conjunction with the
  * {@link EditorInputTransferDropTargetListener} which creates this request and
  * also calls all setters.
  * 
  * @author Jens von Pilgrim
  * @version $Revision$
  * @since Apr 15, 2009
  */
 public class EditorInputDropRequest extends Request implements DropRequest {
 
 	protected Point location;
 
 	protected List<IEditorInput> editorInputs;
 
 	protected IMultiEditor multiEditor;
 
 	/**
 	 * Returns the {@link IEditorInput}s which were dropped onto the
 	 * editor, i.e. the figure. This value is set after the mouse has been
 	 * released and the drop actually occurred.
 	 * 
 	 * @return the editorInputs
 	 */
 	public List<IEditorInput> getEditorInputs() {
 		return editorInputs;
 	}
 
 	/**
 	 * Called by {@link EditorInputTransferDropTargetListener} when the drop
 	 * actually occurred (that is after the mouse button has been released).
 	 * 
 	 * @param i_editorInputs the editorInputs to set
 	 */
 	protected void setEditorInputs(List<IEditorInput> i_editorInputs) {
 		editorInputs = i_editorInputs;
 	}
 
 	/**
 	 * Returns the multi editor in which context this request has been created,
 	 * that is on which the {@link IEditorInput} was been dropped.
 	 * 
 	 * @return the dropableMultiEditor
 	 */
 	public IMultiEditor getMultiEditor() {
 		return multiEditor;
 	}
 
 	/**
 	 * Called by {@link EditorInputTransferDropTargetListener} when the request
 	 * is created.
 	 * 
 	 * @param i_multiEditor the dropableMultiEditor to set
 	 */
 	protected void setMultiEditor(IMultiEditor i_multiEditor) {
 		multiEditor = i_multiEditor;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.gef.requests.DropRequest#getLocation()
 	 */
 	public Point getLocation() {
 		return location;
 	}
 
 	/**
 	 * Called by {@link EditorInputTransferDropTargetListener} when the request
 	 * is created or updated.
 	 * 
 	 * @param i_location the location to set
 	 */
 	protected void setLocation(Point i_location) {
 		location = i_location;
 	}
 
 	/** 
 	 * {@inheritDoc}
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		StringBuilder result = new StringBuilder();
 		result.append("EditorInputRequest");
 		if (location!=null) {
 			result.append(" at ").append(location.toString());
 		}
 		if (editorInputs!=null) {
 			result.append(" with input ").append(editorInputs.toString());
 		} else {
 			result.append(" w/o input information");
 		}
 		return result.toString();
 	}
 	
 	
 
 }
