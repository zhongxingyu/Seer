 /*
  * Copyright 2012 Christian Panadero Martinez
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package bakingcode.androidutils;
 
 import java.awt.Toolkit;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.StringSelection;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.io.IOException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 /**
  * Main class to generate code
  * 
  * @author <a href="http://bakingcode.com">@PaNaVTEC</a> 
  */
 public class Main {
 
 	/**
 	 * Starts the app.
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		// if no copied nothing from clipboard just exit the app.
 		// For this app work needs the xml layout of Android in clipboard
 		String clipboard = getClipboard();
 		if (clipboard == null || clipboard.trim() == "") {
 			return;
 		}

		Pattern p = Pattern.compile("<\\s*(\\w+)\\s*(android:\\w+=\\\"[^\\\"]*\\\"\\s*)*android:id=\\s*\\\"\\s*@(\\+id|id)/(\\w+)\\\"");
 		Matcher matcher = p.matcher(clipboard);
 		
 		StringBuilder declaration = new StringBuilder();
 		StringBuilder getter = new StringBuilder();
 		getter.append("// Assign UI fields\n");
 		
 		while (matcher.find()) {
 			
 			// Get type and name of var
 			String type = matcher.group(1);
			String name = matcher.group(4);
 			
 			// Create variable with type and var name
			declaration.append(String.format("/**\n *\n */\nprivate %s %s;\n\n", type, name));
 			getter.append(String.format("%s = (%s)findViewById(R.id.%s);\n", name, type, name));
 			
 		}
 		
 		
 		setClipboard(declaration + "\n\n\n\n" + getter);
 		
 	}
 
 	/**
 	 * If a string is on the system clipboard, this method returns it;
 	 * otherwise it returns null.
 	 * @return the string on clipboard
 	 */
 	public static String getClipboard() {
 		
 	    Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
 
 	    try {
 	    	
 	        if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
 	        	
 	            String text = (String)t.getTransferData(DataFlavor.stringFlavor);
 	            return text;
 	            
 	        }
 	        
 	    } catch (UnsupportedFlavorException e) {
 	    } catch (IOException e) {
 	    }
 	    return null;
 	    
 	}
 
 	/**
 	 * This method writes a string to the system clipboard.
 	 * otherwise it returns null.
 	 * @param str string to set on clipboard
 	 */
 	public static void setClipboard(String str) {
 		
 	    StringSelection ss = new StringSelection(str);
 	    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
 	    
 	}
 	
 }
