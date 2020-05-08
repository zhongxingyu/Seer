 /**
  * ShopShopViewer
  * Copyright (C) 2012 Remko Plantenga
  * 
  * This file is part of ShopShopViewer.
  *
  * ShopShopViewer is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * ShopShopViewer is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with ShopShopViewer. If not, see <http://www.gnu.org/licenses/>.
  */
 package de.remk0.shopshopviewer.task;
 
 import java.io.InputStream;
 
 import org.easymock.EasyMock;
 
 import com.dd.plist.NSDictionary;
 import com.dd.plist.NSObject;
 
 import de.remk0.shopshopviewer.io.FileAccess;
 import de.remk0.shopshopviewer.parse.ShopShopFileParser;
 import de.remk0.test.AndroidTestCaseWithResources;
 
 /**
  * @author Remko Plantenga
  * 
  */
 public class ReadShopShopFileTaskTest extends AndroidTestCaseWithResources {
 
     public void testExecute() throws Exception {
         ReadShopShopFileTask task = new ReadShopShopFileTask();
 
         FileAccess fileAccess = EasyMock.createMock(FileAccess.class);
         final InputStream is = getResources("de.remk0.shopshopviewer.test")
                 .openRawResource(de.remk0.shopshopviewer.test.R.raw.nederland2);
        EasyMock.expect(fileAccess.getFile("file1.shopshop")).andReturn(is);
         EasyMock.replay(fileAccess);
         task.setFileAccess(fileAccess);
         ShopShopFileParser parser = EasyMock
                 .createMock(ShopShopFileParser.class);
         EasyMock.expect(parser.read(is)).andReturn(true);
         NSDictionary root = new NSDictionary();
         EasyMock.expect(parser.getRoot()).andReturn(root);
         NSObject[] shoppingList = new NSObject[3];
         EasyMock.expect(parser.getShoppingList()).andReturn(shoppingList);
         EasyMock.replay(parser);
         task.setParser(parser);
 
         task.execute(new String[] { "file1" });
 
         assertTrue(task.get());
 
         assertEquals(root, task.getRoot());
         assertEquals(shoppingList, task.getShoppingList());
     }
 
 }
