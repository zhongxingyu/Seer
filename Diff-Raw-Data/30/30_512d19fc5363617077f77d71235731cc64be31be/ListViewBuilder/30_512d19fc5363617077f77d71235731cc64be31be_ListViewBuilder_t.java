 /*******************************************************************************
  * Copyright (c) 2013 Rudy D'hauwe @ Whizu
  * Licensed under the EUPL V.1.1
  *   
  * This Software is provided to You under the terms of the European 
  * Union Public License (the "EUPL") version 1.1 as published by the 
  * European Union. Any use of this Software, other than as authorized 
  * under this License is strictly prohibited (to the extent such use 
  * is covered by a right of the copyright holder of this Software).
  *
  * This Software is provided under the License on an "AS IS" basis and 
  * without warranties of any kind concerning the Software, including 
  * without limitation merchantability, fitness for a particular purpose, 
  * absence of defects or errors, accuracy, and non-infringement of 
  * intellectual property rights other than copyright. This disclaimer 
  * of warranty is an essential part of the License and a condition for 
  * the grant of any rights to this Software.
  *   
  * For more  details, see <http://joinup.ec.europa.eu/software/page/eupl>.
  *
  * Contributors:
  *     2013 - Rudy D'hauwe @ Whizu - initial API and implementation
  *******************************************************************************/
 package org.whizu.jquery.mobile;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import org.whizu.dom.Content;
 import org.whizu.dom.ContentBuilder;
 import org.whizu.dom.ContentList;
 import org.whizu.dom.Element;
 import org.whizu.html.Html;
 import org.whizu.jquery.EventHandler;
 import org.whizu.jquery.OnItemClickListener;
 import org.whizu.jquery.RequestContext;
 import org.whizu.proxy.ProxyBuilder;
 import org.whizu.util.Objects;
 import org.whizu.value.ValueList;
 import org.whizu.value.ValueObject;
 import org.whizu.widget.BuildOnDemand;
 import org.whizu.widget.BuildSupport;
 
 /**
  * A listview is coded as a simple unordered list containing linked list items
  * with a data-role="listview" attribute. jQuery Mobile will apply all the
  * necessary styles to transform the list into a mobile-friendly listview with
  * right arrow indicator that fills the full width of the browser window. When
  * you tap on the list item, the framework will trigger a click on the first
  * link inside the list item, issue an Ajax request for the URL in the link,
  * create the new page in the DOM, then kick off a page transition.
  * 
  * 
  * @author Rudy D'hauwe
  */
 public class ListViewBuilder extends ProxyBuilder<ListView, ListViewBuilder.Build> {
 
 	public static ListViewBuilder create() {
 		ListViewBuilder builder = new ListViewBuilder();
 		builder.proxy_ = new ListViewProxy(builder.build_);
 		return builder;
 	}
 
	private ValueList<? extends ValueObject> valueList_;
 
	public static <T extends ValueObject> ListViewBuilder createWith(ValueList<T> list) {
 		final ListViewBuilder builder = create();
 		builder.valueList_ = list;
 
 		for (ValueObject vo : list) {
 			builder.addItem(vo);
 		}
 
 		list.addPropertyChangeListener(new PropertyChangeListener() {
 
 			@Override
 			public void propertyChange(PropertyChangeEvent evt) {
 				ValueObject vo = Objects.cast(evt.getNewValue());
 				builder.proxy_.addItem(vo);
 			}
 		});
 		return builder;
 	}
 
 	private ListView proxy_;
 
 	private void addItem(ValueObject vo) {
 		build_.addItem(vo);
 	}
 
 	@Override
 	protected Build createPrototype() {
 		return new Build();
 	}
 
 	@Override
 	protected ListView createProxy(Build build) {
 		return proxy_;
 	}
 
 	public <T extends ValueObject> ListViewBuilder onItemClick(OnItemClickListener onItemClickListener) {
 		build_.onItemClickListener_ = onItemClickListener;
 		return this;
 	}
 
 	/**
 	 * A listview can be configured to automatically generate dividers for its
 	 * items by adding a data-autodividers="true" attribute to any listview. By
 	 * default, the text used to create dividers is the uppercased first letter
 	 * of the item's text. Alternatively you can specify divider text by setting
 	 * the autodividersSelector option on the listview programmatically. This
 	 * feature is designed to work seamlessly with the filter.
 	 * 
 	 * @throws UnsupportedOperationException
 	 *             public void setAutodividers(boolean autodividers) { throw new
 	 *             UnsupportedOperationException(); }
 	 */
 
 	/**
 	 * Adds a search filter bar to the listview.
 	 * 
 	 * @throws UnsupportedOperationException
 	 *             public void setFilter(boolean filter) { throw new
 	 *             UnsupportedOperationException(); }
 	 */
 
 	/**
 	 * The filter reveal feature makes is easy to build a simple autocomplete
 	 * with local data. When a filterable list has the filter reveal attribute,
 	 * it will auto-hide all the list items when the search field is blank.
 	 * 
 	 * @throws UnsupportedOperationException
 	 *             public void setFilterReveal(boolean filterReveal) { throw new
 	 *             UnsupportedOperationException(); }
 	 */
 
 	/**
 	 * Adding the inset attribute to the list, applies the inset appearance
 	 * which is useful for mixing a listview with other content on a page.
 	 * 
 	 * @throws UnsupportedOperationException
 	 *             public void setInset(boolean inset) { throw new
 	 *             UnsupportedOperationException(); }
 	 */
 
 	/***************************************************************************
 	 * The <code>ListView</code> that is being built.
 	 */
 	final class Build extends BuildSupport implements ListView {
 
 		private OnItemClickListener<ValueObject> onItemClickListener_;
 
 		int id = 0;
 
 		private ContentList contents_ = new ContentList();
 
 		@Override
 		public void addItem(Content item) {
 			Element a = Html.a().href("#").attr("data-id", ("" + id++)).add(item);
 			Element li = a.wrap("li");
 			contents_.add(li);
 		}
 
 		@Override
 		public void addItem(ContentBuilder builder) {
 			Content item = new BuildOnDemand(builder);
 			addItem(item);
 		}
 
 		@Override
 		public Content build() {
 			Element element = Html.ul(this).decorate(DataRole.LISTVIEW, this).add(contents_);
 
 			if (onItemClickListener_ != null) {
 				EventHandler eh = new EventHandler() {
 					String id_ = session().next();
 					@Override
 					public String id() {
 						return id_;
 					}
 
 					@Override
 					public void handleEvent() {
 						int index = Integer.parseInt(RequestContext.getRequest().getParameter("data-index"));
 						System.out.println("DATA-INDEX = " + index);
 						System.out.println("LIST-SIZE = " + valueList_.size());
 						ValueObject obj = valueList_.get(index-1);
 						onItemClickListener_.click(obj);
 					}};
 				session().addClickListener(eh);
 				//jQuery(this).find("li a").onClickItem(eh);
 				String thiz = jQuery(this).toJavaScript();
 				jQuery("$(document)").on("click", "#" + id() + " li a", eh);
 			}
 
 			return element;
 		}
 	}
 }
