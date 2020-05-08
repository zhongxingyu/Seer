 package execute;
 
 import hcl.Cluster;
 import hcl.ClusterAnalysis;
 import hcl.DataPoint;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class BuildCluster {
 
 	/**
 	 * @param args
 	 */
 	
 	private ArrayList<DataPoint> dpoints;
 	private String poiFilePath = "";
 	private String clusterFilePath = "C:\\Users\\WANG Haozhou\\Documents\\myUQ\\expData\\Dasfaa\\";
 	//private String clusterFilePath = "C:\\myUQ\\expData\\Dasfaa\\";
 	private int num;
 	
 	
 	public BuildCluster(String name){
 		this.num = (int) (readFile(name)/10000);
 		
 	}
 	
 	
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
		int[] numofClusters = {500,250,150,100,50,25};
 		System.out.println("start reading");
 		BuildCluster bc;
 		System.out.println("reading finish");
 	    ClusterAnalysis HCL =new ClusterAnalysis();
 	    System.out.println("reading clustering");
 	    String filePath = "";
 	    File f = new File(filePath);
 	    String[] list = f.list();
 	    
 	    for (String file : list){
 	    	bc = new BuildCluster(filePath);
 	    	List<Cluster> clusters = HCL.startAnalysis(bc.getPoints(), bc.num); 
 		    System.out.println("clustering finish, write to file");
 		    bc.writeToFile(clusters, file, "clusters\\");
 	    }
 	    System.out.println("all done!");
 	}
 	
 	
 	private long readFile(String fileName){
 		BufferedReader reader = null;		
 		String lineWord;
 		this.dpoints = new ArrayList<DataPoint>();
 		long l = 0;
 		try {
 			File f = new File(clusterFilePath + fileName);
 			l = f.length();
 			reader = new BufferedReader(new FileReader(clusterFilePath + fileName));
 			while ((lineWord = reader.readLine()) != null){
 				 String[] tr = lineWord.split(",");
 				 double[] p = new double[2];
 				 p[0] = Double.parseDouble(tr[1]);
 				 p[1] = Double.parseDouble(tr[2]);
 				 DataPoint point = new DataPoint(p, tr[0]);
 				 this.dpoints.add(point);
 				 
 				 
 				 
 			}
 		} catch (NumberFormatException | IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return l;
 	}
 	
 	public ArrayList<DataPoint> getPoints(){
 		
 		return this.dpoints;
 	}
 	
 	public void writeToFile(List<Cluster> clusters, String file, String clusterFolder){
 		String foldPath = "c" + num;
 		File fold = new File(clusterFilePath + foldPath);
 		if (!fold.exists()){
 			fold.mkdir();
 		}
 		
 		
 		for(Cluster cl:clusters){
 			String filename = "l-" + num + "_c_" + cl.getClusterName();
 			try {
 				FileWriter fw = new FileWriter(clusterFilePath + foldPath + "\\" + filename);
 				BufferedWriter bw = new BufferedWriter(fw);
 				List<DataPoint> tempDps = cl.getDataPoints();
 				for(DataPoint tempdp : tempDps){
 					bw.write(tempdp.getDataPointName() + "," + tempdp.getDimensioin()[0] + "," +tempdp.getDimensioin()[1]);
 					bw.newLine();
 				}
 				bw.flush();
 				bw.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	           
 	    }
 	 
 		
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 
 }
