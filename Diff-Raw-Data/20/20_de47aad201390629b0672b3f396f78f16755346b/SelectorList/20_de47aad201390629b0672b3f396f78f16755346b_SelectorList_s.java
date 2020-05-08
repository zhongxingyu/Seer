 package net.cscott.sdr.calls;
 
 import static net.cscott.sdr.util.Tools.m;
 import static net.cscott.sdr.util.Tools.p;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import net.cscott.sdr.util.ListUtils;
 
 /**
  * The selector list creates selectors for various formations.
  * It mostly parallels {@link FormationList}, although there are
  * selectors for general formations which are a combination of
  * several formations in the formation list.
  *
  * @doc.test Apply STATIC_SQUARE selector to a SQUARED_SET:
  *  js> SelectorList.STATIC_SQUARE.match(Formation.SQUARED_SET)
  *  AA^
  *  AA:
  *          3Gv  3Bv
  *     
  *     4B>            2G<
  *     
  *     4G>            2B<
  *     
  *          1B^  1G^
  *   [3G: BELLE; 3B: BEAU; 4B: BEAU; 2G: BELLE; 4G: BELLE; 2B: BEAU; 1B: BEAU; 1G: BELLE]
  * @doc.test Apply COUPLE selector to a SQUARED_SET:
  *  js> fm = SelectorList.COUPLE.match(Formation.SQUARED_SET)
  *       AAv
  *  
  *  BB>       CC<
  *  
  *       DD^
  *  AA:
  *     3B^  3G^
  *   [3B: BEAU; 3G: BELLE]
  *  BB:
  *     4B^  4G^
  *   [4B: BEAU; 4G: BELLE]
  *  CC:
  *     2B^  2G^
  *   [2B: BEAU; 2G: BELLE]
  *  DD:
  *     1B^  1G^
  *   [1B: BEAU; 1G: BELLE]
  *  js> fm.matches.size()
  *  4
  *  js> fm.meta.dancers().size()
  *  4
  * @doc.test Apply COUPLE selector to FOUR_SQUARE:
  *  js> fm = SelectorList.COUPLE.match(Formation.FOUR_SQUARE)
  *  AAv
  *  
  *  BB^
  *  AA:
  *     3B^  3G^
  *   [3B: BEAU; 3G: BELLE]
  *  BB:
  *     1B^  1G^
  *   [1B: BEAU; 1G: BELLE]
  * @doc.test Apply FACING_DANCERS selector to FOUR_SQUARE:
  *  js> fm = SelectorList.FACING_DANCERS.match(Formation.FOUR_SQUARE)
  *  AAv  BB^
  *  AA:
  *     1Bv
  *     
  *     3G^
  *   [1B: TRAILER; 3G: TRAILER]
  *  BB:
  *     3Bv
  *     
  *     1G^
  *   [3B: TRAILER; 1G: TRAILER]
  * @doc.test Apply FACING_COUPLES selector to FOUR_SQUARE:
  *  js> fm = SelectorList.FACING_COUPLES.match(Formation.FOUR_SQUARE)
  *  AA^
  *  AA:
  *     3Gv  3Bv
  *     
  *     1B^  1G^
  *   [3G: BELLE,TRAILER; 3B: BEAU,TRAILER; 1B: BEAU,TRAILER; 1G: BELLE,TRAILER]
  * @doc.test Apply RH_MINIWAVE selector to PARALLEL_RH_WAVES:
  *  js> fm = SelectorList.RH_MINIWAVE.match(FormationList.PARALLEL_RH_WAVES)
  *  AA^  BBv
  *  
  *  CC^  DDv
  *  AA:
  *     ^    v
  *   [ph: BEAU; ph: BEAU]
  *  BB:
  *     ^    v
  *   [ph: BEAU; ph: BEAU]
  *  CC:
  *     ^    v
  *   [ph: BEAU; ph: BEAU]
  *  DD:
  *     ^    v
  *   [ph: BEAU; ph: BEAU]
  */
 public abstract class SelectorList {
     // universal selector
     public static final Selector ANY = new Selector() {
         public FormationMatch match(Formation f) throws NoMatchException {
             TaggedFormation tf = TaggedFormation.coerce(f);
             Formation meta = FormationList.SINGLE_DANCER;
 	    Dancer metaDancer = meta.dancers().iterator().next();
 	    return new FormationMatch(meta, m(p(metaDancer, tf)),
 				      Collections.<Dancer>emptySet());
         }
         public String toString() { return "ANY"; }
     };
     // 0-person selectors
     public static final Selector NONE = new Selector() {
         public FormationMatch match(Formation f) throws NoMatchException {
             throw new NoMatchException("NONE", "NONE selector used");
         }
         public String toString() { return "NONE"; }
     };
     // 1-person selectors
     public static final Selector SINGLE_DANCER =
         GeneralFormationMatcher.makeSelector(FormationList.SINGLE_DANCER);
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
     public static final Selector MINIWAVE =
         OR("MINIWAVE", RH_MINIWAVE, LH_MINIWAVE);
     // 4-person selectors
     public static final Selector GENERAL_LINE =
         GeneralFormationMatcher.makeSelector(FormationList.GENERAL_LINE);
     // this selector has an extra underscore because the parser treats it
     // as two items: the number two, and the identifier x2.
     public static final Selector _2_X2 =
         GeneralFormationMatcher.makeSelector(FormationList._2x2);
     public static final Selector FACING_COUPLES =
         GeneralFormationMatcher.makeSelector(FormationList.FACING_COUPLES);
     public static final Selector BACK_TO_BACK_COUPLES =
         GeneralFormationMatcher.makeSelector(FormationList.BACK_TO_BACK_COUPLES);
     public static final Selector TANDEM_COUPLES =
         GeneralFormationMatcher.makeSelector(FormationList.TANDEM_COUPLES);
     public static final Selector RH_OCEAN_WAVE =
         GeneralFormationMatcher.makeSelector(FormationList.RH_OCEAN_WAVE);
     public static final Selector LH_OCEAN_WAVE =
         GeneralFormationMatcher.makeSelector(FormationList.LH_OCEAN_WAVE);
     public static final Selector OCEAN_WAVE =
         OR("OCEAN WAVE", RH_OCEAN_WAVE, LH_OCEAN_WAVE);
     public static final Selector RH_BOX =
         GeneralFormationMatcher.makeSelector(FormationList.RH_BOX);
     public static final Selector LH_BOX =
         GeneralFormationMatcher.makeSelector(FormationList.LH_BOX);
     public static final Selector BOX =
         OR("BOX", RH_BOX, LH_BOX);
     public static final Selector RH_TWO_FACED_LINE =
         GeneralFormationMatcher.makeSelector(FormationList.RH_TWO_FACED_LINE);
     public static final Selector LH_TWO_FACED_LINE =
         GeneralFormationMatcher.makeSelector(FormationList.LH_TWO_FACED_LINE);
     public static final Selector TWO_FACED_LINE =
         OR("TWO-FACED LINE", RH_TWO_FACED_LINE, LH_TWO_FACED_LINE);
     public static final Selector RH_DIAMOND =
         GeneralFormationMatcher.makeSelector(FormationList.RH_DIAMOND);
     public static final Selector RH_FACING_DIAMOND =
         GeneralFormationMatcher.makeSelector(FormationList.RH_FACING_DIAMOND);
     public static final Selector LH_DIAMOND =
         GeneralFormationMatcher.makeSelector(FormationList.LH_DIAMOND);
     public static final Selector LH_FACING_DIAMOND =
         GeneralFormationMatcher.makeSelector(FormationList.LH_FACING_DIAMOND);
     public static final Selector RH_SINGLE_PROMENADE =
         GeneralFormationMatcher.makeSelector(FormationList.RH_SINGLE_PROMENADE);
     public static final Selector LH_SINGLE_PROMENADE =
         GeneralFormationMatcher.makeSelector(FormationList.LH_SINGLE_PROMENADE);
     public static final Selector RH_SINGLE_QUARTER_TAG =
         GeneralFormationMatcher.makeSelector(FormationList.RH_SINGLE_QUARTER_TAG);
     public static final Selector LH_SINGLE_QUARTER_TAG =
         GeneralFormationMatcher.makeSelector(FormationList.LH_SINGLE_QUARTER_TAG);
     public static final Selector SINGLE_QUARTER_TAG =
         OR("SINGLE 1/4 TAG", RH_SINGLE_QUARTER_TAG, LH_SINGLE_QUARTER_TAG);
     public static final Selector RH_SINGLE_THREE_QUARTER_TAG =
         GeneralFormationMatcher.makeSelector(FormationList.RH_SINGLE_THREE_QUARTER_TAG);
     public static final Selector LH_SINGLE_THREE_QUARTER_TAG =
         GeneralFormationMatcher.makeSelector(FormationList.LH_SINGLE_THREE_QUARTER_TAG);
     public static final Selector SINGLE_THREE_QUARTER_TAG =
         OR("SINGLE 3/4 TAG", RH_SINGLE_THREE_QUARTER_TAG, LH_SINGLE_THREE_QUARTER_TAG);
 
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
     public static final Selector SINGLE_DOUBLE_PASS_THRU =
         GeneralFormationMatcher.makeSelector(FormationList.SINGLE_DOUBLE_PASS_THRU);
     public static final Selector COMPLETED_DOUBLE_PASS_THRU =
         GeneralFormationMatcher.makeSelector(FormationList.COMPLETED_DOUBLE_PASS_THRU);
     public static final Selector COMPLETED_SINGLE_DOUBLE_PASS_THRU =
         GeneralFormationMatcher.makeSelector(FormationList.COMPLETED_SINGLE_DOUBLE_PASS_THRU);
     public static final Selector LINES_FACING_OUT =
         GeneralFormationMatcher.makeSelector(FormationList.LINES_FACING_OUT);
     public static final Selector PARALLEL_RH_WAVES =
         GeneralFormationMatcher.makeSelector(FormationList.PARALLEL_RH_WAVES);
     public static final Selector PARALLEL_LH_WAVES =
         GeneralFormationMatcher.makeSelector(FormationList.PARALLEL_LH_WAVES);
     public static final Selector PARALLEL_WAVES =
         OR("PARALLEL WAVES", PARALLEL_RH_WAVES, PARALLEL_LH_WAVES);
     public static final Selector PARALLEL_RH_TWO_FACED_LINES =
         GeneralFormationMatcher.makeSelector(FormationList.PARALLEL_RH_TWO_FACED_LINES);
     public static final Selector PARALLEL_LH_TWO_FACED_LINES =
         GeneralFormationMatcher.makeSelector(FormationList.PARALLEL_LH_TWO_FACED_LINES);
     public static final Selector PARALLEL_TWO_FACED_LINES =
         OR("PARALLEL TWO-FACED LINES", PARALLEL_RH_TWO_FACED_LINES, PARALLEL_LH_TWO_FACED_LINES);
     public static final Selector RH_COLUMN =
         GeneralFormationMatcher.makeSelector(FormationList.RH_COLUMN);
     public static final Selector LH_COLUMN =
         GeneralFormationMatcher.makeSelector(FormationList.LH_COLUMN);
     public static final Selector COLUMN =
         OR("COLUMN", RH_COLUMN, LH_COLUMN);
     public static final Selector ENDS_IN_INVERTED_LINES =
         GeneralFormationMatcher.makeSelector(FormationList.ENDS_IN_INVERTED_LINES);
     public static final Selector ENDS_OUT_INVERTED_LINES =
         GeneralFormationMatcher.makeSelector(FormationList.ENDS_OUT_INVERTED_LINES);
     public static final Selector INVERTED_LINES =
 	OR("INVERTED LINES", ENDS_IN_INVERTED_LINES, ENDS_OUT_INVERTED_LINES);
     public static final Selector RH_QUARTER_TAG =
         GeneralFormationMatcher.makeSelector(FormationList.RH_QUARTER_TAG);
     public static final Selector LH_QUARTER_TAG =
         GeneralFormationMatcher.makeSelector(FormationList.LH_QUARTER_TAG);
     public static final Selector QUARTER_TAG =
         OR("1/4 TAG", RH_QUARTER_TAG, LH_QUARTER_TAG);
     public static final Selector RH_THREE_QUARTER_TAG =
         GeneralFormationMatcher.makeSelector(FormationList.RH_THREE_QUARTER_TAG);
     public static final Selector LH_THREE_QUARTER_TAG =
         GeneralFormationMatcher.makeSelector(FormationList.LH_THREE_QUARTER_TAG);
     public static final Selector THREE_QUARTER_TAG =
         OR("3/4 TAG", RH_THREE_QUARTER_TAG, LH_THREE_QUARTER_TAG);
     public static final Selector RH_QUARTER_LINE =
         GeneralFormationMatcher.makeSelector(FormationList.RH_QUARTER_LINE);
     public static final Selector LH_QUARTER_LINE =
         GeneralFormationMatcher.makeSelector(FormationList.LH_QUARTER_LINE);
     public static final Selector QUARTER_LINE =
         OR("1/4 LINE", RH_QUARTER_LINE, LH_QUARTER_LINE);
     public static final Selector RH_THREE_QUARTER_LINE =
         GeneralFormationMatcher.makeSelector(FormationList.RH_THREE_QUARTER_LINE);
     public static final Selector LH_THREE_QUARTER_LINE =
         GeneralFormationMatcher.makeSelector(FormationList.LH_THREE_QUARTER_LINE);
     public static final Selector THREE_QUARTER_LINE =
         OR("3/4 LINE", RH_THREE_QUARTER_LINE, LH_THREE_QUARTER_LINE);
     public static final Selector RH_TWIN_DIAMONDS =
         GeneralFormationMatcher.makeSelector(FormationList.RH_TWIN_DIAMONDS);
     public static final Selector LH_TWIN_DIAMONDS =
         GeneralFormationMatcher.makeSelector(FormationList.LH_TWIN_DIAMONDS);
     public static final Selector TWIN_DIAMONDS =
         OR("TWIN DIAMONDS", RH_TWIN_DIAMONDS, LH_TWIN_DIAMONDS);
     public static final Selector RH_POINT_TO_POINT_DIAMONDS =
         GeneralFormationMatcher.makeSelector(FormationList.RH_POINT_TO_POINT_DIAMONDS);
     public static final Selector LH_POINT_TO_POINT_DIAMONDS =
         GeneralFormationMatcher.makeSelector(FormationList.LH_POINT_TO_POINT_DIAMONDS);
     public static final Selector POINT_TO_POINT_DIAMONDS =
         OR("POINT-TO-POINT DIAMONDS", RH_POINT_TO_POINT_DIAMONDS, LH_POINT_TO_POINT_DIAMONDS);
     public static final Selector RH_POINT_TO_POINT_FACING_DIAMONDS =
         GeneralFormationMatcher.makeSelector(FormationList.RH_POINT_TO_POINT_FACING_DIAMONDS);
     public static final Selector LH_POINT_TO_POINT_FACING_DIAMONDS =
         GeneralFormationMatcher.makeSelector(FormationList.LH_POINT_TO_POINT_FACING_DIAMONDS);
     public static final Selector POINT_TO_POINT_FACING_DIAMONDS =
         OR("POINT-TO-POINT FACING DIAMONDS", RH_POINT_TO_POINT_FACING_DIAMONDS, LH_POINT_TO_POINT_FACING_DIAMONDS);
     public static final Selector RH_TWIN_FACING_DIAMONDS =
         GeneralFormationMatcher.makeSelector(FormationList.RH_TWIN_FACING_DIAMONDS);
     public static final Selector LH_TWIN_FACING_DIAMONDS =
         GeneralFormationMatcher.makeSelector(FormationList.LH_TWIN_FACING_DIAMONDS);
     public static final Selector TWIN_FACING_DIAMONDS =
         OR("TWIN FACING DIAMONDS", RH_TWIN_FACING_DIAMONDS, LH_TWIN_FACING_DIAMONDS);
     public static final Selector RH_TIDAL_WAVE =
         GeneralFormationMatcher.makeSelector(FormationList.RH_TIDAL_WAVE);
     public static final Selector LH_TIDAL_WAVE =
         GeneralFormationMatcher.makeSelector(FormationList.LH_TIDAL_WAVE);
     public static final Selector TIDAL_WAVE =
         OR("TIDAL WAVE", RH_TIDAL_WAVE, LH_TIDAL_WAVE);
     public static final Selector RH_TIDAL_TWO_FACED_LINE =
         GeneralFormationMatcher.makeSelector(FormationList.RH_TIDAL_TWO_FACED_LINE);
     public static final Selector LH_TIDAL_TWO_FACED_LINE =
         GeneralFormationMatcher.makeSelector(FormationList.LH_TIDAL_TWO_FACED_LINE);
     public static final Selector TIDAL_TWO_FACED_LINE =
         OR("TIDAL TWO-FACED LINE", RH_TIDAL_TWO_FACED_LINE, LH_TIDAL_TWO_FACED_LINE);
     public static final Selector RH_TIDAL_LINE =
         GeneralFormationMatcher.makeSelector(FormationList.RH_TIDAL_LINE);
     public static final Selector LH_TIDAL_LINE =
         GeneralFormationMatcher.makeSelector(FormationList.LH_TIDAL_LINE);
     public static final Selector TIDAL_LINE =
         OR("TIDAL LINE", RH_TIDAL_LINE, LH_TIDAL_LINE);
 
     // selector combinator
     /**
      * The {@link #OR} function creates a Selector which matches any one of
      * the given alternatives.
      * @doc.test Diamonds or quarter tag:
      *  js> sel = SelectorList.OR("OR(RH BOX,RH DIAMOND)", SelectorList.RH_BOX, SelectorList.RH_DIAMOND)
      *  OR(RH BOX,RH DIAMOND)
      *  js> sel.match(FormationList.RH_BOX)                                    
      *  AA^
      *  AA:
      *     ^    v
      *     
      *     ^    v
      *   [ph: BEAU,LEADER; ph: BEAU,TRAILER; ph: BEAU,TRAILER; ph: BEAU,LEADER]
      *  js> sel.match(FormationList.RH_DIAMOND)
      *  AA^
      *  AA:
      *       >
      *     
      *     
      *     ^    v
      *     
      *     
      *       <
      *   [ph: POINT; ph: BEAU,CENTER; ph: BEAU,CENTER; ph: POINT]
      */
     public static Selector OR(final String name, final Selector... alternatives) {
         return new Selector() {
             @Override
             public FormationMatch match(Formation f) throws NoMatchException {
                 List<String> reasons = new ArrayList<String>(3);
                 for (Selector s : alternatives) {
                     try {
                         return s.match(f);
                     } catch (NoMatchException e) {
                         /* try next selector */
                         reasons.add(e.target+"("+e.reason+")");
                     }
                 }
                 // no matches in any selector
                 throw new NoMatchException(name, ListUtils.join(reasons, ", "));
             }
             @Override
             public String toString() { return name; }
             @SuppressWarnings("unused")
             public String repr() {
                 StringBuilder sb = new StringBuilder("OR(");
                 for (int i=0; i<alternatives.length; i++) {
                     sb.append(alternatives[i].toString());
                     if (i+1 < alternatives.length) sb.append(',');
                 }
                 sb.append(')');
                 return sb.toString();
             }
         };
     }
 
     // unimplemented selectors
     /** Stub for not-yet-implemented {@link Selector}s. */
     private static final Selector _STUB_ = new Selector() {
         @Override
         public FormationMatch match(Formation f) throws NoMatchException {
             throw new NoMatchException(f.toString(), "Unimplemented");
         }
         @Override
         public String toString() { return "*STUB*"; }
     };
     public static final Selector LH_3_AND_1 = _STUB_;
     public static final Selector LH_SPLIT_3_AND_1 = _STUB_;
     public static final Selector RH_3_AND_1 = _STUB_;
     public static final Selector RH_SPLIT_3_AND_1 = _STUB_;
     public static final Selector PARALLEL_GENERAL_LINES =
 	// XXX: 3-and-1 lines, t-bones of various kinds
         OR("PARALLEL GENERAL LINES", FACING_LINES, LINES_FACING_OUT, PARALLEL_WAVES,
 	   PARALLEL_TWO_FACED_LINES, INVERTED_LINES);
     public static final Selector GENERAL_COLUMNS =
 	// XXX: magic columns of various kinds, t-bones
 	OR("GENERAL COLUMNS", COLUMN, EIGHT_CHAIN_THRU, TRADE_BY, DOUBLE_PASS_THRU,
 	   COMPLETED_DOUBLE_PASS_THRU);
 }
