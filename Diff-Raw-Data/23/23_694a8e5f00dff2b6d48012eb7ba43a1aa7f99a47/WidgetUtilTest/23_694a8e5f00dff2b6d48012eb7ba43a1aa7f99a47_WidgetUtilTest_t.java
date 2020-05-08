 /*
  * Copyright (C) 2011 The original author or authors.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.zapta.apps.maniana.widget;
 
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import com.zapta.apps.maniana.model.AppModel;
 import com.zapta.apps.maniana.model.ItemColor;
 import com.zapta.apps.maniana.model.ItemModel;
 import com.zapta.apps.maniana.model.ItemModelReadOnly;
 import com.zapta.apps.maniana.model.PageKind;
 import com.zapta.apps.maniana.model.PushScope;
 import com.zapta.apps.maniana.util.LogUtil;
 
 /**
  * Unit test for WidgetlUtil.
  * 
  * @author Tal Dayan
  */
 public class WidgetUtilTest extends TestCase {
 
     private static final ItemModel ITEMS[] = new ItemModel[] {
         // Today
         new ItemModel("item0", false, false, ItemColor.NONE),
         new ItemModel("item1", true, false, ItemColor.NONE),
 
         // Tommorow
         new ItemModel("item2", false, false, ItemColor.NONE),
         new ItemModel("item3", true, false, ItemColor.NONE),
         new ItemModel("item4", false, true, ItemColor.NONE),
         new ItemModel("item5", true, true, ItemColor.NONE) };
 
     public void testComputePushScope() {
         AppModel model = new AppModel();
 
         for (int i = 0; i < ITEMS.length; i++) {
             model.appendItem((i < 2) ? PageKind.TODAY : PageKind.TOMOROW, ITEMS[i]);
         }
 
         for (PushScope pushScope : PushScope.values()) {
             LogUtil.info("*** %s ****", pushScope);
             final int[] expectedItemIndexes;
             switch (pushScope) {
                 case NONE:
                     expectedItemIndexes = new int[] { 0 };
                     break;
                 case UNLOCKED_ONLY:
                     // Tomorow's items are before today's
                     expectedItemIndexes = new int[] {
                         2,
                         0 };
                     break;
                 case ALL:
                     // Tomorow's items preserve order
                     expectedItemIndexes = new int[] {
                         2,
                         4,
                         0 };
                     break;
                 default:
                     throw new RuntimeException("Unknown push scope kind: " + pushScope);
             }
 
             List<ItemModelReadOnly> actualItems = WidgetUtil.selectTodaysActiveItemsByPushScope(
                             model, pushScope);
 
             assertEquals(expectedItemIndexes.length, actualItems.size());
             for (int i = 0; i < actualItems.size(); i++) {
                 final ItemModel expected = ITEMS[expectedItemIndexes[i]];
                 final ItemModelReadOnly actual = actualItems.get(i);
                 final String message = String.format("%s/%d: expected: %s, actual %s", pushScope,
                                 i, expected.getText(), actual.getText());
                 LogUtil.info("--- " + message);
                 assertSame(message, expected, actual);
             }
         }
     }
         
     public void testTitleTextSize() {
         ListWidgetSize W;
         
         // 1 row widget 
         W = ListWidgetSize.LIST_WIDGET_SIZE1; 
         assertEquals(1, W.heightCells);
        assertEquals(9, WidgetUtil.titleTextSize(W, 10));
         assertEquals(12, WidgetUtil.titleTextSize(W, 16));
         assertEquals(16, WidgetUtil.titleTextSize(W, 24));  
         
         // 2 rows widget  
         W = ListWidgetSize.LIST_WIDGET_SIZE2; 
         assertEquals(2, W.heightCells);
        assertEquals(9, WidgetUtil.titleTextSize(W, 10));
        assertEquals(12, WidgetUtil.titleTextSize(W, 16));
        assertEquals(16, WidgetUtil.titleTextSize(W, 24));  
         
         // 4 rows widget 
         W = ListWidgetSize.LIST_WIDGET_SIZE5; 
         assertEquals(4, W.heightCells);
        assertEquals(10, WidgetUtil.titleTextSize(W, 10));
        assertEquals(13, WidgetUtil.titleTextSize(W, 16));
        assertEquals(17, WidgetUtil.titleTextSize(W, 24));  
     }
 }
