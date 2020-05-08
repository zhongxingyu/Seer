 package org.seamoo.daos.twigImpl.question;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 import org.seamoo.daos.question.QuestionDao;
 import org.seamoo.daos.speed.NumericBagDao;
 import org.seamoo.daos.speed.QuestionEventDao;
 import org.seamoo.daos.twigImpl.TwigGenericDaoImpl;
 import org.seamoo.entities.League;
 import org.seamoo.entities.question.Question;
 import org.seamoo.lookup.NumericBag;
 import org.seamoo.speed.QuestionEvent;
 import org.seamoo.speed.QuestionEventType;
 import org.seamoo.utils.TimeProvider;
 import org.springframework.beans.factory.annotation.Autowired;
 
 public class TwigQuestionDaoImpl extends TwigGenericDaoImpl<Question, Long> implements QuestionDao {
 
 	private Random rndGenerator = new Random();
 	@Autowired
 	NumericBagDao numericBagDao;
 	@Autowired
 	QuestionEventDao questionEventDao;
 	@Autowired
 	TimeProvider timeProvider;
 
 	public static final long NUMERIC_BAG_CACHE_TIME = 60 * 1000;
 	private final String classifierPrefix = League.class.getCanonicalName();
 	private Map<Long, NumericBag> bagByLeagueId;
 	private Map<Long, Long> bagLastUpdated;
 
 	private synchronized NumericBag getNumericBagByLeagueId(Long leagueId) {
 		if (!bagByLeagueId.containsKey(leagueId)
 				|| bagLastUpdated.get(leagueId) + NUMERIC_BAG_CACHE_TIME <= timeProvider.getCurrentTimeStamp()) {
 			String cfier = classifierPrefix + "@" + leagueId;
 			NumericBag b = numericBagDao.findByClassifier(cfier);
 			if (b == null)
 				b = new NumericBag(cfier);
 			bagByLeagueId.put(leagueId, b);
 			bagLastUpdated.put(leagueId, timeProvider.getCurrentTimeStamp());
 			return b;
 		}
 		return bagByLeagueId.get(leagueId);
 	}
 
 	public TwigQuestionDaoImpl() {
 		bagByLeagueId = new HashMap<Long, NumericBag>();
 		bagLastUpdated = new HashMap<Long, Long>();
 	}
 
 	@Override
 	public Question persist(Question entity) {
 		boolean shouldAddKey = entity.getAutoId() == null;
 		Question q = super.persist(entity);
 		if (shouldAddKey) {
 			QuestionEvent qe = new QuestionEvent(QuestionEventType.CREATE, q.getAutoId(), q.getLeagueAutoId(),
 					timeProvider.getCurrentTimeStamp());
 			questionEventDao.persist(qe);
 		}
 		return q;
 	}
 
 	@Override
 	public Question[] persist(Question[] entities) {
 		boolean[] shouldAddKey = new boolean[entities.length];
 		Map<Long, Boolean> shouldPersistKeyBag = new HashMap<Long, Boolean>();
 		for (int i = 0; i < entities.length; i++) {
 			shouldAddKey[i] = entities[i].getAutoId() == null;
 			shouldPersistKeyBag.put(entities[i].getLeagueAutoId(), shouldAddKey[i]);
 		}
 		Question[] qs = super.persist(entities);
 		List<QuestionEvent> qes = new ArrayList<QuestionEvent>();
 		for (int i = 0; i < qs.length; i++) {
 			if (shouldAddKey[i])
 				qes.add(new QuestionEvent(QuestionEventType.CREATE, qs[i].getAutoId(), qs[i].getLeagueAutoId(),
 						timeProvider.getCurrentTimeStamp()));
 		}
 
 		if (qes.size() > 0)
 			questionEventDao.persist(qes.toArray(new QuestionEvent[qes.size()]));
 
 		return qs;
 	}
 
 	public List<Long> getRandomQuestionKeys(Long leagueId, int number) {
 		NumericBag autoIdBag = getNumericBagByLeagueId(leagueId);
 		int totalSize = autoIdBag.getSize();
 		if (number > totalSize) {
 			throw new IllegalArgumentException(String.format("%d exceeds the number of available questions", number));
 		}
		Set<Long> pickedAutoIds = new HashSet<Long>();
 		int pickedSize = 0;
		while (pickedAutoIds.size() < number) {
 			Long value = autoIdBag.get(rndGenerator.nextInt(totalSize));
			if (!pickedAutoIds.contains(value)) {
				pickedAutoIds.add(value);
 				pickedSize++;
 			}
 		}
		return new ArrayList<Long>(pickedAutoIds);
 	}
 
 	@Override
 	public void delete(Question entity) {
 		super.delete(entity);
 		QuestionEvent qe = new QuestionEvent(QuestionEventType.DELETE, entity.getAutoId(), 1L, timeProvider.getCurrentTimeStamp());
 		questionEventDao.persist(qe);
 	}
 }
