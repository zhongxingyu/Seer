 package experimental.info;
 
 import javax.jnlp.DownloadService;
 import javax.jnlp.DownloadService2;
 import javax.jnlp.ServiceManager;
 import javax.swing.*;
 import java.awt.*;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import static javax.jnlp.DownloadService2.ResourceSpec;
 import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
 import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
 import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
 
 /**
  * @author gmatoga
  */
 public class InfoPanel {
 
     public static final String PREFIX = "${env";
 
     public static void main(String... args) throws Exception {
 
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         final JFrame frame = new JFrame("Properties");
         frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         frame.add(new InfoPanel().getPanel());
         frame.pack();
         frame.setVisible(true);
     }
 
     public JPanel getPanel() {
 
 
         JPanel panel = new JPanel(new BorderLayout());
         final GridBagLayout layout = new GridBagLayout();
 
         GridBagConstraints c = new GridBagConstraints();
 
         JPanel innerPanel = new JPanel(layout);
         panel.setBackground(Color.white);
         innerPanel.setBackground(Color.white);
         innerPanel.setBorder(BorderFactory.createLineBorder(Color.white, 5));
         int i = 0;
 
         c.fill = GridBagConstraints.HORIZONTAL;
         c.weightx = 0;
         c.gridx = 0;
         c.gridy = i;
         c.gridwidth = 2;
         c.insets = new Insets(5, 5, 5, 5);
         final JLabel textArea = new JLabel("<html><body style='width: 100%'>This tab presents the environment properties, " +
                 "as of the last build", UIManager.getIcon("OptionPane.informationIcon"), JLabel.LEADING);
         final Font tabFont = new Font("Dialog", Font.PLAIN, 12);
         textArea.setFont(tabFont);
         innerPanel.add(textArea, c);
         i++;
 
         c.fill = GridBagConstraints.HORIZONTAL;
         c.weightx = 0;
         c.gridx = 0;
         c.gridy = i;
         c.gridwidth = 2;
         c.insets = new Insets(5, 5, 5, 5);
         innerPanel.add(new JSeparator(), c);
         i++;
 
         Properties prop = new Properties();
         InputStream in = InfoPanel.class.getResourceAsStream("/about.properties");
         try {
 
             Map<String, String> filteredProperties = loadAndProcessProperties(prop, in);
 
             for (Map.Entry<String, String> entry : filteredProperties.entrySet()) {
                 final JLabel keyLabel = new JLabel(entry.getKey() + ":");
                 keyLabel.setFont(tabFont);
                 final String value = entry.getValue();
                 final JTextField valueLabel = new JTextField(value);
                 valueLabel.setEditable(false);
                 valueLabel.setBackground(Color.white);
                 valueLabel.setBorder(null);
                 valueLabel.setFont(tabFont);
 
                 if (value == null) {
                     valueLabel.setText("---");
                     valueLabel.setEnabled(false);
                 }
                 c.fill = GridBagConstraints.HORIZONTAL;
                 c.weightx = 0;
                 c.gridx = 0;
                 c.gridy = i;
                 c.gridwidth = 1;
                 c.insets = new Insets(5, 5, 0, 0);
                 innerPanel.add(keyLabel, c);
 
                 c.fill = GridBagConstraints.HORIZONTAL;
                 c.weightx = 1;
                 c.gridx = 1;
                 c.gridy = i;
                 c.gridwidth = 1;
                 c.insets = new Insets(5, 15, 0, 0);
                 innerPanel.add(valueLabel, c);
                 i++;
             }
 
             JScrollPane scrollPane = new JScrollPane(innerPanel,
                     VERTICAL_SCROLLBAR_AS_NEEDED,
                     HORIZONTAL_SCROLLBAR_AS_NEEDED);
             panel.add(scrollPane);
         } catch (Exception e) {
             e.printStackTrace();
         }
         return panel;
     }
 
     private Map<String, String> loadAndProcessProperties(Properties prop, InputStream in) throws IOException {
         loadProperties(prop, in);
         Map<String, String> filteredProperties = filterProperties(prop);
         addDynamicValues(prop);
         return filteredProperties;
     }
 
     private void addDynamicValues(Properties prop) {
         String value;
         String label = "getUpdateAvailableResources";
         try {
             DownloadService2 service =
                     (DownloadService2) ServiceManager.lookup("javax.jnlp.DownloadService2");
 
             // create a new instance of ResourceSpec. In this example:
             // - resource is downloaded from a directory on http://foo.bar.com:8080
             // - version is 2. [0-9]+
             // - resource type is JAR
             String codebase = prop.getProperty("maven.jnlpCodebase");
             System.out.println("maven.jnlpCodebase from properties is" + codebase);
            ResourceSpec spec = new ResourceSpec(codebase, "*", DownloadService2.ALL);
             ResourceSpec[] results = service.getUpdateAvailableResources(spec);
             value = Arrays.toString(results);
         } catch (Exception e) {
             e.printStackTrace();
             value = "Exception: " + e.getMessage();
         }
         prop.put(label, value);
     }
 
     private Map<String, String> filterProperties(Properties prop) {
         Map<String, String> filteredProperties = new LinkedHashMap<String, String>();
         for (Map.Entry entry : prop.entrySet()) {
             final String val = entry.getValue().toString();
             final String key = entry.getKey().toString();
             String value = !val.startsWith(PREFIX) ? val : null;
             filteredProperties.put(key, value);
         }
         return filteredProperties;
     }
 
     private void loadProperties(Properties prop, InputStream in) throws IOException {
         prop.load(in);
         in.close();
     }
 }
