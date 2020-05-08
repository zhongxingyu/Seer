 /*******************************************************************************
  * Copyright (c) May 18, 2011 NetXForge.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  You should have received a copy of the GNU Lesser General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>
  *
  * Contributors:
  *    Christophe Bouhier - initial API and implementation and/or initial documentation
  *******************************************************************************/ 
 package com.netxforge.netxstudio.data.fixtures.internal;
 
 import static org.ops4j.peaberry.Peaberry.service;
 import static org.ops4j.peaberry.util.TypeLiterals.export;
 
 import com.google.inject.AbstractModule;
 import com.netxforge.netxstudio.data.fixtures.Fixtures;
 import com.netxforge.netxstudio.data.fixtures.IFixtures;
 
 /**
  * @author Christophe Bouhier christophe.bouhier@netxforge.com
  *
  */
 public class FixturesModule extends AbstractModule {
 
 	/* (non-Javadoc)
 	 * @see com.google.inject.AbstractModule#configure()
 	 */
 	@Override
 	protected void configure() {
 		bind(export(IFixtures.class)).toProvider(service(new Fixtures()).export());
//		this.bind(IFixtures.class).to(Fixtures.class).in(Singleton.class);
 	}
 
 }
