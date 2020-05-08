 package com.os.sp.search;
 
 public class Search {
     
     public static void main(String[] args) {
         
         Field f1 = new Field("contact.firstName", Type.STRING);
         Field f2 = new Field("contact.lastName", Type.STRING);
         Field f3 = new Field("contact.companyName", Type.STRING);
         Field f4 = new Field("contact.id", Type.INTEGER);
 
        SearchCondition p1 = new Predicate(f1, Operator.STRING_CONTAINS, new Value("ab"));
         
         SearchRequest req = new SearchRequest(SearchArea.CONTACT, p1);
         
         String sql = SQLGenerator.generateSql(req);
         
         System.out.println("Generated sql: " + sql);
         
     }
 
 }
