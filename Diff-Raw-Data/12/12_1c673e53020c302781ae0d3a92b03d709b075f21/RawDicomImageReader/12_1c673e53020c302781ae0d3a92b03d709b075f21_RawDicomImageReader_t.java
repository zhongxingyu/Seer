 package de.sofd.viskit.test.windowing;
 
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.image.BandedSampleModel;
 import java.awt.image.BufferedImage;
 import java.awt.image.ColorModel;
 import java.awt.image.DataBuffer;
 import java.awt.image.DataBufferByte;
 import java.awt.image.DataBufferShort;
 import java.awt.image.DataBufferUShort;
 import java.awt.image.PixelInterleavedSampleModel;
 import java.awt.image.Raster;
 import java.awt.image.SampleModel;
 import java.awt.image.WritableRaster;
 import java.io.IOException;
 import java.nio.ByteOrder;
 import java.util.Arrays;
 import java.util.Hashtable;
 import java.util.Iterator;
 
 import javax.imageio.ImageIO;
 import javax.imageio.ImageReadParam;
 import javax.imageio.ImageReader;
 import javax.imageio.ImageTypeSpecifier;
 import javax.imageio.metadata.IIOMetadata;
 import javax.imageio.spi.ImageReaderSpi;
 import javax.imageio.stream.ImageInputStream;
 
 import org.dcm4che2.data.DicomObject;
 import org.dcm4che2.data.Tag;
 import org.dcm4che2.data.VR;
 import org.dcm4che2.image.ByteLookupTable;
 import org.dcm4che2.image.ColorModelFactory;
 import org.dcm4che2.image.LookupTable;
 import org.dcm4che2.image.OverlayUtils;
 import org.dcm4che2.image.PartialComponentSampleModel;
 import org.dcm4che2.image.VOIUtils;
 import org.dcm4che2.imageio.ImageReaderFactory;
 import org.dcm4che2.imageio.ItemParser;
 import org.dcm4che2.imageio.plugins.dcm.DicomImageReadParam;
 import org.dcm4che2.imageio.plugins.dcm.DicomStreamMetaData;
 import org.dcm4che2.io.DicomInputStream;
 import org.dcm4che2.io.StopTagInputHandler;
 import org.dcm4che2.util.ByteUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.sun.media.imageio.stream.RawImageInputStream;
 import com.sun.media.imageio.stream.SegmentedImageInputStream;
 import javax.imageio.spi.IIORegistry;
 
 /**
  * Copy & paste from org.dcm4che2.imageioimpl.plugins.dcm.DicomImageReader,
  * except for leaving out the windowing stuff from the read(int, ImageReadParam)
  * method.
  * <p>
  * DO NOT REFORMAT OR OTHERWISE "BEAUTIFY" THIS CODE in any way (doing so would
  * make it well-nigh impossible to merge future upstream changes of the
  * DicomImageReader class into thus class).
  * <p>
  * TODO: Move this class (and the accompanying Spi) to the de.sofd.util project
  * (as soon as that's been checked into the common repo).
  *
  * @author olaf
  */
 public class RawDicomImageReader extends ImageReader {
 
     private static final Logger log = LoggerFactory
             .getLogger(RawDicomImageReader.class);
 
     private static final String J2KIMAGE_READER = "com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageReader";
 
     private static final int[] OFFSETS_0 = { 0 };
 
     private static final int[] OFFSETS_0_0_0 = { 0, 0, 0 };
 
     private static final int[] OFFSETS_0_1_2 = { 0, 1, 2 };
 
     private ImageInputStream iis;
 
     private DicomInputStream dis;
 
     private DicomObject ds;
 
     private int width;
 
     private int height;
 
     private int frames;
 
     private int allocated;
 
     private int dataType;
 
     private int samples;
 
     private boolean monochrome;
 
     private boolean banded;
 
     private boolean bigEndian;
 
     private boolean swapByteOrder;
 
     private long pixelDataPos;
 
     private int pixelDataLen;
 
     protected boolean compressed;
 
     private DicomStreamMetaData streamMetaData;
 
     protected ImageReader reader;
 
     private ItemParser itemParser;
 
     private SegmentedImageInputStream siis;
 
     private String pmi;
 
     /**
      * Store the transfer syntax locally in case it gets modified to re-write
      * the image
      */
     protected String tsuid;
 
     protected RawDicomImageReader(ImageReaderSpi originatingProvider) {
         super(originatingProvider);
     }
 
     @Override
     public void setInput(Object input, boolean seekForwardOnly,
             boolean ignoreMetadata) {
         super.setInput(input, seekForwardOnly, ignoreMetadata);
 
         resetLocal();
 
         if (input != null) {
             if (!(input instanceof ImageInputStream)) {
                 throw new IllegalArgumentException(
                         "Input not an ImageInputStream!");
             }
             this.iis = (ImageInputStream) input;
         }
     }
 
     @Override
     public void dispose() {
         super.dispose();
         resetLocal();
     }
 
     @Override
     public void reset() {
         super.reset();
         resetLocal();
     }
 
     private void resetLocal() {
         iis = null;
         dis = null;
         ds = null;
         streamMetaData = null;
         width = 0;
         height = 0;
         frames = 0;
         allocated = 0;
         dataType = 0;
         samples = 0;
         banded = false;
         bigEndian = false;
         swapByteOrder = false;
         pixelDataPos = 0L;
         pixelDataLen = 0;
         tsuid = null;
         pmi = null;
         compressed = false;
         if (reader != null) {
             reader.dispose();
             reader = null;
         }
         itemParser = null;
         siis = null;
     }
 
     @Override
     public ImageReadParam getDefaultReadParam() {
         return new DicomImageReadParam();
     }
 
     /**
      * Return a DicomStreamMetaData object that includes the DICOM header.
      * <b>WARNING:</b> If this class is used to read directly from a cache or
      * other location that contains uncorrected data, the DICOM header will have
      * the uncorrected data as well. That is, assume the DB has some fixes to
      * patient demographics. These will not usually be applied to the DICOM
      * files directly, so you can get the wrong information from the header.
      * This is not an issue if you know the DICOM is up to date, or if you use
      * the DB information as authoritative.
      */
     @Override
     public IIOMetadata getStreamMetadata() throws IOException {
         readMetaData();
         return streamMetaData;
     }
 
     /**
      * Gets any image specific meta data. This should return the image specific
      * blocks for enhanced multi-frame, but currently it merely returns null.
      */
     @Override
     public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
         return null;
     }
 
     /**
      * Returns the number of regular images in the study. This excludes
      * overlays.
      */
     @Override
     public int getNumImages(boolean allowSearch) throws IOException {
         readMetaData();
         return frames;
     }
 
     /**
      * Reads the DICOM header meta-data, up to, but not including pixel data.
      *
      * @throws IOException
      */
     private void readMetaData() throws IOException {
         if (iis == null) {
             throw new IllegalStateException("Input not set!");
         }
         if (ds != null) {
             return;
         }
         dis = new DicomInputStream(iis);
         dis.setHandler(new StopTagInputHandler(Tag.PixelData));
         ds = dis.readDicomObject();
         streamMetaData = new DicomStreamMetaData();
         streamMetaData.setDicomObject(ds);
         bigEndian = dis.getTransferSyntax().bigEndian();
         tsuid = ds.getString(Tag.TransferSyntaxUID);
         width = ds.getInt(Tag.Columns);
         height = ds.getInt(Tag.Rows);
         frames = ds.getInt(Tag.NumberOfFrames);
         allocated = ds.getInt(Tag.BitsAllocated, 8);
         banded = ds.getInt(Tag.PlanarConfiguration) != 0;
         dataType = allocated <= 8 ? DataBuffer.TYPE_BYTE
                 : DataBuffer.TYPE_USHORT;
         samples = ds.getInt(Tag.SamplesPerPixel, 1);
         monochrome = ColorModelFactory.isMonochrome(ds);
         pmi = ds.getString(Tag.PhotometricInterpretation);
 
         if (dis.tag() == Tag.PixelData) {
             if (frames == 0)
                 frames = 1;
             swapByteOrder = bigEndian && dis.vr() == VR.OW
                     && dataType == DataBuffer.TYPE_BYTE;
             if (swapByteOrder && banded) {
                 throw new UnsupportedOperationException(
                         "Big Endian color-by-plane with Pixel Data VR=OW not implemented");
             }
             pixelDataPos = dis.getStreamPosition();
             pixelDataLen = dis.valueLength();
             compressed = pixelDataLen == -1;
             if (compressed) {
                 ImageReaderFactory f = ImageReaderFactory.getInstance();
                 log.debug("Transfer syntax for image is " + tsuid
                         + " with image reader class " + f.getClass());
                 f.adjustDatasetForTransferSyntax(ds, tsuid);
             }
         }
     }
 
     /**
      * Sets the input for the image reader.
      *
      * @param imageIndex
      *            The Dicom frame index, or overlay number
      * @throws IOException
      */
     protected void initImageReader(int imageIndex) throws IOException {
         readMetaData();
         if (reader == null) {
             if (compressed) {
                 initCompressedImageReader(imageIndex);
             } else {
                 initRawImageReader();
             }
         }
         // Reset the input stream location if required, and reset the reader if
         // required
         if (compressed) {
             itemParser.seekFrame(siis, imageIndex);
             reader.setInput(siis, false);
         }
     }
 
     private void initCompressedImageReader(int imageIndex) throws IOException {
         ImageReaderFactory f = ImageReaderFactory.getInstance();
         this.reader = f.getReaderForTransferSyntax(tsuid);
         this.itemParser = new ItemParser(dis, iis, frames, tsuid);
         this.siis = new SegmentedImageInputStream(iis, itemParser);
     }
 
     private void initRawImageReader() {
         long[] frameOffsets = new long[frames];
         int frameLen = width * height * samples * (allocated >> 3);
         frameOffsets[0] = pixelDataPos;
         for (int i = 1; i < frameOffsets.length; i++) {
             frameOffsets[i] = frameOffsets[i - 1] + frameLen;
         }
         Dimension[] imageDimensions = new Dimension[frames];
         Arrays.fill(imageDimensions, new Dimension(width, height));
         RawImageInputStream riis = new RawImageInputStream(iis,
                 createImageTypeSpecifier(), frameOffsets, imageDimensions);
         riis.setByteOrder(bigEndian ? ByteOrder.BIG_ENDIAN
                                     : ByteOrder.LITTLE_ENDIAN);
         reader = ImageIO.getImageReadersByFormatName("RAW").next();
         reader.setInput(riis);
     }
 
     /** Create an image type specifier for the entire image */
     protected ImageTypeSpecifier createImageTypeSpecifier() {
         ColorModel cm = ColorModelFactory.createColorModel(ds);
         SampleModel sm = createSampleModel();
         return new ImageTypeSpecifier(cm, sm);
     }
 
     private SampleModel createSampleModel() {
         if (samples == 1) {
             return new PixelInterleavedSampleModel(dataType, width, height, 1,
                     width, OFFSETS_0);
         }
 
         // samples == 3
         if (banded) {
             return new BandedSampleModel(dataType, width, height, width,
                     OFFSETS_0_1_2, OFFSETS_0_0_0);
         }
 
         if( (!compressed) && pmi.endsWith("422" ) ) {
             return new PartialComponentSampleModel(width, height, 2, 1);
         }
 
         if( (!compressed) && pmi.endsWith("420") ) {
             return new PartialComponentSampleModel(width,height,2,2);
         }
 
         return new PixelInterleavedSampleModel(dataType, width, height, 3,
                 width * 3, OFFSETS_0_1_2);
     }
 
     @Override
     public int getHeight(int imageIndex) throws IOException {
         readMetaData();
         if (OverlayUtils.isOverlay(imageIndex))
             return OverlayUtils.getOverlayHeight(ds, imageIndex);
         return height;
     }
 
     @Override
     public int getWidth(int imageIndex) throws IOException {
         readMetaData();
         if (OverlayUtils.isOverlay(imageIndex))
             return OverlayUtils.getOverlayWidth(ds, imageIndex);
         return width;
     }
 
     @Override
     public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex)
             throws IOException {
         // Index changes from 1 to 0 as the Dicom frames start to count at 1
         // ImageReader expects the first frame to be 0.
         initImageReader(0);
         return reader.getImageTypes(0);
     }
 
     @Override
     public boolean canReadRaster() {
         return true;
     }
 
     /**
      * Read the raw raster data from the image, without any LUTs being applied.
      * Cannot read overlay data, as it isn't clear what the raster format should
      * be for those.
      */
     @Override
     public Raster readRaster(int imageIndex, ImageReadParam param)
             throws IOException {
         initImageReader(imageIndex);
         if (param == null) {
             param = getDefaultReadParam();
         }
         if (compressed) {
             ImageReadParam param1 = reader.getDefaultReadParam();
             copyReadParam(param, param1);
             return decompressRaster(imageIndex, param1);
         }
         if( pmi.endsWith("422") || pmi.endsWith("420") ) {
             log.debug("Using a 422/420 partial component image reader.");
             if( param.getSourceXSubsampling()!=1
                     || param.getSourceYSubsampling()!=1
                     || param.getSourceRegion()!=null )
             {
                 log.warn("YBR_*_422 and 420 reader does not support source sub-sampling or source region.");
                 throw new UnsupportedOperationException("Implement sub-sampling/soure region.");
             }
             SampleModel sm = createSampleModel();
             WritableRaster wr = Raster.createWritableRaster(sm, new Point());
             DataBufferByte dbb = (DataBufferByte) wr.getDataBuffer();
             byte[] data = dbb.getData();
             log.debug("Seeking to "+(pixelDataPos + imageIndex * data.length)+" and reading "+data.length+" bytes.");
             iis.seek(pixelDataPos + imageIndex * data.length);
             iis.read(data);
             if (swapByteOrder) {
                 ByteUtils.toggleShortEndian(data);
             }
             return wr;
         }
         Raster raster = reader.readRaster(imageIndex, param);
         if (swapByteOrder) {
             ByteUtils.toggleShortEndian(
                     ((DataBufferByte)raster.getDataBuffer()).getData());
         }
         return raster;
     }
 
     /**
      * Reads the provided image as a buffered image. It is possible to read
      * image overlays by providing the 0x60000000 number associated with the
      * overlay. Otherwise, the imageIndex must be in the range
      * 0..numberOfFrames-1, or 0 for a single frame image. Overlays can be read
      * from PR objects or other types of objects in addition to image objects.
      * param can be used to sepecify GSPS to apply to the image, or to override
      * the default window level values, or to return the raw image.
      */
     @Override
     public BufferedImage read(int imageIndex, ImageReadParam param)
             throws IOException {
         if (OverlayUtils.isOverlay(imageIndex)) {
             readMetaData();
             String rgbs = (param != null) ? ((DicomImageReadParam) param)
                     .getOverlayRGB() : null;
             return OverlayUtils.extractOverlay(ds, imageIndex, this, rgbs);
         }
         initImageReader(imageIndex);
         if (param == null) {
             param = getDefaultReadParam();
         }
         BufferedImage bi;
         if (compressed) {
             ImageReadParam param1 = reader.getDefaultReadParam();
             copyReadParam(param, param1);
             bi = reader.read(0, param1);
             postDecompress();
         } else if( pmi.endsWith("422") || pmi.endsWith("420") ) {
             WritableRaster wr = (WritableRaster) readRaster(imageIndex, param);
             bi = new BufferedImage(ColorModelFactory.createColorModel(ds),
                     wr, false, null);
         } else {
             bi = reader.read(imageIndex, param);
             if (swapByteOrder) {
                 ByteUtils.toggleShortEndian(
                         ((DataBufferByte)bi.getRaster().getDataBuffer()).getData());
             }
         }
         /* leave out the windowing -- that's the sole reason this class exists
         if (monochrome) {
             WritableRaster raster = bi.getRaster();
             LookupTable lut = createLut((DicomImageReadParam) param,
                    imageIndex + 1, raster, bi.getSampleModel().getSampleSize(0));
             if (lut != null) {
                 WritableRaster dest = raster;
                 if (dest.getDataBuffer().getDataType() != DataBuffer.TYPE_BYTE
                         && (lut instanceof ByteLookupTable)) {
                     BufferedImage ret = new BufferedImage(bi.getWidth(), bi
                             .getHeight(), BufferedImage.TYPE_BYTE_GRAY);
                     dest = ret.getRaster();
                     bi = ret;
                 }
                 DataBuffer destData = dest.getDataBuffer();
                 lut.lookup(raster, dest);
                 if (destData.getDataType() == DataBuffer.TYPE_SHORT) {
                     ColorModel cm = bi.getColorModel();
                     short[] ss = ((DataBufferShort) destData).getData();
                     return new BufferedImage(cm, Raster.createWritableRaster(
                             raster.getSampleModel(), new DataBufferUShort(ss,
                                     ss.length), null),
                             cm.isAlphaPremultiplied(),
                             new Hashtable<Object,Object>());
                 }
             }
         }
         */
         return bi;
     }
 
     /**
      * Reads the bytes for the given image as raw data. Useful when copying the
      * image data unchanged to a new file/location etc, but some values are
      * being changed in the header, or some images are being excluded.
      */
     public byte[] readBytes(int imageIndex, ImageReadParam param)
             throws IOException {
         initImageReader(imageIndex);
         if (compressed) {
             return itemParser.readFrame(siis, imageIndex);
         }
 
         int frameLen = width * height * samples * (allocated >> 3);
         byte[] ret = new byte[frameLen];
         long offset = pixelDataPos + imageIndex * (long) frameLen;
         iis.seek(offset);
         iis.read(ret);
         return ret;
     }
 
     protected void copyReadParam(ImageReadParam src, ImageReadParam dst) {
         dst.setDestination(src.getDestination());
         dst.setSourceRegion(src.getSourceRegion());
         dst.setSourceSubsampling(src.getSourceXSubsampling(), src
                 .getSourceYSubsampling(), src.getSubsamplingXOffset(), src
                 .getSubsamplingYOffset());
         dst.setDestinationOffset(src.getDestinationOffset());
         if (ImageReaderFactory.getInstance().needsImageTypeSpecifier(tsuid)) {
             dst.setDestinationType(createImageTypeSpecifier());
         }
     }
 
     private Raster decompressRaster(int imageIndex, ImageReadParam param)
             throws IOException {
         if (!reader.canReadRaster()) {
             BufferedImage bi = reader.read(0, param);
             postDecompress();
             return bi.getRaster();
         }
         Raster raster = reader.readRaster(0, param);
         postDecompress();
         return raster;
     }
 
     protected void postDecompress() {
         // workaround for Bug in J2KImageReader and
         // J2KImageReaderCodecLib.setInput()
         if (reader.getClass().getName().startsWith(J2KIMAGE_READER)) {
             reader.dispose();
             ImageReaderFactory f = ImageReaderFactory.getInstance();
             reader = f.getReaderForTransferSyntax(tsuid);
         } else {
             reader.reset();
         }
     }
 
     /**
      * Return the complete lookup table to apply to the image data. This
      * comprises the Modality LUT, VOI LUT and Presentation LUT. The Modality
      * LUT can be represented as rescale slope/intercept, and the VOI LUT can be
      * represented as window width/center/type. Presentation LUT will come from
      * the specified values in the image read parameter, and is designed to turn
      * p-values into DDLs (digital driving levels).
      *
      * @param param
      * @param raster
      * @return Complete lookup table to apply to the image.
      */
     private LookupTable createLut(DicomImageReadParam param, int frame,
            Raster raster, int outBits) {
         short[] pval2gray = param.getPValue2Gray();
         DicomObject pr = param.getPresentationState();
         float c = param.getWindowCenter();
         float w = param.getWindowWidth();
         String vlutFct = param.getVoiLutFunction();
        /* // windowing disabled for this reader
         if (param.isAutoWindowing()) {
             DicomObject voiObj = VOIUtils.selectVoiObject(ds, pr, frame);
             if (voiObj == null) {
                 float[] cw = VOIUtils.getMinMaxWindowCenterWidth(ds, pr, frame,
                         raster);
                 c = cw[0];
                 w = cw[1];
                 vlutFct = LookupTable.LINEAR;
             }
         }
        */
 
         return LookupTable.createLutForImageWithPR(ds, pr, frame, c, w,
                vlutFct, outBits, pval2gray);   // outBits is fixed to 8 in the original dcm4che source -- will this work?
     }
 
 
 
 
     public static void registerWithImageIO() {
         IIORegistry.getDefaultInstance().registerServiceProvider(new RawDicomImageReaderSpi());
     }
 
 }
