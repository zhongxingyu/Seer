 package org.gwaspi.gui.reports;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.io.File;
 import java.io.IOException;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import javax.swing.AbstractAction;
 import javax.swing.GroupLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.LayoutStyle;
 import org.gwaspi.global.Config;
 import org.gwaspi.global.Text;
 import org.gwaspi.global.Utils;
 import org.gwaspi.gui.GWASpiExplorerPanel;
 import org.gwaspi.gui.MatrixAnalysePanel;
 import org.gwaspi.gui.utils.CursorUtils;
 import org.gwaspi.gui.utils.Dialogs;
 import org.gwaspi.model.Operation;
 import org.gwaspi.model.OperationsList;
 import org.gwaspi.netCDF.operations.MarkerOperationSet;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author Fernando Muñiz Fernandez
  * IBE, Institute of Evolutionary Biology (UPF-CSIC)
  * CEXS-UPF-PRBB
  */
 public final class ManhattanChartDisplay extends JPanel {
 
 	private static final Logger log
 			= LoggerFactory.getLogger(ManhattanChartDisplay.class);
 
 	// Variables declaration - do not modify
 	private JPanel pnl_Chart;
 	private JPanel pnl_Footer;
 	private JScrollPane scrl_Chart;
 //	private ManhattanPlotImageLabel label = new ManhattanPlotImageLabel();
 	private JLabel label = new JLabel();
 	private boolean fired;
 	private JButton btn_Save;
 	private JButton btn_Back;
 	private int opId;
 	private Map<String, int[]> chrSetInfoMap = new LinkedHashMap<String, int[]>();
 	private String chr = "";
 	private int chartWidth = 0;
 	private int chrPlotWidth = 0;
 	private int chrPlotWidthPad = 0;
 	private int padLeft = 64; // Pixel padding to the left of graph
 	private int padGap = 9; // Pixel padding between chromosome plots
 	// End of variables declaration
 
 	public ManhattanChartDisplay(final int studyId, final String chartPath, int _opId) {
 		fired = false;
 		initManhattanChartDisplay(studyId, chartPath, _opId);
 		initChromosmesMap(studyId, chartPath);
 	}
 
 	private void initManhattanChartDisplay(final int studyId, final String chartPath, int _opId) {
 
 		opId = _opId;
 
 		scrl_Chart = new JScrollPane();
 		pnl_Chart = new JPanel();
 		pnl_Chart.setCursor(CursorUtils.HAND_CURSOR);
 
 		label.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				try {
 					if (e.getClickCount() == 1 && !isFired()) {
 						setFired(true);
 						int mouseX = e.getX();
 						if (mouseX > padLeft) {
 							pnl_Chart.setCursor(CursorUtils.WAIT_CURSOR);
 
 							Object[] selectedSliceInfo = getChrSliceInfo(mouseX);
 //							sliceInfo[0] = chrNb;
 //							sliceInfo[1] = chr;
 //							sliceInfo[2] = sliceNb;
 //							sliceInfo[3] = startPhysPos;
 //							sliceInfo[4] = defaultSlotsNb;
 
 							GWASpiExplorerPanel.getSingleton().setPnl_Content(new ManhattanPlotZoom(
 									ManhattanChartDisplay.this,
 									opId,
 									selectedSliceInfo[1].toString(),
 									(Long) selectedSliceInfo[3], // startPhysPos
 									(Long) selectedSliceInfo[4], // physPos window
 									100));
 							GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
 						}
 					}
 				} catch (NumberFormatException ex) {
 					log.warn(null, ex);
 				}
 			}
 		});
 		label.addMouseMotionListener(new MouseMotionAdapter() {
 			@Override
 			public void mouseMoved(MouseEvent e) {
 				int mouseX = e.getX();
 				if (mouseX > padLeft) {
 					Object[] sliceInfo = getChrSliceInfo(mouseX);
 
 					label.setToolTipText("<html>Zoom on chr " + sliceInfo[1].toString()
 							+ "<br>position " + sliceInfo[3] + " to " + ((Long) sliceInfo[3] + (Long) sliceInfo[4])
 							+ "</html>");
 				}
 			}
 		});
 
 		pnl_Footer = new JPanel();
 		btn_Save = new JButton();
 		btn_Back = new JButton();
 
 		//<editor-fold defaultstate="expanded" desc="">
 		btn_Save.setAction(new SaveAsAction(studyId, chartPath));
 
 		btn_Back.setAction(new BackToTableAction(opId));
 
 		GroupLayout pnl_FooterLayout = new GroupLayout(pnl_Footer);
 		pnl_Footer.setLayout(pnl_FooterLayout);
 		pnl_FooterLayout.setHorizontalGroup(
 				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
 				.addContainerGap()
 				.addComponent(btn_Back, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE)
 				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 431, Short.MAX_VALUE)
 				.addComponent(btn_Save, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE)
 				.addContainerGap()));
 		pnl_FooterLayout.setVerticalGroup(
 				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 				.addComponent(btn_Save)
 				.addComponent(btn_Back)));
 		//</editor-fold>
 
 		//<editor-fold defaultstate="expanded" desc="LAYOUT">
 		GroupLayout pnl_ChartLayout = new GroupLayout(pnl_Chart);
 		pnl_Chart.setLayout(pnl_ChartLayout);
 		pnl_ChartLayout.setHorizontalGroup(
 				pnl_ChartLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGap(0, 819, Short.MAX_VALUE)
 				.addGroup(pnl_ChartLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(pnl_ChartLayout.createSequentialGroup()
 				.addContainerGap()
 				.addComponent(scrl_Chart, GroupLayout.DEFAULT_SIZE, 795, Short.MAX_VALUE)
 				.addContainerGap())));
 		pnl_ChartLayout.setVerticalGroup(
 				pnl_ChartLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGap(0, 544, Short.MAX_VALUE)
 				.addGroup(pnl_ChartLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(pnl_ChartLayout.createSequentialGroup()
 				.addContainerGap()
 				.addComponent(scrl_Chart, GroupLayout.DEFAULT_SIZE, 520, Short.MAX_VALUE)
 				.addContainerGap())));
 
 		GroupLayout layout = new GroupLayout(this);
 		this.setLayout(layout);
 		layout.setHorizontalGroup(
 				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
 				.addContainerGap()
 				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
 				.addComponent(pnl_Footer, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 				.addComponent(pnl_Chart, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 				.addContainerGap()));
 		layout.setVerticalGroup(
 				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
 				.addContainerGap()
 				.addComponent(pnl_Chart, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 				.addComponent(pnl_Footer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 				.addContainerGap()));
 		//</editor-fold>
 	}
 
 	private void initChromosmesMap(int studyId, String chartPath) {
 
 		// LOAD MANHATTANPLOT IMAGE
 		try {
 			String reportPath = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "") + "/STUDY_" + studyId + "/";
 			File testF = new File(reportPath + chartPath);
 			if (testF.exists()) {
 				ImageIcon image = new ImageIcon(testF.getPath());
 
 				label.setIcon(image);
 				//label.setHorizontalAlignment(SwingConstants.CENTER);
 
 				chartWidth = image.getIconWidth() - padLeft; // Get width of loaded manhattan plot and remove 62 pixels for Y axis labels
 
 				// Creating a Scroll pane component
 				scrl_Chart.getViewport().add(label);
 				pnl_Chart.add(scrl_Chart, BorderLayout.CENTER);
 				pnl_Chart.doLayout();
 
 			}
 		} catch (IOException ex) {
 			log.warn(Text.Reports.warnCantOpenFile, ex);
 			Dialogs.showWarningDialogue(Text.Reports.warnCantOpenFile);
 		}
 
 		try {
 			MarkerOperationSet opSet = new MarkerOperationSet(studyId, opId);
 			chrSetInfoMap = opSet.getChrInfoSetMap();
 
 			// CHECK HOW MANY CHR HAVE PLOTS (ANY MARKERS?)
 			int chrPlotNb = 0;
 			for (int[] value : chrSetInfoMap.values()) {
 				int[] chrInfo = value; // Nb of markers, first physical position, last physical position, start index number in MarkerSet,
 				if (chrInfo[0] > 0) {
 					chrPlotNb++;
 				}
 			}
 
 			// CALCULATE THE WIDTH OF 1 CHR PLOT
 			chrPlotWidth = Math.round(
 					(float) (chartWidth - ((chrPlotNb - 1) * padGap))
 					/ chrPlotNb);
 
 			chrPlotWidthPad = chrPlotWidth + padGap;
 		} catch (IOException ex) {
 			log.error(null, ex);
 		}
 	}
 
 	private Object[] getChrSliceInfo(int pxXpos) {
 
 		int pxXposNoLeftPad = pxXpos - padLeft;
 
 		int[] chrInfo = getChrInfo(pxXposNoLeftPad); // Nb of markers, first physical position, last physical position, start index number in MarkerSet, placeholder
 
 		int nbMarkers = chrInfo[0];
 		int startPhysPos = chrInfo[1];
 		int maxPhysPos = chrInfo[2];
 		int startIdx = chrInfo[3];
 
 		double avgMarkersPerPx = (double) nbMarkers / chrPlotWidth;
 		double avgSlotsPerPx = (double) (maxPhysPos - startPhysPos) / chrPlotWidth;
 
 		int defaultSliceWidth;
 		// FIXME the following if statement does not make sense (two times the same code)
 		if (avgMarkersPerPx < 0) {
 			defaultSliceWidth = (int) Math.round(ManhattanPlotZoom.MARKERS_NUM_DEFAULT / avgMarkersPerPx); // width of a slice inside current chr
 		} else {
 			defaultSliceWidth = (int) Math.round(ManhattanPlotZoom.MARKERS_NUM_DEFAULT / avgMarkersPerPx); // width of a slice inside current chr
 		}
 		long defaultSlotsNb = Math.round(defaultSliceWidth * avgSlotsPerPx); // Nb of physical slots per slice
 
 		int chrNb = Math.round((float) pxXposNoLeftPad / chrPlotWidthPad);
 		int pxXRest = pxXposNoLeftPad - (chrNb * chrPlotWidthPad);
		int sliceNb = Math.round(pxXRest / defaultSliceWidth);
 		long longStartPhysPos = startPhysPos + (sliceNb * defaultSlotsNb);
 
 		Object[] sliceInfo = new Object[5];
 		sliceInfo[0] = chrNb;
 		sliceInfo[1] = chr;
 		sliceInfo[2] = sliceNb;
 		sliceInfo[3] = longStartPhysPos;
 		sliceInfo[4] = defaultSlotsNb;
 
 		return sliceInfo;
 	}
 
 	private int[] getChrInfo(int pxXposNoLeftPad) {
 
 		int selectedChrMap = Math.round((float) pxXposNoLeftPad / chrPlotWidthPad);
 
 		int[] chrInfo = new int[4];
 		int i = 0;
 		for (Map.Entry<String, int[]> entry : chrSetInfoMap.entrySet()) {
 			if ((i > selectedChrMap) || (i >= chrSetInfoMap.size())) {
 				break;
 			}
 			chr = entry.getKey();
 			chrInfo = entry.getValue(); // Nb of markers, first physical position, last physical position, start index number in MarkerSet,
 			i++;
 		}
 
 		return chrInfo;
 	}
 
 	public boolean isFired() {
 		return fired;
 	}
 
 	public void setFired(boolean fired) {
 		this.fired = fired;
 	}
 
 	private static class SaveAsAction extends AbstractAction {
 
 		private int studyId;
 		private String chartPath;
 
 		SaveAsAction(int studyId, String chartPath) {
 
 			this.studyId = studyId;
 			this.chartPath = chartPath;
 			putValue(NAME, Text.All.save);
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent evt) {
 			try {
 				String reportPath = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "") + "/STUDY_" + studyId + "/";
 				File origFile = new File(reportPath + chartPath);
 				File newFile = new File(Dialogs.selectDirectoryDialog(JOptionPane.OK_OPTION).getPath() + "/" + chartPath);
 				if (origFile.exists()) {
 					Utils.copyFile(origFile, newFile);
 				}
 			} catch (IOException ex) {
 				log.error(null, ex);
 			} catch (Exception ex) {
 				log.error(null, ex);
 			}
 		}
 	}
 
 	private static class BackToTableAction extends AbstractAction {
 
 		private int opId;
 
 		BackToTableAction(int opId) {
 
 			this.opId = opId;
 			putValue(NAME, Text.All.Back);
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent evt) {
 			try {
 				Operation op = OperationsList.getById(opId);
 				GWASpiExplorerPanel.getSingleton().getTree().setSelectionPath(GWASpiExplorerPanel.getSingleton().getTree().getSelectionPath().getParentPath());
 				GWASpiExplorerPanel.getSingleton().setPnl_Content(new MatrixAnalysePanel(op.getParentMatrixId(), opId));
 				GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
 			} catch (IOException ex) {
 				log.error(null, ex);
 			}
 		}
 	}
 }
