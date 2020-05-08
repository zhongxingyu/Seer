 package br.ufrj.jfirn.intelligent.evaluation;
 
 public enum Reason {
 
 	PROBABILITY_EVALUATION("Did probability evaluation."),
 	PARTIAL_EVALUATION    ("Did partial evaluation."),
	NO_INTERSECTION       ("Trajectories doesn't intersect."),
 	TOO_FAR_AWAY          ("Obstacle is too far away."),
 	TOO_CLOSE             ("Obstacle is too close."),
 	NOT_ENOUGH_SAMPLES    ("Unsuficient samples"),
 	NO_REASON             ("Unknown reason");
 
 	private final String message;
 
 	private Reason(String message) {
 		this.message = message;
 	}
 
 	@Override
 	public String toString() {
 		return this.message;
 	}
 
 }
