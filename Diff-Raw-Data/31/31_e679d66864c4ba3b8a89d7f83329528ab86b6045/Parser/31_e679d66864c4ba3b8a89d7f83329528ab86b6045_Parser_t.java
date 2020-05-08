 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.ArrayList;
 
 
 public class Parser {
 	
 	public int[][] Parse(String[] filePaths)
 	{
 		//assumes file format is: each row is a sample, each column is a snp, except the last, 
 		//which is the disease column, each entry separated by a single space
 		//all files must have the same number of lines
 		int[][] finalData = null;
 		try
 		{
 
 		    ArrayList<int[]> data = new ArrayList<int[]>();
 		    BufferedReader[] brs = new BufferedReader[filePaths.length];
 		    for(int i = 0; i < filePaths.length; i++)
 		    {
 		    	brs[i] = new BufferedReader(new FileReader(filePaths[i]));
 		    }
 		    String[] strLines = new String[filePaths.length];
 		    //Read Files Line By Line
 		    while ((strLines[0] = brs[0].readLine()) != null)   
 		    {
 		    	String[] stringArrs[] = new String[filePaths.length][];
 		    	int rowLength = 0;
 		    	for(int i = 0; i < brs.length; i++)
 		    	{
 		    		if(i !=0)
 		    		{
 		    			strLines[i] = brs[i].readLine();
 		    		}
 		    		stringArrs[i] = strLines[i].split(" ");
 		    		rowLength+=stringArrs[i].length-1; //subtract 1 because the last col is the disease state
 		    	}
 		    	rowLength++; //now add the one col for disease state
 	    		int[] row = new int[rowLength];
	    		int rowIndex = 0;
 	    		for(int i = 0; i < stringArrs.length; i++)
 	    		{
	    			for(int j = 0; j < (stringArrs[i].length-1); j++)
 		    		{
		    			row[rowIndex] = Integer.parseInt(stringArrs[i][j]);
		    			rowIndex++;
 		    		}
 	    		}
 	    		row[row.length-1] = Integer.parseInt(stringArrs[0][stringArrs[0].length-1]); //add the disease state
 	    		data.add(row);
 		    }
 		    //Close the input streams
 		    for(int i = 0; i < filePaths.length; i++)
 		    {
 		    	brs[i].close();
 		    }
 		    //convert data to regular arrays
 		    finalData = (int[][])data.toArray(new int[data.size()][]);
 		}
 		catch (Exception e)
 		{//Catch exception if any
 		      System.err.println("Error: " + e.getMessage());
 		}
 		return finalData;
 	}
 
 }
