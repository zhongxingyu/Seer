 package boq.cctags.tag.access;
 
 import java.lang.ref.WeakReference;
 
 import net.minecraft.util.Vec3;
 import boq.cctags.EntityPacketHandler;
 import boq.cctags.tag.EntityTag;
 import boq.cctags.tag.TagData;
 
 import com.google.common.base.Preconditions;
 
 public class EntityTagAccess implements ITagAccess {
 
     public interface IPositionProvider {
         public Vec3 getPosition();
     }
 
     private WeakReference<EntityTag> tag;
     private final IPositionProvider position;
 
     public EntityTagAccess(EntityTag tag, IPositionProvider position) {
         this.tag = new WeakReference<EntityTag>(tag);
         this.position = position;
     }
 
     @Override
     public boolean isValid() {
         EntityTag e = tag.get();

         if (e == null)
             return false;
 
         if (!e.isDead) {
             Vec3 ownPos = position.getPosition();
             Vec3 tagPos = Vec3.createVectorHelper(e.posX, e.posY, e.posZ);
 
             if (ownPos.distanceTo(tagPos) < 2.0)
                 return true;
         }
 
         tag.clear();
         return false;
     }
 
     @Override
     public TagData readData() {
         return tag.get().data;
     }
 
     @Override
     public void writeData(TagData data, boolean updateClients) {
         EntityTag e = tag.get();
         Preconditions.checkState(data == e.data);
 
         if (updateClients)
             EntityPacketHandler.sendUpdateToAllTrackers(e);
     }
 
     @Override
     public int uid() {
         return tag.get().data.uid(tag);
     }
 
 }
