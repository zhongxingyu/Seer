 package View;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import java.awt.BasicStroke;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Point;
 import javax.swing.JSplitPane;
 import java.awt.GridBagLayout;
 import javax.swing.JWindow;
 import java.awt.ComponentOrientation;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 
 import javax.swing.JEditorPane;
 import javax.swing.JFileChooser;
 import javax.swing.JToolBar;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JButton;
 import javax.swing.BorderFactory;
 import javax.swing.border.BevelBorder;
 import javax.swing.border.Border;
 import javax.swing.border.SoftBevelBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.ImageIcon;
 import javax.swing.JTextArea;
 import java.awt.GridBagConstraints;
 import javax.swing.JTextField;
 import javax.swing.BoxLayout;
 import javax.swing.JRadioButton;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.plot.CategoryPlot;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.renderer.category.LineAndShapeRenderer;
 import org.jfree.chart.title.TextTitle;
 import org.jfree.data.category.CategoryDataset;
 import org.jfree.data.category.DefaultCategoryDataset;
 import org.jfree.ui.HorizontalAlignment;
 import org.jfree.ui.RectangleEdge;
 import org.jfree.ui.RefineryUtilities;
 
 import Control.CLOOK.CLook;
 import Control.CSCAN.CScan;
 import Control.FCFS.FCFS;
 import Control.LOOK.Look;
 import Control.Reader.Start;
 import Control.SCAN.Scan;
 import Control.SSTF.SSTF;
 
 import java.awt.Rectangle;
 
 public class DiskOptimizationApp {
 	
 	private static JWindow jWindow = null;  //  @jve:decl-index=0:visual-constraint="123,146"
 	private static JPanel jContentPane = null;
 	private static JToolBar jJToolBarBar = null;
 	private static JButton jButton_close = null;  //  @jve:decl-index=0:visual-constraint="740,79"
 	private static JButton jButton_Resize = null;
 	private static JButton jButton_Hide = null;
 	private static JSplitPane jSplitPane_left_right = null;
 	private static JPanel jPanel_ContentHolder = null;
 	private static JPanel jPanel_WorkingHolder = null;
 	private static JTextArea jTextArea = null;
 	private JTextField jTextField = null;
 	private static JSplitPane jSplitPane_Content = null;
 	public static JPanel jPanel_leftContent = null;
 	final static JFileChooser fc = new JFileChooser();
 	static String fileName=null;  //  @jve:decl-index=0:
 	private JButton jButton_Compute = null;
 	private static JPanel jPanel_Options = null;
 	private static JRadioButton jRadioButton_FCFS = null;
 	private static JRadioButton jRadioButton_SSTF = null;
 	private static JRadioButton jRadioButton_SCAN = null;
 	private static JRadioButton jRadioButton_CSCAN = null;
 	private static JRadioButton jRadioButton_LOOK = null;
 	private static JRadioButton jRadioButton_CLOOK = null;
 	private JPanel jPanel_buttons = null;  //  @jve:decl-index=0:visual-constraint="552,90"
 	private static JButton jButton_Upload = null;
 	private static JButton jButton_Run = null;
 	private static int choice=0;
 	
 	static JWindow getJWindow() {
 		if (jWindow == null) {
 			jWindow = new JWindow(new JFrame());
 			jWindow.setFocusable(true);
 			jWindow.setSize(new Dimension(1000, 700));
 			jWindow.setContentPane(getJContentPane());
 		}
 		return jWindow;
 	}
 	private static JPanel getJContentPane() {
 		if (jContentPane == null) {
 			jContentPane = new JPanel();
 			jContentPane.setLayout(new BorderLayout());
 			jContentPane.setForeground(Color.WHITE);
 			jContentPane.setBorder(BorderFactory.createMatteBorder(0,0, 0,0,new ImageIcon("C:/Users/MoistyBurger/Desktop/images.jpg")) );
 			jContentPane.setBackground(Color.white);
 			jContentPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
 			jContentPane.add(getJJToolBarBar(), BorderLayout.NORTH);
 			jContentPane.add(getJSplitPane_left_right(), BorderLayout.CENTER);
 		}
 		return jContentPane;
 	}
 	private static JToolBar getJJToolBarBar() {
 		if (jJToolBarBar == null) {
 			jJToolBarBar = new JToolBar();
 			jJToolBarBar.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
 			jJToolBarBar.setFloatable(false);
 			jJToolBarBar.setOpaque(false);
 			jJToolBarBar.setBackground(Color.GRAY);
 			jJToolBarBar.setForeground(Color.GRAY);
 			jJToolBarBar.add(getJButton_close());
 			jJToolBarBar.add(getJButton_Resize());
 			jJToolBarBar.add(getJButton_Hide());
 			jJToolBarBar.addMouseMotionListener(new MouseMotionListener() {
 			    private int mx, my;
 
 			    public void mouseMoved(MouseEvent e) {
 			        mx = e.getXOnScreen();
 			        my = e.getYOnScreen();
 			    }
 
 			    @Override
 			    public void mouseDragged(MouseEvent e) {
 			    	
 			        Point p = getJWindow().getLocation();
 			        p.x += e.getXOnScreen() - mx;
 			        p.y += e.getYOnScreen() - my;
 			        mx = e.getXOnScreen();
 			        my = e.getYOnScreen();
 			        getJWindow().setLocation(p);
 			    }
 			});
 		}
 		return jJToolBarBar;
 	}
 	private static JButton getJButton_close() {
 		if (jButton_close == null) {
 			jButton_close = new JButton();
 			jButton_close.setSize(25, 25);
 			jButton_close.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					getJWindow().setVisible(false);
 					getJWindow().removeAll();
 				}
 			});
 			jButton_close.setOpaque(false); 
 			jButton_close.setFocusPainted(false);
 			jButton_close.setText(" X ");
 		}
 		return jButton_close;
 	}
 	private static JButton getJButton_Resize() {
 		if (jButton_Resize == null) {
 			jButton_Resize = new JButton();
 			jButton_Resize.setOpaque(false);
 			jButton_Resize.setRequestFocusEnabled(false);
 			jButton_Resize.setFocusPainted(false);
 			jButton_Resize.setText(" |_| ");
 		}
 		return jButton_Resize;
 	}
 	private static JButton getJButton_Hide() {
 		if (jButton_Hide == null) {
 			jButton_Hide = new JButton();
 			jButton_Hide.setOpaque(false);
 			jButton_Hide.setRequestFocusEnabled(false);
 			jButton_Hide.setFocusPainted(false);
 			jButton_Hide.setText(" _ ");
 		}
 		return jButton_Hide;
 	}
 	private static JSplitPane getJSplitPane_left_right() {
 		if (jSplitPane_left_right == null) {
 			jSplitPane_left_right = new JSplitPane();
 			jSplitPane_left_right.setEnabled(false);
 			jSplitPane_left_right.setDividerSize(2);
 			jSplitPane_left_right.setBackground(Color.BLACK);
 			jSplitPane_left_right.setOrientation(JSplitPane.VERTICAL_SPLIT);
 			jSplitPane_left_right.setDividerLocation(500);
 			jSplitPane_left_right.setBottomComponent(getJPanel_WorkingHolder());
 			jSplitPane_left_right.setTopComponent(getJPanel_ContentHolder());
 		}
 		return jSplitPane_left_right;
 	}
 	private static JPanel getJPanel_ContentHolder() {
 		if (jPanel_ContentHolder == null) {
 			jPanel_ContentHolder = new JPanel();
 			jPanel_ContentHolder.setLayout(new BorderLayout());
 			jPanel_ContentHolder.add(getJSplitPane_Content(), BorderLayout.CENTER);
 		}
 		return jPanel_ContentHolder;
 	}
 	private static JPanel getJPanel_WorkingHolder() {
 		if (jPanel_WorkingHolder == null) {
 			jPanel_WorkingHolder = new JPanel();
 			jPanel_WorkingHolder.setLayout(new BorderLayout());
 			jPanel_WorkingHolder.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
 			jPanel_WorkingHolder.add(getJTextArea(), BorderLayout.CENTER);
 			
 		}
 		return jPanel_WorkingHolder;
 	}
 	public static JTextArea getJTextArea() {
 		if (jTextArea == null) {
 			jTextArea = new JTextArea();
 			jTextArea.setText("test");
 			jTextArea.setEditable(true);
 			jTextArea.setEnabled(true);
 			jTextArea.setFocusable(true);
 		}
 		return jTextArea;
 	}
 	private static JSplitPane getJSplitPane_Content() {
 		if (jSplitPane_Content == null) {
 			jSplitPane_Content = new JSplitPane();
 			jSplitPane_Content.setDividerLocation(450);
 			jSplitPane_Content.setEnabled(false);
 			jSplitPane_Content.setBackground(Color.WHITE);
 			jSplitPane_Content.setDividerSize(0);
 			jSplitPane_Content.setBottomComponent(getJPanel_Options());
 			jSplitPane_Content.setOrientation(JSplitPane.VERTICAL_SPLIT);
 		}
 		return jSplitPane_Content;
 	}
 	private static JPanel getJPanel_leftContent() {
 		Start working= new Start(fileName,choice);
 		return jPanel_leftContent;
 	}
 	  
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 
 	private static JPanel getJPanel_Options() {
 		if (jPanel_Options == null) {
 			jPanel_Options = new JPanel();
 			jPanel_Options.setLayout(new BoxLayout(getJPanel_Options(), BoxLayout.X_AXIS));
 			jPanel_Options.setMaximumSize(new Dimension(200,40));
 			jPanel_Options.setMinimumSize(new Dimension(200,40));
 			jPanel_Options.add(getJButton_Upload(), null);
 			jPanel_Options.setBackground(Color.WHITE);
 			jPanel_Options.setPreferredSize(new Dimension(0, 40));
 			jPanel_Options.add(getJRadioButton_FCFS(), null);
 			jPanel_Options.add(getJRadioButton_SSTF(), null);
 			jPanel_Options.add(getJRadioButton_SCAN(), null);
 			jPanel_Options.add(getJRadioButton_CSCAN(), null);
 			jPanel_Options.add(getJRadioButton_LOOK(), null);
 			jPanel_Options.add(getJRadioButton_CLOOK(), null);
 			jPanel_Options.add(getJButton_Run(), null);
 		}
 		return jPanel_Options;
 	}
 	private static JRadioButton getJRadioButton_FCFS() {
 		if (jRadioButton_FCFS == null) {
 			jRadioButton_FCFS = new JRadioButton();
 			jRadioButton_FCFS.setText("FCFS");
 			jRadioButton_FCFS.setSelected(false);
 			jRadioButton_FCFS.setEnabled(true);
 			jRadioButton_FCFS.setBackground(Color.WHITE);
 			jRadioButton_FCFS.setSize(new Dimension(150,40));
 			jRadioButton_FCFS.setMaximumSize(new Dimension(getJWindow().getWidth()-getJSplitPane_Content().getDividerLocation(),40));
 			jRadioButton_FCFS.setMinimumSize(new Dimension(100,40));
 			jRadioButton_FCFS.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					
 					if(jRadioButton_FCFS.isSelected()){
 					choice=1;
 					System.out.println(choice);
 					}
 					if(choice!=0 && !(fileName.equals(null)) ){
 						getJButton_Run().setEnabled(true);
 						getJRadioButton_SSTF().setSelected(false);
 						getJRadioButton_SCAN().setSelected(false);
 						getJRadioButton_CSCAN().setSelected(false);
 						getJRadioButton_LOOK().setSelected(false);
 						getJRadioButton_CLOOK().setSelected(false);
 					}
 					else getJButton_Run().setEnabled(false);
 					
 				}
 			});
 		}
 		return jRadioButton_FCFS;
 	}
 	protected static void methodChoices() {
 		if(getJRadioButton_FCFS().isSelected()){
 			choice=1;
 			getJRadioButton_SSTF().setSelected(false);
 			getJRadioButton_SCAN().setSelected(false);
 			getJRadioButton_CSCAN().setSelected(false);
 			getJRadioButton_LOOK().setSelected(false);
 			getJRadioButton_CLOOK().setSelected(false);
 		}
 		else if(getJRadioButton_SSTF().isSelected()){
 			choice=2;
 			getJRadioButton_FCFS().setSelected(false);
 			getJRadioButton_SCAN().setSelected(false);
 			getJRadioButton_CSCAN().setSelected(false);
 			getJRadioButton_LOOK().setSelected(false);
 			getJRadioButton_CLOOK().setSelected(false);
 		}
 		else if(getJRadioButton_SCAN().isSelected()){
 			choice=3;
 			getJRadioButton_SSTF().setSelected(false);
 			getJRadioButton_FCFS().setSelected(false);
 			getJRadioButton_CSCAN().setSelected(false);
 			getJRadioButton_LOOK().setSelected(false);
 			getJRadioButton_CLOOK().setSelected(false);
 		}
 		else if(getJRadioButton_CSCAN().isSelected()){
 			choice=4;
 			getJRadioButton_SSTF().setSelected(false);
 			getJRadioButton_SCAN().setSelected(false);
 			getJRadioButton_FCFS().setSelected(false);
 			getJRadioButton_LOOK().setSelected(false);
 			getJRadioButton_CLOOK().setSelected(false);
 		}
 		else if(getJRadioButton_LOOK().isSelected()){
 			choice=5;
 			getJRadioButton_SSTF().setSelected(false);
 			getJRadioButton_SCAN().setSelected(false);
 			getJRadioButton_CSCAN().setSelected(false);
 			getJRadioButton_FCFS().setSelected(false);
 			getJRadioButton_CLOOK().setSelected(false);	
 		}
 		else if(getJRadioButton_CLOOK().isSelected()){
 			choice=6;
 			getJRadioButton_SSTF().setSelected(false);
 			getJRadioButton_SCAN().setSelected(false);
 			getJRadioButton_CSCAN().setSelected(false);
 			getJRadioButton_LOOK().setSelected(false);
 			getJRadioButton_FCFS().setSelected(false);
 		}
 		
 		
 	
 	}
 	private static JRadioButton getJRadioButton_SSTF() {
 		if (jRadioButton_SSTF == null) {
 			jRadioButton_SSTF = new JRadioButton();
 			jRadioButton_SSTF.setText("SSTF");
 			jRadioButton_SSTF.setEnabled(true);
 			jRadioButton_SSTF.setBackground(Color.WHITE);
 			jRadioButton_SSTF.setSize(new Dimension(150,40));
 			jRadioButton_SSTF.setMaximumSize(new Dimension(getJWindow().getWidth()-getJSplitPane_Content().getDividerLocation(),40));
 			jRadioButton_SSTF.setMinimumSize(new Dimension(100,40));
 			jRadioButton_SSTF.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					
 					if(jRadioButton_SSTF.isSelected()){
 						choice=2;
 						System.out.println(choice);
 						}
 					if(choice!=0 && !(fileName.equals(null)) ){
 						getJButton_Run().setEnabled(true);
 						getJRadioButton_FCFS().setSelected(false);
 						getJRadioButton_SCAN().setSelected(false);
 						getJRadioButton_CSCAN().setSelected(false);
 						getJRadioButton_LOOK().setSelected(false);
 						getJRadioButton_CLOOK().setSelected(false);
 					}
 					else getJButton_Run().setEnabled(false);
 				}
 			});
 		}
 		return jRadioButton_SSTF;
 	}
 	private static JRadioButton getJRadioButton_SCAN() {
 		if (jRadioButton_SCAN == null) {
 			jRadioButton_SCAN = new JRadioButton();
 			jRadioButton_SCAN.setText("SCAN");
 			jRadioButton_SCAN.setBackground(Color.WHITE);
 			jRadioButton_SCAN.setSize(new Dimension(150,40));
 			jRadioButton_SCAN.setMaximumSize(new Dimension(getJWindow().getWidth()-getJSplitPane_Content().getDividerLocation(),40));
 			jRadioButton_SCAN.setMinimumSize(new Dimension(100,40));
 			jRadioButton_SCAN.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 			
 					if(jRadioButton_SCAN.isSelected()){
 						choice=3;
 						System.out.println(choice);
 						}
 					if(choice!=0 && !(fileName.equals(null)) ){
 						getJButton_Run().setEnabled(true);
 						getJRadioButton_SSTF().setSelected(false);
 						getJRadioButton_FCFS().setSelected(false);
 						getJRadioButton_CSCAN().setSelected(false);
 						getJRadioButton_LOOK().setSelected(false);
 						getJRadioButton_CLOOK().setSelected(false);
 					}
 					else getJButton_Run().setEnabled(false);
 				}
 			});
 			
 		}
 		return jRadioButton_SCAN;
 	}
 	private static JRadioButton getJRadioButton_CSCAN() {
 		if (jRadioButton_CSCAN == null) {
 			jRadioButton_CSCAN = new JRadioButton();
 			jRadioButton_CSCAN.setText("CSCAN");
 			jRadioButton_CSCAN.setBackground(Color.WHITE);
 			jRadioButton_CSCAN.setSize(new Dimension(150,40));
 			jRadioButton_CSCAN.setMaximumSize(new Dimension(getJWindow().getWidth()-getJSplitPane_Content().getDividerLocation(),40));
 			jRadioButton_CSCAN.setMinimumSize(new Dimension(100,40));
 			jRadioButton_CSCAN.addActionListener(new java.awt.event.ActionListener()  {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					if(jRadioButton_CSCAN.isSelected()){
 						choice=4;
 						System.out.println(choice);
 						}
 					if(choice!=0 && !(fileName.equals(null)) ){
 						getJButton_Run().setEnabled(true);
 						getJRadioButton_SSTF().setSelected(false);
 						getJRadioButton_SCAN().setSelected(false);
 						getJRadioButton_FCFS().setSelected(false);
 						getJRadioButton_LOOK().setSelected(false);
 						getJRadioButton_CLOOK().setSelected(false);
 					}
 					else getJButton_Run().setEnabled(false);
 				}
 			});
 		}
 		return jRadioButton_CSCAN;
 	}
 	private static JRadioButton getJRadioButton_LOOK() {
 		if (jRadioButton_LOOK == null) {
 			jRadioButton_LOOK = new JRadioButton();
 			jRadioButton_LOOK.setText("LOOK");
 			jRadioButton_LOOK.setBackground(Color.WHITE);
 			jRadioButton_LOOK.setSize(new Dimension(150,40));
 			jRadioButton_LOOK.setMaximumSize(new Dimension(getJWindow().getWidth()-getJSplitPane_Content().getDividerLocation(),40));
 			jRadioButton_LOOK.setMinimumSize(new Dimension(100,40));
 			
 			jRadioButton_LOOK.addActionListener(new java.awt.event.ActionListener()  {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 			
 				
 					if(jRadioButton_LOOK.isSelected()){
 						choice=5;
 						System.out.println(choice);
 						}
 					if(choice!=0 && !(fileName.equals(null)) ){
 						getJButton_Run().setEnabled(true);
 						getJRadioButton_SSTF().setSelected(false);
 						getJRadioButton_SCAN().setSelected(false);
 						getJRadioButton_CSCAN().setSelected(false);
 						getJRadioButton_FCFS().setSelected(false);
 						getJRadioButton_CLOOK().setSelected(false);	
 					}
 					else getJButton_Run().setEnabled(false);
 				}
 			});
 		}
 		return jRadioButton_LOOK;
 	}
 	private static JRadioButton getJRadioButton_CLOOK() {
 		if (jRadioButton_CLOOK == null) {
 			jRadioButton_CLOOK = new JRadioButton();
 			jRadioButton_CLOOK.setText("CLOOK");
 			jRadioButton_CLOOK.setBackground(Color.WHITE);
 			jRadioButton_CLOOK.setSize(new Dimension(150,40));
 			jRadioButton_CLOOK.setMaximumSize(new Dimension(getJWindow().getWidth()-getJSplitPane_Content().getDividerLocation(),40));
 			jRadioButton_CLOOK.setMinimumSize(new Dimension(100,40));
			jRadioButton_CLOOK.addChangeListener(new javax.swing.event.ChangeListener() {
				public void stateChanged(javax.swing.event.ChangeEvent e) {
 					
 					if(jRadioButton_CLOOK.isSelected()){
 						choice=6;
 						System.out.println(choice);
 						}
 				}
 			});
 		}
 		return jRadioButton_CLOOK;
 	}
 	/**
 	 * This method initializes jPanel_buttons	
 	 * 	
 	 * @return javax.swing.JPanel	
 	 */
 	private JPanel getJPanel_buttons() {
 		if (jPanel_buttons == null) {
 			jPanel_buttons = new JPanel();
 			jPanel_buttons.setLayout(new BorderLayout());
 			jPanel_buttons.setBackground(Color.WHITE);
 			jPanel_buttons.setSize(new Dimension(186, 47));
 		}
 		return jPanel_buttons;
 	}
 	/**
 	 * This method initializes jButton_Upload	
 	 * 	
 	 * @return javax.swing.JButton	
 	 */
 	private static JButton getJButton_Upload() {
 		if (jButton_Upload == null) {
 			jButton_Upload = new JButton();
 			jButton_Upload.setText("Upload");
 			jButton_Upload.setOpaque(true);
 			jButton_Upload.setFocusable(false);
 			jButton_Upload.setFocusPainted(false);
 			jButton_Upload.setForeground(Color.GRAY);
 			jButton_Upload.setBorderPainted(false);
 			jButton_Upload.setPreferredSize(new Dimension(200,40));
 			jButton_Upload.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					fc.showOpenDialog(fc);
 					try{
 						fileName=fc.getSelectedFile().toString();
 						getJTextArea().setText("File : "+fileName+"\nSelect one run to start...");
 						
 					}
 					catch(Exception error){
 						System.out.println("please upload a file");
 					}
 					finally{
 						if(choice!=0 || !(fileName.equals(null)) ){
 							getJButton_Run().setEnabled(true);
 						}
 						else getJButton_Run().setEnabled(false);
 					}
 				}
 			});
 		}
 		return jButton_Upload;
 	}
 	private static JButton getJButton_Run() {
 		if (jButton_Run == null) {
 			jButton_Run = new JButton();
 			jButton_Run.setText("Run");
 			jButton_Run.setOpaque(true);
 			jButton_Run.setEnabled(false);
 			jButton_Run.setFocusable(false);
 			jButton_Run.setFocusPainted(false);
 			jButton_Run.setBorderPainted(false);
 			jButton_Run.setForeground(Color.GRAY);
 			jButton_Run.setPreferredSize(new Dimension(200,40));
 			jButton_Run.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					getJSplitPane_Content().remove(1);
 					getJSplitPane_Content().setDividerLocation(450);
 					getJSplitPane_Content().setTopComponent(getJPanel_leftContent());
 				}
 			});
 		}
 		return jButton_Run;
 	}
 
 }
