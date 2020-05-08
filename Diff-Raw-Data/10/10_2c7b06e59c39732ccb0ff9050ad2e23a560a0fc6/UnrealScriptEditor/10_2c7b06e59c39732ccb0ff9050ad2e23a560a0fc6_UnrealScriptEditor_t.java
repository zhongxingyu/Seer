 package com.patrick_vane.unrealscript.editor;
 
 import java.io.File;
 import java.io.PrintStream;
 import java.nio.charset.Charset;
 import java.nio.file.Files;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.source.AnnotationRulerColumn;
 import org.eclipse.jface.text.source.CompositeRuler;
 import org.eclipse.jface.text.source.IAnnotationModel;
 import org.eclipse.jface.text.source.IVerticalRuler;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.TreeSelection;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.editors.text.TextEditor;
 import com.patrick_vane.unrealscript.editor.console.UDKLaunchLogConsole;
 import com.patrick_vane.unrealscript.editor.console.UnrealScriptCompilerConsole;
 import com.patrick_vane.unrealscript.editor.constants.ProjectConstant;
 import com.patrick_vane.unrealscript.editor.default_classes.ColorManager;
 import com.patrick_vane.unrealscript.editor.default_classes.DocumentProvider;
 import com.patrick_vane.unrealscript.editor.default_classes.MyRunnable;
 import com.patrick_vane.unrealscript.editor.default_classes.MyStream;
 import com.patrick_vane.unrealscript.editor.parser.CodeException;
 import com.patrick_vane.unrealscript.editor.parser.UnrealScriptParser;
 
 
 public class UnrealScriptEditor extends TextEditor
 {
 	public UnrealScriptEditor()
 	{
 		super();
 		
 		ColorManager.init();
 		
 		UnrealScriptCompilerConsole.init();
 		UDKLaunchLogConsole.init();
 		
 		setSourceViewerConfiguration( new Configuration() );
 		setDocumentProvider( new DocumentProvider() );
 		
 		initializeSourceViewer();
 		updateMarkersThread.start();
 		
 		synchronized( updateLaunchLogThreadSynchronizer )
 		{
 			if( updateLaunchLogThreadRunners == 0 )
 			{
 				updateLaunchLogThread = new UpdateLaunchLogThread();
 				updateLaunchLogThread.start();
 			}
 			updateLaunchLogThreadRunners++;
 		}
 		
 		try
 		{
 			PlatformUI.getWorkbench().showPerspective( UnrealScriptID.PERSPECTIVE, PlatformUI.getWorkbench().getActiveWorkbenchWindow() );
 		}
 		catch( Exception e )
 		{
 		}
 	}
 	
 	
 	private int markers = -1;
 	private boolean updateMarkersThreadRunning = true;
 	private Thread updateMarkersThread = new Thread()
 	{
 		@Override
 		public void run()
 		{
 			while( updateMarkersThreadRunning )
 			{
 				try
 				{
 					Thread.sleep( 2000 );
 					updateMarkers();
 				}
 				catch( Exception e )
 				{
 				}
 			}
 		}
 	};
 	
 	private static final String		LAUNCH_LOG_TAG					= "] ScriptLog: ";
 	private static final int		LAUNCH_LOG_TAG_LENGTH			= LAUNCH_LOG_TAG.length();
 	private static final String		LAUNCH_WARNING_TAG				= "] ScriptWarning: ";
 	private static final int		LAUNCH_WARNING_TAG_LENGTH		= LAUNCH_WARNING_TAG.length();
 	private static final String		LAUNCH_ERROR_TAG				= "] Error: ";
 	private static final int		LAUNCH_ERROR_TAG_LENGTH			= LAUNCH_ERROR_TAG.length();
 	private static File				launchLogFile;
 	private static long				launchLogFileLastModified;
 	private static long				launchLogFileLength;
 	private static int updateLaunchLogThreadRunners = 0;
 	private static final Object updateLaunchLogThreadSynchronizer = new Object();
 	private static final Runnable launchConsoleClearListener = new Runnable()
 	{
 		@Override
 		public void run()
 		{
 			synchronized( UDKLaunchLogConsole.getSynchronizer() )
 			{
 				launchLogFileLastModified = 0;
 				launchLogFileLength = 0;
 			}
 		}
 	};
 	private static UpdateLaunchLogThread updateLaunchLogThread;
 	private class UpdateLaunchLogThread extends Thread
 	{
 		public UpdateLaunchLogThread()
 		{
 			UDKLaunchLogConsole.addClearListener( launchConsoleClearListener );
 		}
 		@Override
 		public void run()
 		{
 			while( updateLaunchLogThreadRunners > 0 )
 			{
 				try
 				{
 					Thread.sleep( 500 );
 					
 					if( launchLogFile == null )
 					{
 						launchLogFile = new File( getActiveProjectFile(), ProjectConstant.subfolders.get("Logs")+"/Launch.log" );
 					}
 					
 					synchronized( UDKLaunchLogConsole.getSynchronizer() )
 					{
 						if( !launchLogFile.exists() )
 						{
 							if( (launchLogFileLastModified != 0) || (launchLogFileLength != 0) )
 							{
 								UDKLaunchLogConsole.clear();
 							}
 						}
 						else
 						{
 							long newLaunchLogFileLastModified 	= launchLogFile.lastModified();
 							long newLaunchLogFileLength 		= launchLogFile.length();
 							if( (newLaunchLogFileLastModified != launchLogFileLastModified) || (newLaunchLogFileLength != launchLogFileLength) )
 							{
 								List<String> lines = Files.readAllLines( launchLogFile.toPath(), Charset.defaultCharset() );
 								UDKLaunchLogConsole.clear();
 								
								launchLogFileLastModified 	= newLaunchLogFileLastModified;
								launchLogFileLength 		= newLaunchLogFileLength;
								
 								int logMode = 0;
 								for( String line : lines )
 								{
 									int logCharAt 		= line.indexOf( LAUNCH_LOG_TAG );
 									int warningCharAt 	= line.indexOf( LAUNCH_WARNING_TAG );
 									int errorCharAt 	= line.indexOf( LAUNCH_ERROR_TAG );
 									
 									if( logCharAt >= 0 )
 									{
 										logMode = 1;
 										UDKLaunchLogConsole.out.println( line.substring(0, logCharAt+1) + " [INFO] " + line.substring(logCharAt+LAUNCH_LOG_TAG_LENGTH) );
 									}
 									else if( warningCharAt >= 0 )
 									{
 										logMode = 2;
 										UDKLaunchLogConsole.warn.println( line.substring(0, warningCharAt+1) + " [WARNING] " + line.substring(warningCharAt+LAUNCH_WARNING_TAG_LENGTH) );
 									}
 									else if( errorCharAt >= 0 )
 									{
 										logMode = 3;
 										UDKLaunchLogConsole.err.println( line.substring(0, errorCharAt+1) + " [ERROR] " + line.substring(errorCharAt+LAUNCH_ERROR_TAG_LENGTH) );
 									}
 									else
 									{
 										if( logMode > 0 )
 										{
 											if( !line.startsWith("[") )
 											{
 												if( logMode == 1 )
 													UDKLaunchLogConsole.out.println( line );
 												else if( logMode == 2 )
 													UDKLaunchLogConsole.warn.println( line );
 												else if( logMode == 3 )
 													UDKLaunchLogConsole.err.println( line );
 											}
 											else
 											{
 												logMode = 0;
 											}
 										}
 									}
 								}
 							}
 						}
 					}
 				}
 				catch( Exception e )
 				{
 				}
 			}
 			
 			UDKLaunchLogConsole.removeClearListener( launchConsoleClearListener );
 		}
 	}
 	
 	
 	protected void initializeSourceViewer()
 	{
 		IAnnotationModel model = getDocumentProvider().getAnnotationModel( getEditorInput() );
 		IDocument document = getDocumentProvider().getDocument( getEditorInput() );
 		
 		if( document != null )
 		{
 			getSourceViewer().setDocument( document, model );
 			createVerticalRuler().setModel( model );
 		}
 	}
 	@Override
 	protected IVerticalRuler createVerticalRuler()
 	{
 		CompositeRuler ruler = new CompositeRuler();
 		ruler.addDecorator( 0, new AnnotationRulerColumn(VERTICAL_RULER_WIDTH) );
 		return ruler;
 	}
 	
 	
 	public void addErrorMarker( final int startCharacter, final int endCharacter, final String message )
 	{
 		final IFile file = getFile();
 		try
 		{
 			if( file != null )
 			{
 				IMarker marker = file.createMarker( UnrealScriptID.MARKER_PROBLEM );
 				marker.setAttribute( IMarker.CHAR_START, startCharacter );
 				marker.setAttribute( IMarker.CHAR_END, endCharacter );
 				marker.setAttribute( IMarker.LOCATION, "UnrealScript File" );
 				marker.setAttribute( IMarker.SEVERITY, IMarker.SEVERITY_ERROR );
 				marker.setAttribute( IMarker.MESSAGE, message );
 			}
 		}
 		catch( CoreException e )
 		{
 		}
 	}
 	public void addWarningMarker( final int startCharacter, final int endCharacter, final String message )
 	{
 		final IFile file = getFile();
 		try
 		{
 			if( file != null )
 			{
 				IMarker marker = file.createMarker( UnrealScriptID.MARKER_PROBLEM );
 				marker.setAttribute( IMarker.CHAR_START, startCharacter );
 				marker.setAttribute( IMarker.CHAR_END, endCharacter );
 				marker.setAttribute( IMarker.LOCATION, "UnrealScript File" );
 				marker.setAttribute( IMarker.SEVERITY, IMarker.SEVERITY_WARNING );
 				marker.setAttribute( IMarker.MESSAGE, message );
 			}
 		}
 		catch( CoreException e )
 		{
 		}
 	}
 	public void clearMarkers()
 	{
 		final IFile file = getFile();
 		if( file != null )
 		{
 			try
 			{
 				file.deleteMarkers( UnrealScriptID.MARKER_PROBLEM, true, IResource.DEPTH_INFINITE );
 			}
 			catch( CoreException e )
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	
 	public void updateMarkers()
 	{
 		CodeException[] exceptions = null;
 		if( getSourceViewer() != null )
 		{
 			if( getSourceViewer().getDocument() != null )
 			{
 				exceptions = UnrealScriptParser.findErrors( getSourceViewer().getDocument().get() );
 			}
 		}
 		
 		if( (markers != 0) || ((exceptions != null) && (exceptions.length != 0)) )
 		{
 			if( exceptions != null )
 				markers = exceptions.length;
 			final CodeException[] EXCEPTIONS = exceptions;
 			IWorkspaceRunnable runnable = new IWorkspaceRunnable()
 			{
 				@Override
 				public void run( IProgressMonitor monitor ) throws CoreException
 				{
 					clearMarkers();
 					if( EXCEPTIONS != null )
 					{
 						for( CodeException e : EXCEPTIONS )
 						{
 							if( e.isError() )
 								addErrorMarker( e.getFirstCharacterPosition(), e.getLastCharacterPosition(), e.getMessage() );
 							else
 								addWarningMarker( e.getFirstCharacterPosition(), e.getLastCharacterPosition(), e.getMessage() );
 						}
 					}
 				}
 			};
 			try
 			{
 				ResourcesPlugin.getWorkspace().run( runnable, new NullProgressMonitor() );
 			}
 			catch( CoreException e )
 			{
 			}
 		}
 	}
 	
 	
 	public IFile getFile()
 	{
 		if( getEditorInput() != null )
 			return (IFile) getEditorInput().getAdapter( IFile.class );
 		return null;
 	}
 	
 	
 	public static IWorkbenchWindow getActiveWorkbenchWindow()
 	{
 		MyRunnable runnable = new MyRunnable()
 		{
 			@Override
 			public void run()
 			{
 				done( PlatformUI.getWorkbench().getActiveWorkbenchWindow() );
 			}
 		};
 		Display.getDefault().syncExec( runnable );
 		while( !runnable.isDone() )
 		{
 			try
 			{
 				Thread.sleep( 20 );
 			}
 			catch( Exception e )
 			{
 			}
 		}
 		if( runnable.getResult() != null )
 			return (IWorkbenchWindow) runnable.getResult();
 		return null;
 	}
 	public static IFile getActiveFile()
 	{
 		try
 		{
 			Object object = getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput().getAdapter( IFile.class );
 			if( object instanceof IFile )
 			{
 				return (IFile) object;
 			}
 		}
 		catch( Exception e )
 		{
 		}
 		return null;
 	}
 	public static IProject getProject( String name )
 	{
 		try
 		{
 			IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
 			return myWorkspaceRoot.getProject( name );
 		}
 		catch( Exception e )
 		{
 		}
 		return null;
 	}
 	public static File getProjectFile( IProject project )
 	{
 		try
 		{
 			return new File( project.getFolder("UnrealScript").getLocationURI() ).getParentFile().getParentFile();
 		}
 		catch( Exception e )
 		{
 			return null;
 		}
 	}
 	public static IProject getActiveProject()
 	{
 		try
 		{
 			IFile file = getActiveFile();
 			IContainer project = file.getProject();
 			while( project.getParent().getParent() != null )
 			{
 				project = project.getParent();
 			}
 			return project.getProject();
 		}
 		catch( Exception e )
 		{
 		}
 		return null;
 	}
 	public static File getActiveProjectFile()
 	{
 		try
 		{
 			return getProjectFile( getActiveProject() );
 		}
 		catch( Exception e )
 		{
 			return null;
 		}
 	}
 	public static IProject getSelectedProject() throws Exception
 	{
 		return getSelectedProject( getActiveWorkbenchWindow() );
 	}
 	public static IProject getSelectedProject( IWorkbenchWindow window ) throws Exception
 	{
 		if( window == null )
 			window = getActiveWorkbenchWindow();
 		if( window == null )
 			return null;
 		IProject project = null;
 		ISelection selection = window.getSelectionService().getSelection( UnrealScriptID.VIEW_NAVIGATOR );
 		if( (selection != null) && !selection.isEmpty() )
 		{
 			if( selection instanceof TreeSelection )
 			{
 				Object[] selectedObjects = ((TreeSelection) selection).toArray();
 				ArrayList<IProject> projects = new ArrayList<IProject>();
 				for( Object selectedObject : selectedObjects )
 				{
 					if( selectedObject instanceof IProject )
 					{
 						projects.add( (IProject) selectedObject );
 					}
 				}
 				if( projects.size() > 1 )
 				{
 					throw new Exception( "You need to select one Project (in the Package Explorer), you've selected several Projects." );
 				}
 				if( projects.size() == 1 )
 				{
 					project = projects.get( 0 );
 				}
 			}
 		}
 		if( project == null )
 		{
 			throw new Exception( "You need to select a Project (in the Package Explorer)" );
 		}
 		return project;
 	}
 	public static IFile getSelectedFile() throws Exception
 	{
 		return getSelectedFile( getActiveWorkbenchWindow() );
 	}
 	public static IFile getSelectedFile( IWorkbenchWindow window ) throws Exception
 	{
 		if( window == null )
 			window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 		if( window == null )
 			return null;
 		IFile file = null;
 		ISelection selection = window.getSelectionService().getSelection( UnrealScriptID.VIEW_NAVIGATOR );
 		if( (selection != null) && !selection.isEmpty() )
 		{
 			if( selection instanceof TreeSelection )
 			{
 				Object[] selectedObjects = ((TreeSelection) selection).toArray();
 				ArrayList<IFile> files = new ArrayList<IFile>();
 				for( Object selectedObject : selectedObjects )
 				{
 					if( selectedObject instanceof IFile )
 					{
 						files.add( (IFile) selectedObject );
 					}
 				}
 				if( files.size() > 1 )
 				{
 					throw new Exception( "You need to select one File (in the Package Explorer), you've selected several Files." );
 				}
 				if( files.size() == 1 )
 				{
 					file = files.get( 0 );
 				}
 			}
 		}
 		if( file == null )
 		{
 			throw new Exception( "You need to select a File (in the Package Explorer)" );
 		}
 		return file;
 	}
 	public static IFolder getSelectedFolder() throws Exception
 	{
 		return getSelectedFolder( getActiveWorkbenchWindow() );
 	}
 	public static IFolder getSelectedFolder( IWorkbenchWindow window ) throws Exception
 	{
 		if( window == null )
 			window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 		if( window == null )
 			return null;
 		IFolder folder = null;
 		ISelection selection = window.getSelectionService().getSelection( UnrealScriptID.VIEW_NAVIGATOR );
 		if( (selection != null) && !selection.isEmpty() )
 		{
 			if( selection instanceof TreeSelection )
 			{
 				Object[] selectedObjects = ((TreeSelection) selection).toArray();
 				ArrayList<IFolder> folders = new ArrayList<IFolder>();
 				for( Object selectedObject : selectedObjects )
 				{
 					if( selectedObject instanceof IFolder )
 					{
 						folders.add( (IFolder) selectedObject );
 					}
 				}
 				if( folders.size() > 1 )
 				{
 					throw new Exception( "You need to select one Folder (in the Package Explorer), you've selected several Folders." );
 				}
 				if( folders.size() == 1 )
 				{
 					folder = folders.get( 0 );
 				}
 			}
 		}
 		if( folder == null )
 		{
 			throw new Exception( "You need to select a Folder (in the Package Explorer)" );
 		}
 		return folder;
 	}
 	
 	public static IProject getSelectedOrActiveProject() throws Exception
 	{
 		return getSelectedOrActiveProject( getActiveWorkbenchWindow() );
 	}
 	public static IProject getSelectedOrActiveProject( IWorkbenchWindow window ) throws Exception
 	{
 		if( window == null )
 			window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 		if( window == null )
 			return null;
 		
 		try
 		{
 			IProject project = getSelectedProject( window );
 			if( project != null )
 				return project;
 		}
 		catch( Exception e )
 		{
 		}
 		
 		try
 		{
 			IFile file = getSelectedFile( window );
 			IProject project = file.getProject();
 			if( project != null )
 				return project;
 		}
 		catch( Exception e )
 		{
 		}
 		
 		try
 		{
 			IFolder folder = getSelectedFolder( window );
 			IProject project = folder.getProject();
 			if( project != null )
 				return project;
 		}
 		catch( Exception e )
 		{
 		}
 		
 		try
 		{
 			IProject project = getActiveProject( );
 			if( project != null )
 				return project;
 		}
 		catch( Exception e )
 		{
 		}
 		
 		throw new Exception( "You need to select a Project or a File from a Project (in the Package Explorer), or you need to have a File from a Project currently open" );
 	}
 	
 	public static boolean isProjectUDK( IProject project )
 	{
 		if( project == null )
 			return false;
 		try
 		{
 			String udkImport = project.getPersistentProperty( UnrealScriptID.PROPERTY_IS_UDK_IMPORT );
 			if( (udkImport == null) || !udkImport.equalsIgnoreCase("true") )
 				return false;
 			return true;
 		}
 		catch( Exception e )
 		{
 			return false;
 		}
 	}
 	public static void testIsProjectUDK( IProject project ) throws Exception
 	{
 		if( !isProjectUDK(project) )
 			throw new Exception( "Project is not an UDK Project" );
 	}
 	
 	
 	/** 
 	 * Returns the exit value:<br>
 	 *  0 = ok<br>
 	 * -1 = failed (inside java)<br>
 	 *  1+ = process failed, number represents an error code<br>
 	 */
 	public static int runUDK( final boolean newWindow, PrintStream output, PrintStream error, final String... params )
 	{
 		ArrayList<String> collection = new ArrayList<String>();
 		for( String param : params )
 			collection.add( param );
 		return runUDK( newWindow, output, error, collection );
 	}
 	/** 
 	 * Returns the exit value:<br>
 	 *  0 = ok<br>
 	 * -1 = failed (inside java)<br>
 	 *  1+ = process failed, number represents an error code<br>
 	 */
 	public static int runUDK( final boolean newWindow, PrintStream output, PrintStream error, final Collection<String> params )
 	{
 		ArrayList<String> collection = new ArrayList<String>();
 		for( String param : params )
 			collection.add( param );
 		return runUDK( newWindow, output, error, collection );
 	}
 	/** 
 	 * Returns the exit value:<br>
 	 *  0 = ok<br>
 	 * -1 = failed (inside java)<br>
 	 *  1+ = process failed, number represents an error code<br>
 	 */
 	public static int runUDK( final boolean newWindow, PrintStream output, PrintStream error, final ArrayList<String> params )
 	{
 		if( output == null )
 			output = System.out;
 		if( error == null )
 		{
 			if( output == System.out )
 				error = System.err;
 			else
 				error = output;
 		}
 		
 		boolean bit64;
 		String root;
 		try
 		{
 			bit64 = Boolean.parseBoolean( getActiveProject().getPersistentProperty(UnrealScriptID.PROPERTY_64BIT) );
 			root = getActiveProjectFile().getAbsolutePath() + "/";
 		}
 		catch( Exception e )
 		{
 			bit64 = false;
 			root = "C:/UDK/";
 			e.printStackTrace( error );
 		}
 		String binFolder = root+"Binaries/"+(bit64?"Win64":"Win32")+"/";
 		String executablePath = binFolder+"UDK."+(newWindow?"exe":"com");
 		
 		File bin = new File( binFolder );
 		File executable = new File( executablePath );
 		if( !executable.exists() )
 		{
 			error.println( "\""+executable.getAbsolutePath()+"\" doesn't exist" );
 			return -1;
 		}
 		params.add( 0, executable.getAbsolutePath() );
 		
 		try
 		{
 			output.println( "Running: "+params.toString() );
 			Process process = Runtime.getRuntime().exec( params.toArray(new String[0]), null, bin );
 			MyStream.copy( process.getInputStream(), output );
 			MyStream.copy( process.getErrorStream(), error  );
 			return process.waitFor();
 		}
 		catch( Exception e )
 		{
 			e.printStackTrace( error );
 			return -1;
 		}
 	}
 	
 	
 	public static String[] getMapNames( IProject project )
 	{
 		String[] maps = null;
 		
 		try
 		{
 			ArrayList<String> names = new ArrayList<String>();
 			getMapNamesRecursive( names, new File(getProjectFile(project), ProjectConstant.subfolders.get("Maps")) );
 			maps = names.toArray( new String[0] );
 		}
 		catch( Exception e )
 		{
 			maps = null;
 		}
 		
 		if( (maps == null) || (maps.length == 0) )
 			maps = new String[]{ "ExampleMap", "EpicCitadel", "DM-Deck" };
 		return maps;
 	}
 	private static void getMapNamesRecursive( ArrayList<String> names, File file ) throws Exception
 	{
 		if( file.exists() )
 		{
 			if( file.isDirectory() )
 			{
 				File[] subfiles = file.listFiles();
 				if( subfiles != null )
 				{
 					for( File subfile : subfiles )
 					{
 						getMapNamesRecursive( names, subfile );
 					}
 				}
 			}
 			else
 			{
 				if( file.getAbsolutePath().endsWith(".udk") )
 				{
 					names.add( file.getName() );
 				}
 			}
 		}
 	}
 	
 	
 	@Override
 	public void dispose()
 	{
 		updateMarkersThreadRunning = false;
 		
 		synchronized( updateLaunchLogThreadSynchronizer )
 		{
 			updateLaunchLogThreadRunners--;
 			if( updateLaunchLogThreadRunners == 0 )
 			{
 				try
 				{
 					updateLaunchLogThread.join();
 					updateLaunchLogThread = null;
 				}
 				catch( Exception e )
 				{
 				}
 			}
 		}
 		
 		ColorManager.dispose();
 		super.dispose();
 	}
 }
