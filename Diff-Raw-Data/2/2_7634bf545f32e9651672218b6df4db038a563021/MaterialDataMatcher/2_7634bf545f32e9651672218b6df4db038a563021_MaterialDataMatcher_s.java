 package edgruberman.bukkit.silkpoke.util;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.Material;
 
 public class MaterialDataMatcher {
 
     private final Set<IdData> materials = new HashSet<IdData>();
 
     public boolean add(final String idData) {
         final String[] values = idData.split(":");
        final Material material = Material.getMaterial(values[0]);
         if (material == null) throw new IllegalArgumentException("Unrecognized Material: " + values[0]);
         final Short data = ( values.length >= 2 ? Short.valueOf(values[1]) : null );
         return this.add(material.getId(), data);
     }
 
     public boolean add(final int id, final Short data) {
         return this.materials.add(new IdData(id, data));
     }
 
     public boolean contains(final int id, final short data) {
         for (final IdData wild : this.materials)
             if (wild.id == id && (wild.data == null || wild.data == data))
                 return true;
 
         return false;
     }
 
 
 
     private static final class IdData {
 
         private final int id;
         private final Short data;
 
         private IdData(final int id, final Short data) {
             this.id = id;
             this.data = data;
         }
 
         @Override
         public int hashCode() {
             final int prime = 31;
             int result = 1;
             result = prime * result + ((this.data == null) ? 0 : this.data.hashCode());
             result = prime * result + this.id;
             return result;
         }
 
         @Override
         public boolean equals(final Object obj) {
             final IdData other = (IdData) obj;
             if (this.data == null) {
                 if (other.data != null) return false;
             } else if (!this.data.equals(other.data)) return false;
             if (this.id != other.id) return false;
             return true;
         }
 
     }
 
 }
