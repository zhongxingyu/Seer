 /* IValaColorConstants.java
  *
  * Copyright (C) 2007  Johann Prieur <johann.prieur@gmail.com>
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  */
 package valable.editors.util;
 
 import org.eclipse.swt.graphics.RGB;
 
 public interface IColorConstants {
 	
 	public static final RGB DEFAULT = new RGB(0, 0, 0);
 	public static final RGB KEYWORD = new RGB(127, 0, 85);
 	public static final RGB NUMBER = new RGB(0, 0, 255);
 	public static final RGB TYPE = new RGB(64, 0, 200);
 	public static final RGB STRING = new RGB(42, 0, 255);
 	
 	public static final RGB COMMENT = new RGB(63, 127, 95);
 	
	public static final RGB GTKDOC_DEFAULT = new RGB(63, 127, 95);
 	public static final RGB GTKDOC_TAG = new RGB(127, 159, 191);
 	public static final RGB GTKDOC_DOCBOOK = new RGB(127, 127, 159);
 	public static final RGB GTKDOC_OTHER = new RGB(63, 95, 191);
 
 }
