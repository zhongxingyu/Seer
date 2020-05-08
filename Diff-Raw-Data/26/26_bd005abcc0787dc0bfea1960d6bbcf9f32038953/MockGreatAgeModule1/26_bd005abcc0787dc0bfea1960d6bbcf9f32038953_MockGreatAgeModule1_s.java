 /*
  * Copyright (c) 2008-2011 Ivan Khalopik.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.example.tapestry0;
 
 import org.greatage.ioc.OrderedConfiguration;
 import org.greatage.ioc.annotations.Build;
 import org.greatage.ioc.annotations.Contribute;
 import org.greatage.ioc.annotations.Named;
 
 import java.util.List;
 
 /**
  * @author Ivan Khalopik
  * @since 1.1
  */
 public class MockGreatAgeModule1 {
 
 	@Build
 	@Named("GreatAgeMessageService")
 	public MockMessageService buildGreatAgeMessageService(final List<String> messages) {
 		return new MockMessageServiceImpl(messages);
 	}
 
 	@Contribute
 	@Named("GreatAgeMessageService")
 	public void contributeGreatAgeMessageService(final OrderedConfiguration<String> configuration) {
 		configuration.add("Great", "great");
 		configuration.add("Age", "age");
 		configuration.add("Rulezz", "rulez");
 	}
 }
