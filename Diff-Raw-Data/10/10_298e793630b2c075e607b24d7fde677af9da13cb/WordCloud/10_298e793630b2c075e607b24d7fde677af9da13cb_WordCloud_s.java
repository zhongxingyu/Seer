 package controllers;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 
 import play.mvc.Controller;
 import utils.MyWordle;
 import utils.MyWordle.Word;
 
 public class WordCloud extends Controller {
 
     public static void generate(String theText,boolean rotate,int imgWidth) {
     	System.out.println("Got a request for WordCloud.generate");
    	response.setContentTypeIfNotSet("application/png");
 
 		try
 		{
 		MyWordle app=new MyWordle();
 		app.setOutputWidth(1000);
 	    app.setAllowRotate(rotate);
 		Color[] wordColors = new Color[5];
 		wordColors[0] = Color.BLACK;
 		wordColors[1] = Color.RED;
 		wordColors[2] = Color.BLUE;
 		wordColors[3] = Color.GREEN;
 		wordColors[4] = Color.ORANGE;
 		Integer wordIndex =0;
 
 		Word currWord;
 		Color wordColor;
 		for(String word:theText.split(" ")){
 			currWord = new Word(word,1+new Double(Math.random()*200).intValue());
 			wordColor = wordColors[(wordIndex++) % 5];
 			currWord.setFill(wordColor);
 			currWord.setStroke(wordColor);
 			app.add(currWord);
 		}
 		app.doLayout();
 		
 		byte[] pngData = app.saveAsPNG().toByteArray();
 		System.out.println("Binary Data:"+pngData);
 		
 		renderBinary(new ByteArrayInputStream(pngData));
 		} 
 	catch(Throwable err)
 		{
 		err.printStackTrace();
 		}
 	}
 
 }
