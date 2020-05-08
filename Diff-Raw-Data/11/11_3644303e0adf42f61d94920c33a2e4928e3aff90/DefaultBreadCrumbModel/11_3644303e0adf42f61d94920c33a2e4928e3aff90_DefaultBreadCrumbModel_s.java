 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.cismet.tools.gui.breadcrumb;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 import java.util.Vector;
 
 /**
  *
  * @author thorsten
  */
 public class DefaultBreadCrumbModel implements BreadCrumbModel {
 
     private Vector<BreadCrumbModelListener> listeners = new Vector<BreadCrumbModelListener>();
     private Vector<BreadCrumb> data = new Vector<BreadCrumb>();
 
     @Override
     public void addBreadCrumbModelListener(BreadCrumbModelListener bcListener) {
         listeners.add(bcListener);
     }
 
     @Override
     public void appendCrumb(BreadCrumb bc) {
         appendCrumbSilently(bc);
         fireBreadCrumbAdded(new BreadCrumbEvent(this,bc));
     }
 
     private void appendCrumbSilently(final BreadCrumb bc) {
         data.add(bc);
         bc.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 BreadCrumbEvent bce=new BreadCrumbEvent(DefaultBreadCrumbModel.this,bc);
                 fireBreadCrumbActionPerformed(bce);
             }
         });
     }
 
     @Override
     public List<BreadCrumb> getAllCrumbs() {
         return new Vector<BreadCrumb>(data);
     }
 
     @Override
     public BreadCrumb getCrumbAt(int position) {
         return data.get(position);
     }
 
     @Override
     public int getPositionOf(BreadCrumb bc) {
         return data.indexOf(bc);
     }
 
     @Override
     public int getSize() {
         return data.size();
     }
 
     @Override
     public BreadCrumb getLastCrumb() {
         return data.lastElement();
     }
 
     @Override
     public BreadCrumb getFirstCrumb() {
         return data.firstElement();
     }
 
 
 
 
 
     @Override
     public void removeBreadCrumbModelListener(BreadCrumbModelListener bcListener) {
         listeners.remove(bcListener);
     }
 
     @Override
     public void removeTill(BreadCrumb bc) {
         int lastIndex = data.lastIndexOf(bc);
         if (lastIndex != -1) {
             for (int i = data.size() - 1; i > lastIndex; --i) {
                 data.remove(i);
             }
         }
         fireBreadCrumbModelChanged(new BreadCrumbEvent(this));
     }
 
     @Override
     public void startWithNewCrumb(BreadCrumb bc) {
         data=new Vector<BreadCrumb>();
         appendCrumbSilently(bc);
         fireBreadCrumbModelChanged(new BreadCrumbEvent(this));
     }
 
 
     public void fireBreadCrumbModelChanged(BreadCrumbEvent bce){
         for (BreadCrumbModelListener listener:new Vector<BreadCrumbModelListener>(listeners)){
             listener.breadCrumbModelChanged(bce);
         }
 
 
     }
 
     public void fireBreadCrumbAdded(BreadCrumbEvent bce){
         for (BreadCrumbModelListener listener:new Vector<BreadCrumbModelListener>(listeners)){
             listener.breadCrumbAdded(bce);
         }
 
     }
 
     public void fireBreadCrumbActionPerformed(BreadCrumbEvent bce){
         for (BreadCrumbModelListener listener:new Vector<BreadCrumbModelListener>(listeners)){
             listener.breadCrumbActionPerformed(bce);
         }
          
     }
 }
