 package org.mule.module.google.spreadsheet.adapter;
 
 import java.util.List;
 import java.util.Map;
 
 import org.mule.api.NestedProcessor;
 import org.mule.module.google.spreadsheet.GoogleSpreadSheetModule;
 import org.mule.module.google.spreadsheet.model.Cell;
 import org.mule.module.google.spreadsheet.model.Row;
 
 /**
  * 
  * @author mariano.gonzalez@mulesoft.com
  *
  */
 public abstract class AbstractBatchUpdateAdapter implements NestedProcessor {
 
 	private GoogleSpreadSheetModule module;
 	
 	public AbstractBatchUpdateAdapter(GoogleSpreadSheetModule module) {
 		this.module = module;
 	}
 	
 	protected GoogleSpreadSheetModule getModule() {
 		return this.module;
 	}
 	
 	@Override
 	public Object process(Object payload) throws Exception {
 		List<Row> rows = this.extractRows(payload); 
 		
 		for (Row row : rows) {
 			int rowNumber = row.getRowNumber();
 			for (Cell cell : row.getCells()) {
				this.getModule().cellValue(rowNumber, cell.getColumnNumber(), cell.getValueOrFormula());
 			}
 		}
 		
 		return rows;
 	}
 	
 	protected abstract List<Row> extractRows(Object payload);
 	
 	@Override
 	public Object process() throws Exception {
 		throw new UnsupportedOperationException();
 	}
 	
 	@Override
 	public Object processWithExtraProperties(Map<String, Object> properties) throws Exception {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Object process(Object payload, Map<String, Object> properties) throws Exception {
 		throw new UnsupportedOperationException();
 	}
 
 }
