 /*
  * Hyperchron, a timeseries data management solution.
  * Copyright (C) 2011 Tobias Wegner
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package org.hyperchron.impl;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Random;
 import java.util.concurrent.atomic.AtomicLong;
 
 import org.hyperchron.TimeSeries;
 import org.hyperchron.impl.blocks.BlockStore;
 
 public class TimeSeriesImplementation implements TimeSeries {
 
 	Hashtable<Long, TimeSeriesIterator> iterators = new Hashtable<Long, TimeSeriesIterator>();
 	
 	Hashtable<String, EntityDescriptor> entityDescriptions = new Hashtable<String, EntityDescriptor>();
 	
 	ArrayList<String> entities = new ArrayList<String>();
 	
 	java.util.concurrent.atomic.AtomicLong entityID = new AtomicLong();	
 
 	public String tsFileDB = null;
 	
 	volatile boolean ShuttingDown = false;
 
 	public TimeSeriesImplementation() {		
 	}
 	
 	public void activate() {
		tsFileDB = System.getProperty("timeseries.blockfile");
 		if (tsFileDB == null)
 			tsFileDB = "D:\\Temp\\ts\\entities.db";		
 
 		File dbFile = new File(tsFileDB);
 		
 		try
 		{
 			BufferedReader br = new BufferedReader(new FileReader(dbFile));
 			
 			String line;
 			
 			while ((line = br.readLine()) != null) {
 				long ID = Long.parseLong(br.readLine());
 				if (line.equals("ENTITY_ID")) {				
 					entityID.set(ID);
 				} else {
 					long elementID = Long.parseLong(br.readLine());
 					
 					EntityDescriptor entityDescriptor = new EntityDescriptor(line, ID);
 					
 					entityDescriptor.setNextElementID(elementID);
 
 					Tree tree = new Tree(entityDescriptor);
 					entityDescriptor.tree = tree;
 
 					entityDescriptions.put(line, entityDescriptor);
 				}
 			}
 			
 			br.close();
 		} catch (Exception e) {
 			if (e instanceof FileNotFoundException) {
 				System.out.println ("Timeseries could not find entities, so create new db");
 			} else
 				e.printStackTrace();
 		}		
 	}
 	
 	public void deactivate() {
 		Shutdown();
 	}
 	
 	@Override
 	public long getIterator(String key) {
 		long newID = Math.abs( new Random().nextLong() );
 		
 		while (iterators.containsKey(new Long(newID))) {
 			newID = Math.abs( new Random().nextLong() );
 		}
 		
 		EntityDescriptor entityDescriptor = entityDescriptions.get(key);
 		
 		if (entityDescriptor == null)
 			return -1; //not known to db
 		
 		iterators.put(new Long(newID), new TimeSeriesIterator(entityDescriptor.tree));
 		
 		setIteratorAtBegin(newID);
 		
 		return newID;
 	}
 
 	@Override
 	public void releaseIterator(long Iterator) {
 		TimeSeriesIterator timeSeriesIterator = iterators.get(new Long(Iterator));
 
 		iterators.remove(timeSeriesIterator);
 	}
 
 	@Override
 	public void setIteratorAtBegin(long Iterator) {
 		TimeSeriesIterator timeSeriesIterator = iterators.get(new Long(Iterator));
 
 		timeSeriesIterator.currentLeaf = timeSeriesIterator.tree.getFirstLeaf();
 		timeSeriesIterator.currentIndex = 0;
 	}
 
 	@Override
 	public void setIteratorAtEnd(long Iterator) {
 		TimeSeriesIterator timeSeriesIterator = iterators.get(new Long(Iterator));
 
 		TreeLeaf lastLeaf = timeSeriesIterator.tree.getLastLeaf();
 
 		while (lastLeaf.length == 0) {
 			if (lastLeaf.previousSibling != null)
 				lastLeaf = lastLeaf.previousSibling;
 			else
 				break;
 		}			
 		
 		timeSeriesIterator.currentLeaf = lastLeaf;
 		timeSeriesIterator.currentIndex = lastLeaf.length - 1;
 	}
 
 	@Override
 	public long setIteratorAtRevision(long Iterator, long Revision) {
 		setIteratorAtEnd(Iterator);
 		
 		long currentRevision = 1;
 		
 		TimeSeriesIterator timeSeriesIterator = iterators.get(new Long(Iterator));
 
 		Revision--;
 		
 		while (Revision > 0)
 		{
 			if (Revision > timeSeriesIterator.currentIndex) {
 				Revision -= timeSeriesIterator.currentIndex;
 				currentRevision += timeSeriesIterator.currentIndex;
 				
 				timeSeriesIterator.currentIndex = 0;
 				
 				if (timeSeriesIterator.currentLeaf.previousSibling == null)
 					return currentRevision;
 				
 				timeSeriesIterator.currentLeaf = timeSeriesIterator.currentLeaf.previousSibling;
 				timeSeriesIterator.currentIndex = timeSeriesIterator.currentLeaf.length - 1; 
 			}
 			else
 			{
 				timeSeriesIterator.currentIndex -= (int)Revision;
 				currentRevision += Revision;
 				
 				Revision = 0;
 			}
 		}
 		
 		return currentRevision;
 	}
 
 	@Override
 	public void setIteratorAfterTimestamp(long Iterator, long Timestamp) {
 		TimeSeriesIterator timeSeriesIterator = iterators.get(new Long(Iterator));
 
 		Tree tree = timeSeriesIterator.tree;
 		
 //		TreeLeaf leaf = tree.GetLeafForTimestamp(Timestamp);
 //		tree.GetIndexForTimestamp(leaf, Timestamp);
 		
 //		TreeElement rootElement = entityDescriptions.get(timeSeriesIterator.key).rootElement;
 		
 		if (Timestamp < tree.GetEndingTimeStamp(tree.rootNode)) {	
 			timeSeriesIterator.currentLeaf = tree.GetLeafForTimestamp(Timestamp);
 			timeSeriesIterator.currentIndex = tree.GetIndexForTimestamp(timeSeriesIterator.currentLeaf, Timestamp);
 		} else {
 			setIteratorAtEnd(Iterator);
 		}
 	}
 
 	@Override
 	public long getIteratorRevision(long Iterator) {
 		TimeSeriesIterator timeSeriesIterator = iterators.get(new Long(Iterator));
 
 		return timeSeriesIterator.currentLeaf.startingRevision + timeSeriesIterator.currentIndex; 
 	}
 	
 	@Override
 	public void IteratorGoToPreviousRevision(long Iterator) {
 		TimeSeriesIterator timeSeriesIterator = iterators.get(new Long(Iterator));
 
 		timeSeriesIterator.previous();
 	}
 
 	@Override
 	public void IteratorGoToNextRevision(long Iterator) {
 		TimeSeriesIterator timeSeriesIterator = iterators.get(new Long(Iterator));
 
 		timeSeriesIterator.next();
 	}
 
 	@Override
 	public long getID(long Iterator) {
 		TimeSeriesIterator timeSeriesIterator = iterators.get(new Long(Iterator));
 
 		if ((timeSeriesIterator.currentIndex < 0) || (timeSeriesIterator.currentLeaf == null))
 			return -1;
 		
 		long ID = timeSeriesIterator.tree.GetIDForIndex(timeSeriesIterator.currentLeaf, timeSeriesIterator.currentIndex);
 		
 		return ID;
 		/*
 		 * For debug purpose return just ID
 		 */
 //		return new TimedValue(Long.toString(ID), timeSeriesIterator.tree.getTimeStampForIndex(timeSeriesIterator.currentLeaf, timeSeriesIterator.currentIndex));	
 		
 //		return new TimedValue(backend.Retrieve(timeSeriesIterator.tree.uuid + Long.toString(ID)), timeSeriesIterator.tree.getTimeStampForIndex(timeSeriesIterator.currentLeaf, timeSeriesIterator.currentIndex));	
 	}
 
 	@Override
 	public long getCurrentTime(long Iterator) {
 		TimeSeriesIterator timeSeriesIterator = iterators.get(new Long(Iterator));
 
 		if ((timeSeriesIterator.currentIndex < 0) || (timeSeriesIterator.currentLeaf == null))
 			return -1;
 		
 		long timestamp = timeSeriesIterator.tree.getTimeStampForIndex(timeSeriesIterator.currentLeaf, timeSeriesIterator.currentIndex);
 		
 		return timestamp;
 	}
 	
 	@Override
 	public long saveTimestamp(String key, long Timestamp) {
 		if (ShuttingDown)
 			return -1;
 		
 		EntityDescriptor entityDescriptor = entityDescriptions.get(key);
 		
 		if (entityDescriptor == null) {
 			entityDescriptor = new EntityDescriptor(key, entityID.getAndIncrement());
 
 			Tree tree = new Tree(entityDescriptor);
 			entityDescriptor.tree = tree;
 
 			entityDescriptions.put(key, entityDescriptor);
 		}
 
 		long ID = entityDescriptor.getNextElementID();
 		
 		ID = entityDescriptor.tree.SaveIDForTimestamp(ID, Timestamp);
 		
 		return ID;
 	}
 
 	@Override
 	public void Shutdown () {
 		if (ShuttingDown)
 			return;
 		
 		ShuttingDown = true;
 		
 		BlockStore.instance.Shutdown();
 		
 		File dbFile = new File(tsFileDB);
 		
 		try
 		{
 			BufferedWriter bw = new BufferedWriter(new FileWriter(dbFile));
 			
 			for (EntityDescriptor entityDescriptor : entityDescriptions.values()) {
 				bw.write(entityDescriptor.uuid + "\r\n" + entityDescriptor.entityID + "\r\n" + entityDescriptor.getNextElementID() + "\r\n");
 			}
 			
 			bw.write("ENTITY_ID\r\n" + entityID.get() + "\r\n");
 			
 			bw.flush();
 			
 			bw.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
