 /**
  * 
  */
 package solution;
 
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author Jacob Evans, Bodaniel Jeanes
  */
 public class CargoInventoryTest {
 
     CargoInventory inventory;
 
     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp() throws Exception {
         inventory = new CargoInventory(3, 3, 10);
     }
 
     // Constructor Tests
     @Test(expected = IllegalArgumentException.class)
     public void testConstructorThrowsIllegalArgumentExceptionWhenFirstParameterIsNull()
             throws CargoException {
         new CargoInventory(null, 1, 1);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testConstructorThrowsIllegalArgumentExceptionWhenSecondParameterIsNull()
             throws CargoException {
         new CargoInventory(1, null, 1);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testConstructorThrowsIllegalArgumentExceptionWhenThirdParameterIsNull()
             throws CargoException {
         new CargoInventory(1, 1, null);
     }
 
     @Test(expected = CargoException.class)
     public void testConstructorThrowsCargoExceptionIfFirstParameterIsNegative()
             throws CargoException {
         new CargoInventory(-1, 1, 1);
     }
 
     @Test(expected = CargoException.class)
     public void testConstructorThrowsCargoExceptionIfSecondParameterIsNegative()
             throws CargoException {
         new CargoInventory(1, -1, 1);
     }
 
     @Test(expected = CargoException.class)
     public void testConstructorThrowsCargoExceptionIfThirdParameterIsNegative()
             throws CargoException {
         new CargoInventory(1, 1, -1);
     }
 
     // loadContainer() tests
     @Test(expected = IllegalArgumentException.class)
     public void testLoadContainerWithNullThrowsIllegalArgumentException()
             throws IllegalArgumentException, CargoException {
         inventory.loadContainer(null);
     }
 
     @Test
     public void testLoadContainerWithNormalContainer() throws LabelException,
             CargoException {
         final ContainerLabel containerOne = new ContainerLabel(1, 1, 1, 1);
         inventory.loadContainer(containerOne);
     }
 
     @Test(expected = CargoException.class)
     public void testLoadContainerThrowsCargoExceptionWhenAddingContainerThatHasNoStackForIt()
             throws IllegalArgumentException, CargoException, LabelException {
         inventory.loadContainer(new ContainerLabel(100, 3, 1, 1));
     }
 
     @Test(expected = CargoException.class)
     public void testLoadContainerThrowsCargoExceptionWhenSameContainerObjectIsAddedTwice()
             throws IllegalArgumentException, LabelException, CargoException {
         ContainerLabel container = new ContainerLabel(100, 3, 1, 1);
        /*try {
             inventory.loadContainer(container);
         } catch (CargoException e) {
             fail("CargoException thrown too early");
         }
*/
         // this call should throw the exception
         inventory.loadContainer(container);
     }
 
     @Test(expected = CargoException.class)
     public void testLoadContainerThrowsCargoExceptionWhenSameContainerLabelIsAddedTwice()
             throws IllegalArgumentException, LabelException, CargoException {
 
         // create two DIFFERENT objects with SAME parameters
         ContainerLabel container1 = new ContainerLabel(100, 3, 1, 1);
         ContainerLabel container2 = new ContainerLabel(100, 3, 1, 1);
        /*try {
             inventory.loadContainer(container1);
         } catch (CargoException e) {
             fail("CargoException thrown too early");
        }*/
 
         // this call should throw the exception
         inventory.loadContainer(container2);
     }
 
     /**
      * This will test to see if an exception is thrown if there are too many
      * kinds of containers for the ship
      * 
      * @throws LabelException
      * @throws CargoException
      */
     @Test(expected = CargoException.class)
     public void testLoadContainerWhenTooManyContainersForShip()
             throws LabelException, CargoException {
         inventory = new CargoInventory(100, 100, 2);
 
         // 3 containers regardless of kind for a ship that can only hold 2
         final ContainerLabel containerOne = new ContainerLabel(1, 1, 1, 1);
         final ContainerLabel containerTwo = new ContainerLabel(1, 1, 2, 1);
         final ContainerLabel containerThree = new ContainerLabel(1, 1, 3, 1);
 
         try {
             inventory.loadContainer(containerOne);
             inventory.loadContainer(containerTwo);
         } catch (final CargoException e) {
             fail("CargoException thrown too early");
         }
 
         // This call should raise an exception as only 2 containers are allowed
         // on the ship
         inventory.loadContainer(containerThree);
     }
 
     /**
      * This will test to see if an exception is thrown if there are too many
      * containers for a stack of a certain kind
      * 
      * @throws LabelException
      * @throws CargoException
      */
     @Test(expected = CargoException.class)
     public void testLoadContainerWhenTooManyContainersForStack()
             throws LabelException, CargoException {
         inventory = new CargoInventory(100, 2, 100);
 
         // 3 containers of the same kind (so will be in the same stack)
         final ContainerLabel containerOne = new ContainerLabel(1, 1, 1, 1);
         final ContainerLabel containerTwo = new ContainerLabel(1, 1, 2, 1);
         final ContainerLabel containerThree = new ContainerLabel(1, 1, 3, 1);
 
         try {
             inventory.loadContainer(containerOne);
             inventory.loadContainer(containerTwo);
         } catch (final CargoException e) {
             fail("CargoException thrown too early");
         }
 
         // This call should raise an exception as the stack height is only 2
         inventory.loadContainer(containerThree);
     }
 
     /**
      * This will test to see if an exception is thrown if there are too many
      * kinds of containers for a ship
      * 
      * @throws LabelException
      * @throws CargoException
      */
     @Test(expected = CargoException.class)
     public void testLoadContainerWhenTooManyContainersForNumberOfStackKinds()
             throws LabelException, CargoException {
         inventory = new CargoInventory(2, 100, 100);
 
         // Three different kinds of containers
         final ContainerLabel containerOne = new ContainerLabel(0, 1, 1, 1);
         final ContainerLabel containerTwo = new ContainerLabel(1, 1, 1, 1);
         final ContainerLabel containerThree = new ContainerLabel(2, 1, 1, 1);
 
         try {
             inventory.loadContainer(containerOne);
             inventory.loadContainer(containerTwo);
         } catch (final CargoException e) {
             fail("CargoException thrown too early");
         }
 
         // This call should raise an exception as their is only room for two
         // stacks
         inventory.loadContainer(containerThree);
     }
 
     @Test(expected = CargoException.class)
     public void testLoadContainerWhenLoadContainerThrowsCargoExceptionWhenInventoryHasMaxSizeOfZero()
             throws IllegalArgumentException, CargoException, LabelException {
         inventory = new CargoInventory(100, 100, 0);
         ContainerLabel container = new ContainerLabel(1, 1, 1, 1);
         inventory.loadContainer(container);
     }
 
     @Test(expected = CargoException.class)
     public void testLoadContainerThrowsCargoExceptionWhenInventoryHasMaxHeightOfZero()
             throws IllegalArgumentException, CargoException, LabelException {
         inventory = new CargoInventory(100, 0, 100);
         ContainerLabel container = new ContainerLabel(1, 1, 1, 1);
         inventory.loadContainer(container);
     }
 
     @Test(expected = CargoException.class)
     public void testLoadContainerThrowsCargoExceptionWhenInventoryHasMaxStacksOfZero()
             throws IllegalArgumentException, CargoException, LabelException {
         inventory = new CargoInventory(0, 100, 100);
         ContainerLabel container = new ContainerLabel(1, 1, 1, 1);
         inventory.loadContainer(container);
     }
 
     // isOnboard() tests
 
     @Test
     public void testIsOnboardWithOnlyOneItemInStack() throws LabelException,
             CargoException {
         final ContainerLabel containerOne = new ContainerLabel(1, 1, 1, 1);
         inventory.loadContainer(containerOne);
         assertTrue(inventory.isOnboard(containerOne));
     }
 
     @Test
     public void testIsOnboardWithMoreThanOneItemInStack()
             throws LabelException, CargoException {
         final ContainerLabel containerOne = new ContainerLabel(1, 1, 1, 1);
         final ContainerLabel containerTwo = new ContainerLabel(1, 1, 2, 1);
         inventory.loadContainer(containerOne);
         inventory.loadContainer(containerTwo);
 
         assertTrue(inventory.isOnboard(containerOne));
     }
 
     /**
      * This will return false as the item is not aboard the cargo ship
      * 
      * @throws LabelException
      * @throws CargoException
      */
     @Test
     public void testIsOnboardWhenNotOnboard() throws LabelException,
             CargoException {
         final ContainerLabel containerOne = new ContainerLabel(1, 1, 1, 1);
         assertFalse(inventory.isOnboard(containerOne));
     }
 
     /**
      * This will throw a IllegalArgumentException as its given a null object
      * 
      * @throws LabelException
      * @throws CargoException
      */
     @Test(expected = IllegalArgumentException.class)
     public void testIsOnboardNullObject() throws LabelException, CargoException {
         final ContainerLabel containerOne = null;
         assertFalse(inventory.isOnboard(containerOne));
     }
 
     // isAccessible() tests
     /**
      * This should return true as the container is the top of the stack
      * 
      * @throws LabelException
      * @throws CargoException
      */
     @Test
     public void testIsAccessibleReturnsTrueWhenOnlyOneItemInTheStack()
             throws LabelException, CargoException {
         final ContainerLabel containerOne = new ContainerLabel(1, 1, 1, 1);
         inventory.loadContainer(containerOne);
         assertTrue(inventory.isAccessible(containerOne));
     }
 
     /**
      * This should return true as the container isn't the top of the stack
      * 
      * @throws LabelException
      * @throws CargoException
      */
     @Test
     public void testIsAccessibleWhenAskingForTopItemInStackWithMoreThanOneItem()
             throws LabelException, CargoException {
         final ContainerLabel containerOne = new ContainerLabel(1, 1, 1, 1);
         final ContainerLabel containerTwo = new ContainerLabel(1, 1, 2, 1);
 
         inventory.loadContainer(containerOne);
         inventory.loadContainer(containerTwo);
 
         assertTrue(inventory.isAccessible(containerTwo));
     }
 
     /**
      * This should return false as the container isn't the top of the stack
      * 
      * @throws LabelException
      * @throws CargoException
      */
     @Test
     public void testIsAccessibleWhenAskingForNonTopItemInStackWithMoreThanOneItem()
             throws LabelException, CargoException {
         final ContainerLabel containerOne = new ContainerLabel(1, 1, 1, 1);
         final ContainerLabel containerTwo = new ContainerLabel(1, 1, 2, 1);
 
         inventory.loadContainer(containerOne);
         inventory.loadContainer(containerTwo);
 
         assertFalse(inventory.isAccessible(containerOne));
     }
 
     @Test
     public void testIsAccessibleReturnsFalseWhenTheContainerIsOutOfRange()
             throws IllegalArgumentException, LabelException {
         final ContainerLabel containerOne = new ContainerLabel(100, 3, 100, 3);
         assertFalse(inventory.isAccessible(containerOne));
 
     }
 
     // unloadContainer() tests
     /**
      * This should not throw any exceptions as the container is the top of the
      * stack
      * 
      * @throws LabelException
      * @throws CargoException
      */
     @Test
     public void testUnloadTopContainer() throws LabelException, CargoException {
         final ContainerLabel containerOne = new ContainerLabel(1, 1, 1, 1);
         final ContainerLabel containerTwo = new ContainerLabel(1, 1, 2, 1);
 
         inventory.loadContainer(containerOne);
         inventory.loadContainer(containerTwo);
 
         inventory.unloadContainer(containerTwo);
     }
 
     /**
      * This should throw a CargoException as the container isn't at the top of
      * the stack
      * 
      * @throws LabelException
      * @throws CargoException
      */
     @Test(expected = CargoException.class)
     public void testUnloadContainerThrowsCargoExceptionWhenContainerNotAtTop()
             throws LabelException, CargoException {
         final ContainerLabel containerOne = new ContainerLabel(1, 1, 1, 1);
         final ContainerLabel containerTwo = new ContainerLabel(1, 1, 2, 1);
 
         inventory.loadContainer(containerOne);
         inventory.loadContainer(containerTwo);
 
         inventory.unloadContainer(containerOne);
     }
 
     /**
      * This should throw a IllegalArgumentException as we can't unload null
      * 
      * @throws LabelException
      * @throws CargoException
      */
     @Test(expected = IllegalArgumentException.class)
     public void testUnloadNullContainerThrowsExceptionOnInventoryWithCargo()
             throws LabelException, CargoException {
         final ContainerLabel containerOne = new ContainerLabel(1, 1, 1, 1);
         inventory.loadContainer(containerOne);
 
         inventory.unloadContainer(null);
     }
 
     /**
      * This should throw a IllegalArgumentException as we can't unload null
      * 
      * @throws LabelException
      * @throws CargoException
      */
     @Test(expected = IllegalArgumentException.class)
     public void testUnloadNullContainerThrowsExceptionOnInventoryWithoutCargo()
             throws LabelException, CargoException {
         inventory.unloadContainer(null);
     }
 
     @Test(expected = CargoException.class)
     public void testUnloadThrowsExceptionWhenContainerIsNotOnShip()
             throws IllegalArgumentException, LabelException, CargoException {
         final ContainerLabel containerOne = new ContainerLabel(1, 1, 1, 1);
         inventory.unloadContainer(containerOne);
     }
 
     // toArray() tests
 
     /**
      * This will return the 2nd stack of the inventory in the form of an array
      * 
      * @throws LabelException
      * @throws CargoException
      */
     @Test
     public void testToArray() throws LabelException, CargoException {
         final ContainerLabel containerOne = new ContainerLabel(1, 1, 1, 1);
         final ContainerLabel containerTwo = new ContainerLabel(1, 1, 2, 1);
         final ContainerLabel containerThree = new ContainerLabel(1, 1, 3, 1);
         final ContainerLabel containerToNotInclude = new ContainerLabel(2, 1,
                 1, 1);
 
         inventory.loadContainer(containerOne);
         inventory.loadContainer(containerTwo);
         inventory.loadContainer(containerThree);
         inventory.loadContainer(containerToNotInclude);
 
         final ContainerLabel[] arrayLabels = { containerOne, containerTwo,
                 containerThree };
 
         assertArrayEquals(arrayLabels, inventory.toArray(1));
     }
 
     /**
      * This will test to see if the array returns properly and returns the
      * objects in the right order
      * 
      * @throws LabelException
      * @throws CargoException
      */
     @Test
     public void testToArrayAndToString() throws LabelException, CargoException {
         final ContainerLabel containerOne = new ContainerLabel(1, 1, 1, 1);
         final ContainerLabel containerTwo = new ContainerLabel(1, 1, 2, 1);
         final ContainerLabel containerThree = new ContainerLabel(1, 1, 3, 1);
 
         inventory.loadContainer(containerOne);
         inventory.loadContainer(containerTwo);
         inventory.loadContainer(containerThree);
 
         ContainerLabel[] arrayLabels = new ContainerLabel[3];
         arrayLabels = inventory.toArray(1);
 
         assertEquals("00100002", arrayLabels[1].toString());
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testToArrayThrowsIllegalArgumentExceptionWhenPassedNull()
             throws IllegalArgumentException, CargoException {
         inventory.toArray(null);
     }
 
     @Test(expected = CargoException.class)
     public void testToArrayThrowsCargoExceptionWhenTheStackDoesNotExist()
             throws IllegalArgumentException, CargoException {
         inventory.toArray(3);
     }
 }
