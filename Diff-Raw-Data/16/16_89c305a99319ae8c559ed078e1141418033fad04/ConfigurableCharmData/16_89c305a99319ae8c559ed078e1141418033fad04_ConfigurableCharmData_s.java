 package net.sf.anathema.charmentry.model;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.sf.anathema.character.generic.impl.magic.CharmAttribute;
 import net.sf.anathema.character.generic.impl.magic.CharmAttributeRequirement;
 import net.sf.anathema.character.generic.impl.magic.charm.type.CharmTypeModel;
 import net.sf.anathema.character.generic.impl.rules.ExaltedEdition;
 import net.sf.anathema.character.generic.magic.ICharm;
 import net.sf.anathema.character.generic.magic.charms.Duration;
 import net.sf.anathema.character.generic.magic.charms.ICharmAttribute;
 import net.sf.anathema.character.generic.magic.charms.ICharmAttributeRequirement;
 import net.sf.anathema.character.generic.magic.charms.IComboRestrictions;
import net.sf.anathema.character.generic.magic.charms.type.CharmType;
 import net.sf.anathema.character.generic.traits.IGenericTrait;
 import net.sf.anathema.character.generic.traits.ITraitType;
 import net.sf.anathema.character.generic.traits.types.OtherTraitType;
 import net.sf.anathema.character.generic.traits.types.ValuedTraitType;
 import net.sf.anathema.character.generic.type.CharacterType;
 import net.sf.anathema.lib.collection.ListOrderedSet;
 import net.sf.anathema.lib.exception.NotYetImplementedException;
 import net.sf.anathema.lib.util.IIdentificate;
 
 public class ConfigurableCharmData implements IConfigurableCharmData {
 
   private CharacterType characterType;
   private String id;
   private Duration duration;
   private IGenericTrait essence = new ValuedTraitType(OtherTraitType.Essence, 1);
   private Map<ITraitType, IGenericTrait> prerequisitesByType = new LinkedHashMap<ITraitType, IGenericTrait>();
   private String groupId;
   private Set<ICharm> parentCharms = new ListOrderedSet<ICharm>();
   private String name;
   private IConfigurableCostList temporaryCost = new ConfigurableCostList();
   private IConfigurablePermanentCostList permanentCost = new ConfigurablePermanentCostList();
   private IConfigurableMagicSource source = new ConfigurableMagicSource();
   private ITraitType primaryType;
   private ExaltedEdition edition;
   private final List<ICharmAttribute> keywords = new ArrayList<ICharmAttribute>();
   private final CharmTypeModel model = new CharmTypeModel();
   private boolean excellencyRequired;
 
   public void setCharacterType(CharacterType type) {
     this.characterType = type;
   }
 
   public CharacterType getCharacterType() {
     return characterType;
   }
 
   public void setId(String id) {
     this.id = id;
   }
 
   public String getId() {
     return id;
   }
 
   public IConfigurablePermanentCostList getPermanentCost() {
     return permanentCost;
   }
 
   public IConfigurableMagicSource getSource() {
     return source;
   }
 
   public IConfigurableCostList getTemporaryCost() {
     return temporaryCost;
   }
 
   public Duration getDuration() {
     return duration;
   }
 
   public IGenericTrait getEssence() {
     return essence;
   }
 
   public IGenericTrait[] getPrerequisites() {
     return prerequisitesByType.values().toArray(new IGenericTrait[0]);
   }
 
   public IGenericTrait getPrerequisiteByType(ITraitType type) {
     return prerequisitesByType.get(type);
   }
 
   public String getGroupId() {
     return groupId;
   }
 
   public Set<ICharm> getParentCharms() {
     return parentCharms;
   }
 
   public void setDuration(Duration duration) {
     this.duration = duration;
   }
 
  public void setCharmType(CharmType type) {
    model.setCharmType(type);
  }

   public void setEssencePrerequisite(IGenericTrait prerequisite) {
     this.essence = prerequisite;
   }
 
   public void addPrerequisite(IGenericTrait prerequisite) {
     prerequisitesByType.put(prerequisite.getType(), prerequisite);
   }
 
   public void setGroupId(String id) {
     this.groupId = id;
   }
 
   public void removePrerequisite(IGenericTrait unwanted) {
     ITraitType type = unwanted.getType();
     if (type == primaryType) {
       throw new IllegalArgumentException("Must not remove primary prerequisite!"); //$NON-NLS-1$
     }
     removePrerequisiteByType(type);
   }
 
   private void removePrerequisiteByType(ITraitType type) {
     prerequisitesByType.remove(type);
   }
 
   public String getName() {
     return name;
   }
 
   public void setName(String newName) {
     this.name = newName;
   }
 
   public IComboRestrictions getComboRules() {
     throw new NotYetImplementedException();
   }
 
   public void setPrimaryPrerequisite(IGenericTrait prerequisite) {
     removePrerequisiteByType(primaryType);
     this.primaryType = prerequisite.getType();
     if (prerequisite.getType() != null) {
       LinkedHashMap<ITraitType, IGenericTrait> map = new LinkedHashMap<ITraitType, IGenericTrait>();
       map.put(primaryType, prerequisite);
       map.putAll(prerequisitesByType);
       prerequisitesByType = map;
     }
   }
 
   public ITraitType getPrimaryPrerequisiteType() {
     return primaryType;
   }
 
   public void clearPrimaryPrerequisite() {
     setPrimaryPrerequisite(ValuedTraitType.NULL_TYPE);
   }
 
   public IGenericTrait getPrimaryPrerequiste() {
     return getPrerequisiteByType(primaryType);
   }
 
   public void setParentCharms(ICharm[] charms) {
     parentCharms.clear();
     Collections.addAll(parentCharms, charms);
   }
 
   public void setEdition(ExaltedEdition edition) {
     this.edition = edition;
   }
 
   public ExaltedEdition getEdition() {
     return edition;
   }
 
   public void addKeyword(IIdentificate newValue) {
     keywords.add(new CharmAttribute(newValue.getId(), true));
   }
 
   public CharmTypeModel getCharmTypeModel() {
     return model;
   }
 
   public void setExcellencyRequired(boolean required) {
     this.excellencyRequired = required;
   }
 
   public ICharmAttributeRequirement[] getAttributeRequirements() {
     if (!excellencyRequired) {
       return new ICharmAttributeRequirement[0];
     }
     return new ICharmAttributeRequirement[] { new CharmAttributeRequirement(new CharmAttribute("Excellency" //$NON-NLS-1$
         + primaryType.getId(), false), 1) };
   }
 }
