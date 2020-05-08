 package com.freeroom.di;
 
 import org.junit.Test;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 public class PackageTest
 {
     @Test
     public void should_load_beans_in_a_package()
     {
         final Package beanPackage = new Package("com.freeroom.test.beans.fieldInjection");
         assertThat(beanPackage.getPods().size(), is(3));
     }
 
     @Test
     public void should_load_beans_in_nested_packages()
     {
         final Package beanPackage = new Package("com.freeroom.test.beans.dummyPackage");
         assertThat(beanPackage.getPods().size(), is(2));
     }
 
     @Test
     public void should_load_beans_with_same_name_in_nested_packages()
     {
         final Package beanPackage = new Package("com.freeroom.test.beans.sameBeanName");
         assertThat(beanPackage.getPods().size(), is(2));
     }
 
     @Test
     public void should_load_beans_from_bean_factory()
     {
         final Package beanPackage = new Package("com.freeroom.test.beans.beanFactory");
        assertThat(beanPackage.getPods().size(), is(3));
     }
 }
