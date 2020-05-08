 /**
  * Copyright (c) 2007 - 2013 www.Abiss.gr
  *
  * This file is part of Calipso, a software platform by www.Abiss.gr.
  *
  * Calipso is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Calipso is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with Calipso. If not, see http://www.gnu.org/licenses/agpl.html
  */
 package gr.abiss.calipso.controller;
 
 import gr.abiss.calipso.jpasearch.controller.AbstractServiceBasedRestController;
 import gr.abiss.calipso.model.User;
 import gr.abiss.calipso.service.UserService;
 
 import javax.inject.Inject;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 
 @Controller
@RequestMapping(value = "/api/user", produces = { "application/json", "application/xml" })
 public class UserController extends AbstractServiceBasedRestController<User, String, UserService> {
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
 
 	@Override
 	@Inject
 	public void setService(UserService service) {
 		this.service = service;
 	}
 
 	@Override
 	public User create(@RequestBody User resource) {
 		User user = super.create(resource);
 		return user;
 
 	}
     
 }
