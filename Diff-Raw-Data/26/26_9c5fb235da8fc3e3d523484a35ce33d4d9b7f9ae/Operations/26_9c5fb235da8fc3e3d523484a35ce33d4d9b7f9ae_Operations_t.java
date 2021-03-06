 package ox.stackgame.stackmachine.instructions;
 
 import java.util.*;
 import java.lang.*;
 
 import ox.stackgame.stackmachine.StackMachine;
 import ox.stackgame.stackmachine.exceptions.StackRuntimeException;
 import ox.stackgame.stackmachine.exceptions.TypeException;
 
 import ox.stackgame.stackmachine.StackValue;
 import ox.stackgame.stackmachine.CharStackValue;
 import ox.stackgame.stackmachine.IntStackValue;
 
 public class Operations {
 	private static final Hashtable< String, Operation > ht = new Hashtable< String, Operation >();
 	private static boolean initialised = false;
 
 	private static void init() {
 		ht.put( "add", new BinOperation() {
 			public StackValue< ? > binop( StackValue< ? > x, StackValue< ? > y ) throws TypeException {
 				return x.add( y );
 			}
 		} );
 
 		ht.put( "sub", new BinOperation() {
 			public StackValue< ? > binop( StackValue< ? > x, StackValue< ? > y ) throws TypeException {
 				return x.sub( y );
 			}
 		} );
 
 		ht.put( "mul", new BinOperation() {
 			public StackValue< ? > binop( StackValue< ? > x, StackValue< ? > y ) throws TypeException {
 				return x.mul( y );
 			}
 		} );
 
 		ht.put( "div", new BinOperation() {
 			public StackValue< ? > binop( StackValue< ? > x, StackValue< ? > y ) throws TypeException {
 				return x.div( y );
 			}
 		} );
 
 		ht.put( "const", new SeqOperation() {
 			public void apply( StackMachine m, List< StackValue< ? > > args ) throws StackRuntimeException {
 				m.getStack().push( args.get( 0 ) );
 			}
 
			public List< Class< ? extends Object > > argTypes() {
				return ( List< Class< ? extends Object > > ) Arrays.asList( Integer.class, Character.class, String.class );
 			}
 		} );
 
 		ht.put( "load", new SeqOperation() {
 			public void apply( StackMachine m, List< StackValue< ? > > args ) throws StackRuntimeException {
 				m.getStack().push( m.getStore( ( Integer ) args.get( 0 ).getValue() ) );
 			}
 
			public List< Class< ? extends Object > > argTypes() {
				return ( List< Class< ? extends Object > > ) Arrays.asList( Integer.class );
 			}
 		} );
 
 		ht.put( "store", new SeqOperation() {
 			public void apply( StackMachine m, List< StackValue< ? > > args ) throws StackRuntimeException {
 				m.setStore( ( Integer ) args.get( 0 ).getValue(), m.getStack().pop() );
 			}
 
			public List< Class< ? extends Object > > argTypes() {
				return ( List< Class< ? extends Object > > ) Arrays.asList( Integer.class );
 			}
 		} );
 
 		ht.put( "label", new SeqOperation() {
 			public void apply( StackMachine m, List< StackValue< ? > > args ) throws StackRuntimeException {
 			}
 
			public List< Class< ? extends Object > > argTypes() {
				return ( List< Class< ? extends Object > > ) Arrays.asList( String.class );
 			}
 		} );
 	}
 
 	public static Operation get( String name ) {
 		if( !initialised ) {
 			init();
 		}
 
 		return ht.get( name );
 	}
 }
