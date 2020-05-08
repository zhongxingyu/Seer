 package com.dmtprogramming.pathfindercombat;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 public class CharacterActivity extends Activity {
 	
 	private static final String TAG = "PFCombat:CharacterActivity";
 	private PFCharacterDataSource datasource;
 	private PFCharacter _char;
 	private List<CharacterModifier> _mods;
	private CharacterModifier _smite;
 		
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.v(TAG, String.format("test"));
         setContentView(R.layout.character);
         
         datasource = new PFCharacterDataSource(this);
         datasource.open();
         
         _char = null;
         
         Bundle extras = getIntent().getExtras();
         if (extras != null) {
         	long _id = extras.getLong("CHARACTER_ID");
         	if (_id > 0) {
         		_char = datasource.findCharacter(_id);
         	}
         }
         
         if (_char != null) {
         	Log.d(TAG, "loaded character with id = " + _char.getId());
         }
     
         setupToggles();
         populateStats("");
 
         setupTriggers();
     }
     
     public void populateStats(String f) {
     	Log.d(TAG, "populateStats(" + f + ")");
     	
     	// same some typing
     	PFCharacter c = _char;
     	
     	populate(R.id.txtCharacter, f, "name", c.getName());
     	populate(R.id.txtPlayer, f, "player", c.getPlayer());
     	populate(R.id.txtLevel, f, "level", String.valueOf(c.getLevel()));
     	populate(R.id.txtStr, f, "str", String.valueOf(c.getStr()));
     	populate(R.id.txtDex, f, "dex", String.valueOf(c.getDex()));
     	populate(R.id.txtCon, f, "con", String.valueOf(c.getCon()));
     	populate(R.id.txtInt, f, "int", String.valueOf(c.getInt()));
     	populate(R.id.txtWis, f, "wis", String.valueOf(c.getWis()));
     	populate(R.id.txtCha, f, "cha", String.valueOf(c.getCha()));
     	populate(R.id.txtStrMod, f, "strmod", String.valueOf(c.getStrMod()));
     	populate(R.id.txtDexMod, f, "dexmod", String.valueOf(c.getDexMod()));
     	populate(R.id.txtConMod, f, "conmod", String.valueOf(c.getConMod()));
     	populate(R.id.txtIntMod, f, "intmod", String.valueOf(c.getIntMod()));
     	populate(R.id.txtWisMod, f, "wismod", String.valueOf(c.getWisMod()));
     	populate(R.id.txtChaMod, f, "chamod", String.valueOf(c.getChaMod()));
     	
     	populate(R.id.txtAttacks, f, "attacks", c.getAttacks());
     	populate(R.id.txtStrModPlusAttack, f, "strmod", String.valueOf(c.getStrMod()));
     	populate(R.id.txtWeaponFocusPlusAttack, f, "weapon_focus_atk", String.valueOf(c.getWeaponFocusMod()));
     	populate(R.id.txtOtherPlusAttack, f, "plus_hit", String.valueOf(0));
     	
     	populate(R.id.txtStrModPlusDamage, f, "strmod", String.valueOf(c.getStrMod()));
     	populate(R.id.txtOtherPlusDamage, f, "plus_damage", String.valueOf(c.getPowerAttackDamage()));
     	
     	int plusHit = 0;
     	plusHit += parseIntField(R.id.txtStrModPlusAttack);
     	plusHit += parseIntField(R.id.txtWeaponPlusAttack);
     	plusHit += parseIntField(R.id.txtWeaponFocusPlusAttack);
     	plusHit += parseIntField(R.id.txtOtherPlusAttack);
     	String attacks = (String) ((TextView) findViewById(R.id.txtAttacks)).getText();
     	calculateFinalPlusHit(attacks, plusHit);
     	
     	int plusDamage = 0;
     	plusDamage += parseIntField(R.id.txtStrModPlusDamage);
     	plusDamage += parseIntField(R.id.txtWeaponPlusDamage);
     	plusDamage += parseIntField(R.id.txtOtherPlusDamage);  
     	String weaponDice = (String) ((TextView) findViewById(R.id.txtWeaponDice)).getText();
     	calculateFinalPlusDamage(weaponDice, plusDamage);
    }
     
     protected int parseIntField(int id) {
     	TextView tv = (TextView) findViewById(id);
     	int i = Integer.parseInt(tv.getText().toString());
     	return i;
     }
     
     protected void calculateFinalPlusHit(String attacks_str, int plusHit) {
     	TextView tv = (TextView) findViewById(R.id.txtPlusAttack);
     	String finalPlusAttack = "";
     	String[] attacks = attacks_str.split("/");
     	for (int i = 0; i < attacks.length; i++) {
     		int hit = Integer.parseInt(attacks[i].trim());
     		if (!finalPlusAttack.equals("")) {
     			finalPlusAttack += " / ";
     		}
     		finalPlusAttack = finalPlusAttack + String.valueOf(hit + plusHit);
     	}
     	tv.setText(finalPlusAttack);
     }
     
     protected void calculateFinalPlusDamage(String weaponDice, int plusDamage) {
     	TextView tv = (TextView) findViewById(R.id.txtPlusDamage);
     	String plusDamageDice = applyToggles("damage_dice", "");
     	if (!plusDamageDice.equals("")) {
     		plusDamageDice = " + " + plusDamageDice;
     	}
     	ToggleButton crit = (ToggleButton) findViewById(R.id.btnCritical);
     	if (crit.isChecked()) {
     		plusDamage *= 2;
     	}
     	tv.setText(weaponDice + " + " + String.valueOf(plusDamage) + plusDamageDice);
     }
     
     protected void populate(int id, String ignore, String field, String value) {
     	if (!ignore.equals(field)) {
     		View v = findViewById(id);
     		String type = v.getClass().getName();
     		if (type.equals("android.widget.EditText")) {
     			EditText e = (EditText) v;
         		e.setText(value);
     		} else if (type.equals("android.widget.TextView")) {
     			TextView e = (TextView) v;
     			e.setText(applyToggles(field, value));
     		}
     	}
     }
     
     protected String applyToggles(String field, String value) {
     	Log.d(TAG, "applyToggles(" + field + ", " + value +") - mods = " + _mods.size());
     	for (int i = 0; i < _mods.size(); i++) {
     		CharacterModifier mod = _mods.get(i);
     		value = mod.apply(field, value);
     	}
     	return value;
     }
     
    protected void updateSmite() {
    	_smite.hit = _char.getChaMod();
    	_smite.damage = _char.getLevel();
    }
    
     protected void setupToggles() {
     	_mods = new ArrayList<CharacterModifier>();
     	// id, +str, +damage dice, +damage, +hit, +size
     	CharacterModifier powerAttack = new CharacterModifier();
     	powerAttack.hit = -2;
     	powerAttack.damage = 4;
     	
    	_smite = new CharacterModifier();
    	updateSmite();
     	
     	CharacterModifier plus2Str = new CharacterModifier();
     	plus2Str.str = 2;
     	
     	CharacterModifier plus4Str = new CharacterModifier();
     	plus4Str.str = 4;
     	
     	CharacterModifier plus1Hit = new CharacterModifier();
     	plus1Hit.hit = 1;
     	
     	CharacterModifier plus2Hit = new CharacterModifier();
     	plus2Hit.hit = 2;
     	
     	CharacterModifier plus4Hit = new CharacterModifier();
     	plus4Hit.hit = 4;
     	
     	CharacterModifier flank = new CharacterModifier();
     	flank.hit = 2;
     	
     	CharacterModifier plus1Damage = new CharacterModifier();
     	plus1Damage.damage = 1;
     	
     	CharacterModifier plus2Damage = new CharacterModifier();
     	plus2Damage.damage = 2;
     	
     	CharacterModifier fightDefensively = new CharacterModifier();
     	fightDefensively.hit = -4;
     	
     	CharacterModifier plus1d6 = new CharacterModifier();
     	plus1d6.damageDice = "1d6";
     	
     	CharacterModifier plus2d6 = new CharacterModifier();
     	plus2d6.damageDice = "2d6";
     	
     	CharacterModifier crit = new CharacterModifier();
     	
     	addToggle(R.id.btnPowerAttack, powerAttack);
    	addToggle(R.id.btnSmite, _smite);
     	addToggle(R.id.btnPlus2Str, plus2Str);
     	addToggle(R.id.btnPlus4Str, plus4Str);
     	addToggle(R.id.btnPlus1Hit, plus1Hit);
     	addToggle(R.id.btnPlus2Hit, plus2Hit);
     	addToggle(R.id.btnPlus4Hit, plus4Hit);
     	addToggle(R.id.btnFlank, flank);
     	addToggle(R.id.btnPlus1Damage, plus1Damage);
     	addToggle(R.id.btnPlus2Damage, plus2Damage);
     	addToggle(R.id.btnFightDefensively, fightDefensively);
     	addToggle(R.id.btnPlus1d6, plus1d6);
     	addToggle(R.id.btnPlus2d6, plus2d6);
     	addToggle(R.id.btnCritical, crit);
     }
     
     protected void addToggle(int id, CharacterModifier mod) {
     	ToggleButton t = (ToggleButton) findViewById(id);
     	t.setOnClickListener(new ToggleClickListener(t, mod, this));
     	_mods.add(mod);
     }
     
     protected void setupTriggers() {
     	setupTrigger(R.id.txtCharacter, "name");
     	setupTrigger(R.id.txtPlayer, "player");
     	setupTrigger(R.id.txtLevel, "level");
     	setupTrigger(R.id.txtStr, "str");
     	setupTrigger(R.id.txtDex, "dex");
     	setupTrigger(R.id.txtCon, "con");
     	setupTrigger(R.id.txtInt, "int");
     	setupTrigger(R.id.txtWis, "wis");
     	setupTrigger(R.id.txtCha, "cha");
     }
     
     protected void setupTrigger(int id, String field) {
     	EditText e = (EditText) findViewById(id);
     	e.addTextChangedListener(new CustomTextWatcher(e, field, this));
     }
     
     public void updateCharacter(String field) {
     	Log.d(TAG, "updateCharacter()");
     	datasource.updatePFCharacter(_char);
    	updateSmite();
     	populateStats(field);

     }
     
     public PFCharacter character() {
     	return _char;
     }
     
 	@Override
 	protected void onResume() {
 		datasource.open();
 		super.onResume();
 	}
 
 	@Override
 	protected void onPause() {
 		datasource.close();
 		super.onPause();
 	}
     
 }
 
 class CustomTextWatcher implements TextWatcher {
 	private static final String TAG = "PFCombat:CustomTextWatcher";
 	private EditText _text;
 	private String _field;
 	private CharacterActivity _activity;
 	
 	public CustomTextWatcher(EditText e, String field, CharacterActivity a) {
 		_text = e;
 		_field = field;
 		_activity = a;
 	}
 	
     public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
     public void onTextChanged(CharSequence s, int start, int before, int count) {}
 
     public void afterTextChanged(Editable s) {
     	String txt = _text.getText().toString();
     	if (txt == null || txt.equals("")) {
     		return;
     	}
     	if (_activity.character().setData(_field, _text.getText().toString())) {
     		Log.d(TAG, _field + " updated successfully, updating the sheet");
     		_activity.updateCharacter(_field);
     	}
     }
 }
 
 class ToggleClickListener implements OnClickListener {
 	//private static final String TAG = "PFCombat:ToggleClickListener";
 	
 	private ToggleButton _toggle;
 	private CharacterModifier _mod;
 	private CharacterActivity _activity;
 	
 	public ToggleClickListener(ToggleButton toggle, CharacterModifier mod, CharacterActivity activity) {
 		_toggle = toggle;
 		_mod = mod;
 		_activity = activity;
 	}
 	
 	public void onClick(View v) {
 		_mod.enabled = _toggle.isChecked();
 		_activity.populateStats("");
 	}
 }
