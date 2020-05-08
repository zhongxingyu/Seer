 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.fakebelieve.netbeans.plugin.logviewer;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.util.LinkedList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.Action;
 import org.openide.util.Exceptions;
 import org.openide.util.RequestProcessor;
 import org.openide.windows.IOContainer;
 import org.openide.windows.IOProvider;
 import org.openide.windows.InputOutput;
 
 /*
  * Netbeans Logging Levels - add option "-J-Dorg.netbeans.level=FINEST" into netbeans.conf file.
  */
 public class LogViewer implements Runnable {
 
     protected static final Logger log = Logger.getLogger(LogViewer.class.getName());
     protected static IOContainer ioc = null;
    protected static LogViewerTopComponent tc = null;
     protected int maxIoName = 36;
     ContextLogSupport logSupport;
     protected InputStream logStream = null;
     protected BufferedReader logReader = null;
     protected LineReader lineReader = null;
     protected InputOutput io;
     protected String logConfig;
     protected String ioName;
     protected boolean shouldStop = false;
     protected int refreshInterval = 10;
     protected int intervalCount = 0;
     protected int maxLines = -1;
     protected int lookbackLines = 2000;
     protected int bufferLines = 2000;
     protected int linesRead;
     Ring ring;
     protected final RequestProcessor.Task task = RequestProcessor.getDefault().create(this);
 
     /**
      * Connects a given process to the output window. Returns immediately, but threads are started that
      * copy streams of the process to/from the output window.
      *
      * @param process process whose streams to connect to the output window
      * @param ioName name of the output window tab to use
      */
     public LogViewer() {
         logSupport = new ContextLogSupport("/tmp", null);
     }
 
     public LogViewer(String logConfig) {
         this();
 
         this.logConfig = logConfig;
         this.ioName = logConfig;
     }
 
     public static boolean handleConfig(String logConfig) {
         return false;
     }
 
     public void setLookbackLines(int lookbackLines) {
         this.lookbackLines = lookbackLines;
     }
 
     public void setRefreshInterval(int refreshInterval) {
         this.refreshInterval = refreshInterval;
     }
 
     private void init() {
         log.log(Level.FINER, "Starting init()");
         ring = new Ring(lookbackLines);
 
         // Read the log file without
         // displaying everything
         try {
             String line;
             log.log(Level.FINER, "start reading ring() lines.");
             while ((line = lineReader.readLine()) != null) {
                 ring.add(line);
             } // end of while ((line = ins.readLine()) != null)
             log.log(Level.FINER, "finish reading ring() lines.");
         } catch (IOException e) {
             log.log(Level.SEVERE, "Failed reading log file.", e);
         } // end of try-catch
 
         // Now show the last OLD_LINES
         linesRead = ring.output();
         log.log(Level.FINER, "displayed ring()");
         ring.setMaxCount(bufferLines);
         log.log(Level.FINE, "Done reading file.");
     }
 
     public boolean checkShouldStop() {
         return shouldStop;
     }
 
     @Override
     public void run() {
         log.log(Level.FINE, "{0} - isClosed() = {1}", new Object[]{ioName, io.isClosed()});
         if (!checkShouldStop() && !io.isClosed()) {
             if (intervalCount == 0) {
                 init();
             } else {
                 try {
                     if (maxLines > 0 && linesRead >= maxLines) {
                         io.getOut().reset();
                         linesRead = ring.output();
                     } // end of if (lines >= MAX_LINES)
 
                     String line;
                     while ((line = lineReader.readLine()) != null) {
                         if ((line = ring.add(line)) != null) {
                             //io.getOut().println(line);
                             processLine(line);
                             linesRead++;
                         } // end of if ((line = ring.add(line)) != null)
                     }
 
                 } catch (IOException e) {
                     log.log(Level.SEVERE, "Failed reading log file and printing to output.", e);
                 }
             }
 
             // For the first few intervals, update every second, just to make sure
             // we catch up quickly.
             task.schedule((intervalCount++ < 10) ? 1000 : (refreshInterval * 1000));
         } else {
             stopUpdatingLogViewer();
         }
     }
 
     public void configViewer() throws IOException {
     }
 
     /* display the log viewer dialog
      *
      **/
     public void showLogViewer() throws IOException {
        if (ioc == null || tc == null || tc.hasClosed()) {
            ioc = IOContainer.create(tc = new LogViewerTopComponent());
         }
         //InputOutput io = IOProvider.getDefault().getIO("test", new Action[0], ioc);
         //io.getOut().println("Hi there");
         //io.select();
 
         io = IOProvider.getDefault().getIO(ioName, new Action[0], ioc);
         io.getOut().reset();
         io.select();
 
         try {
             configViewer();
         } catch (IOException ex) {
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             PrintStream pout = new PrintStream(out);
 
             if (ex instanceof FileNotFoundException) {
                 pout.println("File \"" + logConfig + "\" Not Found.");
             } else {
                 ex.printStackTrace(pout);
                 pout.flush();
             }
 
             logStream = new ByteArrayInputStream(out.toByteArray());
             logReader = new BufferedReader(new InputStreamReader(logStream));
             lineReader = new LineReader(logReader);
 
             log.log(Level.FINE, "Showing error.");
         }
 
         //init();
         task.schedule(0);
     }
 
     /* stop to update  the log viewer dialog
      *
      **/
     public void stopUpdatingLogViewer() {
         log.log(Level.FINE, "Stopping Log Viewer.");
         try {
             logReader.close();
             logStream.close();
             io.getOut().println();
             io.getOut().println("* monitoring ended.");
             //io.closeInputOutput();
             //io.setOutputVisible(false);
         } catch (IOException e) {
             log.log(Level.SEVERE, "Failed to close log file streams.", e);
         }
     }
 
     private void processLine(String line) {
         ContextLogSupport.LineInfo lineInfo = logSupport.analyzeLine(line);
         if (lineInfo.isError()) {
             if (lineInfo.isAccessible()) {
                 try {
                     io.getErr().println(line, logSupport.getLink(lineInfo.message(), lineInfo.path(), lineInfo.line()));
                 } catch (IOException ex) {
                     Exceptions.printStackTrace(ex);
                 }
             } else {
                 io.getErr().println(line);
             }
         } else {
 //            if (line.contains("java.lang.LinkageError: JAXB 2.0 API")) { // NOI18N
 //                File file = InstalledFileLocator.getDefault().locate("modules/ext/jaxws21/api/jaxws-api.jar", null, false); // NOI18N
 //                File endoresedDir = tomcatManager.getTomcatProperties().getJavaEndorsedDir();
 //                if (file != null) {
 //                    writer.println(NbBundle.getMessage(LogViewer.class, "MSG_WSSERVLET11", file.getParent(), endoresedDir));
 //                } else {
 //                    writer.println(NbBundle.getMessage(LogViewer.class, "MSG_WSSERVLET11_NOJAR", endoresedDir));
 //                }
 //            }
             io.getOut().println(line);
         }
     }
 
     class Ring {
 
         private int maxCount;
         private int count;
         private LinkedList<String> anchor;
 
         public Ring(int max) {
             maxCount = max;
             count = 0;
             anchor = new LinkedList<String>();
         }
 
         public String add(String line) {
             if (line == null || line.equals("")) { // NOI18N
                 return null;
             } // end of if (line == null || line.equals(""))
 
             while (count >= maxCount) {
                 anchor.removeFirst();
                 count--;
             } // end of while (count >= maxCount)
 
             anchor.addLast(line);
             count++;
 
             return line;
         }
 
         public void setMaxCount(int newMax) {
             maxCount = newMax;
         }
 
         public int output() {
             int i = 0;
             for (String s : anchor) {
                 processLine(s);
                 i++;
             }
 
             return i;
         }
 
         public void reset() {
             anchor = new LinkedList<String>();
         }
     }
 }
