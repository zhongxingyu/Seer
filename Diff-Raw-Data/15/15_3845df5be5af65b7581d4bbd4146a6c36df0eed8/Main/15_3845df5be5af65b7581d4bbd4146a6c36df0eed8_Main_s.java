 package ru.exorg.processing;
 
 import org.springframework.beans.factory.InitializingBean;
 import ru.exorg.core.model.City;
 import ru.exorg.core.model.Location;
 import ru.exorg.core.model.POI;
 import ru.exorg.core.service.CafeProvider;
 import ru.exorg.core.service.DataProvider;
 import ru.exorg.core.service.POIProvider;
 
 import java.lang.Exception;
 import java.lang.String;
 import java.net.SocketTimeoutException;
 import java.util.*;
 
 import org.tartarus.snowball.ext.russianStemmer;
 
 // ================================================================================
 
 final public class Main implements InitializingBean {
     private DataProvider dataProvider;
     private POIProvider poiProvider;
     private CafeProvider cafeProvider;
 
     private GeoService geoService;
     private Clustering clustering;
 
     private List<POI> pois;
 
     private int clusterLevel = 1;
     private double distLim = 10000;
 
     private static Set<String> prohibitedStems
             = toSet(new String[]{"дом", "особ", "мост", "здан", "двор", "театр", "особняк", "церков", "ансамбл"});
 
     private static Set<String> toSet(final String[] strings) {
         Set<String> res = new HashSet<String>();
 
         for (String s : strings) {
             res.add(s);
         }
 
         return res;
     }
 
 
     public void setGeoService(GeoService gs) {
         this.geoService = gs;
     }
 
     public void setDataProvider(DataProvider p) {
         this.dataProvider = p;
         this.poiProvider = p.getPOIProvider();
         this.cafeProvider = p.getCafeProvider();
     }
 
     public void setClusteringService(Clustering c) {
         this.clustering = c;
     }
 
 
     public void setClusterLevel(int cl) {
         this.clusterLevel = cl;
     }
 
 
     private void addGeoInfo(POI poi) throws Exception {
         if (!poi.getLocation().isValid()) {
             if (poi.hasAddress()) {
                 System.out.println("Quering for " + poi.getLocation().getAddress() + " (" + poi.getName() + ")");
             } else {
                 System.out.println("Quering for " + poi.getName());
             }
 
             List<Location> locs = this.geoService.lookupLocation(poi.getLocation(), poi.getName());
             if (locs != null) {
                 for (Location loc : locs) {
                     if (this.dataProvider.isWithinCity(poi.getLocation().getCityId(), loc)) {
                         double lat = loc.getLat();
                         double lng = loc.getLng();
 
                         poi.getLocation().setAddress(loc.getAddress());
                         poi.getLocation().setLat(lat);
                         poi.getLocation().setLng(lng);
 
                         City c = this.dataProvider.queryCity(poi.getCityId());
                         int sqId = (int)(Math.abs(lat - c.getNeLatLng().getLat())/c.getLngSubdivLen()*c.getLatSubdivs()
                                 +
                                 Math.abs(lng - c.getSwLatLng().getLng())/c.getLatSubdivLen() + 1);
 
                         poi.setSquareId(sqId);
                         break;
                     }
                 }
             }
 
             if (!poi.getLocation().isValid()) {
                 System.out.println("Failed");
             }
 
             Thread.sleep(500);
         }
     }
 
     private void guessType(POI poi) throws Exception {
         if (!poi.hasType()) {
             dataProvider.guessPOIType(poi);
         }
     }
 
     private boolean isLike(final String pn1, final String pn2) {
         return Util.getLevenshteinDistance(pn1, pn2) <= 6;
     }
 
     private int edist(final String s1, final String s2) {
         return Util.getLevenshteinDistance(s1, s2);
     }
 
 
     private void clusterize1() {
         for (POI poi : this.pois) {
             if (poi.hasAddress()) {
                 List<POI> poiList = this.poiProvider.queryByAddress(poi.getAddress());
 
                 if (poiList != null) {
                     if (!this.clustering.isInCluster(poiList.get(0)) && poiList.size() >= 2) {
                         long cid = this.clustering.getMaxClusterId() + 1;
                         for (POI p : poiList) {
                             this.clustering.setPOICluster(p, cid);
                         }
                     }
                 }
             }
         }
 
         Clustering.Clusters clusters = this.clustering.getClusters();
         for (List<Long> cluster : clusters.values()) {
             for (int i = 0; i < cluster.size(); i++) {
                 boolean remove = true;
                 POI curPOI = this.poiProvider.queryById(cluster.get(i));
                 String cur = curPOI.getName();
 
                 for (int j = i + 1; j < cluster.size(); j++) {
                     String other = this.poiProvider.queryById(cluster.get(j)).getName();
 
                     if (isLike(cur, other)) {
                         remove = false;
                         break;
                     }
                 }
 
                 if (remove) {
                     this.clustering.removeFromCluster(curPOI);
                 }
             }
         }
     }
 
     private void clusterize2() {
         russianStemmer stemmer = new russianStemmer();
 
         for (POI poi : this.pois) {
             if (!this.clustering.isInCluster(poi) && !poi.getLocation().isValid()) {
                 String[] ws = poi.getName().split("\\s+");
 
                 for (String w : ws) {
                     stemmer.setCurrent(w);
                     stemmer.stem();
                     String stem = stemmer.getCurrent().toLowerCase();
 
                     if (prohibitedStems.contains(stem) || stem.length() <= 3) {
                         continue;
                     }
 
                     List<POI> likes = this.poiProvider.queryLike(stem);
                     for (POI like : likes) {
                         if (edist(poi.getName(), like.getName()) < 6 && like.getLocation().isValid()) {
                             if (this.clustering.isInCluster(like)) {
                                 this.clustering.setPOICluster(poi, this.clustering.getPOICluster(like));
                                 break;
                             } else {
                                 long cid = this.clustering.getMaxClusterId() + 1;
                                 this.clustering.setPOICluster(poi, cid);
                                 this.clustering.setPOICluster(like, cid);
                                 break;
                             }
                         }
                     }
                 }
             }
         }
     }
 
     private void processPOI() throws Exception {
         this.clustering.clearClusters();
 
         for (POI poi : this.pois) {
             try {
                 this.addGeoInfo(poi);
                 this.guessType(poi);
 
                 this.poiProvider.sync(poi);
             } catch (SocketTimeoutException e) {
                 System.out.println("Failed to retrieve geographic information for " + poi.getName());
             }
         }
 
 
         if (this.clusterLevel >= 1) {
             this.clusterize1();
             this.clusterize2();
             this.clustering.commitClusters();
         }
     }
 
     public void afterPropertiesSet() {
         try {
             /*
             List<POI> l = this.poiProvider.queryLike("марс");
             for (POI i : l) {
                 System.out.println(i.getName());
             }
             */
 
             this.pois = this.poiProvider.poiList();
 
             processPOI();
             //processCafes();
         } catch (Exception e) {
             System.out.println(e.toString());
             e.printStackTrace();
         }
     }
 }
