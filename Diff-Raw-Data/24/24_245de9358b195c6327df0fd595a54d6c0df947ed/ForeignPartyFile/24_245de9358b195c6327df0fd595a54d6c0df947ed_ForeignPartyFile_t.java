 package dacus.model.dossier;
 
 import java.util.Set;
 
import deus.model.common.dossier.DigitalCard;
import deus.model.common.user.UserMetadata;
import deus.model.common.user.id.UserId;
 import deus.model.difgoverning.ForeignInformationFile;
 
 public class ForeignPartyFile extends ForeignInformationFile {
 
 	public ForeignPartyFile(UserId publisherId, UserMetadata publisherMetadata, Set<DigitalCard> digitalCards) {
 		super(publisherId, publisherMetadata, digitalCards);
 	}
 
 }
