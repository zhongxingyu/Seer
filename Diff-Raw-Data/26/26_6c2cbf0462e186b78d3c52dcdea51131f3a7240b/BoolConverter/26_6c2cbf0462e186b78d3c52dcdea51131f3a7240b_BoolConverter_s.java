 package pleocmd.pipe.cvt;
 
 import java.util.List;
 
 import pleocmd.cfg.ConfigDouble;
 import pleocmd.cfg.ConfigEnum;
 import pleocmd.exc.ConverterException;
 import pleocmd.itfc.gui.dgr.DiagramDataSet;
 import pleocmd.pipe.data.Data;
 import pleocmd.pipe.data.SingleBoolData;
 import pleocmd.pipe.data.SingleFloatData;
 
public class BoolConverter extends Converter {
 
 	public enum Comparator {
 		Equals {
 			@Override
 			public String toString() {
 				return "=";
 			}
 		},
 		LessThan {
 			@Override
 			public String toString() {
 				return "<=";
 			}
 		},
 		BiggerThan {
 			@Override
 			public String toString() {
 				return ">=";
 			}
 		}
 	}
 
 	private final ConfigEnum<Comparator> cfgComparator;
 
 	private final ConfigDouble cfgConstant;
 
 	public BoolConverter() {
 		addConfig(cfgComparator = new ConfigEnum<Comparator>(Comparator.class));
 		addConfig(cfgConstant = new ConfigDouble("Constant", 0));
 		constructed();
 	}
 
 	@Override
 	protected void initVisualize0() {
 		final DiagramDataSet ds = getVisualizeDataSet(0);
 		if (ds != null) ds.setLabel(getShortConfigDescr0());
 	}
 
 	@Override
 	public String getInputDescription() {
 		return SingleFloatData.IDENT;
 	}
 
 	@Override
 	public String getOutputDescription() {
 		return SingleBoolData.IDENT;
 	}
 
 	@Override
 	protected String getShortConfigDescr0() {
 		return String.format("%s %s?", cfgComparator.asString(), cfgConstant
 				.asString());
 	}
 
 	@Override
 	protected List<Data> convert0(final Data data) throws ConverterException {
 		if (!SingleFloatData.isSingleFloatData(data)) return null;
 		final double val = SingleFloatData.getValue(data);
 		final boolean res;
 		switch (cfgComparator.getEnum()) {
 		case Equals:
 			res = Math.abs(val - cfgConstant.getContent()) <= Double.MIN_NORMAL;
 			break;
 		case LessThan:
 			res = val <= cfgConstant.getContent() + Double.MIN_NORMAL;
 			break;
 		case BiggerThan:
 			res = val >= cfgConstant.getContent() - Double.MIN_NORMAL;
 			break;
 		default:
 			throw new ConverterException(this, true,
 					"Invalid comparator: '%s'", cfgComparator.getContent());
 		}
 		if (isVisualize()) plot(0, res ? 1 : 0);
 		return asList(SingleBoolData.create(res, data));
 	}
 
 	public static String help(final HelpKind kind) {
 		switch (kind) {
 		case Name:
 			return "Bool Converter";
 		case Description:
 			return "Creates a boolean value from a float (double) value";
 		case Config1:
 			return "Type of comparison";
 		case Config2:
 			return "Constant to compare each value with";
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
