 package mosaicAssignment1;
 
 import java.io.File;
 import java.util.ArrayList;
 
 public class MosaicMaker {
 	
 	/*
 	 * write the constructor and use the uwecimage scaling class 
 	 * 
 	 */
 	private String tileFolder;
 	private int horzMosaicSize;
 	private int vertMosaicSize;
 	private int horzNumTiles;
 	private int vertNumTiles;
 	
 	
 	public MosaicMaker(String tileFolder,int horzMosaicSize,int vertMosaicSize,int horzNumTiles,int vertNumTiles){
 		this.tileFolder = tileFolder;
 		this.horzMosaicSize = horzMosaicSize;
 		this.vertMosaicSize = vertMosaicSize;
 		this.horzNumTiles = horzNumTiles;
 		this.vertNumTiles = vertNumTiles;
 		
 		//use the file class to operate on tilefolder or someshit
 		//scale everything to the correct size (use mosaic size v & h and number of tiles to know
 		//how big the images are going to have to be
 		
 		//creates a file object of the folder path
 		File tileFiles = new File(tileFolder);
 		
 		//creats an array for files and an arraylist for images
 		String[] fileArray;
 		ArrayList<UWECImage> imageAL = new ArrayList<UWECImage>();
 		
 		//lists the files in the folder as an array of abstract pathnames and assigns to our file array
 		fileArray = tileFiles.list();
 		
 		//gets the height and width we want our tiles to be
 		int tileHeight = vertMosaicSize / vertNumTiles; 
 		int tileWidth = horzMosaicSize / horzNumTiles;
 		
 		for(int i = 0; i <fileArray.length; i++){
 			//makes our list of file pathnames into image files
			UWECImage fileToImage = new UWECImage(fileArray[i]);
 			
 			//scales our images based on our earlier calculation
 			fileToImage.scaleImage(tileWidth, tileHeight);
 			
 			//adds the scaled image to an arraylist --i am not sure this is necessary but put it here anyway--
 			imageAL.add(fileToImage);
 		
 		}
 		
 	}
 	
 
 	public UWECImage createMosaic(UWECImage image){
 		//do something to make a mosaic out of the picture 
 		
 		
 		return image;
 	}
 
 }
