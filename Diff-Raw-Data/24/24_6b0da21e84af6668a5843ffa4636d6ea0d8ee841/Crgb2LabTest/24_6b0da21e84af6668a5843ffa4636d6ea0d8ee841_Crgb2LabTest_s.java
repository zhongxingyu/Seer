 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package image.converters;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author gimli
  */
 public class Crgb2LabTest {
 	
 	@Test
 	public void testSRgb2Xyz2Rgb() {
 		double[] sRgb = {235.0, 112.2, 56.3};		
 		double[] result = Crgb2Lab.xyz2sRgb(Crgb2Lab.sRgb2Xyz(sRgb));
 		assertArrayEquals(sRgb, result, 0.01);		
 	}
 	
 	@Test
 	public void testD65sRgb2Lab() {
 		double[] sRgb = {87, 123, 211};
 		double[] lab = {52.835, 13.840, -49.255};
 		
 		Crgb2Lab convertor = new Crgb2Lab(Crgb2Lab.WhitePoint.D65);
 		
 		assertArrayEquals(lab, convertor.sRgb2Lab(sRgb), 0.01);
 		
 		sRgb = new double[] {0,75,111};
 		lab = new double[] {29.862, -5.639, -26.353};
 		
 		assertArrayEquals(lab, convertor.sRgb2Lab(sRgb), 0.01);
 	}
 	
 	@Test
 	public void testD50sRgb2Lab() {
 		double[] sRgb = {121, 37, 120};
 		double[] lab = {31.217, 45.674, -39.961};
 		
 		Crgb2Lab convertor = new Crgb2Lab(Crgb2Lab.WhitePoint.D50);
 		
 		assertArrayEquals(lab, convertor.sRgb2Lab(sRgb), 0.01);
 	}
 	
 	@Test
 	public void testLab2sRgbD65() {
 		double[] lab = {30, 40, 30};
 		double[] sRgb = {131.63, 36.40, 25.88};
 		
 		Crgb2Lab convertor = new Crgb2Lab(Crgb2Lab.WhitePoint.D65);
 		
 		assertArrayEquals(sRgb, convertor.lab2sRgb(lab), 0.01);
 	}
 	
 	@Test
 	public void testMsh2Lab() {
 		double[] lab = {52.34, 43.29, -23.57};
 		
 		assertArrayEquals(lab, Crgb2Lab.msh2Lab(Crgb2Lab.lab2Msh(lab)), 0.01);
 	}
	
	@Test 
	public void testHues() {
		double step = 1;
		double[] maxSat = new double[360];
		double[] maxM = new double[360];
		
		Crgb2Lab convertor = new Crgb2Lab(Crgb2Lab.WhitePoint.D65);
		
		for(int hue=0; hue<360; hue+=step) {
			int sat = 255;
			for(int value=0; value< 256; value++) {

				double[] msh = Crgb2Lab.lab2Msh(convertor.sRgb2Lab(Crgb2hsv.inverse(new double[]{hue, sat, value})));

				if (maxM[hue] < msh[0] + maxSat[hue]/Math.PI*180) {
					maxSat[hue] = msh[1]; 
					maxM[hue] = msh[0] + maxSat[hue]/Math.PI*180;
				}

			}
			System.out.println("Hue: "+hue+" sat: "+maxSat[hue]/Math.PI*180+" M: "+maxM[hue]);
		}
	}
 }
