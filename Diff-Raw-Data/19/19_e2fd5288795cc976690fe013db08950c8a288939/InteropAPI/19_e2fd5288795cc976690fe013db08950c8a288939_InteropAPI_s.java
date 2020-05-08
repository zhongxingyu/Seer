 /* 
  * License: source-license.txt
  * If this code is used independently, copy the license here.
  */
 
 package wombat.scheme.util;
 
 import wombat.scheme.libraries.*;
 import wombat.scheme.libraries.types.ImageData;
 import wombat.scheme.libraries.types.TreeData;
 import wombat.util.Base64;
 
 /**
  * Helper that stores all of the interop functions for calling Java methods from Scheme.
  */
 public class InteropAPI {
 	public static String SchemeCD = null;
 	
 	private InteropAPI() {}
 	
 	/**
 	 * Interop method.
 	 * @param key The method name.
 	 * @param val Any parameters.
 	 * @return Either null to send nothing or a string to send back.
 	 */
 	public static String interop(String key, String val) {
 		key = key.toLowerCase();
 		
 		try {
 			// ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~ 
 			// Image API
 			// ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~ 
 			
 			if ("read-image".equals(key)) {
 				ImageData img = null;
 				if (val == null) {
 					img = ImageAPI.readImage();
 				} else {
 					int split = val.indexOf("\" \"");
 					String cd = val.substring(1, split);
 					String fn = val.substring(split + 3, val.length() - 1);
 				
 					img = ImageAPI.readImage(cd, fn);
 				}
 				
 				return "(" + img.Width + " " + img.Height + " \"" + Base64.encodeBytes(Conversion.int2byte(img.Data)) + "\")";
 			} 
 			
 			// write images to file
 			else if ("write-image".equals(key)) {
 				String cd = null;
 				if (val.startsWith("\"")) {
 					int split = val.indexOf('"', 2);
 					cd = val.substring(1, split);
 					val = val.substring(split + 2, val.length());
 				}
 				
 				String[] args = val.split(" ");
 				ImageData img = new ImageData(
 					Integer.parseInt(args[0]),
 					Integer.parseInt(args[1]),
					Conversion.byte2int(Base64.decode(args[2].substring(1, args[2].length() - 1)))
 				);
 				
 				if (cd == null)
 					ImageAPI.writeImage(img);
 				else
 					ImageAPI.writeImage(cd, img, args[3].substring(1, args[3].length() - 1));
 			}
 			
 			// display images to the screen
 			else if ("draw-image".equals(key)) {
 				String[] args = val.split(" ");
 				ImageData img = new ImageData(
 					Integer.parseInt(args[0]),
 					Integer.parseInt(args[1]),
					Conversion.byte2int(Base64.decode(args[2].substring(1, args[2].length() - 1)))
 				);
 				
 				ImageAPI.displayImage(img);
 			}
 			
 			// ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~ 
 			// Matrix API
 			// ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~
 			else if ("draw-matrix".equals(key)) {
 				MatrixAPI.drawMatrix(val);
 			}
 			
 			// ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~ 
 			// Tree API
 			// ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~
 			else if ("draw-tree".equals(key)) {
 				TreeAPI.drawTree(TreeData.decode(val));
 			}
 			
 			// ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~ 
 			// Meta API
 			// ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~
 			else if ("set-cd".equals(key)) {
 				SchemeCD = val;
 			}
 			
 			// ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~ 
 			// Test API
 			// ~~~~~ ~~~~~ ~~~~~ ~~~~~ ~~~~~
 			
 			// Test method.
 			else if ("fact".equals(key)) {
 				int n = Integer.parseInt(val);
 				int a = 1;
 				for (int i = 2; i <= n; i++) a *= i;
 				return "(" + a + ")";
 			}
 			
 		} catch(Exception e) {
 			e.printStackTrace();
 			System.out.println("(exception " + key + " \"" + e.getMessage().replace("\\", "\\\\") + "\")");
 			return "(exception " + key + " \"" + e.getMessage().replace("\\", "\\\\") + "\")";
 		}
 		
 		return "()";
 	}
 }
