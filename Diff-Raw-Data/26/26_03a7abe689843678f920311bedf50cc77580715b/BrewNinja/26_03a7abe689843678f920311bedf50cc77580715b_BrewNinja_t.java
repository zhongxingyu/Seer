 /*
  * Copyright © 2013 Jarett Creason
  *
  * This file is part of BrewNinja.
  *
  * BrewNinja is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * BrewNinja is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with BrewNinja in the file named COPYING in the root directory.
  * If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.europabrewing;
 
 import com.europabrewing.daos.BurnerDAOHibernate;
 import com.europabrewing.models.Burner;
 import com.europabrewing.util.HibernateUtil;
 import com.google.common.base.Joiner;
 import com.pi4j.io.gpio.GpioController;
 import com.pi4j.io.gpio.GpioFactory;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.hibernate.Session;
 
 import java.util.List;
 
 /**
  * @author jcreason - jcreason@gmail.com
  * @date June 2013
  *
  * Please see the README and/or documentation associated
  */
 public class BrewNinja {
 
 	private final static Logger logger = LogManager.getLogger(BrewNinja.class.getName());
 
	/** The GPIO controller.  If it's null, means we're running in development mode */
 	private final GpioController gpioController;
 
 	private List<Burner> burners;
 
	/**
	 * Build the class the manages it all.
	 * On construction, all equipment is loaded in from the database
	 */
 	public BrewNinja() {
 		// create gpio controller instance
		GpioController tmpGpioCon = null;
		try {
			tmpGpioCon = GpioFactory.getInstance();
		} catch (UnsatisfiedLinkError e) {
			logger.error("Looks like you might not be running on a Raspberry Pi architecture.  Running in DEV mode.");
		}
		this.gpioController = tmpGpioCon;
 
 		initializeEquipmunk();
 
		logger.trace("All burners configured:\n\t * " + Joiner.on("\n\t * ").join(burners));
 	}
 
 	/**
 	 * MAIN
 	 *
 	 * @param args command line arguments
 	 */
 	public static void main(String[] args) {
 		BrewNinja brewNinja = new BrewNinja();
 
 	}
 
 	/**
 	 * Initialize all of the equipment including Burners, liquid Pumps,
 	 * and TempMonitors
 	 */
 	private void initializeEquipmunk() {
 		logger.trace("Retrieving all equipment from database (burners, pumps and valves)");
 
 		Session session = HibernateUtil.getSession();
 		BurnerDAOHibernate burnerDao = new BurnerDAOHibernate(session);
 		this.burners = burnerDao.getEnabledBurners();
 
 
 		session.close();
 	}
 
 }
