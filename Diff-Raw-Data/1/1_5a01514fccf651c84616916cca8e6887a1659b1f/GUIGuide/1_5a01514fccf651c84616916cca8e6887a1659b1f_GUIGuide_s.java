 package net.dockter.sguide.gui;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import net.dockter.sguide.Main;
 import net.dockter.sguide.guide.Guide;
 import net.dockter.sguide.guide.GuideManager;
 import org.getspout.spoutapi.gui.Color;
 import org.getspout.spoutapi.gui.ComboBox;
 import org.getspout.spoutapi.gui.GenericButton;
 import org.getspout.spoutapi.gui.GenericComboBox;
 import org.getspout.spoutapi.gui.GenericGradient;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.gui.GenericPopup;
 import org.getspout.spoutapi.gui.GenericTextField;
 import org.getspout.spoutapi.gui.GenericTexture;
 import org.getspout.spoutapi.gui.RenderPriority;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class GUIGuide extends GenericPopup {
 
 	final GenericTextField guideField, guideInvisible;
 	final GenericLabel guideName, guideDate;
 	final GenericButton close, newb, saveb, deleteb, bb;
 	final ComboBox box;
 	private final SpoutPlayer player;
 
 	public GUIGuide(SpoutPlayer player) {
 		super();
 		this.player = player;
 		// Label
 		GenericLabel label = new GenericLabel(Main.getInstance().getConfig().getString("PromptTitle"));
 		label.setX(175).setY(25);
 		label.setPriority(RenderPriority.Lowest);
 		label.setWidth(-1).setHeight(-1);
 
 		guideName = new GenericLabel("TehGuideName");
 		guideName.setWidth(-1).setHeight(-1);
 		guideName.setX(85).setY(42);
 
 		guideInvisible = new GenericTextField();
 		guideInvisible.setWidth(150);
 		guideInvisible.setHeight(14);
 		guideInvisible.setX(85).setY(37);
 		guideInvisible.setVisible(false);
 
 		guideDate = new GenericLabel(new SimpleDateFormat("HH:mm dd-MM").format(Calendar.getInstance().getTime()));
 		guideDate.setWidth(-1).setHeight(-1);
 		guideDate.setX(175).setY(42);
 
 		box = new MyCombo(this);
 		box.setText("Guides");
 		refreshItems();
 		box.setX(275);
 		box.setY(38);
 		box.setWidth(49);
 		box.setHeight(14);
 
 
 		// Border
 		GenericTexture border = new GenericTexture("http://www.almuramc.com/images/sguide.png");
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
 
 		// TextBox
 		guideField = new GenericTextField();
 		guideField.setText("first guide goes here"); // The default text
 		//textField.setCursorPosition(3); // Puts the cursor after the third character
 		// guideField.setFieldColor(new Color(1.0F, 1.0F, 1.0F, 1.0F)); // White background
 		guideField.setBorderColor(new Color(0, 0, 0, 1.0F)); // Black border
 		guideField.setMaximumCharacters(200);
 		guideField.setHeight(130).setWidth(262);
 		guideField.setX(84).setY(60);
 
 		//Close Button
 		close = new CloseButton(this);
 		close.setAuto(false).setX(310).setY(200).setHeight(14).setWidth(40);
 		
 		bb = new BypassButton(player, this);
 		bb.setAuto(false).setX(280).setY(200).setHeight(14).setWidth(14);
 
 
 		this.setTransparent(true);
 		attachWidgets(Main.getInstance(), border, gradient, label);
 
 		this.setTransparent(true);
 		attachWidget(Main.getInstance(), gradient);
 		attachWidget(Main.getInstance(), label);
 		attachWidget(Main.getInstance(), border);
 		attachWidget(Main.getInstance(), guideField);
 		attachWidget(Main.getInstance(), close);
 		attachWidget(Main.getInstance(), guideName);
 		attachWidget(Main.getInstance(), guideInvisible);
 		attachWidget(Main.getInstance(), guideDate);
 		attachWidget(Main.getInstance(), box);
 		if(Main.getInstance().canBypass(player.getName())|| player.hasPermission("spoutguide.bypass")|| player.hasPermission("spoutguide.admin"))
 			attachWidget(Main.getInstance(), bb);
 
 		// Attach New / Edit / Save / Delete buttons
 		if (player.hasPermission("spoutguide.edit") || player.hasPermission("spoutguide.admin")) {
 
 			saveb = new SaveButton(this);
 			saveb.setAuto(false).setX(130).setY(200).setHeight(14).setWidth(40);
 			attachWidget(Main.getInstance(), saveb);
 		} else {
 			saveb = null;
 		}
 
 		if (player.hasPermission("spoutguide.admin"));
 		{
 			// Add "Set as Default" checkbox
 		}
 
 		if (player.hasPermission("spoutguide.create") || player.hasPermission("spoutguide.edit") || player.hasPermission("spoutguide.admin")) {
 			// Add New Button
 			newb = new NewButton(this);
 			newb.setAuto(false).setX(80).setY(200).setHeight(14).setWidth(40);
 			attachWidget(Main.getInstance(), newb);
 		} else {
 			newb = null;
 		}
 
 		if (player.hasPermission("spoutguide.delete") || player.hasPermission("spoutguide.admin")) {
 			deleteb = new DeleteButton(this);
 			deleteb.setAuto(false).setX(180).setY(200).setHeight(14).setWidth(50);
 			attachWidget(Main.getInstance(), deleteb);
 		} else {
 			deleteb = null;
 		}
 
 		if (player.hasPermission("spoutguide.bypass") || player.hasPermission("spoutguide.admin")) {
 			// Add bypass Button since Checkbox widget doesnt exist.
 		}
 
 		//if (plugin.getConfig().debug) {
 		//logger.enable("Database " + (plugin.getConfig().STORAGE_TYPE));
 		//}
 
 		//this.attachWidget(plugin, configure); 
 		setGuide(GuideManager.getLoadedGuides().get(box.getItems().get(0)));
 	}
 	private Guide guide;
 
 	public void setGuide(Guide guide) {
 		if (guide == null) {
 			return;
 		}
 		this.guide = guide;
 		guideDate.setText(guide.getDate());
 		guideName.setText(guide.getName());
 		guideField.setText(guide.getText());
 
 	}
 
 	public void onNewClick() {
 		setGuide(new Guide("", "", ""));
 		guideName.setVisible(false);
 		guideInvisible.setVisible(true);
 	}
 
 	void onSaveClick() {
 		guide.setText(guideField.getText());
 		guide.setDate(new SimpleDateFormat("HH:mm dd-MM").format(Calendar.getInstance().getTime()));
 		if (guideInvisible.isVisible()) {
 			guide.setName(guideInvisible.getText());
 			guideName.setText(guideInvisible.getText());
 			guideInvisible.setVisible(false);
 			guideName.setVisible(true);
 			GuideManager.addGuide(guide);
 		}
 		guide.save();
 		refreshItems();
 	}
 
 	void onDeleteClick() {
 		GuideManager.removeLoadedGuide(guideName.getText());
 		refreshItems();
 		setGuide(GuideManager.getLoadedGuides().get(box.getItems().get(0)));
 	}
 
 	void onCloseClick() {
 		player.closeActiveWindow();
 	}
 
 	void onSelect(int i, String text) {
 		setGuide(GuideManager.getLoadedGuides().get(text));
 	}
 
 	private void refreshItems() {
 		List<String> items = new ArrayList<String>();
 		for (String gguide : GuideManager.getLoadedGuides().keySet()) {
 			if (player.hasPermission("spoutguide.view." + gguide) || player.hasPermission("spoutguide.view")) {
 				items.add(gguide);
 			}
 		}
 		box.setItems(items);
 	}
 }
