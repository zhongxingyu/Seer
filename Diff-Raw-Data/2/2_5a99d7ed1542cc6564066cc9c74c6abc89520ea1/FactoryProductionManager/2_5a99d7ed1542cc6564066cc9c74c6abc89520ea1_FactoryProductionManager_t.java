 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 
 
 @SuppressWarnings("serial")
 public class FactoryProductionManager extends JPanel implements ActionListener {
 	
 	private FactoryProductionClient fpc;
 	private CardLayout cardlayout = new CardLayout();
 	public FactoryProductionSchedulePanel fpsp;
 	private FactoryProductionButtonPanel fpbp;
 	private FactoryProductionViewPanel fpvp;
 	private JPanel mainpanel;
 
 	
 	public FactoryProductionManager(FactoryProductionClient client){
 		fpc = client;
 		setLayout(new BorderLayout());
 		mainpanel = new JPanel();
 		mainpanel.setLayout(cardlayout);
 		fpsp = new FactoryProductionSchedulePanel();
 		fpbp = new FactoryProductionButtonPanel();
 		fpvp = new FactoryProductionViewPanel();
 		mainpanel.add(fpsp,"fpsp");
 		mainpanel.add(fpvp,"fpvbp");
 		fpbp.btnSwitchSchedule.addActionListener(this);
 		fpbp.btnSwitchView.addActionListener(this);
 		add(mainpanel,BorderLayout.CENTER);
 		add(fpbp,BorderLayout.SOUTH);
 		
 	}
 	
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		// TODO Auto-generated method stub
 		if(e.getSource() == fpbp.btnSwitchSchedule){
 			cardlayout.first(mainpanel);
 			
 		}
 		if(e.getSource() == fpbp.btnSwitchView){
			validate();
			repaint();
 			cardlayout.last(mainpanel);
 			
 		}
 	}
 
 	public FactoryProductionViewPanel getViewPanel() {
 		return fpvp;
 	}
 }
