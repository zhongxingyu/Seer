 package edu.chl.asciicam.filter;
 
 //Copyright 2012 Robin Braaf, Ossian Madisson, Martin Thrnesson, Fredrik Hansson and Jonas strm.
 //
 //This file is part of Asciicam.
 //
 //Asciicam is free software: you can redistribute it and/or modify
 //it under the terms of the GNU General Public License as published by
 //the Free Software Foundation, either version 3 of the License, or
 //(at your option) any later version.
 //
 //Asciicam is distributed in the hope that it will be useful,
 //but WITHOUT ANY WARRANTY; without even the implied warranty of
 //MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //GNU General Public License for more details.
 //
 //You should have received a copy of the GNU General Public License
 //along with Asciicam.  If not, see <http://www.gnu.org/licenses/>.
 
 import java.util.ArrayList;
 import java.util.List;
 import edu.chl.asciicam.util.Convert;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Typeface;
 import android.graphics.Paint.Style;
 
 /**
  * A filter which creates a bitmap consisting of ascii symbols from
  * a input bitmap. 
  * @author Braaf
  *
  */
 public class AsciiFilter2 implements FilterInterface {
 	
 	//How many pixels to skip before checking again
 	private int compression;
 	//Fontsize on the output picture, will change size on the picture.
 	private int fontSize;
 	//Color for background and text, should be a Color.constant variable
 	private int bgColor;
 	private int textColor;
 	
 	private String[] symbol = {"@", "@", "#", "#", "%", "", "", "i", "i", "", "", "+", "+", "m", "m", "*", "*", ".", ".", " ", " "};
 	
 	/**
 	 * Default constructor, sets default options which is 
 	 * compression : 6
 	 * backgroundcolor : white
 	 * textcolor : black.
 	 */
 	public AsciiFilter2(){
 		compression = 7;
 		fontSize = 20;
 		bgColor = Color.WHITE;
 		textColor = Color.BLACK;
 	}
 	
 	/**
 	 * 
 	 * @param compression Compression level for filter.
 	 */
 	public AsciiFilter2(int compression){
 		this.compression = compression;
 		fontSize = 20;
 		bgColor = Color.WHITE;
 		textColor = Color.BLACK;
 	}
 	
 	/**
 	 * 
 	 * @param compression Compression level for filter.
 	 * @param bgColor Backgroundcolor for bitmap output.
 	 */
 	public AsciiFilter2(int compression, int bgColor){
 		compression = 6;
 		fontSize = 10;
 		bgColor = Color.WHITE;
 		textColor = Color.BLACK;
 	}
 	
 	/**
 	 * 
 	 * @param compression Compression level for filter.
 	 * @param bgColor Backgroundcolor for bitmap output.
 	 * @param textColor Textcolor on bitmap output.
 	 */
 	public AsciiFilter2(int compression, int bgColor, int textColor){
 		compression = 6;
 		fontSize = 20;
 		bgColor = Color.WHITE;
 		textColor = Color.BLACK;
 	}
 	
 	/**
 	 * This method takes a 
 	 * @param bm Bitmap to be converted
 	 */
 	public Bitmap convert(Bitmap bm) {
 		List<String> list = filter(bm);
 		return createBitmap(list);
 	}
 	
 	//
 	//
 	//
 	private List<String> filter(Bitmap bmp){
     	
 		//List to populate with rows of strings
     	List<String> list = new ArrayList<String>();
     	
     	int height = bmp.getHeight();
     	int width = bmp.getWidth();
     	
     	//Loop through the picture, i being rows of pixels and
     	//j being column at a row. Skip a number of rows set in
     	//compression
     	for(int i = 0; i < height; i+=compression){
     		String s = "";
     		for(int j =0; j < width; j+=compression){
     			//get average RGB and add a symbol depending
     			//average value should always be between 0 and 255!
     			float color = Convert.averageRGB(bmp.getPixel(j, i)) * symbol.length;
     			color = color / 255;
     			//color can be 0 - 21 here, so we need to remove 1 if its above 20 to
     			//avoid array out of bounds exception.
     			if(color > 20)
     				color -= 1;
     			s += symbol[(int)color]; //something from ascii
     		}
     		list.add(s);
     	}
     	return list;
     }
 	
 	//
     //This method creates the output bitmap from a List<String>.
     //
     private Bitmap createBitmap(List<String> pic){
     	// Bitmap width = length
     	Paint paint = new Paint();
     	Bitmap bitmap = Bitmap.createBitmap(pic.get(0).length() * fontSize, pic.size() * fontSize, Bitmap.Config.RGB_565);
     	Canvas canvas = new Canvas(bitmap);
     	
     	
     	//Fill background with black
     	paint.setColor(bgColor);
     	paint.setStyle(Style.FILL);
     	canvas.drawPaint(paint);
     	
     	//Set color and stuff for drawing text
     	paint.setColor(textColor); 
     	paint.setTextSize(fontSize);
     	//Monospace is needed for correct indentation
     	paint.setTypeface(Typeface.MONOSPACE);
     	//Scale or or will be shrimped in width, 1.7 seems to be the magical number. (1 is default)
     	paint.setTextScaleX((float)1.7);
     	//paint.setTextAlign(Paint.Align.CENTER);
     	
     	float x = 0, y = fontSize;
     	
     	for(String s : pic){
     		char[] string = s.toCharArray();
     		canvas.drawText(string, 0, string.length, x, y, paint);
     		y += fontSize;
     	}
     	return bitmap;
     }
     
     ///////////////////////////////////////////////////////////////////
     ///////SETTERS AND GETTERS FOR OPTIONS/////////////////////////////
     ///////////////////////////////////////////////////////////////////
     /**
      * get bgColor for filter output.
      * @return the Color.color being used for background fill.
      */
	public int getBgColor(){
 		return bgColor;
 	}
 	
 	/**
 	 * get textColor used for ascii symbols on output.
 	 * @return the Color.color used for ascii symbols on output.
 	 */
 	public int getTextColor(){
 		return textColor;
 	}
 	
 	/**
 	 * Get compression rate for filter.
 	 * @return Compression rate for filter.
 	 */
 	public int getCompression(){
 		return compression;
 	}
 	
 	/**
 	 * Set background color for ascii pictures.
 	 * @param color Color.color constant.
 	 */
 	public void setBgColor(int color){
 		this.bgColor = color;
 	}
 	
 	/**
 	 * Set text color for symbols output on filtered bitmap.
 	 * @param color Color.color constant.
 	 */
 	public void setTextColor(int color){
 		this.textColor = color;
 	}
 	
 	/**
 	 * Set compressionrate for filter, must be 1 or greater. 
 	 * If 1, filter will check every pixel in picture and output an ascii sign.
 	 * If 5, filter will check every fifth pixel and output an ascii sign, and so on.
 	 * @param compression
 	 */
 	public void setCompression(int compression){
 		this.compression = compression;
 	}
 }	
