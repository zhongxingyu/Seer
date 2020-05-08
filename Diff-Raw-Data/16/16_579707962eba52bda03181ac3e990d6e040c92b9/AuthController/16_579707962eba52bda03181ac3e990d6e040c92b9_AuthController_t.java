 // --------------------------------------------------------------------------------
 //
 // Copyright 2013 Gonzalo Alvarez - GonzaloAlvarez.es
 // Madrid, Spain
 //
 // This file is part of Spring Rest Starter.
 // 
 // Spring Rest Starter is free software: you can redistribute it and/or
 // modify it under the terms of the GNU General Public License 
 // as published by the Free Software Foundation, either version 3
 // of the License, or (at your option) any later version.
 // 
 // Spring Rest Starter is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 // See the GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License 
 // along with Spring Rest Starter. If not, see http://www.gnu.org/licenses/.
 //
 // --------------------------------------------------------------------------------
 //
 // System : spring-rest-bootstrap
 // Sub-System : es.galvarez.rest.config
 // File Name : PingController.java
 //
 // Author : Gonzalo Alvarez
 // Creation Date : 08/10/2013
 //
 // -----------------------------------------------------------------------------
 package es.galvarez.rest.config;
 
 import org.springframework.data.rest.webmvc.RepositoryLinksResource;
 import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
 import org.springframework.http.HttpEntity;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 /**
  * @author Gonzalo Alvarez
  *
  */
 @Controller
 public class AuthController implements ResourceProcessor<RepositoryLinksResource> {
 
 	@RequestMapping("/auth")
 	public HttpEntity<String> auth() {
 		return new ResponseEntity<String>(HttpStatus.OK);
 	}
 	
 	public RepositoryLinksResource process(RepositoryLinksResource resource) {
		resource.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(AuthController.class).auth()).withRel("auth"));
 		return resource;
 	}
 
 }
