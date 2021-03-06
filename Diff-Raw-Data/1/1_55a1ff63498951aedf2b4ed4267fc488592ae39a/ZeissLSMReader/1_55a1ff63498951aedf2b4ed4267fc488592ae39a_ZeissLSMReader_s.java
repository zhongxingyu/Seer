 //
 // ZeissLSMReader.java
 //
 
 /*
 OME Bio-Formats package for reading and converting biological file formats.
 Copyright (C) 2005-@year@ UW-Madison LOCI and Glencoe Software, Inc.
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
 package loci.formats.in;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import ome.xml.model.primitives.NonNegativeInteger;
 import ome.xml.model.primitives.PositiveInteger;
 
 import loci.common.DataTools;
 import loci.common.DateTools;
 import loci.common.Location;
 import loci.common.RandomAccessInputStream;
 import loci.common.Region;
 import loci.common.services.DependencyException;
 import loci.common.services.ServiceFactory;
 import loci.formats.CoreMetadata;
 import loci.formats.FormatException;
 import loci.formats.FormatReader;
 import loci.formats.FormatTools;
 import loci.formats.ImageTools;
 import loci.formats.MetadataTools;
 import loci.formats.meta.MetadataStore;
 import loci.formats.services.MDBService;
 import loci.formats.tiff.IFD;
 import loci.formats.tiff.IFDList;
 import loci.formats.tiff.PhotoInterp;
 import loci.formats.tiff.TiffCompression;
 import loci.formats.tiff.TiffConstants;
 import loci.formats.tiff.TiffParser;
 
 /**
  * ZeissLSMReader is the file format reader for Zeiss LSM files.
  *
  * <dl><dt><b>Source code:</b></dt>
  * <dd><a href="http://dev.loci.wisc.edu/trac/java/browser/trunk/components/bio-formats/src/loci/formats/in/ZeissLSMReader.java">Trac</a>,
  * <a href="http://dev.loci.wisc.edu/svn/java/trunk/components/bio-formats/src/loci/formats/in/ZeissLSMReader.java">SVN</a></dd></dl>
  *
  * @author Eric Kjellman egkjellman at wisc.edu
  * @author Melissa Linkert melissa at glencoesoftware.com
  * @author Curtis Rueden ctrueden at wisc.edu
  */
 public class ZeissLSMReader extends FormatReader {
 
   // -- Constants --
 
   public static final String[] MDB_SUFFIX = {"mdb"};
 
   /** Tag identifying a Zeiss LSM file. */
   private static final int ZEISS_ID = 34412;
 
   /** Data types. */
   private static final int TYPE_SUBBLOCK = 0;
   private static final int TYPE_ASCII = 2;
   private static final int TYPE_LONG = 4;
   private static final int TYPE_RATIONAL = 5;
 
   /** Subblock types. */
   private static final int SUBBLOCK_RECORDING = 0x10000000;
   private static final int SUBBLOCK_LASER = 0x50000000;
   private static final int SUBBLOCK_TRACK = 0x40000000;
   private static final int SUBBLOCK_DETECTION_CHANNEL = 0x70000000;
   private static final int SUBBLOCK_ILLUMINATION_CHANNEL = 0x90000000;
   private static final int SUBBLOCK_BEAM_SPLITTER = 0xb0000000;
   private static final int SUBBLOCK_DATA_CHANNEL = 0xd0000000;
   private static final int SUBBLOCK_TIMER = 0x12000000;
   private static final int SUBBLOCK_MARKER = 0x14000000;
   private static final int SUBBLOCK_END = (int) 0xffffffff;
 
   /** Data types. */
   private static final int RECORDING_NAME = 0x10000001;
   private static final int RECORDING_DESCRIPTION = 0x10000002;
   private static final int RECORDING_OBJECTIVE = 0x10000004;
   private static final int RECORDING_ZOOM = 0x10000016;
   private static final int RECORDING_SAMPLE_0TIME = 0x10000036;
   private static final int RECORDING_CAMERA_BINNING = 0x10000052;
 
   private static final int TRACK_ACQUIRE = 0x40000006;
   private static final int TRACK_TIME_BETWEEN_STACKS = 0x4000000b;
 
   private static final int LASER_NAME = 0x50000001;
   private static final int LASER_ACQUIRE = 0x50000002;
   private static final int LASER_POWER = 0x50000003;
 
   private static final int CHANNEL_DETECTOR_GAIN = 0x70000003;
   private static final int CHANNEL_PINHOLE_DIAMETER = 0x70000009;
   private static final int CHANNEL_AMPLIFIER_GAIN = 0x70000005;
   private static final int CHANNEL_FILTER_SET = 0x7000000f;
   private static final int CHANNEL_FILTER = 0x70000010;
   private static final int CHANNEL_ACQUIRE = 0x7000000b;
   private static final int CHANNEL_NAME = 0x70000014;
 
   private static final int ILLUM_CHANNEL_NAME = 0x90000001;
   private static final int ILLUM_CHANNEL_ATTENUATION = 0x90000002;
   private static final int ILLUM_CHANNEL_WAVELENGTH = 0x90000003;
   private static final int ILLUM_CHANNEL_ACQUIRE = 0x90000004;
 
   private static final int START_TIME = 0x10000036;
   private static final int DATA_CHANNEL_NAME = 0xd0000001;
   private static final int DATA_CHANNEL_ACQUIRE = 0xd0000017;
 
   private static final int BEAM_SPLITTER_FILTER = 0xb0000002;
   private static final int BEAM_SPLITTER_FILTER_SET = 0xb0000003;
 
   /** Drawing element types. */
   private static final int TEXT = 13;
   private static final int LINE = 14;
   private static final int SCALE_BAR = 15;
   private static final int OPEN_ARROW = 16;
   private static final int CLOSED_ARROW = 17;
   private static final int RECTANGLE = 18;
   private static final int ELLIPSE = 19;
   private static final int CLOSED_POLYLINE = 20;
   private static final int OPEN_POLYLINE = 21;
   private static final int CLOSED_BEZIER = 22;
   private static final int OPEN_BEZIER = 23;
   private static final int CIRCLE = 24;
   private static final int PALETTE = 25;
   private static final int POLYLINE_ARROW = 26;
   private static final int BEZIER_WITH_ARROW = 27;
   private static final int ANGLE = 28;
   private static final int CIRCLE_3POINT = 29;
 
   // -- Static fields --
 
   private static final Hashtable<Integer, String> METADATA_KEYS = createKeys();
 
   // -- Fields --
 
   private double pixelSizeX, pixelSizeY, pixelSizeZ;
   private byte[][][] lut = null;
   private Vector<Double> timestamps;
   private int validChannels;
 
   private String[] lsmFilenames;
   private Vector<IFDList> ifdsList;
   private TiffParser tiffParser;
 
   private int nextLaser = 0, nextDetector = 0;
   private int nextFilter = 0, nextDichroicChannel = 0, nextDichroic = 0;
   private int nextDataChannel = 0, nextIllumChannel = 0, nextDetectChannel = 0;
   private boolean splitPlanes = false;
   private double zoom;
   private Vector<String> imageNames;
   private String binning;
   private Vector<Double> xCoordinates, yCoordinates, zCoordinates;
   private int dimensionM, dimensionP;
   private Hashtable<String, Integer> seriesCounts;
   private String userName;
 
   private double originX, originY, originZ;
 
   private int totalROIs = 0;
 
   private int prevPlane = -1;
   private int prevChannel = 0;
   private byte[] prevBuf = null;
   private Region prevRegion = null;
 
   // -- Constructor --
 
   /** Constructs a new Zeiss LSM reader. */
   public ZeissLSMReader() {
     super("Zeiss Laser-Scanning Microscopy", new String[] {"lsm", "mdb"});
     domains = new String[] {FormatTools.LM_DOMAIN};
     hasCompanionFiles = true;
   }
 
   // -- IFormatReader API methods --
 
   /* @see loci.formats.IFormatReader#isSingleFile(String) */
   public boolean isSingleFile(String id) throws FormatException, IOException {
     if (checkSuffix(id, MDB_SUFFIX)) return false;
     return isGroupFiles() ? getMDBFile(id) != null : true;
   }
 
   /* @see loci.formats.IFormatReader#close(boolean) */
   public void close(boolean fileOnly) throws IOException {
     super.close(fileOnly);
     if (!fileOnly) {
       pixelSizeX = pixelSizeY = pixelSizeZ = 0;
       lut = null;
       timestamps = null;
       validChannels = 0;
       lsmFilenames = null;
       ifdsList = null;
       tiffParser = null;
       nextLaser = nextDetector = 0;
       nextFilter = nextDichroicChannel = nextDichroic = 0;
       nextDataChannel = nextIllumChannel = nextDetectChannel = 0;
       splitPlanes = false;
       zoom = 0;
       imageNames = null;
       binning = null;
       totalROIs = 0;
       prevPlane = -1;
       prevChannel = 0;
       prevBuf = null;
       prevRegion = null;
       xCoordinates = null;
       yCoordinates = null;
       zCoordinates = null;
       dimensionM = 0;
       dimensionP = 0;
       seriesCounts = null;
       originX = originY = originZ = 0d;
       userName = null;
     }
   }
 
   /* @see loci.formats.IFormatReader#isThisType(RandomAccessInputStream) */
   public boolean isThisType(RandomAccessInputStream stream) throws IOException {
     final int blockLen = 4;
     if (!FormatTools.validStream(stream, blockLen, false)) return false;
     TiffParser parser = new TiffParser(stream);
     return parser.isValidHeader() || stream.readShort() == 0x5374;
   }
 
   /* @see loci.formats.IFormatReader#fileGroupOption(String) */
   public int fileGroupOption(String id) throws FormatException, IOException {
     return FormatTools.MUST_GROUP;
   }
 
   /* @see loci.formats.IFormatReader#getSeriesUsedFiles(boolean) */
   public String[] getSeriesUsedFiles(boolean noPixels) {
     FormatTools.assertId(currentId, true, 1);
     if (noPixels) {
       if (checkSuffix(currentId, MDB_SUFFIX)) return new String[] {currentId};
       return null;
     }
     if (lsmFilenames == null) return new String[] {currentId};
     if (lsmFilenames.length == 1 && currentId.equals(lsmFilenames[0])) {
       return lsmFilenames;
     }
     return new String[] {currentId, getLSMFileFromSeries(getSeries())};
   }
 
   /* @see loci.formats.IFormatReader#get8BitLookupTable() */
   public byte[][] get8BitLookupTable() throws FormatException, IOException {
     FormatTools.assertId(currentId, true, 1);
     if (lut == null || lut[getSeries()]  == null ||
       getPixelType() != FormatTools.UINT8)
     {
       return null;
     }
 
     byte[][] b = new byte[3][];
     b[0] = lut[getSeries()][prevChannel * 3];
     b[1] = lut[getSeries()][prevChannel * 3 + 1];
     b[2] = lut[getSeries()][prevChannel * 3 + 2];
 
     return b;
   }
 
   /* @see loci.formats.IFormatReader#get16BitLookupTable() */
   public short[][] get16BitLookupTable() throws FormatException, IOException {
     FormatTools.assertId(currentId, true, 1);
     if (lut == null || lut[getSeries()] == null ||
       getPixelType() != FormatTools.UINT16)
     {
       return null;
     }
     short[][] s = new short[3][65536];
     for (int i=2; i>=3-validChannels; i--) {
       for (int j=0; j<s[i].length; j++) {
         s[i][j] = (short) j;
       }
     }
     return s;
   }
 
   /* @see loci.formats.IFormatReader#setSeries(int) */
   public void setSeries(int series) {
     if (series != getSeries()) {
       prevBuf = null;
     }
     super.setSeries(series);
   }
 
   /**
    * @see loci.formats.IFormatReader#openBytes(int, byte[], int, int, int, int)
    */
   public byte[] openBytes(int no, byte[] buf, int x, int y, int w, int h)
     throws FormatException, IOException
   {
     FormatTools.checkPlaneParameters(this, no, buf.length, x, y, w, h);
 
     if (getSeriesCount() > 1) {
       in.close();
       in = new RandomAccessInputStream(getLSMFileFromSeries(getSeries()));
       in.order(!isLittleEndian());
       tiffParser = new TiffParser(in);
     }
 
     IFDList ifds = ifdsList.get(getSeries());
 
     if (splitPlanes && getSizeC() > 1 && ifds.size() == getSizeZ() * getSizeT())
     {
       int bpp = FormatTools.getBytesPerPixel(getPixelType());
       int plane = no / getSizeC();
       int c = no % getSizeC();
       Region region = new Region(x, y, w, h);
 
       if (prevPlane != plane || prevBuf == null ||
         prevBuf.length < w * h * bpp * getSizeC() || !region.equals(prevRegion))
       {
         prevBuf = new byte[w * h * bpp * getSizeC()];
         tiffParser.getSamples(ifds.get(plane), prevBuf, x, y, w, h);
         prevPlane = plane;
         prevRegion = region;
       }
       ImageTools.splitChannels(
         prevBuf, buf, c, getSizeC(), bpp, false, false, w * h * bpp);
       prevChannel = c;
     }
     else {
       tiffParser.getSamples(ifds.get(no), buf, x, y, w, h);
       prevChannel = getZCTCoords(no)[1];
     }
     if (getSeriesCount() > 1) in.close();
     return buf;
   }
 
   // -- Internal FormatReader API methods --
 
   /* @see loci.formats.FormatReader#initFile(String) */
   protected void initFile(String id) throws FormatException, IOException {
     super.initFile(id);
 
     if (!checkSuffix(id, MDB_SUFFIX) && isGroupFiles()) {
       String mdb = getMDBFile(id);
       if (mdb != null) {
         setId(mdb);
         return;
       }
       lsmFilenames = new String[] {id};
     }
     else if (checkSuffix(id, MDB_SUFFIX)) {
       lsmFilenames = parseMDB(id);
     }
     else lsmFilenames = new String[] {id};
 
     if (lsmFilenames == null || lsmFilenames.length == 0) {
       throw new FormatException("LSM files were not found.");
     }
 
     timestamps = new Vector<Double>();
     imageNames = new Vector<String>();
     xCoordinates = new Vector<Double>();
     yCoordinates = new Vector<Double>();
     zCoordinates = new Vector<Double>();
     seriesCounts = new Hashtable<String, Integer>();
 
     int seriesCount = 0;
 
     Vector<String> validFiles = new Vector<String>();
     for (String filename : lsmFilenames) {
       try {
         int extraSeries = getExtraSeries(filename);
         seriesCounts.put(filename, extraSeries);
         seriesCount += extraSeries;
         validFiles.add(filename);
       }
       catch (IOException e) {
         LOGGER.debug("Failed to parse " + filename, e);
       }
     }
     lsmFilenames = validFiles.toArray(new String[validFiles.size()]);
 
     core = new CoreMetadata[seriesCount];
     ifdsList = new Vector<IFDList>();
     ifdsList.setSize(core.length);
 
     int realSeries = 0;
     for (int i=0; i<lsmFilenames.length; i++) {
       RandomAccessInputStream stream =
         new RandomAccessInputStream(lsmFilenames[i]);
       int count = seriesCounts.get(lsmFilenames[i]);
 
       TiffParser tp = new TiffParser(stream);
       Boolean littleEndian = tp.checkHeader();
       long[] ifdOffsets = tp.getIFDOffsets();
       int ifdsPerSeries = (ifdOffsets.length / 2) / count;
 
       int offset = 0;
       Object zeissTag = null;
       for (int s=0; s<count; s++, realSeries++) {
         core[realSeries] = new CoreMetadata();
         core[realSeries].littleEndian = littleEndian;
 
         IFDList ifds = new IFDList();
         while (ifds.size() < ifdsPerSeries) {
           tp.setDoCaching(offset == 0);
           IFD ifd = tp.getIFD(ifdOffsets[offset]);
           if (offset == 0) zeissTag = ifd.get(ZEISS_ID);
           if (offset > 0 && ifds.size() == 0) {
             ifd.putIFDValue(ZEISS_ID, zeissTag);
           }
           ifds.add(ifd);
           if (zeissTag != null) offset += 2;
           else offset++;
         }
 
         for (IFD ifd : ifds) {
           tp.fillInIFD(ifd);
         }
 
         ifdsList.set(realSeries, ifds);
       }
       stream.close();
     }
 
     MetadataStore store = makeFilterMetadata();
 
     lut = new byte[ifdsList.size()][][];
 
     for (int series=0; series<ifdsList.size(); series++) {
       IFDList ifds = ifdsList.get(series);
       for (IFD ifd : ifds) {
         // check that predictor is set to 1 if anything other
         // than LZW compression is used
         if (ifd.getCompression() != TiffCompression.LZW) {
           ifd.putIFDValue(IFD.PREDICTOR, 1);
         }
       }
 
       // fix the offsets for > 4 GB files
       RandomAccessInputStream s =
         new RandomAccessInputStream(getLSMFileFromSeries(series));
       for (int i=1; i<ifds.size(); i++) {
         long[] stripOffsets = ifds.get(i).getStripOffsets();
         long[] previousStripOffsets = ifds.get(i - 1).getStripOffsets();
 
         if (stripOffsets == null || previousStripOffsets == null) {
           throw new FormatException(
             "Strip offsets are missing; this is an invalid file.");
         }
 
         boolean neededAdjustment = false;
         for (int j=0; j<stripOffsets.length; j++) {
           if (j >= previousStripOffsets.length) break;
           if (stripOffsets[j] < previousStripOffsets[j]) {
             stripOffsets[j] = (previousStripOffsets[j] & ~0xffffffffL) |
               (stripOffsets[j] & 0xffffffffL);
             if (stripOffsets[j] < previousStripOffsets[j]) {
               stripOffsets[j] += 0x100000000L;
             }
             neededAdjustment = true;
           }
           if (neededAdjustment) {
             ifds.get(i).putIFDValue(IFD.STRIP_OFFSETS, stripOffsets);
           }
         }
       }
       s.close();
 
       initMetadata(series);
     }
 
     for (int i=0; i<getSeriesCount(); i++) {
       core[i].imageCount = core[i].sizeZ * core[i].sizeC * core[i].sizeT;
     }
 
     MetadataTools.populatePixels(store, this, true);
     for (int series=0; series<ifdsList.size(); series++) {
       setSeries(series);
       if (series < imageNames.size()) {
         store.setImageName(imageNames.get(series), series);
       }
       store.setPixelsBinDataBigEndian(!isLittleEndian(), series, 0);
     }
     setSeries(0);
   }
 
   // -- Helper methods --
 
   private String getMDBFile(String id) throws FormatException, IOException {
     Location parentFile = new Location(id).getAbsoluteFile().getParentFile();
     String[] fileList = parentFile.list();
     for (int i=0; i<fileList.length; i++) {
       if (fileList[i].startsWith(".")) continue;
       if (checkSuffix(fileList[i], MDB_SUFFIX)) {
         Location file =
           new Location(parentFile, fileList[i]).getAbsoluteFile();
         if (file.isDirectory()) continue;
         // make sure that the .mdb references this .lsm
         String[] lsms = parseMDB(file.getAbsolutePath());
         if (lsms == null) return null;
         for (String lsm : lsms) {
           if (id.endsWith(lsm) || lsm.endsWith(id)) {
             return file.getAbsolutePath();
           }
         }
       }
     }
     return null;
   }
 
   private int getEffectiveSeries(int currentSeries) {
     int seriesCount = 0;
     for (int i=0; i<lsmFilenames.length; i++) {
       Integer count = seriesCounts.get(lsmFilenames[i]);
       if (count == null) count = 1;
       seriesCount += count;
       if (seriesCount > currentSeries) return i;
     }
     return -1;
   }
 
   private String getLSMFileFromSeries(int currentSeries) {
     int effectiveSeries = getEffectiveSeries(currentSeries);
     return effectiveSeries < 0 ? null : lsmFilenames[effectiveSeries];
   }
 
   private int getExtraSeries(String file) throws FormatException, IOException {
     if (in != null) in.close();
     in = new RandomAccessInputStream(file);
     boolean littleEndian = in.read() == TiffConstants.LITTLE;
     in.order(littleEndian);
 
     tiffParser = new TiffParser(in);
     IFD ifd = tiffParser.getFirstIFD();
     RandomAccessInputStream ras = getCZTag(ifd);
     if (ras == null) return 1;
     ras.order(littleEndian);
 
     ras.seek(264);
     dimensionP = ras.readInt();
     dimensionM = ras.readInt();
     ras.close();
 
     int nSeries = dimensionM * dimensionP;
     return nSeries <= 0 ? 1 : nSeries;
   }
 
   private int getPosition(int currentSeries) {
     int effectiveSeries = getEffectiveSeries(currentSeries);
     int firstPosition = 0;
     for (int i=0; i<effectiveSeries; i++) {
       firstPosition += seriesCounts.get(lsmFilenames[i]);
     }
     return currentSeries - firstPosition;
   }
 
   private RandomAccessInputStream getCZTag(IFD ifd)
     throws FormatException, IOException
   {
     // get TIF_CZ_LSMINFO structure
     short[] s = ifd.getIFDShortArray(ZEISS_ID);
     if (s == null) {
       LOGGER.warn("Invalid Zeiss LSM file. Tag {} not found.", ZEISS_ID);
       TiffReader reader = new TiffReader();
       reader.setId(getLSMFileFromSeries(series));
       core[getSeries()] = reader.getCoreMetadata()[0];
       reader.close();
       return null;
     }
     byte[] cz = new byte[s.length];
     for (int i=0; i<s.length; i++) {
       cz[i] = (byte) s[i];
     }
 
     RandomAccessInputStream ras = new RandomAccessInputStream(cz);
     ras.order(isLittleEndian());
     return ras;
   }
 
   protected void initMetadata(int series) throws FormatException, IOException {
     setSeries(series);
     IFDList ifds = ifdsList.get(series);
     IFD ifd = ifds.get(0);
 
     in.close();
     in = new RandomAccessInputStream(getLSMFileFromSeries(series));
     in.order(isLittleEndian());
 
     tiffParser = new TiffParser(in);
 
     PhotoInterp photo = ifd.getPhotometricInterpretation();
     int samples = ifd.getSamplesPerPixel();
 
     core[series].sizeX = (int) ifd.getImageWidth();
     core[series].sizeY = (int) ifd.getImageLength();
     core[series].rgb = samples > 1 || photo == PhotoInterp.RGB;
     core[series].interleaved = false;
     core[series].sizeC = isRGB() ? samples : 1;
     core[series].pixelType = ifd.getPixelType();
     core[series].imageCount = ifds.size();
     core[series].sizeZ = getImageCount();
     core[series].sizeT = 1;
 
     LOGGER.info("Reading LSM metadata for series #{}", series);
 
     MetadataStore store = makeFilterMetadata();
 
     int instrument = getEffectiveSeries(series);
 
     String imageName = getLSMFileFromSeries(series);
     if (imageName.indexOf(".") != -1) {
       imageName = imageName.substring(0, imageName.lastIndexOf("."));
     }
     if (imageName.indexOf(File.separator) != -1) {
       imageName =
         imageName.substring(imageName.lastIndexOf(File.separator) + 1);
     }
     if (lsmFilenames.length != getSeriesCount()) {
       imageName += " #" + (getPosition(series) + 1);
     }
 
     // link Instrument and Image
     store.setImageID(MetadataTools.createLSID("Image", series), series);
     String instrumentID = MetadataTools.createLSID("Instrument", instrument);
     store.setInstrumentID(instrumentID, instrument);
     store.setImageInstrumentRef(instrumentID, series);
 
     RandomAccessInputStream ras = getCZTag(ifd);
     if (ras == null) {
       imageNames.add(imageName);
       return;
     }
 
     ras.seek(16);
 
     core[series].sizeZ = ras.readInt();
     ras.skipBytes(4);
     core[series].sizeT = ras.readInt();
 
     int dataType = ras.readInt();
     switch (dataType) {
       case 2:
         addSeriesMeta("DataType", "12 bit unsigned integer");
         break;
       case 5:
         addSeriesMeta("DataType", "32 bit float");
         break;
       case 0:
         addSeriesMeta("DataType", "varying data types");
         break;
       default:
         addSeriesMeta("DataType", "8 bit unsigned integer");
     }
 
     if (getMetadataOptions().getMetadataLevel() != MetadataLevel.MINIMUM) {
       ras.seek(0);
       addSeriesMeta("MagicNumber ", ras.readInt());
       addSeriesMeta("StructureSize", ras.readInt());
       addSeriesMeta("DimensionX", ras.readInt());
       addSeriesMeta("DimensionY", ras.readInt());
 
       ras.seek(32);
       addSeriesMeta("ThumbnailX", ras.readInt());
       addSeriesMeta("ThumbnailY", ras.readInt());
 
       // pixel sizes are stored in meters, we need them in microns
       pixelSizeX = ras.readDouble() * 1000000;
       pixelSizeY = ras.readDouble() * 1000000;
       pixelSizeZ = ras.readDouble() * 1000000;
 
       addSeriesMeta("VoxelSizeX", new Double(pixelSizeX));
       addSeriesMeta("VoxelSizeY", new Double(pixelSizeY));
       addSeriesMeta("VoxelSizeZ", new Double(pixelSizeZ));
 
       originX = ras.readDouble() * 1000000;
       originY = ras.readDouble() * 1000000;
       originZ = ras.readDouble() * 1000000;
 
       addSeriesMeta("OriginX", originX);
       addSeriesMeta("OriginY", originY);
       addSeriesMeta("OriginZ", originZ);
     }
     else ras.seek(88);
 
     int scanType = ras.readShort();
     switch (scanType) {
       case 0:
         addSeriesMeta("ScanType", "x-y-z scan");
         core[series].dimensionOrder = "XYZCT";
         break;
       case 1:
         addSeriesMeta("ScanType", "z scan (x-z plane)");
         core[series].dimensionOrder = "XYZCT";
         break;
       case 2:
         addSeriesMeta("ScanType", "line scan");
         core[series].dimensionOrder = "XYZCT";
         break;
       case 3:
         addSeriesMeta("ScanType", "time series x-y");
         core[series].dimensionOrder = "XYTCZ";
         break;
       case 4:
         addSeriesMeta("ScanType", "time series x-z");
         core[series].dimensionOrder = "XYZTC";
         break;
       case 5:
         addSeriesMeta("ScanType", "time series 'Mean of ROIs'");
         core[series].dimensionOrder = "XYTCZ";
         break;
       case 6:
         addSeriesMeta("ScanType", "time series x-y-z");
         core[series].dimensionOrder = "XYZTC";
         break;
       case 7:
         addSeriesMeta("ScanType", "spline scan");
         core[series].dimensionOrder = "XYCTZ";
         break;
       case 8:
         addSeriesMeta("ScanType", "spline scan x-z");
         core[series].dimensionOrder = "XYCZT";
         break;
       case 9:
         addSeriesMeta("ScanType", "time series spline plane x-z");
         core[series].dimensionOrder = "XYTCZ";
         break;
       case 10:
         addSeriesMeta("ScanType", "point mode");
         core[series].dimensionOrder = "XYZCT";
         break;
       default:
         addSeriesMeta("ScanType", "x-y-z scan");
         core[series].dimensionOrder = "XYZCT";
     }
 
     core[series].indexed = lut != null && lut[series] != null;
     if (isIndexed()) {
       core[series].rgb = false;
     }
     if (getSizeC() == 0) core[series].sizeC = 1;
 
     if (isRGB()) {
       // shuffle C to front of order string
       core[series].dimensionOrder = getDimensionOrder().replaceAll("C", "");
       core[series].dimensionOrder = getDimensionOrder().replaceAll("XY", "XYC");
     }
 
     if (getEffectiveSizeC() == 0) {
       core[series].imageCount = getSizeZ() * getSizeT();
     }
     else {
       core[series].imageCount = getSizeZ() * getSizeT() * getEffectiveSizeC();
     }
 
     if (getImageCount() != ifds.size()) {
       int diff = getImageCount() - ifds.size();
       core[series].imageCount = ifds.size();
       if (diff % getSizeZ() == 0) {
         core[series].sizeT -= (diff / getSizeZ());
       }
       else if (diff % getSizeT() == 0) {
         core[series].sizeZ -= (diff / getSizeT());
       }
       else if (getSizeZ() > 1) {
         core[series].sizeZ = ifds.size();
         core[series].sizeT = 1;
       }
       else if (getSizeT() > 1) {
         core[series].sizeT = ifds.size();
         core[series].sizeZ = 1;
       }
     }
 
     if (getSizeZ() == 0) core[series].sizeZ = getImageCount();
     if (getSizeT() == 0) core[series].sizeT = getImageCount() / getSizeZ();
 
     long channelColorsOffset = 0;
     long timeStampOffset = 0;
     long eventListOffset = 0;
     long scanInformationOffset = 0;
     long channelWavelengthOffset = 0;
 
     if (getMetadataOptions().getMetadataLevel() != MetadataLevel.MINIMUM) {
       int spectralScan = ras.readShort();
       if (spectralScan != 1) {
         addSeriesMeta("SpectralScan", "no spectral scan");
       }
       else addSeriesMeta("SpectralScan", "acquired with spectral scan");
 
       int type = ras.readInt();
       switch (type) {
         case 1:
           addSeriesMeta("DataType2", "calculated data");
           break;
         case 2:
           addSeriesMeta("DataType2", "animation");
           break;
         default:
           addSeriesMeta("DataType2", "original scan data");
       }
 
       long[] overlayOffsets = new long[9];
       String[] overlayKeys = new String[] {"VectorOverlay", "InputLut",
         "OutputLut", "ROI", "BleachROI", "MeanOfRoisOverlay",
         "TopoIsolineOverlay", "TopoProfileOverlay", "LinescanOverlay"};
 
       overlayOffsets[0] = ras.readInt();
       overlayOffsets[1] = ras.readInt();
       overlayOffsets[2] = ras.readInt();
 
       channelColorsOffset = ras.readInt();
 
       addSeriesMeta("TimeInterval", ras.readDouble());
       ras.skipBytes(4);
       scanInformationOffset = ras.readInt();
       ras.skipBytes(4);
       timeStampOffset = ras.readInt();
       eventListOffset = ras.readInt();
       overlayOffsets[3] = ras.readInt();
       overlayOffsets[4] = ras.readInt();
       ras.skipBytes(4);
 
       addSeriesMeta("DisplayAspectX", ras.readDouble());
       addSeriesMeta("DisplayAspectY", ras.readDouble());
       addSeriesMeta("DisplayAspectZ", ras.readDouble());
       addSeriesMeta("DisplayAspectTime", ras.readDouble());
 
       overlayOffsets[5] = ras.readInt();
       overlayOffsets[6] = ras.readInt();
       overlayOffsets[7] = ras.readInt();
       overlayOffsets[8] = ras.readInt();
 
       if (getMetadataOptions().getMetadataLevel() != MetadataLevel.NO_OVERLAYS)
       {
         for (int i=0; i<overlayOffsets.length; i++) {
           parseOverlays(series, overlayOffsets[i], overlayKeys[i], store);
         }
       }
 
       totalROIs = 0;
 
       addSeriesMeta("ToolbarFlags", ras.readInt());
 
       channelWavelengthOffset = ras.readInt();
       ras.skipBytes(64);
     }
     else ras.skipBytes(182);
 
     MetadataTools.setDefaultCreationDate(store, getCurrentFile(), series);
     if (getSizeC() > 1) {
       if (!splitPlanes) splitPlanes = isRGB();
       core[series].rgb = false;
       if (splitPlanes) core[series].imageCount *= getSizeC();
     }
 
     for (int c=0; c<getEffectiveSizeC(); c++) {
       String lsid = MetadataTools.createLSID("Channel", series, c);
       store.setChannelID(lsid, series, c);
     }
 
     if (getMetadataOptions().getMetadataLevel() != MetadataLevel.MINIMUM) {
       // NB: the Zeiss LSM 5.5 specification indicates that there should be
       //     15 32-bit integers here; however, there are actually 16 32-bit
       //     integers before the tile position offset.
       //     We have confirmed with Zeiss that this is correct, and the 6.0
       //     specification was updated to contain the correct information.
       ras.skipBytes(64);
 
       int tilePositionOffset = ras.readInt();
 
       ras.skipBytes(36);
 
       int positionOffset = ras.readInt();
 
       // read referenced structures
 
       addSeriesMeta("DimensionZ", getSizeZ());
       addSeriesMeta("DimensionChannels", getSizeC());
       addSeriesMeta("DimensionM", dimensionM);
       addSeriesMeta("DimensionP", dimensionP);
 
       if (positionOffset != 0) {
         in.seek(positionOffset);
         int nPositions = in.readInt();
         for (int i=0; i<nPositions; i++) {
           double xPos = originX + in.readDouble() * 1000000;
           double yPos = originY + in.readDouble() * 1000000;
           double zPos = originZ + in.readDouble() * 1000000;
           xCoordinates.add(xPos);
           yCoordinates.add(yPos);
           zCoordinates.add(zPos);
 
           addGlobalMeta("X position for position #" + (i + 1), xPos);
           addGlobalMeta("Y position for position #" + (i + 1), yPos);
           addGlobalMeta("Z position for position #" + (i + 1), zPos);
         }
       }
 
       if (tilePositionOffset != 0) {
         in.seek(tilePositionOffset);
         int nTiles = in.readInt();
         for (int i=0; i<nTiles; i++) {
           double xPos = originX + in.readDouble() * 1000000;
           double yPos = originY + in.readDouble() * 1000000;
           double zPos = originZ + in.readDouble() * 1000000;
           if (xCoordinates.size() > i) {
             xPos += xCoordinates.get(i);
             xCoordinates.setElementAt(xPos, i);
           }
           if (yCoordinates.size() > i) {
             yPos += yCoordinates.get(i);
             yCoordinates.setElementAt(yPos, i);
           }
           if (zCoordinates.size() > i) {
             zPos += zCoordinates.get(i);
             zCoordinates.setElementAt(zPos, i);
           }
 
           addGlobalMeta("X position for position #" + (i + 1), xPos);
           addGlobalMeta("Y position for position #" + (i + 1), yPos);
           addGlobalMeta("Z position for position #" + (i + 1), zPos);
         }
       }
 
       if (channelColorsOffset != 0) {
         in.seek(channelColorsOffset + 12);
         int colorsOffset = in.readInt();
         int namesOffset = in.readInt();
 
         // read the color of each channel
 
         if (colorsOffset > 0) {
           in.seek(channelColorsOffset + colorsOffset);
           lut[getSeries()] = new byte[getSizeC() * 3][256];
           core[getSeries()].indexed = true;
           for (int i=0; i<getSizeC(); i++) {
             int color = in.readInt();
             int red = color & 0xff;
             int green = (color & 0xff00) >> 8;
             int blue = (color & 0xff0000) >> 16;
 
             for (int j=0; j<256; j++) {
               lut[getSeries()][i * 3][j] = (byte) ((red / 255.0) * j);
               lut[getSeries()][i * 3 + 1][j] = (byte) ((green / 255.0) * j);
               lut[getSeries()][i * 3 + 2][j] = (byte) ((blue / 255.0) * j);
             }
           }
         }
 
         // read the name of each channel
 
         if (namesOffset > 0) {
           in.seek(channelColorsOffset + namesOffset + 4);
 
           for (int i=0; i<getSizeC(); i++) {
             if (in.getFilePointer() >= in.length() - 1) break;
             // we want to read until we find a null char
             String name = in.readCString();
             if (name.length() <= 128) {
               addSeriesMeta("ChannelName" + i, name);
             }
           }
         }
       }
 
       if (timeStampOffset != 0) {
         in.seek(timeStampOffset + 8);
         for (int i=0; i<getSizeT(); i++) {
           double stamp = in.readDouble();
           addSeriesMeta("TimeStamp" + i, stamp);
           timestamps.add(new Double(stamp));
         }
       }
 
       if (eventListOffset != 0) {
         in.seek(eventListOffset + 4);
         int numEvents = in.readInt();
         in.seek(in.getFilePointer() - 4);
         in.order(!in.isLittleEndian());
         int tmpEvents = in.readInt();
         if (numEvents < 0) numEvents = tmpEvents;
         else numEvents = (int) Math.min(numEvents, tmpEvents);
         in.order(!in.isLittleEndian());
 
         if (numEvents > 65535) numEvents = 0;
 
         for (int i=0; i<numEvents; i++) {
           if (in.getFilePointer() + 16 <= in.length()) {
             int size = in.readInt();
             double eventTime = in.readDouble();
             int eventType = in.readInt();
             addSeriesMeta("Event" + i + " Time", eventTime);
             addSeriesMeta("Event" + i + " Type", eventType);
             long fp = in.getFilePointer();
             int len = size - 16;
             if (len > 65536) len = 65536;
             if (len < 0) len = 0;
             addSeriesMeta("Event" + i + " Description", in.readString(len));
             in.seek(fp + size - 16);
             if (in.getFilePointer() < 0) break;
           }
         }
       }
 
       if (scanInformationOffset != 0) {
         in.seek(scanInformationOffset);
 
         nextLaser = nextDetector = 0;
         nextFilter = nextDichroicChannel = nextDichroic = 0;
         nextDataChannel = nextDetectChannel = nextIllumChannel = 0;
 
         Vector<SubBlock> blocks = new Vector<SubBlock>();
 
         while (in.getFilePointer() < in.length() - 12) {
           if (in.getFilePointer() < 0) break;
           int entry = in.readInt();
           int blockType = in.readInt();
           int dataSize = in.readInt();
 
           if (blockType == TYPE_SUBBLOCK) {
             SubBlock block = null;
             switch (entry) {
               case SUBBLOCK_RECORDING:
                 block = new Recording();
                 break;
               case SUBBLOCK_LASER:
                 block = new Laser();
                 break;
               case SUBBLOCK_TRACK:
                 block = new Track();
                 break;
               case SUBBLOCK_DETECTION_CHANNEL:
                 block = new DetectionChannel();
                 break;
               case SUBBLOCK_ILLUMINATION_CHANNEL:
                 block = new IlluminationChannel();
                 break;
               case SUBBLOCK_BEAM_SPLITTER:
                 block = new BeamSplitter();
                 break;
               case SUBBLOCK_DATA_CHANNEL:
                 block = new DataChannel();
                 break;
               case SUBBLOCK_TIMER:
                 block = new Timer();
                 break;
               case SUBBLOCK_MARKER:
                 block = new Marker();
                 break;
             }
             if (block != null) {
               blocks.add(block);
             }
           }
           else if (dataSize + in.getFilePointer() <= in.length() &&
             dataSize > 0)
           {
             in.skipBytes(dataSize);
           }
           else break;
         }
 
         Vector<SubBlock> nonAcquiredBlocks = new Vector<SubBlock>();
 
         SubBlock[] metadataBlocks = blocks.toArray(new SubBlock[0]);
         for (SubBlock block : metadataBlocks) {
           block.addToHashtable();
           if (!block.acquire) {
             nonAcquiredBlocks.add(block);
             blocks.remove(block);
           }
         }
 
         for (int i=0; i<blocks.size(); i++) {
           SubBlock block = blocks.get(i);
           // every valid IlluminationChannel must be immediately followed by
           // a valid DataChannel or IlluminationChannel
           if ((block instanceof IlluminationChannel) && i < blocks.size() - 1) {
             SubBlock nextBlock = blocks.get(i + 1);
             if (!(nextBlock instanceof DataChannel) &&
               !(nextBlock instanceof IlluminationChannel))
             {
               ((IlluminationChannel) block).wavelength = null;
             }
           }
           // every valid DetectionChannel must be immediately preceded by
           // a valid Track or DetectionChannel
           else if ((block instanceof DetectionChannel) && i > 0) {
             SubBlock prevBlock = blocks.get(i - 1);
             if (!(prevBlock instanceof Track) &&
               !(prevBlock instanceof DetectionChannel))
             {
               block.acquire = false;
               nonAcquiredBlocks.add(block);
             }
           }
           if (block.acquire) populateMetadataStore(block, store, series);
         }
 
         for (SubBlock block : nonAcquiredBlocks) {
           populateMetadataStore(block, store, series);
         }
       }
     }
 
     imageNames.add(imageName);
 
     if (getMetadataOptions().getMetadataLevel() != MetadataLevel.MINIMUM) {
       if (userName != null) {
         String experimenterID = MetadataTools.createLSID("Experimenter", 0);
         store.setExperimenterID(experimenterID, 0);
         store.setExperimenterUserName(userName, 0);
         store.setExperimenterDisplayName(userName, 0);
       }
 
       Double pixX = new Double(pixelSizeX);
       Double pixY = new Double(pixelSizeY);
       Double pixZ = new Double(pixelSizeZ);
 
       store.setPixelsPhysicalSizeX(pixX, series);
       store.setPixelsPhysicalSizeY(pixY, series);
       store.setPixelsPhysicalSizeZ(pixZ, series);
 
       double firstStamp = 0;
       if (timestamps.size() > 0) {
         firstStamp = timestamps.get(0).doubleValue();
       }
 
       for (int i=0; i<getImageCount(); i++) {
         int[] zct = FormatTools.getZCTCoords(this, i);
 
         if (zct[2] < timestamps.size()) {
           double thisStamp = timestamps.get(zct[2]).doubleValue();
           store.setPlaneDeltaT(thisStamp - firstStamp, series, i);
           int index = zct[2] + 1;
           double nextStamp = index < timestamps.size() ?
             timestamps.get(index).doubleValue() : thisStamp;
           if (i == getSizeT() - 1 && zct[2] > 0) {
             thisStamp = timestamps.get(zct[2] - 1).doubleValue();
           }
           store.setPlaneExposureTime(nextStamp - thisStamp, series, i);
         }
         if (xCoordinates.size() > series) {
           store.setPlanePositionX(xCoordinates.get(series), series, i);
           store.setPlanePositionY(yCoordinates.get(series), series, i);
           store.setPlanePositionZ(zCoordinates.get(series), series, i);
         }
       }
     }
     ras.close();
    in.close();
   }
 
   protected void populateMetadataStore(SubBlock block, MetadataStore store,
     int series)
     throws FormatException
   {
     if (getMetadataOptions().getMetadataLevel() == MetadataLevel.MINIMUM) {
       return;
     }
 
     int instrument = getEffectiveSeries(series);
 
     // NB: block.acquire can be false.  If that is the case, Instrument data
     // is the only thing that should be populated.
     if (block instanceof Recording) {
       Recording recording = (Recording) block;
       String objectiveID = MetadataTools.createLSID("Objective", instrument, 0);
       if (recording.acquire) {
         store.setImageDescription(recording.description, series);
         store.setImageAcquiredDate(recording.startTime, series);
         store.setImageObjectiveSettingsID(objectiveID, series);
         binning = recording.binning;
       }
       store.setObjectiveCorrection(
         getCorrection(recording.correction), instrument, 0);
       store.setObjectiveImmersion(
         getImmersion(recording.immersion), instrument, 0);
       if (recording.magnification != null && recording.magnification > 0) {
         store.setObjectiveNominalMagnification(
           new PositiveInteger(recording.magnification), instrument, 0);
       }
       store.setObjectiveLensNA(recording.lensNA, instrument, 0);
       store.setObjectiveIris(recording.iris, instrument, 0);
       store.setObjectiveID(objectiveID, instrument, 0);
     }
     else if (block instanceof Laser) {
       Laser laser = (Laser) block;
       if (laser.medium != null) {
         store.setLaserLaserMedium(getLaserMedium(laser.medium),
           instrument, nextLaser);
       }
       if (laser.type != null) {
         store.setLaserType(getLaserType(laser.type), instrument, nextLaser);
       }
       if (laser.model != null) {
         store.setLaserModel(laser.model, instrument, nextLaser);
       }
       String lightSourceID =
         MetadataTools.createLSID("LightSource", instrument, nextLaser);
       store.setLaserID(lightSourceID, instrument, nextLaser);
       nextLaser++;
     }
     else if (block instanceof Track) {
       Track track = (Track) block;
       if (track.acquire) {
         store.setPixelsTimeIncrement(track.timeIncrement, series);
       }
     }
     else if (block instanceof DataChannel) {
       DataChannel channel = (DataChannel) block;
       if (channel.name != null && nextDataChannel < getSizeC() &&
         channel.acquire)
       {
         store.setChannelName(channel.name, series, nextDataChannel++);
       }
     }
     else if (block instanceof DetectionChannel) {
       DetectionChannel channel = (DetectionChannel) block;
       if (channel.pinhole != null && channel.pinhole.doubleValue() != 0f &&
         nextDetectChannel < getSizeC() && channel.acquire)
       {
         store.setChannelPinholeSize(channel.pinhole, series, nextDetectChannel);
       }
       if (channel.filter != null) {
         String id = MetadataTools.createLSID("Filter", instrument, nextFilter);
         if (channel.acquire && nextDetectChannel < getSizeC()) {
           store.setLightPathEmissionFilterRef(
             id, instrument, nextDetectChannel, 0);
         }
         store.setFilterID(id, instrument, nextFilter);
         store.setFilterModel(channel.filter, instrument, nextFilter);
 
         int space = channel.filter.indexOf(" ");
         if (space != -1) {
           String type = channel.filter.substring(0, space).trim();
           if (type.equals("BP")) type = "BandPass";
           else if (type.equals("LP")) type = "LongPass";
 
           store.setFilterType(getFilterType(type), instrument, nextFilter);
 
           String transmittance = channel.filter.substring(space + 1).trim();
           String[] v = transmittance.split("-");
           try {
             store.setTransmittanceRangeCutIn(
               PositiveInteger.valueOf(v[0].trim()), instrument, nextFilter);
           }
           catch (NumberFormatException e) { }
           if (v.length > 1) {
             try {
               store.setTransmittanceRangeCutOut(
                 PositiveInteger.valueOf(v[1].trim()), instrument, nextFilter);
             }
             catch (NumberFormatException e) { }
           }
         }
 
         nextFilter++;
       }
       if (channel.channelName != null) {
         String detectorID =
           MetadataTools.createLSID("Detector", instrument, nextDetector);
         store.setDetectorID(detectorID, instrument, nextDetector);
         if (channel.acquire && nextDetector < getSizeC()) {
           store.setDetectorSettingsID(detectorID, series, nextDetector);
           store.setDetectorSettingsBinning(
             getBinning(binning), series, nextDetector);
         }
       }
       if (channel.amplificationGain != null) {
         store.setDetectorAmplificationGain(
           channel.amplificationGain, instrument, nextDetector);
       }
       if (channel.gain != null) {
         store.setDetectorGain(channel.gain, instrument, nextDetector);
       }
       store.setDetectorType(getDetectorType("PMT"), instrument, nextDetector);
       store.setDetectorZoom(zoom, instrument, nextDetector);
       nextDetectChannel++;
       nextDetector++;
     }
     else if (block instanceof BeamSplitter) {
       BeamSplitter beamSplitter = (BeamSplitter) block;
       if (beamSplitter.filterSet != null) {
         if (beamSplitter.filter != null) {
           String id = MetadataTools.createLSID(
             "Dichroic", instrument, nextDichroic);
           store.setDichroicID(id, instrument, nextDichroic);
           store.setDichroicModel(beamSplitter.filter, instrument, nextDichroic);
           if (nextDichroicChannel < getEffectiveSizeC()) {
             store.setLightPathDichroicRef(id, series, nextDichroicChannel);
           }
           nextDichroic++;
         }
         nextDichroicChannel++;
       }
     }
     else if (block instanceof IlluminationChannel) {
       IlluminationChannel channel = (IlluminationChannel) block;
       if (channel.acquire && channel.wavelength != null) {
         store.setChannelEmissionWavelength(
           new PositiveInteger(channel.wavelength), series, nextIllumChannel++);
       }
     }
   }
 
   /** Parses overlay-related fields. */
   protected void parseOverlays(int series, long data, String suffix,
     MetadataStore store) throws IOException
   {
     if (data == 0) return;
     String prefix = "Series " + series + " ";
 
     in.seek(data);
 
     int numberOfShapes = in.readInt();
     int size = in.readInt();
     if (size <= 194) return;
     in.skipBytes(20);
 
     boolean valid = in.readInt() == 1;
 
     in.skipBytes(164);
 
     for (int i=totalROIs; i<totalROIs+numberOfShapes; i++) {
       long offset = in.getFilePointer();
       int type = in.readInt();
       int blockLength = in.readInt();
       double lineWidth = in.readInt();
       int measurements = in.readInt();
       double textOffsetX = in.readDouble();
       double textOffsetY = in.readDouble();
       int color = in.readInt();
       boolean validShape = in.readInt() != 0;
       int knotWidth = in.readInt();
       int catchArea = in.readInt();
       int fontHeight = in.readInt();
       int fontWidth = in.readInt();
       int fontEscapement = in.readInt();
       int fontOrientation = in.readInt();
       int fontWeight = in.readInt();
       boolean fontItalic = in.readInt() != 0;
       boolean fontUnderlined = in.readInt() != 0;
       boolean fontStrikeout = in.readInt() != 0;
       int fontCharSet = in.readInt();
       int fontOutputPrecision = in.readInt();
       int fontClipPrecision = in.readInt();
       int fontQuality = in.readInt();
       int fontPitchAndFamily = in.readInt();
       String fontName = DataTools.stripString(in.readString(64));
       boolean enabled = in.readShort() == 0;
       boolean moveable = in.readInt() == 0;
       in.skipBytes(34);
 
       String roiID = MetadataTools.createLSID("ROI", i);
       String shapeID = MetadataTools.createLSID("Shape", i, 0);
 
       switch (type) {
         case TEXT:
           double x = in.readDouble();
           double y = in.readDouble();
           String text = DataTools.stripString(in.readCString());
           store.setTextValue(text, i, 0);
           store.setTextFontSize(new NonNegativeInteger(fontHeight), i, 0);
           store.setTextStrokeWidth(lineWidth, i, 0);
           store.setROIID(roiID, i);
           store.setTextID(shapeID, i, 0);
           break;
         case LINE:
           in.skipBytes(4);
           double startX = in.readDouble();
           double startY = in.readDouble();
           double endX = in.readDouble();
           double endY = in.readDouble();
 
           store.setLineX1(startX, i, 0);
           store.setLineY1(startY, i, 0);
           store.setLineX2(endX, i, 0);
           store.setLineY2(endY, i, 0);
           store.setLineFontSize(new NonNegativeInteger(fontHeight), i, 0);
           store.setLineStrokeWidth(lineWidth, i, 0);
           store.setROIID(roiID, i);
           store.setLineID(shapeID, i, 0);
           break;
         case SCALE_BAR:
         case OPEN_ARROW:
         case CLOSED_ARROW:
         case PALETTE:
           in.skipBytes(36);
           break;
         case RECTANGLE:
           in.skipBytes(4);
           double topX = in.readDouble();
           double topY = in.readDouble();
           double bottomX = in.readDouble();
           double bottomY = in.readDouble();
           double width = Math.abs(bottomX - topX);
           double height = Math.abs(bottomY - topY);
 
           topX = Math.min(topX, bottomX);
           topY = Math.min(topY, bottomY);
 
           store.setRectangleX(topX, i, 0);
           store.setRectangleY(topY, i, 0);
           store.setRectangleWidth(width, i, 0);
           store.setRectangleHeight(height, i, 0);
           store.setRectangleFontSize(new NonNegativeInteger(fontHeight), i, 0);
           store.setRectangleStrokeWidth(lineWidth, i, 0);
           store.setROIID(roiID, i);
           store.setRectangleID(shapeID, i, 0);
 
           break;
         case ELLIPSE:
           int knots = in.readInt();
           double[] xs = new double[knots];
           double[] ys = new double[knots];
           for (int j=0; j<xs.length; j++) {
             xs[j] = in.readDouble();
             ys[j] = in.readDouble();
           }
 
           double rx = 0, ry = 0, centerX = 0, centerY = 0;
 
           if (knots == 4) {
             double r1x = Math.abs(xs[2] - xs[0]) / 2;
             double r1y = Math.abs(ys[2] - ys[0]) / 2;
             double r2x = Math.abs(xs[3] - xs[1]) / 2;
             double r2y = Math.abs(ys[3] - ys[1]) / 2;
 
             if (r1x > r2x) {
               ry = r1y;
               rx = r2x;
               centerX = Math.min(xs[3], xs[1]) + rx;
               centerY = Math.min(ys[2], ys[0]) + ry;
             }
             else {
               ry = r2y;
               rx = r1x;
               centerX = Math.min(xs[2], xs[0]) + rx;
               centerY = Math.min(ys[3], ys[1]) + ry;
             }
           }
           else if (knots == 3) {
             // we are given the center point and one cut point for each axis
             centerX = xs[0];
             centerY = ys[0];
 
             rx = Math.sqrt(Math.pow(xs[1] - xs[0], 2) +
               Math.pow(ys[1] - ys[0], 2));
             ry = Math.sqrt(Math.pow(xs[2] - xs[0], 2) +
               Math.pow(ys[2] - ys[0], 2));
 
             // calculate rotation angle
             double slope = (ys[2] - centerY) / (xs[2] - centerX);
             double theta = Math.toDegrees(Math.atan(slope));
 
             store.setEllipseTransform("rotate(" + theta + " " + centerX +
               " " + centerY + ")", i, 0);
           }
 
           store.setEllipseX(centerX, i, 0);
           store.setEllipseY(centerY, i, 0);
           store.setEllipseRadiusX(rx, i, 0);
           store.setEllipseRadiusY(ry, i, 0);
           store.setEllipseFontSize(new NonNegativeInteger(fontHeight), i, 0);
           store.setEllipseStrokeWidth(lineWidth, i, 0);
           store.setROIID(roiID, i);
           store.setEllipseID(shapeID, i, 0);
 
           break;
         case CIRCLE:
           in.skipBytes(4);
           centerX = in.readDouble();
           centerY = in.readDouble();
           double curveX = in.readDouble();
           double curveY = in.readDouble();
 
           double radius = Math.sqrt(Math.pow(curveX - centerX, 2) +
             Math.pow(curveY - centerY, 2));
 
           store.setEllipseX(centerX, i, 0);
           store.setEllipseY(centerY, i, 0);
           store.setEllipseRadiusX(radius, i, 0);
           store.setEllipseRadiusY(radius, i, 0);
           store.setEllipseFontSize(new NonNegativeInteger(fontHeight), i, 0);
           store.setEllipseStrokeWidth(lineWidth, i, 0);
           store.setROIID(roiID, i);
           store.setEllipseID(shapeID, i, 0);
 
           break;
         case CIRCLE_3POINT:
           in.skipBytes(4);
           // given 3 points on the perimeter of the circle, we need to
           // calculate the center and radius
           double[][] points = new double[3][2];
           for (int j=0; j<points.length; j++) {
             for (int k=0; k<points[j].length; k++) {
               points[j][k] = in.readDouble();
             }
           }
 
           double s = 0.5 * ((points[1][0] - points[2][0]) *
             (points[0][0] - points[2][0]) - (points[1][1] - points[2][1]) *
             (points[2][1] - points[0][1]));
           double div = (points[0][0] - points[1][0]) *
             (points[2][1] - points[0][1]) - (points[1][1] - points[0][1]) *
             (points[0][0] - points[2][0]);
           s /= div;
 
           double cx = 0.5 * (points[0][0] + points[1][0]) +
             s * (points[1][1] - points[0][1]);
           double cy = 0.5 * (points[0][1] + points[1][1]) +
             s * (points[0][0] - points[1][0]);
 
           double r = Math.sqrt(Math.pow(points[0][0] - cx, 2) +
             Math.pow(points[0][1] - cy, 2));
 
           store.setEllipseX(cx, i, 0);
           store.setEllipseY(cy, i, 0);
           store.setEllipseRadiusX(r, i, 0);
           store.setEllipseRadiusY(r, i, 0);
           store.setEllipseFontSize(new NonNegativeInteger(fontHeight), i, 0);
           store.setEllipseStrokeWidth(lineWidth, i, 0);
           store.setROIID(roiID, i);
           store.setEllipseID(shapeID, i, 0);
 
           break;
         case ANGLE:
           in.skipBytes(4);
           points = new double[3][2];
           for (int j=0; j<points.length; j++) {
             for (int k=0; k<points[j].length; k++) {
               points[j][k] = in.readDouble();
             }
           }
 
           StringBuffer p = new StringBuffer();
           for (int j=0; j<points.length; j++) {
             p.append(points[j][0]);
             p.append(",");
             p.append(points[j][1]);
             if (j < points.length - 1) p.append(" ");
           }
 
           store.setPolylinePoints(p.toString(), i, 0);
           store.setPolylineFontSize(new NonNegativeInteger(fontHeight), i, 0);
           store.setPolylineStrokeWidth(lineWidth, i, 0);
           store.setROIID(roiID, i);
           store.setPolylineID(shapeID, i, 0);
 
           break;
         case CLOSED_POLYLINE:
         case OPEN_POLYLINE:
         case POLYLINE_ARROW:
           int nKnots = in.readInt();
           points = new double[nKnots][2];
           for (int j=0; j<points.length; j++) {
             for (int k=0; k<points[j].length; k++) {
               points[j][k] = in.readDouble();
             }
           }
 
           p = new StringBuffer();
           for (int j=0; j<points.length; j++) {
             p.append(points[j][0]);
             p.append(",");
             p.append(points[j][1]);
             if (j < points.length - 1) p.append(" ");
           }
 
           store.setPolylinePoints(p.toString(), i, 0);
           store.setPolylineClosed(type == CLOSED_POLYLINE, i, 0);
           store.setPolylineFontSize(new NonNegativeInteger(fontHeight), i, 0);
           store.setPolylineStrokeWidth(lineWidth, i, 0);
           store.setROIID(roiID, i);
           store.setPolylineID(shapeID, i, 0);
 
           break;
         case CLOSED_BEZIER:
         case OPEN_BEZIER:
         case BEZIER_WITH_ARROW:
           nKnots = in.readInt();
           points = new double[nKnots][2];
           for (int j=0; j<points.length; j++) {
             for (int k=0; k<points[j].length; k++) {
               points[j][k] = in.readDouble();
             }
           }
 
           p = new StringBuffer();
           for (int j=0; j<points.length; j++) {
             p.append(points[j][0]);
             p.append(",");
             p.append(points[j][1]);
             if (j < points.length - 1) p.append(" ");
           }
 
           store.setPolylinePoints(p.toString(), i, 0);
           store.setPolylineClosed(type != OPEN_BEZIER, i, 0);
           store.setPolylineFontSize(new NonNegativeInteger(fontHeight), i, 0);
           store.setPolylineStrokeWidth(lineWidth, i, 0);
           store.setROIID(roiID, i);
           store.setPolylineID(shapeID, i, 0);
 
           break;
         default:
           i--;
           numberOfShapes--;
           continue;
       }
 
       // populate shape attributes
 
       in.seek(offset + blockLength);
     }
     totalROIs += numberOfShapes;
   }
 
   /** Parse a .mdb file and return a list of referenced .lsm files. */
   private String[] parseMDB(String mdbFile) throws FormatException, IOException
   {
     Location mdb = new Location(mdbFile).getAbsoluteFile();
     Location parent = mdb.getParentFile();
 
     MDBService mdbService = null;
     try {
       ServiceFactory factory = new ServiceFactory();
       mdbService = factory.getInstance(MDBService.class);
     }
     catch (DependencyException de) {
       throw new FormatException("MDB Tools Java library not found", de);
     }
 
     try {
       mdbService.initialize(mdbFile);
     }
     catch (Exception e) {
       return null;
     }
     Vector<Vector<String[]>> tables = mdbService.parseDatabase();
     Vector<String> referencedLSMs = new Vector<String>();
 
     for (Vector<String[]> table : tables) {
       String[] columnNames = table.get(0);
       String tableName = columnNames[0];
 
       for (int row=1; row<table.size(); row++) {
         String[] tableRow = table.get(row);
         for (int col=0; col<tableRow.length; col++) {
           String key = tableName + " " + columnNames[col + 1] + " " + row;
           if (currentId != null) {
             addGlobalMeta(key, tableRow[col]);
           }
 
           if (tableName.equals("Recordings") && columnNames[col + 1] != null &&
             columnNames[col + 1].equals("SampleData"))
           {
             String filename = tableRow[col].trim();
             filename = filename.replace('\\', File.separatorChar);
             filename = filename.replace('/', File.separatorChar);
             filename =
               filename.substring(filename.lastIndexOf(File.separator) + 1);
             if (filename.length() > 0) {
               Location file = new Location(parent, filename);
               if (file.exists()) {
                 referencedLSMs.add(file.getAbsolutePath());
               }
             }
           }
         }
       }
     }
 
     if (referencedLSMs.size() > 0) {
       return referencedLSMs.toArray(new String[0]);
     }
 
     String[] fileList = parent.list(true);
     for (int i=0; i<fileList.length; i++) {
       if (checkSuffix(fileList[i], new String[] {"lsm"}) &&
         !fileList[i].startsWith("."))
       {
         referencedLSMs.add(new Location(parent, fileList[i]).getAbsolutePath());
       }
     }
     return referencedLSMs.toArray(new String[0]);
   }
 
   private static Hashtable<Integer, String> createKeys() {
     Hashtable<Integer, String> h = new Hashtable<Integer, String>();
     h.put(new Integer(0x10000001), "Name");
     h.put(new Integer(0x4000000c), "Name");
     h.put(new Integer(0x50000001), "Name");
     h.put(new Integer(0x90000001), "Name");
     h.put(new Integer(0x90000005), "Detection Channel Name");
     h.put(new Integer(0xb0000003), "Name");
     h.put(new Integer(0xd0000001), "Name");
     h.put(new Integer(0x12000001), "Name");
     h.put(new Integer(0x14000001), "Name");
     h.put(new Integer(0x10000002), "Description");
     h.put(new Integer(0x14000002), "Description");
     h.put(new Integer(0x10000003), "Notes");
     h.put(new Integer(0x10000004), "Objective");
     h.put(new Integer(0x10000005), "Processing Summary");
     h.put(new Integer(0x10000006), "Special Scan Mode");
     h.put(new Integer(0x10000007), "Scan Type");
     h.put(new Integer(0x10000008), "Scan Mode");
     h.put(new Integer(0x10000009), "Number of Stacks");
     h.put(new Integer(0x1000000a), "Lines Per Plane");
     h.put(new Integer(0x1000000b), "Samples Per Line");
     h.put(new Integer(0x1000000c), "Planes Per Volume");
     h.put(new Integer(0x1000000d), "Images Width");
     h.put(new Integer(0x1000000e), "Images Height");
     h.put(new Integer(0x1000000f), "Number of Planes");
     h.put(new Integer(0x10000010), "Number of Stacks");
     h.put(new Integer(0x10000011), "Number of Channels");
     h.put(new Integer(0x10000012), "Linescan XY Size");
     h.put(new Integer(0x10000013), "Scan Direction");
     h.put(new Integer(0x10000014), "Time Series");
     h.put(new Integer(0x10000015), "Original Scan Data");
     h.put(new Integer(0x10000016), "Zoom X");
     h.put(new Integer(0x10000017), "Zoom Y");
     h.put(new Integer(0x10000018), "Zoom Z");
     h.put(new Integer(0x10000019), "Sample 0X");
     h.put(new Integer(0x1000001a), "Sample 0Y");
     h.put(new Integer(0x1000001b), "Sample 0Z");
     h.put(new Integer(0x1000001c), "Sample Spacing");
     h.put(new Integer(0x1000001d), "Line Spacing");
     h.put(new Integer(0x1000001e), "Plane Spacing");
     h.put(new Integer(0x1000001f), "Plane Width");
     h.put(new Integer(0x10000020), "Plane Height");
     h.put(new Integer(0x10000021), "Volume Depth");
     h.put(new Integer(0x10000034), "Rotation");
     h.put(new Integer(0x10000035), "Precession");
     h.put(new Integer(0x10000036), "Sample 0Time");
     h.put(new Integer(0x10000037), "Start Scan Trigger In");
     h.put(new Integer(0x10000038), "Start Scan Trigger Out");
     h.put(new Integer(0x10000039), "Start Scan Event");
     h.put(new Integer(0x10000040), "Start Scan Time");
     h.put(new Integer(0x10000041), "Stop Scan Trigger In");
     h.put(new Integer(0x10000042), "Stop Scan Trigger Out");
     h.put(new Integer(0x10000043), "Stop Scan Event");
     h.put(new Integer(0x10000044), "Stop Scan Time");
     h.put(new Integer(0x10000045), "Use ROIs");
     h.put(new Integer(0x10000046), "Use Reduced Memory ROIs");
     h.put(new Integer(0x10000047), "User");
     h.put(new Integer(0x10000048), "Use B/C Correction");
     h.put(new Integer(0x10000049), "Position B/C Contrast 1");
     h.put(new Integer(0x10000050), "Position B/C Contrast 2");
     h.put(new Integer(0x10000051), "Interpolation Y");
     h.put(new Integer(0x10000052), "Camera Binning");
     h.put(new Integer(0x10000053), "Camera Supersampling");
     h.put(new Integer(0x10000054), "Camera Frame Width");
     h.put(new Integer(0x10000055), "Camera Frame Height");
     h.put(new Integer(0x10000056), "Camera Offset X");
     h.put(new Integer(0x10000057), "Camera Offset Y");
     h.put(new Integer(0x40000001), "Multiplex Type");
     h.put(new Integer(0x40000002), "Multiplex Order");
     h.put(new Integer(0x40000003), "Sampling Mode");
     h.put(new Integer(0x40000004), "Sampling Method");
     h.put(new Integer(0x40000005), "Sampling Number");
     h.put(new Integer(0x40000006), "Acquire");
     h.put(new Integer(0x50000002), "Acquire");
     h.put(new Integer(0x7000000b), "Acquire");
     h.put(new Integer(0x90000004), "Acquire");
     h.put(new Integer(0xd0000017), "Acquire");
     h.put(new Integer(0x40000007), "Sample Observation Time");
     h.put(new Integer(0x40000008), "Time Between Stacks");
     h.put(new Integer(0x4000000d), "Collimator 1 Name");
     h.put(new Integer(0x4000000e), "Collimator 1 Position");
     h.put(new Integer(0x4000000f), "Collimator 2 Name");
     h.put(new Integer(0x40000010), "Collimator 2 Position");
     h.put(new Integer(0x40000011), "Is Bleach Track");
     h.put(new Integer(0x40000012), "Bleach After Scan Number");
     h.put(new Integer(0x40000013), "Bleach Scan Number");
     h.put(new Integer(0x40000014), "Trigger In");
     h.put(new Integer(0x12000004), "Trigger In");
     h.put(new Integer(0x14000003), "Trigger In");
     h.put(new Integer(0x40000015), "Trigger Out");
     h.put(new Integer(0x12000005), "Trigger Out");
     h.put(new Integer(0x14000004), "Trigger Out");
     h.put(new Integer(0x40000016), "Is Ratio Track");
     h.put(new Integer(0x40000017), "Bleach Count");
     h.put(new Integer(0x40000018), "SPI Center Wavelength");
     h.put(new Integer(0x40000019), "Pixel Time");
     h.put(new Integer(0x40000020), "ID Condensor Frontlens");
     h.put(new Integer(0x40000021), "Condensor Frontlens");
     h.put(new Integer(0x40000022), "ID Field Stop");
     h.put(new Integer(0x40000023), "Field Stop Value");
     h.put(new Integer(0x40000024), "ID Condensor Aperture");
     h.put(new Integer(0x40000025), "Condensor Aperture");
     h.put(new Integer(0x40000026), "ID Condensor Revolver");
     h.put(new Integer(0x40000027), "Condensor Revolver");
     h.put(new Integer(0x40000028), "ID Transmission Filter 1");
     h.put(new Integer(0x40000029), "ID Transmission 1");
     h.put(new Integer(0x40000030), "ID Transmission Filter 2");
     h.put(new Integer(0x40000031), "ID Transmission 2");
     h.put(new Integer(0x40000032), "Repeat Bleach");
     h.put(new Integer(0x40000033), "Enable Spot Bleach Pos");
     h.put(new Integer(0x40000034), "Spot Bleach Position X");
     h.put(new Integer(0x40000035), "Spot Bleach Position Y");
     h.put(new Integer(0x40000036), "Bleach Position Z");
     h.put(new Integer(0x50000003), "Power");
     h.put(new Integer(0x90000002), "Power");
     h.put(new Integer(0x70000003), "Detector Gain");
     h.put(new Integer(0x70000005), "Amplifier Gain");
     h.put(new Integer(0x70000007), "Amplifier Offset");
     h.put(new Integer(0x70000009), "Pinhole Diameter");
     h.put(new Integer(0x7000000c), "Detector Name");
     h.put(new Integer(0x7000000d), "Amplifier Name");
     h.put(new Integer(0x7000000e), "Pinhole Name");
     h.put(new Integer(0x7000000f), "Filter Set Name");
     h.put(new Integer(0x70000010), "Filter Name");
     h.put(new Integer(0x70000013), "Integrator Name");
     h.put(new Integer(0x70000014), "Detection Channel Name");
     h.put(new Integer(0x70000015), "Detector Gain B/C 1");
     h.put(new Integer(0x70000016), "Detector Gain B/C 2");
     h.put(new Integer(0x70000017), "Amplifier Gain B/C 1");
     h.put(new Integer(0x70000018), "Amplifier Gain B/C 2");
     h.put(new Integer(0x70000019), "Amplifier Offset B/C 1");
     h.put(new Integer(0x70000020), "Amplifier Offset B/C 2");
     h.put(new Integer(0x70000021), "Spectral Scan Channels");
     h.put(new Integer(0x70000022), "SPI Wavelength Start");
     h.put(new Integer(0x70000023), "SPI Wavelength End");
     h.put(new Integer(0x70000026), "Dye Name");
     h.put(new Integer(0xd0000014), "Dye Name");
     h.put(new Integer(0x70000027), "Dye Folder");
     h.put(new Integer(0xd0000015), "Dye Folder");
     h.put(new Integer(0x90000003), "Wavelength");
     h.put(new Integer(0x90000006), "Power B/C 1");
     h.put(new Integer(0x90000007), "Power B/C 2");
     h.put(new Integer(0xb0000001), "Filter Set");
     h.put(new Integer(0xb0000002), "Filter");
     h.put(new Integer(0xd0000004), "Color");
     h.put(new Integer(0xd0000005), "Sample Type");
     h.put(new Integer(0xd0000006), "Bits Per Sample");
     h.put(new Integer(0xd0000007), "Ratio Type");
     h.put(new Integer(0xd0000008), "Ratio Track 1");
     h.put(new Integer(0xd0000009), "Ratio Track 2");
     h.put(new Integer(0xd000000a), "Ratio Channel 1");
     h.put(new Integer(0xd000000b), "Ratio Channel 2");
     h.put(new Integer(0xd000000c), "Ratio Const. 1");
     h.put(new Integer(0xd000000d), "Ratio Const. 2");
     h.put(new Integer(0xd000000e), "Ratio Const. 3");
     h.put(new Integer(0xd000000f), "Ratio Const. 4");
     h.put(new Integer(0xd0000010), "Ratio Const. 5");
     h.put(new Integer(0xd0000011), "Ratio Const. 6");
     h.put(new Integer(0xd0000012), "Ratio First Images 1");
     h.put(new Integer(0xd0000013), "Ratio First Images 2");
     h.put(new Integer(0xd0000016), "Spectrum");
     h.put(new Integer(0x12000003), "Interval");
     return h;
   }
 
   private Integer readEntry() throws IOException {
     return new Integer(in.readInt());
   }
 
   private Object readValue() throws IOException {
     int blockType = in.readInt();
     int dataSize = in.readInt();
 
     switch (blockType) {
       case TYPE_LONG:
         return new Long(in.readInt());
       case TYPE_RATIONAL:
         return new Double(in.readDouble());
       case TYPE_ASCII:
         String s = in.readString(dataSize).trim();
         StringBuffer sb = new StringBuffer();
         for (int i=0; i<s.length(); i++) {
           if (s.charAt(i) >= 10) sb.append(s.charAt(i));
           else break;
         }
 
         return sb.toString();
       case TYPE_SUBBLOCK:
         return null;
     }
     in.skipBytes(dataSize);
     return "";
   }
 
   // -- Helper classes --
 
   class SubBlock {
     public Hashtable<Integer, Object> blockData;
     public boolean acquire = true;
 
     public SubBlock() {
       try {
         read();
       }
       catch (IOException e) {
         LOGGER.debug("Failed to read sub-block data", e);
       }
     }
 
     protected int getIntValue(int key) {
       Object o = blockData.get(new Integer(key));
       if (o == null) return -1;
       return !(o instanceof Number) ? -1 : ((Number) o).intValue();
     }
 
     protected float getFloatValue(int key) {
       Object o = blockData.get(new Integer(key));
       if (o == null) return -1f;
       return !(o instanceof Number) ? -1f : ((Number) o).floatValue();
     }
 
     protected double getDoubleValue(int key) {
       Object o = blockData.get(new Integer(key));
       if (o == null) return -1d;
       return !(o instanceof Number) ? -1d : ((Number) o).doubleValue();
     }
 
     protected String getStringValue(int key) {
       Object o = blockData.get(new Integer(key));
       return o == null ? null : o.toString();
     }
 
     protected void read() throws IOException {
       blockData = new Hashtable<Integer, Object>();
       Integer entry = readEntry();
       Object value = readValue();
       while (value != null) {
         if (!blockData.containsKey(entry)) blockData.put(entry, value);
         entry = readEntry();
         value = readValue();
       }
     }
 
     public void addToHashtable() {
       String prefix = this.getClass().getSimpleName() + " #";
       int index = 1;
       while (getSeriesMeta(prefix + index + " Acquire") != null) index++;
       prefix += index;
       Integer[] keys = blockData.keySet().toArray(new Integer[0]);
       for (Integer key : keys) {
         if (METADATA_KEYS.get(key) != null) {
           addSeriesMeta(prefix + " " + METADATA_KEYS.get(key),
             blockData.get(key));
 
           if (METADATA_KEYS.get(key).equals("Bits Per Sample")) {
             core[getSeries()].bitsPerPixel =
               Integer.parseInt(blockData.get(key).toString());
           }
           else if (METADATA_KEYS.get(key).equals("User")) {
             userName = blockData.get(key).toString();
           }
         }
       }
       addGlobalMeta(prefix + " Acquire", new Boolean(acquire));
     }
   }
 
   class Recording extends SubBlock {
     public String description;
     public String name;
     public String binning;
     public String startTime;
     // Objective data
     public String correction, immersion;
     public Integer magnification;
     public Double lensNA;
     public Boolean iris;
 
     protected void read() throws IOException {
       super.read();
       description = getStringValue(RECORDING_DESCRIPTION);
       name = getStringValue(RECORDING_NAME);
       binning = getStringValue(RECORDING_CAMERA_BINNING);
       if (binning != null && binning.indexOf("x") == -1) {
         if (binning.equals("0")) binning = null;
         else binning += "x" + binning;
       }
 
       // start time in days since Dec 30 1899
       long stamp = (long) (getDoubleValue(RECORDING_SAMPLE_0TIME) * 86400000);
       if (stamp > 0) {
         startTime = DateTools.convertDate(stamp, DateTools.MICROSOFT);
       }
 
       zoom = getDoubleValue(RECORDING_ZOOM);
 
       String objective = getStringValue(RECORDING_OBJECTIVE);
 
       correction = "";
 
       if (objective == null) objective = "";
       String[] tokens = objective.split(" ");
       int next = 0;
       for (; next<tokens.length; next++) {
         if (tokens[next].indexOf("/") != -1) break;
         correction += tokens[next];
       }
       if (next < tokens.length) {
         String p = tokens[next++];
         try {
           magnification = new Integer(p.substring(0, p.indexOf("/") - 1));
         }
         catch (NumberFormatException e) { }
         try {
           lensNA = new Double(p.substring(p.indexOf("/") + 1));
         }
         catch (NumberFormatException e) { }
       }
 
       immersion = next < tokens.length ? tokens[next++] : "Unknown";
       iris = Boolean.FALSE;
       if (next < tokens.length) {
         iris = new Boolean(tokens[next++].trim().equalsIgnoreCase("iris"));
       }
     }
   }
 
   class Laser extends SubBlock {
     public String medium, type, model;
     public Double power;
 
     protected void read() throws IOException {
       super.read();
       model = getStringValue(LASER_NAME);
       type = getStringValue(LASER_NAME);
       if (type == null) type = "";
       medium = "";
 
       if (type.startsWith("HeNe")) {
         medium = "HeNe";
         type = "Gas";
       }
       else if (type.startsWith("Argon")) {
         medium = "Ar";
         type = "Gas";
       }
       else if (type.equals("Titanium:Sapphire") || type.equals("Mai Tai")) {
         medium = "TiSapphire";
         type = "SolidState";
       }
       else if (type.equals("YAG")) {
         medium = "";
         type = "SolidState";
       }
       else if (type.equals("Ar/Kr")) {
         medium = "";
         type = "Gas";
       }
 
       acquire = getIntValue(LASER_ACQUIRE) != 0;
       power = getDoubleValue(LASER_POWER);
     }
   }
 
   class Track extends SubBlock {
     public Double timeIncrement;
 
     protected void read() throws IOException {
       super.read();
       timeIncrement = getDoubleValue(TRACK_TIME_BETWEEN_STACKS);
       acquire = getIntValue(TRACK_ACQUIRE) != 0;
     }
   }
 
   class DetectionChannel extends SubBlock {
     public Double pinhole;
     public Double gain, amplificationGain;
     public String filter, filterSet;
     public String channelName;
 
     protected void read() throws IOException {
       super.read();
       pinhole = new Double(getDoubleValue(CHANNEL_PINHOLE_DIAMETER));
       gain = new Double(getDoubleValue(CHANNEL_DETECTOR_GAIN));
       amplificationGain = new Double(getDoubleValue(CHANNEL_AMPLIFIER_GAIN));
       filter = getStringValue(CHANNEL_FILTER);
       if (filter != null) {
         filter = filter.trim();
         if (filter.length() == 0 || filter.equals("None")) {
           filter = null;
         }
       }
 
       filterSet = getStringValue(CHANNEL_FILTER_SET);
       channelName = getStringValue(CHANNEL_NAME);
       acquire = getIntValue(CHANNEL_ACQUIRE) != 0;
     }
   }
 
   class IlluminationChannel extends SubBlock {
     public Integer wavelength;
     public Double attenuation;
     public String name;
 
     protected void read() throws IOException {
       super.read();
       wavelength = new Integer(getIntValue(ILLUM_CHANNEL_WAVELENGTH));
       attenuation = new Double(getDoubleValue(ILLUM_CHANNEL_ATTENUATION));
       acquire = getIntValue(ILLUM_CHANNEL_ACQUIRE) != 0;
 
       name = getStringValue(ILLUM_CHANNEL_NAME);
       try {
         wavelength = new Integer(name);
       }
       catch (NumberFormatException e) { }
     }
   }
 
   class DataChannel extends SubBlock {
     public String name;
 
     protected void read() throws IOException {
       super.read();
       name = getStringValue(DATA_CHANNEL_NAME);
       for (int i=0; i<name.length(); i++) {
         if (name.charAt(i) < 10) {
           name = name.substring(0, i);
           break;
         }
       }
 
       acquire = getIntValue(DATA_CHANNEL_ACQUIRE) != 0;
     }
   }
 
   class BeamSplitter extends SubBlock {
     public String filter, filterSet;
 
     protected void read() throws IOException {
       super.read();
 
       filter = getStringValue(BEAM_SPLITTER_FILTER);
       if (filter != null) {
         filter = filter.trim();
         if (filter.length() == 0 || filter.equals("None")) {
           filter = null;
         }
       }
       filterSet = getStringValue(BEAM_SPLITTER_FILTER_SET);
     }
   }
 
   class Timer extends SubBlock { }
   class Marker extends SubBlock { }
 
 }
