 package image.csu.fullerton.edu;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Random;
 
 import javax.imageio.ImageIO;
 
 public class Image {
 
     private static int[][] imageRGBHistogram(BufferedImage input) {
 
         int[][] histogram = new int[3][256];
 
         for(int i=0; i<histogram.length; i++)
         	for (int j=0; j<histogram[i].length; j++)
         		histogram[i][j] = 0;
 
         for(int i=0; i<input.getWidth(); i++) {
             for(int j=0; j<input.getHeight(); j++) {
             	Color c = new Color(input.getRGB (i, j));
             	histogram[0][c.getRed()]++;
             	histogram[1][c.getGreen()]++;
             	histogram[2][c.getBlue()]++;
             }
         }
 
        if (true) {
 	        for(int i=0; i<histogram.length; i++) {
 	        	for (int j=0; j<histogram[i].length; j++) {
 	        		System.out.printf("%04d ", histogram[i][j]);
 	        	}
 	        	System.out.printf("\n");
 	        }
         }
 
         return histogram;
 
     }
     
     static int getHistogramMean(BufferedImage image) {
     	int[][] hist = imageRGBHistogram(image);
     	double mean = 0;
     	double count = 0;
     	
     	for (int c=0; c < hist.length; c++) {
     		for (int x=0; x<hist[c].length; x++) {
    			mean += hist[c][x]; 
     		}
     	}
     	
    	count = (hist.length + 1) * hist[0].length;

     	mean = mean/count;
    	mean = 68.3;
     	System.out.printf("mean: %.2f\n", mean);
     	return (int)mean;
     }
     
     private static double clamp(double v, double min, double max) {
     	if (v > max) {
     		return max;
     	} else if (v < min) {
     		return min;
     	}
     	return v;
     }
     
     private static double mapValue(double c, int max) {
     	double value;
     	
    	value = c / 255.0;
     	
    	value = clamp(value, 0.0, 1.0);
     	
    	value = value * max;
     	
     	return value;
     }
     
     private static int mapRGB(int rgb, int newMax) {
     	// adapted from GEGL's gimpoperationlevels
     	// Note: gamma set to 1.0 which simplifies the math
     	int r, g, b, a;
     	Color c = new Color(rgb);
     	r = c.getRed();
     	g = c.getGreen();
     	b = c.getBlue();
     	a = c.getAlpha();
     	double value;
     	
     	value = mapValue((double)r, newMax);
    	value = mapValue(value, newMax);
     	r = (int)value;
     	
     	value = mapValue((double)g, newMax);
    	value = mapValue(value, newMax);
     	g = (int)value;
     	
     	value = mapValue((double)b, newMax);
    	value = mapValue(value, newMax);
     	b = (int)value;
     	
     	Color newPixel = new Color(r, g, b, a);
     	
     	return newPixel.getRGB();
     }
     
     static BufferedImage stretchLevels(BufferedImage currentImage, int newMax) {
 		System.out.printf("stretchLevels\n");
 		BufferedImage newImage = null;
 		if (currentImage != null) {
 			int width = currentImage.getWidth();
 			int height = currentImage.getHeight();
 			newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
 			
 			for (int x=0; x < width; x++) {
 				for (int y=0; y < height; y++) {
 					newImage.setRGB(x, y, mapRGB(currentImage.getRGB(x, y), newMax));
 				}
 			}
 		}
 		return newImage;
     }
     
     private static boolean nearBlack(Color c) {
     	int threshold = 16;
     	return ( (c.getRed() < threshold) &&
     			(c.getGreen() < threshold) &&
     			(c.getBlue() < threshold));
     }
     
     static BufferedImage maskByEdge(BufferedImage currentImage, BufferedImage edgeImage) {
 		System.out.printf("maskByEdge\n");
 		BufferedImage newImage = null;
 		if (currentImage != null) {
 			newImage = new BufferedImage(currentImage.getWidth(),
 					currentImage.getHeight(), BufferedImage.TYPE_INT_RGB);
 			{
 				int width = edgeImage.getWidth();
 				int height = edgeImage.getHeight();
 				int lx = 0, uy = 0, rx = width, ly = height;
 				int replacement = 0xFFFFFFFF;
 				newImage.setRGB(0, 0, width, height, currentImage.getRGB(0, 0, width, height, null, 0, width), 0, width);
 				// find left margin
 				for (int x = 0; x < width; x++) {
 					boolean clear = true;
 					for (int y = 0; y < height; y++) {
 						Color c = new Color(edgeImage.getRGB(x, y));
 						if (!nearBlack(c)) {
 							clear = false;
 							break;
 						} else {
 							newImage.setRGB(x, y, replacement);
 						}
 					}
 					if (!clear) {
 						if (x - 1 < lx)
 							lx = x - 1;
 					}
 				}
 				// find right margin
 				for (int x = width-1; x > 0; x--) {
 					boolean clear = true;
 					for (int y = height-1; y > 0; y--) {
 						Color c = new Color(edgeImage.getRGB(x, y));
 						if (!nearBlack(c)) {
 							clear = false;
 							break;
 						} else {
 							newImage.setRGB(x, y, replacement);
 						}
 					}
 					if (!clear) {
 						if (x+1 > rx)
 							rx = x + 1;
 					}
 				}
 				// find top margin
 				for (int y = 0; y < height; y++) {
 					boolean clear = true;
 					for (int x = 0; x < width; x++) {
 						Color c = new Color(edgeImage.getRGB(x, y));
 						if (!nearBlack(c)) {
 							clear = false;
 							break;
 						} else {
 							newImage.setRGB(x, y, replacement);
 						}
 					}
 					if (!clear) {
 						if (y-1 < uy)
 							uy = y - 1;
 						
 					}
 				}
 				// find bottom margin
 				for (int y = height - 1; y > 0; y--) {
 					boolean clear = true;
 					for (int x = width-1; x > 0; x--) {
 						Color c = new Color(edgeImage.getRGB(x, y));
 						if (!nearBlack(c)) {
 							clear = false;
 							break;
 						} else {
 							newImage.setRGB(x, y, replacement);
 						}
 					}
 					if (!clear) {
 						if (y+1 > ly)
 							ly = y + 1;
 					}
 				}
 				for (int x=1; x < width - 2; x++) {
 					for (int y=1; y < height - 2; y++) {
 						Color i = new Color(newImage.getRGB(x,y));
 						int j = newImage.getRGB(x-1,y-1);
 						int k = newImage.getRGB(x+1,y-1);
 						int l = newImage.getRGB(x-1,y+1);
 						int m = newImage.getRGB(x+1,y+1);
 						if ((nearBlack(i)) && (j == 0xFFFFFFFF) && (j==k) && (l==m) && (j==l)) {
 							System.out.printf("(%d,%d) is single black pixel", x,y);
 							newImage.setRGB(x, y, replacement);
 						}
 					}
 				}
 				//newImage.setRGB(0,  0, width, height, edgeImage.getRGB(0, 0, width, height, null, 0, width), 0, width);
 			}
 		}
 		return newImage;
     }
 
 	static BufferedImage removeBackground(BufferedImage currentImage) {
 		System.out.printf("removeBackground\n");
 		BufferedImage newImage = null;
 		if (currentImage != null) {
 			if (true) {
 				BufferedImage edgeImage;
 				
 				//edgeImage = cannyEdgeDetectImage(currentImage);
 				edgeImage = sobelEdgeDetectImage(currentImage);
 				//edgeImage = meanShift(edgeImage);
 				//edgeImage = thresholdImage(edgeImage, otsuThreshold(edgeImage));
 				edgeImage = cannyEdgeDetectImage(edgeImage);
 				newImage = maskByEdge(currentImage, edgeImage);
 			} else {
 				// stupid idea
 				BufferedImage bgImage;
 				File f = new File("c:/ordata/background.jpg");
 				try {
 					bgImage = ImageIO.read(f);
 					newImage = new BufferedImage(currentImage.getWidth(),
 							currentImage.getHeight(), BufferedImage.TYPE_INT_RGB);
 					int[][] bgHist = imageRGBHistogram(bgImage);
 					for (int x=0; x < currentImage.getWidth(); x++) { 
 						for (int y=0; y < currentImage.getHeight(); y++) {
 							Color c = new Color(currentImage.getRGB(x, y));
 							if ((bgHist[0][c.getRed()] > 0) &&
 									(bgHist[1][c.getGreen()] > 0) &&
 									(bgHist[2][c.getBlue()] > 0)) {
 								newImage.setRGB(x, y, 0xFFFFFFFF);
 							} else {
 								newImage.setRGB(x, y, c.getRGB());
 							}
 						}
 					}
 					//int[][] fgHist = imageRBGHistogram(currentImage);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return newImage;
 	}
 	
 	static BufferedImage desaturateImage(BufferedImage currentImage) {
 		System.out.printf("desaturateImage\n");
 		BufferedImage newImage = null;
 		if (currentImage != null) {
 			newImage = new BufferedImage(currentImage.getWidth(),
 					currentImage.getHeight(), BufferedImage.TYPE_INT_RGB);
 			for (int y = 0; y < currentImage.getHeight(); y++) {
 				for (int x = 0; x < currentImage.getWidth(); x++) {
 					Color pixel = new Color(currentImage.getRGB(x, y));
 					int b = pixel.getBlue();
 					int g = pixel.getGreen();
 					int r = pixel.getRed();
 					int a = pixel.getAlpha();
 					/* well-known formula, documented in many places */
 					double grayValue = 0.3 * r + 0.59 * g + 0.11 * b;
 					int gray = (0x000000FF & (int) grayValue);
 					int finalGray = gray | gray << 8 | gray << 16;
 					int newPixel = (a << 24) | finalGray;
 					newImage.setRGB(x, y, newPixel);
 				}
 			}
 		}
 		return newImage;
 	}
 
 	static BufferedImage desaturateLightnessImage(BufferedImage currentImage) {
 		System.out.printf("desaturateLightnessImage\n");
 		BufferedImage newImage = null;
 		if (currentImage != null) {
 			newImage = new BufferedImage(currentImage.getWidth(),
 					currentImage.getHeight(), BufferedImage.TYPE_INT_RGB);
 			for (int y = 0; y < currentImage.getHeight(); y++) {
 				for (int x = 0; x < currentImage.getWidth(); x++) {
 					float min, max, value;
 					Color pixel = new Color(currentImage.getRGB(x, y));
 					// formula from GEGL's gimpoperationdesaturate.c
 					int b = pixel.getBlue();
 					int g = pixel.getGreen();
 					int r = pixel.getRed();
 					int a = pixel.getAlpha();
 					max = Math.max(r,g);
 					max = Math.max(max,b);
 					min = Math.min(r,g);
 					min = Math.min(min,b);
 					value = (max + min)/2;
 					r = (int)value;
 					g = (int)value;
 					b = (int)value;
 					Color newPixel = new Color(r, g, b, a);
 					newImage.setRGB(x, y, newPixel.getRGB());
 				}
 			}
 		}
 		return newImage;
 	}
 
 	static BufferedImage downscaleImage(BufferedImage currentImage, int newWidth, int newHeight) {
 		System.out.printf("downscaleImage\n");
 		BufferedImage scaledImage = null;
 		if (currentImage != null) {
 			scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
 			Graphics2D graphics2D = scaledImage.createGraphics();
 			float scale_x = (float) newWidth / (float) currentImage.getWidth();
 			float scale_y = (float) newHeight / (float) currentImage.getHeight();
 			AffineTransform xform = AffineTransform.getScaleInstance(scale_x, scale_y);
 			graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
 			graphics2D.drawImage(currentImage, xform, null);
 			graphics2D.dispose();
 		}
 		return scaledImage;
 	}
 
 	/**
 	 * Bilinear resize ARGB image.
 	 * pixels is an array of size w * h.
 	 * Target dimension is w2 * h2.
 	 * w2 * h2 cannot be zero.
 	 * 
 	 * @param pixels Image pixels.
 	 * @param w Image width.
 	 * @param h Image height.
 	 * @param w2 New width.
 	 * @param h2 New height.
 	 * @return New array with size w2 * h2.
 	 */
 	private static int[] resizeBilinear(int[] pixels, int w, int h, int w2, int h2) {
 	    int[] temp = new int[w2*h2] ;
 	    int a, b, c, d, x, y, index ;
 	    float x_ratio = ((float)(w-1))/w2 ;
 	    float y_ratio = ((float)(h-1))/h2 ;
 	    float x_diff, y_diff, blue, red, green ;
 	    int offset = 0 ;
 	    for (int i=0;i<h2;i++) {
 	        for (int j=0;j<w2;j++) {
 	            x = (int)(x_ratio * j) ;
 	            y = (int)(y_ratio * i) ;
 	            x_diff = (x_ratio * j) - x ;
 	            y_diff = (y_ratio * i) - y ;
 	            index = (y*w+x) ;                
 	            a = pixels[index] ;
 	            b = pixels[index+1] ;
 	            c = pixels[index+w] ;
 	            d = pixels[index+w+1] ;
 
 	            // blue element
 	            // Yb = Ab(1-w)(1-h) + Bb(w)(1-h) + Cb(h)(1-w) + Db(wh)
 	            blue = (a&0xff)*(1-x_diff)*(1-y_diff) + (b&0xff)*(x_diff)*(1-y_diff) +
 	                   (c&0xff)*(y_diff)*(1-x_diff)   + (d&0xff)*(x_diff*y_diff);
 
 	            // green element
 	            // Yg = Ag(1-w)(1-h) + Bg(w)(1-h) + Cg(h)(1-w) + Dg(wh)
 	            green = ((a>>8)&0xff)*(1-x_diff)*(1-y_diff) + ((b>>8)&0xff)*(x_diff)*(1-y_diff) +
 	                    ((c>>8)&0xff)*(y_diff)*(1-x_diff)   + ((d>>8)&0xff)*(x_diff*y_diff);
 
 	            // red element
 	            // Yr = Ar(1-w)(1-h) + Br(w)(1-h) + Cr(h)(1-w) + Dr(wh)
 	            red = ((a>>16)&0xff)*(1-x_diff)*(1-y_diff) + ((b>>16)&0xff)*(x_diff)*(1-y_diff) +
 	                  ((c>>16)&0xff)*(y_diff)*(1-x_diff)   + ((d>>16)&0xff)*(x_diff*y_diff);
 
 	            temp[offset++] = 
 	                    0xff000000 | // hardcode alpha
 	                    ((((int)red)<<16)&0xff0000) |
 	                    ((((int)green)<<8)&0xff00) |
 	                    ((int)blue) ;
 	        }
 	    }
 	    return temp ;
 	}
 	
 	static BufferedImage smoothScale(BufferedImage currentImage, int newWidth, int newHeight) {
 		System.out.printf("smoothScale\n");
 		
 		BufferedImage scaledImage = null;
 		if (currentImage != null) {
 			int w = currentImage.getWidth();
 			int h = currentImage.getHeight();
 			scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
 			int[] pixels = currentImage.getRGB(0,  0, w, h, null, 0, w);
 			pixels = resizeBilinear(pixels, w, h, newWidth, newHeight);
 			scaledImage.setRGB(0, 0, newWidth, newHeight, pixels, 0, newWidth);
 		}
 		return scaledImage;
 	}
 	
 	static boolean blackBackground(BufferedImage currentImage) {
 		System.out.printf("blackBackground\n");
 		boolean isBlack = false;
 		
 		if (currentImage != null) {
 			int width = currentImage.getWidth();
 			int height = currentImage.getHeight();
 			Color[] colors = new Color[4];
 			colors[0] = new Color(currentImage.getRGB(1, 1));
 			colors[1] = new Color(currentImage.getRGB(width-2, 1));
 			colors[2] = new Color(currentImage.getRGB(1, height-2));
 			colors[3] = new Color(currentImage.getRGB(width-2, height-2));
 			Color black = new Color(0);
 			isBlack = true;
 			for (int i=0; i < 4; i++) {
 				if (!black.equals(colors[i])) {
 					isBlack = false;
 				}
 			}
 		}
 		return isBlack;
 	}
 	static BufferedImage cropImage(BufferedImage currentImage) {
 		System.out.printf("cropImage\n");
 		BufferedImage newImage = null;
 		if (currentImage != null) {
 			int width = currentImage.getWidth();
 			int height = currentImage.getHeight();
 			Color[] colors = new Color[4];
 			Color matchedColor = null;
 			colors[0] = new Color(currentImage.getRGB(1, 1));
 			colors[1] = new Color(currentImage.getRGB(width-1, 1));
 			colors[2] = new Color(currentImage.getRGB(1, height-1));
 			colors[3] = new Color(currentImage.getRGB(width-1, height-1));
 			int numMatches = 0;
 			for (int i=0; i < 4; i++) {
 				for (int j=i; j < 4; j++) {
 					if (colors[i].equals(colors[j])) {
 						numMatches++;
 						if (numMatches >= 3) {
 							matchedColor = colors[i];
 						}
 					}
 				}
 			}
 			if (numMatches >= 3) {
 				int lx = 0, uy = 0, rx = width, ly = height;
 				// find left margin
 				for (int x = 1; x < width; x++) {
 					boolean clear = true;
 					for (int y = 0; y < height; y++) {
 						Color c = new Color(currentImage.getRGB(x, y));
 						if (!matchedColor.equals(c)) {
 							clear = false;
 						}
 					}
 					if (!clear) {
 						lx = x - 1;
 						if (lx > 10) lx -= 10; // give a slight border for edge detection
 						break;
 					}
 				}
 				// find right margin
 				for (int x = width-1; x > lx; x--) {
 					boolean clear = true;
 					for (int y = 0; y < height; y++) {
 						Color c = new Color(currentImage.getRGB(x, y));
 						if (!matchedColor.equals(c)) {
 							clear = false;
 						}
 					}
 					if (!clear) {
 						rx = x + 1;
 						if (rx < width-10) rx += 10; // give a slight border for edge detection
 						break;
 					}
 				}
 				// find top margin
 				for (int y = 1; y < height; y++) {
 					boolean clear = true;
 					for (int x = 0; x < width; x++) {
 						Color c = new Color(currentImage.getRGB(x, y));
 						if (!matchedColor.equals(c)) {
 							clear = false;
 						}
 					}
 					if (!clear) {
 						uy = y - 1;
 						if (uy > 10) uy -= 10; // give a slight border for edge detection
 						break;
 					}
 				}
 				// find bottom margin
 				for (int y = height - 1; y > uy; y--) {
 					boolean clear = true;
 					for (int x = 0; x < width; x++) {
 						Color c = new Color(currentImage.getRGB(x, y));
 						if (!matchedColor.equals(c)) {
 							clear = false;
 						}
 					}
 					if (!clear) {
 						ly = y + 1;
 						if (ly < height - 10) ly += 10; // give a slight border for edge detection
 						break;
 					}
 				}
 				
 				System.out.printf("w/h: %d,%d; (%d,%d)(%d,%d)\n", width, height, lx, uy, rx, ly);
 				newImage = currentImage.getSubimage(lx, uy, rx-lx, ly-uy);
 			} else {
 				newImage = currentImage;
 			}
 		}
 		return newImage;
 	}
 	
 	static BufferedImage sobelEdgeDetectImage(BufferedImage currentImage) {
 		/* http://users.ecs.soton.ac.uk/msn/book/new_demo/sobel/ */
 		BufferedImage newImage = null;
 		System.out.printf("sobelEdgeDetectImage\n");
 		
 		if (currentImage != null) {
 			float template[] = {-1,0,1,-2,0,2,-1,0,1};
 			int width, height;
 			width = currentImage.getWidth();
 			height = currentImage.getHeight();
 			newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
 			
 			float[] GY = new float[width*height];
 			float[] GX = new float[width*height];
 			int[] total = new int[width*height];
 			int sum=0;
 			int max=0;
 			int templateSize=3;
 			int[] input = new int[width*height];
 			int[] output = new int[width*height];
 			double[] direction = new double[width*height];
 			
 			currentImage.getRGB(0, 0, width, height, input, 0, width);
 			
 			for(int x=(templateSize-1)/2; x<width-(templateSize+1)/2;x++) {
 				for(int y=(templateSize-1)/2; y<height-(templateSize+1)/2;y++) {
 					sum=0;
 
 					for(int x1=0;x1<templateSize;x1++) {
 						for(int y1=0;y1<templateSize;y1++) {
 							int x2 = (x-(templateSize-1)/2+x1);
 							int y2 = (y-(templateSize-1)/2+y1);
 							float value = (input[y2*width+x2] & 0xff) * (template[y1*templateSize+x1]);
 							sum += value;
 						}
 					}
 					GY[y*width+x] = sum;
 					for(int x1=0;x1<templateSize;x1++) {
 						for(int y1=0;y1<templateSize;y1++) {
 							int x2 = (x-(templateSize-1)/2+x1);
 							int y2 = (y-(templateSize-1)/2+y1);
 							float value = (input[y2*width+x2] & 0xff) * (template[x1*templateSize+y1]);
 							sum += value;
 						}
 					}
 					GX[y*width+x] = sum;
 
 				}
 			}
 			for(int x=0; x<width;x++) {
 				for(int y=0; y<height;y++) {
 					total[y*width+x]=(int)Math.sqrt(GX[y*width+x]*GX[y*width+x]+GY[y*width+x]*GY[y*width+x]);
 					direction[y*width+x] = Math.atan2(GX[y*width+x],GY[y*width+x]);
 					if(max<total[y*width+x])
 						max=total[y*width+x];
 				}
 			}
 			float ratio=(float)max/255;
 			for(int x=0; x<width;x++) {
 				for(int y=0; y<height;y++) {
 					sum=(int)(total[y*width+x]/ratio);
 					output[y*width+x] = 0xff000000 | ((int)sum << 16 | (int)sum << 8 | (int)sum);
 				}
 			}
 			newImage.setRGB(0, 0, width, height, output, 0, width);
 		}
 		
 		return newImage;
 	}
 	
 	static BufferedImage edgeDetectImage(BufferedImage currentImage) {
 		/*
 		 * http://processing.org/learning/topics/edgedetection.html
 		 * modified in that it does an inaccurate grayscale on a color image
 		 */
 		BufferedImage newImage = null;
 		System.out.printf("edgeDetectImage\n");
 		if (currentImage != null) {
 			newImage = new BufferedImage(currentImage.getWidth(), currentImage.getHeight(), BufferedImage.TYPE_INT_RGB);
 			float filter[][] = { { -1, -1, -1 }, { -1, 9, -1 }, { -1, -1, -1 } };
 			for (int y = 1; y < currentImage.getHeight() - 1; y++) {
 				for (int x = 1; x < currentImage.getWidth() - 1; x++) {
 					float sum = 0;
 					for (int dy = -1; dy <= 1; dy++) {
 						for (int dx = -1; dx <= 1; dx++) {
 							int avgVal;
 							int r, g, b;
 							Color pixel = new Color(currentImage.getRGB(x, y));
 							b = pixel.getBlue();
 							g = pixel.getGreen();
 							r = pixel.getRed();
 							avgVal = (b + g + r) / 3;
 							float val = (float) avgVal;
 							sum += filter[dy + 1][dx + 1] * val;
 						}
 					}
 					int newPixel = (int)sum;
 					if (newPixel > 255) newPixel = 255;
 					if (newPixel < 0) newPixel = 0;
 					Color finalPixel = new Color(newPixel,newPixel,newPixel); 
 					newImage.setRGB(x, y, finalPixel.getRGB());
 				}
 			}
 		}
 		return newImage;
 	}
 	
 	static BufferedImage cannyEdgeDetectImage(BufferedImage currentImage) {
 		System.out.printf("cannyEdgeDetectImage\n");
 		BufferedImage newImage = null;
 		if (currentImage != null) {
 			newImage = new BufferedImage(currentImage.getWidth(),
 					currentImage.getHeight(), BufferedImage.TYPE_INT_RGB);
 			CannyEdgeDetector detector = new CannyEdgeDetector();
 			detector.setLowThreshold(0.5f);
 			detector.setHighThreshold(6f);
 			detector.setSourceImage(currentImage);
 			detector.process();
 			newImage = detector.getEdgesImage();
 		}
 		return newImage;
 	}
 	
 	static BufferedImage valueInvertImage(BufferedImage currentImage) {
 		System.out.printf("valueInvertImage\n");
 		BufferedImage newImage = null;
 		if (currentImage != null) {
 			newImage = new BufferedImage(currentImage.getWidth(), currentImage.getHeight(), BufferedImage.TYPE_INT_RGB);
 			for (int y = 0; y < currentImage.getHeight(); y++) {
 				for (int x = 0; x < currentImage.getWidth(); x++) {
 					int r, g, b, a;
 					int value, value2, min, delta;
 					Color pixel = new Color(currentImage.getRGB(x, y));
 					b = pixel.getBlue();
 					g = pixel.getGreen();
 					r = pixel.getRed();
 					a = pixel.getAlpha();
 					// adapted from gimp's value-invert plugin
 					if (r > g) {
 						value = Math.max(r,b);
 						min = Math.min(g,b);
 					} else {
 						value = Math.max(g,b);
 						min = Math.min(r,b);
 					}
 					delta = value - min;
 					if ((value==0)||(delta==0)) {
 						r = 255 - value;
 						g = 255 - value;
 						b = 255 - value;
 					} else {
 						value2 = value / 2;
 						if (r == value) {
 							r = 255 - r;
 							b = ((r*b) + value2) / value;
 							g = ((r*g) + value2) / value;
 						} else if (g == value) {
 							g = 255 - g;
 							r = ((g*r) + value2) / value;
 							b = ((g*b) + value2) / value;
 						} else {
 							b = 255 - b;
 							g = ((b*g) + value2) / value;
 							r = ((b*r) + value2) / value;
 						}
 					}
 					Color newPixel = new Color(r, g, b, a);
 					newImage.setRGB(x, y, newPixel.getRGB());
 				}
 			}
 		}
 		return newImage;
 	}
 
 	static BufferedImage invertImage(BufferedImage currentImage) {
 		System.out.printf("invertImage\n");
 		BufferedImage newImage = null;
 		if (currentImage != null) {
 			newImage = new BufferedImage(currentImage.getWidth(), currentImage.getHeight(), BufferedImage.TYPE_INT_RGB);
 			int[] lookup = new int[256];
 			for (int i = 0; i < 256; i++) {
 				lookup[i] = 255 - i;
 			}
 			for (int y = 0; y < currentImage.getHeight(); y++) {
 				for (int x = 0; x < currentImage.getWidth(); x++) {
 					int r, g, b, a;
 					Color pixel = new Color(currentImage.getRGB(x, y));
 					b = pixel.getBlue();
 					g = pixel.getGreen();
 					r = pixel.getRed();
 					a = pixel.getAlpha();
 					Color newPixel = new Color(lookup[r], lookup[g], lookup[b], a);
 					newImage.setRGB(x, y, newPixel.getRGB());
 				}
 			}
 		}
 		return newImage;
 	}
 
 
     // Return histogram of grayscale image
 	// source: http://zerocool.is-a-geek.net/?p=376
     private static int[] imageHistogram(BufferedImage input) {
 
         int[] histogram = new int[256];
 
         for(int i=0; i<histogram.length; i++) histogram[i] = 0;
 
         for(int i=0; i<input.getWidth(); i++) {
             for(int j=0; j<input.getHeight(); j++) {
                 int red = new Color(input.getRGB (i, j)).getRed();
                 histogram[red]++;
             }
         }
 
         return histogram;
 
     }
     
     // Get binary threshold using Otsu's method
 	// source: http://zerocool.is-a-geek.net/?p=376
     static int otsuThreshold(BufferedImage currentImage) {
 		System.out.printf("otsuThreshold\n");
         int threshold = 0;
     	
 		if (currentImage != null) {
         int[] histogram = imageHistogram(currentImage);
         int total = currentImage.getHeight() * currentImage.getWidth();
 
         float sum = 0;
         for(int i=0; i<256; i++) sum += i * histogram[i];
 
         float sumB = 0;
         int wB = 0;
         int wF = 0;
 
         float varMax = 0;
 
         for(int i=0 ; i<256 ; i++) {
             wB += histogram[i];
             if(wB == 0) continue;
             wF = total - wB;
 
             if(wF == 0) break;
 
             sumB += (float) (i * histogram[i]);
             float mB = sumB / wB;
             float mF = (sum - sumB) / wF;
 
             float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);
 
             if(varBetween > varMax) {
                 varMax = varBetween;
                 threshold = i;
             }
         }
 
 		}
         return threshold;
 
     }
     
     static BufferedImage thresholdImage(BufferedImage currentImage, int threshold) {
 		System.out.printf("thresholdImage\n");
 		BufferedImage newImage = null;
 		if (currentImage != null) {
 			newImage = new BufferedImage(currentImage.getWidth(),
 					currentImage.getHeight(), BufferedImage.TYPE_INT_RGB);
 			for (int y = 0; y < currentImage.getHeight(); y++) {
 				for (int x = 0; x < currentImage.getWidth(); x++) {
 					int avgVal;
 					int a, r, g, b;
 					int finalPixel;
 					Color pixel = new Color(currentImage.getRGB(x, y));
 					b = pixel.getBlue();
 					g = pixel.getGreen();
 					r = pixel.getRed();
 					a = pixel.getAlpha();
 					avgVal = (b + g + r) / 3;
 
 					if (avgVal >= threshold) {
 						finalPixel = 0x00FFFFFF | a << 24;
 					} else {
 						finalPixel = 0x00000000 | a << 24;
 					}
 					newImage.setRGB(x, y, finalPixel);
 				}
 			}
 		}
 		return newImage;
 	}
     
 	static BufferedImage meanShift(BufferedImage currentImage) {
 		System.out.printf("meanShift\n");
 		BufferedImage newImage = null;
 
 		if (currentImage != null) {
 			newImage = new BufferedImage(currentImage.getWidth(),
 					currentImage.getHeight(), BufferedImage.TYPE_INT_RGB);
 			int width = currentImage.getWidth();
 			int height = currentImage.getHeight();
 			int[] pixels = (int[]) currentImage.getRGB(0, 0, width, height, null, 0, width);
 			float[][] pixelsf = new float[width * height][3];
 			int rad = 6, rad2 = rad*rad;
 			float radCol = 25, radCol2 = radCol*radCol;
 
 			for (int i = 0; i < pixelsf.length; i++) {
 				int argb = pixels[i];
 
 				int r = (argb >> 16) & 0xff;
 				int g = (argb >> 8) & 0xff;
 				int b = (argb) & 0xff;
 
 				pixelsf[i][0] = 0.299f * r + 0.587f * g + 0.114f * b; // Y
 				pixelsf[i][1] = 0.5957f * r - 0.2744f * g - 0.3212f * b; // I
 				pixelsf[i][2] = 0.2114f * r - 0.5226f * g + 0.3111f * b; // Q
 			}
 
 			float shift = 0;
 			int iters = 0;
 			for (int y = 0; y < height; y++) {
 				for (int x = 0; x < width; x++) {
 
 					int xc = x;
 					int yc = y;
 					int xcOld, ycOld;
 					float YcOld, IcOld, QcOld;
 					int pos = y * width + x;
 					float[] yiq = pixelsf[pos];
 					float Yc = yiq[0];
 					float Ic = yiq[1];
 					float Qc = yiq[2];
 
 					iters = 0;
 					do {
 						xcOld = xc;
 						ycOld = yc;
 						YcOld = Yc;
 						IcOld = Ic;
 						QcOld = Qc;
 
 						float mx = 0;
 						float my = 0;
 						float mY = 0;
 						float mI = 0;
 						float mQ = 0;
 						int num = 0;
 
 						for (int ry = -rad; ry <= rad; ry++) {
 							int y2 = yc + ry;
 							if (y2 >= 0 && y2 < height) {
 								for (int rx = -rad; rx <= rad; rx++) {
 									int x2 = xc + rx;
 									if (x2 >= 0 && x2 < width) {
 										if (ry * ry + rx * rx <= rad2) {
 											yiq = pixelsf[y2 * width + x2];
 
 											float Y2 = yiq[0];
 											float I2 = yiq[1];
 											float Q2 = yiq[2];
 
 											float dY = Yc - Y2;
 											float dI = Ic - I2;
 											float dQ = Qc - Q2;
 
 											if (dY * dY + dI * dI + dQ * dQ <= radCol2) {
 												mx += x2;
 												my += y2;
 												mY += Y2;
 												mI += I2;
 												mQ += Q2;
 												num++;
 											}
 										}
 									}
 								}
 							}
 						}
 						float num_ = 1f / num;
 						Yc = mY * num_;
 						Ic = mI * num_;
 						Qc = mQ * num_;
 						xc = (int) (mx * num_ + 0.5);
 						yc = (int) (my * num_ + 0.5);
 						int dx = xc - xcOld;
 						int dy = yc - ycOld;
 						float dY = Yc - YcOld;
 						float dI = Ic - IcOld;
 						float dQ = Qc - QcOld;
 
 						shift = dx * dx + dy * dy + dY * dY + dI * dI + dQ * dQ;
 						iters++;
 					} while (shift > 3 && iters < 100);
 
 					int r_ = (int) (Yc + 0.9563f * Ic + 0.6210f * Qc);
 					int g_ = (int) (Yc - 0.2721f * Ic - 0.6473f * Qc);
 					int b_ = (int) (Yc - 1.1070f * Ic + 1.7046f * Qc);
 
 					pixels[pos] = (0xFF << 24) | (r_ << 16) | (g_ << 8) | b_;
 				}
 
 			}
 			newImage.setRGB(0, 0, width, height, pixels, 0, width);
 		}
 		return newImage;
 	}
 
 	/* segmentation algorithm from: http://code.google.com/p/cse160a3/source/browse/trunk/cse160a3/src/Segmentation.java */
 	static BufferedImage segmentImage(BufferedImage currentImage) {
 		System.out.printf("segmentImage\n");
 		BufferedImage newImage = null;
 
 		if (currentImage != null) {
 			newImage = new BufferedImage(currentImage.getWidth(),
 					currentImage.getHeight(), BufferedImage.TYPE_INT_RGB);
 			int width = currentImage.getWidth();
 			int height = currentImage.getHeight();
 			int imagePixels[]; // Array of pixel values from input image, one
 								// int for each color/pixel.
 			int labels[];
 			int pixelWidth;
 
 			imagePixels = currentImage.getRaster().getPixels(0, 0, width,
 					height, (int[]) null);
 
 			// Space between two pixels in the imagePixel array.
 			pixelWidth = currentImage.getSampleModel().getNumDataElements();
 
 			// Apply initial labels (each label is it's own index in the label
 			// array + 1)
 			labels = new int[width * height];
 
 			for (int i = 0; i < width * height; i++) {
 				labels[i] = 0;
 			}
 
 			// Label each pixel as a separate label.
 			for (int i = 0; i < height; i++) {
 				for (int j = 0; j < width; j++) {
 
 					int idx = (i * width + j);
 					int idx3 = idx * pixelWidth;
 
 					// Comment this line if you want to label background pixels
 					if (imagePixels[idx3] == 0)
 						continue;
 
 					labels[idx] = idx + 1;
 
 				}
 			}
 
 			int threshold = 4;
 
 			int pix[] = imagePixels;
 			int maxN = Math.max(width, height);
 
 	        int phases = (int) Math.ceil(Math.log(maxN) / Math.log(2)) + 1;
 	        System.out.println("Ok, " + (phases+1) + " phases scheduled...");
 
 	        boolean changed = true;
 	        int max;
 	        for (int pp = 0; pp <= phases && changed; pp++) {
 	                changed = false;
 	            // pass one. Find neighbors with better labels.
 	            for (int i = height - 1; i >= 0; i--) {
 	                for (int j = width - 1; j >= 0; j--) {
 	                    int idx = i*width + j;
 	                    int idx3 = idx*pixelWidth;
 
 	                    if (labels[idx] == 0) 
 	                        continue;
 
 	                    int ll = labels[idx]; // save previous label
 
 	                    // pixels are stored as 3 ints in "pix" array. we just use the first of them. 
 	                    // Compare with each neighbor
 	                    if (i != height - 1 && 
 	                            Math.abs(pix[((i+1)*width + j)*pixelWidth] - pix[idx3]) < threshold){
 	                        max = Math.max(labels[idx], labels[(i+1)*width + j]);
 	                        if (max != labels[idx])
 	                                changed = true;
 	                        labels[idx] = max;
 	                    }
 
 	                    if (i != 0 && 
 	                            Math.abs(pix[((i-1)*width + j)*pixelWidth] - pix[idx3]) < threshold){
 	                        max = Math.max(labels[idx], labels[(i-1)*width + j]);
 	                        if (max != labels[idx])
 	                                changed = true;
 	                        labels[idx] = max;
 	                    }
 
 	                    if (i != height - 1 && j != width - 1 && 
 	                            Math.abs(pix[((i+1)*width + j + 1)*pixelWidth] - pix[idx3]) < threshold){
 	                        max = Math.max(labels[idx], labels[(i+1) * width + j + 1]);
 	                        if (max != labels[idx])
 	                                changed = true;
 	                        labels[idx] = max;
 	                    }
 
 	                    if (i != 0 && j != width - 1 && 
 	                            Math.abs(pix[((i-1) * width + j + 1)*pixelWidth] - pix[idx3]) < threshold){
 	                        max = Math.max(labels[idx], labels[(i-1) * width + j + 1]);
 	                        if (max != labels[idx])
 	                                changed = true;
 	                        labels[idx] = max;
 	                    }
 
 	                    if (i != height - 1 && j != 0 && 
 	                            Math.abs(pix[((i+1) * width + j - 1)*pixelWidth] - pix[idx3]) < threshold){
 	                        max = Math.max(labels[idx], labels[(i+1) * width + j - 1]);
 	                        if (max != labels[idx])
 	                                changed = true;
 	                        labels[idx] = max;
 	                    }
 
 	                    if (i != 0 && j != 0 && 
 	                            Math.abs(pix[((i-1) * width + j - 1)*pixelWidth] - pix[idx3]) < threshold){
 	                        max = Math.max(labels[idx], labels[(i-1) * width + j - 1]);
 	                        if (max != labels[idx])
 	                                changed = true;
 	                        labels[idx] = max;
 	                    }
 
 	                    if (j != 0 && 
 	                            Math.abs(pix[(i*width + j - 1)*pixelWidth] - pix[idx3]) < threshold){
 	                        max = Math.max(labels[idx], labels[i*width + j - 1]);
 	                        if (max != labels[idx])
 	                                changed = true;
 	                        labels[idx] = max;
 	                    }
 
 	                    if (j != width - 1 && 
 	                            Math.abs(pix[(i*width + j + 1)*pixelWidth] - pix[idx3]) < threshold){
 	                        max = Math.max(labels[idx], labels[i*width + j + 1]);
 	                        if (max != labels[idx])
 	                                changed = true;
 	                        labels[idx] = max;
 	                    }
 
 	                    // if label assigned to this pixel during "follow the pointers" step is worse than label
 	                    // of one of its neighbors, then that means that we're converging to local maximum instead
 	                    // of global one. To correct this, we replace our root pixel's label with better newly found one.
 	                    if (ll < labels[idx]) {
 	                        if (labels[ll - 1] < labels[idx])
 	                            labels[ll - 1] = labels[idx];
 	                    }
 	                }
 	            }
 
 	            // pass two. propagates the updated label of the parent to the children.
 	            for (int i = 0; i < height; i++) {
 	                for (int j = 0; j < width; j++) {
 	                    int idx = i*width + j;
 	                    if (labels[idx] != 0) {
 	                        labels[idx] = Math.max(labels[idx], labels[labels[idx] - 1]); 
 	                        // subtract 1 from pixel's label to convert it to array index
 	                    }
 	                }
 	            }
 
 	            System.out.println( "Phase " + (pp) + " done.");
 
 	        }
 	        
 		    Random random = new Random();
 
 	        int array[] = new int[(width * height) * 3];
 	        HashMap<Integer,Integer> red = new HashMap<Integer,Integer>();
 	        HashMap<Integer,Integer> green = new HashMap<Integer,Integer>();
 	        HashMap<Integer,Integer> blue = new HashMap<Integer,Integer>();
 
 
 	        for (int i = 0; i < height; i++) {
 	            for (int j = 0; j < width; j++) {
 	                int label = labels[i*width+j];
 	                if (label == 0) {
 	                    red.put(label, 0);
 	                    green.put(label, 0);
 	                    blue.put(label, 0);
 	                }
 
 	                if (!red.containsKey(label)) {
 	                    red.put(label, (int)(random.nextDouble()*255));
 	                    green.put(label, (int)(random.nextDouble()*255));
 	                    blue.put(label, (int)(random.nextDouble()*255));
 	                }
 
 	                array[(i*width+j)*3+0] = red.get(label);
 	                array[(i*width+j)*3+1] = green.get(label);
 	                array[(i*width+j)*3+2] = blue.get(label);
 	            }
 	        }
 	 
 	        // Store pixels in BufferedImage
 	        newImage.getRaster().setPixels(0, 0, width, height, array);
 
 		}
 		
 		return newImage;
 	}
 
 	static void calculateMoments(BufferedImage currentImage) {
 		System.out.printf("calculateMoments\n");
 		if (currentImage != null) {
 			ImageMoments moments = new ImageMoments(currentImage);
 			for (int i=1; i <= 7 ; i++) {
 				System.out.printf("  Hu[%d]: %2.2f\n", i, moments.getMoment(i));
 			}
 			System.out.printf("  Flusser/Suk: %2.2f\n", moments.getMoment(8));
 		}
 	}
 	
 	static void saveImage(BufferedImage currentImage, String filename, String filetype) {
 		System.out.printf("saveImage(%s)[%s]\n",filename,filetype);
 		try {
 		    File file = new File(filename);
 		    ImageIO.write(currentImage, filetype, file);
 		} catch (IOException e) {
 		}
 	}
 }
