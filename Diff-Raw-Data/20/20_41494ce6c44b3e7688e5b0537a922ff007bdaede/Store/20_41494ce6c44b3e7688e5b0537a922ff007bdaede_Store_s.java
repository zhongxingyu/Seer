 import java.util.ArrayList;
 import java.util.Random;
 import java.math.BigDecimal;
 import java.awt.*;
 
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
     private double shopProfit; //BigDecimal
     private double currentProbability;
     private double cumulativeProbability;
    private double cumulativeWait;
     private final int DESIRED_AVERAGE_LENGTH = 4;
     private final int CLOSE_THRESHOLD = 1;
     private int totalInStore = 0;
     //graphics
     private Dimension shopFloor;
     private ArrayList<Point> aisles;
     private Dimension checkoutArea;
     private int checkoutCounter = 0;
 
     /**
      * Constructor for objects of class Store
      */
     public Store()
     {
         checkoutList = new ArrayList<Checkout>();
         customerBrowsing = new ArrayList<Customer>();
         itemList = new ArrayList<Item>();
         rand = new Random();
         customerCounter = 0;
         shopProfit = 0;   
         itemList = FileHandler.createAllItems();
         UserDialog ud = new UserDialog();
         FileHandler.add("Start Store",ud.getBoolean("Do you want to clear the loyalty file?"));
         Checkout newCheckoutExpress = new Checkout(true);
         checkoutList.add(newCheckoutExpress); //arbitrary add checkout coords;
         Checkout newCheckout = new Checkout(false);
         checkoutList.add(newCheckout);
     }
     
     public ArrayList<Integer> getQueues()
     {
         ArrayList<Integer> queues = new ArrayList<Integer>();
         
         for (Checkout check:checkoutList)
         {
             queues.add(new Integer(check.getQueueLength()));
         }
         return queues;
     }
     
     public int getCheckoutListSize()
     {
         return checkoutList.size();
     }
     
     public ArrayList<Point> getCustomerLocations()
     {
         ArrayList<Point> customers = new ArrayList<Point>();
         for (Customer cust:customerBrowsing)
         {
             customers.add(cust.getLocation());
         }
         return customers;
     }
     
     /** 
      * Runs once per tick, controls customers on the shop floor in customerBrowsing and controls passing customers to the queue area.
      * @param hour The current global hour the store is in.
      */
     public void Run(int hour)
     {
         createCustomer(hour);
         for(int i = (customerBrowsing.size()-1); i >= 0; i--) //Was error in this loop, but my bad ~Alex
         {
             Customer currentCustomer = customerBrowsing.get(i);
             if (currentCustomer.getShoppingTime() > 0)
             {
                 shopProfit += currentCustomer.addItem();
             }
             else
             {
                 if (currentCustomer.getTrolleyCount() <= 10)
                 {
                     addToSmallestQueue(customerBrowsing.remove(i),true);//join queue with least items
                 }
                 else
                 {
                     addToSmallestQueue(customerBrowsing.remove(i),false);//join queue with least items exclude express
                 }
                 //addToSmallestQueue(customerBrowsing.remove(currentCustomerIndex), (currentCustomer.getTrolleyCount() <=10));
             }
         }
         for (int i = (checkoutList.size()-1); i >= 0; i--)
         {
             Checkout currentCheckout = checkoutList.get(i);
            currentCheckout.run();
             if (currentCheckout.getQueueLength() == 0 && currentCheckout.getClosing() == true) {
                 checkoutList.remove(currentCheckout);
             }
         }
         if (currentAverageLength() > DESIRED_AVERAGE_LENGTH) {
             Checkout newCheckout = new Checkout(false);
             checkoutList.add(newCheckout);
             checkoutCounter++;
         } else if (currentAverageLength() < CLOSE_THRESHOLD && checkoutList.size() > 1) {
             int i = 0;
             boolean closed = false;
             while (i < checkoutList.size() && closed == false) {
                 Checkout checkout = checkoutList.get(i);
                 if (!checkout.isExpress() && checkout.getClosing() == false && checkoutCounter >= 1) {
                     checkout.setClosing();
                     checkoutCounter--;
                     closed = true;
                 }
                 i++;
             }
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
     
     public void updateCumulativeAverage() {
         cumulativeProbability += currentAverageLength();
     }
     
     /**
      * Passes statistic of the total number of customers to the controller.
      * @return customerCounter The sum of customers.
      */
     public int getCustomerCounter()
     {
         return customerCounter;
     }
     
     public double getAverageInStore(int runTime)
     {
         return (double)customerCounter / (runTime / 3600);
     }
     
     public double getAverageQueue(int runTime)
     {
         return (double)cumulativeProbability / runTime;
     }
     
     public double getShopProfit() {
         return shopProfit;
     }
     
     /**
      * Totals up the time spent by all customers in store.
      */
     private void calcAverageInStore(Customer cust)
     {
         totalInStore += cust.getTimeInStore();
     }
     
     public double getAverageWait(int runTime) {        
         return (double)cumulativeWait / runTime;
     }
     
     /**
      * Method to decide if a customer will arrive on this tick and if so, create one and add to the shop floor.
      * @param hour The global hour the customer is created in.
      */
     public void createCustomer(int hour)
     {
         if (rand.nextFloat() <= currentProbability) {
             //id number needs calculating.
             customerBrowsing.add(new Customer(itemList, hour));//arbitrary
             customerCounter++;
             //System.out.println("Person entered store");
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
         //System.out.println("Assigning customer to queue");
         Checkout minCheckout = null;// = new Checkout();
         int min = Integer.MAX_VALUE;
         if (express)
         {
             for (Checkout currentCheckout:checkoutList)
             {
                 if (currentCheckout.itemCount() < min && currentCheckout.getClosing() == false)
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
                     if (currentCheckout.itemCount() < min && currentCheckout.getClosing() == false)
                     {
                         minCheckout = currentCheckout;
                         min = currentCheckout.itemCount();
                 }
             }
         }
         minCheckout.add(myCustomer);
     }
 }
