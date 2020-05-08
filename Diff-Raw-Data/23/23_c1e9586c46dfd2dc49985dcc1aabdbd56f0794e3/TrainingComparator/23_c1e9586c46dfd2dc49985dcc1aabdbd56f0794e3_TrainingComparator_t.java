 // %535933150:hoplugins.trainingExperience.ui%
 /*
  * Created on 20-mar-2005
  *
  * To change the template for this generated file go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 package ho.module.training.ui;
 
 import ho.core.model.UserParameter;
 
 import java.util.Comparator;
 import java.util.Vector;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author Mirtillo To change the template for this generated type comment go to
  *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 public class TrainingComparator implements Comparator<Vector<String>> {
     //~ Methods ------------------------------------------------------------------------------------
    private int first;

    TrainingComparator(int firstColumn) {
        first = firstColumn;
    }
 
     /* (non-Javadoc)
      * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
      */
     @Override
     public int compare(Vector<String> arg0, Vector<String> arg1) {
         Vector<String> v1 =  arg0;
         Vector<String> v2 =  arg1;
 
        for (int i = first; i < (UserParameter.instance().futureWeeks + first); i++) {
             String s1 = "" + v1.get(i); //$NON-NLS-1$
             String s2 = "" + v2.get(i); //$NON-NLS-1$
 
             if (!s1.equalsIgnoreCase("") && s2.equalsIgnoreCase("")) { //$NON-NLS-1$ //$NON-NLS-2$
                 return -1;
             } else if (s1.equalsIgnoreCase("") && !s2.equalsIgnoreCase("")) { //$NON-NLS-1$ //$NON-NLS-2$
                 return 1;
             }
         }
 
         return 1;
     }
 }
