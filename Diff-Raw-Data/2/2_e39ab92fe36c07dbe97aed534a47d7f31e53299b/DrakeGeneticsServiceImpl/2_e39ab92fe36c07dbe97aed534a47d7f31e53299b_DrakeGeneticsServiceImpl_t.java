 /*
  * Copyright (c) 2010 The Jackson Laboratory
  * 
  * This is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this software.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.jax.drakegenetics.server;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jax.drakegenetics.gwtclientapp.client.DrakeGeneticsService;
 import org.jax.drakegenetics.shareddata.client.DiploidGenome;
 import org.jax.drakegenetics.shareddata.client.Tree;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 /**
  * The server side implementation of the GWT {@link DrakeGeneticsService} interface
  * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
  */
 public class DrakeGeneticsServiceImpl extends RemoteServiceServlet implements DrakeGeneticsService
 {
     /**
      * every {@link java.io.Serializable} is supposed to have one of these
      */
     private static final long serialVersionUID = -4876385760655645346L;
     //replace the next two constants with properties...for testing only
     private static final String LIBRARY_ROOT = "/Users/dow/Documents/workspace/CGDEDU/drake-genetics/modules/drake-genetics-client/src/www/Library";
     private static final String HELP_ROOT = "/Users/dow/Documents/workspace/CGDEDU/drake-genetics/modules/drake-genetics-client/src/www/Help";
     
 
     private final ReproductionSimulator reproductionSimulator =
         new ReproductionSimulator();
     private final StaticDocumentLibrary libraryController =
     	new StaticDocumentLibrary(new File(DrakeGeneticsServiceImpl.LIBRARY_ROOT));
     private final StaticDocumentLibrary helpController =
     	new StaticDocumentLibrary(new File(DrakeGeneticsServiceImpl.HELP_ROOT),
     			"HelpRoot");
     
     /**
      * {@inheritDoc}
      */
     public List<DiploidGenome> breedPair(
             DiploidGenome maternalGenome,
             DiploidGenome paternalGenome)
     {
         // TODO right now this is a simple pass through. Implement persistence etc.
         try
         {
             return this.reproductionSimulator.simulateReproduction(
                     maternalGenome,
                     paternalGenome);
         }
         catch(Exception ex)
         {
             ex.printStackTrace();
             return null;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Tree<String> getLibrary()
     {
         try
         {
             return this.libraryController.getLibraryIndex();
         }
         catch(Exception ex)
         {
             ex.printStackTrace();
             return null;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Tree<String> getHelp()
     {
         try
         {
             return this.helpController.getLibraryIndex();
         }
         catch(Exception ex)
         {
             ex.printStackTrace();
             return null;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public String getPublication(String journal, String volume, String article)
     {
     	List<String> nodes = new ArrayList<String>();
     	nodes.add(journal);
     	nodes.add(volume);
     	nodes.add(article);
         try
         {
            return this.libraryController.retrieveDocument(nodes);
         }
         catch(Exception ex)
         {
             ex.printStackTrace();
             return null;
         }
     }
 
 }
