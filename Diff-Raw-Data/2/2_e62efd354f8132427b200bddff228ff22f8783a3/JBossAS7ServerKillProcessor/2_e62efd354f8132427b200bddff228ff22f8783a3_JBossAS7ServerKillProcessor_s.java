 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2009, Red Hat Middleware LLC, and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.arquillian.edg.failover.extension;
 
 import java.util.logging.Logger;
 
 import org.jboss.arquillian.container.impl.DefaultServerKillProcessor;
 import org.jboss.arquillian.container.spi.Container;
 import org.jboss.arquillian.container.spi.ServerKillProcessor;
 
 /**
  * JBossAS7ServerKillProcessor implementatino capable of killing an AS 7 instance
  * running locally via a shell script.
  *
  * @author <a href="mailto:mgencur@redhat.com">Martin Gencur</a>
  * @version $Revision: $
  * 
  */
 public class JBossAS7ServerKillProcessor implements ServerKillProcessor 
 {
   private final Logger log = Logger.getLogger(DefaultServerKillProcessor.class.getName());
    
    private static String killSequence = "kill -9 `ps aux | grep -v 'grep' | grep 'jboss.home.dir=[jbossHome] ' | sed -re '1,$s/[ \\t]+/ /g' | cut -d ' ' -f 2`";
    
    public void kill(Container container) throws Exception 
    {
       killSequence = killSequence.replace("[jbossHome]",container.getContainerConfiguration().getContainerProperties().get("jbossHome"));
     
       log.info("Issuing Kill Sequence: " + killSequence);
 
       Process p = Runtime.getRuntime().exec(new String[] { "sh", "-c", killSequence });
 
       p.waitFor();
 
       if (p.exitValue() != 0) 
       {
          throw new RuntimeException("Kill Sequence failed => server not killed");
       }
       
       log.info("Kill Sequence successful");
    }
 }
