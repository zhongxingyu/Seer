 package es.miguelgonzalezgomez.dataBaseFun.qt;
 
 import com.trolltech.qt.core.Qt;
 import com.trolltech.qt.core.Qt.KeyboardModifiers;
 import com.trolltech.qt.gui.QKeyEvent;
 import com.trolltech.qt.gui.QPlainTextEdit;
 import com.trolltech.qt.gui.QSizePolicy;
 import com.trolltech.qt.gui.QTextCursor;
 import es.miguelgonzalezgomez.dataBaseFun.domain.MPestana;
 import es.miguelgonzalezgomez.dataBaseFun.domain.holders.IndentacionHolder;
 import es.miguelgonzalezgomez.dataBaseFun.qt.controladores.CEditor;
 import java.util.Objects;
 import java.util.UUID;
 
 /**
  *
  * @author Miguel González
  */
     public class EditorTexto extends QPlainTextEdit {
     
     private final int NUM_SPACES_INDENT = 4;
     private UUID uuid;
     private CEditor controlador;
     
     public EditorTexto(CEditor controlador) {
         super();
         
         this.controlador = controlador;
         uuid = UUID.randomUUID();
         setLineWrapMode(LineWrapMode.NoWrap);
         setTabStopWidth(40);
         setSizePolicy(new QSizePolicy(QSizePolicy.Policy.Expanding, QSizePolicy.Policy.Expanding));
         
         establecerEventos();
     }
     
     @Override
     public boolean equals(Object object) {
         if(object == null) {
             return false;
         }
         if(!(object instanceof EditorTexto)) {
             return false;
         }
         EditorTexto editorTextoComparar = (EditorTexto) object;
         return uuid.equals(editorTextoComparar.uuid);
     }
 
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 41 * hash + Objects.hashCode(this.uuid);
         return hash;
     }
     
     public void establecerTexto(String texto) {
         texto = texto.replaceAll("\t", getTabIndent());
         setPlainText(texto);
     }
     
     private void establecerEventos() {
         textChanged.connect(controlador, "eventoTextoCambiado()");
         selectionChanged.connect(controlador, "eventoSeleccionCambiado()");
         verticalScrollBar().valueChanged.connect(controlador, "scrollBarCambiado()");
         cursorPositionChanged.connect(controlador, "cursorTextoCambiado()");
        blockCountChanged.connect(controlador, "blockCountCambiado()");
     }
     
     public int getPositionScrollBar() {
         return verticalScrollBar().value();
     }
    
     public MPestana getModeloEditor() {
         return controlador.getModeloEditor();
     }
     
     @Override
     protected void keyPressEvent(QKeyEvent event) {
         if(suprimirRehacerNativo(event)) {
             return;
         }
         
         if(esRehacer(event)) {
             controlador.rehacer();
             return;
         }
         
         if(esCambiarSiguientePestana(event)) {
             controlador.cambiarSiguientePestana();
             return;
         }
         
         if(esCambiarAnteriorPestana(event)) {
             controlador.cambiarAnteriorPestana();
             return;
         }
         
         if(esIndentarDerecha(event)) {
             indentarDerecha();
             return;
         }
         
         if(esIndentarIzquierda(event)) {
             indentarIzquierda();
             return;
         }
         
         
         super.keyPressEvent(event);
     }
     
     private boolean suprimirRehacerNativo(QKeyEvent e) {
         KeyboardModifiers modifiers = e.modifiers();
         if(
                 modifiers.isSet(Qt.KeyboardModifier.ControlModifier) &&
                 modifiers.isSet(Qt.KeyboardModifier.ShiftModifier)) {
             if(e.key() == Qt.Key.Key_Z.value()) {
                 return true;
             }
         }
         
         return false;
     }
     
     private boolean esRehacer(QKeyEvent e) {
         KeyboardModifiers modifiers = e.modifiers();
         if(
                 modifiers.isSet(Qt.KeyboardModifier.ControlModifier)) {
             if(e.key() == Qt.Key.Key_Y.value()) {
                 return true;
             }
         }
         
         return false;
     }
     
     private boolean esCambiarSiguientePestana(QKeyEvent e) {
         KeyboardModifiers modifiers = e.modifiers();
         if(
                 modifiers.isSet(Qt.KeyboardModifier.ControlModifier)) {
             if(e.key() == Qt.Key.Key_Tab.value()) {
                 return true;
             }
         }
         
         return false;
     }
     
     private boolean esCambiarAnteriorPestana(QKeyEvent e) {
         KeyboardModifiers modifiers = e.modifiers();
         if(
                 modifiers.isSet(Qt.KeyboardModifier.ControlModifier) &&
                 modifiers.isSet(Qt.KeyboardModifier.ShiftModifier)) {
             if(e.key() == Qt.Key.Key_Backtab.value()) {
                 return true;
             }
         }
         
         return false;
     }
     
     private boolean esIndentarDerecha(QKeyEvent e) {
         if(e.key() == Qt.Key.Key_Tab.value()) {
             return true;
         }
         return false;
     }
     
     private boolean esIndentarIzquierda(QKeyEvent e) {
         KeyboardModifiers modifiers = e.modifiers();
         if(
                 modifiers.isSet(Qt.KeyboardModifier.ShiftModifier)) {
             if(e.key() == Qt.Key.Key_Backtab.value()) {
                 return true;
             }
         }
 
         return false;
     }
     
     private void indentarDerecha() {
         if(textCursor().hasSelection()) {
             indentarSeleccionDerecha();
         } else {
             indentarSimpleDerecha();
         }
     }
     
     private void indentarSimpleDerecha() {
         int caracteresPrevios = textCursor().position() -
                 textCursor().block().position();
         String tab = getTabIndentParcial(caracteresPrevios);
         insertPlainText(tab);
     }
     
     private void indentarSeleccionDerecha() {
         IndentacionHolder indentacionHolder = new IndentacionHolder();
         indentacionHolder.curs = textCursor();
         
         obtenemosFilasIndentar(indentacionHolder);
         indentamosFilas(indentacionHolder);
         seleccionamosTodasFilasIndentadas(indentacionHolder);
     }
     
     private void obtenemosFilasIndentar(IndentacionHolder indentacionHolder) {
         QTextCursor curs = indentacionHolder.curs;
         
         int posInicial =  curs.anchor();
         int posFinal = curs.position();
 
         if(posInicial > posFinal) {
             int hold = posInicial;
             posInicial = posFinal;
             posFinal = hold;
         }
         indentacionHolder.posInicial = posInicial;
         indentacionHolder.posFinal = posFinal;
 
         curs.setPosition(posInicial, QTextCursor.MoveMode.MoveAnchor);
         indentacionHolder.bloqueInical = curs.block().blockNumber();
 
         curs.setPosition(posFinal, QTextCursor.MoveMode.MoveAnchor);
         indentacionHolder.bloqueFinal = curs.block().blockNumber();
     }
     
     private void indentamosFilas(IndentacionHolder indentacionHolder) {
         QTextCursor curs = indentacionHolder.curs;
         
         curs.setPosition(indentacionHolder.posInicial,
                 QTextCursor.MoveMode.MoveAnchor);
         curs.beginEditBlock();
 
         String tabIndent = getTabIndent();
         for(int i = 0; i <= (indentacionHolder.bloqueFinal - 
                 indentacionHolder.bloqueInical); ++i) {
             curs.movePosition(QTextCursor.MoveOperation.StartOfBlock,
                     QTextCursor.MoveMode.MoveAnchor);
 
             curs.insertText(tabIndent);
 
             curs.movePosition(QTextCursor.MoveOperation.NextBlock,
                     QTextCursor.MoveMode.MoveAnchor);
         }
 
         curs.endEditBlock();
     }
         
     private void indentarIzquierda() {
         if(textCursor().hasSelection()) {
             indentarSeleccionIzquierda();
         } else {
             indentarSimpleIzquierda();
         }
     }
     
     private void indentarSimpleIzquierda() {
         IndentacionHolder indentacionHolder = new IndentacionHolder();
         indentacionHolder.curs = textCursor();
         indentacionHolder.curs.beginEditBlock();
         indentarFilaIzquierda(indentacionHolder);
         indentacionHolder.curs.endEditBlock();
     }
     
     private void indentarSeleccionIzquierda() {
         IndentacionHolder indentacionHolder = new IndentacionHolder();
         indentacionHolder.curs = textCursor();
                 
         obtenemosFilasIndentarIzquierda(indentacionHolder);
         indentamosFilasIzquierda(indentacionHolder);
         seleccionamosTodasFilasIndentadas(indentacionHolder);
     }
     
     private void obtenemosFilasIndentarIzquierda(IndentacionHolder indentacionHolder) {
         QTextCursor curs = indentacionHolder.curs;
         
         int posInicial =  curs.anchor();
         int posFinal = curs.position();
 
         if(posInicial > posFinal) {
             int hold = posInicial;
             posInicial = posFinal;
             posFinal = hold;
         }
         indentacionHolder.posInicial = posInicial;
         indentacionHolder.posFinal = posFinal;
 
         curs.setPosition(posInicial, QTextCursor.MoveMode.MoveAnchor);
         indentacionHolder.bloqueInical = curs.block().blockNumber();
 
         curs.setPosition(posFinal, QTextCursor.MoveMode.MoveAnchor);
         indentacionHolder.bloqueFinal = curs.block().blockNumber();
     }
     
     private void indentamosFilasIzquierda(IndentacionHolder indentacionHolder) {
         QTextCursor curs = indentacionHolder.curs;
         
         curs.setPosition(indentacionHolder.posInicial,
                 QTextCursor.MoveMode.MoveAnchor);
         curs.beginEditBlock();
         for(int i = 0; i <= (indentacionHolder.bloqueFinal - 
                 indentacionHolder.bloqueInical); ++i) {
             
             indentarFilaIzquierda(indentacionHolder);
             
             curs.movePosition(QTextCursor.MoveOperation.NextBlock,
                     QTextCursor.MoveMode.MoveAnchor);
         }
 
         curs.endEditBlock();
     }
     
     private void indentarFilaIzquierda(IndentacionHolder indentacionHolder) {
         QTextCursor curs = indentacionHolder.curs;
         curs.movePosition(QTextCursor.MoveOperation.StartOfBlock,
                 QTextCursor.MoveMode.MoveAnchor);
         for(int i=0; i<NUM_SPACES_INDENT; i++) {
             
             curs.movePosition(QTextCursor.MoveOperation.NextCharacter,
                     QTextCursor.MoveMode.KeepAnchor);
             String primeraLetra =  curs.selectedText();
 
             curs.movePosition(QTextCursor.MoveOperation.PreviousCharacter,
                     QTextCursor.MoveMode.MoveAnchor);
 
             if(" ".equals(primeraLetra)) {
                 curs.deleteChar();
             }
         }
     }
     
     private void seleccionamosTodasFilasIndentadas(
             IndentacionHolder indentacionHolder) {
         QTextCursor curs = indentacionHolder.curs;
         
         curs.setPosition(indentacionHolder.posInicial,
                 QTextCursor.MoveMode.MoveAnchor);
         curs.movePosition(QTextCursor.MoveOperation.StartOfBlock, 
                 QTextCursor.MoveMode.MoveAnchor);
         
         while(curs.block().blockNumber() < indentacionHolder.bloqueFinal) {
             curs.movePosition(QTextCursor.MoveOperation.NextBlock,
                     QTextCursor.MoveMode.KeepAnchor);
         }
 
         curs.movePosition(QTextCursor.MoveOperation.EndOfBlock,
                 QTextCursor.MoveMode.KeepAnchor);
 
         setTextCursor(curs);
     }
     
     public String getTabIndent() {
         String tabIndent = "";
         for(int i=0; i<NUM_SPACES_INDENT; i++) {
             tabIndent += " ";
         }
         return tabIndent;
     }
     
     public String getTabIndentParcial(int caracteresPrevios) {
         int numSpacesAppend = NUM_SPACES_INDENT - caracteresPrevios % NUM_SPACES_INDENT;
         String indentTab = "";
         for(int i=0; i<numSpacesAppend; i++) {
             indentTab += " ";
         }
         return indentTab;
     }
 
     public void establecerEditorVisible() {
         controlador.establecerEditorVisible();
     }
 
     public int getNumLineas() {
         return blockCount();
     }
 
     public int[] getLineasSeleccionadas() {
         int[] lineasSeleccionadas;
         
         IndentacionHolder indentacionHolder = new IndentacionHolder();
         indentacionHolder.curs = textCursor();
         obtenemosFilasIndentarIzquierda(indentacionHolder);
         
         int numLineasSeleccionadas = indentacionHolder.bloqueFinal -
                 indentacionHolder.bloqueInical + 1;
         lineasSeleccionadas = new int[numLineasSeleccionadas];
         
         for(int i=1; i <= numLineasSeleccionadas; i++) {
             lineasSeleccionadas[i-1] =
                     indentacionHolder.bloqueInical + i;
         }
         
         return lineasSeleccionadas;
     }
 }
