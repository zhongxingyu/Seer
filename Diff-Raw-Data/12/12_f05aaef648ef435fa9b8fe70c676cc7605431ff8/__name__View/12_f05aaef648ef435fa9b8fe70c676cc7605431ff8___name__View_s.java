 #set( $symbol_pound = '#' )
 #set( $symbol_dollar = '$' )
 #set( $symbol_escape = '\' )
 package ${package};
 
 import javax.inject.Inject;
 
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.gwtplatform.mvp.client.ViewImpl;
 import com.gwtplatform.mvp.client.ViewWithUiHandlers;
 
 // TODO if then [ViewImpl|ViewWithUiHandlers<${name}UiHandlers>]
 public class ${name}View extends ViewWithUiHandlers<${name}UiHandlers> implements ${name}Presenter.MyView {
    public interface Binder extends UiBinder<Widget, ${name}View> {
     }
 
     @UiField
     SimplePanel main;
 
     @Inject
    public ApplicationView(Binder uiBinder) {
         initWidget(uiBinder.createAndBindUi(this));
     }
 
     @Override
     public void setInSlot(Object slot, IsWidget content) {
         if (slot == ${name}Presenter.TYPE_SetMainContent) {
             main.setWidget(content);
         } else {
             super.setInSlot(slot, content);
         }
     }
 }
