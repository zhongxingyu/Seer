 package net.jnickg.acamedia.fil;
 
 import java.util.*;
 import java.io.*;
 import com.itextpdf.text.pdf.*;
 
 public class PdfFile
 {
 	private	PdfReader	reader;
 	private	PdfWriter	writer;
 	private	File		file;
 	
 	public PdfFile(String filepath, PrintStream out)
 	{
 		try
 		{
 			out.println("attempting to make PdfReader...");
 			reader = new PdfReader(filepath);
 			out.println("attempting to get info...");
 			Map<String, String> info = reader.getInfo();
 			for(String s: info.keySet())
 			{
 				out.println(s + ": " + info.get(s));
 			}
 			out.println(reader.getPdfVersion());
 		}
		catch(IOException e)
 		{
 			out.println(e.getMessage());
 		}
 	}
 }
