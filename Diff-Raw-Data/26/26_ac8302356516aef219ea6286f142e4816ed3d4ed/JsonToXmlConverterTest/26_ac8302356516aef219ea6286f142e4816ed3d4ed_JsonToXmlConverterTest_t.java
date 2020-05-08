 package com.xebia.workshops;
 
 import org.junit.Test;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.*;
 
 public class JsonToXmlConverterTest {
     private JsonToXmlConverter converter = new JsonToXmlConverter();
 
     // ========================================================================
     // Tests for top-level arrays and basic data types
     // ========================================================================
 
     @Test
     public void shouldConvertTopLevelEmptyArray() {
         assertThat(converter.convert("[]"), is("<array></array>"));
     }
 
     @Test
     public void shouldConvertTopLevelEmptyArrayContainingWhitespace() {
         assertThat(converter.convert("[ ]"), is("<array></array>"));
         assertThat(converter.convert("[ \n\t  ]"), is("<array></array>"));
     }
 
     @Test
     public void shouldConvertTopLevelArrayOfSingleInteger() {
         assertThat(converter.convert("[1]"), is("<array><item>1</item></array>"));
         assertThat(converter.convert("[42]"), is("<array><item>42</item></array>"));
     }
 
     @Test
     public void shouldConvertTopLevelArrayOfSingleFloat() {
         assertThat(converter.convert("[0.3]"), is("<array><item>0.3</item></array>"));
         assertThat(converter.convert("[111.333]"), is("<array><item>111.333</item></array>"));
         assertThat(converter.convert("[3.14159265358979323846264338327950288]"), is("<array><item>3.14159265358979323846264338327950288</item></array>"));
     }
 
     @Test
     public void shouldConvertTopLevelArrayOfNumbers() {
         assertThat(converter.convert("[1,0.2,3.14159265358979323846264338327950288]"),
             is("<array><item>1</item><item>0.2</item><item>3.14159265358979323846264338327950288</item></array>"));
     }
 
     @Test
     public void shouldConvertTopLevelArrayWithWhitespaceBetweenNumbers() {
         assertThat(converter.convert("[1 , 2, \n 3]"), is("<array><item>1</item><item>2</item><item>3</item></array>"));
     }
 
     @Test
     public void shouldConvertTopLevelArrayOfSingleBoolean() {
         assertThat(converter.convert("[false]"), is("<array><item>false</item></array>"));
     }
 
     @Test(expected=IllegalStateException.class)
     public void shouldFailOnTopLevelArrayOfSingleInvalidBooleanFalse() {
         assertThat(converter.convert("[falce]"), is("<array><item>false</item></array>"));
     }
 
     @Test(expected=IllegalStateException.class)
     public void shouldFailOnTopLevelArrayOfSingleInvalidBooleanTrue() {
         assertThat(converter.convert("[trui]"), is("<array><item>true</item></array>"));
     }
 
     @Test
     public void shouldConvertTopLevelArrayOfBooleans() {
         assertThat(converter.convert("[true,false,true]"), is("<array><item>true</item><item>false</item><item>true</item></array>"));
         assertThat(converter.convert("[false,true,false]"), is("<array><item>false</item><item>true</item><item>false</item></array>"));
     }
 
     @Test
     public void shouldConvertTopLevelArrayWithWhitespaceBetweenBooleans() {
         assertThat(converter.convert("[true \n, false  , \t  true]"), is("<array><item>true</item><item>false</item><item>true</item></array>"));
     }
 
     @Test
     public void shouldConvertTopLevelArrayOfSingleString() {
         assertThat(converter.convert("[\"string\"]"), is("<array><item>string</item></array>"));
     }
 
     @Test
     public void shouldConvertTopLevelArrayOfSingleStringWithEscapeSequences() {
         assertThat(converter.convert("[\"str\\ni\\\\ng\\\\\"]"), is("<array><item>str\\ni\\\\ng\\\\</item></array>"));
     }
 
     @Test
     public void shouldConvertTopLevelArrayOfSingleStringWithEscapedQuote() {
         assertThat(converter.convert("[\"str\\\"ing\"]"), is("<array><item>str\\\"ing</item></array>"));
     }
 
     @Test
     public void shouldConvertTopLevelArrayOfStrings() {
         assertThat(converter.convert("[\"string1\" , \"string2\", \"string3\"]"),
             is("<array><item>string1</item><item>string2</item><item>string3</item></array>"));
     }
 
 
     // ========================================================================
     // Tests for simple, non-nested JSON objects
     // ========================================================================
 
     @Test
     public void shouldConvertTopLevelEmptyObject() {
         assertThat(converter.convert("{}"), is("<object></object>"));
     }
 
     @Test
    public void shouldConvertTopLevelEmptyObjectContainingWhitespace() {
        assertThat(converter.convert("{ }"), is("<object></object>"));
        assertThat(converter.convert("{ \n\t\t }"), is("<object></object>"));
    }

    @Test
     public void shouldConvertTopLevelObjectWithSingleStringField() {
         assertThat(converter.convert("{\"field1\":\"value1\"}"), is("<object><field1>value1</field1></object>"));
     }
 
     @Test
     public void shouldConvertTopLevelObjectWithSingleIntegerField() {
         assertThat(converter.convert("{\"field1\":42}"), is("<object><field1>42</field1></object>"));
     }
 
     @Test
     public void shouldConvertTopLevelObjectWithSingleFloatField() {
         assertThat(converter.convert("{\"field1\":42.0}"), is("<object><field1>42.0</field1></object>"));
     }
 
     @Test
     public void shouldConvertTopLevelObjectWithSingleTrueField() {
         assertThat(converter.convert("{\"field1\":true}"), is("<object><field1>true</field1></object>"));
     }
 
     @Test
     public void shouldConvertTopLevelObjectWithSingleFalseField() {
         assertThat(converter.convert("{\"field1\":false}"), is("<object><field1>false</field1></object>"));
     }
 
     @Test
     public void shouldConvertTopLevelObjectWithTwoStringFields() {
         assertThat(converter.convert("{\"field1\":\"value1\",\"field2\":\"value2\"}"), is("<object><field1>value1</field1><field2>value2</field2></object>"));
     }
 
     @Test
     public void shouldConvertTopLevelObjectWithMixedFields() {
         assertThat(converter.convert("{\"field1\":\"value1\",\"field2\":42,\"field3\":false,\"field4\":42.0}"),
             is("<object>" +
                "<field1>value1</field1>" +
                "<field2>42</field2>" +
                "<field3>false</field3>" +
                "<field4>42.0</field4>" +
                "</object>"));
     }
 
     @Test
     public void shouldConvertTopLevelObjectWithFieldValuesContainingColons() {
         assertThat(converter.convert("{\"field1\":\"value:1\",\"field2\":\"value: 2\"}"), is("<object><field1>value:1</field1><field2>value: 2</field2></object>"));
     }
 
     @Test
     public void shouldConvertTopLevelObjectWithTwoFieldsAndWhitespaceBetweenPairs() {
         assertThat(converter.convert("{\"field1\":\"value1\" \n   , \n \t  \"field2\":false}"), is("<object><field1>value1</field1><field2>false</field2></object>"));
     }
 
     @Test
     public void shouldConvertTopLevelObjectWithWhitespaceBetweenColons() {
         assertThat(converter.convert("{\"field1\" : \"value:1\" , \"field2\" \n : \t \"value: 2\"}"), is("<object><field1>value:1</field1><field2>value: 2</field2></object>"));
     }
 
 
     // ========================================================================
     // Tests for arrays containing other arrays and objects
     // ========================================================================
 
     @Test
     public void shouldConvertTopLevelArrayWithNestedEmptyArray() {
         assertThat(converter.convert("[[]]"),
             is("<array><item><array></array></item></array>"));
     }
 
     @Test
     public void shouldConvertTopLevelArrayWithNestedEmptyArrays() {
         assertThat(converter.convert("[[] ,[  \t], [\n]]"),
             is("<array><item><array></array></item><item><array></array></item><item><array></array></item></array>"));
     }
 
     @Test
     public void shouldConvertTopLevelArrayWithNestedArray() {
         assertThat(converter.convert("[[1,2,3], \t []]"),
             is("<array>" +
                "<item><array><item>1</item><item>2</item><item>3</item></array></item>" +
                "<item><array></array></item>" +
                "</array>"));
     }
 
     @Test
     public void shouldConvertTopLevelArrayWithNestedArrays() {
         assertThat(converter.convert("[1,[2,3, 4],[5 ,6,\n7]]"),
             is("<array>" +
                    "<item>1</item>" +
                    "<item><array><item>2</item><item>3</item><item>4</item></array></item>" +
                    "<item><array><item>5</item><item>6</item><item>7</item></array></item>" +
                "</array>"));
     }
 
     @Test
     public void shouldConvertTopLevelArrayWithNestedEmptyObject() {
         assertThat(converter.convert("[{}]"), is("<array><item><object></object></item></array>"));
     }
 
     @Test
    public void shouldConvertTopLevelArrayWithNestedEmptyObjects() {
        assertThat(converter.convert("[{},\n {} , {}]"),
            is("<array><item><object></object></item><item><object></object></item><item><object></object></item></array>"));
    }

    @Test
     public void shouldConvertTopLevelArrayWithSingleNestedObject() {
         assertThat(converter.convert("[{\"field1\":\"value1\"}]"),
             is("<array><item><object><field1>value1</field1></object></item></array>"));
     }
 }
