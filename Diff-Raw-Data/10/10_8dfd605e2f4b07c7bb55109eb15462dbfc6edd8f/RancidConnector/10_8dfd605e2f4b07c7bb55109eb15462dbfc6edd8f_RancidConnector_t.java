 package net.es.oscars.pss.connect;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 
 import org.apache.log4j.Logger;
 
 import net.es.oscars.pss.api.Connector;
 import net.es.oscars.pss.beans.PSSCommand;
 import net.es.oscars.pss.beans.PSSException;
 import net.es.oscars.pss.beans.config.CircuitServiceConfig;
 import net.es.oscars.pss.beans.config.GenericConfig;
 import net.es.oscars.pss.config.ConfigHolder;
 
 public class RancidConnector implements Connector {
     
     private static Logger log = Logger.getLogger(RancidConnector.class);
     private CircuitServiceConfig circuitServiceConfig = null;
     private String executable;
 
     
     public RancidConnector() {
     }
     
     public void setConfig(GenericConfig config) throws PSSException {
 
         if (config == null) {
             throw new PSSException("no config set!");
         } else if (config.getParams() == null) {
             throw new PSSException("login null");
         }
         HashMap<String, Object> params = config.getParams();
         
         this.executable = (String)params.get("executable");
         if (executable == null) {
             throw new PSSException("null executable config parameter");
         } else {
             executable = executable.trim();
         }
         
         if (executable.length() == 0) {
             throw new PSSException("no value set for executable");
         } else if (!executable.startsWith(File.separator)) {
             String err = "must set absolute path for rancid executable: ["+executable+"]";
             System.out.println(err);
             throw new PSSException(err);
         }
         circuitServiceConfig = ConfigHolder.getInstance().getBaseConfig().getCircuitService();
 
     }
 
     
     /**
      * Sends a configure command using RANCID to a router, and 
      * returns any output. Credentials are stored with RANCID.
      *
      * @param command string with command to send to router
      * @throws PSSException
      */
 
     
     public String sendCommand(PSSCommand command) throws PSSException {
         String deviceCommand = command.getDeviceCommand();
         String address = command.getDeviceAddress();
         if (deviceCommand == null) {
             throw new PSSException("null device command");
         } else if (address == null) {
             throw new PSSException("null device address");
         }
         log.debug("sendCommand deviceCommand:\n"+deviceCommand);
         log.debug("sendCommand address "+address);
         log.debug("deciding stub mode");
         if (circuitServiceConfig.isStub()) {
             log.debug("not sending command because in stub mode");
             return "";
         } else {
             log.debug("initiating command send");
             if (circuitServiceConfig.isLogRequest()) {
                 log.info("SENDING to "+address+" :\n\n"+command);
             }
             
             String response = "";
             try {
                 // a temp file 
                 File tmpFile = File.createTempFile("oscars-rancid", "txt");
                 String path = tmpFile.getAbsolutePath();
                 BufferedWriter outputStream = new BufferedWriter(new FileWriter(tmpFile));
                 // write command to temporary file
                 outputStream.write(deviceCommand);
                 outputStream.close();
                 
     
                 
                 log.info(executable+" -x " + path + " " + address);
                 
                 String cmd[] = { executable, "-x", path, address };
                 BufferedReader cmdOutput;
                 cmdOutput = this.runCommand(cmd);
                 String outputLine = null;
                 StringBuilder sb = new StringBuilder();
                 while ((outputLine = cmdOutput.readLine()) != null) {
                     sb.append(outputLine + "\n");
                 }
                 response = sb.toString();
                 cmdOutput.close();
                 tmpFile.delete();
             } catch (IOException ex) {
                 throw new PSSException(ex.getMessage());
             }
             if (circuitServiceConfig.isLogResponse()) {
                 log.info("RESPONSE from "+address+" :\n\n"+response);
             }
             log.info("sendCommand.finish to "+address);
             return response;
         }
     }
 
     /**
      * Sends a command using RANCID to the server, and gets back
      * output.
      *
      * @param cmd array with command and arguments to exec
      * @return cmdOutput BufferedReader for output from the router
      * @throws IOException
      * @throws PSSException
      */
     private BufferedReader runCommand(String[] cmd)
             throws IOException, PSSException {
 
         Process p = Runtime.getRuntime().exec(cmd);
         BufferedReader cmdOutput = new BufferedReader(new InputStreamReader(p.getInputStream()));
         BufferedReader cmdError  = new BufferedReader(new InputStreamReader(p.getErrorStream()));
         String errInfo = cmdError.readLine();
        int exitval;
        try {
            exitval = p.waitFor();
        } catch (InterruptedException ex) {
            log.warn(ex);
            cmdOutput.close();
            cmdError.close();
            throw new PSSException(ex);
        }
         if (exitval != 0) {
             String error = "RANCID command error:\n" + errInfo +"\n\n"+cmdOutput;
             log.warn(error);
             cmdOutput.close();
             cmdError.close();
             throw new PSSException(error);
         }
         cmdError.close();
         return cmdOutput;
     }
     public CircuitServiceConfig getCircuitServiceConfig() {
         return circuitServiceConfig;
     }
     public void setCircuitServiceConfig(CircuitServiceConfig circuitServiceConfig) {
         this.circuitServiceConfig = circuitServiceConfig;
     }
     public String getExecutable() {
         return executable;
     }
     public void setExecutable(String executable) {
         this.executable = executable;
     }
 
 }
