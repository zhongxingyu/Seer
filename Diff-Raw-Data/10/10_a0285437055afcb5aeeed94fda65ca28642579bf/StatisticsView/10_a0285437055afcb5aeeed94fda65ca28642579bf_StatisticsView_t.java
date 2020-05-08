 package de.objectcode.time4u.client.ui.views;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.part.ViewPart;
 
 import de.objectcode.time4u.client.store.api.RepositoryFactory;
 import de.objectcode.time4u.client.store.api.StatisticEntry;
 import de.objectcode.time4u.client.store.api.event.IRepositoryListener;
 import de.objectcode.time4u.client.store.api.event.RepositoryEvent;
 import de.objectcode.time4u.client.store.api.event.RepositoryEventType;
 import de.objectcode.time4u.client.ui.UIPlugin;
 import de.objectcode.time4u.client.ui.preferences.PreferenceConstants;
 import de.objectcode.time4u.client.ui.util.TimeFormat;
 import de.objectcode.time4u.server.api.data.CalendarDay;
 import de.objectcode.time4u.server.api.data.DayInfoSummary;
 import de.objectcode.time4u.server.api.data.ProjectSummary;
 import de.objectcode.time4u.server.api.data.TimePolicy;
 import de.objectcode.time4u.server.api.filter.DayInfoFilter;
 
 public class StatisticsView extends ViewPart implements ISelectionListener, IRepositoryListener
 {
   public static final String ID = "de.objectcode.time4u.client.ui.view.statisticsView";
 
   private ProjectSummary m_selectedProject;
   private Calendar m_selectedDay;
 
   private Label m_projectLabel;
   private Label m_mainProjectLabel;
   private Label m_rowLabels[];
   private Label m_statisticLabels[];
   private boolean m_calculateOvertime;
   private Composite m_top;
   private int m_refreshCounter = 0;
 
   @Override
   public void createPartControl(final Composite parent)
   {
     m_selectedDay = Calendar.getInstance();
 
     createControls(parent);
 
     getSite().getPage().addSelectionListener(this);
 
     RepositoryFactory.getRepository().addRepositoryListener(RepositoryEventType.DAYINFO, this);
 
     UIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
       public void propertyChange(final PropertyChangeEvent event)
       {
         if (PreferenceConstants.UI_OVERTIME_CALCULATE.equals(event.getProperty())) {
           m_top.dispose();
           createControls(parent);
           parent.layout();
           updateStatisticLabels();
         } else if (m_calculateOvertime
             && (PreferenceConstants.UI_OVERTIME_REGULARTIME.equals(event.getProperty()) || PreferenceConstants.UI_OVERTIME_COLORIZE
                 .equals(event.getProperty()))) {
           updateStatisticLabels();
         }
       }
     });
 
     updateStatisticLabels();
   }
 
   @Override
   public void setFocus()
   {
     // Nothing to be focused
   }
 
   @Override
   public void dispose()
   {
     getSite().getPage().removeSelectionListener(this);
 
     RepositoryFactory.getRepository().removeRepositoryListener(RepositoryEventType.DAYINFO, this);
 
     super.dispose();
   }
 
   public void selectionChanged(final IWorkbenchPart part, final ISelection selection)
   {
     if (selection instanceof IAdaptable) {
       final ProjectSummary project = (ProjectSummary) ((IAdaptable) selection).getAdapter(ProjectSummary.class);
      final CalendarDay day = (CalendarDay) ((IAdaptable) selection).getAdapter(CalendarDay.class);
 
       if (!m_selectedDay.equals(day.getCalendar()) || project == null && m_selectedProject != null || project != null
           && !project.equals(m_selectedProject)) {
         m_selectedProject = project;
         m_selectedDay = day.getCalendar();
         updateStatisticLabels();
       }
     }
   }
 
   public void handleRepositoryEvent(final RepositoryEvent event)
   {
     switch (event.getEventType()) {
       case DAYINFO:
         // Do not queue more than 2 refresh
         synchronized (this) {
           if (m_refreshCounter >= 2) {
             return;
           }
 
           m_refreshCounter++;
         }
 
         m_mainProjectLabel.getDisplay().asyncExec(new Runnable() {
           public void run()
           {
             try {
               updateStatisticLabels();
             } finally {
               synchronized (StatisticsView.this) {
                 m_refreshCounter--;
               }
             }
           }
         });
 
         break;
     }
   }
 
   protected void createControls(final Composite parent)
   {
     final Composite top = new Composite(parent, SWT.NONE);
 
     m_top = top;
 
     GridLayout layout = new GridLayout();
     layout.marginHeight = 5;
     layout.marginWidth = 10;
     layout.numColumns = 4;
     top.setLayout(layout);
 
     final Label projectLabel = new Label(top, SWT.LEFT);
 
     projectLabel.setText(UIPlugin.getDefault().getString("project.label"));
     m_projectLabel = new Label(top, SWT.LEFT | SWT.BORDER);
     m_projectLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
     final Label mainProjectLabel = new Label(top, SWT.LEFT);
 
     mainProjectLabel.setText(UIPlugin.getDefault().getString("statisticsView.mainProject.label"));
     m_mainProjectLabel = new Label(top, SWT.LEFT | SWT.BORDER);
     m_mainProjectLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
     final String rows[] = new String[] { UIPlugin.getDefault().getString("day.label"),
         UIPlugin.getDefault().getString("week.label"), UIPlugin.getDefault().getString("month.label"),
         UIPlugin.getDefault().getString("year.label") };
 
     String columns[];
 
     m_calculateOvertime = UIPlugin.getDefault().getPreferenceStore().getBoolean(
         PreferenceConstants.UI_OVERTIME_CALCULATE);
 
     if (m_calculateOvertime) {
       columns = new String[] { UIPlugin.getDefault().getString("project.label"),
           UIPlugin.getDefault().getString("statisticsView.mainProject.label"),
           UIPlugin.getDefault().getString("statisticsView.all.label"),
           UIPlugin.getDefault().getString("statisticsView.overtime.label") };
     } else {
       columns = new String[] { UIPlugin.getDefault().getString("project.label"),
           UIPlugin.getDefault().getString("statisticsView.mainProject.label"),
           UIPlugin.getDefault().getString("statisticsView.all.label") };
     }
 
     final Composite statisticsTable = new Composite(top, SWT.NONE);
 
     GridData data = new GridData(GridData.FILL_BOTH);
     data.horizontalSpan = 4;
     statisticsTable.setLayoutData(data);
 
     layout = new GridLayout(columns.length + 1, true);
     layout.marginWidth = 0;
     layout.marginHeight = 0;
     layout.horizontalSpacing = 1;
     layout.verticalSpacing = 1;
 
     statisticsTable.setLayout(layout);
 
     m_statisticLabels = new Label[rows.length * columns.length];
 
     Label headingLabel = new Label(statisticsTable, SWT.CENTER);
 
     for (final String column : columns) {
       headingLabel = new Label(statisticsTable, SWT.CENTER);
       headingLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
       headingLabel.setText(column);
     }
 
     int i = 0;
 
     m_rowLabels = new Label[rows.length];
 
     for (int j = 0; j < rows.length; j++) {
       m_rowLabels[j] = new Label(statisticsTable, SWT.RIGHT);
       m_rowLabels[j].setText(rows[j] + " ");
 
       data = new GridData(GridData.FILL_HORIZONTAL);
 
       m_rowLabels[j].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
       for (int k = 0; k < columns.length; k++) {
         m_statisticLabels[i] = new Label(statisticsTable, SWT.CENTER | SWT.BORDER);
         m_statisticLabels[i].setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
         i++;
       }
     }
   }
 
   protected void updateStatisticLabels()
   {
     try {
       final int day = m_selectedDay.get(Calendar.DAY_OF_MONTH);
       final int week = m_selectedDay.get(Calendar.WEEK_OF_YEAR);
       final int month = m_selectedDay.get(Calendar.MONTH) + 1;
       final int year = m_selectedDay.get(Calendar.YEAR);
 
       m_rowLabels[0].setText("Day (" + day + "." + month + "." + year + ")");
       m_rowLabels[1].setText("Week (" + week + "/" + year + ")");
       m_rowLabels[2].setText("Month (" + month + "." + year + ")");
       m_rowLabels[3].setText("Year (" + year + ")");
 
       String mainProjectId;
 
       if (m_selectedProject != null) {
         final List<ProjectSummary> projectPath = RepositoryFactory.getRepository().getProjectRepository()
             .getProjectPath(m_selectedProject.getId());
         mainProjectId = projectPath.get(0).getId();
         m_projectLabel.setText(m_selectedProject.getName());
         m_mainProjectLabel.setText(projectPath.get(0).getName());
       } else {
         mainProjectId = null;
       }
 
       final List<Map<String, ? extends StatisticEntry>> statistics = new ArrayList<Map<String, ? extends StatisticEntry>>();
 
       statistics.add(RepositoryFactory.getRepository().getStatisticRepository().getDayStatistic(day, month, year));
       statistics.add(RepositoryFactory.getRepository().getStatisticRepository().getWeekStatistic(week, year));
       statistics.add(RepositoryFactory.getRepository().getStatisticRepository().getMonthStatistic(month, year));
       statistics.add(RepositoryFactory.getRepository().getStatisticRepository().getYearStatistic(year));
 
       int[] overtime;
 
       if (m_calculateOvertime) {
         overtime = calculateOvertime(new CalendarDay(day, month, year));
       } else {
         overtime = null;
       }
 
       int i = 0;
       int j = 0;
 
       for (final Map<String, ? extends StatisticEntry> statistic : statistics) {
         int projectSecs = 0;
         int mainProjectSecs = 0;
         int totalSecs = 0;
 
         if (statistic != null) {
           if (statistic.containsKey(null)) {
             totalSecs = statistic.get(null).getAggregateSumDuration();
           }
           if (mainProjectId != null && statistic.containsKey(mainProjectId)) {
             mainProjectSecs = statistic.get(mainProjectId).getAggregateSumDuration();
           }
           if (m_selectedProject != null && statistic.containsKey(m_selectedProject.getId())) {
             projectSecs = statistic.get(m_selectedProject.getId()).getSumDuration();
           }
         }
 
         if (m_selectedProject != null) {
           m_statisticLabels[i++].setText(TimeFormat.formatFull(projectSecs));
           m_statisticLabels[i++].setText(TimeFormat.formatFull(mainProjectSecs));
         } else {
           m_statisticLabels[i++].setText("");
           m_statisticLabels[i++].setText("");
         }
 
         m_statisticLabels[i++].setText(TimeFormat.formatFull(totalSecs));
 
         if (overtime != null && j < overtime.length) {
           m_statisticLabels[i].setText(TimeFormat.formatFull(overtime[j]));
 
           if (UIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.UI_OVERTIME_COLORIZE)) {
             if (overtime[j] < 0) {
               m_statisticLabels[i].setForeground(m_statisticLabels[i].getDisplay().getSystemColor(SWT.COLOR_RED));
             } else {
               m_statisticLabels[i]
                   .setForeground(m_statisticLabels[i].getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
             }
           } else {
             m_statisticLabels[i].setForeground(m_top.getForeground());
           }
 
           i++;
           j++;
         }
       }
     } catch (final Exception e) {
       UIPlugin.getDefault().log(e);
     }
   }
 
   protected int[] calculateOvertime(final CalendarDay currentDay)
   {
     int overtimeDay = 0;
     int overtimeWeek = 0;
     int overtimeMonth = 0;
 
     final int currentWeek = currentDay.getCalendar().get(Calendar.WEEK_OF_YEAR);
 
     try {
       final IPreferenceStore store = UIPlugin.getDefault().getPreferenceStore();
       final List<TimePolicy> timePolicies = RepositoryFactory.getRepository().getWorkItemRepository().getTimePolicies();
 
       if (timePolicies != null && !timePolicies.isEmpty()) {
         final Calendar now = Calendar.getInstance();
         final Calendar cal = currentDay.getCalendar();
 
         cal.add(Calendar.MONTH, -1);
 
         for (int i = 0; i < 3; i++) {
           final int month = cal.get(Calendar.MONTH) + 1;
           final int year = cal.get(Calendar.YEAR);
 
           final Map<CalendarDay, DayInfoSummary> dayInfos = new HashMap<CalendarDay, DayInfoSummary>();
           for (final DayInfoSummary dayInfo : RepositoryFactory.getRepository().getWorkItemRepository()
               .getDayInfoSummaries(DayInfoFilter.filterMonth(year, month))) {
             dayInfos.put(dayInfo.getDay(), dayInfo);
           }
 
           cal.set(Calendar.DAY_OF_MONTH, 1);
 
           while (cal.get(Calendar.MONTH) + 1 == month) {
             if (cal.compareTo(now) <= 0) {
               final CalendarDay day = new CalendarDay(cal);
 
               DayInfoSummary dayInfo = null;
 
               int overtime = 0;
 
               if (dayInfos != null) {
                 dayInfo = dayInfos.get(day);
               }
 
               if (dayInfo == null || dayInfo.getRegularTime() < 0) {
                 int regularTime = -1;
 
                 for (final TimePolicy timePolicy : timePolicies) {
                   if ((regularTime = timePolicy.getRegularTime(day)) >= 0) {
                     overtime = (dayInfo != null ? dayInfo.getSumDurations() : 0) - regularTime;
 
                     break;
                   }
                 }
               } else if (dayInfo != null && dayInfo.getRegularTime() >= 0) {
                 overtime = dayInfo.getSumDurations() - dayInfo.getRegularTime();
               }
 
               if (day.getMonth() == currentDay.getMonth()) {
                 overtimeMonth += overtime;
               }
 
               if (day.getCalendar().get(Calendar.WEEK_OF_YEAR) == currentWeek) {
                 overtimeWeek += overtime;
               }
 
               if (currentDay.equals(day)) {
                 overtimeDay += overtime;
               }
             }
             cal.add(Calendar.DAY_OF_MONTH, 1);
           }
         }
       } else {
         final int regularTime = TimeFormat.parse(store.getString(PreferenceConstants.UI_OVERTIME_REGULARTIME));
         final Calendar cal = currentDay.getCalendar();
 
         cal.add(Calendar.MONTH, -1);
 
         for (int i = 0; i < 3; i++) {
           final int month = cal.get(Calendar.MONTH) + 1;
           final int year = cal.get(Calendar.YEAR);
 
           final Map<CalendarDay, DayInfoSummary> dayInfos = new HashMap<CalendarDay, DayInfoSummary>();
           for (final DayInfoSummary dayInfo : RepositoryFactory.getRepository().getWorkItemRepository()
               .getDayInfoSummaries(DayInfoFilter.filterMonth(year, month))) {
             dayInfos.put(dayInfo.getDay(), dayInfo);
           }
 
           if (dayInfos != null) {
             for (final CalendarDay day : dayInfos.keySet()) {
               final DayInfoSummary dayInfo = dayInfos.get(day);
 
               if (dayInfo.isHasWorkItems()) {
                 final int overtime = dayInfo.getSumDurations() - regularTime;
 
                 if (day.getMonth() == currentDay.getMonth()) {
                   overtimeMonth += overtime;
                 }
 
                 if (day.getCalendar().get(Calendar.WEEK_OF_YEAR) == currentWeek) {
                   overtimeWeek += overtime;
                 }
 
                 if (currentDay.equals(day)) {
                   overtimeDay += overtime;
                 }
               }
             }
           }
 
           cal.add(Calendar.MONTH, 1);
         }
       }
     } catch (final Exception e) {
       UIPlugin.getDefault().log(e);
     }
 
     return new int[] { overtimeDay, overtimeWeek, overtimeMonth };
   }
 }
