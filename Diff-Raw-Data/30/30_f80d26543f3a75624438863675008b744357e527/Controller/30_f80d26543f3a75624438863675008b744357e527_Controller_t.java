 import java.text.DecimalFormat;
 import java.awt.*;
 import java.util.ArrayList;
 
 /**
  * Write a description of class Controller here.
  * 
  * @author AngryPirates Cabin Boy Greg & Seaman Sam
  * @version 2011,03,14
  */
 public class Controller
 {
     /* Average Execution Times (Sam):
      * 21504 Hours = 48 Minutes 17 Seconds (18.1 Gb - Loyalty File) - Simulation of about 2.5 Years
      * 10752 Hours = 19 Minutes 45 Seconds
      * 5376 Hours = 11 Minutes 51 Seconds
      * 2688 Hours = 5 Minutes 44 Seconds
      * 1344 Hours = 2 Minutes 40 Seconds
      * 672 Hours = 1 Minute 18 Seconds
      * 336 Hours = 38 Seconds
      * 168 Hours = 16 Seconds
      * 24 Hours = 2 Seconds
      */
    private final int MAX_EXECUTION = 21504 * 3600; //Converts into seconds;
     
     private int startTime; //starting hour
     private int runTime; //total running time
     private int currentTick; //current running time
     private Store myStore;
     private Canvas myCanvas;
 
     //Number formating for statistics output
     private DecimalFormat statOutput = new DecimalFormat("#,##0");
     private DecimalFormat currencyOutput = new DecimalFormat("#,##0.00");
     private DecimalFormat avgOutput = new DecimalFormat("#,##0.0000");
 
     /**
      * Constructor for objects of class Controller
      */
     public Controller() 
     {
         myStore = new Store();
         try {
             runController();
         }
         catch(Exception e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Performs the main operations of the simulation, controlling timings and everything else
      */
     public void runController() throws InterruptedException
     {
         //Initialization methods
         int[] myArray = new int[3];
         myArray = menuSystem(); //0 = ticks, 1 = sleepTime
         int totalTicks = myArray[0];
         int sleepTime = myArray[1];
         int startHour = myArray[2];
 
         //Calculate tick related things
         int startingTick = startHour*3600;
         int ticks = startingTick + totalTicks;
         
         //Gets the time from the start of the simulation as a number of seconds
 
         long startTime = System.currentTimeMillis();
 
         //mainMethods
         myStore.calcCurrentProbability(currentTick / 3600);
         myCanvas = new Canvas();
         for (int currentTick = startingTick; currentTick <= ticks; currentTick++)
         {
             if ((currentTick % 3600) == 0)
             {
                 //System.out.println("CalculatingProbability");
                 myStore.calcCurrentProbability((currentTick / 3600)%24);
             }
             //myStore.Run((currentTick % 3600));//What is this doing here? line below is also myStore.run()
             myStore.updateCumulativeAverage();
             myStore.Run((currentTick / 3600)%24);
            if ((!(sleepTime == 0)) && (currentTick%((int)(Math.ceil(60.0/sleepTime))))==0)//arbitrary // do some maths with sleeptime to get the actual framerate. (currentTick%1200==0)
             {
                 drawGraphics();
                 Thread.currentThread().sleep(sleepTime);
             }
         }
         
         //Gets the time from the end of the simulation as a number of seconds
 
         long endTime = System.currentTimeMillis();
         
         reportStatistics(totalTicks, myStore.getCustomerCounter(), myStore.getAverageInStore(totalTicks), myStore.getAverageQueue(totalTicks), myStore.getAverageWait(totalTicks), myStore.getShopProfit(), (endTime - startTime)/1000); 
     }
 
     /**
      * Main method. Runs initialization menu and controlls the ticks through the program.
      */
     public static void main(String [ ] args)
     {
         new Controller();
     }
 
     /**
      * Will at some point handle the visual aspect of the simulation
      */
     public void drawGraphics()
     {
         //clear previous
         myCanvas.erase();
         //gather graphics
         Dimension shopSize = new Dimension(600,450); //arbitrary //Params(y,x)
         ArrayList<Point> aisles = new ArrayList<Point>();
         int aislesCount = (int)((shopSize.getHeight() - 20) / 40);
         for (int i = 0; i < aislesCount; i++)
         {
             aisles.add(new Point(20,(40*(i+1))-20));
         }
         ArrayList<Integer> checkouts = myStore.getQueues();
         Dimension checkoutArea = new Dimension(myCanvas.CHECKOUT_WIDTH*myStore.getCheckoutListSize(),myCanvas.CHECKOUT_LENGTH+4); //arbitrary//Should be myStore.MAX_CHECKOUTS but we don't have a max
         ArrayList<Point> customers = myStore.getCustomerLocations();
         //pass all
         myCanvas.addShopFloor(shopSize, 
                                 aisles, 
                                 checkoutArea,
                                 checkouts,
                                 customers);
     }
 
     /**
      * Handles the output of statistics at the end of the simulation. Outputs to terminal
      * @param totalTicks The total number of ticks the simulation was run for
      * @param customerCounter The total number of customers that came through the store
      * @param averageInStore The average number of customers in the store per hour
      * @param averageQueue The average queue length in the store
      * @param shopProfit The total cash earned from selling items
      * @param executionTime The time the simulation took to run
      */
     public void reportStatistics(int totalTicks, int customerCounter, double averageInStore, double averageQueue, double averageWait, double shopProfit, long executionTime)
     {
         System.out.println("");
         System.out.println("########################## Statistics: ##########################");
        System.out.println(statOutput.format(totalTicks) + " Seconds total running time");
         System.out.print("Execution Time: ");
         if (executionTime < 60) {
             System.out.println(executionTime + " Seconds");
         } else {
             System.out.print((executionTime/60) + " Minutes ");
             System.out.println((executionTime - ((executionTime/60)*60)) + " Seconds");
         }
         System.out.println("");
         
         System.out.println(statOutput.format(customerCounter)+ " Customers in the Store");
         System.out.println(avgOutput.format(averageInStore) + " Average Customers per Hour");
         System.out.println(avgOutput.format(averageQueue) + " Average Customers in a Queue");
         System.out.println(avgOutput.format(averageWait) + " Seconds - Average Waiting Time per Customer"); //Waiting time not yet implemented
         System.out.println("");
         
         System.out.println("" + currencyOutput.format(shopProfit) + " Total Profit"); //Need to let profigt be tracked as BigDecimal, value is being capped (I think) - Sam
         System.out.println("" + currencyOutput.format((shopProfit/customerCounter)) + " Profit per Customer");
         System.out.println("#################################################################");
         System.out.println("");
     }
 
     /**
      * Menu system uses User Dialog to gather data on Stats Only mode, Speed multiplier for the program and virtual hours it wants to be run.
      */
     public int[] menuSystem()
     {
         UserDialog myUD = new UserDialog();
         int sleepTime = -1;
         do{
             if(myUD.getBoolean("Do you want to run in Stats-Only mode?")){
                 sleepTime = 0;
             }
             else{
                 float speed;
                 do{
                     speed = myUD.getInt("What speed multiplier do you want to run at? For real time put 1, maximum is 1000x real time");
                 }while (speed < 1);
                 if (speed > 1000){
                     myUD.showMessage("Speed selected was " + speed + ", number defaulting to 1000");
                     speed = 1000;
                 }
                float myFloat = (1/speed)*1000;
                sleepTime = (int)(myFloat + 0.5f);//the f makes the 0.5 a float instead of double.
             }
         }while (sleepTime == -1);
        int seconds;
         do{
            seconds = myUD.getInt("How many seconds do you want to run the program? Maximum is " + (((MAX_EXECUTION/3600)/24)/7) + " weeks (" + MAX_EXECUTION + " seconds)");
        }while (seconds < 1);
        if (seconds > MAX_EXECUTION){
            myUD.showMessage("Seconds selected was " + seconds + ", number defaulting to " + MAX_EXECUTION);
            seconds = MAX_EXECUTION;
         }
        int ticks = seconds;
         int startHour;
         do{
             startHour = myUD.getInt("Which hour of the day do you want to start the program in? (24h clock)"); //arbitrary // Needs parsing for minutes and if we put in 00:00
         }while (startHour < 0 || startHour > 24);
         int[] myArray = new int[3];
         myArray[0] = ticks;
         myArray[1] = sleepTime;
         myArray[2] = startHour;
         return myArray;
     }
 }
 
