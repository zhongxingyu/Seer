 package com.operativus.senacrs.audit.model;
 
 import java.util.HashMap;
 import java.util.Map;
 
import com.operativus.senacrs.audit.exceptions.InvalidEvaluationType;
 import com.operativus.senacrs.audit.exceptions.MismatchingEvaluationType;
 
 public class StudentEvaluation
 		extends
 		AbstractSequenceStringFieldComparable {
 
 	private final String name;
 	private final Map<EvaluationActivity, EvaluationGrade> grades = new HashMap<EvaluationActivity, EvaluationGrade>();
 	private EvaluationGrade finalGrade = null;
 
 	public StudentEvaluation(final int sequence, final String name) {
 
 		super(sequence);
 		this.name = name;
 	}
 
 	public String getName() {
 
 		return this.name;
 	}
 
 	public Map<EvaluationActivity, EvaluationGrade> getGrades() {
 
 		return this.grades;
 	}
 
 	public EvaluationGrade putGrade(final EvaluationActivity activity, final EvaluationGrade grade) {
 
		
		if (activity == null) {
			throw new IllegalArgumentException();
		}
		if (grade == null) {
			throw new IllegalArgumentException();
		}
		if (EvaluationType.INVALID.equals(activity.getType())) {
			throw new InvalidEvaluationType();
		}
		if (EvaluationType.INVALID.equals(grade.getType())) {
			throw new InvalidEvaluationType();
		}
 		if (activity.getType() != grade.getType()) {
 			throw new MismatchingEvaluationType(activity, grade);
 		}
 
 		return this.grades.put(activity, grade);
 	}
 
 	public EvaluationGrade getFinalGrade() {
 
 		return this.finalGrade;
 	}
 
 	public void setFinalGrade(final EvaluationGrade finalGrade) {
 
 		this.finalGrade = finalGrade;
 	}
 
 	@Override
 	protected String getComparisonStringField() {
 
 		return this.getName();
 	}
 }
