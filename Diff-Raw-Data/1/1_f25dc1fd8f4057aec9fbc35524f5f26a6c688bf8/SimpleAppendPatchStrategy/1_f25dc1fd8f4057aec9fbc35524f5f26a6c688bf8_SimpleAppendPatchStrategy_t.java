 package deus.core.soul.difgoverning.impl;
 
 import org.springframework.stereotype.Component;
 
 import deus.model.common.dossier.AppendDigitalCardPatch;
 import deus.model.common.dossier.Patch;
 import deus.model.difgoverning.ForeignInformationFile;
 
 /**
  * This strategy only handles patches of type <code>AppendDigitalCardPatch</code>.
  * 
  * @author Florian Rampp (Florian.Rampp@informatik.stud.uni-erlangen.de)
  *
  */
// TODO: configure this in spring xml
 @Component
 public class SimpleAppendPatchStrategy implements PatchStrategy {
 
 	@Override
 	public void patch(ForeignInformationFile fif, Patch patch) {
 		if(!(patch instanceof AppendDigitalCardPatch))
 			throw new RuntimeException("this patch strategy can only handle patches of type AppendDigitalCardPatch");
 		
 		fif.getDigitalCards().add(((AppendDigitalCardPatch)patch).getDigitalCardToAppend());
 	}
 
 }
