 /*
  * Copyright (C) 2013 StackFrame, LLC
  * This code is licensed under GPLv2.
  */
 package com.stackframe.sarariman.errors;
 
 import com.google.common.base.Function;
 import com.google.common.collect.ContiguousSet;
 import com.google.common.collect.DiscreteDomain;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Range;
 import com.google.common.collect.Sets;
 import com.stackframe.sarariman.tasks.Task;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 import javax.sql.DataSource;
 
 /**
  *
  * @author mcculley
  */
 public class ErrorsImpl implements Errors {
 
     private final DataSource dataSource;
     private final String mountPoint;
 
     public ErrorsImpl(DataSource dataSource, String mountPoint) {
         this.dataSource = dataSource;
         this.mountPoint = mountPoint;
     }
 
     public Error get(int id) {
         return new ErrorImpl(id, dataSource, mountPoint);
     }
 
     public Iterable<Error> getAll() {
         try {
             Connection connection = dataSource.getConnection();
             try {
                 Statement s = connection.createStatement();
                 try {
                    ResultSet r = s.executeQuery("SELECT id FROM error_log ORDER BY timestamp DESC");
                     try {
                         Collection<Error> c = new ArrayList<Error>();
                         while (r.next()) {
                             c.add(get(r.getInt("id")));
                         }
 
                         return c;
                     } finally {
                         r.close();
                     }
                 } finally {
                     s.close();
                 }
             } finally {
                 connection.close();
             }
         } catch (SQLException se) {
             throw new RuntimeException(se);
         }
     }
 
     public Map<? extends Number, Error> getMap() {
         Set<? extends Number> longKeys = ContiguousSet.create(Range.greaterThan(0L), DiscreteDomain.longs());
         Set<? extends Number> intKeys = ContiguousSet.create(Range.greaterThan(0), DiscreteDomain.integers());
         Set<? extends Number> keys = Sets.union(longKeys, intKeys);
         Function<Number, Error> f = new Function<Number, Error>() {
             public Error apply(Number f) {
                 return get(f.intValue());
             }
 
         };
         return Maps.asMap(keys, f);
     }
 
 }
