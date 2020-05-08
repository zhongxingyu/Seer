 package de.silpion.sommerfest.ejb;
 
 import de.silpion.sommerfest.model.Order;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import javax.persistence.EntityManager;
 import javax.persistence.TypedQuery;
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.sameInstance;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.*;
 
 @RunWith(MockitoJUnitRunner.class)
 public class OrderBeanTest {
 
     @Mock
     private EntityManager em;
 
     @InjectMocks
     private OrderBean orderBean = new OrderBean();
 
     @Test
     public void shouldFindAll() {
         List<Order> expected = new ArrayList<Order>();
         TypedQuery mockTypedQuery = mock(TypedQuery.class);
         when(em.createNamedQuery("Order.findAll", Order.class)).thenReturn(mockTypedQuery);
         when(mockTypedQuery.getResultList()).thenReturn(expected);
 
         List<Order> result = orderBean.findAll();
 
         assertThat(result, is(sameInstance(expected)));
     }
 
     @Test
     public void shouldFindByTarget() {
         List<Order> expected = new ArrayList<Order>();
         String target = "anyTarget";
         TypedQuery mockTypedQuery = mock(TypedQuery.class);
         when(em.createNamedQuery("Order.findByTarget", Order.class)).thenReturn(mockTypedQuery);
         when(mockTypedQuery.setParameter("target", target)).thenReturn(mockTypedQuery);
         when(mockTypedQuery.getResultList()).thenReturn(expected);
 
         List<Order> result = orderBean.findByTarget(target);
 
         assertThat(result, is(sameInstance(expected)));
     }
 
     @Test
     public void shouldDeleteById() {
         Order orderToDelete = new Order();
         when(em.find(Order.class, 42L)).thenReturn(orderToDelete);
 
         orderBean.deleteById(42L);
 
         verify(em).remove(orderToDelete);
     }
 
     @Test
     public void shouldSaveNewOrder() {
         Order order = new Order();
 
         Order result = orderBean.save(order);
 
         assertThat(result, is(order));
         verify(em).persist(order);
     }
 }
