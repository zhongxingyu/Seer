 package ru.spbau.bioinf.tagfinder;
 
 import ru.spbau.bioinf.tagfinder.util.ReaderUtil;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.ArrayList;
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
        peaks.add(new Peak(0, 0 , 0));
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
             peaks.add(new Peak(mass, score , charge));
         }
        peaks.add(new Peak(precursorMass, 0, 0));
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
 
 }
