 package org.fest.assertions.examples.advanced;
 
 import static org.fest.assertions.api.Assertions.assertThat;
 import static org.fest.util.Collections.set;
 
 import java.util.Set;
 
 import org.junit.Test;
 
 import org.fest.assertions.core.Condition;
 import org.fest.assertions.examples.AbstractAssertionsExamples;
 
 public class UsingConditionExamples extends AbstractAssertionsExamples {
 
   @Test
   public void is_condition_example() {
     assertThat("Yoda").is(jedi);
     assertThat("Vador").isNot(jedi);
   }
 
   @Test
   public void has_condition_example() {
     assertThat("Yoda").has(jediPower);
     assertThat("Solo").doesNotHave(jediPower);
   }
   
   @Test
   public void condition_example_on_multiple_elements() {
     // are & areNot
     assertThat(set("Luke", "Yoda")).are(jedi);
     assertThat(set("Leia", "Solo")).areNot(jedi);
     
     // have & doNotHave
     assertThat(set("Luke", "Yoda")).have(jediPower);
     assertThat(set("Leia", "Solo")).doNotHave(jediPower);
     
     // areAtLeast & areNotAtLeast
     assertThat(set("Luke", "Yoda", "Leia")).areAtLeast(2, jedi);
     assertThat(set("Luke", "Yoda", "Obiwan")).areAtLeast(2, jedi);
     assertThat(set("Luke", "Yoda", "Leia")).areNotAtLeast(1, jedi);
     assertThat(set("Luke", "Solo", "Leia")).areNotAtLeast(1, jedi);
     
     // haveAtLeast & doNotHaveAtLeast
     assertThat(set("Luke", "Yoda", "Leia")).haveAtLeast(2, jediPower);
     assertThat(set("Luke", "Yoda", "Obiwan")).haveAtLeast(2, jediPower);
     assertThat(set("Luke", "Yoda", "Leia")).doNotHaveAtLeast(1, jediPower);   
     assertThat(set("Luke", "Solo", "Leia")).doNotHaveAtLeast(1, jediPower);
     
     // areAtMost & areNotAtMost
     assertThat(set("Luke", "Yoda", "Leia")).areAtMost(2, jedi);
     assertThat(set("Luke", "Solo", "Leia")).areAtMost(2, jedi);
     assertThat(set("Luke", "Yoda", "Leia")).areNotAtMost(1, jedi);
     assertThat(set("Luke", "Yoda", "Obiwan")).areNotAtMost(1, jedi);   
     
     // haveAtMost & doNotHaveAtMost
     assertThat(set("Luke", "Yoda", "Leia")).haveAtMost(2, jediPower);
     assertThat(set("Luke", "Solo", "Leia")).haveAtMost(2, jediPower);
     assertThat(set("Luke", "Yoda", "Leia")).doNotHaveAtMost(1, jediPower);
     assertThat(set("Luke", "Yoda", "Obiwan")).doNotHaveAtMost(1, jediPower);   
     
     // areExactly & areNotExactly
     assertThat(set("Luke", "Yoda", "Leia")).areExactly(2, jedi);
     assertThat(set("Luke", "Yoda", "Leia")).areNotExactly(1, jedi);
     
     // haveExactly & haveNotExactly
     assertThat(set("Luke", "Yoda", "Leia")).haveExactly(2, jediPower);
    assertThat(set("Luke", "Yoda", "Leia")).areNotExactly(1, jediPower);    
   }
   
   private final Condition<String> jedi = new Condition<String>("jedi") {
     private final Set<String> jedis = set("Luke", "Yoda", "Obiwan");
 
     @Override
     public boolean matches(String value) {
       return jedis.contains(value);
     };
   };
 
   private final Condition<String> jediPower = new Condition<String>("jedi power") {
     private final Set<String> jedis = set("Luke", "Yoda", "Obiwan");
     
     @Override
     public boolean matches(String value) {
       return jedis.contains(value);
     };
   };
   
 }
