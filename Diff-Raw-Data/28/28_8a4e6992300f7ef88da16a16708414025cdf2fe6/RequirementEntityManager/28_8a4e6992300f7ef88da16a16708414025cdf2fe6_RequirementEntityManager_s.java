 /*******************************************************************************
  * Copyright (c) 2013 WPI-Suite
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Team Rolling Thunder
  ******************************************************************************/
 package edu.wpi.cs.wpisuitetng.modules.requirementmanager.models;
 
 import java.util.List;
 
 import edu.wpi.cs.wpisuitetng.Session;
 import edu.wpi.cs.wpisuitetng.database.Data;
 import edu.wpi.cs.wpisuitetng.exceptions.BadRequestException;
 import edu.wpi.cs.wpisuitetng.exceptions.NotFoundException;
 import edu.wpi.cs.wpisuitetng.exceptions.NotImplementedException;
 import edu.wpi.cs.wpisuitetng.exceptions.UnauthorizedException;
 import edu.wpi.cs.wpisuitetng.exceptions.WPISuiteException;
 import edu.wpi.cs.wpisuitetng.modules.EntityManager;
 import edu.wpi.cs.wpisuitetng.modules.Model;
 import edu.wpi.cs.wpisuitetng.modules.core.models.Role;
 import edu.wpi.cs.wpisuitetng.modules.core.models.User;
import edu.wpi.cs.wpisuitetng.modules.requirementmanager.models.Requirement;
 
 /**
  * This is the entity manager for the Requirement in the
  * RequirementManager module.
  *
  */
 public class RequirementEntityManager implements EntityManager<Requirement> {
 
 	/** The database */
 	Data db;
 	
 	/**
 	 * Constructs the entity manager. This constructor is called by
 	 * {@link edu.wpi.cs.wpisuitetng.ManagerLayer#ManagerLayer()}. To make sure
 	 * this happens, be sure to place add this entity manager to the map in
 	 * the ManagerLayer file.
 	 * 
 	 * @param db a reference to the persistent database
 	 */
 	public RequirementEntityManager(Data db) {
 		this.db = db; 
 	}
 
 	/**
 	 * Saves a Requirement when it is received from a client
 	 * 
 	 * @see edu.wpi.cs.wpisuitetng.modules.EntityManager#makeEntity(edu.wpi.cs.wpisuitetng.Session, java.lang.String)
 	 */
 	@Override
 	public Requirement makeEntity(Session s, String content) throws WPISuiteException {
 		final Requirement newRequirement = Requirement.fromJson(content);
 		if(!db.save(newRequirement, s.getProject())) {
 			throw new WPISuiteException();
 		}
 		return newRequirement;
 	}
 	
 	/**
 	 * Retrieves a single requirement from the database
 	 * @param s the session
 	 * @param id the id number of the requirement to retrieve
 	 * @return the requirement matching the given id
 	 */
 	@Override
 	public Requirement[] getEntity(Session s, String id) throws NotFoundException {
 		final int intId = Integer.parseInt(id);
 		if(intId < 1) {
 			throw new NotFoundException();
 		}
 		Requirement[] requirements = null;
 		try {
 			requirements = db.retrieve(Requirement.class, "id", intId, s.getProject()).toArray(new Requirement[0]);
 		} catch (WPISuiteException e) {
 			e.printStackTrace();
 		}
 		if(requirements.length < 1 || requirements[0] == null) {
 			throw new NotFoundException();
 		}
 		return requirements;
 	}
 
 	/**
 	 * Retrieves all requirements from the database
 	 * @param s the session
 	 * @return array of all stored requirements
 	 */
 	@Override
 	public Requirement[] getAll(Session s) {
 		return db.retrieveAll(new Requirement(), s.getProject()).toArray(new Requirement[0]);
 	}
 
 	/**
 	 * Saves a data model to the database
 	 * @param s the session
 	 * @param model the model to be saved
 	 */
 	@Override
 	public void save(Session s, Requirement model) {
 		db.save(model, s.getProject());
 	}
 	
 	/**
 	 * Ensures that a user is of the specified role
 	 * @param session the session
 	 * @param role the role being verified
 	 * @throws WPISuiteException user isn't authorized for the given role
 	 */
 	private void ensureRole(Session session, Role role) throws WPISuiteException {
 		User user = (User) db.retrieve(User.class, "username", session.getUsername()).get(0);
 		if(!user.getRole().equals(role)) {
 			throw new UnauthorizedException();
 		}
 	}
 	
 	/**
 	 * Deletes a requirement from the database
 	 * @param s the session
 	 * @param id the id of the requirement to delete
 	 * @return true if the deletion was successful
 	 */
 	@Override
 	public boolean deleteEntity(Session s, String id) throws WPISuiteException {
 		ensureRole(s, Role.ADMIN);
 		return (db.delete(getEntity(s, id)[0]) != null) ? true : false;
 	}
 	
 	/**
 	 * Deletes all requirements from the database
 	 * @param s the session
 	 */
 	@Override
 	public void deleteAll(Session s) throws WPISuiteException {
 		ensureRole(s, Role.ADMIN);
 		db.deleteAll(new Requirement(), s.getProject());
 	}
 	
 	/**
 	 * Returns the number of requirements in the database
 	 * @return number of requirements stored
 	 */
 	@Override
 	public int Count() throws WPISuiteException {
 		return db.retrieveAll(new Requirement()).size();
 	}
 
 	@Override
 	public Requirement update(Session session, String content) throws WPISuiteException {
 		
 		Requirement updatedRequirement = Requirement.fromJson(content);
 		/*
 		 * Because of the disconnected objects problem in db4o, we can't just save Requirements.
 		 * We have to get the original defect from db4o, copy properties from updatedRequirement,
 		 * then save the original Requirement again.
 		 */
 		List<Model> oldRequirements = db.retrieve(Requirement.class, "id", updatedRequirement.getId(), session.getProject());
 		if(oldRequirements.size() < 1 || oldRequirements.get(0) == null) {
 			throw new BadRequestException("Requirement with ID does not exist.");
 		}
 				
 		Requirement existingRequirement = (Requirement)oldRequirements.get(0);		
 
 		// copy values to old requirement and fill in our changeset appropriately
 		existingRequirement.copyFrom(updatedRequirement);
 		
 		if(!db.save(existingRequirement, session.getProject())) {
 			throw new WPISuiteException();
 		}
 		
 		return existingRequirement;
 	}
 
 	@Override
 	public String advancedGet(Session arg0, String[] arg1) throws NotImplementedException {
 		throw new NotImplementedException();
 	}
 
 	@Override
 	public String advancedPost(Session arg0, String arg1, String arg2) throws NotImplementedException {
 		throw new NotImplementedException();
 	}
 
 	@Override
 	public String advancedPut(Session arg0, String[] arg1, String arg2) throws NotImplementedException {
 		throw new NotImplementedException();
 	}
 
 }
