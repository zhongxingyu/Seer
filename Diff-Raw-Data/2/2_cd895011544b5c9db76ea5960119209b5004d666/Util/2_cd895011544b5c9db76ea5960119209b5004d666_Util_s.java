 package org.csie.mpp.buku;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.lang3.StringEscapeUtils;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 
 public class Util {
 	private static final Map<String, String> publishers;
 	
 	static {
 		publishers = new HashMap<String, String>();
 		publishers.put("018926", "0445");
 		publishers.put("027778", "0449");
 		publishers.put("037145", "0812");
 		publishers.put("042799", "0785");
 		publishers.put("043144", "0688");
 		publishers.put("044903", "0312");
 		publishers.put("045863", "0517");
 		publishers.put("046594", "0694");
 		publishers.put("047132", "0152");
 		publishers.put("051487", "0816");
 		publishers.put("051488", "0142");
 		publishers.put("060771", "0002");
 		publishers.put("065373", "0373");
 		publishers.put("070992", "0523");
 		publishers.put("070993", "0446");
 		publishers.put("070999", "0345");
 		publishers.put("071001", "0380");
 		publishers.put("071009", "0440");
 		publishers.put("071125", "0886");
 		publishers.put("071136", "0451");
 		publishers.put("071149", "0451");
 		publishers.put("071152", "0515");
 		publishers.put("071162", "0451");
 		publishers.put("071268", "0821");
 		publishers.put("071831", "0425");
 		publishers.put("071842", "0843");
 		publishers.put("072742", "0441");
 		publishers.put("076714", "0671");
 		publishers.put("076783", "0553");
 		publishers.put("076814", "0449");
 		publishers.put("078021", "0872");
 		publishers.put("079808", "0394");
 		publishers.put("090129", "0679");
 		publishers.put("099455", "0061");
 		publishers.put("099769", "0451");
 		publishers.put("037145", "0765");
 		publishers.put("050694", "0345");
 		publishers.put("076714", "0743");
 		publishers.put("077434", "0743");
 		publishers.put("645573", "1595");
 	}
 	
 	public static boolean checkIsbn(String isbn) {
 		switch(isbn.length()) {
 			case 10:
 				return isbn.charAt(9) == getIsbn10CheckDigits(isbn).charAt(0);
 			case 13:
 				return isbn.charAt(12) == getIsbn13CheckDigits(isbn).charAt(0);
 			default:
 				return false;
 		}
 	}
 	
 	public static String getIsbn10CheckDigits(String isbn) {
 		int sum = 0;
 		for(int i = 0; i < 9; ++i) {
 			sum += (isbn.charAt(i) - '0') * (10 - i);
 		}
 		int m = sum % 11;
 		if(m == 1)
 			return "X";
 		else if(m == 0)
 			return "0";
 		else
 			return String.valueOf(11 - m);
 	}
 	
 	public static String toIsbn10(String isbn13) {
 		String sub = isbn13.substring(3,12);
 		return sub + getIsbn10CheckDigits(sub);
 	}
 	
 	public static String getIsbn13CheckDigits(String isbn) {
 		int sum = 0;
 		for(int i = 0; i < 12; i++) {
 			sum += (isbn.charAt(i) - '0') * (((i & 1) == 0)? 1 : 3);
 		}
 		int m = sum % 10;
		if(m==0)
 			return "0";
 		else
 			return String.valueOf(10 - sum % 10);
 	}
 	
 	public static String toIsbn13(String isbn10) {
 		String sub = "978" + isbn10.substring(0, 9);
 		return sub + getIsbn13CheckDigits(sub);
 	}
 	
 	public static String upcToIsbn(String upcPlus5) {
 		String manufacturer = upcPlus5.substring(0, 6);
 		String publisher = publishers.get(manufacturer);
 		String sub = publisher + upcPlus5.substring(12, 17);
 		return sub + getIsbn10CheckDigits(sub);
 	}
 	
 	public static String urlToString(URL url) {
 		StringBuilder builder = new StringBuilder();
 		try {
 			URLConnection conn = url.openConnection();
 			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 			String line;
 			while ((line = reader.readLine()) != null) {
 				builder.append(line);
 			}
 		}
 		catch (Exception e) {
 			Log.e(App.TAG, e.toString());
 		}
 		return builder.toString();
 	}
 	
 	public static Bitmap urlToImage(URL url) {
 		try {
 			URLConnection conn = url.openConnection();
 			InputStream is = new BufferedInputStream(conn.getInputStream());
 			Bitmap bitmap = BitmapFactory.decodeStream(is);
 			is.close();
 			return bitmap;
 		}
 		catch(Exception e) {
 			Log.e(App.TAG, e.toString());
 		}
 		return null;
 	}
 	
 	public static String htmlToText(String str) {
 		str = str.replaceAll("<{1}[^>]{1,}>{1}", "");
 		return StringEscapeUtils.unescapeHtml4(str);
 	}
 	
 	public static byte[] toByteArray(Bitmap bitmap) {
 		try {
 			ByteArrayOutputStream os = new ByteArrayOutputStream();
 			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, os);
 			return os.toByteArray();
 		}
 		catch(Exception e) {
 			Log.e(App.TAG, e.toString());
 		}
 		return null;
 	}
 }
