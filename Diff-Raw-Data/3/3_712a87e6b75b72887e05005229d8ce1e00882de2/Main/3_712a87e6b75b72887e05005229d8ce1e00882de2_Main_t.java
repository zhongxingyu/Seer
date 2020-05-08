 package za.co.bbd.unicoder;
 
 /************************************************************************
  *																		
  * Unicoder - Main.java													
  * 																		
  * Converts every character in all java source files into their unicode	
  * shorthand equivalents - the code should still compile after the 		
  * conversion.															
  * 																		
  * Copyright (C) 2012 - Riaan Van Rooyen & Johnathan Botha				
  *																		
  * This program is free software: you can redistribute it and/or modify	
  * it under the terms of the GNU General Public License as published by	
  * the Free Software Foundation, either version 3 of the License, or		
  * (at your option) any later version.									
  *																		
  * This program is distributed in the hope that it will be useful,		
  * but WITHOUT ANY WARRANTY; without even the implied warranty of		
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the			
  * GNU General Public License for more details.							
  * 																		
  * You should have received a copy of the GNU General Public License		
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.	
  * 																		
  ************************************************************************/
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 
 public class Main {
 
 	public static void main(String... args) {
 
 		if (args.length != 2) {
 
 			System.out
 					.println("Usage: java -jar Unicoder.jar <OldSrcDir> <NewSrcDir>");
 			System.exit(-1);
 
 		} else {
 
 			try {
 
 				final File dst = new File(args[1]);
 
 				copyAll(new File(args[0]), dst);
 				List<File> fles = getJavaFiles(dst);
 
 				for (File fle : fles) {
 
 					if (fle.getName().toLowerCase().endsWith(".java")) {
 
 						try {
 
 							unicodeIt(fle);
 
 						} catch (IOException e) {
 
 							e.printStackTrace();
 
 						}
 
 					}
 
 				}
 			} catch (IOException ex) {
 				
 				System.out.println("Severe error in directory copy operation!");
 				System.exit(-1);
 				
 			}
 
 		}
 
 	}
 
 	private static void unicodeIt(File fle) throws IOException {
 
 		final Pattern p = Pattern
 				.compile("(\\\\u\\d{1,4})|([\\(\\)\\+\\-\\*\\/\\{\\}\\[\\];<>\\t\\s'\"\\,]*)");
 		final String fileData = FileUtils.readFileToString(fle);
 		final Matcher m = p.matcher(fileData);
 
 		FileUtils.writeByteArrayToFile(fle, new byte[0], false);
 
 		final ByteArrayOutputStream boutS = new ByteArrayOutputStream();
 
 		int chunkEnd = 0, nextChunkStart = 0;
 
 		while (m.find()) {
 
 			chunkEnd = m.start();
 			addChunk(fileData.substring(nextChunkStart, chunkEnd), boutS);
 			nextChunkStart = m.end();
 			IOUtils.write(m.group(), boutS);
 
 		}
 
 		addChunk(fileData.substring(nextChunkStart, fileData.length()), boutS);
 
 		FileUtils.writeByteArrayToFile(fle, boutS.toByteArray());
 
 	}
 
 	private static byte[] fmtHex(int ch) {
 
 		String str = Integer.toHexString(ch);
 		char[] arr = new char[4 - str.length()];
 
 		Arrays.fill(arr, '0');
 
 		return ("\\u" + new String(arr) + str).getBytes(Charset
 				.forName("UTF-8"));
 
 	}
 
 	private static byte[] fmtHex(String str) throws IOException {
 
 		char[] data = str.toCharArray();
 		final ByteArrayOutputStream bout = new ByteArrayOutputStream();
 
 		for (int x = 0; x < str.length(); x++) {
 
 			bout.write(fmtHex((int) data[x]));
 
 		}
 
 		return bout.toByteArray();
 
 	}
 
 	private static void addChunk(String str, OutputStream outS)
 			throws IOException {
 
 		byte[] chunk = fmtHex(str);
 		IOUtils.write(chunk, outS);
 
 	}
 
 	private static List<File> getJavaFiles(final File initialDir) {
 
 		final List<File> fleArr = new ArrayList<File>();
 		fleArr.add(initialDir);
 
 		for (int i = 0; i < fleArr.size(); i++) {
 
 			if (fleArr.get(i).isDirectory()) {
 
 				fleArr.addAll(Arrays.asList(fleArr.get(i).listFiles()));
 
 			}
 
 		}
 
 		return fleArr;
 
 	}
 
 	private static void copyAll(File oldDir, File newDir) throws IOException {
 
 		FileUtils.copyDirectory(oldDir, newDir);
 
 	}
 
 }
