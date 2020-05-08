 package hu.modembed.utils.compiler.module;
 
 import org.eclipse.emf.ecore.util.EcoreUtil;
 
 import hu.modembed.model.modembed.abstraction.types.PrimitiveTypeDefinition;
 import hu.modembed.model.modembed.abstraction.types.ReferenceTypeDefinition;
 import hu.modembed.model.modembed.abstraction.types.TypeDefinition;
 import hu.modembed.model.modembed.abstraction.types.TypesFactory;
 import hu.modembed.model.modembed.abstraction.types.UnsignedTypeDefinition;
 import hu.modembed.utils.compiler.TypeSignature;
 
 public final class TypeUtils {
 
 	private TypeUtils() {}
 
 	public static TypeDefinition createTypeForConstant(long constant){
 		int bits = 1;
		int maxv = 1;
 		while(constant > maxv){
 			bits++;
 			maxv *= 2;
 		}
 		return createUnsignedTypeDef(bits);
 	}
 	
 	public static TypeDefinition createUnsignedTypeDef(int bits){
 		UnsignedTypeDefinition utd = TypesFactory.eINSTANCE.createUnsignedTypeDefinition();
 		utd.setBits(bits);
 		return utd;
 	}
 	
 	public static TypeDefinition extend(TypeDefinition td1, TypeDefinition td2){
 		td1 = EcoreUtil.copy(td1);
 		td2 = EcoreUtil.copy(td2);
 		TypeDefinition rd1 = TypeSignature.raw(td1);
 		TypeDefinition rd2 = TypeSignature.raw(td2);
 		
 		if (rd1 instanceof UnsignedTypeDefinition && rd2 instanceof UnsignedTypeDefinition){
 			if (((UnsignedTypeDefinition)rd1).getBits() >= ((UnsignedTypeDefinition)rd2).getBits()){
 				return td1;
 			}else{
 				return td2;
 			}
 		}
 		
 		if (td1 == null) return td2;
 		
 		return td1;
 	}
 	
 	public static TypeDefinition getBaseType(TypeDefinition td){
 		if (td instanceof PrimitiveTypeDefinition) return td;
 		if (td instanceof ReferenceTypeDefinition) return ((ReferenceTypeDefinition) td).getType().getDefinition();
 		
 		// TODD
 		return td;
 	}
 	
 }
