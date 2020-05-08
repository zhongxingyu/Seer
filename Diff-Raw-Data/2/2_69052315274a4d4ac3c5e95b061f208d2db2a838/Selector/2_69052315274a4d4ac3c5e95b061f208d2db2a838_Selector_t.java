 package net.windwaker.textureme.gui;
 
 import net.windwaker.textureme.TextureMe;
 import net.windwaker.textureme.gui.container.ConfigMenu;
 import net.windwaker.textureme.gui.widget.CloseButton;
 import net.windwaker.textureme.gui.widget.ConfigureButton;
 import net.windwaker.textureme.gui.widget.SelectButton;
 import net.windwaker.textureme.gui.widget.TexturePackList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.getspout.spoutapi.gui.Color;
 import org.getspout.spoutapi.gui.GenericButton;
 import org.getspout.spoutapi.gui.GenericGradient;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.gui.GenericListWidget;
 import org.getspout.spoutapi.gui.GenericPopup;
 import org.getspout.spoutapi.gui.GenericTexture;
 import org.getspout.spoutapi.gui.RenderPriority;
 
 public class Selector extends GenericPopup {
 	
 	public TextureMe plugin;
 	private GenericListWidget list;
 	
 	private GenericButton select;
 	private GenericButton close;
 	private GenericButton configure;
 	
 	public Selector(TextureMe plugin, Player player) {
 		this.plugin = plugin;
 		
 		// Label
 		GenericLabel label = new GenericLabel(plugin.getConfig().getString("prompt title"));
 		label.setX(175).setY(25);
 		label.setPriority(RenderPriority.Lowest);
 		label.setWidth(-1).setHeight(-1);
 		
 		// Border
 		GenericTexture border = new GenericTexture("http://dl.dropbox.com/u/27507830/GuildCraft/Images/HUD/blue.png");
 		border.setX(65).setY(20);
 		border.setPriority(RenderPriority.High);
 		border.setWidth(300).setHeight(200);
 		
 		// Background gradient
 		GenericGradient gradient = new GenericGradient();
 		gradient.setTopColor(new Color(0.25F, 0.25F, 0.25F, 1.0F));
 		gradient.setBottomColor(new Color(0.35F, 0.35F, 0.35F, 1.0F));
 		gradient.setWidth(300).setHeight(200);
 		gradient.setX(65).setY(20);
 		gradient.setPriority(RenderPriority.Highest);
 		
 		// Texture list
 		list = new TexturePackList(plugin);
 		list.setX(90).setY(50);
 		list.setWidth(250).setHeight(125);
 		list.setPriority(RenderPriority.Lowest);
 		
 		// Close button
 		close = new CloseButton();
 		close.setX(155).setY(195);
 		close.setWidth(60).setHeight(20);
 		close.setPriority(RenderPriority.Lowest);
 				
 		// Configure button
 		configure = new ConfigureButton(plugin, list, new ConfigMenu(plugin));
 		configure.setX(215).setY(195);
 		configure.setWidth(60).setHeight(20);
 		configure.setPriority(RenderPriority.Lowest);
 		
 		// Select button
 		select = new SelectButton(plugin, list);
 		select.setX(95).setY(195);
 		select.setWidth(60).setHeight(20);
 		select.setPriority(RenderPriority.Lowest);
 		
 		this.setTransparent(true);
 		this.attachWidgets(plugin, border, gradient, select, close, label, list);
 		
 		// Attach configure button
 		if (player.hasPermission("textureme.configure") || player.isOp()) { this.attachWidget(plugin, configure); }
 	}
 }
