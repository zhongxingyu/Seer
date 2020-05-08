package chord.project.analyses.slicer;
 
 public interface ISummaryEdge {
     public boolean matchesSrcNodeOf(ISummaryEdge pe);
     public boolean mergeWith(ISummaryEdge pe);
 }
