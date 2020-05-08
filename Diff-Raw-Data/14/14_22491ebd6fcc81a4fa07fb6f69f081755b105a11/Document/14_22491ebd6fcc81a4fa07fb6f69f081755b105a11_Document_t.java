 /**
  * 
  */
 package model.classes;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 
 import model.interfaces.IBufferMemory;
 import model.interfaces.ICommandVisitor;
 import model.interfaces.IDocument;
 import model.interfaces.ILine;
 import model.interfaces.ISection;
 import model.interfaces.IText;
 
 /**
  * 10 oct. 2012 - EditeurDeTexte.
  * @author Simon Devineau & Pierre Reliquet Ecole des Mines de Nantes Major in
  *         Computer and Information System Engineering Document.java A Document
  *         is composed of a an introduction text and a list of sections which
  *         can have subsections...
  */
 class Document extends Observable implements IDocument {
     /**
      * The introduction text of the document
      */
     private IText          introductionText  = Factory.createText();
     /**
      * The sections list of the document
      */
     private List<ISection> sectionsList      = new ArrayList<ISection>();
     /**
      * True if it is the current document
      */
     private boolean        isCurrentDocument = false;
     /**
      * The buffer memory of this document
      */
     private IBufferMemory  bufferMemory      = Factory.createBufferMemory();
     /**
      * The path of the document
      */
     private String         path              = "";
 
     public Document() {
         this.introductionText = Factory.createText();
         this.sectionsList = new ArrayList<ISection>();
         this.path = " ";
         this.addObserver(Cursor.getCursorInstance());
     }
 
     public Document(String path) {
         this();
         this.path = path;
     }
 
     /**
      * @see model.interfaces.IDocument#addSection(model.interfaces.ISection)
      */
     @Override
     public boolean addSection(ISection aSection) {
         this.sectionsList.add(this.getIndexCurrentSection(), aSection);
         this.setChanged();
         this.notifyObservers();
         return this.sectionsList.contains(aSection);
     }
 
     /**
      * @see model.interfaces.IDocument#appendSection(model.interfaces.ISection)
      */
     @Override
     public boolean appendSection(ISection aSection) {
         return this.sectionsList.add(aSection);
     }
 
     /**
      * @see model.interfaces.IDocument#deleteSection()
      */
     @Override
     public boolean deleteSection() {
         // TODO vérifier
         int index = 0;
         while (this.getCurrentSection().equals(this.sectionsList.get(index))) {
             index++;
         }
         this.setChanged();
         this.notifyObservers();
         return this.sectionsList.remove(this.sectionsList.get(index));
     }
 
     /**
      * @see model.interfaces.IDocument#deleteSection(model.interfaces.ISection)
      */
     @Override
     public boolean deleteSection(ISection aSection) {
         this.setChanged();
         this.notifyObservers();
         return this.sectionsList.remove(aSection);
     }
 
     /**
      * @see model.interfaces.IDocument#addLine(model.interfaces.ILine)
      */
     @Override
     public void addLine(ILine aLine) {
        System.out.println("ligne document " + aLine);
         if (Cursor.getCursorInstance().getCurrentDocument().equals(this)) {
             // Si la section est null, c'est que nous sommes dans le texte
 // introductif du document
             if (Cursor.getCursorInstance().getCurrentSection() == null) {
                 this.introductionText.addLine(aLine);
             }
             else {
                 Cursor.getCursorInstance().getCurrentSection().getText()
                         .addLine(aLine);
             }
         }
         this.setChanged();
         this.notifyObservers();
     }
 
     /**
      * @see model.interfaces.IDocument#getBufferMemory()
      */
     @Override
     public IBufferMemory getBufferMemory() {
         return this.bufferMemory;
     }
 
     /**
      * @see model.interfaces.IDocument#getCurrentSection()
      */
     @Override
     public ISection getCurrentSection() {
         int sectionsSize = sectionsList.size();
         int currentIndex = 0;
         while (currentIndex < sectionsSize
                 && !getSectionsList().get(currentIndex).isCurrentSection())
             currentIndex++;
         if (currentIndex == sectionsSize)
             throw new RuntimeException("No current section found!");
         return getSectionsList().get(currentIndex).getCurrentSection();
     }
 
     /**
      * @see model.interfaces.IDocument#getIindexCurrentSection()
      */
     @Override
     public int getIndexCurrentSection() {
         int sectionsSize = sectionsList.size();
         int index = 0;
         while (index < sectionsSize
                 && !this.sectionsList.get(index).isCurrentSection())
             index++;
         return index;
     }
 
     /**
      * @see model.interfaces.IDocument#getPath()
      */
     @Override
     public String getPath() {
         return this.path;
     }
 
     /**
      * @return the sectionsList
      */
     public List<ISection> getSectionsList() {
         return this.sectionsList;
     }
 
     /**
      * @see model.interfaces.IDocument#getText()
      */
     @Override
     public IText getText() {
         return this.introductionText;
     }
 
     /**
      * @see model.interfaces.IDocument#isCurrentDocument()
      */
     @Override
     public boolean isCurrentDocument() {
         this.setChanged();
         this.notifyObservers();
         return this.isCurrentDocument;
     }
 
     /**
      * @param bufferMemory
      *            the bufferMemory to set
      */
     public void setBufferMemory(IBufferMemory bufferMemory) {
         this.setChanged();
         this.notifyObservers();
         this.bufferMemory = bufferMemory;
     }
 
     /**
      * @see model.interfaces.IDocument#setIsCurrentDocument(boolean)
      */
     @Override
     public void setIsCurrentDocument(boolean aIsCurrentDocument) {
         this.setChanged();
         this.notifyObservers();
         this.isCurrentDocument = aIsCurrentDocument;
     }
 
     /**
      * @see model.interfaces.IDocument#setPath(java.net.URL)
      */
     @Override
     public void setPath(String aString) {
         this.path = aString;
     }
 
     /**
      * @param sectionsList
      *            the sectionsList to set
      */
     public void setSectionsList(List<ISection> sectionsList) {
         this.setChanged();
         this.notifyObservers();
         this.sectionsList = sectionsList;
     }
 
     /**
      * @see model.interfaces.IDocument#setText(model.interfaces.IText)
      */
     @Override
     public void setText(IText aText) {
         this.setChanged();
         this.notifyObservers();
         this.introductionText = aText;
     }
 
     /**
      * @see model.interfaces.IDocument#getSection(int)
      */
     @Override
     public ISection getSection(int index) {
         return sectionsList.get(index);
     }
 
     /**
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
         String toReturn = this.getText().toString() + "\n";
         int size = sectionsList.size();
         for (int i = 0; i < size; i++) {
             toReturn += sectionsList.get(i).toString() + "\n";
         }
         return toReturn;
     }
 
     /**
      * @see model.interfaces.IDocument#toHTML()
      */
     @Override
     public String toHTML() {
         String toReturn = "<html>" + this.getText().toString() + "\n";
         int size = sectionsList.size();
         for (int i = 0; i < size; i++) {
             toReturn += sectionsList.get(i).toString() + "\n";
         }
         toReturn += "</html>";
         return toReturn;
     }
 
     @Override
     public void addSection(ISection section, int index) {
         this.setChanged();
         this.notifyObservers();
         sectionsList.add(index, section);
     }
 
     @Override
     public ISection removeSection(int index) {
         this.setChanged();
         this.notifyObservers();
         return sectionsList.remove(index);
     }
 }
