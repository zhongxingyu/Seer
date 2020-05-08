 package ttp.model;
 
 import ttp.util.TtpSolutionHelper;
 
 public class TTPSolution {
 
 	// [round][team]
 	private int[][] schedule;
 
 	private int[] teamCost;
 	private int[] softConstraintsViolations;
 
 	private int cost;
 
 	private boolean legal;
 
 	private TTPInstance problemInstance;
 
 	private double penalty;
 
 	private int scTotal = 0;
 
 	public TTPSolution() {
 	}
 
 	public TTPSolution(TTPSolution baseSolution) {
 		this.cost = baseSolution.cost;
 		this.legal = baseSolution.legal;
 		this.penalty = baseSolution.penalty;
 		this.problemInstance = baseSolution.problemInstance;
 		this.scTotal = baseSolution.scTotal;
 		this.schedule = TtpSolutionHelper.copyArray(baseSolution.schedule);
 		this.teamCost = TtpSolutionHelper.copyArray(baseSolution.teamCost);
 
 		this.softConstraintsViolations = TtpSolutionHelper
 				.copyArray(baseSolution.softConstraintsViolations);
 
 	}
 
 	public int getCost() {
 		return cost;
 	}
 
 	public double getCostWithPenalty() {
 		return cost + penalty;
 	}
 
 	public double getPenalty() {
 		return penalty;
 	}
 
 	public TTPInstance getProblemInstance() {
 		return problemInstance;
 	}
 
 	public int[][] getSchedule() {
 		return schedule;
 	}
 
 	public int getScTotal() {
 		return scTotal;
 	}
 
 	public int[] getSoftConstraintsViolations() {
 		return softConstraintsViolations;
 	}
 
 	public int[] getTeamCost() {
 		return teamCost;
 	}
 
 	public boolean isLegal() {
 		return legal;
 	}
 
 	public void setCost(int cost) {
 		this.cost = cost;
 	}
 
 	public void setLegal(boolean legal) {
 		this.legal = legal;
 	}
 
 	public void setPenalty(double penalty) {
 		this.penalty = penalty;
 	}
 
 	public void setProblemInstance(TTPInstance problemInstance) {
 		this.problemInstance = problemInstance;
 	}
 
 	public void setSchedule(int[][] schedule) {
 		this.schedule = schedule;
 	}
 
 	public void setScTotal(int scTotal) {
 		this.scTotal = scTotal;
 	}
 
 	public void setSoftConstraintsViolations(int[] softConstraintsViolations) {
 		this.softConstraintsViolations = softConstraintsViolations;
 
 		scTotal = 0;
 		for (int i = 0; i < softConstraintsViolations.length; i++) {
 			scTotal += softConstraintsViolations[i];
 		}
 
 		if (scTotal == 0) {
 			this.legal = true;
 		}
 	}
 
 	public void setTeamCost(int[] teamCost) {
 		this.teamCost = teamCost;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("Round/Games\n");
 		for (int round = 0; round < schedule.length; round++) {
 			sb.append(round);
 			sb.append("|\t");
 			for (int team = 0; team < schedule[0].length; team++) {
				if (schedule[round][team] >= 0) {
 					sb.append(" ");
 					sb.append(schedule[round][team]);
 				} else {
 					sb.append(schedule[round][team]);
 				}
 
 				sb.append(" ");
 			}
 			sb.append("\n");
 		}
 		sb.append("\n");
 		sb.append("Costs:");
 		sb.append(cost);
 
 		return sb.toString();
 	}
 
 	public void updateSoftConstraintsViolations(int i, int value) {
 		scTotal -= softConstraintsViolations[i];
 		softConstraintsViolations[i] = value;
 		scTotal += value;
 
 		if (scTotal == 0) {
 			this.legal = true;
 		} else {
 			this.legal = false;
 		}
 
 	}
 }
