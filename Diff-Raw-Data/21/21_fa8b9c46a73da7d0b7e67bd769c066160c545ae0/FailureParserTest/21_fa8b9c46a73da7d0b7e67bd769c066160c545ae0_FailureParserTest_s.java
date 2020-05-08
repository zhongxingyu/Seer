 // Copyright 2011 Google Inc. All Rights Reserved.
 
 package com.google.jstestdriver;
 
 import com.google.common.base.Joiner;
 import com.google.common.collect.Lists;
 import com.google.gson.Gson;
import com.google.jstestdriver.FailureParser.Failure;
 import com.google.jstestdriver.model.ConcretePathPrefix;
 import com.google.jstestdriver.model.NullPathPrefix;
 
 import static com.google.jstestdriver.FailureParser.Failure;
 
 import java.util.List;
 
 /**
  * Tests for the FailureParser.
  *
  * @author Cory Smith (corbinrsmith@gmail.com)
  *
  */
 public class FailureParserTest extends junit.framework.TestCase {
   Gson gson = new Gson();
 
   public void testParse() throws Exception {
     String name1 = "TypeError";
     String message1 = "Whut?";
     List<String> stack1 = Lists.newArrayList("/static/asserts.js", "/test/foo.js");
     
     Failure failure1 = new Failure(String.format("%s: %s", name1, message1),
         Lists.newArrayList(String.format("%s: %s", name1, message1), "", stack1.get(1)));
 
     String jsonFailures = gson.toJson(Lists.newArrayList(
         new JsException(name1, message1, "doof.js", 1l, Joiner.on("\n").join(stack1))));
     List<Failure> failures = new FailureParser(new NullPathPrefix()).parse(jsonFailures);
     assertEquals(failure1, failures.get(0));
   }
 
   public void testParsePrefixedPathStack() throws Exception {
     ConcretePathPrefix prefix = new ConcretePathPrefix("/jstd");
     String name1 = "TypeError";
     String message1 = "Whut?";
     List<String> stack1 = Lists.newArrayList(
         prefix.prefixPath("/static/asserts.js"), prefix.prefixPath("/test/foo.js"));
     Failure failure1 = new Failure(String.format("%s: %s", name1, message1),
         Lists.newArrayList(String.format("%s: %s", name1, message1), "", stack1.get(1)));
 
     String jsonFailures = gson.toJson(Lists.newArrayList(
         new JsException(name1, message1, "doof.js", 1l, Joiner.on("\n").join(stack1))));
     List<Failure> failures = new FailureParser(prefix).parse(jsonFailures);
     assertEquals(failure1, failures.get(0));
   }
 
   public void testParseMultipleErrors() throws Exception {
     String name1 = "TypeError";
     String message1 = "Whut?";
     List<String> stack1 = Lists.newArrayList("/static/asserts.js", "/test/foo.js");
     Failure failure1 = new Failure(String.format("%s: %s", name1, message1),
         Lists.newArrayList(String.format("%s: %s", name1, message1), "", stack1.get(1)));
 
     String name2 = "TypeError";
     String message2 = "Thuw?";
     String failureMessage1 = String.format("%s: %s", name1, message1);
     List<String> stack2 = Lists.newArrayList("/static/asserts.js", "/test/foo.js", "/test/baz.js");
     Failure failure2 = new Failure(String.format("%s: %s", name2, message2),
         Lists.newArrayList(String.format("%s: %s", name2, message2), "", stack2.get(1), stack2.get(2)));
 
     String jsonFailures = gson.toJson(Lists.newArrayList(
         new JsException(name1, message1, "doof.js", 1l, Joiner.on("\n").join(stack1)),
         new JsException(name2, message2, "food.js", 3l, Joiner.on("\n").join(stack2))));
 
     List<Failure> failures = new FailureParser(new NullPathPrefix()).parse(jsonFailures);
 
     assertEquals(failure1, failures.get(0));
     assertEquals(failure2, failures.get(1));
   }
 }
