 /*
  * #%L
  * SCIFIO support for the OME data model, including OME-XML and OME-TIFF.
  * %%
  * Copyright (C) 2013 - 2014 Board of Regents of the University of
  * Wisconsin-Madison
  * %%
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 1. Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  * #L%
  */
 
 package io.scif.ome.formats;
 
 import io.scif.AbstractChecker;
 import io.scif.AbstractFormat;
 import io.scif.AbstractParser;
 import io.scif.AbstractTranslator;
 import io.scif.ByteArrayPlane;
 import io.scif.ByteArrayReader;
 import io.scif.DefaultImageMetadata;
 import io.scif.Format;
 import io.scif.FormatException;
 import io.scif.ImageMetadata;
 import io.scif.Plane;
 import io.scif.Translator;
 import io.scif.config.SCIFIOConfig;
 import io.scif.formats.MinimalTIFFFormat;
 import io.scif.formats.TIFFFormat;
 import io.scif.formats.tiff.IFD;
 import io.scif.formats.tiff.IFDList;
 import io.scif.formats.tiff.PhotoInterp;
 import io.scif.formats.tiff.TiffIFDEntry;
 import io.scif.formats.tiff.TiffParser;
 import io.scif.formats.tiff.TiffSaver;
 import io.scif.io.Location;
 import io.scif.io.RandomAccessInputStream;
 import io.scif.io.RandomAccessOutputStream;
 import io.scif.ome.OMEMetadata;
 import io.scif.ome.services.OMEMetadataService;
 import io.scif.ome.services.OMEXMLService;
 import io.scif.services.FormatService;
 import io.scif.services.TranslatorService;
 import io.scif.util.FormatTools;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 import java.util.Vector;
 
 import loci.common.services.ServiceException;
 import loci.formats.meta.IMetadata;
 import loci.formats.meta.MetadataRetrieve;
 import loci.formats.meta.MetadataStore;
 import loci.formats.ome.OMEXMLMetadata;
 import net.imglib2.display.ColorTable;
 import net.imglib2.meta.Axes;
 import ome.xml.model.primitives.NonNegativeInteger;
 import ome.xml.model.primitives.PositiveFloat;
 import ome.xml.model.primitives.PositiveInteger;
 import ome.xml.model.primitives.Timestamp;
 
 import org.scijava.Context;
 import org.scijava.plugin.Parameter;
 import org.scijava.plugin.Plugin;
 
 /**
  * OMETiffReader is the file format reader for <a
  * href="http://ome-xml.org/wiki/OmeTiff">OME-TIFF</a> files.
  * 
  * @author Mark Hiner hinerm at gmail.com
  */
 @Plugin(type = Format.class, priority = OMETIFFFormat.PRIORITY)
 public class OMETIFFFormat extends AbstractFormat {
 
 	// -- Constants --
 
 	public static final String URL_OME_TIFF =
 		"http://www.openmicroscopy.org/site/support/ome-model/ome-tiff/";
 	public static final double PRIORITY = TIFFFormat.PRIORITY + 1;
 
 	// -- Fields --
 
 	@Parameter
 	private static OMEXMLService service;
 
 	@Parameter
 	private static OMEMetadataService metaService;
 
 	// -- Format API Methods --
 
 	@Override
 	public String getFormatName() {
 		return "OME-TIFF";
 	}
 
 	@Override
 	protected String[] makeSuffixArray() {
 		return new String[] { "ome.tif", "ome.tiff" };
 	}
 
 	// -- Nested Classes --
 
 	/**
 	 * @author Mark Hiner hinerm at gmail.com
 	 */
 	public static class Metadata extends TIFFFormat.Metadata {
 
 		// -- Fields --
 
 		/** Mapping from series and plane numbers to files and IFD entries. */
 		protected OMETIFFPlane[][] info; // dimensioned [numSeries][numPlanes]
 
 		private IFD firstIFD;
 
 		private List<Integer> samples;
 		private List<Boolean> adjustedSamples;
 
 		/** List of used files. */
 		protected String[] used;
 
 		// TODO maybe this should be an o mexmlmetadata...
 		private OMEMetadata omeMeta;
 
 		private long lastPlane = 0;
 		private boolean hasSPW;
 
 		private int[] tileWidth;
 		private int[] tileHeight;
 
 		// -- OMETIFF Metadata API methods --
 
 		/**
 		 * Returns a MetadataStore that is populated in such a way as to produce
 		 * valid OME-XML. The returned MetadataStore cannot be used by an
 		 * IFormatWriter, as it will not contain the required BinData.BigEndian
 		 * attributes.
 		 */
 		public MetadataStore getMetadataStoreForDisplay() {
 			final MetadataStore store = omeMeta.getRoot();
 			if (service.isOMEXMLMetadata(store)) {
 				service.removeBinData((OMEXMLMetadata) store);
 				for (int i = 0; i < getImageCount(); i++) {
 					if (((OMEXMLMetadata) store).getTiffDataCount(i) == 0) {
 						service.addMetadataOnly((OMEXMLMetadata) store, i);
 					}
 				}
 			}
 			return store;
 		}
 
 		/**
 		 * Returns a MetadataStore that is populated in such a way as to be usable
 		 * by an IFormatWriter. Any OME-XML generated from this MetadataStore is
 		 * <em>very unlikely</em> to be valid, as more than likely both BinData and
 		 * TiffData element will be present.
 		 */
 		public MetadataStore getMetadataStoreForConversion() {
 			final MetadataStore store = omeMeta.getRoot();
 			for (int i = 0; i < getImageCount(); i++) {
 				store.setPixelsBinDataBigEndian(new Boolean(!get(i).isLittleEndian()),
 					i, 0);
 			}
 			return store;
 		}
 
 		// -- OMETIFFMetadata getters and setters --
 
 		public OMETIFFPlane[][] getPlaneInfo() {
 			return info;
 		}
 
 		public void setPlaneInfo(final OMETIFFPlane[][] info) {
 			this.info = info;
 		}
 
 		public String[] getUsed() {
 			return used;
 		}
 
 		public void setUsed(final String[] used) {
 			this.used = used;
 		}
 
 		public OMEMetadata getOmeMeta() {
 			return omeMeta;
 		}
 
 		public void setOmeMeta(final OMEMetadata omeMeta) {
 			this.omeMeta = omeMeta;
 		}
 
 		@Override
 		public long getLastPlane() {
 			return lastPlane;
 		}
 
 		@Override
 		public void setLastPlane(final long lastPlane) {
 			this.lastPlane = lastPlane;
 		}
 
 		public IFD getFirstIFD() {
 			return firstIFD;
 		}
 
 		public void setFirstIFD(final IFD firstIFD) {
 			this.firstIFD = firstIFD;
 		}
 
 		public boolean isHasSPW() {
 			return hasSPW;
 		}
 
 		public void setHasSPW(final boolean hasSPW) {
 			this.hasSPW = hasSPW;
 		}
 
 		public int[] getTileWidth() {
 			return tileWidth;
 		}
 
 		public void setTileWidth(final int[] tileWidth) {
 			this.tileWidth = tileWidth;
 		}
 
 		public int[] getTileHeight() {
 			return tileHeight;
 		}
 
 		public void setTileHeight(final int[] tileHeight) {
 			this.tileHeight = tileHeight;
 		}
 
 		// -- Metadata API Methods --
 
 		/*
 		 * @see io.scif.Metadata#populateImageMetadata()
 		 */
 		@Override
 		public void populateImageMetadata() {
 
 			// populate core metadata
 
 			final OMEXMLMetadata omexmlMeta = getOmeMeta().getRoot();
 
 			for (int s = 0; s < getImageCount(); s++) {
 
 				final ImageMetadata m = get(s);
 				try {
 					m.setPlanarAxisCount(2);
 					m.setAxisLength(Axes.X, omexmlMeta.getPixelsSizeX(s).getValue()
 						.intValue());
 					final int tiffWidth = (int) firstIFD.getImageWidth();
 					if (m.getAxisLength(Axes.X) != tiffWidth && s == 0) {
 						log().warn(
 							"SizeX mismatch: OME=" + m.getAxisLength(Axes.X) + ", TIFF=" +
 								tiffWidth);
 					}
 					m.setAxisLength(Axes.Y, omexmlMeta.getPixelsSizeY(s).getValue()
 						.intValue());
 					final int tiffHeight = (int) firstIFD.getImageLength();
 					if (m.getAxisLength(Axes.Y) != tiffHeight && s == 0) {
 						log().warn(
 							"SizeY mismatch: OME=" + m.getAxisLength(Axes.Y) + ", TIFF=" +
 								tiffHeight);
 					}
 					m.setAxisLength(Axes.Z, omexmlMeta.getPixelsSizeZ(s).getValue()
 						.intValue());
 					m.setAxisLength(Axes.CHANNEL, omexmlMeta.getPixelsSizeC(s).getValue()
 						.intValue());
 					m.setAxisLength(Axes.TIME, omexmlMeta.getPixelsSizeT(s).getValue()
 						.intValue());
 					m.setPixelType(FormatTools.pixelTypeFromString(omexmlMeta
 						.getPixelsType(s).toString()));
 					final int tiffPixelType = firstIFD.getPixelType();
 					if (m.getPixelType() != tiffPixelType &&
 						(s == 0 || adjustedSamples.get(s)))
 					{
 						log().warn(
 							"PixelType mismatch: OME=" + m.getPixelType() + ", TIFF=" +
 								tiffPixelType);
 						m.setPixelType(tiffPixelType);
 					}
 					m.setBitsPerPixel(FormatTools.getBitsPerPixel(m.getPixelType()));
 					String dimensionOrder =
 						omexmlMeta.getPixelsDimensionOrder(s).toString();
 
 					// hackish workaround for files exported by OMERO that have an
 					// incorrect dimension order
 					String uuidFileName = "";
 					try {
 						if (omexmlMeta.getTiffDataCount(s) > 0) {
 							uuidFileName = omexmlMeta.getUUIDFileName(s, 0);
 						}
 					}
 					catch (final NullPointerException e) {}
 					if (omexmlMeta.getChannelCount(s) > 0 &&
 						omexmlMeta.getChannelName(s, 0) == null &&
 						omexmlMeta.getTiffDataCount(s) > 0 &&
 						uuidFileName.indexOf("__omero_export") != -1)
 					{
 						dimensionOrder = "XYZCT";
 					}
 					m.setAxisTypes(metaService.findDimensionList(dimensionOrder));
 
 					m.setOrderCertain(true);
 					final PhotoInterp photo = firstIFD.getPhotometricInterpretation();
 					if (samples.get(s) > 1 || photo == PhotoInterp.RGB) {
 						m.setPlanarAxisCount(3);
 					}
 					if ((samples.get(s) != m.getAxisLength(Axes.CHANNEL) &&
 						(samples.get(s) % m.getAxisLength(Axes.CHANNEL)) != 0 && (m
 						.getAxisLength(Axes.CHANNEL) % samples.get(s)) != 0) ||
 						m.getAxisLength(Axes.CHANNEL) == 1 || adjustedSamples.get(s))
 					{
 						m.setAxisLength(Axes.CHANNEL, m.getAxisLength(Axes.CHANNEL) *
 							samples.get(s));
 					}
 
 					if (m.getAxisLength(Axes.Z) * m.getAxisLength(Axes.TIME) *
 						m.getAxisLength(Axes.CHANNEL) > info[s].length &&
 						!m.isMultichannel())
 					{
 						if (m.getAxisLength(Axes.Z) == info[s].length) {
 							m.setAxisLength(Axes.TIME, 1);
 							m.setAxisLength(Axes.CHANNEL, 1);
 						}
 						else if (m.getAxisLength(Axes.TIME) == info[s].length) {
 							m.setAxisLength(Axes.Z, 1);
 							m.setAxisLength(Axes.CHANNEL, 1);
 						}
 						else if (m.getAxisLength(Axes.CHANNEL) == info[s].length) {
 							m.setAxisLength(Axes.TIME, 1);
 							m.setAxisLength(Axes.Z, 1);
 						}
 					}
 
 					if (omexmlMeta.getPixelsBinDataCount(s) > 1) {
 						log().warn(
 							"OME-TIFF Pixels element contains BinData elements! "
 								+ "Ignoring.");
 					}
 					m.setLittleEndian(firstIFD.isLittleEndian());
 					m.setIndexed(photo == PhotoInterp.RGB_PALETTE &&
 						firstIFD.getIFDValue(IFD.COLOR_MAP) != null);
 					m.setFalseColor(true);
 					m.setMetadataComplete(true);
 				}
 				catch (final NullPointerException exc) {
 					log().error("Incomplete Pixels metadata", exc);
 				}
 				catch (final FormatException exc) {
 					log().error("Format exception when creating ImageMetadata", exc);
 				}
 				// Set the physical pixel sizes
 				FormatTools.calibrate(m.getAxis(Axes.X), getValue(omexmlMeta
 					.getPixelsPhysicalSizeX(s)), 0.0);
 				FormatTools.calibrate(m.getAxis(Axes.Y), getValue(omexmlMeta
 					.getPixelsPhysicalSizeY(s)), 0.0);
 				FormatTools.calibrate(m.getAxis(Axes.Z), getValue(omexmlMeta
 					.getPixelsPhysicalSizeZ(s)), 0.0);
 			}
 
 //      if (getImageCount() == 1) {
 //        CoreMetadata ms0 = core.get(0);
 //        ms0.sizeZ = 1;
 //        if (!ms0.rgb) {
 //          ms0.sizeC = 1;
 //        }
 //        ms0.sizeT = 1;
 //      }
 //      metaService.populatePixels(getOmeMeta().getRoot(), this, false, false);
 			getOmeMeta().setRoot((OMEXMLMetadata) getMetadataStoreForConversion());
 		}
 
 		@Override
 		public void close(final boolean fileOnly) throws IOException {
 			super.close(fileOnly);
 			if (info != null) {
 				for (final OMETIFFPlane[] dimension : info) {
 					for (final OMETIFFPlane plane : dimension) {
 						if (plane.reader != null) {
 							try {
 								plane.reader.close();
 							}
 							catch (final Exception e) {
 								log().error("Plane closure failure!", e);
 							}
 						}
 					}
 				}
 			}
 			if (!fileOnly) {
 				info = null;
 				lastPlane = 0;
 				tileWidth = null;
 				tileHeight = null;
 			}
 		}
 
 		// -- HasColorTable API Methods --
 
 		@Override
 		public ColorTable
 			getColorTable(final int imageIndex, final long planeIndex)
 		{
 			if (info[imageIndex][(int) lastPlane] == null ||
 				info[imageIndex][(int) lastPlane].reader == null ||
 				info[imageIndex][(int) lastPlane].id == null)
 			{
 				return null;
 			}
 			try {
 				info[imageIndex][(int) lastPlane].reader
 					.setSource(info[imageIndex][(int) lastPlane].id);
 				return info[imageIndex][(int) lastPlane].reader.getMetadata()
 					.getColorTable(imageIndex, planeIndex);
 			}
 			catch (final IOException e) {
 				log().error("IOException when trying to read color table", e);
 				return null;
 			}
 		}
 
 		/**
 		 * Helper method to extract values out of {@link PositiveFloat}s, for
 		 * physical pixel sizes (calibration values). Returns 1.0 if given a null or
 		 * invalid (< 0) calibration.
 		 */
 		private double getValue(PositiveFloat pixelPhysicalSize) {
 			if (pixelPhysicalSize == null) return 1.0;
 			Double physSize = pixelPhysicalSize.getValue();
 			if (physSize < 0) return 1.0;
 			return physSize;
 		}
 	}
 
 	/**
 	 * @author Mark Hiner hinerm at gmail.com
 	 */
 	public static class Checker extends AbstractChecker {
 
 		// -- Checker API Methods --
 
 		@Override
 		public boolean suffixNecessary() {
 			return false;
 		}
 
 		@Override
 		public boolean suffixSufficient() {
 			return false;
 		}
 
 		@Override
 		public boolean isFormat(final RandomAccessInputStream stream)
 			throws IOException
 		{
 			final TiffParser tp = new TiffParser(getContext(), stream);
 			tp.setDoCaching(false);
 			final boolean validHeader = tp.isValidHeader();
 			if (!validHeader) return false;
 			// look for OME-XML in first IFD's comment
 			final IFD ifd = tp.getFirstIFD();
 			if (ifd == null) return false;
 			final Object description = ifd.get(IFD.IMAGE_DESCRIPTION);
 			if (description == null) {
 				return false;
 			}
 			String comment = null;
 			if (description instanceof TiffIFDEntry) {
 				comment = tp.getIFDValue((TiffIFDEntry) description).toString();
 			}
 			else if (description instanceof String) {
 				comment = (String) description;
 			}
 			if (comment == null || comment.trim().length() == 0) return false;
 
 			comment = comment.trim();
 
 			// do a basic sanity check before attempting to parse the comment as XML
 			// the parsing step is a bit slow, so there is no sense in trying unless
 			// we are reasonably sure that the comment contains XML
 			if (!comment.startsWith("<") || !comment.endsWith(">")) {
 				return false;
 			}
 
 			try {
 				final IMetadata meta = service.createOMEXMLMetadata(comment);
 				for (int i = 0; i < meta.getImageCount(); i++) {
 					meta.setPixelsBinDataBigEndian(Boolean.TRUE, i, 0);
 					metaService.verifyMinimumPopulated(meta, i);
 				}
 				return meta.getImageCount() > 0;
 			}
 			catch (final ServiceException se) {
 				log().debug("OME-XML parsing failed", se);
 			}
 			catch (final NullPointerException e) {
 				log().debug("OME-XML parsing failed", e);
 			}
 			catch (final FormatException e) {
 				log().debug("OME-XML parsing failed", e);
 			}
 			catch (final IndexOutOfBoundsException e) {
 				log().debug("OME-XML parsing failed", e);
 			}
 			return false;
 		}
 	}
 
 	/**
 	 * @author Mark Hiner hinerm at gmail.com
 	 */
 	public static class Parser extends AbstractParser<Metadata> {
 
 		// -- Fields --
 
 		@Parameter
 		private FormatService formatService;
 
 		// -- Parser API Methods --
 
 		@Override
 		public String[] getImageUsedFiles(final int imageIndex,
 			final boolean noPixels)
 		{
 			FormatTools.assertId(getMetadata().getDatasetName(), true, 1);
 			if (noPixels) return null;
 			final Vector<String> usedFiles = new Vector<String>();
 			for (int i = 0; i < getMetadata().info[imageIndex].length; i++) {
 				if (!usedFiles.contains(getMetadata().info[imageIndex][i].id)) {
 					usedFiles.add(getMetadata().info[imageIndex][i].id);
 				}
 			}
 			return usedFiles.toArray(new String[usedFiles.size()]);
 		}
 
 		@Override
 		public Metadata parse(final String fileName, final Metadata meta,
 			final SCIFIOConfig config) throws IOException, FormatException
 		{
 			return super.parse(normalizeFilename(null, fileName), meta, config);
 		}
 
 		@Override
 		public Metadata parse(final File file, final Metadata meta,
 			final SCIFIOConfig config) throws IOException, FormatException
 		{
 			return super.parse(normalizeFilename(null, file.getPath()), meta, config);
 		}
 
 		@Override
 		public int fileGroupOption(final String id) throws FormatException,
 			IOException
 		{
 			final boolean single = isSingleFile(id);
 			return single ? FormatTools.CAN_GROUP : FormatTools.MUST_GROUP;
 		}
 
 		@Override
 		public Metadata parse(final RandomAccessInputStream stream,
 			final Metadata meta, final SCIFIOConfig config) throws IOException,
 			FormatException
 		{
 			super.parse(stream, meta, config);
 			for (int s = 0; s < meta.getImageCount(); s++) {
 				final OMETIFFPlane[][] info = meta.getPlaneInfo();
 
 				try {
 					if (!info[s][0].reader.getFormat().createChecker().isFormat(
 						info[s][0].id))
 					{
 						info[s][0].id = meta.getSource().getFileName();
 					}
 					for (int plane = 0; plane < info[s].length; plane++) {
 						if (!info[s][plane].reader.getFormat().createChecker().isFormat(
 							info[s][plane].id))
 						{
 							info[s][plane].id = info[s][0].id;
 						}
 					}
 
 					info[s][0].reader.setSource(info[s][0].id);
 					meta.getTileWidth()[s] =
 						(int) info[s][0].reader.getOptimalTileWidth(s);
 					meta.getTileHeight()[s] =
 						(int) info[s][0].reader.getOptimalTileHeight(s);
 				}
 				catch (final FormatException e) {
 					log().debug("OME-XML parsing failed", e);
 				}
 			}
 
 			return meta;
 		}
 
 		// -- Groupable API Methods --
 
 		@Override
 		public boolean isSingleFile(final String id) throws FormatException,
 			IOException
 		{
 			return OMETIFFFormat.isSingleFile(getContext(), id);
 		}
 
 		// -- Abstract Parser API Methods --
 
 		@Override
 		protected void typedParse(final io.scif.io.RandomAccessInputStream stream,
 			final Metadata meta, final SCIFIOConfig config) throws IOException,
 			io.scif.FormatException
 		{
 			// normalize file name
 			final String id = stream.getFileName();
 
 			final String dir = new File(id).getParent();
 
 			// parse and populate OME-XML metadata
 			String fileName =
 				new Location(getContext(), id).getAbsoluteFile().getAbsolutePath();
 			if (!new File(fileName).exists()) {
 				fileName = stream.getFileName();
 			}
 			final RandomAccessInputStream ras =
 				new RandomAccessInputStream(getContext(), fileName);
 			String xml;
 			IFD firstIFD;
 			try {
 				final TiffParser tp = new TiffParser(getContext(), ras);
 				firstIFD = tp.getFirstIFD();
 				xml = firstIFD.getComment();
 			}
 			finally {
 				ras.close();
 			}
 
 			meta.setFirstIFD(firstIFD);
 
 			OMEXMLMetadata omexmlMeta;
 			try {
 				omexmlMeta = service.createOMEXMLMetadata(xml);
 			}
 			catch (final ServiceException se) {
 				throw new FormatException(se);
 			}
 
 			meta.setHasSPW(omexmlMeta.getPlateCount() > 0);
 
 			for (int i = 0; i < meta.getImageCount(); i++) {
 				final int sizeC = omexmlMeta.getPixelsSizeC(i).getValue().intValue();
 				service.removeChannels(omexmlMeta, i, sizeC);
 			}
 
 			final Hashtable<String, Object> originalMetadata =
 				service.getOriginalMetadata(omexmlMeta);
 			if (originalMetadata != null) meta.getTable().putAll(originalMetadata);
 
 			log().trace(xml);
 
 			if (omexmlMeta.getRoot() == null) {
 				throw new FormatException("Could not parse OME-XML from TIFF comment");
 			}
 
 			meta.setOmeMeta(new OMEMetadata(getContext(), omexmlMeta));
 
 			final String[] acquiredDates = new String[meta.getImageCount()];
 			for (int i = 0; i < acquiredDates.length; i++) {
 				final Timestamp acquisitionDate = omexmlMeta.getImageAcquisitionDate(i);
 				if (acquisitionDate != null) {
 					acquiredDates[i] = acquisitionDate.getValue();
 				}
 			}
 
 			final String currentUUID = omexmlMeta.getUUID();
 
 			// determine series count from Image and Pixels elements
 			final int imageCount = omexmlMeta.getImageCount();
 			meta.createImageMetadata(imageCount);
 
 			OMETIFFPlane[][] info = new OMETIFFPlane[imageCount][];
 			meta.setPlaneInfo(info);
 
 			final int[] tileWidth = new int[imageCount];
 			final int[] tileHeight = new int[imageCount];
 
 			meta.setTileWidth(tileWidth);
 			meta.setTileHeight(tileHeight);
 
 			// compile list of file/UUID mappings
 			final Hashtable<String, String> files = new Hashtable<String, String>();
 			boolean needSearch = false;
 			for (int i = 0; i < imageCount; i++) {
 				final int tiffDataCount = omexmlMeta.getTiffDataCount(i);
 				for (int td = 0; td < tiffDataCount; td++) {
 					String uuid = null;
 					try {
 						uuid = omexmlMeta.getUUIDValue(i, td);
 					}
 					catch (final NullPointerException e) {}
 					String filename = null;
 					if (uuid == null) {
 						// no UUID means that TiffData element refers to this file
 						uuid = "";
 						filename = id;
 					}
 					else {
 						filename = omexmlMeta.getUUIDFileName(i, td);
 						if (!new Location(getContext(), dir, filename).exists()) filename =
 							null;
 						if (filename == null) {
 							if (uuid.equals(currentUUID) || currentUUID == null) {
 								// UUID references this file
 								filename = id;
 							}
 							else {
 								// will need to search for this UUID
 								filename = "";
 								needSearch = true;
 							}
 						}
 						else filename = normalizeFilename(dir, filename);
 					}
 					final String existing = files.get(uuid);
 					if (existing == null) files.put(uuid, filename);
 					else if (!existing.equals(filename)) {
 						throw new FormatException("Inconsistent UUID filenames");
 					}
 				}
 			}
 
 			// search for missing filenames
 			if (needSearch) {
 				final Enumeration<String> en = files.keys();
 				while (en.hasMoreElements()) {
 					final String uuid = en.nextElement();
 					final String filename = files.get(uuid);
 					if (filename.equals("")) {
 						// TODO search...
 						// should scan only other .ome.tif files
 						// to make this work with OME server may be a little tricky?
 						throw new FormatException("Unmatched UUID: " + uuid);
 					}
 				}
 			}
 
 			// build list of used files
 			final Enumeration<String> en = files.keys();
 			final int numUUIDs = files.size();
 			final HashSet<String> fileSet = new HashSet<String>(); // ensure no
 																															// duplicate
 																															// filenames
 			for (int i = 0; i < numUUIDs; i++) {
 				final String uuid = en.nextElement();
 				final String filename = files.get(uuid);
 				fileSet.add(filename);
 			}
 			final String[] used = new String[fileSet.size()];
 			meta.setUsed(used);
 
 			final Iterator<String> iter = fileSet.iterator();
 			for (int i = 0; i < used.length; i++)
 				used[i] = iter.next();
 
 			// process TiffData elements
 			final Hashtable<String, MinimalTIFFFormat.Reader<?>> readers =
 				new Hashtable<String, MinimalTIFFFormat.Reader<?>>();
 			final List<Boolean> adjustedSamples = new ArrayList<Boolean>();
 			final List<Integer> samples = new ArrayList<Integer>();
 			meta.adjustedSamples = adjustedSamples;
 			meta.samples = samples;
 
 			for (int i = 0; i < imageCount; i++) {
 				final int s = i;
 				log().debug("Image[" + i + "] {");
 				log().debug("  id = " + omexmlMeta.getImageID(i));
 
 				adjustedSamples.add(false);
 
 				final String order = omexmlMeta.getPixelsDimensionOrder(i).toString();
 
 				PositiveInteger samplesPerPixel = null;
 				if (omexmlMeta.getChannelCount(i) > 0) {
 					samplesPerPixel = omexmlMeta.getChannelSamplesPerPixel(i, 0);
 				}
 				samples.add(i, samplesPerPixel == null ? -1 : samplesPerPixel
 					.getValue());
 				final int tiffSamples = firstIFD.getSamplesPerPixel();
 
 				if (adjustedSamples.get(i) ||
 					(samples.get(i) != tiffSamples && (i == 0 || samples.get(i) < 0)))
 				{
 					log().warn(
 						"SamplesPerPixel mismatch: OME=" + samples.get(i) + ", TIFF=" +
 							tiffSamples);
 					samples.set(i, tiffSamples);
 					adjustedSamples.set(i, true);
 				}
 				else {
 					adjustedSamples.set(i, false);
 				}
 
 				if (adjustedSamples.get(i) && omexmlMeta.getChannelCount(i) <= 1) {
 					adjustedSamples.set(i, false);
 				}
 
 				int effSizeC = omexmlMeta.getPixelsSizeC(i).getValue().intValue();
 				if (!adjustedSamples.get(i)) {
 					effSizeC /= samples.get(i);
 				}
 				if (effSizeC == 0) effSizeC = 1;
 				if (effSizeC * samples.get(i) != omexmlMeta.getPixelsSizeC(i)
 					.getValue().intValue())
 				{
 					effSizeC = omexmlMeta.getPixelsSizeC(i).getValue().intValue();
 				}
 				final int sizeT = omexmlMeta.getPixelsSizeT(i).getValue().intValue();
 				final int sizeZ = omexmlMeta.getPixelsSizeZ(i).getValue().intValue();
 				int num = effSizeC * sizeT * sizeZ;
 
 				OMETIFFPlane[] planes = new OMETIFFPlane[num];
 				for (int no = 0; no < num; no++)
 					planes[no] = new OMETIFFPlane();
 
 				final int tiffDataCount = omexmlMeta.getTiffDataCount(i);
 				Boolean zOneIndexed = null;
 				Boolean cOneIndexed = null;
 				Boolean tOneIndexed = null;
 
 				// pre-scan TiffData indices to see if any of them are indexed from 1
 
 				for (int td = 0; td < tiffDataCount; td++) {
 					final NonNegativeInteger firstC = omexmlMeta.getTiffDataFirstC(i, td);
 					final NonNegativeInteger firstT = omexmlMeta.getTiffDataFirstT(i, td);
 					final NonNegativeInteger firstZ = omexmlMeta.getTiffDataFirstZ(i, td);
 					final int c = firstC == null ? 0 : firstC.getValue();
 					final int t = firstT == null ? 0 : firstT.getValue();
 					final int z = firstZ == null ? 0 : firstZ.getValue();
 
 					if (c >= effSizeC && cOneIndexed == null) {
 						cOneIndexed = true;
 					}
 					else if (c == 0) {
 						cOneIndexed = false;
 					}
 					if (z >= sizeZ && zOneIndexed == null) {
 						zOneIndexed = true;
 					}
 					else if (z == 0) {
 						zOneIndexed = false;
 					}
 					if (t >= sizeT && tOneIndexed == null) {
 						tOneIndexed = true;
 					}
 					else if (t == 0) {
 						tOneIndexed = false;
 					}
 				}
 
 				for (int td = 0; td < tiffDataCount; td++) {
 					log().debug("    TiffData[" + td + "] {");
 					// extract TiffData parameters
 					String filename = null;
 					String uuid = null;
 					try {
 						filename = omexmlMeta.getUUIDFileName(i, td);
 					}
 					catch (final NullPointerException e) {
 						log().debug("Ignoring null UUID object when retrieving filename.");
 					}
 					try {
 						uuid = omexmlMeta.getUUIDValue(i, td);
 					}
 					catch (final NullPointerException e) {
 						log().debug("Ignoring null UUID object when retrieving value.");
 					}
 					final NonNegativeInteger tdIFD = omexmlMeta.getTiffDataIFD(i, td);
 					final int ifd = tdIFD == null ? 0 : tdIFD.getValue();
 					final NonNegativeInteger numPlanes =
 						omexmlMeta.getTiffDataPlaneCount(i, td);
 					final NonNegativeInteger firstC = omexmlMeta.getTiffDataFirstC(i, td);
 					final NonNegativeInteger firstT = omexmlMeta.getTiffDataFirstT(i, td);
 					final NonNegativeInteger firstZ = omexmlMeta.getTiffDataFirstZ(i, td);
 					int c = firstC == null ? 0 : firstC.getValue();
 					int t = firstT == null ? 0 : firstT.getValue();
 					int z = firstZ == null ? 0 : firstZ.getValue();
 
 					// NB: some writers index FirstC, FirstZ and FirstT from 1
 					if (cOneIndexed != null && cOneIndexed) c--;
 					if (zOneIndexed != null && zOneIndexed) z--;
 					if (tOneIndexed != null && tOneIndexed) t--;
 
 					if (z >= sizeZ || c >= effSizeC || t >= sizeT) {
 						log().warn(
 							"Found invalid TiffData: Z=" + z + ", C=" + c + ", T=" + t);
 						break;
 					}
 
 					final long[] pos = metaService.zctToArray(order, z, c, t);
 					final long[] lengths =
 						metaService.zctToArray(order, sizeZ, effSizeC, sizeT);
 					final long index = FormatTools.positionToRaster(lengths, pos);
 					final int count = numPlanes == null ? 1 : numPlanes.getValue();
 					if (count == 0) {
 						meta.get(s);
 						break;
 					}
 
 					// get reader object for this filename
 					if (filename == null) {
 						if (uuid == null) filename = id;
 						else filename = files.get(uuid);
 					}
 					else filename = normalizeFilename(dir, filename);
 					MinimalTIFFFormat.Reader<?> r = readers.get(filename);
 					if (r == null) {
 						r =
 							(io.scif.formats.MinimalTIFFFormat.Reader<?>) formatService
 								.getFormatFromClass(MinimalTIFFFormat.class).createReader();
 						readers.put(filename, r);
 					}
 
 					final Location file = new Location(getContext(), filename);
 					if (!file.exists()) {
 						// if this is an absolute file name, try using a relative name
 						// old versions of OMETiffWriter wrote an absolute path to
 						// UUID.FileName, which causes problems if the file is moved to
 						// a different directory
 						filename =
 							filename.substring(filename.lastIndexOf(File.separator) + 1);
 						filename = dir + File.separator + filename;
 
 						if (!new Location(getContext(), filename).exists()) {
 							filename = stream.getFileName();
 						}
 					}
 
 					// populate plane index -> IFD mapping
 					for (int q = 0; q < count; q++) {
 						final int no = (int) (index + q);
 						planes[no].reader = r;
 						planes[no].id = filename;
 						planes[no].ifd = ifd + q;
 						planes[no].certain = true;
 						log().debug(
 							"      Plane[" + no + "]: file=" + planes[no].id + ", IFD=" +
 								planes[no].ifd);
 					}
 					if (numPlanes == null) {
 						// unknown number of planes; fill down
 						for (int no = (int) (index + 1); no < num; no++) {
 							if (planes[no].certain) break;
 							planes[no].reader = r;
 							planes[no].id = filename;
 							planes[no].ifd = planes[no - 1].ifd + 1;
 							log().debug("      Plane[" + no + "]: FILLED");
 						}
 					}
 					else {
 						// known number of planes; clear anything subsequently filled
 						for (int no = (int) (index + count); no < num; no++) {
 							if (planes[no].certain) break;
 							planes[no].reader = null;
 							planes[no].id = null;
 							planes[no].ifd = -1;
 							log().debug("      Plane[" + no + "]: CLEARED");
 						}
 					}
 					log().debug("    }");
 				}
 
 				if (meta.get(s) == null) continue;
 
 				// verify that all planes are available
 				log().debug("    --------------------------------");
 				for (int no = 0; no < num; no++) {
 					log().debug(
 						"    Plane[" + no + "]: file=" + planes[no].id + ", IFD=" +
 							planes[no].ifd);
 					if (planes[no].reader == null) {
 						log().warn(
 							"Image ID '" + omexmlMeta.getImageID(i) + "': missing plane #" +
 								no + ".  " +
 								"Using TiffReader to determine the number of planes.");
 						final TIFFFormat.Reader<?> r =
 							(io.scif.formats.TIFFFormat.Reader<?>) formatService
 								.getFormatFromClass(TIFFFormat.class).createReader();
 						r.setSource(stream.getFileName());
 						try {
 							planes = new OMETIFFPlane[r.getImageCount()];
 							for (int plane = 0; plane < planes.length; plane++) {
 								planes[plane] = new OMETIFFPlane();
 								planes[plane].id = stream.getFileName();
 								planes[plane].reader = r;
 								planes[plane].ifd = plane;
 							}
 							num = planes.length;
 						}
 						finally {
 							r.close();
 						}
 					}
 				}
 				info[i] = planes;
 				log().debug("  }");
 
 			}
 
 			// remove null CoreMetadata entries
 
 			final Vector<OMETIFFPlane[]> planeInfo = new Vector<OMETIFFPlane[]>();
 			for (int i = meta.getImageCount() - 1; i >= 0; i--) {
 				if (meta.get(i) == null) {
 					meta.getAll().remove(i);
 					adjustedSamples.remove(i);
 					samples.remove(i);
 				}
 				else {
 					planeInfo.add(0, info[i]);
 				}
 			}
 			info = planeInfo.toArray(new OMETIFFPlane[0][0]);
 
 //      meta.getOmeMeta().populateImageMetadata();
 		}
 
 		// -- Helper methods --
 
 		private String normalizeFilename(final String dir, final String name) {
 			final File file = new File(dir, name);
 			if (file.exists()) return file.getAbsolutePath();
 			return name;
 		}
 	}
 
 	/**
 	 * @author Mark Hiner hinerm at gmail.com
 	 */
 	public static class Reader extends ByteArrayReader<Metadata> {
 
 		// -- Reader API Methods --
 
 		@Override
 		public long getOptimalTileWidth(final int imageIndex) {
 			return getMetadata().getTileWidth()[imageIndex];
 		}
 
 		@Override
 		public long getOptimalTileHeight(final int imageIndex) {
 			return getMetadata().getTileHeight()[imageIndex];
 		}
 
 		@Override
 		public String[] getDomains() {
 			FormatTools.assertId(getMetadata().getDatasetName(), true, 1);
 			return getMetadata().isHasSPW() ? new String[] { FormatTools.HCS_DOMAIN }
 				: FormatTools.NON_SPECIAL_DOMAINS;
 		}
 
 		@Override
 		public ByteArrayPlane openPlane(final int imageIndex,
 			final long planeIndex, final ByteArrayPlane plane, final long[] offsets,
 			final long[] lengths, final SCIFIOConfig config)
 			throws io.scif.FormatException, IOException
 		{
 			final Metadata meta = getMetadata();
 			final byte[] buf = plane.getBytes();
 			final OMETIFFPlane[][] info = meta.getPlaneInfo();
 
 			FormatTools.checkPlaneForReading(meta, imageIndex, planeIndex,
 				buf.length, offsets, lengths);
 			meta.setLastPlane(planeIndex);
 			final int i = info[imageIndex][(int) planeIndex].ifd;
 			final MinimalTIFFFormat.Reader<?> r =
 				info[imageIndex][(int) planeIndex].reader;
 			if (r.getCurrentFile() == null) {
 				r.setSource(info[imageIndex][(int) planeIndex].id);
 			}
 			final IFDList ifdList = r.getMetadata().getIfds();
 			if (i >= ifdList.size()) {
 				log()
 					.warn("Error untangling IFDs; the OME-TIFF file may be malformed.");
 				return plane;
 			}
 			final IFD ifd = ifdList.get(i);
 			final RandomAccessInputStream s =
 				new RandomAccessInputStream(getContext(),
 					info[imageIndex][(int) planeIndex].id);
 			final TiffParser p = new TiffParser(getContext(), s);
 			final int xIndex = meta.get(imageIndex).getAxisIndex(Axes.X), yIndex =
 				meta.get(imageIndex).getAxisIndex(Axes.Y);
 			final int x = (int) offsets[xIndex], y = (int) offsets[yIndex], w =
 				(int) lengths[xIndex], h = (int) lengths[yIndex];
 			p.getSamples(ifd, buf, x, y, w, h);
 			s.close();
 			return plane;
 		}
 
 		// -- Groupable API Methods --
 
 		@Override
 		public boolean isSingleFile(final String id) throws FormatException,
 			IOException
 		{
 			return OMETIFFFormat.isSingleFile(getContext(), id);
 		}
 
 		@Override
 		public boolean hasCompanionFiles() {
 			return true;
 		}
 
 		@Override
 		protected String[] createDomainArray() {
 			return FormatTools.NON_GRAPHICS_DOMAINS;
 		}
 
 	}
 
 	/**
 	 * @author Mark Hiner hinerm at gmail.com
 	 */
 	public static class Writer extends TIFFFormat.Writer<Metadata> {
 
 		// -- Constants --
 
 		private static final String WARNING_COMMENT =
 			"<!-- Warning: this comment is an OME-XML metadata block, which " +
 				"contains crucial dimensional parameters and other important metadata. " +
 				"Please edit cautiously (if at all), and back up the original data " +
 				"before doing so. For more information, see the OME-TIFF web site: " +
 				URL_OME_TIFF + ". -->";
 
 		// -- Fields --
 
 		private List<Integer> imageMap;
 		private String[][] imageLocations;
 		private OMEXMLMetadata omeMeta;
 		private final Map<String, Integer> ifdCounts =
 			new HashMap<String, Integer>();
 
 		private final Map<String, String> uuids = new HashMap<String, String>();
 
 		// -- Writer API Methods --
 
 		/* @see IFormatHandler#setId(String) */
 		@Override
 		public void setDest(RandomAccessOutputStream out, int imageIndex,
 			SCIFIOConfig config) throws FormatException, IOException
 		{
 			// TODO if already set, return
 			super.setDest(out, imageIndex, config);
 			if (imageLocations == null) {
 				final MetadataRetrieve r = getMetadata().getOmeMeta().getRoot();
 				imageLocations = new String[r.getImageCount()][];
 				for (int i = 0; i < imageLocations.length; i++) {
 					imageLocations[i] = new String[planeCount(imageIndex)];
 				}
 			}
 		}
 
		/*
		 * @see io.scif.Writer#savePlane(int, int, io.scif.Plane, int, int, int, int)
		 */
		@Override
		public void savePlane(final int imageIndex, final long planeIndex,
			final Plane plane, final long[] offsets, final long[] lengths)
			throws FormatException, IOException
		{
			savePlane(imageIndex, planeIndex, plane, null, offsets, lengths);
		}

 		@Override
 		public void savePlane(final int imageIndex, final long planeIndex,
 			final Plane plane, final IFD ifd, final long[] offsets,
 			final long[] lengths) throws io.scif.FormatException, IOException
 		{
 			if (imageMap == null) imageMap = new ArrayList<Integer>();
 			if (!imageMap.contains(imageIndex)) {
 				imageMap.add(new Integer(imageIndex));
 			}
 
 			super.savePlane(imageIndex, planeIndex, plane, ifd, offsets, lengths);
 
 			// TODO should this be the output id?
 			imageLocations[imageIndex][(int) planeIndex] =
 				getMetadata().getDatasetName();
 		}
 
 		/* @see loci.formats.IFormatHandler#close() */
 		@Override
 		public void close() throws IOException {
 			try {
 				if (getStream() != null) {
 					setupServiceAndMetadata();
 
 					// remove any BinData elements from the OME-XML
 					service.removeBinData(omeMeta);
 
 					for (int series = 0; series < omeMeta.getImageCount(); series++) {
 						populateImage(omeMeta, series);
 					}
 
 					final List<String> files = new ArrayList<String>();
 					for (final String[] s : imageLocations) {
 						for (final String f : s) {
 							if (!files.contains(f) && f != null) {
 								files.add(f);
 
 								final String xml = getOMEXML(f);
 
 								// write OME-XML to the first IFD's comment
 								saveComment(f, xml);
 							}
 						}
 					}
 				}
 			}
 			catch (final ServiceException se) {
 				throw new RuntimeException(se);
 			}
 			catch (final FormatException fe) {
 				throw new RuntimeException(fe);
 			}
 			catch (final IllegalArgumentException iae) {
 				throw new RuntimeException(iae);
 			}
 			finally {
 				super.close();
 
 				boolean canReallyClose =
 					omeMeta == null || ifdCounts.size() == omeMeta.getImageCount();
 
 				if (omeMeta != null && canReallyClose) {
 					int omePlaneCount = 0;
 					for (int i = 0; i < omeMeta.getImageCount(); i++) {
 						final int sizeZ = omeMeta.getPixelsSizeZ(i).getValue();
 						final int sizeC = omeMeta.getPixelsSizeC(i).getValue();
 						final int sizeT = omeMeta.getPixelsSizeT(i).getValue();
 
 						omePlaneCount += sizeZ * sizeC * sizeT;
 					}
 
 					int ifdCount = 0;
 					for (final String key : ifdCounts.keySet()) {
 						ifdCount += ifdCounts.get(key);
 					}
 
 					canReallyClose = omePlaneCount == ifdCount;
 				}
 
 				if (canReallyClose) {
 					imageMap = null;
 					imageLocations = null;
 					omeMeta = null;
 					ifdCounts.clear();
 				}
 				else {
 					for (final String k : ifdCounts.keySet())
 						ifdCounts.put(k, 0);
 				}
 			}
 		}
 
 		// -- Helper methods --
 
 		/** Gets the UUID corresponding to the given filename. */
 		private String getUUID(final String filename) {
 			String uuid = uuids.get(filename);
 			if (uuid == null) {
 				uuid = UUID.randomUUID().toString();
 				uuids.put(filename, uuid);
 			}
 			return uuid;
 		}
 
 		private void setupServiceAndMetadata() throws ServiceException {
 			// extract OME-XML string from metadata object
 			final MetadataRetrieve retrieve = getMetadata().getOmeMeta().getRoot();
 
 			final OMEXMLMetadata originalOMEMeta = service.getOMEMetadata(retrieve);
 			originalOMEMeta.resolveReferences();
 
 			final String omexml = service.getOMEXML(originalOMEMeta);
 			omeMeta = service.createOMEXMLMetadata(omexml);
 		}
 
 		private String getOMEXML(final String file) throws FormatException,
 			IOException
 		{
 			// generate UUID and add to OME element
 			final String uuid =
 				"urn:uuid:" + getUUID(new Location(getContext(), file).getName());
 			omeMeta.setUUID(uuid);
 
 			String xml;
 			try {
 				xml = service.getOMEXML(omeMeta);
 			}
 			catch (final ServiceException se) {
 				throw new FormatException(se);
 			}
 
 			// insert warning comment
 			final String prefix = xml.substring(0, xml.indexOf(">") + 1);
 			final String suffix = xml.substring(xml.indexOf(">") + 1);
 			return prefix + WARNING_COMMENT + suffix;
 		}
 
 		private void saveComment(final String file, final String xml)
 			throws IOException, FormatException
 		{
 			if (getStream() != null) getStream().close();
 			setDest(new RandomAccessOutputStream(getContext(), file));
 			RandomAccessInputStream in = null;
 			try {
 				final TiffSaver saver = new TiffSaver(getContext(), getStream(), file);
 				saver.setBigTiff(isBigTiff());
 				in = new RandomAccessInputStream(getContext(), file);
 				saver.overwriteLastIFDOffset(in);
 				saver.overwriteComment(in, xml);
 				in.close();
 			}
 			catch (final FormatException exc) {
 				final IOException io =
 					new IOException("Unable to append OME-XML comment");
 				io.initCause(exc);
 				throw io;
 			}
 			finally {
 				if (getStream() != null) getStream().close();
 				if (in != null) in.close();
 			}
 		}
 
 		private void populateTiffData(final OMEXMLMetadata omeMeta,
 			final long[] zct, final int ifd, final int series, final int plane)
 		{
 			omeMeta.setTiffDataFirstZ(new NonNegativeInteger((int) zct[0]), series,
 				plane);
 			omeMeta.setTiffDataFirstC(new NonNegativeInteger((int) zct[1]), series,
 				plane);
 			omeMeta.setTiffDataFirstT(new NonNegativeInteger((int) zct[2]), series,
 				plane);
 			omeMeta.setTiffDataIFD(new NonNegativeInteger(ifd), series, plane);
 			omeMeta.setTiffDataPlaneCount(new NonNegativeInteger(1), series, plane);
 		}
 
 		private void populateImage(final OMEXMLMetadata omeMeta,
 			final int imageIndex)
 		{
 			final String dimensionOrder =
 				omeMeta.getPixelsDimensionOrder(imageIndex).toString();
 			final int sizeZ =
 				omeMeta.getPixelsSizeZ(imageIndex).getValue().intValue();
 			int sizeC = omeMeta.getPixelsSizeC(imageIndex).getValue().intValue();
 			final int sizeT =
 				omeMeta.getPixelsSizeT(imageIndex).getValue().intValue();
 
 			final long planeCount = getMetadata().get(imageIndex).getPlaneCount();
 			final int ifdCount = imageMap.size();
 
 			if (planeCount == 0) {
 				omeMeta.setTiffDataPlaneCount(new NonNegativeInteger(0), imageIndex, 0);
 				return;
 			}
 
 			final PositiveInteger samplesPerPixel =
 				new PositiveInteger((int) ((sizeZ * sizeC * sizeT) / planeCount));
 			for (int c = 0; c < omeMeta.getChannelCount(imageIndex); c++) {
 				omeMeta.setChannelSamplesPerPixel(samplesPerPixel, imageIndex, c);
 			}
 			sizeC /= samplesPerPixel.getValue();
 
 			final long[] lengths =
 				metaService.zctToArray(dimensionOrder, sizeC, sizeC, sizeT);
 
 			int nextPlane = 0;
 			for (int plane = 0; plane < planeCount; plane++) {
 				metaService.zctToArray(dimensionOrder, sizeC, sizeC, sizeT);
 				final long[] zct = FormatTools.rasterToPosition(lengths, planeCount);
 
 				int planeIndex = plane;
 				if (imageLocations[imageIndex].length < planeCount) {
 					planeIndex /= (planeCount / imageLocations[imageIndex].length);
 				}
 
 				String filename = imageLocations[imageIndex][planeIndex];
 				if (filename != null) {
 					filename = new Location(getContext(), filename).getName();
 
 					final Integer ifdIndex = ifdCounts.get(filename);
 					final int ifd = ifdIndex == null ? 0 : ifdIndex.intValue();
 
 					omeMeta.setUUIDFileName(filename, imageIndex, nextPlane);
 					final String uuid = "urn:uuid:" + getUUID(filename);
 					omeMeta.setUUIDValue(uuid, imageIndex, nextPlane);
 
 					// fill in any non-default TiffData attributes
 					populateTiffData(omeMeta, zct, ifd, imageIndex, nextPlane);
 					ifdCounts.put(filename, ifd + 1);
 					nextPlane++;
 				}
 			}
 		}
 
 		private int planeCount(final int imageIndex) {
 			final MetadataRetrieve r = getMetadata().getOmeMeta().getRoot();
 			final int z = r.getPixelsSizeZ(imageIndex).getValue().intValue();
 			final int t = r.getPixelsSizeT(imageIndex).getValue().intValue();
 			int c = r.getChannelCount(imageIndex);
 			final String pixelType = r.getPixelsType(imageIndex).getValue();
 			final int bytes = FormatTools.getBytesPerPixel(pixelType);
 
 			if (bytes > 1 && c == 1) {
 				c = r.getChannelSamplesPerPixel(imageIndex, 0).getValue();
 			}
 
 			return z * c * t;
 		}
 
 	}
 
 	// -- Helper Methods --
 
 	private static boolean isSingleFile(final Context context, final String id)
 		throws FormatException, IOException
 	{
 		// parse and populate OME-XML metadata
 		final String fileName =
 			new Location(context, id).getAbsoluteFile().getAbsolutePath();
 		final RandomAccessInputStream ras =
 			new RandomAccessInputStream(context, fileName);
 		final TiffParser tp = new TiffParser(context, ras);
 		final IFD ifd = tp.getFirstIFD();
 		final long[] ifdOffsets = tp.getIFDOffsets();
 		ras.close();
 		final String xml = ifd.getComment();
 
 		OMEXMLMetadata meta;
 		try {
 			meta = service.createOMEXMLMetadata(xml);
 		}
 		catch (final ServiceException se) {
 			throw new FormatException(se);
 		}
 
 		if (meta.getRoot() == null) {
 			throw new FormatException("Could not parse OME-XML from TIFF comment");
 		}
 
 		int nImages = 0;
 		for (int i = 0; i < meta.getImageCount(); i++) {
 			int nChannels = meta.getChannelCount(i);
 			if (nChannels == 0) nChannels = 1;
 			final int z = meta.getPixelsSizeZ(i).getValue().intValue();
 			final int t = meta.getPixelsSizeT(i).getValue().intValue();
 			nImages += z * t * nChannels;
 		}
 		return nImages <= ifdOffsets.length;
 	}
 
 	/**
 	 * This class can be used for translating any io.scif.Metadata to Metadata for
 	 * writing OME-TIFF. files.
 	 * <p>
 	 * Note that Metadata translated from Core is only write-safe.
 	 * </p>
 	 * <p>
 	 * If trying to read, there should already exist an originally-parsed OME-TIFF
 	 * Metadata object which can be used.
 	 * </p>
 	 * <p>
 	 * Note also that any OME-TIFF image written must be reparsed, as the Metadata
 	 * used to write it can not be guaranteed valid.
 	 * </p>
 	 */
 	@Plugin(type = Translator.class, priority = OMETIFFFormat.PRIORITY)
 	public static class OMETIFFTranslator extends
 		AbstractTranslator<io.scif.Metadata, Metadata>
 	{
 
 		// -- Fields --
 
 		@Parameter
 		private FormatService formatService;
 
 		@Parameter
 		private TranslatorService translatorService;
 
 		// -- Translator API Methods --
 
 		@Override
 		public Class<? extends io.scif.Metadata> source() {
 			return io.scif.Metadata.class;
 		}
 
 		@Override
 		public Class<? extends io.scif.Metadata> dest() {
 			return Metadata.class;
 		}
 
 		@Override
 		public void translateFormatMetadata(final io.scif.Metadata source,
 			final Metadata dest)
 		{
 
 			if (dest.getOmeMeta() == null) {
 				final OMEMetadata omeMeta = new OMEMetadata(getContext());
 				translatorService.translate(source, omeMeta, false);
 				dest.setOmeMeta(omeMeta);
 			}
 
 			try {
 				final TIFFFormat.Metadata tiffMeta =
 					(TIFFFormat.Metadata) formatService.getFormatFromClass(
 						TIFFFormat.class).createMetadata();
 
 				translatorService.translate(source, tiffMeta, false);
 
 				dest.setFirstIFD(tiffMeta.getIfds().get(0));
 
 			}
 			catch (final FormatException e) {
 				log().error("Failed to generate TIFF data", e);
 			}
 
 			final OMETIFFPlane[][] info = new OMETIFFPlane[source.getImageCount()][];
 			dest.setPlaneInfo(info);
 
 			final List<Integer> samples = new ArrayList<Integer>();
 			final List<Boolean> adjustedSamples = new ArrayList<Boolean>();
 
 			dest.samples = samples;
 			dest.adjustedSamples = adjustedSamples;
 
 			dest.createImageMetadata(0);
 
 			for (int i = 0; i < source.getImageCount(); i++) {
 				info[i] = new OMETIFFPlane[(int) source.get(i).getPlaneCount()];
 
 				for (int j = 0; j < source.get(i).getPlaneCount(); j++) {
 					info[i][j] = new OMETIFFPlane();
 				}
 
 				dest.add(new DefaultImageMetadata());
 
 				samples.add((int) (source.get(i).isMultichannel() ? source.get(i)
 					.getAxisLength(Axes.CHANNEL) : 1));
 				adjustedSamples.add(false);
 			}
 		}
 
 		@Override
 		protected void translateImageMetadata(final List<ImageMetadata> source,
 			final Metadata dest)
 		{
 			// No implementation necessary. See translateFormatMetadata
 		}
 	}
 
 	// -- Helper classes --
 
 	/** Structure containing details on where to find a particular image plane. */
 	private static class OMETIFFPlane {
 
 		/** Reader to use for accessing this plane. */
 		public MinimalTIFFFormat.Reader<?> reader;
 		/** File containing this plane. */
 		public String id;
 		/** IFD number of this plane. */
 		public int ifd = -1;
 		/** Certainty flag, for dealing with unspecified NumPlanes. */
 		public boolean certain = false;
 	}
 }
