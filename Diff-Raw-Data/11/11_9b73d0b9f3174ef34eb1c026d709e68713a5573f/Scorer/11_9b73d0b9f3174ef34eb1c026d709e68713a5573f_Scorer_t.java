 import com.google.inject.Inject;
 
 import java.util.Set;
 
 public class Scorer {
 	private final TalkIds talkIds;
 	private final Votes scores;
 
 	@Inject
 	public Scorer(TalkIds talkIds, Votes scores) {
 		this.talkIds = talkIds;
 		this.scores = scores;
 	}
 
 	public int get(String keyword) {
 		Set<Integer> ids = talkIds.withKeyword(keyword);
 		return scores.getScore(ids);
 	}
 }
