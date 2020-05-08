 package net.sourceforge.gjtapi.raw.modem;
 
 // NAME
 //      $RCSfile$
 // DESCRIPTION
 //      [given below in javadoc format]
 // DELTA
 //      $Revision$
 // CREATED
 //      $Date$
 // COPYRIGHT
 //      Westhawk Ltd
 // TO DO
 //
 
 import java.io.*;
 import java.net.URI;
 import java.net.URL;
 import java.util.*;
 import javax.telephony.*;
 import javax.telephony.media.*;
 
 import net.sourceforge.gjtapi.*;
 import net.sourceforge.gjtapi.media.SymbolConvertor;
 import net.sourceforge.gjtapi.raw.MediaTpi;
 
 /**
  * An implementation of a Jtapi provider which uses a voice capable
  * modem.
  *
  * @author <a href="mailto:ray@westhawk.co.uk">Ray Tran</a>
  * @version $Revision$ $Date$
  */
 
 public class ModemProvider implements MediaTpi, ModemListener {
     private static final String     version_id =
         "@(#)$Id$ Copyright Westhawk Ltd";
 
     private final static String RESOURCE_NAME = "Modem.props";
     private final static String ADDRESS_PREFIX = "Address";
     private final static String SERIAL = "Serial";//used as a key to select the
                                                 //serial port used by the modem
 
     private Properties provProps;
     private List addresses;
     private TermData terminal;
     private TelephonyListener listener; //According to implementors guide we only need one!
     private Modem modem;
 
     /**
      * Raw constructor used by the GenericJtapiPeer factory
      */
     public ModemProvider() {
         // read provider details and load the resources, if available
         Properties props = new Properties();
         try {
            props.load(this.getClass().getResourceAsStream(File.separator + RESOURCE_NAME));
         } catch (IOException ioe) {
             // ignore and hope that the initialize method sets my required properties
         }
         provProps = props;
     }
 
     //ModemListener implementation
     
     /**
      * The modem is ringing
      * 
      * @return CallId - the id of the new call
      */
     public CallId modemRinging(){
         CallId id = null;
         if (listener != null){
             String address = (String)addresses.get(0);
             try{
                 id = reserveCallId(address);
                 //listener.connectionAlerting(id, address,
                 //        ConnectionEvent.CAUSE_NEW_CALL);
                 listener.terminalConnectionRinging(id, address,
                         terminal.terminal, TerminalConnectionEvent.CAUSE_NORMAL);
             }catch (InvalidArgumentException ex){
                 System.err.println("Invalid argument");
             }
         }
         return id;
     }
     
     /**
      * The modem was ringing but the caller has hung up before we answered
      * 
      * @param id - the id of the call which has just stopped
      */
     public void ringingStopped(CallId id){
         if (listener != null){
             String address = (String)addresses.get(0);
             listener.terminalConnectionDropped(id, address,
                     terminal.terminal, ConnectionEvent.CAUSE_CALL_CANCELLED);
         }
         releaseCallId(id);
     }
 
     public void modemConnected(CallId id){
         if (listener != null){
             String address = (String)addresses.get(0);
             listener.connectionConnected(id, address, ConnectionEvent.CAUSE_NORMAL);
         }
     }
 
     public void modemDisconnected(CallId id){
         if (listener != null){
             String address = (String)addresses.get(0);
             listener.connectionDisconnected(id, address, ConnectionEvent.CAUSE_NORMAL);
         }
     }
 
     public void modemFailed(CallId id){
         if (listener != null){
             String address = (String)addresses.get(0);
             listener.connectionFailed(id, address, ConnectionEvent.CAUSE_DEST_NOT_OBTAINABLE);
         }
     }
 
     //BasicJtapiTpi implementation
     /**
      * Initialize the provider.
      *
      * The main task is setting the serial port and modem up.
      *
      * @param props Map describing how to initialise
      * @throws ProviderUnavailableException
      */
     public void initialize(Map props) throws ProviderUnavailableException {
         if(provProps != null){
             Object obj = provProps.get(SERIAL);
             if((obj != null) && (obj instanceof String)){
                 String portname = (String) obj;
                 //TODO: need to get the modem by reflection or similar
                 modem = new AccuraV92(this);
                 if (modem.initialize(portname) == false){
                     modem = null;
                     throw new ProviderUnavailableException(
                         ProviderUnavailableException.CAUSE_NOT_IN_SERVICE,
                         "The modem on " + portname + " could not be initialized"
                     );
                 }
             }else{
                 throw new ProviderUnavailableException(
                     ProviderUnavailableException.CAUSE_INVALID_ARGUMENT,
                     "The passed in serial port name was \"null\" or not a String"
                 );
             }
         }else{
             throw new ProviderUnavailableException(
                 ProviderUnavailableException.CAUSE_INVALID_ARGUMENT,
                 "Parameter \"props\" was null"
             );
         }
 
         //Now get all of the addresses
         addresses = new Vector();
         Iterator iter = provProps.keySet().iterator();
         while (iter.hasNext()){
             String key = (String)iter.next();
             if (key.startsWith(ADDRESS_PREFIX)){
                 addresses.add(provProps.get(key));
             }
         }
 
         //Finally set up the terminal
         terminal = new TermData((String)provProps.get(SERIAL), false);
     }
 
     /**
      * Add an observer for RawEvents.
      *
      * We can only store a single listener because that is all that is required
      * by the architecture.
      *
      * @param ro TelephonyListener to register
      */
     public void addListener(TelephonyListener ro) {
         if (listener == null){
             listener = ro;
         }else{
             System.err.println("Request to add a TelephonyListener to "
                 + this.getClass().getName() + ", but one is already registered");
         }
     }
 
     /**
      * Remove the observer for RawEvents.
      *
      * If the listener isn't the one registered nothing is done
      *
      * @param ro TelephonyListener to de-register
      */
     public void removeListener(TelephonyListener ro) {
         if (ro == listener){
             listener = null;
         }else{
             System.err.println("Request to remove a TelephonyListener from "
                 + this.getClass().getName() + ", but it wasn't registered");
         }
     }
 
     public String[] getAddresses() throws ResourceUnavailableException {
         Iterator iter = addresses.iterator();
         /*debug
          * while (iter.hasNext()){
          *     System.err.println("    Address: " + iter.next());
          * }
          */
         return (String[]) addresses.toArray(new String[0]);
     }
 
     public String[] getAddresses(String terminal) throws InvalidArgumentException {
         //We only support a single terminal which maps to all addresses
         if (terminal.equals((String) provProps.get(SERIAL)) == false){
             System.err.println("Terminal " + terminal + " is unknown, throwing exception");
             throw new InvalidArgumentException("Terminal " + terminal + "is unknown");
         }
 
         String[] result = null;
         try {
             result = getAddresses();
         }
         catch (ResourceUnavailableException ex) {
             //This shouldn't happen!
             throw new InvalidArgumentException();
         }
         return result;
     }
 
     public TermData[] getTerminals() throws ResourceUnavailableException {
         if (terminal != null){
             return new TermData[]{terminal};
         }else{
             int reason = ResourceUnavailableException.ORIGINATOR_UNAVAILABLE;
             throw new ResourceUnavailableException(reason);
         }
     }
 
     public TermData[] getTerminals(String address) throws InvalidArgumentException {
         //We only support a single terminal which maps to all addresses
         if (addresses.contains(address) == false){
             System.err.println("Address " + address + " is unknown, throwing exception");
             throw new InvalidArgumentException("Address " + address + " is unknown");
         }
 
         TermData[] tda = null;
         try {
             tda = getTerminals();
         }
         catch (ResourceUnavailableException ex) {
             //This shouldn't happen!
             throw new InvalidArgumentException();
         }
 
         return tda;
     }
 
     public Properties getCapabilities() {
         return provProps;
     }
 
     //Doc is a bit unclear on whether this parameter is an address or a terminal
     //we seem to get passed a terminal so for now I have disabled checking!
     public CallId reserveCallId(String address) throws InvalidArgumentException {
         //if (addresses.contains(address)){
             return new ModemCallId();
         //}else{
         //    throw new InvalidArgumentException("Invalid address");
         //}
     }
 
     public void releaseCallId(CallId id) {
         //Nothing to do
     }
 
     public CallId createCall(CallId id, String address, String term, String dest)
         throws ResourceUnavailableException,
         PrivilegeViolationException,
         InvalidPartyException,
         InvalidArgumentException,
         RawStateException,
         MethodNotSupportedException
     {
         if (id instanceof ModemCallId == false){
             throw new InvalidArgumentException("createCall requires a ModemCallId");
         }else if (addresses.contains(address) == false){
             throw new InvalidArgumentException("Invalid address");
         }else if (terminal.terminal.equals(term) == false){
             throw new InvalidArgumentException("Invalid terminal");
         }else if (modem == null){
             throw new RawStateException(id,
                 address,
                 term,
                 RawStateException.TERMINAL_OBJECT,
                 Call.INVALID,
                 "Modem is null"
             );
         }else if (modem.getState() != Modem.IDLE){
             throw new RawStateException(id,
                 address,
                 term,
                 RawStateException.TERMINAL_OBJECT,
                 Call.INVALID,
                 "Modem is not idle"
             );
         }
 
         //Passed all tests so get the modem to make the call
         if (modem.call(id, dest) == false){
             id = null;
         }
 
         return id;
     }
 
     public void answerCall(CallId id, String address, String term)
         throws PrivilegeViolationException,
         ResourceUnavailableException,
         MethodNotSupportedException,
         RawStateException
     {
         if (id instanceof ModemCallId == false){
             throw new MethodNotSupportedException("createCall requires a ModemCallId");
         }else if (addresses.contains(address) == false){
             throw new MethodNotSupportedException("Invalid address");
         }else if (terminal.terminal.equals(term) == false){
             throw new MethodNotSupportedException("Invalid terminal");
         }else if (modem == null){
             throw new RawStateException(id,
                 address,
                 term,
                 RawStateException.TERMINAL_OBJECT,
                 Call.INVALID,
                 "Modem is null"
             );
         }else if (modem.getState() != Modem.RINGING){
             throw new RawStateException(id,
                address,
                 term,
                 RawStateException.TERMINAL_OBJECT,
                 Call.INVALID,
                 "Modem is not ringing"
             );
         }
 
         //Passed all test so get the modem to answer the call
         modem.answer(id);
     }
     
     public void release(String address, CallId call)    
             throws PrivilegeViolationException, ResourceUnavailableException,
                    MethodNotSupportedException, RawStateException
     {
         if ((modem != null) &&
             (modem.getState() == Modem.BUSY) &&
             (addresses.contains(address)))
         {
             modem.drop(call);
         }
     }
 
     public void shutdown() {
         if (modem != null){
             modem.shutdown();
             modem = null;
         }
     }
 
     //MediaTpi implementation
     public boolean allocateMedia(String terminal, int type, Dictionary resourceArgs) {
         return true;
     }
     
     public boolean freeMedia(String terminal, int type) {
         return true;
     }
     
     public boolean isMediaTerminal(String terminal) {
         return true;
     }
     
     public void play(String terminal, String[] streamIds, int offset,RTC[] rtcs,
                      Dictionary optArgs) throws MediaResourceException {
         for (int i=0, len=streamIds.length; i<len; i++){
             try{
                 URL url = new URL(streamIds[i]);
                 InputStream is = url.openStream();
                 modem.play(is);
                 is.close();
             }catch (Exception ex){
                 System.err.println("Exception in play()");
                 ex.printStackTrace();
             }
         }
     }
     
     public void record(String terminal, String streamId, RTC[] rtcs,
                        Dictionary optArgs) throws MediaResourceException {
         try{
             URI uri = new URI(streamId);
             File file = new File(uri);
             FileOutputStream fos = new FileOutputStream(file, false);
             modem.record(fos);
             fos.close();
         }catch (Exception ex){
             System.err.println("Exception in record()");
             ex.printStackTrace();
         }
     }
     
     public RawSigDetectEvent retrieveSignals(String terminal, int num,
             Symbol[] patterns, RTC[] rtcs, Dictionary optArgs)
             throws MediaResourceException {
         String sigs = modem.reportDTMF(num);
         return RawSigDetectEvent.maxDetected(terminal, SymbolConvertor.convert(sigs));
     }
     
     public void sendSignals(String terminal, Symbol[] syms, RTC[] rtcs,
                             Dictionary optArgs) throws MediaResourceException {
         String tones = SymbolConvertor.convert(syms);
         modem.sendDTMF(tones);
     }
     
     public void stop(String terminal) {
 
     }
     
     public void triggerRTC(String terminal, Symbol action) {
 
     }
 }
