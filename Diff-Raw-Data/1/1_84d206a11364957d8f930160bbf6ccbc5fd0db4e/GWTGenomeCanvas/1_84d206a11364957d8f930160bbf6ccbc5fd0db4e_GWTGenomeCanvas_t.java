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
 
 import org.utgenome.gwt.ipad.client.TouchableComposite;
 import org.utgenome.gwt.ipad.event.Touch;
 import org.utgenome.gwt.ipad.event.TouchCancelEvent;
 import org.utgenome.gwt.ipad.event.TouchCancelHandler;
 import org.utgenome.gwt.ipad.event.TouchEndEvent;
 import org.utgenome.gwt.ipad.event.TouchEndHandler;
 import org.utgenome.gwt.ipad.event.TouchMoveEvent;
 import org.utgenome.gwt.ipad.event.TouchMoveHandler;
 import org.utgenome.gwt.ipad.event.TouchStartEvent;
 import org.utgenome.gwt.ipad.event.TouchStartHandler;
 import org.utgenome.gwt.utgb.client.UTGBClientException;
 import org.utgenome.gwt.utgb.client.bio.CDS;
 import org.utgenome.gwt.utgb.client.bio.CIGAR;
 import org.utgenome.gwt.utgb.client.bio.Exon;
 import org.utgenome.gwt.utgb.client.bio.Gap;
 import org.utgenome.gwt.utgb.client.bio.Gene;
 import org.utgenome.gwt.utgb.client.bio.GraphData;
 import org.utgenome.gwt.utgb.client.bio.InfoSilkGenerator;
 import org.utgenome.gwt.utgb.client.bio.Interval;
 import org.utgenome.gwt.utgb.client.bio.GenomeRange;
 import org.utgenome.gwt.utgb.client.bio.GenomeRangeVisitor;
 import org.utgenome.gwt.utgb.client.bio.GenomeRangeVisitorBase;
 import org.utgenome.gwt.utgb.client.bio.Read;
 import org.utgenome.gwt.utgb.client.bio.ReadCoverage;
 import org.utgenome.gwt.utgb.client.bio.ReadList;
 import org.utgenome.gwt.utgb.client.bio.ReferenceSequence;
 import org.utgenome.gwt.utgb.client.bio.SAMReadLight;
 import org.utgenome.gwt.utgb.client.bio.SAMReadPair;
 import org.utgenome.gwt.utgb.client.bio.SAMReadPairFragment;
 import org.utgenome.gwt.utgb.client.canvas.GWTGraphCanvas.GraphStyle;
 import org.utgenome.gwt.utgb.client.canvas.IntervalLayout.IntervalRetriever;
 import org.utgenome.gwt.utgb.client.canvas.IntervalLayout.LocusLayout;
 import org.utgenome.gwt.utgb.client.track.TrackGroup;
 import org.utgenome.gwt.utgb.client.track.TrackWindow;
 import org.utgenome.gwt.utgb.client.ui.FixedWidthLabel;
 import org.utgenome.gwt.utgb.client.ui.RoundCornerFrame;
 import org.utgenome.gwt.utgb.client.util.Optional;
 import org.utgenome.gwt.widget.client.Style;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.ImageElement;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
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
 public class GWTGenomeCanvas extends TouchableComposite {
 
 	private ReadDisplayStyle style = new ReadDisplayStyle();
 	//	private int defaultGeneHeight = 12;
 	//	private int defaultMinGeneHeight = 2;
 
 	private int geneHeight = style.readHeight;
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
 
 	static class PopupInfo extends PopupPanel {
 
 		GenomeRange locus;
 		private FlexTable infoTable = new FlexTable();
 
 		public PopupInfo() {
 			super(true);
 
 			RoundCornerFrame infoFrame = new RoundCornerFrame("336699", 0.7f, 4);
 			infoFrame.setWidget(infoTable);
 			this.setWidget(infoFrame);
 		}
 
 		public void setLocus(GenomeRange g) {
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
 			final int numRowsInCol = 15;
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
 					p.add(new Label(lines.get(index)));
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
 			moveDrag(event);
 			break;
 		}
 		case Event.ONMOUSEOUT: {
 			resetDrag(event);
 			break;
 		}
 		case Event.ONMOUSEDOWN: {
 			// invoke a click event
 			startDrag(event);
 			break;
 		}
 		case Event.ONMOUSEUP: {
 			resetDrag(event);
 			break;
 		}
 		}
 
 	}
 
 	private void startDrag(Event event) {
 		if (startDrag(getXOnCanvas(event), getYOnCanvas(event))) {
 			event.preventDefault();
 		}
 		else {
 			Event.setCapture(this.getElement());
 		}
 	}
 
 	private boolean startDrag(int clientX, int clientY) {
 		GenomeRange g = overlappedInterval(clientX, clientY, 2);
 		if (g != null) {
 			if (clickHandler != null)
 				clickHandler.onClick(clientX, clientY, g);
 			return true;
 		}
 		else {
 			dragStartPoint.set(new DragPoint(clientX, clientY));
 			Style.cursor(canvas, Style.CURSOR_RESIZE_E);
 		}
 		return false;
 	}
 
 	private void moveDrag(Event e) {
 		moveDrag(getXOnCanvas(e), getYOnCanvas(e));
 	}
 
 	private void moveDrag(int clientX, int clientY) {
 		// show readLabels 
 		GenomeRange g = overlappedInterval(clientX, clientY, 2);
 		if (g != null) {
 			if (popupLabel.locus != g) {
 
 				Style.cursor(canvas, Style.CURSOR_POINTER);
 				displayInfo(clientX, clientY, g);
 			}
 		}
 		else {
 			if (dragStartPoint.isDefined() && trackWindow != null) {
 				DragPoint p = dragStartPoint.get();
 				final int x_origin = trackWindow.convertToGenomePosition(p.x);
 				int startDiff = trackWindow.convertToGenomePosition(clientX) - x_origin;
 				if (startDiff != 0) {
 					int newStart = trackWindow.getStartOnGenome() - startDiff;
 					if (newStart < 1)
 						newStart = 1;
 					int newEnd = newStart + trackWindow.getSequenceLength();
 					TrackWindow newWindow = trackWindow.newWindow(newStart, newEnd);
 					if (trackGroup != null)
 						trackGroup.setTrackWindow(newWindow);
 					dragStartPoint.set(new DragPoint(clientX, p.y));
 				}
 			}
 			else {
 				Style.cursor(canvas, Style.CURSOR_AUTO);
 				popupLabel.setLocus(null);
 			}
 		}
 	}
 
 	private void resetDrag(Event event) {
 		resetDrag(getXOnCanvas(event), getYOnCanvas(event));
 		DOM.releaseCapture(this.getElement());
 		event.preventDefault();
 	}
 
 	private void resetDrag(int clientX, int clientY) {
 		if (dragStartPoint.isDefined() && trackWindow != null) {
 			DragPoint p = dragStartPoint.get();
 			final int x_origin = trackWindow.convertToGenomePosition(p.x);
 			int startDiff = trackWindow.convertToGenomePosition(clientX) - x_origin;
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
 
 	public void displayInfo(final int clientX, final int clientY, final GenomeRange g) {
 		if (popupLabel == null)
 			popupLabel = new PopupInfo();
 
 		popupLabel.setLocus(g);
 
 		Timer timer = new Timer() {
 			@Override
 			public void run() {
 				popupLabel.removeFromParent();
 				if (popupLabel.locus == g) {
 
 					int x = clientX + 10 + getAbsoluteLeft();
 					int y = clientY + 3 + getAbsoluteTop();
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
 
 	public void setAllowOverlapPairedReads(boolean allow) {
 	}
 
 	/**
 	 * compute the overlapped intervals for the mouse over event
 	 * 
 	 * @param event
 	 * @param xBorder
 	 * @return
 	 */
 	private GenomeRange overlappedInterval(Event event, int xBorder) {
 		return overlappedInterval(getXOnCanvas(event), getYOnCanvas(event), xBorder);
 	}
 
 	private GenomeRange overlappedInterval(int clientX, int clientY, int xBorder) {
 		int h = getReadHeight();
 		int x = drawPosition(clientX);
 		int y = clientY;
 		GenomeRange g = intervalLayout.overlappedInterval(x, y, xBorder, h);
 		return g;
 	}
 
 	public int getXOnCanvas(Event event) {
 		return getXOnCanvas(DOM.eventGetClientX(event));
 	}
 
 	public int getXOnCanvas(int clientX) {
 		return clientX + Window.getScrollLeft() - basePanel.getAbsoluteLeft();
 	}
 
 	public int getYOnCanvas(int clientY) {
 		return clientY + Window.getScrollTop() - basePanel.getAbsoluteTop();
 	}
 
 	public int getYOnCanvas(Event event) {
 		return getYOnCanvas(DOM.eventGetClientY(event));
 	}
 
 	private void initWidget() {
 		super.initWidget(basePanel);
 
 		panel.add(canvas, 0, 0);
 		basePanel.add(panel, 0, 0);
 		sinkEvents(Event.ONMOUSEMOVE | Event.ONMOUSEOVER | Event.ONMOUSEDOWN | Event.ONMOUSEUP | Event.ONMOUSEOUT);
 
 		// add touch handler for iPad
 		this.addTouchStartHandler(new TouchStartHandler() {
 			public void onTouchStart(TouchStartEvent event) {
 				Touch touch = event.touches().get(0);
 				event.preventDefault();
 				startDrag(touch.getClientX(), touch.getClientY());
 				DOM.setCapture(GWTGenomeCanvas.this.getElement());
 			}
 		});
 
 		this.addTouchMoveHandler(new TouchMoveHandler() {
 			public void onTouchMove(TouchMoveEvent event) {
 				event.preventDefault();
 				Touch touch = event.touches().get(0);
 				moveDrag(touch.getClientX(), touch.getClientY());
 			}
 		});
 
 		this.addTouchEndHandler(new TouchEndHandler() {
 			public void onTouchEnd(TouchEndEvent event) {
 				Touch touch = event.touches().get(0);
 				resetDrag(touch.getClientX(), touch.getClientY());
 				DOM.releaseCapture(GWTGenomeCanvas.this.getElement());
 				//event.preventDefault();
 			}
 		});
 
 		this.addTouchCancelHandler(new TouchCancelHandler() {
 			public void onTouchCancel(TouchCancelEvent event) {
 				Touch touch = event.touches().get(0);
 				resetDrag(touch.getClientX(), touch.getClientY());
 				DOM.releaseCapture(GWTGenomeCanvas.this.getElement());
 				//event.preventDefault();
 			}
 		});
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
 	 * @param newWindow
 	 */
 	public void setTrackWindow(TrackWindow newWindow, boolean resetPrefetchWindow) {
 
 		if (resetPrefetchWindow || !hasCacheCovering(newWindow)) { // when need to prefetch the data
 			int prefetchStart = newWindow.getStartOnGenome() - (int) (newWindow.getSequenceLength() * PREFETCH_FACTOR);
 			int prefetchEnd = newWindow.getEndOnGenome() + (int) (newWindow.getSequenceLength() * PREFETCH_FACTOR);
 			if (prefetchStart <= 0) {
 				prefetchStart = 1;
 				prefetchEnd = newWindow.getEndOnGenome() + (int) (newWindow.getSequenceLength() * PREFETCH_FACTOR * 2);
 			}
 			int prefetchPixelSize = (int) (newWindow.getPixelWidth() * (1 + PREFETCH_FACTOR * 2));
 			prefetchWindow = new TrackWindow(prefetchPixelSize, prefetchStart, prefetchEnd);
 			hasCache = false;
 		}
 
 		if (trackWindow != null) {
 			int newX = newWindow.convertToPixelX(trackWindow.getStartOnGenome());
 			if (!trackWindow.hasSameScaleWith(newWindow)) {
 				int newPixelWidth = newWindow.convertToPixelLength(trackWindow.getSequenceLength());
 				Style.scaleXwithAnimation(canvas, (double) newPixelWidth / trackWindow.getPixelWidth(), newX, 0.5);
 				imageACGT = null;
 			}
 			else {
 				Style.scrollX(canvas, newX, 0.5);
 			}
 		}
 
 		this.trackWindow = newWindow;
 		reverse = newWindow.isReverseStrand();
 
 		intervalLayout.setTrackWindow(newWindow);
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
 
 		scale.removeFromParent();
 
 		for (Widget w : readLabels) {
 			w.removeFromParent();
 		}
 		readLabels.clear();
 		Style.scaleXwithAnimation(canvas, 1, 0.0);
 		panel.setWidgetPosition(canvas, 0, 0);
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
 
 	private static final float repeatColorAlpha = 0.3f;
 	private static final Color[] colors = { getColor(DEFAULT_COLOR_A, 1.0f), getColor(DEFAULT_COLOR_C, 1.0f), getColor(DEFAULT_COLOR_G, 1.0f),
 			getColor(DEFAULT_COLOR_T, 1.0f), getColor(DEFAULT_COLOR_A, repeatColorAlpha), getColor(DEFAULT_COLOR_C, repeatColorAlpha),
 			getColor(DEFAULT_COLOR_G, repeatColorAlpha), getColor(DEFAULT_COLOR_T, repeatColorAlpha), getColor(DEFAULT_COLOR_N, 1.0f) };
 
 	private int getReadHeight() {
 		return geneHeight + geneMargin;
 	}
 
 	/**
 	 * A class for drawing OnGenome objects on a canvas
 	 * 
 	 * @author leo
 	 * 
 	 */
 	class ReadPainter extends GenomeRangeVisitorBase {
 
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
 
 		@Override
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
 
 		public void drawBases(int startOnGenome, int y, String seq, String qual) {
 
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
 
 				int h = imageACGT.getHeight();
 				if (h >= geneHeight)
 					h = geneHeight;
 
 				if (qual == null || h < 5 || !style.showBaseQuality) {
 					canvas.drawImage(imageACGT, pixelWidthOfBase * baseIndex, 0, pixelWidthOfBase, h, (int) x1, y, pixelWidthOfBase, h);
 				}
 				else {
 					canvas.saveContext();
 					final int threshold = 40;
 					if (i < qual.length()) {
 						int qv = qual.charAt(i) - 33;
 						if (qv > threshold)
 							qv = threshold;
 						if (qv < 0)
 							qv = 0;
 						float ratio = (float) qv / threshold;
 						float height = h * ratio;
 
 						canvas.setFillStyle(colors[baseIndex]);
 						canvas.fillRect(x1, y, x2 - x1, geneHeight);
 
 						canvas.setFillStyle(new Color(255, 255, 255, 0.7f));
 						canvas.fillRect((int) x1, y, pixelWidthOfBase, h * (1 - ratio));
 
 						canvas.drawImage(imageACGT, pixelWidthOfBase * baseIndex, h, pixelWidthOfBase, h, (int) x1, y, pixelWidthOfBase, h);
 					}
 					canvas.restoreContext();
 				}
 			}
 
 		}
 
 		private void drawLabel(GenomeRange r, int y) {
 			if (!intervalLayout.hasEnoughHeightForLabels())
 				return;
 
 			IntervalRetriever ir = new IntervalRetriever();
 			ir.allowPEOverlap = style.overlapPairedReads;
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
 
 		@Override
 		public void visitGap(Gap p) {
 			drawPadding(pixelPositionOnWindow(p.getStart()), pixelPositionOnWindow(p.getEnd()), getYPos(), getColor("#666666", 1.0f), true);
 		}
 
 		private void drawLabel(GenomeRange r) {
 			drawLabel(r, getYPos());
 		}
 
 		@Override
 		public void visitInterval(Interval interval) {
 			draw(interval, getYPos());
 		}
 
 		@Override
 		public void visitRead(Read r) {
 			draw(r, getYPos());
 			drawLabel(r);
 		}
 
 		@Override
 		public void visitSAMReadPair(SAMReadPair pair) {
 
 			SAMReadLight first = pair.getFirst();
 			SAMReadLight second = pair.getSecond();
 
 			int y1 = getYPos();
 			int y2 = y1;
 
 			if (!style.overlapPairedReads && first.unclippedSequenceHasOverlapWith(second)) {
 				if (first.unclippedStart > second.unclippedStart) {
 					SAMReadLight tmp = first;
 					first = second;
 					second = tmp;
 				}
 				y2 = getYPos(gl.getYOffset() + 1);
 			}
 			else {
 				visitGap(pair.getGap());
 			}
 
 			drawLabel(pair);
 			drawSAMRead(first, y1, false);
 			drawSAMRead(second, y2, false);
 		}
 
 		@Override
 		public void visitSAMReadPairFragment(SAMReadPairFragment fragment) {
 			drawLabel(fragment);
 			visitGap(fragment.getGap());
 			drawSAMRead(fragment.oneEnd, getYPos(), false);
 		}
 
 		@Override
 		public void visitSAMReadLight(SAMReadLight r) {
 			drawSAMRead(r, getYPos(), true);
 		}
 
 		class PostponedInsertion {
 			final int pixelX;
 			final String subseq;
 
 			public PostponedInsertion(int pixelX, String subseq) {
 				this.pixelX = pixelX;
 				this.subseq = subseq;
 			}
 
 		}
 
 		public void drawSAMRead(SAMReadLight r, int y, boolean drawLabel) {
 
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
 
 					// Drawing insertions should be postponed after all of he read bases are painted.
 					List<PostponedInsertion> postponed = new ArrayList<PostponedInsertion>();
 
 					for (int cigarIndex = 0; cigarIndex < cigar.size(); cigarIndex++) {
 						CIGAR.Element e = cigar.get(cigarIndex);
 
 						int readEnd = readStart + e.length;
 						int x1 = pixelPositionOnWindow(readStart);
 						switch (e.type) {
 						case Deletions:
 							// ref : AAAAAA
 							// read: ---AAA
 							// cigar: 3D3M
 							drawPadding(x1, pixelPositionOnWindow(readEnd), y, style.getSAMReadColor(r), true);
 							break;
 						case Insertions:
 							// ref : ---AAA
 							// read: AAAAAA
 							// cigar: 3I3M
 							if (r.getSequence() != null)
 								postponed.add(new PostponedInsertion(x1, r.getSequence().substring(seqIndex, seqIndex + e.length)));
 							readEnd = readStart;
 							seqIndex += e.length;
 							break;
 						case Padding:
 							// ref : AAAAAA
 							// read: ---AAA
 							// cigar: 3P3M
 							readEnd = readStart;
 							drawPadding(x1, pixelPositionOnWindow(readStart) + 1, y, style.getPaddingColor(), false);
 							break;
 						case Matches: {
 							int x2 = pixelPositionOnWindow(readEnd);
 
 							if (drawBase && r.getSequence() != null) {
 								//drawGeneRect(x1, x2, y, getCDSColor(r, 0.3f), true);
 								drawBases(readStart, y, r.getSequence().substring(seqIndex, seqIndex + e.length),
 										r.getQV() != null ? r.getQV().substring(seqIndex, seqIndex + e.length) : null);
 							}
 							else {
 								drawGeneRect(x1, x2, y, style.getSAMReadColor(r), style.drawShadow);
 							}
 
 							seqIndex += e.length;
 						}
 							break;
 						case SkippedRegion:
 							drawPadding(x1, pixelPositionOnWindow(readEnd), y, style.getReadColor(r), true);
 							break;
 						case SoftClip: {
 							int softclipStart = cigarIndex == 0 ? readStart - e.length : readStart;
 							int softclipEnd = cigarIndex == 0 ? readStart : readStart + e.length;
 							readEnd = softclipEnd;
 
 							int x0 = pixelPositionOnWindow(softclipStart);
 							x1 = pixelPositionOnWindow(softclipEnd);
 
 							if (drawBase && r.getSequence() != null) {
 								drawBases(softclipStart, y, r.getSequence().substring(seqIndex, seqIndex + e.length).toLowerCase(), r.getQV() != null ? r
 										.getQV().substring(seqIndex, seqIndex + e.length) : null);
 							}
 							else {
 								drawGeneRect(x0, x1, y, style.getClippedReadColor(r), style.drawShadow);
 							}
 
 							seqIndex += e.length;
 						}
 							break;
 						case HardClip:
							readEnd -= e.length;
 							break;
 						}
 						readStart = readEnd;
 					}
 
 					for (PostponedInsertion each : postponed) {
 						drawGeneRect(each.pixelX, each.pixelX + 1, y, getColor("#111111", 1.0f), true);
 
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
 
 		@Override
 		public void visitSequence(ReferenceSequence referenceSequence) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void visitReadCoverage(ReadCoverage readCoverage) {
 			drawBlock(readCoverage);
 		}
 
 		@Override
 		public void visitGraph(GraphData graph) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void visitReadList(ReadList readList) {
 			// TODO Auto-generated method stub
 
 		}
 
 	};
 
 	private class FindMaximumHeight extends GenomeRangeVisitorBase {
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
 
 	private class CoveragePainter extends GenomeRangeVisitorBase {
 
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
 
 	private class RoughCoveragePainter extends GenomeRangeVisitorBase {
 
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
 				if (y <= 0)
 					y = 1;
 				canvas.saveContext();
 				canvas.translate(x - x1 + 0.5f, 0);
 				canvas.fillRect(0, 0, 1, y - 0.5f);
 				canvas.restoreContext();
 			}
 
 			canvas.restoreContext();
 		}
 	}
 
 	private final int TRACK_COLLAPSE_COVERAGE_THRESHOLD = 40;
 
 	private GraphScale scale = new GraphScale();
 
 	public <T extends GenomeRange> void drawBlock(ReadCoverage block) {
 
 		// compute max height
 		FindMaximumHeight hFinder = new FindMaximumHeight();
 		block.accept(hFinder);
 
 		//int heightOfRead = hFinder.maxHeight > TRACK_COLLAPSE_COVERAGE_THRESHOLD ? 2 : defaultGeneHeight;
 		int heightOfRead = style.minReadHeight;
 
 		int canvasHeight = hFinder.maxHeight * heightOfRead;
 		float scalingFactor = 1.0f;
 
 		final int MAX_CANVAS_HEIGHT = 50;
 		if (canvasHeight > MAX_CANVAS_HEIGHT) {
 			scalingFactor = (float) (MAX_CANVAS_HEIGHT) / canvasHeight;
 			canvasHeight = MAX_CANVAS_HEIGHT;
 		}
 
 		setPixelSize(trackWindow.getPixelWidth(), canvasHeight);
 
 		// draw coverage
 		GenomeRangeVisitor cPainter;
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
 
 		GraphStyle scaleStyle = new GraphStyle();
 		scaleStyle.autoScale = false;
 		scaleStyle.minValue = hFinder.maxHeight;
 		scaleStyle.maxValue = 0;
 		scaleStyle.windowHeight = canvasHeight;
 		scaleStyle.drawScale = false;
 		scale.draw(scaleStyle, this.trackWindow);
 		panel.add(scale, 0, 0);
 	}
 
 	public void resetData(List<GenomeRange> readSet) {
 		intervalLayout.reset(readSet, geneHeight);
 		hasCache = true;
 	}
 
 	private ImageElement imageACGT = null;
 
 	public void draw() {
 
 		boolean drawBase = trackWindow.getSequenceLength() <= (trackWindow.getPixelWidth() / FONT_WIDTH);
 		if (drawBase && imageACGT == null) {
 			int pixelWidthOfBase = (int) (trackWindow.getPixelLengthPerBase() + 0.1d);
 			ImageLoader.loadImages(new String[] { "utgb-core/ACGT.png?fontWidth=" + pixelWidthOfBase + "&height=" + style.readHeight }, new CallBack() {
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
 			geneHeight = style.minReadHeight;
 		else
 			geneHeight = style.readHeight;
 
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
 		Style.scaleX(canvas, 1);
 		panel.setPixelSize(width, height);
 		basePanel.setPixelSize(width, height);
 	}
 
 	public static Color getColor(String hex, float alpha) {
 		return ColorUtil.toColor(hex, alpha);
 	}
 
 	public Color getGeneColor(Interval l) {
 		return getGeneColor(l, 1f);
 	}
 
 	public Color getGeneColor(Interval l, float alpha) {
 		return style.getReadColor(l, alpha);
 	}
 
 	private static String getExonColorText(GenomeRange g) {
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
 
 	public Color getExonColor(Gene g) {
 		return getGeneColor(g, 0.5f);
 	}
 
 	public Color getCDSColor(Interval g, float alpha) {
 		return getGeneColor(g, alpha);
 	}
 
 	public Color getCDSColor(Interval g) {
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
 
 		drawGeneRect(gx, gx2, yPosition, getCDSColor(gene), style.drawShadow);
 	}
 
 	public void draw(Gene gene, int yPosition) {
 		int gx = pixelPositionOnWindow(gene.getStart());
 		int gx2 = pixelPositionOnWindow(gene.getEnd());
 
 		drawGeneRect(gx, gx2, yPosition, getGeneColor(gene), style.drawShadow);
 	}
 
 	public void drawExon(Gene gene, Exon exon, CDS cds, int yPosition) {
 		int ex = pixelPositionOnWindow(exon.getStart());
 		int ex2 = pixelPositionOnWindow(exon.getEnd());
 
 		drawGeneRect(ex, ex2, yPosition, getExonColor(gene), style.drawShadow);
 
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
 		//		canvas.fillRect(0, 0, boxWidth, geneHeight);
 		//		if (!BrowserInfo.isIE() && (boxWidth > 5 && geneHeight > 4)) {
 		//			CanvasGradient grad = canvas.createLinearGradient(0, 0, 0, geneHeight);
 		//			grad.addColorStop(0, c);
 		//			grad.addColorStop(0.1, Color.WHITE);
 		//			grad.addColorStop(0.5, c);
 		//			grad.addColorStop(1, c);
 		//			canvas.setFillStyle(grad);
 		//		}
 		//		else
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
 
 	public void setReadStyle(ReadDisplayStyle style) {
 		this.style = style;
 		intervalLayout.setKeepSpaceForLabels(style.showLabels);
 		imageACGT = null;
 
 		if (style.coverageStyle != null) {
 			CoverageStyle s = CoverageStyle.valueOf(CoverageStyle.class, style.coverageStyle.toUpperCase());
 			if (s != null) {
 				coverageStyle = s;
 			}
 		}
 
 		intervalLayout.setAllowOverlapPairedReads(style.overlapPairedReads);
 	}
 
 }
