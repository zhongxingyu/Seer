 package sum.isszy;
 
 import sum.isszy.dialog.IInitialDialog;
 
 /**
  *the entry point class for the Isszy Image Sorter program.<BR>
  *This is the entry point for the Isszy program, also referred to as the
  *<I>main</I> class or <I>driver</I> class.
  */
 public class Isszy
 {
 	
	public static final String VERSION = "2.1";
    
     public static void main(String[]args)
     {
 
 	if(IsszyPrefs.getFirstStart())
 	    { 
 		IInitialDialog start = new IInitialDialog(); 
 		start.setVisible(true);
 	    }
 	
 
 	IsszyGui main = new IsszyGui();
 	main.setVisible(true);
     }
 }
