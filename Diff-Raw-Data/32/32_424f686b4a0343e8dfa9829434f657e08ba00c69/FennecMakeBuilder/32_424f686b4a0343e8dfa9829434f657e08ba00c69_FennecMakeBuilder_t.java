 package org.mozilla.ide.eclipse.fennec;
 
import java.io.File;

 import org.eclipse.core.runtime.CoreException;
 
 public class FennecMakeBuilder extends FennecCommandBuilder {
     protected static final String MARKER_ID = "org.mozilla.ide.eclipse.fennec.MakeProblemMarker";
     public static final String ID = "org.mozilla.ide.eclipse.fennec.FennecMakeBuilder";
 
     @Override
     protected String[] getCommands() throws CoreException {
         // XXX: make "make" location generic
        File baseDir = new File(getObjDir(), "mobile/android/base");
        return new String[] {"/usr/bin/make", "-C", baseDir.getPath()};
     }
 
     @Override
     protected String getMarkerId() {
         return MARKER_ID;
     }
 }
