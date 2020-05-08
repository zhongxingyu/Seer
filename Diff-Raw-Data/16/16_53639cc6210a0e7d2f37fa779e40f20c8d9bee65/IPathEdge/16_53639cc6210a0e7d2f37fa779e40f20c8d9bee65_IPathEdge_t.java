package chord.project.analyses.slicer;
 
 public interface IPathEdge {
 	public boolean matchesSrcNodeOf(IPathEdge pe);
 	public boolean mergeWith(IPathEdge pe);
 }
 
