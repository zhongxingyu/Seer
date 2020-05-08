 /*
  * Copyright 2011, MyCellar
  *
  * This file is part of MyCellar.
  *
  * MyCellar is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * MyCellar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with MyCellar. If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.mycellar.interfaces.facade.web.domain;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.metamodel.Attribute;
 
 /**
  * @author speralta
  */
 public class MetamodelUtil {
 
     @SuppressWarnings("unchecked")
     public static <E> Attribute<E, ?> toMetamodelFirstPath(String path, Class<E> from) {
         try {
             String[] pathItems = path.split("\\.");
             Class<?> current = from;
             return (Attribute<E, ?>) Class.forName(current.getName() + "_").getField(pathItems[0])
                     .get(null);
         } catch (Exception e) {
             throw new IllegalStateException(e);
         }
     }
 
     public static Attribute<?, ?>[] toMetamodelOtherPath(String path, Class<?> from) {
         List<Attribute<?, ?>> attributes = toMetamodelListAttributes(path, from);
         return attributes.subList(1, attributes.size()).toArray(
                 new Attribute<?, ?>[attributes.size() - 1]);
     }
 
     public static Attribute<?, ?>[] toMetamodelPath(String path, Class<?> from) {
         List<Attribute<?, ?>> attributes = toMetamodelListAttributes(path, from);
         return attributes.toArray(new Attribute<?, ?>[attributes.size()]);
     }
 
     private static List<Attribute<?, ?>> toMetamodelListAttributes(String path, Class<?> from) {
         try {
             List<Attribute<?, ?>> attributes = new ArrayList<>();
            String[] pathItems = path.split("\\.");
             Class<?> current = from;
            for (String pathItem : pathItems) {
                Attribute<?, ?> attribute = (Attribute<?, ?>) Class
                        .forName(current.getName() + "_").getField(pathItem).get(null);
                 attributes.add(attribute);
                 current = attribute.getJavaType();
             }
             return attributes;
         } catch (Exception e) {
             throw new IllegalStateException(e);
         }
     }
 
 }
