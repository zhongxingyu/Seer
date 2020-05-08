 package ca.drusk.flightmanager.client.ui.main_pages.data_display;
 
 import ca.drusk.flightmanager.client.services.DataQueryServiceAsync;
 
 /**
  * Display for data from the Airlines table.
  * 
  * @author drusk
  * 
  */
 public class AirlineResultsDisplay extends AbstractDataDisplay {
 
 	public AirlineResultsDisplay(DataQueryServiceAsync dataQueryService) {
		super(dataQueryService);
 	}
 
 	@Override
 	protected void retrieveResultsAndAddToDisplay() {
 		dataQueryService.getAirlines(new TableGeneratingCallback());
 	}
 
 }
