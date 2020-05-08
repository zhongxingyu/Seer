 package com.ratethisfest.client;
 
 import java.util.Collections;
 import java.util.List;
 
 import com.google.gwt.animation.client.Animation;
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
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
 import com.ratethisfest.shared.DateTimeUtils;
 import com.ratethisfest.shared.Set;
 
 public class LollapaloozerViewComposite extends Composite {
 
   /**
    * The message displayed to the user when the server cannot be reached or
    * returns an error.
    */
   private static final String SERVER_ERROR = "An error occurred while "
       + "attempting to contact the server. Please check your network "
       + "connection and try again.";
 
   interface Binder extends UiBinder<Widget, LollapaloozerViewComposite> {
   }
 
   private static Binder uiBinder = GWT.create(Binder.class);
 
   /**
    * Create a remote service proxy to talk to the server-side Lollapaloozer
    * service.
    */
   private final LollapaloozerServiceAsync lollapaloozerService = GWT
       .create(LollapaloozerService.class);
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
 
   // @UiField
   // com.google.gwt.user.client.ui.Button queryButton;
 
   // @UiField
   // com.google.gwt.user.client.ui.Button rateButton;
 
   public LollapaloozerViewComposite() {
     initWidget(uiBinder.createAndBindUi(this));
 
     initUiElements();
 
     retrieveSets();
 
     // TODO: see if we wanna auto refresh
     // Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
     // public boolean execute() {
     // retrieveSets();
     // return true;
     // }
     // }, DELAY_MS);
   }
 
   private void initUiElements() {
     title.setText("LOLLAPALOOZER 2012");
     beta.setText("beta");
     android.setHref("http://play.google.com/store/apps/details?id=com.lollapaloozer");
     android.setText("Download Lollapaloozer for Android");
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
     // FlowControl.go(new LollapaloozerChartComposite(appUser.getEmail()));
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
 
     dayInput.addChangeHandler(new ChangeHandler() {
       @Override
       public void onChange(ChangeEvent event) {
         retrieveSets();
         // androidAnimation.run(400);
       }
     });
 
     chartTypeInput.addItem("Score");
     chartTypeInput.addItem("Set Time");
     chartTypeInput.addItem("Artist Name");
 
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
 
     // TODO: DEPRECATED
     // queryButton.addClickHandler(new ClickHandler() {
     // @Override
     // public void onClick(ClickEvent event) {
     // retrieveSets();
     //
     // androidAnimation.run(400);
     // }
     // });
 
     // rateButton.addClickHandler(new ClickHandler() {
     // @Override
     // public void onClick(ClickEvent event) {
     // FlowControl.go(new LollapaloozerEmailComposite());
     // }
     // });
   }
 
   @Override
   public String getTitle() {
     return PageToken.VIEW.getValue();
   }
 
   private void retrieveSets() {
     infoBox.setText("");
     String day = dayInput.getItemText(dayInput.getSelectedIndex());
     lollapaloozerService.getSets("2012", day, new AsyncCallback<List<Set>>() {
 
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
     options.setHeight((setsList.size() * 25) + 20);
     // options.setTitle("Lollapalooza Set Ratings");
     options.setColors("blue", "green");
     AxisOptions axisOptions = AxisOptions.create();
     axisOptions.setMinValue(0);
     axisOptions.setMaxValue(5);
     options.setHAxisOptions(axisOptions);
     ChartArea chartArea = ChartArea.create();
     // chartArea.setTop(10);
     chartArea.setHeight(setsList.size() * 25);
     options.setChartArea(chartArea);
     return options;
   }
 
   private AbstractDataTable createChartDataTable() {
     DataTable data = DataTable.create();
     if (setsList == null) {
       data.addColumn(ColumnType.STRING, "Artist Name");
       data.addColumn(ColumnType.NUMBER, "Average Score");
     } else {
       data.addColumn(ColumnType.STRING, "Artist Name");
       data.addColumn(ColumnType.NUMBER, "Average Score");
       data.addRows(setsList.size());
       if (chartTypeInput.getItemText(chartTypeInput.getSelectedIndex()).equals("Artist Name")) {
         // sort first
         Collections.sort(setsList, ComparatorUtils.SET_NAME_COMPARATOR);
 
         int setNum = 0;
         for (Set set : setsList) {
           data.setValue(setNum, 0, set.getArtistName());
           data.setValue(setNum, 1, set.getAvgScoreOne());
           setNum++;
         }
       } else if (chartTypeInput.getItemText(chartTypeInput.getSelectedIndex()).equals("Score")) {
         // sort first
         Collections.sort(setsList, ComparatorUtils.SET_SCORE_COMPARATOR);
 
         int setNum = 0;
         for (Set set : setsList) {
           data.setValue(setNum, 0, set.getArtistName());
           data.setValue(setNum, 1, set.getAvgScoreOne());
           setNum++;
         }
       } else {
         // sort first
         Collections.sort(setsList, ComparatorUtils.SET_TIME_COMPARATOR);
 
         int setNum = 0;
         for (Set set : setsList) {
           String timeString = DateTimeUtils.militaryToCivilianTime(set.getTimeOne());
           String nameCombo = timeString + ": " + set.getArtistName();
           data.setValue(setNum, 0, nameCombo);
           data.setValue(setNum, 1, set.getAvgScoreOne());
           setNum++;
         }
       }
     }
     return data;
   }
 
   public static class SetsTable extends CellTable<Set> {
 
     public Column<Set, String> dayColumn;
     public Column<Set, String> timeOneColumn;
     public Column<Set, String> artistNameColumn;
     public Column<Set, String> avgScoreOneColumn;
     public Column<Set, String> stageOneColumn;
 
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
           String value = "";
           if (object.getAvgScoreOne() != null) {
             value = object.getAvgScoreOne().toString();
           }
           if (object.getNumRatingsOne() != null) {
             value += " (" + object.getNumRatingsOne().toString() + ")";
           }
           return value;
         }
       };
       addColumn(avgScoreOneColumn, "Average Score");
       addColumnStyleName(4, "columnFill");
       addColumnStyleName(4, resources.cellTableStyle().columnScore());
 
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
