 package com.app.settleexpenses.test;
 
 import com.app.settleexpenses.domain.Participant;
 import com.app.settleexpenses.domain.ParticipantContribution;
 
 import junit.framework.TestCase;
 
 public class ParticipantContributionTest extends TestCase {
 
 	Participant participant;
 	
 	@Override
 	public void setUp() {
 		participant = new Participant("4", "Anand");
 	}
 	
 	public void testShouldAddContributionAmount() {
 		ParticipantContribution participantContribution = new ParticipantContribution(participant, 200);
 		participantContribution.addContribution(300);
		assertEquals(500f, participantContribution.getContribution());
 	}
 	
 	public void testShouldReturnTrueIfThereIsNoSettlementAmount() {
 		ParticipantContribution participantContribution = new ParticipantContribution(participant, 200);
 		participantContribution.addContribution(-200);
 		assertEquals(true, participantContribution.isSettled());
 	}
 	
 	public void testShouldReturnFalseIfThereIsSettlementAmount() {
 		ParticipantContribution participantContribution = new ParticipantContribution(participant, 200);
 		assertEquals(false, participantContribution.isSettled());
 	}
 	
 	public void testShouldReturnTrueIfParticipantHasToPay() {
 		ParticipantContribution participantContribution = new ParticipantContribution(participant, -200);
 		assertEquals(true, participantContribution.isPayer());
 	}
 
 	public void testShouldReturnFalseIfParticipantHasToGet() {
 		ParticipantContribution participantContribution = new ParticipantContribution(participant, 200);
 		assertEquals(false, participantContribution.isPayer());
 	}
 	
 	public void testShouldReturnTrueIfParticipantCanAcceptAmount() {
 		ParticipantContribution participantContribution = new ParticipantContribution(participant, 200);
 		assertEquals(true, participantContribution.canAcceptFullAmount(-100));
 	}
 	
 	public void testShouldReturnFalseIfParticipantCannotAcceptFullAmount() {
 		ParticipantContribution participantContribution = new ParticipantContribution(participant, 200);
 		assertEquals(false, participantContribution.canAcceptFullAmount(-500));
 	}
 	
 	public void testShouldReturnFalseIfParticipantIsAPayer() {
 		ParticipantContribution participantContribution = new ParticipantContribution(participant, -200);
 		assertEquals(false, participantContribution.canAcceptFullAmount(-500));
 	}
 }
