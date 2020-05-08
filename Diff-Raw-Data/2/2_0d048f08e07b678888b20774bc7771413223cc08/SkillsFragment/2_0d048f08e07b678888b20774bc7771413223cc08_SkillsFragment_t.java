 /**
  * 
  */
 package net.mindsoup.pathfindercharactersheet.fragments;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.mindsoup.pathfindercharactersheet.CharacterActivity;
 import net.mindsoup.pathfindercharactersheet.CharacterSkillAdapter;
 import net.mindsoup.pathfindercharactersheet.R;
 import net.mindsoup.pathfindercharactersheet.pf.PfCharacter;
 import net.mindsoup.pathfindercharactersheet.pf.skills.PfSkill;
 import net.mindsoup.pathfindercharactersheet.pf.skills.PfSkills;
 import net.mindsoup.pathfindercharactersheet.pf.skills.SkillFactory;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 
 /**
  * @author Valentijn
  *
  */
 public class SkillsFragment extends CharacterFragment {
 	
 	private PfCharacter character;
 	private List<PfSkill> skills = new ArrayList<PfSkill>();
 	private CharacterSkillAdapter adapter;
 	private CharacterActivity ca;
 	
 	@Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         // Inflate the layout for this fragment
         return inflater.inflate(R.layout.fragment_skills, container, false);
     }
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		
		if(isAdded() && skills.size() == 0) {
 			
 			character = ((CharacterActivity)this.getActivity()).getCharacter();
 			ca = (CharacterActivity)this.getActivity();
 			
 			for(PfSkills s : PfSkills.values()) {
 				PfSkill skill = SkillFactory.getSkill(s);
 				
 				if(character.getTrainedSkills().containsKey(skill.getType())) {
 					skill.setRank(character.getTrainedSkills().get(skill.getType()).getRank());
 				}
 				
 				skills.add(skill);
 			}
 			
 			ListView list = (ListView)this.getActivity().findViewById(R.id.skills_list);
 			adapter = new CharacterSkillAdapter(this.getActivity(), R.layout.skill_list_item, skills, this.getSherlockActivity());
 			list.setAdapter(adapter);
 			list.setOnItemClickListener(new OnItemClickListener() {
 	
 				@Override
 				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 					skills.get(position).getType();
 					int oldSP = character.getAvailableSkillRanks();
 					int newSP = character.spendSkillRankOnSkill(skills.get(position).getType(), 1);
 					
 					// save to DB if there was a change
 					if(oldSP > newSP)
 						ca.updateCharacter();
 					
 					refresh();			
 				}
 			});
 			
 			refresh();
 		}
 	}
 
 	@Override
 	public void refresh() {
 		PfCharacter ca = ((CharacterActivity)this.getActivity()).getCharacter();
 		TextView tv = (TextView)this.getActivity().findViewById(R.id.available_skill_ranks);
 		int ranks = ca.getAvailableSkillRanks();
 		
 		if(ranks > 0)
 			tv.setVisibility(View.VISIBLE);
 		else
 			tv.setVisibility(View.GONE);
 		
 		tv.setText("Available skill ranks: " + ranks);
 		adapter.notifyDataSetChanged();
 	}
 	
 }
