 package tcwi.TCWIFile;
 
 import java.util.ArrayList;
 
 import tcwi.enumFiles.ErrorState;
 
 public class ErrorCompareFile implements Comparable<ErrorFile>{
 
 	protected String path;
 	protected ErrorState excluded;
 	protected ErrorState compileError;
 	protected ErrorState filestate;
 	protected ArrayList<ParserError> oldParserError;
 	protected ArrayList<ParserError> newParserError;
 	protected ArrayList<TypeError> oldTypeError;
 	protected ArrayList<TypeError> newTypeError;
 	
 	public ErrorCompareFile(){
 		this.path = "";
 		oldParserError = new ArrayList<ParserError>();
 		newParserError = new ArrayList<ParserError>();
 		oldTypeError = new ArrayList<TypeError>();
 		newTypeError = new ArrayList<TypeError>();
 	}
 
 	public void addOldError(Object error){
 		if(error instanceof ParserError){
 			oldParserError.add((ParserError) error);
 		}
 		if(error instanceof TypeError){
 			oldTypeError.add((TypeError) error);
 		}
 	}
 
 	public void addNewError(Object error){
 		if(error instanceof ParserError){
 			newParserError.add((ParserError) error);
 		}
 		if(error instanceof TypeError){
 			newTypeError.add((TypeError) error);
 		}
 	}
 	
 	public String getPath() {
 		return path;
 	}
 
 	public void setPath(String path) {
 		this.path = path;
 	}
 
 	public ErrorState getExcluded() {
 		return excluded;
 	}
 
 	public void setExcluded(ErrorState excluded) {
 		this.excluded = excluded;
 	}
 
 	public ErrorState getCompileError() {
 		return compileError;
 	}
 
 	public void setCompileError(ErrorState compileError) {
 		this.compileError = compileError;
 	}
 
 	public ArrayList<ParserError> getOldParserError() {
 		return oldParserError;
 	}
 
 	public ArrayList<ParserError> getNewParserError() {
 		return newParserError;
 	}
 
 	public ArrayList<TypeError> getOldTypeError() {
 		return oldTypeError;
 	}
 	public ArrayList<TypeError> getNewTypeError() {
 		return newTypeError;
 	}
 
 	public boolean haveErrors(){
 		return (oldParserError.size()+oldTypeError.size()+newParserError.size()+newTypeError.size())>0 ? true : false;
 	}
 	
 	public ErrorState getFilestate() {
 		return filestate;
 	}
 
 	public void setFilestate(ErrorState filestate) {
 		this.filestate = filestate;
 	}
 	
 	public boolean isParserErrorCountChanged(){
 		boolean found = false;
 		for(int i=0;i<oldParserError.size();i++){
 			found = false;
 			for(int j=0;j<newParserError.size();j++){
 				if(newParserError.get(j).equals(oldParserError.get(i))){
 					found = true;
 					break;
 				}
 			}
 			if(!found){
				return true;
 			}
 		}
		return false;
 	}
 	
 	public boolean isTypeErrorCountChanged(){
 		boolean found = false;
 		for(int i=0;i<oldTypeError.size();i++){
 			found = false;
 			for(int j=0;j<newTypeError.size();j++){
 				if(newTypeError.get(j).equals(oldTypeError.get(i))){
 					found = true;
 					break;
 				}
 			}
 			if(!found){
				return true;
 			}
 		}
		return false;
 	}
 
 	@Override
 	/**
 	 * Comparing filenames
 	 */
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
 
 	@Override
 	public boolean equals(Object arg0) {
 		if(arg0 instanceof ErrorFile){
 			return this.path.equals(((ErrorFile) arg0).getPath());
 		}else{
 			return false;
 		}
 	}
 	
 	
 }
