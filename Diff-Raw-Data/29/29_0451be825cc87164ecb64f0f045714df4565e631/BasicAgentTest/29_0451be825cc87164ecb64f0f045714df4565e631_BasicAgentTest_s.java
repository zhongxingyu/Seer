 /* BasicAgentTest.java
 
 	Purpose:
 		
 	Description:
 		
 	History:
 		2012/3/22 Created by dennis
 
 Copyright (C) 2011 Potix Corporation. All Rights Reserved.
 */
 package org.zkoss.zats.testcase;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.List;
 import java.util.Stack;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.zkoss.zats.mimic.AgentException;
 import org.zkoss.zats.mimic.ComponentAgent;
 import org.zkoss.zats.mimic.Zats;
 import org.zkoss.zats.mimic.DesktopAgent;
 import org.zkoss.zats.mimic.operation.CheckAgent;
 import org.zkoss.zats.mimic.operation.ClickAgent;
 import org.zkoss.zats.mimic.operation.CloseAgent;
 import org.zkoss.zats.mimic.operation.FocusAgent;
 import org.zkoss.zats.mimic.operation.KeyStrokeAgent;
 import org.zkoss.zats.mimic.operation.MultipleSelectAgent;
 import org.zkoss.zats.mimic.operation.OpenAgent;
 import org.zkoss.zats.mimic.operation.SelectAgent;
 import org.zkoss.zats.mimic.operation.TypeAgent;
 import org.zkoss.zk.ui.AbstractComponent;
 import org.zkoss.zul.Label;
 import org.zkoss.zul.Listbox;
 import org.zkoss.zul.Tree;
 import org.zkoss.zul.Treeitem;
 
 /**
  * @author dennis
  *
  */
 public class BasicAgentTest {
 	@BeforeClass
 	public static void init()
 	{
 		Zats.init(".");
 	}
 
 	@AfterClass
 	public static void end()
 	{
 		Zats.end();
 	}
 
 	@After
 	public void after()
 	{
 		Zats.cleanup();
 	}
 	
 	
 	@Test
 	public void testKeyStrokeAgent(){
 		DesktopAgent desktopAgent = Zats.newClient().connect("/~./basic/keystroke.zul");
 		
 		ComponentAgent inp1 = desktopAgent.query("#inp1");
 		ComponentAgent inp2 = desktopAgent.query("#inp2");
 		ComponentAgent l1 = desktopAgent.query("#l1");
 		
 		Assert.assertEquals("", l1.as(Label.class).getValue());
 		
 		inp1.stroke("#enter");
 		Assert.assertEquals("ENTER key is pressed", l1.as(Label.class).getValue());
 		
 		inp1.stroke("#esc");
 		Assert.assertEquals("ESC key is pressed", l1.as(Label.class).getValue());
 		
 		inp1.stroke("^a");
 		Assert.assertEquals("Ctrl+A is pressed,alt:false,ctrl:true,shift:false", l1.as(Label.class).getValue());
 		
 		inp1.stroke("@b");
 		Assert.assertEquals("Alt+B is pressed,alt:true,ctrl:false,shift:false", l1.as(Label.class).getValue());
 		
 		inp1.stroke("#f8");
 		Assert.assertEquals("F8 is pressed,alt:false,ctrl:false,shift:false", l1.as(Label.class).getValue());
 		
 		
 		inp2.stroke("#right");
 		Assert.assertEquals("keyCode:39 is pressed,alt:false,ctrl:false,shift:false", l1.as(Label.class).getValue());
 		
 		inp2.as(KeyStrokeAgent.class).stroke("$#left");
 		Assert.assertEquals("keyCode:37 is pressed,alt:false,ctrl:false,shift:true", l1.as(Label.class).getValue());
 		
 		try{
 			inp1.as(KeyStrokeAgent.class).stroke("^a#right");//2 key
 			Assert.fail("should not go here");
 		}catch(AgentException x){}
 		
 		try{
 			inp1.as(KeyStrokeAgent.class).stroke("^");//no keycode
 			Assert.fail("should not go here");
 		}catch(AgentException x){}
 		
 	}
 	
 	@Test
 	public void testTypeAgent1(){
 		DesktopAgent desktopAgent = Zats.newClient().connect("/~./basic/type1.zul");
 		
 		ComponentAgent l = desktopAgent.query("#l1");
 		ComponentAgent inp = desktopAgent.query("#inp1");
 		//bandbox
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		
 		inp.type("A");
 		Assert.assertEquals("A",l.as(Label.class).getValue());
 		
 		inp.type("");
 		Assert.assertEquals("A",l.as(Label.class).getValue());
 		
 		inp.type("B");
 		Assert.assertEquals("B",l.as(Label.class).getValue());
 		
 		//combobox
 		l = desktopAgent.query("#l2");
 		inp = desktopAgent.query("#inp2");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("C");
 		Assert.assertEquals("C",l.as(Label.class).getValue());
 		inp.type("");
 		Assert.assertEquals("C",l.as(Label.class).getValue());
 		inp.type("D");
 		Assert.assertEquals("D",l.as(Label.class).getValue());
 		
 		
 		//textbox
 		l = desktopAgent.query("#l10");
 		inp = desktopAgent.query("#inp10");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("E");
 		Assert.assertEquals("E",l.as(Label.class).getValue());
 		inp.type("");
 		Assert.assertEquals("E",l.as(Label.class).getValue());
 		inp.type("F");
 		Assert.assertEquals("F",l.as(Label.class).getValue());
 		
 		//decimalbox
 		l = desktopAgent.query("#l4");
 		inp = desktopAgent.query("#inp4");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("1");
 		Assert.assertEquals("1.0",l.as(Label.class).getValue());
 		inp.type("-1");
 		Assert.assertEquals("1.0",l.as(Label.class).getValue());
 
 		inp.type("2.33");
 		Assert.assertEquals("2.33",l.as(Label.class).getValue());
 		
 		//doublebox
 		l = desktopAgent.query("#l5");
 		inp = desktopAgent.query("#inp5");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		
 		inp.type("3");
 		Assert.assertEquals("3.0",l.as(Label.class).getValue());
 		inp.type("-3");
 		Assert.assertEquals("3.0",l.as(Label.class).getValue());
 		inp.type("4.33");
 		Assert.assertEquals("4.33",l.as(Label.class).getValue());
 		
 		//doublespinner
 		l = desktopAgent.query("#l6");
 		inp = desktopAgent.query("#inp6");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		
 		inp.type("5");
 		Assert.assertEquals("5.0",l.as(Label.class).getValue());
 
 		inp.type("-5");
 		Assert.assertEquals("5.0",l.as(Label.class).getValue());
 
 		inp.type("6.33");
 		Assert.assertEquals("6.33",l.as(Label.class).getValue());
 		
 		//intbox
 		l = desktopAgent.query("#l7");
 		inp = desktopAgent.query("#inp7");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("7");
 		Assert.assertEquals("7",l.as(Label.class).getValue());
 		inp.type("-7");
 		Assert.assertEquals("7",l.as(Label.class).getValue());
 		inp.type("8");
 		Assert.assertEquals("8",l.as(Label.class).getValue());
 		
 		
 		
 		
 		//longbox
 		l = desktopAgent.query("#l8");
 		inp = desktopAgent.query("#inp8");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		
 		inp.type("9");
 		Assert.assertEquals("9",l.as(Label.class).getValue());
 		inp.type("-9");
 		Assert.assertEquals("9",l.as(Label.class).getValue());
 		inp.type("10");
 		Assert.assertEquals("10",l.as(Label.class).getValue());
 		
 		
 		
 		//spinner
 		l = desktopAgent.query("#l9");
 		inp = desktopAgent.query("#inp9");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("11");
 		Assert.assertEquals("11",l.as(Label.class).getValue());
 		inp.type("-11");
 		Assert.assertEquals("11",l.as(Label.class).getValue());
 		inp.type("12");
 		Assert.assertEquals("12",l.as(Label.class).getValue());
 		
 		//datebox
 		l = desktopAgent.query("#l3");
 		inp = desktopAgent.query("#inp3");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("20120223");
 		Assert.assertEquals("20120223",l.as(Label.class).getValue());
 		inp.type("20110101");
 		Assert.assertEquals("20120223",l.as(Label.class).getValue());
 		inp.type("20120320");
 		Assert.assertEquals("20120320",l.as(Label.class).getValue());
 		
 		//timebox
 		l = desktopAgent.query("#l11");
 		inp = desktopAgent.query("#inp11");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("13:00");
 		Assert.assertEquals("13:00",l.as(Label.class).getValue());
 		inp.type("10:00");
 		Assert.assertEquals("13:00",l.as(Label.class).getValue());
 		inp.type("14:02");
 		Assert.assertEquals("14:02",l.as(Label.class).getValue());
 	}
 	
 	@Test
 	public void testTypeAgent2(){
 		DesktopAgent desktopAgent = Zats.newClient().connect("/~./basic/type2.zul");
 		
 		ComponentAgent l = desktopAgent.query("#l1");
 		ComponentAgent inp = desktopAgent.query("#inp1");
 		//bandbox
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		
 		inp.type("A");
 		Assert.assertEquals("A",l.as(Label.class).getValue());
 		
 		inp.type("");
 		Assert.assertEquals("A",l.as(Label.class).getValue());
 		
 		inp.type("B");
 		Assert.assertEquals("B",l.as(Label.class).getValue());
 		
 		//combobox
 		l = desktopAgent.query("#l2");
 		inp = desktopAgent.query("#inp2");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("C");
 		Assert.assertEquals("C",l.as(Label.class).getValue());
 		inp.type("");
 		Assert.assertEquals("C",l.as(Label.class).getValue());
 		inp.type("D");
 		Assert.assertEquals("D",l.as(Label.class).getValue());
 		
 		
 		//textbox
 		l = desktopAgent.query("#l10");
 		inp = desktopAgent.query("#inp10");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("E");
 		Assert.assertEquals("E",l.as(Label.class).getValue());
 		inp.type("");
 		Assert.assertEquals("E",l.as(Label.class).getValue());
 		inp.type("F");
 		Assert.assertEquals("F",l.as(Label.class).getValue());
 		
 		//decimalbox
 		l = desktopAgent.query("#l4");
 		inp = desktopAgent.query("#inp4");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("1");
 		Assert.assertEquals("1.0",l.as(Label.class).getValue());
 		inp.type("-1");
 		Assert.assertEquals("1.0",l.as(Label.class).getValue());
 
 		inp.type("2,222.33");
 		Assert.assertEquals("2222.33",l.as(Label.class).getValue());
 		
 		//doublebox
 		l = desktopAgent.query("#l5");
 		inp = desktopAgent.query("#inp5");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		
 		inp.type("3");
 		Assert.assertEquals("3.0",l.as(Label.class).getValue());
 		inp.type("-3");
 		Assert.assertEquals("3.0",l.as(Label.class).getValue());
 		inp.type("4,444.33");
 		Assert.assertEquals("4444.33",l.as(Label.class).getValue());
 		
 		//doublespinner
 		l = desktopAgent.query("#l6");
 		inp = desktopAgent.query("#inp6");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		
 		inp.type("5");
 		Assert.assertEquals("5.0",l.as(Label.class).getValue());
 
 		inp.type("-5");
 		Assert.assertEquals("5.0",l.as(Label.class).getValue());
 
 		inp.type("6,666.33");
 		Assert.assertEquals("6666.33",l.as(Label.class).getValue());
 		
 		//intbox
 		l = desktopAgent.query("#l7");
 		inp = desktopAgent.query("#inp7");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("7");
 		Assert.assertEquals("7",l.as(Label.class).getValue());
 		inp.type("-7");
 		Assert.assertEquals("7",l.as(Label.class).getValue());
 		inp.type("8,888");
 		Assert.assertEquals("8888",l.as(Label.class).getValue());
 		
 		
 		
 		
 		//longbox
 		l = desktopAgent.query("#l8");
 		inp = desktopAgent.query("#inp8");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		
 		inp.type("9");
 		Assert.assertEquals("9",l.as(Label.class).getValue());
 		inp.type("-9");
 		Assert.assertEquals("9",l.as(Label.class).getValue());
 		inp.type("1,110");
 		Assert.assertEquals("1110",l.as(Label.class).getValue());
 		
 		
 		
 		//spinner
 		l = desktopAgent.query("#l9");
 		inp = desktopAgent.query("#inp9");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("11");
 		Assert.assertEquals("11",l.as(Label.class).getValue());
 		inp.type("-11");
 		Assert.assertEquals("11",l.as(Label.class).getValue());
 		inp.type("1,112");
 		Assert.assertEquals("1112",l.as(Label.class).getValue());
 		
 		//datebox
 		l = desktopAgent.query("#l3");
 		inp = desktopAgent.query("#inp3");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("23022012");
 		Assert.assertEquals("20120223",l.as(Label.class).getValue());
 		inp.type("01012011");
 		Assert.assertEquals("20120223",l.as(Label.class).getValue());
 		inp.type("20032012");
 		Assert.assertEquals("20120320",l.as(Label.class).getValue());
 		
 		//timebox
 		l = desktopAgent.query("#l11");
 		inp = desktopAgent.query("#inp11");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("00:13");
 		Assert.assertEquals("13:00",l.as(Label.class).getValue());
 		inp.type("00:10");
 		Assert.assertEquals("13:00",l.as(Label.class).getValue());
 		inp.type("02:14");
 		Assert.assertEquals("14:02",l.as(Label.class).getValue());
 	}
 	
 	@Test
 	public void testOpenAgentTree(){
 		DesktopAgent desktopAgent = Zats.newClient().connect("/~./basic/open-tree.zul");
 		
 		ComponentAgent tree = desktopAgent.query("#tree");
 		List<ComponentAgent> items = tree.queryAll("treeitem");
 		Assert.assertEquals(2, items.size());
 		
 		Stack<ComponentAgent> stack = new Stack<ComponentAgent>();
 		stack.addAll(items);
 		
 		while(!stack.empty()){
 			ComponentAgent item = stack.pop();
 			
 			if(item.query("treechildren")!=null){
 				Assert.assertFalse(item.as(Treeitem.class).isOpen());
 				items = item.query("treechildren").queryAll("treeitem");//the sub-treeitem.
 				Assert.assertEquals(0, items.size());
 				
 				item.as(OpenAgent.class).open(true);//trigger open to load the tree item.
 				
 				Assert.assertTrue(item.as(Treeitem.class).isOpen());
 				items = item.query("treechildren").queryAll("treeitem");//the sub-treeitem.
 				Assert.assertEquals(2, items.size());
 				for(ComponentAgent si:items){
 					stack.push(si);
 				}
 			}
 		}
 		
 		items = tree.queryAll("treeitem");
 		Assert.assertEquals(14, items.size());
 	}
 	
 	@Test
 	public void testFocusAgent() {
 		DesktopAgent desktopAgent = Zats.newClient().connect("/~./basic/focus.zul");
 		Label curr = desktopAgent.query("#current").as(Label.class);
 		Label lost = desktopAgent.query("#lost").as(Label.class);
 		assertTrue(curr.getValue().length() <= 0);
 		assertTrue(curr.getValue().length() <= 0);
 
 		for (int i = 1; i <= 11; ++i) {
 			ComponentAgent comp = desktopAgent.query("#c" + i);
 			comp.as(FocusAgent.class).focus();
 			String name = comp.as(AbstractComponent.class).getDefinition().getName();
 			assertEquals(name, curr.getValue());
 			comp.as(FocusAgent.class).blur();
 			assertEquals(name, lost.getValue());
 		}
 	}
 
 	@Test
 	public void testCheckAgent() {
 		DesktopAgent desktopAgent = Zats.newClient().connect("/~./basic/check.zul");
 
 		// validate msg
 		Label msg = desktopAgent.query("#msg").as(Label.class);
 		assertTrue(msg.getValue().length() <= 0);
 
 		// test checkbox and menuitem
 		String label = "";
 		for (int i = 1; i <= 6; ++i) {
 			desktopAgent.query("#c" + i).as(CheckAgent.class).check(true);
 			label += "c" + i + " ";
 			assertEquals(label, msg.getValue());
 		}
 		// test radiogroup
 		for (int i = 7; i <= 9; ++i) {
 			desktopAgent.query("#c" + i).as(CheckAgent.class).check(true);
 			assertEquals(label + "c" + i + " ", msg.getValue());
 		}
 	}
 
 	@Test
 	public void testClickAgent() {
 		DesktopAgent desktopAgent = Zats.newClient().connect("/~./basic/click.zul");
 		assertEquals("Hello World!", desktopAgent.query("#msg").as(Label.class).getValue());
 		desktopAgent.query("#btn").as(ClickAgent.class).click();
 		assertEquals("Welcome", desktopAgent.query("#msg").as(Label.class).getValue());
 	}
 	
 	@Test
 	public void testMultipleSelectAgent() {
 		DesktopAgent desktopAgent = Zats.newClient().connect("/~./basic/multiple-select.zul");
 
 		Label msg = desktopAgent.query("#msg").as(Label.class);
 		assertEquals("", msg.getValue());
 
 		ComponentAgent listbox = desktopAgent.query("#lb");
 		assertEquals(4, listbox.as(Listbox.class).getChildren().size()); // include header
 		List<ComponentAgent> items = listbox.queryAll("listitem");
 
 		// listbox multiple selection
 		items.get(0).as(MultipleSelectAgent.class).select();
 		assertEquals("[i0]", msg.getValue());
 		assertEquals(1, listbox.as(Listbox.class).getSelectedCount());
 		items.get(1).as(MultipleSelectAgent.class).select();
 		assertEquals("[i0, i1]", msg.getValue());
 		assertEquals(2, listbox.as(Listbox.class).getSelectedCount());
 		items.get(2).as(MultipleSelectAgent.class).select();
 		assertEquals("[i0, i1, i2]", msg.getValue());
 		assertEquals(3, listbox.as(Listbox.class).getSelectedCount());
 		items.get(1).as(MultipleSelectAgent.class).deselect();
 		assertEquals("[i0, i2]", msg.getValue());
 		assertEquals(2, listbox.as(Listbox.class).getSelectedCount());
 		items.get(0).as(MultipleSelectAgent.class).deselect();
 		assertEquals("[i2]", msg.getValue());
 		assertEquals(1, listbox.as(Listbox.class).getSelectedCount());
 		items.get(2).as(MultipleSelectAgent.class).deselect();
 		assertEquals("[]", msg.getValue());
 		assertEquals(0, listbox.as(Listbox.class).getSelectedCount());
 		items.get(2).as(MultipleSelectAgent.class).deselect(); // should happen nothing
 		assertEquals("[]", msg.getValue());
 		assertEquals(0, listbox.as(Listbox.class).getSelectedCount());
 		
 		// listbox single selection (extra test)
 		desktopAgent.query("#lbcb checkbox").as(CheckAgent.class).check(false);
 		String[] values = { "[i0]", "[i1]", "[i2]" };
 		for (int i = 0; i < 3; ++i) {
 			items.get(i).as(SelectAgent.class).select();
 			assertEquals(values[i], msg.getValue());
 		}
 		
 		// tree multiple selection
 		desktopAgent.query("#ti1").as(MultipleSelectAgent.class).select();
 		assertEquals("[ti1]", msg.getValue());
 		desktopAgent.query("#ti1-2").as(MultipleSelectAgent.class).select();
 		assertEquals("[ti1, ti1-2]", msg.getValue());
 		desktopAgent.query("#ti1-1").as(MultipleSelectAgent.class).select();
 		assertEquals("[ti1, ti1-1, ti1-2]", msg.getValue());
 		desktopAgent.query("#ti1-1").as(MultipleSelectAgent.class).select();
 		assertEquals("[ti1, ti1-1, ti1-2]", msg.getValue());
 		desktopAgent.query("#ti1").as(MultipleSelectAgent.class).deselect();
 		assertEquals("[ti1-1, ti1-2]", msg.getValue());
 		desktopAgent.query("#ti1-2").as(MultipleSelectAgent.class).deselect();
 		assertEquals("[ti1-1]", msg.getValue());
 		desktopAgent.query("#ti1-2").as(MultipleSelectAgent.class).deselect();
 		assertEquals("[ti1-1]", msg.getValue());
 		desktopAgent.query("#ti1-1").as(MultipleSelectAgent.class).deselect();
 		assertEquals("[]", msg.getValue());
 		
 		// tree multiple selection - single select at multiple selection mode
 		desktopAgent.query("#ti1-2").as(SelectAgent.class).select();
 		assertEquals("[ti1-2]", msg.getValue());
 		desktopAgent.query("#ti1-1").as(SelectAgent.class).select();
 		assertEquals("[ti1-1]", msg.getValue());
 		desktopAgent.query("#ti1").as(SelectAgent.class).select();
 		assertEquals("[ti1]", msg.getValue());
 
 		// tree multiple selection - with check mark
 		desktopAgent.queryAll("#tcb > checkbox").get(1).as(CheckAgent.class).check(true);
 		assertTrue(desktopAgent.query("#t").as(Tree.class).isCheckmark());
 
 		desktopAgent.query("#ti1").as(MultipleSelectAgent.class).select();
 		assertEquals("[ti1]", msg.getValue());
 		desktopAgent.query("#ti1-2").as(MultipleSelectAgent.class).select();
 		assertEquals("[ti1, ti1-2]", msg.getValue());
 		desktopAgent.query("#ti1-1").as(MultipleSelectAgent.class).select();
 		assertEquals("[ti1, ti1-1, ti1-2]", msg.getValue());
 		desktopAgent.query("#ti1-1").as(MultipleSelectAgent.class).select();
 		assertEquals("[ti1, ti1-1, ti1-2]", msg.getValue());
 		desktopAgent.query("#ti1").as(MultipleSelectAgent.class).deselect();
 		assertEquals("[ti1-1, ti1-2]", msg.getValue());
 		desktopAgent.query("#ti1-2").as(MultipleSelectAgent.class).deselect();
 		assertEquals("[ti1-1]", msg.getValue());
 		desktopAgent.query("#ti1-2").as(MultipleSelectAgent.class).deselect();
 		assertEquals("[ti1-1]", msg.getValue());
 		desktopAgent.query("#ti1-1").as(MultipleSelectAgent.class).deselect();
 		assertEquals("[]", msg.getValue());
 
 		// tree single selection (extra test)
 		desktopAgent.queryAll("#tcb > checkbox").get(0).as(CheckAgent.class).check(false);
 		assertFalse(desktopAgent.query("#t").as(Tree.class).isMultiple());
 
 		desktopAgent.query("#ti1-2").as(SelectAgent.class).select();
 		assertEquals("[ti1-2]", msg.getValue());
 		desktopAgent.query("#ti1-1").as(SelectAgent.class).select();
 		assertEquals("[ti1-1]", msg.getValue());
 		desktopAgent.query("#ti1").as(SelectAgent.class).select();
 		assertEquals("[ti1]", msg.getValue());
 
 		try {
 			desktopAgent.query("#ti1").as(MultipleSelectAgent.class).select();
 			fail();
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 	
 	@Test
 	public void testSelectAgent() {
 		DesktopAgent desktop = Zats.newClient().connect("/~./basic/select.zul");
 
 		Label selected = desktop.query("#selected").as(Label.class);
 		assertEquals("", selected.getValue());
 
 		// combobox
 		String[] labels = new String[] { "cbi1", "cbi2", "cbi3" };
 		List<ComponentAgent> cbitems = desktop.queryAll("#cb > comboitem");
 		assertEquals(labels.length, cbitems.size());
 		for (int i = 0; i < labels.length; ++i) {
 			cbitems.get(i).as(SelectAgent.class).select();
 			assertEquals(labels[i], selected.getValue());
 		}
 
 		// tabbox
 		labels = new String[] { "tb1.tab1", "tb1.tab2" };
 		List<ComponentAgent> tab = desktop.queryAll("#tb1 tab");
 		assertEquals(labels.length, tab.size());
 		for (int i = 0; i < labels.length; ++i) {
 			tab.get(i).as(SelectAgent.class).select();
 			assertEquals(labels[i], selected.getValue());
 		}
 
 		// tab
 		labels = new String[] { "tb2.tab1", "tb2.tab2" };
 		tab = desktop.queryAll("#tb2 tab");
 		assertEquals(labels.length, tab.size());
 		for (int i = 0; i < labels.length; ++i) {
 			tab.get(i).as(SelectAgent.class).select();
 			assertEquals(labels[i], selected.getValue());
 		}
 
 		// tree
 		labels = new String[] { "ti1", "ti1.1", "ti1.2" };
 		List<ComponentAgent> titems = desktop.queryAll("#t treeitem");
 		assertEquals(labels.length, titems.size());
 		for (int i = 0; i < labels.length; ++i) {
 			titems.get(i).as(SelectAgent.class).select();
 			assertEquals(labels[i], selected.getValue());
 		}
 	}
 	
 	@Test
 	public void testCloseAgent(){
 		DesktopAgent desktopAgent = Zats.newClient().connect("/~./basic/close.zul");
 		
 		ComponentAgent panel = desktopAgent.query("panel[title='closable']");
 		panel.as(CloseAgent.class).close();
 		Assert.assertNull(panel.getComponent().getPage());
 		
 		ComponentAgent window = desktopAgent.query("window[title='closable']");
 		window.as(CloseAgent.class).close();
 		Assert.assertNull(window.getComponent().getPage());
 		
 		ComponentAgent tab = desktopAgent.query("tab[label='closable']");
 		tab.as(CloseAgent.class).close();
 		Assert.assertNull(tab.getComponent().getPage());
 		
 		// issue of checking flag at server-side 
 		//	panel = desktopAgent.query("panel[title='non-close']");
 		//	panel.as(CloseAgent.class).close();
 		//	Assert.assertNotNull(panel.getComponent().getPage());
 	}
 	
 	@Test
 	public void testOpenAgent(){
 		DesktopAgent desktop = Zats.newClient().connect("/~./basic/open.zul");
 
 		Label open = desktop.query("#open").as(Label.class);
 		Label close = desktop.query("#close").as(Label.class);
 		assertEquals("", open.getValue());
 		assertEquals("", close.getValue());
 
 		String values[] = { "", "" };
 		// bandbox
 		String id = "#aBandbox";
 		values[0] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(true);
 		assertEquals(values[0], open.getValue());
 		assertEquals(values[1], close.getValue());
 		values[1] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(false);
 		assertEquals(values[0], open.getValue());
 		assertEquals(values[1], close.getValue());
 
 		// combobox
 		id = "#aCombobox";
 		values[0] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(true);
 		assertEquals(values[0], open.getValue());
 		assertEquals(values[1], close.getValue());
 		values[1] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(false);
 		assertEquals(values[0], open.getValue());
 		assertEquals(values[1], close.getValue());
 
 		// groupbox
 		id = "#aGroupbox";
 		values[0] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(true);
 		assertEquals(values[0], open.getValue());
 		assertEquals(values[1], close.getValue());
 		values[1] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(false);
 		assertEquals(values[0], open.getValue());
 		assertEquals(values[1], close.getValue());
 
 		// detail
 		id = "#aDetail";
 		values[0] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(true);
 		assertEquals(values[0], open.getValue());
 		assertEquals(values[1], close.getValue());
 		values[1] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(false);
 		assertEquals(values[0], open.getValue());
 		assertEquals(values[1], close.getValue());
 
 		// group
 		id = "#aGroup";
 		values[0] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(true);
 		assertEquals(values[0], open.getValue());
 		assertEquals(values[1], close.getValue());
 		values[1] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(false);
 		assertEquals(values[0], open.getValue());
 		assertEquals(values[1], close.getValue());
 
 		// listgroup
 		id = "#aListgroup";
 		values[0] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(true);
 		assertEquals(values[0], open.getValue());
 		assertEquals(values[1], close.getValue());
 		values[1] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(false);
 		assertEquals(values[0], open.getValue());
 		assertEquals(values[1], close.getValue());
 
 		// treeitem
 		id = "#treeitem1";
 		values[0] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(true);
 		assertEquals(values[0], open.getValue());
 		assertEquals(values[1], close.getValue());
 		values[1] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(false);
 		assertEquals(values[0], open.getValue());
 		assertEquals(values[1], close.getValue());
 		id = "#treeitem1-2";
 		values[0] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(true);
 		assertEquals(values[0], open.getValue());
 		assertEquals(values[1], close.getValue());
 		values[1] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(false);
 		assertEquals(values[0], open.getValue());
 		assertEquals(values[1], close.getValue());
 	}
 	
 	@Test
 	public void testCKEditorTypeAgent() {
		DesktopAgent desktop = Conversations.open().connect("/~./basic/type-ckeditor.zul");
 
 		Label content = desktop.query("#content").as(Label.class);
 		assertEquals("", content.getValue());
 
 		desktop.query("#ck").as(TypeAgent.class).type("Hello world");
 		assertEquals("Hello world", content.getValue());
 	}
 }
