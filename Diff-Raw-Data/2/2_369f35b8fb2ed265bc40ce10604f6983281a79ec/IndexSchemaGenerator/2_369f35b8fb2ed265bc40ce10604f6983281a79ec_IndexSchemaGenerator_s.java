 package com.nearinfinity.honeycomb.mysql;
 
 import com.nearinfinity.honeycomb.mysql.gen.IndexSchema;
 import net.java.quickcheck.Generator;
 import net.java.quickcheck.generator.PrimitiveGenerators;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 public class IndexSchemaGenerator implements Generator<IndexSchema> {
     private static final Generator<Integer> lengthGen =
             PrimitiveGenerators.integers(1, 4);
     private static final Random rand = new Random();
     private final List<String> columnNames;
 
 
     public IndexSchemaGenerator(List<String> columnNames) {
         this.columnNames = columnNames;
     }
 
     @Override
     public IndexSchema next() {
         Collections.shuffle(columnNames);
        List<String> columns = columnNames.subList(0, lengthGen.next());
         return new IndexSchema(columns, rand.nextBoolean());
     }
 }
