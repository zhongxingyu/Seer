 package services;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import utils.ImageUtils;
 import utils.ImageUtils;
 
 public class DemotivatorCreatorImpl implements DemotivatorCreator{
 	
	public DemotivatorCreatorImpl(){
		
	}
	
 
 	@Override
 	public String createDemotivator(File imageFile, String title, String text) throws IOException {
 		createImageFolderIfNeeded();
 		
 		long timestamp = System.currentTimeMillis();
 
 		//Create demotivator
 		ImageUtils utils = new ImageUtils();
 		BufferedImage image = ImageIO.read(imageFile);
 		image = utils.scale(image, MAX_WIDTH, MAX_HEIGHT);
 		image = utils.addBorderAndTextSpace(image);
         image = utils.drawTitleAndText(image, title, text);
         
         File outputfile = new File(IMAGE_DIR_PATH + File.separator + timestamp + "." + utils.getImageFormatName(imageFile));
         ImageIO.write(image, utils.getImageFormatName(imageFile), outputfile);
         
         //Create thumbnail
         BufferedImage thumb = utils.scale(image, MAX_THUMB_WIDTH, MAX_THUMB_HEIGHT);
         
         File thumbfile = new File(IMAGE_DIR_PATH + File.separator + "thumb." + timestamp + "." + utils.getImageFormatName(imageFile));
         ImageIO.write(thumb, utils.getImageFormatName(imageFile), thumbfile);
         
         return outputfile.getName();
 	}
 	
 	private void createImageFolderIfNeeded() {
 
 		File imageDir = new File(IMAGE_DIR_PATH);
 		if (!imageDir.exists()) {
 			imageDir.mkdirs();
 		}
 	}
 
 }
