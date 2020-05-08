 package com.eagerlogic.cubee.client.properties.ext;
 
 import com.eagerlogic.cubee.client.properties.AExpression;
 import com.eagerlogic.cubee.client.properties.IProperty;
 
 /**
  * This class is a conditional expression which can be used when you need an expression which returns 2 different values
  * based on a boolean condition.
  *
  * @author dipacs
  *
  * @param <T> The type of the result.
  */
 public final class CondExp<T> extends AExpression<T> {
 
     private final IProperty<Boolean> condition;
     private final IProperty<T> trueProperty;
     private final IProperty<T> falseProperty;
     private final T trueValue;
     private final T falseValue;
     private final boolean isTrueProperty;
     private final boolean isFalseProperty;
 
     /**
      * Creates a new instance of ConditionalExpression using concrete values for both the true and the false cases.
      *
      * @param condition The condition which will be evaluated when deciding what to return.
      * @param trueValue The value which will be returned when the condition is true.
      * @param falseValue The value which will be returned when the condition is false.
      */
     public CondExp(IProperty<Boolean> condition, T trueValue, T falseValue) {
         if (condition == null) {
             throw new NullPointerException("The condition parameter can not be null.");
         }
         this.bind(condition);
         this.condition = condition;
         this.trueProperty = null;
         isTrueProperty = false;
         this.falseProperty = null;
         isFalseProperty = false;
         this.trueValue = trueValue;
         this.falseValue = falseValue;
     }
 
     /**
      * Creates a new instance of ConditionalExpression using property values for both cases.
      *
      * @param condition The condition which will be evaluated when deciding what to return.
     * @param trueProperty The property which value will be returned when the condition is true.
     * @param falseProperty The property which value will be returned when the condition is false.
      */
     public CondExp(IProperty<Boolean> condition, IProperty<T> trueProperty, IProperty<T> falseProperty) {
         if (condition == null) {
             throw new NullPointerException("The condition parameter can not be null.");
         }
         if (trueProperty == null) {
             throw new NullPointerException("The trueProperty parameter can not be null.");
         }
         if (falseProperty == null) {
             throw new NullPointerException("The falseProperty parameter can not be null.");
         }
         this.bind(condition);
         this.bind(trueProperty);
         this.bind(falseProperty);
         this.condition = condition;
         this.trueProperty = trueProperty;
         isTrueProperty = true;
         this.falseProperty = falseProperty;
         isFalseProperty = true;
         this.trueValue = null;
         this.falseValue = null;
     }
 
     /**
      * Creates a new instance of ConditionalExpression using concrete value for the true case, and property value for
      * the false case.
      *
      * @param condition The condition which will be evaluated when deciding what to return.
      * @param trueValue The value which will be returned when the condition is true.
     * @param falseProperty The property which value will be returned when the condition is false.
      */
     public CondExp(IProperty<Boolean> condition, T trueValue, IProperty<T> falseProperty) {
         if (condition == null) {
             throw new NullPointerException("The condition parameter can not be null.");
         }
         if (falseProperty == null) {
             throw new NullPointerException("The falseProperty parameter can not be null.");
         }
         this.bind(condition);
         this.bind(falseProperty);
         this.condition = condition;
         this.trueProperty = null;
         isTrueProperty = false;
         this.falseProperty = falseProperty;
         isFalseProperty = true;
         this.trueValue = trueValue;
         this.falseValue = null;
     }
 
     /**
      * Creates a new instance of ConditionalExpression using property value for the true case, and concrete value for
      * the false case.
      *
      * @param condition The condition which will be evaluated when deciding what to return.
     * @param trueProperty The property which value will be returned when the condition is true.
      * @param falseValue The value which will be returned when the condition is false.
      */
     public CondExp(IProperty<Boolean> condition, IProperty<T> trueProperty, T falseValue) {
         if (condition == null) {
             throw new NullPointerException("The condition parameter can not be null.");
         }
         if (trueProperty == null) {
             throw new NullPointerException("The trueProperty parameter can not be null.");
         }
         this.bind(condition);
         this.bind(trueProperty);
         this.condition = condition;
         this.trueProperty = trueProperty;
         isTrueProperty = true;
         this.falseProperty = null;
         isFalseProperty = false;
         this.trueValue = null;
         this.falseValue = falseValue;
     }
 
     @Override
     public T calculate() {
         if (condition.getObjectValue() == Boolean.TRUE) {
             if (isTrueProperty) {
                 return (T) trueProperty.getObjectValue();
             } else {
                 return trueValue;
             }
         } else {
             if (isFalseProperty) {
                 return (T) falseProperty.getObjectValue();
             } else {
                 return falseValue;
             }
         }
     }
 
 }
