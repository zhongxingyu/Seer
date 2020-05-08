 package db_sample;
 
 public class DbExample {
     public static void main(String[] args) {
         DbSqlCreator<MyDbScheme> sqlCreator = new DbSqlCreator<MyDbScheme>();
 
         sqlCreator.from(Table2Scheme.class).select("table2");
         sqlCreator.from(Table1Scheme.class).select("Table1");
 
        sqlCreator.from(Table1Scheme.class).select("bb");

//        sqlCreator.from()

 //        Table1, Table2, Table3
 //        sqlCreator.table("tableName", TableNum.class) => TableNum
 
 //        sqlCreator.table("hjfahjfa").field("...")
 //        sqlCreator.select("field2").f
 //        sqlCreator.from("humans").select("field2").from("table3");
     }
 }
