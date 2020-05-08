 package com.pate.diablo.sectionlist;
 
 import java.util.UUID;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.pate.diablo.R;
 import com.pate.diablo.model.Rune;
 import com.pate.diablo.string.Replacer;
 import com.pate.diablo.string.Vars;
 
 public class EntryRune implements Item
 {
 
     private final Rune   rune;
     private final String skillName;
     private final UUID   skillUUID;
 
     public EntryRune(Rune rune, String skillName, UUID skillUUID) {
         this.rune = rune;
         this.skillName = skillName;
         this.skillUUID = skillUUID;
     }
 
     public Rune getRune() {
         return rune;
     }
 
     public String getSkillName() {
         return skillName;
     }
 
     public UUID getSkillUUID() {
         return skillUUID;
     }
 
     @Override
     public View inflate(Context c, Item i) {
 
         LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
         Rune r = ((EntryRune) i).getRune();
         View v = vi.inflate(R.layout.list_item_rune, null);
 
         final ImageView runeIcon = (ImageView) v.findViewById(R.id.list_rune_icon);
         final TextView runeTitle = (TextView) v.findViewById(R.id.list_rune_title);
         final TextView runeUnlockedAt = (TextView) v.findViewById(R.id.list_rune_unlocked_at);
         final TextView runeDescription = (TextView) v.findViewById(R.id.list_rune_description);
 
         // Is this a terrible hack?! I think so...
         int runeImage = c.getResources().getIdentifier("drawable/" + r.getIcon(), null, c.getPackageName());
 
         runeIcon.setBackgroundResource(runeImage);
         runeTitle.setText(getRune().getName());
         runeUnlockedAt.setText((v.getContext().getString(R.string.Unlocked_at_level) + " " + r.getRequiredLevel()));
        runeDescription.setText(Replacer.replace(r.getDescription(), "\\d+%?", Vars.DIABLO_GREEN));
 
         v.setTag(r.getUuid());
 
         return v;
     }
 
 }
