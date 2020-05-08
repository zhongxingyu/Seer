 package GUI;
 
 /*
  * Sistema Educativo - Teoría de Control
  * 
  * Tema: Sintonización de Controladores
  * 
  * Director: Ing. Darío Weitz
  * Autor: Pablo Patarca, Leandro Lambertucci
  * 
  * Fecha de lamzamiento: 17/12/2013
  */
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JPanel;
 import javax.swing.JTable;
 import javax.swing.SwingConstants;
 import javax.swing.UIManager;
 import javax.swing.border.EmptyBorder;
 
 
 public class MainView extends JFrame {
 	
 	private double minW = 1060;
 	private double minH = 630;
 
 
 	private static final long serialVersionUID = 1L;
 	private JPanel contentPane;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					MainView frame = new MainView();
 					frame.setVisible(true);
 					//frame.setLocationRelativeTo(null);
 					frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	public MainView() {
 		
 		Dimension d = new Dimension();
 		d.setSize(minW, minH);
 		this.setMinimumSize(d);

 		try
 		{
 		    JFrame.setDefaultLookAndFeelDecorated(true);
 		    JDialog.setDefaultLookAndFeelDecorated(true);
 		    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		}
 		catch (Exception e)
 		{
 		    e.printStackTrace();
 		}
 
 		final MainView frame = this;
 		//this.setResizable(false);
 		
 		setTitle("Sintonizaci\u00F3n de controladores");
 		setIconImage(Toolkit.getDefaultToolkit().getImage(MainView.class.getResource("/icons/imagen de respuessta transitoria.jpg")));
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		//setBounds(100, 100, 1010, 637);//M\u00E9todos de Ziegler y Nichols
 		
 		JMenuBar menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 		
 		contentPane = new JPanel();
 		contentPane.setBackground(Color.LIGHT_GRAY);
 		contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
 		setContentPane(contentPane);
 		contentPane.setLayout(new BorderLayout(0, 0));
 		
 		/* Curva reacción - Ziegler and Nichols */
 		final JMenu curvaR = new JMenu("Curva Reacción Z-N");
 		curvaR.setHorizontalAlignment(SwingConstants.CENTER);
 		curvaR.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				contentPane.removeAll();
 				MethodPanelView panel = new MethodPanelView(frame, "zn");
 				//PanelCurvaReaccion panel = new PanelCurvaReaccion(frame);
 				//contentPane.add(panel.getContentPane(), BorderLayout.CENTER);
 				contentPane.add(panel.getMainPanel(), BorderLayout.CENTER);
 				contentPane.updateUI();
				//setLocationRelativeTo(null);
 				//Focus first field
 				JTable tabla = panel.getInputTable();
 				tabla.requestFocus();
 				tabla.changeSelection(0, 0, false, false);
 				
 			}
 			
 			@Override
 			public void mouseExited(MouseEvent e) {
 				curvaR.setSelected(false);
 		    }
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				curvaR.setSelected(true);
 		    }
 		});
 		
 		/* Cohen y Coon - Lazo abierto */
 		final JMenu cohenCoon = new JMenu("Cohen - Coon");
 		cohenCoon.setHorizontalAlignment(SwingConstants.CENTER);
 		cohenCoon.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				contentPane.removeAll();
 				MethodPanelView panel = new MethodPanelView(frame, "cc");
 				contentPane.add(panel.getMainPanel(), BorderLayout.CENTER);
 				contentPane.updateUI();
				//setLocationRelativeTo(null);
 				//Focus first field
 				JTable tabla = panel.getInputTable();
 				tabla.requestFocus();
 				tabla.changeSelection(0, 0, false, false);
 			}
 			
 			@Override
 			public void mouseExited(MouseEvent e) {
 				cohenCoon.setSelected(false);
 		    }
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				cohenCoon.setSelected(true);
 		    }
 		});
 		
 		/* Lopez - Lazo abierto */
 		final JMenu lopez = new JMenu("Lopez");
 		lopez.setHorizontalAlignment(SwingConstants.CENTER);
 		lopez.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				contentPane.removeAll();
 				MethodPanelView panel = new MethodPanelView(frame, "lopez");
 				contentPane.add(panel.getMainPanel(), BorderLayout.CENTER);
 				contentPane.updateUI();
				//setLocationRelativeTo(null);
 				//Focus first field
 				JTable tabla = panel.getInputTable();
 				tabla.requestFocus();
 				tabla.changeSelection(0, 0, false, false);
 				
 			}
 			@Override
 			public void mouseExited(MouseEvent e) {
 				lopez.setSelected(false);
 		    }
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				lopez.setSelected(true);
 		    }
 		});
 		
 		/* Kaya and Sheib Method */
 		final JMenu kayaSheib = new JMenu("Kaya-Sheib");
 		kayaSheib.setHorizontalAlignment(SwingConstants.CENTER);
 		kayaSheib.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e){
 				contentPane.removeAll();
 				MethodPanelView panel = new MethodPanelView(frame, "ks");
 				contentPane.add(panel.getMainPanel(), BorderLayout.CENTER);
 				contentPane.updateUI();
				//setLocationRelativeTo(null);
 				//Focus first field
 				JTable tabla = panel.getInputTable();
 				tabla.requestFocus();
 				tabla.changeSelection(0, 0, false, false);
 			}
 			@Override
 			public void mouseExited(MouseEvent e) {
 				kayaSheib.setSelected(false);
 		    }
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				kayaSheib.setSelected(true);
 		    }
 		});
 		
 		
 		/* Oscilaciones Sostenidas - Ziegler-Nichols*/
 		final JMenu oscilaciones = new JMenu("Oscilaciones Sostenidas Z-N");
 		oscilaciones.setHorizontalAlignment(SwingConstants.CENTER);
 		oscilaciones.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				contentPane.removeAll();
 				PanelClosedZieglerNichols panel = new PanelClosedZieglerNichols(frame);
 				contentPane.add(panel.getMainPanel(), BorderLayout.CENTER);
 				contentPane.updateUI();
				//setLocationRelativeTo(null);
 			}
 			@Override
 			public void mouseExited(MouseEvent e) {
 				oscilaciones.setSelected(false);
 		    }
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				oscilaciones.setSelected(true);
 		    }
 		});
 		
 		//Results Compare
 		final JMenu compare = new JMenu("Comparación");
 		compare.setHorizontalAlignment(SwingConstants.CENTER);
 		compare.addMouseListener(new MouseAdapter(){
 			@Override
 			public void mouseClicked(MouseEvent e){
 				contentPane.removeAll();
 				PanelCompare panel = new PanelCompare(frame);
 				contentPane.add(panel.getMainPanel(), BorderLayout.CENTER);
 				//PanelCompare panel = new PanelCompare(frame);
 				//contentPane.add(panel.getMainPane(), BorderLayout.CENTER);
 				contentPane.updateUI();
 				panel.getInputField().requestFocus();
 				
 			}
 			
 			@Override
 			public void mouseExited(MouseEvent e) {
 				compare.setSelected(false);
 		    }
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				compare.setSelected(true);
 		    }
 			
 		});
 		
 		
 		/* Salir */
 		
 		final JMenu mnExit = new JMenu("Salir");
 		mnExit.setHorizontalAlignment(SwingConstants.CENTER);
 		mnExit.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				System.exit(0);
 			}
 			@Override
 			public void mouseExited(MouseEvent e) {
 				mnExit.setSelected(false);
 		    }
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				mnExit.setSelected(true);
 		    }
 		});
 		
 		
 		
 		
 		/* Agrego opciones del según orden */
 		//Lazo abierto
 		menuBar.add(curvaR);		//Ziegler-Nichols
 		menuBar.add(cohenCoon);		//Cohen-Coon
 		menuBar.add(lopez);			//Lopez
 		menuBar.add(kayaSheib);		//Kaya-Sheib
 		menuBar.add(compare);
 
 		//JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
 		//separator.setPreferredSize(new Dimension(50, 20));
 		//menuBar.add(separator);
 		JMenu separator = new JMenu("||");
 		JMenu separator2 = new JMenu("||");
 		separator.setEnabled(false);
 		separator2.setEnabled(false);
 		
 		menuBar.add(separator);
 		//Lazo cerrado
 		
 		menuBar.add(oscilaciones);	//Ziegler-Nichols
 		
 		menuBar.add(separator2);
 
 		menuBar.add(mnExit);
 		
 		
 		curvaR.setMargin(new Insets(0, 20, 0, 20));
 		cohenCoon.setMargin(new Insets(0, 20, 0, 20));
 		lopez.setMargin(new Insets(0, 20, 0, 20));
 		kayaSheib.setMargin(new Insets(0, 20, 0, 20));
 		compare.setMargin(new Insets(0, 20, 0, 20));
 		
 		separator.setMargin(new Insets(0, 0, 0, 40));
 		separator2.setMargin(new Insets(0, 0, 0, 40));
 		
 		oscilaciones.setMargin(new Insets(0, 20, 0, 20));
 		mnExit.setMargin(new Insets(0, 20, 0, 20));
 		
 	}
 }
