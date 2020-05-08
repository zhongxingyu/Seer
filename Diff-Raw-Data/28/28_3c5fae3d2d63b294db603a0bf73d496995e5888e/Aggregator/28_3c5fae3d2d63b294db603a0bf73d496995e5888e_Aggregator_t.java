 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
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
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.equinox.log.Logger;
 import org.eclipse.riena.core.injector.Inject;
 import org.eclipse.riena.core.util.Iter;
 import org.eclipse.riena.monitor.client.Category;
 import org.eclipse.riena.monitor.client.ICollectingAggregator;
 import org.eclipse.riena.monitor.client.ICollector;
 import org.eclipse.riena.monitor.client.ISender;
 import org.eclipse.riena.monitor.client.IStore;
 import org.eclipse.riena.monitor.common.Collectible;
 import org.osgi.service.log.LogService;
 
 /**
  * The {@code Aggregator} aggregates all collectibles from the collectors. Each
  * collectible may trigger the transmission of the collectibles.
  */
 public class Aggregator implements ICollectingAggregator {
 
 	private IStore store;
 	private ISender sender;
 	private ICollector[] collectors;
 	private boolean started;
 	private final BlockingQueue<Runnable> workQueue;
 	private final Map<String, Category> categories = new HashMap<String, Category>();
 	private final CountDownLatch workSignal;
 
 	private static final Logger LOGGER = Activator.getDefault().getLogger(Aggregator.class);
 
 	public Aggregator() {
 		this(true);
 	}
 
 	/**
 	 * Constructor that should only be used while testing
 	 * 
 	 * @param autoConfig
 	 *            true perform configuration; otherwise do not configure
 	 */
 	protected Aggregator(boolean autoConfig) {
 		workSignal = new CountDownLatch(1);
 		workQueue = new LinkedBlockingQueue<Runnable>();
		startWorker();
 		if (autoConfig) {
 			Inject
 					.extension("org.eclipse.riena.monitor.collectors").useType(ICollectorExtension.class).into(this).andStart(Activator.getDefault().getContext()); //$NON-NLS-1$
 			Inject
 					.extension("org.eclipse.riena.monitor.store").expectingMinMax(0, 1).useType(IStoreExtension.class).into(this).andStart(Activator.getDefault().getContext()); //$NON-NLS-1$
 			Inject
 					.extension("org.eclipse.riena.monitor.sender").expectingMinMax(0, 1).useType(ISenderExtension.class).into(this).andStart(Activator.getDefault().getContext()); //$NON-NLS-1$
 		}
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
 		store.setCategories(categories);
 		sender.setCategories(categories);
 		store.open();
 		sender.start();
 		for (ICollector collector : Iter.able(collectors)) {
 			collector.start();
 		}
 		started = true;
 		workSignal.countDown();
 	}
 
 	public synchronized void stop() {
 		if (!started) {
 			return;
 		}
 		for (ICollector collector : Iter.able(collectors)) {
 			collector.stop();
 		}
 		sender.stop();
 		store.close();
 		started = false;
 	}
 
 	public synchronized void update(final ICollectorExtension[] collectorExtensions) {
 		for (ICollector collector : Iter.able(this.collectors)) {
 			collector.stop();
 		}
 		List<ICollector> list = new ArrayList<ICollector>(collectorExtensions.length);
 		for (ICollectorExtension extension : collectorExtensions) {
 			Assert.isLegal(!categories.containsKey(extension.getCategory()), "Category " + extension.getCategory() //$NON-NLS-1$
 					+ " is defined twice. Categories must be unique."); //$NON-NLS-1$
 			Category category = new Category(extension.getCategory(), extension.getMaxItems());
 			categories.put(extension.getCategory(), category);
 			ICollector collector = extension.createCollector();
 			collector.setCategory(category);
 			collector.setAggregator(this);
 			list.add(collector);
 		}
 		collectors = list.toArray(new ICollector[list.size()]);
 		// TODO if we were really dynamic aware we should start them here
 	}
 
 	public void update(final ISenderExtension senderExtension) throws CoreException {
 		if (sender != null) {
 			sender.stop();
 		}
 		sender = null;
 		if (senderExtension == null) {
 			return;
 		}
 		sender = senderExtension.createSender();
 		sender.setStore(store);
 		// TODO if we were really dynamic aware we should start it here
 	}
 
 	public void update(final IStoreExtension storeExtension) throws CoreException {
 		if (store != null) {
 			store.flush();
 		}
 		store = null;
 		if (storeExtension == null) {
 			return;
 		}
 		store = storeExtension.createStore();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.riena.monitor.client.ICollectingAggregator#collect(org.eclipse
 	 * .riena.monitor.common.Collectible)
 	 */
 	public synchronized void collect(final Collectible<?> collectible) {
		boolean elementAdded = workQueue.offer(new Runnable() {
 			public void run() {
 				store.collect(collectible);
 			}
 		});
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
		boolean elementAdded = workQueue.offer(new Runnable() {
 			public void run() {
 				store.prepareTransferables(category);
 				sender.triggerTransfer(category);
 			}
 		});
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
 
 			while (true) {
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
 }
