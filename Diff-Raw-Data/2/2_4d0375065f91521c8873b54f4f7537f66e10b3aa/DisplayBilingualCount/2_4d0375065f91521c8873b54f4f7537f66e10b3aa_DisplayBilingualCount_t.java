 package org;
 
 public class DisplayBilingualCount {
 
 	DisplayCount firstLangObj;
 	DisplayCount secondLangObj;
 	
 	public DisplayBilingualCount(DisplayCount firstLang,DisplayCount secondLang){
 		firstLangObj=firstLang;
 		secondLangObj=secondLang;
 	}
 	
 	public void start(){
 		firstLangObj.blfirst=true;
		secondLangObj.blfirst=false;
		
 		for(int i=0;i<=9;i++){
 			firstLangObj.count=i;
 			secondLangObj.count=i;
 			
 			firstLangObj.run();
 			secondLangObj.run();
 		}
 		
 	}
 	
 }
