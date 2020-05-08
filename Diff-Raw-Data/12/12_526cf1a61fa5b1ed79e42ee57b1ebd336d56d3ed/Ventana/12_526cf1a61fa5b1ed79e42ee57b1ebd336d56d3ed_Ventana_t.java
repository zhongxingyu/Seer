 package sistema.vista.visual;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SpringLayout;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.TitledBorder;
 
 import sistema.controladores.ListenerComandos;
 import sistema.controladores.ordenes.Dispatcher;
 import sistema.entidades.personas.ciclistas.Ciclista;
 import sistema.manager.VariablesDeContexto;
 import sistema.vista.Lienzo;
 
 public class Ventana extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 	
 	private JPanel contentPane;
 	
 	private JTextField txtReloj;
 	
 	private PanelCiclista panel;
 	private PanelCiclista panel1;
 	private PanelCiclista panel2;
 	private PanelCiclista panel3;
 	private JTextArea taComandos;
 	private JTextArea taRegistro;
 	
 	private Dispatcher micomandero;
 
 	/**
 	 * Launch the application.
 	 */
 //	public static void main(String[] args) {
 //		EventQueue.invokeLater(new Runnable() {
 //			public void run() {
 //				try {
 //					List<Ciclista> a = new ArrayList<>();
 //					
 //					List<ObjetosConSalidaDeDatos> v = new ArrayList<>();
 //					
 //					ParseadorComandos p = new ParseadorComandos();
 //					
 //					Ventana vv = null;
 //					
 //					vv = new Ventana(new Dispatcher(new Presentador(a, v, new HashMap<Integer, MiViento>(), p.getOrdenes()), p, new FormateadorDatosVista(v, vv)));
 //					
 //				} catch (Exception e) {
 //					e.printStackTrace();
 //				}
 //			}
 //		});
 //	}
 
 	/**
 	 * Create the frame.
 	 */
 	public Ventana(Dispatcher comandero) {
 		
 		micomandero = comandero;
 		
 		init();
 		
 		// Hace saber al sistema que la gui está lista.
 		VariablesDeContexto.SYN_GUI = true;
 	}
 	
 	/**
 	 * Añade nueva información en un área de texto o crea una nueva
 	 * si fuese necesario.
 	 * 
 	 * @param id Objeto que mostrará sus datos.
 	 * @param mensajes Los datos formateados a poner en el área.
 	 */
 	public void ponerDatosEnVentana(String id, Object... mensajes) {
 		
 		try {
 			switch(id) {
 			case "ruloj":
 				
 				txtReloj.setText(mensajes[0] + "h " + mensajes[1] + "m " + mensajes[2] + "s " + mensajes[3] + "ms ");
 				
 				break;
 			case "0 ciclista":
 				
 				String[] datos = (String[])mensajes;
 				
 				panel.setCiclistaData(datos[0] + " " + datos[1], Integer.valueOf(datos[2]), Integer.valueOf(datos[3]), Double.valueOf(datos[4]));
 				
 				break;
 			case "1 ciclista":
 				
 				datos = (String[])mensajes;
 				
 				panel1.setCiclistaData(datos[0] + " " + datos[1], Integer.valueOf(datos[2]), Integer.valueOf(datos[3]), Double.valueOf(datos[4]));
 				
 				break;
 			case "2 ciclista":
 				
 				datos = (String[])mensajes;
 				
 				panel2.setCiclistaData(datos[0] + " " + datos[1], Integer.valueOf(datos[2]), Integer.valueOf(datos[3]), Double.valueOf(datos[4]));
 				
 				break;
 			case "3 ciclista":
 				
 				datos = (String[])mensajes;
 				
 				panel3.setCiclistaData(datos[0] + " " + datos[1], Integer.valueOf(datos[2]), Integer.valueOf(datos[3]), Double.valueOf(datos[4]));
 				
 				break;
 			case "0 bicicleta":
 				
 				datos = (String[])mensajes;
 				
 				panel.setBicicletaData(datos[0], datos[1], Integer.valueOf(datos[2]), Integer.valueOf(datos[3]));
 				
 				break;
 			case "1 bicicleta":
 				
 				datos = (String[])mensajes;
 				
 				panel1.setBicicletaData(datos[0], datos[1], Integer.valueOf(datos[2]), Integer.valueOf(datos[3]));
 				
 				break;
 			case "2 bicicleta":
 				
 				datos = (String[])mensajes;
 				
 				panel2.setBicicletaData(datos[0], datos[1], Integer.valueOf(datos[2]), Integer.valueOf(datos[3]));
 				
 				break;
 			case "3 bicicleta":
 				
 				datos = (String[])mensajes;
 				
 				panel3.setBicicletaData(datos[0], datos[1], Integer.valueOf(datos[2]), Integer.valueOf(datos[3]));
 				
 				break;
 			case "ayudaMain":
 				
				taRegistro.setText(taRegistro.getText() + "\n" + (String)mensajes[0]);
 				
 				break;
 			case "log":
 				
				taRegistro.setText(taRegistro.getText() + "\n" + (String)mensajes[0]);
 				
 				break;
 			default:
 			}
 		} catch (NumberFormatException ne) {
 			ne.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Construye el interfaz.
 	 */
 	private void init() {
 		
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 1220, 900);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(new BorderLayout());
 		
 		JPanel panelReloj = new JPanel();
 		panelReloj.setPreferredSize(new Dimension(35, 35));
 		panelReloj.setMinimumSize(new Dimension(35, 35));
 		contentPane.add(panelReloj, BorderLayout.NORTH);
 		SpringLayout sl_panelReloj = new SpringLayout();
 		panelReloj.setLayout(sl_panelReloj);
 		
 		JLabel lblReloj = new JLabel("Reloj:");
 		sl_panelReloj.putConstraint(SpringLayout.NORTH, lblReloj, 11, SpringLayout.NORTH, panelReloj);
 		sl_panelReloj.putConstraint(SpringLayout.WEST, lblReloj, 518, SpringLayout.WEST, panelReloj);
 		panelReloj.add(lblReloj);
 		
 		txtReloj = new JTextField();
 		sl_panelReloj.putConstraint(SpringLayout.NORTH, txtReloj, 5, SpringLayout.NORTH, panelReloj);
 		sl_panelReloj.putConstraint(SpringLayout.WEST, txtReloj, 558, SpringLayout.WEST, panelReloj);
 		sl_panelReloj.putConstraint(SpringLayout.EAST, txtReloj, 726, SpringLayout.WEST, panelReloj);
 		txtReloj.setMinimumSize(new Dimension(150, 28));
 		txtReloj.setPreferredSize(new Dimension(150, 28));
 		panelReloj.add(txtReloj);
 		txtReloj.setColumns(10);
 		
 		JPanel panelGlobal = new JPanel();
 		panelGlobal.setLayout(new GridLayout(2, 1, 0, 0));
 		contentPane.add(panelGlobal, BorderLayout.CENTER);
 
 		JPanel panelsuperior = new JPanel();
 		panelsuperior.setLayout(new BorderLayout());
 		panelGlobal.add(panelsuperior);
 		
 		JPanel panelCiclistas = new JPanel();
 		panelsuperior.add(panelCiclistas, BorderLayout.CENTER);
 		panelCiclistas.setLayout(new GridLayout(1, 4, 0, 0));
 		
 		panel = new PanelCiclista(micomandero);
 		panelCiclistas.add(panel);
 		
 		panel1 = new PanelCiclista(micomandero);
 		panelCiclistas.add(panel1);
 		
 		panel2 = new PanelCiclista(micomandero);
 		panelCiclistas.add(panel2);
 		
 		panel3 = new PanelCiclista(micomandero);
 		panelCiclistas.add(panel3);
 		
 		JPanel panelComandos = new JPanel();
 		panelComandos.setPreferredSize(new Dimension(10, 110));
 		panelComandos.setMaximumSize(new Dimension(32767, 150));
 		panelsuperior.add(panelComandos, BorderLayout.SOUTH);
 		panelComandos.setLayout(new GridLayout(1, 2, 0, 0));
 		
 		taComandos = new JTextArea();
 		taComandos.setBorder(new TitledBorder("Comandos"));
 		taComandos.setAutoscrolls(true);
 		taComandos.addKeyListener(new ListenerComandos(micomandero));
 		
 		JScrollPane scrollpane = new JScrollPane(taComandos);
 		panelComandos.add(scrollpane);
 		
 		taRegistro = new JTextArea();
 		taRegistro.setBorder(new TitledBorder(null, "Registro", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		taRegistro.setAutoscrolls(true);
 		taRegistro.setEditable(false);
 		
 		JScrollPane scrollpaner = new JScrollPane(taRegistro);
 		panelComandos.add(scrollpaner);
 		
 		Lienzo canvas = new Lienzo(new ArrayList<Ciclista>());
 		panelGlobal.add(canvas);
 		
 		setVisible(true);
 	}
 }
