 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.AdjustmentEvent;
 import java.awt.event.AdjustmentListener;
 import java.applet.*;
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollBar;
 import java.awt.*;
 import java.awt.event.KeyEvent;
 import javax.swing.JTextField;
 import javax.swing.JTextArea;
 import java.io.*;
 import java.net.*;
 
 
 public class RehostingUserInterface extends JDialog implements ActionListener {
 	
 	
 	
 	public JTextArea SourceCode = new JTextArea();
 	public JTextArea XMLCode = new JTextArea();
 	public JTextArea TargetCode = new JTextArea();
 	
 	JButton openSource = new JButton("Open Source");
 	JButton saveSource = new JButton("Save Source");
 	JButton toXML = new JButton("To XML");	
 	JButton openXML = new JButton("Open XML");
 	JButton saveXML = new JButton("Save XML");
 	JButton toMatlab = new JButton("To Matlab");
 	JButton toC = new JButton("  To C  ");
 	JButton toCPlus = new JButton(" To C++ ");
 	JButton toVHDL = new JButton(" To VHDL ");	
 	JButton toCUDA = new JButton(" To CUDA ");
 	JButton toOpenCL = new JButton(" To OpenCL");
 	JButton saveTarget = new JButton("Save Target");
 	
 	boolean MatlabFileOpen = false;
 	boolean CFileOpen = false;
 	boolean CPlusFileOpen = false;
 	boolean VHDLFileOpen = false;
 	
 	
 	int SourceCodeIndicator = 1;
 	CheckboxGroup LanguageGroup; 
 	Checkbox Matlab; 
 	Checkbox C; 
 	Checkbox CPlus; 
 	Checkbox VHDL; 
 	
 	int HardwarePlatformIndicator = 1;
 	CheckboxGroup HardwarePlatformGroup; 
 	Checkbox NVIDIA_GeForce_450; 
 	Checkbox NVIDIA_GeForce_280; 
 	
 	int FloatingFixedPointIndicator = 1;
 	CheckboxGroup FloatingFixedPointGroup; 
 	Checkbox Floating_Point; 
 	Checkbox Fixed_Point; 
 
 	
 	int TargetCodeIndicator = 1;
 	
 	String SourceMatlabCode = "";
 	String SourceCCode = "";
 	String SourceCPlusCode = "";
 	String SourceVHDLCode = "";
 	String TargetCodeOutput = "";
 	String XMLCodeOutput = "";
 
 	
 	JScrollBar vbarSource = null;
 	JScrollBar vbarXML = null;
 	JScrollBar vbarTarget = null;
 	
 	public SSP_Main p;
 	
 	public IntegerTextField wordLength = new IntegerTextField();
 	public IntegerTextField fractionLength = new IntegerTextField();
 	
 		
 	public class IntegerTextField extends JTextField
 	{
 		
 		final static String badchars = "`~!@#$%^&*()-_+=\\/\"':;?/>.<, ";
 		public void processKeyEvent(KeyEvent ev)
 		{
 			char c = ev.getKeyChar();
 			if ((Character.isLetter(c) && !ev.isAltDown()) || badchars.indexOf(c) > -1)
 			{
 				ev.consume();
 				return;
 			}
 			if (c == '-' && getDocument().getLength() > 0)
 				ev.consume();
 			else
 			{			
 				super.processKeyEvent(ev);
 				
 			}
 		}
 	}
 	
 	public void init()
 	{
 		
 	}
 	
   public RehostingUserInterface(JFrame frame, SSP_Main parent) {
     super(frame, true);
     
     //this.frame = frame;
 
     p = parent;
     setBounds(10,200,1250,650);
     setTitle("Semantic Rehosting based on CLBM");
     Container c = this.getContentPane();
     c.setLayout(new GridLayout(1,2));
     
 	JPanel SourceOpenSave = new JPanel();
 	SourceOpenSave.setLayout(new GridLayout(1,3));
 	Box SourceOpenSaveBlank = Box.createVerticalBox();
 	SourceOpenSaveBlank.add(Box.createGlue());
 	SourceOpenSaveBlank.add(new JLabel(" "));
 	SourceOpenSaveBlank.add(Box.createGlue());
 	Box openSourceBox = Box.createVerticalBox();
 	openSourceBox.add(Box.createGlue());
 	openSourceBox.add(openSource);
 	openSourceBox.add(Box.createGlue());	
 	Box saveSourceBox = Box.createVerticalBox();
 	saveSourceBox.add(Box.createGlue());
 	saveSourceBox.add(saveSource);
 	saveSourceBox.add(Box.createGlue());
 	SourceOpenSave.add(openSourceBox);
 	SourceOpenSave.add(SourceOpenSaveBlank);
 	SourceOpenSave.add(saveSourceBox);
 
 	
 	JPanel SourceOpenSaveDisplay = new JPanel();
 	SourceOpenSaveDisplay.setLayout(new GridLayout(4,1));	
 	JPanel sourceCodeShowPanel = new JPanel();
 	sourceCodeShowPanel.setLayout(new GridLayout(1,3));
 	Box sourceCodeShowBlank1 = Box.createVerticalBox();
 	sourceCodeShowBlank1.add(Box.createGlue());
 	sourceCodeShowBlank1.add(new JLabel(" "));
 	sourceCodeShowBlank1.add(Box.createGlue());
 	Box sourceCodeShowBlank2 = Box.createVerticalBox();
 	sourceCodeShowBlank2.add(Box.createGlue());
 	sourceCodeShowBlank2.add(new JLabel(" "));
 	sourceCodeShowBlank2.add(Box.createGlue());
 	Box sourceCodeShow = Box.createVerticalBox();
 	sourceCodeShow.add(Box.createGlue());
 	sourceCodeShow.add(new JLabel("Source Code"));
 	sourceCodeShow.add(Box.createGlue());
 	sourceCodeShowPanel.add(sourceCodeShowBlank1);
 	sourceCodeShowPanel.add(sourceCodeShow);
 	sourceCodeShowPanel.add(sourceCodeShowBlank2);	
 	Box SourceOpenSaveDisplayBlank = Box.createVerticalBox();
 	SourceOpenSaveDisplayBlank.add(Box.createGlue());
 	SourceOpenSaveDisplayBlank.add(new JLabel(" "));
 	SourceOpenSaveDisplayBlank.add(Box.createGlue());
 	SourceOpenSaveDisplay.add(sourceCodeShowPanel);
 	SourceOpenSaveDisplay.add(SourceOpenSaveDisplayBlank);
 	SourceOpenSaveDisplay.add(SourceOpenSave);	
 	LanguageGroup = new CheckboxGroup(); 
     Matlab = new Checkbox("Matlab", LanguageGroup,true); 
     C = new Checkbox("C", LanguageGroup,false); 
     CPlus = new Checkbox("C++", LanguageGroup,false); 
     VHDL = new Checkbox("VHDL", LanguageGroup,false);    
     JPanel SourceCodeTypeDisplay = new JPanel();
     SourceCodeTypeDisplay.setLayout(new GridLayout(1,5));
     Box SourceCodeTypeDisplayBlank = Box.createVerticalBox();
     SourceCodeTypeDisplayBlank.add(Box.createGlue());
     SourceCodeTypeDisplayBlank.add(new JLabel("Language:"));
     SourceCodeTypeDisplayBlank.add(Box.createGlue());
     SourceCodeTypeDisplay.add(SourceCodeTypeDisplayBlank);
     SourceCodeTypeDisplay.add(Matlab);
     SourceCodeTypeDisplay.add(C);
     SourceCodeTypeDisplay.add(CPlus);
     SourceCodeTypeDisplay.add(VHDL);
 	SourceOpenSaveDisplay.add(SourceCodeTypeDisplay);	
 	vbarSource = new JScrollBar(JScrollBar.VERTICAL, 0, 0, 0, 1000);
 	JPanel SourceCodePanel = new JPanel();
 	SourceCodePanel.setLayout(new BorderLayout());       
 	SourceCodePanel.add(SourceOpenSaveDisplay, BorderLayout.NORTH);
 	SourceCodePanel.add(SourceCode, BorderLayout.CENTER);
 	SourceCodePanel.add(vbarSource,BorderLayout.EAST);
 
 	JPanel toXMLpanel = new JPanel();
 	toXMLpanel.setLayout(new GridLayout(3,1));
 	Box toXMLpanelBlank1 = Box.createVerticalBox();
 	toXMLpanelBlank1.add(Box.createGlue());
 	toXMLpanelBlank1.add(new JLabel(" "));
 	toXMLpanelBlank1.add(Box.createGlue());
 	Box toXMLBox = Box.createVerticalBox();
 	toXMLBox.add(Box.createGlue());
 	toXMLBox.add(toXML);
 	toXMLBox.add(Box.createGlue());
 	Box toXMLpanelBlank2 = Box.createVerticalBox();
 	toXMLpanelBlank2.add(Box.createGlue());
 	toXMLpanelBlank2.add(new JLabel(" "));
 	toXMLpanelBlank2.add(Box.createGlue());
 	toXMLpanel.add(toXMLpanelBlank1);
 	toXMLpanel.add(toXMLBox);
 	toXMLpanel.add(toXMLpanelBlank2);
 	
 	JPanel FirstColumnDisplay= new JPanel();
 	FirstColumnDisplay.setLayout(new BorderLayout());
 	FirstColumnDisplay.add(SourceCodePanel,BorderLayout.CENTER);
 	FirstColumnDisplay.add(toXMLpanel,BorderLayout.EAST);
 	
 	
 	
 	JPanel XMLOpenSave = new JPanel();
 	XMLOpenSave.setLayout(new GridLayout(1,4));
 	Box XMLOpenSaveBlank = Box.createVerticalBox();
 	XMLOpenSaveBlank.add(Box.createGlue());
 	XMLOpenSaveBlank.add(new JLabel(" "));
 	XMLOpenSaveBlank.add(Box.createGlue());
 	Box openXMLBox = Box.createVerticalBox();
 	openXMLBox.add(Box.createGlue());
 	openXMLBox.add(openXML);
 	openXMLBox.add(Box.createGlue());	
 	Box saveXMLBox = Box.createVerticalBox();
 	saveXMLBox.add(Box.createGlue());
 	saveXMLBox.add(saveXML);
 	saveXMLBox.add(Box.createGlue());
 	XMLOpenSave.add(XMLOpenSaveBlank);
 	XMLOpenSave.add(openXMLBox);
 	XMLOpenSave.add(XMLOpenSaveBlank);
 	XMLOpenSave.add(saveXMLBox);
 
 	
 	JPanel XMLOpenSaveDisplay = new JPanel();
 	XMLOpenSaveDisplay.setLayout(new GridLayout(4,1));	
 	JPanel XMLCodeShowPanel = new JPanel();
 	XMLCodeShowPanel.setLayout(new GridLayout(1,3));
 	Box XMLCodeShowBlank1 = Box.createVerticalBox();
 	XMLCodeShowBlank1.add(Box.createGlue());
 	XMLCodeShowBlank1.add(new JLabel(" "));
 	XMLCodeShowBlank1.add(Box.createGlue());
 	Box XMLCodeShowBlank2 = Box.createVerticalBox();
 	XMLCodeShowBlank2.add(Box.createGlue());
 	XMLCodeShowBlank2.add(new JLabel(" "));
 	XMLCodeShowBlank2.add(Box.createGlue());
 	Box XMLCodeShow = Box.createVerticalBox();
 	XMLCodeShow.add(Box.createGlue());
 	XMLCodeShow.add(new JLabel("XML Code"));
 	XMLCodeShow.add(Box.createGlue());
 	XMLCodeShowPanel.add(XMLCodeShowBlank1);
 	XMLCodeShowPanel.add(XMLCodeShow);
 	XMLCodeShowPanel.add(XMLCodeShowBlank2);
 	Box XMLCodeTypeDisplay = Box.createVerticalBox();
 	XMLCodeTypeDisplay.add(Box.createGlue());
 	XMLCodeTypeDisplay.add(new JLabel(" "));
 	XMLCodeTypeDisplay.add(Box.createGlue());
 	Box XMLOpenSaveDisplayBlank = Box.createVerticalBox();
 	XMLOpenSaveDisplayBlank.add(Box.createGlue());
 	XMLOpenSaveDisplayBlank.add(new JLabel(" "));
 	XMLOpenSaveDisplayBlank.add(Box.createGlue());
 	XMLOpenSaveDisplay.add(XMLCodeShowPanel);
 	XMLOpenSaveDisplay.add(XMLOpenSaveDisplayBlank);
 	XMLOpenSaveDisplay.add(XMLOpenSave);	
 	XMLOpenSaveDisplay.add(XMLCodeTypeDisplay);
 	vbarXML = new JScrollBar(JScrollBar.VERTICAL, 0, 0, 0, 10000);	
 	JPanel XMLCodePanel = new JPanel();
 	XMLCodePanel.setLayout(new BorderLayout());       
 	XMLCodePanel.add(XMLOpenSaveDisplay, BorderLayout.NORTH);
 	XMLCodePanel.add(XMLCode, BorderLayout.CENTER);
 	XMLCodePanel.add(vbarXML,BorderLayout.EAST);
 
 	JPanel toTargetPanel = new JPanel();
 	toTargetPanel.setLayout(new GridLayout(7,1));
 	Box toTargetPanelBlank1 = Box.createVerticalBox();
 	toTargetPanelBlank1.add(Box.createGlue());
 	toTargetPanelBlank1.add(new JLabel(" "));
 	toTargetPanelBlank1.add(Box.createGlue());
 	Box toMatlabBox = Box.createVerticalBox();
 	toMatlabBox.add(Box.createGlue());
 	toMatlabBox.add(toMatlab);
 	toMatlabBox.add(Box.createGlue());
 	Box toCBox = Box.createVerticalBox();
 	toCBox.add(Box.createGlue());
 	toCBox.add(toC);
 	toCBox.add(Box.createGlue());
 	Box toCPlusBox = Box.createVerticalBox();
 	toCPlusBox.add(Box.createGlue());
 	toCPlusBox.add(toCPlus);
 	toCPlusBox.add(Box.createGlue());
 	Box toVHDLBox = Box.createVerticalBox();
 	toVHDLBox.add(Box.createGlue());
 	toVHDLBox.add(toVHDL);
 	toVHDLBox.add(Box.createGlue());
 	Box toCUDABox = Box.createVerticalBox();
 	toCUDABox.add(Box.createGlue());
 	toCUDABox.add(toCUDA);
 	toCUDABox.add(Box.createGlue());
 	Box toOpenCLBox = Box.createVerticalBox();
 	toOpenCLBox.add(Box.createGlue());
 	toOpenCLBox.add(toOpenCL);
 	toOpenCLBox.add(Box.createGlue());
 	/*Box toTargetPanelBlank2 = Box.createVerticalBox();
 	toTargetPanelBlank2.add(Box.createGlue());
 	toTargetPanelBlank2.add(new JLabel(" "));
 	toTargetPanelBlank2.add(Box.createGlue());*/
 	toTargetPanel.add(toTargetPanelBlank1);
 	toTargetPanel.add(toMatlabBox);
 	toTargetPanel.add(toCBox);
 	toTargetPanel.add(toCPlusBox);
 	toTargetPanel.add(toVHDLBox);
 	toTargetPanel.add(toCUDABox);
 	toTargetPanel.add(toOpenCLBox);
 	//toTargetPanel.add(toTargetPanelBlank2);
 	
 	JPanel SecondColumnDisplay= new JPanel();
 	SecondColumnDisplay.setLayout(new BorderLayout());
 	SecondColumnDisplay.add(XMLCodePanel,BorderLayout.CENTER);
 	SecondColumnDisplay.add(toTargetPanel,BorderLayout.EAST);
 	
 	
 		
 	
 	JPanel TargetOpenSave = new JPanel();
 	TargetOpenSave.setLayout(new GridLayout(1,3));
 	Box TargetOpenSaveBlank1 = Box.createVerticalBox();
 	TargetOpenSaveBlank1.add(Box.createGlue());
 	TargetOpenSaveBlank1.add(new JLabel(" "));
 	TargetOpenSaveBlank1.add(Box.createGlue());
 	Box TargetOpenSaveBlank2 = Box.createVerticalBox();
 	TargetOpenSaveBlank2.add(Box.createGlue());
 	TargetOpenSaveBlank2.add(new JLabel(" "));
 	TargetOpenSaveBlank2.add(Box.createGlue());
 	Box saveTargetBox = Box.createVerticalBox();
 	saveTargetBox.add(Box.createGlue());
 	saveTargetBox.add(saveTarget);
 	saveTargetBox.add(Box.createGlue());
 	TargetOpenSave.add(TargetOpenSaveBlank1);	
 	TargetOpenSave.add(saveTargetBox);
 	TargetOpenSave.add(TargetOpenSaveBlank2);
 	
 	
 	
 	JPanel TargetOpenSaveDisplay = new JPanel();
 	TargetOpenSaveDisplay.setLayout(new GridLayout(6,1));	
 	JPanel targetCodeShowPanel = new JPanel();
 	targetCodeShowPanel.setLayout(new GridLayout(1,3));
 	Box targetCodeShowBlank1 = Box.createVerticalBox();
 	targetCodeShowBlank1.add(Box.createGlue());
 	targetCodeShowBlank1.add(new JLabel(" "));
 	targetCodeShowBlank1.add(Box.createGlue());
 	Box targetCodeShowBlank2 = Box.createVerticalBox();
 	targetCodeShowBlank2.add(Box.createGlue());
 	targetCodeShowBlank2.add(new JLabel(" "));
 	targetCodeShowBlank2.add(Box.createGlue());
 	Box targetCodeShow = Box.createVerticalBox();
 	targetCodeShow.add(Box.createGlue());
 	targetCodeShow.add(new JLabel("Target Code"));
 	targetCodeShow.add(Box.createGlue());
 	targetCodeShowPanel.add(targetCodeShowBlank1);
 	targetCodeShowPanel.add(targetCodeShow);
 	targetCodeShowPanel.add(targetCodeShowBlank2);
 	Box TargetOpenSaveDisplayBlank = Box.createVerticalBox();
 	TargetOpenSaveDisplayBlank.add(Box.createGlue());
 	TargetOpenSaveDisplayBlank.add(new JLabel(" "));
 	TargetOpenSaveDisplayBlank.add(Box.createGlue());
 	TargetOpenSaveDisplay.add(targetCodeShowPanel);
 	TargetOpenSaveDisplay.add(TargetOpenSaveDisplayBlank);
 	TargetOpenSaveDisplay.add(TargetOpenSave);
 	HardwarePlatformGroup = new CheckboxGroup(); 
     NVIDIA_GeForce_450 = new Checkbox("GeForce 450", HardwarePlatformGroup,true); 
     NVIDIA_GeForce_280 = new Checkbox("GeForce 280", HardwarePlatformGroup,false);   
     JPanel HardwareTypeDisplay = new JPanel();
     HardwareTypeDisplay.setLayout(new GridLayout(1,3));
     Box HardwareTypeDisplayBlank = Box.createVerticalBox();
     HardwareTypeDisplayBlank.add(Box.createGlue());
     HardwareTypeDisplayBlank.add(new JLabel("NVIDIA GPU Platforms:"));
     HardwareTypeDisplayBlank.add(Box.createGlue());
     HardwareTypeDisplay.add(HardwareTypeDisplayBlank);
     HardwareTypeDisplay.add(NVIDIA_GeForce_450);
     HardwareTypeDisplay.add(NVIDIA_GeForce_280);
 	TargetOpenSaveDisplay.add(HardwareTypeDisplay);	
     FloatingFixedPointGroup = new CheckboxGroup(); 
     Fixed_Point = new Checkbox("Fixed Point", FloatingFixedPointGroup,true); 
     Floating_Point = new Checkbox("Floating Point", FloatingFixedPointGroup,false);
     JPanel FloatingFixedTypeDisplay = new JPanel();
     FloatingFixedTypeDisplay.setLayout(new GridLayout(1,3));
     Box FloatingFixedTypeDisplayBlank = Box.createVerticalBox();
     FloatingFixedTypeDisplayBlank.add(Box.createGlue());
     FloatingFixedTypeDisplayBlank.add(new JLabel("Fixed versus Floating:"));
     FloatingFixedTypeDisplayBlank.add(Box.createGlue());
     FloatingFixedTypeDisplay.add(FloatingFixedTypeDisplayBlank);
     FloatingFixedTypeDisplay.add(Fixed_Point);
     FloatingFixedTypeDisplay.add(Floating_Point);
     TargetOpenSaveDisplay.add(FloatingFixedTypeDisplay);
     JPanel WordlengthFractionlengthDisplay = new JPanel();
     WordlengthFractionlengthDisplay.setLayout(new GridLayout(1,4));
     Box WordlengthFractionlengthDisplayBlank1 = Box.createVerticalBox();
     WordlengthFractionlengthDisplayBlank1.add(Box.createGlue());
     WordlengthFractionlengthDisplayBlank1.add(new JLabel("Word Length:"));
     WordlengthFractionlengthDisplayBlank1.add(Box.createGlue());
     Box WordlengthFractionlengthDisplayBlank2 = Box.createVerticalBox();
     WordlengthFractionlengthDisplayBlank2.add(Box.createGlue());
     WordlengthFractionlengthDisplayBlank2.add(new JLabel("Fraction Length:"));
     WordlengthFractionlengthDisplayBlank2.add(Box.createGlue());
     WordlengthFractionlengthDisplay.add(WordlengthFractionlengthDisplayBlank1);
     WordlengthFractionlengthDisplay.add(wordLength);
     WordlengthFractionlengthDisplay.add(WordlengthFractionlengthDisplayBlank2); 
     WordlengthFractionlengthDisplay.add(fractionLength);
 	TargetOpenSaveDisplay.add(WordlengthFractionlengthDisplay);	
 	vbarTarget = new JScrollBar(JScrollBar.VERTICAL, 0, 0, 0, 1000);
 	TargetCode.setEditable(false);
 	JPanel TargetCodePanel = new JPanel();
 	TargetCodePanel.setLayout(new BorderLayout());       
 	TargetCodePanel.add(TargetOpenSaveDisplay, BorderLayout.NORTH);
 	TargetCodePanel.add(TargetCode, BorderLayout.CENTER);
 	TargetCodePanel.add(vbarTarget,BorderLayout.EAST);
 		
 	
 	JPanel ThirdColumnDisplay= new JPanel();
 	ThirdColumnDisplay.setLayout(new BorderLayout());
 	ThirdColumnDisplay.add(TargetCodePanel,BorderLayout.CENTER);
 	
 	
 	c.add(FirstColumnDisplay);
 	c.add(SecondColumnDisplay);
 	c.add(ThirdColumnDisplay);
 	
 	
 	openSource.addActionListener(this);
 	saveSource.addActionListener(this);
 	toXML.addActionListener(this);
 	openXML.addActionListener(this);
 	saveXML.addActionListener(this);
 	toMatlab.addActionListener(this);
 	toC.addActionListener(this);
 	toCPlus.addActionListener(this);
 	toVHDL.addActionListener(this);	
 	toCUDA.addActionListener(this);
 	toOpenCL.addActionListener(this);
 	saveTarget.addActionListener(this);
 	
 	vbarSource.addAdjustmentListener(new SourceAdjustmentListener());
 	vbarXML.addAdjustmentListener(new XMLAdjustmentListener());
 	vbarTarget.addAdjustmentListener(new TargetAdjustmentListener());
    
 
     
 
   }
 
   public static void main(String[] args) {
 	JDialog f = new NodeInputDialog(new JFrame(), new SSP_Main());
 	f.show();
 	
   }
   
   class SourceAdjustmentListener implements AdjustmentListener {
       public void adjustmentValueChanged(AdjustmentEvent e) {
       	String TempCode = "";
       	if (MatlabFileOpen)
       	{
       		TempCode = SourceMatlabCode;
       	}
       	if (CFileOpen)
 		{
       		TempCode = SourceCCode;				 
 		}
 		if (CPlusFileOpen)
 		{
 			TempCode = SourceCPlusCode;				 
 		}
 		if (VHDLFileOpen)
 		{
 			TempCode = SourceVHDLCode;
 		}
 		
       	String Output = "";       	
       	int i = 1;
       	while(! TempCode.isEmpty())
       	{
       		if (i > e.getValue())
       		{
       			if (TempCode.indexOf("\n") > -1)
       			{
       				Output = Output + TempCode.substring(0,TempCode.indexOf("\n") + 1);    		
       				TempCode = TempCode.substring(TempCode.indexOf("\n") + 1);
       			}
       			else
       			{
       				Output = Output + TempCode;    		
       				TempCode = "";
       			}
       		}
       		else
       		{
       			if (TempCode.indexOf("\n") > -1)
       				TempCode = TempCode.substring(TempCode.indexOf("\n") + 1);
       			else
       				TempCode = "";
       		}        		
       		i = i + 1;
       	}        	
         SourceCode.setText(Output);
         SourceCode.setEditable(true);       
         repaint();
       }
   }
   
   class XMLAdjustmentListener implements AdjustmentListener {
       public void adjustmentValueChanged(AdjustmentEvent e) {
       	
     	String TempCode = XMLCodeOutput;
       	String Output = "";
       	int i = 1;
       	while(! TempCode.isEmpty())
       	{
       		if (i > e.getValue())
       		{
       			if (TempCode.indexOf("\n") > -1)
       			{
       				Output = Output + TempCode.substring(0,TempCode.indexOf("\n") + 1);    		
       				TempCode = TempCode.substring(TempCode.indexOf("\n") + 1);
       			}
       			else
       			{
       				Output = Output + TempCode;    		
       				TempCode = "";
       			}
       		}
       		else
       		{
       			if (TempCode.indexOf("\n") > -1)
       				TempCode = TempCode.substring(TempCode.indexOf("\n") + 1);
       			else
       				TempCode = "";
       		}        		
       		i = i + 1;
       	}        	
         XMLCode.setText(Output);
         XMLCode.setEditable(true);
         repaint();
       }
   }
   
   class TargetAdjustmentListener implements AdjustmentListener {
       public void adjustmentValueChanged(AdjustmentEvent e) {
       	
     	String TempCode = TargetCodeOutput;
       	String Output = "";       	
       	int i = 1;
       	while(! TempCode.isEmpty())
       	{
       		if (i > e.getValue())
       		{
       			if (TempCode.indexOf("\n") > -1)
       			{
       				Output = Output + TempCode.substring(0,TempCode.indexOf("\n") + 1);    		
       				TempCode = TempCode.substring(TempCode.indexOf("\n") + 1);
       			}
       			else
       			{
       				Output = Output + TempCode;    		
       				TempCode = "";
       			}
       		}
       		else
       		{
       			if (TempCode.indexOf("\n") > -1)
       				TempCode = TempCode.substring(TempCode.indexOf("\n") + 1);
       			else
       				TempCode = "";
       		}        		
       		i = i + 1;
       	}        	
         TargetCode.setText(Output);
         TargetCode.setEditable(false);
         repaint();
       }
   }
   
   public void actionPerformed(ActionEvent evt)
   {
 	  
 	  if ((evt.getSource() == openSource))
 	  {			 
 		  int arrlen = 1000000;
 		  int elementIndex = 0;		  
 		  char[] infile = new char[arrlen];
 		  Frame parent = new Frame();
 		  FileDialog fd = new FileDialog(parent, "Please choose a source file for rehosting:", FileDialog.LOAD);
 		  fd.show();
 		  String selectedItem = fd.getFile();
 		  boolean selectedFlag = false;
 		  if (selectedItem == null)
 		  {
 			  selectedFlag = false;
 			  MatlabFileOpen = false;
 			  CFileOpen = false;
 			  CPlusFileOpen = false;
 			  VHDLFileOpen = false;
 			  SourceMatlabCode = "";
 			  SourceCCode = "";
 			  SourceCPlusCode = "";
 			  SourceVHDLCode = "";
 			  //SourceCodeTypeDisplay.setText("No File Selected!");
 			  SourceCode.setText("");
 		  }
 		  else
 		  {
 			  selectedFlag = true;
 			  while ((selectedFlag&&(fd.getFile().indexOf(".m") == -1))&&(selectedFlag&&(fd.getFile().indexOf(".c") == -1)&&(selectedFlag&&(fd.getFile().indexOf(".cc") == -1))&&(selectedFlag&&(fd.getFile().indexOf(".vhd") == -1))))
 			  {
 				  fd = new FileDialog(parent, "Wrong file selected. Please choose a source file for rehosting:", FileDialog.LOAD);
 				  fd.show();
 				  selectedItem = fd.getFile();
 				  if (selectedItem == null)
 				  {
 					  selectedFlag = false;
 					  break;
 				  }
 			  }
 				 
 			  if(selectedFlag)
 			  {
 				  if(selectedFlag&&(fd.getFile().indexOf(".m") > -1))
 				  {
 					  MatlabFileOpen = true;
 					  CFileOpen = false;
 					  CPlusFileOpen = false;
 					  VHDLFileOpen = false;
 				  }
 				  if((selectedFlag&&(fd.getFile().indexOf(".c") > -1))&&(fd.getFile().indexOf(".cc") == -1))
 				  {
 					  MatlabFileOpen = false;
 					  CFileOpen = true;
 					  CPlusFileOpen = false;
 					  VHDLFileOpen = false;
 				  }
 				  if(selectedFlag&&(fd.getFile().indexOf(".cc") > -1))
 				  {
 					  MatlabFileOpen = false;
 					  CFileOpen = false;
 					  CPlusFileOpen = true;
 					  VHDLFileOpen = false;
 				  }			  
 				  if(selectedFlag&&(fd.getFile().indexOf(".vhd") > -1))
 				  {
 					  MatlabFileOpen = false;
 					  CFileOpen = false;
 					  CPlusFileOpen = false;
 					  VHDLFileOpen = true;
 				  }
 				  
 				  File ffile = new File(fd.getDirectory() + File.separator + fd.getFile());
 				  try
 				  {
 					  FileInputStream fis = new FileInputStream(ffile);
 					  BufferedReader bis = new BufferedReader(new InputStreamReader(fis));
 					  				  
 					  try
 					  {								  		  
 						  int filelength = bis.read(infile);					  
 						  String filestring = new String(infile, 0, filelength).toString();						  		
 						  SourceMatlabCode = filestring;
 					  }
 					  catch (IOException iox)
 					  {
 						  iox.printStackTrace();					  
 					  }
 				  }
 				  catch (FileNotFoundException fnf)
 				  {
 					  fnf.printStackTrace();				  
 				  }
 				  				  
 				  if (MatlabFileOpen)
 				  {
 					  LanguageGroup.setSelectedCheckbox(Matlab);
 				  }
 				  if (CFileOpen)
 				  {
 					  LanguageGroup.setSelectedCheckbox(C);
 				  }
 				  if (CPlusFileOpen)
 				  {
 					  LanguageGroup.setSelectedCheckbox(CPlus);
 				  }
 				  if (VHDLFileOpen)
 				  {
 					  LanguageGroup.setSelectedCheckbox(VHDL);
 				  }
 				  /*String Temp = SourceMatlabCode;
 		          int Linelength = 0;
 		          while(! Temp.isEmpty())
 		          {
 		        	if (Temp.indexOf("\n") > -1)
 		    		{		       			
 		   				Temp = Temp.substring(Temp.indexOf("\n") + 1);
 		   				Linelength = Linelength + 1;
 		   			}   
 	        		else
 	        			Temp = "";   				        	
 		          }*/		          		          		  
 				  SourceCode.setText(SourceMatlabCode);		
 				  			  
 				  if (CFileOpen)
 				  {
 					  SourceCCode = SourceMatlabCode;
 					  SourceMatlabCode = "";					 
 				  }
 				  if (CPlusFileOpen)
 				  {
 					  SourceCPlusCode = SourceMatlabCode;
 					  SourceMatlabCode = "";					 
 				  }
 				  if (VHDLFileOpen)
 				  {
 					  SourceVHDLCode = SourceMatlabCode;
 					  SourceMatlabCode = "";					 
 				  }
 				  
 			  }
 			  else
 			  {
 				  MatlabFileOpen = false;
 				  CFileOpen = false;
 				  CPlusFileOpen = false;
 				  VHDLFileOpen = false;
 				  SourceMatlabCode = "";
 				  SourceCCode = "";
 				  SourceCPlusCode = "";
 				  SourceVHDLCode = "";
 				 // SourceCodeTypeDisplay.setText("No File Selected!");
 				  SourceCode.setText("");
 			  }
 			  
 		  }
 		  
 	  }
 	  
 	  if ((evt.getSource() == saveSource))
 	  {
 		  if (MatlabFileOpen)
 		  {
 			  Frame parent = new Frame();
   			  FileDialog fd = new FileDialog(parent, "Please choose a directory:", FileDialog.SAVE);
 			  fd.show();
 			  String selectedItem = fd.getFile();
 			  if (selectedItem == null)
 			  {
 					  // no file selected
 			  }
 			  else
 			  {		
 				  File ffile = null;	
 				  String fileName = fd.getFile().toString();
 				  if(fileName.indexOf(".m") > -1)
 					  ffile = new File(fd.getDirectory() + File.separator + fd.getFile());
 				  else
 					  ffile = new File(fd.getDirectory() + File.separator + fd.getFile() + ".m");
 				  BufferedWriter bufferedWriter = null;
 				  try 
 				  {
 			          //Construct the BufferedWriter object
 					  bufferedWriter = new BufferedWriter(new FileWriter(ffile));
 					  //Start writing to the output stream
 					  bufferedWriter.write(SourceCode.getText());
 						  										  
 			       } 
 				  catch (FileNotFoundException ex) 
 				  {		            
 					  ex.printStackTrace();		        
 				  } 
 				  catch (IOException ex) 
 				  {
 					  ex.printStackTrace();
 				  } 
 				  finally 
 				  {
 					  //Close the BufferedWriter
 					  try 
 					  {
 						  if (bufferedWriter != null) 
 						  {
 							  bufferedWriter.flush();
 							  bufferedWriter.close();
 						   }
 			           } 
 					  catch (IOException ex) 
 					  {
 						  ex.printStackTrace();
 					   }
 				    }
 			  }  			  
 		  }
 		  if (CFileOpen)
 		  {
 			  Frame parent = new Frame();
   			  FileDialog fd = new FileDialog(parent, "Please choose a directory:", FileDialog.SAVE);
 			  fd.show();
 			  String selectedItem = fd.getFile();
 			  if (selectedItem == null)
 			  {
 					  // no file selected
 			  }
 			  else
 			  {		
 				  File ffile = null;	
 				  String fileName = fd.getFile().toString();
 				  if(fileName.indexOf(".c") > -1)
 					  ffile = new File(fd.getDirectory() + File.separator + fd.getFile());
 				  else
 					  ffile = new File(fd.getDirectory() + File.separator + fd.getFile() + ".c");
 				  BufferedWriter bufferedWriter = null;
 				  try 
 				  {
 			          //Construct the BufferedWriter object
 					  bufferedWriter = new BufferedWriter(new FileWriter(ffile));
 					  //Start writing to the output stream
 					  bufferedWriter.write(SourceCode.getText());
 						  										  
 			       } 
 				  catch (FileNotFoundException ex) 
 				  {		            
 					  ex.printStackTrace();		        
 				  } 
 				  catch (IOException ex) 
 				  {
 					  ex.printStackTrace();
 				  } 
 				  finally 
 				  {
 					  //Close the BufferedWriter
 					  try 
 					  {
 						  if (bufferedWriter != null) 
 						  {
 							  bufferedWriter.flush();
 							  bufferedWriter.close();
 						   }
 			           } 
 					  catch (IOException ex) 
 					  {
 						  ex.printStackTrace();
 					   }
 				    }
 			  }  			  
 		  }
 		  if (CPlusFileOpen)
 		  {
 			  Frame parent = new Frame();
   			  FileDialog fd = new FileDialog(parent, "Please choose a directory:", FileDialog.SAVE);
 			  fd.show();
 			  String selectedItem = fd.getFile();
 			  if (selectedItem == null)
 			  {
 					  // no file selected
 			  }
 			  else
 			  {		
 				  File ffile = null;	
 				  String fileName = fd.getFile().toString();
 				  if(fileName.indexOf(".cc") > -1)
 					  ffile = new File(fd.getDirectory() + File.separator + fd.getFile());
 				  else
 					  ffile = new File(fd.getDirectory() + File.separator + fd.getFile() + ".cc");
 				  BufferedWriter bufferedWriter = null;
 				  try 
 				  {
 			          //Construct the BufferedWriter object
 					  bufferedWriter = new BufferedWriter(new FileWriter(ffile));
 					  //Start writing to the output stream
 					  bufferedWriter.write(SourceCode.getText());
 						  										  
 			       } 
 				  catch (FileNotFoundException ex) 
 				  {		            
 					  ex.printStackTrace();		        
 				  } 
 				  catch (IOException ex) 
 				  {
 					  ex.printStackTrace();
 				  } 
 				  finally 
 				  {
 					  //Close the BufferedWriter
 					  try 
 					  {
 						  if (bufferedWriter != null) 
 						  {
 							  bufferedWriter.flush();
 							  bufferedWriter.close();
 						   }
 			           } 
 					  catch (IOException ex) 
 					  {
 						  ex.printStackTrace();
 					   }
 				    }
 			  }  			  
 		  }
 		  if (VHDLFileOpen)
 		  {
 			  Frame parent = new Frame();
   			  FileDialog fd = new FileDialog(parent, "Please choose a directory:", FileDialog.SAVE);
 			  fd.show();
 			  String selectedItem = fd.getFile();
 			  if (selectedItem == null)
 			  {
 					  // no file selected
 			  }
 			  else
 			  {		
 				  File ffile = null;	
 				  String fileName = fd.getFile().toString();
 				  if(fileName.indexOf(".vhd") > -1)
 					  ffile = new File(fd.getDirectory() + File.separator + fd.getFile());
 				  else
 					  ffile = new File(fd.getDirectory() + File.separator + fd.getFile() + ".vhd");
 				  BufferedWriter bufferedWriter = null;
 				  try 
 				  {
 			          //Construct the BufferedWriter object
 					  bufferedWriter = new BufferedWriter(new FileWriter(ffile));
 					  //Start writing to the output stream
 					  bufferedWriter.write(SourceCode.getText());
 						  										  
 			       } 
 				  catch (FileNotFoundException ex) 
 				  {		            
 					  ex.printStackTrace();		        
 				  } 
 				  catch (IOException ex) 
 				  {
 					  ex.printStackTrace();
 				  } 
 				  finally 
 				  {
 					  //Close the BufferedWriter
 					  try 
 					  {
 						  if (bufferedWriter != null) 
 						  {
 							  bufferedWriter.flush();
 							  bufferedWriter.close();
 						   }
 			           } 
 					  catch (IOException ex) 
 					  {
 						  ex.printStackTrace();
 					   }
 				    }
 			  }  			  
 		  }	
 		  if (!(MatlabFileOpen||CFileOpen||CPlusFileOpen||VHDLFileOpen))
 		  {
 			  Frame parent = new Frame();
   			  FileDialog fd = new FileDialog(parent, "Please choose a directory:", FileDialog.SAVE);
 			  fd.show();
 			  String selectedItem = fd.getFile();
 			  if (selectedItem == null)
 			  {
 					  // no file selected
 			  }
 			  else
 			  {		
 				  File ffile = null;	
 				  String fileName = fd.getFile().toString();
 				  ffile = new File(fd.getDirectory() + File.separator + fd.getFile());
 
 				  BufferedWriter bufferedWriter = null;
 				  try 
 				  {
 			          //Construct the BufferedWriter object
 					  bufferedWriter = new BufferedWriter(new FileWriter(ffile));
 					  //Start writing to the output stream
 					  bufferedWriter.write(SourceCode.getText());
 						  										  
 			       } 
 				  catch (FileNotFoundException ex) 
 				  {		            
 					  ex.printStackTrace();		        
 				  } 
 				  catch (IOException ex) 
 				  {
 					  ex.printStackTrace();
 				  } 
 				  finally 
 				  {
 					  //Close the BufferedWriter
 					  try 
 					  {
 						  if (bufferedWriter != null) 
 						  {
 							  bufferedWriter.flush();
 							  bufferedWriter.close();
 						   }
 			           } 
 					  catch (IOException ex) 
 					  {
 						  ex.printStackTrace();
 					   }
 				    }
 			  }  			  
 		  }
 		  
 	  }
 	  if ((evt.getSource() == openXML))
 	  {			 
 		  int arrlen = 1000000;
 		  int elementIndex = 0;		  
 		  char[] infile = new char[arrlen];
 		  Frame parent = new Frame();
 		  FileDialog fd = new FileDialog(parent, "Please choose an XML file for semantic inference:", FileDialog.LOAD);
 		  fd.show();
 		  String selectedItem = fd.getFile();
 		  boolean selectedFlag = false;
 		  if (selectedItem == null)
 		  {
 			  selectedFlag = false;			  
 			  XMLCodeOutput = "";		  
 			  XMLCode.setText("");
 		  }
 		  else
 		  {
 			  selectedFlag = true;
 			  while (selectedFlag&&(fd.getFile().indexOf(".xml") == -1))
 			  {
 				  fd = new FileDialog(parent, "Wrong file selected. Please choose an XML file for semantic inference:", FileDialog.LOAD);
 				  fd.show();
 				  selectedItem = fd.getFile();
 				  if (selectedItem == null)
 				  {
 					  selectedFlag = false;
 					  break;
 				  }
 			  }
 				 
 			  if(selectedFlag)
 			  {
 				  File ffile = new File(fd.getDirectory() + File.separator + fd.getFile());
 				  try
 				  {
 					  FileInputStream fis = new FileInputStream(ffile);
 					  BufferedReader bis = new BufferedReader(new InputStreamReader(fis));
 					  				  
 					  try                      
 					  {								  		  
 						  int filelength = bis.read(infile);
						  String filestring = new String(infile, 0, filelength).toString();						  		
 						  XMLCodeOutput = filestring;
 					  }
 					  catch (IOException iox)
 					  {
 						  iox.printStackTrace();					  
 					  }
 				  }
 				  catch (FileNotFoundException fnf)
 				  {
 					  fnf.printStackTrace();				  
 				  }
 			        		          		  
 				  XMLCode.setText(XMLCodeOutput);					  
 				  
 			  }
 			  else
 			  {
 				  XMLCodeOutput = "";		  
 				  XMLCode.setText("");
 			  }
 			  
 		  }
 		  
 	  }
 	  if ((evt.getSource() == saveXML))
 	  {
 		  Frame parent = new Frame();
 		  FileDialog fd = new FileDialog(parent, "Please choose a directory:", FileDialog.SAVE);
 		  fd.show();
 		  String selectedItem = fd.getFile();
 		  if (selectedItem == null)
 		  {
 				  // no file selected
 		  }
 		  else
 		  {		
 			  File ffile = null;	
 			  String fileName = fd.getFile().toString();
 			  if(fileName.indexOf(".xml") > -1)
 				  ffile = new File(fd.getDirectory() + File.separator + fd.getFile());
 			  else
 				  ffile = new File(fd.getDirectory() + File.separator + fd.getFile() + ".xml");
 			  BufferedWriter bufferedWriter = null;
 			  try 
 			  {
 		          //Construct the BufferedWriter object
 				  bufferedWriter = new BufferedWriter(new FileWriter(ffile));
 				  //Start writing to the output stream
 				  bufferedWriter.write(XMLCode.getText());
 					  										  
 		       } 
 			  catch (FileNotFoundException ex) 
 			  {		            
 				  ex.printStackTrace();		        
 			  } 
 			  catch (IOException ex) 
 			  {
 				  ex.printStackTrace();
 			  } 
 			  finally 
 			  {
 				  //Close the BufferedWriter
 				  try 
 				  {
 					  if (bufferedWriter != null) 
 					  {
 						  bufferedWriter.flush();
 						  bufferedWriter.close();
 					   }
 		           } 
 				  catch (IOException ex) 
 				  {
 					  ex.printStackTrace();
 				   }
 			    }
 		  }  
 		  
 	  }
 	  if ((evt.getSource() == saveTarget))
 	  {
 		  if (TargetCodeIndicator == 1)
 		  {
 			  Frame parent = new Frame();
   			  FileDialog fd = new FileDialog(parent, "Please choose a directory:", FileDialog.SAVE);
 			  fd.show();
 			  String selectedItem = fd.getFile();
 			  if (selectedItem == null)
 			  {
 					  // no file selected
 			  }
 			  else
 			  {		
 				  File ffile = null;	
 				  String Code = TargetCode.getText().toString();
 					String fileName = fd.getFile().toString();
 					if(fileName.indexOf(".m") > -1)
 						ffile = new File(fd.getDirectory() + File.separator + fd.getFile());
 					else
 						ffile = new File(fd.getDirectory() + File.separator + fd.getFile() + ".m");
 					BufferedWriter bufferedWriter = null;
 					try 
 					{
 				          //Construct the BufferedWriter object
 						bufferedWriter = new BufferedWriter(new FileWriter(ffile));
 						  //Start writing to the output stream						
 						String CodeSaveTemp = Code;
 						if (Code.indexOf("function ")==-1)
 							bufferedWriter.write(Code);
 						else
 						{
 							CodeSaveTemp = Code.substring(0,Code.indexOf("function "));
 							bufferedWriter.write(CodeSaveTemp);
 							bufferedWriter.flush();
 							bufferedWriter.close();
 							Code = Code.substring(Code.indexOf("function "));
 							CodeSaveTemp = Code.substring(Code.indexOf("function ") + 9);
 							String FunctionFileName = "";
 							while(CodeSaveTemp.indexOf("function ")>-1)
 							{
 								FunctionFileName = Code.substring(Code.indexOf("=") + 1, Code.indexOf("("));
 								Code = CodeSaveTemp.substring(CodeSaveTemp.indexOf("function "));
 								CodeSaveTemp = "function " + CodeSaveTemp.substring(0, CodeSaveTemp.indexOf("function "));	
 								//System.out.print("fuck:" + CodeSaveTemp + "\n");
 								ffile = new File(fd.getDirectory() + File.separator + FunctionFileName + ".m");
 								bufferedWriter = new BufferedWriter(new FileWriter(ffile));																						
 								bufferedWriter.write(CodeSaveTemp);
 								bufferedWriter.flush();
 								bufferedWriter.close();
 								CodeSaveTemp = Code.substring(Code.indexOf("function ") + 9);
 							}
 							FunctionFileName = Code.substring(Code.indexOf("=") + 1, Code.indexOf("("));
 							ffile = new File(fd.getDirectory() + File.separator + FunctionFileName + ".m");
 							bufferedWriter = new BufferedWriter(new FileWriter(ffile));
 							bufferedWriter.write(Code);						
 							bufferedWriter.flush();
 							bufferedWriter.close();
 							//
 						}
 						
 						  						
 						  
 				     } 
 					catch (FileNotFoundException ex) 
 					{		            
 						ex.printStackTrace();		        
 					} 
 					catch (IOException ex) 
 					{
 						ex.printStackTrace();
 					} 
 					finally 
 					  {
 						  //Close the BufferedWriter
 						  try 
 						  {
 							  if (bufferedWriter != null) 
 							  {
 								  bufferedWriter.flush();
 								  bufferedWriter.close();
 							   }
 				           } 
 						  catch (IOException ex) 
 						  {
 							  ex.printStackTrace();
 						   }
 					    }
 			  }  			  
 		  }
 		  if (TargetCodeIndicator == 2)
 		  {
 			  Frame parent = new Frame();
   			  FileDialog fd = new FileDialog(parent, "Please choose a directory:", FileDialog.SAVE);
 			  fd.show();
 			  String selectedItem = fd.getFile();
 			  if (selectedItem == null)
 			  {
 					  // no file selected
 			  }
 			  else
 			  {		
 				  File ffile = null;	
 				  String fileName = fd.getFile().toString();
 				  if(fileName.indexOf(".c") > -1)
 					  ffile = new File(fd.getDirectory() + File.separator + fd.getFile());
 				  else
 					  ffile = new File(fd.getDirectory() + File.separator + fd.getFile() + ".c");
 				  BufferedWriter bufferedWriter = null;
 				  try 
 				  {
 			          //Construct the BufferedWriter object
 					  bufferedWriter = new BufferedWriter(new FileWriter(ffile));
 					  //Start writing to the output stream
 					  bufferedWriter.write(TargetCode.getText());
 						  										  
 			       } 
 				  catch (FileNotFoundException ex) 
 				  {		            
 					  ex.printStackTrace();		        
 				  } 
 				  catch (IOException ex) 
 				  {
 					  ex.printStackTrace();
 				  } 
 				  finally 
 				  {
 					  //Close the BufferedWriter
 					  try 
 					  {
 						  if (bufferedWriter != null) 
 						  {
 							  bufferedWriter.flush();
 							  bufferedWriter.close();
 						   }
 			           } 
 					  catch (IOException ex) 
 					  {
 						  ex.printStackTrace();
 					   }
 				    }
 			  }  			  
 		  }
 		  if (TargetCodeIndicator == 3)
 		  {
 			  Frame parent = new Frame();
   			  FileDialog fd = new FileDialog(parent, "Please choose a directory:", FileDialog.SAVE);
 			  fd.show();
 			  String selectedItem = fd.getFile();
 			  if (selectedItem == null)
 			  {
 					  // no file selected
 			  }
 			  else
 			  {		
 				  File ffile = null;	
 				  String fileName = fd.getFile().toString();
 				  if(fileName.indexOf(".cc") > -1)
 					  ffile = new File(fd.getDirectory() + File.separator + fd.getFile());
 				  else
 					  ffile = new File(fd.getDirectory() + File.separator + fd.getFile() + ".cc");
 				  BufferedWriter bufferedWriter = null;
 				  try 
 				  {
 			          //Construct the BufferedWriter object
 					  bufferedWriter = new BufferedWriter(new FileWriter(ffile));
 					  //Start writing to the output stream
 					  bufferedWriter.write(TargetCode.getText());
 						  										  
 			       } 
 				  catch (FileNotFoundException ex) 
 				  {		            
 					  ex.printStackTrace();		        
 				  } 
 				  catch (IOException ex) 
 				  {
 					  ex.printStackTrace();
 				  } 
 				  finally 
 				  {
 					  //Close the BufferedWriter
 					  try 
 					  {
 						  if (bufferedWriter != null) 
 						  {
 							  bufferedWriter.flush();
 							  bufferedWriter.close();
 						   }
 			           } 
 					  catch (IOException ex) 
 					  {
 						  ex.printStackTrace();
 					   }
 				    }
 			  }  			  
 		  }
 		  if (TargetCodeIndicator == 4)
 		  {
 			  Frame parent = new Frame();
   			  FileDialog fd = new FileDialog(parent, "Please choose a directory:", FileDialog.SAVE);
 			  fd.show();
 			  String selectedItem = fd.getFile();
 			  if (selectedItem == null)
 			  {
 					  // no file selected
 			  }
 			  else
 			  {		
 				  File ffile = null;	
 				  String fileName = fd.getFile().toString();
 				  if(fileName.indexOf(".vhd") > -1)
 					  ffile = new File(fd.getDirectory() + File.separator + fd.getFile());
 				  else
 					  ffile = new File(fd.getDirectory() + File.separator + fd.getFile() + ".vhd");
 				  BufferedWriter bufferedWriter = null;
 				  try 
 				  {
 			          //Construct the BufferedWriter object
 					  bufferedWriter = new BufferedWriter(new FileWriter(ffile));
 					  //Start writing to the output stream
 					  bufferedWriter.write(TargetCode.getText());
 						  										  
 			       } 
 				  catch (FileNotFoundException ex) 
 				  {		            
 					  ex.printStackTrace();		        
 				  } 
 				  catch (IOException ex) 
 				  {
 					  ex.printStackTrace();
 				  } 
 				  finally 
 				  {
 					  //Close the BufferedWriter
 					  try 
 					  {
 						  if (bufferedWriter != null) 
 						  {
 							  bufferedWriter.flush();
 							  bufferedWriter.close();
 						   }
 			           } 
 					  catch (IOException ex) 
 					  {
 						  ex.printStackTrace();
 					  }
 				    }
 			  }  			  
 		  }
 		  if (TargetCodeIndicator == 5)
 		  {
 			  Frame parent = new Frame();
   			  FileDialog fd = new FileDialog(parent, "Please choose a directory:", FileDialog.SAVE);
 			  fd.show();
 			  String selectedItem = fd.getFile();
 			  if (selectedItem == null)
 			  {
 					  // no file selected
 			  }
 			  else
 			  {		
 				  File ffile = null;	
 				  String fileName = fd.getFile().toString();
 				  if(fileName.indexOf(".cuda") > -1)
 					  ffile = new File(fd.getDirectory() + File.separator + fd.getFile());
 				  else
 					  ffile = new File(fd.getDirectory() + File.separator + fd.getFile() + ".cuda");
 				  BufferedWriter bufferedWriter = null;
 				  try 
 				  {
 			          //Construct the BufferedWriter object
 					  bufferedWriter = new BufferedWriter(new FileWriter(ffile));
 					  //Start writing to the output stream
 					  bufferedWriter.write(TargetCode.getText());
 						  										  
 			       } 
 				  catch (FileNotFoundException ex) 
 				  {		            
 					  ex.printStackTrace();		        
 				  } 
 				  catch (IOException ex) 
 				  {
 					  ex.printStackTrace();
 				  } 
 				  finally 
 				  {
 					  //Close the BufferedWriter
 					  try 
 					  {
 						  if (bufferedWriter != null) 
 						  {
 							  bufferedWriter.flush();
 							  bufferedWriter.close();
 						   }
 			           } 
 					  catch (IOException ex) 
 					  {
 						  ex.printStackTrace();
 					   }
 				    }
 			  }  			  
 		  }
 		  
 		  
 	  }
 	 	  
 	 if ((evt.getSource() == toXML))
 	 {
 		 if (LanguageGroup.getSelectedCheckbox() == Matlab)
 		 {
 			 SourceCodeIndicator = 1;
 			 SourceMatlabCode = SourceCode.getText();
 		 }			 
 		 if (LanguageGroup.getSelectedCheckbox() == C)
 		 {
 			 SourceCodeIndicator = 2;
 			 SourceCCode = SourceCode.getText();
 		 }
 		 if (LanguageGroup.getSelectedCheckbox() == CPlus)
 		 {
 			 SourceCodeIndicator = 3;
 			 SourceCPlusCode = SourceCode.getText();
 		 }
 		 if (LanguageGroup.getSelectedCheckbox() == VHDL)
 		 {
 			 SourceCodeIndicator = 4;
 			 SourceVHDLCode = SourceCode.getText();
 		 }
 		 
 		 String Temp = SourceCode.getText();	
 		 Temp = Temp.replace('\r', ' ');
 	     String TempOutput = "";	      
 	     while(Temp.indexOf("\"") > -1)
 	     {
 	    	 TempOutput = Temp.substring(0,Temp.indexOf("\""));  
 	    	 TempOutput = TempOutput + " CLBM_QOUTE ";	    		  
 	    	 Temp = TempOutput + Temp.substring(Temp.indexOf("\"") + 1);
 	     }	      
 	     TempOutput = "";
 	     while(! Temp.isEmpty())
 	     {
 	    	 if (Temp.indexOf("\n") > -1)
 	    	 {	    		  
 	    		 TempOutput = Temp.substring(0,Temp.indexOf("\n"));    		    		  	  		  
 	    		 Temp = Temp.substring(Temp.indexOf("\n") + 1);
 	    		 try {
 	    			 p.getAppletContext().showDocument
 	    			 (new URL("javascript:getSourceCode(\"" + TempOutput +"\")"));
 	    			 }
 	    			 catch (MalformedURLException me) { }  
 	    		  	    		 
 	    	 }   
 	    	 else
 	    	 {
 	    		 TempOutput = Temp;	    		 
 	    		 try {
 	    			 p.getAppletContext().showDocument
 	    			 (new URL("javascript:getSourceCode(\"" + TempOutput +"\")"));
 	    			 }
 	    			 catch (MalformedURLException me) { }  
 	    		 Temp = "";
 	    	 }
 	     }  
 	     String LanguageType = "";
 	     if(SourceCodeIndicator == 1)
 	    	 LanguageType = "Matlab";
 	     if(SourceCodeIndicator == 2)
 	    	 LanguageType = "C";
 	     if(SourceCodeIndicator == 3)
 	    	 LanguageType = "C++";
 	     if(SourceCodeIndicator == 4)
 	    	 LanguageType = "VHDL";
 	     try {
 			 p.getAppletContext().showDocument
 			 (new URL("javascript:passXMLCode(\"" + LanguageType +"\")"));
 			 }
 			 catch (MalformedURLException me) { }			 		 		 
 	}
 	
 	if (evt.getSource() == toMatlab)
 	{
 		TargetCodeIndicator = 1;
 		String Temp = XMLCode.getText();
 	    String TempOutput = "";	      
 	    while(Temp.indexOf("\"") > -1)
 	    {
 	    	TempOutput = Temp.substring(0,Temp.indexOf("\""));  
   		    TempOutput = TempOutput + " CLBM_QOUTE ";	    		  
   		    Temp = TempOutput + Temp.substring(Temp.indexOf("\"") + 1);
 	    }	      
 	    TempOutput = "";
 	    while(! Temp.isEmpty())
 	    {
 	    	if (Temp.indexOf("\n") > -1)
 	    	{	    		  
 	    		TempOutput = Temp.substring(0,Temp.indexOf("\n"));    		    		  	  		  
 	    		Temp = Temp.substring(Temp.indexOf("\n") + 1);
 	    		try {
 	    			p.getAppletContext().showDocument
 	    			(new URL("javascript:getXMLCode(\"" + TempOutput +"\")"));
 	    			}
 	    			catch (MalformedURLException me) { }  
 	    		  	    		 
 	    	}   
 	    	else
 	    	{
 	    		TempOutput = Temp;	    		 
 	    		try {
 	    			p.getAppletContext().showDocument
 	    			(new URL("javascript:getXMLCode(\"" + TempOutput +"\")"));
 	    			}
 	    			catch (MalformedURLException me) { }  
 	    		Temp = "";
 	    	}
 	    }       
 	    
 
 	    int fixedPointSelectionTemp = 0;
 	    if (FloatingFixedPointGroup.getSelectedCheckbox() == Fixed_Point)
 	    	fixedPointSelectionTemp = 1; 
 	    else
 	    	fixedPointSelectionTemp = 0; 
 	    try {
 			p.getAppletContext().showDocument
 			(new URL("javascript:getFloatingFixedSelection(\"" + fixedPointSelectionTemp +"\")"));
 			}
			catch (MalformedURLException me) { }
 			
 		int wordLengthTemp = 0;
 		if (wordLength.getText().isEmpty())
 			wordLengthTemp = 16;
 		else
         	wordLengthTemp = Integer.parseInt(wordLength.getText().toString());
 	    try {
 			p.getAppletContext().showDocument
 			(new URL("javascript:getFixedWordLength(\"" + wordLengthTemp +"\")"));
 			}
 			catch (MalformedURLException me) { }
 			
 	    int fractionLengthTemp = 0;
 	    if (fractionLength.getText().isEmpty())
 	    	fractionLengthTemp = 8;
 	    else
 	    	fractionLengthTemp = Integer.parseInt(fractionLength.getText().toString());
 	    try {
 			p.getAppletContext().showDocument
 			(new URL("javascript:getFixedFractionLength(\"" + fractionLengthTemp +"\")"));
 			}
 			catch (MalformedURLException me) { }
 	    
 	    String Type = "Matlab";
 	    try {
 			p.getAppletContext().showDocument
 			(new URL("javascript:getImplementationCode(\"" + Type +"\")"));
 			}
 			catch (MalformedURLException me) { }
 			
 	}
 	
 	if (evt.getSource() == toC)
 	{
 		TargetCodeIndicator = 2;
 		String Temp = XMLCode.getText();
 	    String TempOutput = "";	      
 	    while(Temp.indexOf("\"") > -1)
 	    {
 	    	TempOutput = Temp.substring(0,Temp.indexOf("\""));  
   		    TempOutput = TempOutput + " CLBM_QOUTE ";	    		  
   		    Temp = TempOutput + Temp.substring(Temp.indexOf("\"") + 1);
 	    }	      
 	    TempOutput = "";
 	    while(! Temp.isEmpty())
 	    {
 	    	if (Temp.indexOf("\n") > -1)
 	    	{	    		  
 	    		TempOutput = Temp.substring(0,Temp.indexOf("\n"));    		    		  	  		  
 	    		Temp = Temp.substring(Temp.indexOf("\n") + 1);
 	    		try {
 	    			p.getAppletContext().showDocument
 	    			(new URL("javascript:getXMLCode(\"" + TempOutput +"\")"));
 	    			}
 	    			catch (MalformedURLException me) { }  
 	    		  	    		 
 	    	}   
 	    	else
 	    	{
 	    		TempOutput = Temp;	    		 
 	    		try {
 	    			p.getAppletContext().showDocument
 	    			(new URL("javascript:getXMLCode(\"" + TempOutput +"\")"));
 	    			}
 	    			catch (MalformedURLException me) { }  
 	    		Temp = "";
 	    	}
 	    }        
 	    String Type = "C";
 	    try {
 			p.getAppletContext().showDocument
 			(new URL("javascript:getImplementationCode(\"" + Type +"\")"));
 			}
 			catch (MalformedURLException me) { }
 			
 	}
 	 
 	if (evt.getSource() == toCPlus)
 	{
 		TargetCodeIndicator = 3;
 		String Temp = XMLCode.getText();
 	    String TempOutput = "";	      
 	    while(Temp.indexOf("\"") > -1)
 	    {
 	    	TempOutput = Temp.substring(0,Temp.indexOf("\""));  
   		    TempOutput = TempOutput + " CLBM_QOUTE ";	    		  
   		    Temp = TempOutput + Temp.substring(Temp.indexOf("\"") + 1);
 	    }	      
 	    TempOutput = "";
 	    while(! Temp.isEmpty())
 	    {
 	    	if (Temp.indexOf("\n") > -1)
 	    	{	    		  
 	    		TempOutput = Temp.substring(0,Temp.indexOf("\n"));    		    		  	  		  
 	    		Temp = Temp.substring(Temp.indexOf("\n") + 1);
 	    		try {
 	    			p.getAppletContext().showDocument
 	    			(new URL("javascript:getXMLCode(\"" + TempOutput +"\")"));
 	    			}
	    			catch (MalformedURLException me) { }  
	    		  	    		 
	    	}   
 	    	else
 	    	{
	    		TempOutput = Temp;	    		 
 	    		try {
 	    			p.getAppletContext().showDocument
 	    			(new URL("javascript:getXMLCode(\"" + TempOutput +"\")"));
 	    			}
 	    			catch (MalformedURLException me) { }  
 	    		Temp = "";
 	    	}
 	    }        
 	    String Type = "C++";
 	    try {
 			p.getAppletContext().showDocument
 			(new URL("javascript:getImplementationCode(\"" + Type +"\")"));
 			}
 			catch (MalformedURLException me) { }
 			
 	}
 	
 	if (evt.getSource() == toVHDL)
 	{
 		TargetCodeIndicator = 4;
 		String Temp = XMLCode.getText();
 	    String TempOutput = "";	      
 	    while(Temp.indexOf("\"") > -1)
 	    {
 	    	TempOutput = Temp.substring(0,Temp.indexOf("\""));  
   		    TempOutput = TempOutput + " CLBM_QOUTE ";	    		  
   		    Temp = TempOutput + Temp.substring(Temp.indexOf("\"") + 1);
 	    }	      
 	    TempOutput = "";
 	    while(! Temp.isEmpty())
 	    {
 	    	if (Temp.indexOf("\n") > -1)
 	    	{	    		  
 	    		TempOutput = Temp.substring(0,Temp.indexOf("\n"));    		    		  	  		  
 	    		Temp = Temp.substring(Temp.indexOf("\n") + 1);
 	    		try {
 	    			p.getAppletContext().showDocument
 	    			(new URL("javascript:getXMLCode(\"" + TempOutput +"\")"));
 	    			}
 	    			catch (MalformedURLException me) { }  
 	    		  	    		 
 	    	}   
 	    	else
 	    	{
 	    		TempOutput = Temp;	    		 
 	    		try {
 	    			p.getAppletContext().showDocument
 	    			(new URL("javascript:getXMLCode(\"" + TempOutput +"\")"));
 	    			}
 	    			catch (MalformedURLException me) { }  
 	    		Temp = "";
 	    	}
 	    }        
 	    String Type = "VHDL";
 	    try {
 			p.getAppletContext().showDocument
 			(new URL("javascript:getImplementationCode(\"" + Type +"\")"));
 			}
 			catch (MalformedURLException me) { }
 			
 	}
 	
 	if (evt.getSource() == toCUDA)
 	{
 		TargetCodeIndicator = 5;
 		String CLBMHardwareModelCode = "";
 		if (HardwarePlatformGroup.getSelectedCheckbox() == NVIDIA_GeForce_450)
 		{
 			p.CLBMHardwareModelCode  =  "<Place name = \"GeForce_GTS_450\">\n    <Thing name = \"grid\">\n        <Place name = \"dimensionality\">\n            <Thing>3</Thing>\n            <Action>SET</Action>\n        </Place>\n        <Place name = \"dimensions\">\n            <Thing>\n                <Place name = \"x\">\n                    <Thing>65535</Thing>\n                    <Action>SET</Action>\n                </Place>\n                <Place name = \"y\">\n                    <Thing>65535</Thing>\n                    <Action>SET</Action>\n                </Place>\n                <Place name = \"z\">\n                    <Thing>65535</Thing>\n                    <Action>SET</Action>\n                </Place>\n                <Place name = \"size\">\n                    <Thing>3</Thing>\n                    <Action>SET</Action>\n                </Place>\n            </Thing>\n            <Action>SET</Action>\n        </Place>\n    </Thing>\n    <Thing name = \"block\">\n        <Place name = \"dimensionality\">\n            <Thing>3</Thing>\n            <Action>SET</Action>\n        </Place>\n        <Place name = \"dimensions\">\n            <Thing>\n                <Place name = \"x\">\n                    <Thing>1024</Thing>\n                    <Action>SET</Action>\n                </Place>\n                <Place name = \"y\">\n                    <Thing>1024</Thing>\n                    <Action>SET</Action>\n                </Place>\n                <Place name = \"z\">\n                    <Thing>64</Thing>\n                    <Action>SET</Action>\n                </Place>\n                <Place name = \"size\">\n                    <Thing>3</Thing>\n                    <Action>SET</Action>\n                </Place>\n            </Thing>\n            <Action>SET</Action>\n        </Place>\n    </Thing>\n    <Thing name = \"thread\">1024</Thing>\n    <Thing name = \"sharedMemory\">48KB</Thing>\n    <Thing name = \"localMemory\">512KB</Thing>\n    <Thing name = \"ConstantMemory\">64KB</Thing>\n</Place>\n";
 		}	
 		if (HardwarePlatformGroup.getSelectedCheckbox() == NVIDIA_GeForce_280)
 		{
 			p.CLBMHardwareModelCode  =  "<Place name = \"GeForce_GTX_280\">\n    <Thing name = \"grid\">\n        <Place name = \"dimensionality\">\n            <Thing>2</Thing>\n            <Action>SET</Action>\n        </Place>\n        <Place name = \"dimensions\">\n            <Thing>\n                <Place name = \"x\">\n                    <Thing>65535</Thing>\n                    <Action>SET</Action>\n                </Place>\n                <Place name = \"y\">\n                    <Thing>65535</Thing>\n                    <Action>SET</Action>\n                </Place>\n                <Place name = \"size\">\n                    <Thing>2</Thing>\n                    <Action>SET</Action>\n                </Place>\n            </Thing>\n            <Action>SET</Action>\n        </Place>\n    </Thing>\n    <Thing name = \"block\">\n        <Place name = \"dimensionality\">\n            <Thing>3</Thing>\n            <Action>SET</Action>\n        </Place>\n        <Place name = \"dimensions\">\n            <Thing>\n                <Place name = \"x\">\n                    <Thing>512</Thing>\n                    <Action>SET</Action>\n                </Place>\n                <Place name = \"y\">\n                    <Thing>512</Thing>\n                    <Action>SET</Action>\n                </Place>\n                <Place name = \"z\">\n                    <Thing>64</Thing>\n                    <Action>SET</Action>\n                </Place>\n                <Place name = \"size\">\n                    <Thing>3</Thing>\n                    <Action>SET</Action>\n                </Place>\n            </Thing>\n            <Action>SET</Action>\n        </Place>\n    </Thing>\n    <Thing name = \"thread\">512</Thing>\n    <Thing name = \"sharedMemory\">16KB</Thing>\n    <Thing name = \"localMemory\">16KB</Thing>\n    <Thing name = \"ConstantMemory\">64KB</Thing>\n</Place>\n";			  
 		}
 		String Temp = p.CLBMHardwareModelCode;
 		String TempOutput = "";	      
 	    while(Temp.indexOf("\"") > -1)
 	    {
 	    	TempOutput = Temp.substring(0,Temp.indexOf("\""));  
    		    TempOutput = TempOutput + " CLBM_QOUTE ";	    		  
    		    Temp = TempOutput + Temp.substring(Temp.indexOf("\"") + 1);
 	    }
 	    TempOutput = "";
 	    while(! Temp.isEmpty())
 	    {
 	    	if (Temp.indexOf("\n") > -1)
 	    	{
 	    		TempOutput = Temp.substring(0,Temp.indexOf("\n"));    		    		  
 	    		try {
 	    			  p.getAppletContext().showDocument
 	    			  (new URL("javascript:getXMLHardwareCode(\"" + TempOutput +"\")"));
 	    			  }
 	    			  catch (MalformedURLException me) { }
 	    		  Temp = Temp.substring(Temp.indexOf("\n") + 1);	    		 
 	    	  }   
 	    	  else
 	    	  {
 	    		  TempOutput = Temp;    		    		  
 	    		  try {
 	    			  p.getAppletContext().showDocument
 	    			  (new URL("javascript:getXMLHardwareCode(\"" + TempOutput +"\")"));
 	    			  }
 	    			  catch (MalformedURLException me) { }  
 	    		  Temp = "";
 	    	  }
 	      } 
 		
 		
 		Temp = XMLCode.getText();
 	    TempOutput = "";	      
 	    while(Temp.indexOf("\"") > -1)
 	    {
 	    	TempOutput = Temp.substring(0,Temp.indexOf("\""));  
   		    TempOutput = TempOutput + " CLBM_QOUTE ";	    		  
   		    Temp = TempOutput + Temp.substring(Temp.indexOf("\"") + 1);
 	    }	      
 	    TempOutput = "";
 	    while(! Temp.isEmpty())
 	    {
 	    	if (Temp.indexOf("\n") > -1)
 	    	{	    		  
 	    		TempOutput = Temp.substring(0,Temp.indexOf("\n"));    		    		  	  		  
 	    		Temp = Temp.substring(Temp.indexOf("\n") + 1);
 	    		try {
 	    			p.getAppletContext().showDocument
 	    			(new URL("javascript:getXMLCode(\"" + TempOutput +"\")"));
 	    			}
 	    			catch (MalformedURLException me) { }  
 	    		  	    		 
 	    	}   
 	    	else
 	    	{
 	    		TempOutput = Temp;	    		 
 	    		try {
 	    			p.getAppletContext().showDocument
 	    			(new URL("javascript:getXMLCode(\"" + TempOutput +"\")"));
 	    			}
 	    			catch (MalformedURLException me) { }  
 	    		Temp = "";
 	    	}
 	    } 
 	    String Type = "CUDA";
 	    try {
 			p.getAppletContext().showDocument
 			(new URL("javascript:getImplementationCode(\"" + Type +"\")"));
 			}
 			catch (MalformedURLException me) { }
 			
 	}
 	
 	if (evt.getSource() == toOpenCL)
 	{
 		TargetCodeIndicator = 6;
 		/*String CLBMHardwareModelCode = "";
 		if (HardwarePlatformGroup.getSelectedCheckbox() == NVIDIA_GeForce_450)
 		{
 			p.CLBMHardwareModelCode  =  "<Place name = \"GeForce_GTS_450\">\n    <Thing name = \"grid\">\n        <Place name = \"dimensionality\">\n            <Thing>3</Thing>\n            <Action>SET</Action>\n        </Place>\n        <Place name = \"dimensions\">\n            <Thing>\n                <Place name = \"x\">\n                    <Thing>65535</Thing>\n                    <Action>SET</Action>\n                </Place>\n                <Place name = \"y\">\n                    <Thing>65535</Thing>\n                    <Action>SET</Action>\n                </Place>\n                <Place name = \"z\">\n                    <Thing>65535</Thing>\n                    <Action>SET</Action>\n                </Place>\n                <Place name = \"size\">\n                    <Thing>3</Thing>\n                    <Action>SET</Action>\n                </Place>\n            </Thing>\n            <Action>SET</Action>\n        </Place>\n    </Thing>\n    <Thing name = \"block\">\n        <Place name = \"dimensionality\">\n            <Thing>3</Thing>\n            <Action>SET</Action>\n        </Place>\n        <Place name = \"dimensions\">\n            <Thing>\n                <Place name = \"x\">\n                    <Thing>1024</Thing>\n                    <Action>SET</Action>\n                </Place>\n                <Place name = \"y\">\n                    <Thing>1024</Thing>\n                    <Action>SET</Action>\n                </Place>\n                <Place name = \"z\">\n                    <Thing>64</Thing>\n                    <Action>SET</Action>\n                </Place>\n                <Place name = \"size\">\n                    <Thing>3</Thing>\n                    <Action>SET</Action>\n                </Place>\n            </Thing>\n            <Action>SET</Action>\n        </Place>\n    </Thing>\n    <Thing name = \"thread\">1024</Thing>\n    <Thing name = \"sharedMemory\">48KB</Thing>\n    <Thing name = \"localMemory\">512KB</Thing>\n    <Thing name = \"ConstantMemory\">64KB</Thing>\n</Place>\n";
 		}	
 		if (HardwarePlatformGroup.getSelectedCheckbox() == NVIDIA_GeForce_280)
 		{
 			p.CLBMHardwareModelCode  =  "<Place name = \"GeForce_GTX_280\">\n    <Thing name = \"grid\">\n        <Place name = \"dimensionality\">\n            <Thing>2</Thing>\n            <Action>SET</Action>\n        </Place>\n        <Place name = \"dimensions\">\n            <Thing>\n                <Place name = \"x\">\n                    <Thing>65535</Thing>\n                    <Action>SET</Action>\n                </Place>\n                <Place name = \"y\">\n                    <Thing>65535</Thing>\n                    <Action>SET</Action>\n                </Place>\n                <Place name = \"size\">\n                    <Thing>2</Thing>\n                    <Action>SET</Action>\n                </Place>\n            </Thing>\n            <Action>SET</Action>\n        </Place>\n    </Thing>\n    <Thing name = \"block\">\n        <Place name = \"dimensionality\">\n            <Thing>3</Thing>\n            <Action>SET</Action>\n        </Place>\n        <Place name = \"dimensions\">\n            <Thing>\n                <Place name = \"x\">\n                    <Thing>512</Thing>\n                    <Action>SET</Action>\n                </Place>\n                <Place name = \"y\">\n                    <Thing>512</Thing>\n                    <Action>SET</Action>\n                </Place>\n                <Place name = \"z\">\n                    <Thing>64</Thing>\n                    <Action>SET</Action>\n                </Place>\n                <Place name = \"size\">\n                    <Thing>3</Thing>\n                    <Action>SET</Action>\n                </Place>\n            </Thing>\n            <Action>SET</Action>\n        </Place>\n    </Thing>\n    <Thing name = \"thread\">512</Thing>\n    <Thing name = \"sharedMemory\">16KB</Thing>\n    <Thing name = \"localMemory\">16KB</Thing>\n    <Thing name = \"ConstantMemory\">64KB</Thing>\n</Place>\n";			  
 		}
 		String Temp = p.CLBMHardwareModelCode;
 		String TempOutput = "";	      
 	    while(Temp.indexOf("\"") > -1)
 	    {
 	    	TempOutput = Temp.substring(0,Temp.indexOf("\""));  
    		    TempOutput = TempOutput + " CLBM_QOUTE ";	    		  
    		    Temp = TempOutput + Temp.substring(Temp.indexOf("\"") + 1);
 	    }
 	    TempOutput = "";
 	    while(! Temp.isEmpty())
 	    {
 	    	if (Temp.indexOf("\n") > -1)
 	    	{
 	    		TempOutput = Temp.substring(0,Temp.indexOf("\n"));    		    		  
 	    		try {
 	    			  p.getAppletContext().showDocument
 	    			  (new URL("javascript:getXMLHardwareCode(\"" + TempOutput +"\")"));
 	    			  }
 	    			  catch (MalformedURLException me) { }
 	    		  Temp = Temp.substring(Temp.indexOf("\n") + 1);	    		 
 	    	  }   
 	    	  else
 	    	  {
 	    		  TempOutput = Temp;    		    		  
 	    		  try {
 	    			  p.getAppletContext().showDocument
 	    			  (new URL("javascript:getXMLHardwareCode(\"" + TempOutput +"\")"));
 	    			  }
 	    			  catch (MalformedURLException me) { }  
 	    		  Temp = "";
 	    	  }
 	      }*/ 
 		
 		
 		String Temp = XMLCode.getText();
 	    String TempOutput = "";	      
 	    while(Temp.indexOf("\"") > -1)
 	    {
 	    	TempOutput = Temp.substring(0,Temp.indexOf("\""));  
   		    TempOutput = TempOutput + " CLBM_QOUTE ";	    		  
   		    Temp = TempOutput + Temp.substring(Temp.indexOf("\"") + 1);
 	    }	      
 	    TempOutput = "";
 	    while(! Temp.isEmpty())
 	    {
 	    	if (Temp.indexOf("\n") > -1)
 	    	{	    		  
 	    		TempOutput = Temp.substring(0,Temp.indexOf("\n"));    		    		  	  		  
 	    		Temp = Temp.substring(Temp.indexOf("\n") + 1);
 	    		try {
 	    			p.getAppletContext().showDocument
 	    			(new URL("javascript:getXMLCode(\"" + TempOutput +"\")"));
 	    			}
 	    			catch (MalformedURLException me) { }  
 	    		  	    		 
 	    	}   
 	    	else
 	    	{
 	    		TempOutput = Temp;	    		 
 	    		try {
 	    			p.getAppletContext().showDocument
 	    			(new URL("javascript:getXMLCode(\"" + TempOutput +"\")"));
 	    			}
 	    			catch (MalformedURLException me) { }  
 	    		Temp = "";
 	    	}
 	    } 
 	    String Type = "OpenCL";
 	    try {
 			p.getAppletContext().showDocument
 			(new URL("javascript:getImplementationCode(\"" + Type +"\")"));
 			}
 			catch (MalformedURLException me) { }
 			
 	}
 	
 	  
   }
   
 
 	
 }
