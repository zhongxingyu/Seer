 package tcwi.TCWIFile;
 
 import tcwi.exception.Exceptions;
 
 public class CompareFile extends TCWIFile implements Comparable<CompareFile>{
 	protected String path;
 	protected String haveNoDBG;
 	protected String isNotTrueSucceeded;
 	protected String haveTypeErrors;
	
	public CompareFile(){
		this.path = "";
		this.haveNoDBG = "";
		this.isNotTrueSucceeded = "";
		this.haveTypeErrors = "";
	}
 
 	public CompareFile(String path, String haveNoDBG, String isNotTrueSucceeded, String haveTypeErrors) {
 		this.path = path;
 		this.haveNoDBG = haveNoDBG;
 		this.isNotTrueSucceeded = isNotTrueSucceeded;
 		this.haveTypeErrors = haveTypeErrors;
 	}
 
 	public String getPath() {
 		return path;
 	}
 
 	public void setPath(String path) {
 		this.path = path;
 	}
 
 	public String isNotTrueSucceeded() {
 		return isNotTrueSucceeded;
 	}
 
 	public void setNotTrueSucceeded(String isNotTrueSucceeded) {
 		this.isNotTrueSucceeded = isNotTrueSucceeded;
 	}
 
 	public String isHaveTypeErrors() {
 		return haveTypeErrors;
 	}
 
 	public void setHaveTypeErrors(String haveTypeErrors) {
 		this.haveTypeErrors = haveTypeErrors;
 	}
 	
 	public String isHaveNoDBG() {
 		return haveNoDBG;
 	}
 
 	public void setHaveNoDBG(String haveNoDBG) {
 		this.haveNoDBG = haveNoDBG;
 	}
 	
 	/**
 	 * Check if the CompareFile have changes
 	 * @return
 	 */
 	public boolean haveChanges(){
		String[] noDBG = this.haveNoDBG.split("|");
		String[] notTrueSucc = this.isNotTrueSucceeded.split("|");
		String[] haveTypeErr = this.haveTypeErrors.split("|");
 
 		if(noDBG.length >= 2 && notTrueSucc.length >= 2 && haveTypeErr.length >= 2){
 			if(!noDBG[0].equals(noDBG[1])){
 				return true;
 			}else{
 				if(!notTrueSucc[0].equals(notTrueSucc[1])){
 					return true;
 				}else{
 					if(!haveTypeErr[0].equals(haveTypeErr[1])){
 						return true;
 					}else{
 						return false;
 					}
 				}
 			}		
 		}else{
 			Exceptions exception = new Exceptions();
 			exception.throwException(11, null, true, "");
 			return true;
 		}
 	}
 	
 	@Override
 	public String toString() {
 		return this.path+"\t"+haveNoDBG+"\t"+isNotTrueSucceeded+"\t"+haveTypeErrors;
 	}
 
 	@Override
 	/**
 	 * Comparing filenames
 	 */
 	public int compareTo(CompareFile compareFile) {
 		if(compareFile.getPath().equals(this.path)){
 			return 0;
 		}
 		for(int i=0;i<compareFile.getPath().length();i++){
 			if(this.path.length()>i){
 				if(compareFile.getPath().charAt(i)!=this.getPath().charAt(i)){
 					if(compareFile.getPath().charAt(i)>this.getPath().charAt(i)){
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
