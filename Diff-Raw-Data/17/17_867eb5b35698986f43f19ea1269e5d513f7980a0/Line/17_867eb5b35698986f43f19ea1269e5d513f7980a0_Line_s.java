 package gipfj;
 
 // what clojure interface controls rendering?
 public class Line {
     private final int start;
     private final int delta;
     private final int sig;
 
     private Line(int sig, int start, int delta) {
         this.start = start;
         this.delta = delta;
         this.sig = sig;
     }
 
     @Override
     public String toString() {
        return String.format("%d %d", this.start, this.delta);
         // return String.format("<Line G: %d S: %s D: %s>", this.sig,
         // Geometry.toString(this.start), Geometry.toString(this.delta));
     }
 
     public static Line makeLine(int start, int delta) {
         return new Line(0, start, delta);
     }
 
     public static Line makeSignedLine(int sig, int start, int delta) {
         return new Line(sig, start, delta);
     }
 
     public static Line advanceLine(Line q) {
         return new Line(q.sig, Geometry.padd(q.start, q.delta), q.delta);
     }
 
     public static int getSig(Line q) {
         return q.sig;
     }
 
     public static int getStart(Line q) {
         return q.start;
     }
 
     public static int getDelta(Line q) {
         return q.delta;
     }
 
     public static boolean onLine(int loc, Line q) {
         int delta = Geometry.psubtract(loc, q.start);
         int dist = Geometry.pradius(delta);
         int approx = Geometry.pmultiply(dist, q.delta);
         return (delta == approx || delta == Geometry.pnegate(approx));
     }
 
     public static boolean equals(Line a, Line b) {
         // order is proportional to possibilities
         return (a.start == b.start && a.delta == b.delta && a.sig == b.sig);
     }
 
     public static boolean same(Line a, Line b) {
         // order is proportional to possibilities
         return (a.start == b.start && a.delta == b.delta);
     }
 
     public static Line sign(Line l, int sig) {
         return new Line(sig, l.start, l.delta);
     }
 }
