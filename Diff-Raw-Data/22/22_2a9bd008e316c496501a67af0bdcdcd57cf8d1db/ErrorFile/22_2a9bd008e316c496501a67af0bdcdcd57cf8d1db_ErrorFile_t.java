 package tcwi;
 
 public class ErrorFile implements Comparable<ErrorFile>{
 	private String path;
 	private boolean isEmptyFile;
 	private boolean isNotTrueSucceeded;
 	private boolean haveTypeErrors;
 	
 	public ErrorFile(){
 		this.path = "";
 		this.isEmptyFile = true;
 		this.isNotTrueSucceeded = true;
 		this.haveTypeErrors = true;
 	}
 
 	public ErrorFile(String path, boolean isEmptyFile, boolean isNotTrueSucceeded, boolean haveTypeErrors) {
 		this.path = path;
 		this.isEmptyFile = isEmptyFile;
 		this.isNotTrueSucceeded = isNotTrueSucceeded;
 		this.haveTypeErrors = haveTypeErrors;
 	}
 
 	public ErrorFile(String path, boolean[] flags) {
 		this.path = path;
 		this.isEmptyFile = flags[0];
 		this.isNotTrueSucceeded = flags[1];
 		this.haveTypeErrors = flags[2];
 	}
 
 	public String getPath() {
 		return path;
 	}
 
 	public void setPath(String path) {
 		this.path = path;
 	}
 
 	public boolean isEmptyFile() {
 		return isEmptyFile;
 	}
 
 	public void setEmptyFile(boolean isEmptyFile) {
 		this.isEmptyFile = isEmptyFile;
 	}
 
 	public boolean isNotTrueSucceeded() {
 		return isNotTrueSucceeded;
 	}
 
 	public void setNotTrueSucceeded(boolean isNotTrueSucceeded) {
 		this.isNotTrueSucceeded = isNotTrueSucceeded;
 	}
 
 	public boolean isHaveTypeErrors() {
 		return haveTypeErrors;
 	}
 
 	public void setHaveTypeErrors(boolean haveTypeErrors) {
 		this.haveTypeErrors = haveTypeErrors;
 	}
 
 	@Override
 	public String toString() {
 		return this.path+"\t"+isEmptyFile+"\t"+isNotTrueSucceeded+"\t"+haveTypeErrors;
 	}
 
 	@Override
 	public int compareTo(ErrorFile errFile) {
 		if(errFile.getPath().equals(this.path)){
 			return 0;
 		}
 		for(int i=0;i<errFile.getPath().length();i++){
 			if(this.path.length()>i){
 				if(errFile.getPath().charAt(i)!=this.getPath().charAt(i)){
 					if(errFile.getPath().charAt(i)>this.getPath().charAt(i)){
 						return -1;
					}else{
						return 1;
 					}
 				}
 			}else{
 				return 1;
 			}
 		}
 		
 		return 0;
 	}
 	
 }
