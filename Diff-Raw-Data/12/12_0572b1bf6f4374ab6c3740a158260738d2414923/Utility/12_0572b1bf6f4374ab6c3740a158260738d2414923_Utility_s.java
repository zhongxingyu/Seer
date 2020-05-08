 /**
  * ********
  * Copyright © 2010-2012 Olanto Foundation Geneva
  *
  * This file is part of myCAT.
  *
  * myCAT is free software: you can redistribute it and/or modify it under the
  * terms of the GNU Affero General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  *
  * myCAT is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with myCAT. If not, see <http://www.gnu.org/licenses/>.
  *
  *********
  */
 package org.olanto.TranslationText.client;
 
 import com.google.gwt.user.client.Window;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 /**
  * Encore des utilitaires ...
  */
 public class Utility {
 
     public Utility() {
     }
 
     public static String processWildCard(String Query) {
         String query = Query.trim();
         String[] words = query.split("\\s+");
         if (words.length > 1) {
             GuiConstant.MULTI_WILD_CARD_FLG = true;
             query = filterWildCardExpression(words, MainEntryPoint.stopWords);
             if (GuiConstant.DEBUG_ON) {
                 Window.alert("Wild Term : " + query + "\n Before expression " + MainEntryPoint.beforeWildTerm
                         + "\n after expression " + MainEntryPoint.afterWildTerm);
             }
         } else {
             GuiConstant.MULTI_WILD_CARD_FLG = false;
         }
         return query;
     }
 
     public static int getInd(int curpos, int[][] lines) {
         boolean notfound = true;
         int pos = 0;
         while ((notfound) && (pos < lines.length - 1)) {
             if ((curpos >= lines[pos][1]) && (curpos < lines[pos + 1][1])) {
                 notfound = false;
             } else {
                 pos++;
             }
         }
         return pos;
     }
 
     public static int getln(int pos, int lastpos, int[][] lines) {
         int ln = 0;
         for (int i = pos; i < lastpos; i++) {
             ln += lines[i][0];
         }
         return ln;
     }
 
     public static ArrayList<String> getQueryWords(String query, ArrayList<String> stopWords) {
         String Query = query.trim();
         ArrayList<String> hits = new ArrayList<String>();
 
         if (Query.startsWith("QL(")) { // complex query
             hits.add("xxx$$$xxx"); // pas de recherche
             return hits;
         }
         //Sinon requête avec des mots, donc enlever les mots clés et les mots vides
         String[] words = Query.split("\\s+");
         for (int i = 0; i < words.length; i++) {
             if ((!stopWords.contains(words[i].toLowerCase()))
                     && !(words[i].equalsIgnoreCase("NEAR"))
                     && !(words[i].equalsIgnoreCase("AND"))
                     && !(words[i].equalsIgnoreCase("OR"))
                     && !(words[i].equalsIgnoreCase("QUOTATION"))) {
                 hits.add(words[i].toLowerCase().trim());
             }
         }
 //        Window.alert(hits.toString());
         return hits;
     }
 
     public static String browseRequest(String Query) {
         String query = "";
         if (Query.length() <= 1) {
             query = "*";
         } else if (Query.length() > 1) {
             query = Query.substring(1);
         }
         return query;
     }
 
     public static int getIndex(String[] source, String cookie) {
         int i = 0;
         for (int j = 0; j < source.length; j++) {
             if (source[j].equalsIgnoreCase(cookie)) {
                 i = j;
                 break;
             }
         }
         return i;
     }
 
     public static String filterWildCardExpression(String[] words, ArrayList<String> stopWords) {
         String wildTerm = "";
         boolean found = false;
         StringBuilder before = new StringBuilder("");
         StringBuilder after = new StringBuilder("");
         for (int i = 0; i < words.length; i++) {
             if (words[i].contains("*")) {
                 if (!found) {
                     found = true;
                     wildTerm = words[i];
                 } else {
                     Window.alert("Error: Too many ** in one query: SYNTAX : term1 term2 wildcardexp .. term N");
                     return null;
                 }
             } else {
                 if ((!stopWords.contains(words[i].toLowerCase()))
                         && !(words[i].equalsIgnoreCase("NEAR"))
                         && !(words[i].equalsIgnoreCase("AND"))
                         && !(words[i].equalsIgnoreCase("OR"))
                         && !(words[i].equalsIgnoreCase("QUOTATION"))) {
                     if (!found) {
                         before.append(words[i]).append(" ");
                     } else {
                         after.append(words[i]).append(" ");
                     }
                 }
             }
         }
         MainEntryPoint.beforeWildTerm = before.toString().trim();
         MainEntryPoint.afterWildTerm = after.toString().trim();
         return wildTerm;
     }
 
     public static ArrayList<String> getWildCardQueryWords(String[] words, ArrayList<String> stopWords) {
         ArrayList<String> hits = new ArrayList<String>();
         if (GuiConstant.MULTI_WILD_CARD_FLG) {
             for (int i = 0; i < words.length; i++) {
                 hits.add((MainEntryPoint.beforeWildTerm + " " + words[i] + " " + MainEntryPoint.afterWildTerm).trim());
             }
 //            Window.alert(hits.toString());
         } else {
             for (int i = 0; i < words.length; i++) {
                 if ((!stopWords.contains(words[i].toLowerCase()))
                         && !(words[i].equalsIgnoreCase("NEAR"))
                         && !(words[i].equalsIgnoreCase("AND"))
                         && !(words[i].equalsIgnoreCase("OR"))
                         && !(words[i].equalsIgnoreCase("QUOTATION"))) {
                     hits.add(words[i].trim());
                 }
             }
         }
         return hits;
     }
 
     public static ArrayList<String> getRefWords(String refText) {
         refText = refText.toLowerCase();// not case sensitive (might be origin of a problem in MyRef)
         ArrayList<String> hits = new ArrayList<String>();
         String[] words = refText.split("\\s+");
         hits.addAll(Arrays.asList(words));
 //        Window.alert("Hits : "+hits.size());
         return hits;
     }
 
     public static boolean validateExact(String Query) {
         String queryo = Query.trim();
         if ((queryo.length() > 2) && (queryo.startsWith("\"")) && (queryo.endsWith("\""))) {
             queryo = queryo.substring(1, queryo.length() - 2);
             if (queryo.contains("\"")) {
                 Window.alert("Malformed EXACT query\n SYNTAX = \"expression\"");
                 return false;
             }
         } else {
             Window.alert("Malformed EXACT query\n SYNTAX = \"expression\"");
             return false;
         }
         return true;
     }
 
     public static ArrayList<String> getexactWords(String queryo) {
         String Query = queryo.replace("\"", "");
         Query = Query.trim();
         ArrayList<String> hits = new ArrayList<String>();
         hits.add(Query);
         return hits;
     }
 
     public static boolean validateClose(String Query) {
         String queryo = Query.trim();
         if ((queryo.startsWith("\"")) && (queryo.endsWith("\""))) {
             int idx = queryo.indexOf("\" CLOSE \"");
             if (idx < 1) {
                 Window.alert("Error1: Misplaced Keyword. Malformed CLOSE query\n SYNTAX = \"expr1\" CLOSE \"expr2\"");
                 return false;
             }
             String exact1 = queryo.substring(0, idx + 1);
 //            Window.alert(exact1);
             String exact2 = queryo.substring(idx + 8);
 //            Window.alert(exact2);
             if ((!validateExact(exact1)) || (!validateExact(exact2))) {
                 Window.alert("Error 2: Non exact expression. Malformed CLOSE query\n SYNTAX = \"expr1\" CLOSE \"expr2\"");
                 return false;
             }
         } else {
             Window.alert("Error 3: Missing quotes. Malformed CLOSE query\n SYNTAX = \"expr1\" CLOSE \"expr2\"");
             return false;
         }
         return true;
     }
 
     public static ArrayList<String> getexactClose(String queryo) {
         if (validateClose(queryo)) {
             String Query = queryo.replace("\"", "");
             Query = Query.trim();
             ArrayList<String> hits = new ArrayList<String>();
             String[] words = Query.split(" CLOSE ");
             hits.addAll(Arrays.asList(words));
             if (hits.size() == 2) {
                 return hits;
             }
         }
         Window.alert("Error 4: More than Two expressions. Malformed CLOSE query\n SYNTAX = \"expr1\" CLOSE \"expr2\"");
         return null;
     }
 
     public static String ExactCloseQueryBuilder(String queryo, String langS, String langT, ArrayList<String> stopWords, ArrayList<String> collections) {
         String qt = queryo.trim();
         String query;
         query = "QUOTATION(\"" + qt + "\")";
         String seleColl = "";
         if (!collections.isEmpty()) {
             seleColl = "\"COLLECTION." + collections.get(0) + "\"";
             for (int k = 1; k < collections.size(); k++) {
                 seleColl += " ORL \"COLLECTION." + collections.get(k) + "\"";
             }
             seleColl += " ANDL ";
         }
         String IN_Bitext = " IN[" + seleColl + "\"SOURCE." + langS + "\""
                 + " ANDL \"TARGET." + langT + "\"]";
 
         String IN_Monotext = " IN[" + seleColl + "\"SOURCE." + langS + "\"" + "]";
 
         if (GuiConstant.BITEXT_ONLY) {
             return query + IN_Bitext;
         }
         return query + IN_Monotext;
     }
 
     public static String queryParser(String queryo, String langS, String langT, ArrayList<String> stopWords, ArrayList<String> collections) {
         String Query = queryo.trim();
         String query;
         String qt = Query;
         if (qt.contains(" CLOSE ")) {
             Window.alert("Error 5: Missing first quote. Malformed CLOSE query\n SYNTAX = \"expr1\" CLOSE \"expr2\"");
             return "";
         } else {
             if (qt.startsWith("\"")) {
                 if (qt.endsWith("\"")) {
                     if (validateExact(qt)) {
                         query = "QUOTATION(" + qt + ")";
                     } else {
                         query = "";
                     }
                 } else {
                     query = "QUOTATION(" + qt + "\")";
                 }
             } else if (qt.startsWith("#\"")) {
                 if (qt.endsWith("\"")) {
                     query = "QUOTATION(" + qt.substring(1) + ")";
                 } else {
                     query = "QUOTATION(" + qt.substring(1) + "\")";
                 }
             } else if ((qt.contains("QL(")) || (qt.contains("QL ("))) {
                 int l = Query.lastIndexOf(")");
                 int f = Query.indexOf("(") + 1;
                 query = Query.substring(f, l);
             } else if ((qt.contains(" AND ")) || (qt.contains(" OR "))) {
                 String[] words = Query.split("\\s+");
                 String q = "";
                 for (int i = 0; i < words.length; i++) {
                     if ((words[i].equals("AND")) || (words[i].equals("OR"))
                             || (!stopWords.contains(words[i].toLowerCase())) || (words[i].endsWith("\""))) {
                         q += " " + words[i] + " ";
                     }
                 }
                 int k = q.length() - 1;
                 query = q.substring(0, k);
             } else if (qt.contains(" NEAR ")) { // normalement deux arguments
                 String q = "NEAR (";
                 String[] words = Query.split("\\s+");
                 int i = 0;
                 while ((i < words.length)) {
                     if ((!words[i].equalsIgnoreCase("NEAR")) && (!stopWords.contains(words[i].toLowerCase()))) {
                         q += "\"" + words[i] + "\",";
                     }
                     i++;
                 }
                 int k = q.length() - 1;
                 query = q.substring(0, k) + ")";
             } else {
                 String q = "QUOTATION(\"";
                 String[] words = Query.split("\\s+");
                 for (int i = 0; i < words.length; i++) {
                     if (!stopWords.contains(words[i].toLowerCase())) {
                         q += words[i] + " ";
                     }
                 }
                 int k = q.length() - 1;
                 query = q.substring(0, k) + "\")";
             }
             String seleColl = "";
 
             if (!collections.isEmpty()) {
                 seleColl = "\"COLLECTION." + collections.get(0) + "\"";
                 for (int k = 1; k < collections.size(); k++) {
                     seleColl += " ORL \"COLLECTION." + collections.get(k) + "\"";
                 }
                 seleColl += " ANDL ";
             }
             String IN_Bitext = " IN[" + seleColl + "\"SOURCE." + langS + "\""
                     + " ANDL \"TARGET." + langT + "\"]";
             String IN_Monotext = " IN[" + seleColl + "\"SOURCE." + langS + "\"" + "]";
             if (GuiConstant.BITEXT_ONLY) {
                 return query + IN_Bitext;
             }
             return query + IN_Monotext;
         }
     }
 
     public static String[] getCollections(ArrayList<String> collections) {
         String[] selectedColls = new String[collections.size()];
         for (int k = 0; k < collections.size(); k++) {
             selectedColls[k] = "COLLECTION." + collections.get(k);
         }
         return selectedColls;
     }
 
     public static String getDocListElement(String docListItem) {
         String documentName;
         String collectionPath;
 
         int i = docListItem.lastIndexOf("¦") + 1;
         int j = docListItem.length();
         documentName = docListItem.substring(i, j - 4);
 
         docListItem = docListItem.substring(0, i);
 
         i = docListItem.indexOf("¦");
         j = docListItem.length();
         collectionPath = docListItem.substring(0, i) + "/";
 
         ++i;
         if ((i <= j) && (i >= 1)) {
             docListItem = docListItem.substring(i);
         }
 
         i = docListItem.indexOf("¦");
 
         if (i >= 1) {
             collectionPath += docListItem.substring(0, i);
         }
 //        System.out.println(collectionPath);
 
         return documentName + "-" + collectionPath;
     }
 
     public static ArrayList<String> getDocumentlist(String docs, String separator) {
         if (docs.length() > 2) {
             ArrayList<String> hits = new ArrayList<String>();
             String longName, docName, listElem;
             String[] words = docs.split("\\" + separator);
             for (int i = 0; i < words.length; i++) {
                 int lastslash = words[i].lastIndexOf("/") - 2;
                 longName = words[i].substring(lastslash);
                 docName = getDocListElement(longName.substring(3));
                 listElem = docName + "¦]" + "[¦" + longName;
                 hits.add(listElem);
             }
 //        Window.alert("Hits : " + hits.size() + " " + hits.toString());
             return hits;
         } else {
             return null;
         }
     }
 
     public static String wildCharQueryParser(String[] words, String langS, String langT, ArrayList<String> stopWords, ArrayList<String> collections) {
         String query, q = "";
         if (GuiConstant.MULTI_WILD_CARD_FLG) {
             for (int i = 0; i < words.length; i++) {
                 if (!stopWords.contains(words[i].toLowerCase())) {
                     q += "\"" + MainEntryPoint.beforeWildTerm + " " + words[i] + " " + MainEntryPoint.afterWildTerm + "\" OR ";
                 }
             }
         } else {
             for (int i = 0; i < words.length; i++) {
                 if (!stopWords.contains(words[i].toLowerCase())) {
                     q += "\"" + words[i] + "\" OR ";
                 }
             }
         }
         int k = q.length() - 4;
         query = q.substring(0, k);
         String seleColl = "";
         if (!collections.isEmpty()) {
             seleColl = "\"COLLECTION." + collections.get(0) + "\"";
             for (int c = 1; c < collections.size(); c++) {
                 seleColl += " ORL \"COLLECTION." + collections.get(c) + "\"";
             }
             seleColl += " ANDL ";
         }
         String IN = " IN[" + seleColl + "\"SOURCE." + langS + "\""
                 + " ANDL \"TARGET." + langT + "\"]";
 
         return query + IN;
     }
 
     public static ArrayList<String> getAgLang(String agLang) {
         ArrayList<String> agList = new ArrayList<String>();
         String[] words = agLang.split("\\;");
         agList.addAll(Arrays.asList(words));
 //        Window.alert("Agl Langs : "+agList.size());
         return agList;
     }
 
     public static String addSpace(String s) {
         StringBuilder res = new StringBuilder("");
         for (int i = 0; i < s.length(); i++) {
             res.append(s.charAt(i));
            if (s.charAt(i) > 0x0370) {
                res.append(" ");
             }
         }
 //        Window.alert(res.toString());
         return res.toString();
     }
 
     public static void add(String replace, String by) {
         GuiConstant.entryToReplace.put(replace, by);
     }
 
     /**
      * applique tous les remplacements
      *
      * @param w à traiter
      * @return remplacement effectuer
      */
     public static String replaceAll(String w) {
         String[] toberep = getListOfEntry();
         for (int i = 0; i < toberep.length; i++) {
             String rep = GuiConstant.entryToReplace.get(toberep[i]);
             //System.out.println("process: " + toberep[i] + " -> " + rep);
             w = w.replace(toberep[i], rep);
         }
         return w;
     }
 
     public static String replaceAll2(String w) {
         String[] toberep = getListOfEntry();
         StringBuilder res = new StringBuilder(w);
         for (int i = 0; i < toberep.length; i++) {
             String rep = GuiConstant.entryToReplace.get(toberep[i]);
             //System.out.println("process: " + toberep[i] + " -> " + rep);
             repBuilder(res, toberep[i], rep);
         }
         return res.toString();
     }
 
     public static String replace(String w, String from, String to) { // pas si performant ?
         StringBuilder res = new StringBuilder(w);
         repBuilder(res, from, to);
         return res.toString();
     }
 
     public static void repBuilder(StringBuilder builder, String from, String to) {
         int idx = builder.lastIndexOf(from);
         while (idx != -1) {
             builder.replace(idx, idx + from.length(), to);
             idx += to.length();
             idx = builder.lastIndexOf(from, idx);
         }
     }
 
     /**
      * liste des termes à remplacer.
      *
      * @return la liste
      */
     public static String[] getListOfEntry() {
         String[] result = new String[GuiConstant.entryToReplace.size()];
         GuiConstant.entryToReplace.keySet().toArray(result);
         return result;
     }
 }
