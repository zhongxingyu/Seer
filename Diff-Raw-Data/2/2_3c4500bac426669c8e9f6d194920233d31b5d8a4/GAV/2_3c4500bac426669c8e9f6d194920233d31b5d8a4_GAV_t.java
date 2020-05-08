 package com.cloudbees.sdk;
 
 /**
  * GroupId, artifactId, version tuple.
  *
  * @author Kohsuke Kawaguchi
  */
 public final class GAV {
     public final String groupId, artifactId, version;
 
     public GAV(String groupId, String artifactId, String version) {
         this.groupId = groupId;
         this.artifactId = artifactId;
         this.version = version;
         if (groupId==null || artifactId==null ||version==null)
             throw new IllegalArgumentException();
     }
     
     public GAV(String id) {
         String[] tokens = id.split(":");
         if (tokens.length!=3)   throw new IllegalArgumentException("Expected GROUPID:ARTIFACTID:VERSION but got '"+id+"'");
         this.groupId    = tokens[0];
        this.artifactId = tokens[1];
         this.version    = tokens[2];
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         GAV gav = (GAV) o;
 
         return artifactId.equals(gav.artifactId) && groupId.equals(gav.groupId) && version.equals(gav.version);
 
     }
 
     @Override
     public int hashCode() {
         int result = groupId.hashCode();
         result = 31 * result + artifactId.hashCode();
         result = 31 * result + version.hashCode();
         return result;
     }
 
     @Override
     public String toString() {
         return String.format("%s:%s:%s",groupId,artifactId,version);
     }
 }
