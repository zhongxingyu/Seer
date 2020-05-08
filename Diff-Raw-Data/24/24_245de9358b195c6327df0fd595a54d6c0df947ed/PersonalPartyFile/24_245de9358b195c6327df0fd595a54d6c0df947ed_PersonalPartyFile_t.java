 package dacus.model.dossier;
 
 import java.util.Set;
 
import deus.model.common.dossier.DigitalCard;
 import deus.model.pifgoverning.PersonalInformationFile;
 
 public class PersonalPartyFile extends PersonalInformationFile {
 
 	public PersonalPartyFile(Set<DigitalCard> digitalCards) {
 		super(digitalCards);
 	}
 
 }
