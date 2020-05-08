 package ep.geoschem.builder;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import ep.common.ESID;
 import ep.common.Grid;
 import ep.common.GridSet;
 import ep.common.Grids;
 import ep.geoschem.GCConfiguration;
 import ep.geoschem.Target;
 
 public class GridBuilder {
   GCConfiguration cf;
   Target target;
 
   GridSetBuilder parent;
   DataSetBuilder root;
 
   public GridBuilder(GridSetBuilder parent) {
     this.parent = parent;
     this.root = parent.getParent();
 
     this.cf = root.getConf();
     this.target = root.getTarget();
   }
 
   public void build(GridSet gs, String varName, ESID esid) throws Exception {
     Grid resG = Grids.empty(target.shape);
 
     for (String sn : cf.emissions) {
       if (target.enabledSet != null && !target.enabledSet.contains(sn)) {
         continue;
       }
 
       Grid maskArray = root.getMaskArray(sn);
 
      String[] ss;
      if (cf.vocFactor.isVoc(esid.species)) {
        ss = cf.getSourceSectors(cf.vocFactor.species, esid.sector, sn);
      } else {
        ss = cf.getSourceSectors(esid.species, esid.sector, sn);
       }

      if (ss == null)
        continue;

       //System.out.format("build %s: %s, %s, %s - %s {%s}\n", varName,
       //  esid.date, esid.species, esid.sector, sn, Joiner.on(", ").join(ss));
 
       String year = esid.getYear();
 
       Grid eg;
 
       if (cf.getYearIndex(sn, year).equals(year)) {
         eg = Grids.getCombinedGridding(target.shape,
           cf.getEmissionSource(sn), sn, esid.date, esid.species, ss);
       } else {
         String yearBase = cf.getYearIndex(sn, year);
         String des = cf.defaultEmission;
         String[] dss = cf.getSourceSectors(esid.species, esid.sector, des);
 
         Grid baseFactor = Grids.getCombinedGridding(target.shape,
           cf.getEmissionSource(des), des, yearBase, esid.species, dss);
 
         Grid targetFactor = Grids.getCombinedGridding(target.shape,
           cf.getEmissionSource(des), des, year, esid.species, dss);
 
 
         if (esid.isYearly()) {
           eg = Grids.getCombinedGridding(target.shape,
             cf.getEmissionSource(sn), sn, yearBase, esid.species, ss);
         } else {
           eg = Grids.getCombinedGridding(target.shape,
             cf.getEmissionSource(sn), sn, yearBase + esid.getMonth(), esid.species, ss);
         }
         eg.floatScale2(baseFactor, targetFactor);
       }
 
       if (maskArray != null) {
         resG.floatPlusWithFactor(eg, maskArray);
       } else {
         resG.floatPlus(eg);
       }
     }
 
     Map<String, String> attrs = new HashMap<>();
 
     attrs.put("long_name", varName);
     attrs.put("short_name", esid.name + "__" + varName);
     attrs.put("unit", "mg yr-1");
 
     gs.addGridding(varName, attrs, resG);
   }
 }
