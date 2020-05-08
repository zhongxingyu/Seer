 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.monitor.client;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.osgi.service.log.LogService;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.equinox.log.Logger;
 
 import org.eclipse.riena.core.Log4r;
 import org.eclipse.riena.core.util.Iter;
 import org.eclipse.riena.core.wire.Wire;
 import org.eclipse.riena.core.wire.WirePuller;
 import org.eclipse.riena.core.wire.WireWith;
 import org.eclipse.riena.monitor.client.Category;
 import org.eclipse.riena.monitor.client.IAggregator;
 import org.eclipse.riena.monitor.client.IClientInfoProvider;
 import org.eclipse.riena.monitor.client.ICollector;
 import org.eclipse.riena.monitor.client.ISender;
 import org.eclipse.riena.monitor.client.IStore;
 import org.eclipse.riena.monitor.common.Collectible;
 
 /**
  * The {@code Aggregator} aggregates all collectibles from the collectors. Each
  * collectible may trigger the transmission of the collectibles.
  */
 @WireWith(AggregatorWiring.class)
 public class Aggregator implements IAggregator {
 
 	private IClientInfoProvider clientInfoProvider;
 	private IStore store;
 	private WirePuller storeWiring;
 	private ISender sender;
 	private WirePuller senderWiring;
 	private ICollector[] collectors;
 	private List<WirePuller> collectorWirings;
 	private boolean workerStop;
 	private boolean started;
 	private final BlockingQueue<Runnable> workQueue;
 	private final Map<String, Category> nameCategories = new HashMap<String, Category>();
 	private final Map<ICollector, Category> collectorCategories = new HashMap<ICollector, Category>();
 	private final CountDownLatch workSignal;
 
 	private static final Logger LOGGER = Log4r.getLogger(Activator.getDefault(), Aggregator.class);
 
 	public Aggregator() {
 		workSignal = new CountDownLatch(1);
 		workQueue = new LinkedBlockingQueue<Runnable>();
 		startWorker();
 	}
 
 	private void startWorker() {
 		new Thread(new Worker(), "Client Monitoring Aggregator Worker").start(); //$NON-NLS-1$
 	}
 
 	public synchronized void start() {
 		if (started) {
 			return;
 		}
 		if (collectors.length == 0) {
 			LOGGER.log(LogService.LOG_WARNING, "Client monitoring not started. No collectors defined."); //$NON-NLS-1$
 			return;
 		}
 		if (sender == null) {
 			LOGGER.log(LogService.LOG_WARNING, "Client monitoring not started. No sender defined."); //$NON-NLS-1$
 			return;
 		}
 		if (store == null) {
 			LOGGER.log(LogService.LOG_WARNING, "Client monitoring not started. No store defined."); //$NON-NLS-1$
 			return;
 		}
 		store.open(nameCategories);
 		sender.start(store, nameCategories.values());
 		for (ICollector collector : Iter.able(collectors)) {
 			collector.start(this, collectorCategories.get(collector), clientInfoProvider);
 		}
 		workerStop = false;
 		started = true;
 		workSignal.countDown();
 	}
 
 	public synchronized void stop() {
 		if (!started) {
 			return;
 		}
 		stopWorker();
 		stopCollectors();
 		stopSender();
 		stopStore();
 		started = false;
 	}
 
	public void update(final IClientInfoProviderExtension clientInfoProviderExtension) throws CoreException {
 		stopSender();
 		if (clientInfoProviderExtension == null) {
 			return;
 		}
 		clientInfoProvider = clientInfoProviderExtension.createClientInfoProvider();
 	}
 
 	public synchronized void update(final ICollectorExtension[] collectorExtensions) {
 		stopCollectors();
 		nameCategories.clear();
 		collectorCategories.clear();
 		List<ICollector> list = new ArrayList<ICollector>(collectorExtensions.length);
 		collectorWirings = new ArrayList<WirePuller>(collectorExtensions.length);
 		for (ICollectorExtension extension : collectorExtensions) {
 			Assert.isLegal(!nameCategories.containsKey(extension.getCategory()), "Category " + extension.getCategory() //$NON-NLS-1$
 					+ " is defined twice. Categories must be unique."); //$NON-NLS-1$
 			Category category = new Category(extension.getCategory(), extension.getMaxItems());
 			nameCategories.put(extension.getCategory(), category);
 			ICollector collector = extension.createCollector();
 			collectorCategories.put(collector, category);
 			collectorWirings.add(Wire.instance(collector).andStart(Activator.getDefault().getContext()));
 			list.add(collector);
 		}
 		collectors = list.toArray(new ICollector[list.size()]);
 		// TODO if we were really dynamic aware we should start them here
 	}
 
 	private void stopWorker() {
 		workerStop = true;
 	}
 
 	private void stopCollectors() {
 
 		for (ICollector collector : Iter.able(collectors)) {
 			collector.stop();
 		}
 		for (WirePuller wirings : Iter.able(collectorWirings)) {
 			wirings.stop();
 		}
 	}
 
 	public void update(final ISenderExtension senderExtension) throws CoreException {
 		stopSender();
 		if (senderExtension == null) {
 			return;
 		}
 		sender = senderExtension.createSender();
 		senderWiring = Wire.instance(sender).andStart(Activator.getDefault().getContext());
 		// TODO if we were really dynamic aware we should start it here
 	}
 
 	private void stopSender() {
 		if (sender != null) {
 			sender.stop();
 			sender = null;
 		}
 		if (senderWiring != null) {
 			senderWiring.stop();
 			senderWiring = null;
 		}
 	}
 
	public void update(final IStoreExtension storeExtension) throws CoreException {
 		stopStore();
 		if (storeExtension == null) {
 			return;
 		}
 		store = storeExtension.createStore();
 		storeWiring = Wire.instance(store).andStart(Activator.getDefault().getContext());
 	}
 
 	private void stopStore() {
 		if (store != null) {
 			store.flush();
 			store = null;
 		}
 		if (storeWiring != null) {
 			storeWiring.stop();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.riena.monitor.client.ICollectingAggregator#collect(org.eclipse
 	 * .riena.monitor.common.Collectible)
 	 */
 	public synchronized void collect(final Collectible<?> collectible) {
 		boolean elementAdded = workQueue.offer(new CollectTask(store, collectible));
 		Assert.isTrue(elementAdded);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.riena.monitor.client.IAggregator#triggerTransfer(java.lang
 	 * .String)
 	 */
 	public synchronized void triggerTransfer(final String category) {
 		boolean elementAdded = workQueue.offer(new TriggerTransferTask(store, sender, category));
 		Assert.isTrue(elementAdded);
 	}
 
 	private class Worker implements Runnable {
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see java.lang.Runnable#run()
 		 */
 		public void run() {
 			try {
 				workSignal.await();
 			} catch (InterruptedException e) {
 				Thread.currentThread().interrupt();
 				return;
 			}
 
 			while (!workerStop) {
 				try {
 					Runnable runnable = workQueue.take();
 					runnable.run();
 				} catch (InterruptedException e) {
 					Thread.currentThread().interrupt();
 					break;
 				}
 			}
 		}
 	}
 
 	private static final class CollectTask implements Runnable {
 		private final IStore store;
 		private final Collectible<?> collectible;
 
 		public CollectTask(final IStore store, final Collectible<?> collectible) {
 			this.store = store;
 			this.collectible = collectible;
 		}
 
 		public void run() {
 			store.collect(collectible);
 		}
 	}
 
 	private static final class TriggerTransferTask implements Runnable {
 		private final IStore store;
 		private final ISender sender;
 		private final String category;
 
 		public TriggerTransferTask(final IStore store, final ISender sender, final String category) {
 			this.store = store;
 			this.sender = sender;
 			this.category = category;
 		}
 
 		public void run() {
 			store.prepareTransferables(category);
 			sender.triggerTransfer(category);
 		}
 	}
 }
