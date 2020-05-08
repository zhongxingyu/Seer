 /*
  * Copyright 2011 Alexander Baumgartner
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.madthrax.ridiculousRPG.map;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.io.PrintWriter;
 import java.util.Arrays;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Pixmap.Format;
 import com.badlogic.gdx.graphics.g2d.tiled.TileSet;
 import com.badlogic.gdx.graphics.g2d.tiled.TiledLoader;
 import com.badlogic.gdx.graphics.g2d.tiled.TiledMap;
 import com.badlogic.gdx.tiledmappacker.TiledMapPacker;
 import com.badlogic.gdx.tools.imagepacker.TexturePacker;
 
 /**
  * @author Alexander Baumgartner
  */
 public class OnChangeMapPacker {
 	private File mapInDir;
 	private File mapOutDir;
 
 	public OnChangeMapPacker(String mapInDir, String mapOutDir) {
 		this.mapInDir = new File(mapInDir);
 		this.mapOutDir = new File(mapOutDir);
 	}
 
 	public void packOnChange() {
 		try {
 			boolean packMaps = false;
 			if (!mapOutDir.exists()) {
 				mapOutDir.mkdirs();
 				packMaps = true;
 			} else {
 				try {
 					BufferedReader checkUpdate = new BufferedReader(
							new FileReader(mapOutDir + "check.txt"));
 					File[] tmxFiles = mapInDir.listFiles(new FilenameFilter() {
 						public boolean accept(File dir, String name) {
 							return name.toLowerCase().endsWith(".tmx");
 						}
 					});
 					Arrays.sort(tmxFiles);
 					for (File tmx : tmxFiles) {
 						String line = checkUpdate.readLine();
 						if (line == null
 								|| !line.startsWith(tmx.getName())
 								|| !line.endsWith(String.valueOf(tmx
 										.lastModified()))) {
 							packMaps = true;
 							break;
 						}
 						TiledMap tmxMap = TiledLoader.createMap(Gdx.files
 								.absolute(tmx.getAbsolutePath()));
 						for (TileSet tileSet : tmxMap.tileSets) {
 							File img = new File(mapInDir, tileSet.imageName);
 							line = checkUpdate.readLine();
 							if (line == null
 									|| !line.startsWith(img.getName())
 									|| !line.endsWith(String.valueOf(img
 											.lastModified()))) {
 								packMaps = true;
 								break;
 							}
 						}
 					}
 					checkUpdate.close();
 				} catch (Exception packIt) {
 					packMaps = true;
 				}
 			}
 			if (packMaps) {
 				Gdx.files.absolute(mapOutDir.getAbsolutePath())
 						.deleteDirectory();
 				TexturePacker.Settings settings = new TexturePacker.Settings();
 				settings.defaultFormat = Format.RGBA8888;
 				settings.stripWhitespace = true;
 				settings.incremental = true;
 				settings.alias = true;
 				new TiledMapPacker().processMap(mapInDir, mapOutDir, settings);
 
 				// write info-file
 				PrintWriter checkUpdate = new PrintWriter(mapOutDir
						+ "check.txt");
 				File[] tmxFiles = mapInDir.listFiles(new FilenameFilter() {
 					public boolean accept(File dir, String name) {
 						return name.toLowerCase().endsWith(".tmx");
 					}
 				});
 				Arrays.sort(tmxFiles);
 				for (File tmx : tmxFiles) {
 					checkUpdate.println(tmx.getName() + " / "
 							+ tmx.lastModified());
 					TiledMap tmxMap = TiledLoader.createMap(Gdx.files
 							.absolute(tmx.getAbsolutePath()));
 					for (TileSet tileSet : tmxMap.tileSets) {
 						File img = new File(mapInDir, tileSet.imageName);
 						checkUpdate.println(img.getName() + " / "
 								+ img.lastModified());
 					}
 				}
 				checkUpdate.close();
 			}
 		} catch (Exception noPermission) {
 			noPermission.printStackTrace();
 		}
 	}
 }
