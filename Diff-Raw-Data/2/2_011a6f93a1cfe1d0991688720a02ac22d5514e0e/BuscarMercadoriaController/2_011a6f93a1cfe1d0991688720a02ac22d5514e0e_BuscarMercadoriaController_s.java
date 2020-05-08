 package br.com.yaw.sjpac.controller;
 
 import java.util.List;
 
 import javax.swing.JFrame;
 
 import br.com.yaw.sjpac.action.AbstractAction;
 import br.com.yaw.sjpac.dao.MercadoriaDAO;
 import br.com.yaw.sjpac.dao.MercadoriaDAOJPA;
 import br.com.yaw.sjpac.event.BuscarMercadoriaEvent;
 import br.com.yaw.sjpac.model.Mercadoria;
 import br.com.yaw.sjpac.ui.BuscaMercadoriaFrame;
 
 /**
  * Define a <code>Controller</code> responsável por gerir a tela de Busca de <code>Mercadoria</code> pelo campo <code>nome</code>.
  * 
  * @see br.com.yaw.ssjc.controller.PersistenceController
  * 
  * @author YaW Tecnologia
  */
 public class BuscarMercadoriaController extends PersistenceController {
 
 	private BuscaMercadoriaFrame frame;
 	
 	public BuscarMercadoriaController(AbstractController parent) {
 		super(parent);
 		frame = new BuscaMercadoriaFrame();
 		frame.addWindowListener(this);
 		
 		registerAction(frame.getBuscarButton(), new AbstractAction() {
 			
 			List<Mercadoria> list; 
 			
 			public void action() {
 				if (frame.getText().length() > 0) {
 					MercadoriaDAO dao = new MercadoriaDAOJPA(getPersistenceContext());
 					list = dao.getMercadoriasByNome(frame.getText());
 				}
 			}
 			
 			public void posAction() {
 				cleanUp();
				if (list != null && !list.isEmpty()) {
 					fireEvent(new BuscarMercadoriaEvent(list));
 				}
 			}
 		});
 	}
 	
 	@Override
 	protected JFrame getFrame() {
 		return frame;
 	}
 
 	public void show() {
 		loadPersistenceContext();
 		frame.setVisible(true);
 	}
 
 	@Override
 	protected void cleanUp() {
 		frame.setVisible(false);
 		frame.resetForm();
 		
 		super.cleanUp();
 	}
 }
