 package ch.opentrainingcenter.model.navigation;
 
 import java.util.Date;
 
 import ch.opentrainingcenter.core.helper.TimeHelper;
 import ch.opentrainingcenter.transfer.IImported;
 
 public class ConcreteImported extends ImportedDecorator implements INavigationItem, IImported {
 
     public ConcreteImported(final IImported imported) {
         super(imported);
     }
 
     @Override
     public String getName() {
         return TimeHelper.convertDateToString(super.getActivityId(), false);
     }
 
     @Override
     public Date getDate() {
         return super.getActivityId();
     }
 
     @Override
     public String getImage() {
         return super.getTrainingType().getImageicon();
     }
 
     @Override
     public int compareTo(final INavigationItem o) {
         return o.getDate().compareTo(getDate());
     }
 
     @Override
     public String toString() {
         return "ConcreteImported [getName()=" + getName() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
     }
 }
