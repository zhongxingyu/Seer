 package de.tobiyas.enderdragonsplus.entity.dragon;
 
 import static de.tobiyas.enderdragonsplus.util.MinecraftChatColorUtils.decodeColors;
 
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import javax.naming.OperationNotSupportedException;
 
 import net.minecraft.server.v1_6_R2.DamageSource;
 import net.minecraft.server.v1_6_R2.Entity;
 import net.minecraft.server.v1_6_R2.EntityComplexPart;
 import net.minecraft.server.v1_6_R2.EntityEnderDragon;
 import net.minecraft.server.v1_6_R2.EntityLiving;
 import net.minecraft.server.v1_6_R2.GenericAttributes;
 import net.minecraft.server.v1_6_R2.LocaleI18n;
 import net.minecraft.server.v1_6_R2.MathHelper;
 import net.minecraft.server.v1_6_R2.NBTTagCompound;
 import net.minecraft.server.v1_6_R2.Vec3D;
 import net.minecraft.server.v1_6_R2.World;
 
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;
 import org.bukkit.craftbukkit.v1_6_R2.entity.CraftEntity;
 import org.bukkit.craftbukkit.v1_6_R2.event.CraftEventFactory;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.Vector;
 
 import de.tobiyas.enderdragonsplus.EnderdragonsPlus;
 import de.tobiyas.enderdragonsplus.entity.dragon.age.AgeContainer;
 import de.tobiyas.enderdragonsplus.entity.dragon.age.AgeNotFoundException;
 import de.tobiyas.enderdragonsplus.entity.dragon.controllers.DragonHealthController;
 import de.tobiyas.enderdragonsplus.entity.dragon.controllers.DragonMoveController;
 import de.tobiyas.enderdragonsplus.entity.dragon.controllers.FireballController;
 import de.tobiyas.enderdragonsplus.entity.dragon.controllers.ItemLootController;
 import de.tobiyas.enderdragonsplus.entity.dragon.controllers.NBTTagDragonStore;
 import de.tobiyas.enderdragonsplus.entity.dragon.controllers.NBTTagDragonStore.DragonNBTReturn;
 import de.tobiyas.enderdragonsplus.entity.dragon.controllers.PropertyController;
 import de.tobiyas.enderdragonsplus.entity.dragon.controllers.TargetController;
 
 public class LimitedEnderDragon extends EntityEnderDragon {
 	
 	private EnderdragonsPlus plugin = EnderdragonsPlus.getPlugin();
 	public static int broadcastedError = 0;
 
 	private int logicCall = 0;
 	
 	private FireballController fireballController;
 	private TargetController targetController;
 	private ItemLootController itemController;
 	private DragonHealthController dragonHealthController;
 	private DragonMoveController dragonMoveController;
 	private AgeContainer ageContainer;
 	private PropertyController propertyController;
 	
 	private boolean doNothingLock = false;
 	private Vector oldSpeed;
 	private Vector oldTarget;
 	
 
 	public LimitedEnderDragon(Location location, World world) {
 		this(location, world, "Normal");
 	}
 
 	public LimitedEnderDragon(Location location, World world, UUID uid) {
 		this(location, world, uid, "Normal");
 	}
 
 	public LimitedEnderDragon(Location location, World world, String ageType){
 		super(world);
 
 		setPosition(location.getX(), location.getY(), location.getZ());
 		createAllControllers(ageType, location);
 	}
 	
 	public LimitedEnderDragon(Location location, World world, UUID uid, String ageType) {
 		super(world);
 		changeUUID(uid);
 		
 		setPosition(location.getX(), location.getY(), location.getZ());
 		createAllControllers(ageType, location);
 	}
 
 	/**
 	 * This is the Craft Bukkit spawn
 	 * Controllers are set from the NBTTag
 	 * @param world
 	 */
 	public LimitedEnderDragon(World world) {
 		super(world);
 	}
 	
 	private void createAllControllers(DragonNBTReturn returnContainer){
 		propertyController = new PropertyController(returnContainer);
 		ageContainer = returnContainer.getAgeContainer();
 		
 		ageContainer.setSpawnHealth(returnContainer.getCurrentHealth());
 		
 		targetController = new TargetController(returnContainer.getHomeLocation(), this, ageContainer.isHostile());
 		fireballController = new FireballController(targetController);
 		itemController = new ItemLootController(this);
 		dragonHealthController = new DragonHealthController(this);
 		dragonMoveController = new DragonMoveController(this);
 		
 		this.uniqueID = returnContainer.getUuid();
 		
 		initStats();
 	}
 	
 	private void createAllControllers(String ageType, Location homeLocation){
 		boolean hostile = plugin.interactConfig().getConfig_dragonsAreHostile();
 		try {
 			ageContainer = plugin.getAgeContainerManager().getAgeContainer(ageType);
 		} catch (AgeNotFoundException e) {
 			ageContainer = plugin.getAgeContainerManager().getNormalAgeContainer();
 		}
 		
 		propertyController = new PropertyController();
 		targetController = new TargetController(homeLocation, this, hostile);
 		fireballController = new FireballController(targetController);
 		itemController = new ItemLootController(this);
 		dragonHealthController = new DragonHealthController(this);
 		dragonMoveController = new DragonMoveController(this);
 		
 		initStats();
 	}
 	
 	//This overrides the health setting
 	/*@Override
 	protected void ay(){
 		super.ay();
 		this.getAttributeInstance(GenericAttributes.a).setValue((float) ageContainer.getMaxHealth());
 	}*/
 	
 	private void initStats(){
 		expToDrop = ageContainer.getExp();
 		setHealth((float) ageContainer.getSpawnHealth());
 		this.getAttributeInstance(GenericAttributes.a).setValue((float) ageContainer.getMaxHealth()); //sets health to correct value
 		plugin.getContainer().registerDragon(this);
 		
 		
 		String dragonName = decodeColors(ageContainer.getAgePrettyName()) + " Dragon";
 		if(dragonName.length() > 50){
 			dragonName = dragonName.substring(0, 15);
 		}
 		
 		this.setCustomName(dragonName);
 	}
 	
 	
 	/** This method sets the Life of the EnderDragon
 	 * @see net.minecraft.server.v1_6_R2.v1_5_R2.EntityEnderDragon#a()
 	 */
 	@Override
 	protected void a() {
 		super.a();
 		/*  Actually doing this: 
 		this.datawatcher.a(8, Integer.valueOf(0));
 		this.datawatcher.a(9, Byte.valueOf(0));
 		this.datawatcher.a(16, new Integer(this.getMaxHealth()));*/
 	}
 	
 	@Override
 	protected void dropDeathLoot(boolean flag, int i) {
 		// CraftBukkit start - whole method
 		List<ItemStack> loot = generateLoot();
 
         CraftEventFactory.callEntityDeathEvent(this, loot); // raise event even for those times when the entity does not drop loot
         // CraftBukkit end
     }
 
 	
 
 	@Override
 	public String getLocalizedName() {
 		return LocaleI18n.get("entity.EnderDragon.name");
 	}
 
 	
 	@Override
 	public boolean dealDamage(DamageSource damagesource, float i) { // CraftBukkit - protected -> public
 		dragonHealthController.rememberDamage(damagesource, i);
 		restoreOldDataIfPossible();
 		return super.dealDamage(damagesource, i);
 	}
 	
 
 	private void restoreOldDataIfPossible() {
 		doNothingLock = false;
 		
 		if(oldSpeed != null){
 			this.motX = oldSpeed.getX();
 			this.motY = oldSpeed.getY();
 			this.motZ = oldSpeed.getZ();
 			
 			oldSpeed = null;
 		}
 		
 		if(oldTarget != null){
 			this.h = oldTarget.getX();
 			this.i = oldTarget.getY();
 			this.j = oldTarget.getZ();
 			
 			oldTarget = null;
 		}
 	}
 
 	/**
 	 *  Logic call. All Dragon logic on tick
 	 * @see net.minecraft.server.v1_6_R2.v1_4_R1.Enderdragon#
 	 */
 	@Override
 	public void c(){
 		try{
 			internalLogicTick();
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	
 	@SuppressWarnings("unchecked")
 	public void internalLogicTick(){
 		logicCall++;
 		this.bx = this.by;
 		
 		dragonHealthController.recheckHealthNotOvercaped();
 		boolean shouldSitDown = plugin.interactConfig().getConfig_dragonsSitDownIfInactive();
 		
 
 		//locks dragons to do absolutely nothing...
 		if(doNothingLock){
 			return;
 		}
 		
 		float f;
 		float f1;
 		float f2;
 
 		if (this.getHealth() <= 0)
 			return;
 
 		dragonHealthController.checkRegainHealth();
 		
 		f = 0.2F / (MathHelper.sqrt(this.motX * this.motX + this.motZ
 				* this.motZ) * 10.0F + 1);
 		f *= (float) Math.pow(2.0D, this.motY);
 		
 		if (this.bA) {
 			this.by += f * 0.5F;
 		} else {
 			this.by += f;
 		}
 
 		this.yaw = MathHelper.g(this.yaw);
 
 		if (this.bo < 0) {
             for (int d05 = 0; d05 < this.bn.length; ++d05) {
                 this.bn[d05][0] = (double) this.yaw;
                 this.bn[d05][1] = this.locY;
             }
         }
 		
 		if (++this.bo == this.bn.length) {
             this.bo = 0;
         }
 
         this.bn[this.bo][0] = this.yaw;
         this.bn[this.bo][1] = this.locY;
 
 
 		double d0 = this.h - this.locX;
 		double d1 = this.i - this.locY;
 		double d2 = this.j - this.locZ;
 		double d3 = d0 * d0 + d1 * d1 + d2 * d2;
 		double f3 = 0.6;
 		
 		Entity currentTarget = targetController.getCurrentTarget();
 		boolean attackingMode = true;
 		if (currentTarget != null) {
 			this.h = currentTarget.locX;
 			this.j = currentTarget.locZ;
 			
 			double d4 = this.h - this.locX;
 			double d5 = this.j - this.locZ;
 			double d6 = Math.sqrt(d4 * d4 + d5 * d5);
 			double d7 = 0.4 + d6 / 80D - 1;
 
 			if (d7 > 10.0D) {
 				d7 = 10.0D;
 			}
 
 			this.i = currentTarget.boundingBox.b + d7;
 		} else {
 			if(!targetController.hasTargets() && !targetController.isFlyingHome() && shouldSitDown){
 				attackingMode = false;
 				oldSpeed = new Vector()
 							.setX(motX)
 							.setY(motY)
 							.setZ(motZ);
 				
 				this.motX = 0;
 				this.motY = 0;
 				this.motZ = 0;
 				
 				oldTarget = new Vector()
 							.setX(this.h)
 							.setY(this.i)
 							.setZ(this.j);
 						
 				this.h = this.locX;
 				this.i = this.locY;
 				this.j = this.locZ;
 				this.yaw = 0;
 				
 				Location loc = this.getLocation().clone();
 				loc.subtract(0, 1, 0);
 				if(loc.getBlock().getType() == Material.AIR){
 					this.motY = -0.2;
 					this.i = this.locY-0.2;
 				}else{
 					doNothingLock = true;
 				}
 			}else{
 				this.h += this.random.nextGaussian() * 2D;
 				this.j += this.random.nextGaussian() * 2D;
 			}
 			
 		}
 
 		if (this.bz || (d3 < 100.0D) || d3 > 22500D || this.positionChanged
 				|| this.H) {
 			targetController.changeTarget(false);
 		}
 
 		d1 /= MathHelper.sqrt(d0 * d0 + d2 * d2);
 		f3 = 0.6F;
 		if (d1 < -f3) {
 			d1 = -f3;
 		}
 
 		if (d1 > f3) {
 			d1 = f3;
 		}
 		
 		this.motY += d1 * 0.1;
         this.yaw = MathHelper.g(this.yaw);
 		double d8 = 180.0D - Math.atan2(d0, d2) * 180.0D / Math.PI;
 		double d9 = MathHelper.g(d8 - (double) this.yaw);
 
 		if (d9 > 50.0D) {
 			d9 = 50.0D;
 		}
 
 		if (d9 < -50.0D) {
 			d9 = -50.0D;
 		}
 
 		Vec3D vec3d = this.world.getVec3DPool().create(
 				this.h - this.locX, 
 				this.i - this.locY, 
 				this.j - this.locZ
 			).a();
 		
 		double directionDegree = this.yaw * Math.PI / 180.0F;
         Vec3D vec3d1 = this.world.getVec3DPool().create(
         		MathHelper.sin((float) directionDegree), 
         		this.motY, 
         		-MathHelper.cos((float) directionDegree)
         	).a();
 	
 		float f4 = (float) (vec3d1.b(vec3d) + 0.5D) / 1.5F;
 
 		if (f4 < 0.0F) {
 			f4 = 0.0F;
 		}
 
 		this.bg *= 0.8F;
 		float f5 = MathHelper.sqrt(this.motX * this.motX + this.motZ
 				* this.motZ) + 1;
 		double d10 = Math.sqrt(this.motX * this.motX + this.motZ * this.motZ) + 1.0D;
 
 		if (d10 > 40.0D) {
 			d10 = 40.0D;
 		}
 
 		this.bg = (float) (this.bg + d9 * (0.7 / d10 / f5));
 		this.yaw += this.bg * 0.1;
 		directionDegree = this.yaw * Math.PI / 180.0F; //recalculation
 		float f6 = (float) (2.0D / (d10 + 1.0D));
 		float f7 = 0.06F;
 
 		this.a(0, -1.0F, f7 * (f4 * f6 + (1.0F - f6)));
 		
 		//From tobiyas stop moving when not needed to
 		if(!doNothingLock){
 			if (this.bA) {
 				this.move(this.motX * 0.8, this.motY * 0.8, this.motZ * 0.8);
 			} else {
 				this.move(this.motX, this.motY, this.motZ);
 			}
 		}
 
 		Vec3D vec3d2 = this.world.getVec3DPool().create(this.motX, this.motY, this.motZ).a();
 		float f8 = (float) (vec3d2.b(vec3d1) + 1.0D) / 2.0F;
 
 		f8 = 0.8F + 0.15F * f8;
 		this.motX *= f8;
 		this.motZ *= f8;
 		this.motY *= 0.91;
 
 		this.aN = this.yaw;
         this.bq.width = this.bq.length = 3.0F;
         this.bs.width = this.bs.length = 2.0F;
         this.bt.width = this.bt.length = 2.0F;
         this.bu.width = this.bu.length = 2.0F;
         this.br.length = 3.0F;
         this.br.width = 5.0F;
         this.bv.length = 2.0F;
         this.bv.width = 4.0F;
         this.bw.length = 3.0F;
         this.bw.width = 4.0F;
         
 		f1 = (float) ((this.b(5, 1.0F)[1] - this.b(10, 1.0F)[1]) * 10.0F / 180.0F
 				* Math.PI);
 		f2 = MathHelper.cos(f1);
 		float f9 = -MathHelper.sin((float) f1);
 		float f10 = (float)directionDegree;
 		float f11 = MathHelper.sin(f10);
 		float f12 = MathHelper.cos(f10);
 
 		this.br.l_();
 		this.br.setPositionRotation(this.locX + (f11 * 0.5F),
 				this.locY, this.locZ - (f12 * 0.5F), 0.0F, 0.0F);
 		this.bv.l_();
 		this.bv.setPositionRotation(this.locX + (f12 * 4.5F),
 				this.locY + 2.0D, this.locZ + (f11 * 4.5F), 0.0F, 0.0F);
 		this.bw.l_();
 		this.bw.setPositionRotation(this.locX - (f12 * 4.5F),
 				this.locY + 2.0D, this.locZ - (f11 * 4.5F), 0.0F, 0.0F);
 
 		if (this.hurtTicks == 0 && attackingMode) {
 			dragonMoveController.knockbackNearbyEntities(this.world.getEntities(this, this.bv.boundingBox.grow(4.0D, 2.0D, 4.0D).d(0.0D, -2.0D, 0)));
 			dragonMoveController.knockbackNearbyEntities(this.world.getEntities(this, this.bw.boundingBox.grow(4.0D, 2.0D, 4.0D).d(0.0D, -2.0D, 0)));
 			dragonHealthController.damageEntities(this.world.getEntities(this, this.bq.boundingBox.grow(1.0D, 1.0D, 1.0D)));
 		}
 
 		// LimitedEnderDragon - begin: Added FireBalls here!
 		fireballController.checkSpitFireBall();
 		// LimitedEnderDragon - end
 
 		double[] adouble = this.b(5, 1.0F);
 		double[] adouble1 = this.b(0, 1.0F);
 
 		f3 = MathHelper.sin((float) (directionDegree - this.bg * 0.01F));
 		float f13 = MathHelper.cos((float)directionDegree
 				- this.bg * 0.01F);
 
 		this.bq.l_();
 		this.bq.setPositionRotation(this.locX + (f3 * 5.5F * f2),
 				this.locY + (adouble1[1] - adouble[1])
 						+ (f9 * 5.5F), this.locZ
 						- (f13 * 5.5F * f2), 0, 0);
 
 		for (int j = 0; j < 3; ++j) {
 			EntityComplexPart entitycomplexpart = null;
 
 			if (j == 0) {
 				entitycomplexpart = this.bs;
 			}
 
 			if (j == 1) {
 				entitycomplexpart = this.bt;
 			}
 
 			if (j == 2) {
 				entitycomplexpart = this.bu;
 			}
 
 			double[] adouble2 = this.b(12 + j * 2, 1F);
 			float f14 = (float) (directionDegree + MathHelper.g(adouble2[0] - adouble[0]) * Math.PI / 180F);
 			float f15 = MathHelper.sin(f14);
 			float f16 = MathHelper.cos(f14);
 			float f17 = 1.5F;
 			float f18 = (j + 1) * 2.0F;
 
 			entitycomplexpart.l_();
 			entitycomplexpart.setPositionRotation(this.locX - ((f11 * f17 + f15 * f18) * f2), 
 					this.locY + (adouble2[1] - adouble[1]) * 1.0D - ((f18 + f17) * f9) + 1.5D, 
 					this.locZ + ((f12 * f17 + f16 * f18) * f2), 0.0F, 0.0F);
 		}
 
 		this.bA = dragonMoveController.checkHitBlocks(this.bq.boundingBox)
 				| dragonMoveController.checkHitBlocks(this.br.boundingBox);
 	}
 
 	public boolean spitFireBallOnTarget(Entity target) {
 		if (target == null)
 			return false;
 
 		fireballController.fireFireball(target);
 		return true;
 	}
 
 	/** 
 	 * Function to drop the EXP if a Dragon is dead
 	 * ORIGINAL: aP()
 	 * Moved to: ItemLootController
 	 * 
 	 * @see net.minecraft.server.v1_6_R2.v1_5_R2.EntityEnderDragon#aO()
 	 */
 	@Override
 	protected void aA() {
 		itemController.deathTick();
 	}
 	
 	
 	@Override
 	public void a(NBTTagCompound compound){
 		super.a(compound);
 		DragonNBTReturn returnContainer = NBTTagDragonStore.loadFromNBT(this, compound);
 		createAllControllers(returnContainer);
 	}
 	
 	/**
 	 * Saving dragon data to NBT Compound
 	 * @see net.minecraft.server.v1_6_R2.v1_5_R2.EntityLiving#b(net.minecraft.server.v1_6_R2.v1_5_R2.NBTTagCompound)
 	 */
 	@Override
 	public void b(NBTTagCompound compound){
 		super.b(compound);
 		NBTTagDragonStore.saveToNBT(this, compound, propertyController.getAllProperties());
 	}
 
 	public void remove() {
 		getBukkitEntity().remove();
 	}
 
 	public String getName() {
 		return "EnderDragon";
 	}
 
 	@Override
 	public int getExpReward() {
 		return plugin.interactConfig().getConfig_dropEXP();
 	}
 
 	public Location getLocation() {
 		return getBukkitEntity().getLocation();
 	}
 
 	public boolean spawn() {
 		return spawnCraftBukkit();
 	}
 
 	private boolean spawnCraftBukkit() {
 		World world = ((CraftWorld) getLocation().getWorld()).getHandle();
 		Chunk chunk = getLocation().getChunk();
 		if(!chunk.isLoaded())
 			this.getLocation().getChunk().load();
 		
 		if (!world.addEntity(this))
 			return false;
 		setPosition(locX, locY, locZ);
 		return true;
 	}	
 
 	public Location getHomeLocation() {
 		return targetController.getHomeLocation();
 	}
 
 	public int getID() {
 		return getBukkitEntity().getEntityId();
 	}
 
 	public boolean isFlyingHome() {
 		return targetController.isFlyingHome();
 	}
 
 	public void setTarget(LivingEntity entity) {
 		EntityLiving convertedEntity = (EntityLiving) ((CraftEntity) entity).getHandle();
 		targetController.forceTarget(convertedEntity);
 	}
 
 	public org.bukkit.entity.Entity getTarget() {
 		Entity entity = targetController.getCurrentTarget();
 		if(entity == null)
 			return null;
 		
 		return entity.getBukkitEntity();
 	}
 
 	public int getLogicCalls() {
 		int calls = logicCall;
 		logicCall = 0;
 		return calls;
 	}
 
 	public void goToLocation(Location location) {
 		targetController.setNewTarget(location, true);
 	}
 
 	public void changeUUID(UUID uID) {
 		this.uniqueID = UUID.fromString(uID.toString());
 	}
 
 	public UUID getUUID() {
 		return this.getBukkitEntity().getUniqueId();
 	}
 	
 	public Location getForceLocation(){
 		return targetController.getForceGoTo();
 	}
 	
 	public void addEnemy(org.bukkit.entity.Entity entity){
 		CraftEntity craftEntity = (CraftEntity) entity;
 		targetController.addTarget((EntityLiving)craftEntity.getHandle());
 	}
 	
 	public boolean isInRange(Location loc, double range){
 		return targetController.isInRange(loc, range);
 	}
 	
 	public Map<String, Float> getPlayerDamageDone(){
 		return dragonHealthController.getPlayerDamage();
 	}
 	
 	public String getLastPlayerAttacked(){
 		return dragonHealthController.getLastPlayerAttacked();
 	}
 
 	public float getDamageByPlayer(String player) {
 		return dragonHealthController.getDamageByPlayer(player);
 	}
 
 	public double getMeeleDamage() {
 		return ageContainer.getDmg();
 	}
 	
 	public List<ItemStack> generateLoot(){
 		return itemController.getItemDrops(ageContainer.getDrops());
 	}
 	
 	public String getAgeName(){
 		return ageContainer.getAgeName();
 	}
 
 	public boolean isHostile() {
 		return ageContainer.isHostile();
 	}
 
 	public void forceFlyHome(boolean flyingHome) {
 		targetController.forceFlyingHome(flyingHome);
 	}
 
 	public void setNewHome(Location newHomeLocation) {
 		targetController.setHomeLocation(newHomeLocation);
 	}
 
 	public void setProperty(String property, Object value) throws OperationNotSupportedException{
 		propertyController.addProperty(property, value);
 	}
 
 	public Object getProperty(String property) {
 		return propertyController.getProperty(property);
 	}
 
 	public AgeContainer getAgeContainer() {
 		return ageContainer;
 	}
 }
