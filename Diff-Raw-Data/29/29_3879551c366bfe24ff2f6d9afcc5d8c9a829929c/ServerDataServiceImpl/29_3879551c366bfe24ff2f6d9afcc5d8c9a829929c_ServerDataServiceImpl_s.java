 package nl.tue.fingerpaint.server.serverdata;
 
import nl.tue.fingerpaint.client.serverdata.ServerDataService;
 import nl.tue.fingerpaint.shared.GeometryNames;
 import nl.tue.fingerpaint.shared.ServerDataResult;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 /**
  * Implementation of the {@link ServerDataService}.
  * 
  * @author Group Fingerpaint
  */
 public class ServerDataServiceImpl extends RemoteServiceServlet implements
 		ServerDataService {
 
 	/**
 	 * Serial version UID.
 	 */
 	private static final long serialVersionUID = 8671226026204731779L;
 
 	/**
 	 * Returns server data, i.e. which geometries and mixers are available.
 	 * 
 	 * @return server data, i.e. which geometries and mixers are available.
 	 */
 	@Override
 	public ServerDataResult getServerData() {
 		return new ServerDataResult(
 				new String[] { GeometryNames.RECT_LONG, GeometryNames.SQR_LONG },
 				new String[] { "Default", "Default" },
 				new int[] { 0, 1 }
 			);
 	}
 
 }
