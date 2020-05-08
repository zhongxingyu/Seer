 package edu.umn.msi.tropix.proteomics.conversion.impl;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Scanner;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.util.StringUtils;
 
 import com.google.common.base.Optional;
 import com.google.common.collect.Lists;
 
 import edu.umn.msi.tropix.proteomics.conversion.DtaNameUtils;
 import edu.umn.msi.tropix.proteomics.conversion.DtaNameUtils.DtaNameSummary;
 import edu.umn.msi.tropix.proteomics.conversion.Scan;
 
 class MgfScanExtracter {
   private static final Log LOG = LogFactory.getLog(MgfScanExtracter.class);
   private static final Pattern MSM_LINE_PRECURSOR_MATCHER = Pattern
       .compile(".*[pP][rR][eE][cC][iI][nN][tT][eE][nN][sS][iI][tT][yY]:\\s+([^\\s]+)\\s+.*");
   // private static final Pattern MSM_LINE_PERIOD_MATCHER = Pattern.compile(".*period:\\s+(.*\\.[rR][aA][wW])\\s+.*");
   private static final String FLOATING_POINT_PATTERN = "(?:(?:[\\+\\-]?\\d+\\.?\\d*)|(?:[\\+\\-]?\\d*\\.\\d+))";
   private static final Pattern PEAK_LINE_PATTERN = Pattern.compile("\\s*" + FLOATING_POINT_PATTERN + "\\s+" + FLOATING_POINT_PATTERN + "\\s*");
 
   private String titleStr = "run";
   private Optional<String> defaultTitleStr = Optional.<String>absent();
   private float precursorMz = 0.0f;
   private float precursorIntensity = 0.0f;
   private int end = 0;
   private int start = 0;
   private List<Short> charges = null;
   private int numPeaks = 0;
   private final Iterator<String> scanSectionLines;
   private List<Short> defaultCharges;
   private double[] peaksArray;
   private boolean guessChargeState = false;
   private StringBuilder peaksLines;
 
   MgfScanExtracter(final List<String> scanSectionLines, final List<Short> defaultCharges) {
     this(scanSectionLines.iterator(), defaultCharges);
   }
 
   MgfScanExtracter(final Iterator<String> scanSectionLines, final List<Short> defaultCharges, final Optional<String> defaultParentName) {
     this.scanSectionLines = scanSectionLines;
     this.defaultCharges = defaultCharges;
     this.defaultTitleStr = defaultParentName;
   }
 
   MgfScanExtracter(final Iterator<String> scanSectionLines, final List<Short> defaultCharges) {
     this(scanSectionLines, defaultCharges, Optional.<String>absent());
   }
 
   List<Scan> extractScans() {
     peaksLines = new StringBuilder();
     while(scanSectionLines.hasNext()) {
       final String scanSectionLine = scanSectionLines.next();
       final String upperCaseLine = scanSectionLine.toUpperCase();
       if(upperCaseLine.startsWith("PEPMASS=")) {
         handlePepMassLine(scanSectionLine);
       } else if(upperCaseLine.startsWith("TITLE=")) {
         handleTitleLine(scanSectionLine);
       } else if(upperCaseLine.startsWith("CHARGE=")) {
         handleChargeLine(scanSectionLine);
       } else if(PEAK_LINE_PATTERN.matcher(scanSectionLine).matches()) {
         handlePeaksLine(scanSectionLine);
       } else if(upperCaseLine.startsWith("SCANS=")) {
         handleScansLine(scanSectionLine);
       }
     }
     buildPeaksArray();
     return buildScans();
   }
 
   private void handleScansLine(final String line) {
     end = MgfParseUtils.parseScanEnd(line);
     start = MgfParseUtils.parseScanStart(line);
   }
 
   private void handlePeaksLine(final String line) {
     peaksLines.append(line + " ");
     numPeaks += 2;
   }
 
   private void handleChargeLine(final String line) {
     charges = MgfParseUtils.parseCharges(line);
   }
 
   private void handleTitleLine(final String line) {
     titleStr = line.substring("TITLE=".length()).trim();
     if(DtaNameUtils.isDtaName(titleStr)) {
       final DtaNameSummary dtaSummary = DtaNameUtils.getDtaNameSummary(titleStr);
       if(end == 0) {
         end = dtaSummary.getEnd();
       }
     } else if(MgfParseUtils.isAbSciexTitle(titleStr)) {
       if(end != 0) {
         throw new IllegalStateException("AB SCIEX title found as well as scan number");
       }
       this.end = MgfParseUtils.getAbSciexScanNumber(titleStr);
       this.titleStr = defaultTitleStr.get();
     } else if(MgfParseUtils.isReadw4MascotTitle(titleStr)) {
       this.end = MgfParseUtils.getReadw4MascotScanNumber(titleStr);
      // this.titleStr = defaultTitleStr.get();
       // These files usually don't specify a charge state, so we need to guess.
       this.guessChargeState = true;
     } else {
       if(end == 0) {
         final Pattern scanNumberPattern = Pattern.compile(".*finn[ei]ganscannumber:\\s+(\\d+).*", Pattern.CASE_INSENSITIVE);
         final Matcher scanNumberMatcher = scanNumberPattern.matcher(titleStr);
         if(scanNumberMatcher.matches()) {
           end = Integer.parseInt(scanNumberMatcher.group(1));
         } else {
           // Some with scan number might also have cycle, this check needs to come afterward
           final Pattern cyclePattern = Pattern.compile(".*cycle\\(s\\):\\s+(\\d+).*", Pattern.CASE_INSENSITIVE);
           final Matcher cycleMatcher = cyclePattern.matcher(titleStr);
           if(cycleMatcher.matches()) {
             end = Integer.parseInt(cycleMatcher.group(1));
           }
         }
       }
 
       if(precursorIntensity == 0.0f) {
         final String precursorIntensityStr = getMsmPrecursor(line);
         if(StringUtils.hasText(precursorIntensityStr)) {
           precursorIntensity = Float.parseFloat(precursorIntensityStr);
         }
       }
 
       // Stupidly changing titleStr in place so previous checks need to come first
       final Pattern filePattern = Pattern.compile(".*(?:Raw)?File:\\s+(.*?)(?:\\s+|,).*", Pattern.CASE_INSENSITIVE);
       final Matcher fileMatcher = filePattern.matcher(titleStr);
       if(fileMatcher.matches()) {
         final String matchedText = fileMatcher.group(1);
         if(StringUtils.hasText(matchedText)) {
           this.titleStr = matchedText;
         }
       } else {
         // Some with file also have period, so have to test this after the previous check
         final Pattern periodPattern = Pattern.compile(".*period:\\s+(.*?)(?:\\s+|,).*", Pattern.CASE_INSENSITIVE);
         final Matcher periodMatcher = periodPattern.matcher(titleStr);
         if(periodMatcher.matches()) {
           final String matchedText = periodMatcher.group(1);
           if(StringUtils.hasText(matchedText)) {
             this.titleStr = matchedText;
           }
         }
       }
 
     }
   }
 
   private String getMsmPrecursor(final String line) {
     final Matcher lineMatcher = MSM_LINE_PRECURSOR_MATCHER.matcher(line);
     return lineMatcher.matches() ? lineMatcher.group(1) : null;
   }
 
   private void handlePepMassLine(final String line) {
     final String pepStr = line.substring("PEPMASS=".length()).trim();
     final Scanner pepScanner = new Scanner(pepStr);
     precursorMz = pepScanner.nextFloat();
     if(pepScanner.hasNextFloat()) {
       precursorIntensity = pepScanner.nextFloat();
     }
   }
 
   private void buildPeaksArray() {
     final Scanner peaksScanner = new Scanner(peaksLines.toString());
     peaksArray = new double[numPeaks];
     for(int i = 0; i < numPeaks; i++) {
       peaksArray[i] = peaksScanner.nextDouble();
     }
   }
 
   private List<Scan> buildScans() {
     final Scan templateScan = new Scan(2, end, peaksArray);
     if(start != 0) {
       templateScan.setAlt(start);
     }
     templateScan.setPrecursorMz(precursorMz);
     templateScan.setPrecursorIntensity(precursorIntensity);
     templateScan.setParentFileName(titleStr);
 
     final List<Scan> scansToCache = Lists.newArrayList();
     if(charges == null && templateScan.isPrecursorChargeSet()) {
       scansToCache.add(templateScan);
     } else {
       if(charges == null) {
         if(defaultCharges == null && !guessChargeState) {
           throw new IllegalStateException("MGF scan did not contain a CHARGE line, and no default was specified in file");
        } else if(defaultCharges == null) {
           boolean isPlus1Charge = ConversionUtils.isPlus1ChargeState(templateScan.getPeaks(), templateScan.getPrecursorMz());
           if(isPlus1Charge) {
             this.charges = Lists.<Short>newArrayList((short) 1);
           } else {
            this.charges = Lists.<Short>newArrayList((short) 2, (short) 3);
           }
         } else {
          charges = defaultCharges;
         }
       }

       for(final Short charge : charges) {
         final Scan newScan = templateScan.clone();
         newScan.setPrecursorCharge(charge);
         scansToCache.add(newScan);
       }
     }
     return scansToCache;
   }
 }
