 import java.util.ArrayList;
 import java.util.List;
 
 public class Map {
     private List<Site> sites;
     private MapLayout mapLayout;
 
     public static Map RichMap() {
         List<Site> sites = new ArrayList();
 
         // Starting
         sites.add(new StartingSite());
 
         // Block 1
         for (int index = 0; index < 13; index++){
             sites.add(new Property(new Land(200)));
         }
 
         // Hospital
         sites.add(new HospitalSite());
 
         // Block 2
         for (int index = 0; index < 13; index++){
             sites.add(new Property(new Land(200)));
         }
 
         // Tool house
         sites.add(new ToolHouseSite());
 
         // Block 3
         for (int index = 0; index < 6; index++){
             sites.add(new Property(new Land(500)));
         }
 
         // Gift house
         sites.add(new GiftHouseSite());
 
         // Block 4
         for (int index = 0; index < 13; index++){
             sites.add(new Property(new Land(300)));
         }
 
         // Prison
         sites.add(new PrisonSite());
 
         // Block 5
         for (int index = 0; index < 13; index++){
             sites.add(new Property(new Land(300)));
         }
 
         // Magic house
         sites.add(new MagicHouseSite());
 
         // PointMine Site
         sites.add(new PointMineSite(20));
         sites.add(new PointMineSite(80));
         sites.add(new PointMineSite(100));
         sites.add(new PointMineSite(40));
         sites.add(new PointMineSite(80));
         sites.add(new PointMineSite(60));
 
         return new Map(sites, new MapLayout(29, 8));
     }
 
     public Map(List<Site> sites, MapLayout mapLayout) {
         this.mapLayout = mapLayout;
         setSites(sites);
     }
 
     private void setSites(List<Site> sites) {
         this.sites = sites;
         for (int index = 0; index < size(); index++) {
             Site site = sites.get(index);
             site.setMap(this);
             site.setIndex(index);
         }
     }
 
     public int size() {
         return sites.size();
     }
 
     public Site getSite(int index) {
         return sites.get(index);
     }
 
     public String display() {
         return mapLayout.display(this);
     }
 
     public void setSite(int index, Site site) {
        site.setIndex(index);
         sites.set(index, site);
     }
 }
