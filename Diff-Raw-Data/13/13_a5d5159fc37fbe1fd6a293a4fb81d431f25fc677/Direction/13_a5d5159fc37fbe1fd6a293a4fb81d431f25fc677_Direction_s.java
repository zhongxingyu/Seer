 package org.qnot.pwrecta;
 
 public enum Direction {
     N, S, E, W, NE, NW, SE, SW;
     
     public static Direction fromString(String str) throws SequenceParseException {
         if("N".equalsIgnoreCase(str)) {
             return Direction.N;
         } else if("S".equalsIgnoreCase(str)) {
             return Direction.S;
         } else if("E".equalsIgnoreCase(str)) {
             return Direction.E;
         } else if("W".equalsIgnoreCase(str)) {
             return Direction.W;
         } else if("NE".equalsIgnoreCase(str)) {
             return Direction.NE;
         } else if("NW".equalsIgnoreCase(str)) {
             return Direction.NW;
         } else if("SE".equalsIgnoreCase(str)) {
             return Direction.SE;
         } else if("SW".equalsIgnoreCase(str)) {
             return Direction.SW;
         } else {
             throw new SequenceParseException("Invalid Direction. Must be [n|w|e|w|ne|nw|se|sw]");
         }
     }
     
     public static Direction[] clockwiseCompass() {
         return new Direction[]{N,NW,W,SW,S,SE,E,NE};
     }
     
     public static Direction[] counterclockwiseCompass() {
        return new Direction[]{N,NE,E,SE,S,SW,W,NW};
     }
 
 }
