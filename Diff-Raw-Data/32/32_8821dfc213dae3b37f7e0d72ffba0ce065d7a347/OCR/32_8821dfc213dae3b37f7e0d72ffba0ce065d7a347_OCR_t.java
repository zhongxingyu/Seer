 package com.t_oster.notenschrank.data;
 
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.awt.image.RenderedImage;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.imageio.ImageIO;
 
 public class OCR {
 
 	public static class OCRResult{
 		public String beautified;//The beautified Version of the string real read in the image
 		public int matchquality;// the quality of the matched index
 		public int index;// the index of the string matching best
 	}
 	/*
 	 * This method returns the index of the String which is most probably
 	 * contained in the given Image or the beautified String found in the image when matching is lower than 50%
 	 * 
 	 * This method ignores the order of the words in possible strings
 	 * eg. "hello world" matches to "world hello" 100%
 	 */
 	public static OCRResult findStringInImage(String[] possible, Image image) {
 		
 		String readString = getStringsInImage(image);
 		System.out.println("Read String from Image:"+readString);
 		int maxmatch=0;
 		int index=0;
 		for(int i=0;i<possible.length;i++){
 			int match = match(possible[i],readString,true);
 			if (match>maxmatch){
 				maxmatch=match;
 				index=i;
 				if (maxmatch==100){
 					break;
 				}
 			}
 		}
 		
 		OCRResult result = new OCRResult();
 		result.matchquality=maxmatch;
 		result.index=index;
 		result.beautified=removeBad(readString);
 		return result;
 	}
 
	private static Boolean isAvailable = null;
 	public static boolean isAvailable(){
		if (isAvailable == null){
			try {
				Process p = Runtime.getRuntime().exec(new String[]{"convert", "--version"});
				if (p==null){
					throw new IOException("bla");
				}
				p = Runtime.getRuntime().exec(new String[]{"tesseract"});
				if (p==null){
					throw new IOException("bla");
				}
				isAvailable = true;
			} catch (IOException e) {
				isAvailable = false;
				System.out.println("OCR is not available. OCR disabled.");
				return isAvailable;
			}
		}
		return isAvailable;
 	}
 	
 	private static RenderedImage toBufferedImage(Image image) {
 		if (image instanceof RenderedImage){
 			System.out.println("no conversion needed");
 			return (RenderedImage) image;
 		}
 		BufferedImage buffImg = new BufferedImage(image.getWidth(null),
 				image.getHeight(null), BufferedImage.TYPE_INT_RGB);
 		final Graphics2D g2 = buffImg.createGraphics();
 		g2.drawImage(image, null, null);
 		g2.dispose();
 		return buffImg;
 	}
 
 	public static String getStringsInImage(Image image) {
 		String result=null;
 		File png = SettingsManager.getInstance().getTempFile("png");
 		File tif = SettingsManager.getInstance().getTempFile("tif");
 		File txt = SettingsManager.getInstance().getTempFile("txt");
 
 		try {
 			ImageIO.write(toBufferedImage(image), "png", png);
 			if (!png.exists()){
 				System.err.println("Error writing png");
 				return null;
 			}
			Process p = Runtime.getRuntime().exec(new String[]{"convert",png.getAbsolutePath(),"-depth", "4",tif.getAbsolutePath()});
 			InputStream is = p.getErrorStream();
 			int i;
 			do{
 				i = is.read();
 				if (0<=i && i<=255)
 				System.err.write(i);
 			}while (i>=0);
 			p.waitFor();
 			png.delete();
 			if (!tif.exists()){
 				System.err.println("Error: tif was not created (ImageMagic installed??)");
 				return null;
 			}
 			String txtname = txt.getAbsolutePath();
 			txtname = txtname.substring(0,txtname.length()-4);
 			p = Runtime.getRuntime().exec(new String[]{"tesseract",tif.getAbsolutePath(),txtname,"-l","deu"});
 			is = p.getErrorStream();
 			i=0;
 			do{
 				i = is.read();
 				if (0<=i && i<=255)
 				System.err.write(i);
 			}while (i>=0);
 			p.waitFor();
 			tif.delete();
 			if (txt.exists()){
 				BufferedReader rd = new BufferedReader(new FileReader(txt));
 				String s=null;
 				result="";
 				do{
 					s =rd.readLine();
 					result= s==null?result:result+s;
 				}while(s!=null);
 				rd.close();
 				txt.delete();
 			}
 			else{
 				System.err.println("Error: txt wasnt created. Tessearact failed??");
 				return null;
 			}
 			return result;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return null;
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public static final String allowedChars=" -'’ßabcdefghijklmnopqrstuvwxyzäüö0123456789";
 	/*
 	 * Removes all characters that are not wanted
 	 * just for beautifying String if couldn't be matched
 	 */
 	public static String removeBad(String input){
 		String result="";
 		String linput = input.toLowerCase();
 		int i=0;
 		for(char k:linput.toCharArray()){
 			if (allowedChars.contains(""+k)){
 				result+=input.charAt(i);
 			}
 			i++;
 		}
 		return result.trim();
 	}
 	
 	/*
 	 * Removes all characters which are not important
 	 * and converts to lowerCase
 	 * for decision
 	 */
 	public static final String importantCharacters = "abcdefghijklmnopqrstuvwxyz0123456789äöüß";
 	public static String removeDontCares(String input){
 		String result="";
 		input = input.toLowerCase();
 		for(char k:input.toCharArray()){
 			if (allowedChars.contains(""+k)){
 				result+=k;
 			}
 		}
 		return result.trim();
 	}
 	
 	/*
 	 * Returns a number how good the String good is
 	 * included in bad, eg. how many characters in right sequence
 	 * are recognized
 	 * 
 	 * This method removes dont cares!
 	 * @param split Determines if the order of words should be
 	 * ignored
 	 * 
 	 * returns values from 100 to 0
 	 */
 	public static int match(String good, String bad, boolean split){
 		if (split){
 			String[] arr = good.split(" ");
 			int sum=0;
 			for (String cur:arr){
 				sum+=match(removeDontCares(cur),removeDontCares(bad));
 			}
 			return sum/arr.length;
 		}
 		else{
 			return match(good,bad);
 		}
 	}
 	
 	public static int match(String good, String bad){
 		int maxfound=0;
 		
 		for (int start=0;start<bad.length();start++){
 			int gp=0;
 			int found=0;
 			for (int p=start;p<bad.length();p++){
 				if (gp<good.length() && good.charAt(gp) == (bad.charAt(p))){//for jumping
 					gp++;
 					found++;
 				}
 				else if (gp+1<good.length() && good.charAt(gp+1) == (bad.charAt(p))){
 					gp+=2;
 					found++;
 				}
 				else if (gp+2<good.length() && good.charAt(gp+2) == (bad.charAt(p))){
 					gp+=3;
 					found++;
 				}
 				if (gp>=good.length()){
 					break;
 				}
 			}
 			maxfound=Math.max(maxfound, found);
 		}
 		return maxfound*100/good.length();
 	}
 }
