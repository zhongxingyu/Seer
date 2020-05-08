 package com.app.settleexpenses.domain;
 
 public class ParticipantContribution {
     private Participant participant;
     private float contribution;
 
     public ParticipantContribution(Participant participant, float contribution) {
         this.participant = participant;
         this.contribution = contribution;
     }
 
     public void addContribution(float amount) {
         contribution+=amount;
     }
 
     public boolean isPayer() {
         return contribution < 0;
     }
 
    public boolean isSettled() {
        return ((int)contribution == 0);
    }

     public float getContribution() {
         return contribution;
     }
 
     public boolean canAcceptFullAmount(float contribution) {
         return !isPayer() && (Math.abs(this.contribution) >= Math.abs(contribution));
     }
 
     public Participant getParticipant() {
         return participant;
     }
 }
