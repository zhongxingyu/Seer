 package net.sf.anathema.charms.character.editor.combo;
 
 import net.disy.commons.core.model.listener.IChangeListener;
 import net.sf.anathema.charms.character.combo.IComboModel;
 import net.sf.anathema.charms.character.editor.ComboEditorInput;
 import net.sf.anathema.charms.data.INameMap;
 import net.sf.anathema.lib.ui.AggregatedDisposable;
 import net.sf.anathema.lib.ui.ChangeableModelDisposable;
 
 import org.eclipse.swt.widgets.Composite;
 
 public class ComboListControl {
 
   private final class UpdateLearnedCombos implements IChangeListener {
     private final Composite availableCombos;
 
     private UpdateLearnedCombos(Composite availableCombos) {
       this.availableCombos = availableCombos;
     }
 
     @Override
     public void stateChanged() {
       INameMap charmNameMap = editorInput.getCharmNameMap();
       LearnedComboViewFactory view = new LearnedComboViewFactory(availableCombos, charmNameMap);
       view.exchangeView(editorInput.getAllCombos());
     }
   }
 
   private final AggregatedDisposable disposables;
   private final ComboEditorInput editorInput;
 
   public ComboListControl(ComboEditorInput editorInput, AggregatedDisposable disposables) {
     this.editorInput = editorInput;
     this.disposables = disposables;
   }
 
   public void initControl(ComboInputWidgets widgets) {
     IChangeListener changeListener = new UpdateLearnedCombos(widgets.availableCombos);
     getComboModel().addChangeListener(changeListener);
     changeListener.stateChanged();
     disposables.addDisposable(new ChangeableModelDisposable(getComboModel(), changeListener));
   }
 
   private IComboModel getComboModel() {
     return editorInput.getItem();
   }
 }
