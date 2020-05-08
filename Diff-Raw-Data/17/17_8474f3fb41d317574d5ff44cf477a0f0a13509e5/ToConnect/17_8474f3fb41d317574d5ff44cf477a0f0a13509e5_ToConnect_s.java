 package knx;
 
 
 import tuwien.auto.calimero.CloseEvent;
 import tuwien.auto.calimero.FrameEvent;
 import tuwien.auto.calimero.GroupAddress;
 import tuwien.auto.calimero.exception.KNXException;
 import tuwien.auto.calimero.exception.KNXFormatException;
 import tuwien.auto.calimero.exception.KNXTimeoutException;
 import tuwien.auto.calimero.knxnetip.Discoverer;
 import tuwien.auto.calimero.knxnetip.KNXnetIPConnection;
 import tuwien.auto.calimero.knxnetip.servicetype.SearchResponse;
 import tuwien.auto.calimero.knxnetip.util.HPAI;
 import tuwien.auto.calimero.link.KNXLinkClosedException;
 import tuwien.auto.calimero.link.KNXNetworkLinkIP;
 import tuwien.auto.calimero.link.event.NetworkLinkListener;
 import tuwien.auto.calimero.link.medium.TPSettings;
 import tuwien.auto.calimero.process.ProcessCommunicator;
 import tuwien.auto.calimero.process.ProcessCommunicatorImpl;
 
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.UnknownHostException;
 
 
 public class ToConnect implements IntToConnect {
     //variables d'instances
     private String adresseIP_maquette;
     private String adresseIP_PC;
     private KNXNetworkLinkIP netLinkIp = null;
     private ProcessCommunicator pc = null;
 
     //constructeurs
     public ToConnect() {
         adresseIP_maquette = searchSketch();
         System.out.println("Sketch address " + adresseIP_maquette + " found");
         adresseIP_PC = NSLookup.IPAddress("localhost").toString();
         System.out.println("Local address " + adresseIP_PC + " found");
     }
 
     public ToConnect(String adressePC) {
         adresseIP_PC = adressePC;
         adresseIP_maquette = searchSketch();
         System.out.println("Sketch address " + adresseIP_maquette + " found");
     }
 
     public ToConnect(String adressePC, String adresseMaquette) {
 
         adresseIP_PC = adressePC;
         adresseIP_maquette = adresseMaquette;
 
     }
     //----------------------------------------------------------------------------------------------
     //----------------------------------------------------------------------------------------------
 
     //méthodes
     @Override
     public void connected() {
         // TODO Auto-generated method stubadresseIP_PC
         System.out.println("Tente de se connecter : adresse PC : " + adresseIP_PC + "et adresse maquette : " + adresseIP_maquette);
         try {
             netLinkIp = new KNXNetworkLinkIP(KNXNetworkLinkIP.TUNNEL,
                     new InetSocketAddress(InetAddress.getByName(adresseIP_PC), 0),
                     new InetSocketAddress(InetAddress.getByName(adresseIP_maquette), KNXnetIPConnection.IP_PORT), false, new TPSettings(false));
 
             pc = new ProcessCommunicatorImpl(netLinkIp);
             System.out.println("*** Connected ***");
 
         } catch (UnknownHostException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (KNXException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
     }
 
     //--------------------------------------------------------------------------------------------------
     @Override
     public void disconnected() {
         // TODO Auto-generated method stub
         if (netLinkIp != null) {
             netLinkIp.close();
             System.out.println("Connexion closed");
         } else {
             System.out.println("Error : pas de tunnel créé!!!");
         }
     }
 
     //--------------------------------------------------------------------------------------------------
     @Override
     public void write(String adresseGroupe, boolean bool) {
         // TODO Auto-generated method stub
         try {
             pc.write(new GroupAddress(adresseGroupe), bool);
         } catch (KNXTimeoutException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (KNXLinkClosedException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (KNXFormatException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
     }
 
     //------------------------------------------------------------------------------------------------------
     @Override
     public String read(String adresseGroupe) {
         // TODO Auto-generated method stub
        String valeur = "";
         try {
             valeur = pc.readString(new GroupAddress(adresseGroupe));
         } catch (KNXFormatException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (KNXException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         //System.out.println(valeur);
         return valeur;
     }
 
     //------------------------------------------------------------------------
     //------------------------------------------------------------------------
     public void listener(final String adresse) {
         netLinkIp.addLinkListener(new NetworkLinkListener() {
 
             @Override
             public void confirmation(FrameEvent arg0) {
                 // TODO Auto-generated method stub
 
             }
 
             @Override
             public void indication(FrameEvent arg0) {
                 // TODO Auto-generated method stub
                 String adresseEnvoyee = ((tuwien.auto.calimero.cemi.CEMILData) arg0.getFrame()).getDestination().toString();
                 if (adresseEnvoyee.equals(adresse)) {
 
                 }
 
             }
 
             @Override
             public void linkClosed(CloseEvent arg0) {
                 // TODO Auto-generated method stub
 
             }
 
         });
     }
 
     //--------------------------------------------------------------------------
     //----------------------------------------------------------------------
     public String searchSketch() {
         try {
             System.out.println("ToConnect: Sketch searching ...");
             Discoverer disc = new Discoverer(KNXnetIPConnection.IP_PORT, false);
             disc.startSearch(1, true);
             SearchResponse[] resp = disc.getSearchResponses();
 
             for (SearchResponse r : resp) {
                 HPAI hpai = r.getControlEndpoint();
                 String ipMaq = hpai.getAddress().toString();
                 adresseIP_maquette = ipMaq.replace("/", "");
                 System.out.println("ToConnect: Sketch is found");
                 System.out.println("ToConnect: Sketch address: " + ipMaq);
             }
         } catch (KNXException e) {
             e.printStackTrace();
             System.out.println("ToConnect: Exception: " + e.toString());
         }
           return adresseIP_maquette;
     }
 
     @Override
     public String getProtocol() {
         return "KNX";  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public KNXNetworkLinkIP getNetLink() {
         return netLinkIp;  //To change body of implemented methods use File | Settings | File Templates.
     }
 }
 
 
 
 
 
 
 
 
