 package org.abratuhi.snippettool.util;
 
 import java.awt.Point;
 import java.awt.Polygon;
 import java.awt.Rectangle;
 import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
 import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 
 import org.abratuhi.snippettool.model.InscriptCharacter;
 
 public class ImageUtil {
 
 	public static File[] cutSnippets(File inputImageFile, ArrayList<InscriptCharacter> characters, String directory, String basename){
 		if(!directory.endsWith(File.separator)) directory += File.separator;
 		File[] outputImageFiles = new File[characters.size()];
 		BufferedImage inputImage = load(inputImageFile);
 		for(int i=0; i<characters.size(); i++){
 			InscriptCharacter ch = characters.get(i);
 			BufferedImage outputImage = cutImage(inputImage, ch.shape.main, ch.shape.center, ch.shape.angle);
 			outputImageFiles[i] = new File(directory + basename + "_" + characters.get(i).inscript.id+"_"+characters.get(i).getNumber()+".png");
 			store(outputImage, "PNG", outputImageFiles[i]);
 		}
 		return outputImageFiles;
 	}
 	
 	public static BufferedImage cutImage(BufferedImage inputImage, Polygon shape, Point center, float angle){
 		Rectangle border = shape.getBounds();
		
		double dx = border.getWidth();
		double dy = border.getHeight();
		for(int i=0; i<shape.npoints; i++){
			int nexti = i<shape.npoints-1? i+1 : 0;
			double distx = Line2D.ptLineDist(shape.xpoints[i], shape.ypoints[i], shape.xpoints[nexti], shape.ypoints[nexti], border.getX(), border.getY()+border.getHeight());
			double disty = Line2D.ptLineDist(shape.xpoints[i], shape.ypoints[i], shape.xpoints[nexti], shape.ypoints[nexti], border.getX(), border.getY());
			if(distx < dx) dx = distx;
			if(disty < dy) dy = disty;
		}
		
		BufferedImage boundaryImage = inputImage.getSubimage(Math.max(0, border.x), 
 				Math.max(0, shape.getBounds().y), 
 				Math.min(Math.max(1, shape.getBounds().width), inputImage.getWidth()-border.x), 
 				Math.min(Math.max(1, shape.getBounds().height), inputImage.getHeight()-border.y));
 		AffineTransformOp filter = new AffineTransformOp(AffineTransform.getRotateInstance(angle, center.x-border.x, center.y-border.y), AffineTransformOp.TYPE_BICUBIC);
		BufferedImage rotatedImage = new BufferedImage(boundaryImage.getWidth(), boundaryImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
		filter.filter(boundaryImage, rotatedImage);
		BufferedImage cutrotatedImage = rotatedImage.getSubimage((int)dx, (int)dy, (int)(rotatedImage.getWidth()-2*dx), (int)(rotatedImage.getHeight()-2*dy));
		return cutrotatedImage;
		//return rotatedImage;
 	}
 
 	public static void store(BufferedImage image, String format, File f){
 		try {
 			ImageIO.write(image, format, f);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static BufferedImage load(File f){
 		try {
 			return ImageIO.read(f);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 }
