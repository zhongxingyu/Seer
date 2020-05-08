 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 
 import facturacion.Operador;
 
 public class EscuchadorBotonParticular implements ActionListener{
 
 	JFrame ventana_paso_1;
 	Operador op;
 	
 	public EscuchadorBotonParticular(JFrame ventana_paso_1, Operador op){
 		this.ventana_paso_1 = ventana_paso_1;
 		this.op = op;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		ventana_paso_1.dispose();
 		System.out.println("Seleccionaste cliente particular.");
 		JFrame ventana = new JFrame("Añadir cliente");
 		Container contenedor = ventana.getContentPane();
 		contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.PAGE_AXIS));
 		String html = "<html>" +
                 "<b>Paso 2: </b><br/>" +
                 " <i>Rellenar información del cliente.</i><br/>--------------------------" +
                 "</html>";
     	JLabel etiqueta = new JLabel(html);
     	ventana.getContentPane().add(etiqueta, BorderLayout.NORTH);
     	ventana.setAlwaysOnTop(true);
 		JTextField nombre = new JTextField(20);
 		JLabel nombreLabel = new JLabel("Nombre: ");
 		contenedor.add(nombreLabel);
 		contenedor.add(nombre);
 		
 		JTextField apellidos = new JTextField(20);
 		JLabel apellidosLabel = new JLabel("Apellidos: ");
 		contenedor.add(apellidosLabel);
 		contenedor.add(apellidos);
 		
 		JTextField nif = new JTextField(10);
 		JLabel nifLabel = new JLabel("NIF: ");
 		contenedor.add(nifLabel);
 		contenedor.add(nif);
 		
 		JTextField cp = new JTextField(5);
 		JLabel cpLabel = new JLabel("Código postal: ");
 		contenedor.add(cpLabel);
 		contenedor.add(cp);
 
 		JTextField poblacion = new JTextField(20);
 		JLabel poblacionLabel = new JLabel("Población: ");
 		contenedor.add(poblacionLabel);
 		contenedor.add(poblacion);
 		
 		JTextField provincia = new JTextField(20);
 		JLabel provinciaLabel = new JLabel("Provincia: ");
 		contenedor.add(provinciaLabel);
 		contenedor.add(provincia);
 		
 		JTextField email = new JTextField(20);
 		JLabel emailLabel = new JLabel("Email: ");
 		contenedor.add(emailLabel);
 		contenedor.add(email);
 		
 		//Por defecto tarifa básica
 		String html1 = "<html>" +
                 "<b>Nota: </b><br/>" +
                 " <i>Al añadir cliente, éste tiene asociada la tarifa básica.</i><br/>" +
                 "</html>";
     	JLabel etiqueta1 = new JLabel(html1);
     	ventana.getContentPane().add(etiqueta1, BorderLayout.NORTH);
     	
     	//Botón de aceptar
     	JButton ok = new JButton("Aceptar");
 
     	ok.addActionListener(new EscuchadorBotonParticularOK(ventana, op, nombre, apellidos, nif, cp, poblacion, provincia, email));
 
     	ventana.getContentPane().add(ok);
     	
    	ventana.setLocationRelativeTo(null);
		ventana.pack();
 		ventana.setSize(370, 500);
 	    ventana.setResizable(false);
 		ventana.setVisible(true);
 	}
 }
