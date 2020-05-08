 package kata.holdem;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 public class RoundWinnersTest {
 	@Test
 	public void given_two_players_should_identify_highest_card_as_the_winner() {
 		Map<String, HoleCards> playersAndTheirCards = new HashMap<String, HoleCards>();
 		
 		playersAndTheirCards.put("player-one", new HoleCards(Card.from("3c"), Card.from("4c")));
 		playersAndTheirCards.put("player-two", new HoleCards(Card.from("2d"), Card.from("3d")));
 		
 		RoundWinners winners = new RoundWinners(playersAndTheirCards, Collections.<Card>emptyList());
 		Assert.assertTrue(winners.isWinner("player-one"));
 		Assert.assertFalse(winners.isWinner("player-two"));
 	}
 
 	@Test
 	public void given_two_players_with_same_value_card_should_identify_both_as_the_winner() {
 		Map<String, HoleCards> playersAndTheirCards = new HashMap<String, HoleCards>();
 		
 		playersAndTheirCards.put("player-one", new HoleCards(Card.from("2c"), Card.from("3s")));
 		playersAndTheirCards.put("player-two", new HoleCards(Card.from("2d"), Card.from("3h")));
 		
 		RoundWinners winners = new RoundWinners(playersAndTheirCards, Collections.<Card>emptyList());
 		Assert.assertTrue(winners.isWinner("player-one"));
 		Assert.assertTrue(winners.isWinner("player-two"));
 	}
 
 	@Test
 	public void given_one_player_with_a_low_pair_and_one_player_with_only_high_card_should_identify_pair_as_winner() {
 		Map<String, HoleCards> playersAndTheirCards = new HashMap<String, HoleCards>();
 		
 		playersAndTheirCards.put("player-one", new HoleCards(Card.from("2d"), Card.from("2c")));
 		playersAndTheirCards.put("player-two", new HoleCards(Card.from("Ah"), Card.from("Kc")));
 		
 		RoundWinners winners = new RoundWinners(playersAndTheirCards, Collections.<Card>emptyList());
 		Assert.assertTrue(winners.isWinner("player-one"));
 		Assert.assertFalse(winners.isWinner("player-two"));
 	}
 
 	@Test
 	public void given_both_players_with_a_pair_should_identify_winner_with_the_highest_pair() {
 		Map<String, HoleCards> playersAndTheirCards = new HashMap<String, HoleCards>();
 		
 		playersAndTheirCards.put("pair-of-3s", new HoleCards(Card.from("2d"), Card.from("3c")));
 		playersAndTheirCards.put("pair-of-10s", new HoleCards(Card.from("Tc"), Card.from("Kc")));
 		
 		RoundWinners winners = new RoundWinners(playersAndTheirCards, Cards.from("8h", "3s", "Ad", "Th", "5s"));
 		Assert.assertTrue(winners.isWinner("pair-of-10s"));
 		Assert.assertFalse(winners.isWinner("pair-of-3s"));
 	}
 
 	@Test
 	public void given_both_players_with_the_same_pair_should_identify_winner_with_the_highest_kicker() {
 		Map<String, HoleCards> playersAndTheirCards = new HashMap<String, HoleCards>();
 		
 		playersAndTheirCards.put("7-kicker", new HoleCards(Card.from("2d"), Card.from("7c")));
 		playersAndTheirCards.put("jack-kicker", new HoleCards(Card.from("2c"), Card.from("Jc")));
 		
 		RoundWinners winners = new RoundWinners(playersAndTheirCards, Cards.from("5h", "3s", "Qd", "Th", "5s"));
 		Assert.assertTrue(winners.isWinner("jack-kicker"));
 		Assert.assertFalse(winners.isWinner("7-kicker"));
 	}
 
 	@Test
 	public void given_identical_best_pair_should_both_be_winners() {
 		Map<String, HoleCards> playersAndTheirCards = new HashMap<String, HoleCards>();
 		
 		playersAndTheirCards.put("player-1", new HoleCards(Card.from("2d"), Card.from("3c")));
 		playersAndTheirCards.put("player-2", new HoleCards(Card.from("2c"), Card.from("3d")));
 		
 		RoundWinners winners = new RoundWinners(playersAndTheirCards, Cards.from("5h", "8s", "Qd", "Qh", "9c"));
 		Assert.assertTrue(winners.isWinner("player-1"));
 		Assert.assertTrue(winners.isWinner("player-2"));
 	}
 
 	@Test
 	public void given_two_pairs_over_a_single_pair_should_identify_two_pairs_as_winner() {
 		Map<String, HoleCards> playersAndTheirCards = new HashMap<String, HoleCards>();
 		
 		playersAndTheirCards.put("player-1", new HoleCards(Card.from("2d"), Card.from("6c")));
		playersAndTheirCards.put("player-2", new HoleCards(Card.from("6d"), Card.from("Ah")));
 		
 		RoundWinners winners = new RoundWinners(playersAndTheirCards, Cards.from("2h", "6s", "Qd", "3h", "Tc"));
 		Assert.assertTrue(winners.isWinner("player-1"));
 		Assert.assertFalse(winners.isWinner("player-2"));
 	}
 
 	@Test
 	public void given_two_pairs_over_a_high_card_should_identify_two_pairs_as_winner() {
 		Map<String, HoleCards> playersAndTheirCards = new HashMap<String, HoleCards>();
 		
		playersAndTheirCards.put("player-1", new HoleCards(Card.from("3d"), Card.from("6c")));
 		playersAndTheirCards.put("player-2", new HoleCards(Card.from("2c"), Card.from("Ah")));
 		
 		RoundWinners winners = new RoundWinners(playersAndTheirCards, Cards.from("3h", "6s", "Qd", "5c", "Tc"));
 		Assert.assertTrue(winners.isWinner("player-1"));
 		Assert.assertFalse(winners.isWinner("player-2"));
 	}
 }
