 /*
  * Copyright 2011 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.diffraction;
 
 import static org.junit.Assert.assertEquals;
 import gda.util.TestUtils;
 
 import javax.vecmath.Matrix3d;
 import javax.vecmath.Vector3d;
 
 import junit.framework.Assert;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.io.DataHolder;
 import uk.ac.diamond.scisoft.analysis.io.PNGSaver;
 
 /**
  *
  */
 public class DetectorPropertiesTest {
 
 	static int[] detectorCorners = { 0, 0, 0, 3072, 3072, 0, 3072, 3072 };
 	static double pixelSize = 0.1026;
 	// static double pixelSize = 1.0;
 	static int[] imageSizePix = { 3072, 3072 };
 	static Vector3d detectorOrigin = new Vector3d(157.44, 157.44, 329.8);
 	static Vector3d beamCentre = new Vector3d(0, 0, 329.8);
 	static Matrix3d orientationMatrix = new Matrix3d(0.984657762021401, 0.017187265168157, -0.17364817766693,
 			0.012961831885909, 0.985184016468007, 0.171010071662834, 0.174014604574351, -0.170637192932859,
 			0.969846310392954);
 	static double lambda = 1.001;
 
 	private static String testScratchDirectoryName;
 	private static String filePath = "detectorproperties.png";
 	static double wavelength = 1.4;
 	static int[] ishape = new int[] { 800, 400 };
 	static double[] pxdim = new double[] { 0.2, 0.3 };
 	static double[] origin = new double[] { 80, 60, 234 };
 	static double ttheta = Math.toRadians(11.5);
 
 	private static DoubleDataset makeDiffImage() {
 		DoubleDataset data = new DoubleDataset(ishape[1], ishape[0]);
 		double rho = origin[2] * Math.tan(ttheta);
 
 		double dphi = 45.;
 		Matrix3d orientn = new Matrix3d();
 		orientn.setIdentity();
 		DetectorProperties detprops = new DetectorProperties(new Vector3d(origin), ishape[1], ishape[0], pxdim[1],
 				pxdim[0], orientn);
 		DiffractionCrystalEnvironment diffexp = new DiffractionCrystalEnvironment(wavelength);
 		QSpace qspace = new QSpace(detprops, diffexp);
 		for (double phi = 0; phi < 360.; phi += dphi) {
 			int x = (int) Math.floor((origin[0] + rho * Math.cos(Math.toRadians(phi))) / pxdim[0]);
 			int y = (int) Math.floor((origin[1] + rho * Math.sin(Math.toRadians(phi))) / pxdim[1]);
 
 			data.set(phi + 30, y, x);
 			System.out.println("Q: " + qspace.qFromPixelPosition(x, y) + "; value = " + (phi + 30));
 		}
 
 		return data;
 	}
 
 	/**
 	 * Creates an empty directory for use by test code.
 	 * 
 	 * @throws Exception
 	 *             if the test fails
 	 */
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		testScratchDirectoryName = TestUtils.generateDirectorynameFromClassname(DetectorPropertiesTest.class
 				.getCanonicalName());
 		TestUtils.makeScratchDirectory(testScratchDirectoryName);
 		DataHolder dh = new DataHolder();
 		DoubleDataset data = makeDiffImage();
 		dh.addDataset("testing data", data);
 		new PNGSaver(testScratchDirectoryName + filePath).saveFile(dh);
 	}
 
 	/**
 	 * test the setters
 	 */
 	@Test
 	public void testSetters() {
 		DetectorProperties det = new DetectorProperties();
 		det.setHPxSize(pixelSize);
 		det.setVPxSize(pixelSize);
 		det.setPx(imageSizePix[0]);
 		det.setPy(imageSizePix[1]);
 		det.setOrientation(orientationMatrix);
 		det.setOrigin(new Vector3d(50, 50, 300000));
 	}
 
 	@Test
 	public void testCloningWithSetters() {
 		DetectorProperties det = new DetectorProperties();
 		det.setHPxSize(pixelSize);
 		det.setVPxSize(pixelSize);
 		det.setPx(imageSizePix[0]);
 		det.setPy(imageSizePix[1]);
 		det.setOrientation(orientationMatrix);
 		det.setOrigin(new Vector3d(50, 50, 300000));
 		det.setBeamVector(new Vector3d(0, 0, 1));
 
 		DetectorProperties newDetector = det.clone();
 		if (!det.equals(newDetector)) {
 			Assert.fail("Cloned object not equal");
 		}
 	}
 
 	@Test
 	public void testCloning() {
 		DetectorProperties det = new DetectorProperties(new Vector3d(50, 50, 300000), imageSizePix[0], imageSizePix[1],
 				pixelSize, pixelSize, orientationMatrix);
 		DetectorProperties newDetector = det.clone();
 		if (!det.equals(newDetector)) {
 			Assert.fail("Cloned object not equal");
 		}
 	}
 	
 	@Test
 	public void testImageOrientation(){
 		DetectorProperties det = new DetectorProperties(new Vector3d(50, 50, 300000), imageSizePix[0], imageSizePix[1],
 				pixelSize, pixelSize, orientationMatrix);
 		if(!orientationMatrix.equals(det.getOrientation()))
 			Assert.fail("The orientation matrices are not equal");
 	}
 
 	@Test
 	public void testBeamCentre() {
 		DetectorProperties det = new DetectorProperties(detectorOrigin, imageSizePix[0], imageSizePix[1],
 				pixelSize, pixelSize, orientationMatrix);
 
 		double[] bc1 = det.getBeamCentreCoords();
 		System.out.printf("Initial centre: %f, %f\n", bc1[0], bc1[1]);
 		bc1[0] -= 75;
 		bc1[1] += 120;
 		det.setBeamCentreCoords(bc1);
 		double[] bc2 = det.getBeamCentreCoords();
 		Assert.assertEquals("X coord", bc1[0], bc2[0], 1e-7);
 		Assert.assertEquals("Y coord", bc1[1], bc2[1], 1e-7);
 	}
 
 	@Test
 	public void testDetectorOrigin() {
 		DetectorProperties detector = new DetectorProperties(detectorOrigin, beamCentre, imageSizePix[0], imageSizePix[1], pixelSize,
 				pixelSize, orientationMatrix);
 		Vector3d newOrigin;
 
 		newOrigin = new Vector3d(130, 120, 200);
 		detector.setOrigin(newOrigin);
 		assertEquals(detector.pixelPosition(0, 0), newOrigin);
 
 		newOrigin = new Vector3d(-130, 120, 200);
 		detector.setOrigin(newOrigin);
 		assertEquals(detector.pixelPosition(0, 0), newOrigin);
 
 		newOrigin = new Vector3d(130, -120, 200);
 		detector.setOrigin(newOrigin);
 		assertEquals(detector.pixelPosition(0, 0), newOrigin);
 
 		newOrigin = new Vector3d(130, 120, -200);
 		detector.setOrigin(newOrigin);
 		assertEquals(detector.pixelPosition(0, 0), newOrigin);
 
 		newOrigin = new Vector3d(-130, -120, 200);
 		detector.setOrigin(newOrigin);
 		assertEquals(detector.pixelPosition(0, 0), newOrigin);
 
 		newOrigin = new Vector3d(130, -120, -200);
 		detector.setOrigin(newOrigin);
 		assertEquals(detector.pixelPosition(0, 0), newOrigin);
 
 		newOrigin = new Vector3d(-130, -120, -200);
 		detector.setOrigin(newOrigin);
 		assertEquals(detector.pixelPosition(0, 0), newOrigin);
 	}
 
 	/**
 	 * As a general check test the size of the detector a various orientations
 	 */
 	@Test
 	public void testDetectorSize() {
 	DetectorProperties detector = new DetectorProperties(detectorOrigin, beamCentre, imageSizePix[0], imageSizePix[1], pixelSize,
 			pixelSize, orientationMatrix);
 		// detector size for assert
 		double detSizeX = detector.getDetectorSizeH();
 		double detSizeY = detector.getDetectorSizeV();
 		double diagDetSize = Math.sqrt((detSizeX * detSizeX) + (detSizeY * detSizeY));
 		int[] detectorCorners = { 0, 0, 0, 3072, 3072, 0, 3072, 3072 };
 		Vector3d px1topx4 = new Vector3d();
 
 		px1topx4.sub(detector.pixelPosition(detectorCorners[0], detectorCorners[1]),
 				detector.pixelPosition(detectorCorners[6], detectorCorners[7]));
 		assertEquals(diagDetSize, px1topx4.length(), 0.00001);
 
 		Matrix3d newOri = detector.getOrientation();
 		newOri.rotY(1.5);
 		detector.setOrientation(newOri);
 
 		px1topx4.sub(detector.pixelPosition(detectorCorners[0], detectorCorners[1]),
 				detector.pixelPosition(detectorCorners[6], detectorCorners[7]));
 		assertEquals(diagDetSize, px1topx4.length(), 0.00001);
 
 		detector.setOrigin(new Vector3d(-150, 250, 389));
 
 		px1topx4.sub(detector.pixelPosition(detectorCorners[0], detectorCorners[1]),
 				detector.pixelPosition(detectorCorners[6], detectorCorners[7]));
 		assertEquals(diagDetSize, px1topx4.length(), 0.00001);
 
 	}
 
 	@Test
 	public void testMatrixConvention() {
 		Matrix3d a = new Matrix3d();
 		double angle = Math.PI/6.; 
 		double answer = -Math.sin(angle);
 		a.rotX(angle);
 		Assert.assertEquals("X rotation", answer, a.getM12(), 1e-7);
 		a.rotY(angle);
 		Assert.assertEquals("Y rotation", answer, a.getM20(), 1e-7);
 		a.rotZ(angle);
 		Assert.assertEquals("Z rotation", answer, a.getM01(), 1e-7);
 	}
 
 	@Test
 	public void testEulerConversions() {
 		DetectorProperties det = DetectorProperties.getDefaultDetectorProperties(new int[] {100,100});
 		double[] angle;
 
 		angle = det.getNormalAnglesInDegrees();
 		Assert.assertEquals("Yaw",   0, angle[0], 1e-7);
 		Assert.assertEquals("Pitch", 0, angle[1], 1e-7);
 		Assert.assertEquals("Roll",  0, angle[2], 1e-7);
 
 		det.setNormalAnglesInDegrees(30, 25, -35);
 		angle = det.getNormalAnglesInDegrees();
 		Assert.assertEquals("Yaw",   30, angle[0], 1e-7);
 		Assert.assertEquals("Pitch", 25, angle[1], 1e-7);
 		Assert.assertEquals("Roll",  -35, angle[2], 1e-7);
 
 		det.setNormalAnglesInDegrees(-95, -65, 103);
 		angle = det.getNormalAnglesInDegrees();
 		Assert.assertEquals("Yaw",   -95, angle[0], 1e-7);
 		Assert.assertEquals("Pitch", -65, angle[1], 1e-7);
 		Assert.assertEquals("Roll",  103, angle[2], 1e-7);
 
 		det.setNormalAnglesInDegrees(-95, 90, 103);
 		angle = det.getNormalAnglesInDegrees();
 		Assert.assertEquals("Yaw",   103-95, angle[0], 1e-7);
 		Assert.assertEquals("Pitch", 90, angle[1], 1e-7);
 		Assert.assertEquals("Roll",  0, angle[2], 1e-7);
 
 		det.setNormalAnglesInDegrees(-95, -90, 103);
 		angle = det.getNormalAnglesInDegrees();
 		Assert.assertEquals("Yaw",   103+95-360, angle[0], 1e-7);
 		Assert.assertEquals("Pitch", -90, angle[1], 1e-7);
 		Assert.assertEquals("Roll",  0, angle[2], 1e-7);
 	}
 
 	@Test
 	public void testBeamCentreDistance() {
 		DetectorProperties det = new DetectorProperties(new Vector3d(500, 550, 600), 1000, 1100, 1, 1, null);
 		
 		Vector3d o = det.getOrigin();
 		Assert.assertEquals("Detector origin, x", 500, o.x, 1e-7);
 		Assert.assertEquals("Detector origin, y", 550, o.y, 1e-7);
 		Assert.assertEquals("Detector origin, z", 600, o.z, 1e-7);
 
 		double[] c = det.getBeamCentreCoords();
 		Assert.assertEquals("Beam centre, x", 500, c[0], 1e-7);
 		Assert.assertEquals("Beam centre, y", 550, c[1], 1e-7);
 
 		Assert.assertEquals("Beam centre distance", 600, det.getBeamCentreDistance(), 1e-7);
 		Assert.assertEquals("Detector distance", 600, det.getDetectorDistance(), 1e-7);
 		det.setBeamCentreDistance(700);
 		Assert.assertEquals("Beam centre distance", 700, det.getBeamCentreDistance(), 1e-7);
 		Assert.assertEquals("Detector distance", 700, det.getDetectorDistance(), 1e-7);
 
 		det.setDetectorDistance(600);
 		Assert.assertEquals("Beam centre distance", 600, det.getBeamCentreDistance(), 1e-7);
 		Assert.assertEquals("Detector distance", 600, det.getDetectorDistance(), 1e-7);
 
 		det.setNormalAnglesInDegrees(30, 0, 0);
 		Assert.assertEquals("Beam centre distance", 600, det.getBeamCentreDistance(), 1e-7);
 		Assert.assertEquals("Detector distance", 600*Math.cos(Math.toRadians(30)), det.getDetectorDistance(), 1e-7);
 	}
 }
