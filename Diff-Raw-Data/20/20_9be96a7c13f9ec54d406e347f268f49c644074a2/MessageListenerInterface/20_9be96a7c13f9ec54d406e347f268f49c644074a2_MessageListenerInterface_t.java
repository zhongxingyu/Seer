 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fi.wsnusbcollect.nodeCom;
 
 import fi.wsnusbcollect.nodes.ConnectedNode;
 import java.util.Properties;
 import net.tinyos.message.Message;
 import net.tinyos.message.MessageListener;
 
 /**
  *
  * @author ph4r05
  */
 public interface MessageListenerInterface extends net.tinyos.message.MessageListener{
 
     /**
      * =========================================================================
      *
      * GETTERS + SETTERS
      *
      * =========================================================================
      */
 
     /**
      * Disconnects connected node to this component
      */
     void disconnectNode(ConnectedNode nh, boolean resetQueues);
     void disconnectNode(ConnectedNode nh, Properties props);
     
     /**
      * Connect given node to this sender, if already in, nothing is done if
      * MoteIF equals already set one
      * 
      * @param nh
      * @param props 
      */
     void connectNode(ConnectedNode nh, Properties props);
     
     
     boolean isDropingPackets();
 
     boolean isShutdown();
 
     /**
      * unregister message listener
      */
    void deregisterListener(Message msg, fi.wsnusbcollect.nodeCom.MessageListener listener);
     
     /**
      * Unregister message listener for particular node 
      * @param node
      * @param msg
      * @param listener 
      */
    void deregisterListener(int node, Message msg, fi.wsnusbcollect.nodeCom.MessageListener listener);
     
     /**
      * Register message listener
      * If same listener (.equals()) is registered to same message type, request
      * is ignored.
      * 
      * Registers listener to ALL nodes that are listener listening to
      */
     void registerListener(Message msg, fi.wsnusbcollect.nodeCom.MessageListener listener);
     
     /**
      * Register message listener
      * If same listener (.equals()) is registered to same message type, request
      * is ignored.
      * 
      * Registers only to particular node
      * 
      * @param node
      * @param msg
      * @param listener 
      */
     void registerListener(int node, Message msg, fi.wsnusbcollect.nodeCom.MessageListener listener);
 
     /**
      * After gateway change is needed to register as listener for all registered
      * AMtypes. MyMessageListener is registered as listener to tinyos listener.
      */
     void reregisterListeners();
 
     /**
      * perform hard reset to this object = clears entire memory
      */
     void reset();
 
     /**
      * The thread either executes tasks or sleep.
      */
     void run();
 
     void setDropingPackets(boolean dropingPackets);
 
     void setShutdown(boolean shutdown);
 
     int getQueueLength();
     
     /**
      * Performs shutdown
      */
     void shutdown();
     
     void start();
     
     boolean isAlive();
 }
