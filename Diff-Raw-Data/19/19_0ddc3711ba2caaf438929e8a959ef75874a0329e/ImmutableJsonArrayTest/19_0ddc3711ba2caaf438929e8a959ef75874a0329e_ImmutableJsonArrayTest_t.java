 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 package org.lafayette.server.web.json;
 
 import com.google.common.collect.Lists;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.junit.Test;
 import static org.junit.Assert.assertThat;
 import static org.hamcrest.Matchers.*;
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONObject;
 import org.codehaus.jettison.json.JSONException;
 
 /**
  * Tests for {@code ImmutableJsonArray}.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class ImmutableJsonArrayTest {
 
     @Test
     public void copyOf_strings() throws JSONException {
         final JSONArray src = new JSONArray();
         src.put("foo");
         src.put("bar");
         src.put("baz");
         final ImmutableJsonArray sut = ImmutableJsonArray.copyOf(src);
         assertThat(sut.getProperties(), contains((Object) "foo", "bar", "baz"));
     }
 
     @Test
     public void copyOf_boolean() throws JSONException {
         final JSONArray src = new JSONArray();
         src.put(true);
         src.put(true);
         src.put(false);
         final ImmutableJsonArray sut = ImmutableJsonArray.copyOf(src);
         assertThat(sut.getProperties(), contains((Object) true, true, false));
     }
 
     @Test
     public void copyOf_int() throws JSONException {
         final JSONArray src = new JSONArray();
         src.put(11);
         src.put(22);
         src.put(33);
         final ImmutableJsonArray sut = ImmutableJsonArray.copyOf(src);
         assertThat(sut.getProperties(), contains((Object) 11, 22, 33));
     }
 
     @Test
     public void copyOf_mixed() throws JSONException {
         final JSONArray src = new JSONArray();
         src.put(11);
         src.put(true);
         src.put("foo");
         final ImmutableJsonArray sut = ImmutableJsonArray.copyOf(src);
         assertThat(sut.getProperties(), contains((Object) 11, true, "foo"));
     }
 
     @Test
     @SuppressWarnings("unchecked") // Because JSONArray extends ArrayList as raw type.
     public void copyOf_arrays() throws JSONException {
         final JSONArray src = new JSONArray();
         final JSONArray arr1 = new JSONArray();
         arr1.put("foo");
         arr1.put("bar");
         arr1.put("baz");
         src.put(arr1);
         final JSONArray arr2 = new JSONArray();
         arr2.put(1);
         arr2.put(2);
         arr2.put(3);
         src.put(arr2);
         final ImmutableJsonArray sut = ImmutableJsonArray.copyOf(src);
         final List<Object> properties = Lists.newArrayList(sut.getProperties());
         assertThat(((ImmutableJsonArray) properties.get(0)).getProperties(), contains((Object) "foo", "bar", "baz"));
         assertThat(((ImmutableJsonArray) properties.get(1)).getProperties(), contains((Object) 1, 2, 3));
     }
 
     @Test
     @SuppressWarnings("unchecked") // Because JSONArray extends ArrayList as raw type.
     public void copyOf_object() throws JSONException {
         final JSONArray src = new JSONArray();
         final JSONObject obj1 = new JSONObject();
         obj1.put("foo", "bar");
         obj1.put("baz", "snafu");
         src.put(obj1);
         final JSONObject obj2 = new JSONObject();
         obj2.put("foo", 1);
         obj2.put("bar", 2);
         obj2.put("baz", 3);
         src.put(obj2);
         final ImmutableJsonArray sut = ImmutableJsonArray.copyOf(src);
         final List<Object> properties = Lists.newArrayList(sut.getProperties());
         final ImmutableJsonObject imObj1 = (ImmutableJsonObject) properties.get(0);
         assertThat((Map<String, Object>) imObj1.getProperties(), allOf(
             hasEntry("foo", (Object) "bar"),
             hasEntry("baz", (Object) "snafu")
         ));
         final ImmutableJsonObject imObj2 = (ImmutableJsonObject) properties.get(1);
         assertThat((Map<String, Object>) imObj2.getProperties(), allOf(
             hasEntry("foo", (Object) 1),
             hasEntry("bar", (Object) 2),
             hasEntry("baz", (Object) 3)
         ));
     }
 
     @Test
     public void createOrigin_strings() throws JSONException {
         final ImmutableJsonArray sut = new ImmutableJsonArray(Lists.<Object>newArrayList("foo", "bar", "baz"));
         final JSONArray arr = sut.createOrigin();
         assertThat(arr.length(), is(3));
         assertThat(arr.get(0), is(equalTo((Object) "foo")));
         assertThat(arr.get(1), is(equalTo((Object) "bar")));
         assertThat(arr.get(2), is(equalTo((Object) "baz")));
     }
 
     @Test
     public void createOrigin_boolean() throws JSONException {
         final ImmutableJsonArray sut = new ImmutableJsonArray(Lists.<Object>newArrayList(true, false, false));
         final JSONArray arr = sut.createOrigin();
         assertThat(arr.length(), is(3));
         assertThat(arr.get(0), is(equalTo((Object) true)));
         assertThat(arr.get(1), is(equalTo((Object) false)));
         assertThat(arr.get(2), is(equalTo((Object) false)));
     }
 
     @Test
     public void createOrigin_int() throws JSONException {
         final ImmutableJsonArray sut = new ImmutableJsonArray(Lists.<Object>newArrayList(11, 22, 33));
         final JSONArray arr = sut.createOrigin();
         assertThat(arr.length(), is(3));
         assertThat(arr.get(0), is(equalTo((Object) 11)));
         assertThat(arr.get(1), is(equalTo((Object) 22)));
         assertThat(arr.get(2), is(equalTo((Object) 33)));
     }
 
     @Test
     public void createOrigin_mixed() throws JSONException {
         final ImmutableJsonArray sut = new ImmutableJsonArray(Lists.<Object>newArrayList("foo", 23, true));
         final JSONArray arr = sut.createOrigin();
         assertThat(arr.length(), is(3));
         assertThat(arr.get(0), is(equalTo((Object) "foo")));
         assertThat(arr.get(1), is(equalTo((Object) 23)));
         assertThat(arr.get(2), is(equalTo((Object) true)));
     }
 
     @Test
     public void createOrigin_arrays() throws JSONException {
         final ImmutableJsonArray imArr1 = new ImmutableJsonArray(Lists.<Object>newArrayList("foo", "bar", "baz"));
         final ImmutableJsonArray imArr2 = new ImmutableJsonArray(Lists.<Object>newArrayList(1, 2, 3));
         final ImmutableJsonArray sut = new ImmutableJsonArray(Lists.<Object>newArrayList(imArr1, imArr2));
         final JSONArray outerArr = sut.createOrigin();
         assertThat(outerArr.length(), is(2));
         final JSONArray arr1 = (JSONArray) outerArr.get(0);
         assertThat(arr1.get(0), is(equalTo((Object) "foo")));
         assertThat(arr1.get(1), is(equalTo((Object) "bar")));
         assertThat(arr1.get(2), is(equalTo((Object) "baz")));
         final JSONArray arr2 = (JSONArray) outerArr.get(1);
         assertThat(arr2.get(0), is(equalTo((Object) 1)));
         assertThat(arr2.get(1), is(equalTo((Object) 2)));
         assertThat(arr2.get(2), is(equalTo((Object) 3)));
     }
 
     @Test
     public void createOrigin_objects() throws JSONException {
        final ImmutableJsonObject imObj1 = new ImmutableJsonObject(new HashMap<String, Object>() {
            {
                put("foo", "bar");
                put("baz", "snafu");
            }
        });
        final ImmutableJsonObject imObj2 = new ImmutableJsonObject(new HashMap<String, Object>() {
            {
                put("foo", 1);
                put("bar", 2);
                put("baz", 3);
            }
        });
         final ImmutableJsonArray sut = new ImmutableJsonArray(Lists.<Object>newArrayList(imObj1, imObj2));
         final JSONArray outerArr = sut.createOrigin();
         assertThat(outerArr.length(), is(2));
         final JSONObject obj1 = (JSONObject) outerArr.get(0);
         assertThat(obj1.get("foo"), is(equalTo((Object) "bar")));
         assertThat(obj1.get("baz"), is(equalTo((Object) "snafu")));
         final JSONObject obj2 = (JSONObject) outerArr.get(1);
         assertThat(obj2.get("foo"), is(equalTo((Object) 1)));
         assertThat(obj2.get("bar"), is(equalTo((Object) 2)));
         assertThat(obj2.get("baz"), is(equalTo((Object) 3)));
     }
 
 }
