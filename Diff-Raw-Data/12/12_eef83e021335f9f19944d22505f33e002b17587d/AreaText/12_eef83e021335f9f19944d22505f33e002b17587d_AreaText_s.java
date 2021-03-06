 /*
  * Scriptographer
  * 
  * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
  * 
  * Copyright (c) 2002-2008 Juerg Lehni, http://www.scratchdisk.com.
  * All rights reserved.
  *
  * Please visit http://scriptographer.com/ for updates and contact.
  * 
  * -- GPL LICENSE NOTICE --
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
  * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  * -- GPL LICENSE NOTICE --
  * 
  * File created on 23.10.2005.
  * 
  * $Id$
  */
 
 package com.scriptographer.ai;
 
 /**
  * @author lehni
  */
 public class AreaText extends TextItem {
 
 	protected AreaText(int handle) {
 		super(handle, false);
 	}
 	
 	native private static int nativeCreate(int orientation, int artHandle);
 	
 	/**
 	 * Creates an area text item using the supplied path.
 	 * 
 	 * Sample code:
 	 * <code>
 	 * // create an abstract rectangle:
 	 * var bottomLeft = new Point(10, 10);
 	 * var size = new Size(200, 100);
 	 * var rectangle = new Rectangle(bottomLeft, size);
 	 * 
 	 * // create a rectangular path using the abstract rectangle:
 	 * var path = new Path.Rectangle(rectangle);
 	 * 
 	 * // create a area text item using the path:
 	 * var areaText = new AreaText(path);
 	 * 
 	 * // add some text to the path
 	 * areaText.content = 'Text which will appear within the path';
 	 * </code>
 	 * 
 	 * @param area the path in which the text will appear
 	 * @param orient the text orientation {@default 'horizontal'}
 	 */
 	public AreaText(Path area, TextOrientation orientation) {
 		super(nativeCreate(orientation != null
 				? orientation.value : TextOrientation.HORIZONTAL.value,
 				area != null ? area.handle : 0), true);
 	}
 
 	public AreaText(Path area) {
 		this(area, TextOrientation.HORIZONTAL);
 	}
 
 	/**
	 * The path of the area text.
      */
 	public Path getTextPath() {
 		return (Path) getFirstChild();
 	}
 	
 	/**
 	 * The number of rows for the text frame.
 	 * {@grouptitle Rows and Columns}
 	 */
 	public native int getRowCount();
 	public native void setRowCount(int count);
 	
 	/**
 	 * The number of columns for the text frame.
 	 */
 	public native int getColumnCount();
 	public native void setColumnCount(int count);
 
 	/**
 	 * Specifies whether the text area uses row major order.
 	 * When set to <code>true</code>, the text flows through the columns
 	 * after which it flows to the first column of the next row. When set to
 	 * <code>false</code>, the text flows through the rows after which it
 	 * flows to the first row of the next column.
 	 */
 	public native boolean getRowMajorOrder();
 	public native void setRowMajorOrder(boolean isRowMajor);
 	
 	/**
 	 * The row gutter in the text frame.
 	 */
 	public native float getRowGutter();
 	public native void setRowGutter(float gutter);
 
 	/**
 	 * The column gutter in the text frame.
 	 */	
 	public native float getColumnGutter();
 	public native void setColumnGutter(float gutter);
 }
