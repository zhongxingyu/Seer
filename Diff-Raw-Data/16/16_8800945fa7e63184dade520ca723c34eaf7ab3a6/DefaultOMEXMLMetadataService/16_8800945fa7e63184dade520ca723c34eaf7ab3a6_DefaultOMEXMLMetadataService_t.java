 /*
  * #%L
  * SCIFIO OME-XML Formats.
  * %%
  * Copyright (C) 2013 Open Microscopy Environment:
  *   - Massachusetts Institute of Technology
  *   - National Institutes of Health
  *   - University of Dundee
  *   - Board of Regents of the University of Wisconsin-Madison
  *   - Glencoe Software, Inc.
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
  * 
  * The views and conclusions contained in the software and documentation are
  * those of the authors and should not be interpreted as representing official
  * policies, either expressed or implied, of any organization.
  * #L%
  */
 
 package io.scif.ome.xml.services;
 
 import io.scif.FormatException;
 import io.scif.ImageMetadata;
 import io.scif.Metadata;
 import io.scif.Reader;
 import io.scif.common.DateTools;
 import io.scif.io.Location;
 import io.scif.ome.xml.meta.OMEXMLMetadata;
 import io.scif.services.FormatService;
 import io.scif.services.ServiceException;
 import io.scif.util.FormatTools;
 import io.scif.util.SCIFIOMetadataTools;
 import loci.formats.meta.MetadataRetrieve;
 import loci.formats.meta.MetadataStore;
 import net.imglib2.meta.Axes;
 import net.imglib2.meta.AxisType;
 import net.imglib2.meta.CalibratedAxis;
 import net.imglib2.meta.axis.DefaultLinearAxis;
 import ome.xml.model.BinData;
 import ome.xml.model.OME;
 import ome.xml.model.enums.Binning;
 import ome.xml.model.enums.Correction;
 import ome.xml.model.enums.DetectorType;
 import ome.xml.model.enums.DimensionOrder;
 import ome.xml.model.enums.EnumerationException;
 import ome.xml.model.enums.ExperimentType;
 import ome.xml.model.enums.Immersion;
 import ome.xml.model.enums.LaserMedium;
 import ome.xml.model.enums.LaserType;
 import ome.xml.model.enums.PixelType;
 import ome.xml.model.enums.handlers.BinningEnumHandler;
 import ome.xml.model.enums.handlers.CorrectionEnumHandler;
 import ome.xml.model.enums.handlers.DetectorTypeEnumHandler;
 import ome.xml.model.enums.handlers.ExperimentTypeEnumHandler;
 import ome.xml.model.enums.handlers.ImmersionEnumHandler;
 import ome.xml.model.enums.handlers.LaserMediumEnumHandler;
 import ome.xml.model.enums.handlers.LaserTypeEnumHandler;
 import ome.xml.model.primitives.NonNegativeInteger;
 import ome.xml.model.primitives.NonNegativeLong;
 import ome.xml.model.primitives.PositiveFloat;
 import ome.xml.model.primitives.PositiveInteger;
 import ome.xml.model.primitives.Timestamp;
 
 import org.scijava.log.LogService;
 import org.scijava.plugin.Parameter;
 import org.scijava.plugin.Plugin;
 import org.scijava.service.AbstractService;
 import org.scijava.service.Service;
 
 /**
  * Default implementation of {@link OMEXMLMetadataService}.
  */
 @Plugin(type = Service.class)
 public class DefaultOMEXMLMetadataService extends AbstractService implements
 	OMEXMLMetadataService
 {
 
 	// -- Static fields --
 
 	private boolean defaultDateEnabled = true;
 
 	// -- Parameters --
 
 	@Parameter
 	private FormatService formatService;
 	
 	@Parameter
 	private LogService logService;
 
 	// -- Utility methods - OME-XML --
 
 	@Override
 	public void populatePixels(final MetadataStore store, final Metadata meta) {
 		populatePixels(store, meta, false, true);
 	}
 
 	@Override
 	public void populatePixels(final MetadataStore store, final Metadata meta,
 		final boolean doPlane)
 	{
 		populatePixels(store, meta, doPlane, true);
 	}
 
 	@Override
 	public void populatePixels(final MetadataStore store, final Metadata meta,
 		final boolean doPlane, final boolean doImageName)
 	{
 		if (store == null || meta == null) return;
 		for (int i = 0; i < meta.getImageCount(); i++) {
 
 			String imageName = null;
 			if (doImageName) {
 				final Location f = new Location(getContext(), meta.getDatasetName());
 				imageName = f.getName();
 			}
 			final String pixelType =
 				FormatTools.getPixelTypeString(meta.getPixelType(i));
 			final String order = findDimensionOrder(meta, i);
 
 			final int xSize = (int) meta.getAxisLength(i, Axes.X);
 			int ySize = (int) meta.getAxisLength(i, Axes.Y);
 			final int zSize = (int) meta.getAxisLength(i, Axes.Z);
 			final int cSize = (int) meta.getAxisLength(i, Axes.CHANNEL);
 			int tSize = (int) meta.getAxisLength(i, Axes.TIME);
 			final double calX = FormatTools.getScale(meta, i, Axes.X);
 			final double calY = FormatTools.getScale(meta, i, Axes.Y);
 			final double calZ = FormatTools.getScale(meta, i, Axes.Z);
 			final double calC = FormatTools.getScale(meta, i, Axes.CHANNEL);
 			final double calT = FormatTools.getScale(meta, i, Axes.TIME);
 			int rgbCCount = 1;
 
 			if (meta.isMultichannel(i)) {
 				rgbCCount = cSize;
 			}
 
 			// Compress planar axes to Y
 			for (CalibratedAxis axis : meta.getAxesPlanar(i)) {
 				final AxisType type = axis.type();
 				if (type != Axes.X && type != Axes.Y && type != Axes.CHANNEL) {
 					ySize *= meta.getAxisLength(i, type);
 				}
 			}
 			//Compress non-planar axes to Time
 			for (CalibratedAxis axis : meta.getAxesNonPlanar(i)) {
 				final AxisType type = axis.type();
 				if (type != Axes.Z && type != Axes.TIME && type != Axes.CHANNEL) {
 					tSize *= meta.getAxisLength(i, type);
 				}
 			}
 
 			populateMetadata(store, meta.getDatasetName(), i, imageName, meta
 				.isLittleEndian(i), order, pixelType, xSize, ySize, zSize, cSize,
 				tSize, calX, calY, calZ, calC, calT, rgbCCount);
 
 			final OMEXMLService service =
 				formatService.getInstance(OMEXMLService.class);
 			if (service.isOMEXMLRoot(store.getRoot())) {
 				// TODO any way or reason to access a base store?
 				if (service.isOMEXMLMetadata(store)) {
 					OMEXMLMetadata omeMeta;
 					try {
 						omeMeta = service.getOMEMetadata(service.asRetrieve(store));
 						omeMeta.resolveReferences();
 					}
 					catch (final ServiceException e) {
 						logService.warn("Failed to resolve references", e);
 					}
 				}
 
 				final OME root = (OME) store.getRoot();
 				final BinData bin = root.getImage(i).getPixels().getBinData(0);
 				bin.setLength(new NonNegativeLong(0L));
 				store.setRoot(root);
 			}
 
 			if (doPlane) {
 				for (int q = 0; q < meta.getPlaneCount(i); q++) {
 					final long[] coords = FormatTools.rasterToPosition(i, q, meta);
 					store.setPlaneTheZ(new NonNegativeInteger((int) coords[0]), i, q);
 					store.setPlaneTheC(new NonNegativeInteger((int) coords[1]), i, q);
 					store.setPlaneTheT(new NonNegativeInteger((int) coords[2]), i, q);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void populateMetadata(final MetadataStore store, final int imageIndex,
 		final String imageName, final boolean littleEndian,
 		final String dimensionOrder, final String pixelType, final int sizeX,
 		final int sizeY, final int sizeZ, final int sizeC, final int sizeT,
 		final double calX, final double calY, final double calZ, final double calC,
 		final double calT, final int samplesPerPixel)
 	{
 		populateMetadata(store, null, imageIndex, imageName, littleEndian,
 			dimensionOrder, pixelType, sizeX, sizeY, sizeZ, sizeC, sizeT, calX, calY,
 			calZ, calC, calT, samplesPerPixel);
 	}
 
 	@Override
 	public void populateMetadata(final MetadataStore store, final int imageIndex,
 		final String imageName, final Metadata meta)
 	{
 
 		final int sizeX = (int) meta.getAxisLength(imageIndex, Axes.X);
 		int sizeY = (int) meta.getAxisLength(imageIndex, Axes.Y);
 		final int sizeZ = (int) meta.getAxisLength(imageIndex, Axes.Z);
 		final int sizeC = (int) meta.getAxisLength(imageIndex, Axes.CHANNEL);
 		int sizeT = (int) meta.getAxisLength(imageIndex, Axes.TIME);
 		final double calX = FormatTools.getScale(meta, imageIndex, Axes.X);
 		final double calY = FormatTools.getScale(meta, imageIndex, Axes.Y);
 		final double calZ = FormatTools.getScale(meta, imageIndex, Axes.Z);
 		final double calC = FormatTools.getScale(meta, imageIndex, Axes.CHANNEL);
 		final double calT = FormatTools.getScale(meta, imageIndex, Axes.TIME);
 
 		// Compress planar axes to Y
 		for (CalibratedAxis axis : meta.getAxesPlanar(imageIndex)) {
 			final AxisType type = axis.type();
 			if (type != Axes.X && type != Axes.Y && type != Axes.CHANNEL) {
 				sizeY *= meta.getAxisLength(imageIndex, type);
 			}
 		}
 		// Compress non-planar axes to Time
 		for (CalibratedAxis axis : meta.getAxesNonPlanar(imageIndex)) {
 			final AxisType type = axis.type();
 			if (type != Axes.Z && type != Axes.TIME && type != Axes.CHANNEL) {
 				sizeT *= meta.getAxisLength(imageIndex, type);
 			}
 		}
 
 		final String pixelType =
 			FormatTools.getPixelTypeString(meta.getPixelType(imageIndex));
 		final int effSizeC = (int)(meta.getPlaneCount(imageIndex) / sizeZ / sizeT);
 		final int samplesPerPixel = sizeC / effSizeC;
 		populateMetadata(store, null, imageIndex, imageName, meta
 			.isLittleEndian(imageIndex), findDimensionOrder(meta, imageIndex),
 			pixelType, sizeX, sizeY, sizeZ, sizeC, sizeT, calX, calY,
 			calZ, calC, calT, samplesPerPixel);
 	}
 
 	@Override
 	public void populateMetadata(final MetadataStore store, final String file,
 		final int imageIndex, final String imageName, final boolean littleEndian,
 		final String dimensionOrder, final String pixelType, final int sizeX,
 		final int sizeY, final int sizeZ, final int sizeC, final int sizeT,
 		final double calX, final double calY, final double calZ, final double calC,
 		final double calT, final int samplesPerPixel)
 	{
 		store.setImageID(createLSID("Image", imageIndex), imageIndex);
 		setDefaultCreationDate(store, file, imageIndex);
 		if (imageName != null) store.setImageName(imageName, imageIndex);
 		populatePixelsOnly(store, imageIndex, littleEndian, dimensionOrder,
 			pixelType, sizeX, sizeY, sizeZ, sizeC, sizeT, calX, calY, calZ, calC,
 			calT, samplesPerPixel);
 	}
 
 	@Override
 	public void populatePixelsOnly(final MetadataStore store, final Reader r) {
 		final Metadata meta = r.getMetadata();
 
 		for (int imageIndex = 0; imageIndex < r.getImageCount(); imageIndex++) {
 			final String pixelType =
 				FormatTools.getPixelTypeString(meta.getPixelType(imageIndex));
 			final int xSize = (int) meta.getAxisLength(imageIndex, Axes.X);
 			int ySize = (int) meta.getAxisLength(imageIndex, Axes.Y);
 			final int zSize = (int) meta.getAxisLength(imageIndex, Axes.Z);
 			final int cSize = (int) meta.getAxisLength(imageIndex, Axes.CHANNEL);
 			int tSize = (int) meta.getAxisLength(imageIndex, Axes.TIME);
 			final double calX = FormatTools.getScale(meta, imageIndex, Axes.X);
 			final double calY = FormatTools.getScale(meta, imageIndex, Axes.Y);
 			final double calZ = FormatTools.getScale(meta, imageIndex, Axes.Z);
 			final double calC = FormatTools.getScale(meta, imageIndex, Axes.CHANNEL);
 			final double calT = FormatTools.getScale(meta, imageIndex, Axes.TIME);
 			int rgbCCount = 1;
 
 			if (meta.isMultichannel(imageIndex)) {
 				rgbCCount = cSize;
 			}
 
 			// Compress planar axes to Y
 			for (CalibratedAxis axis : meta.getAxesPlanar(imageIndex)) {
 				final AxisType type = axis.type();
 				if (type != Axes.X && type != Axes.Y && type != Axes.CHANNEL) {
 					ySize *= meta.getAxisLength(imageIndex, type);
 				}
 			}
 			// Compress non-planar axes to Time
 			for (CalibratedAxis axis : meta.getAxesNonPlanar(imageIndex)) {
 				final AxisType type = axis.type();
 				if (type != Axes.Z && type != Axes.TIME && type != Axes.CHANNEL) {
 					tSize *= meta.getAxisLength(imageIndex, type);
 				}
 			}
 			populatePixelsOnly(store, imageIndex, meta.isLittleEndian(imageIndex),
 				findDimensionOrder(meta, imageIndex), pixelType, xSize, ySize, zSize,
 				cSize, tSize, calX, calY, calZ, calC, calT, rgbCCount);
 		}
 	}
 
 	@Override
 	public void populatePixelsOnly(final MetadataStore store,
 		final int imageIndex, final boolean littleEndian,
 		final String dimensionOrder, final String pixelType, final int sizeX,
 		final int sizeY, final int sizeZ, final int sizeC, final int sizeT,
 		final double calX, final double calY, final double calZ, final double calC,
 		final double calT, final int samplesPerPixel)
 	{
 		store.setPixelsID(createLSID("Pixels", imageIndex), imageIndex);
 		store.setPixelsBinDataBigEndian(!littleEndian, imageIndex, 0);
 		try {
 			store.setPixelsDimensionOrder(DimensionOrder.fromString(dimensionOrder),
 				imageIndex);
 		}
 		catch (final EnumerationException e) {
 			logService.warn("Invalid dimension order: " + dimensionOrder, e);
 		}
 		try {
 			store.setPixelsType(PixelType.fromString(pixelType), imageIndex);
 		}
 		catch (final EnumerationException e) {
 			logService.warn("Invalid pixel type: " + pixelType, e);
 		}
 		store.setPixelsSizeX(new PositiveInteger(sizeX), imageIndex);
 		store.setPixelsSizeY(new PositiveInteger(sizeY), imageIndex);
 		store.setPixelsSizeZ(new PositiveInteger(sizeZ), imageIndex);
 		store.setPixelsSizeC(new PositiveInteger(sizeC), imageIndex);
 		store.setPixelsSizeT(new PositiveInteger(sizeT), imageIndex);
 		store.setPixelsPhysicalSizeX(new PositiveFloat(calX), imageIndex);
 		store.setPixelsPhysicalSizeY(new PositiveFloat(calY), imageIndex);
 		store.setPixelsPhysicalSizeZ(new PositiveFloat(calZ), imageIndex);
 		store.setPixelsTimeIncrement(calT, imageIndex);
 		final int effSizeC = sizeC / samplesPerPixel;
 		for (int i = 0; i < effSizeC; i++) {
 			store.setChannelID(createLSID("Channel", imageIndex, i), imageIndex, i);
 			store.setChannelSamplesPerPixel(new PositiveInteger(samplesPerPixel),
 				imageIndex, i);
 		}
 	}
 
 	@Override
 	public void setDefaultDateEnabled(final boolean enabled) {
 		defaultDateEnabled = enabled;
 	}
 
 	@Override
 	public void setDefaultCreationDate(final MetadataStore store,
 		final String id, final int imageIndex)
 	{
 		if (!defaultDateEnabled) {
 			return;
 		}
 		final Location file =
 			id == null ? null : new Location(getContext(), id).getAbsoluteFile();
 		long time = System.currentTimeMillis();
 		if (file != null && file.exists()) time = file.lastModified();
 		store.setImageAcquisitionDate(new Timestamp(DateTools.convertDate(time,
 			DateTools.UNIX)), imageIndex);
 	}
 
 	@Override
 	public void verifyMinimumPopulated(final MetadataRetrieve src)
 		throws FormatException
 	{
 		verifyMinimumPopulated(src, 0);
 	}
 
 	@Override
 	public void verifyMinimumPopulated(final MetadataRetrieve src, final int n)
 		throws FormatException
 	{
 		if (src == null) {
 			throw new FormatException("Metadata object is null; "
 				+ "call IFormatWriter.setMetadataRetrieve() first");
 		}
 		if (src instanceof MetadataStore && ((MetadataStore) src).getRoot() == null)
 		{
 			throw new FormatException("Metadata object has null root; "
 				+ "call IMetadata.createRoot() first");
 		}
 		if (src.getImageID(n) == null) {
 			throw new FormatException("Image ID #" + n + " is null");
 		}
 		if (src.getPixelsID(n) == null) {
 			throw new FormatException("Pixels ID #" + n + " is null");
 		}
 		for (int i = 0; i < src.getChannelCount(n); i++) {
 			if (src.getChannelID(n, i) == null) {
 				throw new FormatException("Channel ID #" + i + " in Image #" + n +
 					" is null");
 			}
 		}
 		if (src.getPixelsBinDataBigEndian(n, 0) == null) {
 			throw new FormatException("BigEndian #" + n + " is null");
 		}
 		if (src.getPixelsDimensionOrder(n) == null) {
 			throw new FormatException("DimensionOrder #" + n + " is null");
 		}
 		if (src.getPixelsType(n) == null) {
 			throw new FormatException("PixelType #" + n + " is null");
 		}
 		if (src.getPixelsSizeC(n) == null) {
 			throw new FormatException("SizeC #" + n + " is null");
 		}
 		if (src.getPixelsSizeT(n) == null) {
 			throw new FormatException("SizeT #" + n + " is null");
 		}
 		if (src.getPixelsSizeX(n) == null) {
 			throw new FormatException("SizeX #" + n + " is null");
 		}
 		if (src.getPixelsSizeY(n) == null) {
 			throw new FormatException("SizeY #" + n + " is null");
 		}
 		if (src.getPixelsSizeZ(n) == null) {
 			throw new FormatException("SizeZ #" + n + " is null");
 		}
 	}
 
 
 	@Override
 	public String findDimensionOrder(Metadata meta, int imageIndex) {
 		String dimOrder = "";
 
 		for (CalibratedAxis axis : meta.getAxes(imageIndex)) {
 			dimOrder += axis.type().getLabel().charAt(0);
 		}
 
 		return makeSaneDimensionOrder(dimOrder);
 	}
 
 	@Override
 	public AxisType[] findDimensionList(String dimensionOrder) {
 		AxisType[] axes = new AxisType[dimensionOrder.length()];
 		
 		int index = 0;
 		for (char d : dimensionOrder.toUpperCase().toCharArray()) {
 			switch (d) {
 				case 'X': axes[index] = Axes.X;
 					break;
 				case 'Y': axes[index] = Axes.Y;
 					break;
 				case 'Z': axes[index] = Axes.Z;
 					break;
 				case 'C': axes[index] = Axes.CHANNEL;
 					break;
 				case 'T': axes[index] = Axes.TIME;
 					break;
 				default:
 					axes[index] = Axes.unknown();
 			}
 			index++;
 		}
 		return axes;
 	}
 
 	@Override
 	public long[] zctToArray(String order, int z, int c, int t) {
 		long[] zct = new long[3];
 		int index = 0;
 		for (char dim : order.toUpperCase().toCharArray()) {
 			switch (dim) {
				case 'C':
					zct[index] = c;
					index++;
 					break;
				case 'Z':
					zct[index] = z;
					index++;
 					break;
				case 'T':
					zct[index] = t;
					index++;
 					break;
 			}
 		}
 		return zct;
 	}
 
 	@Override
 	public String makeSaneDimensionOrder(final String dimensionOrder) {
 		String order = dimensionOrder.toUpperCase();
 		order = order.replaceAll("[^XYZCT]", "");
 		final String[] axes = new String[] { "X", "Y", "C", "Z", "T" };
 		for (final String axis : axes) {
 			if (order.indexOf(axis) == -1) order += axis;
 			while (order.indexOf(axis) != order.lastIndexOf(axis)) {
 				order = order.replaceFirst(axis, "");
 			}
 		}
 		return order;
 	}
 
 	@Override
 	public String createLSID(final String type, final int... indices) {
 		final StringBuffer lsid = new StringBuffer(type);
 		for (final int index : indices) {
 			lsid.append(":");
 			lsid.append(index);
 		}
 		return lsid.toString();
 	}
 
 	@Override
 	public ExperimentType getExperimentType(final String value)
 		throws FormatException
 	{
 		final ExperimentTypeEnumHandler handler = new ExperimentTypeEnumHandler();
 		try {
 			return (ExperimentType) handler.getEnumeration(value);
 		}
 		catch (final EnumerationException e) {
 			throw new FormatException("ExperimentType creation failed", e);
 		}
 	}
 
 	@Override
 	public LaserType getLaserType(final String value) throws FormatException {
 		final LaserTypeEnumHandler handler = new LaserTypeEnumHandler();
 		try {
 			return (LaserType) handler.getEnumeration(value);
 		}
 		catch (final EnumerationException e) {
 			throw new FormatException("LaserType creation failed", e);
 		}
 	}
 
 	@Override
 	public LaserMedium getLaserMedium(final String value) throws FormatException {
 		final LaserMediumEnumHandler handler = new LaserMediumEnumHandler();
 		try {
 			return (LaserMedium) handler.getEnumeration(value);
 		}
 		catch (final EnumerationException e) {
 			throw new FormatException("LaserMedium creation failed", e);
 		}
 	}
 
 	@Override
 	public Immersion getImmersion(final String value) throws FormatException {
 		final ImmersionEnumHandler handler = new ImmersionEnumHandler();
 		try {
 			return (Immersion) handler.getEnumeration(value);
 		}
 		catch (final EnumerationException e) {
 			throw new FormatException("Immersion creation failed", e);
 		}
 	}
 
 	@Override
 	public Correction getCorrection(final String value) throws FormatException {
 		final CorrectionEnumHandler handler = new CorrectionEnumHandler();
 		try {
 			return (Correction) handler.getEnumeration(value);
 		}
 		catch (final EnumerationException e) {
 			throw new FormatException("Correction creation failed", e);
 		}
 	}
 
 	@Override
 	public DetectorType getDetectorType(final String value)
 		throws FormatException
 	{
 		final DetectorTypeEnumHandler handler = new DetectorTypeEnumHandler();
 		try {
 			return (DetectorType) handler.getEnumeration(value);
 		}
 		catch (final EnumerationException e) {
 			throw new FormatException("DetectorType creation failed", e);
 		}
 	}
 
 	@Override
 	public Binning getBinning(final String value) throws FormatException {
 		final BinningEnumHandler handler = new BinningEnumHandler();
 		try {
 			return (Binning) handler.getEnumeration(value);
 		}
 		catch (final EnumerationException e) {
 			throw new FormatException("Binning creation failed", e);
 		}
 	}
 
 	@Override
 	public void populateMetadata(final MetadataRetrieve retrieve,
 		final Metadata meta)
 	{
 
 		final int numImages = retrieve.getImageCount();
 
 		if (numImages > 0) meta.setDatasetName(retrieve.getImageName(0));
 
 		meta.createImageMetadata(numImages);
 
 		for (int i = 0; i < numImages; i++) {
 			populateImageMetadata(retrieve, i, meta.get(i));
 		}
 	}
 
 	@Override
 	public void populateImageMetadata(final MetadataRetrieve retrieve,
 		final int imageIndex, final ImageMetadata iMeta)
 	{
 		// Get axis information from the MetadataRetrieve
 		final int sizeX = retrieve.getPixelsSizeX(imageIndex).getValue();
 		final int sizeY = retrieve.getPixelsSizeY(imageIndex).getValue();
 		final int sizeZ = retrieve.getPixelsSizeZ(imageIndex).getValue();
 		int sizeC = retrieve.getPixelsSizeC(imageIndex).getValue();
 		final int sizeT = retrieve.getPixelsSizeT(imageIndex).getValue();
 		final PositiveFloat physX = retrieve.getPixelsPhysicalSizeX(imageIndex);
 		final PositiveFloat physY = retrieve.getPixelsPhysicalSizeY(imageIndex);
 		final PositiveFloat physZ = retrieve.getPixelsPhysicalSizeZ(imageIndex);
 		final Double physT = retrieve.getPixelsTimeIncrement(imageIndex);
 		double calX = physX == null ? 1.0 : physX.getValue();
 		double calY = physY == null ? 1.0 : physY.getValue();
 		double calZ = physZ == null ? 1.0 : physZ.getValue();
 		double calT = physT == null ? 1.0 : physT;
 
 		final String dimensionOrder =
 			retrieve.getPixelsDimensionOrder(imageIndex).getValue();
 		final PositiveInteger spp =
 			retrieve.getChannelCount(imageIndex) <= 0 ? null : retrieve
 				.getChannelSamplesPerPixel(imageIndex, 0);
 
 		final boolean little = !retrieve.getPixelsBinDataBigEndian(imageIndex, 0);
 		final int pType =
 			FormatTools.pixelTypeFromString(retrieve.getPixelsType(imageIndex)
 				.getValue());
 		final int rgbCCount = spp == null ? 1 : spp.getValue();
 		// if we have RGB planes, there are really two "chnanel" axes
 		final int axisCount = rgbCCount == 1 ? 5 : 6;
 
 		final long[] lengths = new long[axisCount];
 		final CalibratedAxis[] axes = new CalibratedAxis[axisCount];
 		iMeta.setPlanarAxisCount(2);
 
 		// populate the axis information in dimension order
 		int i = 0;
 		for (char d : dimensionOrder.toUpperCase().toCharArray()) {
 			// Check for RGB channel position
 			if (axisCount == 6 && i > 0 && axes[i - 1].type() == Axes.Y) {
 				sizeC /= rgbCCount;
 				lengths[i] = rgbCCount;
 				axes[i] = new DefaultLinearAxis(Axes.CHANNEL, "um", 1.0);
 				i++;
 				iMeta.setPlanarAxisCount(i);
 			}
 			switch (d) {
 				case 'X':
 					lengths[i] = sizeX;
 					axes[i] = new DefaultLinearAxis(Axes.X, "um", calX);
 					break;
 				case 'Y':
 					lengths[i] = sizeY;
 					axes[i] = new DefaultLinearAxis(Axes.Y, "um", calY);
 					break;
 				case 'Z':
 					lengths[i] = sizeZ;
 					axes[i] = new DefaultLinearAxis(Axes.Z, "um", calZ);
 					break;
 				case 'C':
 					lengths[i] = sizeC;
 					if (rgbCCount == 1) {
 						axes[i] = new DefaultLinearAxis(Axes.CHANNEL, "um", 1.0);
 					}
 					else {
 						axes[i] = new DefaultLinearAxis(Axes.get("cPlanes"), "um", 1.0);
 					}
 					break;
 				case 'T':
 					lengths[i] = sizeT;
 					axes[i] = new DefaultLinearAxis(Axes.TIME, "um", calT);
 					break;
 			}
 			i++;
 		}
 
 		// populate the metadata
 		SCIFIOMetadataTools.populate(iMeta, axes, lengths, pType, FormatTools
 			.getBitsPerPixel(pType), true, little, false, false, true);
 	}
 }
