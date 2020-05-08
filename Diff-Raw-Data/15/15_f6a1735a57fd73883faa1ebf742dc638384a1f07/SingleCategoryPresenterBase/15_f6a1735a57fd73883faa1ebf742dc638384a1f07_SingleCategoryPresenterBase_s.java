 package net.premereur.mvp.example.swing.categorymgt;
 
 import net.premereur.mvp.example.domain.model.Category;
 import net.premereur.mvp.example.domain.repository.CategoryRepository;
 
 /**
  * Base class for presenters that manage a single Category.
  * 
  * @author gpremer
  * 
  * @param <V> The type fo the view
  */
 public abstract class SingleCategoryPresenterBase<V extends CategoryPanelBase> extends CategoryPresenterBase<V> {
 
     public SingleCategoryPresenterBase(final CategoryMgtBus eventBus, final V view, final CategoryRepository repository) {
         super(eventBus, view, repository);
         view.setSaveButtonListener(this);
         view.setCancelButtonListener(this);
     }
 
    public void cancelClicked() {
         getEventBus().defaultCategoryPanelActivated();
     }
 
     public abstract void saveClicked(Category category);
 }
