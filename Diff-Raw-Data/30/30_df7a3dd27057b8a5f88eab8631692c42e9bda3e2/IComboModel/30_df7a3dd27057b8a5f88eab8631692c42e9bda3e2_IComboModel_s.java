 package net.sf.anathema.charms.character.model;
 
 import net.sf.anathema.character.core.character.IModel;
 
 public interface IComboModel extends IModel {
 
   public void addExperienceLearned(Combo combo);
 
   public void addCreationLearned(Combo combo);
 
   public Iterable<Combo> getCreationLearned();
 
   public Iterable<Combo> getExperienceLearned();
 
   @Override
   public ComboModelMemento getSaveState();
 }
