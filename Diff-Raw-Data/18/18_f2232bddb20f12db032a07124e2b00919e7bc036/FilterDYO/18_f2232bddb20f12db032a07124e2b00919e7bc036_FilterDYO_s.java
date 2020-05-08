 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.Date;
 
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import com.isti.traceview.TraceViewException;
 import com.isti.traceview.data.RawDataProvider;
 import com.isti.traceview.processing.IFilter;
 import com.isti.xmax.gui.XMAXframe;
 
 import javax.swing.JTextField;
 import javax.swing.JLabel;
 import javax.swing.BorderFactory;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.awt.Dimension;
 import javax.swing.JComboBox;
 import javax.swing.border.EtchedBorder;
 
 /**
  * Filter with visual dialog in constructor do manually design it. Use HP, BP or LP filter to
  * process data, depends from entered values
  * 
  * @author Max Kokoulin
  */
 public class FilterDYO extends JDialog implements IFilter, PropertyChangeListener {
 	private static Double cutLowFrequency = null;
 	private static Double cutHighFrequency = null;
 	private static Integer order = null;
 
 	private IFilter filter = null;
 	private JOptionPane optionPane;
 	private JTextField lowFrequencyTF = null;
 	private JTextField highFrequencyTF = null;
 	private JLabel lowFrequencyL = null;
 	private JLabel highFrequencyL = null;
 	private JLabel orderL = null;
 	private JComboBox orderCB = null;
 	private boolean needProcessing = false;
 
 	public FilterDYO() {
 		super(XMAXframe.getInstance(), "Design your own filter", true);
 		if (cutLowFrequency == null) {
 			cutLowFrequency = 0.1;
 		}
 		if (cutHighFrequency == null) {
 			cutHighFrequency = 0.5;
 		}
 		if (order == null) {
 			order = 2;
 		}
 		Object[] options = { "OK", "Close" };
 		// Create the JOptionPane.
 		optionPane = new JOptionPane(createDesignPanel(order, cutLowFrequency, cutHighFrequency), JOptionPane.PLAIN_MESSAGE,
 				JOptionPane.CLOSED_OPTION, null, options, options[0]);
 		// Make this dialog display it.
 		setContentPane(optionPane);
 		optionPane.addPropertyChangeListener(this);
 		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
 		addWindowListener(new WindowAdapter(){
 			public void windowClosing(WindowEvent we) {
 				/*
 				 * Instead of directly closing the window, we're going to change the JOptionPane's
 				 * value property.
 				 */
 				optionPane.setValue("Close");
 			}
 		});
 		pack();
 		setLocationRelativeTo(super.getOwner());
 		setVisible(true);
 	}
 	
 	public int getMaxDataLength(){
 		return filter.getMaxDataLength();
 	}
 
 	private JPanel createDesignPanel(int order, double cutLowFrequency, double cutHighFrequency) {
 		JPanel panel = new JPanel();
 		panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
 		panel.setLayout(new GridBagLayout());
 
 		orderL = new JLabel();
 		orderL.setText("Order:");
 		GridBagConstraints gridBagConstraints = new GridBagConstraints();
 		gridBagConstraints.anchor = GridBagConstraints.EAST;
 		gridBagConstraints.gridx = 1;
 		gridBagConstraints.gridy = 1;
 		gridBagConstraints.insets = new Insets(2, 3, 2, 3);
 		panel.add(orderL, gridBagConstraints);
 
 		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
 		gridBagConstraints1.gridy = 1;
 		gridBagConstraints1.gridx = 2;
 		panel.add(getOrderCB(), gridBagConstraints1);
 
 		lowFrequencyL = new JLabel();
 		lowFrequencyL.setText("Low cut frequency, Hz:");
 		lowFrequencyL.setLabelFor(lowFrequencyTF);
 		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
 		gridBagConstraints2.gridx = 1;
 		gridBagConstraints2.gridy = 2;
 		gridBagConstraints2.anchor = GridBagConstraints.EAST;
 		gridBagConstraints2.insets = new Insets(2, 3, 2, 3);
 		panel.add(lowFrequencyL, gridBagConstraints2);
 
 		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
 		gridBagConstraints3.fill = GridBagConstraints.VERTICAL;
 		gridBagConstraints3.gridx = 2;
 		gridBagConstraints3.gridy = 2;
 		gridBagConstraints3.insets = new Insets(2, 3, 2, 3);
 		panel.add(getLowFrequencyTF(), gridBagConstraints3);
 
 		highFrequencyL = new JLabel();
 		highFrequencyL.setText("High cut frequency, Hz:");
 		highFrequencyL.setLabelFor(highFrequencyTF);
 		GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
 		gridBagConstraints4.gridx = 1;
 		gridBagConstraints4.gridy = 3;
 		gridBagConstraints4.anchor = GridBagConstraints.EAST;
 		gridBagConstraints4.insets = new Insets(2, 3, 2, 3);
 		panel.add(highFrequencyL, gridBagConstraints4);
 
 		GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
 		gridBagConstraints5.fill = GridBagConstraints.VERTICAL;
 		gridBagConstraints5.gridx = 2;
 		gridBagConstraints5.gridy = 3;
 		gridBagConstraints5.insets = new Insets(2, 3, 2, 3);
 		panel.add(getHighFrequencyTF(), gridBagConstraints5);
 		return panel;
 	}
 
 	public void propertyChange(PropertyChangeEvent e) {
 		String prop = e.getPropertyName();
 		if (isVisible() && (e.getSource() == optionPane) && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
 			Object value = optionPane.getValue();
 			optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
 			// If you were going to check something
 			// before closing the window, you'd do
 			// it here.
 			if (value.equals("Close")) {
 				setVisible(false);
 				needProcessing = false;
 			} else if (value.equals("OK")) {
 				try {
 					cutLowFrequency = Double.parseDouble(lowFrequencyTF.getText());
 				} catch (NumberFormatException e1) {
 					cutLowFrequency = Double.NaN;
 				}
 				try {
 					cutHighFrequency = Double.parseDouble(highFrequencyTF.getText());
 				} catch (NumberFormatException e1) {
 					cutHighFrequency = Double.NaN;
 				}
 System.out.format("==== cutLowFrequency=[%.2f] cutHighFrequency=[%.2f] =========\n", cutLowFrequency, cutHighFrequency);
 				order = (Integer) orderCB.getSelectedItem();
 				if (!Double.isNaN(cutLowFrequency) && !Double.isNaN(cutHighFrequency)) {
 					if (cutLowFrequency < cutHighFrequency) {
 						filter = new FilterBP(order, cutLowFrequency, cutHighFrequency);
 						setVisible(false);
 						needProcessing = true;
 					} else {
 						JOptionPane.showMessageDialog(XMAXframe.getInstance(), "Low frequency should be less then high one", "Error",
 								JOptionPane.ERROR_MESSAGE);
 					}
 				} else if (!Double.isNaN(cutLowFrequency)) {
 					filter = new FilterLP(order, cutLowFrequency);
 					setVisible(false);
 					needProcessing = true;
 				} else if (!Double.isNaN(cutHighFrequency)) {
 					filter = new FilterHP(order, cutHighFrequency);
 					setVisible(false);
 					needProcessing = true;
 				} else {
 					filter = null;
 					JOptionPane.showMessageDialog(XMAXframe.getInstance(), "Please enter either low or high frequencies", "Error",
 							JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method initializes lowFrequencyTF
 	 * 
 	 * @return javax.swing.JTextField
 	 */
 	private JTextField getLowFrequencyTF() {
 		if (lowFrequencyTF == null) {
 			lowFrequencyTF = new JTextField();
 			lowFrequencyTF.setText(new Double(cutLowFrequency).toString());
 			lowFrequencyTF.setPreferredSize(new Dimension(50, 20));
 		}
 		return lowFrequencyTF;
 	}
 
 	/**
 	 * This method initializes highFrequencyTF
 	 * 
 	 * @return javax.swing.JTextField
 	 */
 	private JTextField getHighFrequencyTF() {
 		if (highFrequencyTF == null) {
 			highFrequencyTF = new JTextField();
 			highFrequencyTF.setText(new Double(cutHighFrequency).toString());
 			highFrequencyTF.setPreferredSize(new Dimension(50, 20));
 		}
 		return highFrequencyTF;
 	}
 
 	/**
 	 * This method initializes orderCB
 	 * 
 	 * @return javax.swing.JComboBox
 	 */
 	private JComboBox getOrderCB() {
 		if (orderCB == null) {
 			orderCB = new JComboBox();
 			orderCB.addItem(1);
 			orderCB.addItem(2);
 			orderCB.addItem(3);
 			orderCB.addItem(4);
 			orderCB.addItem(5);
 			orderCB.setSelectedItem(order);
 		}
 		return orderCB;
 	}
 
 	// From interface IFilter
 
 	public double[] filter(double[] data, int length, Date date)  throws TraceViewException {
 		return filter.filter(data, length, date);
 	}
 
 	public void init(RawDataProvider channel) {
 		filter.init(channel);
 	}
 
 	public String getName() {
 		return "DYO";
 	}
 
 	public boolean needProcessing() {
 		return needProcessing;
 	}
 
 	// ----------------------------------------
 
 	public boolean equals(Object o) {
 		if (filter == null)
 			return false;
 		else
 			return filter.equals(o);
 	}
 }
