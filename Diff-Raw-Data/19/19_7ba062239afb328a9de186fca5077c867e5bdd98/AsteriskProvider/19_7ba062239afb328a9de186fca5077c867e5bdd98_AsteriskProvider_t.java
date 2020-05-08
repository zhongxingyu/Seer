package net.sourceforge.gjtapi.raw.asterisk;
 
 import net.sourceforge.gjtapi.raw.*;
 import net.sourceforge.gjtapi.*;
 import net.sourceforge.gjtapi.capabilities.Capabilities;
 import net.sf.asterisk.manager.*;
 import net.sf.asterisk.manager.event.*;
 import net.sf.asterisk.manager.response.*;
 import net.sf.asterisk.manager.response.CommandResponse;
 import net.sf.asterisk.manager.action.*;
 
 import javax.telephony.*;
 import java.util.*;
 import java.io.*;
 
 /**
  *
  * a simple Asteriskprovider
  * @author J. Boesl, 20.06.2005
  */
 
 
 public class AsteriskProvider implements BasicJtapiTpi
 {
   private DefaultAsteriskManager asteriskManager;
   private DefaultManagerConnection managerConnection;
   private Set listeners;
   private Set calls;
   private String[] addresses;
 
   private boolean OUT = true;
 
   public AsteriskProvider()
   {
 	  super();
     listeners = new HashSet();
     calls = new HashSet();
 	}
 
   public void addListener(TelephonyListener ro)
   {
     listeners.add(ro);
   }
 
   public void removeListener(TelephonyListener ro)
   {
     listeners.remove(ro);
   }
 
   /**
    * seems as this is not supported by asterisk
    */
   public void answerCall(CallId call, String address, String terminal) throws PrivilegeViolationException, ResourceUnavailableException,
       MethodNotSupportedException, RawStateException
   {
     throw new MethodNotSupportedException("answering calls is not supported by asterisk");
   }
 
   public CallId createCall(CallId id, String address, String term, String dest) throws ResourceUnavailableException, PrivilegeViolationException,
       InvalidPartyException, InvalidArgumentException, RawStateException,
       MethodNotSupportedException
   {
     final int timeout = 30000;
 
     final OriginateAction originateAction;
     originateAction = new OriginateAction();
     originateAction.setChannel(address);
     originateAction.setContext("default");
     originateAction.setAsync(Boolean.TRUE);
     originateAction.setCallerId(address);
     originateAction.setExten(dest);
     originateAction.setPriority(new Integer(1));
     originateAction.setTimeout(new Integer(timeout));
     try
     {
       ManagerResponse response = managerConnection.sendAction(originateAction, timeout);
       AsteriskConnectionDetail detail = new AsteriskConnectionDetail
       (
         response.getUniqueId(), getChannelForUniqueId(response.getUniqueId()).getName(), address, null
       );
       ((AsteriskCallId)id).addConnection(response.getUniqueId(), detail);
     }
     catch (IOException e)
     {
       e.printStackTrace();
     }
     catch (TimeoutException e)
     {
       e.printStackTrace();
     }
     return id;
   }
 
   public void release(String address, CallId call) throws PrivilegeViolationException,
       ResourceUnavailableException, MethodNotSupportedException, RawStateException
   {
     if(calls.contains(call))
     {
       String channel2Hangup = null;
       for(Iterator i = ((AsteriskCallId)call).getConnections().keySet().iterator(); i.hasNext(); )
       {
         AsteriskConnectionDetail detail = getDetail( (AsteriskCallId)call, i.next().toString());
         if( detail!= null && detail.channel!= null)
         {
           String channelAddress = detail.address;
           if(address != null && address.equals(channelAddress))
           {
             channel2Hangup = detail.channel;
             ((AsteriskCallId)call).removeConnection(detail.uniqueId);
             break;
           }
         }
       }
       if(channel2Hangup != null)
       {
         HangupAction hangupAction = new HangupAction();
         hangupAction.setChannel(channel2Hangup);
         try
         {
           managerConnection.sendAction(hangupAction);
         }
         catch (IOException e)
         {
           e.printStackTrace();
         }
         catch (TimeoutException e)
         {
           e.printStackTrace();
         }
       }
     }
   }
 
   public Properties getCapabilities()
   {
     Properties capabilities = new Properties();
 		capabilities.put(Capabilities.THROTTLE, "f");
 		capabilities.put(Capabilities.MEDIA, "f");
 		capabilities.put(Capabilities.ALL_MEDIA_TERMINALS, "f");
 		capabilities.put(Capabilities.ALLOCATE_MEDIA, "f");
 		return capabilities;
   }
 
   public void releaseCallId(CallId id)
   {
     calls.remove(id);
   }
 
   public CallId reserveCallId(String address) throws InvalidArgumentException
   {
     CallId call = new AsteriskCallId();
     calls.add(call);
     return call;
   }
 
   public void shutdown()
   {
     try
     {
       managerConnection.logoff();
     }
     catch (IOException e)
     {
       e.printStackTrace();
     }
     catch (TimeoutException e)
     {
       e.printStackTrace();
     }
   }
 
   /**
    * lists only sip-addresses
    * @return
    * @throws ResourceUnavailableException
    */
   public String[] getAddresses() throws ResourceUnavailableException
   {
     if(addresses == null)
     {
       CommandAction command = new CommandAction();
       command.setCommand("sip show users");
 
       CommandResponse cr = null;
       try
       {
         cr = (CommandResponse)managerConnection.sendAction(command);
       }
       catch (IOException e)
       {
         e.printStackTrace();
       }
       catch (TimeoutException e)
       {
         e.printStackTrace();
       }
       Object[] result = cr.getResult().toArray();
       addresses = new String[result.length-1];
       for(int i = 1; i < result.length; i++)
         addresses[i-1] = "SIP/" + result[i].toString().split(" ")[0];
     }
     return addresses;
   }
 
   public String[] getAddresses(String terminal) throws InvalidArgumentException
   {
     try
     {
       for(int i = 0; i < getAddresses().length; i++)
       {
         if(getAddresses()[i].equals(terminal))
           return new String[]{terminal};
       }
     }
     catch (ResourceUnavailableException e)
     {
     }
     throw new InvalidArgumentException("no terminal " + terminal);
   }
 
   public TermData[] getTerminals() throws ResourceUnavailableException
   {
     String[] addresses = new String[0];
     try
     {
       addresses = getAddresses();
     }
     catch (ResourceUnavailableException e)
     {
       e.printStackTrace();
     }
     TermData[] termData = new TermData[addresses.length];
     for(int i = 0; i < addresses.length; i++)
     {
       termData[i] = new TermData(addresses[i], false);
     }
     return termData;
   }
 
   public TermData[] getTerminals(String address) throws InvalidArgumentException
   {
     return new TermData[]{new TermData(getAddresses(address)[0], false)};
   }
 
 
   public void initialize(Map props) throws ProviderUnavailableException
   {
     managerConnection = new DefaultManagerConnection();
     asteriskManager = new DefaultAsteriskManager();
 
     managerConnection.setHostname(props.get("Server").toString());
     managerConnection.setPort(Integer.parseInt(props.get("Port").toString()));
     managerConnection.setUsername(props.get("User").toString());
     managerConnection.setPassword(props.get("Password").toString());
 
     asteriskManager.setManagerConnection(managerConnection);
 
     try
     {
       asteriskManager.initialize();
     }
     catch (IOException e)
     {
       throw new ProviderUnavailableException(e.toString());
     }
     catch (AuthenticationFailedException e)
     {
       throw new ProviderUnavailableException(e.toString());
     }
     catch (TimeoutException e)
     {
       throw new ProviderUnavailableException(e.toString());
     }
     managerConnection.addEventHandler(getManagerEventHandler());
   }
 
 
   /**
    * these are the events got by AsteriskJava
    */
   private ManagerEventHandler getManagerEventHandler()
   {
     return new ManagerEventHandler()
     {
       public void handleEvent(ManagerEvent event)
       {
         try
         {
           if (event.getClass().equals(AgentCallbackLoginEvent.class))
           {
             _o("AgentCallbackLoginEvent");
           }
           else if (event.getClass().equals(AgentCallbackLogoffEvent.class))
           {
             _o("AgentCallbackLogoffEvent");
           }
           else if (event.getClass().equals(AgentCalledEvent.class))
           {
             _o("AgentCalledEvent");
           }
           else if (event.getClass().equals(AgentLoginEvent.class))
           {
             _o("AgentLoginEvent");
           }
           else if (event.getClass().equals(AgentLogoffEvent.class))
           {
             _o("AgentLogoffEvent");
           }
           else if (event.getClass().equals(AlarmClearEvent.class))
           {
             _o("AlarmClearEvent");
           }
           else if (event.getClass().equals(AlarmEvent.class))
           {
             _o("AlarmEvent");
           }
           else if (event.getClass().equals(CdrEvent.class))
           {
             _o("CdrEvent");
           }
           else if (event.getClass().equals(ChannelEvent.class))
           {
             _o("ChannelEvent");
           }
           else if (event.getClass().equals(ConnectEvent.class))
           {
             _o("ConnectEvent");
           }
           else if (event.getClass().equals(DialEvent.class))
           {
             _o("DialEvent");
           }
           else if (event.getClass().equals(DisconnectEvent.class))
           {
             _o("DisconnectEvent");
           }
           else if (event.getClass().equals(HangupEvent.class))
           {
             _o("HangupEvent");
             HangupEvent ev = (HangupEvent)event;
             AsteriskCallId call;
             call = getCallIdForUniqueId(ev.getUniqueId());
             String address = null;
             if (call != null)
               address = getAddress(call, ev.getUniqueId());
             if(address != null)
             {
               fireTelephonyEvent(AstTeleEventTypes.terminalConnectionDropped, call, address, Event.CAUSE_NORMAL);
               fireTelephonyEvent(AstTeleEventTypes.connectionDisconnected, call, address, Event.CAUSE_NORMAL);
             }
           }
           else if (event.getClass().equals(HoldedCallEvent.class))
           {
             _o("HoldedCallEvent");
           }
           else if (event.getClass().equals(JoinEvent.class))
           {
             _o("JoinEvent");
           }
           else if (event.getClass().equals(LeaveEvent.class))
           {
             _o("LeaveEvent");
           }
           else if (event.getClass().equals(LinkageEvent.class))
           {
             _o("LinkageEvent");
           }
           else if (event.getClass().equals(LinkEvent.class))
           {
             _o("LinkEvent");
           }
           else if(event .getClass().equals(ManagerEvent.class))
           {
             _o("ManagerEvent");
           }
           else if (event.getClass().equals(MeetMeEvent.class))
           {
             _o("MeetMeEvent");
           }
           else if (event.getClass().equals(MeetMeJoinEvent.class))
           {
             _o("MeetMeJoinEvent");
           }
           else if (event.getClass().equals(MeetMeLeaveEvent.class))
           {
             _o("MeetMeLeaveEvent");
           }
           else if (event.getClass().equals(MessageWaitingEvent.class))
           {
             _o("MessageWaitingEvent");
           }
           else if (event.getClass().equals(NewCallerIdEvent.class))
           {
             _o("NewCallerIdEvent");
           }
           else if (event.getClass().equals(NewChannelEvent.class))
           {
             _o("NewChannelEvent");
             NewChannelEvent ev = (NewChannelEvent) event;
             if (ev.getState().equals("Ringing"))
             {
               AsteriskCallId call = getCallForCallerId(ev.getCallerId());
               if( call!= null)
               {
                 AsteriskConnectionDetail callerDetail = (AsteriskConnectionDetail)
                     call.getConnections().get(getUniqueIdThroughCallerId(call, ev.getCallerId()));
                 AsteriskConnectionDetail detail = new AsteriskConnectionDetail
                     (ev.getUniqueId(), ev.getChannel(), callerDetail.calledAddress, callerDetail.calledAddress);
 
                 call.addConnection(ev.getUniqueId(), detail);
                 fireTelephonyEvent(AstTeleEventTypes.terminalConnectionRinging, call, detail.address, Event.CAUSE_NORMAL);
                 callerDetail.calledAddress = null;
               }
             }
           }
           else if (event.getClass().equals(NewExtenEvent.class))
           {
             _o("NewExtenEvent");
             NewExtenEvent ev = (NewExtenEvent) event;
             if (ev.getApplication().equals("Dial"))
             {
               String callingAddress = getChannelForUniqueId(ev.getUniqueId()).getCallerId();
               String calledAddress = ev.getAppData();
 
               AsteriskCallId call = getCallIdForUniqueId(ev.getUniqueId());
               if(call == null)
               {
                 call = new AsteriskCallId();
                 AsteriskConnectionDetail detail = new AsteriskConnectionDetail
                 (
                   ev.getUniqueId(), ev.getChannel(), callingAddress, calledAddress
                 );
                 call.addConnection(ev.getUniqueId(), detail);
                 calls.add(call);
               }
               AsteriskConnectionDetail detail = getDetail(call, ev.getUniqueId());
               if(calledAddress.equals(detail.address))
                 calledAddress = ev.getExtension();
               detail.calledAddress = calledAddress;
 
               fireTelephonyEvent(AstTeleEventTypes.callActive, call, null, Event.CAUSE_NORMAL);
               fireTelephonyEvent(AstTeleEventTypes.connectionInProgress, call, callingAddress, Event.CAUSE_NORMAL);
               fireTelephonyEvent(AstTeleEventTypes.connectionConnected, call, callingAddress, Event.CAUSE_NORMAL);
               fireTelephonyEvent(AstTeleEventTypes.terminalConnectionCreated, call, callingAddress, Event.CAUSE_NORMAL);
               fireTelephonyEvent(AstTeleEventTypes.terminalConnectionTalking, call, callingAddress, Event.CAUSE_NORMAL);
               fireTelephonyEvent(AstTeleEventTypes.connectionInProgress, call, calledAddress, Event.CAUSE_NORMAL);
               fireTelephonyEvent(AstTeleEventTypes.connectionAlerting, call, calledAddress, Event.CAUSE_NORMAL);
               fireTelephonyEvent(AstTeleEventTypes.connectionConnected, call, calledAddress, Event.CAUSE_NORMAL);
             }
           }
           else if (event.getClass().equals(NewStateEvent.class))
           {
             _o("NewStateEvent");
             NewStateEvent ev = (NewStateEvent) event;
             if (ev.getState().equals("Up"))
             {
               AsteriskCallId call = getCallIdForUniqueId(ev.getUniqueId());
               if(call!= null)
               {
                 AsteriskConnectionDetail detail = getDetail(call, ev.getUniqueId());
                 String address = detail.calledAddress;
                 if(address == null)
                   return ;
                 fireTelephonyEvent(AstTeleEventTypes.terminalConnectionTalking, call, address, Event.CAUSE_NORMAL);
                 detail.calledAddress = null;
               }
             }
           }
           else if (event.getClass().equals(OriginateEvent.class))
           {
             _o("OriginateEvent");
           }
           else if (event.getClass().equals(OriginateFailureEvent.class))
           {
             _o("OriginateFailureEvent");
           }
           else if (event.getClass().equals(OriginateSuccessEvent.class))
           {
             _o("OriginateSuccessEvent");
           }
           else if (event.getClass().equals(ParkedCallEvent.class))
           {
             _o("ParkedCallEvent");
           }
           else if (event.getClass().equals(ParkedCallsCompleteEvent.class))
           {
             _o("ParkedCallsCompleteEvent");
           }
           else if (event.getClass().equals(PeerStatusEvent.class))
           {
             _o("PeerStatusEvent");
           }
           else if (event.getClass().equals(QueueEntryEvent.class))
           {
             _o("QueueEntryEvent");
           }
           else if (event.getClass().equals(QueueEvent.class))
           {
             _o("QueueEvent");
           }
           else if (event.getClass().equals(QueueMemberEvent.class))
           {
             _o("QueueMemberEvent");
           }
           else if (event.getClass().equals(QueueMemberStatusEvent.class))
           {
             _o("QueueMemberStatusEvent");
           }
           else if (event.getClass().equals(QueueParamsEvent.class))
           {
             _o("QueueParamsEvent");
           }
           else if (event.getClass().equals(RegistryEvent.class))
           {
             _o("RegistryEvent");
           }
           else if (event.getClass().equals(ReloadEvent.class))
           {
             _o("ReloadEvent");
           }
           else if (event.getClass().equals(RenameEvent.class))
           {
             _o("RenameEvent");
           }
           else if (event.getClass().equals(ResponseEvent.class))
           {
             _o("ResponseEvent");
           }
           else if (event.getClass().equals(ShutdownEvent.class))
           {
             _o("ShutdownEvent");
           }
           else if (event.getClass().equals(StatusCompleteEvent.class))
           {
             _o("StatusCompleteEvent");
           }
           else if (event.getClass().equals(StatusEvent.class))
           {
             _o("StatusEvent");
           }
           else if (event.getClass().equals(UnlinkEvent.class))
           {
             _o("UnlinkEvent");
           }
           else if (event.getClass().equals(UserEvent.class))
           {
             _o("UserEvent");
           }
           else if (event.getClass().equals(ZapShowChannelsCompleteEvent.class))
           {
             _o("ZapShowChannelsCompleteEvent");
           }
           else if (event.getClass().equals(ZapShowChannelsEvent.class))
           {
             _o("ZapShowChannelsEvent");
           }
         }
         catch (Exception e)
         {
           e.printStackTrace();
         }
       }
     };
   }
 
   /**
    * fires the asterisk events to the GJtapi-Framework
    * @param eventType defined in AstTeleEventTypes
    * @param call the CallId the event might need
    * @param address defines the address and the terminal for the event
    * @param cause the cause
    */
   protected void fireTelephonyEvent(int eventType, CallId call, String address, int cause)
 	{
     for(Iterator i = listeners.iterator(); i.hasNext(); )
     {
       TelephonyListener tL = (TelephonyListener)i.next();
 
       switch (eventType)
       {
         /*case AstTeleEventTypes.addressPrivateData:
         tL.addressPrivateData(String address, Serializable data, int cause);
           break;*/
         case AstTeleEventTypes.callActive:
           tL.callActive(call, cause);
           break;
         case AstTeleEventTypes.callInvalid:
           tL.callInvalid(call, cause);
           break;
         /*case AstTeleEventTypes.callOverloadCeased:
           tL.callOverloadCeased(String address);callOverloadEncountered(String address);
           break;
         case AstTeleEventTypes.callPrivateData:
           tL.callPrivateData(CallId call, Serializable data, int cause);
           break;
         case AstTeleEventTypes.connectionAddressAnalyse:
           tL.connectionAddressAnalyse(CallId id, String address, int cause);
           break;
         case AstTeleEventTypes.connectionAddressCollect:
           tL.connectionAddressCollect(CallId id, String address, int cause);
           break;*/
         case AstTeleEventTypes.connectionAlerting:
           tL.connectionAlerting(call, address, cause);
           break;
         /*case AstTeleEventTypes.connectionAuthorizeCallAttempt:
           tL.connectionAuthorizeCallAttempt(CallId id, String address, int cause);
           break;
         case AstTeleEventTypes.connectionCallDelivery:
           tL.connectionCallDelivery(CallId id, String address, int cause);
           break;*/
         case AstTeleEventTypes.connectionConnected:
           tL.connectionConnected(call, address, cause);
           break;
         case AstTeleEventTypes.connectionDisconnected:
           tL.connectionDisconnected(call, address, cause);
           break;
         case AstTeleEventTypes.connectionFailed:
           tL.connectionFailed(call, address, cause);
           break;
         case AstTeleEventTypes.connectionInProgress:
           tL.connectionInProgress(call, address, cause);
           break;
         /*case AstTeleEventTypes.connectionSuspended:
           //tL.connectionSuspended(CallId id, String address, int cause);
           break;
         case AstTeleEventTypes.mediaPlayPause:
           //tL.mediaPlayPause(String terminal, int index, int offset, Symbol trigger);
           break;
         case AstTeleEventTypes.mediaPlayResume:
           //tL.mediaPlayResume(String terminal, Symbol trigger);
           break;
         case AstTeleEventTypes.mediaRecorderPause:
           //tL.mediaRecorderPause(String terminal, int duration, Symbol trigger);
           break;
         case AstTeleEventTypes.mediaRecorderResume:
           //tL.mediaRecorderResume(String terminal, Symbol trigger);
           break;
         case AstTeleEventTypes.mediaSignalDetectorDetected:
           //tL.mediaSignalDetectorDetected(String terminal, Symbol[] sigs);
           break;
         case AstTeleEventTypes.mediaSignalDetectorOverflow:
           //tL.mediaSignalDetectorOverflow(String terminal, Symbol[] sigs);
           break;
         case AstTeleEventTypes.mediaSignalDetectorPatternMatched:
           //tL.mediaSignalDetectorPatternMatched(String terminal, Symbol[] sigs, int index);
           break;
         case AstTeleEventTypes.providerPrivateData:
           //tL.providerPrivateData(Serializable data, int cause);
           break;*/
         case AstTeleEventTypes.terminalConnectionCreated:
           tL.terminalConnectionCreated(call, address, address, cause);
           break;
         case AstTeleEventTypes.terminalConnectionDropped:
           tL.terminalConnectionDropped(call, address, address, cause);
           break;
         case AstTeleEventTypes.terminalConnectionHeld:
           tL.terminalConnectionHeld(call, address, address, cause);
           break;
         case AstTeleEventTypes.terminalConnectionRinging:
           tL.terminalConnectionRinging(call, address, address, cause);
           break;
         case AstTeleEventTypes.terminalConnectionTalking:
           tL.terminalConnectionTalking(call, address, address, cause);
           break;
         /*case AstTeleEventTypes.terminalPrivateData:
           tL.terminalPrivateData(String terminal, Serializable data, int cause);*/
         default:
           break;
       }
     }
 	}
 
 
   /**
    * returns the Channel for that uniqueId regardless of a CallId
    * @param uniqueId String
    * @return Channel
    */
   private Channel getChannelForUniqueId(String uniqueId)
   {
     return (Channel)asteriskManager.getChannels().get(uniqueId);
   }
 
   /**
    * Searches all callIds for this uniqueId.
    * @param uniqueId String
    * @return the callId when found, null otherwise
    */
   private AsteriskCallId getCallIdForUniqueId(String uniqueId)
   {
     for(Iterator i = calls.iterator(); i.hasNext(); )
     {
       AsteriskCallId next = (AsteriskCallId)i.next();
       if(next.getConnections().containsKey(uniqueId))
         return next;
     }
     return null;
   }
 
   /**
    * return the fitting address for the uniqueId
    * @param call AsteriskCallId
    * @param uniqueId String
    * @return String
    */
   private String getAddress(AsteriskCallId call, String uniqueId)
   {
     Object detail = call.getConnections().get(uniqueId);
     if(detail!=null)
       return ((AsteriskConnectionDetail)detail).address;
     return null;
   }
 
   /**
    * a simple debugging method
    * @param out is put to System.out
    */
   private void _o(String out)
   {
     if(OUT)
       System.out.println(out);
   }
 
   /**
    * returns a AsteriskConnectionDetail for the connection identified by:
    * @param call AsteriskCallId
    * @param uniqueId String
    * @return AsteriskConnectionDetail
    */
   private AsteriskConnectionDetail getDetail(AsteriskCallId call, String uniqueId)
   {
     return (AsteriskConnectionDetail)call.getConnections().get(uniqueId);
   }
 
   /**
    *
    * @param callerId
    * @return
    */
   private AsteriskCallId getCallForCallerId(String callerId)
   {
     for(Iterator i = calls.iterator(); i.hasNext(); )
     {
       AsteriskCallId call = (AsteriskCallId)i.next();
       for(Iterator j = call.getConnections().values().iterator(); j.hasNext(); )
       {
         AsteriskConnectionDetail detail = (AsteriskConnectionDetail)j.next();
         Channel chan = getChannelForUniqueId(detail.uniqueId);
         if(chan != null && chan.getCallerId().equals(callerId))
           return call;
       }
     }
     return null;
   }
 
   /**
    * returns the uniqueId of the calling-connection in a call
    * @param call
    * @param callerId
    * @return
    */
   private String getUniqueIdThroughCallerId(AsteriskCallId call, String callerId)
   {
     for( Iterator i= call.getConnections().entrySet().iterator(); i.hasNext(); )
     {
       AsteriskConnectionDetail detail = (AsteriskConnectionDetail)((Map.Entry) i.next()).getValue();
       if(detail.address.equals( callerId))
         return detail.uniqueId;
     }
     return null;
   }
 
 }
