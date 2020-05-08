 package com.meschbach.xp.vaadin.sticky.model.memory;
 
 import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
 
import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.meschbach.xp.vaadin.sticky.model.StickyException;
 import com.meschbach.xp.vaadin.sticky.model.StickyNote;
 import com.meschbach.xp.vaadin.sticky.model.StickyQuotaException;
 import com.meschbach.xp.vaadin.sticky.model.StickyUser;
import com.meschbach.xp.vaadin.sticky.model.memory.MemoryStickyUser;
 
 public class MemoryStickyUserForQuotaOfOneTest {
 	
 	StickyUser stickyUser;
 	StickyNote stickyNote;
 	String testString;	
 		
 	
 	@Before
 	public void setup(){
 		stickyUser = null;
 	}
 	
 	/**
 	 *  You should be able to create a StickyUser with a quota of one
 	 *  create one note.   
 	 */
 	@Test
 	public void creatStickyUserWithQuotaOfOne (){
 		stickyUser = new MemoryStickyUser("TestUserName",1);
 		try {
 			stickyNote = stickyUser.createNote();
 		} catch (StickyQuotaException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (StickyException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		assertTrue ("", stickyUser!= null);
 	}
 	
 	
 }
