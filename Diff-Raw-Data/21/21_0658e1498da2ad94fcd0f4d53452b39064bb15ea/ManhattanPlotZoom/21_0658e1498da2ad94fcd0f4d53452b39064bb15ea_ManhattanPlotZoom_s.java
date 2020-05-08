 package org.gwaspi.gui.reports;
 
 import org.gwaspi.constants.cNetCDF;
 import org.gwaspi.constants.cNetCDF.Defaults.OPType;
 import org.gwaspi.global.Config;
 import org.gwaspi.global.Text;
 import org.gwaspi.gui.GWASpiExplorerPanel;
 import org.gwaspi.gui.StartGWASpi;
 import org.gwaspi.gui.utils.CursorUtils;
 import org.gwaspi.gui.utils.Dialogs;
 import org.gwaspi.gui.utils.LinksExternalResouces;
 import org.gwaspi.gui.utils.URLInDefaultBrowser;
 import java.awt.BasicStroke;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.io.File;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.List;
 import java.util.Map;
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.GroupLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.LayoutStyle;
 import javax.swing.SwingConstants;
 import javax.swing.border.TitledBorder;
 import org.gwaspi.model.Operation;
 import org.gwaspi.model.Report;
 import org.gwaspi.model.ReportsList;
 import org.gwaspi.netCDF.matrices.MatrixMetadata;
 import org.gwaspi.netCDF.operations.OperationMetadata;
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartMouseEvent;
 import org.jfree.chart.ChartMouseListener;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.ChartRenderingInfo;
 import org.jfree.chart.ChartUtilities;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.LogAxis;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.axis.TickUnitSource;
 import org.jfree.chart.axis.ValueAxis;
 import org.jfree.chart.labels.AbstractXYItemLabelGenerator;
 import org.jfree.chart.labels.ItemLabelAnchor;
 import org.jfree.chart.labels.ItemLabelPosition;
 import org.jfree.chart.labels.StandardXYToolTipGenerator;
 import org.jfree.chart.labels.XYItemLabelGenerator;
 import org.jfree.chart.labels.XYToolTipGenerator;
 import org.jfree.chart.plot.Marker;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.ValueMarker;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.XYItemRenderer;
 import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
 import org.jfree.data.xy.XYDataset;
 import org.jfree.ui.RectangleAnchor;
 import org.jfree.ui.RectangleEdge;
 import org.jfree.ui.TextAnchor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.gwaspi.reports.GenericReportGenerator;
 
 /**
  *
  * @author fernando
  */
 public final class ManhattanPlotZoom extends JPanel {
 
 	private final Logger log = LoggerFactory.getLogger(ManhattanPlotZoom.class);
 
 	private int opId;
 	private Operation op;
 	private OperationMetadata rdOPMetadata;
 	private Map<String, Object> labelerMap;
 	private MatrixMetadata rdMatrixMetadata;
 	private String origMarkerId;
 	private String origChr;
 	private String currentMarkerId;
 	private String currentChr;
 	private String txt_NRows;
 	private long centerPhysPos;
 	private long startPhysPos;
 	public static final int defaultMarkerNb = (int) Math.round(100000 * ((double) StartGWASpi.maxHeapSize / 2000)); //roughly 2000MB needed per 100.000 plotted markers
 	private long requestedSetSize;
 	private long requestedPosWindow;
 	//private int sliderSize;
 	private XYDataset initXYDataset;
 	private JFreeChart zoomChart;
 	private ChartPanel zoomPanel;
	protected double threshold = 5E-7;
	protected Color manhattan_back = Color.getHSBColor(0.1f, 0.0f, 0.9f);
	protected Color manhattan_backalt = Color.getHSBColor(0.1f, 0.0f, 0.85f);
	protected Color manhattan_dot = Color.blue;
 	// Variables declaration - do not modify
 	private JButton btn_Back;
 	private JButton btn_Back2;
 	private JButton btn_Reset;
 	private JButton btn_Save;
 	private JPanel pnl_Chart;
 	private JPanel pnl_ChartNavigator;
 	private JPanel pnl_SearchDB;
 	private JComboBox cmb_SearchDB;
 	private JPanel pnl_Footer;
 	private JPanel pnl_FooterGroup1;
 	//private JPanel pnl_Tracker;
 	private JScrollPane scrl_Chart;
 	private ManhattanChartDisplay parent;
 	//private JSlider slid_Tracker;
 	// End of variables declaration
 
 	/**
 	 * Creates new form ManhattanPlotZoom
 	 *
 	 * @param opId
 	 * @param txt_NRows
 	 * @param _startIdxPos
 	 * @param _requestedSetSize
 	 */
 	public ManhattanPlotZoom(
 			ManhattanChartDisplay parent,
 			int opId,
 			String chr,
 			long startPhysPos,
 			long requestedPosWindow,
 			String txt_NRows)
 	{
 //		long start = new Date().getTime();
 
 		this.parent = parent;
		opId = opId;
		currentChr = chr;
		origChr = chr;
		txt_NRows = txt_NRows;
		startPhysPos = startPhysPos;
		requestedPosWindow = requestedPosWindow;
 
 		//<editor-fold defaultstate="collapsed" desc="PLOT DEFAULTS">
 		try {
 			threshold = Double.parseDouble(Config.getConfigValue("CHART_MANHATTAN_PLOT_THRESHOLD", "5E-7"));
 
 			String[] tmp = Config.getConfigValue("CHART_MANHATTAN_PLOT_BCKG", "200,200,200").split(",");
 			float[] hsbTmp = Color.RGBtoHSB(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]), null);
 			manhattan_back = Color.getHSBColor(hsbTmp[0], hsbTmp[1], hsbTmp[2]);
 
 			tmp = Config.getConfigValue("CHART_MANHATTAN_PLOT_DOT", "0,0,255").split(",");
 			hsbTmp = Color.RGBtoHSB(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]), null);
 			manhattan_dot = Color.getHSBColor(hsbTmp[0], hsbTmp[1], hsbTmp[2]);
 		} catch (IOException ex) {
 			log.error(null, ex);
 		}
 		//</editor-fold>
 
 		try {
 			this.op = new Operation(this.opId);
 			this.rdOPMetadata = new OperationMetadata(this.opId);
 			this.rdMatrixMetadata = new MatrixMetadata(this.rdOPMetadata.getParentMatrixId());
 
 //			OperationSet rdAssocMarkerSet = new OperationSet(this.rdOPMetadata.getStudyId(), this.opId);
 //			this.labelerMap = rdAssocMarkerSet.getOpSetMap();
 		} catch (IOException ex) {
 			log.error(null, ex);
 		}
 
 		initChart(true);
 
 		setCursor(CursorUtils.defaultCursor);
 	}
 
 	/**
 	 * Creates new form ManhattanPlotZoom
 	 *
 	 * @param _opId
 	 * @param _markerId
 	 * @param _centerPhysPos
 	 * @param _requestedSetSize
 	 */
 	public ManhattanPlotZoom(int _opId,
 			String _chr,
 			String _markerId,
 			long _centerPhysPos,
 			long _requestedSetSize,
 			String _txt_NRows)
 	{
 		opId = _opId;
 		currentMarkerId = _markerId;
 		origMarkerId = _markerId;
 		currentChr = _chr;
 		origChr = _chr;
 		txt_NRows = _txt_NRows;
 		centerPhysPos = _centerPhysPos;
 		requestedSetSize = _requestedSetSize;
 
 		try {
 			op = new Operation(opId);
 			rdOPMetadata = new OperationMetadata(opId);
 			rdMatrixMetadata = new MatrixMetadata(rdOPMetadata.getParentMatrixId());
 
 //			OperationSet rdAssocMarkerSet = new OperationSet(rdOPMetadata.getStudyId(), opId);
 //			labelerMap = rdAssocMarkerSet.getOpSetMap();
 		} catch (IOException ex) {
 			log.error(null, ex);
 		}
 
 		initChart(false);
 
 	}
 
 	public void initChart(boolean usePhysicalPosition) {
 
 		if (usePhysicalPosition) {
 			initXYDataset = getXYDataSetByPhysPos(
 					this,
 					opId,
 					origChr,
 					startPhysPos,
 					requestedPosWindow);
 		} else {
 			initXYDataset = getXYDataSetByMarkerIdAndPhysPos(
 					this,
 					opId,
 					origChr,
 					currentMarkerId,
 					centerPhysPos,
 					requestedSetSize);
 		}
 
 //		slid_Tracker = new JSlider();
 
 		zoomChart = createChart(initXYDataset, currentChr);
 		zoomPanel = new ChartPanel(zoomChart);
 		zoomPanel.setInitialDelay(10);
 		zoomPanel.setDismissDelay(5000);
 		zoomPanel.addChartMouseListener(new ChartMouseListener() {
 			@Override
 			public void chartMouseClicked(ChartMouseEvent event) {
 				int mouseX = event.getTrigger().getX();
 				int mouseY = event.getTrigger().getY();
 				Point2D p = zoomPanel.translateScreenToJava2D(new Point(mouseX, mouseY));
 				XYPlot plot = (XYPlot) zoomChart.getPlot();
 				ChartRenderingInfo info = zoomPanel.getChartRenderingInfo();
 				Rectangle2D dataArea = info.getPlotInfo().getDataArea();
 
 				ValueAxis domainAxis = plot.getDomainAxis();
 				RectangleEdge domainAxisEdge = plot.getDomainAxisEdge();
 				long chartX = (long) domainAxis.java2DToValue(p.getX(), dataArea, domainAxisEdge);
 //				ValueAxis rangeAxis = plot.getRangeAxis();
 //				RectangleEdge rangeAxisEdge = plot.getRangeAxisEdge();
 //				double chartY = rangeAxis.java2DToValue(p.getY(), dataArea,
 //						rangeAxisEdge);
 				try {
 					if (LinksExternalResouces.checkIfRsNecessary(cmb_SearchDB.getSelectedIndex())) { //THE SELECTED EXTERNAL RESOURCE NEEDS RSID INFO
 						String tooltip = zoomPanel.getToolTipText(event.getTrigger());
 						if (tooltip == null || tooltip.isEmpty()) { //CHECK IF THERE IS AN RSID
 							Dialogs.showWarningDialogue(Text.Reports.warnExternalResource);
 						} else {
 							String rsId = tooltip.substring(6, tooltip.indexOf('<', 6));
 							URLInDefaultBrowser.browseGenericURL(LinksExternalResouces.getResourceLink(cmb_SearchDB.getSelectedIndex(),
 									currentChr, // chr
 									rsId, // rsId
 									chartX) // pos
 									);
 						}
 					} else { // THE SELECTED EXTERNAL RESOURCE ONLY NEEDS CHR+POS INFO
 						URLInDefaultBrowser.browseGenericURL(LinksExternalResouces.getResourceLink(cmb_SearchDB.getSelectedIndex(),
 								currentChr, // chr
 								"", // rsId
 								chartX) // pos
 								);
 					}
 //					URLInDefaultBrowser.browseGenericURL(LinkEnsemblUrl.getHomoSapiensLink(currentChr, (int) chartX));
 				} catch (IOException ex) {
 					log.error(Text.Reports.cannotOpenEnsembl, ex);
 				}
 			}
 
 			/**
 			 * Receives chart mouse moved events.
 			 *
 			 * @param event the event.
 			 */
 			@Override
 			public void chartMouseMoved(ChartMouseEvent event) {
 				// ignore
 			}
 		});
 
 		initGUI();
 	}
 
 	private void initGUI() {
 
 		setCursor(CursorUtils.waitCursor);
 
 		pnl_ChartNavigator = new JPanel();
 		pnl_Chart = new JPanel();
 		pnl_SearchDB = new JPanel();
 		cmb_SearchDB = new JComboBox();
 		scrl_Chart = new JScrollPane();
 		pnl_Footer = new JPanel();
 		pnl_FooterGroup1 = new JPanel();
 		btn_Save = new JButton();
 		btn_Reset = new JButton();
 		btn_Back = new JButton();
 		btn_Back2 = new JButton();
 //		pnl_Tracker = new JPanel();
 //		slid_Tracker = new JSlider();
 
 		String titlePlot = ": " + origMarkerId + " - Chr" + currentChr;
 		if (origMarkerId == null) {
 			titlePlot = ": Chr" + currentChr + " - Pos: " + startPhysPos + " to " + (startPhysPos + requestedPosWindow);
 		}
 
 
 		pnl_ChartNavigator.setBorder(BorderFactory.createTitledBorder(null, "Manhattan Plot Navigator" + titlePlot, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("FreeSans", 1, 18))); // NOI18N
 
 		pnl_Chart.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
 
 		scrl_Chart.getViewport().add(zoomPanel);
 		pnl_Chart.add(scrl_Chart, BorderLayout.CENTER);
 
 		// <editor-fold defaultstate="collapsed" desc="LAYOUT1">
 		GroupLayout pnl_ChartLayout = new GroupLayout(pnl_Chart);
 		pnl_Chart.setLayout(pnl_ChartLayout);
 		pnl_ChartLayout.setHorizontalGroup(
 				pnl_ChartLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addComponent(scrl_Chart, GroupLayout.DEFAULT_SIZE, 812, Short.MAX_VALUE));
 		pnl_ChartLayout.setVerticalGroup(
 				pnl_ChartLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addComponent(scrl_Chart, GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE));
 
 		GroupLayout pnl_ChartNavigatorLayout = new GroupLayout(pnl_ChartNavigator);
 		pnl_ChartNavigator.setLayout(pnl_ChartNavigatorLayout);
 		pnl_ChartNavigatorLayout.setHorizontalGroup(
 				pnl_ChartNavigatorLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(GroupLayout.Alignment.TRAILING, pnl_ChartNavigatorLayout.createSequentialGroup()
 				.addContainerGap()
 				.addComponent(pnl_Chart, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 				.addContainerGap()));
 		pnl_ChartNavigatorLayout.setVerticalGroup(
 				pnl_ChartNavigatorLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(pnl_ChartNavigatorLayout.createSequentialGroup()
 				.addContainerGap()
 				.addComponent(pnl_Chart, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 				.addContainerGap()));
 
 		// </editor-fold>
 
 		String lblChr = "Chr ";
 		Long currPos = 0L;
 		lblChr += currentChr;
 		if (centerPhysPos != Integer.MIN_VALUE) {
 			currPos = centerPhysPos;
 		} else {
 			currPos = (long) Math.round(defaultMarkerNb / 2);
 		}
 
 		//<editor-fold defaultstate="collapsed" desc="TRACKER">
 //		pnl_Tracker.setBorder(BorderFactory.createTitledBorder("Marker Nº on "+lblChr));
 //
 //
 //		slid_Tracker.setMaximum(max);
 //		slid_Tracker.setValue(currPos);
 //		slid_Tracker.addMouseListener(new event.MouseAdapter() {
 //			public void mouseReleased(event.MouseEvent evt) {
 //				actionSlide();
 //			}
 //		});
 //
 //		GroupLayout pnl_TrackerLayout = new GroupLayout(pnl_Tracker);
 //		pnl_Tracker.setLayout(pnl_TrackerLayout);
 //		pnl_TrackerLayout.setHorizontalGroup(
 //				pnl_TrackerLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 //				.addGroup(GroupLayout.Alignment.TRAILING, pnl_TrackerLayout.createSequentialGroup()
 //				.addGap(18, 18, 18)
 //				.addComponent(slid_Tracker, GroupLayout.DEFAULT_SIZE, 814, Short.MAX_VALUE)
 //				.addContainerGap())
 //				);
 //		pnl_TrackerLayout.setVerticalGroup(
 //				pnl_TrackerLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 //				.addGroup(pnl_TrackerLayout.createSequentialGroup()
 //				.addContainerGap()
 //				.addComponent(slid_Tracker, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 //				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 //				);
 		//</editor-fold>
 
 		//<editor-fold defaultstate="collapsed" desc="EXTERNAL RESOURCE DBs">
 		pnl_SearchDB = new JPanel();
 		pnl_SearchDB.setBorder(BorderFactory.createTitledBorder(Text.Reports.externalResourceDB));
 		cmb_SearchDB = new JComboBox();
 		cmb_SearchDB.setModel(new DefaultComboBoxModel(LinksExternalResouces.getLinkNames()));
 
 		GroupLayout pnl_SearchDBLayout = new GroupLayout(pnl_SearchDB);
 		pnl_SearchDB.setLayout(pnl_SearchDBLayout);
 		pnl_SearchDBLayout.setHorizontalGroup(
 				pnl_SearchDBLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(pnl_SearchDBLayout.createSequentialGroup()
 				.addContainerGap()
 				.addComponent(cmb_SearchDB, 0, 614, Short.MAX_VALUE)
 				.addContainerGap()));
 		pnl_SearchDBLayout.setVerticalGroup(
 				pnl_SearchDBLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(pnl_SearchDBLayout.createSequentialGroup()
 				.addComponent(cmb_SearchDB, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 				.addContainerGap(14, Short.MAX_VALUE)));
 		//</editor-fold>
 
 		btn_Save.setAction(new SaveAsAction());
 
 		btn_Reset.setAction(new ResetAction(opId));
 
 		btn_Back.setAction(new BackToTableAction());
 
 		btn_Back2.setAction(new BackToManhattanPlotAction());
 
 		// <editor-fold defaultstate="collapsed" desc="FOOTER">
 		GroupLayout pnl_FooterGroup1Layout = new GroupLayout(pnl_FooterGroup1);
 		pnl_FooterGroup1.setLayout(pnl_FooterGroup1Layout);
 		pnl_FooterGroup1Layout.setHorizontalGroup(
 				pnl_FooterGroup1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(GroupLayout.Alignment.TRAILING, pnl_FooterGroup1Layout.createSequentialGroup()
 				.addContainerGap()
 				.addComponent(btn_Reset, GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
 				.addGap(18, 18, 18)
 				.addComponent(btn_Save, GroupLayout.PREFERRED_SIZE, 96, GroupLayout.PREFERRED_SIZE)
 				.addContainerGap()));
 
 		pnl_FooterGroup1Layout.linkSize(SwingConstants.HORIZONTAL, new Component[]{btn_Reset, btn_Save});
 
 		pnl_FooterGroup1Layout.setVerticalGroup(
 				pnl_FooterGroup1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(pnl_FooterGroup1Layout.createSequentialGroup()
 				.addGroup(pnl_FooterGroup1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 				.addComponent(btn_Save)
 				.addComponent(btn_Reset))
 				.addContainerGap(16, Short.MAX_VALUE)));
 
 		pnl_FooterGroup1Layout.linkSize(SwingConstants.VERTICAL, new Component[]{btn_Reset, btn_Save});
 
 		GroupLayout pnl_FooterLayout = new GroupLayout(pnl_Footer);
 		pnl_Footer.setLayout(pnl_FooterLayout);
 		pnl_FooterLayout.setHorizontalGroup(
 				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(pnl_FooterLayout.createSequentialGroup()
 				.addContainerGap()
 				.addComponent(btn_Back)
 				.addGap(18, 18, 18)
 				.addComponent(btn_Back2)
 				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 368, Short.MAX_VALUE)
 				.addComponent(pnl_FooterGroup1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 				.addContainerGap()));
 		pnl_FooterLayout.setVerticalGroup(
 				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(pnl_FooterLayout.createSequentialGroup()
 				.addContainerGap()
 				.addGroup(pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
 				.addGroup(pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 				.addComponent(btn_Back)
 				.addComponent(btn_Back2))
 				.addGap(71, 71, 71))
 				.addGroup(GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
 				.addComponent(pnl_FooterGroup1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 				.addGap(55, 55, 55)))));
 		// </editor-fold>
 
 		// <editor-fold defaultstate="collapsed" desc="LAYOUT">
 //		GroupLayout layout = new GroupLayout(this);
 //		this.setLayout(layout);
 //		layout.setHorizontalGroup(
 //				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
 //				.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
 //				.addContainerGap()
 //				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
 //				.addComponent(pnl_Tracker, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 //				.addComponent(pnl_ChartNavigator, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 //				.addComponent(pnl_Footer, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 //				.addContainerGap())
 //				);
 //		layout.setVerticalGroup(
 //				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
 //				.addGroup(layout.createSequentialGroup()
 //				.addComponent(pnl_ChartNavigator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 //				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 //				.addComponent(pnl_Tracker, GroupLayout.PREFERRED_SIZE, 86, GroupLayout.PREFERRED_SIZE)
 //				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 //				.addComponent(pnl_Footer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 //				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 //				);
 
 		GroupLayout layout = new GroupLayout(this);
 		this.setLayout(layout);
 		layout.setHorizontalGroup(
 				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(layout.createSequentialGroup()
 				.addContainerGap()
 				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addComponent(pnl_SearchDB, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 				.addComponent(pnl_ChartNavigator, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 				.addComponent(pnl_Footer, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 				.addContainerGap()));
 		layout.setVerticalGroup(
 				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(layout.createSequentialGroup()
 				.addComponent(pnl_ChartNavigator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 				.addComponent(pnl_SearchDB, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
 				.addComponent(pnl_Footer, GroupLayout.PREFERRED_SIZE, 55, GroupLayout.PREFERRED_SIZE)
 				.addContainerGap()));
 		// </editor-fold>
 
 		setCursor(CursorUtils.defaultCursor);
 	}
 
 	// <editor-fold defaultstate="collapsed" desc="CHART GENERATOR">
 	private XYDataset getXYDataSetByPhysPos(
 			ManhattanPlotZoom manhattanPlotZoom,
 			int _opId,
 			String _origChr,
 			long _startPhysPos,
 			long _requestedPosWindow)
 	{
 		XYDataset xyd = null;
 		if (op.getOperationType().equals(OPType.ALLELICTEST.toString())) {
 			xyd = GenericReportGenerator.getManhattanZoomByChrAndPos(
 					manhattanPlotZoom,
 					_opId,
 					cNetCDF.Association.VAR_OP_MARKERS_ASAllelicAssociationTPOR,
 					_origChr,
 					null,
 					_startPhysPos,
 					_requestedPosWindow);
 		} else if (op.getOperationType().equals(OPType.GENOTYPICTEST.toString())) {
 			xyd = GenericReportGenerator.getManhattanZoomByChrAndPos(
 					manhattanPlotZoom,
 					_opId,
 					cNetCDF.Association.VAR_OP_MARKERS_ASGenotypicAssociationTP2OR,
 					_origChr,
 					null,
 					_startPhysPos,
 					_requestedPosWindow);
 		} else if (op.getOperationType().equals(OPType.TRENDTEST.toString())) {
 			xyd = GenericReportGenerator.getManhattanZoomByChrAndPos(
 					manhattanPlotZoom,
 					_opId,
 					cNetCDF.Association.VAR_OP_MARKERS_ASTrendTestTP,
 					_origChr,
 					null,
 					_startPhysPos,
 					_requestedPosWindow);
 		}
 
 		return xyd;
 	}
 
 	private XYDataset getXYDataSetByMarkerIdAndPhysPos(
 			ManhattanPlotZoom manhattanPlotZoom,
 			int _opId,
 			String _origChr,
 			String _markerId,
 			long _centerPhysPos,
 			long _requestedPosWindow)
 	{
 		XYDataset xyd = null;
 		if (op.getOperationType().equals(OPType.ALLELICTEST.toString())) {
 			xyd = GenericReportGenerator.getManhattanZoomByChrAndPos(
 					manhattanPlotZoom,
 					_opId,
 					cNetCDF.Association.VAR_OP_MARKERS_ASAllelicAssociationTPOR,
 					_origChr,
 					_markerId,
 					_centerPhysPos,
 					_requestedPosWindow);
 		} else if (op.getOperationType().equals(OPType.GENOTYPICTEST.toString())) {
 			xyd = GenericReportGenerator.getManhattanZoomByChrAndPos(
 					manhattanPlotZoom,
 					_opId,
 					cNetCDF.Association.VAR_OP_MARKERS_ASGenotypicAssociationTP2OR,
 					_origChr,
 					_markerId,
 					_centerPhysPos,
 					_requestedPosWindow);
 		} else if (op.getOperationType().equals(OPType.TRENDTEST.toString())) {
 			xyd = GenericReportGenerator.getManhattanZoomByChrAndPos(
 					manhattanPlotZoom,
 					_opId,
 					cNetCDF.Association.VAR_OP_MARKERS_ASTrendTestTP,
 					_origChr,
 					_markerId,
 					_centerPhysPos,
 					_requestedPosWindow);
 		}
 
 		return xyd;
 	}
 
 	/**
 	 * This getXYDataSetByMarkerIdAndIdx has now been deprecated in favor of
 	 * getXYDataSetByMarkerIdAndPhysPos
 	 *
 	 * @deprecated Use getXYDataSetByMarkerIdAndPhysPos instead
 	 */
 	XYDataset getXYDataSetByMarkerIdAndIdx(
 			ManhattanPlotZoom manhattanPlotZoom,
 			int _opId,
 			String _origChr,
 			String _markerId,
 			int _centerPhysPos,
 			int _requestedSetSize)
 	{
 		XYDataset xyd = null;
 		if (op.getOperationType().equals(OPType.ALLELICTEST.toString())) {
 			xyd = GenericReportGenerator.getManhattanZoomByMarkerIdOrIdx(
 					manhattanPlotZoom,
 					_opId,
 					cNetCDF.Association.VAR_OP_MARKERS_ASAllelicAssociationTPOR,
 					_markerId,
 					_centerPhysPos,
 					_requestedSetSize);
 		} else if (op.getOperationType().equals(OPType.GENOTYPICTEST.toString())) {
 			xyd = GenericReportGenerator.getManhattanZoomByMarkerIdOrIdx(
 					manhattanPlotZoom,
 					_opId,
 					cNetCDF.Association.VAR_OP_MARKERS_ASGenotypicAssociationTP2OR,
 					_markerId,
 					_centerPhysPos,
 					_requestedSetSize);
 		} else if (op.getOperationType().equals(OPType.TRENDTEST.toString())) {
 			xyd = GenericReportGenerator.getManhattanZoomByMarkerIdOrIdx(
 					manhattanPlotZoom,
 					_opId,
 					cNetCDF.Association.VAR_OP_MARKERS_ASTrendTestTP,
 					_markerId,
 					_centerPhysPos,
 					_requestedSetSize);
 		}
 
 		return xyd;
 	}
 
 	private JFreeChart createChart(XYDataset dataset, String chr) {
 		JFreeChart chart = ChartFactory.createScatterPlot(null,
 				"",
 				"P value",
 				dataset,
 				PlotOrientation.VERTICAL,
 				true, false, false);
 
 		XYPlot plot = (XYPlot) chart.getPlot();
 		plot.setNoDataMessage("NO DATA");
 		plot.setDomainZeroBaselineVisible(true);
 		plot.setRangeZeroBaselineVisible(true);
 
 		// CHART BACKGROUD COLOR
 		chart.setBackgroundPaint(Color.getHSBColor(0.1f, 0.1f, 1.0f)); //Hue, saturation, brightness
 		plot.setBackgroundPaint(manhattan_back); //Hue, saturation, brightness 9
 
 		// GRIDLINES
 		plot.setDomainGridlineStroke(new BasicStroke(0.0f));
 		plot.setDomainMinorGridlineStroke(new BasicStroke(0.0f));
 		plot.setDomainGridlinePaint(manhattan_back.darker().darker()); //Hue, saturation, brightness 7
 		plot.setDomainMinorGridlinePaint(manhattan_back); //Hue, saturation, brightness 9
 		plot.setRangeGridlineStroke(new BasicStroke(0.0f));
 		plot.setRangeMinorGridlineStroke(new BasicStroke(0.0f));
 		plot.setRangeGridlinePaint(manhattan_back.darker().darker()); //Hue, saturation, brightness 7
 		plot.setRangeMinorGridlinePaint(manhattan_back.darker());  //Hue, saturation, brightness 8
 
 		plot.setDomainMinorGridlinesVisible(true);
 		plot.setRangeMinorGridlinesVisible(true);
 
 		// DOTS RENDERER
 		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
 		renderer.setSeriesPaint(0, manhattan_dot);
 //		renderer.setSeriesOutlinePaint(0, Color.DARK_GRAY);
 //		renderer.setUseOutlinePaint(true);
 		// Set dot shape of the currently appended Series
 		renderer.setSeriesShape(0, new Rectangle2D.Double(0.0, 0.0, 2, 2));
 
 
 		renderer.setSeriesVisibleInLegend(0, false);
 
 		NumberAxis positionAxis = (NumberAxis) plot.getDomainAxis();
 //		domainAxis.setAutoRangeIncludesZero(false);
 //		domainAxis.setTickMarkInsideLength(2.0f);
 //		domainAxis.setTickMarkOutsideLength(2.0f);
 //		domainAxis.setMinorTickCount(2);
 //		domainAxis.setMinorTickMarksVisible(true);
 		positionAxis.setLabelAngle(1.0);
 		positionAxis.setAutoRangeIncludesZero(false);
 		positionAxis.setAxisLineVisible(true);
 		positionAxis.setTickLabelsVisible(true);
 		positionAxis.setTickMarksVisible(true);
 
 
 		// ADD INVERSE LOG(10) Y AXIS
 		LogAxis logPAxis = new LogAxis("P value");
 		logPAxis.setBase(10);
 		logPAxis.setInverted(true);
 		logPAxis.setNumberFormatOverride(new DecimalFormat("0.#E0#"));
 
 		logPAxis.setTickMarkOutsideLength(2.0f);
 		logPAxis.setMinorTickCount(2);
 		logPAxis.setMinorTickMarksVisible(true);
 		logPAxis.setAxisLineVisible(true);
 		logPAxis.setUpperMargin(0);
 
 		TickUnitSource units = NumberAxis.createIntegerTickUnits();
 		logPAxis.setStandardTickUnits(units);
 		plot.setRangeAxis(0, logPAxis);
 
 		// Add significance Threshold to subplot
 		//threshold = 0.5/rdMatrixMetadata.getMarkerSetSize();  //(0.05/10⁶ SNPs => 5*10-⁷)
 		final Marker thresholdLine = new ValueMarker(threshold);
 		thresholdLine.setPaint(Color.red);
 		DecimalFormat df1 = new DecimalFormat("0.#E0#");
 		// Add legend to threshold
 		thresholdLine.setLabel("P = " + df1.format(threshold));
 		thresholdLine.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
 		thresholdLine.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
 		plot.addRangeMarker(thresholdLine);
 
 		// Marker label if below threshold
 		XYItemRenderer lblRenderer = plot.getRenderer();
 
 		// THRESHOLD AND SELECTED LABEL GENERATOR
 		MySeriesItemLabelGenerator lblGenerator = new MySeriesItemLabelGenerator(threshold, chr);
 		lblRenderer.setSeriesItemLabelGenerator(0, lblGenerator);
 		lblRenderer.setSeriesItemLabelFont(0, new Font("SansSerif", Font.PLAIN, 12));
 		lblRenderer.setSeriesPositiveItemLabelPosition(0, new ItemLabelPosition(ItemLabelAnchor.CENTER,
 				TextAnchor.TOP_LEFT,
 				TextAnchor.BOTTOM_LEFT,
 				Math.PI / 4.0));
 
 		// TOOLTIP GENERATOR
 		MyXYToolTipGenerator tooltipGenerator = new MyXYToolTipGenerator(chr);
 
 		lblRenderer.setBaseToolTipGenerator(tooltipGenerator);
 
 		lblRenderer.setSeriesItemLabelsVisible(0, true);
 
 		return chart;
 	}
 
 	public Map<String, Object> getLabelerMap() {
 		return labelerMap;
 	}
 
 	public void setLabelerMap(Map<String, Object> labelerMap) {
 		this.labelerMap = labelerMap;
 	}
 
 	public long getCenterPhysPos() {
 		return centerPhysPos;
 	}
 
 	public void setCenterPhysPos(long centerPhysPos) {
 		this.centerPhysPos = centerPhysPos;
 	}
 
 	private class MyXYToolTipGenerator extends StandardXYToolTipGenerator
 			implements XYToolTipGenerator
 	{
 		private DecimalFormat dfSci = new DecimalFormat("0.##E0#");
 		private DecimalFormat dfInteger = new DecimalFormat("#");
 		private String chr;
 
 		MyXYToolTipGenerator(String _chr) {
 			this.chr = _chr;
 		}
 
 		@Override
 		public String generateToolTip(XYDataset dataset, int series, int item) {
 			StringBuilder toolTip = new StringBuilder("<html>");
 			double position = dataset.getXValue(series, item);
 			double pValue = dataset.getYValue(series, item);
 
 			String chrPos = chr + "_" + dfInteger.format(position);
 			if (getLabelerMap().containsKey(chrPos)) {
 				toolTip.append(getLabelerMap().get(chrPos));
 				toolTip.append("<br>");
 			}
 
 			toolTip.append("pVal: ").append(dfSci.format(pValue));
 			toolTip.append("<br>pos: ").append(dfInteger.format(position));
 			toolTip.append("</html>");
 			return toolTip.toString();
 		}
 	}
 
 	private class MySeriesItemLabelGenerator extends AbstractXYItemLabelGenerator
 			implements XYItemLabelGenerator
 	{
 		private double threshold;
 		private String chr;
 
 		/**
 		 * Creates a new generator that only displays labels that are greater
 		 * than or equal to the threshold value.
 		 *
 		 * @param threshold the threshold value.
 		 */
 		MySeriesItemLabelGenerator(double threshold, String _chr) {
 			this.threshold = threshold;
 			this.chr = _chr;
 		}
 
 		/**
 		 * Generates a label for the specified item. The label is typically a
 		 * formatted version of the data value, but any text can be used.
 		 *
 		 * @param dataset the dataset (<code>null</code> not permitted).
 		 * @param series the series index (zero-based).
 		 * @param category the category index (zero-based).
 		 *
 		 * @return the label (possibly <code>null</code>).
 		 */
 		@Override
 		public String generateLabel(XYDataset dataset, int series, int item) {
 			String rsLabel = null;
 			Number pValue = dataset.getYValue(series, item);
 			int position = (int) dataset.getXValue(series, item);
 			if (pValue != null) {
 				double pV = pValue.doubleValue();
 				StringBuilder chrPos = new StringBuilder(chr);
 				chrPos.append("_");
 				chrPos.append(position);
 				if (pV < this.threshold) {
 
 					rsLabel = getLabelerMap().get(chrPos.toString()).toString();
 
 					//result = value.toString().substring(0, 4);  // could apply formatting here
 				}
 				if (getLabelerMap().get(chrPos.toString()).toString().equals(origMarkerId)) {
 					rsLabel = getLabelerMap().get(chrPos.toString()).toString();
 					rsLabel = "◄ " + rsLabel + "";
 				}
 			}
 			return rsLabel;
 		}
 	}
 	// </editor-fold>
 
 	//<editor-fold defaultstate="collapsed" desc="HELPERS">
 	private void actionSlide() {
 //		if (slid_Tracker.getValue()>=(rdOPMetadata.getOpSetSize()-defaultMarkerNb)){
 //			indexPosition=sliderSize-defaultMarkerNb;
 //		} else {
 //			indexPosition=slid_Tracker.getValue();
 //		}
 //		GWASpiExplorerPanel.getSingleton().pnl_Content = new ManhattanPlotZoom(opId,
 //				 origChr,
 //				 origMarkerId,
 //				 indexPosition,
 //				 defaultMarkerNb,
 //				 txt_NRows);
 //		GWASpiExplorerPanel.getSingleton().scrl_Content.setViewportView(GWASpiExplorerPanel.getSingleton().pnl_Content);
 	}
 
 	private class ResetAction extends AbstractAction {
 
 		private int opId;
 
 		ResetAction(int opId) {
 
 			this.opId = opId;
 			putValue(NAME, Text.All.reset);
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent evt) {
 			GWASpiExplorerPanel.getSingleton().setPnl_Content(new ManhattanPlotZoom(
 					 parent,
 					 opId,
 					 origChr,
 					 startPhysPos, // startPhysPos
 					 requestedPosWindow, // physPos window
 					 txt_NRows));
 
 			GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
 		}
 	}
 
 	private class SaveAsAction extends AbstractAction {
 
 		SaveAsAction() {
 
 			putValue(NAME, Text.All.save);
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent evt) {
 			try {
 				File newFile = new File(Dialogs.selectDirectoryDialog(JOptionPane.OK_OPTION).getPath() + "/zoom_" + origMarkerId + ".png");
 				ChartUtilities.saveChartAsPNG(newFile, zoomChart, scrl_Chart.getWidth(), scrl_Chart.getHeight());
 			} catch (IOException ex) {
 				log.error(null, ex);
 			} catch (NullPointerException ex) {
 				//Dialogs.showWarningDialogue("A table saving error has occurred");
 				log.error(null, ex);
 			} catch (Exception ex) {
 				log.error(null, ex);
 			}
 		}
 	}
 
 	private class BackToTableAction extends AbstractAction {
 
 		BackToTableAction() {
 
 			putValue(NAME, Text.Reports.backToTable);
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent evt) {
 			try {
 				List<Report> reportsList = ReportsList.getReportsList(rdOPMetadata.getOPId(), rdOPMetadata.getParentMatrixId());
 				GWASpiExplorerPanel.getSingleton().setPnl_Content(new Report_AnalysisPanel(rdOPMetadata.getStudyId(), rdOPMetadata.getParentMatrixId(), rdOPMetadata.getOPId(), txt_NRows));
 				GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
 			} catch (IOException ex) {
 				log.error(null, ex);
 			}
 		}
 	}
 
 	private class BackToManhattanPlotAction extends AbstractAction {
 
 		BackToManhattanPlotAction() {
 
 			putValue(NAME, Text.Reports.backToManhattanPlot);
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent evt) {
 			try {
 				parent.setFired(false);
 				List<Report> reportsList = ReportsList.getReportsList(rdOPMetadata.getOPId(), rdOPMetadata.getParentMatrixId());
 				String reportFile = "";
 				for (int i = 0; i < reportsList.size(); i++) {
 					String reportType = reportsList.get(i).getReportType();
 					if (reportType.equals(OPType.MANHATTANPLOT.toString())) {
 						reportFile = reportsList.get(i).getReportFileName();
 					}
 				}
 				GWASpiExplorerPanel.getSingleton().setPnl_Content(new ManhattanChartDisplay(rdOPMetadata.getStudyId(), reportFile, rdOPMetadata.getOPId()));
 				GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
 			} catch (IOException ex) {
 				log.error(null, ex);
 			}
 		}
 	}
 	//</editor-fold>
 }
