 package frontend.functions;
 
 import java.io.PrintStream;
 import frontend.*;
 
 public class LessThanFunction extends Function {
   public static final String NAME = "<";
   public LessThanFunction(){
     super(NAME, new Type[] { Type.IntType, Type.IntType } );
   }
   public Variable<BoolData> compile_checked( Variable<IntTypeData>[] args, Statement owner ) throws CompileException {
     PrintStream ps = ProgramTree.output;
     boolean signed = false;
     for( int i = 0; i < args.length; i++){
       if( args[i].getData().signed() ){
 	signed = true;
       }
     }
     Variable<BoolData> out = new Variable<BoolData>( IntTypeData.lessthan( args[0].getData(), args[1].getData() ) );
     String op = signed ? "lts" : "ltu";
     if( !out.getData().is_constant() ) {
       int len = Variable.maxArgLength( args );
       String[] actual_args = Variable.padArgsToLength( args, len );
       ps.print( out.new_name() + " " + op + " ");
      ps.print( actual_args[1] + " " + actual_args[0] );
       ps.println();
     }
     return out;
   }
   public Variable compile_func( Variable[] args, Statement owner ) throws CompileException{
     return compile_checked( (Variable<IntTypeData>[]) args, owner );
   }
 }
