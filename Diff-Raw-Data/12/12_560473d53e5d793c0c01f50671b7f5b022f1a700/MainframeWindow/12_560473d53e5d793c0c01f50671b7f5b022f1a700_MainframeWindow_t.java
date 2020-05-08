 package mainframe;
 
 import java.awt.Color;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 
 import mainframe.IMainframe.IdentificationError;
 import GUI.BasicPanel;
 import GUI.StationPanel;
 import GUI.View;
 import GUI.WaitForClick;
 import partiesList.IPartiesList;
 import partiesList.IParty;
 
 public class MainframeWindow extends StationPanel implements IMainframeWindow {
 
 	static final long serialVersionUID = 1L;
 	private IMainframe callerStation;
 	private boolean was_pushed=true;
 	private MainframeAction chosen_action;
 	private HistogramPanel histogramPanel;
 	private TablePanel tablePanel;
 	
 	private final Color MainframeBackGround = new Color(255,255,255); 
 	
 	public MainframeWindow(IMainframe callerStation) {
 		super("Main Frame");
 		this.callerStation = callerStation;
 		
 	}
 	
 	@Override
 	public void init() {
 		new Thread(this).start();
 	}
 
 	@Override
 	public void showHistogram(IPartiesList parties) {
 		if(histogramPanel == null){
 			histogramPanel = new HistogramPanel();
 			window.add_button(new View("histogram"), histogramPanel);
 		}
 		histogramPanel.showHistogram(parties);
 		window.show_if_current(histogramPanel);
 	}
 
 	@Override
 	public void showTable(IPartiesList parties) {
 		
 		if(tablePanel == null){
 			tablePanel = new TablePanel();
 			window.add_button(new View("table"), tablePanel);
 		}
 		tablePanel.showTable(parties);
 		window.show_if_current(tablePanel);
 	}
 	
 	void setAction(MainframeAction action){
 		if(!was_pushed){
 			was_pushed=true;
 			chosen_action = action;
 		}
 	}
 	
 	private void make_mainframe_button(JPanel mainframe_panel, MainframeAction action, Object lock){
 		JButton button = new JButton(action.toString());
		button.addActionListener(new MainframeClick(this,action,lock));
 		mainframe_panel.add(button);
 	}
 	
 	 private void make_mainframe_panel(JPanel mainframe_panel, Object lock){
 		 make_mainframe_button(mainframe_panel,MainframeAction.initialize,lock);
 		 make_mainframe_button(mainframe_panel,MainframeAction.countVotes,lock);
 		 make_mainframe_button(mainframe_panel,MainframeAction.restore,lock);
 		 make_mainframe_button(mainframe_panel,MainframeAction.identification,lock);
 		 make_mainframe_button(mainframe_panel,MainframeAction.shutDown,lock);
 	  }
 	
 	private MainframeAction chooseAction(){
 		was_pushed = false;
 		chosen_action = null;
 		
 		JPanel mainframe_panel = new JPanel(new FlowLayout());
 		make_mainframe_panel(mainframe_panel,this);
 		this.removeAll();
 		mainframe_panel.setBackground(MainframeBackGround);
 		this.setBackground(MainframeBackGround);
 		this.add(mainframe_panel);
 		window.show_if_current(this);
 		try{
 			synchronized (this) {
 				this.wait();
 			}
 		}
 		catch(InterruptedException e){}
 		return chosen_action;
 	}
 	
 	private void make_id_panel(JPanel id_panel,JTextField textField, Object lock,String name){
 		id_panel.add(textField);
 		JButton button = new JButton(name);
 		button.addActionListener(new WaitForClick(lock));
 		id_panel.add(button);
 	}
 
 	private int getID(){
 		
 		JPanel id_panel = new JPanel(new GridLayout(2,1));
 	  	JTextField textField = new JTextField();
 	  	make_id_panel(id_panel,textField,this,"enter ID");
 		this.removeAll();
 		this.add(id_panel);
 		window.show_if_current(this);
 		try{
 			synchronized (this) {
 				this.wait();
 			}
 		}
 		catch(InterruptedException e){}
 		
 		String id = textField.getText();
 		return Integer.parseInt(id);
 	}
 
 	@Override
 	public void run() {
 		while(true){
 			MainframeAction action = chooseAction();
 			switch (action) {
 				case initialize:
 					callerStation.initialize();
 					break;
 				case countVotes:
 					callerStation.countVotes();
 					break;
 				case identification:
 					int id = getID();
 					try {
 						callerStation.identification(id);
 					} catch (IdentificationError e) {
 						printError("voter already registered");
 					}
 					break;
 				case restore:
 					callerStation.restore();
 					break;
 				case shutDown:
 					callerStation.shutDown();
 					closeWindow();
 					break;
 			default:
 				break;
 			}
 		}
 	}
 
 }
