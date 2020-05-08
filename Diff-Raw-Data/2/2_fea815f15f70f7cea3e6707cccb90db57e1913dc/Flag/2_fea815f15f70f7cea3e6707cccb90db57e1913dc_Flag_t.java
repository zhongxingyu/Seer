 /*
  * Movie Renamer
  * Copyright (C) 2012 Nicolas Magré
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
 package fr.free.movierenamer.ui.res;
 
 import fr.free.movierenamer.ui.utils.ImageUtils;
 import fr.free.movierenamer.utils.LocaleUtils;
 import java.util.Locale;
 import javax.swing.Icon;
 
 /**
  * Class Flag
  *
  * @author Nicolas Magré
  */
 public abstract class Flag {
 
   //Unknown flag
   private static final Icon Unknown = ImageUtils.getIconFromJar("country/Unknown.png");
 
   // Only most common flag for video media
   private enum localeFlag {
 
     Algeria(ImageUtils.getIconFromJar("country/Algeria.png")),
     Argentina(ImageUtils.getIconFromJar("country/Argentina.png")),
     Australia(ImageUtils.getIconFromJar("country/Australia.png")),
     Austria(ImageUtils.getIconFromJar("country/Austria.png")),
     Belgium(ImageUtils.getIconFromJar("country/Belgium.png")),
     Brazil(ImageUtils.getIconFromJar("country/Brazil.png")),
     Bulgaria(ImageUtils.getIconFromJar("country/Bulgaria.png")),
     Canada(ImageUtils.getIconFromJar("country/Canada.png")),
     China(ImageUtils.getIconFromJar("country/China.png")),
     Colombia(ImageUtils.getIconFromJar("country/Colombia.png")),
     Costa_Rica(ImageUtils.getIconFromJar("country/Costa_Rica.png")),
     Croatia(ImageUtils.getIconFromJar("country/Croatia.png")),
     Czech_Republic(ImageUtils.getIconFromJar("country/Czech_Republic.png")),
     Denmark(ImageUtils.getIconFromJar("country/Denmark.png")),
     Finland(ImageUtils.getIconFromJar("country/Finland.png")),
     France(ImageUtils.getIconFromJar("country/France.png")),
     Germany(ImageUtils.getIconFromJar("country/Germany.png")),
     Greece(ImageUtils.getIconFromJar("country/Greece.png")),
     Hong_Kong(ImageUtils.getIconFromJar("country/Hong_Kong.png")),
     Hungary(ImageUtils.getIconFromJar("country/Hungary.png")),
     Iceland(ImageUtils.getIconFromJar("country/Iceland.png")),
     India(ImageUtils.getIconFromJar("country/India.png")),
     Iran(ImageUtils.getIconFromJar("country/Iran.png")),
     Ireland(ImageUtils.getIconFromJar("country/Ireland.png")),
     Italy(ImageUtils.getIconFromJar("country/Italy.png")),
     Israel(ImageUtils.getIconFromJar("country/Israel.png")),
     Japan(ImageUtils.getIconFromJar("country/Japan.png")),
     Malaysia(ImageUtils.getIconFromJar("country/Malaysia.png")),
     Mexico(ImageUtils.getIconFromJar("country/Mexico.png")),
     Netherlands(ImageUtils.getIconFromJar("country/Netherlands.png")),
     New_Zealand(ImageUtils.getIconFromJar("country/New_Zealand.png")),
     Norway(ImageUtils.getIconFromJar("country/Norway.png")),
     Pakistan(ImageUtils.getIconFromJar("country/Pakistan.png")),
     Poland(ImageUtils.getIconFromJar("country/Poland.png")),
     Portugal(ImageUtils.getIconFromJar("country/Portugal.png")),
     Romania(ImageUtils.getIconFromJar("country/Romania.png")),
     Russian_Federation(ImageUtils.getIconFromJar("country/Russian_Federation.png")),
     Singapore(ImageUtils.getIconFromJar("country/Singapore.png")),
     South_Africa(ImageUtils.getIconFromJar("country/South_Africa.png")),
     South_Korea(ImageUtils.getIconFromJar("country/South_Korea.png")),
     Spain(ImageUtils.getIconFromJar("country/Spain.png")),
     Sweden(ImageUtils.getIconFromJar("country/Sweden.png")),
     Switzerland(ImageUtils.getIconFromJar("country/Switzerland.png")),
     Thailand(ImageUtils.getIconFromJar("country/Thailand.png")),
     Turkey(ImageUtils.getIconFromJar("country/Turkey.png")),
     Ukraine(ImageUtils.getIconFromJar("country/Ukraine.png")),
     United_Kingdom(ImageUtils.getIconFromJar("country/United_Kingdom.png")),
     United_States(ImageUtils.getIconFromJar("country/United_States_of_America.png"));
     private final Icon flag;
 
     localeFlag(Icon flag) {
       this.flag = flag;
     }
   }
 
   public static Icon getFlag(String code) {
    Locale local = LocaleUtils.findCountry(code);
     if(local != null) {
       for (localeFlag lFlag : localeFlag.values()) {
         if (local.getDisplayCountry(Locale.ENGLISH).equalsIgnoreCase(lFlag.name().replace("_", " "))) {
           return lFlag.flag;
         }
       }
     }
     return Unknown;
   }
 }
