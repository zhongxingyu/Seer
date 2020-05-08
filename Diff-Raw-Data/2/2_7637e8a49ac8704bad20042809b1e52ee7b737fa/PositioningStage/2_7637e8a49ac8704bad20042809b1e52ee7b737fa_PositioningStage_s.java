 package fr.aumgn.diamondrush.stage;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 import fr.aumgn.bukkitutils.util.Util;
 import fr.aumgn.bukkitutils.geom.Vector;
 import fr.aumgn.diamondrush.DiamondRush;
 import fr.aumgn.diamondrush.game.Team;
 import fr.aumgn.diamondrush.stage.listeners.NoPVPListener;
 import fr.aumgn.diamondrush.stage.listeners.PositioningListener;
 
 public abstract class PositioningStage extends Stage {
 
     protected Map<Team, Vector> positions;
     protected Map<Team, Player> playersHoldingBlock;
     private List<Listener> listeners;
 
     public PositioningStage(DiamondRush dr, Map<Team, Player> playersHoldingBlock) {
         super(dr);
         this.positions = new HashMap<Team, Vector>();
         this.playersHoldingBlock = playersHoldingBlock;
         this.listeners = new ArrayList<Listener>(2);
         this.listeners.add(new NoPVPListener(dr.getGame()));
         this.listeners.add(new PositioningListener(dr, this));
     }
 
     public PositioningStage(DiamondRush dr) {
         this(dr, new HashMap<Team, Player>());
     }
 
     @Override
     public List<Listener> getListeners() {
         return listeners;
     }
 
     @Override
     public void stop() {
         removeBlocksFromInventories();
         removeBlocksFromWorld();
         for (Team team : dr.getGame().getTeams()) {
             Vector pos = getPosition(team);
             initPosition(team, pos);
         }
     }
 
     public boolean isPosition(Team team, Vector position) {
         return position.equalsBlock(positions.get(team));
     }
 
     public Vector getPosition(Team team) {
         Vector pos = positions.get(team);
         if (pos == null) {
             Player player = playersHoldingBlock.get(team);
             if (player == null) {
                 player = Util.pickRandom(team.getPlayers());
                 playersHoldingBlock.put(team, player);
             }
            pos = new Vector(player.getLocation());
             pos = pos.addZ(2);
         }
         return pos;
     }
 
     public void setPosition(Team team, Vector position) {
         positions.put(team, position);
     }
 
     public void setPlayerHoldingBlock(Team team, Player player) {
         positions.remove(team);
         playersHoldingBlock.put(team, player);
     }
 
     public void clearPositions() {
         positions.clear();
     }
 
     public void giveBlock(Player player) {
         ItemStack stack = new ItemStack(getMaterial(), 1);
         if (player.getItemInHand().getType() == Material.AIR) {
             player.setItemInHand(stack);
         } else {
             player.getInventory().addItem(stack);
         }
     }
 
     protected void removeBlocksFromWorld() {
         for (Vector pos : positions.values()) {
             Block block = pos.toBlock(dr.getGame().getWorld());
             if (block.getType() == getMaterial()) {
                 block.setType(Material.AIR);
             }
         }
     }
 
     protected void removeBlocksFromInventories() {
         for (Team team : dr.getGame().getTeams()) {
             for (Player player : team.getPlayers()) {
                 if (removeBlockFromInventory(player)) {
                     break;
                 }
             }
         }
     }
 
     public boolean removeBlockFromInventory(Player player) {
         Inventory inventory = player.getInventory();
         int index = inventory.first(getMaterial());
         if (index == -1) {
             return false;
         }
         ItemStack stack = inventory.getItem(index);
         if (stack.getAmount() > 1) {
             stack.setAmount(stack.getAmount() - 1);
         } else {
             inventory.clear(index);
         }
         return true;
     }
 
     public abstract void initPosition(Team team, Vector vector);
 
     public abstract Material getMaterial();
 }
