 package IS2011.Interfaz;
 import java.awt.*;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 
 import javax.swing.event.*;
 import javax.swing.*;
 
 //import javazoom.jlgui.basicplayer.BasicPlayerException;
 import IS2011.bibliotecaXML.Track;
 
 import com.sun.awt.AWTUtilities;
 
 /**
  * Nos muestra la playlist en la derecha de la pantalla mediante un JFrame transparente
  */
 public class SongInterfaz extends JFrame{
 	
 	
 	
 	private static final long serialVersionUID = 1L;
 	private JFrame principal;
 	private JList listado;
 	private Dimension pantalla = null;
 	private Dimension ventana = null;
 	private JScrollPane scroll=null;
 	private BotonAvanzado minButton = null;
 	private ImageIcon minIcon1 = new ImageIcon("images/skin1/minIcon1.jpg");
 	private ImageIcon minIcon2 = new ImageIcon("images/skin1/minIcon2.jpg");
 	private InterfazAvanzada interfazAvanzada= null;
 	private Color c= new Color(240,240,240);
 	
 	@SuppressWarnings("restriction")
 	public SongInterfaz(String[] temas, InterfazAvanzada interfazAvanzada){
 		
 		super("Listado de Canciones");
 		this.interfazAvanzada = interfazAvanzada;
 		pantalla = Toolkit.getDefaultToolkit().getScreenSize();
 		ventana = this.getSize();
 		principal = this;
 		principal.setSize(200, pantalla.height-200);	
 		this.centrarVentana();
 		//this.setResizable(false);					//En la otra principal
 		principal.getContentPane().setLayout(new BorderLayout());
 		principal.setUndecorated(true);
 		principal.getContentPane().setBackground(Color.black);
 		this.scroll = new JScrollPane();
 		principal.getContentPane().add(scroll,BorderLayout.EAST);
 		principal.setEnabled(true);	
 		listado = getListado(temas);
 		principal.getContentPane().add(listado, BorderLayout.CENTER);
 		principal.setAlwaysOnTop(true);
 		this.minButton = getMinButton();
 		principal.getContentPane().add(minButton, BorderLayout.SOUTH);
 		principal.setVisible(true);
 		
 		//JFrame.setDefaultLookAndFeelDecorated(true);
 		AWTUtilities.setWindowOpacity(this, (float) 0.3);
 		
 	}
 	
 	public InterfazAvanzada getInterfaz(){
 		return interfazAvanzada;
 	}
 	
 	public  JFrame getPrincipal(){
 		return principal;
 	}
 	
 	public  JList getListado(){
 		return listado;
 	}
 
 	private JList  getListado(String[] temas){
 		
 		listado = new JList(temas);
 		listado.setSize(100, pantalla.height);
 		listado.setBackground(Color.black);
 		listado.setFont(new java.awt.Font("Helvetica", 1, 12));
 
 		listado.setForeground(c);
 		listado.addMouseListener(new java.awt.event.MouseAdapter() {
 			@SuppressWarnings("restriction")
 			public void mouseEntered(java.awt.event.MouseEvent evt) {
 				AWTUtilities.setWindowOpacity(principal, (float)0.9);
 			}
 			@SuppressWarnings("restriction")
 			public void mouseExited(java.awt.event.MouseEvent evt) {
 				AWTUtilities.setWindowOpacity(principal, (float) 0.3);
 			}
 		
 		});
 		 
 		/*listado.addListSelectionListener(new ListSelectionListener() {
 		      public void valueChanged(ListSelectionEvent evt) {
 		        if (evt.getValueIsAdjusting())
 		        	 interfazAvanzada.setTrackNumber(listado.getSelectedIndex());
 		      }
 		});*/
 		
 		listado.addMouseListener(new java.awt.event.MouseAdapter(){ 
 			public void mouseClicked(MouseEvent e) 
 		    {
 				if (e.getClickCount() == 2)
 		        {
 					if(listado.getMaximumSize().getHeight() >= e.getPoint().getY()){
                         interfazAvanzada.setTrackNumber(listado.locationToIndex(e.getPoint()));
                     }
 		        }
 			}
 		});
 
 		listado.addKeyListener(new java.awt.event.KeyAdapter(){
 			public void keyReleased(KeyEvent k){
 				int c = k.getKeyCode();
 				switch(c){	// Elegimos las posibles teclas
 				case 127:	// suprimir
 					borrarSeleccionados();
 				}
 			}
 		});
 		return listado;
 	}
 	/*protected void this_windowOpened(WindowEvent e) {
         centrarVentana();
 	}*/
 	
 	/**
 	 * 
 	 * Borramos los elementos de la lista
 	 */
 	public void borrarSeleccionados(){
 		int[] listaIndices = listado.getSelectedIndices();
 		boolean b = interfazAvanzada.getListaReproduccion().borraTrack(listaIndices);
 		if(b){
 			interfazAvanzada.borradoElemActualPlaylist();
 		}
 		actualizaTemas(interfazAvanzada.getListaReproduccion().getListado());
 	}
 	
 	public void actualizaTemas(String[] temas){
 		listado.setListData(temas);		
 	}
 	
 	private BotonAvanzado getMinButton(){
 		minButton = new BotonAvanzado(minIcon1,minIcon2);
 		
 		minButton.addMouseListener(new java.awt.event.MouseAdapter() {
 			@SuppressWarnings("deprecation")
 			public void mouseReleased(java.awt.event.MouseEvent evt) {
 				setExtendedState(JFrame.CROSSHAIR_CURSOR); 
 				// TODO
 			};
 		});
 		return minButton;
 		
 	}
 	
 	private void centrarVentana() {
         // Se obtienen las dimensiones en pixels de la pantalla.
         Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
         // Se obtienen las dimensiones en pixels de la ventana.
         Dimension ventana = principal.getSize();
         // Una cuenta para situar la ventana en el centro de la pantalla.
         principal.setLocation((pantalla.width)-200,
                         (pantalla.height - ventana.height) / 2);
 	}
 	
 }
