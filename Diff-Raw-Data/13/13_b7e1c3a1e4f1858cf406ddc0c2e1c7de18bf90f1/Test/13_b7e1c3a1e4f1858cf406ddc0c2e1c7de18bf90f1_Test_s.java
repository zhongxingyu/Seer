 package com.kinnack.nthings.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.kinnack.nthings.model.level.Level;
 import com.kinnack.nthings.model.level.pushup.InitialEasyLevel;
 import com.kinnack.nthings.model.level.pushup.InitialHardLevel;
 import com.kinnack.nthings.model.level.pushup.InitialMidLevel;
 import com.kinnack.nthings.model.level.pushup.SecondHardLevel;
 import com.kinnack.nthings.model.level.pushup.SecondMidLevel;
 
 public class Test {
     private static final List<Level> ALL_LEVELS = new ArrayList<Level>() {{
         add(new InitialEasyLevel());
         add(new InitialMidLevel());
         add(new InitialHardLevel());
        add(new SecondMidLevel());
         add(new SecondMidLevel());
         add(new SecondHardLevel());
     }};
     
     private static final List<Level> INITIAL_TEST_LIST = new ArrayList<Level>() {{
         add(ALL_LEVELS.get(0));
         add(ALL_LEVELS.get(1));
         add(ALL_LEVELS.get(2));
         add(ALL_LEVELS.get(4));
         add(ALL_LEVELS.get(5));
     }};
     
     public static Level initialTestLevel(int count_) {
         for(Level level : INITIAL_TEST_LIST) {
             if (level.checkLevel(count_)) return level;
         }
         return INITIAL_TEST_LIST.get(0);
     }
     
     public static Level findLevelForWeekByIndex(int week_, int index_) {
         for (Level level : ALL_LEVELS) {
             if (week_ >= level.getStartWeek() && index_ == level.getIndex()) return level;
         }
         return ALL_LEVELS.get(0);
     }
 }
