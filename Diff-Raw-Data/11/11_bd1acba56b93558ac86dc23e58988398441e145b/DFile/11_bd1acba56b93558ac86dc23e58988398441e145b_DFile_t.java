 package com.phyloa.dlib.util;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.Scanner;
 
 import javax.imageio.ImageIO;
 
 public class DFile
 {
 	public static Object loadObject( String filename ) throws IOException, ClassNotFoundException
 	{
 		FileInputStream fis = new FileInputStream( filename );
 		ObjectInputStream ois = new ObjectInputStream( fis );
 		Object o = ois.readObject();
 		ois.close();
 		return o;
 	}
 	
 	public static void saveObject( String filename, Object file ) throws IOException
 	{
 		FileOutputStream fos = new FileOutputStream( filename );
 		ObjectOutputStream oos = new ObjectOutputStream( fos );
 		oos.writeObject( file );
 		oos.close();
 	}
 	
	public static String loadText( String filename ) throws FileNotFoundException
 	{
		Scanner scanner = new Scanner( new File( filename ) );
 		StringBuilder sb = new StringBuilder();
 		while( scanner.hasNext() )
 		{
			sb.append( scanner.nextLine() + "\n" );
 		}
 		return sb.toString();
 	}
 	
	public static void saveText( String filename, String text ) throws FileNotFoundException
 	{
 		FileOutputStream fos = new FileOutputStream( filename );
 	}
 	
 	public static BufferedImage loadImage( String filename ) throws IOException
 	{
 		return ImageIO.read( new File( filename ) );
 	}
 	
 	public static void saveImage( String filename, String type, BufferedImage image ) throws IOException
 	{
 		 File outputfile = new File( filename );
 		 ImageIO.write( image, type, outputfile );
 	}
 }
 
