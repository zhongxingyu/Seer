 /*
 * Copyright 2010 Bizosys Technologies Limited
 *
 * Licensed to the Bizosys Technologies Limited (Bizosys) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Bizosys licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package com.bizosys.hsearch.functions;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import com.bizosys.hsearch.functions.GroupSortedObject.FieldType;
 
 
 public final class GroupSorter{
 	
 	public static class GroupSorterSequencer implements  Comparable<GroupSorterSequencer>{
 		FieldType fldType;
 		int fldSeq;
 		int sortSeq;
 		boolean isAsc;
 
 		public GroupSorterSequencer(FieldType fldType, int fldSeq, int sortSeq, boolean isAsc) {
 			this.fldType = fldType;
 			this.fldSeq = fldSeq;
 			this.sortSeq = sortSeq;
 			this.isAsc = isAsc;
 		}
 
 
 		@Override
 		public int compareTo(GroupSorterSequencer o) {
 			if ( this.sortSeq == o.sortSeq) return 0;
 			else if ( this.sortSeq > o.sortSeq) return 1;
 			else return -1;
 		}
 	}
 	
 	private List<GroupSorterSequencer> sortedSequences = new ArrayList<GroupSorterSequencer>();
 	
 	public void setSorter(FieldType fldType, int fldSeq, int sortSeq, boolean isAsc) {
 		sortedSequences.add(new GroupSorterSequencer(fldType, fldSeq, sortSeq, isAsc ));
 	}
 	
 	public void setSorter(GroupSorterSequencer seq) {
 		sortedSequences.add(seq);
 	}
 	
	
 	public void sort(GroupSortedObject[] objects) {
 		
 		Collections.sort(sortedSequences);
 		
 		GroupSorterSequencer[] sortedSequencesArr = new GroupSorterSequencer[sortedSequences.size()]; 
 		sortedSequences.toArray(sortedSequencesArr);
 		
 		for (GroupSortedObject groupSortedObject : objects) {
 			groupSortedObject.setSortedSequencers(sortedSequencesArr);
 		}
 		
 		Arrays.sort(objects);
 	}
 	
 }
