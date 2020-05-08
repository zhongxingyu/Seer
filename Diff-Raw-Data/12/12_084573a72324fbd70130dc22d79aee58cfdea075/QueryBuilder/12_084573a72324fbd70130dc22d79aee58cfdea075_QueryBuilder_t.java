 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 42):
  * "Sven Strittmatter" <ich@weltraumschaf.de> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a beer in return.
  */
 
 package org.jvnet.hudson.plugins.darcs.browsers;
 
 /**
  * Helper class to build up URL queries.
  *
  * @author Sven Strittmatter <ich@weltraumschaf.de>
  */
 public class QueryBuilder {
 	/**
 	 * Types for queries.
 	 */
     public enum SeparatorType {
         SLASHES,    // seperates everything with slash, REST like
         SEMICOLONS, // starts wit ? and then seperates with ;
         AMPERSANDS  // starts wit ? and then seperates with &
     }
 
 	/**
 	 * Buffers the builded query string
 	 */
     private final StringBuilder buf = new StringBuilder();
 	/**
 	 * The seperatot type for the query.
 	 */
     private final SeparatorType type;
 
     QueryBuilder(SeparatorType t) {
         this(t, null);
     }
 
     QueryBuilder(SeparatorType t, String s) {
         this.type = t;
         add(s);
     }
 
 	public SeparatorType getType() {
 		return this.type;
 	}
 
     public QueryBuilder add(String s) {
         if (null == s) {
             // nothing to add
             return this;
         }
 
         switch (this.type) {
             case SLASHES:
                if(buf.length() > 0) {
                    buf.append('/');
                }

                 break;
             case SEMICOLONS:
                 if(buf.length() == 0) {
                     buf.append('?');
                 } else {
                     buf.append(';');
                 }
                 
                 break;
             case AMPERSANDS:
                 if (buf.length() == 0) {
                     buf.append('?');
                 } else {
                     buf.append('&');
                 }
 
                 break;
         }
 
         buf.append(s);
 
         return this;
     }
 
     @Override
     public String toString() {
         return buf.toString();
     }
 }
