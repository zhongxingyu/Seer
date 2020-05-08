 package quoridor;
 
 /**
  *
  * A collection of two items of the same class
  *
  * @author Stephen Sherratt
  *
  */
 
public interface Pair <X> extends Iterable {
 
     public X _1(); //the first item in the pair
     public X _2(); //the second item in the pair
 
     public X other(X item);  //the other item in the pair, given an item
 
     public boolean contains(X item); //true if the pair contains item
 
 }
