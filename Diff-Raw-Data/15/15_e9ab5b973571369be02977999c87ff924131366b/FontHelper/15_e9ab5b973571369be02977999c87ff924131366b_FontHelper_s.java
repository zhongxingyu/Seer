 /**
  * 
  */
 package org.mwc.cmap.core.property_support;
 
 import org.eclipse.jface.resource.FontRegistry;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.DialogCellEditor;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FontDialog;
 
 public class FontHelper extends EditorHelper
 {
 	public static class FontDataDialogCellEditor extends DialogCellEditor
 	{
 
 		public FontDataDialogCellEditor(Composite parent)
 		{
 			super(parent);
 		}
 
 		protected Object openDialogBox(Control cellEditorWindow)
 		{
 			Font res = null;
 			FontDialog ftDialog = new FontDialog(cellEditorWindow.getShell());
 			Font thisFont = (Font) getValue();
 			if(thisFont != null)
 			{
 				FontData[] list = thisFont.getFontData();
 				ftDialog.setFontList(list);				
 			}
 			FontData fData = ftDialog.open();
 			if(fData != null)
 			{
 				res = new Font(Display.getCurrent(), fData);
 				
 			}
 				
 			return res;
 		}
 		
 	}
 	
 	public FontHelper()
 	{
 		super(java.awt.Font.class);
 	}
 
 	public CellEditor getEditorFor(Composite parent)
 	{
 		CellEditor editor = new FontDataDialogCellEditor(parent);
 		return editor;
 	}
 
 	public Object translateToSWT(Object value)
 	{
 		// ok, convert the AWT color to SWT
 		java.awt.Font col = (java.awt.Font) value;
 		return convertFont(col);
 	}
 
 	public Object translateFromSWT(Object value)
 	{
 		// ok, convert the AWT color to SWT
 		Font font = (Font) value;
 		return convertFont(font);
 	}
 
 	private static FontRegistry _fontRegistry;
 	
 	public static java.awt.Font convertFont(org.eclipse.swt.graphics.Font swtFont)
 	{
 		// ok, convert the AWT color to SWT
 		java.awt.Font res = null;
 		FontData fd = swtFont.getFontData()[0];
 		res = new java.awt.Font(fd.getName(), fd.getStyle(), fd.getHeight());
 		return res;		
 	}
 	
 	public static org.eclipse.swt.graphics.Font convertFont(java.awt.Font javaFont)
 	{
 		
 		// check we have our registry
 		if(_fontRegistry == null)
 			_fontRegistry = new FontRegistry(Display.getCurrent(), true);
 		
 		final String fontName = javaFont.toString();
 		org.eclipse.swt.graphics.Font thisFont = _fontRegistry.get(fontName);
 		
		
 		// do we have a font for this style?
 		if(!_fontRegistry.hasValueFor(fontName))
 		{
 			// bugger, we'll have to  create it
			FontData newF = new FontData(javaFont.getFontName(), javaFont.getSize(), javaFont.getStyle());
 			_fontRegistry.put(fontName,new FontData[]{newF});
 		}
 		
 		// ok, try to receive it.  if we don't we'll just get a default one any way. cool.
 		thisFont = _fontRegistry.get(fontName);
 		
 		return thisFont;
 	}
 
 	public static org.eclipse.swt.graphics.Font getFont(FontData fd)
 	{
 		org.eclipse.swt.graphics.Font res = null;
 		
 		return res;
 	}
 	
 	public ILabelProvider getLabelFor(Object currentValue)
 	{
 		ILabelProvider label1 = new LabelProvider()
 		{
 			public String getText(Object element)
 			{
 				Font font = (Font) element;
 				FontData[] datas = font.getFontData();
 				FontData data = datas[0];
 				String res = "(" + data.getName() + ", " + data.getHeight() + ")";
 				return res;
 			}
 
 			public Image getImage(Object element)
 			{
 				Image res = null;
 				return res;
 			}
 
 		};
 		return label1;
 	}
 
 }
