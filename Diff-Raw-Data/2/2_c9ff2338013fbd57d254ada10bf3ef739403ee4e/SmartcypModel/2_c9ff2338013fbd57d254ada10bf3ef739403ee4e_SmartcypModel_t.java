 package net.bioclipse.smartcyp.ds;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.openscience.cdk.interfaces.IAtom;
 
 import net.bioclipse.cdk.domain.ICDKMolecule;
 import net.bioclipse.ds.model.AbstractDSTest;
 import net.bioclipse.ds.model.IDSTest;
 import net.bioclipse.ds.model.ITestResult;
 import net.bioclipse.smartcyp.Activator;
 import net.bioclipse.smartcyp.business.ISmartcypManager;
 
 public class SmartcypModel extends AbstractDSTest implements IDSTest {
 
 	public SmartcypModel() {
 	}
 
 	@Override
 	public List<String> getRequiredParameters() {
 		return null;
 	}
 
 	@Override
 	protected List<? extends ITestResult> doRunTest(ICDKMolecule cdkmol,
 			IProgressMonitor monitor) {
 
         //Make room for results
         List<ITestResult> results=new ArrayList<ITestResult>();
 
 		ISmartcypManager smartcyp = Activator.getDefault().getJavaSmartcypManager();
 		ICDKMolecule retmol = null;
 		try {
 			retmol = smartcyp.predictSOM(cdkmol);
 		} catch (Exception e) {
 			return returnError("Error running smartcyp", e.getMessage());
 		}
 //		if (retmol==null)
 //			return returnError("Smartcyp returned null", "");
 		
 		//Set up results, bogus for now
 		SmartCypResult resStandard = new SmartCypResult("Standard", ITestResult.POSITIVE);
 		SmartCypResult res2c9 = new SmartCypResult("CYP2C9", ITestResult.POSITIVE);
 		SmartCypResult res2d6 = new SmartCypResult("CYP2D6", ITestResult.POSITIVE);
 
 		System.out.println("SmartCyp properties per atom: ");
 		for(IAtom atom : retmol.getAtomContainer().atoms() ){
 			//			System.out.println("Atom: " + retmol.getAtomContainer().getAtomNumber(atom));
 			if (atom.getProperty("Ranking")!=null){
 				Integer ranking = (Integer) atom.getProperty("Ranking");
 				resStandard.putAtomResult(retmol.getAtomContainer().getAtomNumber(atom), ranking);
 			}
 			if (atom.getProperty("Ranking2C9")!=null){
 				Integer ranking = (Integer) atom.getProperty("Ranking2C9");
 				res2c9.putAtomResult(retmol.getAtomContainer().getAtomNumber(atom), ranking);
 			}
 			if (atom.getProperty("Ranking2D6")!=null){
 				Integer ranking = (Integer) atom.getProperty("Ranking2D6");
 				res2d6.putAtomResult(retmol.getAtomContainer().getAtomNumber(atom), ranking);
 			}
 		}
 
 		results.add(resStandard);
 		results.add(res2c9);
 		results.add(res2d6);
 		
 		return results;
 	}
 
 }
 
