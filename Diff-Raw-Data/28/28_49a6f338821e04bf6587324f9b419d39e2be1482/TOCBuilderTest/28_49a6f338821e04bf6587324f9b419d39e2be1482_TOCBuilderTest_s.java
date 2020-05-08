 /**
  * Copyright 2011-2012 Alexandre Dutra
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package fr.dutra.confluence2wordpress.core.toc;
 
 import static org.fest.assertions.api.Assertions.*;
 
 import org.apache.commons.io.IOUtils;
 import org.junit.Test;
 import org.mockito.Mockito;
 
 import com.atlassian.confluence.renderer.PageContext;
 
 import fr.dutra.confluence2wordpress.core.author.Author;
 
 
 public class TOCBuilderTest {
 
 	@Test
 	public void test() throws Exception {
 		
 		//Given
 		PageContext pageContext = Mockito.mock(PageContext.class);
 		Mockito.when(pageContext.getPageTitle()).thenReturn("Test");
 		Mockito.when(pageContext.getOriginalContext()).thenReturn(pageContext);
 		String xml = IOUtils.toString(this.getClass().getResourceAsStream("/toc.txt"), "UTF-8");
 		TOCBuilder c = new TOCBuilder();
 		
 		//When
 		Heading toc = c.buildTOC(xml, pageContext);
 		
 		//Then
 		
 		/*
 		 * Expected structure:
 		 *  
 			0 Test [null]*
 	            3 0.0.1 [Test-0.0.1]
 	                4 0.0.1.1 [Test-0.0.1.1]*
 	                4 0.0.1.2 [Test-0.0.1.2]*
 			    1 1 [Test-1]
 		            3 1.0.1 [Test-1.0.1]*
 		 * Nodes marked with an asterisk should have an author.
 		 */
 		
 		assertThat(toc.getLevel()).isEqualTo(0);
 		assertThat(toc.getLabel()).isEqualTo("Test");
 		assertThat(toc.getAnchor()).isNull();
 		assertThat(toc.getAuthor()).isEqualsToByComparingFields(new Author("Author 0", "Author 0", null, null, null));
 		assertThat(toc.getChildren()).hasSize(2);
 
 		Heading _0_0_1 = toc.getChildren().get(0);
 		assertThat(_0_0_1.getLevel()).isEqualTo(3);
		assertThat(_0_0_1.getLabel()).isEqualTo("0.0.1");
		assertThat(_0_0_1.getAnchor()).isEqualTo("Test-0.0.1");
 		assertThat(_0_0_1.getAuthor()).isNull();
 		assertThat(_0_0_1.getChildren()).hasSize(2);
 		
 		Heading _0_0_1_1 = _0_0_1.getChildren().get(0);
 		assertThat(_0_0_1_1.getLevel()).isEqualTo(4);
 		assertThat(_0_0_1_1.getLabel()).isEqualTo("0.0.1.1");
 		assertThat(_0_0_1_1.getAnchor()).isEqualTo("Test-0.0.1.1");
 		assertThat(_0_0_1_1.getAuthor()).isEqualsToByComparingFields(new Author("Author 0.0.1.1", "Author 0.0.1.1", null, null, null));
 		assertThat(_0_0_1_1.getChildren()).isEmpty();
 
 		Heading _0_0_1_2 = _0_0_1.getChildren().get(1);
 		assertThat(_0_0_1_2.getLevel()).isEqualTo(4);
 		assertThat(_0_0_1_2.getLabel()).isEqualTo("0.0.1.2");
 		assertThat(_0_0_1_2.getAnchor()).isEqualTo("Test-0.0.1.2");
 		assertThat(_0_0_1_2.getAuthor()).isEqualsToByComparingFields(new Author("Author 0.0.1.2", "Author 0.0.1.2", null, null, null));
 		assertThat(_0_0_1_2.getChildren()).isEmpty();
 		
 		Heading _1 = toc.getChildren().get(1);
 		assertThat(_1.getLevel()).isEqualTo(1);
 		assertThat(_1.getLabel()).isEqualTo("1");
 		assertThat(_1.getAnchor()).isEqualTo("Test-1");
 		assertThat(_1.getAuthor()).isNull();
 		assertThat(_1.getChildren()).hasSize(1);
 
 		Heading _1_0_1 = _1.getChildren().get(0);
 		assertThat(_1_0_1.getLevel()).isEqualTo(3);
 		assertThat(_1_0_1.getLabel()).isEqualTo("1.0.1");
 		assertThat(_1_0_1.getAnchor()).isEqualTo("Test-1.0.1");
 		assertThat(_1_0_1.getAuthor()).isEqualsToByComparingFields(new Author("Author 1.0.1", "Author 1.0.1", null, null, null));
 		assertThat(_1_0_1.getChildren()).isEmpty();
 	}
 }
