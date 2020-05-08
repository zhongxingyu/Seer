 // PENDIENTE Corregir errores con || y &&
 // PENDIENTE Expresiones de resaltado.
 // PENDIENTE Problemas con UNDO al abrir un archivo. Falla sólo en Linux?
 // PENDIENTE Agregado de palabras a la lista por idioma y en el menú Abrir.
 // PENDIENTE No sirve el resaltado de números de 1 dígito.
 import javax.swing.SwingUtilities;
 import javax.swing.KeyStroke;
 import javax.swing.ActionMap;
 import javax.swing.AbstractAction;
 import javax.swing.InputMap;
 import Clases.Lexema;
 import Clases.Parser.*;
 import Clases.NodoArbol;
 import java.awt.Color;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.swing.text.BadLocationException;
 import javax.swing.ImageIcon;
 import javax.swing.JColorChooser;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.UndoableEditEvent;
 import javax.swing.event.UndoableEditListener;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.text.DefaultHighlighter;
 import javax.swing.text.Highlighter;
 import javax.swing.undo.CannotRedoException;
 import javax.swing.undo.UndoManager;
 import Clases.Analizador;
 import Clases.Parser;
 
 public class editorVentana extends javax.swing.JFrame {
     
     public editorVentana() {
         initComponents();
     }
     public class Hilo extends Thread
     {
         @Override
         public void run() {
             while(true)
              try {
                     Thread.sleep(2000);
                     if(areaTexto.isEnabled())
                         resaltarEstructuras();
              }
                catch(InterruptedException e) {
                    JOptionPane.showMessageDialog(null, "Error en la clase Hilo");   
                    System.exit(1);
                }
         }
     }
         
     class MyDocumentListener implements DocumentListener {
         @Override
         public void insertUpdate(DocumentEvent ev) {
          if (ev.getLength() != 1) {
             return;
         }        
         int pos = ev.getOffset();
         String content = null;
         try {
             content = areaTexto.getText(0, pos + 1);
         } catch (BadLocationException e) {
             e.printStackTrace();
         }
         
         int w;
         for (w = pos; w >= 0; w--)
             if (! Character.isLetter(content.charAt(w)))
                 break;
         if (pos - w < 2)
             return;
         
         String prefix = content.substring(w + 1).toLowerCase();
         //JOptionPane.showMessageDialog(null, "[" + prefix + "]");
         int n = Collections.binarySearch(words, prefix);
         if (n < 0 && -n <= words.size()) {
             String match = words.get(-n - 1);
             if (match.startsWith(prefix)) {
                 String completion = match.substring(pos - w);
                 
                 // Aguas con esto
                 //JOptionPane.showMessageDialog(null, "Esto : " + prefix + " " + completion);
                 prefijo = prefix;                
                 SwingUtilities.invokeLater(
                         new CompletionTask(completion, pos + 1));
                 
             }
         } else 
             mode = Mode.INSERT;
         }
         @Override
         public void removeUpdate(DocumentEvent e) {
             updateLog(e, "removed from");
         }
         @Override
         public void changedUpdate(DocumentEvent e) {}        
         public void updateLog(DocumentEvent e, String action) {}
      
     }
     
     private class CompletionTask implements Runnable {
         String completion;
         int position;
         
         CompletionTask(String completion, int position) {
             this.completion = completion;
             sufijo = completion;
             this.position = position;
         }
         
         @Override
         public void run() {
             areaTexto.insert(completion, position);
             areaTexto.setCaretPosition(position + completion.length());
             areaTexto.moveCaretPosition(position);
             mode = Mode.COMPLETION;
         }
     }
     
     private class CommitAction extends AbstractAction {
         @Override
         public void actionPerformed(ActionEvent ev) {
             if (mode == Mode.COMPLETION) {
                 int pos = areaTexto.getSelectionEnd();
                 areaTexto.insert(" ", pos);
                 areaTexto.setCaretPosition(pos + 1);
                 mode = Mode.INSERT;
             } else
                 areaTexto.replaceSelection("\n");
         }
     }
     
 //  replaceButton.addActionListener(new ActionListener() {
 //      public void actionPerformed(ActionEvent evt) {
 //        String from = fromField.getText();
 //        int start = textArea.getText().indexOf(from);
 //        if (start >= 0 && from.length() > 0)
 //          textArea.replaceRange(toField.getText(), start, start
 //              + from.length());
 //      }
 //    });
         
     public void agregar(String s) throws BadLocationException {
         areaTexto.insert(s, areaTexto.getCaretPosition());
     }
     
     class MyHighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
         public MyHighlightPainter(Color color) {
             super(color);
         }
     }
     
     /* ************************************************************************ */
     public UndoManager undoManager = new UndoManager();
     Pattern pattern;
     Highlighter.HighlightPainter myHighlightPainter = new MyHighlightPainter(Color.cyan);
     Highlighter hilit;
     public int cursorPosicion = 0;
     public int y = 1;
     public String archivoAbierto = null;
     public String directorioArchivo = null;
     public static File f = null;
     Hilo negro = new Hilo(); 
     
     // Variable global que usaremos luego para pasar al analizador sintáctico.
     public static ArrayList<Lexema> palabras = null;
     String texto = "";
     boolean idioma = true;
     private static final String COMMIT_ACTION = "commit";
     private static enum Mode { INSERT, COMPLETION };
     public List<String> words = null;
     private Mode mode = Mode.INSERT;
     String prefijo = null;
     String sufijo = null;
     /************************************************************************* */
     private static boolean isNumeric(String cadena){
         try {
             Integer.parseInt(cadena);
             return true;
         } catch (NumberFormatException nfe){
             return false;
         }
     }
     
     // TODO Método para volcar todo al área de Texto.
     
     /*
     public void verTablaDeSimbolos() {
         texto = "";
         for(int i = 0; i < palabras.size(); i++)
             texto += areaTablaSimbolos.getText() + "{" + "\n" + "\tLexema: [" + 
                     palabras.get(i).getValor() + "]\n\tTipo: " + 
                     "\n\tReservado : " + Reservadas.esReservada(palabras.get(i).getValor(), idiomaCheck.isSelected() ? true : false) + "\n}\n";
         
         areaTablaSimbolos.setText(texto);
         
     }*/
     
     public void resaltarEstructuras() {
         if(idiomaCheck.isSelected()) // Español
             pattern = Pattern.compile("\\b(si|entonces|mientras|caracter|cadena|booleano|decimal|binario|romper|siguiente|analizar|caso|salir|inicio|fin|funcion|regresar|cierto|verdadero|imprimir)\\b"); 
          else
             pattern = Pattern.compile("\\b(if|then|while|int|char|string|boolean|decimal|binary|break|continue|switch|case|exit|begin|end|function|return|true|false|write)\\b"); 
         //pattern = Pattern.compile("\\b(si|entonces|write|fin|analizar|inicio|final|romper|break|salir|hacer|mientras|for)\\b"); 
        Matcher matcher = pattern.matcher(areaTexto.getText());
        Highlighter hilite = areaTexto.getHighlighter();
        Highlighter.HighlightPainter coloreado = new MyHighlightPainter(Color.cyan);
        hilite.removeAllHighlights(); // Removemos todos los anteriores subrayados...
         while( matcher.find() )
             try {
                 hilite.addHighlight(matcher.start(), matcher.end(), coloreado);
             } catch (BadLocationException ex) {
                 Logger.getLogger(editorVentana.class.getName()).log(Level.SEVERE, null, ex);
             }
         
        // Números de todo tipo
        pattern = Pattern.compile("\\b[+-]?\\d+[\\.]?(\\d+)([Ee][\\+\\-]?\\d+)?\\b");  // [+-]?
        matcher = pattern.matcher(areaTexto.getText());
        coloreado = new MyHighlightPainter(Color.orange);
        
        while( matcher.find() )
             try {
                 hilite.addHighlight(matcher.start(), matcher.end(), coloreado);
             } catch (BadLocationException ex) {
                 Logger.getLogger(editorVentana.class.getName()).log(Level.SEVERE, null, ex);
             }
        
        pattern = Pattern.compile("\".*\""); 
        matcher = pattern.matcher(areaTexto.getText());
        coloreado = new MyHighlightPainter(Color.LIGHT_GRAY);
        
        while( matcher.find() )
             try {
                 hilite.addHighlight(matcher.start(), matcher.end(), coloreado);
             } catch (BadLocationException ex) {
                 Logger.getLogger(editorVentana.class.getName()).log(Level.SEVERE, null, ex);
             }
        
        pattern = Pattern.compile("(\\+|\\-|%|\\^|\\*|\\/|\\||\\&|;|\\<=?|\\>=?|=|!=?)"); 
        matcher = pattern.matcher(areaTexto.getText());
        coloreado = new MyHighlightPainter(Color.PINK);
        while( matcher.find() )
             try {
                 hilite.addHighlight(matcher.start(), matcher.end(), coloreado);
             } catch (BadLocationException ex) {
                 Logger.getLogger(editorVentana.class.getName()).log(Level.SEVERE, null, ex);
             }
        
       pattern = Pattern.compile("(\\d)"); 
       matcher = pattern.matcher(areaTexto.getText());
       coloreado = new MyHighlightPainter(Color.orange);
       while( matcher.find() )
            try {
                hilite.addHighlight(matcher.start(), matcher.end(), coloreado);
            } catch (BadLocationException ex) {
                Logger.getLogger(editorVentana.class.getName()).log(Level.SEVERE, null, ex);
            }
       
        pattern = Pattern.compile("(#[\\w\\n\\s\\W\\d\\D\\.]*#)", Pattern.MULTILINE); 
        matcher = pattern.matcher(areaTexto.getText());
        coloreado = new MyHighlightPainter(Color.YELLOW);
        while( matcher.find() )
             try {
                 hilite.addHighlight(matcher.start(), matcher.end(), coloreado);
             } catch (BadLocationException ex) {
                 Logger.getLogger(editorVentana.class.getName()).log(Level.SEVERE, null, ex);
             }
        
     }
     
     // Función para guardar en un archivo, devuelve true si se hizo el guardado,
     // false si no se hizo el guardado...
     public boolean guardarEnArchivo() {
         boolean devuelto = false;
         int estado = selectorDeArchivos.showSaveDialog(this);
         if(estado == JFileChooser.APPROVE_OPTION) {
             f = selectorDeArchivos.getSelectedFile();
             archivoAbierto = f.getName();
             directorioArchivo = f.getParent();
             BufferedWriter bw;
             
             try {
             
                 bw = new BufferedWriter(new FileWriter(f));
                 f = selectorDeArchivos.getSelectedFile();
                 areaTexto.write(bw);
                 bw.close();
                 
             } catch (IOException ex) {
                 Logger.getLogger(editorVentana.class.getName()).log(Level.SEVERE, null, ex);
             }
             
             devuelto = true;
             
         } else 
             devuelto = false;
         
         return devuelto;
     }
     
     public void guardarSinAvisar() {
             
             f = selectorDeArchivos.getSelectedFile();
             archivoAbierto = f.getName();
             directorioArchivo = f.getParent();
             BufferedWriter bw;
             
             try {
             
                 bw = new BufferedWriter(new FileWriter(f));
                 f = selectorDeArchivos.getSelectedFile();
                 areaTexto.write(bw);
                 bw.close();
                 
             } catch (IOException ex) {
                 Logger.getLogger(editorVentana.class.getName()).log(Level.SEVERE, null, ex);
             }
     }
     
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         selectorDeArchivos = new javax.swing.JFileChooser();
         aboutDialog = new javax.swing.JDialog();
         jLabel1 = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         jLabel3 = new javax.swing.JLabel();
         jLabel4 = new javax.swing.JLabel();
         jLabel5 = new javax.swing.JLabel();
         jLabel6 = new javax.swing.JLabel();
         cerrarBtnDialog = new javax.swing.JButton();
         jLabel7 = new javax.swing.JLabel();
         popup = new javax.swing.JPopupMenu();
         seleccionarTodo = new javax.swing.JMenuItem();
         verDatosMenuItem = new javax.swing.JMenuItem();
         colorArea = new javax.swing.JColorChooser();
         dialogoColor = new javax.swing.JDialog();
         ayudaPopup = new javax.swing.JDialog();
         ayudaLabel = new javax.swing.JLabel();
         tabSizeDialog = new javax.swing.JDialog();
         jLabel8 = new javax.swing.JLabel();
         tabSizeTxt = new javax.swing.JTextField();
         aceptarTabBtn = new javax.swing.JButton();
         estadoDeLinea = new javax.swing.JLabel();
         jSeparator1 = new javax.swing.JSeparator();
         jScrollPane1 = new javax.swing.JScrollPane();
         areaTexto = new javax.swing.JTextArea();
         jScrollPane2 = new javax.swing.JScrollPane();
         jTextArea1 = new javax.swing.JTextArea();
         idiomaCheck = new javax.swing.JCheckBox();
         replaceBtn = new javax.swing.JButton();
         menu = new javax.swing.JMenuBar();
         menuArchivo = new javax.swing.JMenu();
         nuevoMenuItem = new javax.swing.JMenuItem();
         abrirMenuItem = new javax.swing.JMenuItem();
         guardarMenuItem = new javax.swing.JMenuItem();
         guardarComoMenuItem = new javax.swing.JMenuItem();
         cerrarArchivoMenuItem = new javax.swing.JMenuItem();
         imprimirMenuItem = new javax.swing.JMenuItem();
         salirMenuItem = new javax.swing.JMenuItem();
         edicionMenu = new javax.swing.JMenu();
         copiarMenuItem = new javax.swing.JMenuItem();
         jMenuItem1 = new javax.swing.JMenuItem();
         deshacerBtn = new javax.swing.JMenuItem();
         rehacerMenuItem = new javax.swing.JMenuItem();
         cortarMenuItem = new javax.swing.JMenuItem();
         selectAllMenuItem = new javax.swing.JMenuItem();
         irAMenuItem = new javax.swing.JMenuItem();
         jMenu1 = new javax.swing.JMenu();
         forCodeMenuItem = new javax.swing.JMenuItem();
         whileCodeMenuItem = new javax.swing.JMenuItem();
         menuFormato = new javax.swing.JMenu();
         cambiarTipoLetra = new javax.swing.JMenuItem();
         colorFuenteMenuItem = new javax.swing.JMenuItem();
         cambiarColorMenuItem = new javax.swing.JMenuItem();
         compilarMenu = new javax.swing.JMenu();
         compilarMenuItem = new javax.swing.JMenuItem();
         syntaxMenuItem = new javax.swing.JMenuItem();
         configuracionMenu = new javax.swing.JMenu();
         idiomaMenuItem = new javax.swing.JMenu();
         espanolMenuItem = new javax.swing.JMenuItem();
         inglesMenuItem = new javax.swing.JMenuItem();
         tabSizeMenuItem = new javax.swing.JMenuItem();
         ayudaMenu = new javax.swing.JMenu();
         aboutMenuItem = new javax.swing.JMenuItem();
 
         selectorDeArchivos.setToolTipText("Elija donde guardar su archivo");
         selectorDeArchivos.setName(""); // NOI18N
 
         aboutDialog.setTitle("Acerca de GLHBR v1.0");
 
         jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11));
         jLabel1.setText("Hecho por:");
 
         jLabel2.setText("Leonardo Gutiérrez Ramírez - leogutierrezramirez@gmail.com");
 
         jLabel3.setText("Yareli Adriana Beltrán Maldonado - yareliitha_beltran01@hotmail.com");
 
         jLabel4.setText("Edgar Gerardo Hernández Altamirano - edgar-371@hotmail.com");
 
         jLabel5.setText("Mariela Carolina Lozoya Armendariz - pink_campante@hotmail.com ");
 
         jLabel6.setText("Manuel Alejandro Ramírez Hernández - gaara_alex@hotmail.com");
 
         cerrarBtnDialog.setMnemonic('C');
         cerrarBtnDialog.setText("Cerrar");
         cerrarBtnDialog.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cerrarBtnDialogActionPerformed(evt);
             }
         });
 
         jLabel7.setForeground(new java.awt.Color(51, 51, 255));
         jLabel7.setText("Instituto Tecnológico de Chihuahua II");
 
         javax.swing.GroupLayout aboutDialogLayout = new javax.swing.GroupLayout(aboutDialog.getContentPane());
         aboutDialog.getContentPane().setLayout(aboutDialogLayout);
         aboutDialogLayout.setHorizontalGroup(
             aboutDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(aboutDialogLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(aboutDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(aboutDialogLayout.createSequentialGroup()
                         .addGroup(aboutDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(jLabel5)
                             .addComponent(jLabel6)
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, aboutDialogLayout.createSequentialGroup()
                                 .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                 .addComponent(cerrarBtnDialog))
                             .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                         .addContainerGap())))
         );
         aboutDialogLayout.setVerticalGroup(
             aboutDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(aboutDialogLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel1)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jLabel2)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jLabel3)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jLabel4)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jLabel5)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jLabel6)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(aboutDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(cerrarBtnDialog)
                     .addComponent(jLabel7))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         seleccionarTodo.setText("Seleccionar todo");
         seleccionarTodo.setName("Lala"); // NOI18N
         seleccionarTodo.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 seleccionarTodoActionPerformed(evt);
             }
         });
         popup.add(seleccionarTodo);
 
         verDatosMenuItem.setText("Ver datos de archivo");
         verDatosMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 verDatosMenuItemActionPerformed(evt);
             }
         });
         popup.add(verDatosMenuItem);
 
         colorArea.setToolTipText("Seleccione el color del área de texto");
         colorArea.getAccessibleContext().setAccessibleParent(this);
 
         dialogoColor.setTitle("Elija el color");
 
         javax.swing.GroupLayout dialogoColorLayout = new javax.swing.GroupLayout(dialogoColor.getContentPane());
         dialogoColor.getContentPane().setLayout(dialogoColorLayout);
         dialogoColorLayout.setHorizontalGroup(
             dialogoColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 400, Short.MAX_VALUE)
         );
         dialogoColorLayout.setVerticalGroup(
             dialogoColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 300, Short.MAX_VALUE)
         );
 
         javax.swing.GroupLayout ayudaPopupLayout = new javax.swing.GroupLayout(ayudaPopup.getContentPane());
         ayudaPopup.getContentPane().setLayout(ayudaPopupLayout);
         ayudaPopupLayout.setHorizontalGroup(
             ayudaPopupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(ayudaPopupLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(ayudaLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(108, Short.MAX_VALUE))
         );
         ayudaPopupLayout.setVerticalGroup(
             ayudaPopupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(ayudaPopupLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(ayudaLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         tabSizeDialog.setTitle("Tab size");
 
         jLabel8.setDisplayedMnemonic('T');
         jLabel8.setLabelFor(tabSizeTxt);
         jLabel8.setText("Tamaño");
 
         aceptarTabBtn.setMnemonic('A');
         aceptarTabBtn.setText("Aceptar");
         aceptarTabBtn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 aceptarTabBtnActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout tabSizeDialogLayout = new javax.swing.GroupLayout(tabSizeDialog.getContentPane());
         tabSizeDialog.getContentPane().setLayout(tabSizeDialogLayout);
         tabSizeDialogLayout.setHorizontalGroup(
             tabSizeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(tabSizeDialogLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(tabSizeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(tabSizeDialogLayout.createSequentialGroup()
                         .addComponent(jLabel8)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(tabSizeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(aceptarTabBtn))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         tabSizeDialogLayout.setVerticalGroup(
             tabSizeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(tabSizeDialogLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(tabSizeDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel8)
                     .addComponent(tabSizeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(aceptarTabBtn)
                 .addContainerGap())
         );
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("GLHBR");
         setBackground(new java.awt.Color(251, 222, 194));
         setForeground(new java.awt.Color(235, 150, 150));
         setName("ventanaPrincipal"); // NOI18N
         setResizable(false);
         addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowClosing(java.awt.event.WindowEvent evt) {
                 alCerrar(evt);
             }
             public void windowClosed(java.awt.event.WindowEvent evt) {
                 alCerrar(evt);
             }
         });
 
         estadoDeLinea.setText("Abra o cree un nuevo archivo");
 
         areaTexto.setColumns(20);
         areaTexto.setFont(new java.awt.Font("FreeMono", 1, 13));
         areaTexto.setRows(5);
         areaTexto.setTabSize(2);
         areaTexto.setToolTipText("Ingrese su texto");
         areaTexto.setWrapStyleWord(true);
         areaTexto.setCaretColor(new java.awt.Color(255, 0, 0));
         areaTexto.setComponentPopupMenu(popup);
         areaTexto.setDragEnabled(true);
         areaTexto.setEnabled(false);
         areaTexto.setInheritsPopupMenu(true);
         areaTexto.addCaretListener(new javax.swing.event.CaretListener() {
             public void caretUpdate(javax.swing.event.CaretEvent evt) {
                 areaTextoCaretUpdate(evt);
             }
         });
         areaTexto.addInputMethodListener(new java.awt.event.InputMethodListener() {
             public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
             }
             public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
                 areaTextoCaretPositionChanged(evt);
             }
         });
         areaTexto.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 areaTextoKeyPressed(evt);
             }
         });
         jScrollPane1.setViewportView(areaTexto);
 
         jTextArea1.setColumns(20);
         jTextArea1.setRows(5);
         jScrollPane2.setViewportView(jTextArea1);
 
         idiomaCheck.setSelected(true);
         idiomaCheck.setText("Español");
         idiomaCheck.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 idiomaCheckActionPerformed(evt);
             }
         });
 
         replaceBtn.setText("Reemplazo");
         replaceBtn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 replaceBtnActionPerformed(evt);
             }
         });
 
         menu.setToolTipText("Diferentes acciones a realizar con los archivos");
 
         menuArchivo.setMnemonic('A');
         menuArchivo.setText("Archivo");
 
         nuevoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
         nuevoMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/tp_new.png"))); // NOI18N
         nuevoMenuItem.setMnemonic('N');
         nuevoMenuItem.setText("Nuevo");
         nuevoMenuItem.setName("nuevoMenuItem"); // NOI18N
         nuevoMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 nuevoMenuItemActionPerformed(evt);
             }
         });
         menuArchivo.add(nuevoMenuItem);
 
         abrirMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
         abrirMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/tp_open.png"))); // NOI18N
         abrirMenuItem.setMnemonic('A');
         abrirMenuItem.setText("Abrir");
         abrirMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 abrirMenuItemActionPerformed(evt);
             }
         });
         menuArchivo.add(abrirMenuItem);
 
         guardarMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
         guardarMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/tp_save.png"))); // NOI18N
         guardarMenuItem.setMnemonic('G');
         guardarMenuItem.setText("Guardar");
         guardarMenuItem.setEnabled(false);
         guardarMenuItem.setName("guardarMenuItem"); // NOI18N
         guardarMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 guardarMenuItemActionPerformed(evt);
             }
         });
         menuArchivo.add(guardarMenuItem);
 
         guardarComoMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
         guardarComoMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/tp_saveas.png"))); // NOI18N
         guardarComoMenuItem.setText("Guardar como ...");
         guardarComoMenuItem.setEnabled(false);
         guardarComoMenuItem.setName("guardarComoMenuItem"); // NOI18N
         guardarComoMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 guardarComoMenuItemActionPerformed(evt);
             }
         });
         menuArchivo.add(guardarComoMenuItem);
 
         cerrarArchivoMenuItem.setMnemonic('C');
         cerrarArchivoMenuItem.setText("Cerrar archivo");
         cerrarArchivoMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cerrarArchivoMenuItemActionPerformed(evt);
             }
         });
         menuArchivo.add(cerrarArchivoMenuItem);
 
         imprimirMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
         imprimirMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/tp_print.png"))); // NOI18N
         imprimirMenuItem.setText("Imprimir");
         imprimirMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 imprimirMenuItemActionPerformed(evt);
             }
         });
         menuArchivo.add(imprimirMenuItem);
 
         salirMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
         salirMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/exit.png"))); // NOI18N
         salirMenuItem.setMnemonic('S');
         salirMenuItem.setText("Salir");
         salirMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 salirMenuItemActionPerformed(evt);
             }
         });
         menuArchivo.add(salirMenuItem);
 
         menu.add(menuArchivo);
 
         edicionMenu.setMnemonic('E');
         edicionMenu.setText("Edición");
 
         copiarMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
         copiarMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/tp_copy.png"))); // NOI18N
         copiarMenuItem.setMnemonic('C');
         copiarMenuItem.setText("Copiar");
         copiarMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 copiarMenuItemActionPerformed(evt);
             }
         });
         edicionMenu.add(copiarMenuItem);
 
         jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
         jMenuItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/tp_paste.png"))); // NOI18N
         jMenuItem1.setText("Pegar");
         edicionMenu.add(jMenuItem1);
 
         deshacerBtn.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
         deshacerBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/tp_undo.png"))); // NOI18N
         deshacerBtn.setText("Deshacer");
         deshacerBtn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 deshacerBtnActionPerformed(evt);
             }
         });
         edicionMenu.add(deshacerBtn);
 
         rehacerMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
         rehacerMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/tp_redo.png"))); // NOI18N
         rehacerMenuItem.setMnemonic('R');
         rehacerMenuItem.setText("Rehacer");
         rehacerMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 rehacerMenuItemActionPerformed(evt);
             }
         });
         edicionMenu.add(rehacerMenuItem);
 
         cortarMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
         cortarMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/tp_cut.png"))); // NOI18N
         cortarMenuItem.setText("Cortar");
         edicionMenu.add(cortarMenuItem);
 
         selectAllMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
         selectAllMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/edit-select-all.png"))); // NOI18N
         selectAllMenuItem.setText("Seleccionar todo");
         selectAllMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 selectAllMenuItemActionPerformed(evt);
             }
         });
         edicionMenu.add(selectAllMenuItem);
 
         irAMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK));
         irAMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/goto_icon_30.png"))); // NOI18N
         irAMenuItem.setMnemonic('I');
         irAMenuItem.setText("Ir a...");
         irAMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 irAMenuItemActionPerformed(evt);
             }
         });
         edicionMenu.add(irAMenuItem);
 
         jMenu1.setText("Código");
 
         forCodeMenuItem.setText("for(..., ..., ...)");
         forCodeMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 forCodeMenuItemActionPerformed(evt);
             }
         });
         jMenu1.add(forCodeMenuItem);
 
         whileCodeMenuItem.setText("while(...)");
         whileCodeMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 whileCodeMenuItemActionPerformed(evt);
             }
         });
         jMenu1.add(whileCodeMenuItem);
 
         edicionMenu.add(jMenu1);
 
         menu.add(edicionMenu);
 
         menuFormato.setMnemonic('F');
         menuFormato.setText("Formato");
         menuFormato.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menuFormatoActionPerformed(evt);
             }
         });
 
         cambiarTipoLetra.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
         cambiarTipoLetra.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/fuente.png"))); // NOI18N
         cambiarTipoLetra.setMnemonic('f');
         cambiarTipoLetra.setText("Cambiar fuente");
         cambiarTipoLetra.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cambiarTipoLetraActionPerformed(evt);
             }
         });
         menuFormato.add(cambiarTipoLetra);
 
         colorFuenteMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/font_color_icon.jpg"))); // NOI18N
         colorFuenteMenuItem.setText("Cambiar color de fuente");
         colorFuenteMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 colorFuenteMenuItemActionPerformed(evt);
             }
         });
         menuFormato.add(colorFuenteMenuItem);
 
         cambiarColorMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/backcolor.png"))); // NOI18N
         cambiarColorMenuItem.setText("Cambiar color de fondo");
         cambiarColorMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cambiarColorMenuItemActionPerformed(evt);
             }
         });
         menuFormato.add(cambiarColorMenuItem);
 
         menu.add(menuFormato);
 
         compilarMenu.setMnemonic('C');
         compilarMenu.setText("Compilar");
 
         compilarMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
         compilarMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/run.png"))); // NOI18N
         compilarMenuItem.setMnemonic('C');
         compilarMenuItem.setText("Compilar");
         compilarMenuItem.setToolTipText("Compile su código fuente");
         compilarMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 compilarMenuItemActionPerformed(evt);
             }
         });
         compilarMenu.add(compilarMenuItem);
 
         syntaxMenuItem.setText("Sintáctico");
         syntaxMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 syntaxMenuItemActionPerformed(evt);
             }
         });
         compilarMenu.add(syntaxMenuItem);
 
         menu.add(compilarMenu);
 
         configuracionMenu.setMnemonic('n');
         configuracionMenu.setText("Configuración");
 
         idiomaMenuItem.setMnemonic('I');
         idiomaMenuItem.setText("Idioma");
 
         espanolMenuItem.setMnemonic('E');
         espanolMenuItem.setText("Español");
         espanolMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 espanolMenuItemActionPerformed(evt);
             }
         });
         idiomaMenuItem.add(espanolMenuItem);
 
         inglesMenuItem.setMnemonic('n');
         inglesMenuItem.setText("Inglés");
         inglesMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 inglesMenuItemActionPerformed(evt);
             }
         });
         idiomaMenuItem.add(inglesMenuItem);
 
         configuracionMenu.add(idiomaMenuItem);
 
         tabSizeMenuItem.setText("Tamaño tabulador");
         tabSizeMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 tabSizeMenuItemActionPerformed(evt);
             }
         });
         configuracionMenu.add(tabSizeMenuItem);
 
         menu.add(configuracionMenu);
 
         ayudaMenu.setMnemonic('u');
         ayudaMenu.setText("Ayuda");
 
         aboutMenuItem.setMnemonic('A');
         aboutMenuItem.setText("Acerca de...");
         aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 aboutMenuItemActionPerformed(evt);
             }
         });
         ayudaMenu.add(aboutMenuItem);
 
         menu.add(ayudaMenu);
 
         setJMenuBar(menu);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 668, Short.MAX_VALUE)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(replaceBtn)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 487, Short.MAX_VALUE)
                 .addComponent(idiomaCheck)
                 .addContainerGap())
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 644, Short.MAX_VALUE)
                 .addContainerGap())
             .addGroup(layout.createSequentialGroup()
                 .addComponent(estadoDeLinea, javax.swing.GroupLayout.PREFERRED_SIZE, 603, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(65, Short.MAX_VALUE))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addGap(15, 15, 15)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(idiomaCheck)
                     .addComponent(replaceBtn))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 620, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 5, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(estadoDeLinea))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
 private void nuevoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nuevoMenuItemActionPerformed
 
         areaTexto.getDocument().addUndoableEditListener(new UndoableEditListener() {
             @Override public void undoableEditHappened(UndoableEditEvent e) {
             undoManager.addEdit(e.getEdit());
           }
         });
     
         areaTexto.getDocument().addDocumentListener(new MyDocumentListener());
         InputMap im = areaTexto.getInputMap();
         ActionMap am = areaTexto.getActionMap();
         im.put(KeyStroke.getKeyStroke("ENTER"), COMMIT_ACTION);
         am.put(COMMIT_ACTION, new CommitAction());
         
         // PENDING Agregado de palabras a resaltar por idioma.
         words = new ArrayList<String>();
         words.add("dobles");
         words.add("entero");
         words.add("finals");
         words.add("inicio");
         words.add("mientras");
         
         deshacerBtn.setEnabled(true);
         rehacerMenuItem.setEnabled(true);
         
         areaTexto.requestFocusInWindow();
         
         ImageIcon icono = new ImageIcon(getClass().getResource("/icons/tp_new.png"));
         compilarMenuItem.setIcon(icono);
         areaTexto.setComponentPopupMenu(popup);
         negro.start();
         
      // Si el usuario al estar editando un archivo, quiere crear un archivo Nuevo...
     // El archivo se debe guardar antes....
     
     // Ha dado clic en nuevo...
     // Así que habilitamos los botones guardar, guardarComo e Imprimir.
     
     // Checar si el area de texto está habilitada..., sino, quiere decir que no hay un archivo
     // Abierto o dió
     
     // Ya dió nuevo pero no ha guardado...
     // Así que hay que preguntar si quiere guardar...
     areaTexto.setTabSize(2);
     if(areaTexto.isEnabled() && (archivoAbierto == null) && (directorioArchivo == null)) {
         // Mandamos guardar..., pero como se supone que ya es uno nuevo
         // Desasignamos las variables archivoAbierto y directorioArchivo
         guardarEnArchivo();
         archivoAbierto = null;
         directorioArchivo = null;
         // Limpiamos la caja de texto, puesto que ya estamos en otro archivo.
         areaTexto.setText("");
         
         return; // Salimos.
         // Ya hay un archivo asignado, primero guardamos lo que ya haya...
     } else if((archivoAbierto != null) && (directorioArchivo != null)) {
         guardarSinAvisar();
         archivoAbierto = null;
         directorioArchivo = null;
         areaTexto.setText("");
     }
     
     setTitle("GLHBR v1.0 - Nuevo archivo");
     estadoDeLinea.setText("Línea: ");
     guardarMenuItem.setEnabled(true);
     guardarComoMenuItem.setEnabled(true);
     //imprimirMenuItem.setEnabled(true);
     
     // Habilitar el area de texto:
     areaTexto.setEnabled(true);
     areaTexto.setEditable(true);
     areaTexto.add(popup);
     cerrarArchivoMenuItem.setEnabled(true);
     
     archivoAbierto = null;
     directorioArchivo = null;
     
 }//GEN-LAST:event_nuevoMenuItemActionPerformed
 
 private void abrirMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abrirMenuItemActionPerformed
     
     FileNameExtensionFilter filter = new FileNameExtensionFilter("GIF", "glhbr");
     selectorDeArchivos.setFileFilter(new FileNameExtensionFilter("GLHBR", "glhbr"));
     selectorDeArchivos.setFileSelectionMode(JFileChooser.FILES_ONLY);
     
     // Estamos abriendo un archivo desde 0
     areaTexto.getDocument().addUndoableEditListener(new UndoableEditListener() {
             @Override public void undoableEditHappened(UndoableEditEvent e) {
             undoManager.addEdit(e.getEdit());
           }
     });
     
     areaTexto.getDocument().addDocumentListener(new MyDocumentListener());
     
     if((archivoAbierto == null) && (directorioArchivo == null)) {
         // Quiere decir que ya ha tecleado algo...
         // Por lo tanto hay que llamar al dialogo Guardar...
         if(areaTexto.getText().isEmpty() == false)
             guardarEnArchivo();
         // TODO Llamar a el dialogo de abrir...
          int state = selectorDeArchivos.showOpenDialog(this);
 
          if(state == JFileChooser.APPROVE_OPTION) {    //si elige abrir el archivo
             f = selectorDeArchivos.getSelectedFile();    //obtiene el archivo seleccionado
          try {
             //abre un flujo de datos desde el archivo seleccionado
             BufferedReader br = new BufferedReader(new FileReader(f));
             areaTexto.read(br, null);    //lee desde el flujo de datos hacia el area de edición
             br.close();
 
             //asigna el manejador de eventos para registrar los cambios en el nuevo documento actual
             setTitle("GLHBR - " + f.getName());
 
             archivoAbierto = f.getName();
             directorioArchivo = f.getParent();
             
             } catch (IOException ex) { 
                 JOptionPane.showMessageDialog(this,ex.getMessage(),ex.toString(),JOptionPane.ERROR_MESSAGE);
             }
         
             negro.start();
             areaTexto.setEnabled(true);
             guardarMenuItem.setEnabled(true);
             guardarComoMenuItem.setEnabled(true);
         
         }
          
         // Quiere decir que estamos abriendo otro archivo desde uno que ya estemos editando
         // Entonces primero hay que guardar lo que ya pusimos en el area de texto.
         // Y luego presentar el diálogo para que elija el archivo.
     } else {
         
             f = selectorDeArchivos.getSelectedFile();
             
             archivoAbierto = f.getName();
             directorioArchivo = f.getParent();
             
             try {
                 BufferedWriter bw = new BufferedWriter(new FileWriter(f));
                 areaTexto.write(bw);
                 bw.close();
                 
                 setTitle(f.getName());
                 
                 archivoAbierto = null;
                 directorioArchivo = null;
                 
          int state = selectorDeArchivos.showOpenDialog(this);
          if(state == JFileChooser.APPROVE_OPTION) {    //si elige abrir el archivo
             f = selectorDeArchivos.getSelectedFile();    //obtiene el archivo seleccionado
             try {
             BufferedReader br = new BufferedReader(new FileReader(f));
             areaTexto.read(br, null);    //lee desde el flujo de datos hacia el area de edición
             br.close();
 
             //asigna el manejador de eventos para registrar los cambios en el nuevo documento actual
             setTitle("GLHBR - " + f.getName());
 
             archivoAbierto = f.getName();
             directorioArchivo = f.getParent();
             
             } catch (IOException ex) {    //en caso de que ocurra una excepción
                 JOptionPane.showMessageDialog(this,ex.getMessage(),ex.toString(),JOptionPane.ERROR_MESSAGE);
             }
         
             areaTexto.setEnabled(true);
             guardarMenuItem.setEnabled(true);
             guardarComoMenuItem.setEnabled(true);
         }
             } catch(IOException ex) {
                 JOptionPane.showMessageDialog(this,ex.getMessage(),ex.toString(),JOptionPane.ERROR_MESSAGE);
             }
         }
         
 }//GEN-LAST:event_abrirMenuItemActionPerformed
 
 private void guardarMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guardarMenuItemActionPerformed
     
     if(areaTexto.isEnabled() == false)
         return;
     
     // Aquí quiere decir que ya tiene el area de texto habilitada, así que mandamos llamar al File Chooser...
     // Igual a Guardar como... puesto que es la primera vez que se da Guardar...
     
     // En caso de que no tengamos nombres de archivos asignados...
     // Significa que el archivo es guardado por primera vez.
     if(archivoAbierto == null && directorioArchivo == null) {
         
          guardarEnArchivo();
          setTitle(archivoAbierto);
         // Sino solo guardamos...
     } else {
         f = selectorDeArchivos.getSelectedFile();
         BufferedWriter bw;
             try {
                 
                 bw = new BufferedWriter(new FileWriter(f));
                 areaTexto.write(bw);
                 bw.close();
                 
             } catch (IOException ex) {
                 Logger.getLogger(editorVentana.class.getName()).log(Level.SEVERE, null, ex);
             }
         
         this.setTitle("GLHBR v1.0 - " + f.getName());
         
     } 
     
 }//GEN-LAST:event_guardarMenuItemActionPerformed
 
 private void guardarComoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guardarComoMenuItemActionPerformed
     
     guardarEnArchivo();
     setTitle(archivoAbierto);
 }//GEN-LAST:event_guardarComoMenuItemActionPerformed
 
 private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
 
    Image icon = new ImageIcon(getClass().getResource("/icons/aboutDialog.png")).getImage();
    aboutDialog.setIconImage(icon);
    this.setVisible(false);
    aboutDialog.setModal(true);
    aboutDialog.setSize(410, 220);
    aboutDialog.setResizable(false);
    aboutDialog.setVisible(true);
    this.setVisible(true);
     
 }//GEN-LAST:event_aboutMenuItemActionPerformed
 
 private void salirMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_salirMenuItemActionPerformed
     // Quiere decir que el usuario eligió salir sin estar editando un archivo
     // Salimos y ya, sin ningún problema.
     if((archivoAbierto == null) && (directorioArchivo == null))
         System.exit(0);
     else {
         int option = JOptionPane.showConfirmDialog(this, "¿Desea guardar los cambios?");
         
         switch (option) {
             case JOptionPane.YES_OPTION:
                 
                 int state = selectorDeArchivos.showSaveDialog(this);
         
         if(state == JFileChooser.APPROVE_OPTION) {    //si elige guardar en el archivo
             f = selectorDeArchivos.getSelectedFile();    //se obtiene el archivo seleccionado   
             archivoAbierto = f.getName();
             directorioArchivo = f.getParent();
             
             try {
                 BufferedWriter bw = new BufferedWriter(new FileWriter(f));
                 areaTexto.write(bw);
                 bw.close();
                 
                 System.exit(0);
                 
             } catch(IOException ex) { 
                 JOptionPane.showMessageDialog(this,ex.getMessage(),ex.toString(),JOptionPane.ERROR_MESSAGE);
             }
         }
                 
                 break;
             case JOptionPane.CANCEL_OPTION:
                 System.exit(0);             //se cancela esta operación
             case JOptionPane.NO_OPTION:
                 System.exit(0);
             
         }
 
     }
     
     if(negro.isAlive())
         negro.stop();
     
 }//GEN-LAST:event_salirMenuItemActionPerformed
 
 private void areaTextoCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_areaTextoCaretUpdate
     
        cursorPosicion = areaTexto.getCaretPosition();
         try {
             y = areaTexto.getLineOfOffset(cursorPosicion) + 1;
        } catch (BadLocationException ex) {
            Logger.getLogger(editorVentana.class.getName()).log(Level.SEVERE, null, ex);
        }
         
         if(y == 0)
             ++y;
         
         if(areaTexto.isEnabled() == true)
            estadoDeLinea.setText("Línea: " + y);
         
 }//GEN-LAST:event_areaTextoCaretUpdate
 
 private void areaTextoCaretPositionChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_areaTextoCaretPositionChanged
 }//GEN-LAST:event_areaTextoCaretPositionChanged
 
 private void areaTextoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_areaTextoKeyPressed
 }//GEN-LAST:event_areaTextoKeyPressed
 
 private void cerrarArchivoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cerrarArchivoMenuItemActionPerformed
     if(negro.isAlive() == true)
         negro.stop();
     // Quiere decir que el wey quiere cerrar algo y ni siquiera ha escrito algo
     // o está en un archivo...
     if((archivoAbierto == null) && (directorioArchivo == null) && areaTexto.getText().isEmpty())
       return;
     
     // Quiere decir que quiere cerrar el archivo pero ya ha escrito algo paneText.getText().isEmpty...
     // Guardamos y cerramos....
     else if((archivoAbierto == null) && (directorioArchivo == null) && !areaTexto.getText().isEmpty()) {
        //presenta un dialogo modal para que el usuario seleccione un archivo
         selectorDeArchivos.setDialogTitle("Guarde el archivo antes de cerrar ...");
         int state = selectorDeArchivos.showSaveDialog(this);
         
         if (state == JFileChooser.APPROVE_OPTION) {    
             // Obtenemos ciertas propiedades del archivo elegido, nombre y carpeta.
             f = selectorDeArchivos.getSelectedFile();    //se obtiene el archivo seleccionado
             // Primero guardamos en el archivo
             archivoAbierto = null;
             directorioArchivo = null;
             
             try {
                 //abre un flujo de datos hacia el archivo asociado seleccionado
                 BufferedWriter bw = new BufferedWriter(new FileWriter(f));
                 //escribe desde el flujo de datos hacia el archivo
                 areaTexto.write(bw);
                 bw.close();
                 
                 // Como eligió cerrar, volvemos a deshabilitar el area de texto y las opciones...
                 areaTexto.setEditable(false);
                 areaTexto.setEditable(false);
                 guardarComoMenuItem.setEnabled(false);
                 guardarMenuItem.setEnabled(false);
                 imprimirMenuItem.setEnabled(false);
                 cerrarArchivoMenuItem.setEnabled(false);
                 //areaTexto.setEnabled(false);
                 areaTexto.setText("");
                 estadoDeLinea.setText("Abra o cree un nuevo archivo");
                 
             } catch(IOException ex) {
                 JOptionPane.showMessageDialog(this,ex.getMessage(),ex.toString(),JOptionPane.ERROR_MESSAGE);
             }
         }
         // Quiere decir que el wey ya dió Guardar... y ya hay un archivo asignado...
         // Así que hay que guardar de nuevo...
     } else if((archivoAbierto != null) && (directorioArchivo != null)) {
         f = selectorDeArchivos.getSelectedFile();
         BufferedWriter bw;
             try {
                 
                 bw = new BufferedWriter(new FileWriter(f));
                 areaTexto.write(bw);
                 bw.close();
                 
             } catch (IOException ex) {
                 Logger.getLogger(editorVentana.class.getName()).log(Level.SEVERE, null, ex);
             }
             // Como eligió cerrar, volvemos a deshabilitar el area de texto y las opciones...
                 areaTexto.setEditable(false);
 //                areaTexto.setEnabled(false);
 //                guardarComoMenuItem.setEnabled(false);
 //                guardarMenuItem.setEnabled(false);
 //                imprimirMenuItem.setEnabled(false);
                 cerrarArchivoMenuItem.setEnabled(false);
                 areaTexto.setText("");
                 estadoDeLinea.setText("Abra o cree un nuevo archivo");
     }
     
     archivoAbierto = null;
     directorioArchivo = null;
     
 }//GEN-LAST:event_cerrarArchivoMenuItemActionPerformed
 
 private void alCerrar(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_alCerrar
     
     if(negro.isAlive())
         negro.stop();
     
     if((archivoAbierto != null) && (directorioArchivo != null)) {
         Object[] options = {"Sí", "No"};
         int n = JOptionPane.showOptionDialog(this, "Desea guardar los cambios ?",
                 "Guardar cambios en : " + archivoAbierto,JOptionPane.YES_NO_OPTION,
                  JOptionPane.QUESTION_MESSAGE,null,options,options[1]);
         
         // Quiere decir que el wey ya ha escrito algo pero no ha guardado...
     } else if((areaTexto.isEnabled() == true) && (areaTexto.getText().isEmpty() == false)) {
         Object[] options = {"Sí", "No"};
         int n = JOptionPane.showOptionDialog(this, "Desea guardar los cambios ?",
         "Guardar",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,null,options,options[0]);
         
         if(n == 0)
             guardarEnArchivo();
         System.exit(0);
     } else
         System.exit(0);
 }//GEN-LAST:event_alCerrar
 
 private void seleccionarTodoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seleccionarTodoActionPerformed
     
     if(areaTexto.isEnabled()) {
         areaTexto.requestFocusInWindow();
         areaTexto.selectAll(); 
     }
 }//GEN-LAST:event_seleccionarTodoActionPerformed
 
 private void cambiarTipoLetraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cambiarTipoLetraActionPerformed
     
     FontChooser font = new FontChooser(this);
     font.setVisible(true);
     
     if(font.getSelectedFont() != null)
         areaTexto.setFont(font.getSelectedFont());
     repaint();
 }//GEN-LAST:event_cambiarTipoLetraActionPerformed
 
 private void cambiarColorMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cambiarColorMenuItemActionPerformed
     areaTexto.setBackground(JColorChooser.showDialog(this, "Escoga el color de fondo", Color.white));
 }//GEN-LAST:event_cambiarColorMenuItemActionPerformed
 
 private void menuFormatoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuFormatoActionPerformed
 }//GEN-LAST:event_menuFormatoActionPerformed
 
 private void colorFuenteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorFuenteMenuItemActionPerformed
     areaTexto.setForeground(JColorChooser.showDialog(this, "Escoja el color del texto", Color.black));
 }//GEN-LAST:event_colorFuenteMenuItemActionPerformed
 
 private void copiarMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copiarMenuItemActionPerformed
     TextTransfer textTransfer = new TextTransfer();
     textTransfer.setClipboardContents(areaTexto.getSelectedText());
 }//GEN-LAST:event_copiarMenuItemActionPerformed
 
 private void imprimirMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imprimirMenuItemActionPerformed
     PrintAction.print(areaTexto, this); // PrintAction devuelve un truee, analizar para ver si se imprimió bien.
 }//GEN-LAST:event_imprimirMenuItemActionPerformed
 
 private void resaltarareaTextoBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resaltarareaTextoBtnActionPerformed
 }//GEN-LAST:event_resaltarareaTextoBtnActionPerformed
 
 private void compilarMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_compilarMenuItemActionPerformed
         
         palabras = new ArrayList<Lexema>();
         palabras.clear();
         
         try {
             Analizador.AnalizadorLexico(f, palabras, idiomaCheck.isSelected() == true ? true : false);
         } catch (FileNotFoundException ex) {
             Logger.getLogger(editorVentana.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(editorVentana.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         //String kaka = Analizador.getLexForm(palabras, idiomaCheck.isSelected() == true ? true : false);
     
         /* areaLexico.setText(kaka);
         
         verTablaDeSimbolos(); // Aquí se vacía toda la información:
             this.setVisible(false);
             verTablaSimbolos.setModal(true);
             verTablaSimbolos.setSize(800, 600);
             verTablaSimbolos.setResizable(true);
             verTablaSimbolos.setVisible(true);
         this.setVisible(true);
         */
         
         Parser.indentno = 0;
         Parser.tokenString = "";
         Parser syntax = new Parser(palabras, archivoAbierto);
         Parser.lineno = 0;
         NodoArbol arbolSintactico = syntax.parse();
         
         //Generador generador = new Generador(syntax.getTablaSimbolos());
         //generador.generarCodigo(); // Generamos código a un archivo.
         
         // TODO Sólo se avisará al usuario, el deberá arreglar los errores.
         if(syntax.getErrorString().length() == 0) {
             ImageIcon imageIcon = new ImageIcon("src/icons/ok_icon.gif");
             JOptionPane.showMessageDialog(this, "Compilación correcta.", "Todo correcto", JOptionPane.INFORMATION_MESSAGE, imageIcon);
         } else {
             JOptionPane.showMessageDialog(null, syntax.getErrorString(),
                     "Error de compilación", JOptionPane.ERROR_MESSAGE);
         }
         
         Parser.imprimirArbol(arbolSintactico);
         
 }//GEN-LAST:event_compilarMenuItemActionPerformed
 
 private void deshacerBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deshacerBtnActionPerformed
 
     if(areaTexto.isEnabled() == false)
         return;
     try {
           undoManager.undo();
         } catch (CannotRedoException cre) {
           cre.printStackTrace();
         }
         deshacerBtn.setEnabled(undoManager.canUndo());
         rehacerMenuItem.setEnabled(undoManager.canRedo());
 }//GEN-LAST:event_deshacerBtnActionPerformed
 
 private void cerrarBtnDialogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cerrarBtnDialogActionPerformed
     aboutDialog.setVisible(false);
 }//GEN-LAST:event_cerrarBtnDialogActionPerformed
 
 private void irAMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_irAMenuItemActionPerformed
 
     String line = JOptionPane.showInputDialog(this, "Número de línea:", "Ir A", JOptionPane.QUESTION_MESSAGE);
     if(line == null)
         return;
     else if(line.isEmpty())
         return;
     else {
         try {
             if((Integer.parseInt(line) < areaTexto.getLineCount()) && isNumeric(line))
             areaTexto.setCaretPosition(areaTexto.getLineStartOffset(Integer.parseInt(line)) - 1);
             areaTexto.grabFocus();
             areaTexto.requestFocusInWindow();
         } catch (BadLocationException ex) {
             Logger.getLogger(editorVentana.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }//GEN-LAST:event_irAMenuItemActionPerformed
 
 private void rehacerMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rehacerMenuItemActionPerformed
         if(areaTexto.isEnabled() == false)
         return;
         try {
               undoManager.redo();
         } catch (CannotRedoException cre) {
           cre.printStackTrace();
         }
         deshacerBtn.setEnabled(undoManager.canUndo());
         rehacerMenuItem.setEnabled(undoManager.canRedo());
 }//GEN-LAST:event_rehacerMenuItemActionPerformed
 
 private void verDatosMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verDatosMenuItemActionPerformed
     if(f != null)
         JOptionPane.showMessageDialog(this, f.getAbsoluteFile());
 }//GEN-LAST:event_verDatosMenuItemActionPerformed
 
 private void idiomaCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_idiomaCheckActionPerformed
     
     if(idiomaCheck.isSelected()) {
         idiomaCheck.setText("Español");
         idioma = true;
     } else {
         idiomaCheck.setText("Inglés");
         idioma = false;
     }
 }//GEN-LAST:event_idiomaCheckActionPerformed
 
 private void espanolMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_espanolMenuItemActionPerformed
     idiomaCheck.setSelected(true);
     idiomaCheck.setText("Español");
     idioma = true;
 }//GEN-LAST:event_espanolMenuItemActionPerformed
 
 private void inglesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inglesMenuItemActionPerformed
     idiomaCheck.setSelected(false);
     idiomaCheck.setText("Inglés");
     idioma = false;
 }//GEN-LAST:event_inglesMenuItemActionPerformed
 
 private void replaceBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceBtnActionPerformed
     
     Pattern patronReemplazo = Pattern.compile("\\b(write)\\b\\(");
     String todoElTexto = areaTexto.getText();
     Matcher matcher = patronReemplazo.matcher(todoElTexto);
     String replaceTo = "imprimir(";
     if(matcher.find())
     todoElTexto = matcher.replaceAll(replaceTo);
     areaTexto.setText(todoElTexto);
     
 }//GEN-LAST:event_replaceBtnActionPerformed
 
 private void selectAllMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllMenuItemActionPerformed
     if(areaTexto.isEnabled()) {
         areaTexto.requestFocusInWindow();
         areaTexto.selectAll(); 
     }
 }//GEN-LAST:event_selectAllMenuItemActionPerformed
 
 private void forCodeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_forCodeMenuItemActionPerformed
         areaTexto.insert("for(  ,   ,   )", areaTexto.getCaretPosition());
 }//GEN-LAST:event_forCodeMenuItemActionPerformed
 
 private void whileCodeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_whileCodeMenuItemActionPerformed
     areaTexto.insert("while()", areaTexto.getCaretPosition());
 }//GEN-LAST:event_whileCodeMenuItemActionPerformed
 
 private void syntaxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_syntaxMenuItemActionPerformed
 //    AnalizadorSintactico.Analizar(palabras);    
 }//GEN-LAST:event_syntaxMenuItemActionPerformed
 
 private void aceptarTabBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aceptarTabBtnActionPerformed
     
     if(!isNumeric(tabSizeTxt.getText()))
     {
         JOptionPane.showMessageDialog(this, "El tamaño debe ser un número entero válido", "Tamaño tabulador", JOptionPane.ERROR_MESSAGE);
     } else if(Integer.parseInt(tabSizeTxt.getText()) > 10) {
         JOptionPane.showMessageDialog(this, "El tamaño del tabulador no debe exceder los 10 caracteres.", "Tamaño tabulador", JOptionPane.ERROR_MESSAGE);
     } else {
         areaTexto.setTabSize(Integer.parseInt(tabSizeTxt.getText()));
         tabSizeDialog.setVisible(false);
     }
     
 }//GEN-LAST:event_aceptarTabBtnActionPerformed
 
 private void tabSizeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tabSizeMenuItemActionPerformed
     //this.setVisible(false);
    tabSizeDialog.setModal(true);
    tabSizeDialog.setSize(130, 100);
    tabSizeDialog.setResizable(false);
    tabSizeDialog.setVisible(true);
    this.setVisible(true);
 }//GEN-LAST:event_tabSizeMenuItemActionPerformed
 
     public static void main(String args[]) {
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
          * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
          */
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(editorVentana.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(editorVentana.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(editorVentana.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(editorVentana.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
         java.awt.EventQueue.invokeLater(new Runnable() {
             @Override
             public void run() {
                 new editorVentana().setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JDialog aboutDialog;
     private javax.swing.JMenuItem aboutMenuItem;
     private javax.swing.JMenuItem abrirMenuItem;
     private javax.swing.JButton aceptarTabBtn;
     javax.swing.JTextArea areaTexto;
     private javax.swing.JLabel ayudaLabel;
     private javax.swing.JMenu ayudaMenu;
     private javax.swing.JDialog ayudaPopup;
     private javax.swing.JMenuItem cambiarColorMenuItem;
     private javax.swing.JMenuItem cambiarTipoLetra;
     private javax.swing.JMenuItem cerrarArchivoMenuItem;
     private javax.swing.JButton cerrarBtnDialog;
     private javax.swing.JColorChooser colorArea;
     private javax.swing.JMenuItem colorFuenteMenuItem;
     private javax.swing.JMenu compilarMenu;
     private javax.swing.JMenuItem compilarMenuItem;
     private javax.swing.JMenu configuracionMenu;
     private javax.swing.JMenuItem copiarMenuItem;
     private javax.swing.JMenuItem cortarMenuItem;
     private javax.swing.JMenuItem deshacerBtn;
     private javax.swing.JDialog dialogoColor;
     private javax.swing.JMenu edicionMenu;
     private javax.swing.JMenuItem espanolMenuItem;
     private javax.swing.JLabel estadoDeLinea;
     private javax.swing.JMenuItem forCodeMenuItem;
     private javax.swing.JMenuItem guardarComoMenuItem;
     private javax.swing.JMenuItem guardarMenuItem;
     private javax.swing.JCheckBox idiomaCheck;
     private javax.swing.JMenu idiomaMenuItem;
     private javax.swing.JMenuItem imprimirMenuItem;
     private javax.swing.JMenuItem inglesMenuItem;
     private javax.swing.JMenuItem irAMenuItem;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JMenu jMenu1;
     private javax.swing.JMenuItem jMenuItem1;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JSeparator jSeparator1;
     private javax.swing.JTextArea jTextArea1;
     private javax.swing.JMenuBar menu;
     private javax.swing.JMenu menuArchivo;
     private javax.swing.JMenu menuFormato;
     private javax.swing.JMenuItem nuevoMenuItem;
     private javax.swing.JPopupMenu popup;
     private javax.swing.JMenuItem rehacerMenuItem;
     private javax.swing.JButton replaceBtn;
     private javax.swing.JMenuItem salirMenuItem;
     private javax.swing.JMenuItem seleccionarTodo;
     private javax.swing.JMenuItem selectAllMenuItem;
     private javax.swing.JFileChooser selectorDeArchivos;
     private javax.swing.JMenuItem syntaxMenuItem;
     private javax.swing.JDialog tabSizeDialog;
     private javax.swing.JMenuItem tabSizeMenuItem;
     private javax.swing.JTextField tabSizeTxt;
     private javax.swing.JMenuItem verDatosMenuItem;
     private javax.swing.JMenuItem whileCodeMenuItem;
     // End of variables declaration//GEN-END:variables
 }
