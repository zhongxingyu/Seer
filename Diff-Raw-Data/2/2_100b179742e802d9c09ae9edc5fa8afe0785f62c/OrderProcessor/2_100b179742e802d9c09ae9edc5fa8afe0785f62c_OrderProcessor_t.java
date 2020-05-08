 /*
 Design and implement a class called OrderProcessor. You must implement at least the following methods:
 accept; // this method accepts a GenericOrder or any of its subclass objects and stores it in any internal collection of OrderProcessor.
 process; // this method sorts all accepted orders in the internal collection of GenericOrder into collections of ComputerPart, Peripheral, Cheese, Fruit, and Service. You must associate each object with the unique identifier. You may refer to the TwoTuple.java example in the text book.
 dispatchXXX; // this method simulates the dispatch of the sorted collections. For example, the method dispatchComputerParts() should produce this output:
 Motherboard  name=Asus, price=$37.5, order number=12345
 Motherboard  name=Asus, price=$37.5, order number=987654
 RAM  name=Kingston, size=512, price=$25.0, order number=123456
 You may overload each of the above methods as you think necessary.
  */
 
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 public class OrderProcessor<T> {
      Collection <T> listOfOrders = new ArrayList<>();
 
     public void accept(T genericOrder) {
 	listOfOrders.add(genericOrder);
     }; // this method accepts a GenericOrder or any of its subclass objects and
        // stores it in any internal collection of OrderProcessor.
 
     public void process() {
     	Collection.sort(listOfOrders);
     }; // this method sorts all accepted orders in the internal collection of
        // GenericOrder into collections of ComputerPart, Peripheral, Cheese,
        // Fruit, and Service. You must associate each object with the unique
        // identifier. You may refer to the TwoTuple.java example in the text
        // book.
 
     public void dispatchXXX() {
 	
 	for(T order : listOfOrders) {
  	    System.out.print(order.getClass() + "\t");
   	}
     }; // this method simulates the dispatch of the sorted collections. For
        // example, the method dispatchComputerParts() should produce this
        // output:
     /*
      * Motherboard  name=Asus, price=$37.5, order number=123456 Motherboard 
      * name=Asus, price=$37.5, order number=987654 RAM  name=Kingston,
      * size=512, price=$25.0, order number=123456
      */
     
     
 }
