 package com.jtbdevelopment.e_eye_o.entities.annotations;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 /**
  * Date: 6/2/13
  * Time: 8:20 PM
  */
 @Retention(RetentionPolicy.RUNTIME)
 @Target({ElementType.METHOD})
 //  TODO  - put tests around usage
 public @interface IdObjectFieldSettings {
     /**
      * NONE - Not editable
      * CONTROLLED - Only via specialized functions
      * ADMIN - Only Admin users may affect the field
      * USER - user editable
      */
     public enum EditableBy {
         NONE, CONTROLLED, ADMIN, USER
     }
 
     public enum DisplayFieldType {
         DEFAULT, TEXT, PASSWORD, MULTI_LINE_TEXT, SINGLE_SELECT_LIST, MULTI_SELECT_PICKER, DATE_TIME, LOCAL_DATE_TIME, CHECKBOX, REVERSE_CHECKBOX, CUSTOM
     }
 
     public enum DisplayAlignment {
         DEFAULT, LEFT, CENTER, RIGHT
     }
 
     boolean viewable() default true;
 
     EditableBy editableBy() default EditableBy.USER;
 
     int width() default 10;  //  Assumed to be in EM unit
 
     int height() default 1;  //  Dependent on display type
 
     String label() default "";
 
     DisplayFieldType fieldType() default DisplayFieldType.DEFAULT;
 
     DisplayAlignment alignment() default DisplayAlignment.DEFAULT;

    String defaultValueSetting() default "";
 }
