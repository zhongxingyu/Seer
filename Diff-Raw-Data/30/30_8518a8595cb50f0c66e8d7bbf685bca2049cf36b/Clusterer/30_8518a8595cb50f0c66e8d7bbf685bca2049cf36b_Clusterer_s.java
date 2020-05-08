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
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.lang3.time.StopWatch;
 import org.joda.time.DateTime;
 import org.thiesen.jiffs.data.db.dao.StoryClusterDAO;
 import org.thiesen.jiffs.data.db.dao.StoryDAO;
 import org.thiesen.jiffs.data.db.dbo.StoryClusterDBO;
 import org.thiesen.jiffs.data.db.dbo.StoryDBO;
 import org.thiesen.jiffs.jobs.Job;
 
 import com.google.common.base.Function;
 import com.google.common.base.Splitter;
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Multimap;
 
 public class Clusterer implements Job {
 
 
 	public static void main(final String...args ) {
 		final Clusterer job = new Clusterer( new StoryDAO(), new StoryClusterDAO() );
 
 		job.execute();
 	}
 
 	private final StoryDAO _storyDAO;
 	private final StoryClusterDAO _storyClusterDAO;
 
 	public Clusterer( final StoryDAO storyDAO, final StoryClusterDAO storyClusterDAO ) {
 		_storyDAO = storyDAO;
 		_storyClusterDAO = storyClusterDAO;
 	}
 
 	private final static ExecutorService EXECUTOR = Executors.newFixedThreadPool( Runtime.getRuntime().availableProcessors() );
 	public static final double BASE_SIMILARITY = 0.6;
 	private static final Function<StoryDBO, ClusterItem> TO_CLUSTER_ITEM = new Function<StoryDBO, ClusterItem>() {
 
 		@Override
 		public ClusterItem apply(StoryDBO story) {
 			return new ClusterItem( story.getStoryUri().toString() , 
 					ImmutableList.copyOf( Splitter.on(',').split( story.getPreprocessedText() ) ) );
 		}
 	
 	};
 
 	@Override
 	public void execute() {
 		final Map<String, Long> foundClusters = findClusters();
 		
 		final Multimap<Long, String> clusters = HashMultimap.create(); 
 		for ( final Map.Entry<String, Long> entry : foundClusters.entrySet() ) {
 			clusters.put( entry.getValue() , entry.getKey() );
 		}
 		
 		storeClusters( clusters );
 	
 	}
 
 	private Map<String, Long> findClusters() {
 		final Iterable<StoryDBO> unprocessed = _storyDAO.findForClustering();
 		final StopWatch watch = new StopWatch();
 		
 		watch.start();
 		
 		final Map<String, Long> foundClusters = Maps.newConcurrentMap();
 		final Semaphore maxEnqueedTasks = new Semaphore( 100000 );
		final List<ClusterItem> clusterItems = Lists.newArrayList( transform( unprocessed ) );
		for ( final ClusterItem firstItem : clusterItems ) {
 			for ( final ClusterItem secondItem : clusterItems ) {
				EXECUTOR.submit( new ClusterFinder( maxEnqueedTasks,foundClusters, firstItem, secondItem )  );
 				maxEnqueedTasks.acquireUninterruptibly();
				
 			}
 		}
		clusterItems.clear();
 		
 		EXECUTOR.shutdown();
 		
 		try {
 			EXECUTOR.awaitTermination( 1 , TimeUnit.DAYS );
 		} catch (InterruptedException e) {
 			Thread.interrupted();
 		}
 		watch.stop();
 		
 		System.out.println("Clustering took " + watch );
 		
 		return foundClusters;
 	}
 
 	private Iterable<ClusterItem> transform(Iterable<StoryDBO> unprocessed) {
 		return Iterables.transform( unprocessed, TO_CLUSTER_ITEM );
 	}
 
 	private void storeClusters(Multimap<Long, String> clusters) {
 		System.out.println("Found " + clusters.keySet().size() + " clusters");
 		
 		for ( final Entry<Long, Collection<String>> entry : clusters.asMap().entrySet() ) {
 			final StoryClusterDBO clusterDBO = new StoryClusterDBO();
 			
 			clusterDBO.setCreatedAt( new DateTime() );
 			clusterDBO.setStoryUris( Lists.newArrayList( entry.getValue() ) );
 			
 			_storyClusterDAO.insert( clusterDBO );
 			
 		}
 		
 	
 	}
 
 }
