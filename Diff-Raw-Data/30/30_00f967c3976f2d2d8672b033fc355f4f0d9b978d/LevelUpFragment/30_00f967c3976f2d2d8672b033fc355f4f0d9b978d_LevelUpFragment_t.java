 /**
  * 
  */
 package net.mindsoup.charactersoup.fragments;
 
 import android.os.Bundle;
 import android.text.Html;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockDialogFragment;
 
 import net.mindsoup.charactersoup.CharacterActivity;
 import net.mindsoup.charactersoup.R;
 import net.mindsoup.charactersoup.pf.PfAttributes;
 import net.mindsoup.charactersoup.pf.PfCharacter;
 import net.mindsoup.charactersoup.pf.PfClasses;
 import net.mindsoup.charactersoup.pf.PfRaces;
 
 /**
  * @author Valentijn
  *
  */
 public class LevelUpFragment extends SherlockDialogFragment {
 	
 	public static String LEVEL = "level";
 	
 	private View view;
 	private PfCharacter character;
 	private int level;
 	private int skillpoints;
 	
 	@Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		Bundle b = this.getArguments();
 		this.level = b.getInt(LEVEL);
 		
 		view = inflater.inflate(R.layout.fragment_levelup, container);
         getDialog().setTitle("Level " + this.level);
         
         character = ((CharacterActivity)this.getActivity()).getCharacter();
         
         TextView text = (TextView)view.findViewById(R.id.levelup_skills_txt);
         skillpoints = character.getPfClass().getBaseSkillRanksPerLevel() + character.getAttributeBonus(character.getIntelligence());
         
         if(character.getRace().getRace() == PfRaces.HUMAN)
         	skillpoints++;
         
         String source = "<b>" + skillpoints + "</b> extra skill ranks";
        if(this.level % 2 == 1 || character.getPfClass().getPfClass() == PfClasses.FIGHTER) {
         	source += "<br><b>1</b> new feat";
         }
 
         int specialPowers = character.getLevelupSpecialPowers(this.level);
         String powerType = "spell(s)";
         if(character.getPfClass().getPfClass() == PfClasses.BARBARIAN) {
             powerType = "rage power";
         }
         if(specialPowers > 0) {
             source += "<br><b>" + specialPowers + "</b> new " + powerType;
         }
 
         text.setText(Html.fromHtml(source));
         
         if(this.level % 4 == 0) {
         	View row = view.findViewById(R.id.levelup_ability_row);
         	row.setVisibility(View.VISIBLE);
         }
         
         text = (TextView)view.findViewById(R.id.levelup_dice_txt);
         text.setText("Hitpoints (" + character.getPfClass().getHitDie().toString() + "):");
         
         Button ok = (Button)view.findViewById(R.id.levelup_button);
         ok.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				try {
 					int hp = Integer.parseInt( ((EditText)view.findViewById(R.id.levelup_dice)).getText().toString() );
                     if(hp > character.getPfClass().getHitDie().getMax()) {
                         Toast.makeText(getActivity(), "Hitpoints too high, roll " + character.getPfClass().getHitDie().toString(), Toast.LENGTH_LONG).show();
                     } else {
                         levelUp();
                     }
 				} catch (NumberFormatException e) {
 					Toast.makeText(getActivity(), "Please fill in your hitpoint roll", Toast.LENGTH_LONG).show();
 				}
 				
 				
 			}
 		});
         
         
         return view;
 	}
 	
 	private void levelUp() {
 		EditText hd = (EditText)view.findViewById(R.id.levelup_dice);
 		
         int hdroll = Integer.parseInt( hd.getText().toString() );
         int feat = this.level % 2;
         int hitpoints = hdroll + character.getAttributeBonus(character.getConstitution());

        if(character.getPfClass().getPfClass() == PfClasses.FIGHTER) {
            feat = feat + ( (this.level + 1) % 2);
        }
         
 		if(((Spinner)this.getView().findViewById(R.id.levelup_perlevel)).getSelectedItemPosition() == 1)
 			skillpoints++;
 		else
 			hitpoints++;
         
         character.setAvailableSpecialPowers(character.getAvailableSpecialPowers() + character.getLevelupSpecialPowers(this.level));
         character.setHitpoints(character.getBaseHitpoints() + hitpoints);
         character.setAvailableFeats(character.getAvailableFeats() + feat);
         character.setAvailableSkillRanks(character.getAvailableSkillRanks() + skillpoints);
         character.setNewLevels(character.getNewLevels() - 1);


         if(this.level % 4 == 0) {
         	int stat = ((Spinner)this.getView().findViewById(R.id.levelup_stat)).getSelectedItemPosition();
         	int value = character.getBaseAttributeValue(PfAttributes.getAttribute(stat));
         	value++;
         	
         	character.setBaseAttributeValue(value, PfAttributes.getAttribute(stat));
         }
         
         this.dismiss();
         ((CharacterActivity)this.getActivity()).updateCharacter(true);
 	}
 
 }
