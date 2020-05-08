 import java.util.*;
 import java.io.*;
 
 public class Driver{
 
     public static void main(String[] args) throws FileNotFoundException{
         int threshold = 5; //threshold for showing frequency counts, default is 5
         int n = 4;//the number that represents how big the phrases should be, default is 4 
         String directory = "med_doc_set"; //directory to look in for essays to analyze, default is med_doc_set
         String temp; //for precessing user input from keyboard
         Scanner k = new Scanner(System.in); //scanner for keyboard input
 
        System.out.println("What directory should be analyzed? (Default is " + directory + ")");
         temp = k.nextLine(); 
         if(temp.length()!=0){
             Scanner s = new Scanner(temp);
             directory = s.next(); 
         }
         System.out.println("How many words in a phrase? (Default is " + n + ")");
         temp = k.nextLine(); 
         if(temp.length()!=0){
             Scanner s = new Scanner(temp);
             n = s.nextInt(); 
         }
         System.out.println("What threshold should be used for frequency counts? (Default is " + threshold + ")");
         temp = k.nextLine(); 
         if(temp.length()!=0){
             Scanner s = new Scanner(temp);
             threshold = s.nextInt(); 
         }
 
 
         ArrayList<ArrayList<String>> bigList = new ArrayList<ArrayList<String>>(); //an array list of array lists, each individual array list representing a document
        //File dir = new File("./test");//directory of documents
         File dir = new File("./"+directory);//directory of documents
         String[] allFiles = dir.list();//array of file names in the directory 
 
         for(String filename : allFiles){//creates an array list for each document and adds it to the bigList 
             if(!filename.equals(".DS_Store")){ 
                //ArrayList<String> list = sequenceList(new File("./test/"+filename), 6); 
                 ArrayList<String> list = sequenceList(new File("./"+directory+"/"+filename), filename, n); 
                 bigList.add(list); 
             }
         }
 
         System.out.println("Threshold: "+threshold); 
         System.out.println("Number of words in a phrase: "+n); 
         System.out.println("Directory: "+directory); 
         for(HitCount h : hitCountList(bigList, threshold)){
             System.out.println("["+h.getFile1() + ", " + h.getFile2() + "]" + " -> " + h.getHits()); 
         }
 
     }
 
 
     public static ArrayList<HitCount> hitCountList(ArrayList<ArrayList<String>> bigList, int threshold){
         ArrayList<ArrayList<String>> bigList2 = bigList; //a copy of bigList
         ArrayList<HitCount> hitCountList = new ArrayList<HitCount>();//an array list with the hits and file names of compared files 
         int hits = 0; 
         
         for(ArrayList lst : bigList){
             for(ArrayList lst2 : bigList2){
                 if(bigList.indexOf(lst)<bigList2.indexOf(lst2)){//to make sure each pair of lists is compared only once
                     for(Object o : lst){
                         for(Object o2 : lst2){
                             if(o.equals(o2)) hits++;//if the two phrases are equal increment hits
                         }
                     }
                     if(hits>=threshold){//if hits is greater or equal to the threshold add it to the hitCountList
                         HitCount temp = new HitCount(lst.get(0), lst2.get(0), hits);
                         hitCountList.add(temp); 
                     }
                     hits=0; 
                 }
             }
         }
         Collections.sort(hitCountList); //sort hitCountList in order of frequency
         return hitCountList; 
     }
 
 
     public static ArrayList<String> sequenceList(File f, String filename, int n) throws FileNotFoundException{
         //given an int n and file f, this method returns an ArrayList of n-word phrases found in the file
         ArrayList<String> list = new ArrayList<String>(); //array list to hold all n-word phrases found in document
         ArrayList<Integer> len = new ArrayList<Integer>(); //array list to hold sizes of all n-word phrases
         String temp = ""; //string to hold an individual n-word phrase
         String nxt = ""; //string to hold next word
         int cnt = 0; //count used to create n-word phrases
         int i = 0; 
         list.add(filename); 
        Scanner s = new Scanner(f);//scanner pointed at document
         while(s.hasNext()){
             if(cnt<n){//while cnt is less than n keep adding words to the phrase
                 nxt = s.next().replaceAll("[\\p{Punct}]",""); 
                 if(nxt.length()!=0){
                     temp = temp + " " + nxt; 
                     len.add(nxt.length()); 
                     cnt++;
                 }
             }
             else{ 
                 if(cnt==n){//if cnt is equal to n, add the phrase, the first n-word phrase, to list 
                     list.add(temp.trim());
                 }
                 nxt = s.next().replaceAll("[\\p{Punct}]",""); 
                 if(nxt.length()!=0){ 
                     temp = temp + " " + nxt;//add the next word to the end of of the phrase 
                     temp = temp.substring(len.get(cnt-n)+1).trim(); //get rid of the first word in phrase
                     list.add(temp);
                     len.add(nxt.length()); 
                     cnt++;
                 }
             }
         }
         return list; //return the list of phrases
 
     }
 }
 
 
