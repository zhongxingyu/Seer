 package org.prot.httpfileserver;
 
 import org.apache.log4j.xml.DOMConfigurator;
 import org.eclipse.jetty.server.Server;
 import org.springframework.beans.factory.xml.XmlBeanFactory;
 import org.springframework.core.io.ClassPathResource;
 
 public class Main
 {
 	public Main()
 	{
 		// Configure logger
 		DOMConfigurator.configure(Main.class.getResource("/etc/log4j.xml"));

 		// start spring ioc container
		XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource("/etc/spring.xml", getClass()));
 
 		// start the server
 		Server server = (Server) factory.getBean("Server");
 		try
 		{
 			server.start();
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public static void main(String arg[])
 	{
 		new Main();
 	}
 }
