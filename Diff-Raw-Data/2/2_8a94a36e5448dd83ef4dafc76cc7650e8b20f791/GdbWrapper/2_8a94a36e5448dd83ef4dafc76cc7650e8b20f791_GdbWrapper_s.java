 package capstone.wrapper;
 
 import capstone.util.*;
 import java.util.*;
 import java.io.*;
 
 public class GdbWrapper extends Wrapper
 {
     public GdbWrapper(int userId, int debuggerId)
     {
         super();
 
         programTextFilename = "/tmp/program_" + userId + "_" + debuggerId + ".cpp";
         programBinaryFilename = "/tmp/program_" + userId + "_" + debuggerId + ".out";
 
         fromProgramFilename = "/tmp/fromprog" + userId + "_" + debuggerId;
         toProgramFilename = "/tmp/toprog" + userId + "_" + debuggerId;
     }
 
     private String programTextFilename;
     private String programBinaryFilename;
 
     private String fromProgramFilename;
     private String toProgramFilename;
 
     private Process debuggerProcess;
     private PrintStream toGdb;
     private Scanner fromGdb;
     private PrintStream toProgram;
     private BufferedInputStream fromProgram;
 
     @Override
     protected List<ProgramError> prepare(String programText)
     throws IOException, InterruptedException
     {
         FileWriter fileOut = new FileWriter(programTextFilename);
         fileOut.write(programText);
         fileOut.close();
 
         //String command = "g++ -g " + programTextFilename + " -o " + programBinaryFilename;
         ProcessBuilder builder = new ProcessBuilder("g++", "-g", programTextFilename, "-o", programBinaryFilename);
         builder.redirectErrorStream(true);
         Process compileProcess = builder.start();
         Scanner compilerOutput = new Scanner(compileProcess.getInputStream());
         int result = compileProcess.waitFor();
 
         String[] mkfifo1 = {"touch", fromProgramFilename}; // FIXME this is a stupid variable name
         String[] mkfifo2 = {"touch", toProgramFilename}; // FIXME this is a stupid variable name
         Runtime.getRuntime().exec(mkfifo1).waitFor();
         Runtime.getRuntime().exec(mkfifo2).waitFor();
 
         if (result != 0)
         {
             ArrayList<ProgramError> errors = new ArrayList<ProgramError>();
 
             while (compilerOutput.hasNextLine())
             {
                 String errorLine = compilerOutput.nextLine();
                 int errorIndex = errorLine.indexOf("error: ");
                 if (errorIndex != -1)
                 {
                     int firstColon = errorLine.indexOf(':');
                     int secondColon = errorLine.indexOf(':', firstColon + 1);
                     Scanner lineNumberScanner = new Scanner(errorLine.substring(firstColon + 1));
                     lineNumberScanner.useDelimiter(":");
                     int lineNumber = lineNumberScanner.nextInt();
                     String errorText = errorLine.substring(errorIndex + 7);
                     errors.add(new ProgramError(lineNumber, errorText));
                 }
             }
 
             return errors;
         }
 
         createGdbProcess();
 
         readUntilPrompt();
         String startCommand = "start 0 < " + toProgramFilename + " 1 > " + fromProgramFilename + " 2 > " + fromProgramFilename;
         write(startCommand);
         fromProgram = new BufferedInputStream(new FileInputStream(fromProgramFilename));
         toProgram = new PrintStream(new FileOutputStream(toProgramFilename));
         readUntilPrompt();
 
         return new ArrayList<ProgramError>();
     }
 
     @Override
     protected void killDebugger()
     {
         if (debuggerProcess != null)
         {
             debuggerProcess.destroy();
         }
     }
 
     @Override
     protected void cleanup()
     {
         try
         {
             if (toGdb != null)
             {
                 toGdb.close();
             }
 
             if (fromGdb != null)
             {
                 fromGdb.close();
             }
 
             if (toProgram != null)
             {
                 toProgram.close();
             }
 
             if (fromProgram != null)
             {
                 fromProgram.close();
             }
 
             safeDelete(programTextFilename);
             safeDelete(programBinaryFilename);
             safeDelete(fromProgramFilename);
             safeDelete(toProgramFilename);
         }
         catch (Exception exception)
         {
             exception.printStackTrace();
             // TODO log the failure here
         }
     }
 
     @Override
     protected void runProgram()
     throws IOException
     {
         write("c");
         String output = readUntilPrompt();
     }
 
     @Override
     protected String getStdOut()
     throws IOException
     {
         int numAvailable = fromProgram.available();
         if (numAvailable == 0)
         {
             return "";
         }
         else
         {
             byte[] bytes = new byte[numAvailable];
             fromProgram.read(bytes, 0, numAvailable);
             String output = new String(bytes);
             return output;
         }
     }
 
     @Override
     public void provideInput(String input)
     {
         toProgram.print(input);
         toProgram.flush();
     }
 
     @Override
     protected StackFrame getLocalValues()
     throws IOException
     {
        getStack()
         return null;
     }
 
     @Override
     protected List<StackFrame> getStack()
     throws IOException
     {
         /*List<StackFrame> retVal;
         
         write("bt");
         String stackNames = readUntilPrompt();
         String[] btLines = stackNames.split("\r\n|\r|\n");
         
         for (int depth = 0; depth < btLines.length; depth++)
         {
             write("frame" + depth);
             String stackFrame = readUntilPrompt();
             int nameEnd = stackFrame.indexOf(" (");
             int nameStart = stackFrame.lastIndexOf(" ", nameEnd);
             
             String stackName = stackFrame.substring(nameStart, nameEnd);
             String[] splitVariables = stackFrame.split(btLines[depth]);
             //splitVariables[1]/
             
         }*/
         
         return null;
     }
 
     @Override
     protected String evaluateExpression(String expression)
     throws IOException
     {
         write("print " + expression);
         String output = readUntilPrompt();
 
         int equalsIndex = output.indexOf('=');
         if (equalsIndex == -1)
         {
             return "error: expression not valid";
         }
         else
         {
             return output.substring(equalsIndex + 2, output.length() - 1);
         }
     }
 
     @Override
     protected void stepIn()
     throws IOException
     {
         write("step");
         readUntilPrompt();
     }
 
     @Override
     protected void stepOut()
     throws IOException
     {
         write("finish");
         String output = readUntilPrompt();
         if (output.equals("\"finish\" not meaningful in the outermost frame.\n"))
         {
             runProgram();
         }
     }
 
     @Override
     protected void stepOver()
     throws IOException
     {
         write("next");
         readUntilPrompt();
     }
 
     @Override
     protected void addBreakpoint(int lineNumber)
     throws IOException
     {
         write("break " + lineNumber);
         readUntilPrompt();
     }
 
     @Override
     protected int getLineNumber()
     throws IOException
     {
         int lineNumber = 0;
 
         write("where");
         String output = readUntilPrompt();
         Scanner outputScanner = new Scanner(output);
         outputScanner.useDelimiter(":");
 
         if (!outputScanner.hasNext())
             return 1;
 
         outputScanner.next();
         outputScanner.useDelimiter(":|\\p{javaWhitespace}+");
 
         if (!outputScanner.hasNextInt())
         {
             return 1;
         }
 
         return outputScanner.nextInt();
     }
 
     private void createGdbProcess()
     throws IOException
     {
         //String command = "gdb -q " + programBinaryFilename;
         ProcessBuilder builder = new ProcessBuilder("gdb", "-q", programBinaryFilename);
         builder.redirectErrorStream(true); // merges stdout, stderr
         debuggerProcess = builder.start();
         fromGdb = new Scanner(new BufferedInputStream(debuggerProcess.getInputStream()));
         toGdb = new PrintStream(debuggerProcess.getOutputStream());
     }
 
     private void write(String command)
     throws IOException
     {
         toGdb.println(command);
         toGdb.flush();
     }
 
     private String readUntilPrompt()
     throws IOException
     {
         fromGdb.useDelimiter("\\(gdb\\) ");
         String line = fromGdb.next();
         return line;
     }
 
     private void safeDelete(String filename)
     {
         if (filename != null)
         {
             try
             {
                 (new File(filename)).delete();
             }
             catch (Exception exception)
             {
                 exception.printStackTrace();
                 // TODO log it here
             }
         }
     }
 }
 
