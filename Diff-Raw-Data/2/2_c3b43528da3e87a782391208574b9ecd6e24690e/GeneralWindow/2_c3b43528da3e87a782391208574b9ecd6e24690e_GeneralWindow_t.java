 package ve.com.fml.view;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.JButton;
 import javax.swing.border.LineBorder;
 import java.awt.Color;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.LayoutManager;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 
 import javax.swing.border.TitledBorder;
 import javax.swing.border.BevelBorder;
 import javax.swing.JLabel;
 import javax.swing.SwingConstants;
 
 import ve.com.fml.model.datasource.GlobalData;
 import ve.com.fml.model.fuzzy.FuzzyDataMining;
 
 import java.awt.CardLayout;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 public class GeneralWindow extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 	private JPanel contentPane;
 	private JPanel real_interface;
 	static JPanel pnl_ProgressCromosoma;
 	static JPanel pnl_ProgressFuncion2;
 	static JPanel pnl_ProgressConfig1;
 	static JPanel pnl_ProgressConfig2;
 	static JPanel pnl_ProgressConfig3;
 	static JPanel pnl_ProgressResultados;
 	
 	//Botones
 	JButton btnDefinirFuncin;
 	JButton btnConfiguraralgoritmo;
 	
 	//Progress data
 	JLabel labelNombreConjuntoValor;
 	JLabel labelAtributosValor;
 	JLabel labelInstanciasValor;
 	JLabel labelNumeroConjuntosDifusosValor;
 	
 	JLabel labelNombreTecnicaValor;
 	JLabel labelConfiguracionValor;
 	
 	JPanel panelTecnicaConfiguracion;
 	
 	/**
 	 * Create the frame.
 	 */
 	public GeneralWindow() {
 		setResizable(false);
 		setTitle("jFML");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 800, 580);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		
 		JPanel buttons = new JPanel();
 		buttons.setBounds(5, 5, 144, 430);
 		
 		final JPanel cardPanel = new JPanel();
 		cardPanel.setBounds(154, 5, 630, 435);
 		cardPanel.setLayout(new CardLayout(0, 0));
 		cardPanel.add(new EsquemaDeTrabajo(), "Esquema de trabajo");
 		cardPanel.add(new DataConfigurationWindow(), "Cargar datos");
 		cardPanel.add(new DataMiningTechniqueWindow(), "Seleccion tecnica");
 		//cardPanel.add(new DefinirCromosoma(), GAL_GUI.language.casosDeUso[1]);
 		//cardPanel.add(new DefinirFuncion(), GAL_GUI.language.casosDeUso[2]);
 		//cardPanel.add(new ConfigurarAlgoritmo(), GAL_GUI.language.casosDeUso[3]);
 		//cardPanel.add(new EjecutarAlgoritmo(), GAL_GUI.language.casosDeUso[4]);
 		
 //		JButton btnEsquemaDeTrabajo = new JButton(GAL_GUI.language.casosDeUso[0]);
 		JButton btnEsquemaDeTrabajo = new JButton("Esquema de trabajo");
 		btnEsquemaDeTrabajo.setBounds(3, 20, 141, 30);
 		btnEsquemaDeTrabajo.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if(! (real_interface instanceof EsquemaDeTrabajo))
 					((CardLayout)cardPanel.getLayout()).show(cardPanel,"Esquema de trabajo");
 			}
 		});
 		btnEsquemaDeTrabajo.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		
 //		JButton btnDefinirCromosoma = new JButton(GAL_GUI.language.casosDeUso[1]);
 		JButton btnLoadData = new JButton("Carga de Datos");
 		btnLoadData.setBounds(3, 90, 141, 30);
 		btnLoadData.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if(! (real_interface instanceof DataConfigurationWindow))
 					((CardLayout)cardPanel.getLayout()).show(cardPanel,"Cargar datos");
 			}
 		});
 		btnLoadData.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		
 		//JButton btnDefinirFuncin = new JButton(GAL_GUI.language.casosDeUso[2]);
 		btnDefinirFuncin = new JButton("Seleccionar Tcnica");
 		btnDefinirFuncin.setBounds(3, 160, 141, 30);
 		btnDefinirFuncin.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if(! (real_interface instanceof DataMiningTechniqueWindow))
 					((CardLayout)cardPanel.getLayout()).show(cardPanel,"Seleccion tecnica");
 			}
 		});
 		btnDefinirFuncin.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		
 //		JButton btnConfiguraralgoritmo = new JButton(GAL_GUI.language.casosDeUso[3]);
 		btnConfiguraralgoritmo = new JButton("Ejecutar algoritmo");
 		btnConfiguraralgoritmo.setBounds(3, 230, 141, 30);
 		btnConfiguraralgoritmo.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				/*if(! (real_interface instanceof ConfigurarAlgoritmo))
 					((CardLayout)cardPanel.getLayout()).show(cardPanel,GAL_GUI.language.casosDeUso[3]);*/
 			}
 		});
 		btnConfiguraralgoritmo.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		
 //		JButton btnSalir = new JButton(GAL_GUI.language.casosDeUso[5]);
 		JButton btnSalir = new JButton("Salir");
 		btnSalir.setBounds(3, 300, 141, 30);
 		btnSalir.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 		});
 		btnSalir.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		
 //		JButton btnEjecutarAlgoritmo = new JButton(GAL_GUI.language.casosDeUso[4]);
 //		JButton btnEjecutarAlgoritmo = new JButton("Cargar datos");
 //		btnEjecutarAlgoritmo.setBounds(3, 300, 141, 30);
 //		btnEjecutarAlgoritmo.addActionListener(new ActionListener() {
 //			public void actionPerformed(ActionEvent e) {
 ////				if(! (real_interface instanceof EjecutarAlgoritmo))
 ////					((CardLayout)cardPanel.getLayout()).show(cardPanel,GAL_GUI.language.casosDeUso[4]);*/
 //				if(! (real_interface instanceof DataConfigurationWindow))
 //					((CardLayout)cardPanel.getLayout()).show(cardPanel,"Cargar datos");
 //			}
 //		});
 //		btnEjecutarAlgoritmo.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		
 		/*
 		 * 
 		 * BARRA DE PROGRESO
 		 * 
 		 */
 		
 		JPanel progress = new JPanel();
 		progress.setBounds(5, 446, 779, 101);
 		progress.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 2), "Progress", TitledBorder.LEADING, TitledBorder.TOP, new Font("Arial",Font.BOLD,12), new Color(0, 0, 0)));
 		
 		JLabel labelDataset = new JLabel("DataSet");
 		labelDataset.setBounds(40, 15, 120, 23);
 		labelDataset.setFont(new Font("Tahoma", Font.BOLD , 14));
 		
 		pnl_ProgressCromosoma = new JPanel();
 		pnl_ProgressCromosoma.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
 		
 		//JLabel lblVariables = new JLabel(GAL_GUI.language.progreso[1]);
 		JLabel labelNombre = new JLabel("Nombre: ");
 		labelNombre.setBounds(40, 36, 60, 12);
 		labelNombre.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		
 			labelNombreConjuntoValor = new JLabel("-");
 			labelNombreConjuntoValor.setBounds(100, 36, 150, 12);
 			labelNombreConjuntoValor.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		
 		JLabel labelAtributos = new JLabel("Atributos: ");
 		labelAtributos.setBounds(40, 50, 60, 12);
 		labelAtributos.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		
 			labelAtributosValor = new JLabel("-");
 			labelAtributosValor.setBounds(100, 50, 150, 12);
 			labelAtributosValor.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		
 		JLabel labelInstancias = new JLabel("Instancias: ");
 		labelInstancias.setBounds(40, 64, 60, 12);
 		labelInstancias.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		
 			labelInstanciasValor = new JLabel("-");
 			labelInstanciasValor.setBounds(100, 64, 150, 12);
 			labelInstanciasValor.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		
 		JLabel labelConjuntos = new JLabel("Conjuntos: ");
 		labelConjuntos.setBounds(40, 78, 60, 12);
 		labelConjuntos.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		
 			labelNumeroConjuntosDifusosValor = new JLabel("-");
 			labelNumeroConjuntosDifusosValor.setBounds(100, 78, 150, 12);
 			labelNumeroConjuntosDifusosValor.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		
 		
 		JLabel labelTecnica = new JLabel("Tcnica");
 		labelTecnica.setBounds(255, 15, 120, 23);
 		labelTecnica.setFont(new Font("Tahoma", Font.BOLD , 14));
 		
 		JLabel labelNombreTecnica = new JLabel("Nombre: ");
 		labelNombreTecnica.setBounds(255, 36, 75, 12);
 		labelNombreTecnica.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		
 			labelNombreTecnicaValor = new JLabel("-");
 			labelNombreTecnicaValor.setBounds(330, 36, 150, 12);
 			labelNombreTecnicaValor.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		
 		JLabel labelConfiguracionTecnica = new JLabel("Configuracin: ");
 		labelConfiguracionTecnica.setBounds(255, 50, 75, 12);
 		labelConfiguracionTecnica.setFont(new Font("Tahoma", Font.PLAIN, 11));
 			
 			panelTecnicaConfiguracion = new JPanel((LayoutManager) new FlowLayout(FlowLayout.LEADING));
 			panelTecnicaConfiguracion.setBounds(330, 50, 80, 38);
 			panelTecnicaConfiguracion.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
 		
 		//JButton btnVerResultados = new JButton(GAL_GUI.language.progreso[9]);
 		JButton btnLimpiar = new JButton("Limpiar Datos");
 		btnLimpiar.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if(JOptionPane.showConfirmDialog(null, "Desea limpiar todo?","Aceptar",JOptionPane.OK_CANCEL_OPTION)== JOptionPane.OK_OPTION){
 					GlobalData.clearInstance();
 					mainWindowRefresh();
 				}
 			}
 		});
 		btnLimpiar.setBounds(450, 30, 120, 23);
 		btnLimpiar.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		
 //		JButton btnVerConfiguracion = new JButton(GAL_GUI.language.progreso[5]);
 		JButton btnAyuda = new JButton("Ayuda");
 		btnAyuda.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				ConfigurationViewWindow Newframe= new ConfigurationViewWindow();
 				Newframe.setVisible(true);
 			}
 		});
 		btnAyuda.setBounds(600, 30, 120, 23);
 		btnAyuda.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		
 //		
 		
 		
 		contentPane.setLayout(null);
 		contentPane.add(progress);
 		progress.setLayout(null);
 		progress.add(labelDataset);
 			progress.add(labelNombre);
 			progress.add(labelNombreConjuntoValor);
 			progress.add(labelAtributos);
 			progress.add(labelAtributosValor);
 			progress.add(labelInstancias);
 			progress.add(labelInstanciasValor);
 			progress.add(labelConjuntos);
 			progress.add(labelNumeroConjuntosDifusosValor);
 			
 			progress.add(labelTecnica);
 			progress.add(labelNombreTecnica);
 			progress.add(labelNombreTecnicaValor);
 			progress.add(labelConfiguracionTecnica);
 			//progress.add(labelConfiguracionValor);
 			progress.add(panelTecnicaConfiguracion);
 	
 		progress.add(btnAyuda);
 		progress.add(btnLimpiar);
 
 		
 		/*JButton btn_LimpiarTodo = new JButton("");
 		btn_LimpiarTodo.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 //				int ret=JOptionPane.showConfirmDialog(null, GAL_GUI.language.Questions[6],GAL_GUI.language.CommonWords[8],JOptionPane.OK_CANCEL_OPTION);
 				int ret=JOptionPane.showConfirmDialog(null, "Desea limpiar todo?","Aceptar",JOptionPane.OK_CANCEL_OPTION);
 				if(ret== JOptionPane.OK_OPTION){
 					//GAL_GUI.gal.limpiarTodo();
 					pnl_ProgressCromosoma.setBackground(new Color(240, 240, 240));
 					pnl_ProgressFuncion1.setBackground(new Color(240, 240, 240));
 					pnl_ProgressFuncion2.setBackground(new Color(240, 240, 240));
 					pnl_ProgressConfig1.setBackground(new Color(240, 240, 240));
 					pnl_ProgressConfig2.setBackground(new Color(240, 240, 240));
 					pnl_ProgressConfig3.setBackground(new Color(240, 240, 240));
 					pnl_ProgressResultados.setBackground(new Color(240, 240, 240));
 				}
 			}
 		});
 		//btn_LimpiarTodo.setIcon(new ImageIcon(GeneralWindow.class.getResource("/Images/limpiar.png")));
 		//btn_LimpiarTodo.setToolTipText(GAL_GUI.language.CommonWords[8]);
 		btn_LimpiarTodo.setBounds(15, 18, 20, 20);
 		progress.add(btn_LimpiarTodo);*/
 		
 		JPanel help = new JPanel();
 //		help.addMouseListener(new MouseAdapter() {
 //			@Override
 //			public void mouseClicked(MouseEvent e) {
 //				GAL_GUI.helpViewer.setCurrentID(GAL_GUI.language.helpTargets[4]);
 //				// Create a new frame.
 //				JFrame helpFrame = new JFrame();
 //				// Set it's size.
 //				helpFrame.setSize(800,600);
 //				// Add the created helpViewer to it.
 //				helpFrame.getContentPane().add(GAL_GUI.helpViewer);
 //				// Set a default close operation.
 //				helpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 //				//Ponemos en visible
 //				helpFrame.setVisible(true);
 //			}
 //		});
 		help.setBackground(new Color(0, 0, 0));
 		help.setBounds(747, 7, 28, 28);
 		progress.add(help);
 		help.setLayout(null);
 		
 		JLabel label = new JLabel("");
 		label.setHorizontalAlignment(SwingConstants.CENTER);
 		//label.setIcon(new ImageIcon(GeneralWindow.class.getResource("/Images/help.png")));
 		label.setBackground(Color.BLACK);
 		label.setBounds(0, 0, 28, 28);
 		help.add(label);
 		contentPane.add(buttons);
 		buttons.setLayout(null);
 		buttons.add(btnEsquemaDeTrabajo);
 		buttons.add(btnSalir);
 		//buttons.add(btnEjecutarAlgoritmo);
 		buttons.add(btnConfiguraralgoritmo);
 		buttons.add(btnDefinirFuncin);
 		buttons.add(btnLoadData);
 		
 		contentPane.add(cardPanel);
 		
 		addWindowListener(new WindowListener() {
 			
 			@Override
 			public void windowOpened(WindowEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void windowIconified(WindowEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void windowDeiconified(WindowEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void windowDeactivated(WindowEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void windowClosing(WindowEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void windowClosed(WindowEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void windowActivated(WindowEvent e) {
 				// TODO Auto-generated method stub
 				mainWindowRefresh();
 			}
 		});
 	}
 	
 	private void mainWindowRefresh(){
 		btnConfiguraralgoritmo.setEnabled(true);
 		btnDefinirFuncin.setEnabled(true);
 
 		if (!GlobalData.instanceCreated()) {
 			btnDefinirFuncin.setEnabled(false);
 			btnConfiguraralgoritmo.setEnabled(false);
 			clearTextFields1();
 		}else if(GlobalData.getInstance().getConfiguredTechnique() == null){
 			btnConfiguraralgoritmo.setEnabled(false);
 			clearTextFields1();
 			setEtapa1Data();
 		}else if(GlobalData.getInstance().getConfiguredTechnique() != null){
 			clearTextFields2();
 			System.out.println("Current technique: "+GlobalData.getInstance().getConfiguredTechnique().values());
 			setEtapa2Data();
 		}
 		
 		
 	}
 	
 	private void setEtapa2Data() {
 		labelNombreTecnicaValor.setText(FuzzyDataMining.names[GlobalData.getInstance().getCurrentTechnique()]);
 		//labelConfiguracionValor.setText(clrTxt);
 		Iterator<Entry<String, Object>> it = GlobalData.getInstance().getConfiguredTechnique().entrySet().iterator();
 		 System.out.println("Hola: "+it);
 		while (it.hasNext()) {
 			Entry<String, Object> pair = it.next();
 	        JLabel config = new JLabel(pair.getKey() + " = " + pair.getValue());
 	        config.setFont(new Font("Tahoma", Font.PLAIN, 11));
 	        panelTecnicaConfiguracion.add(config);       
	        //it.remove(); // avoids a ConcurrentModificationException
 	    }
 	}
 
 	private void setEtapa1Data() {
 		labelNombreConjuntoValor.setText(GlobalData.getInstance().getDatasetName());
 		labelAtributosValor.setText(""+(GlobalData.getInstance().getFuzzyInstances().numAttributes()-1));
 		labelInstanciasValor.setText(""+GlobalData.getInstance().getFuzzyInstances().numInstances());
 		labelNumeroConjuntosDifusosValor.setText(""+GlobalData.getInstance().getFuzzyInstances().numClasses());
 	}
 	
 	
 
 	private void clearTextFields1() {
 		String clrTxt = "-";
 		labelNombreConjuntoValor.setText(clrTxt);
 		labelAtributosValor.setText(clrTxt);
 		labelInstanciasValor.setText(clrTxt);
 		labelNumeroConjuntosDifusosValor.setText(clrTxt);
 	}
 	private void clearTextFields2() {
 		String clrTxt = "-";
 		labelNombreTecnicaValor.setText(clrTxt);
 		//labelConfiguracionValor.setText(clrTxt);
 		panelTecnicaConfiguracion.removeAll();
 		panelTecnicaConfiguracion.validate();
 		panelTecnicaConfiguracion.updateUI();
 	}
 }
