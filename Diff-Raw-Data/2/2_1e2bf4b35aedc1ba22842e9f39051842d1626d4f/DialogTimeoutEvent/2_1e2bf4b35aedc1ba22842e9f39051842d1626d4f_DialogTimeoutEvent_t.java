 
 /**
  * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  * Unpublished - rights reserved under the Copyright Laws of the United States.
  * Copyright  2010 Oracle Inc., Inc. All rights reserved.
  *
  * Use is subject to license terms.
  *
  * This distribution may include materials developed by third parties. 
  *
  * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  *
  * Module Name   : JSIP Specification
 * File Name     : DialogTimeoutEvent
  * Author        : Jean Deruelle
  *
  *  HISTORY
  *  Version   Date         Author              Comments
  *  1.2     Sep 5, 2010  Jean Deruelle   Initial version  
  *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  */
 
 package javax.sip;
 
 import java.util.EventObject;
 
 
 /**
  * 
  * DialogTimeoutEvent is delivered to the Listener when the
  * dialog does not receive or send an ACK. 
  *
  * 
  * @author Oracle Inc., NIST
  * @version 2.0
  * @since 2.0
  *
  */
 public class DialogTimeoutEvent extends EventObject {
 	private static final long serialVersionUID = -2514000059989311925L;
 	public enum Reason {
         AckNotReceived,    // UAC ACK is not seen after OK is sent
         AckNotSent, 	   // UAS did not send ACK after receiving OK. 
         ReInviteTimeout,   // B2BUACould not send re-INVITE (blocked waiting for prior Transaction to complete). 
         EarlyStateTimeout  // Dialog stuck in early state.
 	};	
 	/**
      * Constructs a DialogTerminatedEvent to indicate a dialog
      * timeout.
      *
      * @param source - the source of TimeoutEvent. 
      * @param dialog - the dialog that timed out.
      */
      public DialogTimeoutEvent(Object source, Dialog dialog, Reason reason) {
          super(source);
          m_dialog = dialog;
          m_reason = reason;
       
     }
 
 	/**
      * Gets the Dialog associated with the event. This 
      * enables application developers to access the dialog associated to this 
      * event. 
      * 
      * @return the dialog associated with the response event or null if there is no dialog.
      */
     public Dialog getDialog() {
         return m_dialog;
     }    
     
     /**
      * The reason for the Dialog Timeout Event being delivered to the application.
      * 
      * @return the reason for the timeout event.
      */
     public Reason getReason() {
     	return m_reason;
     }
      
     // internal variables
     private Dialog m_dialog = null;    
     private Reason m_reason = null;
 }
 
