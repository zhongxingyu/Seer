 package monbulk.shared.Form;
 
 import java.util.HashMap;
 
 import monbulk.shared.util.GWTLogger;
 
 import com.google.gwt.event.dom.client.MouseDownEvent;
 import com.google.gwt.event.dom.client.MouseDownHandler;
 import com.google.gwt.event.dom.client.MouseOverEvent;
 import com.google.gwt.event.dom.client.MouseOverHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.FocusWidget;
 import com.google.gwt.user.client.ui.HasValue;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 public class FormWidget extends HorizontalPanel{
 
 	//FieldName
 	private String WidgetName;
 	
 	
 	private HashMap<String, Widget> WidgetList;
 	
 	public final String VALID_WIDGET = "validWidget";
 	public final String FORM_WIDGET = "formWidget";
 	public final String LABEL_WIDGET = "labelWidget";
 	public FormWidget(String WidgetName)
 	{
 		super();
 		WidgetList = new HashMap<String,Widget>();
 		this.WidgetName = WidgetName;
 	}
 	public void setFormWidget(Widget w)
 	{
 		WidgetList.put(FORM_WIDGET, w);
 		
 		
 		w.setStyleName(FORM_WIDGET);
 		
 		this.add(WidgetList.get(FORM_WIDGET));
 		
 	}
 	public void setValidWidget(Widget w)
 	{
 		WidgetList.put(VALID_WIDGET, w);
 		w.setStyleName(VALID_WIDGET);
 		this.add(WidgetList.get(VALID_WIDGET));
 	}
 	public void setLabelWidget(Widget w)
 	{
 		WidgetList.put(LABEL_WIDGET, w);
 		w.setStyleName(LABEL_WIDGET);
 		this.add(WidgetList.get(LABEL_WIDGET));
 	}
 	public void enable()
 	{
 		this.setVisible(true);
 	}
 	public Object getValue()
 	{
 		HasValue<Object> item = (HasValue<Object>)WidgetList.get(FORM_WIDGET);
 		return item.getValue();
 	}
 	public void clear()
 	{
 		try
 		{
 			HasValue<Object> item = (HasValue<Object>)WidgetList.get(FORM_WIDGET);
 			item.setValue(null);
 		}
 		catch(Exception ex)
 		{
 			GWTLogger.Log("Have you been trying to cast incorrectly?", "FormWidget", "clear", "76");
 		}
 		
 	}
 	public void disable()
 	{
 		this.setVisible(false);
 	}
 	public String getWidgetName()
 	{
 		return this.WidgetName;
 	}
 	public Widget getFormWidget()
 	{
 		
 		return this.WidgetList.get(FORM_WIDGET);
 	}	
 	public void setFormValue(Object Value)
 	{
 		HasValue<Object> item = (HasValue<Object>)WidgetList.get(FORM_WIDGET);
 		item.setValue(Value);
 	}
	public void setListValue(String Value)
 	{
 		ListBox item = (ListBox)WidgetList.get(FORM_WIDGET);
		int i= 0; 
		while(i<item.getItemCount())
		{
			//item.setSelectedIndex( Value);
			String value = item.getValue(i);
			if(value.contains(Value)&&value.length()==Value.length())
			{
				item.setSelectedIndex(i);
				return;
			}
			i++;
		}
		
 	}
 }
