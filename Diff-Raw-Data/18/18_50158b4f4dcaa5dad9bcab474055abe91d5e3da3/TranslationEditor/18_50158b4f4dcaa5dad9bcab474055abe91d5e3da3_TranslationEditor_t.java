 package org.dhamma.translations.client.editors;
 
 import org.dhamma.translations.client.models.Translation;
import org.dhamma.translations.client.models.User;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.editor.client.Editor;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DateLabel;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.NumberLabel;
import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 import de.mkristian.gwt.rails.editors.UserLabel;
 
 public class TranslationEditor extends Composite implements Editor<Translation>{
     
     interface Binder extends UiBinder<Widget, TranslationEditor> {}
 
     private static final Binder BINDER = GWT.create(Binder.class);
     
     @Ignore @UiField FlowPanel signature;
 
     @UiField public NumberLabel<Integer> id;
     @UiField DateLabel createdAt;
     @UiField DateLabel updatedAt;
    @UiField UserLabel<User> modifiedBy;
 
     @UiField TextBox text;
 
     public TranslationEditor() {
         initWidget(BINDER.createAndBindUi(this));
     }
 
     public void resetSignature() {
         this.signature.setVisible(id.getValue() != null && id.getValue() > 0);
     }
 
     public void setEnabled(boolean enabled) {
         resetSignature();
         this.text.setEnabled(enabled);
     }
 }
