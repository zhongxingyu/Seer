 package iceepotpc.ui;
 
 import iceepotpc.appication.Context;
 import iceepotpc.charteng.ChartCreator;
 import iceepotpc.servergw.Meauserement;
 import iceepotpc.servergw.Server;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ScrollPaneConstants;
 
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 
 import java.awt.event.ItemListener;
 import java.awt.event.ItemEvent;
 import java.awt.Component;
 import javax.swing.border.CompoundBorder;
 import java.awt.ComponentOrientation;
 import java.awt.Dimension;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.RowSpec;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import javax.swing.border.LineBorder;
 import java.awt.Color;
 
 
 /**
  * @author tsantakis A tab where information and inputs per pot is displayed.
  * 
  */
 public class PotPanel extends JPanel {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private String[] availableMonths = { "Jan", "Feb", "Mar", "Apr", "May",
 			"Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
 	private String[] availableYears = { "2011", "2012", "2013", "2014" };
 
 	private JTextField txtLastValue;
 	private JTextField txtLastTime;
 
 	Context cntx;
 
 	DateFormat df = new SimpleDateFormat();
 	
 	ArrayList<Meauserement> measurements = null;
 
 	/**
 	 * Create the panel.
 	 */
 	public PotPanel(final int pin, final JFrame frame) {
 		setSize(new Dimension(900, 780));
 		setMinimumSize(new Dimension(900, 780));
 		cntx = Context.getInstance();
 		this.setToolTipText("");
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[] { 47, 186, 30, 620 };
 		gridBagLayout.rowHeights = new int[] {29, 450, 85, 0};
 		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0 };
 		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0,
 				Double.MIN_VALUE };
 		setLayout(gridBagLayout);
 
 		// last values panel
 		JPanel pnlLastValues = new JPanel();
 		pnlLastValues.setPreferredSize(new Dimension(410, 20));
 		pnlLastValues.setMinimumSize(new Dimension(410, 20));
 		pnlLastValues.setMaximumSize(new Dimension(410, 20));
 		pnlLastValues
 				.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
 		pnlLastValues.setAlignmentY(Component.TOP_ALIGNMENT);
 		pnlLastValues.setAlignmentX(Component.LEFT_ALIGNMENT);
 		GridBagConstraints gbc_pnlLastValues = new GridBagConstraints();
 		gbc_pnlLastValues.insets = new Insets(0, 0, 5, 0);
 		gbc_pnlLastValues.ipady = 5;
 		gbc_pnlLastValues.ipadx = 5;
 		gbc_pnlLastValues.gridwidth = 3;
 		gbc_pnlLastValues.anchor = GridBagConstraints.NORTHWEST;
 		gbc_pnlLastValues.gridx = 1;
 		gbc_pnlLastValues.gridy = 0;
 		add(pnlLastValues, gbc_pnlLastValues);
 		GridBagLayout gbl_pnlLastValues = new GridBagLayout();
 		gbl_pnlLastValues.columnWidths = new int[] { 150, 90, 40, 120 };
 		gbl_pnlLastValues.rowHeights = new int[] { 20 };
 		gbl_pnlLastValues.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0 };
 		gbl_pnlLastValues.rowWeights = new double[] { 0.0 };
 		pnlLastValues.setLayout(gbl_pnlLastValues);
 
 		JLabel lblLastMeasruement = new JLabel("Last measurement");
 		lblLastMeasruement.setPreferredSize(new Dimension(130, 20));
 		lblLastMeasruement.setSize(new Dimension(130, 20));
 		lblLastMeasruement.setMinimumSize(new Dimension(130, 20));
 		lblLastMeasruement.setMaximumSize(new Dimension(130, 20));
 		lblLastMeasruement.setBorder(new CompoundBorder());
 		GridBagConstraints gbc_lblLastMeasruement = new GridBagConstraints();
 		gbc_lblLastMeasruement.anchor = GridBagConstraints.EAST;
 		gbc_lblLastMeasruement.fill = GridBagConstraints.VERTICAL;
 		gbc_lblLastMeasruement.insets = new Insets(0, 0, 0, 5);
 		gbc_lblLastMeasruement.gridx = 0;
 		gbc_lblLastMeasruement.gridy = 0;
 		pnlLastValues.add(lblLastMeasruement, gbc_lblLastMeasruement);
 
 		txtLastValue = new JTextField();
 		txtLastValue.setColumns(10);
 		txtLastValue.setPreferredSize(new Dimension(90, 20));
 		txtLastValue.setMinimumSize(new Dimension(90, 20));
 		txtLastValue.setMaximumSize(new Dimension(90, 20));
 		GridBagConstraints gbc_txtLastValue = new GridBagConstraints();
 		gbc_txtLastValue.fill = GridBagConstraints.BOTH;
 		gbc_txtLastValue.gridx = 1;
 		gbc_txtLastValue.gridy = 0;
 		pnlLastValues.add(txtLastValue, gbc_txtLastValue);
 		txtLastValue.setEditable(false);
 
 		JLabel lblAt = new JLabel("at");
 		lblAt.setPreferredSize(new Dimension(40, 20));
 		lblAt.setMinimumSize(new Dimension(40, 20));
 		lblAt.setMaximumSize(new Dimension(40, 20));
 		lblAt.setAlignmentX(Component.RIGHT_ALIGNMENT);
 		GridBagConstraints gbc_lblAt = new GridBagConstraints();
 		gbc_lblAt.anchor = GridBagConstraints.EAST;
 		gbc_lblAt.fill = GridBagConstraints.VERTICAL;
 		gbc_lblAt.insets = new Insets(0, 5, 0, 5);
 		gbc_lblAt.gridx = 2;
 		gbc_lblAt.gridy = 0;
 		pnlLastValues.add(lblAt, gbc_lblAt);
 
 		txtLastTime = new JTextField();
 		txtLastTime.setSize(new Dimension(120, 20));
 		txtLastTime.setPreferredSize(new Dimension(120, 20));
 		txtLastTime.setMinimumSize(new Dimension(120, 20));
 		txtLastTime.setMaximumSize(new Dimension(120, 20));
 		GridBagConstraints gbc_txtLastTime = new GridBagConstraints();
 		gbc_txtLastTime.fill = GridBagConstraints.BOTH;
 		gbc_txtLastTime.gridx = 3;
 		gbc_txtLastTime.gridy = 0;
 		pnlLastValues.add(txtLastTime, gbc_txtLastTime);
 		txtLastTime.setColumns(10);
 		txtLastTime.setEditable(false);
 
 		// panel results
 		final JTextArea txtResults = new JTextArea();
 		txtResults.setEditable(false);
 		txtResults.setTabSize(2);
 		txtResults.setPreferredSize(new Dimension(120, 4000));
 		txtResults.setMinimumSize(new Dimension(120, 4000));
 		txtResults.setMaximumSize(new Dimension(120, 4000));
 		// txtResults.setMaximumSize(new Dimension(32767, 32767));
 		// txtResults.setMinimumSize(new Dimension(23, 23));
 
 		// txtResults.setBounds(47, 58, 601, 400);
 		JScrollPane sp = new JScrollPane(txtResults);
 		sp.setPreferredSize(new Dimension(120, 450));
 		sp.setMinimumSize(new Dimension(120, 450));
 		sp.setMaximumSize(new Dimension(120, 450));
 		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		GridBagConstraints gbc_sp = new GridBagConstraints();
 		gbc_sp.fill = GridBagConstraints.BOTH;
 		gbc_sp.insets = new Insets(0, 0, 5, 5);
 		gbc_sp.gridx = 1;
 		gbc_sp.gridy = 1;
 		this.add(sp, gbc_sp);
 		txtResults.setColumns(1);
 		txtResults.setRows(30);
 
 		final ChartPanel pnlChart = new ChartPanel(null);
 		pnlChart.setMaximumDrawWidth(2048);
 		pnlChart.setMinimumDrawWidth(620);
 		pnlChart.setMinimumSize(new Dimension(620, 450));
 		pnlChart.setPreferredSize(new Dimension(620, 450));
 		//FlowLayout flowLayout = (FlowLayout) pnlChart.getLayout();
 		GridBagConstraints gbc_pnlChart = new GridBagConstraints();
 		gbc_pnlChart.fill = GridBagConstraints.BOTH;
 		gbc_pnlChart.insets = new Insets(0, 0, 5, 0);
 		gbc_pnlChart.gridx = 3;
 		gbc_pnlChart.gridy = 1;
 		add(pnlChart, gbc_pnlChart);
 
 		// criteria panel
 		JPanel pnlCriteria = new JPanel();
 		pnlCriteria.setSize(new Dimension(500, 85));
 		pnlCriteria.setMinimumSize(new Dimension(500, 85));
 		pnlCriteria.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
 		pnlCriteria.setAlignmentY(Component.BOTTOM_ALIGNMENT);
 		pnlCriteria.setAlignmentX(Component.RIGHT_ALIGNMENT);
 		GridBagConstraints gbc_pnlCriteria = new GridBagConstraints();
 		gbc_pnlCriteria.insets = new Insets(0, 5, 0, 0);
 		gbc_pnlCriteria.anchor = GridBagConstraints.SOUTHEAST;
 		gbc_pnlCriteria.gridx = 3;
 		gbc_pnlCriteria.gridy = 2;
 		add(pnlCriteria, gbc_pnlCriteria);
 		pnlCriteria.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.UNRELATED_GAP_COLSPEC,
 				ColumnSpec.decode("60px"),
 				FormFactory.UNRELATED_GAP_COLSPEC,
 				ColumnSpec.decode("112px"),
 				FormFactory.UNRELATED_GAP_COLSPEC,
 				ColumnSpec.decode("112px"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.UNRELATED_GAP_COLSPEC,},
 			new RowSpec[] {
 				FormFactory.UNRELATED_GAP_ROWSPEC,
 				RowSpec.decode("26px"),
 				FormFactory.UNRELATED_GAP_ROWSPEC,
 				RowSpec.decode("26px"),
 				FormFactory.UNRELATED_GAP_ROWSPEC,}));
 
 		JLabel lblDateFrom = new JLabel("From");
 		pnlCriteria.add(lblDateFrom, "2, 2, fill, fill");
 		final JComboBox cmbMonthFrom = new JComboBox();
 		pnlCriteria.add(cmbMonthFrom, "4, 2, fill, fill");
 		JLabel lblDateTo = new JLabel("To");
 		pnlCriteria.add(lblDateTo, "2, 4, fill, fill");
 		final JComboBox cmbMonthTo = new JComboBox();
 		pnlCriteria.add(cmbMonthTo, "4, 4, fill, fill");
 		final JComboBox cmbYearTo = new JComboBox();
 		pnlCriteria.add(cmbYearTo, "6, 4, fill, fill");
 
 		JButton btnGet = new JButton("Get Information");
 		pnlCriteria.add(btnGet, "8, 4, fill, fill");
 
 		// handlers
 		
 		this.addComponentListener(new ComponentListener() {
 			
 			@Override
 			public void componentShown(ComponentEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void componentResized(ComponentEvent e) {
 				if(measurements!= null){
 				JFreeChart fc = ChartCreator
 						.createChart(measurements);
 				// pnlChart = new ChartPanel(fc, false);
 				pnlChart.setChart(fc);
 				// pnlChart.setBounds(263, 70, 620, 460);
 				pnlChart.setVisible(true);
 				}
 				
 			}
 			
 			@Override
 			public void componentMoved(ComponentEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void componentHidden(ComponentEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		
 		cmbMonthFrom.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent arg0) {
 				cmbMonthTo.setSelectedIndex(cmbMonthFrom.getSelectedIndex());
 			}
 		});
 		final JComboBox cmbYearFrom = new JComboBox();
 		pnlCriteria.add(cmbYearFrom, "6, 2, fill, fill");
 
 		cmbYearFrom.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent e) {
 				cmbYearTo.setSelectedIndex(cmbYearFrom.getSelectedIndex());
 			}
 		});
 
 		
 
 		btnGet.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
				txtResults.setText("");
 				Calendar from = Calendar.getInstance();
 				from.set(Calendar.MONTH, cmbMonthFrom.getSelectedIndex() + 1);
 				from.set(Calendar.DAY_OF_MONTH, 1);
 				from.set(Calendar.YEAR, Integer.parseInt((String) cmbYearFrom
 						.getSelectedItem()));
 
 				Calendar to = Calendar.getInstance();
 				to.set(Calendar.MONTH, cmbMonthTo.getSelectedIndex() + 1);
 				to.set(Calendar.DAY_OF_MONTH, 1);
 				to.set(Calendar.YEAR,
 						Integer.parseInt((String) cmbYearTo.getSelectedItem()));
 
 				if (from.getTime().getTime() > to.getTime().getTime()) {
 					JOptionPane.showMessageDialog(frame,
 							"The \"From\" date is after the \"To\" one",
 							"Warning", JOptionPane.ERROR_MESSAGE);
 				} else {
 					measurements = null;
 					try {
 						measurements = Server.GetMeasurements(from, to, pin,
 								cntx);
 						if (measurements == null || measurements.size() == 0)
 							JOptionPane.showMessageDialog(frame,
 									"Measurements not available yet",
 									"Warning", JOptionPane.WARNING_MESSAGE);
 						else {
 
 							for (int i = 0; i < measurements.size(); i++)
 								txtResults.setText(txtResults.getText()
 										+ "\n"
 										+ df.format(new Date(measurements
 												.get(i).getMoment())) + "\t"
 										+ measurements.get(i).getValue());
 
 							Calendar c = Calendar.getInstance();
 							c.setTimeInMillis((long) measurements.get(
 									measurements.size() - 1).getMoment());
 
 							txtLastTime.setText(df.format(c.getTime()));
 
 							txtLastValue.setText(String.valueOf(measurements
 									.get(measurements.size() - 1).getValue()));
 
 							JFreeChart fc = ChartCreator
 									.createChart(measurements);
 							// pnlChart = new ChartPanel(fc, false);
 							pnlChart.setChart(fc);
 							// pnlChart.setBounds(263, 70, 620, 460);
 							pnlChart.setVisible(true);
 
 						}
 					} catch (Exception e1) {
 						JOptionPane.showMessageDialog(frame, e1.getMessage(),
 								"Warning", JOptionPane.ERROR_MESSAGE);
 					}
 
 				}
 
 			}
 		});
 
 		// filling data
 		for (int i = 0; i < availableMonths.length; i++)
 			cmbMonthTo.addItem(availableMonths[i]);
 		for (int i = 0; i < availableYears.length; i++)
 			cmbYearTo.addItem(availableYears[i]);
 		for (int i = 0; i < availableMonths.length; i++)
 			cmbMonthFrom.addItem(availableMonths[i]);
 		for (int i = 0; i < availableYears.length; i++)
 			cmbYearFrom.addItem(availableYears[i]);
 
 	}
 
 }
