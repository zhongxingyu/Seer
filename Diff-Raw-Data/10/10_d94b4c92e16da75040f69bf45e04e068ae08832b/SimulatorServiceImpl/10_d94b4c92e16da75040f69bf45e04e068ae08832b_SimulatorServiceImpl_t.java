 package nl.tue.fingerpaint.server.simulator;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.zip.ZipInputStream;
 
 import javax.xml.bind.DatatypeConverter;
 
 import nl.tue.fingerpaint.shared.simulator.Simulation;
 import nl.tue.fingerpaint.shared.simulator.SimulationResult;
 import nl.tue.fingerpaint.shared.simulator.SimulatorService;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 /**
  * Implementation of the {@link SimulatorService} RPC class.
  * 
  * @author Group Fingerpaint
  */
 public class SimulatorServiceImpl extends RemoteServiceServlet implements
 		SimulatorService {
 
 	/**
 	 * Random serial version uid
 	 */
 	private static final long serialVersionUID = 3842884820635044977L;
	
	@Override
	protected void doUnexpectedFailure(Throwable e) {
		// We do not react to IOExceptions, because they will probably amount
		// to a timeout, which can occur is the server is very busy with a lot
		// of heavy mixing requests
		if (!(e instanceof IOException)) {
			throw new Error(e);
		}
	}
 
 	/**
 	 * Simulates a result.
 	 * @param request The {@code Simulation} that contains the parameters
 	 * needed to run the simulation. The concentration vector is compressed.
 	 * @return The {@code SimulationResult} achieved by running the simulation
 	 * on the {@code request}.
 	 */
 	@Override
 	public SimulationResult simulate(Simulation request) {
 
 		int numVectorResults = request.calculatesIntermediateVectors() ? request
 				.getProtocolRuns() : 1;
 
 		double[] segregationPoints = new double[request.getProtocolRuns()];
 
 		double segregationPoint = 0;
 
 		String zippedVector = request.getConcentrationVector();
 
 		double[] doubleVector = doubleArrayFromString(unzip(zippedVector));
 
 		int[][] vectorResults = new int[numVectorResults][doubleVector.length];
 
 		for (int i = 0; i < request.getProtocolRuns(); i++) {
 			for (int j = 0; j < request.getProtocol().getProgramSize(); j++) {
 				segregationPoint = NativeCommunicator.getInstance().simulate(
 						request.getProtocol().getGeometry(),
 						request.getMixer(), doubleVector,
 						request.getProtocol().getStep(j).getStepSize(),
 						request.getProtocol().getStep(j).getName());
 			}
 			segregationPoints[i] = segregationPoint;
 			if (request.calculatesIntermediateVectors()) {
 				vectorResults[i] = toIntVector(doubleVector.clone());
 			}
 		}
 		if (!request.calculatesIntermediateVectors()) {
 			vectorResults[0] = toIntVector(doubleVector.clone());
 		}
 
 		return new SimulationResult(vectorResults, segregationPoints);
 	}
 
 	/**
 	 * Returns the integer vector representation of the given double vector
 	 * 
 	 * @param doubleVector
 	 *            The vector to convert
 	 * @return The integer vector represented by the given double vector
 	 */
 	private static int[] toIntVector(double[] doubleVector) {
 		int[] result = new int[doubleVector.length];
 		for (int i = 0; i < result.length; i++) {
 			result[i] = (int) Math.round(doubleVector[i] * 255);
 		}
 		return result;
 	}
 
 	/**
 	 * Returns the unzipped version of a zipped string
 	 * 
 	 * @param zippedString
 	 *            The string in zip format
 	 * @return The string represented by the zipped string
 	 */
 	private static String unzip(String zippedString) {
 		try {
 			byte[] bytes = DatatypeConverter.parseBase64Binary(zippedString);
 			ByteArrayInputStream byteIS = new ByteArrayInputStream(bytes);
 			ZipInputStream zipIS = new ZipInputStream(byteIS);
 			zipIS.getNextEntry();
 
 			InputStreamReader dataIS = new InputStreamReader(zipIS);
 			BufferedReader br = new BufferedReader(dataIS);
 			StringBuilder sb = new StringBuilder();
 			String append = "";
 			while (append != null) {
 				sb.append(append);
 				append = br.readLine();
 			}
 			return sb.toString();
 		} catch (IOException e) {
 			throw new Error(e);
 		}
 	}
 
 	/**
 	 * Returns the double array represented by the given string
 	 * 
 	 * @param array
 	 *            The string representation of the array
 	 * @return The double array represented by the given string
 	 */
 	private static double[] doubleArrayFromString(String array) {
 		String s = array.substring(1, array.length() - 1);
 		String[] ints = s.split(",");
 		double[] result = new double[ints.length];
 		for (int i = 0; i < result.length; i++) {
 			result[i] = Double.parseDouble(ints[i]) / 255.0;
 		}
 		return result;
 	}
 
 }
