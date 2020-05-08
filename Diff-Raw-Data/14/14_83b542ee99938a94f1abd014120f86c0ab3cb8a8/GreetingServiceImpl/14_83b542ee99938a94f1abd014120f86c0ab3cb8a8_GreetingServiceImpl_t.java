 /**
  * Copyright (c) 2012 AGETO Service GmbH and others.
  * All rights reserved.
  *
  * This program and the accompanying materials are made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Gunnar Wagenknecht - initial API and implementation
  */
 package hello.service;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.time.DateFormatUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Implementation of {@link GreetingService} that uses an in-memory map.
  */
 public class GreetingServiceImpl implements GreetingService {
 
 	public static class Greeting {
 
 		private final long created;
 		private final String text;
 		private final String submittedBy;
 		private long processedOn;
 		private String processedBy;
 
 		public Greeting(final long created, final String text, final String submittedBy) {
 			this.created = created;
 			this.text = text;
 			this.submittedBy = submittedBy;
 		}
 
 		/**
 		 * Returns the created.
 		 * 
 		 * @return the created
 		 */
 		public long getCreated() {
 			return created;
 		}
 
 		/**
 		 * Returns the processedBy.
 		 * 
 		 * @return the processedBy
 		 */
 		public String getProcessedBy() {
 			return processedBy;
 		}
 
 		/**
 		 * Returns the processedOn.
 		 * 
 		 * @return the processedOn
 		 */
 		public long getProcessedOn() {
 			return processedOn;
 		}
 
 		/**
 		 * Returns the submittedBy.
 		 * 
 		 * @return the submittedBy
 		 */
 		public String getSubmittedBy() {
 			return submittedBy;
 		}
 
 		/**
 		 * Returns the text.
 		 * 
 		 * @return the text
 		 */
 		public String getText() {
 			return text;
 		}
 
 		public void process(final long processedOn, final String processedBy) {
 			this.processedOn = processedOn;
 			this.processedBy = processedBy;
 		}
 
 		@Override
 		public String toString() {
 			final StringBuilder greeting = new StringBuilder();
 			greeting.append(StringUtils.trimToEmpty(text));
 			greeting.append(" (");
 			greeting.append("submitted by ").append(submittedBy);
 			if (processedOn > 0) {
 				greeting.append(", processed on ").append(DateFormatUtils.SMTP_DATETIME_FORMAT.format(processedOn));
 				greeting.append(", by ").append(processedBy);
 			}
 			greeting.append(")");
 
 			return greeting.toString();
 		}
 
 	}
 
 	private static final Logger LOG = LoggerFactory.getLogger(GreetingServiceImpl.class);
 	private static final int SIZE_LIMIT = 200;
 
 	private final String myNodeId;
 	private final List<Greeting> unprocessed = new CopyOnWriteArrayList<Greeting>();
 	private final List<Greeting> processed = new CopyOnWriteArrayList<Greeting>();
 
 	/**
 	 * Creates a new instance.
 	 * 
 	 * @param myNodeId
 	 */
 	public GreetingServiceImpl(final String myNodeId) {
 		this.myNodeId = StringUtils.trimToEmpty(myNodeId);
 	}
 
 	private void add(final Greeting greeting, final List<Greeting> list) {
 		if (list.size() >= SIZE_LIMIT) {
 			try {
 				list.remove(0);
 			} catch (final IndexOutOfBoundsException e) {
 				// ignore concurrent removal
 			}
 		}
 
 		list.add(greeting);
 	}
 
 	protected void addProcessed(final Greeting greeting) {
 		add(greeting, processed);
 	}
 
 	protected void addUnprocessed(final Greeting greeting) {
 		add(greeting, unprocessed);
 	}
 
 	@Override
 	public Collection<String> getGreetings() throws Exception {
 		// load all (sorted based on creation time)
 		final List<Greeting> allGreetings = loadAll();
 
 		// generate greetings (but start with latest)
 		final List<String> greetings = new ArrayList<String>(allGreetings.size());
 		for (int i = allGreetings.size() - 1; i >= 0; i--) {
 			greetings.add(allGreetings.get(i).toString());
 		}
 		return greetings;
 	}
 
 	protected List<Greeting> loadAll() {
 		final List<Greeting> allGreetings = new ArrayList<Greeting>();
 		allGreetings.addAll(unprocessed);
 		allGreetings.addAll(processed);
 		Collections.sort(allGreetings, new Comparator<Greeting>() {
 			@Override
 			public int compare(final Greeting o1, final Greeting o2) {
 				return new Date(o1.created).compareTo(new Date(o2.created));
 			}
 		});
 		return allGreetings;
 	}
 
 	protected Greeting nextUnprocessed() {
		try {
			return unprocessed.remove(0);
		} catch (final IndexOutOfBoundsException e) {
			// empty
			return null;
 		}
 	}
 
 	@Override
 	public void processGreetings() throws Exception {
 		if (LOG.isTraceEnabled()) {
 			LOG.trace("Processing greetings...");
 		}
 
 		// each call processed at most 3 greetings
 		// (to have a better demo)
 		for (int i = 0; i < 3; i++) {
 			final Greeting greeting = nextUnprocessed();
 			if (null == greeting) {
 				break;
 			}
 			LOG.info("Processing greeting: {}", greeting);
 			greeting.process(System.currentTimeMillis(), myNodeId);
 			addProcessed(greeting);
 		}
 
 		if (LOG.isTraceEnabled()) {
 			LOG.trace("Finished processing greetings.");
 		}
 	}
 
 	@Override
 	public void sayHello(final String greeting) throws Exception {
 		if (null == greeting) {
 			throw new IllegalArgumentException("greeting must not be null");
 		}
 
 		addUnprocessed(new Greeting(System.currentTimeMillis(), greeting, myNodeId));
 	}
 }
