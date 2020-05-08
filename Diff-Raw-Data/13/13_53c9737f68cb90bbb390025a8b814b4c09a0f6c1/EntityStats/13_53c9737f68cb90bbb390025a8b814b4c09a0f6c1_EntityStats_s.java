 package hunternif.mc.dota2items.core;
 
 import hunternif.mc.dota2items.core.buff.Buff;
 import hunternif.mc.dota2items.core.buff.BuffInstance;
 import hunternif.mc.dota2items.entity.IInvulnerableEntity;
 import hunternif.mc.dota2items.entity.IMagicImmuneEntity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 import net.minecraftforge.common.IExtendedEntityProperties;
 
 public class EntityStats implements IExtendedEntityProperties {
 	private static final float MINECRAFT_PLAYER_MOVE_SPEED = 0.1f;
 	public static final int MAX_MOVE_SPEED = 522;
 	public static final int MAX_HP_PER_STR = 19;
 	public static final float HP_REGEN_PER_STR = 0.03f;
 	public static final int MAX_MANA_PER_INT = 13;
 	public static final float MANA_REGEN_PER_INT = 0.04f;
 	public static final float ARMOR_PER_AGI = 1f/7f;
 	public static final int MIN_ASPD = -80;
 	public static final int MAX_ASPD = 400;
 	
 	public static final int BASE_PLAYER_HP = 150;
 	public static final int BASE_PLAYER_STR = 0;
 	public static final int BASE_PLAYER_AGI = 0;
 	public static final int BASE_PLAYER_INT = 0;
 	public static final int BASE_PLAYER_TOTAL_HP = BASE_PLAYER_HP + BASE_PLAYER_STR*MAX_HP_PER_STR;
 	
 	private static final String TAG_MANA = "D2IMana";
 	private static final String TAG_PARTIAL_HP = "D2IPartialHp";
 	private static final String TAG_GOLD = "D2IGgold";
 	private static final String TAG_BUFFS = "D2IBuffs";
 	
 	
 	public int entityId;
 	
 	public int baseHealth = BASE_PLAYER_HP;
 	public float baseHealthRegen = 0.25f;
 	public int baseMana = 0;
 	public float baseManaRegen = 0.01f;
 	public int baseMovementSpeed = 300;
 	public float baseAttackTime = 1.7f;
 	public int baseArmor = 0;
 	public int baseDamage = 0;
 	
 	public boolean baseInvulnerable = false;
 	public boolean baseMagicImmune = false;
 	
 	public int baseStrength = BASE_PLAYER_STR;
 	public int baseAgility = BASE_PLAYER_AGI;
 	public int baseIntelligence = BASE_PLAYER_INT;
 	
 	// Transient stats:
 	// Health is deferred to Minecraft.
 	private float curMana = 0;
 	private float curGold = 0;
 	/** Used to restrict attack interval by AttackTime. */
 	public long lastAttackTime = 0;
 	
 	/** When applying damage or heal directly to the entity, the amount of health
 	 * will be truncated due to entity's health being a fixed small integer
 	 * (20 half-hearts for the player). In order to apply the damage and heal
 	 * amount properly according to Dota mechanics, this partial health will be
 	 * "carried over" and applied, when sufficient, at the next heal/damage
 	 * instance. */
 	public float partialHalfHeart = 0;
 	
 	private List<BuffInstance> appliedBuffs = new ArrayList<BuffInstance>();
 	
 	
 	public EntityStats(EntityLiving entity) {
 		entityId = entity.entityId;
 		baseHealth = MathHelper.floor_float((float)entity.getMaxHealth() * (float)BASE_PLAYER_HP / 20f);
 		if (entity instanceof EntityPlayer) {
 			baseHealthRegen = 0.25f;
 			baseAttackTime = 1.7f;
 			baseMovementSpeed = 300;
 		} else {
 			baseHealthRegen = 0.5f;
 			baseAttackTime = 1.0f;
 			baseMovementSpeed = 325;
 		}
 		if (entity instanceof IMagicImmuneEntity) {
 			baseMagicImmune = true;
 		}
 		if (entity instanceof IInvulnerableEntity) {
 			baseInvulnerable = true;
 		}
 	}
 	
 	public List<BuffInstance> getAppliedBuffs() {
 		return new ArrayList<BuffInstance>(appliedBuffs);
 	}
 	public void addBuff(BuffInstance buffInst) {
 		if (!buffInst.buff.stacks) {
 			for (BuffInstance curBuffInst : getAppliedBuffs()) {
 				if (curBuffInst.buff.id == buffInst.buff.id) {
 					return;
 				}
 			}
 		}
 		appliedBuffs.add(buffInst);
 	}
 	public void removeBuff(BuffInstance buffInst) {
 		appliedBuffs.remove(buffInst);
 	}
 	
 	public int getMaxHealth() {
 		int health = baseHealth;
 		for (BuffInstance buffInst : getAppliedBuffs()) {
 			health += buffInst.buff.health;
 		}
 		return health + MAX_HP_PER_STR*getStrength();
 	}
 	public int getHealth(EntityLiving entity) {
 		float mcHPProportion = (float)entity.getHealth() / (float)entity.getMaxHealth();
 		float halfHeartEquivalent = (float)getMaxHealth() / (float)entity.getMaxHealth();
 		return MathHelper.floor_float((float)getMaxHealth()*mcHPProportion + halfHeartEquivalent*partialHalfHeart);
 	}
 	public float getHealthRegen() {
 		float regen = baseHealthRegen;
 		for (BuffInstance buffInst : getAppliedBuffs()) {
 			regen += buffInst.buff.healthRegen;
 		}
 		return regen + HP_REGEN_PER_STR*(float)getStrength();
 	}
 	
 	public int getMaxMana() {
 		int mana = baseMana;
 		for (BuffInstance buffInst : getAppliedBuffs()) {
 			mana += buffInst.buff.mana;
 		}
 		return mana + MAX_MANA_PER_INT*getIntelligence();
 	}
 	public int getMana() {
 		int maxMana = getMaxMana();
 		if (curMana > maxMana) {
 			curMana = maxMana;
 		}
 		return MathHelper.floor_float(curMana);
 	}
 	public float getFloatMana() {
 		int maxMana = getMaxMana();
 		if (curMana > maxMana) {
 			curMana = maxMana;
 		}
 		return curMana;
 	}
 	public void setMana(float amount) {
 		float newMana = amount;
 		float maxMana = getMaxMana();
 		if (newMana < 0) newMana = 0;
 		if (newMana > maxMana) newMana = maxMana;
 		curMana = newMana;
 	}
 	public void addMana(float amount) {
 		setMana(curMana + amount);
 	}
 	public void removeMana(float amount) {
 		setMana(curMana - amount);
 	}
 	
 	public float getManaRegen() {
 		float regen = baseManaRegen;
 		for (BuffInstance buffInst : getAppliedBuffs()) {
 			regen += buffInst.buff.manaRegen;
 		}
 		return regen + MANA_REGEN_PER_INT*(float)getIntelligence();
 	}
 	
 	/** A percentage. */
 	public int getIncreasedAttackSpeed() {
 		int aspd = 0;
 		for (BuffInstance buffInst : getAppliedBuffs()) {
 			aspd += buffInst.buff.attackSpeed;
 		}
 		aspd += getAgility();
 		if (aspd < MIN_ASPD) aspd = MIN_ASPD;
 		if (aspd > MAX_ASPD) aspd = MAX_ASPD;
 		return aspd;
 	}
 	
 	public float getAttackTime() {
 		float aspd = ((float)getIncreasedAttackSpeed())/100f;
 		float attackTime = baseAttackTime / (1f + aspd);
 		//FMLLog.log(Dota2Items.ID, Level.FINE, "Attack time = %.2f", attackTime);
 		return attackTime;
 	}
 	
 	public float getDamage(float weaponDamage, boolean melee) {
 		float damage = weaponDamage + this.baseDamage;
 		for (BuffInstance buffInst : getAppliedBuffs()) {
 			damage += buffInst.buff.damage;
 		}
 		for (BuffInstance buffInst : getAppliedBuffs()) {
 			damage += (float)damage * ((float)(melee ? buffInst.buff.damagePercentMelee : buffInst.buff.damagePercentRanged)) / 100f;
 		}
 		//FMLLog.log(Dota2Items.ID, Level.FINE, "Buffed damage from %.2f to %.2f", weaponDamage, damage);
 		return damage;
 	}
 	
 	public int getArmor(int basicArmor) {
 		int armor = basicArmor + this.baseArmor;
 		for (BuffInstance buffInst : getAppliedBuffs()) {
 			armor += buffInst.buff.armor;
 		}
 		armor += (float)getAgility() * ARMOR_PER_AGI;
 		//FMLLog.log(Dota2Items.ID, Level.FINE, "Buffed armor from %d to %d", basicArmor, armor);
 		return armor;
 	}
 	
 	/** @return movement speed calculated for Minecraft. */
 	public float getMovementSpeed() {
 		float movementSpeed = baseMovementSpeed;
 		List<Buff> appliedMSBuffs = new ArrayList<Buff>();
 		for (BuffInstance buffInst : getAppliedBuffs()) {
 			if (!appliedMSBuffs.contains(buffInst.buff)) {
 				movementSpeed += buffInst.buff.movementSpeed;
 				// Movement speed from the same type of Buff doesn't stack
 				appliedMSBuffs.add(buffInst.buff);
 			}
 		}
 		if (movementSpeed > MAX_MOVE_SPEED) {
 			movementSpeed = MAX_MOVE_SPEED;
 		}
 		return movementSpeed * MINECRAFT_PLAYER_MOVE_SPEED / (float) baseMovementSpeed;
 	}
 	
 	public boolean canAttack() {
 		for (BuffInstance buffInst : getAppliedBuffs()) {
 			if (buffInst.buff.disableAttack) {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public boolean canMove() {
 		for (BuffInstance buffInst : getAppliedBuffs()) {
 			if (buffInst.buff.disableMove) {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public boolean canUseItems() {
 		for (BuffInstance buffInst : getAppliedBuffs()) {
 			if (buffInst.buff.disableItems) {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public boolean isMagicImmune() {
 		if (baseMagicImmune) {
 			return true;
 		}
 		for (BuffInstance buffInst : getAppliedBuffs()) {
 			if (buffInst.buff.magicImmune) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean isInvulnerable() {
 		if (baseInvulnerable) {
 			return true;
 		}
 		for (BuffInstance buffInst : getAppliedBuffs()) {
 			if (buffInst.buff.invulnerable) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	//NOTE I may need to implement the Reliable and Unreliable gold someday.
 	public int getGold() {
 		return MathHelper.floor_float(curGold);
 	}
 	public float getFloatGold() {
 		return curGold;
 	}
 	public void setGold(float amount) {
 		float newGold = amount;
 		if (newGold < 0) newGold = 0;
 		curGold = newGold;
 	}
 	public void addGold(float amount) {
 		setGold(curGold + amount);
 	}
 	public void removeGold(float amount) {
 		setGold(curGold - amount);
 	}
 	
 	public int getStrength() {
 		int str = baseStrength;
 		for (BuffInstance buffInst : getAppliedBuffs()) {
 			str += buffInst.buff.strength;
 		}
 		return str;
 	}
 	public int getAgility() {
 		int agi = baseAgility;
 		for (BuffInstance buffInst : getAppliedBuffs()) {
 			agi += buffInst.buff.agility;
 		}
 		return agi;
 	}
 	public int getIntelligence() {
 		int intel = baseIntelligence;
 		for (BuffInstance buffInst : getAppliedBuffs()) {
 			intel += buffInst.buff.intelligence;
 		}
 		return intel;
 	}
 
 	@Override
 	public void saveNBTData(NBTTagCompound compound) {
		compound.setInteger(TAG_MANA, getMana());
 		compound.setFloat(TAG_PARTIAL_HP, partialHalfHeart);
 		compound.setInteger(TAG_GOLD, getGold());
 		NBTTagList buffsList = new NBTTagList();
 		for (BuffInstance buffInst : getAppliedBuffs()) {
 			buffsList.appendTag(buffInst.toNBT());
 		}
 		compound.setTag(TAG_BUFFS, buffsList);
 	}
 
 	@Override
 	public void loadNBTData(NBTTagCompound compound) {
		this.curMana = compound.getInteger(TAG_MANA);
 		this.partialHalfHeart = compound.getFloat(TAG_PARTIAL_HP);
 		this.curGold = compound.getInteger(TAG_GOLD);
 		NBTTagList buffsList = compound.getTagList(TAG_BUFFS);
 		for (int i = 0; i < buffsList.tagCount(); i++) {
 			NBTTagCompound buffTag = (NBTTagCompound) buffsList.tagAt(i);
 			BuffInstance buffInst = BuffInstance.fromNBT(buffTag, entityId);
 			addBuff(buffInst);
 		}
 	}
 
 	@Override
 	public void init(Entity entity, World world) {
 	}
 }
