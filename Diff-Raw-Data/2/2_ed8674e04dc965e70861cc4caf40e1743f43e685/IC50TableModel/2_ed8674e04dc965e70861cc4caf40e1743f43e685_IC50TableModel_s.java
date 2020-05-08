 package net.bioclipse.brunn.ui.editors.plateEditor.model;
 
 import java.math.BigDecimal;
 import java.math.MathContext;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.bioclipse.brunn.ui.editors.plateEditor.Replicates;
 import de.kupzog.ktable.KTableCellEditor;
 import de.kupzog.ktable.KTableCellRenderer;
 import de.kupzog.ktable.KTableDefaultModel;
 import de.kupzog.ktable.renderers.FixedCellRenderer;
 import de.kupzog.ktable.renderers.TextCellRenderer;
 
 public class IC50TableModel extends KTableDefaultModel{
 
 	/*
      * a representation of the matrix
      */
     private String[][] matrix;
     private int rows;
     private int cols;
     private ArrayList<String> columnNames;
     private TextCellRenderer   renderer      = new TextCellRenderer(  TextCellRenderer.INDICATION_FOCUS_ROW );
     private KTableCellRenderer fixedRenderer = new FixedCellRenderer( FixedCellRenderer.STYLE_PUSH       |
                                                                       FixedCellRenderer.INDICATION_FOCUS );
 	private Map<String, String[]> replicatesContent = new HashMap<String, String[]>();
 	private Map<String, String> IC50 = new HashMap<String, String>();
 	private Map<String, String[]> entries = new HashMap<String, String[]>();
     
 
 	public IC50TableModel( //net.bioclipse.brunn.pojos.Plate plate,
 			               //KTable table,
 			               //IC50 editor,
 			               Replicates replicates) {
 		
 		columnNames = new ArrayList<String>();
 		Collections.addAll( columnNames, new String[] {"Compound Names", "IC50"} );
 		
 		/*
 		 * set up the matrix if there are any results for the plate 
 		 */
 		if ( replicates.getPlate().getWell( 1, 'a' )
 		               .getSampleContainer()
 		               .getWorkList()
		               .getAbstractOperations().size() > 1 ) {
     		fillContent(replicates);
     		extractEntries();
 		}
 
 		List<String[]> rows = new ArrayList<String[]>();
 		for( String compound : entries.keySet() ) {
 			rows.add( entries.get(compound) );
 		}
 		
 		Collections.sort(rows, new Comparator<String[]>() {
 			public int compare(String[] o1, String[] o2) {
 				int c = o1[0].compareTo( o2[0] );
 				if ( c != 0 ) 
 					return c;
 				c = Double.compare( Double.parseDouble( 
 						                o1[1].contains(" ") ? o1[1].substring( 0, 
 						                		                               o1[1].indexOf(' '))
 						                		            : o1[1]), 
 						            Double.parseDouble(
 						                o2[1].contains(" ") ? o2[1].substring( 0, 
 						                									   o2[1].indexOf(' '))
 						                				    : o2[1]) );
 				if ( c != 0 )
 					return c;
 				return 0;
 			}
 		});
 		
 		matrix = rows.toArray(new String[0][0]);
 
 		this.rows = matrix.length;
 		this.cols = columnNames.size();
 		
 		initialize();
 	}
 	
 	//reads dataset from replicate table
 	private void fillContent(Replicates replicates) {
 		replicatesContent.clear();
 		String[][] matrix = replicates.getReplicatesContent();
 		String[] headers = matrix[0];
 		for(int i=0; i<headers.length; i++) {
 			if(headers[i].equals("si%") || headers.equals("si") || headers.equals("SI") || headers.equals("Si")) {
 				headers[i] = "SI%";
 			}
 			replicatesContent.put(headers[i], null);
 		}
 		for(int i=1; i<matrix.length; i++) {
 			if(Character.isDigit(matrix[i][3].charAt(0))) {
 				for(int j=0; j<headers.length; j++) {
 					replicatesContent.put(headers[j],addToStringArray(replicatesContent.get(headers[j]), matrix[i][j]));	
 				}
 			}
 		}
 		splitConcAndUnit();
 		storeIC50();
 	}
 	
 	private void extractEntries() {
 		String[] compoundNames = replicatesContent.get("Compound Names");
 		String[] reducedCompoundNames = null;
 		String[] ic50 = replicatesContent.get("IC50");
 		String[] reducedIc50 = null;
 		String[] concUnit = replicatesContent.get("Unit");
 		String compound = compoundNames[0];
 		entries.put(compound, new String[] {compound, ic50[0]+" "+concUnit[0]});
 		for(int i=1; i<ic50.length; i++) {
 			if(!compoundNames[i].equals(compound)) {
 				addToStringArray(reducedCompoundNames,compoundNames[i]);
 				addToStringArray(reducedIc50,ic50[i]);
 				entries.put(compoundNames[i], new String[] {compoundNames[i], ic50[i]+" "+concUnit[i]});
 			}
 		}
 	}
 
 	private void splitConcAndUnit() {
 		String[] concAndUnit = replicatesContent.get("Concentration");
 		String[] conc = null;
 		String[] unit = null;
 		MathContext mc = new MathContext(3);
 		for(int i=0; i<concAndUnit.length; i++) {
 			String[] splitted = concAndUnit[i].split(" ");
 			BigDecimal bd = new BigDecimal(splitted[0]);
 			conc = addToStringArray(conc, bd.round(mc).toString());
 			unit = addToStringArray(unit, splitted[1]);
 		}
 		replicatesContent.put("Concentration", conc);
 		replicatesContent.put("Unit", unit);
 	}
 	
 	private void storeIC50() {
 		if(replicatesContent.containsKey("SI%") && replicatesContent.containsKey("Concentration")) {
 			IC50.clear();
 			String[] names = replicatesContent.get("Compound Names");
 			double[] conc = null;
 			double[] si = null;
 			String current = names[0];
 			for(int i=0;i<names.length; i++) {
 				if(names[i].equals(current)) {
 					conc = addToDoubleArray(conc, Double.parseDouble(replicatesContent.get("Concentration")[i]));
 					si = addToDoubleArray(si, Double.parseDouble(replicatesContent.get("SI%")[i]));
 				}
 				else {
 					IC50.put(current, calculateIC50(conc, si));
 					current = names[i];
 					conc = addToDoubleArray(null, Double.parseDouble(replicatesContent.get("Concentration")[i]));
 					si = addToDoubleArray(null, Double.parseDouble(replicatesContent.get("SI%")[i]));
 				}
 			}
 			IC50.put(current, calculateIC50(conc, si));
 			addIC50ToContent();
 		}
 	}
 
 	private String calculateIC50(double[] conc, double[] si) {
 		String ic50 = "-";
 		int crosses = 0;
 		for(int i=0; i<si.length-1; i++) {
 			if(si[i]<50 && 50<si[i+1] || si[i+1]<50 && 50<si[i]) {
 				crosses++;
 			}
 		}
 		if(crosses>1) {
 			return ic50;
 		}
 		MathContext mc = new MathContext(3);
 		double x1=0,x2=0,y1=0,y2=0;
 		for(int i=0; i<conc.length; i++) {
 			if(si[i]<50) {
 				x2 = conc[i];
 				y2 = si[i];
 				break;
 			}
 			x1 = conc[i];
 			y1 = si[i];
 		}
 		if (x1>0 && x2>0) {
 			BigDecimal bd = new BigDecimal((x1-x2)/(y1-y2)*(50-y1)+x1);
 			ic50 = String.valueOf(bd.round(mc).doubleValue());
 		}
 		else if(x2==0) {
 			ic50 = ">"+x1;
 		}
 		else if(x1==0) {
 			if(!(crosses==1)) {
 				ic50 = "<"+x2;
 			}
 		}
 		return ic50;
 	}
 
 	private void addIC50ToContent() {
 		replicatesContent.put("IC50", null);
 		String[] names = replicatesContent.get("Compound Names");
 		for(String name : names) {
 			if(!IC50.isEmpty()) {
 				String ic50 = IC50.get(name).toString();
 				String[] values = replicatesContent.get("IC50");
 				String[] newValues = addToStringArray(values, ic50);
 				replicatesContent.put("IC50", newValues);	
 			}
 		}
 	}
 	
 	private double[] addToDoubleArray(double[] array, double d) {
 		if(array != null) {
 			double[] newArray = new double[array.length+1];
 			for(int i=0; i<array.length; i++) {
 				newArray[i] = array[i];
 			}
 			newArray[array.length] = d;
 			return newArray;
 		}
 		else {
 			return new double[] {d};
 		}
 	}
 	
 	private String[] addToStringArray(String[] array, String string) {
 		if(array != null) {
 			String[] newArray = new String[array.length+1];
 			for(int i=0; i<array.length; i++) {
 				newArray[i] = array[i];
 			}
 			newArray[array.length] = string;
 			return newArray;
 		}
 		else {
 			return new String[] {string};
 		}
 	}
 	
 	@Override
 	public KTableCellEditor doGetCellEditor(int col, int row) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public KTableCellRenderer doGetCellRenderer(int col, int row) {
 		if (isFixedCell(col, row))
             return fixedRenderer;
         else
             return renderer;
 	}
 
 	@Override
 	public int doGetColumnCount() {
 		return cols;
 	}
 
 	@Override
 	public Object doGetContentAt(int col, int row) {
 		if( col >= 0 && row >= 1 ) {
 			return matrix[row-1][col];
 		}
 		if( row == 0 ) {
 			return columnNames.get(col);
 		}
 		throw new IllegalArgumentException(col +" or " + row + " not a legal argument");
 	}
 
 	@Override
 	public int doGetRowCount() {
 		return rows+1;
 	}
 
 	@Override
 	public void doSetContentAt(int col, int row, Object value) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public int getInitialColumnWidth(int column) {
 		// TODO Auto-generated method stub
 		return 150;
 	}
 
 	@Override
 	public int getInitialRowHeight(int row) {
 		// TODO Auto-generated method stub
 		return 20;
 	}
 
 	public int getFixedHeaderColumnCount() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	public int getFixedHeaderRowCount() {
 		// TODO Auto-generated method stub
 		return 1;
 	}
 
 	public int getFixedSelectableColumnCount() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	public int getFixedSelectableRowCount() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	public int getRowHeightMinimum() {
 		// TODO Auto-generated method stub
 		return 30;
 	}
 
 	public boolean isColumnResizable(int col) {
 		return true;
 	}
 
 	public boolean isRowResizable(int row) {
 		return true;
 	}
 
 }
