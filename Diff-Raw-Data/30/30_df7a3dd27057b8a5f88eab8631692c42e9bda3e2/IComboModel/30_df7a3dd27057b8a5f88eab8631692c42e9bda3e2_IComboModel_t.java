 package net.sf.anathema.charms.character.model;
 
 import net.sf.anathema.character.core.character.IModel;
 
 public interface IComboModel extends IModel {
 
  public static final String MODEL_ID = "net.sf.anathema.charms.character.combo.model"; //$NON-NLS-1$

   public void addExperienceLearned(Combo combo);
 
   public void addCreationLearned(Combo combo);
 
   public Iterable<Combo> getCreationLearned();
 
   public Iterable<Combo> getExperienceLearned();
 
   @Override
   public ComboModelMemento getSaveState();
 }
