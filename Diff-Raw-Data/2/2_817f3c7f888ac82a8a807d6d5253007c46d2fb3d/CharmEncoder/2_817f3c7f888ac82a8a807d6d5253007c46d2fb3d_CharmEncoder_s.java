 package net.sf.anathema.charms.character.sheet;
 
import static net.sf.anathema.charms.providing.CharmProvidingExtensionPoint.*;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 
 import net.sf.anathema.basics.eclipse.extension.UnconfiguredExecutableExtension;
 import net.sf.anathema.character.core.character.ICharacter;
 import net.sf.anathema.character.sheet.common.IEncodeContext;
 import net.sf.anathema.character.sheet.common.IPdfContentBoxEncoder;
 import net.sf.anathema.character.sheet.elements.Bounds;
 import net.sf.anathema.charms.character.evaluation.CharmCharacter;
 import net.sf.anathema.charms.character.print.GenericCharmCollector;
 import net.sf.anathema.charms.character.sheet.generic.GenericDisplayId;
 import net.sf.anathema.charms.character.sheet.stats.CharmStats;
 import net.sf.anathema.charms.character.sheet.stats.IMagicStats;
 import net.sf.anathema.charms.data.CharmDto;
 import net.sf.anathema.charms.data.ICharmDataMap;
 import net.sf.anathema.charms.display.DisplayCharm;
 import net.sf.anathema.charms.tree.ICharmId;
 
 import com.lowagie.text.DocumentException;
 import com.lowagie.text.pdf.PdfContentByte;
 
 public class CharmEncoder extends UnconfiguredExecutableExtension implements IPdfContentBoxEncoder {
 
   @Override
   public String getHeader(ICharacter character) {
     return Messages.CharmEncoder_Title;
   }
 
   @Override
   public void encode(PdfContentByte directContent, IEncodeContext context, ICharacter character, Bounds bounds)
       throws DocumentException {
     Set<ICharmId> learnedCharms = new CharmCharacter(character).getAllLearnedCharms();
     List<IMagicStats> stats = createPrintStats(learnedCharms, character);
     new MagicTableEncoder(context.getBaseFont(), stats).encode(directContent, character, bounds);
   }
 
   private List<IMagicStats> createPrintStats(Collection<ICharmId> learnedCharms, ICharacter character) {
     final List<IMagicStats> printStats = new ArrayList<IMagicStats>();
     ICharmDataMap map = CreateCharmDataMap();
     GenericCharmCollector collector = new GenericCharmCollector(character);
     List<ICharmId> learnedGenerics = collector.getLearnedGenerics();
     Collection<String> genericIdPatterns = collector.getRealGenericIdPatterns();
     for (String id : genericIdPatterns) {
       printStats.add(createGenericStats(map, new GenericDisplayId(character, id)));
     }
     for (ICharmId id : learnedCharms) {
       if (learnedGenerics.contains(id)) {
         continue;
       }
       printStats.add(createPrintStats(map, id));
     }
     return printStats;
   }
 
   private CharmStats createPrintStats(ICharmDataMap map, ICharmId id) {
     CharmDto data = map.getData(id);
     DisplayCharm charm = new DisplayCharm(data);
     return new CharmStats(id, charm);
   }
 
   private CharmStats createGenericStats(ICharmDataMap map, ICharmId id) {
     CharmDto data = map.getData(id);
     DisplayCharm charm = new DisplayCharm(data);
     return new GenericStats(id, charm);
   }
 }
