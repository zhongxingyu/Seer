 package mlproject.models;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Date;
 
 import javax.imageio.ImageIO;
 
 public class Issue{
 	@IgnoreField public String mImagePath;
 	public boolean imageAttached;
 	
 	public Double sales;
 	public Double expectedSales;
 	public Long Issue;
 	public Date date;
 	public String heading;
 	@TargetField public Boolean aboveExpected;
 	public Boolean earthScience, astronomyAndCosmology, physics, technology, neuroSciencePsychology;
 	public Boolean otherBiology, otherTopic, abstractImage, photoImage, typographicImage;
 	public Boolean hasSpecial, isSpecialEdition, isAnniverseryEdition;
 	
 	public Double avgRed = 0.0, avgGreen = 0.0, avgBlue = 0.0;
 	
	
 	public double getPercent(){
 		if(sales != null && expectedSales != null) return sales / expectedSales;
 		return 1;
 	}
 	
 	
 	public void extractImageFeatures(String img) throws IOException{
 		imageAttached = true;
 		File file= new File(img);
 	    BufferedImage image = ImageIO.read(file);
 	    int maxX = image.getWidth();
 	    int maxY = image.getHeight();
 	    int numPixels = maxX * maxY;
 	    int redSum = 0, greenSum = 0, blueSum = 0;
 	    for(int x = 0; x < maxX; x++){
 	    	for(int y = 0; y < maxY; y++){
 	    		int clr = image.getRGB(x, y);
 	    		redSum += ((clr & 0x00ff0000) >> 16);
 	    		greenSum += ((clr & 0x0000ff00) >> 8);
 	    		blueSum += (clr & 0x000000ff);
 	    	}
 	    }
 	    avgRed = redSum / (double)numPixels;
 	    avgGreen = greenSum / (double)numPixels;
 	    avgBlue = blueSum / (double)numPixels;
 	    
 	    
 		
 		//TODO: Matthew to do more.
 	}
 
 
 	public boolean shouldOwn(Date d) {
 		//System.out.println("ME " + date.toGMTString() + " vs " + d.toGMTString());
 		return d.getYear() == date.getYear() && d.getMonth() == date.getMonth() && d.getDay() == date.getDay();
 		
 	}	
 }
