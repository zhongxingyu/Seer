 package src;
 
 import java.io.*;
 import java.util.ArrayList;
 public class FilesLoader{
 	ArrayList<File> files = new ArrayList<File>();
 
 	public void searchFile(String path){
 		files.clear();
 		File file = new File(path);
 		File[] findFile = file.listFiles();
 		for	(int i = 0;i < findFile.length; i++){
 			if (findFile[i].getName().startsWith("ME") && findFile[i].getName().endsWith("TDF")){
 				files.add(findFile[i]);
 			}
 		}
 	}	
 	
 	public void moveFile(String path,int position){
 		try{
			if(files.get(position).renameTo(new File(path+getFilename(position)))){
 				System.out.println(getFilename(position) + " Successful !");
 			}else{
 				System.out.println(getFilename(position) + " Not move !");
 			}
 		}catch(Exception e){
 				e.printStackTrace();
 		}
 	}	
 
 	public void moveAllFile(String path) {
 		try {
 			for (File mFiles : files) {
 				if (mFiles.renameTo(new File(path + "/"
 						+ mFiles.getName().toString()))) {
 					System.out.println(mFiles.getName().toString()
 							+ " Successful !");
 				}else{
 					System.out.println(mFiles.getName().toString() + " Not move !");
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void renameFile(){
 		try{
 			File newFilename = new File(getFilePath(0)+"/MEABCD.TDF");
 			if(files.get(0).renameTo(newFilename)){
 				files.add(0,newFilename);
 			}else{
 				System.out.println("can not rename file !");
 			}
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 
 	public String getFilename(int position){
 		return files.get(position).getName().toString();
 	}
 	
 	public String getFilePath(int position){
 		String filePath = files.get(position).getAbsolutePath();
 		return filePath.substring(0,filePath.lastIndexOf(File.separator));
 	}	
 }
