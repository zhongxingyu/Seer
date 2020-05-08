 package model;
 import java.util.*;
 
 public class Tournament{
 		private List<Participant> participants;
 		private boolean started = false;
 		private int winningNumber = 0;
 
 	public Tournament() {
 		
 	}
 	
 	public void addParticipant(Participant p) {
 		if (started == false){
 			participants.add(p);		
 		} else {
 			System.out.println("you can't add more after start");
 		}			
 	}
 
 	public Round start() {
 		started = true;
 		Round firstRound = new Round(participants);
 		return firstRound;	
 	}
 	
 	public Round startRound() {
 		winningNumber++;
		 List<Participant> p = new ArrayList<Participant>();
 			for(Participant participant : participants) {
 				if (participant.score() == winningNumber) {
 					p.add(participant);
 				}
 			}	
 
 		//skal iterere gennem listen og se hvem der har flest point.. 
 		//1 første gang..
 		//2 anden gang og fremdelse
 		Round nextRound = new Round(participants);
 		return nextRound;
 	}
 
 }	
