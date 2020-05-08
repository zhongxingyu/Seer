 package com.app.settleexpenses.domain;
 
 import android.util.Log;
 
 import java.util.*;
 
 public class Event {
     private int id;
     private String title;
     private List<Expense> expenses;
 
     public Event(int id, String title, List<Expense> expenses) {
         this.id = id;
         this.title = title;
         this.expenses = expenses;
     }
 
     public List<Participant> getParticipants() {
         Set<Participant> participants = new HashSet<Participant>();
         for (Expense expense : expenses) {
             participants.add(expense.getPaidBy());
             participants.addAll(expense.getParticipants());
         }
         return new ArrayList<Participant>(participants);
     }
 
     public List<Settlement> calculateSettlements() {
         ArrayList<Settlement> settlements = new ArrayList<Settlement>();
         ArrayList<ParticipantContribution> participantContributions = calculateParticipantContribution();
         for (ParticipantContribution participantContribution : participantContributions) {
             if (participantContribution.isPayer()) {
                 Log.d("Event : ", participantContribution.getParticipant().getId() + " should pay $" + participantContribution.getContribution());
                 settlements.addAll(payToOneOf(participantContributions, participantContribution));
             } else {
                 Log.d("Event : ", participantContribution.getParticipant().getId() + " should receive $" + participantContribution.getContribution());
             }
         }
         return settlements;
     }
 
     private ArrayList<Settlement> payToOneOf(ArrayList<ParticipantContribution> participantContributions, ParticipantContribution payer) {
         ArrayList<Settlement> settlements = new ArrayList<Settlement>();
         for (ParticipantContribution participantContribution : participantContributions) {
            if (!participantContribution.isPayer()) {
                 float absoluteSettlementAmount = Math.abs(payer.getContribution());
 
                 if (!participantContribution.canAcceptFullAmount(payer.getContribution())) {
                     absoluteSettlementAmount = Math.abs(payer.getContribution()) - Math.abs(participantContribution.getContribution());
                 }
 
                 float settlementAmount = absoluteSettlementAmount * -1;
                 Log.d("", "Absolute settlement amount : " + settlementAmount);
                 participantContribution.addContribution(settlementAmount);
                 payer.addContribution(absoluteSettlementAmount);
                 Log.d("Event : ", participantContribution.getParticipant().getId() + " received a sum of $" + absoluteSettlementAmount);
                 settlements.add(new Settlement(payer.getParticipant(), participantContribution.getParticipant(), absoluteSettlementAmount));
                 if (payer.getContribution() == 0) return settlements;
             }
         }
         return settlements;
     }
 
     private ArrayList<ParticipantContribution> calculateParticipantContribution() {
         HashMap<Participant, ParticipantContribution> participantContributions = new HashMap<Participant, ParticipantContribution>();
         for (Expense expense : expenses) {
             Log.d("", "Expense has these participants : " + expense.getParticipants().size());
             for (Participant participant : expense.getParticipants()) {
                 ParticipantContribution participantContribution = participantContributions.get(participant);
                 if (participantContribution == null) {
                     participantContributions.put(participant, new ParticipantContribution(participant, 0));
                 }
 
                 participantContributions.get(participant).addContribution(expense.contributionAmount(participant));
             }
         }
         Log.d("", "Total expenses : " + expenses.size());
         Log.d("", "Individual contributions : " + participantContributions.values().size());
         for(ParticipantContribution con : participantContributions.values()) {
             Log.d("", con.getParticipant().getId() + " holds " + con.getContribution());
         }
         return new ArrayList<ParticipantContribution>(participantContributions.values());
     }
 }
