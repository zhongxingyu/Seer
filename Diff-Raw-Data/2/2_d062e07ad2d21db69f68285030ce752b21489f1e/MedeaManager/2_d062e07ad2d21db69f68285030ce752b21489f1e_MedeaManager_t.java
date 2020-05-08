 /*******************************************************************************
  * Copyright (c) 2009  Egon Willighagen <egonw@users.sf.net>
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * www.eclipse.org—epl-v10.html <http://www.eclipse.org/legal/epl-v10.html>
  * 
  * Contact: http://www.bioclipse.net/    
  ******************************************************************************/
 package net.bioclipse.medea.business;
 
 import org.openscience.cdk.interfaces.IAtomContainer;
 import org.xmlcml.cml.element.CMLSpectrum;
 
 import net.bioclipse.cdk.domain.ICDKMolecule;
 import net.bioclipse.core.domain.IMolecule;
 import net.bioclipse.core.domain.ISpectrum;
import net.bioclipse.medea.core.Medea;
 import net.bioclipse.spectrum.domain.JumboSpectrum;
 
 public class MedeaManager implements IMedeaManager {
 
     private final static Medea predictor = new Medea();
 
     public String getNamespace() {
         return "medea";
     }
 
     public ISpectrum predictMassSpectrum(IMolecule molecule) {
         ICDKMolecule mol = (ICDKMolecule)molecule.getAdapter(ICDKMolecule.class);
         if (mol == null) {
             throw new RuntimeException("Only supports ICDKMolecule for now.");
         }
 
         IAtomContainer container = mol.getAtomContainer();
         predictor.predictMS(container);
         CMLSpectrum cmlSpectrum = predictor.getPredictedSpectrum();
         ISpectrum spectrum = new JumboSpectrum(cmlSpectrum);
         return spectrum;
     }
 
 }
