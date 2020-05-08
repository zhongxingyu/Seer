 package de.hswt.hrm.report.latex;
 
import de.hswt.hrm.inspection.model.Inspection;

 public class OverallEvaluationParser {
 
    public String parse(Inspection inspection){
        return inspection.getSummary().toString();
    }
 
 }
