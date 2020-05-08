 /**
  * 
  */
 package conf;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashMap;
 import java.util.Map;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFormattedTextField;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.WindowConstants;
 import javax.swing.UIManager;
 
 import values.Settings;
 import values.BrickColor;
 
 /**
  * @author stranger
  *
  */
 @SuppressWarnings("serial")
 public class Dialog extends JDialog implements ActionListener {
 
     private Settings settings;
     private JSpinner move;
     private JSpinner fire;
     private JComboBox locale;
     private JComboBox laf;
     private JComboBox theme;
     private Map<String, String> localeList;
     private Map<String, String> lafList;
     private Map<String, Integer> themeList;
     private boolean reInitRequired = false;
 
     /**
      * Default constructor.
      * 
      * @param settings - game Settings instance
      */
     public Dialog(Settings settings) {
         super();
         this.settings = settings;
         
         // displaying window at the screen
         setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
         setTitle(settings.getString("TITLE_CONF"));
        setIconImage(new ImageIcon(this.getClass().getClassLoader().getResource("images/icon.png")).getImage());
         
         // move delay spinner label
         JLabel moveLabel = new JLabel(settings.getString("CONF_MOVE_DELAY"));
         
         // move delay spinner
         SpinnerNumberModel moveModel = new SpinnerNumberModel(settings.getMoveDelay(), 0, Integer.MAX_VALUE, 5);
         move = new JSpinner(moveModel);
         JFormattedTextField mtf = ((JSpinner.DefaultEditor) move.getEditor()).getTextField();
         mtf.setEditable(false);
         mtf.setBackground(Color.white);
         
         // fire delay spinner label
         JLabel fireLabel = new JLabel(settings.getString("CONF_FIRE_DELAY"));
         
         // fire delay spinner
         SpinnerNumberModel fireModel = new SpinnerNumberModel(settings.getFireDelay(), 0, Integer.MAX_VALUE, 5 * 5);
         fire = new JSpinner(fireModel);
         JFormattedTextField ftf = ((JSpinner.DefaultEditor) fire.getEditor()).getTextField();
         ftf.setEditable(false);
         ftf.setBackground(Color.white);
         
         // locale text label
         JLabel localeLabel = new JLabel(settings.getString("CONF_LOCALE"));
         
         // locale text field
         localeList = new HashMap<String, String>();
         localeList.put(settings.getString("CONF_AUTO"), "auto");
         
         String locales = settings.getString("LANGUAGE");
         for (String tmp : locales.split(";")) {
             String[] values = tmp.split(",");
             if (values.length > 1) {
                 localeList.put(values[0], values[1]);
             } else {
                 localeList.put(values[0], "");
             }
         }
         
         locale = new JComboBox(localeList.keySet().toArray());
         for (int i = 0; i < locale.getItemCount(); i++) {
             if (localeList.get(locale.getItemAt(i)).equals(settings.getLocale())) {
                 locale.setSelectedIndex(i);
                 break;
             }
         }
 
         // LookAndFeel text label
         JLabel lafLabel = new JLabel(settings.getString("CONF_LAF"));
 
         // LookAndFeel text field
         lafList = new HashMap<String, String>();
         lafList.put(settings.getString("CONF_AUTO"), "auto"); 
 
         UIManager.LookAndFeelInfo[] lookAndFeelInfos = UIManager.getInstalledLookAndFeels();
         for (UIManager.LookAndFeelInfo lookAndFeelInfo : lookAndFeelInfos) {
             String name = lookAndFeelInfo.getName();
             String className = lookAndFeelInfo.getClassName();
             lafList.put(name, className);
         }
 
         laf = new JComboBox(lafList.keySet().toArray());
         for (int i = 0; i < laf.getItemCount(); i++) {
             if (lafList.get(laf.getItemAt(i)).equals(UIManager.getLookAndFeel().getClass().getName())) {
                 laf.setSelectedIndex(i);
                 break;
             }
         }
 
         // Theme text label
         JLabel themeLabel = new JLabel(settings.getString("CONF_THEME"));
 
         // Theme text field
         themeList = new HashMap<String, Integer>();
         themeList.put(settings.getString("THEME_MODERN"), 1);
         themeList.put(settings.getString("THEME_CLASSIC"), 2);
         themeList.put(settings.getString("THEME_CIRCLES"), 3);
 
         theme = new JComboBox(themeList.keySet().toArray());
         theme.setSelectedIndex(this.settings.getThemeIndex() - 1);
 
         // ok button
         JButton okButton = new JButton();
         okButton.setText("Ok");
         okButton.addActionListener(this);
         
         // cancel button
         JButton cancelButton = new JButton();
         cancelButton.setText(settings.getString("CANCEL"));
         cancelButton.addActionListener(
                 new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                         setVisible(false);
                     }
                 }
         );
         
         // button panel
         JPanel buttons = new JPanel();
         buttons.add(okButton);
         buttons.add(cancelButton);
         
         // labels panel
         JPanel labels = new JPanel();
         labels.setLayout(new GridLayout(5, 1));
         labels.add(moveLabel);
         labels.add(fireLabel);
         labels.add(localeLabel);
         labels.add(lafLabel);
         labels.add(themeLabel);
 
         // input elements panel
         JPanel input = new JPanel();
         input.setLayout(new GridLayout(5, 1));
         input.add(move);
         input.add(fire);
         input.add(locale);
         input.add(laf);
         input.add(theme);
 
         // conf elements panel
         JPanel elements = new JPanel();
         elements.setLayout(new BoxLayout(elements, BoxLayout.LINE_AXIS));
         elements.add(labels);
         elements.add(input);
         
         // combine all
         JPanel content = new JPanel();
         content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
         content.setBorder(BorderFactory.createEmptyBorder(9, 9, 0, 9));
         content.add(elements);
         content.add(buttons);
         this.setContentPane(content);
         
         pack();
         setResizable(false);
         
         Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
         setLocation((dim.width - getWidth()) / 2, (dim.height - getHeight()) / 2);
 
         setModal(true);
         setVisible(true);
     }
 
     /**
      * Action listener implementation for Ok button.
      * 
      * @param e - ActionEvent object
      */
     public void actionPerformed(ActionEvent e) {
 
         Integer value = (Integer) move.getValue();
         if (this.settings.getMoveDelay() != value) {
             this.settings.setMoveDelay(value);
         }
         
         value = (Integer) fire.getValue();
         if (this.settings.getFireDelay() != value) {
             this.settings.setFireDelay(value);
         }
         
         String text = localeList.get(locale.getSelectedItem());
         if (!text.equals(this.settings.getLocale())) {
             if ("auto".equals(text)) {
                 this.settings.cleanLocale();
             } else {
                 this.settings.setLocale(text);
             }
         }
         
         text = lafList.get(laf.getSelectedItem());
         if (!text.equals(UIManager.getLookAndFeel().getClass().getName())) {
             if ("auto".equals(text)) {
                 this.settings.cleanLaF();
                 text = this.settings.getDefaultLaF();
             } else {
                 this.settings.setLaF(text);
             }
 
             try {
                 UIManager.setLookAndFeel(text);
             } catch (Exception ex) {
                 System.out.println(settings.getString("ERROR_LAF"));
             }
         }
 
         int index = themeList.get(theme.getSelectedItem());
         if (index != this.settings.getThemeIndex()) {
             this.settings.setThemeIndex(index);
             BrickColor.init(index);
             this.reInitRequired = true;
         }
 
         setVisible(false);
     }
 
     /**
      * Getter for reInitRequired variable.
      *
      * @return true if re-initialization required, false otherwise
      */
     public boolean isReInitRequired() {
         return reInitRequired;
     }
 }
