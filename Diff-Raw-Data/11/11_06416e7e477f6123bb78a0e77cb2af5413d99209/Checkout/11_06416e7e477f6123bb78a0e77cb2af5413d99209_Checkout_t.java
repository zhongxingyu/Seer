 import java.util.ArrayList;
 /**
  * Write a description of class Checkout here.
  * 
  * @author AngryPirates 
  * @version 0.1
  */
 public class Checkout
 {
     private ArrayList<Customer> queue;
     private ArrayList<String> itemReceipt;
     private boolean express = false;
     private Customer currentCustomer;
     private final int ITEM_SCAN_SPEED;
     private int scanInterval;
     private int customerCounter;
     private int totalQueue;
     /**
      * Constructor for objects of class Checkout
      * @param isExpress Toggles if checkout to be created is express or not.
      */
     public Checkout(boolean isExpress)
     {
         express = isExpress;
         ArrayList itemReceipt = new ArrayList<String>();
         queue = new ArrayList<Customer>();
         ITEM_SCAN_SPEED = 3;
         scanInterval = 3;
        customerCounter = 0;
     }
 
     /**
      * Runs once per tick, controls one customer at the till and all customers in the queue.
      */
     public void run()
     {
         if(queueHasCustomer())
         {
             for(Customer cust : queue)
             {
                 cust.incWait();
             }
         }
         if (hasCustomer())
         {   
             if (hasItems())
             {
                 if (scanInterval >= ITEM_SCAN_SPEED)
                 {
                     scanItems();
                     scanInterval = 0;
                 }
                 else
                 {
                     scanInterval -= randomDelay();
                     scanInterval++;
                 }
             }
             else 
             {
                 makeReceipt();
                 currentCustomer.getTimeInQueue();
                 currentCustomer = null;
                System.out.println("customer leaving store");
             }
         }
         else if (queueHasCustomer())
         {
             currentCustomer = queue.remove(0);
             customerCounter++;
         }
         else
         {
             //close queue //arbitrary
         }
     }
     
     /**
      * Sums up all the time customers spend in the queue.
      */
     public void calcAverageQueue(Customer cust)
     {
         totalQueue += cust.getTimeInQueue();
     }
     
     /**
      * Calculates the average time a customer spends in the queue.
      * @return Returns the average length as double.
      */
     public double getAverageQueue()
     {
         double average = totalQueue/customerCounter;
         return average;
     }
 
     private void makeReceipt()
     {
         //what does this entail?
     }
 
     /**
      * Returns true if the queue is not empty.
      * @return True if queue has at lest 1 customer.
      */
     private boolean queueHasCustomer()
     {
         if (queue.size() > 0)
         {
             return true;
         }
         else
         {
             return false;
         }
     }
 
     /**
      * Returns true if there is a customer at the till.
      * @return True if the till has a customer. 
      */
     private boolean hasCustomer()
     {
         if (!(currentCustomer == null))
         {
             return true;
         }
         else
         {
             return false;
         }
     }
     
     /**
      * Returns the queue size
      * @return returns queue size as an integer.
      */
     public int getQueueLength()
     {
         return queue.size();
     }
 
     /**
      * Checks if the customer at the till has any more items in the trolley.
      * @return True if the customer has items in the trolley.
      */
     private boolean hasItems()
     {
         if (currentCustomer.getTrolley().size() > 0)
         {
             return true;
         }
         else
         {
             return false;
         }
     }
 
     private int randomDelay()
     {
         return 0; //arbitrary//do something with a really small chance of delay, and the delay can be between a small range.
     }
 
     /**
      * Scan items takes an item out of the trolley of the customer and adds the item name to the reciept list.
      */
     private void scanItems()
     {
         Item thisItem = currentCustomer.removeTrolleyItem();
         itemReceipt.add(thisItem.getName());
         //add price?
     }
 
     /**
      * Method to get the number of Items in all the trolleys in the queue.
      * @return Number of items in the queue as an int.
      */
     public int itemCount()
     {
         int items=0;
         for (Customer c:queue)
         {
             items += c.getTrolleyCount();
         }
         return items;
     }
 
     /**
      * Returns if the checkout is an express checkout.
      */
     public boolean isExpress()
     {
         return express;
     }
 
     /**
      * Method to add a customer to the queue of the checkout.
      */
     public void add(Customer newCustomer)
     {
         queue.add(newCustomer);
     }
 }
