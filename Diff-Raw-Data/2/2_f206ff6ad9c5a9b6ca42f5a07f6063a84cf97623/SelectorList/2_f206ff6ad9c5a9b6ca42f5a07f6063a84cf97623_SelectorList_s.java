 package net.cscott.sdr.calls;
 
 
 /** The selector list creates selectors for various formations. */
 public abstract class SelectorList {
     // 0-person selectors
     public static final Selector NONE = new Selector() {
         public FormationMatch match(Formation f) throws NoMatchException {
             throw new NoMatchException("NONE selector used");
         }
     };
     // 2-person selectors
     public static final Selector COUPLE = 
         GeneralFormationMatcher.makeSelector(FormationList.COUPLE);
     public static final Selector FACING_DANCERS =
         GeneralFormationMatcher.makeSelector(FormationList.FACING_DANCERS);
     public static final Selector BACK_TO_BACK_DANCERS =
         GeneralFormationMatcher.makeSelector(FormationList.BACK_TO_BACK_DANCERS);
     public static final Selector TANDEM =
         GeneralFormationMatcher.makeSelector(FormationList.TANDEM);
     public static final Selector RH_MINIWAVE =
         GeneralFormationMatcher.makeSelector(FormationList.RH_MINIWAVE);
     public static final Selector LH_MINIWAVE =
         GeneralFormationMatcher.makeSelector(FormationList.LH_MINIWAVE);
     // 4-person selectors
     public static final Selector GENERAL_LINE =
         GeneralFormationMatcher.makeSelector(FormationList.GENERAL_LINE);
     public static final Selector FACING_COUPLES =
         GeneralFormationMatcher.makeSelector(FormationList.FACING_COUPLES);
     public static final Selector BACK_TO_BACK_COUPLES =
         GeneralFormationMatcher.makeSelector(FormationList.BACK_TO_BACK_COUPLES);
     public static final Selector RH_OCEAN_WAVE =
         GeneralFormationMatcher.makeSelector(FormationList.RH_OCEAN_WAVE);
    public static final Selector LH_OCEA_WAVE =
         GeneralFormationMatcher.makeSelector(FormationList.LH_OCEAN_WAVE);
     public static final Selector RH_BOX =
         GeneralFormationMatcher.makeSelector(FormationList.RH_BOX);
     public static final Selector LH_BOX =
         GeneralFormationMatcher.makeSelector(FormationList.LH_BOX);
     public static final Selector RH_TWO_FACED_LINE =
         GeneralFormationMatcher.makeSelector(FormationList.RH_TWO_FACED_LINE);
     public static final Selector LH_TWO_FACED_LINE =
         GeneralFormationMatcher.makeSelector(FormationList.LH_TWO_FACED_LINE);
     public static final Selector RH_DIAMOND =
         GeneralFormationMatcher.makeSelector(FormationList.RH_DIAMOND);
     public static final Selector RH_FACING_DIAMOND =
         GeneralFormationMatcher.makeSelector(FormationList.RH_FACING_DIAMOND);
     public static final Selector LH_DIAMOND =
         GeneralFormationMatcher.makeSelector(FormationList.LH_DIAMOND);
     public static final Selector LH_FACING_DIAMOND =
         GeneralFormationMatcher.makeSelector(FormationList.LH_FACING_DIAMOND);
     // 8-person selectors
     public static final Selector STATIC_SQUARE =
         GeneralFormationMatcher.makeSelector(FormationList.STATIC_SQUARE);
     public static final Selector PROMENADE =
         GeneralFormationMatcher.makeSelector(FormationList.PROMENADE);
     public static final Selector WRONG_WAY_PROMENADE =
         GeneralFormationMatcher.makeSelector(FormationList.WRONG_WAY_PROMENADE);
     public static final Selector THAR =
         GeneralFormationMatcher.makeSelector(FormationList.THAR);
     public static final Selector WRONG_WAY_THAR =
         GeneralFormationMatcher.makeSelector(FormationList.WRONG_WAY_THAR);
     public static final Selector FACING_LINES =
         GeneralFormationMatcher.makeSelector(FormationList.FACING_LINES);
     public static final Selector EIGHT_CHAIN_THRU =
         GeneralFormationMatcher.makeSelector(FormationList.EIGHT_CHAIN_THRU);
     public static final Selector TRADE_BY =
         GeneralFormationMatcher.makeSelector(FormationList.TRADE_BY);
     public static final Selector DOUBLE_PASS_THRU =
         GeneralFormationMatcher.makeSelector(FormationList.DOUBLE_PASS_THRU);
     public static final Selector COMPLETED_DOUBLE_PASS_THRU =
         GeneralFormationMatcher.makeSelector(FormationList.COMPLETED_DOUBLE_PASS_THRU);
     public static final Selector LINES_FACING_OUT =
         GeneralFormationMatcher.makeSelector(FormationList.LINES_FACING_OUT);
     public static final Selector PARALLEL_RH_WAVES =
         GeneralFormationMatcher.makeSelector(FormationList.PARALLEL_RH_WAVES);
     public static final Selector PARALLEL_LH_WAVES =
         GeneralFormationMatcher.makeSelector(FormationList.PARALLEL_LH_WAVES);
     public static final Selector PARALLEL_RH_TWO_FACED_LINES =
         GeneralFormationMatcher.makeSelector(FormationList.PARALLEL_RH_TWO_FACED_LINES);
     public static final Selector PARALLEL_LH_TWO_FACED_LINES =
         GeneralFormationMatcher.makeSelector(FormationList.PARALLEL_LH_TWO_FACED_LINES);
     public static final Selector RH_COLUMN =
         GeneralFormationMatcher.makeSelector(FormationList.RH_COLUMN);
     public static final Selector LH_COLUMN =
         GeneralFormationMatcher.makeSelector(FormationList.LH_COLUMN);
     public static final Selector ENDS_IN_INVERTED_LINES =
         GeneralFormationMatcher.makeSelector(FormationList.ENDS_IN_INVERTED_LINES);
     public static final Selector ENDS_OUT_INVERTED_LINES =
         GeneralFormationMatcher.makeSelector(FormationList.ENDS_OUT_INVERTED_LINES);
     public static final Selector RH_QUARTER_TAG =
         GeneralFormationMatcher.makeSelector(FormationList.RH_QUARTER_TAG);
     public static final Selector LH_QUARTER_TAG =
         GeneralFormationMatcher.makeSelector(FormationList.LH_QUARTER_TAG);
     public static final Selector RH_THREE_QUARTER_TAG =
         GeneralFormationMatcher.makeSelector(FormationList.RH_THREE_QUARTER_TAG);
     public static final Selector LH_THREE_QUARTER_TAG =
         GeneralFormationMatcher.makeSelector(FormationList.LH_THREE_QUARTER_TAG);
     public static final Selector RH_QUARTER_LINE =
         GeneralFormationMatcher.makeSelector(FormationList.RH_QUARTER_LINE);
     public static final Selector LH_QUARTER_LINE =
         GeneralFormationMatcher.makeSelector(FormationList.LH_QUARTER_LINE);
     public static final Selector RH_TWIN_DIAMONDS =
         GeneralFormationMatcher.makeSelector(FormationList.RH_TWIN_DIAMONDS);
     public static final Selector LH_TWIN_DIAMONDS =
         GeneralFormationMatcher.makeSelector(FormationList.LH_TWIN_DIAMONDS);
     public static final Selector RH_POINT_TO_POINT_DIAMONDS =
         GeneralFormationMatcher.makeSelector(FormationList.RH_POINT_TO_POINT_DIAMONDS);
     public static final Selector LH_POINT_TO_POINT_DIAMONDS =
         GeneralFormationMatcher.makeSelector(FormationList.LH_POINT_TO_POINT_DIAMONDS);
     public static final Selector RH_POINT_TO_POINT_FACING_DIAMONDS =
         GeneralFormationMatcher.makeSelector(FormationList.RH_POINT_TO_POINT_FACING_DIAMONDS);
     public static final Selector LH_POINT_TO_POINT_FACING_DIAMONDS =
         GeneralFormationMatcher.makeSelector(FormationList.LH_POINT_TO_POINT_FACING_DIAMONDS);
     public static final Selector RH_TWIN_FACING_DIAMONDS =
         GeneralFormationMatcher.makeSelector(FormationList.RH_TWIN_FACING_DIAMONDS);
     public static final Selector LH_TWIN_FACING_DIAMONDS =
         GeneralFormationMatcher.makeSelector(FormationList.LH_TWIN_FACING_DIAMONDS);
     public static final Selector RH_TIDAL_WAVE =
         GeneralFormationMatcher.makeSelector(FormationList.RH_TIDAL_WAVE);
     public static final Selector LH_TIDAL_WAVE =
         GeneralFormationMatcher.makeSelector(FormationList.LH_TIDAL_WAVE);
     public static final Selector RH_TIDAL_TWO_FACED_LINE =
         GeneralFormationMatcher.makeSelector(FormationList.RH_TIDAL_TWO_FACED_LINE);
     public static final Selector LH_TIDAL_TWO_FACED_LINE =
         GeneralFormationMatcher.makeSelector(FormationList.LH_TIDAL_TWO_FACED_LINE);
     public static final Selector RH_TIDAL_LINE =
         GeneralFormationMatcher.makeSelector(FormationList.RH_TIDAL_LINE);
     public static final Selector LH_TIDAL_LINE =
         GeneralFormationMatcher.makeSelector(FormationList.LH_TIDAL_LINE);
 
 
     // unimplemented selectors
     private static final Selector _STUB_ = new Selector() {
         @Override
         public FormationMatch match(Formation f) throws NoMatchException {
             assert false : "unimplemented";
             throw new RuntimeException("unimplemented");
         }
         @Override
         public String toString() { return "*STUB*"; }
     };
     public static final Selector SINGLE_DPT = _STUB_;
     public static final Selector GENERAL_BOX = _STUB_;
     public static final Selector LH_3_AND_1 = _STUB_;
     public static final Selector LH_SPLIT_3_AND_1 = _STUB_;
     public static final Selector RH_3_AND_1 = _STUB_;
     public static final Selector RH_SPLIT_3_AND_1 = _STUB_;
     public static final Selector PARALLEL_GENERAL_LINES = _STUB_;
     public static final Selector GENERAL_COLUMNS = _STUB_;
     public static final Selector FACING_TANDEMS = _STUB_;
 }
