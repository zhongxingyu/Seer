 package org.strangeforest.currencywatch.ui;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.text.*;
 import javax.swing.*;
 
 import org.jfree.chart.*;
 import org.jfree.chart.axis.*;
 import org.jfree.chart.labels.*;
 import org.jfree.chart.plot.*;
 import org.jfree.chart.renderer.xy.*;
 
 import com.finsoft.util.*;
 
 public class CurrencyChartForm {
 
 	private JButton exitButton;
 	private JPanel formPanel;
 	private JPanel toolPanel;
 	private JComboBox<String> currencyComboBox;
 	private JComboBox<String> periodComboBox;
 	private JComboBox<String> qualityComboBox;
 	private JCheckBox movAvgCheckBox;
 	private JCheckBox bollingerBandsCheckBox;
 	private JComboBox<Integer> movAvgPeriodComboBox;
 	private ChartPanel chartPanel;
 	private JPanel statusPanel;
 	private JLabel speedLabel;
 	private JProgressBar progressBar;
 
 	private CurrencyRatePresenter presenter;
 
 	public static void main(String[] args) throws IOException {
 		final CurrencyChartForm form = new CurrencyChartForm();
 		form.inputDataChanged();
 		final JFrame frame = new JFrame(String.format("Currency Watch %s", ManifestUtil.getManifestAttribute("currency-watch", "Implementation-Version")));
 		frame.setContentPane(form.formPanel);
 		form.exitButton.addActionListener(new ActionListener() {
 			@Override public void actionPerformed(ActionEvent e) {
				form.presenter.close();
 				frame.dispose();
 			}
 		});
 		frame.pack();
 		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 	}
 
 	public CurrencyChartForm() {
 		presenter = new CurrencyRatePresenter(chartPanel, progressBar, speedLabel);
 		currencyComboBox.setModel(new DefaultComboBoxModel<>(CurrencyRatePresenter.CURRENCIES));
 		currencyComboBox.setSelectedItem(CurrencyRatePresenter.DEFAULT_CURRENCY);
 		currencyComboBox.addActionListener(new ActionListener() {
 			@Override public void actionPerformed(ActionEvent e) {
 				inputDataChanged();
 			}
 		});
 		periodComboBox.setModel(new DefaultComboBoxModel<>(CurrencyRatePresenter.periods()));
 		periodComboBox.setSelectedItem(CurrencyRatePresenter.DEFAULT_PERIOD);
 		periodComboBox.addActionListener(new ActionListener() {
 			@Override public void actionPerformed(ActionEvent e) {
 				inputDataChanged();
 			}
 		});
 		qualityComboBox.setModel(new DefaultComboBoxModel<>(CurrencyRatePresenter.qualities()));
 		qualityComboBox.setSelectedItem(CurrencyRatePresenter.DEFAULT_QUALITY);
 		qualityComboBox.addActionListener(new ActionListener() {
 			@Override public void actionPerformed(ActionEvent e) {
 				inputDataChanged();
 			}
 		});
 		movAvgCheckBox.addActionListener(new ActionListener() {
 			@Override public void actionPerformed(ActionEvent e) {
 				setPeriodComboBoxEnabled();
 				inputDataChanged();
 			}
 		});
 		bollingerBandsCheckBox.addActionListener(new ActionListener() {
 			@Override public void actionPerformed(ActionEvent e) {
 				setPeriodComboBoxEnabled();
 				inputDataChanged();
 			}
 		});
 		movAvgPeriodComboBox.setModel(new DefaultComboBoxModel<>(CurrencyRatePresenter.MOV_AVG_PERIODS));
 		movAvgPeriodComboBox.setSelectedItem(CurrencyRatePresenter.DEFAULT_MOV_AVG_PERIOD);
 		movAvgPeriodComboBox.addActionListener(new ActionListener() {
 			@Override public void actionPerformed(ActionEvent e) {
 				inputDataChanged();
 			}
 		});
 		formPanel.addComponentListener(new ComponentAdapter() {
 			@Override public void componentResized(ComponentEvent e) {
 				chartPanel.setSize(new Dimension(
 					formPanel.getWidth(),
 					formPanel.getHeight() - toolPanel.getHeight() - statusPanel.getHeight() - 5
 				));
 			}
 		});
 	}
 
 	private void createUIComponents() {
 		DateAxis xAxis = new DateAxis("Date");
 		NumberAxis yAxis = new NumberAxis("Middle");
 		yAxis.setAutoRangeIncludesZero(false);
 
 		XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
 		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator(
 			StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
 			new SimpleDateFormat("d-MMM-yyyy"), new DecimalFormat("0.00")
 		));
 
 		XYAreaRenderer2 bbRenderer = new XYAreaRenderer2();
 		bbRenderer.setSeriesPaint(0, new Color(255, 255, 255, 0));
 		bbRenderer.setSeriesPaint(1, new Color(255, 255, 255, 255));
 		bbRenderer.setSeriesPaint(2, new Color(192, 224, 255, 64));
 		bbRenderer.setOutline(false);
 
 		XYPlot plot = new XYPlot(null, xAxis, yAxis, renderer);
 		plot.setOrientation(PlotOrientation.VERTICAL);
 		plot.setRenderer(1, bbRenderer);
 
 		JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
 		chart.setAntiAlias(true);
 		chart.setBackgroundPaint(SystemColor.control);
 		chartPanel = new ChartPanel(chart);
 	}
 
 	private void inputDataChanged() {
 		presenter.inputDataChanged(
 			(String)currencyComboBox.getSelectedItem(),
 			(String)periodComboBox.getSelectedItem(),
 			(String)qualityComboBox.getSelectedItem(),
 			movAvgCheckBox.isSelected(),
 			bollingerBandsCheckBox.isSelected(),
 			(Integer)movAvgPeriodComboBox.getSelectedItem()
 		);
 	}
 
 	private void setPeriodComboBoxEnabled() {
 		movAvgPeriodComboBox.setEnabled(movAvgCheckBox.isSelected() || bollingerBandsCheckBox.isSelected());
 	}
 }
