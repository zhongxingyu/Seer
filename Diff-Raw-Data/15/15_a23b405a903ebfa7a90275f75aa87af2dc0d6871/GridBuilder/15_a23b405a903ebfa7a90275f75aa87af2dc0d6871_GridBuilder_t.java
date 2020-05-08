 package ep.geoschem.builder;
 
 import com.google.common.base.Joiner;
 import ep.common.ESID;
 import ep.common.Grid;
 import ep.common.GridSet;
 import ep.common.Grids;
 import ep.geoschem.GCConfiguration;
 import ep.geoschem.Target;
 
 public class GridBuilder {
   GCConfiguration conf;
   Target target;
 
   GridSetBuilder parent;
   DataSetBuilder root;
 
   public GridBuilder(GridSetBuilder parent) {
     this.parent = parent;
     this.root = parent.getParent();
 
     this.conf = root.getConf();
     this.target = root.getTarget();
   }
 
   public void build(GridSet gs, String varName, ESID esid) throws Exception {
 
     Grid resG = Grids.empty(target.shape);

     for (String es: conf.emissions) {
       if (target.enabledSet != null && !target.enabledSet.contains(es)) {
         continue;
       }
 
       String[] ss = conf.getSourceSectors(esid.species, esid.sector, es);
       if (ss == null)
         continue;
 
       System.out.format("build %s: %s, %s, %s - %s {%s}\n", varName,
         esid.date, esid.species, esid.sector, es, Joiner.on(", ").join(ss));
 
       String year = esid.getYear();
 
       if (conf.getYearIndex(es, year).equals(year)) {
         Grid eg = Grids.getCombinedGridding(target.shape,
           conf.getEmissionSource(es), es, esid.date, esid.species, ss);
         resG.floatPlus(eg);
         continue;
       }
 
       String yearBase = conf.getYearIndex(es, year);
       String des = conf.defaultEmission;
       String[] dss = conf.getSourceSectors(esid.species, esid.sector, des);
 
       Grid baseFactor = Grids.getCombinedGridding(target.shape,
         conf.getEmissionSource(des), des, yearBase, esid.species, dss);

       Grid targetFactor = Grids.getCombinedGridding(target.shape,
         conf.getEmissionSource(des), des, year, esid.species, dss);

      Grid eg;
      if (esid.isYearly()) {
        eg = Grids.getCombinedGridding(target.shape,
          conf.getEmissionSource(es), es, yearBase, esid.species, ss);
      } else {
        eg = Grids.getCombinedGridding(target.shape,
          conf.getEmissionSource(es), es, yearBase + esid.getMonth(), esid.species, ss);
      }
 
       eg.floatScale2(baseFactor, targetFactor);
       resG.floatPlus(eg);
     }
     // TODO more var's attribute.
     gs.addGridding(varName, null, resG);
   }
 }
