 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.ObjectInputStream;
 import java.io.Serializable;
 
 /**
  * A node to be used for Binary Tree applications.
  * 
  * @author Josh Gillham
  * @version 10-16-12
  * 
  * @param <T> will be the data type.
  */
 public class BTNode< T > implements Serializable {
     /** Holds a reference of the left child. */
     private BTNode< T > left = null;
     /** Holds a reference of the right child. */
     private BTNode< T > right = null;
     /** Holds a reference to the data. */
     private T value = null;
     
     /**
      * Constructs a blank instance.
      */
     public BTNode( ) { }
     
     /**
      * Constructs an end node.
      * 
      * @param value is the data.
      */
     public BTNode( T value ) { 
         this.value = value;
     }
     
     /**
      * Constructs a parent node.
      * 
      * @param value is the data.
      * @param left is a reference to the left node.
      * @param right is a reference to the right node.
      */
     public BTNode( T value,  BTNode< T > left, BTNode< T > right ) {
         this.value = value;
         this.left = left;
         this.right = right;
     }
     
     /**
      * Accesses the left node.
      * 
      * @return a reference to the left node.
      */
     public BTNode< T > getLeftChild( ) {
         return left;
     }
     
     /**
      * Accesses the right node.
      * 
      * @return a reference to the right node.
      */
     public BTNode< T > getRightChild() {
         return right;
     }
     
     /**
      * Accesses the data.
      * 
      * @return a reference to the data.
      */
     public T getValue( ) {
         return value;
     }
     
     /**
      * Sets the left child.
      * <br>
      * Post Conditions:
      * <br>
      * -the left child is set.
      * 
      * @param newLeft is the new left node.
      */
     public void setLeftChild( BTNode< T > newLeft ) {
         this.left = newLeft;
     }
     
     /**
      * Sets the right child.
      * <br>
      * Post Conditions:
      * <br>
      * -the right child is set.
      * 
      * @param newRight is the new right node.
      */
     public void setRightChild( BTNode< T > newRight ) {
         this.right = newRight;
     }
     
     /**
      * Sets the data.
      * <br>
      * Post Conditions:
      * <br>
      * -the data is set.
      * 
      * @param value is the new data.
      */
     public void setValue( T value ) {
         this.value = value;
     }
     
     /**
      * Gives a representation of this node along with the child nodes and
      *  their children.
      *  
      * @return a string representation.
      */
     public String toString( ) {
         StringBuilder retValue = new StringBuilder();
         retValue.append( '{' );
         retValue.append( this.getValue() );
         retValue.append( ", " );
         retValue.append( this.getLeftChild() );
         retValue.append( ", " );
         retValue.append( this.getRightChild() );
         retValue.append( '}' );
         return retValue.toString();
     }
     
     /**
      * Define equality for BTNodes. Indicates whether some other object is
      *  "equal to" this one. The equals method implements an equivalence 
      *  relation on non-null object references:
      * -It is reflexive: for any non-null reference value x, x.equals(x) should
      *  return true.
      * -It is symmetric: for any non-null reference values x and y, x.equals(y)
      *  should return true if and only if y.equals(x) returns true.
      * -It is transitive: for any non-null reference values x, y, and z, if 
      *  x.equals(y) returns true and y.equals(z) returns true, then x.equals(z)
      *  should return true.
      * -It is consistent: for any non-null reference values x and y, multiple 
      *  invocations of x.equals(y) consistently return true or consistently 
      *  return false, provided no information used in equals comparisons on 
      *  the objects is modified.
      * -For any non-null reference value x, x.equals(null) should return false.
      * 
      * @param obj reference object with which to compare.
      * 
      * @return true if this object is the same as the obj argument;
      *  false otherwise.
      */
     public boolean equals(Object obj) {
         try {
             if ( obj == null )
                 return false;
             
             @SuppressWarnings( "unchecked" )
             BTNode<T> node = (BTNode<T>)obj;
             // Make sure they are both not nulls.
            if ( node.getValue() != node.getValue() ) {
                 // Now even if one is null the exception will return false;
                if ( !node.getValue().equals( this.getValue() ) )
                     return false;
             }
             // Make sure they are both not nulls.    
             if ( node.getLeftChild() != this.getLeftChild() ) {
                 // Now even if one is null the exception will return false;
                 if ( node.getLeftChild() == null ||
                         !node.getLeftChild().equals( this.getLeftChild() ) )
                     return false;
             }
             
             // Make sure they are both not nulls.    
             if ( node.getRightChild() != this.getRightChild() ) {
                 // Now even if one is null the exception will return false;
                 if ( node.getRightChild() == null ||
                         !node.getRightChild().equals( this.getRightChild() ) )
                     return false;
             }
         }
         // Null casting exceptions.
         catch ( ClassCastException e ) {
             return false;
         }
         return true;
     }
     
     /**
      * Define hashcode for BTNodes.
      * 
      * If two objects are equal according to the equals(Object) method,
      *  then calling the hashCode method on each of the two objects
      *  produces the same integer result.
      * 
      * @return a hash code value for this object.
      */
     public int hashCode() {
        int valueHash = 0;
         if ( this.getValue() != null ) {
             valueHash = this.getValue().hashCode();
         }
         if ( this.getLeftChild() != null ) {
             valueHash += this.getLeftChild().hashCode();
         }
         if ( this.getRightChild() != null ) {
             valueHash += this.getRightChild().hashCode();
         }
         return valueHash;
     }
 }
