 package control;
 
 import model.LichtModel;
 import view.LichtView;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 /**
  * User: Rouke
  * Date: 07/09/13
  * Time: 14:55
  */
 public class LichtController {
 
 	private LichtModel model;
 	private LichtView view;
 
 	public LichtController(LichtModel model, LichtView view) {
 
 		this.model = model;
 		this.view = view;
 
 		//add Listeners
 		view.getButtonPanel().addGroenButtonListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
				groenLichtActionPerformed();
 			}
 		});
 
 		view.getButtonPanel().addRoodButtonListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
				roodLichtActionPerformed();
 			}
 		});
 
 		view.getButtonPanel().addOranjeButtonListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
				oranjeLichtActionPeformed();
 			}
 		});
 	}
 
	private void roodLichtActionPerformed() {
 
 		if (model.getOranje()) {
 
 			model.setOranje();
 		}
 		if (model.getGroen()) {
 
 			model.setGroen();
 		}
 		model.setRood();
 	}
 
	private void oranjeLichtActionPeformed() {
 
 		if (model.getRood()) {
 
 			model.setRood();
 		}
 		if (model.getGroen()) {
 
 			model.setGroen();
 		}
 		model.setOranje();
 	}
 
	private void groenLichtActionPerformed() {
 
 		if (model.getOranje()) {
 
 			model.setOranje();
 		}
 		if (model.getRood()) {
 
 			model.setRood();
 		}
 		model.setGroen();
 	}
 }
