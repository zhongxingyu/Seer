 package org.cthul.matchers.fluent.value;
 
 import org.hamcrest.Description;
 import org.hamcrest.SelfDescribing;
 import org.hamcrest.StringDescription;
 
 /**
  * Implements {@link #toString()} to return the description.
  */
 public abstract class SelfDescribingBase implements SelfDescribing {
 
     @Override
     public abstract void describeTo(Description description);
 
     /**
      * Calls {@link #describeTo(org.hamcrest.Description)} with a 
      * {@code StringDescription}, and returns its value.
     * @return description string
      */
     @Override
     public String toString() {
         StringDescription d = new StringDescription();
         describeTo(d);
         return d.toString();
     }
 }
