 /**
  *   textManager, a GUI for managing bills for texter jobs
  *
  *   Copyright (C) 2013 philnate
  *
  *   This file is part of textManager.
  *
  *   textManager is free software: you can redistribute it and/or modify it under the terms of the
  *   GNU General Public License as published by the Free Software Foundation, either version 3 of the
  *   License, or (at your option) any later version.
  *
  *   textManager is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  *   without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  *   See the GNU General Public License for more details. You should have received a copy of the GNU
  *   General Public License along with textManager. If not, see <http://www.gnu.org/licenses/>.
  */
 package me.philnate.textmanager.entities;
 
 import static org.hamcrest.Matchers.allOf;
 import static org.hamcrest.Matchers.containsString;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.fail;
 import me.philnate.textmanager.TestBase;
 import me.philnate.textmanager.entities.Entities;
 import me.philnate.textmanager.entities.Entity;
 import me.philnate.textmanager.entities.EntityInvocationHandler;
 import me.philnate.textmanager.entities.annotations.Id;
 
 import org.junit.Test;
 
 /**
  * tests about {@link Id} resolution in Entities
  * 
  * @author philnate
  * 
  */
 public class _Id extends TestBase {
     private EntityInvocationHandler handler;
 
     @Test
     public void testIdResolution() {
 	handler = new EntityInvocationHandler(Ided.class);
 	Entities.instantiate(Ided.class, handler).setId("1234");
 	assertEquals("1234", handler.container.get("_id"));
     }
 
     @Test
     public void testIdAnnotationResolution() {
 	handler = new EntityInvocationHandler(CustomId.class);
	Entities.instantiate(CustomId.class, handler).setMyId("test").save();
 	assertEquals("test", handler.container.get("_id"));
     }
 
     @Test
     public void testNotAllowedMultipleIdAnnotations() {
 	try {
 	    new EntityInvocationHandler(MultiIdInValid.class);
 	    fail("should throw an IAE exception");
 	} catch (IllegalArgumentException e) {
 	    assertThat(
 		    e.getMessage(),
 		    allOf(containsString("You can only specify one @Id annotation per Document type, but found for"),
 			    containsString("[myId,idMy]")));
 	}
     }
 
     @Test
     public void testIgnoreIdOnGet() {
 	handler = new EntityInvocationHandler(IdOnGet.class);
 	Entities.instantiate(IdOnGet.class, handler).setMyId("id");
 	assertEquals("id", handler.container.get("myId"));
 	assertNull(handler.container.get("_id"));
     }
 
     private static interface IdOnGet extends Entity {
 	public IdOnGet setMyId(String id);
 
 	@Id
 	public String getMyId();
     }
 
     private static interface MultiIdInValid extends Entity {
 	@Id
 	public MultiIdInValid setMyId(String myId);
 
 	@Id
 	public MultiIdInValid setIdMy(String idMy);
     }
 
     private static interface Ided extends Entity {
 	public Ided setId(String id);
     }
 
     private static interface CustomId extends Entity {
 	@Id
 	public CustomId setMyId(String myid);
     }
 }
