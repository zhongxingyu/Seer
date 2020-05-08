 package com.stacktraceinc.hypercut2d;
 
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.FileDialog;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.awt.image.RenderedImage;
 import java.awt.print.PrinterJob;
 
 import javax.imageio.ImageIO;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JColorChooser;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingConstants;
 import javax.swing.UIManager;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.filechooser.FileFilter;
 
 import net.miginfocom.swing.MigLayout;
 
 import org.jdesktop.beansbinding.AutoBinding;
 import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
 import org.jdesktop.beansbinding.BeanProperty;
 import org.jdesktop.beansbinding.Bindings;
 import org.jdesktop.beansbinding.ELProperty;
 import org.jdesktop.swingbinding.JTableBinding;
 import org.jdesktop.swingbinding.SwingBindings;
 import javax.swing.SpinnerNumberModel;
 import java.util.List;
 import org.jdesktop.beansbinding.ObjectProperty;
 import org.jdesktop.swingbinding.JComboBoxBinding;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeEvent;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.awt.Toolkit;
 import javax.swing.border.EtchedBorder;
 import java.awt.FlowLayout;
 
 public class HyperCut2D {
 	public static final String UNSELECTED_PART = "UNSELECTED_PART";
 	public static final String SELECTED_PART = "SELECTED_PART";
 	private JFrame frmHypercut;
 	private JTextField txtPavadinimas;
 	private JSpinner txtAukstis;
 	private JSpinner txtPlotis;
 	private JSpinner txtSonoIlgis;
 	private JComboBox cbxForma;
 	private JPanel detaliuNustatymai;
 	
 	private FancyCanvas fancyCanvas = new FancyCanvas(new Dimension(0, 0));
 	private PartListHandler partListHandler = new PartListHandler();
 	private CanvasSizeHandler canvasSizeHandler = new CanvasSizeHandler();
 	private JTable tblDetales;
 	private JLabel lblPavadinimas;
 	private JPanel pnlDetale;
 	private JComboBox cbxBrezinioDydis;
 	private JLabel lblBrezinioDydis;
 	private JScrollPane srlDetales;
 	private JPanel pnlSpalva;
 	private Packer packer = new Packer(0, 0);
 
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		try {
 			UIManager.setLookAndFeel(
 	            UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {
 			e.printStackTrace();
 			return;
 		}
 		
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					HyperCut2D window = new HyperCut2D();
 					window.frmHypercut.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public HyperCut2D() {
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		
 		frmHypercut = new JFrame();
 		frmHypercut.setIconImage(Toolkit.getDefaultToolkit().getImage(HyperCut2D.class.getResource("/resources/layout.png")));
 		frmHypercut.setTitle("HyperCut 2D");
 		frmHypercut.setBounds(100, 100, 906, 764);
 		frmHypercut.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frmHypercut.getContentPane().setLayout(new MigLayout("", "[min:max][min:n,left]", "[max]"));
 		
 		JPanel pnlBrezinys = new JPanel();
 		pnlBrezinys.setBorder(new TitledBorder(null, "Br\u0117\u017Einys", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		frmHypercut.getContentPane().add(pnlBrezinys, "grow,shrink");
 		pnlBrezinys.setLayout(new MigLayout("", "[grow]", "[][grow][]"));
 		
 		JPanel pnlBrezinioDydis = new JPanel();
 		pnlBrezinioDydis.setBorder(null);
 		pnlBrezinys.add(pnlBrezinioDydis, "cell 0 0,grow");
 		pnlBrezinioDydis.setLayout(new MigLayout("", "[][grow][][]", "[][]"));
 		
 		lblBrezinioDydis = new JLabel("Br\u0117\u017Einio dydis:");
 		pnlBrezinioDydis.add(lblBrezinioDydis, "cell 0 0,alignx trailing");
 		
 		cbxBrezinioDydis = new JComboBox();
		cbxBrezinioDydis.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent arg0) {
 				if (cbxBrezinioDydis.getSelectedIndex() == -1){
 					return;
 				}
 				
 				Dimension d = canvasSizeHandler.getSizes().get(cbxBrezinioDydis.getSelectedIndex());
 				fancyCanvas.panelResize(d);
 				packer.resize((int)d.getWidth(), (int)d.getHeight());
 			}
 		});
 		pnlBrezinioDydis.add(cbxBrezinioDydis, "cell 1 0,growx");
 		
 		JButton btnKeistiDydius = new JButton("Keisti dyd\u017Eius...");
 		btnKeistiDydius.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				JDialog cvs = new CanvasSizes(frmHypercut, canvasSizeHandler);
 			}
 		});
 		btnKeistiDydius.setIcon(new ImageIcon(HyperCut2D.class.getResource("/resources/page_gear.png")));
 		pnlBrezinioDydis.add(btnKeistiDydius, "cell 2 0,alignx center,growy");
 		
 		JScrollPane srpBrezinys = new JScrollPane();
 		pnlBrezinys.add(srpBrezinys, "cell 0 1,grow");
 		
 		// Use custom canvas class here
 		JComponent cvsBrezinys = fancyCanvas;
 		srpBrezinys.setViewportView(cvsBrezinys);
 		cvsBrezinys.setBackground(Color.WHITE);
 		
 		JPanel pnlBrezinioVeiksmai = new JPanel();
 		pnlBrezinys.add(pnlBrezinioVeiksmai, "cell 0 2,grow");
 		pnlBrezinioVeiksmai.setLayout(new MigLayout("", "[left][right]", "[][]"));
 		
 		JButton btnOptimizuotiIdstym = new JButton("Optimizuoti i\u0161d\u0117stym\u0105");
 		
 		pnlBrezinioVeiksmai.add(btnOptimizuotiIdstym, "cell 0 0,alignx left,growy");
 		btnOptimizuotiIdstym.setIcon(new ImageIcon(HyperCut2D.class.getResource("/resources/wrench_orange.png")));
 		btnOptimizuotiIdstym.addActionListener(new ActionListener() {
 			
 			public void actionPerformed(ActionEvent arg0) {
 				List<Part> optimisedList = packer.pack(partListHandler.getParts());
 				for(Part part : optimisedList){
 					if (part.getCoord().compare(new Coordinate(-1, -1)))
 					{
 						JOptionPane.showMessageDialog(frmHypercut, "Ne visos detal\u0117s tilpo \u012f panel\u0119.", "Klaida", JOptionPane.ERROR_MESSAGE);
 						break;
 					}
 				}
 				fancyCanvas.setPartList(optimisedList);
 				fancyCanvas.refresh();
 			}
 		});
 		
 		JButton btnSpausdinti = new JButton("Eksportuoti spausdinimui...");
 		btnSpausdinti.setIcon(new ImageIcon(HyperCut2D.class.getResource("/resources/script_go.png")));
 		pnlBrezinioVeiksmai.add(btnSpausdinti, "cell 1 0,alignx left,growy");
 		btnSpausdinti.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				JFileChooser fileChooser = new JFileChooser();
 				FileFilter jpegFileFilter = new FileFilter() {
 					@Override
 					public boolean accept(File f) {
 						String path = f.getName();
 						String extension = path.substring(path.lastIndexOf('.') + 1);
 						if (extension.equals("jpg") || extension.equals("jpeg")) {
 							return true;
 						}
 						return false;
 					}
 
 					@Override
 					public String getDescription() {
 						return "JPEG Image (.jpg, .jpeg)";
 					}
 				};
 				FileFilter pngFileFilter = new FileFilter() {
 					@Override
 					public boolean accept(File f) {
 						String path = f.getName();
 						String extension = path.substring(path.lastIndexOf('.') + 1);
 						if (extension.equals("png")) {
 							return true;
 						}
 						return false;
 					}
 
 					@Override
 					public String getDescription() {
 						return "PNG Image (.png)";
 					}
 				};
 				fileChooser.addChoosableFileFilter(jpegFileFilter);
 				fileChooser.addChoosableFileFilter(pngFileFilter);
 
 				int result = fileChooser.showSaveDialog(frmHypercut);
 				if (result == JFileChooser.APPROVE_OPTION) {
 					File file = fileChooser.getSelectedFile();
 					String path = file.getAbsolutePath();
 					String extension = path.substring(path.lastIndexOf('.') + 1);
 					String format;
 					if (fileChooser.getFileFilter() == jpegFileFilter){
 						// jpeg
 						if (!(extension.equals("jpg") || extension.equals("jpeg"))) {
 							path = path + ".jpg";
 						}
 						format = "JPEG";
 					} else {
 						// It's a png image!
 						if (!extension.equals("png")) {
 							path = path + ".png";
 						}
 						format = "PNG";
 					}
 					System.out.println(path + " " + format);
 					try {
 						File saving_file = new File(path);
 						RenderedImage image = fancyCanvas.getImage();
 						System.out.println(image.getHeight());
 						ImageIO.write(image, format, saving_file);
 					} catch (IOException ioe) {
 						ioe.printStackTrace();
 					}
 				}
 			}
 		});
 		
 		btnOptimizuotiIdstym.setIcon(new ImageIcon(HyperCut2D.class.getResource("/resources/wrench_orange.png")));
 		pnlBrezinioVeiksmai.add(btnOptimizuotiIdstym, "cell 0 0,alignx left,growy");
 
 		
 		JPanel pnlDetales = new JPanel();
 		pnlDetales.setBorder(new TitledBorder(null, "D\u0117tales", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		frmHypercut.getContentPane().add(pnlDetales, "dock north, dock east");
 		pnlDetales.setLayout(new MigLayout("", "[127px,grow]", "[108px][grow]"));
 		
 		JPanel pnlSarasas = new JPanel();
 		pnlSarasas.setBorder(new TitledBorder(null, "S\u0105ra\u0161as", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		pnlDetales.add(pnlSarasas, "cell 0 0,alignx left,aligny top");
 		pnlSarasas.setLayout(new MigLayout("", "[grow]", "[grow][200px:n:200px,grow][grow]"));
 		
 		JPanel pnlSarasoVeiksmai = new JPanel();
 		pnlSarasas.add(pnlSarasoVeiksmai, "cell 0 0,alignx center,growy");
 		pnlSarasoVeiksmai.setLayout(new MigLayout("", "[][]", "[]"));
 		
 		JButton btnAtdaryti = new JButton("Atidaryti...");
 		pnlSarasoVeiksmai.add(btnAtdaryti, "cell 0 0,grow");
 		btnAtdaryti.setIcon(new ImageIcon(HyperCut2D.class.getResource("/resources/layer_open.png")));
 		btnAtdaryti.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				FileDialog fileDialog = new FileDialog(frmHypercut);
 				fileDialog.setMode(FileDialog.LOAD);
 				fileDialog.show();
 				String fileName = fileDialog.getFile();
 				String directoryName = fileDialog.getDirectory();
 				if (fileName != null) {
 					try {
 						BufferedReader in = new BufferedReader(new FileReader(directoryName + fileName));
 					    String name;
 					    String form;
 					    String color;					    
 					    int x;
 					    int y;
 					    int width;
 					    int height;
 					    PartListHandler newHandler = new PartListHandler();
 					    while (in.ready()) {
 					        name = in.readLine();
 					        form = in.readLine();
 					        color = in.readLine();					     
 					        x = Integer.parseInt(in.readLine());
 					        y = Integer.parseInt(in.readLine());
 					        width = Integer.parseInt(in.readLine());
 					        height = Integer.parseInt(in.readLine());
 					        Part newRectangle = new Part(name, form, width, height, new Color(Integer.parseInt(color, 16)));
 					        newRectangle.setCoordinate(new Coordinate(x, y));
 					        newHandler.addPart(newRectangle);
 					    }
 					    while (!partListHandler.getParts().isEmpty()){
 					    	partListHandler.erasePart(0);
 					    }
 					    for (Part part: newHandler.getParts()){
 					    	partListHandler.addPart(part);
 					    }
 					} catch (Exception e) {
 						JOptionPane.showMessageDialog(frmHypercut, "Nurodytas blogas form\u0173 failas.", "Klaida", JOptionPane.ERROR_MESSAGE);
 					}
 				}
 			}
 		});
 		
 		
 		JButton btnIssaugoti = new JButton("I\u0161saugoti...");
 		pnlSarasoVeiksmai.add(btnIssaugoti, "cell 1 0,grow");
 		btnIssaugoti.setIcon(new ImageIcon(HyperCut2D.class.getResource("/resources/layer_save.png")));
 		btnIssaugoti.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				FileDialog fileDialog = new FileDialog(frmHypercut);
 				fileDialog.setMode(FileDialog.SAVE);
 				fileDialog.show();
 				String fileName = fileDialog.getFile();
 				String directoryName = fileDialog.getDirectory();
 				if (fileName != null) {
 					BufferedWriter out;
 					try {
 						out = new BufferedWriter(new FileWriter(directoryName + fileName));
 						for (Part part: partListHandler.getParts()) {
 						    out.write(part.getName() + "\n");
 						    out.write(part.getForm() + "\n");
 						    out.write(Integer.toString(part.getColor().getRed(), 16));
 						    out.write(Integer.toString(part.getColor().getGreen(), 16));
 						    out.write(Integer.toString(part.getColor().getBlue(), 16) + "\n");						 
 						    out.write(Integer.toString(part.getX()) + "\n");
 						    out.write(Integer.toString(part.getY()) + "\n");
 						    out.write(Integer.toString(part.getFirstValue()) + "\n");
 						    out.write(Integer.toString(part.getSecondValue()) + "\n");
 						}
 						out.close();
 					} catch (IOException e) {
 						
 				    }
 				}
 			}
 		});
 		
 		srlDetales = new JScrollPane();
 		pnlSarasas.add(srlDetales, "cell 0 1,grow");
 		
 		tblDetales = new JTable();
 		tblDetales.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 			@Override
 		    public void valueChanged(ListSelectionEvent e) {
 		    	CardLayout cardLayout = (CardLayout)(pnlDetale.getLayout());
 				if (tblDetales.getSelectedRow() == -1){
 					cardLayout.show(pnlDetale, UNSELECTED_PART);
 					return;
 				}
 				
 				
 				cardLayout.show(pnlDetale, SELECTED_PART);
 		    }
 		});
 		srlDetales.setViewportView(tblDetales);
 		tblDetales.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		
 		JPanel pnlDetaliuVeiksmai = new JPanel();
 		pnlSarasas.add(pnlDetaliuVeiksmai, "cell 0 2,alignx center,growy");
 		pnlDetaliuVeiksmai.setLayout(new MigLayout("", "[][][]", "[][]"));
 		
 		JButton btnNaujaDetale = new JButton("Nauja detal\u0117");
 		pnlDetaliuVeiksmai.add(btnNaujaDetale, "cell 0 0 3 1,grow");
 		btnNaujaDetale.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				partListHandler.createNewPart();
 				ListSelectionModel selectionModel = tblDetales.getSelectionModel();
 				selectionModel.setSelectionInterval(0, tblDetales.getRowCount()-1);
 				tblDetales.scrollRectToVisible(tblDetales.getCellRect(tblDetales.getRowCount()-1, 0, true));
 			}
 		});
 		btnNaujaDetale.setIcon(new ImageIcon(HyperCut2D.class.getResource("/resources/layer_add.png")));
 		
 		JButton btnKlonuotiPasirinktaDetal = new JButton("Kopijuoti detal\u0119");
 		btnKlonuotiPasirinktaDetal.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if (tblDetales.getSelectedRow() == -1){
 					return;
 				}
 				partListHandler.clonePart(tblDetales.getSelectedRow());
 				ListSelectionModel selectionModel = tblDetales.getSelectionModel();
 				selectionModel.setSelectionInterval(0, tblDetales.getRowCount()-1);
 				tblDetales.scrollRectToVisible(tblDetales.getCellRect(tblDetales.getRowCount()-1, 0, true));
 				
 			}
 		});
 		btnKlonuotiPasirinktaDetal.setIcon(new ImageIcon(HyperCut2D.class.getResource("/resources/layer_create.png")));
 		btnKlonuotiPasirinktaDetal.setSelectedIcon(null);
 		pnlDetaliuVeiksmai.add(btnKlonuotiPasirinktaDetal, "cell 0 1,growy");
 		
 		JButton btnItrintiDetal = new JButton("I\u0161trinti detal\u0119");
 		btnItrintiDetal.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if (tblDetales.getSelectedRow() == -1){
 					return;
 				}
 				int row = tblDetales.getSelectedRow();
 				partListHandler.erasePart(row);
 
 				if (tblDetales.getRowCount() == 0) {
 					return;
 				}
 				
 				if (tblDetales.getRowCount() <= row) {
 					row = tblDetales.getRowCount()-1;
 				}
 				
 				ListSelectionModel selectionModel = tblDetales.getSelectionModel();
 				selectionModel.setSelectionInterval(0, row);
 
 				tblDetales.scrollRectToVisible(tblDetales.getCellRect(tblDetales.getRowCount()-1, 0, true));
 			}
 		});
 		btnItrintiDetal.setIcon(new ImageIcon(HyperCut2D.class.getResource("/resources/layer_delete.png")));
 		pnlDetaliuVeiksmai.add(btnItrintiDetal, "cell 2 1,growy");
 		
 		pnlDetale = new JPanel();
 		pnlDetale.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Detal\u0117", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
 		pnlDetales.add(pnlDetale, "cell 0 1,grow");
 		pnlDetale.setLayout(new CardLayout(0, 0));
 		
 		JPanel pnlNeisrinktaDetale = new JPanel();
 		pnlDetale.add(pnlNeisrinktaDetale, UNSELECTED_PART);
 		
 		JLabel lblPasirinkiteDetalSarae = new JLabel("<html>Pasirinkite detal\u0117 sara\u0161e,<br>\r\narba sukurkite nauja.");
 		lblPasirinkiteDetalSarae.setHorizontalAlignment(SwingConstants.CENTER);
 		pnlNeisrinktaDetale.add(lblPasirinkiteDetalSarae);
 		
 		JPanel pnlIsrinktaDetale = new JPanel();
 		pnlDetale.add(pnlIsrinktaDetale, SELECTED_PART);
 		pnlIsrinktaDetale.setLayout(new MigLayout("", "[grow][][]", "[][grow][][]"));
 		
 		JPanel pnlPastovusNustatymai = new JPanel();
 		pnlIsrinktaDetale.add(pnlPastovusNustatymai, "cell 0 0,growx,aligny top");
 		pnlPastovusNustatymai.setLayout(new MigLayout("", "[][grow]", "[][][grow]"));
 		
 		lblPavadinimas = new JLabel("Pavadinimas:");
 		pnlPastovusNustatymai.add(lblPavadinimas, "cell 0 0,alignx trailing");
 		
 		txtPavadinimas = new JTextField();
 		pnlPastovusNustatymai.add(txtPavadinimas, "cell 1 0,growx");
 		txtPavadinimas.setColumns(10);
 		
 		JLabel lblForma = new JLabel("Forma:");
 		pnlPastovusNustatymai.add(lblForma, "flowy,cell 0 1,alignx trailing");
 		
 		detaliuNustatymai = new JPanel();
 		cbxForma = new JComboBox();
 		cbxForma.setModel(new DefaultComboBoxModel(new String[] { PartListHandler.RECTANGLE, PartListHandler.SQUARE }));
 		pnlPastovusNustatymai.add(cbxForma, "cell 1 1,growx");
 		
 		JLabel lblSpalva = new JLabel("Spalva:");
 		lblSpalva.setHorizontalAlignment(SwingConstants.LEFT);
 		pnlPastovusNustatymai.add(lblSpalva, "cell 0 2,alignx right,aligny center");
 		
 		pnlSpalva = new JPanel();
 		FlowLayout flowLayout = (FlowLayout) pnlSpalva.getLayout();
 		pnlSpalva.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseReleased(MouseEvent arg0) {
				Color color = JColorChooser.showDialog(frmHypercut, "Pasirinkite detals spalv...", pnlSpalva.getBackground());
 				if (color != null) {
 					pnlSpalva.setBackground(color);
 				}
 			}
 		});
 		pnlSpalva.setBackground(Color.WHITE);
 		pnlSpalva.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
 		pnlPastovusNustatymai.add(pnlSpalva, "cell 1 2,grow");
 		
 		cbxForma.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent arg0) {
 				CardLayout cardLayout = (CardLayout)(detaliuNustatymai.getLayout());
 				cardLayout.show(detaliuNustatymai, (String)arg0.getItem());
 			}
 		});
 		
 		detaliuNustatymai.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Detal\u0117s matmenys", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
 		pnlIsrinktaDetale.add(detaliuNustatymai, "cell 0 1,grow");
 		detaliuNustatymai.setLayout(new CardLayout(0, 0));
 		
 		JPanel pnlStaciakampioNustatymai = new JPanel();
 		detaliuNustatymai.add(pnlStaciakampioNustatymai, PartListHandler.RECTANGLE);
 		pnlStaciakampioNustatymai.setLayout(new MigLayout("", "[][grow][]", "[][][]"));
 		
 		JLabel lblPlotis = new JLabel("Plotis:");
 		pnlStaciakampioNustatymai.add(lblPlotis, "flowy,cell 0 0,alignx trailing");
 		
 		txtPlotis = new JSpinner();
 		txtPlotis.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
 		pnlStaciakampioNustatymai.add(txtPlotis, "cell 1 0,growx");
 		
 		JLabel lblAukstis = new JLabel("Auk\u0161tis:");
 		pnlStaciakampioNustatymai.add(lblAukstis, "flowy,cell 0 1,alignx trailing");
 		
 		JLabel lblMm = new JLabel("mm");
 		pnlStaciakampioNustatymai.add(lblMm, "cell 2 0");
 		
 		txtAukstis = new JSpinner();
 		txtAukstis.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
 		pnlStaciakampioNustatymai.add(txtAukstis, "cell 1 1,growx");
 		
 		JLabel lblMm_1 = new JLabel("mm");
 		pnlStaciakampioNustatymai.add(lblMm_1, "cell 2 1");
 		
 		JPanel pnlKvadratoNustatymai = new JPanel();
 		detaliuNustatymai.add(pnlKvadratoNustatymai, PartListHandler.SQUARE);
 		pnlKvadratoNustatymai.setLayout(new MigLayout("", "[][grow][]", "[][]"));
 		
 		JLabel lblSonoIlgis = new JLabel("\u0160ono ilgis:");
 		pnlKvadratoNustatymai.add(lblSonoIlgis, "cell 0 0,alignx trailing");
 		
 		txtSonoIlgis = new JSpinner();
 		txtSonoIlgis.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
 		pnlKvadratoNustatymai.add(txtSonoIlgis, "flowx,cell 1 0,growx");
 		
 		JLabel lblMm_2 = new JLabel("mm");
 		pnlKvadratoNustatymai.add(lblMm_2, "cell 1 0");
 		initDataBindings();
 	}
 	protected void initDataBindings() {
 		BeanProperty<PartListHandler, List<Part>> partListHandlerBeanProperty = BeanProperty.create("parts");
 		JTableBinding<Part, PartListHandler, JTable> jTableBinding = SwingBindings.createJTableBinding(UpdateStrategy.READ, partListHandler, partListHandlerBeanProperty, tblDetales);
 		//
 		BeanProperty<Part, String> partBeanProperty = BeanProperty.create("name");
 		jTableBinding.addColumnBinding(partBeanProperty).setColumnName("Pavadinimas").setEditable(false);
 		//
 		BeanProperty<Part, String> partBeanProperty_1 = BeanProperty.create("form");
 		jTableBinding.addColumnBinding(partBeanProperty_1).setColumnName("Forma").setEditable(false);
 		//
 		jTableBinding.bind();
 		//
 		BeanProperty<JTable, String> jTableBeanProperty = BeanProperty.create("selectedElement.name");
 		BeanProperty<JTextField, String> jTextFieldBeanProperty = BeanProperty.create("text");
 		AutoBinding<JTable, String, JTextField, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, tblDetales, jTableBeanProperty, txtPavadinimas, jTextFieldBeanProperty);
 		autoBinding_1.bind();
 		//
 		BeanProperty<JTable, Object> jTableBeanProperty_1 = BeanProperty.create("selectedElement");
 		ELProperty<JTable, Object> jTableEvalutionProperty = ELProperty.create(jTableBeanProperty_1, "${form}");
 		BeanProperty<JComboBox, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
 		AutoBinding<JTable, Object, JComboBox, Object> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, tblDetales, jTableEvalutionProperty, cbxForma, jComboBoxBeanProperty);
 		autoBinding.bind();
 		//
 		BeanProperty<JTable, Object> jTableBeanProperty_2 = BeanProperty.create("selectedElement");
 		ELProperty<JTable, Object> jTableEvalutionProperty_1 = ELProperty.create(jTableBeanProperty_2, "${firstValue}");
 		BeanProperty<JSpinner, Object> jTextFieldBeanProperty_1 = BeanProperty.create("value");
 		AutoBinding<JTable, Object, JSpinner, Object> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, tblDetales, jTableEvalutionProperty_1, txtPlotis, jTextFieldBeanProperty_1);
 		autoBinding_2.bind();
 		//
 		BeanProperty<JTable, Object> jTableBeanProperty_3 = BeanProperty.create("selectedElement");
 		ELProperty<JTable, Object> jTableEvalutionProperty_2 = ELProperty.create(jTableBeanProperty_3, "${secondValue}");
 		BeanProperty<JSpinner, Object> jTextFieldBeanProperty_2 = BeanProperty.create("value");
 		AutoBinding<JTable, Object, JSpinner, Object> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, tblDetales, jTableEvalutionProperty_2, txtAukstis, jTextFieldBeanProperty_2);
 		autoBinding_3.bind();
 		//
 		BeanProperty<JTable, Object> jTableBeanProperty_4 = BeanProperty.create("selectedElement");
 		ELProperty<JTable, Object> jTableEvalutionProperty_3 = ELProperty.create(jTableBeanProperty_4, "${firstValue}");
 		BeanProperty<JSpinner, Object> jTextFieldBeanProperty_3 = BeanProperty.create("value");
 		AutoBinding<JTable, Object, JSpinner, Object> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, tblDetales, jTableEvalutionProperty_3, txtSonoIlgis, jTextFieldBeanProperty_3);
 		autoBinding_4.bind();
 		//
 		BeanProperty<CanvasSizeHandler, List<Dimension>> canvasSizeHandlerBeanProperty = BeanProperty.create("sizes");
 		JComboBoxBinding<Dimension, CanvasSizeHandler, JComboBox> jComboBinding = SwingBindings.createJComboBoxBinding(UpdateStrategy.READ, canvasSizeHandler, canvasSizeHandlerBeanProperty, cbxBrezinioDydis);
 		jComboBinding.setConverter(new DimensionConverter());
 		jComboBinding.bind();
 		//
 		BeanProperty<JTable, Object> jTableBeanProperty_5 = BeanProperty.create("selectedElement");
 		ELProperty<JTable, Object> jTableEvalutionProperty_4 = ELProperty.create(jTableBeanProperty_5, "${color}");
 		BeanProperty<JPanel, Color> jPanelBeanProperty = BeanProperty.create("background");
 		AutoBinding<JTable, Object, JPanel, Color> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, tblDetales, jTableEvalutionProperty_4, pnlSpalva, jPanelBeanProperty);
 		autoBinding_5.bind();
 	}
 }
