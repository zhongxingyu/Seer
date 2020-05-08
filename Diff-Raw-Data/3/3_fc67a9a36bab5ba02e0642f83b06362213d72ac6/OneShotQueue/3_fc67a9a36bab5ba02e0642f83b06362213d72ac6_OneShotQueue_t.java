 package com.github.bcap.lightasync.impl;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.bcap.lightasync.Consumer;
 import com.github.bcap.lightasync.Producer;
 import com.github.bcap.lightasync.Queue;
 
 public class OneShotQueue<T> extends Queue<T> {
 
 	private static final Logger logger = LoggerFactory.getLogger(OneShotQueue.class);
 
 	private LinkedBlockingQueue<QueueMessage<T>> queue;
 
 	private Integer maxSize;
 	
 	private List<ConsumerThread> consumerThreads = new ArrayList<ConsumerThread>();
 	private List<ProducerThread> producerThreads = new ArrayList<ProducerThread>();
 	
 	private CountDownLatch startLatch = new CountDownLatch(1);
 	private CountDownLatch finishLatch;
 
 	public OneShotQueue() {
 		this(null);
 	}
 	
 	public void waitExecution() throws InterruptedException {
 		startLatch.await();
 		finishLatch.await();
 	}
 
 	public OneShotQueue(Integer maxSize) {
 		this.maxSize = maxSize;
 		if (maxSize != null) {
 			if (maxSize <= 0)
 				throw new IllegalArgumentException("Max size cannot be 0 or negative");
 			this.queue = new LinkedBlockingQueue<QueueMessage<T>>(maxSize);
 		} else {
 			this.queue = new LinkedBlockingQueue<QueueMessage<T>>();
 		}
 	}
 
 	protected void startConsumer(Consumer<T> consumer) {
 		
 		logger.debug("Starting consumer " + consumer);
 		
 		ConsumerThread thread = new ConsumerThread(consumer);
 		thread.startThread();
 		consumerThreads.add(thread);
 		
 		logger.debug("Consumer " + consumer + " successfully started, associated thread: " + thread.getName());
 	}
 
 	protected void startProducer(Producer<? extends T> producer) {
 		
 		logger.debug("Starting producer " + producer);
 		
 		ProducerThread thread = new ProducerThread(producer);
 		thread.startThread();
 		producerThreads.add(thread);
 		
 		logger.debug("Producer " + producer + " successfully started, associated thread: " + thread.getName());
 	}
 
 	protected void stopConsumer(Consumer<T> consumer) {
 		
 		logger.debug("Stopping consumer " + consumer);
 		
 		ConsumerThread thread = null;
 		for (Iterator<ConsumerThread> it = consumerThreads.iterator(); it.hasNext() && thread == null;) {
 			ConsumerThread itThread = it.next();
 			if (itThread.consumer == consumer)
 				thread = itThread;
 		}
 		thread.stopThread();
 		consumerThreads.remove(thread);
 		
 		logger.debug("Consumer " + consumer + " successfully stopped");
 	}
 
 	protected void stopProducer(Producer<? extends T> producer) {
 		
 		logger.debug("Stopping producer " + producer);
 		
 		ProducerThread thread = null;
 		for (Iterator<ProducerThread> it = producerThreads.iterator(); it.hasNext() && thread == null;) {
 			ProducerThread itThread = it.next();
 			if (itThread.producer == producer)
 				thread = itThread;
 		}
 		thread.stopThread();
 		producerThreads.remove(thread);
 		
 		logger.debug("Producer " + producer + " successfully stopped");
 	}
 
 	protected void preStart() {
 
 	}
 
 	protected void postStart() {
 		finishLatch = new CountDownLatch(consumerThreads.size());
 		startLatch.countDown();
 	}
 
 	protected void preShutdown() {
 
 	}
 
 	protected void postShutdown() {
 		finishLatch.countDown();
 	}
 
 	protected void producerFinished(ProducerThread producer) {
 		boolean finished = true;
 		for (Iterator<ProducerThread> it = producerThreads.iterator(); it.hasNext() && finished; finished = it.next().finished);
 
 		if(finished) {
 			for (ConsumerThread thread : consumerThreads)
 				thread.producersFinished = true;
 			
 		}
 	}
 
 	public int size() {
 		return queue.size();
 	}
 
 	public Integer maxSize() {
 		return maxSize;
 	}
 
 	private abstract class BaseThread extends Thread {
 
 		protected boolean running = false;
 
 		public synchronized void startThread() {
 			running = true;
 			super.start();
 		}
 
 		public synchronized void stopThread() {
 			running = false;
 			this.interrupt();
 		}
 	}
 
 	private class ConsumerThread extends BaseThread {
 
 		protected Consumer<T> consumer;
 
 		protected boolean producersFinished = false;
 
 		public ConsumerThread(Consumer<T> consumer) {
 			this.consumer = consumer;
 		}
 
 		public void run() {
 			while (running) {
 				try {
 					QueueMessage<T> message = queue.poll(50, TimeUnit.MILLISECONDS);
 					if (message != null) {
 						logger.debug("Consuming message " + message);
 						try {
 							consumer.consume(message.getContent());
 						} catch (RuntimeException e) {
 							logger.error("Consumer threw an Exception", e);
 						}
 					} else if (producersFinished) {
 						running = false;
 					}
 				} catch (InterruptedException e) {
 					logger.debug("Consumer thread interrupted");
 				}
 			}
 			
 			if(producersFinished)
 				consumer.finished();
 
 			running = false;
 			finishLatch.countDown();
 		}
 	}
 
 	private class ProducerThread extends BaseThread {
 
 		protected Producer<? extends T> producer;
 
 		protected boolean finished = false;
 
 		public ProducerThread(Producer<? extends T> producer) {
 			this.producer = producer;
 		}
 
 		public void run() {
 			finished = producer.isFinished();
 
 			while (running && !finished) {
 				try {
 					logger.debug("Calling produce method for producer " + producer);
 					T obj = producer.produce();
 					logger.debug("Putting a new message in the queue for the object " + obj);
 					queue.put(new QueueMessage<T>(obj));
 				} catch (InterruptedException e) {
 					logger.debug("Producer thread interrupted");
 				} catch (RuntimeException e) {
 					logger.error("Producer threw an Exception", e);
 				}
				finished = producer.isFinished();
 			}
 
 			running = false;
 
 			if (finished) {
 				logger.debug("Producer " + producer + " finished");
 				producerFinished(this);
 			}
 		}
 	}
 }
