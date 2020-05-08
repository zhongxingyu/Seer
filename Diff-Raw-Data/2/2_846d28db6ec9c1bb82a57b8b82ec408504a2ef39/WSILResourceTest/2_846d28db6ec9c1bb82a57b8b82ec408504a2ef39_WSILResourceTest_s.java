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
 
 import static java.util.Collections.*;
 import static org.eclipse.swordfish.registry.TestData.*;
 import static org.eclipse.swordfish.registry.TestUtilities.*;
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.*;
 import java.io.StringWriter;
 
 import org.junit.Test;
 
 public class WSILResourceTest {
 	
 	public static String BASE_URL = "baseURL";
 	
 	@Test
 	public void getMethodShouldReturnWSIlConformantXML() throws Exception {
 		StringWriter writer = new StringWriter();
 		
 		Iterable<String> empty = emptyList();
 		
 		WSILResource wsilResource = new WSILResource(BASE_URL, empty);
 		
 		wsilResource.get(writer);
 		assertThat(writer.toString(), containsString("inspection"));
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void getMethodShouldReturnElementForEachIdentifierPassed() throws Exception {
 		StringWriter writer = new StringWriter();
 		
 		Iterable<String> identifiers = asIterable(ID_1, ID_2);
 		
 		WSILResource wsilResource = new WSILResource(BASE_URL, identifiers);
 		
 		wsilResource.get(writer);
		assertThat(writer.toString(), allOf(containsString(BASE_URL + "/" + ID_1), containsString(BASE_URL + "/" + ID_2)));
 	}
 }
