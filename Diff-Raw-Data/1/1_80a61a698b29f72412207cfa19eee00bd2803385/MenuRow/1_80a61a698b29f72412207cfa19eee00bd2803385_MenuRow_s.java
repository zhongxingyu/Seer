 /*
  * $Id$
  * $Revision$
  * $Date$
  * 
  * ==============================================================================
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package wicket.contrib.markup.html.navmenu;
 
 import wicket.AttributeModifier;
 import wicket.Component;
 import wicket.ResourceReference;
 import wicket.WicketRuntimeException;
 import wicket.markup.html.WebComponent;
 import wicket.markup.html.WebMarkupContainer;
 import wicket.markup.html.list.ListItem;
 import wicket.markup.html.list.ListView;
 import wicket.markup.html.panel.Panel;
 import wicket.markup.html.resources.StyleSheetReference;
 import wicket.model.IModel;
 import wicket.model.Model;
 
 /**
  * One row of a menu. Starts by 0 (zero).
  * 
  * @author Eelco Hillenius
  */
 public class MenuRow extends Panel
 {
 	/**
 	 * Listview for a menu row.
 	 */
 	private final class RowListView extends ListView
 	{
 		/**
 		 * Construct.
 		 * 
 		 * @param id
 		 * @param model
 		 */
 		public RowListView(String id, IModel model)
 		{
 			super(id, model);
 			setOptimizeItemRemoval(false);
 		}
 
 		/**
 		 * @see wicket.markup.html.list.ListView#populateItem(wicket.markup.html.list.ListItem)
 		 */
 		protected void populateItem(ListItem item)
 		{
 			final MenuItem menuItem = (MenuItem)item.getModelObject();
 
 			final Panel itemPanel = menuItem.newItemPanel("itemPanel", MenuRow.this);
 			if (itemPanel == null)
 			{
 				throw new WicketRuntimeException("item panel must be not-null");
 			}
 			if (!"itemPanel".equals(itemPanel.getId()))
 			{
 				throw new WicketRuntimeException("item panel must have id 'itemPanel' assigned");
 			}
 
			itemPanel.setRenderBodyOnly(true);
 			item.add(itemPanel);
 		}
 	}
 
 	/** this row's style. */
 	private final MenuRowStyle style;
 
 	/**
 	 * Construct using a default style.
 	 * 
 	 * @param id
 	 *            component id
 	 * @param model
 	 *            row model
 	 * @see MenuRowStyle
 	 */
 	public MenuRow(final String id, final MenuRowModel model)
 	{
 		this(id, model, new MenuRowStyle());
 	}
 
 	/**
 	 * Construct using the provided row style.
 	 * 
 	 * @param id
 	 *            component id
 	 * @param model
 	 *            row model
 	 * @param style
 	 *            row style
 	 */
 	public MenuRow(final String id, final MenuRowModel model, final MenuRowStyle style)
 	{
 		super(id, model);
 
 		if (model == null)
 		{
 			throw new NullPointerException("argument model may not be null");
 		}
 		if (style == null)
 		{
 			throw new NullPointerException("argument style may not be null");
 		}
 
 		this.style = style;
 		WebMarkupContainer div = new WebMarkupContainer("div");
 		div.add(new AttributeModifier("class", true, new Model())
 		{
 			public Object getObject(Component component)
 			{
 				return style.getContainerCSSClass();
 			}
 		});
 
 		WebMarkupContainer ul = new WebMarkupContainer("ul");
 		ul.add(new AttributeModifier("class", true, new Model()
 		{
 			public Object getObject(Component component)
 			{
 				return style.getRowCSSClass();
 			}
 		}));
 		ul.add(new RowListView("columns", model));
 		div.add(ul);
 		add(div);
 
 		ResourceReference styleSheetResource = style.getStyleSheetResource();
 		if (styleSheetResource != null)
 		{
 			add(new StyleSheetReference("cssStyleResource", styleSheetResource));
 		}
 		else
 		{
 			add(new WebComponent("cssStyleResource").setVisible(false));
 		}
 	}
 
 	/**
 	 * @see wicket.Component#isVersioned()
 	 */
 	public final boolean isVersioned()
 	{
 		return false;
 	}
 
 	/**
 	 * Gets the row style.
 	 * 
 	 * @return row style
 	 */
 	public final MenuRowStyle getRowStyle()
 	{
 		return style;
 	}
 }
