 
 package com.kennyscott.resizeimages;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 
 public class Resize {
 
 	private static final String PHOTODIR = "/Users/mkns/Pictures/Photos/";
 	private static final String WWWDIR = "/Users/mkns/Pictures/www/";
 
 	/**
 	 * @param args
 	 */
 	public static void main( String[] args ) {
 		Resize r = new Resize();
 		r.execute( args );
 	}
 
 	private void execute( String[] args ) {
 		String month = parseOptions( args );
 		File photoDirectory = new File( PHOTODIR + month );
 		File wwwDirectory = new File( WWWDIR + month );
 		if ( !wwwDirectory.exists() ) {
 			wwwDirectory.mkdir();
 		}
 		File thumbnailsDirectory = new File( wwwDirectory + "/thumbnails" );
 		if ( !thumbnailsDirectory.exists() ) {
 			thumbnailsDirectory.mkdir();
 		}
 
 		String[] files = photoDirectory.list();
 		log( StringUtils.join( files, "," ) );
 
 		List<String> toBeConverted = diffDirectories( month );
 		log( "To be converted: " + ArrayUtils.toString( toBeConverted ) );
 		for ( String image : toBeConverted ) {
 			try {
 				resizeImage( args, month, image );
 			}
 			catch ( IOException e ) {
 				e.printStackTrace();
 			}
 		}
 		generateExifData( month );
 		setTimestamps( month );
 	}
 
 	private List<String> diffDirectories( String month ) {
 		File photoDirectory = new File( PHOTODIR + month );
 		File wwwDirectory = new File( WWWDIR + month );
 		List<String> directories = new ArrayList<String>();
 
 		for ( String allPhotos : photoDirectory.list() ) {
 			List<String> alreadyConverted = Arrays.asList( wwwDirectory.list() );
 			if ( !alreadyConverted.contains( allPhotos ) ) {
 				directories.add( allPhotos );
 			}
 		}
 
 		return directories;
 	}
 
 	private String parseOptions( String[] args ) {
 		Options options = new Options();
 		options.addOption( "m", true, "The month to generate" );
 		CommandLineParser parser = new PosixParser();
 		CommandLine cmd = null;
 		try {
 			cmd = parser.parse( options, args );
 		}
 		catch ( ParseException e1 ) {
 			e1.printStackTrace();
 		}
 		String month = cmd.getOptionValue( "m" );
 		log( "month: " + month );
 		return month;
 	}
 
 	@SuppressWarnings( "unused" )
 	private void getOrientationValue( String month, String image ) throws IOException {
 		String filename = PHOTODIR + month + "/" + image;
 		String targetName = WWWDIR + month + "/" + image;
 		Process Findspace = Runtime.getRuntime().exec( "exiftool " + filename + " | grep Orientation" );
 		BufferedReader Resultset = new BufferedReader( new InputStreamReader( Findspace.getInputStream() ) );
 		StringBuilder output = new StringBuilder();
 		String orientation = null;
 		String line;
 		while ( (line = Resultset.readLine()) != null ) {
 			output.append( line + "\n" );
 			if ( line.contains( "Orientation" ) ) {
 				orientation = line;
 			}
 		}
 		if ( orientation != null ) {
 			log( orientation );
 			if ( orientation.contains( "90" ) ) {
 				rotateImage( targetName, 90 );
 			}
 			else if ( orientation.contains( "180" ) ) {
 				rotateImage( targetName, 180 );
 			}
 			else if ( orientation.contains( "270" ) ) {
 				rotateImage( targetName, 270 );
 			}
 		}
 	}
 
 	private void rotateImage( String filename, int angle ) throws IOException {
 		log( "Rotating " + filename + " by " + angle );
 		File myFile = new File( filename );
 		BufferedImage img = ImageIO.read( myFile );
 
 		int w = img.getWidth();
 		int h = img.getHeight();
 		BufferedImage dimg = new BufferedImage( w, h, img.getType() );
 		Graphics2D g = dimg.createGraphics();
 		g.rotate( Math.toRadians( angle ), w / 2, h / 2 );
 		g.drawImage( img, null, 0, 0 );
 		FileOutputStream fos = new FileOutputStream( myFile );
 		ImageIO.write( dimg, "jpg", fos );
 		fos.close();
 	}
 
 	private void resizeImage( String[] args, String month, String image ) throws IOException {
 
 		File myFile = new File( PHOTODIR + month + "/" + image );
 		File ofName = new File( WWWDIR + month + "/" + image );
 		BufferedImage inputImage = ImageIO.read( myFile );

		if ( inputImage == null ) {
 			// probably because it isn't an image
 			return;
 		}

 		int height = inputImage.getHeight();
 		int width = inputImage.getWidth();
 		log( "OLD: Height: " + height + " Width: " + width );
 
 		int newWidth = 0, newHeight = 0;
		if ( height > width ) {
			newWidth = 800;
			newHeight = (int) (newWidth * ((1.0 * height) / width));
		}
		else {
			newHeight = 800;
			newWidth = (int) (newHeight * ((1.0 * width) / height));
		}
 		log( "NEW: Height: " + newHeight + " Width: " + newWidth );
 
 		Dimension tDim = new Dimension( newWidth, newHeight );
 		Image newImg = inputImage.getScaledInstance( tDim.width, tDim.height, Image.SCALE_SMOOTH );
 		BufferedImage bim = new BufferedImage( tDim.width, tDim.height, BufferedImage.TYPE_INT_RGB );
 		bim.createGraphics().drawImage( newImg, 0, 0, null );
 		FileOutputStream fos = new FileOutputStream( ofName );
 		ImageIO.write( bim, "jpg", fos );
 		fos.close();
 
 		makeThumbnail( month, image );
 	}
 
 	private void makeThumbnail( String month, String image ) throws IOException {
 		File myFile = new File( WWWDIR + month + "/" + image );
 		File ofName = new File( WWWDIR + month + "/thumbnails/" + image );
 		BufferedImage inputImage = ImageIO.read( myFile );
 		int height = inputImage.getHeight();
 		int width = inputImage.getWidth();
 		// log( "OLD: Height: " + height + " Width: " + width );
 
 		int newWidth = 0, newHeight = 0;
 		newWidth = (int) (1.0 * width) / 8;
 		newHeight = (int) (1.0 * height) / 8;
 
 		Dimension tDim = new Dimension( newWidth, newHeight );
 		Image newImg = inputImage.getScaledInstance( tDim.width, tDim.height, Image.SCALE_SMOOTH );
 		BufferedImage bim = new BufferedImage( tDim.width, tDim.height, BufferedImage.TYPE_INT_RGB );
 		bim.createGraphics().drawImage( newImg, 0, 0, null );
 		FileOutputStream fos = new FileOutputStream( ofName );
 		ImageIO.write( bim, "jpg", fos );
 		fos.close();
 	}
 
 	private void setTimestamps( String month ) {
 		try {
 			Process Findspace = Runtime.getRuntime().exec( "./set_timestamp.pl -m " + month );
 			BufferedReader Resultset = new BufferedReader( new InputStreamReader( Findspace.getInputStream() ) );
 			StringBuilder output = new StringBuilder();
 			String line;
 			while ( (line = Resultset.readLine()) != null ) {
 				output.append( line + "\n" );
 			}
 			log( "setTimestamps() output : \n" + output );
 		}
 		catch ( IOException e ) {
 			e.printStackTrace();
 		}
 	}
 
 	private void generateExifData( String month ) {
 		try {
 			Process Findspace = Runtime.getRuntime().exec( "./generate_exif_data.pl -m " + month );
 			BufferedReader Resultset = new BufferedReader( new InputStreamReader( Findspace.getInputStream() ) );
 			StringBuilder output = new StringBuilder();
 			String line;
 			while ( (line = Resultset.readLine()) != null ) {
 				output.append( line + "\n" );
 			}
 			log( "generateExifData() output : \n" + output );
 		}
 		catch ( IOException e ) {
 			e.printStackTrace();
 		}
 	}
 
 	private void log( Object o ) {
 		System.out.println( o );
 	}
 
 }
