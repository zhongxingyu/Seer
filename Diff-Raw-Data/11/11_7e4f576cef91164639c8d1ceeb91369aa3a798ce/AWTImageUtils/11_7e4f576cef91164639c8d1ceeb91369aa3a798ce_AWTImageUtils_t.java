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
 
 package uk.ac.diamond.scisoft.analysis.dataset;
 
 import java.awt.image.BufferedImage;
 import java.awt.image.ColorModel;
 import java.awt.image.DataBuffer;
 import java.awt.image.DataBufferByte;
 import java.awt.image.DataBufferInt;
 import java.awt.image.DataBufferUShort;
 import java.awt.image.PixelInterleavedSampleModel;
 import java.awt.image.Raster;
 import java.awt.image.SampleModel;
 import java.awt.image.SinglePixelPackedSampleModel;
 import java.awt.image.WritableRaster;
 
 import javax.media.jai.DataBufferFloat;
 import javax.media.jai.PlanarImage;
 import javax.media.jai.RasterFactory;
 import javax.media.jai.TiledImage;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ByteDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.FloatDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.RGBDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.ShortDataset;
 
 /**
  * Helper methods to convert to/from AWT images and datasets
  */
 public class AWTImageUtils {
 	private AWTImageUtils() {
 	}
 
 	/**
 	 * Create datasets from a Raster
 	 * @param r raster
 	 * @param data array to output datasets
 	 * @param dbtype data buffer type
 	 * @param keepBitWidth if true, then use signed primitives of same bit width for possibly unsigned data
 	 */
 	static public void createDatasets(Raster r, AbstractDataset[] data, final int dbtype, boolean keepBitWidth) {
 		final int bands = data.length;
 		final int height = r.getHeight();
 		final int width = r.getWidth();
 		AbstractDataset tmp;
 
 		for (int i = 0; i < bands; i++) {
 			switch (dbtype) {
 			case DataBuffer.TYPE_BYTE:
 				tmp = new IntegerDataset(r.getSamples(0, 0, width, height, i, (int[]) null), height, width);
 				data[i] = keepBitWidth ? new ByteDataset(tmp) : new ShortDataset(tmp);
 				break;
 			case DataBuffer.TYPE_SHORT:
 				tmp = new IntegerDataset(r.getSamples(0, 0, width, height, i, (int[]) null), height, width);
 				data[i] = new ShortDataset(tmp);
 				break;
 			case DataBuffer.TYPE_USHORT:
 				tmp = new IntegerDataset(r.getSamples(0, 0, width, height, i, (int[]) null), height, width);
 				data[i] = keepBitWidth ? new ShortDataset(tmp) : tmp;
 				break;
 			case DataBuffer.TYPE_INT:
 				data[i] = new IntegerDataset(r.getSamples(0, 0, width, height, i, (int[]) null), height, width);
 				break;
 			case DataBuffer.TYPE_DOUBLE:
 				data[i] = new DoubleDataset(r.getSamples(0, 0, width, height, i, (double[]) null), height, width);
 				break;
 			case DataBuffer.TYPE_FLOAT:
 				data[i] = new FloatDataset(r.getSamples(0, 0, width, height, i, (float[]) null), height, width);
 				break;
 			}
 		}
 	}
 
 	/**
 	 * Get datasets from an image
 	 * @param image
 	 * @param keepBitWidth if true, then use signed primitives of same bit width for possibly unsigned data
 	 * @return array of datasets
 	 */
 	static public AbstractDataset[] makeDatasets(final BufferedImage image, boolean keepBitWidth) {
 		// make raster from buffered image
 		final Raster ras = image.getData();
 		final int bands = ras.getNumBands();
 		final SampleModel sm = ras.getSampleModel();
 
 		int dbtype = sm.getDataType();
 		final int bits = sm.getSampleSize(0);
 		if (dbtype == DataBuffer.TYPE_INT) {
 			if (bits <= 8) {
 				dbtype = DataBuffer.TYPE_BYTE;
 			} else if (bits <= 16) {
 				dbtype = DataBuffer.TYPE_SHORT;
 			}
 		}
 		if (dbtype == DataBuffer.TYPE_USHORT) {
 			if (bits < 8) {
 				dbtype = DataBuffer.TYPE_BYTE;
 			} else if (bits < 16) {
 				dbtype = DataBuffer.TYPE_SHORT;
 			}
 		}
 		if (dbtype == DataBuffer.TYPE_SHORT) {
 			if (bits <= 8) {
 				dbtype = DataBuffer.TYPE_BYTE;
 			}
 		}
 		AbstractDataset[] data = new AbstractDataset[bands];
 
 		createDatasets(ras, data, dbtype, keepBitWidth);
 
 		return data;
 	}
 
 	/**
 	 * Get image from a dataset
 	 * @param data
 	 * @param bits number of bits (<=16 for non-RGB datasets)
 	 * @return buffered image
 	 */
 	static public BufferedImage makeBufferedImage(final AbstractDataset data, final int bits) {
 		final int[] shape = data.getShape();
 		final int height = shape[0];
 		final int width = shape.length == 1 ? 1 : shape[1]; // allow 1D datasets to be saved
 
 		final int size = data.getSize();
 		BufferedImage image = null;
 		DataBuffer buffer = null;
 		SampleModel sampleModel = null;
 
 		if (data instanceof RGBDataset) {
 			RGBDataset rgbds = (RGBDataset) data;
 
 			buffer = new DataBufferInt(size);
 			sampleModel = new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, width, height, new int[] { 0xff0000, 0x00ff00, 0x0000ff} );
 
 			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
 
 			short maxv = rgbds.max().shortValue();
 			final IndexIterator iter = rgbds.getIterator(true);
 			final int[] pos = iter.getPos();
 			final short[] rgbdata = rgbds.getData();
 			if (maxv < 256) { // 888
 				while (iter.hasNext()) {
 					final int n = iter.index;
					final int rgb = ((rgbdata[n] & 0xff) << 16) | ((rgbdata[n + 1] & 0xff) << 8) | (rgbdata[n + 2] & 0xff);
 					sampleModel.setSample(pos[1], pos[0], 0, rgb, buffer);
 				}			
 			} else {
 				int shift = 0;
 				while (maxv >= 256) {
 					shift++;
 					maxv >>= 2;
 				}
 
 				while (iter.hasNext()) {
 					final int n = iter.index;
					final int rgb = (((rgbdata[n] >> shift) & 0xff) << 16) | (((rgbdata[n + 1] >> shift) & 0xff) << 8) | ((rgbdata[n + 2] >> shift) & 0xff);
 					sampleModel.setSample(pos[1], pos[0], 0, rgb, buffer);
 				}			
 			}
 		} else {
 			// reconcile data with output format
 
 			// populate data buffer using sample model
 			IntegerDataset tmp = (IntegerDataset) DatasetUtils.cast(data, AbstractDataset.INT32);
 
 			if (bits <= 8) {
 				buffer = new DataBufferByte(height * width);
 				sampleModel = new PixelInterleavedSampleModel(DataBuffer.TYPE_BYTE, width, height, 1, width,
 						new int[] { 0 });
 				image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
 
 				sampleModel.setPixels(0, 0, width, height, tmp.getData(), buffer);
 			} else if (bits <= 16) {
 				buffer = new DataBufferUShort(height * width);
 				sampleModel = new PixelInterleavedSampleModel(DataBuffer.TYPE_USHORT, width, height, 1,
 						width, new int[] { 0 });
 				image = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_GRAY);
 
 				sampleModel.setPixels(0, 0, width, height, tmp.getData(), buffer);
 			} else {
 				throw new IllegalArgumentException("Number of bits must be less than or equal to 16");
 			}
 		}
 
 		WritableRaster wRas = Raster.createWritableRaster(sampleModel, buffer, null);
 		image.setData(wRas);
 		return image;
 	}
 
 	/**
 	 * Get image from a dataset
 	 * @param data
 	 * @param bits number of bits (> 32 for float image)
 	 * @return tiled image
 	 */
 	static public TiledImage makeTiledImage(final AbstractDataset data, final int bits) {
 		final int[] shape = data.getShape();
 		int height = shape[0];
 		int width = shape.length == 1 ? 1 : shape[1]; // allow 1D datasets to be saved
 		final int size = data.getSize();
 
 		SampleModel sampleModel;
 		DataBuffer buffer;
 
 		if (data instanceof RGBDataset) {
 			RGBDataset rgbds = (RGBDataset) data;
 
 			buffer = new DataBufferInt(size);
 			sampleModel = new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, width, height, new int[] { 0xff0000, 0x00ff00, 0x0000ff} );
 
 			short maxv = rgbds.max().shortValue();
 			final IndexIterator iter = rgbds.getIterator(true);
 			final int[] pos = iter.getPos();
 			final short[] rgbdata = rgbds.getData();
 
 			if (maxv < 256) { // 888
 				while (iter.hasNext()) {
 					final int n = iter.index;
					final int rgb = ((rgbdata[n] & 0xff) << 16) | ((rgbdata[n + 1] & 0xff) << 8) | (rgbdata[n + 2] & 0xff);
 					sampleModel.setSample(pos[1], pos[0], 0, rgb, buffer);
 				}			
 			} else {
 				int shift = 0;
 				while (maxv >= 256) {
 					shift++;
 					maxv >>= 2;
 				}
 
 				while (iter.hasNext()) {
 					final int n = iter.index;
					final int rgb = (((rgbdata[n] >> shift) & 0xff) << 16) | (((rgbdata[n + 1] >> shift) & 0xff) << 8) | ((rgbdata[n + 2] >> shift) & 0xff);
 					sampleModel.setSample(pos[1], pos[0], 0, rgb, buffer);
 				}			
 			}
 
 		} else {
 			// reconcile data with output format
 
 			// populate data buffer using sample model
 			if (bits <= 32) {
 				buffer = new DataBufferInt(size);
 				sampleModel = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_INT,
 								width, height, 1);
 
 				IntegerDataset tmp = (IntegerDataset) data.cast(AbstractDataset.INT32);
 				sampleModel.setPixels(0, 0, width, height, tmp.getData(), buffer);
 			} else { // Only TIFF supports floats (so far)
 				buffer = new DataBufferFloat(size);
 				sampleModel = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_FLOAT,
 								width, height, 1);
 
 				FloatDataset ftmp = (FloatDataset) data.cast(AbstractDataset.FLOAT32);
 				sampleModel.setPixels(0, 0, width, height, ftmp.getData(), buffer);
 			}
 
 		}
 
 		WritableRaster wRas = Raster.createWritableRaster(sampleModel, buffer, null);
 		ColorModel cm = PlanarImage.createColorModel(sampleModel);
 		TiledImage timage = new TiledImage(0, 0, width, height, 0, 0, sampleModel, cm);
 		timage.setData(wRas);
 		return timage;
 	}
 	
 }
