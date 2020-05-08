 /*
  * Metro allows customers from any affiliate library to join any other member library.
  *    Copyright (C) 2013  Edmonton Public Library
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301, USA.
  *
  */
 package mecard.util;
 
 /**
  *
  * @author metro
  */
 public class Province
 {
     private boolean isValid;
     private String province;
     
     public Province(String p)
     {
         this.province = "";
         this.isValid  = true;
         // Special case where the string is empty
         if (p == null || p.length() == 0)
         {
             this.province = ProvinceType.ALBERTA.toString();
             return;
         }
         if (p.equalsIgnoreCase("Alberta") || p.equalsIgnoreCase("ab"))
         {
             this.province = ProvinceType.ALBERTA.toString();
             return;
         }
         if (p.equalsIgnoreCase("British Columbia") || p.equalsIgnoreCase("bc"))
         {
             this.province = ProvinceType.BRITISH_COLUMBIA.toString();
             return;
         }
         if (p.equalsIgnoreCase("Manitoba") || p.equalsIgnoreCase("mb"))
         {
             this.province = ProvinceType.MANITOBA.toString();
             return;
         }
         if (p.equalsIgnoreCase("New Brunswick") || p.equalsIgnoreCase("nb"))
         {
             this.province = ProvinceType.NEW_BRUNSWICK.toString();
             return;
         }
         if (p.equalsIgnoreCase("Newfoundland") || p.equalsIgnoreCase("nl"))
         {
             this.province = ProvinceType.NEWFOUNDLAND_LABRADOR.toString();
             return;
         }
         if (p.equalsIgnoreCase("Northwest Territories") || p.equalsIgnoreCase("nwt"))
         {
             this.province = ProvinceType.NORTHWEST_TERRITORIES.toString();
             return;
         }
         if (p.equalsIgnoreCase("Nova Scotia") || p.equalsIgnoreCase("ns"))
         {
             this.province = ProvinceType.MANITOBA.toString();
             return;
         }
         if (p.equalsIgnoreCase("Nunavut") || p.equalsIgnoreCase("nu"))
         {
             this.province = ProvinceType.NUNAVUT.toString();
             return;
         }
         if (p.equalsIgnoreCase("Ontario") || p.equalsIgnoreCase("on"))
         {
             this.province = ProvinceType.ONTARIO.toString();
             return;
         }
         if (p.equalsIgnoreCase("Prince Edward Island") || p.equalsIgnoreCase("pei"))
         {
             this.province = ProvinceType.PRINCE_EDWARD_ISLAND.toString();
             return;
         }
         if (p.equalsIgnoreCase("Quebec") || p.equalsIgnoreCase("qc"))
         {
             this.province = ProvinceType.QUEBEC.toString();
             return;
         }
         if (p.equalsIgnoreCase("Saskatchewan") || p.equalsIgnoreCase("sk"))
         {
             this.province = ProvinceType.SASKATCHEWAN.toString();
             return;
         }
         if (p.equalsIgnoreCase("Yukon") || p.equalsIgnoreCase("yk"))
         {
             this.province = ProvinceType.YUKON.toString();
             return;
         }
        else
        {
            this.isValid = false;
        }
     }
     
     public boolean isValid()
     {
         return this.isValid;
     }
     
     @Override
     public String toString()
     {
         return this.province;
     }
 }
