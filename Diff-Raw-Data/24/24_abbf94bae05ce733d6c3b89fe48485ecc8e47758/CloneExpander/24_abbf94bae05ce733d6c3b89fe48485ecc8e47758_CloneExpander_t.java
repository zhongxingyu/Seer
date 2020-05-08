 package ch.unibe.scg.cc;
 
 import static ch.unibe.scg.cc.Backend.MINIMUM_LINES;
 
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 
 import javax.inject.Inject;
 import javax.inject.Provider;
 
 import ch.unibe.scg.cc.Protos.Clone;
 import ch.unibe.scg.cc.Protos.SnippetLocation;
 import ch.unibe.scg.cc.Protos.SnippetMatch;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMultimap;
 import com.google.common.collect.Lists;
 
 /**
  * Given a list of matches, extracts all fully expanded clones.
  *
  * <p>
  * Popular snippets are treated special. We enforce earlier in the pipeline that
  * all functions that contain popular snippets also appear at least once in a
  * match's {@link SnippetMatch#getThisSnippetLocation()} as an input to
  * {@link #expandClones(Iterable)}.
  */
 // TODO(simon): make it so!
 public class CloneExpander {
 	private static final int MAX_GAP = 10;
 	// TODO: this should not be a constant here.
 	// Instead, look at the snippetlocations, they should contain their length.
 
 	/**
 	 * Maps from functions hashes to all of their popular snippet. See the
 	 * popularsnippets Htable definition for details.
 	 */
 	final ImmutableMultimap<ByteBuffer, SnippetLocation> function2PopularSnippets;
 	/** Maps from snippet hash to popular snippet locations. */
 	final ImmutableMultimap<ByteBuffer, SnippetLocation> snippet2PopularSnippet;
 
 	final Comparator<SnippetMatch> snippetMatchComparator;
 
 	@Inject
 	CloneExpander(Provider<PopularSnippetMaps> popularSnippetMapsProvider,
 			Comparator<SnippetMatch> snippetMatchComparator) {
 		PopularSnippetMaps popularSnippetMaps = popularSnippetMapsProvider.get();
 		this.function2PopularSnippets = popularSnippetMaps.getFunction2PopularSnippets();
 		this.snippet2PopularSnippet = popularSnippetMaps.getSnippet2PopularSnippets();
 		this.snippetMatchComparator = snippetMatchComparator;
 	}
 
 	/**
 	 * Stitch together the matches into Clones.
 	 *
 	 * @param matches
 	 *            sorted first by
 	 *            {@code SnippetMatch.getThatSnippetLocation().getFunction()},
 	 *            second by
 	 *            {@code SnippetMatch.getThisSnippetLocation().getPosition()}.
 	 *            {@code SnippetMatch.getThisSnippetLocation.getFunction()} must
 	 *            be constant for the entire list.
 	 * @return The matches, stitched together.
 	 */
 	public Collection<Clone> expandClones(final Iterable<SnippetMatch> matches) {
 		if (!matches.iterator().hasNext()) {
 			return Collections.emptyList();
 		}
 		SnippetMatch first = matches.iterator().next();
 		for (SnippetMatch match : matches) {
 			assert match.getThisSnippetLocation().getFunction().equals(first.getThisSnippetLocation().getFunction());
 		}
 
 		final ImmutableList.Builder<Clone> clones = ImmutableList.builder();
 		final LinkedList<SnippetMatch> unprocessedMatches = Lists.newLinkedList(matches);
 
 		weaveInPopularSnippets(unprocessedMatches);
 
 		while (!unprocessedMatches.isEmpty()) {
 			final Iterator<SnippetMatch> iter = unprocessedMatches.iterator();
 			SnippetMatch last = iter.next();
 			iter.remove();
 
 			Clone.Builder clone = initializeClone(last);
 
 			while (iter.hasNext()) {
 				final SnippetMatch cur = iter.next();
 				if (!cur.getThatSnippetLocation().getFunction().equals(last.getThatSnippetLocation().getFunction())) {
 					clones.add(finalizeClone(clone));
 					iter.remove();
 					clone = initializeClone(cur);
 					last = cur;
 					continue;
 				}
 				if (Math.abs(last.getThisSnippetLocation().getPosition() - cur.getThisSnippetLocation().getPosition()) <= MAX_GAP) {
 					if (Math.abs(last.getThatSnippetLocation().getPosition()
 							- cur.getThatSnippetLocation().getPosition()) <= MAX_GAP) {
 						iter.remove();
 
 						// Note that clones are sorted by thisLength, not
 						// thatLength.
 						clone.setThisLength(cur.getThisSnippetLocation().getPosition() - clone.getThisFromPosition()
 								+ 1);
 						clone.setThatFromPosition(Math.min(clone.getThatFromPosition(), cur.getThatSnippetLocation()
 								.getPosition()));
 						clone.setThatLength(Math.max(clone.getThatLength(), cur.getThatSnippetLocation().getPosition()
 								- clone.getThatFromPosition() + 1));
 					}
 				} else {
 					break;
 				}
 				last = cur;
 			}
 			clones.add(finalizeClone(clone));
 		}
 		final Collection<Clone> builtClones = clones.build();
 		return builtClones;
 	}
 
 	/** The unprocessed matches still lack the popular rows. Weave them in here. */
 	private void weaveInPopularSnippets(LinkedList<SnippetMatch> unprocessedMatches) {
 		if (unprocessedMatches.isEmpty()) {
 			return;
 		}
 
 		final ByteBuffer thisFunction = unprocessedMatches.get(0).getThisSnippetLocation().getFunction()
 				.asReadOnlyByteBuffer();
 
 		if (!function2PopularSnippets.containsKey(thisFunction)) {
 			// Nothing to weave in.
 			return;
 		}
 
 		List<SnippetMatch> toBeWeavedIns = new ArrayList<>();
 		for (SnippetLocation thisLocation : function2PopularSnippets.get(thisFunction)) {
 			assert snippet2PopularSnippet.containsKey(thisLocation.getSnippet().asReadOnlyByteBuffer());
 			for (SnippetLocation thatLocation : snippet2PopularSnippet.get(thisLocation.getSnippet()
 					.asReadOnlyByteBuffer())) {
 				// The following three lines *must* match the test in
 				// MakeFunction2RoughClones.java
 				// The idea is that only clone a to b should be detected, not b
 				// to a.
 				if (thisFunction.compareTo(thatLocation.getFunction().asReadOnlyByteBuffer()) >= 0) {
 					continue;
 				}
 
 				toBeWeavedIns.add(SnippetMatch.newBuilder().setThisSnippetLocation(thisLocation)
 						.setThatSnippetLocation(thatLocation).build());
 			}
 		}
 
 		Collections.sort(toBeWeavedIns, snippetMatchComparator);
 
		final ListIterator<SnippetMatch> target = unprocessedMatches.listIterator();
		SnippetMatch cur = target.next();
 		for (SnippetMatch toBeWeavedIn : toBeWeavedIns) {
 			// Forward until we leave the right function or position is too big.
			while (target.hasNext()
					&& snippetMatchComparator.compare(toBeWeavedIn, cur) > 0) {
				cur = target.next();
 			}
 
 			// We're one step too far now, so walk back left, unless the
 			// insertion point is the last element
			if (snippetMatchComparator.compare(toBeWeavedIn, cur) <= 0) {
				target.previous();
 			}
 
			target.add(toBeWeavedIn);
 		}
 	}
 
 	private Clone finalizeClone(final Clone.Builder clone) {
 		clone.setThisLength(clone.getThisLength() + MINIMUM_LINES - 1);
 		clone.setThatLength(clone.getThatLength() + MINIMUM_LINES - 1);
 		return clone.build();
 	}
 
 	private Clone.Builder initializeClone(SnippetMatch firstMatch) {
 		final Clone.Builder clone = Clone.newBuilder();
 		clone.setThisFunction(firstMatch.getThisSnippetLocation().getFunction());
 		clone.setThatFunction(firstMatch.getThatSnippetLocation().getFunction());
 		clone.setThisFromPosition(firstMatch.getThisSnippetLocation().getPosition());
 		clone.setThatFromPosition(firstMatch.getThatSnippetLocation().getPosition());
 		clone.setThisLength(1);
 		clone.setThatLength(1);
 		return clone;
 	}
 }
