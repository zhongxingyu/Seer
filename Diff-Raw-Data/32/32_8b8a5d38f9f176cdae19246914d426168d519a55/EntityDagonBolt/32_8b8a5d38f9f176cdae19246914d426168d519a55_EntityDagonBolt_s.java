 package hunternif.mc.dota2items.effect;
 
 import hunternif.mc.dota2items.client.particle.ParticleDagon;
 import hunternif.mc.dota2items.entity.TemporaryEntity;
 import hunternif.mc.dota2items.util.Vec3Chain;
 import hunternif.mc.dota2items.util.Vec3Chain.Segment;
 import net.minecraft.client.Minecraft;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.Vec3;
 import net.minecraft.world.World;
 
 public class EntityDagonBolt extends TemporaryEntity {
 	private static final String TAG_END_X = "D2IDagonBoltEndX";
 	private static final String TAG_END_Y = "D2IDagonBoltEndY";
 	private static final String TAG_END_Z = "D2IDagonBoltEndZ";
 	
 	public double endX;
 	public double endY;
 	public double endZ;
 	
 	public int level;
 	
 	public Vec3Chain chain;
 
 	public EntityDagonBolt(World world) {
 		super(world);
 		ignoreFrustumCheck = true;
 	}
 	
 	public EntityDagonBolt(World world, int level,
 			double startX, double startY, double startZ,
 			double endX, double endY, double endZ) {
 		super(world);
 		this.level = level;
 		setPosition(startX, startY, startZ);
 		setEndCoords(endX, endY, endZ);
 	}
 	
 	@Override
 	public int getDuration() {
 		return 8;
 	}
 	
 	public void setEndCoords(double endX, double endY, double endZ) {
 		this.endX = endX;
 		this.endY = endY;
 		this.endZ = endZ;
 		if (this.worldObj.isRemote) {
			chain = new Vec3Chain(posX, posY, posZ, endX, endY, endZ);
			chain.subdivide(0.3, 0.2, 0.3, 0.2);
			for (Segment seg : chain.segments) {
				Vec3 segVec = seg.getVector();
				int particlesPerSegment = rand.nextInt(level*2);
				for (int i = 0; i < particlesPerSegment; i++) {
					ParticleDagon particle = new ParticleDagon(worldObj,
							seg.start.xCoord + segVec.xCoord * rand.nextDouble(),
							seg.start.yCoord + segVec.yCoord * rand.nextDouble(),
							seg.start.zCoord + segVec.zCoord * rand.nextDouble());
					Minecraft.getMinecraft().effectRenderer.addEffect(particle);
				}
 			}
 		}
 	}
 	
 	@Override
 	public boolean shouldRenderInPass(int pass) {
 		return pass == 1;
 	}
 
 	@Override
 	protected void readEntityFromNBT(NBTTagCompound tag) {
 		setEndCoords(tag.getDouble(TAG_END_X), tag.getDouble(TAG_END_Y), tag.getDouble(TAG_END_Z));
 	}
 
 	@Override
 	protected void writeEntityToNBT(NBTTagCompound tag) {
 		tag.setDouble(TAG_END_X, endX);
 		tag.setDouble(TAG_END_Y, endY);
 		tag.setDouble(TAG_END_Z, endZ);
 	}
 }
