 package org.seamoo.competition;
 
 import static org.mockito.Matchers.*;
 import static org.mockito.Mockito.*;
 import static org.testng.Assert.*;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeoutException;
 
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 import org.seamoo.cache.CacheWrapper;
 import org.seamoo.cache.CacheWrapperFactory;
 import org.seamoo.daos.MemberDao;
 import org.seamoo.daos.matching.MatchDao;
 import org.seamoo.daos.question.QuestionDao;
 import org.seamoo.entities.Member;
 import org.seamoo.entities.matching.Match;
 import org.seamoo.entities.matching.MatchPhase;
 import org.testng.annotations.Test;
 
 public class MatchOrganizerPerformanceTest {
 
 	private static long SLEEP_UNIT = 1;
 
 	public static class CountableCacheWrapper<T> implements CacheWrapper<T> {
 
 		protected long lockCount;
 		protected long lockTryTime;
 		private boolean locked;
 		private String key;
 		private Map<String, Object> map;
 
 		public CountableCacheWrapper(String key, Map<String, Object> map) {
 			lockCount = 0;
 			lockTryTime = 0;
 			this.key = key;
 			this.map = map;
 		}
 
 		@Override
 		public T getObject() {
 			// TODO Auto-generated method stub
 			try {
 				Thread.sleep(SLEEP_UNIT * 3 / 10);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return (T) map.get(this.key);
 		}
 
 		@Override
 		public synchronized void lock(long timeout) throws TimeoutException {
 			// TODO Auto-generated method stub
 			long sleep = 0;
 			while (this.locked) {
 				try {
 					Thread.sleep(SLEEP_UNIT);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				sleep += SLEEP_UNIT;
 				lockTryTime += SLEEP_UNIT;
 				if (sleep >= timeout)
 					throw new TimeoutException();
 			}
 			this.locked = true;
 			this.lockCount++;
 		}
 
 		@Override
 		public void putObject(T object) {
 			// TODO Auto-generated method stub
 			try {
 				Thread.sleep(SLEEP_UNIT * 3 / 10);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			this.map.put(key, object);
 		}
 
 		@Override
 		public void unlock() {
 			// TODO Auto-generated method stub
 			if (this.locked == true)
 				this.locked = false;
 			else
 				throw new IllegalStateException("Cannot unlock");
 		}
 
 	}
 
 	public static class CountableCacheWrapperFactory implements CacheWrapperFactory {
 
 		Map<String, Object> map;
 
 		public CountableCacheWrapperFactory() {
 			this.map = new HashMap<String, Object>();
 		}
 
 		@Override
 		public <T> CacheWrapper<T> createCacheWrapper(Class<T> clazz, String key) {
 			// TODO Auto-generated method stub
 			return new CountableCacheWrapper<T>(key, map);
 		}
 
 	}
 
 	private Thread getTypicalMemberActionThread(final MatchOrganizer organizer, final Long memberId) {
 		Thread t = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				// Wait for match to be formed
 				Match m;
 				do {
 					try {
 						Thread.sleep(30 * SLEEP_UNIT);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					m = organizer.getMatchForUser(memberId);
 				} while (m.getPhase() != MatchPhase.PLAYING);
 
 				// Submit answer
 				for (int i = 0; i < m.getQuestions().size(); i++) {
 					try {
 						Thread.sleep(30 * SLEEP_UNIT);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					if (i % 2 == 0)
 						organizer.submitAnswer(memberId, i + 1, "1");
 					else
 						organizer.ignoreQuestion(memberId, i + 1);
 				}
 
 				// Wait for match to be finished
 				do {
 					try {
 						Thread.sleep(30 * SLEEP_UNIT);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					m = organizer.getMatchForUser(memberId);
 				} while (m.getPhase() != MatchPhase.FINISHED);
 
 			}
 		});
 		return t;
 	}
 
 	@Test
 	public void matchOrganizerShouldNotLockTooMuch() {
 		MatchOrganizerSettings settings = new MatchOrganizerSettings();
 		settings.setMatchCountDownTime(100 * SLEEP_UNIT);
 		settings.setMatchTime(120 * 10 * SLEEP_UNIT);
 		settings.setMaxLockWaitTime(50 * SLEEP_UNIT);
 		settings.setCandidateActivePeriod(Long.MAX_VALUE / 2);// make sure
 		// active period
 		// is long
 		// enough for
 		// debugging
 		// purpose
 		// never expired
 		MatchOrganizer organizer = new MatchOrganizer(1L, settings);
 		organizer.matchDao = mock(MatchDao.class);
 		organizer.cacheWrapperFactory = new CountableCacheWrapperFactory();
 		organizer.memberDao = mock(MemberDao.class);
 		when(organizer.memberDao.findByKey(anyLong())).thenAnswer(new Answer<Member>() {
 
 			@Override
 			public Member answer(InvocationOnMock invocation) throws Throwable {
 				// TODO Auto-generated method stub
 				Long memberAutoId = (Long) invocation.getArguments()[0];
 				Member m = new Member();
 				m.setAutoId(memberAutoId);
 				m.setDisplayName("User #" + memberAutoId);
 				return m;
 			}
 		});
 		organizer.questionDao = mock(QuestionDao.class);
 
 		Thread[] threads = new Thread[100];
 		for (int i = 0; i < threads.length; i++) {
 			threads[i] = getTypicalMemberActionThread(organizer, new Long(i + 1));
 			threads[i].start();
 			try {
 				Thread.sleep(5 * SLEEP_UNIT);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		long start = TimeStampProvider.getCurrentTimeMilliseconds();
 
 		while (true) {
 			boolean finishedAll = true;
 			for (int i = 0; i < threads.length; i++)
 				if (threads[i].isAlive()) {
 					finishedAll = false;
 					break;
 				}
 			if (finishedAll)
 				break;
 		}
 
 		long end = TimeStampProvider.getCurrentTimeMilliseconds();
 
 		CountableCacheWrapper<List> fullWaitingMatches = (CountableCacheWrapper<List>) organizer.fullWaitingMatches;
 		CountableCacheWrapper<List> notFullWaitingMatches = (CountableCacheWrapper<List>) organizer.notFullWaitingMatches;
 
 		System.out.println("notFullWaitingMatches.lockCount = " + notFullWaitingMatches.lockCount);
 		System.out.println("notFullWaitingMatches.lockTryTime = " + notFullWaitingMatches.lockTryTime);
 		System.out.println("end-start = " + (end - start));
		System.out.println("expected match time = " + (settings.getMatchCountDownTime()+ settings.getMatchTime()));
 		if (notFullWaitingMatches.lockCount > 200)
 			fail("Expect <=200 lockCount on notFullWaitingMatches but got " + notFullWaitingMatches.lockCount);
 		if (notFullWaitingMatches.lockTryTime > 300 * SLEEP_UNIT)
 			fail("Expect <=300 SLEEP_UNIT lockTryTime on notFullWaitingMatches but got " + notFullWaitingMatches.lockTryTime
 					/ SLEEP_UNIT);
 	}
 }
