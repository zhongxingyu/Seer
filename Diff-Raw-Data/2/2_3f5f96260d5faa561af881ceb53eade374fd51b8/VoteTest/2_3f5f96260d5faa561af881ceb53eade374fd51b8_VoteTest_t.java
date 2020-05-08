 package com.open.rotile.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.assertj.core.api.Assertions;
 import org.junit.Test;
 
 public class VoteTest {
 
 	@Test
 	public void votes_should_all_appear_in_string_representation() {
 		// Given
 		Votes vote = new Votes();
 		List<Integer> voteValues = new ArrayList<Integer>();
 		voteValues.add(3);
 		voteValues.add(2);
 		voteValues.add(4);
 
 		// When
 		for (Integer value : voteValues) {
			vote.vote(new Vote(value));
 		}
 
 		// Then
 		Assertions.assertThat(vote.toString()).isEqualTo(voteValues.toString());
 	}
 }
