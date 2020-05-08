 package littlegruz.marioworld.gui;
 
 import littlegruz.marioworld.MarioMain;
 import littlegruz.marioworld.entities.MarioPlayer;
 
 import org.bukkit.entity.Player;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.player.SpoutPlayer;
 import org.getspout.spoutapi.gui.Color;
 
 public class MarioGUI{
    MarioMain plugin;
    private GenericLabel lifeLabel;
    private GenericLabel coinLabel;
    private GenericLabel stateLabel;
    private GenericLabel lives;
    private GenericLabel coins;
    private GenericLabel state;
    private GenericLabel gameover;
    
    public MarioGUI(MarioMain instance){
       this.plugin = instance;
       lifeLabel = new GenericLabel(plugin.getCurrentRB().getString("Lives"));
       coinLabel = new GenericLabel(plugin.getCurrentRB().getString("Coins"));
       stateLabel = new GenericLabel(plugin.getCurrentRB().getString("State"));
       lives = new GenericLabel();
       coins = new GenericLabel();
       state = new GenericLabel();
       gameover = new GenericLabel(plugin.getCurrentRB().getString("GameOver"));
       
       //Note: Max screen widget X value is about 430 and a row is a Y value of 10
       coinLabel.setX(87);
       stateLabel.setX(195);
       lifeLabel.setX(302);
       coins.setX(96);
       coins.setY(10);
       state.setX(195);
       state.setY(10);
       lives.setX(312);
       lives.setY(10);
       coins.setHeight(9);
       coins.setWidth(80);
       state.setHeight(9);
       state.setWidth(80);
       lives.setHeight(9);
       lives.setWidth(80);
       coinLabel.setHeight(9);
       coinLabel.setWidth(80);
       stateLabel.setHeight(9);
       stateLabel.setWidth(80);
       lifeLabel.setHeight(9);
       lifeLabel.setWidth(80);
       coinLabel.setTextColor(new Color(255,215,0));
       coins.setTextColor(new Color(255,215,0));
       stateLabel.setTextColor(new Color(255,153,18));
       state.setTextColor(new Color(255,153,0));
       lifeLabel.setTextColor(new Color(255,0,0));
       lives.setTextColor(new Color(255,0,0));
       
       gameover.setHeight(30);
       gameover.setWidth(180);
       gameover.setX(160);
       gameover.setY(30);
    }
    
    public void update(Player player){
       SpoutPlayer sp = SpoutManager.getPlayer(player);
       MarioPlayer mp = plugin.getPlayerMap().get(player.getName());
       lifeLabel.setText(plugin.getCurrentRB().getString("Lives"));
       coinLabel.setText(plugin.getCurrentRB().getString("Coins"));
       stateLabel.setText(plugin.getCurrentRB().getString("State"));
       gameover.setText(plugin.getCurrentRB().getString("GameOver"));
       sp.getMainScreen().attachWidget(plugin, lifeLabel);
       sp.getMainScreen().attachWidget(plugin, coinLabel);
       sp.getMainScreen().attachWidget(plugin, stateLabel);
      state.setText(mp.getState());
       lives.setText(Integer.toString(mp.getLives()));
       coins.setText(Integer.toString(mp.getCoins()));
       sp.getMainScreen().attachWidget(plugin, lives);
       sp.getMainScreen().attachWidget(plugin, coins);
       sp.getMainScreen().attachWidget(plugin, state);
    }
    
    public void remove(Player player){
       SpoutManager.getPlayer(player).getMainScreen().removeWidgets(plugin);
    }
    
    public void placeGameOver(Player player){
       SpoutManager.getPlayer(player).getMainScreen().attachWidget(plugin, gameover);
    }
    
    public void removeGameOver(Player player){
       SpoutManager.getPlayer(player).getMainScreen().removeWidget(gameover);
    }
 }
