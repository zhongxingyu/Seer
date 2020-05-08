 package org.oobium.persist.db;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 
 import org.junit.Ignore;
 import org.junit.Test;
 import org.oobium.framework.tests.dyn.DynModel;
 import org.oobium.framework.tests.dyn.DynClasses;
 import org.oobium.persist.Model;
 import org.oobium.persist.Text;
 
 public class CreateTests extends BaseDbTestCase {
 
 	@Test
 	public void testAttrString() throws Exception {
 		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class");
 		
 		migrate(am);
 
 		Model a = am.newInstance();
 		a.set("name", "bob");
 		a.create();
 		
 		assertFalse(a.getErrors().toString(), a.hasErrors());
 		
 		assertEquals("bob", persistService.executeQueryValue("SELECT name from a_models where id=?", 1));
 	}
 	
 	@Test
 	public void testAttrBoolean() throws Exception {
 		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("success", "Boolean.class").addAttr("failure", "Boolean.class").addAttr("huh", "Boolean.class");
 		
 		migrate(am);
 
 		Model a = am.newInstance();
 		a.set("success", true);
 		a.set("failure", false);
 		a.set("huh", null);
 		a.create();
 		
 		assertFalse(a.getErrors().toString(), a.hasErrors());
 		
 		assertEquals(true, persistService.executeQueryValue("SELECT success from a_models where id=?", 1));
 		assertEquals(false, persistService.executeQueryValue("SELECT failure from a_models where id=?", 1));
 		assertEquals(null, persistService.executeQueryValue("SELECT huh from a_models where id=?", 1));
 	}
 	
 	@Test
 	public void testAttrBoolean_Primitive() throws Exception {
 		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("success", "boolean.class").addAttr("failure", "boolean.class").addAttr("huh", "boolean.class");
 		
 		migrate(am);
 
 		Model a = am.newInstance();
 		a.set("success", true);
 		a.set("failure", false);
 		a.set("huh", null);
 		a.create();
 		
 		assertFalse(a.getErrors().toString(), a.hasErrors());
 		
 		assertEquals(true, persistService.executeQueryValue("SELECT success from a_models where id=?", 1));
 		assertEquals(false, persistService.executeQueryValue("SELECT failure from a_models where id=?", 1));
 		assertEquals(false, persistService.executeQueryValue("SELECT huh from a_models where id=?", 1));
 	}
 	
 	@Test
 	public void testAttrText() throws Exception {
 		DynModel am = DynClasses.getModel(pkg, "AModel").addImport(Text.class).addAttr("attr", "Text.class");
 		
 		migrate(am);
 
 		Model a = am.newInstance();
 		a.set("attr", "hello");
 		a.create();
 		
 		assertFalse(a.getErrors().toString(), a.hasErrors());
 		
 		assertEquals("hello", persistService.executeQueryValue("SELECT attr from a_models where id=?", 1));
 	}
 	
 	@Test
 	public void testAttr_Empty() throws Exception {
 		
 		// creating a model with no attributes set currently throws an exception... should it be this way?
 		
 		DynModel am = DynClasses.getModel(pkg, "AModel").addAttr("name", "String.class");
 		
 		migrate(am);
 
 		Model a = am.newInstance();
 		a.create();
 		
 		assertTrue(a.hasErrors());
 		
 		assertEquals(1, a.getErrorCount());
		assertTrue(a.getError(0), a.getError(0).startsWith("java.sql.SQLException: can not create an empty model:"));
 	}
 	
 	@Test
 	public void testHasOne() throws Exception {
 		DynModel am = DynClasses.getModel(pkg, "AModel").addHasOne("bModel", "BModel.class");
 		DynModel bm = DynClasses.getModel(pkg, "BModel").addAttr("name", "String.class");
 		
 		migrate(am, bm);
 
 		Model b = spy(bm.newInstance());
 		b.set("name", "bob");
 		
 		Model a = am.newInstance();
 		a.set("bModel", b);
 		a.create();
 		
 		assertFalse(a.getErrors().toString(), a.hasErrors());
 		
 		assertEquals(1, persistService.executeQueryValue("SELECT b_model FROM a_models WHERE id=?", 1));
 		assertEquals("bob", persistService.executeQueryValue("SELECT name FROM b_models WHERE id=?", 1));
 	}
 
 	@Test
 	public void testHasOne_LinkBack_OneSide() throws Exception {
 		DynModel am = DynClasses.getModel(pkg, "AModel").addHasOne("bModel", "BModel.class");
 		DynModel bm = DynClasses.getModel(pkg, "BModel").addHasOne("aModel", "AModel.class");
 		
 		migrate(am, bm);
 
 		Model b = spy(bm.newInstance());
 		Model a = am.newInstance();
 		a.set("bModel", b);
 		a.create();
 		
 		assertFalse(a.getErrors().toString(), a.hasErrors());
 
 		assertEquals(1, persistService.executeQueryValue("SELECT b_model from a_models where id=?", 1));
 		assertEquals(1, count("b_models"));
 		
 		verify(b, never()).create();
 	}
 
 	@Ignore
 	@Test
 	public void testHasOne_LinkBack_BothSides() throws Exception {
 		
 		// Requires 3 steps: insert AModel with null key, insert BModel, then update AModel... not yet implemented
 		
 		DynModel am = DynClasses.getModel(pkg, "AModel").addHasOne("bModel", "BModel.class");
 		DynModel bm = DynClasses.getModel(pkg, "BModel").addHasOne("aModel", "AModel.class");
 		
 		migrate(am, bm);
 
 //		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(null)");
 //		persistService.executeUpdate("INSERT INTO b_models(a_model) VALUES(?)", 1);
 //		persistService.executeUpdate("UPDATE a_models SET b_model=? WHERE id=?", 1, 1);
 
 		Model b = spy(bm.newInstance());
 		Model a = am.newInstance();
 		b.set("aModel", a);
 		a.set("bModel", b);
 		a.create();
 		
 		assertFalse(a.getErrors().toString(), a.hasErrors());
 
 		assertEquals(1, persistService.executeQueryValue("SELECT b_model from a_models where id=?", 1));
 		assertEquals(1, count("b_models"));
 		
 		verify(b, never()).create();
 	}
 
 	@Test
 	public void testHasOneToOne() throws Exception {
 		DynModel am = DynClasses.getModel(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModel\"");
 		DynModel bm = DynClasses.getModel(pkg, "BModel").timestamps().addHasOne("aModel", "AModel.class", "opposite=\"bModel\"");
 
 		migrate(am, bm);
 
 		Model b = spy(bm.newInstance());
 		Model a = am.newInstance();
 		a.set("bModel", b);
 		a.create();
 		
 		assertFalse(a.getErrors().toString(), a.hasErrors());
 
 		assertEquals(1, persistService.executeQueryValue("SELECT b_model from a_models where id=?", 1));
 		assertEquals(1, count("b_models"));
 		
 		verify(b, never()).create();
 	}
 	
 	@Test
 	public void testHasOneToOne_FromNonKey() throws Exception {
 		DynModel am = DynClasses.getModel(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModel\"");
 		DynModel bm = DynClasses.getModel(pkg, "BModel").timestamps().addHasOne("aModel", "AModel.class", "opposite=\"bModel\"");
 
 		migrate(am, bm);
 
 //		persistService.executeUpdate("INSERT INTO b_models(created_at) VALUES(?)", new Timestamp(System.currentTimeMillis()));
 //		persistService.executeUpdate("INSERT INTO a_models(b_model) VALUES(?)", 1);
 
 		Model b = bm.newInstance();
 		Model a = spy(am.newInstance());
 		a.set("bModel", b);
 		b.create();
 		
 		assertFalse(a.getErrors().toString(), a.hasErrors());
 
 		assertEquals(1, persistService.executeQueryValue("SELECT b_model from a_models where id=?", 1));
 		assertEquals(1, count("b_models"));
 		
 		verify(a, never()).create();
 	}
 	
 	@Test
 	public void testHasOneToMany() throws Exception {
 		DynModel am = DynClasses.getModel(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModels\"");
 		DynModel bm = DynClasses.getModel(pkg, "BModel").timestamps().addHasMany("aModels", "AModel.class", "opposite=\"bModel\"");
 
 		migrate(am, bm);
 
 		Model a = am.newInstance();
 		Model b = spy(bm.newInstance());
 		a.set("bModel", b);
 		a.create();
 		
 		assertFalse(a.getErrors().toString(), a.hasErrors());
 
 		assertEquals(1, persistService.executeQueryValue("SELECT b_model from a_models where id=?", 1));
 		assertEquals(1, count("b_models"));
 		
 		verify(b, never()).create();
 	}
 
 	@Test
 	public void testHasManyToOne() throws Exception {
 		// same as testHasOneToMany, except save the from the "many" side
 		DynModel am = DynClasses.getModel(pkg, "AModel").timestamps().addHasOne("bModel", "BModel.class", "opposite=\"aModels\"");
 		DynModel bm = DynClasses.getModel(pkg, "BModel").timestamps().addHasMany("aModels", "AModel.class", "opposite=\"bModel\"");
 
 		migrate(am, bm);
 
 		Model a = spy(am.newInstance());
 		Model b = bm.newInstance();
 		b.set("aModels", new Model[] { a });
 		a.get("bModel");
 		b.create();
 		
 		assertFalse(a.getErrors().toString(), a.hasErrors());
 
 		assertEquals(1, persistService.executeQueryValue("SELECT b_model from a_models where id=?", 1));
 		assertEquals(1, count("b_models"));
 		
 		verify(a, never()).create();
 	}
 
 	@Test
 	public void testHasManyToNone() throws Exception {
 		DynModel am = DynClasses.getModel(pkg, "AModel").timestamps().addHasMany("bModels", "BModel.class");
 		DynModel bm = DynClasses.getModel(pkg, "BModel").timestamps().addHasOne("aModel", "AModel.class");
 
 		migrate(am, bm);
 
 		Model b = spy(bm.newInstance());
 		Model a = am.newInstance();
 		a.set("bModels", new Model[] { b });
 		a.create();
 		
 		assertFalse(a.getErrors().toString(), a.hasErrors());
 
 		assertEquals(1, count("a_models"));
 		assertEquals(1, count("b_models"));
 		assertEquals(1, persistService.executeQueryValue("SELECT a from a_models__b_models___b_models__null where b=?", 1));
 		
 		verify(b, never()).create();
 	}
 
 	@Test
 	public void testHasManyToMany() throws Exception {
 		DynModel am = DynClasses.getModel(pkg, "AModel").timestamps().addHasMany("bModels", "BModel.class", "opposite=\"aModels\"");
 		DynModel bm = DynClasses.getModel(pkg, "BModel").timestamps().addHasMany("aModels", "AModel.class", "opposite=\"bModels\"");
 
 		migrate(am, bm);
 
 		Model b1 = spy(bm.newInstance());
 		Model b2 = spy(bm.newInstance());
 		Model a = am.newInstance();
 		a.set("bModels", new Model[] { b1, b2 });
 		a.create();
 		
 		assertEquals(1, persistService.executeQueryValue("SELECT id from a_models where id=?", 1));
 		assertEquals(1, persistService.executeQueryValue("SELECT id from b_models where id=?", 1));
 		assertEquals(2, count("a_models__b_models___b_models__a_models"));
 		
 		verify(b1, never()).create();
 		verify(b2, never()).create();
 	}
 
 	@Test
 	public void testHasManyToMany_Through() throws Exception {
 		DynModel am = DynClasses.getModel(pkg, "AModel").timestamps()
 			.addHasMany("bModels", "BModel.class", "opposite=\"aModel\"")
 			.addHasMany("cModels", "CModel.class", "through=\"bModels:cModel\"");
 		DynModel bm = DynClasses.getModel(pkg, "BModel").timestamps()
 			.addHasOne("aModel", "AModel.class", "opposite=\"bModels\"")
 			.addHasOne("cModel", "CModel.class", "opposite=\"bModels\"");
 		DynModel cm = DynClasses.getModel(pkg, "CModel").timestamps()
 			.addHasMany("bModels", "BModel.class", "opposite=\"cModel\"");
 	
 		migrate(am, bm, cm);
 
 		Model c1 = spy(cm.newInstance());
 		Model c2 = spy(cm.newInstance());
 		Model a = am.newInstance();
 		a.set("cModels", new Model[] { c1, c2 });
 		
 		assertTrue(a.create());
 
 		assertEquals(1, persistService.executeQueryValue("SELECT id from a_models where id=?", 1));
 		assertEquals(1, persistService.executeQueryValue("SELECT id from c_models where id=?", 1));
 		assertEquals(2, count("b_models"));
 
 		verify(c1, never()).create();
 		verify(c2, never()).create();
 	}
 
 }
