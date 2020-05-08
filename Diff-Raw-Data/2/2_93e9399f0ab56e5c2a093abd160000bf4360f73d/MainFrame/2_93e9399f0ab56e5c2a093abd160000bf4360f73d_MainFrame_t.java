 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gui;
 
 import elements.DataDisplayOutput;
 import elements.DataDisplayInput;
 import fisparser.Rule;
 import java.awt.Color;
 import java.awt.event.MouseEvent;
 import java.io.FileNotFoundException;
 import java.util.EnumMap;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.Icon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 import setup.PanelFactory;
 import setup.Result;
 import xmlparser.ObservableParser;
 import xmlparser.Timepoint;
 
 /**
  *
  * @author sch1zo
  */
 public class MainFrame extends JFrame implements ObservableParser.Observer{
   private static MainFrame current;
 
   private MainPanel graphPanel;
   private JPanel inputPanel;
   private JPanel outputPanel;
   private JPanel buttonPanel;
   private DataDisplayInput[] inputs;
   private DataDisplayOutput[] outputs;
   private ObservableParser xmlparser;
   private List<String> timelist;
   private List<Float[]> inputList;
   private List<Float[]> outputList;
   private JLabel lpause;
   private boolean pause;
   private Icon ipause;
   private Icon iplay;
   private static final Color foregroundColor = new Color(51, 204, 0);
   private static final Color highlightColor = new Color(0, 80, 0);
   private FuzzyPanel fuzzyPanel;
   private EnumMap<OutputEnum, List<Rule>> rules;
   private JLabel settingsLabel;
   private final Result results;
   private JLabel timestampLabel;
   private JLabel descriptionLabel;
 
   public MainFrame(int sleep) {
     JOptionPane.showMessageDialog(current,"Achtung dies ist eine Testversion.\nBenutzung erfolg auf eigene Gefahr.\nFür Schäden wird nicht gehaftet.","Testversion!",JOptionPane.WARNING_MESSAGE);
     PanelFactory panel_factory = new PanelFactory();
     results = panel_factory.getResult();
     initComponents();
     
     //panel_factory.getWizard().registerOnSave(this);
     startParser(sleep);
   }
 
 
   private void initComponents() {
     loadPlayPauseGfx();
     setLayout(null);
     this.getContentPane().setBackground(Color.BLACK);
 
     
 
     inputPanel = new JPanel();
     inputPanel.setBackground(new Color(0,3,91));
     inputPanel.setLocation(667, 0);
     inputPanel.setSize(133, 480);
     this.add(inputPanel);
     fillInputPanel(inputPanel);
 
     addPauseButton();
 
     outputPanel = new JPanel();
     outputPanel.setBackground(new Color(0,3,91));
     outputPanel.setLocation(133, 480);
     outputPanel.setSize(532, 120);
     this.add(outputPanel);
     fillOutputPanel(outputPanel);
 
     graphPanel = new MainPanel(results, this);
     graphPanel.setBackground(Color.BLACK);
     graphPanel.setLocation(0, 0);
     graphPanel.setSize(667, 480);
     graphPanel.setVisible(true);
     this.add(graphPanel);
 
     buttonPanel = new JPanel();
     buttonPanel.setLocation(667, 480);
     buttonPanel.setSize(133, 120);
     buttonPanel.setBackground(Color.BLACK);
     fillButtonPanel(buttonPanel);
     this.add(buttonPanel);
 
     fuzzyPanel = new FuzzyPanel(rules, new String[]{"Rule#", InputEnum.get(results.getSelected_inputs()[0]).getName(),
                                             InputEnum.get(results.getSelected_inputs()[1]).getName(),
                                             InputEnum.get(results.getSelected_inputs()[2]).getName(),
                                             InputEnum.get(results.getSelected_inputs()[3]).getName(),
                                             "Outputset"},this);
     fuzzyPanel.setLocation(0, 0);
     fuzzyPanel.setSize(667, 480);
     fuzzyPanel.setVisible(false);
     this.add(fuzzyPanel);
   }
 
   public static void main(String... args) {
     int s;
     try{
       s = Integer.parseInt(args[0]);
    }catch(Exception e){
       s = 300;
     }
     current = new MainFrame(s);
     current.setSize(800, 600);
     current.setLocation(10, 10);
 
     current.setResizable(true);
     current.setUndecorated(true);   //with this you cant move the window, but all sizes fit well
 
     current.setVisible(true);
     current.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   }
 
   private void fillInputPanel(JPanel inputPanel) {
     inputPanel.setLayout(null);
     inputs = new DataDisplayInput[4];
 
     for (int i = 0; i < inputs.length; i++) {
       inputs[i] = new DataDisplayInput(132, 119);
       inputs[i].setLocation(1, i * 120);
       inputs[i].setType(InputEnum.get(results.getSelected_inputs()[i]));
       inputPanel.add(inputs[i]);
     }
   }
 
   public void dehighlightAllOutputs() {
     for (DataDisplayOutput output : this.outputs) {
       output.setHighlight(false);
     }
   }
 
   private void addPauseButton() {
     lpause = new JLabel();
     lpause.addMouseListener(new SimpleClickHandler() {
 
       @Override
       public void mouseClicked(MouseEvent e) {
         togglePause();
       }
     });
 
     pause = false;
     lpause.setIcon(ipause); // NOI18N
     lpause.setSize(133, 120);
     lpause.setLocation(0, 480);
     this.add(lpause);
   }
 
   private void togglePause() {
     if (pause) {
       lpause.setIcon(ipause);
       pause = false;
     } else {
       lpause.setIcon(iplay);
       pause = true;
     }
     
     for(DataDisplayOutput output: this.outputs) {
         output.setPause(pause);
     }
   }
   
   private void fillOutputPanel(JPanel outputPanel) {
     outputPanel.setLayout(null);
 
     outputs = new DataDisplayOutput[4];
     for (int i = 0; i < outputs.length; i++) {
         final int num = i;
       outputs[i] = new DataDisplayOutput(132, 119);
       outputs[i].addClickListener(new DataDisplayOutput.ClickListener() {
         public void onClick(DataDisplayOutput sender) {
           dehighlightAllOutputs();
           sender.setHighlight(true);
           fuzzyPanel.setOutputType(sender.getType());
           if(fuzzyPanel.isVisible())
             fuzzyPanel.updateData();
           graphPanel.showDetails(num);
         }
       });
       outputs[i].setLocation(i * 133, 1);
       outputs[i].setType(OutputEnum.get(results.getSelected_drugs()[i]));
       outputPanel.add(outputs[i]);
     }
   }
 
   private void loadPlayPauseGfx() {
 
     ipause = new javax.swing.ImageIcon(getClass().getResource("/resource/Pause.png"));
     iplay = new javax.swing.ImageIcon(getClass().getResource("/resource/Play.png"));
   }
 
   public void update(ObservableParser parser) {
     List<Timepoint> p = parser.getLastTimepoint();
     
     int id = p.get(0).getId();
 
     setTitle(Integer.toString(id)); // HACK
 
     float tmp;
 
     for (int i = 0; i < inputs.length; i++) {
       tmp = inputList.get(id)[i];
       inputs[i].setValue(tmp);
     }
 
     for (int i = 0; i < outputs.length; i++) {
       tmp = outputList.get(id)[i];
       outputs[i].setValue(tmp);
     }
 
     graphPanel.updateInputGraphs(inputList, outputList, id);
     timestampLabel.setText(timelist.get(id));
 
     //fuzzyPanel.updateData(p, OutputEnum.ISDN); //send newest point to fuzzypanel
 
   }
 
   private void startParser(int sleep) {
     OutputEnum[] o = new OutputEnum[]{OutputEnum.get(results.getSelected_drugs()[0]),
                                       OutputEnum.get(results.getSelected_drugs()[1]),
                                       OutputEnum.get(results.getSelected_drugs()[2]),
                                       OutputEnum.get(results.getSelected_drugs()[3])};
     xmlparser = new ObservableParser(o, sleep);
     xmlparser.addObserver(this);
     logparser.Parser lparser = new logparser.Parser();
     fisparser.Parser fparser = new fisparser.Parser();
     try {
       lparser.run("resources/controller.log"); 
       fparser.run(o);
     } catch (FileNotFoundException ex) {
       Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
     }
     inputList = lparser.getInputs();
     outputList = lparser.getOutputs();
     timelist = lparser.getTimestamps();
     rules = fparser.getAllRules();
     fuzzyPanel.setRules(rules);
     fuzzyPanel.setAbsInputs(inputList);
     fuzzyPanel.setParser(xmlparser);
     fuzzyPanel.setTimes(timelist);
     fuzzyPanel.setOutputType(OutputEnum.ISDN);
     xmlparser.start();
   }
 
   private void fillButtonPanel(JPanel buttonPanel) {
     buttonPanel.setLayout(null);
 
     timestampLabel = new JLabel();
     timestampLabel.setForeground(foregroundColor);
     timestampLabel.setHorizontalAlignment(SwingConstants.CENTER);
     timestampLabel.setVerticalAlignment(SwingConstants.CENTER);
     timestampLabel.setSize(66, 60);
     timestampLabel.setLocation(0, 60);
     buttonPanel.add(timestampLabel);
 
     settingsLabel = new JLabel();
     settingsLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/setup.png")));
     settingsLabel.setSize(66, 60);
     settingsLabel.setLocation(0, 0);
     settingsLabel.addMouseListener(new SimpleClickHandler(){
       @Override
       public void mouseClicked(MouseEvent e) {
         PanelFactory p = new PanelFactory();
       }
       @Override
       public void mouseEntered(MouseEvent e) {
         descriptionLabel.setText("<html>Einstel-<br/>lungen</html>");
         descriptionLabel.setVisible(true);
       }
 
       @Override
       public void mouseExited(MouseEvent e) {
         descriptionLabel.setVisible(false);
       }
 
     });
     buttonPanel.add(settingsLabel);
 
     JLabel powerLabel = new JLabel();
     powerLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resource/Power.png")));
     powerLabel.setSize(66, 60);
     powerLabel.setLocation(66, 60);
     powerLabel.addMouseListener(new SimpleClickHandler() {
       @Override
       public void mouseClicked(MouseEvent e) {
         if (JOptionPane.showConfirmDialog(current, "Wirklich beenden?","Quit",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
           current.dispose();
           System.exit(0);
         }
       }
       @Override
       public void mouseEntered(MouseEvent e) {
         descriptionLabel.setText("Beenden");
         descriptionLabel.setVisible(true);
       }
 
       @Override
       public void mouseExited(MouseEvent e) {
         descriptionLabel.setVisible(false);
       }
     });
     buttonPanel.add(powerLabel);
 
     descriptionLabel = new JLabel();
     descriptionLabel.setVerticalAlignment(SwingConstants.CENTER);
     descriptionLabel.setHorizontalAlignment(SwingConstants.CENTER);
     descriptionLabel.setForeground(foregroundColor);
     descriptionLabel.setLocation(66, 0);
     descriptionLabel.setSize(66, 60);
     descriptionLabel.setVisible(false);
     buttonPanel.add(descriptionLabel);
   }
   
   public static Color getForegroundColor() {
     return foregroundColor;
   }
   public static Color getHighlightColor() {
     return highlightColor;
   }
   public void showFuzzy(){
     graphPanel.setVisible(false);
     fuzzyPanel.setVisible(true);
     fuzzyPanel.setCurrent_id(xmlparser.getLastTimepoint().get(0).getId());
     fuzzyPanel.updateData();
   }
   public void unshowFuzzy(){
     graphPanel.setVisible(true);
     fuzzyPanel.setVisible(false);
   }
 
   void showOverview() {
     graphPanel.showInputGraphs();
   }
 }
