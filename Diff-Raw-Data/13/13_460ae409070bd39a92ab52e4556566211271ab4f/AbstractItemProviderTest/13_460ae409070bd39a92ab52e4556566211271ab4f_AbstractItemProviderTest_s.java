 /*******************************************************************************
  * Copyright (c) 2011 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Michael Jastram - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rmf.pror.reqif10.testframework;
 
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.common.command.BasicCommandStack;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.command.CommandStack;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.edit.command.AddCommand;
 import org.eclipse.emf.edit.command.CommandParameter;
 import org.eclipse.emf.edit.command.RemoveCommand;
 import org.eclipse.emf.edit.command.SetCommand;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.INotifyChangedListener;
 import org.eclipse.emf.edit.provider.ItemProvider;
 import org.eclipse.emf.edit.provider.ItemProviderAdapter;
 import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
 import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
 import org.eclipse.rmf.pror.reqif10.configuration.util.ConfigurationAdapterFactory;
 import org.eclipse.rmf.pror.reqif10.provider.ReqIF10ItemProviderAdapterFactory;
 import org.eclipse.rmf.pror.reqif10.testdata.TestData;
 import org.eclipse.rmf.pror.reqif10.xhtml.provider.XhtmlItemProviderAdapterFactory;
 import org.eclipse.rmf.reqif10.ReqIF;
 import org.junit.After;
 import org.junit.Before;
 
 /**
  * Abstract class designed for testing {@link ItemProvider}s with JUnit 4. It
  * provides a basic infrastructure of {@link #adapterFactory},
  * {@link #editingDomain} and {@link #commandStack}.
  * <p>
  * 
  * In addition, it provides a {@link #listener} that can be attached to
  * anything. The notifications it receives are collected in
  * {@link #notifications} for validation.
  * <p>
  * 
  * It is possible, to filter the notifications that are being collected. Often,
  * we attach the listener to an ItemProvider. There is only one ItemProvider
  * for a class of Objects (e.g. SpecObjects), but we're interested only in one
  * particular object. calling #setListenerFilter() can limit the Notifications
  * that are being collected.
  * 
  * Models for testing can be stored as RIF files and retrieved via
  * {@link #getTestRIF(String)}. There is {@link #getItemProvider(Object)} for
  * easily retrieve the provider for a given element.
  * 
  * @author jastram
  */
 abstract public class AbstractItemProviderTest {
 
 	/** The properly configured {@link AdapterFactory} */
 	public ComposedAdapterFactory adapterFactory;
 
 	/** The properly configured {@link EditingDomain} */
 	public AdapterFactoryEditingDomain editingDomain;
 
 	/** The properly configured {@link CommandStack} */
 	public BasicCommandStack commandStack;
 
 	/**
 	 * A listener that can be used for testing. If notified, it writes the
 	 * notification to {@link #notifications}.
 	 */
 	public INotifyChangedListener listener;
 
 	/** The notifications from {@link #listener}. */
 	public List<Notification> notifications = new ArrayList<Notification>();
 
 	private Filter filter;
 
 	@Before
 	public void setupAbstractItemProviderTest() {
 		adapterFactory = new ComposedAdapterFactory(
 				ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 		adapterFactory
 				.addAdapterFactory(new ResourceItemProviderAdapterFactory());
 		adapterFactory.addAdapterFactory(new ConfigurationAdapterFactory());
 		adapterFactory
 				.addAdapterFactory(new ReqIF10ItemProviderAdapterFactory());
 		adapterFactory.addAdapterFactory(new XhtmlItemProviderAdapterFactory());
 		adapterFactory
 				.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());
 
 		commandStack = new BasicCommandStack();
 		editingDomain = new AdapterFactoryEditingDomain(adapterFactory,
				commandStack);
 
 		listener = new INotifyChangedListener() {
 			public void notifyChanged(Notification notification) {
 				if (filter != null) {
 					if (!filter.accept(notification)) {
 						return;
 					}
 				}
 				AbstractItemProviderTest.this.notifications.add(notification);
 			}
 		};
 	}
 
 	@After
 	public void teardownAbstractItemProviderTest() {
 		adapterFactory = null;
 		editingDomain = null;
 		commandStack = null;
 		listener = null;
 		filter = null;
 		notifications = new ArrayList<Notification>();
 	}
 
 	public void setListenerFilter(Filter filter) {
 		this.filter = filter;		
 	}
 
 	/**
 	 * Builds a ReqIF model from a file that is stored in org.eclipse.rmf.pror.reqif10.testdata.
 	 * 
 	 * @param filename
 	 *            without path
 	 * @return a {@link RIF} object
 	 * @throws URISyntaxException
 	 */
 	public ReqIF getTestReqif(String filename) throws URISyntaxException {
 		URI resourceURI = TestData.getURI(filename);
 		Resource resource = editingDomain.getResourceSet().getResource(
 				resourceURI, true);
 		ReqIF reqif = (ReqIF) resource.getContents().get(0);
 		return reqif;
 	}
 
 	/**
 	 * Tries to retrieve the Itemprovider for the given object.
 	 * 
 	 * @return the ItemProvider or null.
 	 */
 	public ItemProviderAdapter getItemProvider(Object obj) {
 		// We assume that all itemproviders implement IItemLabelProvider,
 		// otherwise this wouldn't work.
 		return (ItemProviderAdapter) this.adapterFactory.adapt(obj,
 				IItemLabelProvider.class);
 	}
 
 	protected void setViaCommand(Object object, EStructuralFeature feature, Object value) {
 		ItemProviderAdapter ip = getItemProvider(object);
 		Command cmd;
 		if (feature.isMany()) {
 			List<Object> list = new ArrayList<Object>();
 			list.add(value);
 			cmd = ip.createCommand(object, editingDomain, AddCommand.class,
 					new CommandParameter(object, feature, list));
 		} else {
 			cmd = ip.createCommand(object, editingDomain, SetCommand.class,
 					new CommandParameter(object, feature, value));			
 		}
 		commandStack.execute(cmd);
 	}
 
 	protected void removeViaCommand(Object object, EStructuralFeature feature, Object value) {
 		ItemProviderAdapter ipad = getItemProvider(object);
 		Command cmd;
 		if (feature.isMany()) {
 			List<Object> list = new ArrayList<Object>();
 			list.add(value);
 			cmd = ipad.createCommand(object, editingDomain, RemoveCommand.class,
 					new CommandParameter(object, feature, list));
 		} else {
 			cmd = ipad.createCommand(object, editingDomain, SetCommand.class,
 					new CommandParameter(object, feature, null));			
 		}
 		commandStack.execute(cmd);
 	}
 }
