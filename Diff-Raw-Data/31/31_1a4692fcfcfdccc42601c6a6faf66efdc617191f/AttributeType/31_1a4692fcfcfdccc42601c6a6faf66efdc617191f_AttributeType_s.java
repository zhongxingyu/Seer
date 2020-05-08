 /**
  * TerrainGIS 
  * Android program for mapping
  * 
  * Copyright (c) 2013 Vojtech Kalcik - http://vojta.kalcik.cz/
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
 package cz.kalcik.vojta.terraingis.layer;
 
 import java.util.Locale;
 
 import cz.kalcik.vojta.shapefilelib.files.dbf.DBF_Field.FieldType;
 
 /**
  * Data type of attribute
  * @author jules
  *
  */
 public enum AttributeType
 {
     TEXT, INTEGER, REAL;
     
     public static AttributeType getTypeSpatialite(String spatialiteType)
     {
         spatialiteType = spatialiteType.toLowerCase(Locale.UK);
         if(spatialiteType.equals("text"))
         {
             return TEXT;
         }
         else if(spatialiteType.equals("integer"))
         {
             return INTEGER;
         }
         else if(spatialiteType.equals("real"))
         {
             return REAL;
         }
         else
         {
             return null;
         }
     }
     
     public static AttributeType getTypeShapefile(FieldType type)
     {
         if(type == FieldType.C ||
            type == FieldType.D ||
            type == FieldType.M)
         {
             return TEXT;
         }
        else if(type == FieldType.N || type == FieldType.L)
         {
             return INTEGER;
         }
         else
         {
             return null;
         }
     }
     
     public static FieldType fromSpatialiteToShapefile(AttributeType type)
     {
         if(type == AttributeType.TEXT)
         {
             return FieldType.C;
         }
         else if(type == AttributeType.INTEGER ||
                 type == AttributeType.REAL)
         {
             return FieldType.N;
         }
         else
         {
             return null;
         }
     }
 }
