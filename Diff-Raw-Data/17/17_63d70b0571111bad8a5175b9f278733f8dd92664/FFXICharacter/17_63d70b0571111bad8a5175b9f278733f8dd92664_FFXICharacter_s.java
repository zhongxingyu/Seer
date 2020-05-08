 /*
    Copyright 2011 kanata3249
 
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
 
 	static public final int GETSTATUS_STRING_TOTAL = 0;
 	static public final int GETSTATUS_STRING_SEPARATE = 1;
 
 	JobLevelAndRace mLevel;
 
 	EquipmentSet mEquipment;
 	JobAndRace mJobAndRace;
 	MeritPoint mMerits;
 	boolean mInAbyssea;
 
 	AtmaSet mAtmaset;
 	// TODO food
 	
 	static FFXIDAO Dao;
 	
 	public FFXICharacter() {
 		mLevel = new JobLevelAndRace(JobLevelAndRace.Hum, JobLevelAndRace.WAR, JobLevelAndRace.THF, 1, 0);
 		mJobAndRace = new JobAndRace();
 		mEquipment = new EquipmentSet();
 		mMerits = new MeritPoint();
 		mAtmaset = new AtmaSet();
 		mInAbyssea = false;
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
 		total.add(mJobAndRace.getStatus(level, type));
 		total.add(mMerits.getStatus(level, type));
 		total.add(mEquipment.getStatus(level, type));
 		if (mInAbyssea) {
 			total.add(mAtmaset.getStatus(level, type));
 		}
 		
 		return total;
 	}
 	
 	public int getRace() {
 		return mLevel.getRace();
 	}
 	public void setRace(int race) {
 		mLevel.setRace(race);
 	}
 	public int getJob() {
 		return mLevel.getJob();
 	}
 	public void setJob(int job) {
 		mLevel.setJob(job);
 	}
 	public int getJobLevel() {
 		return ((Integer)mLevel.getLevel());
 	}
 	public void setJobLevel(int level) {
 		mLevel.setLevel(level);
 	}
 	public int getSubJob() {
 		return mLevel.getSubJob();
 	}
 	public void setSubJob(int subjob) {
 		mLevel.setSubJob(subjob);
 	}
 	public int getSubJobLevel() {
 		return ((Integer)mLevel.getSubLevel());
 	}
 	public void setSubJobLevel(int sublevel) {
 		mLevel.setSubLevel(sublevel);
 	}
 	public JobAndRace getJobAndRace() {
 		return mJobAndRace;
 	}
 	public void setJobAndRace(JobAndRace jobandrace) {
 		mJobAndRace = jobandrace;
 	}
 	public Equipment getEquipment(int part) {
 		return mEquipment.getEquipment(part);
 	}
 	public void setEquipment(int part, long id) {
 		mEquipment.setEquipment(part, id);
 	}
 	public MeritPoint getMeritPoint() {
 		return mMerits;
 	}
 	public void setMeritPoint(MeritPoint merits) {
 		mMerits = merits;
 	}
 	public boolean isInAbbisea() {
 		return mInAbyssea;
 	}
 	public void setInAbbisea(boolean inAbbisea) {
 		this.mInAbyssea = inAbbisea;
 	}
 	public void setAbyssiteOfFurtherance(int n) {
 		mAtmaset.setAbyssiteOfFurtherance(n);
 	}
 	public void setAbyssiteOfMerit(int n) {
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
 		mAtmaset.setAtma(index, id);
 	}
 
 	private String getStatusString(StatusValue v, int separate) {
 		int value;
 		
 		if (v.getAdditionalPercent() != 0 && (v.getValue() == 0 && v.getAdditional() == 0)) {
 			StringBuilder sb = new StringBuilder();
 
 			value = v.getAdditionalPercent();
 			sb.append(v.getAdditionalPercent());
 			sb.append('%');
 			return sb.toString();
 		} else if (separate == GETSTATUS_STRING_SEPARATE) {
 			StringBuilder sb = new StringBuilder();
 			
 			sb.append(v.getValue());
 			value = v.getAdditional();
 			if (value != 0) {
 				sb.append(' ');
 				if (value > 0) {
 					sb.append('+');
 				}
 				sb.append(v.getAdditional());
 			}
 			value = v.getAdditionalPercent();
 			if (value != 0) {
 				sb.append(' ');
 				if (value > 0) {
 					sb.append('+');
 				}
 				sb.append(v.getAdditionalPercent());
 				sb.append('%');
 			}
 			return sb.toString();
 		} else {
 			value = v.getValue()+ v.getAdditional();
 			if (v.getAdditionalPercent() != 0) {
 				value += value * v.getAdditionalPercent() / 100;
 			}
 			return ((Integer)value).toString();
 		}
 	}
 
 	public String getHP(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.HP), separate);
 	}
 	public String getMP(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.MP), separate);
 	}
 	public String getSTR(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.STR), separate);
 	}
 	public String getDEX(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.DEX), separate);
 	}
 	public String getVIT(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.VIT), separate);
 	}
 	public String getAGI(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.AGI), separate);
 	}
 	public String getINT(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.INT), separate);
 	}
 	public String getMND(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.MND), separate);
 	}
 	public String getCHR(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.CHR), separate);
 	}
 	public String getD(int separate) {
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
 			return getStatusString(value, separate);
 		} else {
 			return getStatusString(getStatus(mLevel, StatusType.D), separate);
 		}
 	}
 	public String getDSub(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.DSub), separate);
 	}
 	public String getDRange(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.DRange), separate);
 	}
 	public String getDelay(int separate) {
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
 				int rank = martialarts.getValue();
 				
 				if (rank > 0) {
 					base.setValue(400 - 20 * (rank - 1));
 				} else {
 					base.setValue(480);
 				}
 				return getStatusString(base, separate);
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
 						StatusValue dualwield = getStatus(mLevel, StatusType.DualWield);
 						base.setAdditionalPercent(-(dualwield.getAdditional() + dualwield.getAdditionalPercent()));
 						
 						return getStatusString(base, separate);
 					}
 				}
 			}
 		}
 		return getStatusString(getStatus(mLevel, StatusType.Delay), separate);
 	}
 	public String getDelaySub(int separate) {
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
 						StatusValue base = getStatus(mLevel, StatusType.DelaySub);
 						StatusValue dualwield = getStatus(mLevel, StatusType.DualWield);
 						base.setAdditionalPercent(-(dualwield.getAdditional() + dualwield.getAdditionalPercent()));
 
 						return getStatusString(base, separate);
 					}
 				}
 			}
 		}
 		return getStatusString(new StatusValue(0, 0, 0), separate);
 	}
 	public String getDelayRange(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.DelayRange), separate);
 	}
 	int calcAccuracyByWeaponType(StatusType type) {
 		int value;
 		int skillvalue, modvalue;
 		StatusValue skill = getStatus(mLevel, type);
 		StatusValue mod = getStatus(mLevel, StatusType.DEX);
 
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
 	public String getAccuracy(int separate) {
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
 		return getStatusString(mod, separate);
 	}
 	public String getAccuracySub(int separate) {
 		StatusType type;
 		StatusValue mod = getStatus(mLevel, StatusType.Accuracy);
 		Equipment eq = mEquipment.getEquipment(EquipmentSet.SUBWEAPON);
 		if (eq != null) {
 			type = eq.getWeaponType();
 			if (type != null) {
 				mod.setValue(calcAccuracyByWeaponType(type) + mod.getValue());
 			}
 			return getStatusString(mod, separate);
 		} else {
 			return getStatusString(new StatusValue(0, 0, 0), separate);
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
			value = strvalue * 75 / 100 + skillvalue;
 			break;
 		}
 		return value;
 	}
 	public String getAttack(int separate) {
 		StatusType type;
 		StatusValue mod = getStatus(mLevel, StatusType.Accuracy);
 		Equipment eq = mEquipment.getEquipment(EquipmentSet.MAINWEAPON);
 		if (eq != null) {
 			type = eq.getWeaponType();
 		} else {
 			type = StatusType.SKILL_HANDTOHAND;
 		}
 		if (type != null) {
 			mod.setValue(calcAttackByWeaponType(type) + mod.getValue());
 		}
 		return getStatusString(mod, separate);
 	}
 	public String getAttackSub(int separate) {
 		StatusType type;
 		StatusValue mod = getStatus(mLevel, StatusType.Accuracy);
 		Equipment eq = mEquipment.getEquipment(EquipmentSet.SUBWEAPON);
 		if (eq != null) {
 			type = eq.getWeaponType();
 			if (type != null) {
 				mod.setValue(calcAttackByWeaponType(type) + mod.getValue());
 			}
 			return getStatusString(mod, separate);
 		} else {
 			return getStatusString(new StatusValue(0, 0, 0), separate);
 		}
 	}
 	public String getAccuracyRange(int separate) {
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
 		return getStatusString(mod, separate);
 	}
 	public String getAttackRange(int separate) {
 		StatusType type;
 		StatusValue mod = getStatus(mLevel, StatusType.Accuracy);
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
 		return getStatusString(mod, separate);
 	}
 	public String getHaste(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.Haste), separate);
 	}
 	public String getSlow(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.Slow), separate);
 	}
 	public String getSubtleBlow(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.SubtleBlow), separate);
 	}
 	public String getStoreTP(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.StoreTP), separate);
 	}
 	public String getEvasion(int separate) {
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
 		return getStatusString(mod, separate);
 	}
 	public String getDoubleAttack(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.DoubleAttack), separate);
 	}
 	public String getTrippleAttack(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.TrippleAttack), separate);
 	}
 	public String getQuadAttack(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.QuadAttack), separate);
 	}
 	public String getCriticalRate(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.CriticalRate), separate);
 	}
 	public String getCriticalDamage(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.CriticalDamage), separate);
 	}
 	public String getEnmity(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.Enmity), separate);
 	}
 	public String getAttackMagic(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.AttackMagic), separate);
 	}
 	public String getAccuracyMagic(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.AccuracyMagic), separate);
 	}
 	public String getDefence(int separate) {
		int stsvalue;
 		StatusValue mod;
 		StatusValue sts = getStatus(mLevel, StatusType.VIT);
 
 		stsvalue = sts.getValue() + sts.getAdditional();
 		mod = getStatus(mLevel, StatusType.Defence);
		mod.setValue(mLevel.getLevel() + stsvalue * 50 / 100 + mod.getValue());
 		return getStatusString(mod, separate);
 	}
 	public String getDefenceMagic(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.DefenceMagic), separate);
 	}
 	public String getDamageCutPhysical(int separate) {
 		StatusValue value;
 		
 		value = getStatus(mLevel, StatusType.DamageCut);
 		value.add(getStatus(mLevel, StatusType.DamageCutPhysical));
 		return getStatusString(value, separate);
 	}
 	public String getDamageCutMagic(int separate) {
 		StatusValue value;
 		
 		value = getStatus(mLevel, StatusType.DamageCut);
 		value.add(getStatus(mLevel, StatusType.DamageCutMagic));
 		return getStatusString(value, separate);
 	}
 	public String getDamageCutBreath(int separate) {
 		StatusValue value;
 		
 		value = getStatus(mLevel, StatusType.DamageCut);
 		value.add(getStatus(mLevel, StatusType.DamageCutBreath));
 		return getStatusString(value, separate);
 	}
 	public String getRegistFire(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.Regist_Fire), separate);
 	}
 	public String getRegistIce(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.Regist_Ice), separate);
 	}
 	public String getRegistWind(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.Regist_Wind), separate);
 	}
 	public String getRegistEarth(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.Regist_Earth), separate);
 	}
 	public String getRegistLightning(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.Regist_Lightning), separate);
 	}
 	public String getRegistWater(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.Regist_Water), separate);
 	}
 	public String getRegistLight(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.Regist_Light), separate);
 	}
 	public String getRegistDark(int separate) {
 		return getStatusString(getStatus(mLevel, StatusType.Regist_Dark), separate);
 	}
 
 	public String getUnknownTokens() {
 		if (mInAbyssea) {
 			return mEquipment.getUnknownTokens() + mAtmaset.getUnknownTokens();
 		} else {
 			return mEquipment.getUnknownTokens();
 		}
 	}
 }
