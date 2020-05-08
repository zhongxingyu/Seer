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
 package com.github.kanata3249.ffxieq	;
 
 import java.io.Serializable;
 
 import com.github.kanata3249.ffxi.status.*;
 
 public class MeritPoint extends StatusModifier implements Serializable  {
 	private static final long serialVersionUID = 1L;
 
 	//
 	public static final int MAX_JOB_SPECIFIC_MERIT_POINT_CATEGORY = 2;
 	public static final int MAX_JOB_SPECIFIC_MERIT_POINT = 10;
 	int mMeritPoints[];
 	int mJobSpecificMeritPoints[][][];
 	transient MeritPointJobTraitSet mJobTraits;
 
 	public MeritPoint() {
 		super();
 		
 		loadDefaultValues();
 	}
 
 
 	@Override
 	protected void loadDefaultValues() {
 		super.loadDefaultValues();
 		if (mMeritPoints == null) {
 			mMeritPoints = new int[StatusType.MODIFIER_NUM.ordinal()];
 		} else if (mMeritPoints.length != StatusType.MODIFIER_NUM.ordinal()) {
 			int merits[] = new int[StatusType.MODIFIER_NUM.ordinal()]; 
 			for (int i = 0; i < Math.min(merits.length, mMeritPoints.length); i++) {
 				merits[i] = mMeritPoints[i];
 			}
 			mMeritPoints = merits;
 		}
 		if (mJobSpecificMeritPoints == null) {
 			mJobSpecificMeritPoints = new int[JobLevelAndRace.JOB_MAX][][];
 			for (int i = 0; i < mJobSpecificMeritPoints.length; i++) {
 				mJobSpecificMeritPoints[i] = new int[MAX_JOB_SPECIFIC_MERIT_POINT_CATEGORY][];
 				for (int ii = 0; ii < mJobSpecificMeritPoints[i].length; ii++) {
 					mJobSpecificMeritPoints[i][ii] = new int[MAX_JOB_SPECIFIC_MERIT_POINT];
 				}
 			}
 		}
 	}
 
 
 	@Override
 	public StatusValue getStatus(JobLevelAndRace level, StatusType type) {
 		loadDefaultValues();
 		if (mJobTraits == null)
 			mJobTraits = new MeritPointJobTraitSet(level, mJobSpecificMeritPoints);
 		else
 			mJobTraits.setLevel(level, mJobSpecificMeritPoints);
 
 		StatusValue v;
 		int merit = getMeritPoint(type);
 		int clevel = level.getLevel();
 		int meritcap;
 		if (clevel >= 75)
 			meritcap = 999;
 		else if (clevel >= 55) {
			meritcap = 5 + (clevel - 55) / 5;
 		} else {
 			meritcap = clevel / 10;
 		}
 		v = mJobTraits.getStatus(level, type);
 		
 		switch (type) {
 		case HP:
 		case MP:
 			v.setValue(Math.min(meritcap, merit) * 10);
 			break;
 		case STR:
 		case DEX:
 		case VIT:
 		case AGI:
 		case INT:
 		case MND:
 		case CHR:
 			v.setValue(Math.min(meritcap, merit));
 			break;
 			
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
 			v.setValue(Math.min(meritcap, merit) * 2);
 			break;
 		case SKILL_GUARDING:
 		case SKILL_EVASION:
 		case SKILL_SHIELD:
 		case SKILL_PARRYING:
 			v.setValue(Math.min(meritcap, merit) * 2);
 			break;
 			
 		case Enmity:
 			if (merit >= 0) {
 				v.setValue(Math.min(meritcap, merit));
 			} else {
 				v.setValue(Math.max(-meritcap, merit));
 			}
 			break;
 		case CriticalRate:
 		case CriticalRateDefence:	        	
 			v.setAdditionalPercent(StatusValue.makePercentValue(Math.min(meritcap, merit), 0));
 			break;
 		case SpellInterruptionRate:
 			v.setAdditionalPercent(StatusValue.makePercentValue(Math.min(meritcap, merit) * 2, 0));
 			break;
 		}
 
 		return v;
 	}
 	
 	public int getMeritPoint(StatusType type) {
 		loadDefaultValues();  // quick hack for StatusType length change...
 		return mMeritPoints[type.ordinal()];
 	}
 	public void setMeritPoint(StatusType type, int value) {
 		loadDefaultValues();  // quick hack for StatusType length change...
 		mMeritPoints[type.ordinal()] = value;
 	}
 	public int getJobSpecificMeritPoint(int job, int category, int index) {
 		loadDefaultValues();
 		return mJobSpecificMeritPoints[job][category][index];
 	}
 	public void setJobSpecificMeritPoint(int job, int category, int index, int value) {
 		loadDefaultValues();
 		mJobSpecificMeritPoints[job][category][index] = value;
 	}
 }
