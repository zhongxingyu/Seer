 package ep.geoschem;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import com.fasterxml.jackson.annotation.JsonIgnore;
 import com.fasterxml.jackson.annotation.JsonInclude;
 import com.fasterxml.jackson.annotation.JsonProperty;
 import com.fasterxml.jackson.annotation.JsonPropertyOrder;
 import com.fasterxml.jackson.databind.JavaType;
 import com.fasterxml.jackson.databind.MappingIterator;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.google.common.base.Splitter;
 import com.google.common.base.Strings;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 import ep.common.*;
 import ucar.ma2.InvalidRangeException;
 
 
 @JsonPropertyOrder({
   "root", "conf", "emissions", "species", "sectors",
   "esconf", "beginYear", "endYear", "yearmap"})
 @JsonInclude(JsonInclude.Include.NON_NULL)
 public class GCConfiguration extends ep.common.Configuration{
   public String defaultEmission;
   public GridFactor csvTimeFactor;
   public VocFactor vocFactor;
 
   // @JsonIgnore
   public String species[];
   public String vocSpecies[];
   // @JsonIgnore
   public String sectors[];
   // @JsonIgnore
   public String emissions[];
   @JsonIgnore public Map<String, Source> emissionSources;
   //@JsonIgnore
   public Map<String, SectorTable> sectorMapper;
   //@JsonIgnore
   @JsonProperty("yearmap")
   public Map<String, String> yearIndex;
   //@JsonIgnore
   public int beginYear;
   //@JsonIgnore
   public int endYear;
 
   public List<Target> targets;
 
   @JsonProperty("esconf")
   public Map<String, SourceConfig> emissionConfigs;
 
   public static class SectorTable {
     public Map<String, String[]> sectors;
   }
 
 
   public static GCConfiguration load(File filePath) throws IOException {
     return new ObjectMapper().readValue(filePath, GCConfiguration.class);
   }
 
 
   public static GCConfiguration load(String json) throws IOException {
     return new ObjectMapper().readValue(json, GCConfiguration.class);
   }
 
 
   File getConfFile(String fn) {
     return new File(conf, fn);
   }
 
 
   void initEmissionSources() throws IOException, InvalidRangeException {
     emissionSources = new HashMap<>();
     emissions = Iterables.toArray(emissionConfigs.keySet(), String.class);
 
     Arrays.sort(emissions);
     emissionSources = new HashMap<>();
     emissions = Iterables.toArray(emissionConfigs.keySet(), String.class);
 
     Arrays.sort(emissions);
 
     for (Map.Entry<String, SourceConfig> e : emissionConfigs.entrySet()) {
       Source es = null;
       SourceConfig esc = e.getValue();
 
       esc.up = this;
 
       esc.vocFactor = vocFactor;
 
       // simple scalar timefactor.
       if (esc.timeFactorType == null)
         esc.timeFactorType = "csv";
       if ("csv".equals(esc.timeFactorType))
         esc.timeFactor = csvTimeFactor;
 
       if (esc instanceof FileSystemSourceConfig) {
         es = new FileSystemSource((FileSystemSourceConfig) esc);
       }
 
       if (es != null) {
         emissionSources.put(e.getKey(), es);
       }
     }
   }
 
 
   void initSectorMapper() throws IOException {
     HashSet<String> species = new HashSet<>();
     HashSet<String> sectors = new HashSet<>();
     sectorMapper = new HashMap<>();
 
     MappingIterator<Map<String, String>> it = CsvUtil.read(getConfFile("sector_map.csv"));
     Splitter splitter = Splitter.on(',').trimResults();
 
     while (it.hasNext()) {
       Map<String, String> row = it.next();
       String sp = row.get("SPECIES");
       String es = row.get("SOURCE");
       String st = row.get("SECTOR");
       String ss = row.get("SOURCESECTORS");
 
       if (Strings.isNullOrEmpty(sp))
         continue;
       if (Strings.isNullOrEmpty(ss))
         continue;
       if (sp.startsWith("#"))
         continue;
 
       species.add(sp);
       sectors.add(st);
 
       String key = sp + "," + st;
       SectorTable sm = sectorMapper.get(key);
       if (sm == null) {
         sm = new SectorTable();
         sm.sectors = new HashMap<>();
         sectorMapper.put(key, sm);
       }
 
       String[] s = Iterables.toArray(splitter.split(ss), String.class);
       sm.sectors.put(es, s);
     }
 
     this.sectors = Iterables.toArray(sectors, String.class);
     this.species = Iterables.toArray(species, String.class);
   }
 
 
   void initYearIndex() throws IOException {
     yearIndex = new HashMap<>();
     MappingIterator<Map<String, String>> it = CsvUtil.read(getConfFile("year_source.csv"));
 
     beginYear = 99999;
     endYear = 1;
 
     while(it.hasNext()) {
       Map<String, String> row = it.next();
       String year = row.get("YEAR");
       int nYear = Integer.parseInt(year);
       if (nYear > endYear) {
         endYear = nYear;
       }
 
       if (nYear < beginYear) {
         beginYear = nYear;
       }
 
       for (String es: emissions) {
         String k = row.get(es);
         if (k == null || k.equals("0"))
           continue;
 
         String key = es + "," + year;
         yearIndex.put(key, year);
       }
     }
 
     for (String es: emissions) {
       for (int y = beginYear; y <= endYear; ++y) {
         String year = Integer.toString(y);
         String key = es + "," + year;
 
 
         if (yearIndex.get(key) == null) {
           String tYear = null;
           String iYear;
           String sKey;
           String sYear;
 
           // scan forward
           for (int sy = y + 1; sy <= endYear; ++sy) {
             sYear = Integer.toString(sy);
             sKey = es + "," + sYear;
             iYear = yearIndex.get(sKey);
             if (iYear != null) {
               tYear = iYear;
               break;
             }
           }
 
           if (tYear != null) {
             yearIndex.put(key, tYear);
             continue;
           }
 
           for (int sy = y - 1; sy >= beginYear; --sy) {
             sYear = Integer.toString(sy);
             sKey = es + "," + sYear;
             iYear = yearIndex.get(sKey);
             if (iYear != null) {
               tYear = iYear;
               break;
             }
           }
 
           yearIndex.put(key, tYear);
         }
       }
     }
   }
 
 
   public void initCsvTimeFactor() throws IOException {
     ScalarMonthlyTimeFactor timeFactor = new ScalarMonthlyTimeFactor();
     timeFactor.init(getConfFile("timefactor_monthly.csv"));
     this.csvTimeFactor = timeFactor;
   }
 
 
   public void initVocFactor() throws IOException {
     VocFactor factor = new VocFactor();
     factor.init(getConfFile("vocfactor.csv"));
     if (vocSpecies != null) {
       vocFactor.vocs = Sets.newHashSet(vocSpecies);
     } else {
      vocSpecies = (String[])vocFactor.vocs.toArray();
     }
     this.vocFactor = factor;
   }
 
 
   public void init() throws IOException, InvalidRangeException {
     initCsvTimeFactor();
     initVocFactor();
     initEmissionSources();
     initSectorMapper();
     initYearIndex();
   }
 
 
   public Source getEmissionSource(String emission) {
     return emissionSources.get(emission);
   }
 
 
   public String getYearIndex(String emission, String year) {
     return yearIndex.get(emission + "," + year);
   }
 
   public String[] getSourceSectors(String species, String sector, String emissionSource) {
     String key = species + "," + sector;
     SectorTable st = sectorMapper.get(key);
     if (st == null)
       return null;
     return st.sectors.get(emissionSource);
   }
 
   public void loadTargetConfig() throws IOException {
     ObjectMapper mapper = new ObjectMapper();
     JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, Target.class);
     targets = mapper.readValue(getConfFile("target.js"), type);
     for (Target t: targets) {
       t.init();
       t.up = this;
     }
   }
 }
