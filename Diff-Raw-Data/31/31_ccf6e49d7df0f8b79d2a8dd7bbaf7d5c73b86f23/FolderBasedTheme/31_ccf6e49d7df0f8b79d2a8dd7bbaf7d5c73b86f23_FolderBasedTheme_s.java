 /*
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
 
 package com.jostrobin.battleships.view.theme;
 
import java.awt.Image;

import org.springframework.cache.annotation.Cacheable;
 
 import com.jostrobin.battleships.common.data.enums.ShipType;
 
 /**
  * @author rowyss
  *         Date: 13.01.12 Time: 23:57
  */
 public class FolderBasedTheme extends BaseTheme implements DescriptionBasedTheme
 {
 
     private ThemeDescription themeDescription;
     private static final String BASE_DIR = "tiles";
 
     public ThemeDescription getThemeDescription()
     {
         return themeDescription;
     }
 
     public void setThemeDescription(ThemeDescription themeDescription)
     {
         this.themeDescription = themeDescription;
     }
 
     public void setThemeDescription(String themeDescriptionName)
     {
         this.themeDescription = ThemeDescription.valueOf(themeDescriptionName);
     }
 
     @Override
     public String getThemeName()
     {
         return themeDescription.getName();
     }
 
    @Cacheable("images")
     @Override
     public Image getBackground()
     {
         return loadImage("tiles/paper-theme/background.bmp");
     }
 
     @Cacheable(value = "images")
     @Override
     public Image getByShipType(ShipType type)
     {
         String imageUrl = generateImagePath(type);
         return loadImage(imageUrl);
     }
 
     private String generateImagePath(ShipType shipType)
     {
         StringBuffer buffer = new StringBuffer(BASE_DIR);
         buffer.append("/")//
                 .append(themeDescription.getFolder())//
                 .append("/")//
                 .append(shipType.name().toLowerCase())//
                 .append(".bmp");
         return buffer.toString();
     }
 
	@Override
	public Image getGreendDot()
	{
         StringBuffer buffer = new StringBuffer(BASE_DIR);
         buffer.append("/")//
                 .append(themeDescription.getFolder())//
                 .append("/")//
                 .append("green_dot")//
                 .append(".bmp");
         return loadImage(buffer.toString());
	}
 
	@Override
	public Image getRedDot()
	{
         StringBuffer buffer = new StringBuffer(BASE_DIR);
         buffer.append("/")//
                 .append(themeDescription.getFolder())//
                 .append("/")//
                 .append("red_dot")//
                 .append(".bmp");
         return loadImage(buffer.toString());
	}
 }
