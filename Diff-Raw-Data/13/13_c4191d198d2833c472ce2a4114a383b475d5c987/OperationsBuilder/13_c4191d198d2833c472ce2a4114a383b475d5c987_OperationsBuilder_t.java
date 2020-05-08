 package map.ui.cmd;
 
 import java.io.File;
 import java.util.List;
 
import map.core.WayComparison;
 import map.io.core.RoadNetworkBuilder;
 import map.l10n.Messages;
 
 import com.oschrenk.util.Builder;
 
 public class OperationsBuilder implements Builder<Runnable> {
 
 	private StartupArguments arguments;
 
 	public OperationsBuilder setArguments(StartupArguments arguments) {
 		this.arguments = arguments;
 		return this;
 	}
 
 	public Runnable build() throws IllegalArgumentException {
 		if (arguments == null)
 			throw new IllegalArgumentException(Messages.getString("OperationsBuilder.Error.INVALID_ARGUMENTS")); //$NON-NLS-1$
 
 		List<File> paths = arguments.paths();
 
 		if (paths == null) {
 			throw new IllegalArgumentException(Messages.getString("OperationsBuilder.Error.INVALID_ARGUMENT_LIST")); //$NON-NLS-1$
 		}
 
 		if (paths.isEmpty()) {
 			throw new IllegalArgumentException(Messages.getString("OperationsBuilder.Error.EMPTY_ARGUMENT_LIST")); //$NON-NLS-1$
 		}
 
 		if (paths.size() < 2) {
 			throw new IllegalArgumentException(Messages.getString("OperationsBuilder.Error.TOO_FEW_PATHS")); //$NON-NLS-1$
 		}
 
 		if (paths.size() > 2) {
 			throw new IllegalArgumentException(Messages.getString("OperationsBuilder.Error.TOO_MANY_PATHS")); //$NON-NLS-1$
 		}
 
 		// config
 		
 
 		return new WayComparison(
 					new RoadNetworkBuilder(paths.get(0)).build(),
 					new RoadNetworkBuilder(paths.get(1)).build());
 		//			distance);
 
 		//return new NodePositioningComparison(
 		//			new RoadNetworkBuilder(p.get(0)).build(),
 		//			new RoadNetworkBuilder(p.get(1)).build(),
 		//			distance);
 
 	}
 }
