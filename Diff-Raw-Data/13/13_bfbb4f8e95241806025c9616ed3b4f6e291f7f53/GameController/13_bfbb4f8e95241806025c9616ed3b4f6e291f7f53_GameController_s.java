 package controllers;
 
 import java.util.List;
 
 import org.codehaus.jackson.node.ObjectNode;
 import org.openrdf.query.MalformedQueryException;
 import org.openrdf.query.QueryEvaluationException;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.object.ObjectConnection;
 
 import models.*;
 
 import global.AuthenticationTokenGenerator;
 import global.Sesame;
 import play.data.DynamicForm;
 import play.mvc.Result;
 import play.mvc.Controller;
 import repository.HuntRepository;
 import repository.UserRepository;
 
 public class GameController extends Controller {
 
 	public static final String CURRENT_HUNT_SESSION = "current_hunt";
 	public static final String CURRENT_STEP_SESSION = "current_step";
 	public static final String CURRENT_ENIGMA_SESSION = "current_enigma";
 	public static final String CURRENT_CLUE_SESSION = "current_clue";
 	public static final String ENIGMA_SCORE_SESSION = "enigma_score";
 	public static final String TOTAL_SCORE_SESSION = "total_score";
 
 	private static Hunt getCurrentHunt() throws RepositoryException, QueryEvaluationException {
 		ObjectConnection oc = Sesame.getObjectConnection();
 
 		if (null == session(CURRENT_HUNT_SESSION)) {
 			return null;
 		}
 
 		return oc.getObject(Hunt.class, Hunt.URI + session(CURRENT_HUNT_SESSION));
 	}
 
 	private static Step getCurrentStep() throws RepositoryException, QueryEvaluationException {
 		ObjectConnection oc = Sesame.getObjectConnection();
 
 		if (null == session(CURRENT_STEP_SESSION)) {
 			return null;
 		}
 
 		return oc.getObject(Step.class, Step.URI + session(CURRENT_STEP_SESSION));
 	}
 
 	private static Enigma getCurrentEnigma() throws RepositoryException, QueryEvaluationException {
 		ObjectConnection oc = Sesame.getObjectConnection();
 
 		if (null == session(CURRENT_ENIGMA_SESSION)) {
 			return null;
 		}
 
 		return oc.getObject(Enigma.class, Enigma.URI + session(CURRENT_ENIGMA_SESSION));
 	}
 
 	private static Clue getCurrentClue() throws RepositoryException, QueryEvaluationException {
 		ObjectConnection oc = Sesame.getObjectConnection();
 
 		if (null == session(CURRENT_CLUE_SESSION)) {
 			return null;
 		}
 
 		return oc.getObject(Clue.class, Clue.URI + session(CURRENT_CLUE_SESSION));
 	}
 
 	private static void nextEnigma() throws RepositoryException, QueryEvaluationException {
 		Enigma enigma = getCurrentEnigma();
 
 		if (null == enigma) {
 			throw new NullPointerException();
 		}
 
 		Enigma nextEnigma = enigma.getNextEnigma();
 		if (nextEnigma != null) {
 			session().put(CURRENT_ENIGMA_SESSION, nextEnigma.getId());
 		} else {
 			session().remove(CURRENT_ENIGMA_SESSION);
 			nextStep();
 		}
 
 		session().remove(CURRENT_CLUE_SESSION);
 		session().remove(ENIGMA_SCORE_SESSION);
 	}
 
 	private static void nextStep() throws RepositoryException, QueryEvaluationException {
 		Step step = getCurrentStep();
 
 		if (null == step) {
 			throw new NullPointerException();
 		}
 
 		Step nextStep = step.getNextStep();
 		if (nextStep != null) {
 			session().put(CURRENT_STEP_SESSION, nextStep.getId());
 		} else {
 			session().remove(CURRENT_STEP_SESSION);
 		}
 	}
 
 	private static int getCurrentScore() {
 		if (session(ENIGMA_SCORE_SESSION) == null) {
 			return -1;
 		} else {
 			return Integer.parseInt(session(ENIGMA_SCORE_SESSION));
 		}
 	}
 
 	private static int getTotalScore() {
 		if (session(TOTAL_SCORE_SESSION) == null) {
 			return 0;
 		} else {
 			return Integer.parseInt(session(TOTAL_SCORE_SESSION));
 		}
 	}
 
 	private static int getMaximumScore() throws RepositoryException, QueryEvaluationException {
 		Hunt currentHunt = getCurrentHunt();
 
 		int max = 0;
 		for (Step s : currentHunt.getSteps()) {
 			for (Enigma e : s.getEnigmas()) {
 				max += computeScore(0, e.getClues().size());
 			}
 		}
 		
 		return max;
 	}
 
 	private static void saveCurrentScore(int scoreValue) throws RepositoryException, QueryEvaluationException {
 		session().put(ENIGMA_SCORE_SESSION, Integer.toString(scoreValue));
 		session().put(TOTAL_SCORE_SESSION, Integer.toString(scoreValue + getTotalScore()));
 		
 		ObjectConnection oc = Sesame.getObjectConnection();
 		
 		User loggedUser = UserRepository.getLoggedUser();
 		if (null != loggedUser) {
 			String uri = Score.URI + loggedUser.getId() + "/" + getCurrentHunt().getId();
			Score score = oc.getObject(Score.class, uri);
			if (null == score) {
				Hunt currentHunt = getCurrentHunt();
				score = new Score();
				score.setHunt(currentHunt);
				score.setUSer(loggedUser);
			}
 			score.setValue(scoreValue);
 			oc.addObject(uri, score);
 		}
 	}
 
 	private static int computeScore(int usedClues, int totalClues) {
 		if (totalClues == 0) {
 			return 100;
 		}
 
 		int remainingClues = totalClues - usedClues;
 		float rate = (float) remainingClues / (float) totalClues;
 
 		float scoreMin = 50 / (float) totalClues + 1;
 		int score = Math.round((rate * rate * (100 - scoreMin)) + scoreMin);
 
 		return score;
 	}
 
 	public static Result home() throws MalformedQueryException, RepositoryException, QueryEvaluationException {
 		List<Hunt> hunts = HuntRepository.getHuntsSortedByDate();
 
 		return ok(views.html.game.home.render(hunts));
 	}
 
 	public static Result playHunt(String hid) throws RepositoryException, QueryEvaluationException {
 		ObjectConnection oc = Sesame.getObjectConnection();
 		Hunt hunt = oc.getObject(Hunt.class, Hunt.URI + hid);
 		Hunt currentHunt = getCurrentHunt();
 
 		if (hunt.equals(currentHunt)) {
 			return redirect(routes.GameController.go());
 		}
 
 		session().put(CURRENT_HUNT_SESSION, hunt.getId());
 		try {
 			session().put(CURRENT_STEP_SESSION, hunt.getSteps().get(0).getId());
 		} catch (IndexOutOfBoundsException e) {
 			session().remove(CURRENT_STEP_SESSION);
 		}
 
 		session().remove(CURRENT_ENIGMA_SESSION);
 		session().remove(CURRENT_CLUE_SESSION);
 		session().remove(ENIGMA_SCORE_SESSION);
 		session().remove(TOTAL_SCORE_SESSION);
 
 		return ok(views.html.game.playHunt.render(hunt));
 	}
 
 	public static Result startStep() throws RepositoryException, QueryEvaluationException {
 		DynamicForm data = form().bindFromRequest();
 		if (!AuthenticationTokenGenerator.isValid(data.get("token"))) {
 			return redirect(routes.GameController.go());
 		}
 
 		Step currentStep = getCurrentStep();
 
 		if (null == getCurrentEnigma()) {
 			Enigma firstEnigma = currentStep.getEnigmas().get(0);
 			if (null != firstEnigma) {
 				session().put(CURRENT_ENIGMA_SESSION, firstEnigma.getId());
 			}
 		}
 
 		return redirect(routes.GameController.go());
 	}
 
 	public static Result go() throws RepositoryException, QueryEvaluationException {
 		Hunt currentHunt = getCurrentHunt();
 		if (null == currentHunt) {
 			return redirect(routes.GameController.home());
 		}
 
 		Step currentStep = getCurrentStep();
 		if (null == currentStep) {
 			return ok(views.html.game.finish.render(currentHunt, getTotalScore(), getMaximumScore()));
 		}
 
 		if (session(CURRENT_ENIGMA_SESSION) == null) {
 			return ok(views.html.game.go.render(currentStep));
 		}
 
 		Enigma currentEnigma = getCurrentEnigma();
 
 		List<Clue> clues = currentEnigma.getClues();
 		Clue currentClue = getCurrentClue();
 		int indexOfLastClue = clues.indexOf(currentClue);
 		List<Clue> usedClues = clues.subList(0, indexOfLastClue + 1);
 
 		if (-1 == getCurrentScore()) {
 			return ok(views.html.game.enigma.render(currentEnigma, usedClues));
 		} else if (0 == getCurrentScore()) {
 			return ok(views.html.game.summaryEnigmaFail.render(currentEnigma));
 		} else {
 			List<Clue> otherClues = clues.subList(indexOfLastClue + 1, clues.size());
 
 			return ok(views.html.game.summaryEnigmaGood.render(currentEnigma, usedClues, otherClues, getCurrentScore()));
 		}
 	}
 
 	public static Result finishEnigma() throws RepositoryException, QueryEvaluationException {
 		DynamicForm data = form().bindFromRequest();
 		if (AuthenticationTokenGenerator.isValid(data.get("token"))) {
 			nextEnigma();
 		}
 
 		return redirect(routes.GameController.go());
 	}
 
 	public static Result skipEnigma() throws RepositoryException, QueryEvaluationException {
 		DynamicForm data = form().bindFromRequest();
 		if (!AuthenticationTokenGenerator.isValid(data.get("token"))) {
 			return redirect(routes.GameController.go());
 		}
 
 		saveCurrentScore(0);
 
 		return redirect(routes.GameController.go());
 	}
 
 	public static Result checkAnswer() throws RepositoryException, QueryEvaluationException {
 		if (session(CURRENT_ENIGMA_SESSION) == null) {
 			return noContent();
 		}
 
 		DynamicForm data = form().bindFromRequest();
 		String answer = data.get("answer");
 
 		Enigma enigma = getCurrentEnigma();
 
 		if (null == enigma) {
 			return notFound();
 		}
 
 		if (enigma.getAnswer().isCorrect(answer)) {
 			int indexOfLastClue = -1;
 			Clue currentClue = getCurrentClue();
 			indexOfLastClue = enigma.getClues().indexOf(currentClue);
 			int score = computeScore(indexOfLastClue + 1, enigma.getClues().size());
 
 			saveCurrentScore(score);
 
 			return ok("true");
 		} else {
 			return noContent();
 		}
 	}
 
 	public static Result clue() throws RepositoryException, QueryEvaluationException {
 		DynamicForm data = form().bindFromRequest();
 		if (!AuthenticationTokenGenerator.isValid(data.get("token"))) {
 			return noContent();
 		}
 
 		Enigma enigma = getCurrentEnigma();
 		if (null == enigma) {
 			return noContent();
 		}
 
 		Clue nextClue = getCurrentClue();
 		if (null == nextClue) {
 			if (enigma.getClues().size() > 0) {
 				nextClue = enigma.getClues().get(0);
 			} else {
 				noContent();
 			}
 		} else {
 			Clue clue = nextClue.getNextClue();
 			if (clue == null) {
 				return noContent();
 			}
 			nextClue = clue;
 		}
 
 		session().put(CURRENT_CLUE_SESSION, nextClue.getId());
 
 		ObjectNode result = nextClue.toJson();
 
 		return ok(result);
 	}
 
 }
