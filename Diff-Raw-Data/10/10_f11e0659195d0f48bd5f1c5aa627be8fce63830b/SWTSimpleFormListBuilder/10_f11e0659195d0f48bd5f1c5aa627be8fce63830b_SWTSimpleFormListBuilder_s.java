 /*
  * Copyright 2010 Ricardo Pescuma Domenecci
  * 
  * This file is part of jfg.
  * 
  * jfg is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  * 
  * jfg is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License along with jfg. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.pescuma.jfg.gui.swt;
 
 import static org.eclipse.swt.layout.GridData.*;
 import static org.pescuma.jfg.gui.swt.SWTUtils.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.pescuma.jfg.gui.swt.SWTLayoutBuilder.ListBuilder;
 
 public class SWTSimpleFormListBuilder implements ListBuilder
 {
 	private final Group parent;
 	private final Runnable layoutListener;
 	private final JfgFormData data;
 	
 	private int listItemStart = 0;
 	private Composite addMoreParent;
 	private int internalNumColumns = -1;
 	
 	private static class ControlsToRemove implements ListItem
 	{
 		final List<Control> constrols = new ArrayList<Control>();
 	}
 	
 	public SWTSimpleFormListBuilder(String attributeName, Group frame, Runnable layoutListener, JfgFormData data)
 	{
 		this.parent = frame;
 		this.layoutListener = layoutListener;
 		this.data = data;
 	}
 	
 	@Override
 	public Composite getContents()
 	{
 		return parent;
 	}
 	
 	@Override
 	public Runnable getLayoutListener()
 	{
 		return layoutListener;
 	}
 	
 	@Override
 	public Composite getParentForAddMore()
 	{
 		addMoreParent = data.componentFactory.createComposite(parent, SWT.NONE);
 		setupHorizontalComposite(addMoreParent, Integer.MAX_VALUE);
 		return addMoreParent;
 	}
 	
 	@Override
 	public void addAddMore(Control addMore)
 	{
 		if (addMore != null)
 			addMore.setLayoutData(new GridData(FILL_HORIZONTAL));
 	}
 	
 	@Override
 	public Composite startListItem(String attributeName)
 	{
 		listItemStart = parent.getChildren().length;
 		return parent;
 	}
 	
 	@Override
 	public Composite getParentForRemove()
 	{
 		Control[] children = parent.getChildren();
 		if (children.length == listItemStart)
 		{
 			// Create empty composite to fill space
 			Composite composite = data.componentFactory.createComposite(parent, SWT.NONE);
 			setupHorizontalComposite(composite, updateInternalNumColumns());
 		}
 		return parent;
 	}
 	
 	@Override
 	public ListItem endListItem(String attributeName, Control remove)
 	{
 		int addMoreOffset = (addMoreParent != null ? -1 : 0);
 		
 		if (addMoreParent != null)
 		{
 			Control[] children = parent.getChildren();
 			
 			int index = indexOf(children, addMoreParent);
 			if (index < 0)
 				throw new IllegalStateException();
 			if (index < listItemStart)
 				listItemStart--;
 			
 			addMoreParent.moveBelow(null);
 		}
 		
 		if (remove != null)
 		{
 			remove.moveBelow(null);
 			
 			Control[] children = parent.getChildren();
 			
 			int items = 0;
 			for (int i = listItemStart; i < children.length - 1 + addMoreOffset; i++)
 				items += getGridData(children[i]).horizontalSpan;
 			items /= updateInternalNumColumns();
 			
 			if (items == 0)
 				throw new IllegalStateException();
 			
 			int moveBelow = findNextLineControl(children, listItemStart);
 			remove.moveBelow(children[moveBelow]);
 			
 			if (items > 1)
 			{
 				Label empty = data.componentFactory.createLabel(parent, SWT.NONE);
 				GridData gridData = new GridData();
 				gridData.verticalSpan = items - 1;
 				empty.setLayoutData(gridData);
 				
 				moveBelow = findNextLineControl(children, moveBelow + 1);
 				empty.moveBelow(children[moveBelow]);
 			}
 		}
 		
 		if (addMoreParent != null)
 			getGridData(addMoreParent).horizontalSpan = getCurrentParentNumColumns();
 		
 		Control[] children = parent.getChildren();
 		
 		SWTSimpleFormListBuilder.ControlsToRemove constrols = new ControlsToRemove();
 		for (int i = listItemStart; i < children.length + addMoreOffset; i++)
 			constrols.constrols.add(children[i]);
 		
 		layoutListener.run();
 		
 		return constrols;
 	}
 	
 	private int updateInternalNumColumns()
 	{
 		if (internalNumColumns != getCurrentParentNumColumns() - 1)
 		{
 			GridLayout parentLayout = (GridLayout) parent.getLayout();
 			internalNumColumns = parentLayout.numColumns;
 			parentLayout.numColumns++;
 		}
 		
 		return internalNumColumns;
 	}
 	
 	private int getCurrentParentNumColumns()
 	{
 		GridLayout layout = (GridLayout) parent.getLayout();
 		if (layout == null)
 			return 1;
 		return layout.numColumns;
 	}
 	
 	private int findNextLineControl(Control[] children, int start)
 	{
 		int moveBelow = start;
 		int numCols = updateInternalNumColumns();
 		for (int columns = 0; columns < numCols; moveBelow++)
 			columns += getGridData(children[moveBelow]).horizontalSpan;
 		return moveBelow - 1;
 	}
 	
 	private int indexOf(Control[] arr, Control find)
 	{
 		for (int i = 0; i < arr.length; i++)
 		{
 			if (arr[i] == find)
 				return i;
 		}
 		return -1;
 	}
 	
 	private GridData getGridData(Control children)
 	{
 		return (GridData) children.getLayoutData();
 	}
 	
 	@Override
 	public void removeListItem(ListItem item)
 	{
 		SWTSimpleFormListBuilder.ControlsToRemove constrols = (SWTSimpleFormListBuilder.ControlsToRemove) item;
 		for (Control control : constrols.constrols)
 			control.dispose();
 		layoutListener.run();
 	}
 	
 	@Override
 	public void moveAfter(ListItem baseItem, ListItem itemToMove)
 	{
 		SWTSimpleFormListBuilder.ControlsToRemove constrolsToMove = (SWTSimpleFormListBuilder.ControlsToRemove) itemToMove;
 		SWTSimpleFormListBuilder.ControlsToRemove baseConstrols = (SWTSimpleFormListBuilder.ControlsToRemove) baseItem;
 		
 		if (baseConstrols == null)
 		{
 			for (int i = constrolsToMove.constrols.size() - 1; i > 0; i--)
 				constrolsToMove.constrols.get(i).moveAbove(null);
 		}
 		else
 		{
 			Control base = baseConstrols.constrols.get(baseConstrols.constrols.size() - 1);
 			for (int i = constrolsToMove.constrols.size() - 1; i > 0; i--)
 				constrolsToMove.constrols.get(i).moveBelow(base);
 		}
 		
 		layoutListener.run();
 	}
 }
