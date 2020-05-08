package chord.project.analyses.rhs;
 
 public interface IPathEdge {
 	public boolean matchesSrcNodeOf(IPathEdge pe);
 	public boolean mergeWith(IPathEdge pe);
 }
 
