 package fhdw.ipscrum.client.presenter;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Vector;
 
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.Panel;
 
 import fhdw.ipscrum.client.events.EventArgs;
 import fhdw.ipscrum.client.events.EventHandler;
 import fhdw.ipscrum.client.events.args.AssociatePersonAndRoleArgs;
 import fhdw.ipscrum.client.events.args.MultipleRoleArgs;
 import fhdw.ipscrum.client.events.args.PersonArgs;
 import fhdw.ipscrum.client.view.PersonRoleView;
 import fhdw.ipscrum.client.view.interfaces.IPersonRoleView;
 import fhdw.ipscrum.shared.SessionManager;
 import fhdw.ipscrum.shared.constants.TextConstants;
 import fhdw.ipscrum.shared.exceptions.ConsistencyException;
 import fhdw.ipscrum.shared.model.Person;
 import fhdw.ipscrum.shared.model.interfaces.IPerson;
 import fhdw.ipscrum.shared.model.interfaces.IRole;
 
 /**
  * Presenter for PersonRoleView. PRView is intended to be a central management console for persons and roles.
  * It provides controls for creating, modifying and deleting persons and roles as well associate roles to persons or remove associations.
  */
 public class PersonRolePresenter extends Presenter<IPersonRoleView> {
 
 	private IPersonRoleView concreteView;
 
 	/**
 	 * Constructor for PersonRolePresenter.
 	 * @param parent Panel
 	 */
 	public PersonRolePresenter(Panel parent) {
 		super(parent);
 	}
 
 	/**
 	 * Method createView.
 	 * 
 	 * @return IPersonRoleView
 	 */
 	@Override
 	protected IPersonRoleView createView() {
 		this.concreteView = new PersonRoleView();
 
 		this.updateGuiTables();
 		this.setupEventHandlers();
 
 		return this.concreteView;
 	}
 
 	/**
 	 * This method is called to update or fill the GUI with the model-data.
 	 */
 	private void updateGuiTables() {
 		HashSet<IPerson> personSet = SessionManager.getInstance().getModel().getPersons();
 		this.concreteView.updatePersonTable(personSet);
 
 		Person selPerson = this.concreteView.getSelectedPerson();
 		if (selPerson != null) {
 			this.concreteView.updateAssignedRoles(selPerson.getRoles());
 		} else {
 			this.concreteView.updateAssignedRoles(new Vector<IRole>());
 		}
 
 		HashSet<IRole> roleSet = SessionManager.getInstance().getModel().getRoles();
 		this.concreteView.updateAvailRoleList(roleSet);
 	}
 
 	/**
 	 * This method is called to set up the algorithms for each button of the GUI.
 	 */
 	private void setupEventHandlers() {
 
 		this.concreteView.defineNewPersonEventHandler(new EventHandler<EventArgs>() {
 			@Override
 			public void onUpdate(Object sender, EventArgs eventArgs) {
 				final DialogBox box = new DialogBox();
 				final PersonDialogPresenter presenter = new PersonDialogPresenter(box);
 				box.setAnimationEnabled(true);
 				box.setAutoHideEnabled(true);
 				box.setGlassEnabled(true);
 				box.setText(TextConstants.PERSONDIALOG_TITLE_CREATE);
 
 				presenter.getFinished().add(new EventHandler<EventArgs>() {
 					@Override
 					public void onUpdate(Object sender, EventArgs eventArgs) {
 						PersonRolePresenter.this.updateGuiTables();
 						box.hide();
 					}
 				});
 
 				presenter.getAborted().add(new EventHandler<EventArgs>() {
 					@Override
 					public void onUpdate(Object sender, EventArgs eventArgs) {
 						box.hide();
 					}
 				});
 				box.center();
 			}
 		});
 
 		this.concreteView.defineModifyPersonEventHandler(new EventHandler<PersonArgs>() {
 			@Override
 			public void onUpdate(Object sender, PersonArgs eventArgs) {
 				final DialogBox box = new DialogBox();
 				final PersonDialogPresenter presenter = new PersonDialogPresenter(box, eventArgs.getPerson());
 				box.setAnimationEnabled(true);
 				box.setAutoHideEnabled(true);
 				box.setGlassEnabled(true);
 				box.setText(eventArgs.getPerson().getFirstname() + " bearbeiten");
 				box.center();
 
 				presenter.getFinished().add(new EventHandler<EventArgs>() {
 					@Override
 					public void onUpdate(Object sender, EventArgs eventArgs) {
 						PersonRolePresenter.this.updateGuiTables();
 						box.hide();
 					}
 				});
 
 				presenter.getAborted().add(new EventHandler<EventArgs>() {
 					@Override
 					public void onUpdate(Object sender, EventArgs eventArgs) {
 						box.hide();
 					}
 				});
 			}
 		});
 
 		this.concreteView.defineRemoveRoleFromPersonEventHandler(new EventHandler<AssociatePersonAndRoleArgs>() {
 			@Override
 			public void onUpdate(Object sender, AssociatePersonAndRoleArgs eventArgs) {
 				if (eventArgs != null && eventArgs.getPerson() != null && eventArgs.getRoles().size() > 0) {
 					try {
 						eventArgs.getPerson().removeRole(eventArgs.getSingleRole());
 					} catch (ConsistencyException e) {
 						Window.alert(e.getMessage());
 					}
 					PersonRolePresenter.this.updateGuiTables();
 				}
 			}
 		});
 
 		this.concreteView.defineAddRoleToPersonEventHandler(new EventHandler<AssociatePersonAndRoleArgs>() {
 			@Override
 			public void onUpdate(Object sender, AssociatePersonAndRoleArgs eventArgs) {
 				if (eventArgs != null && eventArgs.getPerson() != null && eventArgs.getRoles().size() > 0) {
 					Iterator<IRole> i = eventArgs.getRoles().iterator();
 					while (i.hasNext()) {
 						IRole current = i.next();
 						try {
 							eventArgs.getPerson().addRole(current);
 						} catch (ConsistencyException e) {
							// This error is not severe. The user will notice that the role he wants to add is already in posession of the selected user.
 						}
 					}
 				}
 				PersonRolePresenter.this.updateGuiTables();
 			}
 		});
 
 		this.concreteView.defineNewRoleEventHandler(new EventHandler<EventArgs>() {
 			@Override
 			public void onUpdate(Object sender, EventArgs eventArgs) {
 				final DialogBox box = new DialogBox();
 				final RoleDialogPresenter presenter = new RoleDialogPresenter(box);
 				box.setAnimationEnabled(true);
 				box.setAutoHideEnabled(true);
 				box.setGlassEnabled(true);
 				box.setText(TextConstants.ROLEDIALOG_TITLE_CREATE);
 
 				presenter.getFinished().add(new EventHandler<EventArgs>() {
 					@Override
 					public void onUpdate(Object sender, EventArgs eventArgs) {
 						PersonRolePresenter.this.updateGuiTables();
 						box.hide();
 					}
 				});
 
 				presenter.getAborted().add(new EventHandler<EventArgs>() {
 					@Override
 					public void onUpdate(Object sender, EventArgs eventArgs) {
 						box.hide();
 					}
 				});
 
 				box.center();
 			}
 		});
 
 		this.concreteView.defineRemoveRoleEventHandler(new EventHandler<MultipleRoleArgs>() {
 			@Override
 			public void onUpdate(Object sender, MultipleRoleArgs eventArgs) {
 				for (IRole role : eventArgs.getRoles()) {
 					try {
 						SessionManager.getInstance().getModel().removeRole(role);
 					} catch (ConsistencyException e) {
 						Window.alert(e.getMessage());
 					}
 				}
 				PersonRolePresenter.this.updateGuiTables();
 			}
 		});
 	}
 }
