 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.message_retrieval;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author user
  */
 public class MainQuery {
 
     public static HashMap<String, DocMappingModel> docMapping = new HashMap<>();
     public static ArrayList<HashMap<String, String>> termMapping = new ArrayList<>();
     public static HashMap<String, Integer> terms = new HashMap<>();
     public static double[] avgDocLength = new double[6];
     public static String path;
     public static HashMap<String, HashMap<String, Integer>> allPostList = new HashMap<>();
     public static HashSet<String> allDocID = new HashSet<>();
     public static final String codeName = "irfan_elisafina_pandapotan";
     public final static String NEWLINE = "\r\n";
     public static long start = 0;
     public static long end = 0;
     public static String com = "";
     public static boolean isCompress = false;
     
 
     public static void main(String[] args) {
 
         isCompress = false;
         path = "/Users/hadipratama/Documents/Indexing/";
         if (isCompress) {
             com = "com_";
         }
         
         //path = "/Users/hadipratama/Documents/Indexing/";
 
         // System.exit(0);
 
 
 //        path = args[0];
 //        term = args[1];
 //        
 //        if(args.length == 3){
 //            field = Integer.valueOf(args[2]);
 //        }
 
         //path = "C:\\Users\\user\\Documents\\NetBeansProjects\\message-retrieval\\";
         //path = "/Users/hadipratama/Documents/Indexing/";
 
         docMapping = QueryController.getDocMapping();
         termMapping = QueryController.getTermMapping();
         
         start = System.currentTimeMillis();
        String input = "account number \"your password\"";
         String fileName = codeName + "-" + input + ".txt";
        String query = "account number \"your password\"";
         query = Parser.parseQuery(query);
         terms = QueryController.queryNormalization(query);
         System.out.println("Processing query : " + terms);
         LinkedHashMap<String, Double> weight = QueryController.getWeight(terms, docMapping, avgDocLength, 6);
         try {
             QueryController.printWeight(input, weight, com + fileName.replaceAll("\"", ""));
             //System.out.println(weight);
 
             //        System.out.println(docMapping.size());
             //        for (int i = 0; i < avgDocLength.length; i++) {
             //            System.out.println(avgDocLength[i]);
             //            
             //        }
             //        HashMap<String, Integer> terms = QueryController.queryNormalization("saya \"adalah seorang\" anak gembala saya \"selalu riang\" serta gembira");
             //        System.out.println(terms);
             //        
             //        HashMap<String, ArrayList<Integer>> test = new HashMap<>();
             //        try {
             //            test = QueryController.getPostingList("goes", 5);
             //        } catch (FileNotFoundException ex) {
             //            Logger.getLogger(MainQuery.class.getName()).log(Level.SEVERE, null, ex);
             //        } catch (IOException ex) {
             //            Logger.getLogger(MainQuery.class.getName()).log(Level.SEVERE, null, ex);
             //        System.out.println(test);
             //        System.out.println(test);
 
 
 
             //                    QueryController.getPostingListBig("goes", allPostList, allDocID, 5);
             //                    QueryController.getPostingListBig("golden", allPostList, allDocID, 5);
             //                    QueryController.getPostingListBig("golly", allPostList, allDocID, 5);
 
             //                    QueryController.getPostingListBigSequence("let|me|know", allPostList, allDocID, 5);
             //                    //System.out.println(allDocID);
             //                    System.out.println(allPostList);
 
             //        try {
             //            //        for (int i = 1; i < 6; i++) {
             //            //            System.out.println(docMapping.get("2").docLength[i-1]);
             //            //            System.out.println(QueryController.fieldLengthAcc("2", i));
             //            //            System.out.println("================");
             //            //        }
             //                    
             //                   // System.out.println(QueryController.getPostingListAll("editor"));
             //        } catch (FileNotFoundException ex) {
             //            Logger.getLogger(MainQuery.class.getName()).log(Level.SEVERE, null, ex);
             //        } catch (IOException ex) {
             //            Logger.getLogger(MainQuery.class.getName()).log(Level.SEVERE, null, ex);
             //        }
         } catch (IOException ex) {
             Logger.getLogger(MainQuery.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
