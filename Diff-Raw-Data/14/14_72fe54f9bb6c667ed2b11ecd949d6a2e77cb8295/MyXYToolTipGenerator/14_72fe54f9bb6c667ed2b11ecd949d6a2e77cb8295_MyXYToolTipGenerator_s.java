 package ClassAdminFrontEnd;
 
 import java.text.DecimalFormat;
 import java.util.LinkedList;
 
 import org.jfree.chart.labels.CustomXYToolTipGenerator;
 import org.jfree.data.xy.XYDataset;
 
 import ClassAdminBackEnd.Global;
 import ClassAdminBackEnd.SuperEntity;
 
 public class MyXYToolTipGenerator extends CustomXYToolTipGenerator {
 
 	private static final long serialVersionUID = -383054432396474072L;
 	private final LinkedList<LinkedList<SuperEntity>> diedata = Global.getGlobal().getActiveProject().getHead().getDataLinkedList();
 	private  int[] intarray;
 	public MyXYToolTipGenerator(int [] houer)
 	{
 		intarray = houer;
 		
 	}
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.jfree.chart.labels.CustomXYToolTipGenerator#generateToolTip(org.jfree
 	 * .data.xy.XYDataset, int, int)
 	 */
 	@Override
 	public String generateToolTip(XYDataset data, int series, int item) {
	
 		return "Student number: " + diedata.get(intarray[intarray[item]]).get(0).getValue() + "  ("
				+ Double.toString(roundTwoDecimals(data.getXValue(series, item))) + ","
				+ Double.toString(roundTwoDecimals(data.getYValue(series, item))) + ")";
 	}
 
 	/**
 	 * @param d
 	 * @return Round number to two decimals
 	 */
 	public double roundTwoDecimals(double d) {
 		DecimalFormat twoDForm = new DecimalFormat("#.##");
 		return Double.valueOf(twoDForm.format(d));
 	}
 }
