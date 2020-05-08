 /*
 $Revision: 1.3 $
 $Date: 2008-05-19 20:11:02 $
 
 The Web CGH Software License, Version 1.0
 
 Copyright 2003 RTI. This software was developed in conjunction with the
 National Cancer Institute, and so to the extent government employees are
 co-authors, any rights in such works shall be subject to Title 17 of the
 United States Code, section 105.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 
 1. Redistributions of source code must retain the above copyright notice, this 
 list of conditions and the disclaimer of Article 3, below. Redistributions in 
 binary form must reproduce the above copyright notice, this list of conditions 
 and the following disclaimer in the documentation and/or other materials 
 provided with the distribution.
 
 2. The end-user documentation included with the redistribution, if any, must 
 include the following acknowledgment:
 
 "This product includes software developed by the RTI and the National Cancer 
 Institute."
 
 If no such end-user documentation is to be included, this acknowledgment shall 
 appear in the software itself, wherever such third-party acknowledgments 
 normally appear.
 
 3. The names "The National Cancer Institute", "NCI", 
 Research Triangle Institute, and "RTI" must not be used to endorse or promote 
 products derived from this software.
 
 4. This license does not authorize the incorporation of this software into any 
 proprietary programs. This license does not authorize the recipient to use any 
 trademarks owned by either NCI or RTI.
 
 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, 
 (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE
 NATIONAL CANCER INSTITUTE, RTI, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 package org.rti.webgenome.service.session;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.rti.webgenome.domain.Array;
 import org.rti.webgenome.domain.Experiment;
 import org.rti.webgenome.domain.Organism;
 import org.rti.webgenome.domain.Principal;
 import org.rti.webgenome.domain.ShoppingCart;
 import org.rti.webgenome.service.job.Job;
 
 /**
  * This interface is a session facade to transactional
  * operations involving the WebGenome database.
  * @author dhall
  *
  */
 public interface WebGenomeDbService {
 
 	/**
 	 * Add array object to database and update the shopping
 	 * cart.
 	 * @param array Array object to save
 	 * @param cart Cart to update
 	 */
 	void saveArrayAndUpdateCart(Array array, ShoppingCart cart);
 	
 	/**
 	 * Get shopping cart associated with given user and security domain
 	 * that authenticated that user.
 	 * @param user User account name
 	 * @param domain Security domain that authenticated user
 	 * @return A shopping cart object
 	 */
 	ShoppingCart loadShoppingCart(String user, String domain);
 	
 	/**
 	 * Save given cart to persistent storage.
 	 * @param cart Cart to save
 	 */
 	void saveShoppingCart(ShoppingCart cart);
 	
 	/**
 	 * Update given shopping cart.
 	 * @param cart Cart to update.
 	 */
 	void updateShoppingCart(ShoppingCart cart);
 	
 	/**
 	 * Remove arrays associated from given experiment from database if they
 	 * are not referenced by other experiments
 	 * and persist shopping cart updates.
 	 * @param experiment Experiment containing arrays to remove if not
 	 * referenced by other experiments
 	 * @param cart Cart to update
 	 */
 	void deleteArraysAndUpdateCart(Experiment experiment, ShoppingCart cart);
 	
 	/**
 	 * Add arrays from given experiment to database and persist
 	 * shopping cart changes.
 	 * @param experiment Experiment containing arrays to persist.
 	 * @param cart Shopping cart to update
 	 */
 	void addArraysAndUpdateCart(Experiment experiment, ShoppingCart cart);
 	
 	/**
 	 * Update persistent state of given experiment.
 	 * @param experiment Experiment whose persistent state is to be
 	 * updated.
 	 */
 	void updateExperiment(Experiment experiment);
 	
 	/**
 	 * Update given experiments and shopping cart.
 	 * @param experiments Experiments to update.
 	 * @param cart Cart to update.
 	 */
 	void updateExperimentsAndCart(
 			Collection<Experiment> experiments, ShoppingCart cart);
 	
 	/**
 	 * Is <code>Experiment</code> object with given ID referenced
 	 * by another persistent object?
 	 * @param id Experiment ID
 	 * @return T/F
 	 */
 	boolean isExperimentReferenced(Long id);
 	
 	/**
 	 * Load default organism.
 	 * @return Default organism
 	 */
 	Organism loadDefaultOrganism();
 	
 	/**
 	 * Load all organisms.
 	 * @return All organisms
 	 */
 	List<Organism> loadAllOrganisms();
 	
 	/**
 	 * Load organism with given ID.
 	 * @param id Organism ID
 	 * @return An organism
 	 */
 	Organism loadOrganism(Long id);
 	
 	/**
 	 * Is <code>Plot</code> object with given ID
 	 * referenced by another persistent object?
 	 * @param id Plot ID
 	 * @return T/F
 	 */
 	boolean isPlotReferenced(Long id);
 	
 	/**
 	 * Save given principal to persistent storage.
 	 * @param principal Principal to persist
 	 */
 	void savePrincipal(Principal principal);
 	
 	/**
 	 * Load principal with given account name.
 	 * @param name Account name
 	 * @return A principal or null if there is not
 	 * one with given name
 	 */
 	Principal loadPrincipal(String name);
 	
 	/**
 	 * Load principal with given account name.
 	 * @param email Account email
 	 * @return A principal or null if there is not
 	 * one with given email
 	 */
 	Principal loadPrincipalByEmail(String email);
 	
 	
 	/**
 	 * Update persistent state of given principal.
 	 * @param principal Principal whose persistent state will
 	 * be updated
 	 */
 	void updatePrincipal(Principal principal);
 	
 	/**
 	 * Delete given principal from persistent storage.
 	 * @param principal Principal to delete
 	 */
 	void deletePrincipal(Principal principal);
 	
 	/**
 	 * Load principal from given domain with given username and password.
 	 * @param userName Account name
 	 * @param password Password
 	 * @param domain Security domain
 	 * @return A principal or null if there is no account
 	 * with the given username and password pairing in the given
 	 * security domain
 	 */
 	Principal loadPrincipal(String userName, String password,
 			String domain);
 	
 	/**
 	 * Gets the names of all image files that are referenced
 	 * by persistent objects.  Clients are expected to be
 	 * able to convert the file names into absolute paths.
 	 * @return Image file names (not absolute paths)
 	 */
 	Collection<String> getAllValidImageFileNames();
 	
 	/**
 	 * Gets the names of all data files that are referenced
 	 * by persistent objects.  Clients are expected to be
 	 * able to convert the file names into absolute paths.
 	 * @return Data file names (not absolute paths)
 	 */
 	Collection<String> getAllDataFileNames();
 	
 	/**
 	 * Load all compute jobs from persistent storage.
 	 * @return All persisted compute jobs
 	 */
 	Collection<Job> loadAllJobs();
 	
 	/**
 	 * Save or update the given job.
 	 * @param job Compute job
 	 */
 	void saveOrUpdateJob(Job job);
 	
 	/**
 	 * Remove given job from persistent storage.
 	 * @param job Job to remove
 	 */
 	void deleteJob(Job job);
 }
