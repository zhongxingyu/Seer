 /*
  *  Chat.java
  *  Copyright (C) 2012  Diego Estévez <dgmvecuador@gmail.com>
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.educautecisystems.intefaz;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.util.ArrayList;
 import javax.swing.Timer;
 import org.educautecisystems.core.chat.cliente.ClienteServidorChat;
 import org.educautecisystems.core.chat.elements.UserChat;
 
 /**
  *
  * @author Shadow2012
  */
 public final class Chat extends javax.swing.JInternalFrame {
     private VentanaPrincipal ventanaPrincipal;
     private final StringBuffer logChat = new StringBuffer();
     private ClienteServidorChat clienteServidorChat;
     private ArrayList <UserChat> usuarios;
     private long actualSize = 0;
 	private long actualSizeListaUsuarios = 0;
     
     /**
      * Creates new form ChaPrueba
      */
     public Chat( VentanaPrincipal ventanaPrincipal ) {
         initComponents();
         this.ventanaPrincipal = ventanaPrincipal;
         clienteServidorChat = new ClienteServidorChat(this);
         activarBotones(false);
         clienteServidorChat.start();
         usuarios = null;
         Timer actualizadorChat = new Timer(500, new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 actualizarChat();
             }
         });
         actualizadorChat.start();
 		Timer actualizarListaUsuariosTimer = new Timer(2000, new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				actualizarListaUsuarios();
 			}
 		});
 		actualizarListaUsuariosTimer.start();
     }
     
     public void mostrarError( String txt ) {
         synchronized( logChat ) {
             String mensaje = "<font color=\"red\"><b>Error: </b>" + txt + "</font><br/>";
             logChat.append(mensaje);
         }
     }
     
     public void mostrarInfo( String txt ) {
         synchronized( logChat ) {
             String mensaje = "<font color=\"blue\"><b>Info: </b>" + txt + "</font><br/>";
             logChat.append(mensaje);
         }
     }
     
     public void activarBotones( boolean b ) {
         txtTexto.setEnabled(b);
         btnEnviar.setEnabled(b);
     }
     
     public void recibirMensaje ( String userIdString, String mensaje ) {
         String userName = null;
         synchronized ( this ) {
             int userId = 0;
             try {
                 userId = Integer.parseInt(userIdString);
             } catch( NumberFormatException nfe ) {
                 this.mostrarError("Id de usuario no encontrado.");
                 return;
             }
             
             /* Buascar el nombre del usuario. */
             for ( UserChat userChat:usuarios ) {
                 if ( userChat.getId() == userId ) {
                     userName = userChat.getNickName();
                 }
             }
         }
         
         synchronized ( logChat ) {
             String directorioActual = dameDiretorioActual();
             File imgs = new File(directorioActual, "img");
             File emoticon = new File(imgs, "Emoticon_sorpresa.jpg");
             if (!emoticon.exists()) {
                 System.err.println("No existe imagen.\n\t" + emoticon.getAbsolutePath());
                 return;
             }
             String regex_emoticon = emoticon.getAbsolutePath().
                     replaceAll("\\\\", "\\\\\\\\").replaceAll(":", "|");
 
             String salida = mensaje.
                     replaceAll(":o", "<img src=\"file:///" + regex_emoticon + "\"/>").
                     replaceAll("\\b(www\\.[^ ]+\\.com)\\b", "<a href=\"http://$1\">$1</a>").
                    replaceAll("\\bN[iI]ck\\b", "<b>$0</b>").
					replace(" ", "&nbsp;");
             logChat.append("<font color=\"black\"><b><i>").append(userName).append(":</i></b>&nbsp;").append(salida).append("</font><br>\n");
         }
     }
     
     private String dameDiretorioActual() {
         return System.getProperty("user.dir");
     }
     
     private void enviarMensaje() {
         synchronized ( logChat ) {
             String texto = txtTexto.getText();
             //recibirMensaje("Nick", texto);
             clienteServidorChat.enviarMensaje(texto);
             txtTexto.setText("");
         }
     }
     
     private void actualizarChat() {
         synchronized( logChat ) {
             if ( actualSize != logChat.length() ) {
                 contenidoChat.setText("<html><body>"+logChat.toString()+"</body></html>");
                 contenidoChat.setCaretPosition(contenidoChat.getDocument().getLength());
                 actualSize = logChat.length();
             }
         }
     }
 	
 	private void actualizarListaUsuarios() {
 		synchronized ( this ) {
 			StringBuilder listaUsuarios = new StringBuilder();
 			for ( UserChat usuarioChat:usuarios ) {
 				listaUsuarios.append("<font color=\"green\"><i><b>"+usuarioChat.getNickName()+"&nbsp;</b></i><font>"
 						+ "(<font color=\"blue\">"+usuarioChat.getRealName()+")</font><br/>");
 			}
 			
 			/* No hacer nada hasta que se actualice el mensaje. */
 			if ( actualSizeListaUsuarios == listaUsuarios.toString().length() ) {
 				return;
 			}
 			
 			txtListaUsuarios.setText(listaUsuarios.toString());
 			actualSizeListaUsuarios = listaUsuarios.toString().length();
 		}
 	}
     
     public void nuevaLista( ArrayList <UserChat> usuarios ) {
         synchronized( this ) {
             this.usuarios = usuarios;
         }
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jLabel1 = new javax.swing.JLabel();
         jScrollPane1 = new javax.swing.JScrollPane();
         contenidoChat = new javax.swing.JEditorPane();
         txtTexto = new javax.swing.JTextField();
         btnEnviar = new javax.swing.JButton();
         jLabel2 = new javax.swing.JLabel();
         jScrollPane3 = new javax.swing.JScrollPane();
         txtListaUsuarios = new javax.swing.JEditorPane();
 
         setClosable(true);
         setTitle("Chat");
 
         jLabel1.setText("Charla:");
 
         contenidoChat.setEditable(false);
         contenidoChat.setContentType("text/html"); // NOI18N
         jScrollPane1.setViewportView(contenidoChat);
 
         txtTexto.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 txtTextoKeyPressed(evt);
             }
         });
 
         btnEnviar.setText("Enviar");
         btnEnviar.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnEnviarActionPerformed(evt);
             }
         });
 
         jLabel2.setText("Lista de usuarios:");
 
         txtListaUsuarios.setEditable(false);
         txtListaUsuarios.setContentType("text/html"); // NOI18N
         jScrollPane3.setViewportView(txtListaUsuarios);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(txtTexto)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btnEnviar))
             .addGroup(layout.createSequentialGroup()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 388, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(layout.createSequentialGroup()
                                 .addComponent(jLabel2)
                                 .addGap(0, 225, Short.MAX_VALUE))
                             .addComponent(jScrollPane3)))
                     .addComponent(jLabel1))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(jLabel1)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(txtTexto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(btnEnviar)))
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jLabel2)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(0, 0, Short.MAX_VALUE))))
         );
 
         java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
         setBounds((screenSize.width-736)/2, (screenSize.height-482)/2, 736, 482);
     }// </editor-fold>//GEN-END:initComponents
 
     private void btnEnviarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEnviarActionPerformed
         enviarMensaje();
     }//GEN-LAST:event_btnEnviarActionPerformed
 
     private void txtTextoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtTextoKeyPressed
         if ( evt.getKeyCode() == KeyEvent.VK_ENTER ) {
             enviarMensaje();
         }
     }//GEN-LAST:event_txtTextoKeyPressed
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnEnviar;
     private javax.swing.JEditorPane contenidoChat;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JEditorPane txtListaUsuarios;
     private javax.swing.JTextField txtTexto;
     // End of variables declaration//GEN-END:variables
 }
