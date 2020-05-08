 package com.project944.cov;
 
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.awt.image.FilteredImageSource;
 import java.awt.image.ImageFilter;
 import java.awt.image.ImageProducer;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 import javax.swing.GrayFilter;
 
 public class CoverDetails {
     private final int id;
 	private Image image;
 	private String imageFilename;
 	private String artist;
 	private String album;
 	private String directory;
 	private List<TrackDetails> trackNames;
 	private int x;
 	private int y;
 	private boolean undefinedPosition;
 	private boolean hidden = false;
 	
 	private int discNumber = 1;
 	private List<CoverDetails> otherDiscs = null;
 	private boolean partOfOtherCover = false;  // If is disc 2 of something else
 	
 	private int cachedScaledImageSize = -1;
 	private Image cachedScaledImage;
 	private Image cachedGreyScaledImage;
     private boolean noImage;
     private boolean variousArtists;
 	
 	
 	public CoverDetails(int id, String artist, String album, Image image, int discNumber, boolean noImage) {
 	    this.id = id;
 	    this.artist = artist;
 	    this.album = album;
 	    this.noImage = noImage;
 	    setImage(image);
 	    this.discNumber = discNumber;
 	}
 	
 	public CoverDetails(int id, String directory, String imageFilename, String artist, String album) throws IOException {
 	    this.id = id;
 		this.directory = directory;
 		this.imageFilename = imageFilename;
 		this.artist = artist;
 		this.album = album;
 		
 		try {
 		    URL url;
 			String filename = imageFilename;
 			if ( imageFilename.startsWith("/com/project944/cov/resources") ) {
 				url = CoverDetails.class.getResource(imageFilename);
 				filename = url.getFile();
 			} else {
 			    url = new URL(filename);
 			}
 			this.image = ImageIO.read(url);
 		} catch  (IOException e) {
 			e.printStackTrace();
 			throw e;
 		}
 	}
 	
 	public int getId() {
         return id;
     }
 	
 	public void setTrackNames(List<TrackDetails> trackNames) {
         this.trackNames = trackNames;
     }
 	
 	public List<TrackDetails> getTrackNames() {
 	    return trackNames;
 	}
 	
 	public Image getImage() {
 		if ( image == null ) {
 			System.out.println("Got null image for "+imageFilename+", "+artist+", "+album);
 		}
 		return image;
 	}
 
 	public Image getSmallImage(int sz) {
 		if ( image == null ) {
 			System.out.println("Got null image for "+imageFilename+", "+artist+", "+album);
 			return null;
 		}
 		if ( cachedScaledImageSize != sz ) {
 			cachedScaledImageSize = sz;
 			cachedGreyScaledImage = null;  // Free this up
 			cachedScaledImage = new BufferedImage(sz, sz, BufferedImage.TYPE_INT_RGB);
 			Graphics g = cachedScaledImage.getGraphics();
 			Graphics g2 = g.create(0, 0, sz, sz);
 			g2.drawImage(getImage(), 0, 0, sz, sz, 0, 0, getWidth(), getHeight(), null);
 		}
 		return cachedScaledImage;
 	}
 	
 	public int getWidth() {
 		return getImage().getWidth(null);
 	}
 
 	public int getHeight() {
 		return getImage().getHeight(null);
 	}
 
 	public String getArtist() { return variousArtists?"Various":artist; }
 	
 	public String getAlbum() { return album; }
 
 	public String getDirName() {
 		return directory;
 	}
 
 	public void setXY(int x2, int y2) {
 		x = x2;
 		y = y2;
 	}
 
 	public int getX() {
 		return x;
 	}
 
 	public int getY() {
 		return y;
 	}
 	
 	public boolean isUndefinedPosition() {
 		return undefinedPosition;
 	}
 	
 	public void setUndefinedPosition(boolean val) {
 		undefinedPosition = val;
 	}
 
     public void setImage(Image image) {
         this.image = image;
         if ( this.image != null && this.image instanceof BufferedImage && ((BufferedImage)this.image).getWidth() > PreviewCoverPanel.IMAGE_SIZE ) {
             this.image = getSmallImage(PreviewCoverPanel.IMAGE_SIZE);
         } else {
             if ( image != null && !(image instanceof BufferedImage) ) {
                 System.out.println("No shrink, type is "+image.getClass().getName());
             }
         }
     }
     
     public boolean isHidden() {
         return hidden;
     }
     
     public void setHidden(boolean hidden) {
         this.hidden = hidden;
     }
 
     public void setNoImage(boolean noImage) {
         this.noImage = noImage;
     }
     public boolean isNoImage() {
         return noImage;
     }
     
     public int getDiscNumber() {
         return discNumber;
     }
     
 
     public boolean isPartOfOtherCover() {
         return partOfOtherCover;
     }
     
     public static String trimDisc(String albumTitle) {
         return albumTitle.replaceAll(" \\(disc [0-9]+\\)$", "");
     }
     
     public List<CoverDetails> getOtherDiscs() {
         return otherDiscs;
     }
 
     public boolean addExtraDisc(CoverDetails other) {
         // Only add if matching artist and album (except for the (disc x) bit at end)
         if ( other.getArtist().equals(getArtist()) && trimDisc(other.getAlbum()).equals(trimDisc(album)) ) {
             if ( discNumber == 1 && other.discNumber > discNumber ) {
                 other.partOfOtherCover = true;
                 if ( otherDiscs == null ) {
                     otherDiscs = new LinkedList<CoverDetails>();
                 }
                 if ( !otherDiscs.contains(other) ) {
                     otherDiscs.add(other);
                     if ( otherDiscs.size() > 1 ) {
                         Collections.sort(otherDiscs, new Comparator<CoverDetails>() {
                             public int compare(CoverDetails o1, CoverDetails o2) {
                                 if ( o1.discNumber < o2.discNumber ) {
                                     return -1;
                                 }
                                 if ( o1.discNumber > o2.discNumber ) {
                                     return 1;
                                 }
                                 return 0;
                             }
                         });
                     }
                 }
                 return true;
             }
         }
         return false;
     }
     
     public static void fixMultiDiscAlbums(List<CoverDetails> covers) {
         for (CoverDetails cover : covers) {
             if ( cover.getDiscNumber() > 1 ) {
                 for (CoverDetails parent : covers) {
                     if ( parent.addExtraDisc(cover) ) {
                         break;
                     }
                 }
             }
         }
     }
 
     public Image getSmallGreyImage(Component component, int sz) {
         if ( cachedGreyScaledImage == null ) {
             ImageFilter filter = new GrayFilter(true, 60);  
             ImageProducer producer = new FilteredImageSource(cachedScaledImage.getSource(), filter);  
             cachedGreyScaledImage = component.createImage(producer); // greyed out
         }
         return cachedGreyScaledImage;
     }
 
     public void setVariousArtists(boolean variousArtists) {
         this.variousArtists = variousArtists;
     }
     public boolean isVariousArtists() {
         return variousArtists;
     }
 }
