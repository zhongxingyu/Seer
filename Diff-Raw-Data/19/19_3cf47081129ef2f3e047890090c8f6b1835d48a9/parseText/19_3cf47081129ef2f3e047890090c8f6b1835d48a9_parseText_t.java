 import java.io.*;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 public class parseText {
 	
	ArrayList<dataSets> dataSet;
	static int counter = 0;
 	/*public static void main(String... aArgs) throws FileNotFoundException {
 	    parseText parser = new parseText("beamout.txt");
 	    parser.processLines();
 	    print("Done.");
 	  }
 	*/
 	  public parseText(String aFileName){
 	    fFile = new File(aFileName);  
 	  }
 	  
 	  /** Template method that calls {@link #processLine(String)}.  */
 	  public final void processLines() throws FileNotFoundException {
 	    //Note that FileReader is used, not File, since File is not Closeable
 	    Scanner scanner = new Scanner(new FileReader(fFile));
 	    try {
 	      //first use a Scanner to get each line
 	      while ( scanner.hasNextLine() ){
 	        processLine( scanner.nextLine() );
 	      }
 	    }
 	    finally {
 	      scanner.close();
 	    }
 	  }
 	  protected void processLine(String aLine){
 	    //use a second Scanner to parse the content of each line 
 	    Scanner scanner = new Scanner(aLine);
 	    scanner.useDelimiter(",");
	    dataSet = new ArrayList<dataSets>();
 	    if ( scanner.hasNext() ){
 	    	
 	      double sL = scanner.nextDouble();
 	      double sW = scanner.nextDouble();
 	      double pL = scanner.nextDouble();
 	      double pW = scanner.nextDouble();
 	      String cN = scanner.next();
	      dataSet.add(new dataSets(sL, sW, pL, pW, cN));
	      //dataSets dS = (dataSets)dataSet.get(counter);
	      //print("Sepal length is " + dS.sepalLength + ", Sepal Width is " + dS.getsW()+", Petal length is "+dS.getpL()+", Petal width is "+dS.getpW()+", and Class is "+dS.className );
 	      print(counter);
 	      counter++;
 	    }
 	    else {
 	      print("Empty or invalid line. Unable to process.");
 	    }
	    
	    for (int x = 0; x<dataSet.size();x++){
	    	dataSets dS = (dataSets)dataSet.get(x);
		    print("Sepal length is " + dS.sepalLength + ", Sepal Width is " + dS.getsW()+", Petal length is "+dS.getpL()+", Petal width is "+dS.getpW()+", and Class is "+dS.className );
	    }
 	  
 	  }
 	  
 	  private final File fFile;
 	  
 	  private static void print(Object aObject){
 	    System.out.println(String.valueOf(aObject));
 	  }
 	  
 	  private String quote(String aText){
 	    String QUOTE = "'";
 	    return QUOTE + aText + QUOTE;
 	  }
 }
