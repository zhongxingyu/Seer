 package net.cscott.sdr.calls;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.junit.runner.RunWith;
 
 import net.cscott.jdoctest.JDoctestRunner;
 import net.cscott.jutil.Factories;
 import net.cscott.jutil.GenericMultiMap;
 import net.cscott.jutil.MultiMap;
 import net.cscott.sdr.calls.TaggedFormation.Tag;
 import static net.cscott.sdr.calls.TaggedFormation.Tag.*;
 import net.cscott.sdr.calls.TaggedFormation.TaggedDancerInfo;
 import net.cscott.sdr.calls.grm.BuildGrammars;
 import net.cscott.sdr.util.Fraction;
 import static net.cscott.sdr.util.StringEscapeUtils.escapeJava;
 
 /**
  * Source definitions for {@link FormationList}.  This class defines the
  * formations in an inefficient but "programmer-friendly" way, using
  * automatic tagging, {@link GeneralFormationMatcher},
  * {@link Breather#insert(Formation, Map)} and other aids.  This is good for
  * conciseness, accuracy, and maintainability.  However, it (a) causes the
  * static initialization of this class to be slooow, and (b) creates dependency
  * and testing problems when doctests are defined in terms of operations on
  * formations from the list.  (For example, any bug in breather can cause
  * {@link Breather#insert(Formation, Map)} to fail, which then causes
  * the static initialization of {@link FormationList} to fail, and things
  * start going downhill quickly, making debugging the root cause difficult.)
  * <p>
  * So: what we do is post-process the "slow" formation list into an equivalent
  * "fast" formation list ({@link FormationListFast}) which is what's actually
  * used at runtime (and when trying to debug {@link Breather}!).  The
  * {@link FormationListFast} is stubbed out by simply inheriting from
  * {@link FormationListSlow} when bootstrapping.  Win, win, win!
  * <p>
  * Doctests on properties of "all formations", as well as tests confirming
  * the equivalence of {@link FormationListSlow} and {@link FormationListFast}
  * should appear on the top-level {@link FormationList} class.  This class
  * may have doctests for its helper functions, but not on the formations
  * themselves.
  */
 @RunWith(value=JDoctestRunner.class)
 abstract class FormationListSlow {
 
     // from http://www.penrod-sq-dancing.com/form2.html
     // see also http://www.penrod-sq-dancing.com/fars0.html
 
     // labelled calls named as per the "Callerlab Approved Formations"
     // from April 1980
 
     // 1-person formation
     public static final NamedTaggedFormation SINGLE_DANCER =
         create("SINGLE DANCER",
                 d(0,0,"n"));
     // 2-person formations
     public static final NamedTaggedFormation GENERAL_PARTNERS =
         create("GENERAL PARTNERS", f("||"), WhetherTagger.NO_AUTO_TAGS);
     public static final NamedTaggedFormation GENERAL_TANDEM =
         create("GENERAL TANDEM", f("|","|"), WhetherTagger.NO_AUTO_TAGS);
     public static final NamedTaggedFormation _1x2 =
         create("1x2", f("++"), WhetherTagger.NO_AUTO_TAGS);
     public static final NamedTaggedFormation COUPLE = // callerlab #1
         create("COUPLE",
                 d(-1,0,"n",BEAU),
                 d(+1,0,"n",BELLE));
     public static final NamedTaggedFormation FACING_DANCERS = // callerlab #2
         create("FACING DANCERS",
                 d(0,+1,"s",TRAILER),
                 d(0,-1,"n",TRAILER));
     public static final NamedTaggedFormation BACK_TO_BACK_DANCERS = // callerlab #3
         create("BACK TO BACK DANCERS",
                 d(0,+1,"n",LEADER),
                 d(0,-1,"s",LEADER));
     public static final NamedTaggedFormation TANDEM =
         create("TANDEM",
                d(0,+1,"n",LEADER),
                d(0,-1,"n",TRAILER));
     public static final NamedTaggedFormation RH_MINIWAVE = // callerlab #4
         create("RH MINIWAVE",
                d(-1,0,"n",BEAU),
                d(+1,0,"s",BEAU));
     public static final NamedTaggedFormation LH_MINIWAVE = // callerlab #5
         create("LH MINIWAVE",
                d(-1,0,"s",BELLE),
                d(+1,0,"n",BELLE));
     // 4-person formations
     public static final NamedTaggedFormation GENERAL_LINE =
         create("GENERAL LINE", f("||||"), WhetherTagger.NO_AUTO_TAGS,
                 t(0, END), t(1,CENTER), t(2,CENTER), t(3,END));
     public static final NamedTaggedFormation _1x4 =
         create("1x4", f("++++"), WhetherTagger.NO_AUTO_TAGS,
                 t(0, END), t(1,CENTER), t(2,CENTER), t(3,END));
     public static final NamedTaggedFormation _2x2 =
         create("2x2", f("++","++"), WhetherTagger.NO_AUTO_TAGS);
     public static final NamedTaggedFormation SINGLE_STATIC_SQUARE =
         create("SINGLE STATIC SQUARE",
                 d( 0, 2, "s"),
                 d(-2, 0, "e"),
                 d( 0,-2, "n"),
                 d( 2, 0, "w"));
     public static final NamedTaggedFormation FACING_COUPLES = // callerlab #6
         xofy("FACING COUPLES", FACING_DANCERS, COUPLE);
     public static final NamedTaggedFormation BACK_TO_BACK_COUPLES = // callerlab #7
         xofy("BACK TO BACK COUPLES", BACK_TO_BACK_DANCERS, COUPLE);
     public static final NamedTaggedFormation TANDEM_COUPLES =
         xofy("TANDEM COUPLES", TANDEM, COUPLE);
     public static final NamedTaggedFormation RH_OCEAN_WAVE = // callerlab #8
         xofy("RH OCEAN WAVE", COUPLE, RH_MINIWAVE,
                 t(0, END), t(1, CENTER), t(2, CENTER), t(3, END));
     public static final NamedTaggedFormation LH_OCEAN_WAVE = // callerlab #9
         xofy("LH OCEAN WAVE", COUPLE, LH_MINIWAVE,
                 t(0, END), t(1, CENTER), t(2, CENTER), t(3, END));
     public static final NamedTaggedFormation RH_BOX = // callerlab #10
         xofy("RH BOX", RH_MINIWAVE, TANDEM);
     public static final NamedTaggedFormation LH_BOX = // callerlab #11
         xofy("LH BOX", LH_MINIWAVE, TANDEM);
     public static final NamedTaggedFormation INVERTED_BOX =
         create("INVERTED BOX", f("sn","ns"), WhetherTagger.AUTO_TAGS);
     public static final NamedTaggedFormation RH_IN_PINWHEEL =
 	create("RH IN PINWHEEL",
 	       d(-1, +1, "e", BEAU, TRAILER),
 	       d(+1, +1, "s", BEAU, TRAILER),
 	       d(-1, -1, "n", BEAU, TRAILER),
 	       d(+1, -1, "w", BEAU, TRAILER));
     public static final NamedTaggedFormation LH_IN_PINWHEEL =
 	create("LH IN PINWHEEL",
 	       d(-1, +1, "s", BELLE, TRAILER),
 	       d(+1, +1, "w", BELLE, TRAILER),
 	       d(-1, -1, "e", BELLE, TRAILER),
 	       d(+1, -1, "n", BELLE, TRAILER));
     public static final NamedTaggedFormation RH_OUT_PINWHEEL =
 	create("RH OUT PINWHEEL",
 	       d(-1, +1, "n", BEAU, LEADER),
 	       d(+1, +1, "e", BEAU, LEADER),
 	       d(-1, -1, "w", BEAU, LEADER),
 	       d(+1, -1, "s", BEAU, LEADER));
     public static final NamedTaggedFormation LH_OUT_PINWHEEL =
 	create("LH OUT PINWHEEL",
 	       d(-1, +1, "w", BELLE, LEADER),
 	       d(+1, +1, "n", BELLE, LEADER),
 	       d(-1, -1, "s", BELLE, LEADER),
 	       d(+1, -1, "e", BELLE, LEADER));
     public static final NamedTaggedFormation RH_SINGLE_QUARTER_ZEE =
         create("RH SINGLE 1/4 ZEE",
                 d(+1, 2, "s", BEAU, TRAILER),
                 d(-1, 0, "n", BEAU, CENTER, LEADER),
                d(+1, 0, "s", BEAU, CENTER, TRAILER),
                 d(-1,-2, "n", BEAU, TRAILER));
     public static final NamedTaggedFormation LH_SINGLE_QUARTER_ZEE =
         create("LH SINGLE 1/4 ZEE",
                 d(-1, 2, "s", BELLE, TRAILER),
                 d(-1, 0, "s", BELLE, CENTER, LEADER),
                 d(+1, 0, "n", BELLE, CENTER, LEADER),
                 d(+1,-2, "n", BELLE, TRAILER));
     public static final NamedTaggedFormation RH_SINGLE_THREE_QUARTER_ZEE =
         create("RH SINGLE 3/4 ZEE",
                 d(-1, 2, "n", BEAU, LEADER),
                 d(-1, 0, "n", BEAU, CENTER, TRAILER),
                 d(+1, 0, "s", BEAU, CENTER, TRAILER),
                 d(+1,-2, "s", BEAU, LEADER));
     public static final NamedTaggedFormation LH_SINGLE_THREE_QUARTER_ZEE =
         create("LH SINGLE 3/4 ZEE",
                 d(+1, 2, "n", BELLE, LEADER),
                 d(-1, 0, "s", BELLE, CENTER, TRAILER),
                 d(+1, 0, "n", BELLE, CENTER, TRAILER),
                 d(-1,-2, "s", BELLE, LEADER));
     public static final NamedTaggedFormation RH_TWO_FACED_LINE = // callerlab #12
         xofy("RH TWO-FACED LINE", RH_MINIWAVE, COUPLE,
                 t(0, END), t(1, CENTER), t(2, CENTER), t(3, END));
     public static final NamedTaggedFormation LH_TWO_FACED_LINE = // callerlab #13
         xofy("LH TWO-FACED LINE", LH_MINIWAVE, COUPLE,
                 t(0, END), t(1, CENTER), t(2, CENTER), t(3, END));
     public static final NamedTaggedFormation SINGLE_INVERTED_LINE =
         create("SINGLE INVERTED LINE", f("snns"), WhetherTagger.AUTO_TAGS,
                 t(0, END), t(1,CENTER), t(2,CENTER), t(3,END));
     // NOTE that we use Vic Ceder's position of diamond points: at the 2
     // spot, not the 3 spot.  Diamonds belong to a 2x3 formation, not a 2x4
     // This matches the output of our breathing algorithm.
     public static final NamedTaggedFormation GENERAL_DIAMOND =
         create("GENERAL DIAMOND",
                d( 0, 2,"-",POINT),
                d(-1, 0,"|",CENTER),
                d(+1, 0,"|",CENTER),
                d( 0,-2,"-",POINT));
     public static final NamedTaggedFormation RH_DIAMOND =
         create("RH DIAMOND",
                d( 0, 2,"e",POINT),
                d(-1, 0,"n",BEAU,CENTER),
                d(+1, 0,"s",BEAU,CENTER),
                d( 0,-2,"w",POINT));
     public static final NamedTaggedFormation RH_FACING_DIAMOND =
         create("RH FACING DIAMOND",
                d( 0, 2,"w",POINT),
                d(-1, 0,"n",BEAU,CENTER),
                d(+1, 0,"s",BEAU,CENTER),
                d( 0,-2,"e",POINT));
     public static final NamedTaggedFormation LH_DIAMOND =
         create("LH DIAMOND",
                d( 0, 2,"w",POINT),
                d(-1, 0,"s",BELLE,CENTER),
                d(+1, 0,"n",BELLE,CENTER),
                d( 0,-2,"e",POINT));
     public static final NamedTaggedFormation LH_FACING_DIAMOND =
         create("LH FACING DIAMOND",
                d( 0, 2,"e",POINT),
                d(-1, 0,"s",BELLE,CENTER),
                d(+1, 0,"n",BELLE,CENTER),
                d( 0,-2,"w",POINT));
     public static final NamedTaggedFormation RH_STAR =
         create("RH STAR",
                 d( 0, 1, "e"),
                 d(-1, 0, "n"),
                 d( 0,-1, "w"),
                 d( 1, 0, "s"));
     public static final NamedTaggedFormation LH_STAR =
         create("LH STAR",
                 d( 0, 1, "w"),
                 d(-1, 0, "s"),
                 d( 0,-1, "e"),
                 d( 1, 0, "n"));
     public static final NamedTaggedFormation RH_SINGLE_PROMENADE =
         create("RH SINGLE PROMENADE", // not a star in the center
                 d( 0, 2, "e"),
                 d(-2, 0, "n"),
                 d( 0,-2, "w"),
                 d( 2, 0, "s"));
     public static final NamedTaggedFormation LH_SINGLE_PROMENADE =
         create("LH SINGLE PROMENADE", // not a star in the center
                 d( 0, 2, "w"),
                 d(-2, 0, "s"),
                 d( 0,-2, "e"),
                 d( 2, 0, "n"));
     public static final NamedTaggedFormation GENERAL_SINGLE_QUARTER_TAG =
         create("GENERAL SINGLE QUARTER TAG",
                d( 0, 2,"|",END),
                d(-1, 0,"|",CENTER),
                d(+1, 0,"|",CENTER),
                d( 0,-2,"|",END));
     public static final NamedTaggedFormation RH_SINGLE_QUARTER_TAG =
         create("RH SINGLE 1/4 TAG",
                d( 0, 2,"s",END),
                d(-1, 0,"n",BEAU,CENTER),
                d(+1, 0,"s",BEAU,CENTER),
                d( 0,-2,"n",END));
     public static final NamedTaggedFormation LH_SINGLE_QUARTER_TAG =
         create("LH SINGLE 1/4 TAG",
                d( 0, 2,"s",END),
                d(-1, 0,"s",BELLE,CENTER),
                d(+1, 0,"n",BELLE,CENTER),
                d( 0,-2,"n",END));
     public static final NamedTaggedFormation RH_SINGLE_THREE_QUARTER_TAG =
         create("RH SINGLE 3/4 TAG",
                d( 0, 2,"n",END),
                d(-1, 0,"n",BEAU,CENTER),
                d(+1, 0,"s",BEAU,CENTER),
                d( 0,-2,"s",END));
     public static final NamedTaggedFormation LH_SINGLE_THREE_QUARTER_TAG =
         create("LH SINGLE 3/4 TAG",
                d( 0, 2,"n",END),
                d(-1, 0,"s",BELLE,CENTER),
                d(+1, 0,"n",BELLE,CENTER),
                d( 0,-2,"s",END));
     public static final NamedTaggedFormation SINGLE_DOUBLE_PASS_THRU =
         xofy("SINGLE DOUBLE PASS THRU", FACING_DANCERS, TANDEM,
                 t(0,END),t(1,CENTER),t(2,CENTER),t(3,END));
     public static final NamedTaggedFormation COMPLETED_SINGLE_DOUBLE_PASS_THRU =
         xofy("COMPLETED SINGLE DOUBLE PASS THRU", BACK_TO_BACK_DANCERS, TANDEM,
                 t(0,END),t(1,CENTER),t(2,CENTER),t(3,END));
     // 8-person formations. ///////////////////////////////
     public static final NamedTaggedFormation _1x8 =
         create("1x8", f("++++++++"), WhetherTagger.NO_AUTO_TAGS,
                 t(0, OUTSIDE_4), t(1, OUTSIDE_4), t(2, CENTER), t(3, CENTER),
                 t(4, CENTER), t(5, CENTER), t(6, OUTSIDE_4), t(7, OUTSIDE_4));
     public static final NamedTaggedFormation _2x4 =
         create("2x4", f("++++","++++"), WhetherTagger.NO_AUTO_TAGS,
                 t(0, OUTSIDE_4), t(1, CENTER), t(2, CENTER), t(3, OUTSIDE_4),
                 t(4, OUTSIDE_4), t(5, CENTER), t(6, CENTER), t(7, OUTSIDE_4));
     public static final NamedTaggedFormation PARALLEL_GENERAL_LINES =
         create("PARALLEL GENERAL LINES", f("||||","||||"), WhetherTagger.NO_AUTO_TAGS,
                 t(0, OUTSIDE_4), t(1, CENTER), t(2, CENTER), t(3, OUTSIDE_4),
                 t(4, OUTSIDE_4), t(5, CENTER), t(6, CENTER), t(7, OUTSIDE_4));
     public static final NamedTaggedFormation GENERAL_COLUMNS =
         create("GENERAL COLUMNS", f("||","||","||","||"), WhetherTagger.NO_AUTO_TAGS,
                 t(0, OUTSIDE_4), t(1, OUTSIDE_4), t(2, CENTER), t(3, CENTER),
                 t(4, CENTER), t(5, CENTER), t(6, OUTSIDE_4), t(7, OUTSIDE_4));
     public static final NamedTaggedFormation STATIC_SQUARE = // callerlab #14
         create("STATIC SQUARE", f(" ss ","e  w","e  w"," nn "),
                 WhetherTagger.AUTO_TAGS);
     public static final NamedTaggedFormation STATIC_SQUARE_FACING_OUT =
         create("STATIC SQUARE FACING OUT", f(" nn ","w  e","w  e"," ss "),
                 WhetherTagger.AUTO_TAGS);
     // XXX circle, callerlab #15 (we use STATIC SQUARE for this)
     public static final NamedTaggedFormation SINGLE_FILE_PROMENADE = // callerlab #16
         create("SINGLE FILE PROMENADE", f(" ww ","s  n","s  n"," ee "),
                 WhetherTagger.AUTO_TAGS);
     public static final NamedTaggedFormation REVERSE_SINGLE_FILE_PROMENADE =
         create("REVERSE SINGLE FILE PROMENADE", f(" ee ","n  s","n  s"," ww "),
                 WhetherTagger.AUTO_TAGS);
     // callerlab #17, alamo ring -- this is a little sketchy, because we
     // introduce an asymmetry by using squared-set spots.  In reality RH and
     // LH alamo rings are indistinguishable.
     public static final NamedTaggedFormation RH_ALAMO_RING =
         create("RH ALAMO RING", f(" ns ","e  e","w  w"," ns "),
                 WhetherTagger.AUTO_TAGS);
     public static final NamedTaggedFormation LH_ALAMO_RING =
         create("LH ALAMO RING", f(" sn ","w  w","e  e"," sn "),
                 WhetherTagger.AUTO_TAGS);
     public static final NamedTaggedFormation O_SPOTS =
         create("O SPOTS", f(" oo ","o  o","o  o"," oo "),
                 WhetherTagger.NO_AUTO_TAGS);
     public static final NamedTaggedFormation PROMENADE = // callerlab #18
         xofy("PROMENADE", LH_SINGLE_PROMENADE, COUPLE,
                 t(0, END), t(1, CENTER), t(2, END), t(3, CENTER),
                 t(4, CENTER), t(5, END), t(6, CENTER), t(7, END));
     public static final NamedTaggedFormation WRONG_WAY_PROMENADE =
         xofy("WRONG WAY PROMENADE", RH_SINGLE_PROMENADE, COUPLE,
                 t(0, END), t(1, CENTER), t(2, END), t(3, CENTER),
                 t(4, CENTER), t(5, END), t(6, CENTER), t(7, END));
     public static final NamedTaggedFormation STAR_PROMENADE =
         xofy("STAR PROMENADE", LH_STAR, COUPLE,
                 t(0, END), t(1, CENTER), t(2, END), t(3, CENTER),
                 t(4, CENTER), t(5, END), t(6, CENTER), t(7, END));
     public static final NamedTaggedFormation WRONG_WAY_STAR_PROMENADE =
         xofy("WRONG WAY STAR PROMENADE", RH_STAR, COUPLE,
                 t(0, END), t(1, CENTER), t(2, END), t(3, CENTER),
                 t(4, CENTER), t(5, END), t(6, CENTER), t(7, END));
     public static final NamedTaggedFormation THAR =
         xofy("THAR", LH_STAR, LH_MINIWAVE,
                 t(0, END), t(1, CENTER), t(2, END), t(3, CENTER),
                 t(4, CENTER), t(5, END), t(6, CENTER), t(7, END));
     public static final NamedTaggedFormation WRONG_WAY_THAR =
         xofy("WRONG WAY THAR", LH_STAR, RH_MINIWAVE,
                 t(0, END), t(1, CENTER), t(2, END), t(3, CENTER),
                 t(4, CENTER), t(5, END), t(6, CENTER), t(7, END));
     public static final NamedTaggedFormation RIGHT_AND_LEFT_GRAND =
         xofy("RIGHT AND LEFT GRAND", LH_SINGLE_PROMENADE, RH_MINIWAVE,
                 t(0, END), t(1, CENTER), t(2, END), t(3, CENTER),
                 t(4, CENTER), t(5, END), t(6, CENTER), t(7, END));
     public static final NamedTaggedFormation RIGHT_AND_LEFT_GRAND_DIAMOND =
         xofy("RIGHT AND LEFT GRAND DIAMOND", LH_DIAMOND, RH_MINIWAVE,
                 t(0, END, POINT), t(1, CENTER, POINT), t(2, END), t(3, CENTER),
                 t(4, CENTER), t(5, END), t(6, CENTER, POINT), t(7, END, POINT));
     public static final NamedTaggedFormation LEFT_AND_RIGHT_GRAND =
         xofy("LEFT AND RIGHT GRAND", LH_SINGLE_PROMENADE, LH_MINIWAVE,
                 t(0, END), t(1, CENTER), t(2, END), t(3, CENTER),
                 t(4, CENTER), t(5, END), t(6, CENTER), t(7, END));
     public static final NamedTaggedFormation FACING_LINES = // callerlab #22
         xofy("FACING LINES", FACING_COUPLES, COUPLE,
                 t(0, END), t(1, CENTER), t(2, CENTER), t(3, END),
                 t(4, END), t(5, CENTER), t(6, CENTER), t(7, END));
     public static final NamedTaggedFormation EIGHT_CHAIN_THRU = // callerlab #23
         xofy("EIGHT CHAIN THRU", FACING_COUPLES, FACING_DANCERS,
                 t(0,END   ),t(1,END),
                 t(2,CENTER),t(3,CENTER),
                 t(4,CENTER),t(5,CENTER),
                 t(6,END   ),t(7,END));
     public static final NamedTaggedFormation TRADE_BY = // callerlab #24
         xofy("TRADE BY", FACING_COUPLES, BACK_TO_BACK_DANCERS,
                 t(0,END   ),t(1,END),
                 t(2,CENTER),t(3,CENTER),
                 t(4,CENTER),t(5,CENTER),
                 t(6,END   ),t(7,END));
     public static final NamedTaggedFormation DOUBLE_PASS_THRU = // callerlab #25
         xofy("DOUBLE PASS THRU", FACING_COUPLES, TANDEM,
                 t(0,END   ),t(1,END),
                 t(2,CENTER),t(3,CENTER),
                 t(4,CENTER),t(5,CENTER),
                 t(6,END   ),t(7,END));
     public static final NamedTaggedFormation COMPLETED_DOUBLE_PASS_THRU = // callerlab #26
         xofy("COMPLETED DOUBLE PASS THRU", BACK_TO_BACK_COUPLES, TANDEM,
                 t(0,END   ),t(1,END),
                 t(2,CENTER),t(3,CENTER),
                 t(4,CENTER),t(5,CENTER),
                 t(6,END   ),t(7,END));
     public static final NamedTaggedFormation LINES_FACING_OUT = // callerlab #27
         xofy("LINES FACING OUT", BACK_TO_BACK_COUPLES, COUPLE,
                 t(0, END), t(1, CENTER), t(2, CENTER), t(3, END),
                 t(4, END), t(5, CENTER), t(6, CENTER), t(7, END));
     public static final NamedTaggedFormation PARALLEL_RH_WAVES = // callerlab #28(a)
         xofy("PARALLEL RH WAVES", RH_OCEAN_WAVE, TANDEM,
                 t(0, END), t(1, CENTER), t(2, CENTER), t(3, END),
                 t(4, END), t(5, CENTER), t(6, CENTER), t(7, END));
     public static final NamedTaggedFormation PARALLEL_LH_WAVES = // callerlab #28(b)
         xofy("PARALLEL LH WAVES", LH_OCEAN_WAVE, TANDEM,
                 t(0, END), t(1, CENTER), t(2, CENTER), t(3, END),
                 t(4, END), t(5, CENTER), t(6, CENTER), t(7, END));
     public static final NamedTaggedFormation PARALLEL_RH_TWO_FACED_LINES = // callerlab #29(a)
         xofy("PARALLEL RH TWO-FACED LINES", RH_BOX, COUPLE,
                 t(0, END), t(1, CENTER), t(2, CENTER), t(3, END),
                 t(4, END), t(5, CENTER), t(6, CENTER), t(7, END));
     public static final NamedTaggedFormation PARALLEL_LH_TWO_FACED_LINES = // callerlab #29(b)
         xofy("PARALLEL LH TWO-FACED LINES", LH_BOX, COUPLE,
                 t(0, END), t(1, CENTER), t(2, CENTER), t(3, END),
                 t(4, END), t(5, CENTER), t(6, CENTER), t(7, END));
     public static final NamedTaggedFormation RH_COLUMN = // callerlab #30
         xofy("RH COLUMN", RH_BOX, TANDEM,
                 t(0,NUMBER_1,END   ),t(1,NUMBER_4,END),
                 t(2,NUMBER_2,CENTER),t(3,NUMBER_3,CENTER),
                 t(4,NUMBER_3,CENTER),t(5,NUMBER_2,CENTER),
                 t(6,NUMBER_4,END   ),t(7,NUMBER_1,END));
     public static final NamedTaggedFormation LH_COLUMN = // callerlab #31
         xofy("LH COLUMN", LH_BOX, TANDEM,
                 t(0,NUMBER_4,END   ),t(1,NUMBER_1,END),
                 t(2,NUMBER_3,CENTER),t(3,NUMBER_2,CENTER),
                 t(4,NUMBER_2,CENTER),t(5,NUMBER_3,CENTER),
                 t(6,NUMBER_1,END   ),t(7,NUMBER_4,END));
     // XXX 3-and-1 lines, 8 possible, callerlab #32
     public static final NamedTaggedFormation ENDS_IN_INVERTED_LINES = // callerlab #33
         xofy("ENDS IN INVERTED LINES", BACK_TO_BACK_DANCERS, SINGLE_INVERTED_LINE);
     public static final NamedTaggedFormation ENDS_OUT_INVERTED_LINES = // callerlab #34
         xofy("ENDS OUT INVERTED LINES", FACING_DANCERS, SINGLE_INVERTED_LINE);
     // XXX in t-bone lines, callerlab #35
     // XXX out t-bone lines, callerlab #36
     // xxx both diamond-spot quarter tag & compressed quarter tag?
     // xxx do we want all variants here, or just the "canonical" ones?
     public static final NamedTaggedFormation GENERAL_QUARTER_TAG =
         create("GENERAL 1/4 TAG", f(" || ","||||"," || "),
                WhetherTagger.NO_AUTO_TAGS,
                t(0, OUTSIDE_4), t(1, OUTSIDE_4),
                t(2, CENTER), t(3, CENTER), t(4, CENTER), t(5, CENTER),
                t(6, OUTSIDE_4), t(7, OUTSIDE_4));
     public static final NamedTaggedFormation RH_QUARTER_TAG = // callerlab #37(a)
 	_ends_in(xofy("RH 1/4 TAG", RH_MINIWAVE, RH_SINGLE_QUARTER_TAG,
                       WhetherTagger.NO_AUTO_TAGS,
 		      t(0,OUTSIDE_6,CENTER_6,BELLE),
 		      t(1,OUTSIDE_6,CENTER_6,BEAU),
 		      t(2,OUTSIDE_6),t(3,VERY_CENTER,CENTER_6),
 		      t(4,VERY_CENTER,CENTER_6),t(5,OUTSIDE_6),
 		      t(6,OUTSIDE_6,CENTER_6,BEAU),
 		      t(7,OUTSIDE_6,CENTER_6,BELLE)));
     public static final NamedTaggedFormation LH_QUARTER_TAG = // callerlab #37(a)
 	_ends_in(xofy("LH 1/4 TAG", LH_MINIWAVE, LH_SINGLE_QUARTER_TAG,
                       WhetherTagger.NO_AUTO_TAGS,
 		      t(0,OUTSIDE_6,CENTER_6,BELLE),
 		      t(1,OUTSIDE_6,CENTER_6,BEAU),
 		      t(2,OUTSIDE_6),t(3,VERY_CENTER,CENTER_6),
 		      t(4,VERY_CENTER,CENTER_6),t(5,OUTSIDE_6),
 		      t(6,OUTSIDE_6,CENTER_6,BEAU),
 		      t(7,OUTSIDE_6,CENTER_6,BELLE)));
     public static final NamedTaggedFormation RH_THREE_QUARTER_TAG = // callerlab #38(a)
 	_ends_in(xofy("RH 3/4 TAG", RH_MINIWAVE, RH_SINGLE_THREE_QUARTER_TAG,
                       WhetherTagger.NO_AUTO_TAGS,
 		      t(0,OUTSIDE_6,CENTER_6,BEAU),
 		      t(1,OUTSIDE_6,CENTER_6,BELLE),
 		      t(2,OUTSIDE_6),t(3,VERY_CENTER,CENTER_6),
 		      t(4,VERY_CENTER,CENTER_6),t(5,OUTSIDE_6),
 		      t(6,OUTSIDE_6,CENTER_6,BELLE),
 		      t(7,OUTSIDE_6,CENTER_6,BEAU)));
     public static final NamedTaggedFormation LH_THREE_QUARTER_TAG = // callerlab #38(b)
 	_ends_in(xofy("LH 3/4 TAG", LH_MINIWAVE, LH_SINGLE_THREE_QUARTER_TAG,
                       WhetherTagger.NO_AUTO_TAGS,
 		      t(0,OUTSIDE_6,CENTER_6,BEAU),
 		      t(1,OUTSIDE_6,CENTER_6,BELLE),
 		      t(2,OUTSIDE_6),t(3,VERY_CENTER,CENTER_6),
 		      t(4,VERY_CENTER,CENTER_6),t(5,OUTSIDE_6),
 		      t(6,OUTSIDE_6,CENTER_6,BELLE),
 		      t(7,OUTSIDE_6,CENTER_6,BEAU)));
     public static final NamedTaggedFormation RH_QUARTER_LINE = // callerlab #39(a)
 	_ends_in(xofy("RH 1/4 LINE", RH_SINGLE_QUARTER_TAG, COUPLE,
                       WhetherTagger.NO_AUTO_TAGS,
 		      t(0,OUTSIDE_6,CENTER_6),t(1,OUTSIDE_6,CENTER_6),
 		      t(2,OUTSIDE_6),t(3,VERY_CENTER,CENTER_6),
 		      t(4,VERY_CENTER,CENTER_6),t(5,OUTSIDE_6),
 		      t(6,OUTSIDE_6,CENTER_6),t(7,OUTSIDE_6,CENTER_6)));
     public static final NamedTaggedFormation LH_QUARTER_LINE = // callerlab #39(b)
 	_ends_in(xofy("LH 1/4 LINE", LH_SINGLE_QUARTER_TAG, COUPLE,
                       WhetherTagger.NO_AUTO_TAGS,
 		      t(0,OUTSIDE_6,CENTER_6),t(1,OUTSIDE_6,CENTER_6),
 		      t(2,OUTSIDE_6),t(3,VERY_CENTER,CENTER_6),
 		      t(4,VERY_CENTER,CENTER_6),t(5,OUTSIDE_6),
 		      t(6,OUTSIDE_6,CENTER_6),t(7,OUTSIDE_6,CENTER_6)));
     public static final NamedTaggedFormation RH_THREE_QUARTER_LINE = // callerlab #39(a)
 	_ends_in(xofy("RH 3/4 LINE", RH_SINGLE_THREE_QUARTER_TAG, COUPLE,
                       WhetherTagger.NO_AUTO_TAGS,
 		      t(0,OUTSIDE_6,CENTER_6),t(1,OUTSIDE_6,CENTER_6),
 		      t(2,OUTSIDE_6),t(3,VERY_CENTER,CENTER_6),
 		      t(4,VERY_CENTER,CENTER_6),t(5,OUTSIDE_6),
 		      t(6,OUTSIDE_6,CENTER_6),t(7,OUTSIDE_6,CENTER_6)));
     public static final NamedTaggedFormation LH_THREE_QUARTER_LINE = // callerlab #39(b)
 	_ends_in(xofy("LH 3/4 LINE", LH_SINGLE_THREE_QUARTER_TAG, COUPLE,
                       WhetherTagger.NO_AUTO_TAGS,
 		      t(0,OUTSIDE_6,CENTER_6),t(1,OUTSIDE_6,CENTER_6),
 		      t(2,OUTSIDE_6),t(3,VERY_CENTER,CENTER_6),
 		      t(4,VERY_CENTER,CENTER_6),t(5,OUTSIDE_6),
 		      t(6,OUTSIDE_6,CENTER_6),t(7,OUTSIDE_6,CENTER_6)));
     public static final NamedTaggedFormation RH_TWIN_DIAMONDS = // callerlab #40
         xofy("RH TWIN DIAMONDS", COUPLE, RH_DIAMOND);
     public static final NamedTaggedFormation LH_TWIN_DIAMONDS = // callerlab #41
         xofy("LH TWIN DIAMONDS", COUPLE, LH_DIAMOND);
     public static final NamedTaggedFormation RH_POINT_TO_POINT_DIAMONDS = // callerlab #42(a)
         xofy("RH POINT-TO-POINT DIAMONDS", TANDEM, RH_DIAMOND);
     public static final NamedTaggedFormation RH_POINT_TO_POINT_FACING_DIAMONDS = // callerlab #42(b)
         xofy("RH POINT-TO-POINT FACING DIAMONDS", TANDEM, RH_FACING_DIAMOND);
     public static final NamedTaggedFormation LH_POINT_TO_POINT_DIAMONDS = // callerlab #42(c)
         xofy("LH POINT-TO-POINT DIAMONDS", TANDEM, LH_DIAMOND);
     public static final NamedTaggedFormation LH_POINT_TO_POINT_FACING_DIAMONDS = // callerlab #42(d)
         xofy("LH POINT-TO-POINT FACING DIAMONDS", TANDEM, LH_FACING_DIAMOND);
     public static final NamedTaggedFormation RH_TWIN_FACING_DIAMONDS = // callerlab #43
         xofy("RH TWIN FACING DIAMONDS", COUPLE, RH_FACING_DIAMOND);
     public static final NamedTaggedFormation LH_TWIN_FACING_DIAMONDS = // callerlab #44
         xofy("LH TWIN FACING DIAMONDS", COUPLE, LH_FACING_DIAMOND);
     public static final NamedTaggedFormation TWIN_GENERAL_DIAMONDS =
         xofy("TWIN GENERAL DIAMONDS", COUPLE, GENERAL_DIAMOND);
     public static final NamedTaggedFormation POINT_TO_POINT_GENERAL_DIAMONDS =
         xofy("POINT-TO-POINT GENERAL DIAMONDS", TANDEM, GENERAL_DIAMOND);
     public static final NamedTaggedFormation RH_HOURGLASS =
         create("RH HOURGLASS",
                d( 0, 4, "e", CENTER, POINT),
                d(-3, 2, "n", OUTSIDE_4, END),
                d(+3, 2, "s", OUTSIDE_4, END),
                d(-1, 0, "n", BEAU, CENTER, VERY_CENTER),
                d(+1, 0, "s", BEAU, CENTER, VERY_CENTER),
                d(-3,-2, "n", OUTSIDE_4, END),
                d(+3,-2, "s", OUTSIDE_4, END),
                d( 0,-4, "w", CENTER, POINT)); // callerlab #45(a)
     public static final NamedTaggedFormation LH_HOURGLASS =
         create("LH HOURGLASS",
                d( 0, 4, "w", CENTER, POINT),
                d(-3, 2, "s", OUTSIDE_4, END),
                d(+3, 2, "n", OUTSIDE_4, END),
                d(-1, 0, "s", BELLE, CENTER, VERY_CENTER),
                d(+1, 0, "n", BELLE, CENTER, VERY_CENTER),
                d(-3,-2, "s", OUTSIDE_4, END),
                d(+3,-2, "n", OUTSIDE_4, END),
                d( 0,-4, "e", CENTER, POINT)); // callerlab #45(b)
     // XXX left hand Z, callerlab #46
     // XXX right hand Z, callerlab #47
     // XXX right hand stars, callerlab #48
     // XXX left hand stars, callerlab #49
     public static final NamedTaggedFormation RH_TIDAL_WAVE = // callerlab #50(a)
         xofy("RH TIDAL WAVE", COUPLE, RH_OCEAN_WAVE);
     public static final NamedTaggedFormation LH_TIDAL_WAVE = // callerlab #50(b)
         xofy("LH TIDAL WAVE", COUPLE, LH_OCEAN_WAVE);
     public static final NamedTaggedFormation RH_TIDAL_TWO_FACED_LINE = // callerlab #51(a)
         xofy("RH TIDAL TWO-FACED LINE", COUPLE, RH_TWO_FACED_LINE);
     public static final NamedTaggedFormation LH_TIDAL_TWO_FACED_LINE = // callerlab #51(b)
         xofy("LH TIDAL TWO-FACED LINE", COUPLE, LH_TWO_FACED_LINE);
     public static final NamedTaggedFormation RH_TIDAL_LINE = // callerlab #52(a)
         xofy("RH TIDAL LINE", RH_TWO_FACED_LINE, COUPLE);
     public static final NamedTaggedFormation LH_TIDAL_LINE = // callerlab #52(b)
         xofy("LH TIDAL LINE", LH_TWO_FACED_LINE, COUPLE);
     public static final NamedTaggedFormation GENERAL_TIDAL_LINE =
         create("GENERAL TIDAL LINE", f("||||||||"), WhetherTagger.NO_AUTO_TAGS,
                 t(0, OUTSIDE_4), t(1, OUTSIDE_4), t(2, CENTER), t(3, CENTER),
                 t(4, CENTER), t(5, CENTER), t(6, OUTSIDE_4), t(7, OUTSIDE_4));
     public static final NamedTaggedFormation RH_GALAXY =
         create("RH GALAXY",
                d( 0, 4, "e", OUTSIDE_4, POINT),
                d(-1, 2, "n", CENTER, BEAU),
                d(+1, 2, "s", CENTER, BEAU),
                d(-3, 0, "n", OUTSIDE_4, POINT),
                d(+3, 0, "s", OUTSIDE_4, POINT),
                d(-1,-2, "n", CENTER, BEAU),
                d(+1,-2, "s", CENTER, BEAU),
                d( 0,-4, "w", OUTSIDE_4, POINT)); // callerlab #53(a)
     public static final NamedTaggedFormation LH_GALAXY =
         create("LH GALAXY",
                d( 0, 4, "w", OUTSIDE_4, POINT),
                d(-1, 2, "s", CENTER, BELLE),
                d(+1, 2, "n", CENTER, BELLE),
                d(-3, 0, "s", OUTSIDE_4, POINT),
                d(+3, 0, "n", OUTSIDE_4, POINT),
                d(-1,-2, "s", CENTER, BELLE),
                d(+1,-2, "n", CENTER, BELLE),
                d( 0,-4, "e", OUTSIDE_4, POINT)); // callerlab #53(b)
     public static final NamedTaggedFormation O_DOUBLE_PASS_THRU = // used for grand square
         create("O DOUBLE PASS THRU", f(" ss ","s  s","n  n"," nn "),
                 WhetherTagger.AUTO_TAGS,
                 t(0, BELLE, END),    t(1, BEAU, END),
                 t(2, BELLE, CENTER), t(3, BEAU, CENTER),
                 t(4, BEAU, CENTER),  t(5, BELLE, CENTER),
                 t(6, BEAU, END),     t(7, BELLE, END));
     public static final NamedTaggedFormation BUTTERFLY_DOUBLE_PASS_THRU = // used for grand square
         create("BUTTERFLY DOUBLE PASS THRU", f("s  s"," ss "," nn ","n  n"),
                 WhetherTagger.AUTO_TAGS,
                 t(0, BELLE, END),    t(1, BEAU, END),
                 t(2, BELLE, CENTER), t(3, BEAU, CENTER),
                 t(4, BEAU, CENTER),  t(5, BELLE, CENTER),
                 t(6, BEAU, END),     t(7, BELLE, END));
 
     /** List of all formations defined in this class. */
     public static final List<NamedTaggedFormation> all = _enumerateFormations();
     private static List<NamedTaggedFormation> _enumerateFormations() {
         List<NamedTaggedFormation> result = new ArrayList<NamedTaggedFormation>();
         for (Field f: FormationListSlow.class.getDeclaredFields()) {
             if (Modifier.isStatic(f.getModifiers()) &&
                 Modifier.isPublic(f.getModifiers()) &&
                 !f.getName().equals("all")) {
                 try {
                     result.add((NamedTaggedFormation)f.get(null));
                 } catch (Throwable t) { /* ignore */ }
             }
         }
         return Collections.unmodifiableList(result);
     }
     private static class PositionAndTag {
         public final Position position;
         public final Set<Tag> tags;
         PositionAndTag(Position position, Set<Tag> tags) {
             this.position = position; this.tags = tags;
         }
     }
     private static PositionAndTag d(int x, int y, String facing,
                                     Tag... tags) {
         return new PositionAndTag(Position.getGrid
                                   (x, y, Rotation.fromAbsoluteString(facing)),
                                   TaggedFormation.mkTags(tags));
     }
     private static NamedTaggedFormation create(final String name, PositionAndTag... ptl) {
 	List<TaggedDancerInfo> dil = new ArrayList<TaggedDancerInfo>(ptl.length);
 	for (PositionAndTag pt: ptl)
 	    dil.add(new TaggedDancerInfo(new PhantomDancer(), pt.position, pt.tags, true));
 	return new NamedTaggedFormation(name, dil.toArray(new TaggedDancerInfo[dil.size()]));
     }
     // first string is 'top' of diagram (closest to caller)
     // dancers are numbered left to right, top to bottom. (reading order)
     private static NamedTaggedFormation create(String name, String[] sa, WhetherTagger wt, NumAndTags... tags) {
         Map<Dancer,Position> m = new LinkedHashMap<Dancer,Position>();
 	// check validity
 	assert sa.length>0;
 	for (int i=0; i<sa.length-1; i++)
 	    assert sa[i].length()==sa[i+1].length();
 	// okay, create formation w/ phantoms.
 	for (int y=0; y<sa.length; y++)
 	    for (int x=0; x<sa[y].length(); x++) {
 		if (sa[y].charAt(x)==' ') continue;
 		Rotation r =
 		    Rotation.fromAbsoluteString(sa[y].substring(x,x+1));
 		m.put(new PhantomDancer(),
 		        new Position(Fraction.valueOf(x*2),
 		                Fraction.valueOf(y*2).negate(),
 		                r));
 	    }
 	Formation f = new Formation(m).recenter();
         return addTags(name, f, wt, tags);
     }
     // helper
     private static String[] f(String... sa) { return sa; }
 
     /** Formation composition: create a formation with an X of Ys. */
     private static TaggedFormation _xofy(Formation x, TaggedFormation y) {
         /* create an instance of 'y' for each dancer in 'x'. */
         Map<Dancer,Formation> sub = new LinkedHashMap<Dancer,Formation>();
         MultiMap<Dancer,Tag> tags = new GenericMultiMap<Dancer,Tag>
                                         (Factories.enumSetFactory(Tag.class));
         for (Dancer d: x.dancers()) {
             Map<Dancer,Position> m = new LinkedHashMap<Dancer,Position>();
             for (Dancer dd : y.dancers()) {
                 Dancer phantom = new PhantomDancer();
                 m.put(phantom, y.location(dd));
                 tags.addAll(phantom, y.tags(dd));
             }
             sub.put(d, new Formation(m));
         }
         Formation result = Breather.insert(x,sub);
         // Transfer tags from formation y.
         return new TaggedFormation(result, tags);
     }
     /** Helper function for the above to add dancer tags. The dancers
      * are numbered left to right, top to bottom.  A null indicates
      * "no additional tags".
      */
     private static NamedTaggedFormation xofy(final String name, Formation x, TaggedFormation y, NumAndTags... tags){
         return xofy(name, x, y, WhetherTagger.AUTO_TAGS, tags);
     }
     private static NamedTaggedFormation xofy(final String name, Formation x, TaggedFormation y, WhetherTagger wt, NumAndTags... tags){
         return addTags(name, _xofy(x,y), wt, tags);
     }
     private static enum WhetherTagger { AUTO_TAGS, NO_AUTO_TAGS; }
     private static NamedTaggedFormation addTags(final String name, final Formation f,
             WhetherTagger wt, NumAndTags... tags) {
         List<Dancer> dancers = f.sortedDancers();
         MultiMap<Dancer,Tag> tm = new GenericMultiMap<Dancer,Tag>
             (Factories.enumSetFactory(Tag.class));
         // add existing tags
         if (f instanceof TaggedFormation)
             for (Dancer d : f.dancers())
                 tm.addAll(d, ((TaggedFormation)f).tags(d));
         // add explicitly specified tags.
         for (NumAndTags nt : tags)
             tm.addAll(dancers.get(nt.dancerNum), nt.tags);
         // add implicit/automatic tags
         if (wt != WhetherTagger.NO_AUTO_TAGS) // general formations don't get tags
             Tagger.addAutomatic(f, tm);
         return new NamedTaggedFormation(name, f, tm);
     }
     private static class NumAndTags {
         public final int dancerNum;
         public final Set<Tag> tags;
         NumAndTags(int dancerNum, Set<Tag> tags)
         { this.dancerNum=dancerNum; this.tags=tags; }
     }
     // helper function, for brevity
     private static NumAndTags t(int dancerNum, Tag...  tags) {
         EnumSet<Tag> t = (tags.length==0) ? EnumSet.noneOf(Tag.class) :
             EnumSet.of(tags[0], tags);
         return new NumAndTags(dancerNum, t);
     }
     // tweak ends of a quarter tag or diamond on "diamond spots" in.
     private static NamedTaggedFormation _ends_in(NamedTaggedFormation tf) {
         String name = tf.getName();
 	Rotation ew = Rotation.fromAbsoluteString("|");
 	TaggedFormation result=tf;
 	for (Dancer d: tf.tagged(END)) {
 	    Position p = tf.location(d);
 	    // step "in" in east/west direction towards center line.
 	    if (ew.includes(p.facing))
 		p = p.sideStep(Fraction.ONE, true);
 	    else
 		p = p.forwardStep(Fraction.ONE, true);
 	    result = result.move(d, p);
 	}
 	// transfer name
 	return new NamedTaggedFormation(name, result);
     }
 
     /** Compile formations in this class into "fast" formations. */
     public static void main(String[] args) throws Exception {
         if (args.length==0) {
             emit(new PrintWriter(System.out));
         } else {
             StringWriter sw = new StringWriter();
             emit(new PrintWriter(sw));
             BuildGrammars.writeFile(args[0], sw.toString());
         }
     }
     private static void emit(PrintWriter pw) {
         List<String> allFormations = new ArrayList<String>();
         emitHeader(pw);
         for (Field f : FormationListSlow.class.getDeclaredFields()) {
             if (Modifier.isPublic(f.getModifiers()) &&
                 Modifier.isStatic(f.getModifiers()) &&
                 !f.getName().equals("all")) {
                 try {
                     String fieldName = f.getName();
                     NamedTaggedFormation ntf;
                     ntf = (NamedTaggedFormation) f.get(null);
                     emitOne(pw, fieldName, ntf);
                     allFormations.add(fieldName);
                 } catch (IllegalArgumentException e) {
                     assert false : e;
                 } catch (IllegalAccessException e) {
                     assert false : e;
                 }
             }
         }
         emitAll(pw, allFormations);
         emitFooter(pw);
         pw.flush();
     }
     private static void emitHeader(PrintWriter pw) {
         pw.println("package net.cscott.sdr.calls;");
         pw.println();
         pw.println("import java.util.Arrays;");
         pw.println("import java.util.Collections;");
         pw.println("import java.util.List;");
         pw.println();
         pw.println("import org.junit.runner.RunWith;");
         pw.println();
         pw.println("import net.cscott.jdoctest.JDoctestRunner;");
         pw.println();
         pw.println("import net.cscott.sdr.calls.TaggedFormation.Tag;");
         pw.println("import static net.cscott.sdr.calls.TaggedFormation.TaggedDancerInfo;");
         pw.println("import net.cscott.sdr.util.Fraction;");
         pw.println();
         pw.println("/** Compiled version of {@link FormationListSlow}. */");
         pw.println("@RunWith(value=JDoctestRunner.class)");
         pw.println("abstract class FormationListFast {");
     }
     private static void emitOne(PrintWriter pw, String fieldName,
                                 NamedTaggedFormation ntf) {
         String escapedName = escapeJava(ntf.getName());
         pw.println("    /** "+ntf.getName()+" formation.");
         pw.println("      * @doc.test");
         pw.println("      *  js> FormationList = FormationList.js(this); undefined;");
         pw.println("      *  js> tf = FormationList."+fieldName+"; tf.toStringDiagram('|');");
         pw.println(ntf.toStringDiagram("      *  |"));
         pw.println("      *  js> [tf.tags(dd) for each (dd in Iterator(tf.sortedDancers())) ].join('\\n');");
         for (Dancer d : ntf.sortedDancers())
             pw.println("      *  "+new ArrayList<Tag>(ntf.tags(d)));
         pw.println("      */");
         pw.println("    public static final NamedTaggedFormation "+fieldName+" =");
         pw.print  ("        new NamedTaggedFormation(\""+escapedName+"\"");
         for (Dancer d : ntf.sortedDancers()) {
             pw.println(",");
             pw.print  ("            ");
             pw.print  ("new TaggedDancerInfo(new PhantomDancer(), ");
             pw.print  (ntf.location(d).repr());
             for (Tag t : ntf.tags(d)) {
                 pw.print(", Tag.");
                 pw.print(t.name());
             }
             pw.print(")");
         }
         pw.println(");");
         pw.println();
     }
     private static void emitAll(PrintWriter pw, List<String> allFormations) {
         pw.println("    /** List of all formations defined here. */");
         pw.println("    public static final List<NamedTaggedFormation> all =");
         pw.println("        Collections.unmodifiableList(Arrays.asList(");
         boolean first = true;
         for (String fieldName : allFormations) {
             if (!first) pw.println(",");
             pw.print("            FormationListFast."+fieldName);
             first = false;
         }
         pw.println("));");
     }
     private static void emitFooter(PrintWriter pw) {
         pw.println("}");
     }
 }
