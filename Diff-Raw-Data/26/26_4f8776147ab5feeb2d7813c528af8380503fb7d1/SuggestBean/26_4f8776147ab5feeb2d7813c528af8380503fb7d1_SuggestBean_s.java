 package com.jcertif.web.ihm.speaker;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.enterprise.context.RequestScoped;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import com.jcertif.web.model.Suggest;
 import com.jcertif.web.model.Sujet;
 import com.jcertif.web.model.User;
 import com.jcertif.web.service.ReferentielService;
 import com.jcertif.web.service.ResourceService;
 import com.jcertif.web.service.RestService;
 
 /**
  * @author rossi.oddet
  * 
  */
 @Named
 @RequestScoped
 public class SuggestBean {
 
 	private Suggest suggest;
 
 	/** REST Web Service **/
 	@Inject
 	private RestService restService;
 
 	/** Resource Service **/
 	@Inject
 	private ResourceService resourceService;
 
 	/** Referentiel Service **/
 	@Inject
 	private ReferentielService referentielService;
 
 	private List<String> selectedSujets;
 
 	public SuggestBean() {
 		suggest = new Suggest();
 	}
 
 	public void save() throws IOException {
 
 		FacesContext context = FacesContext.getCurrentInstance();
 
 		// Update Conference for user
 		this.suggest.setConferenceId(referentielService.getConference().getId());
 		List<Long> sujetIds = new ArrayList<Long>();
 		for (String libelle : selectedSujets) {
 			for (Sujet suj : referentielService.getSujets()) {
 				if (libelle.equals(suj.getLibelle())) {
 					sujetIds.add(suj.getId());
 				}
 			}
 
 		}
 		suggest.setSujetIds(sujetIds);
 
 		// Update user
 		User connectedUser = (User) context.getExternalContext().getSessionMap()
 				.get("connectedUser");
 		List<Long> participantIds = new ArrayList<Long>();
 		participantIds.add(connectedUser.getId());
 		suggest.setParticipantIds(participantIds);
 
 		restService.post(resourceService.getSuggestCreateContext(), suggest, Suggest.class);
 		context.getExternalContext().redirect(
 				context.getExternalContext().getRequestContextPath()
 						+ "/faces/speaker/confirmationSuggest.jsf");
 
 	}
 
 	public List<Sujet> getSujets() {
 
 		return referentielService.getSujets();
 
 	}
 
 	/**
 	 * @return the suggest
 	 */
 	public Suggest getSuggest() {
 		return suggest;
 	}
 
 	/**
 	 * @param suggest
 	 *            the suggest to set
 	 */
 	public void setSuggest(Suggest suggest) {
 		this.suggest = suggest;
 	}
 
 	/**
 	 * @return the selectedSujets
 	 */
 	public List<String> getSelectedSujets() {
 		return selectedSujets;
 	}
 
 	/**
 	 * @param selectedSujets
 	 *            the selectedSujets to set
 	 */
 	public void setSelectedSujets(List<String> selectedSujets) {
 		this.selectedSujets = selectedSujets;
 	}
 
 }
