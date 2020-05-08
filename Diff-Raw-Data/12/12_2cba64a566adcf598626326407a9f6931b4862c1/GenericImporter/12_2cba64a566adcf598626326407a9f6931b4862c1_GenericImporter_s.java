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
 package org.atomictagging.core.moleculehandler;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.atomictagging.core.accessors.DbWriter;
 import org.atomictagging.core.configuration.Configuration;
 import org.atomictagging.core.types.Atom;
 import org.atomictagging.core.types.CoreTags;
 import org.atomictagging.core.types.IAtom;
 import org.atomictagging.core.types.IMolecule;
 import org.atomictagging.core.types.Molecule;
 import org.atomictagging.utils.FileUtils;
 import org.atomictagging.utils.StringUtils;
 
 /**
  * Imports any file into a molecule.<br>
  * <br>
  * This is the generic "catch all" importer for Atomic Tagging. It therefore will always say yes if asked whether it can
  * import a file (see {@link #canHandle(File)} and it will put itself at the end of the {@link MoleculeHandlerFactory}
  * importer chain (see {@link #getOrdinal()}). It is expected that there will be a number of better importers for any
  * given file but if all else fails, this importer will just create a basic molecule with whatever information it can
  * get from the file system.
  * 
  * @author Stephan Mann
  */
 public class GenericImporter implements IMoleculeImporter {
 
 	@Override
 	public boolean canHandle( File file ) {
 		return true;
 	}
 
 
 	@Override
 	public int getOrdinal() {
 		return Integer.MAX_VALUE;
 	}
 
 
 	@Override
 	public String getUniqueId() {
 		return "atomictagging-genericimporter";
 	}
 
 
 	@Override
 	public void importFile( final Collection<IMolecule> molecules, final File file ) {
		String baseDir = Configuration.get().getString( "base.dir" );
 
 		String hash = getHashSum( file );
 		List<String> pathArray = new ArrayList<String>( 3 );
 		pathArray.add( hash.substring( 0, 2 ) );
 		pathArray.add( hash.substring( 2, 4 ) );
 
 		File targetDir = new File( baseDir + StringUtils.join( pathArray, "/" ) );
 
 		pathArray.add( hash.substring( 4 ) );
 
 		File target = new File( baseDir + StringUtils.join( pathArray, "/" ) );
 		try {
 			targetDir.mkdirs();
 			target.createNewFile();
 		} catch ( IOException e1 ) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		try {
 			FileUtils.copyFile( file, target );
 		} catch ( Exception e ) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		System.out.println( "Created file: " + target.getAbsolutePath() );
 
 		IAtom filename = Atom.build().withData( file.getName() ).withTag( "filename" ).buildWithDataAndTag();
 		IAtom binRef = Atom.build().withData( "/" + StringUtils.join( pathArray, "/" ) ).withTag( CoreTags.FILEREF_TAG )
 				.withTag( "x-filetype-unknown" ).buildWithDataAndTag();
 		IMolecule molecule = Molecule.build().withAtom( filename ).withAtom( binRef ).withTag( "generic-file" )
 				.buildWithAtomsAndTags();
 		DbWriter.write( molecule );
 		molecules.add( molecule );
 	}
 
 
 	private static String getHashSum( final File file ) {
 		try {
 			FileInputStream fis = new FileInputStream( file );
			return DigestUtils.md5Hex( fis );
 		} catch ( FileNotFoundException e ) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch ( IOException e ) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}

		return null;
 	}
 
 }
