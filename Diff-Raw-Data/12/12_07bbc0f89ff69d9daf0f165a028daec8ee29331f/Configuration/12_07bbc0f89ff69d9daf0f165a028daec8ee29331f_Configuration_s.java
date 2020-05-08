 import java.io.*;
 import java.util.*;
 
 public class Configuration extends Module {
 
 	private HashMap<String, Configuration.PeerInfo> peerList;
         private Configuration.CommonInfo commonInfo;
 
     /**
      * @return the peerList
      */
     public HashMap<String, Configuration.PeerInfo> getPeerList() {
         return peerList;
     }
 
     /**
      * @return the commonInfo
      */
     public Configuration.CommonInfo getCommonInfo() {
         return commonInfo;
     }
         
         public class CommonInfo
         {
             private int numberOfPreferredNeighbors;
             private float unchokingInterval;
             private float optimisticUnchokingInterval;
             private String fileName;
             private int fileSize;
             private int pieceSize;
 
             /**
              * @return the numberOfPreferredNeighbors
              */
             public int getNumberOfPreferredNeighbors() {
                 return numberOfPreferredNeighbors;
             }
 
             /**
              * @param numberOfPreferredNeighbors the numberOfPreferredNeighbors to set
              */
             public void setNumberOfPreferredNeighbors(int numberOfPreferredNeighbors) {
                 this.numberOfPreferredNeighbors = numberOfPreferredNeighbors;
             }
 
             /**
              * @return the unchokingInterval
              */
             public float getUnchokingInterval() {
                 return unchokingInterval;
             }
 
             /**
              * @param unchokingInterval the unchokingInterval to set
              */
             public void setUnchokingInterval(float unchokingInterval) {
                 this.unchokingInterval = unchokingInterval;
             }
 
             /**
              * @return the optimisticUnchokingInterval
              */
             public float getOptimisticUnchokingInterval() {
                 return optimisticUnchokingInterval;
             }
 
             /**
              * @param optimisticUnchokingInterval the optimisticUnchokingInterval to set
              */
             public void setOptimisticUnchokingInterval(float optimisticUnchokingInterval) {
                 this.optimisticUnchokingInterval = optimisticUnchokingInterval;
             }
 
             /**
              * @return the fileName
              */
             public String getFileName() {
                 return fileName;
             }
 
             /**
              * @param fileName the fileName to set
              */
             public void setFileName(String fileName) {
                 this.fileName = fileName;
             }
 
             /**
              * @return the fileSize
              */
             public int getFileSize() {
                 return fileSize;
             }
 
             /**
              * @param fileSize the fileSize to set
              */
             public void setFileSize(int fileSize) {
                 this.fileSize = fileSize;
             }           
 
         /**
          * @return the pieceSize
          */
         public int getPieceSize() {
             return pieceSize;
         }
 
         /**
          * @param pieceSize the pieceSize to set
          */
         public void setPieceSize(int pieceSize) {
             this.pieceSize = pieceSize;
         }
         }
 	
 	public class PeerInfo
 	{
 		private int peerID;
 		private String hostName;
 		private int portNumber;
 		private boolean hasFile;
 		
 		protected void setPeerID(int peerID)
 		{
 			this.peerID = peerID;
 		}
 
 		public int getPeerID()
 		{		
 			return peerID;
 		}
 
 		protected void setHostName(String hostName)
 		{
 			this.hostName = hostName;
 		}
 
 		public String getHostName()
 		{
 			return hostName;
 		}
 
 		protected void setPortNumber(int portNumber)
 		{
 			this.portNumber = portNumber;
 		}
 
 		public int getPortNumber()
 		{
 			return portNumber;
 		}
 
 		protected void setHasFile(boolean hasFile)
 		{
 			this.hasFile = hasFile;
 		}
 
 		public boolean getHasFile()
 		{
 			return hasFile;
 		}
 
 	}
 	
 	public void initialConfiguration() {
 
 		String st;
 		boolean hasFile;
 		peerList = new HashMap<String, Configuration.PeerInfo>();
 		Configuration.PeerInfo node = new Configuration.PeerInfo();
 			try {
 				BufferedReader in = new BufferedReader(new FileReader(Constants.PEER_CFG_FILE));
 		
 
 					while((st = in.readLine()) != null)
 					{
 						String tokens[] = st.split(" ");
 							
 							
 							node.setPeerID(Integer.parseInt(tokens[0]));
 							node.setHostName(tokens[1]);
 							node.setPortNumber(Integer.parseInt(tokens[2]));
 				
 							hasFile = (tokens[3] == "1")  ? true : false;
 							node.setHasFile(hasFile);
 					
 							getPeerList().put(tokens[0], node);
 						}
 						
 						
 				in.close();
 			}	catch(IOException e)
 				{
 					System.out.println("There was a problem opening the peer configuration file. Make sure the file exists");
 				}
                         
                         //read in config info
                         peerList = new HashMap<String, Configuration.PeerInfo>();
                         Configuration.CommonInfo commonInfo = new Configuration.CommonInfo();
 			try {
 				BufferedReader in = new BufferedReader(new FileReader(Constants.COMMON_CFG_FILE));
 		
 
 					while((st = in.readLine()) != null)
 					{
 						String tokens[] = st.split(" ");
 							
 							/*private int numberOfPreferredNeighbors;
                                                         private float unchokingInterval;
                                                         private float optimisticUnchokingInterval;
                                                         private String fileName;
                                                         private int fileSize;
                                                         private int pieceSize;*/
							commonInfo.setNumberOfPreferredNeighbors(Integer.parseInt(tokens[0]));
							commonInfo.setUnchokingInterval(Float.parseFloat(tokens[1]));
							commonInfo.setOptimisticUnchokingInterval(Float.parseFloat(tokens[2]));
                                                        commonInfo.setFileName(tokens[3]);
                                                        commonInfo.setFileSize(Integer.parseInt(tokens[4]));
                                                        commonInfo.setPieceSize(Integer.parseInt(tokens[5]));                                                   		
 						}
 						
 						
 				in.close();
 			}	catch(IOException e)
 				{
 					System.out.println("There was a problem opening the common configuration file. Make sure the file exists");
 				}
 	}
 
 }
 
