 /**
  * Copyright 2013 Ordina
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package nl.ordina.bag.etl.mail;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.log4j.Level;
 
 import nl.ordina.bag.etl.loader.MutatiesLoader;
 import nl.ordina.bag.etl.mail.loader.MailProcessor;
 import nl.ordina.bag.etl.util.BeanLocator;
 import nl.ordina.bag.etl.util.Log4jUtils;
 
 public class LoadMutaties
 {
 	public static Log logger = LogFactory.getLog(LoadMutaties.class);
 
 	public static void main(String[] args) throws Exception
 	{
 		if (args.length >= 0 && args.length < 2)
 		{
 			logger.info("LoadMutaties started");
 			BeanLocator beanLocator = BeanLocator.getInstance("nl/ordina/bag/etl/mail/applicationContext.xml");
			if (args.length == 2)
 				Log4jUtils.setLogLevel("nl.ordina.bag.etl",Level.toLevel(args[0].trim()));
 			MailProcessor mutatiesFileLoader = (MailProcessor)beanLocator.get("mutatiesFileLoader");
 			MutatiesLoader mutatiesLoader = (MutatiesLoader)beanLocator.get("mutatiesLoader");
 			mutatiesFileLoader.processMessages();
 			mutatiesLoader.execute();
 			logger.info("LoadMutaties finished");
 		}
 		else
 			System.out.println("Usage: nl.ordina.bag.etl.mail.LoadMutaties [<loglevel>]");
 		System.exit(0);
 	}
 }
