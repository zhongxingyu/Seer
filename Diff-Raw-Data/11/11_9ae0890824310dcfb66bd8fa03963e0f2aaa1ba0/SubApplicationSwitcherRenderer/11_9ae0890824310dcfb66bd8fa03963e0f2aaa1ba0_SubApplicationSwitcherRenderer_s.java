 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation.ui.swt.lnf.renderer;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Control;
 
 import org.eclipse.riena.navigation.ui.swt.component.SubApplicationItem;
 import org.eclipse.riena.ui.core.marker.HiddenMarker;
 import org.eclipse.riena.ui.swt.lnf.AbstractLnfRenderer;
 import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.riena.ui.swt.lnf.rienadefault.RienaDefaultLnf;
 
 /**
  * Renderer of the switcher between to sub applications.
  */
 public class SubApplicationSwitcherRenderer extends AbstractLnfRenderer {
 
 	private Control control;
 	private List<SubApplicationItem> items;
 
 	@Override
 	public void paint(final GC gc, final Object value) {
 
 		super.paint(gc, value);
 
 		if (getBounds() == null) {
 			return;
 		}
 
 		Assert.isNotNull(gc);
 		Assert.isNotNull(value);
 		Assert.isTrue(value instanceof Control);
 		control = (Control) value;
 
 		final RienaDefaultLnf lnf = LnfManager.getLnf();
 
 		final SubApplicationTabRenderer tabRenderer = getRenderer();
 
 		// calculate width of all tab items
 		int allTabWidth = 0;
 		for (final SubApplicationItem item : getVisibleItems()) {
 			tabRenderer.setLabel(item.getLabel());
 			tabRenderer.setActive(item.isActivated());
 			final Point size = tabRenderer.computeSize(gc, null);
 			allTabWidth += size.x;
 		}
 
 		// line below tab items
 		final Color bottomColor = lnf.getColor(LnfKeyConstants.SUB_APPLICATION_SWITCHER_BORDER_BOTTOM_LEFT_COLOR);
 		final Color bottomSecondaryColor = lnf
 				.getColor(LnfKeyConstants.SUB_APPLICATION_SWITCHER_BORDER_BOTTOM_LEFT_SECONDARY_COLOR);
 		gc.setForeground(bottomColor);
 		int x = 0;
 		int y = getBounds().height - 1;
 		final int x2 = getBounds().width;
 		final int y2 = y;
 		gc.drawLine(x, y - 1, x2, y2 - 1);
 		gc.setForeground(bottomSecondaryColor);
 		gc.drawLine(x, y, x2, y2);
 
 		// all NOT active tab items
 		int xPosition = 0;
 		final int position = lnf.getIntegerSetting(LnfKeyConstants.SUB_APPLICATION_SWITCHER_HORIZONTAL_TAB_POSITION);
 		if (position == SWT.LEFT) {
 			xPosition = 10;
 		} else if (position == SWT.RIGHT) {
 			xPosition = getBounds().width - 10 - allTabWidth;
 		} else {
 			xPosition = getBounds().width / 2 - allTabWidth / 2;
 		}
 		x = xPosition;
 		for (final SubApplicationItem item : getVisibleItems()) {
 			initItemRenderer(tabRenderer, item);
 			final Point size = tabRenderer.computeSize(gc, null);
			y = getBounds().height - size.y;
			tabRenderer.setBounds(x, y, size.x, size.y);
 			if (!item.isActivated()) {
 				tabRenderer.paint(gc, control);
 				item.setBounds(tabRenderer.getBounds());
 			}
 			x += size.x;
 		}
 
 		// active tab item
 		x = xPosition;
 		for (final SubApplicationItem item : getVisibleItems()) {
 			initItemRenderer(tabRenderer, item);
 			final Point size = tabRenderer.computeSize(gc, null);
 			if (item.isActivated()) {
 				y = getBounds().height - size.y;
 				tabRenderer.setBounds(x, y, size.x, size.y);
 				tabRenderer.paint(gc, control);
 				item.setBounds(tabRenderer.getBounds());
 			}
 			x += size.x;
 		}
 
 	}
 
 	/**
 	 * Returns a list of all visible sub-application items.
 	 * 
 	 * @return visible items
 	 */
 	private List<SubApplicationItem> getVisibleItems() {
 
 		final List<SubApplicationItem> visibleItems = new ArrayList<SubApplicationItem>();
 
 		for (final SubApplicationItem item : getItems()) {
 			if (item.getMarkersOfType(HiddenMarker.class).isEmpty()) {
 				visibleItems.add(item);
 			}
 		}
 
 		return visibleItems;
 
 	}
 
 	/**
 	 * Sets some properties of the renderer. The correct value delivers the
 	 * given item.
 	 * 
 	 * @param tabRenderer
 	 *            renderer of an item
 	 * @param item
 	 */
 	private void initItemRenderer(final SubApplicationTabRenderer tabRenderer, final SubApplicationItem item) {
 
 		tabRenderer.setLabel(item.getLabel());
 		tabRenderer.setIcon(item.getIcon());
 		tabRenderer.setActive(item.isActivated());
 		tabRenderer.setMarkers(item.getMarkers());
 
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.ui.swt.lnf.ILnfRenderer#dispose()
 	 */
 	public void dispose() {
 		for (final SubApplicationItem item : getItems()) {
 			item.dispose();
 		}
 	}
 
 	/**
 	 * @return the items
 	 */
 	private List<SubApplicationItem> getItems() {
 		if (items == null) {
 			items = new ArrayList<SubApplicationItem>();
 		}
 		return items;
 	}
 
 	/**
 	 * @param items
 	 *            the items to set
 	 */
 	public void setItems(final List<SubApplicationItem> items) {
 		this.items = items;
 	}
 
 	/**
 	 * Returns the renderer of a tab
 	 * 
 	 * @return renderer of tab
 	 */
 	private SubApplicationTabRenderer getRenderer() {
 		return (SubApplicationTabRenderer) LnfManager.getLnf()
 				.getRenderer(LnfKeyConstants.SUB_APPLICATION_TAB_RENDERER);
 	}
 
 }
