 package org.metware.mzAnnotate.main;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.metware.mzAnnotate.FragmentList;
 import org.metware.mzAnnotate.MzAnnotate;
 import org.metware.mzAnnotate.SpectrumData;
 import org.metware.mzAnnotate.MzAnnotateWriter;
 import org.metware.mzAnnotate.Tools;
 import org.openscience.cdk.ChemFile;
 import org.openscience.cdk.ChemObject;
 import org.openscience.cdk.exception.CDKException;
 import org.openscience.cdk.interfaces.IAtomContainer;
 import org.openscience.cdk.io.MDLReader;
 import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
 import org.xmlcml.cml.element.CMLCml;
 
 
 public class MzAnnotateMain {
 
 	public static void main(String[] args) throws FileNotFoundException, CDKException {
 		
 		String sep = System.getProperty("file.separator");
 		FragmentList fragList = new FragmentList();
 		String fragIDOrig = "";
 		
 		//get example data and create the data structure
 		List<String> exampleStructures = new ArrayList<String>();
 		exampleStructures.add("examples/naringenin/naringenin.mol");
 		exampleStructures.add("examples/naringenin/147_1.mol");
 		exampleStructures.add("examples/naringenin/147_2.mol");
 		exampleStructures.add("examples/naringenin/153.mol");
 		
 		
 		//this only reads in the necessary mol file
 		List<IAtomContainer> mols = new ArrayList<IAtomContainer>();
 		for (String file : exampleStructures) {
 			MDLReader reader;
 			List<IAtomContainer> containersList;
 
 			reader = new MDLReader(new FileReader(new File(file)));
 			ChemFile chemFile = (ChemFile) reader.read((ChemObject) new ChemFile());
 			containersList = ChemFileManipulator.getAllAtomContainers(chemFile);
 			IAtomContainer mol = containersList.get(0);
 			mols.add(mol);
 		}
 		//add the measured structure to the list
 		fragIDOrig = fragList.addStructure(mols.get(0));
 		//add the fragment structures 147
 		String fragID1 = fragList.addFragment(mols.get(1));
 		String fragID2 = fragList.addFragment(mols.get(2));
 		//add the fragment structures 153
 		String fragID3 = fragList.addFragment(mols.get(3));
 				
 
 		SpectrumData specData = new SpectrumData( "examples" + sep + "naringenin" + sep + "PB000122.txt");
 		MzAnnotate mzAnno = new MzAnnotate(specData, fragList);
 		try {
 			//peak 147 #1
 			mzAnno.assignPeakToFragment(Tools.getPeak(specData, specData.getPeakList().get(0).getMass()), fragID1);
 			//peak 147 #2
 			mzAnno.assignPeakToFragment(Tools.getPeak(specData, specData.getPeakList().get(0).getMass()), fragID2);
 			//peak 153
 			mzAnno.assignPeakToFragment(Tools.getPeak(specData, specData.getPeakList().get(1).getMass()), fragID3);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		
 		MzAnnotateWriter writerTest = new MzAnnotateWriter();
 		CMLCml cml = writerTest.getMzAnnotate(mzAnno);
 		System.out.println(cml.toXML());
 	}
 }
 
 
