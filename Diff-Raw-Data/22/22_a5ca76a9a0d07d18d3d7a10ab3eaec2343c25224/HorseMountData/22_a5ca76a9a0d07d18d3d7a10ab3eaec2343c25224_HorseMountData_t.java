 package uk.thecodingbadgers.minekart.mount;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Horse;
 import org.bukkit.entity.Horse.Color;
 import org.bukkit.entity.Horse.Style;
 import org.bukkit.entity.Horse.Variant;
 
 public class HorseMountData extends DefaultMountData {
 
	private Color colour = Color.BLACK;
	private Style style = Style.NONE;
	private Variant variant = Variant.HORSE;
 
 	protected HorseMountData(EntityType type) {
 		super(type);
 	}
 
 	@Override
 	public void loadData(ConfigurationSection section) {
 		super.loadData(section);
 		this.colour = Color.valueOf(section.getString("color", Color.BLACK.name()));
 		this.style = Style.valueOf(section.getString("style", Style.NONE.name()));
 		this.variant = Variant.valueOf(section.getString("variant", Variant.HORSE.name()));
 	}
 
 	@Override
 	public ConfigurationSection getSaveData(ConfigurationSection section) {
 		section.set("color", colour.name());
 		section.set("style", style.name());
 		section.set("variant", variant.name());
 		return super.getSaveData(section);
 	}
 
 	@Override
 	public void applyMountData(Entity npc) {
 		super.applyMountData(npc);
 
 		if (!(npc instanceof Horse)) {
 			return;
 		}
 
 		Horse horse = (Horse) npc;
 
 		if (colour != null) {
 			horse.setColor(colour);
 		}
 
 		if (style != null) {
 			horse.setStyle(style);
 		}
 
 		if (variant != null) {
 			horse.setVariant(variant);
 		}
 	}
 
 	public Color getHorseColor() {
 		return colour;
 	}
 
 	public Style getHorseStyle() {
 		return style;
 	}
 
 	public Variant getHorseVariant() {
 		return variant;
 	}
 
 }
