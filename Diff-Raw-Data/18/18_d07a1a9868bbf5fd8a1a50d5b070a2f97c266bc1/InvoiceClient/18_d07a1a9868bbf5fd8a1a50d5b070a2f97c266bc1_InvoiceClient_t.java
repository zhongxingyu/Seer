 package com.scg.net.client;
 
 import com.scg.domain.ClientAccount;
 import com.scg.domain.Consultant;
 import com.scg.domain.TimeCard;
 import com.scg.net.*;
import com.scg.util.Address;
 import com.scg.util.Name;
import com.scg.util.StateCode;
 
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dcostinett
  * Date: 2/10/13
  * Time: 12:23 PM
  *
  * The client of the InvoiceServer.
  */
 public class InvoiceClient {
     /** The start month for our test cases. */
     private static final int INVOICE_MONTH = Calendar.MARCH;
 
     /** The test year. */
     private static final int INVOICE_YEAR = 2006;
 
     private String host;
     private int port;
     private List<TimeCard> timeCardList;
 
     private List<ClientAccount> clients = new ArrayList<ClientAccount>();
     private List<Consultant> consultants = new ArrayList<Consultant>();
 
     private ThreadLocal<Socket> socket = new ThreadLocal<Socket>();
 
     /**
      * Construct an InvoiceClient with a host and port for the server.
      * @param host - the host for the server.
      * @param port - the port for the server.
      * @param timeCardList - the list of timeCards to send to the server
      */
     public InvoiceClient(String host, int port, List<TimeCard> timeCardList) {
         this.host = host;
         this.port = port;
         this.timeCardList = timeCardList;
 
         try {
             socket.set(new Socket(host, port));
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Runs this InvoiceClient, sending clients, consultants, and time cards to the server, then sending the command
      * to create invoices for a specified month.
      */
     public void run() {
 
         Name bob = new Name("Cowboy", "Bob", "The");
         Name bill = new Name("Cowboy", "Bill", "The");
         Name charlie = new Name("Cowboy", "Charlie", "The");
 
         consultants.add(new Consultant(bob));
         consultants.add(new Consultant(bill));
         consultants.add(new Consultant(charlie));
 
        ClientAccount c1 = new ClientAccount("Bob's Rodeo", bob);
        c1.setAddress(new Address("123 Main St", "Smallville", StateCode.WA, "98102"));
        clients.add(c1);

        ClientAccount c2 = new ClientAccount("Bill's Rodeo", bob);
        c1.setAddress(new Address("345 Main St", "Smallville", StateCode.WA, "98102"));
        clients.add(new ClientAccount(bill.toString(), bill));

        ClientAccount c3 = new ClientAccount("Chuck's Rodeo", bob);
        c1.setAddress(new Address("678 Main St", "Smallville", StateCode.WA, "98102"));
        clients.add(new ClientAccount(charlie.toString(), charlie));
 
         ObjectOutputStream oStream = null;
         try {
             oStream = new ObjectOutputStream(socket.get().getOutputStream());
 
             sendClients(oStream);
             sendConsultants(oStream);
             sendTimeCards(oStream);
 
             createInvoices(oStream, INVOICE_MONTH, INVOICE_YEAR);
 
             sendDisconnect(oStream);
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             if (oStream != null) {
                 try {
                     oStream.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
             if (socket.get() != null) {
                 try {
                     socket.get().close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
 
     /**
      * Set the desired output directory
      */
     private void setOutputDirectory(String dir) {
 
     }
 
     /**
      * Send the clients to the server.
      * @param out - the output stream connecting this client to the server.
      */
     public void sendClients(ObjectOutputStream out) {
         for (ClientAccount client : clients) {
             try {
                 AddClientCommand cmd = new AddClientCommand(client);
                 out.writeObject(cmd);
                 out.flush();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * Send the consultants to the server.
      * @param out - the output stream connecting this client to the server.
      */
     public void sendConsultants(ObjectOutputStream out) {
         for (Consultant consultant : consultants) {
             try {
                 AddConsultantCommand cmd = new AddConsultantCommand(consultant);
                 out.writeObject(cmd);
                 out.flush();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * Send the time cards to the server.
      * @param out - the output stream connecting this client to the server.
      */
     public void sendTimeCards(ObjectOutputStream out) {
         for (TimeCard tc : timeCardList) {
             try {
                 AddTimeCardCommand cmd = new AddTimeCardCommand(tc);
                 out.writeObject(cmd);
                 out.flush();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * Send the disconnect command to the server.
      * @param out - the output stream connecting this client to the server.
      */
     public void sendDisconnect(ObjectOutputStream out) {
         DisconnectCommand cmd = new DisconnectCommand();
         try {
             out.writeObject(cmd);
             out.flush();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Send the command to the server to create invoices for the specified month.
      * @param out - the output stream connecting this client to the server.
      * @param month - the month to create invoice for
      * @param year - the year to create invoice for
      */
     public void createInvoices(ObjectOutputStream out,
                                int month,
                                int year) {
         Calendar cal = Calendar.getInstance();
         cal.set(Calendar.MONTH, month);
         cal.set(Calendar.YEAR, year);
         cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
         CreateInvoicesCommand cmd = new CreateInvoicesCommand(cal.getTime());
         try {
             out.writeObject(cmd);
             out.flush();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Send the quit command to the server.
      */
     public void sendQuit() {
         ShutdownCommand cmd = new ShutdownCommand();
         try {
             socket.set(new Socket(host, port));
             ObjectOutputStream out = new ObjectOutputStream(socket.get().getOutputStream());
             out.writeObject(cmd);
             out.flush();
             socket.get().close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 }
