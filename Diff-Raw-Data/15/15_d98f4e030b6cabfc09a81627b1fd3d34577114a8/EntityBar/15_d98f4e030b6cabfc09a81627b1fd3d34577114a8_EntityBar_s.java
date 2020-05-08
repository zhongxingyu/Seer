 /*
  * This file has been adapted for use with Heroes 
  * from mmoMinecraft (https://github.com/mmoMinecraftDev).
  * 
  * mmoMinecraft is free software: you can redistribute it and/or modify
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
  * 
  * 
  */
 package com.herocraftonline.dev.heroes.spout.gui;
 
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.getspout.spoutapi.gui.Color;
 import org.getspout.spoutapi.gui.Container;
 import org.getspout.spoutapi.gui.ContainerType;
 import org.getspout.spoutapi.gui.GenericContainer;
 import org.getspout.spoutapi.gui.GenericGradient;
 import org.getspout.spoutapi.gui.Gradient;
 import org.getspout.spoutapi.gui.Label;
 import org.getspout.spoutapi.gui.RenderPriority;
 import org.getspout.spoutapi.gui.Widget;
 import org.getspout.spoutapi.gui.WidgetAnchor;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.spout.SpoutUI;
 
 public class EntityBar extends GenericContainer {
 
     private Container bar;
     private Label barLabel;
     private Gradient hpBar;
     private Gradient armorBar;
     private int health = 100;
     private int armor = 100;
     private int def_width = 80;
     private int def_height = 14;
     private int old_health_width = -1;
     private int old_armor_width = -1;
     private final Heroes plugin;
     String face = "~";
     String label = "";
 
     public EntityBar(Heroes plugin) {
         super();
         this.plugin = plugin;
         Color color = new Color(0, 0, 0, 0.75f);
 
         this.addChildren( 
                 bar = (Container) new GenericContainer(    // Used for the bar, this.children with an index 1+ are targets
                         new GenericGradient().setTopColor(color).setBottomColor(color).setPriority(RenderPriority.Highest),
                         new GenericContainer(hpBar = (Gradient) new GenericGradient(), armorBar = (Gradient) new GenericGradient()).setMargin(1).setPriority(RenderPriority.High))
                 .setLayout(ContainerType.OVERLAY).setMaxHeight(def_height).setMargin(0, 0, 1, 0));
                 this.setAlign(WidgetAnchor.TOP_LEFT);
                 this.setMinWidth(def_width);
                 //              .setMaxWidth(def_width * 2)
                 this.setMaxHeight(def_height + 1);
 
         color = new Color(1f, 0, 0, 0.75f);
         hpBar.setTopColor(color).setBottomColor(color);
         color = new Color(0.75f, 0.75f, 0.75f, 0.75f);
         armorBar.setTopColor(color).setBottomColor(color);
     }
 
     /**
      * Set the display from a possibly offline player
      * @param name the target
      * @return this
      */
     public EntityBar setEntity(String name) {
         return setEntity(name, "");
     }
 
     /**
      * Set the display from a possibly offline player.
      * @param name the target
      * @param prefix a string to show before the name
      * @return this
      */
     public EntityBar setEntity(String name, String prefix) {
         Player player = this.getPlugin().getServer().getPlayer(name);
         if (player != null && player.isOnline()) {
             return setEntity(player, prefix);
         }
         setHealth(0);
         setArmor(0);
         setLabel((!"".equals(prefix) ? prefix : "") + SpoutUI.getColor(screen != null ? screen.getPlayer() : null, null) + name);
         return this;
     }
 
     /**
      * Set the display from a player or living entity.
      * @param entity the target
      * @return this
      */
     public EntityBar setEntity(LivingEntity entity) {
         return setEntity(entity, "");
     }
 
     /**
      * Set the display from a player or living entity.
      * @param entity the target
      * @param prefix a string to show before the name
      * @return this
      */
     public EntityBar setEntity(LivingEntity entity, String prefix) {
         if (entity != null && entity instanceof LivingEntity) {
             setHealth(plugin.getHealthPercent(entity)); // Needs a maxHealth() check
             setArmor(SpoutUI.getArmor(entity));
             setLabel((!"".equals(prefix) ? prefix : "") + SpoutUI.getColor(screen != null ? screen.getPlayer() : null, entity) + SpoutUI.getSimpleName(entity, !(getContainer() instanceof EntityBar)));
         } else {
             setHealth(0);
             setArmor(0);
             setLabel("");
         }
         return this;
     }
 
     /**
      * Set the targets of this entity - either actual targets, or pets etc.
      * @param targets a list of targets
      * @return this
      */
     public EntityBar setTargets(Heroes plugin, LivingEntity... targets) {
         Widget[] widgets = this.getChildren();
         if (targets == null) {
             targets = new LivingEntity[0]; // zero-length array is easier to handle
         }
         for (int i=targets.length + 1; i<widgets.length; i++) {
             this.removeChild(widgets[i]);
         }
         for (int i=0; i<targets.length; i++) {
             EntityBar child;
             if (widgets.length > i + 1) {
                 child = (EntityBar) widgets[i+1];
             } else {
                 this.addChild(child = new EntityBar(plugin));
                 child.bar.setMarginLeft(def_width / 4);
             }
             child.setEntity(targets[i]);
         }
         setMaxHeight((targets.length + 1) * (def_height + 1));
         if (getContainer() instanceof Container) {
             getContainer().updateLayout();
         } else {
             updateLayout();
         }
         return this;
     }
 
     /**
      * Set the health to display.
      * @param health percentage
      * @return this
      */
     public EntityBar setHealth(int health) {
         if (this.health != health) {
             this.health = health;
             updateLayout();
         }
         return this;
     }
 
     /**
      * Set the health colour to use.
      * @param color the solid colour for the health bar
      * @return this
      */
     public EntityBar setHealthColor(Color color) {
         hpBar.setTopColor(color).setBottomColor(color);
         return this;
     }
 
     /**
      * Set the armor to display.
      * @param armor percentage
      * @return this
      */
     public EntityBar setArmor(int armor) {
         if (this.armor != armor) {
             this.armor = armor;
             updateLayout();
         }
         return this;
     }
 
     /**
      * Set the armor colour to use.
      * @param color the solid colour for the health bar
      * @return this
      */
     public EntityBar setArmorColor(Color color) {
         armorBar.setTopColor(color).setBottomColor(color);
         return this;
     }
 
     /**
      * Set the label to use.
      * @param label the string to display
      * @return this
      */
     public EntityBar setLabel(String label) {
         if (!this.label.equals(label)) {
             this.label = label;
             barLabel.setText(label).setDirty(true);
             updateLayout();
         }
         return this;
     }
 
     @Override
     public Container updateLayout() {
         armorBar.setWidth(old_armor_width); // cache for bandwidth
         hpBar.setWidth(old_health_width); // cache for bandwidth
         super.updateLayout();
         old_armor_width = armorBar.getWidth(); // cache for bandwidth
         old_health_width = hpBar.getWidth(); // cache for bandwidth
         armorBar.setWidth((armorBar.getContainer().getWidth() * armor) / 100).setDirty(true);
         hpBar.setWidth((hpBar.getContainer().getWidth() * health) / 100).setDirty(true);
         return this;
     }
 }
