 /*
  Copyright (C) 2012 Corey Edwards
 
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License along
  with this program; if not, write to the Free Software Foundation, Inc.,
  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  */
 package org.zmonkey.beacon.data;
 
 import java.io.Serializable;
 import java.util.Vector;
 
 /**
  * User: corey
  * Date: 3/11/12
  * Time: 3:16 PM
  */
 public class Subject implements Storable, Serializable {
     public int id;
     public String name;
     public String nickname;
     public String dateLastSeen;
     public String timeLastSeen;
     public String lastLocation;
     public String lastGps;
     public String sex;
     public String age;
     public String height;
     public String weight;
     public String eyeColor;
     public String hairColor;
     public String hairStyle;
     public String complexion;
     public String build;
     public String shirt;
     public String pants;
     public String jacket;
     public String shoes;
     public String socks;
     public String gloves;
     public String innerWear;
     public String outerWear;
     
     public static Vector<Subject> parseText(String text){
         Vector<Subject> v = new Vector<Subject>();
 
         if (text == null || text.equals("")){
             return v;
         }
 
         String[] n = text.split("\n");
 
         for (String i : n){
             String[] a = i.split(",", -1);
 
             Subject s = new Subject();
             s.id = Integer.parseInt(a[0]);
             if (a[1].trim().length() > 0){
                 s.name = a[1];
             }
             if (a[2].trim().length() > 0){
                 s.nickname = a[2];
             }
             if (a[3].trim().length() > 0){
                 s.dateLastSeen = a[3];
             }
             if (a[4].trim().length() > 0){
                 s.timeLastSeen = a[4];
             }
             if (a[5].trim().length() > 0){
                 s.lastLocation = a[5];
             }
             if (a[6].trim().length() > 0){
                s.lastGps = a[6].replace(" ", ",");
             }
             if (a[7].trim().length() > 0){
                 s.sex = a[7];
             }
             if (a[8].trim().length() > 0){
                 s.age = a[8];
             }
             if (a[9].trim().length() > 0){
                 s.height = a[9];
             }
             if (a[10].trim().length() > 0){
                 s.weight = a[10];
             }
             if (a[11].trim().length() > 0){
                 s.eyeColor = a[11];
             }
             if (a[12].trim().length() > 0){
                 s.hairColor = a[12];
             }
             if (a[13].trim().length() > 0){
                 s.hairStyle = a[13];
             }
             if (a[14].trim().length() > 0){
                 s.complexion = a[14];
             }
             if (a[15].trim().length() > 0){
                 s.build = a[15];
             }
             if (a[16].trim().length() > 0){
                 s.shirt = a[16];
             }
             if (a[17].trim().length() > 0){
                 s.pants = a[17];
             }
             if (a[18].trim().length() > 0){
                 s.jacket = a[18];
             }
             if (a[19].trim().length() > 0){
                 s.shoes = a[19];
             }
             if (a[20].trim().length() > 0){
                 s.socks = a[20];
             }
             if (a[21].trim().length() > 0){
                 s.gloves = a[21];
             }
             if (a[22].trim().length() > 0){
                 s.innerWear = a[22];
             }
             if (a[23].trim().length() > 0){
                 s.outerWear = a[23];
             }
 
             v.add(s);
         }
 
         return v;
     }
     
     public String toString(){
         if (name == null){
             return "";
         }
         StringBuilder s = new StringBuilder();
         s.append(name);
         
         if (sex != null){
             s.append(", ");
             s.append(sex);
         }
         
         if (age != null){
             s.append(", Age ");
             s.append(age);
         }
 
         return s.toString();
     }
 
     @Override
     public StringBuilder store(StringBuilder s) {
         if (s == null){
             s = new StringBuilder();
         }
         s.append("class=Subject\n");
         s.append(" id=" + id + "\n");
         s.append(" name=" + name + "\n");
         s.append(" nickname=" + nickname + "\n");
         s.append(" dateLastSeen=" + dateLastSeen + "\n");
         s.append(" timeLastSeen=" + timeLastSeen + "\n");
         s.append(" lastLocation=" + lastLocation + "\n");
         s.append(" lastGps=" + lastGps + "\n");
         s.append(" sex=" + sex + "\n");
         s.append(" age=" + age + "\n");
         s.append(" height=" + height + "\n");
         s.append(" weight=" + weight + "\n");
         s.append(" eyeColor=" + eyeColor + "\n");
         s.append(" hairColor=" + hairColor + "\n");
         s.append(" hairStyle=" + hairStyle + "\n");
         s.append(" complexion=" + complexion + "\n");
         s.append(" build=" + build + "\n");
         s.append(" shirt=" + shirt + "\n");
         s.append(" pants=" + pants + "\n");
         s.append(" jacket=" + jacket + "\n");
         s.append(" shoes=" + shoes + "\n");
         s.append(" socks=" + socks + "\n");
         s.append(" gloves=" + gloves + "\n");
         s.append(" innerWear=" + innerWear + "\n");
         s.append(" outerWear=" + outerWear + "\n");
         return s;
     }
 
     @Override
     public void load(String s) {
         if (s == null || s.equals("")){
             return;
         }
         String[] lines = s.split("\n");
         boolean me = false;
         for (String line : lines){
             if (me){
                 if (!line.equals("class=Subject") && line.startsWith("class=")){
                     me = false;
                 }
                 else {
                     if (line.startsWith(" id=")){
                         id = Integer.parseInt(line.substring(4));
                     }
                     else if (line.startsWith(" name=")){
                         name = line.substring(6);
                     }
                     else if (line.startsWith(" nickname=")){
                         nickname = line.substring(10);
                     }
                     else if (line.startsWith(" dateLastSeen=")){
                         dateLastSeen = line.substring(14);
                     }
                     else if (line.startsWith(" timeLastSeen=")){
                         timeLastSeen = line.substring(14);
                     }
                     else if (line.startsWith(" lastLocation=")){
                         lastLocation = line.substring(14);
                     }
                     else if (line.startsWith(" lastGps=")){
                         lastGps = line.substring(9);
                     }
                     else if (line.startsWith(" sex=")){
                         sex = line.substring(5);
                     }
                     else if (line.startsWith(" age=")){
                         age = line.substring(5);
                     }
                     else if (line.startsWith(" height=")){
                         height = line.substring(8);
                     }
                     else if (line.startsWith(" weight=")){
                         weight = line.substring(7);
                     }
                     else if (line.startsWith(" eyeColor=")){
                         eyeColor = line.substring(10);
                     }
                     else if (line.startsWith(" hairColor=")){
                         hairColor = line.substring(11);
                     }
                     else if (line.startsWith(" hairStyle=")){
                         hairStyle = line.substring(11);
                     }
                     else if (line.startsWith(" complexion=")){
                         complexion = line.substring(11);
                     }
                     else if (line.startsWith(" build=")){
                         build = line.substring(7);
                     }
                     else if (line.startsWith(" shirt=")){
                         shirt = line.substring(7);
                     }
                     else if (line.startsWith(" pants=")){
                         pants = line.substring(7);
                     }
                     else if (line.startsWith(" jacket=")){
                         jacket = line.substring(8);
                     }
                     else if (line.startsWith(" shoes=")){
                         shoes = line.substring(7);
                     }
                     else if (line.startsWith(" socks=")){
                         socks = line.substring(7);
                     }
                     else if (line.startsWith(" gloves=")){
                         gloves = line.substring(8);
                     }
                     else if (line.startsWith(" innerWear=")){
                         innerWear = line.substring(11);
                     }
                     else if (line.startsWith(" outerWear=")){
                         outerWear = line.substring(11);
                     }
                 }
             }
             else{
                 if (line.equals("class=Subject")){
                     me = true;
                 }
             }
         }
     }
 }
