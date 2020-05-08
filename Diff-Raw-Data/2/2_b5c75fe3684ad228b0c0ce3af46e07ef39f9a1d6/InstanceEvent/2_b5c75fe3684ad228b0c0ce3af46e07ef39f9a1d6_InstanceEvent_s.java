 package btwmods.player;
 
 import java.util.EventObject;
 import java.util.HashMap;
 import java.util.Map;
 
 import btwmods.IMod;
 import btwmods.events.IEventInterrupter;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.NBTTagCompound;
 
 public class InstanceEvent extends EventObject implements IEventInterrupter {
 	
 	public enum TYPE { LOGIN, LOGOUT, RESPAWN, READ_NBT, WRITE_NBT, CHECK_METADATA, METADATA_CHANGED };
 	public enum METADATA { IS_PVP };
 
 	private TYPE type;
 	private EntityPlayer playerInstance = null;
 	private RespawnPosition respawnPosition = null;
 	private NBTTagCompound tagCompound = null;
 	private NBTTagCompound modTagCompound = null;
 	
 	private METADATA metadata = null;
 	private Object metadataValue = null;
 	private boolean isMetadataValueSet = false;
 	
 	private final static Map<METADATA, Class> metadataReturnTypes = new HashMap<METADATA, Class>();
 	
 	static {
 		for (METADATA metadata : METADATA.values()) {
 			switch (metadata) {
 				case IS_PVP:
 					metadataReturnTypes.put(metadata, Boolean.class);
 					break;
 			}
 		}
 	}
 	
 	public TYPE getType() {
 		return type;
 	}
 	
 	public EntityPlayer getPlayerInstance() {
 		return playerInstance;
 	}
 	
 	public RespawnPosition getRespawnPosition() {
 		return respawnPosition;
 	}
 	
 	public NBTTagCompound getNBTTagCompound() {
 		return modTagCompound == null ? tagCompound : modTagCompound;
 	}
 	
 	public void setRespawnPosition(RespawnPosition respawnPosition) {
 		this.respawnPosition = respawnPosition;
 	}
 	
 	public METADATA getMetadata() {
 		return metadata;
 	}
 	
 	public void setMetadataValue(Object val) throws IllegalArgumentException {
 		Class cls = metadataReturnTypes.get(metadata);
 		if (cls == null) {
 			throw new IllegalArgumentException("Metadata value class has not been set for: " + metadata.toString());
 		}
 		else if (cls.isPrimitive()) {
 			throw new IllegalArgumentException("Metadata value class should not be a primative (" + cls.getSimpleName() + ") for: " + metadata.toString());
 		}
 		else if (!cls.isAssignableFrom(val.getClass())) {
 			throw new IllegalArgumentException("The value for " + metadata.toString() + " must be " + cls.getSimpleName() + " not " + val.getClass().getSimpleName());
 		}
 		else {
 			isMetadataValueSet = true;
 			metadataValue = val;
 		}
 	}
 	
 	public boolean getIsMetadataValueSet() {
 		return isMetadataValueSet;
 	}
 	
 	public Object getMetadataValue() {
 			return metadataValue;
 	}
 	
 	public boolean getMetadataBooleanValue() throws IllegalStateException {
 		if (metadataValue instanceof Boolean)
 			return ((Boolean)metadataValue).booleanValue();
 		else
 			throw new IllegalStateException("Metadata value is set to " + (metadataValue == null ? "null" : metadataValue.getClass().getSimpleName()) + " but should be " + Boolean.class.getSimpleName());
 	}
 	
 	public int getMetadataIntValue() throws IllegalStateException {
 		if (metadataValue instanceof Integer)
 			return ((Integer)metadataValue).intValue();
 		else
 			throw new IllegalStateException("Metadata value is set to " + (metadataValue == null ? "null" : metadataValue.getClass().getSimpleName()) + " but should be " + Integer.class.getSimpleName());
 	}
 	
 	public long getMetadataLongValue() throws IllegalStateException {
 		if (metadataValue instanceof Long)
 			return ((Long)metadataValue).longValue();
 		else
 			throw new IllegalStateException("Metadata value is set to " + (metadataValue == null ? "null" : metadataValue.getClass().getSimpleName()) + " but should be " + Long.class.getSimpleName());
 	}
 	
 	public double getMetadataDoubleValue() throws IllegalStateException {
 		if (metadataValue instanceof Double)
 			return ((Double)metadataValue).doubleValue();
 		else
 			throw new IllegalStateException("Metadata value is set to " + (metadataValue == null ? "null" : metadataValue.getClass().getSimpleName()) + " but should be " + Double.class.getSimpleName());
 	}
 	
 	public static InstanceEvent Login(EntityPlayer playerInstance) {
 		InstanceEvent event = new InstanceEvent(TYPE.LOGIN, playerInstance);
 		event.playerInstance = playerInstance;
 		return event;
 	}
 	
 	public static InstanceEvent Logout(EntityPlayer playerInstance) {
 		InstanceEvent event = new InstanceEvent(TYPE.LOGOUT, playerInstance);
 		event.playerInstance = playerInstance;
 		return event;
 	}
 	
 	public static InstanceEvent Respawn(EntityPlayer playerInstance) {
 		InstanceEvent event = new InstanceEvent(TYPE.RESPAWN, playerInstance);
 		event.playerInstance = playerInstance;
 		return event;
 	}
 
 	public static InstanceEvent ReadFromNBT(EntityPlayer playerInstance, NBTTagCompound nbtTagCompound) {
 		InstanceEvent event = new InstanceEvent(TYPE.READ_NBT, playerInstance);
 		event.playerInstance = playerInstance;
 		event.tagCompound = nbtTagCompound;
 		return event;
 	}
 
 	public static InstanceEvent WriteToNBT(EntityPlayer playerInstance, NBTTagCompound nbtTagCompound) {
 		InstanceEvent event = new InstanceEvent(TYPE.WRITE_NBT, playerInstance);
 		event.playerInstance = playerInstance;
 		event.tagCompound = nbtTagCompound;
 		return event;
 	}
 	
 	public static InstanceEvent CheckMetadata(EntityPlayer playerInstance, METADATA metadata) {
 		InstanceEvent event = new InstanceEvent(TYPE.CHECK_METADATA, playerInstance);
 		event.playerInstance = playerInstance;
 		event.metadata = metadata;
 		return event;
 	}
 	
 	public static InstanceEvent MetadataChanged(EntityPlayer playerInstance, METADATA metadata, Object newValue) {
		InstanceEvent event = new InstanceEvent(TYPE.CHECK_METADATA, playerInstance);
 		event.playerInstance = playerInstance;
 		event.metadata = metadata;
 		event.setMetadataValue(newValue);
 		return event;
 	}
 	
 	private InstanceEvent(TYPE type, EntityPlayer playerInstance) {
 		super(playerInstance);
 		this.type = type;
 	}
 
 	@Override
 	public boolean isInterrupted() {
 		return (type == TYPE.RESPAWN && respawnPosition != null) ||
 				(type == TYPE.CHECK_METADATA && isMetadataValueSet);
 	}
 	
 	public void setModCompound(IMod mod) {
 		modTagCompound = tagCompound.getCompoundTag(mod.getClass().getName());
 	}
 	
 	public void unsetModCompound() {
 		if (type == TYPE.WRITE_NBT && modTagCompound != null && modTagCompound.getTags().size() != 0) {
 			tagCompound.setTag(modTagCompound.getName(), modTagCompound);
 		}
 	}
 }
