 package net.paissad.waqtsalat.ui.views;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.LinkedHashSet;
 import java.util.Locale;
 import java.util.Set;
 import java.util.TimeZone;
 import java.util.concurrent.Callable;
 
 import net.paissad.eclipse.logger.ILogger;
 import net.paissad.waqtsalat.core.api.Pray;
 import net.paissad.waqtsalat.core.api.PrayName;
 import net.paissad.waqtsalat.locationsprovider.api.City;
 import net.paissad.waqtsalat.locationsprovider.api.Coordinates;
 import net.paissad.waqtsalat.ui.WaqtSalatUIConstants.ICONS;
 import net.paissad.waqtsalat.ui.WaqtSalatUIPlugin;
 import net.paissad.waqtsalat.ui.actions.HidePrayAction;
 import net.paissad.waqtsalat.ui.actions.OpenPreferencesAction;
 import net.paissad.waqtsalat.ui.actions.RefreshAction;
 import net.paissad.waqtsalat.ui.actions.SetAutomaticUpdateAtMidnightAction;
 import net.paissad.waqtsalat.ui.comparators.CityTableViewerComparator;
 import net.paissad.waqtsalat.ui.components.SearchBox;
 import net.paissad.waqtsalat.ui.components.SearchBox.InputPolicyRule;
 import net.paissad.waqtsalat.ui.filters.PrayViewerFilter;
 import net.paissad.waqtsalat.ui.helpers.PrayTimeHelper;
 import net.paissad.waqtsalat.ui.helpers.PreferenceHelper;
 import net.paissad.waqtsalat.ui.prefs.WaqtSalatPreferenceConstants;
 import net.paissad.waqtsalat.ui.prefs.WaqtSalatPreferencePlugin;
 import net.paissad.waqtsalat.ui.providers.CityTableLabelProvider;
 import net.paissad.waqtsalat.ui.util.LuceneUtil;
 import net.paissad.waqtsalat.ui.util.WaqtSalatUIHelper;
 
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.layout.TableColumnLayout;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.DateTime;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.ViewPart;
 
 public class WaqtSalatView extends ViewPart implements IPropertyChangeListener {
 
     // TODO: add a drop-down menu containing pray times.
     // TODO: add a preference setting which propose into which perspectives a button for drop-down should be added)
     // TODO: implement the action which propose to (re)build the Lucene cities index and it to the menu manager.
     // TODO: Add action in the menu manager which proposes the automatic update of database helping for retrieving
     // locations automatically from the IP address.
 
     public static final String                VIEW_ID                     = "net.paissad.waqtsalat.ui.views.WaqtSalatView";                     //$NON-NLS-1$
 
     private static final ILogger              logger                      = WaqtSalatUIPlugin.getPlugin().getLogger();
 
     public static final int                   PRAYS_TABLE_DEFAULT_ROWS    = 5;
 
     private final AdapterFactoryLabelProvider adapterFactoryLabelProvider = new AdapterFactoryLabelProvider(
                                                                                   new ComposedAdapterFactory(
                                                                                           ComposedAdapterFactory.Descriptor.Registry.INSTANCE));
 
     private IAction                           openPreferencesAction;
     private IAction                           setAutomaticUpdateAtMidnightAction;
     private HidePrayAction                    hideSunriseAction;
     private HidePrayAction                    hideSunsetAction;
     private IAction                           refreshAction;
 
     private SearchBox                         searchBox;
 
     private CLabel                            labelSelectedCity;
     private Table                             praysTable;
 
     private DateTime                          dateTime;
 
     private TableViewer                       praysTableViewer;
 
     private Button                            buttonSetCity;
 
     private CLabel                            labelSelectedtimezone;
 
     private Button                            buttonGetAutomaticCity;
 
     private Group                             groupSearchCity;
 
     /** The specified date for which to show the pray times. */
     private Calendar                          currentSpecifiedDate;
 
     private PrayViewerFilter                  prayViewerFilter;
 
     private Composite                         container;
 
     private Composite                         leftSideComposite;
 
     private Composite                         rightSideComposite;
 
    /** The previous selected city. */
    private City                              previousCitySelected;

     /** The current day for which the pray times are computed and displayed. */
     private String                            currentDayID;
 
     public WaqtSalatView() {
     }
 
     @Override
     public void init(IViewSite site) throws PartInitException {
         super.init(site);
         setContentDescription("WaqtSalat View ...");
         WaqtSalatPreferencePlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
         LuceneUtil.initLuceneCitiesIndex();
         updateCurrentDate();
         updatePrayInputs();
         startPrayHooks();
     }
 
     @Override
     public void dispose() {
         WaqtSalatPreferencePlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
         super.dispose();
     }
 
     @Override
     public void createPartControl(Composite parent) {
         container = new Composite(parent, SWT.NONE);
         container.setLayout(new GridLayout(2, false));
         {
             leftSideComposite = new Composite(container, SWT.NONE);
             leftSideComposite.setLayout(new GridLayout(1, false));
             leftSideComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
             {
                 Composite locationComposite = new Composite(leftSideComposite, SWT.NONE);
                 locationComposite.setLayout(new GridLayout(1, false));
                 locationComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
                 {
                     labelSelectedCity = new CLabel(locationComposite, SWT.NONE);
                     labelSelectedCity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
                     updateLabelSelectedCity(PreferenceHelper.getCityFromPreference());
                 }
                 {
                     labelSelectedtimezone = new CLabel(locationComposite, SWT.NONE);
                     labelSelectedtimezone.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
                     updateLabelSelectedTimezone();
                 }
                 {
                     buttonGetAutomaticCity = new Button(locationComposite, SWT.CHECK);
                     buttonGetAutomaticCity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
                     initButtonGetAutomaticCity();
                 }
                 {
                     groupSearchCity = new Group(leftSideComposite, SWT.NONE);
                     groupSearchCity.setLayout(new GridLayout(2, false));
                     groupSearchCity.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
                     initGroupSearchCity();
                 }
             }
 
         }
         {
             rightSideComposite = new Composite(container, SWT.NONE);
             rightSideComposite.setLayout(new GridLayout(1, false));
             rightSideComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
             {
                 dateTime = new DateTime(rightSideComposite, SWT.BORDER | SWT.DROP_DOWN | SWT.LONG);
                 dateTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
                 initDateTimeComponent();
             }
             initPraysTableViewer(rightSideComposite);
         }
 
         hookComponentsEnabled();
 
         getSite().setSelectionProvider(searchBox.getTableViewer());
 
         createActions();
         contributeToActionBars();
 
         updatePrayInputs();
     }
 
     private void initPraysTableViewer(final Composite parent) {
 
         Composite praysTableContainer = new Composite(parent, SWT.NONE);
         praysTableContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 
         praysTableViewer = new TableViewer(praysTableContainer, SWT.BORDER);
         praysTable = praysTableViewer.getTable();
         praysTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
 
         praysTable.setLinesVisible(true);
         praysTable.setHeaderVisible(true);
 
         TableViewerColumn tableViewerColumnPrayName = new TableViewerColumn(praysTableViewer, SWT.NONE);
         TableColumn columnName = tableViewerColumnPrayName.getColumn();
         columnName.setText("Name");
 
         TableViewerColumn tableViewerColumnPrayTime = new TableViewerColumn(praysTableViewer, SWT.NONE);
         TableColumn columnTime = tableViewerColumnPrayTime.getColumn();
         columnTime.setText("Time");
 
         TableColumnLayout layout = new TableColumnLayout();
         praysTableContainer.setLayout(layout);
 
         // Resize the columns to fit the contents
         columnName.pack();
         columnTime.pack();
 
         // Use the packed widths as the minimum widths
         int columnNameWidth = columnName.getWidth();
         int columnTimeWidth = columnTime.getWidth();
 
         // Set name column to fill 50% and time column to fit the remaining, but with their packed widths as
         // minimums
         layout.setColumnData(columnName, new ColumnWeightData(50, columnNameWidth));
         layout.setColumnData(columnTime, new ColumnWeightData(40, columnTimeWidth));
 
         praysTableViewer.setContentProvider(ArrayContentProvider.getInstance());
         praysTableViewer.setLabelProvider(adapterFactoryLabelProvider);
 
         triggerUpdatePrayTableFilters();
     }
 
     private void triggerUpdatePrayTableFilters() {
         if (prayViewerFilter == null) {
             prayViewerFilter = new PrayViewerFilter();
         }
         prayViewerFilter.setShowSunrise(!PreferenceHelper.getHideSunrise());
         prayViewerFilter.setShowSunset(!PreferenceHelper.getHideSunset());
 
         // add the filter into the table.
         praysTableViewer.addFilter(prayViewerFilter);
     }
 
     private void hookComponentsEnabled() {
         searchBox.setEnabled(!buttonGetAutomaticCity.getSelection());
         buttonSetCity.setEnabled(!buttonGetAutomaticCity.getSelection());
     }
 
     private void initButtonGetAutomaticCity() {
         buttonGetAutomaticCity.setText("Get the location automatically (Need internet connection !)");
         boolean prefValue = PreferenceHelper.getPrefStore().getBoolean(
                 WaqtSalatPreferenceConstants.P_GET_LOCATION_FROM_IP_ADDRESS);
         buttonGetAutomaticCity.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 boolean selection = buttonGetAutomaticCity.getSelection();
                 PreferenceHelper.getPrefStore().setValue(WaqtSalatPreferenceConstants.P_GET_LOCATION_FROM_IP_ADDRESS,
                         selection);
                 try {
                     PreferenceHelper.getPrefStore().save();
                     hookComponentsEnabled();
                 } catch (IOException ioe) {
                     logger.error(
                             "Error while saving preference value for '" //$NON-NLS-1$
                                     + WaqtSalatPreferenceConstants.P_GET_LOCATION_FROM_IP_ADDRESS
                                     + "' : " + ioe.getMessage(), ioe); //$NON-NLS-1$
                 }
             }
         });
         buttonGetAutomaticCity.setSelection(prefValue);
     }
 
     /**
      * @param city - may be <code>null</code>.
      */
     private void updateLabelSelectedCity(final City city) {
         labelSelectedCity.setText(buildLabelForCity(city));
         labelSelectedCity.setToolTipText("Represents the current city for which the pray times will be provided.");
         final String countryCode = city == null ? "-" : city.getCountry().getCode(); //$NON-NLS-1$
         labelSelectedCity.setImage(WaqtSalatUIHelper.getFlagForCountryCode(countryCode));
     }
 
     private void updateLabelSelectedTimezone() {
         TimeZone tz = PreferenceHelper.getTimezoneFromPreference();
         // FIXME: when showing tzInfo, the result should not be the timezone of the current system. ...
         String tzInfo = new SimpleDateFormat("z Z", Locale.ENGLISH).format(currentSpecifiedDate.getTime()); //$NON-NLS-1$
         labelSelectedtimezone.setText(tz.getID() + " - (" + tzInfo + ")"); //$NON-NLS-1$ //$NON-NLS-2$
         labelSelectedtimezone.setImage(WaqtSalatUIPlugin.getImageRegistry().get(ICONS.KEY.TIMEZONE));
         labelSelectedtimezone
                 .setToolTipText("Represents the current timezone for which the pray times calculation is based onto.");
     }
 
     private void initGroupSearchCity() {
         groupSearchCity.setText("Search a city");
         groupSearchCity
                 .setToolTipText("Search a city from the database and pick the desired one for which it is necessary to compute and display the pray times.");
         this.initSearchBox(groupSearchCity);
         this.initButtonSetCity(groupSearchCity);
     }
 
     private void initSearchBox(Composite parent) {
         searchBox = new SearchBox(parent, SWT.NONE);
         searchBox.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1));
         initTableColumns(searchBox.getTableViewer());
         searchBox.getTableViewer().setLabelProvider(new CityTableLabelProvider());
         searchBox.getTableViewer().setContentProvider(ArrayContentProvider.getInstance());
         searchBox.setInputPolicyRule(new InputPolicyRuleImpl(searchBox));
         searchBox.getTableViewer().setComparator(new CityTableViewerComparator());
         GridData gd = (GridData) searchBox.getTableViewer().getTable().getLayoutData();
         gd.minimumHeight = searchBox.getTableViewer().getTable().getItemHeight() * 7;
     }
 
     private void initButtonSetCity(final Composite parent) {
         buttonSetCity = new Button(parent, SWT.NONE);
         buttonSetCity.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
         buttonSetCity.setText("Select");
         buttonSetCity.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 ISelection searchSelection = searchBox.getTableViewer().getSelection();
                 if (searchSelection instanceof IStructuredSelection) {
                     Object selectedElement = ((IStructuredSelection) searchSelection).getFirstElement();
                     if (selectedElement instanceof City) {
                         City selectedCity = (City) selectedElement;
                         updateLabelSelectedCity(selectedCity);
                         PreferenceHelper.saveCityPreference(selectedCity);
                     }
                 }
             }
         });
     }
 
     /**
      * @param city - may be <code>null</code>.
      * @return The label to show.
      */
     private String buildLabelForCity(City city) {
         if (city == null) {
             return "<no city selected yet>";
         } else {
             StringBuilder sb = new StringBuilder().append(city.getName()).append(", ")
                     .append(city.getCountry().getName());
             String postalCode = city.getPostalCode();
             if (postalCode != null && !postalCode.trim().isEmpty()) {
                 sb.append(" (").append(postalCode).append(")");
             }
             String region = city.getRegion();
             if (region != null && !region.trim().isEmpty()) {
                 sb.append(" - ").append(region);
             }
             return sb.toString();
         }
     }
 
     private void initDateTimeComponent() {
         dateTime.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 int year = dateTime.getYear();
                 int month = dateTime.getMonth();
                 int day = dateTime.getDay();
                 currentSpecifiedDate.set(year, month, day);
                 updatePrayInputs();
             }
         });
     }
 
     private void initTableColumns(TableViewer tableViewer) {
         String[] columnNames = new String[] { "Country", "City", "Region", "Postal Code" };
         int[] columnWidth = new int[] { 150, 250, 100, 100 };
         for (int i = 0; i < columnNames.length; i++) {
             TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.None);
             TableColumn tableColumn = tableViewerColumn.getColumn();
             tableColumn.setText(columnNames[i]);
             tableColumn.setWidth(columnWidth[i]);
             tableColumn.setMoveable(true);
             tableColumn.setResizable(true);
         }
     }
 
     /**
      * Fills the action Bars
      */
     private void contributeToActionBars() {
         IActionBars bars = getViewSite().getActionBars();
         fillLocalPullDown(bars.getMenuManager());
         fillLocalToolBar(bars.getToolBarManager());
     }
 
     private void fillLocalPullDown(IMenuManager menuManager) {
         menuManager.add(setAutomaticUpdateAtMidnightAction);
         menuManager.add(hideSunriseAction);
         menuManager.add(hideSunsetAction);
         menuManager.add(new Separator());
         menuManager.add(refreshAction);
         menuManager.add(openPreferencesAction);
     }
 
     /**
      * Initialize the toolbar.
      */
     private void fillLocalToolBar(IToolBarManager toolbarManager) {
         toolbarManager.add(setAutomaticUpdateAtMidnightAction);
         toolbarManager.add(hideSunriseAction);
         toolbarManager.add(hideSunsetAction);
         toolbarManager.add(new Separator());
         toolbarManager.add(refreshAction);
         toolbarManager.add(openPreferencesAction);
     }
 
     /**
      * Create the actions.
      */
     private void createActions() {
 
         // Open Preferences Action -----------------------------------------------------------------
         this.openPreferencesAction = new OpenPreferencesAction();
         this.openPreferencesAction.setText("Open Preferences");
         this.openPreferencesAction.setToolTipText("Open WaqtSalat Preferences Page.");
         this.openPreferencesAction.setImageDescriptor(getImageDescriptor(ICONS.PATH.PREFS));
 
         // Automatic Update At Midnight Action -----------------------------------------------------------------
         this.setAutomaticUpdateAtMidnightAction = new SetAutomaticUpdateAtMidnightAction(new Callable<Void>() {
             @Override
             public Void call() throws Exception {
                 PreferenceHelper.getPrefStore().setValue(WaqtSalatPreferenceConstants.P_AUTOMATIC_UPDATE_AT_MIDNIGHT,
                         setAutomaticUpdateAtMidnightAction.isChecked());
                 PreferenceHelper.getPrefStore().save();
                 return null;
             }
         }, "Automatic Update At Midnight", IAction.AS_CHECK_BOX);
         this.setAutomaticUpdateAtMidnightAction.setImageDescriptor(getImageDescriptor(ICONS.PATH.LOOP));
         this.setAutomaticUpdateAtMidnightAction
                 .setToolTipText("Whether or not the pray times displayed into the table viewer should be re-computed and re-displayed when the day change (at midnight).");
         this.setAutomaticUpdateAtMidnightAction.setChecked(PreferenceHelper.getAutomaticUpdateAtMidnight());
 
         // Hide Sunrise Action -----------------------------------------------------------------
         this.hideSunriseAction = new HidePrayAction(PrayName.SUNRISE, "Hide Sunrise", IAction.AS_CHECK_BOX,
                 getImageDescriptor(ICONS.PATH.SUNRISE));
         this.hideSunriseAction.setCallable(new Callable<Void>() {
 
             @Override
             public Void call() throws Exception {
                 PreferenceHelper.getPrefStore().setValue(WaqtSalatPreferenceConstants.P_HIDE_SUNRISE,
                         hideSunriseAction.isChecked());
                 PreferenceHelper.getPrefStore().save();
                 return null;
             }
         });
         this.hideSunriseAction.setChecked(PreferenceHelper.getHideSunrise());
 
         // Hide Sunset Action -----------------------------------------------------------------
         this.hideSunsetAction = new HidePrayAction(PrayName.SUNSET, "Hide Sunset", IAction.AS_CHECK_BOX,
                 getImageDescriptor(ICONS.PATH.SUNSET));
         this.hideSunsetAction.setCallable(new Callable<Void>() {
 
             @Override
             public Void call() throws Exception {
                 PreferenceHelper.getPrefStore().setValue(WaqtSalatPreferenceConstants.P_HIDE_SUNSET,
                         hideSunsetAction.isChecked());
                 PreferenceHelper.getPrefStore().save();
                 return null;
             }
         });
         this.hideSunsetAction.setChecked(PreferenceHelper.getHideSunset());
 
         // Refresh Action -----------------------------------------------------------------
         this.refreshAction = new RefreshAction("Refresh", getImageDescriptor(ICONS.PATH.REFRESH), leftSideComposite);
         this.refreshAction.setToolTipText("Refresh the view and update widgets layouts.");
     }
 
     private ImageDescriptor getImageDescriptor(String imagePath) {
         return WaqtSalatUIPlugin.getImageDescriptor(imagePath);
     }
 
     @Override
     public void setFocus() {
         if (labelSelectedCity != null) {
             labelSelectedCity.setFocus();
         }
     }
 
     private static class InputPolicyRuleImpl implements InputPolicyRule {
 
         private final SearchBox box;
 
         public InputPolicyRuleImpl(SearchBox searchBox) {
             this.box = searchBox;
         }
 
         @Override
         public Object getInput() {
             String enteredText = box.getCurrentSearchText();
             if (enteredText != null) {
                 enteredText = enteredText.replaceAll("^[\\*\\?]", ""); //$NON-NLS-1$
                 if (enteredText.trim().length() >= 2) {
                     if (!enteredText.endsWith("*")) enteredText += "*"; //$NON-NLS-1$ //$NON-NLS-2$
                     try {
                         final Set<City> cities = new LinkedHashSet<City>(LuceneUtil.searchByCityName(enteredText));
                         cities.addAll(LuceneUtil.searchByCountryName(enteredText));
                         cities.addAll(LuceneUtil.searchByPostalCode(enteredText));
                         cities.addAll(LuceneUtil.searchByRegion(enteredText));
                         return cities;
 
                     } catch (Exception e) {
                         logger.error(
                                 "Error search city by using query --> " + enteredText + " <-- : " + e.getMessage(), e); //$NON-NLS-1$ //$NON-NLS-2$
                     }
                 }
             }
             return null;
         }
     }
 
     @Override
     public void propertyChange(PropertyChangeEvent event) {
         updateCurrentDate(); // IMPORTANT : this method must be called first (the date must be updated first) !!!
         updateLabelSelectedTimezone();
         updatePrayInputs();
         triggerUpdatePrayTableFilters();
         if (praysTableViewer != null) {
             praysTableViewer.refresh();
         }
     }
 
     private synchronized void updatePrayInputs() {
         City city = PreferenceHelper.getCityFromPreference();
         if (city != null && currentSpecifiedDate != null && praysTableViewer != null) {
 
             int dayOfYear = currentSpecifiedDate.get(Calendar.DAY_OF_YEAR);
             int year = currentSpecifiedDate.get(Calendar.YEAR);
 
             String dayId = "" + year + "-" + dayOfYear;
 
            boolean cityChanged = !city.equals(previousCitySelected);
            boolean dayChanged = !dayId.equals(currentDayID);

            if (cityChanged || dayChanged) {
                 Coordinates coordinates = city.getCoordinates();
                 Collection<Pray> prays = PrayTimeHelper.computePrayTimes(currentSpecifiedDate, coordinates,
                         PreferenceHelper.getPrayConfig());
                 praysTableViewer.setInput(prays);
                 praysTableViewer.refresh();
                 currentDayID = dayId;
                previousCitySelected = city;
             }
         }
     }
 
     /**
      * Initializes the current date for which to show pray time or update its timezone if ever the preference timezone
      * value changes.
      */
     private void updateCurrentDate() {
         if (currentSpecifiedDate == null) {
             currentSpecifiedDate = Calendar.getInstance();
         }
         currentSpecifiedDate.setTimeZone(PreferenceHelper.getTimezoneFromPreference());
     }
 
     private void startPrayHooks() {
         Thread updateThread = new Thread() {
             @Override
             public void run() {
                 while (true) {
                     getViewSite().getShell().getDisplay().syncExec(new Runnable() {
                         @Override
                         public void run() {
 
                             int dayOfYear = currentSpecifiedDate.get(Calendar.DAY_OF_YEAR);
 
                             if (PreferenceHelper.getAutomaticUpdateAtMidnight()) {
                                 Calendar cal = Calendar.getInstance(PreferenceHelper.getTimezoneFromPreference());
                                 if (dayOfYear != cal.get(Calendar.DAY_OF_YEAR)) {
                                     currentSpecifiedDate.setTime(cal.getTime());
                                 }
                             }
 
                             updatePrayInputs();
                             praysTableViewer.refresh(); // refresh anyway (to update at least the label provider)
                         }
                     });
                     try {
                         Thread.sleep(5000L);
                     } catch (InterruptedException e) {
                     }
                 }
             };
         };
         updateThread.setDaemon(true);
         updateThread.start();
     }
 }
