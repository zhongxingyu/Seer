 package net.sf.anathema.character.abilities.sheet;
 
import java.awt.Color;
 import java.util.List;
 
 import net.sf.anathema.character.abilities.util.AbilitiesDisplayUtilties;
 import net.sf.anathema.character.core.character.ICharacter;
 import net.sf.anathema.character.trait.display.IDisplayTrait;
 import net.sf.anathema.character.trait.group.IDisplayTraitGroup;
 import net.sf.anathema.lib.util.Identificate;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
 
import com.lowagie.text.Font;
 import com.lowagie.text.pdf.BaseFont;
 
public class AbilitiesEncoder extends FavorableTraitEncoder implements IExecutableExtension{
  private static final BaseFont BASEFONT = new Font(Font.HELVETICA, 7, Font.NORMAL, Color.BLACK).getCalculatedBaseFont(true);
  
  public AbilitiesEncoder() {
    super(BASEFONT,
         new Identificate("Athletics"),
         new Identificate("Dodge"),
         new Identificate("Larceny"),
         new Identificate("Ride"),
         new Identificate("Stealth"));
 
   }
 
   @Override
   public String getHeader(ICharacter character) {
     return "Abilities";
   }
 
   @Override
   public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
       throws CoreException {
     // nothing to do
   }
 
   @Override
   protected List<IDisplayTraitGroup<IDisplayTrait>> getDisplayTraits(ICharacter character) {
     return AbilitiesDisplayUtilties.getDisplayAttributeGroups(character);
   }
 }
