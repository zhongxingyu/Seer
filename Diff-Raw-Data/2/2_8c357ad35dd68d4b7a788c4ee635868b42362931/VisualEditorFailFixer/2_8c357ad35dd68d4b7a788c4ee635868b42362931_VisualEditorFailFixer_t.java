 package com.wikia.runescape;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.lang.ref.WeakReference;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.WeakHashMap;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicLong;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.mediawiki.MediaWiki;
 import org.mediawiki.MediaWiki.MediaWikiException;
 
 /*-
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 public class VisualEditorFailFixer {
 	private static final Logger log = Logger.getLogger("com.wikia.runescape");
 
 	static {
 		log.setLevel(Level.INFO);
 	}
 
 	/**
 	 * @param args
 	 *            unused
 	 */
 	public static void main(String[] args) {
 		// Read the bot's configuration file.
 		final Settings settings = new Settings(new File(System.getProperty("user.home"), ".rtefixer.conf"));
 		// Require some things out of it from the start...
 		{
 			boolean fatalError = false;
 			if (settings.getProperty("Wiki") == null) {
 				log.log(Level.SEVERE, "$HOME/.rtefixer.conf does not contain a value for Wiki, the wiki to work on");
 				fatalError = true;
 			}
 			if (settings.getProperty("LoginName") == null) {
 				log.log(Level.SEVERE, "$HOME/.rtefixer.conf does not contain a value for LoginName, the username of the bot account on the wiki");
 				fatalError = true;
 			}
 			if (settings.getProperty("LoginPassword") == null) {
 				log.log(Level.SEVERE, "$HOME/.rtefixer.conf does not contain a value for LoginPassword, the password of the bot account on the wiki");
 				fatalError = true;
 			}
 			if (fatalError) {
 				System.exit(1);
 				return;
 			}
 		}
 
 		// Which wiki are we working on?
 		final MediaWiki wiki = new MediaWiki(settings.getProperty("Wiki"), settings.getProperty("ScriptPath", "")).setUsingCompression(true);
 
 		Map<String, WeakReference<ScheduledFuture<?>>> pendingPageEdits = new WeakHashMap<String, WeakReference<ScheduledFuture<?>>>();
 
 		EditDelayCalculator editDelayer = new EditDelayCalculator();
 
 		ScheduledExecutorService editorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
 			private final AtomicLong sequenceNumber = new AtomicLong();
 
 			public Thread newThread(Runnable r) {
 				return new Thread(r, "Editor thread " + sequenceNumber.incrementAndGet());
 			}
 		});
 
 		Thread recentChangesMonitorThread = new Thread(new RecentChangesMonitor(wiki, settings, pendingPageEdits, editorService, editDelayer), "Recent changes monitor thread");
 		recentChangesMonitorThread.start();
 		log.log(Level.INFO, "Recent changes monitor thread started");
 
 		BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
 		String line;
 
 		log.log(Level.INFO, "You may enter page names in the console and they will be edited immediately");
 
 		try {
 			while ((line = keyboard.readLine()) != null) {
 				while (true) /*- last revision retry loop */{
 					try {
 						Iterator<MediaWiki.Revision> ri = wiki.getLastRevision(false /*- get content immediately */, line);
 						if (ri.hasNext()) {
 							MediaWiki.Revision r = ri.next();
 							if (r != null) {
 								if (!r.isContentHidden()) {
 									editorService.execute(new Edit(wiki, settings, line, wiki.getNamespaces().removeNamespacePrefix(line), wiki.getNamespaces().getNamespaceForPage(line), r.getRevisionID()));
 								} else {
 									log.log(Level.WARNING, "{0} r{1}'s content is unexpectedly hidden", new Object[] { line, r.getRevisionID() });
 								}
 							} else {
 								log.log(Level.WARNING, "{0} is a missing page", line);
 							}
 						}
 						break;
 					} catch (MediaWiki.IterationException ie) {
 						log.log(Level.SEVERE, "Network error occurred while getting the latest revision's number for the entered page; retrying shortly", ie.getCause());
 						shortDelay();
 					} catch (IOException e) {
 						log.log(Level.SEVERE, "Network error occurred while getting the latest revision's number for the entered page; retrying shortly", e);
 						shortDelay();
 					} catch (MediaWikiException e) {
 						log.log(Level.SEVERE, "Network error occurred while getting the latest revision's number for the entered page; retrying shortly", e);
 						shortDelay();
 					}
 				}
 			}
 		} catch (IOException e) {
 			log.log(Level.WARNING, "Console page input interrupted by an exception", e);
 		}
 	}
 
 	/**
 	 * Settings are Properties that automatically load and store themselves into
 	 * files. Reads and writes ignore <tt>IOException</tt>s; the errors are
 	 * logged to Java Logging instead.
 	 */
 	private static class Settings extends Properties {
 		private static final long serialVersionUID = 1L;
 
 		private final File file;
 
 		public Settings(final File file) {
 			this.file = file;
 			try {
 				final InputStream in = new FileInputStream(file);
 				try {
 					load(in);
 				} finally {
 					in.close();
 				}
 			} catch (final IOException e) {
 				log.log(Level.WARNING, "Settings file cannot be read; using no settings at all", e);
 			}
 		}
 
 		public void store() throws IOException {
 			final OutputStream out = new FileOutputStream(file);
 			try {
 				store(out, null);
 			} finally {
 				out.close();
 			}
 		}
 	}
 
 	private static void shortDelay() {
 		try {
 			Thread.sleep(45000);
 		} catch (final InterruptedException e) {
 			// don't care
 		}
 	}
 
 	public static class EditDelayCalculator {
 		/**
 		 * The minimum edit delay, in milliseconds. If the average reversion
 		 * time would be lower than this, the edit delay is set to this.
 		 * <p>
 		 * This is so that we don't end up editing too fast.
 		 */
 		private static long MIN_DELAY_MILLIS = 4L * 60 * 1000;
 
 		/**
 		 * The maximum edit delay, in milliseconds. If the average reversion
 		 * time would be higher than this, the edit delay is set to this.
 		 * Additionally, the edit delay is initially this, because there have
 		 * been no reversions to prime the delay.
 		 * <p>
 		 * This is so that we don't end up indefinitely delaying edits.
 		 */
 		private static long MAX_DELAY_MILLIS = 15L * 60 * 1000;
 
 		/**
 		 * The number of milliseconds during to consider a reversion as recent.
 		 * Older reversions are discarded.
 		 * <p>
 		 * This is to adjust to periods of high vandalism and to periods of low
 		 * counter-vandalism activity, owing to timezones and the like.
 		 */
 		private static long RECENT_REVERSION_MILLIS = 60L * 60 * 1000;
 
 		/**
 		 * Contains the current edit delay, in milliseconds, as calculated from
 		 * the average reversion delay in recent reversions.
 		 * <p>
 		 * Starts out with the maximum delay. As there have been no reversions,
 		 * we don't know how swift they are.
 		 */
 		private long currentDelayMillis = MAX_DELAY_MILLIS;
 
 		private boolean currentDelayCached = true;
 
 		/**
 		 * A record of the timestamp at which the last N recent reversions have
 		 * been made. Adding or removing an item in this list requires adding a
 		 * corresponding entry in <code>recentReversionDelaysMillis</code>.
 		 */
 		private final LinkedList<Date> recentReversionTimestamps = new LinkedList<Date>();
 
 		/**
 		 * A record of the number of milliseconds it took the N recent
 		 * reversions to be made, after the edit being reverted. Adding or
 		 * removing an item in this list requires adding a corresponding entry
 		 * in <code>recentReversionTimestamps</code>.
 		 */
 		private final LinkedList<Long> recentReversionDelaysMillis = new LinkedList<Long>();
 
 		/**
 		 * Adds a reversion to the queue of reversions used by this
 		 * <tt>EditDelayCalculator</tt> to determine how long the editor thread
 		 * should delay edits.
 		 * 
 		 * @param timestamp
 		 *            The timestamp at which the reversion took place.
 		 * @param delayMillis
 		 *            The delay between the edit that was reverted and the edit
 		 *            that reverted it.
 		 */
 		public synchronized void addReversion(final Date timestamp, final long delayMillis) {
 			if (timestamp == null)
 				throw new NullPointerException("timestamp");
 			if (delayMillis < 0)
 				throw new IllegalArgumentException("delayMillis " + delayMillis + " < 0");
 			if (delayMillis > RECENT_REVERSION_MILLIS)
 				// doesn't even count as it took too long
 				return;
 
 			recentReversionTimestamps.add(timestamp);
 			try {
 				recentReversionDelaysMillis.add(delayMillis);
 
 				currentDelayCached = false;
 			} catch (OutOfMemoryError oome) {
 				recentReversionTimestamps.removeLast();
 				throw oome;
 			}
 
 			removeExpiredReversions();
 		}
 
 		public synchronized long getEditDelayMillis() {
 			removeExpiredReversions();
 
 			if (currentDelayCached)
 				return currentDelayMillis;
 			else {
 				long result = MAX_DELAY_MILLIS;
 
 				if (!recentReversionDelaysMillis.isEmpty()) {
 					long reversionDelayTotal = 0L;
 					int n = 0;
 
 					for (Long l : recentReversionDelaysMillis) {
 						reversionDelayTotal += l;
 						if (reversionDelayTotal < 0)
 							reversionDelayTotal = Long.MAX_VALUE;
 						n++;
 						if (n < 0)
 							n = Integer.MAX_VALUE;
 					}
 
 					result = Math.min(Math.max(reversionDelayTotal / n, MIN_DELAY_MILLIS), MAX_DELAY_MILLIS);
 				}
 
 				currentDelayMillis = result;
 				currentDelayCached = true;
 
 				return result;
 			}
 		}
 
 		protected synchronized void removeExpiredReversions() {
 			Date now = new Date();
 
 			while (!recentReversionTimestamps.isEmpty() && recentReversionTimestamps.getFirst().getTime() + RECENT_REVERSION_MILLIS < now.getTime()) {
 				recentReversionTimestamps.removeFirst();
 				currentDelayCached = false;
 			}
 		}
 	}
 
 	public static class RecentChangesMonitor implements Runnable {
 		private static final long RC_CHECK_MILLIS = 1L * 60 * 1000;
 
 		private final MediaWiki wiki;
 
 		private final Settings settings;
 
 		private final Map<String, WeakReference<ScheduledFuture<?>>> pendingPageEdits;
 
 		private final ScheduledExecutorService editorService;
 
 		private final EditDelayCalculator editDelayer;
 
 		/**
 		 * Matches the edit summary used by the rollback tool. Edit to match the
 		 * rollback message on your wiki language, if needed.
 		 * <p>
 		 * Match groups are as follows:
 		 * <ul>
 		 * <li><tt>\1</tt> refers to the user whose edit was reverted.
 		 * <li><tt>\2</tt> refers to the last user whose edit survived the
 		 * rollback.
 		 * </ul>
 		 */
 		private static final Pattern rollbackMatcher = Pattern.compile("^Reverted edits by \\[\\[Special:Contributions/([^]|]+)\\|\\1\\]\\] \\(\\[\\[User[ _]talk:\\1\\|Talk\\]\\]\\) to last version by \\[\\[Special:Contributions/([^]|]+)\\|\\2\\]\\]$");
 
 		/**
 		 * Matches the edit summary used by the undo tool. Edit to match the
 		 * undo message on your wiki language, if needed.
 		 * <p>
 		 * Match groups are as follows:
 		 * <ul>
 		 * <li><tt>\1</tt> refers to the ID of the revision undone.
 		 * <li><tt>\2</tt> refers to the user whose edit was undone.
 		 * </ul>
 		 */
 		private static final Pattern undoMatcher = Pattern.compile("\\bUndid revision ([0-9]+) by \\[\\[Special:Contributions/([^]|]+)\\|\\2\\]\\] \\(\\[\\[User[ _]talk:\\2\\|Talk\\]\\]\\)(?:$|\\b|\\s)");
 
 		public RecentChangesMonitor(final MediaWiki wiki, final Settings settings, final Map<String, WeakReference<ScheduledFuture<?>>> pendingPageEdits, final ScheduledExecutorService editorService, final EditDelayCalculator editDelayer) {
 			this.wiki = wiki;
 			this.settings = settings;
 			this.pendingPageEdits = pendingPageEdits;
 			this.editorService = editorService;
 			this.editDelayer = editDelayer;
 		}
 
 		public void run() {
 			/*
 			 * Start getting RecentChanges from now.
 			 */
 			Date earliest = new Date();
 			long lastRcidSeen = -1L;
 			while (true) /*- re-get RecentChanges loop */{
 				Iterator<MediaWiki.RecentChange> rci = wiki.recentChanges(earliest, null /*- no latest */, true /*- always chronological */, 10 /*- changes to stream at once */, null /*- show user: all */, settings.getProperty("LoginName") /*- hide user: self */, true /*- show edits modifying pages */, true /*- show edits creating pages */, false /*- don't show log entries */, null /*- minor: don't care */, false /*- bot: only non-bots */, null /*- anon: don't care */,
 						false /*- redirects: only non-redirects */, null /*- patrolled: don't filter */, false /*- getPatrolInformation */, MediaWiki.StandardNamespace.MAIN, 120L /*- custom Beta namespace */);
 
 				MediaWiki.RecentChange rc;
 
 				while (true) /*- re-get current iteration of RecentChanges loop */{
 					try {
 						while (rci.hasNext()) {
 							rc = rci.next();
 
 							// Update the time, advancing in the RecentChanges
 							// stream.
 							if (rc.getRcid() > lastRcidSeen) {
 								processRecentChange(rc);
 								if (rc.getTimestamp().compareTo(earliest) > 0)
 									earliest = rc.getTimestamp();
 								lastRcidSeen = rc.getRcid();
 							}
 						}
 
 						break; // on success
 					} catch (MediaWiki.IterationException ie) {
 						log.log(Level.WARNING, "Error while iterating over RecentChanges; retrying shortly", ie.getCause());
 						shortDelay();
 					}
 				}
 
 				try {
 					Thread.sleep(RC_CHECK_MILLIS);
 				} catch (InterruptedException e) {
 					log.log(Level.WARNING, "Thread {0} interrupted", Thread.currentThread().getName());
 					return;
 				}
 			}
 		}
 
 		protected void processRecentChange(MediaWiki.RecentChange rc) {
 			if (rc.getChangeType().equals(MediaWiki.RecentChangeType.EDIT) && rc.getComment() != null) {
 				long parentRevID = 0L;
 				String undoneUser = null;
 				Matcher m;
 				if ((m = rollbackMatcher.matcher(rc.getComment())).find()) {
 					// reverted edits by some-user... Well, the revision that
 					// was undone is the parent revid, and check that the user
 					// is correct!
 					parentRevID = rc.getOldRevisionID();
 					undoneUser = m.group(1);
 				} else if ((m = undoMatcher.matcher(rc.getComment())).find()) {
 					// undid revision X by some-user... We need to check that
 					// the user is correct!
 					parentRevID = Long.parseLong(m.group(1));
 					undoneUser = m.group(2);
 				}
 
 				if (parentRevID != 0L && undoneUser != null) {
 					Iterator<MediaWiki.Revision> ri = wiki.getRevisions(rc.getOldRevisionID());
 					while (true) /*- retry get previous revision loop */{
 						try {
 							if (ri.hasNext()) {
 								MediaWiki.Revision r = ri.next();
 
 								if (r != null && !r.isUserNameHidden() && r.getUserName().equals(undoneUser)) {
 									/*
 									 * The revision was probably indeed undone
 									 * from this user. Add a reversion, done as
 									 * of the recent change, delayed by the time
 									 * between the undone revision and the
 									 * recent change.
 									 */
 									long reversionMillis = rc.getTimestamp().getTime() - r.getTimestamp().getTime();
 									editDelayer.addReversion(rc.getTimestamp(), reversionMillis);
 									log.log(Level.INFO, "{0}: {1}''s r{2} reverts {3}''s r{4} after {5} seconds", new Object[] { rc.getFullPageName(), rc.getUserName(), rc.getNewRevisionID(), undoneUser, parentRevID, reversionMillis / 1000 });
 								}
 							}
 
 							break; // on success
 						} catch (MediaWiki.IterationException ie) {
 							log.log(Level.WARNING, "Error while getting information about revision " + parentRevID + ", reportedly created by " + undoneUser + "; retrying shortly", ie.getCause());
 							shortDelay();
 						}
 					}
 				}
 			}
 
 			/*
 			 * Regardless of whether this was a reversion, queue a check and/or
 			 * edit for Visual Editor failures. This check will run after a
 			 * delay controlled by how swift vandalism reversions were recently.
 			 */
 			// 1. Is there already a check queued for this very page?
 			if (pendingPageEdits.containsKey(rc.getFullPageName())) {
 				WeakReference<ScheduledFuture<?>> pageEditTaskRef = pendingPageEdits.get(rc.getFullPageName());
 				if (pageEditTaskRef != null) {
 					ScheduledFuture<?> pageEditTask = pageEditTaskRef.get();
 					if (pageEditTask != null) {
 						// Yes. Remove it.
 						pageEditTask.cancel(false);
 						pendingPageEdits.remove(rc.getFullPageName());
 					}
 				}
 			}
 
 			// 2. Queue a check for this page.
 			pendingPageEdits.put(rc.getFullPageName(), new WeakReference<ScheduledFuture<?>>(editorService.schedule(new Edit(wiki, settings, rc.getFullPageName(), rc.getBasePageName(), rc.getNamespace(), rc.getNewRevisionID()), editDelayer.getEditDelayMillis(), TimeUnit.MILLISECONDS)));
 			log.log(Level.INFO, "Queued an edit for {0} r{1}", new Object[] { rc.getFullPageName(), rc.getNewRevisionID() });
 		}
 	}
 
 	public static class Edit implements Runnable {
 		private final MediaWiki wiki;
 
 		private final Settings settings;
 
 		private final String fullPageName, basePageName;
 
 		private final MediaWiki.Namespace namespace;
 
 		/**
 		 * Matches <tt>{C</tt>, <tt>{C}</tt>, <tt>C}</tt> not preceded or
 		 * followed by <tt>{</tt> and <tt>}</tt>.
 		 */
 		private static final Pattern CInBracesRemover = Pattern.compile("((?<!\\{)\\{C\\}?|C\\}(?!\\}))");
 
 		/**
 		 * Matches odd <tt>&lt;span&gt;</tt> tags, capturing the contents as
 		 * <tt>\1</tt>.
 		 */
		private static final Pattern oddSpanRemover = Pattern.compile("<span style=\"(?:color:rgb\\(0,0,0\\);|line-height:[0-9]+px;|color:rgb\\(0,0,0\\);line-height:[0-9]+px;|color:rgb\\(0,0,0\\);font-family:monospace;)\">([^<>]*)</span>");
 
 		/**
 		 * Matches links marked up as external links to the RuneScape Wiki,
 		 * preceded by one <tt>[</tt> and followed by one <tt>]</tt>.
 		 * <p>
 		 * Match groups are as follows:
 		 * <ul>
 		 * <li><tt>\1</tt> refers to the URL part after <tt>/wiki/</tt>.
 		 * <li><tt>\2</tt> refers to the text for the link.
 		 * </ul>
 		 */
 		private static final Pattern externalLinkToWikiMatcher = Pattern.compile("(?<!\\[)\\[http://runescape\\.wikia\\.com/wiki/([^ ?&]+) ([^]]+)\\](?!\\])");
 
 		/**
 		 * Matches category specifications at the end of a page.
 		 */
 		private static final Pattern categoriesAtEndMatcher = Pattern.compile("(?:\\[\\[Category:[^]]+\\]\\]\n*)+$");
 
 		/**
 		 * Matches strings of links pointing to the same wiki page, in the main
 		 * namespace only. This is because multiple links to the same file
 		 * (File:X) are actually warranted.
 		 * <p>
 		 * Match groups are as follows:
 		 * <ul>
 		 * <li><tt>\1</tt> refers to the links' target.
 		 * </ul>
 		 */
 		private static final Pattern linksToSamePageMatcher = Pattern.compile("\\[\\[([^]|:]+)\\|[^]|]+\\]\\](?:\\[\\[\\1\\|[^]|]+\\]\\])+");
 
 		/**
 		 * Matches a link in a string of links pointing to the same wiki page.
 		 * Extracts the link's text.
 		 * <p>
 		 * Match groups are as follows:
 		 * <ul>
 		 * <li><tt>\1</tt> refers to the link's text.
 		 * </ul>
 		 */
 		private static final Pattern linkTextMatcher = Pattern.compile("\\[\\[[^]|:]+\\|([^]|]+)\\]\\]");
 
 		/**
 		 * Matches a regular wikilink whose text ends with a space.
 		 * <p>
 		 * Match groups are as follows:
 		 * <ul>
 		 * <li><tt>\1</tt> refers to the link's target.
 		 * <li><tt>\2</tt> refers to the link's text.
 		 * </ul>
 		 */
 		private static final Pattern linkTrailingSpaceMatcher = Pattern.compile("\\[\\[([^]|:]+)\\|([^]|]+?) \\]\\]");
 
 		/**
 		 * Matches a single category specification.
 		 * <p>
 		 * Match groups are as follows:
 		 * <ul>
 		 * <li><tt>\1</tt> refers to the category's base name.
 		 * </ul>
 		 */
 		private static final Pattern categoryMatcher = Pattern.compile("\\[\\[Category:([^]]+)\\]\\]\n*");
 
 		/**
 		 * The revision ID expected to be the last one for the page. If new
 		 * revisions have been made, we need to abort immediately. The recent
 		 * changes monitor will get the new revision later and queue the page
 		 * anew for editing after a suitable delay.
 		 */
 		private final long expectedRevisionID;
 
 		/**
 		 * <tt>Edit</tt> instances synchronise on this object to preclude other
 		 * <tt>Edit</tt> instances from logging in between the anon check and
 		 * their login attempts.
 		 */
 		private static final Object loginSync = new Object();
 
 		public Edit(final MediaWiki wiki, final Settings settings, final String fullPageName, final String basePageName, final MediaWiki.Namespace namespace, final long expectedRevisionID) {
 			this.wiki = wiki;
 			this.settings = settings;
 			this.fullPageName = fullPageName;
 			this.basePageName = basePageName;
 			this.namespace = namespace;
 			this.expectedRevisionID = expectedRevisionID;
 		}
 
 		public void run() {
 			log.log(Level.INFO, "Processing {0} r{1}", new Object[] { fullPageName, expectedRevisionID });
 			// Multiple Visual Editor failures can be checked for, then fixed.
 			// You may enable these checks in certain namespaces or for certain
 			// pages by wrapping them in the correct 'if' statement.
 
 			// For lesser network/wiki server utilisation, please 'return' right
 			// away if no checks would succeed for the page that was queued.
 			if (!(namespace.getID() == MediaWiki.StandardNamespace.MAIN || namespace.getID() == 120 /*- custom Beta namespace */)) {
 				log.log(Level.INFO, "{0} is not subject to Visual Editor checks", fullPageName);
 				return;
 			}
 
 			// First, check whether we are logged in.
 			boolean isAnon = true;
 
 			synchronized (loginSync) {
 				while (true) {
 					// logged-in check retry loop
 					try {
 						MediaWiki.CurrentUser currentUser = wiki.getCurrentUser();
 						isAnon = currentUser.isAnonymous();
 						break;
 					} catch (IOException e) {
 						log.log(Level.SEVERE, "Network error occurred during a user login check; retrying shortly", e);
 						shortDelay();
 					} catch (MediaWiki.MediaWikiException e) {
 						log.log(Level.SEVERE, "Network error occurred during a user login check; retrying shortly", e);
 						shortDelay();
 					}
 				}
 
 				// If we are not,
 				if (isAnon) {
 					// log in.
 					log.log(Level.INFO, "Logging in");
 					while (true) /*- login retry loop */{
 						try {
 							wiki.logIn(settings.getProperty("LoginName"), settings.getProperty("LoginPassword").toCharArray());
 							log.log(Level.INFO, "Successfully logged in as {0}", settings.getProperty("LoginName"));
 							break;
 						} catch (final MediaWiki.LoginFailureException e) {
 							log.log(Level.SEVERE, "Login failed; please check LoginName and LoginPassword in $HOME/.rtefixer.conf", e);
 							System.exit(1);
 							return;
 						} catch (final MediaWiki.LoginDelayException t) {
 							log.log(Level.INFO, "Login throttled; retrying in {0} seconds", t.getWaitTime());
 							try {
 								Thread.sleep((long) t.getWaitTime() * 1000);
 							} catch (InterruptedException e) {
 								// don't care
 							}
 						} catch (final MediaWiki.BlockException b) {
 							log.log(Level.SEVERE, "User blocked; please check its block log", b);
 							System.exit(1);
 							return;
 						} catch (IOException e) {
 							log.log(Level.WARNING, "Network error occurred while logging in; retrying shortly", e);
 							shortDelay();
 						} catch (MediaWiki.MediaWikiException e) {
 							log.log(Level.WARNING, "Network error occurred while logging in; retrying shortly", e);
 							shortDelay();
 						}
 					}
 				}
 			}
 
 			// Get an edit token and the page's content.
 			MediaWiki.EditToken editToken;
 
 			while (true) /*- edit token retry loop */{
 				try {
 					editToken = wiki.startEdit(fullPageName);
 					break;
 				} catch (IOException e) {
 					log.log(Level.WARNING, "Network error occurred while gathering an edit token; retrying shortly", e);
 					shortDelay();
 				} catch (MediaWiki.BlockException e) {
 					log.log(Level.SEVERE, "User blocked; please check its block log", e);
 					System.exit(1);
 					return;
 				} catch (MediaWiki.MediaWikiException e) {
 					log.log(Level.WARNING, "Network error occurred while gathering an edit token; retrying shortly", e);
 					shortDelay();
 				}
 			}
 
 			String oldContent = null;
 
 			while (true) /*- content retry loop */{
 				try {
 					Iterator<MediaWiki.Revision> ri = wiki.getLastRevision(true /*- get content immediately */, fullPageName);
 					if (ri.hasNext()) {
 						MediaWiki.Revision r = ri.next();
 						if (r != null) {
 							if (r.getRevisionID() == expectedRevisionID) {
 								if (!r.isContentHidden()) {
 									oldContent = r.getContent();
 								} else {
 									log.log(Level.WARNING, "{0} r{1}'s content is unexpectedly hidden", new Object[] { fullPageName, expectedRevisionID });
 									return;
 								}
 							} else {
 								log.log(Level.INFO, "{0} was edited after r{1}", new Object[] { fullPageName, expectedRevisionID });
 								return;
 							}
 						} else {
 							log.log(Level.WARNING, "{0} has become missing after r{1}", new Object[] { fullPageName, expectedRevisionID });
 							return;
 						}
 					}
 					break;
 				} catch (MediaWiki.IterationException ie) {
 					log.log(Level.WARNING, "Network error occurred while getting revision content; retrying shortly", ie.getCause());
 					shortDelay();
 				} catch (IOException e) {
 					log.log(Level.WARNING, "Network error occurred while getting revision content; retrying shortly", e);
 					shortDelay();
 				} catch (MediaWikiException e) {
 					log.log(Level.WARNING, "Network error occurred while getting revision content; retrying shortly", e);
 					shortDelay();
 				}
 			}
 
 			if (oldContent == null) {
 				log.log(Level.WARNING, "{0} r{1} has no content", new Object[] { fullPageName, expectedRevisionID });
 				return;
 			}
 
 			String newContent = oldContent;
 
 			Map<String, Integer> matchCount = new TreeMap<String, Integer>();
 
 			Matcher m;
 			if (namespace.getID() == MediaWiki.StandardNamespace.MAIN || namespace.getID() == 120L /*- custom Beta namespace */) {
 				// CHECK 1. Odd <span>s. Activated in content namespaces only.
 				// Passing this check replaces the <span> tag with its contents.
 				// This check SHOULD be before the others, because it removes
 				// tags.
 				if ((m = oddSpanRemover.matcher(newContent)).find()) {
 					StringBuffer sb = new StringBuffer(newContent.length() - 20);
 					int replacements = 0;
 					do {
 						if (isTagListOK(getActiveTags(newContent, m.start()))) {
 							m.appendReplacement(sb, "$1");
 							replacements++;
 						}
 					} while (m.find());
 					m.appendTail(sb);
 					if (replacements > 0) {
 						matchCount.put("superfluous span tag", replacements);
 						newContent = sb.toString();
 					}
 				}
 
 				// CHECK 2. {C, {C}, C}. Activated in content namespaces only.
 				// Passing this check removes the offending characters.
 				if ((m = CInBracesRemover.matcher(newContent)).find()) {
 					StringBuffer sb = new StringBuffer(newContent.length() - 2);
 					int replacements = 0;
 					do {
 						if (isTagListOK(getActiveTags(newContent, m.start()))) {
 							m.appendReplacement(sb, "");
 							replacements++;
 						}
 					} while (m.find());
 					m.appendTail(sb);
 					if (replacements > 0) {
 						matchCount.put("C in curly brackets", replacements);
 						newContent = sb.toString();
 					}
 				}
 
 				// CHECK 3. External links pointing inside the wiki. Activated
 				// in content namespaces only.
 				// Passing this check replaces the external link with a regular
 				// wikilink, cleaning the link name as needed.
 				// Links containing unescaped ? or & in the link target are not
 				// matched, but %3F is decoded to ? for the purpose of short
 				// wikilinks, and so on.
 				if ((m = externalLinkToWikiMatcher.matcher(newContent)).find()) {
 					StringBuffer sb = new StringBuffer(newContent.length() - 30);
 					int replacements = 0;
 					do {
 						if (isTagListOK(getActiveTags(newContent, m.start()))) {
 							// [[Article%27s_name_here
 							String linkTarget, linkText;
 							try {
 								linkTarget = URLDecoder.decode(m.group(1), "UTF-8");
 							} catch (UnsupportedEncodingException e) {
 								log.log(Level.SEVERE, "UTF-8 is not supported by this Java VM");
 								System.exit(1);
 								return;
 							} catch (IllegalArgumentException iae) {
 								log.log(Level.WARNING, "Invalid link target in {0} r{1}: {2}", new Object[] { fullPageName, expectedRevisionID, m.group(1) });
 								continue;
 							}
 							// |Link&#39;s text here]]
 							linkText = m.group(2);
 
 							String wikilink = createWikilink(linkTarget, linkText);
 
 							m.appendReplacement(sb, Matcher.quoteReplacement(wikilink));
 							replacements++;
 						}
 					} while (m.find());
 					m.appendTail(sb);
 					if (replacements > 0) {
 						matchCount.put("full URL to article", replacements);
 						newContent = sb.toString();
 					}
 				}
 
 				// CHECK 4. Category deduplication. Activated in the main
 				// namespace only.
 				// Passing this check removes categories that are found twice at
 				// the end of an article.
 				if ((m = categoriesAtEndMatcher.matcher(newContent)).find()) {
 					StringBuffer sb = new StringBuffer(newContent.length());
 					sb.append(newContent, 0, m.start());
 					// Add deduplicated categories later.
 					int oldCategoryCount = 0, newCategoryCount = 0;
 
 					StringBuffer dedupedCategories = new StringBuffer();
 					Set<String> seenCategories = new TreeSet<String>();
 
 					Matcher cm = categoryMatcher.matcher(m.group());
 					while (cm.find()) {
 						oldCategoryCount++;
 						if (seenCategories.contains(cm.group(1))) {
 							cm.appendReplacement(dedupedCategories, "");
 						} else {
 							newCategoryCount++;
 							seenCategories.add(cm.group(1));
 						}
 					}
 					cm.appendTail(dedupedCategories);
 					sb.append(dedupedCategories);
 
 					if (oldCategoryCount - newCategoryCount > 0) {
 						matchCount.put("duplicate category", oldCategoryCount - newCategoryCount);
 						newContent = sb.toString();
 					}
 				}
 
 				// CHECK 5. [[X|Cont]][[X|igu]][[X|ous]] links. Activated in
 				// content namespaces only.
 				// Passing this check coalesces the links into one.
 				if ((m = linksToSamePageMatcher.matcher(newContent)).find()) {
 					StringBuffer sb = new StringBuffer(newContent.length());
 					int replacements = 0;
 					do {
 						if (isTagListOK(getActiveTags(newContent, m.start()))) {
 							String linkTarget = m.group(1);
 							StringBuilder linkText = new StringBuilder();
 							Matcher lm = linkTextMatcher.matcher(m.group());
 							while (lm.find()) {
 								linkText.append(lm.group(1));
 							}
 
 							String wikilink = createWikilink(linkTarget, linkText.toString());
 
 							m.appendReplacement(sb, Matcher.quoteReplacement(wikilink));
 							replacements++;
 						}
 					} while (m.find());
 					m.appendTail(sb);
 					if (replacements > 0) {
 						matchCount.put("contiguous links to the same page", replacements);
 						newContent = sb.toString();
 					}
 				}
 
 				// CHECK 6. [[X|X ]] links. Activated in content namespaces
 				// only.
 				// Passing this check moves the space out of the link, and
 				// possibly shrinks the link.
 				if ((m = linkTrailingSpaceMatcher.matcher(newContent)).find()) {
 					StringBuffer sb = new StringBuffer(newContent.length());
 					int replacements = 0;
 					do {
 						if (isTagListOK(getActiveTags(newContent, m.start()))) {
 							String linkTarget = m.group(1), linkText = m.group(2);
 
 							String wikilink = createWikilink(linkTarget, linkText);
 
 							m.appendReplacement(sb, Matcher.quoteReplacement(wikilink) + " ");
 							replacements++;
 						}
 					} while (m.find());
 					m.appendTail(sb);
 					if (replacements > 0) {
 						matchCount.put("link ending with a space", replacements);
 						newContent = sb.toString();
 					}
 				}
 			}
 
 			if (!matchCount.isEmpty()) {
 				StringBuilder fixed = new StringBuilder();
 				for (Map.Entry<String, Integer> entry : matchCount.entrySet()) {
 					if (fixed.length() > 0)
 						fixed.append("; ");
 					fixed.append(entry.getKey()).append(": ").append(entry.getValue());
 				}
 
 				while (true) /*- edit retry loop */{
 					try {
 						wiki.replacePage(editToken, newContent, "Visual Editor glitches fixed: " + fixed, true /*- bot */, true /*- minor */);
 						log.log(Level.INFO, "{0} r{1} has been edited to fix {2}", new Object[] { fullPageName, expectedRevisionID, fixed });
 						break;
 					} catch (IOException e) {
 						log.log(Level.WARNING, "Network error occurred while editing a page; retrying shortly", e);
 						shortDelay();
 					} catch (MediaWiki.BlockException e) {
 						log.log(Level.SEVERE, "User blocked; please check its block log", e);
 						System.exit(1);
 						return;
 					} catch (MediaWiki.ActionDelayException e) {
 						log.log(Level.WARNING, "Edit delayed by maintenance; retrying shortly", e);
 						shortDelay();
 					} catch (MediaWiki.ActionFailureException e) {
 						log.log(Level.SEVERE, "Edit failed permanently", e);
 						return;
 					} catch (MediaWiki.ConflictException e) {
 						log.log(Level.INFO, "Edit conflict received on {0}; waiting for another edit to be queued", fullPageName);
 						return;
 					} catch (MediaWiki.ContentException e) {
 						log.log(Level.SEVERE, "Content cannot be saved to the page", e);
 						return;
 					} catch (MediaWiki.MissingPageException e) {
 						log.log(Level.WARNING, "Page {0} cannot be replaced as it is now missing", fullPageName);
 						return;
 					} catch (MediaWiki.MediaWikiException e) {
 						log.log(Level.WARNING, "Network error occurred while editing a page; retrying shortly", e);
 						shortDelay();
 					}
 				}
 			} else {
 				log.log(Level.INFO, "{0} r{1} has no Visual Editor failures", new Object[] { fullPageName, expectedRevisionID });
 			}
 		}
 	}
 
 	public static String createWikilink(String linkTarget, String linkText) {
 		linkTarget = linkTarget.replace('_', ' ');
 		linkText = htmlEntitiesToCharacters(linkText);
 		if ((linkTarget.substring(0, 1).toLowerCase() + linkTarget.substring(1)).equals(linkText.substring(0, 1).toLowerCase() + linkText.substring(1))) {
 			// [[Name|name]], [[name|Name]]
 			return "[[" + linkText + "]]";
 		} else {
 			return "[[" + linkTarget + "|" + linkText + "]]";
 		}
 	}
 
 	private static final Pattern htmlNumericEntityMatcher = Pattern.compile("&#[0-9]+;");
 
 	public static String htmlEntitiesToCharacters(final String document) {
 		String newDocument = document;
 		Matcher m;
 		if ((m = htmlNumericEntityMatcher.matcher(newDocument)).find()) {
 			StringBuffer sb = new StringBuffer(newDocument.length());
 			do
 				m.appendReplacement(sb, "$1");
 			while (m.find());
 			m.appendTail(sb);
 			newDocument = sb.toString();
 		}
 		return newDocument;
 	}
 
 	private static final Pattern tagMatcher = Pattern.compile("<(/|)([a-zA-Z][^ >]+)[^>]*>");
 
 	/**
 	 * Gets the list of open HTML tags open at the given <code>position</code>
 	 * in the <code>wikitext</code>, outermost tag first.
 	 * 
 	 * @param wikitext
 	 *            The wikitext to parse.
 	 * @param position
 	 *            The position at which the open tags are to be retrieved.
 	 * @return the list of open HTML tags open at the given
 	 *         <code>position</code> in the <code>wikitext</code>, outermost tag
 	 *         first
 	 */
 	public static List<String> getActiveTags(final String wikitext, final int position) {
 		List<String> activeTags = new ArrayList<String>();
 		Matcher m = tagMatcher.matcher(wikitext.substring(0, position));
 		while (m.find()) {
 			if (m.group(1).equals("/")) {
 				// End tag.
 				for (int i = activeTags.size() - 1; i >= 0; i--) {
 					if (activeTags.get(i).equals(m.group(2))) {
 						// This tag matches. Close this one and those inside.
 						for (int j = activeTags.size() - 1; j >= i; j--) {
 							activeTags.remove(j);
 						}
 						break;
 					}
 				}
 			} else /*- if (m.group(1).length() == 0) */{
 				// Start tag. It may be self-closing, in which case it must not
 				// be added.
 				if (!(m.end() >= 2 && wikitext.charAt(m.end() - 2) == '/'))
 					activeTags.add(m.group(2));
 			}
 		}
 		return activeTags;
 	}
 
 	private static final Set<String> tagWhiteList = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
 
 	static {
 		tagWhiteList.add("b");
 		tagWhiteList.add("big"); // used in discussions, like Forum/Talk/UTalk
 		// blockquote is not whitelisted! it may be a verbatim quote
 		tagWhiteList.add("br"); // special case: don't barf on unclosed br
 		// code is not whitelisted! it must be preserved
 		tagWhiteList.add("del"); // used in discussions, like Forum/Talk/UTalk
 		tagWhiteList.add("div");
 		tagWhiteList.add("em");
 		tagWhiteList.add("h1");
 		tagWhiteList.add("h2");
 		tagWhiteList.add("h3");
 		tagWhiteList.add("h4");
 		tagWhiteList.add("h5");
 		tagWhiteList.add("h6");
 		tagWhiteList.add("hr"); // special case: don't barf on unclosed hr
 		tagWhiteList.add("i");
 		tagWhiteList.add("ins");
 		tagWhiteList.add("kbd");
 		// math is not whitelisted! {C} in math is TeX, and needs to be kept
 		// nowiki is not whitelisted for obvious reasons: nothing's interpreted
 		tagWhiteList.add("p");
 		// pre is not whitelisted! it must be preserved
 		// source (custom) is not whitelisted! it must be preserved
 		tagWhiteList.add("s"); // used in discussions, like Forum/Talk/UTalk
 		tagWhiteList.add("small"); // used in discussions, like Forum/Talk/UTalk
 		tagWhiteList.add("span");
 		tagWhiteList.add("strong");
 		tagWhiteList.add("sub");
 		tagWhiteList.add("sup");
 		tagWhiteList.add("tt");
 		tagWhiteList.add("u");
 	}
 
 	/**
 	 * Returns whether it's OK to edit wikitext contained within the given list
 	 * of HTML-style tags.
 	 * 
 	 * @param activeTags
 	 *            Active HTML-style tags, outermost first.
 	 * @return whether it's OK to edit wikitext contained within the given list
 	 *         of HTML-style tags
 	 */
 	public static boolean isTagListOK(final List<String> activeTags) {
 		for (String s : activeTags)
 			if (!tagWhiteList.contains(s))
 				return false;
 		return true;
 	}
 }
