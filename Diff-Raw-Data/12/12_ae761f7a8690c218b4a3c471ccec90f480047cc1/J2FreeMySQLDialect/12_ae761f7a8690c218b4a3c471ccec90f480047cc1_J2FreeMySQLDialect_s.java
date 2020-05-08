/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

 package org.j2free.jpa;
 
 import org.hibernate.Hibernate;
 import org.hibernate.dialect.MySQLInnoDBDialect;
 import org.hibernate.dialect.function.SQLFunctionTemplate;
 
 /**
  *
  * @author ryan
  */
 public class J2FreeMySQLDialect extends MySQLInnoDBDialect {
 
     public J2FreeMySQLDialect() {
         super();
         registerFunction(
             "date_sub_interval",
             new SQLFunctionTemplate(
                 Hibernate.DATE,
                 "DATE_SUB(?1, INTERVAL ?2 ?3)"
             )
         );
 
         registerFunction(
             "days_ago_date",
             new SQLFunctionTemplate(
                 Hibernate.DATE,
                 "DATE_SUB(CURRENT_DATE, INTERVAL ?1 DAY)"
             )
         );
         
         registerFunction(
             "days_ago_timestamp",
             new SQLFunctionTemplate(
                 Hibernate.DATE,
                 "DATE_SUB(CURRENT_TIMESTAMP, INTERVAL ?1 DAY)"
             )
         );
 
         registerFunction(
             "date_diff",
             new SQLFunctionTemplate(
                 Hibernate.INTEGER,
                 "DATEDIFF(?1,?2)"
             )
         );
     }
 }
