 package model.classes;
 
 import java.util.Observable;
 import java.util.Observer;
 
 import model.interfaces.IDocument;
 import model.interfaces.ILine;
 import model.interfaces.ISection;
 import model.interfaces.IStorable;
 import model.interfaces.IText;
 
 /**
  * 31 oct. 2012 - EditeurDeTexte.
  * @author Simon Devineau
  *         Ecole des Mines de Nantes
  *         Major in Computer and Information System Engineering
  *         Cursor.java
  */
 public final class Cursor extends Observable implements Observer {
     /**
      * the single instance of the cursor
      */
     private static volatile Cursor cursorInstance;
     /**
      * The current line in the document
      */
     private ILine                  currentLine;
     /**
      * The current section in the current document
      */
     private ISection               currentSection;
     /**
      * The introductive text of the current document
      */
     private IText                  currentTextIntro;
     /**
      * The current document of the editor
      */
     private IDocument              currentDocument;
     /**
      * The current position of the cursor in the current line
      */
     private int                    currentPosition = 0;
 
     private Cursor() {
     }
 
     /**
      * @return the cursorInstance
      */
     public static Cursor getCursorInstance() {
         initialize();
         return cursorInstance;
     }
 
     /**
      * @param cursorInstance
      *            the cursorInstance to set
      */
     public static void setCursorInstance(Cursor cursorInstance) {
         initialize();
         Cursor.cursorInstance = cursorInstance;
     }
 
     /**
      * @return the currentLine
      */
     public ILine getCurrentLine() {
         initialize();
         if (currentLine == null)
             Cursor.getCursorInstance().getCurrentDocument().addLine(new Line());
         return currentLine;
     }
 
     /**
      * @param currentLine
      *            the currentLine to set
      */
     public void setCurrentLine(ILine currentLine) {
         initialize();
        if(currentLine != null && currentLine.hasCursor())
        	this.currentLine.removeCursor();
         this.currentLine = currentLine;
         if(currentLine.hasCursor())
         	setCurrentPosition(getCurrentLine().getCursorLocation());
     }
     
     public IStorable getCurrentStorable(){
         if(Cursor.getCursorInstance().getCurrentSection()==null){
             return currentSection;
         }else{
             return currentTextIntro;
         }
     }
 
     /**
      * @return the currentSection
      */
     public ISection getCurrentSection() {
         initialize();
         return currentSection;
     }
 
     /**
      * @param currentSection
      *            the currentSection to set
      */
     public void setCurrentSection(ISection currentSection) {
         initialize();
         this.currentSection.setIsCurrentSection(false);
         this.currentSection = currentSection;
         this.currentSection.setIsCurrentSection(true);
         if(getCurrentSection().getTitle().hasCursor()) {
         	currentTextIntro = null;
         	setCurrentLine(getCurrentSection().getTitle());
         } else 
         	setCurrentText(getCurrentSection().getText());
     }
 
     /**
      * @return the currentTextIintro
      */
     public IText getCurrentText() {
         return currentTextIntro;
     }
 
     /**
      * @param currentTextIintro the currentTextIintro to set
      */
     public void setCurrentText(IText currentTextIintro) {
         this.currentTextIntro = currentTextIintro;
         int textSize = currentTextIintro.size();
         int index = 0;
         while(index < textSize && !getCurrentText().getLine(index).hasCursor())
         	index++;
         if(index == textSize)
         	throw new RuntimeException("An error occured in Cursor.setCurrentText");
         else
         	setCurrentLine(getCurrentText().getLine(index));
     }
 
     /**
      * @return the currentDocument
      */
     public IDocument getCurrentDocument() {
         initialize();
         return currentDocument;
     }
 
     /**
      * @param currentDocument
      *            the currentDocument to set
      */
     public void setCurrentDocument(IDocument currentDocument) {
         initialize();
         this.currentDocument = currentDocument;
     }
 
     /**
      * @return the currentPosition
      */
     public int getCurrentPosition() {
         return currentPosition;
     }
 
     /**
      * @param currentPosition
      *            the currentPosition to set
      */
     public void setCurrentPosition(int currentPosition) {
         this.currentPosition = currentPosition;
     }
 
     private static void initialize() {
         if (cursorInstance == null) {
             synchronized (Cursor.class) {
                 if (cursorInstance == null) {
                     cursorInstance = new Cursor();
                 }
             }
         }
     }
 
     /**
      * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
      */
     @Override
     public void update(Observable aO, Object aArg) {
         this.setChanged();
         this.notifyObservers();
     }
 }
