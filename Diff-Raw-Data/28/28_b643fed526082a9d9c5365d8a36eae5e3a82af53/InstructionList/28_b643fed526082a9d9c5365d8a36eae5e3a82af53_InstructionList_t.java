 package uk.ac.cam.db538.dexter.dex.code;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 import lombok.val;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexTryEnd;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexTryStart;
import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction;
 import uk.ac.cam.db538.dexter.dex.code.macro.DexMacro;
 import uk.ac.cam.db538.dexter.utils.Utils;
 
 public class InstructionList implements Collection<DexCodeElement> {
 
     private final List<DexCodeElement> instructionList;
 
     public InstructionList(List<? extends DexCodeElement> insns) {
         insns = expandMacros(insns);
 
         // check instruction list for duplicates
         // (often need to find the index of an instruction,
         //  so having duplicates could result in finding
         //  the wrong occurence)
         val visited = new HashSet<DexCodeElement>();
         for (val insn : insns)
             if (visited.contains(insn))
                 throw new IllegalArgumentException("Duplicates are not allowed in the instruction list");
             else
                 visited.add(insn);
 
         this.instructionList = Utils.finalList(insns);
     }
     
     public InstructionList(DexCodeElement ... insns) {
     	this(Arrays.asList(insns));
     }
 
     private static List<? extends DexCodeElement> expandMacros(List<? extends DexCodeElement> insns) {
         if (!hasMacros(insns))
             return insns;
 
         val expandedInsns = new ArrayList<DexCodeElement>();
         for (val insn : insns) {
             if (insn instanceof DexMacro)
                 expandedInsns.addAll(expandMacros(((DexMacro) insn).getInstructions().instructionList));
             else
                 expandedInsns.add(insn);
         }
         return expandedInsns;
     }
 
     private static boolean hasMacros(List<? extends DexCodeElement> insns) {
         for (val insn : insns)
             if (insn instanceof DexMacro)
                 return true;
         return false;
     }
 
     public DexCodeElement peekFirst() {
         if (instructionList.isEmpty())
             return null;
         else
             return instructionList.get(0);
     }
 
     public DexCodeElement peekLast() {
         if (instructionList.isEmpty())
             return null;
         else
             return instructionList.get(instructionList.size() - 1);
     }
 
     @Override
     public int size() {
         return instructionList.size();
     }
 
     @Override
     public boolean isEmpty() {
         return instructionList.isEmpty();
     }
 
     @Override
     public boolean contains(Object o) {
         return instructionList.contains(o);
     }
 
     @Override
     public Iterator<DexCodeElement> iterator() {
         return instructionList.iterator();
     }
 
     @Override
     public Object[] toArray() {
         return instructionList.toArray();
     }
 
     @Override
     public <T> T[] toArray(T[] a) {
         return instructionList.toArray(a);
     }
 
     @Override
     public boolean add(DexCodeElement e) {
         return instructionList.add(e);
     }
 
     @Override
     public boolean remove(Object o) {
         return instructionList.remove(o);
     }
 
     @Override
     public boolean containsAll(Collection<?> c) {
         return instructionList.containsAll(c);
     }
 
     @Override
     public boolean addAll(Collection<? extends DexCodeElement> c) {
         return instructionList.addAll(c);
     }
 
     @Override
     public boolean removeAll(Collection<?> c) {
         return instructionList.removeAll(c);
     }
 
     @Override
     public boolean retainAll(Collection<?> c) {
         return instructionList.retainAll(c);
     }
 
     @Override
     public void clear() {
         instructionList.clear();
     }
 
     public DexCodeElement get(int index) {
         return instructionList.get(index);
     }
 
     public int getIndex(DexCodeElement elem) {
         int index = instructionList.indexOf(elem);
         if (index < 0)
             throw new NoSuchElementException("Element of InstructionList not found");
         else
             return index;
     }
 
     public DexCodeElement getPreviousInstruction(DexCodeElement elem) {
         return instructionList.get(getIndex(elem) - 1);
     }
 
     public DexCodeElement getNextInstruction(DexCodeElement elem) {
         return instructionList.get(getIndex(elem) + 1);
     }
 
    public DexInstruction getNextProperInstruction(DexCodeElement elem) {
    	int index = getIndex(elem);
    	
    	while (true) {
    		DexCodeElement nextElem = instructionList.get(++index);
    		if (nextElem instanceof DexInstruction)
    			return (DexInstruction) nextElem;
    	}
    }

     public boolean isLast(DexCodeElement elem) {
         return getIndex(elem) == instructionList.size() - 1;
     }
 
     public boolean isBetween(DexCodeElement elemStart, DexCodeElement elemEnd, int indexSought) {
         int indexStart = getIndex(elemStart);
         int indexEnd = getIndex(elemEnd);
 
         return (indexStart <= indexSought) && (indexSought <= indexEnd);
     }
 
     public boolean isBetween(DexCodeElement elemStart, DexCodeElement elemEnd, DexCodeElement elemSought) {
         return isBetween(elemStart, elemEnd, getIndex(elemSought));
     }
 
     public List<DexTryStart> getAllTryBlocks() {
         val list = new ArrayList<DexTryStart>();
         for (val insn : instructionList)
             if (insn instanceof DexTryStart)
                 list.add((DexTryStart) insn);
         return list;
     }
 
     public Set<DexTryStart> getTryBlocks() {
         val set = new HashSet<DexTryStart>();
         for (val elem : instructionList)
             if (elem instanceof DexTryEnd)
                 set.add((DexTryStart) elem);
         return set;
     }
 
     public void dump() {
     	int i = 0;
         for (val insn : instructionList)
             System.err.println(Integer.toString(i++) + ": " + insn.toString());
     }
 }
