 package warborn.controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JFrame;
 
 import warborn.model.Warborn;
 import warborn.view.BattleView;
 
 public class BattleController implements ActionListener{
 	private Warborn model;
 	private BattleView view;
 	private JFrame frame;
 	
 	public BattleController(Warborn model, JFrame frame, BattleView view){
 		this.frame = frame;
 		this.model = model;
 		this.view = view;
 		for(int i=0; i<view.getButtons().length; i++){
 			view.getButtons()[i].addActionListener(this);
 		}
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if((e).getSource() == view.getButtons()[0]){
 			view.logUpdate(model.getBattle().fight());
 		}
 		if((e).getSource() == view.getButtons()[1]){
 			while(model.getBattle().getFirstTerritory().getNbrOfUnits() > 1 && (model.getBattle().getSecondTerritory().getNbrOfUnits()) != 0){
 				view.logUpdate(model.getBattle().fight());
 			}
 		}
 		if((e).getSource() == view.getButtons()[2]){
 			frame.getLayeredPane().remove(view);
 			frame.repaint();
 			view.logClean();
 			if(view.getHeaderText().equalsIgnoreCase("Victory!")){
 				model.getBattle().move();
 			}else{
 				model.getBattle().resetTerritories();
 				model.getMove().resetTerritories();
				model.nextPhase();
 			}
 			view = new BattleView(frame);
 			new BattleController(model, frame, view);
 			model.addObserver(view);
 		}
 	}
 }
