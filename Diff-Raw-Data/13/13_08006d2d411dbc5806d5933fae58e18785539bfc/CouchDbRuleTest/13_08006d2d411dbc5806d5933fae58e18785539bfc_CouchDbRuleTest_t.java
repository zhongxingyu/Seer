 /**
  * Copyright (C) cedarsoft GmbH.
  *
  * Licensed under the GNU General Public License version 3 (the "License")
  * with Classpath Exception; you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *         http://www.cedarsoft.org/gpl3ce
  *         (GPL 3 with Classpath Exception)
  *
  * This code is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License version 3 only, as
  * published by the Free Software Foundation. cedarsoft GmbH designates this
  * particular file as subject to the "Classpath" exception as provided
  * by cedarsoft GmbH in the LICENSE file that accompanied this code.
  *
  * This code is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
  * version 3 for more details (a copy is included in the LICENSE file that
  * accompanied this code).
  *
  * You should have received a copy of the GNU General Public License version
  * 3 along with this work; if not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * Please contact cedarsoft GmbH, 72810 Gomaringen, Germany,
  * or visit www.cedarsoft.com if you need additional information or
  * have any questions.
  */
 
 package com.cedarsoft.couchdb.test.utils;
 
import com.cedarsoft.couchdb.ActionFailedException;
 import org.jcouchdb.exception.CouchDBException;
 import org.junit.*;
 import org.junit.runners.model.*;
 
 import static org.fest.assertions.Assertions.assertThat;
 import static org.fest.assertions.Fail.fail;
 
 /**
  * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
  */
 public class CouchDbRuleTest {
   public static final String COUCH_DB_RULE_TEST_SECOND = "couchdbruletest$second";
   public static final String COUCH_DB_RULE_TEST_THIRD = "couchdbruletest$third";
   private FrameworkMethod frameworkMethod;
 
   @Before
   public void setUp() throws Exception {
     frameworkMethod = new FrameworkMethod( getClass().getMethod( "testBasic" ) );
   }
 
   @Test
   public void testBasic() throws Throwable {
     final CouchDbRule rule = new CouchDbRule();
     assertRuleValuesAreNull( rule );
 
     final String[] dbName = {null};
     final boolean[] called = {false};
     rule.apply( new Statement() {
                   @Override
                   public void evaluate() throws Throwable {
                     called[0] = true;
                     assertThat( rule.getCurrentDb() ).isNotNull();
 
                     dbName[0] = rule.getCurrentDb().getDbName();
                     assertThat( rule.getCurrentServer().listDatabases().contains( dbName[0] ) );
 
                     assertThat( rule.createDb( COUCH_DB_RULE_TEST_SECOND ).getDbName() ).isEqualTo( COUCH_DB_RULE_TEST_SECOND );
                     assertThat( rule.createDb( COUCH_DB_RULE_TEST_THIRD ).getDbName() ).isEqualTo( COUCH_DB_RULE_TEST_THIRD );
                   }
                 }, frameworkMethod, this ).evaluate();
     assertThat( called[0] ).isTrue();
 
     assertRuleValuesAreNull( rule );
     assertThat( rule.getCurrentServer().listDatabases().contains( dbName[0] ) ).isFalse();
     assertThat( rule.getCurrentServer().listDatabases().contains( COUCH_DB_RULE_TEST_SECOND ) ).isFalse();
     assertThat( rule.getCurrentServer().listDatabases().contains( COUCH_DB_RULE_TEST_THIRD ) ).isFalse();
   }
 
   @Test
   public void testWrongName() throws Throwable {
     final CouchDbRule rule = new CouchDbRule();
 
     rule.apply( new Statement() {
                   @Override
                   public void evaluate() throws Throwable {
                     try {
                       rule.createDb( "INVALID_NAme/" );
                       fail( "Where is the Exception" );
                    } catch ( ActionFailedException e ) {
                      assertThat( e.getStatus( ) ).isEqualTo( 400 );
                      assertThat( e.getMessage( ) ).startsWith( "400 illegal_database_name:" );
                     }
                   }
                 }, frameworkMethod, this ).evaluate();
   }
 
   private void assertRuleValuesAreNull( CouchDbRule rule ) {
     try {
       rule.getCurrentDb();
       fail( "Where is the Exception" );
     } catch ( IllegalStateException ignore ) {
     }
   }
 }
