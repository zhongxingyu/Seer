 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
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
 
 import org.eclipse.riena.navigation.ui.swt.component.SubApplicationItem;
 import org.eclipse.riena.ui.swt.lnf.AbstractLnfRenderer;
 import org.eclipse.riena.ui.swt.lnf.ILnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.riena.ui.swt.lnf.rienadefault.RienaDefaultLnf;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Point;
 
 /**
  * Renderer of the switcher between to sub applications.
  */
 public class SubApplicationSwitcherRenderer extends AbstractLnfRenderer {
 
 	private List<SubApplicationItem> items;
 
 	/**
 	 * @see org.eclipse.riena.ui.swt.lnf.AbstractLnfRenderer#paint(org.eclipse.swt.graphics.GC,
 	 *      java.lang.Object)
 	 */
 	@Override
 	public void paint(GC gc, Object value) {
 
 		RienaDefaultLnf lnf = LnfManager.getLnf();
 
 		SubApplicationTabRenderer tabRenderer = getRenderer();
 
 		// calculate width of all tab items
 		int allTabWidth = 0;
 		for (SubApplicationItem item : getItems()) {
 			tabRenderer.setLabel(item.getLabel());
 			tabRenderer.setActivated(item.isActivated());
 			Point size = tabRenderer.computeSize(gc, null);
 			allTabWidth += size.x;
 		}
 
 		// line below tab items
 		Color bottomColor = lnf.getColor(ILnfKeyConstants.SUB_APPLICATION_SWITCHER_BORDER_BOTTOM_LEFT_COLOR);
 		gc.setForeground(bottomColor);
 		int x = 0;
 		int y = getBounds().height - 1;
 		int x2 = getBounds().width;
 		int y2 = y;
 		gc.drawLine(x, y, x2, y2);
 
 		// all NOT active tab items
 		int xPosition = 0;
 		int position = lnf.getIntegerSetting(ILnfKeyConstants.SUB_APPLICATION_SWITCHER_HORIZONTAL_TAB_POSITION);
 		if (position == SWT.LEFT) {
 			xPosition = 10;
 		} else if (position == SWT.RIGHT) {
 			xPosition = getBounds().width - 10 - allTabWidth;
 		} else {
 			xPosition = getBounds().width / 2 - allTabWidth / 2;
 		}
 		x = xPosition;
 		for (SubApplicationItem item : getItems()) {
 			tabRenderer.setLabel(item.getLabel());
 			tabRenderer.setIcon(item.getIcon());
 			tabRenderer.setActivated(item.isActivated());
 			Point size = tabRenderer.computeSize(gc, null);
 			y = getBounds().height - size.y;
 			tabRenderer.setBounds(x, y, size.x, size.y);
 			if (!item.isActivated()) {
 				tabRenderer.paint(gc, null);
 				item.setBounds(tabRenderer.getBounds());
 			}
 			x += size.x;
 		}
 
 		// active tab item
 		x = xPosition;
 		for (SubApplicationItem item : getItems()) {
 			tabRenderer.setLabel(item.getLabel());
 			tabRenderer.setIcon(item.getIcon());
 			tabRenderer.setActivated(item.isActivated());
 			Point size = tabRenderer.computeSize(gc, null);
 			if (item.isActivated()) {
 				y = getBounds().height - size.y;
 				tabRenderer.setBounds(x, y, size.x, size.y);
 				tabRenderer.paint(gc, null);
 				item.setBounds(tabRenderer.getBounds());
 			}
 			x += size.x;
 		}
 
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.ui.swt.lnf.ILnfRenderer#dispose()
 	 */
 	public void dispose() {
 		for (SubApplicationItem item : getItems()) {
 			item.dispose();
 		}
 	}
 
 	/**
 	 * @return the items
 	 */
	public List<SubApplicationItem> getItems() {
 		if (items == null) {
 			items = new ArrayList<SubApplicationItem>();
 		}
 		return items;
 	}
 
 	/**
 	 * @param items
 	 *            the items to set
 	 */
 	public void setItems(List<SubApplicationItem> items) {
 		this.items = items;
 	}
 
 	/**
 	 * Returns the renderer of a tab
 	 * 
 	 * @return renderer of tab
 	 */
 	private SubApplicationTabRenderer getRenderer() {
 		return (SubApplicationTabRenderer) LnfManager.getLnf().getRenderer(
 				ILnfKeyConstants.SUB_APPLICATION_TAB_RENDERER);
 	}
 
 }
