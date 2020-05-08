 /*******************************************************************************
  * Copyright (c) 2009  Ola Spjuth <ola@bioclipse.net>
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contact: http://www.bioclipse.net/
  ******************************************************************************/
 package net.bioclipse.vscreen.business;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import net.bioclipse.core.business.BioclipseException;
 import net.bioclipse.core.util.LogUtils;
 import net.bioclipse.core.util.StringInput;
 import net.bioclipse.core.util.StringStorage;
 import net.bioclipse.core.util.TimeCalculator;
 import net.bioclipse.managers.business.IBioclipseManager;
 import net.bioclipse.scripting.ui.business.IJsConsoleManager;
 import net.bioclipse.structuredb.Activator;
 import net.bioclipse.structuredb.business.IJavaStructuredbManager;
 import net.bioclipse.structuredb.domain.DBMolecule;
 import net.bioclipse.structuredb.domain.TextAnnotation;
 import net.bioclipse.vscreen.filters.IDoubleFilter;
 import net.bioclipse.vscreen.filters.IParamFilter;
 import net.bioclipse.vscreen.filters.IScreeningFilter;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * 
  * @author ola
  *
  */
 public class VScreenManager implements IBioclipseManager {
 
     private static final Logger logger = Logger.getLogger(VScreenManager.class);
 
     /**
      * List of screening filters as read from Extension Point
      */
     private List<String> screeningFilters;
     
     /**
      * Gives a short one word name of the manager used as variable name when
      * scripting.
      */
     public String getManagerName() {
         return "vscreen";
     }
     
 
     public void filter(String dbname, IScreeningFilter filter, String label, 
                        IProgressMonitor monitor) throws BioclipseException{
         
         List<IScreeningFilter> filters=new ArrayList<IScreeningFilter>();
         filters.add(filter);
         filter(dbname, filters, label, monitor);        
     }
 
     /**
      * Filters and puts result in same database under a new label
      * @param dbname
      * @param filters
      * @param label
      * @param monitor
      * @throws BioclipseException
      */
     public void filter(String dbname, List<IScreeningFilter> filters, 
                        String label, 
                        IProgressMonitor monitor) throws BioclipseException{
 
         filter( dbname, filters, dbname, label, monitor );
         
     }
     
     /**
      * Filters and puts result in arbitrary DB and label
      * @param dbname
      * @param filters
      * @param newDBname
      * @param label
      * @param monitor
      * @throws BioclipseException
      */
     public void filter(String dbname, List<IScreeningFilter> filters, 
                        String newDBname, String label, 
                        IProgressMonitor monitor) throws BioclipseException{
 
         if (filters==null || filters.size()<=0)
             throw new BioclipseException( "No filters provided to vscreen." );
         
         //Output the selected filters for debugging purposes
         logger.debug("Filters included: ");
         for (IScreeningFilter filter : filters){
             String f=filter.getName();
             if ( filter instanceof IDoubleFilter ) {
                 IDoubleFilter df = (IDoubleFilter) filter;
                 f=f+ " [op=" + df.getOperator() + " ; thr=" + df.getThreshold() 
                 + "]";
             }
             if ( filter instanceof IParamFilter ) {
                 IParamFilter pf = (IParamFilter) filter;
                 f=f+ " [params=" + pf.getParameterString() + "]";
             }
             logger.debug(f);
         }
 
         //Get managers we need
      IJavaStructuredbManager sdb=Activator.getDefault().getStructuredbManager();
         
         if (!(sdb.allDatabaseNames().contains( dbname )))
             throw new BioclipseException( "No database exists with name: " 
                                           + dbname );
         
         if (filters==null || filters.size()<=0)
             throw new BioclipseException( "Please provide at least one filter");
 
         if (label==null || label.length()<=0)
             throw new BioclipseException( "Label must not be empty");
 
         //Verify new DB exists
         if (!sdb.allDatabaseNames().contains( newDBname )){
             //create new DB
             logger.debug("Database: " + newDBname + " does not exist, " +
             		"creating this now.");
             sdb.createDatabase( newDBname );
                         
         }
 
         //Create the annotation to store in.
         TextAnnotation filteredAnnotation = sdb.createTextAnnotation( newDBname, 
                                                                       "label",
                                                                       label );
 
 
         int noMols=sdb.numberOfMoleculesInDatabaseInstance( dbname );
         monitor.beginTask( "Screening molecules...", noMols );
         Iterator<DBMolecule> iterator = sdb.allStructuresIterator(dbname);
         long starttime=System.currentTimeMillis();
         int cnt=0;
 
         //And iterate over all molecules
         while ( iterator.hasNext() ) {
             cnt++;
             
             //Update progress at regular intervals 
             if (cnt%10==1){
                 monitor.subTask( "Screening molecule " + cnt + " of " + noMols 
                                  + " (" 
                                  + TimeCalculator.generateTimeRemainEst( 
                                                         starttime, cnt, noMols)
                                                         + ")");
             }
 
             //Check for cancellation
             if (monitor.isCanceled())
                 throw new OperationCanceledException();
 
             //Get mol, filter, and add annotation if match
             DBMolecule molecule = iterator.next();
 
             //For each filter, do matching. All must succeed in order to pass
             int matchesRequired=filters.size();
             int matches=0;
             for (IScreeningFilter filter : filters){
                 try {
                     if (filter.passFilter( molecule )){
                         matches++;
                     }else{
                         logger.debug("Molecule # " +cnt +") "+ 
                                    " did not pass filter: " + filter.getName());   
                     }
                 } catch ( BioclipseException e ) {
                     logger.error("Filter " + filter.getName() 
                                  + " failed for molecule # " + cnt 
                                  + ". Reason: " + e.getMessage());
                 }
             }
 
             //If all filters passed, add annotation to mol
             if (matches>=matchesRequired){
                logger.debug("Molecule: " + molecule + 
                " passed all filters.");   
                if (dbname==newDBname){
                    molecule.addAnnotation(filteredAnnotation);
                    sdb.updateMolecule(dbname, molecule);
                }
                else{
                    //New DB, we need to copy the molecule to new database
                    DBMolecule newmol = sdb.createMolecule( newDBname, 
                                        molecule.getName(), 
                                        molecule );

                    //Also, we need to annotate it with the filteredAnnotation
                    sdb.annotate( newDBname, newmol, filteredAnnotation );
                }
            }else{
             logger.debug("Molecule: " + molecule + " did not pass all " +
             		"filters " + "(" + matches + " failures)");   
             }
 
             monitor.worked( 1 );
         
         }        
     }
 
     public IScreeningFilter createFilter(String filtername, String params) 
     throws BioclipseException{
         
         filtername=filtername.toLowerCase();
         
         if (!existsFilter( filtername ))
             throw new BioclipseException( "No filter with name: " +filtername );
 
         //Look up and instantiate filter
         try {
             IScreeningFilter filter = newFilter( filtername );
             if ( filter instanceof IParamFilter ) {
                 IParamFilter paramFilter = (IParamFilter) filter;
                 paramFilter.setParameterString(params);
                 return paramFilter;
             }else{
                 logger.debug("A non-IParamFilter was given " +
                     "params as input.");
             }
         } catch ( Exception e ) {
             throw new BioclipseException( "Error instantiating filter: " 
                                           + e.getMessage() );
         }
 
         throw new BioclipseException( "Filter found but not supported: " 
                                       + filtername );
 
     }
     
     public IScreeningFilter createFilter(String filtername, double value) 
     throws BioclipseException{
 
         //FIXME, for now treat as operator=""
         return createFilter( filtername, "", value );
     }
     
     public IScreeningFilter createFilter(String filtername, String operator, 
                                 double threshold) throws BioclipseException{
         
         filtername=filtername.toLowerCase();
         
         if (!existsFilter( filtername ))
             throw new BioclipseException( "No filter with name: " +filtername );
 
         //Look up and instantiate filter
         IScreeningFilter filter;
         try {
             filter = newFilter( filtername );
             if ( filter instanceof IDoubleFilter ) {
                 IDoubleFilter df= (IDoubleFilter) filter;
                 df.setOperator( operator );
                 df.setThreshold( threshold );
                 return df;
             }else{
                 logger.debug("A non-IDoubleFilter was given operator and " +
                 		"threshold as input.");
             }
         } catch ( Exception e ) {
             throw new BioclipseException( "Error instantiating filter: " 
                                           + e.getMessage() );
         }
 
         throw new BioclipseException( "Filter found but not supported: " 
                                       + filtername );
         
     }
 
     /**
      * Return a list of names of available filters.
      * @return
      * @throws BioclipseException
      */
     public List<String> listFilters() throws BioclipseException{
         if (screeningFilters==null) initializeFilters();
         return screeningFilters;
     }
 
 
 
     /**
      * 
      * @param filterName Name of filter to create
      * @return a new filter with name filterName
      * @throws BioclipseException if no filter with filterName exists or if 
      * creation fails
      */
     private IScreeningFilter newFilter(String filterName) 
     throws BioclipseException {
 
         if (!existsFilter(filterName))
             throw new BioclipseException( "No filter wih name: " + filterName );
 
         //Read EP
         IExtensionRegistry registry = Platform.getExtensionRegistry();
 
         if ( registry == null ) throw new BioclipseException(
                 "Eclipse registry=null. Cannot get screeningfilters from EPs.");
         // it likely means that the Eclipse workbench has not
         // started, for example when running non plugin-tests
 
         IExtensionPoint serviceObjectExtensionPoint = registry
         .getExtensionPoint("net.bioclipse.vscreen.filter");
 
         IExtension[] serviceObjectExtensions
         = serviceObjectExtensionPoint.getExtensions();
 
         for(IExtension extension : serviceObjectExtensions) {
             for( IConfigurationElement element
                     : extension.getConfigurationElements() ) {
 
                 if (element.getName().equals("screeningFilter")){
 
                     String pname=element.getAttribute("name");
                     
                     if (pname.equalsIgnoreCase( filterName )){
                         //This is what we ask for
                         String pid=element.getAttribute("id");
                         String picon=element.getAttribute("icon");
                         String pdescription=element.getAttribute("description");
                         String pluginID=element.getNamespaceIdentifier();
 
                         Object obj;
                         try {
                             obj = element.createExecutableExtension("class");
                             IScreeningFilter filter = (IScreeningFilter) obj;
                             filter.setId( pid );
                             filter.setName(  pname );
                             filter.setIconpath( picon );
                             filter.setDescription( pdescription );
                             filter.setPlugin( pluginID );
 
                             return filter;
                         } catch ( CoreException e ) {
                             LogUtils.handleException( e, logger, 
                                     net.bioclipse.vscreen.Activator.PLUGIN_ID );
                         }
                     }
                 }
             }
         }
 
         throw new BioclipseException( "Could not find filter named " 
                                       + filterName );
 
     }
 
 
     /**
      * 
      * @param filterName Name of filter to check existence for
      * @return true if s screeningfilter with filterName exists.
      * @throws BioclipseException
      */
     private boolean existsFilter( String filterName ) throws BioclipseException{
         
         if (screeningFilters==null) initializeFilters();
         if (screeningFilters==null) 
                throw new BioclipseException( "No screeningfilters available." );
 
          return screeningFilters.contains( filterName.toLowerCase() );
     }
 
 
     /**
      * Create a list of names for the available filters
      * @throws BioclipseException
      */
     private void initializeFilters() throws BioclipseException {
 
         screeningFilters=new ArrayList<String>();
         
         //Read EP
         IExtensionRegistry registry = Platform.getExtensionRegistry();
 
         if ( registry == null ) throw new BioclipseException(
                 "Eclipse registry=null. Cannot get screeningfilters from EPs.");
         // it likely means that the Eclipse workbench has not
         // started, for example when running non plugin-tests
 
         IExtensionPoint serviceObjectExtensionPoint = registry
         .getExtensionPoint("net.bioclipse.vscreen.filter");
 
         IExtension[] serviceObjectExtensions
         = serviceObjectExtensionPoint.getExtensions();
 
         for(IExtension extension : serviceObjectExtensions) {
             for( IConfigurationElement element
                     : extension.getConfigurationElements() ) {
 
                 if (element.getName().equals("screeningFilter")){
 
                     if (element.getAttribute("name")!=null)
                         screeningFilters.add(element.getAttribute("name")
                                              .toLowerCase());
                 }
             }
         }
     }
     
     /**
      * Execute the open editor if based on file or String input.
      * Should probably be moved to scripting.ui when harmonizing scripting.
      * 
      * @throws BioclipseException
      * @throws CoreException
      * @throws IOException
      */
     public void run() throws BioclipseException, CoreException, IOException{
         
         final StringBuffer buf=new StringBuffer();
 
         Display.getDefault().syncExec( new Runnable() {
 
             public void run() {
 
                 IEditorPart editor = PlatformUI.getWorkbench()
                 .getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 //                if (editor==null) throw new BioclipseException( "No editor open." );
 
 
                 IEditorInput input = editor.getEditorInput();
                 InputStream is=null;
                 if ( input instanceof IFileEditorInput ) {
                     IFile file=((IFileEditorInput)input).getFile();
                     try {
                         is=file.getContents();
                     } catch ( CoreException e ) {
                         e.printStackTrace();
                     }
                 }
                 else if (input instanceof StringInput ) {
                     StringStorage st=(StringStorage) ((StringInput)input).getStorage();
                     try {
                         is=st.getContents();
                     } catch ( CoreException e ) {
                         e.printStackTrace();
                     }
                 }
                 else{
                     return;
  //                   throw new BioclipseException( "The open editor is not supported." );
                 }
                 
                 //Read IS into String
                 BufferedReader br=new BufferedReader( new InputStreamReader( is ));
                 String line;
                 try {
                     line = br.readLine();
                     while (line!=null){
                         buf.append( line + "\n");
                         line = br.readLine();
                     }
                 } catch ( IOException e ) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
             }
         });
         
         
         
         String contents=buf.toString();
         if (contents==null) throw new BioclipseException("Editor is empty.");
         
         IJsConsoleManager js=net.bioclipse.scripting.ui.Activator.getDefault()
         .getJavaJsConsoleManager();
         
         js.eval( contents );
     }
 
 }
