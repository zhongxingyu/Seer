 /*
  * Copyright (c) 2006-2015 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_swing.dialogs.prefs;
 
 import com.dmdirc.addons.ui_swing.Apple;
 import com.dmdirc.addons.ui_swing.PrefsComponentFactory;
 import com.dmdirc.addons.ui_swing.UIUtilities;
 import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;
 import com.dmdirc.addons.ui_swing.components.text.TextLabel;
 import com.dmdirc.config.prefs.PreferencesCategory;
 import com.dmdirc.config.prefs.PreferencesSetting;
 import com.dmdirc.config.prefs.PreferencesType;
import com.dmdirc.util.LogUtils;
 
 import java.awt.Component;
 import java.awt.MenuContainer;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.BorderFactory;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 
 import net.miginfocom.layout.PlatformDefaults;
 import net.miginfocom.swing.MigLayout;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Loads a preferences panel for a specified preferences category in the background.
  */
 public class PrefsCategoryLoader extends LoggingSwingWorker<JPanel, Object> {
 
     private static final Logger LOG = LoggerFactory.getLogger(PrefsCategoryLoader.class);
     /** Panel gap. */
     private final int padding = (int) PlatformDefaults.getUnitValueX("related").getValue();
     /** Panel left padding. */
     private final int leftPadding = (int) PlatformDefaults.getPanelInsets(1).getValue();
     /** Panel right padding. */
     private final int rightPadding = (int) PlatformDefaults.getPanelInsets(3).getValue();
     /** Error panel. */
     private JPanel errorCategory;
     /** Category panel. */
     private final CategoryPanel categoryPanel;
     /** Category to display. */
     private final PreferencesCategory category;
     /** Prefs component factory instance. */
     private final PrefsComponentFactory factory;
 
     /**
      * Instantiates a new preferences category loader.
      *
      * @param factory       Prefs component factory instance
      * @param categoryPanel Parent Category panel
      * @param category      Preferences Category to load
      */
     public PrefsCategoryLoader(final PrefsComponentFactory factory,
             final CategoryPanel categoryPanel,
             final PreferencesCategory category) {
         this.factory = factory;
         this.categoryPanel = categoryPanel;
         this.category = category;
 
         UIUtilities.invokeAndWait(() -> {
             errorCategory = new JPanel(new MigLayout("fillx"));
             errorCategory.add(new TextLabel(
                     "There was an error loading this category."));
         });
     }
 
     @Override
     protected JPanel doInBackground() {
         return addCategory(category);
     }
 
     @Override
     protected void done() {
         categoryPanel.categoryLoaded(this, category);
     }
 
     /**
      * Returns the panel for this loader.
      *
      * @return Loaded panel
      */
     public JPanel getPanel() {
         JPanel panel;
         try {
             panel = get();
         } catch (InterruptedException ex) {
             panel = errorCategory;
         } catch (ExecutionException ex) {
            LOG.warn(LogUtils.USER_ERROR, "Error loading prefs panel", ex);
             panel = errorCategory;
         }
         return panel;
     }
 
     /**
      * Initialises the specified category.
      *
      * @since 0.6.3m1
      * @param category The category that is being initialised
      * @param panel    The panel to which we're adding its contents
      */
     private void initCategory(final PreferencesCategory category,
             final JPanel panel) {
 
         if (!category.getDescription().isEmpty()) {
             UIUtilities.invokeAndWait(() -> panel.add(new TextLabel(category.getDescription()),
                     "span, growx, pushx, wrap 2*unrel"));
         }
 
         for (PreferencesCategory child : category.getSubcats()) {
             if (child.isInline() && category.isInlineBefore()) {
                 addInlineCategory(child, panel);
             }
         }
 
         if (category.hasObject()) {
             if (!(category.getObject() instanceof JPanel)) {
                 throw new IllegalArgumentException(
                         "Custom preferences objects" + " for this UI must extend JPanel.");
             }
 
             panel.add((Component) category.getObject(), "growx, pushx");
 
             return;
         }
 
         for (PreferencesSetting setting : category.getSettings()) {
             addComponent(setting, panel);
         }
 
         if (!category.isInlineBefore()) {
             category.getSubcats().stream().filter(PreferencesCategory::isInline)
                     .forEach(child -> addInlineCategory(child, panel));
         }
     }
 
     /**
      * Initialises and adds a component to a panel.
      *
      * @param setting The setting to be used
      * @param panel   The panel to add the component to
      */
     private void addComponent(final PreferencesSetting setting,
             final JPanel panel) {
 
         final TextLabel label = new TextLabel(setting.getTitle() + ": ", false);
 
         final JComponent option = UIUtilities.invokeAndWait(() -> {
             final JComponent option1 = factory.getComponent(setting);
             option1.setToolTipText(null);
             return option1;
         });
 
         categoryPanel.getToolTipPanel().registerTooltipHandler(label,
                 getTooltipText(setting, categoryPanel));
         categoryPanel.getToolTipPanel().registerTooltipHandler(option,
                 getTooltipText(setting, categoryPanel));
 
         if (setting.getType() != PreferencesType.LABEL) {
             if (Apple.isAppleUI()) {
                 panel.add(label, "align right, wmax 40%");
             } else {
                 panel.add(label, "align left, wmax 40%");
             }
         }
         if (option == null) {
             panel.add(new JLabel("Error: See error list."));
         } else if (setting.getType() == PreferencesType.LABEL) {
             panel.add(option, "growx, pushx, w 100%, span");
         } else {
             panel.add(option, "growx, pushx, w 60%");
         }
     }
 
     /**
      * Returns the tooltip text for a preferences setting.
      *
      * @param setting   Setting to get text for
      * @param component Component tooltip applies to
      *
      * @return Tooltip text for the setting
      */
     private String getTooltipText(final PreferencesSetting setting,
             final MenuContainer component) {
         if (setting.isRestartNeeded()) {
             final int size = component.getFont().getSize();
             return "<html>" + setting.getHelptext() + "<br>"
                     + "<img src=\"dmdirc://com/dmdirc/res/restart-needed.png\" " + "width=\"" + size
                     + "\" height=\"" + size + "\">" + "&nbsp;Restart needed if changed</html>";
         }
         return setting.getHelptext();
 
     }
 
     /**
      * Adds a new inline category.
      *
      * @param category The category to be added
      * @param parent   The panel to add the category to
      */
     private void addInlineCategory(final PreferencesCategory category,
             final JPanel parent) {
         final JPanel panel = UIUtilities.invokeAndWait(() -> {
             final JPanel panel1 = new JPanel(new MigLayout(
                             "fillx, gap unrel, wrap 2, "
                             + "hidemode 3, pack, wmax 470-" + leftPadding + '-'
                             + rightPadding + "-2*"
                             + padding));
             panel1.setName(category.getPath());
             panel1.setBorder(BorderFactory.createTitledBorder(UIManager.
                     getBorder("TitledBorder.border"), category.getTitle()));
             return panel1;
         });
 
         parent.add(panel, "span, growx, pushx, wrap");
 
         initCategory(category, panel);
     }
 
     /**
      * Adds the specified category to the preferences dialog.
      *
      * @since 0.6.3m1
      * @param category   The category to be added
      */
     private JPanel addCategory(final PreferencesCategory category) {
         final JPanel panel = UIUtilities.invokeAndWait(() -> {
             final JPanel panel1 = new JPanel(
                     new MigLayout("fillx, gap unrel, wrap 2, pack, "
                             + "hidemode 3, wmax 470-"
                             + leftPadding + '-' + rightPadding + "-2*" + padding));
             panel1.setName(category.getPath());
             return panel1;
         });
 
         initCategory(category, panel);
 
         return panel;
     }
 
 }
