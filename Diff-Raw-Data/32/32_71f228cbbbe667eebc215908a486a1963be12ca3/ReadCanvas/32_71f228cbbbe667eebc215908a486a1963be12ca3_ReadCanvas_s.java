 /*--------------------------------------------------------------------------
  *  Copyright 2011 utgenome.org
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
 // ReadCanvas.java
 // Since: 2011/01/06
 //
 //--------------------------------------
 package org.utgenome.graphics;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 
 import org.utgenome.gwt.utgb.client.UTGBClientException;
 import org.utgenome.gwt.utgb.client.bio.CIGAR;
 import org.utgenome.gwt.utgb.client.bio.Gap;
 import org.utgenome.gwt.utgb.client.bio.Interval;
 import org.utgenome.gwt.utgb.client.bio.OnGenome;
 import org.utgenome.gwt.utgb.client.bio.OnGenomeDataVisitorBase;
 import org.utgenome.gwt.utgb.client.bio.SAMReadLight;
 import org.utgenome.gwt.utgb.client.bio.SAMReadPair;
 import org.utgenome.gwt.utgb.client.bio.SAMReadPairFragment;
 import org.utgenome.gwt.utgb.client.canvas.IntervalLayout;
 import org.utgenome.gwt.utgb.client.canvas.IntervalLayout.LocusLayout;
 import org.utgenome.gwt.utgb.client.canvas.PrioritySearchTree.Visitor;
 import org.utgenome.gwt.utgb.client.track.TrackWindow;
 import org.utgenome.gwt.utgb.server.util.graphic.GraphicUtil;
 import org.xerial.util.log.Logger;
 
 /**
  * For generating {@link BufferedImage} instance of a read layout
  * 
  * @author leo
  * 
  */
 public class ReadCanvas {
 
 	private static Logger _logger = Logger.getLogger(ReadCanvas.class);
 	private final GenomeWindow window;
 	private BufferedImage image;
 	private Graphics2D g;
 
 	private IntervalLayout layout = new IntervalLayout();
 
 	public static class DrawStyle {
 		public int geneHeight = 2;
 		public int geneMargin = 1;
 		public boolean overlapPairedReads = true;
 		public boolean showStrand = true;
 		public boolean drawShadow = true;
 		public int fontWidth = 10;
 		public float clippedRegionAlpha = 0.2f;
 
 		public Color COLOR_GAP = new Color(0x66, 0x66, 0x66);
 		public Color COLOR_PADDING = new Color(0x33, 0x33, 0x66);
 		public Color COLOR_SHADOW = new Color(30, 30, 30, (int) (255 * 0.6f));
 
 		public Color COLOR_READ_DEFAULT = new Color(0xCC, 0xCC, 0xCC);
 		public Color COLOR_FORWARD_STRAND = new Color(0xd8, 0x00, 0x67);
 		public Color COLOR_REVERSE_STRAND = new Color(0x00, 0x67, 0xd8);
 
 		public Color COLOR_WIRED_READ_F = new Color(0xff, 0x99, 0x66);
 		public Color COLOR_WIRED_READ_R = new Color(0x66, 0x99, 0xff);
 		public Color COLOR_ORPHAN_READ_F = new Color(0xff, 0x66, 0x99);
 		public Color COLOR_ORPHAN_READ_R = new Color(0x66, 0x99, 0xff);
 
 		private HashMap<Character, Color> colorTable = new HashMap<Character, Color>();
 
 		private String colorA = "50B6E8";
 		private String colorC = "E7846E";
 		private String colorG = "84AB51";
 		private String colorT = "FFA930";
 		public String colorN = "EEEEEE";
 		public int repeatColorAlpha = 50;
 
 		{
 			colorTable.put('a', GraphicUtil.parseColor(colorA, repeatColorAlpha));
 			colorTable.put('c', GraphicUtil.parseColor(colorC, repeatColorAlpha));
 			colorTable.put('g', GraphicUtil.parseColor(colorG, repeatColorAlpha));
 			colorTable.put('t', GraphicUtil.parseColor(colorT, repeatColorAlpha));
 			colorTable.put('n', GraphicUtil.parseColor(colorN, repeatColorAlpha));
 			colorTable.put('A', GraphicUtil.parseColor(colorA));
 			colorTable.put('C', GraphicUtil.parseColor(colorC));
 			colorTable.put('G', GraphicUtil.parseColor(colorG));
 			colorTable.put('T', GraphicUtil.parseColor(colorT));
 			colorTable.put('N', GraphicUtil.parseColor(colorN));
 		}
 
 		public void setBaseColor(char base, Color c) {
 			colorTable.put(Character.toUpperCase(base), c);
 			colorTable.put(Character.toLowerCase(base), new Color(c.getRed(), c.getGreen(), c.getBlue(), repeatColorAlpha));
 		}
 
 		public Color getBaseColor(char base) {
 			if (colorTable.containsKey(base))
 				return colorTable.get(base);
 			else
 				return Color.white;
 		}
 
 		public Color getReadColor(OnGenome g) {
 
 			if (SAMReadLight.class.isAssignableFrom(g.getClass())) {
 				return getSAMReadColor(SAMReadLight.class.cast(g));
 			}
 			return getReadColor_internal(g);
 		}
 
 		private Color getReadColor_internal(OnGenome g) {
 			if (showStrand) {
 				if (g instanceof Interval) {
 					Interval r = (Interval) g;
 					return r.isSense() ? COLOR_FORWARD_STRAND : COLOR_REVERSE_STRAND;
 				}
 			}
 			return COLOR_READ_DEFAULT;
 		}
 
 		public Color getClippedReadColor(OnGenome g) {
 			Color c = getReadColor(g);
 			return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (255 * clippedRegionAlpha));
 		}
 
 		private Color getSAMReadColor(SAMReadLight r) {
 			if (r.isPairedRead()) {
 				if (r.isMappedInProperPair())
 					return getReadColor_internal(r);
 				else
 					return r.isSense() ? COLOR_WIRED_READ_F : COLOR_WIRED_READ_R;
 			}
 			else {
 				return r.isSense() ? COLOR_ORPHAN_READ_F : COLOR_ORPHAN_READ_R;
 			}
 		}
 
 	}
 
 	private DrawStyle style;
 
 	public ReadCanvas(int width, int height, GenomeWindow window) {
 		this(width, height, window, new DrawStyle());
 	}
 
 	public ReadCanvas(int width, int height, GenomeWindow window, DrawStyle style) {
 		this.window = window;
 		this.style = style;
 		setPixelSize(width, height);
 	}
 
 	public void setPixelSize(int width, int height) {
 		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 		g = image.createGraphics();
 	}
 
 	public Graphics2D getGraphics() {
 		return g;
 	}
 
 	public void toPNG(OutputStream out) throws IOException {
 		ImageIO.write(image, "png", out);
 	}
 
 	public void toPNG(File out) throws IOException {
 		ImageIO.write(image, "png", out);
 	}
 
 	public void draw(List<OnGenome> dataSet) {
	    layout.setAllowOverlapPairedReads(style.overlapPairedReads);
 		layout.setTrackWindow(new TrackWindow(getPixelWidth(), (int) window.startIndexOnGenome, (int) window.endIndexOnGenome));
 		int maxOffset = layout.reset(dataSet, style.geneHeight);
 		final int h = style.geneHeight + style.geneMargin;
 		final int canvasHeight = (maxOffset + 1) * h;
 		setPixelSize(image.getWidth(), canvasHeight);
 
 		final ReadPainter painter = new ReadPainter();
 		layout.depthFirstSearch(new Visitor<IntervalLayout.LocusLayout>() {
 			public void visit(LocusLayout layout) {
 				painter.setLocusLayout(layout);
 				layout.getLocus().accept(painter);
 			}
 		});
 	}
 
 	private int getReadHeight() {
 		return style.geneHeight + style.geneMargin;
 	}
 
 	private class ReadPainter extends OnGenomeDataVisitorBase {
 		private LocusLayout currentLayout;
 
 		public void setLocusLayout(LocusLayout layout) {
 			this.currentLayout = layout;
 		}
 
 		public int getYPos() {
 			return currentLayout.scaledHeight(getReadHeight());
 		}
 
 		public int getYPos(int y) {
 			return LocusLayout.scaledHeight(y, getReadHeight());
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
 				y2 = getYPos(currentLayout.getYOffset() + 1);
 			}
 			else {
 				visitGap(pair.getGap());
 			}
 
 			drawSAMRead(first, y1);
 			drawSAMRead(second, y2);
 		}
 
 		@Override
 		public void visitSAMReadLight(SAMReadLight r) {
 			drawSAMRead(r, getYPos());
 		}
 
 		@Override
 		public void visitSAMReadPairFragment(SAMReadPairFragment fragment) {
 			visitGap(fragment.getGap());
 			drawSAMRead(fragment.oneEnd, getYPos());
 		}
 
 		@Override
 		public void visitGap(Gap p) {
 			drawPadding(p.getStart(), p.getEnd(), getYPos(), style.COLOR_GAP);
 		}
 	}
 
 	private int pixelPositionOnCanvas(int indexOnGenome) {
 		return window.pixelPositionOnWindow(indexOnGenome, image.getWidth());
 	}
 
 	public void drawRegion(OnGenome region, int y) {
 		drawGeneRect(region.getStart(), region.getEnd(), y, style.getReadColor(region));
 	}
 
 	public void drawGeneRect(int startOnGenome, int endOnGenome, int y, Color c) {
 
 		int x1 = pixelPositionOnCanvas(startOnGenome);
 		int x2 = pixelPositionOnCanvas(endOnGenome);
 
 		int boxWidth = x2 - x1;
 		if (boxWidth <= 0)
 			boxWidth = 1;
 
 		AffineTransform saved = g.getTransform();
 		g.translate(x1, y);
 		g.setColor(c);
 		g.fillRect(0, 0, boxWidth, style.geneHeight);
 		g.setTransform(saved);
 
 		if (_logger.isTraceEnabled())
 			_logger.trace(String.format("-gene rect - x:%d, y:%d, width:%d, height:%d, color:%s", x1, y, boxWidth, style.geneHeight, c.toString()));
 
 		if (style.drawShadow) {
 			g.setColor(style.COLOR_SHADOW);
 			//g.setStroke(new BasicStroke(1f));
 
 			saved = g.getTransform();
 			g.translate(x1, y);
 			g.drawLine(1, style.geneHeight, boxWidth, style.geneHeight);
 			g.drawLine(boxWidth, style.geneHeight, boxWidth, 0);
 			g.setTransform(saved);
 		}
 
 	}
 
 	public void drawPadding(int startOnGenome, int endOnGenome, int y, Color c) {
 
 		g.setColor(c);
 		g.setStroke(new BasicStroke(0.5f));
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		int yPos = (int) (y + (style.geneHeight / 2) + 0.5f);
 		g.drawLine((int) (pixelPositionOnCanvas(startOnGenome) + 0.5f), yPos, (int) (pixelPositionOnCanvas(endOnGenome) + 0.5f), yPos);
 	}
 
 	private static class PostponedInsertion {
 		final int start;
 		final String subseq;
 
 		public PostponedInsertion(int start, String subseq) {
 			this.start = start;
 			this.subseq = subseq;
 		}
 
 	}
 
 	public void drawSAMRead(SAMReadLight r, int y) {
 
 		try {
 			int cx1 = pixelPositionOnCanvas(r.unclippedStart);
 			int cx2 = pixelPositionOnCanvas(r.unclippedEnd);
 
 			int gx1 = pixelPositionOnCanvas(r.getStart());
 			int gx2 = pixelPositionOnCanvas(r.getEnd());
 
 			int width = gx2 - gx1;
 
 			if ((cx2 - cx1) <= 5) {
 				// when the pixel range is narrow, draw a rectangle only 
 				drawRegion(r, y);
 			}
 			else {
 
 				boolean drawBase = window.getGenomeRange() <= (image.getWidth() / style.fontWidth);
 
 				CIGAR cigar = new CIGAR(r.cigar);
 				int readStart = r.getStart();
 				int seqIndex = 0;
 
 				// Drawing insertions should be postponed after all of he read bases are painted.
 				List<PostponedInsertion> postponed = new ArrayList<PostponedInsertion>();
 				for (int cigarIndex = 0; cigarIndex < cigar.size(); cigarIndex++) {
 					CIGAR.Element e = cigar.get(cigarIndex);
 					int readEnd = readStart + e.length;
 					switch (e.type) {
 					case Deletions:
 						// ref : AAAAAA
 						// read: ---AAA
 						// cigar: 3D3M
 						drawPadding(readStart, readEnd, y, style.getReadColor(r));
 						break;
 					case Insertions:
 						// ref : ---AAA
 						// read: AAAAAA
 						// cigar: 3I3M
 						if (r.getSequence() != null)
 							postponed.add(new PostponedInsertion(readStart, r.getSequence().substring(seqIndex, seqIndex + e.length)));
 						readEnd = readStart;
 						seqIndex += e.length;
 						break;
 					case Padding:
 						// ref : AAAAAA
 						// read: ---AAA
 						// cigar: 3P3M
 						readEnd = readStart;
 						drawPadding(readStart, readStart + 1, y, style.COLOR_PADDING);
 						break;
 					case Matches: {
 
 						if (drawBase && r.getSequence() != null) {
 							drawBases(readStart, y, r.getSequence().substring(seqIndex, seqIndex + e.length),
 									r.getQV() != null ? r.getQV().substring(seqIndex, seqIndex + e.length) : null);
 						}
 						else {
 							drawGeneRect(readStart, readEnd, y, style.getReadColor(r));
 						}
 
 						seqIndex += e.length;
 					}
 						break;
 					case SkippedRegion:
 						drawPadding(readStart, readEnd, y, style.getReadColor(r));
 						break;
 					case SoftClip: {
 						int softclipStart = cigarIndex == 0 ? readStart - e.length : readStart;
 						int softclipEnd = cigarIndex == 0 ? readStart : readStart + e.length;
 						readEnd = softclipEnd;
 
 						if (drawBase && r.getSequence() != null) {
 							drawBases(softclipStart, y, r.getSequence().substring(seqIndex, seqIndex + e.length).toLowerCase(), r.getQV() != null ? r.getQV()
 									.substring(seqIndex, seqIndex + e.length) : null);
 						}
 						else {
 							drawGeneRect(softclipStart, softclipEnd, y, style.getClippedReadColor(r));
 						}
 
 						seqIndex += e.length;
 					}
 						break;
 					case HardClip:
 						break;
 					}
 					readStart = readEnd;
 				}
 
 				for (PostponedInsertion each : postponed) {
 					drawGeneRect(each.start, each.start + 1, y, new Color(0x11, 0x11, 0x11));
 				}
 			}
 
 		}
 		catch (UTGBClientException e) {
 			// when parsing CIGAR string fails, simply draw a rectangle
 			drawRegion(r, y);
 		}
 	}
 
 	public void drawBases(int startOnGenome, int y, String seq, String qual) {
 
 		Font f = new Font("SansSerif", Font.PLAIN, 1);
 		f = f.deriveFont(style.fontWidth);
 		g.setFont(f);
 
 		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
 		for (int i = 0; i < seq.length(); i++) {
 			int baseIndex = 8;
 			char base = seq.charAt(i);
 
 			drawBase(base, startOnGenome, y, style.getBaseColor(base));
 		}
 
 	}
 
 	public int getPixelWidth() {
 		return image.getWidth();
 	}
 
 	public void drawBase(char base, long startIndexOnGenome, int yOffset, Color color) {
 		int start = window.getXPosOnWindow(startIndexOnGenome, getPixelWidth());
 		int end = window.getXPosOnWindow(startIndexOnGenome + 1, getPixelWidth());
 		int drawStart;
 
 		String b = Character.toString(base);
 		g.setColor(color);
 		FontMetrics fontMetrics = g.getFontMetrics();
 		int fontWidth = fontMetrics.stringWidth(b);
 
 		drawStart = (int) (start + (end - start) / 2.0f - fontWidth / 2.0f);
 		if (drawStart < 0)
 			drawStart = end;
 
 		g.drawString(b, drawStart, yOffset);
 	}
 
 }
