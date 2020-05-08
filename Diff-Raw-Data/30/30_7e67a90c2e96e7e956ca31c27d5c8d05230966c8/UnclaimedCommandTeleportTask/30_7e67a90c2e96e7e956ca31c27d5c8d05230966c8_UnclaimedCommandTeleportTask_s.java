 package net.betterverse.unclaimed.commands;
 
 import org.bukkit.entity.Player;
 
 import java.util.HashSet;
 import java.util.Set;
 
 public class UnclaimedCommandTeleportTask implements Runnable {
 
    private static Set<Player> cooling = new HashSet<Player>();
    private Player player;
 
 
    public UnclaimedCommandTeleportTask(Player player) {
        cooling.add(player);
    }
 
    public static boolean isCooling(Player player) {
        return cooling.contains(player);
    }

    @Override
    public void run() {
        cooling.remove(player);
    }
 }
