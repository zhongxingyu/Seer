 package com.redarc.lteweb;
 
 import java.io.IOException;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 
 import com.redarc.BaseWeb;
 import com.redarc.MonitorDisplay;
 
 public abstract class ConvertWeb extends BaseWeb {
 	private Document doc; 
 	public ConvertWeb(String fileName){
 		try {
 			setDoc(Jsoup.connect(MonitorDisplay.LOCAL_SRV + fileName)
 				     .data("jquery","java")
 				     .userAgent("Mozilla")
 				     .followRedirects(true)
 				     .get());
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public Document getDoc() {
 		return doc;
 	}
 	public void setDoc(Document doc) {
 		this.doc = doc;
 	}
 }
