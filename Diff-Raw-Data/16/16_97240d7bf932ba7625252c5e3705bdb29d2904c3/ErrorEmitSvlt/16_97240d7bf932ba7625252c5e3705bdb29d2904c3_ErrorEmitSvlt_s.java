 /* **********************************************************************
     Copyright 2006 Rensselaer Polytechnic Institute. All worldwide rights reserved.
 
     Redistribution and use of this distribution in source and binary forms,
     with or without modification, are permitted provided that:
        The above copyright notice and this permission notice appear in all
         copies and supporting documentation;
 
         The name, identifiers, and trademarks of Rensselaer Polytechnic
         Institute are not used in advertising or publicity without the
         express prior written permission of Rensselaer Polytechnic Institute;
 
     DISCLAIMER: The software is distributed" AS IS" without any express or
     implied warranty, including but not limited to, any implied warranties
     of merchantability or fitness for a particular purpose or any warrant)'
     of non-infringement of any current or pending patent rights. The authors
     of the software make no representations about the suitability of this
     software for any particular purpose. The entire risk as to the quality
     and performance of the software is with the user. Should the software
     prove defective, the user assumes the cost of all necessary servicing,
     repair or correction. In particular, neither Rensselaer Polytechnic
     Institute, nor the authors of the software are liable for any indirect,
     special, consequential, or incidental damages related to the software,
     to the maximum extent the law permits.
 */
 
 package edu.rpi.sss.util.jsp;
 
 import edu.rpi.sss.util.log.MessageEmit;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionMessage;
 import org.apache.struts.util.MessageResources;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 /** This class allows error message generation in the servlet world.
  *
  * @author Mike Douglass douglm@rpi.edu
  * @version 1.0
  */
 public class ErrorEmitSvlt implements MessageEmit {
   protected boolean debug;
 
   transient protected String id;
   transient protected Object caller;
   transient protected MessageResources messages;
   transient protected ActionErrors errors;
   transient protected String exceptionPname;
 
   /** We save the message property and the parameters in the following
    * class which we can return as an alternative to the struts message
    * generation.
    */
   public class Msg implements Serializable {
    private ArrayList params = new ArrayList();
     private Object p1;
     private Object p2;
     private Object p3;
 
     private String msgId;
     protected MessageResources messages;
 
     /**
      * @param messages
      * @param msgId
      */
     public Msg(MessageResources messages,
                String msgId) {
       this.messages = messages;
       this.msgId = msgId;
     }
 
     /**
      * @param messages
      * @param msgId
      * @param o
      */
     public Msg(MessageResources messages,
                String msgId, Object o) {
       this.messages = messages;
       this.msgId = msgId;
       addParam(o);
       p1 = o;
     }
 
     /**
      * @param messages
      * @param msgId
      * @param o1
      * @param o2
      */
     public Msg(MessageResources messages,
                String msgId, Object o1, Object o2) {
       this.messages = messages;
       this.msgId = msgId;
       addParam(o1);
       addParam(o2);
       p1 = o1;
       p1 = o2;
     }
 
     /**
      * @param messages
      * @param msgId
      * @param o1
      * @param o2
      * @param o3
      */
     public Msg(MessageResources messages,
                String msgId, Object o1, Object o2, Object o3) {
       this.messages = messages;
       this.msgId = msgId;
       addParam(o1);
       addParam(o2);
       addParam(o3);
       p1 = o1;
       p1 = o2;
       p1 = o3;
     }
 
     /**
      * @return Strign message id
      */
     public String getMsgId() {
       return msgId;
     }
 
     /**
      * @return params
      */
     public ArrayList getParams() {
       return params;
     }
 
     /**
      * @return expanded message
      */
     public String getMsg() {
       if (messages == null) {
         return "";
       }
 
       return messages.getMessage(msgId, p1, p2, p3);
     }
 
     private void addParam(Object o) {
       if (o != null) {
         params.add(o);
       }
     }
   }
 
  protected ArrayList msgList = new ArrayList();
 
   /**
    *
    */
   public ErrorEmitSvlt() {
     this(false);
   }
 
   /**
    * @param debug
    */
   public ErrorEmitSvlt(boolean debug) {
     this.debug = debug;
   }
 
   /** Generation of errors in the servlet world means adding them to the
    *  errors object. We need to call this routine on every entry to the
    *  application
    *
    * @param id       An identifying name
    * @param caller   Used for log4j identification
    * @param messages Resources
    * @param errors   Error message will be appended on failure.
    * @param exceptionPname Property name for exceptions
    * @param clear
    */
   public void reinit(String id,
                      Object caller,
                      MessageResources messages,
                      ActionErrors errors,
                      String exceptionPname,
                      boolean clear) {
     this.id = id;
     this.caller = caller;
     this.messages = messages;
     this.errors = errors;
     this.exceptionPname = exceptionPname;
 
     if (clear) {
       msgList.clear();
     }
   }
 
   /**
    * @return msg list
    */
  public ArrayList getMsgList() {
     return msgList;
   }
 
   public void emit(String pname) {
     if (debug) {
       debugMsg(pname, null, null);
     }
 
     msgList.add(new Msg(messages, pname));
 
     if ((messages == null) || !haveOutputObject()) {
       return;
     }
 
     try {
       errors.add(id, new ActionMessage(pname));
     } catch (Throwable t) {
       logError(className() + ": exception adding Action message", t);
     }
   }
 
   /* (non-Javadoc)
    * @see edu.rpi.sss.util.log.MessageEmit#emit(java.lang.String, int)
    */
   public void emit(String pname, int num) {
     if (debug) {
       debugMsg(pname, "int", String.valueOf(num));
     }
 
     emit(pname, new Integer(num));
   }
 
   /* (non-Javadoc)
    * @see edu.rpi.sss.util.log.MessageEmit#setExceptionPname(java.lang.String)
    */
   public void setExceptionPname(String pname) {
     exceptionPname = pname;
   }
 
   /* (non-Javadoc)
    * @see edu.rpi.sss.util.log.MessageEmit#emit(java.lang.Throwable)
    */
   public void emit(Throwable t) {
     if (debug) {
       debugMsg(exceptionPname, "Throwable", String.valueOf(t.getMessage()));
     }
 
     String msg = t.getMessage();
     if (msg == null) {
       msg = "<No-message>";
     }
 
     Logger.getLogger(caller.getClass()).error(msg, t);
 
     emit(exceptionPname, t.getMessage());
   }
 
   public void emit(String pname, Object o){
     if (debug) {
       if (o == null) {
         debugMsg(pname, "null object", String.valueOf(o));
       } else {
         debugMsg(pname, o.getClass().getName(), String.valueOf(o));
       }
     }
 
     if (o == null) {
       msgList.add(new Msg(messages, pname));
     } else {
       msgList.add(new Msg(messages, pname, o));
     }
 
     if ((messages == null) || !haveOutputObject()) {
       return;
     }
 
     try {
       errors.add(id, new ActionMessage(pname, o));
     } catch (Throwable t) {
       logError(className() + ": exception adding Action error", t);
     }
   }
 
   public void emit(String pname, Object o1, Object o2){
     if (debug) {
       debugMsg(pname, "2objects",
                String.valueOf(o1) + "; " +
                String.valueOf(o2));
     }
 
     msgList.add(new Msg(messages, pname, o1, o2));
 
     if ((messages == null) || !haveOutputObject()) {
       return;
     }
 
     try {
       errors.add(id, new ActionMessage(pname, o1, o2));
     } catch (Throwable t) {
       logError(className() + ": exception adding Action error", t);
     }
   }
 
   public void emit(String pname, Object o1, Object o2, Object o3){
     if (debug) {
       debugMsg(pname, "2objects",
                String.valueOf(o1) + "; " +
                String.valueOf(o2) + "; " +
                String.valueOf(o3));
     }
 
     msgList.add(new Msg(messages, pname, o1, o2, o3));
 
     if ((messages == null) || !haveOutputObject()) {
       return;
     }
 
     try {
       errors.add(id, new ActionMessage(pname, o1, o2, o3));
     } catch (Throwable t) {
       logError(className() + ":exception adding Action error" + pname, t);
     }
   }
 
   /** Indicate no messages emitted. Null in this implementation.
    */
   public void clear() {}
 
   /** @return true if any messages emitted
    */
   public boolean messagesEmitted() {
     return !msgList.isEmpty();
   }
 
   /**
    * @return errors
    */
   public ActionErrors getErrors() {
     return errors;
   }
 
   /** Debugging
    *
    * @param msg
    */
   public void debugOut(String msg) {
     try {
       Logger log = Logger.getLogger(caller.getClass());
 
       if (log.isDebugEnabled()) {
         log.debug(msg);
       }
     } catch (Throwable t) {
       System.out.println(msg);
     }
   }
 
   protected boolean haveOutputObject() {
     return errors != null;
   }
 
   protected String className() {
     return "ErrorEmitSvlt";
   }
 
   protected void debugMsg(String pname, String ptype, String pval) {
     debugOut("Emitted: property=" + pname +
              " ptype=" + ptype +
              " val(s)=" + pval);
   }
 
   protected void logError(String msg, Throwable t) {
     try {
       Logger log = Logger.getLogger(caller.getClass());
 
       log.error(msg, t);
     } catch (Throwable t1) {
       System.out.println(caller.getClass().getName() +
                          "Error: " + msg);
       t.printStackTrace();
     }
   }
 }
 
