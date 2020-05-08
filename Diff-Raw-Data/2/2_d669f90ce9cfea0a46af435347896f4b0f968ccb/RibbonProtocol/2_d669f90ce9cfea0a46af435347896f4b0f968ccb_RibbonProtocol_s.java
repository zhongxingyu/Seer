 /*
  * This code is distributed under terms of GNU GPLv2.
  * *See LICENSE file.
  * ©UKRINFORM 2011-2012
  */
 
 package ribbonserver;
 
 /**
  * Ribbon protocol server side class
  * @author Stanislav Nepochatov
  */
 public class RibbonProtocol {
     
     private String LOG_ID = "Мережевий протокол \"Стрічка\"";
     
     /**
      * Tail of protocol result which should be delivered to all peers;
      */
     public String BROADCAST_TAIL;
     
     /**
      * Type of broadcasting;
      */
     public CONNECTION_TYPES BROADCAST_TYPE;
     
     RibbonProtocol(SessionManager.SessionThread upperThread) {
         InitProtocol();
         CURR_SESSION = upperThread;
     }
     
     /**
      * Link to upper level thread
      */
     private SessionManager.SessionThread CURR_SESSION;
     
     /**
      * Protocol revision digit;
      */
     private Integer INT_VERSION = 2;
     
     /**
      * String protocol revision version;
      */
     private String STR_VERSION = RibbonServer.RIBBON_VER;
     
     /**
      * Connection type enumeration
      */
     public enum CONNECTION_TYPES {
         NULL,
         CLIENT,
         CONTROL,
         ANY
     };
     
     /**
      * Current type of connection
      */
     public CONNECTION_TYPES CURR_TYPE = CONNECTION_TYPES.NULL;
     
     /**
      * ArrayList of commands objects
      */
     private java.util.ArrayList<CommandLet> RIBBON_COMMANDS = new java.util.ArrayList<CommandLet>();
     
     /**
      * Command template class
      */
     private class CommandLet {
         
         CommandLet(String givenName, CONNECTION_TYPES givenType) {
             this.COMMAND_NAME = givenName;
             this.COMM_TYPE = givenType;
         }
         
         public String COMMAND_NAME;
         
         public CONNECTION_TYPES COMM_TYPE;
         
         public String exec(String args) {return "";};
         
     }
     
     /**
      * Init protocol and load commands
      */
     private void InitProtocol() {
         
         /** CONNECTION CONTROL COMMANDS [LEVEL_0 SUPPORT] **/
         
         /**
          * RIBBON_NCTL_INIT: commandlet
          * Client and others application send this command to register
          * this connection.
          */
         this.RIBBON_COMMANDS.add(new CommandLet("RIBBON_NCTL_INIT", CONNECTION_TYPES.NULL) {
             @Override
             public String exec(String args) {
                 String[] parsedArgs = args.split(",");
                 if (CURR_TYPE == CONNECTION_TYPES.NULL) {
                     if (parsedArgs[1].equals(STR_VERSION)) {
                         try {
                             CURR_TYPE = CONNECTION_TYPES.valueOf(parsedArgs[0]);
                             if (!parsedArgs[2].equals(System.getProperty("file.encoding"))) {
                                 RibbonServer.logAppend(LOG_ID, 2, "мережева сесія вимогає іншої кодової сторінки:" + parsedArgs[2]);
                                 CURR_SESSION.setReaderEncoding(parsedArgs[2]);
                             }
                             return "OK:";
                         } catch (IllegalArgumentException ex) {
                             return "RIBBON_ERROR:Невідомий тип з'єднання!";
                         }
                     } else {
                         return "RIBBON_ERROR:Невідомий ідентефікатор протокола.";
                     }
                 } else {
                     return "RIBBON_WARNING:З'єднання вже ініціьовано!";
                 }
             }
         });
         
         /**
          * RIBBON_NCTL_LOGIN: commandlet
          * Client and other applications send this command to login user.
          */
         this.RIBBON_COMMANDS.add(new CommandLet("RIBBON_NCTL_LOGIN", CONNECTION_TYPES.ANY) {
           @Override
           public String exec(String args) {
               String[] parsedArgs = Generic.CsvFormat.commonParseLine(args, 2);
               if (!RibbonServer.ACCESS_ALLOW_MULTIPLIE_LOGIN && SessionManager.isAlreadyLogined(parsedArgs[0])) {
                   return "RIBBON_ERROR:Користувач " + parsedArgs[0] + " вже увійшов до системи!";
               }
               if (CURR_TYPE == CONNECTION_TYPES.CONTROL && (!AccessHandler.isUserAdmin(parsedArgs[0]))) {
                   return "RIBBON_ERROR:Користувач " + parsedArgs[0] + " не є адміністратором системи.";
               }
               String returned = AccessHandler.PROC_LOGIN_USER(parsedArgs[0], parsedArgs[1]);
               if (returned == null) {
                   if (CURR_TYPE == CONNECTION_TYPES.CLIENT) {
                       RibbonServer.logAppend(LOG_ID, 3, "користувач " + parsedArgs[0] + " увійшов до системи.");
                   } else if (CURR_TYPE == CONNECTION_TYPES.CONTROL) {
                       RibbonServer.logAppend(LOG_ID, 3, "адміністратор " + parsedArgs[0] + " увійшов до системи.");
                       if (RibbonServer.CONTROL_IS_PRESENT == false) {
                           RibbonServer.logAppend(RibbonServer.LOG_ID, 2, "ініційовано контроль системи!");
                           RibbonServer.CONTROL_IS_PRESENT = true;
                       }
                   }
                   CURR_SESSION.USER_NAME = parsedArgs[0];
                   CURR_SESSION.setSessionName();
                   return "OK:";
               } else {
                   return "RIBBON_ERROR:" + returned;
               }
           }
         });
         
         /**
          * RIBBON_NCTL_CLOSE: commandlet
          * Exit command to close connection.
          */
         this.RIBBON_COMMANDS.add(new CommandLet("RIBBON_NCTL_CLOSE", CONNECTION_TYPES.ANY) {
             @Override
             public String exec(String args) {
                 if (CURR_TYPE == CONNECTION_TYPES.CONTROL && SessionManager.hasOtherControl(CURR_SESSION) == false) {
                     RibbonServer.logAppend(RibbonServer.LOG_ID, 2, "контроль над системою завершено!");
                     RibbonServer.CONTROL_IS_PRESENT = false;
                 }
                 return "COMMIT_CLOSE:";
             }
         });
         
         /** GENERAL PROTOCOL STACK [LEVEL_1 SUPPORT] **/
         
         /**
          * RIBBON_GET_DIRS: commandlet
          * Return all dirs to client in csv form.
          */
         this.RIBBON_COMMANDS.add(new CommandLet("RIBBON_GET_DIRS", CONNECTION_TYPES.CLIENT) {
             @Override
             public String exec(String args) {
                 return Directories.PROC_GET_DIRS();
             }
         });
         
         /**
          * RIBBON_GET_TAGS: commandlet
          * Return all tags to client in csv form.
          */
         this.RIBBON_COMMANDS.add(new CommandLet("RIBBON_GET_TAGS", CONNECTION_TYPES.CLIENT) {
             @Override
             public String exec(String args) {
                 return Messenger.PROC_GET_TAGS();
             }
         });
         
         /**
          * RIBBON_LOAD_BASE_FROM_INDEX: commandlet
          * Return all messages which were released later than specified index.
          */
         this.RIBBON_COMMANDS.add(new CommandLet("RIBBON_LOAD_BASE_FROM_INDEX", CONNECTION_TYPES.CLIENT) {
             @Override
             public String exec(String args) {
                 return Messenger.PROC_LOAD_BASE_FROM_INDEX(args);
             }
         });
         
         /**
          * RIBBON_POST_MESSAGE: commandlet
          * Post message to the system.
          * WARNING! this commandlet grab socket control!
          */
         this.RIBBON_COMMANDS.add(new CommandLet("RIBBON_POST_MESSAGE", CONNECTION_TYPES.CLIENT) {
             @Override
             public String exec(String args) {
                 MessageClasses.Message recievedMessage = new MessageClasses.Message();
                 recievedMessage.createMessageForPost(args);
                 recievedMessage.AUTHOR = CURR_SESSION.USER_NAME;
                 Boolean collectMessage = true;
                 StringBuffer messageBuffer = new StringBuffer();
                 String inLine;
                 while (collectMessage) {
                     try {
                         inLine = CURR_SESSION.inStream.readLine();
                         if (!inLine.equals("END:")) {
                             messageBuffer.append(inLine);
                             messageBuffer.append("\n");
                         } else {
                             collectMessage = false;
                         }
                     } catch (java.io.IOException ex) {
                         return "RIBBON_ERROR:Неможливо прочитати повідомлення з сокету!";
                     }
                 }
                 recievedMessage.CONTENT = messageBuffer.toString();
                 String answer = Procedures.PROC_POST_MESSAGE(recievedMessage);
                 if (answer.equals("OK:")) {
                     BROADCAST_TAIL = "RIBBON_UCTL_LOAD_INDEX:" + recievedMessage.returnEntry().toCsv();
                     BROADCAST_TYPE = CONNECTION_TYPES.CLIENT;
                 }
                 return answer;
             }
         });
         
         /**
          * RIBBON_GET_MESSAGE: commandlet
          * Retrieve message body.
          */
         this.RIBBON_COMMANDS.add(new CommandLet("RIBBON_GET_MESSAGE", CONNECTION_TYPES.CLIENT) {
             @Override
             public String exec(String args) {
                 String[] parsedArgs = args.split(",");
                 String givenDir = parsedArgs[0];
                 String givenIndex = parsedArgs[1];
                 //String returnedContent = "";
                 StringBuffer returnedMessage = new StringBuffer();
                 if (AccessHandler.checkAccess(CURR_SESSION.USER_NAME, givenDir, 0) == false) {
                     return "RIBBON_ERROR:Помилка доступу до напрямку " + givenDir;
                 }
                 String dirPath = Directories.getDirPath(givenDir);
                 if (dirPath == null) {
                     return "RIBBON_ERROR:Напрямок " + givenDir + " не існує!";
                 } else {
                     try {
                         java.io.BufferedReader messageReader = new java.io.BufferedReader(new java.io.FileReader(dirPath + givenIndex));
                         while (messageReader.ready()) {
                             //returnedContent += messageReader.readLine() + "\n";
                             returnedMessage.append(messageReader.readLine());
                             returnedMessage.append("\n");
                         }
                         return returnedMessage.append("END:").toString();
                     } catch (java.io.FileNotFoundException ex) {
                         return "RIBBON_ERROR:Повідмолення не існує!";
                     } catch (java.io.IOException ex) {
                         RibbonServer.logAppend(LOG_ID, 1, "помилка зчитування повідомлення " + givenDir + ":" + givenIndex);
                         return "RIBBON_ERROR:Помилка виконання команди!";
                     }
                 }
             }
         });
         
         /**
          * RIBBON_MODIFY_MESSAGE: commandlet
          * Modify text of existing message.
          * WARNING! this commandlet grab socket control!
          */
         this.RIBBON_COMMANDS.add(new CommandLet("RIBBON_MODIFY_MESSAGE", CONNECTION_TYPES.CLIENT) {
             @Override
             public String exec(String args) {
                 StringBuffer messageBuffer = new StringBuffer();
                 String inLine;
                 Boolean collectMessage = true;
                 String[] parsedArgs = Generic.CsvFormat.splitCsv(args);
                 MessageClasses.MessageEntry matchedEntry = Messenger.getMessageEntryByIndex(parsedArgs[0]);
                 MessageClasses.Message modTemplate = new MessageClasses.Message();
                 modTemplate.createMessageForModify(parsedArgs[1]);
                 while (collectMessage) {
                     try {
                         inLine = CURR_SESSION.inStream.readLine();
                         if (!inLine.equals("END:")) {
                             messageBuffer.append(inLine);
                             messageBuffer.append("\n");
                         } else {
                             collectMessage = false;
                         }
                     } catch (java.io.IOException ex) {
                         return "RIBBON_ERROR:Неможливо прочитати повідомлення з сокету!";
                     }
                 }
                 modTemplate.CONTENT = messageBuffer.toString();
                 if (matchedEntry == null) {
                     return "RIBBON_ERROR:Повідмолення не існує!";
                 }
                 Integer oldIntFlag = AccessHandler.checkAccessForAll(CURR_SESSION.USER_NAME, matchedEntry.DIRS, 2);
                 Integer newIntFlag = AccessHandler.checkAccessForAll(CURR_SESSION.USER_NAME, modTemplate.DIRS, 1);
                 if ((CURR_SESSION.USER_NAME.equals(matchedEntry.AUTHOR) && (newIntFlag == null)) || ((oldIntFlag == null) && (newIntFlag == null))) {
                     for (Integer dirIndex = 0; dirIndex < matchedEntry.DIRS.length; dirIndex++) {
                         if (AccessHandler.checkAccess(CURR_SESSION.USER_NAME, matchedEntry.DIRS[dirIndex], 1) == true) {
                             continue;
                         } else {
                             return "RIBBON_ERROR:Помилка доступу до напрямку " + matchedEntry.DIRS[dirIndex] +  ".";
                         }
                     }
                     //RibbonServer.logAppend(RibbonServer.LOG_ID, 3, "повідомлення за індексом " + parsedArgs[1] + "(" + parsedArgs[0] + ") змінено");
                     //Procedures.writeMessage(matchedEntry.DIRS, matchedEntry.INDEX, messageContent);
                     Procedures.PROC_MODIFY_MESSAGE(matchedEntry, modTemplate);
                     BROADCAST_TAIL = "RIBBON_UCTL_UPDATE_INDEX:" + matchedEntry.toCsv();
                     BROADCAST_TYPE = CONNECTION_TYPES.CLIENT;
                     return "OK:";
                 } else {
                     //return "RIBBON_ERROR:Помилка доступу до повідомлення";
                     if (oldIntFlag != null) {
                         return "RIBBON_ERROR:Помилка доступу до напрямку " + matchedEntry.DIRS[oldIntFlag] +  ".";
                     } else {
                         return "RIBBON_ERROR:Помилка доступу до напрямку " + modTemplate.DIRS[newIntFlag] +  ".";
                     }
                 }
             }
         });
         
         /**
          * RIBBON_DELETE_MESSAGE: commandlet
          * Delete message from all directories.
          */
         this.RIBBON_COMMANDS.add(new CommandLet("RIBBON_DELETE_MESSAGE", CONNECTION_TYPES.CLIENT) {
             @Override
             public String exec(String args) {
                 MessageClasses.MessageEntry matchedEntry = Messenger.getMessageEntryByIndex(args);
                 if (matchedEntry == null) {
                     return "RIBBON_ERROR:Повідмолення не існує!";
                 } else {
                     if (matchedEntry.AUTHOR.equals(CURR_SESSION.USER_NAME) || (AccessHandler.checkAccessForAll(CURR_SESSION.USER_NAME, matchedEntry.DIRS, 2) == null)) {
                         Procedures.PROC_DELETE_MESSAGE(matchedEntry);
                         BROADCAST_TAIL = "RIBBON_UCTL_DELETE_INDEX:" + matchedEntry.INDEX;
                         BROADCAST_TYPE = CONNECTION_TYPES.CLIENT;
                         return "OK:";
                     } else {
                         return "RIBBON_ERROR:Помилка доступу до повідомлення.";
                     }
                 }
             }
         });
         
         /**
          * RIBBON_ADD_MESSAGE_PROPERTY: commandlet
          * Add custom property to message.
          */
         this.RIBBON_COMMANDS.add(new CommandLet("RIBBON_ADD_MESSAGE_PROPERTY", CONNECTION_TYPES.CLIENT) {
             public String exec(String args) {
                 String[] parsedArgs = Generic.CsvFormat.commonParseLine(args, 3);
                 MessageClasses.MessageEntry matchedEntry = Messenger.getMessageEntryByIndex(parsedArgs[0]);
                 if (matchedEntry == null) {
                     return "RIBBON_ERROR:Повідмолення не існує!";
                 }
                 if ((matchedEntry.AUTHOR.equals(CURR_SESSION.USER_NAME) || (AccessHandler.checkAccessForAll(CURR_SESSION.USER_NAME, matchedEntry.DIRS, 2) != null))) {
                     MessageClasses.MessageProperty newProp = new MessageClasses.MessageProperty();
                     newProp.PROPERTY_PREFIX = MessageClasses.MessagePropertyTypes.valueOf(parsedArgs[1]);
                     newProp.TEXT_MESSAGE = parsedArgs[2];
                     newProp.DATE = RibbonServer.getCurrentDate();
                     newProp.USER = CURR_SESSION.USER_NAME;
                     matchedEntry.PROPERTIES.add(newProp);
                     IndexReader.updateBaseIndex();
                    BROADCAST_TAIL = "RIBBON_UCTL_UPDATE_INDEX:" + matchedEntry.INDEX;
                     BROADCAST_TYPE = CONNECTION_TYPES.CLIENT;
                     return "OK:";
                 } else {
                     return "RIBBON_ERROR:Помилка доступу до повідомлення.";
                 }
             }
         });
         
         /** SERVER CONTROL PROTOCOL STACK [LEVEL_2 SUPPORT] **/
         
     }
     
     /**
      * Process input from session socket and return answer;
      * @param input input line from client
      * @return answer form protocol to client
      */
     public String process(String input) {
         String[] parsed = Generic.CsvFormat.parseDoubleStruct(input);
         return this.launchCommand(parsed[0], parsed[1]);
     }
     
     /**
      * Launch command execution
      * @param command command word
      * @param args command's arguments
      * @return return form commandlet object
      */
     private String launchCommand(String command, String args) {
         CommandLet exComm = null;
         java.util.ListIterator<CommandLet> commIter = this.RIBBON_COMMANDS.listIterator();
         while (commIter.hasNext()) {
             CommandLet currComm = commIter.next();
             if (currComm.COMMAND_NAME.equals(command)) {
                 if (currComm.COMM_TYPE == this.CURR_TYPE || currComm.COMM_TYPE == CONNECTION_TYPES.ANY || this.CURR_TYPE == CONNECTION_TYPES.CONTROL) {
                     if (this.CURR_SESSION.USER_NAME == null && (currComm.COMM_TYPE == CONNECTION_TYPES.CLIENT || currComm.COMM_TYPE == CONNECTION_TYPES.CONTROL)) {
                         return "RIBBON_ERROR:Вхід не виконано!\nRIBBON_GCTL_FORCE_LOGIN:";
                     } else {
                         exComm = currComm;
                     }
                     break;
                 } else {
                     return "RIBBON_ERROR:Ця команда не може бути використана цим з’єднанням!";
                 }
             }
         }
         if (exComm != null) {
             try {
                 return exComm.exec(args);
             } catch (Exception ex) {
                 if (RibbonServer.DEBUG_POST_EXCEPTIONS) {
                     StringBuffer exMesgBuf = new StringBuffer();
                     exMesgBuf.append("Помилка при роботі сесії ").append(this.CURR_SESSION.SESSION_TIP).append("(").append(RibbonServer.getCurrentDate()).append(")\n\n");
                     exMesgBuf.append("Команда:" + command + ":" + args + "\n\n");
                     exMesgBuf.append(ex.getClass().getName() + "\n");
                     StackTraceElement[] stackTrace = ex.getStackTrace();
                     for (StackTraceElement element : stackTrace) {
                         exMesgBuf.append(element.toString() + "\n");
                     }
                     MessageClasses.Message exMessage = new MessageClasses.Message(
                             "Звіт про помилку", "root", "UA", new String[] {RibbonServer.DEBUG_POST_DIR}, 
                             new String[] {"ІТУ", "ПОМИЛКИ"}, exMesgBuf.toString());
                     Procedures.PROC_POST_MESSAGE(exMessage);
                     BROADCAST_TAIL = "RIBBON_UCTL_LOAD_INDEX:" + exMessage.returnEntry().toCsv();
                     BROADCAST_TYPE = CONNECTION_TYPES.CLIENT;
                 }
                 RibbonServer.logAppend(LOG_ID, 1, "помилка при виконанні команди " + exComm.COMMAND_NAME + "!");
                 return "RIBBON_ERROR: помилка команди:" + ex.toString();
             }
         } else {
             return "RIBBON_ERROR:Невідома команда!";
         }
     }
 }
