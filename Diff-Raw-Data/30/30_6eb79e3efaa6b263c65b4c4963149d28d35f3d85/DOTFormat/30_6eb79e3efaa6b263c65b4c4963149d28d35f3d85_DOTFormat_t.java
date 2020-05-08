 /*  
  * This file is part of CxALite
  *
  *  CxALite is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  CxALite is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
  *
  *  (c) 2009, University of Geneva (Jean-Luc Falcone), jean-luc.falcone@unige.ch
  *
  */
 
 package cxa.extra.export;
 
 import cxa.CxA;
 import cxa.components.conduits.Conduit;
 import java.io.IOException;
 import java.io.PrintStream;
 
 /**
  * Defines a simple DOT format export.
  */
 public class DOTFormat implements ExportFormat {
 
     private static String sanitize( String in ) {
         return in.replace( " ", "_");
     }
 
    /**
     * Syntactical sugar to export a CxA with the dot format. The call will return
     * an Exporter object set with CxA. So the to() method must be called to start
     * the export. Example usage:
     * <pre>DOTFormat.export( cxa ).to( System.out );</pre>
     * @param cxa the cxa to export
     * @return an exporter instance set with the cxa
     */
    public static Exporter export( CxA cxa ) {
        return new Exporter( new DOTFormat() ).export( cxa );
    }

     @Override
     public void export( CxA cxa, PrintStream ps ) throws IOException {
         ps.println( "digraph " + sanitize( cxa.ID() ) + " {" );
         ps.println( "  rankdir = LR;" );
         for( Conduit c : cxa.getConduits() ) {
             ps.print( "  " + sanitize( c.getSenderKernel().ID() ) );
             ps.print( " -> " +  sanitize( c.getReceiverKernel().ID() ) );
             ps.println( " [" + " label=\"" + sanitize( c.ID() ) + "\" ];");
         }
         ps.println("}");
     }
 }
