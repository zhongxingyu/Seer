 package com.ainast.morepowerfulmobsreloaded;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import me.egordm.simpleattributes.API.SimpleAttributesAPI;
 import me.egordm.simpleattributes.Attributes.AttributeType;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Color;
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.inventory.meta.LeatherArmorMeta;
 
 public class TieredItems {
 	
 	public static ItemStack getRandomItem(int maxChance){
 		ItemStack item = new ItemStack(getRandomMaterial());
 		ItemMeta itemMeta = item.getItemMeta();
 		List<String> lore  = new ArrayList<String>();
 		
 		int chance = MPMTools.generator.nextInt(maxChance)+1;
 		int i = 0;
 		while(chance>0 && i<5){
 			int chance2 = MPMTools.generator.nextInt(100)+1;
 			System.out.println("Chance: " + chance + "\t Chance2: " + chance2);
 			if (chance2 <= chance){
 				if (chance2<=10){
 					int chance3 = MPMTools.generator.nextInt(3)+1;
 					if (chance3==1){
 						lore.add(MPMAttributeType.DRAGON_GROWL);
 					}else if (chance3==2){
 						lore.add(MPMAttributeType.DEATH_DEFYING);
 					}else if (chance3==3){
 						lore.add(MPMAttributeType.INCREASE_EXPERIENCE + ": " + MPMTools.generator.nextInt(30)+1);
 					}	
 				}else if (chance2<=30){
 					int chance3 = MPMTools.generator.nextInt(3)+1;
 					if (chance3==1){
 						lore.add(MPMAttributeType.FIRE_RESISTANCE_1);
 					}else if (chance3==2){
 						lore.add(MPMAttributeType.RESISTANCE_TO_DARK + ": " + MPMTools.generator.nextInt(20)+1);
 					}else if (chance3==3){
 						lore.add(MPMAttributeType.RESISTANCE_TO_MAGIC + ": " + MPMTools.generator.nextInt(20)+1);
 					}
 				}else if (chance2<=70){
 					int chance3 = MPMTools.generator.nextInt(3)+1;
 					if (chance3==1){
 						lore.add(MPMAttributeType.MANA_REGENERATION + ": " + MPMTools.generator.nextInt(10)+1);
 					}else if (chance3==2){
 						lore.add(MPMAttributeType.POISONOUS + ":" + MPMTools.generator.nextInt(10)+1);
 					}
 				}
 				chance = chance - chance2;
 			}
 			i++;
 		}
 		
 		
 		if (lore.size()==0) return item;
 		itemMeta.setDisplayName(ChatColor.DARK_RED + getRandomItemName());
 		itemMeta.setLore(lore);
 		item.setItemMeta(itemMeta);
 		return item;
 	}
 	
 	private static String getRandomItemName() {
 		String message = "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo voluptas nulla pariatur?".replace('?',' ').replace('.', ' ').replace(',', ' ').replace('\'', ' ');
 		String[] words = message.split(" ");
 		String word = " ";
 		int chance = MPMTools.generator.nextInt(100)+1;
 		int wordCount = 1;
 		if (chance<20){
 			wordCount = 5;
 			chance=101;
 		}else if (chance<40){
 			wordCount = 4;
 			chance=101;
 		}else if (chance<60){
 			wordCount = 2;
 			chance= 101;
 		}else wordCount = 1;
 		
 		for (int i = 0; i <= wordCount; i++){
 			word = word + words[MPMTools.generator.nextInt(words.length)] + " ";
 		}
 		
 		return word;
 	}
 
 	public static Material getRandomMaterial(){
 		int type = MPMTools.generator.nextInt(7)+1;
 		int material = MPMTools.generator.nextInt(100)+1;
 		
 		if (type==1){
 			//sword
 			if (material<10){
 				return Material.DIAMOND_SWORD;
 			}else if(material<30){
 				return Material.GOLD_SWORD;
 			}else if (material < 60){
 				return Material.IRON_SWORD;
 			}else{
 				return Material.STONE_SWORD;
 			}
 		}else if(type==2){
 			//axe
 			if (material<10){
 				return Material.DIAMOND_AXE;
 			}else if(material<30){
 				return Material.GOLD_AXE;
 			}else if (material < 60){
 				return Material.IRON_AXE;
 			}else{
 				return Material.STONE_AXE;
 			}
 		}else if (type==3){
 			//hoe
 			if (material<10){
 				return Material.DIAMOND_HOE;
 			}else if(material<30){
 				return Material.GOLD_HOE;
 			}else if (material < 60){
 				return Material.IRON_HOE;
 			}else{
 				return Material.STONE_HOE;
 			}
 		}else if (type==4){
 			//helmet
 			if (material<10){
 				return Material.DIAMOND_HELMET;
 			}else if(material<30){
 				return Material.GOLD_HELMET;
 			}else if (material < 60){
 				return Material.IRON_HELMET;
 			}else{
 				return Material.LEATHER_HELMET;
 			}
 		}else if (type==5){
 			//chestplate
 			if (material<10){
 				return Material.DIAMOND_CHESTPLATE;
 			}else if(material<30){
 				return Material.GOLD_CHESTPLATE;
 			}else if (material < 60){
 				return Material.IRON_CHESTPLATE;
 			}else{
 				return Material.LEATHER_CHESTPLATE;
 			}
 		}else if (type==6){
 			//leggings
 			if (material<10){
 				return Material.DIAMOND_LEGGINGS;
 			}else if(material<30){
 				return Material.GOLD_LEGGINGS;
 			}else if (material < 60){
 				return Material.IRON_LEGGINGS;
 			}else{
 				return Material.LEATHER_LEGGINGS;
 			}
 		}else if (type==7){
 			//boots
 			if (material<10){
 				return Material.DIAMOND_BOOTS;
 			}else if(material<30){
 				return Material.GOLD_BOOTS;
 			}else if (material < 60){
 				return Material.IRON_BOOTS;
 			}else{
 				return Material.LEATHER_BOOTS;
 			}
 		}
 		return Material.WOOD_SWORD;
 	}
 	
 	public static ItemStack getTier1CastingWand(){
 		ItemStack item = new ItemStack(Material.BLAZE_ROD);
 		ItemMeta itemMeta = item.getItemMeta();
 		List<String> lore = new ArrayList<String>();
 		
 		itemMeta.setDisplayName(ChatColor.MAGIC + "[T1] Casting Wand");
 		lore.add(ChatColor.ITALIC.GOLD + "I'm not sure about this...");
 		lore.add(MPMAttributeType.DEATH_DEFYING);
 		lore.add(MPMAttributeType.REDUCE_COOLDOWN_ON_FIREBALL + ": 1");
 		lore.add(MPMAttributeType.MANA_REGENERATION + ": 35");
 		lore.add(MPMAttributeType.MAXIMUM_MANA + ": 1");
 		lore.add(MPMAttributeType.MAXIMUM_HEALTH + ": 1");
 		
 		itemMeta.setLore(lore);
 		item.setItemMeta(itemMeta);
 
 		return item;
 	}
 	
 	public static ItemStack getTier1MrAmazingsSpecialHat(){
 		ItemStack item = new ItemStack(Material.LEATHER_HELMET);
 		ItemMeta itemMeta = item.getItemMeta();
 		List<String> lore = new ArrayList<String>();
 		lore.add(MPMAttributeType.INVISIBILITY);
 		lore.add(MPMAttributeType.DEATH_DEFYING);
 		lore.add(MPMAttributeType.MANA_REGENERATION + ": -10");		
 		itemMeta.setDisplayName(ChatColor.GOLD + "[T1] Mr. Amazing's Special Hat");
 		itemMeta.setLore(lore);
 		item.setItemMeta(itemMeta);
 		LeatherArmorMeta lim = (LeatherArmorMeta) item.getItemMeta();
 		lim.setColor(Color.fromBGR(255, 105, 180));
 		item.setItemMeta(lim);
 		return item;
 	}
 	
 	public static ItemStack getTier1CursedRunningShoes(){
 		ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
 		ItemMeta itemMeta = item.getItemMeta();
 		List<String> lore = new ArrayList<String>();
 		lore.add(ChatColor.GOLD + "" + ChatColor.ITALIC + "With Love, Frank");
 		lore.add(MPMAttributeType.DEATH_DEFYING);
 		lore.add(MPMAttributeType.MAXIMUM_HEALTH + ": 1");
 		lore.add(MPMAttributeType.MAXIMUM_MANA + ": 1");
 		itemMeta.setDisplayName(ChatColor.GOLD + "[T1] Cursed Running Shoes");
 		itemMeta.setLore(lore);
 		item.setItemMeta(itemMeta);
 		LeatherArmorMeta lim = (LeatherArmorMeta) item.getItemMeta();
 		lim.setColor(Color.BLACK);
 		item.setItemMeta(lim);
 		item = SimpleAttributesAPI.addItemAttribute(item, "[T1] Pristine Plate Mail" ,  AttributeType.GENERIC_MOVEMENT_SPEED, .4);
 		return item;
 	}
 	
 	public static ItemStack getBloodSword(){
 		ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
 		ItemMeta itemMeta = item.getItemMeta();
 		int percent = 0;
 		List<String> lore = new ArrayList<String>();
 		lore.add(ChatColor.ITALIC + " " + ChatColor.DARK_PURPLE + "Life is Hell!");
 		lore.add(MPMAttributeType.DEVILS_TAKE);
 		lore.add(MPMAttributeType.MAXIMUM_HEALTH + ": 1");
 		lore.add(MPMAttributeType.MAXIMUM_MANA + ": 1");
 		itemMeta.setDisplayName(ChatColor.YELLOW + "[TX] Blood Sword");
 		itemMeta.setLore(lore);
 		item.setItemMeta(itemMeta);
 		item = SimpleAttributesAPI.addItemAttribute(item, "[T1] Pristine Plate Mail" ,  AttributeType.GENERIC_MOVEMENT_SPEED, -0.1);
 		item = SimpleAttributesAPI.addItemAttribute(item, "[TX] Blood Sword" ,  AttributeType.GENERIC_ATTACK_DAMAGE, 45/100);
 		return item;
 	}
 		
 	//TIER 1 PLATE (Diamond Armor Set)
 	//TIER 1 PLATE (Diamond Armor Set)
 	
 	//TIER 1 PLATEMAIL
 	
 	public static ItemStack getTier1PristinePlateMail(){
 		ItemStack item = new ItemStack(Material.DIAMOND_CHESTPLATE);
 		ItemMeta itemMeta = item.getItemMeta();
 		List<String> lore = new ArrayList<String>();
 		int percent = 0;
 		percent = MPMTools.generator.nextInt(7-3)+3;
 		lore.add(MPMAttributeType.DEATH_DEFYING);
 		lore.add(MPMAttributeType.RESISTANCE_TO_PHYSICAL+": " + percent + "%");
 		percent = MPMTools.generator.nextInt(37-13)+13;
 		lore.add(MPMAttributeType.MAXIMUM_HEALTH + ":" + percent);
 		lore.add(MPMAttributeType.MANA_REGENERATION + ":" + -3);
 		percent = MPMTools.generator.nextInt(5-1)+1;
 		itemMeta.setLore(lore);
 		itemMeta.setDisplayName(ChatColor.GOLD + "[T1] Pristine Plate Mail");
 		item.setItemMeta(itemMeta);
 		item = SimpleAttributesAPI.addItemAttribute(item, "[T1] Pristine Plate Mail" ,  AttributeType.GENERIC_KNOCKBACK_RESISTANCE, percent/100.0);
 		item = SimpleAttributesAPI.addItemAttribute(item, "[T1] Pristine Plate Mail" ,  AttributeType.GENERIC_MOVEMENT_SPEED, -0.01);
 		
 		return item;
 	}
 	
 	/* [T1] Plate Mail
 	 * Includes:
 	 * 		Resistance to Physical: 3-7%
 	 * 		Max HP: 3-27
 	 * 		Knockback Resistance: .1-.4%
 	 * 		Mana Regeneration: -3
 	 * 		Slow 1 Effect
 	 */
 	public static ItemStack getTier1PlateMail(){
 		ItemStack item = new ItemStack(Material.DIAMOND_CHESTPLATE);
 		ItemMeta itemMeta = item.getItemMeta();
 		int percent = 0;
 		List<String> lore = new ArrayList<String>();
 		percent = MPMTools.generator.nextInt(7-3)+3;
 		lore.add(MPMAttributeType.RESISTANCE_TO_PHYSICAL + ": " + percent + "%");
 		percent = MPMTools.generator.nextInt(32-8)+8;
 		lore.add(MPMAttributeType.MAXIMUM_HEALTH + ": " + percent);		
 		percent = MPMTools.generator.nextInt(4-1)+1;
 		lore.add(MPMAttributeType.MANA_REGENERATION + ": -3");
 		itemMeta.setDisplayName(ChatColor.BLUE + "[T1] Plate Mail");
 		itemMeta.setLore(lore);
 		percent = MPMTools.generator.nextInt(4-1)+1;
 		item.setItemMeta(itemMeta);
 		item = SimpleAttributesAPI.addItemAttribute(item, "[T1] Plate Mail" ,  AttributeType.GENERIC_KNOCKBACK_RESISTANCE, percent/100.0);
 		item = SimpleAttributesAPI.addItemAttribute(item, "[T1] Plate Mail" ,  AttributeType.GENERIC_MOVEMENT_SPEED, -0.01);
 		return item;
 	}
 	
 	
 	/* [T1] Dented Plate Mail
 	 * Includes:
 	 * 		Resistance to Physical: 3-7%
 	 * 		Max HP: 3-27
 	 * 		Knockback Resistance: 0.1-0.3%
 	 * 		Mana Regeneration: -3
 	 * 		Slow 1 Effect
 	 */	
 	public static ItemStack getTier1DentedPlateMail(){
 
 		ItemStack item = new ItemStack(Material.DIAMOND_CHESTPLATE);
 		ItemMeta itemMeta = item.getItemMeta();
 		
 		int percent = 0;
 		
 
 		List<String> lore = new ArrayList<String>();
 		
 		percent = MPMTools.generator.nextInt(7-5)+5;
 		lore.add("Resistance to Physical: " + percent + "%");
 		
 		percent = MPMTools.generator.nextInt(27-3)+3;
 		lore.add("Maximum HEALTH: " + percent);
 		
 		lore.add("MANA Regeneration: -3");
 		
 		//lore.add(MPMAttributeType.SLOW_1);
 		
 
 		itemMeta.setDisplayName(ChatColor.GREEN + "[T1] Dented Plate Mail");
 		itemMeta.setLore(lore);
 		item.setItemMeta(itemMeta);
 		
 		percent = MPMTools.generator.nextInt(30-10)+10;  //divide by 100 on next line to get percentage.
 		item = SimpleAttributesAPI.addItemAttribute(item, "Dented Plate Mail" ,  AttributeType.GENERIC_KNOCKBACK_RESISTANCE, percent/100.0);
 
 		percent = -2;
 		item = SimpleAttributesAPI.addItemAttribute(item, "Dented Plate Mail" ,  AttributeType.GENERIC_MOVEMENT_SPEED, -0.01);
 		
 		return item;
 	}
 	
 	//TIER 1 PLATE LEGGINGS
 	
 	public static ItemStack getTier1PristinePlateLeggings(){
 		ItemStack item = new ItemStack(Material.DIAMOND_LEGGINGS);
 		ItemMeta itemMeta = item.getItemMeta();
 		List<String> lore = new ArrayList<String>();
 		int percent = 0;
 		percent = MPMTools.generator.nextInt(4-1)+1;
 		lore.add(MPMAttributeType.DEATH_DEFYING);
 		lore.add(MPMAttributeType.RESISTANCE_TO_PHYSICAL+": " + percent + "%");
 		percent = MPMTools.generator.nextInt(32-8)+8;
 		lore.add(MPMAttributeType.MAXIMUM_HEALTH + ":" + percent);
 		lore.add(MPMAttributeType.MANA_REGENERATION + ":" + -3);
 		percent = MPMTools.generator.nextInt(5-1)+1;
 		itemMeta.setLore(lore);
 		itemMeta.setDisplayName(ChatColor.GOLD + "[T1] Pristine Plate Leggings");
 		item.setItemMeta(itemMeta);
 		item = SimpleAttributesAPI.addItemAttribute(item, "[T1] Pristine Plate Leggings" ,  AttributeType.GENERIC_KNOCKBACK_RESISTANCE, percent/100.0);
 		
 		return item;
 	}
 	
 	public static ItemStack getTier1PlateLeggings(){
 		ItemStack item = new ItemStack(Material.DIAMOND_LEGGINGS);
 		ItemMeta itemMeta = item.getItemMeta();
 		List<String> lore = new ArrayList<String>();
 		int percent = 0;
 		percent = MPMTools.generator.nextInt(4-1)+1;
 		lore.add(MPMAttributeType.RESISTANCE_TO_PHYSICAL+": " + percent + "%");
 		percent = MPMTools.generator.nextInt(27-3)+3;
 		lore.add(MPMAttributeType.MAXIMUM_HEALTH + ":" + percent);
 		lore.add(MPMAttributeType.MANA_REGENERATION + ":" + -3);
 		percent = MPMTools.generator.nextInt(4-1)+1;
 		itemMeta.setLore(lore);
 		itemMeta.setDisplayName(ChatColor.BLUE + "[T1] Plate Leggings");
 		item.setItemMeta(itemMeta);
 		item = SimpleAttributesAPI.addItemAttribute(item, "[T1] Plate Leggings" ,  AttributeType.GENERIC_KNOCKBACK_RESISTANCE, percent/100.0);
 		
 		return item;
 	}
 	
 	public static ItemStack getTier1DentedPlateLeggings(){
 		ItemStack item = new ItemStack(Material.DIAMOND_LEGGINGS);
 		ItemMeta itemMeta = item.getItemMeta();
 		List<String> lore = new ArrayList<String>();
 		int percent = 0;
 		percent = MPMTools.generator.nextInt(4-1)+1;
 		lore.add(MPMAttributeType.RESISTANCE_TO_PHYSICAL+": " + percent + "%");
 		percent = MPMTools.generator.nextInt(22-1)+1;
 		lore.add(MPMAttributeType.MAXIMUM_HEALTH + ":" + percent);
 		lore.add(MPMAttributeType.MANA_REGENERATION + ":" + -3);
 		percent = MPMTools.generator.nextInt(3-1)+1;
 		itemMeta.setLore(lore);
 		itemMeta.setDisplayName(ChatColor.GREEN + "[T1] Dented Plate Leggings");
 		item.setItemMeta(itemMeta);
 		item = SimpleAttributesAPI.addItemAttribute(item, "[T1] Dented Plate Leggings" ,  AttributeType.GENERIC_KNOCKBACK_RESISTANCE, percent/100.0);
 		
 		return item;
 	}
 	
 	//TIER 1 PLATE GREAVES
 	
 	public static ItemStack getTier1PristinePlateGreaves(){
 		ItemStack item = new ItemStack(Material.DIAMOND_BOOTS);
 		ItemMeta itemMeta = item.getItemMeta();
 		List<String> lore = new ArrayList<String>();
 		int percent = 0;
 		percent = MPMTools.generator.nextInt(13-7)+7;
 		lore.add(MPMAttributeType.DEATH_DEFYING);
 		lore.add(MPMAttributeType.RESISTANCE_TO_EARTH+": " + percent + "%");
 		percent = MPMTools.generator.nextInt(27-3)+3;
 		lore.add(MPMAttributeType.MAXIMUM_HEALTH + ":" + percent);
 		itemMeta.setLore(lore);
 		itemMeta.setDisplayName(ChatColor.GOLD + "[T1] Pristine Plate Greaves");
 		item.setItemMeta(itemMeta);
 		
 		return item;
 	}
 	
 	public static ItemStack getTier1PlateGreaves(){
 		ItemStack item = new ItemStack(Material.DIAMOND_BOOTS);
 		ItemMeta itemMeta = item.getItemMeta();
 		List<String> lore = new ArrayList<String>();
 		int percent = 0;
 		percent = MPMTools.generator.nextInt(11-5)+5;
 		lore.add(MPMAttributeType.RESISTANCE_TO_EARTH+": " + percent + "%");
 		percent = MPMTools.generator.nextInt(22-1)+1;
 		lore.add(MPMAttributeType.MAXIMUM_HEALTH + ":" + percent);
 		itemMeta.setLore(lore);
 		itemMeta.setDisplayName(ChatColor.BLUE + "[T1] Plate Greaves");
 		item.setItemMeta(itemMeta);
 		
 		return item;
 	}
 	
 	public static ItemStack getTier1DentedPlateGreaves(){
 		ItemStack item = new ItemStack(Material.DIAMOND_BOOTS);
 		ItemMeta itemMeta = item.getItemMeta();
 		List<String> lore = new ArrayList<String>();
 		int percent = 0;
 		percent = MPMTools.generator.nextInt(8-2)+2;
		lore.add(MPMAttributeType.RESISTANCE_TO_EARTH+": " + percent + "%");
 		percent = MPMTools.generator.nextInt(17-1)+1;
 		lore.add(MPMAttributeType.MAXIMUM_HEALTH + ":" + percent);
 		itemMeta.setLore(lore);
 		itemMeta.setDisplayName(ChatColor.GREEN + "[T1] Dented Plate Greaves");
 		item.setItemMeta(itemMeta);
 		
 		return item;
 	}
 }
