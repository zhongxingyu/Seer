 package de.graind.client.widgets;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.event.dom.client.MouseOverEvent;
 import com.google.gwt.event.dom.client.MouseOverHandler;
 import com.google.gwt.gdata.client.EventEntry;
 import com.google.gwt.gdata.client.When;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DecoratedPopupPanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.UIObject;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 import de.graind.client.util.CalendarUtil;
 
 public class MonthlyWidget extends Composite implements MonthlyWidgetView {
 
   private static final int LAST_DAY_OF_WEEK = 6; // Sunday
 
   private static MonthlyWidgetUiBinder uiBinder = GWT.create(MonthlyWidgetUiBinder.class);
 
   private Controller controller;
   private EventEntry[] events;
 
   @UiField
   FlexTable daysPanel;
   @UiField
   Label currentMonthLabel;
   @UiField
   Style style;
   @UiField
   VerticalPanel prevButton;
   @UiField
   VerticalPanel nextButton;
 
   interface MonthlyWidgetUiBinder extends UiBinder<Widget, MonthlyWidget> {
   }
 
   interface Style extends CssResource {
     String buttonLabel();
 
     String buttonLabelHovered();
 
     String currentMonthLabel();
 
     String normal();
 
     String dayWithoutEvent();
 
     String dayWithEvent();
 
     String firstDayOfWeekWithEvent();
 
     String firstDayOfWeekWithoutEvent();
 
     String daysTable();
   }
 
   public MonthlyWidget() {
     initWidget(uiBinder.createAndBindUi(this));
   }
 
   public MonthlyWidget(String firstName) {
     this();
   }
 
   @Override
   public void init(Controller controller) {
     GWT.log("MonthlyWidget init");
     this.controller = controller;
 
     addButtonEventHandlers();
 
     // Set the update hanlder
     controller.setEventsCallback(new EventUpdater());
 
   }
 
   private void addButtonEventHandlers() {
     // clickhandler for real actions
     nextButton.addDomHandler(new ClickHandler() {
       @Override
       public void onClick(ClickEvent event) {
         MonthlyWidget.this.controller.nextMonth();
       }
     }, ClickEvent.getType());
     prevButton.addDomHandler(new ClickHandler() {
       @Override
       public void onClick(ClickEvent event) {
         MonthlyWidget.this.controller.previousMonth();
       }
     }, ClickEvent.getType());
 
     // style!
     nextButton.setStyleName(style.buttonLabel());
     prevButton.setStyleName(style.buttonLabel());
     nextButton.addDomHandler(new MouseOverHandler() {
 
       @Override
       public void onMouseOver(MouseOverEvent event) {
         nextButton.setStyleName(style.buttonLabelHovered());
       }
     }, MouseOverEvent.getType());
     prevButton.addDomHandler(new MouseOverHandler() {
 
       @Override
       public void onMouseOver(MouseOverEvent event) {
         prevButton.setStyleName(style.buttonLabelHovered());
       }
     }, MouseOverEvent.getType());
 
     nextButton.addDomHandler(new MouseOutHandler() {
 
       @Override
       public void onMouseOut(MouseOutEvent event) {
         nextButton.setStyleName(style.buttonLabel());
       }
     }, MouseOutEvent.getType());
     prevButton.addDomHandler(new MouseOutHandler() {
 
       @Override
       public void onMouseOut(MouseOutEvent event) {
         prevButton.setStyleName(style.buttonLabel());
       }
     }, MouseOutEvent.getType());
   }
 
   private void onUpdateEvent() {
     int month = controller.getMonth();
     int year = controller.getYear();
     int days = CalendarUtil.getDaysOfMonth(year, month);
     currentMonthLabel.setText(CalendarUtil.getMonthLabel(month) + " " + year);
 
     // fill daysPanel with days and the days with events
 
     daysPanel.clear();
     int day = 1;
     int firstWeekdayOfMonth = CalendarUtil.getDayOfWeek(day, month, year);
     int col;
     for (; day <= days; day++) {
       col = (day - 1 + firstWeekdayOfMonth);
       daysPanel.setWidget(col / 21, col % 21, createLabel(day, month, year, (col % 7 == LAST_DAY_OF_WEEK)));
     }
 
   }
 
   /**
    * Creates a Label with click handler and styles
    * 
    * @param day
    * @param month
    * @param year
    * @return a label
    */
   private Label createLabel(int day, int month, int year, boolean lastDayOfWeek) {
     final Label l = new Label((day > 9 ? "" : "0") + day, false);
     l.setWidth("2ex");
     List<EventEntry> todaysEvents = getEventsForDay(day);
     if (todaysEvents.size() > 0) {
       // style and ClickHandler
       if (lastDayOfWeek) {
         l.setStyleName(style.firstDayOfWeekWithEvent());
       } else {
         l.setStyleName(style.dayWithEvent());
       }
 
       // handler
      l.addClickHandler(new DayClickHandler(todaysEvents, l));
     } else {
       // just style.
       if (lastDayOfWeek) {
         l.setStyleName(style.firstDayOfWeekWithoutEvent());
       } else {
         l.setStyleName(style.dayWithoutEvent());
       }
     }
 
     return l;
   }
 
   private List<EventEntry> getEventsForDay(int day) {
     List<EventEntry> ret = new ArrayList<EventEntry>();
 
     for (EventEntry entry : events) {
       for (When when : entry.getTimes()) {
         if (CalendarUtil.getDay(when.getStartTime().getDate()) == day) {
           ret.add(entry);
         }
       }
     }
     return ret;
   }
 
   private class EventUpdater implements AsyncCallback<EventEntry[]> {
 
     @Override
     public void onSuccess(EventEntry[] result) {
       GWT.log("We got " + result.length + " new entries.");
       events = result;
       onUpdateEvent();
     }
 
     @Override
     public void onFailure(Throwable caught) {
       GWT.log("could not get the events: " + caught.getMessage());
     }
   }
 
   private class DayClickHandler implements ClickHandler {
 
     private List<EventEntry> events;
     private UIObject target;
 
    public DayClickHandler(List<EventEntry> events, UIObject target) {
       this.events = events;
       this.target = target;
     }
 
     @Override
     public void onClick(ClickEvent event) {
       DecoratedPopupPanel popup = new DecoratedPopupPanel(true);
       popup.setWidth("200px");
       popup.add(new Label(eventsToMessage()));
       popup.show();
       popup.showRelativeTo(target);
     }
 
     private String eventsToMessage() {
       String ret = "";
       for (EventEntry entry : events) {
         for (When when : entry.getTimes()) {
          ret = ret + "Event: " + entry.getTitle().getText() + " (" + when.getStartTime().getDate().toString() + ")"
              + "\n";
         }
       }
       return ret;
     }
 
   }
 
 }
