 package org.factoryboy.core;
 
 import org.factoryboy.core.sample.Foo;
 import org.factoryboy.core.sample.FooDAO;
 import org.factoryboy.core.sample.FooFactory;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import java.util.List;
 
 import static org.fest.assertions.api.Assertions.assertThat;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 /**
  * Mock测试
  */
 @RunWith(MockitoJUnitRunner.class)
 public class MockFooDAOTest {
 
     @Mock
     FooDAO fooDAO;
 
     FooFactory fooFactory;
 
     @Before
     public void setUp() throws Exception {
         // 设置生成Foo的默认age是30;
         fooFactory = new FooFactory().age(30);
     }
 
     @Test
     public void testFindAll() throws Exception {
         when(fooDAO.findAll()).thenReturn(fooFactory.name("第%d号员工").build(30));
         List<Foo> fooList = fooDAO.findAll();
         assertThat(fooList).isNotEmpty().hasSize(30);
         assertThat(fooList.get(0).getName()).startsWith("第").endsWith("号员工");
         assertThat(fooList.get(3).getAge()).isEqualTo(30);
     }
 
     @Test
     public void testFindById() throws Exception {
         when(fooDAO.findById(eq(9527L))).thenReturn(fooFactory.name("周星星").age(51).build());
         Foo zxx = fooDAO.findById(9527L);
         assertThat(zxx.getName()).isEqualTo("周星星");
         assertThat(zxx.getAge()).isEqualTo(51);
        verify(fooDAO, times(1)).findById(9527L);
         verify(fooDAO, never()).findById(eq(1L));
     }
 }
