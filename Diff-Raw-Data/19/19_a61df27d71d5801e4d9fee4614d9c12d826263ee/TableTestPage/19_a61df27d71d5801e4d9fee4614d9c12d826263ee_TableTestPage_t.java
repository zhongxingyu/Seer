 package test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.detailstable.AjaxFallbackDefaultDetailsTable;
import org.apache.wicket.extensions.markup.html.repeater.data.detailstable.DefaultDetailsTable;
 import org.apache.wicket.extensions.markup.html.repeater.data.detailstable.TogglingDetailsColumn;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.panel.Fragment;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 
 
 
 public class TableTestPage extends WebPage {
 
     private static final long serialVersionUID = 1L;
 
     public TableTestPage() {
         final Form<User> form = new Form<User>("form");
         
         add(form);
 
         final TestDataProvider tdp = new TestDataProvider();
         final List<IColumn<User, String>> columns = new ArrayList<IColumn<User, String>>();
         final DetailsColumn detailsColumn = new DetailsColumn(form);
         
         columns.add(detailsColumn);
         columns.add(new PropertyColumn<User, String>(Model.of("First Name"), "firstName", "firstName"));
         columns.add(new PropertyColumn<User, String>(Model.of("Last Name"), "lastName", "lastName"));
 
        form.add(new DefaultDetailsTable<User, String>("table", columns, tdp, 2));
        form.add(new AjaxFallbackDefaultDetailsTable<User, String>("ajax-table", columns, tdp, 2));
     }
     
     private class DetailsColumn extends TogglingDetailsColumn<User, String> {
 
         private static final long serialVersionUID = 1L;
         
         private MarkupContainer parent;
         
         public DetailsColumn(MarkupContainer parent) {
             this.parent = parent;
         }
         
         public Component getDetailsComponent(String id, IModel<User> model) {
             Fragment detailsFragment = new Fragment(id, "details-fragment", parent);
             
             User user = model.getObject();
             
             Label label = new Label("details", user.getFirstName() + " " + user.getLastName());
 
             detailsFragment.add(label);
             return detailsFragment;
         }
         
         public Component getCellComponent(String id, IModel<User> model, final Component detailsComponent) {
             Fragment buttonFragment = new Fragment(id, "button-fragment", parent);
 
             AjaxButton button = new AjaxButton("button") {
 
                 private static final long serialVersionUID = 1L;
 
                 @Override
                 protected void onError(AjaxRequestTarget target, Form<?> form) {
                     System.out.println("ERROR");
                 }
 
                 @Override
                 protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                     target.add(detailsComponent);
                     detailsComponent.setVisible(detailsComponent.isVisible() ? false : true);
                 }
 
             };
 
             button.setDefaultFormProcessing(false);
             buttonFragment.add(button);
             
             return buttonFragment;
         }
         
     }
 }
