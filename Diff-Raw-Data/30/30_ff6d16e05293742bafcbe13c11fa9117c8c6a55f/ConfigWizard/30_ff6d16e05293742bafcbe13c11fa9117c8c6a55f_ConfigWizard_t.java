 package edu.unh.schwartz.epframework.config;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.ButtonGroup;
 import javax.swing.JCheckBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.ciscavate.cjwizard.PageFactory;
 import org.ciscavate.cjwizard.pagetemplates.DefaultPageTemplate;
 import org.ciscavate.cjwizard.WizardContainer;
 import org.ciscavate.cjwizard.WizardListener;
 import org.ciscavate.cjwizard.WizardPage;
 import org.ciscavate.cjwizard.WizardSettings;
 
 /**
  * GUI Wizard to help create configuration file.
  */ 
 public final class ConfigWizard
 {
     /**
      * The instance.
      */
     private static ConfigWizard instance;
 
     /**
      * The Log.
      */
     private static final Log LOG = LogFactory.getLog(ConfigWizard.class);
 
     /**
      * Seperator string.
      */
     private static final String SEP = "======================================="; 
 
     /**
      * The last configuration created.
      */ 
     private Configuration config;
 
     /**
      * The window for the wizard.
      */
     private JDialog window;
 
     /**
      * Prevents instantiation.
      */
     private ConfigWizard()
     {
         window = new JDialog();
 
         final WizardContainer wc = 
             new WizardContainer(new ConfigPageFactory(),
                     new DefaultPageTemplate());
 
         wc.addWizardListener(new ConfigWizardListener());
 
         window.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
         window.getContentPane().add(wc);
         window.setSize(380, 290);
         window.setLocationRelativeTo(null);
     }
 
     /**
      * @return the instance of the <code>ConfigWizard</code>
      */
     public static ConfigWizard getInstance()
     {
         if (instance == null)
         {
             instance = new ConfigWizard();
         }
         return instance;
     }
 
     /**
      * Goes through the process of creating a configuration file. Saves it?
      *
      * @return the configuration created by the user
      */
     public Configuration createConfiguration()
     {
         this.window.setVisible(true);
         synchronized(this)
         {
             try
             {
                 this.wait();
             }
             catch (InterruptedException e)
             {
                 LOG.fatal("createConfiguration: " + e.getMessage());
             }
         }
 
         return this.config;
     }
 
     /**
      * Handles the callbacks from the wizard.
      */
     private final class ConfigWizardListener implements WizardListener
     {
         /**
          * {@inheritDoc}
          */
         @Override
         public void onCanceled(final List<WizardPage> path, 
                 final WizardSettings settings)
         {
             LOG.debug("Cancelled wizard");
             ConfigWizard.this.window.dispose();
             synchronized(ConfigWizard.this)
             {
                 ConfigWizard.this.notify();
             }
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public void onFinished(final List<WizardPage> path, 
                 final WizardSettings settings)
         {
             LOG.debug("Finished wizard");
             ConfigWizard.this.config = new Configuration(settings);
             ConfigWizard.this.window.dispose();
             synchronized(ConfigWizard.this)
             {
                 ConfigWizard.this.notify();
             }
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public void onPageChanged(final WizardPage newPage, 
                 final List<WizardPage> path) 
         {
             ConfigWizard.this.window.setTitle(newPage.getDescription());
         }
     }
 
     private final class ConfigPageFactory implements PageFactory
     {
         /**
          * The pages of the wizard.
          */
         private final WizardPage[] pages = {new IOPage(), new ExecPage(), 
             new OtherPage(),}; 
 
         /**
          * {@inheritDoc}
          */
         public WizardPage createPage(final List<WizardPage> path, 
                 final WizardSettings settings)
         {
             if (path.size() == pages.length)
             {
                 return new ReviewPage(settings);
             }
             else
             {
                 return pages[path.size()];
             }
         }
     }
 
     private final class IOPage extends WizardPage
     {
         private IOPage()
         {
             super("IO", "IO Settings");
 
             // Settings for the input file
             final JFileChooserButton inChooser = new JFileChooserButton(false);
             inChooser.setName(Configuration.IN_FILE_KEY);
             add((new JLabel("Select the input file: ")));
             add(inChooser);
 
             // Setting for the split pattern
             final JTextField splitField = new JTextField();
             splitField.setName(Configuration.SPLIT_PATTERN_KEY);
             splitField.setPreferredSize(new Dimension(50, 20));
             add(new JLabel("Pattern used to split input: "));
             add(splitField);
             add(new JLabel("(Leave blank to use every new line)"));
             add((new JLabel("               ====== OR ======               ")));
 
             // Settings for the input directory
             final JFileChooserButton inDirChooser = new JFileChooserButton(true);
             inDirChooser.setName(Configuration.IN_FILE_DIR_KEY);
             add((new JLabel("Select the input directory: ")));
             add(inDirChooser);
             add(new JLabel(SEP));
 
             // Settings for the output location
             final JFileChooserButton outChooser = new JFileChooserButton(true);
             outChooser.setName(Configuration.OUT_FILE_KEY);
             add((new JLabel("Select the output directory: ")));
             add(outChooser);
         }
     }
 
     private final class ExecPage extends WizardPage
     {
         private ExecPage()
         {
             super("Exec", "Executable Settings");
 
             // Settings for the output location
             final JFileChooserButton execChooser = 
                 new JFileChooserButton(false);
             execChooser.setName(Configuration.EXEC_LOC_KEY);
             add((new JLabel("Select the executable: ")));
             add(execChooser);
             add(new JLabel(SEP));
 
             // Arguments for executable
             final JTextField argumentField = new JTextField();
             argumentField.setName(Configuration.ARGUMENT_KEY);
            argumentField.setPreferredSize(new Dimension(300, 20));
            add(new JLabel("Enter in the arguments for the executable:"));
             add(argumentField);
            add(new JLabel("Use @ for input file / Use $ for output file"));
             add(new JLabel(SEP));
 
             final JRadioButton jRadioDefault = new JRadioButton("Default");
             final JRadioButton jRadioCustom = new JRadioButton("Custom");
             final JRadioButton jRadioExternal = new JRadioButton("External");
 
             jRadioDefault.setName(Configuration.DEFAULT_MERGE_KEY);
            jRadioCustom.setName(Configuration.CUSTOM_MERGE_KEY);
            jRadioExternal.setName(Configuration.EXTERNAL_MERGE_KEY);
 
             final ButtonGroup group = new ButtonGroup();
             group.add(jRadioDefault);
             group.add(jRadioCustom);
             group.add(jRadioExternal);
 
             add(new JLabel("Select the merge method"));
             add(jRadioDefault);
             add(jRadioCustom);
             add(jRadioExternal);
         }
     }
 
     private final class OtherPage extends WizardPage
     {
         private OtherPage()
         {
             super("Threads", "Other Settings");
             
             // Settings for the number of threads
             final int max = Runtime.getRuntime().availableProcessors();
             final List<String> options = new ArrayList<String>();
             int i = 1;
             while (i <= max)
             {
                 options.add(Integer.toString(i));
                 i++;
             }
 
             final WizardSpinner spinner = new WizardSpinner(options);
             spinner.setName(Configuration.NUM_PROCESSES_KEY);
             add(new JLabel("Select the number of processes: "));
             add(spinner);
             add(new JLabel(SEP));
 
             final JCheckBox box = new JCheckBox();
             box.setName(Configuration.STATS_KEY);
             add(new JLabel("Create stats file?"));
             add(box);
             add(new JLabel(SEP));
 
             final JTextField headerField = new JTextField();
             headerField.setName(Configuration.NUM_HEADER_KEY);
             headerField.setText("0");
             headerField.setPreferredSize(new Dimension(50, 20));
             add(new JLabel("Number of header lines in output files: "));
             add(headerField);
             add(new JLabel(SEP));
  
             final JCheckBox savebox = new JCheckBox();
             savebox.setName(Configuration.SAVE_KEY);
             add(new JLabel("Save Configuration File?"));
             add(savebox);
         }
     }
 
     private final class ReviewPage extends WizardPage
     {
         private ReviewPage(final WizardSettings settings)
         {
             super("Confirmation", "Confirmation Page");
 
             add(new JLabel("Configuration Settings:\n "));
             
             // Get the settings
             final JTextArea label = new JTextArea(10, 25);
             for(final String key : settings.keySet())
             {
                 final Object val = settings.get(key);
                 if (val != null && !val.equals(""))
                 {
                     label.append(key);
                     label.append(": ");
                     label.append(val.toString());
                     label.append("\n");
                 }
             }
             label.setEditable(false);
             label.setCaretPosition(0);
             
             add(new JScrollPane(label));
         }
 
         /**
          * {@inheritDoc}
          */
         public void rendering(final List<WizardPage> path, 
                 final WizardSettings settings) 
         {
             super.rendering(path, settings);
             setFinishEnabled(true);
             setNextEnabled(false);
         }
     }
 }
