 /*
  * Copyright 2008 Ricardo Pescuma Domenecci
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
 
 import java.util.ArrayList;
 import java.util.List;
 
import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.pescuma.jfg.Attribute;
 import org.pescuma.jfg.AttributeGroup;
 import org.pescuma.jfg.AttributeList;
 import org.pescuma.jfg.gui.swt.JfgFormData.FieldConfig;
 
 class InlineObjectListSWTWidget extends AbstractSWTWidget
 {
 	private SWTLayoutBuilder.ListBuilder listLayout;
 	private InnerBuilder innerBuilder;
 	private AttributeList list;
 	private final List<Item> items = new ArrayList<Item>();
 	private Composite frame;
 	private Color background;
 	
 	InlineObjectListSWTWidget(Attribute attrib, JfgFormData data)
 	{
 		super(attrib, data);
 	}
 	
 	class Item
 	{
 		SWTLayoutBuilder.ListBuilder.ListItem listItem;
 		Attribute attrib;
 	}
 	
 	@Override
 	protected void createWidgets(SWTLayoutBuilder layout, InnerBuilder innerBuilder)
 	{
 		this.innerBuilder = innerBuilder;
 		list = attrib.asList();
 		
 		listLayout = layout.addList(list.getName(), createLayoutHints(attrib), createHeightHint(attrib));
 		frame = listLayout.getContents();
 		background = frame.getBackground();
 		
		frame.addListener(SWT.Dispose, getDisposeListener());
		
 		Control addMore = null;
 		if (list.canWrite())
 		{
 			Listener listener = new Listener() {
 				@Override
 				public void handleEvent(Event event)
 				{
 					buildAttributeInsideList(list.createNewEmptyElement(), true);
 					onWidgetModify();
 				}
 			};
 			addMore = data.componentFactory.createFlatButton(listLayout.getParentForAddMore(),
 					data.textTranslator.translate(list.getName() + ":Add"), "icons/add.png", listener);
 			listLayout.addAddMore(addMore);
 		}
 	}
 	
 	protected void buildAttributeInsideList(Attribute itemAttribute, boolean addingNew)
 	{
 		final Item item = new Item();
 		
 		Composite composite = listLayout.startListItem(list.getName());
 		
 		buildInnerAttribute(composite, itemAttribute, addingNew);
 		
 		Control remove = null;
 		if (list.canWrite())
 		{
 			Listener listener = new Listener() {
 				@Override
 				public void handleEvent(Event event)
 				{
 					listLayout.removeListItem(item.listItem);
 					items.remove(item);
 					onWidgetModify();
 				}
 			};
 			
 			remove = data.componentFactory.createFlatButton(listLayout.getParentForRemove(),
 					data.textTranslator.translate(list.getName() + ":Remove"), "icons/delete.png", listener);
 		}
 		
 		item.listItem = listLayout.endListItem(list.getName(), remove);
 		item.attrib = itemAttribute;
 		items.add(item);
 	}
 	
 	private void buildInnerAttribute(Composite composite, Attribute itemAttribute, boolean addingNew)
 	{
 		// If someone set the type, lets build it
 		FieldConfig config = data.fieldsConfig.get(itemAttribute.getName());
 		if (config != null && config.type != null)
 		{
 			SWTLayoutBuilder layout = data.createLayoutFor(itemAttribute.getName(), composite,
 					listLayout.getLayoutListener());
 			
 			innerBuilder.startBuilding();
 			addWidget(innerBuilder.buildInnerAttribute(layout, itemAttribute));
 			innerBuilder.finishBuilding();
 			
 			return;
 		}
 		
 		if (addingNew && itemAttribute.getValue() == null)
 		{
 			// Lets try to create one here
 			Object val = list.createNewElementInstance();
 			if (val != null)
 				itemAttribute.setValue(val);
 		}
 		
 		// Else, if we can get a group from it, let's use it
 		AttributeGroup group = itemAttribute.asGroup();
 		if (group != null)
 		{
 			SWTLayoutBuilder layout = data.createLayoutFor(group.getName(), composite, listLayout.getLayoutListener());
 			
 			innerBuilder.startBuilding();
 			
 			HiddenSWTWidget child = new HiddenSWTWidget(itemAttribute, data);
 			addWidget(child);
 			
 			for (Attribute ga : group.getAttributes())
 				child.addWidget(innerBuilder.buildInnerAttribute(layout, ga));
 			
 			innerBuilder.finishBuilding();
 			
 			return;
 		}
 		
 		// Else try the default way
 		SWTLayoutBuilder layout = data.createLayoutFor(itemAttribute.getName(), composite,
 				listLayout.getLayoutListener());
 		
 		innerBuilder.startBuilding();
 		addWidget(innerBuilder.buildInnerAttribute(layout, itemAttribute));
 		innerBuilder.finishBuilding();
 	}
 	
 	@Override
 	protected boolean canCopyToAttribute()
 	{
 		return list.canWrite();
 	}
 	
 	@Override
 	public Object getValue()
 	{
 		return attrib.getValue();
 	}
 	
 	@Override
 	public void setValue(Object value)
 	{
 		if (value != attrib.getValue())
 			throw new UnsupportedOperationException();
 	}
 	
 	@Override
 	protected void attibuteToGUI()
 	{
 		for (int i = 0; i < items.size(); i++)
 		{
 			Item item = items.get(i);
 			if (list.indexOf(item.attrib) < 0)
 			{
 				listLayout.removeListItem(item.listItem);
 				items.remove(i);
 				--i;
 			}
 		}
 		
 		for (int i = 0; i < list.size(); i++)
 		{
 			Attribute a = list.get(i);
 			int index = indexOf(a);
 			if (index < 0)
 			{
 				buildAttributeInsideList(a, false);
 				index = items.size() - 1;
 			}
 			if (index != i)
 			{
 				Item item = items.get(index);
 				
 				listLayout.moveAfter(i > 0 ? items.get(i - 1).listItem : null, item.listItem);
 				
 				items.remove(index);
 				items.add(i, item);
 			}
 		}
 	}
 	
 	@Override
 	protected void guiToAttribute()
 	{
 		for (int i = 0; i < list.size(); i++)
 		{
 			if (indexOf(list.get(i)) < 0)
 			{
 				list.remove(i);
 				--i;
 			}
 		}
 		
 		for (int i = 0; i < items.size(); i++)
 		{
 			Item item = items.get(i);
 			int index = list.indexOf(item.attrib);
 			if (index < 0)
 			{
 				list.add(i, item.attrib);
 			}
 			else if (index != i)
 			{
 				list.remove(index);
 				list.add(i, item.attrib);
 			}
 		}
 	}
 	
 	private int indexOf(Attribute a)
 	{
 		for (int i = 0; i < items.size(); i++)
 		{
 			Item item = items.get(i);
 			if (item.attrib == a)
 				return i;
 		}
 		return -1;
 	}
 	
 	@Override
 	public void setEnabled(boolean enabled)
 	{
 		frame.setEnabled(enabled);
 		
 		super.setEnabled(enabled);
 	}
 	
 	@Override
 	protected void updateColor()
 	{
 		frame.setBackground(createColor(frame, background));
 	}
 	
 	@Override
 	public void copyToGUI()
 	{
 		Shell shell = frame.getShell();
 		
 		shell.setRedraw(false);
 		try
 		{
 			super.copyToGUI();
 		}
 		finally
 		{
 			shell.setRedraw(true);
 		}
 	}
 }
