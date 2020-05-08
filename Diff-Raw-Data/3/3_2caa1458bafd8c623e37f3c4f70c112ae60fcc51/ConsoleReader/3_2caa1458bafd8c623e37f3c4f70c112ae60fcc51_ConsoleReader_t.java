 /* ODISP -- Message Oriented Middleware
  * Copyright (C) 2003-2005 Valentin A. Alekseev
  * Copyright (C) 2003-2005 Andrew A. Porohin 
  * 
  * ODISP is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, version 2.1 of the License.
  * 
  * ODISP is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with ODISP.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.valabs.stdobj.console;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.logging.Logger;
 
 import org.doomdark.uuid.UUID;
 import org.valabs.odisp.common.Dispatcher;
 import org.valabs.odisp.common.Message;
 
 
 /**     .
  * 
  * @author (C) 2003-2004 <a href="mailto:valeks@novel-il.ru"> . </a>
  * @author (C) 2003-2004 <a href="mailto:dron@novel-il.ru"> . </a>
  * @version $Id: ConsoleReader.java,v 1.20 2005/08/23 11:16:29 dron Exp $
  */
 
 public class ConsoleReader extends Thread {
   /**   . */
   private boolean doExit;
   /** . */
   private static final Logger logger = Logger.getLogger(ConsoleReader.class.getName());
   /**   . */
   private Dispatcher dispatcher;
   /**  . */
   private BufferedReader inp
     = new BufferedReader(new InputStreamReader(System.in));
 
   /**      .
    * 
    * @param disp  ODISP
    */
   public ConsoleReader(final Dispatcher disp) {
     super("ConsoleReader");
     setDaemon(true);
     dispatcher = disp;
   }
 
   /**     -.
    */
   public final void run() {
     try {
       System.out.print("action> ");
       String action, tmp, fieldName;
       while ((action = inp.readLine()) != null) {
         System.out.print("destination> ");
         final Message m
           = dispatcher.getNewMessage(action, inp.readLine(), "console", UUID.getNullUUID());
         System.out.print("params? ");
         int paramCount = 0;
         while (!inp.readLine().equals("")) {
           System.out.print("int|str? ");
           tmp = inp.readLine();
           System.out.print("name> ");
           fieldName = inp.readLine();
           System.out.print("value> ");
           if (tmp.startsWith("i")) {
             try {
               m.addField((fieldName.length() > 0) ? fieldName : ("" + paramCount++),
                 new Integer(inp.readLine()));
             } catch (NumberFormatException e) {
               System.out.println("(ConsoleReader) NumberFormatException: please, retry.");
             }
           } else {
             m.addField((fieldName.length() > 0) ? fieldName : ("" + paramCount++),
               "" + inp.readLine());
           }
           System.out.print("more? ");
         }
         m.setCorrect(true);
         if (m.getDestination() != null && m.getDestination().length() > 0
             && m.getAction() != null && m.getAction().length() > 0) {
           dispatcher.send(m);
         }
         if (doExit) {
           break;
         }
         System.out.print("action> ");
       }
     } catch (IOException e) {
       logger.finest("ConsoleReader: Terminal connection lost. Quitting.");
     }
   }
 
   /**  .
    */
   public final synchronized void exit() {
     try {
      // Just close system input
      System.in.close();
       this.interrupt();
     } catch (IOException e) { /*NOP*/ }
     doExit = true;
   }
 }
