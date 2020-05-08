 package job;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Date;
 
 import os.Pipe;
 
 /**
  * job/Ls.java
  * <br><br>
  * List directory contents.
  * 
  * @author Jan Masopust
  * 
  * @version 0.8
  * 
  * @team <i>OutOfMemory</i> for KIV/OS 2013
  * @teamLeader Radek Petruška  radekp25@students.zcu.cz
  * 
  */
 public class Ls extends Job{
 	/** Is there argument -l **/
 	private boolean isL = false;
 	
 	/**
 	 * Constructor.
 	 * @param PID of the new process.
 	 * @param stdERR  where to write errors.
 	 */
 	public Ls(Integer PID, Pipe stdERR) {
 		super(PID, stdERR);
 	}
 
 	@Override
 	public String getManual() {
 		return "ls - list directory contents\n" +
 				"Description\n" +
 				"\tflags are case sensitive\n" +
 				"\t-l - use a long listing format\n";
 	}
 
 	@Override
 	protected void getJobDone() throws InterruptedException {
 		if(this.path != null){
 			if(this.arguments != null && this.arguments.length == 0){
 				lsDirectory(this.path);
 			}
 			else{
 				/** Options **/
 				for(int i = 0; i < this.arguments.length; i++){
 					if(this.arguments[i].charAt(0) == '-'){
 						/** -l **/
 						if(this.arguments[i].substring(1, this.arguments[i].length()).equals("l")){
 							this.isL = true;
 						}
 					}
 				}
 				
 				lsDirectory(this.path);
 				
 				for(int i = 0; i < this.arguments.length; i++){
 					if(this.arguments[i].charAt(0) != '-'){
 						lsDirectory(this.arguments[i]);
 					}
 				}
 			}
 		}
 		else{
 			pushError("Shell does not provide the path.");
 		}
 			
 			
 	}
 	
 	/**
 	 * Ls on a directory.
 	 * @param directory path
 	 * @throws InterruptedException
 	 */
 	private void lsDirectory(String directory) throws InterruptedException{
 		File file;
 		file =  new File(directory);
 
 		if ( !file.exists() || !file.canRead() ) {
 			pushError("Can't read " + file);
 			return;
 		}
 		if ( !file.isDirectory() )  {
 			pushError("'"+ directory +"' is not a directory");
 			return;
 		}
 
 		try {
 			pushData((directory + ":\n").toCharArray(), 0, directory.length() + 2);
 		} catch (IOException e1) {
 			pushError("Write error");
 		}
 		String [] files = file.list();
 		for(int i = 0; i < files.length; i++){
 			if(this.isL){
 				files[i] = lSwitch(directory + File.separator+ files[i]) + "\t" + files[i] + "\n";
 			}
 			else{
 				files[i] +=  "\n";
 			}
 			
 			try {
 				pushData(files[i].toCharArray(), 0, files[i].length());
 			} catch (IOException e) {
 				pushError("Write error");
 			}
 		}
 	}
 	/**
 	 * -l switch (rwx dir? size date)
 	 * @param filePath
 	 * @return file descriptions
 	 */
 	@SuppressWarnings("boxing")
 	private static String lSwitch(String filePath){
 		File f = new File(filePath);
 		String str = "";
 		str += f.canRead() ? "r" : "-";
 		str += f.canWrite() ? "w" : "-";
		//str += f.canExecute() ? "x" : "-";
 		str += "\t";
 		str += f.isDirectory() ? "dir" : "   ";
 		str += "\t";
 		//FIXME f.length returns primitive long, but String.format makes class Long?
 		str += String.format("%14d", f.length());
 		str += "\t";
 
 		Date date = new Date(f.lastModified());
 		str += date.toString();
 		
 		return str;
 	}
 
 }
