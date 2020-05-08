 List<P> hull = graham(list);
 maxDist(hull);
 
 static double dist(P p1, P p2) {
 	return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x)
 			+ (p1.y - p2.y) * (p1.y - p2.y));
 }
 
 static double maxDist(List<P> hull) {
 	double max = 0, tmp = 0;
 	int j = 0, n = hull.size();
 	for (P p : hull) {
		while (tmp < dist(p, hull.get((j + 1) % n))) {
			j = (j + 1) % n;
			tmp = dist(p, hull.get(j));
		}
		max = Math.max(max, tmp);
 	}
 	return max;
 }
