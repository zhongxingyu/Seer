 package com.logicalpractice.collections;
 
 import static com.logicalpractice.collections.CollectionFunctions.by;
 import static com.logicalpractice.collections.CollectionFunctions.collect;
 import static com.logicalpractice.collections.CollectionFunctions.from;
 import static com.logicalpractice.collections.CollectionFunctions.select;
 import static com.logicalpractice.collections.CollectionFunctions.where;
 import static com.logicalpractice.collections.TypedUtils.typedList;
 import static java.util.Arrays.asList;
 import static org.hamcrest.Matchers.allOf;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.equalToIgnoringCase;
 import static org.hamcrest.Matchers.greaterThan;
 import static org.hamcrest.Matchers.hasItems;
 import static org.hamcrest.Matchers.lessThan;
 import static org.hamcrest.Matchers.not;
 import static org.hamcrest.Matchers.nullValue;
 import static org.junit.Assert.assertThat;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.junit.Test;
 
 public class CollectionFunctionsTest {
 
    List<Person> testData = Arrays.asList(new Person("Billy", "Smith", 8), new Person("Billy",
          "Jones", 32), new Person("James", "Smith", 70));
 
    @Test
    public void selectAllSmiths() throws Exception {
 
       List<Person> output = select(testData, new Script<String>() {
          {
             each(Person.class).getLastName();
          }
       }, equalTo("Smith"));
 
       assertThat(output.size(), equalTo(2));
    }
 
    @Test
    public void selectAllSmiths2() throws Exception {
       List<Person> output = select(testData, where(Person.class).getLastName(), equalTo("Smith"));
       assertThat(output.size(), equalTo(2));
    }
 
    @Test
    public void selectAllSmiths3() throws Exception {
       List<Person> output = select(from(testData).getLastName(), equalTo("Smith"));
       assertThat(output.size(), equalTo(2));
    }
 
    @Test
    public void selectAllBillys() throws Exception {
       List<Person> output = select(from(testData).getFirstName(), equalToIgnoringCase("billy"));
       assertThat(output.size(), equalTo(2));
    }
 
    @Test(expected = IllegalArgumentException.class)
    public void fromThrowsIllegalArgumentException() throws Exception {
       List<String> values = Collections.emptyList();
       from(values);
    }
 
    @Test
    public void selectAllowsTypedZeroLengthInput() throws Exception {
       List<Person> values = Collections.emptyList();
       values = typedList(values, Person.class);
       List<Person> results = select(from(values).getFirstName(), equalTo("Thing"));
 
       assertThat(results.isEmpty(), equalTo(true));
    }
 
    @Test
    public void selectUsingPredicate() {
       final AtomicInteger evaluateCount = new AtomicInteger(0);
 
       List<Person> result = select(testData, new Predicate<Person>() {
          public boolean evaluate(Person p) {
             evaluateCount.incrementAndGet();
             return p.getLastName()
                   .matches("[Ss]mith");
          }
       });
       assertThat(result.size(), equalTo(2));
       assertThat(evaluateCount.intValue(), equalTo(3));
    }
 
    @Test
    public void simpleSelect() throws Exception {
       Integer[] values = { 1, 3, 7, 9, 11, 19 };
 
       List<Integer> result = select(asList(values), lessThan(10));
 
       assertThat(result, not(nullValue()));
       assertThat(result, hasItems(1, 3, 7, 9));
       assertThat(result, not(hasItems(11, 19)));
    }
 
    @Test
    public void scriptCollectOperation() {
 
       // collects all the first names
       List<String> result = collect(testData, new Script<String>() {
          {
             each(Person.class).getFirstName();
          }
       });
 
       assertThat(result, not(nullValue()));
       assertThat(result, hasItems("Billy", "James"));
       assertThat(result.size(), equalTo(3));
    }
 
    @Test
    public void scriptCollectOperation2() {
 
       // collects all the first names
       List<String> result = collect(testData, new Script<String>() {
          {
             each(Person.class).getLastName();
          }
       });
 
       assertThat(result, not(nullValue()));
       assertThat(result, hasItems("Smith", "Jones"));
       assertThat(result.size(), equalTo(3));
    }
 
    @Test
    public void byCollectOperation() {
 
       List<String> result = collect(testData, by(Person.class).getLastName());
 
       assertThat(result, not(nullValue()));
       assertThat(result, hasItems("Smith", "Jones"));
       assertThat(result.size(), equalTo(3));
    }
 
    @SuppressWarnings("unchecked")
    @Test
    public void checkPrimitiveMethod() throws Exception {
 
       List<Person> workingAgeAdults = select(from(testData).getAge(), allOf(greaterThan(18),
             lessThan(65)));
 
       assertThat(workingAgeAdults.isEmpty(), equalTo(false));
    }
 
   @Test(expected=NullPointerException.class)
    public void verifyNullPointerThrown() throws Exception {
      select(testData, where(Person.class).getFirstName().substring(0,1), equalTo("s"));      
    }
   
    public void collectFromOperation() {
       List<String> result = collect(from(testData).getLastName());
 
       assertThat(result, not(nullValue()));
       assertThat(result, hasItems("Smith", "Jones"));
       assertThat(result.size(), equalTo(3));
    }
 }
