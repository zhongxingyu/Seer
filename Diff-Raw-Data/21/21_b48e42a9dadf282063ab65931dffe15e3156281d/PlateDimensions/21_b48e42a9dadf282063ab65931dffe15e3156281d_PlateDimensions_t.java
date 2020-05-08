 package edu.ualberta.med.scannerconfig;
 
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.apache.commons.lang3.tuple.ImmutablePair;
 import org.apache.commons.lang3.tuple.Pair;
 import org.xnap.commons.i18n.I18n;
 import org.xnap.commons.i18n.I18nFactory;
 
 /**
  * The current plate dimensions allowed for decoding from an image.
  * 
  * @author loyola
  * 
  */
 public enum PlateDimensions {
 
    DIM_ROWS_8_COLS_12(
        "ROWS_8_COLS_12",
         new ImmutablePair<Integer, Integer>(8, 12),
         Constants.i18n.tr("8x12")),

    DIM_ROWS_10_COLS_10(
        "ROWS_10_COLS_10",
         new ImmutablePair<Integer, Integer>(10, 10),
        Constants.i18n.tr("10x10")),

    DIM_ROWS_9_COLS_9(
        "ROWS_9_COLS_9",
        new ImmutablePair<Integer, Integer>(9, 9),
        Constants.i18n.tr("9x9")),

    DIM_ROWS_12_COLS_12(
        "ROWS_12_COLS_12",
        new ImmutablePair<Integer, Integer>(12, 12),
        Constants.i18n.tr("12x12"));
 
     private static class Constants {
         private static final I18n i18n = I18nFactory.getI18n(PlateDimensions.class);
     }
 
     private final ImmutablePair<Integer, Integer> dimensions;
     private final String id;
     private final String displayLabel;
 
     private static final Map<String, PlateDimensions> ID_MAP;
 
     static {
         Map<String, PlateDimensions> map = new LinkedHashMap<String, PlateDimensions>();
 
         for (PlateDimensions enumValue : values()) {
             PlateDimensions check = map.get(enumValue.getId());
             if (check != null) {
                 throw new IllegalStateException("plate dimensions value "
                     + enumValue.getId() + " used multiple times");
             }
 
             map.put(enumValue.getId(), enumValue);
         }
 
         ID_MAP = Collections.unmodifiableMap(map);
     }
 
     private PlateDimensions(String id, ImmutablePair<Integer, Integer> dimensions,
         String displayString) {
         this.id = id;
         this.dimensions = dimensions;
         this.displayLabel = displayString;
     }
 
     public String getId() {
         return id;
     }
 
     public String getDisplayLabel() {
         return displayLabel;
     }
 
     public Pair<Integer, Integer> getDimensions() {
         return dimensions;
     }
 
     public Integer getRows() {
         return dimensions.left;
     }
 
     public Integer getCols() {
         return dimensions.right;
     }
 
     public static Map<String, PlateDimensions> valuesMap() {
         return ID_MAP;
     }
 
     public static PlateDimensions getFromIdString(String id) {
         PlateDimensions result = valuesMap().get(id);
         if (result == null) {
             throw new IllegalStateException("invalid plate dimensions: " + id);
         }
         return result;
     }
 }
