 //
 // Importer.java
 //
 
 /*
 LOCI Plugins for ImageJ: a collection of ImageJ plugins including the 4D
 Data Browser, OME Plugin and Bio-Formats Exporter. Copyright (C) 2006
 Melissa Linkert, Christopher Peterson, Curtis Rueden, Philip Huettl
 and Francis Wong.
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU Library General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Library General Public License for more details.
 
 You should have received a copy of the GNU Library General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
 package loci.plugins;
 
 import ij.*;
 import ij.gui.GenericDialog;
 import ij.io.FileInfo;
 import ij.io.OpenDialog;
 import ij.measure.Calibration;
 import ij.process.*;
 import java.awt.*;
 import java.awt.image.*;
 import java.io.File;
 import java.io.IOException;
 import java.util.Hashtable;
 import javax.swing.*;
 import loci.formats.*;
 
 /**
  * Core logic for the LOCI Importer ImageJ plugin.
  *
  * @author Curtis Rueden ctrueden at wisc.edu
  * @author Melissa Linkert linkert at cs.wisc.edu
  */
 public class Importer {
 
   // -- Fields --
 
   private LociImporter plugin;
 
   // -- Constructor --
 
   public Importer(LociImporter plugin) { this.plugin = plugin; }
 
   // -- Importer API methods --
 
   /** Executes the plugin. */
   public void run(String arg) {
     boolean quiet = arg != null && !arg.equals("");
     String options = Macro.getOptions();
 
     // -- Step 1: get filename to open --
 
     String id = null;
 
     // try to get filename from argument
     if (quiet) id = arg;
 
     if (id == null) {
       // try to get filename from macro options
       if (options != null) {
         String open = Macro.getValue(options, "open", null);
         if (open != null) id = open;
       }
     }
 
     // if necessary, prompt the user for the filename
     OpenDialog od = new OpenDialog("Open", id);
     String directory = od.getDirectory();
     String fileName = od.getFileName();
     if (fileName == null) {
       plugin.canceled = true;
       return;
     }
     id = directory + fileName;
 
     // if no valid filename, give up
     if (id == null || !new File(id).exists()) {
       if (!quiet) {
         IJ.error("LOCI Bio-Formats", "The specified file " +
           (id == null ? "" : ("(" + id + ") ")) + "does not exist.");
       }
       return;
     }
 
     // -- Step 2: identify file --
 
     IJ.showStatus("Identifying " + fileName);
 
     // determine whether we can handle this file
     ImageReader reader = new ImageReader();
     IFormatReader r = null;
     try { r = reader.getReader(id); }
     catch (Exception exc) {
       exc.printStackTrace();
       IJ.showStatus("");
       if (!quiet) {
         String msg = exc.getMessage();
         IJ.error("LOCI Bio-Formats", "Sorry, there was a problem " +
           "reading the file" + (msg == null ? "." : (":\n" + msg)));
       }
       return;
     }
 
     // -- Step 3: get parameter values --
 
     IJ.showStatus("");
 
     final String mergeString = "Merge channels to RGB";
     final String ignoreString = "Ignore color lookup table";
     final String colorizeString = "Colorize channels";
     final String splitString = "Open each channel in its own window";
     final String metadataString = "Display associated metadata";
     final String stitchString = "Stitch files with similar names";
     final String rangeString = "Specify range for each series";
 
     // load preferences from IJ_Prefs.txt
     boolean mergeChannels = Prefs.get("bioformats.mergeChannels", false);
     boolean ignoreTables = Prefs.get("bioformats.ignoreTable", false);
     boolean colorize = Prefs.get("bioformats.colorize", false);
     boolean splitWindows = Prefs.get("bioformats.splitWindows", true);
     boolean showMetadata = Prefs.get("bioformats.showMetadata", false);
     boolean stitchFiles = Prefs.get("bioformats.stitchFiles", false);
     boolean specifyRanges = Prefs.get("bioformats.specifyRanges", false);
 
     // prompt for parameters, if necessary
     GenericDialog pd = new GenericDialog("LOCI Bio-Formats Import Options");
     pd.addCheckbox(mergeString, mergeChannels);
     pd.addCheckbox(ignoreString, ignoreTables);
     pd.addCheckbox(colorizeString, colorize);
     pd.addCheckbox(splitString, splitWindows);
     pd.addCheckbox(metadataString, showMetadata);
     pd.addCheckbox(stitchString, stitchFiles);
     pd.addCheckbox(rangeString, specifyRanges);
     pd.showDialog();
     if (pd.wasCanceled()) {
       plugin.canceled = true;
       return;
     }
     mergeChannels = pd.getNextBoolean();
     ignoreTables = pd.getNextBoolean();
     colorize = pd.getNextBoolean();
     splitWindows = pd.getNextBoolean();
     showMetadata = pd.getNextBoolean();
     stitchFiles = pd.getNextBoolean();
     specifyRanges = pd.getNextBoolean();
 
     // -- Step 4: open file --
 
     IJ.showStatus("Analyzing " + fileName);
 
     try {
       boolean doRGBMerge = false;
 
       // -- Step 4a: do some preparatory work --
 
       if (mergeChannels) r = new ChannelMerger(r);
       else r = new ChannelSeparator(r);
       if (stitchFiles) r = new FileStitcher(r);
 
       r.setColorTableIgnored(ignoreTables);
 
       // store OME metadata into OME-XML structure, if available
       OMEXMLMetadataStore store = new OMEXMLMetadataStore();
       store.createRoot();
       r.setMetadataStore(store);
 
       int seriesCount = r.getSeriesCount(id);
       boolean[] series = new boolean[seriesCount];
       series[0] = true;
 
       // build descriptive string and range for each series
       String[] seriesStrings = new String[seriesCount];
       int[] num = new int[seriesCount];
       int[] sizeC = new int[seriesCount];
       int[] sizeZ = new int[seriesCount];
       int[] sizeT = new int[seriesCount];
       boolean[] certain = new boolean[seriesCount];
       int[] cBegin = new int[seriesCount];
       int[] cEnd = new int[seriesCount];
       int[] cStep = new int[seriesCount];
       int[] zBegin = new int[seriesCount];
       int[] zEnd = new int[seriesCount];
       int[] zStep = new int[seriesCount];
       int[] tBegin = new int[seriesCount];
       int[] tEnd = new int[seriesCount];
       int[] tStep = new int[seriesCount];
       for (int i=0; i<seriesCount; i++) {
         r.setSeries(id, i);
         num[i] = r.getImageCount(id);
         sizeC[i] = r.getSizeC(id);
         if (r.isRGB(id)) sizeC[i] = sizeC[i] < 3 ? 1 : (sizeC[i] / 3);
         sizeZ[i] = r.getSizeZ(id);
         sizeT[i] = r.getSizeT(id);
         certain[i] = r.isOrderCertain(id);
         cBegin[i] = zBegin[i] = tBegin[i] = 0;
         if (certain[i]) {
           cEnd[i] = sizeC[i] - 1;
           zEnd[i] = sizeZ[i] - 1;
           tEnd[i] = sizeT[i] - 1;
         }
         else cEnd[i] = num[i] - 1;
         cStep[i] = zStep[i] = tStep[i] = 1;
         StringBuffer sb = new StringBuffer();
         sb.append("Series_");
         sb.append(i + 1);
         sb.append(" - ");
         String name = store.getImageName(new Integer(i));
         if (name != null && name.length() > 0) {
           sb.append(name);
           sb.append(": ");
         }
         sb.append(r.getSizeX(id));
         sb.append(" x ");
         sb.append(r.getSizeY(id));
         sb.append("; ");
         sb.append(num[i]);
         sb.append(" planes");
         if (certain[i]) {
           sb.append(" (");
           boolean first = true;
           if (sizeC[i] > 1) {
             sb.append(sizeC[i]);
             sb.append("C");
             first = false;
           }
           if (sizeZ[i] > 1) {
             if (!first) sb.append(" x ");
             sb.append(sizeZ[i]);
             sb.append("Z");
             first = false;
           }
           if (sizeT[i] > 1) {
             if (!first) sb.append(" x ");
             sb.append(sizeT[i]);
             sb.append("T");
             first = false;
           }
           sb.append(")");
         }
         seriesStrings[i] = sb.toString();
       }
 
       // -- Step 4a: prompt for the series to open, if necessary --
 
       if (seriesCount > 1) {
         IJ.showStatus("");
 
         GenericDialog sd = new GenericDialog("LOCI Bio-Formats Series Options");
         for (int i=0; i<seriesCount; i++) {
           sd.addCheckbox(seriesStrings[i], series[i]);
         }
         addScrollBars(sd);
         sd.showDialog();
         if (sd.wasCanceled()) {
           plugin.canceled = true;
           return;
         }
         for (int i=0; i<seriesCount; i++) series[i] = sd.getNextBoolean();
       }
 
       // -- Step 4b: prompt for the range of planes to import, if necessary --
 
       if (specifyRanges) {
         boolean needRange = false;
         for (int i=0; i<seriesCount; i++) {
           if (series[i] && num[i] > 1) needRange = true;
         }
         if (needRange) {
           IJ.showStatus("");
           GenericDialog rd =
             new GenericDialog("LOCI Bio-Formats Range Options");
           for (int i=0; i<seriesCount; i++) {
             if (!series[i]) continue;
             rd.addMessage(seriesStrings[i].replaceAll("_", " "));
             String s = "_" + (i + 1);
             if (certain[i]) {
               if (sizeC[i] > 1) {
                 rd.addNumericField("C_Begin" + s, cBegin[i] + 1, 0);
                 rd.addNumericField("C_End" + s, cEnd[i] + 1, 0);
                 rd.addNumericField("C_Step" + s, cStep[i], 0);
               }
               if (sizeZ[i] > 1) {
                 rd.addNumericField("Z_Begin" + s, zBegin[i] + 1, 0);
                 rd.addNumericField("Z_End" + s, zEnd[i] + 1, 0);
                 rd.addNumericField("Z_Step" + s, zStep[i], 0);
               }
               if (sizeT[i] > 1) {
                 rd.addNumericField("T_Begin" + s, tBegin[i] + 1, 0);
                 rd.addNumericField("T_End" + s, tEnd[i] + 1, 0);
                 rd.addNumericField("T_Step" + s, tStep[i], 0);
               }
             }
             else {
               rd.addNumericField("Begin" + s, cBegin[i] + 1, 0);
               rd.addNumericField("End" + s, cEnd[i] + 1, 0);
               rd.addNumericField("Step" + s, cStep[i], 0);
             }
           }
           addScrollBars(rd);
           rd.showDialog();
           if (rd.wasCanceled()) {
             plugin.canceled = true;
             return;
           }
           for (int i=0; i<seriesCount; i++) {
             if (!series[i]) continue;
             if (certain[i]) {
               if (sizeC[i] > 1) {
                 cBegin[i] = (int) rd.getNextNumber() - 1;
                 cEnd[i] = (int) rd.getNextNumber() - 1;
                 cStep[i] = (int) rd.getNextNumber();
               }
               if (sizeZ[i] > 1) {
                 zBegin[i] = (int) rd.getNextNumber() - 1;
                 zEnd[i] = (int) rd.getNextNumber() - 1;
                 zStep[i] = (int) rd.getNextNumber();
               }
               if (sizeT[i] > 1) {
                 tBegin[i] = (int) rd.getNextNumber() - 1;
                 tEnd[i] = (int) rd.getNextNumber() - 1;
                 tStep[i] = (int) rd.getNextNumber();
               }
             }
             else {
               cBegin[i] = (int) rd.getNextNumber() - 1;
               cEnd[i] = (int) rd.getNextNumber() - 1;
               cStep[i] = (int) rd.getNextNumber();
             }
             int maxC = certain[i] ? sizeC[i] : num[i];
             if (cBegin[i] < 0) cBegin[i] = 0;
             if (cBegin[i] >= maxC) cBegin[i] = maxC - 1;
             if (cEnd[i] < cBegin[i]) cEnd[i] = cBegin[i];
             if (cEnd[i] >= maxC) cEnd[i] = maxC - 1;
             if (cStep[i] < 1) cStep[i] = 1;
             if (zBegin[i] < 0) zBegin[i] = 0;
             if (zBegin[i] >= sizeZ[i]) zBegin[i] = sizeZ[i] - 1;
             if (zEnd[i] < zBegin[i]) zEnd[i] = zBegin[i];
             if (zEnd[i] >= sizeZ[i]) zEnd[i] = sizeZ[i] - 1;
             if (zStep[i] < 1) zStep[i] = 1;
             if (tBegin[i] < 0) tBegin[i] = 0;
             if (tBegin[i] >= sizeT[i]) tBegin[i] = sizeT[i] - 1;
             if (tEnd[i] < tBegin[i]) tEnd[i] = tBegin[i];
             if (tEnd[i] >= sizeT[i]) tEnd[i] = sizeT[i] - 1;
             if (tStep[i] < 1) tStep[i] = 1;
           }
         }
       }
 
       // -- Step 4c: display metadata, when appropriate --
 
       if (showMetadata) {
         IJ.showStatus("Populating metadata");
 
         // display standard metadata in a table in its own window
         Hashtable meta = r.getMetadata(id);
         meta.put("\tSizeX", new Integer(r.getSizeX(id)));
         meta.put("\tSizeY", new Integer(r.getSizeY(id)));
         meta.put("\tSizeZ", new Integer(r.getSizeZ(id)));
         meta.put("\tSizeT", new Integer(r.getSizeT(id)));
         meta.put("\tSizeC", new Integer(r.getSizeC(id)));
         meta.put("\tIsRGB", new Boolean(r.isRGB(id)));
         meta.put("\tPixelType",
           FormatReader.getPixelTypeString(r.getPixelType(id)));
         meta.put("\tLittleEndian", new Boolean(r.isLittleEndian(id)));
         meta.put("\tDimensionOrder", r.getDimensionOrder(id));
         meta.put("\tIsInterleaved", new Boolean(r.isInterleaved(id)));
         MetadataPane mp = new MetadataPane(meta);
         JFrame frame = new JFrame(id + " Metadata");
         frame.setContentPane(mp);
         frame.pack();
         frame.setVisible(true);
         frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         WindowManager.addWindow(frame);
       }
 
       // -- Step 4d: read pixel data --
 
       IJ.showStatus("Reading " + fileName);
 
       for (int i=0; i<seriesCount; i++) {
         if (!series[i]) continue;
         r.setSeries(id, i);
 
         String name = store.getImageName(new Integer(i));
         String imageName = fileName;
         if (name != null && name.length() > 0) imageName += " - " + name;
 
         boolean[] load = new boolean[num[i]];
         if (certain[i]) {
           for (int c=cBegin[i]; c<=cEnd[i]; c+=cStep[i]) {
             for (int z=zBegin[i]; z<=zEnd[i]; z+=zStep[i]) {
               for (int t=tBegin[i]; t<=tEnd[i]; t+=tStep[i]) {
                 int index = r.getIndex(id, z, c, t);
                 load[index] = true;
               }
             }
           }
         }
         else {
           for (int j=cBegin[i]; j<=cEnd[i]; j+=cStep[i]) load[j] = true;
         }
         int total = 0;
         for (int j=0; j<num[i]; j++) if (load[j]) total++;
 
         // dump OME-XML to ImageJ's description field, if available
         FileInfo fi = new FileInfo();
         fi.description = store.dumpXML();
 
         long startTime = System.currentTimeMillis();
         long time = startTime;
         ImageStack stackB = null, stackS = null, stackF = null, stackO = null;
 
         int q = 0;
         for (int j=0; j<num[i]; j++) {
           if (!load[j]) continue;
 
           // limit message update rate
           long clock = System.currentTimeMillis();
           if (clock - time >= 100) {
             IJ.showStatus("Reading " +
               (seriesCount > 1 ? ("series " + (i + 1) + ", ") : "") +
               "plane " + (j + 1) + "/" + num[i]);
             time = clock;
           }
           IJ.showProgress((double) q++ / total);
 
           byte[] b = r.openBytes(id, j);
 
           int w = r.getSizeX(id);
           int h = r.getSizeY(id);
           int c = r.isRGB(id) ? r.getSizeC(id) : 1;
           int type = r.getPixelType(id);
 
           // construct image processor and add to stack
 
           ImageProcessor ip = null;
 
           int bpp = 0;
           switch (type) {
             case FormatReader.INT8:
             case FormatReader.UINT8: bpp = 1; break;
             case FormatReader.INT16:
             case FormatReader.UINT16: bpp = 2; break;
             case FormatReader.INT32:
             case FormatReader.UINT32:
             case FormatReader.FLOAT: bpp = 4; break;
             case FormatReader.DOUBLE: bpp = 8; break;
           }
 
           if (b.length != w * h * c * bpp) {
             BufferedImage bi = r.openImage(id, j);
             b = ImageTools.padImage(b, r.isInterleaved(id), c, 
               bi.getWidth()*bpp, w, h);
           }
           
           Object pixels = DataTools.makeDataArray(b, bpp,
             type == FormatReader.FLOAT || type == FormatReader.DOUBLE,
             r.isLittleEndian(id));
 
           if (pixels instanceof byte[]) {
             byte[] bytes = (byte[]) pixels;
             if (bytes.length > w*h*c) {
               byte[] tmp = bytes;
               bytes = new byte[w*h*c];
               System.arraycopy(tmp, 0, bytes, 0, bytes.length);
             }
             if (c == 1) {
               ip = new ByteProcessor(w, h, bytes, null);
               if (stackB == null) stackB = new ImageStack(w, h);
               stackB.addSlice(imageName + ":" + (j + 1), ip);
             }
             else {
               if (stackO == null) stackO = new ImageStack(w, h);
               ip = new ColorProcessor(w, h);
               byte[][] pix = new byte[c][w*h];
               for (int k=0; k<c; k++) {
                 System.arraycopy(bytes, k*pix[k].length, pix[k], 0, 
                   pix[k].length);
               }
               ((ColorProcessor) ip).setRGB(pix[0], pix[1], 
                 pix.length >= 3 ? pix[2] : new byte[w*h]);
               stackO.addSlice(imageName + ":" + (j + 1), ip);
             }
           }
           else if (pixels instanceof short[]) {
             short[] s = (short[]) pixels;
             if (s.length > w*h*c) {
               short[] tmp = s;
               s = new short[w*h*c];
               System.arraycopy(tmp, 0, s, 0, s.length);
             }
             if (c == 1) {
               ip = new ShortProcessor(w, h, s, null);
               if (stackS == null) stackS = new ImageStack(w, h);
               stackS.addSlice(imageName + ":" + (j + 1), ip);
             }
             else {
               if (stackO == null) stackO = new ImageStack(w, h);
               short[][] pix = new short[c][w*h];
               for (int k=0; k<c; k++) {
                 System.arraycopy(s, k*pix[k].length, pix[k], 0, 
                   pix[k].length);
               }
               byte[][] bytes = new byte[c][w*h];
               for (int k=0; k<c; k++) {
                 ip = new ShortProcessor(w, h, pix[k], null);
                 ip = ip.convertToByte(true);
                 bytes[k] = (byte[]) ip.getPixels();
               }
               ip = new ColorProcessor(w, h);
               ((ColorProcessor) ip).setRGB(bytes[0], bytes[1], 
                 pix.length >= 3 ? bytes[2] : new byte[w*h]);
               stackO.addSlice(imageName + ":" + (j + 1), ip);
             }
           }
           else if (pixels instanceof int[]) {
             int[] s = (int[]) pixels;
             if (s.length > w*h*c) {
               int[] tmp = s;
               s = new int[w*h*c];
               System.arraycopy(tmp, 0, s, 0, s.length);
             }
             if (c == 1) {
               ip = new FloatProcessor(w, h, s);
               if (stackF == null) stackF = new ImageStack(w, h);
               stackF.addSlice(imageName + ":" + (j + 1), ip);
             }
             else {
               if (stackO == null) stackO = new ImageStack(w, h);
               int[][] pix = new int[c][w*h];
               for (int k=0; k<c; k++) {
                 System.arraycopy(s, k*pix[k].length, pix[k], 0, 
                   pix[k].length);
               }
               byte[][] bytes = new byte[c][w*h];
               for (int k=0; k<c; k++) {
                 ip = new FloatProcessor(w, h, pix[k]);
                 ip = ip.convertToByte(true);
                 bytes[k] = (byte[]) ip.getPixels();
               }
               ip = new ColorProcessor(w, h);
               ((ColorProcessor) ip).setRGB(bytes[0], bytes[1], 
                 pix.length >= 3 ? bytes[2] : new byte[w*h]);
               stackO.addSlice(imageName + ":" + (j + 1), ip);
             }
           }
           else if (pixels instanceof float[]) {
             float[] f = (float[]) pixels;
             if (f.length > w*h*c) {
               float[] tmp = f;
               f = new float[w*h*c];
               System.arraycopy(tmp, 0, f, 0, f.length);
             }
             if (c == 1) {
               ip = new FloatProcessor(w, h, f, null);
               if (stackF == null) stackF = new ImageStack(w, h);
 
               if (stackB != null) {
                 ip = ip.convertToByte(true);
                 stackB.addSlice(imageName + ":" + (j + 1), ip);
                 stackF = null;
               }
               else if (stackS != null) {
                 ip = ip.convertToShort(true);
                 stackS.addSlice(imageName + ":" + (j + 1), ip);
                 stackF = null;
               }
               else stackF.addSlice(imageName + ":" + (j + 1), ip);
             }
             else {
               if (stackO == null) stackO = new ImageStack(w, h);
               float[][] pix = new float[c][w*h];
              if (r.isInterleaved(id)) {
                 for (int k=0; k<f.length; k+=c) {
                   for (int l=0; l<c; l++) {
                     pix[l][k / 3] = f[k + l];
                   }
                 }
               }
               else {
                 for (int k=0; k<c; k++) {
                   System.arraycopy(f, k*pix[k].length, pix[k], 0, 
                     pix[k].length);
                 }
               }
               byte[][] bytes = new byte[c][w*h];
               for (int k=0; k<c; k++) {
                 ip = new FloatProcessor(w, h, pix[k], null);
                 ip = ip.convertToByte(true);
                 bytes[k] = (byte[]) ip.getPixels();
               }
               ip = new ColorProcessor(w, h);
               ((ColorProcessor) ip).setRGB(bytes[0], bytes[1], 
                 pix.length >= 3 ? bytes[2] : new byte[w*h]);
               stackO.addSlice(imageName + ":" + (j + 1), ip);
             }
           }
           else if (pixels instanceof double[]) {
             double[] d = (double[]) pixels;
             if (d.length > w*h*c) {
               double[] tmp = d;
               d = new double[w*h*c];
               System.arraycopy(tmp, 0, d, 0, d.length);
             }
             if (c == 1) {
               ip = new FloatProcessor(w, h, d);
               if (stackF == null) stackF = new ImageStack(w, h);
               stackF.addSlice(imageName + ":" + (j + 1), ip);
             }
             else {
               if (stackO == null) stackO = new ImageStack(w, h);
               double[][] pix = new double[c][w*h];
               for (int k=0; k<c; k++) {
                 System.arraycopy(d, k*pix[k].length, pix[k], 0, 
                   pix[k].length);
               }
               byte[][] bytes = new byte[c][w*h];
               for (int k=0; k<c; k++) {
                 ip = new FloatProcessor(w, h, pix[k]);
                 ip = ip.convertToByte(true);
                 bytes[k] = (byte[]) ip.getPixels();
               }
               ip = new ColorProcessor(w, h);
               ((ColorProcessor) ip).setRGB(bytes[0], bytes[1], 
                 pix.length >= 3 ? bytes[2] : new byte[w*h]);
               stackO.addSlice(imageName + ":" + (j + 1), ip);
             }
           }
         }
 
         IJ.showStatus("Creating image");
         IJ.showProgress(1);
         ImagePlus imp = null;
         if (stackB != null) {
           if (!mergeChannels && splitWindows) {
             slice(stackB, id, sizeZ[i], sizeC[i], sizeT[i],
               fi, r, specifyRanges, colorize);
           }
           else imp = new ImagePlus(imageName, stackB);
         }
         if (stackS != null) {
           if (!mergeChannels && splitWindows) {
             slice(stackS, id, sizeZ[i], sizeC[i], sizeT[i],
               fi, r, specifyRanges, colorize);
           }
           else imp = new ImagePlus(imageName, stackS);
         }
         if (stackF != null) {
           if (!mergeChannels && splitWindows) {
             slice(stackF, id, sizeZ[i], sizeC[i], sizeT[i],
               fi, r, specifyRanges, colorize);
           }
           else imp = new ImagePlus(imageName, stackF);
         }
         if (stackO != null) {
           if (!mergeChannels && splitWindows) {
             slice(stackO, id, sizeZ[i], sizeC[i], sizeT[i],
               fi, r, specifyRanges, colorize);
           }
           else imp = new ImagePlus(imageName, stackO);
         }
 
         if (imp != null) {
           // retrieve the spatial calibration information, if available
 
           double xcal = Double.NaN, ycal = Double.NaN, zcal = Double.NaN;
           Integer ii = new Integer(i);
 
           Float xf = store.getPixelSizeX(ii);
           if (xf != null) xcal = xf.floatValue();
           Float yf = store.getPixelSizeY(ii);
           if (yf != null) ycal = yf.floatValue();
           Float zf = store.getPixelSizeZ(ii);
           if (zf != null) zcal = zf.floatValue();
 
           if (xcal != Double.NaN || ycal != Double.NaN || zcal != Double.NaN) {
             Calibration c = new Calibration();
             if (xcal == xcal) {
               c.pixelWidth = xcal;
               c.setUnit("micron");
             }
             if (ycal == ycal) {
               c.pixelHeight = ycal;
               c.setUnit("micron");
             }
             if (zcal == zcal) {
               c.pixelDepth = zcal;
               c.setUnit("micron");
             }
             imp.setCalibration(c);
           }
 
           imp.setFileInfo(fi);
 
           int c = r.getSizeC(id);
           r.close();
 
           if (doRGBMerge) {
             ImageStack is = imp.getImageStack();
 
             int w = is.getWidth(), h = is.getHeight();
             ImageStack newStack = new ImageStack(w, h);
 
             ImageProcessor[] procs = new ImageProcessor[c];
             for (int k=0; k<is.getSize(); k+=c) {
               for (int j=0; j<c; j++) {
                 procs[j] = is.getProcessor(k + j + 1);
                 procs[j] = procs[j].convertToByte(true);
               }
 
               byte[] red = new byte[w * h];
               byte[] green = new byte[w * h];
               byte[] blue = new byte[w * h];
 
               red = (byte[]) procs[0].getPixels();
               if (c > 1) green = (byte[]) procs[1].getPixels();
               if (c > 2) blue = (byte[]) procs[2].getPixels();
 
               ColorProcessor color = new ColorProcessor(w, h);
               color.setRGB(red, green, blue);
               newStack.addSlice(is.getSliceLabel(k + 1), color);
             }
             imp.setStack(imp.getTitle(), newStack);
           }
           imp.show();
         }
 
         long endTime = System.currentTimeMillis();
         double elapsed = (endTime - startTime) / 1000.0;
         if (num[i] == 1) {
           IJ.showStatus("LOCI Bio-Formats: " + elapsed + " seconds");
         }
         else {
           long average = (endTime - startTime) / num[i];
           IJ.showStatus("LOCI Bio-Formats: " + elapsed + " seconds (" +
             average + " ms per plane)");
         }
       }
 
       r.close();
       plugin.success = true;
 
       // save parameter values to IJ_Prefs.txt
       Prefs.set("bioformats.mergeChannels", mergeChannels);
       Prefs.set("bioformats.ignoreTables", ignoreTables);
       Prefs.set("bioformats.colorize", colorize);
       Prefs.set("bioformats.splitWindows", splitWindows);
       Prefs.set("bioformats.showMetadata", showMetadata);
       Prefs.set("bioformats.stitchFiles", stitchFiles);
       Prefs.set("bioformats.specifyRanges", specifyRanges);
     }
     catch (Exception exc) {
       exc.printStackTrace();
       IJ.showStatus("");
       if (!quiet) {
         String msg = exc.getMessage();
         IJ.error("LOCI Bio-Formats", "Sorry, there was a problem " +
           "reading the data" + (msg == null ? "." : (":\n" + msg)));
       }
     }
   }
 
   // -- Helper methods --
 
   private void slice(ImageStack is, String id, int z, int c, int t,
     FileInfo fi, IFormatReader r, boolean range, boolean colorize)
     throws FormatException, IOException
   {
     int step = 1;
     if (range) {
       step = c;
       c = r.getSizeC(id);
     }
 
     ImageStack[] newStacks = new ImageStack[c];
     for (int i=0; i<newStacks.length; i++) {
       newStacks[i] = new ImageStack(is.getWidth(), is.getHeight());
     }
 
     for (int i=0; i<c; i++) {
       if (range) {
         for (int j=z; j<=t; j+=((t - z + 1) / is.getSize())) {
           int s = (i*step) + (j - z)*c + 1;
           if (s - 1 < is.getSize()) {
             newStacks[i].addSlice(is.getSliceLabel(s), is.getProcessor(s));
           }
         }
       }
       else {
         for (int j=0; j<z; j++) {
           for (int k=0; k<t; k++) {
             int s = r.getIndex(id, j, i, k) + 1;
             newStacks[i].addSlice(is.getSliceLabel(s), is.getProcessor(s));
           }
         }
       }
     }
 
     // retrieve the spatial calibration information, if available
 
     OMEXMLMetadataStore store =
       (OMEXMLMetadataStore) r.getMetadataStore(id);
     double xcal = Double.NaN, ycal = Double.NaN, zcal = Double.NaN;
     Integer ii = new Integer(r.getSeries(id));
 
     Float xf = store.getPixelSizeX(ii);
     if (xf != null) xcal = xf.floatValue();
     Float yf = store.getPixelSizeY(ii);
     if (yf != null) ycal = yf.floatValue();
     Float zf = store.getPixelSizeZ(ii);
     if (zf != null) zcal = zf.floatValue();
 
     for (int i=0; i<newStacks.length; i++) {
       ImagePlus imp = new ImagePlus(id + " - Ch" + (i+1), newStacks[i]);
 
       if (xcal != Double.NaN || ycal != Double.NaN || zcal != Double.NaN) {
         Calibration cal = new Calibration();
         if (xcal == xcal) {
           cal.pixelWidth = xcal;
           cal.setUnit("micron");
         }
         if (ycal == ycal) {
           cal.pixelHeight = ycal;
           cal.setUnit("micron");
         }
         if (zcal == zcal) {
           cal.pixelDepth = zcal;
           cal.setUnit("micron");
         }
         imp.setCalibration(cal);
       }
 
       // colorize channels; mostly copied from the ImageJ source
 
       if (colorize) {
         fi.reds = new byte[256];
         fi.greens = new byte[256];
         fi.blues = new byte[256];
 
         for (int j=0; j<256; j++) {
           switch (i) {
             case 0: fi.reds[j] = (byte) j; break;
             case 1: fi.greens[j] = (byte) j; break;
             case 2: fi.blues[j] = (byte) j; break;
           }
         }
 
         ImageProcessor ip = imp.getProcessor();
         ColorModel cm =
           new IndexColorModel(8, 256, fi.reds, fi.greens, fi.blues);
 
         ip.setColorModel(cm);
         if (imp.getStackSize() > 1) imp.getStack().setColorModel(cm);
       }
 
       imp.setFileInfo(fi);
       imp.show();
     }
   }
 
   private void addScrollBars(Container pane) {
     GridBagLayout layout = (GridBagLayout) pane.getLayout();
 
     // extract components
     int count = pane.getComponentCount();
     Component[] c = new Component[count];
     GridBagConstraints[] gbc = new GridBagConstraints[count];
     for (int i=0; i<count; i++) {
       c[i] = pane.getComponent(i);
       gbc[i] = layout.getConstraints(c[i]);
     }
 
     // clear components
     pane.removeAll();
     layout.invalidateLayout(pane);
 
     // create new container panel
     Panel newPane = new Panel();
     GridBagLayout newLayout = new GridBagLayout();
     newPane.setLayout(newLayout);
     for (int i=0; i<count; i++) {
       newLayout.setConstraints(c[i], gbc[i]);
       newPane.add(c[i]);
     }
 
     // get preferred size for container panel
     // NB: don't know a better way:
     // - newPane.getPreferredSize() doesn't work
     // - newLayout.preferredLayoutSize(newPane) doesn't work
     Frame f = new Frame();
     f.setLayout(new BorderLayout());
     f.add(newPane, BorderLayout.CENTER);
     f.pack();
     Dimension size = newPane.getSize();
     f.remove(newPane);
     f.dispose();
 
     // compute best size for scrollable viewport
     size.width += 15;
     size.height += 15;
     Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
     int maxWidth = 3 * screen.width / 4;
     int maxHeight = 3 * screen.height / 4;
     if (size.width > maxWidth) size.width = maxWidth;
     if (size.height > maxHeight) size.height = maxHeight;
 
     // create scroll pane
     ScrollPane scroll = new ScrollPane();
     scroll.setPreferredSize(size);
     scroll.add(newPane);
 
     // add scroll pane to original container
     GridBagConstraints constraints = new GridBagConstraints();
     constraints.fill = GridBagConstraints.BOTH;
     constraints.weightx = 1.0;
     constraints.weighty = 1.0;
     layout.setConstraints(scroll, constraints);
     pane.add(scroll);
   }
 
 }
