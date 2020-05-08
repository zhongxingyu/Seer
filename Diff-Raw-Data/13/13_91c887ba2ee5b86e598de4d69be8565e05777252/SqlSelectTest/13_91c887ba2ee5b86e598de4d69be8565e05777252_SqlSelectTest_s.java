 package fr.zenexity.dbhelper;
 
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author yma
  */
 public class SqlSelectTest {
 
     @Test
     public void selectNone() {
         assertEquals("", Sql.select().toString());
         assertEquals("", new Sql.Select().toString());
         assertEquals("", new Sql.Select().select().toString());
     }
 
     @Test
     public void selectOneColumn() {
         assertEquals("SELECT x", Sql.select("x").toString());
         assertEquals("SELECT x", new Sql.Select().select("x").toString());
     }
 
     @Test
     public void selectColumns() {
         assertEquals("SELECT x, y", Sql.select("x", "y").toString());
         assertEquals("SELECT x, y", Sql.select("x").select("y").toString());
         assertEquals("SELECT 1, x, 2, y, 3", Sql.select(1, "x", 2, "y", 3).toString());
         assertEquals("SELECT x, y", new Sql.Select().select("x").select("y").toString());
         assertEquals("SELECT x, y", new Sql.Select().select("x", "y").toString());
         assertEquals("SELECT 1, x, 2, y, 3", new Sql.Select().select(1, "x", 2, "y", 3).toString());
     }
 
     @Test
     public void selectColumnsArray() {
         assertEquals("SELECT x, y", new Sql.Select().select(new Object[] { "x", "y" }).toString());
         assertFalse("SELECT x, y, z".equals(new Sql.Select().select("x", new Object[] { "y", "z" }).toString()));
     }
 
     @Test
     public void selectAllDistinct() {
         assertEquals("SELECT ALL x", Sql.select("x").all().toString());
         assertEquals("SELECT DISTINCT x", Sql.select("x").distinct().toString());
         assertEquals("SELECT ALL x", Sql.select().all().select("x").toString());
         assertEquals("SELECT DISTINCT x", Sql.select().distinct().select("x").toString());
         assertEquals("SELECT ALL x", Sql.select().distinct().select("x").all().toString());
         assertEquals("SELECT DISTINCT x", Sql.select().all().select("x").distinct().toString());
     }
 
     @Test
     public void fromOneTable() {
         assertEquals("FROM x", Sql.from("x").toString());
         assertEquals("FROM x", new Sql.Select().from("x").toString());
     }
 
     @Test
     public void fromTables() {
         assertEquals("FROM x, y", Sql.from("x", "y").toString());
         assertEquals("FROM x, y", Sql.from("x").from("y").toString());
         assertEquals("FROM x, y", new Sql.Select().from("x").from("y").toString());
         assertEquals("FROM x, y", new Sql.Select().from("x", "y").toString());
     }
 
     @Test
     public void fromOneClass() {
         assertEquals("FROM Select", Sql.from(Sql.Select.class).toString());
         assertEquals("FROM Select", new Sql.Select().from(Sql.Select.class).toString());
     }
 
     @Test
     public void fromClasses() {
         assertEquals("FROM Select, Long", Sql.from(Sql.Select.class, Long.class).toString());
         assertEquals("FROM Select, Long", Sql.from(Sql.Select.class).from(Long.class).toString());
         assertEquals("FROM Select, Long", new Sql.Select().from(Sql.Select.class).from(Long.class).toString());
         assertEquals("FROM Select, Long", new Sql.Select().from(Sql.Select.class, Long.class).toString());
     }
 
     @Test
    public void fromSelect() {
         Sql.Select subquery1 = Sql.select("a").where("b", 1);
         Sql.Select subquery2 = Sql.select("c").where("d", 2);
         SqlTest.assertQuery(Sql.from(subquery1, "sq1"), "FROM (SELECT a WHERE b) AS sq1", 1);
         SqlTest.assertQuery(Sql.from(subquery1, "sq1").from(subquery2, "sq2"), "FROM (SELECT a WHERE b) AS sq1, (SELECT c WHERE d) AS sq2", 1, 2);
         SqlTest.assertQuery(new Sql.Select().from(subquery1, "sq1"), "FROM (SELECT a WHERE b) AS sq1", 1);
         SqlTest.assertQuery(new Sql.Select().from(subquery1, "sq1").from(subquery2, "sq2"), "FROM (SELECT a WHERE b) AS sq1, (SELECT c WHERE d) AS sq2", 1, 2);
     }
 
     @Test
     public void joinExpr() {
         SqlTest.assertQuery(Sql.select().join("x"), "JOIN x");
         SqlTest.assertQuery(Sql.select().join("x").join("y"), "JOIN x JOIN y");
         SqlTest.assertQuery(Sql.select().join("x", 1).join("y", 2), "JOIN x JOIN y", 1, 2);
     }
 
     @Test
     public void joinWhere() {
         SqlTest.assertQuery(Sql.select().join("x", Sql.where("i")), "JOIN x ON i");
         SqlTest.assertQuery(Sql.select().join("x", Sql.where("i")).join("y", Sql.where("j")), "JOIN x ON i JOIN y ON j");
         SqlTest.assertQuery(Sql.select().join("x", Sql.where("i", 1)).join("y", Sql.where("j", 2)), "JOIN x ON i JOIN y ON j", 1, 2);
     }
 
     @Test
     public void innerJoinExpr() {
         SqlTest.assertQuery(Sql.select().innerJoin("x"), "INNER JOIN x");
         SqlTest.assertQuery(Sql.select().innerJoin("x").innerJoin("y"), "INNER JOIN x INNER JOIN y");
         SqlTest.assertQuery(Sql.select().innerJoin("x", 1).innerJoin("y", 2), "INNER JOIN x INNER JOIN y", 1, 2);
     }
 
     @Test
     public void innerJoinWhere() {
         SqlTest.assertQuery(Sql.select().innerJoin("x", Sql.where("i")), "INNER JOIN x ON i");
         SqlTest.assertQuery(Sql.select().innerJoin("x", Sql.where("i")).innerJoin("y", Sql.where("j")), "INNER JOIN x ON i INNER JOIN y ON j");
         SqlTest.assertQuery(Sql.select().innerJoin("x", Sql.where("i", 1)).innerJoin("y", Sql.where("j", 2)), "INNER JOIN x ON i INNER JOIN y ON j", 1, 2);
     }
 
     @Test
     public void leftJoinExpr() {
         SqlTest.assertQuery(Sql.select().leftJoin("x"), "LEFT JOIN x");
         SqlTest.assertQuery(Sql.select().leftJoin("x").leftJoin("y"), "LEFT JOIN x LEFT JOIN y");
         SqlTest.assertQuery(Sql.select().leftJoin("x", 1).leftJoin("y", 2), "LEFT JOIN x LEFT JOIN y", 1, 2);
     }
 
     @Test
     public void leftJoinWhere() {
         SqlTest.assertQuery(Sql.select().leftJoin("x", Sql.where("i")), "LEFT JOIN x ON i");
         SqlTest.assertQuery(Sql.select().leftJoin("x", Sql.where("i")).leftJoin("y", Sql.where("j")), "LEFT JOIN x ON i LEFT JOIN y ON j");
         SqlTest.assertQuery(Sql.select().leftJoin("x", Sql.where("i", 1)).leftJoin("y", Sql.where("j", 2)), "LEFT JOIN x ON i LEFT JOIN y ON j", 1, 2);
     }
 
     @Test
     public void where() {
         SqlTest.assertQuery(Sql.select().where("x", 1, 2, 3), "x", 1, 2, 3);
         SqlTest.assertQuery(Sql.select().where("x", 1, 2, 3).where("y", 4, 5, 6), "x AND y", 1, 2, 3, 4, 5, 6);
         assertEquals("FROM i WHERE x", Sql.select().from("i").where("x").toString());
         assertEquals("FROM i WHERE x AND y", Sql.select().from("i").where("x").where("y").toString());
         assertEquals("SELECT i FROM j WHERE x", Sql.select().select("i").from("j").where("x").toString());
     }
 
     @Test
     public void andWhere() {
         SqlTest.assertQuery(Sql.select().andWhere("x", 1, 2, 3), "x", 1, 2, 3);
         SqlTest.assertQuery(Sql.select().andWhere("x", 1, 2, 3).andWhere("y", 4, 5, 6), "x AND y", 1, 2, 3, 4, 5, 6);
         assertEquals("FROM i WHERE x", Sql.select().from("i").andWhere("x").toString());
         assertEquals("FROM i WHERE x AND y", Sql.select().from("i").andWhere("x").andWhere("y").toString());
         assertEquals("SELECT i FROM j WHERE x", Sql.select().select("i").from("j").andWhere("x").toString());
     }
 
     @Test
     public void orWhere() {
         SqlTest.assertQuery(Sql.select().orWhere("x", 1, 2, 3), "x", 1, 2, 3);
         SqlTest.assertQuery(Sql.select().orWhere("x", 1, 2, 3).orWhere("y", 4, 5, 6), "x OR y", 1, 2, 3, 4, 5, 6);
         assertEquals("FROM i WHERE x", Sql.select().from("i").orWhere("x").toString());
         assertEquals("FROM i WHERE x OR y", Sql.select().from("i").orWhere("x").orWhere("y").toString());
         assertEquals("SELECT i WHERE x", Sql.select().select("i").orWhere("x").toString());
         assertEquals("SELECT i FROM j WHERE x", Sql.select().select("i").from("j").orWhere("x").toString());
     }
 
     @Test
     public void subWhere() {
         Sql.Where x = Sql.where("x", 1, 2, 3);
         SqlTest.assertQuery(Sql.select().where(x).where(x), "(x) AND (x)", 1, 2, 3, 1, 2, 3);
         SqlTest.assertQuery(Sql.select().orWhere(x).orWhere(x), "(x) OR (x)", 1, 2, 3, 1, 2, 3);
         SqlTest.assertQuery(Sql.select().andWhere(x).andWhere(x), "(x) AND (x)", 1, 2, 3, 1, 2, 3);
     }
 
     @Test
     public void groupBy() {
         assertEquals("GROUP BY x", Sql.select().groupBy("x").toString());
         assertEquals("GROUP BY x, y", Sql.select().groupBy("x").groupBy("y").toString());
         assertEquals("GROUP BY x, y", Sql.select().groupBy("x", "y").toString());
         assertEquals("GROUP BY 1, x, 2, y, 3", Sql.select().groupBy(1, "x", 2, "y", 3).toString());
     }
 
     @Test
     public void having() {
         SqlTest.assertQuery(Sql.select().having("x", 1, 2, 3), "HAVING x", 1, 2, 3);
         SqlTest.assertQuery(Sql.select().having("x", 1, 2, 3).having("y", 4, 5, 6), "HAVING x AND y", 1, 2, 3, 4, 5, 6);
         assertEquals("FROM i HAVING x", Sql.select().from("i").having("x").toString());
         assertEquals("FROM i HAVING x AND y", Sql.select().from("i").having("x").having("y").toString());
     }
 
     @Test
     public void andHaving() {
         SqlTest.assertQuery(Sql.select().andHaving("x", 1, 2, 3), "HAVING x", 1, 2, 3);
         SqlTest.assertQuery(Sql.select().andHaving("x", 1, 2, 3).andHaving("y", 4, 5, 6), "HAVING x AND y", 1, 2, 3, 4, 5, 6);
         assertEquals("FROM i HAVING x", Sql.select().from("i").andHaving("x").toString());
         assertEquals("FROM i HAVING x AND y", Sql.select().from("i").andHaving("x").andHaving("y").toString());
     }
 
     @Test
     public void orHaving() {
         SqlTest.assertQuery(Sql.select().orHaving("x", 1, 2, 3), "HAVING x", 1, 2, 3);
         SqlTest.assertQuery(Sql.select().orHaving("x", 1, 2, 3).orHaving("y", 4, 5, 6), "HAVING x OR y", 1, 2, 3, 4, 5, 6);
         assertEquals("FROM i HAVING x", Sql.select().from("i").orHaving("x").toString());
         assertEquals("FROM i HAVING x OR y", Sql.select().from("i").orHaving("x").orHaving("y").toString());
     }
 
     @Test
     public void subHaving() {
         Sql.Where x = Sql.where("x", 1, 2, 3);
         SqlTest.assertQuery(Sql.select().having(x).having(x), "HAVING (x) AND (x)", 1, 2, 3, 1, 2, 3);
         SqlTest.assertQuery(Sql.select().orHaving(x).orHaving(x), "HAVING (x) OR (x)", 1, 2, 3, 1, 2, 3);
         SqlTest.assertQuery(Sql.select().andHaving(x).andHaving(x), "HAVING (x) AND (x)", 1, 2, 3, 1, 2, 3);
     }
 
     @Test
     public void orderBy() {
         assertEquals("ORDER BY x", Sql.select().orderBy("x").toString());
         assertEquals("ORDER BY x, y", Sql.select().orderBy("x").orderBy("y").toString());
         assertEquals("ORDER BY x, y", Sql.select().orderBy("x", "y").toString());
         assertEquals("ORDER BY 1, x, 2, y, 3", Sql.select().orderBy(1, "x", 2, "y", 3).toString());
     }
 
     @Test
     public void offset() {
         assertEquals("OFFSET 1", Sql.select().offset(1).toString());
     }
 
     @Test(expected=NullPointerException.class)
     public void offsetError() {
         Sql.select().offset(1).offset(2).toString();
     }
 
     @Test
     public void limit() {
         assertEquals("LIMIT 1", Sql.select().limit(1).toString());
     }
 
     @Test(expected=NullPointerException.class)
     public void limitError() {
         Sql.select().limit(1).limit(2).toString();
     }
 
     @Test
     public void params() {
         SqlTest.assertQuery(Sql.select()
                     .andWhere("f", 6, "f")
                     .orWhere("g", 7, "g")
                     .join("c", 3, "c")
                     .innerJoin("d", 4, "d")
                     .leftJoin("e", 5, "e"),
                 "JOIN c INNER JOIN d LEFT JOIN e WHERE f OR g",
                 3, "c", 4, "d", 5, "e", 6, "f", 7, "g");
     }
 
     @Test
     public void trim() {
         assertEquals("SELECT * FROM table", Sql.select("*").from("table").toString());
         assertEquals("SELECT * FROM table GROUP BY a", Sql.select("*").from("table").groupBy("a").toString());
     }
 
     @Test
     public void testString() {
         assertEquals(
             "SELECT a, b, c FROM table INNER JOIN innerJoin LEFT JOIN leftJoin" +
             " WHERE andWhere OR orWhere GROUP BY groupBy" +
             " HAVING andHaving OR orHaving ORDER BY orderBy LIMIT 123",
             Sql
                 .select("a", "b", "c")
                 .from("table")
                 .innerJoin("innerJoin")
                 .leftJoin("leftJoin")
                 .andWhere("andWhere")
                 .orWhere("orWhere")
                 .groupBy("groupBy")
                 .andHaving("andHaving")
                 .orHaving("orHaving")
                 .orderBy("orderBy")
                 .limit(123)
                 .toString());
     }
 
     @Test
     public void testCloned() {
         assertEquals(
             "SELECT DISTINCT a, b, c FROM t1, t2 INNER JOIN ij1 INNER JOIN ij2 LEFT JOIN lj1 LEFT JOIN lj2" +
             " WHERE w1 AND w2 OR w3 GROUP BY g1, g2" +
             " HAVING h1 OR h2 AND h3 ORDER BY o1, o2 OFFSET 12 LIMIT 123",
             new Sql.Select(Sql
                 .selectDistinct("a", "b", "c")
                 .from("t1", "t2")
                 .innerJoin("ij1").innerJoin("ij2")
                 .leftJoin("lj1").leftJoin("lj2")
                 .andWhere("w1")
                 .andWhere("w2")
                 .orWhere("w3")
                 .groupBy("g1", "g2")
                 .having("h1")
                 .orHaving("h2")
                 .andHaving("h3")
                 .orderBy("o1", "o2")
                 .offset(12)
                 .limit(123))
                 .toString());
     }
 
 }
