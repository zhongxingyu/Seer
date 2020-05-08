 /*---
    Copyright 2006-2007 Visual Systems Corporation.
    http://www.vscorp.com
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
         http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 ---*/
 
 package com.googlecode.wicketwebbeans.containers;
 
 import java.io.Serializable;
 import java.util.Arrays;
 
 import junit.framework.TestCase;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.Component.IVisitor;
 import org.apache.wicket.Page;
 import org.apache.wicket.extensions.markup.html.repeater.data.grid.DataGridView;
 import org.apache.wicket.markup.repeater.OddEvenItem;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.util.tester.ITestPageSource;
 import org.apache.wicket.util.tester.WicketTester;
 
 import com.googlecode.wicketwebbeans.containers.BeanForm;
 import com.googlecode.wicketwebbeans.fields.InputField;
 import com.googlecode.wicketwebbeans.model.BeanMetaData;
 import com.googlecode.wicketwebbeans.model.BeanPropertyModel;
 
 
 /**
  * Tests Models with bean containers. <p>
  * 
  * @author Dan Syrstad
  */
 public class ContainerModelTest extends TestCase
 {
     /**
      * Construct a ContainerModelTest. 
      *
      * @param name
      */
     public ContainerModelTest(String name)
     {
         super(name);
     }
 
     /**
      * Tests BeanForm with a LoadableDetachableModel instead of a direct bean.
      */
     public void testBeanFormWithLoadableDetachableModel()
     {
         WicketTester tester = new WicketTester();
 
         final ContainerModelTestPage page = new ContainerModelTestPage();
         
 
         TestLoadableDetachableObjectModel nestedModel = new TestLoadableDetachableObjectModel();
         BeanMetaData meta = new BeanMetaData(nestedModel.getObject().getClass(), null, page, null, false);
         BeanForm form = new BeanForm("beanForm", nestedModel, meta);
 
         page.add(form);
         
         tester.startPage(new ITestPageSource() {
             private static final long serialVersionUID = 1L;
             public Page getTestPage()
             {
                 return page;
             }
         });
         
         //tester.debugComponentTrees();
 
         // Check elements, labels.
         String firstRowPath = "beanForm:f:tabs:r:0";
         String namePath = firstRowPath + ":c:0:c";
         String nameFieldPath = namePath + ":c";
         
         tester.assertLabel(namePath + ":l", "Name");
         tester.assertComponent(nameFieldPath, InputField.class);
         Component nameField = tester.getComponentFromLastRenderedPage(nameFieldPath);
 
         String serialNumPath = firstRowPath + ":c:1:c";
         String serialNumFieldPath = serialNumPath + ":c";
         tester.assertLabel(serialNumPath + ":l", "Serial Number");
         tester.assertComponent(serialNumFieldPath, InputField.class);
         Component serialNumField = tester.getComponentFromLastRenderedPage(serialNumFieldPath);
         
         // Check attaching/detaching component's model (BeanPropertyModel).
         BeanPropertyModel nameFieldModel = (BeanPropertyModel) nameField.getDefaultModel();
         
         assertFalse(nestedModel.isAttached());
 
         // Should attach the nested model's object.
         nameFieldModel.getObject();
         
         assertTrue(nestedModel.isAttached());
         
         NonSerializableBean firstBean = (NonSerializableBean)nestedModel.getObject(); 
         
         // Make the first bean detach. This also tests that the model is attached somewhere below the page.
         //page.detachModels(); // TODO 1.3 doesn't work
         detachModels(page);
         
         assertFalse(nestedModel.isAttached());
         
         NonSerializableBean secondBean = (NonSerializableBean)nestedModel.getObject(); 
 
         // Should be different and attached now.
         assertNotSame(firstBean, secondBean);
         assertTrue(nestedModel.isAttached());
         
         // Assert PropertyChangeListener on BeanForm is called.
         assertFalse( form.isComponentRefreshNeeded() );
         nameFieldModel.setObject("test");
         assertTrue( form.isComponentRefreshNeeded() );
 
         // Clear the refresh components.
         form.clearRefreshComponents();
         
         // Assert PropertyChangeListener on BeanForm is called after detach()/attach().
         //page.detachModels(); // TODO 1.3 doesn't work
         detachModels(page);
         assertFalse(nestedModel.isAttached());
         
         assertFalse( form.isComponentRefreshNeeded() );
         nameFieldModel.setObject("test");
         assertTrue( form.isComponentRefreshNeeded() );
 
         // Clear the refresh components.
         form.clearRefreshComponents();
     }
 
     private void detachModels(Page page)
     {
         page.visitChildren(new IVisitor<Component>() {
             public Object component(Component component)
             {
                 try {
                     // detach any models of the component
                     component.detachModels();
                 }
                 catch (Exception e) {
                     // Ignore
                 }
                 
                 return IVisitor.CONTINUE_TRAVERSAL;
             }
         });
     }
 
     /**
      * Tests BeanForm with an IModel that represents a List. Form should use a BeanTablePanel rather
      * than BeanGridPanel.
      */
     public void testBeanFormWithListModel()
     {
         WicketTester tester = new WicketTester();
 
         final ContainerModelTestPage page = new ContainerModelTestPage();
 
         SerializableBean[] beans = new SerializableBean[20];
         for (int i = 0; i < beans.length; i++) {
             beans[i] = new SerializableBean("Name" + i, "XYZ" + i);
         }
         
         IModel<Serializable> beanModel = new Model<Serializable>((Serializable)(Object) Arrays.asList(beans));
         
         BeanMetaData meta = new BeanMetaData(SerializableBean.class, null, page, null, false);
         BeanForm form = new BeanForm("beanForm", beanModel, meta);
 
         page.add(form);
         
         tester.startPage(new ITestPageSource() {
             private static final long serialVersionUID = 1L;
             public Page getTestPage()
             {
                 return page;
             }
         });
         
         //tester.debugComponentTrees();
 
         checkListPage(tester, page, beans);
     }
 
     /**
      * Tests BeanForm with a List. Form should use a BeanTablePanel rather
      * than BeanGridPanel.
      */
     public void testBeanFormWithList()
     {
         WicketTester tester = new WicketTester();
 
         final ContainerModelTestPage page = new ContainerModelTestPage();
 
         SerializableBean[] beans = new SerializableBean[20];
         for (int i = 0; i < beans.length; i++) {
             beans[i] = new SerializableBean("Name" + i, "XYZ" + i);
         }
         
         BeanMetaData meta = new BeanMetaData(SerializableBean.class, null, page, null, false);
         BeanForm form = new BeanForm("beanForm", Arrays.asList(beans), meta);
 
         page.add(form);
         
         tester.startPage(new ITestPageSource() {
             private static final long serialVersionUID = 1L;
             public Page getTestPage()
             {
                 return page;
             }
         });
         
         //tester.debugComponentTrees();
 
         checkListPage(tester, page, beans);
     }
 
     /**
      * Checks a page that uses a List.
      *
      * @param tester
      * @param page
      * @param beans
      */
     private void checkListPage(WicketTester tester, final ContainerModelTestPage page, SerializableBean[] beans)
     {
         // Check that we have a data grid view and repeating fields.
        String tablePath = "beanForm:f:tabs:t:rows";
         tester.assertComponent(tablePath, DataGridView.class);
 
         for (int i = 1; i <= 10; i++) {
             String rowPath = tablePath + ":" + i;
             tester.assertComponent(rowPath, OddEvenItem.class);
 
             String firstCellPath = rowPath + ":cells:1:cell";
             tester.assertComponent(firstCellPath, InputField.class);
             Component nameField = tester.getComponentFromLastRenderedPage(firstCellPath);
             assertEquals(beans[i - 1].getName(), nameField.getDefaultModel().getObject());
 
             String secondCellPath = rowPath + ":cells:2:cell";
             tester.assertComponent(secondCellPath, InputField.class);
             Component serailNumField = tester.getComponentFromLastRenderedPage(secondCellPath);
             assertEquals(beans[i - 1].getSerialNumber(), serailNumField.getDefaultModel().getObject());
         }
     }
 
 }
