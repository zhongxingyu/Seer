 package org.mozilla.ide.eclipse.fennec;
 
 import org.eclipse.core.runtime.CoreException;
 
 public class FennecMakeBuilder extends FennecCommandBuilder {
     protected static final String MARKER_ID = "org.mozilla.ide.eclipse.fennec.MakeProblemMarker";
     public static final String ID = "org.mozilla.ide.eclipse.fennec.FennecMakeBuilder";
 
     @Override
     protected String[] getCommands() throws CoreException {
         // XXX: make "make" location generic
        return new String[] {"/usr/bin/make", "-C", getObjDir()};
     }
 
     @Override
     protected String getMarkerId() {
         return MARKER_ID;
     }
 }
