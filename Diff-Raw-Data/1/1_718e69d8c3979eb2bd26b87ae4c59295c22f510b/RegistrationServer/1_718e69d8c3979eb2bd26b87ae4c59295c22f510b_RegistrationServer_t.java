 package edu.ncsu.ip.gogo.rs.server;
 
 
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.*;
 import java.io.*;
 
 import edu.ncsu.ip.gogo.dao.KeepAliveRequest;
 import edu.ncsu.ip.gogo.dao.LeaveRequest;
 import edu.ncsu.ip.gogo.dao.MessageRequest;
 import edu.ncsu.ip.gogo.dao.MessageResponse;
 import edu.ncsu.ip.gogo.dao.PQueryRequest;
 import edu.ncsu.ip.gogo.dao.PQueryResponse;
 import edu.ncsu.ip.gogo.dao.PeerInfo;
 import edu.ncsu.ip.gogo.dao.RegisterRequest;
 import edu.ncsu.ip.gogo.dao.RegisterResponse;
 import edu.ncsu.ip.gogo.rs.utils.Utils;
 
 
 public class RegistrationServer {
 
     private final static String myIp;
     private final static String myOs;
     private final int regServerPort;
     private int cookieSeed ;
     private final long ttlDecrementInterval = 10000;     // 10 seconds
     private final String STATUS_OK = "OK";
     private final String STATUS_ERROR = "ERROR";
     private final String version = "P2P-DI-GOGO/1.0";
     
     
     private List<Peer> peerList;
     
     static {
         myIp = Utils.getLocalIpAddress();
         myOs = Utils.getOS();
     }
     
     public RegistrationServer(int prt, int cookie) {
         regServerPort = prt;
         cookieSeed = cookie;
     }
 
     public void init()  {
         
          peerList = new LinkedList<Peer>();
          startTTLDecrementThread(ttlDecrementInterval);
          
          ServerSocket serverSocket = null;
          try {
              serverSocket = new ServerSocket(regServerPort);
          } catch (IOException e) {
              System.out.println("Could not start Registration server on : "+ regServerPort + ". Exiting!");
              System.exit(1);
          }
          
          System.out.println("RegistrationServer.init() - Started RegistrationServer on port: " + regServerPort + ". Waiting for peer requests ...");
          
          while (true) {
              
              Socket clientSocket = null;
              try {
                  clientSocket = serverSocket.accept();
                  
                  InputStream is = clientSocket.getInputStream();   
                  ObjectInputStream ois = new ObjectInputStream(is);   
                  MessageRequest msg = (MessageRequest)ois.readObject();
                  
                    if(msg instanceof RegisterRequest) {
                 
                        RegisterRequest reg = (RegisterRequest) msg;
                        System.out.println("Register request from peer with IP: " + reg.getIp() + " and RFC server port: " + reg.getRfcServerPort());
                        
                        Peer peer = findPeerByIpPort(reg.getIp(), reg.getRfcServerPort());
                 
                        if (peer == null) {
                            peer = new Peer(reg.getIp(), getUniqueCookie(), true, 7200, reg.getRfcServerPort(), 1, new Date());
                            register(peer);
                            RegisterResponse regResponse = new RegisterResponse(myIp, myOs, version, STATUS_OK, null, peer.getCookie());
                            sendResponseToPeer(clientSocket, regResponse);
                        } else {
 
                            /* If the object is already there in the peer list change the TTL value
                             * check  the number of active peer in last 30 days
                             * make the object active
                             * change date to current time
                             */
                            keepAlive(peer);
                            peer.setFlag(true);
                            peer.setNumActive(peer.getNumActive() + 1);
                            peer.setDate(new Date());
                            System.out.println("Peer data modified");
                            RegisterResponse regResponse = new RegisterResponse(myIp, myOs, version, STATUS_OK, null, peer.getCookie());
                            sendResponseToPeer(clientSocket, regResponse);
                        }
                    } else if (msg instanceof LeaveRequest){
                        
                        LeaveRequest leaveReq = (LeaveRequest)msg;
                        Peer peer = findPeerByCookie(leaveReq.getCookie());
                        MessageResponse leaveRsp;
                        if (peer != null) {
                            leave(peer);
                            leaveRsp = new MessageResponse(myIp, myOs, version, STATUS_OK, null);
                        } else {
                            leaveRsp = new MessageResponse(myIp, myOs, version, STATUS_ERROR, "Peer not registered with RS");
                        }
                        sendResponseToPeer(clientSocket, leaveRsp);
             
                    } else if (msg instanceof KeepAliveRequest) {
                        
                        KeepAliveRequest keepAliveReq = (KeepAliveRequest)msg;
                        Peer peer = findPeerByCookie(keepAliveReq.getCookie());
                        MessageResponse keepAliveRsp;
                        
                        if (peer != null) {
                            keepAlive(peer);
                            keepAliveRsp = new MessageResponse(myIp, myOs, version, STATUS_OK, null);
                        } else {
                            keepAliveRsp = new MessageResponse(myIp, myOs, version, STATUS_ERROR, "Peer not registered with RS");
                        }
                        sendResponseToPeer(clientSocket, keepAliveRsp);
             
                    } else if (msg instanceof PQueryRequest) {
                        
                        PQueryRequest pqueryReq = (PQueryRequest) msg;
                        Peer peer = findPeerByCookie(pqueryReq.getCookie());
                        PQueryResponse pqueryRsp;
                        if (peer != null) {
                            List<PeerInfo> peers = pquery();
                            pqueryRsp = new PQueryResponse(myIp, myOs, version, STATUS_OK, null, peers);
                        } else {
                            pqueryRsp = new PQueryResponse(myIp, myOs, version, STATUS_ERROR, "Peer not registered with RS", null);
                        }
                        sendResponseToPeer(clientSocket, pqueryRsp);
             
                    } else {
                        MessageResponse invalidMethodRsp = new MessageResponse(myIp, myOs, version, STATUS_ERROR, "Request type isn't supported by the registration server!");
                        sendResponseToPeer(clientSocket, invalidMethodRsp);
                    } 
         
                 } catch (IOException e) {
                  System.out.println("Accept failed: " + e.getMessage());
                  e.printStackTrace();
              } catch (Exception e){
                  System.out.println("Exception: "+e.getMessage());
                  e.printStackTrace();
              }
          }
     }
     
     private void startTTLDecrementThread(final long interval) {
         
         Thread t = new Thread() {
             public void run() {
                 while(true) {
                     try {
                         Thread.sleep(interval);
                         
                         for (Peer peer : peerList) {     
                               if (peer.getFlag() && peer.getTTL() > 60)
                                   peer.setTTL(peer.getTTL()-60);
                               else {
                                   peer.setTTL(0);
                                   peer.setFlag(false);
                               }            
                          }
                     } catch (InterruptedException ie) {
                         // TODO : Handle this exception
                     }
                 }
             }
          };
          
          t.start();
     }
     
     
     /* 
      * Check if the given peer has registered in the list or not 
      * return null if element not in the list
      * else return the peer object in the list
      */
     private Peer findPeerByIpPort(String ipAdd, int prt){
          
         for (Peer peer : peerList) {
              if (peer.getHostname().equals(ipAdd) && (peer.getPort() == prt)) {
                  return peer;
              }
          }
         return null;
     }
     
     private Peer findPeerByCookie(int cookie){
          
         for (Peer peer : peerList) {
              if (peer.getCookie() == cookie) {
                  return peer;
              }
          }
         return null;
     }
 
     
     /*
      * Add the peer to the linklist
      */
     private void register(Peer p){
         peerList.add(p);
     }
     
     /*
      * Set the peer as inactive
      */
     
     private void leave(Peer p){
         p.setFlag(false);

     }
     
     /*
      * Set the value of TTL to 7200
      * 
      */
     private void keepAlive(Peer p){
         p.setTTL(7200);
     }
     
     /*
      * send the list of active peers
      * done by iterating all the registered list of peers
     */
     private List<PeerInfo> pquery() {
         
          ArrayList <PeerInfo> list  = new ArrayList<PeerInfo>();
          for (Peer peer : peerList) {     
               if (peer.getFlag()) {
                   PeerInfo peerInfo = new PeerInfo(peer.getHostname(), peer.getPort());
                  list.add(peerInfo);
              }
          }
         return list ;
     }
     
     private int getUniqueCookie() {
         
         return ++cookieSeed;
     }
     
     private void sendResponseToPeer(Socket clientSocket, MessageResponse rsp) {
         
         OutputStream os;
         try {
             os = clientSocket.getOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(os);   
             oos.writeObject(rsp);
         } catch (IOException e) {
             System.out.println("RegistrationServer.sendResponseToPeer() - IOException with message: " + e.getMessage());
             e.printStackTrace();
         }
     }
 }
