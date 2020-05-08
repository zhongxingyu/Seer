 package com.ez.EzTimeKeeper.tileentities;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import net.minecraft.tileentity.TileEntity;
 
 import com.ez.EzTimeKeeper.blocks.BlockInfo;
 import com.ez.EzTimeKeeper.blocks.BlockTimeKeeper;
 
 public class TileEntityTimeKeeper extends TileEntity {
 	
 	private int time = 0;
 	private String weatherCondition = "";
 
 	public void updateEntity() {
 		time++;
 		if (time == 20 && BlockInfo.ZIP != 00000) {
 			URL url = null;
 			try {
				url = new URL("http://ezekielelin.com/minecraft/mod/timekeeper/loc.php?zip="+BlockInfo.ZIP);
 			} catch (MalformedURLException e1) {
 				e1.printStackTrace();
 			}
 			String zipCodeData = "";
 			try {
 				BufferedReader reader = new BufferedReader (new InputStreamReader(url.openStream()));
 				BufferedWriter writer = new BufferedWriter (new FileWriter("data.html"));
 				String line;
 				while ((line = reader.readLine()) != null) {
 					zipCodeData += line;
 					writer.write(line);
 					writer.newLine();
 				}
 				reader.close();
 				writer.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			if (zipCodeData.toLowerCase().contains("rain".toLowerCase())) {
 				worldObj.getWorldInfo().setRaining(true);
 			} else {
 				worldObj.getWorldInfo().setRaining(false);
 			}
 		}
 		if (time > 12000) {
 			time = 0;
 		}
 
 		BlockTimeKeeper.timeKeep(worldObj);
 	}
 }
