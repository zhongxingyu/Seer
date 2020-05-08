 package rsmith.fortune;
 
 import java.awt.geom.Point2D;
 import java.util.Iterator;
 import java.util.NavigableSet;
 import java.util.Set;
 
 import rsmith.fortune.event.CircleEvent;
 import rsmith.fortune.event.SiteEvent;
 import rsmith.fortune.event.SweepEvent;
 import rsmith.fortune.point.*;
 
 public class FortuneAlgorithm {
 	private FortuneData fortuneData = null;
 
 	public FortuneAlgorithm(Set<Point2D> points) {
 		fortuneData = new FortuneData(points);
 	}
 
 	public void init() {
 		fortuneData.init();
 		Iterator<Point2D> iter = getFortuneData().getPoints().iterator();
 		while (iter.hasNext()) {
 			Point2D p = (Point2D) iter.next();
 			fortuneData.insertSitePoint(p);
 		}
 	}
 
 	public void step() {
 		if (!isFinished()) {
 			// System.out.println("eventq="+getFortuneData().getEventQueue());
 			 System.out.println("beachline="+getFortuneData().getBeachline());
 			SweepEvent e = (SweepEvent) getFortuneData().getEventQueue()
 					.remove();
 			getFortuneData().setSweepY(e.getEventY());
 			if (e instanceof SiteEvent) {
 				handleSiteEvent((SiteEvent) e);
 			} else {
 				handleCircleEvent((CircleEvent) e);
 			}
 		}
 	}
 
 	public boolean isFinished() {
 		return getFortuneData().getEventQueue().isEmpty();
 	}
 	
 	private static int vernum=0;
 	public  void verifyBeachline() {
 		vernum++;
 		System.out.println("verifyBeachline:vernum="+vernum);
 		FortuneData data = getFortuneData();
 		Iterator<VoronoiNode> iter =data.getBeachline().iterator();
 		double sweepY = data.getSweepY();
 		VoronoiNode last = null;
 		VoronoiNode current = null;
 		System.out.print("[");
 		while(iter.hasNext()) {
 			current = iter.next();
 			if(current.getPoint() instanceof BreakPoint) {
 				BreakPoint bp = (BreakPoint)current.getPoint();
 				BreakPoint bpn = bp.getNext();
 				if(bpn != null && bpn.getPosition().getX() < bp.getPosition().getX()) {
 					System.out.println("Found breakpoints out of order (bp,bpn):"+bp.getPosition()+","+bpn.getPosition());
 				}
 				if(bpn != null && bp.getRight() != bpn.getLeft()) {
 					System.out.println("bp.getRight() == bpn.getLeft() should hold at all times");
 				}
 				BreakPoint bpp = bp.getPrevious();
 				if(bpp != null && bpp.getPosition().getX() > bp.getPosition().getX()) {
 					System.out.println("Found breakpoints out of order (bp,bpp)"+ bp.getPosition() + "," + bpp.getPosition());
 				}
 			} else {
 				if(data.getBeachline().size() > 1) {
 					System.out.println("Found a site point when should not have.");
 				}
 			}
 			
 		}
 		System.out.println("]");
 	}
 	
 	public void handleSiteEvent(SiteEvent se) {
 		System.out.println("handleSiteEvent:y="+fortuneData.getSweepY());
 		NavigableSet<VoronoiNode> beachline = fortuneData.getBeachline();
 
 		if (beachline.isEmpty()) {
 			beachline.add(new VoronoiNode(se.getSite(), null, this
 					.getFortuneData()));
 			return;
 		}
 		SitePoint p = se.getSite();
 		BreakPoint bl = null;
 		BreakPoint br = null;
 		VoronoiNode vp = new VoronoiNode(p, null, this.getFortuneData());
 		SitePoint q = null;
 		// find arc above the site p
 		if (getFortuneData().getBeachline().size() == 1) {
 			// if there is only one element in the tree
 			// then just remove it and get its point q
 			VoronoiNode vn = fortuneData.getBeachline().first();
 			fortuneData.getBeachline().remove(vn);
 			q = (SitePoint) vn.getPoint();
 		} else {
 			// if there is more than one element then
 			// get the breakpoints bl and br that
 			// lie immediately to the left and right of p
 			VoronoiNode before = beachline.floor(vp); // this gets the nodes
 			VoronoiNode after = beachline.ceiling(vp);
 			// get the corresponding breakpoints
 			bl = (before != null ? (BreakPoint) before.getPoint() : null);
 			br = (after != null ? (BreakPoint) after.getPoint() : null);
 			assert (!(bl == null && br == null));
 			if(bl != null && br != null && bl.getRight() != br.getLeft()) {
 				System.out.println("bl.getRight() == br.getLeft()  should hold");
 			} else
 			if(bl != null && br != null && bl.getRight() == br.getLeft()) {
 				System.out.println("bl.getRight() == br.getLeft() holds.");
 			}
 			// if bl is null, then use br
 			if (bl == null && br != null) {
 				q = br.getLeft();
 			} else {
 				// if we get here we know that it is not the case that (bl ==
 				// null && br == null)
 				// is true, so bl cannot be null here since that means size=1
 				// and we know that size>1
 				// at this point, so there must be at least one breakpoint, or
 				// at least it should not be the case
 				// furthermore, if bl is not null, then either br is null or it
 				// isn't.
 				// if br is null then we just use bl.right, otherwise
 				// we still use bl.right so in either case, b
 				q = bl.getRight();
 			}
 		}
 
 		// we should have q at this point
 		assert (q != null);
 		if(q==null) {
 			System.out.println("q was null");
 		}
 
 		// create the two new breakpoints that will intersect this arc
 		BreakPoint b1 = new BreakPoint();
 		BreakPoint b2 = new BreakPoint();
 
 		VoronoiNode vb1 = fortuneData.insertBreakPoint(b1, bl, b2, q, p);
 		VoronoiNode vb2 = fortuneData.insertBreakPoint(b2, b1, br, p, q);
 		 verifyBeachline();
 		 
 		fixCircleEvent(bl, br);
 
 		if (beachline.size() > 2) {
 			fortuneData.insertCircleEvents(vb1, vb2);
 		}
 		 verifyBeachline();
 		 
 	}
 
 	private void fixCircleEvent(BreakPoint left, BreakPoint right) {
 		if (left != null && right != null) {
 			VoronoiNode node = left.getNode();
 			CircleEvent event = node.getCircleEvent();
 			node.setCircleEvent(null);
 			fortuneData.getEventQueue().remove(event);
 		}
 	}
 
 	public void handleCircleEvent(CircleEvent ce) {
		System.out.println("handleCircleEvent:y="+fortuneData.getSweepY()+"-----------------");
 		// the breakpoints representing the disappearing arc
 		BreakPoint leftBP = ce.getLeftBP();
 		BreakPoint rightBP = ce.getRightBP();
		System.out.println("leftBP.id="+leftBP.getID()+",rightBP.id="+rightBP.getID());
 
 		// the breakpoints that lie to the left and right of this arc
 		BreakPoint previous = leftBP.getPrevious();
 		BreakPoint next = rightBP.getNext();
		System.out.println("previous.id="+previous.getID()+",next.id="+next.getID());
 		// the sites that generate the arcs lying to the left and right
 		// of the disappearing arc
 		SitePoint left = leftBP.getLeft();
 		SitePoint right = rightBP.getRight();
 
 		// effectively remove this arc
 		fortuneData.removeBreakPoint(leftBP);
 		fortuneData.removeBreakPoint(rightBP);
 
		// check to see if the current arc has any other current circle events and remove those
 		fortuneData.clearCircleEvent(previous);
 		fortuneData.clearCircleEvent(rightBP);
 
 		BreakPoint b = new BreakPoint();
 		fortuneData.insertBreakPoint(b, previous, next, left, right);
 
 		assert (b.getPrevious() == previous && b.getNext() == next);
 
 		// insert the two new circle events
 		fortuneData.insertCircleEvent(previous, b);
 		fortuneData.insertCircleEvent(b, next);
		System.out.println("-------------------------------");
 	}
 
 	/**
 	 * @return the fortuneData
 	 */
 	public FortuneData getFortuneData() {
 		return fortuneData;
 	}
 
 	/**
 	 * @param fortuneData
 	 *            the fortuneData to set
 	 */
 	public void setFortuneData(FortuneData fortuneData) {
 		this.fortuneData = fortuneData;
 	}
 
 }
