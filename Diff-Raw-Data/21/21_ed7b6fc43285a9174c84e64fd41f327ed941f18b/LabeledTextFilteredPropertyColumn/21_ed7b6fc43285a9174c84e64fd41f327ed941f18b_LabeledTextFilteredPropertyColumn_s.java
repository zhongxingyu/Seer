 package de.elatexam.editor.pages.subtaskdefs;
 
 import net.databinder.components.AjaxOnKeyPausedSubmitter;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.TextFilteredPropertyColumn;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.model.IModel;
 
 import de.elatexam.editor.pages.filter.LabeledTextFilter;
 
 /**
  * Just like {@link TextFilteredPropertyColumn} but with a prepended label.
  *
  * @author Steffen Dienst
  *
  * @param <T>
  */
 public class LabeledTextFilteredPropertyColumn<T> extends TextFilteredPropertyColumn<T, String> {
   private IModel<String> label;
 
   /**
    * @param displayModel
    * @param sortProperty
    * @param propertyExpression
    */
   public LabeledTextFilteredPropertyColumn(IModel<String> displayModel, IModel<String> label, String sortProperty,
       String propertyExpression) {
     super(displayModel, sortProperty, propertyExpression);
     this.label = label;
   }
 
   @Override
   public void populateItem(final Item<ICellPopulator<T>> item, final String componentId, final IModel<T> rowModel) {
     // add a label that renders it's html contents
     item.add(new Label(componentId, createLabelModel(rowModel)).setEscapeModelStrings(false));
   }
 
   @Override
   public Component getFilter(String componentId, final FilterForm form) {
     LabeledTextFilter filter = new LabeledTextFilter<String>(componentId, label, getFilterModel(form), form);
     filter.getFilter().add(new AjaxOnKeyPausedSubmitter() {
       @Override
       protected void onSubmit(AjaxRequestTarget target) {
        target.addComponent(form);
       }
     });
     return filter;
   }
 }
