 package data_minining_algorithms;
 
 
 import java.io.FileReader;
 import java.io.IOException;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 public class Window {
 
 	private static String [][] data; // input dataset
 	private double [][] window; // output
 	private int dataX; //number of columns in dataset
 	private int dataY; // number of records within the dataset
 	private int windowX; // number of columns in window output
 	private int windowY; // number of records within the window
 	private int column; // column number used from dataset
 	private String datasetfile;
 	
 	//housekeeping variables
 	private double [] temp;
 	private static String [] nextRecord;
 	
 	Window(int dataX, int dataY, String datasetfile,int column, int windowX, int windowY) throws IOException{
 		
 
 		this.dataX = dataX;
 		this.dataY = dataY;
 		this.datasetfile = datasetfile;
 		this.column = column;
 		this.windowX = windowX;
 		this.windowY = windowY;
 		
 		data = new String [dataY][dataX];
 		nextRecord = new String[dataX];
 		temp = new double [dataY];
 		window = new double [windowY][windowX];
 		readData();
 		createWindow();
 	
 	}
 	
 	public void readData() throws IOException{
 		//Read the CSV data into a Matrix
 		CSVReader reader = new CSVReader(new FileReader(datasetfile));
 		int m =0;
 		while ((nextRecord = (reader.readNext())) != null) {
 		
 		for(int k=0;k<dataX;k++){
 			data[m][k]=nextRecord[k];
 		}
 			m++; 
 		}
 	}
 	
 	
 	public double [][] createWindow(){
 		
 		//From the matrix put the selected column into a 2d array
 		int track = 0;
		for (int i = 1; i < dataY-1; i ++) {
 			temp[track] = Double.parseDouble(((data[i][column])));
 			//System.out.print(", ");	
 			track++;
 		}
 		
 		//Create the window from the 2D array above.
 		int x=0;
 		int y=0;
	
		for(int j = 0; j < temp.length; j++) {
 		
 			window[y][x] = temp[j];
 			x++;
 				if (x == windowX) {
					
 					y++;
 					x=0;
 					j = j - (windowX-1);
 				}
 			}
 		return window;
 	}
 	
 	 public void print(){	
 		
 		for(int j = 0 ; j < windowY; j++){
 			for (int i = 0; i < windowX; i++){
 				System.out.print(window[j][i]);
 				System.out.print(", ");
 			}
 			System.out.println();
 		}
 	}
 	 
 	 public double get(int row, int column) {
 		 
 		 return window[row][column];
 	 }
 	 
 	 public void set(int row , int column, double input) {
 		 
 		 window[row][column] = input ;
 	 }
 	 
 	 public int getWindowX() {
 		 
 		 return windowX;
 	 }
 	 
 	 public int getWindowY() {
 		 
 		 return windowY;
 	 }
 	 
 	 public static void main(String [] args) throws IOException{
 		 
 		//Testing of Window class
 		//Window London = new Window(7,845,"Year3_Project/Data/Request_analysis_daily.csv",3,5,842);
 		//Window area = new Window(7,845,"Year3_Project/Data/Request_analysis_daily.csv",4,5,842);
 		//Window crown = new Window(7,845,"Year3_Project/Data/Request_analysis_daily.csv",5,5,842);
		Window total = new Window(7,845,"Year3_Project/Data/Request_analysis_daily.csv",4,5,842);
 		//London.print();
 		//area.print();
 		//crown.print();
 		//total.print();
 		//Window(int dataX, int dataY, String datasetfile,int column, int windowX, int windowY)
 		//Window LondonWeek = new Window(6,169,"Year3_Project/Data/Request_analysis_weekly.csv",1,5,166);
 		//LondonWeek.print();
		 Window londonMonth = new Window(6,41,"Year3_Project/Data/Request_analysis_monthly.csv",5,5,40);
 		 londonMonth.print();
 		 
 		
 		
 		}
 }
 
 
