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
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.MutableTreeNode;
 import javax.swing.tree.TreeNode;
 
 import wicket.AttributeModifier;
 import wicket.MarkupContainer;
 import wicket.Page;
 import wicket.PageParameters;
 import wicket.markup.html.basic.Label;
 import wicket.markup.html.link.BookmarkablePageLink;
 import wicket.markup.html.panel.Panel;
 import wicket.model.Model;
 
 /**
  * Represents an entry in a page navigation menu.
  * 
  * @author Eelco Hillenius
  */
 public class MenuItem extends DefaultMutableTreeNode
 {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Default panel for items.
 	 */
 	protected class ItemPanel extends Panel
 	{
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		/**
 		 * Construct.
 		 * 
 		 * @param parent
 		 *            The parent
 		 * @param id
 		 *            component id
 		 * @param row
 		 *            the row
 		 */
 		public ItemPanel(MarkupContainer parent, final String id, final MenuRow row)
 		{
 			super(parent, id);
 			final String label = getLabel();
 			final BookmarkablePageLink pageLink = new BookmarkablePageLink(this, "link",
 					getPageClass(), getPageParameters());
 			pageLink.setAutoEnable(false);
 			new Label(pageLink, "label", label);
 			add(new AttributeModifier("class", true, new Model<String>()
 			{
				/**
				 * 
				 */
 				private static final long serialVersionUID = 1L;
 
 				@Override
 				public String getObject()
 				{
 					return row.getRowStyle().getItemCSSClass(MenuItem.this, row);
 				}
 			}));
 		}
 	}
 
 	/** label of the menu item. */
 	private String label;
 
 	/** class of the page. */
 	private Class< ? extends Page> pageClass;
 
 	/** optional page parameters. */
 	private PageParameters pageParameters;
 
 	/** page classes that should be interpreted as being on the same path. */
 	private Set<Class< ? extends Page>> aliases = new HashSet<Class< ? extends Page>>();
 
 	/**
 	 * Construct.
 	 */
 	public MenuItem()
 	{
 		super();
 	}
 
 	/**
 	 * Construct.
 	 * 
 	 * @param label
 	 *            label of the menu item
 	 * @param pageClass
 	 *            class of the page
 	 * @param pageParameters
 	 *            optional page parameters
 	 */
 	public MenuItem(String label, Class< ? extends Page> pageClass, PageParameters pageParameters)
 	{
 		super();
 		this.label = label;
 		this.pageClass = pageClass;
 		this.pageParameters = pageParameters;
 	}
 
 	/**
 	 * Checks whether the given (current) request may use this item.
 	 * 
 	 * @return true if this item should be visible
 	 */
 	public boolean checkAccess()
 	{
 		return true;
 	}
 
 	/**
 	 * Whether the given page class is part of the selection.
 	 * 
 	 * @param pageClass
 	 *            the page class to check
 	 * @return whether the given page class is part of the selection
 	 */
 	public boolean isPartOfSelection(Class pageClass)
 	{
 		return ((this.pageClass != null && this.pageClass.equals(pageClass)) || aliases
 				.contains(pageClass));
 	}
 
 	/**
 	 * Gets the label of the menu item.
 	 * 
 	 * @return the label of the menu item
 	 */
 	public String getLabel()
 	{
 		return label;
 	}
 
 	/**
 	 * Sets the label of the menu item.
 	 * 
 	 * @param label
 	 *            the label of the menu item
 	 */
 	public void setLabel(String label)
 	{
 		this.label = label;
 	}
 
 	/**
 	 * Gets the class of the page.
 	 * 
 	 * @return the class of the page
 	 */
 	public Class< ? extends Page> getPageClass()
 	{
 		return pageClass;
 	}
 
 	/**
 	 * Sets the class of the page.
 	 * 
 	 * @param pageClass
 	 *            the class of the page
 	 */
 	public void setPageClass(Class< ? extends Page> pageClass)
 	{
 		this.pageClass = pageClass;
 	}
 
 	/**
 	 * Gets the optional page parameters.
 	 * 
 	 * @return the page parameters
 	 */
 	public PageParameters getPageParameters()
 	{
 		return pageParameters;
 	}
 
 	/**
 	 * Sets the page parameters.
 	 * 
 	 * @param pageParameters
 	 *            the page parameters
 	 */
 	public void setPageParameters(PageParameters pageParameters)
 	{
 		this.pageParameters = pageParameters;
 	}
 
 	/**
 	 * Adds an alias.
 	 * 
 	 * @param pageClass
 	 *            the alias page class to add
 	 * @return This
 	 */
 	public MenuItem addAlias(Class< ? extends Page> pageClass)
 	{
 		aliases.add(pageClass);
 		return this;
 	}
 
 	/**
 	 * Removes an alias.
 	 * 
 	 * @param pageClass
 	 *            the alias page class to remove
 	 * @return This
 	 */
 	public MenuItem removeAlias(Class pageClass)
 	{
 		aliases.remove(pageClass);
 		return this;
 	}
 
 	/**
 	 * @see javax.swing.tree.DefaultMutableTreeNode#add(javax.swing.tree.MutableTreeNode)
 	 */
 	@Override
 	public final void add(MutableTreeNode newChild)
 	{
 		check(newChild);
 		super.add(newChild);
 	}
 
 	/**
 	 * @see javax.swing.tree.DefaultMutableTreeNode#insert(javax.swing.tree.MutableTreeNode,
 	 *      int)
 	 */
 	@Override
 	public final void insert(MutableTreeNode newChild, int childIndex)
 	{
 		check(newChild);
 		super.insert(newChild, childIndex);
 	}
 
 	/**
 	 * @see javax.swing.tree.DefaultMutableTreeNode#setParent(javax.swing.tree.MutableTreeNode)
 	 */
 	@Override
 	public final void setParent(MutableTreeNode newParent)
 	{
 		check(newParent);
 		super.setParent(newParent);
 	}
 
 	/**
 	 * Gets a new panel to display the actual menu link. Override this when you
 	 * want something specific other than [link[label]].
 	 * 
 	 * @param parent
 	 *            The parent
 	 * @param panelId
 	 *            the id of the panel. MUST BE USED WHEN CONSTRUCTING THE PANEL
 	 * @param row
 	 *            the parent menu row
 	 * @return the panel instance
 	 */
 	public Panel newItemPanel(MarkupContainer parent, String panelId, MenuRow row)
 	{
 		return new ItemPanel(parent, panelId, row);
 	}
 
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString()
 	{
 		return "MenuItem{pageClass=" + getPageClass() + "}";
 	}
 
 	/**
 	 * Checks whether the given node is not null and of the correct type.
 	 * 
 	 * @param treeNode
 	 *            node to check
 	 */
 	private void check(TreeNode treeNode)
 	{
 		if (treeNode == null)
 		{
 			throw new NullPointerException("treeNode may not be null");
 		}
 		if (!(treeNode instanceof MenuItem))
 		{
 			throw new IllegalArgumentException("argument must be of type "
 					+ MenuItem.class.getName() + " (but is of type "
 					+ treeNode.getClass().getName() + ")");
 		}
 	}
 }
