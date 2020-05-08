 package za.co.yellowfire.carat.web;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.AjaxBehaviorEvent;
 import javax.faces.view.ViewScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import lombok.Getter;
 import lombok.Setter;
 import lombok.extern.slf4j.Slf4j;
 import za.co.yellowfire.carat.db.Item;
 import za.co.yellowfire.carat.db.ItemDao;
 
 @Named
 @ViewScoped @Slf4j
 public class IndexController implements Serializable {
 
     @Inject
     private ItemDao itemDao;
 
 //    @Inject @Metrics
 //    private MetricRegistry metricRegistry;
 
     private List<Item> items;
     private String name;
     @Getter @Setter
     private String title;
     @Getter @Setter
     private int first;
 
     @Getter
     private int increment = 5;
 
     private int rows = 0;
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public List<Item> getItems() {
         if (this.items == null) {
             this.items = itemDao.getItems();
         }
         return items;
     }
 
     public void setItems(List<Item> items) {
         this.items = items;
     }
 
     public void onTitleUpdate(AjaxBehaviorEvent event) {
         System.out.println("event = " + event);
         System.out.println("event = " + getTitle());
     }
 
     public void onSubmit() {
         System.out.println("response");
         //metricRegistry.counter("index.response").inc();
         //return "index";
     }
 
     public int getRows() {
         final int remaining = items.size() - first;
         if (remaining > increment) {
             return increment;
         }
         return remaining;
     }
 
     public int getPageCount() {
         return getItems().size() / increment;
     }
 
     public int getCurrentPage() {
         return (first / increment) + 1;
     }
 
     public Integer[] getPages() {
         final int current = getCurrentPage();
         final int last = getPageCount();
         final List<Integer> pages = new ArrayList<>();
 
         if (current > 1) {
             pages.add(current - 1);
         }
         pages.add(current);
         if (current < last) {
             pages.add(current + 1);
         }
         return pages.toArray(new Integer[pages.size()]);
     }
 
     public String onNext() {
         if (first + increment <= getItems().size() - 1) {
             first = first + increment;
         } else {
             first = items.size() - 1 - increment;
         }
         log.info("next: first={} increment={} size={}", new Object[] {first, rows, getItems().size()});
         return null;
     }
 
     public String onPrevious() {
         if (first - increment <= 0) {
             first = 0;
         } else {
             first = first - increment;
         }
         log.info("next: first={} increment={} size={}", new Object[] {first, rows, getItems().size()});
         return null;
     }
 
     public String onGoto(Integer page) {
         first = (page - 1) * increment;
         return null;
     }
 
     public boolean isNextRendered() {
         return first + increment < items.size();
     }
 
     public boolean isPreviousRendered() {
         return first > 0;
     }
 
     public void onLearnMore(@SuppressWarnings("unused") ActionEvent event) {
         FacesContext.getCurrentInstance().addMessage(
                 null,
                 new FacesMessage(
                         FacesMessage.SEVERITY_INFO,
                         "Sample Info",
                         "This is a detail alert"
                 ));
     }
 
 //    protected BeanManager getBeanManager() {
 //        System.out.println("getBeanManager()");
 //        BeanManager bm = getBeanManagerInServletContext();
 //        if (bm == null) {
 //           bm = getBeanManagerInJndi();
 //        }
 //        return bm;
 //    }
 //
 //    protected BeanManager getBeanManagerInServletContext() {
 //        System.out.println("getBeanManagerInServletContext()");
 //        return (BeanManager)
 //                ((ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext())
 //                        .getAttribute("javax.enterprise.inject.spi.BeanManager");
 //    }
 //
 //    protected BeanManager getBeanManagerInJndi() {
 //        System.out.println("getBeanManagerInJndi()");
 //        try{
 //            return (BeanManager) new InitialContext().lookup("java:comp/env/BeanManager");
 //        } catch (NamingException e) {
 //            return null;
 //        }
 //    }
 
 //    protected MetricRegistry getMetricRegistry() {
 //        BeanManager bm = getBeanManager();
 //        System.out.println("bm = " + bm);
 //        Set<Bean<?>> beans = bm.getBeans(MetricRegistry.class, new AnnotationLiteral<Metrics>() {});
 //        Bean<?> bean = beans.iterator().next();
 //        CreationalContext<?> ctx = bm.createCreationalContext(bean);
 //        return (MetricRegistry) bm.getReference(bean, MetricRegistry.class, ctx);
 //    }
 
 //    protected ItemDao getItemDao() {
 //        System.out.println("getItems()");
 //        BeanManager bm = getBeanManager();
 //        System.out.println("bm = " + bm);
 //        Set<Bean<?>> beans = bm.getBeans(ItemDao.class);
 //        Bean<?> bean = beans.iterator().next();
 //        CreationalContext<?> ctx = bm.createCreationalContext(bean);
 //        return (ItemDao) bm.getReference(bean, ItemDao.class, ctx);
 //    }
 }
