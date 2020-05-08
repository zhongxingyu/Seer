 /*    
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
 package net.minecraft.src;
 
 import net.minecraft.src.wifiremote.WirelessRemote;
 import net.minecraft.src.wifiremote.WirelessRemoteSMP;
 
 public class mod_WirelessRemote extends BaseMod
 {
 	public static boolean wirelessRemote = false;
 	public static boolean wirelessRemoteSMP = false;
 	public static BaseMod instance;
 	
 	@Override
 	public void modsLoaded()
 	{
 		if (ModLoader.isModLoaded("mod_WirelessRedstone"))
 		{
 			if (!wirelessRemote) {
 				wirelessRemote = WirelessRemote.initialize();
 			}
 		}
 		if (wirelessRemote && ModLoader.isModLoaded("mod_WirelessRedstoneClient"))
 		{
			if (!wirelessRemoteSMP)
				wirelessRemoteSMP = WirelessRemoteSMP.initialize();
 		}
 	}
 	
 	public mod_WirelessRemote() {
 		instance = this;
 	}
 
 	@Override
 	public void load() {
 	}
 	
 	@Override
 	public String getVersion() {
 		return "1.0";
 	}
 }
