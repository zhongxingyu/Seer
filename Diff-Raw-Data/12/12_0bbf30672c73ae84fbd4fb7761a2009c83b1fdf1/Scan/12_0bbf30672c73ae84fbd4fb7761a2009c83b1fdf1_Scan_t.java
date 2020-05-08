 package ru.spbau.bioinf.tagfinder;
 
 import ru.spbau.bioinf.tagfinder.util.ReaderUtil;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Properties;
 
 public class Scan {
 
     private int id;
     private List<Peak> peaks = new ArrayList<Peak>();
     private double precursorMz;
     private int precursorCharge;
     private double precursorMass;
 
 
     public Scan(Properties prop, BufferedReader input, int scanId) throws IOException {
         id = scanId;
         precursorCharge = ReaderUtil.getIntValue(prop, "CHARGE");
         precursorMass = ReaderUtil.getDoubleValue(prop, "MONOISOTOPIC_MASS");
         List<String[]> datas;
         while ((datas = ReaderUtil.readDataUntil(input, "END ENVELOPE")).size() > 0) {
             double mass = 0;
             double score = 0;
             int charge = 0;
 
             for (String[] data : datas) {
                 if (data.length > 3) {
                     if ("REAL_MONO_MASS".equals(data[2])) {
                         mass = Double.parseDouble(data[3]);
                     }
                 }
                 if ("CHARGE".equals(data[0])) {
                     charge = Integer.parseInt(data[1]);
                 }
                 if ("SCORE".equals(data[0])) {
                     score = Double.parseDouble(data[1]);
                 }
             }
             if (mass > 0) {
                 peaks.add(new Peak(mass, score , charge));
             }
         }
     }
 
     public Scan(Properties prop, BufferedReader input) throws IOException {
         id = ReaderUtil.getIntValue(prop, "SCANS");
         precursorMz = ReaderUtil.getDoubleValue(prop, "PRECURSOR_MZ");
         precursorCharge = ReaderUtil.getIntValue(prop, "PRECURSOR_CHARGE");
         precursorMass = ReaderUtil.getDoubleValue(prop, "PRECURSOR_MASS");
         List<String[]> datas = ReaderUtil.readDataUntil(input, "END IONS");
         for (String[] data : datas) {
             double mass = Double.parseDouble(data[0]);
             if (mass < precursorMass) {
                 peaks.add(new Peak(mass, Double.parseDouble(data[1]), Integer.parseInt(data[2])));
             }
         }
     }
 
     public Scan(Scan original, List<Peak> peaks) {
         id = original.getId();
         precursorMz = original.getPrecursorMz();
         precursorCharge = original.getPrecursorCharge();
         precursorMass = original.getPrecursorMass();
         this.peaks.addAll(peaks);
     }
 
     public int getId() {
         return id;
     }
 
     public double getPrecursorMass() {
         return precursorMass;
     }
 
     public List<Peak> getPeaks() {
         return peaks;
     }
 
     public int getPrecursorCharge() {
         return precursorCharge;
     }
 
     public double getPrecursorMz() {
         return precursorMz;
     }
 
     public List<Peak> createStandardSpectrum() {
         List<Peak> peaks = new ArrayList<Peak>();
         peaks.addAll(this.peaks);
         peaks.add(new Peak(0, 0, 0));
         peaks.add(new Peak(getPrecursorMass(), 0, 0));
         Collections.sort(peaks);
         return peaks;
     }
 
    public List<Peak> createStandardSpectrumWithOnes() {
        List<Peak> peaks = new ArrayList<Peak>();
        peaks.addAll(this.peaks);
        for (Peak peak : this.peaks) {
            peaks.add(new Peak(peak.getValue() - 1, 0, 0));
            peaks.add(new Peak(peak.getValue() + 1, 0, 0));
        }
        peaks.add(new Peak(0, 0, 0));
        peaks.add(new Peak(getPrecursorMass(), 0, 0));
        Collections.sort(peaks);
        return peaks;
    }
 
     public List<Peak> createSpectrumWithYPeaks(double precursorMassShift) {
         List<Peak> peaks = new ArrayList<Peak>();
         peaks.addAll(this.peaks);
         peaks.add(new Peak(0, 0, 0));
         double newPrecursorMass = precursorMass + precursorMassShift;
         peaks.add(new Peak(newPrecursorMass, 0, 0));
         for (Peak peak : this.peaks) {
              peaks.add(peak.getYPeak(newPrecursorMass));
         }
         Collections.sort(peaks);
         return peaks;
     }
 
     public void save(File dir) throws IOException {
         File file = new File(dir, "scan_" + id + ".env");
         PrintWriter out = ReaderUtil.createOutputFile(file);
         out.println("BEGIN SPECTRUM");
         out.println("ID -1");
         out.println("SCANS "  + id);
         out.println("Ms_LEVEL 2");
         out.println("ENVELOPE_NUMBER "  + peaks.size());
         out.println("MONOISOTOPIC_MASS " + precursorMass);
         out.println("CHARGE " + precursorCharge);
 
         for (Peak peak : peaks) {
                 out.println("BEGIN ENVELOPE");
                 out.println("REF_IDX 0");
                 out.println("CHARGE " + peak.getCharge());
                 out.println("SCORE " + peak.getIntensity());
                 out.println("THEO_PEAK_NUM 2 REAL_PEAK_NUM 2");
                 out.println("THEO_MONO_MZ 295.0114742929687 REAL_MONO_MZ 295.0114742929687");
                 out.println("THEO_MONO_MASS 294.0041982347717 REAL_MONO_MASS " + peak.getMass());
                 out.println("THEO_INTE_SUM 49471.15545654297 REAL_INTE_SUM 49471.15545654297");
                 out.println("295.01092529296875 43400.988214475015 true 55 295.01092529296875 48567.5703125");
                 out.println("296.01386829296877 6070.167242067955 true 57 296.0268859863281 903.5851440429688");
                 out.println("END ENVELOPE");
         }
 
         out.println("END SPECTRUM");
         out.close();
 
     }
 }
