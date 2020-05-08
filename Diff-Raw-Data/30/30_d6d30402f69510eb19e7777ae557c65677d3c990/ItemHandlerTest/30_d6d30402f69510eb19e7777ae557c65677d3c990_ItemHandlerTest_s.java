 package com.eseteam9.shoppyapp.tests;
 
 import com.eseteam9.shoppyapp.*;
 import android.content.Context;
 
 public class ItemHandlerTest extends EmulatorTestclass {
 	private Context context;
 	private Item[] item;
 	
 	public ItemHandlerTest(Context context){
 		super("ShoppingListHandler-test");
 		this.context = context;
 	}
 	
 	protected void setUp(){
 		item = new Item[] {
				new Item("dinner","fred", 0),
				new Item("to buy","max", 0),
				new Item("new list item","fred", 1)};
 	}
 	
 	public boolean run(){start();
 		caseCreateHandler();
 		caseAddItems();
 		caseGetItemsInList();
 		caseDeleteItem();
 		caseSetChecked();
 		caseGetCount();
 		caseGetCountUnbought();
 	return reportAndEnd();}
 	
 	private void caseCreateHandler() {startCase("create handler"); try {
 		new ItemHandler(context);
 	trySucceeded();} catch (Exception e) {tryFailed(e);} endCase();}
 	
 	private void caseAddItems() {startCase("add items"); try {
 		new ItemHandler(context).add(item[0]);
 		new ItemHandler(context).add(item[1]);
 		new ItemHandler(context).add(item[2]);
 	trySucceeded();} catch (Exception e) {tryFailed(e);} endCase();}
 	
 	private void caseGetItemsInList() {startCase("get items in lists"); try {
 		Item getList_0[] = new ItemHandler(context).getListItems(0);
 		Item getList_1[] = new ItemHandler(context).getListItems(1);
 		
 		assertTrue(getList_0.length == 2);
 		assertTrue(getList_1.length == 1);
 		assertTrue(getList_0[1].name.equals(item[1].name));
 		assertTrue(getList_1[0].name.equals(item[2].name));
 		assertTrue(getList_0[1].quantity.equals(item[1].quantity));
 		assertTrue(getList_1[0].quantity.equals(item[2].quantity));
 		assertTrue(getList_0[1].listId == item[1].listId);
 		assertTrue(getList_1[0].listId == item[2].listId);
 	trySucceeded();} catch (Exception e) {tryFailed(e);} endCase();}
 	
 	private void caseDeleteItem() {startCase("delete item with correct / incorrect id"); try {
 		new ItemHandler(context).delete(new ItemHandler(context).getListItems(0)[0].id);
 		new ItemHandler(context).delete(-1);
 	trySucceeded();} catch (Exception e) {tryFailed(e);} endCase();}
 	
 	private void caseSetChecked() {startCase("set an item to bought / unbought"); try {
 		new ItemHandler(context).checked(new ItemHandler(context).getListItems(0)[0].id, false);
 		new ItemHandler(context).checked(-1, false);
 		assertTrue(new ItemHandler(context).getListItems(0)[0].bought == false);
 		
 		new ItemHandler(context).checked(new ItemHandler(context).getListItems(0)[0].id, true);
 		assertTrue(new ItemHandler(context).getListItems(0)[0].bought);
 		new ItemHandler(context).checked(new ItemHandler(context).getListItems(0)[0].id, false);
 		assertTrue(new ItemHandler(context).getListItems(0)[0].bought == false);
 	trySucceeded();} catch (Exception e) {tryFailed(e);} endCase();}
 	
 	private void caseGetCount() {startCase("getCount of all items in a list"); try {
 		new ItemHandler(context).getCount(0);
 		
		assertTrue(new ItemHandler(context).getCount(0) == 2);
 		new ItemHandler(context).add(item[0]);
		assertTrue(new ItemHandler(context).getCount(0) == 3);
 	trySucceeded();} catch (Exception e) {tryFailed(e);} endCase();}
 	
 	private void caseGetCountUnbought() {startCase("getCountUnbought of all items in a list"); try {
 		new ItemHandler(context).getCountUnbought(0);
 		
		assertTrue(new ItemHandler(context).getCountUnbought(0) == 3);
		new ItemHandler(context).checked(new ItemHandler(context).getListItems(0)[0].id, true);
 		assertTrue(new ItemHandler(context).getCountUnbought(0) == 2);
 	trySucceeded();} catch (Exception e) {tryFailed(e);} endCase();}
 }
