 ///////////////////////////////////////////////////////////////////////////////
 //  Filename: $RCSfile: AbstractDouble.java,v $
 //  Purpose:  Wrapper class for a simple counter descriptor.
 //  Language: Java
 //  Compiler: JDK 1.4
 //  Authors:  Joerg Kurt Wegner
 //  Version:  $Revision: 1.7 $
 //            $Date: 2005/02/17 16:48:29 $
 //            $Author: wegner $
 //
 // Copyright OELIB:          OpenEye Scientific Software, Santa Fe,
 //                           U.S.A., 1999,2000,2001
 // Copyright JOELIB/JOELib2: Dept. Computer Architecture, University of
 //                           Tuebingen, Germany, 2001,2002,2003,2004,2005
 // Copyright JOELIB/JOELib2: ALTANA PHARMA AG, Konstanz, Germany,
 //                           2003,2004,2005
 //
 //  This program is free software; you can redistribute it and/or modify
 //  it under the terms of the GNU General Public License as published by
 //  the Free Software Foundation version 2 of the License.
 //
 //  This program is distributed in the hope that it will be useful,
 //  but WITHOUT ANY WARRANTY; without even the implied warranty of
 //  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //  GNU General Public License for more details.
 ///////////////////////////////////////////////////////////////////////////////
 package joelib2.feature;
 
 import joelib2.data.IdentifierExpertSystem;
 
 import joelib2.feature.result.DoubleResult;
 
 import joelib2.molecule.Molecule;
 
 import joelib2.util.BasicProperty;
 import joelib2.util.PropertyHelper;
 
 import java.util.Map;
 
 import org.apache.log4j.Category;
 
 
 /**
  * Representation for a simple double value descriptor.
  *
  * @.author     wegnerj
  * @.license GPL
  * @.cvsversion    $Revision: 1.7 $, $Date: 2005/02/17 16:48:29 $
  */
 public abstract class AbstractDouble implements Feature
 {
     //~ Static fields/initializers /////////////////////////////////////////////
 
     /**
      *  Obtain a suitable logger.
      */
     private static Category logger = Category.getInstance(AbstractDouble.class
             .getName());
    public BasicFeatureInfo descInfo;
 
     //~ Methods ////////////////////////////////////////////////////////////////
 
     /**
      * Gets the double descriptor value.
      *
      * @param the molecule
      * @return the double descriptor value
      */
     public abstract double getDoubleValue(Molecule mol);
 
     public BasicProperty[] acceptedProperties()
     {
         return null;
     }
 
     /**
      * Calculate descriptor for this molecule.
      *
      * @param mol                      molecule for which this descriptor should be calculated
      * @return                         the descriptor calculation result for this molecule
      * @exception FeatureException  descriptor calculation exception
      */
     public FeatureResult calculate(Molecule mol) throws FeatureException
     {
         FeatureResult result = ResultFactory.instance().getFeatureResult(
                 descInfo.getName());
 
         return calculate(mol, result, null);
     }
 
     /**
      * Calculate descriptor for this molecule.
      *
      * @param mol                      molecule for which this descriptor should be calculated
      * @param initData                 initialization properties
      * @return                         the descriptor calculation result for this molecule
      * @exception FeatureException  descriptor calculation exception
      */
     public FeatureResult calculate(Molecule mol, Map properties)
         throws FeatureException
     {
         FeatureResult result = ResultFactory.instance().getFeatureResult(
                 descInfo.getName());
 
         return calculate(mol, result, properties);
     }
 
     /**
      * Calculate descriptor for this molecule.
      *
      * It should be faster, if we can can use an already initialized result class,
      * because this must not be get by Java reflection. Ensure that you will clone
      * this result class before you store these results in molecules, or the next molecule will
      * overwrite this result.
      *
      * @param mol                      molecule for which this descriptor should be calculated
      * @param descResult               the descriptor result class in which the result should be stored
      * @return                         the descriptor calculation result for this molecule
      * @exception FeatureException  descriptor calculation exception
      */
     public FeatureResult calculate(Molecule mol, FeatureResult descResult)
         throws FeatureException
     {
         return calculate(mol, descResult, null);
     }
 
     /**
      * Calculate descriptor for this molecule.
      *
      * It should be faster, if we can can use an already initialized result class,
      * because this must not be get by Java reflection. Ensure that you will clone
      * this result class before you store these results in molecules, or the next molecule will
      * overwrite this result.
      *
      * @param mol                      molecule for which this descriptor should be calculated
      * @param descResult               the descriptor result class in which the result should be stored
      * @param initData                 initialization properties
      * @return                         the descriptor calculation result for this molecule
      * @exception FeatureException  descriptor calculation exception
      */
     public FeatureResult calculate(Molecule mol, FeatureResult descResult,
         Map properties) throws FeatureException
     {
         if (!(descResult instanceof DoubleResult))
         {
             logger.error(descInfo.getName() + " result should be of type " +
                 DoubleResult.class.getName() + " but it's of type " +
                 descResult.getClass().toString());
         }
 
         DoubleResult result = null;
 
         // check if the init type is correct
         if (initialize(properties))
         {
             result = (DoubleResult) descResult;
             result.setDouble(getDoubleValue(mol));
             result.addCMLProperty(IdentifierExpertSystem.instance()
                 .getKernelID());
         }
 
         return result;
     }
 
     /**
      * Clear descriptor calculation method for a new molecule.
      */
     public void clear()
     {
     }
 
     /**
      * Gets the descriptor informations for this descriptor.
      *
      * @return   the descriptor information
      */
     public BasicFeatureInfo getDescInfo()
     {
         return descInfo;
     }
 
     /**
      * Gets the descriptor description.
      *
      * @return   the descriptor description
      */
     public FeatureDescription getDescription()
     {
         return new BasicFeatureDescription(descInfo.getDescriptionFile());
     }
 
     /**
      * Initialize descriptor calculation method for all following molecules.
      *
      * @param initData  initialization properties
      * @return <tt>true</tt> if the initialization was successfull
      */
     public boolean initialize(Map properties)
     {
         boolean allFine = true;
 
         if (!PropertyHelper.checkProperties(this, properties))
         {
             logger.error(
                 "Empty property definition or missing property entry.");
 
             allFine = false;
         }
 
         return allFine;
     }
 
     /**
      * Test the implementation of this descriptor.
      *
      * @return <tt>true</tt> if the implementation is correct
      */
     public boolean testDescriptor()
     {
         return true;
     }
 }
 
 ///////////////////////////////////////////////////////////////////////////////
 //  END OF FILE.
 ///////////////////////////////////////////////////////////////////////////////
 
 ///////////////////////////////////////////////////////////////////////////////
 //  END OF FILE.
 ///////////////////////////////////////////////////////////////////////////////
