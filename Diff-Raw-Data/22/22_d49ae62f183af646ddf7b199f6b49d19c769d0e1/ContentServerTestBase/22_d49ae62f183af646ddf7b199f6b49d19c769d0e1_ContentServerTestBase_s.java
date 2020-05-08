 package org.migus.web.content.rest;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assume.assumeNotNull;
 import static org.junit.Assert.fail;
 
 import java.util.List;
 import java.util.UUID;
 
 import javax.ws.rs.WebApplicationException;
 
 import org.junit.Test;
 import org.migus.web.content.ContentTestBase;
 import org.migus.web.content.rest.ContentServer;
 import org.migus.web.content.types.Content;
import org.migus.web.content.types.NewContent;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 
 @ContextConfiguration
 public abstract class ContentServerTestBase extends ContentTestBase {
 	private @Autowired ContentServer contentServer;
 
 	@Test
 	public void testAdd() {
		NewContent newContent=new NewContent();
 		UUID id=UUID.randomUUID();
 
 		newContent.setAuthor(AUTHOR);
 		newContent.setText(CONTENT);
 		newContent.setTitle(TITLE);
 
 		Content content;
 		try {
 			content=contentServer.add(id.toString(), newContent);
 			assertEquals(content.getId(), id.toString());
 			assertEquals(content.getText(), newContent.getText());
 		}
 		catch(WebApplicationException e) {
 			fail("did not expect to get "+e);
 		}
 
 		try {
 			newContent.setTitle(TITLE+" updated");
 			content=contentServer.add(id.toString(), newContent);
 			fail("expected to get WebApplicationException with status 409");
 		}
 		catch(WebApplicationException e) {
 			assumeNotNull(e);
 			assertEquals(409, e.getResponse().getStatus());
 		}
 	}
 
 	@Test
 	public void testGet() {
 		Content content=contentServer.get(getIdOfTestContent().toString());
 		
 		assertEquals(content.getTitle(), TITLE);
 	}
 
 	@Test
 	public void testByAuthor() {
 		List<Content> contents=
 			contentServer.getByAuthor(AUTHOR).getContents();
 
 		assertNotNull(contents);
 		assertEquals(contents.get(0).getTitle(), TITLE);
 	}
 	
 
 	@Test
 	public void testUpdateText() {
 		contentServer.updateText(getIdOfTestContent().toString(), CONTENT);
 	}
 
 	@Test
 	public void testUpdateTitle() {
 		contentServer.updateTitle(getIdOfTestContent().toString(), TITLE);
 	}
 }
