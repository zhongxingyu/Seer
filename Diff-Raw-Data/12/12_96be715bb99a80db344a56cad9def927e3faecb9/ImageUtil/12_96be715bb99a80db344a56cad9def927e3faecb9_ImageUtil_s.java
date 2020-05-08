 package ch.zhaw.headtracker.image;
 
 public class ImageUtil {
 	private ImageUtil() {
 	}
 
 	// Returns an enlarged version of image. Image dimensions are multiplied by factor. Nearest neighbour interpolation is used.
 	public static Image scaleUp(Image image, int factor) {
 		assert factor > 0;
 
 		Image newImage = new Image(image.width * factor, image.height * factor);
 
 		for (int iy = 0; iy < image.height; iy += 1) {
 			for (int ix = 0; ix < image.width; ix += 1) {
 				int pixel = image.getPixel(ix, iy);
 
 				for (int iy2 = 0; iy2 < image.height; iy2 += 1)
 					for (int ix2 = 0; ix2 < image.width; ix2 += 1)
 						newImage.setPixel(ix * factor + ix2, iy * factor + iy2, pixel);
 			}
 		}
 
 		return newImage;
 	}
 
 	// Returns a shrinked version of image. Image dimensions are divided by factor. Result pixel values are the average of a factor by factor pixel region of the original image.
 	public static Image scaleDown(Image image, int factor) {
 		// Test whether the image's dimensions are multiples of factor 
 		assert image.width % factor == 0 && image.height % factor == 0;
 		
 		Image newImgage = new Image(image.width / factor, image.height / factor);
 
 		for (int iy = 0; iy < newImgage.height; iy += 1) {
 			for (int ix = 0; ix < newImgage.width; ix += 1) {
 				int pixel = 0;
 
 				for (int iy2 = 0; iy2 < factor; iy2 += 1)
 					for (int ix2 = 0; ix2 < factor; ix2 += 1)
						pixel += image.getPixel(ix + ix2, iy + iy2);
 
 				newImgage.setPixel(ix, iy, pixel / (factor * factor));
 			}
 		}
 
 		return newImgage;
 	}
 
 	@SuppressWarnings({ "SuspiciousNameCombination", "ConstantConditions" })
 	public static Image reorient(Image image, OrientationAction ... actions) {
 		boolean flipH = false;
 		boolean flipV = false;
 		boolean flipD = false;
 		
 		for (OrientationAction i : actions) {
 			if (i == OrientationAction.rotateLeft) {
 				boolean temp = flipH;
 				
 				flipH = !flipV;
 				flipV = temp;
 				flipD = !flipD;
 			} else if (i == OrientationAction.rotateRight) {
 				boolean temp = !flipH;
 
 				flipH = flipV;
 				flipV = temp;
 				flipD = !flipD;
 			} else if (i == OrientationAction.turnUpsideDown) {
 				flipH = !flipH;
 				flipV = !flipV;
 			} else if (i == OrientationAction.flipHorizontal) {
 				flipH = !flipH;
 			} else if (i == OrientationAction.flipVertical) {
 				flipV = !flipV;
 			}
 		}
 		
 		Image newImage = flipD ? new Image(image.height, image.width) : new Image(image.width, image.height);
 		
 		for (int iy = 0; iy < image.height; iy += 1) {
 			for (int ix = 0; ix < image.width; ix += 1) {
 				int pixel = image.getPixel(ix, iy);
 				int destX = flipH ? image.width - ix - 1 : ix;
 				int destY = flipV ? image.height - iy - 1 : iy;
 				
 				if (flipD)
 					newImage.setPixel(destY, destX, pixel);
 				else
 					newImage.setPixel(destX, destY, pixel);
 			}
 		}
 		
 		return newImage;
 	}
 
 	// Enlarge bright parts by radius
 	public static void maximum(Image image, int radius) {
 		Image tempImage = new Image(image.width, image.height);
 
 		for (int iy = 0; iy < image.height; iy += 1) {
 			for (int ix = 0; ix < image.width; ix += 1) {
 				int pixel = 0;
 				int min = Math.max(0, ix - radius);
 				int max = Math.min(image.width, ix + radius + 1);
 
 				for (int ix2 = min; ix2 < max; ix2 += 1)
 					pixel = Math.max(pixel, image.getPixel(ix2, iy));
 
 				tempImage.setPixel(ix, iy, pixel);
 			}
 		}
 
 		for (int iy = 0; iy < image.height; iy += 1) {
 			for (int ix = 0; ix < image.width; ix += 1) {
 				int pixel = 0;
 				int min = Math.max(0, iy - radius);
 				int max = Math.min(image.height, iy + radius + 1);
 
 				for (int iy2 = min; iy2 < max; iy2 += 1)
 					pixel = Math.max(pixel, tempImage.getPixel(ix, iy2));
 
 				image.setPixel(ix, iy, pixel);
 			}
 		}
 	}
 
 	// Enlarge bright parts by radius
 	public static void minimum(Image image, int radius) {
 		Image tempImage = new Image(image.width, image.height);
 
 		for (int iy = 0; iy < image.height; iy += 1) {
 			for (int ix = 0; ix < image.width; ix += 1) {
 				int pixel = 0xff;
 				int min = Math.max(0, ix - radius);
 				int max = Math.min(image.width, ix + radius + 1);
 
 				for (int ix2 = min; ix2 < max; ix2 += 1)
 					pixel = Math.min(pixel, image.getPixel(ix2, iy));
 
 				tempImage.setPixel(ix, iy, pixel);
 			}
 		}
 
 		for (int iy = 0; iy < image.height; iy += 1) {
 			for (int ix = 0; ix < image.width; ix += 1) {
 				int pixel = 0xff;
 				int min = Math.max(0, iy - radius);
 				int max = Math.min(image.height, iy + radius + 1);
 
 				for (int iy2 = min; iy2 < max; iy2 += 1)
 					pixel = Math.min(pixel, tempImage.getPixel(ix, iy2));
 
 				image.setPixel(ix, iy, pixel);
 			}
 		}
 	}
 
 	public enum OrientationAction {
 		rotateLeft, rotateRight, turnUpsideDown, flipHorizontal, flipVertical
 	}
 }
