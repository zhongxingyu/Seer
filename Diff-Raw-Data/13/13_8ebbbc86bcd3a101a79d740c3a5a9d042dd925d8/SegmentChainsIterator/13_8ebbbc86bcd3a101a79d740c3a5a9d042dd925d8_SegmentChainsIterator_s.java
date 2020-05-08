 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.epsilony.tsmf.model;
 
 import java.util.Iterator;
 import java.util.List;
 import net.epsilony.tsmf.util.MiscellaneousUtils;
 
 /**
  *
  * @author epsilon
  */
 class SegmentChainsIterator<T extends Segment2D> implements Iterator<T> {
 
     Iterator<T> headIterator;
     T seg;
     T last;
     T head;
    boolean reachChainEnd;
 
     SegmentChainsIterator(List<T> chainsHeads) {
         headIterator = chainsHeads.iterator();
         seg = headIterator.hasNext() ? headIterator.next() : null;
        reachChainEnd = seg == null;
         head = seg;
     }
 
     @Override
     public boolean hasNext() {
        return headIterator.hasNext() || !reachChainEnd;
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public T next() {
         T res = seg;
         seg = (T) seg.getSucc();
         if (seg.getPred() != res || seg.getPred() == seg) {
             throw new IllegalStateException("Meet broken Segment2D link, may cause self ring");
         }
         if (seg == head) {
            seg = headIterator.next();
             head = seg;
         }
         last = res;
         return res;
     }
 
     @Override
     public void remove() {
         if (last.getPred().getPred() == last.getSucc()) {
             throw new IllegalStateException("The chain is only a triangle, and no segments can be removed!");
         }
         MiscellaneousUtils.link(last.getPred(), last.getSucc());
     }
 }
