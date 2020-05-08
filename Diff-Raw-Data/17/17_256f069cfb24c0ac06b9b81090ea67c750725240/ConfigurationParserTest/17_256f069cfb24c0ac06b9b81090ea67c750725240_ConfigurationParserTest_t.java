 /*
  *  This file is part of Cotopaxi.
  *
  *  Cotopaxi is free software: you can redistribute it and/or modify
  *  it under the terms of the Lesser GNU General Public License as published
  *  by the Free Software Foundation, either version 3 of the License, or
  *  any later version.
  *
  *  Cotopaxi is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  Lesser GNU General Public License for more details.
  *
  *  You should have received a copy of the Lesser GNU General Public License
  *  along with Cotopaxi. If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.cotopaxi.config;
 
 import java.io.InputStream;
 
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 import br.octahedron.cotopaxi.config.ConfigurationParser.Token;
 import br.octahedron.cotopaxi.config.ConfigurationParser.TokenType;
 
 /**
  * The objective of this test is verify if the lines from the config file are 
  * being parsed correctly. The basic idea is identify correctly the tokens and 
  * its values.
  * 
  * @author Vítor Avelino - contact@vitoravelino.net
  *
  */
 public class ConfigurationParserTest {
 
 	private ConfigurationParser parser;
 	private InputStream in;
 	
 	@Test
 	public void testProperties() {
 		in = ClassLoader.getSystemResourceAsStream("configTestFiles/properties.config");
 		parser = new ConfigurationParser(in);
 		Token token = parser.nextToken();
 		assertEquals(TokenType.PROPERTIES, token.getTokenType());
 		assertEquals("properties", token.getContent());
 		token = parser.nextToken();
 		assertEquals(TokenType.PROPERTY, token.getTokenType());
 		assertEquals("APPLICATION_BASE_URL", token.getContent());
 		token = parser.nextToken();
 		assertEquals(TokenType.STRING, token.getTokenType());
 		assertEquals("http://localhost:8080", token.getContent());
 		token = parser.nextToken();
 		assertEquals(TokenType.PROPERTY, token.getTokenType());
 		assertEquals("I18N_SUPPORTED_LOCALES", token.getContent());
 		token = parser.nextToken();
 		assertEquals(TokenType.STRING, token.getTokenType());
 		assertEquals("pt_BR", token.getContent());
 	}
 	
 	@Test
 	public void testControllers() {
 		in = ClassLoader.getSystemResourceAsStream("configTestFiles/controllers.config");
 		parser = new ConfigurationParser(in);
 		
 		Token token = parser.nextToken();
 		assertEquals(TokenType.CONTROLLERS, token.getTokenType());
 		assertEquals("controllers", token.getContent());
 		// br.octahedron.cotopaxi.FakeImpl
 		token = parser.nextToken();
 		assertEquals(TokenType.CLASS, token.getTokenType());
 		assertEquals("br.octahedron.cotopaxi.FakeImpl", token.getContent());
 		// 	/test		get	 test
 		token = parser.nextToken();
 		assertEquals(TokenType.URL, token.getTokenType());
 		assertEquals("/test", token.getContent());
 		token = parser.nextToken();
 		assertEquals(TokenType.STRING, token.getTokenType());
 		assertEquals("get", token.getContent());
 		token = parser.nextToken();
 		assertEquals(TokenType.STRING, token.getTokenType());
 		assertEquals("test", token.getContent());
 		token = parser.nextToken();
 		// 	/{user}		get	 showUser
 		assertEquals(TokenType.URL, token.getTokenType());
 		assertEquals("/{user}", token.getContent());
 		token = parser.nextToken();
 		assertEquals(TokenType.STRING, token.getTokenType());
 		assertEquals("get", token.getContent());
 		token = parser.nextToken();
 		assertEquals(TokenType.STRING, token.getTokenType());
 		assertEquals("showUser", token.getContent());
 		
 		// br.octahedron.cotopaxi.FakeTwo
 		token = parser.nextToken();
 		assertEquals(TokenType.CLASS, token.getTokenType());
 		assertEquals("br.octahedron.cotopaxi.FakeTwo", token.getContent());
 		// 	/edit/{user} 	post 	editUser
 		token = parser.nextToken();
 		assertEquals(TokenType.URL, token.getTokenType());
 		assertEquals("/edit/{user}", token.getContent());
 		token = parser.nextToken();
 		assertEquals(TokenType.STRING, token.getTokenType());
 		assertEquals("post", token.getContent());
 		token = parser.nextToken();
 		assertEquals(TokenType.STRING, token.getTokenType());
 		assertEquals("editUser", token.getContent());
 		//	/users.json		get 	index
 		token = parser.nextToken();
 		assertEquals(TokenType.URL, token.getTokenType());
 		assertEquals("/users.json", token.getContent());
 		token = parser.nextToken();
 		assertEquals(TokenType.STRING, token.getTokenType());
 		assertEquals("get", token.getContent());
 		token = parser.nextToken();
 		assertEquals(TokenType.STRING, token.getTokenType());
 		assertEquals("index", token.getContent());
		//	/users/{id}.json		get 	index
		token = parser.nextToken();
		assertEquals(TokenType.URL, token.getTokenType());
		assertEquals("/users/{id}.json", token.getContent());
		token = parser.nextToken();
		assertEquals(TokenType.STRING, token.getTokenType());
		assertEquals("get", token.getContent());
		token = parser.nextToken();
		assertEquals(TokenType.STRING, token.getTokenType());
		assertEquals("index", token.getContent());
 	}
 	
 	@Test
 	public void testBootloaders() {
 		in = ClassLoader.getSystemResourceAsStream("configTestFiles/bootloaders.config");
 		parser = new ConfigurationParser(in);
 		
 		Token token = parser.nextToken();
 		assertEquals(TokenType.BOOTLOADERS, token.getTokenType());
 		assertEquals("bootloaders", token.getContent());
 		token = parser.nextToken();
 		assertEquals(TokenType.CLASS, token.getTokenType());
 		assertEquals("br.octahedron.cotopaxi.fake.FakeClass", token.getContent());
 	}
 	
 	@Test
 	public void testDependencies() {
 		in = ClassLoader.getSystemResourceAsStream("configTestFiles/dependencies.config");
 		parser = new ConfigurationParser(in);
 		
 		Token token = parser.nextToken();
 		assertEquals(TokenType.DEPENDENCIES, token.getTokenType());
 		assertEquals("dependencies", token.getContent());
 		token = parser.nextToken();
 		assertEquals(TokenType.CLASS, token.getTokenType());
 		assertEquals("br.octahedron.cotopaxi.FakeIF", token.getContent());
 		token = parser.nextToken();
 		assertEquals(TokenType.CLASS, token.getTokenType());
 		assertEquals("br.octahedron.cotopaxi.FakeImpl", token.getContent());
 	}
 	
 	@Test
 	public void testInterceptors() {
 		in = ClassLoader.getSystemResourceAsStream("configTestFiles/interceptors.config");
 		parser = new ConfigurationParser(in);
 		
 		Token token = parser.nextToken();
 		assertEquals(TokenType.INTERCEPTORS, token.getTokenType());
 		assertEquals("interceptors", token.getContent());
 		token = parser.nextToken();
 		assertEquals(TokenType.CLASS, token.getTokenType());
 		assertEquals("br.octahedron.cotopaxi.interceptor.FakeControllerInterceptor", token.getContent());
 		token = parser.nextToken();
 		assertEquals(TokenType.CLASS, token.getTokenType());
 		assertEquals("br.octahedron.cotopaxi.interceptor.FakeTemplateInterceptor", token.getContent());
 		token = parser.nextToken();
 		assertEquals(TokenType.CLASS, token.getTokenType());
 		assertEquals("br.octahedron.cotopaxi.interceptor.FakeFinalizerInterceptor", token.getContent());
 	}
 	
 	// TODO examples throwing exceptions
 }
