 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edacc.properties;
 
 import edacc.model.ComputationMethodDAO;
 import edacc.model.ComputationMethodDoesNotExistException;
 import edacc.model.DatabaseConnector;
 import edacc.model.ExperimentResult;
 import edacc.model.ExperimentResultDAO;
 import edacc.model.ExperimentResultHasProperty;
 import edacc.model.ExperimentResultHasPropertyDAO;
 import edacc.model.ExperimentResultNotInDBException;
 import edacc.model.InstanceDAO;
 import edacc.model.InstanceHasProperty;
 import edacc.model.InstanceHasPropertyDAO;
 import edacc.model.InstanceNotInDBException;
 import edacc.model.NoConnectionToDBException;
 import edacc.model.Property;
 import edacc.model.PropertyDAO;
 import edacc.model.PropertyNotInDBException;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.ObjectOutputStream;
 import java.io.OutputStreamWriter;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  *
  * @author rretz
  * @author dgall
  */
 public class PropertyComputationUnit implements Runnable {
 
     private ExperimentResultHasProperty erhp;
     private InstanceHasProperty ihp;
     private PropertyComputationController callback;
     private Property property;
     /**
      * Defines the maximum time the unit will wait for a value of an external program (in millis).
      * default: 10 sec
      */
     private static final int MAX_WAIT_TIME = 10000;
 
     PropertyComputationUnit(ExperimentResultHasProperty erhp, PropertyComputationController callback) {
         this.property = erhp.getProperty();
         this.erhp = erhp;
         this.callback = callback;
     }
 
     PropertyComputationUnit(InstanceHasProperty ihp, PropertyComputationController callback) {
         this.ihp = ihp;
         this.callback = callback;
         this.property = ihp.getProperty();
     }
 
     @Override
     public void run() {
         if (erhp != null) {
             try {
                 Property property = erhp.getProperty();
                 switch (property.getPropertySource()) {
                     case LauncherOutput:
                         compute(ExperimentResultDAO.getLauncherOutput(erhp.getExpResult()).getBinaryStream());
                         break;
                     case SolverOutput:
                         compute(ExperimentResultDAO.getSolverOutput(erhp.getExpResult()).getBinaryStream());
                         break;
                     case VerifierOutput:
                         compute(ExperimentResultDAO.getVerifierOutput(erhp.getExpResult()).getBinaryStream());
                         break;
                     case WatcherOutput:
                         compute(ExperimentResultDAO.getWatcherOutput(erhp.getExpResult()).getBinaryStream());
                         break;
                 }
             } catch (NoConnectionToDBException ex) {
                 Logger.getLogger(PropertyComputationUnit.class.getName()).log(Level.SEVERE, null, ex);
             } catch (FileNotFoundException ex) {
                 Logger.getLogger(PropertyComputationUnit.class.getName()).log(Level.SEVERE, null, ex);
             } catch (IOException ex) {
                 Logger.getLogger(PropertyComputationUnit.class.getName()).log(Level.SEVERE, null, ex);
             } catch (ExperimentResultNotInDBException ex) {
                 Logger.getLogger(PropertyComputationUnit.class.getName()).log(Level.SEVERE, null, ex);
             } catch (InstanceNotInDBException ex) {
                 Logger.getLogger(PropertyComputationUnit.class.getName()).log(Level.SEVERE, null, ex);
             } catch (ComputationMethodDoesNotExistException ex) {
                 Logger.getLogger(PropertyComputationUnit.class.getName()).log(Level.SEVERE, null, ex);
             } catch (ErrorInExternalProgramException ex) {
                 Logger.getLogger(PropertyComputationUnit.class.getName()).log(Level.SEVERE, null, ex);
             } catch (SQLException ex) {
                 Logger.getLogger(PropertyComputationUnit.class.getName()).log(Level.SEVERE, null, ex);
             } catch (Exception ex) {
                 Logger.getLogger(PropertyComputationUnit.class.getName()).log(Level.SEVERE, null, ex);
                 callback.exceptionCausedByThread(ex);
             }
         } else if (ihp != null) {
             try {
                 switch (property.getPropertySource()) {
                     case Instance:
                         try {
                             InputStream inputStream = InstanceDAO.getBinary(ihp.getInstance());
                             compute(inputStream);
                             inputStream.close();
                         } catch (InstanceNotInDBException ex) {
                             Logger.getLogger(PropertyComputationUnit.class.getName()).log(Level.SEVERE, null, ex);
                         }
                         break;
                     case InstanceName:
                         parseInstanceName();
                         break;
                     case ExperimentResults:
                         computeFromExperimentResults();
                         break;
                 }
             } catch (NoConnectionToDBException ex) {
                 Logger.getLogger(PropertyComputationUnit.class.getName()).log(Level.SEVERE, null, ex);
             } catch (FileNotFoundException ex) {
                 Logger.getLogger(PropertyComputationUnit.class.getName()).log(Level.SEVERE, null, ex);
             } catch (IOException ex) {
                 Logger.getLogger(PropertyComputationUnit.class.getName()).log(Level.SEVERE, null, ex);
             } catch (InstanceNotInDBException ex) {
                 Logger.getLogger(PropertyComputationUnit.class.getName()).log(Level.SEVERE, null, ex);
             } catch (ComputationMethodDoesNotExistException ex) {
                 Logger.getLogger(PropertyComputationUnit.class.getName()).log(Level.SEVERE, null, ex);
             } catch (ErrorInExternalProgramException ex) {
                 Logger.getLogger(PropertyComputationUnit.class.getName()).log(Level.SEVERE, null, ex);
             } catch (SQLException ex) {
                 Logger.getLogger(PropertyComputationUnit.class.getName()).log(Level.SEVERE, null, ex);
             } catch (Exception ex) {
                 callback.exceptionCausedByThread(ex);
             }
         }
         callback.callback();
     }
     public static final Object sync = new Object();
 
     private void computeFromExperimentResults() throws NoConnectionToDBException, SQLException, ComputationMethodDoesNotExistException, FileNotFoundException, IOException, InstanceNotInDBException, ErrorInExternalProgramException {
         if (ihp == null) {
             // only instance properties can be computed from experiment results
             return;
         }
         File bin;
         synchronized (sync) {
             bin = ComputationMethodDAO.getBinaryOfComputationMethod(property.getComputationMethod());
             bin.setExecutable(true);
         }
         System.out.println("java -jar " + bin.getAbsolutePath() + " " + property.getComputationMethodParameters());
         // only java files, we pipe objects :-)
         Process p = Runtime.getRuntime().exec("java -jar " + bin.getAbsolutePath() + " " + property.getComputationMethodParameters());
 
         // The std output stream of the external program (-> output of the program). We read the calculated value from this stream.
         BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
         // The error stream of the program
         BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
 
         try {
             ArrayList<ExperimentResult> er = ExperimentResultDAO.getAllByInstanceId(ihp.getInstance().getId());
            System.out.println("found " + er.size() + " results for this instance");            ObjectOutputStream os = new ObjectOutputStream(p.getOutputStream());
             os.writeUnshared(er);
             os.flush();
             os.close();
         } catch (Exception e) {
             // TODO: error
             e.printStackTrace();
         }
 
         // check, if already an error occured
         if (err.ready()) {
            throw new ErrorInExternalProgramException(err.readLine());
         }
         // check, if program already has terminated
         try {
             int exit;
             if ((exit = p.exitValue()) != 0) {
                 throw new ErrorInExternalProgramException("External program exited with errors! Exit value: " + exit);
             }
         } catch (IllegalThreadStateException e) {
             // do nothing if program is still running
         }
 
         long time = System.currentTimeMillis();
         while (System.currentTimeMillis() - time < MAX_WAIT_TIME && !in.ready()) {
             try {
                 Thread.sleep(100);
             } catch (InterruptedException ex) {
                 break;
             }
         }
         // if no value is available after waitng time, kill the program
         if (!in.ready()) {
             p.destroy();
             throw new ErrorInExternalProgramException("Time limit of external calculation exceeded! The external program has been terminated!");
         }
         // Read first line of program output
         String value = in.readLine();
         System.out.println("Value: " + value);
         // set the value and save it
         ihp.setValue(value);
         InstanceHasPropertyDAO.save(ihp);
     }
 
     private void compute(InputStream input) throws FileNotFoundException, IOException, SQLException, NoConnectionToDBException, InstanceNotInDBException, ComputationMethodDoesNotExistException, ErrorInExternalProgramException {
         if (input == null) {
             return;
         }
         if (property.getComputationMethod() != null) {
 
             // parse instance file (external program call)
             if (ihp != null) {
                 File bin;
                 synchronized (sync) {
                     bin = ComputationMethodDAO.getBinaryOfComputationMethod(property.getComputationMethod());
                 }
                 bin.setExecutable(true);
                 System.out.println(bin.getAbsolutePath());
                 String prefix = "";
                 if (System.getProperty("os.name") != null && System.getProperty("os.name").contains("Windows")) {
                     prefix = "cmd /c ";
                 }
                 if (bin.getAbsolutePath().endsWith(".jar")) {
                     prefix += "java -jar ";
                 }
                 Process p = Runtime.getRuntime().exec(prefix + bin.getAbsolutePath() + " " + property.getComputationMethodParameters());
                 InputStream is = input;
                 InputStreamReader ir = new InputStreamReader(is);
                 BufferedReader instanceReader = new BufferedReader(ir);
                 // The std input stream of the external program. We pipe the content of the instance file into that stream
                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
                 // The std output stream of the external program (-> output of the program). We read the calculated value from this stream.
                 BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                 // The error stream of the program
                 BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
 
 
                 // pipe the content of the instance file to the input of the external program
                 try {
                     String s;
                     while ((s = instanceReader.readLine()) != null) {
                         out.write(s);
                         out.newLine();
                     }
                 } catch (IOException e) {
                     // if a program stops reading from the stream, stop writing to it but show no error. //!Otherwise show an error message
                 } finally {
                     // Close output stream, after whole instance file has been written to program input
                     try {
                         instanceReader.close();
                         ir.close();
                         is.close();
                         out.close();
                     } catch (Exception e) {
                     }
                 }
 
 
                 // check, if already an error occured
                 if (err.ready()) {
                     in.close();
                     String error = err.readLine();
                     err.close();
                     throw new ErrorInExternalProgramException(error == null ? "unknown error" : error);
                 }
                 // check, if program already has terminated
                 try {
                     int exit;
                     if ((exit = p.exitValue()) != 0) {
                         in.close();
                         err.close();
                         throw new ErrorInExternalProgramException("External program exited with errors! Exit value: " + exit);
                     }
                 } catch (IllegalThreadStateException e) {
                     // do nothing if program is still running
                 }
 
                 /**
                  * Read the program output and save it as value of the property
                  * for the given instance.
                  * IMPORTANT!
                  * The value will be saved as a String in the DB and no
                  * conversion will be done!
                  * When loading the value from the DB, the method
                  * PropertyValueType.getJavaRepresentation(String) will be called
                  * on that value for the PropertyValueType of the property (so
                  * the String will be converted to the correct Java type).
                  */
                 // wait some time till program output is available (maybe external program needs some time to calculate the property)
                 long time = System.currentTimeMillis();
                 while (System.currentTimeMillis() - time < MAX_WAIT_TIME && !in.ready()) {
                     try {
                         Thread.sleep(100);
                     } catch (InterruptedException ex) {
                         break;
                     }
                 }
                 // if no value is available after waitng time, kill the program
                 if (!in.ready()) {
                     err.close();
                     in.close();
                     p.destroy();
                     throw new ErrorInExternalProgramException("Time limit of external calculation exceeded! The external program has been terminated!");
                 }
                 // Read first line of program output
                 String value = in.readLine();
 
                 // set the value and save it
                 if(value.isEmpty()){
                     value = null;
                 }
                 ihp.setValue(value);
                 System.out.println(value);
                 InstanceHasPropertyDAO.save(ihp);
                 err.close();
                 in.close();
             } else if (erhp != null) {
                 File bin = ComputationMethodDAO.getBinaryOfComputationMethod(property.getComputationMethod());
                 bin.setExecutable(true);
                 Process p = Runtime.getRuntime().exec(bin.getAbsolutePath());
                 BufferedReader outputFileReader = new BufferedReader(new InputStreamReader(input));
                 // The std input stream of the external program. We pipe the content of the Blob b
                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
                 // The std output stream of the external program (-> output of the program). We read the calculated value from this stream.
                 BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
 
                 // pipe the content of the output file to the input of the external program
                 try {
                     int i;
                     while ((i = outputFileReader.read()) != -1) {
                         out.write(i);
                     }
                 } catch (IOException e) {
                     if (!e.getMessage().contains("Broken pipe")) {
                         throw e;
                     }
                 }
                 Vector<String> value = new Vector<String>();
                 while (in.ready()) {
                     value.add(in.readLine());
                 }
                 erhp.setValue(value);
                 ExperimentResultHasPropertyDAO.save(erhp);
             }
         } else if (property.getRegularExpression() != null) {
             Vector<String> res = new Vector<String>();
             InputStream is = input;
             InputStreamReader ir = new InputStreamReader(is);
             BufferedReader buf = new BufferedReader(ir);
             String tmp;
             Vector<String> toAdd = new Vector<String>();
             while ((tmp = buf.readLine()) != null) {
                 if (!(toAdd = parse(tmp)).isEmpty()) {
                     res.addAll(toAdd);
                     if (!property.isMultiple() || ihp != null) {
                         break;
                     }
                 }
             }
             if (ihp != null) {
                 if (!res.isEmpty()) {
                     ihp.setValue(res.firstElement());
                     InstanceHasPropertyDAO.save(ihp);
                 }
 
             } else if (erhp != null) {
                 erhp.setValue(res);
                 ExperimentResultHasPropertyDAO.save(erhp);
             }
             buf.close();
             ir.close();
             is.close();
         }
     }
 
     private Vector<String> parse(String toParse) {
         Vector<String> res = new Vector<String>();
         for (int i = 0; i < property.getRegularExpression().size(); i++) {
             Pattern pat = Pattern.compile(property.getRegularExpression().get(i));
             Matcher m = pat.matcher(toParse);
             while (m.find()) {
                 res.add(m.group(1));
                 if (!property.isMultiple() || ihp != null) {
                     return res;
                 }
             }
         }
         return res;
     }
 
     private void parseInstanceName() {
         if (ihp != null) {
             Vector<String> res = parse(ihp.getInstance().getName());
             if (!res.isEmpty()) {
                 ihp.setValue(res.firstElement());
                 try {
                     InstanceHasPropertyDAO.save(ihp);
                 } catch (SQLException ex) {
                     Logger.getLogger(PropertyComputationUnit.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
 
         }
 
     }
 
     public static void main(String[] args) {
         try {
             DatabaseConnector.getInstance().connect("edacc.informatik.uni-ulm.de", 3306, "edacc", "EDACC2", "edaccteam", false, false, 1);
 
             PropertyComputationUnit unit = new PropertyComputationUnit(InstanceHasPropertyDAO.createInstanceHasInstanceProperty(InstanceDAO.getById(1), PropertyDAO.getById(1)), null);
             unit.compute(null);
         } catch (NoConnectionToDBException ex) {
             Logger.getLogger(PropertyComputationUnit.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SQLException ex) {
             Logger.getLogger(PropertyComputationUnit.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(PropertyComputationUnit.class.getName()).log(Level.SEVERE, null, ex);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
