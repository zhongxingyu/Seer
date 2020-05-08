 /*
 The MIT License (MIT)
 
Copyright (c) 2013 Joey Gibson <joey@joeygibson.com>
 
 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in
 the Software without restriction, including without limitation the rights to
 use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 the Software, and to permit persons to whom the Software is furnished to do so,
 subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
 
 package com.joeygibson.option;
 
 import static com.joeygibson.option.Option.option;
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import java.util.NoSuchElementException;
 
 public class OptionTest
 {
     @Test
     public void testGetOrElseOnSome() throws Exception {
         String string = "foo";
 
         Option<String> foo = option(string);
 
         assertThat(foo, notNullValue());
         assertThat(foo.getOrElse(""), equalTo(string));
     }
 
     @Test
     public void testGetOrElseOnNone() throws Exception {
         String nothing = "nothing";
 
         Option<String> foo = option(null);
 
         assertThat(foo, notNullValue());
         assertThat(foo.getOrElse(nothing), equalTo(nothing));
     }
 
     @Test
     public void testGetOnSome() throws Exception {
         String string = "foo";
 
         Option<String> foo = option(string);
 
         assertThat(foo, notNullValue());
         assertThat(foo.get(), equalTo(string));
     }
 
     @Test(expected = NoSuchElementException.class)
     public void testGetOnNone() throws Exception {
         Option<String> foo = option(null);
 
         assertThat(foo, notNullValue());
 
         foo.get();
     }
 
     @Test
     public void testIsDefined() throws Exception {
         Option<String> some = option("foo");
         Option<String> none = option(null);
 
         assertThat(some, notNullValue());
         assertThat(none, notNullValue());
 
         assertThat(some.isDefined(), is(true));
         assertThat(some.isEmpty(), is(false));
 
         assertThat(none.isDefined(), is(false));
         assertThat(none.isEmpty(), is(true));
     }
 }
