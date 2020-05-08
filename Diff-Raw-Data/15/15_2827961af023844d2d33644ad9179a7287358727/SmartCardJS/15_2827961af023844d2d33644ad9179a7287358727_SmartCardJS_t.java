 package org.irmacard.scjs;
 
 import java.applet.Applet;
 import java.security.AccessController;
 import java.security.PrivilegedActionException;
 import java.security.PrivilegedExceptionAction;
 import java.util.List;
 import java.util.Vector;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.smartcardio.CardException;
 import javax.smartcardio.CardTerminal;
 
 import net.sourceforge.scuba.smartcards.CardEvent;
 import net.sourceforge.scuba.smartcards.CardManager;
 import net.sourceforge.scuba.smartcards.CardServiceException;
 import net.sourceforge.scuba.smartcards.CardTerminalEvent;
 import net.sourceforge.scuba.smartcards.CardTerminalListener;
 import net.sourceforge.scuba.smartcards.CommandAPDU;
 import net.sourceforge.scuba.smartcards.IResponseAPDU;
 import net.sourceforge.scuba.smartcards.TerminalCardService;
 import net.sourceforge.scuba.smartcards.TerminalFactoryListener;
 import net.sourceforge.scuba.util.Hex;
 import netscape.javascript.JSException;
 import netscape.javascript.JSObject;
 
import org.irmacard.chvservice.CardHolderVerificationService;

 public class SmartCardJS extends Applet
     implements CardTerminalListener, TerminalFactoryListener {
    
     private static final long serialVersionUID = -4855017287165883462L;
 
     /**
      * JavaScript communication object.
      */
     private JSObject js = null;
 
     /**
      * Whether signals should be emitted or not.
      */
     private boolean signalsEnabled = false;
     
     /**
      * JavaScript object which will handle signals emitted by the applet.
      */
     private String jsSignalHandler = null;
     
     /**
      * Java object which will handle signals emitted by the applet.
      */
     private SignalHandler jSignalHandler = null;
 
     /**
      * Execution service to handle events asynchronously.
      */
     private ExecutorService executorService = null;
 
     /**
      * Console object to handle the output behaviour.
      */
     private Console console;
 
     /**
      * Manager which polls factories and terminals for terminals and cards.
      */
     private CardManager cardManager;    
     
     /*************************************************************************
      *** Applet life cycle functionality                                   ***
      *************************************************************************/
     
     public void init() {
         console = new Console(this);
         console.traceCall("init()");
         jSignalHandler = console;        
         executorService = Executors.newCachedThreadPool();        
         
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
         cardManager.startPolling();
         
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
         emit(new Signal(this, "Woooot, testing (v.13)!!!", null));
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
             jSignalHandler.handle(signal);
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
      * Called by the card manager when a terminal is added.
      *
      * @param event generated by the card manager
      */
     public void cardTerminalAdded(CardTerminalEvent event) {
         console.traceCall("cardTerminalAdded(" + event + ")");
         
         emit(new Signal(this, "terminalAdded", new Object[]{event.getTerminal()}));
     }
 
     /**
      * Called by the card manager when a terminal is removed.
      *
      * @param event generated by the card manager
      */
     public void cardTerminalRemoved(CardTerminalEvent event) {
         console.traceCall("cardTerminalRemoved(" + event + ")");
         
         emit(new Signal(this, "terminalRemoved", new Object[]{event.getTerminal()}));
     }
     
     /**
      * Called by the card manager when a card is inserted.
      *
      * @param event generated by the card manager
      */
     public void cardInserted(CardEvent event) {
         console.traceCall("cardInserted(" + event + ")");
         
         emit(new Signal(this, "cardInserted", new Object[]{event.getService()}));
     }
 
     /**
      * Called by the card manager when a card is removed.
      *
      * @param event generated by the card manager
      */
     public void cardRemoved(CardEvent event) {
         console.traceCall("cardRemoved(" + event + ")");
 
         emit(new Signal(this, "cardRemoved", new Object[]{event.getService()}));
     }
 
     /**
      * Get a list of all available readers.
      * 
      * @return a list of readers
      */
     public String getReaderList() {
         console.traceCall("getReaderList()");
         
         List<CardTerminal> readers = cardManager.getTerminals();
         
         // Turn this list of readers into a String
         if (readers.isEmpty()) {
             return "bananen";
         } else {
             String list = "";
             for (CardTerminal reader : readers) {
                 list += "\n" + reader.getName();
             }
             return list.substring(1);
         }
     }
 
     /**
      * Return last error message
      */
     public String getLastError() {
     	return lastErrorMessage;
     }
 
     /**
      * Get a list of readers with cards present.
      * 
      * @return a list of readers
      */
     public String getCardList() {
         console.traceCall("getCardList()");
         
         List<CardTerminal> readers = cardManager.getTerminals();        
         List<CardTerminal> cards = new Vector<CardTerminal>();
         
         // Filter out readers with no cards
         for (CardTerminal reader : readers) {
             try {
                 if (reader.isCardPresent()) {
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
     
     private CardHolderVerificationService cardService = null;
     private String lastErrorMessage = "";
     
     public CardHolderVerificationService getCardService() {
     	return cardService;
     }
     
     public boolean ConnectCard(String readerName) {
     	List<CardTerminal> readers = cardManager.getTerminals();
         for (CardTerminal reader : readers) {
             try {
                 if (reader.isCardPresent() && reader.getName().equals(readerName)) {
 					cardService = new CardHolderVerificationService(
 							new TerminalCardService(reader));
 					cardService.addPinVerificationListener(new PinListener(this));
                     try {
                         cardService.open();
 					} catch (CardServiceException e) {
 						lastErrorMessage = "Cannot connect to the card";
 						return false;
 					}
                 }
             } catch (CardException e) {
                 e.printStackTrace();
             }
         }
         return true;
     }
 
     /**
      * Connect to the first card found on the card readers.
      * @return
      */
     public boolean connectFirstCard() {
         Boolean response = false;
         try {
             response = AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {
                 public Boolean run() {
                 	List<CardTerminal> readers = cardManager.getTerminals();
                     for (CardTerminal reader : readers) {
                     	return ConnectCard(reader.getName());
                     }
                     lastErrorMessage = "No card found.";
                     return false;
                 }
             });
         } catch(PrivilegedActionException e) {
             e.printStackTrace();
         }
         return response;
     }
     
     /**
      * Transmit APDU commands to the card
      * @param strAPDU APDU command represented by a hex-encoded byte-array
      * @return response of the card represented by a hex-encoded byte-array
      */
     public String transmitString(String strAPDU) {
     	final String ApduCmd = strAPDU;
         try {
             return AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {
                 public String run() {
                     CommandAPDU getData = new CommandAPDU(Hex.hexStringToBytes(ApduCmd));
                     
                     try {
                         IResponseAPDU resp = cardService.transmit(getData);
                         return Hex.bytesToHexString(resp.getBytes());
                     } catch(CardServiceException e) {
                         e.printStackTrace();
                         return (new StringBuilder("Exception ")).append(e.getMessage()).toString();
                     }
                 }
             });
         } catch(PrivilegedActionException e) {
             e.printStackTrace();
         }
         return "";
     }
     
     public int verifyPin() {
         try {
             return AccessController.doPrivileged(new PrivilegedExceptionAction<Integer>() {
                 public Integer run() {
                     try {
                     	int nr_tries_left = cardService.verifyPIN();
                         return nr_tries_left;
                     } catch(Exception e) {
                         e.printStackTrace();
                         return -17;
                     }
                 }
             });
         } catch(PrivilegedActionException e) {
             e.printStackTrace();
         }
         
         return -19;
     }
 }
