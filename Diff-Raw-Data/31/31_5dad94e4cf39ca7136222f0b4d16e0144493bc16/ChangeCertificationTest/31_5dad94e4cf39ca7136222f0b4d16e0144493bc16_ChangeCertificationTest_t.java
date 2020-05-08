/*******************************************************************************
 * Copyright (c) 2013 EclipseSource Muenchen GmbH.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Edgar Mueller - initial API and implementation
 ******************************************************************************/
 package org.eclipse.emf.emfstore.client.test.server;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.security.KeyStoreException;
 import java.security.cert.Certificate;
 
 import org.apache.commons.io.FileUtils;
 import org.eclipse.emf.emfstore.client.ESUsersession;
 import org.eclipse.emf.emfstore.client.exceptions.ESCertificateException;
 import org.eclipse.emf.emfstore.client.exceptions.ESInvalidCertificateException;
 import org.eclipse.emf.emfstore.client.test.Activator;
 import org.eclipse.emf.emfstore.client.test.SetupHelper;
 import org.eclipse.emf.emfstore.internal.client.model.connectionmanager.KeyStoreManager;
 import org.eclipse.emf.emfstore.internal.common.model.util.SerializationException;
 import org.eclipse.emf.emfstore.internal.server.ServerConfiguration;
 import org.eclipse.emf.emfstore.internal.server.connection.ServerKeyStoreManager;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class ChangeCertificationTest extends ServerTests {
 
 	@Override
 	@Before
 	public void before() {
 		SetupHelper.stopServer();
 		try {
 			unsetKeystoreField();
 		} catch (IllegalArgumentException e1) {
 			fail(e1.getMessage());
 		} catch (SecurityException e1) {
 			fail(e1.getMessage());
 		} catch (IllegalAccessException e1) {
 			fail(e1.getMessage());
 		} catch (NoSuchFieldException e1) {
 			fail(e1.getMessage());
 		}
 		try {
 			replaceKeystore();
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		getServer().toInternalAPI().setLastUsersession(null);
 		SetupHelper.startSever();
 	}
 
 	@Override
 	@After
 	public void teardown() throws IOException, SerializationException, ESException {
 		super.teardown();
 		try {
 			unsetKeystoreField();
 		} catch (IllegalArgumentException e) {
 			fail(e.getMessage());
 		} catch (SecurityException e) {
 			fail(e.getMessage());
 		} catch (IllegalAccessException e) {
 			fail(e.getMessage());
 		} catch (NoSuchFieldException e) {
 			fail(e.getMessage());
 		}
 		try {
 			KeyStoreManager.getInstance().removeCertificate("testAlias");
 		} catch (ESCertificateException e) {
 			fail(e.getMessage());
 		}
 		KeyStoreManager.getInstance().setDefaultCertificate("emfstore test certificate (do not use in production!)");
 	}
 
 	private void unsetKeystoreField() throws IllegalArgumentException, IllegalAccessException, SecurityException,
 		NoSuchFieldException {
 		unsetPrivateFieldOnServerKeyStoreManager("keyStore");
 	}
 
 	private void unsetPrivateFieldOnServerKeyStoreManager(String field) throws IllegalArgumentException,
 		IllegalAccessException, SecurityException,
 		NoSuchFieldException {
 		java.lang.reflect.Field privateKeystoreField = ServerKeyStoreManager.class.getDeclaredField(field);
 		privateKeystoreField.setAccessible(true);
 		privateKeystoreField.set(ServerKeyStoreManager.getInstance(), null);
 	}
 
 	private void replaceKeystore() throws IOException {
 
 		InputStream stream = getResource("sampleFiles/emfstoreServer.keystore");
 		FileUtils.copyInputStreamToFile(stream,
 			new File(ServerConfiguration.getServerKeyStorePath()));
 		stream.close();
 
 		stream = getResource("sampleFiles/es.properties");
 		FileUtils.copyInputStreamToFile(stream,
 			new File(ServerConfiguration.getConfDirectory() + File.separatorChar + "es.properties"));
 		stream.close();
 	}
 
 	private InputStream getResource(String resource) throws IOException {
 
 		URL configURL = Activator.getDefault().getBundle().getBundleContext().getBundle()
 			.getEntry(resource);
 
 		if (configURL != null) {
 			InputStream input = configURL.openStream();
 			return input;
 		}
 
 		return null;
 	}
 
 	private void importCertificate() throws ESInvalidCertificateException, ESCertificateException, IOException {
 		InputStream stream = getResource("sampleFiles/server.public-key");
 		KeyStoreManager.getInstance().addCertificate("testAlias", stream);
 		stream.close();
 	}
 
 	@Test
 	public void exchangeCertificateAndLogin()
 		throws ESInvalidCertificateException, ESCertificateException, ESException, KeyStoreException, IOException {
 		importCertificate();
 		assertFalse(KeyStoreManager.getInstance().isDefaultCertificate("testAlias"));
 		KeyStoreManager.getInstance().setDefaultCertificate("testAlias");
 		assertTrue(KeyStoreManager.getInstance().isDefaultCertificate("testAlias"));
 		getServer().setCertificateAlias("testAlias");
 		ESUsersession login = getServer().login("super", "super");
 		Certificate certificate = KeyStoreManager.getInstance().getCertificate("testAlias");
 		assertNotNull(certificate);
 		assertTrue(login.isLoggedIn());
 	}
 }
