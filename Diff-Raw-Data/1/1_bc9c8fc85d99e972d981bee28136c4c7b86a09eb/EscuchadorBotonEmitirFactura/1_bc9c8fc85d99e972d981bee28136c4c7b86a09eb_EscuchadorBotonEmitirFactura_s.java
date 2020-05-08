 package gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 
 import facturacion.ExcepcionNIFnoValido;
 import facturacion.NIF;
 import facturacion.Operador;
 
 public class EscuchadorBotonEmitirFactura implements ActionListener {
 
 	JFrame ventana_paso1;
 	JTextField nif;
 	Operador op;
 	
 	public EscuchadorBotonEmitirFactura(JFrame ventana_paso1, Operador op, JTextField nif){
 		this.ventana_paso1 = ventana_paso1;
 		this.op = op;
 		this.nif = nif;
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		boolean todo_ok = true;
 		ventana_paso1.dispose();
 		NIF nif_valido = null;
 		try {
 			nif_valido = new NIF(nif.getText());
 			if(!op.getClientes().keySet().contains(nif_valido)){
 				todo_ok = false;
 			}
 		} catch (ExcepcionNIFnoValido e) {
 			todo_ok = false;
 		}
 		if(todo_ok){
 			//TODO Do stuff
 		}else{
 			JOptionPane.showMessageDialog(null, "El NIF/NIE no es de un cliente existente o no es válido.");
 		}
 	}
 
 }
