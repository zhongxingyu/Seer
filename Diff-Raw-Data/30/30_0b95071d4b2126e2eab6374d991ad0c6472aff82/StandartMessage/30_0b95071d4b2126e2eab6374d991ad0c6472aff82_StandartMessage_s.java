 package com.novel.stdmsg;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import com.novel.odisp.common.Message;
 
 /**       ODISP.
  * @author  . 
  * @author (C) 2003,  "-"
 * @version $Id: StandartMessage.java,v 1.13 2004/03/31 13:15:22 dron Exp $
  */
 public class StandartMessage implements Message, Serializable {
   /**  . */
   private boolean routable = true;
   /**    . */
   private static int id = 0;
   /**     . */
   private int myId = -1;
   /**   . */
   private Map fields = new HashMap();
   /** . */
   private String action = null;
   /**  . */
   private String destination = null;
   /** . */
   private String origin = null;
   /**      . */
   private int inReplyTo = -1;
   /**    . */
   private int lastIdx = 0;
   /**   . */
   private boolean ce = false;
   /**   .
    * @param newAction    
    * @param newDestination  
    * @param newOrigin  
    * @param newInReplyTo      
    */
   public StandartMessage(final String newAction,
 			 final String newDestination,
 			 final String newOrigin,
 			 final int newInReplyTo) {
     action = newAction;
     destination = newDestination;
     inReplyTo = newInReplyTo;
     origin = newOrigin;
     myId = id++;
   }
 
   /**  -. */
   public StandartMessage() {
     myId = id++;
   }
 
  /**       .
   * @param msg     
   */
  public StandartMessage(final Message msg) {
     action = msg.getAction();
     destination = msg.getDestination();
     inReplyTo = msg.getReplyTo();
     origin = msg.getOrigin();
     fields = new HashMap(msg.getContents());
     routable = msg.isRoutable();
    myId = id++;
     setCE(msg.isCorrect());
   }
 
   /**      .
    * @param field     
    * @deprecated  addField(String, Object)
    */
   protected final void addField(final Object field) {
     addField((new Integer(lastIdx++)).toString(), field);
   }
 
   /**        .
    * @param field  
    * @return  
    * @deprecated  getField(String)
    */
   protected final Object getField(final int field) {
     return getField((new Integer(field)).toString());
   }
 
   /**     .
    * @return 
    */
   public final String getAction() {
     return action;
   }
 
   /**     .
    * @param newAction 
    */
   public final void setAction(final String newAction) {
     action = newAction;
   }
 
   /**   .
    * @return 
    */
   public final String getDestination() { return destination; }
 
   /**   .
    * @param newDest 
    */
   public final void setDestination(final String newDest) {
     destination = newDest;
   }
 
   /**   .
    * @return 
    */
   public final String getOrigin() {
     return origin;
   }
 
   /**    .
    * @param newOrigin    
    */
   public final void setOrigin(final String newOrigin) {
     this.origin = newOrigin;
   }
 
   /**       .
    * @return 
    */
   public final int getReplyTo() {
     return inReplyTo;
   }
 
   /**       .
    * @param newId 
    */
   public final void setReplyTo(final int newId) {
     inReplyTo = newId;
   }
 
   /**     .
    * @return - 
    */
   public final int getFieldsCount() {
     return fields.size();
   }
 
   /**    .
    * @return 
    */
   public final int getId() {
     return myId;
   }
 
   /**    .
    * @param newId 
    */
   public final void setId(final int newId) {
     myId = newId;
   }
 
   /**      .
    * @return   
    */
   public final String toString() {
     return "stdmessage id=" + myId + " replyto=" + inReplyTo
       + " action=" + action + ", destination=" + destination
       + ", origin=" + origin + ", fields.size()=" + fields.size() + (isRoutable() ? " routable" :"");
   }
 
   /**           .
    * @return   
    */
   public final String toString(boolean doStackTrace) {
     if (!doStackTrace) {
       return toString();
     }
     //    ,      
     StackTraceElement[] stea = {};
     try {
       throw new Exception();
     } catch (Exception e) {
       stea = e.getStackTrace();
     }
     String stackTraceMessage = "\nStack trace:";
     for(int i = 1; i < 10 && i < stea.length; i++) {
       stackTraceMessage += stea[i] + "\n";
     }
     String fieldDump = "\nField dump:";
     Iterator it = fields.keySet().iterator();
     while (it.hasNext()) {
       String key = (String) it.next();
       fieldDump += "\n(" + key + ") " + getField(key);
     }
     return "stdmessage id=" + myId + " replyto=" + inReplyTo
       + " action=" + action + ", destination=" + destination
       + ", origin=" + origin + ", fields.size()=" + fields.size()
       + stackTraceMessage + fieldDump;
   }
 
   /**   .
    * @return  
    */
   public boolean isCorrect() {
     ce = true;
     return true;
   }
 
   /**     .
    * @return  
    * @deprecated  getContents()
    */
   protected final List getFields() {
     return new ArrayList(fields.values());
   }
 
   public final Map getContents() {
     return fields;
   }
 
   /**   .
    * @param newCE  
    */
   public final void setCE(final boolean newCE) {
     ce = newCE;
   }
 
   /**   .
    * @return  
    */
   public final boolean isCE() {
     return ce;
   }
   public boolean isRoutable() {
     return routable;
   }
   public void setRoutable(final boolean newRoutable) {
     routable = newRoutable;
   }
 
   public void addField(final String name, final Object value) {
     if (fields.containsKey(name)) {
       fields.remove(name);
     }
     fields.put(name, value);
   }
 
   public Object getField(final String name) {
     return fields.get(name);
   }
 }
