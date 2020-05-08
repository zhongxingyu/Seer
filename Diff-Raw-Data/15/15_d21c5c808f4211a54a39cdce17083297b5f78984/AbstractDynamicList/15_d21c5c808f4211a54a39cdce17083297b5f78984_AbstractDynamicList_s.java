 /*
  * Copyright 2009 Andrew Pietsch 
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you 
  * may not use this file except in compliance with the License. You may 
  * obtain a copy of the License at 
  *      
  *      http://www.apache.org/licenses/LICENSE-2.0 
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
  * implied. See the License for the specific language governing permissions 
  * and limitations under the License. 
  */
 
 package com.pietschy.gwt.pectin.client.components;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.ui.*;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 /**
  *
  */
 public abstract class AbstractDynamicList<T>
 extends Composite implements HasValue<Collection<T>>
 {
    private ValueChangeHandler<T> rowComponentListener = new WidgetChangeMonitor();
 
    private VerticalPanel layout = new VerticalPanel();
    private VerticalPanel list = new VerticalPanel();
    private AddButtonListener listener = new AddButtonListener();
    private boolean allowEmpty;
 
    public AbstractDynamicList(String addLinkText)
    {
       this(addLinkText, true);
    }
 
    public AbstractDynamicList(String addLinkText, boolean allowEmpty)
    {
       layout.setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);
       layout.setSpacing(0);
       list.setHorizontalAlignment(VerticalPanel.ALIGN_LEFT);
 
       DOM.setStyleAttribute(list.getElement(), "marginBottom", "0.3em");
 
       this.allowEmpty = allowEmpty;
       if (!this.allowEmpty)
       {
          list.add(new Row(createWidget()));
       }
 
       Hyperlink add = new Hyperlink(addLinkText, "");
       add.addClickHandler(listener);
 
       layout.add(list);
       layout.add(add);
 
       initWidget(layout);
       updateUI();
    }
 
 
    private Row
    addNewRow()
    {
       Row row = createRemovableRow();
      addRows(row);
       return row;
    }
 
    private Row
    createRemovableRow()
    {
       return new Row(createWidget());
    }
 
    private Row
    createPermenantRow()
    {
       return new Row(createWidget(), false);
    }
 
    private void
   addRows(Row... rows)
   {
      addRows(Arrays.asList(rows));
   }

   private void
    addRows(List<Row> rows)
    {
       for (Row row : rows)
       {
          list.add(row);
       }
 
       updateUI();
       fireValueChange();
    }
 
    private void
   removeRows(Row... rows)
    {
       for (Row row : rows)
       {
          list.remove(row);
          row.dispose();
       }
 
       updateUI();
       fireValueChange();
    }
 
    private void
    clearRows(boolean notifyChange)
    {
       int rows = list.getWidgetCount();
       for (int i = 0; i < rows; i++)
       {
          getRow(i).dispose();
       }
 
       list.clear();
 
       if (notifyChange)
       {
          updateUI();
          fireValueChange();
       }
    }
 
    @SuppressWarnings("unchecked")
    private Row getRow(int i)
    {
       return ((Row) list.getWidget(i));
    }
    
    public int getRowCount()
    {
       return list.getWidgetCount();
    }
 
    private void fireValueChange()
    {
       fireValueChange(getValue());
    }
 
    private void
    updateUI()
    {
       list.setVisible(list.getWidgetCount() > 0);
    }
 
    protected abstract HasValue<T>
    createWidget();
 
    public Collection<T>
    getValue()
    {
       ArrayList<T> result = new ArrayList<T>();
       for (int i = 0; i < list.getWidgetCount(); i++)
       {
          @SuppressWarnings("unchecked")
          Row row = (Row) list.getWidget(i);
          result.add(row.getComponent().getValue());
       }
       
       return result;
    }
 
    public void
    setValue(Collection<T> values)
    {
       setValue(values, false);   
    }
 
    public void setValue(Collection<T> values, boolean fireEvents)
    {
       clearRows(false);
       ArrayList<Row> rows = new ArrayList<Row>();
       int i = 0;
       for (T value : values)
       {
          Row row = (!allowEmpty && i == 0) ? createPermenantRow() : createRemovableRow();
          row.getComponent().setValue(value, false);
          rows.add(row);
          i++;
       }
 
       if (rows.size() < 1 && !allowEmpty)
       {
          rows.add(createPermenantRow());
       }
 
       addRows(rows);   
       
       if (fireEvents)
       {
          fireValueChange(values);
       }
    }
 
    private void fireValueChange(Collection<T> values)
    {
       ValueChangeEvent.fire(this, values);
    }
 
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Collection<T>> handler)
    {
       return addHandler(handler, ValueChangeEvent.getType());
    }
 
    public Widget getChildWidget(int index)
    {
       return (Widget) getRow(index).getComponent();
    }
 
    public class Row extends HorizontalPanel
    implements ClickHandler
    {
       private HasValue<T> component;
       private HandlerRegistration registration;
 
       private Row(HasValue<T> component)
       {
          this(component, true);
       }
 
       private Row(HasValue<T> component, boolean enableRemove)
       {
          registration = component.addValueChangeHandler(rowComponentListener);
 
          this.component = component;
                   
          setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
 
          add((Widget) component);
 
          if (enableRemove)
          {
             HTML gap = new HTML("&nbsp;");
             Hyperlink remove = new Hyperlink("Remove", "");
             add(gap);
             add(remove);
 
             remove.addClickHandler(this);
          }
       }
 
       public void onClick(ClickEvent event)
       {
         removeRows(this);
       }
 
       public HasValue<T>
       getComponent()
       {
          return component;
       }
 
       public void 
       dispose()
       {
          registration.removeHandler();
       }
 
       public void tryAndFocus()
       {
          if (component instanceof Focusable)
          {
             ((Focusable) component).setFocus(true);
          }
       }
    }
    
 
    private class WidgetChangeMonitor
    implements ValueChangeHandler<T>
    {
       public void onValueChange(ValueChangeEvent<T> event)
       {
          fireValueChange();
       }
    }
 
    private class AddButtonListener 
    implements ClickHandler
    {
       public void onClick(ClickEvent event)
       {
          Row row = addNewRow();
          row.tryAndFocus();
       }
    }
 }
