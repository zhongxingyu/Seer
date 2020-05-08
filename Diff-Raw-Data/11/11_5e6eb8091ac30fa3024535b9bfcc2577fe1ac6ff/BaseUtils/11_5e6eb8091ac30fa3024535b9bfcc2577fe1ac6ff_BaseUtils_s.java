 package at.ac.tuwien.swag.webapp.in.base;
 
 import at.ac.tuwien.swag.model.domain.MapUser;
 import at.ac.tuwien.swag.model.domain.StoredRessource;
 
 public class BaseUtils {
 
     public boolean checkRessources(MapUser mapuser, Integer res) {
 
        if (mapuser.getClayRessource().getAmount() < res) {
             return false;
         }
 
        if (mapuser.getGrainRessource().getAmount() < res) {
             return false;
         }
 
        if (mapuser.getIronRessource().getAmount() < res) {
             return false;
         }
 
        if (mapuser.getWoodRessource().getAmount() < res) {
             return false;
         }
 
         return true;
     }
 
     public MapUser reduceRessources(MapUser mapuser, Integer res) {
 
         StoredRessource clay = mapuser.getClayRessource();
         clay.setAmount(clay.getAmount() - res);
 
         StoredRessource wood = mapuser.getWoodRessource();
         wood.setAmount(wood.getAmount() - res);
 
         StoredRessource iron = mapuser.getIronRessource();
         iron.setAmount(iron.getAmount() - res);
 
         StoredRessource grain = mapuser.getGrainRessource();
         grain.setAmount(grain.getAmount() - res);
 
         mapuser.setClayRessource(clay);
         mapuser.setGrainRessource(grain);
         mapuser.setIronRessource(iron);
         mapuser.setWoodRessource(wood);
 
         return mapuser;
     }
 
 }
