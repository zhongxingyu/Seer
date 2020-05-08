 /*
  * MyResidence, Bukkit plugin for managing your towns and residences
  * Copyright (C) 2011, Michael Hohl
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package at.co.hohl.myresidence.bukkit.persistent;
 
 import at.co.hohl.myresidence.Nation;
 import at.co.hohl.myresidence.TownManager;
 import at.co.hohl.myresidence.storage.persistent.Inhabitant;
 import at.co.hohl.myresidence.storage.persistent.Major;
 import at.co.hohl.myresidence.storage.persistent.Residence;
 import at.co.hohl.myresidence.storage.persistent.Town;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Implementation of town manager.
  *
  * @author Michael Hohl
  */
 public class PersistTownManager extends PersistTownFlagManager implements TownManager {
   /**
    * Creates a new FlagManager implementation.
    *
    * @param nation nation which holds the town.
    * @param town   the town to manage.
    */
   public PersistTownManager(Nation nation, Town town) {
     super(nation, town);
   }
 
   /**
    * Adds a major for the town.
    *
    * @param inhabitant the inhabitant to add as major.
    */
   public void addMajor(Inhabitant inhabitant) {
     Major major = nation.getDatabase().find(Major.class)
             .where()
             .eq("inhabitantId", inhabitant.getId())
             .eq("townId", town.getId())
             .findUnique();
 
     if (major == null) {
       major = new Major();
       major.setInhabitantId(inhabitant.getId());
       major.setTownId(town.getId());
     }
 
     nation.getDatabase().save(major);
   }
 
   /**
    * Removes a major of the town.
    *
    * @param inhabitant the inhabitant to remove as major.
    */
   public void removeMajor(Inhabitant inhabitant) {
     List<Major> majorsToRemove = nation.getDatabase().find(Major.class)
             .where().eq("townId", town.getId()).eq("inhabitantId", inhabitant.getId()).findList();
 
     nation.getDatabase().delete(majorsToRemove);
   }
 
   /**
    * Checks if the inhabitant is a major in the town.
    *
    * @param inhabitant the inhabitant to check.
    * @return true, if the inhabitant is major.
    */
   public boolean isMajor(Inhabitant inhabitant) {
     return nation.getDatabase().find(Major.class)
             .where().eq("townId", town.getId()).eq("inhabitantId", inhabitant.getId())
             .findRowCount() > 0;
   }
 
   /**
    * @return the major of the town.
    */
   public List<Inhabitant> getMajors() {
     List<Major> majors = nation.getDatabase().find(Major.class)
             .where()
             .eq("townId", town.getId())
             .findList();
 
     List<Inhabitant> inhabitants = new LinkedList<Inhabitant>();
     for (Major major : majors) {
       inhabitants.add(nation.getInhabitant(major.getInhabitantId()));
     }
 
     return inhabitants;
   }
 
   /**
    * @return all public majors of the town.
    */
   public List<Inhabitant> getPublicMajors() {
     List<Major> majors = nation.getDatabase().find(Major.class)
             .where()
             .eq("townId", town.getId())
             .eq("hidden", false)
             .findList();
 
     List<Inhabitant> inhabitants = new LinkedList<Inhabitant>();
     for (Major major : majors) {
       inhabitants.add(nation.getInhabitant(major.getInhabitantId()));
     }
 
     return inhabitants;
   }
 
   /**
    * @return inhabitants of the town.
    */
   public List<Inhabitant> getInhabitants() {
     List<Residence> residences = nation.getDatabase().find(Residence.class)
             .where().eq("townId", town.getId()).findList();
 
     List<Inhabitant> inhabitants = new LinkedList<Inhabitant>();
     for (Residence residence : residences) {
       Inhabitant owner = nation.getInhabitant(residence.getOwnerId());
 
       if (owner == null) {
         inhabitants.add(owner);
       }
     }
 
     return inhabitants;
   }
 
   /**
    * Checks if the passed inhabitant is an inhabitant of the town.
    *
    * @param inhabitant the inhabitant to check.
    * @return true, if the inhabitant is an inhabitant of the town.
    */
   public boolean isInhabitant(Inhabitant inhabitant) {
     return nation.getDatabase().find(Residence.class).where()
             .eq("townId", town.getId())
            .eq("ownerId", inhabitant.getId())
             .findRowCount() > 0;
   }
 
   /**
    * @return residences of the town.
    */
   public List<Residence> getResidences() {
     return nation.getDatabase().find(Residence.class).where().eq("townId", town.getId()).findList();
   }
 }
