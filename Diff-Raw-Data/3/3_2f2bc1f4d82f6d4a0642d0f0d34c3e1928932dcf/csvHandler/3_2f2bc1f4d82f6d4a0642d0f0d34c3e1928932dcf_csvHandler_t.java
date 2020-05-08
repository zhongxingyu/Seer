 /*
  * This code is distributed under terms of GNU GPLv2.
  * *See LICENSE file.
  * ©UKRINFORM 2011-2012
  */
 
 package ribbonserver;
 
 /**
  * Read, parse and store CSV Ribbon configurations and base index class
  * @author Stanislav Nepochatov
  */
 public abstract class csvHandler {
     
     private static String LOG_ID = "Обробник конфігурацій";
     
     /**
      * Read directories in directory index file
      * @return arraylist of dir shemas
      * @see Directories.dirSchema
      */
     public static java.util.ArrayList<Directories.dirSchema> readDirectories() {
         java.util.ArrayList<Directories.dirSchema> Dirs = new java.util.ArrayList<Directories.dirSchema>();
         try {
             java.io.BufferedReader dirIndexReader = new java.io.BufferedReader(new java.io.FileReader(RibbonServer.BASE_PATH + "/" + RibbonServer.DIR_INDEX_PATH));
             while (dirIndexReader.ready()) {
                 Dirs.add(new Directories.dirSchema(parseDirLine(dirIndexReader.readLine())));
             }
         } catch (java.io.FileNotFoundException ex) {
             RibbonServer.logAppend(LOG_ID, 2, "попередній файл індексу напрявків не знайдено. Створюю новий.");
             java.io.File dirIndexFile = new java.io.File(RibbonServer.BASE_PATH + "/" + RibbonServer.DIR_INDEX_PATH);
             try {
                 dirIndexFile.createNewFile();
                 java.io.FileWriter dirIndexWriter = new java.io.FileWriter(dirIndexFile);
                 dirIndexWriter.write("СИСТЕМА.ТЕСТ,{Тестовий напрямок випуску},1\nСИСТЕМА.Оголошення,{Системні оголошення},0");
                 dirIndexWriter.close();
                Dirs.add(new Directories.dirSchema("СИСТЕМА.ТЕСТ", "Тестовий напрямок випуску", "1"));
                Dirs.add(new Directories.dirSchema("СИСТЕМА.Оголошення", "Системні оголошення", "0"));
             } catch (java.io.IOException exq) {
                 RibbonServer.logAppend(LOG_ID, 0, "неможливо створити новий файл індексу напрямків!");
                 System.exit(4);
             }
         } catch (java.io.IOException ex) {
             RibbonServer.logAppend(LOG_ID, 0, "помилка читання файлу індекса напрямків!");
             System.exit(4);
         }
         return Dirs;
     }
     
     /**
      * Parse single directory cvs line
      * @param dirLine line of text config
      * @return array of string for creating directory schema
      */
     private static String[] parseDirLine(String dirLine) {
         String[] returned = new String[3];
         Integer beginSlice = 0;
         Integer acceptedIndex = -1;
         for (Integer index = 0; index < dirLine.length(); index++) {
             char currChar = dirLine.charAt(index);
             if ((currChar != '{') && (currChar != ',')) {
                 continue;
             } else if (currChar == '{') {
                 beginSlice = index + 1;
                 for (Integer closeIndex = index + 1; closeIndex < dirLine.length(); closeIndex++) {
                     char incurrChar = dirLine.charAt(closeIndex);
                     if (incurrChar == '}') {
                         returned[++acceptedIndex] = dirLine.substring(beginSlice, closeIndex);
                         index = closeIndex + 1;
                         break;
                     }
                     if (closeIndex == dirLine.length() - 1) {
                         RibbonServer.logAppend(LOG_ID, 1, "пошкоджена строка: " + dirLine);
                     }
                 }
             } else if ((currChar == ',') && (dirLine.charAt(index + 1) != '}')) {
                 returned[++acceptedIndex] = dirLine.substring(beginSlice, index);
                 beginSlice = index + 1;
             } 
             if (dirLine.substring(index + 1).indexOf(",") == -1) {
                 returned[++acceptedIndex] = dirLine.substring(index + 1);
                 break;
             }
         }
         return returned;
     }
     
     /**
      * Read users in users index file
      * @return arrayList of users entries
      */
     public static java.util.ArrayList<Users.userEntry> readUsers() {
         java.util.ArrayList<Users.userEntry> returnedUsers = new java.util.ArrayList<Users.userEntry>();
         try {
             java.io.BufferedReader userIndexReader = new java.io.BufferedReader(new java.io.FileReader(RibbonServer.BASE_PATH + "/" + RibbonServer.USERS_INDEX_PATH));
             while (userIndexReader.ready()) {
                 returnedUsers.add(new Users.userEntry(complexParseLine(userIndexReader.readLine(), 4, 1)));
             }
         } catch (java.io.FileNotFoundException ex) {
             RibbonServer.logAppend(LOG_ID, 2, "попередній файл індексу користувачів не знайдено. Створюю новий.");
             java.io.File usersIndexFile = new java.io.File(RibbonServer.BASE_PATH + "/" + RibbonServer.USERS_INDEX_PATH);
             try {
                 usersIndexFile.createNewFile();
                 java.io.FileWriter usersIndexWriter = new java.io.FileWriter(usersIndexFile);
                 usersIndexWriter.write("{root},{Root administrator, pass: root},[ADM],74cc1c60799e0a786ac7094b532f01b1,1\n");
                 usersIndexWriter.write("{test},{Test user, pass: test},[test],d8e8fca2dc0f896fd7cb4cb0031ba249,1\n");
                 usersIndexWriter.close();
                 returnedUsers.add(new Users.userEntry(complexParseLine("{root},{Root administrator, pass: root},[ADM],74cc1c60799e0a786ac7094b532f01b1,1", 4, 1)));
                 returnedUsers.add(new Users.userEntry(complexParseLine("{test},{Test user, pass: test},[test],d8e8fca2dc0f896fd7cb4cb0031ba249,1", 4, 1)));
             } catch (java.io.IOException exq) {
                 RibbonServer.logAppend(LOG_ID, 0, "неможливо створити новий файл індексу користувачів!");
                 System.exit(5);
             }
         } catch (java.io.IOException ex) {
             RibbonServer.logAppend(LOG_ID, 0, "помилка читання файлу індекса користувачів!");
             System.exit(4);
         }
         return returnedUsers;
     }
     
     /**
      * Read groups in groups index file
      * @return arrayList of groups entries
      */
     public static java.util.ArrayList<Groups.groupEntry> readGroups() {
         java.util.ArrayList<Groups.groupEntry> returnedGroups = new java.util.ArrayList<Groups.groupEntry>();
         try {
             java.io.BufferedReader groupIndexReader = new java.io.BufferedReader(new java.io.FileReader(RibbonServer.BASE_PATH + "/" + RibbonServer.GROUPS_INDEX_PATH));
             Integer currentLine = 0;
             while (groupIndexReader.ready()) {
                 String[] parsedArgs = commonParseLine(groupIndexReader.readLine(), 2);
                 if (parsedArgs != null) {
                     returnedGroups.add(new Groups.groupEntry(parsedArgs));
                 } else {
                     RibbonServer.logAppend(LOG_ID, 1, "помилка при опрацюванні індексу груп (" + currentLine + ")");
                 }
                 currentLine++;
             }
         } catch (java.io.FileNotFoundException ex) {
             RibbonServer.logAppend(LOG_ID, 2, "попередній файл індексу груп не знайдено. Створюю новий.");
             java.io.File usersIndexFile = new java.io.File(RibbonServer.BASE_PATH + "/" + RibbonServer.GROUPS_INDEX_PATH);
             try {
                 usersIndexFile.createNewFile();
                 java.io.FileWriter usersIndexWriter = new java.io.FileWriter(usersIndexFile);
                 usersIndexWriter.write("{test},{Test group}\n");
                 usersIndexWriter.close();
                 returnedGroups.add(new Groups.groupEntry(new String[] {"test", "Test group"}));
             } catch (java.io.IOException exq) {
                 RibbonServer.logAppend(LOG_ID, 0, "неможливо створити новий файл індексу груп!");
                 System.exit(5);
             }
         } catch (java.io.IOException ex) {
             RibbonServer.logAppend(LOG_ID, 0, "помилка читання файлу індекса груп!");
             System.exit(4);
         }
         return returnedGroups;
     }
     
     /**
      * Notify main parser method about special chars<br><br>
      * <<b>Statuses:</b><br>
      * <b>0</b> : ordinary char<br>
      * <b>1</b> : comma separator<br>
      * <b>2</b> : solid begining<br>
      * <b>3</b> : solid ending<br>
      * <b>4</b> : group begining<br>
      * <b>5</b> : group ending<br>
      * <b>6</b> : ignore comma separator<br>
      * <b>7</b> : increase index command<br>
      * @param prevCh previos char
      * @param ch current char
      * @param nextCh next char
      * @return parse code status<br>
      */
     private static Integer parseMarker(char prevCh, char ch, char nextCh) {
         switch (ch) {
             case ',':
                 if (((nextCh == '{') || (nextCh == '[')) && ((prevCh == '}') || (prevCh == ']'))) {
                     return 0;
                 } else if ((prevCh == '}') || (prevCh == ']')) {
                     return 7;
                 } else {
                     return 1;
                 }
             case '{':
                 return 2;
             case '}':
                 return 3;
             case '[':
                 return 4;
             case ']':
                 return 5;
         }
         return 0;
     }
     
     /**
      * Find out if there is more separators
      * @param restOfLine rest of parsed line
      * @return true if rest of line contains at least one separator;
      */
     private static Boolean hasMoreSeparators(String restOfLine) {
         String[] separators = new String[] {",", "{", "}", "[", "]"};
         for (Integer sepIndex = 0; sepIndex < separators.length; sepIndex++) {
             if (restOfLine.contains(separators[sepIndex]) == true) {
                 return true;
             }
         }
         return false;
     }
     
     /**
      * Parse single user index line
      * @param userLine line of config
      * @return userEntry object
      * @see Users.userEntry
      * @deprecated 
      */
     public static Users.userEntry parseUserLine(String userLine) {
         String[] userArray = new String[2];
         Integer beginSlice = 0;
         Integer acceptedIndex = -1;
         Boolean ignoreComma = false;
         for (Integer index = 0; index < userLine.length(); index++) {
             char currChar = userLine.charAt(index);
             char nextChar = '1';
             char prevChar = '1';
             if (index > 0) {
                 prevChar = userLine.charAt(index - 1);
             }
             if (index < userLine.length() - 1) {
                 nextChar = userLine.charAt(index + 1);
             }
             switch (parseMarker(prevChar, currChar, nextChar)) {
                 case 0:
                     continue;
                 case 1:
                     if (ignoreComma == false) {
                         userArray[++acceptedIndex] = userLine.substring(beginSlice, index);
                         beginSlice = index + 1;
                     }
                     break;
                 case 2:
                     beginSlice = index + 1;
                     ignoreComma = true;
                     break;
                 case 3:
                     if (ignoreComma == true) {
                         ignoreComma = false;
                         userArray[++acceptedIndex] = userLine.substring(beginSlice, index);
                         beginSlice = index + 1;
                     }
                     break;
                 case 6:
                     ignoreComma = true;
                     break;
             }
             if ((!hasMoreSeparators(userLine.substring(index + 1))) && (index < userLine.length() - 1)) {
                 userArray[++acceptedIndex] = userLine.substring(index + 1);
                 break;
             }
         }
         return new Users.userEntry(userArray);
     }
     
     /**
      * Read message indexes in base index file
      * @return arraylist with index entries
      */
     public static java.util.ArrayList<Messenger.messageEntry> readBaseIndex() {
         java.util.ArrayList<Messenger.messageEntry> returnedIndex = new java.util.ArrayList<Messenger.messageEntry>();
         try {
             java.io.BufferedReader baseIndexReader = new java.io.BufferedReader(new java.io.FileReader(RibbonServer.BASE_PATH + "/" + RibbonServer.BASE_INDEX_PATH));
             while (baseIndexReader.ready()) {
                 returnedIndex.add(parseBaseLine(baseIndexReader.readLine()));
             }
         } catch (java.io.FileNotFoundException ex) {
             RibbonServer.logAppend(LOG_ID, 2, "попередній файл індексу бази не знайдено. Створюю новий.");
             java.io.File usersIndexFile = new java.io.File(RibbonServer.BASE_PATH + "/" + RibbonServer.BASE_INDEX_PATH);
             try {
                 usersIndexFile.createNewFile();
             } catch (java.io.IOException exq) {
                 RibbonServer.logAppend(LOG_ID, 0, "неможливо створити новий файл індексу бази!");
                 System.exit(5);
             }
         } catch (java.io.IOException ex) {
             RibbonServer.logAppend(LOG_ID, 0, "помилка читання файлу індекса бази повідомлень!");
             System.exit(4);
         }
         return returnedIndex;
     }
 
     /**
      * Parse single base index line
      * @param baseLine line of config
      * @return messageEntry object
      * @see Messenger.messageEntry
      */
     private static Messenger.messageEntry parseBaseLine(String baseLine) {
         String[] baseArray = new String[4];
         String[] dirArray = null;
         String[] tagArray = null;
         Integer beginSlice = 0;
         Integer acceptedIndex = -1;
         Boolean ignoreComma = false;
         for (Integer index = 0; index < baseLine.length(); index++) {
             char currChar = baseLine.charAt(index);
             char nextChar = '1';
             char prevChar = '1';
             if (index > 0) {
                 prevChar = baseLine.charAt(index - 1);
             }
             if (index < baseLine.length() - 1) {
                 nextChar = baseLine.charAt(index + 1);
             }
             switch (parseMarker(prevChar, currChar, nextChar)) {
                 case 0:
                     continue;
                 case 1:
                     if (ignoreComma == false) {
                         baseArray[++acceptedIndex] = baseLine.substring(beginSlice, index);
                         beginSlice = index + 1;
                     }
                     break;
                 case 2:
                     beginSlice = index + 1;
                     ignoreComma = true;
                     break;
                 case 3:
                     if (ignoreComma == true) {
                         ignoreComma = false;
                         baseArray[++acceptedIndex] = baseLine.substring(beginSlice, index);
                         beginSlice = index + 1;
                     }
                     break;
                 case 4:
                     beginSlice = index + 1;
                     ignoreComma = true;
                     break;
                 case 5:
                     if (dirArray == null) {
                         if (ignoreComma == true) {
                             ignoreComma = false;
                             dirArray = baseLine.substring(beginSlice, index).split(",");
                             beginSlice = index + 1;
                         }
                     } else {
                         if (ignoreComma == true) {
                             ignoreComma = false;
                             tagArray = baseLine.substring(beginSlice, index).split(",");
                             beginSlice = index + 1;
                         }
                     }
                     break;
                 case 6:
                     ignoreComma = true;
                     break;
                 case 7:
                     beginSlice = index + 1;
                     break;
             }
             if ((!hasMoreSeparators(baseLine.substring(index + 1))) && (index < baseLine.length() - 1)) {
                 baseArray[++acceptedIndex] = baseLine.substring(index + 1);
                 break;
             }
         }
         return new Messenger.messageEntry(baseArray, dirArray, tagArray);
     }
     
     /**
      * Append new message csv to base index file
      * @param csvReport csv formated string
      */
     public static void appendToBaseIndex(String csvReport) {
         try {
             java.io.FileWriter messageWriter = new java.io.FileWriter(RibbonServer.BASE_PATH + "/" + RibbonServer.BASE_INDEX_PATH, true);
             messageWriter.write(csvReport + "\n");
             messageWriter.close();
         } catch (java.io.IOException ex) {
             RibbonServer.logAppend(LOG_ID, 0, "Неможливо записита файл индекса бази повідомлень!");
         }
     }
     
     /**
      * Update base index file after message manipulations
      * @param currStore arrayList of messages entries
      */
     public static void updateBaseIndex(java.util.ArrayList<Messenger.messageEntry> currStore) {
         java.util.ListIterator<Messenger.messageEntry> storeIter = currStore.listIterator();
         String newIndexFileContent = "";
         while (storeIter.hasNext()) {
             newIndexFileContent += storeIter.next().toCsv() + "\n";
         }
         try {
             java.io.FileWriter messageWriter = new java.io.FileWriter(RibbonServer.BASE_PATH + "/" + RibbonServer.BASE_INDEX_PATH);
             messageWriter.write(newIndexFileContent);
             messageWriter.close();
         } catch (java.io.IOException ex) {
             RibbonServer.logAppend(LOG_ID, 0, "Неможливо записита файл индекса бази повідомлень!");
         }
     }
     
     /**
      * Parse single base index line with variable amount of base arguments
      * @param messageLine line of config
      * @param argsCount amount of basic arguments
      * @return messageEntry object
      * @see Messenger.messageEntry
      */
     public static Messenger.Message net_parseMessageLine(String messageLine, Integer argsCount) {
         String[] baseArray = new String[argsCount];
         String[] dirArray = null;
         String[] tagArray = null;
         Integer beginSlice = 0;
         Integer acceptedIndex = -1;
         Boolean ignoreComma = false;
         for (Integer index = 0; index < messageLine.length(); index++) {
             char currChar = messageLine.charAt(index);
             char nextChar = '1';
             char prevChar = '1';
             if (index > 0) {
                 prevChar = messageLine.charAt(index - 1);
             }
             if (index < messageLine.length() - 1) {
                 nextChar = messageLine.charAt(index + 1);
             }
             switch (parseMarker(prevChar, currChar, nextChar)) {
                 case 0:
                     continue;
                 case 1:
                     if (ignoreComma == false) {
                         baseArray[++acceptedIndex] = messageLine.substring(beginSlice, index);
                         beginSlice = index + 1;
                     }
                     break;
                 case 2:
                     beginSlice = index + 1;
                     ignoreComma = true;
                     break;
                 case 3:
                     if (ignoreComma == true) {
                         ignoreComma = false;
                         baseArray[++acceptedIndex] = messageLine.substring(beginSlice, index);
                         beginSlice = index + 1;
                     }
                     break;
                 case 4:
                     beginSlice = index + 1;
                     ignoreComma = true;
                     break;
                 case 5:
                     if (dirArray == null) {
                         if (ignoreComma == true) {
                             ignoreComma = false;
                             dirArray = messageLine.substring(beginSlice, index).split(",");
                             beginSlice = index + 1;
                         }
                     } else {
                         if (ignoreComma == true) {
                             ignoreComma = false;
                             tagArray = messageLine.substring(beginSlice, index).split(",");
                             beginSlice = index + 1;
                         }
                     }
                     break;
                 case 6:
                     ignoreComma = true;
                     break;
                 case 7:
                     beginSlice = index + 1;
                     break;
             }
             if ((!hasMoreSeparators(messageLine.substring(index + 1))) && (index < messageLine.length() - 1)) {
                 baseArray[++acceptedIndex] = messageLine.substring(index + 1);
                 break;
             }
         }
         return new Messenger.Message(baseArray, dirArray, tagArray);
     }
     
     /**
      * Commin parse line method (without groups support).<br>
      * Using to unify parse methodic.
      * @param inputLine line to parse
      * @param baseArrLength length of base fields in csv line
      * @return string array with parsed words or null if parsing error occured
      * @since RibbonServer a2
      */
     public static String[] commonParseLine(String inputLine, Integer baseArrLength) {
         String[] baseArray = new String[baseArrLength];
         Integer beginSlice = 0;
         Integer acceptedIndex = -1;
         Boolean ignoreComma = false;
         for (Integer index = 0; index < inputLine.length(); index++) {
             char currChar = inputLine.charAt(index);
             char nextChar = '1';
             char prevChar = '1';
             if (index > 0) {
                 prevChar = inputLine.charAt(index - 1);
             }
             if (index < inputLine.length() - 1) {
                 nextChar = inputLine.charAt(index + 1);
             }
             switch (parseMarker(prevChar, currChar, nextChar)) {
                 case 0:
                     continue;
                 case 1:
                     if (ignoreComma == false) {
                         if (acceptedIndex < baseArrLength - 1) {
                             baseArray[++acceptedIndex] = inputLine.substring(beginSlice, index);
                             beginSlice = index + 1;
                         } else {
                             return null;
                         }
                     }
                     break;
                 case 2:
                     beginSlice = index + 1;
                     ignoreComma = true;
                     break;
                 case 3:
                     if (ignoreComma == true) {
                         ignoreComma = false;
                         if (acceptedIndex < baseArrLength - 1) {
                             baseArray[++acceptedIndex] = inputLine.substring(beginSlice, index);
                             beginSlice = index + 1;
                         } else {
                             return null;
                         }
                     }
                     break;
                 case 4:
                     return null;
                 case 5:
                     return null;
                 case 6:
                     ignoreComma = true;
                     break;
                 case 7:
                     beginSlice = index + 1;
                     break;
             }
             if ((!hasMoreSeparators(inputLine.substring(index + 1))) && (index < inputLine.length() - 1)) {
                 if (acceptedIndex < baseArrLength - 1) {
                     baseArray[++acceptedIndex] = inputLine.substring(index + 1);
                 } else {
                     return null;
                 }
                 break;
             }
         }
         return baseArray;
     }
     
     /**
      * Complex parse line method (with groups support).<br>
      * Using to unify parse methodic.
      * @param inputLine line to parse
      * @param baseArrLength length of base fields in csv line
      * @param groupsCount count of additional arrays with groups parsed words
      * @return arraylist with string arrays of parsed words or null if parsing error occured
      * @since RibbonServer a2
      */
     public static java.util.ArrayList<String[]> complexParseLine(String inputLine, Integer baseArrLength, Integer groupsCount) {
         java.util.ArrayList<String[]> returnedArr = new java.util.ArrayList();
         String[] baseArray = new String[baseArrLength];
         java.util.ArrayList<String[]> tempGroupArray = new java.util.ArrayList();
         Integer beginSlice = 0;
         Integer acceptedIndex = -1;
         Boolean ignoreComma = false;
         for (Integer index = 0; index < inputLine.length(); index++) {
             char currChar = inputLine.charAt(index);
             char nextChar = '1';
             char prevChar = '1';
             if (index > 0) {
                 prevChar = inputLine.charAt(index - 1);
             }
             if (index < inputLine.length() - 1) {
                 nextChar = inputLine.charAt(index + 1);
             }
             switch (parseMarker(prevChar, currChar, nextChar)) {
                 case 0:
                     continue;
                 case 1:
                     if (ignoreComma == false) {
                         if (acceptedIndex < baseArrLength) {
                             baseArray[++acceptedIndex] = inputLine.substring(beginSlice, index);
                             beginSlice = index + 1;
                         } else {
                             return null;
                         }
                     }
                     break;
                 case 2:
                     beginSlice = index + 1;
                     ignoreComma = true;
                     break;
                 case 3:
                     if (ignoreComma == true) {
                         ignoreComma = false;
                         if (acceptedIndex < baseArrLength) {
                             baseArray[++acceptedIndex] = inputLine.substring(beginSlice, index);
                             beginSlice = index + 1;
                         } else {
                             return null;
                         }
                     }
                     break;
                 case 4:
                     beginSlice = index + 1;
                     ignoreComma = true;
                     break;
                 case 5:
                     if (ignoreComma == true) {
                         ignoreComma = false;
                         if (tempGroupArray.size() < groupsCount) {
                             tempGroupArray.add(inputLine.substring(beginSlice, index).split(","));
                         } else {
                             return null;
                         }
                         beginSlice = index + 1;
                     }
                     break;
                 case 6:
                     ignoreComma = true;
                     break;
                 case 7:
                     beginSlice = index + 1;
                     break;
             }
             if ((!hasMoreSeparators(inputLine.substring(index + 1))) && (index < inputLine.length() - 1)) {
                 if (acceptedIndex < baseArrLength) {
                     baseArray[++acceptedIndex] = inputLine.substring(index + 1);
                 } else {
                     return null;
                 }
                 break;
             }
         }
         returnedArr.add(baseArray);
         returnedArr.addAll(tempGroupArray);
         return returnedArr;
     }
 }
