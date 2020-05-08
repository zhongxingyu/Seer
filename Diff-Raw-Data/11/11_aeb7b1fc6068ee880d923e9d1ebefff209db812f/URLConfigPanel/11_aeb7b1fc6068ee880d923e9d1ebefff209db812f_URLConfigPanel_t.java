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
 
 import com.dmdirc.ClientModule.GlobalConfig;
 import com.dmdirc.ClientModule.UserConfig;
 import com.dmdirc.addons.ui_swing.components.GenericTableModel;
 import com.dmdirc.addons.ui_swing.components.IconManager;
 import com.dmdirc.addons.ui_swing.components.PackingTable;
 import com.dmdirc.addons.ui_swing.components.URLProtocolPanel;
 import com.dmdirc.addons.ui_swing.dialogs.StandardInputDialog;
 import com.dmdirc.addons.ui_swing.injection.MainWindow;
 import com.dmdirc.config.prefs.PreferencesInterface;
 import com.dmdirc.config.validators.URLProtocolValidator;
 import com.dmdirc.interfaces.config.AggregateConfigProvider;
 import com.dmdirc.interfaces.config.ConfigProvider;
 
 import java.awt.Dialog.ModalityType;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.stream.Collectors;
 
 import javax.inject.Inject;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * URL Config panel. List all known url protocols and allows them to be configured.
  */
 public class URLConfigPanel extends JPanel implements
         ListSelectionListener, ActionListener, PreferencesInterface {
 
     /** Serial version UID. */
     private static final long serialVersionUID = 1;
     /** The global configuration to read settings from. */
     private final AggregateConfigProvider globalConfig;
     /** The user configuration to store settings in. */
     private final ConfigProvider userConfig;
     /** The icon manager to use for input dialogs. */
     private final IconManager iconManager;
     /** Protocol list. */
     private PackingTable table;
     /** Table mode. */
     private GenericTableModel<URLHandlerHolder> model;
     /** Table scrollpane. */
     private JScrollPane tableScrollPane;
     /** Protocol config panel. */
     private Map<URI, URLProtocolPanel> details;
     /** Empty info panel. */
     private URLProtocolPanel empty;
     /** Current component. */
     private URLProtocolPanel activeComponent;
     /** Add button. */
     private JButton add;
     /** Removed button. */
     private JButton remove;
     /** Selected row. */
     private int selectedRow;
     /** Parent window. */
     private final Window parentWindow;
 
     /**
      * Instantiates a new URL config panel.
      *
      * @param parentWindow Parent window
      * @param globalConfig The global configuration to read settings from.
      * @param userConfig   The user configuration to write settings to.
      * @param iconManager  The icon manager to use for input dialogs.
      */
     @Inject
     public URLConfigPanel(
             @MainWindow final Window parentWindow,
             @GlobalConfig final AggregateConfigProvider globalConfig,
             @UserConfig final ConfigProvider userConfig,
             final IconManager iconManager) {
 
         this.parentWindow = parentWindow;
         this.globalConfig = globalConfig;
         this.userConfig = userConfig;
         this.iconManager = iconManager;
 
         initComponents();
         addListeners();
         layoutComponents();
         selectedRow = -1;
     }
 
     /**
      * Initialises the components.
      */
     private void initComponents() {
         tableScrollPane = new JScrollPane();
         model = new GenericTableModel<>(URLHandlerHolder.class, "getUri", "getHandler");
        model.setHeaderNames("Protocol", "Handler");
         table = new PackingTable(model, tableScrollPane);
         table.setDefaultRenderer(URISchemeCellRenderer.class, new URISchemeCellRenderer());
         table.setDefaultRenderer(URIHandlerCellRenderer.class, new URIHandlerCellRenderer());
         table.setAutoCreateRowSorter(true);
         table.setAutoCreateColumnsFromModel(true);
         table.setColumnSelectionAllowed(false);
         table.setCellSelectionEnabled(false);
         table.setFillsViewportHeight(false);
         table.setRowSelectionAllowed(true);
         table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         table.getRowSorter().toggleSortOrder(0);
         details = new HashMap<>();
         empty = new URLProtocolPanel(globalConfig, userConfig, null, true);
         activeComponent = empty;
         add = new JButton("Add");
         remove = new JButton("Remove");
         remove.setEnabled(false);
 
         tableScrollPane.setViewportView(table);
 
         final Set<String> options = globalConfig.getOptions("protocol").keySet();
 
         for (final String option : options) {
             try {
                 final URI uri = new URI(option + "://example.test.com");
                 model.addValue(new URLHandlerHolder(uri, getURLHandler(uri)));
                 details.put(uri, new URLProtocolPanel(globalConfig, userConfig, uri, true));
             } catch (final URISyntaxException ex) {
                 //Ignore wont happen
             }
         }
     }
 
     private String getURLHandler(final URI uri) {
         if (globalConfig.hasOptionString("protocol", uri.getScheme())) {
             return globalConfig.getOption("protocol", uri.getScheme());
         } else {
             return "";
         }
     }
 
     /**
      * Adds listeners.
      */
     private void addListeners() {
         table.getSelectionModel().addListSelectionListener(this);
         add.addActionListener(this);
         remove.addActionListener(this);
     }
 
     /**
      * Lays out the components.
      */
     private void layoutComponents() {
         removeAll();
         setLayout(new MigLayout("ins 0, wrap 1, nocache"));
 
         add(tableScrollPane, "growx, pushx, h 150!");
         add(add, "split 2, growx, pushx");
         add(remove, "growx, pushx");
         add(activeComponent, "growx, pushx, wmax 100%");
     }
 
     @Override
     public void save() {
         valueChanged(null);
         final Map<URI, String> handlers = model.elements().stream()
                 .collect(Collectors.toMap(URLHandlerHolder::getUri, URLHandlerHolder::getHandler));
         final Set<String> protocols = globalConfig.getOptions("protocol").keySet();
         for (final String protocol : protocols) {
             URI uri;
             try {
                 uri = new URI(protocol + "://example.test.com");
             } catch (final URISyntaxException ex) {
                 uri = null;
             }
             if (uri != null && handlers.containsKey(uri)) {
                 saveHandler(protocol, handlers.get(uri));
             } else {
                 saveHandler(protocol, "");
             }
             handlers.remove(uri);
         }
         for (final Entry<URI, String> entry : handlers.entrySet()) {
             saveHandler(entry.getKey().getScheme(), entry.getValue());
         }
     }
 
     /**
      * Saves or updates a handler to the config.
      *
      * @param protocol Protocol for the handler
      * @param handler  Handler for the protocol
      */
     private void saveHandler(final String protocol, final String handler) {
         if (handler.isEmpty()) {
             userConfig.unsetOption("protocol", protocol);
         } else {
             userConfig.setOption("protocol", protocol, handler);
 
         }
 
     }
 
     @Override
     public void valueChanged(final ListSelectionEvent e) {
         if (e == null || !e.getValueIsAdjusting()) {
             setVisible(false);
             if (selectedRow != -1 && selectedRow < model.getRowCount()) {
                 final URLProtocolPanel panel = details.get(model.getValue(selectedRow).getUri());
                 model.getValue(selectedRow).setHandler(panel.getSelection());
             }
             if (table.getSelectedRow() == -1) {
                 activeComponent = empty;
                 layoutComponents();
                 add.setEnabled(false);
                 remove.setEnabled(false);
                 selectedRow = -1;
             } else {
                 activeComponent = details.get(model.getValue(table.getRowSorter().
                         convertRowIndexToModel(table.getSelectedRow())).getUri());
                 layoutComponents();
                 add.setEnabled(true);
                 remove.setEnabled(true);
                 selectedRow = table.getRowSorter().convertRowIndexToModel(table.
                         getSelectedRow());
             }
             setVisible(true);
         }
     }
 
     @Override
     public void actionPerformed(final ActionEvent e) {
         if (e.getSource() == add) {
             new StandardInputDialog(parentWindow,
                     ModalityType.MODELESS, iconManager, "New URL handler",
                     "Please enter the name of the new protocol.",
                     new URLProtocolValidator(globalConfig), this::saveAddNewURLHandler).display();
 
         } else if (e.getSource() == remove) {
             model.removeValue(model.getValue(table.getRowSorter().convertRowIndexToModel(
                     table.getSelectedRow())));
         }
     }
 
     private boolean saveAddNewURLHandler(final String text) {
         try {
             final URI uri = new URI(text + "://example.test.com");
             model.addValue(new URLHandlerHolder(uri, getURLHandler(uri)));
             details.put(uri, new URLProtocolPanel(globalConfig, userConfig, uri, true));
             return true;
         } catch (final URISyntaxException ex) {
             return false;
         }
     }
 
 }
