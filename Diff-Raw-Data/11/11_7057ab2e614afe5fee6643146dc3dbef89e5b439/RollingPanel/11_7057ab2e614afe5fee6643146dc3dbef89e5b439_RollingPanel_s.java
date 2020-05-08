 /*
  * Copyright 2011 cruxframework.org.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.cruxframework.crux.widgets.client.rollingpanel;
 
 import org.cruxframework.crux.core.client.screen.Screen;
 import org.cruxframework.crux.core.client.utils.StyleUtils;
 
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.core.client.Scheduler.RepeatingCommand;
 import com.google.gwt.core.client.Scheduler.ScheduledCommand;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.event.dom.client.MouseDownEvent;
 import com.google.gwt.event.dom.client.MouseDownHandler;
 import com.google.gwt.event.dom.client.MouseUpEvent;
 import com.google.gwt.event.dom.client.MouseUpHandler;
 import com.google.gwt.event.logical.shared.ResizeEvent;
 import com.google.gwt.event.logical.shared.ResizeHandler;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CellPanel;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.InsertPanel;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author Thiago da Rosa de Bustamante -
  *
  */
 @SuppressWarnings("deprecation")
 public class RollingPanel extends Composite implements InsertPanel, HasHorizontalAlignment, HasVerticalAlignment
 {
 	public static final String DEFAULT_NEXT_HORIZONTAL_STYLE_NAME = "crux-RollingPanelHNext";
 	public static final String DEFAULT_NEXT_VERTICAL_STYLE_NAME = "crux-RollingPanelVNext";
 
 	public static final String DEFAULT_PREVIOUS_HORIZONTAL_STYLE_NAME = "crux-RollingPanelHPrevious";
 	public static final String DEFAULT_PREVIOUS_VERTICAL_STYLE_NAME = "crux-RollingPanelVPrevious";
 	
 	public static final String DEFAULT_BODY_VERTICAL_STYLE_NAME = "crux-RollingPanelVBody";
 	public static final String DEFAULT_BODY_HORIZONTAL_STYLE_NAME = "crux-RollingPanelHBody";
 	
 	public static final String DEFAULT_STYLE_NAME = "crux-RollingPanel";
 
 	private String nextButtonStyleName;
 	private String previousButtonStyleName;
 	private String bodyStyleName;
 
 	protected CellPanel itemsPanel;
     protected DockPanel layoutPanel;
 	
 	private Button horizontalNextButton = null;
 	private Button horizontalPreviousButton = null;
 	private Button verticalNextButton = null;
 	private Button verticalPreviousButton = null;
 	
 	private SimplePanel itemsScrollPanel;
 	private boolean scrollToAddedWidgets = false;
 	private boolean vertical;
 
 
 	private HorizontalAlignmentConstant horizontalAlign;
 
 	private VerticalAlignmentConstant verticalAlign;
 	
 	/**
 	 * @param vertical
 	 */
 	public RollingPanel(boolean vertical)
 	{
 		this.vertical = vertical;
 		
 		this.layoutPanel = new DockPanel();
 		this.itemsScrollPanel = new SimplePanel();
 		
 		DOM.setStyleAttribute(this.itemsScrollPanel.getElement(), "overflowX", "hidden");
 		DOM.setStyleAttribute(this.itemsScrollPanel.getElement(), "overflowY", "hidden");
 		
 		if (vertical)
 		{
 			this.layoutPanel.setHeight("100%");
 			this.itemsScrollPanel.setHeight("100%");
 			this.itemsScrollPanel.setStyleName(DEFAULT_BODY_VERTICAL_STYLE_NAME);
 			this.itemsPanel = new VerticalPanel();
 			createVerticalNavigationButtons();
 		}
 		else
 		{	
 			this.layoutPanel.setWidth("100%");
 			this.itemsScrollPanel.setWidth("100%");
 			this.itemsScrollPanel.setStyleName(DEFAULT_BODY_HORIZONTAL_STYLE_NAME);
 			this.itemsPanel = new HorizontalPanel();
 			createHorizontalNavigationButtons();
 		}
 		
 		this.itemsScrollPanel.add(this.itemsPanel);
 		
 		this.layoutPanel.add(this.itemsScrollPanel, DockPanel.CENTER);
 		this.layoutPanel.getElement().getStyle().setProperty("tableLayout", "fixed");
 	
 		if (vertical)
 		{
 			this.layoutPanel.setCellHeight(this.itemsScrollPanel, "100%");
 		}
 		initWidget(layoutPanel);
 		setSpacing(5);
 		setStyleName(DEFAULT_STYLE_NAME);
 		
 		Screen.addResizeHandler(new ResizeHandler()
 		{
 			public void onResize(ResizeEvent event)
 			{
 				checkNavigationButtons();
 			}
 		});
 
 		maybeShowNavigationButtons();
 	}
 	
 	/**
 	 * @param child
 	 */
 	public void add(final Widget child)
 	{
 		this.itemsPanel.add(child);
 		maybeShowNavigationButtons();
 		if (scrollToAddedWidgets)
 		{
 			Scheduler.get().scheduleDeferred(new ScheduledCommand()
 			{
 				public void execute()
 				{
 					scrollToWidget(child);
 				}
 			});
 		}
 	}
 
 	/**
 	 * 
 	 */
 	public void clear()
 	{
 		this.itemsPanel.clear();
 		maybeShowNavigationButtons();
 	}
 
 	/**
 	 * @return
 	 */
 	public int getHorizontalScrollPosition()
 	{
 		return itemsScrollPanel.getElement().getScrollLeft();
 	}
 
 	/**
 	 * @return
 	 */
 	public int getSpacing()
 	{
 		return itemsPanel.getSpacing();
 	}
 
 	/**
 	 * @return
 	 */
 	public int getVerticalScrollPosition()
 	{
 		return DOM.getElementPropertyInt(itemsScrollPanel.getElement(), "scrollTop");
 	}
 
 	/**
 	 * @param i
 	 * @return
 	 */
 	public Widget getWidget(int i)
     {
 	    return itemsPanel.getWidget(i);
     }
 
 	/**
 	 * @return
 	 */
 	public int getWidgetCount()
     {
 	    return itemsPanel.getWidgetCount();
     }
 
 	/**
 	 * @see com.google.gwt.user.client.ui.IndexedPanel#getWidgetIndex(com.google.gwt.user.client.ui.Widget)
 	 */
 	public int getWidgetIndex(Widget child)
     {
 	    return ((InsertPanel)itemsPanel).getWidgetIndex(child);
     }
 
 	/**
 	 * @param widget
 	 * @param i
 	 */
 	public void insert(final Widget widget, int i)
     {
 	    ((InsertPanel)itemsPanel).insert(widget, i);
 		maybeShowNavigationButtons();
 		if (scrollToAddedWidgets)
 		{
 			Scheduler.get().scheduleDeferred(new ScheduledCommand()
 			{
 				public void execute()
 				{
 					scrollToWidget(widget);
 				}
 			});
 		}
     }
 	
 	/**
 	 * @return
 	 */
 	public boolean isScrollToAddedWidgets()
     {
     	return scrollToAddedWidgets;
     }
 	
 	/**
 	 * @return
 	 */
 	public boolean isVertical()
 	{
 		return vertical;
 	}
 
 	/**
 	 * @see com.google.gwt.user.client.ui.IndexedPanel#remove(int)
 	 */
 	public boolean remove(int index)
     {
 	    boolean ret = ((InsertPanel)itemsPanel).remove(index);
 		maybeShowNavigationButtons();
 		return ret;
     }
 	
 	/**
 	 * @param toRemove
 	 */
 	public void remove(Widget toRemove)
     {
 		itemsPanel.remove(toRemove);
 		maybeShowNavigationButtons();
     }
 	
 	/**
 	 * @param widget
 	 */
 	public void scrollToWidget(Widget widget)
 	{
 		if (widget != null)
 		{
 			if (isVertical())
 			{
 				verticalScrollToWidget(itemsScrollPanel.getElement(), widget.getElement());
 			}
 			else
 			{
 				horizontalScrollToWidget(itemsScrollPanel.getElement(), widget.getElement());
 			}
 		}
 	}
 	
 	/**
 	 * @param child
 	 * @param cellHeight
 	 */
 	public void setCellHeight(Widget child, String cellHeight)
     {
 		this.itemsPanel.setCellHeight(child, cellHeight);
     }
 	
 	/**
 	 * @param w
 	 * @param align
 	 */
 	public void setCellHorizontalAlignment(Widget w, HorizontalAlignmentConstant align)
 	{
 		this.itemsPanel.setCellHorizontalAlignment(w, align);
 	}	
 	
 	/**
 	 * @param verticalAlign
 	 */
 	public void setCellVerticalAlignment(Widget w, VerticalAlignmentConstant verticalAlign)
     {
 		this.itemsPanel.setCellVerticalAlignment(w, verticalAlign);
     }	
 
 	/**
 	 * @param child
 	 * @param cellWidth
 	 */
 	public void setCellWidth(Widget child, String cellWidth)
     {
 		this.itemsPanel.setCellWidth(child, cellWidth);
     }
 	
 	/**
 	 * @param align
 	 */
 	public void setHorizontalAlignment(HorizontalAlignmentConstant align)
     {
 		this.horizontalAlign = align;
 		this.layoutPanel.setCellHorizontalAlignment(this.itemsScrollPanel, align);
     }
 	
 	/**
 	 * @param position
 	 */
 	public void setHorizontalScrollPosition(int position)
 	{
 		if (position <0)
 		{
 			position = 0;
 		}
		else if (position > itemsScrollPanel.getOffsetWidth())
 		{
			position = itemsScrollPanel.getOffsetWidth();
 		}
 		itemsScrollPanel.getElement().setScrollLeft(position);
 	}
 
 	/**
 	 * @param scrollToAddedWidgets
 	 */
 	public void setScrollToAddedWidgets(boolean scrollToAddedWidgets)
     {
     	this.scrollToAddedWidgets = scrollToAddedWidgets;
     }
 	
 	/**
 	 * @param spacing
 	 */
 	public void setSpacing(int spacing)
 	{
 		itemsPanel.setSpacing(spacing);
 	}
 	
 	/**
 	 * @param verticalAlign
 	 */
 	public void setVerticalAlignment(VerticalAlignmentConstant verticalAlign)
     {
 		this.verticalAlign = verticalAlign;
 		this.layoutPanel.setCellVerticalAlignment(this.itemsScrollPanel, verticalAlign);
     }
 
 	/**
 	 * @param position
 	 */
 	public void setVerticalScrollPosition(int position)
 	{
 		if (position <0)
 		{
 			position = 0;
 		}
		else if (position > itemsScrollPanel.getOffsetHeight())
 		{
			position = itemsScrollPanel.getOffsetHeight();
 		}
 	    DOM.setElementPropertyInt(itemsScrollPanel.getElement(), "scrollTop", position);
 	}
 	
 	/**
 	 * 
 	 */
 	protected void createHorizontalNavigationButtons()
 	{
 		horizontalPreviousButton = new Button(" ");
 		horizontalPreviousButton.setStyleName(DEFAULT_PREVIOUS_HORIZONTAL_STYLE_NAME);
 		HorizontalNavButtonEvtHandler handler = new HorizontalNavButtonEvtHandler(-20, -5);
 		horizontalPreviousButton.addMouseDownHandler(handler);
 		horizontalPreviousButton.addMouseUpHandler(handler);
 
 		this.layoutPanel.add(horizontalPreviousButton, DockPanel.WEST);
 		
 		horizontalNextButton = new Button(" ");
 		horizontalNextButton.setStyleName(DEFAULT_NEXT_HORIZONTAL_STYLE_NAME);
 		handler = new HorizontalNavButtonEvtHandler(20, 5);
 		horizontalNextButton.addMouseDownHandler(handler);
 		horizontalNextButton.addMouseUpHandler(handler);
 
 		this.layoutPanel.add(horizontalNextButton, DockPanel.EAST);
 
 		Scheduler.get().scheduleDeferred(new ScheduledCommand()
 		{
 			public void execute()
 			{
 				getWrapperElement(horizontalPreviousButton).setClassName(DEFAULT_PREVIOUS_HORIZONTAL_STYLE_NAME + "Wrapper");
 				getWrapperElement(horizontalNextButton).setClassName(DEFAULT_NEXT_HORIZONTAL_STYLE_NAME + "Wrapper");
 			}
 		});
 	}
 
 	/**
 	 * @return
 	 */
 	private Element getWrapperElement(Button button)
 	{
 		return button.getElement().getParentElement();
 	}
 	
 	/**
 	 * 
 	 */
 	protected void createVerticalNavigationButtons()
 	{
 		verticalPreviousButton = new Button(" ");
 		verticalPreviousButton.setStyleName(DEFAULT_PREVIOUS_VERTICAL_STYLE_NAME);
 		VerticalNavButtonEvtHandler handler = new VerticalNavButtonEvtHandler(-20, -5);
 		verticalPreviousButton.addMouseDownHandler(handler);
 		verticalPreviousButton.addMouseUpHandler(handler);
 		this.layoutPanel.add(verticalPreviousButton, DockPanel.NORTH);
 
 		verticalNextButton = new Button(" ");
 		handler = new VerticalNavButtonEvtHandler(20, 5);
 		verticalNextButton.addMouseDownHandler(handler);
 		verticalNextButton.addMouseUpHandler(handler);
 		verticalNextButton.setStyleName(DEFAULT_NEXT_VERTICAL_STYLE_NAME);
 		this.layoutPanel.add(verticalNextButton, DockPanel.SOUTH);
 
 		Scheduler.get().scheduleDeferred(new ScheduledCommand()
 		{
 			public void execute()
 			{
 				getWrapperElement(verticalPreviousButton).setClassName(DEFAULT_PREVIOUS_VERTICAL_STYLE_NAME + "Wrapper");
 				getWrapperElement(verticalNextButton).setClassName(DEFAULT_NEXT_VERTICAL_STYLE_NAME + "Wrapper");
 			}
 		});
     }
 	
 	/**
 	 * 
 	 */
 	protected void checkNavigationButtons()
 	{
 		if (isVertical())
 		{
 			if (itemsPanel.getOffsetHeight() > layoutPanel.getOffsetHeight())
 			{
 				enableNavigationButtons();
 			}
 			else
 			{
 				disableNavigationButtons();
 				setVerticalScrollPosition(0);
 			}
 		}
 		else
 		{
 			if (itemsPanel.getOffsetWidth() > layoutPanel.getOffsetWidth())
 			{
 				enableNavigationButtons();
 			}
 			else
 			{
 				disableNavigationButtons();
 				setHorizontalScrollPosition(0);
 			}
 		}
 	}
 
 	/**
 	 * 
 	 */
 	protected void maybeShowNavigationButtons()
     {
 		Scheduler.get().scheduleDeferred(new ScheduledCommand()
 		{
 			public void execute()
 			{
 				checkNavigationButtons();
 			}
 		});
     }
 
 	/**
 	 * 
 	 */
 	protected void disableNavigationButtons()
 	{
 		if (isVertical())
 		{
 	    	StyleUtils.addStyleDependentName(getWrapperElement(verticalPreviousButton), "disabled");
 	    	StyleUtils.addStyleDependentName(getWrapperElement(verticalNextButton), "disabled");
 		}
 		else
 		{
 	    	StyleUtils.addStyleDependentName(getWrapperElement(horizontalPreviousButton), "disabled");
 	    	StyleUtils.addStyleDependentName(getWrapperElement(horizontalNextButton), "disabled");
 		}
 	}
 	
 	/**
 	 * 
 	 */
 	protected void enableNavigationButtons()
 	{
 		if (isVertical())
 		{
 			StyleUtils.removeStyleDependentName(getWrapperElement(verticalPreviousButton), "disabled");
 			StyleUtils.removeStyleDependentName(getWrapperElement(verticalNextButton), "disabled");
 		}
 		else
 		{
 			StyleUtils.removeStyleDependentName(getWrapperElement(horizontalPreviousButton), "disabled");
 			StyleUtils.removeStyleDependentName(getWrapperElement(horizontalNextButton), "disabled");
 		}
 	}
 	
 	/**
 	 * @param scroll
 	 * @param item
 	 */
 	private void horizontalScrollToWidget(com.google.gwt.dom.client.Element scroll, com.google.gwt.dom.client.Element item)
     {
 		if (itemsPanel.getOffsetWidth() > layoutPanel.getOffsetWidth())
 		{		
 			int realOffset = 0;
 			int itemOffsetWidth = item.getOffsetWidth();
 			while (item != null && item != scroll)
 			{
 				realOffset += item.getOffsetLeft();
 				item = item.getParentElement();
 			}
 			int scrollLeft = getHorizontalScrollPosition();
 			int scrollOffsetWidth = scroll.getOffsetWidth();
 			int right = realOffset + itemOffsetWidth;
 			int visibleWidth = scrollLeft + scrollOffsetWidth;
 			
 			if (realOffset < scrollLeft)	
 			{
 				setHorizontalScrollPosition(realOffset);
 			}
 			else if (right > visibleWidth)
 			{
 				setHorizontalScrollPosition(scrollLeft + right - visibleWidth);
 			}
 		}
     }
 
 	/**
 	 * @param scroll
 	 * @param item
 	 */
 	private void verticalScrollToWidget(com.google.gwt.dom.client.Element scroll, com.google.gwt.dom.client.Element item)
     {
 		if (itemsPanel.getOffsetHeight() > layoutPanel.getOffsetHeight())
 		{
 			int realOffset = 0;
 			int itemOffsetHeight = item.getOffsetHeight();
 			while (item != null && item != scroll)
 			{
 				realOffset += item.getOffsetTop();
 				item = item.getParentElement();
 			}
 			int scrollTop = scroll.getScrollTop();
 			int scrollOffsetHeight = scroll.getOffsetHeight();
 			int bottom = realOffset + itemOffsetHeight;
 			int visibleHeight = scrollTop + scrollOffsetHeight;
 			
 			if (realOffset < scrollTop)	
 			{
 				scroll.setScrollTop(realOffset);
 			}
 			else if (bottom > visibleHeight)
 			{
 				scroll.setScrollTop(scrollTop + bottom - visibleHeight);
 			}
 		}
     }
 	
 	/**
 	 * @author Thiago da Rosa de Bustamante -
 	 *
 	 */
 	class HorizontalNavButtonEvtHandler implements MouseDownHandler, MouseUpHandler
 	{
 		private int adjust;
 		private boolean buttonPressed = false;
 		private int delta;
 		private int incrementalAdjust;
 		private int originalIncrementalAdjust;
 
 		HorizontalNavButtonEvtHandler(int adjust, int incrementalAdjust)
 		{
 			this.adjust = adjust;
 			this.incrementalAdjust = incrementalAdjust;
 			this.originalIncrementalAdjust = incrementalAdjust;
 			this.delta = incrementalAdjust / 4;
 		}
 		
 		public void onMouseDown(MouseDownEvent event)
 		{
 			buttonPressed = true;
 			adjustScrollPosition(adjust);
 			Scheduler.get().scheduleFixedDelay(new RepeatingCommand()
 			{
 				public boolean execute()
 				{
 					if (buttonPressed)
 					{
 						adjustScrollPosition(incrementalAdjust+=delta);
 					}
 					return buttonPressed;
 				}
 			}, 50);
 		}
 
 		public void onMouseUp(MouseUpEvent event)
 		{
 			buttonPressed = false;
 			incrementalAdjust = originalIncrementalAdjust;
 		}
 		
 		/**
 		 * @param adjust
 		 */
 		protected void adjustScrollPosition(int adjust)
 	    {
 			int position = getHorizontalScrollPosition() + adjust;
 			setHorizontalScrollPosition(position);
 	    }
 	}
 
 	/**
 	 * @author Thiago da Rosa de Bustamante -
 	 *
 	 */
 	class VerticalNavButtonEvtHandler extends HorizontalNavButtonEvtHandler
 	{
 
 		VerticalNavButtonEvtHandler(int adjust, int incrementalAdjust)
         {
 	        super(adjust, incrementalAdjust);
         }
 
 		@Override
 		protected void adjustScrollPosition(int adjust)
 	    {
 	        int position = getVerticalScrollPosition() + adjust;
 			setVerticalScrollPosition(position);
 	    }
 	}
 
 	public VerticalAlignmentConstant getVerticalAlignment()
     {
 	    return this.verticalAlign;
     }
 
 	public HorizontalAlignmentConstant getHorizontalAlignment()
     {
 	    return this.horizontalAlign;
     }
 
 	/**
 	 * @param nextButtonStyleName
 	 */
 	public void setNextButtonStyleName(String nextButtonStyleName)
     {
     	this.nextButtonStyleName = nextButtonStyleName;
     	
     	if(this.isVertical())
     	{
     		this.verticalNextButton.setStyleName(this.nextButtonStyleName);
     	}
     	else
     	{
     		this.horizontalNextButton.setStyleName(this.nextButtonStyleName);
     	}
     	
     	Scheduler.get().scheduleDeferred(new ScheduledCommand()
 		{
     		public void execute()
 			{
     			Button btn = horizontalNextButton;
     			
     			if(isVertical())
     			{
         			btn = verticalNextButton;
     			}
     			
 				getWrapperElement(btn).setClassName(RollingPanel.this.nextButtonStyleName + "Wrapper");
 			}
 		});
     }
 
 	/**
 	 * @param previousButtonStyleName
 	 */
 	public void setPreviousButtonStyleName(String previousButtonStyleName)
     {
     	this.previousButtonStyleName = previousButtonStyleName;
     	
     	if(this.isVertical())
     	{
     		this.verticalPreviousButton.setStyleName(this.nextButtonStyleName);
     	}
     	else
     	{
     		this.horizontalPreviousButton.setStyleName(this.nextButtonStyleName);
     	}
     	
     	Scheduler.get().scheduleDeferred(new ScheduledCommand()
 		{
     		public void execute()
 			{
     			Button btn = horizontalPreviousButton;
     			
     			if(isVertical())
     			{
         			btn = verticalPreviousButton;
     			}
     			
 				getWrapperElement(btn).setClassName(RollingPanel.this.previousButtonStyleName + "Wrapper");
 			}
 		});
     }
 
 	/**
 	 * @param bodyStyleName the bodyStyleName to set
 	 */
 	public void setBodyStyleName(String bodyStyleName)
 	{
 		this.bodyStyleName = bodyStyleName;
 		this.itemsScrollPanel.setStyleName(this.bodyStyleName);
 	}
 
 	/**
 	 * @return the bodyStyleName
 	 */
 	public String getBodyStyleName()
 	{
 		return bodyStyleName;
 	}
 
 	/**
 	 * @return the nextButtonStyleName
 	 */
 	public String getNextButtonStyleName()
 	{
 		return nextButtonStyleName;
 	}
 
 	/**
 	 * @return the previousButtonStyleName
 	 */
 	public String getPreviousButtonStyleName()
 	{
 		return previousButtonStyleName;
 	}
 }
