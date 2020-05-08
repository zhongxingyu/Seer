 package main;
 
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.testng.Assert.assertEquals;
 
 public class DeadlockDetectorTest {
     private static final String[] SIMULATIONS = {"1 2 5 1 1 1 2 3 10 2 1 2 2",
             "3 3 5 1 1 1 2 3 2 2 1 2 2 5 1 1 1 2 3 2 2 1 2 2 4 1 3 3 5 2 3 3 2",
             "2 2 2 1 1 1 2 2 1 2 1 1",
             "3 3 6 1 1 3 3 1 2 3 3 2 1 2 2 6 1 2 3 3 1 3 3 3 2 2 2 3 6 1 3 3 3 1 1 3 3 2 3 2 1"};
     private static final String SECOND_SOLUTION = "All processes successfully terminated.\n" +
             "Process 1: run time = 6, ended at 11\n" +
             "Process 2: run time = 6, ended at 20\n" +
             "Process 3: run time = 9, ended at 18";
 
     @DataProvider(name = "testCases")
     public Object[][] createTestCases() {
         return new Object[][]{multipleSimulations(), traceOutput()};
     }
 
     private Object[] multipleSimulations() {
         List<String> inputs = new ArrayList<String>();
         List<String> outputs = new ArrayList<String>();
 
         inputs.add(SIMULATIONS[0]);
         outputs.add("All processes successfully terminated.\n" +
                 "Process 1: run time = 14, ended at 13");
         inputs.add(SIMULATIONS[1]);
         outputs.add(SECOND_SOLUTION);
         inputs.add(SIMULATIONS[2]);
         outputs.add("Deadlock detected at time 2 involving...\n" +
                "\tprocesses: 1 2\n\tresources: 2 1");
         inputs.add(SIMULATIONS[3]);
         outputs.add("Deadlock detected at time 12 involving...\n" +
                "\tprocesses: 2 1 3\n\tresources: 3 2 1");
 
         StringBuilder inputSb = new StringBuilder();
         StringBuilder outputSb = new StringBuilder();
 
         for (String input : inputs) {
             inputSb.append(input).append("\n");
         }
         int i = 1;
         for (String output : outputs) {
             outputSb.append("Simulation ").append(i++).append("\n")
                     .append(output).append("\n\n");
         }
 
         return new Object[]{null, inputSb.toString(), outputSb.toString()};
     }
 
     private Object[] traceOutput() {
         String output = "Simulation 1\n" +
                 "0: process 1: (1,1)\n" +
                 "\t(resource 1 allocated)\n" +
                 "1: process 2: (1,1)\n" +
                 "\t(resource 1 unavailable)\n" +
                 "1: process 3: (1,3)\n" +
                 "\t(resource 3 allocated)\n" +
                 "2: process 1: (1,2)\n" +
                 "\t(resource 2 allocated)\n" +
                 "3: process 3: (3,5)\n" +
                 "4: process 1: (3,2)\n" +
                 "5: process 3: (3,4)\n" +
                 "6: process 1: (3,1)\n" +
                 "7: process 3: (3,3)\n" +
                 "8: process 1: (2,1)\n" +
                 "\t(resource 1 released)\n" +
                 "\t(process 2 unblocked)\n" +
                 "9: process 3: (3,2)\n" +
                 "10: process 2: (1,1)\n" +
                 "\t(resource 1 allocated)\n" +
                 "11: process 1: (2,2)\n" +
                 "\t(resource 2 released)\n" +
                 "\t(process 1 terminated)\n" +
                 "12: process 3: (3,1)\n" +
                 "13: process 2: (1,2)\n" +
                 "\t(resource 2 allocated)\n" +
                 "14: process 3: (2,3)\n" +
                 "\t(resource 3 released)\n" +
                 "15: process 2: (3,2)\n" +
                 "16: process 3: (3,2)\n" +
                 "17: process 2: (3,1)\n" +
                 "18: process 3: (3,1)\n" +
                 "\t(process 3 terminated)\n" +
                 "19: process 2: (2,1)\n" +
                 "\t(resource 1 released)\n" +
                 "20: process 2: (2,2)\n" +
                 "\t(resource 2 released)\n" +
                 "\t(process 2 terminated)\n" +
                 SECOND_SOLUTION +
                 "\n\n";
 
         return new Object[]{"", SIMULATIONS[1], output};
     }
 
     @Test(dataProvider = "testCases")
     public void basicTest(String argument, String input, String expectedOutput) throws Exception {
         input += "\n0 0";
         InputStream systemInput = new ByteArrayInputStream(input.getBytes());
         System.setIn(systemInput);
         OutputStream systemOutput = new ByteArrayOutputStream();
         System.setOut(new PrintStream(systemOutput));
 
         DeadlockDetector.main(argument == null ? new String[]{} : new String[]{argument});
         assertEquals(systemOutput.toString(), expectedOutput);
     }
 }
