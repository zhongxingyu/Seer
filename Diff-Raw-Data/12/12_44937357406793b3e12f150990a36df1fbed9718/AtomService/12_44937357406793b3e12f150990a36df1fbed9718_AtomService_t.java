 /**
  * This file is part of Atomic Tagging.
  * 
  * Atomic Tagging is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
  * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * Atomic Tagging is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with Atomic Tagging. If not, see
  * <http://www.gnu.org/licenses/>.
  */
 package org.atomictagging.core.services.impl;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.commons.lang.NotImplementedException;
 import org.atomictagging.core.accessors.DB;
 import org.atomictagging.core.services.ATService;
 import org.atomictagging.core.services.IAtomService;
 import org.atomictagging.core.types.Atom;
 import org.atomictagging.core.types.Atom.AtomBuilder;
 import org.atomictagging.core.types.CoreTags;
 import org.atomictagging.core.types.IAtom;
 import org.atomictagging.utils.StringUtils;
 import org.eclipse.core.runtime.Assert;
 
 /**
  * @author Stephan Mann
  */
 public class AtomService extends AbstractService implements IAtomService {
 
 	private final static String			ID				= "atomid";
 	private final static String			DATA			= "data";
 	private final static String			TAG				= "tag";
 
 	private final static String			SELECT_ALL		= "SELECT " + ID + ", " + DATA + ", " + TAG + " ";
 	private final static String			FROM_JOIN_WHERE	= " FROM atoms JOIN atom_has_tags JOIN tags "
 																+ "WHERE atomid = atoms_atomid AND tags_tagid = tagid ";
 
 	private static PreparedStatement	checkAtom;
 	private static PreparedStatement	readAtom;
 	private static PreparedStatement	insertAtom;
 	private static PreparedStatement	checkAtomTags;
 	private static PreparedStatement	insertAtomTags;
 
 	static {
 		try {
 			checkAtom = DB.CONN.prepareStatement( "SELECT atomid FROM atoms WHERE data = ?" );
 			readAtom = DB.CONN.prepareStatement( SELECT_ALL + FROM_JOIN_WHERE
 					+ " AND tags_tagid = tagid AND atomid = ?" );
 			insertAtom = DB.CONN.prepareStatement( "INSERT INTO atoms (data) VALUES (?)",
 					Statement.RETURN_GENERATED_KEYS );
 			checkAtomTags = DB.CONN
 					.prepareStatement( "SELECT atoms_atomid FROM atom_has_tags WHERE atoms_atomid = ? AND tags_tagid = ?" );
 			insertAtomTags = DB.CONN
 					.prepareStatement( "INSERT INTO atom_has_tags (atoms_atomid, tags_tagid) VALUES (?, ?)" );
 		} catch ( final SQLException e ) {
 			e.printStackTrace();
 		}
 	}
 
 
 	@Override
 	public IAtom find( final long atomId ) {
 		try {
 			readAtom.setLong( 1, atomId );
 			final ResultSet atomResult = readAtom.executeQuery();
 
 			final List<IAtom> atoms = readFromResultSet( atomResult );
 			Assert.isTrue( atoms.size() <= 1 );
 
 			if ( atoms.size() == 1 ) {
 				return atoms.get( 0 );
 			}
 		} catch ( final SQLException e ) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 
 	@Override
 	public List<IAtom> find( final List<String> inclusiveTags ) {
 		return find( inclusiveTags, Filter.INCLUDE );
 	}
 
 
 	@Override
 	public List<IAtom> find( final List<String> tags, final Filter filter ) {
 		List<IAtom> atoms = new ArrayList<IAtom>();
 
 		try {
 			String is = "";
 			if ( Filter.EXCLUDE == filter ) {
 				is = " NOT ";
 			}
 
 			// FIXME Tags need to be escaped
 			final String subQuery = "SELECT " + ID + FROM_JOIN_WHERE + " AND tag " + is + in( tags );
 			final String query = SELECT_ALL + FROM_JOIN_WHERE + " AND " + ID + " IN (" + subQuery + " ) ORDER BY " + ID;
 
 			final Statement stmt = DB.CONN.createStatement();
 			final ResultSet atomsResult = stmt.executeQuery( query );
 			atoms = readFromResultSet( atomsResult );
 		} catch ( final SQLException e ) {
 			e.printStackTrace();
 		}
 
 		return atoms;
 	}
 
 
 	@Override
 	public List<IAtom> findUserAtoms() {
 		return find( CoreTags.asList(), Filter.EXCLUDE );
 	}
 
 
 	@Override
 	public String[] findUserAtomsAsArray() {
 		final List<IAtom> atoms = findUserAtoms();
 		final String[] atomNames = new String[atoms.size()];
 
 		for ( int i = 0; i < atoms.size(); i++ ) {
 			atomNames[i] = atoms.get( i ).getData();
 		}
 
 		return atomNames;
 	}
 
 
 	@Override
 	public IAtom findByData( final String data ) {
 		IAtom atom = null;
 
 		try {
 			final String query = SELECT_ALL + FROM_JOIN_WHERE + " AND tags_tagid = tagid AND data = '" + data + "'";
 			final Statement stmt = DB.CONN.createStatement();
 			final ResultSet atomsResult = stmt.executeQuery( query );
 			final List<IAtom> resultSet = readFromResultSet( atomsResult );
			if ( !resultSet.isEmpty() ) {
				atom = resultSet.get( 0 );
			}
 		} catch ( final SQLException e ) {
 			e.printStackTrace();
 		}
 
 		return atom;
 	}
 
 
 	@Override
 	public long save( final IAtom atom ) {
 		try {
 			final List<Long> ids = save( Arrays.asList( atom ) );
 			Assert.isTrue( ids.size() == 1 );
 		} catch ( final SQLException e ) {
 			e.printStackTrace();
 		}
 		return -1;
 	}
 
 
 	@Override
 	public List<Long> save( final List<IAtom> atoms ) throws SQLException {
 		final List<Long> atomIds = new ArrayList<Long>();
 
 		for ( final IAtom atom : atoms ) {
 			checkAtom.setString( 1, atom.getData() );
 			checkAtom.execute();
 			long atomId = getIdOfExistingEntity( checkAtom, "atomid" );
 
 			if ( atomId == -1 ) {
 				insertAtom.setString( 1, atom.getData() );
 				insertAtom.execute();
 				atomId = getAutoIncrementId( insertAtom );
 			}
 
 			for ( final String tag : atom.getTags() ) {
 				final long tagId = ATService.getTagService().save( tag );
 
 				checkAtomTags.setLong( 1, atomId );
 				checkAtomTags.setLong( 2, tagId );
 				checkAtomTags.execute();
 				final long tagCheck = getIdOfExistingEntity( checkAtomTags, "atoms_atomid" );
 
 				if ( tagCheck == -1 ) {
 					insertAtomTags.setLong( 1, atomId );
 					insertAtomTags.setLong( 2, tagId );
 					insertAtomTags.execute();
 				}
 			}
 
 			atomIds.add( atomId );
 		}
 
 		return atomIds;
 	}
 
 
 	@Override
 	public void delete( final IAtom atom ) {
 		throw new NotImplementedException( "Not yet implemented. Sorry." );
 	}
 
 
 	private List<IAtom> readFromResultSet( final ResultSet atomsResult ) throws SQLException {
 		final List<IAtom> atoms = new ArrayList<IAtom>();
 
 		try {
			// atomsResult.next();
			//
			// while ( !atomsResult.isAfterLast() ) {
			while ( atomsResult.next() ) {
 				final long atomId = atomsResult.getLong( ID );
 
 				final AtomBuilder builder = Atom.build().withId( atomId );
 				builder.withData( atomsResult.getString( DATA ) );
 				builder.withTag( atomsResult.getString( TAG ) );
 
 				while ( atomsResult.next() && atomsResult.getLong( ID ) == atomId ) {
 					builder.withTag( atomsResult.getString( TAG ) );
 				}
 
 				atoms.add( builder.buildWithDataAndTag() );
 			}
 		} finally {
 			atomsResult.close();
 		}
 
 		return atoms;
 	}
 
 
 	private static String in( final List<String> tags ) {
 		return " IN ('" + StringUtils.join( tags, "', '" ) + "') ";
 	}
 
 }
