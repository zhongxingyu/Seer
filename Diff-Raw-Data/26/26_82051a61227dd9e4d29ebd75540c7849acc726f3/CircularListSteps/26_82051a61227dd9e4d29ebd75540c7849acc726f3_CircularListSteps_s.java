 /**
  * User: Travis Beatty
  * Date: 9/28/13
  * Time: 5:59 PM
  */
 
 import junit.framework.Assert;
 import org.jbehave.core.annotations.Given;
 import org.jbehave.core.annotations.Then;
 import org.jbehave.core.annotations.When;
 
import java.security.PublicKey;

 @SuppressWarnings({"InstanceVariableOfConcreteClass", "SuppressionAnnotation", "UnusedDeclaration", "Annotation", "ClassWithoutLogger", "PublicMethodWithoutLogging", "ClassHasNoToStringMethod"})
 class CircularListSteps<E> {
 
     private CircularListArrayBased<E> arrayBased = null;
     private CircularListReferenceBased<E> referenceBased = null;
     private E arrayBasedItem = null;
     private E referenceBasedItem = null;
     private Exception thrownByArrayBased = null;
     private Exception thrownByReferenceBased = null;
 
     CircularListSteps() {
     }
 
     @Given("a circularlist size $size with items $items")
     public final void setList(int size, E[] items) {
         arrayBased = new CircularListArrayBased<E>();
         referenceBased = new CircularListReferenceBased<E>();
         for (int i = 0; i < size; i++) {
             arrayBased.add(i, items[i]);
             referenceBased.add(i, items[i]);
         }
     }
 

     @When("I get item $index")
     public final void getItem(int index) {
         arrayBasedItem = arrayBased.get(index);
         referenceBasedItem = referenceBased.get(index);
     }
 
     @When("I add item $item to index $index")
     public final void addItem(E item, int index) {
         try {
             arrayBased.add(index, item);
        } catch(Exception e) {
             thrownByArrayBased = e;
         }
 
         try {
             referenceBased.add(index, item);
        } catch(Exception e) {
             thrownByReferenceBased = e;
         }
     }
 
     @When("I remove item $index")
     public final void removeItem(int index) {
         try {
             arrayBased.remove(index);
        } catch(Exception e) {
             thrownByArrayBased = e;
         }
 
         try {
             referenceBased.remove(index);
        } catch(Exception e) {
             thrownByReferenceBased = e;
         }
     }
 
     @When("I ask if it is an empty list")
     public final void setIsEmpty() {
         // TODO
     }
 
     @When("I add item $item to the end of the list")
     public final void addItemToEnd(E item) {
         // TODO
     }
 
     @When("I ask for the size")
     public final void setSize() {
         // TODO
     }
 
     @When("I clear the list")
     public final void clear() {
         // TODO
     }
 
     @Then("it is item $value")
     public final void isEqualToItemAtIndex(E value) {
         Assert.assertEquals(value, arrayBasedItem);
         Assert.assertEquals(value, referenceBasedItem);
     }
 
     @Then("throws $exceptionClass")
     public final void whenThrew(Class exceptionCls) {
         Assert.assertEquals(exceptionCls, thrownByArrayBased.getClass());
        Assert.assertEquals(exceptionCls, thrownByReferenceBased.getClass);
     }
 
     @Then("our list is $list")
     public final void ourListIs(E[] list) {
         // TODO
     }
 
     @Then("the boolean return should be $tf")
     public final void boolReturn(boolean tf) {
         // TODO
     }
 
     @Then("the item return should be $item")
        public final void itemReturn(E item) {
            // TODO
        }
 
     @Then("the integer return should be $value")
     public final void intReturn(int value) {
         // TODO
     }
 
     @Then("the size should be $size")
     public final void isSize(int size) {
         // TODO
     }
 
 }
