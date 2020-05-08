 package ch.sfdr.fractals.gui;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.GridBagLayout;
 
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JSpinner;
 import javax.swing.JTabbedPane;
 import javax.swing.SpinnerNumberModel;
 
 import ch.sfdr.fractals.gui.component.DisplayArea;
 import ch.sfdr.fractals.gui.component.GBC;
 
 /**
  * Fractals main application window
  * @author S.Freihofer
  */
 public class MainFrame
 	extends JFrame
 {
 	private static final long serialVersionUID = 1L;
 
 	private DisplayArea displayArea;
 	private JLabel lblX;
 	private JLabel lblY;
 	private JLabel lblPercent;
 	private JLabel lblMilliSec;
 	private JTabbedPane paneType;
 	private JPanel pnlFractals;
 	private JPanel pnlSettings;
 	private JComboBox cbFractals;
 	private SpinnerNumberModel snmIterations;
 	private SpinnerNumberModel snmThreads;
 	private JPanel pnlColor;
 	private JPanel pnlPathDraw;
 	private JComboBox cbColor;
 	private JComboBox cbPathColor;
 	private JCheckBox chkAuto;
 	private SpinnerNumberModel snmDelay;
 
 
 	public MainFrame()
 	{
 		super("Fractals");
 		createGUI();
 	}
 
 	private void createGUI()
 	{
 		JPanel pane = new JPanel(new GridBagLayout());
 		setContentPane(pane);
 
 		Font bold = pane.getFont().deriveFont(Font.BOLD);
 
 		JPanel pnlTop = new JPanel(new GridBagLayout());
 		JPanel pnlBottom = new JPanel(new GridBagLayout());
 
 		pane.add(pnlTop,				GBC.get(0, 0, 1, 1, 1.0, 1.0, 'b', "nw"));
 		pane.add(pnlBottom,				GBC.get(0, 1, 1, 1, 'h', "nw"));
 
 		// Panel Top
 		displayArea = new DisplayArea(1);
 		displayArea.setBackground(Color.BLACK);
 		JPanel pnlInfo = new JPanel(new GridBagLayout());
 		pnlInfo.setBorder(BorderFactory.createTitledBorder("Info"));
 		JPanel pnlClick = new JPanel(new GridBagLayout());
 		pnlClick.setBorder(BorderFactory.createTitledBorder("Click Action"));
 		JButton btnDraw = new JButton("Draw");
 		JButton btnReset = new JButton("Reset");
 
 		pnlTop.add(displayArea,			GBC.get(0, 0, 1, 5, 1.0, 1.0, 'b', "nw"));
 		pnlTop.add(pnlInfo,				GBC.get(1, 0, 1, 1));
 		pnlTop.add(pnlClick,			GBC.get(1, 1, 1, 1));
 		pnlTop.add(new JPanel(), 		GBC.get(1, 2, 1, 1, 0.0, 1.0, 'v', "nw"));
 		pnlTop.add(btnDraw,				GBC.get(1, 3, 1, 1, 'n', "sw"));
 		pnlTop.add(btnReset,			GBC.get(1, 4, 1, 1, 'n', "sw"));
 
 		// Panel Info
 		JLabel lblVisible = new JLabel("Visible Area");
 		lblVisible.setFont(bold);
 		lblX = new JLabel("x blub");
 		lblY = new JLabel("y blub");
 		JLabel lblZoom = new JLabel("Zoom");
 		lblZoom.setFont(bold);
 		lblPercent = new JLabel("100%");
 		JLabel lblTime = new JLabel("Time to draw");
 		lblTime.setFont(bold);
 		lblMilliSec = new JLabel("281ms");
 
 		pnlInfo.add(lblVisible,			GBC.get(0, 0, 1, 1));
 		pnlInfo.add(lblX,				GBC.get(0, 1, 1, 1));
 		pnlInfo.add(lblY,				GBC.get(0, 2, 1, 1));
 		pnlInfo.add(lblZoom,			GBC.get(0, 3, 1, 1));
 		pnlInfo.add(lblPercent,			GBC.get(0, 4, 1, 1));
 		pnlInfo.add(lblTime,			GBC.get(0, 5, 1, 1));
 		pnlInfo.add(lblMilliSec,		GBC.get(0, 6, 1, 1));
 
 		// Panel Click Action
 		JRadioButton rbtnZoom = new JRadioButton("Zoom");
 		JRadioButton rbtnPath = new JRadioButton("Draw path");
 
 		ButtonGroup clickGroup = new ButtonGroup();
 		clickGroup.add(rbtnZoom);
 		clickGroup.add(rbtnPath);
 		rbtnZoom.setSelected(true);
 
 		pnlClick.add(rbtnZoom,			GBC.get(0, 0, 1, 1));
 		pnlClick.add(rbtnPath,			GBC.get(0, 1, 1, 1));
 
 		// Panel Bottom
 		paneType = new JTabbedPane();
 		pnlFractals = new JPanel(new GridBagLayout());
 		paneType.add("Fractals", pnlFractals);
 		pnlSettings = new JPanel(new GridBagLayout());
 
 		pnlBottom.add(paneType,			GBC.get(0, 0, 1, 1, 0.5, 0.0, 'h', "nw"));
 		pnlBottom.add(pnlSettings,		GBC.get(1, 0, 1, 1, 0.5, 0.0, 'h', "nw"));
 
 		// Panel Fractals
 		cbFractals = new JComboBox(new String[] {"Mandelbrot"});
 		JLabel lblIterations = new JLabel("Max. # of Iterations");
 		snmIterations = new SpinnerNumberModel(200, 50, 500, 10);
 		JSpinner spinIterations = new JSpinner(snmIterations);
 		JLabel lblThreads = new JLabel("# Concurrent Threads");
 		snmThreads = new SpinnerNumberModel(2, 1, 10, 1);
 		JSpinner spinThreads = new JSpinner(snmThreads);
 
 		pnlFractals.add(cbFractals,		GBC.get(0, 0, 2, 1, 0.5, 0.0, 'h', "nw"));
 		pnlFractals.add(lblIterations,	GBC.get(0, 1, 1, 1));
 		pnlFractals.add(spinIterations,	GBC.get(1, 1, 1, 1, "ne"));
 		pnlFractals.add(lblThreads,		GBC.get(0, 2, 1, 1));
 		pnlFractals.add(spinThreads,	GBC.get(1, 2, 1, 1, "ne"));
 
 		// Panel Settings
 		pnlColor = new JPanel(new GridBagLayout());
 		pnlColor.setBorder(BorderFactory.createTitledBorder("Colorization"));
 		pnlPathDraw = new JPanel(new GridBagLayout());
 		pnlPathDraw.setBorder(BorderFactory.createTitledBorder("Path drawing"));
 
 		pnlSettings.add(pnlColor,		GBC.get(0, 0, 1, 1, 1.0, 0.0, 'h', "nw"));
 		pnlSettings.add(pnlPathDraw,	GBC.get(0, 1, 1, 1, "nw"));
 
 		// Panel Colorization
 		cbColor = new JComboBox(new String[] {"Gray scale"});
 
 		pnlColor.add(cbColor,			GBC.get(0, 0, 1, 1, 1.0, 0.0, 'h', "nw"));
 
 		// Panel Path Drawing
 		cbPathColor = new JComboBox(new String[] {"Red"});
 		chkAuto = new JCheckBox("Auto-cycle");
 		JLabel lblDelay = new JLabel("Step delay (ms)");
 		snmDelay = new SpinnerNumberModel(50, 0, 250, 10);
 		JSpinner spinDelay = new JSpinner(snmDelay);
 
 		pnlPathDraw.add(cbPathColor,	GBC.get(0, 0, 1, 1));
 		pnlPathDraw.add(chkAuto,		GBC.get(1, 0, 1, 1));
 		pnlPathDraw.add(lblDelay,		GBC.get(0, 1, 1, 1));
 		pnlPathDraw.add(spinDelay,		GBC.get(1, 1, 1, 1));
 
 		pack();
 		setMinimumSize(getPreferredSize());
 	}
 }
