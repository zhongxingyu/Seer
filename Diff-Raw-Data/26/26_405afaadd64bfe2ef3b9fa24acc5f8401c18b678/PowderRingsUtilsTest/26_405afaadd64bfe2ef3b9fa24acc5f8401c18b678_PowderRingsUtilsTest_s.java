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
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import javax.measure.unit.NonSI;
 import javax.vecmath.Vector3d;
 
 import org.jscience.physics.amount.Amount;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import uk.ac.diamond.scisoft.analysis.TestUtils;
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
 	static final double WAVELENGTH = 1.5405929; // in Angstroms
 	static final double LATTICE_PARAMETER = 5.431195;  // in Angstroms
 //	static final double[] CONE_ANGLES = {28.411};
 //	static final double[] CONE_ANGLES = {28.411, 47.300, 56.120};
 	static final double[] CONE_ANGLES = {28.411, 47.300, 56.120, 69.126};
 //	static final double[] CONE_ANGLES = {28.411, 47.300, 56.120, 69.126, 76.372, 88.025,
 //	94.947, 106.701, 114.084, 127.534, 136.880, 158.603}; // in degrees
 	static final double[] SPACINGS = {3.13570, 1.92022, 1.63757, 1.35780}; // in Angstromss
 //	static final double[] SPACINGS = {3.13570, 1.92022, 1.63757, 1.35780,
 //	1.24600, 1.10864, 1.04523, 1.04523, 0.96011, 0.91804};
 
 	UnitCell siliconCell;
 	MillerSpace mSpace;
 	private ArrayList<HKL> spacings;
 
 	@Before
 	public void setUpSilicon() {
 		siliconCell = new UnitCell(LATTICE_PARAMETER);
 		spacings = new ArrayList<HKL>();
 		for (double d : SPACINGS) {
 			spacings.add(new HKL(Amount.valueOf(d, NonSI.ANGSTROM)));
 		}
 		PowderRingsUtils.seed = 1237L; // set seed for evolution strategy fitting
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
 
 	private static final int N_W = 128;
 	private static final int N_D = 128;
 //	private static final int N_T = 64;
 	private static final int N_TD = 32;
 
 	@Ignore
 	@Test
 	public void createRMSDatasets() {
 		double pixel = 0.25; // in mm
 		double distance = 153.0; // in mm
 		DetectorProperties det = new DetectorProperties(new Vector3d(0, 0, distance), 10, 10, pixel, pixel, 0, 0, 0);
 
 		double wavelength = WAVELENGTH; // in Angstroms
 	
 		List<EllipticalROI> ells = new ArrayList<EllipticalROI>();
 		List<Double> list = new ArrayList<Double>();
 		for (HKL d : spacings) {
 			double s = d.getD().doubleValue(NonSI.ANGSTROM);
 			IROI r = DSpacing.conicFromDSpacing(det, wavelength, s);
 			if (r instanceof EllipticalROI) {
 				ells.add((EllipticalROI) r);
 				list.add(s);
 			}
 		}
 
 		save("/tmp/rms7.npy", calcFit7Values(det, wavelength, distance, list, ells));
 		save("/tmp/rms4.npy", calcFit4Values(det, wavelength, distance, list, ells));
 	}
 
 	private DoubleDataset calcFit7Values(DetectorProperties det, double wavelength, double distance, List<Double> list, List<EllipticalROI> ells) {
 		FitFunction f = PowderRingsUtils.createQFitFunction7(ells, det, wavelength, false);
 		f.setSpacings(list);
 		double[] init = new double[7];
 		DoubleDataset rms = new DoubleDataset(N_W, N_D, N_D, N_TD);
 		int t = 0;
 		init[2] = 0;
 		init[5] = 0;
 		init[6] = 0;
 		for (int i = 0; i < N_W; i++) {
 			init[0] = wavelength + (i - N_W*0.5)*0.01; // 0.01A
 			for (int j = 0; j < N_D; j++) {
 				init[1] = (j - N_D*0.5)*0.01; // 0.01mm
 				for (int l = 0; l < N_D; l++) {
 					init[3] = distance + (l - N_D * 0.5) * 0.01; // 0.01mm
 					for (int m = 0; m < N_TD; m++) {
 						init[4] = (m - N_TD * 0.5) / 20; // 1/20 degrees
 						double x = f.value(init);
 						rms.setAbs(t++, x);
 					}
 				}
 			}
 		}
 		return rms;
 	}
 
 	private DoubleDataset calcFit4Values(DetectorProperties det, double wavelength, double distance, List<Double> list, List<EllipticalROI> ells) {
 		FitFunction f = PowderRingsUtils.createQFitFunction4(ells, det, wavelength, false);
 		f.setSpacings(list);
 		double[] init = new double[4];
 		DoubleDataset rms = new DoubleDataset(N_W, N_D, N_D);
 		int t = 0;
 		init[1] = 0;
 		for (int i = 0; i < N_W; i++) {
 			init[0] = wavelength + (i - N_W*0.5)*0.01; // 0.01A
 			for (int k = 0; k < N_D; k++) {
 				init[2] = (k - N_D * 0.5) * 0.01; // 0.01mm
 				for (int l = 0; l < N_D; l++) {
 					init[3] = distance + (l - N_D * 0.5) * 0.01; // 0.01mm
 					double x = f.value(init);
 					rms.setAbs(t++, x);
 				}
 			}
 		}
 		return rms;
 	}
 
 	private void save(String file, DoubleDataset rms) {
 		DataHolder dh = new DataHolder();
 		dh.addDataset("RMS", rms);
 
 		try {
 			new NumPyFileSaver(file).saveFile(dh);
 		} catch (ScanFileHolderException e) {
 			System.err.println("Problem saving file: " + file);
 		}
 	}
 
 	/**
 	 * @param rnd
 	 * @param size
 	 * @return [-size, size]
 	 */
 	private static double nextDouble(Random rnd, double size) {
 		return 2 * size * (rnd.nextDouble() - 0.5);
 	}
 
 	/**
 	 * @param rnd
 	 * @param frac
 	 * @return [1-frac, 1+frac]
 	 */
 	private static double nextFractionDiff(Random rnd, double frac) {
 		return 1 + nextDouble(rnd, frac);
 	}
 
 	@Test
 	public void calibrateDetector() {
 		double pixel = 0.25; // in mm
 		double distance = 153.0; // in mm
 		DetectorProperties det = new DetectorProperties(new Vector3d(0, 0, distance), 3000, 3000, pixel, pixel, null);
 
 		DiffractionCrystalEnvironment env = new DiffractionCrystalEnvironment(WAVELENGTH);
 
 		Random rnd = new Random(12345);
 		for (int i = 0; i < 15; i+= 5) {
 			for (int j = 0; j <= 180; j += 30) {
 				System.err.println("Angle " + i + "; " + j);
 				det.setNormalAnglesInDegrees(i, 0, j);
 				List<EllipticalROI> ells = new ArrayList<EllipticalROI>();
 				List<HKL> ds = new ArrayList<HKL>();
 				QSpace q;
 				DetectorProperties nDet;
 				double roll;
 
 				// perfect data but rough initial parameters
 				ds.clear();
 				ells.clear();
 				for (HKL d : spacings) {
 					EllipticalROI e;
 					try {
 						e = (EllipticalROI) DSpacing.conicFromDSpacing(det, env.getWavelength(),
 								d.getD().doubleValue(NonSI.ANGSTROM));
 						ds.add(d);
 						ells.add(e);
 					} catch (UnsupportedOperationException ex) {
 						continue;
 					}
 				}
 
 				for (int r = 0; r < 1; r++) {
 					DiffractionCrystalEnvironment renv = new DiffractionCrystalEnvironment(env.getWavelength() * nextFractionDiff(rnd, 0.01));
 					DetectorProperties rdet = det.clone();
 					rdet.setDetectorDistance(rdet.getDetectorDistance() * nextFractionDiff(rnd, 0.01));
 					double[] c = rdet.getBeamCentreCoords();
 					c[0] += nextDouble(rnd, 0.5);
 					c[1] += nextDouble(rnd, 0.5);
 					rdet.setBeamCentreCoords(c);
 					double[] a = rdet.getNormalAnglesInDegrees();
 					a[0] += nextDouble(rnd, 0.5);
 					a[1] += nextDouble(rnd, 0.5);
 					a[2] += nextDouble(rnd, 0.5);
 					rdet.setNormalAnglesInDegrees(a);
 					q = PowderRingsUtils.fitAllEllipsesToQSpace(null, rdet, renv, ells, ds, true);
 					nDet = q.getDetectorProperties();
 
 					System.err.println(q.getResidual() + "; D = " + nDet.getDetectorDistance() + "; ta = "
 							+ Math.toDegrees(nDet.getTiltAngle()) + "; ra = "
 							+ nDet.getNormalAnglesInDegrees()[2] + "; wl = " + q.getWavelength());
 //					Assert.assertEquals("Residual", 0, q.getResidual(), 1);
 					Assert.assertEquals("Distance", det.getDetectorDistance(), nDet.getDetectorDistance(), 30);
 					Assert.assertEquals("Tilt", det.getTiltAngle(), nDet.getTiltAngle(), 8e-3);
 					roll = nDet.getNormalAnglesInDegrees()[2];
 					if (roll < -(j*0.3+20))
 						roll += 360;
 					Assert.assertEquals("Roll", det.getNormalAnglesInDegrees()[2], roll, j*0.3+20);
 					Assert.assertEquals("Wavelength", env.getWavelength(), q.getWavelength(), 1e-1);
 				}
 
 				// noisy data but exact initial parameters
 				for (EllipticalROI e : ells) {
 					double r = e.getSemiAxis(0) + nextDouble(rnd, 2);
 					if (e.isCircular()) {
 						e.setSemiAxis(0, r);
 						e.setSemiAxis(1, r);
 					} else {
 						e.setSemiAxis(0, r);
 						e.setSemiAxis(1, e.getSemiAxis(1) + nextDouble(rnd, 2));
 					}
 					double[] pt = e.getPointRef();
 					e.setPoint(pt[0] + nextDouble(rnd, 1), pt[1] + nextDouble(rnd, 2));
 					e.setAngleDegrees(e.getAngleDegrees() + nextDouble(rnd, 2));
 				}
 
 				q = PowderRingsUtils.fitAllEllipsesToQSpace(null, det, env, ells, ds, true);
 				nDet = q.getDetectorProperties();
 
 				System.err.println(q.getResidual() + "; D = " + nDet.getDetectorDistance() + "; ta = "
 						+ Math.toDegrees(nDet.getTiltAngle()) + "; ra = "
 						+ nDet.getNormalAnglesInDegrees()[2] + "; wl = " + q.getWavelength());
 				Assert.assertEquals("Distance", det.getDetectorDistance(), nDet.getDetectorDistance(), 5 * (i + 1.5));
 				Assert.assertEquals("Tilt", det.getTiltAngle(), nDet.getTiltAngle(), 7e-2 * (i + 1));
 				roll = nDet.getNormalAnglesInDegrees()[2];
 				if (roll < -31)
 					roll += 360;
 				Assert.assertEquals("Roll", det.getNormalAnglesInDegrees()[2], roll, 31);
 				Assert.assertEquals("Wavelength", env.getWavelength(), q.getWavelength(), 1e-9);
 
 				q = PowderRingsUtils.fitAllEllipsesToQSpace(null, det, env, ells, ds, false);
 				nDet = q.getDetectorProperties();
 
 				System.err.println(q.getResidual() + "; D = " + nDet.getDetectorDistance() + "; ta = "
 						+ Math.toDegrees(nDet.getTiltAngle()) + "; ra = "
 						+ nDet.getNormalAnglesInDegrees()[2] + "; wl = " + q.getWavelength());
 				Assert.assertEquals("Distance", det.getDetectorDistance(), nDet.getDetectorDistance(), 5 * (i + 1));
 				Assert.assertEquals("Tilt", det.getTiltAngle(), nDet.getTiltAngle(), 6e-2 * (i + 1));
 				roll = nDet.getNormalAnglesInDegrees()[2];
 				if (roll < -25)
 					roll += 360;
 				Assert.assertEquals("Roll", det.getNormalAnglesInDegrees()[2], roll, 25);
 				Assert.assertEquals("Wavelength", env.getWavelength(), q.getWavelength(), 3e-2);
 			}
 		}
 	}
 
 	@Test
 	public void calibrateDetectors() {
 		double pixel = 0.25; // in mm
 		double[] distances = new double[] {153.0, 253.0, 354.}; // in mm
 
 		List<DetectorProperties> dets = new ArrayList<DetectorProperties>();
 
 		for (double d : distances) {
 			dets.add(new DetectorProperties(new Vector3d(0, 0, d), 3000, 3000, pixel, pixel, null));
 		}
 
 		DiffractionCrystalEnvironment env = new DiffractionCrystalEnvironment(WAVELENGTH);
 
 		List<List<? extends IROI>> le = new ArrayList<List<? extends IROI>>();
 
 		Random rnd = new Random(12345);
 		for (int i = 0; i < 15; i += 5) {
 			for (int j = 0; j <= 180; j += 30) {
 				System.err.println("Angle " + i + "; " + j);
 				le.clear();
 
 				// perfect data but rough initial parameters
 				for (DetectorProperties det : dets) {
 					det.setNormalAnglesInDegrees(i, 0, j);
 					List<EllipticalROI> ells = new ArrayList<EllipticalROI>();
 					for (HKL d : spacings) {
 						EllipticalROI e = null;
 						try {
 							e = (EllipticalROI) DSpacing.conicFromDSpacing(det, env.getWavelength(), d.getD()
 									.doubleValue(NonSI.ANGSTROM));
 						} catch (UnsupportedOperationException ex) {
 							continue;
 						} finally {
 							ells.add(e);
 						}
 					}
 					le.add(ells);
 				}
 
 				double roll;
 				List<QSpace> qs;
 				for (int r = 0; r < 1; r++) {
 //					DiffractionCrystalEnvironment renv = new DiffractionCrystalEnvironment(env.getWavelength());
 					DiffractionCrystalEnvironment renv = new DiffractionCrystalEnvironment(env.getWavelength() * nextFractionDiff(rnd, 0.01));
 					List<DetectorProperties> rDets = new ArrayList<DetectorProperties>();
 					for (DetectorProperties det : dets) {
 						DetectorProperties rdet = det.clone();
 						rdet.setDetectorDistance(rdet.getDetectorDistance() * nextFractionDiff(rnd, 0.01));
 						double[] c = rdet.getBeamCentreCoords();
 						c[0] += nextDouble(rnd, 0.5);
 						c[1] += nextDouble(rnd, 0.5);
 						rdet.setBeamCentreCoords(c);
 						double[] a = rdet.getNormalAnglesInDegrees();
 						a[0] += nextDouble(rnd, 0.5);
 						a[1] += nextDouble(rnd, 0.5);
 						a[2] += nextDouble(rnd, 0.5);
 						rdet.setNormalAnglesInDegrees(a);
 						rDets.add(rdet);
 					}
 
 					qs = PowderRingsUtils.fitAllEllipsesToAllQSpaces(null, rDets, renv, le, spacings);
 					Assert.assertEquals("Qs", distances.length, qs.size());
 					for (int k = 0; k < distances.length; k++) {
 						QSpace q = qs.get(k);
 						DetectorProperties nDet = q.getDetectorProperties();
 
 						System.err.println(q.getResidual() + "; D = " + nDet.getDetectorDistance() + "; ta = "
 								+ Math.toDegrees(nDet.getTiltAngle()) + "; ra = "
 								+ nDet.getNormalAnglesInDegrees()[2] + "; wl = " + q.getWavelength());
 //						Assert.assertEquals("Residual", 0, q.getResidual(), 1);
 						Assert.assertEquals("Distance", dets.get(k).getDetectorDistance(), nDet.getDetectorDistance(), 5);
 						Assert.assertEquals("Tilt", dets.get(k).getTiltAngle(), nDet.getTiltAngle(), 1.5e-2);
 						roll = nDet.getNormalAnglesInDegrees()[2];
 						if (roll < -1)
 							roll += 360;
 						Assert.assertEquals("Roll", dets.get(k).getNormalAnglesInDegrees()[2],
 								roll, j*0.3+5);
 						Assert.assertEquals("Wavelength", env.getWavelength(), q.getWavelength(), 1e-1);
 					}
 				}
 
 				// noisy data but exact initial parameters
 				for (List<? extends IROI> ls : le) {
 					@SuppressWarnings("unchecked")
 					List<EllipticalROI> ells = (List<EllipticalROI>) ls;
 					for (EllipticalROI e : ells) {
 						if (e == null)
 							continue;
 
 						double r = e.getSemiAxis(0) + nextDouble(rnd, 2);
 						if (e.isCircular()) {
 							e.setSemiAxis(0, r);
 							e.setSemiAxis(1, r);
 						} else {
 							e.setSemiAxis(0, r);
 							e.setSemiAxis(1, e.getSemiAxis(1) + nextDouble(rnd, 2));
 						}
 						double[] pt = e.getPointRef();
 						e.setPoint(pt[0] + nextDouble(rnd, 1), pt[1] + nextDouble(rnd, 2));
 						e.setAngleDegrees(e.getAngleDegrees() + nextDouble(rnd, 2));
 					}
 				}
 
 				qs = PowderRingsUtils.fitAllEllipsesToAllQSpaces(null, dets, env, le, spacings);
 				Assert.assertEquals("Qs", distances.length, qs.size());
 				for (int k = 0; k < distances.length; k++) {
 					QSpace q = qs.get(k);
 					DetectorProperties nDet = q.getDetectorProperties();
 
 					System.err.println(q.getResidual() + "; D = " + nDet.getDetectorDistance() + "; ta = "
 							+ Math.toDegrees(nDet.getTiltAngle()) + "; ra = "
 							+ nDet.getNormalAnglesInDegrees()[2] + "; wl = " + q.getWavelength());
 					Assert.assertEquals("Distance", distances[k], nDet.getDetectorDistance(), distances[k] * 5e-2);
 					Assert.assertEquals("Tilt", dets.get(k).getTiltAngle(), nDet.getTiltAngle(), 1e-1 * (i + 1));
 					roll = nDet.getNormalAnglesInDegrees()[2];
 					if (roll < -30)
 						roll += 360;
 					Assert.assertEquals("Roll", dets.get(k).getNormalAnglesInDegrees()[2],
 							roll, i == 0 ? 60 : 30);
 					Assert.assertEquals("Wavelength", env.getWavelength(), q.getWavelength(), 2e-2);
 				}
 			}
 
 		}
 	}
 
 	@Test
 	public void testFitFunction() {
 		double wavelength = WAVELENGTH; // in Angstroms
 	
 		List<EllipticalROI> ells = new ArrayList<EllipticalROI>();
 		List<Double> list = new ArrayList<Double>();
 
 		DetectorProperties det = DetectorProperties.getDefaultDetectorProperties(300, 300);
 		det.setNormalAnglesInDegrees(21, 0, 30);
 		System.out.println("1st detector: " + det);
 		DetectorProperties det2 = DetectorProperties.getDefaultDetectorProperties(300, 300);
 		det2.setNormalAnglesInDegrees(18, 0, 60);
 		det2.setDetectorDistance(325.7);
 		System.out.println("2nd detector: " + det2);
 
 		DetectorProperties[] dets = new DetectorProperties[] {det, det2};
 
 		for (HKL d : spacings) {
 			double dspacing = d.getD().doubleValue(NonSI.ANGSTROM); 
 			list.add(dspacing);
 			try {
 				ells.add((EllipticalROI) DSpacing.conicFromDSpacing(det, wavelength, dspacing));
 				ells.add((EllipticalROI) DSpacing.conicFromDSpacing(det2, wavelength, dspacing));
 			} catch (UnsupportedOperationException e) {
 				// do nothing
 			}
 		}
 
 		FitFunction f;
 		double[] init;
 
 		// test functions
 		for (DetectorProperties dt : dets) {
 			for (int j = 0; j <= 180; j += 30) {
 				dt.setNormalAnglesInDegrees(0, 0, j);
 				ells.clear();
 				list.clear();
 				for (HKL d : spacings) {
 					double dspacing = d.getD().doubleValue(NonSI.ANGSTROM);
 					try {
 						EllipticalROI e = (EllipticalROI) DSpacing.conicFromDSpacing(dt, wavelength, dspacing);
 						list.add(dspacing);
 						ells.add(e);
 					} catch (Exception e) {
 						// do nothing
 					}
 				}
 
 				f = PowderRingsUtils.createQFitFunction4(ells, dt, wavelength, false);
 				init = f.getInit();
 				f.setSpacings(list);
 				Assert.assertEquals("", 0, f.value(init), 1e-2);
 
 				f = PowderRingsUtils.createQFitFunction4(ells, dt, wavelength, true);
 				init = f.getInit();
 				f.setSpacings(list);
 				Assert.assertEquals("", 0, f.value(init), 1e-2);
 			}
 		}
 
 		List<List<EllipticalROI>> le = new ArrayList<List<EllipticalROI>>();
 		List<DetectorProperties> ld = new ArrayList<DetectorProperties>();
 		ld.add(det);
 		ld.add(det2);
 		List<Double> bList = new ArrayList<Double>();
 		for (int i = 0; i <= 20; i += 5) {
 			for (int k = 0; k <= 20; k += 5) {
 				for (int j = 0; j <= 180; j += 30) {
 					le.clear();
 					bList.clear();
 					for (int l = 0; l < 2; l++) {
 						DetectorProperties dt = dets[l];
 						dt.setNormalAnglesInDegrees(i-l*2.3, k, j+3.5*l);
 						ells.clear();
 						list.clear();
 						for (HKL d : spacings) {
 							double dspacing = d.getD().doubleValue(NonSI.ANGSTROM);
 							try {
 								EllipticalROI e = (EllipticalROI) DSpacing.conicFromDSpacing(dt, wavelength, dspacing);
 								list.add(dspacing);
 								ells.add(e);
 							} catch (Exception e) {
 								// do nothing
 							}
 						}
 
 						f = PowderRingsUtils.createQFitFunction7(ells, dt, wavelength, false);
 						init = f.getInit();
 						f.setSpacings(list);
 						Assert.assertEquals("", 0, f.value(init), 1e-2);
 
 						f = PowderRingsUtils.createQFitFunction7(ells, dt, wavelength, true);
 						init = f.getInit();
 						f.setSpacings(list);
 						Assert.assertEquals("", 0, f.value(init), 1e-2);
 
 						le.add(new ArrayList<EllipticalROI>(ells));
 						bList.addAll(list);
 					}
 
 					f = PowderRingsUtils.createQFitFunctionForAllImages(le, ld, wavelength);
 					init = f.getInit();
 					f.setSpacings(bList);
 					Assert.assertEquals("", 0, f.value(init), 1e-2);
 				}
 			}
 		}
 	}
 }
