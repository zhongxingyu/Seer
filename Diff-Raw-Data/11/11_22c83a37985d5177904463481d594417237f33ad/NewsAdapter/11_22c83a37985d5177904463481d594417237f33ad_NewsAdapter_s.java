 package de.kile.zapfmaster2000.rest.core.util;
 
 import org.apache.log4j.Logger;
 
 import de.kile.zapfmaster2000.rest.api.news.AbstractNewsResponse;
 import de.kile.zapfmaster2000.rest.api.news.AchievementNewsResponse;
 import de.kile.zapfmaster2000.rest.api.news.DrawingNewsResponse;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.AchievementNews;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Challenge1v1DeclinedNews;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Challenge1v1DoneNews;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Challenge1v1StartedNews;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Drawing;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.DrawingNews;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.GainedAchievement;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.News;
 import de.kile.zapfmaster2000.rest.model.zapfmaster2000.Zapfmaster2000Package;
 
 public class NewsAdapter {
 
 	private static final Logger LOG = Logger.getLogger(NewsAdapter.class);
 
 	public AbstractNewsResponse adapt(News pNews) {
 		AbstractNewsResponse newsResponse = null;
 
 		switch (pNews.eClass().getClassifierID()) {
 		case Zapfmaster2000Package.DRAWING_NEWS:
 			DrawingNews drawingNews = (DrawingNews) pNews;
 			newsResponse = adaptDrawingNews(drawingNews);
 			break;
 		case Zapfmaster2000Package.ACHIEVEMENT_NEWS:
 			AchievementNews achievementNews = (AchievementNews) pNews;
 			newsResponse = adaptAchievementNews(achievementNews);
 			break;
 		case Zapfmaster2000Package.CHALLENGE1V1_STARTED_NEWS:
 			Challenge1v1StartedNews startedNews = (Challenge1v1StartedNews) pNews;
 			newsResponse = adaptChallenge1v1StartedNews(startedNews);
 			break;
 		case Zapfmaster2000Package.CHALLENGE1V1_DECLINED_NEWS:
 			Challenge1v1DeclinedNews declinedNews = (Challenge1v1DeclinedNews) pNews;
 			newsResponse = adaptChallenge1v1DeclinedNews(declinedNews);
 			break;
 		case Zapfmaster2000Package.CHALLENGE1V1_DONE_NEWS:
 			Challenge1v1DoneNews doneNews = (Challenge1v1DoneNews) pNews;
 			newsResponse = adaptChallenge1v1DonedNews(doneNews);
 			break;
 
 		default:
 			LOG.error("Unsupported news type: " + pNews.getClass().getName());
 		}
 
 		if (newsResponse != null) {
 			newsResponse.loadDate(pNews.getDate());
 		}
 		return newsResponse;
 	}
 
 	private AbstractNewsResponse adaptDrawingNews(DrawingNews pNews) {
 		DrawingNewsResponse drawingResp = new DrawingNewsResponse();
 		Drawing drawing = pNews.getDrawing();
 
 		drawingResp.loadDate(pNews.getDate());
 		drawingResp.setAmount(drawing.getAmount());
 		drawingResp.setKegId(drawing.getKeg().getId());
 		drawingResp.setBrand(drawing.getKeg().getBrand());
 		drawingResp.setLocation(drawing.getKeg().getBox().getLocation());
 		drawingResp.setImage(drawing.getUser().getImagePath());
 		drawingResp.setUserId(drawing.getUser().getId());
 		drawingResp.setUserName(drawing.getUser().getName());
 
 		return drawingResp;
 	}
 
 	private AbstractNewsResponse adaptAchievementNews(AchievementNews pNews) {
 		AchievementNewsResponse achievementResp = new AchievementNewsResponse();
 		GainedAchievement gainedAchievement = pNews.getGainedAchievment();
 
 		achievementResp.loadDate(pNews.getDate());
 		achievementResp.setImage(gainedAchievement.getAchievement()
 				.getImagePath());
 		achievementResp.setUserName(gainedAchievement.getUser().getName());
 		achievementResp.setUserId(gainedAchievement.getUser().getId());
 		achievementResp.setAchievementName(gainedAchievement.getAchievement()
 				.getName());
 		achievementResp.setAchievementId(gainedAchievement.getUser().getId());
 
 		return achievementResp;
 	}
 
 	private AbstractNewsResponse adaptChallenge1v1StartedNews(
 			Challenge1v1StartedNews pStartedNews) {
		AbstractNewsResponse news = new ChallengeAdapter()
 				.adaptChallenge(pStartedNews.getChallenge());
 		news.loadDate(pStartedNews.getDate());
 		return news;
 	}
 
 	private AbstractNewsResponse adaptChallenge1v1DeclinedNews(
 			Challenge1v1DeclinedNews pDeclinedNews) {
 		AbstractNewsResponse news = new ChallengeAdapter()
 				.adaptChallenge(pDeclinedNews.getChallenge());
 		news.loadDate(pDeclinedNews.getDate());
 		return news;
 	}
 
 	private AbstractNewsResponse adaptChallenge1v1DonedNews(
 			Challenge1v1DoneNews pDoneNews) {
 		AbstractNewsResponse news = new ChallengeAdapter()
 				.adaptChallenge(pDoneNews.getChallenge());
 		news.loadDate(pDoneNews.getDate());
 		return news;
 	}
 
 }
