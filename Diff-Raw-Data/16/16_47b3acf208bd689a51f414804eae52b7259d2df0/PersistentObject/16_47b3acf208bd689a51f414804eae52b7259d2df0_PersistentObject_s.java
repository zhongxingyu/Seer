 /**************************************************************************************************
  * This file is part of [SpringAtom] Copyright [kornicameister@gmail.com][2013]                   *
  *                                                                                                *
  * [SpringAtom] is free software: you can redistribute it and/or modify                           *
  * it under the terms of the GNU General Public License as published by                           *
  * the Free Software Foundation, either version 3 of the License, or                              *
  * (at your option) any later version.                                                            *
  *                                                                                                *
  * [SpringAtom] is distributed in the hope that it will be useful,                                *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of                                 *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                  *
  * GNU General Public License for more details.                                                   *
  *                                                                                                *
  * You should have received a copy of the GNU General Public License                              *
  * along with [SpringAtom].  If not, see <http://www.gnu.org/licenses/gpl.html>.                  *
  **************************************************************************************************/
 
 package org.agatom.springatom.server.model.beans;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ComparisonChain;
 import com.google.common.primitives.Longs;
 import com.google.common.reflect.TypeToken;
 import org.agatom.springatom.server.model.types.PersistentBean;
 import org.hibernate.annotations.DynamicInsert;
 import org.hibernate.annotations.DynamicUpdate;
 import org.springframework.data.jpa.domain.AbstractPersistable;
 
 import javax.persistence.Access;
 import javax.persistence.AccessType;
 import javax.persistence.MappedSuperclass;
 import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
 import java.io.Serializable;
 import java.util.Comparator;
 
 /**
  * @author kornicameister
  * @version 0.0.1
  * @since 0.0.1
  */
 @DynamicInsert
 @DynamicUpdate
 @MappedSuperclass
 @Access(value = AccessType.FIELD)
 abstract public class PersistentObject<PK extends Serializable>
         extends AbstractPersistable<PK>
         implements PersistentBean,
                    Comparable<PersistentObject<PK>> {
     @Transient
     private static final Comparator<Serializable> ID_COMPARATOR = new Comparator<Serializable>() {
         private static final String COMPARED_KEYS_ARE_NOT_EQUAL_IN_TYPE_O1_S_O2_S = "Compared keys are not equal in type >> o1=%s != o2=%s";
 
         @Override
         public int compare(final Serializable o1, final Serializable o2) {
             Preconditions.checkArgument(o1 != null, "o1 key can not be null");
             Preconditions.checkArgument(o2 != null, "o2 key can not be null");
             assert o1 != null;
             assert o2 != null;
             final TypeToken<? extends Serializable> typeToken1 = TypeToken.of(o1.getClass());
             final TypeToken<? extends Serializable> typeToken2 = TypeToken.of(o2.getClass());
             if (this.areLongs(typeToken1, typeToken2)) {
                 return Longs.compare((Long) o1, (Long) o2);
             } else if (this.areStrings(typeToken1, typeToken2)) {
                 return ((String) o1).compareTo((String) o2);
             }
             throw new IllegalArgumentException(String
                     .format(COMPARED_KEYS_ARE_NOT_EQUAL_IN_TYPE_O1_S_O2_S, typeToken1.getType(), typeToken2
                             .getType()));
         }
 
         private boolean areStrings(final TypeToken<? extends Serializable> typeToken1, final TypeToken<? extends Serializable> typeToken2) {
             return typeToken1.isAssignableFrom(String.class) && typeToken2
                     .isAssignableFrom(String.class);
         }
 
         private boolean areLongs(final TypeToken<? extends Serializable> typeToken1, final TypeToken<? extends Serializable> typeToken2) {
             return typeToken1.isAssignableFrom(Long.class) && typeToken2
                     .isAssignableFrom(Long.class);
         }
     };
 
     public PersistentObject() {
         super();
     }
 
     @Override
     public int compareTo(
            @NotNull
             final PersistentObject<PK> pObject) {
         return ComparisonChain
                 .start()
                 .compare(this.getId(), pObject.getId(), ID_COMPARATOR)
                 .result();
     }
 }
