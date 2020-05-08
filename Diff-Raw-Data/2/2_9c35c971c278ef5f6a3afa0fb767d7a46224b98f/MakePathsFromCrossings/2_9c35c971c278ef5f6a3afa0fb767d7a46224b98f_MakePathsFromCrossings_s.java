 package nogbeter.crossing;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import nogbeter.crossing.util.BidirectionalOrdering;
 import nogbeter.crossing.util.ForwardSegmentMaker;
 import nogbeter.crossing.util.IMap;
 import nogbeter.crossing.util.ISegmentMaker;
 import nogbeter.crossing.util.ISimpleList;
 import nogbeter.crossing.util.ReverseSegmentMaker;
 import nogbeter.crossing.util.SimpleListView;
 import nogbeter.graphtheory.ClosedPathsToShapes;
 import nogbeter.paths.Path;
 import nogbeter.paths.PathIndex;
 import nogbeter.paths.compound.ClosedPath;
 import nogbeter.paths.factory.PathFactory;
 
 public class MakePathsFromCrossings <L extends PathIndex,R extends PathIndex>{
 
 	private static final int NoLooseEnd = -1;
 	private final BidirectionalOrdering<Crossing<L,R>> bidir;
 	private final ISimpleList<Crossing<L,R>> left;
 	private final ISegmentMaker<L> leftSegs;
 	private final boolean[] leftUsed;
 	private final ISegmentMaker<R> rightSegs;
 	private final boolean leftInside;
 	private ISimpleList<Crossing<L, R>> right;
 	private final Path<L> l;
 	private final Path<R> r;
 	private final boolean rightInside;
 	private final boolean reverseRight;
 	
 	public MakePathsFromCrossings(Path<L> l, Path<R> r, boolean leftInside, boolean rightInside,
 			boolean reverseRight, List<Crossing<L, R>> crossingsSortedOnLeft) {
 		this.l = l;
 		this.r = r;
 		bidir = makeBidir(crossingsSortedOnLeft);
 		left = bidir.getLeft();
 		leftUsed = new boolean[bidir.size()];
 		leftSegs = makeSegmentMaker(false,l, getLeftList(left));
 		right = bidir.getRight();
 		rightSegs = makeSegmentMaker(reverseRight,r, getRightList(right));
 		this.leftInside = leftInside;
 		this.rightInside = rightInside;
 		this.reverseRight = reverseRight;
 	}
 	
 	public Path makeAllPaths(){
 		List<ClosedPath> res = makePathsFromCrossings();
 		res.addAll(makePathsFromNonIntersections());
 		return ClosedPathsToShapes.closedPathsToShapes(res);
 	}
 	
 	List<ClosedPath> makePathsFromCrossings(){
 		List<ClosedPath> res = new ArrayList<ClosedPath>();
 		int beginIndex;
 		while((beginIndex = findLooseEnd()) != NoLooseEnd){
 			res.add(makeClosedPath(beginIndex));
 		}
 		return res;
 	}
 
 	List<ClosedPath> makePathsFromNonIntersections(){
 		List<ClosedPath> leftNon = getNonIntersectionSegments(l, getLeftList(left));
 		leftNon = filter(leftInside, leftNon, r);
 		List<ClosedPath> rightNon = getNonIntersectionSegments(r, getRightList(right));
 		rightNon = filter(rightInside, rightNon, l);
 		if(reverseRight){
 			rightNon = ReverseSegmentMaker.reverseAll(rightNon);
 		}
 		leftNon.addAll(rightNon);
 		return leftNon;
 	}
 
 
 	private List<ClosedPath> filter(boolean shouldBeInside,
 			List<ClosedPath> leftNon, Path other) {
 		List<ClosedPath> result = new ArrayList<ClosedPath>();
 		for(ClosedPath p : leftNon){
 			if(other.contains(p) == shouldBeInside){
 				result.add(p);
 			}
 		}
 		return result;
 	}
 
 	private ClosedPath makeClosedPath(int beginIndex) {
 		List<Path> res = new ArrayList<Path>();
 		do{
 			leftUsed[beginIndex] = true;
 			leftSegs.getSegment(res, beginIndex);
 			beginIndex = bidir.fromLeftToRight(leftSegs.getEndIndex(beginIndex));
 			rightSegs.getSegment(res, beginIndex);
 			beginIndex = bidir.fromRightToLeft(rightSegs.getEndIndex(beginIndex));
 		} while(!leftUsed[beginIndex]);
 		return PathFactory.createClosedPath(res);
 	}
 
 	int findLooseEnd(){
 		for(int i = 0 ; i < leftUsed.length ; i++){
 			if(!leftUsed[i] && 
 					left.get(i).leftAfterInside == leftInside){
 				return i;
 			}
 		}
 		return NoLooseEnd;
 	}
 	
 	
 	private <P extends PathIndex> ISegmentMaker<P> makeSegmentMaker(boolean reverse,
 			Path p, ISimpleList<P> list) {
 		List<Integer> ends = getEnds(p,list);
 		if(reverse){
 			return new ReverseSegmentMaker<P>(p,list,ends);
 		} else {
 			return new ForwardSegmentMaker<P>(p, list, ends);
 		}
 	}
 	
 	private <P extends PathIndex> List<ClosedPath> getNonIntersectionSegments(
 			Path<P> p, ISimpleList<P> list) {
 		Set<Path> withIntersections = new HashSet<Path>();
 		for(int i = 0 ; i < list.size() ; i++){
 			withIntersections.add(p.getSegment(list.get(i)));
 		}
 		List<ClosedPath> res = new ArrayList<ClosedPath>();
 		p.getClosedSegmentsNotInSet(withIntersections, res);
 		return res;
 	}
 	
 	private <P extends PathIndex> List<Integer> getEnds(Path p,ISimpleList<P> list){
 		List<Integer> res = new ArrayList<Integer>();
 		if(list.size() == 0){
 			return res;
 		}
 		Path prev = p.getSegment(list.get(0));
 		for(int i = 1 ; i < list.size() ; i++){
 			Path cur = p.getSegment(list.get(i));
 			if(prev != cur){
 				res.add(i);
 				prev = cur;
 			}
 		}
		res.add(list.size()-1);
 		return res;
 	}
 
 	private SimpleListView<Crossing<L, R>, L> getLeftList(
 			ISimpleList<Crossing<L, R>> l) {
 		return new SimpleListView<Crossing<L,R>, L>(
 				new IMap<Crossing<L,R>, L>() {
 
 					@Override
 					public L apply(Crossing<L,R> a) {
 						return a.l;
 					}
 				}
 				, l);
 	}
 	
 	private SimpleListView<Crossing<L, R>, R> getRightList(
 			ISimpleList<Crossing<L, R>> l) {
 		return new SimpleListView<Crossing<L,R>, R>(
 				new IMap<Crossing<L,R>, R>() {
 
 					@Override
 					public R apply(Crossing<L,R> a) {
 						return a.r;
 					}
 				}
 				, l);
 	}
 
 	private BidirectionalOrdering<Crossing<L, R>> makeBidir(
 			List<Crossing<L, R>> crossingsSortedOnLeft) {
 		return new BidirectionalOrdering<Crossing<L,R>>(crossingsSortedOnLeft, 
 				new Comparator<Crossing<L,R>>() {
 					@Override
 					public int compare(Crossing<L, R> o1, Crossing<L, R> o2) {
 						return o1.r.compareTo(o2.r);
 					}
 		});
 	}
 }
