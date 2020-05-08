 /*
  * Copyright (c) 2013, Timothy Stack
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  *  Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer.
  *  Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHORS AND CONTRIBUTORS ''AS IS'' AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.sqlitejdbcng;
 
 import com.carrotsearch.junitbenchmarks.BenchmarkRule;
 import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
 import com.carrotsearch.junitbenchmarks.annotation.LabelType;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TestRule;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 
 /**
  * XXX This is just a toy at the moment, it needs more work.
  */
 @BenchmarkHistoryChart(labelWith = LabelType.CUSTOM_KEY)
 public class BasicBenchmarkTest extends SqliteTestHelper {
 
     @Rule
     public TestRule benchmarkRun = new BenchmarkRule();
 
     @Test
     public void testFillTable() throws Exception {
        String name = "small string value";

        this.conn.setAutoCommit(false);
         try (PreparedStatement ps = this.conn.prepareStatement("INSERT INTO test_table VALUES (?, ?)")) {
            for (int lpc = 0; lpc < 10000; lpc++) {
                 ps.setInt(1, 100 + lpc);
                ps.setString(2, name);
                ps.executeUpdate();
             }
         }
        this.conn.commit();
     }
 
     @Test
     public void testFindColumn() throws Exception {
         try (Statement stmt = this.conn.createStatement()) {
             for (int lpc = 0; lpc < 500; lpc++) {
                 try (ResultSet rs = stmt.executeQuery("SELECT * FROM test_table")) {
                     if (rs.next()) {
                         rs.getString("name");
                     }
                 }
             }
         }
     }
 }
