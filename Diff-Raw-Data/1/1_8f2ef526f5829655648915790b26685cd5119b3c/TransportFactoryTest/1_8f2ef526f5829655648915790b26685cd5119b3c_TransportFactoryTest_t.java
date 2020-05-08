 /*******************************************************************************
  * Copyright (c) 2010 The Eclipse Foundation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     The Eclipse Foundation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.epp.mpc.tests.util;
 
 import static org.junit.Assert.assertNotNull;
 
 import java.io.InputStream;
 import java.net.URI;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.epp.internal.mpc.core.util.ITransport;
 import org.eclipse.epp.internal.mpc.core.util.TransportFactory;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.BlockJUnit4ClassRunner;
 
 @RunWith(BlockJUnit4ClassRunner.class)
 public class TransportFactoryTest {
 
 	@Test
 	public void testTansportFactoryInstance() {
 		ITransport transport = TransportFactory.instance().getTransport();
 		assertNotNull(transport);
 	}
 
 	@Test
 	public void testStream() throws Exception {
 		ITransport transport = TransportFactory.instance().getTransport();
 		URI uri = new URI("http://www.eclipse.org");
 		InputStream stream = transport.stream(uri, new NullProgressMonitor());
 		assertNotNull(stream);
		stream.close();
 	}
 }
