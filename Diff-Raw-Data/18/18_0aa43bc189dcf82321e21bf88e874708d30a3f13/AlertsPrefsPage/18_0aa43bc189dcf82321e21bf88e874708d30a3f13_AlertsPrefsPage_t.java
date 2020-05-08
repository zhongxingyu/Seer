 package net.paissad.waqtsalat.ui.prefs.pages;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import net.paissad.eclipse.logger.ILogger;
 import net.paissad.waqtsalat.core.api.PrayName;
 import net.paissad.waqtsalat.ui.prefs.WaqtSalatPreferenceConstants;
 import net.paissad.waqtsalat.ui.prefs.WaqtSalatPreferencePlugin;
 import net.paissad.waqtsalat.ui.prefs.editors.EnhancedBooleanFieldEditor;
 import net.paissad.waqtsalat.ui.prefs.editors.EnhancedRadioGroupFieldEditor;
 import net.paissad.waqtsalat.ui.prefs.util.SWTUtil;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.jface.preference.BooleanFieldEditor;
 import org.eclipse.jface.preference.FieldEditorPreferencePage;
 import org.eclipse.jface.preference.FileFieldEditor;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 import org.osgi.framework.Bundle;
 
 public class AlertsPrefsPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
 
     public static final String                     PAGE_ID              = "net.paissad.waqtsalat.ui.prefs.AlertsPrefsPage"; //$NON-NLS-1$
 
     private static final ILogger                   logger               = WaqtSalatPreferencePlugin.getDefault()
                                                                                 .getLogger();
 
     private final String                           customAdhanLabel     = "Custom adhan";
     private Group                                  soundsGroup;
     private EnhancedRadioGroupFieldEditor          adhanModeEditor;
 
     private TabFolder                              tabFolder;
 
     private Composite                              soundsComposite;
 
     private Composite                              notificationsComposite;
 
     private Group                                  notificationsGroup;
 
     private BooleanFieldEditor                     enableNotificationsEditor;
 
     private Collection<EnhancedBooleanFieldEditor> notificationsEditors = new ArrayList<EnhancedBooleanFieldEditor>();
 
     private Composite                              buttonsComposite;
 
     public AlertsPrefsPage() {
         super(GRID);
     }
 
     @Override
     public void init(IWorkbench workbench) {
         setPreferenceStore(WaqtSalatPreferencePlugin.getDefault().getPreferenceStore());
         setDescription("Set the sounds and notifications for pray times.");
     }
 
     @Override
     protected void createFieldEditors() {
 
         tabFolder = new TabFolder(getFieldEditorParent(), SWT.BORDER);
         tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 
         this.initSoundsPrefsTab();
 
         this.initNotificationsPrefsTab();
 
         this.hookAllListeners();
     }
 
     private void initSoundsPrefsTab() {
         TabItem soundsTabbedItem = new TabItem(tabFolder, SWT.NONE);
         soundsTabbedItem.setText("Sounds");
 
         soundsComposite = new Composite(tabFolder, SWT.NONE);
         soundsTabbedItem.setControl(soundsComposite);
         soundsComposite.setLayout(new GridLayout(1, false));
 
         this.addSilentOrDefaultAdhanEditor();
         this.addSoundsEditors();
     }
 
     private void initNotificationsPrefsTab() {
         TabItem notificationsTabbedItem = new TabItem(tabFolder, SWT.NONE);
         notificationsTabbedItem.setText("Notifications");
 
         notificationsComposite = new Composite(tabFolder, SWT.NONE);
         notificationsTabbedItem.setControl(notificationsComposite);
         notificationsComposite.setLayout(new GridLayout(1, false));
 
         this.addEnableNotificationsEditor();
         this.addNotificationsEditors();
 
         this.addSelectionButtons();
 
         CLabel explanationsLabel = new CLabel(notificationsComposite, SWT.NONE);
         ImageRegistry reg = WaqtSalatPreferencePlugin.getDefault().getImageRegistry();
         explanationsLabel.setImage(reg.get(WaqtSalatPreferenceConstants.IconsKeys.NOTIFICATION_1));
         explanationsLabel
                 .setText("Notifications will alert you when it is time to pray.\nNotification will fade automatically. A small window will be shown at the corner of the screen.");
     }
 
     private void addSelectionButtons() {
         buttonsComposite = new Composite(notificationsComposite, SWT.NONE);
         buttonsComposite.setLayout(new GridLayout(2, false));
         Button selectAllButton = new Button(buttonsComposite, SWT.PUSH);
         selectAllButton.setText("Select all");
         selectAllButton.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 for (EnhancedBooleanFieldEditor editor : notificationsEditors) {
                     Button checkBox = editor.getCheckBox(notificationsGroup);
                     checkBox.setSelection(true);
                 }
             }
         });
 
         Button deselectAllButton = new Button(buttonsComposite, SWT.PUSH);
         deselectAllButton.setText("Deselect all");
         deselectAllButton.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 for (EnhancedBooleanFieldEditor editor : notificationsEditors) {
                     Button checkBox = editor.getCheckBox(notificationsGroup);
                     checkBox.setSelection(false);
                 }
             }
         });
     }
 
     private void addEnableNotificationsEditor() {
         enableNotificationsEditor = new BooleanFieldEditor(WaqtSalatPreferenceConstants.P_ENABLE_NOTIFICATIONS,
                 "Enable notifications", notificationsComposite);
         enableNotificationsEditor.setPreferenceStore(getPreferenceStore());
         enableNotificationsEditor.load();
         addField(enableNotificationsEditor);
     }
 
     private void addNotificationsEditors() {
         notificationsGroup = new Group(notificationsComposite, SWT.NONE);
         notificationsGroup.setLayout(new FillLayout(SWT.HORIZONTAL));
         notificationsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
         for (final PrayName prayName : PrayName.values()) {
             if (PrayName.SUNSET.equals(prayName) || PrayName.SUNRISE.equals(prayName)) continue;
             EnhancedBooleanFieldEditor editor = new EnhancedBooleanFieldEditor(WaqtSalatPreferenceConstants
                     .getNotificationsPrefConstant(prayName).getKey(), prayName.getLiteral(), notificationsGroup);
             editor.setPreferenceStore(getPreferenceStore());
             editor.load();
             addField(editor);
             this.notificationsEditors.add(editor);
         }
     }
 
     private void addSilentOrDefaultAdhanEditor() {
         adhanModeEditor = new EnhancedRadioGroupFieldEditor(WaqtSalatPreferenceConstants.P_SOUND_MODE,
                 "Adhan configuration", 1, this.getAdhanModes(), soundsComposite);
         adhanModeEditor.setPreferenceStore(getPreferenceStore());
         adhanModeEditor.load();
         addField(adhanModeEditor);
     }
 
     private String[][] getAdhanModes() {
         return new String[][] { { "None", WaqtSalatPreferenceConstants.AdhanValues.NO_SOUND },
                 { "Default adhan", WaqtSalatPreferenceConstants.AdhanValues.DEFAULT_ADHAN },
                 { customAdhanLabel, WaqtSalatPreferenceConstants.AdhanValues.CUSTOM_ADHAN } };
     }
 
     private void addSoundsEditors() {
         soundsGroup = new Group(soundsComposite, SWT.None);
         soundsGroup.setLayout(new FillLayout(SWT.HORIZONTAL));
         soundsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
         soundsGroup.setText(customAdhanLabel);
         Bundle bundle = WaqtSalatPreferencePlugin.getDefault().getBundle();
         URL soundsEntryURL = bundle.getEntry("/sounds"); //$NON-NLS-1$
         try {
             soundsEntryURL = FileLocator.resolve(soundsEntryURL);
         } catch (IOException e) {
             logger.error(
                     "Error while retrieving the initial path to use for FielEditorDialog for choosing adhans : " + e.getMessage(), e); //$NON-NLS-1$
         }
         File initialPath = null;
         try {
             if (soundsEntryURL != null) {
                 initialPath = new File(soundsEntryURL.toURI());
             }
         } catch (URISyntaxException e) {
             logger.error(
                     "Error while retrieving the initial path to use for FielEditorDialog for choosing adhans : " + e.getMessage(), e); //$NON-NLS-1$
         }
         final String[] extensions = { "*.mp3;*.wav", "*.*" }; //$NON-NLS-1$ //$NON-NLS-2$
         for (final PrayName prayName : PrayName.values()) {
             if (PrayName.SUNRISE.equals(prayName) || PrayName.SUNSET.equals(prayName)) {
                 continue;
             }
             String prefName = WaqtSalatPreferenceConstants.getSoundPrefConstantForPrayname(prayName).getKey();
             FileFieldEditor editor = new FileFieldEditor(prefName, prayName.getLiteral(), true, soundsGroup);
             editor.setEmptyStringAllowed(false);
             editor.setFileExtensions(extensions);
             if (initialPath != null) editor.setFilterPath(initialPath);
             editor.setPreferenceStore(getPreferenceStore());
             editor.load();
             addField(editor);
         }
     }
 
     @Override
     public void propertyChange(PropertyChangeEvent event) {
         super.propertyChange(event);
         this.hookAllListeners();
     }
 
     @Override
     protected void performDefaults() {
         super.performDefaults();
         this.hookAllListeners();
     }
 
     private void hookAllListeners() {
         Button customAdhanButton = adhanModeEditor.getButton(customAdhanLabel, soundsComposite);
         boolean customSelected = customAdhanButton.getSelection();
         SWTUtil.setEnabledRecursively(soundsGroup, customSelected);
         boolean notificationsEnabled = enableNotificationsEditor.getBooleanValue();
         SWTUtil.setEnabledRecursively(notificationsGroup, notificationsEnabled);
         SWTUtil.setEnabledRecursively(buttonsComposite, notificationsEnabled);
     }

    @Override
    public boolean isValid() {
        Button customAdhanButton = adhanModeEditor.getButton(customAdhanLabel, soundsComposite);
        boolean customSelected = customAdhanButton.getSelection();
        if (!customSelected) {
            setErrorMessage(null);
        }
        return !customSelected || super.isValid();
    }
 }
