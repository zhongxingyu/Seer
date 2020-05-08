 /*
  * Copyright 2009 Andrew Pietsch
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you
  * may not use this file except in compliance with the License. You may
  * obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied. See the License for the specific language governing permissions
  * and limitations under the License.
  */
 
 package com.pietschy.gwt.pectin.client.bean;
 
 
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.pietschy.gwt.pectin.client.value.ValueHolder;
 import com.pietschy.gwt.pectin.reflect.test.TestBean;
 import org.mockito.ArgumentMatcher;
 import org.mockito.Matchers;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import static org.mockito.Matchers.argThat;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.*;
 import static org.testng.Assert.*;
 
 
 /**
  * BeanModelProvider Tester.
  *
  * @author <Authors name>
  * @version 1.0
  * @since <pre>09/23/2009</pre>
  */
 public class BeanPropertyListModelTest
 {
    private static final String PROPERTY_PATH = "stringList";
 
    private ValueHolder<TestBean> source;
    private BeanPropertyAccessor accessor;
    private BeanPropertyListModel<String> model;
    private TestBean sourceBean;
    private PropertyKey<String> propertyKey;
    private List<String> listOne;
    private List<String> listTwo;
    private List<String> listThree;
    private ValueHolder<Boolean> autoCommit;
 
    @BeforeMethod
    protected void setUp() throws Exception
    {
       source = new ValueHolder<TestBean>();
       accessor = mock(BeanPropertyAccessor.class);
       propertyKey = new PropertyKey<String>(String.class, PROPERTY_PATH);
       autoCommit = new ValueHolder<Boolean>(false);
       model = new BeanPropertyListModel<String>(source,
                                                 propertyKey,
                                                 accessor,
                                                 CollectionConverters.LIST_CONVERTER,
                                                 autoCommit);
       sourceBean = new TestBean();
       source.setValue(sourceBean);
       listOne = Arrays.asList("abc", "def");
       listTwo = Arrays.asList("ghi", "jkl", "mno");
       listThree = Arrays.asList("pqr", "stu", "vwx", "yz");
    }
 
 
    @Test
    public void immutableWhenSourceIsNull()
    {
       // the property is mutable
       when(accessor.isMutable(propertyKey.getPropertyName())).thenReturn(true);
       // but the source is null
       source.setValue(null);
       assertFalse(model.isMutable());
       // once the source is non-null we should be mutable again.
       source.setValue(new TestBean());
       assertTrue(model.isMutable());
    }
 
    @Test
    public void immutableWhenPropertyIsReadOnly()
    {
       // the property is mutable
       when(accessor.isMutable(propertyKey.getPropertyName())).thenReturn(false);
       // but the source is null
       source.setValue(null);
       assertFalse(model.isMutable());
       // always immutable, even when source isn't null
       source.setValue(new TestBean());
       assertFalse(model.isMutable());
    }
 
    @Test
    public void mutatingWithImmutablePropertyBarfs()
    {
       when(accessor.isMutable(propertyKey.getPropertyName())).thenReturn(false);
       try
       {
          model.setElements(new ArrayList<String>());
          fail("Expected ReadOnlyPropertyException");
       }
       catch (ReadOnlyPropertyException e)
       {
       }
 
       try
       {
          model.add("blah");
          fail("Expected ReadOnlyPropertyException");
       }
       catch (ReadOnlyPropertyException e)
       {
       }
 
       try
       {
          model.remove("blah");
          fail("Expected IllegalStateException");
       }
       catch (ReadOnlyPropertyException e)
       {
       }
    }
 
    @Test
    public void mutateWithNullSourceBarfs()
    {
       when(accessor.isMutable(propertyKey.getPropertyName())).thenReturn(true);
       source.setValue(null);
 
       try
       {
          model.setElements(new ArrayList<String>());
          fail("Expected NullSourceException");
       }
       catch (SourceBeanIsNullException e)
       {
       }
 
       try
       {
          model.add("blah");
          fail("Expected NullSourceException");
       }
       catch (SourceBeanIsNullException e)
       {
       }
 
       try
       {
          model.remove("blah");
          fail("Expected ReadOnlyPropertyException");
       }
       catch (SourceBeanIsNullException e)
       {
       }
    }
 
    @Test(expectedExceptions = ReadOnlyPropertyException.class)
    public void writeToSourceWithImmutablePropertyBarfs()
    {
       when(accessor.isMutable(propertyKey.getPropertyName())).thenReturn(false);
       model.writeToSource(true);
    }
 
    @Test(expectedExceptions = SourceBeanIsNullException.class)
    public void writeToSourceWithNullSourceBarfs()
    {
       when(accessor.isMutable(propertyKey.getPropertyName())).thenReturn(true);
       source.setValue(null);
       model.writeToSource(true);
    }
    
 
    @Test
    public void valueChangesAreReflectedByDirtyModel()
    {
       List<String> listOne = Arrays.asList("abc", "def");
 
       assertEquals(model.size(), 0);
       when(accessor.readProperty(sourceBean, propertyKey.getPropertyName())).thenReturn(listOne);
       when(accessor.isMutable(propertyKey.getPropertyName())).thenReturn(true);
       model.readFromSource();
 
       assertFalse(model.getDirtyModel().getValue());
 
       model.setElements(Arrays.asList("ghi"));
       assertTrue(model.getDirtyModel().getValue());
 
       // dirty should recover
       model.setElements(listOne);
       assertFalse(model.getDirtyModel().getValue());
    }
 
    @Test
    public void readFromSource()
    {
       when(accessor.isMutable(propertyKey.getPropertyName())).thenReturn(true);
       when(accessor.readProperty(sourceBean, propertyKey.getPropertyName())).thenReturn(listOne, listTwo);
       model.readFromSource();
 
       assertEquals(model.asUnmodifiableList(), listOne);
       assertFalse(model.getDirtyModel().getValue());
 
       model.setElements(listTwo);
       assertEquals(model.asUnmodifiableList(), listTwo);
       assertTrue(model.getDirtyModel().getValue());
 
       // we should get the second mock value (listTwo) and dirty should return to false
       model.readFromSource();
       assertEquals(model.asUnmodifiableList(), listTwo);
       assertFalse(model.getDirtyModel().getValue());
    }
 
 
    @Test
    public void readFromSourceWithNullProperty()
    {
       when(accessor.readProperty(sourceBean, propertyKey.getPropertyName())).thenReturn(null);
       model.readFromSource();
 
       assertEquals(model.size(), 0);
       assertFalse(model.getDirtyModel().getValue());
    }
 
 
    @Test
    public void checkpoint()
    {
       when(accessor.isMutable(propertyKey.getPropertyName())).thenReturn(true);
       when(accessor.readProperty(sourceBean, propertyKey.getPropertyName())).thenReturn(listOne);
       model.readFromSource();
 
       assertEquals(model.asUnmodifiableList(), listOne);
       assertFalse(model.getDirtyModel().getValue());
 
       model.setElements(listTwo);
       assertEquals(model.asUnmodifiableList(), listTwo);
       assertTrue(model.getDirtyModel().getValue());
 
       // dirty should recover
       model.checkpoint();
       assertEquals(model.asUnmodifiableList(), listTwo);
       assertFalse(model.getDirtyModel().getValue());
    }
 
    @Test
    public void revert()
    {
       when(accessor.isMutable(propertyKey.getPropertyName())).thenReturn(true);
       when(accessor.readProperty(sourceBean, propertyKey.getPropertyName())).thenReturn(listOne);
       model.readFromSource();
 
 
       assertEquals(model.asUnmodifiableList(), listOne);
       assertFalse(model.getDirtyModel().getValue());
 
       model.setElements(listTwo);
       assertEquals(model.asUnmodifiableList(), listTwo);
       assertTrue(model.getDirtyModel().getValue());
 
       // dirty should recover
       model.revert();
       assertEquals(model.asUnmodifiableList(), listOne);
       assertFalse(model.getDirtyModel().getValue());
    }
 
    @Test
    public void writeValueWithCheckpoint()
    {
       when(accessor.isMutable(propertyKey.getPropertyName())).thenReturn(true);
       when(accessor.readProperty(sourceBean, propertyKey.getPropertyName())).thenReturn(listOne);
       model.readFromSource();
 
 
       model.setElements(listTwo);
       assertTrue(model.getDirtyModel().getValue());
 
       model.writeToSource(true);
       // not dirty anymore
       assertFalse(model.getDirtyModel().getValue());
       // and value was written out to the bean...
       verify(accessor).writeProperty(isA(TestBean.class),
                                      eq(propertyKey.getPropertyName()),
                                      argThat(new ThatMatchesList(listTwo)));
    }
 
    @Test
    public void writeValueWithoutCheckpoint()
    {
       when(accessor.isMutable(propertyKey.getPropertyName())).thenReturn(true);
       when(accessor.readProperty(sourceBean, propertyKey.getPropertyName())).thenReturn(listOne);
       model.readFromSource();
 
 
       model.setElements(listTwo);
       assertTrue(model.getDirtyModel().getValue());
 
 
       model.writeToSource(false);
       // still dirty
       assertTrue(model.getDirtyModel().getValue());
       // and value was written out to the bean...
       verify(accessor).writeProperty(isA(TestBean.class),
                                      eq(propertyKey.getPropertyName()),
                                      argThat(new ThatMatchesList(listTwo)));
    }
 
    @Test
    public void dirtyChecksCollectionOrder()
    {
       when(accessor.isMutable(propertyKey.getPropertyName())).thenReturn(true);
 
       model.setElements(Arrays.asList("abc", "def", "qbt"));
       // make sure our dirty state is based on the above values
       model.checkpoint();
       model.setElements(Arrays.asList("qbt", "def", "abc"));
 
       assertTrue(model.getDirtyModel().getValue());
    }
 
    @Test
    public void dirtyIsNotFooledByDuplicateEntries()
    {
       when(accessor.isMutable(propertyKey.getPropertyName())).thenReturn(true);
 
       model.setElements(Arrays.asList("abc", "abc", "def"));
       // make sure our dirty state is based on the above values
       model.checkpoint();
       model.setElements(Arrays.asList("abc", "def", "abc"));
 
       assertTrue(model.getDirtyModel().getValue());
    }
 
 
    @Test
    public void autoCommit()
    {
       autoCommit.setValue(true);
 
       when(accessor.isMutable(propertyKey.getPropertyName())).thenReturn(true);
       when(accessor.readProperty(sourceBean, propertyKey.getPropertyName())).thenReturn(listOne);
       model.readFromSource();
 
       ValueChangeHandler<Boolean> dirtyHandler = mock(ValueChangeHandler.class);
 
       model.getDirtyModel().addValueChangeHandler(dirtyHandler);
 
       assertEquals(model.asUnmodifiableList(), listOne);
 
       model.setElements(listTwo);
 
       verify(dirtyHandler, never()).onValueChange(isA(ValueChangeEvent.class));
       verify(accessor).writeProperty(isA(TestBean.class),
                                      eq(propertyKey.getPropertyName()),
                                      argThat(new ThatMatchesList(listTwo)));
    }
 
 
    @Test
    public void readFromSourceDoesNotWriteToBeanWithAutoCommit()
    {
       autoCommit.setValue(true);
 
       when(accessor.isMutable(propertyKey.getPropertyName())).thenReturn(true);
       when(accessor.readProperty(sourceBean, propertyKey.getPropertyName())).thenReturn(listOne);
 
       model.readFromSource();
       verify(accessor, never()).writeProperty(Matchers.<Object>any(),
                                               Matchers.<String>any(),
                                               Matchers.<Object>any());
    }
 
    @Test
    public void checkpointAndRevertWriteToSourceCorrectlyWithAutoCommit()
    {
       autoCommit.setValue(true);
 
       when(accessor.isMutable(propertyKey.getPropertyName())).thenReturn(true);
       when(accessor.readProperty(sourceBean, propertyKey.getPropertyName())).thenReturn(listOne);
       model.readFromSource();
 
       // this should write the new value to the accessor
       model.setElements(listTwo);
 
       // should revert to list one
       model.revert();
 
       model.setElements(listTwo);
 
       model.checkpoint();
 
       // this should write the new value to the accessor
       model.setElements(listThree);
 
       //  should write the original value back to the bean.
       model.revert();
 
      // setElements to listTwo x 2 plus revert
      verify(accessor, times(3)).writeProperty(isA(TestBean.class),
                                                eq(propertyKey.getPropertyName()),
                                                argThat(new ThatMatchesList(listTwo)));
 
       // revert to listOne
       verify(accessor, times(1)).writeProperty(isA(TestBean.class),
                                                eq(propertyKey.getPropertyName()),
                                                argThat(new ThatMatchesList(listOne)));
       // setElements to listThree
       verify(accessor, times(1)).writeProperty(isA(TestBean.class),
                                                eq(propertyKey.getPropertyName()),
                                                argThat(new ThatMatchesList(listThree)));
    }
 
 
    private static class ThatMatchesList extends ArgumentMatcher<Object>
    {
       private List<String> listToMatch;
 
       public ThatMatchesList(List<String> listToMatch)
       {
          this.listToMatch = listToMatch;
       }
 
       @Override
       public boolean matches(Object o)
       {
          List<String> testList = (List<String>) o;
 
          if (testList.size() != listToMatch.size())
          {
             return false;
          }
 
          for (int i = 0; i < testList.size(); i++)
          {
             if (!listToMatch.get(i).equals(testList.get(i)))
             {
                return false;
             }
          }
          return true;
       }
    }
 }
