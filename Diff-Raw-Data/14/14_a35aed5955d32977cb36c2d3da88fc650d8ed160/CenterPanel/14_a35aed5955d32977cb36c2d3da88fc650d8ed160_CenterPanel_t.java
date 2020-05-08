 /**
  * Credit Worthiness System Version 1.0
  */
 package UI;
 
 import DbConnection.TransactionDetails;
 import UI.Charts.Chart;
 import UI.Charts.ChartPlot;
 import UI.Charts.GraphNode;
 import UI.Charts.Node;
 import credit.worthiness.system.CreditWorthinessSystem;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.geom.Point2D;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Vector;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.SwingConstants;
 
 /**
  *
  * @author Reagan Mbitiru <reaganmbitiru@gmail.com>
  */
 public class CenterPanel extends JPanel
 {
     private static AlertLabel alerter ;
     private MonthChartNavigator monthNav ;
     private final JLabel title ;
     private final VerticalJLabel yAxisLabel; 
     private static Chart chart;
     public static int month ;
     public static int year ;
     public static int[] minMaxTransValues= {0, 0};    
     public static ChartPlot[] allChartPlots = null ;
     
     public CenterPanel()
     {
         // initialise the components
         alerter = new AlertLabel(0.00);
         monthNav = new MonthChartNavigator();
         title = new JLabel("Transactions");
         title.setFont(new Font("sanSerif", Font.PLAIN, 31));
         yAxisLabel = new VerticalJLabel("Amount, Kshs");
         
         // get the current month and year
         getCurrentMonthAndYear();
         
         // create a new Chart instance
         chart = new Chart();       
         
         // set the model to null since no user has been selected
         allChartPlots = null ;
         
         // set the chart model
         setChartModel(month, year, minMaxTransValues, allChartPlots);                
         
         // lay out and add the components
         setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
         
         // topmost panel
         JPanel topPanel = new JPanel();
         topPanel.setOpaque(false);
         topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
         topPanel.add(Box.createRigidArea(new Dimension(72, 0)));
         topPanel.add(title);
         topPanel.add(Box.createHorizontalGlue());
         topPanel.add(alerter);
         topPanel.add(Box.createRigidArea(new Dimension(25, 0)));
         
         // center panel
         JPanel centerPanel = new JPanel();
         centerPanel.setOpaque(false);
         centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
         centerPanel.add(yAxisLabel);
         centerPanel.add(Box.createRigidArea(new Dimension(5, 0)));
         centerPanel.add(chart);
         centerPanel.add(Box.createHorizontalGlue());
         
         // lower panel
         JPanel lowerPanel = new JPanel();
         lowerPanel.setOpaque(false);
         lowerPanel.setLayout(new FlowLayout());
         lowerPanel.add(monthNav);
         
         // now add the components vertically
         add(Box.createRigidArea(new Dimension(0, 10 )));
         add(topPanel);
         add(Box.createRigidArea(new Dimension(0, 25)));
         add(centerPanel);
         add(Box.createRigidArea(new Dimension(0, 13)));
         add(lowerPanel);
         add(Box.createRigidArea(new Dimension(0, 10)));
         add(new JSeparator(SwingConstants.HORIZONTAL));
         add(Box.createRigidArea(new Dimension(0, 10)));
                         
         // finalize the JPanel
         setOpaque(false);
         setPreferredSize(new Dimension(686, 344));
     }
     
     public static void setChartModel(int chartMonth, int chartYear, 
             int[] yMinMax,ChartPlot[] allChartPlots)
     {        
          chart.setModel(chartMonth, chartYear, yMinMax, allChartPlots);
     }
 
     public static int[] getCurrentMonthAndYear() 
     {
         Calendar cal = Calendar.getInstance();        
         month = cal.get(Calendar.MONTH) ;
         year = cal.get(Calendar.YEAR) ;
         
         int[] currMonthAndYear = new int[2];
         currMonthAndYear[0] = month ;
         currMonthAndYear[1] = year ;
         return currMonthAndYear ;
     }  
     
     public static int[] getChartMonthAndYear()
     {
         if( Chart.currTime[0] == 0 )
         {
             System.out.println("Chart Time empty, using current date & time") ;
             return getCurrentMonthAndYear() ;
         }
         else
         {
             System.out.println("Using Chart time: ");
             return Chart.currTime ;
         }
     }
     
     /**
      * This method displays the alerter with the 
      * required color and the credit/debit amount 
      * for the currently selected user
      * @param amount 
      */
     public static void updateAlertLabel(double amount)
     {
         //alerter = new AlertLabel(amount) ;
         alerter.setAlerterAmount(amount);
         
         if(amount < 0 ) 
         {
             alerter.alertStatus = AlertLabel.OVERSPENT_CONDITION ;
         }            
         else 
         {
             alerter.alertStatus = AlertLabel.UNDERSPENT_CONDITION ;
         }       
         
         // repaint the alerter
         alerter.repaint();
     }
     
     public static void updateTransactionsChart() 
     {        
         int numberOfTransForUserForMonth = 0; 
                 
         // get the current year and month
         int [] currYearAndMonth = getChartMonthAndYear() ;  
         
         // initialise the chart plots and max and min transaction values to null
         allChartPlots = null ;
         minMaxTransValues[0] = 0 ;
         minMaxTransValues[1] = 0 ;
             
         // get the number of transactions for the current user
         // for the current month and year   
         TransactionDetails t = new TransactionDetails();
         numberOfTransForUserForMonth = t.getUserTransactionsNumberForMonth(CreditWorthinessSystem.getCurrentUserID()
                                     , currYearAndMonth[0],currYearAndMonth[1]);
         System.out.println("\n\n" + currYearAndMonth[0] + "- " + currYearAndMonth[1]
                 + " [" + CreditWorthinessSystem.getCurrentUserID()+ "]");
         if(numberOfTransForUserForMonth > 0)
         {           
             System.out.println("Getting plots for model, trans #: " + numberOfTransForUserForMonth);
             // get the minimum and maximum value for transactions
             minMaxTransValues =  t.getMinMaxTransValues(CreditWorthinessSystem.getCurrentUserID(), 
                     currYearAndMonth[0],currYearAndMonth[1]) ;
 
            // correct for similar values of minimum and maximum to
            // ensure that a NAN is not obtained
            if( minMaxTransValues[0] == minMaxTransValues[1] )
            {
                minMaxTransValues[0] = 0 ;
            }
            
             // get the number of days in a month
             int noOfDaysInMonth ;
             Calendar cal = new GregorianCalendar(currYearAndMonth[0], 
                     currYearAndMonth[1],  1) ;
             noOfDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH) ; 
 
             // get the transaction details for the selected user for
             // the selected month and year. These details should be packaged in Plots   
             //
             // create plot instance
             // ChartPlots should be 2 to have one for credit transactions
             // and another for debit transactions
             allChartPlots = new ChartPlot[1] ;
 
             // start with the credit plot
             // determine the number of nodes in creditplot
             ChartPlot plot1 = new ChartPlot(ChartPlot.TRANSACTION_PLOT,
                     numberOfTransForUserForMonth);
 
              // get the transactions details
             String [][] transactionDetails = new String[numberOfTransForUserForMonth][] ; // string mulridimensional array stores the 
                                                             // details of the transactions
 
             transactionDetails = t.getPlottedTransactionDetails(CreditWorthinessSystem.getCurrentUserID(), 
                     currYearAndMonth[0],currYearAndMonth[1], numberOfTransForUserForMonth ) ;
 
             // set the number of nodes for the transanction details
             GraphNode[] nodes = new GraphNode[numberOfTransForUserForMonth];
 
             // initialise the variables to store the results
             Vector<String> transactionIDs = new Vector<String>() ; 
             Vector<String> itemNames = new Vector<String>() ;
             Vector<String> transactionDays = new Vector<String>() ;
             Vector<String> itemsNumber = new Vector<String>() ;
             Vector<String> totalItemsCost = new Vector<String>() ;
 
             // unpack the transaction details for plotting
             for(int i = 0 ; i < transactionDetails.length ; i++)
             {                    
                 // get the current transaction day
                 int currTransDay = Integer.parseInt(transactionDetails[i][1]);                                 
 
                 // get the transaction ID
                 transactionIDs.add(transactionDetails[i][0]);
 
                 // get the day
                 transactionDays.add(transactionDetails[i][1]);
 
                 // get the item name
                 itemNames.add(transactionDetails[i][2]);
 
                 // get the items number
                 itemsNumber.add(transactionDetails[i][3]);
 
                 // get the total items cost
                 totalItemsCost.add(transactionDetails[i][4]);                    
 
 
                 // set the plot locations for the node
                 Point2D.Double plotsLocationA = new Point2D.Double(Double.parseDouble(transactionDetails[i][1])
                         ,Double.parseDouble(transactionDetails[i][4])) ;
 
                System.out.println("A[" + plotsLocationA.x + "," + plotsLocationA.y + "] \n" ) ;
 
                 // scale the node
                 // get the translated values
                 Point2D.Double plotsLocation0 = new Point2D.Double(( 72 
                     + ( (plotsLocationA.x - 1)* (570-72)/(noOfDaysInMonth-1) )),
                     ( 200  - 2 - ( (plotsLocationA.y - minMaxTransValues[0]) * 198.0/(minMaxTransValues[1]-minMaxTransValues[0]) ))) ;
 
                System.out.println("B[" + plotsLocation0.x + "," + plotsLocation0.y + "] \n"
                        + minMaxTransValues[0] + " - " + minMaxTransValues[1]) ;
 
                 String[] infor = {transactionDetails[i][2], 
                                 transactionDetails[i][4]}; 
 
                 // create the node
                 int[] transIDs = {Integer.parseInt(transactionDetails[i][0])};
                 nodes[i] = new GraphNode(transIDs , 
                         Node.TRANSACTION_ITEM_NODE, plotsLocation0, infor) ;
 
                 // add the node to the plot
                 plot1.addNode(nodes[i]);
             }
 
             /*
 
             // set the transaction IDs
             int[] transactionsID0 = {1234, 4567 } ;
             String[] info = {"Rice, Biscuits, Milk", "200"} ;
 
             int [] transactionsID1 = {2345};
             String[] info1 = {"Rice", "200"} ;
 
             int[] transactionsID2 = {1234, 4567 } ;
             String[] info2 = {"Rice, Biscuits, Milk", "200"} ;
 
             int [] transactionsID3 = {2345};
             String[] info3 = {"Rice", "200"} ;                                    
 
              // set the plot locations for the nodes
             Point2D.Double plotsLocationA = new Point2D.Double(12,100) ;
 
             // get the translated values
             Point2D.Double plotsLocation0 = new Point2D.Double(( 72 
                     + (plotsLocationA.x * (minMaxTransValues[1]-minMaxTransValues[0]-2)/noOfDaysInMonth )),
                     ( 200  - 2 - ( plotsLocationA.y * 198.0/(minMaxTransValues[1]-minMaxTransValues[0]-2) ))) ;
 
             // actual values for  node
             Point2D.Double plotsLocationB = new Point2D.Double(18,280) ;
 
             // translated values for a node
             Point2D.Double plotsLocation1 = new Point2D.Double(( 72 
                     + (plotsLocationB.x * (minMaxTransValues[1]-minMaxTransValues[0]-2)/noOfDaysInMonth )),
                     ( 200  - 2 - ( plotsLocationB.y * 198.0/(minMaxTransValues[1]-minMaxTransValues[0]-2) ))) ;
 
             // actual values for  node
             Point2D.Double plotsLocationC = new Point2D.Double(20,280) ;
 
             // translated values for a node
             Point2D.Double plotsLocation2 = new Point2D.Double(( 72 
                     + (plotsLocationC.x * (minMaxTransValues[1]-minMaxTransValues[0]-2)/noOfDaysInMonth )),
                     ( 200  - 2 - ( plotsLocationC.y * 198.0/(minMaxTransValues[1]-minMaxTransValues[0]-2) ))) ;
 
             // actual values for  node
             Point2D.Double plotsLocationD = new Point2D.Double(23,200) ;
 
             // translated values for a node
             Point2D.Double plotsLocation3 = new Point2D.Double(( 72 
                     + (plotsLocationD.x * (minMaxTransValues[1]-minMaxTransValues[0]-2)/noOfDaysInMonth )),
                     ( 200  - 2 - ( plotsLocationD.y * 198.0/(minMaxTransValues[1]-minMaxTransValues[0]-2) ))) ;
 
             // add all the variables into the nodes
             nodes[0] = new GraphNode(transactionsID0, Node.TRANSACTION_ITEM_NODE, plotsLocation0, info) ;
             nodes[1] = new GraphNode(transactionsID1, Node.TRANSACTION_ITEM_NODE, plotsLocation1, info1) ;
             nodes[2] = new GraphNode(transactionsID2, Node.TRANSACTION_ITEM_NODE, plotsLocation2, info1) ;
             nodes[3] = new GraphNode(transactionsID3, Node.TRANSACTION_ITEM_NODE, plotsLocation3, info1) ;
 
             // add the nodes to the plot
             plot1.addNode(nodes[0]);
             plot1.addNode(nodes[1]);
             plot1.addNode(nodes[2]);
             plot1.addNode(nodes[3]);
 
             */
 
             // add these to the array of plots
             allChartPlots[0] = plot1 ;
 
             // go to the debit plot
             System.out.println("Finished setting the model for the chart"); 
 
             // get the transactions for the selected user
             // for the obtained month and year
            // CenterPanel.setChartModel(currYearAndMonth[0], currYearAndMonth[1], 
             //        minMaxTransValues, allChartPlots);
         }        
     }
 }
