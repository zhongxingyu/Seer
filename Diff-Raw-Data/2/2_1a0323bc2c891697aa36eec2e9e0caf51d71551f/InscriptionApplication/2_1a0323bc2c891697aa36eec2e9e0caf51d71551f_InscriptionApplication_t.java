 /**
  * 
  */
 package com.jcertif.presentation.ui.inscription;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.jcertif.presentation.data.bo.Adresse;
 import com.jcertif.presentation.data.bo.participant.Participant;
 import com.jcertif.presentation.data.bo.participant.ProfilUtilisateur;
 import com.jcertif.presentation.data.bo.participant.RoleParticipant;
 import com.jcertif.presentation.internationalisation.Messages;
 import com.jcertif.presentation.ui.inscription.adresse.AdresseForm;
 import com.jcertif.presentation.ui.inscription.complement.ComplementForm;
 import com.jcertif.presentation.ui.inscription.participant.ParticipantForm;
 import com.jcertif.presentation.ui.inscription.profilutilisateur.ProfilUtilisateurBean;
 import com.jcertif.presentation.ui.inscription.profilutilisateur.ProfilUtilisateurForm;
 import com.jcertif.presentation.ui.util.PaysUtility;
 import com.jcertif.presentation.ui.util.UIConst;
 import com.jcertif.presentation.wsClient.ParticipantClient;
 import com.sun.jersey.api.client.UniformInterfaceException;
 import com.vaadin.Application;
 import com.vaadin.data.util.BeanItem;
 import com.vaadin.terminal.ExternalResource;
 import com.vaadin.terminal.UserError;
 import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Window;
 import com.vaadin.ui.Window.CloseEvent;
 import com.vaadin.ui.Window.CloseListener;
 
 /**
  * Inscription Application.
  * 
  * @author rossi
  * 
  */
 public class InscriptionApplication extends Application implements ClickListener,
 		HttpServletRequestListener, CloseListener {
 
 	private static final long serialVersionUID = 1L;
 	private static final Logger LOGGER = LoggerFactory.getLogger(InscriptionApplication.class);
 
 	private ProfilUtilisateurForm profilForm;
 
 	private ParticipantForm participantForm;
 
 	private AdresseForm addressForm;
 
 	private ComplementForm complementForm;
 
 	private String contextPath;
 
 	private Button commitButton;
 
 	private boolean complementFormOK;
 
 	@Override
 	public void init() {
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug("Initialize Inscription Application");
 		}
 		final Window mainWindow = new Window();
 		setMainWindow(mainWindow);
 
 		refreshForm(false);
 
 	}
 
 	private void refreshForm(boolean complement) {
 		getMainWindow().getContent().removeAllComponents();
 		getMainWindow().getContent().addComponent(getProfilForm());
 		HorizontalLayout hLayout = new HorizontalLayout();
 
 		hLayout.addComponent(getParticipantForm());
 		hLayout.addComponent(new Label("                                             "));
 		hLayout.addComponent(getAddressForm());
 		getMainWindow().getContent().addComponent(hLayout);
 		getMainWindow().getContent().addComponent(getCommitButton());
 	}
 
 	@Override
 	public void buttonClick(ClickEvent event) {
 		if (event.getButton().equals(getComplementForm().getValidateBt())) {
 			if (getComplementForm().isValid()) {
 				getMainWindow().removeWindow((getComplementForm()));
 				commitAndSaveParticipant(true);
 			} else {
 				getComplementForm().setComponentError(
 						new UserError(Messages.getString("ui.inscription.complement.msgerror")));
 			}
 		} else {
 			commitAndSaveParticipant(false);
 		}
 
 	}
 
 	/**
 	 * @param complementFormOK
 	 */
 	private void commitAndSaveParticipant(boolean complementFormOK) {
 		if (commitProfilForm()) {
 			if (commitParticipant()) {
 				Participant participant = ((BeanItem<Participant>) getParticipantForm()
 						.getItemDataSource()).getBean();
 
 				RoleParticipant selectedRole = participant.getRoleparticipant();
 
 				if (!complementFormOK
 						&& (UIConst.ROLE_SPEAKER.equals(selectedRole.getCode()) || UIConst.ROLE_PARTENAIRE
 								.equals(selectedRole.getCode()))) {
 
 					getComplementForm().setPositionX(400);
 					getComplementForm().setPositionY(400);
 					getMainWindow().addWindow((getComplementForm()));
 
 				} else {
 					saveParticipant();
 				}
 
 			}
 		}
 	}
 
 	/**
 	 * @param participant
 	 */
 	private void saveParticipant() {
 		Participant participant = ((BeanItem<Participant>) getParticipantForm().getItemDataSource())
 				.getBean();
 		ProfilUtilisateur profilUtilisateur = ((BeanItem<ProfilUtilisateurBean>) getProfilForm()
 				.getItemDataSource()).getBean();
 
 		participant.setEmail(profilUtilisateur.getEmail());
 		participant.setProfilUtilisateur(profilUtilisateur);
 
 		String bio = (String) getComplementForm().getBioTx().getValue();
 		participant.setDetails(bio);
 		File photo = getComplementForm().getFile();
 
 		Adresse adresse = ((BeanItem<Adresse>) getAddressForm().getItemDataSource()).getBean();
 
 		// Update pays
 		if (adresse.getPays() != null) {
 			adresse.setPays(PaysUtility.getName(adresse.getPays()));
 		}
 
 		participant.setAdresse(adresse);
 		Participant parti = ParticipantClient.getInstance().create_XML(participant);
 
 		try {
 			String[] extensionTab = photo.getName().split("\\.");
 			String ext = extensionTab[extensionTab.length - 1];
 			ParticipantClient.getInstance().store(photo,
 					participant.getRoleparticipant().getCode(), parti.getId(), ext);
 		} catch (UniformInterfaceException e) {
 			LOGGER.error("Erreur lors de la sauvegarde de la photo du participant", e);
 		} catch (FileNotFoundException e) {
 			LOGGER.error("Erreur lors de la sauvegarde de la photo du participant", e);
 		}
 		getParticipantForm().reinitParticipantBean();
 		getProfilForm().reinitProfilBean();
 		getAddressForm().reinitAdresseBean();
 		getComplementForm().reinitComplementBean();
 		ExternalResource res = new ExternalResource(contextPath + UIConst.CONFIRMATION_VIEW);
 		this.getMainWindow().open(res);
 	}
 
 	private boolean commitParticipant() {
 		boolean result = true;
 		getParticipantForm().commit();
 		ParticipantClient client = ParticipantClient.getInstance();
 		if (client.isEmailExist(((BeanItem<Participant>) getParticipantForm().getItemDataSource())
 				.getBean().getEmail())) {
 			getParticipantForm().setComponentError(
 					new UserError("Cette adresse email est dj utilis."));
 			result = false;
 		}
 		return result;
 	}
 
 	private boolean commitProfilForm() {
 		boolean result = true;
 		getProfilForm().commit();
 		if (!getProfilForm().getField("confirmEmail").getValue()
 				.equals(getProfilForm().getField("email").getValue())) {
 			getProfilForm().setComponentError(
 					new UserError("Les adresses email saisies ne sont identiques."));
 			result = false;
 		} else if (!getProfilForm().getField("confirmPassword").getValue()
 				.equals(getProfilForm().getField("password").getValue())) {
 			getProfilForm().setComponentError(
 					new UserError("Les adresses email saisies ne sont identiques."));
 			result = false;
 		}
 		return result;
 	}
 
 	/**
 	 * @return the profilForm
 	 */
 	public ProfilUtilisateurForm getProfilForm() {
 		if (profilForm == null) {
 			profilForm = new ProfilUtilisateurForm();
 		}
 		return profilForm;
 	}
 
 	/**
 	 * @return the commitButton
 	 */
 	public Button getCommitButton() {
 		if (commitButton == null) {
 			commitButton = new Button(Messages.getString("inscription.button"));
 			commitButton.addListener(this);
 		}
 		return commitButton;
 	}
 
 	/**
 	 * @return the complementForm
 	 */
 	public ComplementForm getComplementForm() {
 		if (complementForm == null) {
 			complementForm = new ComplementForm();
 			complementForm.getValidateBt().addListener(this);
 			complementForm.addListener(this);
 		}
 		return complementForm;
 	}
 
 	/**
 	 * @return the participantForm
 	 */
 	public ParticipantForm getParticipantForm() {
 		if (participantForm == null) {
 			participantForm = new ParticipantForm();
 		}
 		return participantForm;
 	}
 
 	/**
 	 * @return the addressForm
 	 */
 	public AdresseForm getAddressForm() {
 		if (addressForm == null) {
 			addressForm = new AdresseForm();
 		}
 		return addressForm;
 	}
 
 	@Override
 	public void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
 		contextPath = request.getContextPath();
 	}
 
 	@Override
 	public void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * @see com.vaadin.ui.Window.CloseListener#windowClose(com.vaadin.ui.Window.CloseEvent)
 	 */
 	@Override
 	public void windowClose(CloseEvent e) {
 
 		//
 		if (e.getWindow().equals(getComplementForm())) {
			getComplementForm().reinitComplementBean();
 		}
 
 	}
 
 }
