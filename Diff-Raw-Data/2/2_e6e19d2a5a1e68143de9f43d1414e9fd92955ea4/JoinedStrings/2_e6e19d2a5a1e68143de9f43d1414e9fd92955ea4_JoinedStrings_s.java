 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.core.spring;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.config.AbstractFactoryBean;
 
 import org.apache.commons.lang.Validate;
 
 /** A spring factory bean that creates a string concatenating other strings.
  */
 public class JoinedStrings extends AbstractFactoryBean {
 
   /** The values to concatenate, in the provided oder, never null.
    */
   private String[] values = new String[0];
 
   /** {@inheritDoc}
    */
   @Override
   protected Object createInstance() {
     return StringUtils.join(values);
   }
 
   /** {@inheritDoc}
    */
   @Override
  public Class getObjectType() {
     return String.class;
   }
 
   /** Sets the values to concatenate.
    *
    * @param theValues the values to concatenate, never null. The values are
    * concatenated in the provided order.
    */
   public void setValues(String[] theValues) {
     Validate.notNull(theValues, "The values cannot be null.");
     values = theValues;
   }
 }
 
