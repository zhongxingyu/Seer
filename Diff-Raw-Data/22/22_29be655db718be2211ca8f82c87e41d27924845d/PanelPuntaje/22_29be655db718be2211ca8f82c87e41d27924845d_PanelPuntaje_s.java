 package vista.Ventanas.Menu;
 
import java.awt.BorderLayout;
 import java.awt.Color;
import java.awt.FlowLayout;
 import java.awt.Font;
import java.awt.GridLayout;
 
 import javax.swing.BoxLayout;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
import javax.swing.JTextArea;
 
 public class PanelPuntaje extends JPanel{
 	private static final long serialVersionUID = 1L;
 	private JLabel nivelNumero;
 	private JLabel avionesCantidad;
 	public static PanelPuntaje elPanel;
 	
 
 public static PanelPuntaje obtenerPanel(){
 		if (elPanel==null){
 			elPanel=new PanelPuntaje();
 		}
 		return elPanel;
 	}
 private PanelPuntaje(){
 		
 		//this.setBackground(Color.black);
 		setOpaque(false);
 		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		this.setSize(20,20);
 		
 		Font fuente=new Font("Arial", Font.BOLD, 20);
 		JLabel titulo = new JLabel("PUNTAJE:   ");
 		titulo.setForeground(Color.blue);
 		titulo.setFont(new Font("Arial", Font.BOLD, 30));
 		
 		JLabel nivel = new JLabel("-Nivel:   ");
 		nivel.setForeground(Color.blue);
 		nivel.setFont(fuente);
 		
 		nivelNumero = new JLabel("1");
 		nivelNumero.setForeground(Color.blue);
 		nivelNumero.setFont(fuente);
 		
 		JLabel avionesAterrizados = new JLabel("-Aviones Aterrizados:   ");
 		avionesAterrizados.setForeground(Color.blue);
 		avionesAterrizados.setFont(fuente);
 		
 		avionesCantidad = new JLabel("0");
 		avionesCantidad.setForeground(Color.blue);
 		avionesCantidad.setFont(fuente);
 		
 		add(titulo);
 		add(nivel);
 		add(nivelNumero);
 		add(avionesAterrizados);
 		add(avionesCantidad);
 	}
 	
 	public void cambiarAvionesAterrizados(String textoAEscribir){
 		avionesCantidad.setText(textoAEscribir);
 	}
 	
 	public void reiniciar(){
 		this.cambiarAvionesAterrizados("0");
 		this.cambiarNivel("1");
 	}
 	
 	public void cambiarNivel(String textoAEscribir){
 		nivelNumero.setText(textoAEscribir);
 	}
 
 }
