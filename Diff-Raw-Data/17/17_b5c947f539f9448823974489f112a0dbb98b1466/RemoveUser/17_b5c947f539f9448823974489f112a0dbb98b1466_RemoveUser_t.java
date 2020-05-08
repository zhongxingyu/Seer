 package com.technosophos.sinciput.commands.admin;
 
 import java.util.List;
 import java.util.Map;
 
 import com.technosophos.rhizome.command.AbstractCommand;
 import com.technosophos.rhizome.controller.CommandResult;
 import com.technosophos.rhizome.controller.ReRouteRequest;
 import com.technosophos.sinciput.types.admin.UserEnum;
 import com.technosophos.rhizome.repository.RepositoryAccessException;
 import com.technosophos.rhizome.repository.RepositorySearcher;
 import com.technosophos.rhizome.repository.DocumentRepository;
 import com.technosophos.rhizome.repository.RhizomeInitializationException;
 import com.technosophos.rhizome.document.Metadatum;
 
 import static com.technosophos.sinciput.servlet.ServletConstants.SETTINGS_REPO;
 
 public class RemoveUser extends AbstractCommand {
 
 	/**
 	 * Remove a user.
 	 * <p>There are two supposedly unique identifiers for a user. The Doc ID, which is truly
 	 * unique, and the username, which ought to be unique within the repository. This
 	 * will first check for a UID (aka doc ID), and if none is found, it will check for a username.</p>
 	 * <p>Once it has found a UID or username, it will attempt to delete the record.</p>
 	 * <p>If both a UUID and username are supplied, the command will only try to delete
 	 * the UUID.</p>
 	 */
 	public void doCommand(Map<String, Object> params,
 			List<CommandResult> results) throws ReRouteRequest {
 
 		RepositorySearcher search = null;
 		try {
 			search = this.repoman.getSearcher(SETTINGS_REPO);
 		} catch (RhizomeInitializationException e) {
 			String errMsg = "RepositorySearcher could not be retrieved.";
 			String friendlyErrMsg = "This server has had a woeful tragedy attempting to answer your request. Please try again when I stop whimpering.";
 			results.add(this.createErrorCommandResult(errMsg, friendlyErrMsg, e));
 			return;
 		}
 
 		// CASE 1: We get a document ID to delete.
 		if(this.hasParam(params, "id")){
 			/* 
 			 * Get a document, verify it is right, and delete it.
 			 */
 			String docID = this.getParam(params, "id").toString();
 			try {
 				// #1: Make sure doc exists.
 				DocumentRepository repo = this.repoman.getRepository(SETTINGS_REPO);
 				if( repo.hasDocument(docID)) {
 					// #2 Make sure the document is the correct type.
 					Metadatum m = search.getMetadatumByDocID(UserEnum.TYPE.getKey(), docID);
					if(!m.hasValue(UserEnum.TYPE.getFieldDescription().getDefaultValue())) {
 						String errMsg = String.format("DocID %s is not a user account: Wrong type.", docID);
 						String friendlyErrMsg = String.format("There is no user record with the ID %s. Nothing deleted.", docID);
 						results.add(this.createErrorCommandResult(errMsg, friendlyErrMsg));
 						return;
 					}
 					
 					// #3: Try to delete from index.
 					try {
 						this.repoman.getIndexer(SETTINGS_REPO).deleteFromIndex(docID);
 					} catch (RhizomeInitializationException e) {
 						String errMsg = String.format("Error deleting from indexer: %.", e.getMessage());
 						String friendlyErrMsg = String.format("The system could not access the record for %s. Nothing deleted.", docID);
 						results.add(this.createErrorCommandResult(errMsg, friendlyErrMsg, e));
 						return;
 					}
 					// #4:  Remove record from repository.
 					repo.removeDocument(docID);
 				} else {
 					String errMsg = String.format("DocID %s not found.", docID);
 					String friendlyErrMsg = String.format("There is no user record with the ID %s. Nothing deleted.", docID);
 					results.add(this.createErrorCommandResult(errMsg, friendlyErrMsg));
 					return;
 				}
 			} catch (RhizomeInitializationException e) {
 				String errMsg = "DocumentRepository could not be created.";
 				String friendlyErrMsg = String.format("The system could not access the record for %s. Nothing deleted.", docID);
 				results.add(this.createErrorCommandResult(errMsg, friendlyErrMsg, e));
 				return;
 			} catch (RepositoryAccessException e) {
 				String errMsg = String.format("User %s could not be deleted from the document repository.", docID);
 				String friendlyErrMsg = String.format("The system could not remove the record for %s. Nothing deleted.", docID);
 				results.add(this.createErrorCommandResult(errMsg, friendlyErrMsg, e));
 				return;
 			}
 			results.add(this.createCommandResult(String.format("User %s removed.")));
 			return;
 		
 		// CASE #2: Delete by user name
 		} else if(this.hasParam(params, UserEnum.USERNAME.getKey())) {
 			// Step #1: Get the user record with the given username
 			String username_field = UserEnum.USERNAME.getKey();
 			String username = this.getParam(params, username_field).toString();
 			try {
 				String [] docids = search.getDocIDsByMetadataValue(username_field, username); // RepositoryAccessException
 				if(docids == null) {
 					// Shouldn't happen
 					String errMsg = String.format("WARNING: Unexpected null looking for user %s.", username);
 					String friendlyErrMsg = String.format("There is no user named %s. Nothing deleted.", username);
 					results.add(this.createErrorCommandResult(errMsg, friendlyErrMsg));
 					return;
 				} else if( docids.length == 0 ) {
 					// No docs
 					String errMsg = String.format("User %s not found.", username);
 					String friendlyErrMsg = String.format("There is no user named %s. Nothing deleted.", username);
 					results.add(this.createErrorCommandResult(errMsg, friendlyErrMsg));
 					return;
 				} else if (docids.length == 1 ) {
 					// Step #2: BINGO! Now delete document 
 					try {
 						this.repoman.removeDocument(SETTINGS_REPO, docids[0]);
 					/*} catch (RhizomeInitializationException e) {
 						String errMsg = "DocumentRepository could not be created.";
 						String friendlyErrMsg = String.format("The system could not access the record for %s. Nothing deleted.", username);
 						results.add(this.createErrorCommandResult(errMsg, friendlyErrMsg, e));
 						return;*/
 					} catch (RepositoryAccessException e) {
 						String errMsg = String.format("User %s could not be deleted from the document repository.", username);
 						String friendlyErrMsg = String.format("The system could not remove the record for %s. Nothing deleted.", username);
 						results.add(this.createErrorCommandResult(errMsg, friendlyErrMsg, e));
 						return;
 					} catch (com.technosophos.rhizome.RhizomeException e) {
 						String errMsg = String.format("User %s could not be deleted from the document repository.", username);
 						String friendlyErrMsg = String.format("The system could not remove the record for %s. Nothing deleted.", username);
 						results.add(this.createErrorCommandResult(errMsg, friendlyErrMsg, e));
 						return;
 					}
 					results.add(this.createCommandResult(String.format("User %s removed.")));
 					return;
 				} else {
 					// Oops! How did we get multiple users? Better delete by docID instead!
 					String errMsg = String.format("WARNING: More than one user named %s!.", username);
 					String friendlyErrMsg = String.format("There are multiple users named %s. This is bad. Contact your system administrator. Nothing deleted.", username);
 					results.add(this.createErrorCommandResult(errMsg, friendlyErrMsg));
 					return;
 				}
 			} catch (RepositoryAccessException e) {
 				String errMsg = "RepositorySearcher could not be created.";
 				String friendlyErrMsg = String.format("Due to an error on our part, we could not find the record for %s. Nothing deleted.", username);
 				results.add(this.createErrorCommandResult(errMsg, friendlyErrMsg, e));
 				return;
 			}
 		// CASE #3: No user at all!
 		} else {
 			String errMsg = "No user or UUID provided.";
 			String friendlyErrMsg = "You must specify a user to delete.";
 			results.add(this.createErrorCommandResult(errMsg, friendlyErrMsg));
 			return;
 		}
 
 	}
 
 }
