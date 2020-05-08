 /**
  * @author
  * wolfgang
  * Teves
  * @version
  * 0.6
  */
 package ac.at.fhkufstein.EventFunc;
 
 import ac.at.fhkufstein.bean.BmwEventController;
 import ac.at.fhkufstein.bean.BmwParticipantsController;
 import ac.at.fhkufstein.bean.BmwUserController;
 import ac.at.fhkufstein.bean.PersonenController;
 import ac.at.fhkufstein.entity.BmwEvent;
 import ac.at.fhkufstein.entity.BmwParticipants;
 import ac.at.fhkufstein.entity.BmwUser;
 import ac.at.fhkufstein.entity.Personen;
 import ac.at.fhkufstein.session.BmwParticipantsFacade;
 import ac.at.fhkufstein.session.BmwUserFacade;
 import ac.at.fhkufstein.session.PersonenFacade;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 import javax.persistence.EntityManager;
 
 @ManagedBean(name = "Participants")
 @ViewScoped
 public class Participants implements Serializable {
 	/*
 	 * This Bean Provides Functionality for adding Participants
 	 */
 
 	//private BmwUserController bmwUserController;
 	private BmwEventController bmwEventController;// Controllers
 	private BmwParticipantsController bmwParticipantsController;
 	private BmwUserController bmwUserController;
 	private PersonenController personenController;
 	private Integer eventID; //Current Event ID
 	private List<BmwUser> u = new ArrayList<BmwUser>();//List of Users
 	private BmwEvent current;//Current Event
 	private BmwUser[] selected; //Selected Participants
 	private ArrayList<BmwParticipants> selectedp; //Selected Participants
 	private ParticipantDataModel selectedParticipant;
 	private BmwParticipants selectedPart;
 	private String rep;
 	private BmwParticipants pc;//current Participant
 	private String pcState;//Current Participant State
 	//	public EntityManager em;
 
 	/**
 	 * Creates
 	 * a
 	 * new
 	 * instance
 	 * of
 	 * bmwEventFunc
 	 */
 	public Participants() {
 
 
 		try {
 
 
 
 			//bmwUserController = FacesContext.getCurrentInstance().getApplication().evaluateExpressionGet(FacesContext.getCurrentInstance(), "#{bmwUserController}", BmwUserController.class);
 			//Instanciate Controllers
 			personenController = FacesContext.getCurrentInstance().getApplication().evaluateExpressionGet(FacesContext.getCurrentInstance(), "#{personenController}", PersonenController.class);
 			bmwEventController = FacesContext.getCurrentInstance().getApplication().evaluateExpressionGet(FacesContext.getCurrentInstance(), "#{bmwEventController}", BmwEventController.class);
 			bmwParticipantsController = FacesContext.getCurrentInstance().getApplication().evaluateExpressionGet(FacesContext.getCurrentInstance(), "#{bmwParticipantsController}", BmwParticipantsController.class);
 			bmwUserController = FacesContext.getCurrentInstance().getApplication().evaluateExpressionGet(FacesContext.getCurrentInstance(), "#{bmwUserController}", BmwUserController.class);
 
 			//Get eventID from URL
 			FacesContext facesContext = FacesContext.getCurrentInstance();
 			this.eventID = (Integer) Integer.parseInt(facesContext.getExternalContext().getRequestParameterMap().get("eventID"));
 
 			//Get Current Event for eventID
 			current = bmwEventController.getFacade().find(eventID);
 
 
 			//Load participants for Event
 			EntityManager em = ((BmwParticipantsFacade) bmwParticipantsController.getFacade()).getEntityManager();
 			List p = em.createNamedQuery("BmwParticipants.findByEventId")
 					.setParameter("id", current)
 					.getResultList();
 
 
 			//Set Size for Arrays
 			selected = new BmwUser[p.size()];
 			selectedp = new ArrayList();
 			//selectedp = new BmwParticipants[p.size()];
 
 			//Iterate alle Participants
 			Iterator<BmwParticipants> it = p.iterator();
 			int ii = 0;
 
 			while (it.hasNext()) {
 				BmwParticipants x = it.next();
 				selected[ii] = x.getUserId();//Write in Vars
 				selectedp.add(x);
 				//selectedp[ii] = x;
 				ii++;
 			}
 			selectedParticipant = new ParticipantDataModel(selectedp);
 		} catch (Exception ex) {
 			System.out.println(ex.getMessage());
 		}
 	}
 
 	public List<String> autoComplete(String query) {
 		List<String> results = new ArrayList<String>();
 
 		EntityManager em = ((PersonenFacade) personenController.getFacade()).getEntityManager();
 		List p = em.createNamedQuery("Personen.searchByEMail1")
 				.setParameter("eMail1", "%" + query + "%")
 				.getResultList();
 
 		System.out.println("Anzahl gefunden:" + p.size());
 		Iterator<Personen> it = p.iterator();
 
 		while (it.hasNext()) {
 			Personen x = it.next();
 			results.add(x.getEMail1());
 			System.out.println("email" + x.getEMail1());
 
 		}
 
 		return results;
 	}
 
 	public void saveRepresentant() {
 					FacesContext context = FacesContext.getCurrentInstance();
 
 		System.out.println("Saving" + rep);
 		//Get Person to email
 		
 		EntityManager em = ((PersonenFacade) personenController.getFacade()).getEntityManager();
 		List<Personen> p = em.createNamedQuery("Personen.findByEMail1")
 				.setParameter("eMail1", rep)
 				.getResultList();
 		if(p.size()>0){//wants to set representation
 		Personen blah = p.get(0);
 		
 		//Get User
 		EntityManager em2 = ((BmwUserFacade) bmwUserController.getFacade()).getEntityManager();
 		List<BmwUser> p2 = em.createNamedQuery("BmwUser.findByPersonenID")
 				.setParameter("personenID", blah)
 				.getResultList();
 
 		BmwUser blah2 = p2.get(0);
 
 
 		//Check if already participant
 
 		EntityManager em3 = ((BmwParticipantsFacade) bmwParticipantsController.getFacade()).getEntityManager();
 		List<BmwParticipants> p3 = em.createNamedQuery("BmwParticipants.findByUserIdAndEventId")
 				.setParameter("userId", blah2)
 				.setParameter("eventId", current)
 				.getResultList();
 		if (p3.size() > 0) {
 			pc = p3.get(0);
 			//Is Already Participant
 			System.out.println("Ist schon Teilnehmer");
 
 			context.addMessage(null, new FacesMessage("Schon Teilnehmer", "Die gewählte Vertretung ist Bereits Teilnehmer")); //Send message
 
 
 		} else {
 
 			BmwUser newuser = blah2;
 			//Saving new
 			pc = bmwParticipantsController.prepareCreate(null);
 			pc.setUserId(newuser);
 			pc.setEventId(current);
 			pc.setPState("eingeladen");//Set state to eingeladen. This is the initial State
 			System.out.println("Added:" + newuser.getPersonenID().getNameVollstaendig() + " to event" + current.getName() + "with old user" + selectedPart.getUserId().getPersonenID().getNameVollstaendig());
 			bmwParticipantsController.setSelected(pc);
 			bmwParticipantsController.saveNew(null);//save to database
 			System.out.println("changin old participant state to rep_set");
 			selectedPart.setPState("vertretung_gewaehlt");
 			bmwParticipantsController.setSelected(selectedPart);
 			bmwParticipantsController.save(null);
 			
 			context.addMessage(null, new FacesMessage("Vertretung hinzugefügt", "Die gewählte Vertretung wurde hinzugefügt.")); //Send message
 
 		}
 		}else{
 			pc=selectedPart;//Change state of selectet part
 		}
 		System.out.println("changing state to"+pcState);
 		pc.setPState(pcState);
 		bmwParticipantsController.setSelected(pc);
 		bmwParticipantsController.save(null);
 		
 			context.addMessage(null, new FacesMessage("Status gesetzt", "Status wurde gesetzt")); //Send message
 
 	}
 
 	public Integer getEventID() {
 		return eventID;
 	}
 
 	public BmwUser[] getSelected() {
 		return selected;
 	}
 
 	public void setSelected(BmwUser[] selected) {
 		this.selected = selected;
 	}
 
 	public void saveSelected() {//Saves the participants
 		//Get all Participants for Event
 		EntityManager em = ((BmwParticipantsFacade) bmwParticipantsController.getFacade()).getEntityManager();
 		List p = em.createNamedQuery("BmwParticipants.findByEventId")
 				.setParameter("id", current)
 				.getResultList();
 
 		//System.out.println("Size:" + p.size());
 		//System.out.println("Anzahl Selected" + selected.length);
 		int i = 0;
 		while (i < selected.length) {
 			BmwUser b = selected[i];
 
 			Iterator<BmwParticipants> it = p.iterator();
 
 			while (it.hasNext()) {
 				//check if element is already in database, if yes, delete from array "selected"
 				if (b.getUid() == it.next().getUserId().getUid()) {//Delete all Participants which are already in database from selected array, we dont want to invite them again. Delete of an invitation is currently not possible
 					selected[i] = null;
 				}
 			}
 			i++;
 		}
 
 		int ii = 0;
 		while (ii < selected.length) { //save to database
 
 			if (selected[ii] != null) {//Only those which are left in the array
 				BmwUser b = selected[ii];
 
 				BmwParticipants pc = bmwParticipantsController.prepareCreate(null);
 				pc.setUserId(b);
 				pc.setEventId(current);
 				pc.setPState("eingeladen");//Set state to eingeladen. This is the initial State
 				System.out.println("Added:" + b.getPersonenID().getNameVollstaendig());
 				bmwParticipantsController.setSelected(pc);
 				bmwParticipantsController.saveNew(null);//save to database
 			}
 			ii++;
 		}
 
 		FacesContext context = FacesContext.getCurrentInstance();
 
		context.addMessage(null, new FacesMessage("Event", "Teilnehmer gespeichert")); //Send message
 
 
 	}
 
 	public void setEventID(Integer eventID) {
 		this.eventID = eventID;
 	}
 
 	public BmwEvent getCurrent() {
 		return current;
 	}
 
 	public void setCurrent(BmwEvent current) {
 		this.current = current;
 
 	}
 
 	public List<BmwUser> getU() {
 		return u;
 	}
 
 	public void setU(List<BmwUser> u) {
 		this.u = u;
 	}
 
 	public ArrayList<BmwParticipants> getSelectedp() {
 		return selectedp;
 	}
 
 	public void setSelectedp(ArrayList<BmwParticipants> selectedp) {
 		this.selectedp = selectedp;
 	}
 
 	public ParticipantDataModel getSelectedParticipant() {
 		return selectedParticipant;
 	}
 
 	public void setSelectedParticipant(ParticipantDataModel selectedParticipant) {
 		this.selectedParticipant = selectedParticipant;
 	}
 
 	public BmwParticipants getSelectedPart() {
 		return selectedPart;
 	}
 
 	public void setSelectedPart(BmwParticipants selectedPart) {
 		this.selectedPart = selectedPart;
 	}
 
 	public String getRep() {
 		return rep;
 	}
 
 	public void setRep(String rep) {
 		this.rep = rep;
 	}
 
 	public String getPcState() {
 		return pcState;
 	}
 
 	public void setPcState(String pcState) {
 		this.pcState = pcState;
 	}
 }
