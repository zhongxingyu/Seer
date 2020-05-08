 package ch9k.plugins;
 
 import ch9k.core.I18n;
 import javax.swing.GroupLayout;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JButton;
 import javax.swing.JTextField;
 import javax.swing.JList;
 import javax.swing.JScrollPane;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.SwingConstants;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.ListSelectionEvent;
 import java.util.Set;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.net.URL;
 import java.net.MalformedURLException;
 import java.io.File;
 import javax.swing.filechooser.FileFilter;
 
 /**
  * Preference pane for the plugins.
  */
 public class PluginPreferencePane
         extends JPanel implements ChangeListener {
     /**
      * The plugin manager.
      */
     private final PluginManager manager;
 
     /**
      * Input field for url.
      */
     private final JTextField urlField;
 
     /**
      * Browse button.
      */
     private final JButton browseButton;
 
     /**
      * Install plugin button.
      */
     private JButton installPluginButton;
 
     /**
      * Plugin list.
      */
     private final JList pluginList;
 
     /**
      * Remove plugin button.
      */
     private final JButton removePluginButton;
 
     /**
      * Constructor.
      * @param settings Settings to manipulate.
      */
     public PluginPreferencePane(final PluginManager manager) {
         this.manager = manager;
         GroupLayout layout = new GroupLayout(this);
 
         layout.setAutoCreateGaps(true);
         layout.setAutoCreateContainerGaps(true);
 
         JLabel urlLabel = new JLabel(I18n.get("ch9k.plugins", "set_url"));
         urlField = new JTextField();
         browseButton = new JButton(I18n.get("ch9k.plugins", "browse_file"));
         installPluginButton =
                 new JButton(I18n.get("ch9k.plugins", "install_plugin"));
         JLabel pluginListLabel =
                 new JLabel(I18n.get("ch9k.plugins", "plugin_list"));
         pluginList = new JList();
         JScrollPane pluginListScrollPane = new JScrollPane();
         pluginListScrollPane.getViewport().setView(pluginList);
         removePluginButton = new JButton(
                 I18n.get("ch9k.plugins", "remove_plugin"));
 
         layout.setHorizontalGroup(layout.createParallelGroup()
             .addComponent(urlLabel)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(urlField)
                 .addComponent(browseButton))
             .addComponent(installPluginButton)
             .addComponent(pluginListLabel)
             .addComponent(pluginListScrollPane)
             .addComponent(removePluginButton));
 
         layout.setVerticalGroup(layout.createSequentialGroup()
             .addComponent(urlLabel)
             .addGroup(layout.createParallelGroup()
                 .addComponent(urlField, GroupLayout.PREFERRED_SIZE,
                         GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                 .addComponent(browseButton))
             .addComponent(installPluginButton)
             .addGap(20)
             .addComponent(pluginListLabel)
             .addComponent(pluginListScrollPane)
             .addComponent(removePluginButton));
 
         layout.linkSize(SwingConstants.HORIZONTAL,
                 installPluginButton, removePluginButton);
         layout.linkSize(SwingConstants.VERTICAL,
                 urlField, browseButton);
 
         setLayout(layout);
 
         /* React on changes in the url field. */
         urlField.getDocument().addDocumentListener(new DocumentListener() {
             public void changedUpdate(DocumentEvent e) {
                 updateInstallPluginButton();
             }
 
             public void insertUpdate(DocumentEvent e) {
                 updateInstallPluginButton();
             }
 
             public void removeUpdate(DocumentEvent e) {
                 updateInstallPluginButton();
             }
         });
 
         /* Install a plugin when the plugin button is clicked. */
         installPluginButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 JOptionPane.showMessageDialog(PluginPreferencePane.this,
                         I18n.get("ch9k.plugins", "plugin_will_install"));
                 String text = urlField.getText();
                 try {
                     final URL url = new URL(text);
                     new Thread(new Runnable() {
                         public void run() {
                             manager.getPluginInstaller().installPlugin(url);
                         }
                     }).start();
                     urlField.setText("");
                 } catch(MalformedURLException exception) {
                     /* Should not happen, because then the button would not be
                      * enabled. */
                 }
             }
         });
 
         /* Allow the user to select a file. */
         browseButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 final JFileChooser chooser = new JFileChooser();
                 chooser.setFileFilter(new FileFilter(){
                     public boolean accept(File f) {
                         return f.getPath().toLowerCase().endsWith(".jar") || f.isDirectory();
                     }
                     public String getDescription() {
                         return "jar files";
                     }
                 });
                 int result = chooser.showOpenDialog(PluginPreferencePane.this);
                 if(result == JFileChooser.APPROVE_OPTION) {
                     File file = chooser.getSelectedFile();
                     try {
                         urlField.setText(file.toURI().toURL().toString());
                     } catch(MalformedURLException exception) {
                         /* Should not happen, because a selected file is always
                          * valid. */
                     }
                 }
             }
         });
 
         /* Listen to changes in the plugin list selection. */
         pluginList.addListSelectionListener(new ListSelectionListener() {
             public void valueChanged(ListSelectionEvent event) {
                 updateRemovePluginButton();
             }
         });
 
         /* Remove a plugin. */
         removePluginButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 Object selection = pluginList.getSelectedValue();
                 if(selection != null && selection instanceof String) {
                     String name = (String) selection;
                     String plugin = manager.getPrettyNames().get(name);
                     manager.softRemovePlugin(plugin);
                 }
 
                 updateRemovePluginButton();
             }
         });
 
         updateInstallPluginButton();
         updatePluginList();
         updateRemovePluginButton();
 
         manager.addChangeListener(this);
     }
 
     /**
      * Update the install plugin button.
      */
     private void updateInstallPluginButton() {
         String text = urlField.getText();
         try {
             URL url = new URL(text);
             installPluginButton.setEnabled(true);
         } catch(MalformedURLException exception) {
             installPluginButton.setEnabled(false);
         }
     }
 
     /**
      * Update the plugin list.
      */
     private void updatePluginList() {
         Set<String> names = manager.getPrettyNames().keySet();
         pluginList.setListData(names.toArray());
     }
 
     /**
      * Update the remove plugin button.
      */
     private void updateRemovePluginButton() {
         Object selection = pluginList.getSelectedValue();
         if(selection != null && selection instanceof String) {
             String name = (String) selection;
             String plugin = manager.getPrettyNames().get(name);
             removePluginButton.setEnabled(
                     manager.getPluginFileName(plugin) != null);
         }
     }
 
     @Override
     public void stateChanged(ChangeEvent event) {
         updatePluginList();
     }
 }
