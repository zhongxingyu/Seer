 import java.util.*;
 
 public class Map {
     private List<Site> sites;
     private MapLayout mapLayout;
 
     public Map(List<Site> sites, MapLayout mapLayout) {
         this.mapLayout = mapLayout;
         setSites(sites);
     }
 
     private void setSites(List<Site> sites) {
         this.sites = sites;
         for(int index = 0; index < size();index++) {
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
        sites.set(index, site);
        site.setMap(this);
        site.setIndex(index);
    }
 }
