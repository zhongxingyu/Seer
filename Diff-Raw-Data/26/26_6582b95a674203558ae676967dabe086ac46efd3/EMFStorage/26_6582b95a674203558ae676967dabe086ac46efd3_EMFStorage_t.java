 package org.jnect.emfstore;
 
 import java.security.AccessControlException;
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Observable;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EContentAdapter;
 import org.eclipse.emf.emfstore.client.model.ModelFactory;
 import org.eclipse.emf.emfstore.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.client.model.Usersession;
 import org.eclipse.emf.emfstore.client.model.Workspace;
 import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
 import org.eclipse.emf.emfstore.client.model.impl.WorkspaceImpl;
 import org.eclipse.emf.emfstore.client.model.util.EMFStoreClientUtil;
 import org.eclipse.emf.emfstore.client.model.util.EMFStoreCommand;
 import org.eclipse.emf.emfstore.common.model.ModelElementId;
 import org.eclipse.emf.emfstore.common.model.Project;
 import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.emf.emfstore.server.model.ProjectInfo;
 import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
 import org.eclipse.emf.emfstore.server.model.versioning.LogMessage;
 import org.eclipse.emf.emfstore.server.model.versioning.PrimaryVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.VersioningFactory;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.AbstractOperation;
 import org.eclipse.emf.emfstore.server.model.versioning.operations.AttributeOperation;
 import org.jnect.bodymodel.Body;
 import org.jnect.bodymodel.BodymodelFactory;
 import org.jnect.bodymodel.CenterHip;
 import org.jnect.bodymodel.CenterShoulder;
 import org.jnect.bodymodel.Head;
 import org.jnect.bodymodel.HumanLink;
 import org.jnect.bodymodel.LeftAnkle;
 import org.jnect.bodymodel.LeftElbow;
 import org.jnect.bodymodel.LeftFoot;
 import org.jnect.bodymodel.LeftHand;
 import org.jnect.bodymodel.LeftHip;
 import org.jnect.bodymodel.LeftKnee;
 import org.jnect.bodymodel.LeftShoulder;
 import org.jnect.bodymodel.LeftWrist;
 import org.jnect.bodymodel.PositionedElement;
 import org.jnect.bodymodel.RightAnkle;
 import org.jnect.bodymodel.RightElbow;
 import org.jnect.bodymodel.RightFoot;
 import org.jnect.bodymodel.RightHand;
 import org.jnect.bodymodel.RightHip;
 import org.jnect.bodymodel.RightKnee;
 import org.jnect.bodymodel.RightShoulder;
 import org.jnect.bodymodel.RightWrist;
 import org.jnect.bodymodel.Spine;
 
 public class EMFStorage extends Observable {
 
 	private static EMFStorage INSTANCE;
 	private static String PROJECT_NAME = "jnectEMFStorage";
 
 	ProjectSpace projectSpace;
 	Usersession usersession;
 
 	private Body replayBody;
 	private Body recordingBody;
 	private List<ChangePackage> changePackages;
 	private boolean changePackagesUpdateNeeded;
 
 	public static EMFStorage getInstance() {
 		if (INSTANCE == null) {
 			INSTANCE = new EMFStorage();
 		}
 		return INSTANCE;
 	}
 
 	protected EMFStorage() {
 		this.changePackagesUpdateNeeded = true;
 		connectToEMFStoreAndInit();
 	}
 
 	private void connectToEMFStoreAndInit() {
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				try {
 					// create a default Usersession and log in
 					usersession = EMFStoreClientUtil.createUsersession();
 					Workspace currentWorkspace = WorkspaceManager.getInstance().getCurrentWorkspace();
 					currentWorkspace.getUsersessions().add(usersession);
 					usersession.logIn();
 
					// search for existing storage project on server
 					Iterator<ProjectInfo> projectInfos = currentWorkspace.getRemoteProjectList(usersession).iterator();
 					ProjectInfo projectInfo = null;
 					while (projectInfos.hasNext()) {
 						ProjectInfo currentProjectInfo = projectInfos.next();
 						if (currentProjectInfo.getName().equals(PROJECT_NAME)) {
 							projectInfo = currentProjectInfo;
 							break;
 						}
 					}
 
					// if storage project is not existing on server create one, else retrieve it
 					if (projectInfo == null) {
 						projectSpace = ModelFactory.eINSTANCE.createProjectSpace();
 						projectSpace.setProject(org.eclipse.emf.emfstore.common.model.ModelFactory.eINSTANCE
 							.createProject());
 						projectSpace.setProjectName(PROJECT_NAME);
 						projectSpace.setProjectDescription("Project for jnect-storage");
 						projectSpace.setLocalOperations(ModelFactory.eINSTANCE.createOperationComposite());
 						projectSpace.initResources(currentWorkspace.eResource().getResourceSet());
 						((WorkspaceImpl) currentWorkspace).addProjectSpace(projectSpace);
 						currentWorkspace.save();
 						projectSpace.shareProject(usersession, new NullProgressMonitor());
 					} else {
						// check if we already have a local copy, else checkout the project
						boolean found = false;
						for (ProjectSpace ps : currentWorkspace.getProjectSpaces()) {
							if (ps.getProjectInfo().getName().equals(PROJECT_NAME)) {
								projectSpace = ps;
								projectSpace.setUsersession(usersession);
								found = true;
								break;
							}
						}
						if (!found) {
							projectSpace = currentWorkspace.checkout(usersession, projectInfo);
						}
 					}
 
 					Project project = projectSpace.getProject();
 					boolean found = false;
 					for (EObject obj : project.getAllModelElements()) {
 						if (obj instanceof Body) {
 							recordingBody = (Body) obj;
 							found = true;
 							break;
 						}
 					}
 					if (!found) {
 						recordingBody = createAndFillBody();
 						project.addModelElement(recordingBody);
 					}
 					recordingBody.eAdapters().add(new CommitBodyChangesAdapter());
 					projectSpace.commit(createLogMessage(usersession.getUsername(), "commit initial body"), null,
 						new NullProgressMonitor());
 				} catch (AccessControlException e) {
 					ModelUtil.logException(e);
 				} catch (EmfStoreException e) {
 					ModelUtil.logException(e);
 				}
 			}
 		}.run(false);
 
 	}
 
 	private Body createAndFillBody() {
 		Body body = BodymodelFactory.eINSTANCE.createBody();
 		BodymodelFactory factory = BodymodelFactory.eINSTANCE;
 		// create Elements
 		Head head = factory.createHead();
 		CenterShoulder shoulderCenter = factory.createCenterShoulder();
 		LeftShoulder shoulderLeft = factory.createLeftShoulder();
 		RightShoulder shoulderRight = factory.createRightShoulder();
 		LeftElbow elbowLeft = factory.createLeftElbow();
 		RightElbow elbowRight = factory.createRightElbow();
 		LeftWrist wristLeft = factory.createLeftWrist();
 		RightWrist wristRight = factory.createRightWrist();
 		LeftHand handLeft = factory.createLeftHand();
 		RightHand handRight = factory.createRightHand();
 		Spine spine = factory.createSpine();
 		CenterHip hipCenter = factory.createCenterHip();
 		LeftHip hipLeft = factory.createLeftHip();
 		RightHip hipRight = factory.createRightHip();
 		LeftKnee kneeLeft = factory.createLeftKnee();
 		RightKnee kneeRight = factory.createRightKnee();
 		LeftAnkle ankleLeft = factory.createLeftAnkle();
 		RightAnkle ankleRight = factory.createRightAnkle();
 		LeftFoot footLeft = factory.createLeftFoot();
 		RightFoot footRight = factory.createRightFoot();
 
 		// set color
 		footLeft.setColor_g(255);
 		footRight.setColor_g(255);
 		handLeft.setColor_r(255);
 		handLeft.setColor_g(0);
 		handLeft.setColor_b(0);
 		handRight.setColor_r(255);
 		head.setColor_b(255);
 
 		// add elements to body
 		body.setHead(head);
 		body.setLeftAnkle(ankleLeft);
 		body.setRightAnkle(ankleRight);
 		body.setLeftElbow(elbowLeft);
 		body.setRightElbow(elbowRight);
 		body.setLeftFoot(footLeft);
 		body.setRightFoot(footRight);
 		body.setLeftHand(handLeft);
 		body.setRightHand(handRight);
 		body.setCenterHip(hipCenter);
 		body.setLeftHip(hipLeft);
 		body.setRightHip(hipRight);
 		body.setLeftKnee(kneeLeft);
 		body.setRightKnee(kneeRight);
 		body.setCenterShoulder(shoulderCenter);
 		body.setLeftShoulder(shoulderLeft);
 		body.setRightShoulder(shoulderRight);
 		body.setSpine(spine);
 		body.setLeftWrist(wristLeft);
 		body.setRightWrist(wristRight);
 
 		// create links
 		createLink(head, shoulderCenter, body);
 		createLink(shoulderCenter, shoulderLeft, body);
 		createLink(shoulderCenter, shoulderRight, body);
 		createLink(shoulderLeft, elbowLeft, body);
 		createLink(shoulderRight, elbowRight, body);
 		createLink(elbowLeft, wristLeft, body);
 		createLink(elbowRight, wristRight, body);
 		createLink(wristLeft, handLeft, body);
 		createLink(wristRight, handRight, body);
 		createLink(shoulderCenter, spine, body);
 		createLink(spine, hipCenter, body);
 		createLink(hipCenter, hipLeft, body);
 		createLink(hipCenter, hipRight, body);
 		createLink(hipLeft, kneeLeft, body);
 		createLink(hipRight, kneeRight, body);
 		createLink(kneeLeft, ankleLeft, body);
 		createLink(kneeRight, ankleRight, body);
 		createLink(ankleLeft, footLeft, body);
 		createLink(ankleRight, footRight, body);
 		return body;
 	}
 
 	private void createLink(PositionedElement source, PositionedElement target, Body body) {
 		HumanLink link = BodymodelFactory.eINSTANCE.createHumanLink();
 		link.setSource(source);
 		link.setTarget(target);
 
 		source.getOutgoingLinks().add(link);
 		target.getIncomingLinks().add(link);
 
 		body.getLinks().add(link);
 	}
 
 	private LogMessage createLogMessage(String name, String message) {
 		LogMessage logMessage = VersioningFactory.eINSTANCE.createLogMessage();
 		logMessage.setAuthor(name);
 		logMessage.setDate(Calendar.getInstance().getTime());
 		logMessage.setClientDate(Calendar.getInstance().getTime());
 		logMessage.setMessage(message);
 		return logMessage;
 	}
 
 	public Body getRecordingBody() {
 		return recordingBody;
 	}
 
 	public Body getReplayingBody() {
 		if (replayBody == null)
 			replayBody = createAndFillBody();
 		return replayBody;
 	}
 
 	public int getReplayStatesCount() {
 		return changePackages.size();
 	}
 
 	public void initReplay() {
 		if (changePackagesUpdateNeeded) {
 			PrimaryVersionSpec start = VersioningFactory.eINSTANCE.createPrimaryVersionSpec();
 			start.setIdentifier(1);
 			try {
 				changePackages = projectSpace.getChanges(start, projectSpace.getBaseVersion());
 				changePackagesUpdateNeeded = false;
 			} catch (EmfStoreException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Replays the body model from emfstore
 	 * 
 	 * @param initCommit
 	 * @throws EmfStoreException
 	 */
 	public void replay(final int version) {
 		Thread replayThread = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				List<AbstractOperation> operations;
 
 				for (int i = version; i < changePackages.size(); i++) {
 					ChangePackage cp = changePackages.get(i);
 					cp.getOperations();
 					operations = cp.getLeafOperations();
 
 					for (AbstractOperation o : operations) {
 						replayElement(o);
 					}
 					setChanged();
 					notifyObservers(i);
 					try {
 						// pause for a moment to see changes
 						Thread.sleep(250);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		});
 		replayThread.start();
 
 	}
 
 	private void replayElement(AbstractOperation o) {
 		if (o instanceof AttributeOperation) {
 			AttributeOperation ao = (AttributeOperation) o;
 			ModelElementId id = ao.getModelElementId();
 			EObject element = projectSpace.getProject().getModelElement(id);
 			Object newValue = ao.getNewValue();
 			String attribute = ao.getFeatureName(); // gets attribute name
 
 			if (element instanceof Head) {
 				setValue(attribute, replayBody.getHead(), newValue);
 			} else if (element instanceof CenterShoulder) {
 				setValue(attribute, replayBody.getCenterShoulder(), newValue);
 			} else if (element instanceof LeftShoulder) {
 				setValue(attribute, replayBody.getLeftShoulder(), newValue);
 			} else if (element instanceof RightShoulder) {
 				setValue(attribute, replayBody.getRightShoulder(), newValue);
 			} else if (element instanceof LeftElbow) {
 				setValue(attribute, replayBody.getLeftElbow(), newValue);
 			} else if (element instanceof RightElbow) {
 				setValue(attribute, replayBody.getRightElbow(), newValue);
 			} else if (element instanceof LeftWrist) {
 				setValue(attribute, replayBody.getLeftWrist(), newValue);
 			} else if (element instanceof RightWrist) {
 				setValue(attribute, replayBody.getRightWrist(), newValue);
 			} else if (element instanceof LeftHand) {
 				setValue(attribute, replayBody.getLeftHand(), newValue);
 			} else if (element instanceof RightHand) {
 				setValue(attribute, replayBody.getRightHand(), newValue);
 			} else if (element instanceof Spine) {
 				setValue(attribute, replayBody.getSpine(), newValue);
 			} else if (element instanceof CenterHip) {
 				setValue(attribute, replayBody.getCenterHip(), newValue);
 			} else if (element instanceof LeftHip) {
 				setValue(attribute, replayBody.getLeftHip(), newValue);
 			} else if (element instanceof RightHip) {
 				setValue(attribute, replayBody.getRightHip(), newValue);
 			} else if (element instanceof LeftKnee) {
 				setValue(attribute, replayBody.getLeftKnee(), newValue);
 			} else if (element instanceof RightKnee) {
 				setValue(attribute, replayBody.getRightKnee(), newValue);
 			} else if (element instanceof LeftAnkle) {
 				setValue(attribute, replayBody.getLeftAnkle(), newValue);
 			} else if (element instanceof RightAnkle) {
 				setValue(attribute, replayBody.getRightAnkle(), newValue);
 			} else if (element instanceof LeftFoot) {
 				setValue(attribute, replayBody.getLeftFoot(), newValue);
 			} else if (element instanceof RightFoot) {
 				setValue(attribute, replayBody.getRightFoot(), newValue);
 			}
 		}
 	}
 
 	private void setValue(String attribute, PositionedElement element, Object value) {
 		if (attribute.equalsIgnoreCase("x")) {
 			element.setX((Float) value);
 		} else if (attribute.equalsIgnoreCase("y")) {
 			element.setY((Float) value);
 		} else if (attribute.equalsIgnoreCase("z")) {
 			element.setZ((Float) value);
 		}
 	}
 
 	public void setReplayToState(int state) {
 		ChangePackage cp = changePackages.get(state);
 		cp.getOperations();
 		List<AbstractOperation> operations = cp.getLeafOperations();
 
 		for (AbstractOperation o : operations) {
 			replayElement(o);
 		}
 	}
 
 	private void commitBodyChanges() {
 		// commit the pending changes of the project to the EMF Store
 		try {
 			projectSpace.commit(createLogMessage(usersession.getUsername(), "commit new state"), null,
 				new NullProgressMonitor());
 			changePackagesUpdateNeeded = true;
 		} catch (EmfStoreException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private class CommitBodyChangesAdapter extends EContentAdapter {
 		// 3 changes (x, y, z) in every body element
 		private final int NEEDED_CHANGES = 3 * recordingBody.eContents().size();
 
 		@Override
 		public void notifyChanged(Notification notification) {
 			super.notifyChanged(notification);
 			// once there a NEEDED_CHANGES local changes all body elements are updated and they can be committed
 			if (projectSpace.getLocalOperations().getOperations().size() == NEEDED_CHANGES) {
 				commitBodyChanges();
 			}
 		}
 	}
 
 }
