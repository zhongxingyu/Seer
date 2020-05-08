 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package de.cismet.tools.gui.breadcrumb;
 
 import java.util.List;
 
 /**
  *
  * @author thorsten
  */
 public interface BreadCrumbModel {
     public void addBreadCrumbModelListener(BreadCrumbModelListener bcListener);
     public void removeBreadCrumbModelListener(BreadCrumbModelListener bcListener);
     public int getSize();
     public BreadCrumb getCrumbAt(int position);
     public int getPositionOf(BreadCrumb bc);
     public void appendCrumb(BreadCrumb bc);
     public void startWithNewCrumb(BreadCrumb bc);
     public void removeTill(BreadCrumb bc);
     public List<BreadCrumb> getAllCrumbs();
     public BreadCrumb getLastCrumb();
     public BreadCrumb getFirstCrumb();
 
 }
