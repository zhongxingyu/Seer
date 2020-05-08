 /*
    Copyright 2011-2012 kanata3249
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
 package com.github.kanata3249.ffxieq;
 
 import java.io.Serializable;
 
 import com.github.kanata3249.ffxi.FFXIDAO;
 import com.github.kanata3249.ffxi.status.*;
 
 public class FFXICharacter implements IStatus, Serializable {
 	private static final long serialVersionUID = 1L;
 
 	JobLevelAndRace mLevel;
 
 	EquipmentSet mEquipment;
 	JobAndRace mJobAndRace;
 	MeritPoint mMerits;
 	boolean mInAbyssea;
 	AtmaSet mAtmaset;
 	Food mFood;
 	MagicSet mMagicSet;
 	VWAtmaSet mVWAtmaset;
 	BlueMagicSet mBlueMagicSet;
 
 	long mMeritPointId;
 
 	transient boolean mModified;
 	transient boolean mStatusCacheValid;
 	transient private StatusValue mCachedValues[];
 
 	static FFXIDAO Dao;
 	
 	public FFXICharacter() {
 		mLevel = new JobLevelAndRace(JobLevelAndRace.Hum, JobLevelAndRace.WAR, JobLevelAndRace.MNK, 95, 47);
 		mJobAndRace = new JobAndRace();
 		mEquipment = new EquipmentSet();
 		mMerits = new MeritPoint();
 		mAtmaset = new AtmaSet();
 		mMagicSet = new MagicSet();
 		mInAbyssea = false;
 		mVWAtmaset = new VWAtmaSet();
 		mBlueMagicSet = new BlueMagicSet();
 		mModified = false;
 		mStatusCacheValid = false;
 	}
 
 	public static FFXIDAO getDao() {
 		return Dao;
 	}
 	
 	public static void setDao(FFXIDAO dao) {
 		Dao = dao;
 	}
 
 	// IStatus
 	public StatusValue getStatus(JobLevelAndRace level, StatusType type) {
 		StatusValue total = new StatusValue(0, 0);
 		total.add(mJobAndRace.getStatus(level, mBlueMagicSet, type));
 		total.add(mMerits.getStatus(level, type));
 		total.add(mEquipment.getStatus(level, type));
 		if (mFood != null) {
 			total.add(mFood.getStatus(level, type));
 		}
 		if (mMagicSet != null) {
 			total.add(mMagicSet.getStatus(level, type));
 		}
 
 		if (mInAbyssea) {
 			total.add(mAtmaset.getStatus(level, type));
 		}
 		if (mVWAtmaset != null) {
 			total.add(mVWAtmaset.getStatus(level, type));
 		}
 		if (mBlueMagicSet != null) {
 			total.add(mBlueMagicSet.getStatus(level, type));
 		}
 		
 		return total;
 	}
 	
 	public int getRace() {
 		return mLevel.getRace();
 	}
 	public void setRace(int race) {
 		mModified = mModified || (mLevel.getRace() != race);
 		mStatusCacheValid = (mLevel.getRace() == race) && mStatusCacheValid;
 		mLevel.setRace(race);
 	}
 	public int getJob() {
 		return mLevel.getJob();
 	}
 	public void setJob(int job) {
 		mModified = mModified || (mLevel.getJob() != job);
 		mStatusCacheValid = (mLevel.getJob() == job) && mStatusCacheValid;
 		mLevel.setJob(job);
 	}
 	public int getJobLevel() {
 		return ((Integer)mLevel.getLevel());
 	}
 	public void setJobLevel(int level) {
 		mModified = mModified || (mLevel.getLevel() != level);
 		mStatusCacheValid = (mLevel.getLevel() == level) && mStatusCacheValid;
 		mLevel.setLevel(level);
 	}
 	public int getSubJob() {
 		return mLevel.getSubJob();
 	}
 	public void setSubJob(int subjob) {
 		mModified = mModified || (mLevel.getSubJob() != subjob);
 		mStatusCacheValid = (mLevel.getSubJob() == subjob) && mStatusCacheValid;
 		mLevel.setSubJob(subjob);
 	}
 	public int getSubJobLevel() {
 		return ((Integer)mLevel.getSubLevel());
 	}
 	public void setSubJobLevel(int sublevel) {
 		mModified = mModified || (mLevel.getSubLevel() != sublevel);
 		mStatusCacheValid = (mLevel.getSubLevel() == sublevel) && mStatusCacheValid;
 		mLevel.setSubLevel(sublevel);
 	}
 	public JobAndRace getJobAndRace() {
 		return mJobAndRace;
 	}
 	public void setJobAndRace(JobAndRace jobandrace) {
 		mModified = true;
 		mStatusCacheValid = false;
 		mJobAndRace = jobandrace;
 	}
 	public Equipment getEquipment(int part) {
 		return mEquipment.getEquipment(part);
 	}
 	public void setEquipment(int part, long id, long augId) {
 		mModified = true;
 		mStatusCacheValid = false;
 		mEquipment.setEquipment(part, id, augId);
 	}
 	public MeritPoint getMeritPoint() {
 		return mMerits;
 	}
 	public void setMeritPoint(MeritPoint merits) {
 		mModified = true;
 		mStatusCacheValid = false;
 		mMerits = merits;
 	}
 	public boolean isInAbbysea() {
 		return mInAbyssea;
 	}
 	public void setInAbysea(boolean inAbbysea) {
 		mModified = mModified || (mInAbyssea != inAbbysea);
 		mStatusCacheValid = (mInAbyssea == inAbbysea) && mStatusCacheValid;
 		this.mInAbyssea = inAbbysea;
 	}
 	public void setAbyssiteOfFurtherance(int n) {
 		mModified = mModified || (mAtmaset.getAbyssiteOfFurtherance() != n);
 		mStatusCacheValid = (mAtmaset.getAbyssiteOfFurtherance() == n) && mStatusCacheValid;
 		mAtmaset.setAbyssiteOfFurtherance(n);
 	}
 	public void setAbyssiteOfMerit(int n) {
 		mModified = mModified || (mAtmaset.getAbyssiteOfMerit() != n);
 		mStatusCacheValid = (mAtmaset.getAbyssiteOfMerit() == n) && mStatusCacheValid;
 		mAtmaset.setAbyssiteOfMerit(n);
 	}
 	public int getAbyssiteOfFurtherance() {
 		return mAtmaset.getAbyssiteOfFurtherance();
 	}
 	public int getAbyssiteOfMerit() {
 		return mAtmaset.getAbyssiteOfMerit();
 	}
 	public Atma getAtma(int index) {
 		return mAtmaset.getAtma(index);
 	}
 	public void setAtma(int index, long id) {
 		mModified = true;
 		mStatusCacheValid = false;
 		mAtmaset.setAtma(index, id);
 	}
 	public Food getFood(int index) {
 		return mFood;
 	}
 	public void setFood(int index, Food food) {
 		long cId, nId;
 		
 		cId = nId = -1;
 		if (food != null)
 			nId = food.getId();
 		if (mFood != null)
 			cId = mFood.getId();
 		if (nId == cId)
 			return;
 
 		mModified = true;
 		mStatusCacheValid = false;
 		mFood = food;
 	}
 	public int getNumMagic() {
 		if (mMagicSet == null)
 			mMagicSet = new MagicSet();
 		return mMagicSet.getNumMagic();
 	}
 	public Magic getMagic(int index) {
 		if (mMagicSet == null)
 			mMagicSet = new MagicSet();
 		return mMagicSet.getMagic(index);
 	}
 	public void setMagic(long id, long subid) {
 		Magic magic;
 		
 		if (mMagicSet == null)
 			mMagicSet = new MagicSet();
 		for (int i = 0; i < mMagicSet.getNumMagic(); i++) {
 			magic = mMagicSet.getMagic(i);
 			if (magic.getSubId() == subid) {
 				mMagicSet.setMagic(i, id);
 				mModified = true;
 				mStatusCacheValid = false;
 				return;
 			}
 		}
 
 		if (id >= 0) {
 			mMagicSet.addMagic(id);
 			mModified = true;
 			mStatusCacheValid = false;
 		}
 		return;
 	}
 	public Atma getVWAtma(int index) {
 		if (mVWAtmaset == null)
 			mVWAtmaset = new VWAtmaSet();
 		return mVWAtmaset.getAtma(index);
 	}
 	public void setVWAtma(int index, long id) {
 		mModified = true;
 		mStatusCacheValid = false;
 		if (mVWAtmaset == null)
 			mVWAtmaset = new VWAtmaSet();
 		mVWAtmaset.setAtma(index, id);
 	}
 	public int getNumBlueMagic() {
 		if (mBlueMagicSet == null)
 			mBlueMagicSet = new BlueMagicSet();
 		return mBlueMagicSet.getNumMagic();
 	}
 	public BlueMagic getBlueMagic(int index) {
 		if (mBlueMagicSet == null)
 			mBlueMagicSet = new BlueMagicSet();
 		return mBlueMagicSet.getMagic(index);
 	}
 	public void setBlueMagic(long id, boolean enable) {
 		if (mBlueMagicSet == null)
 			mBlueMagicSet = new BlueMagicSet();
 
 		if (enable) {
 			mBlueMagicSet.addMagic(id);
 		} else {
 			mBlueMagicSet.removeMagic(id);
 		}
 		mModified = true;
 		mStatusCacheValid = false;
 		return;
 	}
 	public boolean isBlueMagicSet(long itemId) {
 		if (mBlueMagicSet == null)
 			mBlueMagicSet = new BlueMagicSet();
 		return mBlueMagicSet.isSet(itemId);
 	}
 	public int getBP() {
 		int level = 0;
 
 		if (mLevel.getJob() == JobLevelAndRace.BLU) {
 			level = mLevel.getLevel();
 		} else if (mLevel.getSubJob() == JobLevelAndRace.BLU) {
 			level = mLevel.getSubLevel();
 		}
 		if (level > 0) {
 			int bp;
 			
 			bp = 10 + ((level - 1) / 10) * 5;
 			if (level >= 75)
 				bp += mMerits.getJobSpecificMeritPoint(JobLevelAndRace.BLU, 1, 3);
 			return bp;
 		}
 		return 0;
 	}
 	public int getCurrentBP() {
 		if (mBlueMagicSet == null)
 			mBlueMagicSet = new BlueMagicSet();
 		return mBlueMagicSet.getCurrentBP();
 	}
 	public int getBSP() {
 		int bp = getBP();
 
 		if (bp == 0)
 			return 0;
 		return Math.min(20, bp * 4 / 10 + 2);
 	}
 	public int getCurrentBSP() {
 		if (mBlueMagicSet == null)
 			mBlueMagicSet = new BlueMagicSet();
 		return mBlueMagicSet.getCurrentBSP();
 	}
 	public boolean isModified() {
 		return mModified;
 	}
 	public void setNotModified() {
 		mModified = false;
 	}
 	public boolean isCacheValid() {
 		return mStatusCacheValid;
 	}
 
 	public void cacheStatusValues() {
 		if (mStatusCacheValid && mCachedValues != null) {
 			return;
 		}
 
 		mStatusCacheValid = false;
 		mCachedValues = new StatusValue[StatusType.MODIFIER_NUM.ordinal()];
 		
 		StatusType types[] = StatusType.values();
 
 		for (int i = 0; i < types.length; i++) {
 			StatusType type = types[i];
 
 			switch (type) {
 			default:
 				mCachedValues[type.ordinal()] = getStatus(mLevel, type);
 				break;
 
 			case D:
 				mCachedValues[type.ordinal()] = getD();
 				break;
 			case Delay:
 				mCachedValues[type.ordinal()] = getDelay();
 				break;
 			case DelaySub:
 				mCachedValues[type.ordinal()] = getDelay();
 				break;
 			case DelayModifiedByHaste:
 				mCachedValues[type.ordinal()] = getDelayModifiedByHaste();
 				break;
 			case TP:
 				mCachedValues[type.ordinal()] = getTP();
 				break;
 			case TPRange:
 				mCachedValues[type.ordinal()] = getTPRange();
 				break;
 			case Accuracy:
 				mCachedValues[type.ordinal()] = getAccuracy();
 				break;
 			case AccuracySub:
 				mCachedValues[type.ordinal()] = getAccuracySub();
 				break;
 			case Attack:
 				mCachedValues[type.ordinal()] = getAttack();
 				break;
 			case AttackSub:
 				mCachedValues[type.ordinal()] = getAttackSub();
 				break;
 			case AccuracyRange:
 				mCachedValues[type.ordinal()] = getAccuracyRange();
 				break;
 			case AttackRange:
 				mCachedValues[type.ordinal()] = getAttackRange();
 				break;
 			case Evasion:
 				mCachedValues[type.ordinal()] = getEvasion();
 				break;
 			case Defence:
 				mCachedValues[type.ordinal()] = getDefence();
 				break;
 			case DamageCutPhysical:
 				mCachedValues[type.ordinal()] = getDamageCutPhysical();
 				break;
 			case DamageCutMagic:
 				mCachedValues[type.ordinal()] = getDamageCutMagic();
 				break;
 			case DamageCutBreath:
 				mCachedValues[type.ordinal()] = getDamageCutBreath();
 				break;
 				
 			case HP:
 				mCachedValues[type.ordinal()] = getHP();
 				break;
 			case MP:
 				mCachedValues[type.ordinal()] = getMP();
 				break;
 				
 			case Haste:
 				mCachedValues[type.ordinal()] = getHaste();
 				break;
 			case HasteByEquipment:
 				mCachedValues[type.ordinal()] = getHasteByEquipment();
 				break;
 			case Recast:
 				mCachedValues[type.ordinal()] = getRecast();
 				break;
 				
 			case SKILL_DIVINE_MAGIC:
 			case SKILL_HEALING_MAGIC:
 			case SKILL_ENCHANCING_MAGIC:
 			case SKILL_ENFEEBLING_MAGIC:
 			case SKILL_ELEMENTAL_MAGIC:
 			case SKILL_DARK_MAGIC:
 				mCachedValues[type.ordinal()] = getMagicSkill(type);
 				break;
 
 			case MODIFIER_NUM:
 				break;
 			}
 		}
 		mStatusCacheValid = true;
 	}
 
 	public StatusValue getStatus(StatusType type) {
 		if (mStatusCacheValid && mCachedValues != null) {
 			return mCachedValues[type.ordinal()];
 		}
 		
 		switch (type) {
 		default:
 			return getStatus(mLevel, type);
 
 		case D:
 			return getD();
 		case Delay:
 			return getDelay();
 		case DelaySub:
 			return getDelay();
 		case DelayModifiedByHaste:
 			return getDelayModifiedByHaste();
 		case Accuracy:
 			return getAccuracy();
 		case AccuracySub:
 			return getAccuracySub();
 		case Attack:
 			return getAttack();
 		case AttackSub:
 			return getAttackSub();
 		case AccuracyRange:
 			return getAccuracyRange();
 		case AttackRange:
 			return getAttackRange();
 		case Evasion:
 			return getEvasion();
 		case Defence:
 			return getDefence();
 		case DamageCutPhysical:
 			return getDamageCutPhysical();
 		case DamageCutMagic:
 			return getDamageCutMagic();
 		case DamageCutBreath:
 			return getDamageCutBreath();
 		case TP:
 			return getTP();
 		case TPRange:
 			return getTPRange();
 			
 		case HP:
 			return getHP();
 		case MP:
 			return getMP();
 
 		case Haste:
 			return getHaste();
 		case HasteByEquipment:
 			return getHasteByEquipment();
 		case Recast:
 			return getRecast();
 
 		case SKILL_DIVINE_MAGIC:
 		case SKILL_HEALING_MAGIC:
 		case SKILL_ENCHANCING_MAGIC:
 		case SKILL_ENFEEBLING_MAGIC:
 		case SKILL_ELEMENTAL_MAGIC:
 		case SKILL_DARK_MAGIC:
 			return getMagicSkill(type);
 		}
 	}
 
 	public StatusValue getD() {
 		StatusType type;
 		Equipment eq;
 
 		eq = mEquipment.getEquipment(EquipmentSet.MAINWEAPON);
 		if (eq != null) {
 			type = eq.getWeaponType();
 		} else {
 			type = StatusType.SKILL_HANDTOHAND;
 		}
 		if (type == StatusType.SKILL_HANDTOHAND) {
 			StatusValue value;
 			StatusValue skill;
 			int D;
 			
 			skill = getStatus(mLevel, type);
 			D = (skill.getValue() + skill.getAdditional()) * 11 / 100 + 3;
 			value = new StatusValue(D, 0, 0);
 			
 			value.add(getStatus(mLevel, StatusType.D));
 			return value;
 		} else {
 			return getStatus(mLevel, StatusType.D);
 		}
 	}
 
 	public StatusValue getDelay() {
 		StatusType type, subtype;
 		Equipment eq;
 
 		type = subtype = null;
 		eq = mEquipment.getEquipment(EquipmentSet.MAINWEAPON);
 		if (eq != null) {
 			type = eq.getWeaponType();
 		}
 		eq = mEquipment.getEquipment(EquipmentSet.SUBWEAPON);
 		if (eq != null) {
 			subtype = eq.getWeaponType();
 		}
 		if (type == null) {
 			type = StatusType.SKILL_HANDTOHAND;
 		}
 		switch (type) {
 		case SKILL_HANDTOHAND:
 			{	// Martial Arts
 				StatusValue base = getStatus(mLevel, StatusType.Delay);
 				StatusValue martialarts = getStatus(mLevel, StatusType.MartialArts);
 				
 				base.setValue(Math.max(0, 480 - martialarts.getTotal()) / 2);
 				return base;
 			}
 
 		case SKILL_DAGGER:
 		case SKILL_SWORD:
 		case SKILL_AXE:
 		case SKILL_KATANA:
 		case SKILL_CLUB:
 			if (subtype != null) {
 				switch (subtype) {
 				case SKILL_DAGGER:
 				case SKILL_SWORD:
 				case SKILL_AXE:
 				case SKILL_KATANA:
 				case SKILL_CLUB:
 					{
 						// Dual Wield
 						StatusValue base = getStatus(mLevel, StatusType.Delay);
 						StatusValue sub = getStatus(mLevel, StatusType.DelaySub);
 						StatusValue dualwield = getStatus(mLevel, StatusType.DualWield);
 						base.setValue((base.getValue() + sub.getValue()) / 2);
 						base.setAdditionalPercent(-(StatusValue.makePercentValue(dualwield.getAdditional(), 0) + dualwield.getAdditionalPercent()));
 						
 						return base;
 					}
 				}
 			}
 		}
 		return getStatus(mLevel, StatusType.Delay);
 	}
 	public StatusValue getDelayModifiedByHaste() {
 		StatusType type, subtype;
 		Equipment eq;
 		StatusValue v, haste;
 		int cap;
 
 		type = subtype = null;
 		eq = mEquipment.getEquipment(EquipmentSet.MAINWEAPON);
 		if (eq != null) {
 			type = eq.getWeaponType();
 		}
 		eq = mEquipment.getEquipment(EquipmentSet.SUBWEAPON);
 		if (eq != null) {
 			subtype = eq.getWeaponType();
 		}
 		if (type == null) {
 			type = StatusType.SKILL_HANDTOHAND;
 		}
 
 		v = getStatus(mLevel, StatusType.Delay);
 		cap = v.getTotal() * 200 / 1000;
 		haste = getHaste();
 		switch (type) {
 		case SKILL_HANDTOHAND:
 			{	// Martial Arts
 				StatusValue martialarts = getStatus(mLevel, StatusType.MartialArts);
 				
 				v.setValue(Math.max(0, 480 - martialarts.getTotal()) / 2);
 				v.setValue(v.getTotal());
 				v.setAdditional(0);
 				break;
 			}
 
 		case SKILL_DAGGER:
 		case SKILL_SWORD:
 		case SKILL_AXE:
 		case SKILL_KATANA:
 		case SKILL_CLUB:
 			if (subtype != null) {
 				switch (subtype) {
 				case SKILL_DAGGER:
 				case SKILL_SWORD:
 				case SKILL_AXE:
 				case SKILL_KATANA:
 				case SKILL_CLUB:
 					{
 						// Dual Wield
 						StatusValue base = getStatus(mLevel, StatusType.Delay);
 						StatusValue sub = getStatus(mLevel, StatusType.DelaySub);
 						StatusValue dualwield = getStatus(mLevel, StatusType.DualWield);
 
 						cap = (base.getTotal() + sub.getTotal()) / 2 * 200 / 1000;
 						v.setValue((base.getValue() + sub.getValue()) / 2);
 						v.setAdditionalPercent(-(StatusValue.makePercentValue(dualwield.getAdditional(), 0) + dualwield.getAdditionalPercent()));
 						break;
 					}
 				}
 			}
 			break;
 		}
 		v.setValue(Math.max(cap, v.getTotal() * (10000 - haste.getAdditionalPercent()) / 10000));
 		v.setAdditionalPercent(0);
 		return v;
 	}
 
 	private StatusValue calcTPByDelay(int delay) {
 		int tp, storeTP;
 
 		if (delay == 0) {
 			tp = 0;
 		} else if (delay <= 180) {
 			tp = 500 + (delay - 180) * 150 / 180;
 		} else if (delay <= 450) {
 			tp = 500 + (delay - 180) * 650 / 270;
 		} else if (delay <= 480) {
 			tp = 1150 + (delay - 450) * 150 / 30;
 		} else if (delay <= 530) {
 			tp = 1300 + (delay - 480) * 150 / 50;
 		} else {
 			tp = 1450 + (delay - 530) * 350 / 470;
 		}
 		tp -= tp % 10;
 		
 		storeTP = getStatus(mLevel, StatusType.StoreTP).getTotal();
 		if (storeTP != 0) {
 			tp = tp * (100 + storeTP) / 100;
 			tp -= tp % 10;
 		}
 		
 		return new StatusValue(0, 0, StatusValue.makePercentValue(tp / 100, tp % 100 - tp % 10));
 	}
 
 	public StatusValue getTP() {
 		return calcTPByDelay(getDelay().getTotal());
 	}
 
 	public StatusValue getTPRange() {
 		return calcTPByDelay(getStatus(mLevel, StatusType.DelayRange).getTotal());
 	}
 
 	int calcAccuracyByWeaponType(StatusType type) {
 		int value;
 		int skillvalue, modvalue;
 		StatusValue skill = getStatus(mLevel, type);
 		StatusValue mod;
 
 		value = 0;
 		skillvalue = skill.getValue() + skill.getAdditional();
 		switch (type) {
 		case SKILL_HANDTOHAND:
 		case SKILL_DAGGER:
 		case SKILL_SWORD:
 		case SKILL_AXE:
 		case SKILL_KATANA:
 		case SKILL_CLUB:
 			mod = getStatus(mLevel, StatusType.DEX);
 			modvalue = mod.getValue() + mod.getAdditional();
 			if (skillvalue < 200) {
 				value = modvalue * 50 / 100 + skillvalue;
 			} else {
 				value = modvalue * 50 / 100 + 200 + (skillvalue - 200) * 90 / 100;
 			}
 			break;
 
 		case SKILL_GREATSWORD:
 		case SKILL_GREATAXE:
 		case SKILL_SCYTH:
 		case SKILL_POLEARM:
 		case SKILL_GREATKATANA:
 		case SKILL_STAFF:
 			mod = getStatus(mLevel, StatusType.DEX);
 			modvalue = mod.getValue() + mod.getAdditional();
 			if (skillvalue < 200) {
 				value = modvalue * 75 / 100 + skillvalue;
 			} else {
 				value = modvalue * 75 / 100 + 200 + (skillvalue - 200) * 90 / 100;
 			}
 			break;
 		case SKILL_ARCHERY:
 		case SKILL_MARKSMANSHIP:
 		case SKILL_THROWING:
 			mod = getStatus(mLevel, StatusType.AGI);
 			modvalue = mod.getValue() + mod.getAdditional();
 			if (skillvalue < 200) {
 				value = modvalue * 50 / 100 + skillvalue;
 			} else {
 				value = modvalue * 50 / 100 + 200 + (skillvalue - 200) * 90 / 100;
 			}
 			break;
 		}
 		return value;
 	}
 	public StatusValue getAccuracy() {
 		StatusType type;
 		StatusValue mod = getStatus(mLevel, StatusType.Accuracy);
 		Equipment eq = mEquipment.getEquipment(EquipmentSet.MAINWEAPON);
 		if (eq != null) {
 			type = eq.getWeaponType();
 		} else {
 			type = StatusType.SKILL_HANDTOHAND;
 		}
 		if (type != null) {
 			mod.setValue(calcAccuracyByWeaponType(type) + mod.getValue());
 		}
 		return mod;
 	}
 	public StatusValue getAccuracySub() {
 		StatusType type;
 		StatusValue mod = getStatus(mLevel, StatusType.Accuracy);
 		Equipment eq = mEquipment.getEquipment(EquipmentSet.SUBWEAPON);
 		if (eq != null) {
 			type = eq.getWeaponType();
 			if (type != null && type != StatusType.SKILL_SHIELD) {
 				mod.setValue(calcAccuracyByWeaponType(type) + mod.getValue());
 			} else {
 				mod = new StatusValue(0, 0, 0);
 			}
 			return mod;
 		} else {
 			return new StatusValue(0, 0, 0);
 		}
 	}
 	int calcAttackByWeaponType(StatusType type) {
 		int value;
 		int skillvalue, strvalue;
 		StatusValue skill = getStatus(mLevel, type);
 		StatusValue str = getStatus(mLevel, StatusType.STR);
 
 		value = 0;
 		strvalue = str.getValue() + str.getAdditional();
 		skillvalue = skill.getValue() + skill.getAdditional();
 		switch (type) {
 		case SKILL_HANDTOHAND:
 		case SKILL_DAGGER:
 		case SKILL_SWORD:
 		case SKILL_AXE:
 		case SKILL_KATANA:
 		case SKILL_CLUB:
 		case SKILL_ARCHERY:
 		case SKILL_MARKSMANSHIP:
 		case SKILL_THROWING:
 			value = strvalue * 50 / 100 + skillvalue + 8;
 			break;
 
 		case SKILL_GREATSWORD:
 		case SKILL_GREATAXE:
 		case SKILL_SCYTH:
 		case SKILL_POLEARM:
 		case SKILL_GREATKATANA:
 		case SKILL_STAFF:
 			value = strvalue * 75 / 100 + skillvalue + 8;
 			break;
 		}
 		return value;
 	}
 	public StatusValue getAttack() {
 		StatusType type;
 		StatusValue mod = getStatus(mLevel, StatusType.Attack);
 		Equipment eq = mEquipment.getEquipment(EquipmentSet.MAINWEAPON);
 		if (eq != null) {
 			type = eq.getWeaponType();
 		} else {
 			type = StatusType.SKILL_HANDTOHAND;
 		}
 		if (type != null) {
 			mod.setValue(calcAttackByWeaponType(type) + mod.getValue());
 		}
 		return mod;
 	}
 	public StatusValue getAttackSub() {
 		StatusType type;
 		StatusValue mod = getStatus(mLevel, StatusType.Attack);
 		Equipment eq = mEquipment.getEquipment(EquipmentSet.SUBWEAPON);
 		if (eq != null) {
 			type = eq.getWeaponType();
 			if (type != null && type != StatusType.SKILL_SHIELD) {
 				mod.setValue(calcAttackByWeaponType(type) + mod.getValue());
 			} else {
 				mod = new StatusValue(0, 0, 0);
 			}
 			return mod;
 		} else {
 			return new StatusValue(0, 0, 0);
 		}
 	}
 	public StatusValue getAccuracyRange() {
 		StatusType type;
 		StatusValue mod = getStatus(mLevel, StatusType.AccuracyRange);
 		Equipment eq = mEquipment.getEquipment(EquipmentSet.RANGE);
 		if (eq == null) {
 			eq = mEquipment.getEquipment(EquipmentSet.ANMO);
 		}
 		if (eq != null) {
 			type = eq.getWeaponType();
 			if (type != null) {
 				mod.setValue(calcAccuracyByWeaponType(type) + mod.getValue());
 			}
 		}
 		return mod;
 	}
 	public StatusValue getAttackRange() {
 		StatusType type;
 		StatusValue mod = getStatus(mLevel, StatusType.AttackRange);
 		Equipment eq = mEquipment.getEquipment(EquipmentSet.RANGE);
 		if (eq == null) {
 			eq = mEquipment.getEquipment(EquipmentSet.ANMO);
 		}
 		if (eq != null) {
 			type = eq.getWeaponType();
 			if (type != null) {
 				mod.setValue(calcAttackByWeaponType(type) + mod.getValue());
 			}
 		}
 		return mod;
 	}
 
 	public StatusValue getEvasion() {
 		int value;
 		int skillvalue, stsvalue;
 		StatusValue skill = getStatus(mLevel, StatusType.SKILL_EVASION);
 		StatusValue sts = getStatus(mLevel, StatusType.AGI);
 		StatusValue mod = getStatus(mLevel, StatusType.Evasion);
 
 		value = 0;
 		stsvalue = sts.getValue() + sts.getAdditional();
 		skillvalue = skill.getValue() + skill.getAdditional();
 		if (skillvalue < 200) {
 			value = stsvalue * 50 / 100 + skillvalue;
 		} else {
 			value = stsvalue * 50 / 100 + 200 + (skillvalue - 200) * 90 / 100;
 		}
 		mod.setValue(value + mod.getValue());
 		return mod;
 	}
 	public StatusValue getDefence() {
 		int stsvalue, lvlvalue;
 		StatusValue mod;
 		StatusValue sts = getStatus(mLevel, StatusType.VIT);
 
 		stsvalue = sts.getValue() + sts.getAdditional();
 		lvlvalue = mLevel.getLevel();
 		if (lvlvalue > 50) {
 			if (lvlvalue > 60) {
 				lvlvalue += 10;
 			} else {
 				lvlvalue += lvlvalue - 50;
 			}
 		}
 		mod = getStatus(mLevel, StatusType.Defence);
 		mod.setValue(lvlvalue + 8 + stsvalue * 50 / 100 + mod.getValue());
 		return mod;
 	}
 	public StatusValue getDamageCutPhysical() {
 		StatusValue value;
 		
 		value = getStatus(mLevel, StatusType.DamageCut);
 		value.add(getStatus(mLevel, StatusType.DamageCutPhysical));
 		return value;
 	}
 	public StatusValue getDamageCutMagic() {
 		StatusValue value;
 		
 		value = getStatus(mLevel, StatusType.DamageCut);
 		value.add(getStatus(mLevel, StatusType.DamageCutMagic));
 		return value;
 	}
 	public StatusValue getDamageCutBreath() {
 		StatusValue value;
 		
 		value = getStatus(mLevel, StatusType.DamageCut);
 		value.add(getStatus(mLevel, StatusType.DamageCutBreath));
 		return value;
 	}
 	
 	public StatusValue getHP() {
 		StatusValue HP, MP, convToHP, convToMP;
 		int conv;
 		
 		HP = getStatus(mLevel, StatusType.HP);
 		MP = getStatus(mLevel, StatusType.MP);
 		convToMP = getStatus(mLevel, StatusType.Convert_HP_TO_MP);
 		convToHP = getStatus(mLevel, StatusType.Convert_MP_TO_HP);
 		conv = convToHP.getTotal() - convToMP.getTotal();
 		if (conv < 0) {
 			conv = Math.min(HP.getTotal(), -conv);
 			HP.setAdditional(HP.getAdditional() - conv);
 			MP.setAdditional(MP.getAdditional() + conv);
 		} else {
 			conv = Math.min(MP.getTotal(), conv);
 			HP.setAdditional(HP.getAdditional() + conv);
 			MP.setAdditional(MP.getAdditional() - conv);
 		}
 		
 		return HP;
 	}
 	public StatusValue getMP() {
 		StatusValue HP, MP, convToHP, convToMP;
 		int conv;
 		
 		HP = getStatus(mLevel, StatusType.HP);
 		MP = getStatus(mLevel, StatusType.MP);
 		convToMP = getStatus(mLevel, StatusType.Convert_HP_TO_MP);
 		convToHP = getStatus(mLevel, StatusType.Convert_MP_TO_HP);
 		conv = convToHP.getTotal() - convToMP.getTotal();
 		if (conv < 0) {
 			conv = Math.min(HP.getTotal(), -conv);
 			HP.setAdditional(HP.getAdditional() - conv);
 			MP.setAdditional(MP.getAdditional() + conv);
 		} else {
 			conv = Math.min(MP.getTotal(), conv);
 			HP.setAdditional(HP.getAdditional() + conv);
 			MP.setAdditional(MP.getAdditional() - conv);
 		}
 		
 		return MP;
 	}
 	
 	public StatusValue getHaste() {
 		StatusValue haste, ehaste, mhaste, ahaste;
 
 		haste = getStatus(mLevel, StatusType.Haste);
 		haste.sub(getStatus(mLevel, StatusType.Slow));
 		ehaste  = mEquipment.getStatus(mLevel, StatusType.Haste);
 		if (mInAbyssea) {
 			ehaste.add(mAtmaset.getStatus(mLevel, StatusType.Haste));
 			ehaste.sub(mAtmaset.getStatus(mLevel, StatusType.Slow));
 		}
 		if (mVWAtmaset != null) {
 			ehaste.add(mVWAtmaset.getStatus(mLevel, StatusType.Haste));
 			ehaste.sub(mVWAtmaset.getStatus(mLevel, StatusType.Slow));
 		}
 		/* Equipment haste cap: 25% (Typical 25% build is 24.7%, so we use 26% cap at this time. */
 		if (ehaste.getAdditionalPercent() > 2600) {
 			haste.setAdditionalPercent(haste.getAdditionalPercent() - (ehaste.getAdditionalPercent() - 2600));
 		}
 		if (mMagicSet == null)
 			mMagicSet = new MagicSet();
 		/* Magicset haste cap 43.75% */
 		mhaste  = mMagicSet.getStatus(mLevel, StatusType.Haste);
 		mhaste.sub(mMagicSet.getStatus(mLevel, StatusType.Slow));
 
 		if (mhaste.getAdditionalPercent() > 4375)
 			haste.setAdditionalPercent(haste.getAdditionalPercent() - (mhaste.getAdditionalPercent() - 4375));
 		
 		/* Ability haste cap 25% */
 		ahaste = mMagicSet.getStatus(mLevel, StatusType.HasteAbility);
 		if (ahaste.getAdditionalPercent() > 2500)
 			ahaste.setAdditionalPercent(2500);
 
 		haste.add(ahaste);
 		return haste;
 	}
 
 	public StatusValue getHasteByEquipment() {
 		StatusValue haste, ehaste;
 
 		haste = ehaste  = mEquipment.getStatus(mLevel, StatusType.Haste);
 		if (mInAbyssea) {
 			ehaste.add(mAtmaset.getStatus(mLevel, StatusType.Haste));
 			ehaste.sub(mAtmaset.getStatus(mLevel, StatusType.Slow));
 		}
 		if (mVWAtmaset != null) {
 			ehaste.add(mVWAtmaset.getStatus(mLevel, StatusType.Haste));
 			ehaste.sub(mVWAtmaset.getStatus(mLevel, StatusType.Slow));
 		}
 		/* Equipment haste cap: 25% (Typical 25% build is 24.7%, so we use 26% cap at this time. */
 		if (ehaste.getAdditionalPercent() > 2600) {
 			haste.setAdditionalPercent(haste.getAdditionalPercent() - (ehaste.getAdditionalPercent() - 2600));
 		}
 		return haste;
 	}
 
 	public StatusValue getRecast() {
 		StatusValue haste, ehaste, mhaste, fastcast;
 		StatusValue v;
 
 		haste = getStatus(mLevel, StatusType.Haste);
 		haste.sub(getStatus(mLevel, StatusType.Slow));
 		ehaste  = mEquipment.getStatus(mLevel, StatusType.Haste);
 		if (mInAbyssea) {
 			ehaste.add(mAtmaset.getStatus(mLevel, StatusType.Haste));
 			ehaste.sub(mAtmaset.getStatus(mLevel, StatusType.Slow));
 		}
 		if (mVWAtmaset != null) {
 			ehaste.add(mVWAtmaset.getStatus(mLevel, StatusType.Haste));
 			ehaste.sub(mVWAtmaset.getStatus(mLevel, StatusType.Slow));
 		}
 		/* Equipment haste cap: 25% (Typical 25% build is 24.7%, so we use 26% cap at this time. */
 		if (ehaste.getAdditionalPercent() > 2600) {
 			haste.setAdditionalPercent(haste.getAdditionalPercent() - (ehaste.getAdditionalPercent() - 2600));
 		}
 		if (mMagicSet == null)
 			mMagicSet = new MagicSet();
 		/* Magicset haste cap 43.75% */
 		mhaste  = mMagicSet.getStatus(mLevel, StatusType.Haste);
 		mhaste.sub(mMagicSet.getStatus(mLevel, StatusType.Slow));
 
 		if (mhaste.getAdditionalPercent() > 4375)
 			haste.setAdditionalPercent(haste.getAdditionalPercent() - (mhaste.getAdditionalPercent() - 4375));
 		
 		fastcast = getStatus(mLevel, StatusType.FastCast);
 		
		v = new StatusValue(0, 0, (10000 - StatusValue.makePercentValue(fastcast.getTotal(), 0) / 2) * (10000 - haste.getAdditionalPercent()) / 10000);
 		v.setAdditionalPercent(Math.max(v.getAdditionalPercent(), StatusValue.makePercentValue(20, 0)));
 
 		return v;
 	}
 
 	public StatusValue getMagicSkill(StatusType type) {
 		StatusValue skill, bskill, arts, larts, darts;
 		int fskill, cskill, lskill;
 
 		skill = getStatus(mLevel, type);
 		larts = getStatus(mLevel, StatusType.LightArts);
 		darts = getStatus(mLevel, StatusType.DarkArts);
 
 		arts = null;
 		switch (type) {
 		case SKILL_DIVINE_MAGIC:
 		case SKILL_HEALING_MAGIC:
 		case SKILL_ENCHANCING_MAGIC:
 			if (larts.getTotal() > 0) {
 				arts = larts;
 			}
 			break;
 
 		case SKILL_ENFEEBLING_MAGIC:
 			if (larts.getTotal() > 0) {
 				arts = larts;
 			} else if (darts.getTotal() > 0) {
 				arts = darts;
 			}
 			break;
 		case SKILL_ELEMENTAL_MAGIC:
 		case SKILL_DARK_MAGIC:
 			if (darts.getTotal() > 0) {
 				arts = darts;
 			}
 			break;
 		}
 		
 		if (arts != null) {
 			bskill = mJobAndRace.getStatus(mLevel, mBlueMagicSet, type);
 			fskill = Dao.getSkillCap(type, "D", mLevel.getLevel());
 			lskill = Dao.getSkillCap(type, "E", mLevel.getLevel());
 			cskill = Dao.getSkillCap(type, "B+", mLevel.getLevel());
 			if (arts.getAdditionalPercent() > 1) {
 				cskill += 15;
 			}
 			if (bskill.getValue() < lskill) {
 				skill.setValue(lskill + (cskill - fskill));
 			} else if (bskill.getValue() < fskill) {
 				skill.setValue(skill.getValue() + (cskill - fskill));
 			} else if (bskill.getValue() < cskill) {
 				skill.setValue(skill.getValue() - bskill.getValue() + cskill);
 			} else {
 				// nop
 			}
 		}
 
 		return skill;
 	}
 
 	public SortedStringList getUnknownTokens() {
 		SortedStringList unknownTokens = new SortedStringList();
 		unknownTokens.mergeList(mEquipment.getUnknownTokens());
 		unknownTokens.mergeList(mJobAndRace.getUnknownTokens());
 		if (mInAbyssea) {
 			unknownTokens.mergeList(mAtmaset.getUnknownTokens());
 		}
 		if (mFood != null) {
 			unknownTokens.mergeList(mFood.getUnknownTokens());
 		}
 		if (mMagicSet != null) {
 			unknownTokens.mergeList(mMagicSet.getUnknownTokens());
 		}
 		if (mVWAtmaset != null) {
 			unknownTokens.mergeList(mVWAtmaset.getUnknownTokens());
 		}
 		return unknownTokens;
 	}
 	
 	public void reload() {
 		mEquipment.reloadEquipments();
 		mAtmaset.reloadAtmas();
 		mModified = true;
 	}
 
 	public long[] reloadForUpdatingDatabase() {
 		long result[] = mEquipment.reloadEquipmentsForUpdatingDatabase();
 		if (result[EquipmentSet.EQUIPMENT_NUM] != 0)
 			mModified = true;
 		
 		return result;
 	}
 	
 	public boolean reloadMagicsForUpdatingDatabase() {
 		boolean ret = mMagicSet.reloadMagicsForUpdatingDatabase();
 		
 		if (ret)
 			mModified = true;
 		
 		return ret;
 	}
 
 	public boolean reloadAugmentsIfChangesThere() {
 		return mEquipment.reloadAugmentsIfChangesThere();
 	}
 
 	public long getMeritPointId() {
 		return mMeritPointId;
 	}
 
 	public void setMeritPointId(long meritPointId) {
 		if (meritPointId != mMeritPointId)
 			mModified = true;
 		this.mMeritPointId = meritPointId;
 	}
 }
