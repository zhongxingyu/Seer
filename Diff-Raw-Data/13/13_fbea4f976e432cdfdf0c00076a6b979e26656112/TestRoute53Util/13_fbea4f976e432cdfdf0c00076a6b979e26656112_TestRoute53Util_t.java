 /*
  *  Straight - A system to manage financial demands for small and decentralized
  *  organizations.
  *  Copyright (C) 2011  Octahedron 
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.figgo.modules.admin.util;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 import static junit.framework.Assert.assertTrue;
 
 import java.io.IOException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.security.spec.InvalidKeySpecException;
 
 import org.junit.Test;
 
 import br.octahedron.figgo.modules.admin.util.Route53Util;
 
 /**
  * @author Danilo Queiroz
  */
 public class TestRoute53Util {
 
 	@Test
 	public void signTest() throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException {
 		assertEquals("4cP0hCJsdCxTJ1jPXo7+e/YSu0g=", Route53Util.sign("Thu, 14 Aug 2008 17:08:48 GMT", "/Ml61L9VxlzloZ091/lkqVV5X1/YvaJtI9hW4Wr9"));
 	}
 
 	@Test
 	public void fetchDateTest() throws IOException {
 		String date = Route53Util.fetchDate();
 		assertNotNull(date);
 		// System.out.println(">"+date+"<");
 	}
 
 	@Test
 	public void generateRequestBodyTest() {
		String body = Route53Util.generateRequestBody("test.figgo.com.br");
 		assertNotNull(body);
 		assertTrue(body.contains("test.figgo.com.br"));
 		// System.out.println("**"+body+"**");
 	}
 }
