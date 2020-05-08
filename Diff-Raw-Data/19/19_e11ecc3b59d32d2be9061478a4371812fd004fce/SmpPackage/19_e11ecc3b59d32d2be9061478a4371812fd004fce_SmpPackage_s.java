 /*
  The MIT License
 
  Copyright (c) 2012 Zloteanu Nichita (ZNickq) and Andre Mohren (IceReaper)
 
  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:
 
  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.
 
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.
  */
 
 package net.morematerials.morematerials.smp;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.security.MessageDigest;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import javax.imageio.ImageIO;
 import me.znickq.furnaceapi.SpoutFurnaceRecipe;
 import me.znickq.furnaceapi.SpoutFurnaceRecipes;
 import net.morematerials.morematerials.Main;
 import net.morematerials.morematerials.manager.MainManager;
 import net.morematerials.morematerials.materials.CustomShape;
 import net.morematerials.morematerials.materials.SMCustomBlock;
 import net.morematerials.morematerials.materials.SMCustomItem;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.inventory.Recipe;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.block.design.GenericBlockDesign;
 import org.getspout.spoutapi.block.design.GenericCuboidBlockDesign;
 import org.getspout.spoutapi.block.design.Texture;
 import org.getspout.spoutapi.inventory.SpoutItemStack;
 import org.getspout.spoutapi.inventory.SpoutShapedRecipe;
 import org.getspout.spoutapi.inventory.SpoutShapelessRecipe;
 import org.getspout.spoutapi.material.Material;
 import org.getspout.spoutapi.material.MaterialData;
 
 public class SmpPackage {
 	private SmpManager smpManager = null;
 	public String name = "";
 	private ZipFile smpFile = null;
 	private Map<String, SMCustomBlock> customBlocksList = new HashMap<String, SMCustomBlock>();
 	private Map<String, SMCustomItem> customItemsList = new HashMap<String, SMCustomItem>();
 	private List<SpoutFurnaceRecipe> furnaceRecipeList = new ArrayList<SpoutFurnaceRecipe>();
 	private List<Recipe> craftingRecipeList = new ArrayList<Recipe>();
 
 	public SmpPackage(SmpManager smpManager, ZipFile smpFile, String name) {
 		this.name = name;
 		this.smpFile = smpFile;
 		this.smpManager = smpManager;
 		Map<String, YamlConfiguration> materials = new HashMap<String, YamlConfiguration>();
 		try {
 			// Getting all materials.
 			Enumeration<? extends ZipEntry> entries = this.smpFile.entries();
 			while (entries.hasMoreElements()) {
 				ZipEntry entry = entries.nextElement();
 				if (entry.getName().matches("^.+\\.yml$")) {
 					String materialName = entry.getName().replaceAll("\\.yml$", "");
 					materials.put(materialName, new YamlConfiguration());
 					try {
 						materials.get(materialName).load(this.smpFile.getInputStream(entry));
 					} catch (Exception e) {
 						MainManager.getUtils().log(
 							"Error loading YML " + materialName + " from " + this.name + ".", Level.WARNING
 						);
 					}
 				}
 			}
 
 			// Initialize all materials
 			for (String materialName : materials.keySet()) {
 				this.loadMaterial(materialName, materials.get(materialName), this.smpFile);
 			}
 
 			// Initialize all crafting recipes
 			for (String materialName : this.customBlocksList.keySet()) {
 				if (materials.get(materialName).contains("Recipes")) {
 					this.loadCraftingRecipe(
 						materialName, this.customBlocksList.get(materialName), materials.get(materialName)
 					);
 				}
 			}
 			for (String materialName : this.customItemsList.keySet()) {
 				if (materials.get(materialName).contains("Recipes")) {
 					this.loadCraftingRecipe(
 						materialName, this.customItemsList.get(materialName), materials.get(materialName)
 					);
 				}
 			}
 
 			// Initialize all block-break drops
 			for (String materialName : this.customBlocksList.keySet()) {
 				this.setDrops(materials.get(materialName), materialName);
 			}
 
 			this.smpFile.close();
 		} catch (Exception e) {
 			MainManager.getUtils().log("Couldn't load " + this.name + ".", Level.WARNING);
 		}
 	}
 
 	private void loadMaterial(String materialName, YamlConfiguration config, ZipFile smpFile) {
 		try {
 			String textureName = this.cacheFile(materialName + ".png");
 			try {
 				if (config.getString("Type", "").equals("Block")) {
 					// Initialize a block.
 					GenericBlockDesign design;
 					if (smpFile.getEntry(materialName + ".shape") != null) {
 						design = new CustomShape(
 							this.smpManager,
 							smpFile.getInputStream(smpFile.getEntry(materialName + ".shape")),
 							textureName, config.getInt("BlockID", 1)
 						);
 					} else {
 						design = this.getCuboidDesign(textureName, config.getInt("BlockID", 1));
 					}
 					float brightness = (float) config.getDouble("Brightness", 0.2);
 					//TODO check this values, remove wrong ones.
 					design.setBrightness(brightness);
 					design.setMinBrightness(brightness);
 					design.setMaxBrightness(brightness);
 					SMCustomBlock customBlock = new SMCustomBlock(
 						this, config.getString("Title", materialName),
						config.getInt("BlockID", 1), design
 					);
 					customBlock.setConfig(config);
 					this.customBlocksList.put(materialName, customBlock);
 				} else if (config.getString("Type", "").equals("Item")) {
 					// Initialize an item.
 					SMCustomItem customItem = new SMCustomItem(
 						this, config.getString("Title", materialName), textureName
 					);
 					customItem.setConfig(config);
 					this.customItemsList.put(materialName, customItem);
 				}
 			} catch (Exception e) {
 				MainManager.getUtils().log(
 					"Couldn't load material " + materialName + " from " + this.name + ".", Level.WARNING
 				);
 			}
 		} catch (Exception e) {
 			MainManager.getUtils().log(
 				"Couldn't load texture " + materialName + ".png from " + this.name + ".", Level.WARNING
 			);
 		}
 	}
 
 	private void loadCraftingRecipe(String materialName, Material material, YamlConfiguration config) {
 		List<?> recipes = config.getList("Recipes");
 		// Make sure we have a valid list.
 		if (recipes == null) {
 			return;
 		}
 		// This allows us to have multiple recipes.
 		for (Object orecipe : recipes) {
 			// TODO unsafe cast warning remove
 			Map<String, Object> recipe = (Map<String, Object>) orecipe;
 			String type = (String) recipe.get("type");
 			Integer amount = (Integer) recipe.get("amount");
 			amount = amount == null ? 1 : amount;
 			if (type.equalsIgnoreCase("furnace")) {
 				String ingredientName = (String) recipe.get("ingredients");
 				Material ingredient;
 				if (ingredientName.matches("^[0-9]+$")) {
 					ingredient = org.getspout.spoutapi.material.MaterialData.getMaterial(Integer.parseInt(ingredientName));
 				} else if (materialName.split("\\.").length == 1) {
 					ingredient = this.getMaterial(ingredientName);
 				} else {
 					Map<String, Material> materialList = this.smpManager.getMaterial(materialName);
 					ingredient = materialList.get((String) materialList.keySet().toArray()[0]);
 				}
 				SpoutFurnaceRecipe fRecipe;
 				fRecipe = new SpoutFurnaceRecipe(new SpoutItemStack(ingredient, 1), new SpoutItemStack(material, 1));
 				SpoutFurnaceRecipes.registerSpoutRecipe(fRecipe);
 				this.furnaceRecipeList.add(fRecipe);
 			} else if (type.equalsIgnoreCase("shaped")) {
 				SpoutShapedRecipe sRecipe = new SpoutShapedRecipe(
 					new SpoutItemStack(material, amount)
 				).shape("abc","def", "ghi");
 				String ingredients = (String) recipe.get("ingredients");
 				this.doRecipe(sRecipe, ingredients);
 			} else if (type.equalsIgnoreCase("shapeless")) {
 				SpoutShapelessRecipe sRecipe = new SpoutShapelessRecipe(new SpoutItemStack(material, amount));
 				String ingredients = (String) recipe.get("ingredients");
 				this.doRecipe(sRecipe, ingredients);
 			} else {
 				MainManager.getUtils().log(
 					"Couldn't load crafting recipe for " + materialName + " from " + this.name + ".", Level.WARNING
 				);
 			}
 		}
 	}
 
 	private GenericCuboidBlockDesign getCuboidDesign(String textureName, int blockID) throws IOException {
 		GenericCuboidBlockDesign design;
 		File textureFile = null;
 		if (Main.getConf().getBoolean("Use-WebServer")) {
 			textureFile = new File(
 				this.smpManager.getPlugin().getDataFolder().getPath() + File.separator + "cache",
 				textureName.substring(textureName.lastIndexOf("/"))
 			);
 		} else {
 			textureFile = new File(
 				this.smpManager.getPlugin().getDataFolder().getPath() + File.separator + "cache",
 				textureName
 			);
 		}
 		BufferedImage bufferedImage = ImageIO.read(textureFile);
 
 		// for different textures on each block side
 		if (bufferedImage.getWidth() > bufferedImage.getHeight()) {
 			Texture texture = new Texture(
 				this.smpManager.getPlugin(), textureName,
 				bufferedImage.getWidth() * 8, bufferedImage.getWidth(), bufferedImage.getWidth()
 			);
 			int[] idMap = new int[6];
 			for (int i = 0; i < 6; i++) {
 				idMap[i] = i;
 			}
 			design = new GenericCuboidBlockDesign(
 				this.smpManager.getPlugin(), texture, idMap, 0, 0, 0, 1, 1, 1
 			);
 			// default block, with same texture on each side
 		} else {
 			design = new GenericCuboidBlockDesign(
 				this.smpManager.getPlugin(), textureName, bufferedImage.getWidth(), 0, 0, 0, 1, 1, 1
 			);
 		}
 		if (blockID == 20) {
 			design.setRenderPass(1);
 		}
 		return design;
 	}
 
 	private void doRecipe(Recipe recipe, String ingredients) {
 		Integer currentLine = 0;
 		Integer currentColumn = 0;
 
 		ingredients = ingredients.replaceAll("\\s{2,}", " ");
 		for (String line : ingredients.split("\\r?\\n")) {
 			// make sure we stop at the third line
 			if (currentLine >= 3) {
 				continue;
 			}
 			for (String ingredientitem : line.split(" ")) {
 				// make sure we stop at the third entry in this line
 				if (currentColumn >= 3) {
 					continue;
 				}
 
 				// this character is required for matching the current material
 				// into the recipe
 				char a = (char) ('a' + currentColumn + currentLine * 3);
 
 				// getting the correct material
 				Material ingredient;
 				if (ingredientitem.matches("^[0-9]+$")) {
 					ingredient = org.getspout.spoutapi.material.MaterialData.getMaterial(Integer.parseInt(ingredientitem));
 				} else if (ingredientitem.split("\\.").length == 1) {
 					ingredient = this.getMaterial(ingredientitem);
 				} else {
 					Map<String, Material> materialList = this.smpManager.getMaterial(ingredientitem);
 					ingredient = materialList.get((String) materialList.keySet().toArray()[0]);
 				}
 
 				if (ingredient == null) {
 					ingredient = MaterialData.getMaterial(ingredientitem);
 				}
 				// Do not require an "air-block" in empty fields :D
 				if (ingredient == null || ingredientitem.equals("0")) {
 					currentColumn++;
 					continue;
 				}
 
 				// adding the ingredient
 				if (recipe instanceof SpoutShapedRecipe) {
 					((SpoutShapedRecipe) recipe).setIngredient(a, ingredient);
 				} else {
 					((SpoutShapelessRecipe) recipe).addIngredient(ingredient);
 				}
 
 				currentColumn++;
 			}
 			currentColumn = 0;
 			currentLine++;
 		}
 
 		// Putting the recipe into the register
 		SpoutManager.getMaterialManager().registerSpoutRecipe(recipe);
 		this.craftingRecipeList.add(recipe);
 	}
 
 	public String cacheFile(String fileName) throws Exception {
 		// Getting the hash of the file.
 		InputStream fis = this.smpFile.getInputStream(this.smpFile.getEntry(fileName));
 		byte[] buffer = new byte[1024];
 		MessageDigest complete = MessageDigest.getInstance("MD5");
 		int numRead;
 		do {
 			numRead = fis.read(buffer);
 			if (numRead > 0) {
 				complete.update(buffer, 0, numRead);
 			}
 		} while (numRead != -1);
 		fis.close();
 		byte[] b = complete.digest();
 		String result = "";
 		for (int i = 0; i < b.length; i++) {
 			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
 		}
 
 		// Saving the file.
 		InputStream inputStream = this.smpFile.getInputStream(this.smpFile.getEntry(fileName));
 		File tempDir = new File(smpManager.getPlugin().getDataFolder() + File.separator + "cache");
 		tempDir.mkdir();
 		File cacheFile = new File(tempDir, result + fileName.substring(fileName.lastIndexOf(".")));
 		// No need of creating the file again, its already present!
 		if (!cacheFile.exists()) {
 			OutputStream out = new FileOutputStream(cacheFile);
 			int read;
 			byte[] bytes = new byte[1024];
 			while ((read = inputStream.read(bytes)) != -1) {
 				out.write(bytes, 0, read);
 			}
 			out.flush();
 			out.close();
 		}
 		inputStream.close();
 		if (Main.getConf().getBoolean("Use-WebServer")) {
 			result = "http://" + Main.getConf().getString("Hostname") + ":" + Main.getConf().getInt("PublicPort") + "/"
 				+ result + fileName.substring(fileName.lastIndexOf("."));
 			return result;
 		} else {
 			SpoutManager.getFileManager().addToCache(this.smpManager.getPlugin(), cacheFile);
 			return cacheFile.getName();
 		}
 	}
 
 	private void setDrops(YamlConfiguration config, String name) {
 		String objectType = config.getString("Type", "");
 		if (objectType.equalsIgnoreCase("Block")) {
 			String dropItem = config.getString("ItemDrop", name);
 			int dropAmount = config.getInt("ItemDropAmount", 1);
 
 			Material dropMaterial;
 			if (dropItem.matches("^[0-9]+$")) {
 				dropMaterial = org.getspout.spoutapi.material.MaterialData.getMaterial(Integer.parseInt(dropItem));
 			} else if (dropItem.split("\\.").length == 1) {
 				dropMaterial = this.getMaterial(dropItem);
 			} else {
 				Map<String, Material> materialList = this.smpManager.getMaterial(dropItem);
 				dropMaterial = materialList.get((String) materialList.keySet().toArray()[0]);
 			}
 			this.customBlocksList.get(name).setItemDrop(new SpoutItemStack(dropMaterial, dropAmount));
 		}
 	}
 
 	public Material getMaterial(String materialName) {
 		if (this.customBlocksList.containsKey(materialName)) {
 			return this.customBlocksList.get(materialName);
 		} else if (this.customItemsList.containsKey(materialName)) {
 			return this.customItemsList.get(materialName);
 		}
 		return null;
 	}
 
 	public Material getMaterial(SpoutItemStack itemStack) {
 		for (String itemName : this.customBlocksList.keySet()) {
 			SMCustomBlock tempBlock = this.customBlocksList.get(itemName);
 			if (tempBlock.getRawData() == itemStack.getMaterial().getRawData()) {
 				return tempBlock;
 			}
 		}
 		for (String itemName : this.customItemsList.keySet()) {
 			SMCustomItem tempItem = this.customItemsList.get(itemName);
 			if (tempItem.getRawData() == itemStack.getMaterial().getRawData()) {
 				return tempItem;
 			}
 		}
 		return null;
 	}
 
 	public SmpManager getSmpManager() {
 		return this.smpManager;
 	}
 
 	public int getMaterialCount() {
 		return customBlocksList.size() + customItemsList.size();
 	}
 }
