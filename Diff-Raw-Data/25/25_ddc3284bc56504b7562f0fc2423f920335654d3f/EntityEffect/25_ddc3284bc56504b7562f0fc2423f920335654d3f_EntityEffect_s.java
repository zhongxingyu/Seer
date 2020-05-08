 package hunternif.mc.dota2items.effect;
 
 import java.util.Arrays;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.entity.Entity;
 
 import com.google.common.io.ByteArrayDataInput;
 import com.google.common.io.ByteArrayDataOutput;
 
 public abstract class EntityEffect extends Effect {
 
 	public EntityEffect(int id) {
 		super(id);
 	}
 	
 	@Override
 	public final void perform(EffectInstance inst) {
 		if (inst.data.length > 0 && inst.data[0] instanceof Entity) {
 			Entity entity = (Entity)inst.data[0];
 			perform(entity, Arrays.copyOfRange(inst.data, 1, inst.data.length));
 		}
 	}
 	
 	public abstract void perform(Entity entity, Object ... data);
 
 	@Override
 	public Object[] readInstanceData(ByteArrayDataInput in) {
 		Entity entity = null;
 		if (Minecraft.getMinecraft().theWorld != null) {
 			entity = Minecraft.getMinecraft().theWorld.getEntityByID(in.readInt());
 		}
 		return new Object[] {entity};
 	}
 
 	@Override
 	public void writeInstanceData(Object[] data, ByteArrayDataOutput out) {
 		if (data.length > 0 && data[0] instanceof Entity) {
 			out.writeInt(((Entity)data[0]).entityId);
 		}
 	}
 
 }
