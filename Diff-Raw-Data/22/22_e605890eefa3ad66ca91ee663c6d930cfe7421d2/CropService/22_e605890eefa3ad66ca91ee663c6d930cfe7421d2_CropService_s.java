 package com.megalogika.sv.service;
 
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.image.BufferedImage;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import javax.imageio.ImageIO;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.util.Assert;
 
 import com.megalogika.sv.model.Photo;
 import com.megalogika.sv.model.Transformation;
 
 @Service("cropService")
 public class CropService {
 
 	protected transient static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(CropService.class);
 
 	@Autowired
 	private File destDir;
 
 	public PhotoDimension resize(Photo photo) throws Exception {
 		return transform(photo, false);
 	}
 
 	public PhotoDimension rotate(Photo photo) throws Exception {
 		return transform(photo, true);
 	}
 	
 	public void rotateIngredientPhoto(Photo photo) throws Exception {
 		BufferedImage inImage = getImage(photo.getOriginalPhoto());
 		
 		int w = inImage.getWidth();
 		int h = inImage.getHeight();
 		logger.debug("CropService.rotateIngredients(): [w;h] = [" + w + ";" + h + "]");
 
 		inImage = transformImage(inImage, w, h, photo.getCropping().getRotation(), new File(destDir, getCroppedFilePath(photo)));
 		photo.setResizedPhoto(getCroppedFilePath(photo));
 		
 		photo.updateMeasurements(inImage);
 	}
 	
 	private PhotoDimension transform(Photo photo, boolean rotate) throws Exception {
 		BufferedImage inImage = getImage(photo.getOriginalPhoto());
 
 		int w = inImage.getWidth();
 		int h = inImage.getHeight();
 
 		logger.debug("CropService.transform(): [w;h] = [" + w + ";" + h + "]");
 		int newW = 0;
 		int newH = 0;
 		if (noResizeNeeded(photo, w, h)) {
 			if (!rotate) {
 				photo.setResizedPhoto(photo.getOriginalPhoto());
 				adjustCropping(new PhotoDimension(w, h), photo.getCropping());
 				return new PhotoDimension(w, h);
 			} else {
 				newW = w;
 				newH = h;
 			}
 		} else { 
 			if (w > h) {
 				newW = photo.getCropping().getMaxWidth();
 				newH = resizeHbyW(photo, w, h);
 			} else {
 				newW = resizeWbyH(photo, w, h);
 				newH = photo.getCropping().getMaxHeight();
 			}
 		}
 
 		inImage = transformImage(inImage, newW, newH, photo.getCropping().getRotation(), new File(destDir, getCroppedFilePath(photo)));
 		photo.setResizedPhoto(getCroppedFilePath(photo));
 
 		PhotoDimension rez = null;
 		if (photo.getCropping().getRotation() == 90 || photo.getCropping().getRotation() == 270) {
 			rez = new PhotoDimension(newH, newW);
 		} else {
 			rez = new PhotoDimension(newW, newH);
 		}
 		adjustCropping(rez, photo.getCropping());
 
 		return rez;
 	}
 
 	private String getCroppedFilePath(Photo photo) {
 		return photo.getOriginalPhoto().replace(".", "_resized.");
 	}
 	
 	public PhotoDimension resizeAndCrop(Photo photo) throws Exception {
 		Assert.notNull(photo);
 
 		Transformation cropping = photo.getCropping();
 		if (null == cropping) {
 			cropping = new Transformation();
 			photo.setCropping(cropping);
 			logger.debug("NEW CROPPING ASSIGNED: " + photo);
 		}
 		// cropping.reset();
 		cropping.setRotation(0);
 
 		PhotoDimension rez = resize(photo);
 		crop(photo);
 		
 		photo.assignDimensions(rez);
 		
 		return rez;
 	}	
 	
 	public void crop(Photo photo) throws Exception {
 		logger.debug("CropService.crop(): photo is:" + photo);
 		logger.debug("CropService.crop(): crop is:" + photo.getCropping());
 		logger.debug("CropService.crop(): photoOrigPhoto is:" + photo.getOriginalPhoto());
 		logger.debug("CropService.crop(): photoResized is:" + photo.getResizedPhoto());
 		logger.debug("CropService.crop(): photoPhoto is:" + photo.getPhoto());
 		logger.debug("CropService.crop(): destDir:" + destDir);
 		
 		if (photo.getCropping().getHeight() == photo.getHeight() &&
 			photo.getCropping().getWidth() == photo.getWidth()) 
 		{
 			logger.debug("NO CROPPING NEEDED!");
 			
 			photo.setResizedPhoto(photo.getOriginalPhoto());
 			return;
 		}
 		
 		try {
 			String originalFilePath = photo.getOriginalPhoto();
 			BufferedImage inImage = getImage(originalFilePath);
 
 			inImage = rotate(photo.getCropping().getRotation(), inImage);
 			float coef = Math.min(((float) inImage.getWidth() / (float) getImage(photo.getResizedPhoto()).getWidth()),
 								  ((float) inImage.getHeight() / (float) getImage(photo.getResizedPhoto()).getHeight()));
 
 			logger.debug("CropService.crop(): coef is:" + coef);
 			logger.debug("CropService.crop(): photo.getCropping().getX1()*coef:" + photo.getCropping().getX1() * coef + " floor:"
 					+ Math.floor(photo.getCropping().getX1() * coef));
 
 			logger.debug("CropService.crop(): photo.getCropping().getY1()*coef:" + photo.getCropping().getY1() * coef + " floor:"
 					+ Math.floor(photo.getCropping().getY1() * coef));
 
 			logger.debug("CropService.crop(): photo.getCropping().getWidth()*coef:" + photo.getCropping().getWidth() * coef + " floor:"
 					+ Math.floor(photo.getCropping().getWidth() * coef));
 
 			logger.debug("CropService.crop(): photo.getCropping().getHeight()*coef:" + photo.getCropping().getHeight() * coef + " floor:"
 					+ Math.floor(photo.getCropping().getHeight() * coef));
 
 			logger.debug("CropService.crop(): image dims: w: " + inImage.getWidth() + " h: " + inImage.getHeight());
 			
 			inImage = inImage.getSubimage((int) Math.floor(photo.getCropping().getX1() * coef), 
 										  (int) Math.floor(photo.getCropping().getY1() * coef),
 										  (int) Math.floor(photo.getCropping().getWidth() * coef), 
 										  (int) Math.floor(photo.getCropping().getHeight() * coef));
 
 			logger.debug("CropService.crop(): cropping done");
 
 			String croopedFilePath = originalFilePath.replace(".", "_cropped.");
 			inImage = scaleRect(inImage, (photo.getCropping().getMaxHeight() > photo.getCropping().getMaxWidth() ? photo.getCropping().getMaxHeight() : photo.getCropping().getMaxWidth()), new File(destDir, croopedFilePath));
 			photo.setPhoto(croopedFilePath);
 
 			logger.debug("CropService.crop(): croppedFile is:" + photo.getPhoto());
 			logger.debug("CropService.crop(): file saved" + photo);
 		} catch (Exception e) {
 			logger.error("CropService.crop() Error, exception occured: ", e);
 			throw e;
 		}
 	}
 	
 	public void cropIngredients(Photo photo) throws Exception {
 		logger.debug("CropService.cropIngredients(): photo is:" + photo);
 		logger.debug("CropService.cropIngredients(): crop is:" + photo.getCropping());
 		logger.debug("CropService.cropIngredients(): photoOrigPhoto is:" + photo.getOriginalPhoto());
 		logger.debug("CropService.cropIngredients(): photoResized is:" + photo.getResizedPhoto());
 		logger.debug("CropService.cropIngredients(): photoPhoto is:" + photo.getPhoto());
 		logger.debug("CropService.cropIngredients(): destDir:" + destDir);
 		
		if (photo.getCropping().getHeight() == photo.getHeight() &&
			photo.getCropping().getWidth() == photo.getWidth()) 
		{
			logger.debug("Ingredients: NO CROPPING NEEDED!");
			
			photo.setResizedPhoto(photo.getOriginalPhoto());
			return;
		}
		
 		try {
 			String originalFilePath = photo.getOriginalPhoto();
 			BufferedImage inImage = getImage(originalFilePath);
 
 
 			inImage = rotate(photo.getCropping().getRotation(), inImage);
 			float xscale = ((float) inImage.getWidth() / (float) getImage(photo.getResizedPhoto()).getWidth());
 			float yscale = ((float) inImage.getHeight() / (float) getImage(photo.getResizedPhoto()).getHeight());
 
 			logger.debug("CropService.cropIngredients(): xscale is:" + xscale + " yscale is: " + yscale);
 			logger.debug("CropService.cropIngredients(): photo.getCropping().getX1()*xscale:" + photo.getCropping().getX1() * xscale + " floor:"
 					+ Math.floor(photo.getCropping().getX1() * xscale));
 
 			logger.debug("CropService.cropIngredients(): photo.getCropping().getY1()*yscale:" + photo.getCropping().getY1() * yscale + " floor:"
 					+ Math.floor(photo.getCropping().getY1() * yscale));
 
 			logger.debug("CropService.cropIngredients(): photo.getCropping().getWidth()*xscale:" + photo.getCropping().getWidth() * xscale + " floor:"
 					+ Math.floor(photo.getCropping().getWidth() * xscale));
 
 			logger.debug("CropService.cropIngredients(): photo.getCropping().getHeight()*yscale:" + photo.getCropping().getHeight() * yscale + " floor:"
 					+ Math.floor(photo.getCropping().getHeight() * yscale));
 
 			
 			inImage = inImage.getSubimage((int) Math.floor(photo.getCropping().getX1() * xscale), 
 										  (int) Math.floor(photo.getCropping().getY1() * yscale),
 										  (int) Math.floor(photo.getCropping().getWidth() * xscale), 
 										  (int) Math.floor(photo.getCropping().getHeight() * yscale));
 
 			logger.debug("CropService.cropIngredients(): cropping done");
 
 			String croppedFilePath = originalFilePath.replace(".", "_cropped.");
 			writeImageToFile(inImage, new File(destDir, croppedFilePath));
 			photo.setPhoto(croppedFilePath);
 
 			logger.debug("CropService.cropIngredients(): croppedFile is:" + photo.getPhoto());
 			logger.debug("CropService.cropIngredients(): file saved" + photo);
 		} catch (Exception e) {
 			logger.error("CropService.cropIngredients() Error, exception occured: ", e);
 			throw e;
 		}
 	}	
 
 	public long currentTimestamp() {
 		return System.currentTimeMillis();
 	}
 
 	protected BufferedImage scaleRect(BufferedImage img, int maxDim, File outputFile) throws IOException {
 		return transformImage(img, maxDim, maxDim, 0, outputFile);
 	}
 
 	protected BufferedImage transformImage(BufferedImage sourceImage, int wDim, int hDim, double rotation, File outputFile) throws IOException {
 		logger.debug("CropService.scale(): rotation is:" + rotation);
 		logger.debug("CropService.scale(): wDim is:" + wDim);
 		logger.debug("CropService.scale(): hDim is:" + hDim);
 
 		BufferedImage scaledImage = new BufferedImage(wDim, hDim, BufferedImage.TYPE_INT_RGB);
 
 		Graphics2D g2d = createGraphics(scaledImage);
 
 		logger.debug("CropService.scale(): coef: " + wDim / (float) sourceImage.getWidth());
 		g2d.drawImage(sourceImage, 0, 0, wDim, hDim, 0, 0, sourceImage.getWidth(), sourceImage.getHeight(), null);
 		g2d.dispose();
 		scaledImage.flush();
 
 		BufferedImage destImage = rotate(rotation, scaledImage);
 
 		writeImageToFile(destImage, outputFile);
 		return destImage;
 	}
 
 	private void writeImageToFile(BufferedImage image, File outputFile) throws IOException {
 		OutputStream os = new BufferedOutputStream(new FileOutputStream(outputFile));
 		ImageIO.write(image, "jpeg", os);
 		os.close();
 	}
 	
 	private BufferedImage rotate(double rotation, BufferedImage sourceImage) {
 		BufferedImage destImage;
 		if (0 != rotation) {
 			int wDim = sourceImage.getWidth();
 			int hDim = sourceImage.getHeight();
 			destImage = new BufferedImage(Math.max(hDim, wDim), Math.max(hDim, wDim), BufferedImage.TYPE_INT_RGB);
 			logger.debug("CropService.rotate(): anchorW is:" + destImage.getWidth() / 2.0);
 			logger.debug("CropService.rotate(): anchorH is:" + destImage.getHeight() / 2.0);
 			logger.debug("CropService.rotate(): wDim is:" + destImage.getWidth());
 			logger.debug("CropService.rotate(): hDim is:" + destImage.getHeight());
 
 			Graphics2D g2d = createGraphics(destImage);
 			g2d.rotate(Math.toRadians(rotation), destImage.getWidth() / 2.0, destImage.getHeight() / 2.0);
 			g2d.drawImage(sourceImage, 0, 0, wDim, hDim, 0, 0, sourceImage.getWidth(), sourceImage.getHeight(), null);
 			g2d.dispose();
 
 			destImage.flush();
 
 			if (hDim > wDim) {
 				if (rotation == 90) {
 					destImage = destImage.getSubimage(0, 0, hDim, wDim);
 				} else if (rotation == 180) {
 					destImage = destImage.getSubimage(hDim - wDim, 0, wDim, hDim);
 				} else if (rotation == 270) {
 					destImage = destImage.getSubimage(0, hDim - wDim, hDim, wDim);
 				}
 			} else if (hDim < wDim) {
 				if (rotation == 90) {
 					destImage = destImage.getSubimage(wDim - hDim, 0, hDim, wDim);
 				} else if (rotation == 180) {
 					destImage = destImage.getSubimage(0, wDim - hDim, wDim, hDim);
 				} else if (rotation == 270) {
 					destImage = destImage.getSubimage(0, 0, hDim, wDim);
 				}
 			}
 			return destImage;
 		} else {
 			logger.debug("NO ROTATION NEEDED!");
 			return sourceImage;
 		}
 	}
 
 	private void adjustCropping(PhotoDimension rez, Transformation cropping) {
 		int w = rez.getWidth();
 		int h = rez.getHeight();
 		logger.debug("CropService.adjustCropping(): [w;h] = [" + w + ";" + h + "]");
 		if (w < h) {
 			cropping.setHeight(w);
 			cropping.setWidth(w);
 			cropping.setX1(0);
 			cropping.setX2(w);
 			cropping.setY1((h - w) / 2);
 			cropping.setY2(cropping.getY1() + w);
 		} else {
 			cropping.setHeight(h);
 			cropping.setWidth(h);
 			cropping.setX1((w - h) / 2);
 			cropping.setX2(cropping.getX1() + h);
 			cropping.setY1(0);
 			cropping.setY2(h);
 		}
 		logger.debug("CropService.adjustCropping(): cropping is:" + cropping);
 	}
 
 	private int resizeHbyW(Photo photo, int w, int h) {
 		logger.debug("CropService.resizeHbyW(): coef is:" + photo.getCropping().getMaxHeight() / (float) w);
 		return (int) Math.floor(h * photo.getCropping().getMaxHeight() / (float) w);
 	}
 
 	private int resizeWbyH(Photo photo, int w, int h) {
 		logger.debug("CropService.resizeWbyH(): coef is:" + photo.getCropping().getMaxWidth() / (float) h);
 		return (int) Math.floor(w * photo.getCropping().getMaxWidth() / (float) h);
 	}
 
 	private boolean noResizeNeeded(Photo photo, int w, int h) {
 		boolean ret = h <= photo.getCropping().getMaxHeight() && w <= photo.getCropping().getMaxWidth();
 		logger.debug("SHOULD RESIZE? : " + ret + " BECAUSE: " + photo.getCropping().getMaxHeight() + " <> " + h + " | "+ photo.getCropping().getMaxWidth() + " <> " + w);
 		return ret;
 	}
 
 	public BufferedImage getImage(String fileName) throws IOException, CropServiceException {
 		logger.debug("CropService.getImage(): file is:" + fileName);
 		if (null == fileName) {
 			throw new CropServiceException("Nenurodytas failo vardas!");
 		}
 		return ImageIO.read(new File(destDir, fileName));
 	}
 
 	private Graphics2D createGraphics(BufferedImage image) {
 		Graphics2D g2d = image.createGraphics();
 		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
 		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
 		return g2d;
 	}	
 	
 	public File getDestDir() {
 		return destDir;
 	}
 
 	public void setDestDir(File destDir) {
 		this.destDir = destDir;
 	}
 
}
