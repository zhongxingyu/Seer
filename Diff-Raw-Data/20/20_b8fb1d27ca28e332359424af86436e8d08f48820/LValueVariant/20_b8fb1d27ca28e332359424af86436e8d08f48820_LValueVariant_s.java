 package zinara.ast.expression;
 
 import zinara.ast.type.Type;
 import zinara.ast.type.DictType;
 import zinara.ast.type.VariantType;
 import zinara.ast.type.ListType;
 import zinara.ast.type.IntType;
 import zinara.ast.type.FloatType;
 import zinara.ast.type.BoolType;
 import zinara.exceptions.KeyErrorException;
 import zinara.exceptions.TypeClashException;
 import zinara.exceptions.InvalidCodeException;
 import zinara.code_generator.Genx86;
 
 import java.io.IOException;
 
 public class LValueVariant extends LValue {
     private LValue constructor;
     private String identifier;
 
     // requires c.getType() be of VariantType
     public LValueVariant(LValue c, String i)
 	throws KeyErrorException, TypeClashException {
 	// check if i is in the dictionary
 	((VariantType)c.getType().getType()).getOrDie(i);
 
 	constructor = c;
 	identifier = i;
     }
 
     public Type getType() throws TypeClashException {
 	if (type != null) return type;
 	type = ((VariantType)constructor.getType().getType()).get(identifier);
 	return type;
     }
 
     public String toString() { return constructor + "." + identifier; }
 
     public void tox86(Genx86 generator)
 	throws IOException,InvalidCodeException{
 	constructor.register = register;
 	String constructorReg = generator.addrRegName(constructor.register);
 	String indexValue = generator.regName(constructor.register,type);
 
 	//Deja en constructorReg la direccion del LValue
 	currentDirection(generator);
 
 	generator.write(generator.mov(indexValue,
 				      "[" + constructorReg + "]",
 				      type.getType()
 				      )
 			);
     }
 
     public void currentDirection(Genx86 generator)
 	throws InvalidCodeException, IOException{
 	constructor.register = register;
 	String constructorReg = generator.addrRegName(constructor.register);
 
	constructor.tox86(generator);
 	try {
 	    Integer offset = ((VariantType)constructor.getType().getType()).getOffsetFor(identifier);
 	    generator.write(generator.add(constructorReg, offset.toString()));
 	} catch (TypeClashException e) {}
     }
 
     public boolean isStaticallyKnown() {
 	// for now,
 	return false;
     }
 
     public Object staticValue() { return null; };
 
     public boolean isConstant() { return constructor.isConstant(); }
 }
