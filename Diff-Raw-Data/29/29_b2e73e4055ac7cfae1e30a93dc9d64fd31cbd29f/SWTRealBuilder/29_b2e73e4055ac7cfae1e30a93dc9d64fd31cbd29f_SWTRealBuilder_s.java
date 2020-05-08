 package jfg.gui.swt;
 
 import static jfg.gui.swt.TypeUtils.*;
 
 import java.text.DecimalFormatSymbols;
 import java.text.NumberFormat;
 
 import jfg.Attribute;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 
 public class SWTRealBuilder extends SWTTextBuilder
 {
 	@Override
 	public boolean accept(Attribute attrib)
 	{
 		Object type = attrib.getType();
 		return typeIsReal(type) || "real".equals(type);
 	}
 	
 	@Override
 	protected void addValidation(final Text text, final Object type)
 	{
 		text.addListener(SWT.Verify, new Listener() {
 			public void handleEvent(Event e)
 			{
 				if (e.character != 0 && Character.isISOControl(e.character))
 					return;
 				
 				String append = e.text;
 				String txt = text.getText();
 				txt = txt.substring(0, e.start) + append + txt.substring(e.end);
 				
 				if (!isValidNumber(txt))
 				{
 					e.doit = false;
 					return;
 				}
 				
 				if ("-".equals(txt))
 					return;
 				
 				try
 				{
 					parseDouble(txt, type);
 				}
 				catch (NumberFormatException ex)
 				{
 					e.doit = false;
 					return;
 				}
 			}
 		});
 	}
 	
 	@Override
 	protected String convertToString(Text text, Object value, Object type)
 	{
 		if (value == null)
 			return "";
 		
 		String oldText = text.getText();
 		double o = parseDouble(oldText, type);
 		double v = asDouble(value);
 		
 		if (o == v)
 			return oldText;
 		else
 		{
 			NumberFormat formater = NumberFormat.getNumberInstance();
 			formater.setGroupingUsed(false);
 			return formater.format(v);
 		}
 	}
 	
 	@Override
 	protected Object convertToObject(String value, Object type, boolean canBeNull)
 	{
		if ("-".equals(value))
			value = null;
		return valueOf(value, type, canBeNull ? null : "0");
 	}
 	
 	static boolean isValidNumber(String append)
 	{
 		char sep = new DecimalFormatSymbols().getDecimalSeparator();
 		boolean hasSeparator = false;
 		for (int i = 0; i < append.length(); i++)
 		{
 			char c = append.charAt(i);
 			
 			if (i == 0 && c == '-')
 				continue;
 			
 			if (c == sep && !hasSeparator)
 			{
 				hasSeparator = true;
 				continue;
 			}
 			
 			if ('0' <= c && c <= '9')
 				continue;
 			
 			return false;
 		}
 		return true;
 	}
 	
 	@Override
 	protected Object getType(Object type)
 	{
 		if ("real".equals(type))
 			return float.class;
 		return type;
 	}
 }
