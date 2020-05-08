 package org.sonatype.spice.zapper.internal;
 
 import org.sonatype.spice.zapper.Path;
 
 public class PathImpl
     extends StringIdentifier
     implements Path
 {
     public PathImpl( final String stringValue )
     {
         super( stringValue );
        Check.argument( !stringValue.contains( "\\" ), "Path must contain only forward slashes! path=" + stringValue );
     }
 }
