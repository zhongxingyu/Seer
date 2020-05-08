 package edu.cornell.opencomm;
 
 import java.util.LinkedList;
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 
 /**
  * The main space for the entire application,
  * it has added functionality compared to the private spaces like
  * the ability to initialize everyone in the space
  * @author jnfrye
  *
  */
 class MainSpace extends PrivateSpace {
 	
 	public static final int MAINSPACE_BG = R.color.main_background;
 	Context context;
 	
 	
 	public MainSpace(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		this.context = context;
 		init(context);//Not the same init() as in Private space
 	}
 	
 	public MainSpace(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		this.context = context;
 		init(context);//Not the same init() as in Private space
 	}
 	
 	public MainSpace(Context context) {
 		super(context);
 		this.context = context;
 		init(context); //Not the same init() as in Private space
 	}
 	
 	private void init(Context context){
 
 	}
 	
 	/** Initialize everyone, intended only for the main chat.
 	 * Also, initialize a few private spaces for the demonstration */
 	public void initializeEveryone(){
 		// create all the people
 		Person nora = new Person("Nora", "She's the best!", R.drawable.nora);
 		Person najla = new Person("Najla", "Is dating Jack Sparrow" , R.drawable.naj);
 		Person makoto = new Person("Makoto", "Doesn't respond to texts", R.drawable.mak);
 		Person risa = new Person("Risa", "Is destined to marry her dog", R.drawable.risa);
 		
 		// the main conference room, but everybody in it
 		peopleInSpace = new LinkedList<Person>(); 
 		peopleInSpace.add(nora);
 		peopleInSpace.add(najla);
 		peopleInSpace.add(makoto);
 		peopleInSpace.add(risa);
 		
 		// create more private spaces, put some people in them
 		// p1 has makoto
 		// p2 has najla, risa
 		// p3 has najla, risa, nora
 		MainChat m = (MainChat)context;
 		PrivateSpace p1 = m.createNewPrivateSpace();
 		p1.add(makoto);
		PrivateSpace p2 = m.createNewPrivateSpace();
 		p2.add(najla);
 		p2.add(risa);
 		PrivateSpace p3 = m.createNewPrivateSpace();
 		p3.add(najla);
 		p3.add(risa);
		p3.add(nora);
		
		/*m.removePrivateSpace(p3);
		m.removePrivateSpace(p2);
		m.removePrivateSpace(p1); */
 	}
 
 	@Override
 	public int getColor() {
 		return MainSpace.MAINSPACE_BG;
 	}
 	
 	
 }
