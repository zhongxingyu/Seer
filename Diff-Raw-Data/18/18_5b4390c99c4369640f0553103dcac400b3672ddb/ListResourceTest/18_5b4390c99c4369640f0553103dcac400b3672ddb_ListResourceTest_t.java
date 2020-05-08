 /*******************************************************************************
 * Copyright (c) 2008, 2009 SOPERA GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * SOPERA GmbH - initial API and implementation
 *******************************************************************************/
 package org.eclipse.swordfish.registry;
 
 import java.io.StringWriter;
 import java.util.Arrays;
 import java.util.List;
 
 import static org.eclipse.swordfish.registry.IsIteratorReturning.isIteratorReturning;
 import static org.eclipse.swordfish.registry.TstData.*;
 import static org.eclipse.swordfish.registry.TstUtil.*;
 
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.assertThat;
 import static org.junit.matchers.JUnitMatchers.*;
 
 
 public class ListResourceTest {
 
 	private List<WSDLResource> input;
 
 	@Before
 	public void setUp() {
 		input = Arrays.asList(createWSDLResource(ID_1), createWSDLResource(ID_2));
 	}
 	
 	@Test
	public void testGetResources() {
 		ListResource<WSDLResource> resources = new ListResource<WSDLResource>(input);
 		assertThat(resources.getResources(), isIteratorReturning(input));
 	}
 
 	@Test
 	public void testappendContent() throws Exception{
 		StringWriter writer = new StringWriter();
 		ListResource<WSDLResource> resources = new ListResource<WSDLResource>(input);
 		resources.appendContent(writer);
 		assertThat(writer.toString(), both(containsString(ID_1)).and(containsString(ID_1)));
 	}
 }
