 /**
  * 
  */
 package de.findus.cydonia.appstates;
 
 import java.awt.image.BufferedImage;
 import java.text.SimpleDateFormat;
 
 import com.jme3.niftygui.RenderImageJme;
 import com.jme3.texture.Image;
 import com.jme3.texture.Texture2D;
 import com.jme3.texture.plugins.AWTLoader;
 
 import de.findus.cydonia.level.Flag;
 import de.findus.cydonia.main.GameController;
 import de.findus.cydonia.player.Player;
 import de.lessvoid.nifty.builder.PanelBuilder;
 import de.lessvoid.nifty.builder.TextBuilder;
 import de.lessvoid.nifty.elements.Element;
 import de.lessvoid.nifty.elements.render.ImageRenderer;
 import de.lessvoid.nifty.elements.render.TextRenderer;
 import de.lessvoid.nifty.render.NiftyImage;
 import de.lessvoid.nifty.spi.render.RenderImage;
 import de.lessvoid.nifty.tools.SizeValue;
 
 /**
  * This AppState controls the menu.
  * Should be attached when the game is paused and the menu should be visible.
  * @author Findus
  *
  */
 public class MenuController {
 
 	private static final SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss");
 	
 	/**
 	 * Path to the menu description xml files.
 	 */
 	public static final String MENU_PATH = "de/findus/cydonia/gui/menu/";
 	
 	private GameController gameController;
 	
 	private Element timetext;
 	private Element flagredtext;
 	private Element flagbluetext;
 	private Element healthpointstext;
 	private Element	inventoryimg;
 	private Element scoreboardlayer;
 	private Element messagelayer;
 	private Element messagetext;
 	private Element hudlayer;
 	private Element scorespanel;
 	private Element eventpanel;
 	
 	
 	public MenuController(GameController game) {
 		this.gameController = game;
 		
 		gameController.getNifty().registerScreenController(gameController);
 		gameController.getNifty().fromXmlWithoutStartScreen(MENU_PATH + "menu.xml");
 		
 		this.timetext = gameController.getNifty().getScreen("ingamescreen").findElementByName("timetext");
 		this.flagbluetext = gameController.getNifty().getScreen("ingamescreen").findElementByName("flagbluetext");
 		this.flagredtext = gameController.getNifty().getScreen("ingamescreen").findElementByName("flagredtext");
 		this.healthpointstext = gameController.getNifty().getScreen("ingamescreen").findElementByName("healthpointstext");
 		this.inventoryimg = gameController.getNifty().getScreen("ingamescreen").findElementByName("inventoryimg");
 		this.scoreboardlayer = gameController.getNifty().getScreen("ingamescreen").findElementByName("scoreboardlayer");
 		this.messagelayer = gameController.getNifty().getScreen("ingamescreen").findElementByName("messagelayer");
 		this.messagetext = gameController.getNifty().getScreen("ingamescreen").findElementByName("messagetext");
 		this.hudlayer = gameController.getNifty().getScreen("ingamescreen").findElementByName("hudlayer");
 		this.scorespanel = gameController.getNifty().getScreen("ingamescreen").findElementByName("scorespanel");
 		this.eventpanel = gameController.getNifty().getScreen("ingamescreen").findElementByName("eventpanel");
 	}
 	
 	public void actualizeScreen() {
 		switch (gameController.getGamestate()) {
 		case LOBBY:
 			gameController.getNifty().gotoScreen("lobbymenu");
 			gameController.getInputManager().setCursorVisible(true);
 			break;
 
 		case MENU:
 			gameController.getNifty().gotoScreen("pausemenu");
 			gameController.getInputManager().setCursorVisible(true);
 			break;
 
 		case SPECTATE:
 			gameController.getNifty().gotoScreen("ingamescreen");
 			gameController.getInputManager().setCursorVisible(false);
 			hideHUD();
 			showMessage("Press 'Fire' to join the game!");
 			break;
 
 		case RUNNING:
 			gameController.getNifty().gotoScreen("ingamescreen");
 			gameController.getInputManager().setCursorVisible(false);
 			hideMessage();
 			showHUD();
 			break;
 
 		case ROUNDOVER:
 			gameController.getNifty().gotoScreen("ingamescreen");
 			gameController.getInputManager().setCursorVisible(false);
 			hideHUD();
 			String message = "Round is over. New round will start automatically in a view seconds...";
 			int winteam = gameController.getWinTeam();
 			if(winteam == 1) {
 				message += "\nBlue team is the winner!";
 			}else if(winteam == 2) {
 				message += "\nRed team is the winner!";
 			}
 			showMessage(message);
 			break;
 
 		default:
 			break;
 		}
 	}
 	
 	public void showScoreboard() {
 		updateScoreboard();
 		scoreboardlayer.setVisible(true);
 	}
 	
 	public void hideScoreboard() {
 		scoreboardlayer.setVisible(false);
 	}
 	
 	public void showMessage(String msg) {
 		messagetext.getRenderer(TextRenderer.class).setText(msg);
 		messagelayer.setVisible(true);
 	}
 	
 	public void hideMessage() {
 		messagelayer.setVisible(false);
 	}
 	
 	public void showHUD() {
 		hudlayer.setVisible(true);
 	}
 	
 	public void hideHUD() {
 		hudlayer.setVisible(false);
 	}
 
 	public void updateHUD() {
 		this.timetext.getRenderer(TextRenderer.class).setText(timeFormat.format(gameController.getRemainingTime()));
 		
		this.flagbluetext.setVisible(false);
		this.flagredtext.setVisible(false);
 		for(Flag f : gameController.getWorldController().getAllFlags()) {
			if(f.getTeam() == 1 && !f.isInBase()) {
				this.flagbluetext.setVisible(true);
			}else if(f.getTeam() == 2 && !f.isInBase()) {
				this.flagredtext.setVisible(true);
 			}
 		}
 		
 		if(gameController.getPlayer() != null && gameController.getPlayer().getCurrentEquipment() != null) {
 			BufferedImage img = gameController.getPlayer().getCurrentEquipment().getHUDImage();
 
 			AWTLoader loader =new AWTLoader();
 			Image imageJME = loader.load(img, true);
 			
 			RenderImage rimg = new RenderImageJme(new Texture2D(imageJME));
 			NiftyImage nimg = new NiftyImage(gameController.getNifty().getRenderEngine(), rimg);
 			this.inventoryimg.getRenderer(ImageRenderer.class).setImage(nimg);
 			this.inventoryimg.setConstraintWidth(new SizeValue(String.valueOf(nimg.getWidth())));
 			this.hudlayer.layoutElements();
 		}
 		
 		if(gameController.getPlayer() != null) {
 			this.healthpointstext.getRenderer(TextRenderer.class).setText(String.valueOf(Math.round(gameController.getPlayer().getHealthpoints())));
 		}
 	}
 	
 	public void updateScoreboard() {
 		Element team1 = this.scorespanel.findElementByName("team1");
 		for(Element e : team1.getElements()) {
 			e.markForRemoval();
 		}
 		
 		Element team2 = this.scorespanel.findElementByName("team2");
 		for(Element e : team2.getElements()) {
 			e.markForRemoval();
 		}
 		
 		PanelBuilder teampb = new ScoreLineBuilder("Team 1", "", "#4488", "ffff");
 		teampb.build(gameController.getNifty(), gameController.getNifty().getScreen("ingamescreen"), team1);
 		
 		teampb = new ScoreLineBuilder("Team 2", "", "#8448", "ffff");
 		teampb.build(gameController.getNifty(), gameController.getNifty().getScreen("ingamescreen"), team2);
 		
 		PanelBuilder pb;
 		for(Player p : gameController.getPlayerController().getAllPlayers()) {
 			String name = p.getName();
 			String score = String.valueOf(p.getScores());
 			String color = "#8888";
 			
 			if(p.getTeam() == 1) {
 				if(p.getId() == gameController.getPlayer().getId()) {
 					color = "#7798";
 				}
 				pb = new ScoreLineBuilder(name, score, color, "#ffff");
 				pb.build(gameController.getNifty(), gameController.getNifty().getScreen("ingamescreen"), team1);
 			}else if(p.getTeam() == 2){
 				if(p.getId() == gameController.getPlayer().getId()) {
 					color = "#9778";
 				}
 				pb = new ScoreLineBuilder(name, score, color, "#ffff");
 				pb.build(gameController.getNifty(), gameController.getNifty().getScreen("ingamescreen"), team2);
 			}
 		}
 	}
 	
 	public void displayEvent(final String event) {
 		TextBuilder tb = new TextBuilder(){{
 			textHAlignLeft();
 			color("#80f9");
 			font("de/findus/cydonia/gui/fonts/aurulent-sans-16.fnt");
 			text(event);
 		}};
 		tb.build(gameController.getNifty(), gameController.getNifty().getScreen("ingamescreen"), this.eventpanel);
 		
 		if(this.eventpanel.getElements().size() > 4) {
 			this.eventpanel.getElements().get(0).markForRemoval();
 		}
 	}
 	
 	public void clearEventPanel() {
 		for(Element e : this.eventpanel.getElements()) {
 			e.markForRemoval();
 		}
 	}
 	
 	private class ScoreLineBuilder extends PanelBuilder {
 		
 		private ScoreLineBuilder(final String name, final String score, final String bgcolor, final String fontcolor)
 		{
 			padding("2px");
 			childLayoutHorizontal();
 			panel(new PanelBuilder(){{
 				childLayoutHorizontal();
 				backgroundColor(bgcolor);
 				width("90%");
 				padding("2px");
 				text(new TextBuilder(){{
 					textHAlignLeft();
 					color(fontcolor);
 					font("de/findus/cydonia/gui/fonts/aurulent-sans-16.fnt");
 					text(name);
 				}});
 			}});
 			panel(new PanelBuilder(){{
 				childLayoutHorizontal();
 				backgroundColor(bgcolor);
 				width("10%");
 				padding("2px");
 				text(new TextBuilder(){{
 					textHAlignRight();
 					color(fontcolor);
 					font("de/findus/cydonia/gui/fonts/aurulent-sans-16.fnt");
 					text(score);
 				}});
 			}});
 		}
 	}
 	
 }
