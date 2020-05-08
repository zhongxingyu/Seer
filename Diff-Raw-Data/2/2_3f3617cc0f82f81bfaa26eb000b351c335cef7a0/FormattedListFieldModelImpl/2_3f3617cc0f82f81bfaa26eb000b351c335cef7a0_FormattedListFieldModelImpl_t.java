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
 
 package com.pietschy.gwt.pectin.client;
 
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.pietschy.gwt.pectin.client.format.Format;
 import com.pietschy.gwt.pectin.client.format.FormatException;
 import com.pietschy.gwt.pectin.client.list.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: andrew
  * Date: Aug 5, 2009
  * Time: 1:02:17 PM
  * To change this template use File | Settings | File Templates.
  */
 public class FormattedListFieldModelImpl<T>
   extends AbstractListFieldModelBase<T>
    implements FormattedListFieldModel<T>
 {
    private Format<T> format;
    private ArrayListModel<String> textModel = new ArrayListModel<String>();
    private ListFormatExceptionPolicy<T> formatExceptionPolicy = new ListFormatExceptionPolicy<T>()
    {
       public void onFormatException(List<T> values, FormatException e)
       {
       }
    };
 
    private GuardedListModelChangedHandler<T> valueMonitor = new GuardedListModelChangedHandler<T>()
    {
       public void onGuardedListDataChanged(ListModelChangedEvent<T> event)
       {
          writeSourceToText();
       }
    };
 
    private GuardedListModelChangedHandler<String> textMonitor = new GuardedListModelChangedHandler<String>()
    {
       public void onGuardedListDataChanged(ListModelChangedEvent<String> event)
       {
          writeTextToSource();
       }
    };
 
    protected ValueChangeHandler<Format<T>> formatMonitor = new ValueChangeHandler<Format<T>>()
    {
       public void onValueChange(ValueChangeEvent<Format<T>> event)
       {
          writeSourceToText();
       }
    };
 
 
    public FormattedListFieldModelImpl(FormModel formModel, ListModel<T> source, Format<T> format, Class<T> valueType)
    {
       super(formModel, source, valueType);
       setFormat(format);
 
       addListModelChangedHandler(valueMonitor);
       textModel.addListModelChangedHandler(textMonitor);
    }
 
    public MutableListModel<String> getTextModel()
    {
       return textModel;
    }
 
    public void setFormat(Format<T> format)
    {
       if (format == null)
       {
          throw new NullPointerException("format is null");
       }
 
       this.format = format;
 
       // this will trigger the text model to update using
       // the new format.
       writeSourceToText();
    }
 
    public Format<T> getFormat()
    {
       return format;
    }
 
    public void setFormatExceptionPolicy(ListFormatExceptionPolicy<T> formatExceptionPolicy)
    {
       this.formatExceptionPolicy = formatExceptionPolicy;
    }
 
    protected void writeSourceToText()
    {
       try
       {
          textMonitor.setIgnoreEvents(true);
          ArrayList<String> newValues = new ArrayList<String>(size());
          for (T value : this)
          {
             newValues.add(getFormat().format(value));
          }
 
          textModel.setElements(newValues);
       }
       finally
       {
          textMonitor.setIgnoreEvents(false);
       }
    }
 
    protected void writeTextToSource()
    {
       try
       {
          valueMonitor.setIgnoreEvents(true);
 
          ArrayList<T> newValues = new ArrayList<T>(textModel.size());
 
          for (String value : textModel)
          {
             try
             {
                newValues.add(getFormat().parse(value));
             }
             catch (FormatException e)
             {
                formatExceptionPolicy.onFormatException(newValues, e);
             }
          }
 
          setElements(newValues);
 
       }
       finally
       {
          valueMonitor.setIgnoreEvents(false);
       }
    }
 
 
 }
