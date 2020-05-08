 package org.enernoc.open.oadr2.xmpp;
 
 import java.util.List;
 import java.util.Scanner;
 
 import javax.xml.bind.JAXBException;
 import javax.xml.datatype.DatatypeConfigurationException;
 
 import org.enernoc.open.oadr2.model.EventResponses;
 import org.enernoc.open.oadr2.model.EventResponses.EventResponse;
 import org.enernoc.open.oadr2.model.OadrCreatedEvent;
 import org.enernoc.open.oadr2.model.OadrDistributeEvent;
 import org.enernoc.open.oadr2.model.OadrDistributeEvent.OadrEvent;
 import org.jivesoftware.smack.PacketListener;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.packet.IQ;
 import org.jivesoftware.smack.packet.Packet;
 
 public class OadrApp {
    static String username;
    static String password;
    static boolean isVen;
    static ConnHandler connHandler = new ConnHandler();
    static XMPPConnection ven = null, vtn = null;
    static final String OADR2_XMLNS = "http://openadr.org/oadr-2.0a/2012/07";
 
    public static void main(String[] args)
          throws DatatypeConfigurationException, JAXBException,
          InterruptedException {
 
       XMPPConnection.DEBUG_ENABLED = true;
       loginProcedure();
 
       // vtn operations
       if (!isVen) {
          vtnOperations();
       }
 
       // ven operations
       else {
          venOperations();
       }
    }
 
    private static void vtnOperations() throws DatatypeConfigurationException,
          JAXBException {
       @SuppressWarnings("resource")
       Scanner s = new Scanner(System.in);
 
       // connect and add listener
       vtnConnect();
       PacketListener oadrCreatedEventListener = new PacketListener() {
          @Override
          public void processPacket(Packet packet) {
             printPacket(packet);
          }
       };
       vtn.addPacketListener(oadrCreatedEventListener, new OADR2PacketFilter());
 
       // display roster, select contact
       while (true) {
          try {
             connHandler.displayVtnRoster();
          }
          catch (InterruptedException e1) {
             e1.printStackTrace();
          }
          System.out.println("------------------------");
          System.out
                .println("Who do you want to send a payload to?"
                      + "\nType contacts full JID (including resource, i.e. contact@domain.com/resource). "
                      + "\nType 'refresh()' to refresh list and presence status:"
                      + "\nType 'quit()' to quit");
 
          String to = "";
          s = new Scanner(System.in);
          boolean valid = false;
 
          while (!valid) {
             to = s.nextLine();
             valid = true;
 
             if (to.equals("quit()")) {
                System.out.println("Quitting...");
                connHandler.disconnect();
                System.exit(0);
             }
             else if (to.equals("refresh()")) {
                valid = false;
 
                try {
                   connHandler.displayVtnRoster();
                }
                catch (InterruptedException e) {
                   e.printStackTrace();
                }
 
                System.out.println("------------------------");
                System.out.println("Who do you want to send a payload to?"
                      + "\nType contacts full JID. "
                      + "\nType 'refresh' to refresh list and presence status:"
                      + "\nType 'quit()' to quit");
             }
             else if (!connHandler.checkIfExists(to, vtn.getRoster())) {
                valid = false;
                System.out.println("Contact does not exist. Try again: ");
             }
          }
 
          System.out.println("---------------------------");
          System.out.println("All payloads will be sent to: " + to + ", name: "
                + vtn.getRoster().getEntry(connHandler.getBareJid()).getName());
          System.out.println("---------------------------");
 
          // Create IQ with oadr packet extension and send to "to"
          while (true) {
             System.out
                   .println("Do you want to send an OadrDistributeEvent payload to "
                         + to
                         + "? Type 'yes' or 'no' \n Type 'quit()' to quit"
                         + "\n Type 'reselect()' to select new contact");
             String ans = s.next();
 
             if (ans.equals("yes")) {
                OadrDistributeEvent ode = new OadrPayloadFactory()
                      .createEventPayload();
                IQ iq = connHandler.createIQ(to, ode);
                vtn.sendPacket(iq);
 
                System.out
                      .println("Sending the following oadrDistributeEvent payload to "
                            + to + ": ");
                ConnHandler.testNamespace(ode);
             }
 
             else if (ans.equals("no"))
                System.out.println("Message not sent");
 
             else if (ans.equals("quit()")) {
                System.out.println("Quitting...");
                connHandler.disconnect();
                System.exit(0);
             }
 
             else if (ans.equals("reselect()"))
                break;
 
             else
                System.out
                      .println("Invalid answer. 'yes', 'no', 'reselect', or 'quit()'");
          }
       }
    }
 
    private static void venOperations() throws InterruptedException {
       @SuppressWarnings("resource")
       Scanner s = new Scanner(System.in);
       venConnect();
 
       // add listener to ven
       PacketListener oadrDistributeEventListener = new PacketListener() {
          @Override
          public void processPacket(Packet packet) {
             System.out.println("OadrDistributeEvent payload received!: ");
 
             // grab OadrDistributeEvent payload
             printPacket(packet);
             OADR2PacketExtension oadrExtension = (OADR2PacketExtension) packet
                   .getExtension(OADR2_XMLNS);
 
            if (!oadrExtension.getElementName().equals("oadrDistributeEvent")) {
                System.err
                      .println("Incoming payload is not oadrDistributeEvent. Exiting");
                System.exit(1);
             }
             OadrDistributeEvent ode = (OadrDistributeEvent) oadrExtension
                   .getPayload();
 
             // parsing oadrDistributeEvent payload for relevant elements and
             // instantiating OadrCreatedEvent payload via factory method
             System.out.println("parsing oadrDistributeEvent payload...");
             OadrCreatedEvent oce = new OadrPayloadFactory()
                   .createResponsePayload(ode);
 
             // print xml of OadrCreatedEvent payload
             try {
                Thread.sleep(2500L);
                System.out.println("Responding with oadrCreatedEvent payload: ");
                Thread.sleep(2500L);
                ConnHandler.testNamespace(oce);
             }
             catch (Exception e) {
                e.printStackTrace();
             }
             finally {
                System.out.println();
             }
 
             // creates IQ packet for oadrCreatedEvent and sends to vtn
             System.out.println("sending oadrCreatedEvent payload...");
             try {
                IQ oceiq = new ConnHandler().createIQ(packet.getFrom(), oce);
                ven.sendPacket(oceiq);
             }
             catch (Exception e) {
                e.printStackTrace();
             }
 
             System.out.println("oadrCreatedEvent payload sent!");
          }
       };
       ven.addPacketListener(oadrDistributeEventListener,
             new OADR2PacketFilter());
 
       while (true) {
          System.out.println("Would you like to quit? Type 'quit()' to exit"
                + "\nType 'show()' to show buddy list");
          String ans = s.next();
 
          if (ans.equals("quit()")) {
             System.out.println("Quitting...");
             connHandler.disconnect();
             System.exit(0);
          }
          else if (ans.equals("show()"))
             connHandler.displayVenRoster();
          else
             System.out.println("Invalid answer");
       }
    }
 
    private static boolean VenOrVtn(String msg) {
       @SuppressWarnings("resource")
       Scanner s = new Scanner(System.in);
       boolean isVen = true, valid = false;
 
       while (!valid) {
          String ans = s.next();
          valid = true;
          if (ans.equals("ven"))
             isVen = true;
          else if (ans.equals("vtn"))
             isVen = false;
          else {
             System.err.println("      Invalid choice. " + msg);
             valid = false;
          }
       }
 
       return isVen;
    }
 
    private static boolean vtnConnect() {
       boolean pass = false;
 
       while (!pass) {
          try {
             vtn = connHandler.connect(username, password, "talk.google.com",
                   "vtn");
             connHandler.setVtnConnection(vtn);
             return true;
          }
          catch (XMPPException e) {
             System.out.println("Login error. Invalid username or password."
                   + "\nTry again:");
             loginProcedure();
             pass = vtnConnect();
          }
       }
 
       return pass;
    }
 
    private static boolean venConnect() {
       boolean pass = false;
 
       while (!pass) {
          try {
             ven = connHandler.connect(username, password, "talk.google.com",
                   "ven");
             connHandler.setVenConnection(ven);
             return true;
          }
          catch (XMPPException e) {
             System.out.println("Login error. Invalid username or password."
                   + "\nTry again:");
             loginProcedure();
             pass = venConnect();
          }
       }
 
       return pass;
    }
 
    private static void loginProcedure() {
       @SuppressWarnings("resource")
       Scanner s = new Scanner(System.in);
 
       System.out.println("login: ");
       System.out.print("   username: ");
       username = s.next();
       System.out.print("   password: ");
       password = s.next();
 
       // check if vtn or ven
       System.out.print("   ven or vtn? (Type 'ven' or 'vtn' to specify): ");
       isVen = VenOrVtn("Type 'ven' or 'vtn' to specify");
    }
 
    private static void printPacket(Packet packet) {
       final String OADR2_xmlns = "http://openadr.org/oadr-2.0a/2012/07";
 
       if (packet == null) {
          System.err.println("packet is null! Exiting...");
          System.exit(1);
       }
 
       OADR2PacketExtension ope = (OADR2PacketExtension) packet
             .getExtension(OADR2_xmlns);
 
       System.out.println("packet id: " + packet.getPacketID());
       System.out.println("packet from: " + packet.getFrom());
       System.out.println("packet class: " + packet.getClass());
       System.out.println("packet namespace: " + packet.getXmlns());
       System.out.println("packet extension class: " + ope.getClass());
       System.out.println("packet extension namespace: " + ope.getNamespace());
       System.out.println("packet extension element name: "
             + ope.getElementName());
 
       if (ope.getElementName().equals("oadrCreatedEvent")) {
          OadrCreatedEvent payload = (OadrCreatedEvent) ope.getPayload();
          
          @SuppressWarnings("unchecked")
          List<EventResponses.EventResponse> list = (List<EventResponses.EventResponse>) payload
                .getEiCreatedEvent().getEventResponses();
          
          for (int i = 0; i < list.size(); i++) {
             System.out.println();
             EventResponses.EventResponse e = (EventResponse) list.get(i);
             System.out.println("Request id: " + e.getRequestID());
             System.out.println("Response description: "
                   + e.getResponseDescription());
             System.out.println("OptType: " + e.getOptType());
             System.out.println("Event ID: "
                   + e.getQualifiedEventID().getEventID());
          }
 
       }
       else if (ope.getElementName().equals("oadrDistributeEvent")) {
          OadrDistributeEvent payload = (OadrDistributeEvent) ope.getPayload();
          List<OadrEvent> list = (List<OadrEvent>) payload.getOadrEvents();
 
          System.out.println("Request id: " + payload.getRequestID());
 
          for (int i = 0; i < list.size(); i++) {
             System.out.println();
             OadrEvent e = (OadrEvent) list.get(i);
             System.out.println("Oadr Response Required: "
                   + e.getOadrResponseRequired());
             System.out.println("Event ID: "
                   + e.getEiEvent().getEventDescriptor().getEventID());
             System.out.println("Event status: "
                   + e.getEiEvent().getEventDescriptor().getEventStatus());
             System.out.println("Date and time: "
                   + e.getEiEvent().getEventDescriptor().getCreatedDateTime());
          }
       }
       else {
          System.err
                .println("Packet ext. is neither oadrCreatedEvent or oadrDistributeEvent. Exiting");
          System.exit(1);
       }
 
       System.out.println();
    }
 
 }
