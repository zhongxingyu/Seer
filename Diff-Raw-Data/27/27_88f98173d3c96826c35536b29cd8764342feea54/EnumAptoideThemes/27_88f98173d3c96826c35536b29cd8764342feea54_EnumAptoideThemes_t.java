 /*
  * EnumOptionsMenu		typeSafes Scattered menu options in Aptoide
  * Copyright (C) 20011  Duarte Silveira
  * duarte.silveira@caixamagica.pt
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
 
 package cm.aptoide.pt;
 
 public enum EnumAptoideThemes {
 	
 	APTOIDE_THEME_DEFAULT,
 	APTOIDE_THEME_MIDNIGHT, 
 	APTOIDE_THEME_MAGALHAES, 
 	APTOIDE_THEME_MAROON, 
 	APTOIDE_THEME_GOLD, 
 	APTOIDE_THEME_ORANGE, 
 	APTOIDE_THEME_SPRINGGREEN, 
 	APTOIDE_THEME_LIGHTSKY,
 	APTOIDE_THEME_PINK, 
 	APTOIDE_THEME_BLUE,
 	APTOIDE_THEME_RED, 
 	APTOIDE_THEME_MAGENTA, 
 	APTOIDE_THEME_SLATEGRAY, 
 	APTOIDE_THEME_SEAGREEN, 
 	APTOIDE_THEME_SILVER, 
 	APTOIDE_THEME_DIMGRAY, 
 	APTOIDE_THEME_TIMWE,
 	APTOIDE_THEME_DIGITALLYDIFFERENT,
 	APTOIDE_THEME_EOCEAN,
	APTOIDE_THEME_LAZERPLAY,
	APTOIDE_THEME_JBLOW;
 	
 	
 	public static EnumAptoideThemes reverseOrdinal(int ordinal){
 		return values()[ordinal];
 	}
 }
