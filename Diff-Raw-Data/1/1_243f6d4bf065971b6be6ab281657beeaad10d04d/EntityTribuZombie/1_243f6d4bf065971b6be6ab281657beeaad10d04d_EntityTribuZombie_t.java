 /*******************************************************************************
  * Copyright or � or Copr. Quentin Godron (2011)
  * 
  * cafe.en.grain@gmail.com
  * 
  * This software is a computer program whose purpose is to create zombie 
  * survival games on Bukkit's server. 
  * 
  * This software is governed by the CeCILL-C license under French law and
  * abiding by the rules of distribution of free software.  You can  use, 
  * modify and/ or redistribute the software under the terms of the CeCILL-C
  * license as circulated by CEA, CNRS and INRIA at the following URL
  * "http://www.cecill.info". 
  * 
  * As a counterpart to the access to the source code and  rights to copy,
  * modify and redistribute granted by the license, users are provided only
  * with a limited warranty  and the software's author,  the holder of the
  * economic rights,  and the successive licensors  have only  limited
  * liability. 
  * 
  * In this respect, the user's attention is drawn to the risks associated
  * with loading,  using,  modifying and/or developing or reproducing the
  * software by the user in light of its specific status of free software,
  * that may mean  that it is complicated to manipulate,  and  that  also
  * therefore means  that it is reserved for developers  and  experienced
  * professionals having in-depth computer knowledge. Users are therefore
  * encouraged to load and test the software's suitability as regards their
  * requirements in conditions enabling the security of their systems and/or 
  * data to be ensured and,  more generally, to use and operate it in the 
  * same conditions as regards security. 
  * 
  * The fact that you are presently reading this means that you have had
  * knowledge of the CeCILL-C license and that you accept its terms.
  ******************************************************************************/
 
 /*
  * Thanks to xXKeyleXx (plugin MyWolf) for the inspiration
  */
 
 package graindcafe.tribu.TribuZombie;
 
 import graindcafe.tribu.Tribu;
 import graindcafe.tribu.Configuration.FocusType;
 
 import java.util.Calendar;
 import java.util.UUID;
 
 import net.minecraft.server.v1_6_R2.AttributeInstance;
 import net.minecraft.server.v1_6_R2.AttributeModifier;
 import net.minecraft.server.v1_6_R2.AttributeRanged;
 import net.minecraft.server.v1_6_R2.Block;
 import net.minecraft.server.v1_6_R2.DamageSource;
 import net.minecraft.server.v1_6_R2.Entity;
 import net.minecraft.server.v1_6_R2.EntityHuman;
 import net.minecraft.server.v1_6_R2.EntityLiving;
 import net.minecraft.server.v1_6_R2.EntityMonster;
 import net.minecraft.server.v1_6_R2.EntityVillager;
 import net.minecraft.server.v1_6_R2.EntityZombie;
 import net.minecraft.server.v1_6_R2.EnumMonsterType;
 import net.minecraft.server.v1_6_R2.GenericAttributes;
 import net.minecraft.server.v1_6_R2.GroupDataEntity;
 import net.minecraft.server.v1_6_R2.IAttribute;
 import net.minecraft.server.v1_6_R2.Item;
 import net.minecraft.server.v1_6_R2.ItemStack;
 import net.minecraft.server.v1_6_R2.MathHelper;
 import net.minecraft.server.v1_6_R2.MobEffect;
 import net.minecraft.server.v1_6_R2.MobEffectList;
 import net.minecraft.server.v1_6_R2.NBTTagCompound;
 import net.minecraft.server.v1_6_R2.PathfinderGoalBreakDoor;
 import net.minecraft.server.v1_6_R2.PathfinderGoalFloat;
 import net.minecraft.server.v1_6_R2.PathfinderGoalHurtByTarget;
 import net.minecraft.server.v1_6_R2.PathfinderGoalLookAtPlayer;
 import net.minecraft.server.v1_6_R2.PathfinderGoalMeleeAttack;
 import net.minecraft.server.v1_6_R2.PathfinderGoalMoveTowardsRestriction;
 import net.minecraft.server.v1_6_R2.PathfinderGoalNearestAttackableTarget;
 import net.minecraft.server.v1_6_R2.PathfinderGoalRandomLookaround;
 import net.minecraft.server.v1_6_R2.PathfinderGoalRandomStroll;
 import net.minecraft.server.v1_6_R2.World;
 import net.minecraft.server.v1_6_R2.WorldServer;
 
 import org.bukkit.event.entity.CreatureSpawnEvent;
 
 public class EntityTribuZombie extends EntityMonster {
 
 	protected static final IAttribute bp = (new AttributeRanged(
 			"zombie.spawnReinforcements", 0.0D, 0.0D, 1.0D))
 			.a("Spawn Reinforcements Chance");
 	private static final UUID bq = UUID.randomUUID();
 	private static final AttributeModifier br = new AttributeModifier(bq,
 			"Baby speed boost", 0.5D, 1);
 
 	public static EntityTribuZombie spawn(final Tribu plugin,
 			final WorldServer world, final double x, final double y,
 			final double z) throws CannotSpawnException {
 		final EntityTribuZombie tz = new EntityTribuZombie(plugin, world, x,
 				y + 0.1, z);
 		synchronized (tz) {
 			if (world.addEntity(tz, CreatureSpawnEvent.SpawnReason.CUSTOM))
 				return tz;
 			else
 				throw new CannotSpawnException();
 		}
 
 	}
 
 	private int bs;
 
 	@SuppressWarnings("unused")
 	private Tribu plugin;
 	private int maxHealth = 20;
 	private boolean sunProof = true;
 	protected int d = 0;
 	private int damage = 3;
 	final double baseSpeed = 0.23000000417232513D;
 	// private double normalSpeed = 0.23000000417232513D;
 	private double normalSpeedCoef = 1;
 	// private final double rushSpeed = 0.23000000417232513D;
 	private final double followRange = 40d;
 	private double rushSpeedCoef = 1;
 	private static final IAttribute attrMaxHealth = GenericAttributes.a;
 	private static final IAttribute attrFollowRange = GenericAttributes.b;
 	private static final IAttribute attrKnockbackResistance = GenericAttributes.c;
 	private static final IAttribute attrSpeed = GenericAttributes.d;
 	private static final IAttribute attrAttackDamage = GenericAttributes.e;
 
 	public EntityTribuZombie(final Tribu plugin, final World world,
 			final double x, final double y, final double z) {
 		this(world, x, y, z);
 		// fireProof = plugin.config().ZombiesFireProof;
 		sunProof = plugin.config().ZombiesFireProof
 				|| plugin.config().ZombiesSunProof;
 		// from 0.85 to 1.18
 		normalSpeedCoef = ((plugin.config().ZombiesSpeedRandom) ? .1d + (random
 				.nextDouble() / 3d) : .25d)
 				+ (plugin.config().ZombiesSpeedBase * .75d);
 		rushSpeedCoef = (((plugin.config().ZombiesSpeedRandom) ? (random
 				.nextDouble() / 2d) : .25d) + (plugin.config().ZombiesSpeedRush - .25d));
 		damage = plugin.getWaveStarter().getCurrentDamage();
 		maxHealth = plugin.getWaveStarter().getCurrentHealth();
 		this.plugin = plugin;
 
 		// Can break wooden door ?
 		getNavigation().b(true);
 		goalSelector.a(0, new PathfinderGoalFloat(this));
 		goalSelector.a(1, new PathfinderGoalBreakDoor(this));
 		goalSelector.a(2, new PathfinderGoalMeleeAttack(this,
 				EntityHuman.class, rushSpeedCoef, false));
 		goalSelector.a(3, new PathfinderGoalMeleeAttack(this,
 				EntityVillager.class, rushSpeedCoef, true));
 		goalSelector.a(4, new PathfinderGoalMoveTowardsRestriction(this, 1.0D));
 		final FocusType focus = plugin.config().ZombiesFocus;
 		if (focus.equals(FocusType.None)) {
 
 			goalSelector.a(5, new PathfinderGoalRandomStroll(this, 1d));
 		} else if (focus.equals(FocusType.NearestPlayer)
 				|| focus.equals(FocusType.RandomPlayer)) {
 			goalSelector.a(
 					5,
 					new PathfinderGoalTrackPlayer(plugin, focus
 							.equals(FocusType.RandomPlayer), this, 1d, 20));
 		} else if (focus.equals(FocusType.InitialSpawn)
 				|| focus.equals(FocusType.DeathSpawn))
 			goalSelector.a(
 					5,
 					new PathfinderGoalMoveTo(this, focus
 							.equals(FocusType.InitialSpawn) ? plugin.getLevel()
 							.getInitialSpawn() : plugin.getLevel()
 							.getDeathSpawn(), 1f, 4f));
 		this.goalSelector.a(6, new PathfinderGoalRandomStroll(this, 1.0D));
 		this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this,
 				EntityHuman.class, 8.0F));
 		this.goalSelector.a(7, new PathfinderGoalRandomLookaround(this));
 
 		targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));
 		targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this,
 				EntityHuman.class, 0, true));
 		targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this,
 				EntityVillager.class, 0, false));
 		getAttributeInstance(attrMaxHealth).setValue(maxHealth);
 		final double speed = baseSpeed * normalSpeedCoef;
 		getAttributeInstance(attrSpeed).setValue(speed);
		getAttributeInstance(attrAttackDamage).setValue(damage);
 	}
 
 	public EntityTribuZombie(final World world) {
 		super(world);
 
 		bukkitEntity = new CraftTribuZombie(world.getServer(), this);
 
 	}
 
 	private EntityTribuZombie(final World world, final double x,
 			final double y, final double z) {
 		this(world);
 		setPosition(x, y, z);
 	}
 
 	@Override
 	protected void a() {
 		super.a();
 		getDataWatcher().a(12, Byte.valueOf((byte) 0));
 		getDataWatcher().a(13, Byte.valueOf((byte) 0));
 		getDataWatcher().a(14, Byte.valueOf((byte) 0));
 	}
 
 	@Override
 	public boolean a(final EntityHuman entityhuman) {
 		final ItemStack itemstack = entityhuman.bx();
 
 		if (itemstack != null && itemstack.getItem() == Item.GOLDEN_APPLE
 				&& itemstack.getData() == 0 && isVillager()
 				&& this.hasEffect(MobEffectList.WEAKNESS)) {
 			if (!entityhuman.abilities.canInstantlyBuild)
 				--itemstack.count;
 
 			if (itemstack.count <= 0)
 				entityhuman.inventory
 						.setItem(entityhuman.inventory.itemInHandIndex,
 								(ItemStack) null);
 
 			if (!world.isStatic)
 				this.a(random.nextInt(2401) + 3600);
 
 			return true;
 		} else
 			return false;
 	}
 
 	@Override
 	public void a(final EntityLiving entityliving) {
 		super.a(entityliving);
 		if (world.difficulty >= 2 && entityliving instanceof EntityVillager) {
 			if (world.difficulty == 2 && random.nextBoolean())
 				return;
 
 			final EntityZombie entityzombie = new EntityZombie(world);
 
 			entityzombie.j(entityliving);
 			world.kill(entityliving);
 			entityzombie.a((GroupDataEntity) null);
 			entityzombie.setVillager(true);
 			if (entityliving.isBaby())
 				entityzombie.setBaby(true);
 
 			world.addEntity(entityzombie);
 			world.a((EntityHuman) null, 1016, (int) locX, (int) locY,
 					(int) locZ, 0);
 		}
 	}
 
 	@Override
 	public GroupDataEntity a(final GroupDataEntity groupdataentity) {
 		Object object = super.a(groupdataentity);
 		final float f = world.b(locX, locY, locZ);
 
 		this.h(random.nextFloat() < 0.55F * f);
 		if (object == null)
 			object = new GroupDataTribuZombie(this,
 					world.random.nextFloat() < 0.05F,
 					world.random.nextFloat() < 0.05F, (Object) null);
 
 		if (object instanceof GroupDataTribuZombie) {
 			final GroupDataTribuZombie groupdatazombie = (GroupDataTribuZombie) object;
 
 			if (groupdatazombie.b)
 				setVillager(true);
 
 			if (groupdatazombie.a)
 				setBaby(true);
 		}
 
 		bw();
 		bx();
 		if (this.getEquipment(4) == null) {
 			final Calendar calendar = world.W();
 
 			if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31
 					&& random.nextFloat() < 0.25F) {
 				setEquipment(4, new ItemStack(
 						random.nextFloat() < 0.1F ? Block.JACK_O_LANTERN
 								: Block.PUMPKIN));
 				dropChances[4] = 0.0F;
 			}
 		}
 
 		getAttributeInstance(attrKnockbackResistance).a(
 				new AttributeModifier("Random spawn bonus",
 						random.nextDouble() * 0.05000000074505806D, 0));
 		getAttributeInstance(attrFollowRange).a(
 				new AttributeModifier("Random zombie-spawn bonus", random
 						.nextDouble() * 1.5D, 2));
 		if (random.nextFloat() < f * 0.05F) {
 			getAttributeInstance(bp).a(
 					new AttributeModifier("Leader zombie bonus", random
 							.nextDouble() * 0.25D + 0.5D, 0));
 			getAttributeInstance(attrMaxHealth).a(
 					new AttributeModifier("Leader zombie bonus", random
 							.nextDouble() * 3.0D + 1.0D, 2));
 		}
 
 		return (GroupDataEntity) object;
 	}
 
 	protected void a(final int i) {
 		bs = i;
 		getDataWatcher().watch(14, Byte.valueOf((byte) 1));
 		this.k(MobEffectList.WEAKNESS.id);
 		addEffect(new MobEffect(MobEffectList.INCREASE_DAMAGE.id, i, Math.min(
 				world.difficulty - 1, 0)));
 		world.broadcastEntityEffect(this, (byte) 16);
 	}
 
 	@Override
 	protected void a(final int i, final int j, final int k, final int l) {
 		makeSound("mob.zombie.step", 0.15F, 1.0F);
 	}
 
 	@Override
 	public void a(final NBTTagCompound nbttagcompound) {
 		super.a(nbttagcompound);
 		if (nbttagcompound.getBoolean("IsBaby"))
 			setBaby(true);
 
 		if (nbttagcompound.getBoolean("IsVillager"))
 			setVillager(true);
 
 		if (nbttagcompound.hasKey("ConversionTime")
 				&& nbttagcompound.getInt("ConversionTime") > -1)
 			this.a(nbttagcompound.getInt("ConversionTime"));
 	}
 
 	@Override
 	protected String aN() {
 		return "mob.zombie.hurt";
 	}
 
 	@Override
 	protected String aO() {
 		return "mob.zombie.death";
 	}
 
 	@Override
 	public int aP() {
 		int i = super.aP() + 2;
 
 		if (i > 20)
 			i = 20;
 
 		return i;
 	}
 
 	@Override
 	protected void ay() {
 		super.ay();
 		// final double speed = baseSpeed * normalSpeedCoef;
 		getAttributeInstance(attrFollowRange).setValue(followRange);
 		getAttributeInstance(attrSpeed).setValue(baseSpeed);
 		getAttributeInstance(attrAttackDamage).setValue(damage);
 		aW().b(bp).setValue(random.nextDouble() * 0.10000000149011612D);
 	}
 
 	@Override
 	public void b(final NBTTagCompound nbttagcompound) {
 		super.b(nbttagcompound);
 		if (isBaby())
 			nbttagcompound.setBoolean("IsBaby", true);
 
 		if (isVillager())
 			nbttagcompound.setBoolean("IsVillager", true);
 
 		nbttagcompound.setInt("ConversionTime", bV() ? bs : -1);
 	}
 
 	@Override
 	protected boolean be() {
 		return true;
 	}
 
 	public boolean bV() {
 		return getDataWatcher().getByte(14) == 1;
 	}
 
 	@Override
 	protected void bw() {
 		super.bw();
 		if (random.nextFloat() < (world.difficulty == 3 ? 0.05F : 0.01F)) {
 			final int i = random.nextInt(3);
 
 			if (i == 0)
 				setEquipment(0, new ItemStack(Item.IRON_SWORD));
 			else
 				setEquipment(0, new ItemStack(Item.IRON_SPADE));
 		}
 	}
 
 	protected void bW() {
 		final EntityVillager entityvillager = new EntityVillager(world);
 
 		entityvillager.j(this);
 		entityvillager.a((GroupDataEntity) null);
 		entityvillager.bX();
 		if (isBaby())
 			entityvillager.setAge(-24000);
 
 		world.kill(this);
 		world.addEntity(entityvillager);
 		entityvillager.addEffect(new MobEffect(MobEffectList.CONFUSION.id, 200,
 				0));
 		world.a((EntityHuman) null, 1017, (int) locX, (int) locY, (int) locZ, 0);
 	}
 
 	protected int bX() {
 		int i = 1;
 
 		if (random.nextFloat() < 0.01F) {
 			int j = 0;
 
 			for (int k = (int) locX - 4; k < (int) locX + 4 && j < 14; ++k)
 				for (int l = (int) locY - 4; l < (int) locY + 4 && j < 14; ++l)
 					for (int i1 = (int) locZ - 4; i1 < (int) locZ + 4 && j < 14; ++i1) {
 						final int j1 = world.getTypeId(k, l, i1);
 
 						if (j1 == Block.IRON_FENCE.id || j1 == Block.BED.id) {
 							if (random.nextFloat() < 0.3F)
 								++i;
 
 							++j;
 						}
 					}
 		}
 
 		return i;
 	}
 
 	@Override
 	public void c() {
 		if (!sunProof && world.v() && !world.isStatic && !isBaby()) {
 			final float f = this.d(1.0F);
 
 			if (f > 0.5F
 					&& random.nextFloat() * 30.0F < (f - 0.4F) * 2.0F
 					&& world.l(MathHelper.floor(locX), MathHelper.floor(locY),
 							MathHelper.floor(locZ))) {
 				boolean flag = true;
 				final ItemStack itemstack = this.getEquipment(4);
 
 				if (itemstack != null) {
 					if (itemstack.g()) {
 						itemstack.setData(itemstack.j() + random.nextInt(2));
 						if (itemstack.j() >= itemstack.l()) {
 							this.a(itemstack);
 							setEquipment(4, (ItemStack) null);
 						}
 					}
 
 					flag = false;
 				}
 
 				if (flag)
 					setOnFire(8);
 			}
 		}
 
 		super.c();
 	}
 
 	@Override
 	public boolean damageEntity(final DamageSource damagesource, final float f) {
 		if (!super.damageEntity(damagesource, f))
 			return false;
 		else {
 			EntityLiving entityliving = getGoalTarget();
 
 			if (entityliving == null && bN() instanceof EntityLiving)
 				entityliving = (EntityLiving) bN();
 
 			if (entityliving == null
 					&& damagesource.getEntity() instanceof EntityLiving)
 				entityliving = (EntityLiving) damagesource.getEntity();
 
 			if (entityliving != null && world.difficulty >= 3
 					&& random.nextFloat() < getAttributeInstance(bp).getValue()) {
 				final int i = MathHelper.floor(locX);
 				final int j = MathHelper.floor(locY);
 				final int k = MathHelper.floor(locZ);
 				final EntityZombie entityzombie = new EntityZombie(world);
 
 				for (int l = 0; l < 50; ++l) {
 					final int i1 = i + MathHelper.nextInt(random, 7, 40)
 							* MathHelper.nextInt(random, -1, 1);
 					final int j1 = j + MathHelper.nextInt(random, 7, 40)
 							* MathHelper.nextInt(random, -1, 1);
 					final int k1 = k + MathHelper.nextInt(random, 7, 40)
 							* MathHelper.nextInt(random, -1, 1);
 
 					if (world.w(i1, j1 - 1, k1)
 							&& world.getLightLevel(i1, j1, k1) < 10) {
 						entityzombie.setPosition(i1, j1, k1);
 						if (world.b(entityzombie.boundingBox)
 								&& world.getCubes(entityzombie,
 										entityzombie.boundingBox).isEmpty()
 								&& !world
 										.containsLiquid(entityzombie.boundingBox)) {
 							world.addEntity(entityzombie);
 							entityzombie.setGoalTarget(entityliving);
 							entityzombie.a((GroupDataEntity) null);
 							getAttributeInstance(bp)
 									.a(new AttributeModifier(
 											"Zombie reinforcement caller charge",
 											-0.05000000074505806D, 0));
 							entityzombie
 									.getAttributeInstance(bp)
 									.a(new AttributeModifier(
 											"Zombie reinforcement callee charge",
 											-0.05000000074505806D, 0));
 							break;
 						}
 					}
 				}
 			}
 
 			return true;
 		}
 	}
 
 	@Override
 	protected int getLootId() {
 		return Item.ROTTEN_FLESH.id;
 	}
 
 	@Override
 	public EnumMonsterType getMonsterType() {
 		return EnumMonsterType.UNDEAD;
 	}
 
 	@Override
 	public boolean isBaby() {
 		return getDataWatcher().getByte(12) == 1;
 	}
 
 	@Override
 	protected boolean isTypeNotPersistent() {
 		return !bV();
 	}
 
 	public boolean isVillager() {
 		return getDataWatcher().getByte(13) == 1;
 	}
 
 	@Override
 	protected ItemStack l(final int i) {
 		switch (random.nextInt(3)) {
 		case 0:
 			this.b(Item.IRON_INGOT.id, 1);
 			break;
 
 		case 1:
 			this.b(Item.CARROT.id, 1);
 			break;
 
 		case 2:
 			this.b(Item.POTATO.id, 1);
 		}
 		return null;
 	}
 
 	@Override
 	public void l_() {
 		if (!world.isStatic && bV()) {
 			final int i = bX();
 
 			bs -= i;
 			if (bs <= 0)
 				bW();
 		}
 
 		super.l_();
 	}
 
 	@Override
 	public boolean m(final Entity entity) {
 		final boolean flag = super.m(entity);
 
 		if (flag && aY() == null && isBurning()
 				&& random.nextFloat() < world.difficulty * 0.3F)
 			entity.setOnFire(2 * world.difficulty);
 
 		return flag;
 	}
 
 	@Override
 	protected String r() {
 		return "mob.zombie.say";
 	}
 
 	public void setBaby(final boolean flag) {
 		getDataWatcher().watch(12, Byte.valueOf((byte) (flag ? 1 : 0)));
 		if (world != null && !world.isStatic) {
 			final AttributeInstance attributeinstance = getAttributeInstance(attrSpeed);
 
 			attributeinstance.b(br);
 			if (flag)
 				attributeinstance.a(br);
 		}
 	}
 
 	public void setVillager(final boolean flag) {
 		getDataWatcher().watch(13, Byte.valueOf((byte) (flag ? 1 : 0)));
 	}
 
 	public double getSpeed() {
 		return baseSpeed * normalSpeedCoef;
 	}
 
 	public double getRushSpeed() {
 		return baseSpeed * normalSpeedCoef * rushSpeedCoef;
 	}
 }
