 package com.teqlabs.bryan.OmniLinkII.OmniLinkIIImpl;
 
 import net.xeoh.plugins.base.annotations.PluginImplementation;
 import net.xeoh.plugins.base.annotations.events.Init;
 
 import net.homeip.mleclerc.omnilink.CommunicationException;
 import net.homeip.mleclerc.omnilink.MessageManager;
 import net.homeip.mleclerc.omnilink.NetworkCommunication;
 import net.homeip.mleclerc.omnilink.enumeration.ProtocolTypeEnum;
 import net.homeip.mleclerc.omnilink.enumeration.SystemTypeEnum;
 import net.homeip.mleclerc.omnilink.message.NotificationsCommand;
 import net.homeip.mleclerc.omnilink.messagebase.Message;
 import net.homeip.mleclerc.omnilink.messagebase.ReplyMessage;
 import net.homeip.mleclerc.omnilink.messagebase.RequestMessage;
 
 import com.teqlabs.bryan.OmniLinkII.OmniLinkII;
 
 @PluginImplementation
 public class OmniLinkIIImpl implements OmniLinkII {
 	
 	//Class variables
 	private MessageManager comm;
 	private CommandManager commandManager = new CommandManager();
 	
 	//Constants
 	private final static String PLUGIN_NAME = "OmniLinkII";
 	private final static SystemTypeEnum SYSTEM_TYPE = SystemTypeEnum.HAI_OMNI_PRO_II;
 	private final static String IP_ADDRESS = "192.168.1.4";
 	private final static int PORT = 4369;
 	private final static String PRIVATE_KEY = "a0-4e-e0-2b-4e-9f-9a-e1-04-81-1f-dc-e4-4f-e0-bf";
 	private final static ProtocolTypeEnum PROTOCOL_TYPE = ProtocolTypeEnum.HAI_OMNI_LINK_II; // or ProtocolTypeEnum.HAI_OMNI_LINK_II for Omni-Link II
 
 	@Init
 	public void init() {
 		
 		try {
 			this.comm = new NetworkCommunication(SYSTEM_TYPE, IP_ADDRESS, PORT, NetworkCommunication.DEFAULT_TIMEOUT, PRIVATE_KEY, PROTOCOL_TYPE);
 			if (PROTOCOL_TYPE == ProtocolTypeEnum.HAI_OMNI_LINK_II) {
 				// Register a listener to receive notifications from the controller
 				((NetworkCommunication) comm).addListener(new NetworkCommunication.NotificationListener() {
 					public void notify(Message notification) {
 						System.out.println("Notification: " + notification);
 					}
 				});
 			}
 			
 			//Open communication session
 			open();
 			
 			//Enable notifications
 			ReplyMessage notifications = comm.execute(new NotificationsCommand(true));
 			System.out.println(notifications);
 		
 			//Plugin initialized
 			System.out.println(PLUGIN_NAME + "Plugin initialized.");
 			
 		} catch (CommunicationException ce) {
 			// TODO Auto-generated catch block
 			System.err.println("Could not open " + PLUGIN_NAME + " communication session");
 			ce.printStackTrace();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 
 	private void open() throws CommunicationException, Exception {
 		this.comm.open();
 	}
 	
 	@Override
	public ReplyMessage executeCommand(String c) throws Exception {
 		
         RequestMessage msg = commandManager.createRequestMessage(c);
         if (msg != null && comm.isOpen())
         {
         	ReplyMessage reply = comm.execute(msg);
            return reply;
         } else if(!comm.isOpen()) {
         	open();
         	executeCommand(c);
         }
		return null;
 		
 	}
 
 }
