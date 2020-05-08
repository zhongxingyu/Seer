 
 package gui.panels.subcontrolpanels;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import engine.agent.BinAgent;
 import engine.util.Config;
 import engine.util.GlassType;
 import gui.panels.ControlPanel;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import transducer.TChannel;
 import transducer.TEvent;
 
 /**
  * The ConfigSelectPanel class contains buttons allowing the user to select what
  * type of glass to produce.
  */
 @SuppressWarnings("serial")
 public class ConfigSelectPanel extends JPanel implements ActionListener
 {
 	/** The ControlPanel this is linked to */
 	private ControlPanel parent;
 	
 	//private List <Config> configList = new ArrayList<Config>();
 	//alex change into glasstype
 	private List <GlassType> configList;
 	
 	//ALEX: SET UP GUI FOR PRODUCTION PANEL
 	JButton produceButton;
 	//JList list;
 	BinAgent bin;
 	//String[] name;
 	
 	/**
 	 * Creates a new ConfigSelect and links it to the control panel
 	 * @param cp
 	 *        the ConfigSelect linked to it
 	 */
 	public ConfigSelectPanel(ControlPanel cp)
 	{
 		parent = cp;
 		bin = null;
 		//this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		//Alex GUI for production panel
 		configList = Collections.synchronizedList(new ArrayList<GlassType>());
 //		name=new String[10];
 //		list=new JList(name);
 //		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 //		list.setVisibleRowCount(10);
 //		list.addListSelectionListener(this);
 		
 		this.setLayout(new BorderLayout());
 		produceButton = new JButton("Produce");
 		produceButton.addActionListener(this);
 		
 		
 		//this.add(list,BorderLayout.WEST);
 		this.add(produceButton,BorderLayout.SOUTH);
 	}
 
 	/**
 	 * Returns the parent panel
 	 * @return the parent panel
 	 */
 	public ControlPanel getGuiParent()
 	{
 		return parent;
 	}
 	
 	
 	
 	
 	
 	
 	
 
 	public void actionPerformed(ActionEvent ae)
 	{
 		
 		// alex: produce the chosen glasstype
 		if(ae.getSource() == produceButton){
 			if(bin!=null){
 				System.out.println("product created");
 				//ArrayList <Config> testConfig = new ArrayList<Config>();
 				//testConfig.add(new Config(true,true,true, "Dragon"));
 				//bin.hereIsConfig(testConfig);
 				while(!configList.isEmpty()){
 					new Timer().schedule(new TimerTask(){
 					    public void run(){//this routine is like a message reception    
 					    	bin.hereIsConfig(configList.remove(0));
 					    }
 					}, 500);
 				}
 			}
 		}
 	}
 	
 	public List<GlassType> getConfigList(){
 		return configList;
 	}
 	
 	public void setBinAgent(BinAgent b){
 		this.bin = b;
 	}
 
 //	public JList getList() {
 //		return list;
 //	}
 
 
 }
