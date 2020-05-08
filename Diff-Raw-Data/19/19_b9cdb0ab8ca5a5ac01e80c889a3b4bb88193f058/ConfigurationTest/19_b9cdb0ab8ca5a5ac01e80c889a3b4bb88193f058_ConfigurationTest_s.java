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
 
 import static junit.framework.Assert.assertEquals;
 
 import java.io.InputStream;
 import java.io.StringBufferInputStream;
 import java.util.NoSuchElementException;
 
 import org.junit.Test;
 
 import br.octahedron.cotopaxi.config.ConfigurationParser.Token;
 import br.octahedron.cotopaxi.config.ConfigurationParser.TokenType;
 import br.octahedron.cotopaxi.interceptor.InterceptorManager;
 import br.octahedron.cotopaxi.route.Router;
 
 /**
  * @author Danilo Queiroz - daniloqueiroz@octahedron.com.br
  */
 @SuppressWarnings("deprecation")
 public class ConfigurationTest {
 
 	@Test(expected = NoSuchElementException.class)
 	public void testParser() {
 		StringBufferInputStream in = new StringBufferInputStream("java.String\n# line comment\nAPPLICATION_BASE_URL http://localhost");
 		ConfigurationParser parser = new ConfigurationParser(in);
 		Token tk = parser.nextToken();
 		assertEquals(TokenType.CLASS, tk.getTokenType());
 
 		tk = parser.nextToken();
 		assertEquals(TokenType.PROPERTY, tk.getTokenType());
 		tk = parser.nextToken();
 		assertEquals(TokenType.STRING, tk.getTokenType());
 		tk = parser.nextToken();
 	}
 
 	@Test
 	public void testLoader() throws Exception {
 		InputStream in = ClassLoader.getSystemResourceAsStream("test.config");
 		ConfigurationLoader loader = new ConfigurationLoader(new Router(), new InterceptorManager(), in);
 		loader.loadConfiguration();
 		// TODO improve this tests
 	}
 }
