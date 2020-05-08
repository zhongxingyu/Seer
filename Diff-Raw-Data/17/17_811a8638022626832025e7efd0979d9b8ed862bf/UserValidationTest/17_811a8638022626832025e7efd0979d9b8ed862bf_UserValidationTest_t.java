 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package au.id.hazelwood.sos.model.security;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 import org.apache.commons.lang.StringUtils;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import javax.validation.ConstraintViolation;
 import javax.validation.Validator;
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.hasItems;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = "/application-context-test-validation.xml")
 public class UserValidationTest
 {
     @Autowired
     private Validator validator;
 
     @Test
     public void testValidationSuccess() throws Exception
     {
         User user = new User("ricky@hazelwood.id.au", "password", "Ricky", "Hazelwood");
         assertThat(validator.validate(user).isEmpty(), is(true));
     }
 
     @Test
     public void testEmailValidation() throws Exception
     {
         assertValidation("email", "abcdefghijklmnopqrtsuvwxyz@b.com");
         assertValidation("email", "1234567890@b.com");
         assertValidation("email", "a.z@b.com");
         assertValidation("email", "!#$%&'*+/=?^_`{|}~-.!#$%&'*+/=?^_`{|}~-@b.com");
         assertValidation("email", "a@b-c.com");
         assertValidation("email", "a@test-host.com");
         assertValidation("email", "a@test.host.com");
         assertValidation("email", "a@192.168.0.1");
         assertValidation("email", null, "may not be null");
         assertValidation("email", "", "size must be between 1 and 254", "not a well-formed email address");
         assertValidation("email", StringUtils.repeat("a", 255), "size must be between 1 and 254", "not a well-formed email address");
         assertValidation("email", "a", "not a well-formed email address");
         assertValidation("email", "a@b", "not a well-formed email address");
         assertValidation("email", "a@b.c", "not a well-formed email address");
         assertValidation("email", ".@b.com", "not a well-formed email address");
         assertValidation("email", "a.@b.com", "not a well-formed email address");
         assertValidation("email", "a@b-.com", "not a well-formed email address");
         assertValidation("email", "a@-b.com", "not a well-formed email address");
         assertValidation("email", "a@b_.com", "not a well-formed email address");
         assertValidation("email", "a@_b.com", "not a well-formed email address");
     }
 
     @Test
     public void testPassword() throws Exception
     {
         assertValidation("password", "password");
         assertValidation("password", "!#$%&'*+/=?^_`{|}~-.!#$%&'*+/=?^_`{|}~-@");
         assertValidation("password", StringUtils.repeat("a", 254));
         assertValidation("password", null, "may not be null");
         assertValidation("password", "", "size must be between 1 and 254");
         assertValidation("password", StringUtils.repeat("a", 255), "size must be between 1 and 254");
     }
 
     @Test
     public void testFirstName() throws Exception
     {
         assertValidation("firstName", "Ricky");
         assertValidation("firstName", "Ricky Jack");
         assertValidation("firstName", "O'Brien-O'Connor");
         assertValidation("firstName", "Connor Tomas O'Brien");
         assertValidation("firstName", "");
         assertValidation("firstName", null);
         assertValidation("firstName", StringUtils.repeat("a", 20));
         assertValidation("firstName", StringUtils.repeat("a", 21), "size must be between 0 and 20");
         assertValidation("firstName", " ", "not a well-formed name");
         assertValidation("firstName", "-", "not a well-formed name");
         assertValidation("firstName", "A- -B", "not a well-formed name");
     }
 
     @Test
     public void testLastName() throws Exception
     {
         assertValidation("lastName", "Hazelwood");
         assertValidation("lastName", "Hazel Wood");
         assertValidation("lastName", "O'Brien-O'Connor");
         assertValidation("lastName", "Connor Tomas O'Brien");
         assertValidation("lastName", "");
         assertValidation("lastName", null);
         assertValidation("lastName", StringUtils.repeat("a", 20));
         assertValidation("lastName", StringUtils.repeat("a", 21), "size must be between 0 and 20");
         assertValidation("lastName", " ", "not a well-formed name");
         assertValidation("lastName", "-", "not a well-formed name");
         assertValidation("lastName", "A- -B", "not a well-formed name");
     }
 
     private void assertValidation(String property, String value, String... expectedViolations)
     {
         List<String> violations = getViolations(User.class, property, value);
         assertThat(violations.size(), is(expectedViolations.length));
         assertThat(violations, hasItems(expectedViolations));
     }
 
     private List<String> getViolations(Class<?> beanType, String property, String value)
     {
        return Lists.transform(new ArrayList<>(validator.validateValue(beanType, property, value)), new ConstraintViolationMessageFunction());
    }

    private static class ConstraintViolationMessageFunction implements Function<ConstraintViolation<?>, String>
    {
        @Override
        public String apply(ConstraintViolation<?> violation)
         {
            return violation == null ? null : violation.getMessage();
        }
     }
 }
