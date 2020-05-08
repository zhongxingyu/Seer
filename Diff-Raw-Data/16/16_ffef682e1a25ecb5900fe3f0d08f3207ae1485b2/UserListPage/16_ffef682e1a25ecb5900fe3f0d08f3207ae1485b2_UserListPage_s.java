 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.sample.wicket.view;
 
 import java.util.Iterator;
 
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.markup.repeater.data.DataView;
 import org.apache.wicket.markup.repeater.data.IDataProvider;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import com.globant.katari.hibernate.coreuser.SecurityUtils;
 
 import com.globant.katari.user.application.DeleteUserCommand;
 import com.globant.katari.user.application.UserFilterCommand;
 import com.globant.katari.user.domain.User;
 import com.globant.katari.user.domain.filter.ContainsFilter;
 
 /** Paginates through all the users.
  *
  * This is a wicket page that shows a paginated list of users.
  */
 public class UserListPage extends WebPage {
 
   /** The number of elements per page.
    */
   private static final int DEFAULT_PAGE_SIZE = 5;
 
   /** The command that provides the list of users.
    *
    * Injected by spring, never null.
    */
  @SpringBean(name = "filterCommand")
   private UserFilterCommand filterCommand;
 
   /** The command that deletes a user.
    *
    * Injected by spring, never null.
    */
   @SpringBean
   private DeleteUserCommand deleteCommand;
 
   /** Constructor.
    */
   public UserListPage() {
 
     DataView<User> dataView;
     dataView = new DataView<User>("users", new UsersDataProvider()) {
       private static final long serialVersionUID = 7992666042888348658L;
 
       @SuppressWarnings("serial")
       public void populateItem(final Item<User> listItem) {
         final User user = listItem.getModelObject();
         listItem.setModel(new CompoundPropertyModel<User>(user));
         listItem.add(new Label("name"));
         listItem.add(new Label("email"));
         listItem.add(new UserPage.Link("edit", user).add(new Label("id")));
         listItem.add(new Link<User>("delete") {
 
           @Override
           public void onClick() {
             deleteCommand.setUserId(user.getId());
             deleteCommand.execute();
           }
 
           @Override
           protected CharSequence getOnClickScript(final CharSequence url) {
             return "if (!confirm('Are you sure you want to delete user "
               + user.getName() + "?')) return false;";
           }
 
           @Override
           public boolean isEnabled() {
             return user.getId() != SecurityUtils.getCurrentUser().getId();
           }
 
         });
       }
     };
     dataView.setItemsPerPage(DEFAULT_PAGE_SIZE);
     add(dataView);
     add(new PagingNavigator("pager", dataView));
   }
 
   /** Addapts the DataView to the the User list.
    */
   private class UsersDataProvider implements IDataProvider<User> {
 
     /** Serialization version.
      */
     private static final long serialVersionUID = 1L;
 
     /** Cache for the total number of users to show.
      */
     private int size = -1;
 
     /** {@inheritDoc}.
      */
     public Iterator<User> iterator(final int first, final int count) {
       int currentPage = 0;
       if (count != 0) {
         currentPage = first / count;
       }
       filterCommand.getPaging().setPageNumber(currentPage);
       filterCommand.getPaging().setPageSize(count);
       return filterCommand.execute().iterator();
     }
 
     /** {@inheritDoc}.
      */
     @SuppressWarnings("serial")
     public IModel<User> model(final User user) {
       return new LoadableDetachableModel<User>() {
         @Override
         protected User load() {
           return user;
         }
       };
     }
 
     /** {@inheritDoc}.
      */
     public int size() {
       if (size == -1) {
         filterCommand.setContainsFilter(new ContainsFilter());
         filterCommand.getPaging().setPageSize(0);
         size = filterCommand.execute().size();
       }
       return size;
     }
 
     /** {@inheritDoc}
      */
     public void detach() {
     }
   }
 }
 
