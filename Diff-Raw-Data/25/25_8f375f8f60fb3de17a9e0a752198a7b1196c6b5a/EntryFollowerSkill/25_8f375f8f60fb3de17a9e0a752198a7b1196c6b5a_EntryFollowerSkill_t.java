 
 package com.wemakestuff.diablo3builder.sectionlist;
 
 import android.os.AsyncTask;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.wemakestuff.diablo3builder.R;
 import com.wemakestuff.diablo3builder.model.Skill;
 import com.wemakestuff.diablo3builder.sectionlist.EntrySkillAdapter.RowType;
 import com.wemakestuff.diablo3builder.string.Replacer;
 import com.wemakestuff.diablo3builder.string.Replacer.D3Color;
 import com.wemakestuff.diablo3builder.util.Util;
 
 public class EntryFollowerSkill implements Item
 {
 
     private final Skill          skill;
     private final String         followerName;
     private boolean              isChecked;
     private ImageView            checkmark;
     private final LayoutInflater inflater;
     ViewHolder                   holder;
 
     public EntryFollowerSkill(LayoutInflater inflater, Skill skill, String followerName, boolean isChecked)
     {
 
         this.skill = skill;
         this.followerName = followerName;
         this.isChecked = isChecked;
         this.inflater = inflater;
 
     }
 
     public Skill getSkill()
     {
 
         return skill;
     }
 
     public void setIsChecked(boolean isChecked)
     {
 
         this.isChecked = isChecked;
        holder.checkmark.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
     }
 
     public boolean isChecked()
     {
 
         return isChecked;
     }
 
     private static class ViewHolder
     {
 
         final ImageView checkmark;
         final ImageView skillIcon;
         final TextView  skillName;
         final TextView  unlockedAt;
         final TextView  skillCooldown;
         final TextView  skillDescription;
 
         public ViewHolder(ImageView checkmark, ImageView skillIcon, TextView skillName, TextView unlockedAt, TextView skillCooldown, TextView skillDescription)
         {
 
             super();
             this.checkmark = checkmark;
             this.skillIcon = skillIcon;
             this.skillName = skillName;
             this.unlockedAt = unlockedAt;
             this.skillCooldown = skillCooldown;
             this.skillDescription = skillDescription;
         }
 
     }
 
     public View getView(View convertView)
     {
 
         View view;
         if (convertView == null)
         {
             ViewGroup v = (ViewGroup) inflater.inflate(R.layout.list_item_follower_skill, null);
 
             //@formatter:off
             holder = new ViewHolder((ImageView) v.findViewById(R.id.follower_checkmark)
                                   , (ImageView) v.findViewById(R.id.follower_skill_icon)
                                   ,  (TextView) v.findViewById(R.id.follower_skill_title)
                                   ,  (TextView) v.findViewById(R.id.follower_skill_unlocked_at)
                                   ,  (TextView) v.findViewById(R.id.follower_skill_cooldown)
                                   ,  (TextView) v.findViewById(R.id.follower_skill_description));
             
             //@formatter:on
             v.setTag(holder);
             view = v;
         }
         else
         {
             view = convertView;
             holder = (ViewHolder) convertView.getTag();
         }
 
         // Is this a terrible hack?! I think so...
         String icon = followerName.toLowerCase() + "_" + skill.getName().replace(" ", "").toLowerCase();
         int skillImage = Util.findImageResource(icon);
        loadIconAsync(holder, skillImage);
         holder.skillName.setText(skill.getName());
         holder.unlockedAt.setText("Unlocked at level: " + skill.getRequiredLevel());
 
         if (skill.getCooldownText() == null || skill.getCooldownText().equals(""))
         {
             holder.skillCooldown.setVisibility(View.GONE);
         }
         else
         {
             if (convertView == null)
                 holder.skillCooldown.setText(view.getContext().getString(R.string.Cooldown) + " " + skill.getCooldownText());
             loadTextAsync(holder, holder.skillCooldown, view.getContext().getString(R.string.Cooldown) + " " + skill.getCooldownText(), "\\d+%?", D3Color.DIABLO_GREEN);
         }
 
         if (skill.getDescription() == null || skill.getDescription().equals(""))
         {
             holder.skillDescription.setVisibility(View.GONE);
         }
         else
         {
             if (convertView == null)
                 holder.skillDescription.setText(skill.getDescription().trim());
             loadTextAsync(holder, holder.skillDescription, skill.getDescription().trim(), "\\d+%?", D3Color.DIABLO_GREEN);
         }
 
        holder.checkmark.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
 
         return view;
     }
 
     @Override
     public int getViewType()
     {
 
         return RowType.EMPTY_FOLLOWER_SKILL.ordinal();
     }
 
     private void loadIconAsync(ViewHolder holder, final int iconImg)
     {
 
         AsyncTask<ViewHolder, Void, Integer> loadImage = new AsyncTask<ViewHolder, Void, Integer>()
         {
 
             private ViewHolder v;
 
             @Override
             protected Integer doInBackground(ViewHolder... params)
             {
 
                 v = params[0];
                 return iconImg;
             }
 
             protected void onPostExecute(Integer result)
             {
 
                 super.onPostExecute(result);
                 v.skillIcon.setImageResource(iconImg);
             }
 
         }.execute(holder);
     }
 
     private void loadTextAsync(ViewHolder holder, final TextView textView, final CharSequence text, final String regEx, final D3Color color)
     {
 
         AsyncTask<ViewHolder, Void, CharSequence> loadText = new AsyncTask<ViewHolder, Void, CharSequence>()
         {
 
             private ViewHolder v;
 
             @Override
             protected CharSequence doInBackground(ViewHolder... params)
             {
 
                 v = params[0];
                 return Replacer.replace(text, regEx, color);
             }
 
             protected void onPostExecute(CharSequence result)
             {
 
                 super.onPostExecute(result);
                 textView.setText(result);
                 textView.setVisibility(View.VISIBLE);
             }
 
         }.execute(holder);
     }
 
 }
