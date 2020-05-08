 package com.pate.diablo.sectionlist;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.pate.diablo.R;
 import com.pate.diablo.model.Skill;
 import com.pate.diablo.string.Replacer;
 import com.pate.diablo.string.Vars;
 
 
 public class EntrySkill implements Item 
 {
 
     private final Skill skill;
 
 	public EntrySkill(Skill skill) 
 	{
 		this.skill = skill;
 	}
 	
 	public Skill getSkill() 
 	{
 	    return skill;
 	}
 
 	@Override
     public View inflate(Context c, Item i) {
         
         LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         Skill s = ((EntrySkill) i).getSkill();
         
         View v = vi.inflate(R.layout.list_item_skill, null);
 
         final ImageView skillIcon = (ImageView) v.findViewById(R.id.list_skill_icon);
         final TextView skillName = (TextView) v.findViewById(R.id.list_skill_title);
         final TextView skillCost = (TextView) v.findViewById(R.id.list_skill_cost_text);
         final TextView skillGenerates = (TextView) v.findViewById(R.id.list_skill_generates);
         final TextView skillCooldown = (TextView) v.findViewById(R.id.list_skill_cooldown);
         final TextView skillRequiredLevel = (TextView) v.findViewById(R.id.list_skill_required_level);
         final TextView skillDescription = (TextView) v.findViewById(R.id.list_skill_description);
 
         // Is this a terrible hack?! I think so...
                 int skillImage = c.getResources().getIdentifier("drawable/" + s.getIcon(), null, c.getPackageName());
 
                 //@formatter:off
                 skillIcon         .setImageResource(skillImage);
                 skillName         .setText(s.getName());
 
                 if (s.getCostText() == null || s.getCostText().equals(""))
                 {
                     skillCost.setVisibility(View.GONE);
                 }
                 else
                 {
                    skillCost.setText(Replacer.replace(v.getContext().getString(R.string.Cost) + " " + s.getCostText(), "\\d+%?", Vars.DIABLO_GREEN));
                     skillCost.setVisibility(View.VISIBLE);
                 }
 
                 if (s.getGenerateText() == null || s.getGenerateText().equals(""))
                 {
                     skillGenerates.setVisibility(View.GONE);
                 }
                 else
                 {
                    skillGenerates.setText(Replacer.replace(v.getContext().getString(R.string.Generate) + " " + s.getGenerateText(), "\\d+%?", Vars.DIABLO_GREEN));
                     skillGenerates.setVisibility(View.VISIBLE);
                 }
 
                 if (s.getCooldownText() == null || s.getCooldownText().equals(""))
                 {
                     skillCooldown.setText(Replacer.replace(s.getCooldownText(), "\\d+%?", Vars.DIABLO_GREEN));
                     skillCooldown.setVisibility(View.GONE);
                 }
                 else
                 {
                    skillCooldown.setText(Replacer.replace(v.getContext().getString(R.string.Cooldown) + " " + s.getCooldownText(), "\\d+%?", Vars.DIABLO_GREEN));
                     skillCooldown.setVisibility(View.VISIBLE);
                 }
 
                 if (s.getRequiredLevel() == 0)
                 {
                     skillRequiredLevel.setVisibility(View.GONE);
                 }
                 else
                 {
                     skillRequiredLevel.setText(v.getContext().getString(R.string.Required_level) + " " + String.valueOf(s.getRequiredLevel()));
                     skillRequiredLevel.setVisibility(View.VISIBLE);
                 }
 
                 if (s.getDescription() == null || s.getDescription().equals(""))
                 {
                     skillDescription.setVisibility(View.GONE);
                 }
                 else
                 {
                    skillDescription.setText(Replacer.replace(s.getDescription().trim(), "\\d+%?", Vars.DIABLO_GREEN));
                     skillDescription.setVisibility(View.VISIBLE);
                 }   
                 
                 v.setTag(s.getUuid());
         return v;
     }
 	
 }
