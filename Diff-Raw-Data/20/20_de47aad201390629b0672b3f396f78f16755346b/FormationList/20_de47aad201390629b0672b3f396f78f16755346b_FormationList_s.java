 package net.cscott.sdr.calls;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.cscott.jutil.Factories;
 import net.cscott.jutil.GenericMultiMap;
 import net.cscott.jutil.MultiMap;
 import net.cscott.sdr.calls.TaggedFormation.Tag;
 import static net.cscott.sdr.calls.TaggedFormation.Tag.*;
 import net.cscott.sdr.calls.TaggedFormation.TaggedDancerInfo;
 import net.cscott.sdr.util.Fraction;
 
 /**
  * A list of common formations, specified with phantoms.
  *
  * @doc.test Almost all standard formations are maximally "breathed in".
  *  Diamonds are a special case.  There should be "expanded" diamonds and
  *  "compressed" diamonds.  (Also the case for quarter tags?)
  *  js> for (f in Iterator(FormationList.all)) {
  *    >   if (!Breather.breathe(f).equals(f)) {
  *    >     print("Unbreathed formation: "+f.getName());
  *    >   }
  *    > }
  *  Unbreathed formation: RH DIAMOND
  *  Unbreathed formation: RH FACING DIAMOND
  *  Unbreathed formation: LH DIAMOND
  *  Unbreathed formation: LH FACING DIAMOND
  *  Unbreathed formation: RH TWIN DIAMONDS
  *  Unbreathed formation: LH TWIN DIAMONDS
  *  Unbreathed formation: RH POINT-TO-POINT DIAMONDS
  *  Unbreathed formation: RH POINT-TO-POINT FACING DIAMONDS
  *  Unbreathed formation: LH POINT-TO-POINT DIAMONDS
  *  Unbreathed formation: LH POINT-TO-POINT FACING DIAMONDS
  *  Unbreathed formation: RH TWIN FACING DIAMONDS
  *  Unbreathed formation: LH TWIN FACING DIAMONDS
  * @doc.test Canonical formations should be centered.
  *  js> for (f in Iterator(FormationList.all)) {
  *    >   if (!f.isCentered()) {
  *    >     print("Uncentered formation: "+f.getName());
  *    >   }
  *    > } ; undefined
  *  js> // note no output from the above.
  * @doc.test Canonical formations should be oriented so that "most"
  *  dancers are facing north or south.  This seems to match standard
  *  diagrams best.
  *  js> ns = Rotation.fromAbsoluteString("|");
  *  0 mod 1/2
  *  js> for (f in Iterator(FormationList.all)) {
  *    >   l=[ns.includes(f.location(d).facing) for (d in Iterator(f.dancers()))]
  *    >   if ([b for each (b in l) if (b)].length <
  *    >       [b for each (b in l) if (!b)].length) {
  *    >     print("Unexpected orientation: "+f.getName());
  *    >   }
  *    > } ; undefined
  *  js> // note no output from the above.
  */
 // can use SelectorList to associate phantoms with real dancers.
 public abstract class FormationList {
 
 
     // from http://www.penrod-sq-dancing.com/form2.html
     // see also http://www.penrod-sq-dancing.com/fars0.html
     // 'create' should really be the constructor for a formation subclass.
     // things like 'three and one' lines really want another programming
     // "check formation" method which can be overridden??
 /*
     public static final Formation GENERAL_DIAMONDS = // too permissive?
 	create(// centers
 	       d(0,+3,"-", CENTER),
 	       d(0,+1,"-", CENTER, VERY_CENTER),
 	       d(0,-1,"-", CENTER, VERY_CENTER),
 	       d(0,-3,"-", CENTER),
 	       // points
 	       d(-3,+2,"|", END),
 	       d(-3,-2,"|", END),
 	       d(+3,+2,"|", END),
 	       d(+3,-2,"|", END));
     public static final Formation GENERAL_TAG =
 	create(" - ","---","---"," - ");
     public static final Formation Z_FORMATION =
 	create("s ","sn","sn","sn"," n");
 */
     // labelled calls named as per the "Callerlab Approved Formations"
     // from April 1980
     
     // 1-person formation
     public static final TaggedFormation SINGLE_DANCER =
         create("SINGLE DANCER",
                 d(0,0,"n"));
     // 2-person formations
     public static final TaggedFormation COUPLE = // callerlab #1
         create("COUPLE",
                 d(-1,0,"n",BEAU),
                 d(+1,0,"n",BELLE));
     public static final TaggedFormation FACING_DANCERS = // callerlab #2
         create("FACING DANCERS",
                 d(0,+1,"s",TRAILER),
                 d(0,-1,"n",TRAILER));
     public static final TaggedFormation BACK_TO_BACK_DANCERS = // callerlab #3
         create("BACK TO BACK DANCERS",
                 d(0,+1,"n",LEADER),
                 d(0,-1,"s",LEADER));
     public static final TaggedFormation TANDEM =
         create("TANDEM",
                d(0,+1,"n",LEADER),
                d(0,-1,"n",TRAILER));
     public static final TaggedFormation RH_MINIWAVE = // callerlab #4
         create("RH MINIWAVE",
                d(-1,0,"n",BEAU),
                d(+1,0,"s",BEAU));
     public static final TaggedFormation LH_MINIWAVE = // callerlab #5
         create("LH MINIWAVE",
                d(-1,0,"s",BELLE),
                d(+1,0,"n",BELLE));
     // 4-person formations
     public static final TaggedFormation GENERAL_LINE =
         create("GENERAL LINE", f("||||"), WhetherTagger.NO_AUTO_TAGS,
                 t(0, END), t(1,CENTER), t(2,CENTER), t(3,END));
     public static final TaggedFormation _2x2 =
         create("2x2", f("++","++"), WhetherTagger.NO_AUTO_TAGS);
     public static final TaggedFormation FACING_COUPLES = // callerlab #6
         xofy("FACING COUPLES", FACING_DANCERS, COUPLE);
     public static final TaggedFormation BACK_TO_BACK_COUPLES = // callerlab #7
         xofy("BACK TO BACK COUPLES", BACK_TO_BACK_DANCERS, COUPLE);
     public static final TaggedFormation TANDEM_COUPLES =
         xofy("TANDEM COUPLES", TANDEM, COUPLE);
     public static final TaggedFormation RH_OCEAN_WAVE = // callerlab #8
         xofy("RH OCEAN WAVE", COUPLE, RH_MINIWAVE,
                 t(0, END), t(1, CENTER), t(2, CENTER), t(3, END));
     public static final TaggedFormation LH_OCEAN_WAVE = // callerlab #9
         xofy("LH OCEAN WAVE", COUPLE, LH_MINIWAVE,
                 t(0, END), t(1, CENTER), t(2, CENTER), t(3, END));
     public static final TaggedFormation RH_BOX = // callerlab #10
         xofy("RH BOX", RH_MINIWAVE, TANDEM);
     public static final TaggedFormation LH_BOX = // callerlab #11
         xofy("LH BOX", LH_MINIWAVE, TANDEM);
     public static final TaggedFormation RH_TWO_FACED_LINE = // callerlab #12
         xofy("RH TWO-FACED LINE", RH_MINIWAVE, COUPLE);
     public static final TaggedFormation LH_TWO_FACED_LINE = // callerlab #13
         xofy("LH TWO-FACED LINE", LH_MINIWAVE, COUPLE);
     public static final TaggedFormation SINGLE_INVERTED_LINE =
         create("SINGLE INVERTED LINE", f("snns"), WhetherTagger.AUTO_TAGS,
                 t(0, END), t(1,CENTER), t(2,CENTER), t(3,END));
     public static final TaggedFormation RH_DIAMOND =
         create("RH DIAMOND",
                d( 0, 3,"e",POINT),
                d(-1, 0,"n",BEAU,CENTER),
                d(+1, 0,"s",BEAU,CENTER),
                d( 0,-3,"w",POINT));
     public static final TaggedFormation RH_FACING_DIAMOND =
         create("RH FACING DIAMOND",
                d( 0, 3,"e",POINT),
                d(-1, 0,"s",BELLE,CENTER),
                d(+1, 0,"n",BELLE,CENTER),
                d( 0,-3,"w",POINT));
     public static final TaggedFormation LH_DIAMOND =
         create("LH DIAMOND",
                d( 0, 3,"w",POINT),
                d(-1, 0,"s",BELLE,CENTER),
                d(+1, 0,"n",BELLE,CENTER),
                d( 0,-3,"e",POINT));
     public static final TaggedFormation LH_FACING_DIAMOND =
         create("LH FACING DIAMOND",
                d( 0, 3,"w",POINT),
                d(-1, 0,"n",BEAU,CENTER),
                d(+1, 0,"s",BEAU,CENTER),
                d( 0,-3,"e",POINT));
     public static final TaggedFormation RH_SINGLE_PROMENADE =
         create("RH SINGLE PROMENADE",
                 d( 0, 1, "e"),
                 d(-1, 0, "n"),
                 d( 0,-1, "w"),
                 d( 1, 0, "s")); // this is a star: is that correct?
     public static final TaggedFormation LH_SINGLE_PROMENADE =
         create("LH SINGLE PROMENADE",
                 d( 0, 1, "w"),
                 d(-1, 0, "s"),
                 d( 0,-1, "e"),
                 d( 1, 0, "n")); // this is a star: is that correct?
     public static final TaggedFormation RH_SINGLE_QUARTER_TAG =
         create("RH SINGLE 1/4 TAG",
                d( 0, 2,"s",END),
                d(-1, 0,"n",BEAU,CENTER),
                d(+1, 0,"s",BEAU,CENTER),
                d( 0,-2,"n",END));
     public static final TaggedFormation LH_SINGLE_QUARTER_TAG =
         create("LH SINGLE 1/4 TAG",
                d( 0, 2,"s",END),
                d(-1, 0,"s",BELLE,CENTER),
                d(+1, 0,"n",BELLE,CENTER),
                d( 0,-2,"n",END));
     public static final TaggedFormation RH_SINGLE_THREE_QUARTER_TAG =
         create("RH SINGLE 3/4 TAG",
                d( 0, 2,"n",END),
                d(-1, 0,"n",BEAU,CENTER),
                d(+1, 0,"s",BEAU,CENTER),
                d( 0,-2,"s",END));
     public static final TaggedFormation LH_SINGLE_THREE_QUARTER_TAG =
         create("LH SINGLE 3/4 TAG",
                d( 0, 2,"n",END),
                d(-1, 0,"s",BELLE,CENTER),
                d(+1, 0,"n",BELLE,CENTER),
                d( 0,-2,"s",END));
     public static final TaggedFormation SINGLE_DOUBLE_PASS_THRU =
         xofy("SINGLE DOUBLE PASS THRU", FACING_DANCERS, TANDEM,
                 t(0,END),t(1,CENTER),t(2,CENTER),t(3,END));
     public static final TaggedFormation COMPLETED_SINGLE_DOUBLE_PASS_THRU =
         xofy("COMPLETED SINGLE DOUBLE PASS THRU", BACK_TO_BACK_DANCERS, TANDEM,
                 t(0,END),t(1,CENTER),t(2,CENTER),t(3,END));
     // 8-person formations. ///////////////////////////////
     public static final TaggedFormation STATIC_SQUARE = // callerlab #14
         create("STATIC SQUARE", f(" ss ","e  w","e  w"," nn "),
                 WhetherTagger.AUTO_TAGS);
     // XXX circle, callerlab #15
     // XXX single file promenade, callerlab #16
     // XXX alamo style, callerlab #17
     public static final TaggedFormation PROMENADE = // callerlab #18
         xofy("PROMENADE", LH_SINGLE_PROMENADE, COUPLE);
     public static final TaggedFormation WRONG_WAY_PROMENADE =
         xofy("WRONG WAY PROMENADE", RH_SINGLE_PROMENADE, COUPLE);
     public static final TaggedFormation THAR =
         xofy("THAR", LH_SINGLE_PROMENADE, LH_MINIWAVE);
     public static final TaggedFormation WRONG_WAY_THAR =
         xofy("WRONG WAY THAR", LH_SINGLE_PROMENADE, RH_MINIWAVE);
     public static final TaggedFormation FACING_LINES = // callerlab #22
         xofy("FACING LINES", FACING_COUPLES, COUPLE,
                 t(0, END), t(1, CENTER), t(2, CENTER), t(3, END),
                 t(4, END), t(5, CENTER), t(6, CENTER), t(7, END));
     public static final TaggedFormation EIGHT_CHAIN_THRU = // callerlab #23
         xofy("EIGHT CHAIN THRU", FACING_COUPLES, FACING_DANCERS,
                 t(0,END   ),t(1,END),
                 t(2,CENTER),t(3,CENTER),
                 t(4,CENTER),t(5,CENTER),
                 t(6,END   ),t(7,END));
     public static final TaggedFormation TRADE_BY = // callerlab #24
         xofy("TRADE BY", FACING_COUPLES, BACK_TO_BACK_DANCERS,
                 t(0,END   ),t(1,END),
                 t(2,CENTER),t(3,CENTER),
                 t(4,CENTER),t(5,CENTER),
                 t(6,END   ),t(7,END));
     public static final TaggedFormation DOUBLE_PASS_THRU = // callerlab #25
         xofy("DOUBLE PASS THRU", FACING_COUPLES, TANDEM,
                 t(0,END   ),t(1,END),
                 t(2,CENTER),t(3,CENTER),
                 t(4,CENTER),t(5,CENTER),
                 t(6,END   ),t(7,END));
     public static final TaggedFormation COMPLETED_DOUBLE_PASS_THRU = // callerlab #26
         xofy("COMPLETED DOUBLE PASS THRU", BACK_TO_BACK_COUPLES, TANDEM,
                 t(0,END   ),t(1,END),
                 t(2,CENTER),t(3,CENTER),
                 t(4,CENTER),t(5,CENTER),
                 t(6,END   ),t(7,END));
     public static final TaggedFormation LINES_FACING_OUT = // callerlab #27
         xofy("LINES FACING OUT", BACK_TO_BACK_COUPLES, COUPLE,
                 t(0, END), t(1, CENTER), t(2, CENTER), t(3, END),
                 t(4, END), t(5, CENTER), t(6, CENTER), t(7, END));
     public static final TaggedFormation PARALLEL_RH_WAVES = // callerlab #28(a)
         xofy("PARALLEL RH WAVES", RH_OCEAN_WAVE, TANDEM,
                 t(0, END), t(1, CENTER), t(2, CENTER), t(3, END),
                 t(4, END), t(5, CENTER), t(6, CENTER), t(7, END));
     public static final TaggedFormation PARALLEL_LH_WAVES = // callerlab #28(b)
         xofy("PARALLEL LH WAVES", LH_OCEAN_WAVE, TANDEM,
                 t(0, END), t(1, CENTER), t(2, CENTER), t(3, END),
                 t(4, END), t(5, CENTER), t(6, CENTER), t(7, END));
     public static final TaggedFormation PARALLEL_RH_TWO_FACED_LINES = // callerlab #29(a)
         xofy("PARALLEL RH TWO-FACED LINES", RH_BOX, COUPLE,
                 t(0, END), t(1, CENTER), t(2, CENTER), t(3, END),
                 t(4, END), t(5, CENTER), t(6, CENTER), t(7, END));
     public static final TaggedFormation PARALLEL_LH_TWO_FACED_LINES = // callerlab #29(b)
         xofy("PARALLEL LH TWO-FACED LINES", LH_BOX, COUPLE,
                 t(0, END), t(1, CENTER), t(2, CENTER), t(3, END),
                 t(4, END), t(5, CENTER), t(6, CENTER), t(7, END));
     public static final TaggedFormation RH_COLUMN = // callerlab #30
         xofy("RH COLUMN", RH_BOX, TANDEM,
                 t(0,NUMBER_1,END   ),t(1,NUMBER_4,END),
                 t(2,NUMBER_2,CENTER),t(3,NUMBER_3,CENTER),
                 t(4,NUMBER_3,CENTER),t(5,NUMBER_2,CENTER),
                 t(6,NUMBER_4,END   ),t(7,NUMBER_1,END));
     public static final TaggedFormation LH_COLUMN = // callerlab #31
         xofy("LH COLUMN", LH_BOX, TANDEM,
                 t(0,NUMBER_4,END   ),t(1,NUMBER_1,END),
                 t(2,NUMBER_3,CENTER),t(3,NUMBER_2,CENTER),
                 t(4,NUMBER_2,CENTER),t(5,NUMBER_3,CENTER),
                 t(6,NUMBER_1,END   ),t(7,NUMBER_4,END));
     // XXX 3-and-1 lines, 8 possible, callerlab #32
     public static final TaggedFormation ENDS_IN_INVERTED_LINES = // callerlab #33
         xofy("ENDS IN INVERTED LINES", BACK_TO_BACK_DANCERS, SINGLE_INVERTED_LINE);
     public static final TaggedFormation ENDS_OUT_INVERTED_LINES = // callerlab #34
         xofy("ENDS OUT INVERTED LINES", FACING_DANCERS, SINGLE_INVERTED_LINE);
     // XXX in t-bone lines, callerlab #35
     // XXX out t-bone lines, callerlab #36
     // xxx both diamond-spot quarter tag & compressed quarter tag?
     // xxx do we want all variants here, or just the "canonical" ones?
     public static final TaggedFormation RH_QUARTER_TAG = // callerlab #37(a)
 	_ends_in(xofy("RH 1/4 TAG", RH_MINIWAVE, RH_SINGLE_QUARTER_TAG,
 		      t(0,OUTSIDE_6,CENTER_6),t(1,OUTSIDE_6,CENTER_6),
 		      t(2,OUTSIDE_6),t(3,VERY_CENTER,CENTER_6),
 		      t(4,VERY_CENTER,CENTER_6),t(5,OUTSIDE_6),
 		      t(6,OUTSIDE_6,CENTER_6),t(7,OUTSIDE_6,CENTER_6)));
     public static final TaggedFormation LH_QUARTER_TAG = // callerlab #37(a)
 	_ends_in(xofy("LH 1/4 TAG", LH_MINIWAVE, LH_SINGLE_QUARTER_TAG,
 		      t(0,OUTSIDE_6,CENTER_6),t(1,OUTSIDE_6,CENTER_6),
 		      t(2,OUTSIDE_6),t(3,VERY_CENTER,CENTER_6),
 		      t(4,VERY_CENTER,CENTER_6),t(5,OUTSIDE_6),
 		      t(6,OUTSIDE_6,CENTER_6),t(7,OUTSIDE_6,CENTER_6)));
     public static final TaggedFormation RH_THREE_QUARTER_TAG = // callerlab #38(a)
 	_ends_in(xofy("RH 3/4 TAG", RH_MINIWAVE, RH_SINGLE_THREE_QUARTER_TAG,
 		      t(0,OUTSIDE_6,CENTER_6),t(1,OUTSIDE_6,CENTER_6),
 		      t(2,OUTSIDE_6),t(3,VERY_CENTER,CENTER_6),
 		      t(4,VERY_CENTER,CENTER_6),t(5,OUTSIDE_6),
 		      t(6,OUTSIDE_6,CENTER_6),t(7,OUTSIDE_6,CENTER_6)));
     public static final TaggedFormation LH_THREE_QUARTER_TAG = // callerlab #38(b)
 	_ends_in(xofy("LH 3/4 TAG", LH_MINIWAVE, LH_SINGLE_THREE_QUARTER_TAG,
 		      t(0,OUTSIDE_6,CENTER_6),t(1,OUTSIDE_6,CENTER_6),
 		      t(2,OUTSIDE_6),t(3,VERY_CENTER,CENTER_6),
 		      t(4,VERY_CENTER,CENTER_6),t(5,OUTSIDE_6),
 		      t(6,OUTSIDE_6,CENTER_6),t(7,OUTSIDE_6,CENTER_6)));
     public static final TaggedFormation RH_QUARTER_LINE = // callerlab #39(a)
 	_ends_in(xofy("RH 1/4 LINE", RH_SINGLE_QUARTER_TAG, COUPLE,
 		      t(0,OUTSIDE_6,CENTER_6),t(1,OUTSIDE_6,CENTER_6),
 		      t(2,OUTSIDE_6),t(3,VERY_CENTER,CENTER_6),
 		      t(4,VERY_CENTER,CENTER_6),t(5,OUTSIDE_6),
 		      t(6,OUTSIDE_6,CENTER_6),t(7,OUTSIDE_6,CENTER_6)));
     public static final TaggedFormation LH_QUARTER_LINE = // callerlab #39(b)
 	_ends_in(xofy("LH 1/4 LINE", LH_SINGLE_QUARTER_TAG, COUPLE,
 		      t(0,OUTSIDE_6,CENTER_6),t(1,OUTSIDE_6,CENTER_6),
 		      t(2,OUTSIDE_6),t(3,VERY_CENTER,CENTER_6),
 		      t(4,VERY_CENTER,CENTER_6),t(5,OUTSIDE_6),
 		      t(6,OUTSIDE_6,CENTER_6),t(7,OUTSIDE_6,CENTER_6)));
     public static final TaggedFormation RH_THREE_QUARTER_LINE = // callerlab #39(a)
 	_ends_in(xofy("RH 3/4 LINE", RH_SINGLE_THREE_QUARTER_TAG, COUPLE,
 		      t(0,OUTSIDE_6,CENTER_6),t(1,OUTSIDE_6,CENTER_6),
 		      t(2,OUTSIDE_6),t(3,VERY_CENTER,CENTER_6),
 		      t(4,VERY_CENTER,CENTER_6),t(5,OUTSIDE_6),
 		      t(6,OUTSIDE_6,CENTER_6),t(7,OUTSIDE_6,CENTER_6)));
     public static final TaggedFormation LH_THREE_QUARTER_LINE = // callerlab #39(b)
 	_ends_in(xofy("LH 3/4 LINE", LH_SINGLE_THREE_QUARTER_TAG, COUPLE,
 		      t(0,OUTSIDE_6,CENTER_6),t(1,OUTSIDE_6,CENTER_6),
 		      t(2,OUTSIDE_6),t(3,VERY_CENTER,CENTER_6),
 		      t(4,VERY_CENTER,CENTER_6),t(5,OUTSIDE_6),
 		      t(6,OUTSIDE_6,CENTER_6),t(7,OUTSIDE_6,CENTER_6)));
     public static final TaggedFormation RH_TWIN_DIAMONDS = // callerlab #40
         xofy("RH TWIN DIAMONDS", COUPLE, RH_DIAMOND);
     public static final TaggedFormation LH_TWIN_DIAMONDS = // callerlab #41
         xofy("LH TWIN DIAMONDS", COUPLE, LH_DIAMOND);
     public static final TaggedFormation RH_POINT_TO_POINT_DIAMONDS = // callerlab #42(a)
         xofy("RH POINT-TO-POINT DIAMONDS", TANDEM, RH_DIAMOND);
     public static final TaggedFormation RH_POINT_TO_POINT_FACING_DIAMONDS = // callerlab #42(b)
         xofy("RH POINT-TO-POINT FACING DIAMONDS", TANDEM, RH_FACING_DIAMOND);
     public static final TaggedFormation LH_POINT_TO_POINT_DIAMONDS = // callerlab #42(c)
         xofy("LH POINT-TO-POINT DIAMONDS", TANDEM, LH_DIAMOND);
     public static final TaggedFormation LH_POINT_TO_POINT_FACING_DIAMONDS = // callerlab #42(d)
         xofy("LH POINT-TO-POINT FACING DIAMONDS", TANDEM, LH_FACING_DIAMOND);
     public static final TaggedFormation RH_TWIN_FACING_DIAMONDS = // callerlab #43
         xofy("RH TWIN FACING DIAMONDS", COUPLE, RH_FACING_DIAMOND);
     public static final TaggedFormation LH_TWIN_FACING_DIAMONDS = // callerlab #44
         xofy("LH TWIN FACING DIAMONDS", COUPLE, LH_FACING_DIAMOND);
     // XXX hourglass, callerlab #45
     // XXX left hand Z, callerlab #46
     // XXX right hand Z, callerlab #47
     // XXX right hand stars, callerlab #48
     // XXX left hand stars, callerlab #49
     public static final TaggedFormation RH_TIDAL_WAVE = // callerlab #50(a)
         xofy("RH TIDAL WAVE", COUPLE, RH_OCEAN_WAVE);
     public static final TaggedFormation LH_TIDAL_WAVE = // callerlab #50(b)
         xofy("LH TIDAL WAVE", COUPLE, LH_OCEAN_WAVE);
     public static final TaggedFormation RH_TIDAL_TWO_FACED_LINE = // callerlab #51(a)
         xofy("RH TIDAL TWO-FACED LINE", COUPLE, RH_TWO_FACED_LINE);
     public static final TaggedFormation LH_TIDAL_TWO_FACED_LINE = // callerlab #51(b)
         xofy("LH TIDAL TWO-FACED LINE", COUPLE, LH_TWO_FACED_LINE);
     public static final TaggedFormation RH_TIDAL_LINE = // callerlab #52(a)
         xofy("RH TIDAL LINE", RH_TWO_FACED_LINE, COUPLE);
     public static final TaggedFormation LH_TIDAL_LINE = // callerlab #52(b)
         xofy("LH TIDAL LINE", LH_TWO_FACED_LINE, COUPLE);
     // XXX galaxy, callerlab #53
 
     /** List of all formations defined in this class. */
     public static final List<Formation> all = _enumerateFormations();
     private static List<Formation> _enumerateFormations() {
         ArrayList<Formation> result = new ArrayList<Formation>();
         for (Field f: FormationList.class.getDeclaredFields()) {
             if (f.getName().toUpperCase().equals(f.getName()) &&
                 Modifier.isStatic(f.getModifiers()) &&
                 Modifier.isPublic(f.getModifiers())) {
                 try {
                     result.add((Formation)f.get(null));
                 } catch (Throwable t) { /* ignore */ }
             }
         }
         result.trimToSize();
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
         return new PositionAndTag(Position.getGrid(x,y,facing),
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
         return addTags(name, _xofy(x,y), WhetherTagger.AUTO_TAGS, tags);
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
 
     public static void main(String[] args) throws Exception {
         //System.out.println(xofy("test formation", FACING_DANCERS, COUPLE).toStringDiagram());
         for (Field f : FormationList.class.getFields()) {
             if (Modifier.isPublic(f.getModifiers()) &&
                 Modifier.isStatic(f.getModifiers()) &&
 		f.getName().toUpperCase().equals(f.getName())) {
                 TaggedFormation ff = (TaggedFormation) f.get(null);
                 System.out.println("FormationList."+f.getName());
                 System.out.println(ff.toStringDiagram());
                 System.out.println(ff.toString());
 		System.out.println();
             }
         }
     }
 }
