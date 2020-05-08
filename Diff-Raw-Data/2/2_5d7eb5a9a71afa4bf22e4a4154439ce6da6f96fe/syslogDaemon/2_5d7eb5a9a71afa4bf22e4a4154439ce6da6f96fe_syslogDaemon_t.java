 import com.sleepycat.je.*;
 
 
 import java.net.DatagramSocket;
 import java.net.DatagramPacket;
 import java.net.InetAddress;
 import java.util.Date;
 import java.io.File;
 import java.nio.ByteBuffer;
 
 /*
  * This file is part of syslogUnity.
  *
  *     syslogUnity is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     syslogUnity is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with syslogUnity.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 public class syslogDaemon {
 
     public static void main(String[] args) throws Exception {
         new syslogDaemon().getEntries();
     }
 
     private void getEntries() throws Exception {
         final File dbEnvDir = new File("/var/lib/syslogUnity/queueDB/");
         int BUFFER_SIZE = 1024;
 
         DatagramSocket syslog = new DatagramSocket(514);
         DatagramPacket logEntry = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
 
         EnvironmentConfig envConfig = new EnvironmentConfig();
         envConfig.setTransactional(true);
         envConfig.setAllowCreate(true);
         envConfig.setCacheSize(20971520);
         envConfig.setSharedCache(true);
 
         final Environment env = new Environment(dbEnvDir, envConfig);
 
         DatabaseConfig config = new DatabaseConfig();
         config.setAllowCreate(true);
         final Database queue = env.openDatabase(null, "logQueue", config);
 
         final Cursor cursor;
         cursor = queue.openCursor(null, null);
        LongEntry lastRecNoDbt = new LongEntry(0);
         DatabaseEntry throwaway = new DatabaseEntry();
         cursor.getLast(lastRecNoDbt, throwaway, null);
         long currentRecNo = lastRecNoDbt.getLong() + 1;
         cursor.close();
 
         Runtime.getRuntime().addShutdownHook(new Thread() {
             public void run() {
                 try {
                     queue.close();
                     env.close();
                     System.out.print("Closed DB Gracefully...\n");
                 } catch (Exception dbe) {
                     System.out.print("Caught SIGINT, couldn't close queueDB successfully\n");
                 }
             }
         });
 
         while (true) {
             syslog.receive(logEntry);
             InetAddress logHost = logEntry.getAddress();
             String logLine = new String(logEntry.getData(), 0, logEntry.getLength());
             doStuff(logLine, logHost, queue, currentRecNo);
             currentRecNo++;
         }
     }
 
     void doStuff(String logLine, InetAddress logHost, Database queue, long currentRecNo) {
         Date logDate = new Date();
         String logPriority = "";
         int i;
         for (i = 0; i <= logLine.length(); i++) {
             if (logLine.charAt(i) == '>') break;
             if (logLine.charAt(i) != '<') logPriority = logPriority + logLine.charAt(i);
         }
         String logData = logLine.substring(i + 1, logLine.length());
         String dateEpoch = Long.toString(logDate.getTime());
         String insertRecord = dateEpoch.concat("##FD##").
                 concat(logPriority).concat("##FD##").
                 concat(logHost.getHostAddress()).concat("##FD##").
                 concat(logData);
 
         byte[] k = ByteBuffer.allocate(8).putLong(currentRecNo).array();
         byte[] d = insertRecord.getBytes();
 
         DatabaseEntry kdbt = new DatabaseEntry(k);
         kdbt.setSize(8);
         DatabaseEntry ddbt = new DatabaseEntry(d, 0, d.length);
         ddbt.setSize(d.length);
 
         try {
             queue.put(null, kdbt, ddbt);
         } catch (Exception dbe) {
             System.out.print("Couldn't add record to database\n");
         }
     }
 }
 
