 package com.redcareditor.mate;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import com.redcareditor.onig.Match;
 import com.redcareditor.onig.Rx;
 
 public class Scanner implements Iterable<Marker> {
 	private Scope currentScope;
 	public int position;
 	public String line;
 	public int lineLength;
 	public ArrayList<Marker> cachedMarkers;
 	
 	public void setCurrentScope(Scope scope) {
 		this.currentScope = scope;
 		cachedMarkers.clear();
 	}
 	
 	public Scope getCurrentScope() {
 		return this.currentScope;
 	}
 	
 	public Scanner(Scope startScope, String line) {
 		this.currentScope = startScope;
 		this.line = line;
		this.lineLength = line.length();
 		this.position = 0;
 		this.cachedMarkers = new ArrayList<Marker>();
 	}
 
 	// if we have already scanned this line for this scope then
 	// simply return the next cached marker (choosing the longest
 	// match in case of a tie).
 	public Marker getCachedMarker() {
 		Marker m = null;
 		int bestLength = 0;
 		int newLength;
 		for (Marker m1 : cachedMarkers) {
 			newLength = m1.match.getCapture(0).end - m1.from;
 			if (m == null || 
 				m1.from < m.from || 
 				(m1.from == m.from && newLength > bestLength) ||
 				(m1.from == m.from && newLength == bestLength && m1.isCloseScope)
 				) {
 				m = m1;
 				bestLength = newLength;
 			}
 		}
 		return m;
 	}
 	
 	public void removePrecedingCachedMarkers(Marker m) {
 		int ix = 0;
 		int len = cachedMarkers.size();
 		//stdout.printf("num cached: %d\n", len);
 		for(int i = 0; i < len; i++, ix++) {
 			if (cachedMarkers.get(ix).from < m.match.getCapture(0).end) {
 				cachedMarkers.remove(ix);
 				ix--;
 			}
 		}
 		//stdout.printf("num cached after removals: %d\n", cached_markers.size);
 	}
 	
 	public Match scanForMatch(int from, Pattern p) {
 		if (p.name != null && (p.name.startsWith("#") || p.name.startsWith("$")))
 			System.out.printf("*** WARNING trying to scan for pattern called %s\n", p.name);
 		
 		Match match = null;
 		if (p instanceof SinglePattern) {
 			SinglePattern sp = (SinglePattern) p;
 			match = sp.match.search(line, from, this.lineLength);
 		}
 		else if (p instanceof DoublePattern) {
 			match = ((DoublePattern) p).begin.search(this.line, from, this.lineLength);
 		}
 		return match;
 	}
 	
 	public Marker findNextMarker() {
 		System.out.printf("find_next_marker from (current_scope is %s)\n", currentScope.name);
 		System.out.printf("scanning: '%s' from %d to %d\n", this.line, this.position, this.lineLength);
 		Marker m;
 		int bestLength = 0;
 		int newLength;
 		boolean isCloseMatch = false;
 		if ((m = getCachedMarker()) != null) {
 			System.out.printf("got cached marker\n");
 			this.cachedMarkers.remove(m);
 			removePrecedingCachedMarkers(m);
 			return m;
 		}
 		System.out.printf("no cached marker\n");
 		assert(cachedMarkers.size() == 0);
 		Rx closingRegex = currentScope.closingRegex;
 		if (closingRegex != null) {
 			Match match = closingRegex.search(this.line, this.position, this.lineLength);
 			if (match != null) {
 				System.out.printf("closing match: %s (%d-%d)\n", this.currentScope.name, match.getCapture(0).start, match.getCapture(0).end);
 				Marker newMarker = new Marker();
 				newMarker.pattern = this.currentScope.pattern;
 				newMarker.match = match;
 				newMarker.from = match.getCapture(0).start;
 				newMarker.isCloseScope = true;
 				this.cachedMarkers.add(newMarker);
 				m = newMarker;
 				bestLength = newMarker.match.getCapture(0).end - newMarker.from;
 				isCloseMatch = true;
 			}
 		}
 		System.out.printf("scanning for %d patterns\n", ((DoublePattern) currentScope.pattern).patterns.size());
 		for (Pattern p : ((DoublePattern) currentScope.pattern).patterns) {
 //			System.out.printf("     scanning for %s\n", p.name);
 			if (p.disabled)
 				continue;
 			int positionNow = position;
 			int positionPrev = position-1;
 			Match match;
 			while ((match = scanForMatch(positionNow, p)) != null &&
 				   positionNow != positionPrev // some regex's have zero width (meta.selector.css)
 				) {
 				positionPrev = positionNow;
 				System.out.printf("matched: %s (%d-%d)\n", p.name, match.getCapture(0).start, match.getCapture(0).end);
 				Marker newMarker = new Marker();
 				newMarker.pattern = p;
 				newMarker.match = match;
 				newMarker.from = match.getCapture(0).start;
 				newMarker.isCloseScope = false;
 				this.cachedMarkers.add(newMarker);
 				newLength = newMarker.match.getCapture(0).end - newMarker.from;
 				if (m == null || newMarker.from < m.from || (newMarker.from == m.from && 
 						newLength > bestLength && !isCloseMatch)) {
 					m = newMarker;
 					bestLength = newLength;
 				}
 				positionNow = match.getCapture(0).end;
 				System.out.printf("  new position: %d\n", positionNow);
 			}
 		}
 		if (m != null) {
 			this.cachedMarkers.remove(m);
 			this.removePrecedingCachedMarkers(m);
 		}
 		return m;
 	}
 	
 	public Iterator<Marker> iterator() {
 		return new ScannerIterator(this);
 	}
 	
 	// TODO: implement this class for real
 	public class ScannerIterator implements Iterator<Marker> {
 		private Scanner scanner;
 		private Marker nextMarker;
 		
 		public ScannerIterator(Scanner scanner) {
 			this.scanner = scanner;
 		}
 		
 		public boolean hasNext() {
 			nextMarker = scanner.findNextMarker();
 			return (nextMarker != null);
 		}
 		
 		public Marker next() {
 			return nextMarker;
 		}
 		
 		public void remove() {
 			nextMarker = null;
 		}
 	}
 }
