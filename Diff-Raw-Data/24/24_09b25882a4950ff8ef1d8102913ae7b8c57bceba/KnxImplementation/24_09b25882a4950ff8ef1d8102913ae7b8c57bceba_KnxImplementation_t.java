 package esir.dom12.moduleKnx;
 
 import org.kevoree.annotation.*;
 import org.kevoree.framework.AbstractComponentType;
 import org.kevoree.framework.MessagePort;
import tuwien.auto.calimero.GroupAddress;
 import tuwien.auto.calimero.exception.KNXException;
 import tuwien.auto.calimero.knxnetip.KNXnetIPConnection;
 import tuwien.auto.calimero.link.KNXNetworkLinkIP;
 import tuwien.auto.calimero.link.medium.TPSettings;
 import tuwien.auto.calimero.process.ProcessCommunicator;
 import tuwien.auto.calimero.process.ProcessCommunicatorImpl;
 
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.UnknownHostException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: guillaumelefloch
  * Date: 12/23/12
  * Time: 11:06 AM
  * To change this template use File | Settings | File Templates.
  */
 
 /**
  * Definition des ports
  */
 @Library(name = "JavaSE")
 @Requires({
         @RequiredPort(name = "getData", type = PortType.MESSAGE, optional = true),
         @RequiredPort(name = "log", type = PortType.MESSAGE, optional = true)
 })
 @Provides({
         @ProvidedPort(name = "setEquipementState", type = PortType.MESSAGE),
         @ProvidedPort(name = "getEquipementState", type = PortType.MESSAGE)
 
 })
 @DictionaryType({
        @DictionaryAttribute(name = "ipMaquette", defaultValue = "192.168.1.193", optional = true),
        @DictionaryAttribute(name = "myIp", defaultValue = "192.168.1.100", optional = true)
 })
 @ComponentType
 public class KnxImplementation extends AbstractComponentType {
 
     /**
      * Attributs
      */
     String ipPasserelle = (String) getDictionary().get("ipMaquette");  // Récupere l'addres IP dans le Dictionnaire
    String myIp = null;
     Boolean connect = false;                                           // Boolean pour savoir si l'on est connecté
     KNXNetworkLinkIP netLinkIp;                                        // Variable Calimero
     ProcessCommunicator pc;                                            // Variable Calimero
     Logger log = Logger.getLogger(KnxImplementation.class.getName());  // Variable logger
 
     // Variables neccessaire pour agir sur les équipements
     String addComposant;                                                // Adresse de l'équipement
     float value;                                                       // Prochaine valeur du composant
 
     // Variable pour le Thread
 
 
     @Start
     public void startComponent() {
 
        ipPasserelle = (String) getDictionary().get("ipMaquette");      // Get Ip
        myIp =(String) getDictionary().get("myIp");                     // Get Ip
         System.out.println("Module KNX :: Start");
        connexionKnx(ipPasserelle);                                     // Connexion Calimero
     }
 
     @Stop
     public void stopComponent() {
         System.out.println("Module KNX :: Stop");
     }
 
     @Update
     public void updateComponent() {
         System.out.println("Module KNX :: Update");
         this.stopComponent();                                           // Stop the component
         this.startComponent();                                          // Start the component
     }
 
     /**
      * Implémentation de la methode setComposant
      */
 
     @Port(name = "setEquipementState")
     public void setComposant(Object o) {
 
         /**
          * Variable de la classe
          */
         String data = null;                                             // Variable pour stocke les données du port
         String[] temp;                                                 // Tableau pour gérer les données
         Boolean value = false;                                          // Valeur appliqué a l'équipement
 
         // On lit les paramètres ecrit sur le port
         data = new String(o.toString());                                // Récupere les paramètres
         temp = data.split(":");                                         // Split les parametres
         addComposant = temp[0];                                         // Récupere l'adresse de l'équipement
         String valueTemp = temp[1];                                     // Get the value
 
         log.log(Level.INFO, "Valeur récupérés :: addComposant :: " + addComposant + " value :: " + valueTemp);
 
         // Convert the value into a boolean
         // Datatype we receive:
         // 2/0/2:false
 
         log.log(Level.INFO, "Difference between false :: " + valueTemp.compareTo("false"));
 
         if (valueTemp.compareTo("false") == 0) {
             value = false;
         } else if (valueTemp.compareTo("true") == 0) {
             value = true;
         }
         log.log(Level.INFO, "Valeur Boolean :: " + value);
 
         MessagePort dataLog = getPortByName("log", MessagePort.class);
         if (dataLog != null) {
             dataLog.process("L'etat de l'équipement :: " + addComposant + " est maintenant :: " + value);
         }
 
         // Connexion à la passerelle KNX (si l'on est pas deja connecté)
       /*  if (connect == false) {
             connexionKnx(ipPasserelle);                                 // Connexion a la passerelle
             connect = true;
         }
 
         // Action sur l'équipement
         try {
             pc.write(new GroupAddress(addComposant), value);            // Commande l'équipement
             */
 
         // Ecrit sur le port log que tout a bien été effectué
 
 
 
        /* } catch (KNXException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             log.log(Level.WARNING, "KNX Exception lors de l'ecriture sur le composant");
 
             // Ecrit sur le port log l'erreur qui est retourné
             MessagePort dataPort = getPortByName("log",MessagePort.class);
             if(dataPort != null) {
                 dataPort.process("Erreur lors la modification de l'état d'un équipement :: " + e);
              }
 
             // Si il y a un problème on returne false et on se déconnecte
             deconnexionKNX(netLinkIp);
 
             log.log(Level.INFO, "Déconnexion");
         }
 
         // Sinon on retourne true et on se déconnect
         deconnexionKNX(netLinkIp);
         */
         log.log(Level.INFO, "Déconnexion");
     }
 
     /**
      * Implémentation de la méthode GetState,
      * Cette méthode retourne la valeur de chaque équipement
      */
     @Port(name = "getEquipementState")
     public void getEquipementStates(Object o) {
 
         /* Variable de la Classe */
 
         int i = 0;                                                             // Variable utile pour les itérations
         String data = null;                                                    // Variable qui stocke les donnée du port
         String[] dataTemp = new String[4];                                     // Tableau temporaire pour le split
         String[] tempName = new String[4];                                                  // Tableau pour stocké les noms
         String[] tempAdd = new String[4];                                      // Tableau pour stocké les addresses
         String[] tempsData = new String[4];                                    // Tableau temporaire
         String[] tempResult = new String[4];                                   // Tableau pour sauvegarder les résults
         boolean tempValue = false;                                             // Temp val to store result
         String finalData = null;                                               // Variable qui sera ecrite sur le port
         MessagePort portSorti = getPortByName("getData", MessagePort.class);   // Variable du port de sorti
 
         // Nous Allons recevoir une trame de la forme suivante:
         // BOARD_LIGHT:0/0/3;
         // ROOM_LIGHT:0/0/4;
         // SHUTTER:0/0/5;
         // BRIGHTNESS:0/0/6
 
         /* Parse ans extract Data */
         data = new String(o.toString());                                       // Convertie les données recu en String
         dataTemp = data.split(";");                                            // Split les données par ligne
 
         log.log(Level.INFO, dataTemp[0]);
 
         // Get name and address
        for (i = 0; i < dataTemp.length; i++) {
             if (dataTemp[i] != null) {
                 tempsData = new String[0];                                     // Re initialise le tableau temporaire
                 String temp = dataTemp[i];
                 tempsData = temp.split(":");
                 log.log(Level.INFO, String.valueOf(tempsData));
                 if (tempsData.length > 1) {
                     tempName[i] = tempsData[0];
                     tempAdd[i] = tempsData[1];
                 }
             }
         }
 
         /********** FAKE KNX MODULE **********/
 
         tempResult[0] = "false";
         tempResult[1] = "true";
         tempResult[2] = "false";
         tempResult[3] = "400";
 
 
         // Once adresse are extract, we can make request
 
         // Check if we are connected to the KNX bus
        if (connect != true) {
             connexionKnx(ipPasserelle);                                        // Crée le pont entre la machine et KNX
         }
 
 
         try {
 
             // Pour les 3 premieres addresse on lit juste le bus KNX
             for (i = 0; i < 3; i++) {
                 tempValue = pc.readBool(new GroupAddress(tempAdd[i]));         // Récupere la valeur boolean du device
                 tempResult[i] = String.valueOf(tempValue);                     // Ajoute le résultat dans le tableau
             }
 
             // Le quatrieme équipement étant un capteur, nous avons besoin d'un DATAPOINT
 
             /**
              *  DATAPOINT !!!!!
              */
 
        } catch (KNXException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
 
         // Une fois toutes les données récupéré, on créé le String que l'on va envoyé
         for (i = 0; i < tempResult.length; i++) {
             if (i == 0) {
                 finalData = tempName[i] + ":" + tempResult[i] + ";";           // Cas de la premiere itération
             } else {
                 finalData = finalData + tempName[i] + ":" + tempResult[i] + ";";
             }
         }
 
         // Ecris les données sur le port de Sortie
         if (portSorti != null) {
             portSorti.process(finalData);
         }
     }
 
     /**
      * Connexion via calimero a la passerelle KNX
      *
      * @param ip: adresse IP de la passerelle
      */
     public void connexionKnx(String ip) {
 
         log.log(Level.INFO, "Connexion a la passerelle");
         try {
            netLinkIp = new KNXNetworkLinkIP(KNXNetworkLinkIP.TUNNEL, new InetSocketAddress(InetAddress.getByName(myIp), 0), new InetSocketAddress(
                     InetAddress.getByName(ip),
                     KNXnetIPConnection.IP_PORT), false, new TPSettings(
                     false));
             log.log(Level.INFO, "KNX netLinkIp créé");
 
             pc = new ProcessCommunicatorImpl(netLinkIp);
 
 
             log.log(Level.INFO, "Process Communicator créé");
         } catch (KNXException e) {
 
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             log.log(Level.WARNING, "KNX Exception " + e);
         } catch (UnknownHostException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             log.log(Level.WARNING, "UnKnownHostException " + e);
         }
 
         connect = true; // Indique que l'on est connecté a la passerelle KNX
     }
 
 
     /**
      * Deconnexion a la passerelle KNX
      *
      * @param linkIp
      */
     public void deconnexionKNX(KNXNetworkLinkIP linkIp) {
 
         try {
             linkIp.close();
         } finally {
             if (linkIp != null) {
                 linkIp.close();
 
                 log.log(Level.INFO, "Connection closed");
                 connect = false; // Indique que l'on est déconnecté de la passerelle
             }
         }
 
     }
 }
