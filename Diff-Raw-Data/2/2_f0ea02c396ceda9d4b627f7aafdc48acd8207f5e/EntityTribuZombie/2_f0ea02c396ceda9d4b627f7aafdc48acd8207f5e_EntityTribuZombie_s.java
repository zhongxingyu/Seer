 /*******************************************************************************
  * Copyright or  or Copr. Quentin Godron (2011)
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
 
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 
 import net.minecraft.server.EntityHuman;
 import net.minecraft.server.EntityVillager;
 import net.minecraft.server.EntityZombie;
 import net.minecraft.server.ItemStack;
 import net.minecraft.server.MathHelper;
 import net.minecraft.server.MonsterType;
 import net.minecraft.server.PathfinderGoalBreakDoor;
 import net.minecraft.server.PathfinderGoalFloat;
 import net.minecraft.server.PathfinderGoalHurtByTarget;
 import net.minecraft.server.PathfinderGoalLookAtPlayer;
 import net.minecraft.server.PathfinderGoalMeleeAttack;
 import net.minecraft.server.PathfinderGoalMoveThroughVillage;
 import net.minecraft.server.PathfinderGoalMoveTowardsRestriction;
 import net.minecraft.server.PathfinderGoalMoveTowardsTarget;
 import net.minecraft.server.PathfinderGoalNearestAttackableTarget;
 import net.minecraft.server.PathfinderGoalRandomLookaround;
 import net.minecraft.server.PathfinderGoalRandomStroll;
 import net.minecraft.server.World;
 import net.minecraft.server.WorldServer;
 
 public class EntityTribuZombie extends EntityZombie {
 	@SuppressWarnings("unused")
 	private Tribu plugin;
 	private boolean fireResistant;
 
 	public EntityTribuZombie(World world) {
 		super(world);
 		this.bukkitEntity = new CraftTribuZombie(world.getServer(), this);
 	}
 
 	private EntityTribuZombie(World world, double d0, double d1, double d2) {
 		this(world);
 		this.setPosition(d0, d1, d2);
 
 	}
 
 	public EntityTribuZombie(Tribu plugin, World world, double d0, double d1, double d2) {
 		this(world, d0, d1, d2);
 		fireResistant = plugin.config().ZombiesFireResistant;
 		this.texture = "/mob/zombie.png";
 		// from 0.85 to 1.18
 		float normalSpeedCoef = ((plugin.config().ZombiesSpeedRandom) ? .1f + (this.an().nextFloat() / 3f) : .25f)
 				+ (plugin.config().ZombiesSpeedBase - .25f);
 		// from 1 to 1.77
 		// .85 * 1.18 = 1 and we'll have normalSpeed * rushSpeed * speed, if
 		// normalSpeed=.85 * rushSpeed=1 = 1
		float rushSpeedCoef = (1.18f * (((plugin.config().ZombiesSpeedRandom) ? (this.an().nextFloat() / 2f) : .25f) + (plugin.config().ZombiesSpeedRush - .25f)));
 		// Speed: 0.23 normal speed
 		this.bb = 0.23F * normalSpeedCoef;
 		this.damage = plugin.getWaveStarter().getCurrentDamage();
 		// Can break wooden door ?
 		this.al().b(true);
 		this.goalSelector.a(0, new PathfinderGoalFloat(this));
 		this.goalSelector.a(1, new PathfinderGoalMeleeAttack(this, EntityHuman.class, this.bb * rushSpeedCoef, false));
 		this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this, EntityVillager.class, this.bb * rushSpeedCoef, true));
 		this.goalSelector.a(3, new PathfinderGoalBreakDoor(this));
 		this.goalSelector.a(4, new PathfinderGoalMoveTowardsTarget(this, this.bb * rushSpeedCoef, Float.MAX_VALUE));
 		FocusType focus = plugin.config().ZombiesFocus;
 
 		if (focus.equals(FocusType.None))
 			this.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(this, this.bb));
 		else if (focus.equals(FocusType.NearestPlayer) || focus.equals(FocusType.RandomPlayer)) {
 
 			this.goalSelector.a(5, new PathfinderGoalTrackPlayer(this, plugin, focus.equals(FocusType.RandomPlayer), this.bb, true));
 		} else if (focus.equals(FocusType.InitialSpawn) || focus.equals(FocusType.DeathSpawn)) {
 			this.goalSelector.a(5, new PathfinderGoalMoveToLocation(this, focus.equals(FocusType.DeathSpawn) ? plugin.getLevel().getDeathSpawn()
 					: plugin.getLevel().getInitialSpawn(), this.bb, true));
 		}
 
 		this.goalSelector.a(6, new PathfinderGoalMoveThroughVillage(this, this.bb, false));
 		this.goalSelector.a(7, new PathfinderGoalRandomStroll(this, this.bb));
 		this.goalSelector.a(8, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
 		this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
 		this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false));
 		this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class,
 				(float) (focus.equals(FocusType.NearestPlayer) ? plugin.config().LevelClearZone : 16.0F), 0, true));
 		this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityVillager.class, 16.0F, 0, false));
 		this.plugin = plugin;
 	}
 
 	public int getMaxHealth() {
 		return 20;
 	}
 
 	public int T() {
 		return 2;
 	}
 
 	protected boolean c_() {
 		return true;
 	}
 
 	public void e() {
 		if (this.world.e() && !this.world.isStatic && !fireResistant) {
 			float f = this.b(1.0F);
 
 			if (f > 0.5F && this.world.isChunkLoaded(MathHelper.floor(this.locX), MathHelper.floor(this.locY), MathHelper.floor(this.locZ))
 					&& this.random.nextFloat() * 30.0F < (f - 0.4F) * 2.0F) {
 
 				this.setOnFire(8);
 
 			}
 		}
 
 		super.e();
 	}
 
 	protected String i() {
 		return "mob.zombie";
 	}
 
 	protected String j() {
 		return "mob.zombiehurt";
 	}
 
 	protected String k() {
 		return "mob.zombiedeath";
 	}
 
 	protected int getLootId() {
 		return -1;
 	}
 
 	public MonsterType getMonsterType() {
 		return MonsterType.UNDEAD;
 	}
 
 	// CraftBukkit start - return rare dropped item instead of dropping it
 	protected ItemStack b(int i) {
 		return null;
 	}
 
 	// CraftBukkit end
 
 	public static EntityTribuZombie spawn(Tribu plugin, WorldServer world, double x, double y, double z) throws CannotSpawnException {
 		EntityTribuZombie tz = new EntityTribuZombie(plugin, world, x, y, z);
 		synchronized (tz) {
 			if (world.addEntity(tz, SpawnReason.CUSTOM))
 				return tz;
 			else
 				throw new CannotSpawnException();
 		}
 
 	}
 }
