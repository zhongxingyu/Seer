 /*
 ###############################
 # Copyright (C) 2012 Jon Schang
 # 
 # This file is part of jSchangLib, released under the LGPLv3
 # 
 # jSchangLib is free software: you can redistribute it and/or modify
 # it under the terms of the GNU Lesser General Public License as published by
 # the Free Software Foundation, either version 3 of the License, or
 # (at your option) any later version.
 # 
 # jSchangLib is distributed in the hope that it will be useful,
 # but WITHOUT ANY WARRANTY; without even the implied warranty of
 # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 # GNU Lesser General Public License for more details.
 # 
 # You should have received a copy of the GNU Lesser General Public License
 # along with jSchangLib.  If not, see <http://www.gnu.org/licenses/>.
 ###############################
 */
 package com.jonschang.investing;
 
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.*;
 import org.springframework.beans.factory.xml.*;
 import org.springframework.core.io.*;
 
 import com.jonschang.investing.model.*;
 
 public class Investing {
 
 	static private Investing instance;
	private BeanFactory beanFactory;
 	
 	synchronized static public Investing instance() {
 		if( instance==null ) {
 			com.jonschang.utils.LoggingUtils.configureLogger();
 			instance = new Investing();
 		}
 		return instance;
 	}
 	
	private Investing() {
		beanFactory = new XmlBeanFactory(new ClassPathResource("conf/spring/spring.xml"));
	}
 	
 	public ExchangeServiceFactory getExchangeServiceFactory() {
		return (ExchangeServiceFactory)(beanFactory.getBean("ExchangeServiceFactory"));
 	}
 	
 	public ExchangeContextFactory getExchangeContextFactory() {
		return (ExchangeContextFactory)(beanFactory.getBean("ExchangeContextFactory"));
 	}
 	
 	public QuotableServiceFactory getQuotableServiceFactory() {
		return (QuotableServiceFactory)(beanFactory.getBean("QuotableServiceFactory"));
 	}
 	
 	public QuoteServiceFactory getQuoteServiceFactory() {
		return (QuoteServiceFactory)(beanFactory.getBean("QuoteServiceFactory"));
 	}
 	
 	public SessionFactory getSessionFactory() {
		return (SessionFactory)beanFactory.getBean("HSF");
 	}
 }
