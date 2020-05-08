 package org.strangeforest.currencywatch.app;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 import javax.swing.*;
 
 import org.jfree.chart.*;
 import org.strangeforest.currencywatch.core.*;
 import org.strangeforest.currencywatch.ui.*;
 
 import com.finsoft.util.*;
 
 import static org.strangeforest.currencywatch.ui.UIUtil.*;
 
 public class CurrencyChartForm {
 
 	JPanel formPanel;
 	private JPanel toolPanel;
 	private JComboBox<CurrencySymbol> currencyComboBox;
 	private JComboBox<Period> periodComboBox;
 	private JComboBox<SeriesQuality> qualityComboBox;
 	private JCheckBox bidAskCheckBox;
 	private JCheckBox movAvgCheckBox;
 	private JCheckBox bollingerBandsCheckBox;
 	private JComboBox<Integer> movAvgPeriodComboBox;
 	private JButton exitButton;
 	private ChartPanel chartPanel;
 	private JPanel statusPanel;
 	private JLabel messageLabel;
 	private JProgressBar progressBar;
 	private JLabel statusLabel;
	private JLabel speedLabel;
 
 	private final CurrencyRatePresenter presenter;
 
 	public CurrencyChartForm(CurrencyRatePresenter presenter, final JFrame frame) {
 		this.presenter = presenter;
 		presenter.addListener(new FormCurrencyRatePresenterListener());
 		currencyComboBox.setModel(new DefaultComboBoxModel<>(CurrencySymbol.values()));
 		currencyComboBox.setSelectedItem(DEFAULT_CURRENCY);
 		currencyComboBox.addActionListener(new InputDataListener());
 		periodComboBox.setModel(new DefaultComboBoxModel<>(Period.values()));
 		periodComboBox.setSelectedItem(DEFAULT_PERIOD);
 		periodComboBox.addActionListener(new InputDataListener());
 		qualityComboBox.setModel(new DefaultComboBoxModel<>(SeriesQuality.values()));
 		qualityComboBox.setSelectedItem(DEFAULT_QUALITY);
 		qualityComboBox.addActionListener(new InputDataListener());
 		bidAskCheckBox.addActionListener(new InputDataListener());
 		movAvgCheckBox.addActionListener(new PeriodComboBoxEnabledInputDataListener());
 		bollingerBandsCheckBox.addActionListener(new PeriodComboBoxEnabledInputDataListener());
 		movAvgPeriodComboBox.setModel(new DefaultComboBoxModel<>(MOV_AVG_PERIODS));
 		movAvgPeriodComboBox.setSelectedItem(DEFAULT_MOV_AVG_PERIOD);
 		movAvgPeriodComboBox.addActionListener(new InputDataListener());
 		exitButton.addActionListener(new ActionListener() {
 			@Override public void actionPerformed(ActionEvent e) {
 				frame.dispose();
 			}
 		});
 		formPanel.addComponentListener(new ComponentAdapter() {
 			@Override public void componentResized(ComponentEvent e) {
 				resizeChartPanel();
 			}
 		});
 	}
 
 	private void createUIComponents() {
 		chartPanel = new ChartPanel(presenter.getChart());
 	}
 
 	void inputDataChanged() {
 		presenter.inputDataChanged(
 			(CurrencySymbol)currencyComboBox.getSelectedItem(),
 			(Period)periodComboBox.getSelectedItem(),
 			(SeriesQuality)qualityComboBox.getSelectedItem(),
 			bidAskCheckBox.isSelected(),
 			movAvgCheckBox.isSelected(),
 			bollingerBandsCheckBox.isSelected(),
 			(Integer)movAvgPeriodComboBox.getSelectedItem()
 		);
 	}
 
 	private void setMovAvgPeriodComboBoxEnabled() {
 		movAvgPeriodComboBox.setEnabled(movAvgCheckBox.isSelected() || bollingerBandsCheckBox.isSelected());
 	}
 
 	private void resizeChartPanel() {
 		Dimension size = new Dimension(
 			formPanel.getWidth(),
 			formPanel.getHeight() - toolPanel.getHeight() - statusPanel.getHeight() - 10
 		);
 		chartPanel.setSize(size);
 		chartPanel.setPreferredSize(size);
 		chartPanel.setMinimumSize(size);
 		chartPanel.setMaximumSize(size);
 	}
 
 	private class FormCurrencyRatePresenterListener implements CurrencyRatePresenterListener {
 		@Override public void currentRate(CurrentRate currentRate) {
 			RateValue rate = currentRate.getRate();
 			messageLabel.setText(String.format("<html>&nbsp;%1$td-%1$tm-%1$tY: Bid %2$.2f, Middle <span style='color: %5$s;'>%3$.2f</span>, Ask %4$.2f</html>", currentRate.getDate(), rate.getBid(), rate.getMiddle(), rate.getAsk(), currentRate.getColor()));
 		}
 		@Override public void statusChanged(String status, boolean isError) {
 			statusLabel.setText(StringUtil.maxLength(status, 25));
 			statusLabel.setForeground(isError ? Color.RED : Color.BLACK);
 		}
 		@Override public void progressChanged(int progress) {
 			progressBar.setValue(progress);
 		}
 		@Override public void ratesPerSecChanged(double ratesPerSec) {
 			speedLabel.setText(ratesPerSec > 0.0 ? String.format("%8.1f", ratesPerSec) + " rate/s" : StringUtil.EMPTY);
 		}
 	}
 
 	private class InputDataListener implements ActionListener {
 		@Override public void actionPerformed(ActionEvent e) {
 			inputDataChanged();
 		}
 	}
 
 	private class PeriodComboBoxEnabledInputDataListener implements ActionListener {
 		@Override public void actionPerformed(ActionEvent e) {
 			setMovAvgPeriodComboBoxEnabled();
 			inputDataChanged();
 		}
 	}
 }
