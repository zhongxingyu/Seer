 
 package com.github.shimal.query_utils.hql;
 
 import com.github.shimal.query_utils.AliasAlreadyUsedException;
 import com.github.shimal.query_utils.Constrainable;
 import com.github.shimal.query_utils.Orderable;
 import com.github.shimal.query_utils.Querable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 
 
 public class HqlQuery implements Querable {
 
 
 
     //~ --- [INSTANCE FIELDS] ------------------------------------------------------------------------------------------
 
     private String                  countParam;
     private HashMap<String, String> entities;
     private List<Orderable>         orders;
     private String                  selectParam;
     private Constrainable           topConstrainable;
 
 
 
     //~ --- [CONSTRUCTORS] ---------------------------------------------------------------------------------------------
 
     public HqlQuery(Class entity) {
 
         this(entity, "s");
     }
 
 
 
     public HqlQuery(Class entity, String alias) {
 
         entities         = new HashMap<String, String>();
         orders           = new ArrayList<Orderable>();
         topConstrainable = new HqlAnd();
         topConstrainable = null;
         countParam       = alias;
         selectParam      = alias;
 
         entities.put(alias, entity.getSimpleName());
     }
 
 
 
     //~ --- [METHODS] --------------------------------------------------------------------------------------------------
 
     @Override
     public Querable asc(String column) {
 
         this.orders.add(new HqlOrder(column, ORDER_ASCENDING));
 
         return this;
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     @Override
     public String count() {
 
         return "select count(" + countParam + ") from " + generateSelectWoEntity();
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     @Override
     public Querable desc(String column) {
 
         this.orders.add(new HqlOrder(column, ORDER_DESCENDING));
 
         return this;
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     @Override
     public String getCountParam() {
 
         return countParam;
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     @Override
     public String getSelectParam() {
 
         return selectParam;
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     public Querable join(Class entity, String alias) throws AliasAlreadyUsedException {
 
         if (entities.containsKey(alias)) {
             throw new AliasAlreadyUsedException();
         }
 
         entities.put(alias, entity.getSimpleName());
 
         return this;
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     public Querable join(Class entity, String alias, Constrainable constraint) throws AliasAlreadyUsedException {
 
         if (entities.containsKey(alias)) {
             throw new AliasAlreadyUsedException();
         }
 
         entities.put(alias, entity.getSimpleName());
         where(constraint);
 
         return this;
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     public Querable join(Class entity, String alias, Object leftSide, Object rightSide)
         throws AliasAlreadyUsedException {
 
         if (entities.containsKey(alias)) {
             throw new AliasAlreadyUsedException();
         }
 
         entities.put(alias, entity.getSimpleName());
         where(leftSide, rightSide);
 
         return this;
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     @Override
     public Querable order(Orderable... orders) {
 
         this.orders.addAll(Arrays.asList(orders));
 
         return this;
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     @Override
     public String select() {
 
         return "select " + selectParam + " from " + generateSelectWoEntity() + generateOrders();
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     @Override
     public void setCountParam(String countParam) {
 
         this.countParam = countParam;
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     @Override
     public void setSelectParam(String selectParam) {
 
         this.selectParam = selectParam;
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     @Override
     public Querable where(Constrainable constrainable) {
 
         if (topConstrainable == null) {
 
             if (constrainable instanceof HqlConstraint) {
                 topConstrainable = new HqlAnd(constrainable);
             } else {
                 topConstrainable = constrainable;
             }
         } else {
             topConstrainable.add(constrainable);
         }
 
         return this;
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     @Override
     public Querable where(Object leftSide, Object rightSide) {
 
         HqlConstraint constraint = new HqlConstraint(leftSide.toString(), rightSide.toString());
 
         if (topConstrainable == null) {
             topConstrainable = new HqlAnd(constraint);
         } else {
             topConstrainable.add(constraint);
         }
 
         return this;
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     private String generateOrders() {
 
         String query = "";
 
         for (Orderable order : orders) {
             query += ", " + order.getColumn() + (order.getMethod() == ORDER_ASCENDING ? " asc" : " desc");
         }
 
         if (!query.isEmpty()) {
             query = " order by " + query.substring(2);
         }
 
         return query;
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     private String generateSelectWoEntity() {
 
         String query = "";
 
         for (String alias : entities.keySet()) {
             query += ", " + entities.get(alias) + " " + alias;
         }
 
         query = query.substring(2);
 
        if (topConstrainable != null && topConstrainable.size() > 0) {
             query += " where " + wh(topConstrainable);
         }
 
         return query;
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     private String wh(Constrainable constrainable) {
 
         String                  query      = "";
         Iterator<Constrainable> iterator   = constrainable.getIterator();
         String                  whereQuery = "";
 
         if (constrainable instanceof HqlConstraint) {
             HqlConstraint constraint = (HqlConstraint) constrainable;
 
             if (constraint.getOperator() == HqlConstraint.EQUAL) {
                 query += constraint.getLeftSide() + " = " + constraint.getRightSide();
             } else if (constraint.getOperator() == HqlConstraint.NOT_EQUAL) {
                 query += constraint.getLeftSide() + " != " + constraint.getRightSide();
             } else if (constraint.getOperator() == HqlConstraint.LIKE) {
                 query += constraint.getLeftSide() + " LIKE " + constraint.getRightSide();
             } else if (constraint.getOperator() == HqlConstraint.LIKE_LOWER) {
                 query += "lower(" + constraint.getLeftSide() + ") like lower('" + constraint.getRightSide() + "')";
             } else if (constraint.getOperator() == HqlConstraint.LESS_THAN) {
                 query += constraint.getLeftSide() + " < " + constraint.getRightSide();
             } else if (constraint.getOperator() == HqlConstraint.LESS_THAN_OR_EQUAL) {
                 query += constraint.getLeftSide() + " <= " + constraint.getRightSide();
             } else if (constraint.getOperator() == HqlConstraint.GREATER_THAN) {
                 query += constraint.getLeftSide() + " > " + constraint.getRightSide();
             } else if (constraint.getOperator() == HqlConstraint.GREATER_THAN_OR_EQUAL) {
                 query += constraint.getLeftSide() + " >= " + constraint.getRightSide();
             }
         } else if (constrainable instanceof HqlAnd) {
 
             while (iterator.hasNext()) {
                 whereQuery += " and " + wh(iterator.next());
             }
 
             query += "(" + whereQuery.substring(5) + ")";
         } else if (constrainable instanceof HqlOr) {
 
             while (iterator.hasNext()) {
                 whereQuery += " or " + wh(iterator.next());
             }
 
             query += "(" + whereQuery.substring(4) + ")";
         }
 
         return query;
     }
 }
