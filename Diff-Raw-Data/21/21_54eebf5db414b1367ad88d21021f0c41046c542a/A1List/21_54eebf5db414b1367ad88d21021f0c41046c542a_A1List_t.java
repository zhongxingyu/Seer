 package net.cscott.sdr.calls.lists;
 
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.Map.Entry;
 
 import net.cscott.jutil.Factories;
 import net.cscott.jutil.GenericMultiMap;
 import net.cscott.jutil.MultiMap;
 import net.cscott.sdr.calls.BadCallException;
 import net.cscott.sdr.calls.Breather;
 import net.cscott.sdr.calls.Call;
 import net.cscott.sdr.calls.DanceState;
 import net.cscott.sdr.calls.Dancer;
 import net.cscott.sdr.calls.DancerPath;
 import net.cscott.sdr.calls.Evaluator;
 import net.cscott.sdr.calls.Formation;
 import net.cscott.sdr.calls.FormationList;
 import net.cscott.sdr.calls.FormationMatch;
 import net.cscott.sdr.calls.GeneralFormationMatcher;
 import net.cscott.sdr.calls.Matcher;
 import net.cscott.sdr.calls.NamedTaggedFormation;
 import net.cscott.sdr.calls.NoMatchException;
 import net.cscott.sdr.calls.PhantomDancer;
 import net.cscott.sdr.calls.Position;
 import net.cscott.sdr.calls.Program;
 import net.cscott.sdr.calls.TaggedFormation;
 import net.cscott.sdr.calls.TimedFormation;
 import net.cscott.sdr.calls.TaggedFormation.Tag;
 import net.cscott.sdr.calls.ast.Apply;
 import net.cscott.sdr.calls.ast.Expr;
 import net.cscott.sdr.calls.grm.Grm;
 import net.cscott.sdr.calls.grm.Rule;
 import net.cscott.sdr.util.Fraction;
 
 /**
  * The <code>A1List</code> class contains complex call
  * and concept definitions which are on the 'A1' program.
  * Note that "simple" calls and concepts are defined in
  * the resource file at
  * <a href="doc-files/a1.calls"><code>net/cscott/sdr/calls/lists/a1.calls</code></a>;
  * this class contains only those definitions for which an
  * executable component is required.
  * @author C. Scott Ananian
  */
 public abstract class A1List {
     // hide constructor.
     private A1List() { }
 
     private static abstract class A1Call extends Call {
         private final String name;
         A1Call(String name) { this.name = name; }
         @Override
         public final String getName() { return name; }
         @Override
         public final Program getProgram() { return Program.A1; }
         @Override
         public List<Expr> getDefaultArguments() {
             return Collections.emptyList();
         }
     }
 
     public static final Call AS_COUPLES = new A1Call("as couples") {
         @Override
         public int getMinNumberOfArguments() { return 1; }
         @Override
         public Rule getRule() {
             Grm g = Grm.parse("as couples <0=anything>");
            return new Rule("anything", g, Fraction.valueOf(-15));
         }
         @Override
         public Evaluator getEvaluator(DanceState ds, List<Expr> args) {
             assert args.size() == 1;
             return new SolidEvaluator(args.get(0), FormationList.COUPLE,
                                       SolidMatch.ALL, SolidType.SOLID);
         }
     };
 
     /** How members of the solid formation move in a {@link SolidEvaluator}. */
     public static enum SolidType { SOLID, TWOSOME };
     /** Whether all members of the formation are expected to be part of
      *  a solid using a {@link SolidEvaluator}, or just some of them. */
     public static enum SolidMatch { SOME, ALL };
     /** Evaluator for couples/tandem/solid formations. */
     public static class SolidEvaluator extends Evaluator {
         private final Expr subCall;
         private final String formationName;
         private final Matcher solidMatcher;
         private final SolidType type;
         public SolidEvaluator(Expr subCall,
                               final NamedTaggedFormation solidFormation,
                               final SolidMatch match, SolidType type) {
             this(subCall, solidFormation.getName(), new Matcher() {
                 @Override
                 public FormationMatch match(Formation f) throws NoMatchException {
                     return GeneralFormationMatcher.doMatch
                         (f, solidFormation, (match == SolidMatch.SOME),
                          false /* no phantoms */);
                 }
                 @Override
                 public String getName() { return solidFormation.getName(); }
             }, type);
         }
         public SolidEvaluator(Expr subCall, String formationName,
                               Matcher solidMatcher, SolidType type) {
             this.subCall = subCall;
             this.formationName = formationName;
             this.solidMatcher = solidMatcher;
             this.type = type;
         }
 
         @Override
         public Evaluator evaluate(DanceState ds) {
             // this is very similar to what we do in MetaEvaluator
             FormationMatch fm;
             try {
                 fm = this.solidMatcher.match(ds.currentFormation());
             } catch (NoMatchException nme) {
                 throw new BadCallException("No "+this.formationName+" dancers");
             }
             // ok, now tag the meta dancers the way the real dancers are
             // tagged
             fm = transferTags(fm);
             // and do the call in the meta formation
             Formation metaF = fm.meta;
             DanceState metaS = ds.cloneAndClear(metaF);
             new Apply(this.subCall).evaluator(metaS).evaluateAll(metaS);
             metaS.syncDancers();
             // now go through moment-by-moment and insert the subformations
             // XXX lots of cut-and-paste from MetaEvaluator; we should
             //     refactor out the common code.
 
             // go through all the moments in time, constructing an appropriately
             // breathed formation.
             TreeMap<Fraction,Formation> merged =
                 new TreeMap<Fraction,Formation>();
             for (TimedFormation tf : metaS.formations()) {
                 Map<Dancer,Formation> pieces =
                     new LinkedHashMap<Dancer,Formation>();
                 for (Entry<Dancer,TaggedFormation> e : fm.matches.entrySet()) {
                     Dancer d = e.getKey();
                     TaggedFormation solidPiece = e.getValue();
                     if (this.type == SolidType.TWOSOME) {
                         // XXX for twosome, rotate formations
                         throw new BadCallException("twosome not implemented");
                     }
                     // transfer roll, etc from tf.formation to solidPiece
                     solidPiece = transferPositionFlags
                         (tf.formation.location(d), solidPiece);
                     pieces.put(d, solidPiece);
                 }
                 Formation mergeF = Breather.insert(tf.formation, pieces);
                 merged.put(tf.time, mergeF);
             }
             // okay, now go through the individual dancer paths, adjusting the
             // 'to' and 'from' positions to match breathed.
             for (Dancer metaD : metaS.dancers()) {
                 for (Dancer d : fm.matches.get(metaD).dancers()) {
                     Fraction t = Fraction.ZERO;
                     for (DancerPath dp : metaS.movements(metaD)) {
                         Position nfrom = merged.get(t).location(d);
                         t = t.add(dp.time);
                         Position nto = merged.get(t).location(d);
                         DancerPath ndp = dp.translate(nfrom, nto);
                         ds.add(d, ndp);
                     }
                 }
             }
             // dancers should all be in sync at this point.
             // no more to evaluate
             return null;
         }
         /** Transfer position flags from 'location' to all dancers in the
          *  'solidPiece', returning the new 'solidPiece'. */
         private TaggedFormation transferPositionFlags
             (Position location, TaggedFormation solidPiece) {
             TaggedFormation npiece = solidPiece;
             for (Dancer d : solidPiece.dancers()) {
                 Position p = solidPiece.location(d);
                 p = p.setFlags(location.flags);
                 npiece = npiece.move(d, p); // XXX not very efficient
             }
             return npiece;
         }
         /** Add any tags which a subformation has in common to the meta-dancer
          *  and meta-formation. */
         private static FormationMatch transferTags(FormationMatch fm) {
             MultiMap<Dancer,Tag> metaTags = new GenericMultiMap<Dancer,Tag>
                 (Factories.enumSetFactory(Tag.class));
             Map<Dancer,Position> metaPos =
                 new LinkedHashMap<Dancer,Position>();
             Set<Dancer> unmatched =
                 new LinkedHashSet<Dancer>();
             Map<Dancer,TaggedFormation> matches =
                 new LinkedHashMap<Dancer,TaggedFormation>();
 
             for (Dancer md : fm.meta.dancers()) {
                 // this is a little bit awkward because primitive tags are
                 // kept by the dancer, not by the formation
                 EnumSet<Tag> commonTags =
                     EnumSet.allOf(Tag.class);
                 EnumSet<Position.Flag> commonFlags =
                     EnumSet.allOf(Position.Flag.class);
                 TaggedFormation subF = fm.matches.get(md);
 
                 for (Dancer d : subF.dancers()) {
                     // compute the set of common tags and flags
                     EnumSet<Tag> dancerTags = primitiveTags(d);
                     dancerTags.addAll(subF.tags(d));
                     commonTags.retainAll(dancerTags);
                     commonFlags.retainAll(subF.location(d).flags);
                 }
                 commonTags.remove(Tag.ALL); // redundant; everyone matches ALL
                 EnumSet<Tag> commonPrim = EnumSet.copyOf(commonTags);
                 commonPrim.retainAll(PRIMITIVE_TAGS);
                 commonTags.removeAll(commonPrim);
 
                 // primitive common tags go on the new meta dancer
                 Dancer nmd = new PhantomDancer(commonPrim);
                 // and non-primitive common tags will go in the taggedformation
                 metaTags.addAll(nmd, commonTags);
                 // position flags go on the position
                 metaPos.put(nmd, fm.meta.location(md).setFlags(commonFlags));
 
                 // transfer information from FormationMatch
                 if (fm.unmatched.contains(md)) unmatched.add(nmd);
                 matches.put(nmd, subF);
             }
             // create new FormationMatch with the new meta formation
             TaggedFormation metaF =
                 new TaggedFormation(new Formation(metaPos), metaTags);
             return new FormationMatch(metaF, matches, unmatched);
         }
         /** Return all the primitive tags for the given dancer (including
          *  the 'ALL' tag). */
         private static EnumSet<Tag> primitiveTags(Dancer d) {
             EnumSet<Tag> result = EnumSet.noneOf(Tag.class);
             for (Tag t : PRIMITIVE_TAGS)
                 if (d.matchesTag(t))
                     result.add(t);
             return result;
         }
         /** All primitive tags, including the 'ALL' tag. */
         private static final EnumSet<Tag> PRIMITIVE_TAGS = EnumSet.noneOf(Tag.class);
         static {
             for (Tag t : Tag.values())
                 if (t.isPrimitive())
                     PRIMITIVE_TAGS.add(t);
         }
     };
 }
