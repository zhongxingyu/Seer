 /*---
    Copyright 2007 Visual Systems Corporation.
    http://www.vscorp.com
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
         http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 ---*/
 
 package com.googlecode.wicketwebbeans.annotations;
 
import static java.lang.annotation.ElementType.FIELD;
 import static java.lang.annotation.ElementType.METHOD;
 import static java.lang.annotation.ElementType.TYPE;
 import static java.lang.annotation.RetentionPolicy.RUNTIME;
 
 import java.lang.annotation.Documented;
 import java.lang.annotation.Retention;
 import java.lang.annotation.Target;
 
 import com.googlecode.wicketwebbeans.containers.BeanForm;
 import com.googlecode.wicketwebbeans.containers.BeanGridPanel;
 import com.googlecode.wicketwebbeans.fields.Field;
 
 
 
 /**
  * Property annotation. May be specified within {@link Bean}, {@link Tab}, or on a property method. 
  * <p>
  * 
  * @author Dan Syrstad
  */
 @Documented
@Target({TYPE, METHOD, FIELD}) @Retention(RUNTIME)
 public @interface Property {
     public static final String EMPTY = "EMPTY";
     
     /** The property name. You may also specify "-propertyName" to remove a property. You may specify "action.actionMethodName" to 
      * include an action. Also, Property.EMPTY can be used to specify an empty cell. */
     String name() default "";
 
     /** Tells WWB whether the field is required. Note that you must call {@link BeanForm#validateRequired()} in your action method to have the validation occur. 
      * This is automatically set to true if you are using JPA and the @Column annotation has nullable set to false. If you are using the JDO @Column 
      * annotation, this is set to true if @Column.allowsNull is "false". */
     boolean required() default false;
 
     /** The maximum input length for a field. This is automatically set if you are using the JPA or JDO @Column.length annotation. */
     int maxLength() default 0;
     
     /** The default string value for a field. This is automatically set if you are using the JDO @Column.defaultValue annotation. */
     String defaultValue() default "";
 
     /** Specifies a {@link Field} class name to use for this property. This overrides the Field type defined for the property type in !ComponentRegistry. Note that if the Field type is already defined for a different class in !ComponentRegistry, you can just specify the base class name, not the package which is optional. Otherwise, you can explicitly specify the package name.
      */
     Class<? extends Field> fieldType() default Field.class;
 
     /** For Collections, elementType is the type of the elements contained within the collection. By default, WWB automatically deduces the element type from the collection if it is not null and not empty. In this case, elementType is detected from the first element of the collection. 
      * Otherwise, you can use this parameter to explicitly specify the type. */
     Class<?> elementType() default Object.class;
     
     /** The number of rows to display for TextAreaField and BeanTableFields, etc. */
     int rows() default 0;
     
     /** The number of columns to display for a TextAreaField. */
     int columns() default 0;
 
     /** Tell WWB whether this element is view-only or editable. This is automatically set to true if you are using JPA and the @Column annotation has 
      * both insertable and updatable set to false.
      * Note that this is defined as an array so WWB can tell if the attribute was explicitly set. However, you should just say viewOnly = true/false 
      * and not use the array syntax. */
     boolean[] viewOnly() default {};
 
     // --- Common to Action and Property
     /** The label to display for this element. */
     String label() default "";
 
     /** An image file name relative to the component. Specifies that the property's field should have the specified image rather than a text label. */
     String labelImage() default "";
     
     /** Number of columns to span in the grid. Though not strictly a "standard" parameter, this is a parameter supported by {@link BeanGridPanel}, 
      * which is the default layout used by {@link BeanForm}. See {@link BeanGridPanel} parameters for more details. */
     int colspan() default 1;
     
     /** CSS class to be used for the property. */
     String css() default "";
     
     /** Dynamic CSS class method name to be used for the property. This method must have the signature: public String methodName(<BeanClass> bean, ElementMetaData elementMetaData). */
     String dynamicCss() default "";
 
     /** Arbitrary non-standard parameters. These are interpreted by the component. */
     Parameter[] params() default {};
     /** Short-cut to specify a single parameter. This is the parameter's name. */
     String paramName() default "";
     /** Short-cut to specify a single parameter. This is the parameter's value. */
     String paramValue() default "";
 
     // End Common to Action and Property ---
 }
