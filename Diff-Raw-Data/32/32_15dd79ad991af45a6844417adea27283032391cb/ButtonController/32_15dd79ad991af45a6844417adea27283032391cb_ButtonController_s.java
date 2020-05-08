 /*
  * This file is part of LinkJVM.
  *
  * Java Framework for the KIPR Link
  * Copyright (C) 2013 Markus Klein<m@mklein.co.at>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package linkjvm.sensors.buttons;
 
 import linkjvm.low.buttons.ExtraButtons;
 
 /**
  * This class contains static methods to show an hide the extra buttons.
  * @author Markus Klein
  * @version 2.1.0
  * @since 2.0.0
  */
public class ButtonController {	
 	/**
 	 * shows the extra buttons
 	 */
 	public static void showExtraButtons(){
 		ExtraButtons.show();
 	}
 	
 	/**
 	 * hides the extra buttons
 	 */
 	public static void hideExtraButtons(){
 		ExtraButtons.hide();
 	}
 	
 	/**
 	 * Returns if the extra buttons are shown.
 	 * @return <code>true</code> if the extra buttons are shown, <code>false</code> if not
 	 */
 	public static boolean getExtraButtonsShown(){
 		return ExtraButtons.isShown();
 	}
 	
 	/**
 	 * Sets the extra buttons shown
 	 * @param shown if the extra buttons should be shown
 	 */
 	public static void setExtraButtonsDhown(boolean shown){
 		ExtraButtons.setShown(shown);
 	}
 	
 }
