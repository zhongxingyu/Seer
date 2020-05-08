 /* 
  * Audiolicious - Your Music Library Statistics
  * Copyright (C) 2011, Michal Huniewicz
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see http://www.m1key.me
  */
 
 package me.m1key.audiolicious.web.beans;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collection;
 
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIPanel;
 import javax.inject.Inject;
 
 import me.m1key.audiolicious.domain.to.LibraryTo;
 import me.m1key.audiolicious.services.LibraryImporter;
 
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.shrinkwrap.api.ArchivePaths;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
 import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.richfaces.event.FileUploadEvent;
 import org.richfaces.model.UploadedFile;
 
 @RunWith(Arquillian.class)
 public class FileUploadBeanIT {
 
 	@Inject
 	private FileUploadBean uploadBean;
 	@Inject
 	private StubConsumptionBean stubConsumptionBean;
 	@Inject
 	private StubLibrarySaveBean stubLibrarySaveBean;
 
 	private FileUploadEvent fileUploadEvent;
 
 	@Deployment
 	public static WebArchive createTestArchive()
 			throws IllegalArgumentException, IOException {
 		return ShrinkWrap
 				.create(WebArchive.class,
 						FileUploadBeanIT.class.getSimpleName() + ".war")
 				.addAsWebInfResource(
 						new File("src/test/resources/WEB-INF/beans.xml"),
 						ArchivePaths.create("beans.xml"))
 				.addClasses(AudioliciousConfigurationException.class,
 						ConsumptionBean.class, FileUploadBean.class,
						FileUploadEvent.class, LibraryImporter.class,
						LibrarySaveBean.class, LibraryTo.class,
						StubConsumptionBean.class, StubLibraryImporter.class,
						StubLibrarySaveBean.class, UploadedFile.class)
 				.addAsLibraries(
						DependencyResolvers.use(MavenDependencyResolver.class)
								.artifacts("org.slf4j:slf4j-api:1.6.1")
 								.resolveAsFiles());
 	}
 
 	@Before
 	public void setup() {
 		UIComponent component = new UIPanel();
 		UploadedFile uploadedFile = new StubUploadedFile();
 		fileUploadEvent = new FileUploadEvent(component, uploadedFile);
 	}
 
 	@Test
 	public void shouldCallConsumptionBean() throws Exception {
 		assertFalse(stubConsumptionBean.hasConsumeBeenCalled());
 		assertFalse(stubLibrarySaveBean.hasSaveBeenCalled());
 		uploadBean.listener(fileUploadEvent);
 		assertTrue(stubConsumptionBean.hasConsumeBeenCalled());
 		assertTrue(stubLibrarySaveBean.hasSaveBeenCalled());
 	}
 
 	private static class StubUploadedFile implements UploadedFile {
 
 		@Override
 		public String getContentType() {
 			return null;
 		}
 
 		@Override
 		public byte[] getData() {
 			return null;
 		}
 
 		@Override
 		public InputStream getInputStream() {
 			return null;
 		}
 
 		@Override
 		public String getName() {
 			return null;
 		}
 
 		@Override
 		public long getSize() {
 			return 0;
 		}
 
 		@Override
 		public void delete() throws IOException {
 		}
 
 		@Override
 		public String getHeader(String arg0) {
 			return null;
 		}
 
 		@Override
 		public Collection<String> getHeaderNames() {
 			return null;
 		}
 
 		@Override
 		public Collection<String> getHeaders(String arg0) {
 			return null;
 		}
 
 		@Override
 		public String getParameterName() {
 			return null;
 		}
 
 		@Override
 		public void write(String arg0) throws IOException {
 		}
 
 	}
 
 }
