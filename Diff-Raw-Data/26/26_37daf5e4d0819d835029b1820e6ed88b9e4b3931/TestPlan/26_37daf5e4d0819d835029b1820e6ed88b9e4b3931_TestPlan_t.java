 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.redhat.nitrate;
 
 /**
  *
  * @author asaleh
  */
 public class TestPlan {
    public Integer plan_id;
     public static class add_tag extends TcmsArrayCommand{
         @RequiredField
         public Integer[] plan_ids;
         @RequiredField
         public String[]  tags;
     }
     public static class check_plan_type extends TcmsArrayCommand{
         @RequiredField
         public String name;
     }
     public static class create extends TcmsHashCommand{
         @RequiredField
         public Integer product;
         @RequiredField
         public String  name;
         @RequiredField
         public Integer type;
         @RequiredField
         public Integer default_product_version;
         @RequiredField
         public String  text;
         public Integer parent;
         public Boolean is_active;
     }
     /*public static class filter extends TcmsCommand{
         TOO COMPLICATED
     }*/
     public static class get extends TcmsArrayCommand{
         @RequiredField
         public Integer id;
     };
     public static class get_change_history extends TestPlan.get{}
     public static class get_env_groups extends TestPlan.get{}
     public static class get_plan_type extends TestPlan.get{}
     public static class get_product extends TestPlan.get{}
     public static class get_tags extends TestPlan.get{}
     public static class get_test_cases extends TestPlan.get{}
     public static class get_test_runs extends TestPlan.get{}
     public static class get_text extends TestPlan.get{}
 
      public static class remove_tag extends TcmsArrayCommand{
         @RequiredField
         public Integer plan_id;
         @RequiredField
         public String  tag;
     }
 
     public static class store_text extends TcmsArrayCommand{
         @RequiredField
         public Integer plan_id;
         @RequiredField
         public String  text;
         public Integer author;
     }
 
     public static class update extends TcmsArrayCommand{
          @RequiredField
          public Integer id;
          public update.Values values=new Values();
          public static class Values extends TcmsHashCommand{
              String name;
              Integer type;
              Integer product;
              Integer default_product_version;
              Integer parent;
              Boolean is_active;
              Integer env_group;
          }
     }
 }
