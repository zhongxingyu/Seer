 /*
  * Sonar Erlang Plugin
  * Copyright (C) 2012 Tamás Kende
  * kende.tamas@gmail.com
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 package org.sonar.plugins.erlang.violation;
 
 import static org.junit.Assert.assertThat;
 
 import java.io.IOException;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.hamcrest.Matchers;
 import org.junit.Before;
 import org.junit.Test;
 import org.sonar.plugins.erlang.violations.RuleHandler;
 import org.sonar.plugins.erlang.violations.dialyzer.DialyzerRuleRepository;
 import org.xml.sax.SAXException;
 
 public class RuleHandlerTest {
 
 	private RuleHandler ruleHandler;
 
 	@Before
 	public void setup(){
 		SAXParserFactory factory = SAXParserFactory.newInstance();
 		SAXParser saxParser;
 		ruleHandler = new RuleHandler();
 		try {
 			saxParser = factory.newSAXParser();
 			saxParser.parse(RuleHandlerTest.class.getResourceAsStream(DialyzerRuleRepository.RULES_FILE), ruleHandler);
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Test
 	public void checkRuleHandler(){
		assertThat(ruleHandler.getRules().size(), Matchers.equalTo(42));
 	}
 	
 }
