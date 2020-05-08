 package com.timgroup.stanislavski.magic.builders;
 
 import org.junit.Test;
 
 import com.google.common.base.Supplier;
 import com.timgroup.stanislavski.interpreters.AddressesProperty;
 
 public class BuilderFactoryTest {
 
     public static class Record {
         String foo;
         int bar;
     }
     
     public interface BuilderWithMisspeltProperty extends Supplier<Record> {
         @AddressesProperty("foo") BuilderWithMisspeltProperty with_fog(String foo);
         BuilderWithMisspeltProperty with_bag(int bar);
     }
     
     public interface BuilderWithIncorrectType extends Supplier<Record> {
         BuilderWithIncorrectType with_foo(String foo);
         BuilderWithIncorrectType with_bar(String bar);
     }
     
    @Test(expected=IllegalArgumentException.class) public void
     detects_missing_properties() {
         BuilderFactory.validating(BuilderWithMisspeltProperty.class).against(Record.class);
     }
     
 }
