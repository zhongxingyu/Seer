 package net.sf.anathema.character.spiritualtraits.validator;
 
 import static net.sf.anathema.character.spiritualtraits.plugin.IPluginConstants.*;
 import static net.sf.anathema.character.trait.validator.ValidationDtoObjectMother.*;
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 import net.sf.anathema.character.spiritualtraits.model.SpiritualTraitGroupTemplate;
 import net.sf.anathema.character.spiritualtraits.virtues.Virtues;
 import net.sf.anathema.character.trait.IBasicTrait;
 import net.sf.anathema.character.trait.collection.ITraitCollectionModel;
 import net.sf.anathema.character.trait.collection.TraitCollectionModelFactory;
 import net.sf.anathema.character.trait.group.TraitGroup;
 import net.sf.anathema.character.trait.validator.where.ValidationDto;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class VirtueSumMaximumValidator_Test {
 
   private ValidationDto validationDto;
   private VirtueSumMaximumValidator validator;
   private ITraitCollectionModel collection;
 
   @Before
   public void createValidationDtoWithCreation1Virtues() throws Exception {
     TraitGroup[] groups = new SpiritualTraitGroupTemplate().getGroups();
     collection = TraitCollectionModelFactory.create(groups);
     for (IBasicTrait virtue : new Virtues(collection).getTraits()) {
       virtue.getCreationModel().setValue(1);
     }
     this.validationDto = ForModel(MODEL_ID, collection);
   }
 
   @Before
   public void createValivdator() throws Exception {
     validator = new VirtueSumMaximumValidator();
   }
 
   @Test
   public void allowsValueOf7ForLowVirtues() throws Exception {
     validator.initValidation(validationDto);
     assertThat(validator.getValidValue(7), is(7));
   }
 
   @Test
   public void onlyAllowsValueOf8ForLowVirtues0() throws Exception {
     validator.initValidation(validationDto);
     assertThat(validator.getValidValue(9), is(8));
   }
 }
