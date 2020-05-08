 /*===========================================================================
   Copyright (C) 2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.common;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
 
 public class RegexUtilTest {
 
 	@Test
 	public void testReplaceAll() {
 		
 		assertEquals("e{1@^}ddddd{2@^5}+", RegexUtil.replaceAll("e{1,}ddddd{2,5}+", "\\{.*?(,).*?\\}", 1, "@^"));
 	}
 	
 	@Test
 	public void testCountMatches() {
 		
 		assertEquals(3, RegexUtil.countMatches("1 text 2 text 1 text 1 text 2", "1"));
 		assertEquals(2, RegexUtil.countMatches("1 text 2 text 1 text 1 text 2", "2"));
 	}
 	
 	@Test
 	public void testCountQualifiers() {
 		
 		assertEquals(3, RegexUtil.countLeadingQualifiers("\"text, \"text\", text,\"text\"\"\"", "\""));
 		assertEquals(4, RegexUtil.countTrailingQualifiers("\"text, \"text\", text,\"text\"\"\"", "\""));
		assertEquals(3, RegexUtil.countLeadingQualifiers("\"\u0432\u0430\u0432\u044b, " +
				"\"\u044b\u0432\u044b\u0432\u0430\u044b\u0432\u0432\u0430\", \u044b\u0432\u0430\u0430\u0430," +
				"\"\u044b\u0444\u044b\u0432\u044b\"\"\"", 
				"\""));
		assertEquals(4, RegexUtil.countTrailingQualifiers("\"\u0432\u0430\u0432\u044b, " +
				"\"\u044b\u0432\u044b\u0432\u0430\u044b\u0432\u0432\u0430\", \u044b\u0432\u0430\u0430\u0430," +
				"\"\u044b\u0444\u044b\u0432\u044b\"\"\"",
				"\""));
 	}
 	
 }
