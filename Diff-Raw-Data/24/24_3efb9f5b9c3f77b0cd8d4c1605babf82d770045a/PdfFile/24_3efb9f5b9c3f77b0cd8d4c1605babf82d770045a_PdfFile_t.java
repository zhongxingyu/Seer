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
		catch(Exception e)
 		{
			out.println("SOMETHING WENT WRONG!!! EXCEPTION");
 			out.println(e.getMessage());
			out.println(e.getLocalizedMessage());
			out.println(e.getCause().toString());
			out.println(e.getStackTrace().toString());
		}
		catch(Error e)
		{
			out.println("SOMETHING WENT WRONG!!! ERROR");
			out.println(e.getMessage());
			out.println(e.getLocalizedMessage());
			out.println(e.getCause().toString());
			out.println(e.getStackTrace().toString());

 		}
 	}
 }
