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
 package com.github.kanata3249.ffxieq.android;
 
 import com.github.kanata3249.ffxi.FFXIDAO;
 import com.github.kanata3249.ffxi.FFXIString;
 import com.github.kanata3249.ffxi.status.SortedStringList;
 import com.github.kanata3249.ffxi.status.StatusType;
 import com.github.kanata3249.ffxi.status.StatusValue;
 import com.github.kanata3249.ffxieq.FFXICharacter;
 import com.github.kanata3249.ffxieq.R;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.ProgressBar;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.app.Activity;
 
 public class CharacterStatusView extends ScrollView {
 	int mDisplayParam;
 	boolean mShowSkill;
 	FFXICharacter mCharInfo;
 	FFXICharacter mCharInfoToCompare;
 	static public final int GETSTATUS_STRING_TOTAL = 0;
 	static public final int GETSTATUS_STRING_SEPARATE = 1;
 	static final Object [] sObjLock = new Object[0];
 	boolean mUnusedStatusValues[];
 	FFXIDAO mDao;
 
 	public CharacterStatusView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		
 		mDisplayParam = GETSTATUS_STRING_SEPARATE;
 		mShowSkill = false;
 		mCharInfo = null;
 		View children = ((Activity)context).getLayoutInflater().inflate(R.layout.statusview, null);
 		this.addView(children);
 	}
 
 	public boolean bindFFXICharacter(FFXICharacter charinfo, FFXICharacter toCompare) {
 		mCharInfo = charinfo;
 		mCharInfoToCompare = toCompare;
 		mDao = FFXICharacter.getDao();
 		notifyDatasetChanged();
 
 		return true;
 	}
 	
 	public void setDisplayParam(int param) {
 		if (mDisplayParam != param) {
 			mDisplayParam = param;
 			if (mCharInfo != null) {
 				notifyDatasetChanged();
 			}
 		}
 	}
 
 	public void showSkillValue(boolean showSkill) {
 		if (mShowSkill != showSkill) {
 			mShowSkill = showSkill;
 			if (mCharInfo != null) {
 				notifyDatasetChanged();
 			}
 		}
 	}
 
 	public void notifyDatasetChanged() {
 		TextView tv;
 
 		if (cacheStatusAsync()) {
 			return;
 		}
 		
 		mUnusedStatusValues = new boolean[StatusType.MODIFIER_NUM.ordinal()];
 		for (int i = 0; i < mUnusedStatusValues.length; i++) {
 			mUnusedStatusValues[i] = true;
 		}
 
 		tv = (TextView)findViewById(R.id.HP);
     	if (tv != null) {
     		tv.setText(getHP(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.MP);
     	if (tv != null) {
     		tv.setText(getMP(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.STR);
     	if (tv != null) {
     		tv.setText(getSTR(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.DEX);
     	if (tv != null) {
     		tv.setText(getDEX(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.VIT);
     	if (tv != null) {
     		tv.setText(getVIT(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.AGI);
     	if (tv != null) {
     		tv.setText(getAGI(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.INT);
     	if (tv != null) {
     		tv.setText(getINT(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.MND);
     	if (tv != null) {
     		tv.setText(getMND(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.CHR);
     	if (tv != null) {
     		tv.setText(getCHR(mDisplayParam));
     	}
 
     	tv = (TextView)findViewById(R.id.Accuracy);
     	if (tv != null) {
     		tv.setText(getAccuracy(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.Attack);
     	if (tv != null) {
     		tv.setText(getAttack(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.AccuracySub);
     	if (tv != null) {
     		tv.setText(getAccuracySub(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.AttackSub);
     	if (tv != null) {
     		tv.setText(getAttackSub(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.AccuracyRange);
     	if (tv != null) {
     		tv.setText(getAccuracyRange(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.AttackRange);
     	if (tv != null) {
     		tv.setText(getAttackRange(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.Haste);
     	if (tv != null) {
     		tv.setText(getHaste(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.Slow);
     	if (tv != null) {
     		tv.setText(getSlow(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.SubtleBlow);
     	if (tv != null) {
     		tv.setText(getSubtleBlow(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.StoreTP);
     	if (tv != null) {
     		tv.setText(getStoreTP(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.Evasion);
     	if (tv != null) {
     		tv.setText(getEvasion(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.DoubleAttack);
     	if (tv != null) {
     		tv.setText(getDoubleAttack(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.TrippleAttack);
     	if (tv != null) {
     		tv.setText(getTrippleAttack(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.QuadAttack);
     	if (tv != null) {
     		tv.setText(getQuadAttack(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.CriticalRate);
     	if (tv != null) {
     		tv.setText(getCriticalRate(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.CriticalDamage);
     	if (tv != null) {
     		tv.setText(getCriticalDamage(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.CriticalDamage);
     	if (tv != null) {
     		tv.setText(getCriticalDamage(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.Enmity);
     	if (tv != null) {
     		tv.setText(getEnmity(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.AccuracyMagic);
     	if (tv != null) {
     		tv.setText(getAccuracyMagic(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.AttackMagic);
     	if (tv != null) {
     		tv.setText(getAttackMagic(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.D);
     	if (tv != null) {
     		tv.setText(getD(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.DSub);
     	if (tv != null) {
     		tv.setText(getDSub(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.DRange);
     	if (tv != null) {
     		tv.setText(getDRange(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.Delay);
     	if (tv != null) {
     		tv.setText(getDelay(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.DelaySub);
     	if (tv != null) {
     		tv.setText(getDelaySub(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.DelayRange);
     	if (tv != null) {
     		tv.setText(getDelayRange(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.TP);
     	if (tv != null) {
     		tv.setText(getTP(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.TPRange);
     	if (tv != null) {
     		tv.setText(getTPRange(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.Defence);
     	if (tv != null) {
     		tv.setText(getDefence(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.DefenceMagic);
     	if (tv != null) {
     		tv.setText(getDefenceMagic(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.DamageCutPhysical);
     	if (tv != null) {
     		tv.setText(getDamageCutPhysical(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.DamageCutMagic);
     	if (tv != null) {
     		tv.setText(getDamageCutMagic(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.DamageCutBreath);
     	if (tv != null) {
     		tv.setText(getDamageCutBreath(mDisplayParam));
     	}
 
     	tv = (TextView)findViewById(R.id.RegistFire);
     	if (tv != null) {
     		tv.setText(getRegistFire(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.RegistIce);
     	if (tv != null) {
     		tv.setText(getRegistIce(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.RegistWind);
     	if (tv != null) {
     		tv.setText(getRegistWind(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.RegistEarth);
     	if (tv != null) {
     		tv.setText(getRegistEarth(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.RegistLightning);
     	if (tv != null) {
     		tv.setText(getRegistLightning(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.RegistWater);
     	if (tv != null) {
     		tv.setText(getRegistWater(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.RegistLight);
     	if (tv != null) {
     		tv.setText(getRegistLight(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.RegistDark);
     	if (tv != null) {
     		tv.setText(getRegistDark(mDisplayParam));
     	}
     	tv = (TextView)findViewById(R.id.UnknownTokens);
     	if (tv != null) {
     		tv.setText(getUnknownTokens() + "\n" + getSkillValues());
     	}
 	}
 
 	private String getStatusString(StatusType type, int separate) {
 		StatusValue v = getStatus(type);
 		int value;
 		boolean hide_percent = false;
 		
 		if (type == StatusType.TP || type == StatusType.TPRange)
 			hide_percent = true;
 		if (mCharInfoToCompare != null) {
 			StatusValue v1, v2;
 			
 			separate = GETSTATUS_STRING_SEPARATE;
 			v1 = mCharInfo.getStatus(type);
 			v2 = mCharInfoToCompare.getStatus(type);
 			if (v1.getValue() == 0 && v1.getAdditional() == 0 && v2.getValue() == 0 && v1.getAdditional() == 0
 				&& (v1.getAdditionalPercent() != 0 || v2.getAdditionalPercent() != 0)) {
 				StringBuilder sb = new StringBuilder();
 
 				value = v.getValue();
 				sb.append(value / 100);
 				if (value % 100 != 0) {
 					sb.append('.');
					sb.append(Math.abs(value) % 100);
 					if (value % 10 == 0)
 						sb.deleteCharAt(sb.length() - 1);
 				}
 				if (!hide_percent)
 					sb.append('%');
 				value = v.getAdditionalPercent();
 				if (value != 0) {
 					if (value > 0) {
 						sb.append('+');
 					} else {
 						sb.append('-');
 						value = -value;
 					}
 					sb.append(value / 100);
 					if ((value % 100) != 0) {
 						sb.append('.');
 						if (value % 100 < 10)
 							sb.append('0');
 						sb.append(value % 100);
 						if (value % 10 == 0)
 							sb.deleteCharAt(sb.length() - 1);
 					}
 					if (!hide_percent)
 						sb.append('%');
 				}
 				return sb.toString();
 			}
 		}
 		if (v.getAdditionalPercent() != 0 && (v.getValue() == 0 && v.getAdditional() == 0)) {
 			StringBuilder sb = new StringBuilder();
 
 			value = v.getAdditionalPercent();
 			sb.append(value / 100);
 			if ((value % 100) != 0) {
 				if (value < 0)
 					value = -value;
 				sb.append('.');
 				if (value % 100 < 10)
 					sb.append('0');
 				sb.append(value % 100);
 				if (value % 10 == 0)
 					sb.deleteCharAt(sb.length() - 1);
 			}
 			if (!hide_percent)
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
 				sb.append(value / 100);
 				if ((value % 100) != 0) {
 					if (value < 0)
 						value = -value;
 					sb.append('.');
 					if (value % 100 < 10)
 						sb.append('0');
 					sb.append(value % 100);
 					if (value % 10 == 0)
 						sb.deleteCharAt(sb.length() - 1);
 				}
 				if (!hide_percent)
 					sb.append('%');
 			}
 			value = v.getAdditionalPercentWithCap();
 			if (value != 0) {
 				sb.append('\n');
 				if (value > 0) {
 					sb.append('+');
 				}
 				sb.append(value / 100);
 				if ((value % 100) != 0) {
 					if (value < 0)
 						value = -value;
 					sb.append('.');
 					if (value % 100 < 10)
 						sb.append('0');
 					sb.append(value % 100);
 					if (value % 10 == 0)
 						sb.deleteCharAt(sb.length() - 1);
 				}
 				if (!hide_percent)
 					sb.append('%');
 				sb.append("(");
 				sb.append(v.getCap());
 				sb.append(')');
 			}
 			return sb.toString();
 		} else {
 			value = v.getTotal();
 			return ((Integer)value).toString();
 		}
 	}
 	
 	private StatusValue getStatus(StatusType type) {
 		if (mUnusedStatusValues != null) {
 			mUnusedStatusValues[type.ordinal()] = false;
 		}
 		if (mCharInfoToCompare != null) {
 			StatusValue base = new StatusValue(0, 0, 0);
 			
 			base.add(mCharInfoToCompare.getStatus(type));
 			base.diff(mCharInfo.getStatus(type));
 			return base;
 		} else {
 			return mCharInfo.getStatus(type);
 		}
 	}
 
 	public String getHP(int separate) {
 		return getStatusString(StatusType.HP, separate);
 	}
 	public String getMP(int separate) {
 		return getStatusString(StatusType.MP, separate);
 	}
 	public String getSTR(int separate) {
 		return getStatusString(StatusType.STR, separate);
 	}
 	public String getDEX(int separate) {
 		return getStatusString(StatusType.DEX, separate);
 	}
 	public String getVIT(int separate) {
 		return getStatusString(StatusType.VIT, separate);
 	}
 	public String getAGI(int separate) {
 		return getStatusString(StatusType.AGI, separate);
 	}
 	public String getINT(int separate) {
 		return getStatusString(StatusType.INT, separate);
 	}
 	public String getMND(int separate) {
 		return getStatusString(StatusType.MND, separate);
 	}
 	public String getCHR(int separate) {
 		return getStatusString(StatusType.CHR, separate);
 	}
 	public String getD(int separate) {
 		return getStatusString(StatusType.D, separate);
 	}
 	public String getDSub(int separate) {
 		return getStatusString(StatusType.DSub, separate);
 	}
 	public String getDRange(int separate) {
 		return getStatusString(StatusType.DRange, separate);
 	}
 	public String getDelay(int separate) {
 		return getStatusString(StatusType.Delay, separate);
 	}
 	public String getDelaySub(int separate) {
 		return getStatusString(StatusType.DelaySub, separate);
 	}
 	public String getDelayRange(int separate) {
 		return getStatusString(StatusType.DelayRange, separate);
 	}
 	public String getTP(int separate) {
 		return getStatusString(StatusType.TP, separate);
 	}
 	public String getTPRange(int separate) {
 		return getStatusString(StatusType.TPRange, separate);
 	}
 	public String getAccuracy(int separate) {
 		return getStatusString(StatusType.Accuracy, separate);
 	}
 	public String getAccuracySub(int separate) {
 		return getStatusString(StatusType.AccuracySub, separate);
 	}
 	public String getAttack(int separate) {
 		return getStatusString(StatusType.Attack, separate);
 	}
 	public String getAttackSub(int separate) {
 		return getStatusString(StatusType.AttackSub, separate);
 	}
 	public String getAccuracyRange(int separate) {
 		return getStatusString(StatusType.AccuracyRange, separate);
 	}
 	public String getAttackRange(int separate) {
 		return getStatusString(StatusType.AttackRange, separate);
 	}
 	public String getHaste(int separate) {
 		return getStatusString(StatusType.Haste, separate);
 	}
 	public String getSlow(int separate) {
 		return getStatusString(StatusType.Slow, separate);
 	}
 	public String getSubtleBlow(int separate) {
 		return getStatusString(StatusType.SubtleBlow, separate);
 	}
 	public String getStoreTP(int separate) {
 		return getStatusString(StatusType.StoreTP, separate);
 	}
 	public String getEvasion(int separate) {
 		return getStatusString(StatusType.Evasion, separate);
 	}
 	public String getDoubleAttack(int separate) {
 		return getStatusString(StatusType.DoubleAttack, separate);
 	}
 	public String getTrippleAttack(int separate) {
 		return getStatusString(StatusType.TrippleAttack, separate);
 	}
 	public String getQuadAttack(int separate) {
 		return getStatusString(StatusType.QuadAttack, separate);
 	}
 	public String getCriticalRate(int separate) {
 		return getStatusString(StatusType.CriticalRate, separate);
 	}
 	public String getCriticalDamage(int separate) {
 		return getStatusString(StatusType.CriticalDamage, separate);
 	}
 	public String getEnmity(int separate) {
 		return getStatusString(StatusType.Enmity, separate);
 	}
 	public String getAttackMagic(int separate) {
 		return getStatusString(StatusType.AttackMagic, separate);
 	}
 	public String getAccuracyMagic(int separate) {
 		return getStatusString(StatusType.AccuracyMagic, separate);
 	}
 	public String getDefence(int separate) {
 		return getStatusString(StatusType.Defence, separate);
 	}
 	public String getDefenceMagic(int separate) {
 		return getStatusString(StatusType.DefenceMagic, separate);
 	}
 	public String getDamageCutPhysical(int separate) {
 		return getStatusString(StatusType.DamageCutPhysical, separate);
 	}
 	public String getDamageCutMagic(int separate) {
 		return getStatusString(StatusType.DamageCutMagic, separate);
 	}
 	public String getDamageCutBreath(int separate) {
 		return getStatusString(StatusType.DamageCutBreath, separate);
 	}
 	public String getRegistFire(int separate) {
 		return getStatusString(StatusType.Regist_Fire, separate);
 	}
 	public String getRegistIce(int separate) {
 		return getStatusString(StatusType.Regist_Ice, separate);
 	}
 	public String getRegistWind(int separate) {
 		return getStatusString(StatusType.Regist_Wind, separate);
 	}
 	public String getRegistEarth(int separate) {
 		return getStatusString(StatusType.Regist_Earth, separate);
 	}
 	public String getRegistLightning(int separate) {
 		return getStatusString(StatusType.Regist_Lightning, separate);
 	}
 	public String getRegistWater(int separate) {
 		return getStatusString(StatusType.Regist_Water, separate);
 	}
 	public String getRegistLight(int separate) {
 		return getStatusString(StatusType.Regist_Light, separate);
 	}
 	public String getRegistDark(int separate) {
 		return getStatusString(StatusType.Regist_Dark, separate);
 	}
 	public String getUnknownTokens() {
 		SortedStringList tokens, tokensToCompare, unusedTokens;
 		boolean unusedStatusValues[];
 
 		unusedStatusValues = mUnusedStatusValues;
 		mUnusedStatusValues = null;
 		tokens = mCharInfo.getUnknownTokens();
 		unusedTokens = getUnusedStatusValues(unusedStatusValues);
 		tokens.mergeList(unusedTokens);
 		if (mCharInfoToCompare != null) {
 			tokensToCompare = mCharInfoToCompare.getUnknownTokens();
 			tokensToCompare.mergeList(unusedTokens);
 			return tokens.diffList(tokensToCompare);
 		}
 		return tokens.toString();
 	}
 	public String getSkillValues() {
 		StatusType skills[] = StatusType.values();
 		String result = "";
 		
 		if (!mShowSkill) {
 			return result;
 		}
 		for (int i = 0; i < skills.length - 1; i++) {  /* skip last MODIFIER_NUM */
 			String name = getSkillName(skills[i]);
 			if (name != null) {
 				result += getSkillName(skills[i]) + " " + getStatusString(skills[i], mDisplayParam) + "\n";
 			}
 		}
 		return result;
 	}
 	
 	public boolean cacheStatusAsync() {
 		if (mCharInfo.isCacheValid() && (mCharInfoToCompare == null || mCharInfoToCompare.isCacheValid()))
 			return false;
 		final ProgressBar progress = (ProgressBar)findViewById(R.id.ProgressBar);
 		if (progress != null) {
 			progress.setVisibility(VISIBLE);
 		}
 		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
 			@Override
 			protected Void doInBackground(Void... params) {
 				synchronized (sObjLock) {
 					mCharInfo.cacheStatusValues();
 					if (mCharInfoToCompare != null)
 						mCharInfoToCompare.cacheStatusValues();
 				}
 				return null;
 			}
 
 			@Override
 			protected void onPostExecute(Void result) {
 				notifyDatasetChanged();
 				if (progress != null)
 					progress.setVisibility(INVISIBLE);
 			}
 		};
 		task.execute();
 		
 		return true;
 	}
 	
 	SortedStringList getUnusedStatusValues(boolean unusedStatusValues[]) {
 		StatusType unused[] = StatusType.values();
 		SortedStringList result;
 		
 		result = new SortedStringList();
 		for (int i = 0; i < unused.length - 1; i++) {  /* skip last MODIFIER_NUM */
 			if (unusedStatusValues[unused[i].ordinal()]) {
 				String name = isStatusTypeToDisplay(unused[i]);
 				if (name != null) {
 					result.addString(name + " " + getStatusString(unused[i], mDisplayParam));
 				}
 			}
 		}
 		return result;
 	}
 	
 	String isStatusTypeToDisplay(StatusType type) {
 		StatusValue v = getStatus(type);
 		if (v.getValue() == 0 && v.getAdditional() == 0 && v.getAdditionalPercent() == 0 && v.getAdditionalPercentWithCap() == 0)
 			return null;
 		switch (type) {
 		case CriticalRateDefence:
 			return mDao.getString(FFXIString.TOKEN_CRITICAL_RATE_DEFENCE);
 		case CriticalDamageDefence:
 			return mDao.getString(FFXIString.TOKEN_CRITICAL_DAMAGE_DEFENCE);
 		case SpellInterruptionRate:
 			return getResources().getString(R.string.SpellInterruptionRate);
 		case MagicEvasion:
 			return mDao.getString(FFXIString.TOKEN_MAGIC_EVASION);
 		case Counter:
 			return mDao.getString(FFXIString.TOKEN_COUNTER);
 		case CriticalDamageRange:
 			return mDao.getString(FFXIString.TOKEN_CRITICAL_DAMAGE_RANGE);
 		case HealingHP:
 			return mDao.getString(FFXIString.TOKEN_HEALING_HP);
 		case HealingMP:
 			return mDao.getString(FFXIString.TOKEN_HEALING_MP);
 		case CurePotency:
 			return mDao.getString(FFXIString.TOKEN_CURE_POTENCY);
 		case SongSpellCastingTime:
 			return mDao.getString(FFXIString.TOKEN_SONG_SPELL_CASTING_TIME);
 		case SongRecastDelay:
 			return mDao.getString(FFXIString.TOKEN_SONG_RECAST_DELAY);
 		case ConserveTP:
 			return mDao.getString(FFXIString.TOKEN_CONSERVE_TP);
 		case ConserveMP:
 			return mDao.getString(FFXIString.TOKEN_CONSERVE_MP);
 		case KickAttack:
 			return mDao.getString(FFXIString.TOKEN_KICK_ATTACK);
 		case FastCast:
 			return mDao.getString(FFXIString.TOKEN_FASTCAST);
 		}
 		return null;
 	}
 
 	String getSkillName(StatusType type) {
 		StatusValue v = getStatus(type);
 		if (v.getTotal() == 0)
 			return null;
 		switch (type) {
 		case SKILL_HANDTOHAND:
 			return mDao.getString(FFXIString.TOKEN_SKILL_HANDTOHAND);
 		case SKILL_DAGGER:
 			return mDao.getString(FFXIString.TOKEN_SKILL_DAGGER);
 		case SKILL_SWORD:
 			return mDao.getString(FFXIString.TOKEN_SKILL_SWORD);
 		case SKILL_GREATSWORD:
 			return mDao.getString(FFXIString.TOKEN_SKILL_GREATSWORD);
 		case SKILL_AXE:
 			return mDao.getString(FFXIString.TOKEN_SKILL_AXE);
 		case SKILL_GREATAXE:
 			return mDao.getString(FFXIString.TOKEN_SKILL_GREATAXE);
 		case SKILL_SCYTH:
 			return mDao.getString(FFXIString.TOKEN_SKILL_SCYTH);
 		case SKILL_POLEARM:
 			return mDao.getString(FFXIString.TOKEN_SKILL_POLEARM);
 		case SKILL_KATANA:
 			return mDao.getString(FFXIString.TOKEN_SKILL_KATANA);
 		case SKILL_GREATKATANA:
 			return mDao.getString(FFXIString.TOKEN_SKILL_GREATKATANA);
 		case SKILL_CLUB:
 			return mDao.getString(FFXIString.TOKEN_SKILL_CLUB);
 		case SKILL_STAFF:
 			return mDao.getString(FFXIString.TOKEN_SKILL_STAFF);
 		case SKILL_ARCHERY:
 			return mDao.getString(FFXIString.TOKEN_SKILL_ARCHERY);
 		case SKILL_MARKSMANSHIP:
 			return mDao.getString(FFXIString.TOKEN_SKILL_MARKSMANSHIP);
 		case SKILL_THROWING:
 			return mDao.getString(FFXIString.TOKEN_SKILL_THROWING);
 		case SKILL_GUARDING:
 			return mDao.getString(FFXIString.TOKEN_SKILL_GUARDING);
 		case SKILL_EVASION:
 			return mDao.getString(FFXIString.TOKEN_SKILL_EVASION);
 		case SKILL_SHIELD:
 			return mDao.getString(FFXIString.TOKEN_SKILL_SHIELD);
 		case SKILL_PARRYING:
 			return mDao.getString(FFXIString.TOKEN_SKILL_PARRYING);
 
 		case SKILL_DIVINE_MAGIC:
 			return mDao.getString(FFXIString.TOKEN_SKILL_DIVINE_MAGIC);
 		case SKILL_HEALING_MAGIC:
 			return mDao.getString(FFXIString.TOKEN_SKILL_HEALING_MAGIC);
 		case SKILL_ENCHANCING_MAGIC:
 			return mDao.getString(FFXIString.TOKEN_SKILL_ENCHANCING_MAGIC);
 		case SKILL_ENFEEBLING_MAGIC:
 			return mDao.getString(FFXIString.TOKEN_SKILL_ENFEEBLING_MAGIC);
 		case SKILL_ELEMENTAL_MAGIC:
 			return mDao.getString(FFXIString.TOKEN_SKILL_ELEMENTAL_MAGIC);
 		case SKILL_DARK_MAGIC:
 			return mDao.getString(FFXIString.TOKEN_SKILL_DARK_MAGIC);
 		case SKILL_SINGING:
 			return mDao.getString(FFXIString.TOKEN_SKILL_SINGING);
 		case SKILL_STRING_INSTRUMENT:
 			return mDao.getString(FFXIString.TOKEN_SKILL_STRING_INSTRUMENT);
 		case SKILL_WIND_INSTRUMENT:
 			return mDao.getString(FFXIString.TOKEN_SKILL_WIND_INSTRUMENT);
 		case SKILL_NINJUTSU:
 			return mDao.getString(FFXIString.TOKEN_SKILL_NINJUTSU);
 		case SKILL_SUMMONING:
 			return mDao.getString(FFXIString.TOKEN_SKILL_SUMMONING);
 		case SKILL_BLUE_MAGIC:
 			return mDao.getString(FFXIString.TOKEN_SKILL_BLUE_MAGIC);
 		}
 		return null;
 	}
 
 }
