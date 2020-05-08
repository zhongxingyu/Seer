 /*--------------------------------------------------------------------------
  *  Copyright 2008 utgenome.org
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
 // GWTGenomeCanvas.java
 // Since: Jul 8, 2008
 //
 //--------------------------------------
 package org.utgenome.gwt.utgb.client.canvas;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.utgenome.gwt.utgb.client.UTGBClientException;
 import org.utgenome.gwt.utgb.client.bio.CDS;
 import org.utgenome.gwt.utgb.client.bio.CIGAR;
 import org.utgenome.gwt.utgb.client.bio.Exon;
 import org.utgenome.gwt.utgb.client.bio.Gene;
 import org.utgenome.gwt.utgb.client.bio.GraphData;
 import org.utgenome.gwt.utgb.client.bio.InfoSilkGenerator;
 import org.utgenome.gwt.utgb.client.bio.Interval;
 import org.utgenome.gwt.utgb.client.bio.OnGenome;
 import org.utgenome.gwt.utgb.client.bio.OnGenomeDataVisitor;
 import org.utgenome.gwt.utgb.client.bio.OnGenomeDataVisitorBase;
 import org.utgenome.gwt.utgb.client.bio.Read;
 import org.utgenome.gwt.utgb.client.bio.ReadCoverage;
 import org.utgenome.gwt.utgb.client.bio.ReferenceSequence;
 import org.utgenome.gwt.utgb.client.bio.SAMRead;
 import org.utgenome.gwt.utgb.client.bio.SAMReadPair;
 import org.utgenome.gwt.utgb.client.canvas.IntervalLayout.IntervalRetriever;
 import org.utgenome.gwt.utgb.client.canvas.IntervalLayout.LocusLayout;
 import org.utgenome.gwt.utgb.client.track.TrackGroup;
 import org.utgenome.gwt.utgb.client.track.TrackWindow;
 import org.utgenome.gwt.utgb.client.ui.FixedWidthLabel;
 import org.utgenome.gwt.utgb.client.ui.RoundCornerFrame;
 import org.utgenome.gwt.utgb.client.util.BrowserInfo;
 import org.utgenome.gwt.utgb.client.util.Optional;
 import org.utgenome.gwt.widget.client.Style;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.ImageElement;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.widgetideas.graphics.client.CanvasGradient;
 import com.google.gwt.widgetideas.graphics.client.Color;
 import com.google.gwt.widgetideas.graphics.client.GWTCanvas;
 import com.google.gwt.widgetideas.graphics.client.ImageLoader;
 import com.google.gwt.widgetideas.graphics.client.ImageLoader.CallBack;
 
 /**
  * Browser-side graphic canvas for drawing gene objects
  * 
  * @author leo
  * 
  */
 public class GWTGenomeCanvas extends Composite {
 
 	private int defaultGeneHeight = 12;
 	private int defaultMinGeneHeight = 2;
 
 	private int geneHeight = defaultGeneHeight;
 	private int geneMargin = 2;
 
 	private boolean reverse = false;
 
 	// widget
 	private GWTCanvas canvas = new GWTCanvas();
 	private AbsolutePanel basePanel = new AbsolutePanel();
 	private AbsolutePanel panel = new AbsolutePanel();
 	private static PopupInfo popupLabel = new PopupInfo();
 	private LocusClickHandler clickHandler = null;
 
 	private IntervalLayout intervalLayout = new IntervalLayout();
 	private TrackWindow trackWindow;
 
 	public static enum CoverageStyle {
 		DEFAULT, SMOOTH
 	};
 
 	private CoverageStyle coverageStyle = CoverageStyle.DEFAULT;
 
 	private List<Widget> readLabels = new ArrayList<Widget>();
 
 	private TrackGroup trackGroup;
 
 	public GWTGenomeCanvas() {
 
 		initWidget();
 	}
 
 	public void setCoverageStyle(String style) {
 		if (style == null)
 			return;
 
 		CoverageStyle s = CoverageStyle.valueOf(CoverageStyle.class, style.toUpperCase());
 		if (s != null) {
 			coverageStyle = s;
 		}
 
 	}
 
 	static class PopupInfo extends PopupPanel {
 
 		OnGenome locus;
 		private FlexTable infoTable = new FlexTable();
 
 		public PopupInfo() {
 			super(true);
 
 			RoundCornerFrame infoFrame = new RoundCornerFrame("336699", 0.7f, 4);
 			infoFrame.setWidget(infoTable);
 			this.setWidget(infoFrame);
 		}
 
 		public void setLocus(OnGenome g) {
 			if (this.locus == g)
 				return;
 
 			this.locus = g;
 
 		}
 
 		public void update() {
 			if (locus == null)
 				return;
 
 			InfoSilkGenerator silk = new InfoSilkGenerator();
 			locus.accept(silk);
 			infoTable.clear();
 			final int numRowsInCol = 12;
 			final List<String> lines = silk.getLines();
 			final int cols = lines.size() / numRowsInCol + (lines.size() % numRowsInCol != 0 ? 1 : 0);
 			for (int col = 0; col < cols; col++) {
 				VerticalPanel p = new VerticalPanel();
 				Style.padding(p, Style.LEFT | Style.RIGHT, 5);
 				Style.fontColor(p, "white");
 				Style.fontSize(p, 14);
 				Style.margin(p, 0);
 				Style.preserveWhiteSpace(p);
 
 				for (int i = 0; i < numRowsInCol; i++) {
 					int index = numRowsInCol * col + i;
 					if (index >= lines.size())
 						break;
 					p.add(new HTML(lines.get(index)));
 				}
 				infoTable.setWidget(0, col, p);
 			}
 		}
 
 	}
 
 	public void setLocusClickHandler(LocusClickHandler handler) {
 		this.clickHandler = handler;
 	}
 
 	@Override
 	public void onBrowserEvent(Event event) {
 
 		super.onBrowserEvent(event);
 
 		int type = DOM.eventGetType(event);
 		switch (type) {
 		case Event.ONMOUSEOVER:
 
 			break;
 		case Event.ONMOUSEMOVE: {
 			// show readLabels 
 			OnGenome g = overlappedInterval(event, 2);
 			if (g != null) {
 				if (popupLabel.locus != g) {
 
 					Style.cursor(canvas, Style.CURSOR_POINTER);
 
 					int clientX = DOM.eventGetClientX(event) + Window.getScrollLeft();
 					int clientY = DOM.eventGetClientY(event) + Window.getScrollTop();
 					displayInfo(clientX, clientY, g);
 				}
 			}
 			else {
 				if (dragStartPoint.isDefined()) {
 					// scroll the canvas
 					int clientX = DOM.eventGetClientX(event) + Window.getScrollLeft();
 					int clientY = DOM.eventGetClientY(event) + Window.getScrollTop();
 
 					DragPoint p = dragStartPoint.get();
 					int xDiff = clientX - p.x;
 					//int yDiff = clientY - p.y;
 					basePanel.setWidgetPosition(panel, xDiff, 0);
 				}
 				else {
 					Style.cursor(canvas, Style.CURSOR_AUTO);
 					popupLabel.setLocus(null);
 				}
 			}
 
 			break;
 		}
 		case Event.ONMOUSEOUT: {
 			resetDrag(event);
 			break;
 		}
 		case Event.ONMOUSEDOWN: {
 			// invoke a click event 
 			int clientX = DOM.eventGetClientX(event) + Window.getScrollLeft();
 			int clientY = DOM.eventGetClientY(event) + Window.getScrollTop();
 			OnGenome g = overlappedInterval(event, 2);
 			if (g != null) {
 				if (clickHandler != null)
 					clickHandler.onClick(clientX, clientY, g);
 				event.preventDefault();
 			}
 			else if (dragStartPoint.isUndefined()) {
 				dragStartPoint.set(new DragPoint(clientX, clientY));
 				Style.cursor(canvas, Style.CURSOR_RESIZE_E);
 			}
 
 			break;
 		}
 		case Event.ONMOUSEUP: {
 
 			resetDrag(event);
 			break;
 		}
 		}
 
 	}
 
 	private void resetDrag(Event event) {
 
 		int clientX = DOM.eventGetClientX(event) + Window.getScrollLeft();
 		int clientY = DOM.eventGetClientY(event) + Window.getScrollTop();
 
 		if (dragStartPoint.isDefined() && trackWindow != null) {
 			DragPoint p = dragStartPoint.get();
 			int startDiff = trackWindow.convertToGenomePosition(clientX) - trackWindow.convertToGenomePosition(p.x);
 			if (startDiff != 0) {
 				int newStart = trackWindow.getStartOnGenome() - startDiff;
 				if (newStart < 1)
 					newStart = 1;
 				int newEnd = newStart + trackWindow.getSequenceLength();
 				TrackWindow newWindow = trackWindow.newWindow(newStart, newEnd);
 				if (trackGroup != null)
 					trackGroup.setTrackWindow(newWindow);
 			}
 		}
 
 		dragStartPoint.reset();
 		event.preventDefault();
 
 		Style.cursor(canvas, Style.CURSOR_AUTO);
 	}
 
 	public static class DragPoint {
 		public final int x;
 		public final int y;
 
 		public DragPoint(int x, int y) {
 			this.x = x;
 			this.y = y;
 		}
 	}
 
 	private Optional<DragPoint> dragStartPoint = new Optional<DragPoint>();
 
 	public void displayInfo(final int clientX, final int clientY, final OnGenome g) {
 		if (popupLabel == null)
 			popupLabel = new PopupInfo();
 
 		popupLabel.setLocus(g);
 
 		Timer timer = new Timer() {
 			@Override
 			public void run() {
 				popupLabel.removeFromParent();
 				if (popupLabel.locus == g) {
 
 					int x = clientX + 10;
 					int y = clientY + 3;
 					final int w = Window.getClientWidth();
 					final int h = Window.getClientHeight();
 					final int xMax = w - 300;
 					//final int yMax = Math.max(h - 200 + Window.getScrollTop(), Window.getScrollTop());
 
 					if (x > xMax)
 						x = xMax;
 					//					if (y > yMax)
 					//						y = yMax;
 
 					popupLabel.setPopupPosition(x, y);
 					popupLabel.update();
 					popupLabel.show();
 				}
 			}
 		};
 
 		timer.schedule(100);
 	}
 
 	public void setShowLabels(boolean show) {
 		intervalLayout.setKeepSpaceForLabels(show);
 	}
 
 	/**
 	 * compute the overlapped intervals for the mouse over event
 	 * 
 	 * @param event
 	 * @param xBorder
 	 * @return
 	 */
 	private OnGenome overlappedInterval(Event event, int xBorder) {
 
 		int h = getReadHeight();
 		int x = drawPosition(getXOnCanvas(event));
 		int y = getYOnCanvas(event);
 
 		OnGenome g = intervalLayout.overlappedInterval(x, y, xBorder, h);
 
 		return g;
 	}
 
 	public int getXOnCanvas(Event event) {
 		int clientX = DOM.eventGetClientX(event);
 		return clientX - canvas.getAbsoluteLeft() + Window.getScrollLeft();
 	}
 
 	public int getYOnCanvas(Event event) {
 		int clientY = DOM.eventGetClientY(event);
 		return clientY - canvas.getAbsoluteTop() + Window.getScrollTop();
 	}
 
 	private void initWidget() {
 		panel.add(canvas, 0, 0);
 		basePanel.add(panel, 0, 0);
 		initWidget(basePanel);
 
 		sinkEvents(Event.ONMOUSEMOVE | Event.ONMOUSEOVER | Event.ONMOUSEDOWN | Event.ONMOUSEUP | Event.ONMOUSEOUT);
 	}
 
 	private boolean hasCache = false;
 	private TrackWindow prefetchWindow;
 
 	public boolean hasCacheCovering(TrackWindow newWindow) {
 		return prefetchWindow != null && hasCache && prefetchWindow.contains(newWindow);
 	}
 
 	public TrackWindow getPrefetchWindow() {
 		return prefetchWindow;
 	}
 
 	private float DEFAULT_PREFETCH_FACTOR = 1.0f;
 	private float PREFETCH_FACTOR = DEFAULT_PREFETCH_FACTOR;
 
 	public void resetPrefetchFactor() {
 		this.PREFETCH_FACTOR = DEFAULT_PREFETCH_FACTOR;
 	}
 
 	public float getPrefetchFactor() {
 		return this.PREFETCH_FACTOR;
 	}
 
 	public void setPrefetchFactor(float factor) {
 		if (factor <= 0.3f)
 			factor = 0.3f;
 		this.PREFETCH_FACTOR = factor;
 	}
 
 	/**
 	 * @param w
 	 */
 	public void setTrackWindow(TrackWindow w, boolean resetPrefetchWindow) {
 
 		if (resetPrefetchWindow || !hasCacheCovering(w)) {
 			int prefetchStart = w.getStartOnGenome() - (int) (w.getSequenceLength() * PREFETCH_FACTOR);
 			int prefetchEnd = w.getEndOnGenome() + (int) (w.getSequenceLength() * PREFETCH_FACTOR);
 			if (prefetchStart <= 0) {
 				prefetchStart = 1;
 				prefetchEnd = w.getEndOnGenome() + (int) (w.getSequenceLength() * PREFETCH_FACTOR * 2);
 			}
 			int prefetchPixelSize = (int) (w.getPixelWidth() * (1 + PREFETCH_FACTOR * 2));
 			prefetchWindow = new TrackWindow(prefetchPixelSize, prefetchStart, prefetchEnd);
 			hasCache = false;
 		}
 
 		if (trackWindow != null) {
 			if (!trackWindow.hasSameScaleWith(w)) {
 				imageACGT = null;
 			}
 		}
 
 		this.trackWindow = w;
 		reverse = w.isReverseStrand();
 
 		intervalLayout.setTrackWindow(w);
 	}
 
 	public void setTrackGroup(TrackGroup trackGroup) {
 		this.trackGroup = trackGroup;
 	}
 
 	public int pixelPositionOnWindow(int indexOnGenome) {
 		return trackWindow.convertToPixelX(indexOnGenome);
 	}
 
 	public void clearWidgets() {
 		canvas.clear();
 		//imageACGT = null;
 
 		if (popupLabel != null)
 			popupLabel.removeFromParent();
 
 		for (Widget w : readLabels) {
 			w.removeFromParent();
 		}
 		readLabels.clear();
 		basePanel.setWidgetPosition(panel, 0, 0);
 	}
 
 	public void clear() {
 		clearWidgets();
 		prefetchWindow = null;
 		hasCache = false;
 		intervalLayout.clear();
 	}
 
 	public static int width(int x1, int x2) {
 		return (x1 < x2) ? x2 - x1 : x1 - x2;
 	}
 
 	private final int FONT_WIDTH = 7;
 
 	private static final String DEFAULT_COLOR_A = "#50B6E8";
 	private static final String DEFAULT_COLOR_C = "#E7846E";
 	private static final String DEFAULT_COLOR_G = "#84AB51";
 	private static final String DEFAULT_COLOR_T = "#FFA930";
 	private static final String DEFAULT_COLOR_N = "#FFFFFF";
 
 	private static final float repeatColorAlpha = 0.25f;
 	private static final Color[] colors = { getColor(DEFAULT_COLOR_A, 1.0f), getColor(DEFAULT_COLOR_C, 1.0f), getColor(DEFAULT_COLOR_G, 1.0f),
 			getColor(DEFAULT_COLOR_T, 1.0f), getColor(DEFAULT_COLOR_A, repeatColorAlpha), getColor(DEFAULT_COLOR_C, repeatColorAlpha),
 			getColor(DEFAULT_COLOR_G, repeatColorAlpha), getColor(DEFAULT_COLOR_T, repeatColorAlpha), getColor(DEFAULT_COLOR_N, 1.0f) };
 
 	private int getReadHeight() {
 		return geneHeight + geneMargin;
 	}
 
 	class ReadPainter implements OnGenomeDataVisitor {
 
 		private LocusLayout gl;
 		private int h = getReadHeight();
 
 		public void setLayoutInfo(LocusLayout layout) {
 			this.gl = layout;
 		}
 
 		public int getYPos() {
 			return gl.scaledHeight(h);
 		}
 
 		public int getYPos(int y) {
 			return LocusLayout.scaledHeight(y, h);
 		}
 
 		public void visitGene(Gene g) {
 			int gx1 = pixelPositionOnWindow(g.getStart());
 			int gx2 = pixelPositionOnWindow(g.getEnd());
 
 			int geneWidth = gx2 - gx1;
 			if (geneWidth <= 10) {
 				draw(g, getYPos());
 			}
 			else {
 				CDS cds = g.getCDS().size() > 0 ? g.getCDS().get(0) : null;
 				draw(g, g.getExon(), cds, getYPos());
 			}
 
 			drawLabel(g);
 		}
 
 		public void drawBases(int startOnGenome, int y, String seq) {
 
 			int pixelWidthOfBase = (int) (trackWindow.getPixelLengthPerBase() + 0.1d);
 
 			if (imageACGT == null) {
 				GWT.log("font image is not loaded");
 				return;
 			}
 
 			for (int i = 0; i < seq.length(); i++) {
 				int baseIndex = 8;
 				switch (seq.charAt(i)) {
 				case 'A':
 					baseIndex = 0;
 					break;
 				case 'C':
 					baseIndex = 1;
 					break;
 				case 'G':
 					baseIndex = 2;
 					break;
 				case 'T':
 					baseIndex = 3;
 					break;
 				case 'a':
 					baseIndex = 4;
 					break;
 				case 'c':
 					baseIndex = 5;
 					break;
 				case 'g':
 					baseIndex = 6;
 					break;
 				case 't':
 					baseIndex = 7;
 					break;
 				case 'N':
 					baseIndex = 8;
 					break;
 				default:
 					continue;
 				}
 
 				double x1 = trackWindow.convertToPixelXDouble(startOnGenome + i);
 				double x2 = trackWindow.convertToPixelXDouble(startOnGenome + i + 1);
 
 				canvas.saveContext();
 				//canvas.setFillStyle(colors[baseIndex]);
 				//canvas.fillRect(x1, y, x2 - x1, geneHeight);
 
 				int h = imageACGT.getHeight();
 				if (h >= geneHeight)
 					h = geneHeight;
 
 				canvas.drawImage(imageACGT, pixelWidthOfBase * baseIndex, 0, pixelWidthOfBase, h, (int) x1, y, pixelWidthOfBase, h);
 				canvas.restoreContext();
 			}
 
 		}
 
 		private void drawLabel(OnGenome r, int y) {
 			if (!intervalLayout.hasEnoughHeightForLabels())
 				return;
 
 			IntervalRetriever ir = new IntervalRetriever();
 			r.accept(ir);
 
 			int gx1 = pixelPositionOnWindow(ir.start);
 			int gx2 = pixelPositionOnWindow(ir.end);
 
 			String n = r.getName();
 			if (n != null) {
 				int textWidth = IntervalLayout.estimiateLabelWidth(r, geneHeight);
 
 				Widget label = new FixedWidthLabel(n, textWidth);
 				Style.fontSize(label, geneHeight);
 				Style.fontColor(label, getExonColorText(r));
 
 				Style.verticalAlign(label, "middle");
 
 				int yPos = y - 1;
 
 				if (gx1 - textWidth < 0) {
 					if (reverse) {
 						Style.textAlign(label, "right");
 						panel.add(label, drawPosition(gx2) - textWidth - 1, yPos);
 					}
 					else {
 						Style.textAlign(label, "left");
 						panel.add(label, drawPosition(gx2) + 1, yPos);
 					}
 				}
 				else {
 					if (reverse) {
 						Style.textAlign(label, "left");
 						panel.add(label, drawPosition(gx1) + 1, yPos);
 					}
 					else {
 						Style.textAlign(label, "right");
 						panel.add(label, drawPosition(gx1) - textWidth - 1, yPos);
 					}
 				}
 
 				readLabels.add(label);
 			}
 
 		}
 
 		private void drawLabel(OnGenome r) {
 			drawLabel(r, getYPos());
 		}
 
 		public void visitInterval(Interval interval) {
 			draw(interval, getYPos());
 		}
 
 		public void visitRead(Read r) {
 			draw(r, getYPos());
 			drawLabel(r);
 		}
 
 		public void visitSAMReadPair(SAMReadPair pair) {
 
 			SAMRead first = pair.getFirst();
 			SAMRead second = pair.getSecond();
 
 			int y1 = getYPos();
 			int y2 = y1;
 
 			if (first.unclippedSequenceHasOverlapWith(second)) {
 				if (first.unclippedStart > second.unclippedStart) {
 					SAMRead tmp = first;
 					first = second;
 					second = tmp;
 				}
 				y2 = getYPos(gl.getYOffset() + 1);
 			}
 			else {
 				drawPadding(pixelPositionOnWindow(first.unclippedEnd), pixelPositionOnWindow(second.unclippedStart), y1, getColor("#666666", 0.8f), true);
 			}
 
 			visitSAMRead(first, y1, true);
 			visitSAMRead(second, y2, true);
 		}
 
 		public void visitSAMRead(SAMRead r) {
 			visitSAMRead(r, getYPos(), true);
 		}
 
 		public void visitSAMRead(SAMRead r, int y, boolean drawLabel) {
 
 			try {
 				int cx1 = pixelPositionOnWindow(r.unclippedStart);
 				int cx2 = pixelPositionOnWindow(r.unclippedEnd);
 
 				int gx1 = pixelPositionOnWindow(r.getStart());
 				int gx2 = pixelPositionOnWindow(r.getEnd());
 
 				int width = gx2 - gx1;
 
 				if ((cx2 - cx1) <= 5) {
 					// when the pixel range is narrow, draw a rectangle only 
 					draw(r, y);
 				}
 				else {
 
 					boolean drawBase = trackWindow.getSequenceLength() <= (trackWindow.getPixelWidth() / FONT_WIDTH);
 
 					CIGAR cigar = new CIGAR(r.cigar);
 					int readStart = r.getStart();
 					int seqIndex = 0;
 
 					for (int cigarIndex = 0; cigarIndex < cigar.size(); cigarIndex++) {
 						CIGAR.Element e = cigar.get(cigarIndex);
 
 						int readEnd = readStart + e.length;
 						int x1 = pixelPositionOnWindow(readStart);
 						switch (e.type) {
 						case Deletions:
 							// ref : AAAAAA
 							// read: ---AAA
 							// cigar: 3D3M
 							drawPadding(x1, pixelPositionOnWindow(readEnd), y, getCDSColor(r), true);
 							break;
 						case Insertions:
 							// ref : ---AAA
 							// read: AAAAAA
 							// cigar: 3I3M
 							readEnd = readStart;
 							seqIndex += e.length;
 							drawGeneRect(x1, pixelPositionOnWindow(readStart) + 1, y, getColor("#FFAAFF", 0.8f), false);
 							break;
 						case Padding:
 							// ref : AAAAAA
 							// read: ---AAA
 							// cigar: 3P3M
 							readEnd = readStart;
 							drawPadding(x1, pixelPositionOnWindow(readStart) + 1, y, getColor("#CCCCCC", 0.8f), false);
 							break;
 						case Matches: {
 							int x2 = pixelPositionOnWindow(readEnd);
 
 							if (drawBase) {
 								//drawGeneRect(x1, x2, y, getCDSColor(r, 0.3f), true);
 								drawBases(readStart, y, r.seq.substring(seqIndex, seqIndex + e.length));
 							}
 							else {
 								drawGeneRect(x1, x2, y, getCDSColor(r), true);
 							}
 
 							seqIndex += e.length;
 						}
 							break;
 						case SkippedRegion:
 							drawPadding(x1, pixelPositionOnWindow(readEnd), y, getCDSColor(r), true);
 							break;
 						case SoftClip: {
 							int softclipStart = cigarIndex == 0 ? readStart - e.length : readStart;
 							int softclipEnd = cigarIndex == 0 ? readStart : readStart + e.length;
 							readEnd = softclipEnd;
 
 							int x0 = pixelPositionOnWindow(softclipStart);
 							x1 = pixelPositionOnWindow(softclipEnd);
 
 							if (drawBase) {
 								drawBases(softclipStart, y, r.seq.substring(seqIndex, seqIndex + e.length).toLowerCase());
 							}
 							else {
 								drawGeneRect(x0, x1, y, getCDSColor(r, 0.2f), true);
 							}
 
 							seqIndex += e.length;
 						}
 							break;
 						case HardClip:
 							break;
 						}
 						readStart = readEnd;
 					}
 				}
 
 			}
 			catch (UTGBClientException e) {
 				// when parsing CIGAR string fails, simply draw a rectangle
 				draw(r, y);
 			}
 
 			if (drawLabel)
 				drawLabel(r, y);
 		}
 
 		public void visitSequence(ReferenceSequence referenceSequence) {
 			// TODO Auto-generated method stub
 
 		}
 
 		public void visitReadCoverage(ReadCoverage readCoverage) {
 			drawBlock(readCoverage);
 		}
 
 		public void visitGraph(GraphData graph) {
 			// TODO Auto-generated method stub
 
 		}
 
 	};
 
 	private class FindMaximumHeight extends OnGenomeDataVisitorBase {
 		int maxHeight = 1;
 
 		@Override
 		public void visitReadCoverage(ReadCoverage readCoverage) {
 			if (readCoverage.coverage == null)
 				return;
 
 			int startPosOfCoverageOnGenome = readCoverage.getStart();
 			int viewStartOnGenome = trackWindow.getStartOnGenome();
 			int viewEndOnGenome = trackWindow.getEndOnGenome();
 
 			TrackWindow w = new TrackWindow(readCoverage.pixelWidth, readCoverage.getStart(), readCoverage.getEnd());
 			int startPosInCoveragePixel = w.convertToPixelX(viewStartOnGenome);
 			int endPosInCoveragePixel = w.convertToPixelX(viewEndOnGenome);
 			if (endPosInCoveragePixel > readCoverage.pixelWidth)
 				endPosInCoveragePixel = readCoverage.pixelWidth;
 
 			// set canvas size
 			for (int i = startPosInCoveragePixel; i < endPosInCoveragePixel; ++i) {
 				int height = readCoverage.coverage[i];
 				if (height > maxHeight)
 					maxHeight = height;
 			}
 		}
 	}
 
 	private class CoveragePainter extends OnGenomeDataVisitorBase {
 
 		private int heigtOfRead = 1;
 		private float scalingFactor = 1.0f;
 
 		public CoveragePainter(int heightOfRead, float scalingFactor) {
 			this.heigtOfRead = heightOfRead;
 			this.scalingFactor = scalingFactor;
 		}
 
 		@Override
 		public void visitReadCoverage(ReadCoverage readCoverage) {
 			canvas.saveContext();
 			canvas.setStrokeStyle(getColor("#FFFFFF", 0.0f));
 			canvas.setFillStyle(getColor("#6699CC", 0.6f));
 			canvas.setLineWidth(0.1f);
 			canvas.setLineCap("round");
 
 			int x1 = pixelPositionOnWindow(trackWindow.getStartOnGenome());
 			int x2 = pixelPositionOnWindow(trackWindow.getEndOnGenome());
 
 			if (x1 < 0)
 				x1 = 0;
 			if (x2 > readCoverage.pixelWidth)
 				x2 = readCoverage.pixelWidth;
 
 			int w = x2 - x1;
 			canvas.scale(trackWindow.getPixelWidth() / (double) w, 1.0f);
 
 			canvas.saveContext();
 			canvas.beginPath();
 			canvas.moveTo(-0.5f, -1.0f);
 			for (int x = x1; x < x2; ++x) {
 				int h = readCoverage.coverage[x];
 				if (h <= 0) {
 					canvas.lineTo(x - x1 + 0.5f, -1.0f);
 					continue;
 				}
 
 				int y = (int) ((h * heigtOfRead) * scalingFactor);
 				canvas.lineTo(x - x1 + 0.5f, y + 0.5f);
 				canvas.stroke();
 			}
 			canvas.moveTo(readCoverage.pixelWidth + 0.5f, -1.0f);
 			canvas.fill();
 			canvas.restoreContext();
 
 			canvas.restoreContext();
 		}
 	}
 
 	private class RoughCoveragePainter extends OnGenomeDataVisitorBase {
 
 		private int heigtOfRead = 1;
 		private float scalingFactor = 1.0f;
 
 		public RoughCoveragePainter(int heightOfRead, float scalingFactor) {
 			this.heigtOfRead = heightOfRead;
 			this.scalingFactor = scalingFactor;
 		}
 
 		@Override
 		public void visitReadCoverage(ReadCoverage readCoverage) {
 			canvas.saveContext();
 			canvas.setFillStyle(getColor("#6699CC", 0.8f));
 			canvas.setLineWidth(1.0f);
 			canvas.setLineCap("round");
 
 			if (prefetchWindow == null)
 				return;
 
 			double x1 = prefetchWindow.convertToPixelXDouble(trackWindow.getStartOnGenome());
 			double x2 = prefetchWindow.convertToPixelXDouble(trackWindow.getEndOnGenome() + 1);
 
 			double w = x2 - x1;
 			canvas.scale(trackWindow.getPixelWidth() / w, 1.0f);
 
 			if (x1 < 0)
 				x1 = 0;
 			if (x2 > readCoverage.pixelWidth)
 				x2 = readCoverage.pixelWidth;
 
 			for (int x = (int) x1; x < (int) x2; ++x) {
 				int h = readCoverage.coverage[x];
 				if (h <= 0) {
 					continue;
 				}
 				int y = (int) ((h * heigtOfRead) * scalingFactor);
 				canvas.saveContext();
 				canvas.translate(x - x1 + 0.5f, 0);
 				canvas.fillRect(0, 0, 1, y - 0.5f);
 				canvas.restoreContext();
 			}
 
 			canvas.restoreContext();
 		}
 	}
 
 	private final int TRACK_COLLAPSE_COVERAGE_THRESHOLD = 40;
 
 	public <T extends OnGenome> void drawBlock(ReadCoverage block) {
 
 		// compute max height
 		FindMaximumHeight hFinder = new FindMaximumHeight();
 		block.accept(hFinder);
 
 		//int heightOfRead = hFinder.maxHeight > TRACK_COLLAPSE_COVERAGE_THRESHOLD ? 2 : defaultGeneHeight;
 		int heightOfRead = defaultMinGeneHeight;
 
 		int canvasHeight = hFinder.maxHeight * heightOfRead;
 		float scalingFactor = 1.0f;
 
 		final int MAX_CANVAS_HEIGHT = 150;
 		if (canvasHeight > MAX_CANVAS_HEIGHT) {
 			scalingFactor = (float) (MAX_CANVAS_HEIGHT) / canvasHeight;
 			canvasHeight = MAX_CANVAS_HEIGHT;
 		}
 
 		setPixelSize(trackWindow.getPixelWidth(), canvasHeight);
 
 		// draw coverage
 		OnGenomeDataVisitor cPainter;
 		switch (coverageStyle) {
 		case SMOOTH:
 			cPainter = new CoveragePainter(heightOfRead, scalingFactor);
 			break;
 		case DEFAULT:
 		default:
 			cPainter = new RoughCoveragePainter(heightOfRead, scalingFactor);
 			break;
 		}
 
 		block.accept(cPainter);
 	}
 
 	public void resetData(List<OnGenome> readSet) {
 		intervalLayout.reset(readSet, geneHeight);
 		hasCache = true;
 	}
 
 	private ImageElement imageACGT = null;
 
 	public void draw() {
 
 		boolean drawBase = trackWindow.getSequenceLength() <= (trackWindow.getPixelWidth() / FONT_WIDTH);
 		if (drawBase && imageACGT == null) {
 			int pixelWidthOfBase = (int) (trackWindow.getPixelLengthPerBase() + 0.1d);
 			ImageLoader.loadImages(new String[] { "utgb-core/ACGT.png?fontWidth=" + pixelWidthOfBase + "&height=" + defaultGeneHeight }, new CallBack() {
 				public void onImagesLoaded(ImageElement[] imageElements) {
 					imageACGT = imageElements[0];
 					layout();
 				}
 			});
 		}
 		else {
 			layout();
 		}
 	}
 
 	private void layout() {
 
 		int maxOffset = intervalLayout.createLocalLayout(geneHeight);
 
 		if (maxOffset > TRACK_COLLAPSE_COVERAGE_THRESHOLD)
 			geneHeight = defaultMinGeneHeight;
 		else
 			geneHeight = defaultGeneHeight;
 
 		int h = geneHeight + geneMargin;
 		int height = (maxOffset + 1) * h;
 
 		clearWidgets();
 		setPixelSize(trackWindow.getPixelWidth(), height);
 
 		final ReadPainter painter = new ReadPainter();
 
 		intervalLayout.depthFirstSearch(new PrioritySearchTree.Visitor<LocusLayout>() {
 			public void visit(LocusLayout gl) {
 				painter.setLayoutInfo(gl);
 				gl.getLocus().accept(painter);
 			}
 		});
 	}
 
 	@Override
 	public void setPixelSize(int width, int height) {
 		canvas.setCoordSize(width, height);
 		canvas.setPixelWidth(width);
 		canvas.setPixelHeight(height);
 		panel.setPixelSize(width, height);
 		basePanel.setPixelSize(width, height);
 	}
 
 	public static Color getGeneColor(Interval l) {
 		return getGeneColor(l, 1f);
 	}
 
 	public static Color getGeneColor(Interval l, float alpha) {
 		return getColor(getExonColorText(l), alpha);
 	}
 
 	public static Color getColor(String hex, float alpha) {
 		int r = Integer.parseInt(hex.substring(1, 3), 16);
 		int g = Integer.parseInt(hex.substring(3, 5), 16);
 		int b = Integer.parseInt(hex.substring(5, 7), 16);
 		return new Color(r, g, b, alpha);
 	}
 
 	private static String getExonColorText(OnGenome g) {
 		final String senseColor = "#d80067";
 		final String antiSenseColor = "#0067d8";
 
 		if (g instanceof Interval) {
 			Interval r = (Interval) g;
 			if (r.getColor() == null) {
 				return r.isSense() ? senseColor : antiSenseColor;
 			}
 			else {
 				return r.getColor();
 			}
 		}
 		else
 			return senseColor;
 	}
 
 	public static Color getExonColor(Gene g) {
 		return getGeneColor(g, 0.5f);
 	}
 
 	public static Color getCDSColor(Interval g, float alpha) {
 		return getGeneColor(g, alpha);
 	}
 
 	public static Color getCDSColor(Interval g) {
 		return getGeneColor(g);
 	}
 
 	public void draw(Gene gene, List<Exon> exonList, CDS cds, int yPosition) {
 		// assume that exonList are sorted
 
 		if (exonList.isEmpty()) {
 			Color c = getGeneColor(gene);
 			drawGeneRect(pixelPositionOnWindow(gene.getStart()), pixelPositionOnWindow(gene.getEnd()), yPosition, c, true);
 		}
 
 		for (Exon e : exonList) {
 			drawExon(gene, e, cds, yPosition);
 		}
 
 		canvas.saveContext();
 		canvas.setFillStyle(getGeneColor(gene, 0.7f));
 		canvas.setStrokeStyle(getGeneColor(gene, 0.7f));
 		canvas.setLineWidth(0.5f);
 
 		// draw the arrow between exons
 		boolean isSense = gene.isSense() ? !reverse : reverse;
 		double arrowHeight = geneHeight / 2.0 + 0.5;
 
 		for (int i = 0; i < exonList.size() - 1; i++) {
 			Exon prev = exonList.get(i);
 			Exon next = exonList.get(i + 1);
 
 			int x1 = pixelPositionOnWindow(prev.getEnd());
 			int x2 = pixelPositionOnWindow(next.getStart());
 			float yAxis = yPosition + (geneHeight / 2) + 0.5f;
 
 			canvas.saveContext();
 			canvas.beginPath();
 			canvas.moveTo(drawPosition(x1) + 0.5f, yAxis);
 			canvas.lineTo(drawPosition(x2) - 0.5f, yAxis);
 			canvas.stroke();
 			canvas.restoreContext();
 
 			for (int x = x1; x + 4 <= x2; x += 5) {
 				canvas.saveContext();
 				canvas.translate(drawPosition(x) + 2.0f, yPosition + arrowHeight);
 				if (!isSense)
 					canvas.rotate(Math.PI);
 				canvas.beginPath();
 				canvas.moveTo(-2.0f, -arrowHeight + 1.5f);
 				canvas.lineTo(1.5f, 0);
 				canvas.lineTo(-2.0f, arrowHeight - 1.5f);
 				canvas.stroke();
 				canvas.restoreContext();
 			}
 
 		}
 		canvas.restoreContext();
 
 	}
 
 	public void drawPadding(int x1, int x2, int y, Color c, boolean drawShadow) {
 
 		canvas.saveContext();
 		canvas.setFillStyle(c);
 		canvas.setStrokeStyle(c);
 		canvas.setLineWidth(0.5f);
 		float yPos = y + (geneHeight / 2) + 0.5f;
 
 		canvas.beginPath();
 		canvas.moveTo(drawPosition(x1) + 0.5f, yPos);
 		canvas.lineTo(drawPosition(x2) - 0.5f, yPos);
 		canvas.stroke();
 
 		canvas.restoreContext();
 
 	}
 
 	private int drawPosition(int x) {
 		if (reverse)
 			return (trackWindow.getPixelWidth() - x);
 		else
 			return x;
 	}
 
 	public void draw(Interval gene, int yPosition) {
 		int gx = pixelPositionOnWindow(gene.getStart());
 		int gx2 = pixelPositionOnWindow(gene.getEnd());
 
 		drawGeneRect(gx, gx2, yPosition, getCDSColor(gene), true);
 	}
 
 	public void draw(Gene gene, int yPosition) {
 		int gx = pixelPositionOnWindow(gene.getStart());
 		int gx2 = pixelPositionOnWindow(gene.getEnd());
 
 		drawGeneRect(gx, gx2, yPosition, getGeneColor(gene), true);
 	}
 
 	public void drawExon(Gene gene, Exon exon, CDS cds, int yPosition) {
 		int ex = pixelPositionOnWindow(exon.getStart());
 		int ex2 = pixelPositionOnWindow(exon.getEnd());
 
 		drawGeneRect(ex, ex2, yPosition, getExonColor(gene), true);
 
 		// draw CDS
 		if (cds != null) {
 			int cx = pixelPositionOnWindow(cds.getStart());
 			int cx2 = pixelPositionOnWindow(cds.getEnd());
 
 			if (cx <= cx2) {
 				if (ex <= cx2 && ex2 >= cx) {
 					int cdsStart = (ex <= cx) ? cx : ex;
 					int cdsEnd = (ex2 <= cx2) ? ex2 : cx2;
 
 					drawGeneRect(cdsStart, cdsEnd, yPosition, getCDSColor(gene), false);
 				}
 			}
 
 		}
 
 	}
 
 	public void drawGeneRect(int x1, int x2, int y, Color c, boolean drawShadow) {
 
 		float boxWidth = x2 - x1 - 0.5f;
 		if (boxWidth <= 0)
 			boxWidth = 1f;
 
 		canvas.saveContext();
 		double drawX = drawPosition(reverse ? x2 : x1);
 		canvas.translate(drawX, y);
 		canvas.setFillStyle(Color.WHITE);
 		canvas.fillRect(0, 0, boxWidth, geneHeight);
		if (!BrowserInfo.isIE() && (boxWidth > 5 && geneHeight > 4)) {
 			CanvasGradient grad = canvas.createLinearGradient(0, 0, 0, geneHeight);
 			grad.addColorStop(0, c);
 			grad.addColorStop(0.1, Color.WHITE);
 			grad.addColorStop(0.5, c);
 			grad.addColorStop(1, c);
 			canvas.setFillStyle(grad);
 		}
 		else
 			canvas.setFillStyle(c);
 		canvas.fillRect(0, 0, boxWidth, geneHeight);
 		canvas.restoreContext();
 
 		if (drawShadow) {
 			canvas.saveContext();
 			canvas.setStrokeStyle(new Color(30, 30, 30, 0.6f));
 			double shadowStart = drawPosition(reverse ? x2 : x1);
 			canvas.translate(shadowStart, y);
 			canvas.beginPath();
 			canvas.moveTo(1.5f, geneHeight + 0.5f);
 			canvas.lineTo(boxWidth + 0.5f, geneHeight + 0.5f);
 			canvas.lineTo(boxWidth + 0.5f, 0.5f);
 			canvas.stroke();
 			canvas.restoreContext();
 		}
 
 	}
 
 	public void setReadHeight(int height) {
 		this.defaultGeneHeight = height;
 		imageACGT = null;
 	}
 
 	public void setReadHeightMin(int height) {
 		this.defaultMinGeneHeight = height;
 		imageACGT = null;
 	}
 
 }
