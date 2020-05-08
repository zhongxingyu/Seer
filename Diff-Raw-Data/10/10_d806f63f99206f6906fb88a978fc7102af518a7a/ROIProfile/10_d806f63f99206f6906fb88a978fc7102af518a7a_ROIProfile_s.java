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
 
 package uk.ac.diamond.scisoft.analysis.roi;
 
 import java.util.List;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.FloatDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Maths;
 import uk.ac.diamond.scisoft.analysis.dataset.function.DatasetToDatasetFunction;
 import uk.ac.diamond.scisoft.analysis.dataset.function.Integrate2D;
 import uk.ac.diamond.scisoft.analysis.dataset.function.LineSample;
 import uk.ac.diamond.scisoft.analysis.dataset.function.MapToPolarAndIntegrate;
 import uk.ac.diamond.scisoft.analysis.dataset.function.MapToRotatedCartesianAndIntegrate;
 
 /**
  * Utility methods for calculating region of interest profiles.
  */
 public class ROIProfile {
 	/**
 	 * @param data
 	 * @param lroi
 	 * @param step
 	 *            size
 	 * @return line profile
 	 */
 	public static AbstractDataset[] line(IDataset data, LinearROI lroi, double step) {
 		int[] spt = lroi.getIntPoint();
 		int[] ept = lroi.getIntEndPoint();
 		AbstractDataset[] profiles = new AbstractDataset[] { null, null };
 
 		LineSample ls = new LineSample(spt[0], spt[1], ept[0], ept[1], step);
 
 		List<AbstractDataset> dsets = ls.value(data);
 
 		if (dsets == null)
 			return null;
 
 		profiles[0] = dsets.get(0);
 
 		if (lroi.isCrossHair()) {
 			spt = lroi.getPerpendicularBisectorIntPoint(0.0);
 			ept = lroi.getPerpendicularBisectorIntPoint(1.0);
 
 			LineSample bls = new LineSample(spt[0], spt[1], ept[0], ept[1], step);
 
 			dsets = bls.value(data);
 
 			if (dsets != null)
 				profiles[1] = dsets.get(0);
 		}
 
 		return profiles;
 	}
 
 	private static void clippingCompensate(IDataset data, IDataset mask, AbstractDataset[] profiles,
 			DatasetToDatasetFunction map) {
 		// normalise plot for case when region is clipped to size of image
 		// and/or when a mask is specified
 		if (mask == null) {
 			mask = AbstractDataset.ones(data.getShape(), AbstractDataset.BOOL);
 		}
 
 		List<AbstractDataset> dsets = map.value(mask);
 		AbstractDataset nintx = dsets.get(1);
 		AbstractDataset ninty = dsets.get(0);
 		// calculate fraction in each element that was not clipped
 		nintx = Maths.dividez(nintx, dsets.get(3));
 		ninty = Maths.dividez(ninty, dsets.get(2));
 
 		profiles[0] = Maths.dividez(profiles[0], nintx);
 		profiles[1] = Maths.dividez(profiles[1], ninty);
 	}
 
 	/**
 	 * @param data
 	 * @param rroi
 	 * @return box profile
 	 */
 	public static AbstractDataset[] box(AbstractDataset data, RectangularROI rroi) {
 		return box(data, null, rroi);
 	}
 
 	/**
 	 * @param data
 	 * @param mask
 	 *            used for clipping compensation (can be null)
 	 * @param rroi
 	 * @return box profile
 	 */
 	public static AbstractDataset[] box(AbstractDataset data, AbstractDataset mask, RectangularROI rroi) {
 		
 		return ROIProfile.box(data, mask, rroi, false);
 	}
 	
 	/**
 	 * @param data
 	 * @param mask
 	 *            used for clipping compensation (can be null)
 	 * @param rroi
 	 * @param maskWithNans - normally masked pixels will use a multiply with 0 to mask. The plotting
 	 *                       deals with Nans however, in this case we can set maskWithNans true and masked
 	 *                       pixels are NaN instead of 0.
 	 * @return box profile
 	 */
 	public static AbstractDataset[] box(AbstractDataset data, AbstractDataset mask, RectangularROI rroi, boolean maskWithNans) {
 
 		int[] spt = rroi.getIntPoint();
 		int[] len = rroi.getIntLengths();
 		double ang = rroi.getAngle();
 		boolean clip = rroi.isClippingCompensation();
 		AbstractDataset[] profiles = new AbstractDataset[] { null, null };
 
 
 		if (len[0] == 0)
 			len[0] = 1;
 		if (len[1] == 0)
 			len[1] = 1;
 
 		if (ang == 0.0) {
 			
 			final int xtart  = Math.max(0,  spt[1]);
			final int xend   = Math.min(spt[1] + len[1],  data.getShape()[1]);
 			final int ystart = Math.max(0,  spt[0]);
			final int yend   = Math.min(spt[0] + len[0],  data.getShape()[0]);
 			
 			// We slice down data to reduce the work the masking and the integrate needs to do.
 			// TODO Does this always work? This makes large images profile better...
 			AbstractDataset slicedData = null;
 			try {
 			    slicedData = data.getSlice(new int[]{xtart,   ystart}, 
 					                       new int[]{xend,    yend},
 					                       new int[]{1,1});
 			} catch (Exception ne) {
 				// We cannot process the profiles for a region totally outside the image!
 				return null;
 			}
 			
 			final AbstractDataset slicedMask = mask!=null
 					                         ? mask.getSlice(new int[]{xtart, ystart}, 
 					                                         new int[]{xend,  yend},
 					                                         new int[]{1,1})
 					                         : null;
 			
 			
 			if (slicedMask != null && slicedData != null) {
 				if (slicedData.isCompatibleWith(slicedMask)) {
 					clip = true;
 					if (!maskWithNans || !(slicedMask instanceof BooleanDataset)) {
 						// convertToAbstractDataset should not be necessart here? 
 						// AbstractDataset is already here, the data is loaded - is this right?
 						slicedData = Maths.multiply(DatasetUtils.convertToAbstractDataset(slicedData), DatasetUtils.convertToAbstractDataset(slicedMask));
 					} else {
 						// Masks values to NaN, also changes Dtype to Float
 						slicedData = nanalize(slicedData, (BooleanDataset)slicedMask);
 					}
 				}
 			}
 
 			
 			Integrate2D int2d = new Integrate2D(0, 0, Math.min(len[0], data.getShape()[1]), Math.min(len[1], data.getShape()[0]));
 
 			List<AbstractDataset> dsets = int2d.value(slicedData);
 			if (dsets == null) return null;
 
 			profiles[0] = maskWithNans
 					    ? processColumnNans(dsets.get(1), slicedData)
 					    : dsets.get(1);
 			
 			profiles[1] = maskWithNans
 					    ?  processRowNans(dsets.get(0), slicedData)
 					    : dsets.get(0);
 
 		} else {
 			
 			if (mask != null && data != null) {
 				if (data.isCompatibleWith(mask)) {
 					clip = true;
 					// TODO both multiply and nanalize create copies of the whole data passed in
 					if (!maskWithNans || !(mask instanceof BooleanDataset)) {
 						// convertToAbstractDataset should not be necessart here? 
 						// AbstractDataset is already here, the data is loaded - is this right?
 						data = Maths.multiply(DatasetUtils.convertToAbstractDataset(data), DatasetUtils.convertToAbstractDataset(mask));
 					} else {
 						// Masks values to NaN, also changes Dtype to Float
 						data = nanalize(data, (BooleanDataset)mask);
 					}
 				}
 			}
 
 			MapToRotatedCartesianAndIntegrate rcmapint = new MapToRotatedCartesianAndIntegrate(spt[0], spt[1], len[0],
 					len[1], ang, false);
 			List<AbstractDataset> dsets = rcmapint.value(data);
 			if (dsets == null)
 				return null;
 
 			profiles[0] = dsets.get(1);
 			profiles[1] = dsets.get(0);
 
 			if (clip) {
 				clippingCompensate(data, mask, profiles, rcmapint);
 			}
 		}
 
 		return profiles;
 	}
 
 	/**
 	 * This method will nanalize any data which the mask has set to false. NOTE
 	 * the data and the mask must be precisely the same in size and value order.
 	 * 
 	 * No compatibility test is done for speed reasons, instead a failure will occur
 	 * during the nanalise operation.
 	 * 
 	 * It always returns a FloatDataset for speed and size reasons, this being the
 	 * smallest Dtype which allows NaNs.
 	 * 
 	 * 
 	 * @param data
 	 * @param mask
 	 * @return clone of dataset with NaNs at the appropriate place.
 	 */
 	public static FloatDataset nanalize(AbstractDataset data, BooleanDataset mask) {
 		
 		FloatDataset nanalized = new FloatDataset(data.getShape());
 		float[]      buffer    = nanalized.getData();
 		for (int i = 0; i < buffer.length; i++) {
 			buffer[i] = mask.getElementBooleanAbs(i)
 					  ? (float)data.getElementDoubleAbs(i) // NOTE: Do not round, just loose precision.
 					  : Float.NaN;
 		}
 		return nanalized;
 	}
 
 
 	private static AbstractDataset processColumnNans(AbstractDataset cols, AbstractDataset data) {
 		
 		
         MAIN_LOOP: for (int i = 0; i < cols.getSize(); i++) {
 			for(int j = 0; j<data.getShape()[0]; j++) {
 				if (!Float.isNaN(data.getFloat(j, i))) continue MAIN_LOOP; 
 			}
 			cols.set(Float.NaN, i);
 		}
 	    return cols;
 	}
 	
 	private static AbstractDataset processRowNans(AbstractDataset rows, AbstractDataset data) {
 		
 		
         MAIN_LOOP: for (int i = 0; i < rows.getSize(); i++) {
 			for(int j = 0; j<data.getShape()[1]; j++) {
 				if (!Float.isNaN(data.getFloat(i, j))) continue MAIN_LOOP; 
 			}
 			rows.set(Float.NaN, i);
 		}
 	    return rows;
 	}
 
 
 
 	/**
 	 * @param data
 	 * @param sroi
 	 * @return box profile
 	 */
 	public static AbstractDataset[] sector(AbstractDataset data, SectorROI sroi) {
 		return sector(data, null, sroi);
 	}
 
 	/**
 	 * @param data
 	 * @param sroi
 	 * @return box profile
 	 */
 	public static AbstractDataset[] sector(AbstractDataset data, AbstractDataset mask, SectorROI sroi) {
 		return sector(data, mask, sroi, true, true, false);
 	}
 
 	/**
 	 * @param data
 	 * @param mask
 	 *            used for clipping compensation (can be null)
 	 * @param sroi
 	 * @return sector profile
 	 */
 	public static AbstractDataset[] sector(AbstractDataset data, AbstractDataset mask, SectorROI sroi, boolean doRadial, boolean doAzimuthal, boolean fast) {
 		final double[] spt = sroi.getPointRef();
 		final double[] rad = sroi.getRadii();
 		final double[] ang = sroi.getAngles();
 		int symmetry = sroi.getSymmetry();
 		boolean clip = sroi.isClippingCompensation();
 		boolean aver = sroi.isAverageArea();
 		double dpp = sroi.getDpp();
 
 		AbstractDataset[] profiles = new AbstractDataset[] { null, null, null, null };
 
 		if (Math.abs(rad[0] - rad[1]) < 1 || Math.abs(ang[0] - ang[1]) < 1e-2) {
 			return null;
 		}
 
 		if (symmetry == SectorROI.FULL) {
 			MapToPolarAndIntegrate pmapfint = new MapToPolarAndIntegrate(spt[0], spt[1], rad[0], ang[0], rad[1], ang[0]
 					+ 2 * Math.PI, dpp, false);
 			if (mask != null && data != null)
 				if (mask.isCompatibleWith(data))
 					pmapfint.setMask(mask);
 			pmapfint.setClip(clip);
 			pmapfint.setAverage(aver);
 			pmapfint.setInterpolate(!fast);
 			pmapfint.setDoRadial(doRadial);
 			pmapfint.setDoAzimuthal(doAzimuthal);
 			List<AbstractDataset> dsetsf = pmapfint.value(data);
 			if (dsetsf == null)
 				return null;
 
 			if (aver) {
 				profiles[0] = Maths.dividez(dsetsf.get(1), dsetsf.get(3));
 				profiles[1] = Maths.dividez(dsetsf.get(0), dsetsf.get(2));
 			} else {
 				profiles[0] = dsetsf.get(1);
 				profiles[1] = dsetsf.get(0);
 			}
 			return profiles;
 		}
 
 		MapToPolarAndIntegrate pmapint = new MapToPolarAndIntegrate(spt[0], spt[1], rad[0], ang[0], rad[1], ang[1],
 				dpp, false);
 		if (mask != null && data != null)
 			if (mask.isCompatibleWith(data))
 				pmapint.setMask(mask);
 		pmapint.setClip(clip);
 		pmapint.setAverage(aver);
 		pmapint.setInterpolate(!fast);
 		pmapint.setDoRadial(doRadial);
 		pmapint.setDoAzimuthal(doAzimuthal);
 		List<AbstractDataset> dsets = pmapint.value(data);
 		if (dsets == null)
 			return null;
 
 		if (aver) {
 			profiles[0] = Maths.dividez(dsets.get(1), dsets.get(3));
 			profiles[1] = Maths.dividez(dsets.get(0), dsets.get(2));
 		} else {
 			profiles[0] = dsets.get(1);
 			profiles[1] = dsets.get(0);
 		}
 		
 		if (symmetry != SectorROI.NONE) {
 			double[] nang = sroi.getSymmetryAngles();
 
 			AbstractDataset[] symProfiles = new AbstractDataset[] { null, null };
 			MapToPolarAndIntegrate pmapsint = new MapToPolarAndIntegrate(spt[0], spt[1], rad[0], nang[0], rad[1],
 					nang[1], dpp, false);
 			if (mask != null && data != null)
 				if (mask.isCompatibleWith(data))
 					pmapsint.setMask(mask);
 			pmapsint.setClip(clip);
 			pmapsint.setAverage(aver);
 			pmapsint.setInterpolate(!fast);
 			pmapsint.setDoRadial(doRadial);
 			pmapsint.setDoAzimuthal(doAzimuthal);
 			List<AbstractDataset> dsetss = pmapsint.value(data);
 			if (dsetss != null) {
 
 				if (aver) {
 					symProfiles[0] = Maths.dividez(dsetss.get(1), dsetss.get(3));
 					symProfiles[1] = Maths.dividez(dsetss.get(0), dsetss.get(2));
 				} else {
 					symProfiles[0] = dsetss.get(1);
 					symProfiles[1] = dsetss.get(0);
 				}
 				if (sroi.isCombineSymmetry()) {
 					profiles[0] = Maths.add(profiles[0], symProfiles[0]);
 					profiles[1] = Maths.add(profiles[1], symProfiles[1]);
 				} else {
 					profiles[2] = symProfiles[0];
 					profiles[3] = symProfiles[1];
 				}
 			}
 		}
 		return profiles;
 	}
 	
 	/**
 	 * @param shape
 	 *            image dimensions
 	 * @param sroi
 	 * @return sector profile
 	 */
 	public static AbstractDataset[] area(int[] shape, SectorROI sroi) {
 		return area(shape, null, sroi);
 	}
 	
 	/**
 	 * @param shape
 	 *            image dimensions
 	 * @param mask
 	 *            used for clipping compensation (can be null)
 	 * @param sroi
 	 * @return sector profile
 	 */
 	public static AbstractDataset[] area(int[] shape, AbstractDataset mask, SectorROI sroi) {
 		return area(shape, mask, sroi, true, true, false);
 	}
 	
 	/**
 	 * @param shape
 	 *            image dimensions
 	 * @param mask
 	 *            used for clipping compensation (can be null)
 	 * @param sroi
 	 * @return sector profile
 	 */
 	public static AbstractDataset[] area(int[] shape, AbstractDataset mask, SectorROI sroi, boolean doRadial, boolean doAzimuthal, boolean fast) {
 		final double[] spt = sroi.getPointRef();
 		final double[] rad = sroi.getRadii();
 		final double[] ang = sroi.getAngles();
 		int symmetry = sroi.getSymmetry();
 		boolean clip = sroi.isClippingCompensation();
 		double dpp = sroi.getDpp();
 
 		AbstractDataset[] profiles = new AbstractDataset[] { null, null, null, null };
 
 		if (Math.abs(rad[0] - rad[1]) < 1 || Math.abs(ang[0] - ang[1]) < 1e-2) {
 			return null;
 		}
 
 		if (symmetry == SectorROI.FULL) {
 			MapToPolarAndIntegrate pmapfint = new MapToPolarAndIntegrate(spt[0], spt[1], rad[0], ang[0], rad[1], ang[0]
 					+ 2 * Math.PI, dpp, false);
 			if (mask != null)
 				pmapfint.setMask(mask);
 			pmapfint.setClip(clip);
 			pmapfint.setInterpolate(!fast);
 			pmapfint.setDoRadial(doRadial);
 			pmapfint.setDoAzimuthal(doAzimuthal);
 			List<AbstractDataset> dsetsfa = pmapfint.area(shape);
 			if (dsetsfa == null)
 				return null;
 
 			profiles[0] = dsetsfa.get(1);
 			profiles[1] = dsetsfa.get(0);
 			return profiles;
 		}
 
 		MapToPolarAndIntegrate pmapint = new MapToPolarAndIntegrate(spt[0], spt[1], rad[0], ang[0], rad[1], ang[1],
 				dpp, false);
 		if (mask != null)
 			pmapint.setMask(mask);
 		pmapint.setClip(clip);
 		pmapint.setInterpolate(!fast);
 		pmapint.setDoRadial(doRadial);
 		pmapint.setDoAzimuthal(doAzimuthal);
 		List<AbstractDataset> dsetsa = pmapint.area(shape);
 		if (dsetsa == null)
 			return null;
 
 		profiles[0] = dsetsa.get(1);
 		profiles[1] = dsetsa.get(0);
 
 		if (symmetry != SectorROI.NONE) {
 			double[] nang = sroi.getSymmetryAngles();
 
 			AbstractDataset[] symProfiles = new AbstractDataset[] { null, null };
 			MapToPolarAndIntegrate pmapsint = new MapToPolarAndIntegrate(spt[0], spt[1], rad[0], nang[0], rad[1],
 					nang[1], dpp, false);
 			if (mask != null)
 				pmapsint.setMask(mask);
 			pmapsint.setClip(clip);
 			pmapsint.setInterpolate(!fast);
 			pmapsint.setDoRadial(doRadial);
 			pmapsint.setDoAzimuthal(doAzimuthal);
 
 			List<AbstractDataset> dsetssa = pmapsint.area(shape);
 			if (dsetssa == null)
 				return null;
 
 			symProfiles[0] = dsetssa.get(1);
 			symProfiles[1] = dsetssa.get(0);
 			if (sroi.isCombineSymmetry()) {
 				profiles[0] = Maths.add(profiles[0], symProfiles[0]);
 				profiles[1] = Maths.add(profiles[1], symProfiles[1]);
 			} else {
 				profiles[2] = symProfiles[0];
 				profiles[3] = symProfiles[1];
 			}
 		}
 		return profiles;
 	}
 }
