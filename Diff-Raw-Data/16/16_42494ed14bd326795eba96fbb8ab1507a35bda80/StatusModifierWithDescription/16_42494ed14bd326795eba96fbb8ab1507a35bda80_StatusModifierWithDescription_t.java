 /*
    Copyright 2011-2013 kanata3249
 
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
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 
 import com.github.kanata3249.ffxi.FFXIString;
 
 public class StatusModifierWithDescription extends StatusModifier {
 	private static final long serialVersionUID = 1L;
 
 	protected String mDescription;
 	
 	protected transient SortedStringList mUnknownTokens;
 	protected transient boolean mNeedParseDescription;
 	private transient Hashtable<String, DescriptionTokenHandler> fTokenHandler;
 	static transient String mConvertCharactersFrom;
 	static transient String mConvertCharactersTo;
 	private transient String mIgnoreSuffix;
 	
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
 	
 	private String []splitTokenToModAndParameter(String token) {
 		String [] result = new String[2];
 		boolean inParameter;
 		int start, end;
 
 		start = end = 0;
 		inParameter = false;
 		result[0] = "";
 		result[1] = "";
 		for (int i = 0; i < token.length(); i++) {
 			char ch = token.charAt(i);
 			
 			if (Character.isDigit(ch) || ch == '+' || ch == '-') {
 				if (inParameter) {
 					end = i + 1;
 				} else {
 					inParameter = true;
 					start = i;
 					end = i + 1;
 				}
 			} else if (ch == '%' || ch == '(' || ch == ')' || ch == '.' || ch == '%') {
 				if (inParameter)
 					end = i + 1;
 			} else {
 				inParameter = false;
 			}
 		}
 		result[1] = token.substring(start, end).trim();
 		result[0] = token.substring(0, start).trim();
 		if (token.length() != end) {
 			if (result[0].length() != 0)
 				result[0] += " ";
 			result[0] += token.substring(end, token.length()).trim();
 		}
 
 		return result;
 	}
 
 	private boolean parseDescriptionToken(String token) {
 		String modAndParam[];
 		DescriptionTokenHandler handler;
 		boolean updated;
 		int modlen;
 
 		updated = false;
 		if (token.length() == 0)
 			return updated;
 		modAndParam = splitTokenToModAndParameter(token);
 		modlen = modAndParam[0].length();
		if (modlen > 0 && modAndParam[0].charAt(0) == '"') {
			modAndParam[0] = modAndParam[0].substring(1, modlen);
			modlen--;
		}
		if (modlen > 0 && modAndParam[0].charAt(modlen - 1) == '"') {
			modAndParam[0] = modAndParam[0].substring(0, modlen - 1);
 		}
 
 		handler = fTokenHandler.get(modAndParam[0]);
 		if (handler != null) {
 			if (handler.handleToken(modAndParam[0], modAndParam[1])) {
 				updated = true;
 			}
 		}
 		return updated;
 	}
 
 	private String [] tokenize(String description) {
 		List<String> tokens = new ArrayList<String>();
 		String cur_token;
 		boolean quoting;
 		int token_len;
 
 		token_len = 0;
 		quoting = false;
 		cur_token = "";
 		for (int i = 0; i < description.length(); i++) {
 			char ch;
 			
 			ch = description.charAt(i);
 			switch (ch) {
 			case '"':
 				quoting = !quoting;
 				/* fall thru */
 			default:
 				if (token_len == 0 && Character.isLowerCase(ch) && tokens.size() != 0) {
 					cur_token = tokens.remove(tokens.size() - 1);
 					cur_token += ' ';
 					token_len = cur_token.length();					
 				}
 				token_len++;
 				cur_token += ch;
 				break;
 			case '\n':
 			case '\r':
 				if (token_len != 0) {
 					tokens.add(cur_token);
 					tokens.add("\n");
 					cur_token = "";
 					token_len = 0;
 				}
				quoting = false;
 				break;
 			case ' ':
 			case '\t':
 				if (quoting) {
 					if (cur_token.charAt(token_len - 1) != ' ') {
 						cur_token += " ";
 						token_len++;
 					}
 				} else {
 					if (token_len != 0) {
 						tokens.add(cur_token);
 						cur_token = "";
 						token_len = 0;
 					}
 					
 				}
 				break;
 			}
 		}
 		if (token_len != 0) {
 			tokens.add(cur_token);
 		}
 		
 		return tokens.toArray(new String[tokens.size()]);
 	}
 
 	public boolean parseDescriptionSub(String description) {
 		boolean updated = false;
 		String tokens[];
 		String pending_unknown_token;
 
 		pending_unknown_token = "";
 		tokens = new String[1];
 		tokens[0] = description;
 		tokens = tokens[0].split(Dao.getString(FFXIString.TOKEN_AugmentComment));
 		if (tokens.length > 1) {
 			mUnknownTokens.addString(Dao.getString(FFXIString.TOKEN_AugmentComment) + tokens[1]);
 			updated = true;
 		}
 		tokens = tokenize(tokens[0]);
 		for (int i = 0; i < tokens.length; i++) {
 			String rebuilt_token;
 			int ii;
 
 			if (tokens[i].charAt(0) == '\n')
 				continue;
 
 			rebuilt_token = "";
 			for (ii = i; ii < tokens.length; ii++) {
 				if (tokens[ii].charAt(0) == '\n')
 					break;
 				rebuilt_token += " " + tokens[ii];
 			}
 			rebuilt_token = rebuilt_token.trim();	// skip first SPC
 			if (rebuilt_token.length() == 0)
 				continue;
 			for (int t = ii - i; t > 0; t--) {
 				if (parseDescriptionToken(rebuilt_token)) {
 					// skip used tokens
 					i += t - 1;
 					updated |= true;
 					if (pending_unknown_token.length() > 0)
 						mUnknownTokens.addString(pending_unknown_token);
 					pending_unknown_token = "";
 					break;
 				} else {
 					if (t == 1) {
 						// all tokens didn't not match
 						if (rebuilt_token.contains(":")) {
 							// skip until next ':'
 							rebuilt_token = tokens[i];
 							for (ii = i + 1; ii < tokens.length; ii++) {
 								int coffset;
 								
 								if (tokens[ii].charAt(0) == '\n') {
 									break;
 								}
 								coffset = tokens[ii].lastIndexOf(":");
 								if (coffset >= 0) {
 									if (coffset < tokens[ii].length() - 1) {
 										char ch = tokens[ii].charAt(coffset + 1);
 										if (Character.isDigit(ch) || ch == '+' || ch == '-') {
 											coffset = -1;
 										}
 									}
 									if (coffset >= 0)
 										break;
 								}
 								i++;
 								rebuilt_token += " " + tokens[ii];
 							}
 						}
 						if (rebuilt_token.contains(":")) {
 							if (pending_unknown_token.length() != 0)
 								mUnknownTokens.addString(pending_unknown_token);
 							pending_unknown_token = rebuilt_token;
 						} else {
 							if (pending_unknown_token.length() != 0)
 								pending_unknown_token += " ";
 							pending_unknown_token += rebuilt_token;
 						}
 						updated = true;
 					} else {
 						// remove last token
 						rebuilt_token = rebuilt_token.substring(0, rebuilt_token.length() - (1 + tokens[t + i - 1].length()));
 					}
 				}
 			}
 		}
 		if (pending_unknown_token.length() > 0)
 			mUnknownTokens.addString(pending_unknown_token);
 		return updated;
 	}
 	
 	public String canonicalizeDescription(String string) {
 		StringBuilder newString = new StringBuilder();
 		if (string == null) {
 			return "";
 		}
 		String tmpString = new String(string);
 		
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
 			if (ch == '\r' || ch == '\n') {
 				if (skipwhite)
 					continue;
 				ch = '\n';
 				skipwhite = true;
 			} else if (Character.isWhitespace(ch)) {
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
 
 		if (mIgnoreSuffix != null && parameter.endsWith(mIgnoreSuffix)) {
 			parameter = parameter.substring(0, parameter.length() - mIgnoreSuffix.length());
 		}
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
 		mIgnoreSuffix = Dao.getString(FFXIString.ItemDescriptionToken_IgnoreSuffix);
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
 		setupCommonTokenHandler(FFXIString.TOKEN_HASTE, StatusType.Haste);
 		setupCommonTokenHandler(FFXIString.TOKEN_HASTE_ABILITY, StatusType.HasteAbility);
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
 		
 		setupCommonTokenHandler(FFXIString.TOKEN_CURE_POTENCY, StatusType.CurePotency);
 		setupCommonTokenHandler(FFXIString.TOKEN_CURE_POTENCY2, StatusType.CurePotency);
 		setupCommonTokenHandler(FFXIString.TOKEN_SONG_SPELL_CASTING_TIME, StatusType.SongSpellCastingTime);
 		setupCommonTokenHandler(FFXIString.TOKEN_SONG_RECAST_DELAY, StatusType.SongRecastDelay);
 		setupCommonTokenHandler(FFXIString.TOKEN_CONSERVE_MP, StatusType.ConserveMP);
 		setupCommonTokenHandler(FFXIString.TOKEN_CONSERVE_TP, StatusType.ConserveTP);
 		setupCommonTokenHandler(FFXIString.TOKEN_HEALING_MP, StatusType.HealingMP);
 		setupCommonTokenHandler(FFXIString.TOKEN_HEALING_HP, StatusType.HealingHP);
 		setupCommonTokenHandler(FFXIString.TOKEN_COUNTER, StatusType.Counter);
 		setupCommonTokenHandler(FFXIString.TOKEN_MAGIC_EVASION, StatusType.MagicEvasion);
 		setupCommonTokenHandler(FFXIString.TOKEN_CRITICAL_DAMAGE_RANGE, StatusType.CriticalDamageRange);
 		setupCommonTokenHandler(FFXIString.TOKEN_CRITICAL_DAMAGE_DEFENCE, StatusType.CriticalDamageDefence);
 		setupCommonTokenHandler(FFXIString.TOKEN_CRITICAL_RATE_DEFENCE, StatusType.CriticalRateDefence);
 		setupCommonTokenHandler(FFXIString.TOKEN_SPELL_INTERRUPTION_RATE, StatusType.SpellInterruptionRate);
 		setupCommonTokenHandler(FFXIString.TOKEN_KICK_ATTACK, StatusType.KickAttack);
 		setupCommonTokenHandler(FFXIString.TOKEN_CONVERT_HP_TO_MP, StatusType.Convert_HP_TO_MP);
 		setupCommonTokenHandler(FFXIString.TOKEN_CONVERT_MP_TO_HP, StatusType.Convert_MP_TO_HP);
 		setupCommonTokenHandler(FFXIString.TOKEN_FASTCAST, StatusType.FastCast);
 		setupCommonTokenHandler(FFXIString.TOKEN_BLOODBOON, StatusType.BloodBoon);
 		setupCommonTokenHandler(FFXIString.TOKEN_AUTOREFRESH, StatusType.AutoRefresh);
 		setupCommonTokenHandler(FFXIString.TOKEN_AUTOREGEN, StatusType.AutoRegen);
 		setupCommonTokenHandler(FFXIString.TOKEN_AVATAR_PERPETUATION_COST, StatusType.AvatarPerpetuationCost);
 		setupCommonTokenHandler(FFXIString.TOKEN_BLOODPACT_ABILITY_DELAY, StatusType.BloodPactAbilityDelay);
 		setupCommonTokenHandler(FFXIString.TOKEN_SAVE_TP, StatusType.SaveTP);
 		setupCommonTokenHandler(FFXIString.TOKEN_LIGHT_ARTS, StatusType.LightArts);
 		setupCommonTokenHandler(FFXIString.TOKEN_DARK_ARTS, StatusType.DarkArts);
 		setupCommonTokenHandler(FFXIString.TOKEN_FIRE_AFFINITY_RECAST, StatusType.FireAffinityRecast);
 		setupCommonTokenHandler(FFXIString.TOKEN_ICE_AFFINITY_RECAST, StatusType.IceAffinityRecast);
 		setupCommonTokenHandler(FFXIString.TOKEN_WIND_AFFINITY_RECAST, StatusType.WindAffinityRecast);
 		setupCommonTokenHandler(FFXIString.TOKEN_EARTH_AFFINITY_RECAST, StatusType.EarthAffinityRecast);
 		setupCommonTokenHandler(FFXIString.TOKEN_LIGHTNING_AFFINITY_RECAST, StatusType.LightningAffinityRecast);
 		setupCommonTokenHandler(FFXIString.TOKEN_WATER_AFFINITY_RECAST, StatusType.WaterAffinityRecast);
 		setupCommonTokenHandler(FFXIString.TOKEN_LIGHT_AFFINITY_RECAST, StatusType.LightAffinityRecast);
 		setupCommonTokenHandler(FFXIString.TOKEN_DARK_AFFINITY_RECAST, StatusType.DarkAffinityRecast);
 
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
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_GREATAXE, StatusType.SKILL_GREATAXE);
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
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_GEOMANCER_MAGIC, StatusType.SKILL_GEOMANCER_MAGIC);
 		setupCommonTokenHandler(FFXIString.TOKEN_SKILL_HANDBELL, StatusType.SKILL_HANDBELL);
 		
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
