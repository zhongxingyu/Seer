 package org.valabs.tplParser;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
import java.util.TreeMap;
 
 
 /**     .
  * @author <a href="mailto:valeks@valabs.spb.ru">  .</a>
 * @version $Id: TplFile.java,v 1.3 2005/02/12 22:30:20 valeks Exp $
  * 
  *  :
  * 
  * NAME [] [ ] [ ODISP action] IMPORT [] (*)
  * 
  * AUTHOR [ (  (a)author)] (*)
  * 
  * DESC [ ] (*) FIELD [  (  )] [ ]
  * 
  * FCHECK [ ] [    boolean checkMessage()] (**)
  * 
  * FDESC [ ] [ ] (*) DEFORIGIN [ 
  * -]
  * 
  * DEFDEST [  -]
  *
  * DEFROUTABLE [Routable -]
  * 
  * DEFREPLTO [       ]
  * 
  * DEFOOB [OOB -]
  * 
  * VERBATIM (***)
  * 
  *    (a)version   CVS- Id.
  * 
  * (*)  multiline comments,   , 
  *     . : AUTHOR 1  AUTHOR 2
  * 
  * 
  * (**)  - get[ ](msg) != null
  * 
  * (***) VERBATIM       .
  *   VERBATIM.    ,  
  *      .
  */
 class TplFile {
   private String packageName;
 
   private String messageName;
 
   private String actionName;
 
   private List imports = new ArrayList();
 
   private List authors = new ArrayList();
 
   private List description = new ArrayList();
 
   private String defaultOrigin;
 
   private String defaultDestination;
 
   private boolean defaultRoutable = false;
 
   private boolean defaultOOB = false;
 
   private String defaultReplyTo;
 
   private List verbatim = new ArrayList();
 
   private String cvsId;
   
   private Map fields = new HashMap();
   
  private List fieldOrder = new ArrayList();
  
   private String fileName;
   
   private long lastModified;
   
   public static final int TYPE_PLAIN = 0;
   
   public static final int TYPE_REPLY = 1;
   
   public static final int TYPE_ERROR = 2;
   
   public static final int TYPE_NOTIFY = 3;
   
   private int type = TYPE_PLAIN;
   
   private TplFile errorMessage;
   
   private TplFile replyMessage;
   
   private TplFile notifyMessage;
 
   /**      .
    * @param toParse   
    * @throws IOException        
    */
   public TplFile(File toParse) throws IOException {
     fileName = toParse.getPath();
     lastModified = toParse.lastModified();
     BufferedReader tplReader = new BufferedReader(new FileReader(toParse));
     String line;
     while ((line = tplReader.readLine()) != null) {
       parseTagLine(line);
     }
     System.out.print("TPL parsed: " + fileName);
   }
   
   /**     . */
   public TplFile(TplFile parent, int _type) {
     packageName = new String(parent.packageName);
     messageName = new String(parent.messageName);
     actionName = new String(parent.actionName);
     imports.addAll(parent.imports);
     authors.addAll(parent.authors);
     description.addAll(parent.description);
     if (parent.defaultOrigin != null) {
       defaultOrigin = new String(parent.defaultOrigin);
     }
     if (parent.defaultOrigin != null) {
       defaultDestination = new String(parent.defaultDestination);
     }
     defaultRoutable = parent.defaultRoutable;
     defaultOOB = parent.defaultOOB;
     // verbatim = new ArrayList(parent.verbatim); //  
     cvsId = new String(parent.cvsId);
     fields.putAll(parent.fields);
     fileName = new String(parent.fileName);
     lastModified = parent.lastModified;
     type = _type;
     switch (type) {
       case TYPE_REPLY:
         messageName = messageName.replaceFirst("Message", "ReplyMessage");
         actionName = actionName + "_reply";
         fileName = fileName.replaceFirst("Message", "ReplyMessage");
         break;
       case TYPE_ERROR:
         messageName = messageName.replaceFirst("Message", "ErrorMessage");
         actionName = actionName + "_error";
         fileName = fileName.replaceFirst("Message", "ErrorMessage");
         break;
       case TYPE_NOTIFY:
         messageName = messageName.replaceFirst("Message", "NotifyMessage");
         actionName = actionName + "_notify";
         fileName = fileName.replaceFirst("Message", "NotifyMessage");
         break;
     }
   }
   
   public int getType() {
     return type;
   }
   /**   .tpl 
    * @param tagLine  .tpl 
    */
   private void parseTagLine(String tagLine) {
     if (tagLine.startsWith("NAME")) {
       String[] s = tagLine.split(" ");
       packageName = s[1];
       messageName = s[2];
       actionName = s[3];
     } else
 
     if (tagLine.startsWith("$")) {
       cvsId = tagLine;
     } else if (tagLine.startsWith("IMPORT")) {
       imports.add(tagLine.split(" ")[1]);
     } else if (tagLine.startsWith("AUTHOR")) {
       authors.add(tagLine.substring(7));
     } else if (tagLine.startsWith("DESC")) {
       description.add(tagLine.substring(5));
     } else if (tagLine.startsWith("FIELD")) {
       String[] tokens = tagLine.split(" ");
       getFieldRecordByName(tokens[1]).setType(new String(tokens[2]));
     } else if (tagLine.startsWith("FCHECK")) {
       String[] tokens = tagLine.split(" ");
       getFieldRecordByName(tokens[1]).setCheck(tagLine.substring(8 + tokens[1].length()));
     } else if (tagLine.startsWith("FDESC")) {
       String[] tokens = tagLine.split(" ");
       getFieldRecordByName(tokens[1]).setDesc(tagLine.substring(7 + tokens[1].length()));
     } else if (tagLine.startsWith("DEFORIGIN")) {
       defaultOrigin = tagLine.split(" ")[1];
     } else if (tagLine.startsWith("DEFDEST")) {
       defaultDestination = tagLine.split(" ")[1];
     } else if (tagLine.startsWith("DEFROUTABLE")) {
       defaultRoutable = true;
     } else if (tagLine.startsWith("DEFREPLTO")) {
       defaultReplyTo = tagLine.split(" ")[1];
     } else if (tagLine.startsWith("DEFOOB")) {
       defaultOOB = true;
     } else if (tagLine.startsWith("ERROR")) { 
       _getErrorMessage().parseTagLine(new String(tagLine.substring(5).trim()));
     } else if (tagLine.startsWith("REPLY")) { 
       _getReplyMessage().parseTagLine(new String(tagLine.substring(5).trim()));
     } else if (tagLine.startsWith("NOTIFY")) { 
       _getNotifyMessage().parseTagLine(new String(tagLine.substring(6).trim()));
     } else if (!tagLine.startsWith("#")) {
       // 
       //TODO may be it's good to skip empty lines
       if (tagLine != "") {
         verbatim.add(tagLine);
       }
     }
   }
   
   public TplFile getReplyMessage() {
     return replyMessage;
   }
   
   public TplFile getErrorMessage() {
     return errorMessage;
   }
 
   public TplFile getNotifyMessage() {
     return notifyMessage;
   }
 
   private TplFile _getReplyMessage() {
     if (replyMessage == null) {
       replyMessage = new TplFile(this, TYPE_REPLY);
     }
     return replyMessage;
   }
   
   private TplFile _getErrorMessage() {
     if (errorMessage == null) {
       errorMessage = new TplFile(this, TYPE_ERROR);
     }
     return errorMessage;
   }
 
   private TplFile _getNotifyMessage() {
     if (notifyMessage == null) {
       notifyMessage = new TplFile(this, TYPE_NOTIFY);
     }
     return notifyMessage;
   }
 
   
   /**      .
    * @param name  
    */
   private FieldRecord getFieldRecordByName(String name) {
     if (!fields.containsKey(name)) {
      fieldOrder.add(name);
       fields.put(name, new FieldRecord(name));
     }
     return (FieldRecord) fields.get(name);
   }
 
   public String getActionName() {
     return actionName;
   }
   public List getAuthors() {
     return authors;
   }
   public String getCvsId() {
     return cvsId;
   }
   public String getDefaultDestination() {
     return defaultDestination;
   }
   public boolean isDefaultOOB() {
     return defaultOOB;
   }
   public String getDefaultOrigin() {
     return defaultOrigin;
   }
   public String getDefaultReplyTo() {
     return defaultReplyTo;
   }
   public boolean isDefaultRoutable() {
     return defaultRoutable;
   }
   public List getDescription() {
     return description;
   }
   public Map getFields() {
    Map result = new TreeMap(new Comparator() {
      public int compare(Object arg0, Object arg1) {
        if (!fieldOrder.contains(arg0) || !fieldOrder.contains(arg1)) {
          return 0;
        } else {
          if (fieldOrder.indexOf(arg0) < fieldOrder.indexOf(arg1)) {
            return -1;
          } else if (arg0.equals(arg1)) {
            return 0;
          } else {
            return 1;
          }
        }
      }
      });
    result.putAll(fields);
    return result;
   }
   public List getImports() {
     return imports;
   }
   public String getMessageName() {
     return messageName;
   }
   public String getPackageName() {
     return packageName;
   }
   public List getVerbatim() {
     return verbatim;
   }
   public String getFileName() {
     return fileName;
   }
   public long lastModified() {
     return lastModified;
   }
 }
