 package org.oneandone.qxwebdriver.widgetbrowser;
 
 import static org.junit.Assert.*;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.oneandone.qxwebdriver.By;
 import org.oneandone.qxwebdriver.ui.Selectable;
 import org.oneandone.qxwebdriver.ui.Widget;
 import org.oneandone.qxwebdriver.ui.tree.core.AbstractItem;
 
 public class TreeIT extends Common {
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		Common.setUpBeforeClass();
 		selectTab("Tree");
 	}
 	
 	protected void treeTestCommon(String treeLocator) {
 		Selectable tree = (Selectable) tabPage.findWidget(By.qxh(treeLocator));
 		
 		//By item1Locator = By.qxh("*/[@label=" + item1LabelExpected + "]");
 		//TreeItem item1 = (TreeItem) tree.findWidget(item1Locator);
 		
 		String item1LabelExpected = "Desktop";
 		AbstractItem item1 = (AbstractItem) tree.getSelectableItem(item1LabelExpected);
 		
 		item1.click();
 		if (item1.isOpen()) {
 			item1.clickOpenCloseButton();
 		}
 		
 		String item2LabelExpected = "Inbox";
 		AbstractItem item2 = (AbstractItem) tree.getSelectableItem(item2LabelExpected);
 		if (!item2.isOpen()) {
 			item2.clickOpenCloseButton();
 		}
 		
 		String item3LabelExpected = "Trash";
 		AbstractItem item3 = (AbstractItem) tree.getSelectableItem(item3LabelExpected);
 		item3.clickOpenCloseButton();
 		
 		String item4LabelExpected = "Junk #12";
 		tree.selectItem(item4LabelExpected);
 		
		java.util.List<Widget> selection = (java.util.List<Widget>) tree.getWidgetFromProperty("selection");
		assertEquals(1, selection.size());
		Widget selected = selection.get(0);
		String selectedLabel = (String) selected.getPropertyValue("label");
		assertEquals(item4LabelExpected, selectedLabel);
 	}
 	
 	@Test
 	public void tree() {
 		treeTestCommon("*/qx.ui.tree.Tree");
 	}
 	
 	@Test
 	public void virtualTree() {
 		treeTestCommon("*/qx.ui.tree.VirtualTree");
 	}
 }
