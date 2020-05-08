 /**
  *
  */
 package expensable.client.view.dashboard;
 
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.ResizeComposite;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.visualization.client.AbstractDataTable;
 import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
 import com.google.gwt.visualization.client.DataTable;
 import com.google.gwt.visualization.client.VisualizationUtils;
 import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine;
 import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine.Options;
 import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine.WindowMode;
 import com.google.gwt.visualization.client.visualizations.PieChart;
 
 import expensable.shared.models.NewsItem;
 
 /**
  * @author dpurpura
  */
 public class DashboardViewImpl extends ResizeComposite implements DashboardView {
 
   private static Binder binder = GWT.create(Binder.class);
 
   interface Binder extends UiBinder<Widget, DashboardViewImpl>{}
 
   private DashboardPresenter presenter;
 
   @UiField ScrollPanel scrollPanel;
   @UiField SimplePanel timelinePanel;
   @UiField NewsFeedView newsfeed;
 
   public DashboardViewImpl() {
     initWidget(binder.createAndBindUi(this));
  }

  @Override
  public void setPresenter(DashboardPresenter presenter) {
    this.presenter = presenter;
 
     Runnable onLoadCallback = new Runnable() {
       @Override
       public void run() {
         AnnotatedTimeLine timeline
             = new AnnotatedTimeLine(createTable(), createOptions(), "720px", "300px");
         timelinePanel.add(timeline);
       }
     };
     VisualizationUtils.loadVisualizationApi(onLoadCallback, PieChart.PACKAGE);
     newsfeed.clear();
     newsfeed.add(presenter.getNewsItems());
   }
 
   private Options createOptions() {
     Options options = Options.create();
     options.setWindowMode(WindowMode.WINDOW);
     options.setDisplayAnnotations(true);
     return options;
   }
 
   private AbstractDataTable createTable() {
     DataTable data = DataTable.create();
     data.addColumn(ColumnType.DATE, "Date");
     data.addColumn(ColumnType.NUMBER, "Amount");
     data.addColumn(ColumnType.STRING, "Title");
 
     List<NewsItem> items = presenter.getNewsItems();
     data.addRows(items.size());
     int row = 0;
     int runningTotal = 0;
     for (NewsItem item : items) {
       data.setValue(row, 0, item.getLastModified());
       runningTotal += item.getAmount();
       data.setValue(row, 1, item.getAmount());
       data.setValue(row, 2, item.getName());
       row++;
     }
     return data;
   }
 }
