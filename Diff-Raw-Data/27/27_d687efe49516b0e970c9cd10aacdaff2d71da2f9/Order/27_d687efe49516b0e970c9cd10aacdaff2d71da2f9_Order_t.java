 package org.lazydog.data.access.criterion;
 
 
 /**
  * Order.
  * 
  * @author  Ron Rickard
  */
 public class Order {
 
     /**
      * Ascending order.
      * 
      * @param  operand  the operand.
      * 
      * @return  the resulting criterion.
      */
     public static Criterion asc(String operand) {
         return Order.order(OrderDirection.ASC, operand);
     }
     
     /**
      * Descending order.
      * 
     * @param  operand  the operand.
      * 
      * @return  the resulting criterion.
      */
     public static Criterion desc(String operand) {
         return Order.order(OrderDirection.DESC, operand);
     }
            
     /**
      * Order.
      * 
     * @param  orderDirection  the order direction.
     * @param  operand         the operand.
      * 
      * @return  the resulting criterion.
      */
     private static Criterion order(OrderDirection orderDirection,
                                    String operand) {
         
         // Declare.
         Criterion criterion;
 
         // Set the criterion.
         criterion = new Criterion();
         criterion.setOrderDirection(orderDirection);
         criterion.setOperand(operand);
         
         return criterion;
     }
 }
