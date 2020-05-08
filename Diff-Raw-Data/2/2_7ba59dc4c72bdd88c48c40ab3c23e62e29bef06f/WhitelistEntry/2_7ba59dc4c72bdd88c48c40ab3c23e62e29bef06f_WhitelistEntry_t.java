 package com.mollom.client;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  * Mollom automatically blocks unwanted content and learns from all participating sites to improve its filters.
 * On top of automatic filtering, you can define a custom whitelist.
  * Whitelist entries are checked first. On a positive whitelist match, no other checks are performed.
  *    Content API's spam check returns ham.
  *    Content API's profanity check returns non-profane.
  *    Blacklist entries are not checked.
  */
 @XmlRootElement(name = "entry")
 @XmlAccessorType(XmlAccessType.FIELD)
 public class WhitelistEntry {
 
   private String id;
   private int created;
   private int status;
   private int lastMatch;
   private long matchCount;
   private String value;
   private String context;
   private String note;
 
   public WhitelistEntry() {
     context = Context.AUTHORID.toString();
     status = 1;
   }
 
   /**
    * @return Unique whitelist entry ID assigned by Mollom.
    */
   public String getId() {
     return id;
   }
 
   /**
    * @return Unix timestamp (seconds) of when the blacklist entry was created.
    */
   public int getCreated() {
     return created;
   }
 
   /**
    * @return Unix timestamp (seconds) of when the last time this blacklist entry matched.
    */
   public int getLastMatch() {
     return lastMatch;
   }
 
   /**
    * @return Number of times this blacklist entry has matched content
    */
   public long getMatchCount() {
     return matchCount;
   }
 
   /**
    * @return The string/value to blacklist.
    */
   public String getValue() {
     return value;
   }
 
   /**
    * @return Where the entry's value may match.
    */
   public Context getContext() {
     return Context.valueOf(context.toUpperCase());
   }
 
   /**
    * @return A custom string explaining the entry. Useful in a multi-moderator scenario.
    */
   public String getNote() {
     return note;
   }
 
   void setId(String id) {
     this.id = id;
   }
 
   void setCreated(int created) {
     this.created = created;
   }
 
   void setStatus(int status) {
     this.status = status;
   }
 
   void setLastMatch(int lastMatch) {
     this.lastMatch = lastMatch;
   }
 
   void setMatchCount(long matchCount) {
     this.matchCount = matchCount;
   }
 
   /**
    * @param value
    *          The string/value to blacklist.
    */
   public void setValue(String value) {
     this.value = value;
   }
 
   /**
    * @oaram context Where the entry's value may match.
    */
   public void setContext(Context context) {
     this.context = context.toString();
   }
 
   /**
    * @param note
    *          A custom string explaining the entry. Useful in a multi-moderator scenario.
    */
   public void setNote(String note) {
     this.note = note;
   }
 
   public boolean isEnabled() {
     return status == 1;
   }
 
   public void disable() {
     status = 0;
   }
 
   public void enable() {
     status = 1;
   }
 }
