 package to.joe.util.Packeteer;
 
 import net.minecraft.server.Packet20NamedEntitySpawn;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.getspout.spout.packet.standard.MCCraftPacket;
 import org.getspout.spoutapi.packet.listener.PacketListener;
 import org.getspout.spoutapi.packet.standard.MCPacket;
 
 import to.joe.util.Vanish;
 
 public class Packeteer20NamedEntitySpawn implements PacketListener {
 
     private final Vanish vanish;
 
     public Packeteer20NamedEntitySpawn(Vanish vanish) {
         this.vanish = vanish;
     }
 
     @Override
     public boolean checkPacket(Player player, MCPacket packet) {
         final Packet20NamedEntitySpawn packet20 = (Packet20NamedEntitySpawn) ((MCCraftPacket) packet).getPacket();
         if (this.vanish.isEIDVanished(packet20.a)) {
             packet20.b = ChatColor.DARK_AQUA + packet20.b;
            if(packet20.b.length()==16){
                 packet20.b=packet20.b.substring(0, 15);
             }
         }
         return !this.vanish.shouldHide(player, packet20.a);
     }
 
 }
