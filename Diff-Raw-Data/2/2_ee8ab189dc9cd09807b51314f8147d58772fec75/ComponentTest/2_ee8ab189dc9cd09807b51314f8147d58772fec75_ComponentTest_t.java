 /**
  * Fonti is a web application for billing and budgeting
  * Copyright (C) 2009  Carlos Fernandez
  *
  * This file is part of Fonti.
  *
  * Fonti is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Fonti is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.carlos.projects.billing.domain;
 
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.assertThat;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author Carlos Fernandez
  *
  * @date 25 Jul 2009
  *
  * Unit tests for {@link Component}}
  */
 public class ComponentTest {
 	private Component component;
 
 	@Before
 	public void setup() {
 		component = new Component();
 	}
 	
 	@Test
 	public void shouldCreateNonNullComponent() {
		assertThat("The component is null", component, is(notNullValue()));
 	}
 }
