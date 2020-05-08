 /*
  * Copyright (c) 2010 by Guido Steinacker
  */
 
 package de.steinacker.jcg.transform.type;
 
 import de.steinacker.jcg.Context;
 import de.steinacker.jcg.model.*;
 import de.steinacker.jcg.util.DefaultFormatStringProvider;
 import de.steinacker.jcg.util.FormatStringProvider;
 import de.steinacker.jcg.util.NameUtil;
 import org.apache.log4j.Logger;
 
 /**
 * A TypeTransformer which adds setter methods for all attributes of the different types.
  * <p/>
  *
  * @author Guido Steinacker
  * @version %version: 28 %
  */
 public final class AddSetters extends AbstractFieldToMethodTransformer implements TypeTransformer {
 
     private final static Logger LOG = Logger.getLogger(AddSetters.class);
 
     private FormatStringProvider formatStringProvider = new DefaultFormatStringProvider();
 
     /**
      * Inject a FormatStringProvider implementation used to generate method bodies for setters,
      * getters and constructors.
      *
      * @param provider the FormatStringProvider
      */
     public void setFormatStringProvider(final FormatStringProvider provider) {
         this.formatStringProvider = provider;
     }
 
     @Override
     public String getName() {
         return "AddSetters";
     }
 
     @Override
     protected Method transformFieldToMethod(final Field field, final Context context) {
         // no setters for final or static fields:
         if (field.is(FieldModifier.FINAL) || field.is(FieldModifier.STATIC))
             return null;
 
         final MethodBuilder mb = new MethodBuilder();
         // Alle Getter sind public:
         mb.addModifier(MethodModifier.PUBLIC);
         // Wenn die Klasse final ist, mssen es die Methoden nicht sein:
         if (!getType(context).getModifiers().contains(TypeModifier.FINAL))
             mb.addModifier(MethodModifier.FINAL);
         // Der Name der Methode:
         final String fieldName = field.getName().toString();
         mb.setName(SimpleName.valueOf("set" + NameUtil.toCamelHumpName(fieldName, true)));
         // Der Parameter fr den setter
         mb.addParameter(new ParameterBuilder()
                 .setComment(field.getTypeName().getSimpleName().toString())
                 .setFinal(true)
                 .setName(field.getName())
                 .setTypeName(field.getTypeName())
                 .toParameter());
         // Method body:
         final String formatString = formatStringProvider.getFormatForSetter(field.getTypeName());
         final String code = String.format(formatString, field.getName(), field.getName());
         mb.setMethodBody(code);
 
         /*
     // TODO Der Sourcecode:
     mb.setBody(CodeUtil.indent("return " + field.getName() + ";"));
     // TODO Kommentar nicht vergessen:
     final StringBuilder sb = new StringBuilder();
     sb.append(field.getComment());
     sb.append("\n").append("@return ").append(field.getName());
     mb.setComment(CodeUtil.toJavaDocComment(sb.toString()));
     */
         return mb.toMethod();
     }
 
     @Override
     public String toString() {
         return getName();
     }
 
 }
