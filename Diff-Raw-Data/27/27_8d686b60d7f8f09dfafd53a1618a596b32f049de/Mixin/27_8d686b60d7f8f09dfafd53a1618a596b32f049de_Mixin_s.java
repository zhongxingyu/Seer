 package net.sf.beezle.ssass.scss;
 
 import net.sf.beezle.mork.misc.GenericException;
 import net.sf.beezle.sushi.util.Strings;
 
 public class Mixin implements Statement {
     private final String name;
     public final String[] variables;
     private final SsassDeclaration[] ssassDeclarations;
 
    public Mixin(String[] nameAndVariables, SsassDeclaration[] ssassDeclarations) {
        this.name = nameAndVariables[0];
        this.variables = Strings.cdr(nameAndVariables);
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
