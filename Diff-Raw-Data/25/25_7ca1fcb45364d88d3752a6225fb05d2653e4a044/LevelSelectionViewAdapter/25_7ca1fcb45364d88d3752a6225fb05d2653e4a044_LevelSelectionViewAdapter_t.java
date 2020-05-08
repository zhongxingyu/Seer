 package com.kinnack.nthings.model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.kinnack.nthings.R;
 import com.kinnack.nthings.model.level.Level;
 import com.kinnack.nthings.model.level.pushup.InitialEasyLevel;
 import com.kinnack.nthings.model.level.pushup.InitialHardLevel;
 import com.kinnack.nthings.model.level.pushup.InitialMidLevel;
 
 import android.content.Context;
 import android.widget.SimpleAdapter;
 
 public class LevelSelectionViewAdapter extends SimpleAdapter {
     private static final String LEVEL="level";
     
     public LevelSelectionViewAdapter(Context context_, boolean showTest_) {
         super(context_, levelsToViewMap(showTest_), R.layout.level_display, 
                 new String[]{LEVEL},
                 new int[]{R.id.HomeCurrentLevel});
     }
 
     private static List<Map<String,Object>> levelsToViewMap(boolean showTest_) {
         List<Map<String,Object>> levelMap = new ArrayList<Map<String,Object>>();
         levelMap.add(new HashMap<String, Object>(){{
             put(LEVEL, "EASY" );
         }});
         levelMap.add(new HashMap<String, Object>(){{
             put(LEVEL, "MEDIUM" );
         }});
         levelMap.add(new HashMap<String, Object>(){{
             put(LEVEL, "HARD" );
         }});
         if (showTest_) {
             levelMap.add(new HashMap<String,Object>(){{
                 put(LEVEL,"TEST");
             }});
         }
         return levelMap;
     }
     
    public static int getPositionForLevel(Level level_) {
         return level_.getIndex();
     }
     
     public static Level getLevelByPosition(int position_) {
         switch(position_) {
             case 0: 
                 return new InitialEasyLevel();
             case 1:
                 return new InitialMidLevel();
             case 2:
                 return new InitialHardLevel();
         }
         return new InitialEasyLevel();
     }
 }
