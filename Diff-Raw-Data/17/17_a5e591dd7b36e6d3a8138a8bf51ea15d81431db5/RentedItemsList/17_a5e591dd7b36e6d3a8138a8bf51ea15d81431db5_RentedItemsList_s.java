 package org.lajcik.kociolek.gui.components;
 
 import org.lajcik.kociolek.domain.Item;
 import org.lajcik.kociolek.domain.Rental;
 import org.lajcik.kociolek.service.RentalListener;
 import org.lajcik.kociolek.util.WtfException;
 
 import javax.swing.*;
 import java.awt.*;
 import java.util.*;
 import java.util.List;
 
 /**
  * @author lajcik
  */
 public class RentedItemsList extends JList implements RentalListener {
 
     public RentedItemsList() {
         super(new RentedItemsListModel());
         setCellRenderer(new RentedItemsRenderer());
     }
 
     public void itemRented(Rental rental) {
         for (Item item : rental.getRentedItems()) {
             getMyModel().addItem(item.getName(), rental.getTicketNumber());
         }
         invalidate();
     }
 
     public void itemReturned(Rental rental) {
         for (Item item : rental.getRentedItems()) {
             getMyModel().removeItem(item.getName(), rental.getTicketNumber());
         }
         invalidate();
     }
 
     public void itemsChanged(Rental before, Rental after) {
         itemReturned(before);
         itemRented(after);
     }
 
     @Override
     public void setModel(ListModel model) {
         throw new UnsupportedOperationException();
     }
 
     protected RentedItemsListModel getMyModel() {
         return (RentedItemsListModel) getModel();
     }
 
     private static class RentedItemsRenderer extends JLabel implements ListCellRenderer {
 
         public Component getListCellRendererComponent(JList list,
                                                       Object value,
                                                       int index,
                                                       boolean isSelected,
                                                       boolean cellHasFocus) {
             JLabel label = this;
             RentedItem val = (RentedItem) value;
             label.setText(val.name + " (" + val.tickets.size() + ")");
 
             StringBuilder sb = new StringBuilder("Tickets: ");
             for(Integer ticketNumber : val.tickets) {
                 sb.append("#").append(ticketNumber);
             }
             label.setToolTipText(sb.toString());
 
             // TODO: selected color
 
             return label;
         }
     }
 
     private static class RentedItem implements Comparable<RentedItem> {
         private String name;
         private Set<Integer> tickets = new TreeSet<Integer>();
 
         private RentedItem(String name) {
             this.name = name;
         }
 
         public int compareTo(RentedItem o) {
             return this.name.compareTo(o.name);
         }
     }
 
     private static class RentedItemsListModel extends AbstractListModel {
         private List<RentedItem> data = new ArrayList<RentedItem>();
 
         public int getSize() {
             return data.size();
         }
 
         public Object getElementAt(int index) {
             return data.get(index);
         }
 
         public void addItem(String name, int ticketNumber) {
             int idx = Collections.binarySearch(data, new RentedItem(name));
             RentedItem item;
             if (idx >= 0) {
                 item = data.get(idx);
             } else {
                 item = new RentedItem(name);
             }
             item.tickets.add(ticketNumber);
 
             // add the item if necessary and fire model change events
             if (idx < 0) {
                 idx = Math.abs(idx + 1);
                 data.add(idx, item);
                 fireIntervalAdded(this, idx, idx);
             }
         }
 
         public void removeItem(String name, int ticketNumber) {
             int idx = Collections.binarySearch(data, new RentedItem(name));
             if (idx < 0) {
                 throw new WtfException("Item " + name + " not present in model");
             }
             RentedItem item = data.get(idx);
             if (!item.tickets.remove(Integer.valueOf(ticketNumber))) {
                 throw new WtfException("Item " + name + " does not have ticket #" + ticketNumber + " assigned");
             }
 
             if (item.tickets.size() == 0) {
                 data.remove(idx);
                 fireIntervalRemoved(this, idx, idx);
             }
         }
     }
 }
