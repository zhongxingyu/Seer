 /*
  * Copyright 2008 JRimum Project
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
  * applicable law or agreed to in writing, software distributed under the
  * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
  * OF ANY KIND, either express or implied. See the License for the specific
  * language governing permissions and limitations under the License.
  * 
  * Created at: 16/02/2010 - 18:40:40
  * 
  * ================================================================================
  * 
  * Direitos autorais 2008 JRimum Project
  * 
  * Licenciado sob a Licença Apache, Versão 2.0 ("LICENÇA"); você não pode usar
  * esse arquivo exceto em conformidade com a esta LICENÇA. Você pode obter uma
  * cópia desta LICENÇA em http://www.apache.org/licenses/LICENSE-2.0 A menos que
  * haja exigência legal ou acordo por escrito, a distribuição de software sob
  * esta LICENÇA se dará “COMO ESTÁ”, SEM GARANTIAS OU CONDIÇÕES DE QUALQUER
  * TIPO, sejam expressas ou tácitas. Veja a LICENÇA para a redação específica a
  * reger permissões e limitações sob esta LICENÇA.
  * 
  * Criado em: 16/02/2010 - 18:40:40
  * 
  */
 
 package org.jrimum.utilix;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * <p>
  * Teste unitário para a classe utilitária de objetos
  * </p>
  * 
  * @author <a href="mailto:romulomail@gmail.com">Rômulo Augusto</a>
  * 
  * @since 0.2
  * 
  * @version 0.2
  *
  */
 public class TestObjects {
 	
 	private static final Object NULL_OBJECT = null;
 	
 	private static final Object EMPTY_OBJECT = new Object();
 	
 	@Test
 	public void testIsNull() {
 		assertTrue(Objects.isNull(NULL_OBJECT));
 		assertFalse(Objects.isNull(EMPTY_OBJECT));
 	}
 	
 	@Test
 	public void testIsNotNull() {
 		assertTrue(Objects.isNotNull(EMPTY_OBJECT));
 		assertFalse(Objects.isNotNull(NULL_OBJECT));
 	}
 	
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void testCheckNull() {
 		Objects.checkNull(EMPTY_OBJECT);
 	}
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void testCheckNullComMensagem() {
 		Objects.checkNull(EMPTY_OBJECT, "Argumento não nulo");
 	}
 	
 	@Test
 	public void testMensagemCheckNullComMensagem() {
 		
 		try {
 			
 			Objects.checkNull(EMPTY_OBJECT, "Argumento não nulo");
 			Assert.fail("Exceção não disparada.");
 			
 		} catch (IllegalArgumentException e) {
 			assertEquals("Argumento não nulo", e.getMessage());
 		}
 	}
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void testCheckNotNull() {
 		Objects.checkNotNull(NULL_OBJECT);
 	}
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void testCheckNotNullComMensagem() {
 		Objects.checkNotNull(NULL_OBJECT, "Argumento nulo");
 	}
 	
 	@Test
 	public void testMensagemChecNotkNullComMensagem() {
 		
 		try {
 			
 			Objects.checkNotNull(null, "Argumento nulo");
 			Assert.fail("Exceção não disparada.");
 			
 		} catch (IllegalArgumentException e) {
 			assertEquals("Argumento nulo", e.getMessage());
 		}
 	}
 	
 	@Test
 	public void testWhenNull1(){
 		
 		assertTrue(null == Objects.whenNull(null, null));
 		assertEquals("ok", Objects.whenNull(null, "ok"));
 		assertEquals("ok", Objects.whenNull("ok", "is ok?"));
 		assertFalse("ok".equals(Objects.whenNull(Boolean.TRUE, "ok")));
 	}
 	
 	@Test
 	public void testWhenNull2(){
 		
 		assertTrue(null == Objects.whenNull(null, null,null));
 		assertEquals("ok:1", Objects.whenNull(null, "ok:1","ok:2"));
 		assertEquals("ok:2", Objects.whenNull("ok", "ok:1","ok:2"));
 		assertFalse("ok".equals(Objects.whenNull(Boolean.TRUE, "ok","nops")));
 	}
 }
