 package ttp.constructionheuristics;
 
 import ttp.model.TTPInstance;
 import ttp.model.TTPSolution;
 import ttp.util.TtpSolutionHelper;
 
 public class SimpleConstruction implements
 		IConstructionHeuristics<TTPInstance, TTPSolution> {
 
 	private int noTeams;
 	private TTPInstance problemInstance;
 	private int upperBound = 3;
 
 	public SimpleConstruction() {
 	}
 
 	public SimpleConstruction(int noTeams) {
 		this.noTeams = noTeams;
 	}
 
 	private int[][] createSchedule() {
 
 		int u = upperBound;
 		int noT = noTeams;
 		int days = (noTeams - 1) * 2;
 		if (problemInstance != null) {
 			u = problemInstance.getU();
 			noT = problemInstance.getNoTeams();
 			days = problemInstance.getNoRounds();
 		}
 		// neu
 		int[][] schedule = new int[days][noT];
 		// ende neu
 		int half = days / 2;
 
 		int n = noT;
 		int[] position = new int[n];
 
 		for (int i = 0; i < n; i++) {
 			position[i] = i + 1;
 		}
 
 		for (int k = 0; k <= (n - 2); k++) {
 			for (int l = 2; l <= (n / 2); l++) {
 				// l plays agains n +1 - l
 				int oponent = position[n + 1 - l - 1];
 
 				schedule[k][position[l - 1] - 1] = oponent;
 				schedule[k][oponent - 1] = -(position[l - 1]);
 
 				schedule[k + half][position[l - 1] - 1] = -(oponent);
 				schedule[k + half][oponent - 1] = (position[l - 1]);
 			}
 
 			// 1 plays against 6
 			schedule[k][position[0] - 1] = position[n - 1];
 			schedule[k][position[n - 1] - 1] = -position[0];
 
 			schedule[k + half][position[0] - 1] = -(position[n - 1]);
 			schedule[k + half][position[n - 1] - 1] = position[0];
 
 			// shift teams
 
 			for (int i = 0; i < (n - 1); i++) {
				position[i] = (position[i] - 1) % 6;
 				if (position[i] == 0)
 					position[i] = n - 1;
 			}
 
 		}
 
 		return schedule;
 
 		/*
 		 * int counter = 0; boolean home = true; // int[][] schedule = new
 		 * int[days][noT];
		 * 
 		 * for (int i = 0; i < (noT - 1); i++) { boolean[] attached = new
 		 * boolean[noT]; home = true; counter = 0; for (int j = 0; j < half;
 		 * j++) { if (schedule[j][i] == 0) {
		 * 
 		 * // find free team int unassignedTeam = 0; for (int k = i + 1; k <
 		 * noT; k++) { if (schedule[j][k] == 0 && !attached[k]) { unassignedTeam
 		 * = k; attached[k] = true; break; } }
		 * 
 		 * // set home or away if (home) { schedule[j][i] = unassignedTeam + 1;
 		 * schedule[j][unassignedTeam] = -(i + 1);
		 * 
 		 * schedule[j + half][i] = -(unassignedTeam + 1); schedule[j +
 		 * half][unassignedTeam] = (i + 1); } else { schedule[j][i] =
 		 * -(unassignedTeam + 1); schedule[j][unassignedTeam] = (i + 1);
		 * 
 		 * schedule[j + half][i] = (unassignedTeam + 1); schedule[j +
 		 * half][unassignedTeam] = -(i + 1); }
		 * 
 		 * counter++;
		 * 
 		 * if (counter >= u) { home = !home; counter = 0; } } } }
		 * 
 		 * return schedule;
 		 */
 	}
 
 	@Override
 	public TTPSolution getInitialSolution() {
 		TTPSolution sol = new TTPSolution();
 
 		sol.setSchedule(createSchedule());
 		if (problemInstance != null)
 			sol.setProblemInstance(problemInstance);
 		else {
 			TTPInstance inst = new TTPInstance();
 			inst.setNoTeams(noTeams);
 			inst.setL(1);
 			inst.setU(3);
 			inst.setD(new int[(noTeams - 1) * 2][noTeams]);
 
 			sol.setProblemInstance(inst);
 		}
 
 		TtpSolutionHelper.initializeSolution(sol, sol.getProblemInstance());
 
 		return sol;
 	}
 
 	public int getNoTeams() {
 		return noTeams;
 	}
 
 	public void setNoTeams(int noTeams) {
 		this.noTeams = noTeams;
 	}
 
 	@Override
 	public void setProblemInstance(TTPInstance instance) {
 		this.problemInstance = instance;
 	}
 
 }
