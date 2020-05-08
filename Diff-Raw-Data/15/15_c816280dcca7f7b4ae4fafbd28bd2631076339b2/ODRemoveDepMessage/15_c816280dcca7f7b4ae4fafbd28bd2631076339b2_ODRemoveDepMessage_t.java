 package com.novel.stdmsg;
 
 /**     ODISP  
  *  .
  *      .
  *       .
  * @author <a href="mailto:valeks@novel-il.ru">Valentin A. Alekseev</a>
  * @author (C) 2003,  "-"
 * @version $Id: ODRemoveDepMessage.java,v 1.6 2004/02/15 20:58:57 valeks Exp $
  */
 
 public class ODRemoveDepMessage extends StandartMessage {
   /**   . */
   public static final String NAME = "od_remove_dep";
   /**  . */
   private transient String depName = "";
   /**  . */
   private static final int DEPNAME_IDX = 0;
   /**      .
    * @param origin 
    * @param replyTo      
    */
   public ODRemoveDepMessage(final String origin, final int replyTo) {
    super("od_remove_dep", "stddispatcher", origin, replyTo);
   }
 
   /**    .
    * @return  
    */
   public final String getDepName() {
     if (isCE()) {
       return (String) getField(DEPNAME_IDX);
     }
     return depName;
   }
 
   /**    .
    * @param newDepName   
    * @return   
    */
   public final ODRemoveDepMessage setDepName(final String newDepName) {
     depName = newDepName;
     return this;
   }
 
   /**   .
    * @return  
    */
   public final boolean isCorrect() {
     if (isCE()) {
       return true;
     }
     if (depName != "") {
       getFields().clear();
       addField(depName);
       setCE(true);
     }
     return isCE();
   }
 } // ODShutdownMessage
