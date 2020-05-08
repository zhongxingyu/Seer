 /**
  * Acceso Inteligente
  *
  * Copyright (C) 2010-2011 Fundación Ciudadano Inteligente
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.accesointeligente.server;
 
 import org.apache.log4j.Logger;
 
 import java.util.Calendar;
 import java.util.Timer;
 
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 public class BackgroundServiceManager implements ServletContextListener {
 	private static final Logger logger = Logger.getLogger(BackgroundServiceManager.class);
 	private Timer timer;
 
 	@Override
 	public void contextDestroyed(ServletContextEvent event) {
 		logger.info("Context destroyed");
 		timer = null;
 	}
 
 	@Override
 	public void contextInitialized(ServletContextEvent event) {
 		logger.info("Context initialized");
 		timer = new Timer();
		timer.scheduleAtFixedRate(new ResponseCheckerTask(), 60000, 3600000);
 
 		Calendar cal = Calendar.getInstance();
 		cal.add(Calendar.DAY_OF_MONTH, 1);
 		cal.set(Calendar.HOUR_OF_DAY, 0);
 		cal.set(Calendar.MINUTE, 0);
 		cal.set(Calendar.SECOND, 0);
 		cal.set(Calendar.MILLISECOND, 0);
 
		timer.scheduleAtFixedRate(new RequestCreationTask(), cal.getTime(), 86400000);
		timer.scheduleAtFixedRate(new RequestUpdateTask(), 60000, 3600000);
 
 		cal.add(Calendar.HOUR_OF_DAY, 3);
 
		timer.scheduleAtFixedRate(new RobotCheckTask(), cal.getTime(), 86400000);
 	}
 }
