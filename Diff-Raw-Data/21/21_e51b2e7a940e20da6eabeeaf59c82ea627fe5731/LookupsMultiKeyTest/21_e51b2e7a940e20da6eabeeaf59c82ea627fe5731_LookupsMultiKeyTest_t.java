 package com.sharneng.lookup;
 
 import static org.hamcrest.Matchers.*;
 
 import com.sharneng.lookup.testdata.CountyCode;
 import com.sharneng.lookup.testdata.LookupMatcher;
 
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

 import org.hamcrest.Matcher;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
@SuppressWarnings("NP_NONNULL_PARAM_VIOLATION")
 public class LookupsMultiKeyTest {
     private static final Lookup<CountyCode> argumentDefault = new EmptyLookup<CountyCode>(CountyCode.DEFAULT);
     private static final CountyCode found = new CountyCode(1081, "Alabama", "Lee");
     private static final Matcher<Lookup<CountyCode>> foundMatcher = new LookupMatcher(found.getCounty(), found,
             "a lookup for state Alabama that can find county Lee");
     private static final Matcher<Lookup<CountyCode>> defaultMatcher = new LookupMatcher("Greene", CountyCode.DEFAULT,
             "an empty lookup");
     private static final Converter<CountyCode, Object> toState = new Converter<CountyCode, Object>() {
         @Override
         public Object convert(CountyCode source) {
             return source.getState();
         }
     };
     private static final Converter<CountyCode, Object> toCounty = new Converter<CountyCode, Object>() {
         @Override
         public Object convert(CountyCode source) {
             return source.getCounty();
         }
     };
 
     @Rule
     public ExpectedException exception = ExpectedException.none();
 
     private static final Map<Integer, String> map = new HashMap<Integer, String>();
     static {
         map.put(1, "A");
         map.put(2, "B");
     }
 
     @Test
     public void create_chokes_onNullProperty() {
         exception.expect(IllegalArgumentException.class);
         exception.expectMessage("property2");
 
         Lookups.create(CountyCode.codes, "state", (String) null);
     }
 
     @Test
     public void createWithDefault_chokes_onNullProperty() {
         exception.expect(IllegalArgumentException.class);
         exception.expectMessage("property2");
 
         Lookups.create(CountyCode.DEFAULT, CountyCode.codes, "state", (String) null);
     }
 
     @Test
     public void createWithClass_chokes_onNullProperty() {
         exception.expect(IllegalArgumentException.class);
         exception.expectMessage("property2");
 
         Lookups.create(CountyCode.codes, CountyCode.class, "state", (String) null);
     }
 
     @Test
     public void createWithDefaultAndClass_chokes_onNullProperty() {
         exception.expect(IllegalArgumentException.class);
         exception.expectMessage("property2");
 
         Lookups.create(CountyCode.DEFAULT, CountyCode.codes, CountyCode.class, "state", (String) null);
     }
 
     @Test
     public void create_chokes_onNullValues() {
         exception.expect(IllegalArgumentException.class);
         exception.expectMessage("values");
 
         Lookups.create((Collection<CountyCode>) null, "state", "county");
     }
 
     @Test
     public void create_chokes_onEmptyValues() {
         exception.expect(IllegalArgumentException.class);
         exception.expectMessage("values");
 
         Lookups.create(new ArrayList<CountyCode>(), "state", "county");
     }
 
     @Test
     public void create_chokes_onNullFilledValues() {
         final ArrayList<CountyCode> values = new ArrayList<CountyCode>();
         values.add(null);
         exception.expect(IllegalArgumentException.class);
         exception.expectMessage("values");
 
         Lookups.create(values, "state", "county");
     }
 
     @Test
     public void createWithClass_chokes_onNullClass() {
         exception.expect(IllegalArgumentException.class);
         exception.expectMessage("clazz");
 
         Lookups.create(CountyCode.codes, (Class<CountyCode>) null, "state", "county");
     }
 
     @Test
     public void createWithDefaultAndClass_chokes_onNullClass() {
         exception.expect(IllegalArgumentException.class);
         exception.expectMessage("clazz");
 
         Lookups.create(CountyCode.DEFAULT, CountyCode.codes, (Class<CountyCode>) null, "state", "county");
     }
 
     @Test
     public void create_chokes_onNullConverter() {
         exception.expect(IllegalArgumentException.class);
         exception.expectMessage("converter2");
 
         Lookups.create(CountyCode.DEFAULT, CountyCode.codes, toState, (Converter<CountyCode, Object>) null);
     }
 
     @Test
     public void create_chokes_onNullConverters() {
         exception.expect(IllegalArgumentException.class);
         exception.expectMessage("converters");
 
         Lookups.create(CountyCode.DEFAULT, CountyCode.codes, (Converter<CountyCode, Object>[]) null);
     }
 
     @Test
     public void create_chokes_onEmptyConverters() {
         exception.expect(IllegalArgumentException.class);
         exception.expectMessage("converter");
 
         Lookups.create(CountyCode.DEFAULT, CountyCode.codes, Utils.toGeneric(new Converter<?, ?>[0]));
     }
 
     @Test
     public void create_chokes_onDuplicateValues() {
         List<CountyCode> codes = new ArrayList<CountyCode>();
         codes.add(new CountyCode(1, "A", "B"));
         codes.add(new CountyCode(2, "A", "B"));
         exception.expect(DuplicateKeyException.class);
 
         Lookups.create(CountyCode.DEFAULT, codes, "state", "county");
     }
 
     public static class FirstLevelFound extends LookupWithDefaultFoundTest<Lookup<CountyCode>> {
         public FirstLevelFound() {
             super(found.getState(), argumentDefault, defaultMatcher, foundMatcher);
         }
 
         @Override
         protected Lookup<Lookup<CountyCode>> newLookup() {
             return Lookups.create(CountyCode.DEFAULT, CountyCode.codes, CountyCode.class, "state", "county");
         }
     }
 
     public static class FirstLevelNotFound extends LookupWithDefaultNotFoundTest<Lookup<CountyCode>> {
         public FirstLevelNotFound() {
             super("NoState", argumentDefault, defaultMatcher);
         }
 
         @Override
         protected Lookup<Lookup<CountyCode>> newLookup() {
             return Lookups.create(CountyCode.DEFAULT, CountyCode.codes, CountyCode.class, "state", "county");
         }
     }
 
     public static class LastLevelWithDefaultFound extends LookupWithDefaultFoundTest<CountyCode> {
         public LastLevelWithDefaultFound() {
             super(found.getCounty(), CountyCode.DEFAULT, equalTo(CountyCode.DEFAULT), equalTo(found));
         }
 
         @Override
         protected Lookup<CountyCode> newLookup() {
             return Lookups.create(CountyCode.DEFAULT, CountyCode.codes, toState, toCounty).get(found.getState());
         }
     }
 
     public static class LastLevelWithDefaultNotFound extends LookupWithDefaultNotFoundTest<CountyCode> {
         public LastLevelWithDefaultNotFound() {
             super("NoState", CountyCode.DEFAULT, equalTo(CountyCode.DEFAULT));
         }
 
         @Override
         protected Lookup<CountyCode> newLookup() {
             return Lookups.create(CountyCode.DEFAULT, CountyCode.codes, "state", "county").get(found.getState());
         }
     }
 
     public static class LastLevelWithoutDefaultFound extends LookupWithoutDefaultFoundTest<CountyCode> {
         public LastLevelWithoutDefaultFound() {
             super(found.getCounty(), CountyCode.DEFAULT, equalTo(found));
         }
 
         @Override
         protected Lookup<CountyCode> newLookup() {
             return Lookups.create(null, CountyCode.codes, "state", "county").get(found.getState());
         }
     }
 
     public static class LastLevelWithoutDefaultNotFound extends LookupWithoutDefaultNotFoundTest<CountyCode> {
         public LastLevelWithoutDefaultNotFound() {
             super("NoState", CountyCode.DEFAULT);
         }
 
         @Override
         protected Lookup<CountyCode> newLookup() {
             return Lookups.create(CountyCode.codes, "state", "county").get(found.getState());
         }
     }
 
 }
