 package com.open.rotile.model;
 
 import org.assertj.core.api.Assertions;
 import org.junit.Test;
 
 public class ProjectTest {
 
 	@Test
 	public void vote_adds_new_vote() {
 		// Given
 		final int voteValue = 4;
 		Project project = new Project();
 
 		// When
 		project.vote(voteValue);
 
 		// Then
 		Assertions.assertThat(project.votes.size()).isEqualTo(1);
 		Assertions.assertThat(project.votes.get(0).vote()).isEqualTo(voteValue);
 	}
 
 	@Test
 	public void nbvotes_returns_number_of_votes() {
 		// Given
 		Project project = new Project();
 		project.vote(2);
 		project.vote(4);
 		project.vote(6);
 		project.vote(8);
 		final int expectedNbVotes = 4;
 
 		// When
 		int nbVotes = project.nbVotes();
 
 		// Then
 		Assertions.assertThat(nbVotes).isEqualTo(expectedNbVotes);
 	}
 
 	@Test
 	public void average_returns_votes_average() {
 		// Given
 		Project project = new Project();
 		project.vote(2);
 		project.vote(4);
 		project.vote(6);
 		project.vote(8);
 		final int expectedAverage = 5;
 
 		// When
 		int average = project.average();
 
 		// Then
 		Assertions.assertThat(average).isEqualTo(expectedAverage);
 	}
 }
