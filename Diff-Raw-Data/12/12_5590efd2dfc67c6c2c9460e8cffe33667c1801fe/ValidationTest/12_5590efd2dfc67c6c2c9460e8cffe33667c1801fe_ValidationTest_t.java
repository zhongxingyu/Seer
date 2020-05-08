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
 package br.octahedron.cotopaxi.validation;
 
 import static junit.framework.Assert.*;
 
 import org.junit.Test;
 
 /**
  * @author Danilo Queiroz - daniloqueiroz@octahedron.com.br
  */
 public class ValidationTest {
 	
 	@Test
 	public void regexRuleTest() {
 		ValidationRule regex = new RegexRule("^(([0-9]{2}|\\([0-9]{2}\\))[ ])?[0-9]{4}[-. ]?[0-9]{4}$");
 		assertTrue(regex.isValid("0000 0000"));
 		assertTrue(regex.isValid("00 0000 0000"));
 		assertTrue(regex.isValid("(00) 0000 0000"));
 		assertTrue(regex.isValid("00000000"));
 		assertTrue(regex.isValid("00 00000000"));
 		
 		regex = new RegexRule("([a-zA-ZáéíóúÁÉÍÓÚÂÊÎÔÛâêîôûÃÕãõçÇ] *){2,}");
 		assertTrue(regex.isValid("Danilo Queiroz"));
 		assertTrue(regex.isValid("Da"));
		regex = new RegexRule(".*");
		assertTrue(regex.isValid(""));
		assertTrue(regex.isValid(null));
 	}
 
 	@Test
 	public void requiredRuleTest() {
 		ValidationRule required = new RequiredRule();
 		assertTrue(required.isValid("a"));
 		assertFalse(required.isValid(""));
 		assertFalse(required.isValid(null));
 	}
 }
