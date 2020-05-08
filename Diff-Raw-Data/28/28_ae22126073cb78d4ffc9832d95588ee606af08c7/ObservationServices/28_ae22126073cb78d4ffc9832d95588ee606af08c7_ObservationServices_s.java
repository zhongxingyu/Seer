 package es.weso.rest;
 
 import java.util.ArrayDeque;
 import java.util.Collection;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import es.weso.business.IndicatorManagement;
 import es.weso.business.ObservationManagement;
 import es.weso.model.Indicator;
 import es.weso.model.Observation;
 import es.weso.model.ObservationWithoutIndicator;
 
 /**
  * Web services to retrieve {@link es.weso.model.Observation observations}
  * 
  * @author Alejandro Montes García
  * @since 03/07/2013
  * @version 1.0
  */
 @Controller
 @RequestMapping("/observation")
 public class ObservationServices {
 
 	private static ObservationManagement observationManager;
	private static IndicatorManagement indicationManager;
 
 	public void setObservationManager(ObservationManagement observationManager) {
 		ObservationServices.observationManager = observationManager;
 	}
 
	public void setIndicatorManager(IndicatorManagement indicationManager) {
		ObservationServices.indicationManager = indicationManager;
 	}
 
 	@RequestMapping(value = "/{uri}", method = RequestMethod.GET)
 	public String getObservationByUri(@PathVariable String uri, ModelMap model) {
 		model.addAttribute("observation",
 				observationManager.getObservationByURI(uri));
 		return "observation";
 	}
 
 	@RequestMapping(value = "/{country}/{year}/{indicator}", method = RequestMethod.GET)
 	public String getObservations(@PathVariable String country,
 			@PathVariable String year, @PathVariable String indicator,
 			ModelMap model) {
 		model.addAttribute("ranking", deleteIndicator(observationManager
 				.getRanking(indicator, Integer.parseInt(year))));
 		model.addAttribute("history", deleteIndicator(observationManager
 				.getHistory(country, indicator)));
 		model.addAttribute(
 				"observations",
 				deleteIndicator(observationManager.getBarchart(country,
 						Integer.parseInt(year), indicator)));
		Indicator ind = indicationManager.getIndicator(indicator);
 		model.addAttribute("indicator", ind);
 		int firstYear = Integer.parseInt(year) - 1;
 		int secondYear = Integer.parseInt(year) + 1;
 		if (ind.getStart() >= Integer.parseInt(year)) {
 			firstYear = secondYear;
 			secondYear++;
 		}
		if (ind.getEnd() <= Integer.parseInt(year)) {
 			secondYear = firstYear;
 			firstYear--;
 		}
 		model.addAttribute("relatedObservations1",
 				deleteIndicator(observationManager.getBarchart(country,
 						firstYear, indicator)));
		model.addAttribute(
				"relatedObservations2",
 				deleteIndicator(observationManager.getBarchart(country,
 						secondYear, indicator)));
 		return "observation";
 	}
 
 	private Collection<ObservationWithoutIndicator> deleteIndicator(
 			Collection<Observation> observations) {
 		Collection<ObservationWithoutIndicator> obs = new ArrayDeque<ObservationWithoutIndicator>(
 				observations.size());
 		for (Observation o : observations) {
 			obs.add(new ObservationWithoutIndicator(o));
 		}
 		return obs;
 	}
 }
