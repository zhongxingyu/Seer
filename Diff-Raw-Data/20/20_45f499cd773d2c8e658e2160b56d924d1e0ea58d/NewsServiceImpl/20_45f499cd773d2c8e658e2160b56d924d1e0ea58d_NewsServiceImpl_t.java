 package de.kile.zapfmaster2000.rest.impl.core.news;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 import de.kile.zapfmaster2000.rest.core.Zapfmaster2000Core;
 import de.kile.zapfmaster2000.rest.core.achievement.AchievementService;
 import de.kile.zapfmaster2000.rest.core.achievement.AchievementServiceListener;
 import de.kile.zapfmaster2000.rest.core.box.BoxService;
 import de.kile.zapfmaster2000.rest.core.box.BoxServiceListener;
 import de.kile.zapfmaster2000.rest.core.box.LoginFailureReason;
 import de.kile.zapfmaster2000.rest.core.challenge.ChallengeService;
 import de.kile.zapfmaster2000.rest.core.challenge.ChallengeServiceListener;
 import de.kile.zapfmaster2000.rest.core.keg.KegService;
 import de.kile.zapfmaster2000.rest.core.keg.KegServiceListener;
 import de.kile.zapfmaster2000.rest.core.news.NewsService;
 import de.kile.zapfmaster2000.rest.core.news.NewsServiceListener;
 import de.kile.zapfmaster2000.rest.core.registration.RegistrationService;
 import de.kile.zapfmaster2000.rest.core.registration.RegistrationServiceListener;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Account;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.AchievementNews;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Box;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Challenge;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Challenge1v1;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Challenge1v1DeclinedNews;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Challenge1v1DoneNews;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Challenge1v1StartedNews;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Drawing;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.DrawingNews;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.GainedAchievement;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Keg;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.NewKegNews;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.NewUserNews;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.News;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.User;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Zapfmaster2000Factory;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Zapfmaster2000Package;
 
 public class NewsServiceImpl implements NewsService {
 
 	/** logger */
 	private static final Logger LOG = Logger.getLogger(NewsServiceImpl.class);
 
 	/** listeners */
 	private static List<NewsServiceListener> listeners = new ArrayList<>();
 
 	public NewsServiceImpl(BoxService pBoxService,
 			AchievementService pAchievementService,
 			ChallengeService pChallengeService, KegService pKegService,
 			RegistrationService pRegistrationService) {
 		pBoxService.addListener(createBoxServiceListener());
 		pAchievementService.addListener(createAchievementServiceListener());
 		pChallengeService.addListener(createChallengeServiceListener());
 		pKegService.addListener(createKegServiceListener());
 		pRegistrationService.addListener(createRegistrationServiceListener());
 	}
 
 	@Override
 	public void addListener(NewsServiceListener pListener) {
 		if (pListener != null) {
 			listeners.add(pListener);
 		}
 
 	}
 
 	@Override
 	public void removeListener(NewsServiceListener pListener) {
 		listeners.remove(pListener);
 	}
 
 	private AchievementServiceListener createAchievementServiceListener() {
 		return new AchievementServiceListener() {
 			@Override
 			public void onAchievementGained(GainedAchievement pGainedAchievement) {
 				postNewsAchievementGained(pGainedAchievement);
 			}
 		};
 	}
 
 	private BoxServiceListener createBoxServiceListener() {
 		return new BoxServiceListener() {
 
 			@Override
 			public void onLoginsuccessful(Box pBox, User pUser) {
 			}
 
 			@Override
 			public void onDrawing(Box pBox, User pUser, double pAmount) {
 			}
 
 			@Override
 			public void onEndDrawing(Box pBox, Drawing pDrawing) {
 				postNewsDrawFinished(pBox, pDrawing);
 			}
 
 			@Override
 			public void onLogout(Box pBox, User pUser) {
 			}
 
 			@Override
 			public void onLoginFailed(Box pBox, LoginFailureReason pReason,
 					long pTag) {
 			}
 		};
 	}
 
 	private ChallengeServiceListener createChallengeServiceListener() {
 		return new ChallengeServiceListener() {
 
 			@Override
 			public void onPendingChallengeCreated(Challenge pChallenge) {
 			}
 
 			@Override
 			public void onChallengeStarted(Challenge pChallenge) {
 				postNewsChallengeStarted(pChallenge);
 			}
 
 			@Override
 			public void onChallengeFinished(Challenge pChallenge) {
 				postNewsChallengeFinished(pChallenge);
 
 			}
 
 			@Override
 			public void onChallengeDeclined(Challenge pChallenge) {
 				postNewsChallengeDeclined(pChallenge);
 			}
 		};
 	}
 
 	private KegServiceListener createKegServiceListener() {
 		return new KegServiceListener() {
 			@Override
 			public void onNewKeg(Keg pKeg) {
 				postNewsNewKeg(pKeg);
 			}
 		};
 	}
 
 	public RegistrationServiceListener createRegistrationServiceListener() {
 		return new RegistrationServiceListener() {
 			
 			@Override
 			public void onNewUserRegistered(User pUser) {
 				postNewUserNews(pUser);
 			}
 		};
 	}
 
	private synchronized void postNewsDrawFinished(Box pBox, Drawing pDrawing) {
 		LOG.debug("Posting news (draw finished): " + pDrawing.getId());
 
 		// find system for the given box
 		Session session = Zapfmaster2000Core.INSTANCE.getTransactionService()
 				.getSessionFactory().getCurrentSession();
 		Transaction tx = session.beginTransaction();
 
 		@SuppressWarnings("unchecked")
 		List<Account> accounts = session
 				.createQuery("SELECT b.account FROM Box b WHERE b.id = :id")
 				.setLong("id", pBox.getId()).list();
 		pDrawing = (Drawing) session.load(Zapfmaster2000Package.eINSTANCE
 				.getDrawing().getName(), pDrawing.getId());
 
 		DrawingNews news = null;
 		if (accounts.size() == 1) {
 			news = Zapfmaster2000Factory.eINSTANCE.createDrawingNews();
 			news.setAccount(accounts.get(0));
 			news.setDate(new Date());
 			news.setDrawing(pDrawing);
 			session.save(news);
 		} else {
 			LOG.error("Could not post news: No (unique) system found for box "
 					+ pBox.getId());
 		}
 
 		tx.commit();
 
 		if (news != null) {
 			notifiyListenersNewsPosted(news);
 		}
 	}
 
	private synchronized void postNewsAchievementGained(GainedAchievement pGainedAchievement) {
 		LOG.debug("Posting news (achievement gained): "
 				+ pGainedAchievement.getId());
 
 		Session session = Zapfmaster2000Core.INSTANCE.getTransactionService()
 				.getSessionFactory().getCurrentSession();
 		Transaction tx = session.beginTransaction();
 
 		pGainedAchievement = (GainedAchievement) session.load(
 				Zapfmaster2000Package.eINSTANCE.getGainedAchievement()
 						.getName(), pGainedAchievement.getId());
 
 		AchievementNews news = Zapfmaster2000Factory.eINSTANCE
 				.createAchievementNews();
 		news.setAccount(pGainedAchievement.getUser().getAccount());
 		news.setDate(new Date());
 		news.setGainedAchievment(pGainedAchievement);
 		session.save(news);
 
 		tx.commit();
 		notifiyListenersNewsPosted(news);
 	}
 
	private synchronized void postNewsChallengeStarted(Challenge pChallenge) {
 		if (pChallenge instanceof Challenge1v1) {
 			Challenge1v1 challenge1v1 = (Challenge1v1) pChallenge;
 
 			Session session = Zapfmaster2000Core.INSTANCE
 					.getTransactionService().getSessionFactory()
 					.getCurrentSession();
 			Transaction tx = session.beginTransaction();
 			challenge1v1 = (Challenge1v1) session
 					.load(Zapfmaster2000Package.eINSTANCE.getChallenge1v1()
 							.getName(), challenge1v1.getId());
 
 			Challenge1v1StartedNews news = Zapfmaster2000Factory.eINSTANCE
 					.createChallenge1v1StartedNews();
 			news.setAccount(challenge1v1.getUser1().getAccount());
 			news.setDate(new Date());
 			news.setChallenge(challenge1v1);
 			session.save(news);
 
 			tx.commit();
 			notifiyListenersNewsPosted(news);
 		} else {
 			LOG.error("Unknown challenge type:" + pChallenge.eClass().getName());
 		}
 	}
 
	private synchronized void postNewsChallengeFinished(Challenge pChallenge) {
 		if (pChallenge instanceof Challenge1v1) {
 			Challenge1v1 challenge1v1 = (Challenge1v1) pChallenge;
 
 			Session session = Zapfmaster2000Core.INSTANCE
 					.getTransactionService().getSessionFactory()
 					.getCurrentSession();
 			Transaction tx = session.beginTransaction();
 			challenge1v1 = (Challenge1v1) session
 					.load(Zapfmaster2000Package.eINSTANCE.getChallenge1v1()
 							.getName(), challenge1v1.getId());
 
 			Challenge1v1DoneNews news = Zapfmaster2000Factory.eINSTANCE
 					.createChallenge1v1DoneNews();
 			news.setAccount(challenge1v1.getUser1().getAccount());
 			news.setDate(new Date());
 			news.setChallenge(challenge1v1);
 			session.save(news);
 
 			tx.commit();
 			notifiyListenersNewsPosted(news);
 		} else {
 			LOG.error("Unknown challenge type:" + pChallenge.eClass().getName());
 		}
 	}
 
	private synchronized void postNewsChallengeDeclined(Challenge pChallenge) {
 		if (pChallenge instanceof Challenge1v1) {
 			Challenge1v1 challenge1v1 = (Challenge1v1) pChallenge;
 
 			Session session = Zapfmaster2000Core.INSTANCE
 					.getTransactionService().getSessionFactory()
 					.getCurrentSession();
 			Transaction tx = session.beginTransaction();
 			challenge1v1 = (Challenge1v1) session
 					.load(Zapfmaster2000Package.eINSTANCE.getChallenge1v1()
 							.getName(), challenge1v1.getId());
 
 			Challenge1v1DeclinedNews news = Zapfmaster2000Factory.eINSTANCE
 					.createChallenge1v1DeclinedNews();
 			news.setAccount(challenge1v1.getUser1().getAccount());
 			news.setDate(new Date());
 			news.setChallenge(challenge1v1);
 			session.save(news);
 
 			tx.commit();
 			notifiyListenersNewsPosted(news);
 		} else {
 			LOG.error("Unknown challenge type:" + pChallenge.eClass().getName());
 		}
 	}
 
	private synchronized void postNewsNewKeg(Keg pKeg) {
 		Session session = Zapfmaster2000Core.INSTANCE.getTransactionService()
 				.getSessionFactory().getCurrentSession();
 		Transaction tx = session.beginTransaction();
 
 		pKeg = (Keg) session.load(Zapfmaster2000Package.eINSTANCE.getKeg()
 				.getName(), pKeg.getId());
 
 		NewKegNews news = Zapfmaster2000Factory.eINSTANCE.createNewKegNews();
 		news.setAccount(pKeg.getBox().getAccount());
 		news.setDate(new Date());
 		news.setKeg(pKeg);
 		session.save(news);
 
 		tx.commit();
 		notifiyListenersNewsPosted(news);
 	}
 
	private synchronized void postNewUserNews(User pUser) {
 		Session session = Zapfmaster2000Core.INSTANCE.getTransactionService()
 				.getSessionFactory().getCurrentSession();
 		Transaction tx = session.beginTransaction();
 
 		pUser = (User) session.load(Zapfmaster2000Package.eINSTANCE.getUser()
 				.getName(), pUser.getId());
 
 		NewUserNews news = Zapfmaster2000Factory.eINSTANCE.createNewUserNews();
 		news.setAccount(pUser.getAccount());
 		news.setDate(new Date());
 		news.setUser(pUser);
 		session.save(news);
 
 		tx.commit();
 		notifiyListenersNewsPosted(news);
 	}
 	
 	private void notifiyListenersNewsPosted(News pNews) {
 		for (NewsServiceListener listener : listeners) {
 			listener.onNewsPosted(pNews);
 		}
 	}
 
 }
