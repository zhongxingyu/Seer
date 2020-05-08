 package com.phyloa.dlib.util;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.PrintWriter;
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
		scanner.close();
 		return sb.toString();
 	}
 	
 	public static void saveText( String filename, String text ) throws FileNotFoundException
 	{
 		FileWriter outFile = null;
 		try
 		{
 			outFile = new FileWriter( filename );
 		} catch( IOException e )
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		PrintWriter out = new PrintWriter( outFile );
 		out.print( text );
 		
 		out.close();
 		try
 		{
 			outFile.close();
 		} catch( IOException e )
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
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
 	
 	public static int[][] loadCSV( String filename, int x, int y, int width, int height ) throws IOException
 	{
 		String text = DFile.loadText( filename );
 		int[][] vals = new int[width][height];
 		String[] lines = text.split( "\n" );
 		String[][] svals = new String[lines.length][];
 		for( int i = 0; i < lines.length; i++ )
 		{
 			svals[i] = lines[i].split( "," );
 		}
 		
 		for( int xx = 0; xx < width; xx++ )
 		{
 			for( int yy = 0; yy < height; yy++ )
 			{
 				vals[xx][yy] = Integer.parseInt( svals[xx+x][yy+y] );
 				
 			}
 		}
 		
 		return vals;
 	}
 }
 
