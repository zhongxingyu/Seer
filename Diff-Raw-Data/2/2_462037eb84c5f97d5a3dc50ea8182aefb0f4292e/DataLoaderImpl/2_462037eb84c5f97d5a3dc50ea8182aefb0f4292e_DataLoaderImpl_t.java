 /**
  * Apache License 2.0
  */
 package com.googlecode.madschuelerturnier.business;
 
 import com.google.common.io.Resources;
 import com.googlecode.madschuelerturnier.business.xls.FromXLSLoader;
 import com.googlecode.madschuelerturnier.model.Mannschaft;
 import com.googlecode.madschuelerturnier.model.Spiel;
 import com.googlecode.madschuelerturnier.model.enums.GeschlechtEnum;
 import org.apache.log4j.Logger;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * Dient dazu zuvor gespeicherte XLS Spiele zu laden
  *
  * @author $Author: marthaler.worb@gmail.com $
  * @since 0.7
  */
 public final class DataLoaderImpl implements DataLoader {
 
     private static final Logger LOG = Logger.getLogger(DataLoaderImpl.class);
 
     private int jahr;
 
     private FromXLSLoader xls = new FromXLSLoader();
 
     public static DataLoader getDataLoader() {
         return new DataLoaderImpl(2013);
     }
 
     public static DataLoader getDataLoader(int jahr) {
         return new DataLoaderImpl(jahr);
     }
 
     private DataLoaderImpl(int jahr) {
         this.jahr = jahr;
     }
 
     @Override
     public List<Mannschaft> loadMannschaften() {
         return xls.convertXLSToMannschaften(readFile("schuetu-" + jahr + ".xls"));
     }
 
     @Override
     public List<Spiel> loadSpiele() {
         return xls.convertXLSToSpiele(readFile("schuetu-" + jahr + ".xls"));
     }
 
     @Override
     public List<Mannschaft> loadMannschaften(boolean knaben, boolean maedchen, Integer... klassenIn) {
 
         List<Mannschaft> result = new ArrayList<Mannschaft>();
 
         List<Mannschaft> temp = loadMannschaften();
         List<Integer> klassen = null;
 
        if (klassenIn != null && klassenIn.length > 0) {
             klassen = Arrays.asList(klassenIn);
         }
 
         for (Mannschaft mannschaft : temp) {
             if (knaben) {
                 if (mannschaft.getGeschlecht() == GeschlechtEnum.K && containsKlasse(mannschaft, klassen)) {
                     result.add(mannschaft);
                 }
             }
             if (maedchen) {
                 if (mannschaft.getGeschlecht() == GeschlechtEnum.M && containsKlasse(mannschaft, klassen)) {
                     result.add(mannschaft);
                 }
             }
 
         }
 
         return result;
     }
 
     private boolean containsKlasse(Mannschaft mannschaft, List<Integer> klassen) {
         return klassen == null || klassen.contains(mannschaft.getKlasse());
     }
 
     public static byte[] readFile(String file) {
         byte[] in = null;
         URL url = Resources.getResource(file);
         try {
             in = Resources.toByteArray(url);
         } catch (IOException e) {
             LOG.error(e.getMessage(), e);
         }
         return in;
     }
 }
