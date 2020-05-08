 import org.neuroph.core.NeuralNetwork;
 import org.neuroph.nnet.MultiLayerPerceptron;
 import org.neuroph.core.learning.TrainingSet;
 import org.neuroph.core.learning.TrainingElement;
 import org.neuroph.core.learning.SupervisedTrainingElement;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 
 import org.neuroph.util.TransferFunctionType;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import javax.swing.*;
 import java.awt.event.*;
 /**
 * This sample shows how to create, train, save and load simple Multi Layer Perceptron
 */
 		
 public class OCRnet extends JPanel implements MouseListener, ActionListener{
 	ButtonPixel pixelTable[][];
 	int xpixels = 8;
 	int ypixels = 8;
 	String addTrainingDataCmd = "AddToTrainingData";
     static final double[] four0 = { 
 	0,0,0,0,0,0,0,0,
 	0,1,0,0,0,1,0,0,
 	0,1,0,0,0,1,0,0,
 	0,1,1,1,1,1,0,0,
 	0,0,0,0,0,1,0,0,
 	0,0,0,0,0,1,0,0,
 	0,0,0,0,0,1,0,0,
 	0,0,0,0,0,0,0,0};
     
     static final double[] four1 = { 
 	0,0,0,0,0,0,0,0,
 	0,0,1,0,0,0,1,0,
 	0,0,1,0,0,0,1,0,
 	0,0,1,1,1,1,1,0,
 	0,0,0,0,0,0,1,0,
 	0,0,0,0,0,0,1,0,
 	0,0,0,0,0,0,1,0,
 	0,0,0,0,0,0,0,0};
     
     static final double[] four2 = { 
 	0,0,0,0,0,0,0,0,
 	0,0,0,1,0,0,0,1,
 	0,0,0,1,0,0,0,1,
 	0,0,0,1,1,1,1,1,
 	0,0,0,0,0,0,0,1,
 	0,0,0,0,0,0,0,1,
 	0,0,0,0,0,0,0,1,
 	0,0,0,0,0,0,0,0};
     
     static final double[] four3 = { 
 	0,0,0,0,0,0,0,0,
 	0,0,0,0,1,1,0,0,
 	0,0,0,1,0,1,0,0,
 	0,0,1,0,0,1,0,0,
 	0,1,1,1,1,1,0,0,
 	0,0,0,0,0,1,0,0,
 	0,0,0,0,0,1,0,0,
 	0,0,0,0,0,0,0,0};
     
     static final double[] four4 = { 
 	0,0,0,0,0,0,0,0,
 	0,0,0,0,0,1,1,0,
 	0,0,0,0,1,0,1,0,
 	0,0,0,1,0,0,1,0,
 	0,0,1,1,1,1,1,0,
 	0,0,0,0,0,0,1,0,
 	0,0,0,0,0,0,1,0,
 	0,0,0,0,0,0,0,0};
     
     static final double[] four5 = { 
 	0,0,0,0,1,0,0,0,
 	0,0,0,1,1,0,0,0,
 	0,0,1,0,1,0,0,0,
 	0,1,0,0,1,0,0,0,
 	1,1,1,1,1,1,0,0,
 	0,0,0,0,1,0,0,0,
 	0,0,0,0,1,0,0,0,
 	0,0,0,0,0,0,0,0};
     
     static final double[] four6 = { 
 	0,0,0,1,1,0,0,0,
 	0,0,1,0,1,0,0,0,
 	0,1,0,0,1,0,0,0,
 	0,1,0,0,1,0,0,0,
 	1,1,1,1,1,0,0,0,
 	0,0,0,0,1,0,0,0,
 	0,0,0,0,1,0,0,0,
 	0,0,0,0,0,0,0,0};
     
     static final double[] four7 = { 
 	0,0,0,0,0,1,0,0,
 	0,0,0,0,0,1,0,0,
 	0,0,0,0,1,1,0,0,
 	0,0,0,1,0,1,0,0,
 	0,1,1,1,1,1,0,0,
 	0,0,0,0,0,1,0,0,
 	0,0,0,0,0,1,0,0,
 	0,0,0,0,0,0,0,0};
     
     static final double[] h0 = {
 	0,0,0,0,0,0,0,0,
 	0,0,1,0,0,0,0,0,
 	0,0,1,0,0,0,0,0,
 	0,0,1,0,0,0,0,0,
 	0,0,1,1,1,1,0,0,
 	0,0,1,0,0,1,0,0,
 	0,0,1,0,0,1,0,0,
 	0,0,0,0,0,0,0,0 };
 
     static final double[] h1 = {
 	0,0,0,0,0,0,0,0,
 	0,1,0,0,0,0,0,0,
 	0,1,0,0,0,0,0,0,
 	0,1,0,0,0,0,0,0,
 	0,1,1,1,1,0,0,0,
 	0,1,0,0,1,0,0,0,
 	0,1,0,0,1,0,0,0,
 	0,0,0,0,0,0,0,0 };
     JFrame guiFrame;
     JPanel pixelMapPanel;
     GridBagLayout gridBag;
     public OCRnet() {
    	//init();
     }
     
     public void init(){
     	double[] pixelMap = new double[xpixels*ypixels];
     	
     	guiFrame = new JFrame();
     	guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     	guiFrame.setTitle("OCR Neural Network");
     	guiFrame.setSize(400,600);
     	pixelMapPanel = new JPanel();
 
     	gridBag = new GridBagLayout();
         GridBagConstraints cons = new GridBagConstraints();
         cons.fill = GridBagConstraints.BOTH;
         cons.gridx = 16;
         cons.gridy = 8;
 
     	pixelMapPanel.setLayout(gridBag);
   
     	pixelTable = new ButtonPixel[xpixels][ypixels];
     	for (int x = 0 ; x < xpixels ; x++)
     	{
     	    for (int y = 0 ; y < ypixels ; y++)
     	    {
     	    	pixelTable[x][y] = new ButtonPixel("",x, y, this.xpixels, this.ypixels, pixelMap);
     	    	pixelTable[x][y].addMouseListener(this);
     	    	cons.ipady = 2;
     	    	cons.ipadx = 2;
     	    	cons.gridx = x;
     	    	cons.gridy = y;
     	    	pixelTable[x][y].setVisible(true);
     	    	gridBag.setConstraints(pixelTable[x][y], cons);
     	    	pixelMapPanel.add (pixelTable [x] [y],cons);
     	    }
     	}
     	guiFrame.add(pixelMapPanel);
     	JButton addButton = new JButton("Add");
     	addButton.setActionCommand(addTrainingDataCmd);
     	addButton.addActionListener( this);
    
     	guiFrame.setBackground(Color.red);
     	guiFrame.setVisible(true);
     	
     }
 
     public void actionPerformed(ActionEvent e){
     	if( addTrainingDataCmd == e.getActionCommand()){
     		;
     	}
 	}
 
 	
 	public static void main(String[] args) {
 		try {
 			UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() );
 			//UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
 		 } catch (Exception e) {
 		            e.printStackTrace();
 		 }
 		OCRnet ocrNet = new OCRnet();
		ocrNet.init();
         // create training set (logical XOR function)
         TrainingSet<SupervisedTrainingElement> trainingSet = new TrainingSet<SupervisedTrainingElement>(64, 1);
         trainingSet.addElement(new SupervisedTrainingElement(four0, new double[]{1}));
         trainingSet.addElement(new SupervisedTrainingElement(four1, new double[]{1}));
         trainingSet.addElement(new SupervisedTrainingElement(four2, new double[]{1}));
         trainingSet.addElement(new SupervisedTrainingElement(four3, new double[]{1}));
         trainingSet.addElement(new SupervisedTrainingElement(four4, new double[]{1}));
         //trainingSet.addElement(new SupervisedTrainingElement(four5, new double[]{1}));
         trainingSet.addElement(new SupervisedTrainingElement(h0, new double[]{0}));
         trainingSet.addElement(new SupervisedTrainingElement(h1, new double[]{0}));
         TrainingSet<SupervisedTrainingElement> testSet = new TrainingSet<SupervisedTrainingElement>(64,1);
         testSet.addElement(new SupervisedTrainingElement(four5, new double[]{1}));
         testSet.addElement(new SupervisedTrainingElement(four6, new double[]{1}));
         testSet.addElement(new SupervisedTrainingElement(four7, new double[]{1}));
         testSet.addElement(new SupervisedTrainingElement(h1, new double[]{0}));
 
         // create multi layer perceptron
         MultiLayerPerceptron myMlPerceptron = new MultiLayerPerceptron(TransferFunctionType.TANH, 64, 8, 1);
         // learn the training set
         myMlPerceptron.learn(trainingSet);
 
         // test perceptron
         System.out.println("Testing trained neural network");
         testNeuralNetwork(myMlPerceptron, trainingSet);
 
         // save trained neural network
         myMlPerceptron.save("myOCR.nnet");
 
         // load saved neural network
         NeuralNetwork loadedMlPerceptron = NeuralNetwork.load("myOCR.nnet");
 
         // test loaded neural network
         System.out.println("Testing loaded neural network");
         testNeuralNetwork(loadedMlPerceptron, testSet);
 
     }
 
     public static void testNeuralNetwork(NeuralNetwork nnet, TrainingSet tset) {
     	List<TrainingElement> training_elements = tset.elements();
     	
         for(TrainingElement trainingElement : training_elements) {
 
             nnet.setInput(trainingElement.getInput());
             nnet.calculate();
             double[ ] networkOutput = nnet.getOutput();
             System.out.print("Input: " + Arrays.toString(trainingElement.getInput()) );
             System.out.println(" Output: " + Arrays.toString(networkOutput) );
 
         }
     }
     /////////////////////////////////////////////////////////////////
     //These are not used but are necessary for mouseListener
     public void mouseEntered (MouseEvent e)
     {
     	this.guiFrame.repaint();
     }
 
     public void mouseClicked(MouseEvent e){
     	this.guiFrame.repaint();
     }
 
     public void mouseExited (MouseEvent e)
     {
     	System.out.println("OCRnet");
     }
     
 
     public void mousePressed (MouseEvent e)
     {
     	System.out.println("OCRnet");
     }
     
 
     public void mouseReleased (MouseEvent e)
     {
     	System.out.println("OCRnet");
     }
 }
