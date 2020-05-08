 /*
  * Copyright 2012 Odysseus Software GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package de.odysseus.ithaka.treemap;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.Rectangle;
 
 public class TreemapRenderer {
 	public static enum AttachLabelMode {
 		AnyVisible, // any frames and leafs
 		VisibleForked, // subsequent frames and highest visible descendants of forked cell
 		TopLevelsOnly; // root elements, frames and first non-frame child
 	}
 
 	public static enum TruncateLabelMode {
 		LeadingDots,
 		TrailingDots;
 	}
 	
 	private static final String DOTS = "..."; // TODO "\u2026";
 		
 	private final TreemapLabelProvider labelProvider;
 	private final TreemapColorProvider colorProvider;
 	private final int labelSpaceLR;
 	private final int labelSpaceTB;
 
 	private int labelLevels = Integer.MAX_VALUE;
 	private AttachLabelMode attachLabelMode = AttachLabelMode.VisibleForked;
 	private TruncateLabelMode truncateLabelMode = TruncateLabelMode.TrailingDots;
 
 	public TreemapRenderer(TreemapLabelProvider labelProvider, TreemapColorProvider colorProvider) {
 		this(labelProvider, colorProvider, 4, 2);
 	}
 
 	public TreemapRenderer(
 			TreemapLabelProvider labelProvider,
 			TreemapColorProvider colorProvider,
 			int labelSpaceLR,
 			int labelSpaceTB) {
 		this.labelProvider = labelProvider;
 		this.colorProvider = colorProvider;
 		this.labelSpaceLR = labelSpaceLR;
 		this.labelSpaceTB = labelSpaceTB;
 	}
 
 	/**
 	 * Render all cells and labels.
 	 * @param graphics
 	 * @param layout
 	 */
 	public void render(TreemapGraphics graphics, Treemap layout) {
 		renderTree(graphics, layout);
 		if (labelProvider != null) {
 			renderLabels(graphics, layout);
 		}
 	}
 
 	/**
 	 * Render all cells.
 	 * @param graphics
 	 * @param layout
 	 */
 	public void renderTree(TreemapGraphics graphics, Treemap layout) {
 		for (Object item : layout.getElements()) {
 			renderTree(graphics, layout, item, false);
 		}
 	}
 
 	/**
 	 * Render cells, starting at specified item.
 	 * @param graphics
 	 * @param layout
 	 * @param item
 	 * @param active
 	 */
 	public void renderTree(TreemapGraphics graphics, Treemap layout, Object item, boolean active) {
 		TreemapCell cell = layout.getCell(item);
 		if (cell == null) {
 			return;
 		}
 		boolean visible = cell.isLeaf() || cell.getFramed() != null;
 		Object value = cell.getValue();
 
 		if (active) {
 			if (visible) {
 				graphics.fill(cell, colorProvider.getHighlightColor(item, value));
 			}
 		} else {
 			Rectangle borders = cell.getBorders();
 			if (borders != null) {
 				graphics.fill(borders, colorProvider.getBorderColor(item, value));
 			}
 			if (visible) {
 				Color color1 = colorProvider.getBackgroundColor(item, value);
 				Color color2 = colorProvider.getBackgroundGradientColor(item, value);
 				graphics.fill(cell, color1, color2);
 			}
 		}
 		if (!cell.isLeaf()) {
 			Rectangle framed = cell.getFramed();
 			if (framed != null) {
 				graphics.fill(framed, colorProvider.getBorderColor(item, value));
 			}
 			for (Object node : cell.getChildren()) {
 				renderTree(graphics, layout, node, false);
 			}
 		}
 	}
 
 	/**
 	 * Render all labels.
 	 * @param graphics
 	 * @param layout
 	 * @param input
 	 */
 	public void renderLabels(TreemapGraphics graphics, Treemap layout) {
 		Object[] elements = layout.getElements();
 		for (Object node : elements) {
 			renderLabels(graphics, layout, node, elements.length > 1, labelLevels);
 		}
 	}
 
 	private void renderLabels(TreemapGraphics graphics, Treemap layout, Object item, boolean forked, int levels) {
 		if (levels == 0) {
 			return;
 		}
 		TreemapCell cell = layout.getCell(item);
 		if (cell == null) {
 			return;
 		}
 		boolean attachLabel;
 		switch (attachLabelMode) {
 		case AnyVisible:
 			attachLabel = cell.isLeaf() || cell.getFramed() != null;
 			break;
 		case VisibleForked:
 			attachLabel = cell.isLeaf() || cell.getFramed() != null || forked;
 			break;
 		default:
 			attachLabel = true;
 		}
 		if (attachLabel) {
			if (renderLabel(graphics, cell, item)) {
 				renderChildrenLabels(graphics, layout, cell.getChildren(), levels - 1);
 			}
 		} else if (attachLabelMode != AttachLabelMode.TopLevelsOnly) {
 			renderChildrenLabels(graphics, layout, cell.getChildren(), levels - 1);
 		}
 	}
 
 	private void renderChildrenLabels(TreemapGraphics graphics, Treemap layout, Object[] children, int levels) {
 		boolean forked = children.length > 1;
 		for (Object child : children) {
 			renderLabels(graphics, layout, child, forked, levels);
 		}
 	}
 
 	private String truncateLabel(String label, int availableWidth, int requiredWidth, int minCharacters) {
 		int fittingCharacters = (label.length() * availableWidth) / requiredWidth - DOTS.length();
 		if (fittingCharacters >= minCharacters) { // at least three character --> render label
 			switch (truncateLabelMode) {
 			case LeadingDots:
 				return DOTS + label.substring(label.length() - fittingCharacters);
 			case TrailingDots:
 				return label.substring(0, fittingCharacters) + DOTS;
 			}
 		}
 		return null;
 	}
 
 	private boolean renderLabel(TreemapGraphics graphics, TreemapCell cell, Object item) {
 		Rectangle framed = cell.getFramed();
 		int width = framed != null ? (cell.width - framed.width) / 2 : cell.width;
 		int height = framed != null ? (cell.height - framed.height) / 2 : cell.height;
 		Object value = cell.getValue();
 		String label = labelProvider.getText(item, value);
 		Dimension dimension = graphics.getDimension(label);
 		Point anchor = null;
 		boolean vertical = false;
 		if (dimension.width <= cell.width - labelSpaceLR && dimension.height <= height - labelSpaceTB) { // string fits horizontally
 			anchor = new Point((cell.width - dimension.width) / 2, (height - dimension.height) / 2);
 		} else if (dimension.width <= cell.height - labelSpaceLR && dimension.height <= width - labelSpaceTB) {	// string fits vertically
 			anchor = new Point((width - dimension.height) / 2, (cell.height + dimension.width) / 2);
 			vertical = true;
 		} else if (truncateLabelMode != null) { // string doesn't fit, try to truncate...
 			String hLabel = null;
 			if (dimension.height <= height - labelSpaceTB) {
 				hLabel = truncateLabel(label, cell.width - labelSpaceLR, dimension.width, 3);
 			}
 			String vLabel = null;
 			if (dimension.height <= width - labelSpaceTB && (hLabel == null || cell.height > cell.width)) {
 				vLabel = truncateLabel(label, cell.height - labelSpaceLR, dimension.width, 3);
 			}
 			if (hLabel != null && (vLabel == null || hLabel.length() >= vLabel.length())) { // ... horizontally
 				dimension = graphics.getDimension(hLabel);
 				anchor = new Point((cell.width - dimension.width) / 2, (height - dimension.height) / 2);
 				label = hLabel;
 			} else if (vLabel != null) { // ... vertically
 				dimension = graphics.getDimension(vLabel);
 				anchor = new Point((width - dimension.height) / 2, (cell.height + dimension.width) / 2);
 				label = vLabel;
 				vertical = true;
 			}
 		}
 		if (anchor == null) {
 			return false;
 		}
 		// label background in dark gray
 		Color shadow = colorProvider.getForegroundShadowColor(item, value);
 		if (shadow != null) {
 			graphics.draw(label, cell.x + anchor.x + 1, cell.y + anchor.y, shadow, vertical);
 			graphics.draw(label, cell.x + anchor.x - 1, cell.y + anchor.y, shadow, vertical);
 			graphics.draw(label, cell.x + anchor.x, cell.y + anchor.y + 1, shadow, vertical);
 			graphics.draw(label, cell.x + anchor.x, cell.y + anchor.y - 1, shadow, vertical);
 		}
 		// label in foreground color
 		Color foreground = colorProvider.getForegroundColor(item, value);
 		graphics.draw(label, cell.x + anchor.x, cell.y + anchor.y, foreground, vertical);
 
 		return true;
 	}
 
 	public TruncateLabelMode getTruncateLabelMode() {
 		return truncateLabelMode;
 	}
 	
 	public void setTruncateLabelMode(TruncateLabelMode truncateLabelMode) {
 		this.truncateLabelMode = truncateLabelMode;
 	}
 	
 	public AttachLabelMode getAttachLabelMode() {
 		return attachLabelMode;
 	}
 	
 	public void setAttachLabelMode(AttachLabelMode attachLabelMode) {
 		this.attachLabelMode = attachLabelMode;
 	}
 	
 	public int getLabelLevels() {
 		return labelLevels;
 	}
 	
 	public void setLabelLevels(int labelLevels) {
 		this.labelLevels = labelLevels;
 	}
 }
