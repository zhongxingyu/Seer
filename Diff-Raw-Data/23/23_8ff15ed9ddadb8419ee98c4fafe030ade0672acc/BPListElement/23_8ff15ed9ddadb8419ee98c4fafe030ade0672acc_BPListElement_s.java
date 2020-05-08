 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.ui.wizards.buildpath;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.compiler.util.Util;
 import org.eclipse.dltk.core.BuildpathContainerInitializer;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKModelUtil;
 import org.eclipse.dltk.core.IAccessRule;
 import org.eclipse.dltk.core.IBuildpathAttribute;
 import org.eclipse.dltk.core.IBuildpathContainer;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IDLTKProject;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.internal.core.BuildpathEntry;
 import org.eclipse.dltk.launching.ScriptRuntime;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 
 
 public class BPListElement
 {
 	
 	public static final String EXCLUSION = "exclusion"; //$NON-NLS-1$
 
 	public static final String INCLUSION = "inclusion"; //$NON-NLS-1$
 
 	public static final String ACCESSRULES = "accessrules"; //$NON-NLS-1$
 
 	public static final String COMBINE_ACCESSRULES = "combineaccessrules"; //$NON-NLS-1$
 
 	 public static final String NATIVE_LIB_PATH= ScriptRuntime.BUILDPATH_ATTR_LIBRARY_PATH_ENTRY;
 
 	private IDLTKProject fProject;
 
 	private int fEntryKind;
 
 	private IPath fPath, fOrginalPath;
 
 	private IResource fResource;
 
 	private boolean fIsExported;
 	
 	private boolean fExternal;
 
 	private boolean fIsMissing;
 
 	private Object fParentContainer;
 
 	private IBuildpathEntry fCachedEntry;
 
 	private ArrayList fChildren;
 
 	private IPath fLinkTarget, fOrginalLinkTarget;
 
 	public BPListElement( IDLTKProject project, int entryKind, IPath path, IResource res, boolean external ) {
 
 		this( null, project, entryKind, path, res, external );
 	}
 
 	public BPListElement( Object parent, IDLTKProject project, int entryKind, IPath path, IResource res, boolean external ) {
 
 		this( parent, project, entryKind, path, res, null, external );
 	}
 
 	public BPListElement( IDLTKProject project, int entryKind, boolean external ) {
 
 		this( null, project, entryKind, null, null, external );
 	}
 
 	public BPListElement( Object parent, IDLTKProject project, int entryKind, IPath path, IResource res, IPath linkTarget, boolean external ) {
 
 		fProject = project;
 
 		fEntryKind = entryKind;
 		fPath = path;
 		fOrginalPath = path;
 		fLinkTarget = linkTarget;
 		fOrginalLinkTarget = linkTarget;
 		fChildren = new ArrayList( );
 		fResource = res;
 		fIsExported = false;
 
 		fIsMissing = false;
 		fCachedEntry = null;
 		fParentContainer = parent;
 		fExternal = external;
 
 		switch( entryKind ) {
 			case IBuildpathEntry.BPE_SOURCE:
 				createAttributeElement( INCLUSION, new Path [ 0 ], true );
 				createAttributeElement( EXCLUSION, new Path [ 0 ], true );
 				if( DLTKCore.DEBUG ) {
 					System.err.println( "TODO: Add adding containers for languages here" );
 					// createAttributeElement(NATIVE_LIB_PATH, null, false);
 				}
 				break;
 			case IBuildpathEntry.BPE_LIBRARY:
 				createAttributeElement( ACCESSRULES, new IAccessRule [ 0 ], true );
 				break;
 			case IBuildpathEntry.BPE_PROJECT:
 				createAttributeElement( ACCESSRULES, new IAccessRule [ 0 ], true );
 				createAttributeElement( COMBINE_ACCESSRULES, Boolean.FALSE, true ); // not rendered
 				
 				if( DLTKCore.DEBUG ) {
 					System.err.println( "TODO: Add adding containers for languages here" );
 					// createAttributeElement(NATIVE_LIB_PATH, null, false);
 				}
 				break;
 			case IBuildpathEntry.BPE_CONTAINER:
 				createAttributeElement( ACCESSRULES, new IAccessRule [ 0 ], true );
 				try {
 					IBuildpathContainer container = DLTKCore.getBuildpathContainer( fPath, fProject );
 					if( container != null ) {
 						IBuildpathEntry[] entries = container.getBuildpathEntries( );
 						for( int i = 0; i < entries.length; i++ ) {
 							IBuildpathEntry entry = entries[ i ];
 							if( entry != null ) {
 								BPListElement curr = createFromExisting( this, entry, fProject );
 								fChildren.add( curr );
 							}
 							else {
 								DLTKUIPlugin.logErrorMessage( "Null entry in container '" + fPath + "'" ); //$NON-NLS-1$//$NON-NLS-2$
 							}
 						}
 					}
 				}
 				catch( ModelException e ) {
 				}
 				//createAttributeElement( NATIVE_LIB_PATH, null, false );
 				if( DLTKCore.DEBUG ) {
 					System.err.println( "TODO: Add adding containers for languages here" );
 					// createAttributeElement(NATIVE_LIB_PATH, null, false);
 				}
 				break;
 			default:
 		}
 
 	}
 
 	public IBuildpathEntry getBuildpathEntry( ) {
 
 		if( fCachedEntry == null ) {
 			fCachedEntry = newBuildpathEntry( );
 		}
 		return fCachedEntry;
 	}
 
 	private IBuildpathAttribute[] getBuildpathAttributes( ) {
 
 		ArrayList res = new ArrayList( );
 		for( int i = 0; i < fChildren.size( ); i++ ) {
 			Object curr = fChildren.get( i );
 			if( curr instanceof BPListElementAttribute ) {
 				BPListElementAttribute elem = ( BPListElementAttribute )curr;
 				if( !elem.isBuiltIn( ) && elem.getValue( ) != null ) {
 					res.add( elem.newBuildpathAttribute( ) );
 				}
 			}
 		}
 		return ( IBuildpathAttribute[] )res.toArray( new IBuildpathAttribute [ res.size( ) ] );
 	}
 
 	private IBuildpathEntry newBuildpathEntry( ) {
 
 		IBuildpathAttribute[] extraAttributes = getBuildpathAttributes( );
 		switch( fEntryKind ) {
 			case IBuildpathEntry.BPE_SOURCE:
 				IPath[] inclusionPattern = ( IPath[] )getAttribute( INCLUSION );
 				IPath[] exclusionPattern = ( IPath[] )getAttribute( EXCLUSION );				
 				return DLTKCore.newSourceEntry( fPath, inclusionPattern, exclusionPattern, extraAttributes );
 			case IBuildpathEntry.BPE_LIBRARY: {				
 				IAccessRule[] accesRules = ( IAccessRule[] )getAttribute( ACCESSRULES );
				if( fPath.equals(IBuildpathEntry.BUILTIN_EXTERNAL_ENTRY)) {
 					return DLTKCore.newBuiltinEntry( fPath,  accesRules, extraAttributes, new IPath[0], BuildpathEntry.INCLUDE_ALL, isExported( ), fExternal );
 				}
 				else {
 					return DLTKCore.newLibraryEntry( fPath,  accesRules, extraAttributes, isExported( ), fExternal );
 				}
 			}
 			case IBuildpathEntry.BPE_PROJECT: {
 				IAccessRule[] accesRules = ( IAccessRule[] )getAttribute( ACCESSRULES );
 				boolean combineAccessRules = ( ( Boolean )getAttribute( COMBINE_ACCESSRULES ) ).booleanValue( );
 				return DLTKCore.newProjectEntry( fPath, accesRules, combineAccessRules, extraAttributes, isExported( ) );
 			}
 			case IBuildpathEntry.BPE_CONTAINER: {
 				IAccessRule[] accesRules = ( IAccessRule[] )getAttribute( ACCESSRULES );
 				return DLTKCore.newContainerEntry( fPath, accesRules, extraAttributes, isExported( ) );
 			}			
 			default:
 				return null;
 		}
 	}
 
 	/**
 	 * Gets the class path entry path.
 	 * 
 	 * @see IBuildpathEntry#getPath()
 	 */
 	public IPath getPath( ) {
 
 		return fPath;
 	}
 
 	/**
 	 * Gets the class path entry kind.
 	 * 
 	 * @see IBuildpathEntry#getEntryKind()
 	 */
 	public int getEntryKind( ) {
 
 		return fEntryKind;
 	}
 
 	/**
 	 * Entries without resource are either non existing or a variable entry External archives do not have a resource
 	 */
 	public IResource getResource( ) {
 
 		return fResource;
 	}
 
 	public BPListElementAttribute setAttribute( String key, Object value ) {
 
 		BPListElementAttribute attribute = findAttributeElement( key );
 		if( attribute == null ) {
 			return null;
 		}
 		if( key.equals( EXCLUSION ) || key.equals( INCLUSION ) ) {
 			Assert.isTrue( value != null || fEntryKind != IBuildpathEntry.BPE_SOURCE );
 		}
 
 		if( key.equals( ACCESSRULES ) ) {
 			Assert.isTrue( value != null || fEntryKind == IBuildpathEntry.BPE_SOURCE );
 		}
 		if( key.equals( COMBINE_ACCESSRULES ) ) {
 			Assert.isTrue( value instanceof Boolean );
 		}
 
 		attribute.setValue( value );
 		attributeChanged( key );
 		return attribute;
 	}
 
 	public boolean addToExclusions( IPath path ) {
 
 		String key = BPListElement.EXCLUSION;
 		return addFilter( path, key );
 	}
 
 	public boolean addToInclusion( IPath path ) {
 
 		String key = BPListElement.INCLUSION;
 		return addFilter( path, key );
 	}
 
 	public boolean removeFromExclusions( IPath path ) {
 
 		String key = BPListElement.EXCLUSION;
 		return removeFilter( path, key );
 	}
 
 	public boolean removeFromInclusion( IPath path ) {
 
 		String key = BPListElement.INCLUSION;
 		return removeFilter( path, key );
 	}
 
 	private boolean addFilter( IPath path, String key ) {
 
 		IPath[] exclusionFilters = ( IPath[] )getAttribute( key );
 		if( !DLTKModelUtil.isExcludedPath( path, exclusionFilters ) ) {
 			IPath pathToExclude = path.removeFirstSegments( getPath( ).segmentCount( ) ).addTrailingSeparator( );
 			IPath[] newExclusionFilters = new IPath [ exclusionFilters.length + 1 ];
 			System.arraycopy( exclusionFilters, 0, newExclusionFilters, 0, exclusionFilters.length );
 			newExclusionFilters[ exclusionFilters.length ] = pathToExclude;
 			setAttribute( key, newExclusionFilters );
 			return true;
 		}
 		return false;
 	}
 
 	private boolean removeFilter( IPath path, String key ) {
 
 		IPath[] exclusionFilters = ( IPath[] )getAttribute( key );
 		IPath pathToExclude = path.removeFirstSegments( getPath( ).segmentCount( ) ).addTrailingSeparator( );
 		if( DLTKModelUtil.isExcludedPath( pathToExclude, exclusionFilters ) ) {
 
 			List l = new ArrayList( Arrays.asList( exclusionFilters ) );
 			l.remove( pathToExclude );
 			IPath[] newExclusionFilters = ( IPath[] )l.toArray( new IPath [ l.size( ) ] );
 			setAttribute( key, newExclusionFilters );
 			return true;
 		}
 		return false;
 	}
 
 	public BPListElementAttribute findAttributeElement( String key ) {
 
 		for( int i = 0; i < fChildren.size( ); i++ ) {
 			Object curr = fChildren.get( i );
 			if( curr instanceof BPListElementAttribute ) {
 				BPListElementAttribute elem = ( BPListElementAttribute )curr;
 				if( key.equals( elem.getKey( ) ) ) {
 					return elem;
 				}
 			}
 		}
 		return null;
 	}
 
 	public Object getAttribute( String key ) {
 
 		BPListElementAttribute attrib = findAttributeElement( key );
 		if( attrib != null ) {
 			return attrib.getValue( );
 		}
 		return null;
 	}
 
 	private void createAttributeElement( String key, Object value, boolean builtIn ) {
 
 		fChildren.add( new BPListElementAttribute( this, key, value, builtIn ) );
 	}
 
 	private static boolean isFiltered( Object entry, String[] filteredKeys ) {
 
 		if( entry instanceof BPListElementAttribute ) {
 			String key = ( ( BPListElementAttribute )entry ).getKey( );
 			for( int i = 0; i < filteredKeys.length; i++ ) {
 				if( key.equals( filteredKeys[ i ] ) ) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	private Object[] getFilteredChildren( String[] filteredKeys ) {
 
 		int nChildren = fChildren.size( );
 		ArrayList res = new ArrayList( nChildren );
 
 		for( int i = 0; i < nChildren; i++ ) {
 			Object curr = fChildren.get( i );
 			if( !isFiltered( curr, filteredKeys ) ) {
 				res.add( curr );
 			}
 		}
 		return res.toArray( );
 	}
 
 	public Object[] getChildren(  ) {
 		
 		if( fParentContainer instanceof BPListElement ) {
 			if(DLTKCore.DEBUG) {
 				System.err.println("TODO:Add navive library containers support");
 			}
 //			IPath InterpreterEnvironmentContainerPath = new Path( ScriptRuntime.InterpreterEnvironment_CONTAINER );
 //			if( InterpreterEnvironmentContainerPath.isPrefixOf( ( ( CPListElement )fParentContainer ).getPath( ) ) ) {
 //				// don't show access rules and native path for containers (bug 98710)
 //				return getFilteredChildren( new String [] { ACCESSRULES, COMBINE_ACCESSRULES, NATIVE_LIB_PATH } );
 //			}
 		}
 		if( fEntryKind == IBuildpathEntry.BPE_PROJECT ) {
 			return getFilteredChildren( new String [] { COMBINE_ACCESSRULES } );
 		}
 		return fChildren.toArray( );
 	}
 
 	public Object getParentContainer( ) {
 
 		return fParentContainer;
 	}
 
 	private void attributeChanged( String key ) {
 
 		fCachedEntry = null;
 	}
 
 	private boolean canUpdateContainer( ) {
 
 		if( fEntryKind == IBuildpathEntry.BPE_CONTAINER && fProject != null ) {
 			BuildpathContainerInitializer initializer = DLTKCore.getBuildpathContainerInitializer( fPath.segment( 0 ) );
 			return ( initializer != null && initializer.canUpdateBuildpathContainer( fPath, fProject ) );
 		}
 		return false;
 	}
 
 	public boolean isInNonModifiableContainer( ) {
 
 		if( fParentContainer instanceof BPListElement ) {
 			return ! ( ( BPListElement )fParentContainer ).canUpdateContainer( );
 		}
 		return false;
 	}
 
 	/*
 	 * @see Object#equals(java.lang.Object)
 	 */
 	public boolean equals( Object other ) {
 
 		if( other != null && other.getClass( ).equals( getClass( ) ) ) {
 			BPListElement elem = ( BPListElement )other;
 			return getBuildpathEntry( ).equals( elem.getBuildpathEntry( ) );
 		}
 		return false;
 	}
 
 	/*
 	 * @see Object#hashCode()
 	 */
 	public int hashCode( ) {
 
 		return fPath.hashCode( ) + fEntryKind;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString( ) {
 
 		return getBuildpathEntry( ).toString( );
 	}
 
 	/**
 	 * Returns if a entry is missing.
 	 * 
 	 * @return Returns a boolean
 	 */
 	public boolean isMissing( ) {
 
 		return fIsMissing;
 	}
 
 	/**
 	 * Sets the 'missing' state of the entry.
 	 */
 	public void setIsMissing( boolean isMissing ) {
 
 		fIsMissing = isMissing;
 	}
 
 	/**
 	 * Returns if a entry is exported (only applies to libraries)
 	 * 
 	 * @return Returns a boolean
 	 */
 	public boolean isExported( ) {
 
 		return fIsExported;
 	}
 
 	/**
 	 * Sets the export state of the entry.
 	 */
 	public void setExported( boolean isExported ) {
 
 		if( isExported != fIsExported ) {
 			fIsExported = isExported;
 
 			attributeChanged( null );
 		}
 	}
 
 	/**
 	 * Gets the project.
 	 * 
 	 * @return Returns a IDLTKProject
 	 */
 	public IDLTKProject getDLTKProject( ) {
 
 		return fProject;
 	}
 
 	public static BPListElement createFromExisting( IBuildpathEntry curr, IDLTKProject project ) {
 
 		return createFromExisting( null, curr, project );
 	}
 
 	public static BPListElement createFromExisting( Object parent, IBuildpathEntry curr, IDLTKProject project ) {
 
 		IPath path = curr.getPath( );
 		IWorkspaceRoot root = ResourcesPlugin.getWorkspace( ).getRoot( );
 
 		// get the resource
 		IResource res = null;
 		boolean isMissing = false;
 		IPath linkTarget = null;
 		boolean isExternel = false;
 
 		switch( curr.getEntryKind( ) ) {
 			case IBuildpathEntry.BPE_CONTAINER:
 				res = null;
 				try {
 					isMissing = project != null && ( DLTKCore.getBuildpathContainer( path, project ) == null );
 				}
 				catch( ModelException e ) {
 					isMissing = true;
 				}
 				break;			
 			case IBuildpathEntry.BPE_LIBRARY:
 				res = root.findMember( path );
 				isExternel = curr.isExternal();
 				if( res == null ) {
 					if( !ArchiveFileFilter.isArchivePath( path ) ) {
 						if( root.getWorkspace( ).validatePath( path.toString( ), IResource.FOLDER ).isOK( ) && root.getProject( path.segment( 0 ) ).exists( ) ) {
 							res = root.getFolder( path );
 						}
 					}
 					if( isExternel ) {
 						File file = new File(path.toOSString());
 						if( file == null || !file.exists()) {
 							isMissing = true;
 						}
 					}
 					else {
 						isMissing = !path.toFile( ).isFile( ); // look for external archives
 					}
 				}
 				else if( res.isLinked( ) ) {
 					linkTarget = res.getLocation( );
 				}
 				break;
 			case IBuildpathEntry.BPE_SOURCE:
 				path = path.removeTrailingSeparator( );
 				res = root.findMember( path );
 				if( res == null ) {
 					if( root.getWorkspace( ).validatePath( path.toString( ), IResource.FOLDER ).isOK( ) ) {
 						res = root.getFolder( path );
 					}
 					isMissing = true;
 				}
 				else if( res.isLinked( ) ) {
 					linkTarget = res.getLocation( );
 				}
 				break;
 			case IBuildpathEntry.BPE_PROJECT:
 				res = root.findMember( path );
 				isMissing = ( res == null );
 				break;
 		}
 		BPListElement elem = new BPListElement( parent, project, curr.getEntryKind( ), path, res, linkTarget, isExternel );
 		elem.setExported( curr.isExported( ) );		
 		elem.setAttribute( EXCLUSION, curr.getExclusionPatterns( ) );
 		elem.setAttribute( INCLUSION, curr.getInclusionPatterns( ) );
 		elem.setAttribute( ACCESSRULES, curr.getAccessRules( ) );
 		elem.setAttribute( COMBINE_ACCESSRULES, new Boolean( curr.combineAccessRules( ) ) );
 
 		IBuildpathAttribute[] extraAttributes = curr.getExtraAttributes( );
 		for( int i = 0; i < extraAttributes.length; i++ ) {
 			IBuildpathAttribute attrib = extraAttributes[ i ];
 			elem.setAttribute( attrib.getName( ), attrib.getValue( ) );
 		}
 
 		if( project != null && project.exists( ) ) {
 			elem.setIsMissing( isMissing );
 		}
 		return elem;
 	}
 
 	public static StringBuffer appendEncodePath( IPath path, StringBuffer buf ) {
 
 		if( path != null ) {
 			String str = path.toString( );
 			buf.append( '[' ).append( str.length( ) ).append( ']' ).append( str );
 		}
 		else {
 			buf.append( '[' ).append( ']' );
 		}
 		return buf;
 	}
 
 	public static StringBuffer appendEncodedString( String str, StringBuffer buf ) {
 
 		if( str != null ) {
 			buf.append( '[' ).append( str.length( ) ).append( ']' ).append( str );
 		}
 		else {
 			buf.append( '[' ).append( ']' );
 		}
 		return buf;
 	}
 
 	public static StringBuffer appendEncodedFilter( IPath[] filters, StringBuffer buf ) {
 
 		if( filters != null ) {
 			buf.append( '[' ).append( filters.length ).append( ']' );
 			for( int i = 0; i < filters.length; i++ ) {
 				appendEncodePath( filters[ i ], buf ).append( ';' );
 			}
 		}
 		else {
 			buf.append( '[' ).append( ']' );
 		}
 		return buf;
 	}
 
 	public static StringBuffer appendEncodedAccessRules( IAccessRule[] rules, StringBuffer buf ) {
 
 		if( rules != null ) {
 			buf.append( '[' ).append( rules.length ).append( ']' );
 			for( int i = 0; i < rules.length; i++ ) {
 				appendEncodePath( rules[ i ].getPattern( ), buf ).append( ';' );
 				buf.append( rules[ i ].getKind( ) ).append( ';' );
 			}
 		}
 		else {
 			buf.append( '[' ).append( ']' );
 		}
 		return buf;
 	}
 
 	public StringBuffer appendEncodedSettings( StringBuffer buf ) {
 
 		buf.append( fEntryKind ).append( ';' );
 		if( getLinkTarget( ) == null ) {
 			appendEncodePath( fPath, buf ).append( ';' );
 		}
 		else {
 			appendEncodePath( fPath, buf ).append( '-' ).append( '>' );
 			appendEncodePath( getLinkTarget( ), buf ).append( ';' );
 		}
 		buf.append( Boolean.valueOf( fIsExported ) ).append( ';' );
 		for( int i = 0; i < fChildren.size( ); i++ ) {
 			Object curr = fChildren.get( i );
 			if( curr instanceof BPListElementAttribute ) {
 				BPListElementAttribute elem = ( BPListElementAttribute )curr;
 				if( elem.isBuiltIn( ) ) {
 					String key = elem.getKey( );
 					if( EXCLUSION.equals( key ) || INCLUSION.equals( key ) ) {
 						appendEncodedFilter( ( IPath[] )elem.getValue( ), buf ).append( ';' );
 					}
 					else if( ACCESSRULES.equals( key ) ) {
 						appendEncodedAccessRules( ( IAccessRule[] )elem.getValue( ), buf ).append( ';' );
 					}
 					else if( COMBINE_ACCESSRULES.equals( key ) ) {
 						buf.append( ( ( Boolean )elem.getValue( ) ).booleanValue( ) ).append( ';' );
 					}
 				}
 				else {
 					appendEncodedString( ( String )elem.getValue( ), buf );
 				}
 			}
 		}
 		return buf;
 	}
 
 	public IPath getLinkTarget( ) {
 
 		return fLinkTarget;
 	}
 
 	public void setPath( IPath path ) {
 
 		fCachedEntry = null;
 		fPath = path;
 	}
 
 	public void setLinkTarget( IPath linkTarget ) {
 
 		fCachedEntry = null;
 		fLinkTarget = linkTarget;
 	}
 
 	public static void insert( BPListElement element, List cpList ) {
 
 		int length = cpList.size( );
 		BPListElement[] elements = ( BPListElement[] )cpList.toArray( new BPListElement [ length ] );
 		int i = 0;
 		while( i < length && elements[ i ].getEntryKind( ) != element.getEntryKind( ) ) {
 			i++;
 		}
 		if( i < length ) {
 			i++;
 			while( i < length && elements[ i ].getEntryKind( ) == element.getEntryKind( ) ) {
 				i++;
 			}
 			cpList.add( i, element );
 			return;
 		}
 
 		switch( element.getEntryKind( ) ) {
 			case IBuildpathEntry.BPE_SOURCE:
 				cpList.add( 0, element );
 				break;
 			case IBuildpathEntry.BPE_CONTAINER:
 			case IBuildpathEntry.BPE_LIBRARY:
 			case IBuildpathEntry.BPE_PROJECT:			
 			default:
 				cpList.add( element );
 				break;
 		}
 	}
 
 	public static IBuildpathEntry[] convertToBuildpathEntries( List/* <CPListElement> */cpList ) {
 
 		IBuildpathEntry[] result = new IBuildpathEntry [ cpList.size( ) ];
 		int i = 0;
 		for( Iterator iter = cpList.iterator( ); iter.hasNext( ); ) {
 			BPListElement cur = ( BPListElement )iter.next( );
 			result[ i ] = cur.getBuildpathEntry( );
 			i++;
 		}
 		return result;
 	}
 
 	public static BPListElement[] createFromExisting( IDLTKProject project ) throws ModelException {
 
 		IBuildpathEntry[] rawBuildpath = project.getRawBuildpath( );
 		BPListElement[] result = new BPListElement [ rawBuildpath.length ];
 		for( int i = 0; i < rawBuildpath.length; i++ ) {
 			result[ i ] = BPListElement.createFromExisting( rawBuildpath[ i ], project );
 		}
 		return result;
 	}
 
 	public static boolean isProjectSourceFolder( BPListElement[] existing, IDLTKProject project ) {
 
 		IPath projPath = project.getProject( ).getFullPath( );
 		for( int i = 0; i < existing.length; i++ ) {
 			IBuildpathEntry curr = existing[ i ].getBuildpathEntry( );
 			if( curr.getEntryKind( ) == IBuildpathEntry.BPE_SOURCE ) {
 				if( projPath.equals( curr.getPath( ) ) ) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public IPath getOrginalPath( ) {
 
 		return fOrginalPath;
 	}
 
 	public IPath getOrginalLinkTarget( ) {
 
 		return fOrginalLinkTarget;
 	}
 
 	public boolean isExtenralFolder() {		
 		return this.fExternal && this.fEntryKind == IBuildpathEntry.BPE_LIBRARY && !Util.isArchiveFileName(this.fPath.toOSString());
 	}
 
 }
