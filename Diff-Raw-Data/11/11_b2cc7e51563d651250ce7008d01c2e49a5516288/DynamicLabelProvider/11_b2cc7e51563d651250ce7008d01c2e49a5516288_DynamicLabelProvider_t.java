 /*******************************************************************************
  * Copyright (c) 2012 Olivier Moises
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Olivier Moises- initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.wazaabi.engine.swt.views.collections;
 
 import java.util.HashMap;
 import java.util.List;
 
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.wazaabi.engine.edp.EDPSingletons;
 import org.eclipse.wazaabi.engine.edp.coderesolution.AbstractCodeDescriptor;
 import org.eclipse.wazaabi.mm.core.styles.ColorRule;
 import org.eclipse.wazaabi.mm.core.styles.FontRule;
 import org.eclipse.wazaabi.mm.core.styles.collections.DynamicProvider;
 
 public class DynamicLabelProvider implements ILabelProvider,
 		ITableLabelProvider {
 
 	private AbstractCodeDescriptor.MethodDescriptor getTextMethodDescriptor = null;
 	private AbstractCodeDescriptor.MethodDescriptor getColumnTextMethodDescriptor = null;
 	private AbstractCodeDescriptor.MethodDescriptor getImageMethodDescriptor = null;
 	private AbstractCodeDescriptor.MethodDescriptor getColumnImageMethodDescriptor = null;
 	private AbstractCodeDescriptor.MethodDescriptor getBackgroundColorMethodDescriptor = null;
 	private AbstractCodeDescriptor.MethodDescriptor getColumnBackgroundColorMethodDescriptor = null;
 	private AbstractCodeDescriptor.MethodDescriptor getForegroundColorMethodDescriptor = null;
 	private AbstractCodeDescriptor.MethodDescriptor getColumnForegroundColorMethodDescriptor = null;
 	private AbstractCodeDescriptor.MethodDescriptor getFontMethodDescriptor = null;
 	private AbstractCodeDescriptor.MethodDescriptor getColumnFontMethodDescriptor = null;
 	// TODO : very bad and verbose code
 	// we should be able to get the codeDescriptor from the methodDescriptor
 	private AbstractCodeDescriptor getTextCodeDescriptor = null;
 	private AbstractCodeDescriptor getColumnTextCodeDescriptor = null;
 
 	private AbstractCodeDescriptor getImageCodeDescriptor = null;
 	private AbstractCodeDescriptor getColumnImageCodeDescriptor = null;
 	private AbstractCodeDescriptor getBackgroundColorCodeDescriptor = null;
 	private AbstractCodeDescriptor getColumnBackgroundColorCodeDescriptor = null;
 	private AbstractCodeDescriptor getForegroundColorCodeDescriptor = null;
 	private AbstractCodeDescriptor getColumnForegroundColorCodeDescriptor = null;
 	private AbstractCodeDescriptor getFontCodeDescriptor = null;
 	private AbstractCodeDescriptor getColumnFontCodeDescriptor = null;
 
 	public void updateDynamicProviderURIs(
 			List<DynamicProvider> dynamicProviders, String baseURI) {
 		for (DynamicProvider dynamicProvider : dynamicProviders) {
 			String uri = dynamicProvider.getUri();
 			if (baseURI != null && !baseURI.isEmpty())
 				uri = EDPSingletons.getComposedCodeLocator().getFullPath(
 						baseURI, uri, dynamicProvider);
 			AbstractCodeDescriptor codeDescriptor = EDPSingletons
 					.getComposedCodeLocator().resolveCodeDescriptor(uri);
 			if (codeDescriptor != null) {
 				AbstractCodeDescriptor.MethodDescriptor methodDescriptor = codeDescriptor
 						.getMethodDescriptor(
 								"getText", new String[] { "element" }, new Class[] { Object.class }, String.class); //$NON-NLS-1$ //$NON-NLS-2$
 				if (methodDescriptor != null) {
 					getTextMethodDescriptor = methodDescriptor;
 					getTextCodeDescriptor = codeDescriptor;
 				}
 				methodDescriptor = codeDescriptor
 						.getMethodDescriptor(
 								"getText", new String[] { "element", "columnIndex" }, new Class[] { Object.class, int.class }, String.class); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				if (methodDescriptor != null) {
 					getColumnTextMethodDescriptor = methodDescriptor;
 					getColumnTextCodeDescriptor = codeDescriptor;
 				}
 
 				methodDescriptor = codeDescriptor
 						.getMethodDescriptor(
 								"getImage", new String[] { "element" }, new Class[] { Object.class }, Image.class); //$NON-NLS-1$ //$NON-NLS-2$
 				if (methodDescriptor != null) {
 					getImageMethodDescriptor = methodDescriptor;
 					getImageCodeDescriptor = codeDescriptor;
 				}
 				methodDescriptor = codeDescriptor
 						.getMethodDescriptor(
 								"getImage", new String[] { "element", "columnIndex" }, new Class[] { Object.class, int.class }, Image.class); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				if (methodDescriptor != null) {
 					getColumnImageMethodDescriptor = methodDescriptor;
 					getColumnImageCodeDescriptor = codeDescriptor;
 				}
 
 				methodDescriptor = codeDescriptor
 						.getMethodDescriptor(
 								"getBackgroundColor", new String[] { "element" }, new Class[] { Object.class }, ColorRule.class); //$NON-NLS-1$ //$NON-NLS-2$
 				if (methodDescriptor != null) {
 					getBackgroundColorMethodDescriptor = methodDescriptor;
 					getBackgroundColorCodeDescriptor = codeDescriptor;
 				}
 				methodDescriptor = codeDescriptor
 						.getMethodDescriptor(
 								"getBackgroundColor", new String[] { "element", "columnIndex" }, new Class[] { Object.class, int.class }, ColorRule.class); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				if (methodDescriptor != null) {
 					getColumnBackgroundColorMethodDescriptor = methodDescriptor;
 					getColumnBackgroundColorCodeDescriptor = codeDescriptor;
 				}
 
 				methodDescriptor = codeDescriptor
 						.getMethodDescriptor(
 								"getForegroundColor", new String[] { "element" }, new Class[] { Object.class }, ColorRule.class); //$NON-NLS-1$ //$NON-NLS-2$
 				if (methodDescriptor != null) {
 					getForegroundColorMethodDescriptor = methodDescriptor;
 					getForegroundColorCodeDescriptor = codeDescriptor;
 				}
 				methodDescriptor = codeDescriptor
 						.getMethodDescriptor(
 								"getForegroundColor", new String[] { "element", "columnIndex" }, new Class[] { Object.class, int.class }, ColorRule.class); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				if (methodDescriptor != null) {
 					getColumnForegroundColorMethodDescriptor = methodDescriptor;
 					getColumnForegroundColorCodeDescriptor = codeDescriptor;
 				}
 
 				methodDescriptor = codeDescriptor
 						.getMethodDescriptor(
 								"getFont", new String[] { "element" }, new Class[] { Object.class }, FontRule.class); //$NON-NLS-1$ //$NON-NLS-2$
 				if (methodDescriptor != null) {
 					getFontMethodDescriptor = methodDescriptor;
 					getFontCodeDescriptor = codeDescriptor;
 				}
 				methodDescriptor = codeDescriptor
 						.getMethodDescriptor(
 								"getFont", new String[] { "element", "columnIndex" }, new Class[] { Object.class, int.class }, FontRule.class); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				if (methodDescriptor != null) {
 					getColumnFontMethodDescriptor = methodDescriptor;
 					getColumnFontCodeDescriptor = codeDescriptor;
 				}
 			}
 		}
 	}
 
 	public void addListener(ILabelProviderListener listener) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void dispose() {
 		for (Color color : registeredColors.values())
 			color.dispose();
 		registeredColors.clear();
 		for (Font font : registeredFonts.values())
 			font.dispose();
 		registeredFonts.clear();
 	}
 
 	public boolean isLabelProperty(Object element, String property) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public void removeListener(ILabelProviderListener listener) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public Image getColumnImage(Object element, int columnIndex) {
 		if (getColumnImageMethodDescriptor != null
 				&& getColumnImageCodeDescriptor != null) {
 			return (Image) getColumnImageCodeDescriptor.invokeMethod(
 					getColumnImageMethodDescriptor, new Object[] { element,
 							columnIndex });
 		}
 		if (columnIndex == 0)
 			if (getImageMethodDescriptor != null
 					&& getImageCodeDescriptor != null) {
 				return (Image) getImageCodeDescriptor.invokeMethod(
 						getImageMethodDescriptor, new Object[] { element });
 			}
 		return null;
 	}
 
 	public String getColumnText(Object element, int columnIndex) {
 		if (getColumnTextMethodDescriptor != null
 				&& getColumnTextCodeDescriptor != null) {
 			String result = (String) getColumnTextCodeDescriptor.invokeMethod(
 					getColumnTextMethodDescriptor, new Object[] { element,
 							columnIndex });
 			return result != null ? result : ""; //$NON-NLS-1$
 		}
 		if (columnIndex == 0)
 			if (getTextMethodDescriptor != null
 					&& getTextCodeDescriptor != null) {
 				String result = (String) getTextCodeDescriptor.invokeMethod(
 						getTextMethodDescriptor, new Object[] { element });
 				return result != null ? result : ""; //$NON-NLS-1$
 			}
 		return ""; //$NON-NLS-1$
 	}
 
 	public Image getImage(Object element) {
 		return getColumnImage(element, 0);
 	}
 
 	public String getText(Object element) {
 		if (getTextMethodDescriptor != null && getTextCodeDescriptor != null) {
 			String result = (String) getTextCodeDescriptor.invokeMethod(
 					getTextMethodDescriptor, new Object[] { element });
 			return result != null ? result : ""; //$NON-NLS-1$
 		}
 		return getColumnText(element, 0);
 	}
 
 	public Color getBackgroundColor(Object element, int columnIndex,
 			Display display) {
 		if (getColumnBackgroundColorMethodDescriptor != null
 				&& getColumnBackgroundColorCodeDescriptor != null) {
 			return getRegisteredColor(
 					(ColorRule) getColumnBackgroundColorCodeDescriptor.invokeMethod(
 							getColumnBackgroundColorMethodDescriptor,
 							new Object[] { element, columnIndex }), display);
 		}
 		if (getBackgroundColorMethodDescriptor != null
 				&& getBackgroundColorCodeDescriptor != null) {
 			return getRegisteredColor(
 					(ColorRule) getBackgroundColorCodeDescriptor.invokeMethod(
 							getBackgroundColorMethodDescriptor,
 							new Object[] { element }), display);
 		}
 		return null;
 	}
 
 	public Color getForegroundColor(Object element, int columnIndex,
 			Display display) {
 		if (getColumnForegroundColorMethodDescriptor != null
 				&& getColumnForegroundColorCodeDescriptor != null) {
 			return getRegisteredColor(
 					(ColorRule) getColumnForegroundColorCodeDescriptor.invokeMethod(
 							getColumnForegroundColorMethodDescriptor,
 							new Object[] { element, columnIndex }), display);
 		}
 		if (getForegroundColorMethodDescriptor != null
 				&& getForegroundColorCodeDescriptor != null) {
 			return getRegisteredColor(
 					(ColorRule) getForegroundColorCodeDescriptor.invokeMethod(
 							getForegroundColorMethodDescriptor,
 							new Object[] { element }), display);
 		}
 		return null;
 	}
 
 	public Font getFont(Object element, int columnIndex, Display display,
 			Font existingFont) {
 		if (getColumnFontMethodDescriptor != null
 				&& getColumnFontCodeDescriptor != null) {
 			return getRegisteredFont(
 					(FontRule) getColumnFontCodeDescriptor.invokeMethod(
 							getColumnFontMethodDescriptor, new Object[] {
 									element, columnIndex }), display,
 					existingFont);
 		}
 		if (getFontMethodDescriptor != null && getFontCodeDescriptor != null) {
 			return getRegisteredFont(
 					(FontRule) getFontCodeDescriptor.invokeMethod(
 							getFontMethodDescriptor, new Object[] { element }),
 					display, existingFont);
 		}
 		return null;
 	}
 
 	private HashMap<RGB, Color> registeredColors = new HashMap<RGB, Color>();
 
 	protected Color getRegisteredColor(ColorRule colorRule, Display display) {
 		if (colorRule == null)
 			return null;
 		RGB rgb = new RGB(colorRule.getRed(), colorRule.getGreen(),
 				colorRule.getBlue());
 		Color color = registeredColors.get(rgb);
 		if (color == null) {
 			color = new Color(display, rgb);
 			registeredColors.put(rgb, color);
 		}
 		return color;
 	}
 
 	private HashMap<FontData, Font> registeredFonts = new HashMap<FontData, Font>();
 
 	protected Font getRegisteredFont(FontRule fontRule, Display display,
 			Font existingFont) {
 
 		if (fontRule == null)
 			return existingFont;
 
 		FontData oldFontData = existingFont.getFontData()[0];
 		FontData newFontData = new FontData();
 		if (fontRule.getName() != null && !fontRule.getName().isEmpty())
			newFontData.setName(fontRule.getName());
 		else
			newFontData.setName(oldFontData.getName());
 		if (fontRule.getHeight() > 0)
 			newFontData.height = fontRule.getHeight();
 		else
 			newFontData.height = oldFontData.height;
 
 		if (fontRule.isItalic())
			newFontData.setStyle(SWT.ITALIC | newFontData.getStyle());
 
 		if (fontRule.isBold())
			newFontData.setStyle(SWT.BOLD | newFontData.getStyle());
 		if (oldFontData.equals(newFontData))
 			return null;
 
 		Font font = registeredFonts.get(newFontData);
 		if (font == null) {
 			font = new Font(display, newFontData);
 			registeredFonts.put(newFontData, font);
 		}
 		return font;
 	}
 }
