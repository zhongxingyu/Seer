 package dacus.model.dossier;
 
 import java.util.Set;
 
import deus.model.dc.DigitalCard;
 import deus.model.pifgoverning.PersonalInformationFile;
 
 public class PersonalPartyFile extends PersonalInformationFile {
 
 	public PersonalPartyFile(Set<DigitalCard> digitalCards) {
 		super(digitalCards);
 	}
 
 }
