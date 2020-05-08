package chord.project.analyses.rhs;
 
 public interface ISummaryEdge {
     public boolean matchesSrcNodeOf(ISummaryEdge pe);
     public boolean mergeWith(ISummaryEdge pe);
 }
