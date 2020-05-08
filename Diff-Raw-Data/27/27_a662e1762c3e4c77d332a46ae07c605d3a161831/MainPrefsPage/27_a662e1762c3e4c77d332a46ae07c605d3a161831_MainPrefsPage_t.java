 package net.paissad.waqtsalat.ui.prefs.pages;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.TimeZone;
 
 import net.paissad.waqtsalat.core.WaqtSalatPackage;
 import net.paissad.waqtsalat.core.api.CalculationMethod;
 import net.paissad.waqtsalat.core.util.WaqtSalatUtil;
 import net.paissad.waqtsalat.locationsprovider.LocationsProviderPlugin;
 import net.paissad.waqtsalat.locationsprovider.LocationsProviderPlugin.LocationsProviderExtension;
 import net.paissad.waqtsalat.ui.prefs.WaqtSalatPreferenceConstants;
 import net.paissad.waqtsalat.ui.prefs.WaqtSalatPreferencePlugin;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EEnumLiteral;
 import org.eclipse.jface.preference.BooleanFieldEditor;
 import org.eclipse.jface.preference.ComboFieldEditor;
 import org.eclipse.jface.preference.FieldEditor;
 import org.eclipse.jface.preference.FieldEditorPreferencePage;
 import org.eclipse.jface.preference.RadioGroupFieldEditor;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 
 public class MainPrefsPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
 
     public static final String PAGE_ID = "net.paissad.waqtsalat.ui.prefs.MainPrefsPage"; //$NON-NLS-1$
 
     private BooleanFieldEditor timezoneFromCountryEditor;
     private BooleanFieldEditor useSystemTimezoneEditor;
     private ComboFieldEditor   timezonesEditor;
     private Group              timezonePrefsGroup;
 
     public MainPrefsPage() {
         super(GRID);
     }
 
     @Override
     public void init(IWorkbench workbench) {
         setPreferenceStore(WaqtSalatPreferencePlugin.getDefault().getPreferenceStore());
         setDescription("WaqtSalat preferences page configuration.");
     }
 
     /**
      * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to manipulate various
      * types of preferences. Each field editor knows how to save and restore itself.
      */
     @Override
     public void createFieldEditors() {
         this.addLocationsProviderPrefsEditor();
         this.addCalculationMethodsPrefsEditor();
         this.addJuristicMethodsPrefsEditor();
         this.addTimezonePrefsEditors();
         this.hookAllListeners();
     }
 
     /**
      * Configuration of Locations Provider.
      */
     private void addLocationsProviderPrefsEditor() {
         ComboFieldEditor editor = new ComboFieldEditor(WaqtSalatPreferenceConstants.P_LOCATIONS_PROVIDER,
                 "Locations Providers", getLocationsProviders(), getFieldEditorParent());
         editor.getLabelControl(getFieldEditorParent()).setToolTipText(
                 "Set the locations provider to use in order to retrieve the list of cities, coordinates and so forth.");
         addField(editor);
     }
 
     /**
      * Configuration of Calculation method.
      */
     private void addCalculationMethodsPrefsEditor() {
         ComboFieldEditor editor = new ComboFieldEditor(WaqtSalatPreferenceConstants.P_CALCULATION_METHOD,
                 "Calculation method", this.getCalculationMethods(), getFieldEditorParent());
         editor.getLabelControl(getFieldEditorParent())
                 .setToolTipText(
                         "This represent the calculation method used in order to retrieve the most accurately possible the pray times for the specified location.");
         addField(editor);
     }
 
     /**
      * Configuration of Juristic Method.
      */
     private void addJuristicMethodsPrefsEditor() {
         RadioGroupFieldEditor editor = new RadioGroupFieldEditor(WaqtSalatPreferenceConstants.P_JURISTIC_METHOD,
                 "Madhab", 1, getJuristicMethods(), getFieldEditorParent(), true);
         editor.getRadioBoxControl(getFieldEditorParent()).setToolTipText("Set the juristic method to use.");
         addField(editor);
     }
 
     /**
      * Configuration of timezone.
      */
     private void addTimezonePrefsEditors() {
         timezonePrefsGroup = new Group(getFieldEditorParent(), SWT.NONE);
        timezonePrefsGroup.setLayout(new GridLayout(1, false));
        timezonePrefsGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
         timezonePrefsGroup.setText("Timezone settings");
 
         timezoneFromCountryEditor = new BooleanFieldEditor(WaqtSalatPreferenceConstants.P_GET_TIMEZONE_FROM_COUNTRY,
                 "Try to get timezone from country/city", timezonePrefsGroup);
// FIXME: fix tooltip display issues in windows XP        
//        timezoneFromCountryEditor
//                .getLabelControl(timezonePrefsGroup)
//                .setToolTipText(
//                        "Be aware that there may exist several timezones for the same country. In that case, it is highly recommended to set explicitely the timezone or even use system default timezone.");
 
         useSystemTimezoneEditor = new BooleanFieldEditor(WaqtSalatPreferenceConstants.P_USE_SYSTEM_TIMEZONE,
                 "Use system timezone", timezonePrefsGroup);
 
         timezonesEditor = new ComboFieldEditor(WaqtSalatPreferenceConstants.P_TIMEZONE, "Timezone",
                 getAvailableTimezones(), timezonePrefsGroup);
 
         BooleanFieldEditor daylightSavingsEditor = new BooleanFieldEditor(
                 WaqtSalatPreferenceConstants.P_DAYLIGHT_SAVINGS, "Daylight savings", timezonePrefsGroup);
 
         for (FieldEditor editor : new FieldEditor[] { timezoneFromCountryEditor, useSystemTimezoneEditor,
                 timezonesEditor, daylightSavingsEditor }) {
             editor.setPreferenceStore(getPreferenceStore());
             editor.load();
             addField(editor);
         }
     }
 
     private String[][] getLocationsProviders() {
         final Map<String, LocationsProviderExtension> locationsProviderManager = LocationsProviderPlugin
                 .getLocationsProviderManager();
         final String[][] result = new String[locationsProviderManager.size()][2];
         int i = 0;
         for (Iterator<String> iterator = locationsProviderManager.keySet().iterator(); iterator.hasNext();) {
             String providerID = iterator.next();
             LocationsProviderExtension providerExtension = locationsProviderManager.get(providerID);
             result[i][0] = providerExtension.getName();
             result[i][1] = providerExtension.getId(); // as the same as providerID
             i++;
         }
         return result;
     }
 
     private String[][] getCalculationMethods() {
         final CalculationMethod[] calculationMethods = CalculationMethod.values();
         final String[][] result = new String[calculationMethods.length][2];
         int i = 0;
         for (CalculationMethod calcMethod : calculationMethods) {
             result[i][0] = calcMethod.getLiteral() + "  (" + CalculationMethod.getDescription(calcMethod) + ")";
             result[i][1] = calcMethod.getLiteral();
             i++;
         }
         return result;
     }
 
     private String[][] getJuristicMethods() {
         EList<EEnumLiteral> eLiterals = WaqtSalatPackage.Literals.JURISTIC_METHOD.getELiterals();
         final String[][] result = new String[eLiterals.size()][2];
         int i = 0;
         for (Iterator<EEnumLiteral> iterator = eLiterals.iterator(); iterator.hasNext();) {
             EEnumLiteral eEnumLiteral = (EEnumLiteral) iterator.next();
             result[i][0] = eEnumLiteral.getLiteral();
             result[i][1] = eEnumLiteral.getLiteral();
             i++;
         }
         return result;
     }
 
     private String[][] getAvailableTimezones() {
         Collection<TimeZone> availableTimezones = WaqtSalatUtil.getAvailableTimezones();
         String[][] result = new String[availableTimezones.size()][2];
         int i = 0;
         for (Iterator<TimeZone> iterator = availableTimezones.iterator(); iterator.hasNext();) {
             TimeZone timeZone = (TimeZone) iterator.next();
             String tzID = timeZone.getID();
             result[i][0] = tzID;
             result[i][1] = tzID;
             i++;
         }
         return result;
     }
 
     @Override
     public void propertyChange(PropertyChangeEvent event) {
         super.propertyChange(event);
         this.hookAllListeners();
     }
 
     private void hookAllListeners() {
         this.hookTimezoneListeners();
     }
 
     private void hookTimezoneListeners() {
         boolean useSystemTimezone = useSystemTimezoneEditor.getBooleanValue();
         timezoneFromCountryEditor.setEnabled(!useSystemTimezone, timezonePrefsGroup);
 
         boolean getTimezoneFromCountry = timezoneFromCountryEditor.getBooleanValue();
         useSystemTimezoneEditor.setEnabled(!getTimezoneFromCountry, timezonePrefsGroup);
 
         timezonesEditor.setEnabled(!(getTimezoneFromCountry || useSystemTimezone), timezonePrefsGroup);
     }
 
     @Override
     protected void performDefaults() {
         super.performDefaults();
         this.hookAllListeners();
     }
 
 }
