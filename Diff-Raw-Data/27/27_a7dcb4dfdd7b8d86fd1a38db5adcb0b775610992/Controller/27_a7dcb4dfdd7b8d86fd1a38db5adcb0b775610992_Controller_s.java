 package GIS.graphviewer;
 
 import GIS.graphviewer.Coloring.ALGORITHM;
 import java.awt.CardLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.Timer;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 public class Controller {
     
     
         private static final String DSATURPANEL = "DSATURPANEL";
         private static final String RLFPANEL = "RLFPANEL";
 	private int interval;
         private String algorithm;
 	private Model model;
         private View view;
         private Timer animationTimer;
         private AnimationState state = AnimationState.NOT_RUNNING;
         
         private enum AnimationState { RUNNING, PAUSED, NOT_RUNNING };
 	
 	public int getInterval() {
 		return interval;
 	}
 	public void setInterval(int interval) {
 		this.interval = interval;
 	}
 	public Model getModel() {
 		return model;
 	}
 	public void setModel(Model model) {
 		this.model = model;
 	}
 	
 	public Controller(Model m, View v)
 	{
 		model = m;
                 view = v;
                 ALGORITHM[] algorithms = Coloring.ALGORITHM.values();
                 algorithm = algorithms[0].toString();
                 interval = view.getSlider().getValue();
                 CardLayout cl = (CardLayout)(view.getParamsPanel().getLayout());
                 if(algorithm.equals("DSATUR"))
                     cl.show(view.getParamsPanel(), DSATURPANEL);
                 else
                     cl.show(view.getParamsPanel(), RLFPANEL);
                 
                 // add listeners to the view
                 ControlsActionListener listener = new ControlsActionListener();
                 view.addBtnActionListener(view.getLoadGraphBtn(), listener);
                 view.addBtnActionListener(view.getSaveBtn(), listener);
                 view.addAlgBoxActionListener(listener);
                 view.addSliderChangeListener(listener);
                 view.addBtnActionListener(view.getRunBtn(), listener);
                 view.addBtnActionListener(view.getExitBtn(), listener);
 	}
 	
 	public ArrayList<ArrayList<String>> loadNeighboursMatrix(String path)
 	{
             return FileUtility.loadFile(path);
 //		model.setNeighboursMatrix(FileUtility.loadFile(path));
 //                view.log("Graph loaded:" + View.LINE_END);
 //                view.log(Model.matrixToString(model.getNeighboursMatrix())+View.LINE_END);
 	}
         
         public ArrayList<ArrayList<String>> loadNeighboursMatrix(File f)
 	{
             return FileUtility.loadFile(f);
 //		model.setNeighboursMatrix(FileUtility.loadFile(f));
 //                view.log("Graph loaded:" + View.LINE_END);
 //                view.log(Model.matrixToString(model.getNeighboursMatrix())+View.LINE_END);
 	}
 	
 	public ArrayList<ArrayList<String>> loadCoordinatesMatrix(String path)
 	{
             return FileUtility.loadFile(path);
 //		model.setCoordinatesMatrix(FileUtility.loadFile(path));
 //                view.log("Coordinates loaded:" + View.LINE_END);
 //                view.log(Model.matrixToString(model.getCoordinatesMatrix())+View.LINE_END);
 	}
         
         public ArrayList<ArrayList<String>> loadCoordinatesMatrix(File f)
 	{
             return FileUtility.loadFile(f);
 //		model.setCoordinatesMatrix(FileUtility.loadFile(f));
 //                view.log("Coordinates loaded:" + View.LINE_END);
 //                view.log(Model.matrixToString(model.getCoordinatesMatrix())+View.LINE_END);
 	}
         
         /**
          * 
          */
         public class ControlsActionListener implements ActionListener, ChangeListener{
             
             JFileChooser fc;
             int returnVal;
             File file;
 //            String algorithm;
 
             @Override
             public void actionPerformed(ActionEvent ae) {
                 //handle load graph
                 ArrayList<ArrayList<String>> neighbours;
                 ArrayList<ArrayList<String>> coordinates;
                 if(ae.getSource() == view.getLoadGraphBtn()){
                     fc = new JFileChooser();
                     fc.setDialogTitle("Load Neighbourhood Matrix");
                     returnVal = fc.showOpenDialog(view);
                     if (returnVal == JFileChooser.APPROVE_OPTION) {
                         file = fc.getSelectedFile();
                         neighbours = loadNeighboursMatrix(file);
                         
                         //coordinates
                         fc = new JFileChooser();
                         fc.setDialogTitle("Load Coordinates Matrix");
                         returnVal = fc.showOpenDialog(view);
                         if (returnVal == JFileChooser.APPROVE_OPTION) {
                             file = fc.getSelectedFile();
                             coordinates = loadCoordinatesMatrix(file);
                             
                             // draw graph
                             animationTimer = null;
                             state = AnimationState.NOT_RUNNING;
                             view.getRunBtn().setText("Run");
                             model.setNeighboursMatrix(neighbours);
                             model.setCoordinatesMatrix(coordinates);
                             model.setCurrentColors(null);
                             model.setColorsIntegerMatrix(null);
                             model.setDSATURinfos(null);
                             model.setRLFinfos(null);
                             view.drawGraph();
                         } else {
                             System.out.println("Coordinates not loaded!" + View.LINE_END);
                         }
                     } else {
                         System.out.println("Graph not loaded!" + View.LINE_END);
                     }
 //                } else if (ae.getSource() == view.getLoadCoordBtn()){
 //                    fc = new JFileChooser();
 //                    returnVal = fc.showOpenDialog(view);
 //                    if (returnVal == JFileChooser.APPROVE_OPTION) {
 //                        file = fc.getSelectedFile();
 //                        loadCoordinatesMatrix(file);
 //                        //This is where a real application would open the file.
 //                        view.log("Coordinates loaded:" + View.LINE_END);
 //                        view.log(Model.matrixToString(model.getCoordinatesMatrix()));
 //                    } else {
 //                        view.log("Coordinates not loaded!" + View.LINE_END);
 //                    }
 //                } else if (ae.getSource() == view.getSaveBtn()){
 //                    model.setCurrentColors(null);
 //                    view.drawGraph();
                 } else if (ae.getSource() == view.getSaveBtn()){
                     ArrayList<ArrayList<Integer>> colorsMatrix = model.getColorsIntegerMatrix();
                         if(colorsMatrix != null && colorsMatrix.size()>0) {
                         fc = new JFileChooser();
                         fc.setDialogTitle("Save colors matrix");
                         returnVal = fc.showSaveDialog(view);
                         if (returnVal == JFileChooser.APPROVE_OPTION) {
                             file = fc.getSelectedFile();
 
                             String content = Model.arrayListToString(colorsMatrix.get(colorsMatrix.size()-1));
                             System.out.println(content);
                             try {
                                 FileUtility.saveFile(file, content);
                             } catch (FileNotFoundException ex) {
                                 System.out.println("File not saved!");
                                 Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                             }
                         } else {
                                 System.out.println("Colors not saved!" + View.LINE_END);
                         }
                     }
                 } else if (ae.getSource() == view.getAlgBox()){
                     algorithm = view.getAlgBox().getSelectedItem().toString();
                     CardLayout cl = (CardLayout)(view.getParamsPanel().getLayout());
                     if(algorithm.equals("DSATUR")) {
                         view.getParamsPanel().removeAll();
                         view.getParamsPanel().add(new DSATURInfoPanel(), DSATURPANEL);
                         cl.show(view.getParamsPanel(), DSATURPANEL);
                         view.revalidate();
                     }
                     else {
                         view.getParamsPanel().removeAll();
                         view.getParamsPanel().add(new RLFInfoPanel(), RLFPANEL);
                         cl.show(view.getParamsPanel(), RLFPANEL);
                         view.revalidate();
                     }
                     model.setCurrentColors(null);
                     view.drawGraph();
                     if(animationTimer!=null)
                         animationTimer.stop();
                     view.getRunBtn().setText("Run");
                     state = AnimationState.NOT_RUNNING;
                     
                 } else if (ae.getSource() == view.getRunBtn()){
                     switch(state) {
                         case NOT_RUNNING :
                             run(algorithm, interval*3);
                             view.getRunBtn().setText("PAUSE (running)");
                             state = AnimationState.RUNNING;
                             break;
                         case RUNNING :
                             animationTimer.stop();
                             view.getRunBtn().setText("Run (paused)");
                             state = AnimationState.PAUSED;
                             break;
                         case PAUSED :
                             animationTimer.setDelay(interval*3);
                             animationTimer.start();
                             view.getRunBtn().setText("PAUSE (running)");
                             state = AnimationState.RUNNING;
                             break;
                     }
                 } else if (ae.getSource() == view.getExitBtn()){
                     view.closeWindow();
                 }
             }
 
             @Override
             public void stateChanged(ChangeEvent ce) {
                 JSlider source = (JSlider)ce.getSource();
                 if (!source.getValueIsAdjusting()) {
                     interval = (int)source.getValue();
                 }
             }
         
             private void run(String alg, int interval) {
                 view.log("Coloring with " + alg + " algorithm" + " and " + interval + " ms interval" + "\n");
                 ArrayList<ArrayList<Integer>> colorsMatrix = null;
                 ArrayList<ArrayList<String>> nM = model.getNeighboursMatrix();
                 ArrayList<ArrayList<Integer>> neighboursMatrix = Model.convertStringToIntegerMatrix(nM);
                 
                 if(alg.equals(Coloring.ALGORITHM.DSATUR.toString())) {
                     DSATUROutput dso = Coloring.DSATUR(neighboursMatrix);
                     colorsMatrix = dso.getNodeColors();
                     model.setDSATURinfos(dso.getInfos());
                 } else if(alg.equals(Coloring.ALGORITHM.RLF.toString())) {
                     RLFOutput rlfo = Coloring.RLF(neighboursMatrix);
                     colorsMatrix = rlfo.getNodeColors();
                     model.setRLFinfos(rlfo.getInfos());
                 }
                 model.setColorsIntegerMatrix(colorsMatrix);
 
                 animationTimer = new Timer(interval, new AnimationActionListener());
                 animationTimer.setInitialDelay(0);
                 animationTimer.start();
             }
         }
         
         public class AnimationActionListener implements ActionListener {
             private int counter = 0;
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 if(counter < model.getColorsIntegerMatrix().size()) {
                     setCurrentColors();;
                     view.drawGraph();
                     if(algorithm.equals(Coloring.ALGORITHM.DSATUR.toString())) {
                         DSATURInfoItem infos = model.getDSATURinfos().get(counter);
                         JPanel infoPanel = new DSATURInfoPanel(infos);
                         view.getParamsPanel().removeAll();
                         view.getParamsPanel().add(infoPanel, DSATURPANEL);
                         CardLayout cl = (CardLayout)(view.getParamsPanel().getLayout());
                         cl.show(view.getParamsPanel(), DSATURPANEL);
                         view.revalidate();
                     } else if(algorithm.equals(Coloring.ALGORITHM.RLF.toString())) {
                         RLFInfoItem info = model.getRLFinfos().get(counter);
                         JPanel infoPanel = new RLFInfoPanel(info);
                         view.getParamsPanel().removeAll();
                         view.getParamsPanel().add(infoPanel, DSATURPANEL);
                         CardLayout cl = (CardLayout)(view.getParamsPanel().getLayout());
                         cl.show(view.getParamsPanel(), DSATURPANEL);
                         view.revalidate();
                     }
                     counter++;
                 } else {
                     animationTimer.stop();
                     state = AnimationState.NOT_RUNNING;
                     view.getRunBtn().setText("Run");
                 }
             }
             
             private void setCurrentColors() {
                 ArrayList<Integer> colors = model.getColorsIntegerMatrix().get(counter);
                 model.setCurrentColors(colors);
 //                System.out.println("Current colors:");
 //                System.out.println(model.arrayListToString(colors));
             }
             
         }
 }
