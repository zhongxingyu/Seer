 package org.openengsb.openticket.model;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.IFilterStateLocator;
 import org.apache.wicket.extensions.markup.html.repeater.util.SingleSortState;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 
 public class TicketDataProvider implements ISortableDataProvider<Ticket>, IFilterStateLocator {
 
     ISortState sortState = new SingleSortState();
     TicketFilter filter = new TicketFilter();
 
    final static int SIZE = 99;
     List<Ticket> list;
 
     @Override
     public Object getFilterState() {
         return filter;
     }
 
     @Override
     public void setFilterState(Object state) {
         filter=(TicketFilter)state;
     }
 
     @Override
     public Iterator<? extends Ticket> iterator(int first, int count) {
         initList();
 
         List<Ticket> ret = list;
         if (ret.size() > (first + count)) {
             ret = ret.subList(first, first + count);
        }else{
            ret=ret.subList(first, ret.size());
         }
         return ret.iterator();
     }
 
     @Override
     public int size() {
         initList();
         return list.size();
     }
 
     @Override
     public IModel<Ticket> model(Ticket object) {
         return Model.of(object);
     }
 
     @Override
     public void detach() {
         list=null;
     }
 
     @Override
     public ISortState getSortState() {
         return sortState;
     }
 
     @Override
     public void setSortState(ISortState state) {
         sortState=state;
 
     }
 
     private void initList() {
         if (list == null) {
             final int idSort;
             final int typeSort;
 
             if (sortState != null) {
                 idSort = sortState.getPropertySortOrder("id");
                 typeSort = sortState.getPropertySortOrder("type");
             } else {
                 idSort = ISortState.NONE;
                 typeSort = ISortState.NONE;
             }
             list = getSortedList(idSort, typeSort, filter);
         }
     }
 
     private List<Ticket> getSortedList(final int idSort, final int typeSort, TicketFilter filter2) {
         List<Ticket> result = TicketGenerator.getBeans(SIZE, filter);
         Collections.sort(result, new Comparator<Ticket>() {
 
             @Override
             public int compare(Ticket t1, Ticket t2) {
                 int compId = t1.getId().compareTo(t2.getId());
                 int compType = t1.getType().compareTo(t2.getType());
 
                 switch (idSort) {
                     case ISortState.NONE:
                        compId = 0;
                        break;
                    case ISortState.DESCENDING:
                        compId = -compId;
                        break;
                }
                switch (typeSort) {
                    case ISortState.NONE:
                        compType = 0;
                        break;
                    case ISortState.DESCENDING:
                        compType = -compType;
                        break;
                }
                if (compId != 0)
                    return compId;
                return compType;
            }
 
         });
         return result;
     }
 }
