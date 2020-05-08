 package ioGame;
 
 public class Options {
 	private String[] args;
 	private String mode;
 	private String fileName;
 	private String player;
 	
 	public Options(String[] args){
		if (args.length > 0 ){
			this.mode = args[0].substring(1);
		}
 		this.args = args;
 	}
 	
 	
 	public String getFileName(){
 		return fileName;
 	}
 	
 	public String getPlayerNumber(){
 		return player;
 	}
 	
 	
 	public boolean valid(){
 		if (!consoleMode() && !visualMode()){
 			return false;
 		}
 		
 		if (consoleMode()){
 			if (this.args.length < 4){
 				return false;
 			}
 			
 			this.fileName = args[1];
 			if (args[3].equals("1")) {
 				this.player = "1";
 			} else if (args[3].equals("2")){
 				this.player = "2";
 			} else {
 				return false;
 			}
 		}
 		
 		return true;
 	}
 	
 	public boolean visualMode(){
		return mode != null && mode.equals("visual");
 	}
 	
 	public boolean consoleMode(){
		return mode != null && mode.equals("file");
 	}
 
 }
