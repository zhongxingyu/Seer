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
 package wicket.contrib.phonebook.web.page;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import wicket.contrib.phonebook.Contact;
 import wicket.contrib.phonebook.ContactDao;
 import wicket.contrib.phonebook.QueryParam;
 import wicket.contrib.phonebook.web.DetachableContactModel;
 import wicket.contrib.phonebook.web.PhonebookApplication;
import wicket.extensions.markup.html.repeater.data.sort.SortParam;
import wicket.extensions.markup.html.repeater.data.sort.SortableDataProvider;
import wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import wicket.extensions.markup.html.repeater.data.table.DataTable;
import wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
 import wicket.markup.html.link.Link;
 import wicket.markup.html.list.ListItem;
 import wicket.markup.html.panel.Panel;
 import wicket.model.IModel;
 import wicket.model.Model;
 
 /**
  * Display a Pageable List of Contacts.
  *
  * @author igor
  */
 public class ListContactsPage extends BasePage {
 	/**
 	 * Constructor. Having this constructor public means that the page is
 	 * 'bookmarkable' and hence can be called/ created from anywhere.
 	 */
     	public ListContactsPage() {
 
 		add(new Link("createLink") {
 			/**
 			 * Go to the Edit page when the link is clicked, passing an empty Contact details
 			 */
 			public void onClick() {
 				setResponsePage(new EditContactPage(getPage(), 0));
 			}
 
 		});
 
 		List columns = new ArrayList();
 
 		/*
 		 * First column is a composite column, created by extending
 		 * AbstractColumn in order to add a UserActionsPanel as the
 		 * cell contents.
 		 */
 		columns.add(new AbstractColumn(new Model("Actions")) {
 
 			public void populateItem(ListItem cellItem, String componentId, IModel model) {
 				final Contact contact = (Contact) model.getObject(cellItem);
 				cellItem.add(new UserActionsPanel(componentId, contact));
 			}
 
 		});
 
 		/*
 		 * Use PropertyColumn to provide the 'normal' columns.
 		 */
 		columns.add(new PropertyColumn(new Model("First Name"), "firstname", "firstname"));
 		columns.add(new PropertyColumn(new Model("Last Name"), "lastname", "lastname"));
 		columns.add(new PropertyColumn(new Model("Phone Number"), "phone", "phone"));
 		columns.add(new PropertyColumn(new Model("Email"), "email", "email"));
 
 		// set up data provider and default sort for the data table
 		ContactsDataProvider dataProvider = new ContactsDataProvider();
 		dataProvider.setSort("firstname", true);
 
 		DataTable users = new DataTable("users", columns, dataProvider, 10);
 
 		add(users);
 	}
 
 	/**
 	 * 
 	 * @author igor
 	 * 
 	 * note static is important here because dataprovider is the model of the
 	 * dataview so having a reference to the page will cause an out of memory
 	 * error eventually
 	 */
 	private static class ContactsDataProvider extends SortableDataProvider {
 		private ContactDao getDao() {
 			return PhonebookApplication.getInstance().getContactDao();
 		}
 
 		/**
 		 * Gets an iterator for the subset of total data.
 		 *
 		 * @param first first row of data
 		 * @param count minumum number of elements to retrieve
 		 * @return iterator capable of iterating over {first, first+count} items
 		 */
 		public Iterator iterator(int first, int count) {
 			SortParam sp = getSort();
 			QueryParam qp = null;
 			if (sp != null) {
 				qp = new QueryParam(first, count, sp.getProperty(), sp
 						.isAscending());
 			} else {
 				qp = new QueryParam(first, count);
 			}
 			return getDao().find(qp);
 		}
 
 		/**
 		 * Gets total number of items in the collection.
 		 *
 		 * @return total item count
 		 */
 		public int size() {
 			return getDao().count();
 		}
 
 		/**
 		 * Converts the object in the collection to its model representation.
 		 * A good place to wrap the object in a detachable model.
 		 *
 		 * @param object The object that needs to be wrapped
 		 * @return The model representation of the object
 		 */
 		public IModel model(Object object) {
 			return new DetachableContactModel((Contact) object);
 		}
 	};
 
 	/**
 	 * Provides a composite User Actions panel for the Actions column.
 	 *
 	 * @author igor
 	 */
 	private static class UserActionsPanel extends Panel {
 
 		public UserActionsPanel(String id, Contact contact) {
 			super(id);
 			final long contactId = contact.getId();
 
 			add(new Link("editLink") {
 				/**
 				 * Go to the Edit page, passing this page and the id of the Contact involved.
 				 */
 				public void onClick() {
 					setResponsePage(new EditContactPage(getPage(), contactId));
 				}
 
 			});
 
 			add(new Link("deleteLink") {
 				/**
 				 * Go to the Delete page, passing this page and the id of the Contact involved. 
 				 */
 				public void onClick() {
 					setResponsePage(new DeleteContactPage(getPage(), contactId));
 				}
 
 			});
 
 		}
 
 	}
 }
