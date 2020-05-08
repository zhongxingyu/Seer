 package de.hh.changeRing;
 
import de.hh.changeRing.infrastructure.eclipselink.MappingCustomizer;
import org.eclipse.persistence.annotations.Customizer;

 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.MappedSuperclass;
 import javax.persistence.Version;
 import javax.xml.bind.annotation.XmlElement;
 import java.util.logging.Logger;
 
 /**
  * ----------------GNU General Public License--------------------------------
  * <p/>
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * <p/>
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * <p/>
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * <p/>
  * ----------------in addition-----------------------------------------------
  * <p/>
  * In addition, each military use, and the use for interest profit will be
  * excluded.
  * Environmental damage caused by the use must be kept as small as possible.
  */
 @MappedSuperclass
@Customizer(MappingCustomizer.class)
 public abstract class BaseEntity {
     private static final Logger log = Logger.getLogger(BaseEntity.class.getName());
     @Id
     @GeneratedValue
     @XmlElement
     protected Long id;
 
     @SuppressWarnings("UnusedDeclaration")
     @Version
     private int version;
 
     private static long idCounter = -1;
 
     public Long getId() {
         if (id == null) {
             log.severe("lazy id loading is only for Test!!");
             id = idCounter--;
         }
         return id;
     }
 
     @Override
     public boolean equals(Object other) {
         if (this == other) {
             return true;
         }
         if (other == null || getClass() != other.getClass()) {
             return false;
         }
         BaseEntity that = (BaseEntity) other;
         if (id == null) {
             return that.id == null && super.equals(other);
         }
         return id.equals(that.id);
     }
 
     @Override
     public int hashCode() {
         return id != null ? id.hashCode() : super.hashCode();
     }
 }
