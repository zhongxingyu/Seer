 /**
  * Copyright 2012. Bjørn Remseth (rmz@rmz.no).
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package no.rmz.rmatch.impls;
 
 import no.rmz.rmatch.interfaces.MatchSet;
 import no.rmz.rmatch.interfaces.DFANode;
 import no.rmz.rmatch.interfaces.Regexp;
 import no.rmz.rmatch.interfaces.Match;
 import no.rmz.rmatch.interfaces.NodeStorage;
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.ConcurrentSkipListSet;
 import no.rmz.rmatch.utils.Counter;
 import no.rmz.rmatch.utils.Counters;
 
 /**
  * A an implementation of the MatchSet interface. A MatchSet keeps a set of
  * matches which starts from the same location in the input. The MatchSet will
  * initially contain several matches. As the matching process progressses fewer
  * and fewer matches will remain, and eventually they will all be removed either
  * when firing an action, or just removed since it is discovered that the match
  * can not be brought to be final and then executed..
  */
 public final class MatchSetImpl implements MatchSet {
 
     /**
      * The set of matches being pursued through this MatchSetImpl.
      */
     private Set<Match> matches =
             new ConcurrentSkipListSet<Match>(Match.COMPARE_BY_OBJECT_ID);
     /**
      * The current determinstic node that is used when pushing the matches
      * further.
      */
     private DFANode currentNode;
     /**
      * The start position of all the matches associated with this MatchSetImpl.
      */
     private int start;
     /**
      * An identifier uniquely identifying this MatchSetImpl among other
      * MatchSetImpl instances.
      */
     private long id;
     /**
      * A counter for MatchSetImpls.
      */
     private static final Counter MY_COUNTER =
             Counters.newCounter("MatchSetImpl");
 
     @Override
     public int getStart() {
         return start;
     }
 
     /**
      * Create a new MatchSetImpl.
      *
      * @param startIndex The start position in the input.
      * @param startNode The deterministic start node to start with.
      */
     public MatchSetImpl(
             final int startIndex,
             final DFANode startNode) {
         checkNotNull(startNode, "Startnode can't be null");
         checkArgument(startIndex >= 0, "Start index can't be negative");
         currentNode = startNode;
         start = startIndex;
         id = MY_COUNTER.inc();
         addMatches(startNode);
     }
 
     /**
      * Populate the set of matches with matches that could possibly start from
      * startNode starting point.
      *
      * @param startNode the to populate the initial set of matches from.
      */
     private void addMatches(final DFANode startNode) {
         assert (startNode != null);
 
         for (final Regexp r : currentNode.getRegexps()) {
 
             matches.add(startNode.newMatch(this, r));
         }
 
         // This was necessary to nail the bug caused by the natural
         // comparison for matches not being by id. Don't want to
         // see that again so I'm keeping the assertion.
         assert (matches.size() == currentNode.getRegexps().size());
 
     }
 
     @Override
     public Set getMatches() {
         return matches;
     }
 
     @Override
     public boolean hasMatches() {
         return !matches.isEmpty();
     }
 
     /**
      * Progress one, if any matches are inactivated they are removed from the
      * match set. If they have something to contribute they are committed to
      * runnableMatches too.
      *
      * @param ns A NodeStorage used to find new nodes.
      * @param currentChar The currenc character.
      * @param currentPos The current position.
      * @param runnableMatches a container for runnable matches. Matches will
      *        be put here if they can be run, possibly, pending domination
      *        stuff.
      */
     @Override
     public void progress(
             final NodeStorage ns,
             final Character currentChar,
             final int currentPos,
             final RunnableMatchesHolder runnableMatches) {
 
         checkNotNull(ns, "NodeStorage can't be null");
         checkNotNull(currentChar, "currentChar can't be null");
         checkArgument(currentPos >= 0, "currentPos must be non-negative");
         checkNotNull(runnableMatches, "runnableMatches can't be null");
         checkNotNull(currentNode,
                 "currentNode can never be null when progressing");
 
         // If no matches are active, then there is nothing to do
         // so just return.
         if (!hasMatches()) {
             return;
         }
 
         // This nested if/for/if statement takes
         // care of all the circumstances
 
         currentNode = currentNode.getNext(currentChar, ns);
 
         if (currentNode == null) {
             // Found no nodes going out of the current node, so we have
             // to stop pursuing the matches we've already got.
             // This actually marks the MatchSetImpl instance for
             // destruction, but we won't do anything more about that fact
             // from within this loop.
 
             for (final Match m : matches) {
                 m.setInactive();
                 if (m.isFinal()) {
                     commitMatch(m, runnableMatches);
                     if (!m.isAbandoned()) {
                         m.abandon();
                     }
                 }
                 removeMatch(m);
             }
             return;
         }
 
         // Check if there are any regexps for which matches must fail
         // for this node, and fail them.
         if (currentNode.failsSomeRegexps()) {
             for (final Match m : matches) {
                 if (currentNode.isFailingFor(m.getRegexp())) {
                     m.abandon();
                     matches.remove(m);
                 }
             }
         }
 
         // got a current  node, so we'll se what we can do to progress
         // the matches we've got.
         for (final Match m : matches) {
 
             // Get the regexp associated with the
             // match we're currently processing.
             final Regexp regexp = m.getRegexp();
             final boolean isActive = currentNode.isActiveFor(regexp);
 
             m.setActive(isActive);
 
             // If this node is active for the current regexp,
             // that means that we don't have to abandon
             if (!isActive) {
 
                 // Ok, we can't continue this match, perhaps it's already
                 // final, and in that case we should commit what we've got
                 // before abandoning it.
                 if (m.isFinal()) {
                     commitMatch(m, runnableMatches);
                 }
 
                 if (!m.isAbandoned()) {
                     m.abandon();
                 }
 
                 removeMatch(m);
             } else {
 
                 // Mmkay, this is an active match and we have somewhere
                 // to progress to, so we're advancing the end position of
                 // the match by one.
                 m.setEnd(currentPos);
 
                 final boolean isFinal = currentNode.isTerminalFor(regexp);
                 // If we're also in a final position for this match, note that
                 // fact so that we can trigger actions for this match.
                 m.setFinal(isFinal);
             }
         }
     }
 
     /**
      * Commit this match relative to a bunch of other matches.
      *
      * Now committing simply means adding this match to a collection of matches
      * given as parameters.
      *
      * However, the current match is only added to the collection of runnable
      * matches if it's dominating the regular expression it's representing
      *
      * If the current match is dominating it's regular expression, then add it
      * to the set of runnable matches given as parameter.
      *
      * This method is public only to facilitate testing. It's not part of any
      * interface and shouldn't be used directly anywhere.
      *
      * @param m the match to commit.
      * @param runnableMatches a collector of runnable matches
      */
     public static void commitMatch(
             final Match m,
             final RunnableMatchesHolder runnableMatches) {
         assert (!m.isActive());
         assert (m.isFinal());
 
         final boolean isDominating =
                 m.getRegexp().isDominating(m);
         final boolean isStronglyDominating =
                 m.getRegexp().isStronglyDominated(m);
 
         if (isDominating && !isStronglyDominating) {
             runnableMatches.add(m);
             m.getMatchSet().removeMatch(m);
         }
     }
 
     @Override
     public void removeMatch(final Match m) {
         assert m != null;
         m.getRegexp().abandonMatchSet(this);
         matches.remove(m);
     }
 
     @Override
     public void finalCommit(final RunnableMatchesHolder runnableMatches) {
         checkNotNull(runnableMatches, "Target can't be null");
 
         final Set<Regexp> visitedRegexps = new HashSet<Regexp>();
 
         for (final Match m : matches) {
             // We can't commit what isn't final or is still active
             if (!m.isFinal() || m.isActive()) {
                 continue;
             }
             final Regexp r = m.getRegexp();
             if (!visitedRegexps.contains(r)) {
                 visitedRegexps.add(r);
                 r.commitUndominated(runnableMatches);
             }
             removeMatch(m);
         }
     }
 
     @Override
     public long getId() {
         return id;
     }
 }
