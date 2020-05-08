 /*
  * Copyright 2013 Turkcell Teknoloji Inc. and individual contributors
  * by the @authors tag.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.ttech.cordovabuild.domain.application;
 
 import com.ttech.cordovabuild.domain.asset.AssetRef;
 import com.ttech.cordovabuild.domain.user.User;
import org.springframework.transaction.annotation.Transactional;
 
@Transactional
 public interface ApplicationService {
 
 	Application createApplication(User owner, String repositoryURI);
 
 	Application createApplication(User owner, AssetRef assetRef);
 
 	ApplicationBuilt prepareApplicationBuilt(Application application);
 
 	Application findApplication(Long id);
 
     ApplicationBuilt findApplicationBuilt(Long id);
 
     ApplicationBuilt addBuiltTarget(ApplicationBuilt applicationBuilt,BuiltTarget builtTarget);
 }
