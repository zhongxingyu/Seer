 package com.patrick_vane.unrealscript.editor;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import org.eclipse.core.filesystem.EFS;
 import org.eclipse.core.filesystem.IFileStore;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.source.AnnotationRulerColumn;
 import org.eclipse.jface.text.source.CompositeRuler;
 import org.eclipse.jface.text.source.IAnnotationModel;
 import org.eclipse.jface.text.source.IVerticalRuler;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.TreeSelection;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.editors.text.TextEditor;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.ui.texteditor.IDocumentProvider;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.FrameworkUtil;
 import com.patrick_vane.unrealscript.editor.class_hierarchy.TypeHierarchyView;
 import com.patrick_vane.unrealscript.editor.class_hierarchy.parser.UnrealScriptClass;
 import com.patrick_vane.unrealscript.editor.class_hierarchy.parser.UnrealScriptClassHierarchyParser;
 import com.patrick_vane.unrealscript.editor.constants.ProjectConstant;
 import com.patrick_vane.unrealscript.editor.default_classes.DocumentProvider;
 import com.patrick_vane.unrealscript.editor.default_classes.MyRunnable;
 import com.patrick_vane.unrealscript.editor.default_classes.MyStream;
 import com.patrick_vane.unrealscript.editor.default_classes.WhitespaceDetector;
 import com.patrick_vane.unrealscript.editor.outline.OutlineLabelProvider;
 import com.patrick_vane.unrealscript.editor.parser.CodeException;
 import com.patrick_vane.unrealscript.editor.parser.UnrealScriptAttributeParser;
 import com.patrick_vane.unrealscript.editor.parser.UnrealScriptAttributes;
 import com.patrick_vane.unrealscript.editor.parser.UnrealScriptParser;
 
 
 public class UnrealScriptEditor extends TextEditor
 {
 	private static boolean firstInitStaticClassesCall = true;
 	
 	private final Configuration configuration;
 	
 	
 	public UnrealScriptEditor()
 	{
 		super();
 		
 		configuration = new Configuration();
 		setSourceViewerConfiguration( configuration );
 		setDocumentProvider( new DocumentProvider() );
 		
 		initializeSourceViewer();
 		updateMarkersThread.start();
 		initStaticClasses();
 		
 		try
 		{
 			PlatformUI.getWorkbench().showPerspective( UnrealScriptID.PERSPECTIVE, getActiveWorkbenchWindow() );
 		}
 		catch( Exception e )
 		{
 		}
 	}
 	
 	public static void initStaticClasses()
 	{
 		if( firstInitStaticClassesCall )
 		{
 			firstInitStaticClassesCall = false;
 			TypeHierarchyView.init();
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
 					Thread.sleep( 1000 );
 				}
 				catch( Exception e )
 				{
 				}
 				
 				try
 				{
 					updateIsDirty();
 				}
 				catch( Exception e )
 				{
 				}
 				
 				try
 				{
 					updateMarkers();
 				}
 				catch( Exception e )
 				{
 				}
 			}
 		}
 	};
 	
 	
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
 	
 	
 	// markers >>
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
 		
 		public void addErrorMarker( final int startCharacter, final int endCharacter, final String message )
 		{
 			final IFile file = getIFile( this );
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
 			final IFile file = getIFile( this );
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
 			final IFile file = getIFile( this );
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
 	// markers <<
 	
 	
 	// this class methods >>
 		@Override
 		public boolean isDirty()
 		{
 			return calculateIsDirty();
 		}
 		public boolean calculateIsDirty()
 		{
 			try
 			{
 				String currentContent = getEditorContent( this );
 				if( currentContent == null )
 					return false;
 				
 				String savedContent = getFileContent( getIFile(this) );
 				if( savedContent == null )
 					return false;
 				
 				return !savedContent.equals( currentContent );
 			}
 			catch( Exception e )
 			{
 			}
 			return false;
 		}
 		public void updateIsDirty()
 		{
 			Display.getDefault().syncExec
 			( 
 				new Runnable()
 				{
 					@Override
 					public void run()
 					{
 						firePropertyChange( PROP_DIRTY );
 					}
 				}
 			);
 		}
 		
 		@Override
 		public Object getAdapter( Class required ) // redirected to the Configuration class
 		{
 			Object adapter = configuration.getAdapter( this, required );
 			if( adapter != null )
 				return adapter;
 			return super.getAdapter( required );
 		}
 	// this class methods <<
 	
 	
 	// static methods >>
 		public static void openFile( final File file )
 		{
 			openFile( file, 0, 0 );
 		}
 		public static void openFile( final File file, final int startChar, final int endChar )
 		{
 			if( (file != null) && file.exists() )
 			{
 				Display.getDefault().syncExec
 				(
 					new Runnable()
 					{
 						@Override
 						public void run()
 						{
 							IWorkbenchPage pageTmp = null;
 							try
 							{
 								pageTmp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 							}
 							catch( Exception e )
 							{
 							}
 							final IWorkbenchPage page = pageTmp;
 							
 							if( page != null )
 							{
 								try
 								{
 									IFileStore fileStore = EFS.getLocalFileSystem().getStore( file.toURI() );
 									final IEditorPart openedFile = IDE.openEditorOnFileStore( page, fileStore );
 									if( openedFile != null )
 									{
 										new Thread()
 										{
 											@Override
 											public void run()
 											{
 												try
 												{
 													Thread.sleep( 200 );
 												}
 												catch( Exception e )
 												{
 												}
 												
 												Display.getDefault().syncExec
 												(
 													new Runnable()
 													{
 														@Override
 														public void run()
 														{
 															try
 															{
 																page.activate( openedFile );
 																HashMap map = new HashMap();
 																map.put( IMarker.CHAR_START, startChar );
 																map.put( IMarker.CHAR_END, endChar );
 																IMarker marker = getActiveIFile().createMarker( IMarker.TEXT );
 																marker.setAttributes( map );
 																IDE.openEditor( page, marker );
 																marker.delete();
 															}
 															catch( Exception e )
 															{
 																e.printStackTrace();
 															}
 														}
 													}
 												);
 											}
 										}.start();
 									}
 								}
 								catch( Exception e )
 								{
 								}
 							}
 						}
 					}
 				);
 			}
 		}
 		public static IFile fileToIFile( final File file )
 		{
 			try
 			{
 				IWorkspace workspace = ResourcesPlugin.getWorkspace();
 				IPath location = Path.fromOSString( file.getAbsolutePath() );
 				return workspace.getRoot().getFileForLocation( location );
 			}
 			catch( Exception e )
 			{
 				e.printStackTrace();
 				return null;
 			}
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
 			if( runnable.getResult() != null )
 				return (IWorkbenchWindow) runnable.getResult();
 			return null;
 		}
 		public static UnrealScriptEditor getActiveEditor()
 		{
 			IWorkbenchWindow window = getActiveWorkbenchWindow();
 			if( window == null )
 				return null;
 			IWorkbenchPage page = getActiveWorkbenchWindow().getActivePage();
 			if( page == null )
 				return null;
 			IEditorPart activeEditor = page.getActiveEditor();
 			if( activeEditor instanceof UnrealScriptEditor )
 				return (UnrealScriptEditor) activeEditor;
 			return null;
 		}
 		public static IFile getActiveIFile()
 		{
 			return getIFile( getActiveEditor() );
 		}
 		public static IFile getIFile( UnrealScriptEditor editor )
 		{
 			try
 			{
 				Object object = editor.getEditorInput().getAdapter( IFile.class );
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
 		public static File getActiveFile()
 		{
 			return getFile( getActiveEditor() );
 		}
 		public static File getFile( UnrealScriptEditor editor )
 		{
 			return getFile( getIFile(editor) );
 		}
 		public static File getFile( IFile file )
 		{
 			try
 			{
 				return file.getRawLocation().toFile();
 			}
 			catch( Exception e )
 			{
 			}
 			return null;
 		}
 		
 		public static UnrealScriptClass getActiveUnrealScriptClass()
 		{
 			return getUnrealScriptClass( getActiveClassName() );
 		}
 		public static UnrealScriptClass getUnrealScriptClass( IFile file )
 		{
 			if( TypeHierarchyView.isRootFromThisProject() )
 			{
 				try
 				{
					return TypeHierarchyView.getClasses().get( file.getName().toLowerCase().replaceAll(".uc", "") );
 				}
 				catch( Exception e )
 				{
 				}
 			}
 			return null;
 		}
 		public static UnrealScriptClass getUnrealScriptClass( File file )
 		{
 			if( TypeHierarchyView.isRootFromThisProject() )
 			{
 				try
 				{
					return TypeHierarchyView.getClasses().get( file.getName().toLowerCase().replaceAll(".uc", "") );
 				}
 				catch( Exception e )
 				{
 				}
 			}
 			return null;
 		}
 		public static UnrealScriptClass getUnrealScriptClass( String name )
 		{
 			if( TypeHierarchyView.isRootFromThisProject() )
 			{
 				try
 				{
					return TypeHierarchyView.getClasses().get( name.toLowerCase().replaceAll(".uc", "") );
 				}
 				catch( Exception e )
 				{
 				}
 			}
 			return null;
 		}
 		
 		public static UnrealScriptAttributes getActiveUnrealScriptAttributes()
 		{
 			return getUnrealScriptAttributes( getActiveClassName() );
 		}
 		public static UnrealScriptAttributes getUnrealScriptAttributes( String className )
 		{
 			return UnrealScriptAttributeParser.parseAttributesOfClass( className );
 		}
 		public static UnrealScriptAttributes getActiveUnrealScriptAttributesWithParents()
 		{
 			return getUnrealScriptAttributesWithParents( getActiveClassName() );
 		}
 		public static UnrealScriptAttributes getUnrealScriptAttributesWithParents( String className )
 		{
 			return UnrealScriptAttributeParser.parseAttributesOfClassAndParents( className );
 		}
 		public static String getActiveClassName()
 		{
 			return getClassName( getActiveEditorContent() );
 		}
 		public static String getClassName( File file ) throws IOException
 		{
 			return getClassName( getFileContent(file) );
 		}
 		public static String getClassName( IFile file ) throws IOException
 		{
 			return getClassName( getFileContent(file) );
 		}
 		public static String getClassName( String classContent )
 		{
 			return UnrealScriptClassHierarchyParser.getClassName( classContent );
 		}
 		
 		public static IDocument getActiveEditorDocument()
 		{
 			return getEditorDocument( getActiveEditor() );
 		}
 		public static IDocument getEditorDocument( UnrealScriptEditor editor )
 		{
 			if( editor == null )
 				return null;
 			IEditorInput input = editor.getEditorInput();
 			IDocumentProvider provider = editor.getDocumentProvider();
 			if( (input == null) || (provider == null) )
 				return null;
 			return provider.getDocument( input );
 		}
 		public static String getActiveEditorContent()
 		{
 			return getEditorContent( getActiveEditor() );
 		}
 		public static String getEditorContent( UnrealScriptEditor editor )
 		{
 			IDocument doc = getEditorDocument( editor );
 			if( doc == null )
 				return null;
 			return doc.get();
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
 				IFile file = getActiveIFile();
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
 		public static File getSelectedOrActiveProjectFile()
 		{
 			try
 			{
 				return getProjectFile( getSelectedOrActiveProject() );
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
 				throw new Exception( "No workbench window available" );
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
 				window = getActiveWorkbenchWindow();
 			if( window == null )
 				throw new Exception( "No workbench window available" );
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
 				window = getActiveWorkbenchWindow();
 			if( window == null )
 				throw new Exception( "No workbench window available" );
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
 				window = getActiveWorkbenchWindow();
 			if( window == null )
 				throw new Exception( "No workbench window available" );
 			
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
 		
 		public static IProject getSelectedOrActiveUDKProject() throws Exception
 		{
 			return getSelectedOrActiveUDKProject( getActiveWorkbenchWindow() );
 		}
 		public static IProject getSelectedOrActiveUDKProject( IWorkbenchWindow window ) throws Exception
 		{
 			if( window == null )
 				window = getActiveWorkbenchWindow();
 			if( window == null )
 				throw new Exception( "No workbench window available" );
 			
 			try
 			{
 				IProject project = getSelectedProject( window );
 				if( isProjectUDK(project) )
 					return project;
 			}
 			catch( Exception e )
 			{
 			}
 			
 			try
 			{
 				IFile file = getSelectedFile( window );
 				IProject project = file.getProject();
 				if( isProjectUDK(project) )
 					return project;
 			}
 			catch( Exception e )
 			{
 			}
 			
 			try
 			{
 				IFolder folder = getSelectedFolder( window );
 				IProject project = folder.getProject();
 				if( isProjectUDK(project) )
 					return project;
 			}
 			catch( Exception e )
 			{
 			}
 			
 			try
 			{
 				IProject project = getActiveProject( );
 				if( isProjectUDK(project) )
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
 		
 		
 		/** 
 		 * Returns the exit value:<br>
 		 *  0 = ok<br>
 		 * -1 = failed (inside java)<br>
 		 *  1+ = process failed, number represents an error code<br>
 		 */
 		public static int runUDK( final IProject project, boolean exe, PrintStream output, PrintStream error, final String... params )
 		{
 			ArrayList<String> collection = new ArrayList<String>();
 			for( String param : params )
 				collection.add( param );
 			return runUDK( project, exe, output, error, collection );
 		}
 		/** 
 		 * Returns the exit value:<br>
 		 *  0 = ok<br>
 		 * -1 = failed (inside java)<br>
 		 *  1+ = process failed, number represents an error code<br>
 		 */
 		public static int runUDK( final IProject project, boolean exe, PrintStream output, PrintStream error, final Collection<String> params )
 		{
 			ArrayList<String> collection = new ArrayList<String>();
 			for( String param : params )
 				collection.add( param );
 			return runUDK( project, exe, output, error, collection );
 		}
 		/** 
 		 * Returns the exit value:<br>
 		 *  0 = ok<br>
 		 * -1 = failed (inside java)<br>
 		 *  1+ = process failed, number represents an error code<br>
 		 */
 		public static int runUDK( final IProject project, boolean exe, PrintStream output, PrintStream error, final ArrayList<String> params )
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
 				bit64 = Boolean.parseBoolean( project.getPersistentProperty(UnrealScriptID.PROPERTY_64BIT) );
 				root = getProjectFile(project).getAbsolutePath() + "/";
 			}
 			catch( Exception e )
 			{
 				bit64 = false;
 				root = "C:/UDK/";
 				e.printStackTrace( error );
 			}
 			String binFolder = root+"Binaries/"+(bit64?"Win64":"Win32")+"/";
 			String executablePath = binFolder;
 			if( exe )
 				executablePath += "UDK.exe";
 			else
 				executablePath += "UDK.com";
 			
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
 				if( !params.contains("server") )
 				{
 					MyStream.copy( process.getInputStream(), output );
 					MyStream.copy( process.getErrorStream(), error  );
 				}
 				return process.waitFor();
 			}
 			catch( Exception e )
 			{
 				e.printStackTrace( error );
 				return -1;
 			}
 		}
 		
 		private static final String PACKAGE_FOLDER_TEXT;
 		static
 		{
 			String packageFolderText = ProjectConstant.subfolders.get( "UnrealScript" );
 			if( packageFolderText == null )
 				packageFolderText = "Development/Src";
 			PACKAGE_FOLDER_TEXT = packageFolderText;
 		}
 		public static String getPackageName( File file )
 		{
 			try
 			{
 				return getPackageName( file.getAbsolutePath() );
 			}
 			catch( Exception e )
 			{
 				return null;
 			}
 		}
 		public static String getPackageName( String filePath )
 		{
 			try
 			{
 				String path = filePath.replaceAll( "\\\\", "/" );
 				String packageName = path.substring( path.indexOf(PACKAGE_FOLDER_TEXT)+PACKAGE_FOLDER_TEXT.length()+1 );
 				return packageName.substring( 0, packageName.indexOf("/") );
 			}
 			catch( Exception e )
 			{
 				return null;
 			}
 		}
 		
 		public static String getFolderName( File file )
 		{
 			try
 			{
 				return getFolderName( file.getAbsolutePath() );
 			}
 			catch( Exception e )
 			{
 				return null;
 			}
 		}
 		public static String getFolderName( String filePath )
 		{
 			try
 			{
 				String path = filePath.replaceAll( "\\\\", "/" );
 				String[] parts = path.split( "/" );
 				return parts[parts.length-2];
 			}
 			catch( Exception e )
 			{
 				return null;
 			}
 		}
 		
 		
 		public static UnrealScriptClass[] getSubClasses( String unrealscriptClassName )
 		{
 			return getSubClasses( getUnrealScriptClass(unrealscriptClassName) );
 		}
 		public static UnrealScriptClass[] getSubClasses( String unrealscriptClassName, boolean addCurrentToo )
 		{
 			return getSubClasses( getUnrealScriptClass(unrealscriptClassName), addCurrentToo );
 		}
 		
 		public static UnrealScriptClass[] getSubClasses( UnrealScriptClass unrealscriptClass )
 		{
 			return getSubClasses( unrealscriptClass, false );
 		}
 		public static UnrealScriptClass[] getSubClasses( UnrealScriptClass unrealscriptClass, boolean addCurrentToo )
 		{
 			UnrealScriptClass[] classes = null;
 			
 			try
 			{
 				ArrayList<UnrealScriptClass> unrealscriptClasses = new ArrayList<UnrealScriptClass>();
 				if( addCurrentToo && (unrealscriptClass != null) )
 					unrealscriptClasses.add( unrealscriptClass );
 				getSubClassesRecursive( unrealscriptClasses, unrealscriptClass );
 				classes = unrealscriptClasses.toArray( new UnrealScriptClass[0] );
 			}
 			catch( Exception e )
 			{
 				classes = null;
 			}
 			
 			if( classes == null )
 			{
 				ArrayList<UnrealScriptClass> unrealscriptClasses = new ArrayList<UnrealScriptClass>();
 				if( addCurrentToo && (unrealscriptClass != null) )
 					unrealscriptClasses.add( unrealscriptClass );
 				classes = unrealscriptClasses.toArray( new UnrealScriptClass[0] );
 			}
 			return classes;
 		}
 		private static void getSubClassesRecursive( ArrayList<UnrealScriptClass> classes, UnrealScriptClass unrealscriptClass ) throws Exception
 		{
 			if( unrealscriptClass != null )
 			{
 				for( UnrealScriptClass subclass : unrealscriptClass.getChilds() )
 				{
 					classes.add( subclass );
 					getSubClassesRecursive( classes, subclass );
 				}
 			}
 		}
 		
 		
 		public static String[] getClassesNames( UnrealScriptClass[] unrealscriptClasses )
 		{
 			ArrayList<String> names = new ArrayList<String>();
 			for( UnrealScriptClass unrealscriptClass : unrealscriptClasses )
 			{
 				names.add( unrealscriptClass.getName() );
 			}
 			return names.toArray( new String[0] );
 		}
 		public static String[] getClassesAndPackageNames( UnrealScriptClass[] unrealscriptClasses )
 		{
 			ArrayList<String> names = new ArrayList<String>();
 			for( UnrealScriptClass unrealscriptClass : unrealscriptClasses )
 			{
 				names.add( getPackageName(unrealscriptClass.getFile())+"."+unrealscriptClass.getName() );
 			}
 			Collections.sort( names );
 			return names.toArray( new String[0] );
 		}
 		
 		
 		public static String[] getMapNames( IProject project )
 		{
 			String[] maps = null;
 			
 			try
 			{
 				ArrayList<String> names = new ArrayList<String>();
 				getMapNamesRecursive( names, new File(getProjectFile(project), ProjectConstant.subfolders.get("Maps")) );
 				Collections.sort( names );
 				maps = names.toArray( new String[0] );
 			}
 			catch( Exception e )
 			{
 				maps = null;
 			}
 			
 			if( (maps == null) || (maps.length == 0) )
 				maps = new String[]{ "EpicCitadel", "ExampleMap", "DM-Deck" };
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
 						names.add( getFolderName(file)+"."+file.getName().replaceAll(".udk", "") );
 					}
 				}
 			}
 		}
 		
 		private static final HashMap<String,FileCache> fileCache = new HashMap<String,FileCache>();
 		private static class FileCache
 		{
 			public final long lastModified;
 			public final String data;
 			public FileCache( long lastChangedTime, String data )
 			{
 				this.lastModified = lastChangedTime;
 				this.data = data;
 			}
 		}
 		public static String getFileContent( IFile file ) throws IOException
 		{
 			return getFileContent( getFile(file) );
 		}
 		public static String getFileContent( File file ) throws IOException
 		{
 			if( file == null )
 				return "";
 			
 			String data = null;
 			
 			FileCache fCache = fileCache.get( file.getAbsolutePath() );
 			if( (fCache != null) && (file.lastModified() == fCache.lastModified) )
 				data = fCache.data;
 			
 			if( data == null )
 			{
 				StringBuilder buffer = new StringBuilder();
 				FileInputStream inputStream = null;
 				try
 				{
 					inputStream = new FileInputStream( file );
 					char current;
 					while( inputStream.available() > 0 )
 					{
 						current = (char) inputStream.read();
 						buffer.append( current );
 					}
 				}
 				finally
 				{
 					try
 					{
 						if( inputStream != null )
 						{
 							inputStream.close();
 						}
 					}
 					catch( Exception e )
 					{
 					}
 				}
 				data = buffer.toString();
 				fileCache.put( file.getAbsolutePath(), new FileCache(file.lastModified(), data) );
 			}
 			
 			if( data == null )
 				data = "";
 			
 			return data;
 		}
 		
 		public static String getCode( String text )
 		{
 			StringBuilder builder = new StringBuilder();
 			boolean stringOpen = false;
 			boolean charOpen   = false;
 			int docsOpen = 0;
 			boolean previousWasSlash = false;
 			boolean previousWasStar  = false;
 			boolean skipTillNewline  = false;
 			for( int i=0; i<text.length(); i++ )
 			{
 				char c = text.charAt( i );
 				
 				if( skipTillNewline )
 				{
 					if( (c != '\n') && (c != '\r') )
 						continue;
 					skipTillNewline = false;
 				}
 				
 				if( stringOpen )
 				{
 					if( c == '"' )
 						stringOpen = false;
 				}
 				else if( charOpen )
 				{
 					if( c == '\'' )
 						charOpen = false;
 				}
 				else
 				{
 					if( docsOpen <= 0 )
 					{
 						if( c == '"' )
 							stringOpen = true;
 						else if( c == '\'' )
 							charOpen = true;
 						builder.append( c );
 					}
 					
 					if( c == '/' )
 					{
 						if( previousWasStar )
 						{
 							docsOpen--;
 							previousWasSlash = false;
 							previousWasStar  = false;
 							continue;
 						}
 						if( previousWasSlash )
 						{
 							skipTillNewline  = true;
 							builder.append( '\n' );
 							previousWasSlash = false;
 							previousWasStar  = false;
 							continue;
 						}
 					}
 					else if( c == '*' )
 					{
 						if( previousWasSlash )
 						{
 							docsOpen++;
 							previousWasSlash = false;
 							previousWasStar  = false;
 							continue;
 						}
 					}
 					
 					previousWasSlash = (c == '/');
 					previousWasStar  = (c == '*');
 				}
 			}
 			return builder.toString();
 		}
 		public static String getCodeLine( String code, int offset )
 		{
 			while( offset > 0 )
 			{
 				char character = code.charAt( offset );
 				if( (character == '\n') || (character == '\r') )
 				{
 					offset++;
 					break;
 				}
 				offset--;
 			}
 			
 			StringBuffer buffer = new StringBuffer();
 			while( offset < code.length() )
 			{
 				char character = code.charAt( offset );
 				if( (character == '\n') || (character == '\r') )
 				{
 					break;
 				}
 				buffer.append( character );
 				offset++;
 			}
 			
 			return buffer.toString();
 		}
 		public static String[] getCodeWords( String code )
 		{
 			ArrayList<String> words = new ArrayList<String>();
 			StringBuffer buffer = new StringBuffer();
 			for( int i=0; i<code.length(); i++ )
 			{
 				char c = code.charAt( i );
 				if( WhitespaceDetector.getSharedInstance().isWhitespace(c) )
 				{
 					if( buffer.length() > 0 )
 					{
 						words.add( buffer.toString() );
 						buffer = new StringBuffer();
 					}
 				}
 				else
 				{
 					buffer.append( c );
 				}
 			}
 			if( buffer.length() > 0 )
 			{
 				words.add( buffer.toString() );
 			}
 			return words.toArray( new String[0] );
 		}
 		
 		public static ImageDescriptor getImageDescriptor( String file )
 		{
 			Bundle bundle = FrameworkUtil.getBundle( OutlineLabelProvider.class );
 			URL url = FileLocator.find( bundle, new Path("icons/" + file), null );
 			return ImageDescriptor.createFromURL( url );
 		}
 		
 		public static IWorkspaceRoot getRoot()
 		{
 			return ResourcesPlugin.getWorkspace().getRoot();
 		}
 	// static methods <<
 	
 	
 	@Override
 	public void dispose()
 	{
 		updateMarkersThreadRunning = false;
 		super.dispose();
 	}
 }
