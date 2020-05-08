 /*
  * Copyright (c) 2011 for Jacek Bzdak
  *
  * This file is part of query builder.
  *
  * Query builder is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Query builder is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with Query builder.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package cx.ath.jbzdak.sqlbuilder.dialect;
 
 import cx.ath.jbzdak.sqlbuilder.Alias;
 import cx.ath.jbzdak.sqlbuilder.ParameterSetTwice;
 import cx.ath.jbzdak.sqlbuilder.Select;
 import cx.ath.jbzdak.sqlbuilder.Table;
 import cx.ath.jbzdak.sqlbuilder.dialect.config.DialectConfig;
 import cx.ath.jbzdak.sqlbuilder.dialect.config.DialectConfigKey;
 import cx.ath.jbzdak.sqlbuilder.dialect.config.PrettifySQLLevel;
 import cx.ath.jbzdak.sqlbuilder.literal.LiteralFactory;
 import cx.ath.jbzdak.sqlbuilder.parameter.DefaultParameter;
 import cx.ath.jbzdak.sqlbuilder.parameter.TableParameter;
 import org.junit.Test;
 
 /**
  * Created by: Jacek Bzdak
  */
 public class TestParameterLogic {
 
 
    DefaultDialect defaultDialect;
 
    Select select;
 
    @org.junit.Before
    public void setUp() throws Exception {
 
       DialectConfig dialectConfig = new DialectConfig();
       dialectConfig.setConfig(DialectConfigKey.IDENTIFIER_QUOTING_STRATEGY, IdentifierQuotingStrategy.ALWAYS);
       defaultDialect = new DefaultDialect(dialectConfig);
       defaultDialect.getDialectConfig().setConfig(DialectConfigKey.PRETTIFY_SQL, PrettifySQLLevel.MULTILINE);
 //      literalFactory = defaultDialect.getLiteralFactory();
 
       select = defaultDialect.select();
 //      dp = new Alias("DP");
    }
 
    @Test(expected = cx.ath.jbzdak.sqlbuilder.InvalidParameterException.class)
    public void testSetUnknownParameter() throws Exception {
       select.setParameterValue("foo", "bar");
    }
 
    @Test(expected = ParameterSetTwice.class)
    public void testSetTheSameParameterTwice() throws Exception {
      select.addParameter(new DefaultParameter("foo"));
       select.addParameter(new TableParameter("foo"));
    }
 }
