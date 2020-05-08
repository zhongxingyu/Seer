 /*
  * Copyright (c) 2011, Julio Jiménez, René Toro, José Vargas. All rights reserved.
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  * 
  * This file is part of JTaxi.
  * 
  * JTaxi is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * JTaxi is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with JTaxi.  If not, see <http://www.gnu.org/licenses/>.
  */
 package cl.pucv.eii.jtaxi.gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JSeparator;
 
 import cl.pucv.eii.jtaxi.modelo.*;
 import cl.pucv.eii.jtaxi.utilidades.listas.ListaDoble;
 import cl.pucv.eii.jtaxi.utilidades.rut.Rut;
 
 @SuppressWarnings("serial")
 public class JTaxi extends JFrame implements ActionListener{
 	
 	private Central central;
 	private JLabel centralLabel = new JLabel();
 	private JButton tomarOrdenBoton = new JButton("Tomar orden...");
	private JButton funcion2Boton = new JButton("Funcionalidad 2");
 	private JButton manipularBoton = new JButton("Manipular Estructuras");
 	private JSeparator separador1 = new JSeparator();
 	private ListaDoble<Rut> rutsTemporales = new ListaDoble<>();
 	
 	public JTaxi(){
 		this.setLocationRelativeTo(null);
 	}
 	
 	private void initComponents(){
 		
 		tomarOrdenBoton.setActionCommand("tomarOrden");
 		tomarOrdenBoton.addActionListener(this);
 		manipularBoton.setActionCommand("manipular");
 		manipularBoton.addActionListener(this);
 		centralLabel.setText("Central: "+central.getNombre());
 		this.setTitle("JTaxi");
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(centralLabel)
                 .addContainerGap(96, Short.MAX_VALUE))
             .addComponent(separador1, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(tomarOrdenBoton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
                    .addComponent(funcion2Boton, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
                     .addComponent(manipularBoton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(centralLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(separador1, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addComponent(tomarOrdenBoton)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(funcion2Boton)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(manipularBoton)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         pack();
         this.setResizable(false);
 	}
 	
 	public void setCentral(Central c){
 		this.central = c;
 		initComponents();
 		this.setVisible(true);
 	}
 	
 	private void mostrarInspector(){
 		Inspector ins = new Inspector(this, central, rutsTemporales);
 		ins.setVisible(true);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent evt) {
 		switch(evt.getActionCommand()){
 		case "manipular":
 			mostrarInspector();
 			break;
 		case "tomarOrden":
 			tomarOrden();
 			break;
 		}
 		
 	}
 
 	private void tomarOrden() {
 		
 	}
 	
 	private int pedirInt(String mensaje, int min) {
 		String input;
 		int n = -1;
 		boolean valido = false;
 		while (!valido) {
 			input = pedirString(mensaje);
 			try {
 				n = Integer.parseInt(input);
 				if (n > min) {
 					valido = true;
 				} else {
 					mostrarDialogoError("Debe ser mayor que " + min);
 				}
 			} catch (NumberFormatException e) {
 				mostrarDialogoError("Debe ingresar un número");
 			}
 
 		}
 
 		return n;
 	}
 
 	private void mostrarDialogoError(String mensaje) {
 		JOptionPane.showMessageDialog(this, mensaje, "Error",
 				JOptionPane.ERROR_MESSAGE);
 	}
 
 	private String pedirString(String mensaje) {
 		String input = null;
 		boolean valido = false;
 		while (!valido) {
 			input = JOptionPane.showInputDialog(this, mensaje,
 					"Ingresar datos", JOptionPane.QUESTION_MESSAGE);
 			if (input != null) {
 				if (input.trim().length() > 0 && !input.matches("[\u00A0]+")) {
 					valido = true;
 				} else {
 					JOptionPane.showMessageDialog(this,
 							"No puede ingresar datos vacios.", "Error",
 							JOptionPane.ERROR_MESSAGE);
 				}
 
 			}
 		}
 		return input;
 	}
 
 	private Rut pedirRut(String mensaje) {
 		boolean valido = false;
 		String input = null;
 		Rut r = null;
 		while (!valido) {
 			input = pedirString(mensaje);
 			r = Rut.fromString(input);
 			if (r != null)
 				return r;
 			else
 				mostrarDialogoError("Ha ingresado un rut no válido o con un formato distinto a: X.XXX.XXX-X o: XX.XXX.XXX-X.");
 		}
 
 		return null;
 	}
 	
 }
