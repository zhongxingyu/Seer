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
 
 import com.github.kanata3249.ffxi.status.*;
 
 // These status calculation fomula is based on the information from http://www.geocities.jp/lc7385/ffxieq/statuscalc.html
 public class JobAndRace extends StatusModifier implements Serializable  {
 	private static final long serialVersionUID = 1L;
 
 	// These values should be equal to StatusType.??
 	static final int HP = 0;
 	static final int MP = 1;
 	static final int STR = 2;
 	static final int DEX = 3;
 	static final int VIT = 4;
 	static final int AGI = 5;
 	static final int INT = 6;
 	static final int MND = 7;
 	static final int CHR = 8;
 	
	JobTraitSet mJobTraits;
 	int mSkills[];
 
 	public JobAndRace() {
 		super();
 		
 		loadDefaultValues();
 	}
 	
 	@Override
 	protected void loadDefaultValues() {
 		super.loadDefaultValues();
 		if (mSkills == null) {
 			// fill all skill value to 999
 
 			mSkills = new int[StatusType.MODIFIER_NUM.ordinal()];
 			for (int i = 0; i < mSkills.length; i++) {
 				mSkills[i] = 999;
 			}
 		} else if (mSkills.length != StatusType.MODIFIER_NUM.ordinal()) {
 			int skills[] = new int[StatusType.MODIFIER_NUM.ordinal()]; 
 			for (int i = 0; i < Math.min(skills.length, mSkills.length); i++) {
 				skills[i] = mSkills[i];
 			}
 			mSkills = skills;
 		}
 
 	}
 
 	// IStatus
 	public StatusValue getStatus(JobLevelAndRace level, StatusType type) {
 		StatusValue value;
 		if (mJobTraits == null)
 			mJobTraits = new JobTraitSet();
 		mJobTraits.setLevel(level);
 		value = mJobTraits.getStatus(level, type);
 
 		switch (type) {
 		case HP:
 			value.add(calcHP(level));
 			return value;
 		case MP:
 			value.add(calcMP(level));
 			return value;
 		case STR:
 		case DEX:
 		case VIT:
 		case AGI:
 		case INT:
 		case MND:
 		case CHR:
 			value.add(calcStatus(level, type));
 			return value;
 		case SKILL_HANDTOHAND:
 		case SKILL_DAGGER:
 		case SKILL_SWORD:
 		case SKILL_GREATSWORD:
 		case SKILL_AXE:
 		case SKILL_GREATAXE:
 		case SKILL_SCYTH:
 		case SKILL_POLEARM:
 		case SKILL_KATANA:
 		case SKILL_GREATKATANA:
 		case SKILL_CLUB:
 		case SKILL_STAFF:
 		case SKILL_ARCHERY:
 		case SKILL_MARKSMANSHIP:
 		case SKILL_THROWING:
 		case SKILL_GUARDING:
 		case SKILL_EVASION:
 		case SKILL_SHIELD:
 		case SKILL_PARRYING:
 
 		case SKILL_DIVINE_MAGIC:
 		case SKILL_HEALING_MAGIC:
 		case SKILL_ENCHANCING_MAGIC:
 		case SKILL_ENFEEBLING_MAGIC:
 		case SKILL_ELEMENTAL_MAGIC:
 		case SKILL_DARK_MAGIC:
 		case SKILL_SINGING:
 		case SKILL_STRING_INSTRUMENT:
 		case SKILL_WIND_INSTRUMENT:
 		case SKILL_NINJUTSU:
 		case SKILL_SUMMONING:
 		case SKILL_BLUE_MAGIC:
 			value.add(calcSkill(level, type));
 			return value;
 		default:
 			value.add(super.getStatus(level, type));
 			return value;
 		}
 	}
 
 	// util
 	private StatusValue calcHP(JobLevelAndRace level) {
 		StatusValue value;
 		value = new StatusValue(Dao.getHP(level.getRace(), level.getJob(), level.getLevel(), level.getSubJob(), level.getSubLevel()), 0);
 		return value;
 	}
 
 	private StatusValue calcMP(JobLevelAndRace level) {
 		StatusValue value;
 		value = new StatusValue(Dao.getMP(level.getRace(), level.getJob(), level.getLevel(), level.getSubJob(), level.getSubLevel()), 0);
 		return value;
 	}
 
 	private StatusValue calcStatus(JobLevelAndRace level, StatusType type) {
 		StatusValue value;
 		value = new StatusValue(Dao.getStatus(type, level.getRace(), level.getJob(), level.getLevel(), level.getSubJob(), level.getSubLevel()), 0);
 
 		return value;
 	}
 	
 	private StatusValue calcSkill(JobLevelAndRace level, StatusType type) {
 		int cap;
 		StatusValue v = new StatusValue(mSkills[type.ordinal()], 0, 0);
 		
 		cap = Dao.getSkillCap(type, level.getJob(), level.getLevel(), level.getSubJob(), level.getSubLevel());
 		if (v.getValue() > cap) {
 			v.setValue(cap);
 		}
 		return v;
 	}
 	public int getSkill(StatusType type) {
 		loadDefaultValues();  // quick hack for StatusType length change...
 		return mSkills[type.ordinal()];
 	}
 	public void setSkill(StatusType type, int value) {
 		loadDefaultValues();  // quick hack for StatusType length change...
 		mSkills[type.ordinal()] = value;
 	}
 }
