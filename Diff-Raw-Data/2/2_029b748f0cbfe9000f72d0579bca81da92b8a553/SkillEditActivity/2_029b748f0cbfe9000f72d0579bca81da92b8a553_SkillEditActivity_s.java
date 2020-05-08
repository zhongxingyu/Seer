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
 
 import com.github.kanata3249.ffxi.status.StatusType;
 import com.github.kanata3249.ffxieq.JobAndRace;
 import com.github.kanata3249.ffxieq.R;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
 public class SkillEditActivity extends FFXIEQBaseActivity {
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.skilleditor);
 		
 		ControlBindableInteger values[] = (ControlBindableInteger[])getTemporaryValues();
 		
 		bindControlAndValue(R.id.HANDTOHAND, values[StatusType.SKILL_HANDTOHAND.ordinal()]);
 		bindControlAndValue(R.id.DAGGER, values[StatusType.SKILL_DAGGER.ordinal()]);
 		bindControlAndValue(R.id.SWORD, values[StatusType.SKILL_SWORD.ordinal()]);
 		bindControlAndValue(R.id.GREATSWORD, values[StatusType.SKILL_GREATSWORD.ordinal()]);
 		bindControlAndValue(R.id.AXE, values[StatusType.SKILL_AXE.ordinal()]);
 		bindControlAndValue(R.id.GREATAXE, values[StatusType.SKILL_GREATAXE.ordinal()]);
 		bindControlAndValue(R.id.SCYTH, values[StatusType.SKILL_SCYTH.ordinal()]);
 		bindControlAndValue(R.id.POLEARM, values[StatusType.SKILL_POLEARM.ordinal()]);
 		bindControlAndValue(R.id.KATANA, values[StatusType.SKILL_KATANA.ordinal()]);
 		bindControlAndValue(R.id.GREATKATANA, values[StatusType.SKILL_GREATKATANA.ordinal()]);
 		bindControlAndValue(R.id.CLUB, values[StatusType.SKILL_CLUB.ordinal()]);
 		bindControlAndValue(R.id.STAFF, values[StatusType.SKILL_STAFF.ordinal()]);
 		bindControlAndValue(R.id.ARCHERY, values[StatusType.SKILL_ARCHERY.ordinal()]);
 		bindControlAndValue(R.id.MARKSMANSHIP, values[StatusType.SKILL_MARKSMANSHIP.ordinal()]);
 		bindControlAndValue(R.id.THROWING, values[StatusType.SKILL_THROWING.ordinal()]);
 		bindControlAndValue(R.id.GUARDING, values[StatusType.SKILL_GUARDING.ordinal()]);
 		bindControlAndValue(R.id.EVASION, values[StatusType.SKILL_EVASION.ordinal()]);
 		bindControlAndValue(R.id.SHIELD, values[StatusType.SKILL_SHIELD.ordinal()]);
 		bindControlAndValue(R.id.PARRYING, values[StatusType.SKILL_PARRYING.ordinal()]);
 
 		bindControlAndValue(R.id.DIVINEMAGIC, values[StatusType.SKILL_DIVINE_MAGIC.ordinal()]);
 		bindControlAndValue(R.id.HEALINGMAGIC, values[StatusType.SKILL_HEALING_MAGIC.ordinal()]);
 		bindControlAndValue(R.id.ENCHANCINGMAGIC, values[StatusType.SKILL_ENCHANCING_MAGIC.ordinal()]);
 		bindControlAndValue(R.id.ENFEEBLINGMAGIC, values[StatusType.SKILL_ENFEEBLING_MAGIC.ordinal()]);
 		bindControlAndValue(R.id.ELEMENTALMAGIC, values[StatusType.SKILL_ELEMENTAL_MAGIC.ordinal()]);
 		bindControlAndValue(R.id.DARKMAGIC, values[StatusType.SKILL_DARK_MAGIC.ordinal()]);
 		bindControlAndValue(R.id.SINGING, values[StatusType.SKILL_SINGING.ordinal()]);
 		bindControlAndValue(R.id.STRINGINSTRUMENT, values[StatusType.SKILL_STRING_INSTRUMENT.ordinal()]);
 		bindControlAndValue(R.id.WINDINSTRUMENT, values[StatusType.SKILL_WIND_INSTRUMENT.ordinal()]);
 		bindControlAndValue(R.id.NINJUTSU, values[StatusType.SKILL_NINJUTSU.ordinal()]);
 		bindControlAndValue(R.id.SUMMONING, values[StatusType.SKILL_SUMMONING.ordinal()]);
 		bindControlAndValue(R.id.BLUEMAGIC, values[StatusType.SKILL_BLUE_MAGIC.ordinal()]);
 
 		updateValues();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		saveValues();
 	}
 
 	@Override
 	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_UP){
 			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK){
 				saveValues();
 				
 				{
 					JobAndRace jobandrace = new JobAndRace();
 					JobAndRace oldjobandrace = getFFXICharacter().getJobAndRace();
 					StatusType[] types = StatusType.values();
 					ControlBindableInteger values[] = (ControlBindableInteger[])getTemporaryValues();
 					boolean modified;
 		
 					
 					modified = false;
 					for (int i = 0; i < values.length - 1; i++) {
 						if (oldjobandrace.getSkill(types[i]) != values[i].getIntValue())
 							modified = true;
 						jobandrace.setSkill(types[i], values[i].getIntValue());
 					}
 					
 					if (modified)
 						getFFXICharacter().setJobAndRace(jobandrace);
 				}
 				
 				Intent result = new Intent();
 				
 				result.putExtra("From", "SkillEdit");
 				setResult(RESULT_OK, result);
 				
 				finish();
 				return true;
 			}
 		}
 		return super.dispatchKeyEvent(event);
 	}
 	
 	static public boolean startActivity(FFXIEQActivity from, int request) {
 		{ // Create temporary copy of skill values
 			ControlBindableInteger values[];
 	
 			JobAndRace jobandrace = from.getFFXICharacter().getJobAndRace();
 			values = new ControlBindableInteger[StatusType.MODIFIER_NUM.ordinal()];
 			StatusType[] types = StatusType.values();
 			for (int i = 0; i < values.length; i++) {
 				values[i] = new ControlBindableInteger(jobandrace.getSkill(types[i]));
 			}
 			from.setTemporaryValues(values);
 		}
 
 		{
 			Intent intent = new Intent(from, SkillEditActivity.class);
 			
 			from.startActivityForResult(intent, request);
 		}
 		return true;
 	}
 
 	static public boolean isComeFrom(Intent data) {
 		return data.getStringExtra("From").equals("MeritPointEdit");
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.skilledit, menu);
 		
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.Revert:
 			{  // Reload from character data.
 				ControlBindableInteger values[];
 		
 				JobAndRace jobandrace = getFFXICharacter().getJobAndRace();
 				values = (ControlBindableInteger[])getTemporaryValues();
 				StatusType[] types = StatusType.values();
 				for (int i = 0; i < values.length; i++) {
 					values[i] = new ControlBindableInteger(jobandrace.getSkill(types[i]));
 				}
 				updateValues();
 			}
 			return true;
 		}
 		return false;
 	}
 }
