 package client;
 /*
  * jTPCC - Open Source Java implementation of a TPC-C like benchmark
  *
  * Copyright (C) 2003, Raul Barbosa
  * Copyright (C) 2004-2006, Denis Lussier
  *
  */
 
 import java.io.*;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.text.SimpleDateFormat;
 import java.util.Properties;
 import java.util.Random;
 
 public class jTPCCConsole implements jTPCCConfig, TerminalEndedTransactionListener {
     private jTPCCTerminal[] terminals;
     private String[] terminalNames;
     private boolean terminalsBlockingExit = false;
     private Random random;
     private long terminalsStarted = 0, sessionCount = 0, transactionCount;
 
     private long newOrderCounter, sessionStartTimestamp, sessionEndTimestamp, sessionNextTimestamp=0, sessionNextKounter=0;
     private long sessionEndTargetTime = -1, fastNewOrderCounter, recentTpmC=0;
     private boolean signalTerminalsRequestEndSent = false, databaseDriverLoaded = false;
 
     private FileOutputStream fileOutputStream;
     private PrintStream printStreamReport;
     private String sessionStart, sessionEnd;
     private Properties ini;
 
 
     public static void main(String args[])
     {
         new jTPCCConsole();
     }
 
     public jTPCCConsole()
     {
         // load the ini file
        Properties ini = new Properties();
         try {
             ini.load( new FileInputStream(System.getProperty("prop")));
         } catch (IOException e) {
             System.out.println("could not load properties file");
         }
 
         // display the values we need
         System.out.println("driver=" + ini.getProperty("driver"));
         System.out.println("conn=" + ini.getProperty("conn"));
         System.out.println("user=" + ini.getProperty("user"));
         System.out.println("password=******");
 
         this.random = new Random(System.currentTimeMillis());
 
         this.createTerminals();
         this.startTransactions();
     }
 
     public void createTerminals()
     {
         fastNewOrderCounter = 0;
         try
         {
             String driver = ini.getProperty("driver", defaultDriver);
             printMessage("Loading database driver: \'" + driver + "\'...");
             Class.forName(driver);
             databaseDriverLoaded = true;
         }
         catch(Exception ex)
         {
             errorMessage("Unable to load the database driver!");
             databaseDriverLoaded = false;
         }
 
         if(databaseDriverLoaded)
         {
             try
             {
                 boolean limitIsTime = defaultRadioTime;
                 int numTerminals = -1, transactionsPerTerminal = -1, numWarehouses = -1;
                 int paymentWeightValue = -1, orderStatusWeightValue = -1, deliveryWeightValue = -1, stockLevelWeightValue = -1;
                 long executionTimeMillis = -1;
 
                 try
                 {
                     numWarehouses = Integer.parseInt(defaultNumWarehouses);
                     if(numWarehouses <= 0)
                         throw new NumberFormatException();
                 }
                 catch(NumberFormatException e1)
                 {
                     errorMessage("Invalid number of warehouses!");
                     throw new Exception();
                 }
 
                 try
                 {
                     numTerminals = Integer.parseInt(defaultNumTerminals);
                     if(numTerminals <= 0 || numTerminals > 10*numWarehouses)
                         throw new NumberFormatException();
                 }
                 catch(NumberFormatException e1)
                 {
                     errorMessage("Invalid number of terminals!");
                     throw new Exception();
                 }
 
                 boolean debugMessages = defaultDebugMessages;
 
                 if(limitIsTime)
                 {
                     try
                     {
                         executionTimeMillis = Long.parseLong(defaultMinutes) * 60000;
                         if(executionTimeMillis <= 0)
                             throw new NumberFormatException();
                     }
                     catch(NumberFormatException e1)
                     {
                         errorMessage("Invalid number of minutes!");
                         throw new Exception();
                     }
                 }
                 else
                 {
                     try
                     {
                         transactionsPerTerminal = Integer.parseInt(defaultTransactionsPerTerminal);
                         if(transactionsPerTerminal <= 0)
                             throw new NumberFormatException();
                     }
                     catch(NumberFormatException e1)
                     {
                         errorMessage("Invalid number of transactions per terminal!");
                         throw new Exception();
                     }
                 }
 
                 try
                 {
                     paymentWeightValue = Integer.parseInt(defaultPaymentWeight);
                     orderStatusWeightValue = Integer.parseInt(defaultOrderStatusWeight);
                     deliveryWeightValue = Integer.parseInt(defaultDeliveryWeight);
                     stockLevelWeightValue = Integer.parseInt(defaultStockLevelWeight);
 
                     if(paymentWeightValue < 0 || orderStatusWeightValue < 0 || deliveryWeightValue < 0 || stockLevelWeightValue < 0)
                         throw new NumberFormatException();
                 }
                 catch(NumberFormatException e1)
                 {
                     errorMessage("Invalid number in mix percentage!");
                     throw new Exception();
                 }
 
                 if(paymentWeightValue + orderStatusWeightValue + deliveryWeightValue + stockLevelWeightValue > 100)
                 {
                     errorMessage("Sum of mix percentage parameters exceeds 100%!");
                     throw new Exception();
                 }
 
                 newOrderCounter = 0;
                 printMessage("Session #" + (++sessionCount) + " started!");
                 if(!limitIsTime)
                     printMessage("Creating " + numTerminals + " terminal(s) with " + transactionsPerTerminal + " transaction(s) per terminal...");
                 else
                     printMessage("Creating " + numTerminals + " terminal(s) with " + (executionTimeMillis/60000) + " minute(s) of execution...");
                 printMessage("Transaction Weights: " + (100 - (paymentWeightValue + orderStatusWeightValue + deliveryWeightValue + stockLevelWeightValue)) + "% New-Order, " + paymentWeightValue + "% Payment, " + orderStatusWeightValue + "% Order-Status, " + deliveryWeightValue + "% Delivery, " + stockLevelWeightValue + "% Stock-Level");
 
                 String reportFileName = reportFilePrefix + getFileNameSuffix() + ".txt";
                 fileOutputStream = new FileOutputStream(reportFileName);
                 printStreamReport = new PrintStream(fileOutputStream);
                 printStreamReport.println("Number of Terminals\t" + numTerminals);
                 printStreamReport.println("\nTerminal\tHome Warehouse");
                 printMessage("A complete report of the transactions will be saved to the file \'" + reportFileName + "\'");
 
                 terminals = new jTPCCTerminal[numTerminals];
                 terminalNames = new String[numTerminals];
                 terminalsStarted = numTerminals;
                 try
                 {
                     String database = ini.getProperty("conn", defaultDatabase);
                     String username = ini.getProperty("user", defaultUsername);
                     String password = ini.getProperty("password", defaultPassword);
 
                     int[][] usedTerminals = new int[numWarehouses][10];
                     for(int i = 0; i < numWarehouses; i++)
                         for(int j = 0; j < 10; j++)
                             usedTerminals[i][j] = 0;
 
                     for(int i = 0; i < numTerminals; i++)
                     {
                         int terminalWarehouseID;
                         int terminalDistrictID;
                         do
                         {
                             terminalWarehouseID = (int)randomNumber(1, numWarehouses);
                             terminalDistrictID = (int)randomNumber(1, 10);
                         }
                         while(usedTerminals[terminalWarehouseID-1][terminalDistrictID-1] == 1);
                         usedTerminals[terminalWarehouseID-1][terminalDistrictID-1] = 1;
 
                         String terminalName = terminalPrefix + (i>=9 ? ""+(i+1) : "0"+(i+1));
                         Connection conn = null;
                         printMessage("Creating database connection for " + terminalName + "...");
                         conn = DriverManager.getConnection(database, username, password);
                         conn.setAutoCommit(false);
                        JOutputArea terminalOutputArea = new JOutputArea();
                        long maxChars = 150000/numTerminals;
                        if(maxChars > JOutputArea.DEFAULT_MAX_CHARS) maxChars = JOutputArea.DEFAULT_MAX_CHARS;
                        if(maxChars < 2000) maxChars = 2000;
                        terminalOutputArea.setMaxChars(maxChars);
                         jTPCCTerminal terminal = new jTPCCTerminal(terminalName, terminalWarehouseID, terminalDistrictID, conn, transactionsPerTerminal, new PrintOutputStream(System.out), new PrintOutputStream(System.err), debugMessages, paymentWeightValue, orderStatusWeightValue, deliveryWeightValue, stockLevelWeightValue, numWarehouses, this);
                         terminals[i] = terminal;
                         terminalNames[i] = terminalName;
                         printStreamReport.println(terminalName + "\t" + terminalWarehouseID);
                     }
 
                     sessionEndTargetTime = executionTimeMillis;
                     signalTerminalsRequestEndSent = false;
 
                     printStreamReport.println("\nTransaction\tWeight\n% New-Order\t" + (100 - (paymentWeightValue + orderStatusWeightValue + deliveryWeightValue + stockLevelWeightValue)) + "\n% Payment\t" + paymentWeightValue + "\n% Order-Status\t" + orderStatusWeightValue + "\n% Delivery\t" + deliveryWeightValue + "\n% Stock-Level\t" + stockLevelWeightValue);
                     printStreamReport.println("\n\nTransaction Number\tTerminal\tType\tExecution Time (ms)\t\tComment");
 
                     printMessage("Created " + numTerminals + " terminal(s) successfully!");
                 }
                 catch(Exception e1)
                 {
                     try
                     {
                         printStreamReport.println("\nThis session ended with errors!");
                         printStreamReport.close();
                         fileOutputStream.close();
                     }
                     catch(IOException e2)
                     {
                         errorMessage("An error occurred writing the report!");
                     }
 
                     errorMessage("An error occurred!");
                     StringWriter stringWriter = new StringWriter();
                     PrintWriter printWriter = new PrintWriter(stringWriter);
                     e1.printStackTrace(printWriter);
                     printWriter.close();
                     throw new Exception();
                 }
 
             }
             catch(Exception ex)
             {
             }
         }
 
     }
 
     public void startTransactions()
     {
         sessionStart = getCurrentTime();
         sessionStartTimestamp = System.currentTimeMillis();
         sessionNextTimestamp = sessionStartTimestamp;
         if(sessionEndTargetTime != -1)
             sessionEndTargetTime += sessionStartTimestamp;
 
         synchronized(terminals)
         {
             printMessage("Starting all terminals...");
             transactionCount = 1;
             for(int i = 0; i < terminals.length; i++)
                 (new Thread(terminals[i])).start();
         }
 
         printMessage("All terminals started executing " + sessionStart);
     }
 
     public void stopTransactions()
     {
         signalTerminalsRequestEnd(false);
     }
 
     private void signalTerminalsRequestEnd(boolean timeTriggered)
     {
         synchronized(terminals)
         {
             if(!signalTerminalsRequestEndSent)
             {
                 if(timeTriggered)
                     printMessage("The time limit has been reached.");
                 printMessage("Signalling all terminals to stop...");
                 signalTerminalsRequestEndSent = true;
 
                 for(int i = 0; i < terminals.length; i++)
                     if(terminals[i] != null)
                         terminals[i].stopRunningWhenPossible();
 
                 printMessage("Waiting for all active transactions to end...");
             }
         }
     }
 
     public void signalTerminalEnded(jTPCCTerminal terminal, long countNewOrdersExecuted)
     {
         synchronized(terminals)
         {
             boolean found = false;
             terminalsStarted--;
             for(int i = 0; i < terminals.length && !found; i++)
             {
                 if(terminals[i] == terminal)
                 {
                     terminals[i] = null;
                     terminalNames[i] = "(" + terminalNames[i] + ")";
                     newOrderCounter += countNewOrdersExecuted;
                     found = true;
                 }
             }
         }
 
         if(terminalsStarted == 0)
         {
             sessionEnd = getCurrentTime();
             sessionEndTimestamp = System.currentTimeMillis();
             sessionEndTargetTime = -1;
             printMessage("All terminals finished executing " + sessionEnd);
             endReport();
             terminalsBlockingExit = false;
             printMessage("There were errors on this session!");
             printMessage("Session #" + sessionCount + " finished!");
         }
     }
 
     public void signalTerminalEndedTransaction(String terminalName, String transactionType, long executionTime, String comment, int newOrder)
     {
         if(comment == null) comment = "None";
 
         try
         {
             synchronized(printStreamReport)
             {
                 printStreamReport.println("" + transactionCount + "\t" + terminalName + "\t" + transactionType + "\t" + executionTime + "\t\t" + comment);
                 transactionCount++;
                 fastNewOrderCounter += newOrder;
             }
         }
         catch(Exception e)
         {
             errorMessage("An error occurred writing the report!");
         }
 
         if(sessionEndTargetTime != -1 && System.currentTimeMillis() > sessionEndTargetTime)
         {
             signalTerminalsRequestEnd(true);
         }
 
     }
 
     private void endReport()
     {
         try
         {
             printStreamReport.println("\n\nMeasured tpmC\t=60000*" + newOrderCounter + "/" + (sessionEndTimestamp - sessionStartTimestamp));
             printStreamReport.println("\nSession Start\t" + sessionStart + "\nSession End\t" + sessionEnd);
             printStreamReport.println("Transaction Count\t" + (transactionCount-1));
             printStreamReport.close();
             fileOutputStream.close();
         }
         catch(IOException e)
         {
             errorMessage("An error occurred writing the report!");
         }
     }
 
 
     private void printMessage(String message)
     {
         if(OUTPUT_MESSAGES) System.out.println("[BenchmarkSQL] " + message);
     }
 
     private void errorMessage(String message)
     {
         System.out.println("[ERROR] " + message);
     }
 
     private void exit()
     {
         if(!terminalsBlockingExit)
         {
             System.exit(0);
         }
         else
         {
             printMessage("Disable all terminals before quitting!");
         }
     }
 
     private long randomNumber(long min, long max)
     {
         return (long)(random.nextDouble() * (max-min+1) + min);
     }
 
     private String getCurrentTime()
     {
         return dateFormat.format(new java.util.Date());
     }
 
     private String getFileNameSuffix()
     {
         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
         return dateFormat.format(new java.util.Date());
     }
 }
