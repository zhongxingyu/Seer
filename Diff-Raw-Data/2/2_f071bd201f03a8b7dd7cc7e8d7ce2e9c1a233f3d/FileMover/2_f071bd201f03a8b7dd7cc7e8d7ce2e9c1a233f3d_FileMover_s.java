 package com.tortel.externalize;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Handler;
 
 public class FileMover extends Thread {
 	//Debug
 	public static final boolean D = true;
 	
 	//Private variables
 	private List<File> fileList;
 	private List<File> dirList;
 	private Shell sh;
 	private Handler copyProgressHandler;
 	private int type;
 	
 	private File rootDir;
 	
 	
 	public FileMover(Handler handler, int type){
 		copyProgressHandler = handler;
 		sh = new Shell();
 		this.type = type;
 		rootDir = new File(Paths.internal+Paths.dir[type]);
 	}
 	
 	private void linkDirs(){
     	Log.d("Deleting files");
         sh.exec("rm -rf "+Paths.internal+Paths.dir[type]+"*");
         sh.exec("mount -o loop "+Paths.external+Paths.dir[type]+" "+Paths.internal+Paths.dir[type]);
         
     }
     
     
     public int getFileList() {
 		fileList = new ArrayList<File>();
 		dirList = new ArrayList<File>();
         if( !rootDir.exists() || !rootDir.isDirectory() ) 
             return -1;
         //Add all files that comply with the given filter
         File[] files = rootDir.listFiles();
         for( File f : files) {
         	if(f.isDirectory()){
         		dirList.add(f);
         		if( f.canRead() )
         			getFileList();
         	}
             if(!fileList.contains(f) )
             	fileList.add(f);
         }
         
         return fileList.size();
     }
     
 	 /**
 	  *  Copies src file to dst file.
 	  *  If the dst file does not exist, it is created
 	  * @param src
 	  * @param dst
 	  * @throws IOException
 	  */
 	 private void copy(File src, File dst) throws IOException {
 		 if(D) Log.d("Copying "+src.toString()+"\t"+dst.toString());
 	     InputStream in = new FileInputStream(src);
 	     OutputStream out = new FileOutputStream(dst);
 	
 	     // Transfer bytes from in to out
 	     //Using 2kb chunks
 	     byte[] buf = new byte[1024 * 2];
 	     int len;
 	     while ((len = in.read(buf)) > 0) {
 	         out.write(buf, 0, len);
 	     }
 	     in.close();
 	     out.close();
 	 }
 	 
 	 
 	public void run() {
 		if(fileList == null || dirList == null)
 			getFileList();
 		Log.d("Copying files");
 	   String outDir = Paths.external+Paths.dir[type];
 	   //This is for the substring to make to cut /sdcard/IMAGES/
 	   int index = outDir.length() + 1;
 	   
 	   File tmp = new File(outDir);
 	   if( !tmp.exists() )
 		   tmp.mkdir();
 	   //Make the dirs
 	   for(File f : dirList){
 		   if(D) Log.d("Making dir: "+outDir+f.toString().substring(index));
 		   new File(outDir+f.toString().substring(index)).mkdir();
 	   }
 	   for(File f: fileList){
 		   	try {
 		   		tmp = new File(outDir+f.toString().substring(index));
 		   		if( !(tmp.exists() && tmp.isDirectory()) )
 		   			copy(f, tmp);
 				} catch (IOException e) {
 					Log.e("Error copying "+f.toString());
 				}
 		   	//Prevent exceptions
 		   	if(copyProgressHandler != null)
 		   		copyProgressHandler.sendMessage(copyProgressHandler.obtainMessage());
 	   }
 	   //This is to handle cases when tthe fileList is empty
 	   if(copyProgressHandler != null)
 		   copyProgressHandler.sendMessage(copyProgressHandler.obtainMessage());
 	   linkDirs();
 	}
 	
 	
     /**
      * Directory Filter
      */
     public static class DirFilter implements FileFilter {
         @Override
         public boolean accept(File pathname) {
             if( pathname.isDirectory() ) 
                 return true;
 
             return false;
         }
 
     }
 }
