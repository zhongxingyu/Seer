/*
 * Copyright (C) 2011 Jeremie Huchet
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

 package fr.dudie.acrachilisync;
 
 import java.io.IOException;
 import java.text.ParseException;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.PropertiesConfiguration;
 import org.redmine.ta.AuthenticationException;
 import org.redmine.ta.NotFoundException;
 import org.redmine.ta.RedmineException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.gdata.util.ServiceException;
 
 /**
  * @author Jérémie Huchet
  */
 public final class AcraChiliSync {
 
     /** The event logger. */
     private static final Logger LOGGER = LoggerFactory.getLogger(AcraChiliSync.class);
 
     /**
      * @param args
      * @throws RedmineException
      * @throws NotFoundException
      * @throws AuthenticationException
      * @throws ServiceException
      * @throws IOException
      * @throws ParseException
      * @throws ConfigurationException
      */
     public static void main(final String[] args) throws IOException, ServiceException,
             AuthenticationException, NotFoundException, RedmineException, ParseException,
             ConfigurationException {
 
         final PropertiesConfiguration conf = new PropertiesConfiguration("acrachilisync.properties");
         final AcraToChiliprojectSyncer syncer = new AcraToChiliprojectSyncer(conf);
         syncer.startSynchronization();
     }
 }
