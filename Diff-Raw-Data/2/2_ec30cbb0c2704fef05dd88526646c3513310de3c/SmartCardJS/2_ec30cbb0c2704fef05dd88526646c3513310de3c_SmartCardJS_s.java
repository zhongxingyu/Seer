 package org.ovchip.scjs;
 
 import java.applet.Applet;
 import java.security.AccessController;
 import java.security.PrivilegedActionException;
 import java.security.PrivilegedExceptionAction;
 import java.util.List;
 import java.util.Vector;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.smartcardio.ATR;
 import javax.smartcardio.Card;
 import javax.smartcardio.CardChannel;
 import javax.smartcardio.CardException;
 import javax.smartcardio.CardTerminal;
 import javax.smartcardio.CardTerminals;
 import javax.smartcardio.CommandAPDU;
 import javax.smartcardio.ResponseAPDU;
 import javax.smartcardio.TerminalFactory;
 
 import net.sourceforge.scuba.smartcards.CardEvent;
 import net.sourceforge.scuba.smartcards.CardManager;
 import net.sourceforge.scuba.smartcards.CardTerminalEvent;
 import net.sourceforge.scuba.smartcards.CardTerminalListener;
 import net.sourceforge.scuba.smartcards.TerminalFactoryListener;
 import netscape.javascript.JSException;
 import netscape.javascript.JSObject;
 
 public class SmartCardJS 
     extends Applet 
     implements CardTerminalListener<CommandAPDU, ResponseAPDU>, TerminalFactoryListener {
    
     private static final long serialVersionUID = -4855017287165883462L;
 
     /**
      * JavaScript communication object.
      */
     private JSObject js;
 
     /**
      * JavaScript object which will handle signals emitted by the applet.
      */
     private String jsSignalHandler = null;
     
     /**
      * Java object which will handle signals emitted by the applet.
      */
     private SignalHandler javaSignalHandler = null;
 
     /**
      * Whether signals should be emitted or not.
      */
     private boolean signalsEnabled = false;
     
     /**
      * Execution service to handle events asynchronously.
      */
     private ExecutorService executorService = Executors.newCachedThreadPool();
 
     /**
      * Console object to handle the output behaviour.
      */
     private Console console;
 
     /**
      * Manager which polls factories and terminals for terminals and cards.
      */
     private CardManager cardManager;    
     
     String jsResult;
 
     // Return values for JavaScript calls
     boolean CardIsPresent;
     int NbReaders;
     String CardATR;
     String ApduRsp;
     String ReaderName;
     String ConnectResponse;
 
     // Class internals
     Card card;    
     CardPoller cardPoller;
     CardTerminal workingReader;
     Thread cardPollerThread = null;
 
     /*************************************************************************
      *** Applet life cycle functionality                                   ***
      *************************************************************************/
     
     public void init() {
         console = new Console(this);
         console.traceCall("init()");
         
         String parameter;
         
         // Set up Java Script signal handling
         parameter = getParameter("jsSignalHandler");
         if (parameter != null) {
             jsSignalHandler = parameter;
         }
         
         // Set up Java signal handling
         javaSignalHandler = console;
         
         try {
             js = JSObject.getWindow(this);
         } catch(JSException e) {
             e.printStackTrace();
         }
         
         emit(new Signal(this, "appletInitialised"));
     }
 
     public void start() {
         console.traceCall("start()");
         
         emit(new Signal(this, "appletStarted"));
     }
 
     public boolean run() {
         console.traceCall("run()");
         
         cardManager = CardManager.getInstance();
         cardManager.addTerminalFactoryListener(this);
         cardManager.addCardTerminalListener(this);
         
         emit(new Signal(this, "appletRunning"));
         
         return true;
     }
     
     public void stop() {
         console.traceCall("stop()");
         
         cardManager.stopPolling();
         executorService.shutdown();
         
         emit(new Signal(this, "appletStopped"));
     }
 
     public void destroy() {
         console.traceCall("destroy()");
         
         emit(new Signal(this, "appletDestroyed"));    
     }
     
     /*************************************************************************
      *** Setters and getters for parameters                                ***
      *************************************************************************/
     
     public String getOutputFilter() {
         console.traceCall("getOutputFilter()");
         
         return console.getOutputFilter();
     }
     
     public void setOutputFilter(String filter) {
         console.traceCall("setOutputFilter(" + filter + ")");
         
         console.setOutputFilter(filter);
     }
     
     public void addOutputLevel(String level) {
         console.traceCall("addOutputLevel(" + level + ")");
         
         console.addOutputLevel(level);
     }
     
     public void removeOutputLevel(String level) {
         console.traceCall("removeOutputLevel(" + level + ")");
         
         console.removeOutputLevel(level);
     }
         
     /*************************************************************************
      *** Signal handling                                                   ***
      *************************************************************************/
     
     public void enableSignals(String handler) {
         jsSignalHandler = handler;
         signalsEnabled = true;        
     }
     
     public void disableSignals() {
         signalsEnabled = false;
     }
     
     public void emit(final Signal signal) {
         console.traceCall("emit(" + signal + ")");
         
         if (signalsEnabled) {
             executorService.execute(new Runnable() {
                 public void run() { 
                     jEmit(signal);
                 }
             });
         
             executorService.execute(new Runnable() {
                 public void run() {
                     jsEmit(signal);
                 }
             });
         }
     }
     
     public void jEmit(Signal signal) {
         console.traceCall("jEmit(" + signal + ")");
         
         try {
             javaSignalHandler.handle(signal);
         } catch (Exception e) {
             console.warning("Failed to emit " + signal + 
                     " due to an Exception: " + e.getMessage());
         }
     }
     
     public void jsEmit(Signal signal) {
         console.traceCall("jsEmit(" + signal + ")");
         
         try {
             ((JSObject) js.getMember(jsSignalHandler)).call(
                     "dispatch", new Object[]{signal});
         } catch (JSException e) {
             console.warning("Failed to emit " + signal + 
                     " due to a JSException: " + e.getMessage());
         }
     }
     
     /*************************************************************************
      *** SmartCardIO interaction                                           ***
      *************************************************************************/
 
     /**
      * Called when terminal added.
      *
      * @param event addition event
      */
     public void cardTerminalAdded(CardTerminalEvent event) {
         console.traceCall("cardTerminalAdded(" + event + ")");
         
         emit(new Signal(this, "terminalAdded", new Object[]{event.getTerminal()}));
     }
 
     /**
      * Called when terminal removed.
      *
      * @param event removal event
      */
     public void cardTerminalRemoved(CardTerminalEvent event) {
         console.traceCall("cardTerminalRemoved(" + event + ")");
         
         emit(new Signal(this, "terminalRemoved", new Object[]{event.getTerminal()}));
     }
     
     /**
      * Called when card inserted.
      *
      * @param event insertion event
      */
     public void cardInserted(CardEvent<CommandAPDU, ResponseAPDU> event) {
         console.traceCall("cardInserted(" + event + ")");
         
         emit(new Signal(this, "cardInserted", new Object[]{event.getService()}));
     }
 
     /**
      * Called when card removed.
      *
      * @param event removal event
      */
     public void cardRemoved(CardEvent<CommandAPDU, ResponseAPDU> event) {
         console.traceCall("cardRemoved(" + event + ")");
 
         emit(new Signal(this, "cardRemoved", new Object[]{event.getService()}));
     }
 
     
     public String getReaderList() {
         console.traceCall("getReaderList()");
         
         List<CardTerminal> readers = cardManager.getTerminals();
         
         // Turn this list of readers into a String
         if (readers.isEmpty()) {
             return "";
         } else {
             String list = "";
             for (CardTerminal reader : readers) {
                 list += "\n" + reader.getName();
             }
             return list.substring(1);
         }
     }
     
     public String getCardList() {
         console.traceCall("getCardList()");
         
         List<CardTerminal> readers = cardManager.getTerminals();
         
         List<CardTerminal> cards = new Vector<CardTerminal>();
         // Filter out readers with no cards
         for (CardTerminal reader : readers) {
             try {
                if (!reader.isCardPresent()) {
                     cards.add(reader);
                 }
             } catch (CardException e) {
                 e.printStackTrace();
             }
         }
 
         // Turn this list of readers into a String
         if (cards.isEmpty()) {
             return "";
         } else {
             String list = "";
             for (CardTerminal reader : cards) {
                 list += "\n" + reader.getName();
             }
             return list.substring(1);
         }
     }
     
     /*
      * Old smart card stuff 
      */
     
     public void killThread() {
         if(cardPollerThread != null && cardPollerThread.isAlive()) {
             Thread t = cardPollerThread;
             cardPollerThread = null;
             t.interrupt();
         }
     }
     
     public void cardPresent() {
         js.call("RefreshCardState", new String[0]);
         killThread();
         
         try {
             cardPoller = new CardPoller(workingReader, CardPoller.POLL_FOR_ABSENT);
             cardPollerThread = new Thread(cardPoller);
             cardPollerThread.start();
         } catch(Exception e) {
             e.printStackTrace();
         }
     }
 
     public void cardAbsent() {
         js.call("RefreshCardState", new String[0]);
         killThread();
         
         try {
             cardPoller = new CardPoller(workingReader, CardPoller.POLL_FOR_PRESENT);
             cardPollerThread = new Thread(cardPoller);
             cardPollerThread.start();
         } catch(Exception e) {
             e.printStackTrace();
         }
     }
     
     public boolean IsCardPresent() {
         try {
             AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                 public Boolean run() {
                     try {
                         CardIsPresent = workingReader.isCardPresent();
                     } catch(CardException e) {
                         e.printStackTrace();
                     }
                     return Boolean.valueOf(true);
                 }
             });
         } catch(PrivilegedActionException e) {
             e.printStackTrace();
         }
         return CardIsPresent;
     }
 
     public String SetReader(String newName) {
         final String NewReaderName = newName;
         try {
             AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                 public Boolean run() {
                     TerminalFactory factory = TerminalFactory.getDefault();
                     CardTerminals terminalList = factory.terminals();
                     workingReader = terminalList.getTerminal(NewReaderName);
 
                     try {
                         if(workingReader.isCardPresent()) {
                              cardPresent();
                         } else {
                                 cardAbsent();
                         }
                     } catch(CardException e1) {
                         e1.printStackTrace();
                     }
 
                     return Boolean.valueOf(true);
                 }
             });
         } catch(PrivilegedActionException e) {
             e.printStackTrace();
         }
         return workingReader.getName();
     }
 
     public void Disconnect(boolean reset) {
         final boolean ResetChoice = reset;
         try {
             AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                 public Boolean run() {
                     try {
                         card.disconnect(ResetChoice);
                     } catch(CardException e) {
                         e.printStackTrace();
                     }
                     return Boolean.valueOf(true);
                 }
             });
         } catch(PrivilegedActionException e) {
             e.printStackTrace();
         }
     }
 
     public String TransmitString(String ApduIn) {
         final String ApduCmd = ApduIn;
         try {
             AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                 public Boolean run() {
                     CardChannel comm = card.getBasicChannel();
                     CommandAPDU getData = new CommandAPDU(SmartCardJS.hexStringToByteArray(ApduCmd));
                     try {
                         ResponseAPDU resp = comm.transmit(getData);
                         ApduRsp = SmartCardJS.byteArrayToHexString(resp.getBytes());
                     } catch(CardException e) {
                         e.printStackTrace();
                         ApduRsp = (new StringBuilder("Exception ")).append(e.getMessage()).toString();
                     }
                     return Boolean.valueOf(true);
                 }
             });
         } catch(PrivilegedActionException e) {
             e.printStackTrace();
         }
         return ApduRsp;
     }
 
     public String TransmitArray(byte ApduIn[]) {
         final byte ApduCmd[] = ApduIn;
         try {
             AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                 public Boolean run() {
                     CardChannel comm = card.getBasicChannel();
                     CommandAPDU getData = new CommandAPDU(ApduCmd);
                     try {
                         ResponseAPDU resp = comm.transmit(getData);
                         ApduRsp = SmartCardJS.byteArrayToHexString(resp.getBytes());
                     } catch(CardException e) {
                         e.printStackTrace();
                         ApduRsp = (new StringBuilder("Exception ")).append(e.getMessage()).toString();
                     }
                     return Boolean.valueOf(true);
                 }
             });
         } catch(PrivilegedActionException e) {
             e.printStackTrace();
         }
         return ApduRsp;
     }
 
     public int GetReaderCount() {
         NbReaders = 0;
         try {
             AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                 public Boolean run() {
                     try {
                         TerminalFactory factory = TerminalFactory.getDefault();
                         CardTerminals terminalList = factory.terminals();
                         NbReaders = terminalList.list().size();
                     } catch(CardException e) {
                         e.printStackTrace();
                     }
                     return Boolean.valueOf(true);
                 }
             });
         } catch(PrivilegedActionException e) {
             e.printStackTrace();
         }
         return NbReaders;
     }
 
     public String GetReaderName(int readerIndex) {
         final int Index = readerIndex;
         try {
             AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                 public Boolean run() {
                     try {
                         TerminalFactory factory = TerminalFactory.getDefault();
                         CardTerminals terminalList = factory.terminals();
                         ReaderName = ((CardTerminal)terminalList.list().get(Index)).getName();
                     } catch(CardException e) {
                         e.printStackTrace();
                     }
                     return Boolean.valueOf(true);
                 }
             });
         } catch(PrivilegedActionException e) {
             e.printStackTrace();
         }
         return ReaderName;
     }
 
     public String GetATR() {
         try {
             AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                 public Boolean run() {
                     try {
                         Card card = workingReader.connect("*");
                         ATR atr = card.getATR();
                         CardATR = SmartCardJS.byteArrayToHexString(atr.getBytes());
                     } catch(CardException e) {
                         e.printStackTrace();
                     }
                     return Boolean.valueOf(true);
                 }
             });
         } catch(PrivilegedActionException e) {
             e.printStackTrace();
         }
         return CardATR;
     }
 
     public String Connect(String protocol) {
         final String Protocol_str = protocol;
         try {
             AccessController.doPrivileged(new PrivilegedExceptionAction<Integer>() {
                 public Integer run() {
                     try {
                         card = workingReader.connect(Protocol_str);
                         if(card != null)
                             ConnectResponse = "";
                     } catch(CardException e) {
                         e.printStackTrace();
                         try {
                             if(workingReader.isCardPresent()) {
                                 ConnectResponse = "Cannot connect card, try with another protocol";
                             } else {
                                 ConnectResponse = "Put a card in the terminal before connecting";
                             }
                         } catch(CardException e1) {
                             e1.printStackTrace();
                         }
                     }
                     return null;
                 }
             });
         } catch(PrivilegedActionException e) {
             e.printStackTrace();
         }
         return ConnectResponse;
     }
 
     static String byteArrayToHexString(byte bArray[]) {
         StringBuffer buffer = new StringBuffer();
         for(int i = 0; i < bArray.length; i++) {
             buffer.append(String.format(" %02X", bArray[i]));
         }
         return buffer.toString().toUpperCase();
     }
 
     public static byte[] hexStringToByteArray(String s) {
         s = s.replace(" ", "");
         int len = s.length();
         byte data[] = new byte[len / 2];
         for(int i = 0; i < len; i += 2) {
             data[i / 2] = (byte)((Character.digit(s.charAt(i), 16) << 4) + 
                     Character.digit(s.charAt(i + 1), 16));
         }
 
         return data;
     }
 
     class CardPoller implements Runnable {
         
         public static final boolean POLL_FOR_PRESENT = false;
         public static final boolean POLL_FOR_ABSENT = true;
         private static final long POLL_INTERVAL = 250;
         
         CardTerminal terminal;
         boolean pollType = POLL_FOR_PRESENT;
         long pollInterval = POLL_INTERVAL;
 
         public CardPoller(CardTerminal reader, boolean type, long interval) {
             terminal = reader;
             pollType = type;
             pollInterval = interval;
         }
 
         public CardPoller(CardTerminal reader, boolean type) {
             terminal = reader;
             pollType = type;
         }
 
         public void run() {
             try {
 //                while(pollType ^ terminal.isCardPresent()) {
 //                    Thread.sleep(pollInterval);
 //                }
                 if (pollType == POLL_FOR_PRESENT) {
                     terminal.waitForCardPresent(0L);
                 } else { // pollType == POLL_FOR_ABSENT
                     terminal.waitForCardAbsent(0L);
                 }
                 
                 if (pollType == POLL_FOR_PRESENT) {
                     executorService.execute(new Runnable() {
                         public void run() {
                             cardPresent();
                         }
                     });
                 } else { // pollType == POLL_FOR_ABSENT
                     executorService.execute(new Runnable() {
                         public void run() {
                             cardAbsent();
                         }
                     });
                 }
             } catch(CardException ce) { 
             }
         }
     }
 
 
 }
