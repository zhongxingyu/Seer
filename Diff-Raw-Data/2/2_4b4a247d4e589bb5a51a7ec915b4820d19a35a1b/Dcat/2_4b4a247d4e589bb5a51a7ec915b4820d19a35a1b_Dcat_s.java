 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dcat;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Scanner;
 
 
 /**
  * This computes an upper bound for the discrete LS category of a graph.  This
  * algorithm takes a random vertex and adds all incident vertices in the 
  * superstructure, thus creating a "star". It then repeats this process for all 
  * added vertices, adding all incident vertices such that a cycle is not formed.
  * It repeats this process for every added, and then stores the resultant vertex 
  * in the Cover. It then repeats starting with an edge that is not in the first 
  * tree.  Etc.
  * @author Brian Green
  */
 public class Dcat {
      public static Complex getComplex(){
         System.out.println("Get complex from file?");
         Scanner in = new Scanner(System.in);
         if(in.nextLine().equalsIgnoreCase("y")){
             return getComplexFromFile();
         }
         else{
             System.out.println("Generate complete complex?");
             if(in.nextLine().equalsIgnoreCase("y"))
                 return generateComplete();
             else
                 return getComplexFromUser();
         }
     }
     /**
      * This function queries the user for the complex.
      * 
      * @return the input complex
      */
     public static Complex getComplexFromUser(){
         Scanner in = new Scanner(System.in);
         String message = "Enter a (space separated) simplex or Q to calculate";
         System.out.println(message);
         String simplex= in.nextLine();
         Complex complex = new Complex();
         while(!simplex.equals("Q")){
             complex.addSimplexWithDescription(simplex);
             System.out.println(message);
             simplex= in.nextLine();
         }
         return complex;
     }
     
    /**
     * Reads in a list of simplices from a user provided file and returns the 
     * generated complex.
     * Runs in linear time 
     * @return Complex
     */
    public static Complex getComplexFromFile(){
         Complex c = new Complex();
         String s;
         BufferedReader r=null;
         while(true){
         try{
             r = getFileReader();
         
             while((s=r.readLine())!=null){
                 c.addSimplexWithDescription(s);
             }
             return c;
         }
         catch(IOException e){
          System.out.println("Error trying to read file \n"+e.getMessage());
         }
         }
     }
    
    /**
     * Generates a complete complex with user defined specifications.
     * @return Complex
     */
    public static Complex generateComplete(){
        System.out.println("How many vertices?");
        int n = getInt();
        System.out.println("Max dimension?");
        int k = getInt();
        Complex c = new Complex();
        int size = (int) Math.pow(2, n);
        String binRep;
        String temp;
        for(int i=1; i<size; i++){
            binRep=Integer.toBinaryString(i);
            if(dimension(binRep)<=k){
             while(binRep.length()<n){
                     binRep="0"+binRep;
                 }
             temp = new String();
             for(int j=0; j<n; ++j){
                 if(binRep.charAt(j)=='1')
                     temp=temp+j+" ";
             }
             c.addSimplex(new Simplex(temp));  
         }
        }
        c.sort();
        return c;
    }
     
    private static int dimension(String s){
        int count=0;
        for(int i=0; i<s.length();i++){
            if(s.charAt(i)=='1'){
                count++;
            }
        }
        return count;
    }
    
    public static int getInt(){
        Scanner in = new Scanner(System.in);
        int n;
        while(true){
            try{
             return Integer.parseInt(in.nextLine());
            }
            catch(NumberFormatException e){
                System.out.println("Input must be an integer");
            }
        }
    }
    
    /**
     * Queries the user for a file path, and returns a buffered reader of the file.
     * @return BufferedReader
     */
    public static BufferedReader getFileReader(){
        Scanner in = new Scanner(System.in);
        BufferedReader br=null;
        while(true){
            try{
                System.out.println("Enter the file location");
                br= new BufferedReader(new FileReader(new File(in.nextLine())));
            
                if(br.ready()){
                    return br;
                }
            }
            catch(IOException e){
                System.out.println("Error trying to access file at "+e.getMessage());
            }
        }
        
    }
 
    public static void search(Simplex s, Complex collapse,Complex original){
        int n = s.dimension();//Let n be the dimension of s
        int width=original.numberOfSimplicesInDimension(n+1);//The number of simplices of dimension n+1
        int minIndex = original.getIndexOfFirstSimplexOfDimension(n+1);//Index we start checking
       System.out.println("Searching simplex "+ s.name()+" starting at index "+minIndex+"with width "+width);
        Simplex currentSimplex = null;//Create a place to store the working simplex
        ArrayList<Simplex> candidates;//All possible expansions
        for(int i=0;i<width; i++){//check every candidate, indices between minIndex and minIndex+width
            log("Checking at Index "+(i+minIndex));
            currentSimplex=original.getSimplexAtIndex(i+minIndex);//grab the next possible expansion
            if(currentSimplex.contains(s)&&!collapse.inComplex(s)){//If it contains s, and isn't already there
                candidates=s.getSkeleton(n);//get all of it's n simplices
                int containedElements =0;//create a counter
                for(int j=0; j<candidates.size(); j++){//loop through said n-simplices
                    if(collapse.inComplex(candidates.get(j))){//if collapse has said simplex
                        containedElements++;//increment the counter
                        if(containedElements==n+1){//and if the counter = n+1
                            collapse.addSimplexWithDescription(candidates.get(j).name());//add it to collapse
                        }
                    }
                }
            }
        }
    }
   
    public void determineCategory(Complex complex){
        
    }
    
     /**
      * 
      */
     public static void main(String args[]) {
         
         Complex complex = getComplex();
         Complex check=new Complex();
         Complex collapse;
         ArrayList<Complex> collapses=new ArrayList();
         while(check.getSimplices().size()!=complex.getSimplices().size()){
             collapse=new Complex();
             //do{
                 collapse.addSimplexWithDescription(complex.getRandomUnsearchedSimplexOfDimension(complex.dimension()).name());
             //}
             //while(collapse.getSimplexAtIndex(collapse.getIndexOfFirstSimplexOfDimension(complex.dimension())).isSearched());
             log("Starting new Complex with the seed ");
             //collapse.print();
             //Catch each dimension sequentially going up
             int maxDim=complex.dimension();//Highest dimension to consider
             for(int i=0; i<maxDim; i++){
                 //create a star at each i-simplex
                 while(true){
                     Simplex simplex =collapse.getRandomUnsearchedSimplexOfDimension(i);
                     if(!simplex.name().equals("Q")){
                         search(simplex, collapse,complex);
                     }
                     else break;
                 }
             }
             collapses.add(collapse);
             check.union(collapse);
             //complex.resetSearchedFlag();
         }
         System.out.println("The category of the complex is at most "+(collapses.size()-1));
     }
     public static void log(String s){
         System.out.println(s);
     }
 
 }
