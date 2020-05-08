 /**
  * This file is part of RibbonServer application (check README).
  * Copyright (C) 2012-2013 Stanislav Nepochatov
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 **/
 
 package ribbonserver;
 
 /**
  * Ribbon protocol server side class
  * @author Stanislav Nepochatov
  * @since RibbonServer a1
  */
 public class RibbonProtocol {
     
     private String LOG_ID = "ПРОТОКОЛ";
     
     /**
      * Tail of protocol result which should be delivered to all peers;
      * @since RibbonServer a1
      */
     public String BROADCAST_TAIL;
     
     /**
      * Type of broadcasting;
      * @since RibbonServer a1
      */
     public CONNECTION_TYPES BROADCAST_TYPE;
     
     /**
      * Remote session flag.
      */
     private Boolean IS_REMOTE = false;
     
     /**
      * Default constructor.
      * @param upperThread given session thread to link with;
      * @since RibbonServer a1
      */
     RibbonProtocol(SessionManager.SessionThread upperThread) {
         InitProtocol();
         CURR_SESSION = upperThread;
     }
     
     /**
      * Link to upper level thread
      * @since RibbonServer a1
      */
     private SessionManager.SessionThread CURR_SESSION;
     
     /**
      * Protocol revision digit.
      * @since RibbonServer a1
      */
     private Integer INT_VERSION = 2;
     
     /**
      * String protocol revision version.
      * @since RibbonServer a1
      */
     private String STR_VERSION = RibbonServer.RIBBON_VER;
     
     /**
      * Connection type enumeration.
      * @since RibbonServer a1
      */
     public enum CONNECTION_TYPES {
         
         /**
          * NULL connection: peer didn't call RIBBON_NCTL_INIT: yet.
          */
         NULL,
         
         /**
          * CLIENT connection for all user applications.
          */
         CLIENT,
         
         /**
          * CONTROL connection for all adm applications.
          */
         CONTROL,
         
         /**
          * Connection for any application.
          */
         ANY
     };
     
     /**
      * Current type of connection.
      * @since RibbonServer a1
      */
     public CONNECTION_TYPES CURR_TYPE = CONNECTION_TYPES.NULL;
     
     /**
      * ArrayList of commands objects.
      * @since RibbonServer a1
      */
     private java.util.ArrayList<CommandLet> RIBBON_COMMANDS = new java.util.ArrayList<CommandLet>();
     
     /**
      * Command template class.
      * @since RibbonServer a1
      */
     private class CommandLet {
         
         /**
          * Default constroctor.
          * @param givenName name of command;
          * @param givenType type of connections which may use this command;
          * @since RibbonServer a1
          */
         CommandLet(String givenName, CONNECTION_TYPES givenType) {
             this.COMMAND_NAME = givenName;
             this.COMM_TYPE = givenType;
         }
         
         /**
          * Name of command.
          * @since RibbonServer a1
          */
         public String COMMAND_NAME;
         
         /**
          * Type of connections which may use this command.
          * @since RibbonServer a1
          */
         public CONNECTION_TYPES COMM_TYPE;
         
         /**
          * Main command body.
          * @param args arguments from application <i>(may be in CSV format)</i>;
          * @return command answer;
          * @since RibbonServer a1
          */
         public String exec(String args) {return "";};
         
     }
     
     /**
      * Init protocol and load commands.
      * @since RibbonServer a1
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
                             if (parsedArgs[0].equals("ANY") || parsedArgs[0].equals("NULL")) {
                                 throw new IllegalArgumentException();
                             }
                             CURR_TYPE = CONNECTION_TYPES.valueOf(parsedArgs[0]);
                             if (!parsedArgs[2].equals(System.getProperty("file.encoding"))) {
                                 RibbonServer.logAppend(LOG_ID, 2, "мережева сесія вимогає іншої кодової сторінки:" + parsedArgs[2]);
                                 CURR_SESSION.setReaderEncoding(parsedArgs[2]);
                             }
                             return "OK:\nRIBBON_GCTL_FORCE_LOGIN:";
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
                   return "RIBBON_ERROR:Користувач " + parsedArgs[0] + " вже увійшов до системи!\nRIBBON_GCTL_FORCE_LOGIN:";
               }
               if (CURR_TYPE == CONNECTION_TYPES.CONTROL && (!AccessHandler.isUserIsMemberOf(parsedArgs[0], "ADM"))) {
                   return "RIBBON_ERROR:Користувач " + parsedArgs[0] + " не є адміністратором системи.\nRIBBON_GCTL_FORCE_LOGIN:";
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
                   if (RibbonServer.ACCESS_ALLOW_SESSIONS) {
                       CURR_SESSION.CURR_ENTRY = SessionManager.createSessionEntry(parsedArgs[0]);
                       CURR_SESSION.setSessionName();
                       return "OK:" + CURR_SESSION.CURR_ENTRY.SESSION_HASH_ID;
                   } else {
                       return "OK:";
                   }
               } else {
                   return "RIBBON_ERROR:" + returned + "\nRIBBON_GCTL_FORCE_LOGIN:";
               }
           }
         });
         
         /**
          * RIBBON_NCTL_GET_ID: commandlet
          * Find out session ID.
          */
         this.RIBBON_COMMANDS.add(new CommandLet("RIBBON_NCTL_GET_ID", CONNECTION_TYPES.ANY) {
             @Override
             public String exec(String args) {
                 if (!RibbonServer.ACCESS_ALLOW_SESSIONS) {
                     return "RIBBON_ERROR:Сесії вимкнено!\nRIBBON_GCTL_FORCE_LOGIN:";
                 } else if (CURR_SESSION.CURR_ENTRY == null) {
                     return "RIBBON_ERROR:Вхід не виконано!\nRIBBON_GCTL_FORCE_LOGIN:";
                 } else {
                     return CURR_SESSION.CURR_ENTRY.SESSION_HASH_ID;
                 }
             }
         });
         
         /**
          * RIBBON_NCTL_RESUME: commandlet
          * Resume session by given hash id.
          */
         this.RIBBON_COMMANDS.add(new CommandLet("RIBBON_NCTL_RESUME", CONNECTION_TYPES.ANY) {
             @Override
             public String exec(String args) {
                 if (!RibbonServer.ACCESS_ALLOW_SESSIONS) {
                     return "RIBBON_ERROR:Сесії вимкнено!\nRIBBON_GCTL_FORCE_LOGIN:";
                 }
                 SessionManager.SessionEntry exicted = SessionManager.getUserBySessionEntry(args);
                 if (exicted == null) {
                     return "RIBBON_GCTL_FORCE_LOGIN:";
                 } else {
                     String returned = AccessHandler.PROC_RESUME_USER(exicted);
                     if (returned == null) {
                         SessionManager.reniewEntry(exicted);
                           CURR_SESSION.USER_NAME = exicted.SESSION_USER_NAME;
                           CURR_SESSION.CURR_ENTRY = exicted;
                           CURR_SESSION.setSessionName();
                         return "OK:";
                     } else {
                         return "RIBBON_ERROR:" + returned + "\nRIBBON_GCTL_FORCE_LOGIN:";
                     }
                 }
             }
         });
         
         /**
          * RIBBON_NCTL_REM_LOGIN: commandlet
          * Remote login command.
          */
         this.RIBBON_COMMANDS.add(new CommandLet("RIBBON_NCTL_REM_LOGIN", CONNECTION_TYPES.ANY) {
             @Override
             public String exec(String args) {
                 if (!RibbonServer.ACCESS_ALLOW_REMOTE && !IS_REMOTE) {
                     return "RIBBON_ERROR:Видалений режим вимкнено!";
                 } else if (CURR_SESSION.USER_NAME == null) {
                     return "RIBBON_ERROR:Вхід не виконано!";
                 }
                 String[] parsedArgs = Generic.CsvFormat.commonParseLine(args, 2);
                 if (CURR_TYPE == CONNECTION_TYPES.CONTROL && (!AccessHandler.isUserIsMemberOf(parsedArgs[0], "ADM"))) {
                     return "RIBBON_ERROR:Користувач " + parsedArgs[0] + " не є адміністратором системи.\nRIBBON_GCTL_FORCE_LOGIN:";
                 }
                 String returned = AccessHandler.PROC_LOGIN_USER(parsedArgs[0], parsedArgs[1]);
                 if (returned == null) {
                     if (CURR_TYPE == CONNECTION_TYPES.CLIENT) {
                         RibbonServer.logAppend(LOG_ID, 3, "користувач " + parsedArgs[0] + " видалено увійшов до системи.");
                     } else if (CURR_TYPE == CONNECTION_TYPES.CONTROL) {
                         RibbonServer.logAppend(LOG_ID, 3, "адміністратор " + parsedArgs[0] + " видалено увійшов до системи.");
                     }
                     return "OK:";
                 } else {
                     return "RIBBON_ERROR:" + returned + "\nRIBBON_GCTL_FORCE_LOGIN:";
                 }
             }
         });
         
         /**
          * RIBBON_NCTL_GET_USERNAME: commandlet
          * Get current session username.
          */
         this.RIBBON_COMMANDS.add(new CommandLet("RIBBON_NCTL_GET_USERNAME", CONNECTION_TYPES.ANY) {
             public String exec(String args) {
                 if (CURR_SESSION.USER_NAME != null) {
                     return "OK:" + CURR_SESSION.USER_NAME;
                 } else {
                     return "RIBBON_ERROR:Вхід до системи не виконано!";
                 }
             }
         });
         
         /**
          * RIBBON_NCTL_SET_REMOTE_MODE: commandlet
          * Set remote flag of this session.
          */
         this.RIBBON_COMMANDS.add(new CommandLet("RIBBON_NCTL_SET_REMOTE_MODE", CONNECTION_TYPES.ANY) {
             @Override
             public String exec(String args) {
                 if (CURR_SESSION.USER_NAME == null) {
                     return "RIBBON_ERROR:Вхід не виконано!";
                 } else if (!RibbonServer.ACCESS_ALLOW_REMOTE) {
                     return "RIBBON_ERROR:Видалений режим вимкнено!";
                 } else if (!AccessHandler.isUserIsMemberOf(CURR_SESSION.USER_NAME, RibbonServer.ACCESS_REMOTE_GROUP)) {
                     return "RIBBON_ERROR:Ця сессія не може використовувати видалений режим!";
                 }
                 IS_REMOTE = "1".equals(args) ? true : false;
                 if (IS_REMOTE) {
                     RibbonServer.logAppend(LOG_ID, 3, "увімкнено видалений режим (" + CURR_SESSION.SESSION_TIP + ")");
                 } else {
                     RibbonServer.logAppend(LOG_ID, 3, "вимкнено видалений режим (" + CURR_SESSION.SESSION_TIP + ")");
                 }
                 return "OK:" + (IS_REMOTE ? "1" : "0");
             }
         });
         
         /**
          * RIBBON_NCTL_ACCESS_CONTEXT: commandlet
          * Change access mode of next command.
          * WARNING! this commandlet grab socket control!
          * WARNING! this commandlet calls to process() method directly!
          */
         this.RIBBON_COMMANDS.add(new CommandLet("RIBBON_NCTL_ACCESS_CONTEXT", CONNECTION_TYPES.ANY) {
             @Override
             public String exec(String args) {
                 if (CURR_SESSION.USER_NAME == null) {
                     return "RIBBON_ERROR:Вхід не виконано!";
                 } else if (!IS_REMOTE) {
                     return "RIBBON_ERROR:Видалений режим вимкнено!";
                 }
                 UserClasses.UserEntry overUser = AccessHandler.getEntryByName(Generic.CsvFormat.commonParseLine(args, 1)[0]);
                 if (overUser == null) {
                     return "RIBBON_ERROR:Користувача не знайдено!";
                 } else if (!overUser.IS_ENABLED) {
                     return "RIBBON_ERROR:Користувача заблоковано!";
                 }
                 String oldUserName = CURR_SESSION.USER_NAME;
                 CURR_SESSION.USER_NAME = overUser.USER_NAME;
                 CURR_SESSION.outStream.println("PROCEED:");
                 String subResult = null;
                 try {
                     subResult = process(CURR_SESSION.inStream.readLine());
                 } catch (java.io.IOException ex) {
                     RibbonServer.logAppend(LOG_ID, 1, "неможливо прочитати дані з сокету!");
                     SessionManager.closeSession(CURR_SESSION);
                 }
                 CURR_SESSION.USER_NAME = oldUserName;
                 return subResult;
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
          * RIBBON_GET_PSEUDO: commandlet
          * Return csv list of pseudo directories which user may use.
          */
         this.RIBBON_COMMANDS.add(new CommandLet("RIBBON_GET_PSEUDO", CONNECTION_TYPES.CLIENT) {
             @Override
             public String exec(String args) {
                 if (IS_REMOTE) {
                     if (CURR_SESSION.USER_NAME == null) {
                         return "RIBBON_ERROR:Вхід не виконано!";
                     }
                     return Directories.PROC_GET_PSEUDO(CURR_SESSION.USER_NAME);
                 } else {
                     return "RIBBON_ERROR:Видалений режим вимкнено!";
                 }
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
          * RIBBON_POST_MESSAGE_BY_PSEUDO: commandlet
          * Post message from remote interface to the system by using pseudo directory.
          * WARNING! this commandlet grab socket control!
          * WARNING! this commandlet calls to RIBBON_POST_MESSAGE commandlet
          */
         this.RIBBON_COMMANDS.add(new CommandLet("RIBBON_POST_MESSAGE_BY_PSEUDO", CONNECTION_TYPES.CLIENT) {
             @Override
             public String exec(String args) {
                 if (IS_REMOTE) {
                     java.util.ArrayList<String[]> parsed = Generic.CsvFormat.complexParseLine(args, 4, 1);
                     Directories.PseudoDirEntry currPostPseudo = Directories.getPseudoDir(parsed.get(0)[0]);
                     if (currPostPseudo == null) {
                         return "RIBBON_ERROR:Псевдонапрямок " + parsed.get(0)[0] + " не існує.";
                     }
                     String[] postDirs = currPostPseudo.getinternalDirectories();
                    String commandToPost = "RIBBON_POST_MESSAGE:-1," + Generic.CsvFormat.renderGroup(postDirs) + args.substring(currPostPseudo.PSEUDO_DIR_NAME.length() + 2);
                     RibbonServer.logAppend(LOG_ID, 3, "додано повідомлення через псевдонапрямок '" + currPostPseudo.PSEUDO_DIR_NAME + "'");
                     return process(commandToPost);
                 } else {
                     return "RIBBON_ERROR:Видалений режим вимкнено!";
                 }
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
                     Procedures.PROC_MODIFY_MESSAGE(matchedEntry, modTemplate);
                     BROADCAST_TAIL = "RIBBON_UCTL_UPDATE_INDEX:" + matchedEntry.toCsv();
                     BROADCAST_TYPE = CONNECTION_TYPES.CLIENT;
                     return "OK:";
                 } else {
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
             @Override
             public String exec(String args) {
                 String[] parsedArgs = Generic.CsvFormat.commonParseLine(args, 3);
                 MessageClasses.MessageEntry matchedEntry = Messenger.getMessageEntryByIndex(parsedArgs[0]);
                 if (matchedEntry == null) {
                     return "RIBBON_ERROR:Повідмолення не існує!";
                 }
                 if ((matchedEntry.AUTHOR.equals(CURR_SESSION.USER_NAME) || (AccessHandler.checkAccessForAll(CURR_SESSION.USER_NAME, matchedEntry.DIRS, 2) != null))) {
                     MessageClasses.MessageProperty newProp = new MessageClasses.MessageProperty(parsedArgs[1], CURR_SESSION.USER_NAME, parsedArgs[2], RibbonServer.getCurrentDate());
                     newProp.TYPE = parsedArgs[1];
                     newProp.TEXT_MESSAGE = parsedArgs[2];
                     newProp.DATE = RibbonServer.getCurrentDate();
                     newProp.USER = CURR_SESSION.USER_NAME;
                     matchedEntry.PROPERTIES.add(newProp);
                     IndexReader.updateBaseIndex();
                     BROADCAST_TAIL = "RIBBON_UCTL_UPDATE_INDEX:" + matchedEntry.toCsv();
                     BROADCAST_TYPE = CONNECTION_TYPES.CLIENT;
                     return "OK:";
                 } else {
                     return "RIBBON_ERROR:Помилка доступу до повідомлення.";
                 }
             }
         });
         
         /**
          * RIBBON_DEL_MESSAGE_PROPERTY: commandlet
          * Del custom property from specified message.
          */
         this.RIBBON_COMMANDS.add(new CommandLet("RIBBON_DEL_MESSAGE_PROPERTY", CONNECTION_TYPES.CLIENT) {
             @Override
             public String exec(String args) {
                 String[] parsedArgs = Generic.CsvFormat.commonParseLine(args, 3);
                 MessageClasses.MessageEntry matchedEntry = Messenger.getMessageEntryByIndex(parsedArgs[0]);
                 if (matchedEntry == null) {
                     return "RIBBON_ERROR:Повідмолення не існує!";
                 }
                 if ((matchedEntry.AUTHOR.equals(CURR_SESSION.USER_NAME) || (AccessHandler.checkAccessForAll(CURR_SESSION.USER_NAME, matchedEntry.DIRS, 2) != null))) {
                     MessageClasses.MessageProperty findedProp = null;
                     java.util.ListIterator<MessageClasses.MessageProperty> propIter = matchedEntry.PROPERTIES.listIterator();
                     while (propIter.hasNext()) {
                         MessageClasses.MessageProperty currProp = propIter.next();
                         if (currProp.TYPE.equals(parsedArgs[1]) && currProp.DATE.equals(parsedArgs[2])) {
                             findedProp = currProp;
                             break;
                         }
                     }
                     if (findedProp != null) {
                         matchedEntry.PROPERTIES.remove(findedProp);
                         IndexReader.updateBaseIndex();
                         BROADCAST_TAIL = "RIBBON_UCTL_UPDATE_INDEX:" + matchedEntry.toCsv();
                         BROADCAST_TYPE = CONNECTION_TYPES.CLIENT;
                         return "OK:";
                     } else {
                         return "RIBBON_ERROR:Системної ознаки не існує!";
                     }
                 } else {
                     return "RIBBON_ERROR:Помилка доступу до повідомлення.";
                 }
             }
         });
         
         /**
          * RIBBON_GET_USERS: commandlet
          * Get all system users without ADM group members.
          */
         this.RIBBON_COMMANDS.add(new CommandLet("RIBBON_GET_USERS", CONNECTION_TYPES.CLIENT) {
             @Override
             public String exec(String args) {
                 return AccessHandler.PROC_GET_USERS_UNI(false);
             }
         });
         
         /** SERVER CONTROL PROTOCOL STACK [LEVEL_2 SUPPORT] **/
         
     }
     
     /**
      * Process input from session socket and return answer;
      * @param input input line from client
      * @return answer form protocol to client
      * @since RibbonServer a1
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
      * @since RibbonServer a1
      */
     private String launchCommand(String command, String args) {
         CommandLet exComm = null;
         java.util.ListIterator<CommandLet> commIter = this.RIBBON_COMMANDS.listIterator();
         while (commIter.hasNext()) {
             CommandLet currComm = commIter.next();
             if (currComm.COMMAND_NAME.equals(command)) {
                 if (currComm.COMM_TYPE == this.CURR_TYPE || (currComm.COMM_TYPE == CONNECTION_TYPES.ANY && this.CURR_TYPE != CONNECTION_TYPES.NULL) || this.CURR_TYPE == CONNECTION_TYPES.CONTROL) {
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
