 package com.objet.chat_rmi;
 
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 
 /**
  * Manage the receiving and displaying of the messages
  */
 public class UpdateThread extends Thread
 {
     /**
      * Thread active or not, must not be accessed directly
      * 
      * @see enable, disable, isEnabled
      */
     private boolean m_enabled;
 
     /**
      * The client to be updated
      */
     private Client m_client;
 
     /**
      * Constructor: the thread's default status is inactive
      * 
      * @param client
      */
     public UpdateThread(Client client)
     {
         m_client = client;
         m_enabled = false;
         start();
     }
 
     @Override
     public void run()
     {
         while (true)
         {
             try
             {
                 sleep(1000);
             }
             catch (InterruptedException e)
             {
 
             }
             
             synchronized (this)
             {
                 if (m_enabled)
                 {
                     try
                     {
                         ArrayList<Message> listMsg = m_client.receive();

                         for (Message msg : listMsg)
                         {
                             System.out.println(msg.getUser().getPseudo() + " :");
                             System.out.println(msg.getData());
                         }
                     }
                     catch (RemoteException e)
                     {
 
                     }
                 }
             }
         }
     }
 
     /**
      * Sets the thread's status to 'enable'
      */
     public synchronized void enable()
     {
         m_enabled = true;
     }
 
     /**
      * Sets the thread's status to 'disable'
      */
     public synchronized void disable()
     {
         m_enabled = false;
     }
 }
