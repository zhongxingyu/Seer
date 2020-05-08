 package net.cscott.sdr.calls.lists;
 
 import net.cscott.sdr.calls.Call;
 import net.cscott.sdr.calls.Program;
 
 /** 
  * The <code>MainstreamList</code> class contains complex call
  * and concept definitions which are on the 'mainstream' program.
  * Note that "simple" calls and concepts are defined in
  * the resource file at
 * <code>net/cscott/sdr/calls/lists/mainstream.calls</code>;
  * this class contains only those definitions for which an
  * executable component is required.
  * @author C. Scott Ananian
 * @version $Id: MainstreamList.java,v 1.6 2006-11-14 16:12:39 cananian Exp $
  */
 public abstract class MainstreamList {
     // hide constructor.
     private MainstreamList() { }
 
     private static abstract class MSCall extends Call {
         private final String name;
         MSCall(String name) { this.name = name; }
         @Override
         public final String getName() { return name; }
         @Override
         public final Program getProgram() { return Program.MAINSTREAM; }
     }
 
 }
