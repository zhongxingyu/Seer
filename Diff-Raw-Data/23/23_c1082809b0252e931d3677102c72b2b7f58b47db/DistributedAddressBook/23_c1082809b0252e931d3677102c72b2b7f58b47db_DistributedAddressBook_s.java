 package dacus.model.depository;
 
 import dacus.model.dossier.ForeignPartyFile;
import deus.model.depository.DistributedInformationFolder;
 import deus.model.user.id.UserId;
 
 
 public interface DistributedAddressBook<Id extends UserId> extends
 		DistributedInformationFolder<Id, ForeignPartyFile<Id>> {
 
 }
