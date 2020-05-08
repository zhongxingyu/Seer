 package com.nevkontakte.steganistic;
 
 public class RSDetector {
 	public static enum Group {REGULAR, SINGULAR, UNUSABLE}
 
 	public static final int[] positivePattern = new int[]{0, 1, 0};
 	public static final int[] negativePattern = new int[positivePattern.length];
 
 	static {
 		for (int i = 0; i < positivePattern.length; i++) {
 			negativePattern[i] = -positivePattern[i];
 		}
 	}
 
 	public int flip(int value, int mode) {
 		switch (mode) {
 			case 1:
 				return value ^ 1;
 			case 0:
 				return value;
 			case -1:
 				return (((value + 1) ^ 1) - 1);
 			default:
 				throw new IllegalArgumentException("Invalid flipping mode.");
 		}
 	}
 
 	public int discriminator(int[] raster, int first, int length, int[] flipPattern) {
 		if (flipPattern != null && length != flipPattern.length) {
 			throw new IllegalArgumentException("Flipping pattern length isn't equal to sample length.");
 		}
 		int result = 0;
 		for (int i = 0; (i < length - 1) && (i + first + 1 < raster.length); i++) {
 			if (null == flipPattern) {
 				result += Math.abs(raster[first + i + 1] - raster[first + i]);
 			} else {
 				result += Math.abs(
 						this.flip(raster[first + i + 1], flipPattern[i+1]) -
 						this.flip(raster[first + i], flipPattern[i])
 				);
 			}
 		}
 		return result;
 	}
 
 	public Group classifySample(int[] raster, int first, int length, int[] flipPattern) {
 		int normal = this.discriminator(raster, first, length, null);
 		int flipped = this.discriminator(raster, first, length, flipPattern);
 
 		if (flipped > normal) {
 			return Group.REGULAR;
 		} else if (flipped < normal) {
 			return Group.SINGULAR;
 		} else {
 			return Group.UNUSABLE;
 		}
 	}
 
 	public double scan(PGMImage image) {
 		// Original image properties
 		int reg_pos_orig = 0;   // Number of regular groups with positive pattern
 		int sing_pos_orig = 0;  // Number of singular groups with positive pattern
 		int reg_neg_orig = 0;   // Number of regular groups with negative pattern
 		int sing_neg_orig = 0;  // Number of singular groups with negative pattern
 		
 		// Image with flipped LSB properties
 		int reg_pos_flip = 0;   // Number of regular groups with positive pattern
 		int sing_pos_flip = 0;  // Number of singular groups with positive pattern
 		int reg_neg_flip = 0;   // Number of regular groups with negative pattern
 		int sing_neg_flip = 0;  // Number of singular groups with negative pattern
 
 		// Define shortcuts
 		int groupLength = positivePattern.length;
 		int[] raster = image.raster;
 
 		// Process original image groups
 		for (int groupStart = 0; groupStart < raster.length; groupStart += groupLength) {
 			Group group;
 
 			group = this.classifySample(raster, groupStart, groupLength, positivePattern);
 			if (group == Group.REGULAR) {
 				reg_pos_orig++;
 			} else if (group == Group.SINGULAR) {
 				sing_pos_orig++;
 			}
 			group = this.classifySample(raster, groupStart, groupLength, negativePattern);
 			if (group == Group.REGULAR) {
 				reg_neg_orig++;
 			} else if (group == Group.SINGULAR) {
 				sing_neg_orig++;
 			}
 		}
 
 		// Process flipped image groups
 		for (int groupStart = 0; groupStart < raster.length; groupStart += groupLength) {
 			Group group;
 
 			// Flip group
			for (int i = 0; i < groupLength && i + groupStart < raster.length; i++) {
				raster[i + groupStart] ^= 1;
 			}
 
 			group = this.classifySample(raster, groupStart, groupLength, positivePattern);
 			if (group == Group.REGULAR) {
 				reg_pos_flip++;
 			} else if (group == Group.SINGULAR) {
 				sing_pos_flip++;
 			}
 			group = this.classifySample(raster, groupStart, groupLength, negativePattern);
 			if (group == Group.REGULAR) {
 				reg_neg_flip++;
 			} else if (group == Group.SINGULAR) {
 				sing_neg_flip++;
 			}
 
 			// Flip group back
			for (int i = 0; i < groupLength && i + groupStart < raster.length; i++) {
				raster[i + groupStart] ^= 1;
 			}
 		}
 
 		return this.intersectCurves(reg_pos_orig, sing_pos_orig, reg_neg_orig, sing_neg_orig,
 				reg_pos_flip, sing_pos_flip, reg_neg_flip, sing_neg_flip);
 	}
 
 	public double intersectCurves(int reg_pos_orig, int sing_pos_orig, int reg_neg_orig, int sing_neg_orig, int reg_pos_flip, int sing_pos_flip, int reg_neg_flip, int sing_neg_flip) {
 		// R_M(p/2) - S_M(p/2)
 		double d0 = reg_pos_orig - sing_pos_orig;
 		// R_M(1-p/2) - S_M(1-p/2)
 		double d1 = reg_pos_flip - sing_pos_flip;
 		// R_{-M}(p/2) - S_{-M}(p/2)
 		double dn0 = reg_neg_orig - sing_neg_orig;
 		// R_{-M}(1-p/2) - S_{-M}(1-p/2)
 		double dn1 = reg_neg_flip - sing_neg_flip;
 
 		double a = 2 * (d1 + d0);
 		double b = dn0 - dn1 - d1 - 3 * d0;
 		double c = d0 - dn0;
 
 		double x = solve(a, b, c);
 		return x / (x - 0.5);
 	}
 
 	public double solve(double a, double b, double c) {
		double d = (b * b) - (4 * a * c);
 
 		if (d < 0) {
 			return Double.NaN;
 		}
 
 		double x1 = (-b + Math.sqrt(d)) / (2 * a);
 		double x2 = (-b - Math.sqrt(d)) / (2 * a);
 
 		return Math.min(x1, x2);
 	}
 }
