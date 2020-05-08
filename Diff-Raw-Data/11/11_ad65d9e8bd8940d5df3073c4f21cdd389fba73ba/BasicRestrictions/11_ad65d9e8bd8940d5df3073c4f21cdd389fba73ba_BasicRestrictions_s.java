 /*
 This file is part of Legends.
 
     Legends is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Legends is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Legends.  If not, see <http://www.gnu.org/licenses/>.
 */
 package net.dawnfirerealms.legends.library.restriction;
 
 import net.dawnfirerealms.legends.library.level.LevelRestrictions;
 
 import java.util.HashMap;
 
 /**
  * @author B2OJustin
  */
 public class BasicRestrictions implements BasicRestrictor<IDRestrictable> {
     protected HashMap<String, Boolean> rMap;
     protected boolean defaultAllow;
     protected LevelRestrictions levelRestrictions;
 
     public BasicRestrictions() {
         rMap = new HashMap<>();
         defaultAllow = false;
        levelRestrictions = new LevelRestrictions(0, 0);
     }
 
     public void setAllowed(IDRestrictable restrictable, boolean allowed) {
         setAllowed(restrictable.getId(), allowed);
     }
 
     public boolean isAllowed(IDRestrictable restrictable) {
         return isAllowed(restrictable.getId());
     }
 
     public void setAllowed(String id, boolean allowed) {
         rMap.put(id, allowed);
     }
 
     public boolean isAllowed(String id) {
         Boolean allowed = rMap.get(id);
         if(allowed == null) return defaultAllow;
         else return allowed;
     }
 
     public void setDefault(boolean defaultAllow) {
         this.defaultAllow = defaultAllow;
     }
 }
