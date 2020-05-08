 package it.gas.altichierock.insert;
 
 import it.gas.altichierock.database.entities.Product;
 import it.gas.altichierock.insert.ProductPanel.OnSelectionListener;
 
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.SwingWorker;
 import javax.swing.WindowConstants;
 
 import net.miginfocom.swing.MigLayout;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class InsertWindow extends JFrame implements ActionListener,
 		ProductPanel.OnSelectionListener {
 	private static final long serialVersionUID = 1L;
 	private Logger log = LoggerFactory.getLogger(InsertWindow.class);
 	private InsertLogic logic;
 	private List<ProductPanel> panels;
 	private ReceiptKeeper keeper;
 	private ProductPanel ppSelected;
 
 	private JScrollPane slpContainer, slpReceipt;
 	private JPanel pnlContainer;
 	private JButton btnInsert, btnPlus, btnMinus, btnConfirm;
 	private JTable tblReceipt;
 	private JTextField txtQuantity;
 	private JLabel lblTotalPrice;
 
 	public InsertWindow(Frame f) {
 		super();
 		logic = new InsertLogic();
 		panels = new ArrayList<ProductPanel>();
 		keeper = new ReceiptKeeper();
 		
 		setTitle("Make-a-menu!");
 		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 		initComponents();
 		initListeners();
 
 		setSize(500, 400);
 		setLocationRelativeTo(f);
 
 		// start the worker!!!
 		Worker w = new Worker(Worker.REFRESH);
 		w.addParam(this);
 		w.execute();
 	}
 
 	private void initComponents() {
 		setLayout(new MigLayout("fill",
 				"[grow 3, growprio 3][grow 1, growprio 1]"));
 
 		// left side of the window
 		pnlContainer = new JPanel(new MigLayout("fill, wrap 1"));
 		slpContainer = new JScrollPane(pnlContainer);
 		add(slpContainer, "grow, push");
 
 		// and the right
 		JPanel filler = new JPanel(new MigLayout("fill, ins 0, wrap 1"));
 		add(filler, "grow");
 
 		// fill the filler!!!
 		btnInsert = new JButton("Insert");
 		btnInsert.setEnabled(false);
 		filler.add(btnInsert, "grow");
 
 		btnPlus = new JButton("+");
 		txtQuantity = new JTextField("1");
 		btnMinus = new JButton("-");
 		filler.add(btnPlus, "split 3");
 		filler.add(txtQuantity, "grow");
 		filler.add(btnMinus);
 
 		lblTotalPrice = new JLabel("€ 0.00", JLabel.CENTER);
 		filler.add(lblTotalPrice, "grow");
 
 		tblReceipt = new JTable(keeper.getModel());
 		slpReceipt = new JScrollPane(tblReceipt);
 		filler.add(slpReceipt, "grow, push");
 
 		btnConfirm = new JButton("Confirm");
 		filler.add(btnConfirm, "grow");
 	}
 
 	private void initListeners() {
 		btnPlus.addActionListener(this);
 		btnMinus.addActionListener(this);
 		btnInsert.addActionListener(this);
 		btnConfirm.addActionListener(this);
 	}
 	
 	private void updateTotalPrice() {
 		float total = 0;
 		for (int i = 0; i < keeper.getSize(); i++) {
 			if (keeper.getQuantity(i) != 0)
 				total += keeper.getQuantity(i) * keeper.getPrice(i);
 		}
		lblTotalPrice.setText("€ " + total);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == btnPlus) {
 			int qty = Integer.valueOf(txtQuantity.getText());
 			qty++;
 			txtQuantity.setText(Integer.toString(qty));
 		} else if (e.getSource() == btnMinus) {
 			int qty = Integer.valueOf(txtQuantity.getText());
 			qty--;
 			txtQuantity.setText(Integer.toString(qty));
 		} else if (e.getSource() == btnInsert) {
 			keeper.add(Integer.valueOf(txtQuantity.getText()), 
 					ppSelected.getProduct(), ppSelected.getTotalPrice());
 			String[] compos = ppSelected.getComponentsSelected();
 			for (int i = 0; i < compos.length; i++)
 				keeper.add(0, compos[i], 0);
 			keeper.add(0,  "", 0); //white line
 			updateTotalPrice(); //update total
 		} else if (e.getSource() == btnConfirm) {
 			int number = logic.storeKeeper(keeper);
 			JOptionPane.showMessageDialog(this, "Number: " + number);
			keeper.clear();
			lblTotalPrice.setText("€ 0.00");
 		}
 	}
 
 	@Override
 	public void onProductSelected(ProductPanel pp) {
 		for (int i = 0; i < panels.size(); i++) {
 			if (panels.get(i) != pp)
 				panels.get(i).disablePanel(true);
 		}
 		ppSelected = pp;
 		btnInsert.setEnabled(true);
 	}
 
 	@Override
 	public void onProductDeselected(ProductPanel pp) {
 		for (int i = 0; i < panels.size(); i++)
 			panels.get(i).disablePanel(false);
 		ppSelected = null;
 		btnInsert.setEnabled(false);
 	}
 
 	/*@Override
 	public void onComponentSelected(Product product, Component c) {
 	}
 
 	@Override
 	public void onComponentDeselected(Product p, Component c) {
 	}*/
 
 	private class Worker extends SwingWorker<List<Product>, Void> {
 		public static final int REFRESH = 0;
 		private int status;
 		private ArrayList<Object> params;
 
 		public Worker(int status) {
 			if (status != 0)
 				throw new IllegalArgumentException();
 			this.status = status;
 			params = new ArrayList<Object>();
 		}
 
 		public void addParam(Object o) {
 			params.add(o);
 		}
 
 		@Override
 		protected List<Product> doInBackground() throws Exception {
 			switch (status) {
 			case REFRESH:
 				List<Product> l = logic.getProducts();
 				return l;
 			}
 			return null;
 		}
 
 		@Override
 		protected void done() {
 			try {
 				switch (status) {
 				case REFRESH:
 					pnlContainer.removeAll();
 					OnSelectionListener listener = (OnSelectionListener) params
 							.get(0);
 					// remove listeners, so GC can remove the panels
 					for (int i = 0; i < panels.size(); i++)
 						panels.get(i).removeListener(listener);
 					panels.clear();
 					// make and add new panels
 					List<Product> l = get();
 					for (int i = 0; i < l.size(); i++) {
 						ProductPanel pp = new ProductPanel(l.get(i));
 						pp.addListener(listener);
 						panels.add(pp);
 						pnlContainer.add(pp, "grow");
 					}
 					pnlContainer.add(Box.createGlue(), "push");
 				}
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} catch (ExecutionException e) {
 				e.printStackTrace();
 			} finally {
 				log.debug("Just a log inside a \"finally\" block.");
 			}
 		}
 	}
 
 }
