 /*
  * Copyright (C) 2012  David Hudson
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.londonsburning.proxy;
 
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 /**
  * Runs the ProxyPrinter.
  */
 public final class ProxyPrinterRunner {
     /**
      *
      */
     private ProxyPrinterRunner() {
     }
 
     /**
      * @param args FileName
      */
     public static void main(final String[] args) {
         String applicationContext = "applicationContext.xml";
         ClassPathXmlApplicationContext context =
                 new ClassPathXmlApplicationContext(applicationContext);
         FlagParser flagParser = (FlagParser) context.getBean("flagParser");
         flagParser.setFlags(args);
         flagParser.parse();
         ProxyPrinter printer =
                 (ProxyPrinter) context.getBean("proxyPrinter");
         printer.printProxies(flagParser);
        context.close();
     }
 }
