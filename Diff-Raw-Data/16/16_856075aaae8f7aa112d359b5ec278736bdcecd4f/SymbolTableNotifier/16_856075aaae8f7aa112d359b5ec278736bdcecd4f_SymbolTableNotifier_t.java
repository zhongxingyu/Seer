 //
 // Overview 
 //
 //	A little wrapper class that notifies GUI component to reflect changes as
 //	symbol table is changed. 
 //
 // Purpose of This Class
 //
 //	Extending SymbolTable lets SymbolTable be as it is without having logic to
 //	interact with anything like GUI components. 
 //
 // Future Extension
 //
 //	Being an observable can notify other GUI components like Console.
 //	But, we cannot do multiple inheritance with Java. Need to make a mixin.
 //
 // TODO 
 //
 //	Symbol objects need to convert itself to some form GUI components
 //	understand. Probably, List type.
 //
 //  Think about Observable Observer classes offered by java.util.
 //  At least the interfaces since multiple inheritance is not allowed in Java.
 //
 //  Add Manifest
 //  
 //	What is the compilation warning about?
 //	"uses unchecked or unsafe operations"
 //
 //	Add to overview how this works
 //
 
 import java.util.Iterator;
 import java.util.ArrayList;
 import java.util.List;
 
 
 enum SymbolTableEvent {
 	scopePushedInParallel,
 	scopePushedAsChild,
 	scopePopped,
	symbolInserted,
 	symbolAssignedNewValue,
 };
 
 // ---------------------------------------------------------------------------
 // Workaround  : multiple inheritance 
 //
 //	SymbolTableNotifier cannot subclass both SymbolTable and Observable
 //
 
 interface CitrinObservable {
 	void notifyObservers(Object arg);		
 }
 
 interface CitrinObserver {
 	void update(CitrinObservable o, Object arg);
 };
 
 // ---------------------------------------------------------------------------
 
 class SymbolTableNotifier extends SymbolTable implements CitrinObservable {
 
 	private Symbol insertedSymbol = null;
 	private SymbolDiagnosis insertedSymbolDiagnosis = null;
	private ArrayList<CitrinObserver> observers = new ArrayList<CitrinObserver>();
 
 	// -----------------------------------------------------------------------
 	// Constructors
 
 	public SymbolTableNotifier()
 	{
 		super();
 	}
 
 	// -----------------------------------------------------------------------
 	// Functions For GUI Components To Pull Out Data From Symbol Table
 	
 	public Symbol getInsertedSymbol()
 	{
 		return insertedSymbol;
 	}
 
 	public SymbolDiagnosis getInsertedSymbolDiagnosis()
 	{
 		return insertedSymbolDiagnosis;
 	}
 
 	// -----------------------------------------------------------------------
 	// CitrinObserver Pattern Methods (Observable Side)
 	
 	public void notifyObservers(Object arg)
 	{ // Java Observable uses setChanged() and stuff
		Iterator<CitrinObserver> i = observers.iterator();
 		CitrinObserver o;
 		while (i.hasNext()) {
 			o = (CitrinObserver) i.next();
 			o.update(this, arg); // HERE this requires Observable,Object by Observer::update
 		}
 	}
 
 	public void addObserver(CitrinObserver o)
 	{
 		observers.add(o);
 	}
 
 	public void removeObserver(CitrinObserver o)
 	{
 		System.out.println("todo removeObserver() ");
 	}
 
 	// -----------------------------------------------------------------------
 	// Symbol Table Functions That Need to Notify GUI components
 
 	public void pushFuncScope(String funcName){
 		super.pushFuncScope(funcName);
 
 		// notify here
 		SymbolTableEvent event;
 		notifyObservers(SymbolTableEvent.scopePushedInParallel);
 	}
 
 	public void pushLocalScope(){
 		super.pushLocalScope();
 		// notify here
		notifyObservers(SymbolTableEvent.scopePushedAsChild);
 	}
 
 	public void popScope(){
 		super.popScope();
 		// notify
 		notifyObservers(SymbolTableEvent.scopePopped);
 	}
 
 	public SymbolDiagnosis pushSymbol(Symbol s){
 		// Store data for data display
 		insertedSymbol = s;
 		insertedSymbolDiagnosis = super.pushSymbol(s);
 		
 		// notify SymbolTable if no conflict happens (should be compilation error)
		// notify console if conflict happens - interpreter will catch it during prescan so don't notify the console
		notifyObservers( SymbolTableEvent.symbolInserted );
 
 		return insertedSymbolDiagnosis;
 
 	}
 
 	void assignVar(String varName, var_type value){
 		super.assignVar(varName, value);
 		// notify
 		notifyObservers( SymbolTableEvent.symbolAssignedNewValue);
 	}
 
 }
 
