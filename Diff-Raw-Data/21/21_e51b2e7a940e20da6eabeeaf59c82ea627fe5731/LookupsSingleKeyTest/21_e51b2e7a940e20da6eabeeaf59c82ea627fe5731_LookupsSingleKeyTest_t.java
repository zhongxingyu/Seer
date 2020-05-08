 package com.sharneng.lookup;
 
 import static org.hamcrest.Matchers.*;
 
 import com.sharneng.lookup.testdata.CountyCode;
 
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
@SuppressWarnings("NP_NONNULL_PARAM_VIOLATION")
 public class LookupsSingleKeyTest {
     private static final CountyCode found = CountyCode.codes.get(56);
     private static final Converter<CountyCode, Object> toCode = new Converter<CountyCode, Object>() {
         @Override
         public Object convert(CountyCode source) {
             return source.getCode();
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
     public void create_chokes_onNullMap() {
         exception.expect(IllegalArgumentException.class);
         exception.expectMessage("map");
 
         Lookups.create(null);
     }
 
     @Test
     public void create_chokes_onNullProperty() {
         exception.expect(IllegalArgumentException.class);
         exception.expectMessage("property");
 
         Lookups.create(CountyCode.codes, (String) null);
     }
 
     @Test
     public void createWithDefault_chokes_onNullProperty() {
         exception.expect(IllegalArgumentException.class);
         exception.expectMessage("property");
 
         Lookups.create(CountyCode.DEFAULT, CountyCode.codes, (String) null);
     }
 
     @Test
     public void createWithClass_chokes_onNullProperty() {
         exception.expect(IllegalArgumentException.class);
         exception.expectMessage("property");
 
         Lookups.create(CountyCode.codes, CountyCode.class, (String) null);
     }
 
     @Test
     public void createWithDefaultAndClass_chokes_onNullProperty() {
         exception.expect(IllegalArgumentException.class);
         exception.expectMessage("property");
 
         Lookups.create(CountyCode.DEFAULT, CountyCode.codes, CountyCode.class, (String) null);
     }
 
     @Test
     public void create_chokes_onNullValues() {
         exception.expect(IllegalArgumentException.class);
         exception.expectMessage("values");
 
         Lookups.create((Collection<CountyCode>) null, "code");
     }
 
     @Test
     public void create_chokes_onEmptyValues() {
         exception.expect(IllegalArgumentException.class);
         exception.expectMessage("values");
 
         Lookups.create(new ArrayList<CountyCode>(), "code");
     }
 
     @Test
     public void create_chokes_onNullFilledValues() {
         final ArrayList<CountyCode> values = new ArrayList<CountyCode>();
         values.add(null);
         exception.expect(IllegalArgumentException.class);
         exception.expectMessage("values");
 
         Lookups.create(values, "code");
     }
 
     @Test
     public void createWithClass_chokes_onNullClass() {
         exception.expect(IllegalArgumentException.class);
         exception.expectMessage("clazz");
 
         Lookups.create(CountyCode.codes, (Class<CountyCode>) null, "code");
     }
 
     @Test
     public void createWithDefaultAndClass_chokes_onNullClass() {
         exception.expect(IllegalArgumentException.class);
         exception.expectMessage("clazz");
 
         Lookups.create(CountyCode.DEFAULT, CountyCode.codes, (Class<CountyCode>) null, "code");
     }
 
     @Test
     public void create_chokes_onNullConverter() {
         exception.expect(IllegalArgumentException.class);
         exception.expectMessage("converter");
 
         Lookups.create(CountyCode.DEFAULT, CountyCode.codes, (Converter<CountyCode, Object>) null);
     }
 
     @Test
     public void create_chokes_onDuplicateValues() {
         List<CountyCode> codes = new ArrayList<CountyCode>();
         codes.add(new CountyCode(1, "A", "X"));
         codes.add(new CountyCode(1, "B", "Y"));
         exception.expect(DuplicateKeyException.class);
 
         Lookups.create(CountyCode.DEFAULT, codes, "code");
     }
 
     public static class MapWithDefaultFound extends LookupWithDefaultFoundTest<String> {
         public MapWithDefaultFound() {
             super(2, "X", equalTo("Z"), equalTo("B"));
         }
 
         @Override
         protected Lookup<String> newLookup() {
             return Lookups.create(map, "Z");
         }
     }
 
     public static class MapWithDefaultNotFound extends LookupWithDefaultNotFoundTest<String> {
         public MapWithDefaultNotFound() {
             super(-1, "X", equalTo("Z"));
         }
 
         @Override
         protected Lookup<String> newLookup() {
             return Lookups.create(map, "Z");
         }
     }
 
     public static class MapWithoutDefaultFound extends LookupWithoutDefaultFoundTest<String> {
         public MapWithoutDefaultFound() {
             super(2, "X", equalTo("B"));
         }
 
         @Override
         protected Lookup<String> newLookup() {
             return Lookups.create(map);
         }
     }
 
     public static class MapWithoutDefaultNotFound extends LookupWithoutDefaultNotFoundTest<String> {
         public MapWithoutDefaultNotFound() {
             super(-1, "X");
         }
 
         @Override
         protected Lookup<String> newLookup() {
             return Lookups.create(map);
         }
     }
 
     public static class WithDefaultFound extends LookupWithDefaultFoundTest<CountyCode> {
         public WithDefaultFound() {
             super(found.getCode(), CountyCode.DEFAULT, equalTo(CountyCode.DEFAULT), equalTo(found));
         }
 
         @Override
         protected Lookup<CountyCode> newLookup() {
             return Lookups.create(CountyCode.DEFAULT, CountyCode.codes, "code");
         }
     }
 
     public static class WithDefaultNotFound extends LookupWithDefaultNotFoundTest<CountyCode> {
         public WithDefaultNotFound() {
             super(-1, CountyCode.DEFAULT, equalTo(CountyCode.DEFAULT));
         }
 
         @Override
         protected Lookup<CountyCode> newLookup() {
             return Lookups.create(CountyCode.DEFAULT, CountyCode.codes, CountyCode.class, "code");
         }
     }
 
     public static class WithoutDefaultFound extends LookupWithoutDefaultFoundTest<CountyCode> {
         public WithoutDefaultFound() {
             super(found.getCode(), CountyCode.DEFAULT, equalTo(found));
         }
 
         @Override
         protected Lookup<CountyCode> newLookup() {
             return Lookups.create(null, CountyCode.codes, "code");
         }
     }
 
     public static class WithoutDefaultNotFound extends LookupWithoutDefaultNotFoundTest<CountyCode> {
         public WithoutDefaultNotFound() {
             super(-1, CountyCode.DEFAULT);
         }
 
         @Override
         protected Lookup<CountyCode> newLookup() {
             return Lookups.create(CountyCode.codes, CountyCode.class, "code");
         }
     }
 
     public static class UseConverter extends LookupWithDefaultFoundTest<CountyCode> {
         public UseConverter() {
             super(found.getCode(), CountyCode.DEFAULT, equalTo(CountyCode.DEFAULT), equalTo(found));
         }
 
         @Override
         protected Lookup<CountyCode> newLookup() {
             return Lookups.create(CountyCode.DEFAULT, CountyCode.codes, toCode);
         }
     }
 
 }
