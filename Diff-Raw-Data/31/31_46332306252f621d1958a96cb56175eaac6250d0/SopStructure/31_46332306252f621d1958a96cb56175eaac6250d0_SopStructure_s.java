 package sequenceplanner.model.SOP;
 
 import java.util.LinkedList;
 import java.util.ListIterator;
 import sequenceplanner.view.operationView.graphextension.Cell;
 
 /**
  *
  * @author Qw4z1
  * *Till viktor*
  * Vi mste ha en Linked List fr varje sekvens dr ny root "Before ->Operation"
  * lggs till som ny "addFirst". Lggs en ny operation till "after" s lggs den
  * i sist i listan. Lggs en parallell eller alternativ till s mste de lnkas
  * ihop i en annan lista via listan.
  *
  */
 public class SopStructure implements ISopStructure {
 
     private ASopNode node;
     private LinkedList<LinkedList<Object>> sopSeqs = new LinkedList<LinkedList<Object>>();
     private LinkedList<Object> sopStructure;
     private LinkedList<Object> li;
 
     public SopStructure() {
     }
     /*public SopStructure(Cell cell, ASopNode sopNode, boolean before) {
     //If the cell exists in the sequence, the new cell should be added
     //*This is not really true, since the cell can exists within two
     //sequences in the same OpView. So have to rethink this structure*
     for (SopSequence sopSeq :sopSeqs)  {
     if (sopSeqs.contains(sopNode)) {
     sopSeqs.add(sopNode);
     } else {
     //*******Fixa till Lista!*******
     SopSequence sopSeq = new SopSequence(sopNode, cell, before);
 
     }
     }
     }
 
     @Override
     public void addNode(ISopNode node) {
     }
 
     @Override
     public void addNodeToRoot(ISopNode node) {
     throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void addNodeToSequence(ISopNode node) {
     throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void addSopSequence(SopSequence seq) {
     sopSeqs.add(seq);
     }
     //Should be a list that is returned
 
     public LinkedList<SopSequence> getAllSopSequences() {
     return sopSeqs;
     }*/
     //A new Operation is created -> new SopSequence
 
     public LinkedList getSopSequence() {
         return sopStructure;
     }
 
     @Override
     public void setSopSequence(ASopNode sopNode) {
         //Create new SOPList
         sopStructure = new LinkedList<Object>();
         sopStructure.add(sopNode);
         sopSeqs.add(sopStructure);
     }
 
     @Override
     public void setSopSequence(Cell cell, ASopNode sopNode, boolean before) {
 
         //sopStructure.add(sopNode);
         for (LinkedList sopSeq : sopSeqs) {
             System.out.println("First: "+sopSeq.getFirst().toString());
             System.out.println("Second:"+sopNode.toString());
            if (sopSeq.contains(sopNode)) {
                 //If the cell is inserted before an other cell
                 if (before == true) {
                     for (ListIterator<Object> it = sopSeq.listIterator(); it.hasNext();) {
                         if (it.next() == cell) {
                             it.add(sopNode);
                             break;
                         }
                        it.next();
                     }
                     //If the cell is inserted after an other cell
                 } else if (before == false) {
                     for (ListIterator<Object> it = sopSeq.listIterator(); it.hasNext();) {
                         if (it.next().equals(cell)) {
                             it.next();
                             it.add(sopNode);
                             break;
                         }
                        it.next();
 
                     }
                 }
 
 
 
            } else {
                System.out.println("Something went wrong!");
            }
         }
     }
 }
