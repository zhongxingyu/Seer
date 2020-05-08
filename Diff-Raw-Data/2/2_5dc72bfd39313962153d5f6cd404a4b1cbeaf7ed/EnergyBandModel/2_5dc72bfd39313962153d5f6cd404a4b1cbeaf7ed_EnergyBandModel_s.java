 
 package net.bioclipse.nm.ds.models;
 
import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bitbucket.nanojava.data.Nanomaterial;
 import org.bitbucket.nanojava.descriptor.EnergyBandDescriptor;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.openscience.cdk.qsar.DescriptorValue;
 import org.openscience.cdk.qsar.result.DoubleArrayResult;
 import org.openscience.cdk.qsar.result.IDescriptorResult;
 
 import net.bioclipse.cdk.business.ICDKManager;
 import net.bioclipse.cdk.domain.ICDKMolecule;
 import net.bioclipse.core.business.BioclipseException;
 import net.bioclipse.core.domain.IBioObject;
 import net.bioclipse.core.domain.IMaterial;
 import net.bioclipse.core.domain.IMolecule;
 import net.bioclipse.ds.model.AbstractDSTest;
 import net.bioclipse.ds.model.ITestResult;
 import net.bioclipse.ds.model.result.SimpleResult;
 import net.bioclipse.nm.business.INmManager;
 import net.bioclipse.nm.domain.Material;
 
 
 
 public class EnergyBandModel extends AbstractDSTest{
 
 	@Override
 	public List<String> getRequiredParameters() {
 		return new ArrayList<String>();
 	}
 
 	@Override
 	protected List<? extends ITestResult> doRunTest(IBioObject input,
 			IProgressMonitor monitor) {
 
 		Material material =null;
 		if (input instanceof Material) {
 			material = (Material) input;
 		}
 		else if (input instanceof ICDKMolecule) {
 			ICDKMolecule cdkmol = (ICDKMolecule) input;
 
         	ICDKManager cdk = net.bioclipse.cdk.business.Activator.getDefault().getJavaCDKManager();
         	INmManager nm = net.bioclipse.nm.Activator.getDefault().getJavaNmManager();
         	
         	String nmstr = (String) cdkmol.getProperty("bc.nm", IMolecule.Property.USE_CACHED_OR_CALCULATED);
         	try {
 				material = nm.fromString(nmstr);
 			} catch (Exception e) {
 				return returnError(e.getMessage(),e.getLocalizedMessage());
 			}
 
 		}
 		else
 			return returnError("Not a NNMaterial", "Not a NNMaterial");
 
 		Nanomaterial nm = material.getInternalModel();
 		
 		EnergyBandDescriptor descriptor = new EnergyBandDescriptor();
 		DescriptorValue value = descriptor.calculate(nm);
 		IDescriptorResult result = value.getValue();
 		DoubleArrayResult cBandEv = (DoubleArrayResult)result;
 		
 		double a = cBandEv.get(0);
 		double b = cBandEv.get(1);
 
 		SimpleResult res = new SimpleResult("[" + a +"," + b + "]", ITestResult.NEGATIVE);
 		if (a>-4.84 && b<-4.12){
 			res.setClassification(ITestResult.POSITIVE);
 		}
 		List<SimpleResult> results = new ArrayList<SimpleResult>();
 		results.add(res);
 		return results;
 	}
 	
 	
 }
