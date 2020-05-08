 /*  AutoXenon is a noob-friendly libxenon installer
     Copyright (C) 2011-2012  chemone
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package autoxenon.pkg;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JOptionPane;
 
 /**
  *
  * @author chemone
  */
 public class Controlador {
 
     /*Éste método ejecuta un terminal donde se instalarán los paquetes básicos
      * para la toolchain de libxenon
      */
         private Process terminal;
         private void instalarPaquetes(){
         try {
             terminal = Runtime.getRuntime().exec("gnome-terminal -x sudo apt-get install libgmp3-dev libmpfr-dev libmpc-dev texinfo git-core gettext build-essential");
         } catch (Exception ex) {
             JOptionPane.showMessageDialog(null, "Se ha producido un error el programa terminará", "Error", 
                     JOptionPane.ERROR_MESSAGE);
             System.exit(0);
         }
     }
     /*Éste método ejecuta un terminal donde se ejecutará el script con 
      * la instalación de la toolchain
      */
         private void llamarScript(){
             try {
                 terminal.waitFor();
                 terminal = Runtime.getRuntime().exec("gnome-terminal --working-directory=/opt/free60-git/toolchain -x sudo ./build-xenon-toolchain toolchain");
                 terminal.waitFor();
                 JOptionPane.showMessageDialog(null, "¡¡¡¡Enhorabuena!!!!\nSe ha terminado de instalar libxenon.\n"
                         + "Por favor, añade las dos últimas líneas de éste mensaje al fichero .bashrc,\n"
                         + "tanto del usuario root como de tu usuario normal\n"
                         + "(/home/nombredeususario/.bashrc, /root/.bashrc):\n"
                         + "export DEVKITXENON=\"/usr/local/xenon\"\n"
                         + "export PATH=\"$PATH:$DEVKITXENON/bin:$DEVKITXENON/usr/bin\"", "¡¡¡Enhorabuena!!!",
                         JOptionPane.INFORMATION_MESSAGE);
             }
             catch (Exception ex) {
                 System.out.println(ex.toString());
                 JOptionPane.showMessageDialog(null, "Se ha producido un error el programa terminará", "Error", 
                     JOptionPane.ERROR_MESSAGE);
                 System.exit(0);
             }
         }
         /*Éste método comprueba si existen los directorios y si no los crea,
          * además comprueba que exista el fichero opt y, si existe, pide al usuario
          * su nombre y le otorga derechos para leer y escribir en ellos.
          */
         private void crearDirectorios(){
             String usuario;
             File opt= new File("/opt");
             File losFicheros=new File("/opt/free60-git");
             if (opt.exists()){
                 do
                 usuario = JOptionPane.showInputDialog("Escribe tu nombre de usuario habitual:");
                 while (usuario.isEmpty());
                 try {
                         terminal = Runtime.getRuntime().exec("gnome-terminal -x sudo chown -R "+usuario+":"+usuario+
                             " /opt");
                     } 
                     catch (Exception ex) {
                     Logger.getLogger(Controlador.class.getName()).log(Level.SEVERE, null, ex);
                   }
             }
             if(!opt.exists() || !losFicheros.exists()){
                 losFicheros.mkdirs();
             }
             else if (losFicheros.exists()){
             try {
                 terminal.waitFor();
                 terminal = Runtime.getRuntime().exec("gnome-terminal -x rm -R /opt/free60-git");
                 losFicheros.mkdirs();
             } catch (Exception ex) {
                 Logger.getLogger(Controlador.class.getName()).log(Level.SEVERE, null, ex);
             }
                 
             }
         }
         /*Éste método, espera un String que representa la toolchain que se va a 
          * descargar e instalar,luego, abre un terminal y descarga los repositorios correspondientes*/
         private void descargarRepositorios(String toolchain){
             String xenon=new String();
             try {
                 if (toolchain.equalsIgnoreCase("gligli")){
                     toolchain="gligli";
                     xenon="libxenon";
                 }
 //                else if (toolchain.equalsIgnoreCase("ced2911_Experimental")){
 //                    toolchain="Ced2911";
 //                    xenon="libxenon-experimental";
 //                }
                 else if (toolchain.equalsIgnoreCase("Free60")){
                     toolchain="Free60Project";
                     xenon="libxenon";
                 }
                 terminal.waitFor();
                 crearDirectorios();
                 terminal.waitFor();
                 terminal = Runtime.getRuntime().exec("gnome-terminal -x git clone git://github.com/"+toolchain+"/"+xenon+" /opt/free60-git");
                 } catch (Exception ex) {
                         JOptionPane.showMessageDialog(null, "Se ha producido un error el programa terminará", "Error", 
                             JOptionPane.ERROR_MESSAGE);
                         System.exit(0);
                 }
         }
         /*Éste método espera un String que representa la toolchain
          * que se va a instalar y ejecuta todo el proceso de instalación de la toolchain, 
          * llamando a los métodos creados anteriormente
          */
         public void instalarToolchain(String toolchain){
             instalarPaquetes();
             descargarRepositorios(toolchain);
             llamarScript();
         }
         /*
          * Este método instala todas las librerías necesarias para libxenon, incluyendo SDL
          */
         public void instalarLibrerías(){
         try {
 //            terminal.waitFor();
             terminal = Runtime.getRuntime().exec("gnome-terminal --working-directory=/opt/free60-git/toolchain -x sudo ./build-xenon-toolchain libs");
             terminal.waitFor();
             terminal = Runtime.getRuntime().exec("gnome-terminal --working-directory=/opt/free60-git -x git clone git://github.com/lantus/libSDLXenon.git");
             terminal.waitFor();
            terminal = Runtime.getRuntime().exec("gnome-terminal --working-directory=/opt/free60-git/libSDLXenon -x sudo make -f Makefile.xenon install");
         } catch (Exception ex) {
             Logger.getLogger(Controlador.class.getName()).log(Level.SEVERE, null, ex);
         }
                 
 }
 }
