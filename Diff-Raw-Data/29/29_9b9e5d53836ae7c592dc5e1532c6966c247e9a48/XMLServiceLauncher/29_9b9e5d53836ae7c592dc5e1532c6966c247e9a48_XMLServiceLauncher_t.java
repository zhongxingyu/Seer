 /**
  *
  * Software written by Remi Emonet.
  *
  */
 package com.heeere.eaa.adaptertools;
 
 import fr.prima.omiscid.user.connector.ConnectorType;
 import fr.prima.omiscid.user.connector.Message;
 import fr.prima.omiscid.user.exception.InvalidDescriptionException;
 import fr.prima.omiscid.user.exception.MessageInterpretationException;
 import fr.prima.omiscid.user.exception.UnknownVariable;
 import fr.prima.omiscid.user.service.Service;
 import fr.prima.omiscid.user.service.ServiceFactory;
 import fr.prima.omiscid.user.service.impl.ServiceFactoryImpl;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Starts an omiscid service from an xml description. Handles specific features
  * such as code that get executed by the service.
  */
 public class XMLServiceLauncher {
 
     public static void main(String[] args) throws FileNotFoundException, InvalidDescriptionException, IOException {
         if (args.length != 1) {
             System.err.println("This program takes only one parameter: the input file (xml).");
             return;
         }
         final ServiceFactory f = new ServiceFactoryImpl();
         Service s = f.createFromXML(new FileInputStream(args[0]));
 
         if (Adapters.isAnAdapterFactory(s)) {
            String startCode_ = null;
            if (hasVariable(s, "start")) {
                startCode_ = s.getVariableValue("start");
                s.setVariableValue("start", "(((hidden)))"); // shorten the description of the service to work around a problem in omiscid
            }
            String adapterCode_ = null;
            if (hasVariable(s, "code")) {
                adapterCode_ = s.getVariableValue("code");
                s.setVariableValue("code", "(((hidden)))"); // shorten the description of the service to work around a problem in omiscid
            }
            final String startCode = startCode_;
            final String adapterCode = adapterCode_;
             s.addConnector("create", "instantiation connector", ConnectorType.INPUT);
             s.addConnectorListener("create", new ConnectorListenerAdapter() {
                 @Override
                 public void messageReceived(Service service, String connector, Message message) {
                     try {
                         AdapterInstance adapter = Adapters.createAdapterFrom(f, service, message.getBufferAsString(), startCode, adapterCode);
                         adapter.start();
                     } catch (MessageInterpretationException ex) {
                         Logger.getLogger(XMLServiceLauncher.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
             });
         }
 
         s.start();
     }
 
     private static boolean hasVariable(Service s, String name) {
         try {
             s.getVariableValue(name);
             return true;
         } catch (UnknownVariable ex) {
             return false;
         }
     }
 }
