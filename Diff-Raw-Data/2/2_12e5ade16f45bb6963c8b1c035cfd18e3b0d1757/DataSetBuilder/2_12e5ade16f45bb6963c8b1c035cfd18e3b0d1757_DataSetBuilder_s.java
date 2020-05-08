 package ep.geoschem.builder;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import com.google.common.base.Splitter;
 import com.google.common.collect.Lists;
 import ep.common.DateRange;
 import ep.common.ESID;
 import ep.common.Grid;
 import ep.geoschem.GCConfiguration;
 import ep.geoschem.Target;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.stringtemplate.v4.ST;
 
 
 public class DataSetBuilder {
   GCConfiguration conf;
   List<Target> targets;
   List<TargetHelper> helpers;
 
   Map<String, List<SubTarget>> gridClusters;
   Map<String, Grid> maskArrays;
 
   static final Logger logger = LoggerFactory.getLogger(DataSetBuilder.class);
 
   public GCConfiguration getConf() {
     return conf;
   }
 
 
   public List<SubTarget> getGridCluster(String fn) {
     return gridClusters.get(fn);
   }
 
 
   public DataSetBuilder(GCConfiguration conf) {
     this.conf = conf;
     this.targets = conf.targets;
 
   }
 
   void initTargetHelpers() {
     this.helpers = new ArrayList(this.targets.size());
 
     for (Target target: this.targets) {
       TargetHelper helper = new TargetHelper(conf, target);
       helper.initMaskArrays();
      helpers.add(new TargetHelper(conf, target));
     }
   }
 
 
   public void initGridCluster() {
     gridClusters = new HashMap<>();
 
     for (TargetHelper helper : helpers) {
       Target target = helper.getTarget();
 
       DateRange range = new DateRange(target.beginDate, target.endDate);
       Splitter splitter = Splitter.on("|||");
 
       List<String> speciesList;
 
       if (target.species != null) {
         speciesList = Lists.newArrayList(target.species);
       } else {
         speciesList = Lists.newArrayList(conf.species);
 
         if (conf.vocSpecies != null) {
           speciesList.addAll(Lists.newArrayList(conf.vocSpecies));
         }
       }
 
       for (String date : range) {
         for (String sector : conf.sectors) {
           for (String species : speciesList) {
             ESID esid = new ESID(target.name, date, species, sector);
 
             ST st = new ST(target.pathTemplate);
             st.add("cf", conf);
             st.add("ta", target);
             st.add("es", esid);
             String oPath = st.render();
 
             List<String> oPathTokens = Lists.newArrayList(splitter.split(oPath));
             String ncPath = oPathTokens.get(0);
 
             List<SubTarget> cluster = gridClusters.get(ncPath);
 
             if (cluster == null) {
               cluster = new LinkedList<>();
               gridClusters.put(ncPath, cluster);
             }
             cluster.add(new SubTarget(helper, esid));
           }
         }
       }
     }
   }
 
 
 
   public void build() throws Exception {
     initTargetHelpers();
     initGridCluster();
     ArrayList<String> ncFiles = new ArrayList<>(gridClusters.keySet());
     Collections.sort(ncFiles);
 
     GridSetBuilder gridSetBuilder = new GridSetBuilder(this);
     for (String ncFile : ncFiles) {
       gridSetBuilder.build(ncFile);
     }
   }
 }
