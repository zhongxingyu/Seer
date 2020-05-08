 package com.github.krassekoder.mm13client.gui.data;
 
 import com.github.krassekoder.mm13client.network.Packet;
 import com.github.krassekoder.mm13client.network.Packet3Data;
 import com.trolltech.qt.core.QDate;
 import com.trolltech.qt.core.QDateTime;
 import com.trolltech.qt.core.Qt;
 import com.trolltech.qt.gui.QHeaderView;
 import com.trolltech.qt.gui.QResizeEvent;
 import com.trolltech.qt.gui.QShowEvent;
 import com.trolltech.qt.gui.QTreeWidget;
 import com.trolltech.qt.gui.QTreeWidgetItem;
 import com.trolltech.qt.gui.QWidget;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class SalesView extends QTreeWidget {
 
     public SalesView(QWidget parent) {
         super(parent);
         setHeaderHidden(true);
         setColumnCount(2);
     }
 
     @Override
     protected void showEvent(QShowEvent qse) {
         super.showEvent(qse);
         List<Packet3Data.Sale> sales = null;
         try {
              sales = Packet3Data.instance.getSales();
         } catch(Packet.InvalidPacketException ex) {
             Logger.getLogger(SalesView.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             if(sales == null)
                  sales = new ArrayList<Packet3Data.Sale>();
         }
 
         int currentDate = -1;
         QTreeWidgetItem currentRoot = null;
 
        System.out.println(sales);

         for(Packet3Data.Sale sale : sales) {
             QDateTime dt = QDateTime.fromTime_t(sale.time_t);
             if(dt.date().toJulianDay() != currentDate) {
                 currentDate = dt.date().toJulianDay();
                 currentRoot = new QTreeWidgetItem(this);
                 currentRoot.setText(0, dt.date().toString());
             }
 
             QTreeWidgetItem currentItem = new QTreeWidgetItem(currentRoot);
             currentItem.setText(0, dt.time().toString());
            currentItem.setText(1, String.format("%1$.2f", sale.sum));
         }
     }
 }
