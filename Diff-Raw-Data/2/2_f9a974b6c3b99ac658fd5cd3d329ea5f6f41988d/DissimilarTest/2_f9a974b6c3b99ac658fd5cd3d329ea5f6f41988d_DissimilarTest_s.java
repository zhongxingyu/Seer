 /*
 * Copyright 2013 The Britsh Library/SCAPE Project Consortium
  * Author: William Palmer (William.Palmer@bl.uk)
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package dissimilar;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 
 import org.junit.Test;
 
 import uk.bl.dpt.qa.DissimilarV2;
 
 /**
  * Test cases for DissimilarV2
  * @author wpalmer
  *
  */
 @SuppressWarnings("javadoc")
 public class DissimilarTest {
 	
 	private static final String TESTFILEDIR = "src/test/resources/";
 
 	//PSNR tests
 	
 	@Test
 	public final void testCalcPSNR1() {
 		
 		final double imagemagickPSNR = 45.7174;
 		
 		//note these files are here as there appears to be an issue loading png files?
 		//files must be encoded-decoded for consistent testing of a decoded file 
 		
 		final File testmaster = new File(TESTFILEDIR+"test1-bw.png.bmp");
 		final File testcopy = new File(TESTFILEDIR+"test1-bw.jpg.bmp");
 		
 		final double psnr = DissimilarV2.calcPSNR(testmaster, testcopy);
 		if(Math.abs(psnr-imagemagickPSNR)>0.0001) {
 			System.out.println("Failure for test file: "+testcopy.getName());
 			System.out.println("Calculated PSNR: "+psnr);
 			System.out.println("Expected: "+imagemagickPSNR);
 			fail("PSNR does not match ground truth from ImageMagick");
 		}
 		
 	}
 	
 	@Test
 	public final void testCalcPSNR2() {
 		
 		final double imagemagickPSNR = 36.1236;
 		
 		final File testmaster = new File(TESTFILEDIR+"test1-bw.png");
 		final File testcopy = new File(TESTFILEDIR+"test1-bw-dot.png");
 		
 		final double psnr = DissimilarV2.calcPSNR(testmaster, testcopy);
 		if(Math.abs(psnr-imagemagickPSNR)>0.0001) {
 			System.out.println("Failure for test file: "+testcopy.getName());
 			System.out.println("Calculated PSNR: "+psnr);
 			System.out.println("Expected: "+imagemagickPSNR);
 			fail("PSNR does not match ground truth from ImageMagick");
 		}
 		
 	}
 	
 	@Test
 	public final void testCalcPSNR3() {
 		
 		final double imagemagickPSNR = 18.3421;
 		
 		final File testmaster = new File(TESTFILEDIR+"test1-bw.png");
 		final File testcopy = new File(TESTFILEDIR+"test1-bw-line.png");
 		
 		final double psnr = DissimilarV2.calcPSNR(testmaster, testcopy);
 		if(Math.abs(psnr-imagemagickPSNR)>0.0001) {
 			System.out.println("Failure for test file: "+testcopy.getName());
 			System.out.println("Calculated PSNR: "+psnr);
 			System.out.println("Expected: "+imagemagickPSNR);
 			fail("PSNR does not match ground truth from ImageMagick");
 		}
 		
 	}
 	
 	@Test
 	public final void testCalcPSNR4() {
 		
 		final double imagemagickPSNR = 21.9568;
 		
 		//note these files are here as there appears to be an issue loading png files?
 		//files must be encoded-decoded for consistent testing of a decoded file 
 		
 		final File testmaster = new File(TESTFILEDIR+"test1-colour.png.bmp");
 		final File testcopy = new File(TESTFILEDIR+"test1-colour.jpg.bmp");
 		
 		final double psnr = DissimilarV2.calcPSNR(testmaster, testcopy);
 		if(Math.abs(psnr-imagemagickPSNR)>0.0001) {
 			System.out.println("Failure for test file: "+testcopy.getName());
 			System.out.println("Calculated PSNR: "+psnr);
 			System.out.println("Expected: "+imagemagickPSNR);
 			fail("PSNR does not match ground truth from ImageMagick");
 		}
 		
 	}
 	
 	@Test
 	public final void testCalcPSNR5() {
 		
 		final double imagemagickPSNR = 39.0337;
 		
 		final File testmaster = new File(TESTFILEDIR+"test1-colour.png");
 		final File testcopy = new File(TESTFILEDIR+"test1-colour-reddot.png");
 		
 		final double psnr = DissimilarV2.calcPSNR(testmaster, testcopy);
 		if(Math.abs(psnr-imagemagickPSNR)>0.0001) {
 			System.out.println("Failure for test file: "+testcopy.getName());
 			System.out.println("Calculated PSNR: "+psnr);
 			System.out.println("Expected: "+imagemagickPSNR);
 			fail("PSNR does not match ground truth from ImageMagick");
 		}
 		
 	}
 	
 	@Test
 	public final void testCalcPSNR6() {
 		
 		final double imagemagickPSNR = 39.0337;
 		
 		final File testmaster = new File(TESTFILEDIR+"test1-colour.png");
 		final File testcopy = new File(TESTFILEDIR+"test1-colour-reddot2.png");
 		
 		final double psnr = DissimilarV2.calcPSNR(testmaster, testcopy);
 		if(Math.abs(psnr-imagemagickPSNR)>0.0001) {
 			System.out.println("Failure for test file: "+testcopy.getName());
 			System.out.println("Calculated PSNR: "+psnr);
 			System.out.println("Expected: "+imagemagickPSNR);
 			fail("PSNR does not match ground truth from ImageMagick");
 		}
 		
 	}
 	
 	@Test
 	public final void testCalcPSNR7() {
 		
 		final double imagemagickPSNR = 21.2522;
 		
 		final File testmaster = new File(TESTFILEDIR+"test1-colour.png");
 		final File testcopy = new File(TESTFILEDIR+"test1-colour-redline.png");
 		
 		final double psnr = DissimilarV2.calcPSNR(testmaster, testcopy);
 		if(Math.abs(psnr-imagemagickPSNR)>0.0001) {
 			System.out.println("Failure for test file: "+testcopy.getName());
 			System.out.println("Calculated PSNR: "+psnr);
 			System.out.println("Expected: "+imagemagickPSNR);
 			fail("PSNR does not match ground truth from ImageMagick");
 		}
 		
 	}
 	
 	@Test
 	public final void testCalcPSNR8() {
 		
 		final double imagemagickPSNR = Double.POSITIVE_INFINITY;
 		
 		final File testmaster = new File(TESTFILEDIR+"test1-colour.png");
 		final File testcopy = new File(TESTFILEDIR+"test1-colour.png");
 		
 		final double psnr = DissimilarV2.calcPSNR(testmaster, testcopy);
 		if(Math.abs(psnr-imagemagickPSNR)>0.0001) {
 			System.out.println("Failure for test file: "+testcopy.getName());
 			System.out.println("Calculated PSNR: "+psnr);
 			System.out.println("Expected: "+imagemagickPSNR);
 			fail("PSNR does not match ground truth from ImageMagick");
 		}
 		
 	}
 	
 	@Test
 	public final void testCalcPSNR9() {
 		
 		final double imagemagickPSNR = Double.POSITIVE_INFINITY;
 		
 		final File testmaster = new File(TESTFILEDIR+"test1-bw.png");
 		final File testcopy = new File(TESTFILEDIR+"test1-bw.png");
 		
 		final double psnr = DissimilarV2.calcPSNR(testmaster, testcopy);
 		if(Math.abs(psnr-imagemagickPSNR)>0.0001) {
 			System.out.println("Failure for test file: "+testcopy.getName());
 			System.out.println("Calculated PSNR: "+psnr);
 			System.out.println("Expected: "+imagemagickPSNR);
 			fail("PSNR does not match ground truth from ImageMagick");
 		}
 		
 	}
 
 	//SSIM tests - note that these use values calculated previously calculated by dissimilar 
 	//NOTE: results from matchbox and pyssim have been copied by hand, not copied and pasted!
 	
 	@Test
 	public final void testCalcSSIM1() {
 		
 		//matchbox: 0.999617
 		//pyssim: 0.999135215724
 		final double knownSSIM = 0.9999256555672021;
 		
 		final File testmaster = new File(TESTFILEDIR+"test1-bw.png.bmp");
 		final File testcopy = new File(TESTFILEDIR+"test1-bw.jpg.bmp");
 		
 		final double ssim = DissimilarV2.calcSSIM(testmaster, testcopy);
 		if(Math.abs(ssim-knownSSIM)>0.000001) {
 			System.out.println("Failure for test file: "+testcopy.getName());
 			System.out.println("Calculated SSIM: "+ssim);
 			System.out.println("Expected: "+knownSSIM);
 			fail("SSIM does not match ground truth");
 		}
 		
 	}
 	
 	@Test
 	public final void testCalcSSIM2() {
 		
 		//matchbox: 0.998197
 		//pyssim: 0.995240065107
 		final double knownSSIM = 0.9852258778483007;
 		
 		final File testmaster = new File(TESTFILEDIR+"test1-bw.png");
 		final File testcopy = new File(TESTFILEDIR+"test1-bw-dot.png");
 		
 		final double ssim = DissimilarV2.calcSSIM(testmaster, testcopy);
 		if(Math.abs(ssim-knownSSIM)>0.000001) {
 			System.out.println("Failure for test file: "+testcopy.getName());
 			System.out.println("Calculated SSIM: "+ssim);
 			System.out.println("Expected: "+knownSSIM);
 			fail("SSIM does not match ground truth");
 		}
 		
 	}
 	
 	@Test
 	public final void testCalcSSIM3() {
 		
 		//matchbox: 0.964193
 		//pyssim: 0.894466658277
 		final double knownSSIM = 0.8760675506953541;
 		
 		final File testmaster = new File(TESTFILEDIR+"test1-bw.png");
 		final File testcopy = new File(TESTFILEDIR+"test1-bw-line.png");
 		
 		final double ssim = DissimilarV2.calcSSIM(testmaster, testcopy);
 		if(Math.abs(ssim-knownSSIM)>0.000001) {
 			System.out.println("Failure for test file: "+testcopy.getName());
 			System.out.println("Calculated SSIM: "+ssim);
 			System.out.println("Expected: "+knownSSIM);
 			fail("SSIM does not match ground truth");
 		}
 		
 	}
 	
 	@Test
 	public final void testCalcSSIM4() {
 		
 		//matchbox: 0.745646
 		//pyssim: 0.997971434487
 		final double knownSSIM = 0.997781727710864;
 		
 		final File testmaster = new File(TESTFILEDIR+"test1-colour.png.bmp");
 		final File testcopy = new File(TESTFILEDIR+"test1-colour.jpg.bmp");
 		
 		final double ssim = DissimilarV2.calcSSIM(testmaster, testcopy);
 		if(Math.abs(ssim-knownSSIM)>0.000001) {
 			System.out.println("Failure for test file: "+testcopy.getName());
 			System.out.println("Calculated SSIM: "+ssim);
 			System.out.println("Expected: "+knownSSIM);
 			fail("SSIM does not match ground truth");
 		}
 		
 	}
 	
 	@Test
 	public final void testCalcSSIM5() {
 		
 		//matchbox: 0.993372
 		//pyssim: 0.991874207916
 		final double knownSSIM = 0.9859623627676727;
 		
 		final File testmaster = new File(TESTFILEDIR+"test1-colour.png");
 		final File testcopy = new File(TESTFILEDIR+"test1-colour-reddot.png");
 		
 		final double ssim = DissimilarV2.calcSSIM(testmaster, testcopy);
 		if(Math.abs(ssim-knownSSIM)>0.000001) {
 			System.out.println("Failure for test file: "+testcopy.getName());
 			System.out.println("Calculated SSIM: "+ssim);
 			System.out.println("Expected: "+knownSSIM);
 			fail("SSIM does not match ground truth");
 		}
 		
 	}
 	
 	@Test
 	public final void testCalcSSIM6() {
 		
 		//matchbox: 0.996566
 		//pyssim: 0.996047231102
 		final double knownSSIM = 0.9859623627676727;
 		
 		final File testmaster = new File(TESTFILEDIR+"test1-colour.png");
 		final File testcopy = new File(TESTFILEDIR+"test1-colour-reddot2.png");
 		
 		final double ssim = DissimilarV2.calcSSIM(testmaster, testcopy);
 		if(Math.abs(ssim-knownSSIM)>0.000001) {
 			System.out.println("Failure for test file: "+testcopy.getName());
 			System.out.println("Calculated SSIM: "+ssim);
 			System.out.println("Expected: "+knownSSIM);
 			fail("SSIM does not match ground truth");
 		}
 		
 	}
 	
 	@Test
 	public final void testCalcSSIM7() {
 		
 		//matchbox: 0.913329
 		//pyssim: 0.904681645303
 		final double knownSSIM = 0.877086605653693;
 		
 		final File testmaster = new File(TESTFILEDIR+"test1-colour.png");
 		final File testcopy = new File(TESTFILEDIR+"test1-colour-redline.png");
 		
 		final double ssim = DissimilarV2.calcSSIM(testmaster, testcopy);
 		if(Math.abs(ssim-knownSSIM)>0.000001) {
 			System.out.println("Failure for test file: "+testcopy.getName());
 			System.out.println("Calculated SSIM: "+ssim);
 			System.out.println("Expected: "+knownSSIM);
 			fail("SSIM does not match ground truth");
 		}
 		
 	}
 	
 	@Test
 	public final void testCalcSSIM8() {
 		
 		//matchbox: 1.0
 		//pyssim: 1.0
 		final double knownSSIM = 1.0;
 		
 		final File testmaster = new File(TESTFILEDIR+"test1-colour.png");
 		final File testcopy = new File(TESTFILEDIR+"test1-colour.png");
 		
 		final double ssim = DissimilarV2.calcSSIM(testmaster, testcopy);
 		if(Math.abs(ssim-knownSSIM)>0.000001) {
 			System.out.println("Failure for test file: "+testcopy.getName());
 			System.out.println("Calculated SSIM: "+ssim);
 			System.out.println("Expected: "+knownSSIM);
 			fail("SSIM does not match ground truth");
 		}
 		
 	}
 	
 	@Test
 	public final void testCalcSSIM9() {
 		
 		//matchbox: 1.0
 		//pyssim: 1.0
 		final double knownSSIM = 1.0;
 		
 		final File testmaster = new File(TESTFILEDIR+"test1-bw.png");
 		final File testcopy = new File(TESTFILEDIR+"test1-bw.png");
 		
 		final double ssim = DissimilarV2.calcSSIM(testmaster, testcopy);
 		if(Math.abs(ssim-knownSSIM)>0.000001) {
 			System.out.println("Failure for test file: "+testcopy.getName());
 			System.out.println("Calculated SSIM: "+ssim);
 			System.out.println("Expected: "+knownSSIM);
 			fail("SSIM does not match ground truth");
 		}
 		
 	}
 	
 	@Test
 	public final void testCalcSSIM10() {
 		
 		//matchbox: assert/failure
 		//pyssim: 0.785092086775
 		final double knownSSIM = 0.7914528347562869;
 		
 		final File testmaster = new File(TESTFILEDIR+"test1-colour.png");
 		final File testcopy = new File(TESTFILEDIR+"test1-bw.png");
 		
 		final double ssim = DissimilarV2.calcSSIM(testmaster, testcopy);
 		if(Math.abs(ssim-knownSSIM)>0.000001) {
 			System.out.println("Failure for test file: "+testcopy.getName());
 			System.out.println("Calculated SSIM: "+ssim);
 			System.out.println("Expected: "+knownSSIM);
 			fail("SSIM does not match ground truth");
 		}
 		
 	}
 	
 
 	
 
 
 }
