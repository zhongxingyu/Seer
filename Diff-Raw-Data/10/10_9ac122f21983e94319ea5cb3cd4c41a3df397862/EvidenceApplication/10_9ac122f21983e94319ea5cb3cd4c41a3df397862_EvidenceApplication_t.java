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
 package com.evidence.fe;
 
 import lombok.extern.slf4j.Slf4j;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 import org.springframework.context.MessageSource;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 import org.vaadin.mvp.presenter.spring.SpringMvpApplication;
 import org.vaadin.mvp.uibinder.resource.spring.SpringUiMessageSource;
 
 import com.evidence.fe.main.MainPresenter;
 
 /**
  * @author Michal Bocek
  * @since 1.0.0
  */
 @Component("springMvpApp")
 @Scope("prototype")
 @Configurable(preConstruction = true)
 @Slf4j
 public class EvidenceApplication extends SpringMvpApplication {
 	
 	private static final long serialVersionUID = -1814298499647961355L;
 
 	private MainPresenter mainPresenter;
 	
 	@Autowired 
 	private MessageSource messageSource;
 	
 	@Override
 	public void preInit() {
 		log.debug("preInit with locale {}", this.getLocale());
		this.setLocale(this.getLocale());
		this.presenterFactory.setLocale(this.getLocale());
 		this.setMessageSource(new SpringUiMessageSource(messageSource));
 	}
 
 	@Override
 	public void postInit() {
 		mainPresenter = (MainPresenter) presenterFactory.createPresenter("mainPresenter");
 		mainPresenter.getEventBus().start(this);
 	}
 }
