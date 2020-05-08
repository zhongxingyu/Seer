 package jpaoletti.jpm.test;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
import java.util.Map;
 import jpaoletti.jpm.core.*;
 
 /**
  *
  * @author jpaoletti
  */
 public abstract class DataAccessTest extends AbstractDataAccess {
 
     protected List<Object> list;
 
     protected abstract void fill();
 
     protected int random(int from, int to) {
         return from + (int) (Math.random() * (to - from) + 0.5);
     }
 
     @Override
     public Object getItem(PMContext ctx, String property, String value) throws PMException {
         for (Object object : list) {
             final Object actualValue = PresentationManager.getPm().get(object, property);
             if (actualValue != null && actualValue.toString().equals(value)) {
                 return object;
             }
         }
         return null;
     }
 
     @Override
     public List<?> list(PMContext ctx, EntityFilter filter, ListFilter lfilter, ListSort sort, Integer from, Integer count) throws PMException {
         if (list == null) {
             fill();
         }
         List result = new ArrayList(list);
         if (sort != null) {
             Collections.sort(result, new TestComparator(sort.getFieldId(), sort.isAsc()));
         }
         int f = (from == null) ? 0 : from;
         int c = (int) ((count == null) ? count(ctx) : count);
         System.out.println(String.format("count: %d ; c: %d, f: %d", count(ctx), c, f));
         if (count(ctx) < c) {
             return result;
         }
         if (count(ctx) < f) {
             return null;
         }
         int fpc = f + c;
         final List subList = result.subList(f, (int) ((fpc > count(ctx)) ? count(ctx) : fpc));
        if (filter == null) {
            return subList;
        } else {
            final List finalList = new ArrayList<Object>();
            for (Map.Entry entry : filter.getFilterValues().entrySet()) {
                //TODO apply a filter
            }
            return subList;
        }
     }
 
     @Override
     public Long count(PMContext ctx) throws PMException {
         return new Long(list.size());
     }
 
     @Override
     public void delete(PMContext ctx, Object object) throws PMException {
         list.remove(object);
     }
 
     @Override
     public void update(PMContext ctx, Object instance) throws PMException {
     }
 
     @Override
     public void add(PMContext ctx, Object instance) throws PMException {
         list.add(instance);
     }
 
     @Override
     public Object refresh(PMContext ctx, Object o) throws PMException {
         return o;
     }
 
     @Override
     public EntityFilter createFilter(PMContext ctx) throws PMException {
        return new EntityFilter();
     }
 }
