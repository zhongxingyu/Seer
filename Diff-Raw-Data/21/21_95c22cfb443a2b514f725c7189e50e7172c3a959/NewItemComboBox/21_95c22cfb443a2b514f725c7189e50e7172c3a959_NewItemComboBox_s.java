 package com.scurab.web.drifmaps.client.controls;
 
 import java.util.HashSet;
 import java.util.List;
 
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.OptionElement;
 import com.google.gwt.dom.client.SelectElement;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.i18n.client.HasDirection.Direction;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.HasValue;
 import com.google.gwt.user.client.ui.ListBox;
 import com.scurab.web.drifmaps.client.DataService;
 import com.scurab.web.drifmaps.client.DrifMaps;
 import com.scurab.web.drifmaps.client.dialog.InputDialog;
 import com.scurab.web.drifmaps.client.dialog.NotificationDialog;
 import com.scurab.web.drifmaps.shared.exception.NotUniqueValueException;
 import com.scurab.web.drifmaps.shared.interfaces.MapItemTypeService;
 
 /**
  * Simple combobox with initialized with 3 values.
  * This implementation allows only distinc items!
  * First is null, has {@value #NULL_VALUE}
  * Secons is just separator between values and item for ask, has {@value #SEPARATOR_VALUE}
  * Last item is for asking {@value #ASK_FOR_NEW_ITEM_VALUE}
  * If dataservice is not null, data are sended to server by addMapItemType
  * 
  * To check if item was really added to list, check {@link #getLastInsertException()}, 
  * if return null item was added, otherwise there was some trouble. Now can be only {@link #NotUniqueValueException}
  * Next succesful adding erase this exception
  * @author Joe Scurab
  *
  */
 public class NewItemComboBox extends ListBox implements HasValue<String>
 {
 	public static final String SEPARATOR_VALUE = "--SEPARATOR_VALUE--";
 	public static final String ASK_FOR_NEW_ITEM_VALUE = "--ASK_FOR_NEW_ITEM_VALUE--";
 	public static final String NULL_VALUE = "--NULL--";
 	private OptionElement mSeparatorOption = null;
 	private HashSet<String> mValues = null;
 	private Exception mLastException = null;
 	private int mLongestText = 0;
 
 	public NewItemComboBox()
 	{		
 		mValues = new HashSet<String>();
 		//first null item
 		super.insertItem(" ", Direction.LTR,NULL_VALUE,0);
 		//second separator
 		super.insertItem("---", Direction.LTR,SEPARATOR_VALUE,1); //		
 		mSeparatorOption = getSelectElement().getOptions().getItem(1);
 		String addNewItemWord = DrifMaps.Words.AddNewItem();
 		handleChangeSeparator(addNewItemWord.length());
 		//last one is item for ask
 		super.insertItem(addNewItemWord, Direction.LTR, ASK_FOR_NEW_ITEM_VALUE,2);
 
 		addChangeHandler(new ChangeHandler()
 		{
 			@Override
 			public void onChange(ChangeEvent event)
 			{
 				if(getSelectedIndex() == getItemCount() - 2)//separator
 					setSelectedIndex(0);
 				ValueChangeEvent.fire(NewItemComboBox.this, getValue());
 				if (getSelectedIndex() == getItemCount() - 1)//addnewitemvalue
					onAddNewItemSelection();
 			}
 		});
 	}
 
 	private MapItemTypeService mDataService;
 
 	public void setDataService(MapItemTypeService ds)
 	{
 		mDataService = ds;
 	}
 	
 	public void loadItemsFromServer() throws Exception
 	{
 		if(mDataService == null)
 			throw new Exception("DataService not set!");
 		mDataService.getMapItemTypes(new AsyncCallback<List<String>>()
 		{
 			@Override
 			public void onSuccess(List<String> result)
 			{
 				for(String s : result)
 					addItem(s);
 			}
 			
 			@Override
 			public void onFailure(Throwable caught){/*don't bother, next time can u make it*/}
 		});
 	}
 
 	private final static int OFFSET = 3;
 
	public void onAddNewItemSelection()
 	{
 		InputDialog.show(DrifMaps.Words.AddNewItem(), new InputDialog.OnInputDialogButtonClick()
 		{
 			@Override
 			public void onOkClick(final String value)
 			{
 				handleOkClick(value);
 			}
 
 			@Override 
 			public void onCancelClick()
 			{
 				setSelectedIndex(0);
 			}
 		});
 	}
 	
 	private void handleOkClick(final String value)
 	{
 		if(value == null || value.length() == 0)
 		{
 			NotificationDialog.show("Invalid value!");
 			return;
 		}
 		if(mDataService == null)
 		{
 			onAddNewItem(value);
 			return;
 		}
 		
 		mDataService.processString(value, DataService.TYPE_MAPITEM_TYPE, DataService.ADD, new AsyncCallback<Void>()
 		{
 			@Override
 			public void onSuccess(Void result)
 			{
 				onAddNewItem(value);
 			}
 			
 			@Override
 			public void onFailure(Throwable caught)
 			{
 				NotificationDialog.show(caught);
 			}
 		});
 	}
 
 	public void onAddNewItem(String item)
 	{
 		//add new item and select it
 		addItem(item);
 		setSelectedIndex(getItemCount()-OFFSET);//was added after null and before 2 last items, +1 first null value and -1 for index from 0 => 1-1 :)
 	}
 	
 	@Override
 	public void removeItem(int index)
 	{
 		String value = getValue(index); //can throw bad index exception
 		mValues.remove(value);
 		super.removeItem(index);		
 	}
 
 	@Override
 	public void insertItem(String item, Direction dir, String value, int index) 
 	{
 		if(mValues.contains(value))
 		{
 			mLastException = new NotUniqueValueException(item + ", " + value);
 			return;
 		}
 		else
 		{
 			mValues.add(value);
 			mLastException = null;
 		}
 		if(item.length() > mLongestText)
 		{
 			handleChangeSeparator(item.length());
 		}
 			
 		SelectElement select = getSelectElement();
 		OptionElement option = Document.get().createOptionElement();
 		setOptionText(option, item, dir);
 		option.setValue(value);
 
 		int itemCount = select.getLength()-OFFSET;
 		if (index < 0 || index > (itemCount))
 			index = itemCount;
 
 		if (index == itemCount)
 		{
 			select.add(option, mSeparatorOption);
 		} 
 		else
 		{
 			OptionElement before = select.getOptions().getItem(index);
 			select.add(option, before);
 		}
 	}
 	
 	private void handleChangeSeparator(int len)
 	{
 		mLongestText = len;
 		String x = "---------------------------------------------------------------------------------------";
 		if(len <= x.length())
 			mSeparatorOption.setText(x.substring(0, len));
 		else
 			mSeparatorOption.setText(x);
 	}
 
 	private SelectElement getSelectElement()
 	{
 		return getElement().cast();
 	}
 
 	@Override
 	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler)
 	{				
 		return addHandler(handler,ValueChangeEvent.getType());
 	}
 
 	@Override
 	public String getValue()
 	{
 		return getItemText(getSelectedIndex());
 	}
 
 	@Override
 	public void setValue(String value)
 	{
 		for (int i = 0; i < getItemCount(); i++)
 		{
 			if (getItemText(i).equals(value))
 			{
 				setSelectedIndex(i);
 				break;
 			}
 		}
	}

	@Override
	public void setValue(String value, boolean fireEvents)
	{
		setValue(value);
 		if(fireEvents)
 			ValueChangeEvent.fire(this, value);
 	}
 	
 	/**
 	 * Returns last adding exception
 	 * 
 	 * @return null or {@link #NotUniqueValueException}
 	 */
 	public Exception getLastInsertException()
 	{
 		return mLastException;
 	}
 	
 	@Override
 	public void setSelectedIndex(int index)
 	{
 		if(index == getItemCount()-2)
 			index = 0;
 		super.setSelectedIndex(index);
 	}
 }
