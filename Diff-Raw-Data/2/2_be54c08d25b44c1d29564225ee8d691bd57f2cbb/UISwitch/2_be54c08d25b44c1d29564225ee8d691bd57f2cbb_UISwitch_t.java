 /* Created on 12-Jan-2006
  */
 package uk.org.ponder.rsf.components;
 
 /** A "placeholder" component (similar to UIReplicator) implementing a boolean
  * test. Based on whether two object values (the lvalue and the rvalue) compare
  * equal by means of Java Object.equals(), one of two genuine components specified
  * in a serialized "proto-tree" will be placed in the view tree prior to rendering.
  * 
  * @author Antranig Basman (amb26@ponder.org.uk)
  */
 public class UISwitch extends UIComponent {
   /** The lvalue to form the basis of the comparison. If this is an instance of
    * ELReference, this will cause the value to be fetched from the request container.
    */
   public Object lvalue;
   /** The rvalue to form the basis of the comparison. If this is an instance of
    * ELReference, this will cause the value to be fetched from the request container.
    */
   public Object rvalue;
   
   /** The component to be rendered if the lvalue and rvalue compare equal */
   public UIComponent truecomponent;
   /** The component to be rendered if the lvalue and rvalue do not compare equal */
   public UIComponent falsecomponent;
 }
