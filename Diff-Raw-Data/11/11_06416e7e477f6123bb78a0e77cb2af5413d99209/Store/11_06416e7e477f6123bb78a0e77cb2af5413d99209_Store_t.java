 import java.util.ArrayList;
 import java.util.Random;
 
 /**
  * Write a description of class Store here.
  * 
  * @author AngryPirates, Cabin boy Greg and Seaman Sam
  * @version 2011,03,10
  */
 public class Store
 {
     private int checkoutLimit;
     private Random rand;
     private ArrayList<Checkout> checkoutList;
     private ArrayList<Customer> customerBrowsing;
     private ArrayList<Item> itemList;
     private int customerCounter;
     private double currentProbability;
     private final int DESIRED_AVERAGE_LENGTH = 4;
     private int totalInStore = 0;
 
     /**
      * Constructor for objects of class Store
      */
     public Store()
     {
         checkoutList = new ArrayList<Checkout>();
         customerBrowsing = new ArrayList<Customer>();
         itemList = new ArrayList<Item>();
         rand = new Random();
         FileHandler myFileHandler = new FileHandler();
         itemList = myFileHandler.getItemList();
         Checkout newCheckoutExpress = new Checkout(true);
         checkoutList.add(newCheckoutExpress);
         Checkout newCheckout = new Checkout(false);
         checkoutList.add(newCheckout);
     }
     
     /** 
      * Runs once per tick, controls customers on the shop floor in customerBrowsing and controls passing customers to the queue area.
      * @param hour The current global hour the store is in.
      */
     public void Run(int hour)
     {
         createCustomer(hour);
         for(int i = (customerBrowsing.size()-1); i == 0; i--)
         {
             Customer currentCustomer = customerBrowsing.get(i);
             if (currentCustomer.getShoppingTime() > 0)
             {
                 currentCustomer.addItem();
             }
             else
             {
                 if (currentCustomer.getTrolleyCount() <= 10)
                 {
                     addToSmallestQueue(customerBrowsing.remove(i),true);//join queue with lest items
                 }
                 else
                 {
                     addToSmallestQueue(customerBrowsing.remove(i),false);//join queue with lest items exclude express
                 }
                 //addToSmallestQueue(customerBrowsing.remove(currentCustomerIndex), (currentCustomer.getTrolleyCount() <=10));
             }
         }
         for (Checkout currentCheckout:checkoutList)
         {
             currentCheckout.run();
         }
         if (currentAverageLength() > DESIRED_AVERAGE_LENGTH)
         {
             Checkout newCheckout = new Checkout(false);
             checkoutList.add(newCheckout);
         }
     }
     
     /**
      * Gets the current Average Queue length.
      * @return average The average number of people in the queues as a double.
      */
     public double currentAverageLength()
     {
         int sum = 0;
         for (Checkout c:checkoutList)
         {
             sum += c.getQueueLength();
         }
         return sum / checkoutList.size();
     }
     
     /**
      * Passes statistic of the total number of customers to the controller.
      * @return customerCounter The sum of customers.
      */
     public int getCustomerCounter()
     {
         return customerCounter;
     }
     
     /**
      * Totals up the time spent by all customers in store.
      */
     private void calcAverageInStore(Customer cust)
     {
         totalInStore += cust.getTimeInStore();
     }
     
     /**
      * Returns the average time spent in store by all customers
      * @return average Returns the average as a double.
      */
     public double getAverageInStore()
     {
         double average = totalInStore/customerCounter;
         return average;
     }
     
     /**
      * Gets the average length of time customers have spent in queues.
      * @return average The average length of time spent in queues as a dobule.
      */
     public double getAverageQueue()
     {
         double avgTotal = 0;
         for (Checkout check:checkoutList)
         {
             avgTotal += check.getAverageQueue();
         }
         double average = avgTotal/checkoutList.size();
         return average;
     }
     
     /**
      * Method to decide if a customer will arrive on this tick and if so, create one and add to the shop floor.
      * @param hour The global hour the customer is created in.
      */
     public void createCustomer(int hour)
     {
         if (rand.nextFloat() <= currentProbability) {
             //id number needs calculating.
             customerBrowsing.add(new Customer(itemList, 5, hour));//arbitrary
             customerCounter++;
            System.out.println("Person entered store");
         }
     }
     
     /**
      * Calculates the probability that a customer will enter the store at the current hour.
      * @param currentHour The current global hour.
      */
     public void calcCurrentProbability(int currentHour)
     {
         //Assuming the the chances are going to vary to the nearest hour
         double[] timeProbabilities =  {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5};
         currentProbability = timeProbabilities[currentHour];
     }
 
     /**
      * Works out which checkout in checkoutList has the smallest queue length in terms of number of items in the queue and then adds that customer to the queue.
      * @param myCustomer The customer to be added to the queue.
      * @param express Toggles if the customer is eligable to join an express queue.
      */
     public void addToSmallestQueue(Customer myCustomer, boolean express)
     {
        System.out.println("Assigning customer to queue");
         Checkout minCheckout = null;// = new Checkout();
         int min = Integer.MAX_VALUE;
         if (express)
         {
             for (Checkout currentCheckout:checkoutList)
             {
                 if (currentCheckout.itemCount() < min)
                 {
                     minCheckout = currentCheckout;
                     min = currentCheckout.itemCount();
                 }
             }
         }
         else
         {
             for (Checkout currentCheckout:checkoutList)
             {
                 if (!currentCheckout.isExpress())
                     if (currentCheckout.itemCount() < min)
                     {
                         minCheckout = currentCheckout;
                         min = currentCheckout.itemCount();
                 }
             }
         }
         minCheckout.add(myCustomer);
     }
 }
