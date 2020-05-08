 package xtremweb.serv.dt;
 
 import xtremweb.core.obj.dc.Data;
 import xtremweb.core.obj.dc.Locator;
 import xtremweb.core.obj.dr.Protocol;
 import xtremweb.core.obj.dt.Transfer;
 
 
 /**
  *  <code>OOBTransfer</code> is an interface which defines the methods
  *  which are necessary to implement when one wants to write a method
  *
  * @author <a href="mailto:fedak@lri.fr">Gilles Fedak</a>
  * @version 1.0
  */
 public interface OOBTransfer {
 
     /**
      * <code>connect</code>
      *
      * @exception OOBException if an error occurs
      */
     public void connect   () throws OOBException; 
 
     /**
      * <code>sendSenderSide</code> is called by the Sender whenever a
      * Send transfer is performed
      *
      * @exception OOBException if an error occurs
      */
     public void sendSenderSide      () throws OOBException;
 
     /**
      * <code>sendReceiverSide</code> is called by the Receiver whenever a
      * Send transfer is performed
      *
      * @exception OOBException if an error occurs
      */
     public void sendReceiverSide    () throws OOBException;
 
     /**
      * <code>receiveSenderSide</code> is called by the Sender whenever a
      * Receive transfer is performed
      *
      * @exception OOBException if an error occurs
      */
     public void receiveSenderSide   () throws OOBException;
 
     /**
      * <code>receiveReceiverSide</code> is called by the Receiver whenever a
      * Receive transfer is performed
      *
      * @exception OOBException if an error occurs
      */
     public void receiveReceiverSide () throws OOBException; 
  
     /**
      * Describe <code>disconnect</code> method here.
      *
      * @exception OOBException if an error occurs
      */
     public void disconnect() throws OOBException;
 
     /**
      * Describe <code>persist</code> method here.
      *
      */
     public void persist() ;
 
     /**
      * Describe <code>poolTransfer</code> method here.
      *
      * @return a <code>boolean</code> value
      */
     public boolean poolTransfer();
 
     /**
      * Describe <code>getData</code> method here.
      *
      * @return a <code>Data</code> value
      */
     public Data getData();
 
     /**
      * Describe <code>getTransfer</code> method here.
      *
      * @return a <code>Transfer</code> value
      */
     public Transfer getTransfer();
 
     /**
      * Describe <code>getLocalProtocol</code> method here.
      *
      * @return a <code>Protocol</code> value
      */
     public Protocol getLocalProtocol();
 
     /**
      * Describe <code>getRemoteProtocol</code> method here.
      *
      * @return a <code>Protocol</code> value
      */
     public Protocol getRemoteProtocol();
 
     /**
      * Describe <code>getLocalLocator</code> method here.
      *
      * @return a <code>Locator</code> value
      */
     public Locator getLocalLocator();
 
     /**
      * Describe <code>getRemoteLocator</code> method here.
      *
      * @return a <code>Locator</code> value
      */
     public Locator getRemoteLocator();
    
    /**
     * Is this transfer currently active?
     * @return
     */
     public boolean isTransfering();
    
    /**
     * Waits this transfer to be finished
     */
     public void waitFor();
    
    /**
     * Reports if the tranfer has erros
     * @return
     */
     public boolean error();
    
    /**
     * Mark this trasfer with an error
     */
    public void setError();
 } // ftpreceiver
