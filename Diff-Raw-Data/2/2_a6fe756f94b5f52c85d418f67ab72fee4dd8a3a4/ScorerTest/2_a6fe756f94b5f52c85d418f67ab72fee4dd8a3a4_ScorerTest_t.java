 import com.google.common.collect.ImmutableSet;
 import org.junit.Test;
 
 import java.util.Arrays;
 
 import static org.fest.assertions.Assertions.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 public class ScorerTest {
 	TalkIds talkIds = mock(TalkIds.class);
 	Scores scores = mock(Scores.class);
 
 	@Test
 	public void should_compute_score_for_keywords() {
 		when(talkIds.withKeyword("angularJs")).thenReturn(ImmutableSet.of(1, 2));
		when(scores.getScore(ImmutableSet.of(1, 2))).thenReturn(100);
 
 		Scorer scorer = new Scorer(talkIds, scores);
 
 		int score = scorer.get("angularJs");
 
 		assertThat(score).isEqualTo(100);
 	}
 
 }
