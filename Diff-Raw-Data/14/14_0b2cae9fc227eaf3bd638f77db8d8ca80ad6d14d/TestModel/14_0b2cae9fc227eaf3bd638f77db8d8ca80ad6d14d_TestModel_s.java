 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package com.evidence.fe.annotation;
 
 import javax.validation.constraints.NotNull;
 
 import lombok.Delegate;
 import lombok.Getter;
 import lombok.Setter;
 import lombok.ToString;
 
 import com.evidence.fe.form.EvidenceFormFieldFactory;
 import com.evidence.fe.form.Model;
 
 /**
  * @author Michal Bocek
  * @since 1.0.0
  */
 @ToString
 @AutomaticForm(formFieldFactory = EvidenceFormFieldFactory.class)
 public class TestModel implements Model {
 
 	@Caption("Name")
 	@Order(1)
 	@NotNull
 	@Getter @Setter
 	public String name;
 
 	@Caption("Absolut Name")
 	@Order(3)
 	@Getter @Setter
 	private String absoluteName;
 	
 	@Delegate
 	private RawModel rawModel = new RawModel();
 	
 	@Delegate
 	@Order(2)
 	private TestInnerModel innerModel = new TestInnerModel();
 }
