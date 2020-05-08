 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.znickq.playerplus;
 
 import java.util.ArrayList;
 import java.util.List;
 import me.znickq.playerplus.widgets.*;
 import org.bukkit.Material;
 import org.getspout.spoutapi.gui.GenericButton;
 import org.getspout.spoutapi.gui.GenericPopup;
 import org.getspout.spoutapi.gui.GenericTexture;
 import org.getspout.spoutapi.gui.ListWidget;
 import org.getspout.spoutapi.gui.ListWidgetItem;
 import org.getspout.spoutapi.gui.WidgetAnchor;
 import org.getspout.spoutapi.player.SpoutPlayer;
 import org.getspout.spoutapi.player.accessories.AccessoryType;
 
 /**
  *
  * @author ZNickq
  */
 public class TextureChooser extends GenericPopup {
 
 	private PlayerPlus instance;
 	private SpoutPlayer player;
 	private ListWidget lw;
 	private MyComboBox cb;
 	private GenericTexture gt;
 	private AccessoryType current;
 	private List<WebAccessory> list;
 
 	public TextureChooser(PlayerPlus instance, SpoutPlayer player) {
 		this.instance = instance;
 		this.player = player;
 		current = AccessoryType.BRACELET;
 
 		cb = new MyComboBox(this);
 		cb.setText("Accessories");
 		cb.setAnchor(WidgetAnchor.CENTER_CENTER);
		cb.setX(-160).setY(-110);
 		cb.setHeight(20).setWidth(100);
 		cb.setSelection(0);
 		cb.setItems(getAvailableAccessories(player));
 
 		lw = new MyListWidget(this);
 		lw.setAnchor(WidgetAnchor.CENTER_CENTER);
 		lw.setHeight(150).setWidth(100);
		lw.setX(-160).setY(-70);
 		updateList();
 
 		gt = new GenericTexture();
 		gt.setAnchor(WidgetAnchor.CENTER_CENTER);
 		gt.setHeight(200).setWidth(200);
		gt.setX(-10).setY(-110);
 		updateTexture();
 
 		GenericButton pre = new ActionButton("<", this, -1);
 		pre.setAnchor(WidgetAnchor.CENTER_CENTER);
 		pre.setHeight(20).setWidth(20);
		pre.setX(-10).setY(80);
 
 		GenericButton select = new ActionButton("Select", this, 0);
 		select.setAnchor(WidgetAnchor.CENTER_CENTER);
 		select.setHeight(20).setWidth(50);
		select.setX(59).setY(80);
 
 		GenericButton next = new ActionButton(">", this, 1);
 		next.setAnchor(WidgetAnchor.CENTER_CENTER);
 		next.setHeight(20).setWidth(20);
		next.setX(148).setY(80);
 
 		updateSelection();
 		
 		attachWidgets(instance, lw, cb, gt, pre, select, next);
 		player.getMainScreen().attachPopupScreen(this);
 	}
 
 	public void updateTexture() {
 		int sel = lw.getSelectedRow();
 		if (sel < 1) {
 			gt.setUrl("");
 		} else {
 			gt.setUrl(list.get(sel - 1).getUrl());
 		}
 		gt.setDirty(true);
 	}
 
 	public void updateList() {
 		lw.clear();
 		list = instance.getAvailable(current);
 		lw.addItem(new ListWidgetItem("None", ""));
 
 
 		for (WebAccessory toAdd : list) {
 			lw.addItem(new ListWidgetItem(toAdd.getName(), "", toAdd.getUrl()));
 		}
 		lw.setDirty(true);
 	}
 
 	private List<String> getAvailableAccessories(SpoutPlayer player) {
 		//TODO Dockter, good place to put permision check in ^^
 		List<String> available = new ArrayList<String>();
 		for (AccessoryType type : AccessoryType.values()) {
 			available.add(type.name().toLowerCase());
 		}
 		return available;
 	}
 
 	public void onSelected(int item) {
 		if (lw != null && item != -1) {
 			current = AccessoryType.values()[item];
 			updateList();
 			updateSelection();
 			updateTexture();
 		}
 	}
 
 	public void onListSelected(int item) {
 		updateTexture();
 	}
 
 	public void onActionClick(int id) {
 		if (id == 0) {
 			if (lw.getSelectedRow() > 0) {
 				player.addAccessory(current, list.get(lw.getSelectedRow() - 1).getUrl());
 			} else {
 				System.out.println("Removing!");
 				player.removeAccessory(current);
 			}
 			player.sendNotification("Set Accessory", current.name().toLowerCase(), Material.GOLD_CHESTPLATE);
 			instance.save(player, current);
 			return;
 		}
 		int cuRow = lw.getSelectedRow();
 		cuRow += id;
 		if (cuRow == -1) {
 			cuRow = lw.getItems().length - 1;
 		}
 		if (cuRow == lw.getItems().length) {
 			cuRow = 0;
 		}
 		lw.setSelection(cuRow);
 		lw.setDirty(true);
 		updateTexture();
 	}
 
 	private void updateSelection() {
 		int which = 0;
 		String url = player.getAccessoryURL(current);
 		if(url == null) {
 			lw.setSelection(which);
 			return;
 		}
 		for(int i=0;i<list.size();i++) {
 			WebAccessory wa = list.get(i);
 			if(url.equals(wa.getUrl())) {
 				which = i + 1;
 				break;
 			}
 		}
 		lw.setSelection(which);
 	}
 }
