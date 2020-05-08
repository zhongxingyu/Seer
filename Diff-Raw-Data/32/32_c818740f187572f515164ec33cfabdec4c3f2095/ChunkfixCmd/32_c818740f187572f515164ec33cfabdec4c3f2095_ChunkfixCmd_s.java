 package Commands;
 
 import java.util.ArrayList;
 
 import org.bukkit.Chunk;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import Util.CommandHandle;
 
 public class ChunkfixCmd {
 	
 	@CommandHandle(name = "chunkfix", perms = "ec.chunkfix")
     public static boolean chunkfix(CommandSender s, String[] args) {
         if (!(s instanceof Player)) return false;
         final ArrayList<Chunk> chunks = new ArrayList();
         final Player p = ((Player) s);
         if (args.length >= 1) {
             try {
                 final int radius = Integer.parseInt(args[0]);
                 final int chunkval = (radius * 16);
                 int minX = p.getLocation().getBlockX() - chunkval;
                 int minZ = p.getLocation().getBlockZ() - chunkval;
                 int maxX = p.getLocation().getBlockX() + chunkval;
                 int maxZ = p.getLocation().getBlockZ() + chunkval;
                 for (int opx = minX; opx <= maxX; opx += 16) {
                     for (int opz = minZ; opz <= maxZ; opz += 16) {
                         chunks.add(p.getWorld().getChunkAt(opx, opz));
                     }
                 }
             } catch (NumberFormatException e) {
                 s.sendMessage("7" + args[0] + " cis not a valid number");
                 return true;
             }
         } else {
             chunks.add(p.getWorld().getChunkAt(p.getLocation()));
         }
         
         int op = 0;
         for (Chunk chunk : chunks) {
             if (p.getWorld().refreshChunk(chunk.getX(), chunk.getZ()))
                 op++;
         }
         
         s.sendMessage("aRefreshede " + op + " achunks");
         return true;
     }
     
}

}
