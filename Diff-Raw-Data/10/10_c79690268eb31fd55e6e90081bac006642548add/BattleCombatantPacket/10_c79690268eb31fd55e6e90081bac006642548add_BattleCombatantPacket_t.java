 package burnedkirby.TurnBasedMinecraft.core.network;
 
 import java.io.EOFException;
 import java.io.IOException;
 
 import net.minecraft.entity.player.EntityPlayer;
 
 import burnedkirby.TurnBasedMinecraft.CombatantInfo;
 import burnedkirby.TurnBasedMinecraft.ModMain;
 import burnedkirby.TurnBasedMinecraft.CombatantInfo.Type;
 import burnedkirby.TurnBasedMinecraft.core.ClientProxy;
 import burnedkirby.TurnBasedMinecraft.gui.BattleGui;
 
 import com.google.common.io.ByteArrayDataInput;
 import com.google.common.io.ByteArrayDataOutput;
 
 import cpw.mods.fml.relauncher.Side;
 
 /**
  * This packet is sent from server to player with information on a combatant that is
  * in the player's current battle.
  * This is sent as a response from the player's BattleQueryPacket.
  *
  */
 public class BattleCombatantPacket extends CommandPacket {
 
 	CombatantInfo combatant;
 	
 	public BattleCombatantPacket(CombatantInfo combatant)
 	{
 		this.combatant = combatant;
 	}
 	
 	public BattleCombatantPacket() {
 		combatant = new CombatantInfo();
 	}
 	
 	@Override
 	public void write(ByteArrayDataOutput out) {
 		out.writeBoolean(combatant.isPlayer);
 		out.writeInt(combatant.id);
 		out.writeBoolean(combatant.isSideOne);
 		out.writeUTF(combatant.name);
 		out.writeBoolean(combatant.ready);
 		out.writeInt(combatant.type.ordinal());
 		out.writeInt(combatant.target);
 	}
 
 	@Override
 	public void read(ByteArrayDataInput in) throws ProtocolException {
 		combatant.isPlayer = in.readBoolean();
 		combatant.id = in.readInt();
 		combatant.isSideOne = in.readBoolean();
 		combatant.name = in.readUTF();
 		combatant.ready = in.readBoolean();
 		combatant.type = Type.values()[in.readInt()];
 		combatant.target = in.readInt();
 	}
 
 	@Override
 	public void execute(EntityPlayer player, Side side)
 			throws ProtocolException {
 		if(side.isServer())
 		{
 			
 		}
 		else
 		{
			if(ModMain.proxy.getGui() != null)
				((BattleGui)ModMain.proxy.getGui()).receiveCombatant(combatant);
 		}
 	}
 
 }
