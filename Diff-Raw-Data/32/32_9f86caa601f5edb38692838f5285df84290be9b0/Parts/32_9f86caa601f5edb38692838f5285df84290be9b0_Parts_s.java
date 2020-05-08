 /*
 ** Authors: David Cox, Timi Okuboyejo, Nikhil Handyal
 ** Date: 11/07/12
 ** Project: Cs200-Factory
 ** Description: Parts Class
 ** 
 ** Pre-Conditions: None
 */
 
 //package factory.global.data;
 
 // Java imports
 package factory.global.data;
 import java.io.*;
 import javax.swing.*;
 
 public class Parts implements java.io.Serializable{
 		int partNumber;
 		String name;
 		String desc;
 		int imageIndex; 
 		int mapIndex; 
 		
 		public Parts(int partNumber, String name, String desc, int imageIndex){
 			this.partNumber = partNumber; 
 			this.name = name; 
 			this.desc = desc; 
 			this.imageIndex = imageIndex; 
 			this.mapIndex = -1; 
 		}
 
 		public void setMapIndex(int mI){
 			mapIndex = mI; 
 
 		}
 
 		public int getMapIndex(){
 			return mapIndex; 
 		}
 
 		public int getPartNumber(){
 			return partNumber; 
 		}
 
 		public void setPartNumber(int partNumber){
 			this.partNumber = partNumber; 
 		}
 
 		public String getName(){
 			return name; 
 		}
 
 		public void setName(String name){
 			this.name = name; 
 		}
 
 		public String getDesc(){
 			return desc; 
 		}
 
 		public void setDesc(String desc){
 			this.desc = desc; 
 		}
 
 		public int getImageIndex(){
 			return imageIndex; 
 		}
 
 		public void setImageIndex(int imageIndex){
 			this.imageIndex = imageIndex; 
 		}
 		
 		public void print(){
 				System.out.print("partNumber: "+partNumber);
 				System.out.print(" name: "+name);
				System.out.print(" desc: "+desc);
 				System.out.println(" ImageIndex: "+imageIndex);
 		}
 }
 
 		
