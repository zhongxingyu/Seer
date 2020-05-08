 package com.wimbli.TexturePackMenu;
 
 import java.util.ArrayList;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.ChatColor;
 
 import org.getspout.spoutapi.event.screen.ButtonClickEvent;
 import org.getspout.spoutapi.gui.Color;
 import org.getspout.spoutapi.gui.GenericButton;
 import org.getspout.spoutapi.gui.GenericPopup;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.player.SpoutPlayer;
 import org.getspout.spoutapi.SpoutManager;
 
 
 // SCREEN SIZE NOTE: scaled screen size seems to always be precisely 427x240... not sure why 427 width, but there you have it
 
 public class TPMPopup extends GenericPopup
 {
 	private Plugin tmpPlugin;
 	private SpoutPlayer sPlayer;
 	private GenericButton bNext, bPrev;
 	private ArrayList<GenericButton> bChoice = new ArrayList<GenericButton>(10);
 	private String[] packNames = new String[0];
 	private int page = 0, maxPage = 0;
 
 
 	public static void create(Plugin plugin, Player player)
 	{
 		if (!player.hasPermission("texturepackmenu.texture")) {
 			player.sendMessage("You do not have the necessary permission to choose a texture pack.");
 			return;
 		}
 
 		TPMPopup newPopup = new TPMPopup(plugin, player);
 		newPopup.initiate();
 	}
 
 	public TPMPopup(Plugin mainPlugin, Player player)
 	{
 		if (player == null)
 			return;
 
 		sPlayer = SpoutManager.getPlayer(player);
 		if (sPlayer == null)
 			return;
 
 		this.tmpPlugin = mainPlugin;
 
 		if (Config.texturePackCount() == 0)
 		{
 			player.sendMessage("Sorry, but no texture packs are currently configured.");
 			return;
 		}
 
 		if (!sPlayer.isSpoutCraftEnabled())
 		{
 			player.sendMessage("This only works with the Spoutcraft client. See here:");
 			player.sendMessage("              "+ChatColor.BLUE+"http://bit.ly/spoutclient");
 			return;
 		}
 
 		packNames = Config.texPackNames();
		maxPage = (int)Math.ceil((double)packNames.length / 10.0) - 1;
 	}
 
 	public void initiate()
 	{
 		this.initLabels();
 		this.initOtherButtons();
 		this.initChoiceButtons();
 		this.refreshButtons();
 
 		sPlayer.getMainScreen().attachPopupScreen(this); // Show the player the popup
 	}
 
 	public void exit()
 	{
 		sPlayer.getMainScreen().closePopup();
 	}
 
 
 
 	private void makeChoice(int buttonIndex)
 	{
 		Config.setPack(sPlayer, (page * 10) + buttonIndex);
 		exit();
 	}
 
 	private void refreshButtons()
 	{
 		bPrev.setEnabled(page > 0);
 		bNext.setEnabled(page < maxPage);
 
 		int loop, offset = page * 10;
 
 		for (loop = 0; loop < 10; loop++) {
 			int index = offset + loop;
 			if (index > packNames.length - 1)
 				break;
 
 			String text = packNames[index];
 			GenericButton btn = bChoice.get(loop);
 
 			btn.setTextColor(new Color(255,255,255,0));
 
 			if (index == 0)
 			{	// default pack
 				text = "* " + text;
 				btn.setTextColor(new Color(127,255,255,0));
 			}
 			if (Config.getPack(sPlayer.getName()).equals(packNames[index]))
 			{	// current pack
 				text = "@ " + text;
 				btn.setTextColor(new Color(191,255,191,0));
 			}
 
 			btn.setText(text);
 			btn.setVisible(true);
 		}
 		while (loop < 10)
 		{
 			bChoice.get(loop).setVisible(false);
 			loop++;
 		}
 		this.setDirty(true);
 	}
 
 	private void nextPage()
 	{
 		if (page < maxPage)
 			page += 1;
 		refreshButtons();
 	}
 
 	private void lastPage()
 	{
 		if (page > 0)
 			page -= 1;
 		refreshButtons();
 	}
 
 
 
 	private void initLabels()
 	{
 		GenericLabel label = new GenericLabel("Choose a texture pack below:");
 		label.setTextColor(new Color(63,255,63,0));
 		label.setScale(2.0f);
 		label.setX(64).setY(20);
 		this.attachWidget(tmpPlugin, label);
 
 		label = new GenericLabel("* - Default Pack");
 		label.setX(207).setY(191);
 		label.setTextColor(new Color(127,255,255,0));
 		this.attachWidget(tmpPlugin, label);
 
 		label = new GenericLabel("@ - Current Pack");
 		label.setX(204).setY(201);
 		label.setTextColor(new Color(191,255,191,0));
 		this.attachWidget(tmpPlugin, label);
 	}
 
 	private void initOtherButtons()
 	{
 		GenericButton cancel = new GenericButton("Cancel")
 		{
 			@Override
 			public void onButtonClick(ButtonClickEvent event)
 			{
 				exit();
 			}
 		};
 		cancel.setWidth(95).setHeight(20);
 		cancel.setX(311).setY(190);
 		cancel.setTextColor(new Color(255,191,191,0));
 		this.attachWidget(tmpPlugin, cancel);
 
 		bPrev = new GenericButton("< Prev Page")
 		{
 			@Override
 			public void onButtonClick(ButtonClickEvent event)
 			{
 				lastPage();
 			}
 		};
 		bPrev.setWidth(80).setHeight(20);
 		bPrev.setX(21).setY(190);
 		if (maxPage == 0)
 			bPrev.setVisible(false);
 		this.attachWidget(tmpPlugin, bPrev);
 
 		bNext = new GenericButton("Next Page >")
 		{
 			@Override
 			public void onButtonClick(ButtonClickEvent event)
 			{
 				nextPage();
 			}
 		};
 		bNext.setWidth(80).setHeight(20);
 		bNext.setX(105).setY(190);
 		if (maxPage == 0)
 			bNext.setVisible(false);
 		this.attachWidget(tmpPlugin, bNext);
 	}
 
 	private void initChoiceButtons()
 	{
 		bChoice = new ArrayList<GenericButton>(10);
 
 		boolean rowToggle = true;
 		GenericButton current;
 		final int bWidth = 190, bHeight = 20, offsetLeft1 = 21, offsetLeft2 = 216;
 		int offsetTop = 50;
 
 		for (int i = 0; i < 10; i++)
 		{
 			final int idx = i;
 
 			current = new GenericButton(Integer.toString(i+1))
 			{
 				int index = idx;
 
 				@Override
 				public void onButtonClick(ButtonClickEvent event)
 				{
 					makeChoice(index);
 				}
 			};
 
 			current.setWidth(bWidth).setHeight(bHeight);
 			current.setX(rowToggle ? offsetLeft1 : offsetLeft2).setY(offsetTop);
 
 			bChoice.add(current);
 			this.attachWidget(tmpPlugin, current);
 
 			rowToggle ^= true;
 			if (rowToggle)
 				offsetTop += 25;
 		}
 	}
 }
