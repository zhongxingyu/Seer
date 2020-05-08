 package uk.ac.nott.mrl.homework.client.ui;
 
 import uk.ac.nott.mrl.homework.client.DevicesClient;
 import uk.ac.nott.mrl.homework.client.DevicesService;
 import uk.ac.nott.mrl.homework.client.model.Item;
 import uk.ac.nott.mrl.homework.client.model.Model;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.BlurEvent;
 import com.google.gwt.event.dom.client.BlurHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.DoubleClickEvent;
 import com.google.gwt.event.dom.client.DoubleClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyDownEvent;
 import com.google.gwt.event.dom.client.KeyDownHandler;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.event.dom.client.MouseDownEvent;
 import com.google.gwt.event.dom.client.MouseDownHandler;
 import com.google.gwt.event.dom.client.TouchStartEvent;
 import com.google.gwt.event.dom.client.TouchStartHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextBox;
 
 public class Device extends FlowPanel
 {
 	private static final boolean mobile = Window.Navigator.getUserAgent().matches("Mozilla/5\\.0 \\(((iPad)|(iPhone)|(iPod)|(android)|(webOS)).*"); 
 	
 	private boolean init = false;
 
 	// private boolean isSignalDevice = false;
 	private Item item;
 
 	private DevicesService service;
 
 	private final Label text = new Label();
 
 	private final TextBox textBoxName = new TextBox();
 	
 	private String sortString;
 
 	private final Model model;
 
 	public Device(final Model model, final Item item)
 	{
 		this.model = model;
 		this.item = item;
 
		setStyleName(DevicesClient.resources.style().device());
 
 		text.setStyleName(DevicesClient.resources.style().deviceName());
 		text.setText(item.getName());
 		textBoxName.setMaxLength(80);
 		add(text);
 		// updateStyle(null);
 
 		add(textBoxName);
 		textBoxName.addBlurHandler(new BlurHandler()
 		{
 			@Override
 			public void onBlur(final BlurEvent event)
 			{
 				cancelEdit();
 			}
 		});
 		textBoxName.addKeyDownHandler(new KeyDownHandler()
 		{
 			@Override
 			public void onKeyDown(final KeyDownEvent event)
 			{
 				clearQuotes();
 				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
 				{
 					GWT.log("Accept");
 					acceptEdit();
 				}
 				else if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE)
 				{
 					cancelEdit();
 				}
 			}
 		});
 		textBoxName.addKeyUpHandler(new KeyUpHandler()
 		{
 			@Override
 			public void onKeyUp(final KeyUpEvent event)
 			{
 				clearQuotes();
 				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
 				{
 					GWT.log("Accept");
 					acceptEdit();
 				}
 				else if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE)
 				{
 					cancelEdit();
 				}
 			}
 		});
 		
 		textBoxName.setVisible(false);
 
 		setLeft(25);
 		update(item);
 	}
 	
 	public String getSortString()
 	{
 		if(sortString == null)
 		{
 			String mark = "d";
 			if("old".equals(item.getChange()))
 			{
 				mark = "c";
 			}
 			else if("del".equals(item.getLeaseAction()))
 			{
 				mark = "b";
 			}
 			else if(item.getState() != null || item.getIPAddress() != null)
 			{
 				mark = "a";
 			}
 			sortString = model.getZone(item) + mark + item.getName().toLowerCase();
 		}
 		return sortString;
 	}
 
 	private void clearQuotes()
 	{
 		textBoxName.setText(textBoxName.getText().replace("\"", ""));
 	}
 	
 	public void acceptEdit()
 	{
 		text.setText(textBoxName.getText());
 		textBoxName.setFocus(false);
 		service.setName(item.getMacAddress(), textBoxName.getText());
 	}
 
 	public void addClickHandler(final ClickHandler handler)
 	{
 		addDomHandler(handler, ClickEvent.getType());
 	}
 
 	public void addDoubleClickHandler(final DoubleClickHandler handler)
 	{
 		addDomHandler(handler, DoubleClickEvent.getType());
 	}
 
 	public void addMouseDownHandler(final MouseDownHandler handler)
 	{
 		addDomHandler(handler, MouseDownEvent.getType());
 	}
 
 	public void addTouchStartHandler(final TouchStartHandler handler)
 	{
 		addDomHandler(handler, TouchStartEvent.getType());
 	}
 
 	public void cancelEdit()
 	{
 		service.log("Name Edit Ended", item.getName());
 		textBoxName.setFocus(false);
 		textBoxName.setVisible(false);
 		text.setVisible(true);
 	}
 
 	public void edit(final DevicesService service)
 	{
 		this.service = service;
 		if (item.getMacAddress() == null) { return; }
 		service.log("Name Edit Started", item.getName());
 		textBoxName.setText(text.getText());
 		text.setVisible(false);
 		textBoxName.setVisible(true);
 		textBoxName.setFocus(true);
 	}
 
 	public Item getItem()
 	{
 		return item;
 	}
 
 	public void setLeft(final int left)
 	{
 		getElement().getStyle().setLeft(left, Unit.PX);
 	}
 
 	public void setSignalDevice(final boolean signal)
 	{
 		// isSignalDevice = signal;
 		// updateStyle(item);
 	}
 
 	public void setTop(final int top)
 	{
 		if (!init)
 		{
 			init = true;
 		}
 		else
 		{
 			addStyleName(DevicesClient.resources.style().deviceAnim());
 		}
 		getElement().getStyle().setTop(top, Unit.PX);
 	}
 
 	@Override
 	public String toString()
 	{
 		return text.getText();
 	}
 	
 	private void setOpacity(final double opacity)
 	{
 		if(opacity > 1)
 		{
 			setOpacity(1);
 		}
 		else
 		{
 			if(mobile)
 			{
 				// Get original colour & Calculate intermediate colour
 				// Feels more than a little hacky.
 				Colour border;
 				Colour bg;
 				Colour text = new Colour(0,0,0);
 				if(getStyleName().equals(DevicesClient.resources.style().device()) || getStyleName().equals(DevicesClient.resources.style().requestingDevice()))
 				{
 					border = new Colour(85, 119, 153);
 					bg = new Colour(119, 153, 187);	
 				}
 				else if(getStyleName().equals(DevicesClient.resources.style().deniedDevice()))
 				{
 					border = new Colour(170, 102, 102);
 					bg = new Colour(187, 153, 153);
 				}
 				else
 				{
 					border = new Colour(119, 119, 119);
 					bg = new Colour(153,153, 153);
 				}
 				
 				border.mixWithWhite(opacity);
 				bg.mixWithWhite(opacity);
 				text.mixWithWhite(opacity);
 				
 				getElement().getStyle().setBackgroundColor(bg.getHexValue());
 				getElement().getStyle().setBorderColor(border.getHexValue());
 				getElement().getStyle().setColor(text.getHexValue());
 			}
 			else
 			{
 				getElement().getStyle().setOpacity(opacity);
 			}
 		}
 	}
 
 	public void update(final Item item)
 	{
 		sortString = null;
		this.item = item;		
 		if (!text.getText().equals(item.getName()))
 		{
 			text.setText(item.getName());
 		}
 
		final ZonePanel zone = (ZonePanel) getParent();
		if (zone != null)
		{
			setStyleName(zone.getZone().getDeviceStyle(item));
		}		
		
 		if("old".equals(item.getChange()))
 		{
 			setOpacity(0.2);
 		}
 		else
 		{
 			setOpacity(item.getOpacity());
 		}
 	}
 
 	public String getStateSource()
 	{
 		return item.getStateSource();
 	}
 }
