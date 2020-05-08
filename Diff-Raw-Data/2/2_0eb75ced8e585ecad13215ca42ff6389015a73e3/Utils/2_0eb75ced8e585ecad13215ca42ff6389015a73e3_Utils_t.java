 /*
  *  Copyright (C) 2012 The Animo Project
  *  http://animotron.org
  *
  *  This file is part of Animi.
  *
  *  Animotron is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as
  *  published by the Free Software Foundation, either version 3 of
  *  the License, or (at your option) any later version.
  *
  *  Animotron is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of
  *  the GNU Affero General Public License along with Animotron.
  *  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.animotron.animi;
 
 import static org.jocl.CL.*;
 
 import java.awt.image.BufferedImage;
 
 import org.animotron.animi.cortex.CortexZoneComplex;
 import org.animotron.animi.cortex.Mapping;
 import org.jocl.Pointer;
 import org.jocl.Sizeof;
 import org.jocl.cl_event;
 
 /**
  * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
  *
  */
 public class Utils {
 
     private final static double LUM_RED = 0.299;
     private final static double LUM_GREEN = 0.587;
     private final static double LUM_BLUE = 0.114;
 
     public static int calcGrey(final BufferedImage img, final int x, final int y) {
         int value = img.getRGB(x, y);
 
         int r = get_red(value);
         int g = get_green(value);
         int b = get_blue(value);
         
 //        return r+g+b;
 //        return (r+g+b) /3;
         return (int) Math.round(r * LUM_RED + g * LUM_GREEN + b * LUM_BLUE);
     }
 
     public static int create_rgb(int alpha, int r, int g, int b) {
         int rgb = (alpha << 24) + (r << 16) + (g << 8) + b;
         return rgb;
     }
 
     public static int get_alpha(int rgb) {
         return (rgb >> 24) & 0xFF;
         // return rgb & 0xFF000000;
     }
 
     public static int get_red(int rgb) {
         return (rgb >> 16) & 0xFF;
         // return rgb & 0x00FF0000;
     }
 
     public static int get_green(int rgb) {
         return (rgb >> 8) & 0xFF;
     }
 
     public static int get_blue(int rgb) {
         return rgb & 0xFF;
     }
     
 	public static BufferedImage drawRF(
 			final BufferedImage image, final int boxSize,
 			final int offsetX, final int offsetY,
 			final int cnX, final int cnY,
 			final int pN,
 			final Mapping m) {
 
 		final CortexZoneComplex cz = m.toZone;
 
 		final int offset = (cnY * cz.width * m.linksSenapseRecordSize) + (m.linksSenapseRecordSize * cnX);
 //        final int offsetWeight = (cnY * cz.width * m.linksWeightRecordSize) + (m.linksWeightRecordSize * cnX);
 	    
         int offsetPackagers = cz.package_size * m.ns_links;
 		int lOffset = (cnY * cz.width * offsetPackagers) + (cnX * offsetPackagers) + (pN * m.ns_links);        
         
 		int pX = 0, pY = 0;
         for (int l = 0; l < m.ns_links; l++) {
         	int xi = m.linksSenapse[offset + 2*l    ];
         	int yi = m.linksSenapse[offset + 2*l + 1];
         	
         	pX = (boxSize / 2) + (xi - (int)(cnX * m.fX));
 			pY = (boxSize / 2) + (yi - (int)(cnY * m.fY));
                     	
 			if (       pX > 0 
         			&& pX < boxSize 
         			&& pY > 0 
         			&& pY < boxSize) {
 
 		        int value = image.getRGB(offsetX + pX, offsetY + pY);
 
 		        int G = Utils.get_green(value);
 		        int B = Utils.get_blue(value);
 		        int R = Utils.get_red(value);
 
 //		        switch (link.delay) {
 //				case 0:
 //					g += 255 * link.q;;
 //					if (g > 255) g = 255;
 //
 //					break;
 //				case 1:
 //					b += 255 * link.q;
 //					if (b > 255) b = 255;
 //
 //					break;
 //				default:
 //					r += 255 * link.q;
 //					if (r > 255) r = 255;
 //
 //					break;
 //				}
 //				image.setRGB(pX, pY, Utils.create_rgb(255, r, g, b));
 
 //				int c = calcGrey(image, offsetX + pX, offsetY + pY);
 //				c += 255 * m.linksWeight[lOffset + l];
 //				if (c > 255) c = 255;
 //				else if (c < 0) c = 0;
 //				image.setRGB(offsetX + pX, offsetY + pY, create_rgb(255, c, c, c));
 
 				if (m.linksWeight[lOffset + l] > 0.0f) {
 					B += 255 * m.linksWeight[lOffset + l] * 5;
 					if (B > 255) B = 255;
 				} else {
 					G += 255 * m.linksWeight[lOffset + l] * 5;
 					if (G > 255) G = 255;
 				};
				image.setRGB(offsetX + pX, offsetY + pY, create_rgb(255, 0, G, B));
         	}
         }
         return image;
 	}
 	
 	public static BufferedImage drawRF(
 			final BufferedImage image,
 			final int cnX, final int cnY, 
 			final Mapping m) {
 		
 		final CortexZoneComplex cz = m.toZone;
 		
 		final int pos = cnY * cz.width + cnX;
 
 		final int offset = (cnY * cz.width * m.linksSenapseRecordSize) + (m.linksSenapseRecordSize * cnX);
         final int offsetWeight = (cnY * cz.width * m.linksWeightRecordSize) + (m.linksWeightRecordSize * cnX);
         
         int pX = 0, pY = 0;
         for (int l = 0; l < m.ns_links; l++) {
         	int xi = m.linksSenapse[offset + 2*l    ];
         	int yi = m.linksSenapse[offset + 2*l + 1];
         	
         	pX = xi;
 			pY = yi;
                     	
 			if (       pX > 0 
         			&& pX < image.getWidth() 
         			&& pY > 0 
         			&& pY < image.getHeight()) {
 
 //		        int value = image.getRGB(pX, pY);
 //
 //		        int g = Utils.get_green(value);
 //		        int b = Utils.get_blue(value);
 //		        int r = Utils.get_red(value);
 //
 //		        switch (link.delay) {
 //				case 0:
 //					g += 255 * link.q;;
 //					if (g > 255) g = 255;
 //
 //					break;
 //				case 1:
 //					b += 255 * link.q;
 //					if (b > 255) b = 255;
 //
 //					break;
 //				default:
 //					r += 255 * link.q;
 //					if (r > 255) r = 255;
 //
 //					break;
 //				}
 //				image.setRGB(pX, pY, Utils.create_rgb(255, r, g, b));
 				
 				int packageNumber = 0;
 				for (int p = 0; p < cz.package_size; p++) {
 					if (cz.pCols[(cnY * cz.width * cz.package_size) + (cnX * cz.package_size) + p] >= cz.cols[pos]) {
 						packageNumber = p;
 						break;
 					}
 				}
 
 
 				int c = calcGrey(image, pX, pY);
 				c += 255 * cz.cols[pos] * m.linksWeight[offsetWeight + (packageNumber * m.ns_links) + l];
 				if (c > 255) c = 255;
 				image.setRGB(pX, pY, create_rgb(255, c, c, c));
         	}
         }
         return image;
 	}
 
 	/*
      * Print "benchmarking" information 
      */
     public static void printBenchmarkInfo(String description, cl_event event) {
         StringBuilder sb = new StringBuilder();
         sb
         	.append(description)
         	.append(" ")
         	.append(computeExecutionTimeMs(event))
         	.append(" ms");
         
         System.out.println(sb.toString());
     }
     
     /*
      * Compute the execution time for the given event, in milliseconds
      */
     private static double computeExecutionTimeMs(cl_event event) {
         long startTime[] = new long[1];
         long endTime[] = new long[1];
         clGetEventProfilingInfo(event, CL_PROFILING_COMMAND_END,   
             Sizeof.cl_ulong, Pointer.to(endTime), null);
         clGetEventProfilingInfo(event, CL_PROFILING_COMMAND_START, 
             Sizeof.cl_ulong, Pointer.to(startTime), null);
         return (endTime[0]-startTime[0]) / 1e6;
     }
 
 	public static String debug(float[] array) {
 		return debug(array, 7);
 	}
 	
 	public static String debug(float[] array, int count) {
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < count; i++) {
 			sb.append(array[i]).append(", ");
 		}
 		return sb.append(array[count]).toString();
 	}
 }
