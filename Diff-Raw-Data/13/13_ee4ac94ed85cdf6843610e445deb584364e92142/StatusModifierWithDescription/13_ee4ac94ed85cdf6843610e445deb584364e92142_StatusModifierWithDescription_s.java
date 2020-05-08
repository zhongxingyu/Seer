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
 package com.github.kanata3249.ffxi.status;
 
 import java.util.Hashtable;
 
 import com.github.kanata3249.ffxi.FFXIString;
 
 public class StatusModifierWithDescription extends StatusModifier {
 	private static final long serialVersionUID = 1L;
 
 	protected String mDescription;
 	
 	protected transient SortedStringList mUnknownTokens;
 	protected transient boolean mNeedParseDescription;
 	private transient Hashtable<String, DescriptionTokenHandler> fTokenHandler;
 	static transient String mConvertCharactersFrom;
 	static transient String mConvertCharactersTo;
 	
 	abstract class DescriptionTokenHandler {
 		abstract boolean handleToken(String token, String parameter);
 	}
 
 	public String getAdditionalDescription() {
 		return null;
 	}
 
 	public boolean parseDescription() {
 		boolean updated = false;
 
 		if (fTokenHandler == null) {
 			setupTokenHandler();
 		} else {
 			if (!mNeedParseDescription)
 				return updated;
 		}
 
 		loadDefaultValues();
 		mUnknownTokens = new SortedStringList();
 		updated = parseDescriptionSub(mDescription);
 		if (getAdditionalDescription() != null) {
 			if (parseDescriptionSub(getAdditionalDescription()))
 				updated = true;
 		}
 		mNeedParseDescription = false;
 		
 		return updated;
 	}
 	
 	public boolean parseDescriptionSub(String description) {
 		boolean updated = false;
 		String tokens[];
 
 		tokens = new String[1];
 		tokens[0] = description;
 		tokens = tokens[0].split(Dao.getString(FFXIString.TOKEN_AugmentComment));
 		if (tokens.length > 1) {
 			mUnknownTokens.addString(Dao.getString(FFXIString.TOKEN_AugmentComment) + tokens[1]);
 			updated = true;
 		}
 		tokens = tokens[0].split(Dao.getString(FFXIString.TOKEN_Latent_Effect));
 		if (tokens.length > 1) {
 			mUnknownTokens.addString(Dao.getString(FFXIString.TOKEN_Latent_Effect) + tokens[1]);
 			updated = true;
 		}
 		tokens = tokens[0].split(Dao.getString(FFXIString.TOKEN_Assult));
 		if (tokens.length > 1) {
 			mUnknownTokens.addString(Dao.getString(FFXIString.TOKEN_Assult) + tokens[1]);
 			updated = true;
 		}
 		tokens = tokens[0].split(Dao.getString(FFXIString.TOKEN_Besieged));
 		if (tokens.length > 1) {
 			mUnknownTokens.addString(Dao.getString(FFXIString.TOKEN_Besieged) + tokens[1]);
 			updated = true;
 		}
 		tokens = tokens[0].split(Dao.getString(FFXIString.TOKEN_Campaign));
 		if (tokens.length > 1) {
 			mUnknownTokens.addString(Dao.getString(FFXIString.TOKEN_Campaign) + tokens[1]);
 			updated = true;
 		}
 		tokens = tokens[0].split(Dao.getString(FFXIString.TOKEN_Dynamis));
 		if (tokens.length > 1) {
 			mUnknownTokens.addString(Dao.getString(FFXIString.TOKEN_Dynamis) + tokens[1]);
 			updated = true;
 		}
 		tokens = tokens[0].split(Dao.getString(FFXIString.TOKEN_SetBonus));
 		if (tokens.length > 1) {
 			mUnknownTokens.addString(Dao.getString(FFXIString.TOKEN_SetBonus) + tokens[1]);
 			updated = true;
 		}
 		tokens = tokens[0].split(Dao.getString(FFXIString.TOKEN_Pet));
 		if (tokens.length > 1) {
 			mUnknownTokens.addString(Dao.getString(FFXIString.TOKEN_Pet) + tokens[1]);
 			updated = true;
 		}
 		tokens = tokens[0].split(Dao.getString(FFXIString.TOKEN_Aftermath));
 		if (tokens.length > 1) {
 			mUnknownTokens.addString(Dao.getString(FFXIString.TOKEN_Aftermath) + tokens[1]);
 			updated = true;
 		}
 		tokens = tokens[0].split(Dao.getString(FFXIString.ItemDescriptionTokenSeparator));
 		for (int i = 0; i < tokens.length; i++) {
			String token, parameter;
 			DescriptionTokenHandler handler;
 			
			token = (tokens[i].split("[\\+\\-0-9]"))[0];
			parameter = tokens[i].substring(token.length());
 
 			handler = fTokenHandler.get(token);
 			if (handler != null) {
 				if (handler.handleToken(token, parameter)) {
 					updated = true;
 				} else {
 					mUnknownTokens.addString(tokens[i]);
 					updated = true;
 				}
 			} else {
 				mUnknownTokens.addString(tokens[i]);
 				updated = true;
 			}
 		}
 		return updated;
 	}
 	
 	public String canonicalizeDescription(String string) {
 		StringBuilder newString = new StringBuilder();
 		if (string == null) {
 			return "";
 		}
 		String tmpString = new String(string);
 		// TODO Convert SPC to UNDERLINE for some multi-word token.  (for English/German version?)
 		
 		// Canonicalize some error characters in database
 		boolean skipwhite = false;
 		
 		String from = Dao.getString(FFXIString.ItemDescriptionConvertCharsFrom);
 		String to = Dao.getString(FFXIString.ItemDescriptionConvertCharsTo);
 		boolean convert = (from.length() > 0
 						   && from.length() == to.length());
 		for (int i =0; i < tmpString.length(); i++) {
 			char ch;
 			int index;
 			
 			ch = tmpString.charAt(i);
 			if (convert) {
 				index = from.indexOf(ch);
 				if (index >= 0) {
 					ch = to.charAt(index);
 				}
 			}
 			if (Character.isWhitespace(ch)) {
 				if (skipwhite)
 					continue;
 				ch = ' ';
 				skipwhite = true;
 			} else {
 				skipwhite = false;
 			}
 			newString.append(ch);
 		}
 
 		return newString.toString();
 	}
 	
 	protected StatusValue handleCommonToken(StatusValue base, String parameter) {
 		StatusValue newValue;
 
 		newValue = StatusValue.valueOf(parameter);
 		if (newValue != null) {
 			newValue.add(base);
 		}
 		
 		return newValue;
 	}
 	
 	private void setupTokenHandler() {
 		if (fTokenHandler != null) {
 			return;
 		}
 		fTokenHandler = new Hashtable<String, DescriptionTokenHandler>();
 		setupCommonTokenHandler(FFXIString.TOKEN_STR, StatusType.STR);
 		setupCommonTokenHandler(FFXIString.TOKEN_VIT, StatusType.VIT);
 		setupCommonTokenHandler(FFXIString.TOKEN_DEX, StatusType.DEX);
 		setupCommonTokenHandler(FFXIString.TOKEN_AGI, StatusType.AGI);
 		setupCommonTokenHandler(FFXIString.TOKEN_INT, StatusType.INT);
 		setupCommonTokenHandler(FFXIString.TOKEN_MND, StatusType.MND);
 		setupCommonTokenHandler(FFXIString.TOKEN_CHR, StatusType.CHR);
 		setupCommonTokenHandler(FFXIString.TOKEN_HP, StatusType.HP);
 		setupCommonTokenHandler(FFXIString.TOKEN_MP, StatusType.MP);
 		setupCommonTokenHandler(FFXIString.TOKEN_DMP, StatusType.D);
 		setupCommonTokenHandler(FFXIString.TOKEN_DELAY, StatusType.Delay);
 		setupCommonTokenHandler(FFXIString.TOKEN_ATTACK, StatusType.Attack);
 		setupCommonTokenHandler(FFXIString.TOKEN_ATTACK_RANGE, StatusType.AttackRange);
 		setupCommonTokenHandler(FFXIString.TOKEN_DEF, StatusType.Defence);
 		setupCommonTokenHandler(FFXIString.TOKEN_HASTE, StatusType.Haste);
 		setupCommonTokenHandler(FFXIString.TOKEN_SLOW, StatusType.Slow);
 		setupCommonTokenHandler(FFXIString.TOKEN_SUBTLE_BLOW, StatusType.SubtleBlow);
 		setupCommonTokenHandler(FFXIString.TOKEN_STORE_TP, StatusType.StoreTP);
 		setupCommonTokenHandler(FFXIString.TOKEN_EVASION, StatusType.Evasion);
 		setupCommonTokenHandler(FFXIString.TOKEN_ACCURACY, StatusType.Accuracy);
 		setupCommonTokenHandler(FFXIString.TOKEN_ACCURACY_RANGE, StatusType.AccuracyRange);
 		setupCommonTokenHandler(FFXIString.TOKEN_DOUBLE_ATTACK, StatusType.DoubleAttack);
 		setupCommonTokenHandler(FFXIString.TOKEN_TRIPPLE_ATTACK, StatusType.TrippleAttack);
 		setupCommonTokenHandler(FFXIString.TOKEN_QUAD_ATTACK, StatusType.QuadAttack);
 		setupCommonTokenHandler(FFXIString.TOKEN_ENMITY, StatusType.Enmity);
 		setupCommonTokenHandler(FFXIString.TOKEN_CRITICAL_RATE, StatusType.CriticalRate);
 		setupCommonTokenHandler(FFXIString.TOKEN_CRITICAL_DAMAGE, StatusType.CriticalDamage);
 		setupCommonTokenHandler(FFXIString.TOKEN_ACCURACY_MAGIC, StatusType.AccuracyMagic);
 		setupCommonTokenHandler(FFXIString.TOKEN_ATTACK_MAGIC, StatusType.AttackMagic);
 		setupCommonTokenHandler(FFXIString.TOKEN_ATTACK_MAGIC2, StatusType.AttackMagic);
 		setupCommonTokenHandler(FFXIString.TOKEN_DEFENCE, StatusType.Defence);
 		setupCommonTokenHandler(FFXIString.TOKEN_DEFENCE_MAGIC, StatusType.DefenceMagic);
 		setupCommonTokenHandler(FFXIString.TOKEN_DEFENCE_MAGIC2, StatusType.DefenceMagic);
 		setupCommonTokenHandler(FFXIString.TOKEN_DAMAGECUT, StatusType.DamageCut);
 		setupCommonTokenHandler(FFXIString.TOKEN_DAMAGECUT_PHYSICAL, StatusType.DamageCutPhysical);
 		setupCommonTokenHandler(FFXIString.TOKEN_DAMAGECUT_MAGIC, StatusType.DamageCutMagic);
 		setupCommonTokenHandler(FFXIString.TOKEN_DAMAGECUT_BREATH, StatusType.DamageCutBreath);
 		setupCommonTokenHandler(FFXIString.TOKEN_DUALWIELD, StatusType.DualWield);
 		setupCommonTokenHandler(FFXIString.TOKEN_DUALWIELDUP, StatusType.DualWield);
 		setupCommonTokenHandler(FFXIString.TOKEN_MARTIALARTS, StatusType.MartialArts);
 
 		setupCommonTokenHandler(FFXIString.TOKEN_REGIST_FIRE, StatusType.Regist_Fire);
 		setupCommonTokenHandler(FFXIString.TOKEN_REGIST_ICE, StatusType.Regist_Ice);
 		setupCommonTokenHandler(FFXIString.TOKEN_REGIST_WIND, StatusType.Regist_Wind);
 		setupCommonTokenHandler(FFXIString.TOKEN_REGIST_EARTH, StatusType.Regist_Earth);
 		setupCommonTokenHandler(FFXIString.TOKEN_REGIST_LIGHTNING, StatusType.Regist_Lightning);
 		setupCommonTokenHandler(FFXIString.TOKEN_REGIST_WATER, StatusType.Regist_Water);
 		setupCommonTokenHandler(FFXIString.TOKEN_REGIST_LIGHT, StatusType.Regist_Light);
 		setupCommonTokenHandler(FFXIString.TOKEN_REGIST_DARK, StatusType.Regist_Dark);
 
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_HANDTOHAND, StatusType.SKILL_HANDTOHAND);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_DAGGER, StatusType.SKILL_DAGGER);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_SWORD, StatusType.SKILL_SWORD);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_GREATSWORD, StatusType.SKILL_GREATSWORD);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_AXE, StatusType.SKILL_AXE);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_GREATEAXE, StatusType.SKILL_GREATAXE);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_SCYTH, StatusType.SKILL_SCYTH);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_POLEARM, StatusType.SKILL_POLEARM);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_KATANA, StatusType.SKILL_KATANA);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_GREATKATANA, StatusType.SKILL_GREATKATANA);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_CLUB, StatusType.SKILL_CLUB);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_STAFF, StatusType.SKILL_STAFF);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_ARCHERY, StatusType.SKILL_ARCHERY);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_MARKSMANSHIP, StatusType.SKILL_MARKSMANSHIP);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_THROWING, StatusType.SKILL_THROWING);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_GUARDING, StatusType.SKILL_GUARDING);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_EVASION, StatusType.SKILL_EVASION);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_SHIELD, StatusType.SKILL_SHIELD);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_PARRYING, StatusType.SKILL_PARRYING);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_DIVINE_MAGIC, StatusType.SKILL_DIVINE_MAGIC);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_HEALING_MAGIC, StatusType.SKILL_HEALING_MAGIC);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_ENCHANCING_MAGIC, StatusType.SKILL_ENCHANCING_MAGIC);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_ENFEEBLING_MAGIC, StatusType.SKILL_ENFEEBLING_MAGIC);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_ELEMENTAL_MAGIC, StatusType.SKILL_ELEMENTAL_MAGIC);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_DARK_MAGIC, StatusType.SKILL_DARK_MAGIC);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_SINGING, StatusType.SKILL_SINGING);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_STRING_INSTRUMENT, StatusType.SKILL_STRING_INSTRUMENT);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_WIND_INSTRUMENT, StatusType.SKILL_WIND_INSTRUMENT);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_NINJUTSU, StatusType.SKILL_NINJUTSU);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_SUMMONING, StatusType.SKILL_SUMMONING);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_BLUE_MAGIC, StatusType.SKILL_BLUE_MAGIC);
 		
 	}
 	
 	public void setupCommonTokenHandler(int token, final StatusType type) {
 		fTokenHandler.put(Dao.getString(token), new DescriptionTokenHandler() {
 			boolean handleToken(String token, String parameter) {
 				StatusValue v = handleCommonToken(getStatus(type), parameter);
 				if (v != null) {
 					setStatus(v, type);
 					return true;
 				}
 				return false;
 			}
 		});
 	}
 
 
 	public SortedStringList getUnknownTokens() {
 		return mUnknownTokens;
 	}
 }
