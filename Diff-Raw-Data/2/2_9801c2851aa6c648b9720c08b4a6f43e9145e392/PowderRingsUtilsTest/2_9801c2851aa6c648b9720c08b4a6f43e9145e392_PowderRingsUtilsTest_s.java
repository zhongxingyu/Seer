 /*-
  * Copyright 2013 Diamond Light Source Ltd.
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
 
 import gda.analysis.io.ScanFileHolderException;
 import gda.util.TestUtils;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import javax.measure.unit.NonSI;
 import javax.measure.unit.SI;
 import javax.vecmath.Vector3d;
 
 import org.jscience.physics.amount.Amount;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import uk.ac.diamond.scisoft.analysis.crystallography.HKL;
 import uk.ac.diamond.scisoft.analysis.crystallography.MillerSpace;
 import uk.ac.diamond.scisoft.analysis.crystallography.UnitCell;
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.diffraction.PowderRingsUtils.FitFunction;
 import uk.ac.diamond.scisoft.analysis.io.ADSCImageLoader;
 import uk.ac.diamond.scisoft.analysis.io.DataHolder;
 import uk.ac.diamond.scisoft.analysis.io.NumPyFileSaver;
 import uk.ac.diamond.scisoft.analysis.roi.CircularFitROI;
 import uk.ac.diamond.scisoft.analysis.roi.CircularROI;
 import uk.ac.diamond.scisoft.analysis.roi.EllipticalFitROI;
 import uk.ac.diamond.scisoft.analysis.roi.EllipticalROI;
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 import uk.ac.diamond.scisoft.analysis.roi.PolylineROI;
 
 public class PowderRingsUtilsTest {
 	static String TestFileFolder;
 	@BeforeClass
 	static public void setUpClass() {
 		TestFileFolder = TestUtils.getGDALargeTestFilesLocation();
 		if( TestFileFolder == null){
 			Assert.fail("TestUtils.getGDALargeTestFilesLocation() returned null - test aborted");
 		}
 	}
 
 	// NIST Silicon SRM 640C as mentioned in IUCR International Tables vC 5.2.10
 	static final double WAVELENGTH = 1.5405929; // in nm
 	static final double LATTICE_PARAMETER = 0.5431195;  // in nm
 	static final double[] CONE_ANGLES = {28.411, 47.300, 56.120};
 //	static final double[] CONE_ANGLES = {28.411, 47.300, 56.120, 69.126, 76.372, 88.025,
 //		94.947, 106.701, 114.084, 127.534, 136.880, 158.603}; // in degrees
 	UnitCell siliconCell;
 	MillerSpace mSpace;
 	private ArrayList<HKL> spacings;
 
 	@Before
 	public void setUpSilicon() {
 		siliconCell = new UnitCell(LATTICE_PARAMETER);
 		spacings = new ArrayList<HKL>();
 		for (double c : CONE_ANGLES) {
 			spacings.add(new HKL(Amount.valueOf(0.5 * WAVELENGTH / Math.sin(0.5 * Math.toRadians(c)), NonSI.ANGSTROM)));
 		}
 //		mSpace = new MillerSpace(siliconCell, null);
 	}
 
 	@Test
 	public void findEllipse() {
 		AbstractDataset image = null;
 		try {
 			image = new ADSCImageLoader(TestFileFolder + "ADSCImageTest/Si_200_1_0001.img").loadFile().getDataset(0);
 		} catch (Exception e) {
 			Assert.fail("Could not open image");
 		}
 
 		CircularROI roi = new CircularROI(631, 1528.6, 1533.9);
 		PolylineROI points = PowderRingsUtils.findPOIsNearCircle(null, image, null, roi);
 		System.err.println(points);
 		System.err.println(new CircularFitROI(points));
 		System.err.println(new EllipticalFitROI(points));
 
 		System.err.println(PowderRingsUtils.fitAndTrimOutliers(null, points, true));
 		System.err.println(PowderRingsUtils.fitAndTrimOutliers(null, points, false));
 	}
 
 	private static final int N_W = 256;
 	private static final int N_D = 256;
 	private static final int N_T = 64;
 
 	@Ignore
 	public void checkFitFunction() {
 		double pixel = 0.25; // in mm
 		double distance = 153.0; // in mm
 		double wavelength = WAVELENGTH * 1e-7; // in mm
 	
 		List<EllipticalROI> ells = new ArrayList<EllipticalROI>();
 		List<Double> list = new ArrayList<Double>();
 		for (HKL d : spacings) {
 			double xi = 0.5 * WAVELENGTH / d.getD().doubleValue(NonSI.ANGSTROM);
 			list.add(d.getD().doubleValue(SI.MILLIMETRE));
 			double om = 0.5 - xi*xi;
 			double r = distance * xi * Math.sqrt(0.5 + om) / (pixel * om);
 			ells.add(new EllipticalROI(r, 0, 0));
 		}
 
 		DetectorProperties det = new DetectorProperties(new Vector3d(0, 0, 1), 10, 10, pixel, pixel, 1, 0, 0);
 		FitFunction f = PowderRingsUtils.createQFitFunction2(ells, det, 1., false);
 		f.setSpacings(list);
 		double[] init = new double[3];
 		DoubleDataset rms = new DoubleDataset(N_W, N_D, N_T);
 		int l = 0;
 		for (int i = 0; i < N_W; i++) {
 			init[0] = wavelength + (i - N_W*0.5)*1e-7*0.01; // 0.01A
 			for (int j = 0; j < N_D; j++) {
 				init[1] = distance + (j - N_D*0.5)*0.01; // 0.01mm
 				for (int k = 0; k < N_T; k++) {
 					init[2] = Math.sin((k/128.)/N_T); // 1/128 radians
 					double x = f.value(init);
 					rms.setAbs(l++, x);
 				}
 			}
 		}
 		DataHolder dh = new DataHolder();
 		dh.addDataset("RMS", rms);
 
 		String file = "/tmp/rms.npy";
 		try {
 			new NumPyFileSaver(file).saveFile(dh);
 		} catch (ScanFileHolderException e) {
 			System.err.println("Problem saving file: " + file);
 		}
 	}
 
 	@Test
 	public void calibrateDetector() {
 		double pixel = 0.25; // in mm
 		double distance = 153.0; // in mm
 		DetectorProperties det = new DetectorProperties(new Vector3d(0, 0, distance*0.99), 3000, 3000, pixel, pixel, null);
 
 		DiffractionCrystalEnvironment env = new DiffractionCrystalEnvironment(WAVELENGTH*1.01);
 
 		Random rnd = new Random(12345);
 		for (int i = 0; i < 15; i+= 5) {
 			System.err.println("Angle " + i);
 			det.setNormalAnglesInDegrees(i, 0, 0);
 			List<EllipticalROI> ells = new ArrayList<EllipticalROI>();
 			List<IROI> rois = new ArrayList<IROI>();
 			for (HKL d : spacings) {
 				EllipticalROI e = (EllipticalROI) DSpacing.conicFromDSpacing(det, WAVELENGTH,
 						d.getD().doubleValue(NonSI.ANGSTROM));
 				double r = e.getSemiAxis(0) + rnd.nextDouble() * 3;
 				if (e.isCircular()) {
 					e.setSemiAxis(0, r);
 					e.setSemiAxis(1, r);
 				} else {
 					e.setSemiAxis(0, r);
 					e.setSemiAxis(1, e.getSemiAxis(0) + rnd.nextDouble() * 3);
 				}
 				ells.add(e);
 				rois.add(e);
 			}
 
 			QSpace q = PowderRingsUtils.fitEllipsesToQSpace(null, det, env, ells, spacings);
 			DetectorProperties nDet = q.getDetectorProperties();
 
 			Assert.assertEquals("Distance", distance, nDet.getDetectorDistance(), 5*(i+1));
 			Assert.assertEquals("Tilt", det.getTiltAngle(), nDet.getTiltAngle(), 6e-2*(i+1));
 			Assert.assertEquals("Wavelength", WAVELENGTH, q.getWavelength(), 2e-2);
 
 			q = PowderRingsUtils.fitAllEllipsesToQSpace(null, det, env, rois, spacings);
 			nDet = q.getDetectorProperties();
 
 			Assert.assertEquals("Distance", distance, nDet.getDetectorDistance(), 5*(i+1));
 			Assert.assertEquals("Tilt", det.getTiltAngle(), nDet.getTiltAngle(), 6e-2*(i+1));
 			Assert.assertEquals("Wavelength", WAVELENGTH, q.getWavelength(), 2e-2);
 		}
 	}
 
 	@Test
 	public void testFitFunction() {
 		double wavelength = WAVELENGTH * 1e-7; // in mm
 	
 		List<EllipticalROI> ells = new ArrayList<EllipticalROI>();
 		List<Double> list = new ArrayList<Double>();
 
 		DetectorProperties det = DetectorProperties.getDefaultDetectorProperties(300, 300);
 		det.setNormalAnglesInDegrees(21, 0, 30);
 		for (HKL d : spacings) {
 			double dspacing = d.getD().doubleValue(SI.MILLIMETRE); 
 			list.add(dspacing);
 			ells.add((EllipticalROI) DSpacing.conicFromDSpacing(det, wavelength, dspacing));
 		}
 
 		FitFunction f;
 		double[] init;
 
 		// test functions
 		f = PowderRingsUtils.createQFitFunction(ells, det, wavelength, false);
 		init = f.getInit();
 		f.setSpacings(list);
 		Assert.assertEquals("", 0, f.value(init), 1e-2);
 
 		f = PowderRingsUtils.createQFitFunction(ells, det, wavelength, true);
 		init = f.getInit();
 		f.setSpacings(list);
 		Assert.assertEquals("", 0, f.value(init), 1e-2);
 
 		f = PowderRingsUtils.createQFitFunction2(ells, det, wavelength, false);
 		init = f.getInit();
 		f.setSpacings(list);
 		Assert.assertEquals("", 0, f.value(init), 1e-2);
 
 		f = PowderRingsUtils.createQFitFunction2(ells, det, wavelength, true);
 		init = f.getInit();
 		f.setSpacings(list);
 		Assert.assertEquals("", 0, f.value(init), 1e-2);
 
 		det.setNormalAnglesInDegrees(0, 0, 0);
 		ells.clear();
 		list.clear();
 		for (HKL d : spacings) {
 			double dspacing = d.getD().doubleValue(SI.MILLIMETRE); 
 			list.add(dspacing);
 			ells.add((EllipticalROI) DSpacing.conicFromDSpacing(det, wavelength, dspacing));
 		}
 
 		f = PowderRingsUtils.createQFitFunction3(ells, det, wavelength, false);
 		init = f.getInit();
 		f.setSpacings(list);
 		Assert.assertEquals("", 0, f.value(init), 1e-2);
 
 		f = PowderRingsUtils.createQFitFunction3(ells, det, wavelength, true);
 		init = f.getInit();
 		f.setSpacings(list);
 		Assert.assertEquals("", 0, f.value(init), 1e-2);
 
 		f = PowderRingsUtils.createQFitFunction4(ells, det, wavelength, false);
 		init = f.getInit();
 		f.setSpacings(list);
 		Assert.assertEquals("", 0, f.value(init), 1e-2);
 
 		f = PowderRingsUtils.createQFitFunction4(ells, det, wavelength, true);
 		init = f.getInit();
 		f.setSpacings(list);
 		Assert.assertEquals("", 0, f.value(init), 1e-2);
 
 		f = PowderRingsUtils.createQFitFunction5(ells, det, wavelength, false);
 		init = f.getInit();
 		f.setSpacings(list);
 		Assert.assertEquals("", 0, f.value(init), 1e-2);
 
 		f = PowderRingsUtils.createQFitFunction5(ells, det, wavelength, true);
 		init = f.getInit();
 		f.setSpacings(list);
 		Assert.assertEquals("", 0, f.value(init), 1e-2);
 	}
 }
