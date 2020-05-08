 package com.loukides.jl.transceivers;
 import com.loukides.jl.util.*;
 import com.loukides.jl.jl.*;
 
 import java.util.*;
 import gnu.io.*; // Change to gnu.io if using rxtx for communications
 import java.io.*;
 
 import java.text.NumberFormat;
 import java.text.DecimalFormat;
 import java.text.FieldPosition;
 
 /**
 A class to implement Hamlib rig control for jl
 */
 public class Hamlib extends AbstractTransceiver {
 
   private InputStream is;
   private OutputStream os;
   private DecimalFormat frequencyFormat = new DecimalFormat("00000000000");
   private DecimalFormat memoryFormat = new DecimalFormat("00");
   private FieldPosition pos = new FieldPosition(NumberFormat.INTEGER_FIELD);
   private String commandName = "";
  private String commandArgs[] = [];
 
   public Hamlib() {}
 
   public void setProperties(Properties p) throws Exception {
      super.setProperties(p);
      commandName = p.getProperty("transceiver.command", "rigctl -m 122 -r /dev/ttyUSB0 -s 4800");
      commandArgs = commandName.split(" \t\n\r\f");
   }
 
   private String runCommand(List<String> args) throws Exception {
     List commands = Arrays.asList(commandArgs);
     commands.addAll(args);
     ProcessBuilder pb = new ProcessBuilder(commands);
     Process proc = pb.start();
     InputStream is = proc.getInputStream();
     String line;
     StringBuilder sb = new StringBuilder();
     BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
     if ((line = reader.readLine()) != null) {
         sb.append(line);
     }
     /*
     while ((line = reader.readLine()) != null) {
         sb.append(line).append("\n");
     }
     */
     is.close();
     int exitcode = proc.waitFor();
     if (exitcode != 0) {
         throw new Exception("Nonzero exit code from command");
     }
     return sb.toString();
   }
 
 /**
 Store the current state of the rig in one of its memories.
 */
    public void storeMemory(int mem) {
      List args = new ArrayList<String>;
       args.add("set_mem");
       args.add(Integer.toString(mem));
 
       try {
          runCommand(args);
       } catch (Exception ee) {
          ee.printStackTrace();
       }
    }
 
 /**
 Recalls the current state of the rig from one of its memories.
 Note this may? different behavior than the jl docs, since, for
 example, it will remember if the rig is in split, etc.
 */
    public void setVFOFromMemory(int mem) {
      List args = newArrayList<String>;
       args.add("get_mem");
       args.add(Integer.toString(mem));
       
       try {
          runCommand(args);
       } catch (Exception ee) {
          ee.printStackTrace();
       }
    }
 
 /**
 Set transmit frequency for split operation.
 @param f the transmit frequency in KHz. If zero cancel split.
 */
 /*
    public void setSplit(float f) {
       List args = new ArrayList<String>;
       args.add("
       StringBuffer frequencyCommand;
       int irx = getRxVFO();
       if (f == 0.0) {
          setRxVFO(irx); //cancel split
          return;
       }
       frequencyCommand = new StringBuffer("ft");
       if (irx == 0) {
          frequencyCommand.append("1;fb");
       } else {
          frequencyCommand.append("0;fa");
       }
       frequencyFormat.format((long) (f*1000.0f),frequencyCommand,pos);
       frequencyCommand.append(";");
       try {
          os.write(frequencyCommand.toString().getBytes());
          os.flush();
       } catch (Exception ee) {
          ee.printStackTrace();
       }
    }
 */
 /**
 Cancel split and set the current receive VFO to f.
 @param f The frequency in KHz.
 */
    public void setFrequency(float f) {
       long freq = (long) (f * 1000.0f);
      List args = newArrayList<String>;
       args.add("set_freq");
       args.add(Long.toString(freq));
       try {
          runCommand(args);
       } catch (Exception ee) {
          ee.printStackTrace();
       }
    }
 
 /**
 Return 0 if the current receive VFO is A, 1 if B
 */
 /*
    public int getRxVFO() {
       byte[] b = new byte[4];
       try {
          while (is.available() > 0) is.read();
          os.write("fr;".getBytes());
          os.flush();
          for (int i=0;i<4;i++) {
             b[i] = (byte) is.read();
          }
       } catch (Exception ee) {
          ee.printStackTrace();
       }
       return (int) (b[2]-0x30); //0 for VFO-A, 1 for VFO-B
    }
 */
 /**
 Set the current receive VFO, cancelling any split operation.
 @param vfo 0 for VFO A, 1 for VFO B
 */
 /*
    public void setRxVFO(int vfo) { //cancels split
       byte[] b = new byte[4];
       b = "frX;".getBytes();
       b[2] = (byte) ((vfo == 0)?0x30:0x31);
       try {
          while (is.available() > 0) is.read();
          os.write(b);
          os.flush();
       } catch (Exception ee) {
          ee.printStackTrace();
       }
    }
 */
 /**
 Get the frequency in KHz of the current receive VFO.
 */
    public float getFrequency() {
      List args = new ArrayList<String>;
       args.add("get_freq");
       try {
          String result = runCommand(args);
         Long l = Long.parseLong(result.trim());
         return ((l.floatValue()) / 1000.0f);
       } catch (Exception ee) {
          ee.printStackTrace();
       }
  } 
 
 /**
 Get the Mode. The possible returned values are CW, PH, or RY.
 */
 /*
   public String getMode() {
       List args = new ArrayList<String>;
       
       byte[] b = new byte[4];
       try {
          while (is.available() > 0) is.read();
          os.write("md;".getBytes());
          os.flush();
          for (int i=0;i<4;i++) {
             b[i] = (byte) is.read();
          }
       } catch (Exception ee) {
          ee.printStackTrace();
       }
       int i = new Integer(new String(b,2,1)).intValue();
       switch (i) {
          case 1: //lsb
          case 2: //usb
             return "PH";
          case 3: //cw
          case 7: //cw reverse
             return "CW";
          case 6: //rtty
          case 9: //rtty reverse
             return "RY";
          default:
             return "";
       }
    } 

 }
*/
