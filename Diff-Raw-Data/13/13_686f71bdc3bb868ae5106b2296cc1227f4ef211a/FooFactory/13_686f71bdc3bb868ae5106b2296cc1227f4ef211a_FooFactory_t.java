 package org.factoryboy.core.sample;
 
 import org.factoryboy.core.SequenceValue;
 import org.factoryboy.core.FactoryBoy;
 import org.factoryboy.core.Mold;
 
 import java.math.BigDecimal;
 
 /**
  * Foo工厂.
  */
 public class FooFactory extends FactoryBoy<Foo> {
     @Override
     public Foo defaultObject() {
         Foo foo = new Foo();
         foo.setAmount(BigDecimal.ONE);
        foo.setAge(3 + currentSequenct());
         return foo;
     }
 
     public FooFactory name(final String value) {
         return add(this, new Mold<Foo>() {
             @Override
             public void build(Foo foo) {
                 foo.setName(value);
             }
         });
     }
 
     public FooFactory name(final SequenceValue<String> seqValue) {
         return add(this, new Mold<Foo>() {
             @Override
             public void build(Foo foo) {
                 foo.setName(withThis(seqValue).get());
             }
         });
     }
 
     public FooFactory age(final Integer age) {
         return add(this, new Mold<Foo>() {
             @Override
             public void build(Foo foo) {
                 foo.setAge(age);
             }
         });
     }
 }
