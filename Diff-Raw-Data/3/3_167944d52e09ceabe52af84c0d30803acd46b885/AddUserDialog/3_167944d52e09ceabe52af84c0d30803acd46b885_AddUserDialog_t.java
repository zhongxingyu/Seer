 package org.cotrix.web.users.client.codelists;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.cotrix.web.common.client.util.FilteredCachedDataProvider;
 import org.cotrix.web.common.client.util.FilteredCachedDataProvider.Filter;
 import org.cotrix.web.common.client.widgets.SearchBox;
 import org.cotrix.web.users.client.resources.UsersResources;
 import org.cotrix.web.users.shared.UIUserDetails;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.cell.client.AbstractCell;
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.core.client.Scheduler.ScheduledCommand;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.shared.EventHandler;
 import com.google.gwt.event.shared.GwtEvent;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.resources.client.ImageResource;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.cellview.client.CellList;
 import com.google.gwt.user.client.ui.AbstractImagePrototype;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.google.inject.Inject;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class AddUserDialog extends PopupPanel {
 
 	interface Binder extends UiBinder<Widget, AddUserDialog> {}
 
 	static class UserDetailsCell extends AbstractCell<UIUserDetails> {
 
 		/**
 		 * The html of the image used for contacts.
 		 */
 		private final String imageHtml;
 
 		public UserDetailsCell(ImageResource image) {
 			this.imageHtml = AbstractImagePrototype.create(image).getHTML();
 		}
 
 		@Override
 		public void render(Context context, UIUserDetails value, SafeHtmlBuilder sb) {
 			// Value can be null, so do a null check..
 			if (value == null) {
 				return;
 			}
 
 			sb.appendHtmlConstant("<table>");
 
 			// Add the contact image.
 			sb.appendHtmlConstant("<tr><td rowspan='2' style=\"vertical-align: middle;padding-right:5px;\">");
 			sb.appendHtmlConstant(imageHtml);
 			sb.appendHtmlConstant("</td>");
 
 			// Add the name and email.
 			sb.appendHtmlConstant("<td>");
 			sb.appendEscaped(value.getFullName());
 			sb.appendHtmlConstant("</td></tr><tr><td>");
 			sb.appendEscaped(value.getEmail());
 			sb.appendHtmlConstant("</td></tr></table>");
 		}
 	}
 	
 	protected interface UsersListResources extends CellList.Resources {
 
 	    /**
 	     * The styles used in this widget.
 	     */
 	    @Source("UsersList.css")
 	    UsersListStyle cellListStyle();
 	}
 	
 	protected interface UsersListStyle extends CellList.Style { }
 
 	@UiField SearchBox filterTextBox;
 	@UiField Button addUser;
 	@UiField(provided=true) CellList<UIUserDetails> usersList;
 
 	@Inject
 	protected UsersDetailsDataProvider dataProvider;
 
 	protected SingleSelectionModel<UIUserDetails> selectionModel = new SingleSelectionModel<UIUserDetails>();
 	protected ByIdFilter byIdFilter = new ByIdFilter();
 	protected ByNameFilter byNameFilter = new ByNameFilter();
 
 	@Inject
 	@SuppressWarnings("unchecked")
 	protected void init(Binder binder) {
 		setWidget(binder.createAndBindUi(this));
 		setAutoHideEnabled(true);	
 		dataProvider.setFilters(new FilteredCachedDataProvider.AndFilter<UIUserDetails>(byIdFilter, byNameFilter));
 	}
 	
 	@UiHandler("addUser")
 	protected void onAddUserClick(ClickEvent event) {
 		fireEvent(new AddUserEvent(selectionModel.getSelectedObject()));
 		hide();
 	}
 	
 	public void clean() {
 		filterTextBox.clear();
 		selectionModel.clear();
 	}
 	
 	/** 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void show() {
 		super.show();
 		clean();
 		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
 
 		    @Override
 		    public void execute() {
 		    	filterTextBox.setFocus(true);
 		    }
 		});
 	}
 
 	@Inject
 	protected void getCellList(UsersListResources resources) {
 		UserDetailsCell cell = new UserDetailsCell(UsersResources.INSTANCE.listUser());
 		usersList = new CellList<UIUserDetails>(cell, resources);
 		
 		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 			
 			@Override
 			public void onSelectionChange(SelectionChangeEvent event) {
 				addUser.setEnabled(true);
 			}
 		});
 
 		usersList.setSelectionModel(selectionModel);
 		dataProvider.addDataDisplay(usersList);
 	}
 	
 	@UiHandler("filterTextBox")
 	protected void onValueChange(ValueChangeEvent<String> event) {
 		Log.trace("onValueChange value: "+event.getValue());
 		updateFilter(event.getValue());
 	}
 	
 
 	protected void updateFilter(String filter)
 	{
 		byNameFilter.setName(!filter.isEmpty()?filter:null);
 		dataProvider.applyFilters();
		dataProvider.refresh();
 	}
 	
 	public void setIds(Set<String> ids) {
 		byIdFilter.setIds(ids);
 		dataProvider.applyFilters();
		dataProvider.refresh();
 	}
 	
 	protected class ByNameFilter implements Filter<UIUserDetails> {
 
 		protected String name;
 
 		/**
 		 * @param name
 		 */
 		public ByNameFilter() {
 		}
 
 		/**
 		 * @param name the name to set
 		 */
 		public void setName(String name) {
 			this.name = (name!=null)?name.toUpperCase():null;
 			Log.trace("ByNameFilter name: "+this.name);
 		}
 
 		@Override
 		public boolean accept(UIUserDetails data) {
 			return name==null || data.getFullName().toUpperCase().contains(name);
 		}
 	}
 	
 	protected class ByIdFilter implements Filter<UIUserDetails> {
 
 		protected Set<String> ids;
 
 		/**
 		 * @param name
 		 */
 		public ByIdFilter() {
 			ids = new HashSet<String>();
 		}
 
 		/**
 		 * @param ids the ids to set
 		 */
 		public void setIds(Set<String> ids) {
 			this.ids = ids;
 		}
 
 		@Override
 		public boolean accept(UIUserDetails data) {
 			return !ids.contains(data.getId());
 		}
 	}
 	
 	public HandlerRegistration addAddUserHandler(AddUserHandler handler)
 	{
 		return addHandler(handler, AddUserEvent.getType());
 	}
 	
 	public interface AddUserHandler extends EventHandler {
 		void onAddUser(AddUserEvent event);
 	}
 
 	public static class AddUserEvent extends GwtEvent<AddUserHandler> {
 
 		public static Type<AddUserHandler> TYPE = new Type<AddUserHandler>();
 		
 		protected UIUserDetails user;
 
 		public AddUserEvent(UIUserDetails user) {
 			this.user = user;
 		}
 
 		public UIUserDetails getUser() {
 			return user;
 		}
 
 		@Override
 		protected void dispatch(AddUserHandler handler) {
 			handler.onAddUser(this);
 		}
 
 		@Override
 		public Type<AddUserHandler> getAssociatedType() {
 			return TYPE;
 		}
 
 		public static Type<AddUserHandler> getType() {
 			return TYPE;
 		}
 	}
 }
