 package org.eclipse.dltk.javascript.parser;
 
 import org.eclipse.dltk.compiler.problem.DefaultProblem;
 import org.eclipse.dltk.compiler.problem.IProblem;
 import org.eclipse.dltk.compiler.problem.IProblemReporter;
 import org.eclipse.dltk.compiler.problem.ProblemSeverities;
 import org.eclipse.dltk.core.builder.ISourceLineTracker;
 
 public class Reporter extends LineTracker implements IProblemReporter {
 
 	public enum Severity {
 		WARNING, ERROR
 	}
 
 	private final IProblemReporter problemReporter;
 
 	private int id;
 	private String message;
 	private int line;
 	private int start;
 	private int end;
 	private Severity severity;
 
 	public Reporter(ISourceLineTracker lineTracker,
 			IProblemReporter problemReporter) {
 		super(lineTracker);
 		this.problemReporter = problemReporter;
 		reset();
 	}
 
 	private void reset() {
 		severity = Severity.WARNING;
 		id = 0;
 		message = null;
 		line = -1;
 		start = -1;
 		end = -1;
 	}
 
 	public void report() {
 		if (problemReporter != null && message != null) {
			problemReporter.reportProblem(createProblem());
 		}
 		reset();
 	}
 
 	private IProblem createProblem() {
 		if (line == -1 && start != -1) {
 			line = getLineNumberOfOffset(start);
 		}
 		if (line > getNumberOfLines() && start >= 0 && start <= getLength()) {
 			line = getLineNumberOfOffset(start);
 		}
 		return new DefaultProblem(message, id, null,
 				severity == Severity.ERROR ? ProblemSeverities.Error
 						: ProblemSeverities.Warning, start, end, line);
 	}
 
 	public void setMessage(int id, String message) {
 		this.id = id;
 		this.message = message;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public String getMessage() {
 		return message;
 	}
 
 	public void setMessage(String message) {
 		this.message = message;
 	}
 
 	public int getLine() {
 		return line;
 	}
 
 	public void setLine(int line) {
 		this.line = line;
 	}
 
 	public int getStart() {
 		return start;
 	}
 
 	public void setStart(int start) {
 		this.start = start;
 	}
 
 	public int getEnd() {
 		return end;
 	}
 
 	public void setEnd(int end) {
 		this.end = end;
 	}
 
 	public Severity getSeverity() {
 		return severity;
 	}
 
 	public void setSeverity(Severity severity) {
 		this.severity = severity;
 	}
 
 	public void setRange(int start, int end) {
 		this.start = start;
 		this.end = end;
 	}
 
 	public void reportProblem(IProblem problem) {
 		if (problemReporter != null) {
 			problemReporter.reportProblem(problem);
 		}
 	}
 
 	public Object getAdapter(Class adapter) {
 		return null;
 	}
 
 	public void reportProblem(int id, String message, int start, int end) {
 		reportProblem(new DefaultProblem(message, id, null,
 				ProblemSeverities.Warning, start, end,
 				getLineNumberOfOffset(start)));
 	}
 
 }
