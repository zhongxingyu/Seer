 package net.sf.beezle.ssass.scss;
 
 import net.sf.beezle.mork.misc.GenericException;
 import net.sf.beezle.sushi.util.Strings;
 
 public class Mixin implements Statement {
     private final String name;
     public final String[] variables;
     private final SsassDeclaration[] ssassDeclarations;
 
    public Mixin(String name, String[] variables, SsassDeclaration[] ssassDeclarations) {
        if (name == null) {
            if (variables.length != 1) {
                throw new IllegalStateException();
            }
            this.name = variables[0];
            this.variables = new String[0];
        } else {
            this.name = Strings.removeRight(name, "(");
            this.variables = variables;
        }
         this.ssassDeclarations = ssassDeclarations;
     }
 
     public String getName() {
         return name;
     }
 
     public SsassDeclaration[] getSsassDeclarations() {
         return ssassDeclarations;
     }
 
     @Override
     public void toCss(Output output) throws GenericException {
         output.defineMixin(this);
     }
 }
