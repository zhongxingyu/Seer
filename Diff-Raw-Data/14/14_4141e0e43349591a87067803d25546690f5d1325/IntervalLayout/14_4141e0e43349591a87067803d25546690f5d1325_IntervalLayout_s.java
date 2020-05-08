 /*--------------------------------------------------------------------------
  *  Copyright 2009 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // utgb-core Project
 //
 // IntervalLayout.java
 // Since: 2010/05/27
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.gwt.utgb.client.canvas;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 import org.utgenome.gwt.utgb.client.bio.Interval;
 import org.utgenome.gwt.utgb.client.bio.OnGenome;
 import org.utgenome.gwt.utgb.client.bio.OnGenomeDataVisitorBase;
 import org.utgenome.gwt.utgb.client.bio.ReadCoverage;
 import org.utgenome.gwt.utgb.client.bio.SAMRead;
 import org.utgenome.gwt.utgb.client.bio.SAMReadPair;
 import org.utgenome.gwt.utgb.client.track.TrackWindow;
 
 /**
  * On-genome data layout
  * 
  * @author leo
  * 
  */
 public class IntervalLayout {
 
 	private boolean keepSpaceForLabels = true;
 	private boolean hasEnoughHeightForLabels = false;
 	private PrioritySearchTree<OnGenome> globalLayout = new PrioritySearchTree<OnGenome>();
 	private PrioritySearchTree<LocusLayout> localLayoutInView = new PrioritySearchTree<LocusLayout>();
 
 	private TrackWindow w;
 
 	public static class LocusLayout {
 		private OnGenome locus;
 		private int yOffset;
 		private int height = 1;
 
 		public LocusLayout(OnGenome locus, int yOffset) {
 			this.locus = locus;
 			this.yOffset = yOffset;
 		}
 
 		public LocusLayout(OnGenome locus, int yOffset, int height) {
 			this.locus = locus;
 			this.yOffset = yOffset;
 			this.height = height;
 		}
 
 		public int getHeight() {
 			return height;
 		}
 
 		public OnGenome getLocus() {
 			return locus;
 		}
 
 		public int getYOffset() {
 			return yOffset;
 		}
 
 		public int scaledHeight(int scale) {
 			return scaledHeight(yOffset, scale);
 		}
 
 		public static int scaledHeight(int y, int scale) {
 			return y * scale;
 		}
 
 	}
 
 	public IntervalLayout() {
 	}
 
 	public void setTrackWindow(TrackWindow window) {
 		this.w = window;
 	}
 
 	public static int estimiateLabelWidth(OnGenome l, int geneHeight) {
 		String name = l.getName();
 		int labelWidth = name != null ? (int) (name.length() * geneHeight * 0.9) : 0;
 		if (labelWidth > 150)
 			labelWidth = 150;
 		return labelWidth;
 	}
 
 	public List<OnGenome> activeReads() {
 		final ArrayList<OnGenome> activeData = new ArrayList<OnGenome>();
 		globalLayout.depthFirstSearch(new PrioritySearchTree.Visitor<OnGenome>() {
 			public void visit(OnGenome l) {
 				if (w.overlapWith(l))
 					activeData.add(l);
 			}
 		});
 		return activeData;
 	}
 
 	public static class IntervalRetriever extends OnGenomeDataVisitorBase {
 
 		public int start = -1;
 		public int end = -1;
 		public int height = 1;
 		public boolean isDefined = false;
 
 		public void clear() {
 			isDefined = false;
 			height = 1;
 		}
 
 		@Override
 		public void visitInterval(Interval interval) {
 			start = interval.getStart();
 			end = interval.getEnd();
 			isDefined = true;
 		}
 
 		@Override
 		public void visitSAMRead(SAMRead r) {
 			start = r.unclippedStart;
 			end = r.unclippedEnd;
 			isDefined = true;
 		}
 
 		@Override
 		public void visitReadCoverage(ReadCoverage readCoverage) {
 			this.height = Math.abs(readCoverage.maxHeight - readCoverage.minHeight);
 			visitInterval(readCoverage);
 		}
 
 		@Override
 		public void visitSAMReadPair(SAMReadPair pair) {
 			start = pair.getStart();
 			end = pair.getEnd();
 			isDefined = true;
 
 			if (pair.getFirst().unclippedSequenceHasOverlapWith(pair.getSecond())) {
 				height = 2;
 			}
 		}
 
 	}
 
 	private static class DepthMeasure implements PrioritySearchTree.ResultHandler<LocusLayout> {
 		int maxDepth = 0;
 
 		public void handle(LocusLayout l) {
 			if (maxDepth < l.getYOffset()) {
 				maxDepth = l.getYOffset();
 			}
 		}
 
 		public boolean toContinue() {
 			return true;
 		}
 	}
 
 	public int maxDepth(TrackWindow view) {
 		int x1 = pixelPositionOnWindow(view.getStartOnGenome());
 		int x2 = pixelPositionOnWindow(view.getEndOnGenome());
 		int maxDepth = 0;
 
 		DepthMeasure dm = new DepthMeasure();
 		localLayoutInView.rangeQuery(x1, Integer.MAX_VALUE, x2, dm);
 		return dm.maxDepth;
 	}
 
 	/**
 	 * Creates an X-Y layout of the given intervals
 	 * 
 	 * @param <T>
 	 * @param intervalList
 	 * @param geneHeight
 	 *            * @return
 	 */
 	<T extends OnGenome> void reset(List<T> intervalList, int geneHeight) {
 
 		globalLayout.clear();
 		IntervalRetriever ir = new IntervalRetriever();
 		for (OnGenome l : intervalList) {
 			ir.clear();
 			l.accept(ir);
 			if (!ir.isDefined)
 				continue;
 			globalLayout.insert(l, ir.end, ir.start);
 		}
 		createLocalLayout(geneHeight);
 	}
 
 	private class LayoutGenerator implements PrioritySearchTree.ResultHandler<OnGenome> {
 
 		private final int geneHeight;
 
 		IntervalRetriever ir = new IntervalRetriever();
 
 		private boolean toContinue = true;
 		boolean showLabelsFlag = keepSpaceForLabels;
 		boolean needRelayout = false;
 		int maxYOffset = 0;
 
 		public LayoutGenerator(int geneHeight) {
 			this.geneHeight = geneHeight;
 			reset();
 		}
 
 		/**
 		 * reset except the show lables flag
 		 */
 		public void reset() {
 			toContinue = true;
 			needRelayout = false;
 			maxYOffset = 0;
 		}
 
 		public void handle(OnGenome l) {
 			ir.clear();
 			l.accept(ir);
 			if (!ir.isDefined)
 				return;
 
 			int x1 = pixelPositionOnWindow(ir.start);
 			int x2 = pixelPositionOnWindow(ir.end);
 
 			if (showLabelsFlag) {
 				int labelWidth = estimiateLabelWidth(l, geneHeight);
 				if (x1 - labelWidth > 0)
 					x1 -= labelWidth;
 				else
 					x2 += labelWidth;
 			}
 
 			List<LocusLayout> activeLocus = localLayoutInView.rangeQuery(x1, Integer.MAX_VALUE, x2);
 
 			HashSet<Integer> filledY = new HashSet<Integer>();
 			// overlap test
 			for (LocusLayout al : activeLocus) {
 				for (int i = 0; i < al.getHeight(); i++)
 					filledY.add(al.yOffset + i);
 			}
 
 			int blankY = 0;
 			for (; filledY.contains(blankY); blankY++) {
 			}
 
 			localLayoutInView.insert(new LocusLayout(l, blankY, ir.height), x2, x1);
 			if (blankY > maxYOffset) {
 				maxYOffset = blankY;
 				if (showLabelsFlag && maxYOffset > 30) {
 					showLabelsFlag = false;
 					// reset the current layout, then create another layout without the read labels 
 					needRelayout = true;
 					toContinue = false;
 				}
 			}
 
 		}
 
 		public boolean toContinue() {
 			return toContinue;
 		}
 
 	}
 
 	/**
 	 * Creates an X-Y layout of the given intervals, then return the max depth of the intervals
 	 * 
 	 * @param <T>
 	 * @param intervalList
 	 * @param geneHeight
 	 *            * @return
 	 */
 	<T extends OnGenome> int createLocalLayout(final int geneHeight) {
 
 		//int maxYOffset = 0;
 
 		LayoutGenerator layoutGenerator = new LayoutGenerator(geneHeight);
 
 		boolean toContinue = false;
 		do {
 			localLayoutInView.clear();
 			layoutGenerator.reset();
 			globalLayout.rangeQuery(w.getStartOnGenome(), Integer.MAX_VALUE, w.getEndOnGenome(), layoutGenerator);
 			toContinue = layoutGenerator.needRelayout;
 		}
 		while (toContinue);
 
 		int maxYOffset = layoutGenerator.maxYOffset;
 		if (maxYOffset <= 0)
 			maxYOffset = 1;
 
 		hasEnoughHeightForLabels = layoutGenerator.showLabelsFlag;
 		return maxYOffset;
 	}
 
 	public void setKeepSpaceForLabels(boolean keep) {
 		this.keepSpaceForLabels = keep;
 	}
 
 	public boolean hasEnoughHeightForLabels() {
 		return hasEnoughHeightForLabels;
 	}
 
 	public void depthFirstSearch(PrioritySearchTree.Visitor<LocusLayout> visitor) {
 		localLayoutInView.depthFirstSearch(visitor);
 	}
 
 	/**
 	 * compute the overlapped intervals for the mouse over event
 	 * 
 	 * @param event
 	 * @param xBorder
 	 * @return
 	 */
 	public OnGenome overlappedInterval(int x, int y, int xBorder, int geneHeight) {
 
 		for (LocusLayout gl : localLayoutInView.rangeQuery(x, Integer.MAX_VALUE, x)) {
 			OnGenome g = gl.getLocus();
 			int y1 = gl.getYOffset() * geneHeight;
 			int y2 = y1 + geneHeight * gl.getHeight();
 
 			if (y1 <= y && y <= y2) {
 				int x1 = pixelPositionOnWindow(g.getStart()) - xBorder;
 				int x2 = pixelPositionOnWindow(g.getStart() + g.length()) + xBorder;
 
 				if (hasEnoughHeightForLabels) {
 					int labelWidth = estimiateLabelWidth(g, geneHeight);
 					if (x1 - labelWidth > 0)
 						x1 -= labelWidth;
 					else
 						x2 += labelWidth;
 				}
 
 				if (x1 <= x && x <= x2)
 					return g;
 			}
 		}
 		return null;
 
 	}
 
 	public int pixelPositionOnWindow(int indexOnGenome) {
 		return w.convertToPixelX(indexOnGenome);
 	}
 
 	public void clear() {
 		localLayoutInView.clear();
 		globalLayout.clear();
 	}
 
 }
