 package pleocmd.pipe.cvt;
 
 import java.util.List;
 
 import pleocmd.cfg.ConfigInt;
 import pleocmd.exc.ConverterException;
 import pleocmd.itfc.gui.dgr.DiagramDataSet;
 import pleocmd.pipe.data.CommandData;
 import pleocmd.pipe.data.Data;
 import pleocmd.pipe.data.SingleValueData;
 
 public final class SingleJointMovement extends Converter {
 
 	enum OutOfRangeBehavior {
 		CutOff, FitToRange
 	}
 
 	enum Transformation {
 		Linear
 	}
 
 	private static final int ANGLE_UNDEFINED = 1000000;
 
 	private final ConfigInt cfgJointNumber;
 	private final ConfigInt cfgMinAngleMovement;
 
 	private double currentAngle;
 
 	public SingleJointMovement() {
 		addConfig(cfgJointNumber = new ConfigInt("Joint-Number", 9, 0, 13));
 		addConfig(cfgMinAngleMovement = new ConfigInt("Minimal Angle-Movement",
 				3, 0, 50));
 		constructed();
 	}
 
 	@Override
 	protected void configure0() {
 		// nothing to do
 	}
 
 	@Override
 	protected void init0() {
 		currentAngle = ANGLE_UNDEFINED;
 	}
 
 	@Override
 	protected void close0() {
 		// nothing to do
 	}
 
 	@Override
 	protected void initVisualize0() {
 		final DiagramDataSet ds = getVisualizeDataSet(0);
 		if (ds != null)
 			ds.setLabel(String.format("Angle for Joint %d", cfgJointNumber
 					.getContent()));
 	}
 
 	@Override
 	public String getInputDescription() {
 		return SingleValueData.IDENT;
 	}
 
 	@Override
 	public String getOutputDescription() {
 		return "PMC";
 	}
 
 	@Override
 	protected List<Data> convert0(final Data data) throws ConverterException {
 		if (!SingleValueData.isSingleValueData(data)) return null;
 		final double val = SingleValueData.getValue(data);
 		if (Math.abs(currentAngle - val) < cfgMinAngleMovement.getContent())
 			return emptyList(); // ignore small movements
 		currentAngle = val;
 		if (isVisualize()) plot(0, val);
 		return asList(new CommandData("PMC", String.format("JOINT MOVE %d %d",
				cfgJointNumber.getContent(), (int) val), data));
 	}
 
 	public static String help(final HelpKind kind) {
 		switch (kind) {
 		case Name:
 			return "Single Joint Movement";
 		case Description:
 			return "Produces a JOINT MOVE command for the Pleo for one joint "
 					+ "based on the data in a single channel";
 		case Config1:
 			return "Number of Pleo joint (motor) to move";
 		case Config2:
 			return "Minimal angle; all movements below this will be ignored";
 		default:
 			return null;
 		}
 	}
 
 	@Override
 	public String isConfigurationSane() {
 		return null;
 	}
 
 	@Override
 	protected int getVisualizeDataSetCount() {
 		return 1;
 	}
 
 }
