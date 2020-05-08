 /*
  * Copyright 2000 - 2010 Ivan Khalopik. All Rights Reserved.
  */
 
 package org.greatage.ioc.resource;
 
 /**
  * @author Ivan Khalopik
  * @since 1.0
  */
 public class ClasspathResourceLocator extends AbstractResourceLocator {
 
	public ClasspathResourceLocator() {
		super(new ClasspathResource(Thread.currentThread().getContextClassLoader(), null, "", null));
 	}
 
 }
