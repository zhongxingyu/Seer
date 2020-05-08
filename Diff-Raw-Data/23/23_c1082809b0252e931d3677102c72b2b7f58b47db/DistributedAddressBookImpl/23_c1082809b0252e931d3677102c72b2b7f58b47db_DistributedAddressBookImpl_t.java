 package dacus.model.depository.impl;
 
 import dacus.model.depository.DistributedAddressBook;
 import dacus.model.dossier.ForeignPartyFile;
import deus.model.depository.generic.impl.DistributedInformationFolderImpl;
 import deus.model.user.id.UserId;
 
 public class DistributedAddressBookImpl<Id extends UserId> extends
 		DistributedInformationFolderImpl<Id, ForeignPartyFile<Id>> implements DistributedAddressBook<Id> {
 
 }
