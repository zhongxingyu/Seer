 package eu.wuttke.nrf.ui.sample;
 
 import eu.wuttke.nrf.domain.sample.Sample;
 import eu.wuttke.nrf.ui.component.EditableListComposite;
 import eu.wuttke.nrf.ui.misc.StringToShortDateConverter;
 
 public class SampleListView 
 extends EditableListComposite<Sample> {
 		
 	public SampleListView() {
 		super(Sample.class);
 	}
 	
 	@Override
 	protected void initTable() {
 		getTable().setVisibleColumns(new String[] {"id", "sampleType", "sampleDate", "amount"});
 		getTable().setColumnHeaders(new String[] {"Sample Id", "Type", "Date", "Amount"});
		getTable().setConverter("sampleDate", new StringToShortDateConverter());
 	}
 		
 	private static final long serialVersionUID = 1L;
 
 }
