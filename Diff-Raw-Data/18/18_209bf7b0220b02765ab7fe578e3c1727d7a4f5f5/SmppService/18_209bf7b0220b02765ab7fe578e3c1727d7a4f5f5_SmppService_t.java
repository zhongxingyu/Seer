 package org.motechproject.spike;
 
 import org.smslib.AGateway;
 import org.smslib.IOutboundMessageNotification;
 import org.smslib.OutboundMessage;
 import org.smslib.Service;
 import org.smslib.smpp.BindAttributes;
 import org.smslib.smpp.jsmpp.JSMPPGateway;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.util.Properties;
 
 @Component
 public class SmppService {
     private static final String GATEWAY_ID = "SMPP_GATEWAY_ID";
 
     private Service service;
    private boolean enabled;
 
     @Autowired
     public SmppService(Properties smppProperties) throws Exception {
         final String enabled = smppProperties.getProperty("smpp.enabled");
        this.enabled = Boolean.valueOf(enabled);
        if(!this.enabled){
             return;
         }
         final String name = smppProperties.getProperty("smpp.name");
         final String password = smppProperties.getProperty("smpp.password");
         final String host = smppProperties.getProperty("smpp.host");
         final int port = Integer.valueOf(smppProperties.getProperty("smpp.port"));
         final BindAttributes bindAttributes = new BindAttributes(name, password, null, BindAttributes.BindType.TRANSCEIVER);
 
         service = Service.getInstance();
         service.addGateway(new JSMPPGateway(GATEWAY_ID, host, port, bindAttributes));
 
         service.setOutboundMessageNotification(new IOutboundMessageNotification() {
             @Override
             public void process(AGateway aGateway, OutboundMessage outboundMessage) {
                 System.out.println("Outbound message notification. " + outboundMessage);
             }
         });
         service.startService();
     }
 
     public void sendMessage() throws Exception {
         OutboundMessage outboundMessage = new OutboundMessage();
         outboundMessage.setRecipient("12345");
         outboundMessage.setText("Hola Mundo...");
         outboundMessage.setGatewayId(GATEWAY_ID);
 
         service.queueMessage(outboundMessage);
     }

    public boolean isEnabled() {
        return enabled;
    }
 }
