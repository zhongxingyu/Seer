 /*
  * (c) Copyright 2010 Marcus Thiesen (marcus@thiesen.org)
  *
  *  This file is part of jiffs.
  *
  *  jiffs is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  jiffs is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with jiffs.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package org.thiesen.jiffs.jobs.clusterer;
 
 import gnu.trove.TObjectIntHashMap;
 
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.atomic.AtomicLong;
 
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 
 public class ClusterFinder implements Runnable {
 
 	private final static Object CLUSTER_LOCK = new Object();
 	private final static AtomicLong CLUSTER_COUNT = new AtomicLong();
 	
 	private Map<String, Long> _clusters;
 	private ClusterItem _firstItem;
 	private ClusterItem _secondItem;
 	private Semaphore _maxEnqueuedTasks;
 
 	public ClusterFinder(Semaphore maxEnqueedTasks, Map<String, Long> clusters,
 			ClusterItem firstItem,
 			ClusterItem secondItem ) {
 		_maxEnqueuedTasks = maxEnqueedTasks;
 		_clusters = clusters;
 		_firstItem = firstItem;
 		_secondItem = secondItem;
 	}
 
 
 	@Override
 	public void run() {
 		try {
 			findCluster();
 		} finally {
 			_maxEnqueuedTasks.release();
 		}
 		
 	}
 
 	private void findCluster() {
		if ( _firstItem == _secondItem ) {
			return;
		}
		
 		final double similarity = computeSimilarity();
 		
 		if ( similarity >= Clusterer.BASE_SIMILARITY ) {
 			final String firstUri = _firstItem.getUri();
 			final String secondUri = _secondItem.getUri();
 			synchronized ( CLUSTER_LOCK ) {
 				final Long clusterId = findClusterId( firstUri, secondUri );
 		
 				_clusters.put( firstUri, clusterId );
 				_clusters.put( secondUri, clusterId );
 		
 			}
 		}
 	}
 
 	private double computeSimilarity() {
 		final Iterable<String> firstWords = _firstItem.getWords();
 		final Iterable<String> secondWords = _secondItem.getWords();
 		
 		final Set<String> allWords = makeAllWordsSet(firstWords, secondWords);
 		final TObjectIntHashMap<String> wordsByPosition = makeWordsByBositionMap(allWords);
 		
 		final TermVector firstVector = makeVector(firstWords, allWords,
 				wordsByPosition);
 		final TermVector secondVector = makeVector(secondWords, allWords,
 				wordsByPosition);
 		
 	
 		final double similarity = firstVector.dot( secondVector );
 		return similarity;
 	}
 
 	private TermVector makeVector(final Iterable<String> firstWords,
 			final Set<String> allWords,
 			final TObjectIntHashMap<String> wordsByPosition) {
 		final TermVector firstVector = new TermVector( allWords.size() );
 		for ( final String word : firstWords ) {
 			firstVector.put( wordsByPosition.get( word ),  1.0D );
 		}
 		firstVector.normalize();
 		return firstVector;
 	}
 
 	private Set<String> makeAllWordsSet(final Iterable<String> firstWords,
 			final Iterable<String> secondWords) {
 		final Set<String> allWords = Sets.newTreeSet();
 		Iterables.addAll( allWords, firstWords );
 		Iterables.addAll( allWords, secondWords );
 		return allWords;
 	}
 
 	private TObjectIntHashMap<String> makeWordsByBositionMap(
 			final Set<String> allWords) {
 		final TObjectIntHashMap<String> wordsByPosition = new TObjectIntHashMap<String>();
 		int position = 1;
 		for ( final String word : allWords ) {
 			wordsByPosition.put( word , position++ );
 		}
 		return wordsByPosition;
 	}
 
 	private Long findClusterId(String firstUri, String secondUri) {
 		if ( _clusters.containsKey( firstUri ) ) {
 			return _clusters.get( firstUri );
 		}
 		if ( _clusters.containsKey( secondUri ) ) {
 			return _clusters.get( secondUri );
 		}
 	
 		return Long.valueOf( CLUSTER_COUNT.incrementAndGet() );
 	}
 
 }
