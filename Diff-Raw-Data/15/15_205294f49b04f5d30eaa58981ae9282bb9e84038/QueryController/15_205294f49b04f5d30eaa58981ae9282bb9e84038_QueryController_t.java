 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.message_retrieval;
 
 import com.indexing.controller.IndexCompression2;
 import com.query.controller.QueryProcessor;
 import indexing.Indexing;
 import java.io.*;
 import java.util.Map.Entry;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author user
  */
 public class QueryController {
 
     final static double k1 = 1.2;
     final static double b = 0.75;
     final static double k2 = 100;
 
     public static HashMap<String, DocMappingModel> getDocMapping() {
         //hashmap <docID, DocMappingModel (messID, docLength)
         //avgDocLength itu rata2 panjang document buat semua field+all.. array by reference
         //
         long[] tempLength = {0, 0, 0, 0, 0, 0};
         String path = MainQuery.path;
         String termMappingFileName = path + QueryProcessor.DOC_MAPPING;
         HashMap<String, DocMappingModel> temp = new HashMap<>();
 
         try {
             FileInputStream fstream = new FileInputStream(termMappingFileName);
             try (DataInputStream in = new DataInputStream(fstream)) {
                 BufferedReader br = new BufferedReader(new InputStreamReader(in));
                 String strLine;
                 String[] a;
                 while ((strLine = br.readLine()) != null) {
                     a = strLine.replaceAll("=", "\\|").split("\\|");
                     String[] b = a[2].split(",");
                     int[] c = new int[b.length];
                     for (int i = 0; i < b.length; i++) {
                         c[i] = Integer.valueOf(b[i]);
                         tempLength[i] += c[i];
                     }
                     temp.put(a[0], new DocMappingModel(a[1], c));
                 }
             }
         } catch (Exception e) {//Catch exception if any
             System.err.println("Error: " + e.getMessage());
         }
         for (int i = 0; i < tempLength.length; i++) {
             MainQuery.avgDocLength[i] = tempLength[i] * 1.00 / temp.size();
 
         }
         return temp;
     }
 
     public static HashMap<String, Integer> queryNormalization(String query) {
         // agus dan "Budi bermain" "bola Kaki" --> budi bermain bola|kaki
         // si | buat tanda klo pke kutip
         HashMap<String, Integer> res = new HashMap<String, Integer>();
         String[] yy = query.split("\\s");
         for (int i = 0; i < yy.length; i++) {
             String temp = "";
             if (yy[i].startsWith("\"")) {
                 for (int j = i; j < yy.length; j++) {
                     temp += yy[j] + "|";
                     if (!yy[j].endsWith("\"")) {
                         i++;
                     } else {
                         break;
                     }
                 }
                 //res.add(temp.replaceAll("\"", ""));
                 putToHashMap(temp.replaceAll("\"", ""), res);
             } else {
                 //res.add(yy[i]);
                 putToHashMap(yy[i], res);
             }
         }
         return res;
     }
 
     public static void putToHashMap(String key, HashMap<String, Integer> map) {
 
         Integer freq = (Integer) map.get(key);
         if (freq == null) {
             freq = new Integer(1);
         } else {
             int value = freq.intValue();
             freq = new Integer(value + 1);
         }
         map.put(key, freq);
     }
 
     public static int fieldLengthAcc(String docID, int field) {
         int hasil = 0;
         DocMappingModel doc = MainQuery.docMapping.get(docID);
         for (int i = 0; i < field - 1; i++) {
             hasil += doc.docLength[i];
         }
         return hasil;
     }
 
     /**
      * Fungsi untuk menyimpan document mapping kedalam hashmap
      *
      * @param path
      * @throws IOException
      */
     public static HashMap<String, String> dumpTermMapping(String path) throws IOException {
         HashMap<String, String> res = new HashMap<>();
         try {
             FileInputStream fstream = new FileInputStream(path);
             DataInputStream in = new DataInputStream(fstream);
             BufferedReader br = new BufferedReader(new InputStreamReader(in));
             String strLine;
             String[] a;
             while ((strLine = br.readLine()) != null) {
                 a = strLine.split("=");
                 res.put(a[0], a[1]);
             }
             in.close();
         } catch (Exception e) {//Catch exception if any
             System.err.println("Error: " + e.getMessage());
         }
 
         return res;
     }
 
     public static ArrayList<HashMap<String, String>> getTermMapping() {
         ArrayList<HashMap<String, String>> hasil = new ArrayList<>();
         String field = "";
         for (int i = 1; i < 6; i++) {
 
             switch (i) {
                 case 1:
                     field = "date";
                     break;
                 case 2:
                     field = "to";
                     break;
                 case 3:
                     field = "from";
                     break;
                 case 4:
                     field = "subject";
                     break;
                 case 5:
                     field = "body";
                     break;
                 default:
                     field = "body";
                     break;
             }
 
 
             String path = MainQuery.path;
             // String indexFileName = path +MainQuery.com+ QueryProcessor.PREFIX_INDEX_FILENAME + field + ".txt";
             String termMappingFileName = path + MainQuery.com + QueryProcessor.PREFIX_TERM_MAPPING_FILENAME + field + ".txt";
             try {
                 HashMap<String, String> termTemp = dumpTermMapping(termMappingFileName);
                 hasil.add(termTemp);
             } catch (IOException ex) {
                 Logger.getLogger(QueryController.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         return hasil;
     }
 
     public static HashMap<String, ArrayList<Integer>> getPostingList(String term, int fieldCode) throws FileNotFoundException, IOException {
         // dari 1 term ambil hashmap <docID, arrayList of position>
         // klo fieldnya all, klo ada doc yg sama posisinya disatuin terus di sort
         // field 1=date, 2=to, 3=from, 4=subject, 5=body, 6=all
 
         String field;
         HashMap<String, ArrayList<Integer>> temp = new HashMap<>();
         if (fieldCode == 6) {
             temp = getPostingListAll(term);
         } else {
             switch (fieldCode) {
                 case 1:
                     field = "date";
                     break;
                 case 2:
                     field = "to";
                     break;
                 case 3:
                     field = "from";
                     break;
                 case 4:
                     field = "subject";
                     break;
                 case 5:
                     field = "body";
                     break;
                 default:
                     field = "body";
                     break;
             }
 
 
             String path = MainQuery.path;
             String indexFileName = path + MainQuery.com + QueryProcessor.PREFIX_INDEX_FILENAME + field + ".txt";
 //            String termMappingFileName = path + QueryProcessor.PREFIX_TERM_MAPPING_FILENAME + field + ".txt";
 //            
 //            HashMap<String, String> termTemp = dumpTermMapping(termMappingFileName);
             HashMap<String, String> termTemp = MainQuery.termMapping.get(fieldCode - 1);
             String strFromHashMap = termTemp.get(term);
             ArrayList<Object> position = new ArrayList<>();
 
             if (strFromHashMap != null) {
                String[] poss = strFromHashMap.split("\\|");
                position.add(Long.parseLong(poss[1]));
                position.add(Integer.valueOf(poss[2]));
                 position.add(strFromHashMap.split("=")[0]);
 
                 RandomAccessFile indexFile = new RandomAccessFile(indexFileName, "r");
                 indexFile.seek((Long) position.get(0));
                 byte[] buffer = new byte[(int) position.get(1) - Indexing.NEWLINE.getBytes().length];
                 indexFile.read(buffer);
                 String str = new String(buffer);
                 //System.out.println(str);
                 String content = str.split("=")[1];
                 if (MainQuery.isCompress) {
                     String tests[] = content.split(";");
                     String posID[] = tests[1].split(":");
                     ArrayList<Integer> docID = IndexCompression2.StringToVByte(tests[0]);
                     StringBuilder tempss = new StringBuilder("");
                     for (int i = 0; i < docID.size(); i++) {
                         tempss.append(docID.get(i) + ":");
                         //tempss += docID.get(i) + ":";
                         ArrayList<Integer> posIDs = IndexCompression2.StringToVByte(posID[i]);
                         StringBuilder temp2 = new StringBuilder("");
                         for (int j = 0; j < posIDs.size(); j++) {
                             //temp2 += posIDs.get(j) + ",";
                             temp2.append(posIDs.get(j) + ",");
                         }
                         String temp3 = temp2.toString();
                         temp3 = temp3.toString().substring(0, temp3.length() - 1);
                         //tempss += temp2 + ";";
                         tempss.append(temp2 + ";");
 
                     }
                     content = tempss.toString();
                     //  System.out.println(content);
                 }
                 String[] msgs = content.split(";");
 
                 for (String docs : msgs) {
                     String[] pos = docs.split(":");
                     String docID = pos[0];
                     String[] posisi = pos[1].split(",");
                     ArrayList<Integer> tempPos = new ArrayList<>();
                     for (String posTerm : posisi) {
                         tempPos.add(Integer.valueOf(posTerm));
                     }
                     temp.put(docID, tempPos);
                 }
             }
         }
         return temp;
     }
 
     public static HashMap<String, ArrayList<Integer>> getPostingListAll(String term) throws FileNotFoundException, IOException {
         HashMap<String, ArrayList<Integer>> hasil = new HashMap<>();
         ArrayList<HashMap<String, ArrayList<Integer>>> temp = new ArrayList<>();
 
         for (int i = 1; i < 6; i++) {
             temp.add(getPostingList(term, i));
         }
 
         for (int i = 0; i < temp.size(); i++) {
 
             HashMap<String, ArrayList<Integer>> pos = temp.get(i);
             if (pos != null) {
                 //System.out.println(pos);
                 Iterator<Entry<String, ArrayList<Integer>>> itr = pos.entrySet().iterator();
                 while (itr.hasNext()) {
                     Entry<String, ArrayList<Integer>> entry = itr.next();
                     String docID = entry.getKey();
                     ArrayList<Integer> docPos = entry.getValue();
                     ArrayList<Integer> docPosOld = hasil.get(docID);
                     if (docPosOld == null) {
                         docPosOld = new ArrayList<>();
                     }
                     for (int j = 0; j < docPos.size(); j++) {
                         Integer posID = docPos.get(j);
                         posID += fieldLengthAcc(docID, i + 1);
                         docPosOld.add(posID);
                     }
                     hasil.put(docID, docPosOld);
                 }
             }
         }
 
         return hasil;
     }
 
     public static void getPostingListBig(String term, HashMap<String, HashMap<String, Integer>> allPostList,
             HashSet<String> allDocID, int field) {
         //untuk term standar misalkan budi doank
         //nanti panggil c get posting list
         //terus hasilnya dimasukin ke all posting list, tp isinya docID, sama termfreq nya lngs
         // terus semua docID yg ada dimasukin ke allDocID.. krn hashSet tar dia lngs nimpa..
         //System.out.println("masuk");
         HashMap<String, Integer> postListFreq = new HashMap<>();
         HashMap<String, ArrayList<Integer>> postList = null;
         try {
             postList = getPostingList(term, field);
         } catch (FileNotFoundException ex) {
             Logger.getLogger(QueryController.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(QueryController.class.getName()).log(Level.SEVERE, null, ex);
         }
         if (postList != null) {
             Iterator<Entry<String, ArrayList<Integer>>> itr = postList.entrySet().iterator();
             while (itr.hasNext()) {
                 Entry<String, ArrayList<Integer>> entry = itr.next();
                 String docID = entry.getKey();
                 ArrayList<Integer> docPos = entry.getValue();
                 allDocID.add(docID);
                 postListFreq.put(docID, docPos.size());
             }
             allPostList.put(term, postListFreq);
         }
         //System.out.println(allDocID);
 
     }
 
     public static void getPostingListBigSequence(String term, HashMap<String, HashMap<String, Integer>> allPostList,
             HashSet<String> allDocID, int field) {
         //untuk term sequence bola|kaki
         //nanti panggil c get posting list
         //dicek dulu docID mana yg posisinya ada yg sebelahan, klo yg ga ada buang c docID nya
         //terus hasilnya dimasukin ke all posting list, tp isinya docID, sama termfreq nya lngs
         // terus semua docID yg ada dimasukin ke allDocID.. krn hashSet tar dia lngs nimpa..
         HashMap<String, Integer> hasil = new HashMap<>();
         HashSet<String> docIDS = new HashSet<>();
         ArrayList<HashMap<String, ArrayList<Integer>>> postList = new ArrayList<>();
         String terms[] = term.split("\\|");
         for (String string : terms) {
             try {
                 //System.out.println(string);
                 HashMap<String, ArrayList<Integer>> temp = getPostingList(string, field);
                 //System.out.println(temp);
                 Set<String> doc = temp.keySet();
                 docIDS.addAll(doc);
                 postList.add(temp);
             } catch (FileNotFoundException ex) {
                 Logger.getLogger(QueryController.class.getName()).log(Level.SEVERE, null, ex);
             } catch (IOException ex) {
                 Logger.getLogger(QueryController.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
         Iterator iter = docIDS.iterator();
         while (iter.hasNext()) {
             String docID = (String) iter.next();
             boolean allHaveDoc = true;
             int counter = 0;
             for (int i = 0; i < postList.size(); i++) {
                 HashMap<String, ArrayList<Integer>> postID = postList.get(i);
                 if (!postID.containsKey(docID)) {
                     allHaveDoc = false;
                 }
             }
             if (allHaveDoc) {
                 ArrayList<Integer> root = postList.get(0).get(docID);
 
                 for (int i = 0; i < root.size(); i++) {
                     int posRoot = root.get(i);
                     boolean isSequence = true;
                     for (int j = 1; j < postList.size(); j++) {
                         ArrayList<Integer> check = postList.get(j).get(docID);
                         if (!check.contains(posRoot + j)) {
                             isSequence = false;
                         }
                     }
                     if (isSequence) {
                         counter++;
                     }
                 }
                 if (counter != 0) {
                     allDocID.add(docID);
                     hasil.put(docID, counter);
                 }
             }
         }
         allPostList.put(term, hasil);
     }
 
     public static LinkedHashMap<String, Double> getWeight(HashMap<String, Integer> query,
             HashMap<String, DocMappingModel> docMapping,
             double[] avgDocLength, int field) {
         HashMap<String, Double> hasils = new HashMap<>();
         HashMap<String, HashMap<String, Integer>> allPostList = new HashMap<>();
         HashSet<String> allDocID = new HashSet<>();
         Iterator<Entry<String, Integer>> itr = query.entrySet().iterator();
         while (itr.hasNext()) {
             Entry<String, Integer> entry = itr.next();
             String term = entry.getKey();
             //System.out.println(term);
             if (term.indexOf("|") != -1) {
                 //System.out.println(term);
                 getPostingListBigSequence(term, allPostList, allDocID, field);
             } else {
                 getPostingListBig(term, allPostList, allDocID, field);
             }
         }
         //System.out.println(allDocID.size());
 
         hasils = calculateRank(query, allPostList, allDocID, docMapping, avgDocLength, field);
         return sortByValue(hasils);
     }
 
     public static HashMap<String, Double> calculateRank(HashMap<String, Integer> query, HashMap<String, HashMap<String, Integer>> allPostList,
             HashSet<String> allDocID, HashMap<String, DocMappingModel> docMapping,
             double[] avgDocLength, int field) {
         HashMap<String, Double> hasil = new HashMap<>();
 
         int totalDoc = docMapping.size();
         Iterator it = allDocID.iterator();
 
         while (it.hasNext()) {
             double BM25Score = 0;
             String docID = (String) it.next();
             DocMappingModel docMod = docMapping.get(docID);
             int docLength = (int) docMod.getDocLength()[field - 1];
             double avgDoc = avgDocLength[field - 1];
 
             Iterator<Entry<String, HashMap<String, Integer>>> itr = allPostList.entrySet().iterator();
             while (itr.hasNext()) {
                 Entry<String, HashMap<String, Integer>> entry = itr.next();
                 String term = entry.getKey();
                 HashMap<String, Integer> postList = entry.getValue();
                 int qf = query.get(term);
                 int docFreq = postList.size();
                 int termFreq = 0;
                 if (postList.get(docID) != null) {
                     termFreq = postList.get(docID);
                 }
                 BM25Score += BM25Calculator(docLength, avgDoc, docFreq, termFreq, totalDoc, qf);
             }
 
             hasil.put(docMod.messID, BM25Score);
         }
 
 
         //System.out.println(hasil);
         //ngitung pke allpostList sama alldoc, pke algo BM25
         return hasil;
     }
 
     public static double BM25Calculator(int docLength, double avgDocLength, int docFreq,
             int termFreq, int totalDoc, int queryFreq) {
         double hasil = 0.0;
         double persen = docLength * 1.0 / avgDocLength;
         //System.out.println(persen);
         double K = k1 * ((1 - b) + (b * persen));
 
         double a1 = Math.log((0.5 / 0.5) / ((docFreq + 0.5) / (totalDoc - docFreq + 0.5)));
         //System.out.println(a1);
         double a2 = ((k1 + 1) * termFreq) / (K + termFreq);
         //System.out.println(a2);
         double a3 = ((k2 + 1) * queryFreq) / (k2 + queryFreq);
         //System.out.println(a3);
         hasil = a1 * a2 * a3;
 
         return hasil;
     }
 
     public static LinkedHashMap sortByValue(Map map) {
         List list = new LinkedList(map.entrySet());
         Collections.sort(list, new Comparator() {
 
             public int compare(Object o1, Object o2) {
                 return ((Comparable) ((Map.Entry) (o2)).getValue()).compareTo(((Map.Entry) (o1)).getValue());
             }
         });
 
         LinkedHashMap result = new LinkedHashMap();
         for (Iterator it = list.iterator(); it.hasNext();) {
             Map.Entry entry = (Map.Entry) it.next();
             result.put(entry.getKey(), entry.getValue());
         }
         return result;
     }
 
     public static void printWeight(String query, LinkedHashMap<String, Double> weight, String fileName) throws IOException {
         BufferedWriter weightFile = new BufferedWriter(new FileWriter(fileName));
         Iterator<Entry<String, Double>> itr = weight.entrySet().iterator();
         int rank = 0;
         MainQuery.end = System.currentTimeMillis();
         long time = MainQuery.end - MainQuery.start;
         //System.out.println("time:" + (end - start) * 1.00 / 1000);
         weightFile.write(query + MainQuery.NEWLINE);
         while (itr.hasNext() && rank < 40) {
             try {
                 rank++;
                 Entry<String, Double> entry = itr.next();
                 String docID = entry.getKey();
                 Double bm = entry.getValue();
                 //indexMapping.seek(indexMapping.length());
                 String toWrite = docID + " " + rank + " " + bm + MainQuery.NEWLINE;
                 weightFile.write(toWrite);
             } catch (IOException ex) {
                 ex.printStackTrace();
             }
         }
         weightFile.write("Execution time : " + time + " miliseconds");
         weightFile.close();
     }
 
     public static void main(String[] args) {
 
         double k = BM25Calculator(90, 100, 300, 25, 500000, 1);
         //System.out.println(k);
     }
 }
