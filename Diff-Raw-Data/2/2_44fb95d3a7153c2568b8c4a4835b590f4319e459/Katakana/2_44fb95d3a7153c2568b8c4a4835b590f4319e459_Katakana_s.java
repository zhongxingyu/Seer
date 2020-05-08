 package pgu.client.language;
 
 import java.util.ArrayList;
 
 import pgu.client.utils.guava.HashBiMap;
 import pgu.client.utils.guava.Lists;
 
 public enum Katakana implements HasLevels {
     INSTANCE;
 
     private final HashBiMap<String, String> latin2katakana = HashBiMap.create();
     private final ArrayList<String> availableLevels = Lists.newArrayList( //
             A, K, S, T, N, //
             H, M, Y, R, W, V);
 
     @Override
     public ArrayList<String> availableLevels() {
         return availableLevels;
     }
 
     @Override
     public final HashBiMap<String, String> availableSymbols(final ArrayList<String> selectedLevels) {
         final HashBiMap<String, String> availableSymbols = HashBiMap.create();
         for (final String selectedLevel : selectedLevels) {
 
             if (A.equals(selectedLevel)) {
                 availableSymbols.put("A", latin2katakana.get("A"));
                 availableSymbols.put("I", latin2katakana.get("I"));
                 availableSymbols.put("U", latin2katakana.get("U"));
                 availableSymbols.put("E", latin2katakana.get("E"));
                 availableSymbols.put("O", latin2katakana.get("O"));
                 availableSymbols.put("N", latin2katakana.get("N"));
 
             } else if (K.equals(selectedLevel)) {
                 availableSymbols.put("KA", latin2katakana.get("KA"));
                 availableSymbols.put("KI", latin2katakana.get("KI"));
                 availableSymbols.put("KU", latin2katakana.get("KU"));
                 availableSymbols.put("KE", latin2katakana.get("KE"));
                 availableSymbols.put("KO", latin2katakana.get("KO"));
                 availableSymbols.put("GA", latin2katakana.get("GA"));
                 availableSymbols.put("GI", latin2katakana.get("GI"));
                 availableSymbols.put("GU", latin2katakana.get("GU"));
                 availableSymbols.put("GE", latin2katakana.get("GE"));
                 availableSymbols.put("GO", latin2katakana.get("GO"));
 
             } else if (S.equals(selectedLevel)) {
                 availableSymbols.put("SA", latin2katakana.get("SA"));
                 availableSymbols.put("SHI", latin2katakana.get("SHI"));
                 availableSymbols.put("SU", latin2katakana.get("SU"));
                 availableSymbols.put("SE", latin2katakana.get("SE"));
                 availableSymbols.put("SO", latin2katakana.get("SO"));
                 availableSymbols.put("ZA", latin2katakana.get("ZA"));
                 availableSymbols.put("ZI", latin2katakana.get("ZI"));
                 availableSymbols.put("ZU", latin2katakana.get("ZU"));
                 availableSymbols.put("ZE", latin2katakana.get("ZE"));
                 availableSymbols.put("ZO", latin2katakana.get("ZO"));
 
             } else if (T.equals(selectedLevel)) {
                 availableSymbols.put("TA", latin2katakana.get("TA"));
                 availableSymbols.put("CHI", latin2katakana.get("CHI"));
                 availableSymbols.put("TSU", latin2katakana.get("TSU"));
                 availableSymbols.put("TE", latin2katakana.get("TE"));
                 availableSymbols.put("TO", latin2katakana.get("TO"));
                 availableSymbols.put("DA", latin2katakana.get("DA"));
                 availableSymbols.put("DI", latin2katakana.get("DI"));
                 availableSymbols.put("DU", latin2katakana.get("DU"));
                 availableSymbols.put("DE", latin2katakana.get("DE"));
                 availableSymbols.put("DO", latin2katakana.get("DO"));
 
             } else if (N.equals(selectedLevel)) {
                 availableSymbols.put("NA", latin2katakana.get("NA"));
                 availableSymbols.put("NI", latin2katakana.get("NI"));
                 availableSymbols.put("NU", latin2katakana.get("NU"));
                 availableSymbols.put("NE", latin2katakana.get("NE"));
                 availableSymbols.put("NO", latin2katakana.get("NO"));
 
             } else if (H.equals(selectedLevel)) {
                 availableSymbols.put("HA", latin2katakana.get("HA"));
                 availableSymbols.put("HI", latin2katakana.get("HI"));
                 availableSymbols.put("FU", latin2katakana.get("FU"));
                 availableSymbols.put("HE", latin2katakana.get("HE"));
                 availableSymbols.put("HO", latin2katakana.get("HO"));
                 availableSymbols.put("BA", latin2katakana.get("BA"));
                 availableSymbols.put("BI", latin2katakana.get("BI"));
                 availableSymbols.put("BU", latin2katakana.get("BU"));
                 availableSymbols.put("BE", latin2katakana.get("BE"));
                 availableSymbols.put("BO", latin2katakana.get("BO"));
                 availableSymbols.put("PA", latin2katakana.get("PA"));
                 availableSymbols.put("PI", latin2katakana.get("PI"));
                 availableSymbols.put("PU", latin2katakana.get("PU"));
                 availableSymbols.put("PE", latin2katakana.get("PE"));
                 availableSymbols.put("PO", latin2katakana.get("PO"));
 
             } else if (M.equals(selectedLevel)) {
                 availableSymbols.put("MA", latin2katakana.get("MA"));
                 availableSymbols.put("MI", latin2katakana.get("MI"));
                 availableSymbols.put("MU", latin2katakana.get("MU"));
                 availableSymbols.put("ME", latin2katakana.get("ME"));
                 availableSymbols.put("MO", latin2katakana.get("MO"));
 
             } else if (Y.equals(selectedLevel)) {
                 availableSymbols.put("YA", latin2katakana.get("YA"));
                 availableSymbols.put("YU", latin2katakana.get("YU"));
                 availableSymbols.put("YO", latin2katakana.get("YO"));
 
             } else if (R.equals(selectedLevel)) {
                 availableSymbols.put("RA", latin2katakana.get("RA"));
                 availableSymbols.put("RI", latin2katakana.get("RI"));
                 availableSymbols.put("RU", latin2katakana.get("RU"));
                 availableSymbols.put("RE", latin2katakana.get("RE"));
                 availableSymbols.put("RO", latin2katakana.get("RO"));
 
             } else if (W.equals(selectedLevel)) {
                 availableSymbols.put("WA", latin2katakana.get("WA"));
                 availableSymbols.put("WI", latin2katakana.get("WI"));
                 availableSymbols.put("WE", latin2katakana.get("WE"));
                 availableSymbols.put("WO", latin2katakana.get("WO"));
 
             } else if (V.equals(selectedLevel)) {
                 availableSymbols.put("VU", latin2katakana.get("VU"));
                 availableSymbols.put("VA", latin2katakana.get("VA"));
                 availableSymbols.put("VI", latin2katakana.get("VI"));
                 availableSymbols.put("VE", latin2katakana.get("VE"));
                 availableSymbols.put("VO", latin2katakana.get("VO"));
 
             } else {
                 throw new IllegalArgumentException("Unknown level: " + selectedLevel);
             }
         }
         return availableSymbols;
     }
 
     private static final String A = "A";
     private static final String K = "K - G";
     private static final String S = "S - Z";
     private static final String T = "T - D";
     private static final String N = "N";
     private static final String H = "H - B - P";
     private static final String M = "M";
     private static final String Y = "Y";
     private static final String R = "R";
     private static final String W = "W";
     private static final String V = "V";
 
     {
         latin2katakana.put("A", "&#x30A2;");
         latin2katakana.put("I", "&#x30A4;");
         latin2katakana.put("U", "&#x30A6;");
         latin2katakana.put("E", "&#x30A8;");
         latin2katakana.put("O", "&#x30AA;");
         latin2katakana.put("KA", "&#x30AB;");
         latin2katakana.put("KI", "&#x30AD;");
         latin2katakana.put("KU", "&#x30AF;");
         latin2katakana.put("KE", "&#x30B1;");
         latin2katakana.put("KO", "&#x30B3;");
         latin2katakana.put("GA", "&#x30AC;");
         latin2katakana.put("GI", "&#x30AE;");
        latin2katakana.put("GU", "&#x30A0;");
         latin2katakana.put("GE", "&#x30B2;");
         latin2katakana.put("GO", "&#x30B4;");
         latin2katakana.put("SA", "&#x30B5;");
         latin2katakana.put("SHI", "&#x30B7;");
         latin2katakana.put("SU", "&#x30B9;");
         latin2katakana.put("SE", "&#x30BB;");
         latin2katakana.put("SO", "&#x30BD;");
         latin2katakana.put("ZA", "&#x30B6;");
         latin2katakana.put("ZI", "&#x30B8;");
         latin2katakana.put("ZU", "&#x30BA;");
         latin2katakana.put("ZE", "&#x30BC;");
         latin2katakana.put("ZO", "&#x30BE;");
         latin2katakana.put("TA", "&#x30BF;");
         latin2katakana.put("CHI", "&#x30C1;");
         latin2katakana.put("TSU", "&#x30C4;");
         latin2katakana.put("TE", "&#x30C6;");
         latin2katakana.put("TO", "&#x30C8;");
         latin2katakana.put("DA", "&#x30C0;");
         latin2katakana.put("DI", "&#x30C2;");
         latin2katakana.put("DU", "&#x30C5;");
         latin2katakana.put("DE", "&#x30C7;");
         latin2katakana.put("DO", "&#x30C9;");
         latin2katakana.put("NA", "&#x30CA;");
         latin2katakana.put("NI", "&#x30CB;");
         latin2katakana.put("NU", "&#x30CC;");
         latin2katakana.put("NE", "&#x30CD;");
         latin2katakana.put("NO", "&#x30CE;");
         latin2katakana.put("HA", "&#x30CF;");
         latin2katakana.put("HI", "&#x30D2;");
         latin2katakana.put("HE", "&#x30D8;");
         latin2katakana.put("HO", "&#x30DB;");
         latin2katakana.put("FU", "&#x30D5;");
         latin2katakana.put("BA", "&#x30D0;");
         latin2katakana.put("BI", "&#x30D3;");
         latin2katakana.put("BU", "&#x30D6;");
         latin2katakana.put("BE", "&#x30D9;");
         latin2katakana.put("BO", "&#x30DC;");
         latin2katakana.put("PA", "&#x30D1;");
         latin2katakana.put("PI", "&#x30D4;");
         latin2katakana.put("PU", "&#x30D7;");
         latin2katakana.put("PE", "&#x30DA;");
         latin2katakana.put("PO", "&#x30DD;");
         latin2katakana.put("MA", "&#x30DE;");
         latin2katakana.put("MI", "&#x30DF;");
         latin2katakana.put("MU", "&#x30E0;");
         latin2katakana.put("ME", "&#x30E1;");
         latin2katakana.put("MO", "&#x30E2;");
         latin2katakana.put("YA", "&#x30E4;");
         latin2katakana.put("YU", "&#x30E6;");
         latin2katakana.put("YO", "&#x30E8;");
         latin2katakana.put("RA", "&#x30E9;");
         latin2katakana.put("RI", "&#x30EA;");
         latin2katakana.put("RU", "&#x30EB;");
         latin2katakana.put("RE", "&#x30EC;");
         latin2katakana.put("RO", "&#x30ED;");
         latin2katakana.put("WA", "&#x30EF;");
         latin2katakana.put("WI", "&#x30F0;");
         latin2katakana.put("WE", "&#x30F1;");
         latin2katakana.put("WO", "&#x30F2;");
         latin2katakana.put("N", "&#x30F3;");
         latin2katakana.put("VU", "&#x30F4;");
         latin2katakana.put("VA", "&#x30F7;");
         latin2katakana.put("VI", "&#x30F8;");
         latin2katakana.put("VE", "&#x30F9;");
         latin2katakana.put("VO", "&#x30FA;");
     }
 
 }
