 /*******************************************************************************
  * Copyright (c) 2007,2009  Egon Willighagen <egonw@users.sf.net>
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contact: http://www.bioclipse.net/
  ******************************************************************************/
 package net.bioclipse.joelib.qsar.impl;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import joelib2.feature.Feature;
 import joelib2.feature.FeatureException;
 import joelib2.feature.FeatureResult;
 import joelib2.feature.result.BitResult;
 import joelib2.feature.result.DoubleArrayResult;
 import joelib2.feature.result.DoubleResult;
 import joelib2.feature.result.IntArrayResult;
 import joelib2.feature.result.IntResult;
 import joelib2.feature.types.BCUT;
 import joelib2.feature.types.count.AcidicGroups;
 import joelib2.feature.types.count.BasicGroups;
 import joelib2.feature.types.count.NumberOfAtoms;
 import joelib2.feature.types.BurdenModifiedEigenvalues;
 import joelib2.feature.types.count.HeteroCycles;
 import joelib2.feature.types.count.NumberOfBonds;
 import joelib2.feature.types.PolarSurfaceArea;
 import joelib2.feature.types.MolecularWeight;
 import joelib2.feature.types.LogP;
 import joelib2.io.types.ChemicalMarkupLanguage;
 import joelib2.molecule.BasicConformerMolecule;
 import joelib2.molecule.Molecule;
 import net.bioclipse.core.business.BioclipseException;
 import net.bioclipse.core.domain.IMolecule;
 import net.bioclipse.qsar.DescriptorType;
 import net.bioclipse.qsar.descriptor.DescriptorResult;
 import net.bioclipse.qsar.descriptor.IDescriptorCalculator;
 import net.bioclipse.qsar.descriptor.IDescriptorResult;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 
 @SuppressWarnings("serial")
 public class JOELibDescriptorCalculator implements IDescriptorCalculator {
 
     private static final Logger logger =
         Logger.getLogger(JOELibDescriptorCalculator.class);
 
     private final static String NS_BOQSAR =
         "http://www.blueobelisk.org/ontologies/chemoinformatics-algorithms/#";
     
     private final static Map<String,Feature> descriptors =
         new HashMap<String,Feature>() {{
             this.put(NS_BOQSAR + "BCUT", new BCUT());
             this.put(NS_BOQSAR + "atomCount", new NumberOfAtoms());
             this.put(NS_BOQSAR + "bondCount", new NumberOfBonds());
             this.put(NS_BOQSAR + "numberOfBasicGroups", new BasicGroups());
             this.put(NS_BOQSAR + "numberOfAcidicGroups", new AcidicGroups());
             this.put(NS_BOQSAR + "burdenModifiedEigenvalues",
                      new BurdenModifiedEigenvalues());
             this.put(NS_BOQSAR + "heteroCycles", new HeteroCycles());
             this.put(NS_BOQSAR + "tpsa", new PolarSurfaceArea());
             this.put(NS_BOQSAR + "weight", new MolecularWeight());
             this.put(NS_BOQSAR + "logPbyAtomicContributions", new LogP());
     }};
     
     public Map<? extends IMolecule, List<IDescriptorResult>>
         calculateDescriptor(Map<IMolecule, List<DescriptorType>> moldesc,
             IProgressMonitor monitor) throws BioclipseException {
         
         if (monitor == null)
             monitor = new NullProgressMonitor();
 
         Map<IMolecule, List<IDescriptorResult>> allResults=
             new HashMap<IMolecule, List<IDescriptorResult>>();
 
         monitor.beginTask("Calculating descriptors...", moldesc.size());
         for (IMolecule mol : moldesc.keySet()){
             if (monitor.isCanceled()) return allResults;
             List<IDescriptorResult> retlist =
                 calculate(mol, moldesc.get( mol ), monitor);
             allResults.put(mol, retlist);
             monitor.worked(1);
         }
 
         return allResults;
     }
 
     private List<IDescriptorResult> calculate(IMolecule mol,
             List<DescriptorType> list, IProgressMonitor monitor) {
         List<IDescriptorResult> results = new ArrayList<IDescriptorResult>();
         
         Molecule joeMol = new BasicConformerMolecule();
         try {
             String cmlSerialization = mol.toCML();
             ChemicalMarkupLanguage reader = new ChemicalMarkupLanguage();
             reader.initReader(
                 new ByteArrayInputStream(cmlSerialization.getBytes())
             );
             boolean success = reader.read(joeMol);
             if (!success) joeMol = null;
         } catch (IOException e) {
            logger.warn("Error while reading into a JOELib Molecule", e);
             joeMol = null;
         } catch (BioclipseException e) {
            logger.warn("Error while reading into a JOELib Molecule", e);
             joeMol = null;
         }
 
         for (DescriptorType descType : list) {
             if (monitor.isCanceled()) return results;
             
             if (joeMol == null) {
                 IDescriptorResult res = new DescriptorResult();
                 res.setDescriptor( descType );
                 res.setErrorMessage("Could not create a JOELib molecule.");
                 res.setValues(new Float[0]);
                 res.setLabels(new String[0]);
             }
 
             Feature descriptor =
                 descriptors.get(descType.getOntologyid());
             if (descriptor != null) {
                 IDescriptorResult res = new DescriptorResult();
                 res.setDescriptor( descType );
                 res = calculateDescriptor(joeMol, descriptor, res);
             } else {
                 IDescriptorResult res = new DescriptorResult();
                 res.setDescriptor( descType );
                 res.setErrorMessage("Could not that JOELib descriptor.");
                 res.setValues(new Float[0]);
                 res.setLabels(new String[0]);
             }
         }
         return results;
     }
 
     public IDescriptorResult calculateDescriptor(
         Molecule mol, Feature descriptor,
         IDescriptorResult result) {
 
         // get the values
         Float[] resultVals = new Float[0];
         try {
             FeatureResult joeResults = descriptor.calculate(mol);
             System.out.println("Class: " + joeResults.getClass().getName());
             if (joeResults instanceof IntResult) {
                 resultVals = new Float[1];
                 resultVals[0] = (float)((IntResult)joeResults).getInt();
             } else if (joeResults instanceof IntArrayResult) {
                 int[] intResults = ((IntArrayResult)joeResults).getIntArray();
                 resultVals = new Float[intResults.length];
                 for (int j=0; j<resultVals.length; j++) {
                     resultVals[j] = (float)intResults[j];
                 }
             } else if (joeResults instanceof DoubleResult) {
                 resultVals = new Float[1];
                 resultVals[0] = (float)((DoubleResult)joeResults).getDouble();
             } else if (joeResults instanceof BitResult) {
                 int[] doubleResults = ((BitResult)joeResults).getBinaryValue().toIntArray();
                 resultVals = new Float[doubleResults.length];
                 for (int j=0; j<resultVals.length; j++) {
                     resultVals[j] = (float)doubleResults[j];
                 }
             } else if (joeResults instanceof DoubleArrayResult) {
                 double[] doubleResults = ((DoubleArrayResult)joeResults).getDoubleArray();
                 resultVals = new Float[doubleResults.length];
                 for (int j=0; j<resultVals.length; j++) {
                     resultVals[j] = (float)doubleResults[j];
                 }
             } else {
                 logger.error(
                     "No idea what to do with this result class: " +
                     joeResults.getClass().getName()
                 );
             }
         } catch (FeatureException e) {
             logger.error("Could not calculate JOELib descriptor: " + e.getMessage());
             e.printStackTrace();
             for (int j=0; j<resultVals.length; j++) {
                 resultVals[j] = Float.NaN;
             }
         }
 
         // set up column labels
         String[] resultLabels = new String[resultVals.length];
         if (resultLabels.length == 1) {
             resultLabels[0] = descriptor.getDescInfo().getName();
         } else {
             for (int j=0; j<resultLabels.length; j++) {
                 resultLabels[j] = descriptor.getDescInfo().getName() + (j+1);
             }
         }
 
         if (resultLabels.length != resultVals.length) {
             System.out.println(
                 "WARN: #labels != #vals for " +
                 descriptor.getDescInfo().getName()
             );
         }
         result.setValues(resultVals);
         result.setLabels(resultLabels);
         return result;
     }
 }
