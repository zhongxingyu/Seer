 /*
  * BioJava development code
  *
  * This code may be freely distributed and modified under the
  * terms of the GNU Lesser General Public Licence. This should
  * be distributed with the code. If you do not have a copy,
  * see:
  *
  * http://www.gnu.org/copyleft/lesser.html
  *
  * Copyright for this code is held jointly by the individual
  * authors. These should be listed in @author doc comments.
  *
  * For more information on the BioJava project and its aims,
  * or to join the biojava-l mailing list, visit the home page
  * at:
  *
  * http://www.biojava.org/
  *
  * This code was contributed from the Molecular Biology Toolkit
  * (MBT) project at the University of California San Diego.
  *
  * Please reference J.L. Moreland, A.Gramada, O.V. Buzko, Qing
  * Zhang and P.E. Bourne 2005 The Molecular Biology Toolkit (MBT):
  * A Modular Platform for Developing Molecular Visualization
  * Applications. BMC Bioinformatics, 6:21.
  *
  * The MBT project was funded as part of the National Institutes
  * of Health PPG grant number 1-P01-GM63208 and its National
  * Institute of General Medical Sciences (NIGMS) division. Ongoing
  * development for the MBT project is managed by the RCSB
  * Protein Data Bank(http://www.pdb.org) and supported by funds
  * from the National Science Foundation (NSF), the National
  * Institute of General Medical Sciences (NIGMS), the Office of
  * Science, Department of Energy (DOE), the National Library of
  * Medicine (NLM), the National Cancer Institute (NCI), the
  * National Center for Research Resources (NCRR), the National
  * Institute of Biomedical Imaging and Bioengineering (NIBIB),
  * the National Institute of Neurological Disorders and Stroke
  * (NINDS), and the National Institute of Diabetes and Digestive
  * and Kidney Diseases (NIDDK).
  *
  * Created on 2007/02/08
  *
  */ 
 package org.rcsb.mbt.structLoader;
 
 
 import java.io.IOException;
 import java.util.Map;
 
 import org.rcsb.mbt.model.Structure;
 import org.rcsb.mbt.model.UnitCell;
 import org.rcsb.mbt.model.geometry.ModelTransformationList;
 
 
 /**
  *  Defines the standard interface for classes which know how to load
  *  Structure objects. While a StructureLoader sub-class can
  *  be instantiated and used directly to load Structure objects, the
 *  StructureFactory class provides a wrapper to enable an application
  *  to make calls to a single common interface which provides the logic
  *  to determine which loader is capable of loading a given named structure.
  *  <P>
  *  @author	John L. Moreland
  *  @see	org.rcsb.mbt.model.Structure
  */
 public interface IStructureLoader
 {
 	/**
 	 * Returns the common name for the loader implementation.
 	 * This is the string that might appear in a user-selectable menu.
 	 */
 	public String getLoaderName( );
 
 	/**
 	 * Returns a reference to a named structure as a Structure object.
 	 * The "name" may be interpreted by the specific implementation
 	 * of the StructureLoader class. For example, a file loader would
 	 * interpret the "name" as a file or URL path, while a database loader
 	 * would interpret the "name" as a structure name. This enables a
 	 * common interface for all StructureLoader classes, yet does not
 	 * prevent a specific implementation from implementing additional
 	 * methods. Also, since each StructureLoader sub-class must
 	 * implement the "canLoad" method, an application can always
 	 * determine if a given loader is capable of delivering a specific
 	 * structure or not.
 	 * @throws IOException 
 	 */
 	public Structure load( String name ) throws IOException;
 
 	/**
 	 * Returns true if the loader is capable of loading the structure,
 	 * or false otherwise. This enables higher-level code to be able
 	 * to build a context sensitive menu of only the loaders that can
 	 * load a given structure name.
 	 */
 	public boolean canLoad( String name );
 	
 	/**
 	 * get the completed structure.
 	 * @return
 	 */
     public abstract Structure getStructure();
 	
     
     /**
 	 * get names for entities in the structure.
 	 * @return
 	 */
     public abstract Map<Integer, String> getEntityNameMap();
 	
 	/**
 	 * Test
 	 */
 	public abstract boolean hasUnitCell();
 	
 	/**
 	 * get the unit cell for biological units
 	 * @return
 	 */
 	public abstract UnitCell getUnitCell();
 	
 	/**
 	 * Test
 	 * @return
 	 */
     public abstract boolean hasBiologicUnitTransformationMatrices();
     
     /**
      * Accessor
      * @return
      */
     public abstract ModelTransformationList getBiologicalUnitTransformationMatrices();
     
     /**
      * Test
      */
     public abstract boolean hasNonCrystallographicOperations();
     
     /**
      * Accessor
      * @return
      */
     public abstract ModelTransformationList getNonCrystallographicOperations();
 }
 
