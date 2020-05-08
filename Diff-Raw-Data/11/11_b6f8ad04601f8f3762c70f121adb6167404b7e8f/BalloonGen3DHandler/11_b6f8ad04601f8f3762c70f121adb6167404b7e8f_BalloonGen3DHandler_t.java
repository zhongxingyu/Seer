 /* *****************************************************************************
  * Copyright (c) 2009 Ola Spjuth.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Ola Spjuth - initial API and implementation
  ******************************************************************************/
 package net.bioclipse.balloon.handlers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.bioclipse.balloon.business.Activator;
 import net.bioclipse.balloon.business.IBalloonManager;
import net.bioclipse.cdk.business.ICDKManager;
import net.bioclipse.cdk.domain.ICDKMolecule;
 import net.bioclipse.core.business.BioclipseException;
 import net.bioclipse.core.util.LogUtils;
 import net.bioclipse.jobs.BioclipseUIJob;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.content.IContentDescription;
 import org.eclipse.core.runtime.content.IContentType;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 /**
  * Our sample handler extends AbstractHandler, an IHandler base class.
  * @see org.eclipse.core.commands.IHandler
  * @see org.eclipse.core.commands.AbstractHandler
  */
 public class BalloonGen3DHandler extends AbstractHandler {
 
     private static final Logger logger = Logger.getLogger(BalloonGen3DHandler.class);
 
     /**
      * The constructor.
      */
     public BalloonGen3DHandler() {
     }
 
     /**
      * the command has been executed, so extract extract the needed information
      * from the application context.
      */
     public Object execute(ExecutionEvent event) throws ExecutionException {
 
         ISelection sel = HandlerUtil.getCurrentSelection( event );
         if (sel==null) return null;
         if (!( sel instanceof StructuredSelection )) return null;
         IStructuredSelection ssel = (IStructuredSelection)sel;
 
         //We operate on files and IMolecules
         List<IFile> inputFiles = new ArrayList<IFile>();
         List<IResource> foldersToRefresh=new ArrayList<IResource>();
         //Collect files
         for (Object obj : ssel.toList()){
             if ( obj instanceof IFile ) {
                 IFile file = (IFile) obj;
                 //                filenames.add( file.getRawLocation().toOSString() );
                 inputFiles.add( file );
                 foldersToRefresh.add( file.getParent() );
             }
         }
 
         logger.debug( "Balloon selection contained: " + inputFiles.size()
                       + " files." );
 
         if ( inputFiles.size() <= 0 )
             return null;
 
         final List<String> final_fnames = null;
         final List<IResource> final_foldersToRefresh = foldersToRefresh;
 
         IContentType cmlType = Platform
                         .getContentTypeManager()
                         .getContentType( "net.bioclipse.contenttypes.cml.singleMolecule2d" );
 
         IBalloonManager balloon = Activator.getDefault().getJavaBalloonManager();
         for ( IFile input : inputFiles ) {
             try {
                 IContentDescription contentDescirpton = input
                                 .getContentDescription();
                 if ( contentDescirpton != null && contentDescirpton
                                      .getContentType().isKindOf( cmlType ) ) {
                    String output =balloon.generate3Dcoordinates( input.getRawLocation()
                                     .toOSString() );
                    ICDKManager cdk = net.bioclipse.cdk.business.Activator
                    					.getDefault().getJavaCDKManager();
                    List<ICDKMolecule> molecules = cdk.loadMolecules(output);
                    cdk.saveMolecule(molecules.get(0),output,true);

                     return null;
                 }
             } catch ( Exception e ) {
                 LogUtils.handleException( e, logger,
                                           "net.bioclipse.balloon.business" );
                 return null;
             }
 
             balloon.generate3Dcoordinates( input, new BioclipseUIJob<IFile>() {
 
                 @Override
                 public void runInUI() {
 
                     try {
                         IFile result = this.getReturnValue();
                         result.getParent()
                                         .refreshLocal( IResource.DEPTH_ONE,
                                                        new NullProgressMonitor() );
                     } catch ( CoreException e ) {
                         LogUtils.handleException( e, logger,
                                                   "net.bioclipse.balloon.business" );
                     }
                 }
             } );
         }
         //Set up a job
         Job job = new Job("Ballon 3D conformer generation") {
             protected IStatus run(IProgressMonitor monitor) {
 
                 monitor.beginTask( "Running Balloon 3D conformer generation", 2 );
                 monitor.worked( 1 );
                 
                 //Run balloon on the files
                 IBalloonManager balloon = Activator.getDefault().getJavaBalloonManager();
                 List<String> ret=null;
                 try {
                     ret = balloon.generate3Dcoordinates( final_fnames);
                 } catch ( BioclipseException e ) {
                     logger.error("Balloon failed: " + e.getMessage());
                     return new Status(IStatus.ERROR,Activator.PLUGIN_ID,"Balloon failed: " + e.getMessage());
                 }
 
                 if (ret==null){
                     logger.error( "Balloon failed: " + ret );
                     return new Status(IStatus.ERROR,Activator.PLUGIN_ID,"Balloon failed.");
                 }
                 for (String r : ret){
                     logger.debug( "Balloon wrote output file: " + r  );
                 }
 
                 //Refresh folders in UI thread
                 Display.getDefault().syncExec( new Runnable(){
                     public void run() {
                         for (IResource res : final_foldersToRefresh){
                             try {
                                 res.refreshLocal( IResource.DEPTH_ONE, new NullProgressMonitor() );
                             } catch ( CoreException e ) {
                                 logger.error( "Could not refresh resource: " + res + " - " + e.getMessage() );
                             }
                         }
 
                     }
                 });
 
                 monitor.done();
 
                 return Status.OK_STATUS;
             }
         };
         // job.setPriority(Job.LONG);
         // job.setUser( true );
         // job.schedule(); // start as soon as possible
 
         //Bring forth the ProgressView
 
 
 
 
         /*
         //Collect and serialize smiles to temp file, 
         String smilesfile="";
         String linesep=System.getProperty("line.separator");
         if (linesep==null) linesep="\n";
 
         for (Object obj : ssel.toList()){
             if ( obj instanceof IMolecule ) {
                 IMolecule mol = (IMolecule) obj;
                 String smilestext;
                 try {
                     smilestext = mol.getSMILES();
                     smilesfile=smilesfile+smilestext+ linesep;
                 } catch ( BioclipseException e ) {
                     logger.debug("Could not get smiles from Imol: " + mol 
                                  + ". Skipped in balloon: " + e.getMessage());
                 }
             }
         }
 
 
 
         try {
             File tfile = File.createTempFile( "balloontemp", "smi" );
             FileWriter writer= new FileWriter(tfile);
             writer.write( smilesfile );
             writer.close();
 
             //Run balloon on this file
 
         } catch ( IOException e ) {
             e.printStackTrace();
         }
          */
 
         return null;
 
     }
 }
