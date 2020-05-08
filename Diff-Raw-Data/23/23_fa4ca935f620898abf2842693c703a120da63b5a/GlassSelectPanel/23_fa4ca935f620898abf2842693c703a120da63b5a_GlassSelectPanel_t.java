 
 package gui.panels.subcontrolpanels;
 
 import java.awt.Color;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import engine.agent.Alex.BinAgent;
 import engine.agent.Alex.V1_GUI;
 import engine.interfaces.ConveyorFamily;
 import gui.panels.ControlPanel;
 
 import javax.swing.*;
 
 import shared.Barcode;
 import transducer.TChannel;
 
 /**
  * The GlassSelectPanel class contains buttons allowing the user to select what
  * type of glass to produce.
  */
 @SuppressWarnings("serial")
 public class GlassSelectPanel extends JPanel
 {
 	
 	ConveyorFamily c;
 	/** The ControlPanel this is linked to */
 	private ControlPanel parent;
 
 	 Timer timer = new Timer();
 	JCheckBox Cutter;
 	JCheckBox BreakOut;
 	JCheckBox ManualBreakout;
 	JCheckBox CrossSeamer;
 	JCheckBox Grinder;
 	JCheckBox Washer;
 	JCheckBox UVLamp;
 	JCheckBox Oven;
 	JCheckBox Painter;
 	JCheckBox Drill;
 	
 	
 	
 	private BinAgent binAgent;
 	JTextPane textPane;
 	String textOnPane;
 	private static Barcode ALL_RECIPE = new Barcode(0x3FF);
 	private static Barcode NONE_RECIPE = new Barcode(0);
 	private static Barcode SOME_1_RECIPE = new Barcode(0x1F);
 	private static Barcode SOME_2_RECIPE = new Barcode(0x3E0);
 	private static Barcode SOME_3_RECIPE = new Barcode(0x2AA);
 	private static Barcode FIRST_RECIPE = new Barcode(0x1);
 	
 	
 	/**
 	 * Creates a new GlassSelect and links it to the control panel
 	 * @param cp
 	 *        the ControlPanel linked to it
 	 */
 	public GlassSelectPanel(ControlPanel cp)
 	{
 		parent = cp;
 		this.setBackground(Color.GRAY);
 		
 		
 		textPane = new JTextPane();
 		textOnPane = new String();
 		
 		JPanel glasschoose = new JPanel();
 		glasschoose.setLayout(new GridLayout(6,2));
 		
 		
 		Cutter = new JCheckBox("Cutter");
 		BreakOut = new JCheckBox("BreakOut");
 		ManualBreakout = new JCheckBox("Manual-BreakOut");
 		Drill = new JCheckBox("DRILL");
 		CrossSeamer = new JCheckBox("Cross-Seamer");
 		Grinder = new JCheckBox("Grinder");
 		Washer = new JCheckBox("Washer");
 		UVLamp = new JCheckBox("UV_LAMP");
 		Oven = new JCheckBox("OVEN");
 		Painter = new JCheckBox("Painter");
 		
 		JButton submit = new JButton("Submit");
 		
 		
 		submit.addActionListener(new add());
 		glasschoose.add(Cutter);
 		glasschoose.add(BreakOut);
 		glasschoose.add(ManualBreakout);
 		glasschoose.add(Drill);
 		glasschoose.add(CrossSeamer);
 		glasschoose.add(Grinder);
 		glasschoose.add(Washer);
 		glasschoose.add(UVLamp);
 		glasschoose.add(Oven);
 		glasschoose.add(Painter);
 		this.add(glasschoose);
 		this.add(submit);
 		this.add(textPane);
 		
 		
 		
 		
 		glasschoose.setBackground(Color.GRAY);
 
 	}
 
 	public void setBinAgent(BinAgent binAgent){
 		this.binAgent = binAgent;
 	}
 	public void warnWaitingForCFFree() {
 		// TODO Auto-generated method stub
 		textOnPane = textOnPane + "Waiting for nc cutter to free to create glass!\n";
 		textPane.setText(textOnPane);
 	}
 
 	public void warnCreatingGlass() {
 		// TODO Auto-generated method stub
 		textOnPane = textOnPane + "Bin is creating glass!\n";
 		textPane.setText(textOnPane);
 		
 	}
 	
 	
 	
 	public class add implements ActionListener
 	{
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			// TODO Auto-generated method stub
 			
 			
 			 /*
 			timer.scheduleAtFixedRate(new TimerTask() {
   			  @Override
   			  public void run() {
   				  
   				 */
 
 			Long l = Long.parseLong((""+booleanToNumber(Painter.isSelected())+booleanToNumber(Oven.isSelected())+
 					booleanToNumber(UVLamp.isSelected())+booleanToNumber(Washer.isSelected())
 					+booleanToNumber(Grinder.isSelected())+booleanToNumber(CrossSeamer.isSelected())
 					+booleanToNumber(Drill.isSelected())+booleanToNumber(ManualBreakout.isSelected())
 					+booleanToNumber(BreakOut.isSelected())+booleanToNumber(Cutter.isSelected())
 									));
 			
	
			String s= ((""+booleanToNumber(Painter.isSelected())+booleanToNumber(Oven.isSelected())+
					booleanToNumber(UVLamp.isSelected())+booleanToNumber(Washer.isSelected())
					+booleanToNumber(Grinder.isSelected())+booleanToNumber(CrossSeamer.isSelected())
					+booleanToNumber(Drill.isSelected())+booleanToNumber(ManualBreakout.isSelected())
					+booleanToNumber(BreakOut.isSelected())+booleanToNumber(Cutter.isSelected())
									));
			
			long l1 = Long.parseLong(s, 2);
			
			System.out.println("number is "+l1);
			
			System.out.println("drill is "+Drill.isSelected());
			System.out.println("grinder is "+Grinder.isSelected());
			System.out.println("washer is "+Washer.isSelected());
 			
			binAgent.msgCreateGlass(new Barcode(l1));
   			
   			  /*
   			  }
   			}, 1100, 1100); */
 		}
 		
 	}
 	
 	
 	
 	
 	
 	
 	public int booleanToNumber(boolean b) {
 	    return b ? 1 : 0;
 	}
 	
 	
 	public void setConveyorFamily(ConveyorFamily ctemp)
 	{
 		c=ctemp;
 	}
 	
 	
 	
 	/**
 	 * Returns the parent panel
 	 * @return the parent panel
 	 */
 	public ControlPanel getGuiParent()
 	{
 		return parent;
 	}
 }
