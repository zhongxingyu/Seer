 /*
  * SafeOnline project.
  *
  * Copyright 2006-2009 Lin.k N.V. All rights reserved.
  * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
  */
 package net.link.safeonline.attribute.provider.input;
 
 import java.io.Serializable;
 import java.util.Date;
 import net.link.safeonline.attribute.provider.AttributeCore;
 import net.link.util.wicket.component.feedback.ErrorComponentFeedbackLabel;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.wicket.behavior.SimpleAttributeModifier;
 import org.apache.wicket.extensions.markup.html.form.DateTextField;
 import org.apache.wicket.extensions.yui.calendar.DatePicker;
 import org.apache.wicket.markup.html.form.*;
 import org.apache.wicket.model.IModel;
 
 
 public class DefaultAttributeInputPanel extends AttributeInputPanel {
 
     private static final Log LOG = LogFactory.getLog( DefaultAttributeInputPanel.class );
 
     public static final String VALUE_ID          = "value";
     public static final String VALUE_FEEDBACK_ID = "value_feedback";
 
     public static final String BOOLEAN_ID = "boolean";
 
     FormComponent<?> component;
     CheckBox         checkBox;
 
     public DefaultAttributeInputPanel(String id, AttributeCore attribute) {
 
         super( id, attribute );
 
         checkBox = createBooleanField();
         checkBox.setOutputMarkupId( true );
         checkBox.setEnabled( attribute.getAttributeType().isUserEditable() );
         checkBox.setVisible( false );
         add( checkBox );
 
         if (attribute.isUnavailable()) {
 
             component = new TextField<String>( VALUE_ID, localize( "unavailable" ) );
             component.setEnabled( false );
             component.add( new SimpleAttributeModifier( "class", "error" ) );
         } else {
             switch (attribute.getAttributeType().getType()) {
 
                 case STRING:
                     component = createTextField( String.class );
                     break;
                 case BOOLEAN:
                     // dummy
                     component = createTextField( String.class );
                     component.setVisible( false );
                     checkBox.setVisible( true );
                     break;
                 case INTEGER:
                     component = createTextField( Integer.class );
                     break;
                 case DOUBLE:
                     component = createTextField( Double.class );
                     break;
                 case DATE:
                     component = createDateField();
                     break;
                 case COMPOUNDED:
                     throw new RuntimeException( "No support for compounds." );
             }
 
             component.setEnabled( attribute.getAttributeType().isUserEditable() );
         }
 
         component.setOutputMarkupId( true );
         add( component );
 
         ErrorComponentFeedbackLabel feedback = new ErrorComponentFeedbackLabel( VALUE_FEEDBACK_ID, component );
         feedback.setOutputMarkupId( true );
         add( feedback );
     }
 
     @Override
     public void onMissingAttribute() {
 
         // do nothing
     }
 
     private <T extends Serializable> TextField<T> createTextField(Class<T> clazz) {
 
         return new TextField<T>( VALUE_ID, new IModel<T>() {
 
             @SuppressWarnings("unchecked")
             public T getObject() {
 
                 return (T) attribute.getValue();
             }
 
             public void setObject(final T object) {
 
                 if (object instanceof String && null != object) {
                     String stringObject = (String) object;
                    if (stringObject.isEmpty()) {
                         attribute.setValue( null );
                     } else {
                         attribute.setValue( stringObject );
                     }
                 } else {
                     attribute.setValue( object );
                 }
             }
 
             public void detach() {
 
             }
         }, clazz ) {
 
             @Override
             public boolean isInputNullable() {
 
                 return true;
             }
         };
     }
 
     private CheckBox createBooleanField() {
 
         return new CheckBox( BOOLEAN_ID, new IModel<Boolean>() {
 
             public Boolean getObject() {
 
                 return (Boolean) attribute.getValue();
             }
 
             public void setObject(final Boolean object) {
 
                 attribute.setValue( object );
             }
 
             public void detach() {
 
             }
         } );
     }
 
     private DateTextField createDateField() {
 
         DateTextField field = new DateTextField( VALUE_ID, new IModel<Date>() {
 
             public Date getObject() {
 
                 return (Date) attribute.getValue();
             }
 
             public void setObject(final Date object) {
 
                 attribute.setValue( object );
             }
 
             public void detach() {
 
             }
         }, "dd/MM/yyyy" );
         field.add( new DatePicker() );
         field.add( new SimpleAttributeModifier( "class", "label date_label" ) );
         return field;
     }
 }
