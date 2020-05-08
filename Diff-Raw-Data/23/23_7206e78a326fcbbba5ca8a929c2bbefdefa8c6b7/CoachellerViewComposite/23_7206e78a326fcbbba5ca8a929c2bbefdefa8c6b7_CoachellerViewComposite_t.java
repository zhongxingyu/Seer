 package com.coacheller.client;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import com.coacheller.shared.DateTimeUtils;
 import com.coacheller.shared.Set;
 import com.google.gwt.animation.client.Animation;
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.gwt.visualization.client.AbstractDataTable;
 import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
 import com.google.gwt.visualization.client.ChartArea;
 import com.google.gwt.visualization.client.DataTable;
 import com.google.gwt.visualization.client.VisualizationUtils;
 import com.google.gwt.visualization.client.visualizations.corechart.AxisOptions;
 import com.google.gwt.visualization.client.visualizations.corechart.BarChart;
 import com.google.gwt.visualization.client.visualizations.corechart.Options;
 
 public class CoachellerViewComposite extends Composite {
 
   /**
    * The message displayed to the user when the server cannot be reached or
    * returns an error.
    */
   private static final String SERVER_ERROR = "An error occurred while "
       + "attempting to contact the server. Please check your network "
       + "connection and try again.";
 
   interface Binder extends UiBinder<Widget, CoachellerViewComposite> {
   }
 
   private static Binder uiBinder = GWT.create(Binder.class);
 
   /**
    * Create a remote service proxy to talk to the server-side Coacheller
    * service.
    */
   private final CoachellerServiceAsync coachellerService = GWT.create(CoachellerService.class);
   private List<Set> setsList;
 
   @UiField
   Label title;
 
   @UiField
   Label beta;
 
   @UiField
  Anchor android;

  @UiField
   Label infoBox;
 
   @UiField
   ListBox dayInput;
 
   @UiField
   ListBox chartTypeInput;
 
   @UiField
   SimplePanel setsChartPanel;
 
   @UiField
   SetsTable setsTable;
 
   @UiField
   com.google.gwt.user.client.ui.Button queryButton;
 
   @UiField
   com.google.gwt.user.client.ui.Button rateButton;
 
   public CoachellerViewComposite() {
     initWidget(uiBinder.createAndBindUi(this));
 
     initUiElements();
 
     // TODO: see if we wanna auto refresh
     // Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
     // public boolean execute() {
     // retrieveSets();
     // return true;
     // }
     // }, DELAY_MS);
   }
 
   private void initUiElements() {
     title.setText("Coacheller 2012");
     beta.setText("beta");
    android.setHref("http://play.google.com/store/apps/details?id=com.coacheller");
    android.setText("Download Coacheller for Android");
    android.setTarget("_blank");
 
     ListDataProvider<Set> listDataProvider = new ListDataProvider<Set>();
     listDataProvider.addDataDisplay(setsTable);
     setsList = listDataProvider.getList();
 
     // Create a callback to be called when the visualization API
     // has been loaded.
     Runnable onLoadCallback = new Runnable() {
       public void run() {
         // Create a pie chart visualization.
         BarChart chart = new BarChart(createChartDataTable(), createOptions());
         setsChartPanel.add(chart);
       }
     };
 
     // Load the visualization api, passing the onLoadCallback to be called
     // when loading is done.
     VisualizationUtils.loadVisualizationApi(onLoadCallback, BarChart.PACKAGE);
 
     Element androidElement = getElement().getFirstChildElement().getFirstChildElement();
     final Animation androidAnimation = new AndroidAnimation(androidElement);
 
     // usersTable.setRowStyles(new RowStyles<Set>() {
     // public String getStyleNames(Set row, int rowIndex) {
     // return isActive(row) ? "Active" : "Inactive";
     // }
     // });
     //
     // usersTable.activeColumn.setFieldUpdater(new FieldUpdater<Set, Boolean>()
     // {
     // public void update(int index, Set appUser, Boolean value) {
     // FlowControl.go(new CoachellerChartComposite(appUser.getEmail()));
     // }
     // });
     //
     // usersTable.dateColumn.setFieldUpdater(new FieldUpdater<Set, Date>() {
     // public void update(int index, Set appUser, Date value) {
     // AppUserRequest request = requestFactory.appUserRequest();
     // Set updatedTask = request.edit(appUser);
     // updatedTask.setDateModified(value);
     // request.updateAppUser(updatedTask).fire();
     // }
     // });
     //
     // usersTable.deleteColumn.setFieldUpdater(new FieldUpdater<Set, String>() {
     // public void update(int index, Set appUser, String value) {
     // AppUserRequest request = requestFactory.appUserRequest();
     // request.deleteAppUser(appUser).fire();
     // setsList.remove(appUser);
     // }
     // });
 
     dayInput.addItem("Friday");
     dayInput.addItem("Saturday");
     dayInput.addItem("Sunday");
 
     chartTypeInput.addItem("Artist Name");
     chartTypeInput.addItem("Set Time");
 
     chartTypeInput.addChangeHandler(new ChangeHandler() {
       @Override
       public void onChange(ChangeEvent event) {
         final DataTable dataTable = (DataTable) createChartDataTable();
 
         // Create a callback to be called when the visualization API
         // has been loaded.
         Runnable onLoadCallback = new Runnable() {
           public void run() {
             // Create a pie chart visualization.
             BarChart chart = new BarChart(dataTable, createOptions());
             setsChartPanel.clear();
             setsChartPanel.add(chart);
           }
         };
 
         // Load the visualization api, passing the onLoadCallback to be called
         // when loading is done.
         VisualizationUtils.loadVisualizationApi(onLoadCallback, BarChart.PACKAGE);
       }
     });
 
     // userNameInput.addKeyUpHandler(new KeyUpHandler() {
     // public void onKeyUp(KeyUpEvent event) {
     // if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
     // String message = userNameInput.getText();
     // userNameInput.setText("");
     // sendNewUserToServer(message);
     //
     // androidAnimation.run(400);
     // }
     // }
     // });
 
     queryButton.addClickHandler(new ClickHandler() {
       @Override
       public void onClick(ClickEvent event) {
         retrieveSets();
 
         androidAnimation.run(400);
       }
     });
 
     rateButton.addClickHandler(new ClickHandler() {
       @Override
       public void onClick(ClickEvent event) {
         FlowControl.go(new CoachellerEmailComposite());
       }
     });
   }
 
   @Override
   public String getTitle() {
     return PageToken.VIEW.getValue();
   }
 
   private void retrieveSets() {
     infoBox.setText("");
     String day = dayInput.getItemText(dayInput.getSelectedIndex());
     coachellerService.getSets("2012", day, new AsyncCallback<List<Set>>() {
 
       public void onFailure(Throwable caught) {
         // Show the RPC error message to the user
         infoBox.setText(SERVER_ERROR);
       }
 
       public void onSuccess(List<Set> result) {
         setsList.clear();
         setsList.addAll(result);
 
         final DataTable dataTable = (DataTable) createChartDataTable();
 
         // Create a callback to be called when the visualization API
         // has been loaded.
         Runnable onLoadCallback = new Runnable() {
           public void run() {
             // Create a pie chart visualization.
             BarChart chart = new BarChart(dataTable, createOptions());
             setsChartPanel.clear();
             setsChartPanel.add(chart);
           }
         };
 
         // Load the visualization api, passing the onLoadCallback to be called
         // when loading is done.
         VisualizationUtils.loadVisualizationApi(onLoadCallback, BarChart.PACKAGE);
 
       }
     });
   }
 
   private Options createOptions() {
     Options options = Options.create();
     options.setHeight((setsList.size() * 30) + 20);
     // options.setTitle("Coachella Set Ratings");
     options.setColors("blue", "green");
     AxisOptions axisOptions = AxisOptions.create();
     axisOptions.setMinValue(0);
     axisOptions.setMaxValue(5);
     options.setHAxisOptions(axisOptions);
     ChartArea chartArea = ChartArea.create();
     // chartArea.setTop(10);
     chartArea.setHeight(setsList.size() * 30);
     options.setChartArea(chartArea);
     return options;
   }
 
   private AbstractDataTable createChartDataTable() {
     DataTable data = DataTable.create();
     if (setsList == null) {
       data.addColumn(ColumnType.STRING, "Artist Name");
       data.addColumn(ColumnType.NUMBER, "Week 1 Average Score");
       data.addColumn(ColumnType.NUMBER, "Week 2 Average Score");
     } else {
       data.addColumn(ColumnType.STRING, "Artist Name");
       data.addColumn(ColumnType.NUMBER, "Week 1 Average Score");
       data.addColumn(ColumnType.NUMBER, "Week 2 Average Score");
       data.addRows(setsList.size());
       if (chartTypeInput.getItemText(chartTypeInput.getSelectedIndex()).equals("Artist Name")) {
         // sort first
         Collections.sort(setsList, SET_NAME_COMPARATOR);
 
         int setNum = 0;
         for (Set set : setsList) {
           data.setValue(setNum, 0, set.getArtistName());
           data.setValue(setNum, 1, set.getAvgScoreOne());
           data.setValue(setNum, 2, set.getAvgScoreTwo());
           setNum++;
         }
       } else {
         // sort first
         Collections.sort(setsList, SET_TIME_COMPARATOR);
 
         int setNum = 0;
         for (Set set : setsList) {
           String timeString = DateTimeUtils.militaryToCivilianTime(set.getTimeOne());
           String nameCombo = timeString + ": " + set.getArtistName();
           data.setValue(setNum, 0, nameCombo);
           data.setValue(setNum, 1, set.getAvgScoreOne());
           data.setValue(setNum, 2, set.getAvgScoreTwo());
           setNum++;
         }
       }
     }
     return data;
   }
 
   public static final Comparator<? super Set> SET_NAME_COMPARATOR = new Comparator<Set>() {
     public int compare(Set t0, Set t1) {
       return t0.getArtistName().compareTo(t1.getArtistName());
     }
   };
 
   public static final Comparator<? super Set> SET_TIME_COMPARATOR = new Comparator<Set>() {
     public int compare(Set t0, Set t1) {
       // Sort by set time first
       if (t0.getTimeOne() > t1.getTimeOne()) {
         return 1;
       } else if (t0.getTimeOne() < t1.getTimeOne()) {
         return -1;
       } else {
         // Sort items alphabetically within each group
         return t0.getArtistName().compareTo(t1.getArtistName());
       }
     }
   };
 
   public static class SetsTable extends CellTable<Set> {
 
     public Column<Set, String> dayColumn;
     public Column<Set, String> timeOneColumn;
     // public Column<Set, String> timeTwoColumn;
     public Column<Set, String> artistNameColumn;
     public Column<Set, String> avgScoreOneColumn;
     public Column<Set, String> avgScoreTwoColumn;
     public Column<Set, String> numRatingsOneColumn;
     public Column<Set, String> numRatingsTwoColumn;
     public Column<Set, String> stageOneColumn;
 
     // public Column<Set, String> stageTwoColumn;
 
     interface TasksTableResources extends CellTable.Resources {
       @Source("SetsTable.css")
       TableStyle cellTableStyle();
     }
 
     interface TableStyle extends CellTable.Style {
 
       String columnTime();
 
       String columnDay();
 
       String columnName();
 
       String columnScore();
 
       String columnCount();
 
       String columnStage();
     }
 
     private static TasksTableResources resources = GWT.create(TasksTableResources.class);
 
     public SetsTable() {
       super(100, resources);
 
       dayColumn = new Column<Set, String>(new TextCell()) {
         @Override
         public String getValue(Set object) {
           return object.getDay();
         }
       };
       addColumn(dayColumn, "Day");
       addColumnStyleName(0, "columnFill");
       addColumnStyleName(0, resources.cellTableStyle().columnDay());
 
       timeOneColumn = new Column<Set, String>(new TextCell()) {
         @Override
         public String getValue(Set object) {
           return DateTimeUtils.militaryToCivilianTime(object.getTimeOne());
         }
       };
       addColumn(timeOneColumn, "Set Time");
       addColumnStyleName(1, "columnFill");
       addColumnStyleName(1, resources.cellTableStyle().columnTime());
 
       stageOneColumn = new Column<Set, String>(new TextCell()) {
         @Override
         public String getValue(Set object) {
           return object.getStageOne();
         }
       };
       addColumn(stageOneColumn, "Stage");
       addColumnStyleName(2, "columnFill");
       addColumnStyleName(2, resources.cellTableStyle().columnStage());
 
       artistNameColumn = new Column<Set, String>(new TextCell()) {
         @Override
         public String getValue(Set object) {
           return object.getArtistName();
         }
       };
       addColumn(artistNameColumn, "Artist Name");
       addColumnStyleName(3, "columnFill");
       addColumnStyleName(3, resources.cellTableStyle().columnName());
 
       avgScoreOneColumn = new Column<Set, String>(new TextCell()) {
         @Override
         public String getValue(Set object) {
           if (object.getAvgScoreOne() != null) {
             return object.getAvgScoreOne().toString();
           }
           return "";
         }
       };
       addColumn(avgScoreOneColumn, "Week 1 Score");
       addColumnStyleName(4, "columnFill");
       addColumnStyleName(4, resources.cellTableStyle().columnScore());
 
       numRatingsOneColumn = new Column<Set, String>(new TextCell()) {
         @Override
         public String getValue(Set object) {
           if (object.getNumRatingsOne() != null) {
             return object.getNumRatingsOne().toString();
           }
           return "";
         }
       };
       addColumn(numRatingsOneColumn, "# Ratings");
       addColumnStyleName(5, "columnFill");
       addColumnStyleName(5, resources.cellTableStyle().columnCount());
 
       avgScoreTwoColumn = new Column<Set, String>(new TextCell()) {
         @Override
         public String getValue(Set object) {
           if (object.getAvgScoreTwo() != null) {
             return object.getAvgScoreTwo().toString();
           }
           return "";
         }
       };
       addColumn(avgScoreTwoColumn, "Week 2 Score");
       addColumnStyleName(6, "columnFill");
       addColumnStyleName(6, resources.cellTableStyle().columnScore());
 
       numRatingsTwoColumn = new Column<Set, String>(new TextCell()) {
         @Override
         public String getValue(Set object) {
           if (object.getNumRatingsTwo() != null) {
             return object.getNumRatingsTwo().toString();
           }
           return "";
         }
       };
       addColumn(numRatingsTwoColumn, "# Ratings");
       addColumnStyleName(7, "columnFill");
       addColumnStyleName(7, resources.cellTableStyle().columnCount());
 
     }
   }
 
   class AndroidAnimation extends Animation {
     private static final int TOP = -50;
     private static final int BOTTOM = 150;
     Element element;
 
     public AndroidAnimation(Element element) {
       this.element = element;
     }
 
     @Override
     protected void onStart() {
       element.getStyle().setTop(TOP, Unit.PX);
     }
 
     @Override
     protected void onUpdate(double progress) {
       element.getStyle().setTop(TOP + (BOTTOM - TOP) * interpolate(progress), Unit.PX);
     }
 
     @Override
     protected void onComplete() {
       element.getStyle().setTop(TOP, Unit.PX);
     }
   }
 
 }
