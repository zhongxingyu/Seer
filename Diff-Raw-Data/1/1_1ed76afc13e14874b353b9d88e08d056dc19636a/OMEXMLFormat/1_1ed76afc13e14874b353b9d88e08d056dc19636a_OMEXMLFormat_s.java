 /*
  * #%L
  * SCIFIO support for the OME data model (OME-XML and OME-TIFF).
  * %%
  * Copyright (C) 2013 - 2014 Open Microscopy Environment:
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
  * #L%
  */
 
 package io.scif.ome.formats;
 
 import io.scif.AbstractChecker;
 import io.scif.AbstractFormat;
 import io.scif.AbstractMetadata;
 import io.scif.AbstractParser;
 import io.scif.AbstractTranslator;
 import io.scif.AbstractWriter;
 import io.scif.ByteArrayPlane;
 import io.scif.ByteArrayReader;
 import io.scif.Format;
 import io.scif.FormatException;
 import io.scif.Plane;
 import io.scif.Translator;
 import io.scif.codec.Base64Codec;
 import io.scif.codec.CodecOptions;
 import io.scif.codec.CompressionType;
 import io.scif.codec.JPEG2000Codec;
 import io.scif.codec.JPEGCodec;
 import io.scif.codec.ZlibCodec;
 import io.scif.common.Constants;
 import io.scif.config.SCIFIOConfig;
 import io.scif.io.CBZip2InputStream;
 import io.scif.io.RandomAccessInputStream;
 import io.scif.ome.OMEMetadata;
 import io.scif.ome.services.OMEXMLMetadataService;
 import io.scif.ome.services.OMEXMLService;
 import io.scif.ome.translators.FromOMETranslator;
 import io.scif.services.FormatService;
 import io.scif.util.FormatTools;
 import io.scif.util.ImageTools;
 import io.scif.util.SCIFIOMetadataTools;
 import io.scif.xml.BaseHandler;
 import io.scif.xml.XMLService;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.Vector;
 
 import loci.common.services.ServiceException;
 import loci.formats.meta.MetadataRetrieve;
 import loci.formats.ome.OMEXMLMetadata;
 import loci.formats.ome.OMEXMLMetadataImpl;
 import net.imglib2.meta.Axes;
 
 import org.scijava.Priority;
 import org.scijava.log.StderrLogService;
 import org.scijava.plugin.Parameter;
 import org.scijava.plugin.Plugin;
 import org.xml.sax.Attributes;
 import org.xml.sax.Locator;
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  * Format for OME-XML files.
  * 
  * @author Melissa Linkert melissa at glencoesoftware.com
  * @author Mark Hiner hinerm at gmail.com
  */
 @Plugin(type = Format.class)
 public class OMEXMLFormat extends AbstractFormat {
 
 	// -- Format API Methods --
 
 	@Override
 	public String getFormatName() {
 		return "OME-XML";
 	}
 
 	@Override
 	protected String[] makeSuffixArray() {
 		return new String[] { "ome" };
 	}
 
 	// -- Nested Classes --
 
 	/**
 	 * io.scif.Metadata class wrapping an OME-XML root.
 	 * 
 	 * @see OMEXMLMetadata
 	 * @see io.scif.Metadata
 	 * @author Mark Hiner
 	 */
 	public static class Metadata extends AbstractMetadata {
 
 		// -- Constants --
 
 		public static final String CNAME =
 			"io.scif.ome.xml.meta.OMEXMLFormat$Metadata";
 
 		// -- Fields --
 
 		/** OME core */
 		protected OMEMetadata omeMeta;
 
 		// compression value and offset for each BinData element
 		private Vector<BinData> binData;
 		private Vector<Long> binDataOffsets;
 		private Vector<String> compression;
 
 		private String omexml;
 		private boolean hasSPW = false;
 
 		// -- OMEXMLMetadata getters and setters --
 
 		public void setOMEMeta(final OMEMetadata ome) {
 			omeMeta = ome;
 		}
 
 		public OMEMetadata getOMEMeta() {
 			return omeMeta;
 		}
 
 		public Vector<BinData> getBinData() {
 			return binData;
 		}
 
 		public void setBinData(final Vector<BinData> binData) {
 			this.binData = binData;
 		}
 
 		public Vector<Long> getBinDataOffsets() {
 			return binDataOffsets;
 		}
 
 		public void setBinDataOffsets(final Vector<Long> binDataOffsets) {
 			this.binDataOffsets = binDataOffsets;
 		}
 
 		public Vector<String> getCompression() {
 			return compression;
 		}
 
 		public void setCompression(final Vector<String> compression) {
 			this.compression = compression;
 		}
 
 		public String getOmexml() {
 			return omexml;
 		}
 
 		public void setOmexml(final String omexml) {
 			this.omexml = omexml;
 		}
 
 		public boolean isSPW() {
 			return hasSPW;
 		}
 
 		public void setSPW(final boolean hasSPW) {
 			this.hasSPW = hasSPW;
 		}
 
 		// -- Metadata API Methods --
 
 		@Override
 		public void populateImageMetadata() {
 			getContext().getService(OMEXMLMetadataService.class).populateMetadata(
 				getOMEMeta().getRoot(), this);
 
 			for (int i = 0; i < getImageCount(); i++) {
 				get(i).setIndexed(false);
 				get(i).setFalseColor(true);
 				get(i).setPlanarAxisCount(2);
 			}
 		}
 
 		@Override
 		public void close(final boolean fileOnly) throws IOException {
 			super.close(fileOnly);
 			if (!fileOnly) {
 				compression = null;
 				binDataOffsets = null;
 				binData = null;
 				omexml = null;
 				hasSPW = false;
 			}
 		}
 	}
 
 	/**
 	 * @author Mark Hiner hinerm at gmail.com
 	 */
 	public static class Checker extends AbstractChecker {
 
 		// -- Checker API --
 
 		@Override
 		public boolean suffixNecessary() {
 			return false;
 		}
 
 		@Override
 		public boolean isFormat(final RandomAccessInputStream stream)
 			throws IOException
 		{
 			final int blockLen = 64;
 			final String xml = stream.readString(blockLen);
 			return xml.startsWith("<?xml") && xml.indexOf("<OME") >= 0;
 		}
 	}
 
 	/**
 	 * @author Mark Hiner hinerm at gmail.com
 	 */
 	public static class Parser extends AbstractParser<Metadata> {
 
 		// -- Fields --
 
 		@Parameter
 		private XMLService xmlService;
 
 		@Parameter
 		private FormatService formatService;
 
 		// -- Parser API Methods --
 
 		@Override
 		protected void typedParse(final RandomAccessInputStream stream,
 			final Metadata meta, final SCIFIOConfig config) throws IOException,
 			FormatException
 		{
 			final Vector<BinData> binData = new Vector<BinData>();
 			final Vector<Long> binDataOffsets = new Vector<Long>();
 			final Vector<String> compression = new Vector<String>();
 			meta.setBinData(binData);
 			meta.setBinDataOffsets(binDataOffsets);
 			meta.setCompression(compression);
 
 			final DefaultHandler handler = new OMEXMLHandler(meta);
 			try {
 				final RandomAccessInputStream s =
 					new RandomAccessInputStream(getContext(), stream.getFileName());
 				xmlService.parseXML(s, handler);
 				s.close();
 			}
 			catch (final IOException e) {
 				throw new FormatException("Malformed OME-XML", e);
 			}
 
 			int lineNumber = 1;
 			for (final BinData bin : binData) {
 				final int line = bin.getRow();
 				final int col = bin.getColumn();
 
 				while (lineNumber < line) {
 					getSource().readLine();
 					lineNumber++;
 				}
 				binDataOffsets.add(stream.getFilePointer() + col - 1);
 			}
 
 			if (binDataOffsets.size() == 0) {
 				throw new FormatException("Pixel data not found");
 			}
 
 			log().info("Populating metadata");
 
 			final OMEMetadata omeMeta = meta.getOMEMeta();
 			OMEXMLMetadata omexmlMeta = null;
 			if (omeMeta != null) omexmlMeta = meta.getOMEMeta().getRoot();
 			final OMEXMLService service =
 				formatService.getInstance(OMEXMLService.class);
 
 			try {
 
 				if (omexmlMeta == null) {
 					omexmlMeta = service.createOMEXMLMetadata(meta.getOmexml());
 					meta.setOMEMeta(new OMEMetadata(getContext(), omexmlMeta));
 				}
 
 				service.convertMetadata(meta.getOmexml(), omexmlMeta);
 
 			}
 			catch (final ServiceException se) {
 				throw new FormatException(se);
 			}
 
 			for (int i = 0; i < omexmlMeta.getImageCount(); i++)
 				omexmlMeta.setImageName(stream.getFileName(), i);
 
 			meta.setSPW(omexmlMeta.getPlateCount() > 0);
 			meta.getTable().put("Is SPW file", meta.isSPW());
 		}
 	}
 
 	/**
 	 * @author Mark Hiner hinerm at gmail.com
 	 */
 	public static class Reader extends ByteArrayReader<Metadata> {
 
 		// -- Reader API Methods --
 
 		@Override
 		protected String[] createDomainArray() {
 			return FormatTools.NON_GRAPHICS_DOMAINS;
 		}
 
 		@Override
 		public ByteArrayPlane openPlane(final int imageIndex,
 			final long planeIndex, final ByteArrayPlane plane, final long[] offsets,
 			final long[] lengths, final SCIFIOConfig config) throws FormatException,
 			IOException
 		{
 			final byte[] buf = plane.getBytes();
 			final Metadata meta = getMetadata();
 
 			FormatTools.checkPlaneForReading(meta, imageIndex, planeIndex,
 				buf.length, offsets, lengths);
 
 			int index = (int) planeIndex;
 
 			for (int i = 0; i < imageIndex; i++) {
 				index += meta.get(i).getPlaneCount();
 			}
 			if (index >= meta.getBinDataOffsets().size()) {
 				index = meta.getBinDataOffsets().size() - 1;
 			}
 
 			final long offset = meta.getBinDataOffsets().get(index).longValue();
 			final String compress = meta.getCompression().get(index);
 
 			getStream().seek(offset);
 
 			final int depth =
 				FormatTools.getBytesPerPixel(meta.get(imageIndex).getPixelType());
 			final int planeSize =
 				(int) (meta.get(imageIndex).getAxisLength(Axes.X) *
 					meta.get(imageIndex).getAxisLength(Axes.Y) * depth);
 
 			final CodecOptions options = new CodecOptions();
 			options.width = (int) meta.get(imageIndex).getAxisLength(Axes.X);
 			options.height = (int) meta.get(imageIndex).getAxisLength(Axes.Y);
 			options.bitsPerSample = depth * 8;
 			options.channels =
 				(int) (meta.get(imageIndex).isMultichannel() ? meta.get(imageIndex)
 					.getAxisLength(Axes.CHANNEL) : 1);
 			options.maxBytes = planeSize;
 			options.littleEndian = meta.get(imageIndex).isLittleEndian();
 			options.interleaved = meta.get(imageIndex).getInterleavedAxisCount() > 0;
 
 			byte[] pixels = new Base64Codec().decompress(getStream(), options);
 
 			// return a blank plane if no pixel data was stored
 			if (pixels.length == 0) {
 				log().debug("No pixel data for plane #" + planeIndex);
 				return plane;
 			}
 
 			// TODO: Create a method uncompress to handle all compression methods
 			if (compress.equals("bzip2")) {
 				byte[] tempPixels = pixels;
 				pixels = new byte[tempPixels.length - 2];
 				System.arraycopy(tempPixels, 2, pixels, 0, pixels.length);
 
 				ByteArrayInputStream bais = new ByteArrayInputStream(pixels);
 				CBZip2InputStream bzip =
 					new CBZip2InputStream(bais, new StderrLogService());
 				pixels = new byte[planeSize];
 				bzip.read(pixels, 0, pixels.length);
 				tempPixels = null;
 				bais.close();
 				bais = null;
 				bzip = null;
 			}
 			else if (compress.equals("zlib")) {
 				pixels = new ZlibCodec().decompress(pixels, options);
 			}
 			else if (compress.equals("J2K")) {
 				pixels = new JPEG2000Codec().decompress(pixels, options);
 			}
 			else if (compress.equals("JPEG")) {
 				pixels = new JPEGCodec().decompress(pixels, options);
 			}
 
 			final int xIndex = meta.get(imageIndex).getAxisIndex(Axes.X), yIndex =
 				meta.get(imageIndex).getAxisIndex(Axes.Y);
 			final int x = (int) offsets[xIndex], y = (int) offsets[yIndex], w =
 				(int) lengths[xIndex], h = (int) lengths[yIndex];
 			for (int row = 0; row < h; row++) {
 				final int off =
 					(int) ((row + y) * meta.get(imageIndex).getAxisLength(Axes.X) * depth + x *
 						depth);
 				System.arraycopy(pixels, off, buf, row * w * depth, w * depth);
 			}
 
 			pixels = null;
 
 			return plane;
 		}
 
 		@Override
 		public String[] getDomains() {
 			FormatTools.assertId(getStream(), true, 1);
 			return getMetadata().isSPW() ? new String[] { FormatTools.HCS_DOMAIN }
 				: FormatTools.NON_SPECIAL_DOMAINS;
 		}
 	}
 
 	/**
 	 * @author Mark Hiner hinerm at gmail.com
 	 */
 	public static class Writer extends AbstractWriter<Metadata> {
 
 		// -- Fields --
 
 		private Vector<String> xmlFragments;
 		private String currentFragment;
 
 		@Parameter
 		private XMLService xmlService;
 
 		@Parameter
 		private OMEXMLService omexmlService;
 
 		// -- Writer API Methods --
 
 		@Override
 		protected String[] makeCompressionTypes() {
 			return new String[] { CompressionType.UNCOMPRESSED.getCompression(),
 				CompressionType.ZLIB.getCompression() };
 		}
 
 		@Override
 		public void writePlane(final int imageIndex, final long planeIndex,
 			final Plane plane, final long[] offsets, final long[] lengths)
 			throws FormatException, IOException
 		{
 			final Metadata meta = getMetadata();
 			final byte[] buf = plane.getBytes();
 			final boolean interleaved =
 				meta.get(imageIndex).getInterleavedAxisCount() > 1;
 
 			checkParams(imageIndex, planeIndex, buf, offsets, lengths);
 			if (!SCIFIOMetadataTools.wholePlane(imageIndex, meta, offsets, lengths)) {
 				throw new FormatException(
 					"OMEXMLWriter does not yet support saving image tiles.");
 			}
 			final MetadataRetrieve retrieve = meta.getOMEMeta().getRoot();
 
 			if (planeIndex == 0) {
 				getStream().writeBytes(xmlFragments.get(imageIndex));
 			}
 
 			final String type = retrieve.getPixelsType(imageIndex).toString();
 			final int pixelType = FormatTools.pixelTypeFromString(type);
 			final int bytes = FormatTools.getBytesPerPixel(pixelType);
 			final int nChannels =
 				(int) (meta.get(imageIndex).isMultichannel() ? meta.get(imageIndex)
 					.getAxisLength(Axes.CHANNEL) : 1);
 			final int sizeX =
 				retrieve.getPixelsSizeX(imageIndex).getValue().intValue();
 			final int sizeY =
 				retrieve.getPixelsSizeY(imageIndex).getValue().intValue();
 			final int planeSize = sizeX * sizeY * bytes;
 			final boolean bigEndian =
 				retrieve.getPixelsBinDataBigEndian(imageIndex, 0);
 
 			final String namespace =
 				"xmlns=\"http://www.openmicroscopy.org/Schemas/BinaryFile/" +
 					omexmlService.getLatestVersion() + "\"";
 
 			for (int i = 0; i < nChannels; i++) {
 				final byte[] b =
 					ImageTools.splitChannels(buf, new long[] { i },
 						new long[] { nChannels }, bytes, false, interleaved);
 				final byte[] encodedPix = compress(b, imageIndex);
 
 				final StringBuffer omePlane = new StringBuffer("\n<BinData ");
 				omePlane.append(namespace);
 				omePlane.append(" Length=\"");
 				omePlane.append(planeSize);
 				omePlane.append("\"");
 				omePlane.append(" BigEndian=\"");
 				omePlane.append(bigEndian);
 				omePlane.append("\"");
 				if (getCompression() != null && !getCompression().equals("Uncompressed")) {
 					omePlane.append(" Compression=\"");
 					omePlane.append(getCompression());
 					omePlane.append("\"");
 				}
 				omePlane.append(">");
 				omePlane.append(new String(encodedPix, Constants.ENCODING));
 				omePlane.append("</BinData>");
 				getStream().writeBytes(omePlane.toString());
 			}
 		}
 
 		@Override
 		public void setMetadata(final Metadata meta) throws FormatException {
 			super.setMetadata(meta);
 			final MetadataRetrieve retrieve = meta.getOMEMeta().getRoot();
 
 			String xml;
 			try {
 				xml = omexmlService.getOMEXML(retrieve);
 				final OMEXMLMetadata noBin = omexmlService.createOMEXMLMetadata(xml);
 				omexmlService.removeBinData(noBin);
 				xml = omexmlService.getOMEXML(noBin);
 			}
 			catch (final ServiceException se) {
 				throw new FormatException(se);
 			}
 
 			final OMEHandler handler =
 				new OMEHandler(new Vector<String>(),
 					"<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
 
 			try {
 				xmlService.parseXML(xml, handler);
 			}
 			catch (final IOException e) {
 				throw new FormatException(e);
 			}
 
 			xmlFragments = handler.getFragments();
 			currentFragment = handler.getCurrentFragment();
 
 			xmlFragments.add(currentFragment);
 		}
 
 		@Override
 		public boolean canDoStacks() {
 			return true;
 		}
 
 		@Override
 		public int[] getPixelTypes(final String codec) {
 			if (codec != null && (codec.equals("J2K") || codec.equals("JPEG"))) {
 				return new int[] { FormatTools.INT8, FormatTools.UINT8 };
 			}
 			return super.getPixelTypes(codec);
 		}
 
 		@Override
 		public void close() throws IOException {
 			if (getStream() != null) {
 				getStream().writeBytes(xmlFragments.get(xmlFragments.size() - 1));
 			}
 			super.close();
 			xmlFragments = null;
 		}
 
 		// -- Helper methods --
 
 		/**
 		 * Compress the given byte array using the current codec. The compressed
 		 * data is then base64-encoded.
 		 */
 		private byte[] compress(byte[] b, final int imageIndex)
 			throws FormatException
 		{
 			final MetadataRetrieve r = getMetadata().getOMEMeta().getRoot();
 			final String type = r.getPixelsType(imageIndex).toString();
 			final int pixelType = FormatTools.pixelTypeFromString(type);
 			final int bytes = FormatTools.getBytesPerPixel(pixelType);
 
 			final CodecOptions options = new CodecOptions();
 			options.width = r.getPixelsSizeX(imageIndex).getValue().intValue();
 			options.height = r.getPixelsSizeY(imageIndex).getValue().intValue();
 			options.channels = 1;
 			options.interleaved = false;
 			options.signed = FormatTools.isSigned(pixelType);
 			options.littleEndian =
 				!r.getPixelsBinDataBigEndian(imageIndex, 0).booleanValue();
 			options.bitsPerSample = bytes * 8;
 
 			if (getCompression().equals("J2K")) {
 				b = new JPEG2000Codec().compress(b, options);
 			}
 			else if (getCompression().equals("JPEG")) {
 				b = new JPEGCodec().compress(b, options);
 			}
 			else if (getCompression().equals("zlib")) {
 				b = new ZlibCodec().compress(b, options);
 			}
 			return new Base64Codec().compress(b, options);
 		}
 	}
 
 	@Plugin(type = Translator.class)
 	public static class OMETranslator extends FromOMETranslator<Metadata> {
 
 		@Override
 		public Class<? extends io.scif.Metadata> source() {
 			return OMEMetadata.class;
 		}
 
 		@Override
 		public Class<? extends io.scif.Metadata> dest() {
 			return Metadata.class;
 		}
 
 		@Override
 		public void translateOMEXML(final OMEMetadata source, final Metadata dest) {
 			dest.setOMEMeta(source);
 		}
 	}
 
 	@Plugin(type = Translator.class, priority = Priority.LOW_PRIORITY)
 	public static class OMEXMLTranslator extends
 		AbstractTranslator<io.scif.Metadata, Metadata>
 	{
 
 		// -- Fields --
 
 		@Parameter
 		private OMEXMLMetadataService omexmlMetadataService;
 
 		// -- Translator API --
 
 		@Override
 		public Class<? extends io.scif.Metadata> source() {
 			return io.scif.Metadata.class;
 		}
 
 		@Override
 		public Class<? extends io.scif.Metadata> dest() {
 			return Metadata.class;
 		}
 
 		@Override
 		public void typedTranslate(final io.scif.Metadata source,
 			final Metadata dest)
 		{
 			final OMEXMLMetadata root = new OMEXMLMetadataImpl();
 			final OMEMetadata meta = new OMEMetadata(getContext(), root);
 			omexmlMetadataService.populatePixels(root, source);
 			dest.setOMEMeta(meta);
 		}
 	}
 
 	// -- Helper class --
 
 	private static class OMEHandler extends BaseHandler {
 
 		// -- Fields --
 
 		private final Vector<String> xmlFragments;
 		private String currentFragment;
 
 		@Parameter
 		private XMLService xmlService;
 
 		// -- Constructor --
 
 		public OMEHandler(final Vector<String> xmlFragments,
 			final String currentFragment)
 		{
 			super(new StderrLogService());
 			this.xmlFragments = xmlFragments;
 			this.currentFragment = currentFragment;
 		}
 
 		// -- OMEHandler API methods --
 
 		public Vector<String> getFragments() {
 			return xmlFragments;
 		}
 
 		public String getCurrentFragment() {
 			return currentFragment;
 		}
 
 		@Override
 		public void characters(final char[] ch, final int start, final int length) {
 			currentFragment += new String(ch, start, length);
 		}
 
 		@Override
 		public void startElement(final String uri, final String localName,
 			final String qName, final Attributes attributes)
 		{
 			final StringBuffer toAppend = new StringBuffer("\n<");
 			toAppend.append(xmlService.escapeXML(qName));
 			for (int i = 0; i < attributes.getLength(); i++) {
 				toAppend.append(" ");
 				toAppend.append(xmlService.escapeXML(attributes.getQName(i)));
 				toAppend.append("=\"");
 				toAppend.append(xmlService.escapeXML(attributes.getValue(i)));
 				toAppend.append("\"");
 			}
 			toAppend.append(">");
 			currentFragment += toAppend.toString();
 		}
 
 		@Override
 		public void endElement(final String uri, final String localName,
 			final String qName)
 		{
 			if (qName.equals("Pixels")) {
 				xmlFragments.add(currentFragment);
 				currentFragment = "";
 			}
 			currentFragment += "</" + qName + ">";
 		}
 
 	}
 
 	private static class OMEXMLHandler extends BaseHandler {
 
 		private final XMLService xmlService;
 
 		private final StringBuffer xmlBuffer;
 		private String currentQName;
 		private Locator locator;
 		private final Metadata meta;
 
 		public OMEXMLHandler(final Metadata meta) {
 			super(new StderrLogService());
 			xmlBuffer = new StringBuffer();
 			this.meta = meta;
 			xmlService = meta.getContext().getService(XMLService.class);
 		}
 
 		@Override
 		public void characters(final char[] ch, final int start, final int length) {
 			if (currentQName.indexOf("BinData") < 0) {
 				xmlBuffer.append(new String(ch, start, length));
 			}
 		}
 
 		@Override
 		public void endElement(final String uri, final String localName,
 			final String qName)
 		{
 			xmlBuffer.append("</");
 			xmlBuffer.append(qName);
 			xmlBuffer.append(">");
 		}
 
 		@Override
 		public void startElement(final String ur, final String localName,
 			final String qName, final Attributes attributes)
 		{
 			currentQName = qName;
 
 			if (qName.indexOf("BinData") == -1) {
 				xmlBuffer.append("<");
 				xmlBuffer.append(qName);
 				for (int i = 0; i < attributes.getLength(); i++) {
 					final String key = xmlService.escapeXML(attributes.getQName(i));
 					String value = xmlService.escapeXML(attributes.getValue(i));
 					if (key.equals("BigEndian")) {
 						String endian = value.toLowerCase();
 						if (!endian.equals("true") && !endian.equals("false")) {
 							// hack for files that specify 't' or 'f' instead of
 							// 'true' or 'false'
 							if (endian.startsWith("t")) endian = "true";
 							else if (endian.startsWith("f")) endian = "false";
 						}
 						value = endian;
 					}
 					xmlBuffer.append(" ");
 					xmlBuffer.append(key);
 					xmlBuffer.append("=\"");
 					xmlBuffer.append(value);
 					xmlBuffer.append("\"");
 				}
 				xmlBuffer.append(">");
 			}
 			else {
 				meta.getBinData().add(
 					new BinData(locator.getLineNumber(), locator.getColumnNumber()));
 				final String compress = attributes.getValue("Compression");
 				meta.getCompression().add(compress == null ? "" : compress);
 
 				xmlBuffer.append("<");
 				xmlBuffer.append(qName);
 				for (int i = 0; i < attributes.getLength(); i++) {
 					final String key = xmlService.escapeXML(attributes.getQName(i));
 					String value = xmlService.escapeXML(attributes.getValue(i));
 					if (key.equals("Length")) value = "0";
 					xmlBuffer.append(" ");
 					xmlBuffer.append(key);
 					xmlBuffer.append("=\"");
 					xmlBuffer.append(value);
 					xmlBuffer.append("\"");
 				}
 				xmlBuffer.append(">");
 			}
 		}
 
 		@Override
 		public void endDocument() {
 			meta.setOmexml(xmlBuffer.toString());
 		}
 
 		@Override
 		public void setDocumentLocator(final Locator locator) {
 			this.locator = locator;
 		}
 	}
 
 	private static class BinData {
 
 		private final int row;
 		private final int column;
 
 		public BinData(final int row, final int column) {
 			this.row = row;
 			this.column = column;
 		}
 
 		public int getRow() {
 			return row;
 		}
 
 		public int getColumn() {
 			return column;
 		}
 	}
 }
