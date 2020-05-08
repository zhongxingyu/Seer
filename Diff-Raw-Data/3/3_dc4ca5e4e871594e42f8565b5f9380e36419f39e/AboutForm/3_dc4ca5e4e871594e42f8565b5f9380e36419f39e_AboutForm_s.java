 package org.fruct.oss.russianriddles;
 
 import java.io.IOException;
 
 import javax.microedition.lcdui.Form;
 import javax.microedition.lcdui.Image;
 import javax.microedition.lcdui.ImageItem;
 
 public class AboutForm extends Form{
 	
 	public AboutForm() {
 		super(" ");
 		try {
 			this.append(new ImageItem(null, Image.createImage("/podkova.png"), ImageItem.LAYOUT_CENTER, null));
 		} catch (IOException ex) {}
		this.append("     FRUCT () 2013");
 		this.append(" :  ");
 		this.append(": GPL2");
 		this.append(" : https://github.com/seekerk/RussianRiddles");
 	}
 }
